package com.vt.fitaware.Team;

public class Team {

    private String name;
    private String captain;
    private String rank;
    private String goal;
    private int teamSteps;
    private String periodical;
    private String color;



    public Team(String name, String captain, String rank, String goal, int teamSteps, String periodical, String color) {
        super();
        this.name = name;
        this.captain = captain;
        this.rank = rank;
        this.goal = goal;
        this.teamSteps = teamSteps;
        this.periodical = periodical;
        this.color = color;


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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
