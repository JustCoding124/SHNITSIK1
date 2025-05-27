// Fixed CartAdapter.java
package com.example.shnitsik.models;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shnitsik.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Cart adapter.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final List<Product> cartProducts;
    private final CartManager cartManager;
    private final OnCartChangedListener cartChangedListener;
    private Context context;

    /**
     * The interface On cart changed listener.
     */
    public interface OnCartChangedListener {
        /**
         * On cart changed.
         */
        void onCartChanged();
    }

    /**
     * Instantiates a new Cart adapter.
     *
     * @param cartProducts the cart products
     * @param cartManager  the cart manager
     * @param listener     the listener
     */
    public CartAdapter(List<Product> cartProducts, CartManager cartManager, OnCartChangedListener listener) {
        this.cartProducts = cartProducts;
        this.cartManager = cartManager;
        this.cartChangedListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartProducts.get(position);

        holder.productName.setText(product.getProductName());
        holder.productPrice.setText("₪" + product.getTotalProductPrice());

        Glide.with(context).clear(holder.productImage);
        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);

        StringBuilder addOnText = new StringBuilder();
        for (AddOn addOn : product.getAddOns()) {
            if (addOn.getAmount() > 0) {
                addOnText.append("+ ").append(addOn.getAddOnName())
                        .append(" (x").append(addOn.getAmount()).append(")\n");
            }
        }
        holder.productAddOns.setText(addOnText.toString().trim());

        holder.itemView.setOnClickListener(v -> showEditDialog(product, position));
    }

    private void showEditDialog(Product product, int position) {
        List<AddOn> clonedAddOns = new ArrayList<>();
        for (AddOn addOn : product.getAddOns()) {
            AddOn cloned = new AddOn(addOn.getAddOnName(), addOn.getPricePerOneAmount());
            cloned.setAmount(addOn.getAmount());
            clonedAddOns.add(cloned);
        }

        String[] addOnNames = new String[clonedAddOns.size()];
        boolean[] selected = new boolean[clonedAddOns.size()];
        for (int i = 0; i < clonedAddOns.size(); i++) {
            AddOn addon = clonedAddOns.get(i);
            addOnNames[i] = addon.getAddOnName() + " (₪" + addon.getPricePerOneAmount() + ")";
            selected[i] = addon.getAmount() > 0;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit " + product.getProductName());
        builder.setMultiChoiceItems(addOnNames, selected, (dialog, which, isChecked) -> {
            if (!isChecked) {
                clonedAddOns.get(which).setAmount(0);
            } else {
                showQuantityDialog(clonedAddOns.get(which));
            }
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            Product updatedProduct = new Product(
                    product.getProductId(),
                    product.requiresFreshness(),
                    product.getProductName(),
                    product.getPrice(),
                    product.getCategory(),
                    product.getDescription(),
                    clonedAddOns
            );
            updatedProduct.setImageUrl(product.getImageUrl());

            cartManager.remove(product);
            cartProducts.set(position, updatedProduct);
            cartManager.addProductToCart(updatedProduct);

            notifyItemChanged(position);
            if (cartChangedListener != null) cartChangedListener.onCartChanged();
        });

        builder.setNegativeButton("Remove", (dialog, which) -> {
            cartManager.remove(product);
            cartProducts.remove(position);
            notifyItemRemoved(position);
            if (cartChangedListener != null) cartChangedListener.onCartChanged();
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void showQuantityDialog(AddOn addOn) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_addon_quantity, null);
        SeekBar seekBar = view.findViewById(R.id.quantitySeekBar);
        TextView quantityValue = view.findViewById(R.id.quantityValue);

        seekBar.setProgress(addOn.getAmount() > 0 ? addOn.getAmount() - 1 : 0);
        quantityValue.setText(String.valueOf(seekBar.getProgress() + 1));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quantityValue.setText(String.valueOf(progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new AlertDialog.Builder(context)
                .setTitle("Select Quantity for " + addOn.getAddOnName())
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    int quantity = seekBar.getProgress() + 1;
                    addOn.setAmount(quantity);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return cartProducts.size();
    }

    /**
     * The type Cart view holder.
     */
    static class CartViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Product image.
         */
        ImageView productImage;
        /**
         * The Product name.
         */
        TextView productName;
        /**
         * The Product price.
         */
        TextView productPrice;
        /**
         * The Product add ons.
         */
        TextView productAddOns;

        /**
         * Instantiates a new Cart view holder.
         *
         * @param itemView the item view
         */
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartItemImage);
            productName = itemView.findViewById(R.id.cartItemName);
            productPrice = itemView.findViewById(R.id.cartItemPrice);
            productAddOns = itemView.findViewById(R.id.cartItemAddOns);
        }
    }
}
