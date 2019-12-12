package com.vt.fitaware.Home;

public class BarEntryDetail {

    private String mDate;
    private String mSteps;
    private String mDuration;
    private String mHeartPoints;
    private String mDistance;
    private String mCalories;


    public BarEntryDetail(String mDate, String mSteps, String mDuration, String mHeartPoints, String mDistance, String mCalories) {
        super();
        this.mDate = mDate;
        this.mSteps = mSteps;
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


    public String getmSteps() {
        return mSteps;
    }

    public void setmSteps(String mSteps) {
        this.mSteps = mSteps;
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
