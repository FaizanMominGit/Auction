package com.example.auction;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    private RecyclerView liveRecyclerView, scheduledRecyclerView;
    private FirebaseFirestore db;
    private AuctionAdapter liveAuctionsAdapter, scheduledAuctionsAdapter;
    private ViewPager2 viewPager;
    private SliderAdapter sliderAdapter;
    private List<Object> sliderItems;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private static final long SLIDER_DELAY = 5000; // 5 seconds

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && sliderItems != null && sliderItems.size() > 0) {
                int currentItem = viewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % sliderItems.size();
                viewPager.setCurrentItem(nextItem, true);
            }
            sliderHandler.postDelayed(this, SLIDER_DELAY);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerViews for live and scheduled auctions
        liveRecyclerView = view.findViewById(R.id.liveRecyclerView);
        liveRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        liveAuctionsAdapter = new AuctionAdapter(getContext(), new ArrayList<>());
        liveRecyclerView.setAdapter(liveAuctionsAdapter);

        scheduledRecyclerView = view.findViewById(R.id.scheduledRecyclerView);
        scheduledRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        scheduledAuctionsAdapter = new AuctionAdapter(getContext(), new ArrayList<>());
        scheduledRecyclerView.setAdapter(scheduledAuctionsAdapter);

        // Initialize ViewPager2 for image slider
        viewPager = view.findViewById(R.id.viewPager);
        sliderItems = new ArrayList<>();
        sliderItems.add(R.drawable.img_1);
        sliderItems.add(R.drawable.img2);
        sliderItems.add(R.drawable.img3);
        sliderAdapter = new SliderAdapter(getContext(), sliderItems);
        viewPager.setAdapter(sliderAdapter);

        // Optimize ViewPager2 scrolling
        setupViewPager();

        fetchAndSetLiveAuctions();
        fetchAndSetScheduledAuctions();
        startSlider();

        return view;
    }

    private void setupViewPager() {
        viewPager.setOffscreenPageLimit(3);
        viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleY(scale);
        });

        viewPager.setPageTransformer(transformer);

        // Prevent rapid scrolling issues when swiping manually
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
            }
        });
    }

    private void fetchAndSetLiveAuctions() {
        db.collection("auctionItems")
                .whereEqualTo("status", "live")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Auction> auctions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Auction auction = document.toObject(Auction.class);
                            Log.d("Home", "Auction Title (Live): " + auction.getTitle());
                            auctions.add(auction);
                        }
                        liveAuctionsAdapter.setAuctions(auctions);
                    } else {
                        Log.e("Home", "Error fetching live auctions", task.getException());
                    }
                });
    }

    private void fetchAndSetScheduledAuctions() {
        db.collection("auctionItems")
                .whereEqualTo("status", "scheduled")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Auction> auctions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Auction auction = document.toObject(Auction.class);
                            Log.d("Home", "Auction Title (Scheduled): " + auction.getTitle());
                            auctions.add(auction);
                        }
                        scheduledAuctionsAdapter.setAuctions(auctions);
                    } else {
                        Log.e("Home", "Error fetching scheduled auctions", task.getException());
                    }
                });
    }

    private void startSlider() {
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
    }

    private void stopSlider() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        startSlider();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSlider();
    }
}
