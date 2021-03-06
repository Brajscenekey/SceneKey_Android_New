package com.scenekey.model;

import android.graphics.Bitmap;

import com.scenekey.util.Utility;

import java.io.Serializable;

/**
 * Created by mindiii on 15/2/18.
 */

public class ImagesUpload implements Serializable {

    public Bitmap bitmap;
    public String path;
    public String key;
    private static String url = "https://s3-us-west-1.amazonaws.com/scenekey-profile-images/";

    public ImagesUpload(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public ImagesUpload( String key) {
        Utility.e("KEY : _only",key);
        this.key = key;
        this.path = url+key;
    }
    public ImagesUpload( String key ,Bitmap bitmap) {
        Utility.e("KEY : _bitmap",key);
        this.key = key;
        this.path = url+key;
        this.bitmap = bitmap;
    }

}
