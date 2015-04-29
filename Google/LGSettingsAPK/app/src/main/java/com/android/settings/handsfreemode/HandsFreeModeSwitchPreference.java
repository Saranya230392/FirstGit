package com.android.settings.handsfreemode;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class HandsFreeModeSwitchPreference extends SwitchPreference {

    //    public static final String IS_TIMED_SILENT = "Is_Timed_Silent";

    private final Listener mListener = new Listener();
    private Activity activity = null;
    private HandsFreeModeInfo mHandsFreeModeInfo;
    private Switch switchView;
    private static final String TAG = "HandsFreeModeSwitchPreference";
    private Intent mHandsFreeIntent;
    private static final int RESULT_HANDSFREE_MODE = 17;

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.i("soosin", "isCheced : " + isChecked);
            mHandsFreeModeInfo.setDBHandsFreeModeState(isChecked == true ? 1 : 0);
            setChecked(isChecked);
            if ("JP".equals(Config.getCountry()) && "KDDI".equals(Config.getOperator())) {
                mHandsFreeModeInfo.setDBHandsFreeModeCall(mHandsFreeModeInfo
                        .getDBHandsFreeModeState());
            }
            if (isChecked == true) {
                if (mHandsFreeModeInfo.isEmptyCheckHandsFreeMode()) {
                    jumptoHandsFreeMode();
                }
            }
        }
    }

    public HandsFreeModeSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandsFreeModeInfo = new HandsFreeModeInfo(context);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        // TODO Auto-generated constructor stub
        activity = (Activity)context;
        setSwitchTextOff("");
        setSwitchTextOn("");

    }

    public void setActivity(Activity _activity) {
        activity = _activity;
    }

    public void setCheckedUpdate() {
        setChecked(mHandsFreeModeInfo.getDBHandsFreeModeState() == 1 ? true : false);
    }

    public void setSwitchEnableStatus(boolean value) {
        if (switchView != null) {
            switchView.setEnabled(value);
        }
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);

        if (Utils.isUI_4_1_model(getContext())) {
            if (false == "KDDI".equals(Config.getOperator())) {
                setTitle(R.string.voice_notifications_title_changed);
            }
            if ("JP".equals(Config.getCountry())
                    && "DCM".equals(Config.getOperator())) {
                setTitle(R.string.voice_notifications_title);
                setSummary(R.string.hands_free_mode_read_out_readmessage_summary);
            }
        }
        View checkableView = view.findViewById(R.id.switchWidget);
        if (checkableView != null && checkableView instanceof Checkable) {
            ((Checkable)checkableView).setChecked(super.isChecked());

            if (checkableView instanceof Switch) {
                switchView = (Switch)checkableView;
                switchView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "setOnClickListener");
                    }
                });
                switchView.setOnCheckedChangeListener(mListener);
            }
        }
    }

    @Override
    protected void onClick() {
        // TODO Auto-generated method stub
        //super.onClick();
    }

    private void jumptoHandsFreeMode() {
        mHandsFreeIntent = new Intent("com.lge.settings.HANDSFREE_MODE_SETTING");
        activity.startActivityForResult(mHandsFreeIntent, RESULT_HANDSFREE_MODE);
    }
}
