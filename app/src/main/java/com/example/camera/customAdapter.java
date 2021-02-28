package com.example.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class customAdapter extends BaseAdapter {
    private final int layout;
    private final ArrayList<imageLists> recordList;
    private final Context context;

    public customAdapter(Context context, int layout, ArrayList<imageLists> recordList) {
        this.context = context;
        this.recordList = recordList;
        this.layout = layout;
    }

    public int getCount() {
        return recordList.size();
    }

    public Object getItem(int position) {
        return recordList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, layout, null);
        }


        ImageView imageView = convertView.findViewById(R.id.imageView);

        imageLists datalist = recordList.get(position);


        byte[] recordImage = datalist.getImg();
        Bitmap bitmap = BitmapFactory.decodeByteArray(recordImage, 0, recordImage.length);
        imageView.setImageBitmap(bitmap);

        return convertView;
    }


}
