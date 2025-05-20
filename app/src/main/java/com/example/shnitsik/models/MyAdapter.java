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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private List<Order> orderList;

    public MyAdapter(Context context, List<Order> orderList) {
        this.orderList = orderList;
        this.context = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView id;
        TextView customerId;
        TextView subtitle;
        TextView orderTime;
        TextView isFinished;
        TextView idealPrepTime;
        Button changeStatusButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.orderId);
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

        holder.orderTime.setText(formatter.format(new Date(currentOrder.getDateOfOrder())));
        holder.idealPrepTime.setText(formatter.format(new Date(currentOrder.getIdealPrepTime())));

        updateStatusText(holder, currentOrder);

        holder.changeStatusButton.setOnClickListener(v -> {
            currentOrder.setFinished(!currentOrder.getIsFinished());
            updateStatusText(holder, currentOrder);

            if (currentOrder.getIsFinished()) {
                NotificationUtils.sendNotification(context, currentOrder.getoId(), "Order Ready", "Order is ready for pickup!");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Order Details: " + currentOrder.getoId());
            String productDescription = productWideDescription(currentOrder);
            builder.setMessage(
                    "Status (Finished?): " + currentOrder.getIsFinished() +
                            "\nTime Ordered: " + formatter2.format(currentOrder.getDateOfOrder()) +
                            "\nProduct Description: " + productDescription +
                            "\nUser ID: " + currentOrder.getOrdererUID() +
                            "\nOrder ID: " + currentOrder.getoId()
            );
            builder.setNegativeButton("Return", (dialog, which) -> dialog.dismiss());
            builder.show();
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

    public String productWideDescription(Order currentOrder) {
        StringBuilder description = new StringBuilder();
        List<Product> products = currentOrder.getProducts();
        for (Product p : products) {
            description.append(p.getProductName()).append(": ").append(p.getAddOnDescription()).append("\n");
        }
        return description.toString();
    }
}
