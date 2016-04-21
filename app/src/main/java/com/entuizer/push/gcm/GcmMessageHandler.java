package com.entuizer.push.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.entuizer.push.MainActivity;
import com.entuizer.push.R;
import com.entuizer.push.data.UserData;
import com.entuizer.push.services.GPSTracker;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Diego Acosta on 12/04/2016.
 */
public class GcmMessageHandler extends IntentService {

    //Atributos para Notifications en barra de estado
    String message;
    String messageId;
    String title;
    Uri alarmSound;
    private final int NOTIFICATION_ID = 12345;
    //GCM
    private Handler handler;
    //Noitificaciones
    private NotificationManager mNotificationManager;
    //Variables para controlar grupos
    private static int number = 0;
    private static final String GROUP_NOTIFICATIONS = "group_notifications";
    //Geolocalización
    /*private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private double latitude;
    private double longitude;*/
    GPSTracker gps;


    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();

        gps = new GPSTracker(GcmMessageHandler.this);

        //initGeolocalization();
        //updatePosition();

    }

    /*public void initGeolocalization() {
        locationManager = (LocationManager) this.getSystemService(getApplicationContext().LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Called when a new location is found by the network provider
                updatePosition(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }catch (SecurityException e){
            Log.d("ERROR",e.toString());
        }
    }

    public void updatePosition() {
        try{
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            updatePosition(location);
        }catch (SecurityException e){
            Log.d("ERROR", e.toString());
        }

    }

    public void updatePosition(Location location){
        if(location != null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();Toast.makeText(getApplicationContext(),"Latitude: "+latitude+" - Longitude: "+longitude,Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        number++;
        Log.d("# of notification: ", " " + number);

        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = new GoogleCloudMessaging().getInstance(this);

        String messageType =  gcm.getMessageType(intent);

        messageId = extras.getString("messageId");
        message = extras.getString("message");
        title = "Notificación Entuizer";
        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.putExtra("mensaje", message);
        notificationIntent.putExtra("titulo", title);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        mNotificationManager = (NotificationManager)this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = new BitmapFactory().decodeResource(getResources(), R.drawable.bullet_notifi);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.bullet_notifi)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message)
                                .setSummaryText(number+" mensajes nuevo"))
                        .setGroup(GROUP_NOTIFICATIONS)
                        .setGroupSummary(true);

        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        //showToast();

        Log.i("GCM", "Received: ("+messageType+") "+extras.getString("message")+" - "+extras.getString("messageId"));

        //GEOLOCATION ACTIONS
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        Log.i("POSITION","Latitude: "+latitude+" - Longitude: "+longitude);

        //SEND DATA TO SERVER ABOUT DE USER LOCATION
        storeUserLocationWhenReceiveMessage(extras.getString("messageId"), latitude, longitude);

        gps.stopUsingGPS();

        //Para indicar que una vez sea posible apague el servicio
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void storeUserLocationWhenReceiveMessage(final String messageId, final double latitude, final double longitude){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.entuizer.tech/administrators/pri/webServices/setMessageUserLocation.php";
        StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                int userId = UserData.getUserId(getApplicationContext());
                Log.d("VALORES", "USERID: "+userId+" - MESSAGEID: "+messageId);

                params.put("latitude", ""+latitude);
                params.put("longitude", ""+longitude);
                params.put("messageId", messageId);
                params.put("userId", ""+userId);

                return params;
            }

        };

        queue.add(putRequest);

    }

    public void showToast(){
        handler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

        });
    }

}
