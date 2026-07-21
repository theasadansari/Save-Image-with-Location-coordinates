package com.example.camera;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView selectedImage;
    private EditText etLatitude;
    private EditText etLongitude;
    private TextView tvCoordinates;

    private Uri currentPhotoUri;
    private String currentPhotoPath;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private AppDatabase database;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString("photo_path");
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        database = AppDatabase.getInstance(this);

        selectedImage = findViewById(R.id.imageViewWindow);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        Button searchBtn = findViewById(R.id.button);
        etLatitude = findViewById(R.id.editTextLatitude);
        etLongitude = findViewById(R.id.editTextLongitude);
        tvCoordinates = findViewById(R.id.coordinates);

        // Must be registered before the activity starts
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                this::onPhotoCaptured
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::onPermissionsResult
        );

        displayLatestImage();

        cameraBtn.setOnClickListener(v -> askPermissionsAndOpenCamera());

        searchBtn.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ImageListActivity.class);
            i.putExtra("lat", etLatitude.getText().toString().trim());
            i.putExtra("lon", etLongitude.getText().toString().trim());
            startActivity(i);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentPhotoPath != null) {
            outState.putString("photo_path", currentPhotoPath);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Permissions
    // ──────────────────────────────────────────────────────────────────────────

    private void askPermissionsAndOpenCamera() {
        boolean hasCameraPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasLocationPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasCameraPermission && hasLocationPermission) {
            openCamera();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        }
    }

    private void onPermissionsResult(Map<String, Boolean> results) {
        Boolean cameraGranted = results.getOrDefault(Manifest.permission.CAMERA, false);
        Boolean locationGranted = results.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        if (Boolean.TRUE.equals(cameraGranted) && Boolean.TRUE.equals(locationGranted)) {
            openCamera();
        } else {
            Toast.makeText(this,
                    "Camera and location permissions are required to capture images.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Camera
    // ──────────────────────────────────────────────────────────────────────────

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            currentPhotoUri = FileProvider.getUriForFile(
                    this, "com.example.android.fileprovider", photoFile);
            cameraLauncher.launch(currentPhotoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create image file.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("IMG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void onPhotoCaptured(boolean success) {
        if (!success || currentPhotoPath == null) {
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        if (bitmap != null) {
            selectedImage.setImageBitmap(bitmap);
        }

        final String photoPath = currentPhotoPath;
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationProviderClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnCompleteListener(task -> {
                    android.location.Location location = task.getResult();
                    if (location != null) {
                        String latitude = Double.toString(location.getLatitude());
                        String longitude = Double.toString(location.getLongitude());
                        tvCoordinates.setText(latitude + " * " + longitude);
                        Toast.makeText(this, latitude + "\n" + longitude, Toast.LENGTH_SHORT).show();
                        saveImageRecord(photoPath, latitude, longitude);
                    } else {
                        Toast.makeText(this, "Could not retrieve location.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Database
    // ──────────────────────────────────────────────────────────────────────────

    private void saveImageRecord(String filePath, String latitude, String longitude) {
        ImageRecord record = new ImageRecord();
        record.filePath = filePath;
        record.latitude = latitude;
        record.longitude = longitude;

        long result = database.imageDao().insert(record);
        if (result == -1) {
            Toast.makeText(this, "Error saving image.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Image saved successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayLatestImage() {
        ImageRecord latest = database.imageDao().getLatestImage();
        if (latest != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(latest.filePath);
            if (bitmap != null) {
                selectedImage.setImageBitmap(bitmap);
            }
            tvCoordinates.setText(latest.latitude + " * " + latest.longitude);
        }
    }
}
