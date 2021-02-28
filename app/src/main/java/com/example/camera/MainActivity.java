package com.example.camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    ImageView selectedImage;
    Button cameraBtn;
    Button searchBtn;
    String currentPhotoPath;
    TextView ForCoordinates;

    TextView sLatitude;
    TextView sLongitude;


    private String latitude,longitude;
    CancellationToken cancellationToken;
    private FusedLocationProviderClient fusedLocationProviderClient;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Getting IDs
        selectedImage = findViewById(R.id.imageViewWindow);
        cameraBtn = findViewById(R.id.cameraBtn);
        searchBtn = findViewById(R.id.button);
        sLatitude = findViewById(R.id.editTextLatitude);
        sLongitude = findViewById(R.id.editTextLongitude);

        ForCoordinates = (TextView) findViewById(R.id.coordinates);

        db=openOrCreateDatabase("images.db", Context.MODE_PRIVATE, null);
        db.execSQL("Create Table IF NOT EXISTS Images(ID INTEGER PRIMARY KEY AUTOINCREMENT, name BLOB, latitude TEXT, longitude Text)");

        Bitmap imageFromDatabase = getRecentlyAddedImage();
        if(imageFromDatabase!=null){
            selectedImage.setImageBitmap(imageFromDatabase);
        }
        String temp = getRecentlyAddedImageLatitude();
        String temp1 = getRecentlyAddedImageLongitude();
        if(temp!=null){
            ForCoordinates.setText(temp+" * "+temp1);
        }


        //Opening Cameara and override the function
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting Camera Permissions

                askCameraPermission();

            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, imageList.class);
                i.putExtra("lat", sLatitude.getText().toString());
                i.putExtra("lon",sLongitude.getText().toString());

                startActivity(i);
            }
        });




    }

    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {

            openCamera();

        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");
                selectedImage.setImageBitmap(photo);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken).addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location=task.getResult();

                        if(location != null){
                            latitude =Double.toString(location.getLatitude());
                            longitude = Double.toString(location.getLongitude());
                            Toast.makeText(MainActivity.this,latitude + "\n" + longitude, Toast.LENGTH_SHORT).show();
                            ForCoordinates.setText(latitude+" * "+longitude);

                            ContentValues contentValues = new ContentValues();
                            contentValues.put("name",byteArray);
                            contentValues.put("latitude",latitude);
                            contentValues.put("longitude",longitude);

                            long result = db.insert("Images",null, contentValues);
                            if(result==-1){
                                Toast.makeText(getApplicationContext(),"Error in inserting.",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(getApplicationContext(),"Image added to Database successfully", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        }

    }

    public Bitmap getRecentlyAddedImage() {
        Cursor cursor = db.rawQuery(" SELECT * FROM Images", null, null);
        if (cursor != null && cursor.moveToLast()) {
            byte[] imgByte = cursor.getBlob(cursor.getColumnIndex("name"));
            cursor.close();
            return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return null;
    }

    public String getRecentlyAddedImageLongitude() {
        Cursor cursor = db.rawQuery(" SELECT * FROM Images", null, null);
        if (cursor != null && cursor.moveToLast()) {
            String longi = cursor.getString(cursor.getColumnIndex("longitude"));

            cursor.close();
            return longi;
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return null;
    }
    public String getRecentlyAddedImageLatitude() {
        Cursor cursor = db.rawQuery(" SELECT * FROM Images", null, null);
        if (cursor != null && cursor.moveToLast()) {
            String lati = cursor.getString(cursor.getColumnIndex("latitude"));

            cursor.close();
            return lati;
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode==CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED){
                openCamera();
                Toast.makeText(this,currentPhotoPath,Toast.LENGTH_SHORT);

            }else{
                Toast.makeText(this, "Camera Permission is required to Use Camera",Toast.LENGTH_SHORT).show();
            }
        }
    }
//    public ArrayList<imageLists> getli(){
//        String sslatitude = sLatitude.getText().toString();
//        String sslongitude = sLongitude.getText().toString();
//
//        imageLists list = null;
//        ArrayList<imageLists> imagelist = new ArrayList<>();
//        Cursor cursor = db.rawQuery(" SELECT * FROM Images WHERE latitude = ? AND longitude = ?", new String[]{sslatitude, sslongitude},null);
//        cursor.moveToFirst();
//        while(!cursor.isAfterLast()){
//            list = new imageLists(cursor.getBlob(1));
//            imagelist.add(list);
//
//        }
//        return imagelist;
//    }
}