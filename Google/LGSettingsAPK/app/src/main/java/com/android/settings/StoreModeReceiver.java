package com.android.settings;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.content.BroadcastReceiver;
import android.content.ComponentName; //[12.03.22][common][susin.park] Receiver on/off
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager; //[12.03.22][common][susin.park] Receiver on/off
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Build;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;

//LGE_CHANGE_S [donghan07.lee@lge.com 2012.02.13] Change default value for KR
import android.os.SystemProperties;

//[S] insook.kim@lge.com 2012.03.01: refer to operator
import com.android.settings.lgesetting.Config.Config;

//[E]insook.kim@lge.com 2012.03.01: refer to operator

public class StoreModeReceiver extends BroadcastReceiver {

    private static int MAXIMUM_BACKLIGHT = 255;
    private static int DEFAULT_BACKLIGHT = 0;

    private static final int SHOP_MODE_ENABLE = 1;
    private static final int SHOP_MODE_DISENABLE = 0;

    private static final int VZW_GB_UPGRADE_CASE = 1;
    private static final int VZW_ICS_NORMAL_CASE = 0;

    private static int sSHOP_MODE_SCREEN_OFF_TIMEOUT = 120000; //store mode fix value
    private static int USER_MODE_SCREEN_OFF_TIMEOUT = 0;
    private static int BRIGHTNESS_MODE = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
    private static int NIGHT_MODE = 0;
    private static boolean isSystemProperty = true;

    private static final boolean SHOP_MODE = false;
    private static final boolean USER_MODE = true;

    private static final int SYSTEM_PROPERTY_EMPTY = -1;

    private static final String PRODUCT_NAME = "ro.product.name";

    // VS920 GB upgrade support
    private static final String VZW_GB_ICS_UPGRADE = "vzw_gb_ics_upgrade";
    private boolean launchKit = false;

    Context context;
    private static final String TAG = "StoreModeReceiver";

    private int mScreenBrightnessDim;

