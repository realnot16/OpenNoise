package com.example.opennoise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class TaraActivity extends AppCompatActivity {
    private static final String TAG = "Taratura";
    private static final String LOGIN = "Login";
    private static final String LOGI = "LogStat";


    //VARIABILI CATTURA SUONO LOCALE
    private static String fileName = null;
    private MediaRecorder taraRec=null;
    private SoundMeter mSensor;

    private boolean recording=false;
    private boolean taraQuiet=false;
    private boolean taraModerate=false;
    private boolean taraLoud=false;

    private final static int QUIET = 1;
    private final static int MODERATE = 2;
    private final static int LOUD = 3;

    private ImageView quietStatus;
    private ImageView moderateStatus;
    private ImageView loudStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tara_layout);
        initUI();
        mSensor = new SoundMeter();
    }

    @Override
    public void onStart() {
        super.onStart();
        loginStatus();
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.tara_toolbar_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);

        quietStatus = findViewById(R.id.tara_quiet_status_id);
        moderateStatus = findViewById(R.id.tara_moderate_status_id);
        loudStatus = findViewById(R.id.tara_loud_status_id);
    }

    private void loginStatus() {
        SharedPreferences prefs = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        String textData= prefs.getString("logged","unlogged");
        if(textData.compareTo("yes")!=0){
            Log.i(TAG, "Non sei loggato, redirect a LoginActivity");
            Log.i(LOGI, "PrefA: Non sei loggato, redirect a LoginActivity, stato:"+ textData);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else
            Log.i(LOGI, "OK, LOGGATO, profileActivity, stato:"+textData);
    }



    //INIZIO TARATURA
    public void setAmplitudeQuiet(View view){
        if(!recording){
            recording=true;
            mSensor.start();
            quietStatus.setImageResource(R.drawable.ic_more_horiz_24dp);
            new ListenSound().execute(QUIET);
        }
        else{
            Toast.makeText(this,"Registrazione ancora in corso!",Toast.LENGTH_SHORT).show();
        }

    }
    public void setAmplitudeModerate(View view){
        if(!recording){
            recording=true;
            mSensor.start();
            moderateStatus.setImageResource(R.drawable.ic_more_horiz_24dp);
            new ListenSound().execute(MODERATE);
        }
        else{
            Toast.makeText(this,"Registrazione ancora in corso!",Toast.LENGTH_SHORT).show();
        }
    }
    public void setAmplitudeLoud(View view){
        if(!recording){
            recording=true;
            mSensor.start();
            loudStatus.setImageResource(R.drawable.ic_more_horiz_24dp);
            new ListenSound().execute(LOUD);
        }
        else{
            Toast.makeText(this,"Registrazione ancora in corso!",Toast.LENGTH_SHORT).show();
        }
        return;
    }

    public void goToMain(View view){
        if(recording){
            Toast.makeText(this,"Registrazione ancora in corso!",Toast.LENGTH_SHORT).show();
        }
        else if(!taraQuiet){
            Log.i(TAG,"Livello basso non tarato");
            Toast.makeText(this,"Livello basso non tarato!",Toast.LENGTH_SHORT).show();
        }
        else if(!taraModerate){
            Log.i(TAG,"Livello medio non tarato");
            Toast.makeText(this,"Livello intermedio non tarato!",Toast.LENGTH_SHORT).show();
        }
        else if(!taraLoud){
            Log.i(TAG,"Livello alto non tarato");
            Toast.makeText(this,"Livello alto non tarato!",Toast.LENGTH_SHORT).show();
        }
        else{
            Log.i(TAG,"Redirect a MainActivity");
            finish();
        }



    }

    //AGGIORNO INTERFACCIA TARATURA
    private void updateUI(int i){
        if(i==QUIET){
            Log.i(TAG,"Aggiorno interfaccia quiet");
            taraQuiet=true;
            quietStatus.setImageResource(R.drawable.ic_check_24dp);
        }
        else if(i==MODERATE){
            Log.i(TAG,"Aggiorno interfaccia moderate");
            taraModerate=true;
            moderateStatus.setImageResource(R.drawable.ic_check_24dp);
        }
        else if (i==LOUD){
            Log.i(TAG,"Aggiorno interfaccia loud");
            taraLoud=true;
            loudStatus.setImageResource(R.drawable.ic_check_24dp);
        }
        else
            Log.i(TAG,"ACCESSO INASPETTATO");
    }


    private class ListenSound extends AsyncTask<Integer,Void,Integer> {

        @Override
        protected Integer doInBackground(Integer... ints) {
            Log.i(TAG,"Inizio Taratura");
            boolean status=true;
            try {
                Thread.sleep(6000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return 0;
            }
            if(ints[0]==QUIET){
                Log.i(TAG,"Taratura quiet..");
                status=mSensor.setReferenceQuiet();
                mSensor.stop();
                if(status)
                    return ints[0];
                else
                    return -ints[0];


            }
            else if(ints[0]==MODERATE){
                Log.i(TAG,"Taratura moderate..");
                status=mSensor.setReferenceModerate();
                mSensor.stop();
                if(status)
                    return ints[0];
                else
                    return -ints[0];
            }
            else if(ints[0]==LOUD){
                Log.i(TAG,"Taratura loud..");
                status=mSensor.setReferenceLoud();
                mSensor.stop();
                if(status)
                    return ints[0];
                else
                    return -ints[0];
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer status) {
            super.onPostExecute(status);
            if(status>0){
                Log.i(TAG,"Taratura terminata decentemente..");
                recording=false;
                Toast.makeText(TaraActivity.this,"Taratura terminata!",Toast.LENGTH_SHORT).show();
                updateUI(status);  //AGGIORNO INTERFACCIA CORRISPONDENTE
            }
            else if(status==0){
                Toast.makeText(TaraActivity.this,"Errore anomalo",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
