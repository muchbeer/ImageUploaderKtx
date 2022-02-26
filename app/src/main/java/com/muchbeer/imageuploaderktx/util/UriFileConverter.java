package com.muchbeer.imageuploaderktx.util;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

public class UriFileConverter {
    public static File convertImageUriToFile(Uri imageUri, Activity activity) {
        Cursor cursor = null;//  w  w  w.  ja  va  2 s .  c o m
        try {
            String[] proj = { MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.ImageColumns.ORIENTATION };
            cursor = activity
                    .managedQuery(imageUri, proj, null, null, null);
            int file_ColumnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int orientation_ColumnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
            if (cursor.moveToFirst()) {
                String orientation = cursor
                        .getString(orientation_ColumnIndex);
                return new File(cursor.getString(file_ColumnIndex));
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
