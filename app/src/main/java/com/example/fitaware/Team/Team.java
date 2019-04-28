package com.example.fitaware.Team;

import android.graphics.Bitmap;

public class Team {

    private Bitmap image;
    private String name;
    private String captain;
    private String rank;


    public Team(Bitmap image, String name, String captain, String rank) {
        super();
        this.image = image;
        this.name = name;
        this.captain = captain;
        this.rank = rank;

    }



    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaptain() {
        return captain;
    }

    public void setCaptain(String date) {
        this.captain = captain;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
