package com.entuizer.push.images;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.entuizer.push.MainActivity;
import com.entuizer.push.cache.AppData;

import java.net.URL;

/**
 * Created by Diego Acosta on 21/04/2016.
 */
public class GetMessageBitmap extends AsyncTask<Void, Void, Void>{

    private Context context;
    private String[] urls;
    private ProgressDialog loading;
    private MainActivity mainActivity;

    public GetMessageBitmap(Context context, MainActivity mainActivity, String[] urls){
        this.context = context;
        this.urls = urls;
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loading = ProgressDialog.show(context,"Descargando imagen","Espere un momento...",false,false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        loading.dismiss();
        mainActivity.showData();
    }

    @Override
    protected Void doInBackground(Void... params) {
        for(int i=0; i<urls.length; i++){
            AppData.pictureBitmap[i] = getImage(urls[i]);
        }
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
