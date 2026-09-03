package com.nikhilfuel.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;

    public class AndroidBridge {
        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(MainActivity.this, message == null ? "" : message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
                String darkJs = dark ? "true" : "false";

                view.evaluateJavascript(
                    "(function(){" +
                    "var t=document.getElementById('themeBtn');if(t)t.remove();" +
                    "try{localStorage.removeItem('npTheme')}catch(e){}" +
                    "document.body.classList.toggle('light',!" + darkJs + ");" +
                    "window.showToast=function(message,type,duration){" +
                    "message=String(message==null?'':message);" +
                    "try{if(window.AndroidBridge&&AndroidBridge.showToast){AndroidBridge.showToast(message);return}}catch(e){}" +
                    "var el=document.getElementById('npToast');if(!el)return;" +
                    "if(window.npToastTimer)clearTimeout(window.npToastTimer);" +
                    "el.textContent=message;el.className='show '+(type||'success');" +
                    "window.npToastTimer=setTimeout(function(){el.className=''},duration||2200);" +
                    "};" +
                    "var s=document.getElementById('android-header-alignment-fix');" +
                    "if(!s){s=document.createElement('style');s.id='android-header-alignment-fix';" +
                    "s.textContent='.header-actions{display:flex!important;align-items:center!important;justify-content:flex-end!important;gap:0!important}.header-actions .refresh-icon-btn{margin:0!important;flex:0 0 40px!important;width:40px!important;min-width:40px!important;height:40px!important;min-height:40px!important;padding:0!important;box-sizing:border-box!important;align-self:center!important;display:inline-flex!important;align-items:center!important;justify-content:center!important;line-height:1!important}@media(max-width:520px){.header-actions .refresh-icon-btn{flex-basis:40px!important;width:40px!important;min-width:40px!important;height:40px!important;min-height:40px!important}}';document.head.appendChild(s)}" +
                    "})()",
                    null
                );
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleExternalUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleExternalUrl(Uri.parse(url));
            }
        });
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.loadUrl("file:///android_asset/index.html");

        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                new android.window.OnBackInvokedCallback() {
                    @Override public void onBackInvoked() {
                        handleBackPress();
                    }
                }
            );
        }
    }

    private boolean handleExternalUrl(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        String host = uri.getHost();

        if ("https".equalsIgnoreCase(scheme) && "wa.me".equalsIgnoreCase(host)) {
            String text = uri.getQueryParameter("text");
            if (text == null) text = "";
            openWhatsApp(text);
            return true;
        }

        if ("file".equalsIgnoreCase(scheme) || "about".equalsIgnoreCase(scheme)) {
            return false;
        }
        return true;
    }

    private void openWhatsApp(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        if (isPackageInstalled("com.whatsapp")) {
            intent.setPackage("com.whatsapp");
            try {
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException ignored) { }
        }

        if (isPackageInstalled("com.whatsapp.w4b")) {
            intent.setPackage("com.whatsapp.w4b");
            try {
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException ignored) { }
        }

        intent.setPackage(null);
        try {
            startActivity(Intent.createChooser(intent, "Share report"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleBackPress() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }

        webView.evaluateJavascript(
            "(function(){" +
            "var p=document.getElementById('historyPopup');" +
            "if(p&&getComputedStyle(p).display!=='none'){" +
            "if(typeof window.closeHistoryPopup==='function')window.closeHistoryPopup();else p.style.display='none';return 'modal';}" +
            "var m=document.getElementById('segmentHistoryOverlay');" +
            "if(m&&m.classList.contains('show')){if(typeof window.closeSegmentHistory==='function')window.closeSegmentHistory();else m.classList.remove('show');return 'modal';}" +
            "m=document.getElementById('denomHistoryOverlay');" +
            "if(m&&m.classList.contains('show')){if(typeof window.closeDenomHistory==='function')window.closeDenomHistory();else m.classList.remove('show');return 'modal';}" +
            "m=document.getElementById('termsOverlay');" +
            "if(m&&m.classList.contains('show')){if(typeof window.closeTerms==='function')window.closeTerms();else m.classList.remove('show');return 'modal';}" +
            "m=document.getElementById('appInfo');" +
            "if(m&&m.classList.contains('show')){if(typeof window.closeAppInfo==='function')window.closeAppInfo();else m.classList.remove('show');return 'modal';}" +
            "return 'none';})()",
            new ValueCallback<String>() {
                @Override public void onReceiveValue(String value) {
                    if (!"\"modal\"".equals(value)) {
                        if (webView.canGoBack()) webView.goBack();
                        else MainActivity.super.onBackPressed();
                    }
                }
            }
        );
    }

    @Override public void onBackPressed() {
        if (Build.VERSION.SDK_INT < 33) {
            handleBackPress();
        }
    }
}
