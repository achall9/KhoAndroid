package com.kholabs.khoand.Model;

/**
 * Created by Aladar-PC2 on 3/1/2018.
 */

public class MarkerItem {
    double lat;
    double lon;
    String name;

    public MarkerItem(double lat, double lon, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    public double getLat() {return lat;}
    public double getLon() {return lon;}
    public String getName() {return name;}
    public void setLat(double lat) {this.lat = lat;}
    public void setLon(double lon) {this.lon = lon;}
    public void setName(String name) {this.name = name;}
}
