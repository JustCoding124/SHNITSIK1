package com.example.shnitsik;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.shnitsik.models.AddOn;
import com.example.shnitsik.models.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.*;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.*;

public class EditInfoFragment extends Fragment {

    private Button btnSetOpeningHours, btnSaveProduct, btnDeleteProduct, btnAddAddon;
    // בתוך השורות הראשיות:
    private EditText inputProductId, inputProductName, inputPrice, inputCategory, inputDescription;
    private CheckBox checkboxRequiresFreshness;
    private ImageView productImageView;
    private EditText inputPrepTime;
    private TextView addonsSummary;

    private Uri selectedImageUri;
    private List<AddOn> addOnsList = new ArrayList<>();

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("product_images");

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_info, container, false);

        btnSetOpeningHours = view.findViewById(R.id.btn_set_opening_hours);
        btnSaveProduct = view.findViewById(R.id.btn_save_product);
        btnDeleteProduct = view.findViewById(R.id.btn_delete_product);
        btnAddAddon = view.findViewById(R.id.btn_add_addon);
        inputPrepTime = view.findViewById(R.id.input_prep_time);
        inputProductId = view.findViewById(R.id.input_product_id);
        inputProductName = view.findViewById(R.id.input_product_name);
        inputPrice = view.findViewById(R.id.input_price);
        inputCategory = view.findViewById(R.id.input_category);
        inputDescription = view.findViewById(R.id.input_description);
        checkboxRequiresFreshness = view.findViewById(R.id.checkbox_requires_freshness);
        productImageView = view.findViewById(R.id.product_image);
        addonsSummary = view.findViewById(R.id.addons_summary);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                productImageView.setImageURI(selectedImageUri);
            }
        });

        productImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSetOpeningHours.setOnClickListener(v -> showOpeningHoursDialog());
        btnAddAddon.setOnClickListener(v -> showAddOnDialog());
        btnSaveProduct.setOnClickListener(v -> saveOrUpdateProduct());
        btnDeleteProduct.setOnClickListener(v -> deleteProduct());

        return view;
    }

    private void showOpeningHoursDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set Opening Hours");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_opening_hours, null);

        Spinner daySpinner = dialogView.findViewById(R.id.daySpinner);
        TimePicker openingPicker = dialogView.findViewById(R.id.openingTimePicker);
        TimePicker closingPicker = dialogView.findViewById(R.id.closingTimePicker);
        CheckBox closedCheckbox = dialogView.findViewById(R.id.closedCheckbox);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"});
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        closedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            openingPicker.setEnabled(!isChecked);
            closingPicker.setEnabled(!isChecked);
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String day = daySpinner.getSelectedItem().toString();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Root/openingHours/").child(day);

            if (closedCheckbox.isChecked()) {
                ref.child("closed").setValue(true);
            } else {
                int openHour = openingPicker.getHour();
                int openMinute = openingPicker.getMinute();
                int closeHour = closingPicker.getHour();
                int closeMinute = closingPicker.getMinute();

                ref.child("openHour").setValue(openHour);
                ref.child("openMinute").setValue(openMinute);
                ref.child("closeHour").setValue(closeHour);
                ref.child("closeMinute").setValue(closeMinute);
                ref.child("closed").setValue(false);
            }
            Toast.makeText(getContext(), "Opening hours saved", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddOnDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Add-On");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText nameInput = new EditText(getContext());
        nameInput.setHint("Add-On Name");
        layout.addView(nameInput);

        EditText priceInput = new EditText(getContext());
        priceInput.setHint("Price Per One");
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(priceInput);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            if (!name.isEmpty() && !priceStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                addOnsList.add(new AddOn(name, price));
                updateAddOnSummary();
            } else {
                Toast.makeText(getContext(), "Name and price required", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateAddOnSummary() {
        if (addOnsList.isEmpty()) {
            addonsSummary.setText("No Add-Ons Yet");
        } else {
            StringBuilder sb = new StringBuilder();
            for (AddOn addOn : addOnsList) {
                sb.append(addOn.getAddOnName())
                        .append(" - ")
                        .append(addOn.getPricePerOneAmount())
                        .append("₪ per unit\n");
            }
            addonsSummary.setText(sb.toString().trim());
        }
    }

    private void saveOrUpdateProduct() {
        String id = inputProductId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(getContext(), "Product ID is required", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("products").document(id).get().addOnSuccessListener(doc -> {
            boolean exists = doc.exists();
            if (!exists && fieldsMissing()) {
                Toast.makeText(getContext(), "All fields are required to create a new product", Toast.LENGTH_LONG).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageAndSave(id, exists, doc);
            } else {
                saveProductData(id, exists, doc, null);
            }
        });
    }


    private boolean fieldsMissing() {
        return inputProductId.getText().toString().trim().isEmpty()
                || inputPrice.getText().toString().trim().isEmpty()
                || inputCategory.getText().toString().trim().isEmpty()
                || inputDescription.getText().toString().trim().isEmpty()
                || inputPrepTime.getText().toString().trim().isEmpty();
    }


    private void uploadImageAndSave(String productId, boolean exists, DocumentSnapshot existingDoc) {
        String filename = productId + "_" + System.currentTimeMillis();
        storageRef.child(filename).putFile(selectedImageUri)
                .addOnSuccessListener(task -> storageRef.child(filename).getDownloadUrl()
                        .addOnSuccessListener(uri -> saveProductData(productId, exists, existingDoc, uri.toString())));
    }


    private void saveProductData(String productId, boolean exists, DocumentSnapshot existingDoc, String imageUrl) {
        Map<String, Object> data = new HashMap<>();

        if (!inputProductId.getText().toString().trim().isEmpty())
            data.put("productId", productId);

        if (!inputProductName.getText().toString().trim().isEmpty())
            data.put("productName", inputProductName.getText().toString().trim());
        else if (exists)
            data.put("productName", existingDoc.getString("productName"));

        if (!inputPrice.getText().toString().trim().isEmpty())
            data.put("price", Double.parseDouble(inputPrice.getText().toString().trim()));
        else if (exists && existingDoc.contains("price"))
            data.put("price", existingDoc.getDouble("price"));

        if (!inputCategory.getText().toString().trim().isEmpty())
            data.put("category", inputCategory.getText().toString().trim());
        else if (exists)
            data.put("category", existingDoc.getString("category"));

        if (!inputDescription.getText().toString().trim().isEmpty())
            data.put("description", inputDescription.getText().toString().trim());
        else if (exists)
            data.put("description", existingDoc.getString("description"));

        if (!inputPrepTime.getText().toString().trim().isEmpty())
            data.put("prepTime", Long.parseLong(inputPrepTime.getText().toString().trim()));
        else if (exists && existingDoc.contains("prepTime"))
            data.put("prepTime", existingDoc.getLong("prepTime"));

        data.put("requiresFreshness", checkboxRequiresFreshness.isChecked());
        data.put("addOns", addOnsList);

        if (imageUrl != null) {
            data.put("imageUrl", imageUrl);
        } else if (exists) {
            data.put("imageUrl", existingDoc.getString("imageUrl"));
        }

        firestore.collection("products").document(productId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), exists ? "Product updated" : "Product created", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error saving product", Toast.LENGTH_SHORT).show());
    }


    private void deleteProduct() {
        String productId = inputProductId.getText().toString().trim();
        if (productId.isEmpty()) {
            Toast.makeText(getContext(), "Enter Product ID to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("products")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "Product with this ID not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            ref.delete(); // לא חייב להמתין – תמונה תימחק ברקע
                        }

                        firestore.collection("products").document(doc.getId()).delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error accessing Firestore", Toast.LENGTH_SHORT).show());
    }
    private void clearFields() {
        inputProductId.setText("");
        inputProductName.setText("");
        inputPrice.setText("");
        inputCategory.setText("");
        inputDescription.setText("");
        inputPrepTime.setText("");
        checkboxRequiresFreshness.setChecked(false);
        productImageView.setImageResource(R.drawable.ic_food_placeholder); // או תמונת ברירת מחדל שלך
        selectedImageUri = null;
        addOnsList.clear();
        addonsSummary.setText("No Add-Ons Yet");
    }


}
