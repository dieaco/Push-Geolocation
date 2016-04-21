package com.entuizer.push;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.entuizer.push.data.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        initViews();
    }

    public void initViews(){
        progressDialog = new ProgressDialog(this);
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
