package com.android.settings.sound;

import com.android.settings.R;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import com.lge.constants.SettingsConstants;
import android.provider.Settings;

public class SoundSwitchPreference extends SwitchPreference {

    private Context mContext;

    private final Listener mListener = new Listener();
    private Switch mSwitch;

    private class Listener implements CompoundButton.OnCheckedChangeListener,
            View.OnClickListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {

            int value = isChecked ? 1 : 0;

            if ("auto_compose".equals(getKey())) {
                int preState = Settings.System.getInt(
                        mContext.getContentResolver(),
                        SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED, 0);
                if (preState != value) {
                    Settings.System.putInt(mContext.getContentResolver(),
                            SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED, value);
                }
            }
        }

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
        }
    }

    public void setCheckedUpdate(boolean checked) {
        if (mSwitch != null) {
            mSwitch.setChecked(checked);
        }
    }


    public SoundSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mContext = context;

    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        mSwitch = (Switch)view.findViewById(R.id.switchWidget);

        if ("auto_compose".equals(getKey())) {
            mSwitch.setChecked(Settings.System.getInt(
                    mContext.getContentResolver(),
                    SettingsConstants.System.AUTO_COMPOSED_RINGTONES_ENABLED, 0) > 0);
        }
        mSwitch.setOnCheckedChangeListener(mListener);
        mSwitch.setOnClickListener(mListener);
    }

}
