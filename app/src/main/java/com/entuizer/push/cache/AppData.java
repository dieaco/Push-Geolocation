package com.entuizer.push.cache;

import android.graphics.Bitmap;

/**
 * Created by Diego Acosta on 21/04/2016.
 */
public class AppData {

    public static String[] id;
    public static String[] mensaje;
    public static String[] timestamp;
    public static int[] isRead;
    public static int[] userId;
    public static String[] picture;
    public static Bitmap[] pictureBitmap;

    public static int i = 0;

    public static final String GET_URL = "http://www.entuizer.tech/administrators/pri/webServices/getMessages.php";
    public static final String TAG_JSON_ARRAY = "result";
    public static final String TAG_MESSAGE_ID = "id";
    public static final String TAG_MESSAGE = "mensaje";
    public static final String TAG_MESSAGE_TIMESTAMP = "timestamp";
    public static final String TAG_MESSAGE_IS_READ = "isRead";
    public static final String TAG_MESSAGE_USER_ID = "userId";
    public static final String TAG_MESSAGE_PICTURE = "picture";

    public AppData(int i){
        this.i = i;

        id = new String[i];
        mensaje = new String[i];
        timestamp = new String[i];
        isRead = new int[i];
        userId = new int[i];
        picture = new String[i];
        pictureBitmap = new Bitmap[i];
    }

}
