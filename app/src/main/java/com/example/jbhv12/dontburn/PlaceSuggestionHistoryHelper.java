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
    private String arrayKey;

    public PlaceSuggestionHistoryHelper(Activity activity,String arrayKey){
        lh = activity.getSharedPreferences(PREFS_NAME, 0);
        this.arrayKey = arrayKey;
    }
    public ArrayList<PlaceSuggestion> getHistory(){

        ArrayList<PlaceSuggestion> locationHistory;

        String json = lh.getString(arrayKey, "");
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

        String json = lh.getString(arrayKey, "");
        Type type = new TypeToken<List<PlaceSuggestion>>(){}.getType();
        Gson gson = new Gson();
        locationHistory = gson.fromJson(json, type);

        if(locationHistory==null){      //first time
            locationHistory = new ArrayList<PlaceSuggestion>();
            locationHistory.add(placeSuggestion);
        }else {
            for(int i=0;i<locationHistory.size();i++){
                if(locationHistory.get(i).getPlaceDesc().equals(placeSuggestion.getPlaceDesc())){
                    locationHistory.remove(i);
                    break;
                }
            }
            locationHistory.add(0,placeSuggestion);
            if(locationHistory.size()>historylimit){
                for(int i=locationHistory.size()-1;i>historylimit-1;i--)
                locationHistory.remove(i);
            }
        }

        SharedPreferences.Editor prefsEditor = lh.edit();
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(locationHistory);
        prefsEditor.putString(arrayKey, json2);
        prefsEditor.commit();
    }

}
