package com.example.cs446project.bestfuel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.cs446project.bestfuel.helper.StationAlgorithm;
import com.example.cs446project.bestfuel.helper.StationData;


public class MapActivity extends Activity {
    static WebAppInterface waInterface;
    Boolean saCreated=false;
    StationAlgorithm sa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_map);
        WebView webview=(WebView)findViewById(R.id.webkit);
        webview.getSettings().setJavaScriptEnabled(true);
        //Inject WebAppInterface methods into Web page by having Interface name 'Android'
        waInterface = new WebAppInterface(this, webview);
        webview.addJavascriptInterface(waInterface, "Android");
        //Load URL inside WebView


        if(saCreated==false) {
            MapActivity curAct = this;
            sa = new StationAlgorithm(curAct);
            saCreated = true;
        }

        webview.loadUrl("file:///android_asset/map.html");
    }

    public void stationBypass(int bestStation){
        waInterface.sendStationResult(this.sa.result, bestStation);
    }


    //Class to be injected in Web page
    public class WebAppInterface {
        Context mContext;
        WebView webview;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c, WebView webview) {
            mContext = c;
            this.webview=webview;
        }

        /**
         * Show Toast Message
         * @param toast
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        /**
         * Show Dialog
         * @param dialogMsg
         */
        @JavascriptInterface
        public void showDialog(String dialogMsg){
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

            // Setting Dialog Title
            alertDialog.setTitle("JS triggered Dialog");

            // Setting Dialog Message
            alertDialog.setMessage(dialogMsg);

            // Setting alert dialog icon
            //alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

            // Setting OK Button
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(mContext, "Dialog dismissed!", Toast.LENGTH_SHORT).show();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }

        /**
         * Intent - Move to next screen
         */
        @JavascriptInterface
        public void moveToNextScreen(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            // Setting Dialog Title
            alertDialog.setTitle("Alert");
            // Setting Dialog Message
            alertDialog.setMessage("Are you sure you want to leave to next screen?");
            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Move to Next screen
                       //     Intent chnIntent = new Intent(MapActivity.this, ChennaiIntent.class);
                       //     startActivity(chnIntent);
                        }
                    });
            // Setting Negative "NO" Button
            alertDialog.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel Dialog
                            dialog.cancel();
                        }
                    });
            // Showing Alert Message
            alertDialog.show();
        }

        @JavascriptInterface
        public void sendStationResult(String result, int bestStation){
            //send it to the map javascript

            webview.loadUrl("javascript:sendStationResult("+result+","+bestStation+")");
        }

        @JavascriptInterface
        public void findBestStation(double lat, double lon){
            sa.findBestStation(lat, lon);
        }
    }


}
