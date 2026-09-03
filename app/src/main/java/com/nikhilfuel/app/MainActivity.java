package com.nikhilfuel.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;

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

        webView.setWebViewClient(new WebViewClient() {
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
    }

    private boolean handleExternalUrl(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        String host = uri.getHost();

        // Keep the app fully offline/local, but hand WhatsApp links to Android
        // instead of trying to load wa.me inside the WebView.
        if ("https".equalsIgnoreCase(scheme) && "wa.me".equalsIgnoreCase(host)) {
            String text = uri.getQueryParameter("text");
            if (text == null) text = "";
            openWhatsApp(text);
            return true;
        }

        // Do not allow accidental external web pages to replace the app.
        if ("file".equalsIgnoreCase(scheme) || "about".equalsIgnoreCase(scheme)) {
            return false;
        }
        return true;
    }

    private void openWhatsApp(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        // Prefer the normal WhatsApp app, then WhatsApp Business.
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

        // If neither WhatsApp app is installed, show Android's share chooser.
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

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
