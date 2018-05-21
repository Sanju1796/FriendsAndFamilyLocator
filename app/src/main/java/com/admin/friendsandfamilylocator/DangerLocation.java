package com.admin.friendsandfamilylocator;

public class DangerLocation {

    private String locationId, locationAddress, markedBy_PhoneNumber, markedBy_Name,markedBy_photo, reason;
    double lat, lon;

    DangerLocation(){

    }

    public DangerLocation(String locationId, String locationAddress, String markedBy_PhoneNumber, String markedBy_Name, String markedBy_photo, String reason, double lat, double lon) {
        this.locationId = locationId;
        this.locationAddress = locationAddress;
        this.markedBy_PhoneNumber = markedBy_PhoneNumber;
        this.markedBy_Name = markedBy_Name;
        this.markedBy_photo = markedBy_photo;
        this.reason = reason;
        this.lat = lat;
        this.lon = lon;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getMarkedBy_PhoneNumber() {
        return markedBy_PhoneNumber;
    }

    public void setMarkedBy_PhoneNumber(String markedBy_PhoneNumber) {
        this.markedBy_PhoneNumber = markedBy_PhoneNumber;
    }

    public String getMarkedBy_Name() {
        return markedBy_Name;
    }

    public void setMarkedBy_Name(String markedBy_Name) {
        this.markedBy_Name = markedBy_Name;
    }

    public String getMarkedBy_photo() {
        return markedBy_photo;
    }

    public void setMarkedBy_photo(String markedBy_photo) {
        this.markedBy_photo = markedBy_photo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
