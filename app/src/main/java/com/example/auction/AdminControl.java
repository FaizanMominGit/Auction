package com.example.auction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminControl extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_control);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button manageUsersButton = findViewById(R.id.users);
        Button manageAuctionsButton = findViewById(R.id.Auctions);
        manageUsersButton.setOnClickListener(view -> startActivity(new Intent(AdminControl.this, AdminUsersControl.class)));
        manageAuctionsButton.setOnClickListener(view -> startActivity(new Intent(AdminControl.this, AdminAuctionsControl.class)));

    }
}