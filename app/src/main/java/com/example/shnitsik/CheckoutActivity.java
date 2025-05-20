package com.example.shnitsik;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shnitsik.models.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import okhttp3.*;

public class CheckoutActivity extends AppCompatActivity {
    private TextView dateTimeTextView;
    private List<String> allowedDays = new ArrayList<>();
    private Button selectTimeButton, confirmOrderButton;
    private RadioGroup paymentMethodGroup;
    private CartManager cartManager;
    private Calendar selectedTime;
    private int openHour = 8, openMinute = 0, closeHour = 16, closeMinute = 0;
    private PaymentSheet paymentSheet;
    private List<Product> tempProducts;
    private FirebaseUser tempUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent); // פותח למסך הרשאות
                return;
            }
        }

        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        confirmOrderButton = findViewById(R.id.confirmOrderButton);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        cartManager = SharedCart.getInstance().getCartManager();
        selectedTime = Calendar.getInstance();

        PaymentConfiguration.init(getApplicationContext(), "pk_test_...");
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
                String today = getTodayDayName();
                for (DataSnapshot daySnap : snapshot.getChildren()) {
                    String day = daySnap.getKey();
                    boolean closed = daySnap.child("closed").getValue(Boolean.class) != null &&
                            daySnap.child("closed").getValue(Boolean.class);
                    if (!closed) allowedDays.add(day);
                    if (day.equals(today)) {
                        Integer oh = daySnap.child("openHour").getValue(Integer.class);
                        Integer om = daySnap.child("openMinute").getValue(Integer.class);
                        Integer ch = daySnap.child("closeHour").getValue(Integer.class);
                        Integer cm = daySnap.child("closeMinute").getValue(Integer.class);
                        if (oh != null) openHour = oh;
                        if (om != null) openMinute = om;
                        if (ch != null) closeHour = ch;
                        if (cm != null) closeMinute = cm;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CheckoutActivity.this, "Failed to fetch hours", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDateTimePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedTime.set(Calendar.YEAR, year);
            selectedTime.set(Calendar.MONTH, month);
            selectedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String selectedDayName = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(selectedTime.getTime());
            if (!allowedDays.contains(selectedDayName)) {
                Toast.makeText(this, "Cafeteria is closed on " + selectedDayName, Toast.LENGTH_LONG).show();
                return;
            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, hour, minute) -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTime.set(Calendar.SECOND, 0);
                selectedTime.set(Calendar.MILLISECOND, 0);

                Calendar openTime = (Calendar) selectedTime.clone();
                openTime.set(Calendar.HOUR_OF_DAY, openHour);
                openTime.set(Calendar.MINUTE, openMinute);

                Calendar closeTime = (Calendar) selectedTime.clone();
                closeTime.set(Calendar.HOUR_OF_DAY, closeHour);
                closeTime.set(Calendar.MINUTE, closeMinute);

                if (selectedTime.getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(this, "Selected time has already passed", Toast.LENGTH_LONG).show();
                } else if (selectedTime.before(openTime) || selectedTime.after(closeTime)) {
                    Toast.makeText(this, "Allowed between " + formatTime(openHour, openMinute)
                            + " and " + formatTime(closeHour, closeMinute), Toast.LENGTH_LONG).show();
                } else {
                    String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(selectedTime.getTime());
                    dateTimeTextView.setText("Selected Time: " + formatted);
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private String formatTime(int h, int m) {
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }

    private String getTodayDayName() {
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().getTime());
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
            int amount = (int) (cartManager.getTotalCartPrice() * 100);
            createPaymentIntent(amount, cartProducts, user);
        } else {
            saveOrder(cartProducts, user);
        }
    }

    private void saveOrder(List<Product> cartProducts, FirebaseUser user) {
        String uniqueOrderId = FirebaseDatabase.getInstance()
                .getReference("Root/OrdersSortedByIdealTime")
                .push().getKey();

        if (uniqueOrderId == null) {
            Toast.makeText(this, "Failed to generate unique order ID", Toast.LENGTH_LONG).show();
            return;
        }

        Order order = new Order(uniqueOrderId, new Date(), user.getUid(), cartProducts.size());
        order.setRequestedTime(selectedTime.getTimeInMillis());
        order.setProducts(cartProducts);
        order.requiresFreshness();
        order.calculatePrepTime();

        FirebaseDatabase.getInstance().getReference("Root/OrdersSortedByIdealTime")
                .orderByChild("requestedTime")
                .startAt(getDayStart(order.getRequestedTime()))
                .endAt(getDayEnd(order.getRequestedTime()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long latestEndTime = getDayStart(order.getRequestedTime());

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Order existingOrder = orderSnap.getValue(Order.class);
                            if (existingOrder != null) {
                                long end = existingOrder.getIdealPrepTime() + existingOrder.getPrepTime();
                                if (end > latestEndTime) {
                                    latestEndTime = end;
                                }
                            }
                        }

                        order.setIdealPrepTime(Math.max(latestEndTime, System.currentTimeMillis()));

                        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference("Root");
                        baseRef.child("Orders")
                                .child(String.valueOf(order.getIdealPrepTime()))
                                .child(order.getoId())
                                .setValue(order)
                                .addOnSuccessListener(aVoid -> {
                                    SharedCart.getInstance().getCartManager().getCart().clear();
                                    scheduleIdealTimeNotification(order);
                                    Toast.makeText(CheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CheckoutActivity.this, "Error placing order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CheckoutActivity.this, "Error fetching orders: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private long getDayStart(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getDayEnd(long timeInMillis) {
        return getDayStart(timeInMillis) + 24 * 60 * 60 * 1000;
    }

    private void createPaymentIntent(int amount, List<Product> products, FirebaseUser user) {
        String backendUrl = "http://10.100.102.186:4242";
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CheckoutActivity.this, "Failed to create payment intent", Toast.LENGTH_SHORT).show());
            }

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
        this.tempProducts = products;
        this.tempUser = user;
        paymentSheet.presentWithPaymentIntent(clientSecret, new PaymentSheet.Configuration("Cafeteria App"));
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            saveOrder(tempProducts, tempUser);
        } else {
            Toast.makeText(this, "Payment cancelled or failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleIdealTimeNotification(Order order) {
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        intent.setPackage(getPackageName()); // חובה כדי למנוע SecurityException
        intent.putExtra("orderId", order.getoId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                order.getoId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // לשם בדיקה מיידית (5 שניות):
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, order.getIdealPrepTime(), pendingIntent);
    }

}
