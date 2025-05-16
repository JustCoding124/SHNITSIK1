package com.example.shnitsik;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.SharedPreferences;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView signUpTextView, forgotPasswordTextView; // Updated to TextView
    private CheckBox rememberMeCheckBox;
    private ProgressBar progressBar;
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);//אתחול Firebase הוא השלב שבו האפליקציה מתחברת למשאבים בפרויקט Firebase.
        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonSignIn);
        signUpTextView = findViewById(R.id.SignUp);
        forgotPasswordTextView = findViewById(R.id.ForgotPassword);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        progressBar = findViewById(R.id.progress_bar);

        fbAuth = FirebaseAuth.getInstance();
        // בבניית מאזינים (Listeners) כמו onClick, לעיתים משתמשים בלמדא (Lambda) במקום להגדיר את המתודה באופן מלא.
        // הלמדא היא פונקציה אנונימית ותמציתית, שמקבלת את ערך ה-View (כמו כפתור שנלחץ) כפרמטר (v),
        // ומבצעת את הפעולה המבוקשת (כמו קריאה לפונקציה loginUser()).
        // הלמדא מקלה על הקריאה ומפשטת את הקוד, ומאפשרת לכתוב פונקציות בקצרה וללא הצורך בהגדרת מחלקות נפרדות.

        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordTextView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class)));
        signUpTextView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SignUpActivity.class)));

        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        //MODE_PRIVATE: מציין שהגישה לקובץ תעשה רק מתוך האפליקציה שלנו, ואי אפשר לקרוא או לשנות את המידע על ידי אפליקציות אחרות.
        //אם לא נמצא ערך, יחזור עם ערך ברירת המחדל false
        boolean isRemembered = preferences.getBoolean("isRemembered", false);
        String savedEmail = preferences.getString("email", "");
        // אם לא נמצא ערך, יחזור עם מחרוזת ריקה כערך ברירת מחדל
        if (isRemembered) {
            // Automatically login using the stored email (you can store the password if you want, but it's not recommended)
            String savedemail = preferences.getString("email", "");
            emailInput.setText(savedEmail);
            String savedpassword = preferences.getString("password", "");
            passwordInput.setText(savedpassword);

            // Call loginUser directly if the user is remembered
            loginUser();
        }
    }
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isRemembered = preferences.getBoolean("isRemembered", false);
        if (isRemembered){
            rememberMeCheckBox.setChecked(true);
        }
        //ה-return שבפונקציה לא מחזיר שום ערך (כי הפונקציה היא void), אלא פשוט מפסיק את הביצוע של הפונקציה ברגע שיש בעיה.
        //השימוש ב-TextUtils במקום בבדיקה רגילה של isEmpty() הוא כדי להימנע ממקרים שבהם הערך של ה-EditText הוא null.
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
                        if (user != null && user.isEmailVerified()){
                            Toast.makeText(MainActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
                            SharedPreferences preferences1 = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            if (rememberMeCheckBox.isChecked()) {
                                editor.putBoolean("isRemembered", true);
                                editor.putString("email", email);
                                editor.putString("password", password);

                            }
                            else {
                                editor.putBoolean("isRemembered", false);
                            }
                            editor.apply();  // שמור את המידע
                            // לנווט למסך הבית
                            Intent intent = new Intent(MainActivity.this, FragmentsCenterActivity.class);
                            startActivity(intent);
                            finish();}

                        else {
                            // Email is not verified
                            Toast.makeText(MainActivity.this, "Please Verify Your Email", Toast.LENGTH_LONG).show();
                            if (user!= null) {
                                user.sendEmailVerification().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "A verification email has been sent.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "We were unable to send the email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    }
                    else {
                        Exception exception = task.getException();
                        String errorMessage = (exception != null) ? exception.getMessage() : "Unknown error occurred";
                        Toast.makeText(MainActivity.this, "Error " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}