package com.example.caterer_module;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class SharedPrefsManager {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_CATERER = "caterer";
    private static final String KEY_BOOKED_ORDER = "bookedOrder";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ASSIGNED = "assigned";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Save Transporter object with username and password
    public void saveTransporter(Caterer caterer, String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Save the transporter as a JSON string
        String transporterJson = gson.toJson(caterer);
        editor.putString(KEY_CATERER, transporterJson);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }
    public void ChangeAssigned(boolean assigned)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean( KEY_ASSIGNED, assigned );
        editor.apply();

    }
    public void SetBookedOrder(Order order)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String orderJson = gson.toJson(order);
        editor.putString( KEY_BOOKED_ORDER, orderJson);
        editor.apply();
    }
    public Order getBookedOrder() {
        String orderJson = sharedPreferences.getString(KEY_BOOKED_ORDER, null);
        return gson.fromJson(orderJson, Order.class);
    }


    // Retrieve the Transporter object
    public Caterer getCaterer() {
        String transporterJson = sharedPreferences.getString(KEY_CATERER, null);
        return gson.fromJson(transporterJson, Caterer.class);
    }
    public Boolean getAssigned()
    {
        return sharedPreferences.getBoolean( KEY_ASSIGNED, false );
    }

    // Retrieve username
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    // Retrieve password
    public String getPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, null);
    }

    // Clear stored data
    public void clearTransporterData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CATERER);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.apply();
    }
}
