package com.android.settings;

import com.android.settings.R;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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

public class AdvancedCallingSwitchPreference extends SwitchPreference {

    private Context mContext;

    private final Listener mListener = new Listener();
    private Switch mSwitch;
    private boolean mSwitchEnabled = true;

    private static final String TAG = "AdvancedCallingSwitchPreference";
    private static final String mCALL_ORDER_PRIORITY = "call_order_priority";
    private ConnectivityManager mCm;

    private class Listener implements CompoundButton.OnCheckedChangeListener,
            View.OnClickListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {

            int value = isChecked ? 1 : 0;
            if ("advanced_calling_switch_pref".equals(getKey())) {
                Log.d(TAG, "onCheckedChanged - advanced_calling_switch_pref");
                Log.d(TAG, "value : " + value);
                if (false == mCm.getMobileDataEnabled()) {

                }

                int preState = AdvancedCalling.queryCallSettingValueByKey(
                        mContext, mCALL_ORDER_PRIORITY);
                if (preState != value) {
                    AdvancedCalling.updateCallSettingByKey(mContext, null,
                            isChecked ? 1 : 0, mCALL_ORDER_PRIORITY);
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

    public void setSwitchEnabled(boolean enabled) {
        mSwitchEnabled = enabled;
    }

    public AdvancedCallingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
        mContext = context;
        mCm = (ConnectivityManager)mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);

        mSwitch = (Switch)view.findViewById(R.id.switchWidget);

        if ("advanced_calling_switch_pref".equals(getKey())) {
            mSwitch.setChecked(AdvancedCalling.queryCallSettingValueByKey(
                    mContext, mCALL_ORDER_PRIORITY) > 0);
        }
        mSwitch.setOnCheckedChangeListener(mListener);
        mSwitch.setOnClickListener(mListener);
    }

}
