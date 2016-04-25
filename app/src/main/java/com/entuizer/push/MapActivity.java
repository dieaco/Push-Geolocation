package com.entuizer.push;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.entuizer.push.services.GPSTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity {

    private GoogleMap mMap;

    //GPS OBJECT TO GET THE CURRENT LOCATION OF THE USER
    private LocationManager locManager;
    private LocationListener locListener;

    private Marker marker;
    private Circle circle;

    private boolean isNetworkEnabled = false;

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            Log.d("LOCATION FINISHED", "Se termina geolocalización");
            locManager.removeUpdates(locListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void initViews(){

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.map);
        SupportMapFragment supportMapFragment = (SupportMapFragment)fragment;
        mMap = supportMapFragment.getMap();

        comenzarLocalizacion();

    }

    private void comenzarLocalizacion()
    {
        try{
            //Obtenemos una referencia al LocationManager
            locManager =
                    (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            isNetworkEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!isNetworkEnabled){
                Log.d("GPS_PROVIDER", " - "+isNetworkEnabled);
                showSettingsAlert();
            }

            //Obtenemos la última posición conocida
            Location loc =
                    locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            //Mostramos la última posición conocida
            mostrarPosicion(loc);

            //Nos registramos para recibir actualizaciones de la posición
            locListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    mostrarPosicion(location);
                }
                public void onProviderDisabled(String provider){
                    Log.d("Provider OFF","OFF");
                }
                public void onProviderEnabled(String provider){
                    Log.d("Provider ON ","ON");
                }
                public void onStatusChanged(String provider, int status, Bundle extras){
                    Log.i("PROVIDER STATUS", "Provider Status: " + status);
                }
            };

            locManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 0, locListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void mostrarPosicion(Location loc) {
        if(loc != null)
        {
            Log.i("", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));

            if(marker != null){
                Log.d("REMOVING MARKER", ""+marker);
                marker.remove();
                marker = null;
            }
            if(circle != null){
                Log.d("REMOVING CIRCLE", ""+circle);
                circle.remove();
                circle = null;
            }

            latitude = loc.getLatitude();
            longitude = loc.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);

            moveCamera2(latLng);

            /*marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Aquí estoy"));*/

            circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(20)
                    .fillColor(R.color.colorWhite)
                    .strokeColor(R.color.colorAccent)
                    .strokeWidth(4.0f)
            );

        }
        else
        {
            Log.d("LOCATION_VACIO", "Vacío");
        }
    }

    //Camera movement and zoom
    private void moveCamera2(LatLng userCurrentLocation){

        CameraPosition.Builder builder = new CameraPosition.Builder();
        builder.target(userCurrentLocation);
        builder.zoom(18);

        CameraPosition cameraPosition = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        //map.moveCamera(cameraUpdate);
        mMap.animateCamera(cameraUpdate, 3000, null);
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS");

        // Setting Dialog Message
        alertDialog.setMessage("GPS no está habilitado. ¿Desea habilitarlo ahora?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

}
