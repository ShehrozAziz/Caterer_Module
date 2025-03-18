package com.example.caterer_module;

import java.io.Serializable;

public class AssignedOrder implements Serializable {
    private String orderID;
    private String date;
    private int fare;
    private String source;
    private String sourcePin;
    private String status;
    private String time;
    private String catererID; // Renamed from transporterID
    private String userID;
    private String name;
    private String phone;
    private String orderDetails; // Added field

    // Constructor
    public AssignedOrder(String orderID, String date, int fare, String name, String phone,
                         String source, String sourcePin, String status, String time,
                         String catererID, String userID, String orderDetails) {
        this.orderID = orderID;
        this.date = date;
        this.fare = fare;
        this.name = name;
        this.phone = phone;
        this.source = source;
        this.sourcePin = sourcePin;
        this.status = status;
        this.time = time;
        this.catererID = catererID;
        this.userID = userID;
        this.orderDetails = orderDetails;
    }

    // Getters
    public String getOrderID() {
        return orderID;
    }

    public String getDate() {
        return date;
    }

    public int getFare() {
        return fare;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getSource() {
        return source;
    }

    public String getSourcePin() {
        return sourcePin;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public String getCatererID() { // Renamed getter
        return catererID;
    }

    public String getUserID() {
        return userID;
    }

    public String getOrderDetails() { // Added getter
        return orderDetails;
    }
}
