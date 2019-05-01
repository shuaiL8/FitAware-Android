package com.example.fitaware.Team;


/**
 * Created by fredliu on 12/3/17.
 */

public class Member {
    private String mRank;
    private String mName;
    private int mSteps;
    private String mGoal;
    private String mColor;

    public Member(String mRank, String mName, int mSteps, String mGoal, String mColor) {
        super();
//        this.deco = deco;
        this.mRank = mRank;
        this.mName = mName;
        this.mSteps = mSteps;
        this.mGoal = mGoal;
        this.mColor = mColor;


    }



    public String getmRank() {
        return mRank;
    }

    public void setmRank(String mRank) {
        this.mRank = mRank;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getmSteps() {
        return mSteps;
    }

    public void setmSteps(int mSteps) {
        this.mSteps = mSteps;
    }

    public String getmColor() {
        return mColor;
    }

    public void setmColor(String mColor) {
        this.mColor = mColor;
    }

    public String getmGoal() {
        return mGoal;
    }

    public void setmGoal(String mGoal) {
        this.mGoal = mGoal;
    }

}
