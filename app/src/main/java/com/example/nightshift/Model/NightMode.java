package com.example.nightshift.Model;

/**
 * Created by SonPham on 18/07/2019
 * Company : Gpaddy
 */
public class NightMode {
    private String name;
    private int avatar;
    private int avatarClick;
    private int colorTemperature;
    private boolean isChoose;
    private int red;
    private int green;
    private int blue;

    public NightMode(String name, int avatar, int avatarClick, int colorTemperature, boolean isChoose, int red, int green, int blue) {
        this.name = name;
        this.avatar = avatar;
        this.avatarClick = avatarClick;
        this.colorTemperature = colorTemperature;
        this.isChoose = isChoose;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public int getAvatarClick() {
        return avatarClick;
    }

    public void setAvatarClick(int avatarClick) {
        this.avatarClick = avatarClick;
    }

    public int getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(int colorTemperature) {
        this.colorTemperature = colorTemperature;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }
}
