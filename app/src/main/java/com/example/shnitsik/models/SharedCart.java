package com.example.shnitsik.models;

/**
 * The type Shared cart.
 */
public class SharedCart {
    private static final SharedCart instance = new SharedCart();
    private final CartManager cartManager;

    private SharedCart() {
        cartManager = new CartManager();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static SharedCart getInstance() {
        return instance;
    }

    /**
     * Gets cart manager.
     *
     * @return the cart manager
     */
    public CartManager getCartManager() {
        return cartManager;
    }
}
