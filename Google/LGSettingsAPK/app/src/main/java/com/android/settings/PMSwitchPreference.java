package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

public class PMSwitchPreference extends SwitchPreference {

    private String TAG = "PMSwitchPreference";

    private boolean mOnDivider = true;
    private Switch mSwitch;
    private PMEnabler mPMEnabler;
//    private Context mContext;

    public PMSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
//        this.mContext = context;
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        setDivider(false);
        mPMEnabler = new PMEnabler(context, new Switch(context));
    }

    public PMSwitchPreference(Context context) {
        this(context, null);
    }

    public void setDivider(boolean enable) {
        mOnDivider = enable;
    }

    public void setListener() {
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Log.i (TAG, "onBindView() is called");

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        mPMEnabler.setSwitch(mSwitch);

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
    }

    public void resume() {
        //notifyChanged();
        if (mPMEnabler != null) {
            Log.i (TAG, "resume() called");
            mPMEnabler.resume();
        }
    }

    public void pause() {
        if (mPMEnabler != null) {
            Log.i (TAG, "pause() called");
            mPMEnabler.pause();
        }
    }
}
