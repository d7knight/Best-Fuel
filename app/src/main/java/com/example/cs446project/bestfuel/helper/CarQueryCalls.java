package com.example.cs446project.bestfuel.helper;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.example.cs446project.bestfuel.R;

/**
 * Created by Kyle on 20/05/2015.
 */
public class CarQueryCalls {

    private WebView webview;
    private Context context;

    //initialize
    //to call this use: webview.addJavaScriptInterface(new CarQueryCalls(webview, context), "Android");
    public CarQueryCalls (WebView webview, Context c){
        //you must create webview in profile activity then send it to me
        this.webview = webview;
        webview.getSettings().setJavaScriptEnabled(true);
        this.context=c;
    }

    @JavascriptInterface
    private void initJS() {
        this.webview.loadUrl("javasript:cqjs('functionname')");//general outline
    }

    @JavascriptInterface
    public int getYear(){

        return 1;
    }


}
