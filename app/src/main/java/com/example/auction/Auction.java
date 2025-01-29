package com.example.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Auction {
    private String auctionItemId;
    private String title;
    private String description;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private Double startingPrice;
    private String category;
    private String address;
    private String userId;
    private List<String> imageUrls;
    private String status;
    private Double highestBid;
    private String highestBidder;
    private List<Map<String, Object>> bidders;

    // Required default constructor for Firebase deserialization
    public Auction() {
    }

    // Constructor to initialize highestBid with startingPrice
    public Auction(String title, String description, Double startingPrice, String category, String address, String startDate, String startTime, String endDate, String endTime, String userId, List<String> imageUrls, String status) {
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.highestBid = startingPrice; // Initialize highestBid to startingPrice
        this.category = category;
        this.address = address;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.userId = userId;
        this.imageUrls = imageUrls;
        this.status = status;
        this.bidders = new ArrayList<>(); // Initialize bidders list
    }

    public String getAuctionItemId() {
        return auctionItemId;
    }

    public void setAuctionItemId(String auctionItemId) {
        this.auctionItemId = auctionItemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(Double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(Double highestBid) {
        this.highestBid = highestBid;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public List<Map<String, Object>> getBidders() {
        return bidders;
    }

    public void setBidders(List<Map<String, Object>> bidders) {
        this.bidders = bidders;
    }
}