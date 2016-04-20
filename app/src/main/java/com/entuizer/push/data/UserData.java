package com.entuizer.push.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Diego Acosta on 13/04/2016.
 */
public class UserData {

    //Consultar una preferencia
    public static boolean isLogged(Context context){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        return preferences.getBoolean("IS_LOGGED", false);
    }

    public static int getUserId(Context context){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        return preferences.getInt("USER_ID", 0);
    }

    public static String getUserAccount(Context context){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        return preferences.getString("USER_ACCOUNT", "");
    }

    public static String getRegId(Context context){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        return preferences.getString("REG_ID", "");
    }

    //escribir una preferencia
    public static void setLogged(Context context, boolean isLogged){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("IS_LOGGED", isLogged);
        edit.commit();
    }

    public static void setUserId(Context context, int userId){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = preferences.edit();
        edit.putInt("USER_ID", userId);
        edit.commit();
    }

    public static void setUserAccount(Context context, String userAccount){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("USER_ACCOUNT", userAccount);
        edit.commit();
    }

    public static void setRegId(Context context, String regId){
        SharedPreferences preferences =
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("REG_ID", regId);
        edit.commit();
    }

}
