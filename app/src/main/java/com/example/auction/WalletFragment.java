package com.example.auction;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

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
        if (currencyCode != null) {
            String url = "https://api.freecurrencyapi.com/v1/latest?apikey=fca_live_pufydwMbfP0UFTXKrPy29cUoM85wq5DtoWWR6wZx&base_currency=USD";
            new Thread(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject rates = jsonResponse.getJSONObject("data");

                    double conversionRate = rates.getDouble(currencyCode);
                    double convertedAmount = radianite * conversionRate;

                    // Update UI with the converted amount
                    getActivity().runOnUiThread(() -> convertedAmountTextView.setText(String.format("%s %.2f", currencyCode, convertedAmount)));

                } catch (Exception e) {
                    Log.e("Currency Conversion", "Error fetching conversion rate", e);
                }
            }).start();
        }
    }

    // Method to get the currency code from the country name
    private String getCurrencyCodeFromCountry(String country) {
        switch (country) {
            case "Australia":
                return "AUD";
            case "Bulgaria":
                return "BGN";
            case "Brazil":
                return "BRL";
            case "Canada":
                return "CAD";
            case "Switzerland":
                return "CHF";
            case "China":
                return "CNY";
            case "Czech Republic":
                return "CZK";
            case "Denmark":
                return "DKK";
            case "European Union":
                return "EUR";
            case "United Kingdom":
                return "GBP";
            case "Hong Kong":
                return "HKD";
            case "Croatia":
                return "HRK";
            case "Hungary":
                return "HUF";
            case "Indonesia":
                return "IDR";
            case "India":
                return "INR";
            case "Iceland":
                return "ISK";
            case "Japan":
                return "JPY";
            case "South Korea":
                return "KRW";
            case "Mexico":
                return "MXN";
            case "Malaysia":
                return "MYR";
            case "Norway":
                return "NOK";
            case "New Zealand":
                return "NZD";
            case "Philippines":
                return "PHP";
            case "Poland":
                return "PLN";
            case "Romania":
                return "RON";
            case "Russia":
                return "RUB";
            case "Sweden":
                return "SEK";
            case "Singapore":
                return "SGD";
            case "Thailand":
                return "THB";
            case "Turkey":
                return "TRY";
            case "United States":
                return "USD";
            case "South Africa":
                return "ZAR";
            default:
                return null;
        }
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
                    // Document exists, update the existing fields
                    Double currentTotalBalance = documentSnapshot.getDouble("totalBalance");
                    Double currentUtilisedBalance = documentSnapshot.getDouble("utilisedBalance");

                    if (currentTotalBalance == null) {
                        currentTotalBalance = 0.0;
                    }
                    if (currentUtilisedBalance == null) {
                        currentUtilisedBalance = 0.0;
                    }

                    double newTotalBalance = currentTotalBalance + amount;
                    double newAvailableBalance = newTotalBalance - currentUtilisedBalance;

                    Map<String, Object> walletData = new HashMap<>();
                    walletData.put("totalBalance", newTotalBalance);
                    walletData.put("availableBalance", newAvailableBalance);

                    userRef.update(walletData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Amount added to wallet", Toast.LENGTH_SHORT).show();
                                fetchWalletData();
                                amountEditText.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to add amount", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Error adding amount to Firestore", e);
                            });
                } else {
                    // Document does not exist, create a new one with utilisedBalance set to null
                    Map<String, Object> newUser = new HashMap<>();
                    newUser.put("totalBalance", amount);
                    newUser.put("availableBalance", amount);
                    newUser.put("utilisedBalance", null); // Set utilisedBalance to null

                    userRef.set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "New user created", Toast.LENGTH_SHORT).show();
                                fetchWalletData();
                                amountEditText.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to create new user", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Error creating new user", e);
                            });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to add amount", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error adding amount to Firestore", e);
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

                    if (totalBalance == null) {
                        totalBalance = 0.0;
                    }
                    if (utilisedBalance == null) {
                        utilisedBalance = 0.0;
                    }

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