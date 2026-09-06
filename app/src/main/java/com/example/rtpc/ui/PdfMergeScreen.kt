package com.example.rtpc.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rtpc.utils.PageSource
import com.example.rtpc.data.AppDatabase
import com.example.rtpc.data.ScannedDocument
import com.example.rtpc.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class MergeState {
    object Selection : MergeState()
    object Processing : MergeState()
    data class PageSelection(val allPages: List<PageSource>) : MergeState()
    object Saving : MergeState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfMergeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var mergeState by remember { mutableStateOf<MergeState>(MergeState.Selection) }
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedPageSources by remember { mutableStateOf<List<PageSource>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedFiles = (selectedFiles + uris).distinct()
            }
        }
    )

    val saverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri != null) {
                mergeState = MergeState.Saving
                scope.launch {
                    val success = withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            PdfGenerator.mergeSelectedPages(context, selectedPageSources, outputStream)
                        } ?: false
                    }
                    if (success) {
                        Toast.makeText(context, "PDF saved successfully", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            AppDatabase.getDatabase(context).documentDao().insertDocument(
                                ScannedDocument(
                                    name = uri.lastPathSegment ?: "Merged PDF",
                                    uri = uri.toString(),
                                    type = "MERGE"
                                )
                            )
                        }
                        onBack()
                    } else {
                        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                        mergeState = MergeState.Selection // Fallback
                    }
                }
            } else {
                // If user cancels saving, go back to page selection
                // (or stay where we were)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (mergeState) {
                            is MergeState.Selection -> "Select Documents"
                            is MergeState.PageSelection -> "Select Pages"
                            else -> "Processing..."
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (mergeState is MergeState.PageSelection) {
                            mergeState = MergeState.Selection
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            when (mergeState) {
                is MergeState.Selection -> {
                    if (selectedFiles.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                mergeState = MergeState.Processing
                                scope.launch {
                                    val allPages = withContext(Dispatchers.IO) {
                                        val pagesList = mutableListOf<PageSource>()
                                        selectedFiles.forEach { uri ->
                                            val mimeType = context.contentResolver.getType(uri) ?: ""
                                            val finalUri = when {
                                                mimeType == "application/pdf" -> uri
                                                mimeType.startsWith("image/") -> PdfGenerator.convertImageToPdf(context, uri)
                                                mimeType == "text/plain" -> PdfGenerator.convertTxtToPdf(context, uri)
                                                else -> null // Office docs unsupported locally for now
                                            }
                                            
                                            if (finalUri != null) {
                                                pagesList.addAll(PdfGenerator.getPdfPages(context, finalUri))
                                            }
                                        }
                                        pagesList
                                    }
                                    if (allPages.isEmpty()) {
                                        Toast.makeText(context, "No valid pages found", Toast.LENGTH_SHORT).show()
                                        mergeState = MergeState.Selection
                                    } else {
                                        mergeState = MergeState.PageSelection(allPages)
                                        // Default select all
                                        selectedPageSources = allPages
                                    }
                                }
                            },
                            icon = { Icon(Icons.Rounded.ChevronRight, null) },
                            text = { Text("Next: Select Pages") }
                        )
                    } else {
                        FloatingActionButton(onClick = { 
                            filePickerLauncher.launch(arrayOf("application/pdf", "image/*", "text/plain"))
                        }) {
                            Icon(Icons.Rounded.Add, "Add Files")
                        }
                    }
                }
                is MergeState.PageSelection -> {
                    if (selectedPageSources.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                saverLauncher.launch("${timestamp}_RTPC.pdf")
                            },
                            icon = { Icon(Icons.Rounded.Check, null) },
                            text = { Text("Save PDF (${selectedPageSources.size} pages)") }
                        )
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = mergeState) {
                is MergeState.Selection -> {
                    SelectionUI(
                        selectedFiles = selectedFiles,
                        onAddFiles = { filePickerLauncher.launch(arrayOf("application/pdf", "image/*", "text/plain")) },
                        onRemoveFile = { selectedFiles = selectedFiles - it }
                    )
                }
                is MergeState.PageSelection -> {
                    PageSelectionUI(
                        allPages = state.allPages,
                        selectedPages = selectedPageSources,
                        onTogglePage = { page ->
                            selectedPageSources = if (selectedPageSources.contains(page)) {
                                selectedPageSources - page
                            } else {
                                (selectedPageSources + page).sortedBy { state.allPages.indexOf(it) }
                            }
                        }
                    )
                }
                is MergeState.Processing, MergeState.Saving -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(if (state is MergeState.Processing) "Converting and extracting pages..." else "Saving final PDF...")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionUI(
    selectedFiles: List<Uri>,
    onAddFiles: () -> Unit,
    onRemoveFile: (Uri) -> Unit
) {
    val context = LocalContext.current
    if (selectedFiles.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PictureAsPdf, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("No files selected", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Select PDF, Images, or Text files to merge them into a single PDF.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddFiles) {
                Icon(Icons.Rounded.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Files")
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedFiles) { uri ->
                val fileName = uri.path?.split("/")?.last() ?: "Unknown File"
                val mimeType = context.contentResolver.getType(uri) ?: ""
                
                ListItem(
                    headlineContent = { Text(fileName, maxLines = 1) },
                    supportingContent = { Text(mimeType) },
                    leadingContent = {
                        Icon(
                            when {
                                mimeType == "application/pdf" -> Icons.Rounded.PictureAsPdf
                                mimeType.startsWith("image/") -> Icons.Rounded.Image
                                else -> Icons.Rounded.Description
                            },
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { onRemoveFile(uri) }) {
                            Icon(Icons.Rounded.Close, null)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )
            }
            item {
                TextButton(onClick = onAddFiles, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add more files")
                }
            }
        }
    }
}

@Composable
fun PageSelectionUI(
    allPages: List<PageSource>,
    selectedPages: List<PageSource>,
    onTogglePage: (PageSource) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(allPages) { page ->
            val isSelected = selectedPages.contains(page)
            Box(
                modifier = Modifier
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onTogglePage(page) }
            ) {
                if (page.preview != null) {
                    Image(
                        bitmap = page.preview.asImageBitmap(),
                        contentDescription = "Page ${page.pageIndex + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                
                // Selection badge
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.3f),
                    contentColor = Color.White
                ) {
                    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                        } else {
                            Text("${allPages.indexOf(page) + 1}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                // Page Info
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Text(
                        "Page ${page.pageIndex + 1}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
