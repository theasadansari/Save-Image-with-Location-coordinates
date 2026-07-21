package com.example.camera;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "images")
public class ImageRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "file_path")
    public String filePath;

    @ColumnInfo(name = "latitude")
    public String latitude;

    @ColumnInfo(name = "longitude")
    public String longitude;
}
