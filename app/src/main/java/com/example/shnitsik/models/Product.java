package com.example.shnitsik.models;

import java.util.List;

/**
 * The type Product.
 */
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

    /**
     * Instantiates a new Product.
     */
// קונסטרקטור ריק נדרש ל־Firestore
    public Product() {}

    /**
     * Instantiates a new Product.
     *
     * @param productId         the product id
     * @param requiresFreshness the requires freshness
     * @param productName       the product name
     * @param price             the price
     * @param category          the category
     * @param description       the description
     * @param addOns            the add ons
     */
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

    /**
     * Instantiates a new Product.
     *
     * @param category the category
     * @param isHeader the is header
     */
    public Product(String category, boolean isHeader) {
        this.productName = category;
        this.isHeader = isHeader;
    }

    /**
     * Is header boolean.
     *
     * @return the boolean
     */
    public boolean isHeader() {
        return isHeader;
    }

    /**
     * Gets product id.
     *
     * @return the product id
     */
    public String getProductId() {
        return this.productId;
    }

    /**
     * Sets product id.
     *
     * @param productId the product id
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Gets product name.
     *
     * @return the product name
     */
    public String getProductName() {
        return this.productName;
    }

    /**
     * Sets product name.
     *
     * @param productName the product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Gets prep time.
     *
     * @return the prep time
     */
// זמן הכנה בדקות
    public long getPrepTime() {
        return this.prepTime;
    }

    /**
     * Sets prep time.
     *
     * @param prepTime the prep time
     */
    public void setPrepTime(long prepTime) {
        this.prepTime = prepTime;
    }

    /**
     * Gets price.
     *
     * @return the price
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * Sets price.
     *
     * @param price the price
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets category.
     *
     * @return the category
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Sets category.
     *
     * @param category the category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Requires freshness boolean.
     *
     * @return the boolean
     */
    public boolean requiresFreshness() {
        return this.requiresFreshness;
    }

    /**
     * Sets requires freshness.
     *
     * @param requiresFreshness the requires freshness
     */
    public void setRequiresFreshness(boolean requiresFreshness) {
        this.requiresFreshness = requiresFreshness;
    }

    /**
     * Gets add ons.
     *
     * @return the add ons
     */
    public List<AddOn> getAddOns() {
        return this.addOns;
    }

    /**
     * Sets add ons.
     *
     * @param addOns the add ons
     */
    public void setAddOns(List<AddOn> addOns) {
        this.addOns = addOns;
    }

    /**
     * Gets image url.
     *
     * @return the image url
     */
    public String getImageUrl() {
        return this.imageUrl != null ? this.imageUrl : "";
    }

    /**
     * Sets image url.
     *
     * @param imageUrl the image url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets total product price.
     *
     * @return the total product price
     */
    public double getTotalProductPrice() {
        double addOnPrice = 0;
        if (addOns != null) {
            for (AddOn addOn : addOns) {
                addOnPrice += addOn.getTotalAddOnPrice();
            }
        }
        return this.price + addOnPrice;
    }

    /**
     * Gets add on description.
     *
     * @return the add on description
     */
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
