package com.acer.paroyalty;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class AdData implements Parcelable {
    public String url;
    public String title;
    public String content;
    public String trackToken;
    public int displayMode;
    public int action;
    public String actionData;
    public long startDate;
    public long endDate;
    public int adId;
    public String rawJson;

    public AdData(String url, String title, String content, String trackToken,int displayMode, int action, String actionData,int adId ,long startDate, long endDate, String rawJson) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.trackToken = trackToken;
        this.displayMode = displayMode;
        this.action = action;
        this.actionData = actionData;
        this.startDate = startDate;
        this.endDate = endDate;
        this.adId = adId;
        this.rawJson = rawJson;
    }

    public AdData() {

    }

    public static AdData parseFromJson(JSONObject object) {
        try {
            JSONObject extendObject = new JSONObject(object.getString("extendData"));

            return new AdData(object.getString("url"), object.getString("title"), object.getString("content"), object.getString("trackToken"), extendObject.getInt("displayMode"), extendObject.getInt("action"), extendObject.getString("actionData"), object.getInt("adId"), object.getLong("adStartTimestamp"), object.getLong("adEndTimestamp"), object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        // 尚未很了解此用途，
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        // Parcel : 將指定的資料寫入包內( 您宣告的參數 )
        // 請依照順序擺放，不然無法對應到
        parcel.writeString(url); // String 型別
        parcel.writeString(title); // String 型別
        parcel.writeString(content); // String 型別
        parcel.writeString(trackToken); // String 型別
        parcel.writeInt(displayMode); // int 型別
        parcel.writeInt(action); // int 型別
        parcel.writeString(actionData); // String 型別
        parcel.writeInt(adId);
        parcel.writeLong(startDate);
        parcel.writeLong(endDate);
        parcel.writeString(rawJson);
    }


    public static final Parcelable.Creator<AdData> CREATOR = new Creator(){

        @Override
        public AdData[] newArray(int size) {

            return new AdData[size];
        }

        @Override
        public AdData createFromParcel(Parcel parcel) {

            AdData adData = new AdData();

            adData.setUrl(parcel.readString());
            adData.setTitle(parcel.readString());
            adData.setContent(parcel.readString());
            adData.setTrackToken(parcel.readString());
            adData.setDisplayMode(parcel.readInt());
            adData.setAction(parcel.readInt());
            adData.setActionData(parcel.readString());
            adData.setAdId(parcel.readInt());
            adData.setStartDate(parcel.readLong());
            adData.setEndDate(parcel.readLong());
            adData.setRawJson(parcel.readString());

            return adData;
        }
    };


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return title;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public String getTrackToken() {
        return trackToken;
    }


    public void setTrackToken(String trackToken) {
        this.trackToken = trackToken;
    }


    public int getDisplayMode() {
        return displayMode;
    }


    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }

    public int getAction() {
        return action;
    }


    public void setAction(int action) {
        this.action = action;
    }


    public String getActionData() {
        return actionData;
    }


    public void setActionData(String actionData) {
        this.actionData = actionData;
    }



    public long getStartDate() {
        return startDate;
    }


    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }


    public long getEndDate() {
        return endDate;
    }


    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }


    public long getAdId() {
        return adId;
    }


    public void setAdId(int adId) {
        this.adId = adId;
    }


    public String getRawJson() {
        return rawJson;
    }


    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public String toString() {
        return ("url:" + url + ", title=" + title + ", content:" + content+ ", displayMode:" +displayMode +", action=" + action + ", actionData=" + actionData + ",adId=" + adId + ",startdate=" + startDate + ", enddate=" + endDate +", json=" + rawJson);
    }
}