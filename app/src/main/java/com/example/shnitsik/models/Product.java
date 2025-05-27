package com.example.shnitsik.models;

import java.util.List;

/**
 * Represents a product within the application, such as a food item in a restaurant menu.
 * This class encapsulates all relevant details of a product, including its identification,
 * pricing, categorization, and any associated modifications or add-ons. It also handles
 * properties like preparation time and whether the product needs to be fresh, which can be
 * crucial for inventory and order management. Additionally, it supports a visual representation
 * through an image URL and can function as a header for grouping products in a user interface.
 *
 * The class is designed to be compatible with Firestore, as indicated by the presence of a
 * no-argument constructor. It provides constructors for creating fully detailed product instances
 * and simpler instances primarily used as UI headers.
 *
 * The variables within this class serve specific purposes:
 * <ul>
 *     <li>{@code prepTime}: A {@code long} representing the estimated preparation time for the product in minutes. This is useful for kitchen order systems or for providing customers with estimated wait times.</li>
 *     <li>{@code productId}: A {@code String} that uniquely identifies the product. This is essential for database operations, order tracking, and inventory management.</li>
 *     <li>{@code productName}: A {@code String} representing the display name of the product (e.g., "Chicken Schnitzel", "Caesar Salad").</li>
 *     <li>{@code price}: A {@code double} indicating the base price of the product before any add-ons or modifications.</li>
 *     <li>{@code isHeader}: A {@code boolean} flag that, when true, signifies this product instance is being used as a header or separator in a list of products, typically for grouping items by category in a UI. If true, the {@code productName} might be used as the header title.</li>
 *     <li>{@code category}: A {@code String} specifying the category the product belongs to (e.g., "Main Courses", "Appetizers", "Drinks"). This helps in organizing and filtering products.</li>
 *     <li>{@code description}: A {@code String} providing a more detailed description of the product, which could include ingredients, preparation methods, or portion size.</li>
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
