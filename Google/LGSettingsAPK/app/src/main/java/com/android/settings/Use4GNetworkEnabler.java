/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.telephony.TelephonyManager.SIM_STATE_READY;

import java.util.Observable;
import java.util.Observer;

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.Phone;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lge.OverlayUtils;
import android.provider.Settings.Global;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentQueryMap;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.content.ActivityNotFoundException;
import android.provider.Settings.System;
import android.provider.Settings.SettingNotFoundException;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

import com.lge.constants.SettingsConstants;
import com.android.settings.utils.LGSubscriptionManager;

public class Use4GNetworkEnabler implements CompoundButton.OnCheckedChangeListener {

    private final Context mContext;

    private static ContentQueryMap mContentQueryMap;
    private IntentFilter mIntentFilter;
    private static Observer mSettingsObserver;
    Cursor settingsCursor;
    private ConnectivityManager mCM;
    private static final boolean TEST_RADIOS = false;
    private static final String TEST_RADIOS_PROP = "test.radios";

    AlertDialog mAlertDialog;
    private Switch mSwitch;
    private static boolean isUse4gNetworkDataDisable = false;
    boolean fromTouch = false;

    private boolean mIsResume = false;

    static final String TAG = "Use4GNetworkEnabler";
    private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final String INTENT_KEY_ICC_STATE = "ss";
    private static final String INTENT_VALUE_ICC_READY = "READY";
    private final int mDO_TIMER_DELAY = 3000;
    private final Handler mTimerHandler = new Handler();

    private static long sSim1SubId;
    private static final int SIM_SLOT_1_SEL = 0;

