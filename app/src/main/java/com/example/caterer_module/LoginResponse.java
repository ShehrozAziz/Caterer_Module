package com.example.caterer_module;

public class LoginResponse {
    private boolean success;
    private String message;
    private Caterer caterer;  // Assuming the transporter object is returned on successful login

    // Constructor
    public LoginResponse(boolean success, String message, Caterer caterer) {
        this.success = success;
        this.message = message;
        this.caterer = caterer;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Caterer getTransporter() {
        return caterer;
    }

    // Optional: Setters if needed
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTransporter(Caterer caterer) {
        this.caterer = caterer;
    }
}
