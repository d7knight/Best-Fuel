package com.example.cs446project.bestfuel.helper;

/**
 * Created by Kyle on 02/06/2015.
 */
public class StationData {

    //poor design to give public access to everything but its quick and easy
    public int id;
    public String name;
    public String address;
    public double lat;
    public double lon;
    public String phone;
    public String area;
    public String created;
    public String updated;
    public String grade;
    public Double price;
    public String fuelUpdate;
    public Double distance;

    StationData(int id, String name, String address, double lat, double lon, String phone, String area, String created, String updated, String grade, double price, String fuelUpdate, double distance){
        this.id=id;
        this.name=name;
        this.address=address;
        this.lat=lat;
        this.lon=lon;
        this.phone=phone;
        this.area=area;
        this.created=created;
        this.updated=updated;
        this.grade=grade;
        this.price=price;
        this.fuelUpdate=fuelUpdate;
        this.distance=distance;
    }


}
