package com.example.fitaware.Home;

import android.graphics.Bitmap;

public class Teammates {

    private Bitmap image;
    private String name;
    private String rank;
    private int steps;
    private String goal;
    private String color;

    public Teammates(Bitmap image, String name, String rank, int steps, String goal, String color) {
        super();
        this.image = image;
        this.name = name;
        this.rank = rank;
        this.steps = steps;
        this.goal = goal;
        this.color = color;

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

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
