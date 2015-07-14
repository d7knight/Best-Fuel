package com.example.cs446project.bestfuel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.cs446project.bestfuel.app.AppConfig;

import com.example.cs446project.bestfuel.helper.MyLocation;
import com.example.cs446project.bestfuel.helper.SQLiteHandler;
import com.example.cs446project.bestfuel.helper.StationAlgorithm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MapActivity extends Activity {
    static WebAppInterface waInterface;
    Boolean saCreated=false;
    StationAlgorithm sa;
    Location location;

    LocationManager locationManager;
    Context mContext;
    String mode;
    String preferences;
    private SQLiteHandler db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext=this;
        mode = getIntent().getExtras().getString("mode");
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

        MapActivity.clearCache(mContext,0);

        WebView webview=(WebView)findViewById(R.id.webkit);
        webview.clearCache(true);
        webview.getSettings().setJavaScriptEnabled(true);
        //Inject WebAppInterface methods into Web page by having Interface name 'Android'
        waInterface = new WebAppInterface(this, webview);
        webview.addJavascriptInterface(waInterface, "Android");
        //Load URL inside WebView


        Log.d("Main", "Main activity loaded");

        db = new SQLiteHandler(getApplicationContext());


        if(saCreated==false) {
            MapActivity curAct = this;
            sa = new StationAlgorithm(curAct, db);
            saCreated = true;
        }

        Map<String, String> noCacheHeaders = new HashMap<String, String>(2);
        noCacheHeaders.put("Pragma", "no-cache");
        noCacheHeaders.put("Cache-Control", "no-cache");
        webview.loadUrl(AppConfig.URL_MAP,noCacheHeaders);





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

    public JSONObject grabPrefs(){
        HashMap<String, String> user = db.getUserDetails();
        ContentValues prefs = db.getPrefs(user.get("name"));
        JSONObject prefObj = new JSONObject();
        ArrayList<String> prefList = new ArrayList<String>();
        if(prefs.getAsBoolean("airport")){
            prefList.add("airport");
        }
        if(prefs.getAsBoolean("atm")){
            prefList.add("atm");
        }
        if(prefs.getAsBoolean("bakery")){
            prefList.add("bakery");
        }
        if(prefs.getAsBoolean("bank")){
            prefList.add("bank");
        }
        if(prefs.getAsBoolean("bar")){
            prefList.add("bar");
        }
        if(prefs.getAsBoolean("cafe")){
            prefList.add("cafe");
        }
        if(prefs.getAsBoolean("car_dealer")){
            prefList.add("car_dealer");
        }
        if(prefs.getAsBoolean("car_wash")){
            prefList.add("car_wash");
        }
        if(prefs.getAsBoolean("convenience_store")){
            prefList.add("convenience_store");
        }
        if(prefs.getAsBoolean("food")){
            prefList.add("food");
        }
        if(prefs.getAsBoolean("hospital")){
            prefList.add("hospital");
        }
        if(prefs.getAsBoolean("liquor_store")){
            prefList.add("liquor_store");
        }
        if(prefs.getAsBoolean("lodging")){
            prefList.add("lodging");
        }
        if(prefs.getAsBoolean("meal_delivery")){
            prefList.add("meal_delivery");
        }
        if(prefs.getAsBoolean("park")){
            prefList.add("park");
        }
        if(prefs.getAsBoolean("parking")){
            prefList.add("parking");
        }
        if(prefs.getAsBoolean("restaurant")){
            prefList.add("restaurant");
        }
        if(prefs.getAsBoolean("shopping_mall")){
            prefList.add("shopping_mall");
        }


        try {
            prefObj.put("price", prefs.getAsInteger("price"));
            prefObj.put("open", prefs.getAsBoolean("open_only"));
            prefObj.put("fuel_type", prefs.getAsInteger("fuel_type"));
            prefObj.put("places", new JSONArray(prefList));


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return prefObj;
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
                        webview.loadUrl("javascript:sendMode('" + mode + "')");
                        webview.loadUrl("javascript:sendPlacePreferences('" + preferences + "')");
                    }
                });

            }

        }

        @JavascriptInterface
        public void switchToProfile() {
            Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
            startActivity(intent);
        }

        public JSONObject getUserPrefs(){
            JSONObject prefs=grabPrefs();
            return prefs;
        }
    }
    //helper method for clearCache() , recursive
//returns number of deleted files
    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            }
            catch(Exception e) {
                Log.e("map", String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    /*
     * Delete the files older than numDays days from the application cache
     * 0 means all files.
     */
    public static void clearCache(final Context context, final int numDays) {
        Log.i("MAP", String.format("Starting cache prune, deleting files older than %d days", numDays));
        int numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays);
        Log.i("MAP", String.format("Cache pruning completed, %d files deleted", numDeletedFiles));
    }


}
