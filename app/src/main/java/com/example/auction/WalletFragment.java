package com.example.auction;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WalletFragment extends Fragment {

    private EditText amountEditText;
    private TextView convertedAmountTextView;
    private TextView utilAmountTextView;
    private TextView avaAmountTextView;
    private TextView totalAmountTextView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        TextView bids = view.findViewById(R.id.Bids);
        bids.setOnClickListener(view1 -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new BidsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        amountEditText = view.findViewById(R.id.editTextNumber);
        Button addButton = view.findViewById(R.id.button3);
        TextView countryTextView = view.findViewById(R.id.textView46);
        convertedAmountTextView = view.findViewById(R.id.textView43);
        utilAmountTextView = view.findViewById(R.id.Utilisedamount);
        avaAmountTextView = view.findViewById(R.id.Availablebalance);
        totalAmountTextView = view.findViewById(R.id.Totalbalance);

        // Fetch and display country
        String country = getCountryFromTelephonyManager(getContext());
        if (country == null) {
            // Fallback to Geocoder if TelephonyManager is not available
            country = getCountryFromGeocoder(getContext());
        }

        countryTextView.setText("Country: " + country);

        // Set up editor action listener to trigger currency conversion on "Done" or "Enter" key
        amountEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String radianite = amountEditText.getText().toString();
                if (!radianite.isEmpty()) {
                    double amount = Double.parseDouble(radianite);
                    convertCurrency(amount);
                    hideKeyboard(v); // Hide keyboard after conversion
                    return true; // Indicate that the action was handled
                }
            }
            return false;
        });
        addButton.setOnClickListener(v -> {
            String radianite = amountEditText.getText().toString();
            if (!radianite.isEmpty()) {
                double amount = Double.parseDouble(radianite);
                storeAmountInFirebase(amount);
            } else {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            }
        });
        fetchWalletData();
        return view;
    }

    // Method to fetch country from TelephonyManager
    private String getCountryFromTelephonyManager(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String countryCode = telephonyManager.getNetworkCountryIso();
            if (countryCode != null && !countryCode.isEmpty()) {
                Locale locale = new Locale("", countryCode.toUpperCase());
                return locale.getDisplayCountry();
            }
        }
        return null; // Return null if TelephonyManager fails
    }

    // Method to fetch country from Geocoder (Location-based)
    private String getCountryFromGeocoder(Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(0, 0, 1); // Current location (use actual lat/lng in real app)
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown"; // Default value in case of error
    }

    // Method to convert the currency
    private void convertCurrency(double radianite) {
        String country = getCountryFromTelephonyManager(getContext());
        if (country == null) {
            country = getCountryFromGeocoder(getContext());
        }

        String currencyCode = getCurrencyCodeFromCountry(country);
        if (currencyCode != null && !currencyCode.equals("USD")) {
            // Using Frankfurter API (Free, no key required)
            String url = "https://api.frankfurter.app/latest?from=USD&to=" + currencyCode;

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONObject rates = response.getJSONObject("rates");
                            double conversionRate = rates.getDouble(currencyCode);
                            double convertedAmount = radianite * conversionRate;

                            // Update UI with the converted amount
                            convertedAmountTextView.setText(String.format(Locale.getDefault(), "%s %.2f", currencyCode, convertedAmount));
                        } catch (JSONException e) {
                            Log.e("Currency Conversion", "JSON error", e);
                            Toast.makeText(getContext(), "Conversion error", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("Currency Conversion", "Volley error", error);
                        Toast.makeText(getContext(), "Failed to fetch rates", Toast.LENGTH_SHORT).show();
                    });

            queue.add(jsonObjectRequest);
        } else if ("USD".equals(currencyCode)) {
            convertedAmountTextView.setText(String.format(Locale.getDefault(), "USD %.2f", radianite));
        }
    }

    // Modern method to get the currency code using Java Locale and Currency APIs
    private String getCurrencyCodeFromCountry(String countryName) {
        if (countryName == null || countryName.isEmpty() || countryName.equals("Unknown")) {
            return "USD"; // Default fallback
        }

        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry().equalsIgnoreCase(countryName)) {
                try {
                    return Currency.getInstance(locale).getCurrencyCode();
                } catch (Exception e) {
                    return "USD";
                }
            }
        }
        return "USD"; // Default fallback
    }

    // Method to hide keyboard after input
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void storeAmountInFirebase(double amount) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double currentTotalBalance = documentSnapshot.getDouble("totalBalance");
                    if (currentTotalBalance == null) currentTotalBalance = 0.0;

                    double newTotalBalance = currentTotalBalance + amount;
                    Map<String, Object> walletData = new HashMap<>();
                    walletData.put("totalBalance", newTotalBalance);

                    userRef.update(walletData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Amount added to wallet", Toast.LENGTH_SHORT).show();
                                fetchWalletData();  // Refresh UI
                                amountEditText.setText("");

                                // Play success animation
                                LottieAnimationView successAnim = getView().findViewById(R.id.successAnimation);
                                successAnim.setVisibility(View.VISIBLE);
                                successAnim.playAnimation();

                                // Hide animation after 2 seconds
                                new Handler().postDelayed(() -> successAnim.setVisibility(View.GONE), 2000);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to add amount", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Error adding amount to Firestore", e);
                            });
                }
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Error fetching wallet data", e);
            });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }



    private void fetchWalletData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double totalBalance = documentSnapshot.getDouble("totalBalance");
                    Double utilisedBalance = documentSnapshot.getDouble("utilisedBalance");

                    if (totalBalance == null) totalBalance = 0.0;
                    if (utilisedBalance == null) utilisedBalance = 0.0;

                    // Compute available balance dynamically
                    double availableBalance = totalBalance - utilisedBalance;

                    totalAmountTextView.setText(String.valueOf(totalBalance));
                    utilAmountTextView.setText(String.valueOf(utilisedBalance));
                    avaAmountTextView.setText(String.valueOf(availableBalance));
                } else {
                    totalAmountTextView.setText("0.0");
                    utilAmountTextView.setText("0.0");
                    avaAmountTextView.setText("0.0");
                }
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Error fetching wallet data", e);
            });
        }
    }

}