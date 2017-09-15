package com.example.jbhv12.dontburn;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by jbhv12 on 13/09/17.
 */

public class LocationSuggestion implements SearchSuggestion {
    private String place_description;
    private String place_id;
    private boolean mIsHistory = false;

    public LocationSuggestion(String suggestion) {
        this.place_description = suggestion.toLowerCase();
    }

    public LocationSuggestion(Parcel source) {
        this.place_description = source.readString();
        this.mIsHistory = source.readInt() != 0;
    }

    public void setIsHistory(boolean isHistory) {
        this.mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return this.mIsHistory;
    }

    @Override
    public String getBody() {
        return place_description;
    }

    public String getPlaceID() {
        return place_id;
    }

    public static final Creator<LocationSuggestion> CREATOR = new Creator<LocationSuggestion>() {
        @Override
        public LocationSuggestion createFromParcel(Parcel in) {
            return new LocationSuggestion(in);
        }

        @Override
        public LocationSuggestion[] newArray(int size) {
            return new LocationSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(place_description);
        dest.writeInt(mIsHistory ? 1 : 0);
    }
}
