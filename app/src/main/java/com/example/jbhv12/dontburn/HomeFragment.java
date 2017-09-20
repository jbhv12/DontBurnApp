package com.example.jbhv12.dontburn;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

/**
 * Created by jbhv12 on 13/09/17.
 */

public class HomeFragment extends BaseFragment  implements  Response.Listener<String>, Response.ErrorListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public FloatingSearchView sourceSearchView, destinationSearchView;
    private int activeSearchView;

    private LinearLayout resultLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public TextView resultText;
    private VolleyJSONRequest request;
    private Handler handler;
    double latitude;
    double longitude;

    private PlacePredictions predictions;
    private Legs legs;

    private String suggestionClicked;
    private String lastQuery = "";
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
    private static final String GETPLACESHIT = "places_hit";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sourceSearchView = (FloatingSearchView) view.findViewById(R.id.search_source);
        destinationSearchView = (FloatingSearchView) view.findViewById(R.id.search_destination);
        resultLayout = (LinearLayout) view.findViewById(R.id.result_layout);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.container);

        setupRefreshLayout();
        setupFloatingSearch(sourceSearchView);
        setupFloatingSearch(destinationSearchView);
        setupDrawer();

        checkNetworkConnectivity();
        initializeGoogleAppClient();
    }
    @Override
    public boolean onActivityBackPress() {  //TODO implement this
        //if mSearchView.setSearchFocused(false) causes the focused search
        //to close, then we don't want to close the activity. if mSearchView.setSearchFocused(false)
        //returns false, we know that the search was already closed so the call didn't change the focus
        //state and it makes sense to call supper onBackPressed() and close the activity
        if (sourceSearchView.isSearchBarFocused()) {
            sourceSearchView.clearSearchFocus();
            return false;
        }
        return true;
    }
    public void checkNetworkConnectivity(){
        if(!isNetworkConnected()) {
            Snackbar.make(resultLayout, R.string.network_not_connected, Snackbar.LENGTH_LONG) //TODO change length maybe
                    .setAction(R.string.open_network_setting, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(myIntent);
                        }
                    }).show();
        }
    }
    private void initializeGoogleAppClient(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initializeGoogleAPIClient();
        } else {
            if (!isLocationPermissionGranted()) {
                askForLocationPermission();
            } else {
                initializeGoogleAPIClient();
            }
        }
    }
    private void setupRefreshLayout(){
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                //Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        tryToFetchResults();
                    }
                }, 2000);       //TODO maybe change this?
            }
        });
    }
    private void setupFloatingSearch(final FloatingSearchView sv){
        sv.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener(){
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("") || newQuery.equals(suggestionClicked)) {
                    sv.clearSuggestions();
                } else {
                    activeSearchView = sv.getId();
                    sv.showProgress();

                    if(newQuery.length()>1){  //only qury when len >2      //TODO somehow optimize this?
                        Runnable run = new Runnable() {
                            @Override
                            public void run() {

                                // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                                MainActivity.volleyQueueInstance.cancelRequestInQueue(GETPLACESHIT);

                                //build Get url of PlaceSuggestion Autocomplete and hit the url to fetch result.
                                request = new VolleyJSONRequest(Request.Method.GET, getPlaceAutoCompleteUrl(sv.getQuery()), null, null, HomeFragment.this, HomeFragment.this);

                                //Give a tag to your request so that you can use this tag to cancle request later.
                                request.setTag(GETPLACESHIT);

                                MainActivity.volleyQueueInstance.addToRequestQueue(request);
                            }

                        };
                        if (handler != null) {
                            handler.removeCallbacksAndMessages(null);
                        } else {
                            handler = new Handler();
                        }
                        handler.postDelayed(run, 1000);  //TODO study this
                    }
                    sv.hideProgress();
                }
                Log.d(TAG, "onSearchTextChanged()");
            }
        });
        sv.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                suggestionClicked = searchSuggestion.getBody();
                lastQuery = suggestionClicked;
                //Toast.makeText(getActivity(), "suggestion clicked " + searchSuggestion.getBody(), Toast.LENGTH_SHORT).show();
                sv.setSearchBarTitle(suggestionClicked);
                sv.clearSearchFocus();
                tryToFetchResults();
            }
            @Override
            public void onSearchAction(String query) {
                lastQuery = query;
                //Toast.makeText(getActivity(), "search clicked" + query, Toast.LENGTH_SHORT).show();
                tryToFetchResults();
            }
        });
        sv.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                //Toast.makeText(getActivity(), "focus " + sv.getQuery(), Toast.LENGTH_SHORT).show();
                sv.bringToFront();
                if (lastQuery.length()!=0) sv.setSearchBarTitle(lastQuery); //TODO fix this
                if(sv.getId() == sourceSearchView.getId()) {
                    sv.swapSuggestions(new PlaceSuggestionHistoryHelper(getActivity(),"sourceSeachHistory").getHistory());
                }else {
                    sv.swapSuggestions(new PlaceSuggestionHistoryHelper(getActivity(),"destinationSeachHistory").getHistory());
                    //TODO float destination view to top with animation
                }
            }

            @Override
            public void onFocusCleared() {
                //Toast.makeText(getActivity(), "focus Cleared " + sv.getQuery(), Toast.LENGTH_SHORT).show();
                sv.clearSuggestions();

                if(sv.getId() == sourceSearchView.getId()) {
                    new PlaceSuggestionHistoryHelper(getActivity(),"sourceSeachHistory").addToHistory(sv.getQuery());
                }else {
                    new PlaceSuggestionHistoryHelper(getActivity(),"destinationSeachHistory").addToHistory(sv.getQuery());
                }
            }
        });
        sv.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                if (item.getItemId() == R.id.action_location) {
                    if(!isLocationPermissionGranted()) askForLocationPermission();
                    if(!isLocationEnabled()) askToTurnOnLocation();
                    if(isLocationPermissionGranted() && isLocationEnabled()){
                        try {
                            //TODO handle mglooglepai null error
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            if (mLastLocation != null) {
                                latitude = mLastLocation.getLatitude();
                                longitude = mLastLocation.getLongitude();
                            }
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        sv.setSearchBarTitle(String.valueOf(latitude)+","+String.valueOf(longitude));
                        tryToFetchResults();
                    }

                } else if(item.getItemId() == R.id.action_voice_rec) {
                    activeSearchView = sv.getId();
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    // Specify the calling package to identify your application
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                            .getPackage().getName());
                    // Display an hint to the user about what he should say.
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.voice_search_hint);

                    // Given an hint to the recognizer about what the user is going to say
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

                    // If number of Matches is not selected then return show toast message

                    int noOfMatches = 1;
                    // Specify how many results you want to receive. The results will be
                    // sorted where the first result is the one with higher confidence.

                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);

                    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);

                }
            }
        });
        sv.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                PlaceSuggestion placeSuggestion = (PlaceSuggestion) item;
                if (placeSuggestion.getIsHistory()) {
                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_history_black_24dp, null));

                    //Util.setIconColor(leftIcon, Color.parseColor(textColor));
                    leftIcon.setAlpha(.36f);
                } else {
                    //TODO set location icon here
                    leftIcon.setAlpha(0.0f);
                    leftIcon.setImageDrawable(null);
                }
                //TODO add <- icon at end
            }

        });

        //TODO: setTransaltionYmethod & setonclearsearchaction
    }

    private void setupDrawer() {
        attachSearchViewActivityDrawer(sourceSearchView);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    public String getPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&location=");
        urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        urlString.append("&radius=500&language=en");
        urlString.append("&key=" + "AIzaSyDtAErhpx1IhGArwC-WRa_0BERbsu5EeAg");      //TODO secure this

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("error","googlemaps error");
    }
    @Override
    public void onResponse(String response) {

        Log.d("PLACES RESULT:::", response);
        predictions = new Gson().fromJson(response, PlacePredictions.class);
        ArrayList<PlaceSuggestion>  a = predictions.getPlaces();

        try{
            if(a.size()>0){
                if(activeSearchView == sourceSearchView.getId()) {
                    sourceSearchView.swapSuggestions(a);
                }else {
                    destinationSearchView.swapSuggestions(a);
                }
            }
        }catch (Exception e){
            Log.e("exception",e.toString());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(isLocationEnabled()) {       //TODO break this
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);

                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                }

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        else askToTurnOnLocation();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
Log.e("connedcte","fail");
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission granted!
                    initializeGoogleAPIClient();

                } else {
                    // permission denied!
                    //TODO show alert
                    Toast.makeText(getActivity(), "Please grant permission for using this app!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    public void initializeGoogleAPIClient(){
        //Build google API client to use fused location
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private boolean isLocationPermissionGranted(){
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void askForLocationPermission(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOC);
    }
    private void askToTurnOnLocation() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private void tryToFetchResults(){
        String sourceInputText = sourceSearchView.getQuery();
        String destinationInputText = destinationSearchView.getQuery();
        if(sourceInputText.length()>0 && destinationInputText.length()>0)
            ((MainActivity)getActivity()).startDownload(sourceInputText,destinationInputText);
    }
    public void renderResults(String rawResult){
        //TODO do this
        ArrayList<Leg> realResults = null;

        Log.d("LEGS RESULT:::", rawResult);
        try {
            legs = new Gson().fromJson(rawResult,Legs.class);
            realResults = legs.getLegs();
//            Log.e("ya bithc",String.valueOf(realResults.get(0).dir_end));
//            Log.e("ya bithc",String.valueOf(realResults.get(0).distance));
//            Log.e("ya bithc",String.valueOf(realResults.get(0).duration));
//            Log.e("ya bithc",String.valueOf(realResults.get(0).dir_start));
        }catch (Exception e){
            Log.e("exp",e.toString());
        }

        resultLayout.removeAllViews();

        if(realResults == null){
            //TODO handle this
        }if(realResults.size() == 0){
            //TODO handle this
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View resultTemplate = inflater.inflate(R.layout.result_template,null);

        resultText = (TextView)resultTemplate.findViewById(R.id.resut_text);
        resultText.setText("fake result");


        LineChart lineChart = (LineChart)resultTemplate.findViewById(R.id.linechart);
        List<Entry> entries = new ArrayList<Entry>();
        float i=0;
        for(Leg leg : realResults) {
            entries.add(new Entry(i+leg.duration,(float)leg.dir_start));
            entries.add(new Entry(i+leg.duration,(float)leg.dir_end));
            i+=leg.duration;
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);


        LinearLayout barGraph = (LinearLayout)resultTemplate.findViewById(R.id.bar_graph);
        ImageView barGraphFraction;
        for(Leg leg : realResults){
            barGraphFraction = new ImageView(getActivity());

            if(leg.dir_start>0 && leg.dir_start<2) barGraphFraction.setImageResource(R.drawable.graph_shape1);
                //else if(leg.direction==0) barGraphFraction.setImageResource(R.drawable.graph_shape2);
            else barGraphFraction.setImageResource(R.drawable.graph_shape3);
            int barGraphWidht = resultLayout.getWidth();        //fix this
            int fractionWidth = (leg.duration*barGraphWidht)/(int)i;
            barGraphFraction.setLayoutParams(new LinearLayout.LayoutParams(fractionWidth,100));
            barGraphFraction.setScaleType(ImageView.ScaleType.CENTER_CROP);

            barGraph.addView(barGraphFraction);
        }

        resultLayout.addView((LinearLayout)resultTemplate.findViewById(R.id.result_template));
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)

            //If Voice recognition is successful then it returns RESULT_OK
            if(resultCode == RESULT_OK) {

                ArrayList<String> textMatchList = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if (!textMatchList.isEmpty()) {
                    if(activeSearchView == sourceSearchView.getId()){
                        sourceSearchView.setSearchBarTitle(textMatchList.get(0));
                    }else {
                        destinationSearchView.setSearchBarTitle(textMatchList.get(0));
                    }
                }
                //Result code for various error.
            }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
                showToastMessage("Audio Error");
            }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
                showToastMessage("Client Error");
            }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
                showToastMessage("Network Error");
            }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
                showToastMessage("No Match");
            }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
                showToastMessage("Server Error");
            }
        super.onActivityResult(requestCode, resultCode, data);
    }
    void showToastMessage(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
