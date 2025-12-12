package com.bloodinventory.bloodinventory;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private TextInputEditText itemNameEditText;
    private MaterialButton saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        itemNameEditText = findViewById(R.id.itemNameEditText);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        String itemName = itemNameEditText.getText().toString().trim();

        if (itemName.isEmpty()) {
            itemNameEditText.setError(getString(R.string.required_field));
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> item = new HashMap<>();
        item.put("name", itemName);
        item.put("userId", user.getUid());
        item.put("createdAt", System.currentTimeMillis());

        db.collection("items")
                .add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddItemActivity.this, "تم إضافة الصنف بنجاح", Toast.LENGTH_SHORT).show();
                    itemNameEditText.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "فشل إضافة الصنف: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

