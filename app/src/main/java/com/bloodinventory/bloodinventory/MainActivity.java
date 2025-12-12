package com.bloodinventory.bloodinventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Schedule notification check on app start
        ExpirationNotificationService.scheduleNotificationCheck(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        if (currentUser.getDisplayName() != null) {
            welcomeTextView.setText("مرحباً " + currentUser.getDisplayName());
        } else {
            welcomeTextView.setText("مرحباً");
        }

        MaterialCardView addItemCard = findViewById(R.id.addItemCard);
        MaterialCardView addStockCard = findViewById(R.id.addStockCard);
        MaterialCardView dispenseStockCard = findViewById(R.id.dispenseStockCard);
        MaterialCardView returnStockCard = findViewById(R.id.returnStockCard);

        addItemCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        addStockCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddStockActivity.class);
            startActivity(intent);
        });

        dispenseStockCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DispenseStockActivity.class);
            startActivity(intent);
        });

        returnStockCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReturnStockActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

