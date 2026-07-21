package com.example.camera;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ImageRecord record);

    @Query("SELECT * FROM images ORDER BY id DESC LIMIT 1")
    ImageRecord getLatestImage();

    @Query("SELECT * FROM images ORDER BY id DESC")
    List<ImageRecord> getAllImages();

    /**
     * Returns images whose GPS coordinates fall within {@code range} degrees of the target.
     * Using a 0.001-degree radius (~111 metres) as the default search radius.
     */
    @Query("SELECT * FROM images WHERE " +
           "ABS(CAST(latitude AS REAL) - :lat) <= :range AND " +
           "ABS(CAST(longitude AS REAL) - :lon) <= :range " +
           "ORDER BY id DESC")
    List<ImageRecord> getImagesByLocation(double lat, double lon, double range);
}
