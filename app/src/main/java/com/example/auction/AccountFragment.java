package com.example.auction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    private ImageView profileImageView;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private String currentUserId;

    // ActivityResultLauncher for picking an image from the gallery
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                selectedImageUri = data.getData();
                                profileImageView.setImageURI(selectedImageUri);
                                uploadImageToFirebase();
                            }
                        }
                    });

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        TextView about = view.findViewById(R.id.About);
        TextView wallet = view.findViewById(R.id.Wallet);
        TextView details = view.findViewById(R.id.Details);
        profileImageView = view.findViewById(R.id.profile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // Load the current profile picture if available
        loadProfilePicture();

        // Set click listener for the profile image
        profileImageView.setOnClickListener(v -> showChangePictureDialog());

        details.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            startActivity(intent);
        });

        about.setOnClickListener(v -> {
            AboutFragment targetFragment = new AboutFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit();
        });

        wallet.setOnClickListener(view1 -> {
            WalletFragment targetFragment = new WalletFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit();
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();

            TextView Email_id = view.findViewById(R.id.Email);
            TextView User_Name = view.findViewById(R.id.User__Name);

            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            Email_id.setText(email);
                            User_Name.setText(name);
                        } else {
                            Log.d(TAG, "User document not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting user document", e);
                    });
        } else {
            Log.d(TAG, "User not logged in");
        }

        return view;
    }

    private void showChangePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Profile Picture");
        builder.setMessage("Do you want to change your profile picture?");
        builder.setPositiveButton("Yes", (dialog, which) -> openGallery());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(galleryIntent);
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            StorageReference imageRef = storageRef.child("profile_pictures/" + currentUserId + ".jpg");
            UploadTask uploadTask = imageRef.putFile(selectedImageUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserProfilePicture(uri);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    Toast.makeText(getContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image", e);
                Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateUserProfilePicture(Uri imageUri) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(imageUri)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            updateFirestoreProfilePicture(imageUri);
                        } else {
                            Log.e(TAG, "Error updating user profile", task.getException());
                            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateFirestoreProfilePicture(Uri imageUri) {
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.update("profilePictureUrl", imageUri.toString())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore profile picture updated.");
                    Toast.makeText(getContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating Firestore profile picture", e);
                    Toast.makeText(getContext(), "Failed to update Firestore profile picture", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProfilePicture() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.account) // Placeholder image
                    .error(R.drawable.account) // Error image
                    .into(profileImageView);
        } else {
            // Load default image if no profile picture is set
            profileImageView.setImageResource(R.drawable.account);
        }
    }
}