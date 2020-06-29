package com.example.opennoise;

import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ServerTask {

    private static final String TAG = "SignupActivity";

    public ServerTask() {
    }

    public static JSONArray askToServer(String encodedRequest, String url ){
        JSONArray output=null;
        try {
            URL urlserver = new URL(url);
            HttpsURLConnection urlConnection = (HttpsURLConnection) urlserver.openConnection(); //apertura connessione
            urlConnection.setReadTimeout(1000);
            urlConnection.setConnectTimeout(1500);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(encodedRequest);
            dos.flush();
            dos.close();


            urlConnection.connect(); //connessione
            Log.i(TAG,encodedRequest);
            InputStream is = urlConnection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();//chiusura

            String result = sb.toString();
            Log.i(TAG, "risultato: "+ result);
            if(result.isEmpty()){ }
            else output=new JSONArray(result);
        }
        catch (Exception e) {
            Log.e(TAG, "Errore " + e.toString());
        }

        return output;

    }

}