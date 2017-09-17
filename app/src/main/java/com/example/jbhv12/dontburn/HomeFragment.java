package com.example.jbhv12.dontburn;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.util.Util;
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
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

/**
 * Created by jbhv12 on 13/09/17.
 */

public class HomeFragment extends BaseFragment  implements  Response.Listener<String>, Response.ErrorListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private FloatingSearchView sourceSearchView, destinationSearchView;
    private int activeSearchView;
    private LinearLayout resultLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private SharedPreferences lh;
    public static final String PREFS_NAME = "LocationHistory";
    public ArrayList<PlaceSuggestion> locationHistory;

    private boolean mDownloading = false;     //TODO do something with this
    private String GETPLACESHIT = "places_hit";
    private VolleyJSONRequest request;
    private Handler handler;   //TODO study runable in java
    double latitude;
    double longitude;
    private PlacePredictions predictions;
    private String suggestionClicked;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;


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

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        setupFloatingSearch(sourceSearchView);
        setupFloatingSearch(destinationSearchView);
        setupDrawer();



        if(!isNetworkConnected()) {
            Snackbar.make(resultLayout, "No Network Connectivity", Snackbar.LENGTH_LONG)
                    .setAction("Open Settings", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent myIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(myIntent);
                            Log.e("lo  ", "snack");
                        }
                    }).show();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initializeGoogleAPIClient();
        } else {

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOC);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            } else {
                initializeGoogleAPIClient();
            }
        }

        lh = getActivity().getSharedPreferences(PREFS_NAME, 0);

