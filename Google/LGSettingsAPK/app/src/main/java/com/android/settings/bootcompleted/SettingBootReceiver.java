package com.android.settings.bootcompleted;

import java.util.List;

import com.android.settings.SLog;
import com.android.settings.Settings.RemoteFragmentActivity;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.constants.SettingsConstants;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.lge.constants.LGIntent;
import android.provider.Settings.SettingNotFoundException;

public class SettingBootReceiver extends BroadcastReceiver {

    private static final String TAG = "SettingBootReceiver";
    private static final String ACTION_TCL_DATA_CONSUME = "com.lge.intent.DATA_CONSUME_WARNING";
    private static final String ACTION_SHOW_DATA_CONSUME_SETUP_WIZARD = "com.lge.setupwizard.ACTION_SHOW_DATA_CONSUMPTION_WARNING";
    private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final String INTENT_KEY_ICC_STATE = "ss";
    private static final String INTENT_VALUE_ICC_LOADED = "LOADED";

    private PackageManager mPm;
    private Context mContext = null;

    private final int BOOT_COMPLETE = 0;
    private final int UNINSTALL_COMPLETE = 1;
    private final int SYSTEM_READY = 2;
    private final int mCHECK_SIM_STATE = 4;
    private final static int SHOW_DATA_CONSUME_WIZ = 5;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SYSTEM_READY:
                setKidsModeIcon();
                setStyle();
                changeAccountActivity();
                break;
            case BOOT_COMPLETE:
                setStyle();
                setKidsModeIcon();
                changeAccountActivity();
                doBootProcess();
                break;
            case mCHECK_SIM_STATE:
                showDataWarningPopupTELCEL();
                break;
            case SHOW_DATA_CONSUME_WIZ:
                String simOp = ((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE)).getSimOperator();
                if ((simOp != null) && !("".equals(simOp))) {
                    showDataWarningPopupTELCEL();
                }
                break;
            case UNINSTALL_COMPLETE:
            {
                String packageName = (String)msg.obj;
                switch (msg.arg1) {
                case PackageManager.DELETE_SUCCEEDED:
                    Log.d(TAG, "Uninstall succeed :" + packageName);
                    break;
                case PackageManager.DELETE_FAILED_DEVICE_POLICY_MANAGER:
                    Log.d(TAG, "Uninstall failed because " + packageName + " is a device admin");
                    break;
                default:
                    Log.d(TAG, "Uninstall failed for " + packageName + " with code " + msg.arg1);
                    break;
                }
            }
                break;
            default:
                break;
            }
        }

    };

    private void doBootProcess() {
        TimeZoneBoot.onReceive(mContext);
        
        showDataWarningPopupVZW();
        setRemoteFragmentActivity();
        setQuickButtonClass();
        setComponentDisabled();
        
        AntiFlickerBoot.onReceive(mContext);
        UsbSettingsBoot.onReceive(mContext);
        NightModeBoot.onReceive(mContext);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long time = System.currentTimeMillis();

        Log.d(TAG, "onReceive");
        String action = intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            mContext = context;
            Log.d(TAG, "Boot Completed intent received, action=" + action);

            Thread doThread = new Thread() {
                @Override
                public void run() {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(BOOT_COMPLETE);
                    }
                }
            };
            doThread.start();
            time = System.currentTimeMillis() - time;
            Log.d(TAG, "boot completed process end time=" + time);
        } else if (LGIntent.ACTION_SYSTEM_READY.equals(action)) { //  "com.lge.android.intent.action.SYSTEM_READY"
            mContext = context;
            Log.d(TAG, "System Ready intent received, action=" + action);
            Thread doThread = new Thread() {
                @Override
                public void run() {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(SYSTEM_READY);
                    }
                }
            };
            doThread.start();
        } else if (ACTION_SIM_STATE_CHANGED.equals(action)) {
            mContext = context;
            String stateExtra = intent.getStringExtra(INTENT_KEY_ICC_STATE);
            Log.d(TAG, "[Extra] : " + stateExtra
                    + " == '" + INTENT_VALUE_ICC_LOADED + "' ??");
            if (stateExtra != null && INTENT_VALUE_ICC_LOADED.equals(stateExtra)) {
                Thread doThread = new Thread() {
                    @Override
                    public void run() {
                        if (mHandler != null) {
                            mHandler.sendEmptyMessageDelayed(mCHECK_SIM_STATE, 3000);
                        }
                    }
                };
                doThread.start();
            }
        } else if (ACTION_SHOW_DATA_CONSUME_SETUP_WIZARD.equals(action)) {
            mContext = context;
            Log.d(TAG, "Setup Wizard finished and must show data warning, action=" + action);
            Thread doThread = new Thread() {
                @Override
                public void run() {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(SHOW_DATA_CONSUME_WIZ);
                    }
                }
            };
            doThread.start();
        }
    }

    private void changeAccountActivity() {
        PackageManager mAccountPm = mContext.getPackageManager();
        Log.d(TAG, "setAccountMenu()");
        IntentFilter accountFilter1 = new IntentFilter("android.settings.SYNC_SETTINGS");
        accountFilter1.addCategory(Intent.CATEGORY_DEFAULT);

        Intent intent1 = new Intent("android.settings.SYNC_SETTINGS");
        List<ResolveInfo> resolveInfoList = mAccountPm.queryIntentActivities(intent1,
                PackageManager.MATCH_DEFAULT_ONLY
                        | PackageManager.GET_RESOLVED_FILTER);

        final int N = resolveInfoList.size();
        ComponentName[] set = new ComponentName[N];
        int bestMatch = 0;
        for (int i = 0; i < N; i++) {
            ResolveInfo r = resolveInfoList.get(i);
            set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
            if (r.match > bestMatch) {
                bestMatch = r.match;
            }
        }

        /*ComponentName[] account_set = new ComponentName[2];
        account_set[0] = new ComponentName("com.android.settings","com.android.settings.accounts.SyncSettingsActivity");
        account_set[1] = new ComponentName("com.android.settings","com.android.settings.Settings$AccountsGroupSettingsActivity");*/

        ComponentName accountMenu = null;
        if (Utils.isSupportAccountMenu_VZW()) {
            accountMenu = new ComponentName("com.android.settings",
                    "com.android.settings.accounts.SyncSettingsActivity");
        } else {
            accountMenu = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$AccountsGroupSettingsActivity");
        }
        mAccountPm.replacePreferredActivity(accountFilter1, bestMatch, set, accountMenu);
    }

    private void switchStyle(int style) {
        IntentFilter filter = new IntentFilter("android.settings.SETTINGS");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        Intent intent = new Intent("android.settings.SETTINGS");
        List<ResolveInfo> resolveInfoList = mPm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY
                        | PackageManager.GET_RESOLVED_FILTER);

        final int N = resolveInfoList.size();
        ComponentName[] set = new ComponentName[N];
        int bestMatch = 0;
        for (int i = 0; i < N; i++) {
            ResolveInfo r = resolveInfoList.get(i);
            set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
            if (r.match > bestMatch) {
                bestMatch = r.match;
            }
        }
        if (style == 0) {
            mPm.replacePreferredActivity(filter, bestMatch, set, new ComponentName(
                    "com.android.settings", "com.android.settings.Settings"));
        } else {
            mPm.replacePreferredActivity(filter, bestMatch, set, new ComponentName(
                    "com.lge.settings.easy", "com.lge.settings.easy.EasySettings"));
        }
        changeDisplayPreferActivity(bestMatch, style);
        changeSoundPreferActivity(bestMatch, style);
        changeAirplanePreferActivity(bestMatch, style);
        changeAccountActivity(bestMatch, style);
    }

    private void changeDisplayPreferActivity(int match, int style) {
        IntentFilter displayFilter1 = new IntentFilter("android.settings.DISPLAY_SETTINGS");
        IntentFilter displayFilter2 = new IntentFilter("com.android.settings.DISPLAY_SETTINGS");

        displayFilter1.addCategory(Intent.CATEGORY_DEFAULT);
        displayFilter2.addCategory(Intent.CATEGORY_DEFAULT);

        ComponentName[] display_set = new ComponentName[2];
        display_set[0] = new ComponentName("com.lge.settings.easy",
                "com.lge.settings.easy.EasySettings");
        display_set[1] = new ComponentName("com.android.settings",
                "com.android.settings.Settings$DisplaySettingsActivity");

        ComponentName list_view_display = null;
        if (style == 0) {
            list_view_display = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$DisplaySettingsActivity");
        } else {
            list_view_display = new ComponentName("com.lge.settings.easy",
                    "com.lge.settings.easy.EasySettings");
        }
        mPm.replacePreferredActivity(displayFilter1, match, display_set, list_view_display);
        mPm.replacePreferredActivity(displayFilter2, match, display_set, list_view_display);

    }

    private void changeSoundPreferActivity(int match, int style) {
        IntentFilter soundFilter1 = new IntentFilter("android.settings.SOUND_SETTINGS");
        IntentFilter soundFilter2 = new IntentFilter("com.android.settings.SOUND_SETTINGS");

        soundFilter1.addCategory(Intent.CATEGORY_DEFAULT);
        soundFilter2.addCategory(Intent.CATEGORY_DEFAULT);

        ComponentName[] sound_set = new ComponentName[2];
        sound_set[0] = new ComponentName("com.lge.settings.easy",
                "com.lge.settings.easy.EasySettings");
        sound_set[1] = new ComponentName("com.android.settings",
                "com.android.settings.Settings$SoundSettingsActivity");

        ComponentName list_view_sound = null;
        if (style == 0) {
            list_view_sound = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$SoundSettingsActivity");
        } else {
            list_view_sound = new ComponentName("com.lge.settings.easy",
                    "com.lge.settings.easy.EasySettings");
        }
        mPm.replacePreferredActivity(soundFilter1, match, sound_set, list_view_sound);
        mPm.replacePreferredActivity(soundFilter2, match, sound_set, list_view_sound);
    }

    private void changeAirplanePreferActivity(int match, int style) {
        IntentFilter airplaneFilter1 = new IntentFilter("android.settings.AIRPLANE_MODE_SETTINGS");

        airplaneFilter1.addCategory(Intent.CATEGORY_DEFAULT);

        ComponentName[] airplane_set = new ComponentName[2];
        airplane_set[0] = new ComponentName("com.lge.settings.easy",
                "com.lge.settings.easy.EasySettings");
        airplane_set[1] = new ComponentName("com.android.settings",
                "com.android.settings.Settings$WirelessSettingsActivity");

        ComponentName list_view_airplane = null;
        if (style == 0) {
            list_view_airplane = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$WirelessSettingsActivity");
        } else {
            list_view_airplane = new ComponentName("com.lge.settings.easy",
                    "com.lge.settings.easy.EasySettings");
        }
        mPm.replacePreferredActivity(airplaneFilter1, match, airplane_set, list_view_airplane);
    }

    private void changeAccountActivity(int match, int style) {
        Log.d(TAG, "setAccountMenu()");
        IntentFilter accountFilter1 = new IntentFilter("android.settings.SYNC_SETTINGS");
        accountFilter1.addCategory(Intent.CATEGORY_DEFAULT);

        ComponentName[] account_set = new ComponentName[2];
        account_set[0] = new ComponentName("com.android.settings",
                "com.android.settings.accounts.SyncSettingsActivity");
        account_set[1] = new ComponentName("com.android.settings",
                "com.android.settings.Settings$AccountsGroupSettingsActivity");

        ComponentName accountMenu = null;
        if (Utils.isSupportAccountMenu_VZW()) {
            accountMenu = new ComponentName("com.android.settings",
                    "com.android.settings.accounts.SyncSettingsActivity");
        } else {
            accountMenu = new ComponentName("com.android.settings",
                    "com.android.settings.Settings$AccountsGroupSettingsActivity");
        }
        mPm.replacePreferredActivity(accountFilter1, match, account_set, accountMenu);
    }

    private void setStyle() {
        Log.d(TAG, "setStyle()");
        if (Utils.supportEasySettings(mContext) == false) {
            return;
        }
        int setting_style_default_value = 0;
        if ("SPR".equals(Config.getOperator()) && Utils.supportEasySettings(mContext)) {
            setting_style_default_value = 1;
            sprintDefaultStyleSet();
        }

        int style = Settings.System.getInt(mContext.getContentResolver(),
                SettingsConstants.System.SETTING_STYLE, setting_style_default_value);
        mPm = mContext.getPackageManager();
        Log.d(TAG, "SettingStyle=" + style);
        switchStyle(style);
    }

    private void sprintDefaultStyleSet() {

        try {
            Settings.System.getInt(mContext.getContentResolver(), "setting_style_spr");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            Settings.System.putInt(mContext.getContentResolver(), "setting_style_spr", 1);
            Settings.System.putInt(mContext.getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 1);
        }
    }
    
    // [START][2014-06-11][seungyeop.yeom] condition of icon for Kids mode
    private void setKidsModeIcon() {
        Log.d("TAG", "setKidsMode");
        boolean mIsOwner = UserHandle.myUserId() == UserHandle.USER_OWNER;
        if (mIsOwner) {
            Log.d("TAG", "mIsOwner, setKidsMode");
            if (Config.getFWConfigBool(mContext,
                    com.lge.R.bool.config_kidsmode_available,
                    "com.lge.R.bool.config_kidsmode_available") == true) {
                PackageManager pm = mContext.getPackageManager();
                ComponentName componentName = new ComponentName("com.android.settings",
                        "com.android.settings.users.KidsModeMainActivity");
                pm.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            }
        }
    }
    // [END]

    //[s][2013-04-18][chris.won@lge.com] MX TCL - Add data consumption warning popup
    private void showDataWarningPopupVZW() {
        if ("VZW".equals(Config.getOperator())) {
            ConnectivityManager cm = ConnectivityManager.from(mContext);

            if (mContext != null && checkInProcessingSetupWizard()) {
                Log.d(TAG, "SetupWizard alive condition");
                return;
            } else {
                Log.d(TAG, "Not SetupWizard alive condition");
            }

            if (!cm.getMobileDataEnabled()) {
                Intent intent = new Intent();
                intent.setAction(ACTION_TCL_DATA_CONSUME);
                intent.setClass(mContext, com.android.settings.data.DataWarningDialog.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (mContext != null) {
                    mContext.startActivity(intent);
                }
            }
        }
    }

    private boolean checkInProcessingSetupWizard() {
        String setupWizard = "com.android.LGSetupWizard";
        PackageManager pm = mContext.getPackageManager();
        boolean result = false;
        try {
            if (pm.getApplicationEnabledSetting(setupWizard) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                result = true;   
                // SetupWizard alive condition.
            }
        } catch (Exception e) {
        }
        return result;
    }

    private void showDataWarningPopupTELCEL() {
        if ("TCL".equals(Config.getOperator())
            || "UNF".equals(Config.getOperator())
            || SystemProperties.getBoolean("persist.sys.cust.data_warning", false)) {
            boolean mIsSimChanged =
                ("1".equals(SystemProperties.get("persist.radio.iccid-changed"))
                    || "2".equals(SystemProperties.get("persist.radio.iccid-changed")));

            if (SystemProperties.getBoolean("ro.lge.mtk_dualsim", false)) {
                mIsSimChanged = isSimChanged();
            }

            if (mIsSimChanged) {
                android.provider.Settings.Secure.putInt(
                        mContext.getContentResolver(),
                        SettingsConstants.Secure.DO_NOT_SHOW_AGAIN_TCL_WARN,
                        0);
                SLog.d(TAG, "showDataWarningPopupTELCEL SimChanged");
            }

            int dontshowCheck = android.provider.Settings.Secure.getInt(
                    mContext.getContentResolver(),
                    SettingsConstants.Secure.DO_NOT_SHOW_AGAIN_TCL_WARN, 0);
            SLog.d(TAG, "showDataWarningPopupTELCEL :" + dontshowCheck);

            // Check if device is provisioned
            int deviceProvisioned = Settings.Global.getInt(
                    mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);

            if (dontshowCheck != 1 && deviceProvisioned == 1) {
                Intent intent = new Intent();
                intent.setAction(ACTION_TCL_DATA_CONSUME);
                intent.setClass(mContext, com.android.settings.data.DataWarningDialog.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (mContext != null) {
                    mContext.startActivity(intent);
                }
            }
        }
    }

    //[e][2013-04-18][chris.won@lge.com] MX TCL - Add data consumption warning popup

    private boolean isSimChanged() {
        Log.d(TAG, "isSimChanged()");

        boolean simChanged1 = false;
        boolean simChanged2 = false;

        String actualIccid1 = "";
        String actualIccid2 = "";

        SharedPreferences sp = mContext.getSharedPreferences("GetCurrentICCID", Context.MODE_PRIVATE);

        String prevIccid1 = sp.getString("PrevIccIdSlot1", "new");
        String prevIccid2 = sp.getString("PrevIccIdSlot2", "new");

        TelephonyManager msim = TelephonyManager.getDefault();

        if (msim != null) {
            actualIccid1 = msim.getSimSerialNumber(0);
            actualIccid2 = msim.getSimSerialNumber(1);
        }

        if ((actualIccid1 != null) && !("".equals(actualIccid1))) {
            simChanged1 = !(actualIccid1.equals(prevIccid1));
        }

        if ((actualIccid2 != null) && !("".equals(actualIccid2))) {
            simChanged2 = !(actualIccid2.equals(prevIccid2));
        }

        if (simChanged1) {
            prevIccid1 = actualIccid1;

            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putString("PrevIccIdSlot1", prevIccid1);
            spEditor.commit();
        }

        if (simChanged2) {
            prevIccid2 = actualIccid2;

            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putString("PrevIccIdSlot2", prevIccid2);
            spEditor.commit();
        }

        return simChanged1 || simChanged2;
    }

    private void setRemoteFragmentActivity() {
        mPm = mContext.getPackageManager();
        if (Utils.supportRemoteFragment(mContext)) {
            mPm.setComponentEnabledSetting(
                    new ComponentName(mContext, RemoteFragmentActivity.class.getName()),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private void setQuickButtonClass() {
        if (Utils.isSharingBetweenSimHotkey(mContext.getApplicationContext())) {
            mContext.getPackageManager().setComponentEnabledSetting(
                    new ComponentName(mContext,
                            com.android.settings.Settings.HotkeySettingsActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            mContext.getPackageManager().setComponentEnabledSetting(
                    new ComponentName(mContext,
                            com.android.settings.Settings.Hotkey4thSettingsActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    /**
     * @method    setComponentDisabled()
     * @author    seungyeop.yeom
     * @brief     disable of activity for specific operator
     * @warning   except for the overlay process
     */
    private void setComponentDisabled() {
        if ("AIO".equals(Config.getOperator())) {
            Log.i("TAG", "disable DataUsageSummaryShortCutLauncherSupport");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName("com.android.settings",
                            "com.android.settings.DataUsageSummaryShortCutLauncherSupport"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d("YSY", "Exception, disable DataUsageSummaryShortCutLauncherSupport");
            }
            
            Log.i("TAG", "disable PowerSaveBatteryShortCutLauncherSupport");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName("com.android.settings",
                            "com.android.settings.PowerSaveBatteryShortCutLauncherSupport"),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d("YSY", "Exception, disable PowerSaveBatteryShortCutLauncherSupport");
            }
        }

        if (!"KDDI".equals(Config.getOperator())) {
            Log.i(TAG, "disable au");
            mContext.getPackageManager().setComponentEnabledSetting(
                new ComponentName(mContext,
                    com.android.settings.Settings.AuCustomerSupportActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        if ("TRF".equals(Config.getOperator())) {
            Log.i("TAG", "disable TetherSettingsActivity");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.Settings.TetherSettingsActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d("YSY", "Exception, disable TetherSettingsActivity");
            }

            Log.i("TAG", "disable TetherSettingsHelpUsb");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.lge.TetherSettingsHelpUsb.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d("YSY", "Exception, disable TetherSettingsHelpUsb");
            }

            Log.i("TAG", "disable UsbTetheringActivity");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.Settings.UsbTetheringActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d("YSY", "Exception, disable UsbTetheringActivity");
            }
        }

        if ("DCM".equals(Config.getOperator())) {
            Log.i("TAG", "disable ShareConnectionActivity");
            Log.i("TAG", "disable TetherNetworkSettingsActivity");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.Settings.ShareConnectionActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.Settings.TetherNetworkSettingsActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

            } catch (Exception e) {
                Log.d("YSY", "Exception, disable ShareConnectionActivity "
                        + ", TetherNetworkSettingsActivity");
            }
        }

        if (!"DCM".equals(Config.getOperator()) && !Utils.isWifiOnly(mContext)) {
            Log.i("TAG", "disable WirelessSettingsActivity");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.Settings.WirelessSettingsActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d("YSY", "Exception, disable WirelessSettingsActivity");
            }
        }

        if (!"SKT".equals(Config.getOperator())) {
            Log.i("TAG", "disable SKTPhoneMode");
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                                com.android.settings.lge.SKTPhoneMode.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                                com.android.settings.lge.SKTPhoneModeSetupWizard.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d("YSY", "Exception, disable SKTPhoneMode");
            }
        }

        if (Utils.checkPackage(mContext, "com.android.settingsaccessibility")) {
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.Settings.AccessibilitySettingsActivity.class),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d(TAG, "Exception, disable Native Accessibility");
            }
        } else {
            try {
                mContext.getPackageManager().setComponentEnabledSetting(
                        new ComponentName(mContext,
                            com.android.settings.Settings.LGAccessibilityShortcut.class),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.d(TAG, "Exception, disable LG Accessibility Shortcut");
            }
        }
    }
}
