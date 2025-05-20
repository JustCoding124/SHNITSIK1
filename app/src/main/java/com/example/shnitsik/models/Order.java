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
import java.util.concurrent.CountDownLatch;

public class Order {
    private long prepTime;
    private String oId;
    private long idealPrepTime;
    private long dateOfOrder;
    private String ordererUID;
    private boolean isFinished = false;
    private Product[] products;
    private boolean requiresFreshness;
    private long requestedTime;
    private String productsString = "";

    public Order(String oId, Date dateOfOrder, String ordererUID, int productsLen) {
        this.dateOfOrder = dateOfOrder.getTime();
        this.ordererUID = ordererUID;
        this.oId = oId;
        this.products = new Product[productsLen];
        this.isFinished = false;
        calculatePrepTime();
    }

    public long getRequestedTime() {
        return requestedTime;
    }

    public void setIdealPrepTime(long idealPrepTime) {
        this.idealPrepTime = idealPrepTime;
    }

    public long getDateOfOrder() {
        return dateOfOrder;
    }

    public void setRequestedTime(long timeInMillis) {
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
            if (product == p)
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
        calculateIdealPreparationTime();
        return this.idealPrepTime;
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


    public void calculateIdealPreparationTime() {
        if (this.requiresFreshness) {
            this.idealPrepTime = requestedTime - this.prepTime;
        } else {
            this.idealPrepTime = findBestTimeSlotSync();
        }
    }

    public long findBestTimeSlotSync() {
        long bestTime;
        List<Order> orders = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Root/OrdersSortedByIdealTime");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot bucket : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot : bucket.getChildren()) {
                        Order order = snapshot.getValue(Order.class);
                        if (order != null) {
                            orders.add(order);
                        }
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving orders", databaseError.toException());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Calendar now = Calendar.getInstance();
        Calendar requested = Calendar.getInstance();
        requested.setTimeInMillis(this.requestedTime);

        boolean isToday = now.get(Calendar.YEAR) == requested.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == requested.get(Calendar.DAY_OF_YEAR);

        if (isToday) {
            bestTime = System.currentTimeMillis();
        } else {
            requested.set(Calendar.HOUR_OF_DAY, 8);
            requested.set(Calendar.MINUTE, 0);
            requested.set(Calendar.SECOND, 0);
            requested.set(Calendar.MILLISECOND, 0);
            bestTime = requested.getTimeInMillis();
        }

        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return Long.compare(o1.getIdealPrepTime(), o2.getIdealPrepTime());
            }
        });

        for (Order order : orders) {
            long orderStart = order.getIdealPrepTime();
            long orderEnd = orderStart + order.prepTime;

            if (bestTime + this.prepTime <= orderStart) {
                return bestTime;
            }
            bestTime = Math.max(bestTime, orderEnd);
        }

        return bestTime;
    }
}
