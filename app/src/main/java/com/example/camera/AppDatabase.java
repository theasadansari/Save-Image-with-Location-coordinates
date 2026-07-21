package com.example.camera;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ImageRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract ImageDao imageDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "images_db"
                            )
                            // allowMainThreadQueries is used here for simplicity in this
                            // educational project. In a production app, queries should be
                            // performed on a background thread using LiveData or Coroutines.
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }
}
