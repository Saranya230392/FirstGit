package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;

import java.util.ArrayList;
import java.util.List;



public class AirplaneModeFragment extends PreferenceFragment implements OnCheckedChangeListener, Indexable {
    
    private final String TAG = "AirplaneModeFragment";
    
    private Switch mActionBarSwitch;
    private AirplaneModeEnabler_VZW mAirplaneModeEnabler_VZW;
    private SettingsBreadCrumb      mBreadCrumb;
    private TextView                textView_summary; 
    private Activity                activity;
//    private boolean                 isAirplaneMode;
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();

    private final BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(AirplaneModeEnabler_VZW.ACTION_UPDATE_MOBILE_NETWORKS)) {
//                if ( AirplaneModeEnabler_VZW.isAirplaneModeOn(activity) ) {
//                    textView_summary.setVisibility(View.VISIBLE);
//                    textView_summary.setText(activity.getString(
//                            R.string.sp_airplane_mode_summary_vzw_NORMAL));
//                } else {
//                    textView_summary.setVisibility(View.INVISIBLE);
//                }
//           }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        activity = getActivity();
       
        if ( activity == null ) {
            Log.e (TAG, "Context is null!!!");
            return;
        }
       
        mActionBarSwitch = new Switch(activity);
        mActionBarSwitch.setOnCheckedChangeListener(this);
        mActionBarSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
              Log.d (TAG, "setOnClickListener");
            }
        }); 
        
        if (activity instanceof PreferenceActivity) {
            //PreferenceActivity preferenceActivity = (PreferenceActivity)activity;
            final int padding = activity.getResources().getDimensionPixelSize(
                                                                R.dimen.action_bar_switch_padding);
            mActionBarSwitch.setPaddingRelative(padding, 0, padding, 0);
            
            // mBreadCrumb
            if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
                activity.getActionBar().setTitle(R.string.settings_label);
                activity.getActionBar().setDisplayOptions( ActionBar.DISPLAY_SHOW_CUSTOM, 
                                                           ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView( mActionBarSwitch,
                        new ActionBar.LayoutParams(
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                ActionBar.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER_VERTICAL | Gravity.END));
            }
        }
        mAirplaneModeEnabler_VZW = new AirplaneModeEnabler_VZW(activity, mActionBarSwitch, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.airplanemode_fragment, container, false);

        if (view != null) {
            textView_summary = (TextView)view.findViewById(R.id.airplane_mode_summary);
//            if ( AirplaneModeEnabler_VZW.isAirplaneModeOn(activity) ) {
//                textView_summary.setVisibility(View.VISIBLE);
                textView_summary.setText(activity.getString(
                        R.string.settings_airplane_mode_summary));
//            } else {
//                textView_summary.setVisibility(View.INVISIBLE);
//            }

            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-191][ID-MDM-424]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                MDMSettingsAdapter.getInstance().setDisallowAirplaneModeSummary_Tablet(activity,
                        textView_summary);
            }
            // LGMDM_END
            return view; //super.onCreateView(inflater, container, savedInstanceState);
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }
    
    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        Log.d (TAG," onCheckedChanged ");
        mAirplaneModeEnabler_VZW.setSwitch(mActionBarSwitch);
//        mAirplaneModeEnabler_VZW.setSwitchChecked(arg1);// setSwitch(mActionBarSwitch);
        
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        // rebestm - mBreadCrumb
        if ( SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mActionBarSwitch);
            }
            mActionBarSwitch.setOnClickListener(new OnClickListener() {
            @Override
                public void onClick(View arg0) {
                Log.d (TAG," onClick ");
                }
            });
        }
        if (mAirplaneModeEnabler_VZW != null) {
            mAirplaneModeEnabler_VZW.resume();
        }
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(AirplaneModeEnabler_VZW.ACTION_UPDATE_MOBILE_NETWORKS);
        activity.registerReceiver(mAirplaneModeReceiver, intentFilter);
    }
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // rebestm - mBreadCrumb
        if (mBreadCrumb != null) {
            mBreadCrumb.removeSwitch();
        }
        
        if (mAirplaneModeEnabler_VZW != null) {
            mAirplaneModeEnabler_VZW.pause();
        }
        
        activity.unregisterReceiver(mAirplaneModeReceiver);
    }
 
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }
            
            int visible = Config.supportAirplaneListMenu() == true ? 1 : 0;

            setSearchIndexData(context, "airplane_mode",
                    context.getString(R.string.airplane_mode), "main", null,
                    null, "android.intent.action.MAIN",
                    "com.android.settings.Settings$AirplaneModeFragment",
                    "com.android.settings", 1, null, null, null, visible, 0);
            return mResult;
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}