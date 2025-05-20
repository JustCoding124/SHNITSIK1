package com.example.shnitsik;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shnitsik.models.CartManager;
import com.example.shnitsik.models.MyAdapter;
import com.example.shnitsik.models.MyMenuAdapter;
import com.example.shnitsik.models.Order;
import com.example.shnitsik.models.Product;
import com.example.shnitsik.models.SharedCart;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView topProduct;
    private TextView orderLoadTextView;
    private MyAdapter adapter;
    private LinearLayout adminSection;
    private LinearLayout userSection;
    private MyMenuAdapter menueAdapter;
    private FirebaseUser currentUser;
    private List<Order> orderList;
    private List<Product> menueList;
    private CartManager cartManager;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        this.adminSection = rootView.findViewById(R.id.adminSection);
        this.userSection = rootView.findViewById(R.id.userSection);
        this.userSection.setVisibility(View.GONE);
        this.adminSection.setVisibility(View.GONE);
        this.orderList = new ArrayList<>();
        this.recyclerView = rootView.findViewById(R.id.recyclerView);
        this.adapter = new MyAdapter(getContext(), this.orderList);
        recyclerView.setAdapter(adapter);
        this.adapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        this.menueList = new ArrayList<>();
        this.menueAdapter = new MyMenuAdapter(getContext(), menueList);
        this.recyclerView = rootView.findViewById(R.id.menuRecyclerView);
        recyclerView.setAdapter(menueAdapter);
        this.menueAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = null;
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        DatabaseReference roleRef = database.getReference("Root").child("Users").child(userId).child("role");
        roleRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Boolean role = task.getResult().getValue(Boolean.class);
                if (role != null && role) {
                    adminOnCreate(rootView);
                } else {
                    String userIdlambda = currentUser.getUid();
                    userOnCreate(rootView, userIdlambda);
                }
            } else {
                Toast.makeText(getActivity(), "No clear user access permissions", Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

    public void adminOnCreate(View rootView) {
        this.userSection.setVisibility(View.GONE);
        this.adminSection.setVisibility(View.VISIBLE);

        recyclerViewInitialize();

        this.orderLoadTextView = rootView.findViewById(R.id.rushHourTextView);
        this.topProduct = rootView.findViewById(R.id.topProductTextView);
    }

    public void recyclerViewInitialize() {
        long todayStartMillis = getStartOfTodayMillis();
        DatabaseReference ordersRef = database.getReference("Root/Orders");
        ordersRef.orderByKey().startAt(String.valueOf(todayStartMillis)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HomeFragment.this.orderList.clear();
                for (DataSnapshot timeSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot orderSnapshot : timeSnapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null && isToday(order.getRequestedTime())) {
                            HomeFragment.this.orderList.add(order);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                updateOrderLoadText(HomeFragment.this.orderList);
                updateTopProduct(HomeFragment.this.orderList);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error Connecting To DB:" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    public long getStartOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    public String getTodayDate() {
        Calendar calendar = Calendar.getInstance();  // מקבל את התאריך והשעה הנוכחיים
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  // קובע את פורמט התאריך
        return dateFormat.format(calendar.getTime());  // מחזיר את התאריך בפורמט הרצוי
    }
    public void updateOrderLoadText(List<Order> orders) {
        if (orders.isEmpty()) {
            this.orderLoadTextView.setText("No Orders Yet");
        } else {
            //חישוב השעת עומס
            String peakHour = calculatePeakOrderHour(orders);
            this.orderLoadTextView.setText("Today's Peak Hour: " + peakHour);
        }
    }
    public void updateTopProduct(List<Order> orders) {
        if (orders.isEmpty()) {
            this.orderLoadTextView.setText("No Orders Currently");
        } else {
            String topProduct1 = findTopProduct(calculateProductFrequency(orders));
            this.topProduct.setText("Today's Top Selling Product: " + topProduct1);
        }
    }
    public String calculatePeakOrderHour(List<Order> orders) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");  // פורמט שעה (שעה ודקה)
        Map<String, Integer> hourCounts = new HashMap<>();
        for (Order order : orders) {
            String orderHour = dateFormat.format(order.getRequestedTime()).substring(0, 2);
            if (hourCounts.containsKey(orderHour)) {
                hourCounts.put(orderHour, hourCounts.get(orderHour) + 1);
            } else {
                hourCounts.put(orderHour, 1);
            }

        }
        // מציאת השעה עם הכי הרבה הזמנות
        String peakHour = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : hourCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                peakHour = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return peakHour != null ? peakHour + ":00" : "Could Not Calculate";
    }
    public boolean isToday(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));

        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }
    public Map<String, Integer> calculateProductFrequency(@NonNull List<Order> orders) {
        Map<String, Integer> productFrequency = new HashMap<>();

        for (Order order : orders) {
            if (!isToday(order.getDateOfOrder())) continue;

            for (Product product : order.getProducts()) {
                String productName = product.getProductName();
                int currentCount = productFrequency.containsKey(productName) ? productFrequency.get(productName) : 0;
                productFrequency.put(productName, currentCount + 1);
            }
        }

        return productFrequency;
    }

    public String findTopProduct(Map<String, Integer> productFrequency) {
        String topProduct = null;
        int maxFrequency = 0;

        for (Map.Entry<String, Integer> entry : productFrequency.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                topProduct = entry.getKey();
                maxFrequency = entry.getValue();
            }
        }

        return topProduct + " Purchased " + maxFrequency + " times today";
    }
    // עבור משתמש רגיל(לא אדמין)
    public void userOnCreate(View rootView, String userIdlambda){
        userGreeting(rootView, userIdlambda);
        this.adminSection.setVisibility(View.GONE);
        this.userSection.setVisibility(View.VISIBLE);

        // אתחול ה-adapter עם הנתונים שהתקבלו
        recyclerViewInitializeForUser();
        EditText searchEditText = rootView.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                menueAdapter.filterListByName(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        ImageView cartImageView = rootView.findViewById(R.id.cartImageView);
        ImageView profilePictureImageView = rootView.findViewById(R.id.profilePictureImageView);
        Button checkoutButton = rootView.findViewById(R.id.checkoutButton);
        cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartDialog();
            }
        });
        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileChange();
            }
        });
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payment();
            }
        });
    }
    public void recyclerViewInitializeForUser() {
        FirebaseFirestore.getInstance()
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    menueList.clear();
                    Map<String, List<Product>> categorized = new HashMap<>();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Product product = snapshot.toObject(Product.class);
                        if (!categorized.containsKey(product.getCategory())) {
                            categorized.put(product.getCategory(), new ArrayList<>());
                        }
                        categorized.get(product.getCategory()).add(product);
                    }

                    // הכנס כותרת קטגוריה ואחריה את המוצרים שלה
                    for (String category : categorized.keySet()) {
                        menueList.add(new Product(category, true)); // כותרת
                        menueList.addAll(categorized.get(category)); // מוצרים
                    }

                    menueAdapter.setOriginalList(menueList);
                    menueAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Error loading menu: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );

    }
    public void userGreeting(View rootView, String userIdlambda){
        TextView greetingTextView = rootView.findViewById(R.id.greetingTextView);
        database.getReference("Root").child("Users").child(userIdlambda)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String username = snapshot.child("userName").getValue(String.class);
                        if (username != null) {
                            greetingTextView.setText("Hello, " + username);
                        } else {
                            greetingTextView.setText("Hello, user");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getActivity(), "ERROR LOADING USERNAME " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void profileChange(){
        // יצירת Layout ראשי (LinearLayout)
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // יצירת EditText לשם משתמש חדש
        final EditText usernameInput = new EditText(getContext());
        usernameInput.setHint("New Username");
        layout.addView(usernameInput);

        // יצירת EditText לאימייל חדש
        final EditText emailInput = new EditText(getContext());
        emailInput.setHint("New Email Address");
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(emailInput);

        //  EditText לססמא
        final EditText passwordInput = new EditText(getContext());
        passwordInput.setHint("Current Password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);


        // יצירת CheckBox לעדכון אימייל
        final CheckBox checkBoxEmail = new CheckBox(getContext());
        checkBoxEmail.setText("Update Email Address");
        layout.addView(checkBoxEmail);

        // יצירת CheckBox לעדכון שם משתמש
        final CheckBox checkBoxUsername = new CheckBox(getContext());
        checkBoxUsername.setText("Update Username");
        layout.addView(checkBoxUsername);

        // כפתור לעדכון פרופיל
        Button updateButton = new Button(getContext());
        updateButton.setText("Update Profile Info");
        layout.addView(updateButton);

        // כפתור לשחזור ססמא
        Button resetPasswordButton = new Button(getContext());
        resetPasswordButton.setText("Replace Password");
        layout.addView(resetPasswordButton);

        // יצירת AlertDialog עם ה-Layout המוגדר
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(layout).setCancelable(true);

        // מאזין ל-CheckBox של עדכון אימייל
        checkBoxEmail.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                emailInput.setVisibility(View.VISIBLE);
            } else {
                emailInput.setVisibility(View.GONE);
            }
        });

        // מאזין ל-CheckBox של עדכון שם משתמש
        checkBoxUsername.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                usernameInput.setVisibility(View.VISIBLE);
            } else {
                usernameInput.setVisibility(View.GONE);
            }
        });

        // מאזין לכפתור עדכון פרופיל
        updateButton.setOnClickListener(v -> {
            if (checkBoxEmail.isChecked()) {
                String email = emailInput.getText().toString();
                if (!email.isEmpty()) {
                    updateEmail(email, passwordInput.getText().toString());
                }
            }
            if (checkBoxUsername.isChecked()) {
                String username = usernameInput.getText().toString();
                if (!username.isEmpty()) {
                    updateUsername(username);
                }
            }
        });

        // מאזין לכפתור לשחזור ססמא
        resetPasswordButton.setOnClickListener(v -> {
            String email = currentUser.getEmail();
            if (email != null) {
                sendPasswordResetEmail(email);
            }
        });

        // הצגת דיאלוג
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void updateEmail(String newEmail, String currentPassword) {
        if (this.currentUser != null) {
            // אם המשתמש מחובר, נבצע אוטנטיקציה מחדש עם הסיסמה הנוכחית
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

            // אתחול מחדש של האותנטיקציה
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // אם האותנטיקציה הצליחה, נעדכן את האימייל
                    currentUser.updateEmail(newEmail)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getContext(), "Email Updated Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error Updating Email", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // אם האותנטיקציה נכשלה
                    Toast.makeText(getContext(), "Given Password Incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    // פונקציה לעדכון שם משתמש
    private void updateUsername(String newUsername ) {
        if (this.currentUser != null) {
            // אם המשתמש מחובר, נעדכן את שם המשתמש ב-Firebase Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Root").child("Users")
                    .child(currentUser.getUid());
            userRef.child("userName").setValue(newUsername)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Username Updated Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error Updating Username", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    // פונקציה לשחזור ססמא
    private void sendPasswordResetEmail(String email) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password Replacement Link Has Been Sent Via Email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error Sending Link Via Email", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void cartDialog() {
        startActivity(new Intent(getContext(), CartActivity.class));
    }
    public void payment() {
        CartManager cart = SharedCart.getInstance().getCartManager();
        if (cart.getCart().isEmpty()) {
            Toast.makeText(getContext(), "The cart is empty. Please add items before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(getContext(), CheckoutActivity.class));
    }





}
