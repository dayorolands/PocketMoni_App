package com.sdk.pocketmonisdk.Model;

import java.util.Comparator;

public class RecyclerModel {
    int passImage;
    int textColor;
    String cardNo;
    String transAmt;
    String transTime;
    String respCode;
    String transType;
    String data;

    public RecyclerModel(int passImage, int textColor, String respCode, String cardNo, String transAmt, String transTime, String transType, String data) {
        this.passImage = passImage;
        this.textColor = textColor;
        this.cardNo = cardNo;
        this.transAmt = transAmt;
        this.transTime = transTime;
        this.respCode = respCode;
        this.transType = transType;
        this.data = data;
    }

    public RecyclerModel() {
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public int getPassImage() {
        return passImage;
    }

    public void setPassImage(int passImage) {
        this.passImage = passImage;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getTransAmt() {
        return transAmt;
    }

    public void setTransAmt(String transDate) {
        this.transAmt = transDate;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    //Use collections.sort to set this values
    public static Comparator<RecyclerModel> SortByName = new Comparator<RecyclerModel>() {
        @Override
        public int compare(RecyclerModel o1, RecyclerModel o2) {
            return 0; //o1.getDesc().compareTo(o2.getDesc());
        }
    };
}
