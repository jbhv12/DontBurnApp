package com.example.jbhv12.dontburn;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.arlib.floatingsearchview.FloatingSearchView;

/**
 * Created by jbhv12 on 14/09/17.
 */

public class ResultRenderer {
    private View main;
    public ResultRenderer(View activity){
        main = activity;
    }
    public void render(){
        Log.e("inRR","render");
        Log.e("work","f" + main.getMeasuredHeight());
    }
}
