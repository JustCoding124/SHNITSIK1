package com.example.shnitsik;

import androidx.annotation.NonNull;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditInfoFragment extends Fragment {
    private static final int REQUEST_PERMISSION_READ_MEDIA_IMAGES = 100;
    private static final int PICK_IMAGE_REQUEST = 101;
    private Button btnDeleteProduct;
    private Button btnSetOpeningHours;
    private List<AddOn> currentAddOns = new ArrayList<>();
    private ImageView productImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private EditText inputProductId, inputProductName, inputPrice, inputStock, inputCategory, inputDescription;
    private CheckBox checkboxRequiresFreshness;
    private Button btnAddAddon;
    private Button btnSaveProduct;
    private TextView addonsSummary;
    private Bitmap selectedBitmap;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public static EditInfoFragment newInstance(String param1, String param2) {
        EditInfoFragment fragment = new EditInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_info, container, false);
        productImageView = view.findViewById(R.id.product_image);
        btnAddAddon = view.findViewById(R.id.btn_add_addon);
        addonsSummary = view.findViewById(R.id.addons_summary);
        inputProductId = view.findViewById(R.id.input_product_id);
        inputProductName = view.findViewById(R.id.input_product_name);
        inputPrice = view.findViewById(R.id.input_price);
        inputStock = view.findViewById(R.id.input_stock);
        inputCategory = view.findViewById(R.id.input_category);
        inputDescription = view.findViewById(R.id.input_description);
        checkboxRequiresFreshness = view.findViewById(R.id.checkbox_requires_freshness);
        btnSaveProduct = view.findViewById(R.id.btn_save_product);
        btnSetOpeningHours = view.findViewById(R.id.btn_set_opening_hours);
        btnSetOpeningHours.setOnClickListener(v -> showOpeningHoursDialog());

        /// "כשלוחצים על התמונה, תבדוק אם יש לי הרשאה לגשת לגלריה.
        /// אם אין לי – תבקש.
        /// אם יש לי – תפתח את הגלריה כדי לבחור תמונה."
        productImageView.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openImageChooser();
            }
        });

        btnSaveProduct.setOnClickListener(v -> {
            uploadImageToStorage(selectedImageUri, imageUrl -> {
                saveProductWithImageUrl(imageUrl);
            });
        });
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        productImageView.setImageURI(selectedImageUri);
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImageChooser();
                    } else {
                        Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        btnDeleteProduct = view.findViewById(R.id.btn_delete_product);
        btnDeleteProduct.setOnClickListener(v -> deleteProduct());
        btnAddAddon.setOnClickListener(v -> showAddOnDialog(addonsSummary));
        return view;
    }
    private void showAddOnDialog(TextView addonsSummary) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Add-On");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputName = new EditText(getContext());
        inputName.setHint("Add-On Name");
        layout.addView(inputName);

        final EditText inputPrice = new EditText(getContext());
        inputPrice.setHint("Price");
        inputPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputPrice);

        final EditText inputStock = new EditText(getContext());
        inputStock.setHint("Stock");
        inputStock.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputStock);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String priceStr = inputPrice.getText().toString().trim();
            String stockStr = inputStock.getText().toString().trim();
            if (!name.isEmpty() && !priceStr.isEmpty() && !stockStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                AddOn addOn = new AddOn(name, price, stock);
                currentAddOns.add(addOn);
                updateAddOnsSummary(addonsSummary);
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }
    private void updateAddOnsSummary(TextView summaryView) {
        StringBuilder sb = new StringBuilder();
        for (AddOn addOn : currentAddOns) {
            sb.append(addOn.getAddOnName())
                    .append(" - ₪").append(addOn.getPricePerOneAmount())
                    .append(" | Stock: ").append(addOn.getStock())
                    .append("\n");
        }
        summaryView.setText(sb.toString());
    }
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
    private void uploadImageToStorage(Uri imageUri, OnImageUploadCallback callback) {
        if (imageUri == null) {
            callback.onComplete(null); // לא נבחרה תמונה
            return;
        }

        String fileName = "product_images/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                callback.onComplete(uri.toString())
                        )
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    callback.onComplete(null);
                });
    }

    private void showOpeningHoursDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set Opening Hours");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_opening_hours, null);

        Spinner daySpinner = dialogView.findViewById(R.id.daySpinner);
        TimePicker openingPicker = dialogView.findViewById(R.id.openingTimePicker);
        TimePicker closingPicker = dialogView.findViewById(R.id.closingTimePicker);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"});
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String day = daySpinner.getSelectedItem().toString();
            int openHour = openingPicker.getHour();
            int closeHour = closingPicker.getHour();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Root").child("openingHours");
            ref.child(day).child("open").setValue(openHour);
            ref.child(day).child("close").setValue(closeHour);
            Toast.makeText(getContext(), "Opening hours saved for " + day, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void saveProductWithImageUrl(String imageUrl) {
        String productId = inputProductId.getText().toString().trim();
        String name = inputProductName.getText().toString().trim();
        String priceStr = inputPrice.getText().toString().trim();
        String stockStr = inputStock.getText().toString().trim();
        String category = inputCategory.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        boolean requiresFreshness = checkboxRequiresFreshness.isChecked();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Product name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .whereEqualTo("productName", name)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // מוצר חדש → יצירה
                        try {
                            double price = Double.parseDouble(priceStr);
                            int stock = Integer.parseInt(stockStr);
                            boolean available = true;

                            Product product = new Product(
                                    productId,
                                    requiresFreshness,
                                    name,
                                    price,
                                    stock,
                                    category,
                                    description,
                                    currentAddOns,
                                    available
                            );
                            product.setImageUrl(imageUrl); // כאן שומרים את ה־URL

                            db.collection("products")
                                    .document(productId)
                                    .set(product)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(getContext(), "Product saved successfully", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Error saving product", Toast.LENGTH_SHORT).show());

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // מוצר קיים → עדכון
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Product already exists")
                                .setMessage("A product with this name already exists.\nDo you want to update the filled-in fields?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    Map<String, Object> updates = new HashMap<>();
                                    if (!priceStr.isEmpty()) updates.put("price", Double.parseDouble(priceStr));
                                    if (!stockStr.isEmpty()) updates.put("stock", Integer.parseInt(stockStr));
                                    if (!category.isEmpty()) updates.put("category", category);
                                    if (!description.isEmpty()) updates.put("description", description);
                                    if (imageUrl != null) updates.put("imageBase64", imageUrl);
                                    updates.put("requiresFreshness", requiresFreshness);
                                    if (!currentAddOns.isEmpty())
                                        updates.put("addOns", currentAddOns);

                                    String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                    db.collection("products").document(docId)
                                            .update(updates)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Error updating product", Toast.LENGTH_SHORT).show());
                                })
                                .setNegativeButton("No", (dialog, which) ->
                                        Toast.makeText(getContext(), "Update cancelled", Toast.LENGTH_SHORT).show())
                                .show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error accessing database", Toast.LENGTH_SHORT).show());
    }


    private void deleteProduct() {
        String name = inputProductName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Enter product name to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .whereEqualTo("productName", name)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(getContext(), "No product found with that name", Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Delete Product")
                                .setMessage("Are you sure you want to delete \"" + name + "\"?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    String docId = query.getDocuments().get(0).getId();
                                    db.collection("products").document(docId)
                                            .delete()
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error accessing database", Toast.LENGTH_SHORT).show());
    }

    private interface OnImageUploadCallback {
        void onComplete(String imageUrl);
    }

}