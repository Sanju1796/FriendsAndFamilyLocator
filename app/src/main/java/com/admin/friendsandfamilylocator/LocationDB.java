package com.admin.friendsandfamilylocator;

public class LocationDB {

    private long time;
    private String date;
    double lat, lon;

    LocationDB(){

    }

    LocationDB(long time, String date, double lat, double lon){
        this.time = time;
        this.date = date;
        this.lat = lat;
        this.lon = lon;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
