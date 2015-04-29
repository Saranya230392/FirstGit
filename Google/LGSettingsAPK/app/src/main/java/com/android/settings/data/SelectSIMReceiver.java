//package com.android.systemui.data;
package com.android.settings.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.os.SystemProperties;

import com.lge.constants.SettingsConstants;
import com.android.settings.Utils;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import android.provider.Settings;

public class SelectSIMReceiver extends BroadcastReceiver {
    final static String TAG = "SelectSIMReceiver";

    final static String SIM_CHANGED_INFO = "com.lge.intent.action.SIM_CHANGED_INFO";
    final static String SETUPWIZARD_DONE = "android.intent.action.SETUPWIZARD_DONE";
    final static String SIM_CHANGE_DATA_ENABLE_POPUP = "android.intent.action.SETTINGS.SIM_CHANGE_DATA_ENABLE_POPUP";
    private static final String G1_FACTORY_PROPERTY = "ro.factorytest";
    private static final String P2_FACTORY_PROPERTY = "sys.factory.qem";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();

        Log.d(TAG, "onReceive : " + action);

        /*
          *     0. Check whether VDF_UK version
          *     1. Check whether VDF_UK SIM (MCC:234, MNC:15)
          */
        if (SystemProperties.get("sys.allautotest.run").equals("true")) {
            Log.d(TAG, "sys.allautotest.run = " + SystemProperties.get("sys.allautotest.run"));
            return;
        }

        if (SIM_CHANGED_INFO.equals(action)) {
            if (isFirstBooting()) {
                if ("KDDI".equals(Config.getOperator())) {
                    //add the lte value for kddi Operater.
                    Log.i(TAG, "first boot lte_value_when_data_onoff : true ");
                    android.provider.Settings.Secure.putInt(context.getContentResolver(),
                            "lte_value_when_data_onoff", 1);
                } else if ("ORG".equals(Config.getOperator())) {
                    //add the display network name for ORG Operater.
                    Log.i(TAG, "first boot : default OPERATOR_TEXT_ON value Set true ");
                    android.provider.Settings.Secure.putInt(context.getContentResolver(),
                            SettingsConstants.Global.OPERATOR_TEXT_ON, 1);
                    Intent mIntent = new Intent();
                    mIntent.setAction("com.lge.action.STATUSBAR_OPERATOR_TEXT");
                    context.sendBroadcast(mIntent);
                }
            }

            //[s][2013-04-18][chris.won@lge.com] MX TCL - Add data consumption warning popup
            if ("TCL".equals(Config.getOperator())
                    || "UNF".equals(Config.getOperator())
                    || SystemProperties.getBoolean("persist.sys.cust.data_warning", false)) {
                Settings.Secure.putInt(
                        context.getContentResolver(),
                        SettingsConstants.Secure.DO_NOT_SHOW_AGAIN_TCL_WARN,
                        0);
            }
            //[e][2013-04-18][chris.won@lge.com] MX TCL - Add data consumption warning popup

            if ("HK".equals(Config.getCountry()) || "SG".equals(Config.getCountry())) {
                if (Utils.isHuchisonSim(context)) {
                    Log.i(TAG, "sim swap : default VOLTE value Set False for Hutchsion sim");
                    Settings.Secure.putInt(
                        context.getContentResolver(),
                        SettingsConstants.Secure.DATA_NETWORK_ENHANCED_4G_LTE_MODE, 0);
                }
            }

            // [OPERATOR.EURnD - Defect #16416] data roaming default value is true for TMobile pl
            if (Utils.isTMobileplSim(context)) {
                Settings.Global.putInt(context.getContentResolver(), 
                    Settings.Global.DATA_ROAMING + OverlayUtils.getDefaultPhoneID(context), 1);
            }
        }

