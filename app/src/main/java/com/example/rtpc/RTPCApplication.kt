package com.example.rtpc

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class RTPCApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
    }
}
