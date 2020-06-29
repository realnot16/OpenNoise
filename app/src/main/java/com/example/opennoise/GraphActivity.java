package com.example.opennoise;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {

    private static final String LOGIN = "Login";
    private static final String LOGI = "LogStat";
    private static final String TAG = "Informazioni";

    private static final String FILENAME="registrations.txt";

    private ArrayList<Float> decibels;
    private ArrayList<Integer> id;

    private TextView dailyAvg;
    private TextView dailyexc;
    private LineChart mChart;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_layout);
        initUI();
        decibels = new ArrayList<>();
        id = new ArrayList<>();
        readFromFile(this);
        setData();
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
            Log.i(LOGI, "OK, LOGGATO, GraphActivity, stato:"+textData);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.graph_toolbar_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);

        dailyAvg = findViewById(R.id.graph_day_value_id);
        dailyexc = findViewById(R.id.graph_eccessi_value_id);
        mChart = findViewById(R.id.graph_line_id);
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


    private void readFromFile(Context context) {

        try {
            InputStream inputStream = context.openFileInput(FILENAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    String[] tokens = receiveString.split("-");
                    id.add(Integer.parseInt(tokens[0]));
                    decibels.add(Float.parseFloat(tokens[1]));
                    Log.i(TAG,"RIGA: "+tokens[0]+" - "+tokens[1]);
                }
                inputStream.close();
                Log.i(TAG,"GraphActivity: Valori registrazione letti correttamente");
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Graph activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Graph activity", "Can not read file: " + e.toString());
        }

        return;
    }

    private void setData(){
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);
        String exc = String.valueOf(decibels.size());
        dailyexc.setText(exc);
        float avg=0;
        if(decibels.size()>0) {
            for (Float decibel : decibels) {
                avg += decibel;
            }
            avg = avg / decibels.size();
            dailyAvg.setText(avg + " dB");
        }
        else
            dailyAvg.setText("-");

        //GRAFICO
        LineDataSet lineDataSet;
        ArrayList<Entry> values = new ArrayList<>();
        for (int i=0; i<decibels.size();i++) {
            Log.i(TAG,"Aggiunta nuova entry: "+decibels.get(i));
            float dec = decibels.get(i);
            values.add(new Entry(i+1,decibels.get(i)));
        }

        LineDataSet set1;
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        }
        else {
            set1 = new LineDataSet(values, "Decibels");
            int mBlue = Color.argb(1,0,117,187);
            set1.setDrawIcons(false);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.DKGRAY);
            set1.setCircleColor(Color.DKGRAY);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            /*if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.DKGRAY);
            }*/
            set1.setFillColor(mBlue);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);
            mChart.setData(data);
        }


    }
}
