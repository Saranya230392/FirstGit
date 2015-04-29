package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import com.android.settings.R;

public class RcseNotiView extends Activity {
    private String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rcs_e_notice_detail);
        Intent intent = getIntent();
        URL = intent.getStringExtra("url");
        WebView webview = (WebView)findViewById(R.id.noti_detail);
        try {
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setDefaultTextEncodingName("Euc-kr");

        } catch (NullPointerException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        webview.setWebViewClient(new WebViewClient() {

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

        });
        //webview.setWebViewClient(new WebViewClient());
        webview.loadUrl(URL);
    }

}
