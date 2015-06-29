/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package com.example.cs446project.bestfuel.helper;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHandler extends SQLiteOpenHelper {

	private static final String TAG = SQLiteHandler.class.getSimpleName();

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "android_api";

	// Login table name
	private static final String TABLE_LOGIN = "login";

	// Login Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_UID = "uid";
	private static final String KEY_CREATED_AT = "created_at";

	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
				+ KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
				+ KEY_CREATED_AT + " TEXT" + ")";
		db.execSQL(CREATE_LOGIN_TABLE);

		Log.d(TAG, "Database tables created");
		createCarDB();
	}


	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);

		// Create tables again
		onCreate(db);
	}

	//creates table for car data
	public void createCarDB() {
		SQLiteDatabase db = this.getWritableDatabase();
		String CREATE_CAR_TABLE = "CREATE TABLE IF NOT EXISTS car_table (name TEXT, " +
				"isdefault BOOLEAN, "+
				"year INT," +
				"make TEXT, "+
				"model TEXT, "+
				"id INT, "+
				"body TEXT, "+
				"engine_position TEXT, "+
				"engine_cc INT, "+
				"engine_cyl INT, "+
				"engine_type TEXT, "+
				"engine_valves_per_cyl INT, "+
				"engine_power_rpm INT, "+
				"engine_fuel TEXT, "+
				"top_speed_kmh INT, "+
				"kph0to100 FLOAT, "+
				"drive TEXT, "+
				"transmission TEXT, "+
				"seats INT, "+
				"doors INT, "+
				"weight_kg INT, "+
				"length_mm INT, "+
				"width_mm INT, "+
				"height_mm INT, "+
				"wheelbase_mm INT, "+
				"hwy_lkm FLOAT, "+
				"mixed_lkm FLOAT, "+
				"city_lkm FLOAT, "+
				"fuel_capacity_l INT, "+
				"sold_in_us INT, "+
				"engine_hp INT, "+
				"top_speed_mph INT, "+
				"weight_ibs INT, "+
				"length_in FLOAT, "+
				"width_in FLOAT, "+
				"height_in FLOAT, "+
				"hwy_mpg FLOAT, "+
				"city_mpg FLOAT, "+
				"mixed_mpg FLOAT, "+
				"fuel_capacity_g FLOAT, "+
				"country TEXT )";
		db.execSQL(CREATE_CAR_TABLE);
		Log.d(TAG, "Database car table created");
	}


	/**
	 * Storing user details in database
	 * */
	public void addUser(String name, String email, String uid, String created_at) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name); // Name
		values.put(KEY_EMAIL, email); // Email
		values.put(KEY_UID, uid); // Email
		values.put(KEY_CREATED_AT, created_at); // Created At

		// Inserting Row
		long id = db.insert(TABLE_LOGIN, null, values);
		db.close(); // Closing database connection

		Log.d(TAG, "New user inserted into sqlite: " + id);
	}

	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		//Log.d(TAG, "Cursor is testing: " + cursor.getString(0));
		if (cursor.getCount() > 0) {
			user.put("name", cursor.getString(1));
			user.put("email", cursor.getString(2));
			user.put("uid", cursor.getString(3));
			user.put("created_at", cursor.getString(4));
		}
		cursor.close();
		db.close();
		// return user
		Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

		return user;
	}

	/**
	 * Getting user login status return true if rows are there in table
	 * */
	public int getRowCount() {
		String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();

		// return row count
		return rowCount;
	}

	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_LOGIN, null, null);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");
	}

	public void addCar(String name, Boolean isdefault, String year, String make, String model, String id, String body, String engine_position, String engine_cc, String engine_cyl, String engine_type,
					   String engine_valves_per_cyl, String engine_power_rpm, String engine_fuel, String top_speed_kmh, String kph0to100, String drive, String transmission, String seats, String doors, String weight_kg,
					   String length_mm, String height_mm, String width_mm, String wheelbase_mm, String hwy_lkm, String mixed_lkm, String city_lkm, String fuel_capacity_l, String sold_in_us, String engine_hp, String top_speed_mph,
					   String weight_ibs, String length_in, String width_in, String height_in, String hwy_mpg, String city_mpg, String mixed_mpg, String fuel_capacity_g, String country) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("isdefault", isdefault);
		values.put("year", year);
		values.put("make", make);
		values.put("model", model);
		values.put("id", id);
		values.put("body", body);
		values.put("engine_position", engine_position);
		values.put("engine_cc", engine_cc);
		values.put("engine_cyl", engine_cyl);
		values.put("engine_type", engine_type);
		values.put("engine_valves_per_cyl", engine_valves_per_cyl);
		values.put("engine_power_rpm", engine_power_rpm);
		values.put("engine_fuel", engine_fuel);
		values.put("top_speed_kmh", top_speed_kmh);
		values.put("kph0to100", kph0to100);
		values.put("drive", drive);
		values.put("transmission", transmission);
		values.put("seats", seats);
		values.put("doors", doors);
		values.put("weight_kg", weight_kg);
		values.put("length_mm", length_mm);
		values.put("height_mm", height_mm);
		values.put("width_mm", width_mm);
		values.put("wheelbase_mm", wheelbase_mm);
		values.put("hwy_lkm", hwy_lkm); //25
		values.put("mixed_lkm", mixed_lkm);
		values.put("city_lkm", city_lkm);
		values.put("fuel_capacity_l", fuel_capacity_l);
		values.put("sold_in_us", sold_in_us);
		values.put("engine_hp", engine_hp);
		values.put("top_speed_mph", top_speed_mph);
		values.put("weight_ibs", weight_ibs);
		values.put("length_in", length_in);
		values.put("width_in", width_in);
		values.put("height_in", height_in);
		values.put("hwy_mpg", hwy_mpg);
		values.put("city_mpg", city_mpg);
		values.put("mixed_mpg", mixed_mpg);
		values.put("fuel_capacity_g", fuel_capacity_g);
		values.put("country", country);

		// Inserting Row
		long idr = db.insert("car_table", null, values);
		db.close(); // Closing database connection

		Log.d(TAG, "New car inserted into sqlite: " + idr);
	}

	public void deleteCar(int id, String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "DELETE FROM car_table WHERE id ='" + id + "' AND name ='" + name+"'";
		db.execSQL(query);
	}

	public ArrayList<HashMap<String, String>> getCars(String name){
		ArrayList<HashMap<String, String>> carList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT  * FROM  car_table WHERE name='"+name+"'";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		//Log.d(TAG, "Cursor is testing: " + cursor.getString(0));
		Log.d("GETCAR", "starting to retrieve cars");
		if (cursor.getCount() > 0) {
			do {
				HashMap<String, String> car = new HashMap<String, String>();
				car.put("name", cursor.getString(0));
				car.put("isdefault", cursor.getString(1));
				car.put("year", cursor.getString(2));
				car.put("make", cursor.getString(3));
				car.put("model", cursor.getString(4));
				car.put("id", cursor.getString(5));
				car.put("hwy_lkm", cursor.getString(25));
				car.put("mixed_lkm", cursor.getString(26));
				car.put("city_lkm", cursor.getString(27));
				car.put("fuel_capacity_l", cursor.getString(28));
				Log.d("CURSOR TEST", "cursor is " + car.get("name"));
				Log.d("CURSOR TEST", "cursor is " + car.get("isdefault"));
				Log.d("CURSOR TEST", "cursor is " + car.get("year"));
				Log.d("CURSOR TEST", "cursor is " + car.get("make"));
				carList.add(car);

			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return user
		//Log.d(TAG, "Fetching car from Sqlite: " + car.toString());

		return carList;
	}

	public HashMap<String, String> getCar(Boolean isdefault, String name) {
		String selectQuery = "SELECT  * FROM  car_table WHERE name='"+name+"' AND isdefault='"+isdefault+"'";
		HashMap<String, String> car = new HashMap<String, String>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		cursor.moveToFirst();
		//Log.d(TAG, "Cursor is testing: " + cursor.getString(0));
		Log.d("GETCAR", "starting to retrieve car");
		if (cursor.getCount() > 0) {
			car.put("name", cursor.getString(0));
			car.put("isdefault", cursor.getString(1));
			car.put("year", cursor.getString(2));
			car.put("make", cursor.getString(3));
			car.put("model", cursor.getString(4));
			car.put("id", cursor.getString(5));
			car.put("hwy_lkm", cursor.getString(25));
			car.put("mixed_lkm", cursor.getString(26));
			car.put("city_lkm", cursor.getString(27));
			car.put("fuel_capacity_l", cursor.getString(28));
		}
		cursor.close();
		db.close();
		return car;
	}

}
