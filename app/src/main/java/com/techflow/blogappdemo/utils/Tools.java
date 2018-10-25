package com.techflow.blogappdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import id.zelory.compressor.Compressor;

/**
 * Created by DILIP on 18/10/2018
 */

public class Tools {

    private static final int MAX_LENGTH = 100;

    // Random string generator method---
    public static String randomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static byte[] imageCompressor(Uri image_uri, Context context) {
        Bitmap compressedImageFile = null;

        File newImageFile = new File(image_uri.getPath());
        try {

            compressedImageFile = new Compressor(context)
                    .setMaxHeight(100)
                    .setMaxWidth(100)
                    .setQuality(2)
                    .compressToBitmap(newImageFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        return imageData;
    }

}
