package com.example.jbhv12.dontburn;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.NavigationView;
import android.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;

import java.util.List;

public class MainActivity extends Activity implements BaseFragment.BaseExampleFragmentCallbacks, NavigationView.OnNavigationItemSelectedListener, DownloadCallback<String>{

    private DrawerLayout drawerLayout;
    private NetworkFragment mNetworkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), "smartass.pythonanywhere.com");

        showFragment(new HomeFragment());
    }
    //TODO: implemetn onBackPressed


    @Override
    public void onAttachSearchViewToDrawer(FloatingSearchView searchView) {
        searchView.attachNavigationDrawerToMenuButton(drawerLayout);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.about:
                Toast.makeText(this.getApplicationContext(), "about--actually start fragment",
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return true;
        }
    }
    private void showFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
    }


    public void startDownload(String source, String destination){
        mNetworkFragment.startDownload(source,destination);
    }



    @Override
    public void updateFromDownload(String result) {
        // Update your UI here based on result of download.
        Log.e("wah--from main",result);
        FragmentManager fm = getFragmentManager();

        HomeFragment fragment = (HomeFragment)fm.findFragmentById(R.id.content_frame);
        fragment.test();
    }
    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
//        switch(progressCode) {
//            // You can add UI behavior for progress updates here.
//            case Progress.ERROR:
//            ...
//                break;
//            case Progress.CONNECT_SUCCESS:
//            ...
//                break;
//            case Progress.GET_INPUT_STREAM_SUCCESS:
//            ...
//                break;
//            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
//            ...
//                break;
//            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
//            ...
//                break;
//        }
    }
    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void finishDownloading() {
        //mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }

}
