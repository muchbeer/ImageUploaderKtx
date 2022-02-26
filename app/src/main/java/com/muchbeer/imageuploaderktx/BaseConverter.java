package com.muchbeer.imageuploaderktx;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;


public class BaseConverter {
    public String getBase64FromBitmap(Bitmap bitmap)  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, baos); //bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();

        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.NO_WRAP);

        Log.d("BaseCOnverter", "The base 64: "+ encodedImage);
        return encodedImage;
    }
}
