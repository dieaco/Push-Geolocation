package com.entuizer.push.cache;

import android.util.Log;

import com.entuizer.push.models.Message;

import java.util.ArrayList;

/**
 * Created by Diego Acosta on 25/04/2016.
 */
public class AppCache {

    private static ArrayList<Message> listMessages;

    public static ArrayList<Message> getListMessages(){
        return listMessages;
    }

    public static void setListMessages(ArrayList<Message> listMessages){
        AppCache.listMessages = listMessages;
    }

    public static void setMessage(Message message){
        Log.i("MENSAJE-APPCACHE", message+" - "+message.getMensaje()+" - "+message.getPicture());
        AppCache.listMessages.add(message);
    }

    public static Message getMessage(int position){
        return listMessages.get(position);
    }

}
