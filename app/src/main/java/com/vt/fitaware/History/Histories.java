package com.vt.fitaware.History;


/**
 * Created by fredliu on 12/3/17.
 */

public class Histories {
    private String mDate;
    private String mRank;
    private String mSteps;
    private String mGoal;
    private String mDuration;
    private String mHeartPoints;
    private String mDistance;
    private String mCalories;


    public Histories(String mDate, String mRank, String mSteps, String mGoal, String mDuration, String mHeartPoints, String mDistance, String mCalories) {
        super();
        this.mDate = mDate;
        this.mRank = mRank;
        this.mSteps = mSteps;
        this.mGoal = mGoal;
        this.mDuration = mDuration;
        this.mHeartPoints = mHeartPoints;
        this.mDistance = mDistance;
        this.mCalories = mCalories;


    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }


    public String getmRank() {
        return mRank;
    }

    public void setmRank(String mRank) {
        this.mRank = mRank;
    }

    public String getmSteps() {
        return mSteps;
    }

    public void setmSteps(String mSteps) {
        this.mSteps = mSteps;
    }

    public String getmGoal() {
        return mGoal;
    }

    public void setmGoal(String mGoal) {
        this.mGoal = mGoal;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getmHeartPoints() {
        return mHeartPoints;
    }

    public void setmHeartPoints(String mHeartPoints) {
        this.mHeartPoints = mHeartPoints;
    }

    public String getmDistance() {
        return mDistance;
    }

    public void setmDistance(String mDistance) {
        this.mDistance = mDistance;
    }

    public String getmCalories() {
        return mCalories;
    }

    public void setmCalories(String mCalories) {
        this.mCalories = mCalories;
    }

}
