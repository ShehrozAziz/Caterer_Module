package com.example.caterer_module;

public class Caterer {
    private String id;
    private String name;
    private String phone;
    private String password;  // Plain password, should be hashed in the controller
    private int complaintCount;
    public Caterer()
    {
        this.id = "Null";
    }

    // Constructor
    public Caterer(String id, String name, String phone, String password, int complaintCount) {
        this.id = id != null ? id : ""; // Ensure ID is not null, could be handled differently based on your logic
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.complaintCount = complaintCount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getComplaintCount() {
        return complaintCount;
    }

    public void setComplaintCount(int complaintCount) {
        this.complaintCount = complaintCount;
    }

    // toString() method for logging or debugging
    @Override
    public String toString() {
        return "Transporter{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", complaintCount=" + complaintCount +
                '}';
    }
}
