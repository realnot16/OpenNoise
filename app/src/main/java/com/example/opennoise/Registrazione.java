package com.example.opennoise;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class Registrazione implements Parcelable {
    String email;
    String data;
    String edificio;
    String stanza;
    boolean microfono;
    String api;
    String channel;
    int idArea;

    public Registrazione(String email, String data, String edificio, String stanza, int idArea, boolean microfono) {
        this.email = email;
        this.data = data;
        this.edificio = edificio;
        this.stanza = stanza;
        this.microfono = microfono;
        this.idArea=idArea;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Registrazione(Parcel in) {
        email=in.readString();
        data=in.readString();
        edificio=in.readString();
        stanza= in.readString();
        idArea=in.readInt();
        microfono = in.readByte() != 0;
        if(microfono){
            api = in.readString();
            channel = in.readString();
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeString(data);
        parcel.writeString(edificio);
        parcel.writeString(stanza);
        parcel.writeInt(idArea);
        parcel.writeByte((byte) (microfono ? 1 : 0));
        if(microfono){
            parcel.writeString(api);
            parcel.writeString(channel);
        }


    }

    public static final Creator<Registrazione> CREATOR = new Creator<Registrazione>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Registrazione createFromParcel(Parcel in) {
            return new Registrazione(in);
        }

        @Override
        public Registrazione[] newArray(int size) {
            return new Registrazione[size];
        }
    };

    public int getIdArea() {
        return idArea;
    }

    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isMicrofono() {
        return microfono;
    }

    public void setMicrofono(boolean microfono) {
        this.microfono = microfono;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getEdificio() {
        return edificio;
    }

    public void setEdificio(String edificio) {
        this.edificio = edificio;
    }

    public String getStanza() {
        return stanza;
    }

    public void setStanza(String stanza) {
        this.stanza = stanza;
    }
}
