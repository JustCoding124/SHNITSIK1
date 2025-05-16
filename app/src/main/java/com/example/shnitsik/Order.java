package com.example.shnitsik;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Order {
    private long openingTime;
    private long prepTime;
    private String oId;
    private long idealPrepTime;
    private long dateOfOrder;
    private String ordererUID;
    private boolean isFinished = false;
    private Product[] products;
    private boolean requiresFreshness;
    private long requestedTime;
    private String productsString;
    public Order(String oId,Date dateOfOrder,String ordererUID,int productsLen) {
        this.dateOfOrder = dateOfOrder.getTime();
        this.ordererUID = ordererUID;
        this.oId = oId;
        this.products = new Product[productsLen];
        this.isFinished =false;
        calculatePrepTime();
        //calculateIdealPreparationTime();

    }
    public long getRequestedTime(){return requestedTime;}
    public void setIdealPrepTime(long idealPrepTime){this.idealPrepTime =idealPrepTime; }
    public long getDateOfOrder() {
        return dateOfOrder;
    }

    public void setRequestedTime(long timeInMillis){
        this.requestedTime = timeInMillis;
    }
    public void setDateOfOrder(long dateOfOrder) {
        this.dateOfOrder = dateOfOrder;
    }

    public String getoId() {
        return oId;
    }

    public void setoId(String oId) {
        this.oId = oId;
    }

    public String getOrdererUID() {
        return ordererUID;
    }

    public void setOrdererUID(String ordererUID) {
        this.ordererUID = ordererUID;
    }

    public Product[] getProducts() {
        return products;
    }

    public void setProducts(Product[] products) {
        System.arraycopy(products, 0, this.products, 0, products.length);
        for (Product product: this.products) {
            this.productsString += String.format(", %s",product.getProductName());
        }

    }
    public void setFinished(boolean isFinished){
        this.isFinished=isFinished;
    }
    public boolean getIsFinished(){
        return this.isFinished;
    }
    public String getProductsString(){
        return this.productsString;
    }
    public double getTotalOrderPrice(){
        double sum=0;
        for (Product p:this.products) {
            sum+=p.getTotalProductPrice();
        }
        return sum;
    }
    public int getProductAmount(Product p){
        int amount =0;
        for (Product product:this.products) {
            if (product == p )
                amount += 1;
        }
        return amount;
    }
    public void requiresFreshness(){
        for (Product p:this.products) {
            if (p.requiresFreshness()) {
                this.requiresFreshness = true;
                return;
            }
        }
        this.requiresFreshness = false;
    }
    public long getIdealPrepTime(){
        this.calculateIdealPreparationTime();
        return this.idealPrepTime;
    }
    public void calculateIdealPreparationTime() {
        if (this.requiresFreshness) {
            // אם דורש טריות, נכין קרוב לזמן האיסוף
            this.idealPrepTime = requestedTime - this.prepTime;
        } else {
            // אחרת, נכניס אותו לפי עומס קיים
            this.idealPrepTime = findBestTimeSlot();
        }
    }
    public void calculatePrepTime(){
        for (Product p: this.products) {
            this.prepTime += p.getPrepTime();
        }
    }
    public long findBestTimeSlot(){
        long bestTime=0;
        List<Order> orders = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        Calendar requested = Calendar.getInstance();
        requested.setTimeInMillis(this.requestedTime);

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Root").child("orders");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null) {
                        orders.add(order);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving orders", databaseError.toException());
            }
        });
        FirebaseDatabase.getInstance().getReference("Root").child("openingTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // קבלת זמן הפתיחה מ-Firebase (נניח שמורים שם כ long)
                Order.this.openingTime = dataSnapshot.getValue(Long.class);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long millis = calendar.getTimeInMillis();
                Order.this.openingTime += millis;

            }
            public void onCancelled(DatabaseError error) {
                System.out.println("שגיאה בשליפה מ-Firebase: " + error.getMessage());
            }
        });
        boolean isToday = now.get(Calendar.YEAR) == requested.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == requested.get(Calendar.DAY_OF_YEAR);
        if(isToday)
            bestTime = System.currentTimeMillis();
        else{
            bestTime = this.openingTime;
        }
        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return Long.compare(o1.getIdealPrepTime(), o2.getIdealPrepTime());
            }
        });
        for (Order order : orders) {
            long orderStart = order.getIdealPrepTime();
            long orderEnd = orderStart + order.getRequestedTime(); // סוף הזמן של ההזמנה

            // אם יש רווח בין הזמנה קודמת לנוכחית – אפשר לשבץ כאן
            if (bestTime + prepTime <= orderStart) {
                return bestTime; // מחזיר את הזמן שבו אפשר להתחיל להכין
            }

            // אם אין רווח, קופצים קדימה לסוף ההזמנה הנוכחית
            bestTime = orderEnd;
        }
        return bestTime;
    }
}