        if (!Utils.isVodafoneUKSIM(context)
                && !"ATT".equals(Config.getOperator()) && !"SPR".equals(Config.getOperator())
                && !("TMO".equals(Config.getOperator()) && "US".equals(Config.getCountry())) && !"USC".equals(Config.getOperator())
                && !"ACG".equals(Config.getOperator())
                && !"VZW".equals(Config.getOperator()) && !("CCM".equals(Config.getOperator())
                && "IL".equals(Config.getCountry()))) {
            // [S][2012.03.18][youngmin.jeon][LGP700][NA] start Data enable popup
/* [S][2014.04.24][girisekhar1.a@lge.com][TLF-COM] DataSwitcher pop-up for TLF_COM */
            if (SIM_CHANGE_DATA_ENABLE_POPUP.equals(action)) {
                if ("KR".equals(Config.getCountry())
                        || ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator()))
                        || ("TLF".equals(Config.getOperator()) && "ES".equals(Config.getCountry()))
                        || ("TLF".equals(Config.getOperator()) && "COM".equals(Config.getCountry()))
                        || ("TLF".equals(Config.getOperator()) && Utils.isTlfSpainSim(context))
                        || ("ESA".equals(Config.getCountry()) && Utils.isRelianceJioSim(context))
                        || ("VDA".equals(Config.getOperator()) && "AU".equals(Config.getCountry()))
                        || ("RJIL".equals(Config.getOperator()) && "IN".equals(Config.getCountry())) ) {
                    //The mobile data popup is replaced the TLF Data switcher popup.
                    Log.i(TAG, "Data enabled popup skip === ");
                    return;
                }
                // [S][2012.03.23][youngmin.jeon][Common][Settings][Common] If SetupWizard is enabled, forbid data enabled popup.
                // [S][2012.04.10][seungeene][Common][NA] Add check routine whether LGSetupWizard is installed
                /*
                int enabledState = context.getPackageManager().getComponentEnabledSetting(
                                       new ComponentName("com.android.LGSetupWizard",
                                       "com.android.LGSetupWizard.SetupFlowController"));
                */

                int enabledState;
                if (isSetupWizardAvailable(context)) {
                    enabledState = context.getPackageManager().getComponentEnabledSetting(
                            new ComponentName("com.android.LGSetupWizard",
                                    "com.android.LGSetupWizard.SetupFlowController"));
                } else {
                    enabledState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                }
                // [E][2012.04.10][seungeene][Common][NA] Add check routine whether LGSetupWizard is installed

                Log.i(TAG, "Data enabled popup SetupWizard enabledState : " + enabledState);
                if (enabledState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        && enabledState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                    return;
                }
                // [E][2012.03.23][youngmin.jeon][Common][Settings][Common] If SetupWizard is enabled, forbid data enabled popup.

