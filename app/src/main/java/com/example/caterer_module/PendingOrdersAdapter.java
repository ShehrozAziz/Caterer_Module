package com.example.caterer_module;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PendingOrdersAdapter extends RecyclerView.Adapter<PendingOrdersAdapter.PendingViewHolder> {
    private List<String> orders;
    private Context context;
    private int openedPosition = -1;  // No item is opened initially
    private int swipedPosition = -1;

    public PendingOrdersAdapter(List<String> orders, Context context) {
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
        String order = orders.get(position);
        holder.tvGraveID.setText(order);
        if (position == swipedPosition) {
            holder.btnMoreInfo2.setVisibility(View.VISIBLE);
            holder.btnswipe.setText("Close ⬇\uFE0F");
            holder.tvResidenceArea.setVisibility(View.INVISIBLE);
            holder.tvUsername.setVisibility(View.INVISIBLE);
            holder.tvMoistureSensor.setVisibility(View.INVISIBLE);
            holder.btnswipe.setClickable(true);
        } else {
            holder.btnMoreInfo2.setVisibility(View.GONE);
            holder.btnswipe.setText("Swipe ⬆\uFE0F");
            holder.btnswipe.setClickable(false);
            holder.tvResidenceArea.setVisibility(View.VISIBLE);
            holder.tvUsername.setVisibility(View.VISIBLE);
            holder.tvMoistureSensor.setVisibility(View.VISIBLE);
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
        TextView tvGraveID,tvGraveType,tvUsername,tvResidenceArea,tvMoistureSensor,tvStatus;

        Button btnswipe;
        MaterialCardView btnMoreInfo,mcvTextualArea,btnMoreInfo2;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGraveID = itemView.findViewById(R.id.tvGraveID);
            btnswipe = itemView.findViewById(R.id.tvSwipeIndicator);
            btnMoreInfo = itemView.findViewById(R.id.btnMoreInfo);
            mcvTextualArea = itemView.findViewById(R.id.mcvtextualarea);
            tvGraveType = itemView.findViewById(R.id.tvGraveType);
            tvMoistureSensor = itemView.findViewById(R.id.tvSensorEnability);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvResidenceArea = itemView.findViewById(R.id.tvUserResidence);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnMoreInfo2 = itemView.findViewById(R.id.btnMoreInfo2);
        }
    }
}
