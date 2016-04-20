package com.entuizer.push;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.entuizer.push.data.UserData;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Signin extends AppCompatActivity {

    //GCM registro
    GoogleCloudMessaging gcm;
    String regId;
    String PROJECT_NUMBER = "100447446537";
    //Views
    private EditText etUsername;
    private EditText etPassword;
    private Button btnIniciarSesion;
    private CheckBox cbRecordar;
    private ProgressDialog progressDialog;

    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    //Inicialización de vistas
    public void initViews(){

        etUsername = (EditText)findViewById(R.id.etUsername);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btnIniciarSesion = (Button)findViewById(R.id.btnIniciarSesion);
        cbRecordar = (CheckBox)findViewById(R.id.cbRecordar);

        progressDialog = new ProgressDialog(this);

        //Se establece como usuario la cuenta registrada por el usuario para que haga su inicio de sesión
        userName = UserData.getUserAccount(this);
        Log.d("USER_ACCOUNT_LOCAL", userName);
        etUsername.setText(userName);

        //Establece como no editable el edit text de Usuario
        etUsername.setKeyListener(null);

        //progressDialog = new ProgressDialog(this, R.style.MyTheme);
        progressDialog.setTitle("Registro en GCM");
        progressDialog.setMessage("Espere un momento...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //Devuelve REGID de GCM para recepción de Notificaciones
        getRegId();

        //Evento click para iniciar sesión
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSession();
            }
        });

    }

    //Registra dispositivo en Google Cloud Messaging
    public void getRegId(){
        new AsyncTask<Void, Void, String>() {
            @SuppressWarnings("deprecation")
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId = gcm.register(PROJECT_NUMBER);
                    msg = "" + regId;
                    Log.i("GCM", msg);

                    progressDialog.dismiss();

                } catch (IOException ex) {
                    Toast.makeText(getApplicationContext(),"Ocurrió un error con el registro en GCM. Reinice la aplicación.",Toast.LENGTH_LONG).show();
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                UserData.setRegId(getApplicationContext(),msg.toString().trim());
            }
        }.execute(null, null, null);
    }

    //Inicio de sesión y registro del RegId en la base de datos
    public void startSession(){
        progressDialog.setTitle("Inicio de sesión");
        progressDialog.setMessage("Validando...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if(validateFields()){
            validateLogin();
        }
    }

    //Validación de campos
    public boolean validateFields(){
        if(etPassword.getText().toString().equals("")){
            progressDialog.dismiss();
            etPassword.requestFocus();
            etPassword.setError(getString(R.string.emptyPassword));
            return false;
        }else if(!etPassword.getText().toString().matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8}$")){
            progressDialog.dismiss();
            etPassword.requestFocus();
            etPassword.setError(getString(R.string.wrongPassword));
            return false;
        }else{
            return true;
        }
    }

    //Valida inicio de sesión desde  servidor
    public void validateLogin(){


        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.entuizer.tech/administrators/pri/webServices/validateLoginApp.php";
        StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        // response
                        try {
                            progressDialog.dismiss();
                            Log.d("RESPONSE", response.toString());
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            int ERROR = jsonObject.getInt("ERROR");

                            //Validación de la respuesta JSON
                            if(ERROR == 0){
                                if(cbRecordar.isChecked())
                                    UserData.setLogged(Signin.this, true);
                                Intent intent = new Intent(Signin.this, MainActivity.class);
                                startActivity(intent);
                                Signin.this.finish();
                            }else{
                                Toast.makeText(Signin.this, getString(R.string.wrongValidation), Toast.LENGTH_SHORT).show();
                                etPassword.requestFocus();
                            }
                        } catch (JSONException e) {
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
                //Reg ID almacenando localmente
                String token = UserData.getRegId(Signin.this);Log.d("REGID:", token);
                //Obtener el número serial del dispositivo
                String numSerie = Build.SERIAL;
                //User ID almacenado localmente
                int userId = UserData.getUserId(Signin.this);Log.d("USERIDPARAMS",""+userId);
                //Verificamos si se desea mantener sesión abierta o no
                boolean isLogged = cbRecordar.isChecked();
                String userIsLogged;
                if(isLogged)
                    userIsLogged = "1";
                else
                    userIsLogged = "0";

                Map<String, String>  params = new HashMap<String, String>();

                params.put("regisId", token.toString());
                params.put("numSerie", numSerie);
                params.put("userId", ""+userId);
                params.put("userIsLogged", userIsLogged);
                params.put("userName", etUsername.getText().toString());
                params.put("userPassword", etPassword.getText().toString());

                return params;
            }

        };

        queue.add(putRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Log.d("UPBUTTON", "Se presionó el botón up");
            UserData.setUserAccount(this, "");
            UserData.setUserId(this, 0);
            Intent intent = new Intent(Signin.this, CreatePassword.class);
            startActivity(intent);
            Signin.this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
