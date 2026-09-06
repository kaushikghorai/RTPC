package com.example.rtpc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CallMerge
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rtpc.data.AppDatabase
import com.example.rtpc.data.ScannedDocument
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val documents by database.documentDao().getAllDocuments().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            StatsSection(documents)
            
            HorizontalDivider()
            
            Text(
                "Scanning History",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
            
            if (documents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No documents recorded yet", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(documents) { doc ->
                        DocumentHistoryItem(doc, onDelete = {
                            // scope.launch { database.documentDao().deleteDocument(doc.id) }
                            // For safety, we just delete from list in a real app, 
                            // here we actually delete from DB
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun StatsSection(documents: List<ScannedDocument>) {
    val scanCount = documents.count { it.type == "SCAN" }
    val mergeCount = documents.count { it.type == "MERGE" }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard("Total", documents.size.toString(), Icons.Rounded.History, Modifier.weight(1f))
        StatCard("Scans", scanCount.toString(), Icons.Rounded.CameraAlt, Modifier.weight(1f))
        StatCard("Merges", mergeCount.toString(), Icons.Rounded.CallMerge, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun DocumentHistoryItem(document: ScannedDocument, onDelete: () -> Unit) {
    val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(document.timestamp))
    ListItem(
        headlineContent = { Text(document.name) },
        supportingContent = { Text("$date • ${document.type}") },
        leadingContent = {
            Icon(
                if (document.type == "SCAN") Icons.Rounded.CameraAlt else Icons.Rounded.CallMerge,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    )
}
