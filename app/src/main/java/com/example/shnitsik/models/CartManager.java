package com.example.shnitsik.models;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the shopping cart for an application.
 * This class is responsible for handling operations related to products within a user's shopping cart,
 * such as adding, removing, and calculating the total cost.
 * It utilizes a {@link HashSet} to store {@link Product} objects, ensuring that each unique product
 * (based on its {@code equals} and {@code hashCode} implementation, which typically relies on the product ID)
 * is present only once. This is particularly useful for scenarios where adding the same product multiple
 * times might be handled by quantity updates rather than duplicate entries, or where the cart represents
 * a set of distinct items.
 * * Note: While the cart uses a {@link HashSet} to store products, uniqueness is only enforced
 *  * if the {@link Product} class properly overrides {@code equals()} and {@code hashCode()}.
 *  * Otherwise, each added {@link Product} instance, even with identical content, may be treated as distinct.
 * A key feature of this class is its use of product cloning. When a {@link Product} is added to the cart,
 * a deep copy of the product is created and stored. This prevents external modifications to the original
 * product object from affecting the state of the product within the cart, and vice-versa. This ensures
 * data integrity and isolation between the cart's contents and the broader application's product catalog.
 *
 * The cart itself is represented by the private instance variable {@code cart}, which is a {@code HashSet<Product>}.
 * This choice of data structure implies that the order of items in the cart is not guaranteed and that
 * duplicate product entries (based on their equality) are automatically handled.
 *
 * @author Ariel Kanitork
 */
public class CartManager {
    private HashSet<Product> cart = new HashSet<>(); // Store cart items

    /**
     * Add product to cart.
     *
     * @param product the product
     */
// Add a product to the cart
    public void addProductToCart(Product product) {
        cart.add(cloneProduct(product));
    }

    /**
     * Remove product from cart.
     *
     * @param product the product
     */
// Remove all instances of a product from the cart
    public void removeProductFromCart(Product product) {
        Iterator<Product> iterator = cart.iterator();
        while (iterator.hasNext()) {
            Product p = iterator.next();
            if (p.getProductId().equals(product.getProductId())) {
                iterator.remove();
            }
        }
    }

    /**
     * Remove.
     *
     * @param product the product
     */
// Remove a product from the cart
    public void remove(Product product) {
        cart.remove(product);
    }

    /**
     * Gets cart.
     *
     * @return the cart
     */
// Get all products in the cart
    public HashSet<Product> getCart() {
        return cart;
    }

    /**
     * Gets total cart price.
     *
     * @return the total cart price
     */
// Get the total price of all items in the cart, including add-ons
    public double getTotalCartPrice() {
        double total = 0;
        for (Product product : cart) {
            total += getTotalProductPriceWithAddOns(product);
        }
        return total;
    }

    /**
     * Gets total product price with add ons.
     *
     * @param product the product
     * @return the total product price with add ons
     */
// Get total price for a single product, including add-ons
    public double getTotalProductPriceWithAddOns(Product product) {
        double total = product.getPrice(); // Base price
        for (AddOn addOn : product.getAddOns()) {
            total += addOn.getPricePerOneAmount() * addOn.getAmount(); // כמות * מחיר
        }
        return total;
    }
    /**
     * Creates a deep copy of the given product, including its add-ons,
     * to avoid shared references when adding to the cart.
     *
     * @param original the product to clone
     * @return a new product instance with the same data
     */

    private Product cloneProduct(Product original) {
        Product clone = new Product(
                original.getProductId(),
                original.requiresFreshness(),
                original.getProductName(),
                original.getPrice(),
                original.getCategory(),
                original.getDescription(),
                new ArrayList<>()
        );
        clone.setImageUrl(original.getImageUrl());
        clone.setPrepTime(original.getPrepTime());

        // שיבוט AddOns
        if (original.getAddOns() != null) {
            List<AddOn> addOnClones = new ArrayList<>();
            for (AddOn addOn : original.getAddOns()) {
                AddOn addOnClone = new AddOn(addOn.getAddOnName(), addOn.getPricePerOneAmount());
                addOnClone.setAmount(addOn.getAmount());
                addOnClones.add(addOnClone);
            }
            clone.setAddOns(addOnClones);
        }

        return clone;
    }


}
