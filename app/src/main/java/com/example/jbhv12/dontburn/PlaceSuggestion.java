package com.example.jbhv12.dontburn;


import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by Kyra on 1/11/2016.
 */
public class PlaceSuggestion implements SearchSuggestion {

    private String place_id;
    private String description;

    public PlaceSuggestion(String suggestion) {
        this.description = suggestion;
    }

    public PlaceSuggestion(Parcel source) {
        this.description = source.readString();
        //this.mIsHistory = source.readInt() != 0;
    }

//    public void setIsHistory(boolean isHistory) {
//        this.mIsHistory = isHistory;
//    }

//    public boolean getIsHistory() {
//        return this.mIsHistory;
//    }
//
    @Override
    public String getBody() {
        return description;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(mLocation);
//        dest.writeInt(mIsHistory ? 1 : 0);
    }
    public static final Creator<PlaceSuggestion> CREATOR = new Creator<PlaceSuggestion>() {
        @Override
        public PlaceSuggestion createFromParcel(Parcel in) {
            return new PlaceSuggestion(in);
        }

        @Override
        public PlaceSuggestion[] newArray(int size) {
            return new PlaceSuggestion[size];
        }
    };

    public String getPlaceDesc() {
        return description;
    }

    public void setPlaceDesc(String placeDesc) {
        description = placeDesc;
    }

    public String getPlaceID() {
        return place_id;
    }

    public void setPlaceID(String placeID) {
        place_id = placeID;
    }

}