                startDataEnableDialog(context);
            }
            // [E][2012.03.18][youngmin.jeon][LGP700][NA] start Data enable popup
            return;
        }

        if (SIM_CHANGED_INFO.equals(action)) {
            /*
              *     1. Set sim_type_settings db value to 0  (0: user not select, 1: Vodafone Contract, 2: Vodafone PAYG)
              *     2. Check whether SetupWizard is enabled
              *         => Y : neglect
              *         => N : next
              *     3. Show sim type selection popup
              */
            // 1
            //[2012.04.04][seungeene][LGP700] disable code to keep previous value
            //Settings.Secure.putInt(context.getContentResolver(), SettingsConstants.Secure.SIM_TYPE_SETTINGS, 0);

            // 2
            // [S][2012.04.10][seungeene][Common][NA] Add check routine whether LGSetupWizard is installed
            /*
            int enabledState = context.getPackageManager().getComponentEnabledSetting(
                                                new ComponentName("com.android.LGSetupWizard",
                                                        "com.android.LGSetupWizard.SetupFlowController"));
            */
            int enabledState;
            if (isSetupWizardAvailable(context)) {
                enabledState = context.getPackageManager().getComponentEnabledSetting(
                        new ComponentName("com.android.LGSetupWizard",
                                "com.android.LGSetupWizard.SetupFlowController"));
            } else {
                enabledState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            }
            // [E][2012.04.10][seungeene][Common][NA] Add check routine whether LGSetupWizard is installed

            Log.i(TAG, "SetupWizard enabledState : " + enabledState);
            if (enabledState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    && enabledState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                return;
            }

            // 3
            startSelectSIMDialog(context);
        }
        // [S][2012.03.08][seungeene][LGP700][NA] Add SETUPWIZARD_DONE action
        else if (SETUPWIZARD_DONE.equals(action)) {
            boolean simChangeInfoReceived = intent
                    .getBooleanExtra(SIM_CHANGED_INFO, false /*default*/); //[2012.04.04][seungeene][LGP700] change default value

            // if in setupwizard, SIM_CHANGED_INFO intent didn't received, neglect this intent.
            if (!simChangeInfoReceived) {
                return;
            }
            int simType = Settings.Secure.getInt(context.getContentResolver(),
                    SettingsConstants.Secure.SIM_TYPE_SETTINGS, 0);
            Log.e(TAG, "SimType (0:NS,1:Post,2:Pre): " + simType);

            if (simType == 1 || simType == 2) {
                if ((!"ATT".equals(Config.getOperator()))
                        && (!"CTC".equals(Config.getOperator()))
                        && (!"CTO".equals(Config.getOperator()))
                        && (!"SPR".equals(Config.getOperator()))
                        && (!("CCM".equals(Config.getOperator())) && ("IL".equals(Config
                                .getCountry())))) {
                    startDataEnableDialog(context);
                }
            } else {
                startSelectSIMDialog(context);
            }
        }
        // [E][2012.03.08][seungeene][LGP700][NA] Add SETUPWIZARD_DONE action
    }

    private void startSelectSIMDialog(Context context) {
        Log.i(TAG, "start setup sim type popup");
        Intent i = new Intent("android.intent.action.SELECT_SIM_TYPE");
        i.putExtra("cancelable", false); //[2012.03.18][seungeene][LGP700][NA] Set cancelable for false when sim is changed.
        i.setClass(context, SelectSIMDialog.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    // [S][2012.03.18][youngmin.jeon][LGP700][NA] start Data enable popup
    private void startDataEnableDialog(Context context) {
        Log.i(TAG, "start startDataEnableDialog");
        /* LGE_CHANGE_S : support miniOS for Factory (M4) */
        if (Utils.is_miniOS()
                || !isPopupDisabled()
                || !isP2PopupDisabled()
                || isFirstBooting()
            ) {
            Log.i(TAG, "if first booting, skip the mobile data popup.");
            return;
        }
        /* LGE_CHANGE_E : support miniOS for Factory (M4) */
        Intent i = new Intent("android.intent.action.DATA_ENABLE_DIALOG");
        i.putExtra("cancelable", false); //[2012.03.18][youngmin.jeon][LGP700][NA] Set cancelable for false when sim is changed.
        i.setClass(context, DataEnableDialog.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    // [E][2012.03.18][youngmin.jeon][LGP700][NA] start Data enable popup

    // [S][2012.04.10][seungeene][Common][NA] Add check routine whether LGSetupWizard is installed
    private boolean isSetupWizardAvailable(Context context) {
        boolean bResult = true;
        try {
            context.getPackageManager().getApplicationInfo("com.android.LGSetupWizard", 0);
        } catch (Exception e) {
            bResult = false;
        }

        Log.d(TAG, "isSetupWizardAvailable : " + bResult);

        return bResult;
    }

    // [E][2012.04.10][seungeene][Common][NA] Add check routine whether LGSetupWizard is installed
    private boolean isPopupDisabled() {
        // Don't display all popup when factory test mode is on
        String factoryTestStr = SystemProperties.get(G1_FACTORY_PROPERTY);
        Log.i(TAG, "isPopupDisabled" + factoryTestStr);
        if (factoryTestStr != null && "2".equals(factoryTestStr)) {
            return false;
        }
        return true;
    }

    private boolean isP2PopupDisabled() {
        // Don't display all popup when factory test mode is on
        String factoryTestStr = SystemProperties.get(P2_FACTORY_PROPERTY);
        Log.i(TAG, "isP2PopupDisabled" + factoryTestStr);
        if (factoryTestStr != null && "1".equals(factoryTestStr)) {
            return false;
        }
        return true;
    }

    private boolean isFirstBooting() {
        if ("2".equals(SystemProperties.get("persist.radio.iccid-changed"))) {
            // "2" mean is factoryReset or Firstboot. skip the popup as VDF Requestion.
            Log.i(TAG, "The device state is Factoryreset or First reboot");
            return true;
        }
        else {
            // "1" mean is changed the sim.
            Log.i(TAG, "The device state is reboot :: "
                    + SystemProperties.get("persist.radio.iccid-changed"));
            return false;
        }
    }
}
