package com.android.settings.deviceinfo;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.settings.IconPreferenceScreen;
import com.android.settings.R;
import com.android.settings.SLog;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class NetworkTypeStrength extends PreferenceActivity {

    private static final String TAG = "NetworkTypeStrength";

    private static String s_callService = "";
    private static int s_callLevel = 0;
    private static int s_callValue = 0;
    private static String s_callTech = "";
    private static String s_dataService = "";
    private static int s_dataLevel = 0;
    private static int s_dataValue = 0;
    private static String s_dataTech = "";
    private static String s_dBm = "dBm";

    private PreferenceScreen mData_1;
    private PreferenceScreen mData_2;
    private PreferenceScreen mData_3;
    private PreferenceScreen mData_4;
    private PreferenceScreen mData_5;

    private PreferenceScreen mCall_0;
    private PreferenceScreen mCall_1;
    private PreferenceScreen mCall_2;
    private PreferenceScreen mCall_3;
    private PreferenceScreen mCall_4;
    private PreferenceScreen mCall_5;

    private String m_KEY_DATA_STATUS_0 = "data_status_0";
    private String m_KEY_DATA_STATUS_1 = "data_status_1";
    private String m_KEY_DATA_STATUS_2 = "data_status_2";
    private String m_KEY_DATA_STATUS_3 = "data_status_3";
    private String m_KEY_DATA_STATUS_4 = "data_status_4";
    private String m_KEY_DATA_STATUS_5 = "data_status_5";

    private String m_KEY_CALL_STATUS_0 = "call_status_0";
    private String m_KEY_CALL_STATUS_1 = "call_status_1";
    private String m_KEY_CALL_STATUS_2 = "call_status_2";
    private String m_KEY_CALL_STATUS_3 = "call_status_3";
    private String m_KEY_CALL_STATUS_4 = "call_status_4";
    private String m_KEY_CALL_STATUS_5 = "call_status_5";

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        makePreference();
        // updatePreference(0,0);
        SLog.i("onCreate");
        mTM = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mTM.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_5);

        removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_5);

        if (findPreference(m_KEY_CALL_STATUS_0) != null) {
            findPreference(m_KEY_CALL_STATUS_0).setTitle(
                    getString(R.string.sp_network_noservice_NORMAL));
        }

        // 2012-11-08 shlee1219.lee Network type strength Actionbar issue START
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (Utils.supportSplitView(getApplicationContext())) {
                actionBar.setIcon(R.mipmap.ic_launcher_settings);
            }
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // 2012-11-08 shlee1219.lee Network type strength Actionbar issue END

    }

    public void updateStatus() {

        cleanAllPreference();
        makePreference();
        int call = 0;
        int data = 0;

        call = s_callLevel; // +1; //shlee1219 20120507 Network type and signal
                            // strength call&data level
        data = s_dataLevel; // +1;
        updatePreference(data, call);

    }

    private void makePreference() {
        addPreferencesFromResource(R.xml.network_type_strength);

    }

    private void updatePreference(int data, int call) {
        SLog.i("updatePreference :: s_dataService = " + s_dataService);
        if (s_dataService.equals("valid") || s_dataService.equals("")
                || s_dataService.equals("invalid")) {
            if (data == 0) {

                removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_5);

            } else if (data == 1) {
                removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_5);
                mData_1 = (PreferenceScreen)findPreference(m_KEY_DATA_STATUS_1);
                mData_1.setTitle(s_dataTech + " " + s_dataValue + s_dBm);
            } else if (data == 2) {
                removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_5);
                mData_2 = (PreferenceScreen)findPreference(m_KEY_DATA_STATUS_2);
                mData_2.setTitle(s_dataTech + " " + s_dataValue + s_dBm);
            } else if (data == 3) {
                removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_5);
                mData_3 = (PreferenceScreen)findPreference(m_KEY_DATA_STATUS_3);
                mData_3.setTitle(s_dataTech + " " + s_dataValue + s_dBm);
            } else if (data == 4) {
                removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_5);
                mData_4 = (PreferenceScreen)findPreference(m_KEY_DATA_STATUS_4);
                mData_4.setTitle(s_dataTech + " " + s_dataValue + s_dBm);
            } else if (data == 5) {
                removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
                removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
                mData_5 = (PreferenceScreen)findPreference(m_KEY_DATA_STATUS_5);
                mData_5.setTitle(s_dataTech + " " + s_dataValue + s_dBm);
            }

            if (s_dataValue == 0 || s_dataTech == null) {
                SLog.i("updatePreference :: cleanDataPreference ");
                cleanDataPreference();
            }
        } else if (s_dataService.equals("noservice")) {
            removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
            removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
            removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
            removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
            removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
            removePreferenceFromScreen(m_KEY_DATA_STATUS_5);
        }

        SLog.i("updatePreference :: s_callService = " + s_callService);
        if (s_callService.equals("valid") || s_callService.equals("")
                || s_callService.equals("invalid")) {
            if (call == 0) {
                if ((!s_dataService.equals("noservice")) && (data != 0) && (s_dataValue != 0)) {
                    removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
                }
                removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_5);

            } else if (call == 1) {
                removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_5);
                mCall_1 = (PreferenceScreen)findPreference(m_KEY_CALL_STATUS_1);
                mCall_1.setTitle(s_callTech + " " + s_callValue + s_dBm);
            } else if (call == 2) {
                removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_5);
                mCall_2 = (PreferenceScreen)findPreference(m_KEY_CALL_STATUS_2);
                mCall_2.setTitle(s_callTech + " " + s_callValue + s_dBm);
            } else if (call == 3) {
                removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_5);
                mCall_3 = (PreferenceScreen)findPreference(m_KEY_CALL_STATUS_3);
                mCall_3.setTitle(s_callTech + " " + s_callValue + s_dBm);
            } else if (call == 4) {
                removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_5);
                mCall_4 = (PreferenceScreen)findPreference(m_KEY_CALL_STATUS_4);
                mCall_4.setTitle(s_callTech + " " + s_callValue + s_dBm);
            } else if (call == 5) {
                removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
                mCall_5 = (PreferenceScreen)findPreference(m_KEY_CALL_STATUS_5);
                mCall_5.setTitle(s_callTech + " " + s_callValue + s_dBm);
            }
            if (s_callValue == 0) {
                if ((!s_dataService.equals("noservice")) && (data != 0) && (s_dataValue != 0)) {
                    removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
                }
                removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
                removePreferenceFromScreen(m_KEY_CALL_STATUS_5);

            }
        } else if (s_callService.equals("noservice")) {
            removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
            removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
            removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
            removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
            removePreferenceFromScreen(m_KEY_CALL_STATUS_5);
            if ((!s_dataService.equals("noservice")) && (data != 0) && (s_dataValue != 0)) {
                removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
            } else {
                mCall_0 = (PreferenceScreen)findPreference(m_KEY_CALL_STATUS_0);
                mCall_0.setTitle(getString(R.string.sp_network_noservice_NORMAL));
            }
        }
    }

    public void cleanAllPreference() {
        // [E] WBT_309263 hsmodel.jeon@lge.com
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }
        // [E] WBT_309263 hsmodel.jeon@lge.com
    }

    public void cleanDataPreference() {
        removePreferenceFromScreen(m_KEY_DATA_STATUS_0);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_1);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_2);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_3);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_4);
        removePreferenceFromScreen(m_KEY_DATA_STATUS_5);
    }

    public void cleanCallPreference() {
        removePreferenceFromScreen(m_KEY_CALL_STATUS_0);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_1);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_2);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_3);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_4);
        removePreferenceFromScreen(m_KEY_CALL_STATUS_5);
    }

    protected void onResume() {
        super.onResume();
        Utils.set_TelephonyListener(mTM, mPhoneStateListener);
    }

    public void onPause() {
        super.onPause();
        Utils.release_TelephonyListener(mTM, mPhoneStateListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);

        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    // 2012-11-08 shlee1219.lee Network type strength Actionbar issue START
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == android.R.id.home) { // See
                                           // ActionBar#setDisplayHomeAsUpEnabled()
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 2012-11-08 shlee1219.lee Network type strength Actionbar issue END

    public static final String INTENT_KEY_SUBSCRIPTION = "phone_subscription";
    public static final int LTE = 1;
    public static final int GSM = 2;
    public static final int CDMA = 3;
    public static final int EVDO = 4;
    public static final int INVALID_DATA_RADIO = 5;

    ServiceState mServiceState;
    private int mDataServiceState = ServiceState.STATE_OUT_OF_SERVICE; // 3G, 4G
                                                                       // icon
    SignalStrength mSignalStrength;
    protected TelephonyManager mTM;

    private boolean isCdma() {
        return (mSignalStrength != null) && !mSignalStrength.isGsm();
    }

    private boolean isLTE() {
        // 3G, 4G icon
        return (mSignalStrength != null)
                && (mTM.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE);
    }

    private boolean hasService() {
        if (mServiceState != null) {
            switch (mServiceState.getState()) {
            case ServiceState.STATE_OUT_OF_SERVICE:
            case ServiceState.STATE_POWER_OFF:
                return false;

            case ServiceState.STATE_EMERGENCY_ONLY:
                if ("ATT".equals(Config.getOperator()) || "VZW".equals(Config.getOperator())) {
                    // [VS950][CFW][VZW] cwjung-for antena bar from VS870_JB
                    SLog.d(TAG, "hasService state=" + mServiceState);
                    return true;
                }
                return false;
            default:
                return true;
            }
        } else {
            return false;
        }
    }

    public static int dataRadio(ServiceState serviceState) {
        if (serviceState == null) {
            SLog.e(TAG, "Service state not updated");
            return INVALID_DATA_RADIO;
        }
        switch (serviceState.getRadioTechnology()) {
        case ServiceState.RIL_RADIO_TECHNOLOGY_LTE:
            return LTE;
        case ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_0:
        case ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_A:
        case ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_B:
        case ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD:
            return EVDO;
        case ServiceState.RIL_RADIO_TECHNOLOGY_IS95A:
        case ServiceState.RIL_RADIO_TECHNOLOGY_IS95B:
        case ServiceState.RIL_RADIO_TECHNOLOGY_1xRTT:
            return CDMA;
        case ServiceState.RIL_RADIO_TECHNOLOGY_GPRS:
        case ServiceState.RIL_RADIO_TECHNOLOGY_EDGE:
        case ServiceState.RIL_RADIO_TECHNOLOGY_UMTS:
        case ServiceState.RIL_RADIO_TECHNOLOGY_HSDPA:
        case ServiceState.RIL_RADIO_TECHNOLOGY_HSUPA:
        case ServiceState.RIL_RADIO_TECHNOLOGY_HSPA:
        case ServiceState.RIL_RADIO_TECHNOLOGY_HSPAP :
            return GSM;
        default:
            return INVALID_DATA_RADIO;
        }
    }

    public PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            SLog.d(TAG, "onSignalStrengthsChanged signalStrength=" + signalStrength
                    + ((signalStrength == null) ? "" : (" level=" + signalStrength.getLevel())));
            mSignalStrength = signalStrength;
            UpdateSignalStrength();
        }

        @Override
        public void onServiceStateChanged(ServiceState state) {
            SLog.d(TAG, "onServiceStateChanged state=" + state.getState());
            mServiceState = state;
            if (SystemProperties.getBoolean("ro.config.combined_signal", true)) {
                /*
                 * if combined_signal is set to true only then consider data
                 * service state for signal display
                 */
                mDataServiceState = mServiceState.getDataRegState();
                SLog.d(TAG, "Combining data service state" + mDataServiceState + "for signal");
            }
            UpdateSignalStrength();
        }
    };

    private void UpdateSignalStrength() {

        if (mSignalStrength == null || mServiceState == null) {
            return;
        }

        SLog.d(TAG, "sendRssi()=" + mServiceState.getRadioTechnology());
        if (hasService()) {
            if (isCdma()) {
                s_callLevel = mSignalStrength.getCdmaLevel();
                s_callValue = mSignalStrength.getCdmaDbm();
                s_callTech = "1X";
                s_callService = "valid";
            } else {
                if (isLTE()) {
                    //s_callLevel = mSignalStrength.getCdmaLevel();
                    //s_callValue = mSignalStrength.getCdmaDbm();
                    s_callTech = "";
                    s_callService = "noservice";
                } else if ((mServiceState.getRadioTechnology()
                            == ServiceState.RIL_RADIO_TECHNOLOGY_GPRS)
                        || (mServiceState.getRadioTechnology()
                            == ServiceState.RIL_RADIO_TECHNOLOGY_EDGE)
                        || (mServiceState.getRadioTechnology()
                            == ServiceState.RIL_RADIO_TECHNOLOGY_GSM)) {
                    s_callLevel = mSignalStrength.getGsmLevel();
                    s_callValue = mSignalStrength.getGsmDbm();
                    s_callTech = "2G";
                    s_callService = "valid";
                } else if ((mServiceState.getRadioTechnology()
                == ServiceState.RIL_RADIO_TECHNOLOGY_UNKNOWN)) {
                    s_callLevel = mSignalStrength.getGsmLevel();
                    s_callValue = mSignalStrength.getGsmDbm();
                    s_callTech = "  ";
                    s_callService = "valid";
                } else {
                    s_callService = "noservice";
                }
            }
        } else {
            s_callService = "noservice";
        }

        if (mDataServiceState == ServiceState.STATE_IN_SERVICE) {
            // Get the appropriate data radio level
            int radio = dataRadio(mServiceState);
            switch (radio) {
            case LTE:
                s_dataTech = "4G";
                s_dataLevel = mSignalStrength.getLteLevel();
                s_dataValue = mSignalStrength.getLteRsrp();
                s_dataService = "valid";
                break;
            case GSM:
                if ((mServiceState.getRadioTechnology()
                            != ServiceState.RIL_RADIO_TECHNOLOGY_GPRS)
                        && (mServiceState.getRadioTechnology()
                            != ServiceState.RIL_RADIO_TECHNOLOGY_EDGE)
                        && (mServiceState.getRadioTechnology()
                            != ServiceState.RIL_RADIO_TECHNOLOGY_GSM)) {
                    s_dataTech = "3G";
                    s_dataLevel = mSignalStrength.getGsmLevel();
                    s_dataValue = mSignalStrength.getGsmDbm();
                    s_dataService = "valid";
                } else {
                    s_dataService = "noservice";
                }
                break;
            case EVDO:
                s_dataTech = "3G";
                s_dataLevel = mSignalStrength.getEvdoLevel();
                s_dataValue = mSignalStrength.getEvdoDbm();
                s_dataService = "valid";
                break;
            case CDMA:
            case INVALID_DATA_RADIO:
            default:
                s_dataService = "noservice";
                break;
            }
        } else {
            s_dataService = "noservice";
        }

        if (!hasService() && (mDataServiceState != ServiceState.STATE_IN_SERVICE)) {
            s_dataService = "noservice";
            s_callService = "noservice";
        }
        updateStatus();
    }

}
