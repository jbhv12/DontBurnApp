package com.example.jbhv12.dontburn;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jbhv12 on 17/09/17.
 */

public class PlaceSuggestionHistoryHelper {
    private SharedPreferences lh;
    public static final String PREFS_NAME = "LocationHistory";
    private static final int historylimit = 10;

    public PlaceSuggestionHistoryHelper(Activity activity){
        lh = activity.getSharedPreferences(PREFS_NAME, 0);
    }
    public ArrayList<PlaceSuggestion> getHistory(){

        ArrayList<PlaceSuggestion> locationHistory;

        String json = lh.getString("historyArray", "");
        Type type = new TypeToken<List<PlaceSuggestion>>(){}.getType();
        Gson gson = new Gson();
        locationHistory = gson.fromJson(json, type);

        ArrayList<PlaceSuggestion> results;
        if(locationHistory!=null) results = locationHistory;
        else results = new ArrayList<PlaceSuggestion>();

        return results;
    }
    public void addToHistory(String placeDescriptino){
        if(placeDescriptino.length()==0) return;
        PlaceSuggestion placeSuggestion = new PlaceSuggestion(placeDescriptino);
        placeSuggestion.setIsHistory(true);

        ArrayList<PlaceSuggestion> locationHistory;

        String json = lh.getString("historyArray", "");
        Type type = new TypeToken<List<PlaceSuggestion>>(){}.getType();
        Gson gson = new Gson();
        locationHistory = gson.fromJson(json, type);

        if(locationHistory==null){      //first time
            locationHistory = new ArrayList<PlaceSuggestion>();
            locationHistory.add(placeSuggestion);
        }else {
            locationHistory.add(0,placeSuggestion);
            if(locationHistory.size()>historylimit){
                for(int i=locationHistory.size()-1;i>historylimit-1;i--)
                locationHistory.remove(i);
            }
        }

        SharedPreferences.Editor prefsEditor = lh.edit();
        Gson gson2 = new Gson();
        String json2 = gson.toJson(locationHistory);
        prefsEditor.putString("historyArray", json2);
        prefsEditor.commit();
    }

}
