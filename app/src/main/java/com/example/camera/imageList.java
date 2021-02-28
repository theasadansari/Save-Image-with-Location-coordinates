package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class imageList extends AppCompatActivity {
//    MainActivity object = new MainActivity();
//    ArrayList<imageLists> imagelist1 = new ArrayList<>();
    String lat;
    String lon;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);
        db=openOrCreateDatabase("images.db", Context.MODE_PRIVATE, null);
        db.execSQL("Create Table IF NOT EXISTS Images(ID INTEGER PRIMARY KEY AUTOINCREMENT, name BLOB, latitude TEXT, longitude Text)");


        ListView listView = findViewById(R.id.listView);
        ArrayList<imageLists> imagelist = new ArrayList<>();
        lat=getIntent().getStringExtra("lat");
        lon = getIntent().getStringExtra("lon");
        Toast.makeText(this,lat +" * "+lon,Toast.LENGTH_SHORT).show();
        Cursor cursor;
        if(lat.length()==0 || lon.length()==0){
            cursor = db.rawQuery(" SELECT * FROM Images",null);
        }
        else{
            cursor = db.rawQuery(" SELECT * FROM Images WHERE latitude = ? AND longitude = ?", new String[]{lat, lon}, null);
        }
        //cursor = db.rawQuery(" SELECT * FROM Images WHERE latitude = ? AND longitude = ?", new String[]{lat, lon}, null);
//        Cursor cursor = db.rawQuery(" SELECT * FROM Images",null);
        imagelist.clear();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                byte[] image = cursor.getBlob(cursor.getColumnIndex("name"));

                imagelist.add(new imageLists(image));
            }
        }else{
            Toast.makeText(this,"there is no image in the list",Toast.LENGTH_SHORT);
        }



        customAdapter cust = new customAdapter(this,R.layout.viewcontentlayout, imagelist);


        listView.setAdapter(cust);



    }
    public ArrayList<imageLists> getli() {


        //imageLists list = null;
        ArrayList<imageLists> imagelist = new ArrayList<>();
        Cursor cursor = db.rawQuery(" SELECT * FROM Images WHERE latitude = ? AND longitude = ?", new String[]{lat, lon}, null);
        cursor = db.rawQuery(" SELECT * FROM Images",null);
        imagelist.clear();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                byte[] image = cursor.getBlob(cursor.getColumnIndex("name"));

                imagelist.add(new imageLists(image));
            }
            return imagelist;
        }else{
            Toast.makeText(this,"there is no image in the list",Toast.LENGTH_SHORT);
        }

        return imagelist;
    }
}
