package com.example.jbhv12.dontburn;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by jbhv12 on 13/09/17.
 */

public class HomeFragment extends BaseFragment {
    private FloatingSearchView sourceSearchView, destinationSearchView;
    public View v;
    //private LinearLayout barGraph;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
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

//        Brains b = new Brains();
//        b.getDataSet("vadodara","ahmedabd");
//        Log.e("call","bairns");
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

    private void tryToFetchResults(){
        String sourceInputText = sourceSearchView.getQuery();
        String destinationInputText = destinationSearchView.getQuery();
        new Brains().getDataSet(sourceInputText,destinationInputText);
    }

//    private void sampleGraph(){
//         ImageView i = new ImageView(getActivity());
//        i.setImageResource(R.drawable.graph_shape1);
//        i.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT, 1));
//
//        ImageView i2 = new ImageView(getActivity());
//        i2.setImageResource(R.drawable.graph_shape2);
//        i2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT, 1));
//        barGraph.addView(i);
//        barGraph.addView(i2);
//
//    }

}
