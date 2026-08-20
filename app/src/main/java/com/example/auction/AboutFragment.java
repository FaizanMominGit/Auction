package com.example.auction;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        TextView aboutTextView = view.findViewById(R.id.about);

        String aboutContent = "About Auction App\n\n" +
                "Auction App is a feature-rich platform designed to provide a seamless and secure online auction experience. " +
                "It allows users to discover, participate in, and manage auctions with ease, leveraging real-time technologies for a responsive bidding environment.\n\n" +
                "Core Features:\n" +
                "• Live & Scheduled Auctions: Participate in real-time bidding or plan ahead for upcoming items.\n" +
                "• Secure Wallet System: Manage your digital currency (Radianite) for transparent transactions.\n" +
                "• Multi-Category Marketplace: Explore products across Electronics, Fashion, Home, and more.\n" +
                "• Real-time Notifications: Stay updated on bid status and auction results.\n\n" +
                "Technological Stack:\n" +
                "The application is built using Java and XML for a native Android experience, with a robust backend powered by Firebase, ensuring reliability and high performance.";

        aboutTextView.setText(aboutContent);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());

        return view;
    }
}