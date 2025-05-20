package com.example.shnitsik;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shnitsik.models.AddOn;
import com.example.shnitsik.models.CartManager;
import com.example.shnitsik.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Product> cartProducts;
    private CartManager cartManager;
    private OnCartChangedListener cartChangedListener;
    private Context context;

    public interface OnCartChangedListener {
        void onCartChanged();
    }

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
        holder.productPrice.setText("₪" + cartManager.getTotalProductPriceWithAddOns(product));

        Glide.with(context)
                .load(product.getImageUrl())
                .into(holder.productImage);

        StringBuilder addOnText = new StringBuilder();
        for (AddOn addOn : product.getAddOns()) {
            if (addOn.isSelected()) {
                addOnText.append("+ ")
                        .append(addOn.getAddOnName())
                        .append(" (x")
                        .append(addOn.getAmount())
                        .append(")\n");
            }
        }
        holder.productAddOns.setText(addOnText.toString().trim());

        holder.itemView.setOnClickListener(v -> showEditDialog(product, position));
    }

    private void showEditDialog(Product product, int position) {
        List<AddOn> originalAddOns = product.getAddOns();
        List<AddOn> clonedAddOns = new ArrayList<>();

        for (AddOn addOn : originalAddOns) {
            AddOn clone = new AddOn(addOn.getAddOnName(), addOn.getPricePerOneAmount());
            clone.setAmount(addOn.getAmount());
            clonedAddOns.add(clone);
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
            product.setAddOns(clonedAddOns);
            cartProducts.set(position, product); // במקום cartManager.getCart().set
            notifyItemChanged(position);
            if (cartChangedListener != null) cartChangedListener.onCartChanged();
        });

        builder.setNegativeButton("Remove", (dialog, which) -> {
            cartManager.removeProductFromCart(product);
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

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productAddOns;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartItemImage);
            productName = itemView.findViewById(R.id.cartItemName);
            productPrice = itemView.findViewById(R.id.cartItemPrice);
            productAddOns = itemView.findViewById(R.id.cartItemAddOns);
        }
    }
}
