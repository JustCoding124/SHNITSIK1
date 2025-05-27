package com.example.shnitsik.models;

/**
 * Represents a shared shopping cart that can be accessed globally within the application.
 * This class follows the Singleton design pattern, meaning that only one instance of
 * {@code SharedCart} can exist throughout the application's lifecycle. This single instance
 * holds a reference to a {@link CartManager}, which is responsible for managing the
 * items within the shopping cart, such as adding, removing, and updating quantities.
 *
 * The primary purpose of this class is to provide a centralized access point to the
 * application's shopping cart functionality. By using the Singleton pattern, any part
 * of the application can retrieve the same cart instance and interact with it, ensuring
 * data consistency across different views or components.
 *
 * @author Ariel Kanitork
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
