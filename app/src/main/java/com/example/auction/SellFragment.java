package com.example.auction;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SellFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> selectedImagePaths = new ArrayList<>();
    private List<String> downloadUrls = new ArrayList<>(); // List to store image URLs
    private RecyclerView selectedImagesRecyclerView;
    private EditText startTimeButton, startDateDisplay, endDateDisplay, endTimeButton, itemTitle, itemDescription, startingPrice, fullAddress;
    private Spinner categorySpinner4;
    private RadioGroup radioGroup;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("auction_images");
    private String status;

    public SellFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        selectedImagesRecyclerView = view.findViewById(R.id.selectedImagesRecyclerView);
        view.findViewById(R.id.chooseImageButton).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PICK_IMAGE_REQUEST);
                } else {
                    openGallery();
                }
            } else {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
                } else {
                    openGallery();
                }
            }
        });

        startTimeButton = view.findViewById(R.id.startTextTime);
        startDateDisplay = view.findViewById(R.id.startTextDate);
        endDateDisplay = view.findViewById(R.id.endTextDate2);
        endTimeButton = view.findViewById(R.id.endTextTime2);
        itemTitle = view.findViewById(R.id.itemTitle);
        itemDescription = view.findViewById(R.id.itemDescription);
        startingPrice = view.findViewById(R.id.startingPrice);
        categorySpinner4 = view.findViewById(R.id.AuctionItem);
        fullAddress = view.findViewById(R.id.Fulladdress);
        endDateDisplay.setEnabled(false); // Disable end date EditText initially
        radioGroup = view.findViewById(R.id.radioGroup);
        final Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MINUTE);
        startTimeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    (view1, hourOfDay, minute) -> {
                        // Round to nearest 15 minutes
                        minute = Math.round(minute / 15.0f) * 15;

                        // Handle 60 minutes correctly
                        if (minute == 60) {
                            minute = 0;
                            hourOfDay++;
                        }

                        // Set the correct minute in the TimePickerDialog
                        view1.setCurrentMinute(minute);

                        String amPm = (hourOfDay < 12 || hourOfDay == 24) ? "AM" : "PM";
                        int displayHour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;
                        String selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm);
                        startTimeButton.setText(selectedTime);
                    },
                    currentHour,
                    currentMinute,
                    false);
            timePickerDialog.show();
        });
        startDateDisplay.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Initialize datePickerDialog here
            DatePickerDialog startDateDialog = new DatePickerDialog(getActivity(),
                    (view12, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDateString = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                        startDateDisplay.setText(selectedDateString);

                        // Enable end date EditText
                        endDateDisplay.setEnabled(true);
                    },
                    year, month, day);

            startDateDialog.getDatePicker().setMinDate(System.currentTimeMillis()); // Prevent selecting past dates
            startDateDialog.show();
        });

        endDateDisplay.setOnClickListener(v -> {
            DatePickerDialog endDateDialog = getDatePickerDialog();

            // Get the selected start date
            String startDateString = startDateDisplay.getText().toString();

            // Set minimum date for end date picker only if start date is valid
            if (!startDateString.isEmpty()) {
                try {
                    Calendar minEndDate = getCalendar(startDateString);

                    endDateDialog.getDatePicker().setMinDate(minEndDate.getTimeInMillis());
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Invalid start date format", Toast.LENGTH_SHORT).show();
                }
            }

            endDateDialog.show();
        });
        endTimeButton.setOnClickListener(v -> new TimePickerDialog(getActivity(), (view1, hourOfDay, minute) -> {
            // Round to the nearest 15 minutes
            minute = Math.round(minute / 15.0f) * 15;

            // Check if the rounded minute is 60 and adjust the hour accordingly
            if (minute == 60) {
                minute = 0;
                hourOfDay++;
                // Handle the case where hour exceeds 23 (to wrap back to 0)
                if (hourOfDay == 24) {
                    hourOfDay = 0;
                }
            }

            String amPm = (hourOfDay < 12) ? "AM" : "PM";
            int displayHour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;
            String selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm);
            endTimeButton.setText(selectedTime);

        }, currentHour, currentMinute, false).show());


        // Submit button click listener
        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> onSubmitButtonClicked());
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.liveRadioButton) {
                status = "live";
            } else if (checkedId == R.id.scheduledRadioButton) {
                status = "scheduled";
            }
        });

        // Set up the Spinner
        String[] categories = {"Electronics", "Clothing", "Home", "Furniture", "Books", "Sports", "Other"}; // Example categories
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner4.setAdapter(adapter);

        return view;
    }

    @NonNull
    private Calendar getCalendar(String startDateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date startDate = dateFormat.parse(startDateString);
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        // Set minimum date for end date as the day after start date
        Calendar minEndDate = Calendar.getInstance();
        minEndDate.setTime(startDate);
        minEndDate.add(Calendar.DAY_OF_MONTH, 1);
        return minEndDate;
    }

    @NonNull
    private DatePickerDialog getDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog endDateDialog = new DatePickerDialog(getActivity(),
                (view12, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDateString = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    endDateDisplay.setText(selectedDateString);
                },
                year, month, day);
        return endDateDialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUris.clear();
            selectedImagePaths.clear();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                    selectedImagePaths.add(getLocalPathFromUri(imageUri));
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUris.add(imageUri);
                selectedImagePaths.add(getLocalPathFromUri(imageUri));
            }

            selectedImagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            SelectedImagesAdapter adapter = new SelectedImagesAdapter(getContext(), selectedImageUris);
            selectedImagesRecyclerView.setAdapter(adapter);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getLocalPathFromUri(Uri uri) {
        String path = null;
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = new File(requireContext().getCacheDir(), "image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            inputStream.close();
            path = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show();
        }
        return path;
    }

    private void uploadImagesToStorage(List<String> imagePaths, String itemId) {
        // Create a list of tasks to upload each image
        List<Task<UploadTask.TaskSnapshot>> uploadTasks = new ArrayList<>();
        for (String path : imagePaths) {
            File file = new File(path);
            StorageReference imageRef = storageRef.child(itemId + "/" + UUID.randomUUID().toString() + ".jpg"); // Unique filename
            UploadTask uploadTask = imageRef.putFile(Uri.fromFile(file));
            uploadTasks.add(uploadTask);
        }
        Task<Void> allTasks = Tasks.whenAll(uploadTasks.toArray(new Task[0]));

        allTasks.addOnSuccessListener(aVoid -> {
            // All upload tasks completed successfully
            for (Task<UploadTask.TaskSnapshot> task : uploadTasks) {
                if (task.isSuccessful()) {
                    // Get download URL for successful uploads
                    Task<Uri> downloadUrlTask = task.getResult().getStorage().getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(uri -> {
                        downloadUrls.add(uri.toString());
                        if (downloadUrls.size() == imagePaths.size()) {
                            // All download URLs obtained
                            saveAuctionItemToFirestore();
                        }
                    }).addOnFailureListener(e -> {
                        // Handle download URL error
                    });
                } else {
                    // Handle individual upload failures
                    Log.e("UploadError", "Image upload failed: " + task.getException().getMessage());
                }
            }
        }).addOnFailureListener(e -> {
            // Handle overall failure (e.g., network issues)
            Log.e("UploadError", "Overall upload failure: " + e.getMessage());
        });
    }

    private void saveAuctionItemToFirestore() {
        // Use a fixed document ID for the auction item
        String auctionItemId = UUID.randomUUID().toString(); // Generate a unique ID for the auction item
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("auctionItemId", auctionItemId); // Add the auction item ID to the data
        data.put("title", itemTitle.getText().toString());
        data.put("description", itemDescription.getText().toString());
        data.put("startDate", startDateDisplay.getText().toString());
        data.put("startTime", startTimeButton.getText().toString());
        data.put("endDate", endDateDisplay.getText().toString());
        data.put("endTime", endTimeButton.getText().toString());
        data.put("startingPrice", Double.parseDouble(startingPrice.getText().toString()));
        data.put("category", categorySpinner4.getSelectedItem().toString());
        data.put("address", fullAddress.getText().toString());
        data.put("userId", userId);
        data.put("imageUrls", downloadUrls);
        // Add the status to the data
        data.put("status", status);

        db.collection("auctionItems").document(auctionItemId) // Use the auction item ID as the document ID
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Item added successfully!", Toast.LENGTH_SHORT).show();
                    // Clear input fields
                    itemTitle.setText("");
                    itemDescription.setText("");
                    startDateDisplay.setText("");
                    startTimeButton.setText("");
                    endDateDisplay.setText("");
                    endTimeButton.setText("");
                    startingPrice.setText("");
                    fullAddress.setText("");
                    selectedImageUris.clear();
                    selectedImagePaths.clear();
                    downloadUrls.clear();
                    selectedImagesRecyclerView.getAdapter().notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add item.", Toast.LENGTH_SHORT).show();
                });
    }

    private void onSubmitButtonClicked() {
        // Get values from input fields
        String title = itemTitle.getText().toString();
        String description = itemDescription.getText().toString();
        String startDateString = startDateDisplay.getText().toString();
        String startTimeString = startTimeButton.getText().toString();
        String endDateString = endDateDisplay.getText().toString();
        String endTimeString = endTimeButton.getText().toString();
        String startingPriceStr = startingPrice.getText().toString();
        String category = categorySpinner4.getSelectedItem().toString();
        String address = fullAddress.getText().toString();

        // Validate input fields
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Please enter item title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (category.isEmpty()) {
            Toast.makeText(getContext(), "Please select category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Please enter item description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startDateString.isEmpty()) {
            Toast.makeText(getContext(), "Please select start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startTimeString.isEmpty()) {
            Toast.makeText(getContext(), "Please select start time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDateString.isEmpty()) {
            Toast.makeText(getContext(), "Please select end date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endTimeString.isEmpty()) {
            Toast.makeText(getContext(), "Please select end time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startingPriceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter starting price", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.isEmpty()) {
            Toast.makeText(getContext(), "Please enter full address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (status == null || status.isEmpty()) {
            Toast.makeText(getContext(), "Please select auction status", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse start and end date/time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        Date startDate = null;
        Date startTime = null;
        Date endDate = null;
        Date endTime = null;
        try {
            startDate = dateFormat.parse(startDateString);
            startTime = timeFormat.parse(startTimeString);
            endDate = dateFormat.parse(endDateString);
            endTime = timeFormat.parse(endTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid date/time format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload images to Firebase Storage
        uploadImagesToStorage(selectedImagePaths, UUID.randomUUID().toString());
    }

    private class SelectedImagesAdapter extends RecyclerView.Adapter<SelectedImagesAdapter.ViewHolder> {

        private List<Uri> imageUris;
        private Context context;

        public SelectedImagesAdapter(Context context, List<Uri> imageUris) {
            this.context = context;
            this.imageUris = imageUris;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_selected_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Uri imageUri = imageUris.get(position);
            Glide.with(context).load(imageUri).into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUris.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.selectedImageView);
            }
        }
    }
}