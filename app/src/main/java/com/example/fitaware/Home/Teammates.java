package com.example.fitaware.Home;

import android.graphics.Bitmap;

public class Teammates {

    private String tab;
    private Bitmap image;
    private String name;
    private String rank;
    private int steps;
    private String goal;
    private int duration;
    private int heartPoints;
    private int distance;
    private int calories;

    private String color;

    public Teammates(String tab, Bitmap image, String name, String rank, int steps, String goal, int duration, int heartPoints, int distance, int calories, String color) {
        super();
        this.tab = tab;
        this.image = image;
        this.name = name;
        this.rank = rank;
        this.steps = steps;
        this.goal = goal;
        this.duration = duration;
        this.heartPoints = heartPoints;
        this.distance = distance;
        this.calories = calories;
        this.color = color;

    }


    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getHeartPoints() {
        return heartPoints;
    }

    public void setHeartPoints(int heartPoints) {
        this.heartPoints = heartPoints;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
