package com.example.jbhv12.dontburn;

import android.content.Context;
import android.app.Fragment;

import com.arlib.floatingsearchview.FloatingSearchView;

/**
 * Created by jbhv12 on 13/09/17.
 */

public abstract class BaseFragment extends Fragment {


    private BaseExampleFragmentCallbacks mCallbacks;

    public interface BaseExampleFragmentCallbacks{

        void onAttachSearchViewToDrawer(FloatingSearchView searchView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseExampleFragmentCallbacks) {
            mCallbacks = (BaseExampleFragmentCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BaseExampleFragmentCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    protected void attachSearchViewActivityDrawer(FloatingSearchView searchView){
        if(mCallbacks != null){
            mCallbacks.onAttachSearchViewToDrawer(searchView);
        }
    }

    public abstract boolean onActivityBackPress();
}
