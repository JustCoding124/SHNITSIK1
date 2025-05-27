package com.example.shnitsik.models;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * The type Cart manager.
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
