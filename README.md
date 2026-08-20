# Auction Portal Project

[![Java](https://img.shields.io/badge/Java-96.5%25-orange?style=flat-square)](#tech-stack)
[![Python](https://img.shields.io/badge/Python-3.5%25-blue?style=flat-square)](#tech-stack)
[![Android API](https://img.shields.io/badge/Android%20API-28%2B-green?style=flat-square)](#requirements)

An innovative online auction portal built as an Android application with real-time bidding capabilities, automated auction management, and Firebase backend integration.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Automated Auction Closing System](#automated-auction-closing-system)
- [Firebase Setup](#firebase-setup)
- [Building & Running](#building--running)
- [Troubleshooting](#troubleshooting)
- [APK Download](#apk-download)
- [Contributing](#contributing)

## Overview

Auction Portal is a comprehensive mobile platform that enables users to create, manage, and participate in online auctions. The application features real-time bidding, secure user authentication, and an automated system that closes expired auctions and manages user wallets.

**Note:** The backend server is currently not operational. To fully utilize features like auction scheduling and automatic auction closing, you'll need to set up the Cloud Run infrastructure as described in the [Automated Auction Closing System](#automated-auction-closing-system) section.

## Features

### User Management
- **User Registration & Authentication**: Secure sign-up and login via Firebase Authentication
- **User Profiles**: Create and manage personal auction profiles
- **Wallet System**: Balance management with real-time credit tracking

### Auction Management
- **Create Auctions**: List items with title, description, starting price, and end date/time
- **Live Auctions**: View all active auctions in real-time
- **Search & Filter**: Discover auctions by category and keywords
- **Auction History**: Track completed auctions and bidding records

### Bidding System
- **Real-Time Bidding**: Place bids instantly with live bid updates
- **Bid Verification**: Automatic validation ensuring bids exceed current highest bid
- **Bidding History**: View complete bidding timeline for each auction
- **Winner Notification**: Automatic winner determination and notification

### Automated Features
- **Automatic Auction Closing**: Scheduled system closes expired auctions every 15 minutes
- **Payment Settlement**: Automatic deduction from winner's balance and credit to seller's balance
- **User Cleanup**: Automated deletion of flagged user accounts with cascade deletion

### Additional Features
- **Real-Time Updates**: Firestore integration for instant data synchronization
- **Animations**: Smooth UI transitions powered by Lottie
- **Image Support**: Firebase Storage integration for item images
- **Maps Integration**: Location-based auction features
- **Push Notifications**: Firebase Cloud Messaging support

## Tech Stack

### Frontend
- **Language**: Java (96.5%)
- **Framework**: Android SDK (API 28+)
- **Build System**: Gradle
- **SDK Version**: Compile SDK 35

### Backend & Database
- **Authentication**: Firebase Authentication
- **Database**: Cloud Firestore
- **Storage**: Firebase Cloud Storage
- **Functions**: Firebase Cloud Functions
- **Analytics**: Firebase Analytics
- **Messaging**: Firebase Cloud Messaging

### Backend Automation
- **Language**: Python (3.5%)
- **Infrastructure**: Google Cloud Run
- **Scheduler**: Google Cloud Scheduler
- **Database Client**: Firebase Admin SDK

### Libraries & Dependencies
- **UI Components**: Material Design, AndroidX
- **Image Loading**: Glide 4.16.0
- **Authentication UI**: FirebaseUI Auth 8.0.2
- **Animations**: Lottie 6.1.0
- **Networking**: OkHttp 4.12.0, Volley
- **Database ORM**: Android Room 2.6.1
- **Phone Validation**: libphonenumber 8.13.53
- **Navigation**: AndroidX Navigation Fragment
- **Maps**: Google Play Services Maps

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Android Mobile App (Java)                     │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  UI Layer: Activities, Fragments, Adapters                │ │
│  │  - Authentication, Auctions, Bidding, User Profile         │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Business Logic Layer                                      │ │
│  │  - Auction Management, Bidding Logic, User Management      │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Firebase SDK Integration                                  │ │
│  │  - Authentication, Firestore, Storage, Functions           │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│               Firebase Backend Services                          │
│  ├─ Authentication (User credentials)                           │
│  ├─ Cloud Firestore (Real-time data)                            │
│  ├─ Cloud Storage (Images & files)                              │
│  ├─ Cloud Functions (Triggers)                                  │
│  └─ Cloud Messaging (Push notifications)                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│     Cloud Automation: Auction Closing System (Python)            │
│  ┌─────────────────────┐          ┌──────────────────────────┐  │
│  │ Cloud Scheduler     │────────▶ │ Cloud Run Job            │  │
│  │ (Every 15 minutes)  │          │ close-auctions           │  │
│  └─────────────────────┘          └──────────────────────────┘  │
│                                            │                     │
│                                            ▼                     │
│                                  close_auctions.py               │
│                                  ├─ Close expired auctions       │
│                                  ├─ Update user balances         │
│                                  └─ Delete flagged users         │
│                                            │                     │
└────────────────────────────────────────────┼─────────────────────┘
                                             │
                                             ▼
                                   Cloud Firestore Updates
```

## Requirements

### For Development
- Android Studio (latest stable version)
- Java Development Kit (JDK 8+)
- Gradle 8.0+
- Android SDK API 35 (for compilation)
- Android SDK API 28+ (for runtime)

### For Deployment
- Google Cloud Platform (GCP) account
- Firebase project
- Firebase service account credentials
- Python 3.8+ (for Cloud Run function)

### Device Requirements
- Android device or emulator with API level 28 or higher
- Minimum 2GB RAM
- Internet connection for real-time features

## Getting Started

### Prerequisites

1. Clone the repository:
```bash
git clone https://github.com/FaizanMominGit/Auction.git
cd Auction
```

2. Set up Firebase:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Download your Firebase configuration file (`google-services.json`)
   - Place it in the `app/` directory

3. Install Android Studio and necessary SDKs

### Building the Project

1. Open the project in Android Studio
2. Sync Gradle files:
   ```bash
   ./gradlew sync
   ```

3. Build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

4. Build the release APK:
   ```bash
   ./gradlew assembleRelease
   ```

### Running the Application

**Using Android Studio:**
- Click the "Run" button or press `Shift + F10`
- Select your target device/emulator

**Using Command Line:**
```bash
./gradlew installDebug
adb shell am start -n com.example.auction/.MainActivity
```

### First Time Setup

1. **Launch the app** - You'll see the login/registration screen
2. **Create an account** - Sign up with your email and password
3. **Verify email** - Check your email for verification link
4. **Set up profile** - Complete your user profile
5. **Add funds** - Top up your wallet balance to start bidding
6. **Start exploring** - Browse auctions and place bids

## Project Structure

```
Auction/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/auction/
│   │   │   │   ├── Activities/          # UI Activities (Login, Home, Auction, Bidding)
│   │   │   │   ├── Adapters/            # RecyclerView Adapters
│   │   │   │   ├── Fragments/           # UI Fragments
│   │   │   │   ├── Models/              # Data models (User, Auction, Bid)
│   │   │   │   └── Utils/               # Utility classes & helpers
│   │   │   ├── res/
│   │   │   │   ├── layout/              # XML layout files
│   │   │   │   ├── drawable/            # Images & vectors
│   │   │   │   ├── values/              # Strings, colors, dimens
│   │   │   │   └── raw/                 # Lottie animations
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                        # Unit tests
│   │   └── androidTest/                 # Instrumented tests
│   └── build.gradle                     # App-level build configuration
├── gradle/
│   └── wrapper/                         # Gradle wrapper files
├── close_auctions.py                    # Cloud Run automation script
├── build.gradle                         # Project-level build configuration
├── settings.gradle                      # Project settings
├── gradle.properties                    # Gradle properties
├── auction-6a02c-firebase-adminsdk-*.json  # Firebase credentials
└── README.md                            # This file
```

## Automated Auction Closing System

The project includes an automated Python script that runs on Google Cloud Run to handle time-critical operations:

### How It Works

The system uses three components:

1. **Cloud Scheduler**: Triggers the job every 15 minutes
2. **Cloud Run**: Executes the Python function
3. **close_auctions.py**: Performs the actual operations

### What It Does

#### 1. Close Expired Auctions
- Queries Firestore for all `live` status auctions
- Compares current IST time with auction end date/time
- Updates expired auctions to `closed` status
- Records the final bid amount

#### 2. Settle Payments
For each closed auction with bids:
- **Winner**: Deducts the winning bid amount from their balance
- **Seller**: Credits the winning bid amount to their balance
- **Fallback**: If winner/seller account doesn't exist, credits/debits fallback account

#### 3. Delete Flagged Users
- Checks `users_to_delete` collection
- Deletes user from Firebase Authentication
- Removes user document from Firestore
- Cleans up the deletion flag

## Modernized Currency Conversion

I have updated the currency conversion logic in the **Wallet** screen to use a more reliable and modern technique.

### Changes Made

#### 1. Key-less API (Frankfurter)
Replaced the potentially expired `freecurrencyapi.com` with the **Frankfurter API** (`frankfurter.app`).
- **Benefit**: No API key required, open-source, and highly reliable for general currency conversion from USD.

#### 2. Modern Networking with Volley
Migrated from manual `Thread` + `HttpURLConnection` to **Volley**.
- **Benefit**: Better request management, automatic UI thread handling for responses, and more robust error handling.

#### 3. Dynamic Currency Detection
Replaced the long, manual `switch` statement with a dynamic lookup using `java.util.Currency` and `java.util.Locale`.
- **Benefit**: Automatically supports almost every country in the world without needing to manually add case statements.

### Security Check
All operations include authorization verification - they only proceed if the `server.isServer` flag is `true` in Firestore.

### Deployment Steps

1. **Prepare Python Environment**:
```bash
pip install firebase-admin pytz
```

2. **Create Cloud Run Service**:
```bash
gcloud run deploy close-auctions \
  --source . \
  --runtime python311 \
  --no-allow-unauthenticated \
  --entry-point close_expired_auctions
```

3. **Set Up Cloud Scheduler**:
```bash
gcloud scheduler jobs create pubsub close-auctions-trigger \
  --location=us-central1 \
  --schedule="*/15 * * * *" \
  --topic=close-auctions \
  --message-body='{"test":"data"}'
```

4. **Place Firebase credentials** (`auction-6a02c-firebase-adminsdk-fbsvc-4cc5dd13d3.json`) in your Cloud Run service container

### Environment Variables

Ensure these are set in Cloud Run:
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to Firebase service account JSON
- `TZ`: Should be set to Asia/Kolkata for correct time comparisons

## Firebase Setup

### Required Firestore Collections

The application expects the following Firestore structure:

```
┌─ auctionItems (collection)
│  ├─ (document)
│  │  ├─ title: string
│  │  ├─ description: string
│  │  ├─ startingPrice: number
│  │  ├─ highestBid: number
│  │  ├─ highestBidder: string (user ID)
│  │  ├─ userId: string (seller ID)
│  │  ├─ status: string ("live" or "closed")
│  │  ├─ endDate: string (DD/MM/YYYY)
│  │  ├─ endTime: string (HH:MM AM/PM)
│  │  └─ createdAt: timestamp
│
├─ users (collection)
│  ├─ (user ID)
│  │  ├─ email: string
│  │  ├─ displayName: string
│  │  ├─ totalBalance: number
│  │  ├─ utilisedBalance: number
│  │  ├─ rating: number
│  │  └─ createdAt: timestamp
│
├─ bids (collection)
│  ├─ (document)
│  │  ├─ auctionId: string
│  │  ├─ bidderId: string
│  │  ├─ amount: number
│  │  ├─ timestamp: timestamp
│  │  └─ status: string
│
└─ users_to_delete (collection)
   ├─ (document)
   │  ├─ userId: string
   │  └─ requestedAt: timestamp

```

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Auction items - readable by all, writable by owner
    match /auctionItems/{document=**} {
      allow read: if true;
      allow create: if request.auth.uid != null;
      allow update: if resource.data.userId == request.auth.uid;
      allow delete: if resource.data.userId == request.auth.uid;
    }
    
    // Users - readable by all, writable by self
    match /users/{userId} {
      allow read: if true;
      allow write: if request.auth.uid == userId;
    }
    
    // Bids - readable by all, writable by authenticated
    match /bids/{document=**} {
      allow read: if true;
      allow create: if request.auth.uid != null;
      allow update: if resource.data.bidderId == request.auth.uid;
      allow delete: if resource.data.bidderId == request.auth.uid;
    }
  }
}
```

## Building & Running

### Development Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run app directly
./gradlew runDebug
```

### Production Build

```bash
# Build release APK (requires keystore configuration)
./gradlew assembleRelease

# Build AAB (Android App Bundle) for Play Store
./gradlew bundleRelease
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests com.example.auction.AuctionActivityTest
```

## Troubleshooting

### Common Issues

**Issue: "Google services not available" error**
- Solution: Ensure `google-services.json` is placed in the `app/` directory
- Verify your Firebase project is properly configured
- Check that your device/emulator has Google Play Services installed

**Issue: Auctions not closing automatically**
- Solution: Cloud Run job may not be deployed or Cloud Scheduler may be paused
- Check Cloud Run deployment status and logs
- Verify the `server.isServer` flag is set to `true` in Firestore
- Ensure the Python script has proper Firebase credentials

**Issue: Firebase Authentication failures**
- Solution: Check Firebase project settings and authentication providers
- Verify email verification is enabled if required
- Check that the app package name matches Firebase configuration

**Issue: Gradle sync fails**
- Solution: Update Android Studio and SDK tools
- Clear Gradle cache: `./gradlew clean`
- Delete `.gradle` folder and resync

**Issue: Build fails with "Duplicate class" error**
- Solution: Check for dependency conflicts in `app/build.gradle`
- Update all Firebase dependencies to latest compatible versions
- Run `./gradlew dependencies` to analyze dependency tree

### Logging

Enable Firebase Debug Logging:
```java
FirebaseFirestore.setLoggingEnabled(true);
```

Check logs in Android Studio's Logcat:
- Filter by tag: `Auction`
- Monitor network calls: `OkHttp`

## APK Download

Pre-built APK files are available at:
[Google Drive Folder](https://drive.google.com/drive/folders/1JBk5t1OgNeQG4cXGFDMBDbSymANxRaRQ?usp=drive_link)

Download the latest release APK to install directly on your Android device without building from source.

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java code style conventions
- Add unit tests for new features
- Update documentation as needed
- Test on multiple Android API levels (28+)
- Ensure Firebase security rules are updated for new collections

## Project Statistics

- **Total Code**: ~2,443 KB
- **Primary Language**: Java (96.5%)
- **Automation**: Python (3.5%)
- **Minimum SDK**: API 28 (Android 9.0)
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: API 35 (Android 15)
- **Build System**: Gradle with AndroidX

## License

This project is open source. Check the LICENSE file for details.

## Support

For questions or issues:
- Open an issue on GitHub
- Check existing issues for solutions
- Review Firebase documentation for backend issues
- Refer to Android documentation for platform-specific questions

---

**Last Updated**: 2026-08-20
**Maintained by**: [FaizanMominGit](https://github.com/FaizanMominGit)
