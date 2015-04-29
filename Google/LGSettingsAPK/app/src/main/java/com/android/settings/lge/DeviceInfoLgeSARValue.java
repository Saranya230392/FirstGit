package com.android.settings.lge;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;

public class DeviceInfoLgeSARValue extends SettingsPreferenceFragment {

    public static final boolean DEBUG = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            int resultOption = Utils.getUsingSettings(settingStyle, getActivity());
            if (resultOption == Utils.sUSING_EASYSETTINGS) {
                getActivity().onBackPressed();
                return true;
            } else if (resultOption == Utils.sUSING_SETTINGS) {
                finish();
                return true;
            }
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootview = inflater.inflate(R.layout.device_info_sarvalue, null);
        String sarValue = SystemProperties.get("ro.lge.sar.value", "");
        Log.d("simple settings ", "sarValue : " + sarValue);
        if ("2".equals(sarValue)) {
            ImageView ivTop = (ImageView)mRootview.findViewById(R.id.sar_value_topimage);
            ivTop.setImageResource(R.drawable.img_sar_value_setting_02);
        }

        return getView(mRootview);
    }

    public View getView(View v) {
        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        LayoutInflater mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        populateViewForOrientation(mInflater, (ViewGroup)getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.device_info_sarvalue, viewGroup);
        String sarValue = SystemProperties.get("ro.lge.sar.value", "");
        if ("2".equals(sarValue)) {
            ImageView ivTop = (ImageView)v.findViewById(R.id.sar_value_topimage);
            ivTop.setImageResource(R.drawable.img_sar_value_setting_02);
        }
        getView(v);
    }
}
