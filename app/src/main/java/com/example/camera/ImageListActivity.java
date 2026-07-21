package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

public class ImageListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        AppDatabase database = AppDatabase.getInstance(this);

        String lat = getIntent().getStringExtra("lat");
        String lon = getIntent().getStringExtra("lon");

        List<ImageRecord> images;
        if (lat == null || lat.isEmpty() || lon == null || lon.isEmpty()) {
            images = database.imageDao().getAllImages();
        } else {
            try {
                double latVal = Double.parseDouble(lat);
                double lonVal = Double.parseDouble(lon);
                // Search within ~111 metres (0.001 degrees) of the entered coordinates
                images = database.imageDao().getImagesByLocation(latVal, lonVal, 0.001);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid coordinates entered.", Toast.LENGTH_SHORT).show();
                images = database.imageDao().getAllImages();
            }
        }

        if (images.isEmpty()) {
            Toast.makeText(this, "No images found.", Toast.LENGTH_SHORT).show();
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ImageAdapter(images));
    }
}
