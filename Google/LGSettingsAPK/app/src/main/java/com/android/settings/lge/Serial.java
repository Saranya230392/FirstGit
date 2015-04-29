package com.android.settings.lge;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
//import com.android.settings.Utils;
import com.android.settings.lge.Regulatory_mark;
import com.android.settings.lgesetting.Config.Config;

public class Serial extends SettingsPreferenceFragment {
    private static final String LOG_TAG = "aboutphone # Serial";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate");
        Log.i(LOG_TAG, "Build.DEVICE:" + Build.DEVICE);
        //Log.i(LOG_TAG, "targetOperator: " + targetOperator);

        View view = inflater.inflate(R.layout.device_phone_info_regulatory, null);
        //        setContentView(R.layout.device_phone_info_regulatory);
        if ("DCM".equals(Config.getOperator())) {
            view.findViewById(R.id.regulatory_botton_text).setVisibility(View.GONE);
        }

        ImageView img_view = (ImageView)view.findViewById(R.id.regulatory_mark_img);

        Regulatory_mark.setRes_Regulatory(img_view);

        return view;

    }
}