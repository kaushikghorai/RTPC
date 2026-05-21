package com.example.rtpc.ui

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CameraScanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var tempPdfUri by remember { mutableStateOf<Uri?>(null) }

    val saverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { destinationUri ->
            if (destinationUri != null && tempPdfUri != null) {
                isSaving = true
                scope.launch {
                    val success = withContext(Dispatchers.IO) {
                        try {
                            context.contentResolver.openInputStream(tempPdfUri!!)?.use { inputStream ->
                                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            false
                        }
                    }
                    isSaving = false
                    if (success) {
                        Toast.makeText(context, "PDF saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                    }
                    onBack()
                }
            } else {
                onBack()
            }
        }
    )

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scanningResult = com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                tempPdfUri = scanningResult?.pdf?.uri
                if (tempPdfUri != null) {
                    saverLauncher.launch("SCAN_PDF.pdf")
                } else {
                    onBack()
                }
            } else {
                onBack()
            }
        }
    )

    LaunchedEffect(Unit) {
        val activity = context as? Activity
        if (activity == null) {
            Toast.makeText(context, "Activity context not found", Toast.LENGTH_SHORT).show()
            onBack()
            return@LaunchedEffect
        }

        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setResultFormats(RESULT_FORMAT_PDF)
            .setGalleryImportAllowed(true)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to start scanner: ${it.message}", Toast.LENGTH_SHORT).show()
                onBack()
            }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isSaving) {
            CircularProgressIndicator()
        }
    }
}
