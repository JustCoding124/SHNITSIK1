package com.example.shnitsik;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shnitsik.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * The type Sign up activity.
 */
public class SignUpActivity extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private EditText userNameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button createAccountButton;
    private TextView alreadyHaveAccountText;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        emailInput = findViewById(R.id.editTextEmail);
        userNameInput = findViewById(R.id.userNameInput);
        passwordInput = findViewById(R.id.editTextPassword);
        confirmPasswordInput = findViewById(R.id.editTextConfirmPassword);
        createAccountButton = findViewById(R.id.buttonCreateAccount);
        alreadyHaveAccountText = findViewById(R.id.textViewAlreadyHaveAccount);
        auth = FirebaseAuth.getInstance();

        alreadyHaveAccountText.setOnClickListener(v ->{
            startActivity(new Intent(SignUpActivity.this , MainActivity.class));
            finish();});
    }

    /**
     * Create account.
     *
     * @param view the view
     */
    public void createAccount(View view) {
        String email = emailInput.getText().toString().trim();
        String userName = userNameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // בדיקה אם הסיסמאות תואמות
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            passwordInput.setText("");
            confirmPasswordInput.setText("");
            return;
        }
        if (userName.length() < 3 || userName.length() > 20) {
            userNameInput.setError("User name must be between 3 and 20 characters");
            userNameInput.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && password.length() >= 6 && !TextUtils.isEmpty(userName)) {

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    realTimeSave(userName);
                    Toast.makeText(SignUpActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                } else {
                    Exception exception = task.getException();
                    String errorMessage = (exception != null) ? exception.getMessage() : "Unknown error occurred";
                    Toast.makeText(SignUpActivity.this, "Error " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(SignUpActivity.this, "Invalid email or password (less than 6 digits)", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Real time save.
     *
     * @param userName the user name
     */
    public void realTimeSave(String userName){
        FirebaseUser firebaseUser = auth.getCurrentUser();
        User user = new User(userName, firebaseUser.getUid(),firebaseUser.getEmail(),false);
        DatabaseReference ref = database.getReference("Root");
        ref.child("Users").child(firebaseUser.getUid()).setValue(user);
    }
}