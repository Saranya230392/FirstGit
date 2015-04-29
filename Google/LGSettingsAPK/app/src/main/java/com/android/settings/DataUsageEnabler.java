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
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
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
import android.content.ContentResolver;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.settings.utils.LGSubscriptionManager;

import com.lge.constants.SettingsConstants;

public class DataUsageEnabler implements CompoundButton.OnCheckedChangeListener {

    private final Context mContext;

    private static ContentQueryMap mContentQueryMap;
    private IntentFilter mIntentFilter;
    private static Observer mSettingsObserver;
    Cursor settingsCursor;
    private TelephonyManager mTelephonyManager;

    AlertDialog mAlertDialog;
    private Switch mSwitch;
    private static boolean isDataDisable = false;
    boolean fromTouch = false;
    private boolean mIsResume = false;
    private boolean mIs_Ctc_Cn = "CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator());
    static final String TAG = "DataUsageEnabler";
    private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final String INTENT_KEY_ICC_STATE = "ss";
    private static final String INTENT_VALUE_ICC_READY = "READY";

    private static long sSim1SubId;
    private static final int SIM_SLOT_1_SEL = 0;

    public DataUsageEnabler(Context context, Switch switch_, PreferenceScreen pref) {
        mContext = context;
        mSwitch = switch_;

        if (mIntentFilter == null) {
            mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            //If in callings, DataEnabled preperence is dimmed
            if ("DCM".equals(Config.getOperator()) 
                || "VZW".equals(Config.getOperator())) {
                SLog.i("DataUsageEnabler::register the ACTION_PHONE_STATE_CHANGED for DCM");
                mIntentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            }
            if (OverlayUtils.isMultiSimEnabled()) {
                mIntentFilter.addAction(ACTION_SIM_STATE_CHANGED);
            }
        }
    }

    public void pause() {
        //td144251 :: Performing pause of activity that is not resumed.
        if (mIsResume) {
            //td132697 :: it is an exception processing for null.
            mContext.unregisterReceiver(mReceiver);

            if (null != mContentQueryMap) {
                mContentQueryMap.deleteObserver(mSettingsObserver);
            }

            if (null != settingsCursor) {
                settingsCursor.close();
            }
            mIsResume = false;
        } else {
            SLog.i("skip pause(), Performing pause of activity that is not resumed.");
        }
    }

    public void resume() {

        if (mTelephonyManager == null) {
            mTelephonyManager = TelephonyManager.from(mContext);
        }
        if (isAirplaneModeOn(mContext) || !Utils.hasReadyMobileRadio(mContext)) {
            if (mSwitch != null) {
                if ((false == isAirplaneModeOn(mContext))
                        && OverlayUtils.isMultiSimEnabled()
                        && false == OverlayUtils.isEmptySim(Utils.getCurrentDDS(mContext))) {
                    mSwitch.setChecked(false);
                    //mSwitch.setChecked(isMobileDataEnabled());
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
                //If in callings, DataEnabled preperence is dimmed
                if ("DCM".equals(Config.getOperator())) {
                    TelephonyManager tm = TelephonyManager.getDefault();
                    if ((tm != null) && (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE)) {
                        mSwitch.setEnabled(false);
                    }
                }
                if (!fromTouch) {
                    if (Utils.hasNetworkLocked(mContext)) {
                        mSwitch.setChecked(false);
                    } else {
                        mSwitch.setChecked(isMobileDataEnabled());
                    }
                }
                updateMobileDataSwitchState();
            }
        }

        settingsCursor = mContext.getContentResolver().query(Global.CONTENT_URI, null,
                "(" + android.provider.Settings.Global.NAME + "=?)",
                new String[] { Global.MOBILE_DATA }, null);
        if (settingsCursor != null) {
            mContentQueryMap = new ContentQueryMap(settingsCursor,
                    android.provider.Settings.Global.NAME, true, null);
        }

        mSettingsObserver = new Observer() {
            public void update(Observable o, Object arg) {

                if (null == mContext) {
                    return;
                }

                boolean enabled = getCurrentMobileDataEnabledDB();

                boolean isAirplaneMode = isAirplaneModeOn(mContext);
                boolean hasReadyMobile = Utils.hasReadyMobileRadio(mContext);

                if ((false == fromTouch) && (false == isAirplaneMode && true == hasReadyMobile)) {
                    if (mSwitch != null) {
                        if (Utils.hasNetworkLocked(mContext)) {
                            mSwitch.setChecked(false);
                        } else {
                            mSwitch.setChecked(enabled);
                        }
                    }
                    //setMobileDataEnabled(enabled);
                } else {
                    fromTouch = false;
                }
                updateMobileDataSwitchState();
            }
        };
        if (mContentQueryMap != null) {
            mContentQueryMap.addObserver(mSettingsObserver);
        }

        mContext.registerReceiver(mReceiver, mIntentFilter);
        //td144251 :: Performing pause of activity that is not resumed.
        mIsResume = true;

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-223][ID-MDM-167]
        // Must be located at the bottom!!!
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setDataEnablerSwitch(mSwitch);
        }
        // LGMDM_END

