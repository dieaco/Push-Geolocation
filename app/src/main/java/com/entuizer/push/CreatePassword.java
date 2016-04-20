package com.entuizer.push;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
import java.util.regex.Pattern;

public class CreatePassword extends AppCompatActivity {

    private EditText etUsername;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private Button btnCuentas;
    private Button btnRegister;
    private FloatingActionButton fab;

    private String userName = "";
    private int userId = 0;
    private boolean userIsLogged = false;

    private String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Registro");
        setSupportActionBar(toolbar);

        //Obtenemos preferencias almacenadas localmente para inicio de sesión
        userIsLogged = UserData.isLogged(this);
        userName = UserData.getUserAccount(this);
        userId = UserData.getUserId(this);

        //Valida si existen preferencias
        if(userIsLogged){
            launchNotificationsListActivty();
        }else if(userName.equals("") && userId == 0){
            initViews();
        }else{
            launchLoginActivity();
        }

    }

    public void initViews(){
        etUsername = (EditText)findViewById(R.id.etUsername2);
        etNewPassword = (EditText)findViewById(R.id.etNewPassword);
        etConfirmNewPassword = (EditText)findViewById(R.id.etConfirmPassword);
        btnCuentas = (Button)findViewById(R.id.btnCuentas2);
        btnRegister = (Button)findViewById(R.id.btnRegister);

        //Hint para usuario acerca del patrón de la contraseña
        etNewPassword.setError(getString(R.string.wrongPassword), getResources().getDrawable(android.R.drawable.ic_dialog_alert));

        //Establece como no editable el edit text de Usuario
        etUsername.setKeyListener(null);

        //Evento click para mostrar dialogo con cuentas del dispositivo
        btnCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAccounts().show();
            }
        });

        //Evento click para registrar usuario y contraseña
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        //Inicialización de fab button para recuperación de contraseña
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Recuperación de contraseña", Snackbar.LENGTH_LONG)
                        .setAction("Enviar a email", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendMailRetrievePassword();
                            }
                        })
                        .show();
            }
        });
    }

    //Cuadro de dialogo para mostrar cuentas del dispositivo
    public AlertDialog loadAccounts(){
        final ArrayAdapter<String> array = new ArrayAdapter<String>(
                CreatePassword.this,
                android.R.layout.select_dialog_singlechoice
        );

        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                String emailType = account.type;
                array.add(possibleEmail);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CreatePassword.this);
        builder.setTitle("Seleccione una cuenta")
                .setAdapter(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etUsername.setText(array.getItem(which));
                        getUserId();
                        fab.setVisibility(View.VISIBLE);
                    }
                });

        return builder.create();

    }

    public AlertDialog messageDialogOptions(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CreatePassword.this);
        builder.setTitle("Aviso")
                .setMessage(getString(R.string.goToSignIn))
                .setPositiveButton("Ir a inicio de sesión", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserData.setUserAccount(getApplicationContext(), etUsername.getText().toString().trim());
                        dialog.dismiss();
                        launchLoginActivity();
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

    //Registra usuario en el servidor
    public void registerUser(){
        if(validateFields()){
            serverRequest();

        }
    }

    //Consulta al servidor para obtener String del PHP
    public void serverRequest(){
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Cargando...");
        dialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.entuizer.tech/administrators/pri/webServices/registerUser.php";
        StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            int ERROR = jsonObject.getInt("ERROR");

                            Log.d("ERROR", ""+ERROR);
                            if(ERROR == 1){
                                etUsername.setError(getString(R.string.repeatedAccount));
                                dialog.dismiss();
                            }else{
                                int userId = jsonObject.getInt("user_id");Log.d("USERID",""+userId);
                                //Se almacenan preferencias de usuario
                                UserData.setUserId(getApplicationContext(), userId);
                                UserData.setUserAccount(getApplicationContext(), etUsername.getText().toString());
                                //Lanza actividad de inicio de sesión
                                launchLoginActivity();
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
                        //Log.d("Error.Response", token);
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                String userN = etUsername.getText().toString();
                String userP = etNewPassword.getText().toString();
                Log.d("VALORES", "Username: "+userN+" - Userpassword: "+userP);

                params.put("userName", etUsername.getText().toString());
                params.put("userPassword", etNewPassword.getText().toString());
                params.put("userType", "1");
                params.put("userCompany", "1");
                params.put("userIsLogged", "0");

                return params;
            }

        };

        queue.add(putRequest);

    }

    //Envío de contraseña al correo seleccionado
    public void sendMailRetrievePassword(){
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Enviando...");
        dialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.entuizer.tech/administrators/pri/includes/sendMailRetrievePassword.php";
        StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        // response
                        dialog.dismiss();
                        Log.d("Response", response);
                        sendMailRetrievePasswordConfirmation(response).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.Response", token);
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                String userN = etUsername.getText().toString();
                Log.d("VALORES", "Username: " + userN);

                params.put("for", userN);

                return params;
            }

        };

        queue.add(putRequest);

    }

    public AlertDialog sendMailRetrievePasswordConfirmation(String response){
        ImageView ivResponse = null;
        if(ivResponse == null)
            ivResponse = new ImageView(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreatePassword.this);
        builder.setTitle("Aviso")
                .setCancelable(true);

        //Convertir respuesta a Json y extracción de datos
        try {
            JSONArray jsonArray = new JSONArray(response);
            final JSONObject jsonObject = jsonArray.getJSONObject(0);
            if(jsonObject.getInt("ERROR") == 0) {
                userId = jsonObject.getInt("user_id");
                ivResponse.setImageResource(R.mipmap.ic_done);
                builder.setMessage("Su contraseña ha sido enviada al correo " + etUsername.getText() + ".")
                        .setView(ivResponse)
                        .setPositiveButton("Ir a inicio de sesión", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserData.setUserAccount(getApplicationContext(), etUsername.getText().toString().trim());
                                UserData.setUserId(getApplicationContext(), userId);
                                dialog.dismiss();
                                launchLoginActivity();
                            }
                        });
            }
            else {
                ivResponse.setImageResource(R.mipmap.ic_error);
                builder.setMessage("Ocurrió un error con el envío de la contraseña. Intente nuevamente.")
                        .setView(ivResponse)
                        .setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return builder.create();
    }

    //Valida que no haya campos vacíos y que los campos cumplan con las características solicitadas
    public boolean validateFields(){
        if(etUsername.getText().toString().equals("")){
            etUsername.setError(getString(R.string.emptyUser));
            return  false;
        }else if(etNewPassword.getText().toString().equals("")){
            etNewPassword.requestFocus();
            etNewPassword.setError(getString(R.string.emptyPassword));
            return false;
        }else if(!etNewPassword.getText().toString().matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8}$")){
            etNewPassword.requestFocus();
            etNewPassword.setError(getString(R.string.wrongPassword));
            return false;
        }else if(!etConfirmNewPassword.getText().toString().equals(etNewPassword.getText().toString())){
            etConfirmNewPassword.requestFocus();
            etConfirmNewPassword.setError(getString(R.string.equalPassword));
            return false;
        }else{
            return true;
        }
    }

    public void getUserId(){
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Cargando...");
        dialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.entuizer.tech/administrators/pri/webServices/getUserId.php";
        StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {

                    public void onResponse(String response) {
                        // response
                        dialog.dismiss();
                        Log.d("Response", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            userId = jsonObject.getInt("user_id");
                            UserData.setUserId(getApplicationContext(), userId);
                            messageDialogOptions().show();
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
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                String userN = etUsername.getText().toString();
                Log.d("VALORES", "Username: "+userN);

                params.put("username", userN);

                return params;
            }

        };

        queue.add(putRequest);

    }

    //Lanza actividad de inicio de sesión
    public void launchLoginActivity(){
        Intent intent = new Intent(CreatePassword.this, Signin.class);
        startActivity(intent);
        CreatePassword.this.finish();
    }

    //Lanza actividad principal
    public void launchNotificationsListActivty(){
        Intent intent = new Intent(CreatePassword.this, MainActivity.class);
        startActivity(intent);
        CreatePassword.this.finish();
    }

}
