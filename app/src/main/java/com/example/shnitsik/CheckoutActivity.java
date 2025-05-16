package com.example.shnitsik;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity {
    private TextView dateTimeTextView;
    private List<String> allowedDays = new ArrayList<>();
    private Button selectTimeButton, confirmOrderButton;
    private RadioGroup paymentMethodGroup;
    private CartManager cartManager;
    private Calendar selectedTime;
    private int openingHour = 8;
    private int closingHour = 16;

    private PaymentSheet paymentSheet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        confirmOrderButton = findViewById(R.id.confirmOrderButton);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        cartManager = SharedCart.getInstance().getCartManager();
        selectedTime = Calendar.getInstance();

        PaymentConfiguration.init(getApplicationContext(), "pk_test_51RP3MuBDGmywb1ECke5sT7Upe1dsA7Q01y8pIhBLn0Ovgj4YoTXviTpGLwys093313TdZ0BlAu8PN52dIUk8A9uD00dJBCut95"); // <-- החלף ב־Publishable Key שלך


        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        fetchOpeningHours();

        selectTimeButton.setOnClickListener(v -> openDateTimePicker());
        confirmOrderButton.setOnClickListener(v -> confirmOrder());
    }

    private void fetchOpeningHours() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Root/openingHours");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("open")) {
                    openingHour = snapshot.child("open").getValue(Integer.class);
                }
                if (snapshot.hasChild("close")) {
                    closingHour = snapshot.child("close").getValue(Integer.class);
                }
                if (snapshot.hasChild("days")) {
                    for (DataSnapshot daySnap : snapshot.child("days").getChildren()) {
                        allowedDays.add(daySnap.getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CheckoutActivity.this, "Failed to fetch settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDateTimePicker() {
        Calendar now = Calendar.getInstance();
        Calendar maxTime = (Calendar) now.clone();
        maxTime.add(Calendar.HOUR_OF_DAY, 24);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedTime.set(Calendar.YEAR, year);
            selectedTime.set(Calendar.MONTH, month);
            selectedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
            String selectedDay = dayFormat.format(selectedTime.getTime());

            if (!allowedDays.contains(selectedDay)) {
                Toast.makeText(this, "Cafeteria is closed on " + selectedDay, Toast.LENGTH_LONG).show();
                return;
            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                if (hourOfDay < openingHour || hourOfDay >= closingHour) {
                    Toast.makeText(this, "Only available between " + openingHour + ":00 and " + closingHour + ":00", Toast.LENGTH_LONG).show();
                    return;
                }

                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTime.set(Calendar.SECOND, 0);
                selectedTime.set(Calendar.MILLISECOND, 0);

                if (selectedTime.after(maxTime)) {
                    Toast.makeText(this, "Orders can be scheduled only up to 24 hours in advance", Toast.LENGTH_LONG).show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                dateTimeTextView.setText("Selected Time: " + sdf.format(selectedTime.getTime()));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            timePickerDialog.show();

        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void confirmOrder() {
        if (selectedTime == null || selectedTime.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedMethodId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedMethodId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedMethodButton = findViewById(selectedMethodId);
        String paymentMethod = selectedMethodButton.getText().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Product> cartProducts = new ArrayList<>(cartManager.getCart());

        if (paymentMethod.equalsIgnoreCase("Credit Card")) {
            int amount = (int) (cartManager.getTotalCartPrice() * 100); // cents
            createPaymentIntent(amount, cartProducts, user);
        } else {
            saveOrder(cartProducts, user);
        }
    }

    private void createPaymentIntent(int amount, List<Product> products, FirebaseUser user) {
        String backendUrl = "https://stripe-server.onrender.com";

        OkHttpClient client = new OkHttpClient();
        JSONObject body = new JSONObject();
        try {
            body.put("amount", amount);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        Request request = new Request.Builder()
                .url(backendUrl + "/create-payment-intent")
                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Failed to create payment intent", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Server error", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    String clientSecret = json.getString("clientSecret");
                    runOnUiThread(() -> startStripePaymentFlow(clientSecret, products, user));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startStripePaymentFlow(String clientSecret, List<Product> products, FirebaseUser user) {
        paymentSheet.presentWithPaymentIntent(
                clientSecret,
                new PaymentSheet.Configuration("Cafeteria App")
        );

        // נשמור אותם זמנית עבור שימוש ב־onPaymentSheetResult
        this.tempProducts = products;
        this.tempUser = user;
    }

    private List<Product> tempProducts;
    private FirebaseUser tempUser;

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            saveOrder(tempProducts, tempUser);
        } else {
            Toast.makeText(this, "Payment cancelled or failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrder(List<Product> cartProducts, FirebaseUser user) {
        Order order = new Order("OID" + System.currentTimeMillis(), new Date(), user.getUid(), cartProducts.size());
        order.setRequestedTime(selectedTime.getTimeInMillis());
        order.setProducts(cartProducts.toArray(new Product[0]));
        order.requiresFreshness();
        order.calculatePrepTime();
        order.calculateIdealPreparationTime();

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Root").child("Orders");
        ordersRef.child(order.getoId()).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                    SharedCart.getInstance().getCartManager().getCart().clear();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error placing order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
