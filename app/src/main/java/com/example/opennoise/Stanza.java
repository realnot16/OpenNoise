package com.example.opennoise;

public class Stanza {
    int id;
    int edificio_id;
    String nome;
    float dimensioni;

    public Stanza(int id, int edificio_id, String nome, float dimensioni) {
        this.id = id;
        this.edificio_id = edificio_id;
        this.nome = nome;
        this.dimensioni = dimensioni;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEdificio_id() {
        return edificio_id;
    }

    public void setEdificio_id(int edificio_id) {
        this.edificio_id = edificio_id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public float getDimensioni() {
        return dimensioni;
    }

    public void setDimensioni(float dimensioni) {
        this.dimensioni = dimensioni;
    }
}
