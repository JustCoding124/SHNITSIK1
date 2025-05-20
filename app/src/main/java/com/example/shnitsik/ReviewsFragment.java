package com.example.shnitsik;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shnitsik.models.Review;
import com.example.shnitsik.models.ReviewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReviewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText reviewEditText;
    private Button submitButton;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private FirebaseUser currentUser;
    private DatabaseReference reviewRef;
    private boolean isAdmin = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        recyclerView = view.findViewById(R.id.reviewsRecyclerView);
        reviewEditText = view.findViewById(R.id.reviewEditText);
        submitButton = view.findViewById(R.id.submitReviewButton);

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(reviewAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reviewRef = FirebaseDatabase.getInstance().getReference("Root/Reviews");

        fetchReviews();
        checkUserRole();

        submitButton.setOnClickListener(v -> submitReview());

        return view;
    }

    private void fetchReviews() {
        reviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Review r = child.getValue(Review.class);
                    if (r != null) reviewList.add(r);
                }
                reviewAdapter.setCurrentUserId(currentUser.getUid());
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load reviews", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole() {
        FirebaseDatabase.getInstance().getReference("Root/Users/" + currentUser.getUid() + "/role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean role = snapshot.getValue(Boolean.class);
                        isAdmin = role != null && role;

                        if (isAdmin) {
                            reviewEditText.setVisibility(View.GONE);
                            submitButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error checking role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitReview() {
        String content = reviewEditText.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Review cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewId = reviewRef.push().getKey();
        String userId = currentUser.getUid();

        // שליפת שם המשתמש לפי ה-UID הנוכחי
        FirebaseDatabase.getInstance().getReference("Root/Users/" + userId + "/userName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String username = snapshot.getValue(String.class);
                        if (username == null || username.isEmpty()) {
                            username = "Unknown";
                        }

                        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());

                        Review review = new Review(reviewId, userId, content, username, timestamp);

                        reviewRef.child(reviewId).setValue(review)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Review submitted", Toast.LENGTH_SHORT).show();
                                    reviewEditText.setText("");
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to submit review", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to fetch user name", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
