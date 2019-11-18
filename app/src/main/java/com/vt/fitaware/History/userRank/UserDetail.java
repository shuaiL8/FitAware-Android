package com.vt.fitaware.History.userRank;

public class UserDetail {
    private String mDate;
    private String mRank;
    private int mSteps;
    private String mName;
    private String mLikes;
    private String mChecked;
    private String mToken;


    public UserDetail(String mDate, String mName, String mRank, int mSteps, String mLikes, String mChecked, String mToken) {
        super();
        this.mDate = mDate;
        this.mRank = mRank;
        this.mSteps = mSteps;
        this.mName = mName;
        this.mLikes = mLikes;
        this.mChecked = mChecked;
        this.mToken = mToken;


    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmRank() {
        return mRank;
    }

    public void setmRank(String mRank) {
        this.mRank = mRank;
    }

    public int getmSteps() {
        return mSteps;
    }

    public void setmSteps(int mSteps) {
        this.mSteps = mSteps;
    }

    public String getmLikes() {
        return mLikes;
    }

    public void setmLikes(String mLikes) {
        this.mLikes = mLikes;
    }

    public String getmChecked() {
        return mChecked;
    }

    public void setmChecked(String mChecked) {
        this.mChecked = mChecked;
    }

    public String getmToken() {
        return mToken;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }

}

