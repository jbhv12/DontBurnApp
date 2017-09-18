package com.example.jbhv12.dontburn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.vansuita.library.CheckNewAppVersion;

public class SplashActivity extends AppCompatActivity {
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;
        intent = new Intent(this, MainActivity.class);

        new CheckNewAppVersion(this).setOnTaskCompleteListener(new CheckNewAppVersion.ITaskComplete() {
            @Override
            public void onTaskComplete(CheckNewAppVersion.Result result) {

                final CheckNewAppVersion.Result bc = result;
                //

                if(!result.hasNewVersion()){
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(context);
                    }
                    builder.setTitle("Update available")
                            .setMessage("u must update")
                            .setPositiveButton("update", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    bc.openUpdateLink();
                                }
                            })
                            .setNegativeButton("close app", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    System.exit(0);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }else {
                    startActivity(intent);
                    finish();
                }
            }
        }).execute();
    }

}
