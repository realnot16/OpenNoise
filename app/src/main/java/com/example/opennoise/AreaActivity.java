package com.example.opennoise;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AreaActivity extends AppCompatActivity {

    private static final String LOGIN = "Login";
    private static final String TAG = "Area_Activity";
    private boolean eLoad=false;

    private Switch rem;
    private EditText api;
    private EditText channel;
    private Spinner spinnerEdificio;
    private Spinner spinnerAmbiente;

    private static double decibel=-5;

    private ArrayList<Edificio> edificiSpinner;
    private ArrayList<Stanza> stanzeSpinner;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginStatus();
        setContentView(R.layout.area_layout);
        spinnerEdificio = findViewById(R.id.area_spinner_edificio_id);
        spinnerAmbiente = findViewById(R.id.area_spinner_stanza_id);
        edificiSpinner = new ArrayList<>();
        stanzeSpinner = new ArrayList<>();
        InitUI();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void InitUI() {
        rem = findViewById(R.id.area_remoto_switch_id);
        api = findViewById(R.id.area_remoto_api_id);
        channel = findViewById(R.id.area_remoto_channel_id);
        rem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    api.setVisibility(View.VISIBLE);
                    channel.setVisibility(View.VISIBLE);
                }
                else{
                    api.setVisibility(View.GONE);
                    channel.setVisibility(View.GONE);
                }
            }
        });
        spinnerEdificio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(spinnerEdificio.getSelectedItem().toString().compareTo("...")==0){
                    if(!eLoad){
                        new getEdificio().execute();
                        eLoad=true;
                    }
                }
                return false;
            }
        });

        spinnerEdificio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spinnerEdificio.getSelectedItem().toString().compareTo("...")!=0) {
                    Edificio ed = getEdFromString(spinnerEdificio.getSelectedItem().toString());
                    String idE = String.valueOf(ed.getId());
                    Log.i(TAG, "Ricerca stanze avviata");
                    new getStanze().execute(idE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {


            }
        });

    }

    private void loginStatus() {
        SharedPreferences prefs = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        String textData= prefs.getString("logged","unlogged");
        if(textData.compareTo("yes")!=0){
            Log.i(TAG, "Non sei loggato, redirect a LoginActivity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else
            Log.i(TAG, "OK, LOGGATO_AreaActivty");
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
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void avvioRegistrazione (View v){
        Log.i(TAG, "Avvio procedura redirect");
        if(spinnerEdificio.getSelectedItem().toString().compareTo("...")==0){
            Toast.makeText(AreaActivity.this, "Inserire Edificio", Toast.LENGTH_SHORT).show();
            return;
        }
        if(rem.isChecked()){
            boolean valid=true;
            if (TextUtils.isEmpty(api.getText().toString())){
                api.setError("Campo obbligatorio!");
                valid=false;
            }
            if (TextUtils.isEmpty(channel.getText().toString())){
                channel.setError("Campo obbligatorio!");
                valid=false;
            }

            if(api.getText().toString().length()!=16){
                api.setError("Formato errato! ");
                valid=false;
            }
            if(channel.getText().toString().length()!=7){
                channel.setError("Formato errato!");
                valid=false;
            }

            if(!valid)  return;

        }

        Log.i(TAG, "Campi verificati correttamente");
        SharedPreferences prefs = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        String email= prefs.getString("email","unlogged");
        LocalDateTime ldt =LocalDateTime.now();
        String data = ldt.getDayOfMonth()+" "+ldt.getMonth()+" "+ldt.getYear();
        String ed = spinnerEdificio.getSelectedItem().toString();
        String st = spinnerAmbiente.getSelectedItem().toString();
        Stanza sta = getStFromString(st);
        Registrazione reg = new Registrazione(email,data,ed,st,sta.getId(), rem.isChecked());
        if(reg.isMicrofono()){
            if(checkAPI(api.getText().toString(),channel.getText().toString())){
                reg.setApi(api.getText().toString());
                reg.setChannel(channel.getText().toString());
            }
            else{
                Toast.makeText(AreaActivity.this, "Microfono remoto inesistente", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        else{
            Log.i(TAG,"Richiesta permessi AUDIO");
            ActivityCompat.requestPermissions(AreaActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            if(ActivityCompat.checkSelfPermission(AreaActivity.this,   //controlla che il permesso sia garantito
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG,"Permessi AUDIO negati");
                return;
            }
            Log.i(TAG,"Permessi AUDIO concessi");
        }
        Log.i(TAG,"REGISTRAZIONE: "+reg.getEmail()+" "+reg.getData()+" "+reg.isMicrofono());
        Bundle regBundle = new Bundle();
        regBundle.putParcelable("registrazione",reg);
        Intent intent = new Intent(AreaActivity.this, MainActivity.class);
        intent.putExtra("registrazione",regBundle);
        startActivity(intent);

        return;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean checkAPI(String api, String chan) {
        AsyncTask getDeci = new requestDecibel().execute(api,chan);
        while(decibel==-5){
        }
        Log.i(TAG,"TASK ENDED: "+decibel);
        if(decibel>0)
            return true;
        else
            return false;

    }

    //TASK ASINCRONO PER RICHIESTA DECIBEL --> RENDERE INFINITO
    @SuppressLint("StaticFieldLeak")
    private static class requestDecibel extends AsyncTask<String,Void,Double>{

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Double doInBackground(String... strings) {

            double deci=-1;
            try{
                String API = URLEncoder.encode(strings[0],"UTF-8");
                String channel = URLEncoder.encode(strings[1],"UTF-8");
                String url = "https://api.thingspeak.com/channels/"+channel+"/fields/1/last?api_key="+API;
                URL urlserver = new URL(url);
                HttpsURLConnection urlConnection = (HttpsURLConnection) urlserver.openConnection(); //apertura connessione

                Log.i(TAG, "Connessione effettuata");
                InputStream is = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                is.close();//chiusura

                String result = sb.toString();
                Log.i(TAG, "risultato ricevuto: "+result);
                if(!result.isEmpty()){
                    deci = Double.parseDouble(result);
                }

            }
            catch (Exception e){
                Log.e(TAG, "Error " + e.toString());
            }
            decibel=deci;
            return deci;
        }
    }


    //RECUPERO DATI EDIFICIO
    private class getEdificio extends AsyncTask<Void, Void, ArrayList<Edificio>> {

        @Override
        protected ArrayList<Edificio> doInBackground(Void... strings) {
            String url = "https://quietspace.altervista.org/getEdificio.php";
            String params = "";

            //Encoding parametri:

            try {
                Log.i(TAG, "Contatto il DB per ottenere gli edifici");
                JSONArray jsonArray=ServerTask.askToServer(params,url);
                Log.i(TAG, "Ritorno");
                for (int i = 0; i < jsonArray.length(); i++) {         //Ciclo di estrazione oggetti
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String indirizzo = json_data.getString("Indirizzo");
                    String citta = json_data.getString("Citta");
                    Log.i(TAG, "RISULTATO: "+citta+", "+indirizzo);
                    Integer id =Integer.parseInt(json_data.getString("Edificio_ID"));
                    Integer cap =Integer.parseInt(json_data.getString("CAP"));

                    Edificio edificio = new Edificio(id,indirizzo,citta,cap);
                    edificiSpinner.add(edificio);

                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return edificiSpinner;
        }

        @Override
        protected void onPostExecute(ArrayList<Edificio> edifici) {
            super.onPostExecute(edifici);
            List<String> spinnerArray =  new ArrayList<String>();
            for (int i = 0; i < edifici.size(); i++) {         //Ciclo di estrazione oggetti
                Edificio stat = edifici.get(i);
                String spinItem = stat.getCitta()+", "+stat.getIndirizzo();
                spinnerArray.add(spinItem);
                Log.i(TAG, "spinner edificio aggiunto");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    AreaActivity.this,R.layout.spinner_layout, spinnerArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinnerEdificio.setAdapter(adapter);
        }
    }

    //RECUPERO DATI STANZA
    private class getStanze extends AsyncTask<String, Void, ArrayList<Stanza>> {

        @Override
        protected ArrayList<Stanza> doInBackground(String... strings) {
            String url = "https://quietspace.altervista.org/getStanza.php";
            String params = "";
            ArrayList<Stanza> stanze = new ArrayList<Stanza>();

            //Encoding parametri:

            try {
                Log.i(TAG, "Contatto il DB per ottenere le stanze");
                params="edificio=" + URLEncoder.encode(strings[0], "UTF-8");
                JSONArray jsonArray=ServerTask.askToServer(params,url);
                Log.i(TAG, "Ritorno");
                for (int i = 0; i < jsonArray.length(); i++) {         //Ciclo di estrazione oggetti

                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String nome = json_data.getString("Nome");
                    float dimensioni = Float.parseFloat(json_data.getString("Dimensioni"));
                    Integer areaId =Integer.parseInt(json_data.getString("AREA_ID"));
                    Integer edificioId =Integer.parseInt(json_data.getString("Edificio_ID"));
                    Log.i(TAG, "RISULTATO: "+nome+", "+edificioId);
                    Stanza stanza = new Stanza(areaId,edificioId,nome,dimensioni);
                    stanzeSpinner.add(stanza);

                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return stanzeSpinner;
        }

        @Override
        protected void onPostExecute(ArrayList<Stanza> stanze) {
            super.onPostExecute(stanze);
            List<String> spinnerArray =  new ArrayList<String>();
            for (int i = 0; i < stanze.size(); i++) {         //Ciclo di estrazione oggetti
                Stanza stat = stanze.get(i);
                spinnerArray.add(stat.getNome());
                Log.i(TAG, "spinner Ambiente aggiunto");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    AreaActivity.this,R.layout.spinner_layout, spinnerArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            Spinner sItems = (Spinner) findViewById(R.id.area_spinner_stanza_id);
            sItems.setAdapter(adapter);
        }
    }


    private Edificio getEdFromString(String descrizione){
        for (int i = 0; i < edificiSpinner.size(); i++) {
            Edificio edd = edificiSpinner.get(i);
            String desc = edd.getCitta()+", "+edd.getIndirizzo();
            if(desc.compareTo(descrizione)==0)
                return edd;
        }
        return null;
    }
    private Stanza getStFromString(String descrizione){
        for (int i = 0; i < stanzeSpinner.size(); i++) {
            Stanza st = stanzeSpinner.get(i);
            if(st.getNome().compareTo(descrizione)==0)
                return st;
        }
        return null;
    }


}
