package com.example.auction;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnDeleteClickListener onDeleteClickListener;

    // Interface for the delete button click listener
    public interface OnDeleteClickListener {
        void onDeleteClick(String userId);
    }


    public UserAdapter(Context context, List<User> userList, OnDeleteClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        if (position < 0 || position >= userList.size()) {
            Log.e("UserAdapter", "Invalid position: " + position);
            return;  // Prevent IndexOutOfBoundsException
        }

        User user = userList.get(position);

        // Check if user or user ID is null
        if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
            Log.e("UserAdapter", "Invalid user or user ID at position: " + position);
            return; // Skip binding this item if data is invalid
        }

        holder.nameTextView.setText(user.getName());
        holder.emailTextView.setText(user.getEmail());

        // Load profile picture using Glide
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.drawable.account) // Placeholder image
                    .error(R.drawable.account) // Error image
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.account);
        }

        // Delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (onDeleteClickListener != null) {
                            onDeleteClickListener.onDeleteClick(user.getUserId());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView emailTextView;
        ImageView deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.userProfileImageView);
            nameTextView = itemView.findViewById(R.id.userNameTextView);
            emailTextView = itemView.findViewById(R.id.userEmailTextView);
            deleteButton = itemView.findViewById(R.id.deleteUserButton);
        }
    }
}
