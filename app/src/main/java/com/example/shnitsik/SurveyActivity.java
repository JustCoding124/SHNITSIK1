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
 * The type Survey activity.
 */
public class SurveyActivity extends AppCompatActivity {

    private Button satisfiedButton, notSatisfiedButton;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

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
