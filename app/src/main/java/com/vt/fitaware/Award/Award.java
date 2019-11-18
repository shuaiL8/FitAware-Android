package com.vt.fitaware.Award;

import android.graphics.Bitmap;

public class Award {

    private Bitmap image;
    private String name;
    private String steps;
    private String date;
    private String prize;


    public Award(Bitmap image, String name, String steps, String date, String prize) {
        super();
        this.image = image;
        this.name = name;
        this.steps = steps;
        this.date = date;
        this.prize = prize;

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

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }
}
