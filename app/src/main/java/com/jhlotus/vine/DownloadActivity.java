package com.jhlotus.vine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class DownloadActivity extends AppCompatActivity {


    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        WebView wv = findViewById(R.id.webview_download);

        WebSettings webSettings = wv.getSettings();
        //与js交互必须设置
        webSettings.setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/html.html");
        wv.addJavascriptInterface(this,"android");


        wv.loadUrl("https://www.jhlotus.com/other/index/download");
    }

    @JavascriptInterface
    public void jsCallAndroid(){
       finish();
    }
}
