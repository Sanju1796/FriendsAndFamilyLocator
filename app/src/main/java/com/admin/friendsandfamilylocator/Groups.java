package com.admin.friendsandfamilylocator;

import java.util.ArrayList;

/**
 * Created by Admin on 14-04-2018.
 */

public class Groups {
    private String groupCode;
    private String groupName;
    private String owner;
    private ArrayList<String> members;


    public Groups() {

        members = new ArrayList<>();
    }

    public Groups(String groupCode, String groupName, String owner, ArrayList<String> members) {

        this.groupCode = groupCode;
        this.groupName = groupName;
        this.owner = owner;
        this.members = members;

    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }
}