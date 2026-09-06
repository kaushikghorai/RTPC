package com.example.rtpc.ui

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MergeType
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.rtpc.ui.theme.RTPCTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class Feature(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val description: String,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    
    var documents by remember { mutableStateOf(getRecentDocuments(context)) }
    var documentToDelete by remember { mutableStateOf<File?>(null) }

    // Refresh list when screen is shown
    LaunchedEffect(Unit) {
        documents = getRecentDocuments(context)
    }

    val features = listOf(
        Feature(
            "Camera Scan", 
            Icons.Rounded.CameraAlt, 
            Screen.CameraScan, 
            "Snap to PDF",
            listOf(colorScheme.primary, colorScheme.primaryContainer)
        ),
        Feature(
            "Merge PDFs", 
            Icons.AutoMirrored.Rounded.MergeType, 
            Screen.PdfMerge, 
            "Combine Files",
            listOf(colorScheme.tertiary, colorScheme.tertiaryContainer)
        ),
        Feature(
            "OCR Tool",
            Icons.Rounded.TextFormat,
            Screen.Ocr,
            "Extract Text",
            listOf(colorScheme.secondary, colorScheme.secondaryContainer)
        ),
        Feature(
            "PDF Tools",
            Icons.Rounded.Build,
            Screen.PdfTools,
            "Security & More",
            listOf(colorScheme.error, colorScheme.errorContainer)
        ),
        Feature(
            "Analytics",
            Icons.Rounded.Insights,
            Screen.Analytics,
            "Usage Stats",
            listOf(Color(0xFF4CAF50), Color(0xFFC8E6C9))
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Bolt, 
                            contentDescription = null, 
                            tint = colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "RTPC", 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surface,
                            colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        "Raw To PDF Convertor",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        "Effortless document management at your fingertips.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1: Camera and Merge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FeatureCard(feature = features[0], onClick = { onNavigate(features[0].screen) })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FeatureCard(feature = features[1], onClick = { onNavigate(features[1].screen) })
                        }
                    }
                    
                    // Row 2: OCR and PDF Tools
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FeatureCard(feature = features[2], onClick = { onNavigate(features[2].screen) })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FeatureCard(feature = features[3], onClick = { onNavigate(features[3].screen) })
                        }
                    }

                    // Row 3: Analytics (Single wide or half)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FeatureCard(feature = features[4], onClick = { onNavigate(features[4].screen) })
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Text(
                    "Recent Documents",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            if (documents.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(documents) { file ->
                    DocumentItem(
                        file = file,
                        onShare = { shareFile(context, file) },
                        onDelete = { documentToDelete = file }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        documentToDelete?.let { file ->
            AlertDialog(
                onDismissRequest = { documentToDelete = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            file.delete()
                            documents = getRecentDocuments(context)
                            documentToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { documentToDelete = null }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Delete Document") },
                text = { Text("Are you sure you want to delete '${file.name}'?") }
            )
        }
    }
}

@Composable
fun FeatureCard(
    feature: Feature,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                feature.gradientColors[1].copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = feature.gradientColors[0].copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = feature.title,
                            modifier = Modifier.size(24.dp),
                            tint = feature.gradientColors[0]
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DocumentItem(
    file: File,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val fileSize = String.format(Locale.US, "%.2f MB", file.length().toDouble() / (1024 * 1024))
    val lastModified = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))

    ListItem(
        headlineContent = { Text(file.name, maxLines = 1) },
        supportingContent = { Text("$fileSize • $lastModified") },
        leadingContent = {
            Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Row {
                IconButton(onClick = onShare) {
                    Icon(Icons.Rounded.Share, contentDescription = "Share")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No PDFs found in RTPC folder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getRecentDocuments(context: Context): List<File> {
    val allFiles = mutableListOf<File>()
    
    // 1. App-specific Documents folder (Always accessible)
    val appDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    if (appDir != null && appDir.exists()) {
        appDir.listFiles { file -> file.extension.lowercase() == "pdf" }?.let {
            allFiles.addAll(it)
        }
    }

    // 2. Public Documents/RTPC folder
    // Note: On Android 11+, we can only list files we created or if we have special permission.
    // If the app created them, they should show up here.
    val publicDocDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "RTPC")
    if (publicDocDir.exists() && publicDocDir.isDirectory) {
        publicDocDir.listFiles { file -> file.extension.lowercase() == "pdf" }?.let {
            allFiles.addAll(it)
        }
    } else {
        // Try to create it so it's available for saving
        try { publicDocDir.mkdirs() } catch (e: Exception) {}
    }
    
    return allFiles.distinctBy { it.absolutePath }.sortedByDescending { it.lastModified() }
}

private fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share PDF"))
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RTPCTheme {
        HomeScreen(onNavigate = {})
    }
}
