package com.admin.friendsandfamilylocator;

import java.util.HashMap;

public class Messages {

    private String messageCode, message, senderNumber, senderName, date, time;

    Messages(){

    }

    public Messages(String messageCode, String message, String senderNumber, String senderName, String date, String time) {
        this.messageCode = messageCode;
        this.message = message;
        this.senderNumber = senderNumber;
        this.senderName = senderName;
        this.date = date;
        this.time = time;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
