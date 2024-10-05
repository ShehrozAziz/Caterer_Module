package com.example.caterer_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ImageButton btnMore;
    RecyclerView rvPendings;
    public static BadgeDrawable Pending;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.BG)); // Replace 'your_color' with the desired color
        setContentView(R.layout.activity_main);
        btnMore = findViewById(R.id.btnLogout);
        rvPendings = findViewById(R.id.rvPendings);
        List<String> pendingOrders = new ArrayList<>();
        pendingOrders.add("Order 1");
        pendingOrders.add("Order 2");
        pendingOrders.add("Order 3");
        rvPendings.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        PendingOrdersAdapter adapter = new PendingOrdersAdapter(pendingOrders,MainActivity.this);
        rvPendings.setAdapter(adapter);
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
        TextView tvCarLocation = dialogView.findViewById(R.id.tvCarLocation);
        TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
        TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);

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
                    tvEmail.setVisibility(View.GONE);
                } else {
                    // Keyboard is closed, show the elements
                    tvName.setVisibility(View.VISIBLE);
                    tvCarLocation.setVisibility(View.VISIBLE);
                    tvPhone.setVisibility(View.VISIBLE);
                    tvEmail.setVisibility(View.VISIBLE);
                }
            }
        });

        // Optional: Adjust the dialog window's soft input mode
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }


}