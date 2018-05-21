package com.admin.friendsandfamilylocator;


import java.util.ArrayList;
import java.util.HashMap;

public class Meetings {

    private String meetingId, title, date, time, locationName, groupCode, owner;
    double locationLat, locationLon;
    private ArrayList<String> meetingMembers;
    private HashMap<String,Messages> message;

    Meetings(){
        meetingMembers = new ArrayList<>();
        message = new HashMap<>();
    }

    Meetings(String meetingId, String title, String date, String time, String locationName, String groupCode, double locationLat, double locationLon, ArrayList<String> meetingMembers, String owner, HashMap<String, Messages> message){

        this.meetingId = meetingId;
        this.title = title;
        this.date = date;
        this.time = time;
        this.locationName = locationName;
        this.groupCode = groupCode;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.meetingMembers = meetingMembers;
        this.owner = owner;
        this.message = message;

    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLon() {
        return locationLon;
    }

    public void setLocationLon(double locationLon) {
        this.locationLon = locationLon;
    }

    public ArrayList<String> getMeetingMembers() {
        return meetingMembers;
    }

    public void setMeetingMembers(ArrayList<String> meetingMembers) {
        this.meetingMembers = meetingMembers;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public HashMap<String, Messages> getMessage() {
        return message;
    }

    public void setMessage(HashMap<String, Messages> message) {
        this.message = message;
    }
}
