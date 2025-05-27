package com.example.shnitsik.models;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shnitsik.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The type My adapter.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private List<Order> orderList;

    /**
     * Instantiates a new My adapter.
     *
     * @param context   the context
     * @param orderList the order list
     */
    public MyAdapter(Context context, List<Order> orderList) {
        this.orderList = orderList;
        this.context = context;
    }

    /**
     * The type My view holder.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Id.
         */
        TextView id;
        /**
         * The Total to pay text view.
         */
        TextView totalToPayTextView;
        /**
         * The Customer id.
         */
        TextView customerId;
        /**
         * The Subtitle.
         */
        TextView subtitle;
        /**
         * The Order time.
         */
        TextView orderTime;
        /**
         * The Is finished.
         */
        TextView isFinished;
        /**
         * The Ideal prep time.
         */
        TextView idealPrepTime;
        /**
         * The Change status button.
         */
        Button changeStatusButton;

        /**
         * Instantiates a new My view holder.
         *
         * @param itemView the item view
         */
        public MyViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.orderId);
            totalToPayTextView = itemView.findViewById(R.id.totalToPayTextView);
            customerId = itemView.findViewById(R.id.customerId);
            subtitle = itemView.findViewById(R.id.orderDetails);
            orderTime = itemView.findViewById(R.id.orderTime);
            isFinished = itemView.findViewById(R.id.isFinished);
            idealPrepTime = itemView.findViewById(R.id.idealpreptime);
            changeStatusButton = itemView.findViewById(R.id.changestatusbutton);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order currentOrder = orderList.get(position);
        holder.id.setText(currentOrder.getoId());
        holder.customerId.setText(currentOrder.getOrdererUID());
        holder.subtitle.setText(currentOrder.getProductsString());

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        holder.orderTime.setText(formatter.format(new Date(currentOrder.getRequestedTime())));
        holder.idealPrepTime.setText(formatter.format(new Date(currentOrder.getIdealPrepTime())));

        updateStatusText(holder, currentOrder);
        // הצגת סכום לתשלום אם יש
        if (currentOrder.getTotalToPay() > 0) {
            holder.totalToPayTextView.setText("Cash Total: ₪" + currentOrder.getTotalToPay());
            holder.totalToPayTextView.setVisibility(View.VISIBLE);
        } else {
            holder.totalToPayTextView.setVisibility(View.GONE);
        }

        holder.changeStatusButton.setOnClickListener(v -> {
            currentOrder.setFinished(!currentOrder.getIsFinished());
            updateStatusText(holder, currentOrder);

            // עדכון ב-Firebase של השדה isFinished
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Root/Orders")
                    .child(String.valueOf(currentOrder.getIdealPrepTime()))
                    .child(currentOrder.getoId());

            ref.child("isFinished").setValue(currentOrder.getIsFinished());

        });

        holder.itemView.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_order_details, null);
            TextView detailsText = dialogView.findViewById(R.id.detailsTextView);

            String productDescription = productWideDescription(currentOrder);

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Status (Finished?): ").append(currentOrder.getIsFinished())
                    .append("\nTime Ordered: ").append(formatter2.format(currentOrder.getDateOfOrder()))
                    .append("\nProduct Description: ").append(productDescription)
                    .append("\nUser ID: ").append(currentOrder.getOrdererUID())
                    .append("\nOrder ID: ").append(currentOrder.getoId());

            if (currentOrder.getTotalToPay() > 0) {
                messageBuilder.append("\nTotal To Pay (CASH): ₪").append(currentOrder.getTotalToPay());
            }

            detailsText.setText(messageBuilder.toString());

            new AlertDialog.Builder(context)
                    .setTitle("Order Details: " + currentOrder.getoId())
                    .setView(dialogView)
                    .setNegativeButton("Return", (dialog, which) -> dialog.dismiss())
                    .show();
        });



    }

    private void updateStatusText(MyViewHolder holder, Order currentOrder) {
        String status = currentOrder.getIsFinished() ? "Finished" : "Pending";
        holder.isFinished.setText(status);
        holder.isFinished.setTextColor(currentOrder.getIsFinished() ? Color.GREEN : Color.RED);
    }

    @Override
    public int getItemCount() {
        return this.orderList.size();
    }

    /**
     * Product wide description string.
     *
     * @param currentOrder the current order
     * @return the string
     */
    public String productWideDescription(Order currentOrder) {
        StringBuilder description = new StringBuilder();
        List<Product> products = currentOrder.getProducts();
        for (Product p : products) {
            description.append(p.getProductName()).append(": ").append(p.getAddOnDescription()).append("\n");
        }
        return description.toString();
    }
}
