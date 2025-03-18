package com.example.caterer_module;

import java.io.Serializable;

public class Order implements Serializable {
    private String orderID;
    private String date;
    private int fare;
    private String name;
    private String orderDetails;
    private String phone;
    private String sourceAddress;
    private String sourcePin;
    private String status;
    private String time;
    private String userID;

    // Constructor
    public Order(String orderID, String date, int fare, String name, String orderDetails,
                 String phone, String sourceAddress, String sourcePin,
                 String status, String time, String userID) {
        this.orderID = orderID;
        this.date = date;
        this.fare = fare;
        this.name = name;
        this.orderDetails = orderDetails;
        this.phone = phone;
        this.sourceAddress = sourceAddress;
        this.sourcePin = sourcePin;
        this.status = status;
        this.time = time;
        this.userID = userID;
    }

    // Getters and Setters
    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getFare() { return fare; }
    public void setFare(int fare) { this.fare = fare; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrderDetails() { return orderDetails; }
    public void setOrderDetails(String orderDetails) { this.orderDetails = orderDetails; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSourceAddress() { return sourceAddress; }
    public void setSourceAddress(String sourceAddress) { this.sourceAddress = sourceAddress; }

    public String getSourcePin() { return sourcePin; }
    public void setSourcePin(String sourcePin) { this.sourcePin = sourcePin; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
}
