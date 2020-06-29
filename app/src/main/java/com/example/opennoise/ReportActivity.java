package com.example.opennoise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ReportActivity extends AppCompatActivity {

    private static final String LOGIN = "Login";
    private static final String LOGI = "LogStat";
    private static final String TAG = "Report";

    private EditText textBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_layout);
        initUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        loginStatus();
    }

    private void loginStatus() {
        SharedPreferences prefs = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        String textData= prefs.getString("logged","unlogged");
        if(textData.compareTo("yes")!=0){
            Log.i(TAG, "Non sei loggato, redirect a LoginActivity");
            Log.i(LOGI, "ReportA: Non sei loggato, redirect a LoginActivity, stato:"+ textData);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else
            Log.i(LOGI, "OK, LOGGATO, ReportActivity, stato:"+textData);
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.report_toolbar_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            Log.i(TAG, "Back, redirect a MainActivity");
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void inviaFeedback(View view){
        textBox = findViewById(R.id.report_box_text_id);
        if(textBox.getText().toString().isEmpty()){
            Log.i(TAG, "Segnalazione vuota");
            Toast.makeText(ReportActivity.this,"Inserisci un feedback!",Toast.LENGTH_SHORT).show();
        }
        else{
            Log.i(TAG, "Invia feedback al DB");
            SharedPreferences prefs = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
            String email= prefs.getString("email","unlogged");
            new InviaSegnalazione().execute(email,textBox.getText().toString());
        }


    }


    //TASK PER EFFETTUARE IL LOGIN
    private class InviaSegnalazione extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            String url = "https://quietspace.altervista.org/insertSegnalazione.php";
            String params = null;

            //Encoding parametri:

            try {
                params = "email=" + URLEncoder.encode(strings[0], "UTF-8")
                        + "&body=" + URLEncoder.encode(strings[1], "UTF-8");

                Log.i(TAG, "Contatto il DB");
                JSONArray jsonArray=ServerTask.askToServer(params,url);
                JSONObject jsonObjectControl=jsonArray.getJSONObject(0);
                String control=jsonObjectControl.getString("control");
                Log.i(TAG, control);
                if (control.equals("OK")){
                    //SEGNALAZIONE INVIATA CORRETTAMENTE
                    Log.i(TAG, "Segnalazione inviata");
                    return true;
                }
            }
            catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean send) {
            super.onPostExecute(send);
            if(send){
                Log.i(TAG, "Reindirizzo a main activity");
                Toast.makeText(ReportActivity.this,"Feedback Inviato\n Ritorna alla pagina principale",Toast.LENGTH_SHORT).show();
            }
        }
    }

}

