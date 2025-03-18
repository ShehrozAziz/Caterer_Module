package com.example.caterer_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ImageButton btnMore,btnLogout;
    RecyclerView rvPendings;
    public static BadgeDrawable Pending;

    public static EditText etHiddenEditText;
    private WebSocketClient webSocketClient;
    private static final String TAG = "MainActivity";


    List<Order> pendingOrders = new ArrayList<>();
    PendingOrdersAdapter adapter;
    ProgressDialog progressDialog;

    public View BookedOrderLayout;


    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.BG)); // Replace 'your_color' with the desired color
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        etHiddenEditText = findViewById( R.id.etHiddenEditText);
        etHiddenEditText.setVisibility(View.INVISIBLE);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);  // Prevent canceling by tapping outside
        progressDialog.setIndeterminate(true);  // Show indeterminate spinner
        progressDialog.show();
        btnLogout = findViewById(R.id.btnLogout);
        btnMore = findViewById(R.id.btnMore);
        rvPendings = findViewById(R.id.rvPendings);
        BookedOrderLayout = findViewById( R.id.booked_order_layout );
        pendingOrders = new ArrayList<>();
        rvPendings.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        adapter = new PendingOrdersAdapter(pendingOrders,MainActivity.this);
        rvPendings.setAdapter(adapter);
        etHiddenEditText.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("No"))
                {
                    progressDialog.dismiss();
                    progressDialog.show();
                    fetchBookedOrder();
                }
                else if(s.toString().equals("Yes") || s.toString().equals("Dont"))
                {
                    progressDialog.dismiss();
                }
                else if(s.toString().equals("Pending"))
                {
                    progressDialog.show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        } );
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView messageTextView = new TextView(context);
                messageTextView.setText("Do you really want to logout?");
                messageTextView.setTextSize(18); // Set the desired text size
                messageTextView.setPadding(50, 50, 50, 20); // Set padding for left and right margins
                messageTextView.setGravity( Gravity.CENTER); // Center the text
                // Set custom font
                messageTextView.setTypeface( ResourcesCompat.getFont(v.getContext(), R.font.poppins_medium));  // Apply Poppins font
                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(messageTextView);
                builder.setCancelable(true); // Allow canceling the dialog by tapping outside or pressing back
                // Set Positive Button
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    // Handle logout action here
                    SharedPrefsManager manager = new SharedPrefsManager( context );
                    manager.clearTransporterData();
                    SignIn.locationfetched=false;
                    dialog.dismiss();
                    Intent intent = new Intent(MainActivity.this,SignIn.class);
                    startActivity( intent );
                    finish();
                });

                // Set Negative Button
                builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } );
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileDialog();
            }
        });
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Prevent full swipe, reveal the buttons without removing the item
                adapter.showButtons(viewHolder.getAdapterPosition());
                adapter.notifyItemChanged(viewHolder.getAdapterPosition()); // Reset the swipe state
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                //adapter.hideButtons(viewHolder.getAdapterPosition());
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                // Only allow partial swipe to reveal buttons (e.g., 0.3f means 30% of the item width)
                return 0.3f;
            }
            /*
            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return super.getSwipeEscapeVelocity(defaultValue) * 2; // Slow down the swipe speed
            }

            @Override
            public float getSwipeVelocityThreshold(float defaultValue) {
                return super.getSwipeVelocityThreshold(defaultValue) * 2; // Slow down the swipe release velocity
            }*/
        };


        connectWebSocket();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvPendings);
        // Attach swipe actions

    }
    private void showProfileDialog() {
        // Inflate the layout_profile.xml
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_profile, null);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        // Make the dialog background transparent
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Get all views by their IDs
        TextView tvName = dialogView.findViewById(R.id.tvName);
        tvName.setText(SignIn.caterer.getName());
        TextView tvCarLocation = dialogView.findViewById(R.id.tvCarLocation);
        tvCarLocation.setText("Pick-Up Ravi, Lahore");
        TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
        tvPhone.setText(SignIn.caterer.getPhone());
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        etPhone.setText(SignIn.caterer.getPhone());
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        MaterialButton btnChangePassword = dialogView.findViewById( R.id.btnUpdatePassword );
        MaterialButton btnChangePhone = dialogView.findViewById(R.id.btnUpdatePhone);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the confirmation dialog
                ChangeDetails( etPassword.getText().toString(), true);

            }
        });
        btnChangePhone.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeDetails( etPhone.getText().toString(), false);
            }
        });



        // Show the dialog
        alertDialog.show();
        final View rootView = findViewById(android.R.id.content).getRootView();

        // Add an OnGlobalLayoutListener to detect keyboard visibility changes
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // If the visible height is much smaller than the total screen height, keyboard is open
                //boolean keyboardVisible = screenHeight - visibleHeight > screenHeight * 0.2;

                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is open, hide the elements
                    tvName.setVisibility(View.GONE);
                    tvCarLocation.setVisibility(View.GONE);
                    tvPhone.setVisibility(View.GONE);
                } else {
                    // Keyboard is closed, show the elements
                    tvName.setVisibility(View.VISIBLE);
                    tvCarLocation.setVisibility(View.VISIBLE);
                    tvPhone.setVisibility(View.VISIBLE);
                }
            }
        });

        // Optional: Adjust the dialog window's soft input mode
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    public void fetchBookedOrder()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SignIn.baseURL)
                .addConverterFactory( GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Create the request body
        AcceptOrderData request = new AcceptOrderData(" ", SignIn.caterer.getId());
        apiService.getAssignedOrder(request).enqueue(new Callback<ApiAssignOrderResponse>() {
            @Override
            public void onResponse(Call<ApiAssignOrderResponse> call, Response<ApiAssignOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiAssignOrderResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<AssignedOrder> orders = apiResponse.getOrders();
                        Log.d("API Response", "Orders fetched successfully: " + orders.size());
                        SignIn.BookedOrder = orders.get(0);
                        rvPendings.setVisibility( View.INVISIBLE);
                        BookedOrderLayout.setVisibility( View.VISIBLE);
                        inflateBookedOrder(BookedOrderLayout,SignIn.BookedOrder);

                        progressDialog.dismiss();
                        // Process and update UI with orders
                    } else {
                        Log.e("API Response", "Error: " + apiResponse.getMessage());

                        progressDialog.dismiss();
                    }
                } else {
                    Log.e("API Error", "Failed with status: " + response.code());

                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ApiAssignOrderResponse> call, Throwable t) {
                Log.e("API Error", "Network error: " + t.getMessage());

                progressDialog.dismiss();
            }
        });
    }
    private void connectWebSocket() {
        try {
            String baseAddress = getString(R.string.websocket_IP);

            URI uri = new URI(baseAddress); // Replace with your server's WebSocket URL
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket Opened");
                    runOnUiThread(() -> Toast.makeText(context, "Connected to Server", Toast.LENGTH_SHORT).show());
                    String idToSend = SignIn.caterer.getId(); // Replace with your ID value
                    webSocketClient.send(idToSend);
                    Log.d(TAG, "ID sent to server: " + idToSend);
                }
                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Message received: " + message);

                    try {
                        // Convert the message into a JSON object
                        JSONObject jsonMessage = new JSONObject(message);

                        // Check the event type
                        String eventType = jsonMessage.getString("event");

                        if ("orderCancelled".equals(eventType)) {
                            // Handle order cancellation event
                            String orderID = jsonMessage.getString("orderID");
                            String cancelMessage = jsonMessage.getString("message");

                            // Show a toast with the cancellation message
                            runOnUiThread(() ->
                                            adapter.removeorder(orderID)
                                    //Toast.makeText(getApplicationContext(), "Order Cancelled: " + cancelMessage + " (ID: " + orderID + ")", Toast.LENGTH_SHORT).show()
                            );
                        } else if ("allOrders".equals(eventType)) {
                            // Handle all orders event
                            String dataJson = jsonMessage.getJSONArray( "data" ).toString();

                            // Parse the order data
                            List<Order> orders = OrderParser.parseOrderData( dataJson );

                            for (Order order : orders) {
                                Log.d( TAG, "Order by: " + order.getName() + ", ID: " + order.getOrderID() );
                            }

                            // Update the UI with the new order data
                            runOnUiThread( () -> {
                                pendingOrders.clear();
                                pendingOrders.addAll( orders );
                                adapter.notifyDataSetChanged();
                                progressDialog.dismiss();
                            } );
                        } else if ("newOrder".equals(eventType))
                        {
                            String dataJson = jsonMessage.getJSONArray( "data" ).toString();
                            // Parse the order data
                            List<Order> orders = OrderParser.parseOrderData( dataJson );
                            runOnUiThread( () ->
                            {
                                adapter.addOrder(orders.get(0));
                            });
                        }
                        else if ("oneOrder".equals( eventType ))
                        {
                            String dataJson = jsonMessage.getJSONArray("data").toString();
                            // Parse the order data
                            List<AssignedOrder> orders = OrderParser.parseAssignedOrderData( dataJson );
                            runOnUiThread( () ->
                            {
                                Log.d(TAG,orders.get(0).getOrderID());
                                if(orders.size() == 1)
                                {
                                    SignIn.BookedOrder = orders.get(0);
                                    rvPendings.setVisibility(View.INVISIBLE);
                                    BookedOrderLayout.setVisibility(View.VISIBLE);
                                    inflateBookedOrder( BookedOrderLayout,SignIn.BookedOrder);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON message: " + e.getMessage());


                    }
                    progressDialog.dismiss();
                }
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket Closed: " + reason);
                    runOnUiThread(() -> Toast.makeText(context, "Server Closed: " + reason, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket Error: " + ex.getMessage());
                    runOnUiThread(() -> Toast.makeText(context, "Server Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Error connecting WebSocket: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(context, "Error Connecting Server: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    @SuppressLint("SetTextI18n")
    public void inflateBookedOrder(View layout, AssignedOrder order)
    {
        TextView username, userContact, fare, distance, details, destination;
        MaterialButton btnComplete,btnOpenMap;
        ImageButton sourcepin, destpin;

        username = layout.findViewById( R.id.tvUsername2 );
        username.setText( "Username: " + order.getName() );
        userContact = layout.findViewById( R.id.tvUserContact2 );
        userContact.setText( "Contact: " + order.getPhone());
        fare = layout.findViewById( R.id.tvFare2 );
        fare.setText( "Amount: " + String.valueOf(order.getFare() + " Rs.") );
        details = layout.findViewById( R.id.tvDetails2 );
        details.setText("Details: " + order.getOrderDetails());
        destination = layout.findViewById( R.id.tvDestinationAddress2 );
        destination.setText(order.getSource());
        btnComplete = layout.findViewById( R.id.btnOrderFinished2 );
        btnComplete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView messageTextView = new TextView(context);
                messageTextView.setText("Confirm that Order is Finished!");
                messageTextView.setTextSize(18); // Set the desired text size
                messageTextView.setPadding(50, 50, 50, 20); // Set padding for left and right margins
                messageTextView.setGravity( Gravity.CENTER); // Center the text
                // Set custom font
                messageTextView.setTypeface( ResourcesCompat.getFont(v.getContext(), R.font.poppins_medium));  // Apply Poppins font
                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(messageTextView);
                builder.setCancelable(true); // Allow canceling the dialog by tapping outside or pressing back

                // Set Positive Button
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    progressDialog.show();
                    // Handle logout action here
                    FinishOrder(SignIn.BookedOrder.getOrderID());
                    SignIn.BookedOrder = null;
                    dialog.dismiss();

                });
                // Set Negative Button
                builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } );
        sourcepin = layout.findViewById(R.id.btnDestinationAddressMap);
        sourcepin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourceLat = String.valueOf(SignIn.latitude);
                String sourceLng = String.valueOf(SignIn.longitude);

                // Get the destination pin (latitude,longitude)
                String destinationPin = order.getSourcePin(); // Example: "31.481218950076983,74.3029832839966"

                // Split the destinationPin into latitude and longitude
                String[] destLatLng = destinationPin.split(",");
                if (destLatLng.length == 2) {
                    String destinationLat = destLatLng[0];
                    String destinationLng = destLatLng[1];

                    // Create an Intent to show directions from the source to the destination
                    String url = "https://www.google.com/maps/dir/?api=1&origin=" + sourceLat + "," + sourceLng +
                            "&destination=" + destinationLat + "," + destinationLng + "&travelmode=driving";

                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mapIntent.setPackage("com.google.android.apps.maps"); // Ensure it opens in Google Maps
                    startActivity(mapIntent);
                }
            }
        });

    }
    public void ChangeDetailsApi(String oldpassword, String newdata, boolean password) {

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SignIn.baseURL)
                .addConverterFactory( GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        ApidataChangeDetails request = new ApidataChangeDetails(oldpassword,newdata,SignIn.caterer.getId());
        if(password)
        {
            apiService.updatePassword(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Log.d("API Response", "Order Completed successfully: " + apiResponse.getMessage());
                            Toast.makeText( context, "Password Updated Successfully", Toast.LENGTH_SHORT ).show();
                        } else {
                            Log.e("API Response", "Failed to Complete order: " + apiResponse.getMessage());
                            Toast.makeText( context, "Failed: Wrong Current Password", Toast.LENGTH_SHORT ).show();
                        }
                    } else {

                        Toast.makeText( context, "Network Error", Toast.LENGTH_SHORT ).show();
                        Log.e("API Error", "Request failed with status: " + response.code());
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {

                    Toast.makeText( context, "Network Error", Toast.LENGTH_SHORT ).show();
                    Log.e("API Error", "Network error: " + t.getMessage());
                }
            });
        }
        else
        {
            apiService.updatePhone(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Toast.makeText( context, "Phone No. Updated Successfully", Toast.LENGTH_SHORT ).show();
                            Log.d("API Response", "Order Completed successfully: " + apiResponse.getMessage());

                        } else {
                            Toast.makeText( context, "Failed: Wrong Current Password", Toast.LENGTH_SHORT ).show();
                            Log.e("API Response", "Failed to Complete order: " + apiResponse.getMessage());
                        }
                    } else {
                        Toast.makeText( context, "Network Error", Toast.LENGTH_SHORT ).show();
                        Log.e("API Error", "Request failed with status: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {

                    Toast.makeText( context, "Network Error", Toast.LENGTH_SHORT ).show();
                    Log.e("API Error", "Network error: " + t.getMessage());
                }
            });

        }

    }
    public void ChangeDetails(String newdata, Boolean password)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView2 = inflater.inflate(R.layout.confirmation_password, null);

        // Create an AlertDialog for the confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView2);

        // Add custom behavior if required for views in the confirmation_password.xml
        AlertDialog confirmationDialog = builder.create();


        // Show the dialog
        confirmationDialog.show();

        MaterialButton btnConfirm, btnClose;
        TextInputEditText etPasswordConfirm;
        etPasswordConfirm = confirmationDialog.findViewById( R.id.etConfirmPassword);
        btnConfirm = confirmationDialog.findViewById( R.id.btnConfirmPasswordConfirm );
        btnConfirm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password)
                {
                    ChangeDetailsApi( etPasswordConfirm.getText().toString().trim(),newdata,true);
                }
                else
                {
                    ChangeDetailsApi( etPasswordConfirm.getText().toString().trim(),newdata,false);
                }
            }
        } );
        btnClose = confirmationDialog.findViewById( R.id.btnConfirmPasswordClose );
        btnClose.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.dismiss();
            }
        } );

    }
    public void FinishOrder(String orderID) {
        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SignIn.baseURL)
                .addConverterFactory( GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Create the request body
        AcceptOrderData request = new AcceptOrderData(SignIn.BookedOrder.getOrderID(), SignIn.BookedOrder.getCatererID());
        //String request = SignIn.BookedOrder.getOrderID();
        // Make the API call
        apiService.markOrderCompleted(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d("API Response", "Order Completed successfully: " + apiResponse.getMessage());
                        Toast.makeText( context, "Order Completed Successfully", Toast.LENGTH_SHORT ).show();
                        rvPendings.setVisibility( View.VISIBLE );
                        BookedOrderLayout.setVisibility( View.INVISIBLE);

                        progressDialog.dismiss();
                    } else {
                        Log.e("API Response", "Failed to Complete order: " + apiResponse.getMessage());
                        Toast.makeText( context, "Unexpected Issue", Toast.LENGTH_SHORT ).show();

                        progressDialog.dismiss();
                    }
                } else {
                    Log.e("API Error", "Request failed with status: " + response.code());
                    Toast.makeText( context, "Network Issue", Toast.LENGTH_SHORT ).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API Error", "Network error: " + t.getMessage());
                Toast.makeText( context, "Netword Issue", Toast.LENGTH_SHORT ).show();
                progressDialog.dismiss();
            }
        });
    }
    public class ApidataChangeDetails {
        String oldpassword;
        String newdata;
        String catererID;
        ApidataChangeDetails(String oldpassword,String newdata,String transporterID)
        {
            this.oldpassword = oldpassword;
            this.newdata = newdata;
            this.catererID = transporterID;
        }


    }
    public class ApiAssignOrderResponse {
        private boolean success;
        private String message; // Optional, only for failure
        private List<AssignedOrder> orders; // List of assigned orders

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<AssignedOrder> getOrders() {
            return orders;
        }
    }


}