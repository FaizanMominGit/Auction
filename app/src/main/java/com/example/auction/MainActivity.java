package com.example.auction;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imageView, home;
    FirebaseAuth auth;
    Button loginButton;
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color._131921)); // Set your color here
        }

        // Initialize views
        home = findViewById(R.id.imageView);
        imageView = findViewById(R.id.account);
        loginButton = findViewById(R.id.loginButton);
        searchEditText = findViewById(R.id.searchEditText);

        auth = FirebaseAuth.getInstance();

        // Check if user is logged in and update UI accordingly
        if (auth.getCurrentUser() == null) {
            imageView.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        }

        // Login button listener
        loginButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

        // Handle home button click to load the Home fragment
        home.setOnClickListener(view -> loadFragment(new Home()));

        // Bottom navigation setup
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);

        // Set initial fragment to Home if not restored
        if (savedInstanceState == null) {
            loadFragment(new Home());
        }

        // Bottom navigation item selection handling
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.item1) {
                loadFragment(new Home());
                return true;
            } else if (itemId == R.id.item2) {
                loadFragment(new BidsFragment());
                return true;
            } else if (itemId == R.id.item3) {
                loadFragment(new WalletFragment());
                return true;
            } else if (itemId == R.id.item4) {
                loadFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        // Account icon click listener to open account menu dialog
        imageView.setOnClickListener(view -> {
            AccountMenuDialogFragment dialogFragment = new AccountMenuDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "account_menu_dialog");
        });

        // Search EditText listener for "Enter" key press
        searchEditText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    // Trigger the navigation to the search fragment
                    navigateToSearchFragment(query);
                }

                // Optionally hide the keyboard after search
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                return true; // Indicating that the action has been handled
            }
            return false; // Default action handling if the action ID is not a search
        });


    }

    // Helper method to load fragments
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Method to navigate to SearchListFragment and pass the search query
    private void navigateToSearchFragment(String query) {
        SearchListFragment searchListFragment = new SearchListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("searchQuery", query);
        searchListFragment.setArguments(bundle);
        loadFragment(searchListFragment);
    }

    // Dialog fragment for account menu
    public static class AccountMenuDialogFragment extends DialogFragment {

        @NonNull
        @OptIn(markerClass = UnstableApi.class)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Dialog(requireActivity());
            Objects.requireNonNull(dialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.account_menu);

            TextView sell = dialog.findViewById(R.id.textView30);
            TextView logoutButton = dialog.findViewById(R.id.textView37);

            ImageView closeButton = dialog.findViewById(R.id.imageView9);
            closeButton.setOnClickListener(view -> dismiss());

            TextView Email_id = dialog.findViewById(R.id.Email_id);
            TextView User_Name = dialog.findViewById(R.id.User_Name);
            TextView ManageAccount = dialog.findViewById(R.id.Manage_Account);
            TextView about = dialog.findViewById(R.id.About);
            TextView my_bids = dialog.findViewById(R.id.my_bids);
            my_bids.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), MyBids.class));
                dismiss();
            });
            about.setOnClickListener(view -> {
                loadFragment(new AboutFragment());
                dismiss();
            });

            // Dismiss dialog when clicking outside
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            ManageAccount.setOnClickListener(view -> {
                loadFragment(new AccountFragment());
                dismiss();
            });

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String email = user.getEmail();
                String uid = user.getUid();

                FirebaseFirestore.getInstance().collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                Email_id.setText(email);
                                User_Name.setText(name);
                            } else {
                                Log.d("AccountMenuDialog", "User document not found");
                            }
                        })
                        .addOnFailureListener(e -> Log.e("AccountMenuDialog", "Error getting user document", e));
            } else {
                Log.d("AccountMenuDialog", "User not logged in");
            }

            sell.setOnClickListener(view -> {
                if (user != null) {
                    String uid = user.getUid();

                    FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String kycStatus = documentSnapshot.getString("kyc");
                                    if ("done".equals(kycStatus)) {
                                        loadFragment(new SellFragment());
                                        dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "Please complete your KYC first.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getContext(), DetailsActivity.class);
                                        startActivity(intent);
                                    }
                                } else {
                                    Log.d("AccountMenuDialog", "User document not found");
                                }
                            })
                            .addOnFailureListener(e -> Log.e("AccountMenuDialog", "Error getting user document", e));
                } else {
                    Log.d("AccountMenuDialog", "User not logged in");
                }
            });

            logoutButton.setOnClickListener(view -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                if (getActivity() != null) getActivity().finish();
                dismiss();
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            return dialog;
        }

        // Helper method to load fragments inside dialog
        private void loadFragment(Fragment fragment) {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
