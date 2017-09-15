package com.example.jbhv12.dontburn;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by jbhv12 on 13/09/17.
 */

public class HomeFragment extends BaseFragment  implements  Response.Listener<String>, Response.ErrorListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private FloatingSearchView sourceSearchView, destinationSearchView;
    private boolean mDownloading = false;
    private String GETPLACESHIT = "places_hit";
    private VolleyJSONRequest request;
    private Handler handler;
    double latitude;
    double longitude;
    private PlacePredictions predictions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sourceSearchView = (FloatingSearchView) view.findViewById(R.id.search_source);
        destinationSearchView = (FloatingSearchView) view.findViewById(R.id.search_destination);

        //barGraph = (LinearLayout)view.findViewById(R.id.barGraph);

        //mSearchResultsList = (RecyclerView) view.findViewById(R.id.search_results_list);

        setupFloatingSearch(sourceSearchView);
        setupFloatingSearch(destinationSearchView);
        //setupResultsList();
        setupDrawer();
        //sampleGraph();



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
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    sv.clearSuggestions();
                } else {

                    //this shows the top left circular progress
                    //you can call it where ever you want, but
                    //it makes sense to do it when loading something in
                    //the background.
                    sv.showProgress();


                            // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                            MainActivity.volleyQueueInstance.cancelRequestInQueue(GETPLACESHIT);

                            //build Get url of Place Autocomplete and hit the url to fetch result.
                            request = new VolleyJSONRequest(Request.Method.GET, getPlaceAutoCompleteUrl(sourceSearchView.getQuery()), null, null, HomeFragment.this, HomeFragment.this);

                            //Give a tag to your request so that you can use this tag to cancle request later.
                            request.setTag(GETPLACESHIT);

                            MainActivity.volleyQueueInstance.addToRequestQueue(request);


                    //simulates a query call to a data source
                    //with a new query.
                    List<LocationSuggestion> sample = new ArrayList<LocationSuggestion>();
                    sample.add(new LocationSuggestion("sample1"));
                    sample.add(new LocationSuggestion("sample2"));
                    sample.add(new LocationSuggestion("sample3"));


                    sv.swapSuggestions(sample);

                                    //let the users know that the background
                                    //process has completed
                    sv.hideProgress();


                }
                Log.d(TAG, "onSearchTextChanged()");
            }
        });
        sv.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                Toast.makeText(getActivity(), "suggestion clicked" + searchSuggestion.getBody(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "focus" + sv.getQuery(), Toast.LENGTH_SHORT).show();
                sv.bringToFront();
//                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)sv.getLayoutParams();
//                params.setMargins(0,10,0,0);
//                sv.setLayoutParams(params);
            }

            @Override
            public void onFocusCleared() {

                //set the title of the bar so that when focus is returned a new query begins
                //sv.setSearchBarTitle(mLastQuery);
                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //mSearchView.setSearchText(searchSuggestion.getBody());

                Toast.makeText(getActivity(), "focus Cleared" + sv.getQuery(), Toast.LENGTH_SHORT).show();

            }
        });
        sv.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                if (item.getItemId() == R.id.action_location) {
                    Toast.makeText(getActivity(), "location", Toast.LENGTH_SHORT).show();
                } else if(item.getItemId() == R.id.action_voice_rec) {
                    Toast.makeText(getActivity(), "voice", Toast.LENGTH_SHORT).show();
                }
            }
        });
        sv.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                LocationSuggestion colorSuggestion = (LocationSuggestion) item;
                //history,saved,location icon
            }

        });

        //TODO: setTransaltionYmethod & setonclearsearchaction
    }

    private void setupDrawer() {
        attachSearchViewActivityDrawer(sourceSearchView);
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

        //searchBtn.setVisibility(View.VISIBLE);

    }
    @Override
    public void onResponse(String response) {


//        searchBtn.setVisibility(View.VISIBLE);
        Log.d("PLACES RESULT:::", response);
        Gson gson = new Gson();
        predictions = gson.fromJson(response, PlacePredictions.class);
        ArrayList<Place>  a = predictions.getPlaces();

        if(a.size()>0)Log.e("fiiinn",a.get(0).getPlaceDesc());
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

//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//    }
    @Override
    public void onConnected(Bundle bundle) {

        Log.d("onconnected","func call");
//        try {
//            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                    mGoogleApiClient);
//
//            if (mLastLocation != null) {
//                latitude = mLastLocation.getLatitude();
//                longitude = mLastLocation.getLongitude();
//            }
//
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    private void tryToFetchResults(){
        String sourceInputText = sourceSearchView.getQuery();
        String destinationInputText = destinationSearchView.getQuery();
        if(sourceInputText.length()>0 && destinationInputText.length()>0)
            ((MainActivity)getActivity()).startDownload(sourceInputText,destinationInputText);
    }
    public void test(){
        Log.e("frag","tessst");
    }







}
