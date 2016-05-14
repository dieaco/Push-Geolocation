package com.entuizer.push.parsers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.entuizer.push.MainActivity;
import com.entuizer.push.cache.AppCache;
import com.entuizer.push.models.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Diego Acosta on 25/04/2016.
 */
public class GetMessages {

    private Context context;
    private MainActivity mainActivity;

    public GetMessages(Context context, MainActivity mainActivity){
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public void getMessages(JSONObject response){
        JSONObject jsonObject = response;

        JSONArray array = null;

        ArrayList<Message> listMessages = new ArrayList<Message>();

        try {
            array = jsonObject.getJSONArray("result");

            for(int i=0; i <array.length(); i++)
            {
                JSONObject json = array.getJSONObject(i);
                Message message = parseMessage(json, array.length(), i+1);
                listMessages.add(message);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        AppCache.setListMessages(listMessages);
    }

    private Message parseMessage(JSONObject json, int arrayLength, int itemPosition){
        String id = "";
        String mensaje = "";
        String timestamp = "";
        int isRead = 0;
        int userId = 0;
        String picture = "";

        try {
            id = json.getString("id");
            mensaje = json.getString("mensaje");
            timestamp = json.getString("timestamp");
            isRead = json.getInt("isRead");
            userId = json.getInt("userId");
            picture = json.getString("picture");
        } catch (JSONException e) {
            e.printStackTrace();
        }



        Message message = new Message();
        message.setId(id);
        message.setMensaje(mensaje);
        message.setTimestamp(timestamp);
        message.setIsRead(isRead);
        message.setUserId(userId);
        message.setUrlPicture(picture);

        new GetMessageBitmap(context, mainActivity, message.getUrlPicture(), message, arrayLength, itemPosition).execute();

        return message;
    }


    public class GetMessageBitmap extends AsyncTask<Void, Void, Void> {

        private Context context;
        private String url;
        private MainActivity mainActivity;
        private Message message;
        private int arrayLength;
        private int itemPosition;

        public GetMessageBitmap(Context context, MainActivity mainActivity, String url, Message message, int arrayLength, int itemPosition){
            this.context = context;
            this.url = url;
            this.mainActivity = mainActivity;
            this.message = message;
            this.arrayLength = arrayLength;
            this.itemPosition = itemPosition;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("MESSAGE FROM ASYNC TASK", message + " - " + this.message.getPicture() + " - " + this.message.getMensaje());
            //AppCache.setMessage(message);
            if(itemPosition == arrayLength){
                mainActivity.progressDialog.dismiss();
                mainActivity.showData(AppCache.getListMessages());
            }
            //mainActivity.showData();
        }

        @Override
        protected Void doInBackground(Void... params) {
            this.message.setPicture(getImage(this.url));
            Log.d("MESSAGE", message + " - " + this.message.getMensaje());
            return null;
        }

        private Bitmap getImage(String bitmapUrl){
            URL url;
            Bitmap image = null;
            try {
                url = new URL(bitmapUrl);
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }catch(Exception e){}
            return image;
        }

    }


}
