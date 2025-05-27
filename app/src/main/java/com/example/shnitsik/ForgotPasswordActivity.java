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

/**
 * The type Forgot password activity.
 */
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

    /**
     * Is input valid boolean.
     *
     * @param email the email
     * @return the boolean
     */
    public static boolean isInputValid(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void send_link(String email) {
        if (isInputValid(email)) {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "If an account with that email exists, a reset link has been sent.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Failed to send reset link. Please check the email address.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            emailEditText.setError("Please enter a valid email address.");
        }
    }
    private void  back_to_signin(){
        Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}