package com.example.cs446project.bestfuel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.cs446project.bestfuel.helper.SQLiteHandler;
import com.example.cs446project.bestfuel.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProfileActivity extends Activity {

    private TextView txtInfo;
    private SQLiteHandler db;
    private SessionManager session;
    private ArrayList<String> carYears = new ArrayList<String>();
    private View inflated;
    private Spinner yearSpin;
    private ProgressDialog pDialog;
    ListView list;
    CarAdapter adapter;
    ArrayList<Car> arraylist=new ArrayList<Car>();

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

        // Pass results to ListViewAdapter Class
        adapter = new CarAdapter(this, arraylist);

        // Binds the Adapter to the ListView
        list = (ListView) findViewById(R.id.listview);
        list.setAdapter(adapter);



        //Kyle see this code below for how to add a car at any time


        adapter.add(new Car("Lambo", "hello ",
                "hello", "hello", "hello"));
        adapter.notifyDataSetChanged();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

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

        //Add car button
        FrameLayout addFrame = (FrameLayout) findViewById(R.id.addFrame);
        addFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CarFrame", "Clicked on add a car");
                addCarDialogue(inflated);
                cq.getYears();
            }
        });





        //testing carquery stuff
        WebView webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        cq = new CQInterface(this, webview);
        webview.addJavascriptInterface(cq, "Android");

        webview.loadUrl("file:///android_asset/carquery.html");


        inflated = getLayoutInflater().inflate(R.layout.vehicle_layout, null);
        yearSpin = (Spinner) inflated.findViewById(R.id.year);

        //cq.getYears();
    }



   public class Car{
       String make,model,year,capacity,stats;
       public Car(String make, String model, String year, String capacity, String stats){
           this.make=make;
           this.model=model;
           this.year=year;
           this.capacity=capacity;
           this.stats=stats;
       }
   }


    public class CarAdapter extends ArrayAdapter<Car> {

        // Declare Variables
        Context mContext;
        LayoutInflater inflater;
        private List<Car> carlist = null;
        private ArrayList<Car> arraylist;
        public CarAdapter(Context context, ArrayList<Car> cars) {
            super(context, 0, cars);
            mContext = context;
            this.carlist = cars;
            inflater = LayoutInflater.from(mContext);
            this.arraylist = new ArrayList<Car>();
            this.arraylist.addAll(carlist);
        }


        public class ViewHolder {
            View view;
            TextView make,model,year,capacity,stats;

        }


        public View getView(final int position, View view, ViewGroup parent) {
            final ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();

                view = inflater.inflate(R.layout.car_template,parent, false);

                holder.view=view;
                holder.make= (TextView)view.findViewWithTag("Make");
                holder.model= (TextView)view.findViewWithTag("Model");
                holder.year= (TextView)view.findViewWithTag("Year");
                holder.capacity= (TextView)view.findViewWithTag("Capacity");
                holder.stats= (TextView)view.findViewWithTag("Stats");
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            Car c=carlist.get(position);
            holder.make.setText(c.make);
            holder.model.setText(c.model);
            holder.year.setText(c.year);
            holder.capacity.setText(c.capacity);
            holder.stats.setText(c.stats);



            view.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    // Send single item click data to SingleItemView Class
                    final View customView = inflater.inflate(R.layout.manage_car_dialog, null);
                    AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle("Car Settings")
                            .setView(customView)
                            .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            }).create();
                    dialog.show();
                    return true;

                }
            });


            return view;
        }



    }


    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void addCarDialogue(View inflated){

        Dialog dialog = new Dialog(this);
        dialog.setContentView(inflated);
        dialog.setTitle("Add A Vehicle");

        dialog.show();
    }

    private void adjustYearSpinner(ArrayList<String> years){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpin.setAdapter(adapter);
    }



    //Loading dialog stuff
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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
            Log.d("CarQuery", "Calling getYears");
            pDialog.setMessage("Fetching Vehicle Years ...");
            showDialog();
            webview.loadUrl("javascript:getYears()");
        }

        //RETURNS: JSON String {"min_year":"#", "max_year":"#"}
        @JavascriptInterface
        public void getYearsRet(String retObj) {

            Log.d("CQ", "the actual retobj is " + retObj);

            final ArrayList<String> years = new ArrayList<String>();
            JSONObject jObj = null;
            int start=0;
            int end=0;
            try {
                jObj = new JSONObject(retObj);
                start = Integer.parseInt(jObj.getString("min_year"));
                end = Integer.parseInt(jObj.getString("max_year"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for(int i=end; i>= start; i--) {
                years.add(Integer.toString(i));
            }
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  adjustYearSpinner(years);
                              }
                          });

            hideDialog();
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
