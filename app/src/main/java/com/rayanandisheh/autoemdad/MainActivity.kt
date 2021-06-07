package com.rayanandisheh.autoemdad

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var lastUrl:String
    var initialLoading = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        try_again.setOnClickListener {
            no_connection.visibility = View.GONE
            loading.visibility = View.VISIBLE
            webview.loadUrl(lastUrl)
        }

        webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null){
                    if(url.startsWith("https://autoemdad.com/")){
                        loading.visibility = View.VISIBLE
                        lastUrl = url
                        view?.loadUrl(url)
                    } else {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        view?.loadUrl("https://autoemdad.com/")
                    }
                }

                return true
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                if (url != null) {
                    Log.e("onLoadResource", url)
                }
                super.onLoadResource(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (url != null) {
                    Log.e("onPageStarted", url)
                    loading.visibility = View.VISIBLE
                }
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null) {
                    Log.e("onPageFinished", url)
                    lastUrl = url
                    injectCSS()
                    if (!initialLoading){
                        splash.visibility = View.GONE
                    }
                    loading.visibility = View.GONE
                }
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                no_connection.visibility = View.VISIBLE
                super.onReceivedError(view, request, error)
            }
        }

        webview.settings.javaScriptEnabled = true

        webview.loadUrl("https://autoemdad.com/")
    }

    private fun injectCSS() {
        try
        {
            val css = "footer{display:none !important;}"
            val encoded = Base64.encodeToString(css.toByteArray(), Base64.NO_WRAP)

            webview.loadUrl(("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()"))
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (!loading.isVisible){
            if (webview.canGoBack()) {
                loading.visibility = View.VISIBLE
                webview.goBack()
            } else {
                finish()
            }
        }
    }
}