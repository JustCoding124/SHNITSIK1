package com.example.shnitsik;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        holder.productPrice.setText("$" + cartManager.getTotalProductPriceWithAddOns(product));

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
        List<AddOn> addOns = product.getAddOns();
        String[] addOnNames = new String[addOns.size()];
        boolean[] selected = new boolean[addOns.size()];

        for (int i = 0; i < addOns.size(); i++) {
            addOnNames[i] = addOns.get(i).getAddOnName();
            selected[i] = addOns.get(i).isSelected();
        }

        new AlertDialog.Builder(context)
                .setTitle("Edit " + product.getProductName())
                .setMultiChoiceItems(addOnNames, selected, (dialog, which, isChecked) -> {
                    addOns.get(which).setAmount(isChecked ? 1 : 0);
                })
                .setPositiveButton("Save", (dialog, which) -> {
                    notifyItemChanged(position);
                    if (cartChangedListener != null) cartChangedListener.onCartChanged();
                })
                .setNegativeButton("Remove", (dialog, which) -> {
                    cartManager.removeProductFromCart(product);
                    cartProducts.remove(position);
                    notifyItemRemoved(position);
                    if (cartChangedListener != null) cartChangedListener.onCartChanged();
                })
                .setNeutralButton("Cancel", null)
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
