// CartActivity_Fixed.java
package com.example.shnitsik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shnitsik.models.CartManager;
import com.example.shnitsik.models.Product;
import com.example.shnitsik.models.SharedCart;
import com.example.shnitsik.models.CartAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Cart activity.
 */
public class CartActivity extends AppCompatActivity {
    private CartManager cartManager;
    private RecyclerView cartRecyclerView;
    private TextView totalPriceTextView;
    private View checkoutButton;
    private View backButton;
    private CartAdapter cartAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = SharedCart.getInstance().getCartManager();
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        backButton = findViewById(R.id.backButton);

        productList = new ArrayList<>(cartManager.getCart());

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(productList, cartManager, () -> {
            productList.clear();
            productList.addAll(cartManager.getCart());
            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
        });

        cartRecyclerView.setAdapter(cartAdapter);
        updateTotalPrice();

        checkoutButton.setOnClickListener(v -> {
            if (cartManager.getCart().isEmpty()) {
                Toast.makeText(CartActivity.this, "Cart is empty. Add items before proceeding to checkout.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        productList.clear();
        productList.addAll(cartManager.getCart());
        cartAdapter.notifyDataSetChanged();
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        totalPriceTextView.setText("Total: ₪" + cartManager.getTotalCartPrice());
    }
}
