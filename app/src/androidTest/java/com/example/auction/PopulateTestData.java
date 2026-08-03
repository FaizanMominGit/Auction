package com.example.auction;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class PopulateTestData {

    private static final String TAG = "PopulateTestData";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Test
    public void addTestProducts() throws InterruptedException, ExecutionException, TimeoutException {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "No user logged in. Please log in mdfaizanmomin12@gmail.com on the device first.");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Adding products for user: " + userId);

        List<Task<Void>> tasks = new ArrayList<>();

        // Add 5 Live products
        for (int i = 1; i <= 5; i++) {
            tasks.add(createProduct(userId, "live", i));
        }

        // Add 5 Scheduled products
        for (int i = 1; i <= 5; i++) {
            tasks.add(createProduct(userId, "scheduled", i));
        }

        // Add 5 Closed products
        for (int i = 1; i <= 5; i++) {
            tasks.add(createProduct(userId, "closed", i));
        }

        Tasks.await(Tasks.whenAll(tasks), 60, TimeUnit.SECONDS);
        Log.d(TAG, "All test products added successfully.");
    }

    private Task<Void> createProduct(String userId, String status, int index) {
        String auctionItemId = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();

        data.put("auctionItemId", auctionItemId);
        data.put("title", "Test " + status + " Item " + index);
        data.put("description", "This is a test description for " + status + " item number " + index + ". High quality product for auction.");
        data.put("startingPrice", 100.0 + (index * 10));
        data.put("category", "Electronics");
        data.put("address", "123 Test Street, Auction City");
        data.put("userId", userId);
        data.put("status", status);

        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://picsum.photos/seed/" + auctionItemId + "/800/600");
        data.put("imageUrls", imageUrls);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        if (status.equals("live")) {
            data.put("startDate", dateFormat.format(calendar.getTime()));
            data.put("startTime", timeFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            data.put("endDate", dateFormat.format(calendar.getTime()));
            data.put("endTime", timeFormat.format(calendar.getTime()));
        } else if (status.equals("scheduled")) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            data.put("startDate", dateFormat.format(calendar.getTime()));
            data.put("startTime", timeFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            data.put("endDate", dateFormat.format(calendar.getTime()));
            data.put("endTime", timeFormat.format(calendar.getTime()));
        } else if (status.equals("closed")) {
            calendar.add(Calendar.DAY_OF_MONTH, -10);
            data.put("startDate", dateFormat.format(calendar.getTime()));
            data.put("startTime", timeFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 5);
            data.put("endDate", dateFormat.format(calendar.getTime()));
            data.put("endTime", timeFormat.format(calendar.getTime()));
        }

        data.put("highestBid", 100.0 + (index * 10));

        return db.collection("auctionItems").document(auctionItemId).set(data);
    }
}
