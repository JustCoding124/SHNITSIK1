package com.example.shnitsik.models;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private String currentUserId;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    public void setCurrentUserId(String uid) {
        this.currentUserId = uid;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.title.setText(review.getUserName() + " - " + review.getTimestamp());
        holder.subtitle.setText(review.getText());

        holder.itemView.setOnClickListener(v -> {
            if (review.getUserId().equals(currentUserId)) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Review")
                        .setMessage("Do you want to delete this review?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseDatabase.getInstance()
                                    .getReference("Root/Reviews")
                                    .child(review.getId())
                                    .removeValue()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
