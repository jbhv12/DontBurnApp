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
            new getDirectionData().execute(serverURL,source,destination);
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
class getDirectionData extends AsyncTask<String,Integer,String>{

    private int timeoutConstant = 8000;
    private int STRING_MAX_LENGTH = 1000;


    protected String doInBackground(String ... urlAndParams){
     URL url = null;
     Uri.Builder builder = new Uri.Builder();
     builder.scheme("https")
             .authority(urlAndParams[0])
             .appendPath("lai_lidho")
             .appendQueryParameter("par1", urlAndParams[1])
             .appendQueryParameter("par2", urlAndParams[2]);
     try{
         String urlstring = builder.build().toString();
         url = new URL(urlstring);
     }catch (Exception e){
        Log.e("exection",e.toString());
     }
     InputStream stream = null;
     HttpsURLConnection connection = null;
     String result = null;
     try {
         connection = (HttpsURLConnection) url.openConnection();
         connection.setReadTimeout(timeoutConstant);
         connection.setConnectTimeout(timeoutConstant);
         connection.setRequestMethod("GET");

         connection.setDoInput(true);
         connection.connect();
         if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
             throw new IOException("HTTP error code: " + connection.getResponseCode());
         }
         stream = connection.getInputStream();
         if (stream != null) {
             // Converts Stream to String with max length of 1000.
             result = readStream(stream, STRING_MAX_LENGTH);
         }

     }catch (Exception e){
         Log.e("exection",e.toString());

     }finally {
         if (stream != null) {
             try{stream.close();}catch (Exception e){Log.e("exection",e.toString());}
         }
         if (connection != null) {
             connection.disconnect();
         }
     }
     return result;
 }
    protected void onPostExecute(String rawString) {
        ArrayList<Leg> legs = parse(rawString);
        if(legs.size()>0) renderResult(legs);
    }
    public String readStream(InputStream stream, int maxReadSize)
            throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }
    public ArrayList<Leg> parse(String raw){
        ArrayList<Leg> fakedata = new ArrayList<>();
        fakedata.add(new Leg(1,20,30));
        fakedata.add(new Leg(1,20,30));
        fakedata.add(new Leg(1,20,30));
        fakedata.add(new Leg(1,20,30));
        return fakedata;
    }
    public void renderResult(ArrayList<Leg> legs){
    }
}
