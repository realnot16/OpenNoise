package com.example.opennoise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN = "Login";
    private static final String LOGI = "LogStat";
    private static final String TAG = "Login_Activity";

    private EditText email;
    private EditText password;

    private static final String FILENAME="registrations.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginStatus();
        setContentView(R.layout.login_layout);
        initUI();
    }

    //Inizializzazione
    private void initUI() {
        email = findViewById(R.id.login_emailField_id);
        password = findViewById(R.id.login_passwordField_id);

    }

    private void loginStatus() {
        Log.i(TAG, "Verifico status login");
        SharedPreferences prefs = getSharedPreferences(LOGIN,Context.MODE_PRIVATE);
        String textData= prefs.getString("logged","unlogged");
        if(textData.compareTo("yes")==0){
            Log.i(TAG, "Sei loggato, redirect a AreaActivity");
            Log.i(LOGI, "LoginA: Sei loggato, redirect a AreaActivity, stato:"+ textData);
            Intent intent = new Intent(this, AreaActivity.class);
            startActivity(intent);
        }
        else
            Log.i(LOGI, "LoginA: NON sei loggato, stato: "+ textData);
    }


    @Override
    public void onStart() {
        super.onStart();
        loginStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences logPreferences = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = logPreferences.edit();
        editor.putString("logged", "no");
        editor.commit();
        emptyFile(this);
        String textData= logPreferences.getString("logged","unlogged");
        Log.i(LOGI, "LoginA: segnalo chiusura, stato:"+ textData);
    }


    //Richiamato con click su Registrati
    public void goToSignup(View view){
        Log.i(TAG, "Hai cliccato su Registrati. Verrai reindirizzato alla pagina di registrazione.");
        //REDIRECT ALLA REGISTRAZIONE
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    //GESTIONE DEL LOGIN
    public void login(View view){
        Log.i(TAG, "Hai cliccato su Login!");

        String mail = email.getText().toString();
        String psw = password.getText().toString();
        new LoginUser().execute(mail,psw);

    }


    //TASK PER EFFETTUARE IL LOGIN
    private class LoginUser extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            String url = "https://quietspace.altervista.org/Login.php";
            String params = null;

            //Encoding parametri:

            try {
                params = "email=" + URLEncoder.encode(strings[0], "UTF-8");

                Log.i(TAG, "Contatto il DB");
                JSONArray jsonArray=ServerTask.askToServer(params,url);
                for (int i = 0; i < jsonArray.length(); i++) {         //Ciclo di estrazione oggetti
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String psw = json_data.getString("Password");
                    Log.i(TAG, "RISULTATO: "+psw);
                    if(psw.compareTo(strings[1])==0)
                        return true;
                }

            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if(b){
                SharedPreferences logPreferences = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = logPreferences.edit();
                editor.putString("logged", "yes");
                editor.putString("email", email.getText().toString());
                editor.commit();
                Log.i(TAG, "Login Effettuato Correttamente");
                Log.i(LOGI, "LoginA, Login Effettuato Correttamente");
                Intent intent = new Intent(LoginActivity.this, AreaActivity.class);
                startActivity(intent);
            }
            else{
                Log.i(TAG, "Login Non Effettuato");
                Toast.makeText(LoginActivity.this, "Email o Password errati, riprovare!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //OVERRIDE FILE
    private void emptyFile(Context context) {
        Log.i(TAG,"ELIMINAZIONE FILE");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILENAME, Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
            Log.i(TAG,"LoginActivity: File Svuotato");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
