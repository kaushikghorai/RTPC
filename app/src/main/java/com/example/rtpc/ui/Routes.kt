package com.example.rtpc.ui

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Home : Screen

    @Serializable
    data object CameraScan : Screen

    @Serializable
    data object PdfMerge : Screen
}
