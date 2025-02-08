package com.example.auction;

public class User {
    private String userId;
    private String name;
    private String email;
    private String profilePictureUrl;

    // Required empty constructor for Firestore
    public User() {}

    public User(String userId, String name, String email, String profilePictureUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}