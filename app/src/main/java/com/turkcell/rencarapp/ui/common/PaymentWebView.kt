package com.turkcell.rencarapp.ui.common

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
fun WebView.applyPaymentPageSettings() {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        useWideViewPort = true
        loadWithOverviewMode = true
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        textZoom = 100
        defaultTextEncodingName = "UTF-8"
        loadsImagesAutomatically = true
        cacheMode = WebSettings.LOAD_DEFAULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            safeBrowsingEnabled = true
        }
    }
    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = false
    overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
    scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    setBackgroundColor(Color.WHITE)
}

fun WebView.ensureMobileViewport() {
    evaluateJavascript(
        """
        (function() {
            var viewport = document.querySelector('meta[name="viewport"]');
            if (!viewport) {
                viewport = document.createElement('meta');
                viewport.name = 'viewport';
                document.head.appendChild(viewport);
            }
            viewport.content = 'width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=3.0, user-scalable=yes, viewport-fit=cover';
            document.body.style.margin = '0';
            document.body.style.padding = '0';
            document.documentElement.style.webkitTextSizeAdjust = '100%';
        })();
        """.trimIndent(),
        null,
    )
}
