package com.example.fitaware.Home.Calendar;

import java.util.ArrayList;

class HomeCollection {
    public String date="";
    public String dur="";
    public String dist="";
    public String cal="";


    public static ArrayList<HomeCollection> date_collection_arr;
    public HomeCollection(String date, String dur, String dist, String cal){

        this.date = date;
        this.dur = dur;
        this.dist = dist;
        this.cal = cal;

    }
}
