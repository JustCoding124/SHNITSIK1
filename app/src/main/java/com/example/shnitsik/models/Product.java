package com.example.shnitsik.models;

import java.util.List;

public class Product {
    // זמן הכנה בדקות
    private long prepTime;
    private String productId;
    private String productName;
    private double price;
    private boolean isHeader = false;
    private String category;
    private String description;
    private List<AddOn> addOns;
    private boolean requiresFreshness;
    private String imageUrl;

    // קונסטרקטור ריק נדרש ל־Firestore
    public Product() {}

    public Product(String productId, boolean requiresFreshness, String productName, double price,
                   String category, String description,
                   List<AddOn> addOns) {
        this.productId = productId;
        this.requiresFreshness = requiresFreshness;
        this.productName = productName;
        this.price = price;
        this.category = category;
        this.description = description;
        this.addOns = addOns;
    }

    public Product(String category, boolean isHeader) {
        this.productName = category;
        this.isHeader = isHeader;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    // זמן הכנה בדקות
    public long getPrepTime() {
        return this.prepTime;
    }

    public void setPrepTime(long prepTime) {
        this.prepTime = prepTime;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean requiresFreshness() {
        return this.requiresFreshness;
    }

    public void setRequiresFreshness(boolean requiresFreshness) {
        this.requiresFreshness = requiresFreshness;
    }

    public List<AddOn> getAddOns() {
        return this.addOns;
    }

    public void setAddOns(List<AddOn> addOns) {
        this.addOns = addOns;
    }

    public String getImageUrl() {
        return this.imageUrl != null ? this.imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getTotalProductPrice() {
        double addOnPrice = 0;
        if (addOns != null) {
            for (AddOn addOn : addOns) {
                addOnPrice += addOn.getTotalAddOnPrice();
            }
        }
        return this.price + addOnPrice;
    }

    public String getAddOnDescription() {
        if (this.addOns == null || this.addOns.isEmpty()) {
            return "No Addons";
        }
        StringBuilder description = new StringBuilder();
        for (AddOn a : this.addOns) {
            description.append(a.getAddOnName())
                    .append(", Amount: ")
                    .append(a.getAmount())
                    .append("\n");
        }
        return description.toString();
    }
}
