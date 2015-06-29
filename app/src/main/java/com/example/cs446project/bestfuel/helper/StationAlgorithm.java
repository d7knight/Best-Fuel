package com.example.cs446project.bestfuel.helper;

import android.app.Application;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.cs446project.bestfuel.MapActivity;
import com.example.cs446project.bestfuel.app.AppConfig;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.cs446project.bestfuel.app.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kyle on 28/05/2015.
 */
public class StationAlgorithm{

    //global data list
    ArrayList<StationData> stationList = new ArrayList<StationData>();
    public String result;
    int bestStationIndex;
    private int rad=5;
    private double curLat;
    private double curLon;
    private String curGrade;
    private int carCapacity;
    private float carEconomoy;
    private SQLiteHandler db;
    MapActivity curAct;

    //constructor
    public StationAlgorithm(MapActivity curAct, SQLiteHandler db) {
        // Progress dialog
        //testing this out
        //getPrices(51.5033630,-0.1276250, 10, "Regular"); //near me
        //getData(40.3532924, -79.8307726, 10, "Regular"); //For testing
        //findBestStation(40.3532924, -79.8307726);

        this.db = db;

        this.curAct=curAct;
    }

    //this is function that map would call
    //PARAMETERS: unknown currently. JSON String?? of what design?
    //RETURNS: JSON String
    public void findBestStation(double lat, double lon) { //Please wrap this in "loading" dialogue inside activity
        //TODO get preferred gas type from profile
        //for now assume regular
        stationList.clear();
        bestStationIndex=-1;
        rad = 5;
        curLat = lat;
        curLon=lon;
        curGrade="Regular";//TODO use car settings for this
        HashMap<String,String> user = db.getUserDetails();
        HashMap<String, String> curCar = db.getCar(true, user.get("name"));
        Log.d("SA", "name is "+ curCar.get("make"));
        Log.d("SA", "precapacity is "+curCar.get("fuel_capacity_l"));
        if(curCar != null && !curCar.isEmpty()) {
            this.carCapacity = Integer.parseInt(curCar.get("fuel_capacity_l"));
            this.carEconomoy = Float.parseFloat(curCar.get("mixed_lkm"));
        } else {
            this.carCapacity = 50;
            this.carEconomoy = 17;
        }
        Log.d("SA", "capacity is "+this.carCapacity);
        Log.d("SA", "economy is "+this.carEconomoy);

        getData(lat, lon, rad, "Regular");
    }

    //connects with database to grab prices of provided stations
    private void getData(final double lat,final double lon, final int radius,final String grade) {


        String url = AppConfig.URL_STATIONS;
        StringRequest jsObj = new StringRequest(Method.POST, url, new Response.Listener<String>() {
        //StringRequest jsObj = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("DBTest", "response was " +response.toString());
                try {
                    JSONObject js =  new JSONObject(response);
                    JSONArray jsa = js.getJSONArray("result");
                    stationList.clear();
                    for (int i=0;i<jsa.length();i++) {
                        JSONObject jsPart = jsa.getJSONObject(i);

                        //splitting values
                        int id = Integer.parseInt(jsPart.getString("id"));
                        String name = jsPart.getString("name");
                        String address = jsPart.getString("address");
                        double lat = Double.parseDouble(jsPart.getString("lat"));
                        double lon = Double.parseDouble(jsPart.getString("lon"));
                        String phone = jsPart.getString("phone");
                        String area = jsPart.getString("area");
                        String created = jsPart.getString("created_at");
                        String updated = jsPart.getString("updated_at");
                        String grade = jsPart.getString("grade");
                        double price = Double.parseDouble(jsPart.getString("price"));
                        String fuelUpdate = jsPart.getString("price_updated");
                        double distance = Double.parseDouble(jsPart.getString("distance"));

                        StationData tempSD = new StationData(id, name, address, lat, lon, phone, area, created, updated, grade, price, fuelUpdate, distance);
                        stationList.add(tempSD);
                    }
                    result= js.toString();
                    dbCallback(Integer.parseInt(js.getString("result_set_size")));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DBTest", "errorererer "+ error);
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                String nLat = Double.toString(lat);
                String nLon = Double.toString(lon);
                String rad = Integer.toString(radius);
                params.put("tag", "get_stations_by_distance_then_price");
                params.put("lat", nLat);
                params.put("lon", nLon);
                params.put("radius", rad);
                params.put("grade", grade);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(jsObj);
    }


    private void dbCallback(int size){
        Log.d("DBTest", "dbcallback reached");
        if(size<10){
            //retry with bigger radius.
            rad+=2;
            getData(curLat, curLon, rad, curGrade);
        } else {
            double score=-1;
            bestStationIndex=-1;
            for(int i=0; i<size; i++) {
                Log.d("ALG", "testing "+i+" which is "+stationList.get(i).address);
                double curVal =calculate(stationList.get(i).distance, stationList.get(i).price, this.carEconomoy, this.carCapacity);
                if(score==-1 || curVal<score){
                    score=curVal;
                    bestStationIndex=i;
                }
            }
            Log.d("DBTest", "best station is "+bestStationIndex);
            this.curAct.stationBypass(bestStationIndex);
        }
    }


    private double calculate(double distance, double price, float economy, int capacity) {
        //currently not great for routes. Mostly for Get Gas Now. Needs to calculate
        //total route fuel usage to be useful for other option

        //easy part calculating fuel used getting to station (and back potentially)
        double lUsed = (economy / 100) * distance; //economy is given in L/100km
        //Canada = price/L
        double fuelPUsed = price * lUsed;
        double fuelEconomy = fuelPUsed*economy; //basic features of trip there. Economy doesnt do a whole lot yet for the calculation

        double fillUp = price*(capacity*0.7);//price of filling up the car using a default tank of 30% full (make this an option later) TODO

        double total = fuelEconomy + fillUp;

        Log.d("ALG", "Distance of "+distance+" resulted in "+ total);
        return total;
    }


}
