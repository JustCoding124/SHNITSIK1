package com.example.shnitsik;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView signUpTextView, forgotPasswordTextView;
    private CheckBox rememberMeCheckBox;
    private ProgressBar progressBar;
    private FirebaseAuth fbAuth;

    private final String CHANNEL_ID = "order_channel";
    private boolean permissionGranted = false;
    private boolean tryAutoLoginAfterPermission = false;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonSignIn);
        signUpTextView = findViewById(R.id.SignUp);
        forgotPasswordTextView = findViewById(R.id.ForgotPassword);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        progressBar = findViewById(R.id.progress_bar);

        fbAuth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        createNotificationChannel();
        checkNotificationPermission();

        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordTextView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class)));
        signUpTextView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                onPermissionResult(true);
            }
        } else {
            onPermissionResult(true);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);

    private void onPermissionResult(boolean isGranted) {
        permissionGranted = isGranted;
        if (!isGranted) {
            Toast.makeText(this, "Notification permission is required for placing orders", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isRemembered = preferences.getBoolean("isRemembered", false);
        if (isRemembered) {
            String savedEmail = preferences.getString("email", "");
            String savedPassword = preferences.getString("password", "");
            emailInput.setText(savedEmail);
            passwordInput.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
            loginUser();
        }


    }

    private void loginUser() {
        if (!permissionGranted) {
            Toast.makeText(this, "You must allow notifications to login", Toast.LENGTH_LONG).show();
            return;
        }

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Please Enter Email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Please Enter Password");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                FirebaseUser user = fbAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    if (rememberMeCheckBox.isChecked()) {
                        preferences.edit()
                                .putBoolean("isRemembered", true)
                                .putString("email", email)
                                .putString("password", password)
                                .apply();
                    } else {
                        preferences.edit().putBoolean("isRemembered", false).apply();
                    }

                    Toast.makeText(MainActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, FragmentsCenterActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Please Verify Your Email", Toast.LENGTH_LONG).show();
                    if (user != null) {
                        user.sendEmailVerification().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } else {
                String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notifications";
            String description = "Notifies users when orders are ready or in prep";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
