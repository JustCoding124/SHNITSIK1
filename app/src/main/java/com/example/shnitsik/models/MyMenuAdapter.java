package com.example.shnitsik.models;

import android.app.AlertDialog;
import android.content.Context;
import com.bumptech.glide.Glide;
import com.example.shnitsik.R;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyMenuAdapter extends RecyclerView.Adapter<MyMenuAdapter.MenuViewHolder> {

    private Context context;
    private List<Product> originalList = new ArrayList<>();
    private List<Product> productList;
    private final CartManager cartManager = SharedCart.getInstance().getCartManager();

    public MyMenuAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_header, parent, false);
            return new CategoryHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.menue_item, parent, false);
            return new MenuViewHolder(view);
        }
    }

    public void setOriginalList(List<Product> list) {
        originalList = new ArrayList<>(list);
        productList = new ArrayList<>(list);
    }

    @Override
    public int getItemViewType(int position) {
        return productList.get(position).isHeader() ? 0 : 1;
    }

    public void filterListByName(String query) {
        if (query.isEmpty()) {
            productList = new ArrayList<>(originalList);
        } else {
            List<Product> filtered = new ArrayList<>();
            String currentCategory = null;

            for (Product item : originalList) {
                if (item.isHeader()) {
                    currentCategory = item.getProductName();
                } else if (item.getProductName().toLowerCase().contains(query.toLowerCase())) {
                    if (filtered.isEmpty() || !filtered.get(filtered.size() - 1).isHeader() ||
                            !filtered.get(filtered.size() - 1).getProductName().equals(currentCategory)) {
                        filtered.add(new Product(currentCategory, true));
                    }
                    filtered.add(item);
                }
            }
            productList = filtered;
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Product product = productList.get(position);
        if (holder instanceof CategoryHeaderViewHolder) {
            ((CategoryHeaderViewHolder) holder).categoryTitle.setText(product.getProductName());
        } else {
            holder.textViewProductName.setText(product.getProductName());
            holder.textViewProductDescription.setText(product.getDescription());
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .into(holder.imageViewProduct);
            }

            holder.textViewPrice.setText("Price: " + product.getPrice());

            if (cartManager.getCart().contains(product)) {
                holder.itemView.setBackgroundColor(Color.GREEN);
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }

            holder.itemView.setOnClickListener(v -> showAddonDialog(position, product));
        }
    }

    private void showAddonDialog(int position, Product product) {
        if (product.isHeader()) return;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_addon_quantity, null);
        SeekBar productSeekBar = dialogView.findViewById(R.id.quantitySeekBar);
        TextView quantityValue = dialogView.findViewById(R.id.quantityValue);

        productSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quantityValue.setText(String.valueOf(progress + 1));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setTitle("Select Addons for " + product.getProductName());

        List<AddOn> allAddOns = product.getAddOns();
        List<AddOn> selectedAddOns = new ArrayList<>();

        String[] addOnNames = new String[allAddOns.size()];
        boolean[] selectedItems = new boolean[allAddOns.size()];

        for (int i = 0; i < allAddOns.size(); i++) {
            addOnNames[i] = allAddOns.get(i).getAddOnName() + " - ₪" + allAddOns.get(i).getPricePerOneAmount();
        }

        builder.setMultiChoiceItems(addOnNames, selectedItems, (dialog, index, isChecked) -> {
            AddOn originalAddOn = allAddOns.get(index);
            if (isChecked) {
                showAddOnAmountDialog(originalAddOn, () -> {
                    AddOn copiedAddOn = new AddOn(
                            originalAddOn.getAddOnName(),
                            originalAddOn.getPricePerOneAmount()
                    );
                    copiedAddOn.setAmount(originalAddOn.getAmount());
                    selectedAddOns.add(copiedAddOn);
                });
            } else {
                for (Iterator<AddOn> iterator = selectedAddOns.iterator(); iterator.hasNext();) {
                    if (iterator.next().getAddOnName().equals(originalAddOn.getAddOnName())) {
                        iterator.remove();
                        break;
                    }
                }
            }
        });

        builder.setPositiveButton("Add to Cart", (dialog, which) -> {
            int mainQuantity = productSeekBar.getProgress() + 1;

            for (int i = 0; i < mainQuantity; i++) {
                Product productToCart = new Product(
                        product.getProductId(),
                        product.requiresFreshness(),
                        product.getProductName(),
                        product.getPrice(),
                        product.getCategory(),
                        product.getDescription(),
                        new ArrayList<>(selectedAddOns)
                );
                productToCart.setImageUrl(product.getImageUrl());
                cartManager.addProductToCart(productToCart);
            }

            notifyItemChanged(position);
        });

        builder.setNegativeButton("Remove from Cart", (dialog, which) -> {
            cartManager.removeProductFromCart(product);
            notifyItemChanged(position);
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void showAddOnAmountDialog(AddOn addOn, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select quantity for " + addOn.getAddOnName());

        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_addon_quantity, null);
        SeekBar seekBar = view.findViewById(R.id.quantitySeekBar);
        TextView quantityText = view.findViewById(R.id.quantityValue);

        seekBar.setProgress(0);
        quantityText.setText("1");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quantityText.setText(String.valueOf(progress + 1));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(view);
        builder.setPositiveButton("OK", (dialog, which) -> {
            int quantity = seekBar.getProgress() + 1;
            addOn.setAmount(quantity);
            onConfirm.run();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class CategoryHeaderViewHolder extends MenuViewHolder {
        TextView categoryTitle;
        public CategoryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitleTextView);
        }
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName;
        ImageView imageViewProduct;
        TextView textViewProductDescription;
        TextView textViewPrice;
        Button addAnotherButton;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.itemImage);
            textViewProductName = itemView.findViewById(R.id.itemName);
            textViewProductDescription = itemView.findViewById(R.id.itemDescription);
            textViewPrice = itemView.findViewById(R.id.itemPrice);
            addAnotherButton = itemView.findViewById(R.id.addAnotherButton);
        }
    }
}
