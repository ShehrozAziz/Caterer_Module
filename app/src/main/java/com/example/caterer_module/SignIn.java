package com.example.caterer_module;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignIn extends AppCompatActivity {
    MaterialButton btnSignIn;
    ImageView ivTransport_Clip;
    TextInputEditText etUsernameSignin,etPasswordSignin;
    Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    static public boolean locationfetched;
    static public Caterer caterer;
    static public boolean LoggedIn;
    static public double longitude;
    static public double latitude;
    static public String baseURL;

    ProgressDialog progressDialog;
    SharedPrefsManager manager;

    static public AssignedOrder BookedOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        context = SignIn.this;
        // Set status bar color
        baseURL = getString(R.string.server_IP);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.BG)); // Replace 'your_color' with the desired color
        new Handler().postDelayed(() -> {
            manager = new SharedPrefsManager(context);
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);  // Prevent canceling by tapping outside
            progressDialog.setIndeterminate(true);  // Show indeterminate spinner
            progressDialog.show();

            //FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    if(!locationfetched)
                    {
                        Got();
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        Toast.makeText( context,"Location Fetched",Toast.LENGTH_LONG ).show();
                        locationfetched = true;
                        progressDialog.dismiss();

                        if(manager.getUsername() != null)
                        {
                            SignIn.caterer = manager.getCaterer();
                            Intent intent = new Intent(SignIn.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(@NonNull String provider) {}

                @Override
                public void onProviderDisabled(@NonNull String provider) {}
            };
            // Request Location Permissions
            checkLocationPermissions();

        }, 5000);



    }
    public void Got()
    {

        setContentView(R.layout.activity_sign_in);
        btnSignIn = findViewById(R.id.btnSignIn);

        etPasswordSignin = findViewById(R.id.etPasswordSignin);
        etUsernameSignin = findViewById(R.id.etUsernameSignin);
        ivTransport_Clip = findViewById(R.id.ivTransport_Clip);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSignIn.setClickable(false);
                SignInrequest(etUsernameSignin.getText().toString(), etPasswordSignin.getText().toString(), new LoginCallBack() {
                    @Override
                    public void onLoginResult(int result, LoginResponse loginResponse) {
                        switch (result) {
                            case 1:
                                // Successful login
                                caterer = loginResponse.getTransporter();
                                Log.d("Transporter", "Transporter Name: " + caterer.getName());
                                manager.clearTransporterData();
                                manager.saveTransporter(caterer,etUsernameSignin.getText().toString(),etPasswordSignin.getText().toString());
                                Log.d("Sign In", caterer.getId());
                                btnSignIn.setClickable(true);
                                Intent intent = new Intent(SignIn.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                            case -1:
                                // Login failed (incorrect credentials)
                                Toast.makeText(context, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                btnSignIn.setClickable(true);
                                break;
                            case -2:
                                // Response failure
                                Toast.makeText(context, "Login Failed: Server is Down", Toast.LENGTH_SHORT).show();
                                btnSignIn.setClickable(true);
                                break;
                            case -3:
                                // Network error
                                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                btnSignIn.setClickable(true);
                                break;
                            default:
                                // Unknown error
                                Toast.makeText(context, "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                                btnSignIn.setClickable(true);
                                break;
                        }
                    }
                });
            }
        });
        final View rootView = findViewById(android.R.id.content).getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    ivTransport_Clip.setVisibility(View.GONE);

                } else {
                    ivTransport_Clip.setVisibility(View.VISIBLE);

                }
            }
        });


    }
    public void SignInrequest(String phone, String password, LoginCallBack callback)
    {
        // Create the ClientData object
        LoginData clientData = new LoginData(phone, password);

        // Retrofit setupw
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory( GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Call the login method
        Call<LoginResponse> call = apiService.login(clientData);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        // Handle success
                        if (serverResponse.isSuccess()) {
                            // Successful login, do something (e.g., navigate to another activity)
                            Log.d("API Response", "Login successful");
                            caterer = serverResponse.getTransporter();
                            Log.d("Transporter", "Transporter Name: " + caterer.getId());
                            callback.onLoginResult(1,response.body());
                            //Toast.makeText(SignIn.this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            // Login failed
                            Log.e("API Response", "Login failed: " + serverResponse.getMessage());
                            callback.onLoginResult( -1,response.body() );
                            //Toast.makeText(SignIn.this, serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Log.e("API Error", "Response failed: " + response.message());
                    //Toast.makeText(SignIn.this, "Login failed", Toast.LENGTH_SHORT).show();
                    callback.onLoginResult( -2,response.body() );
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("API Error", "Request failed: " + t.getMessage());
                Toast.makeText(SignIn.this, "Network error", Toast.LENGTH_SHORT).show();
                callback.onLoginResult( -3,null);
            }
        });
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request foreground location permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);  // Stop receiving updates when the app is paused
        }
    }


}