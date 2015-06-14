package com.example.cs446project.bestfuel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;


import com.example.cs446project.bestfuel.helper.SQLiteHandler;
import com.example.cs446project.bestfuel.helper.SessionManager;
import com.example.cs446project.bestfuel.helper.StationAlgorithm;

import java.util.HashMap;


public class ProfileActivity extends Activity {

    private TextView txtInfo;
    private SQLiteHandler db;
    private SessionManager session;

    CQInterface cq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_profile);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        //Profile Data ========================================
        String name = user.get("name");
        String email = user.get("email");

        TextView nameTxt = (TextView) findViewById(R.id.profileName);
        nameTxt.setText(name);

        TextView emailTxt = (TextView) findViewById(R.id.profileEmail);
        emailTxt.setText(email);

        //Profile end ========================================


        android.widget.Spinner dropdown = (android.widget.Spinner)findViewById(R.id.year);
        String[] items = new String[]{"2015", "2014", "2013", "2012", "2011", "2010", "2009", "2008", "2007", "2006", "2005", "2004", "2003", "2002",
                "2001", "2000", "1999", "1998", "1997", "1996", "1995", "1994", "1993", "1992", "1991", "1990", "1989", "1988", "1987", "1986", "1985"
                , "1984", "1983", "1982", "1981", "1980", "1979", "1978", "1977", "1976", "1975", "1974", "1973", "1972", "1971", "1970", "1969", "1968"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);

        //testing carquery stuff
        WebView webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        cq = new CQInterface(this, webview);
        webview.addJavascriptInterface(cq, "Android");

        webview.loadUrl("file:///android_asset/carquery.html");

        Log.d("CQ", "past init");
    }

    //storing years in the database so we don't have to wait for loading
    private void storeYears(){

    }

    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void testCall(View v){
        Log.d("CQ", "running test call");
        Object retObj=null;
        //getYears(retObj);
        //cq.getMakes(2009);
        //temp stuff for testing
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
        public void initd(String str) {
            Log.d("CQ", "done initializing " + str);
        }

        //returns the range of all possible years for vehicles
        @JavascriptInterface
        public void getYears(){
            webview.loadUrl("javascript:getYears()");
        }

        //RETURNS: JSON String {"min_year":"#", "max_year":"#"}
        @JavascriptInterface
        public void getYearsRet(String retObj) {

            Log.d("CQ", "the actual retobj is " + retObj);
        }

        //returns all the possible makes in a specified year
        @JavascriptInterface
        public void getMakes(int year){
            webview.loadUrl("javascript:getMakes(" + year + ")");
        }

        //RETURNS: JSON String [{"make_id":"ac","make_display":"AC","make_is_common":"0","make_country":"UK"},
        //{"make_id":"acura","make_display":"Acura","make_is_common":"1","make_country":"USA"}...]
        @JavascriptInterface
        public void getMakesRet(String retObj) {
            Log.d("CQ", "the actual retobj for makes is " + retObj);
        }

        //gets all the models from a specified make in a specified year
        //make example = "ford"
        @JavascriptInterface
        public void getModels(String make, int year) {
            webview.loadUrl("javascript:getModels(" + make + "," + year + ")");
        }

        //RETURNS: JSON String [{"model_name":"Escape","model_make_id":"ford"}...]
        @JavascriptInterface
        public void getModelsRet(String retObj) {
            Log.d("CQ", "the actual retobj for models is " + retObj);
        }

        //gets all the details you need including vehicle id. Necessary step as there can be
        //many versions of the same/similar vehicle
        //PARAMETERS: fullDetails is 1 for all data or 0 for sparse data, test both to see what you need
        @JavascriptInterface
        public void getData(String make, String model, int year, int fullDetails) {
            webview.loadUrl("javascript:getData(" + make + "," + year + "," + model + "," + fullDetails + ")");
        }

        //returns a ton of data... print out to test it
        @JavascriptInterface
        public void getDataRet(String retObj) {
            Log.d("CQ", "the actual retobj for data is " + retObj);
        }

        //you need to grab vehicleId from getData and run it through here to get the exact car
        @JavascriptInterface
        public void getVehicle(int vehicleID) {
            webview.loadUrl("javascript:getVehicle(" + vehicleID + ")");
        }

        //returns all available details about the car. print out to test
        @JavascriptInterface
        public void getVehicleRet(String retObj) {
            Log.d("CQ", "the actual retobj for vehicle is " + retObj);
        }


    }

}
