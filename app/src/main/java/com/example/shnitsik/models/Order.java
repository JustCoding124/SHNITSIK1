package com.example.shnitsik.models;

import android.util.Log;
import com.google.firebase.database.*;
import java.util.*;

/**
 * The type Order.
 */
public class Order {
    private long prepTime;
    private double totalToPay=0;
    private String oId;
    private long idealPrepTime;
    private long dateOfOrder;
    private String ordererUID;
    private boolean isFinished = false;
    private List<Product> products = new ArrayList<>();
    private boolean requiresFreshness;
    private long requestedTime;
    private String productsString = "";

    /**
     * Instantiates a new Order.
     */
    public Order() {
        // נדרש על ידי Firebase
    }

    /**
     * Instantiates a new Order.
     *
     * @param oId         the o id
     * @param dateOfOrder the date of order
     * @param ordererUID  the orderer uid
     * @param productsLen the products len
     */
    public Order(String oId, Date dateOfOrder, String ordererUID, int productsLen) {
        this.dateOfOrder = dateOfOrder.getTime();
        this.ordererUID = ordererUID;
        this.oId = oId;
        this.isFinished = false;
        this.products = new ArrayList<>(productsLen); // מוכן לגודל משוער
        calculatePrepTime();
    }

    /**
     * Gets total to pay.
     *
     * @return the total to pay
     */
    public double getTotalToPay() {
        return totalToPay;
    }

    /**
     * Sets total to pay.
     *
     * @param totalToPay the total to pay
     */
    public void setTotalToPay(double totalToPay) {
        this.totalToPay = totalToPay;
    }

    /**
     * Gets requested time.
     *
     * @return the requested time
     */
    public long getRequestedTime() {
        return requestedTime;
    }

    /**
     * Sets requested time.
     *
     * @param timeInMillis the time in millis
     */
    public void setRequestedTime(long timeInMillis) {
        this.requestedTime = timeInMillis;
    }

    /**
     * Gets date of order.
     *
     * @return the date of order
     */
    public long getDateOfOrder() {
        return dateOfOrder;
    }

    /**
     * Sets date of order.
     *
     * @param dateOfOrder the date of order
     */
    public void setDateOfOrder(long dateOfOrder) {
        this.dateOfOrder = dateOfOrder;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getoId() {
        return oId;
    }

    /**
     * Sets id.
     *
     * @param oId the o id
     */
    public void setoId(String oId) {
        this.oId = oId;
    }

    /**
     * Gets orderer uid.
     *
     * @return the orderer uid
     */
    public String getOrdererUID() {
        return ordererUID;
    }

    /**
     * Sets orderer uid.
     *
     * @param ordererUID the orderer uid
     */
    public void setOrdererUID(String ordererUID) {
        this.ordererUID = ordererUID;
    }

    /**
     * Gets products.
     *
     * @return the products
     */
    public List<Product> getProducts() {
        return products;
    }

    /**
     * Sets products.
     *
     * @param products the products
     */
    public void setProducts(List<Product> products) {
        this.products = products;
        this.productsString = "";
        for (Product product : this.products) {
            this.productsString += String.format("%s ,", product.getProductName());
        }
    }

    /**
     * Sets finished.
     *
     * @param isFinished the is finished
     */
    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    /**
     * Gets is finished.
     *
     * @return the is finished
     */
    public boolean getIsFinished() {
        return this.isFinished;
    }

    /**
     * Gets products string.
     *
     * @return the products string
     */
    public String getProductsString() {
        return this.productsString;
    }

    /**
     * Gets total order price.
     *
     * @return the total order price
     */
    public double getTotalOrderPrice() {
        double sum = 0;
        for (Product p : this.products) {
            sum += p.getTotalProductPrice();
        }
        return sum;
    }

    /**
     * Gets product amount.
     *
     * @param p the p
     * @return the product amount
     */
    public int getProductAmount(Product p) {
        int amount = 0;
        for (Product product : this.products) {
            if (product.equals(p)) // שים לב: השוואה לפי equals
                amount += 1;
        }
        return amount;
    }

    /**
     * Requires freshness.
     */
    public void requiresFreshness() {
        for (Product p : this.products) {
            if (p.requiresFreshness()) {
                this.requiresFreshness = true;
                return;
            }
        }
        this.requiresFreshness = false;
    }

    /**
     * Gets ideal prep time.
     *
     * @return the ideal prep time
     */
    public long getIdealPrepTime() {
        return this.idealPrepTime;
    }

    /**
     * Gets prep time.
     *
     * @return the prep time
     */
    public long getPrepTime() {
        return this.prepTime;
    }

    /**
     * Sets ideal prep time.
     *
     * @param idealPrepTime the ideal prep time
     */
    public void setIdealPrepTime(long idealPrepTime) {
        this.idealPrepTime = idealPrepTime;
    }

    /**
     * Calculate prep time.
     */
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

    /**
     * Calculate ideal preparation time firebase based sync.
     */
    public void calculateIdealPreparationTimeFirebaseBasedSync() {
        if (this.requiresFreshness) {
            long calculated = this.requestedTime - this.prepTime;
            this.idealPrepTime = Math.max(calculated, System.currentTimeMillis());
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
