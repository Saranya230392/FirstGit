package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

public class MirrorLinkSwitchPreference extends SwitchPreference {

    private String TAG = "MirrorLinkPref";

    private boolean mOnDivider = true;
    private Switch mSwitch;
    private MirrorLinkEnabler mMirrorLinkEnabler;

    public MirrorLinkSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mMirrorLinkEnabler = new MirrorLinkEnabler(context, new Switch(context));
    }

    public MirrorLinkSwitchPreference(Context context) {
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

        Log.i(TAG, "MirrorLinkSwitchPreference - onBindView() is called");

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);
        mMirrorLinkEnabler.setSwitch(mSwitch, false);

        if (!mOnDivider) {
            View vertical_divider = view.findViewById(R.id.switchImage);
            vertical_divider.setVisibility(View.INVISIBLE);
        }
    }

    public void resume() {
        //notifyChanged();
        if (mMirrorLinkEnabler != null) {
            Log.i(TAG, "resume() called");
            mMirrorLinkEnabler.resume();
        }
    }

    public void pause() {
        if (mMirrorLinkEnabler != null) {
            Log.i(TAG, "pause() called");
            mMirrorLinkEnabler.pause();
        }
    }
}
