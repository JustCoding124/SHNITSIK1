package com.example.shnitsik;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * SurveyActivity allows users to submit a satisfaction vote regarding the cafeteria.
 * The user can vote "satisfied" or "not satisfied". The result is saved in Firestore
 * under the user's UID in the "Survey" collection, replacing any previous vote.
 * After voting, the activity finishes and returns RESULT_OK to the calling fragment.
 *
 * This activity demonstrates proper usage of ActivityResultLauncher and Firestore update logic.
 * It ensures that each user can only have one active vote.
 */
public class SurveyActivity extends AppCompatActivity {

    private Button satisfiedButton, notSatisfiedButton;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    /**
     * Initializes the activity, views, Firebase instances,
     * and sets click listeners for voting buttons.
     *
     * @param savedInstanceState previous state (if any)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survey);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        satisfiedButton = findViewById(R.id.buttonSatisfied);
        notSatisfiedButton = findViewById(R.id.buttonNotSatisfied);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        satisfiedButton.setOnClickListener(v -> submitSurvey(true));
        notSatisfiedButton.setOnClickListener(v -> submitSurvey(false));
    }

    /**
     * Submits the user's satisfaction vote to Firestore.
     * If a vote already exists for this user, it will be overwritten.
     *
     * @param isSatisfied true if the user is satisfied, false otherwise
     */
    private void submitSurvey(boolean isSatisfied){
        String uid = currentUser.getUid();
        Map<String, Object> surveyData = new HashMap<>();
        surveyData.put("satisfied", isSatisfied);

        firestore.collection("Survey")
                .document(uid) // ensures one vote per user
                .set(surveyData)
                .addOnSuccessListener(unused -> {
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit survey", Toast.LENGTH_SHORT).show();
                });
    }
}
