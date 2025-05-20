package com.example.shnitsik.models;

import android.util.Log;
import com.google.firebase.database.*;
import java.util.*;

public class Order {
    private long prepTime;
    private String oId;
    private long idealPrepTime;
    private long dateOfOrder;
    private String ordererUID;
    private boolean isFinished = false;
    private List<Product> products = new ArrayList<>();
    private boolean requiresFreshness;
    private long requestedTime;
    private String productsString = "";

    public Order() {
        // נדרש על ידי Firebase
    }

    public Order(String oId, Date dateOfOrder, String ordererUID, int productsLen) {
        this.dateOfOrder = dateOfOrder.getTime();
        this.ordererUID = ordererUID;
        this.oId = oId;
        this.isFinished = false;
        this.products = new ArrayList<>(productsLen); // מוכן לגודל משוער
        calculatePrepTime();
    }

    public long getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(long timeInMillis) {
        this.requestedTime = timeInMillis;
    }

    public long getDateOfOrder() {
        return dateOfOrder;
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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        this.productsString = "";
        for (Product product : this.products) {
            this.productsString += String.format(", %s", product.getProductName());
        }
    }

    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public boolean getIsFinished() {
        return this.isFinished;
    }

    public String getProductsString() {
        return this.productsString;
    }

    public double getTotalOrderPrice() {
        double sum = 0;
        for (Product p : this.products) {
            sum += p.getTotalProductPrice();
        }
        return sum;
    }

    public int getProductAmount(Product p) {
        int amount = 0;
        for (Product product : this.products) {
            if (product.equals(p)) // שים לב: השוואה לפי equals
                amount += 1;
        }
        return amount;
    }

    public void requiresFreshness() {
        for (Product p : this.products) {
            if (p.requiresFreshness()) {
                this.requiresFreshness = true;
                return;
            }
        }
        this.requiresFreshness = false;
    }

    public long getIdealPrepTime() {
        return this.idealPrepTime;
    }

    public long getPrepTime() {
        return this.prepTime;
    }

    public void setIdealPrepTime(long idealPrepTime) {
        this.idealPrepTime = idealPrepTime;
    }

    public void calculatePrepTime() {
        long total = 0;
        int count = 0;
        for (Product p : products) {
            if (p != null) {
                total += p.getPrepTime();
                count++;
            }
        }
        this.prepTime = (count > 0) ? total / count : 0;
    }

    public void calculateIdealPreparationTimeFirebaseBasedSync() {
        if (this.requiresFreshness) {
            this.idealPrepTime = this.requestedTime - this.prepTime;
            return;
        }

        Calendar requestedDay = Calendar.getInstance();
        requestedDay.setTimeInMillis(this.requestedTime);
        requestedDay.set(Calendar.HOUR_OF_DAY, 0);
        requestedDay.set(Calendar.MINUTE, 0);
        requestedDay.set(Calendar.SECOND, 0);
        requestedDay.set(Calendar.MILLISECOND, 0);

        long dayStart = requestedDay.getTimeInMillis();
        long dayEnd = dayStart + 24 * 60 * 60 * 1000;

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Root/Orders");

        ordersRef.orderByChild("requestedTime").startAt(dayStart).endAt(dayEnd)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        long latestEndTime = dayStart;

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Order existingOrder = orderSnap.getValue(Order.class);
                            if (existingOrder != null) {
                                long end = existingOrder.idealPrepTime + existingOrder.prepTime;
                                if (end > latestEndTime) {
                                    latestEndTime = end;
                                }
                            }
                        }

                        idealPrepTime = Math.max(latestEndTime, System.currentTimeMillis());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        idealPrepTime = System.currentTimeMillis();
                        Log.e("Order", "Failed to calculate idealPrepTime", error.toException());
                    }
                });
    }
}
