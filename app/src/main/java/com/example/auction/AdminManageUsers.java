package com.example.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminManageUsers extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_manage_users);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, this::addUserToDeletionList);
        usersRecyclerView.setAdapter(userAdapter);

        checkAndCreateUsersToDeleteCollection();
        fetchUsers();
    }

    private void checkAndCreateUsersToDeleteCollection() {
        db.collection("users_to_delete").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                try {
                    db.collection("users_to_delete").document("dummy").set(new HashMap<String, Object>())
                            .addOnSuccessListener(aVoid -> {
                                db.collection("users_to_delete").document("dummy").delete();
                                Log.d("Firestore", "users_to_delete collection created.");
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error creating collection", e));
                } catch (Exception e) {
                    Log.e("Firestore", "Error creating collection", e);
                }
            } else {
                Log.d("Firestore", "users_to_delete collection already exists.");
            }
        });
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                User user = document.toObject(User.class);
                                String userId = document.getId();
                                user.setUserId(userId);
                                userList.add(user);
                                Log.d("AdminManageUsers", "User added: " + user.getName() + ", ID: " + userId);
                            } catch (Exception e) {
                                Log.e("AdminManageUsers", "Error converting document to User object", e);
                            }
                        }
                        userAdapter.notifyDataSetChanged();

                        if (userList.isEmpty()) {
                            Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("AdminManageUsers", "Error getting documents.", task.getException());
                        Toast.makeText(this, "Error fetching users", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }

    public void addUserToDeletionList(String userId) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users_to_delete")
                .document(userId)
                .set(new AdminManageUsers.UserToDelete(userId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminManageUsers", "User ID added to users_to_delete: " + userId);
                    Toast.makeText(this, "User deletion requested", Toast.LENGTH_SHORT).show();

                    db.collection("users").document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d("AdminManageUsers", "User document deleted from users collection: " + userId);
                                Toast.makeText(this, "User removed from database", Toast.LENGTH_SHORT).show();
                                fetchUsers();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AdminManageUsers", "Error deleting user document: " + userId, e);
                                Toast.makeText(this, "Failed to delete user from database", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminManageUsers", "Error adding user ID to users_to_delete: " + userId, e);
                    Toast.makeText(this, "Failed to request user deletion", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    public static class UserToDelete {
        private String userId;

        public UserToDelete() {}

        public UserToDelete(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
