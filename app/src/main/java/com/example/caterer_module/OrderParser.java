package com.example.caterer_module;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OrderParser {
    public static List<Order> parseOrderData(String jsonData) {
        Gson gson = new Gson();

        // Specify the list type for deserialization
        Type orderListType = new TypeToken<ArrayList<Order>>() {}.getType();

        // Parse the JSON array
        return gson.fromJson(jsonData, orderListType);
    }
    public static List<AssignedOrder> parseAssignedOrderData(String jsonData) {
        Gson gson = new Gson();

        // Specify the list type for deserialization
        Type orderListType = new TypeToken<ArrayList<AssignedOrder>>() {}.getType();

        // Parse the JSON array
        return gson.fromJson(jsonData, orderListType);
    }
}

