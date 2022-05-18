package com.etranzact.pocketmoni.ViewModel;

public class WelcomeViewHolderModel {
    String bigText;
    String smallText;
    int image;

    public WelcomeViewHolderModel(String bigText, String smallText, int image) {
        this.bigText = bigText;
        this.smallText = smallText;
        this.image = image;
    }

    public String getBigText() {
        return bigText;
    }

    public void setBigText(String boldText) {
        this.bigText = boldText;
    }

    public String getSmallText() {
        return smallText;
    }

    public void setSmallText(String smallText) {
        this.smallText = smallText;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
