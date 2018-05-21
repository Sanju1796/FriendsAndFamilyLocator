package com.admin.friendsandfamilylocator;

import java.util.ArrayList;
import java.util.HashMap;

public class Users {

    private String phoneNumber, email, userName, photo;

    private ArrayList<String> invitations, groupCodes, groupNames;
    private HashMap<String,LocationDB> location;
    private double lastLatitude,lastLongitude;

    Users(){
        invitations = new ArrayList<>();
        groupCodes = new ArrayList<>();
        groupNames = new ArrayList<>();
        location = new HashMap<>();
    }

    public Users(String phoneNumber, String email, String userName, String photo,ArrayList<String> invitations, ArrayList<String> groupCodes, ArrayList<String> groupNames, HashMap<String,LocationDB>location, double lastLatitude, double lastLongitude){

        this.phoneNumber = phoneNumber;
        this.email = email;
        this.userName = userName;
        this.photo = photo;
        this.invitations = invitations;
        this.groupCodes = groupCodes;
        this.groupNames = groupCodes;
        this.location = location;
        this.lastLatitude = lastLatitude;
        this.lastLongitude = lastLongitude;
    }
    @Override
    public boolean equals(Object obj){
        Users u = (Users) obj;
        String p1,p2;
        p1 =this.phoneNumber.replace("+91","");
        p2 =u.phoneNumber.replace("+91","");


        p1= p1.replace("","");
        p2= p2.replace("","");


        if(p1.equals(p2)){
            return true;
        }
        else{
            return false;
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public ArrayList<String> getInvitations() {
        return invitations;
    }

    public void setInvitations(ArrayList<String> invitations) {
        this.invitations = invitations;
    }

    public ArrayList<String> getGroupCodes() {
        return groupCodes;
    }

    public void setGroupCodes(ArrayList<String> groupCode) {
        this.groupCodes = groupCode;
    }

    public ArrayList<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(ArrayList<String> groupName) {
        this.groupNames = groupName;
    }

    public HashMap<String, LocationDB> getLocation() {
        return location;
    }

    public void setLocation(HashMap<String, LocationDB> location) {
        this.location = location;
    }

    public double getLastLatitude() {
        return lastLatitude;
    }

    public void setLastLatitude(double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public double getLastLongitude() {
        return lastLongitude;
    }

    public void setLastLongitude(double lastLongitude) {
        this.lastLongitude = lastLongitude;
    }
}

