package com.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class CustomImagePreference extends Preference {
    private MultiTaskingGuide mMultiTaskingGuide = null;
    public CustomImagePreference(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        //setLayoutResource(R.layout.custom_image_preference);
    }

    public CustomImagePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        Log.d("kimyow", "CustomImagePreference::onCreateView");
        if (mMultiTaskingGuide != null) {
            //mMultiTaskingGuide.cleanUp();
            //mMultiTaskingGuide = null;
            Log.d("kimyow", "CustomImagePreference::mMultiTaskingGuide.cleanUp()");
            return mMultiTaskingGuide.getView();
        }
        mMultiTaskingGuide = new MultiTaskingGuide(getContext());
        return mMultiTaskingGuide.getView();
    }
    public View getView() {
       // if (mMultiTaskingGuide == null) {
            mMultiTaskingGuide = new MultiTaskingGuide(getContext());
            Log.d("kimyow", "CustomImagePreference::getview()");
       // }
        return mMultiTaskingGuide.getView();
    }

    public void cleanup(){
        if (mMultiTaskingGuide != null) {
            mMultiTaskingGuide.cleanUp();
            mMultiTaskingGuide = null;
        }
    }
}
