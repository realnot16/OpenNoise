package com.example.opennoise;
import java.io.IOException;
import android.media.MediaRecorder;
import android.util.Log;

public class SoundMeter {
    private static final String TAG = "Decibels";
    // This file is used to record voice
    static final private double EMA_FILTER = 0.6;

    public static final double QUIETLEVEL=30;
    public static final double MODERATELEVEL=60;
    public static final double LOUDLEVEL=70;

    public static double referenceQuiet=0;
    public static double referenceModerate=0;
    public static double referenceLoud=0;

    public static int limitQuiet;
    public static int limitModerate;
    public static int limitLoud;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    public void start() {

        if (mRecorder == null) {

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");

            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mRecorder.start();
            getDecibels();
            mEMA = 0.0;
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getDecibels() {
        if (mRecorder != null) {
            int amplitude = mRecorder.getMaxAmplitude();
            Log.i(TAG, "Calcolo i Decibels: " + mRecorder.getMaxAmplitude() + "  ,  " + amplitude);
            if(amplitude<limitQuiet)
                return 20 * Math.log10((double) (amplitude / referenceQuiet));
            else if(amplitude<limitModerate)
                return 20 * Math.log10((double) (amplitude /referenceModerate));
            else
                return 20 * Math.log10((double) (amplitude / referenceLoud));
        }
        else
            return 0;

    }


    public boolean setReferenceQuiet(){
        if (mRecorder != null) {
            limitQuiet = mRecorder.getMaxAmplitude()+1000;
            Log.i(TAG, "Imposto tara quiet: " + mRecorder.getMaxAmplitude() + "  ,  " + limitQuiet);
            referenceQuiet =(double) limitQuiet/(Math.pow(10,(double)QUIETLEVEL/20));
            return true;
        }
        else
            return false;
    }

    public boolean setReferenceModerate() {
        if (mRecorder != null) {
            limitModerate = mRecorder.getMaxAmplitude()+1000;
            Log.i(TAG, "Imposto tara moderate: " + mRecorder.getMaxAmplitude() + "  ,  " + limitModerate);
            referenceModerate =(double) limitModerate/(Math.pow(10,(double)MODERATELEVEL/20));
            return true;
        }
        else
            return false;
    }

    public boolean setReferenceLoud() {
        if (mRecorder != null) {
            limitLoud= mRecorder.getMaxAmplitude();
            Log.i(TAG, "Imposto tara loud: " + mRecorder.getMaxAmplitude() + "  ,  " + limitLoud);
            referenceLoud =(double) limitLoud/(Math.pow(10,(double)LOUDLEVEL/20));
            return true;
        }
        else
            return false;
    }
}
