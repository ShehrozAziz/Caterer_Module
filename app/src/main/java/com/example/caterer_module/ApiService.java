package com.example.caterer_module;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/login")  // Assuming '/login' is the login endpoint
    Call<LoginResponse> login(@Body LoginData loginData);
    @POST("/api/assignOrder")
    Call<ApiResponse> assignOrder(@Body AcceptOrderData assignOrderRequest);

    @POST("/api/setorderstatus")
    Call<ApiResponse> markOrderCompleted(@Body AcceptOrderData OrderID);

    @POST("/api/fetchOrdersForCaterer")
    Call<MainActivity.ApiAssignOrderResponse> getAssignedOrder(@Body AcceptOrderData TransporterID);

    @POST("/api/updatePassword")
    Call<ApiResponse> updatePassword(@Body MainActivity.ApidataChangeDetails data);
    @POST("/api/updatePhone")
    Call<ApiResponse> updatePhone(@Body MainActivity.ApidataChangeDetails data);
}
