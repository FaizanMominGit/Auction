package com.example.auction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imageView, home;
    FirebaseAuth auth;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        home = findViewById(R.id.imageView);
        imageView = findViewById(R.id.account);
        loginButton = findViewById(R.id.loginButton);

        auth = FirebaseAuth.getInstance();


        // Check if user is logged in and update UI accordingly
        if (auth.getCurrentUser() == null) {
            imageView.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }
        else {
            imageView.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        }
        loginButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

        // Handle home button to load Home
        home.setOnClickListener(view -> getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Home())
                .commit());

        // Bottom navigation setup
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);

        // Set initial fragment to Home if not restored
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Home())
                    .commit();
        }

        // Account icon click listener to open account menu dialog
        imageView.setOnClickListener(view -> {
            AccountMenuDialogFragment dialogFragment = new AccountMenuDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "account_menu_dialog");
        });

        // Bottom navigation item selection handling
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.item1) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Home())
                        .commit();
                return true;
            } else if (itemId == R.id.item2) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BidsFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.item3) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new WalletFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.item4) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AccountFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }

    // Dialog fragment for account menu
    public static class AccountMenuDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(requireActivity());
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.account_menu);
            TextView sell = dialog.findViewById(R.id.textView30);
            TextView logoutButton = dialog.findViewById(R.id.textView37);

            sell.setOnClickListener(view -> {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new SellFragment()); // Replace with SellFragment
                transaction.addToBackStack(null); // Optional: Add to back stack so you can navigate back
                transaction.commit(); // Commit the transaction
                dismiss();
            });

            logoutButton.setOnClickListener(view -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class)); // Use getActivity()
                if (getActivity() != null) {
                    getActivity().finish();
                }
                dismiss(); // Dismiss the dialog
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            return dialog;
        }
    }
}