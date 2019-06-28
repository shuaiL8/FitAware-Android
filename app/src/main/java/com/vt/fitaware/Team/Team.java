package com.example.fitaware.Team;

import android.graphics.Bitmap;

public class Team {

    private Bitmap image;
    private String name;
    private String captain;
    private String rank;
    private String goal;
    private int teamSteps;
    private String periodical;



    public Team(Bitmap image, String name, String captain, String rank, String goal, int teamSteps, String periodical) {
        super();
        this.image = image;
        this.name = name;
        this.captain = captain;
        this.rank = rank;
        this.goal = goal;
        this.teamSteps = teamSteps;
        this.periodical = periodical;


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

    public String getPeriodical() {
        return periodical;
    }

    public void setPeriodical(String periodical) {
        this.periodical = periodical;
    }


    public String getRank() {
        return rank;
    }

    public int getTeamSteps() {
        return teamSteps;
    }

    public void setTeamSteps(int teamSteps) {
        this.teamSteps = teamSteps;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }
}
