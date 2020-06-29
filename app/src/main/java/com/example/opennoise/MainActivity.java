package com.example.opennoise;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String LOGIN = "Login";
    private static final String LOGI = "LogStat";
    private static final String TAG = "Decibels";

    private boolean regStarted=false;

    //private DrawerLayout drawerLayout;
    private int pause=0;
    private Registrazione registrazione;

    String api;
    String channel;

    private Soglia soglia;
    private TextView pageDesciption;
    private int contSoglia=0;
    private int conta=0;

    private static final String FILENAME="registrations.txt";

    //NOTIFICHE
    private static final String CHANNEL_ID_AUDIO="3";
    private static final String CHANNEL_ID_NO_AUDIO="4";

    private boolean notificationState=false;

    //VARIABILI CATTURA SUONO LOCALE
    private SoundMeter mSensor;

    //VARIABILI CATTURA SUONO REMOTO
    private Button tara;

    //ON CREATE
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound_layout);
        initUI();
        setData();
        createNotificationChannel();
    }

    @Override
    public void onStart() {
        super.onStart();
        loginStatus();
        if(!regStarted){
            Log.i(TAG,"Registrazione avviata da onStart");
            startRegis();
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences logPreferences = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = logPreferences.edit();
        editor.putString("logged", "no");
        editor.commit();

        String textData= logPreferences.getString("logged","unlogged");
        Log.i(LOGI, "MainA: segnalo chiusura, stato:"+ textData);
    }

    //VERIFICO LOGIN
    private void loginStatus() {
        SharedPreferences prefs = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        String textData= prefs.getString("logged","unlogged");
        if(textData.compareTo("yes")!=0){
            Log.i(TAG, "Non sei loggato, redirect a LoginActivity");
            Log.i(LOGI, "MainA: Non sei loggato, redirect a LoginActivity, stato:"+ textData);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else{
            Log.i(TAG, "OK, LOGGATO");
            Log.i(LOGI, "MainActivity, loggato, stato: "+textData);
        }

    }

    //IMPOSTO DATI REGISTRAZIONE ED UI
    private void setData() {
        Bundle regBundle = getIntent().getBundleExtra("registrazione");
        //Controllo pancia dell'intent
        if (regBundle != null) {
            registrazione = regBundle.getParcelable("registrazione");
            Log.i(TAG, "mail: "+registrazione.getEmail());
            Log.i(TAG, "edif: "+registrazione.getEdificio());
            Log.i(TAG, "stanza: "+registrazione.getStanza());
            Log.i(TAG, "mic: "+registrazione.isMicrofono());

            Log.i(TAG, "Cerco soglia");

            pageDesciption = findViewById(R.id.sound_pageDescription_id);
            new getSoglia().execute(registrazione.getIdArea());


        } else {
            Log.i(TAG,"Accesso non previsto");
            //accesso non consentito direttamente su MainActivity
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initUI(){
        Log.i(TAG, "Avvio Applicazione");

        // TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        //IMPOSTO UN FONT PERSONALIZZATO
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/myriad_pro_regular.ttf");
        TextView dbTextView = findViewById(R.id.sound_dbText_id);
        dbTextView.setTypeface(type);
        tara = findViewById(R.id.sound_tara_button_id);

    }

    //RECUPERO SOGLIA
    private class getSoglia extends AsyncTask<Integer,Void,Soglia>{

        @Override
        protected Soglia doInBackground(Integer... integers) {
            Soglia sogliaAttuale=null;
            String url = "https://quietspace.altervista.org/getSoglia.php";
            String params = "";

            //Encoding parametri:

            try {
                Log.i(TAG, "Contatto il DB per ottenere la soglia");
                params="area=" + URLEncoder.encode(String.valueOf(integers[0]), "UTF-8");
                JSONArray jsonArray=ServerTask.askToServer(params,url);
                Log.i(TAG, "Ritorno");
                JSONObject json_data = jsonArray.getJSONObject(0);
                String descrizione = json_data.getString("descrizione");
                double valore = Double.parseDouble(json_data.getString("Valore"));
                Integer audioInt =Integer.parseInt(json_data.getString("Segnale_audio"));
                Integer iterazioni =Integer.parseInt(json_data.getString("Iterazioni_video"));
                String data =json_data.getString("Data");
                boolean audio=false;
                if(audioInt==1) audio=true;
                Log.i(TAG,"NOTIFICHE, audio: "+audio+" - iterazioni:"+iterazioni);
                Log.i(TAG, "RISULTATO SOGLIA: "+descrizione+", "+valore);
                sogliaAttuale = new Soglia(valore,descrizione,audio,iterazioni);

            }
            catch (Exception e) {
                e.printStackTrace();
            }



            return sogliaAttuale;
        }

        @Override
        protected void onPostExecute(Soglia s) {
            super.onPostExecute(s);
            soglia=s;
            String data="Data: "+registrazione.getData();
            String ambiente=registrazione.getEdificio()+". " +registrazione.getStanza();
            String sogl="Soglia Attuale: "+soglia.getValore()+" dB";
            s.setDescrizione(data+"\n"
                    +ambiente+".\n"
                    +sogl);
            pageDesciption.setText(soglia.getDescrizione());
        }
    }

    //RIMANDO ALLA TARA
    private void showADialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.sound_dialog_title);
        builder.setMessage(R.string.sound_dialog_text);
        builder.setPositiveButton(R.string.sound_dialog_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG,"Redirect a tara er prima tara");
                        Intent intent = new Intent(MainActivity.this, TaraActivity.class);
                        startActivity(intent);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    //GESTIONE NOTIFICHE
    private void notification(boolean audio){
        if(audio){
            NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_AUDIO)
                    .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                    .setContentTitle(getResources().getString(R.string.sound_notification_title))
                    .setContentText(getResources().getString(R.string.sound_notification_text))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getResources().getString(R.string.sound_notification_text)))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationManager.notify(1,builder.build());
        }
        else{
            NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_NO_AUDIO)
                    .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
                    .setContentTitle(getResources().getString(R.string.sound_notification_title))
                    .setContentText(getResources().getString(R.string.sound_notification_text))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getResources().getString(R.string.sound_notification_text)))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationManager.notify(1,builder.build());
        }

    }

    private void createNotificationChannel() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            CharSequence name = getString(R.string.sound_notification_channel_audio_name);
            String description = getString(R.string.sound_notification_channel_audio_description);
            Uri sound = Uri.parse("android.resource://" + getPackageName() + "/"+R.raw.sound_notification);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_AUDIO, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.WHITE);
            channel.setSound(sound,audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.sound_notification_channel_no_audio_name);
            String description = getString(R.string.sound_notification_channel_no_audio_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channelNA = new NotificationChannel(CHANNEL_ID_NO_AUDIO, name, importance);
            channelNA.setDescription(description);
            channelNA.enableLights(true);
            channelNA.setLightColor(Color.WHITE);
            channelNA.enableVibration(true);
            channelNA.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelNA);
        }
    }

    //SCELGO PROCEDURA DI REGISTRAZIONE
    private void startRegis(){
        if(registrazione.isMicrofono()){
            //INIZIO PROCEDURA MICROFONO REMOTO
            api =registrazione.getApi();
            channel=registrazione.getChannel();
            regStarted=true;
            Log.i(TAG,"Registrazione REMOTA avviata");
            regisRemoto();
            Log.i(TAG, "Bottone Tara disattivato");
            tara.setActivated(false);

            tara.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(MainActivity.this,"Funzione non attiva",Toast.LENGTH_SHORT).show();
                }
            });

        }
        else{
            //INIZIO PROCEDURA MICROFONO LOCALE
            if(SoundMeter.referenceQuiet==0|| SoundMeter.referenceModerate==0||SoundMeter.referenceLoud==0){
                //NECESSARIO EFFETTUARE TARA
                showADialog();
            }
            else {
                mSensor = new SoundMeter();
                regStarted=true;
                Log.i(TAG,"Registrazione avviata");
                regisLocale();
            }
        }



    }

    //REGISTRAZIONE MICROFONO LOCALE
    private void regisLocale(){
        mSensor.start();
        new ListenSound().execute();
    }

    private class ListenSound extends AsyncTask<String,Void,Double> {

        @Override
        protected Double doInBackground(String... strings) {
            try {
                Thread.sleep(4000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            double decibels = mSensor.getDecibels();
            mSensor.stop();
            return decibels;
        }

        @Override
        protected void onPostExecute(Double decib) {
            super.onPostExecute(decib);

            TextView dbTextView = findViewById(R.id.sound_dbText_id);
            DecimalFormat df = new DecimalFormat("##.00");
            String text = df.format(decib)+" dB";
            dbTextView.setText(text);
            updateUI(decib);
            checkSoglia(decib);
            regisLocale();
        }
    }


    //REGISTRAZIONE MICROFONO REMOTO
    private void regisRemoto(){
        new requestDecibel().execute(api,channel);
    }

    //TASK ASINCRONO PER RICHIESTA DECIBEL --> RENDERE INFINITO
    @SuppressLint("StaticFieldLeak")
    private class requestDecibel extends AsyncTask<String,Void,Double>{

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Double doInBackground(String... strings) {
            try {
                Thread.sleep(16000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            double decibel=-1;
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
                    decibel = Double.parseDouble(result);
                }

            }
            catch (Exception e){
                Log.e(TAG, "Error " + e.toString());
            }
            return decibel;
        }

        @Override
        protected void onPostExecute(Double result){
            TextView dbText = findViewById(R.id.sound_dbText_id);
            DecimalFormat df = new DecimalFormat("##.00");
            String text = df.format(result)+" dB";
            dbText.setText(text);
            updateUI(result);
            checkSoglia(result);
            regisRemoto();
        }
    }


    //VALUTAZIONE SOGLIA
    void checkSoglia(double decibels){
        if (decibels > soglia.getValore()) {

            if(contSoglia>soglia.getInterazioni()){
                if(soglia.isAudio()){
                    Log.i(TAG,"INVIO NOTIFICA AUDIO");
                    notification(true);
                    notificationState=true;
                }
            }
            if(!notificationState) {
                contSoglia++;
                conta++;
                String fileValue = conta + "-" + decibels + "\n";
                writeToFile(fileValue, this);
                Log.i(TAG, "INVIO NOTIFICA NO AUDIO, " + contSoglia);
                notification(false);
            }

            notificationState=false;
        }
        else
            contSoglia = 0;
    }
    //SCRITTURA FILE
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILENAME, Context.MODE_APPEND));
            outputStreamWriter.append(data);
            outputStreamWriter.close();
            Log.i(TAG,"MainActivity: File aggiornato correttamente");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    //AGGIORNO LA UI IN BASE AL VALORE OTTENUTO
    private void updateUI(Double db){
        String state="";
        if(db<40)
            state="quiet";
        else if(db<soglia.getValore())
            state="moderate";
        else
            state="loud";

        ImageView dbImage = findViewById(R.id.sound_dbImage_id);
        switch (state) {
            case "quiet":
                dbImage.setImageResource(R.drawable.ic_sound_db_quiet);
                return;
            case "moderate":
                dbImage.setImageResource(R.drawable.ic_sound_db_moderate);
                return;
            case "loud":
                dbImage.setImageResource(R.drawable.ic_sound_db_loud);
                return;
            default:
        }
    }



    //BOTTONI
    public void profile(View view){
        Log.i(TAG,"Redirect a profilo");
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    public void feedback(View view){
        Log.i(TAG,"Redirect a feedback");
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }
    public void tara(View view){
        Log.i(TAG,"Redirect a tara");
        Intent intent = new Intent(this, TaraActivity.class);
        startActivity(intent);
    }
    public void informazioni(View view){
        Log.i(TAG,"Redirect a informazioni");
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

}
