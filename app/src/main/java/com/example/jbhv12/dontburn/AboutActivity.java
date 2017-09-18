package com.example.jbhv12.dontburn;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.klinker.android.sliding.SlidingActivity;

/**
 * Created by jbhv12 on 17/09/17.
 */

public class AboutActivity extends SlidingActivity {
    @Override
    public void init(Bundle savedInstanceState) {
        setTitle("About");
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.common_google_signin_btn_icon_dark);
        //setImage(R.drawable.common_google_signin_btn_icon_dark);
        setPrimaryColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark)
        );
        //disableHeader();
        //setContent(R.layout.activity_about);
    }
}
