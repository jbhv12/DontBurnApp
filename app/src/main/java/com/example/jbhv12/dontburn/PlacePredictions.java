package com.example.jbhv12.dontburn;

import java.util.ArrayList;

/**
 * Created by Kyra on 1/11/2016.
 */
public class PlacePredictions {

    public ArrayList<Place> getPlaces() {
        return predictions;
    }

    public void setPlaces(ArrayList<Place> places) {
        this.predictions = places;
    }

    private ArrayList<Place> predictions;
}
