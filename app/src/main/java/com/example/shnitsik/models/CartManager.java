package com.example.shnitsik;
import java.util.HashSet;

public class CartManager {
    private HashSet<Product> cart = new HashSet<>(); // Store cart items

    // Add a product to the cart
    public void addProductToCart(Product product) {
        cart.add(product);
    }

    // Remove a product from the cart
    public void removeProductFromCart(Product product) {
        cart.remove(product);
    }

    // Get all products in the cart
    public HashSet<Product> getCart() {
        return cart;
    }

    // Get the total price of all items in the cart, including add-ons
    public double getTotalCartPrice() {
        double total = 0;
        for (Product product : cart) {
            total += getTotalProductPriceWithAddOns(product);
        }
        return total;
    }

    // Get total price for a single product, including add-ons
    public double getTotalProductPriceWithAddOns(Product product) {
        double total = product.getPrice(); // Base price of product
        for (AddOn addOn : product.getAddOns()) {
            total += addOn.getPricePerOneAmount(); // Add add-on price
        }
        return total;
    }
}
