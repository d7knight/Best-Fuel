package com.example.cs446project.bestfuel;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class MapActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        WebView webview=(WebView)findViewById(R.id.webView);
        webview.loadUrl("file:///android_asset/test.html");
    }


}
