package com.example.rtpc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.rtpc.ui.HomeScreen
import com.example.rtpc.ui.Screen
import com.example.rtpc.ui.CameraScanScreen
import com.example.rtpc.ui.PdfMergeScreen
import com.example.rtpc.ui.OcrScreen
import com.example.rtpc.ui.PdfToolsScreen
import com.example.rtpc.ui.AnalyticsScreen
import com.example.rtpc.ui.theme.RTPCTheme
import androidx.navigation3.runtime.NavBackStack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RTPCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RTPCApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Suppress("UNCHECKED_CAST")
@Composable
fun RTPCApp() {
    val backStack = rememberNavBackStack(Screen.Home) as NavBackStack<Screen>
    val strategy = rememberListDetailSceneStrategy<Screen>()

    NavDisplay(
        backStack = backStack,
        sceneStrategy = strategy,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
        entryProvider = { key ->
            when (key) {
                is Screen.Home -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.listPane()
                ) {
                    HomeScreen(onNavigate = { backStack.add(it) })
                }

                is Screen.CameraScan -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    CameraScanScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }

                is Screen.PdfMerge -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    PdfMergeScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }

                is Screen.Ocr -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    OcrScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }

                is Screen.PdfTools -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    PdfToolsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }

                is Screen.Analytics -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    AnalyticsScreen(onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) })
                }
            }
        }
    )
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