    @Override
    public void onReceive(Context _context, Intent intent) {

        String action = intent.getAction();
        String stateExtra = intent.getStringExtra("ss");

        Log.d(TAG, "intent.getAction() : " + intent.getAction());
        Log.d(TAG, "sim state :" + stateExtra);
        context = _context;

        if (!Config.getFWConfigBool(context, com.lge.R.bool.config_hasBackLed,
                           "com.lge.R.bool.config_hasBackLed")) {
            Log.d(TAG, "Back LED don't supported.");
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_LED, 0);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_INCOMING_CALL, 0);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_ALARM, 0);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_CALL, 0);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_MESSAGES, 0);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_MISSED_EMAILS, 0);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_CALENDAR_NOTI, 0);
            Settings.System.putInt(context.getContentResolver(),
                    SettingsConstants.System.EMOTIONAL_LED_BACK_VOICE_RECORDING, 0);
            Settings.System.putInt(context.getContentResolver(),
                    "emotional_led_back_camera_timer_noti", 0);
            Settings.System.putInt(context.getContentResolver(),
                    "emotional_led_back_camera_face_detecting_noti", 0);
            Settings.System
                    .putInt(context.getContentResolver(), "notification_light_pulse_back", 0);
        }

        if (true == isStoreModeNotUse()) {
            ReceiverDisable();
            return;
        }

        if (null == stateExtra) {
            stateExtra = "empty";
        }
        try {
            setDefaultBrightnessValues();
            setDefaultScreenOffTimeoutValues();
            setDefaultBrightnessMode();
            //[S][12.04.02][VZW][susin.park] VZW only storemode set
            setVZWScreenTimeValue(_context);
            //[E][12.04.02][VZW][susin.park] VZW only storemode set
            if (!Config.getFWConfigBool(context, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available")) {
                setDefaultNightMode();
            }

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "Default value set Exception!");
        }
        setStoreModeBirghtness(context);

        if (true == isFactoryTest()) {
            return;
        }

        if (true == isPMIC()) {
            int checkShopMode = Settings.System.getInt(context.getContentResolver(),
                    "shop_mode_check", 0);
            if (checkShopMode == 1) {
                setUserDefaultPMICMode(context.getContentResolver());
            }
            return;
        }

        //if (true == isTablet()) {
        //    return;
        //}
        
        // [S][12.03.27][susin.park] userMode case - no sim model support
        // [P2 U+] checking phone number
        String product_name = SystemProperties.get(PRODUCT_NAME, "empty");
        if (true == getPhoneNumber()
                &&
                (true == product_name.equals("w3c_trf_us")
                        || true == product_name.equals("w3c_vzw")
                        || true == product_name.equals("w5c_vzw") || true == product_name
                        .equals("w5c_trf_us")
                        || context.getResources().getBoolean(com.lge.R.bool.config_uicc_simnotsupported))) {
            setUserMode(context.getContentResolver());
        }// [E][12.03.27][susin.park] userMode case - no sim model support
        else if (action.equals(Intent.ACTION_BOOT_COMPLETED) || stateExtra.equals("LOADED")) {
            Log.d(TAG, "go to StoreModeCheck()");
            StoreModeCheck(context);
        }
    }

    /* LGE_CHANGE
     * it doen't get into shop mode. (Tablet)
    */
    /*private boolean isTablet() {
        if ("tablet".equals(SystemProperties.get("ro.build.characteristics"))) { // A
            if (!Config.getOperator().equals(Config.VZW)) {
                return true;
            }
        }
        return false;
    }*/

    /* LGE_CHANGE
     * When PMIC mode, it doen't get into shop mode.
    */
    private boolean isPMIC() {
        if (SystemProperties.get("ro.lge.usbcable.info").equals("pif_56k")
                || SystemProperties.get("sys.lge.pif").equals("1")) { // I-project, G-TDR
            if (Build.PRODUCT.equals("g3_vzw")) { // Add G3_VZW requested by daegeun.yoon
                return false;
            }
            return true;
        }

        return false;
    }

    private final void StoreModeCheck(Context context) {
        boolean isStoremode;
        boolean isStoremode1;
        boolean isStoremode2;
        boolean isStoremode3;

        if (Utils.isMultiSimEnabled()) {
            if (TelephonyManager.getDefault() != null) {
                Log.d(TAG, "is MultiSIM Storemode not null");
                isStoremode1 = TelephonyManager.getDefault().hasIccCard(0);
                isStoremode2 = TelephonyManager.getDefault().hasIccCard(1);
                isStoremode = isStoremode1 || isStoremode2;
            } else {
                Log.d(TAG, "is MultiSIM Storemode null");
                isStoremode = SHOP_MODE;
            }
        }
        else if (Utils.isTripleSimEnabled()) {
            if (TelephonyManager.getDefault() != null) {
                Log.d(TAG, "is TripleSIM Storemode not null");
                isStoremode1 = TelephonyManager.getDefault().hasIccCard(0);
                isStoremode2 = TelephonyManager.getDefault().hasIccCard(1);
                isStoremode3 = TelephonyManager.getDefault().hasIccCard(2);
                isStoremode = isStoremode1 || isStoremode2 || isStoremode3;
            } else {
                Log.d(TAG, "is TripleSIM Storemode null");
                isStoremode = SHOP_MODE;
            }
        }
        else {
            if (TelephonyManager.getDefault() != null) {
                Log.d(TAG, "isStoremode not null");
                isStoremode = TelephonyManager.getDefault().hasIccCard();
            }
            else {
                Log.d(TAG, "isStoremode null");
                isStoremode = SHOP_MODE;
            }
        }
        int shop_mode = Settings.System.getInt(context.getContentResolver(), "shop_mode",
                SHOP_MODE_ENABLE);
        Settings.System.putInt(context.getContentResolver(), "shop_mode_check", shop_mode);
        //[S][susin.park][VZW] VS920 VS840 model GB support
        String product_name = SystemProperties.get("ro.product.name", "notFound");
        Log.d(TAG, "product_name : " + product_name);
        if (true) {
            if (VZW_GB_UPGRADE_CASE == Settings.System.getInt(context.getContentResolver(),
                    VZW_GB_ICS_UPGRADE, 0)) {
                // GB upgrade case : GB shop mode state use
                if (USER_MODE == isStoremode) {
                    //[S][susin.park][VZW][VS920] launchkit support code add
                    if (true == launchKit) {
                        shop_mode = SHOP_MODE_ENABLE;
                        Log.d(TAG, "VZW GB upgrade case - LaunchKit : true");
                    }
                    else {
                        shop_mode = SHOP_MODE_DISENABLE;
                        Log.d(TAG, "VZW GB upgrade case - LaunchKit : false");
                    }
                    //[E][susin.park][VZW][VS920] launchkit support code add
                    Settings.System.putInt(context.getContentResolver(), "shop_mode", shop_mode);

                }
                Settings.System.putInt(context.getContentResolver(), VZW_GB_ICS_UPGRADE,
                        VZW_ICS_NORMAL_CASE);
                Log.d(TAG, "VZW GB upgrade case");
            }
        }
        //[E][susin.park][VZW] VS920 model GB support
        ContentResolver cr = context.getContentResolver();

        try {
            if (SHOP_MODE == isStoremode && shop_mode == SHOP_MODE_ENABLE) {
                Log.d(TAG, "Store mode start!");

                //[S][susin.park][VZW][VS920] launchkit support code add
                if (launchKit == true
                        || Config.getFWConfigBool(context, com.lge.R.bool.config_lcd_oled,
                               "com.lge.R.bool.config_lcd_oled")) {
                    sSHOP_MODE_SCREEN_OFF_TIMEOUT = 60000;
                    Log.d(TAG, "SHOP_MODE_SCREEN_OFF_TIMEOUT = " + sSHOP_MODE_SCREEN_OFF_TIMEOUT);
                }
                //[E][susin.park][VZW][VS920] launchkit support code add
                setStoreMode(cr);
            } else if (USER_MODE == isStoremode && shop_mode == SHOP_MODE_ENABLE) {

                //[S][susin.park][VZW][VS920] launchkit support code add
                Log.d(TAG,
                        "lauhchkit db value : "
                                + Settings.System.getString(context.getContentResolver(),
                                        "launchkit_value"));
                Log.d(TAG, "lauhchkit boolean value : " + launchKit);
                /*if(true == Utils.isbuildProduct("i_vzw") && true == launchKit){
                    Log.d(TAG,"vs920 launchkit : true, no sim case");
                    setStoreMode(cr);
                }*/
                Log.d(TAG, "User mode start!");
                setUserMode(cr);
                //[E][susin.park][VZW][VS920] launchkit support code add
            } else {
                Log.d(TAG, "####User setting mode start!####");
                ReceiverDisable();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception in StoreModeReceiver");
        }
    }

    private void setBrightness(int brightness) {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(
                    ServiceManager.getService("power"));
            if (power != null) {
                if (Settings.System.getInt(context.getContentResolver(), "shop_mode",
                        SHOP_MODE_ENABLE) == SHOP_MODE_DISENABLE &&
                        BRIGHTNESS_MODE == 1) {
                    brightness -= mScreenBrightnessDim;
                    float valf = (((float)brightness * 2) / (MAXIMUM_BACKLIGHT - mScreenBrightnessDim)) - 1.0f; //JB native
                    power.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf); //JB native
                    Log.d(TAG, "setBrightness() : MultiALC=" + valf);
                }
                else {
                    power.setTemporaryScreenBrightnessSettingOverride(brightness);
                    Log.d(TAG, "setBrightness() : NoMultiALC=" + brightness);
                }
            }
        } catch (RemoteException doe) {
            Log.d(TAG, "RemoteException");
        }
    }

    private void setDefaultScreenOffTimeoutValues() throws NameNotFoundException {
        USER_MODE_SCREEN_OFF_TIMEOUT = getDefaultIntegerValue("com.android.providers.settings",
                "def_screen_off_timeout");
        Log.d(TAG, "Default Screen off timeout value : " + USER_MODE_SCREEN_OFF_TIMEOUT);
    }

    private void setDefaultBrightnessValues() throws NameNotFoundException {
        int sp_def_brightness = SystemProperties.getInt("ro.lge.lcd_default_brightness",
                SYSTEM_PROPERTY_EMPTY);
        //        if (SystemProperties.get("ro.build.target_region").equals("SCA")) {
        //            sp_def_brightness = SYSTEM_PROPERTY_EMPTY;
        //        }

        mScreenBrightnessDim = Config.getFWConfigInteger(context, com.android.internal.R.integer.config_screenBrightnessDim, 
                                    "com.android.internal.R.integer.config_screenBrightnessDim");

        Log.d(TAG, "SystemProperty Default brightness value [0-255] : " + sp_def_brightness);
        if (SYSTEM_PROPERTY_EMPTY != sp_def_brightness) {
            DEFAULT_BACKLIGHT = sp_def_brightness;
        } else {
            DEFAULT_BACKLIGHT = getDefaultIntegerValue("com.android.providers.settings",
                    "def_screen_brightness");
        }
        Log.d(TAG, "Default brightness[0-255] : " + DEFAULT_BACKLIGHT);
    }

    private void setDefaultBrightnessMode() throws NameNotFoundException {
        boolean sp_def_brightness_Mode = false;
        if (SystemProperties.get("ro.lge.lcd_auto_brightness_mode").equals("")) {
            isSystemProperty = false;
            Log.d(TAG, "Systemproperty is Empty!!!");
        }
        else {
            sp_def_brightness_Mode = SystemProperties.getBoolean("ro.lge.lcd_auto_brightness_mode",
                    false);
        }
        Log.d(TAG, "SystemProperty Default brightness Mode value[true/false] : "
                + sp_def_brightness_Mode);
        if (!Config.getFWConfigBool(context, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available")) {
            BRIGHTNESS_MODE = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            Log.d(TAG, "BRIGHTNESS_MODE - " + BRIGHTNESS_MODE);
        }
        else if (isSystemProperty == true) {
            String simOperator = SystemProperties.get("gsm.sim.operator.numeric");
            String mcc = "";
            String mnc = "";

            if (simOperator != null) {
                if (simOperator.length() >= 3) {
                    mcc = simOperator.substring(0, 3);
                    mnc = simOperator.substring(3);
                }
            }

            BRIGHTNESS_MODE = sp_def_brightness_Mode == true ? 1 : 0;

            if ((("208").equals(mcc) && ("10").equals(mnc))
                    || (("647").equals(mcc) && ("10").equals(mnc))) {

                BRIGHTNESS_MODE = 1;
            }
        }
        else {
            sp_def_brightness_Mode = getDefaultBooleanValue("com.android.providers.settings",
                    "def_screen_brightness_automatic_mode");
            BRIGHTNESS_MODE = sp_def_brightness_Mode == true ? 1 : 0;
        }
        Log.d(TAG, "Default brightness Mode [0/1] : " + BRIGHTNESS_MODE);
    }

    private void setDefaultNightMode() {
        NIGHT_MODE = 0;
        Log.d(TAG, "Default NIGHT_MODE : " + NIGHT_MODE);
    }

    private int getDefaultIntegerValue(String pkgName, String Name) {
        Context con = null;
        int id = 0;
        try {
            con = context.createPackageContext(pkgName, Context.MODE_PRIVATE);
            Resources res = null;
            if (null != con && null != con.getResources()) {
                res = con.getResources();
                id = res.getIdentifier(Name, "integer", pkgName);
                return Integer.parseInt(con.getText(id).toString());
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "NameNotFoundException : " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException : " + e.getMessage());
        }
        return 0;
    }

    private boolean getDefaultBooleanValue(String pkgName, String Name) {
        Context con = null;
        int id = 0;
        try {
            con = context.createPackageContext(pkgName, Context.MODE_PRIVATE);
            Resources res = null;
            if (null != con && null != con.getResources()) {
                res = con.getResources();
                id = res.getIdentifier(Name, "bool", pkgName);
                return Boolean.parseBoolean(con.getText(id).toString());
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "NameNotFoundException : " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException : " + e.getMessage());
        }
        return false;
    }

    private void setStoreModeBirghtness(Context context) {
        // all model max brightness
        if (SystemProperties.get("ro.build.product").equals("p2")) {
            MAXIMUM_BACKLIGHT = 179; //70%
        }

        Log.d(TAG, "Set MaxBrightness : " + MAXIMUM_BACKLIGHT);
    }

    private void setStoreMode(ContentResolver cr) {
        BRIGHTNESS_MODE = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        setMode(cr,
                SHOP_MODE_ENABLE,
                sSHOP_MODE_SCREEN_OFF_TIMEOUT,
                MAXIMUM_BACKLIGHT);
    }

    private void setUserMode(ContentResolver cr) {
        setMode(cr,
                SHOP_MODE_DISENABLE,
                USER_MODE_SCREEN_OFF_TIMEOUT,
                DEFAULT_BACKLIGHT);
        ReceiverDisable();
    }

    private void setMode(ContentResolver cr, int shopMode, int screenTimeout, int brightness) {
        Settings.System.putInt(cr, "shop_mode", shopMode);
        Settings.System.putInt(cr, "shop_mode_check", shopMode);
        setBrightness(brightness);
        sendBroadcast("android.setting.updateBrightness");
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, BRIGHTNESS_MODE);
        Settings.System.putInt(cr, SCREEN_OFF_TIMEOUT, screenTimeout);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
            brightness);
        if (!Config.getFWConfigBool(context, com.android.internal.R.bool.config_automatic_brightness_available, 
                                               "com.android.internal.R.bool.config_automatic_brightness_available")) {
            Settings.System.putInt(cr, "check_night_mode", NIGHT_MODE);
        }
        if (Build.DEVICE.equals("g3")) {    //Requested by CFW(Retail mode)
            Settings.System.putInt(cr, "Retail_Mode", shopMode);  
        }
        if (shopMode == SHOP_MODE_DISENABLE && BRIGHTNESS_MODE == 1) {
            Settings.System.putFloat(context.getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, getBrigtnessADJ(brightness));
        }
    }

    private void setUserDefaultPMICMode(ContentResolver cr) {
        setMode(cr,
                SHOP_MODE_ENABLE,
                USER_MODE_SCREEN_OFF_TIMEOUT,
                DEFAULT_BACKLIGHT);
    }

    private void sendBroadcast(String broadcast) {
        Intent intent = new Intent(broadcast);
        context.sendBroadcast(intent);
    }

    private boolean isFactoryTest() {
        /* LGE_CHANGE
         * When Factory bootimg mode, it doen't get into shop mode (maximum brightness).
         * 2012-02-28, baryun.hwang@lge.com
         */
        int factoryTestInt = SystemProperties.getInt("ro.factorytest", 0);
        String factoryqem = SystemProperties.get("sys.factory.qem");
        boolean factoryTest = false;

        if ("1".equals(factoryqem))
        {
            //Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, DEFAULT_BACKLIGHT);
            Log.d(TAG, "ro.factoryqem");
            return true;
        }

        if (factoryTestInt != 0) {
            factoryTest = true;
            Log.d(TAG, "ro.factorytest : " + factoryTestInt);
        }

        if (factoryTest == true) {
            setBrightness(DEFAULT_BACKLIGHT);
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                    DEFAULT_BACKLIGHT);
            sendBroadcast("android.setting.updateBrightness");
            Log.d(TAG, "ro.factorytest case");
            return true;
        }
        return false;
    }

    private boolean isStoreModeNotUse() {
        if (Config.getOperator().equals(Config.SPRINT)) {
            Log.d(TAG, "Skip the routine of store mode temporaly for verizon model");
            return true;
        }

        if (Utils.isWifiOnly(context)) {
            return true;
        }
        
        return false;
    }

    //[S][12.03.22][common][susin.park] Receiver on/off
    private void ReceiverDisable() {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, StoreModeReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Log.d(TAG, "StoreModeReceiver disenabled!");
    }

    //[E][12.03.22][common][susin.park] Receiver on/off

    //[S][12.04.02][VZW][susin.park] VZW only storemode set
    /* - VZW Default value-
     * Screen timeout value : 1 minute [not change]
     * Brightness : ?
     * Automatic : on
     */
    private void setVZWScreenTimeValue(Context context) {
        if (true == Config.getOperator().equals(Config.VZW)) {
            String launchKit_status = Settings.System.getString(context.getContentResolver(),
                    "launchkit_value");
            //launchKit = Boolean.parseBoolean(LaunchingKitInfo.getLaunchingKitFromNv());

            Log.d(TAG, "setVZWScreenTimeValue(): launchKit_status from db = " + launchKit_status);
            if (launchKit_status == null) {
                Log.d(TAG, "launchKit_status value is null");
                launchKit_status = "0";
            }
            launchKit = Boolean.parseBoolean(launchKit_status);
            Log.d(TAG, "setVZWScreenTimeValue(): launchKit after parseBoolean= " + launchKit);

            if (launchKit_status.equals("0")) {
                launchKit = false;
            } else if (launchKit_status.equals("1")) {
                launchKit = true;
            } else {
                Log.d(TAG, "launchKit_status is not 0 and 1, launchKit_status = "
                        + launchKit_status);
                launchKit = false;
            }

            Log.d(TAG, "launchKit value after equals is : " + launchKit);

            if (launchKit == false) {
                sSHOP_MODE_SCREEN_OFF_TIMEOUT = 60000;
            } else {
                sSHOP_MODE_SCREEN_OFF_TIMEOUT = 1000000000; //never value
                Log.d(TAG, "SHOP_MODE_SCREEN_OFF_TIMEOUT : " + sSHOP_MODE_SCREEN_OFF_TIMEOUT);
            }
        }
    }

    //[E][12.04.02][VZW][susin.park] VZW only storemode set

    private boolean getPhoneNumber() {
        String rawNumber = TelephonyManager.getDefault().getLine1Number(); // may be null or empty

        if (!TextUtils.isEmpty(rawNumber)) {

            if (rawNumber.startsWith("0000000000") || rawNumber.startsWith("0100000000"))
            {
                Log.d(TAG, "getPhoneNumber is 0000000000 or 01000000000: " + rawNumber);
                return false; //not naming
            } else {
                Log.d(TAG, "getPhoneNumber is not empty");
                return true;
            }
        }

        Log.d(TAG, "getPhoneNumber is empty");
        return false;

    }

    private float getBrigtnessADJ(int brightness) {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        int mScreenBrightnessMinimum = pm.getMinimumScreenBrightnessSetting();
        int mScreenBrightnessMaximum = pm.getMaximumScreenBrightnessSetting();
        int seekBarRange = mScreenBrightnessMaximum - mScreenBrightnessMinimum;
        brightness -= mScreenBrightnessMinimum;
        float valf = (((float)brightness * 2) / seekBarRange) - 1.0f;
        Log.d(TAG, "getBrigtnessADJ : " + valf);
        return valf;
    }
}
