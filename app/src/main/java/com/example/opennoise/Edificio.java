package com.example.opennoise;

public class Edificio {

    int id;
    String indirizzo;
    String citta;
    int cap;

    public Edificio(int id, String indirizzo, String citta, int cap) {
        this.id = id;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.cap = cap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public int getCap() {
        return cap;
    }

    public void setCap(int cap) {
        this.cap = cap;
    }
}
