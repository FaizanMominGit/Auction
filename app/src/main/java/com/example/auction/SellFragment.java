package com.example.auction;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SellFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> selectedImageUris = new ArrayList<>();

    public SellFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        view.findViewById(R.id.chooseImageButton).setOnClickListener(v -> {
            // Check and request permissions
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
        TextView StartTime = view.findViewById(R.id.StartDateButton);
        TextView StartTimeDisplay = view.findViewById(R.id.StartTimeDisplay);
        final Calendar c = Calendar.getInstance();
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MINUTE);

        StartTime.setOnClickListener(v -> new TimePickerDialog(getActivity(), (view1, hourOfDay, minute) -> {
            if (minute % 15 != 0) {
                minute = Math.round(minute / 15.0f) * 15;
                view1.setCurrentMinute(minute);
            }

            // Check if the date is today
            Calendar selectedDateTime = Calendar.getInstance();
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);

            if (selectedDateTime.before(c)) {
                // Show an error message or handle the invalid time
                Toast.makeText(getContext(), "Please select a future time", Toast.LENGTH_SHORT).show();
            } else {
                // Format the time in 12-hour format
                String amPm = (hourOfDay < 12 || hourOfDay == 24) ? "AM" : "PM";
                int displayHour = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;
                String selectedTime = String.format("%02d:%02d %s", displayHour, minute, amPm);

                StartTime.setText("Start Time: " + selectedTime);
                StartTimeDisplay.setText(selectedTime);
            }
        }, currentHour, currentMinute, false).show()); // Set is24HourView to false
        TextView StartDateButton = view.findViewById(R.id.StartDateButton);

        StartDateButton.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view12, year1, monthOfYear, dayOfMonth) -> {
                        // Check if the selected date is in the past
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year1);
                        selectedDate.set(Calendar.MONTH, monthOfYear);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        if (selectedDate.before(calendar)) {
                            // Show an error message or handle the invalid date
                            Toast.makeText(getContext(), "Please select a future date", Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle the selected date
                            String selectedDateString = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                            StartDateButton.setText("Start Date: " + selectedDateString);
                            // You might want to update another TextView to display the date
                        }
                    },
                    year, month, day);

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });

        return view;
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                if (count > 10) {
                    Toast.makeText(getContext(), "Please select at most 10 images", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedImageUris.clear(); // Clear previous selections
                for (int i = 0; i < count; i++) {Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                    // Handle image URI (e.g., display in an ImageView)
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUris.clear(); // Clear previous selections
                selectedImageUris.add(imageUri);
                // Handle image URI
            }
            // You can now access the selected images using selectedImageUris
        }
    }
}