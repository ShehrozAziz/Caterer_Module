package com.example.caterer_module;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.PendingViewHolder> {
    private List<Order> orders;
    private Context context;
    private int openedPosition = -1;  // No item is opened initially
    private int swipedPosition = -1;

    public PendingOrdersAdapter(List<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_pending_order, parent, false);
        return new PendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingViewHolder holder, int position) {
        Order order = orders.get(position);holder.tvUsername.setText(order.getName());
        holder.tvFare.setText("Amount: " + String.valueOf(order.getFare() + " Rs."));
        holder.tvPhone.setText(order.getPhone());
        String[] sourceCoordinates = order.getSourcePin().split(",");

        // Convert the extracted coordinates to doubles
        double sourceLat = Double.parseDouble(sourceCoordinates[0]);
        double sourceLon = Double.parseDouble(sourceCoordinates[1]);

        double distance = haversine(SignIn.latitude,SignIn.longitude,sourceLat, sourceLon);
        String formattedDistance = String.format("%.2f", distance);
        double formattedDistanceValue = Double.parseDouble(formattedDistance);
        holder.tvDistance.setText(formattedDistance+" Kms");

        if (position == swipedPosition) {
            holder.btnMoreInfo2.setVisibility(View.VISIBLE);
            holder.btnswipe.setText("Close ⬇\uFE0F");
            holder.tvOrderType.setVisibility( View.INVISIBLE );
            holder.tvDistance.setVisibility( View.INVISIBLE );
            holder.tvOrderType.setVisibility( View.INVISIBLE );
            holder.tvAwayText.setVisibility( View.INVISIBLE);
            holder.btnswipe.setClickable(true);
        } else {
            holder.btnMoreInfo2.setVisibility(View.GONE);
            holder.btnswipe.setText("Swipe ⬆\uFE0F");
            holder.btnswipe.setClickable(false);
            holder.tvOrderType.setVisibility( View.VISIBLE );
            holder.tvDistance.setVisibility( View.VISIBLE );
            holder.tvOrderType.setVisibility( View.VISIBLE );
            holder.tvAwayText.setVisibility( View.VISIBLE );
        }

        // Check if the current position is the opened one
        if (holder.getAdapterPosition() == openedPosition) {
            // Show the "More Info" button and set the swipe button text to "Close"

            collapseView(holder.mcvTextualArea,holder.btnMoreInfo);

            //resizeParent(holder.itemView, true);
        } else {
            // Hide the "More Info" button and set the swipe button text to "More Info"
            expandView(holder.mcvTextualArea,holder.btnMoreInfo);

            //resizeParent(holder.itemView, false);
        }

        // Handle the swipe button click
        holder.btnswipe.setOnClickListener(v -> {
            if (swipedPosition == holder.getAdapterPosition()) {
                // If this item is already opened, close it
                swipedPosition = -1;
                notifyItemChanged(holder.getAdapterPosition());
            }
        });
        holder.btnMoreInfo2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                // Set the title of the dialog
                builder.setTitle("More Info");

                // Create a linear layout to hold the TextViews for displaying data
                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(40, 40, 40, 40); // Padding around the layout

                // Create TextViews for the details
                TextView sourceAddressTextView = createStyledTextView(v, "Address: " + order.getSourceAddress());
                TextView fareTextView = createStyledTextView(v, "Amount: " + String.valueOf(order.getFare()) + " Rs.");
                TextView usernameTextView = createStyledTextView(v, "Username: " + order.getName());
                TextView phoneTextView = createStyledTextView(v, "Phone: " + order.getPhone());
                TextView detailsTextView = createStyledTextView( v, "Details: " + order.getOrderDetails() );

                // Add the TextViews to the layout
                layout.addView(sourceAddressTextView);
                layout.addView(fareTextView);
                layout.addView(usernameTextView);
                layout.addView(phoneTextView);
                layout.addView(detailsTextView);

                // Set the layout to the AlertDialog builder
                builder.setView(layout);

                // Set the positive button (Accept)
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the accept button click
                        // You can add logic here for what happens when Accept is clicked
                        SharedPrefsManager manager = new SharedPrefsManager(context);
                        Caterer caterer = manager.getCaterer();
                        Log.d("Adapter",caterer.getId());
                        if(orders.contains(order))
                        {
                            MainActivity.etHiddenEditText.setText("Pending");
                            assignOrder( order.getOrderID(),SignIn.caterer.getId());
                            swipedPosition = -1;
                        }
                        else
                        {
                            Toast.makeText( context, "Order Deleted or Not Found", Toast.LENGTH_SHORT ).show();
                        }

                        dialog.dismiss();
                    }
                });

                // Set the negative button (Close)
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the close button click
                        dialog.dismiss();  // Dismiss the dialog
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
        );
    }
    public void assignOrder(String orderID, String transporterID) {

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(/*"http:" + */SignIn.baseURL)
                .addConverterFactory( GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Create the request body
        AcceptOrderData request = new AcceptOrderData(orderID, transporterID);

        SharedPrefsManager manager = new SharedPrefsManager( context );

        // Make the API call
        apiService.assignOrder(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d("API Response", "Order assigned successfully: " + apiResponse.getMessage());
                        MainActivity.etHiddenEditText.setText("No");


                    } else {
                        Log.e("API Response", "Failed to assign order: " + apiResponse.getMessage());
                        MainActivity.etHiddenEditText.setText("Yes");
                    }
                } else {
                    Log.e("API Error", "Request failed with status: " + response.code());
                    MainActivity.etHiddenEditText.setText("Dont");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API Error", "Network error: " + t.getMessage());
            }
        });
    }
    public void addOrder(Order order)
    {
        orders.add(order);
        notifyDataSetChanged();
    }
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in kilometers
        final int R = 6371;

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Differences in coordinates
        double dlat = lat2Rad - lat1Rad;
        double dlon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in kilometers
        double distance = R * c;

        return distance;
    }
    private TextView createStyledTextView(View v, String text) {
        TextView textView = new TextView(v.getContext());
        textView.setText(text);
        textView.setTextSize(18);  // Increase text size
        textView.setTypeface( ResourcesCompat.getFont(v.getContext(), R.font.poppins_medium));  // Apply Poppins font

        // Apply margin and padding
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(20, 20, 20, 20);  // Add margin to the left, top, right, and bottom
        textView.setLayoutParams(params);
        textView.setGravity( Gravity.START);  // Align the text to the left
        textView.setTextColor(v.getResources().getColor(android.R.color.black)); // Set text color

        return textView;
    }
    public void removeorder(String id)
    {
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getOrderID().equals(id)) {
                iterator.remove(); // Remove the order if the ID matches
                break; // Exit loop once the order is found and removed
            }
        }
        // Notify the adapter about the change
        notifyDataSetChanged();
    }

    // Helper method to animate the visibility of the button (fade in/out)
    // Method to expand the Textual area and hide the btnMoreInfo button
    private void expandView(final View textualArea, final View btnMoreInfo) {
        // Hide the More Info button
        btnMoreInfo.setVisibility(View.GONE);

        // Measure the parent width to expand to full width
        ViewGroup parent = (ViewGroup) textualArea.getParent();
        final int targetWidth = parent.getMeasuredWidth(); // Full width

        // Animate the width of the Textual area to match parent
        ValueAnimator widthAnimator = ValueAnimator.ofInt(textualArea.getWidth(), targetWidth);
        widthAnimator.addUpdateListener(valueAnimator -> {
            LayoutParams layoutParams = textualArea.getLayoutParams();
            layoutParams.width = (int) valueAnimator.getAnimatedValue();
            textualArea.setLayoutParams(layoutParams);
        });

        // Set duration for smooth animation
        widthAnimator.setDuration(200);
        widthAnimator.start();
    }
    public void showButtons(int position) {
        swipedPosition = position;
        notifyDataSetChanged();
    }

    public void hideButtons(int position) {
        if (swipedPosition == position) {
            swipedPosition = -1;
        }
        notifyDataSetChanged();
    }
    // Method to collapse the Textual area and show the btnMoreInfo button
    private void collapseView(final View textualArea, final View btnMoreInfo) {
        // Get the full width of the parent layout and the width of btnMoreInfo
        ViewGroup parent = (ViewGroup) textualArea.getParent();
        int totalParentWidth = parent.getMeasuredWidth();
        int buttonWidth = btnMoreInfo.getMeasuredWidth();

        // Target width is the parent's width minus the button's width
        int targetWidth = totalParentWidth - buttonWidth;

        // Animate the width of the Textual area to accommodate the More Info button
        ValueAnimator widthAnimator = ValueAnimator.ofInt(textualArea.getWidth(), targetWidth);
        widthAnimator.addUpdateListener(valueAnimator -> {
            LayoutParams layoutParams = textualArea.getLayoutParams();
            layoutParams.width = (int) valueAnimator.getAnimatedValue();
            textualArea.setLayoutParams(layoutParams);
        });

        // Set listener to show the More Info button once the animation ends
        widthAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                btnMoreInfo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationStart(@NonNull Animator animation) {}

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });

        // Set duration for smooth animation
        widthAnimator.setDuration(200);
        widthAnimator.start();
    }


    @Override
    public int getItemCount() {
        return orders.size();
    }

    // Resizing logic for opening and closing the parent layout
    private void resizeParent(View itemView, boolean expand) {
        final ViewGroup parentLayout = (ViewGroup) itemView;

        int totalParentWidth = parentLayout.getWidth();
        MaterialCardView btnMoreInfo = parentLayout.findViewById(R.id.btnMoreInfo);
        int buttonWidth = btnMoreInfo.getWidth();

        // Animate the width of the parent layout to either expand or collapse
        int targetWidth = expand ? totalParentWidth - buttonWidth : totalParentWidth;

        ValueAnimator animator = ValueAnimator.ofInt(parentLayout.getWidth(), targetWidth);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // Dynamically set the width of the parent layout during the animation
                LayoutParams layoutParams = parentLayout.getLayoutParams();
                layoutParams.width = (int) valueAnimator.getAnimatedValue();
                parentLayout.setLayoutParams(layoutParams);
            }
        });

        // Set duration for smooth animation (in milliseconds)
        animator.setDuration(500); // Adjust duration if needed for smoother animation

        // Start the animation
        animator.start();
    }

    public static class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhone,tvUsername,tvFare,tvDistance,tvOrderType,tvAwayText;

        Button btnswipe;
        MaterialCardView btnMoreInfo,mcvTextualArea,btnMoreInfo2;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAwayText = itemView.findViewById(R.id.tvAwayText);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvFare = itemView.findViewById(R.id.tvFare);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvUsername = itemView.findViewById(R.id.tvUserName);
            mcvTextualArea = itemView.findViewById(R.id.mcvtextualarea);
            btnMoreInfo2 = itemView.findViewById(R.id.btnMoreInfo2);
            btnswipe = itemView.findViewById(R.id.tvSwipeIndicator);
            btnMoreInfo = itemView.findViewById(R.id.btnMoreInfo);
            tvOrderType = itemView.findViewById(R.id.tvOrderType);
        }
    }
}
