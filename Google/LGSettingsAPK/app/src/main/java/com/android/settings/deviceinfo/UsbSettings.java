/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lge.RadioButtonPreference;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.io.InputStream;
import java.util.Locale;
import android.net.ConnectivityManager;
import android.os.storage.StorageManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lge.constants.SettingsConstants;

import com.lge.constants.UsbManagerConstants;

public class UsbSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "UsbSettings";
    private static final boolean DEBUG = true;

    private static final String KEY_CHARGER = "usb_charger";
    private static final String KEY_MEDIA = "usb_media_sync";
    private static final String KEY_MASS_STORAGE = "usb_mass_storage";
    private static final String KEY_SUITE = "usb_pc_suite";
    private static final String KEY_TETHER = "usb_tether";
    private static final String KEY_PTP = "usb_ptp";
    private static final String KEY_INTERNET = "usb_internet";
    private static final int AUTORUN_BUTTON_ID = 1000;
    private static final int INSTALL_PC_ID = 10;
    private static final int HELP_ID = 11;
    private static final int HELP_REQUEST = -1;

    private RadioButtonPreference mCharger;
    private RadioButtonPreference mMediasync;
    private RadioButtonPreference mTether;
    private RadioButtonPreference mPcsuite;
    private RadioButtonPreference mMassStorage;
    private RadioButtonPreference mPtp;
    private RadioButtonPreference mInternet;

    private ProgressDialog mProgress;
    private AlertDialog mFirstAutorunDialog;
    private AlertDialog mInstallAutorunDialog;

    private String mCurrentFunction;
    private String mDefaultFunction;
    private int mInternetConnection;
    private ConnectivityManager mConnectivityManager;

    private UsbManager mUsbManager;
    private Context mContext;

    private boolean mUsbLauncher = false;
    private static final int MHS_REQUEST = 0;
    private static final int TETHER_USC_REQUEST = MHS_REQUEST + 1;
    private static final int UMS_REQUEST = TETHER_USC_REQUEST + 1;
    // delay for Usb mode set
    private boolean mNeedSleep = false;

    private boolean mChekshowDoNot = false;
    private boolean mSetChekshowDoNot = false;
    private ImageButton mButton; // using breadcrumb
    private Menu mMenu;
    // L0 MPCS req : Check lte connection state
    /*private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            updateTetherState();
        }
    };*/

    private void updateTetherState() {
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-242][ID-MDM-42]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM
                && (com.android.settings.MDMSettingsAdapter.getInstance()
                        .checkDisabledUsbType(com.lge.cappuccino.IMdm.USB_TETHER))) {
            return;
        }
        // LGMDM_END

        int networkLockMode = Settings.System.getInt(mContext.getContentResolver(),
                "network_lock_setting_mode", 0);
        if (networkLockMode == 0) {
            Log.d(TAG, "Skip LTE check");
            return;
        }

        if (mTelephonyManager != null &&
                mTelephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_LTE) {

            if (mTether != null) {
                mTether.setEnabled(false);
            }
        }
        else {
            if (mTether != null) {
                mTether.setEnabled(true);
            }
        }

        // Hidden : Android NDIS Port - ASCOM Tool
        int enable = Settings.System.getInt(mContext.getContentResolver(),
                "hidden_usb_tethering_enable", 0);
        if (enable == 1) {
            if (mTether != null) {
                mTether.setEnabled(true);
            }
        }

    }

    private TelephonyManager mTelephonyManager;

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "[AUTORUN] mStateReceiver() : action=" + action);
            Log.d(TAG,
                    "[AUTORUN] onReceive() : app userid = "
                            + UserHandle.getUserId(context.getApplicationInfo().uid)
                            + ", current userid = " + ActivityManager.getCurrentUser());

            if (UserHandle.getUserId(context.getApplicationInfo().uid) != ActivityManager
                    .getCurrentUser()) {
                return;
            }

            if (Utils.isMonkeyRunning()) {
                return;
            }

            if (action.equals(UsbManager.ACTION_USB_STATE)) {
                boolean usbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                boolean usbConfigured = intent.getBooleanExtra(UsbManager.USB_CONFIGURED, false);

                Log.d(TAG,
                        "[AUTORUN] mStateReceiver() : getDefaultFunction = "
                                + mUsbManager.getDefaultFunction());
                Log.d(TAG,
                        "[AUTORUN] mStateReceiver() : USB_FUNCTION_CDROM_STORAGE = "
                                + intent.getBooleanExtra(
                                        UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE, false));
                Log.d(TAG, "[AUTORUN] mStateReceiver() : mDirectAutorun = "
                        + UsbSettingsControl.mDirectAutorun);
                Log.d(TAG, "[AUTORUN] mStateReceiver() : mActivityUsbModeChange = "
                        + UsbSettingsControl.mActivityUsbModeChange);
                Log.d(TAG, "[AUTORUN] mStateReceiver() : mActivityFinish = "
                        + UsbSettingsControl.mActivityFinish);

                if (usbConnected == true && usbConfigured == false) {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Connected =====");
                    UsbSettingsControl.setUsbConnected(context, true);

                    if (intent.getBooleanExtra(UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE,
                            false)) {
                        callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                    }
                } else if (usbConnected == true && usbConfigured == true) {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Configured =====");
                    UsbSettingsControl.setUsbConnected(context, true);

                    if (intent.getBooleanExtra(UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE,
                            false)) {
                        callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                    } else {
                        cancelProgressPopup();

                        /*if (!mUsbManager.getDefaultFunction().equals(mDefaultFunction)) {
                            mDefaultFunction = mUsbManager.getDefaultFunction();
                            updateToggles(mDefaultFunction);
                        }*/

                        if (!Config.getOperator().equals(Config.VZW)
                                && !UsbSettingsControl.isMultiUser()
                                && !UsbSettingsControl.isDirectAutorunModel()) {
                            // using breadcrumb
                            if (Utils.supportSplitView(getActivity())
                                    && !Utils.isUI_4_1_model(mContext)) {
                                SettingsBreadCrumb breadCrumb = SettingsBreadCrumb
                                        .get(getActivity());
                                if (breadCrumb != null) {
                                    ImageButton button = breadCrumb.getImageButton();
                                    if (button != null) {
                                        button.setVisibility(View.VISIBLE);
                                        button.setImageResource(R.drawable.ic_menu_installed_applications);
                                        button.setEnabled(true);
                                    }
                                }
                            } else {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    ActionBar actionBar = activity.getActionBar();
                                    if (actionBar != null) {
                                        View v = actionBar.getCustomView();
                                        if (v != null) {
                                            if (Utils.isUI_4_1_model(mContext)) {
                                                setHasOptionsMenu(true);
                                            } else {
                                                ImageButton autorunButton = (ImageButton)v
                                                        .findViewById(AUTORUN_BUTTON_ID);
                                                setHasOptionsMenu(true);
                                                autorunButton.setVisibility(View.GONE);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!Config.getOperator().equals(Config.VZW)
                                && !UsbSettingsControl.isDirectAutorunModel()
                                && UsbSettingsControl.supportAutorunMode(mContext)
                                && !UsbSettingsControl.getAutorunDialogDoNotShow(mContext)
                                && !UsbSettingsControl.mActivityUsbModeChange
                                && !UsbSettingsControl.mDirectAutorun
                                && !UsbSettingsControl.isAutorunTimer()
                                && !UsbSettingsControl.isMultiUser()
                                && UsbSettingsControl.getUsbConnected(mContext)) {
                            callPopup(UsbSettingsControl.DIALOG_FIRST_AUTORUN);
                        }
                    }

                    if (!UsbSettingsControl.mActivityUsbModeChange
                            &&
                            intent.getBooleanExtra(UsbManagerConstants.USB_FUNCTION_TETHER, false)
                            &&
                            !UsbSettingsControl.getTetherStatus(context)
                            &&
                            !Config.getOperator().equals(Config.VZW)
                            &&
                            !intent.getBooleanExtra(UsbManagerConstants.USB_FUNCTION_CDROM_STORAGE,
                                    false) &&
                            !UsbSettingsControl.isMultiUser() &&
                            !UsbSettingsControl.isAutorunTimer()) {
                        if (UsbSettingsControl.getAutorunDialogDoNotShow(mContext)) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                if (DEBUG) {
                                    Log.d(TAG, "[AUTORUN] waiting exception");
                                }
                            }
                            UsbSettingsControl.callTetherPopup(context);
                        }
                    } else if (!UsbSettingsControl.mActivityUsbModeChange &&
                            intent.getBooleanExtra(UsbManager.USB_FUNCTION_MASS_STORAGE, false) &&
                            !UsbSettingsControl.isDisconnectBugModel()) {
                        if (UsbSettingsControl.isMassSeperatedModel()) {
                            startUmsSelection();
                        }
                        else {
                            UsbSettingsControl.setMassStorage(mContext, true);
                        }
                    }
                } else if (usbConnected == false && usbConfigured == false) {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Disconnected =====");
                    UsbSettingsControl.setUsbConnected(context, false);

                    if (!UsbSettingsControl.isDisconnectBugModel()) {
                        if (Config.getOperator().equals(Config.VZW)
                                || UsbSettingsControl.isDirectAutorunModel()) {
                            UsbSettingsControl.mDirectAutorun = true;
                        } else {
                            UsbSettingsControl.mDirectAutorun = false;
                        }
                        UsbSettingsControl.mActivityUsbModeChange = false;
                    }

                    UsbSettingsControl.cancelAutorunTimer();
                    cancelProgressPopup();

                    if (!Config.getOperator().equals(Config.VZW)) {
                        Log.d(TAG, "[AUTORUN] mStateReceiver() : mDefaultFunction="
                                + mDefaultFunction);

                        updateToggles(mDefaultFunction);
                        mCurrentFunction = mDefaultFunction;

                        if (UsbSettingsControl.isDirectAutorunModel()) {
                            if (!UsbSettingsControl.isDisconnectBugModel()) {
                                if (mDefaultFunction
                                        .equals(UsbManagerConstants.USB_FUNCTION_MTP_ONLY)) {
                                    UsbSettingsControl.changeAutorunMode(mContext,
                                            UsbSettingsControl.USBMODE_MTP, false);
                                } else if (mDefaultFunction
                                        .equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                                    UsbSettingsControl.changeAutorunMode(mContext,
                                            UsbSettingsControl.USBMODE_TETHER, false);
                                } else if (mDefaultFunction
                                        .equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)) {
                                    UsbSettingsControl.changeAutorunMode(mContext,
                                            UsbSettingsControl.USBMODE_PCSUITE, false);
                                } else {
                                    mUsbManager.setCurrentFunction(mDefaultFunction, true);
                                }
                            }
                        } else {
                            if (!UsbSettingsControl.isMultiUser()) {
                                // using breadcrumb
                                if (Utils.supportSplitView(getActivity())
                                        && !Utils.isUI_4_1_model(mContext)) {
                                    SettingsBreadCrumb breadcrumb = SettingsBreadCrumb
                                            .get(getActivity());
                                    if (breadcrumb != null) {
                                        ImageButton button = breadcrumb.getImageButton();
                                        if (button != null) {
                                            button.setVisibility(View.VISIBLE);
                                            button.setEnabled(false);
                                            button.setImageResource(R.drawable.ic_menu_installed_applications_disabled);
                                        }
                                    }
                                } else {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        ActionBar actionBar = activity.getActionBar();
                                        if (actionBar != null) {
                                            View v = actionBar.getCustomView();
                                            if (v != null) {
                                                if (Utils.isUI_4_1_model(mContext)) {
                                                    setHasOptionsMenu(true);
                                                } else {
                                                    ImageButton autorunButton = (ImageButton)v
                                                            .findViewById(AUTORUN_BUTTON_ID);
                                                    setHasOptionsMenu(false);
                                                    autorunButton.setVisibility(View.VISIBLE);
                                                    autorunButton.setEnabled(false);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (mFirstAutorunDialog != null) {
                                    mFirstAutorunDialog.dismiss();
                                }
                                if (mInstallAutorunDialog != null) {
                                    mInstallAutorunDialog.dismiss();
                                }
                            }
                        }
                    }

                    if (mUsbLauncher) {
                        finish();
                    }
                } else {
                    Log.d(TAG, "[AUTORUN] mStateReceiver() : ===== USB Unknown Connected ====");
                    UsbSettingsControl.setUsbConnected(context, false);
                }
                if (Utils.isUI_4_1_model(mContext)) {
                    checkProperOptionMenu();
                }
            } else if (action.equals(UsbSettingsControl.ACTION_AUTORUN_TIMEOUT)) {
                UsbSettingsControl.cancelAutorunTimer();
                cancelProgressPopup();

                if (mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
                    mDefaultFunction = mCurrentFunction;
                    UsbSettingsControl.callTetherPopup(context);
                    return;
                } else {
                    if (Config.getOperator().equals(Config.VZW)
                            && (mCurrentFunction.equals("cdrom_storage")
                            || mCurrentFunction.equals("cdrom_storage,adb"))) {
                        if (UsbSettingsControl.isMassStorageSupport(mContext)) {
                            mCurrentFunction = UsbManager.USB_FUNCTION_MASS_STORAGE;
                            UsbSettingsControl.mActivityUsbModeChange = true;
                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                            mDefaultFunction = mCurrentFunction;
                            UsbSettingsControl.setMassStorage(mContext, true);
                        } else {
                            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_MTP_ONLY;
                            UsbSettingsControl.mActivityUsbModeChange = true;
                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                            mDefaultFunction = mCurrentFunction;
                        }
                    } else {
                        UsbSettingsControl.mActivityUsbModeChange = true;
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                    }
                }

                if (UsbSettingsControl.mActivityFinish) {
                    if (DEBUG) {
                        Log.d(TAG, "[AUTORUN] mStateReceiver() : finish");
                    }
                    getActivity().finish();
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "[AUTORUN] mStateReceiver() : Do not finish");
                    }
                    UsbSettingsControl.mActivityFinish = true;
                }
            } else if (action.equals(UsbSettingsControl.ACTION_AUTORUN_ACK)) {
                if ("VZW".equals(Config.getOperator())) {
                    return;
                }
            } else if (action.equals(UsbSettingsControl.ACTION_AUTORUN_CHANGE_MODE)) {
                if ("VZW".equals(Config.getOperator())) {
                    return;
                }
                UsbSettingsControl.cancelAutorunTimer();
                cancelProgressPopup();
                autorunChangeMode();
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {

                if (mTether != null) {
                    mTether.setEnabled(!isAirplaneModeOn());
                }
                // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-242][ID-MDM-42][ID-MDM-249]
                // [ID-MDM-292][ID-MDM-250]
                if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                    com.android.settings.MDMSettingsAdapter.getInstance().setUsbMenu(null, null,
                            null, null, mTether, null, null, -1, null);
                }
                // LGMDM_END
            } else if (action.equals(UsbSettingsControl.ACTION_MASS_STATE_CHANGE)) {
                UsbSettingsControl.checkStorageVolume(context);
                String usbMode = intent.getStringExtra(UsbSettingsControl.EXTRA_USB_DEFAULT_MODE);

                if (usbMode != null && usbMode.equals(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY)) {
                    mCurrentFunction = usbMode;
                    updateToggles(mCurrentFunction);
                }
                else {
                    mMassStorage.setEnabled(UsbSettingsControl.isMassStorageEnable());
                    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-242][ID-MDM-42][ID-MDM-249]
                    // [ID-MDM-292][ID-MDM-250]
                    if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                        com.android.settings.MDMSettingsAdapter.getInstance().setUsbMenu(null,
                                null, mMassStorage, null, null, null, null, -1, null);
                    }
                    // LGMDM_END
                }
            } else if (action.equals(UsbSettingsControl.ACTION_TETHER_STATE_CHANGE)) {
                context.removeStickyBroadcast(intent);
                String usbMode = intent.getStringExtra(UsbSettingsControl.EXTRA_USB_DEFAULT_MODE);
                if (usbMode != null && usbMode.equals(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY)) {
                    mCurrentFunction = usbMode;
                    mDefaultFunction = mCurrentFunction;
                    updateToggles(mCurrentFunction);
                }
            } else if (action.equals(UsbSettingsControl.ACTION_ACTIVITY_FINISH)) {
                context.removeStickyBroadcast(intent);
                finish();
            }
        }
    };

    private PreferenceScreen createPreferenceHierarchy() {
        addPreferencesFromResource(R.xml.usb_settings);

        PreferenceScreen root = getPreferenceScreen();

        mCharger = (RadioButtonPreference)root.findPreference(KEY_CHARGER);
        mCharger.setOnPreferenceChangeListener(this);
        mMediasync = (RadioButtonPreference)root.findPreference(KEY_MEDIA);
        mMediasync.setOnPreferenceChangeListener(this);
        mTether = (RadioButtonPreference)root.findPreference(KEY_TETHER);
        mTether.setOnPreferenceChangeListener(this);
        mPcsuite = (RadioButtonPreference)root.findPreference(KEY_SUITE);
        mPcsuite.setOnPreferenceChangeListener(this);
        mPtp = (RadioButtonPreference)root.findPreference(KEY_PTP);
        mPtp.setOnPreferenceChangeListener(this);
        mMassStorage = (RadioButtonPreference)root.findPreference(KEY_MASS_STORAGE);
        mMassStorage.setOnPreferenceChangeListener(this);
        mInternet = (RadioButtonPreference)root.findPreference(KEY_INTERNET);
        mInternet.setOnPreferenceChangeListener(this);

        UserManager um = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            mCharger.setEnabled(false);
            mMediasync.setEnabled(false);
            mTether.setEnabled(false);
            mPcsuite.setEnabled(false);
            mPtp.setEnabled(false);
            mMassStorage.setEnabled(false);
            mInternet.setEnabled(false);
        }

        if ("VZW".equals(Config.getOperator())) {
            mCharger.setSummary(R.string.sp_usbtype_charge_summary_NORMAL);
            mMediasync.setSummary(R.string.usbtype_mtp_summary2);
            mPtp.setTitle(R.string.usb_ptp_title);
            mPtp.setSummary(R.string.sp_usbtype_ptp_summary_NORMAL);
        }

        if (Config.getOperator().equals(Config.ATT)
                && Config.getCountry().equals("US")) {
            mPcsuite.setTitle(R.string.sp_pc_software_NORMAL);
            mMediasync.setSummary(R.string.usbtype_mtp_summary2);
        }
        if (Utils.isUI_4_1_model(mContext)) {
        	mCharger.setSummary(R.string.sp_usbtype_charge_summary_ex3);
        	if (!"VZW".equals(Config.getOperator())
        	    && !"ATT".equals(Config.getOperator())) {
        	    mMediasync.setTitle(R.string.sp_usbtype_mtp_title_NORMAL_ex);
        	}
        }

        if (Config.getOperator().equals(Config.VZW)) {
            root.removePreference(mTether);
            root.removePreference(mPcsuite);
        } else {
            root.removePreference(mInternet);
        }

        if (Config.getOperator2().equals(Config.TRF)
                || Config.getOperator().equals(Config.DCM)
                || (Config.getOperator().equals(Config.KDDI))
                || (Config.getOperator().equals(Config.TMO) && Config.getCountry().equals("US"))
                || (Config.getOperator().equals(Config.ACG) && !mConnectivityManager
                        .isTetheringSupported())
                || "AIO".equals(Config.getOperator2())
                || "MPCS".equals(Config.getOperator())) {
            root.removePreference(mTether);

            if (UsbSettingsControl.isPCsoftwareTRFModel()) {
                root.removePreference(mPcsuite);
            }

        } else if (Config.getOperator2().equals(Config.SPRINT)
                || Config.getOperator().equals(Config.BM)) {
            root.removePreference(mTether);
            root.removePreference(mPcsuite);
        }

        if (!UsbSettingsControl.isMtpSupport(mContext)) {
            root.removePreference(mMediasync);
            root.removePreference(mPtp);
        }
        if (!UsbSettingsControl.isMassStorageSupport(mContext)) {
            root.removePreference(mMassStorage);
        }
        if (Utils.isWifiOnly(mContext)) {
            root.removePreference(mTether);
        }

        if (Utils.isSupportUSBMultipleConfig(mContext)) {
            root.removePreference(mTether);
            root.removePreference(mPcsuite);
            if ("VZW".equals(Config.getOperator())) {
                root.removePreference(mInternet);
            }
        } else {
            root.removePreference(mTether);
        }

        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mContext = getActivity().getApplicationContext();

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mDefaultFunction = mUsbManager.getDefaultFunction();
        if (!UsbSettingsControl.mActivityUsbModeChange) {
            mCurrentFunction = mDefaultFunction;
        } else {
            if (UsbSettingsControl.mActiveCurrentFunction != null
                    && !"".equals(UsbSettingsControl.mActiveCurrentFunction)) {
                mCurrentFunction = UsbSettingsControl.mActiveCurrentFunction;
            } else {
                mCurrentFunction = mDefaultFunction;
            }
        }

        if (Config.getOperator().equals(Config.VZW)) {
            if (mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                mInternetConnection = 0;
            } else if (mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)) {
                mInternetConnection = 1;
            } else {
                mInternetConnection = -1;
            }
        }

        Log.d(TAG, "[AUTORUN] onCreate() : mDefaultFunction=" + mDefaultFunction);
        Log.d(TAG, "[AUTORUN] onCreate() : mCurrentFunction=" + mCurrentFunction);

        createPreferenceHierarchy();

        Bundle args = getArguments();
        mUsbLauncher = args != null ? args.getBoolean(UsbSettingsControl.EXTRA_USB_LAUNCHER) : true;

        // L0 MPCS req : Check lte connection state
        /*if (Config.getOperator().equals(Config.MPCS)) {
            mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyManager != null) {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
            }

            updateTetherState();
        }*/

        Intent i1 = new Intent(UsbSettingsControl.ACTION_TETHER_STATE_CHANGE);
        mContext.removeStickyBroadcast(i1);
        Intent i2 = new Intent(UsbSettingsControl.ACTION_ACTIVITY_FINISH);
        mContext.removeStickyBroadcast(i2);

        if (!Config.getOperator().equals(Config.VZW)
                && !UsbSettingsControl.isDirectAutorunModel()
                && !UsbSettingsControl.isMultiUser()
                && UsbSettingsControl.supportAutorunMode(mContext)
                && !Utils.isUI_4_1_model(mContext)) {
            mButton = new ImageButton(getActivity());
            mButton.setId(AUTORUN_BUTTON_ID);
            mButton.setBackgroundColor(Color.TRANSPARENT);
            mButton.setImageResource(R.drawable.ic_menu_installed_applications_disabled);

            final int padding = getActivity().getResources().getDimensionPixelSize(
                    R.dimen.action_bar_option_menu_padding);
            if (SettingsBreadCrumb.isAttached(getActivity())
                    && !Utils.isUI_4_1_model(mContext)) {
                mButton.setClickable(true);
                mButton.setBackground(getActivity().getResources().getDrawable(
                        R.drawable.breadcrumb_background));
                mButton.setPaddingRelative(0, 0, 0, 0);
                mButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        callPopup(UsbSettingsControl.DIALOG_INSTALL_AUTORUN);
                    }
                });
            } else {
                mButton.setPaddingRelative(0, 0, padding, 0);
            }

            if (Utils.supportSplitView(getActivity()) == false) {
                getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                getActivity().getActionBar().setCustomView(mButton, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
            }
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (Utils.supportSplitView(getActivity())
                        && !Utils.isUI_4_1_model(mContext)) {
                    mButton.setImageResource(R.drawable.ic_menu_installed_applications);
                    mButton.setEnabled(true);
                } else {
                    setHasOptionsMenu(true);
                    mButton.setVisibility(View.GONE);
                }
            } else {
                if (Utils.isUI_4_1_model(mContext)) {
                    setHasOptionsMenu(true);
                } else {
                    setHasOptionsMenu(false);
                    mButton.setVisibility(View.VISIBLE);
                    mButton.setEnabled(false);
                }
            }

            if (!UsbSettingsControl.getAutorunDialogDoNotShow(mContext) &&
                    !UsbSettingsControl.mActivityUsbModeChange &&
                    !UsbSettingsControl.mDirectAutorun &&
                    !UsbSettingsControl.isAutorunTimer() &&
                    UsbSettingsControl.getUsbConnected(mContext)) {
                callPopup(UsbSettingsControl.DIALOG_FIRST_AUTORUN);
            }
            // TEST CODE
            //            UsbSettingsControl.setAutorunDialogDoNotShow(mContext, false);
        } else {
            if (Utils.isUI_4_1_model(mContext)) {
                setHasOptionsMenu(true);
            } else {
                setHasOptionsMenu(false);
            }
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            IntentFilter filterLGMDM = new IntentFilter();
            com.android.settings.MDMSettingsAdapter.getInstance().addUsbPolicyChangeIntentFilter(
                    filterLGMDM);
            getActivity().registerReceiver(mLGMDMReceiver, filterLGMDM);
        }
        // LGMDM_END

        if (UsbSettingsControl.isLCDCrackATT()) {
            Log.d(TAG, "[AUTORUN] LCD Crack in ATT start Autorun");
            startAutorun();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        if (mFirstAutorunDialog != null && mFirstAutorunDialog.isShowing()) {
            mSetChekshowDoNot = mChekshowDoNot;
            mFirstAutorunDialog.dismiss();
            mFirstAutorunDialog = null;
            callPopup(UsbSettingsControl.DIALOG_FIRST_AUTORUN);
        }

        super.onConfigurationChanged(newConfig);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,
                "[AUTORUN] onDestroy() : getDefaultFunction =" + mUsbManager.getDefaultFunction());

        if (mProgress != null) {
            mProgress.dismiss();
        }
        if (mFirstAutorunDialog != null) {
            mFirstAutorunDialog.dismiss();
        }

        ImageButton autorunButton = (ImageButton)getActivity().findViewById(AUTORUN_BUTTON_ID);
        if (autorunButton != null) {
            setHasOptionsMenu(true);
            autorunButton.setVisibility(View.GONE);
        }

        // MPCS req : Check lte connection state
        /*if (Config.getOperator().equals(Config.MPCS)) {
            if (mTelephonyManager != null) {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        }*/

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
        // [ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            try {
                getActivity().unregisterReceiver(mLGMDMReceiver);
            } catch (Exception e) {
                Log.w("MDM", "mLGMDMReceiver unregisterReceiver ", e);
            }
        }
        // LGMDM_END
        if (DEBUG) {
            String functions1 = SystemProperties.get("persist.sys.usb.config", "");
            String functions2 = SystemProperties.get("sys.usb.config", "");
            String functions3 = SystemProperties.get("sys.usb.state", "");
            Log.v(TAG, "[AUTORUN] onDestroy() : persist.sys.usb.config=" + functions1);
            Log.v(TAG, "[AUTORUN] onDestroy() : sys.usb.config=" + functions2);
            Log.v(TAG, "[AUTORUN] onDestroy() : sys.usb.state=" + functions3);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            SettingsBreadCrumb breadcrumb = SettingsBreadCrumb.get(getActivity());
            if (breadcrumb != null) {
                breadcrumb.removeImageButton();
            }
        }

        UsbSettingsControl.mUsbSettingsRun = false;
        getActivity().unregisterReceiver(mStateReceiver);

        if (UsbSettingsControl.isAutorunTimer()) {
            UsbSettingsControl.cancelAutorunTimer();

            if (mProgress != null) {
                mProgress.cancel();
            }

            //            if (mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
            //                mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
            //            }

            UsbSettingsControl.mActivityUsbModeChange = true;
            mUsbManager.setCurrentFunction(mCurrentFunction, true);
            mDefaultFunction = mCurrentFunction;
            updateToggles(mCurrentFunction);

            if (mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                UsbSettingsControl.callTetherPopup(mContext);
            }
        }

        UsbSettingsControl.mActiveCurrentFunction = mCurrentFunction;
        Log.d(TAG, "[AUTORUN] onPause() : mActiveCurrentFunction = "
                + UsbSettingsControl.mActiveCurrentFunction);

        if (mNeedSleep) {
            mNeedSleep = false;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                if (DEBUG) {
                    Log.d(TAG, "[AUTORUN] waiting exception");
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "[AUTORUN] onStop()");

        UsbSettingsControl.mActiveCurrentFunction = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "[AUTORUN] onResume() : getDefaultFunction=" + mUsbManager.getDefaultFunction());

        // using breadcrumb
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            SettingsBreadCrumb breadcrumb = SettingsBreadCrumb.get(getActivity());
            if (breadcrumb != null) {
                breadcrumb.addImageButton(mButton);
            }
        }

        UsbSettingsControl.mUsbSettingsRun = true;

        // ACTION_USB_STATE is sticky so this will call updateToggles
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_STATE);
        intentFilter.addAction(UsbSettingsControl.ACTION_AUTORUN_TIMEOUT);
        intentFilter.addAction(UsbSettingsControl.ACTION_AUTORUN_ACK);
        intentFilter.addAction(UsbSettingsControl.ACTION_AUTORUN_CHANGE_MODE);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(UsbSettingsControl.ACTION_MASS_STATE_CHANGE);
        intentFilter.addAction(UsbSettingsControl.ACTION_TETHER_STATE_CHANGE);
        intentFilter.addAction(UsbSettingsControl.ACTION_ACTIVITY_FINISH);
        getActivity().registerReceiver(mStateReceiver, intentFilter);

        /*mDefaultFunction = mUsbManager.getDefaultFunction();
        if (!UsbSettingsControl.mActivityUsbModeChange) {
            mCurrentFunction = mDefaultFunction;
        } else {
            if (UsbSettingsControl.mActiveCurrentFunction != null
                    && !"".equals(UsbSettingsControl.mActiveCurrentFunction))
                mCurrentFunction = UsbSettingsControl.mActiveCurrentFunction;
            else
                mCurrentFunction = mDefaultFunction;
        }*/

        UsbSettingsControl.checkStorageVolume(mContext);
        updateToggles(mCurrentFunction);

        if (mTether != null) {
            mTether.setEnabled(!isAirplaneModeOn());

            // MPCS req : Check lte connection state
            /*if (Config.getOperator().equals(Config.MPCS)) {
                updateTetherState();
            }*/
        }

        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-242][ID-MDM-42][ID-MDM-249][ID-MDM-292]
        // [ID-MDM-250]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance().setUsbMenu(null, null, null,
                    null, mTether, null, null, -1, null);
        }
        // LGMDM_END

        // 3LM_MDM_DCM jihun.im
        // If 3LM_MDM policy blocks USB usage, set it Charge_only on every call of resume()
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            if (Settings.Secure.getInt(mContext.getContentResolver(), "usb_blocked", 0) == 1) {
                if (!mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY)) {
                    onPreferenceChange(mCharger, null);
                } else {
                }
                if (mMediasync != null) {
                    mMediasync.setEnabled(false);
                }
                // to block tethering connection
                if (mTether != null) {
                    mTether.setEnabled(false);
                }
                if (mPcsuite != null) {
                    mPcsuite.setEnabled(false);
                }
                if (mPtp != null) {
                    mPtp.setEnabled(false);
                }
            } else {
                if (mMediasync != null) {
                    mMediasync.setEnabled(true);
                }
                if (mPcsuite != null) {
                    mPcsuite.setEnabled(true);
                }
                if (mPtp != null) {
                    mPtp.setEnabled(true);
                }
            }
        }
    }

    private void updateToggles(String function) {
        if (mCharger == null || mMediasync == null || mTether == null || mPcsuite == null ||
                mPtp == null || mMassStorage == null || mInternet == null) {
            return;
        }
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-242][ID-MDM-42][ID-MDM-249][ID-MDM-292]
        // [ID-MDM-250]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            if (com.android.settings.MDMSettingsAdapter.getInstance().setUsbMenu(mCharger,
                    mMediasync, mMassStorage, mPcsuite, mTether, mPtp, mInternet,
                    mInternetConnection, mCurrentFunction)) {
                function = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
            }
        }
        // LGMDM_END

        //SPRINT_USB :     //Only Check "restricted"
        //Sprint Android Requirements Version 4.0-1   - 6.9.3.8 USB
        if (Config.getOperator().equals(Config.SPR) || Config.getOperator().equals(Config.BM)) {
            //USB_DRIVE
            boolean allowUsbDrive = OverlayUtils.getAllowUsbDrive(null);
            if (allowUsbDrive == false) {
                function = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                mCharger.setChecked(true); //Charge Only
                if (mMediasync.isEnabled()) {
                    mMediasync.setEnabled(false); //MTP gray
                    mMediasync.setSummary(R.string.sp_lgmdm_block_media_sync_NORMAL);
                }
                if (mPtp.isEnabled()) {
                    mPtp.setEnabled(false); //PTP gray
                    mPtp.setSummary(R.string.sp_lgmdm_block_camera_ptp_NORMAL);
                }
                if (mPcsuite.isEnabled()) {
                    mPcsuite.setEnabled(false); //LG Software gray
                    mPcsuite.setSummary(R.string.sp_lgmdm_block_lg_software_NORMAL);
                }
            }
            //USB_DATA
            boolean allowTethering = OverlayUtils.getAllowTethering(null); //spr/overlayutils.java
            if (allowTethering == false) {
                if (mTether.isEnabled()) {
                    mTether.setEnabled(false); //USB Tethering gray
                    mTether.setSummary(R.string.sp_block_usb_thering_NORMAL);
                }
            }
        }

        if (UsbSettingsControl.isMassStorageSupport(mContext)) {
            mMassStorage.setEnabled(UsbSettingsControl.isMassStorageEnable());
            // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-242][ID-MDM-42][ID-MDM-249][ID-MDM-292]
            // [ID-MDM-250]
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                com.android.settings.MDMSettingsAdapter.getInstance().setUsbMenu(null, null,
                        mMassStorage, null, null, null, null, -1, null);
            }
            // LGMDM_END
        }

        if (UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY.equals(function)) {
            mCharger.setChecked(true);
            mMediasync.setChecked(false);
            mTether.setChecked(false);
            mPcsuite.setChecked(false);
            mPtp.setChecked(false);
            mMassStorage.setChecked(false);
            mInternet.setChecked(false);
        } else if (UsbManagerConstants.USB_FUNCTION_MTP_ONLY.equals(function)) {
            mCharger.setChecked(false);
            mMediasync.setChecked(true);
            mPcsuite.setChecked(false);
            mTether.setChecked(false);
            mPtp.setChecked(false);
            mMassStorage.setChecked(false);
            mInternet.setChecked(false);
        } else if (UsbManagerConstants.USB_FUNCTION_PTP_ONLY.equals(function)) {
            mCharger.setChecked(false);
            mMediasync.setChecked(false);
            mTether.setChecked(false);
            mPcsuite.setChecked(false);
            mPtp.setChecked(true);
            mMassStorage.setChecked(false);
            mInternet.setChecked(false);
        } else if (UsbManager.USB_FUNCTION_MASS_STORAGE.equals(function)) {
            mCharger.setChecked(false);
            mMediasync.setChecked(false);
            mTether.setChecked(false);
            mPcsuite.setChecked(false);
            mPtp.setChecked(false);
            mMassStorage.setChecked(true);
            mInternet.setChecked(false);

            if (UsbSettingsControl.isMassSeperatedModel()) {
                int selectedStorage = Settings.System.getInt(getContentResolver(),
                        "ums_selected_storage", 0);
                if (selectedStorage == 0) {
                    mMassStorage.setSummary(R.string.sp_settings_apps_internal_memory_NORMAL);
                } else if (selectedStorage == 1) {
                    mMassStorage.setSummary(R.string.sd_memory);
                } else {
                    mMassStorage.setSummary(R.string.sp_settings_apps_internal_memory_NORMAL);
                }
            }
        } else if (UsbManagerConstants.USB_FUNCTION_TETHER.equals(function)
                || UsbManagerConstants.USB_FUNCTION_PC_SUITE.equals(function)) {
            if (Config.getOperator().equals(Config.VZW)) {
                mCharger.setChecked(false);
                mMediasync.setChecked(false);
                mTether.setChecked(false);
                mPcsuite.setChecked(false);
                mPtp.setChecked(false);
                mMassStorage.setChecked(false);
                mInternet.setChecked(true);

                if (mInternetConnection == 0) {
                    mInternet.setSummary(R.string.data_usage_tab_ethernet);
                } else if (mInternetConnection == 1) {
                    mInternet.setSummary(R.string.sp_modem_NORMAL);
                } else {
                    mInternet.setSummary("");
                }
            } else {
                if (UsbManagerConstants.USB_FUNCTION_TETHER.equals(function)) {
                    mCharger.setChecked(false);
                    mMediasync.setChecked(false);
                    mTether.setChecked(true);
                    mPcsuite.setChecked(false);
                    mPtp.setChecked(false);
                    mMassStorage.setChecked(false);
                    mInternet.setChecked(false);
                } else if (UsbManagerConstants.USB_FUNCTION_PC_SUITE.equals(function)) {
                    mCharger.setChecked(false);
                    mTether.setChecked(false);
                    mPtp.setChecked(false);
                    mMassStorage.setChecked(false);
                    mInternet.setChecked(false);
                    mPcsuite.setChecked(true);
                    mMediasync.setChecked(false);
                }
            }
        } else if (UsbManagerConstants.USB_FUNCTION_AUTO_CONF.equals(function)) {
            mCharger.setChecked(false);
            mMediasync.setChecked(true);
            mPcsuite.setChecked(false);
            mTether.setChecked(false);
            mPtp.setChecked(false);
            mMassStorage.setChecked(false);
            mInternet.setChecked(false);
        } else {
            if (Config.getOperator().equals("SPR")) {
                if ("usb_enable_diag".equals(function)) {
                    mCharger.setChecked(true);
                    mMediasync.setChecked(false);
                    mTether.setChecked(false);
                    mPcsuite.setChecked(false);
                    mPtp.setChecked(false);
                    mMassStorage.setChecked(false);
                    mInternet.setChecked(false);
                } else if ("usb_enable_ecm".equals(function)) {
                    mCharger.setChecked(true);
                    mMediasync.setChecked(false);
                    mTether.setChecked(false);
                    mPcsuite.setChecked(false);
                    mPtp.setChecked(false);
                    mMassStorage.setChecked(false);
                    mInternet.setChecked(false);
                } else {
                    mTether.setChecked(false);
                    mPcsuite.setChecked(false);
                    mPtp.setChecked(false);
                    mMassStorage.setChecked(false);
                    mInternet.setChecked(false);
                    mCharger.setChecked(true);
                    mMediasync.setChecked(false);
                }
            } else {
                mTether.setChecked(false);
                mPcsuite.setChecked(false);
                mPtp.setChecked(false);
                mMassStorage.setChecked(false);
                mInternet.setChecked(false);
                // default value check
                if (Config.getOperator().equals(Config.VZW)/*
                                                           || Config.getOperator().equals(Config.LGU)
                                                           || Config.getOperator().equals(Config.USC)*/) {
                    mCharger.setChecked(false);
                    mMediasync.setChecked(true);
                }
                else {
                    mCharger.setChecked(true);
                    mMediasync.setChecked(false);
                }
            }
        }

        UserManager um = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            Log.d(TAG, "[AUTORUN] USB is locked down");
            mCharger.setChecked(false);
            mMediasync.setChecked(false);
            mTether.setChecked(false);
            mPcsuite.setChecked(false);
            mPtp.setChecked(false);
            mMassStorage.setChecked(false);
            mInternet.setChecked(false);
        }
    }

    private void autorunAck() {
        UsbSettingsControl.cancelAutorunTimer();
        cancelProgressPopup();

        //        String value = UsbSettingsControl.readToFile(UsbSettingsControl.AUTORUN_USBMODE);
        //        if (value.equals(UsbSettingsControl.USBMODE_MTP)) {
        //            UsbSettingsControl.mActivityUsbModeChange = true;
        //            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_MTP_ONLY, true);
        //        } else if (value.equals(UsbSettingsControl.USBMODE_TETHER)) {
        //            UsbSettingsControl.mActivityUsbModeChange = true;
        //            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
        //            UsbSettingsControl.callTetherPopup(mContext);
        //            return;
        //        } else if (value.equals(UsbSettingsControl.USBMODE_PCSUITE)) {
        //            UsbSettingsControl.mActivityUsbModeChange = true;
        //            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_PC_SUITE, true);
        //        }

        if (mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
            UsbSettingsControl.mActivityUsbModeChange = true;
            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
            mDefaultFunction = mCurrentFunction;
            UsbSettingsControl.callTetherPopup(mContext);
            return;
        } else {
            UsbSettingsControl.mActivityUsbModeChange = true;
            mUsbManager.setCurrentFunction(mCurrentFunction, true);
            mDefaultFunction = mCurrentFunction;
        }

        if (UsbSettingsControl.mActivityFinish) {
            if (DEBUG) {
                Log.d(TAG, "[AUTORUN] autorunAck() : finish");
            }
            getActivity().finish();
        } else {
            if (DEBUG) {
                Log.d(TAG, "[AUTORUN] autorunAck() : Do not finish");
            }
            UsbSettingsControl.mActivityFinish = true;
        }
    }

    private void autorunChangeMode() {
        // give a time for PC Autorun App to complete normally.
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            if (DEBUG) {
                Log.d(TAG,
                        "[AUTORUN] Exception wait for termination of PC Launcher, so just skip it");
            }
        }

        //        String value = UsbSettingsControl.readToFile(UsbSettingsControl.AUTORUN_USBMODE);
        //        if (value.equals(UsbSettingsControl.USBMODE_MTP)) {
        //            UsbSettingsControl.mActivityUsbModeChange = true;
        //            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_MTP_ONLY, true);
        //        } else if (value.equals(UsbSettingsControl.USBMODE_TETHER)) {
        //            UsbSettingsControl.mActivityUsbModeChange = true;
        //            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
        //            UsbSettingsControl.callTetherPopup(mContext);
        //            return;
        //        } else if (value.equals(UsbSettingsControl.USBMODE_PCSUITE)) {
        //            UsbSettingsControl.mActivityUsbModeChange = true;
        //            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_PC_SUITE, true);
        //        }

        if (mCurrentFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
            UsbSettingsControl.mActivityUsbModeChange = true;
            mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
            mDefaultFunction = mCurrentFunction;
            UsbSettingsControl.callTetherPopup(mContext);
            return;
        } else {
            UsbSettingsControl.mActivityUsbModeChange = true;
            mUsbManager.setCurrentFunction(mCurrentFunction, true);
            mDefaultFunction = mCurrentFunction;
        }

        if (UsbSettingsControl.mActivityFinish) {
            if (DEBUG) {
                Log.d(TAG, "[AUTORUN] autorunChangeMode() : finish");
            }
            getActivity().finish();
        } else {
            if (DEBUG) {
                Log.d(TAG, "[AUTORUN] autorunChangeMode() : Do not finish");
            }
            UsbSettingsControl.mActivityFinish = true;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Don't allow any changes to take effect as the USB host will be disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }

        UserManager um = (UserManager)getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            return true;
        }

        if (!UsbSettingsControl.mUsbSettingsRun) {
            return true;
        }
        
        if (preference == mTether) {
        	if (!UsbSettingsControl.isDirectAutorunModel()
        			&& !Config.getOperator().equals(Config.VZW)
        			&& mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                finish();
        		return false;
        	}
        }
        
        if (UsbSettingsControl.getUsbConnected(mContext)) {
            UsbSettingsControl.mDirectAutorun = false;
            UsbSettingsControl.mActivityFinish = true;

            UsbSettingsControl.connectUsbTether(mContext, false);

            if (preference != mMassStorage) {
                UsbSettingsControl.setMassStorage(mContext, false);
            }
        }

        if (preference == mCharger) {
            Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_CHARGE_ONLY");
            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
            updateToggles(mCurrentFunction);

            if (Config.getOperator().equals(Config.VZW)) {
                UsbSettingsControl.writeToFile(UsbSettingsControl.AUTORUN_USBMODE,
                        UsbSettingsControl.USBMODE_CHARGE);

                if (UsbSettingsControl.getUsbConnected(mContext)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;

                    doSleep(20);
                    UsbSettingsControl.changeAutorunMode(mContext,
                            UsbSettingsControl.USBMODE_CHARGE, false);
                    mNeedSleep = true;
                }
            } else {
                if (UsbSettingsControl.getUsbConnected(mContext)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;

                    mNeedSleep = true;
                }
            }
            finish();
        } else if (preference == mMediasync) {
            if (Utils.isSupportUSBMultipleConfig(mContext)
                    && !"SPR".equals(Config.getOperator())) {
                mCurrentFunction = UsbManagerConstants.USB_FUNCTION_AUTO_CONF;
                Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_AUTO_CONF");
            } else {
                if ("VZW".equals(Config.getOperator())) {
                    mCurrentFunction = UsbManagerConstants.USB_FUNCTION_AUTO_CONF;
                    Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_AUTO_CONF");
                } else {
                    mCurrentFunction = UsbManagerConstants.USB_FUNCTION_MTP_ONLY;
                    Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_MTP_ONLY");
                }
            }
            updateToggles(mCurrentFunction);

            if (Config.getOperator().equals(Config.VZW)) {
                UsbSettingsControl.writeToFile(UsbSettingsControl.AUTORUN_USBMODE,
                        UsbSettingsControl.USBMODE_AUTOCONF);

                if (UsbSettingsControl.getUsbConnected(mContext)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;

                    doSleep(20);
                    UsbSettingsControl.changeAutorunMode(mContext, UsbSettingsControl.USBMODE_AUTOCONF,
                            false);
                    mNeedSleep = true;
                }
                finish();
            } else {
                if (UsbSettingsControl.isDirectAutorunModel()) {
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        if (!UsbSettingsControl.changeAutorunMode(mContext,
                                UsbSettingsControl.USBMODE_MTP, true)) {
                            UsbSettingsControl.mActivityUsbModeChange = true;
                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                            mDefaultFunction = mCurrentFunction;
                            finish();
                        } else {
                            callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                        }
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                        if (!UsbSettingsControl.isDisconnectBugModel()) {
                            UsbSettingsControl.changeAutorunMode(mContext,
                                    UsbSettingsControl.USBMODE_MTP, false);
                        }
                        mNeedSleep = true;
                        finish();
                    }
                } else {
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        UsbSettingsControl.mActivityUsbModeChange = true;
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;

                        mNeedSleep = true;
                    }
                    finish();
                }
            }
        } else if (preference == mTether) {
            Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_TETHER");
            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_TETHER;
            updateToggles(mCurrentFunction);

            if (Config.getOperator().equals(Config.VZW)) {
                return false;
            } else {
                if (UsbSettingsControl.isDirectAutorunModel()) {
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        if (!UsbSettingsControl.changeAutorunMode(mContext,
                                UsbSettingsControl.USBMODE_TETHER, true)) {
                            UsbSettingsControl.callTetherPopup(mContext);
                        } else {
                            callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                        }
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                        if (!UsbSettingsControl.isDisconnectBugModel()) {
                            UsbSettingsControl.changeAutorunMode(mContext,
                                    UsbSettingsControl.USBMODE_TETHER, false);
                        }
                        mNeedSleep = true;
                        finish();
                    }
                } else {
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        UsbSettingsControl.callTetherPopup(mContext);
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                        mNeedSleep = true;
                        finish();
                    }
                }
            }
        } else if (preference == mPcsuite) {
            Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_PC_SUITE");
            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_PC_SUITE;
            updateToggles(mCurrentFunction);

            if (Config.getOperator().equals(Config.VZW)) {
                return false;
            } else {
                if (UsbSettingsControl.isDirectAutorunModel()) {
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        if (!UsbSettingsControl.changeAutorunMode(mContext,
                                UsbSettingsControl.USBMODE_PCSUITE, true)) {
                            UsbSettingsControl.mActivityUsbModeChange = true;
                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                            mDefaultFunction = mCurrentFunction;
                            finish();
                        } else {
                            callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                        }
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                        if (!UsbSettingsControl.isDisconnectBugModel()) {
                            UsbSettingsControl.changeAutorunMode(mContext,
                                    UsbSettingsControl.USBMODE_PCSUITE, false);
                        }
                        mNeedSleep = true;
                        finish();
                    }
                } else {
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        UsbSettingsControl.mActivityUsbModeChange = true;
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                        mNeedSleep = true;
                    }
                    finish();
                }
            }

        } else if (preference == mPtp) {
            Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_PTP_ONLY");
            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_PTP_ONLY;
            updateToggles(mCurrentFunction);

            if (Config.getOperator().equals(Config.VZW)) {
                UsbSettingsControl.writeToFile(UsbSettingsControl.AUTORUN_USBMODE,
                        UsbSettingsControl.USBMODE_PTP);

                if (UsbSettingsControl.getUsbConnected(mContext)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;

                    doSleep(20);
                    UsbSettingsControl.changeAutorunMode(mContext, UsbSettingsControl.USBMODE_PTP,
                            false);
                    mNeedSleep = true;
                }
            } else {
                if (UsbSettingsControl.getUsbConnected(mContext)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;

                    mNeedSleep = true;
                }
            }
            finish();
        } else if (preference == mMassStorage) {
            Log.d(TAG, "[AUTORUN] onPreferenceChange() : USB_FUNCTION_MASS_STORAGE");
            mCurrentFunction = UsbManager.USB_FUNCTION_MASS_STORAGE;
            updateToggles(mCurrentFunction);

            if (Config.getOperator().equals(Config.VZW)) {
                UsbSettingsControl.writeToFile(UsbSettingsControl.AUTORUN_USBMODE,
                        UsbSettingsControl.USBMODE_UMS);

                if (UsbSettingsControl.getUsbConnected(mContext)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;

                    UsbSettingsControl.setMassStorage(mContext, true);
                } else {
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;

                    doSleep(20);
                    UsbSettingsControl.changeAutorunMode(mContext, UsbSettingsControl.USBMODE_UMS,
                            false);
                    mNeedSleep = true;
                }
                finish();
            } else {
                if (UsbSettingsControl.isMassSeperatedModel()) {
                    startUmsSelection();
                } else {
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        UsbSettingsControl.mActivityUsbModeChange = true;
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;

                        UsbSettingsControl.setMassStorage(mContext, true);
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;

                        mNeedSleep = true;
                    }
                    finish();
                }
            }
        } else if (preference == mInternet) {
            updateToggles(UsbManagerConstants.USB_FUNCTION_TETHER);

            if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                mInternetConnection = 0;
            }
            else if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)) {
                mInternetConnection = 1;
            }
            else {
                mInternetConnection = -1;
            }
            callPopup(UsbSettingsControl.DIALOG_INTERNET_CONNECTION);
        }

        return true;
    }

    private void callPopup(int dialogId) {
        Log.d(TAG, "[AUTORUN] callPopup() : popup id=" + dialogId);

        Message m = null;
        m = mHandler.obtainMessage(dialogId);
        mHandler.sendMessage(m);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            try {
                if (getActivity() == null) {
                    return;
                }
                switch (message.what) {
                case UsbSettingsControl.DIALOG_FIRST_AUTORUN:
                    if (mFirstAutorunDialog == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_first_autorun, null);
                        TextView mSummary2 = (TextView)dialogView.findViewById(R.id.textview_desc2);
                        mSummary2.setVisibility(View.GONE);
                        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                            TextView mSummary1 = (TextView)dialogView.findViewById(R.id.textview_desc1);
                            mSummary1.setVisibility(View.GONE);
                            TextView mContent = (TextView)dialogView.findViewById(R.id.textview_content);
                            mContent.setText(R.string.autorun_first_dialog_content_new);
                        }
                        builder.setView(dialogView);

                        final CheckBox donotshow = (CheckBox)dialogView
                                .findViewById(R.id.checkbox_donotshow);
                        donotshow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //UsbSettingsControl.setAutorunDialogDoNotShow(mContext, donotshow.isChecked());
                                mChekshowDoNot = donotshow.isChecked();
                            }
                        });
                        donotshow.setChecked(mSetChekshowDoNot);

                        builder.setTitle(R.string.autorun_dialog_title_new);
                        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    if (!UsbSettingsControl.mActivityUsbModeChange
                                            && mDefaultFunction
                                                    .equals(UsbManagerConstants.USB_FUNCTION_TETHER)
                                            && !UsbSettingsControl.getTetherStatus(mContext)
                                            && !Config.getOperator().equals(Config.VZW)
                                            && !UsbSettingsControl.isAutorunTimer()) {
                                        UsbSettingsControl.callTetherPopup(mContext);
                                    }
                                    return true;
                                }
                                return false;
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                        if (!UsbSettingsControl.mActivityUsbModeChange
                                                && mDefaultFunction
                                                        .equals(UsbManagerConstants.USB_FUNCTION_TETHER)
                                                && !UsbSettingsControl.getTetherStatus(mContext)
                                                && !Config.getOperator().equals(Config.VZW)
                                                && !UsbSettingsControl.isAutorunTimer()) {
                                            UsbSettingsControl.callTetherPopup(mContext);
                                        }
                                    }
                                });
                        builder.setPositiveButton(R.string.autorun_dialog_install,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        UsbSettingsControl.setAutorunDialogDoNotShow(mContext,
                                                donotshow.isChecked());
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                        startAutorun();
                                    }
                                });
                        mFirstAutorunDialog = builder.create();
                        mFirstAutorunDialog.show();
                    } else {
                        mFirstAutorunDialog.show();
                    }
                    UsbSettingsControl.mDirectAutorun = true;
                    break;
                case UsbSettingsControl.DIALOG_INSTALL_AUTORUN:
                    if (mInstallAutorunDialog == null) {
                        String dialog_message = getResources().getString(R.string.autorun_install_dialog_content);
                        if ("CN".equals(Config.getCountry())
                            || com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                            dialog_message = getResources().getString(R.string.autorun_first_dialog_content_new);
                        }
                        mInstallAutorunDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.autorun_dialog_title_new)
                                .setMessage(dialog_message)
                                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                    @Override
                                    public boolean onKey(DialogInterface dialog, int keyCode,
                                            KeyEvent event) {
                                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                                            if (dialog != null) {
                                                dialog.dismiss();
                                            }
                                            return true;
                                        }
                                        return false;
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (dialog != null) {
                                                    dialog.dismiss();
                                                }
                                            }
                                        })
                                .setPositiveButton(R.string.autorun_dialog_install,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (dialog != null) {
                                                    dialog.dismiss();
                                                }
                                                startAutorun();

                                            }
                                        })
                                .create();
                        mInstallAutorunDialog.show();
                    } else {
                        mInstallAutorunDialog.show();
                    }
                    break;
                case UsbSettingsControl.DIALOG_PROGRESS:
                    if ("VZW".equals(Config.getOperator())) {
                        break;
                    }

                    if (mProgress == null) {
                        mProgress = new ProgressDialog(getActivity());
                        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                        String cancelString = "";
                        if (UsbSettingsControl.isDirectAutorunModel()) {
                            mProgress.setMessage(getString(R.string.autorun_usb_driver_check2));
                            cancelString = getString(R.string.skip_label);
                        } else {
                            mProgress.setTitle(R.string.autorun_dialog_title_new);
                            mProgress.setMessage(getString(R.string.autorun_usb_driver_check3_new));
                            cancelString = getString(android.R.string.cancel);
                        }

                        mProgress.setCancelable(false);
                        mProgress.setButton(cancelString, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int whitch) {
                                UsbSettingsControl.cancelAutorunTimer();
                                if (dialog != null) {
                                    dialog.dismiss();
                                }

                                if (mCurrentFunction
                                        .equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                                    UsbSettingsControl.mActivityUsbModeChange = true;
                                    mUsbManager.setCurrentFunction(
                                            UsbManagerConstants.USB_FUNCTION_TETHER, true);
                                    mDefaultFunction = mCurrentFunction;

                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        if (DEBUG) {
                                            Log.d(TAG, "[AUTORUN] waiting exception");
                                        }
                                    }

                                    UsbSettingsControl.callTetherPopup(mContext);
                                    return;
                                } else {
                                    if (Config.getOperator().equals(Config.VZW)
                                            && (mCurrentFunction.equals("cdrom_storage")
                                            || mCurrentFunction.equals("cdrom_storage,adb"))) {
                                        if (UsbSettingsControl.isMassStorageSupport(mContext)) {
                                            mCurrentFunction = UsbManager.USB_FUNCTION_MASS_STORAGE;
                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                            mDefaultFunction = mCurrentFunction;
                                            UsbSettingsControl.setMassStorage(mContext, true);
                                        } else {
                                            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_MTP_ONLY;
                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                            mDefaultFunction = mCurrentFunction;
                                        }
                                    } else {
                                        UsbSettingsControl.mActivityUsbModeChange = true;
                                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                        mDefaultFunction = mCurrentFunction;
                                    }
                                }

                                if (UsbSettingsControl.mActivityFinish) {
                                    if (DEBUG) {
                                        Log.d(TAG, "[AUTORUN] DIALOG_PROGRESS : finish");
                                    }
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    finish();
                                } else {
                                    if (DEBUG) {
                                        Log.d(TAG, "[AUTORUN] DIALOG_PROGRESS : Do not finish");
                                    }
                                    UsbSettingsControl.mActivityFinish = true;
                                }
                            }
                        });
                        mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                            }
                        });
                        mProgress.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    UsbSettingsControl.cancelAutorunTimer();
                                    mProgress.cancel();

                                    //                                        String value = UsbSettingsControl.readToFile(UsbSettingsControl.AUTORUN_USBMODE);
                                    //                                        if (value.equals(UsbSettingsControl.USBMODE_MTP)) {
                                    //                                            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_MTP_ONLY;
                                    //                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                    //                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                    //                                            mDefaultFunction = mCurrentFunction;
                                    //                                            updateToggles(mCurrentFunction);
                                    //                                        } else if (value.equals(UsbSettingsControl.USBMODE_TETHER)) {
                                    //                                            if (UsbSettingsControl.mActivityFinish) {
                                    //                                                if (DEBUG) Log.d(TAG, "[AUTORUN] mHandler() : finish");
                                    //                                                UsbSettingsControl.connectUsbTether(mContext, false);
                                    //
                                    //                                                mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                                    //                                                updateToggles(mCurrentFunction);
                                    //
                                    //                                                UsbSettingsControl.mActivityUsbModeChange = true;
                                    //                                                mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                    //                                                mDefaultFunction = mCurrentFunction;
                                    //
                                    //                                                dialog.dismiss();
                                    //                                                finish();
                                    //                                            } else {
                                    //                                                if (DEBUG) Log.d(TAG, "[AUTORUN] mHandler() : Do not finish");
                                    //                                                UsbSettingsControl.mActivityFinish = true;
                                    //
                                    //                                                UsbSettingsControl.mActivityUsbModeChange = true;
                                    //                                                mUsbManager.setCurrentFunction(UsbManagerConstants.USB_FUNCTION_TETHER, true);
                                    //                                                mDefaultFunction = mCurrentFunction;
                                    //                                                UsbSettingsControl.callTetherPopup(mContext);
                                    //                                            }
                                    //                                            return true;
                                    //                                        } else if (value.equals(UsbSettingsControl.USBMODE_PCSUITE)) {
                                    //                                            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_PC_SUITE;
                                    //                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                    //                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                    //                                            mDefaultFunction = mCurrentFunction;
                                    //                                            updateToggles(mCurrentFunction);
                                    //                                        }

                                    if (mCurrentFunction
                                            .equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
                                        //                                            UsbSettingsControl.connectUsbTether(mContext, false);
                                        //
                                        //                                            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                                        //                                            updateToggles(mCurrentFunction);
                                        //
                                        //                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                        //                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                        //                                            mDefaultFunction = mCurrentFunction;
                                        //                                            UsbSettingsControl.mActivityFinish = false;
                                        UsbSettingsControl.mActivityUsbModeChange = true;
                                        mUsbManager.setCurrentFunction(
                                                UsbManagerConstants.USB_FUNCTION_TETHER, true);
                                        mDefaultFunction = mCurrentFunction;

                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            if (DEBUG) {
                                                Log.d(TAG, "[AUTORUN] waiting exception");
                                            }
                                        }

                                        UsbSettingsControl.callTetherPopup(mContext);
                                        return true;
                                    } else {
                                        UsbSettingsControl.mActivityUsbModeChange = true;
                                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                        mDefaultFunction = mCurrentFunction;
                                    }

                                    if (UsbSettingsControl.mActivityFinish) {
                                        if (DEBUG) {
                                            Log.d(TAG, "[AUTORUN] DIALOG_PROGRESS : finish");
                                        }
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                        finish();
                                    } else {
                                        if (DEBUG) {
                                            Log.d(TAG, "[AUTORUN] DIALOG_PROGRESS : Do not finish");
                                        }
                                        UsbSettingsControl.mActivityFinish = true;
                                    }

                                    return true;
                                }
                                return false;
                            }
                        });
                    }
                    mProgress.show();
                    break;
                case UsbSettingsControl.DIALOG_INTERNET_CONNECTION:
                    Builder internetBuilder = new AlertDialog.Builder(getActivity());
                    internetBuilder.setTitle(R.string.sp_internet_connection_NORMAL);
                    internetBuilder.setSingleChoiceItems(R.array.usb_internet_conncection_entries,
                            mInternetConnection, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mInternetConnection = which;

                                    if (mInternetConnection == 0) {
                                        Log.d(TAG,
                                                "[AUTORUN] onPreferenceChange() : USB_FUNCTION_TETHER");
                                        mCurrentFunction = UsbManagerConstants.USB_FUNCTION_TETHER;
                                        updateToggles(mCurrentFunction);

                                        UsbSettingsControl.writeToFile(
                                                UsbSettingsControl.AUTORUN_USBMODE,
                                                UsbSettingsControl.USBMODE_TETHER);

                                        if (UsbSettingsControl.getUsbConnected(mContext)) {
                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                            mDefaultFunction = mCurrentFunction;
                                        } else {
                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                            mDefaultFunction = mCurrentFunction;

                                            doSleep(20);
                                            UsbSettingsControl.changeAutorunMode(mContext,
                                                    UsbSettingsControl.USBMODE_TETHER, false);
                                            mNeedSleep = true;
                                        }
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                        finish();
                                    } else if (mInternetConnection == 1) {
                                        Log.d(TAG,
                                                "[AUTORUN] onPreferenceChange() : USB_FUNCTION_PC_SUITE");
                                        mCurrentFunction = UsbManagerConstants.USB_FUNCTION_PC_SUITE;
                                        updateToggles(mCurrentFunction);

                                        UsbSettingsControl.writeToFile(
                                                UsbSettingsControl.AUTORUN_USBMODE,
                                                UsbSettingsControl.USBMODE_PCSUITE);

                                        if (UsbSettingsControl.getUsbConnected(mContext)) {
                                            UsbSettingsControl.mActivityUsbModeChange = true;
                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                            mDefaultFunction = mCurrentFunction;
                                        } else {
                                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                                            mDefaultFunction = mCurrentFunction;

                                            doSleep(20);
                                            UsbSettingsControl.changeAutorunMode(mContext,
                                                    UsbSettingsControl.USBMODE_PCSUITE, false);
                                            mNeedSleep = true;
                                        }
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                        finish();
                                    }
                                }
                            });

                    internetBuilder.setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateToggles(mCurrentFunction);
                                }
                            });
                    internetBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                        }
                    });

                    internetBuilder.show();
                    break;
                default:
                    break;

                }
            } catch (NullPointerException e) {
                Log.w(TAG, "[AUTORUN] Handle message process nullpointer exception for dialog=" + e);
            }
        }
    };

    private void cancelProgressPopup() {
        if (mProgress != null) {
            mProgress.cancel();
        }
    }

    private String getCurrentFunction() {
        String functions = SystemProperties.get("sys.usb.config", "");
        int commaIndex = functions.indexOf(',');
        if (commaIndex > 0) {
            return functions.substring(0, commaIndex);
        } else {
            return functions;
        }
    }

    private void checkEntitlement() {
        Intent send = new Intent(Intent.ACTION_MAIN);
        send.putExtra("Tether_Type", "USB");
        send.setClassName("com.android.settings", "com.android.settings.EntitlementDialogActivity");
        startActivityForResult(send, MHS_REQUEST);
    }

    private void startUmsSelection() {
        Intent send = new Intent(Intent.ACTION_MAIN);
        send.putExtra("DirectUMS", false);
        send.setClassName("com.android.settings", "com.android.settings.lge.UmsSelection");
        startActivityForResult(send, UMS_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "[AUTORUN] onActivityResult() : requestCode=" + requestCode);
        Log.d(TAG, "[AUTORUN] onActivityResult() : resultCode=" + resultCode);
        Log.d(TAG, "[AUTORUN] onActivityResult() : intent=" + intent);

        /*if (requestCode == MHS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null && intent.getExtra("Tether_Type").equals("USB")){
                    callPopup(UsbSettingsControl.DIALOG_TETHERING_ALERT);
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED){
                if (intent != null && intent.getExtra("Tether_Type").equals("USB")){
                    boolean usb_disConnected = intent.getBooleanExtra("usb_disConnected", false);

                    UsbSettingsControl.connectUsbTether(mContext, false);
                    if (usb_disConnected) {
                        // nothing
                    }
                    else {
                        mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                        updateToggles(mCurrentFunction);

                        UsbSettingsControl.mActivityUsbModeChange = true;
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                    }

                    if (UsbSettingsControl.mActivityFinish) {
                        if (DEBUG) Log.d(TAG, "[AUTORUN] onActivityResult() : finish");
                        finish();
                    } else {
                        if (DEBUG) Log.d(TAG, "[AUTORUN] onActivityResult() : Do not finish");
                        UsbSettingsControl.mActivityFinish = true;
                    }
                }
            }
        }
        else if (requestCode == TETHER_USC_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                UsbSettingsControl.mActivityUsbModeChange = true;
                mUsbManager.setCurrentFunction(mCurrentFunction, true);
                mDefaultFunction = mCurrentFunction;

                UsbSettingsControl.connectUsbTether(mContext, true);

                if (UsbSettingsControl.mActivityFinish) {
                    if (DEBUG) Log.d(TAG, "[AUTORUN] onActivityResult() : finish");
                    finish();
                } else {
                    if (DEBUG) Log.d(TAG, "[AUTORUN] onActivityResult() : Do not finish");
                    UsbSettingsControl.mActivityFinish = true;
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED){
                UsbSettingsControl.connectUsbTether(mContext, false);

                mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                updateToggles(mCurrentFunction);

                UsbSettingsControl.mActivityUsbModeChange = true;
                mUsbManager.setCurrentFunction(mCurrentFunction, true);
                mDefaultFunction = mCurrentFunction;

                if (UsbSettingsControl.mActivityFinish) {
                    if (DEBUG) Log.d(TAG, "[AUTORUN] onActivityResult() : finish");
                    finish();
                } else {
                    if (DEBUG) Log.d(TAG, "[AUTORUN] onActivityResult() : Do not finish");
                    UsbSettingsControl.mActivityFinish = true;
                }
            }
        }
        else*/ if (requestCode == UMS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                mCurrentFunction = UsbManager.USB_FUNCTION_MASS_STORAGE;
                mDefaultFunction = mCurrentFunction;
                finish();
            }
            else {
                StorageManager storageManager = (StorageManager)mContext.getApplicationContext()
                        .getSystemService(Context.STORAGE_SERVICE);
                if (storageManager == null) {
                    mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                    if (UsbSettingsControl.getUsbConnected(mContext)) {
                        UsbSettingsControl.mActivityUsbModeChange = true;
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                    } else {
                        mUsbManager.setCurrentFunction(mCurrentFunction, true);
                        mDefaultFunction = mCurrentFunction;
                    }

                    UsbSettingsControl.setMassStorage(mContext, false);
                }
                else {
                    if (storageManager.isUsbMassStorageEnabled()) {
                        UsbSettingsControl.mActivityUsbModeChange = true;
                        mCurrentFunction = UsbManager.USB_FUNCTION_MASS_STORAGE;
                        mDefaultFunction = mCurrentFunction;
                    }
                    else {
                        if (UsbSettingsControl.getUsbConnected(mContext)) {
                            mCurrentFunction = UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY;
                            UsbSettingsControl.mActivityUsbModeChange = true;
                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                            mDefaultFunction = mCurrentFunction;

                            UsbSettingsControl.setMassStorage(mContext, false);
                        } else {
                            mCurrentFunction = UsbManager.USB_FUNCTION_MASS_STORAGE;
                            mUsbManager.setCurrentFunction(mCurrentFunction, true);
                            mDefaultFunction = mCurrentFunction;
                        }
                    }
                }
                updateToggles(mCurrentFunction);
            }
        }
    }

    private boolean isAirplaneModeOn() {
        return Settings.System.getInt(getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    // LGMDM [a1-mdm-dev@lge.com][ID-MDM-75][ID-MDM-225][ID-MDM-249][ID-MDM-292][ID-MDM-250]
    // [ID-MDM-42][ID-MDM-243][ID-MDM-242][ID-MDM-235]
    private final BroadcastReceiver mLGMDMReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
                if (com.android.settings.MDMSettingsAdapter.getInstance()
                        .receiveUsbPolicyChangeIntent(intent)) {
                    getActivity().finish();
                }
            }
        }
    };

    // LGMDM_END

    private void doSleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            if (DEBUG) {
                Log.d(TAG, "[AUTORUN] waiting exception");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (Utils.isUI_4_1_model(mContext)) {
            if (!Config.getOperator().equals(Config.VZW)
            		&& !UsbSettingsControl.isMultiUser()
                    && UsbSettingsControl.supportAutorunMode(mContext)) {
                menu.add(0, INSTALL_PC_ID, 0,
                        getResources().getString(R.string.autorun_dialog_title_new));
            }
            menu.add(1, HELP_ID, 0,
                    getResources().getString(R.string.tethering_help_button_text));
        } else if (!Utils.supportSplitView(getActivity())) {
            inflater.inflate(R.menu.usbsettings_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (Utils.isUI_4_1_model(mContext)) {
            mMenu = menu;
            checkProperOptionMenu();
        }
    }

    public void checkProperOptionMenu() {
        if (mMenu != null && mMenu.getItem(0) != null) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (mMenu.getItem(0).getItemId() == INSTALL_PC_ID) {
                    mMenu.getItem(0).setEnabled(true);
                }
            } else {
                if (mMenu.getItem(0).getItemId() == INSTALL_PC_ID) {
                    mMenu.getItem(0).setEnabled(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Utils.isUI_4_1_model(mContext)) {
            if (item.getItemId() == HELP_ID) {
                if (Utils.supportSplitView(mContext)) {
                    startFragment(this,
                            com.android.settings.lge.ConnectivityHelperPopup.class
                                    .getCanonicalName(),
                            HELP_REQUEST, null, R.string.tethering_help_button_text);
                } else {
                    Intent send = new Intent(Intent.ACTION_MAIN);
                    send.setClassName("com.android.settings",
                            "com.android.settings.Settings$ConnectivityHelperPopupActivity");
                    startActivity(send);
                }
            } else if (item.getItemId() == INSTALL_PC_ID) {
                if (UsbSettingsControl.getUsbConnected(mContext)) {
                    callPopup(UsbSettingsControl.DIALOG_INSTALL_AUTORUN);
                }
            }
        } else {
            if (item.getItemId() == R.id.item1) {
                callPopup(UsbSettingsControl.DIALOG_INSTALL_AUTORUN);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAutorun() {
        Log.d(TAG, "[AUTORUN] startAutorun() : mDefaultFunction=" + mDefaultFunction);

        if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_CHARGE_ONLY)) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (!UsbSettingsControl.changeAutorunMode(mContext,
                        UsbSettingsControl.USBMODE_CHARGE, true)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                }
            }
        } else if (mDefaultFunction.equals(UsbManager.USB_FUNCTION_MASS_STORAGE)) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (!UsbSettingsControl.changeAutorunMode(mContext, UsbSettingsControl.USBMODE_UMS,
                        true)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                }
            }
        } else if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_MTP_ONLY)) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (!UsbSettingsControl.changeAutorunMode(mContext, UsbSettingsControl.USBMODE_MTP,
                        true)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                } else {
                    callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                }
            }
        } else if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_TETHER)) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (!UsbSettingsControl.changeAutorunMode(mContext,
                        UsbSettingsControl.USBMODE_TETHER, true)) {
                    UsbSettingsControl.callTetherPopup(mContext);
                } else {
                    callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                }
            }
        } else if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_PC_SUITE)) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (!UsbSettingsControl.changeAutorunMode(mContext,
                        UsbSettingsControl.USBMODE_PCSUITE, true)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                    finish();
                } else {
                    callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                }
            }
        } else if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_PTP_ONLY)) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (!UsbSettingsControl.changeAutorunMode(mContext, UsbSettingsControl.USBMODE_PTP,
                        true)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                    finish();
                } else {
                    callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                }
            }
        } else if (mDefaultFunction.equals(UsbManagerConstants.USB_FUNCTION_AUTO_CONF)) {
            if (UsbSettingsControl.getUsbConnected(mContext)) {
                if (!UsbSettingsControl.changeAutorunMode(mContext,
                        UsbSettingsControl.USBMODE_AUTOCONF, true)) {
                    UsbSettingsControl.mActivityUsbModeChange = true;
                    mUsbManager.setCurrentFunction(mCurrentFunction, true);
                    mDefaultFunction = mCurrentFunction;
                    finish();
                } else {
                    callPopup(UsbSettingsControl.DIALOG_PROGRESS);
                }
            }
        }
    }
}
