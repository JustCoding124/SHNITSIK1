package com.example.shnitsik;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    @NonNull


    private Context context;
    private List<Order> orderList;
    public MyAdapter(Context context, List<Order> orderList) {
        this.orderList = orderList;
        this.context = context;
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
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
            isFinished = itemView.findViewById(R.id.orderTime);
            idealPrepTime = itemView.findViewById(R.id.idealpreptime);
            changeStatusButton = itemView.findViewById(R.id.changestatusbutton);

        }
    }
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ///הופך קובץ xml לאובייקט Java מסוג View שניתן להשתמש בו בתוכנה
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order currentOrder = orderList.get(position);
        holder.id.setText(currentOrder.getoId());
        holder.customerId.setText(currentOrder.getOrdererUID());
        holder.subtitle.setText(currentOrder.getProductsString());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
        holder.orderTime.setText(formatter.format(new Date(currentOrder.getDateOfOrder())));
        holder.idealPrepTime.setText(formatter.format(new Date(currentOrder.getIdealPrepTime())));

        holder.changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentOrder.setFinished(!currentOrder.getIsFinished());
            }
        });
        String status="";
        status = currentOrder.getIsFinished() ?"Finished":"Pending";
        if (!currentOrder.getIsFinished()){
            holder.isFinished.setTextColor(Color.RED);
        }
        holder.isFinished.setText(status);
        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(currentOrder.getoId()).setTitle(currentOrder.getoId());
            String productDescription =  productWideDescription(currentOrder);
            builder.setMessage("Status (Finished?): "+ currentOrder.getIsFinished() + "/nTime Ordered: "+ formatter2.format(currentOrder.getDateOfOrder()) +"/nProduct Description: " + productDescription + "/nUser ID: " + currentOrder.getOrdererUID() + "/nOrder ID: " + currentOrder.getoId());
            builder.setNegativeButton("Return", (dialog, which) -> {
                dialog.dismiss();
            });
        });

    }
    @Override
    public int getItemCount() {
        return this.orderList.size();
    }
    public String productWideDescription(Order currentOrder){
        StringBuilder description= new StringBuilder();
        Product[] products = currentOrder.getProducts();
        for (Product p:products) {
            description.append(p.getAddOnDescription());
        }
        return description.toString();
    }
}
