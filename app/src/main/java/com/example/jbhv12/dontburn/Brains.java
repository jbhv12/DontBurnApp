package com.example.jbhv12.dontburn;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jbhv12 on 14/09/17.
 */

public class Brains {

    private String serverURL = "smartass.pythonanywhere.com";
    private String TAG = "brains";

    public void getDataSet(String source, String destination){
        try{
            //new getDirectionData().execute(serverURL,source,destination);
        } catch (Exception e){

        }
    }
    public int getPercentageDistanceLeft(ArrayList<Leg> dataset){
        return 0;
    }
    public ArrayList<Leg> parse(String raw){
        ArrayList<Leg> fakedata = new ArrayList<>();
        fakedata.add(new Leg(1,20,30));
        fakedata.add(new Leg(1,20,30));
        fakedata.add(new Leg(1,20,30));
        fakedata.add(new Leg(1,20,30));
        return fakedata;
    }
}
