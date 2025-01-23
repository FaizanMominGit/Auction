package com.example.auction;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
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

        String aboutContent = "About Us\n\n" +
                "Welcome to our Auctions App, a project developed as part of our diploma in Computer Engineering. " +
                "This app represents our dedication to learning and applying advanced software development techniques. " +
                "It has been created using Java, XML, and Firebase as part of our C++ project under the esteemed guidance of Ms. Hafsah Siddique.\n\n" +
                "Team Members:\n" +
                "1. Md Faizan Momin (2205690343)\n" +
                "2. Aasim Ansari (2205690314)\n" +
                "3. Salman Firfirey (2205690341)\n" +
                "4. Amaan Kazi (2205690352)\n" +
                "5. Saqlain Bashier (2205690342)\n\n" +
                "Our goal is to deliver a platform where users can explore and participate in auctions seamlessly, leveraging modern tools and technologies. " +
                "This project is a testament to our collaborative efforts and technical growth.\n\n" +
                "Thank you for supporting us on this journey!";

        // Create a SpannableString
        SpannableString spannableContent = new SpannableString(aboutContent);

        // Apply bold to "Ms."
        int msStartIndex = aboutContent.indexOf("Ms.");
        int msEndIndex = msStartIndex + 19;
        spannableContent.setSpan(new StyleSpan(Typeface.BOLD), msStartIndex, msEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set text to TextView
        aboutTextView.setText(spannableContent);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());

        return view;
    }
}