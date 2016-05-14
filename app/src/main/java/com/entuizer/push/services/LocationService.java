package com.entuizer.push.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.entuizer.push.data.UserData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Diego Acosta on 10/05/2016.
 */
public class LocationService extends Service
{
    public static final String BROADCAST_ACTION = "android.permission.RECEIVE_BOOT_COMPLETED";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private  static final int TIME_TO_UPDATE = 1000 * 60 * 1;
    private static final int METERS_TO_UPDATE = 1;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    Intent intent;
    int counter = 0;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //intent = new Intent(BROADCAST_ACTION);

        //Detiene el servicio en dado caso que el usuario no cuente con sesión iniciada
        Log.e("USER_IS_LOGGED", " - "+UserData.isLogged(getApplicationContext()));
        if(!UserData.isLogged(getApplicationContext())){
            this.stopSelf();
            Log.e("STOP_SELF", "Se detuvo el servicio");
        }else{
            UserData.setUpLocationService(getApplicationContext(), true);
        }

    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_TO_UPDATE, METERS_TO_UPDATE, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_TO_UPDATE, METERS_TO_UPDATE, listener);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e("ONSTARTCOMMAND", "onStartCommand LOCATION SERVICE");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        try{
            locationManager.removeUpdates(listener);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }




    public class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(final Location loc)
        {
            Log.i("*********************", "Location changed");
            if(isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                Log.d("COORDENADAS - ", loc.getLatitude() + ", " + loc.getLongitude());

                updateUserLocation(loc.getLatitude(), loc.getLongitude());

                //intent.putExtra("Latitude", loc.getLatitude());
                //intent.putExtra("Longitude", loc.getLongitude());
                //intent.putExtra("Provider", loc.getProvider());
                //sendBroadcast(intent);

            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            //Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
            Log.i("onProviderDisabled", "GPS Disabled");
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            //Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
            Log.i("onProviderDisabled", "GPS Enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        public void updateUserLocation(final double latitude, final double longitude){

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://www.entuizer.tech/administrators/pri/webServices/updateUserGeolocation.php";
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
                    Log.d("VALORES", "USERID: " + userId);

                    params.put("userLatitude", ""+latitude);
                    params.put("userLongitude", ""+longitude);
                    params.put("userId", ""+userId);

                    return params;
                }

            };

            queue.add(putRequest);

        }

    }
}
