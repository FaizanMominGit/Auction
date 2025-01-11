package com.example.auction;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private Button addButton;
    private TextView countryTextView, convertedAmountTextView;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        // Initialize Firebase Firestore and get the current user
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        amountEditText = view.findViewById(R.id.editTextNumber);
        addButton = view.findViewById(R.id.button3);
        countryTextView = view.findViewById(R.id.textView46);
        convertedAmountTextView = view.findViewById(R.id.textView43);

        // Fetch and display country
        String country = getCountryFromTelephonyManager(getContext());
        if (country == null) {
            // Fallback to Geocoder if TelephonyManager is not available
            country = getCountryFromGeocoder(getContext());
        }

        countryTextView.setText("Country: " + country);
        saveUserCountryToFirestore(country);

        // Set up editor action listener to trigger currency conversion on "Done" or "Enter" key
        amountEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                String radianite = amountEditText.getText().toString();
                if (!radianite.isEmpty()) {
                    double amount = Double.parseDouble(radianite);
                    convertCurrency(amount);
                    return true; // Indicate that the action was handled
                }
            }
            return false;
        });

        return view;
    }

    // Method to fetch country from TelephonyManager
    private String getCountryFromTelephonyManager(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String countryCode = telephonyManager.getNetworkCountryIso();
            if (countryCode != null && !countryCode.isEmpty()) {
                // Convert country code to full country name
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

    // Method to save the user's country to Firebase Firestore
    private void saveUserCountryToFirestore(String country) {
        if (user != null) {
            String userId = user.getUid();
            // Create a map to store country information
            Map<String, Object> userCountryData = new HashMap<>();
            userCountryData.put("country", country);

            // Save to Firestore
            db.collection("users").document(userId)
                    .set(userCountryData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Country saved to Firestore");
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Firestore", "Error saving country: " + e.getMessage());
                    });
        } else {
            Log.d("Firestore", "User is not logged in.");
        }
    }

    // Method to convert the currency
    private void convertCurrency(double radianite) {
        String country = getCountryFromTelephonyManager(getContext());
        if (country == null) {
            // Fallback to Geocoder if TelephonyManager is not available
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
                    getActivity().runOnUiThread(() -> {
                        convertedAmountTextView.setText(String.format("%s %.2f", currencyCode, convertedAmount));
                    });

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
                return "AUD"; // Australian Dollar
            case "Bulgaria":
                return "BGN"; // Bulgarian Lev
            case "Brazil":
                return "BRL"; // Brazilian Real
            case "Canada":
                return "CAD"; // Canadian Dollar
            case "Switzerland":
                return "CHF"; // Swiss Franc
            case "China":
                return "CNY"; // Chinese Yuan
            case "Czech Republic":
                return "CZK"; // Czech Koruna
            case "Denmark":
                return "DKK"; // Danish Krone
            case "European Union":
                return "EUR"; // Euro
            case "United Kingdom":
                return "GBP"; // British Pound
            case "Hong Kong":
                return "HKD"; // Hong Kong Dollar
            case "Croatia":
                return "HRK"; // Croatian Kuna
            case "Hungary":
                return "HUF"; // Hungarian Forint
            case "Indonesia":
                return "IDR"; // Indonesian Rupiah
            case "India":
                return "INR"; // Indian Rupee
            case "Iceland":
                return "ISK"; // Icelandic Krona
            case "Japan":
                return "JPY"; // Japanese Yen
            case "South Korea":
                return "KRW"; // Korean Won
            case "Mexico":
                return "MXN"; // Mexican Peso
            case "Malaysia":
                return "MYR"; // Malaysian Ringgit
            case "Norway":
                return "NOK"; // Norwegian Krone
            case "New Zealand":
                return "NZD"; // New Zealand Dollar
            case "Philippines":
                return "PHP"; // Philippine Peso
            case "Poland":
                return "PLN"; // Polish Zloty
            case "Romania":
                return "RON"; // Romanian Leu
            case "Russia":
                return "RUB"; // Russian Ruble
            case "Sweden":
                return "SEK"; // Swedish Krona
            case "Singapore":
                return "SGD"; // Singapore Dollar
            case "Thailand":
                return "THB"; // Thai Baht
            case "Turkey":
                return "TRY"; // Turkish Lira
            case "United States":
                return "USD"; // US Dollar
            case "South Africa":
                return "ZAR"; // South African Rand
            default:
                return null; // Return null if currency code is not available for the country
        }
    }
}
