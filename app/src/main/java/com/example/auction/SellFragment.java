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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SellFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> selectedImagePaths = new ArrayList<>();
    private RecyclerView selectedImagesRecyclerView;
    private EditText startTimeButton, startDateDisplay, endDateDisplay, endTimeButton;

    public SellFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell, container, false);selectedImagesRecyclerView = view.findViewById(R.id.selectedImagesRecyclerView);
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

        endDateDisplay.setEnabled(false); // Disable end date EditText initially

        final Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MINUTE);

        startTimeButton.setOnClickListener(v -> new TimePickerDialog(getActivity(), (view1, hourOfDay, minute) -> {
            if (minute % 15 != 0) {
                minute = Math.round(minute / 15.0f) * 15;
                view1.setCurrentMinute(minute);
            }

            String amPm = (hourOfDay < 12 || hourOfDay == 24) ? "AM" : "PM";
            int displayHour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;
            String selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm);
            startTimeButton.setText(selectedTime);

        }, currentHour, currentMinute, false).show());

        startDateDisplay.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view12, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDateString = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                        startDateDisplay.setText(selectedDateString);

                        // Enable end date EditText after start date selection
                        endDateDisplay.setEnabled(true);
                    },
                    year, month, day);

            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        endDateDisplay.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view12, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDateString = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                        endDateDisplay.setText(selectedDateString);
                    },
                    year, month, day);

            // Set minimum date for end date DatePickerDialog
            String startDateString = startDateDisplay.getText().toString();
            if (!startDateString.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date startDate = dateFormat.parse(startDateString);
                    datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                    // Handle parsing error, e.g., show a Toast message
                    Toast.makeText(getContext(), "Invalid start date format", Toast.LENGTH_SHORT).show();
                }
            }

            datePickerDialog.show();
        });

        endTimeButton.setOnClickListener(v -> new TimePickerDialog(getActivity(), (view1, hourOfDay, minute) -> {
            if (minute % 15 != 0) {
                minute = Math.round(minute / 15.0f) * 15;
                view1.setCurrentMinute(minute);
            }

            String amPm = (hourOfDay < 12 || hourOfDay == 24) ? "AM" : "PM";
            int displayHour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;
            String selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm);
            endTimeButton.setText(selectedTime);

        }, currentHour, currentMinute, false).show());


        // Submit button click listener
        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> onSubmitButtonClicked());

        return view;
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

    private void openGallery() {Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getLocalPathFromUri(Uri uri) {
        String path = null;
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);File file = new File(requireContext().getCacheDir(), "image_" + System.currentTimeMillis() + ".jpg");
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

    private void onSubmitButtonClicked() {
        for (String imagePath : selectedImagePaths) {
            // ... (Your logic to handle the images, e.g., upload to server) ...
        }
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