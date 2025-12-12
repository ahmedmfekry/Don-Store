package com.bloodinventory.bloodinventory;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddStockActivity extends AppCompatActivity {

    private TextInputEditText dateEditText;
    private AutoCompleteTextView itemNameAutoComplete;
    private TextInputEditText quantityEditText;
    private TextInputEditText unitEditText;
    private TextInputEditText lotNumberEditText;
    private TextInputEditText expireDateEditText;
    private TextInputEditText notesEditText;
    private MaterialButton saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Calendar dateCalendar;
    private Calendar expireDateCalendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dateCalendar = Calendar.getInstance();
        expireDateCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dateEditText = findViewById(R.id.dateEditText);
        itemNameAutoComplete = findViewById(R.id.itemNameAutoComplete);
        quantityEditText = findViewById(R.id.quantityEditText);
        unitEditText = findViewById(R.id.unitEditText);
        lotNumberEditText = findViewById(R.id.lotNumberEditText);
        expireDateEditText = findViewById(R.id.expireDateEditText);
        notesEditText = findViewById(R.id.notesEditText);
        saveButton = findViewById(R.id.saveButton);

        dateEditText.setText(dateFormat.format(dateCalendar.getTime()));
        dateEditText.setOnClickListener(v -> showDatePicker(dateCalendar, dateEditText));

        expireDateEditText.setOnClickListener(v -> showDatePicker(expireDateCalendar, expireDateEditText));

        loadItems();

        saveButton.setOnClickListener(v -> saveStock());
    }

    private void loadItems() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("items")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> items = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String itemName = document.getString("name");
                            if (itemName != null) {
                                items.add(itemName);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_dropdown_item_1line, items);
                        itemNameAutoComplete.setAdapter(adapter);
                    }
                });
    }

    private void showDatePicker(Calendar calendar, TextInputEditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveStock() {
        String date = dateEditText.getText().toString().trim();
        String itemName = itemNameAutoComplete.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String unit = unitEditText.getText().toString().trim();
        String lotNumber = lotNumberEditText.getText().toString().trim();
        String expireDate = expireDateEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        if (date.isEmpty() || itemName.isEmpty() || quantity.isEmpty() || unit.isEmpty() ||
                lotNumber.isEmpty() || expireDate.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع الحقول المطلوبة", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> stock = new HashMap<>();
        stock.put("date", date);
        stock.put("itemName", itemName);
        stock.put("quantity", Integer.parseInt(quantity));
        stock.put("unit", unit);
        stock.put("lotNumber", lotNumber);
        stock.put("expireDate", expireDate);
        stock.put("notes", notes);
        stock.put("userId", user.getUid());
        stock.put("createdAt", System.currentTimeMillis());
        stock.put("type", "add");

        db.collection("stock")
                .add(stock)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddStockActivity.this, "تم إضافة المخزون بنجاح", Toast.LENGTH_SHORT).show();
                    clearFields();
                    // Schedule notification check
                    ExpirationNotificationService.scheduleNotificationCheck(this);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddStockActivity.this, "فشل إضافة المخزون: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        quantityEditText.setText("");
        unitEditText.setText("");
        lotNumberEditText.setText("");
        notesEditText.setText("");
        dateEditText.setText(dateFormat.format(dateCalendar.getTime()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

