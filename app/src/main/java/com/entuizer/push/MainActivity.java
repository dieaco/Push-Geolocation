package com.entuizer.push;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.entuizer.push.adapters.CardMessageAdapter;
import com.entuizer.push.adapters.CardMessageAdapter2;
import com.entuizer.push.cache.AppCache;
import com.entuizer.push.cache.AppData;
import com.entuizer.push.data.UserData;
import com.entuizer.push.images.GetMessageBitmap;
import com.entuizer.push.listeners.EndlessRecyclerOnScrollListener;
import com.entuizer.push.models.Message;
import com.entuizer.push.parsers.GetMessages;
import com.entuizer.push.services.LocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;
    private TextView txtLoadMoreData;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    private AppData config;

    private int cacheLength = 10;
    private int offset = 0;
    private int current_page = 1;

    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    public void initViews(){
        progressDialog = new ProgressDialog(this);

        txtLoadMoreData = (TextView)findViewById(R.id.txtLoadMoreData);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Log.i("SCROLL", "Si se esta moviendo - dy= " + dy);
                if (dy > 0) //check for scroll down
                {
                    Log.i("DY", "Se movio verticalmente");
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        Log.i("LOADING", "Cargando: " + loading);
                        Log.i("VALORES_DE_SCROLL", "CuentaElementosVisibles: " + visibleItemCount + " - CuentaElementosTotales: " + totalItemCount + " - ElementosVisiblesPasados: " + pastVisiblesItems);
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            txtLoadMoreData.setVisibility(View.VISIBLE);
                            txtLoadMoreData.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getMoreData();
                                }
                            });
                        }
                    }
                }if(dy < 0){
                    txtLoadMoreData.setVisibility(View.GONE);
                }
            }
        });

        //Carga recyclerView con los datos del servidor
        getData();
    }

    private void getData(){
        progressDialog.setTitle("Cargando datos");
        progressDialog.setMessage("Espere un momento...");
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest putRequest = new StringRequest(Request.Method.POST, AppData.GET_URL,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        // response
                        //progressDialog.dismiss();
                        //parseJSON(response);
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            GetMessages getMessages = new GetMessages(getApplicationContext(), MainActivity.this);
                            getMessages.getMessages(jsonObject);


                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        progressDialog.dismiss();
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                //User ID almacenado localmente
                int userId = UserData.getUserId(MainActivity.this);Log.d("USERIDPARAMS", "" + userId);

                Map<String, String>  params = new HashMap<String, String>();

                params.put("userId", ""+userId);
                params.put("limit", "10");
                params.put("offset", ""+offset);

                return params;
            }

        };

        queue.add(putRequest);
    }

    public void showData(){
        adapter = new CardMessageAdapter(AppData.id, AppData.mensaje, AppData.timestamp, AppData.isRead, AppData.userId, AppData.pictureBitmap);
        recyclerView.setAdapter(adapter);
    }

    public void showData(ArrayList<Message> list){
        CardMessageAdapter2 adapter2 = new CardMessageAdapter2(getApplicationContext(), list);
        recyclerView.setAdapter(adapter2);
    }

    private void parseJSON(String json){Log.d("RESPONSE_JSON",json);
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray(AppData.TAG_JSON_ARRAY);

            config = new AppData(cacheLength);

            for(int i=offset; i<cacheLength; i++){Log.i("APPDATA LENGTH", ""+AppData.id.length);
                JSONObject j = array.getJSONObject(i);
                AppData.id[i] = j.getString(AppData.TAG_MESSAGE_ID);
                AppData.mensaje[i] = j.getString(AppData.TAG_MESSAGE);
                AppData.timestamp[i] = j.getString(AppData.TAG_MESSAGE_TIMESTAMP);
                AppData.isRead[i] = j.getInt(AppData.TAG_MESSAGE_IS_READ);
                AppData.userId[i] = j.getInt(AppData.TAG_MESSAGE_USER_ID);
                AppData.picture[i] = j.getString(AppData.TAG_MESSAGE_PICTURE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        GetMessageBitmap gb = new GetMessageBitmap(this,this, AppData.picture);
        gb.execute();

        cacheLength = cacheLength + 10;
        offset = offset + 10;
        Log.i("VALOR DE ARRAY",AppData.mensaje[0]);
    }

    public void getMoreData(){
        progressDialog.setTitle("Cargando datos");
        progressDialog.setMessage("Espere un momento...");
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest putRequest = new StringRequest(Request.Method.POST, AppData.GET_URL,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        // response
                        // response
                        //progressDialog.dismiss();
                        //parseJSON(response);
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            GetMessages getMessages = new GetMessages(getApplicationContext(), MainActivity.this);
                            getMessages.getMessages(jsonObject);
                            loading = true;
                            txtLoadMoreData.setVisibility(View.GONE);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        progressDialog.dismiss();
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                //User ID almacenado localmente
                int userId = UserData.getUserId(MainActivity.this);Log.d("USERIDPARAMS", "" + userId);

                Map<String, String>  params = new HashMap<String, String>();

                params.put("userId", ""+userId);
                params.put("limit", ""+(cacheLength+10));
                params.put("offset", ""+offset);

                return params;
            }

        };

        queue.add(putRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_location) {
            intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
            return true;
        }else if(id == android.R.id.home){
            signoutConfirmation().show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Dialogo de confirmación para cierre de sesión
    public AlertDialog signoutConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setMessage("¿Está seguro que desea cerrar su sesión?")
                .setCancelable(true)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Actualización del listado una vez que se presiona el botón Listo
                        signOutFromServer();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    //Cerrar la sesión desde el servidor
    public void signOutFromServer(){
        progressDialog.setTitle("Aviso");
        progressDialog.setMessage("Actualizando...");
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        //String url = "http://suterm.comli.com/regID.php";
        String url = "http://www.entuizer.tech/administrators/pri/webServices/updateSessionStatus.php";
        StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        // response
                        Log.d("Response", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            int ERROR = jsonObject.getInt("ERROR");

                            Log.d("ERROR", "" + ERROR);

                            UserData.setLogged(MainActivity.this, false);
                            UserData.setRegId(MainActivity.this, "");
                            UserData.setUpLocationService(MainActivity.this, false);
                            stopService(new Intent(MainActivity.this, LocationService.class));

                            launchMainActivity();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                //Extrae UserId Local y RegIdLocal
                String regId = UserData.getRegId(MainActivity.this);
                int userId = UserData.getUserId(MainActivity.this);

                Map<String, String> params = new HashMap<String, String>();

                Log.d("VALORES", "REGID: " + regId + " - USERID: " + userId);

                params.put("regisId", regId);
                params.put("userId", "" + userId);

                return params;
            }

        };

        queue.add(putRequest);
    }

    public void launchMainActivity(){
        Intent intent = new Intent(MainActivity.this, Signin.class);
        startActivity(intent);
        MainActivity.this.finish();
    }

}