//        String json = lh.getString("historyArray", "");
//
//        Type type = new TypeToken<List<PlaceSuggestion>>(){}.getType();
//        Gson gson = new Gson();
//        locationHistory = gson.fromJson(json, type);
//        if(locationHistory==null) locationHistory=new ArrayList<PlaceSuggestion>();

    }
    @Override
    public boolean onActivityBackPress() {
        //if mSearchView.setSearchFocused(false) causes the focused search
        //to close, then we don't want to close the activity. if mSearchView.setSearchFocused(false)
        //returns false, we know that the search was already closed so the call didn't change the focus
        //state and it makes sense to call supper onBackPressed() and close the activity
        if (!sourceSearchView.setSearchFocused(false)) {
            return false;
        }
        return true;
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
                            // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                            MainActivity.volleyQueueInstance.cancelRequestInQueue(GETPLACESHIT);

                            //build Get url of PlaceSuggestion Autocomplete and hit the url to fetch result.
                            request = new VolleyJSONRequest(Request.Method.GET, getPlaceAutoCompleteUrl(sv.getQuery()), null, null, HomeFragment.this, HomeFragment.this);

                            //Give a tag to your request so that you can use this tag to cancle request later.
                            request.setTag(GETPLACESHIT);

                            MainActivity.volleyQueueInstance.addToRequestQueue(request);

                    sv.hideProgress();
                }
                Log.d(TAG, "onSearchTextChanged()");
            }
        });
        sv.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                suggestionClicked = searchSuggestion.getBody();
                Toast.makeText(getActivity(), "suggestion clicked " + searchSuggestion.getBody(), Toast.LENGTH_SHORT).show();
                sv.setSearchBarTitle(searchSuggestion.getBody());

                sv.clearSearchFocus();
                //sv.clearSuggestions();

                //sv.clearSearchFocus();

                tryToFetchResults();
            }
            @Override
            public void onSearchAction(String query) {
                Toast.makeText(getActivity(), "search clicked" + query, Toast.LENGTH_SHORT).show();
                tryToFetchResults();
            }
        });
        sv.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {

                //show suggestions when search bar gains focus (typically history suggestions)
                Toast.makeText(getActivity(), "focus " + sv.getQuery(), Toast.LENGTH_SHORT).show();
                sv.bringToFront();
//                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)sv.getLayoutParams();
//                params.setMargins(0,10,0,0);
//                sv.setLayoutParams(params);
                if(sv.getId() == sourceSearchView.getId()) {
                    sv.swapSuggestions(new PlaceSuggestionHistoryHelper(getActivity(),"sourceSeachHistory").getHistory());
                }else {
                    sv.swapSuggestions(new PlaceSuggestionHistoryHelper(getActivity(),"destinationSeachHistory").getHistory());
                }
                renderResults();
            }

            @Override
            public void onFocusCleared() {

                //set the title of the bar so that when focus is returned a new query begins
                //sv.setSearchBarTitle(mLastQuery);
                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //mSearchView.setSearchText(searchSuggestion.getBody());

                Toast.makeText(getActivity(), "focus Cleared " + sv.getQuery(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "location", Toast.LENGTH_SHORT).show();
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
                    sv.setSearchBarTitle(String.valueOf(latitude)+","+String.valueOf(longitude)); //TODO: make sure this order is correct
                } else if(item.getItemId() == R.id.action_voice_rec) {
                    activeSearchView = sv.getId();
                    Toast.makeText(getActivity(), "voice", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    // Specify the calling package to identify your application
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                            .getPackage().getName());

                    // Display an hint to the user about what he should say.
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "hint");

                    // Given an hint to the recognizer about what the user is going to say
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

                    // If number of Matches is not selected then return show toast message


                    int noOfMatches = 2;
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
        urlString.append("&key=" + "AIzaSyDtAErhpx1IhGArwC-WRa_0BERbsu5EeAg");

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        //TODO toast
        //searchBtn.setVisibility(View.VISIBLE);

    }
    //TODO set also for destination searchview
    @Override
    public void onResponse(String response) {


//        searchBtn.setVisibility(View.VISIBLE);
        Log.d("PLACES RESULT:::", response);
        Gson gson = new Gson();
        predictions = gson.fromJson(response, PlacePredictions.class);
        ArrayList<PlaceSuggestion>  a = predictions.getPlaces();

        if(a.size()>0){
            Log.e("fiiinn",a.get(0).getPlaceDesc());
            if(activeSearchView == sourceSearchView.getId()) {
                sourceSearchView.swapSuggestions(a);
            }else {
                destinationSearchView.swapSuggestions(a);
            }
        }
//
//        if (mAutoCompleteAdapter == null) {
//            mAutoCompleteAdapter = new AutoCompleteAdapter(this, predictions.getPlaces(), PickLocationActivity.this);
//            mAutoCompleteList.setAdapter(mAutoCompleteAdapter);
//        } else {
//            mAutoCompleteAdapter.clear();
//            mAutoCompleteAdapter.addAll(predictions.getPlaces());
//            mAutoCompleteAdapter.notifyDataSetChanged();
//            mAutoCompleteList.invalidate();
//        }
    }



    @Override
    public void onConnected(Bundle bundle) {

        Log.d("onconnected","func call");
        if(isLocationEnabled()) Log.e("locset","enavle");
        else askToTurnOnLocation();
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
    public void renderResults(){


        Log.e("frag","tessst");
        ArrayList<Leg> fakedata = new ArrayList<>();
        fakedata.add(new Leg(-2,20,1));
        fakedata.add(new Leg(-1,20,2));
        fakedata.add(new Leg(3,20,3));
        fakedata.add(new Leg(-2,20,6));

//        TextView textResult = new TextView(getActivity());
//        textResult.setText("fake text loooong");
//        textResult.setTextSize(100);
//        LinearLayout.LayoutParams textResultParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        textResultParams.setMargins(10,0,10,0);
//        textResultParams.gravity= Gravity.CENTER;
//        textResult.setLayoutParams(textResultParams);
//
//        CardView textCard = new CardView(getActivity());
//        textCard.setRadius(20);
//        LinearLayout.LayoutParams textCardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, textResultParams.height);
//        textCardParams.setMargins(10, 10, 10, 10);
//        textCard.setLayoutParams(textCardParams);
//
//        textCard.addView(textResult);
//
//        resultLayout.addView(textCard);
//
//
//
//        LinearLayout barGraph = new LinearLayout(getActivity());
//        barGraph.setOrientation(LinearLayout.HORIZONTAL);
//        barGraph.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,190));
//        ImageView barGraphFraction;
//        for(Leg leg : fakedata){
//            barGraphFraction = new ImageView(getActivity());
//
//            if(leg.direction<0) barGraphFraction.setImageResource(R.drawable.graph_shape1);
//            else if(leg.direction==0) barGraphFraction.setImageResource(R.drawable.graph_shape2);
//            else barGraphFraction.setImageResource(R.drawable.graph_shape3);
//            int barGraphWidht = resultLayout.getWidth();
//            int fractionWidth = (leg.time*barGraphWidht)/11;
//            barGraphFraction.setLayoutParams(new LinearLayout.LayoutParams(fractionWidth,190));
//            barGraphFraction.setScaleType(ImageView.ScaleType.CENTER_CROP);
//
//            barGraph.addView(barGraphFraction);
//        }
//        resultLayout.addView(barGraph);


        LayoutInflater inflater = getActivity().getLayoutInflater();
        View resultTemplate = inflater.inflate(R.layout.result_template,null);

        TextView t = (TextView)resultTemplate.findViewById(R.id.resut_text);
        t.setText("fake result");

        LinearLayout barGraph = (LinearLayout)resultTemplate.findViewById(R.id.bar_graph);
        ImageView barGraphFraction;
        for(Leg leg : fakedata){
            barGraphFraction = new ImageView(getActivity());

            if(leg.direction<0) barGraphFraction.setImageResource(R.drawable.graph_shape1);
            else if(leg.direction==0) barGraphFraction.setImageResource(R.drawable.graph_shape2);
            else barGraphFraction.setImageResource(R.drawable.graph_shape3);
            int barGraphWidht = resultLayout.getWidth();        //fix this
            int fractionWidth = (leg.time*barGraphWidht)/11;
            barGraphFraction.setLayoutParams(new LinearLayout.LayoutParams(fractionWidth,190));
            barGraphFraction.setScaleType(ImageView.ScaleType.CENTER_CROP);

            barGraph.addView(barGraphFraction);
        }

        LineChart lineChart = (LineChart)resultTemplate.findViewById(R.id.linechart);
        List<Entry> entries = new ArrayList<Entry>();
        for(Leg leg : fakedata) {
            entries.add(new Entry(leg.time,(float)leg.direction));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label");

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

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
                    Log.e("spch",textMatchList.get(0));
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
