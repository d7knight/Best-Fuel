package com.example.cs446project.bestfuel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;



public class ProfileActivity extends Activity {

    CQInterface cq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_profile);

        //testing carquery stuff
        WebView webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        cq = new CQInterface(this, webview);
        webview.addJavascriptInterface(cq, "Android");

        webview.loadUrl("file:///android_asset/carquery.html");

        Log.d("CQ", "past init");

    }

    public void testCall(View v){
        Log.d("CQ", "running test call");
        Object retObj=null;
        //getYears(retObj);
        cq.getMakes(2009);
    }

    public class CQInterface {
        Context context;
        WebView webview;

        CQInterface(Context c, WebView wb) {
            context = c;
            webview = wb;


        }

        @JavascriptInterface
        public void testCall(View v){
            Log.d("CQ2", "running test call");
            Object retObj=null;
            getMakes(2009);
        }

        @JavascriptInterface
        public void getYears(){
            webview.loadUrl("javascript:getYears()");
        }

        @JavascriptInterface
        public void getYearsRet(String retObj) {
            Log.d("CQ", "the actual retobj is " + retObj);
        }

        @JavascriptInterface
        public void initd(String str) {
            Log.d("CQ", "done initializing "+ str);
        }

        @JavascriptInterface
        public void getMakes(int year){
            webview.loadUrl("javascript:getMakes(" + year + ")");
        }

        @JavascriptInterface
        public void getMakesRet(String retObj) {
            Log.d("CQ", "the actual retobj for makes is " + retObj);
        }

    }

}
