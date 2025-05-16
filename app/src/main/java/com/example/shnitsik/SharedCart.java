package com.example.shnitsik;

public class SharedCart {
    private static final SharedCart instance = new SharedCart();
    private final CartManager cartManager;

    private SharedCart() {
        cartManager = new CartManager();
    }

    public static SharedCart getInstance() {
        return instance;
    }

    public CartManager getCartManager() {
        return cartManager;
    }
}
