package com.android.settings.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;

import com.android.settings.R;

public class PRISettingResult extends Activity {
    //private static final String TAG = "PRISettingResult";

    private TextView mResult;
    private TextView mMcc;
    private TextView mMnc;
    private TextView mImsi;
    private TextView mSpn;
    private TextView mGid;
    private TextView mMobileData;
    private TextView mDataRoaming;
    private TextView mDefaultLang;
    private TextView mDateFormat;
    private TextView mTimeFormat;
    private TextView mDefaultTimezone;
    private TextView mAutoTime;
    private TextView mScreenTimeout;
    private TextView mLocationallowed;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Intent intent = getIntent();

        setContentView(R.layout.setting_pri_result);

        mResult = (TextView)findViewById(R.id.result);
        mMcc = (TextView)findViewById(R.id.mcc);
        mMnc = (TextView)findViewById(R.id.mnc);
        mImsi = (TextView)findViewById(R.id.imsi);
        mSpn = (TextView)findViewById(R.id.spn);
        mGid = (TextView)findViewById(R.id.gid);
        mMobileData = (TextView)findViewById(R.id.mobiledata);
        mDataRoaming = (TextView)findViewById(R.id.dataroaming);
        mDefaultLang = (TextView)findViewById(R.id.defaultlanguage);
        mDateFormat = (TextView)findViewById(R.id.dateformat);
        mTimeFormat = (TextView)findViewById(R.id.timeformat);
        mDefaultTimezone = (TextView)findViewById(R.id.defaulttimezone);
        mAutoTime = (TextView)findViewById(R.id.autotime);
        mScreenTimeout = (TextView)findViewById(R.id.screentimeout);
        mLocationallowed = (TextView)findViewById(R.id.locationallowed);

        mResult.setText("" + intent.getStringExtra("RESULT"));
        mMcc.setText("" + intent.getStringExtra("PRI_MCC"));
        mMnc.setText("" + intent.getStringExtra("PRI_MNC"));
        mImsi.setText("" + intent.getStringExtra("PRI_IMSI"));
        mSpn.setText("" + intent.getStringExtra("PRI_SPN"));
        mGid.setText("" + intent.getStringExtra("PRI_GID"));
        mMobileData.setText("" + intent.getStringExtra("MOBILE_DATA"));
        mDataRoaming.setText("" + intent.getStringExtra("DATA_ROAMING"));
        mDefaultLang.setText("" + intent.getStringExtra("default_language"));
        mDateFormat.setText("" + intent.getStringExtra("DATE_FORMAT"));
        mTimeFormat.setText("" + intent.getStringExtra("TIME_12_24"));
        mDefaultTimezone.setText("" + intent.getStringExtra("default_timezone"));
        mAutoTime.setText("" + intent.getStringExtra("AUTO_TIME"));
        mScreenTimeout.setText("" + intent.getStringExtra("SCREEN_OFF_TIMEOUT"));
        mLocationallowed.setText("" + intent.getStringExtra("LOCATION_PROVIDERS_ALLOWED"));

        /*Log.d(TAG, "RESULT [ " + intent.getIntExtra("RESULT") + " ] PRI_MCC [ " + intent.getIntExtra("PRI_MCC") + " ]
            PRI_MNC [ " + intent.getIntExtra("PRI_MNC") + " ] ");
        Log.d(TAG, "PRI_IMSI [ " + intent.getIntExtra("PRI_IMSI") + " ] PRI_SPN [ " + intent.getIntExtra("PRI_SPN") + " ]
               PRI_GID [ " + intent.getIntExtra("PRI_GID") + " ] ");
            Log.d(TAG, "MOBILE_DATA [ " + intent.getIntExtra("MOBILE_DATA") + " ] DATA_ROAMING [ " + intent.getIntExtra("DATA_ROAMING") + " ]
                   default_language [ " + intent.getIntExtra("default_language") + " ] ");
            Log.d(TAG, "DATE_FORMAT [ " + intent.getIntExtra("DATE_FORMAT") + " ] TIME_12_24 [ " + intent.getIntExtra("TIME_12_24") + " ]
                   default_timezone [ " + intent.getIntExtra("default_timezone") + " ] ");
            Log.d(TAG, "AUTO_TIME [ " + intent.getIntExtra("AUTO_TIME") + " ] SCREEN_OFF_TIMEOUT [ " + intent.getIntExtra("SCREEN_OFF_TIMEOUT") + " ]
                   LOCATION_PROVIDERS_ALLOWED [ " + intent.getIntExtra("LOCATION_PROVIDERS_ALLOWED") + " ] "); */

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
