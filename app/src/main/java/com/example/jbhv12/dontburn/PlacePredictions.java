package com.example.jbhv12.dontburn;

import java.util.ArrayList;

/**
 * Created by Kyra on 1/11/2016.
 */
public class PlacePredictions {

    public ArrayList<PlaceSuggestion> getPlaces() {
        return predictions;
    }

    public void setPlaces(ArrayList<PlaceSuggestion> placeSuggestions) {
        this.predictions = placeSuggestions;
    }

    private ArrayList<PlaceSuggestion> predictions;
}
