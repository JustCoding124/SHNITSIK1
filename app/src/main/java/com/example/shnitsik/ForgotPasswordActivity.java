package com.example.shnitsik;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextView backToSignIn;
    private EditText emailEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email_edit_text);
        resetPasswordButton = findViewById(R.id.submit_button);
        backToSignIn = findViewById(R.id.back_to_login);
        backToSignIn.setOnClickListener(v -> back_to_signin());

        resetPasswordButton.setOnClickListener(v -> {if (TextUtils.isEmpty(emailEditText.getText().toString().trim())) emailEditText.setError("Please Enter Email");
            else{send_link(emailEditText.getText().toString().trim());}});

    }
    public static boolean isInputValid(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void send_link(String email){
        if (isInputValid(emailEditText.getText().toString().trim())){
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Notify the user that the reset email has been sent
                                Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle failure
                                Exception exception = task.getException();
                                String errorMessage = (exception != null) ? exception.getMessage() : "Unknown error occurred";
                                Toast.makeText(ForgotPasswordActivity.this, "Error " + errorMessage, Toast.LENGTH_SHORT).show();                            }
                        }
                    });
        }
    }
    private void  back_to_signin(){
        Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}