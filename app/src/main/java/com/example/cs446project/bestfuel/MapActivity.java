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

import com.example.cs446project.bestfuel.helper.StationAlgorithm;

import java.security.Provider;


public class MapActivity extends Activity {
    static WebAppInterface waInterface;
    Boolean saCreated=false;
    StationAlgorithm sa;
    Location location;

    String myProvider;
    LocationManager locationManager;
    LocationListener locationListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_map);

        // Don't initialize location manager, retrieve it from system services.
         locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        requestGPS(locationManager);

       locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(MapActivity.this,
                        "Provider enabled: " + provider, Toast.LENGTH_SHORT)
                        .show();
                myProvider=provider;
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MapActivity.this,
                        "Provider disabled: " + provider, Toast.LENGTH_SHORT)
                        .show();
                myProvider="";
            }

            @Override
            public void onLocationChanged(Location location) {
                // Do work with new location. Implementation of this method will be covered later.
                doWorkWithNewLocation(location);
            }
        };

        long minTime = 5 * 1000; // Minimum time interval for update in seconds, i.e. 5 seconds.
        long minDistance = 10; // Minimum distance change for update in meters, i.e. 10 meters.

// Assign LocationListener to LocationManager in order to receive location updates.
// Acquiring provider that is used for location updates will also be covered later.
// Instead of LocationListener, PendingIntent can be assigned, also instead of
// provider name, criteria can be used, but we won't use those approaches now.
        locationManager.requestLocationUpdates(getProviderName(), minTime,
                minDistance, locationListener);


        WebView webview=(WebView)findViewById(R.id.webkit);
        webview.getSettings().setJavaScriptEnabled(true);
        //Inject WebAppInterface methods into Web page by having Interface name 'Android'
        waInterface = new WebAppInterface(this, webview);
        webview.addJavascriptInterface(waInterface, "Android");
        //Load URL inside WebView

        myProvider = LocationManager.NETWORK_PROVIDER;

        // Returns last known location, this is the fastest way to get a location fix.
        Location fastLocation = locationManager.getLastKnownLocation(myProvider);


        if(saCreated==false) {
            MapActivity curAct = this;
            sa = new StationAlgorithm(curAct);
            saCreated = true;
        }
        webview.clearCache(true);
        webview.loadUrl(AppConfig.URL_MAP);
    }
    /**
     * Get provider name.
     * @return Name of best suiting provider.
     * */
    String getProviderName() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        criteria.setSpeedRequired(false); // Chose if speed for first location fix is required.
        criteria.setAltitudeRequired(false); // Choose if you use altitude.
        criteria.setBearingRequired(false); // Choose if you use bearing.
        criteria.setCostAllowed(false); // Choose if this provider can waste money :-)

        // Provide your criteria and flag enabledOnly that tells
        // LocationManager only to return active providers.
        return locationManager.getBestProvider(criteria, true);
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

    /**
     * Make use of location after deciding if it is better than previous one.
     *
     * @param newlocation Newly acquired location.
     */
    void doWorkWithNewLocation(Location newlocation) {
        if(isBetterLocation(location, newlocation)){
            // If location is better, do some user preview.
            Toast.makeText(MapActivity.this,
                    "Better location found: " + myProvider, Toast.LENGTH_SHORT)
                    .show();
        }

        location=newlocation;
    }

    /**
     * Time difference threshold set for one minute.
     */
    static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;

    /**
     * Decide if new location is better than older by following some basic criteria.
     * This algorithm can be as simple or complicated as your needs dictate it.
     * Try experimenting and get your best location strategy algorithm.
     *
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            return true;
        }

        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();
        if(isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else if(isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if(timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }

        return false;
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

            webview.loadUrl("javascript:sendStationResult(" + result + "," + bestStation + ")");
        }

        @JavascriptInterface
        public void findBestStation(double lat, double lon){
            sa.findBestStation(lat, lon);
        }

        //call this from the map to request current location of user
        @JavascriptInterface
        public void requestCurLocation(){

            Log.d("GPS","Android side gps request received");
            sendCurLocation();
        }

        @JavascriptInterface
        public void sendCurLocation(){


            if(location==null){
                Log.d("gps","error");
            }
            else{
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
              //  Toast.makeText(mContext, "Gps data "+latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                webview.loadUrl("javascript:sendCurLocation(" + latitude + "," + longitude + ")");
            }
           locationManager.removeUpdates(locationListener);
        }
    }


}
