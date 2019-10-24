package com.vt.fitaware.Setting;

public class Settings {

    private String sName;
    private boolean addSwitch;
    private boolean sSwitch;


    public Settings(String sName, boolean addSwitch, boolean sSwitch) {
        super();
        this.sName = sName;
        this.sSwitch = sSwitch;
        this.addSwitch = addSwitch;


    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public boolean getAddSwitch() {
        return addSwitch;
    }

    public void setAddSwitch(boolean addSwitch) {
        this.addSwitch = addSwitch;
    }

    public boolean getsSwitch() {
        return sSwitch;
    }

    public void setsSwitch(boolean sSwitch) {
        this.sSwitch = sSwitch;
    }
}
