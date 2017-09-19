package com.example.jbhv12.dontburn;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

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

    public ArrayList<Leg> getLegArrayFromRawString(String raw){
        ArrayList<Leg> fakedata = new ArrayList<>();
//        fakedata.add(new Leg(-2,20,1));
//        fakedata.add(new Leg(-1,20,2));
//        fakedata.add(new Leg(3,20,3));
//        fakedata.add(new Leg(-2,20,6));

        //JSONObject json = new JSONObject(raw);
        return fakedata;

    }
}
