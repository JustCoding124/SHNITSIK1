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

/**
 * The type Home fragment.
 */
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

    /**
     * Admin on create.
     *
     * @param rootView the root view
     */
    public void adminOnCreate(View rootView) {
        this.userSection.setVisibility(View.GONE);
        this.adminSection.setVisibility(View.VISIBLE);
        Button logoutButton = rootView.findViewById(R.id.adminLogoutButton);
        logoutButton.setOnClickListener(v -> {
            // מנקה את כל ההעדפות האוטומטיות כדי למנוע התחברות מחדש
            requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE)
                    .edit().clear().apply();

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), MainActivity.class));
            requireActivity().finish();
        });



        recyclerViewInitialize();

        this.orderLoadTextView = rootView.findViewById(R.id.rushHourTextView);
        this.topProduct = rootView.findViewById(R.id.topProductTextView);
    }

    /**
     * Recycler view initialize.
     */
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

    /**
     * Gets start of today millis.
     *
     * @return the start of today millis
     */
    public long getStartOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Gets today date.
     *
     * @return the today date
     */
    public String getTodayDate() {
        Calendar calendar = Calendar.getInstance();  // מקבל את התאריך והשעה הנוכחיים
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  // קובע את פורמט התאריך
        return dateFormat.format(calendar.getTime());  // מחזיר את התאריך בפורמט הרצוי
    }

    /**
     * Update order load text.
     *
     * @param orders the orders
     */
    public void updateOrderLoadText(List<Order> orders) {
        if (orders.isEmpty()) {
            this.orderLoadTextView.setText("No Orders Yet");
        } else {
            //חישוב השעת עומס
            String peakHour = calculatePeakOrderHour(orders);
            this.orderLoadTextView.setText("Today's Peak Hour: " + peakHour);
        }
    }

    /**
     * Update top product.
     *
     * @param orders the orders
     */
    public void updateTopProduct(List<Order> orders) {
        if (orders.isEmpty()) {
            this.orderLoadTextView.setText("No Orders Currently");
        } else {
            String topProduct1 = findTopProduct(calculateProductFrequency(orders));
            this.topProduct.setText("Today's Top Selling Product: " + topProduct1);
        }
    }

    /**
     * Calculate peak order hour string.
     *
     * @param orders the orders
     * @return the string
     */
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

    /**
     * Is today boolean.
     *
     * @param date the date
     * @return the boolean
     */
    public boolean isToday(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));

        Calendar today = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Calculate product frequency map.
     *
     * @param orders the orders
     * @return the map
     */
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

    /**
     * Find top product string.
     *
     * @param productFrequency the product frequency
     * @return the string
     */
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

    /**
     * User on create.
     *
     * @param rootView     the root view
     * @param userIdlambda the user idlambda
     */
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

    /**
     * Recycler view initialize for user.
     */
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

    /**
     * User greeting.
     *
     * @param rootView     the root view
     * @param userIdlambda the user idlambda
     */
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

    /**
     * Profile change.
     */
    public void profileChange() {
        // יצירת Layout ראשי (LinearLayout)
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // יצירת EditText לשם משתמש חדש
        final EditText usernameInput = new EditText(getContext());
        usernameInput.setHint("New Username");
        usernameInput.setVisibility(View.VISIBLE);
        layout.addView(usernameInput);

        // כפתור לעדכון פרופיל
        Button updateButton = new Button(getContext());
        updateButton.setText("Update Profile Info");
        layout.addView(updateButton);

        // כפתור לשחזור ססמא
        Button resetPasswordButton = new Button(getContext());
        resetPasswordButton.setText("Replace Password");
        layout.addView(resetPasswordButton);

        // כפתור התנתקות
        Button logoutButton = new Button(getContext());
        logoutButton.setText("LOG OUT");
        layout.addView(logoutButton);

        // יצירת AlertDialog עם ה-Layout המוגדר
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(layout).setCancelable(true);

        // מאזין לכפתור עדכון פרופיל
        updateButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                usernameInput.setError("Please enter a new username");
            } else {
                updateUsername(username);
            }
        });



        // מאזין לכפתור לשחזור ססמא
        resetPasswordButton.setOnClickListener(v -> {
            String email = currentUser.getEmail();
            if (email != null) {
                sendPasswordResetEmail(email);
            }
        });

        // מאזין לכפתור התנתקות
        logoutButton.setOnClickListener(v -> {
            // נקה את ההעדפות כדי למנוע התחברות אוטומטית
            requireContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE)
                    .edit().clear().apply();

            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), MainActivity.class));
            requireActivity().finish();
        });


        // הצגת דיאלוג
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    // פונקציה לעדכון שם משתמש
    private void updateUsername(String newUsername) {
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("Root/Users")
                    .child(currentUser.getUid())
                    .child("userName")
                    .setValue(newUsername)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update username", Toast.LENGTH_SHORT).show();
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

    /**
     * Cart dialog.
     */
    public void cartDialog() {
        startActivity(new Intent(getContext(), CartActivity.class));
    }

    /**
     * Payment.
     */
    public void payment() {
        CartManager cart = SharedCart.getInstance().getCartManager();
        if (cart.getCart().isEmpty()) {
            Toast.makeText(getContext(), "The cart is empty. Please add items before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(getContext(), CheckoutActivity.class));
    }





}
