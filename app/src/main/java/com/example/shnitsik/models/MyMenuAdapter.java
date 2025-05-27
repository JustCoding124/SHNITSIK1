package com.example.shnitsik.models;

import android.app.AlertDialog;
import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.example.shnitsik.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Adapter for displaying a list of products in a RecyclerView.
 * This adapter is responsible for managing and displaying product items, including category headers,
 * in a scrollable list. It allows users to view product details, select addons, specify quantities,
 * and add products to a shopping cart. It also supports filtering the product list by name.
 *
 * The adapter uses two distinct view types:
 * 1.  A view for displaying category headers ({@code viewType == 0}).
 * 2.  A view for displaying individual product items ({@code viewType == 1}).
 *
 * It interacts with a {@link CartManager} (obtained via {@link SharedCart}) to handle cart-related
 * operations such as adding or removing products. Product images are loaded and displayed
 * using the Glide library.
 *
 * Key functionalities include:
 * - Displaying product name, description, price, and image.
 * - Displaying category headers to organize products.
 * - Filtering the product list based on a user-provided search query.
 * - Presenting a dialog for selecting product addons and specifying the quantity for the main product.
 * - Presenting a sub-dialog for specifying the quantity of each selected addon.
 * - Adding products (along with their selected addons and quantities) to the shopping cart.
 * - Removing products from the shopping cart.
 * - Visually indicating (e.g., by changing background color) if a product is already in the cart.
 *
 * @see RecyclerView.Adapter
 * @see MenuViewHolder
 * @see CartManager
 * @see SharedCart
 */
public class MyMenuAdapter extends RecyclerView.Adapter<MyMenuAdapter.MenuViewHolder> {

    private Context context;
    private List<Product> originalList = new ArrayList<>();
    private List<Product> productList;
    private final CartManager cartManager = SharedCart.getInstance().getCartManager();

    /**
     * Instantiates a new My menu adapter.
     *
     * @param context     the context
     * @param productList the product list
     */
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

    /**
     * Sets original list.
     *
     * @param list the list
     */
    public void setOriginalList(List<Product> list) {
        originalList = new ArrayList<>(list);
        productList = new ArrayList<>(list);
    }

    @Override
    public int getItemViewType(int position) {
        return productList.get(position).isHeader() ? 0 : 1;
    }

    /**
     * Filter list by name.
     *
     * @param query the query
     */
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
            Glide.with(context).clear(holder.imageViewProduct);
            Glide.with(context)
                    .load(product.getImageUrl())
                    .into(holder.imageViewProduct);

            holder.textViewPrice.setText("Price: " + product.getPrice());

            holder.itemView.setBackgroundColor(cartManager.getCart().contains(product) ? Color.GREEN : Color.WHITE);

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
                Iterator<AddOn> iterator = selectedAddOns.iterator();
                while (iterator.hasNext()) {
                    AddOn addOn = iterator.next();
                    if (addOn.getAddOnName().equals(originalAddOn.getAddOnName())) {
                        iterator.remove();
                        break;
                    }
                }
            }
        });

        builder.setPositiveButton("Add to Cart", (dialog, which) -> {
            int mainQuantity = productSeekBar.getProgress() + 1;
            for (int i = 0; i < mainQuantity; i++) {
                Product newProduct = createProductInstanceFromMenu(product, selectedAddOns);
                cartManager.addProductToCart(newProduct);
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

    private Product createProductInstanceFromMenu(Product original, List<AddOn> selectedAddOns) {
        List<AddOn> allAddOnsWithAmounts = new ArrayList<>();
        for (AddOn addOn : original.getAddOns()) {
            int selectedAmount = 0;
            for (AddOn selected : selectedAddOns) {
                if (selected.getAddOnName().equals(addOn.getAddOnName())) {
                    selectedAmount = selected.getAmount();
                    break;
                }
            }
            AddOn cloned = new AddOn(addOn.getAddOnName(), addOn.getPricePerOneAmount());
            cloned.setAmount(selectedAmount);
            allAddOnsWithAmounts.add(cloned);
        }
        Product newProduct = new Product(
                original.getProductId(),
                original.requiresFreshness(),
                original.getProductName(),
                original.getPrice(),
                original.getCategory(),
                original.getDescription(),
                allAddOnsWithAmounts
        );
        newProduct.setImageUrl(original.getImageUrl());
        return newProduct;
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * The type Category header view holder.
     */
    public static class CategoryHeaderViewHolder extends MenuViewHolder {
        /**
         * The Category title.
         */
        TextView categoryTitle;

        /**
         * Instantiates a new Category header view holder.
         *
         * @param itemView the item view
         */
        public CategoryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitleTextView);
        }
    }

    /**
     * The type Menu view holder.
     */
    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Text view product name.
         */
        TextView textViewProductName;
        /**
         * The Image view product.
         */
        ImageView imageViewProduct;
        /**
         * The Text view product description.
         */
        TextView textViewProductDescription;
        /**
         * The Text view price.
         */
        TextView textViewPrice;
        /**
         * The Add another button.
         */
        Button addAnotherButton;

        /**
         * Instantiates a new Menu view holder.
         *
         * @param itemView the item view
         */
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
