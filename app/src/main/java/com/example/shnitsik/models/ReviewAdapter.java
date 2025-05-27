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

/**
 * Adapter for displaying a list of reviews in a RecyclerView.
 * This adapter is responsible for creating view holders, binding data to them,
 * and handling user interactions such as deleting a review.
 *
 * @author Ariel Kanitork
 *
 * The {@code ReviewAdapter} class serves as a bridge between the {@link RecyclerView} and the
 * underlying data set (a list of {@link Review} objects). It manages the creation of individual
 * item views ({@link ReviewViewHolder}) and binds the specific data from each {@code Review}
 * object to its corresponding view.
 *
 * It holds references to:
 * <ul>
 *   <li>{@code context}: The {@link Context} from which the adapter is created. This is
 *       used for layout inflation and displaying dialogs or toasts.</li>
 *   <li>{@code reviewList}: A {@link List} of {@link Review} objects that represents the
 *       data to be displayed in the RecyclerView.</li>
 *   <li>{@code currentUserId}: A {@link String} representing the ID of the currently logged-in
 *       user. This is used to determine if a user can delete a specific review (only the author
 *       of the review can delete it).</li>
 * </ul>
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private String currentUserId;

    /**
     * Instantiates a new Review adapter.
     *
     * @param context    the context
     * @param reviewList the review list
     */
    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    /**
     * Sets current user id.
     *
     * @param uid the uid
     */
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

    /**
     * The type Review view holder.
     */
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Title.
         */
        TextView title, /**
         * The Subtitle.
         */
        subtitle;

        /**
         * Instantiates a new Review view holder.
         *
         * @param itemView the item view
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}