    public Use4GNetworkEnabler(Context context, Switch switch_, PreferenceScreen pref) {
        mContext = context;
        mSwitch = switch_;
        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            if (OverlayUtils.isMultiSimEnabled()) {
                mIntentFilter.addAction(ACTION_SIM_STATE_CHANGED);
            }
        }
    }

    public void pause() {
        if (mIsResume) {
            mContext.unregisterReceiver(mReceiver);

            if (null != mContentQueryMap) {
                mContentQueryMap.deleteObserver(mSettingsObserver);
            }

            if (null != settingsCursor) {
                settingsCursor.close();
            }
            mIsResume = false;
            mTimerHandler.removeCallbacks(mDoHoldSettingsTimer);
            mUseDoHoldSettingsTimer = false;
        } else {
            SLog.i("skip pause(), Performing pause of activity that is not resumed.");
        }
    }

    public void resume() {

        if (mCM == null) {
            mCM = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (isAirplaneModeOn(mContext) || !hasReadyMobileRadio(mContext)) {
            if (mSwitch != null) {
                if ((false == isAirplaneModeOn(mContext))
                        && OverlayUtils.isMultiSimEnabled()
                        && false == OverlayUtils.isEmptySim(OverlayUtils.get_current_sim_slot(mContext))) {
                    mSwitch.setChecked(false);
                } else {
                    mSwitch.setChecked(false);
                }
                if (mSwitch.isEnabled()) {
                    mSwitch.setEnabled(false);
                }
            }
        } else {
            if (mSwitch != null) {
                if (!mSwitch.isEnabled()) {
                    mSwitch.setEnabled(true);
                }
                if (!fromTouch) {
                    mSwitch.setChecked(is4gNetworkDataEnabled());
                }
                updateButtonForUse4gNetwork();
            }
        }

        settingsCursor = mContext.getContentResolver().query(Global.CONTENT_URI, null,
                "(" + android.provider.Settings.Global.NAME + "=?)",
                new String[] { "use_4g_network_onoff" }, null);
        if (settingsCursor != null) {
            mContentQueryMap = new ContentQueryMap(settingsCursor,
                    android.provider.Settings.Global.NAME, true, null);
        }

        mSettingsObserver = new Observer() {
            public void update(Observable o, Object arg) {
                if (null == mContext) {
                    return;
                }
                boolean enabled = is4gNetworkDataEnabled();
                boolean isAirplaneMode = isAirplaneModeOn(mContext);
                boolean hasReadyMobile = hasReadyMobileRadio(mContext);
                SLog.i("mSettingsObserver hasReadyMobile = " + hasReadyMobile 
                    + ", isAirplaneMode = " + isAirplaneMode + ", enabled = " + enabled);
                if ((false == fromTouch) && (false == isAirplaneMode && true == hasReadyMobile)) {
                    if (mSwitch != null) {
                        mSwitch.setChecked(enabled);
                    }
                } else {
                    fromTouch = false;
                }
                updateButtonForUse4gNetwork();
            }
        };
        if (mContentQueryMap != null) {
            mContentQueryMap.addObserver(mSettingsObserver);
        }

        mContext.registerReceiver(mReceiver, mIntentFilter);
        mIsResume = true;

    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            return;
        }
        /*
        if (true == is4gNetworkDataEnabled()) {
            Global.putInt(mContext.getContentResolver(), "use_4g_network_onoff", 1);
        } else {
            Global.putInt(mContext.getContentResolver(), "use_4g_network_onoff", 0);
        }
        */
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);

        mSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
            }
        });

        mSwitch.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    fromTouch = true;
                }

                // TODO Auto-generated method stub
                return false;
            }
        });

        boolean isAirplaneMode = isAirplaneModeOn(mContext);
        boolean hasReadyMobile = hasReadyMobileRadio(mContext);
        boolean enabled = is4gNetworkDataEnabled();

        if (isAirplaneMode == true || hasReadyMobile == false) {
            if ((false == isAirplaneModeOn(mContext))
                    && OverlayUtils.isMultiSimEnabled()
                    && false == OverlayUtils.isEmptySim(OverlayUtils.get_current_sim_slot(mContext))) {
                mSwitch.setChecked(false);
            }
            mSwitch.setEnabled(false);
        } else {
            mSwitch.setChecked(enabled);
        }
        updateButtonForUse4gNetwork();
    }

    public void setSwitchChecked(boolean checked) {
        if (mSwitch == null) {
            fromTouch = false;
            return;
        }

        SLog.d (TAG, "setSwitchChecked(checked) = " + checked);
        if (mSwitch.isEnabled() == true) {
            fromTouch = true;
            mSwitch.setChecked(!checked);
        } else {
            fromTouch = false;
        }
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private boolean is4gNetworkDataEnabled() {
        int mValue = Settings.Global.getInt(
                        mContext.getContentResolver(),
                        "use_4g_network_onoff",
                        0);
        SLog.d("getUse4gSingleNetworkEnabled = " + mValue);
        return mValue == 0 ? false : true;
    }

    private void set4gNetworkEnabled(boolean enabled) {
        if (mSwitch == null) {
            return;
        }
        mSwitch.setChecked(enabled);
        if (true == enabled) {
            Global.putInt(mContext.getContentResolver(), "use_4g_network_onoff", 1);
        } else {
            Global.putInt(mContext.getContentResolver(), "use_4g_network_onoff", 0);
        }
        send4gNetworkIntent(enabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

        boolean dataEnabled = is4gNetworkDataEnabled();
        if (mSwitch == null) {
            return;
        }

        if (true == fromTouch) {
            SLog.d (TAG, "onCheckedChanged(checked) = " + dataEnabled);
            if (dataEnabled) {
                mSwitch.setChecked(false);
                set4gNetworkEnabled(false);
                //show4gNetworkEnablerDialog();
            } else {
                mSwitch.setChecked(true);
                set4gNetworkEnabled(true);
                //show4gNetworkEnablerDialog();
            }
            //fromTouch = false;
            mTimerHandler.postDelayed(mDoHoldSettingsTimer, mDO_TIMER_DELAY);
            SLog.d("mDoHoldSettingsTimer dimming start");
            mSwitch.setEnabled(false);
            mUseDoHoldSettingsTimer = true;
        } else {
            SLog.d (TAG, "onCheckedChanged fromTouch skip (checked) = " + dataEnabled);
        }
    }

    private void show4gNetworkEnablerDialog() {
        boolean enabled = mCM.getMobileDataEnabled();
        int strOk = android.R.string.ok;
        int strCancel = android.R.string.cancel;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.data_usage_enable_mobile);
        if (true == enabled) {
            builder.setMessage(R.string.data_usage_disable_mobile);
            isUse4gNetworkDataDisable = true;
        } else {
            builder.setMessage(R.string.sp_mobile_network_data_connection_charges_NORMAL);
            isUse4gNetworkDataDisable = false;
        }
        builder.setPositiveButton(
                strOk, 
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (isUse4gNetworkDataDisable == true) {
                            set4gNetworkEnabled(false);
                        } else {
                            set4gNetworkEnabled(true);
                        }
                    }
                });
        builder.setNegativeButton(
                strCancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (isUse4gNetworkDataDisable == true) {
                            set4gNetworkEnabled(true);
                        } else {
                            set4gNetworkEnabled(false);
                        }
                        mAlertDialog.dismiss();
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                if (isUse4gNetworkDataDisable == true) {
                    set4gNetworkEnabled(true);
                } else {
                    set4gNetworkEnabled(false);
                }
                mAlertDialog.dismiss();
            }
        });

        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                if (mSwitch == null) {
                    return;
                }
                boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.AIRPLANE_MODE_ON, 0) != 0;
                boolean hasReadyMobile = hasReadyMobileRadio(mContext);

                if (true == isAirplaneMode || false == hasReadyMobile) {
                    SLog.i("isAirplaneMode  = " + hasReadyMobile);
                    if ((false == isAirplaneModeOn(mContext))
                            && OverlayUtils.isMultiSimEnabled()
                            && false == OverlayUtils
                                    .isEmptySim(OverlayUtils.get_current_sim_slot(context))) {
                        mSwitch.setChecked(false);
                    } else {
                        mSwitch.setChecked(false);
                    }
                    mSwitch.setEnabled(false);
                } else {
                    mSwitch.setEnabled(true);
                    set4gNetworkEnabled(is4gNetworkDataEnabled());
                }
                updateButtonForUse4gNetwork();
            }

            if (ACTION_SIM_STATE_CHANGED.equals(action)) {
                String mStateExtra = intent.getStringExtra(INTENT_KEY_ICC_STATE);
                SLog.d("receiver [Extra] : " + mStateExtra + " == '" + INTENT_VALUE_ICC_READY + "' ??");
                if (mStateExtra != null && INTENT_VALUE_ICC_READY.equals(mStateExtra)) {
                    boolean isAirplaneMode = isAirplaneModeOn(mContext);
                    boolean hasReadyMobile = hasReadyMobileRadio(mContext);
                    SLog.i("receiver isAirplaneMode " + isAirplaneMode);
                    SLog.i("receiver hasReadyMobileRadio " + hasReadyMobile);
                    if (false == isAirplaneMode && hasReadyMobile) {
                        SLog.i("receiver enable === " + is4gNetworkDataEnabled());
                        if (mSwitch != null) {
                            if (!mSwitch.isEnabled()) {
                                mSwitch.setEnabled(true);
                            }
                            if (!fromTouch) {
                                mSwitch.setChecked(is4gNetworkDataEnabled());
                            }
                            updateButtonForUse4gNetwork();
                        }
                    }
                }
            }
        }
    };


    public static boolean hasReadyMobileRadio(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("mobile");
        }
        final ConnectivityManager conn = ConnectivityManager.from(context);
        final TelephonyManager tele = TelephonyManager.from(context);

        // require both supported network and ready SIM
        if (tele.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA
                && tele.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_FALSE) {
            SLog.i("hasReadyMobileRadio :: " + conn.isNetworkSupported(TYPE_MOBILE));
            return conn.isNetworkSupported(TYPE_MOBILE);
        } else {
            if (OverlayUtils.isMultiSimEnabled()) {
                SLog.i("hasReadyMobileRadio :: "
                        + OverlayUtils.getAvailableCurrNetworkSimState(context));
                return conn.isNetworkSupported(TYPE_MOBILE)
                        && OverlayUtils.getAvailableCurrNetworkSimState(context);
            } else {
                SLog.i("hasReadyMobileRadio :: " + (tele.getSimState() == SIM_STATE_READY));
                return conn.isNetworkSupported(TYPE_MOBILE)
                        && tele.getSimState() == SIM_STATE_READY;
            }
            //return conn.isNetworkSupported(TYPE_MOBILE) && tele.getSimState() == SIM_STATE_READY;
        }
    }

    //Set auto LTE when Data_enable set on
    private final String mSET_NETWORK_MODE = "SetNetworkMode_CT_LTE";

    private void send4gNetworkIntent(boolean mIsLteEnable) {
        sSim1SubId = LGSubscriptionManager.getSubIdBySlotId(SIM_SLOT_1_SEL);
        if (Config.isDataRoaming(sSim1SubId)) {
            SLog.d("send4gNetworkIntent :: skip to broadcast the intent SetNetworkMode_CT_LTE");
            return;
        }
        if (mIsLteEnable) {
            Intent intent = new Intent(mSET_NETWORK_MODE);
            intent.putExtra("NetworkType", 0);
            mContext.sendBroadcast(intent);
        } else {
            Intent intent = new Intent(mSET_NETWORK_MODE);
            intent.putExtra("NetworkType", 1);
            mContext.sendBroadcast(intent);
        }
    }
    
    public boolean getUse4gSingleNetworkEnabled()
    {
        int mValue = Settings.Global.getInt(
                        mContext.getContentResolver(),
                        "use_4g_single_data_network_onoff",
                        0);
        SLog.d("getUse4gSingleNetworkEnabled = " + mValue);
        return mValue == 0 ? false : true;
    }

    private void updateButtonForUse4gNetwork() {
        if (mUseDoHoldSettingsTimer || (mSwitch == null)) {
            SLog.d("updateButtonForUse4gNetwork mDoHoldSettingsTimer dimming skip");
            return;
        }
        boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        boolean hasReadyMobile = hasReadyMobileRadio(mContext);
        if (mCM == null) {
            mCM = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        SLog.d("updateButtonForUse4gNetwork = " + mCM.getMobileDataEnabled());
        if (true == isAirplaneMode || false == hasReadyMobile) {
            SLog.d("updateButtonForUse4gNetwork under mobile data off");
            mSwitch.setEnabled(false);
        } else {
            mSwitch.setEnabled(mCM.getMobileDataEnabled());
        }

        /*
        if (Config.isDataRoaming()) {
            if (mSwitch != null) {
                SLog.d("updateButtonForUse4gNetwork under roaming");
                // mSwitch.setEnabled(false);
            }
        }
        */
    }

    private boolean mUseDoHoldSettingsTimer = false;
    private Runnable mDoHoldSettingsTimer = new Runnable() {
        public void run() {
            /*
            boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
            boolean hasReadyMobile = hasReadyMobileRadio(mContext);
            mTimerHandler.removeCallbacks(mDoHoldSettingsTimer);
            SLog.d("mDoHoldSettingsTimer dimming end");
            if (mSwitch != null 
                && !(true == isAirplaneMode || false == hasReadyMobile)) {
                mSwitch.setEnabled(true);
            }
            */
            mUseDoHoldSettingsTimer = false;
            updateButtonForUse4gNetwork();
            SLog.d("mDoHoldSettingsTimer dimming end");
            fromTouch = false;
        }
    };
    
}
