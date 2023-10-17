package com.meeting.webview;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebUtils {

    @SuppressLint("SetJavaScriptEnabled")
    public static void configureWebView(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setMixedContentMode(0);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(webSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else if(Build.VERSION.SDK_INT >=11 && Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
    }
}