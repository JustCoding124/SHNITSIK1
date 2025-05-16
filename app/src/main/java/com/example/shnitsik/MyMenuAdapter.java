package com.example.shnitsik;

import android.app.AlertDialog;
import android.content.Context;
import com.bumptech.glide.Glide;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
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
import java.util.Arrays;
import java.util.List;
public class MyMenuAdapter extends RecyclerView.Adapter<MyMenuAdapter.MenuViewHolder> {

    private Context context;
    private List<Product> productList;
    private final CartManager cartManager = SharedCart.getInstance().getCartManager();

    public MyMenuAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menue_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewProductName.setText(product.getProductName());
        holder.textViewProductDescription.setText(product.getDescription());
        holder.textViewProductDescription.setText(product.getDescription());
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl()) // זה בעצם URL
                    .into(holder.imageViewProduct);
        }




        // הצג את המחיר, כולל תוספות אם נבחר.
        double totalPrice = product.getPrice();
        for (AddOn addOn : product.getAddOns()) {
            if (addOn.isSelected()) {
                totalPrice += addOn.getTotalAddOnPrice();
            }
        }
        holder.textViewPrice.setText("Price: " + totalPrice);

        // שנה את צבע הרקע על סמך האם הפריט נמצא בעגלה.
        if (cartManager.getCart().contains(product)) {
            holder.itemView.setBackgroundColor(Color.GREEN);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> showAddonDialog(position, product));
    }

    private void showAddonDialog(int position, Product product) {
        List<AddOn> addons = product.getAddOns();
        boolean[] selected = new boolean[addons.size()];
        List<AddOn> currentSelectedAddons = new ArrayList<>();
        // הגדר תוספות נבחרות על סמך תכולת העגלה.
        if (cartManager.getCart().contains(product)) {
            for (AddOn addOn : product.getAddOns()) {
                if (currentSelectedAddons.contains(addOn)) {
                    selected[addons.indexOf(addOn)] = true;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        SeekBar seekBar = builder.create().findViewById(R.id.quantitySeekBar);
        TextView quantityValue = builder.create().findViewById(R.id.quantityValue);

        // שימוש בשרשור מתודות לכתיבת קוד תמציתי וקריא, מתאפשר כי כל מתודה מחזירה את האובייקט AlertDialog.Builder עצמו:

        builder.setTitle("Select Addons for " + product.getProductName())
                .setMultiChoiceItems(getAddOnNamesWithPrices(addons), selected, (dialog, which, isChecked) -> {
                    AddOn addOn = addons.get(which);
                    if (isChecked) {
                        currentSelectedAddons.add(addOn);
                    } else {
                        currentSelectedAddons.remove(addOn);
                    }
                    if (isChecked) {
                        builder.setView(R.layout.dialog_addon_quantity); // Add the quantity selection view
                    } else {
                        builder.setView(null);
                    }
                })
                .setPositiveButton("Add to Cart", (dialog, which) -> {
                    // Set amount for selected addons
                    for (AddOn selectedAddOn : currentSelectedAddons) {
                        int quantity = seekBar.getProgress() + 1;
                        selectedAddOn.setAmount(quantity);
                    }
                    cartManager.addProductToCart(product); // הוסף מוצר לעגלה
                    notifyItemChanged(position); //עדכון הממשק הגרפי עבור המוצר הספציפי.
                })
                .setNegativeButton("Remove from Cart", (dialog, which) -> {
                    cartManager.removeProductFromCart(product); // הסרת המוצר מהעגלה
                    notifyItemChanged(position); //עדכון הממשק הגרפי עבור המוצר הספציפי.
                })
                .setNeutralButton("Cancel", null); //ב-AlertDialog, ברירת המחדל היא שכל לחיצה על כפתור (חיובי, שלילי, או נייטרלי) סוגרת את הדיאלוג אוטומטית

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quantityValue.setText(String.valueOf(progress + 1)); // להוסיף 1 כדי להתחיל מ-1 ולא מ-0
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        builder.setItems(getAddOnNamesWithPrices(addons), null);

        builder.show();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
    // פונקציה שמבצעת את מה שstream() היה עושה
    public String[] getAddOnNamesWithPrices(List<AddOn> addons) {
        List<String> addonNamesWithPrices = new ArrayList<>();
        for (AddOn addOn : addons) {
            addonNamesWithPrices.add(addOn.getAddOnName() + " - Price: " + addOn.getPricePerOneAmount());
        }
        return addonNamesWithPrices.toArray(new String[0]);
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