        sSim1SubId = LGSubscriptionManager.getSubIdBySlotId(SIM_SLOT_1_SEL);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            return;
        }

        setMobileDataEnabledDB(isMobileDataEnabled());

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

        mSwitch.setOnKeyListener(mSwitchOnKeyListener);

        boolean isAirplaneMode = isAirplaneModeOn(mContext);
        boolean hasReadyMobile = Utils.hasReadyMobileRadio(mContext);
        boolean enabled = getCurrentMobileDataEnabledDB();

        if (isAirplaneMode == true || hasReadyMobile == false) {
            if ((false == isAirplaneModeOn(mContext))
                    && OverlayUtils.isMultiSimEnabled()
                    && false == OverlayUtils.isEmptySim(Utils.getCurrentDDS(mContext))) {
                mSwitch.setChecked(false);
                //mSwitch.setChecked(enabled);
            }
            mSwitch.setEnabled(false);
        } else {
            if (Utils.hasNetworkLocked(mContext)) {
                mSwitch.setChecked(false);
            } else {
                mSwitch.setChecked(enabled);
            }
        }

        //If in callings, DataEnabled preperence is dimmed
        if ("DCM".equals(Config.getOperator())) {
            TelephonyManager tm = TelephonyManager.getDefault();
            if ((tm != null) && (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE)) {
                mSwitch.setEnabled(false);
            }
        }
        updateMobileDataSwitchState();
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-223][ID-MDM-167]
        // Must be located at the bottom!!!
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            MDMSettingsAdapter.getInstance().setDataEnablerSwitch(mSwitch);
        }
        // LGMDM_END
    }

    private void updateMobileDataSwitchState() {
        if (Config.SKT.equals(Config.getOperator())
                || Config.LGU.equals(Config.getOperator())
                || Config.KT.equals(Config.getOperator())) {
            if (Config.isDataRoaming()) {
                if (mSwitch != null) {
                    mSwitch.setEnabled(false);
                }
            }
        } else if (Config.VZW.equals(Config.getOperator())) {
            if (Utils.isDimmingMobileDataForVolte(mContext)) {
                if (mSwitch != null) {
                    mSwitch.setEnabled(false);
                }
            }
        }

        if (Config.LGU.equals(Config.getOperator()) && Utils.isInVideoCall()) {
            mSwitch.setEnabled(false);
        }
    }

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    private boolean isMobileDataEnabled() {
        return mTelephonyManager.getDataEnabled();
    }

    private void setMobileDataEnabled(boolean enabled) {
        if (Utils.hasNetworkLocked(mContext)) {
            mSwitch.setChecked(false);
            return;
        }
        mTelephonyManager.setDataEnabled(enabled);
        Utils.sendBroadcastDdsForMMS(mContext, enabled);
        mSwitch.setChecked(enabled);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-223][ID-MDM-167]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (com.lge.mdm.LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)
                    || !com.lge.mdm.LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
                return;
            }
        }
        // LGMDM_END

        if (true != enabled) {
            if ("KDDI".equals(Config.getOperator()) && Utils.isSupportedVolte(mContext)) {
                Intent intent = new Intent();
                intent.putExtra("is_roaming_onoff", false);
                intent.putExtra("change_networkmode", -1);
                intent.setAction("com.lge.settings.action.CHANGE_ROAMING_MODE");
                mContext.sendBroadcast(intent);
            }
        }        
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

        boolean dataEnabled = isMobileDataEnabled();

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-223][ID-MDM-167]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (com.lge.mdm.LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(null)
                    || !com.lge.mdm.LGMDMManager.getInstance().getAllowMobileNetwork(null)) {
                return;
            }
        }
        // LGMDM_END

        if (true == fromTouch) {

            if (Utils.hasNetworkLocked(mContext)) {
                fromTouch = false;
                // add the toast popup as the usc requirement
                Toast.makeText(mContext, R.string.sp_unavailable_sim_datawarning_msg,
                            Toast.LENGTH_SHORT).show();
                mSwitch.setChecked(false);
                return;
            }

            if (dataEnabled) {
                mSwitch.setChecked(false);
                if ("VZW".equals(Config.getOperator())) {
                    if (Settings.Secure.getInt(mContext.getContentResolver(),
                            "vzw_data_off_enabled", 0) == 0) {
                        showDataEnablerDialog_VZW();
                    } else {
                        setMobileDataEnabled(false);
                    }
                } else if ("TLF".equals(Config.getOperator()) && Utils.isTlfSpainSim(mContext)) {
                    setMobileDataEnabled(false);
                } else if (true == mIs_Ctc_Cn) {
                    setMobileDataEnabled(false);
                } else {
                    showDataEnablerDialog();
                }
            } else {
                mSwitch.setChecked(true);
                if ("ATT".equals(Config.getOperator()) ||
                        "SKT".equals(Config.getOperator()) ||
                        "KT".equals(Config.getOperator()) ||
                        "LGU".equals(Config.getOperator()) ||
                        "KDDI".equals(Config.getOperator()) ||
                        "DCM".equals(Config.getOperator())) {
                    showDataEnablerDialog();
                } else if (true == mIs_Ctc_Cn) {
                    if (Config.isDataRoaming(sSim1SubId) == true) {
                        if (true == isRememberOptionChecked()) {
                            setMobileDataEnabled(true);
                        } else {
                            showDataEnablerDialog();
                            setRememberOptionChecked(true);
                        }
                    } else {
                        setRememberOptionChecked(false);
                        //showDataEnablerDialog();
                        setMobileDataEnabled(true);
                    }
                } else if ("TLF".equals(Config.getOperator()) && Utils.isTlfSpainSim(mContext)) {
                    if (isRememberOptionChecked()) {
                        setMobileDataEnabled(true);
                        android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                                SettingsConstants.Secure.PREFERRED_DATA_NETWORK_MODE,
                                (isMobileDataEnabled() ? 1 : 0));
                    } else {
                        //Create intent to DialogActivity java from ConnectionManagerWidget
                        Intent connectionDialogIntent = new Intent();
                        connectionDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        connectionDialogIntent.setClassName(
                                "com.lge.android.connectionmanager.widget",
                                "com.lge.android.connectionmanager.widget.DialogActivity");
                        try
                        {
                            mContext.startActivity(connectionDialogIntent);
                        } catch (ActivityNotFoundException e) {
                            //There is no widget installed
                            setMobileDataEnabled(true);
                        }
                    }
                } else {
                    setMobileDataEnabled(true);
                }
            }
            fromTouch = false;
        }
    }

    private boolean isRememberOptionChecked() {
        return (android.provider.Settings.System.getInt(
                mContext.getContentResolver(),
                SettingsConstants.System.DATA_CONNECTIVITY_POPUP_REMEMBER,
                0) == 1);
    }

    private void setRememberOptionChecked(boolean mValue) {
        android.provider.Settings.System.putInt(
            mContext.getContentResolver(),
            SettingsConstants.System.DATA_CONNECTIVITY_POPUP_REMEMBER, 
            mValue ? 1 : 0);
    }

    private void showDataEnablerDialog() {
        boolean enabled = mTelephonyManager.getDataEnabled();

        int strOk = android.R.string.ok;
        int strCancel = android.R.string.cancel;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if (false == Utils.isUI_4_1_model(mContext)) {
            builder.setIconAttribute(android.R.attr.alertDialogIcon);
            builder.setTitle(R.string.roaming_reenable_title);
        } else {
            builder.setTitle(R.string.data_usage_enable_mobile);
        }

        if (true == "SKT".equals(Config.getOperator())) {
            if (Utils.isUI_4_1_model(mContext)) {
                builder.setTitle(R.string.data_network_settings_title);
            }
            if (enabled) {
                builder.setMessage(R.string.sp_data_networks_disable_SKT_NORMAL);
                isDataDisable = true;
            } else {
                builder.setMessage(R.string.sp_data_networks_enable_SKT_NORMAL);
                isDataDisable = false;
            }
        } else if (true == "KT".equals(Config.getOperator())) {
            if (Utils.isUI_4_1_model(mContext)) {
                builder.setTitle(R.string.data_network_settings_title);
            }
            if (true == enabled) {
                //add the OMA MMS Service spec 1.1.3.
                if (Config.isDataRoaming() == true) {
                    builder.setMessage(R.string.sp_data_disable_msg_mms_disabled_kt);
                } else {
                    builder.setMessage(R.string.sp_data_disable_msg_mms_enabled_kt);
                }
                //builder.setMessage(R.string.sp_data_networks_disabled_KT_NORMAL);
                isDataDisable = true;
            } else {
                builder.setMessage(R.string.sp_data_networks_enabled_KT_NORMAL);
                isDataDisable = false;
            }
        } else if (true == "LGU".equals(Config.getOperator())) {
            if (Utils.isUI_4_1_model(mContext)) {
                strOk = R.string.yes;
                strCancel = R.string.no;
            }
            if (enabled) {
                builder.setMessage(R.string.sp_data_network_mode_disabled_desc_lgu_NORMAL);
                isDataDisable = true;
            } else {
                builder.setMessage(R.string.sp_data_network_mode_enabled_desc_lgu_NORMAL);
                isDataDisable = false;
            }
        } else if (true == mIs_Ctc_Cn) {
            if (true == enabled) {
                builder.setMessage(R.string.data_usage_disable_mobile);
                isDataDisable = true;
            } else {
                if (Config.isDataRoaming(sSim1SubId) == true) {
                    builder.setTitle(R.string.sp_dlg_note_NORMAL);
                    builder.setMessage(R.string.sp_mobile_data_enable_ct_roaming_normal);
                    isDataDisable = false;
                    strOk = R.string.sp_got_it_normal;
                } else {
                    builder.setMessage(R.string.sp_mobile_network_data_connection_charges_NORMAL);
                    isDataDisable = false;
                }
            }
        } else if (true == "ATT".equals(Config.getOperator())) {
            if (enabled) {
                builder.setMessage(R.string.data_usage_disable_mobile);
                isDataDisable = true;
            } else {
                builder.setMessage(R.string.sp_network_msg_data_NORMAL);
                isDataDisable = false;
            }
        } else if (true == "VZW".equals(Config.getOperator())) {
            if (enabled) {
                builder.setMessage(R.string.sp_mobile_data_off_VZW_NORMAL);
                isDataDisable = true;
            }
        } else if (true == "DCM".equals(Config.getOperator())) {
            if (enabled) {
                //builder.setMessage(R.string.sp_data_enabled_uncheck_NORMAL);
                builder.setMessage(R.string.sp_DataDisabledPopup_NORMAL);
                isDataDisable = true;
            } else {
                //builder.setMessage(R.string.sp_data_enabled_check_NORMAL);
                builder.setMessage(R.string.sp_DataEnabledPopup_NORMAL);
                isDataDisable = false;
            }
        } else if (true == "KDDI".equals(Config.getOperator())) {
            strOk = R.string.yes;
            strCancel = R.string.no;
            if (enabled) {
                builder.setMessage(R.string.sp_data_enabled_uncheck_NORMAL);
                isDataDisable = true;
            } else {
                builder.setMessage(R.string.sp_data_enabled_check_NORMAL);
                isDataDisable = false;
            }
        } else {
            if (Utils.isUI_4_1_model(mContext)) {
                strOk = R.string.yes;
                strCancel = R.string.no;
            }
            builder.setMessage(R.string.data_usage_disable_mobile);
            isDataDisable = true;
        }

        builder.setPositiveButton(
                strOk,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isDataDisable == true) {
                            if (true == "KDDI".equals(Config.getOperator())) {
                                Utils.restoreLteModeForKddi(mContext, false);
                            }
                            setMobileDataEnabled(false);
                        } else {
                            if (true == "KDDI".equals(Config.getOperator())) {
                                Utils.restoreLteModeForKddi(mContext, true);
                            }
                            setMobileDataEnabled(true);
                        }
                    }
                });
        if (!(true == mIs_Ctc_Cn 
                && true == Config.isDataRoaming(sSim1SubId)
                && false == isDataDisable)) {
            SLog.i("data enable dialog popup");
            builder.setNegativeButton(
                strCancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isDataDisable == true) {
                            if (true == "KDDI".equals(Config.getOperator())) {
                                Utils.restoreLteModeForKddi(mContext, true);
                            }
                            setMobileDataEnabled(true);
                        } else {
                            if (true == "KDDI".equals(Config.getOperator())) {
                                Utils.restoreLteModeForKddi(mContext, false);
                            }
                            setMobileDataEnabled(false);
                        }
                        mAlertDialog.dismiss();
                    }
                });
        }
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (isDataDisable == true) {
                    setMobileDataEnabled(true);
                } else if (true == mIs_Ctc_Cn 
                            && (Config.isDataRoaming(sSim1SubId) == true && false == isDataDisable)
                            ) {
                    setMobileDataEnabled(true);
                    SLog.i("data enable dialog popup true");
                } else {
                    setMobileDataEnabled(false);
                }
                mAlertDialog.dismiss();
            }
        });

        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void showDataEnablerDialog_VZW() {
        int strOk = R.string.yes;
        int strCancel = R.string.no;
        boolean enabled = mTelephonyManager.getDataEnabled();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if (false == Utils.isUI_4_1_model(mContext)) {
            builder.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        builder.setTitle(R.string.data_usage_enable_mobile);

        LayoutInflater inflater = (LayoutInflater)mContext
                .getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_checkbox_vzw, null);
        TextView messageText = (TextView)view.findViewById(R.id.vzw_disable_popup_text);

        if (enabled) {
            messageText.setText(R.string.sp_mobile_data_off_VZW_NORMAL);
            isDataDisable = true;
        }

        builder.setPositiveButton(strOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isDataDisable == true) {
                    setMobileDataEnabled(false);
                } else {
                    setMobileDataEnabled(true);
                }
            }
        });
        builder.setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isDataDisable == true) {
                    setMobileDataEnabled(true);
                } else {
                    setMobileDataEnabled(false);
                }
                mAlertDialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                if (isDataDisable == true) {
                    setMobileDataEnabled(true);
                } else {
                    setMobileDataEnabled(false);
                }
                mAlertDialog.dismiss();
            }
        });
        builder.setView(view);
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mAlertDialog.show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.AIRPLANE_MODE_ON, 0) != 0;
                boolean hasReadyMobile = Utils.hasReadyMobileRadio(mContext);

                if (true == isAirplaneMode || false == hasReadyMobile) {
                    SLog.i("isAirplaneMode  = " + hasReadyMobile);
                    if ((false == isAirplaneModeOn(mContext))
                            && OverlayUtils.isMultiSimEnabled()
                            && false == OverlayUtils
                                    .isEmptySim(Utils.getCurrentDDS(mContext))) {
                        mSwitch.setChecked(false);
                        //mSwitch.setChecked(isMobileDataEnabled());
                    } else {
                        mSwitch.setChecked(false);
                    }
                    mSwitch.setEnabled(false);
                } else {
                    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-198][ID-MDM-223][ID-MDM-167]
                    if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                        if (com.lge.mdm.LGMDMManager.getInstance().getEnforceMobileNetworkEnabled(
                                null)
                                || !com.lge.mdm.LGMDMManager.getInstance().getAllowMobileNetwork(
                                        null)) {
                            return;
                        }
                    }
                    // LGMDM_END
                    mSwitch.setEnabled(true);

                    boolean dataSavedState = false;
                    dataSavedState = mTelephonyManager.getDataEnabled();

                    if (true == dataSavedState) {
                        setMobileDataEnabled(true);
                    }
                }
                updateMobileDataSwitchState();
            }

            //If in callings, DataEnabled preperence is dimmed
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                if ("DCM".equals(Config.getOperator()) 
                    || "VZW".equals(Config.getOperator())) {
                    boolean isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                            Settings.System.AIRPLANE_MODE_ON, 0) != 0;
                    TelephonyManager tm = TelephonyManager.getDefault();
                    SLog.i("ACTION_PHONE_STATE_CHANGED :: DCM ");
                    if ((tm != null)
                            && false == isAirplaneMode
                            && (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE)) {
                        SLog.i("ACTION_PHONE_STATE_CHANGED :: tm.getCallState() = "
                                + tm.getCallState());
                        if (mSwitch != null) {
                            mSwitch.setEnabled(true);
                        }
                    }
                }
            }

            if (ACTION_SIM_STATE_CHANGED.equals(action)) {
                String mStateExtra = intent.getStringExtra(INTENT_KEY_ICC_STATE);
                SLog.d("receiver [Extra] : " + mStateExtra + " == '" + INTENT_VALUE_ICC_READY + "' ??");
                if (mStateExtra != null && INTENT_VALUE_ICC_READY.equals(mStateExtra)) {
                    boolean isAirplaneMode = isAirplaneModeOn(mContext);
                    boolean hasReadyMobile = Utils.hasReadyMobileRadio(mContext);
                    SLog.i("receiver isAirplaneMode " + isAirplaneMode);
                    SLog.i("receiver hasReadyMobileRadio " + hasReadyMobile);
                    if (false == isAirplaneMode && hasReadyMobile) {
                        SLog.i("receiver enable === " + isMobileDataEnabled());
                        if (mSwitch != null) {
                            if (!mSwitch.isEnabled()) {
                                mSwitch.setEnabled(true);
                            }
                            //If in callings, DataEnabled preperence is dimmed
                            if ("DCM".equals(Config.getOperator())) {
                                TelephonyManager tm = TelephonyManager.getDefault();
                                if ((tm != null) && (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE)) {
                                    mSwitch.setEnabled(false);
                                }
                            }
                            if (!fromTouch) {
                                if (Utils.hasNetworkLocked(mContext)) {
                                    mSwitch.setChecked(false);
                                } else {
                                    mSwitch.setChecked(isMobileDataEnabled());
                                }
                            }
                            updateMobileDataSwitchState();
                        }
                    }
                }
            }
        }
    };

    OnKeyListener mSwitchOnKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View arg0, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_UP) {
                fromTouch = true;
            }
            return false;
        }
   };

   private void setMobileDataEnabledDB(boolean enabled) {
       mTelephonyManager.setDataEnabled(enabled);
   }

   private boolean getCurrentMobileDataEnabledDB() {
       return mTelephonyManager.getDataEnabled();
   }
}
