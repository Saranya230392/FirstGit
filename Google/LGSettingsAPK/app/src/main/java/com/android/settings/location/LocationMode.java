/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.settings.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.location.XT.IXTSrv;
import com.android.location.XT.IXTSrvCb;
import com.android.location.XT.IXTSrvCb.Stub;
import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

/**
 * A page with 3 radio buttons to choose the location mode.
 *
 * There are 3 location modes when location access is enabled:
 *
 * High accuracy: use both GPS and network location.
 *
 * Battery saving: use network location only to reduce the power consumption.
 *
 * Sensors only: use GPS location only.
 */
public class LocationMode extends LocationSettingsBase
        implements RadioButtonPreference.OnClickListener {
    private static final String KEY_HIGH_ACCURACY = "high_accuracy";
    private RadioButtonPreference mHighAccuracy;
    private static final String KEY_BATTERY_SAVING = "battery_saving";
    private RadioButtonPreference mBatterySaving;
    private static final String KEY_SENSORS_ONLY = "sensors_only";
    private RadioButtonPreference mSensorsOnly;
    private WrappingIZatSwitchPreference mIZat;
    private static final String KEY_LOCATION_IZAT = "location_izat";
    private static final String KEY_ENHANCED_LOCATION = "enhanced_location";
    private static final String TAG = "LocationMode";

    private static final int IZAT_MENU_TEXT = 0;
    private static final int IZAT_SUB_TITLE_TEXT = 1;
    private static final int POPUP_BOX_DISAGREE = 0;
    private static final int POPUP_BOX_AGREE = 1;
    private static final int PRINT = 1;

    private IXTSrv mXTService = null;
    private XTServiceConnection mServiceConn = null;
    //This variable is used to record the IZat service connection result
    private boolean mIzatConnResult = false;

    // Add switch for tablet. [S]
    SettingsBreadCrumb mBreadCrumb;
    // Add switch for tablet. [E]

    //This is the IZat handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case PRINT:
                if (POPUP_BOX_DISAGREE == msg.arg1) {
                    mIZat.setChecked(false);
                } else if (POPUP_BOX_AGREE == msg.arg1) {
                    mIZat.setChecked(true);
                }
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };

    private IXTSrvCb mCallback = new IXTSrvCb.Stub() {
        public void statusChanged(boolean status) {
            if (false == status)
            {
                mHandler.sendMessage(mHandler.obtainMessage(PRINT, 0, 0));
            } else
            {
                mHandler.sendMessage(mHandler.obtainMessage(PRINT, 1, 0));
            }
        }
    };

    /**
     * Bind Izat service
     */
    private void initUserPrefService() {
        mServiceConn = new XTServiceConnection();
        Intent i = new Intent(IXTSrv.class.getName());
        i.setPackage("com.qualcomm.location.XT");
        mIzatConnResult = getActivity().bindService(i, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * IZat service connection
     */
    private class XTServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXTService = IXTSrv.Stub.asInterface((IBinder)service);
            Log.d(TAG, "onServiceConnected, service=" + mXTService);
            try {
                if (null != mXTService) {
                    String izatMenuTitle = mXTService.getText(IZAT_MENU_TEXT);
                    String izatSubtitle = mXTService.getText(IZAT_SUB_TITLE_TEXT);
                    if (null != mIZat) {
                        mIZat.setTitle(izatMenuTitle);
                        mIZat.setSummary(Html.fromHtml(izatSubtitle));
                    }
                    mXTService.registerCallback(mCallback);
                }
            } catch (RemoteException e) {
                Log.d(TAG, "Failed connecting service!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (null != mXTService) {
                try {
                    mXTService.unregisterCallback(mCallback);
                } catch (RemoteException e) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                    Log.d(TAG, "onServiceDisconnected!");
                }
                mXTService = null;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initUserPrefService();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mServiceConn);
    }

    @Override
    public void onResume() {
        super.onResume();
        // [S] Add switch for tablet.
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = SettingsBreadCrumb.get(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
        // [E] Add switch for tablet.          
        createPreferenceHierarchy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();

        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.location_mode);
        root = getPreferenceScreen();

        mHighAccuracy = (RadioButtonPreference)root.findPreference(KEY_HIGH_ACCURACY);
        mBatterySaving = (RadioButtonPreference)root.findPreference(KEY_BATTERY_SAVING);
        mSensorsOnly = (RadioButtonPreference)root.findPreference(KEY_SENSORS_ONLY);
        mHighAccuracy.setOnClickListener(this);
        mBatterySaving.setOnClickListener(this);
        mSensorsOnly.setOnClickListener(this);

        PreferenceCategory enhancedLocation = (PreferenceCategory)
                root.findPreference(KEY_ENHANCED_LOCATION);
        mIZat = (WrappingIZatSwitchPreference)root.findPreference(KEY_LOCATION_IZAT);
        if (!mIzatConnResult) {
            root.removePreference(enhancedLocation);
        } else {
            try {
                if (null != mXTService) {
                    String izatMenuTitle = mXTService.getText(IZAT_MENU_TEXT);
                    String izatSubtitle = mXTService.getText(IZAT_SUB_TITLE_TEXT);
                    mIZat.setTitle(izatMenuTitle);
                    mIZat.setSummary(Html.fromHtml(izatSubtitle));
                    mIZat.setChecked(mXTService.getStatus());
                }
            } catch (RemoteException e) {
                Log.d(TAG, "Service connection error!");
            }
        }
        if (null != mIZat) {
            mIZat.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            final boolean chooseValue = (Boolean)newValue;
                            if (chooseValue) {
                                try {
                                    if (null != mXTService) {
                                        mXTService.showDialog();
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    if (null != mXTService) {
                                        mXTService.disable();
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            return true;
                        }
                    }
                    );
        }

        if (Utils.isWifiOnly(getActivity())) {
            mHighAccuracy.setSummary(getString(R.string.location_mode_high_accuracy_summary));
            mBatterySaving.setSummary(getString(R.string.location_mode_battery_saving_summary));
        }

        if ("AU".equals(Config.getCountry())
                || "NZ".equals(Config.getCountry())) {
            mHighAccuracy.setTitle(getString(R.string.location_mode_high_accuracy_add_title_ex));
            mHighAccuracy
                    .setSummary(getString(R.string.location_mode_high_accuracy_description_new));
            mSensorsOnly.setTitle(getString(R.string.location_mode_sensors_only_add_title_ex));
            mSensorsOnly.setSummary(getString(R.string.location_mode_high_accuracy_description_ex));
        }

        refreshLocationMode();
        return root;
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        if (activated == null) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(false);
        } else if (activated == mHighAccuracy) {
            mHighAccuracy.setChecked(true);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(false);
        } else if (activated == mBatterySaving) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(true);
            mSensorsOnly.setChecked(false);
        } else if (activated == mSensorsOnly) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(true);
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if ((Config.DCM).equals(Config.getOperator())) {
            doGpsPopupSenario(emiter);
            return;
        }
        int mode = Settings.Secure.LOCATION_MODE_OFF;
        if (emiter == mHighAccuracy) {
            mode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
        } else if (emiter == mBatterySaving) {
            mode = Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
        } else if (emiter == mSensorsOnly) {
            mode = Settings.Secure.LOCATION_MODE_SENSORS_ONLY;
        }
        setLocationMode(mode);
    }

    private void doGpsPopupSenario(RadioButtonPreference emiter) {
        int nPrevMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);

        if (emiter == mHighAccuracy) {
            if (nPrevMode == Settings.Secure.LOCATION_MODE_BATTERY_SAVING) {
                showGpsModeChangeDialog(nPrevMode, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            } else {
                setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            }
        } else if (emiter == mBatterySaving) {
            setLocationMode(Settings.Secure.LOCATION_MODE_BATTERY_SAVING);
        } else if (emiter == mSensorsOnly) {
            if (nPrevMode == Settings.Secure.LOCATION_MODE_BATTERY_SAVING) {
                showGpsModeChangeDialog(nPrevMode, Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
            } else {
                setLocationMode(Settings.Secure.LOCATION_MODE_SENSORS_ONLY);
            }
        }
    }

    @Override
    public void onModeChanged(int mode, boolean restricted) {
        switch (mode) {
        case Settings.Secure.LOCATION_MODE_OFF:
            updateRadioButtons(null);
            break;
        case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
            updateRadioButtons(mSensorsOnly);
            break;
        case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
            updateRadioButtons(mBatterySaving);
            break;
        case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
            updateRadioButtons(mHighAccuracy);
            break;
        default:
            break;
        }

        boolean enabled = (mode != Settings.Secure.LOCATION_MODE_OFF) && !restricted;
        mHighAccuracy.setEnabled(enabled);
        mBatterySaving.setEnabled(enabled);
        mSensorsOnly.setEnabled(enabled);
        // LGMDM [a1-mdm-dev@lge.com][ID-MDM-79][ID-MDM-227][ID-MDM-189]
        if (com.lge.config.ConfigBuildFlags.CAPP_MDM) {
            com.android.settings.MDMSettingsAdapter.getInstance().setLocationEnableMenuKK(
                    getPreferenceScreen());
        }
        // LGMDM_END
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_location_access;
    }
}

class WrappingIZatSwitchPreference extends SwitchPreference {

    private final Listener mListener = new Listener();

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
                return;
            }
            setChecked(isChecked);
        }
    }

    public WrappingIZatSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
    }

    public WrappingIZatSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_switch);
    }

    @Override
    protected void onClick() {
        // TODO Auto-generated method stub
        //super.onClick();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView title = (TextView)view.findViewById(android.R.id.title);
        View checkableView = view.findViewById(R.id.switchWidget);
        ImageView dividerImage = (ImageView)view.findViewById(R.id.switchImage);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(3);
        }

        if (checkableView != null && checkableView instanceof Checkable) {
            ((Checkable)checkableView).setChecked(super.isChecked());

            if (checkableView instanceof Switch) {
                final Switch switchView = (Switch)checkableView;
                switchView.setOnCheckedChangeListener(mListener);
            }
            // [SWITCH_SOUND]
            checkableView.setOnClickListener(new CompoundButton.OnClickListener() {
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });
        }

        if (dividerImage != null) {
            dividerImage.setVisibility(View.GONE);
        }
    }
}
