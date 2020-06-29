package com.example.opennoise;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private static final String LOGIN = "Login";
    private static final String LOGI = "LogStat";
    private static final String TAG = "Profile";



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
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
            Log.i(LOGI, "PrefA: Non sei loggato, redirect a LoginActivity, stato:"+ textData);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else
            Log.i(LOGI, "OK, LOGGATO, profileActivity, stato:"+textData);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.profile_toolbar_text);
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

    public void logout(View view){
        SharedPreferences logPreferences = getSharedPreferences(LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = logPreferences.edit();
        editor.putString("logged", "no");
        editor.commit();
        Log.i(TAG, "Logout, redirect a LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    public void privacy(View view){
        Log.i(TAG, "redirect a PrivacyActivity");
        Toast.makeText(this,"Visualizza impostazioni Privacy",Toast.LENGTH_SHORT).show();
    }

    public void changeP(View view){
        Log.i(TAG, "redirect a changePActivity");
        Toast.makeText(this,"Modifica la tua password",Toast.LENGTH_SHORT).show();
    }
    public void settings(View view){
        Log.i(TAG, "redirect a changeSettingsActivity");
        Toast.makeText(this,"Modifica le impostazioni di avviso",Toast.LENGTH_SHORT).show();
    }


}

