package com.example.fitaware.Award;

import android.graphics.Bitmap;

public class Award {

    private Bitmap image;
    private String name;
    private String date;

    public Award(Bitmap image, String name, String date) {
        super();
        this.image = image;
        this.name = name;
        this.date = date;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
