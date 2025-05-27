package com.example.shnitsik.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying a list of previous orders in a RecyclerView.
 * This adapter is responsible for taking a list of {@link Order} objects and
 * efficiently displaying them as individual items within a scrolling list.
 * Each item in the list will utilize the standard Android layout `android.R.layout.simple_list_item_2`,
 * which provides two lines of text.
 * The first line (title) will show a concatenated string of product names
 * and their respective add-ons for a given order.
 * The second line (subtitle) will display the date and time the order was placed.
 *
 * This adapter manages the creation of ViewHolders ({@link OrderViewHolder})
 * which hold references to the views for each item, and binds the data from
 * the {@link Order} objects to these views.
 *
 * @author Ariel Kanitork
 */
public class PreviousOrdersAdapter extends RecyclerView.Adapter<PreviousOrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    /**
     * Instantiates a new Previous orders adapter.
     *
     * @param context   the context
     * @param orderList the order list
     */
    public PreviousOrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // תאריך
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(order.getDateOfOrder()));
        holder.subtitle.setText("Ordered on: " + date);

        // תיאור ההזמנה (מוצרים ותוספות)
        StringBuilder description = new StringBuilder();
        for (Product p : order.getProducts()) {
            description.append(p.getProductName());
            List<AddOn> addOns = p.getAddOns();
            if (addOns != null && !addOns.isEmpty()) {
                description.append(" (");
                for (AddOn a : addOns) {
                    if (a.getAmount() > 0) {
                        description.append(a.getAddOnName())
                                .append(" x")
                                .append(a.getAmount())
                                .append(", ");
                    }
                }
                if (description.toString().endsWith(", ")) {
                    description.setLength(description.length() - 2); // להסיר פסיק אחרון
                }
                description.append(")");
            }
            description.append("\n");
        }

        holder.title.setText(description.toString().trim());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    /**
     * The type Order view holder.
     */
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Title.
         */
        TextView title, /**
         * The Subtitle.
         */
        subtitle;

        /**
         * Instantiates a new Order view holder.
         *
         * @param itemView the item view
         */
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
