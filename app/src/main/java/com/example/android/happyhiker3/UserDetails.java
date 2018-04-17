package com.example.android.happyhiker3;

/**
 * Created by Noor on 2018-04-02.
 */

public class UserDetails {
    public Double bpm;
    public Double lat;
    public Double lng;
    public Boolean distress;


    public UserDetails(){};

    public  UserDetails(Double lng, Double bpm, Double lat){
        this.bpm = bpm;
        this.lat = lat;
        this.lng = lng;
    }

    public  UserDetails(Boolean distress, Double lng, Double bpm, Double lat){
        this.bpm = bpm;
        this.lat = lat;
        this.lng = lng;
        this.distress = distress;
    }
}
