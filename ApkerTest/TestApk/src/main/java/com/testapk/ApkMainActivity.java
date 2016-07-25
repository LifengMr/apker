package com.testapk;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


/**
 * Created by chenlifeng on 16/5/18.
 */
public class ApkMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("clf", "!!!!ApkMainActivity onCreate1");
        super.onCreate(savedInstanceState);

        Log.i("clf", "!!!!ApkMainActivity appname=" + getString(R.string.app_name));
        View view = LayoutInflater.from(this).inflate(R.layout.activity_apk, null);
        Log.i("clf", "!!!!ApkMainActivity onCreate view=" + view);
        setContentView(view);
        Log.i("clf", "!!!!ApkMainActivity onCreate 2");
//        WebView webView = (WebView) findViewById(R.id.webview);
//        Log.i("clf", "!!!!ApkMainActivity webView=" + webView);
//        webView.loadUrl("http://www.baidu.com");
    }
}
