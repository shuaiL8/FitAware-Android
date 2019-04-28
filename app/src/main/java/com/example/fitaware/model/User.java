package com.example.fitaware.model;


public class User {

    private String id;
    private String email;
    private String password;
    private String newPassword;
    private String team;
    private String captain;
    private String currentSteps;
    private String goal;
    private String periodical;

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setCaptain(String captain) {
        this.captain = captain;
    }

    public void setCurrentSteps(String currentSteps) {
        this.currentSteps = currentSteps;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setPeriodical(String periodical) {
        this.periodical = periodical;
    }


    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getCurrentSteps() {
        return currentSteps;
    }

    public String getCaptain() {
        return captain;
    }

    public String getTeam() {
        return team;
    }

    public String getGoal() {
        return goal;
    }

    public String getPeriodical() {
        return periodical;
    }


}
