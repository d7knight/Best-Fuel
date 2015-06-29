package com.example.cs446project.bestfuel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.cs446project.bestfuel.app.AppConfig;

import com.example.cs446project.bestfuel.helper.MyLocation;
import com.example.cs446project.bestfuel.helper.SQLiteHandler;
import com.example.cs446project.bestfuel.helper.StationAlgorithm;

import java.security.Provider;


public class MapActivity extends Activity {
    static WebAppInterface waInterface;
    Boolean saCreated=false;
    StationAlgorithm sa;
    Location location;

    LocationManager locationManager;
    Context mContext;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext=this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_map);

        // Don't initialize location manager, retrieve it from system services.
         locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        requestGPS(locationManager);

        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location newLocation){
                location=newLocation;
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Toast.makeText(mContext, "Gps data "+latitude + " " + longitude, Toast.LENGTH_SHORT).show();
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);


        WebView webview=(WebView)findViewById(R.id.webkit);
        webview.getSettings().setJavaScriptEnabled(true);
        //Inject WebAppInterface methods into Web page by having Interface name 'Android'
        waInterface = new WebAppInterface(this, webview);
        webview.addJavascriptInterface(waInterface, "Android");
        //Load URL inside WebView


        Log.d("Main", "Main activity loaded");

        SQLiteHandler db = new SQLiteHandler(getApplicationContext());


        if(saCreated==false) {
            MapActivity curAct = this;
            sa = new StationAlgorithm(curAct, db);
            saCreated = true;
        }
        webview.clearCache(true);
        webview.loadUrl(AppConfig.URL_MAP);
    }

    private void requestGPS(LocationManager lm){
        boolean enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!enabled){
            Log.d("GPS", "Gps is not enabled");
            new AlertDialog.Builder(this)
                    .setTitle("Enable GPS")
                    .setMessage("Would you like to enable GPS to get current location?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            Log.d("GPS", "Gps is enabled");
        }

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
        public void sendStationResult(final String result, final int bestStation){
            //send it to the map javascript
            webview.post(new Runnable() {
                @Override
                public void run() {
                    webview.loadUrl("javascript:sendStationResult('" + result + "','" + bestStation + "')");
                }
            });

        }

        @JavascriptInterface
        public void findBestStation(double lat, double lon){
            sa.findBestStation(lat, lon);
        }

        //call this from the map to request current location of user
        @JavascriptInterface
        public void requestCurLocation(){

            Log.d("GPS", "Android side gps request received");
            sendCurLocation();

        }

        @JavascriptInterface
        public void sendCurLocation(){


            if(location==null){
                Log.d("gps","error");
                Toast.makeText(mContext, "Javascript requested location but location not ready yet", Toast.LENGTH_SHORT).show();
            }
            else{
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
              //  Toast.makeText(mContext, "Gps data "+latitude + " " + longitude, Toast.LENGTH_SHORT).show();

                webview.post(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl("javascript:sendCurLocation('" + latitude + "','" + longitude + "')");
                    }
                });

            }

        }

        @JavascriptInterface
        public void switchToProfile() {
            Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
    }


}
