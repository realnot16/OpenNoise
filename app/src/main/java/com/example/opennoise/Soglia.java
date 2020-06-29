package com.example.opennoise;

public class Soglia {

    double valore;
    String descrizione;
    boolean audio;
    int interazioni;

    public Soglia(double valore, String descrizione, boolean audio, int interazioni) {
        this.valore = valore;
        this.descrizione = descrizione;
        this.audio = audio;
        this.interazioni = interazioni;
    }

    public double getValore() {
        return valore;
    }

    public void setValore(double valore) {
        this.valore = valore;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public int getInterazioni() {
        return interazioni;
    }

    public void setInterazioni(int interazioni) {
        this.interazioni = interazioni;
    }
}
