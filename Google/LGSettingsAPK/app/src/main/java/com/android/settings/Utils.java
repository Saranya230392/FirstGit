/**
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.android.settings;

import static android.content.Intent.EXTRA_USER;

import android.annotation.Nullable;

//ask130402 :: +
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
//ask130402 :: -

import android.app.Activity;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.telephony.TelephonyManager.SIM_STATE_READY;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.IActivityManager;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.nfc.NfcAdapter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity.Header;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceGroup;
import android.provider.ContactsContract.CommonDataKinds;
//import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.service.persistentdata.PersistentDataBlockManager;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabWidget;

import com.android.internal.util.ImageUtils;
import com.android.internal.util.UserIcons;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.drawable.CircleFramedDrawable;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.hotkey.HotkeyInfo;

import java.net.URISyntaxException;
import com.android.settings.UserSpinnerAdapter.UserDetails;
import com.android.settings.users.ProfileUpdateReceiver;
import com.lge.systemservice.core.LGContext;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import android.os.Build;
import android.os.Handler;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lge.ShareConnection;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import com.android.settings.powersave.BatterySettings;
import com.android.settings.R;
import com.lge.nfcaddon.NfcAdapterAddon; // rebestm - tap&pay
//[S] geungju.yu@lge.com 2014.07.09
import com.lge.nfcconfig.NfcConfigure;
//[E] geungju.yu@lge.com 2014.07.09
//[S] insook.kim@lge.com 2012.05.18: Apply ICS number type. ex) 201-655-2326 --> (201)-655-2326
import android.location.CountryDetector;
//[E] insook.kim@lge.com 2012.05.18: Apply ICS number type. ex) 201-655-2326 --> (201)-655-2326
/* LGE_CHANGE_S : support miniOS for Factory (M4) */
import java.lang.Exception;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
/* LGE_CHANGE_E : support miniOS for Factory (M4) */

//[[LGE_CHANGE_S [L2][SPR] : byungsu.jeon@lge.com : 120604 : chameleon value for Sprint and BM
import java.util.StringTokenizer;
//LGE_CHANGE_E [L2][SPR] : byungsu.jeon@lge.com : 120604 : chameleon value for Sprint and BM ]]
import com.android.internal.telephony.PhoneConstants; // yonguk.kim JB+ Migration 20130125

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.Locale;
import com.lge.constants.LGIntent;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.lge.systemservice.core.LGContext;
//import com.lge.systemservice.core.MyFolderManager;
import com.lge.constants.SettingsConstants;
import android.content.pm.Signature;
import android.telephony.SubscriptionManager;
import com.android.settings.utils.LGSubscriptionManager;

//[jaewoong87.lee] Check hotspot use app or common tethering
import com.lge.wifi.config.LgeWifiConfig;

public class Utils {

    /**
     * Set the preference's title to the matching activity's label.
     */
    public static final int UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY = 1;

    /**
     * The opacity level of a disabled icon.
     */
    public static final float DISABLED_ALPHA = 0.4f;

    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    /*
     * Color spectrum to use to indicate badness.  0 is completely transparent (no data),
     * 1 is most bad (red), the last value is least bad (green).
     */
    public static final int[] BADNESS_COLORS = new int[] {
            0x00000000, 0xffc43828, 0xffe54918, 0xfff47b00,
            0xfffabf2c, 0xff679e37, 0xff0a7f42
    };

    /**
     * Name of the meta-data item that should be set in the AndroidManifest.xml
     * to specify the icon that should be displayed for the preference.
     */
    private static final String META_DATA_PREFERENCE_ICON = "com.android.settings.icon";

    /**
     * Name of the meta-data item that should be set in the AndroidManifest.xml
     * to specify the title that should be displayed for the preference.
     */
    private static final String META_DATA_PREFERENCE_TITLE = "com.android.settings.title";

    /**
     * Name of the meta-data item that should be set in the AndroidManifest.xml
     * to specify the summary text that should be displayed for the preference.
     */
    private static final String META_DATA_PREFERENCE_SUMMARY = "com.android.settings.summary";

    private static final String TAG = "Utils";

    //[S][2012.01.15][jm.lee2] ES4 merge
    /**
     * Resource index and multisim_resid willbe used to load corresponding xml
     * files in case of DSDS and NON-DSDS
     */
    public static final String RESOURCE_INDEX = "Resource_index";
    public static final int MULTISIM_DEF_RESID = 0;
    public static final int MULTISIM_RESID = 1;

    /*
    private static final String KEY_OLD_VERSION1 = "u0"; //[2012.04.10][jamy] old.version
    private static final String KEY_OLD_VERSION2 = "m4"; //[2012.04.10][jamy] old.version
    private static final String KEY_OLD_VERSION3 = "m4ds"; //[2012.04.10][jamy] old.version
    private static final String KEY_OLD_VERSION4 = "u0_cdma"; //[2012.04.10][jamy] old.version
    private static final String KEY_OLD_VERSION5 = "l1a"; //[2012.04.10][jamy] old.version
    private static final String KEY_OLD_VERSION6 = "vee5ds"; //[2012.08.27][never1029] V5 category removed
    */

    //[E][2012.01.15][jm.lee2] ES4 merge
    /* LGE_CHANGE_S : support miniOS for Factory (M4) */
    private static final String IS_MINIOS_PATH = "/sys/module/lge_emmc_direct_access/parameters/is_miniOS";
    private static final String SKIP_MINIOS_PATH = "/sdcard/skip_minios";
    /* LGE_CHANGE_E : support miniOS for Factory (M4) */

    // [S][2012.08.14][yongjaeo.lee] Hardware version add
    private static final String LOG_TAG = "Utils";
    // [E][2012.08.14][yongjaeo.lee] Hardware version add

    //private final static String CURRENT_MODE = "service.plushome.currenthome";
    //private final static String GUEST_MODE_STATE_STRING = "kids";
    //private final static String MYROOM_MODE_STATE_STRING = "myroom";
    //private final static String NORMAL_MODE_STATE_STRING = "standard";
    private static final String PACKAGE_NAME_PCSUITE = "com.lge.sync";
    private static final String PACKAGE_NAME_PCSUITEUI = "com.lge.pcsyncui";

    private static final String[] REGULATORY_DEVICE = {
            // G model
            "geeb",
            "geeb3g",
            "geefhd",
            "gvfhd",
            "geehrc",
            "geehrc4g"
    };

    private static final String[] G_MODEL_DEVICE = {
            // G model
            "geeb",
            "geeb3g",
            "geefhd",
            "geehdc",
            "geehrc",
            "geehrc4g",
            "geefhd4g",
            "gvfhd"
    };

    private static boolean mBatteryNotChargeForTemperature = false;

    public static final int sUSING_NOTHING = 0;
    public static final int sUSING_EASYSETTINGS = 1;
    public static final int sUSING_SETTINGS = 2;

    private static boolean sIsInUseVoLTE = false;

    public static final String AUTHORITY_LTE = "com.lge.ims.provider.uc";
    public static final String TABLE_NAME = "ucstate";
    public static final Uri CONTENT_URI = Uri
            .parse("content://" + AUTHORITY_LTE + "/" + TABLE_NAME);
    private static ContentObserver sContentObserver;
    private static Context sContext;

    private static String IPSEC_SERVICE_PACKAGE_NAME = "com.ipsec.service";

    private static final int LGE_GESTURE_SENSOR_TILT = 21;
    private static final int LGE_GESTURE_SENSOR_FACING = 23;

    private static LockPatternUtils mLockPatternUtils;

    /**
     * Finds a matching activity for a preference's intent. If a matching
     * activity is not found, it will remove the preference.
     *
     * @param context The context.
     * @param parentPreferenceGroup The preference group that contains the
     *            preference whose intent is being resolved.
     * @param preferenceKey The key of the preference whose intent is being
     *            resolved.
     * @param flags 0 or one or more of
     *            {@link #UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY}
     *            .
     * @return Whether an activity was found. If false, the preference was
     *         removed.
     */
    public static boolean updatePreferenceToSpecificActivityOrRemove(Context context,
            PreferenceGroup parentPreferenceGroup, String preferenceKey, int flags) {
        // WBT fixed
        if (parentPreferenceGroup == null) {
            return false;
        }
        Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }

        Intent intent = preference.getIntent();
        if (intent != null) {
            // Find the activity that is in the system image
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                != 0) {

                    // Replace the intent with this specific activity
                    preference.setIntent(new Intent().setClassName(
                            resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name));

                    if ((flags & UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY) != 0) {
                        // Set the preference title to the activity's label
                        preference.setTitle(resolveInfo.loadLabel(pm));
                    }

                    return true;
                }
            }
        }

        // Did not find a matching activity, so remove the preference
        parentPreferenceGroup.removePreference(preference);

        return false;
    }

    /**
     * Finds a matching activity for a preference's intent. If a matching
     * activity is not found, it will remove the preference. The icon, title and
     * summary of the preference will also be updated with the values retrieved
     * from the activity's meta-data elements. If no meta-data elements are
     * specified then the preference title will be set to match the label of the
     * activity, an icon and summary text will not be displayed.
     *
     * @param context The context.
     * @param parentPreferenceGroup The preference group that contains the
     *            preference whose intent is being resolved.
     * @param preferenceKey The key of the preference whose intent is being
     *            resolved.
     *
     * @return Whether an activity was found. If false, the preference was
     *         removed.
     *
     * @see {@link #META_DATA_PREFERENCE_ICON}
     *      {@link #META_DATA_PREFERENCE_TITLE}
     *      {@link #META_DATA_PREFERENCE_SUMMARY}
     */
    public static boolean updatePreferenceToSpecificActivityFromMetaDataOrRemove(Context context,
            PreferenceGroup parentPreferenceGroup, String preferenceKey) {

        IconPreferenceScreen preference = (IconPreferenceScreen)parentPreferenceGroup
                .findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }

        Intent intent = preference.getIntent();
        if (intent != null) {
            // Find the activity that is in the system image
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags
                & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    Drawable icon = null;
                    String title = null;
                    String summary = null;

                    // Get the activity's meta-data
                    try {
                        Resources res = pm
                                .getResourcesForApplication(resolveInfo.activityInfo.packageName);
                        Bundle metaData = resolveInfo.activityInfo.metaData;

                        if (res != null && metaData != null) {
                            icon = res.getDrawable(metaData.getInt(META_DATA_PREFERENCE_ICON));
                            title = res.getString(metaData.getInt(META_DATA_PREFERENCE_TITLE));
                            summary = res.getString(metaData.getInt(META_DATA_PREFERENCE_SUMMARY));
                        }
                    } catch (NameNotFoundException e) {
                        Log.w(TAG, "NameNotFoundException");
                    } catch (NotFoundException e) {
                        Log.w(TAG, "NameNotFoundException");
                    }

                    // Set the preference title to the activity's label if no
                    // meta-data is found
                    if (TextUtils.isEmpty(title)) {
                        title = resolveInfo.loadLabel(pm).toString();
                    }

                    // Set icon, title and summary for the preference
                    preference.setIcon(icon);
                    preference.setTitle(title);
                    preference.setSummary(summary);

                    // Replace the intent with this specific activity
                    preference.setIntent(new Intent().setClassName(
                            resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name));

                    return true;
                }
            }
        }

        // Did not find a matching activity, so remove the preference
        parentPreferenceGroup.removePreference(preference);

        return false;
    }

    public static boolean updateHeaderToSpecificActivityFromMetaDataOrRemove(Context context,
            List<Header> target, Header header) {

        Intent intent = header.intent;
        if (intent != null) {
            // Find the activity that is in the system image
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                != 0) {
                    //Drawable icon = null;
                    String title = null;
                    String summary = null;

                    // Get the activity's meta-data
                    try {
                        Resources res = pm.getResourcesForApplication(
                                resolveInfo.activityInfo.packageName);
                        Bundle metaData = resolveInfo.activityInfo.metaData;

                        if (res != null && metaData != null) {
                            //icon = res.getDrawable(metaData.getInt(META_DATA_PREFERENCE_ICON));
                            title = res.getString(metaData.getInt(META_DATA_PREFERENCE_TITLE));
                            summary = res.getString(metaData.getInt(META_DATA_PREFERENCE_SUMMARY));
                        }
                    } catch (NameNotFoundException e) {
                        Log.w(TAG, "NameNotFoundException");
                    } catch (NotFoundException e) {
                        Log.w(TAG, "NotFoundException");
                    }

                    // Set the preference title to the activity's label if no
                    // meta-data is found
                    if (TextUtils.isEmpty(title)) {
                        title = resolveInfo.loadLabel(pm).toString();
                    }

                    // Set icon, title and summary for the preference
                    // TODO:
                    //header.icon = icon;
                    header.title = title;
                    header.summary = summary;
                    // Replace the intent with this specific activity
                    header.intent = new Intent().setClassName(resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name);

                    return true;
                }
            }
        }

        // Did not find a matching activity, so remove the preference
        if (target.remove(header)) {
            System.err.println("Removed " + header.id);
        }
        return false;
    }

    /**
     * Returns true if Monkey is running.
     */
    public static boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    /**
     * Returns whether the device is voice-capable (meaning, it is also a phone).
     */
    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony =
                (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony != null && telephony.isVoiceCapable();
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
    }

    /**
     * Returns the WIFI IP Addresses, if any, taking into account IPv4 and IPv6 style addresses.
     * @param context the application context
     * @return the formatted and comma-separated IP addresses, or null if none.
     */

    public static String getWifiIpAddresses(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        LinkProperties prop = cm.getLinkProperties(ConnectivityManager.TYPE_WIFI);
        return formatIpAddresses(prop);
    }

    /**
     * Returns the default link's IP addresses, if any, taking into account IPv4 and IPv6 style
     * addresses.
     * @param context the application context
     * @return the formatted and comma-separated IP addresses, or null if none.
     */
    public static String getDefaultIpAddresses(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        LinkProperties prop = cm.getActiveLinkProperties();
        return formatIpAddresses(prop);
    }

    private static String formatIpAddresses(LinkProperties prop) {
        if (prop == null) {
            return null;
        }
        Iterator<InetAddress> iter = prop.getAllAddresses().iterator();
        // If there are no entries, return null
        if (!iter.hasNext()) {
            return null;
        }
        // Concatenate all available addresses, comma separated
        String addresses = "";
        while (iter.hasNext()) {
            addresses += iter.next().getHostAddress();
            if (iter.hasNext()) {
                addresses += "\n";
            }
        }
        return addresses;
    }

    public static Locale createLocaleFromString(String localeStr) {
        // TODO: is there a better way to actually construct a locale that will match?
        // The main problem is, on top of Java specs, locale.toString() and
        // new Locale(locale.toString()).toString() do not return equal() strings in
        // many cases, because the constructor takes the only string as the language
        // code. So : new Locale("en", "US").toString() => "en_US"
        // And : new Locale("en_US").toString() => "en_us"
        if (null == localeStr) {
            return Locale.getDefault();
        }
        String[] brokenDownLocale = localeStr.split("_", 3);
        // split may not return a 0-length array.
        if (1 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0]);
        } else if (2 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0], brokenDownLocale[1]);
        } else {
            return new Locale(brokenDownLocale[0], brokenDownLocale[1], brokenDownLocale[2]);
        }
    }

    /** Formats the ratio of amount/total as a percentage. */
    public static String formatPercentage(long amount, long total) {
        return formatPercentage(((double)amount) / total);
    }

    /** Formats an integer from 0..100 as a percentage. */
    public static String formatPercentage(int percentage) {
        return formatPercentage(((double)percentage) / 100.0);
    }

    /** Formats a double from 0.0..1.0 as a percentage. */
    private static String formatPercentage(double percentage) {
        BidiFormatter bf = BidiFormatter.getInstance();
        return bf.unicodeWrap(NumberFormat.getPercentInstance().format(percentage));
    }

    public static boolean isBatteryPresent(Intent batteryChangedIntent) {
        return batteryChangedIntent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
    }

    public static String getBatteryPercentage(Intent batteryChangedIntent) {

        int level;
        if (batteryChangedIntent.getAction().equals(
                Intent.ACTION_BATTERY_CHANGED)) {
            level = batteryChangedIntent.getIntExtra(
                    BatteryManager.EXTRA_LEVEL, 0);
        } else {
            level = batteryChangedIntent.getIntExtra(
                    LGIntent.EXTRA_BATTERY_LEVEL, 0);
        }

        int scale = batteryChangedIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        /*LGSI_CHANGE_S: Battery percentage in powersave option
         2011-03-26,anuj.singhaniya@lge.com,
         Numbers are changed according to the current language*/
        if (isRTLLanguage())
        {
            /*LGSI_CHANGE_S: Battery percentage in about phone option
             2013-01-11,ukrishnan.arangath@lge.com,
             For locales arabic and farsi percentage shown before the number*/
            //return String.format(Locale.getDefault(),"%d", level * 100 / scale) + "%";
            return String.format(Locale.getDefault(), "%d", level * 100 / scale) + "\u200F" + "%";
            /*LGSI_CHANGE_E: Battery percentage in about phone option*/
        }
        else {
            return String.valueOf(level * 100 / scale) + "%";
        }
        /*LGSI_CHANGE_E: Battery indication in powersave option*/
    }

    public static void setBatteryNotChargeForTemperature(boolean value) {
        mBatteryNotChargeForTemperature = value;
    }

    public static boolean getBatteryNotChargeForTemperature() {
        return mBatteryNotChargeForTemperature;
    }

    /*
     * EXTRA_CHARGING_CURRENT
     * 0 = CHARGING_CURRENT_NOT_CHARGING
     * 1 = CHARGING_CURRENT_NORMAL_CHARGING.
     * 2 = CHARGING_CURRENT_INCOMPATIBLE_CHARGING
     * 3 = CHARGING_CURRENT_UNDER_CURRENT_CHARGING
     * 4 = CHARGING_CURRENT_USB_DRIVER_UNINSTALLED
     * 5 = CHARGING_CURRENT_LLK_NOT_CHARGING
    */
    public static String getBatteryStatus(Resources res, Intent batteryChangedIntent) {
        final Intent intent = batteryChangedIntent;

        int plugType;
        int status;
        int level;
        int scale;

        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            level = intent.getIntExtra("level", 0);
            scale = intent.getIntExtra("scale", 100);
        } else {
            plugType = intent.getIntExtra(LGIntent.EXTRA_BATTERY_PLUGGED, 0);
            status = intent.getIntExtra(LGIntent.EXTRA_BATTERY_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);

            level = intent.getIntExtra(LGIntent.EXTRA_BATTERY_LEVEL, 0);
            scale = intent.getIntExtra("scale", 100);
        }
        boolean bSupportChargingCurrent = "VZW".equals(Config.getOperator());

        boolean isPluged = plugType > 0;

        String statusString = "";

        Log.d(TAG, "[getBatteryStatus] plugType = " + plugType + ", status = " + status);

        if (bSupportChargingCurrent) {
            int high_temperature_max = 550;
            int high_temperature_min = 430;
            int low_temperature_max = -100;
            int low_temperature_min = -80;

            if ("g3".equals(Build.DEVICE)) {
                high_temperature_max = 501;
                high_temperature_min = 375;

            }

            //            int mBatteryTemperature = intent.getIntExtra("temperature", 0);
            int mBatteryTemperature = BatterySettings.getTemperature();
            //            int mChargingCurrent = intent.getIntExtra("charging_current", 0);
            int mChargingCurrent = intent.getIntExtra(LGIntent.EXTRA_CHARGING_CURRENT, 0);
            Log.d(TAG, "[getBatteryStatus] mBatteryTemperature = " + mBatteryTemperature
                    + ", mChargingCurrent = " + mChargingCurrent);

            if (isPluged) {
                if (mBatteryTemperature >= high_temperature_max) {
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        statusString = res.getString(R.string.battery_info_status_charging_vzw);
                    } else {
                        statusString = res.getString
                                (R.string.battery_info_status_high_temp_not_charging_vzw);
                    }
                    setBatteryNotChargeForTemperature(true);
                } else if (mBatteryTemperature <= low_temperature_max) {
                    statusString = res
                            .getString(R.string.battery_info_status_low_temp_not_charging_vzw);
                    setBatteryNotChargeForTemperature(true);
                } else if ((mBatteryTemperature > high_temperature_min)
                        && getBatteryNotChargeForTemperature()) {
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        statusString = res.getString(R.string.battery_info_status_charging_vzw);
                    } else {
                        statusString = res.getString
                                (R.string.battery_info_status_high_temp_not_charging_vzw);
                    }
                } else if ((mBatteryTemperature < low_temperature_min)
                        && getBatteryNotChargeForTemperature()) {
                    statusString = res
                            .getString(R.string.battery_info_status_low_temp_not_charging_vzw);
                } else {
                    setBatteryNotChargeForTemperature(false);

                    if (mChargingCurrent == 2) { // CHARGING_CURRENT_INCOMPATIBLE_CHARGING
                        statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                    } else if (mChargingCurrent == 3) { // CHARGING_CURRENT_UNDER_CURRENT_CHARGING
                        if (level * 100 / scale >= 100) {
                            statusString = res
                                    .getString(R.string.battery_info_status_battery_full_vzw);
                        } else {
                            statusString = res
                                    .getString(R.string.battery_info_status_slow_charging_vzw);
                        }
                    } else if (mChargingCurrent == 4) { // CHARGING_CURRENT_USB_DRIVER_UNINSTALLED
                        statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                    } else if (mChargingCurrent == 5) { // CHARGING_CURRENT_LLK_NOT_CHARGING
                        statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                    } else {
                        if (status == BatteryManager.BATTERY_STATUS_FULL
                                || (level * 100 / scale >= 100)) {
                            statusString = res
                                    .getString(R.string.battery_info_status_battery_full_vzw);
                        } else {
                            statusString = res.getString(R.string.battery_info_status_charging_vzw);
                        }
                    }
                }
            } else {
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    statusString = res.getString(R.string.battery_info_status_charging_vzw);
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    statusString = res.getString(R.string.battery_info_status_discharging);
                } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    statusString = res.getString(R.string.battery_info_status_full_vzw);
                } else {
                    statusString = res.getString(R.string.battery_info_status_unknown);
                }
            }
        } else {
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                statusString = res.getString(R.string.battery_info_status_charging);
                if (plugType > 0) {
                    int resId;
                    if (plugType == BatteryManager.BATTERY_PLUGGED_AC) {
                        resId = R.string.battery_info_status_charging_ac;
                    } else if (plugType == BatteryManager.BATTERY_PLUGGED_USB) {
                        resId = R.string.battery_info_status_charging_usb;
                    } else {
                        resId = R.string.battery_info_status_charging_wireless;
                    }
                    statusString = statusString + " " + res.getString(resId);
                }
            } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                statusString = res.getString(R.string.battery_info_status_discharging);
            } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                statusString = res.getString(R.string.battery_info_status_not_charging);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                statusString = res.getString(R.string.battery_info_status_full);
            } else {
                statusString = res.getString(R.string.battery_info_status_unknown);
            }
        }

        Log.d(TAG, "[getBatteryStatus] statusString = " + statusString);

        return statusString;
    }

    // [jongwon007.kim][SmartCharging]
    /*
     * EXTRA_CHARGING_CURRENT
     * 0 = CHARGING_CURRENT_NOT_CHARGING
     * 1 = CHARGING_CURRENT_NORMAL_CHARGING.
     * 2 = CHARGING_CURRENT_INCOMPATIBLE_CHARGING
     * 3 = CHARGING_CURRENT_UNDER_CURRENT_CHARGING
     * 4 = CHARGING_CURRENT_USB_DRIVER_UNINSTALLED
     * 5 = CHARGING_CURRENT_LLK_NOT_CHARGING
    */
    public static String getSmartBatteryStatus(Resources res, Intent batteryChangedIntent,
            Context context) {
        final Intent intent = batteryChangedIntent;

        boolean bSupportChargingCurrent = "VZW".equals(Config.getOperator());
        int plugType = intent.getIntExtra(LGIntent.EXTRA_BATTERY_PLUGGED, 0);
        int status = intent.getIntExtra(LGIntent.EXTRA_BATTERY_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN);
        int level = intent.getIntExtra(LGIntent.EXTRA_BATTERY_LEVEL, 0);
        int scale = intent.getIntExtra("scale", 100);
        boolean isPluged = plugType > 0;

        String statusString = "";

        Log.d(TAG, "[getBatteryStatus] plugType = " + plugType + ", status = " + status);

        if (bSupportChargingCurrent) {
            int high_temperature_max = 550;
            int high_temperature_min = 520;
            int low_temperature_max = -100;
            int low_temperature_min = -80;

            if ("g3".equals(Build.DEVICE)) {
                high_temperature_max = 501;
                high_temperature_min = 375;

            }

            int mBatteryTemperature = BatterySettings.getTemperature();
            int mChargingCurrent = intent.getIntExtra(LGIntent.EXTRA_CHARGING_CURRENT, 0);
            Log.d(TAG, "[getBatteryStatus] mBatteryTemperature = " + mBatteryTemperature
                    + ", mChargingCurrent = " + mChargingCurrent);

            if (isPluged) {
                if (mBatteryTemperature >= high_temperature_max) {
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        statusString = res.getString(R.string.battery_info_status_charging_vzw);
                    } else {
                        statusString = res.getString
                                (R.string.battery_info_status_high_temp_not_charging_vzw);
                    }
                    setBatteryNotChargeForTemperature(true);
                } else if (mBatteryTemperature <= low_temperature_max) {
                    statusString = res
                            .getString(R.string.battery_info_status_low_temp_not_charging_vzw);
                    setBatteryNotChargeForTemperature(true);
                } else if ((mBatteryTemperature > high_temperature_min)
                        && getBatteryNotChargeForTemperature()) {
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        statusString = res.getString(R.string.battery_info_status_charging_vzw);
                    } else {
                        statusString = res.getString
                                (R.string.battery_info_status_high_temp_not_charging_vzw);
                    }
                } else if ((mBatteryTemperature < low_temperature_min)
                        && getBatteryNotChargeForTemperature()) {
                    statusString = res
                            .getString(R.string.battery_info_status_low_temp_not_charging_vzw);
                } else {
                    setBatteryNotChargeForTemperature(false);
                    if (mChargingCurrent == 2) { // CHARGING_CURRENT_INCOMPATIBLE_CHARGING
                        statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                    } else if (mChargingCurrent == 3) { // CHARGING_CURRENT_UNDER_CURRENT_CHARGING
                        if (level * 100 / scale >= 100) {
                            statusString = res
                                    .getString(R.string.battery_info_status_battery_full_vzw);
                        } else {
                            statusString = res
                                    .getString(R.string.battery_info_status_slow_charging_vzw);
                        }
                    } else if (mChargingCurrent == 4) { // CHARGING_CURRENT_USB_DRIVER_UNINSTALLED
                        statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                    } else if (mChargingCurrent == 5) { // CHARGING_CURRENT_LLK_NOT_CHARGING
                        statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                    } else {
                        if (status == BatteryManager.BATTERY_STATUS_FULL
                                || (level * 100 / scale >= 100)) {
                            statusString = res
                                    .getString(R.string.battery_info_status_battery_full_vzw);
                        } else {
                            if (android.provider.Settings.System.getInt(
                                    context.getContentResolver(),
                                    "power_save_smart_charge", 0) > 0
                                    && plugType == 1) {
                                Log.d("jw ", "Smart Battery ");
                                statusString = res
                                        .getString(R.string.sp_power_save_battery_smart_charge);
                                return statusString;
                            }
                            statusString = res.getString(R.string.battery_info_status_charging_vzw);
                        }
                    }
                }
            } else {
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    statusString = res.getString(R.string.battery_info_status_charging_vzw);
                } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    statusString = res.getString(R.string.battery_info_status_discharging);
                } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    statusString = res.getString(R.string.battery_info_status_not_charging_vzw);
                } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    statusString = res.getString(R.string.battery_info_status_full_vzw);
                } else {
                    statusString = res.getString(R.string.battery_info_status_unknown);
                }
            }
        } else {
            if (android.provider.Settings.System.getInt(context.getContentResolver(),
                    "power_save_smart_charge", 0) > 0 && plugType == 1) {
                Log.d("jw ", "Smart Battery ");
                statusString = res.getString(R.string.sp_power_save_battery_smart_charge);
                //                statusString = statusString + " ";
                return statusString;
            }

            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                statusString = res.getString(R.string.battery_info_status_charging);
                if (plugType > 0) {
                    int resId;
                    if (plugType == BatteryManager.BATTERY_PLUGGED_AC) {
                        resId = R.string.battery_info_status_charging_ac;
                    } else if (plugType == BatteryManager.BATTERY_PLUGGED_USB) {
                        resId = R.string.battery_info_status_charging_usb;
                    } else {
                        resId = R.string.battery_info_status_charging_wireless;
                    }
                    statusString = statusString + " " + res.getString(resId);
                }
            } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                statusString = res.getString(R.string.battery_info_status_discharging);
            } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                statusString = res.getString(R.string.battery_info_status_not_charging);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                statusString = res.getString(R.string.battery_info_status_full);
            } else {
                statusString = res.getString(R.string.battery_info_status_unknown);
            }
        }

        Log.d(TAG, "[getBatteryStatus] statusString = " + statusString);

        return statusString;
    }

    // rebestm - kk migration
    public static void forcePrepareCustomPreferencesList(
            ViewGroup parent, View child, ListView list, boolean ignoreSidePadding) {
        list.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        list.setClipToPadding(false);
        prepareCustomPreferencesList(parent, child, list, ignoreSidePadding);
    }

    /**
     * Prepare a custom preferences layout, moving padding to {@link ListView}
     * when outside scrollbars are requested. Usually used to display
     * {@link ListView} and {@link TabWidget} with correct padding.
     */
    // rebestm - kk migration
    public static void prepareCustomPreferencesList(
            ViewGroup parent, View child, View list, boolean ignoreSidePadding) {
        final boolean movePadding = list.getScrollBarStyle() == View.SCROLLBARS_OUTSIDE_OVERLAY;
        if (movePadding && parent instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams)child.getLayoutParams()).removeBorders = true;
            child.setTextDirection(View.TEXT_DIRECTION_LOCALE);
            final Resources res = list.getResources();
            final int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);
            final int paddingBottom = res.getDimensionPixelSize(
                    R.dimen.preference_fragment_padding_bottom);

            final int effectivePaddingSide = ignoreSidePadding ? 0 : paddingSide;
            list.setPaddingRelative(effectivePaddingSide, 0, effectivePaddingSide, paddingBottom);
        }
    }

    public static void forceCustomPadding(View view, boolean additive) {
        final Resources res = view.getResources();
        final int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);

        final int paddingStart = paddingSide + (additive ? view.getPaddingStart() : 0);
        final int paddingEnd = paddingSide + (additive ? view.getPaddingEnd() : 0);
        final int paddingBottom = res.getDimensionPixelSize(R.dimen.settings_side_margin);

        view.setPaddingRelative(paddingStart, 0, paddingEnd, paddingBottom);
    }

    /**
     * Return string resource that best describes combination of tethering
     * options available on this device.
     */
    public static int getTetheringLabel(ConnectivityManager cm) {
        String[] usbRegexs = cm.getTetherableUsbRegexs();
        String[] wifiRegexs = cm.getTetherableWifiRegexs();
        String[] bluetoothRegexs = cm.getTetherableBluetoothRegexs();

        boolean usbAvailable = usbRegexs.length != 0;
        boolean wifiAvailable = wifiRegexs.length != 0;
        boolean bluetoothAvailable = bluetoothRegexs.length != 0;

        int tetheringLabel = R.string.tether_settings_title_all;
        if ("VZW".equals(Config.getOperator())) {
            tetheringLabel = R.string.tethering_and_mobile_hotspot_vzw;
        }

        if (wifiAvailable && usbAvailable && bluetoothAvailable) {
            return tetheringLabel;
        } else if (wifiAvailable && usbAvailable) {
            return tetheringLabel;
        } else if (wifiAvailable && bluetoothAvailable) {
            return tetheringLabel;
        } else if (wifiAvailable) {
            return R.string.tether_settings_title_wifi;
        } else if (usbAvailable && bluetoothAvailable) {
            return R.string.tether_settings_title_usb_bluetooth_jb_plus;
        } else if (usbAvailable) {
            return R.string.tether_settings_title_usb;
        } else {
            return R.string.tether_settings_title_bluetooth;
        }
    }

    // [S][2012.09.05][yongjaeo.lee] Hardware version add
    public static String checkHW_rev(String ver, String sDefault) {
        String newConvertHWversion = "";
        Log.i(LOG_TAG, "checkHW_rev ()");

        if (ver == null) {
            newConvertHWversion = sDefault;
        } else if (ver.substring(0, 0 + "rev_".length()).matches("rev_")) { // e.g. rev_c => Rev.C
            Log.d(LOG_TAG, "checkHW_rev (rev_):" + ver);

            String upper_stringHwVersion = ver.toUpperCase();
            StringBuilder s_ConvetHWversion = new StringBuilder();
            String prefix = upper_stringHwVersion.substring(4);

            s_ConvetHWversion.append("Rev.");

            if (prefix.length() == 2) {
                char prefix1 = prefix.charAt(0);
                char prefix2 = prefix.charAt(1);

                // 00 ~ 99
                if ((prefix1 >= '0' && prefix1 <= '9') && (prefix2 >= '0' && prefix2 <= '9')) {
                    s_ConvetHWversion.append(prefix1);
                    s_ConvetHWversion.append(".");
                    s_ConvetHWversion.append(prefix2);
                } else {
                    s_ConvetHWversion.append(prefix);
                }
            } else {
                s_ConvetHWversion.append(prefix);
            }

            newConvertHWversion = s_ConvetHWversion.toString();
        } else {
            Log.i(LOG_TAG, "Not rev_");
            newConvertHWversion = ver;
        }

        Log.d(LOG_TAG, "newConvertHWversion:" + newConvertHWversion);
        return newConvertHWversion;
    }

    public static String readHwVersion_rev(String sDefault) {
        String stringHwVersion = SystemProperties.get("ro.lge.hw.revision");
        Log.d(LOG_TAG, "readHwVersion_rev() , ro.lge.hw.revision:" + stringHwVersion);

        return checkHW_rev(stringHwVersion, sDefault);
    }

    public static String readHwVersion_pcb(String sDefault) {
        String stringHwVersion = SystemProperties.get("ro.pcb_ver");
        Log.d(LOG_TAG, "readHwVersion_pcb() , ro.pcb_ver = " + stringHwVersion);

        return checkHW_rev(stringHwVersion, sDefault);
    }

    // [E][2012.09.05][yongjaeo.lee] Hardware version add

    //[S][2013.04.10][never1029] Update Center list apply
    public static boolean IsVersionCheck(Context context, String packageName) {
        boolean bResult = false;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(packageName, 0);
            Log.d(TAG, "IsVersionCheck ex : " + pkgInfo.versionCode);
            if (packageName.equals("com.lge.updatecenter") && pkgInfo.versionCode >= 400000) {
                bResult = true;
            }

            if (packageName.equals("com.lge.camera") && pkgInfo.versionCode >= 44000000) {
                bResult = true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "pkg info null");
            bResult = false;
        }

        Log.d(TAG, "IsVersionCheck : " + bResult);
        return bResult;
    }

    //[E][2013.04.10][never1029] Update Center list apply

    public static boolean checkPackage(Context context, String packageName) {
        PackageInfo pi = null;
        PackageManager pm = context.getPackageManager();
        try {
            pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (pi.packageName.equals(packageName)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "checkPackage() : package is not found(" + packageName + ")");
            return false;
        }

        return false;
    }

    public static boolean isTripleSimEnabled() {
        if ("true".equals(SystemProperties.get("ro.lge.mtk_triplesim"))) {
            return true;
        }
        else {
            String isTripleSim = SystemProperties.get("persist.radio.multisim.config");
            return "tsts".equals(isTripleSim);
            //return false;
        }
    }

    // [S][2012-01-19][jm.lee2] Qualcomm multi SIM settings
    public static boolean isMultiSimEnabled() {
        boolean mtk_dual = SystemProperties.getBoolean("ro.lge.mtk_dualsim", false);
        if (mtk_dual) {
            return true;
        } else {
            String isMutiSim = SystemProperties.get("persist.radio.multisim.config");
            return "dsds".equals(isMutiSim) || "dsda".equals(isMutiSim);
        }
    }

    // [E][2012-01-19][jm.lee2] Qualcomm multi SIM settings

    public static boolean isTopActivity(String className) {

        try {
            if (ActivityManagerNative.getDefault() != null) {
                List<ActivityManager.RunningTaskInfo> list = ActivityManagerNative.getDefault()
                        .getTasks(1, 0);
                if (list == null) {
                    return false;
                }
                ActivityManager.RunningTaskInfo info = list.get(0);
                if (null == info || null == info.topActivity) {
                    return false;
                }
                Log.d("soosin", info.topActivity.getClassName());
                if (className.equals(info.topActivity.getClassName())) {
                    return true;
                }
            }
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
        return false;
    }

    // [S][2012.03.08][seungeene][LGP700][NA] Add SETUPWIZARD_DONE action
    public static boolean isVodafoneUKSIM(Context context) {
        Log.i(TAG, "isVodafoneUKSIM");
        //0
        String targetCountry = Config.getCountry();
        String targetOperator = Config.getOperator();

        Log.i(TAG, "targetCountry: " + targetCountry + "/ targetOperator: " + targetOperator);
        if (!("UK".equals(targetCountry) && "VDF".equals(targetOperator))) {
            return false;
        }
        String mccmnc = ((TelephonyManager)context
                .getSystemService(Context.TELEPHONY_SERVICE)).getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            return false;
        }
        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        Log.i(TAG, "MCC: " + mcc + "/ MNC: " + mnc);
        if (!("234".equals(mcc) && "15".equals(mnc))) {
            return false;
        }
        return true;
    }
    // [E][2012.03.08][seungeene][LGP700][NA] Add SETUPWIZARD_DONE action

    public static boolean isCostaRicaMCC(Context mContext) {
        String mccmnc = ((TelephonyManager)mContext
                .getSystemService(Context.TELEPHONY_SERVICE)).getSimOperator();

        if (mccmnc == null || mccmnc.length() < 5) {
            return false;
        }

        String mcc = mccmnc.substring(0, 3);

        if ("712".equals(mcc)) {
            return true;
        }

        return false;
    }

    public static boolean isTestSim(Context context) {
        SLog.i(TAG, "isTestSim");
        //kddi requirement
        String mccmnc = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            SLog.i(TAG, "isTestSim false");
            return false;
        }

        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        SLog.i(TAG, "MCC: " + mcc + "/ MNC: " + mnc);
        if (!("001".equals(mcc) && ("01".equals(mnc)))) {
            return false;
        }

        SLog.i(TAG, "isTestSim true");
        return true;
    }

    public static boolean isHuchisonSim(Context context) {
        SLog.i(TAG, "isHuchisonSim");
        String mccmnc = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            SLog.i(TAG, "isHuchisonSim false");
            return false;
        }
        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        SLog.i(TAG, "MCC: " + mcc + "/ MNC: " + mnc);
        if (!("454".equals(mcc) && ("03".equals(mnc)))) {
            return false;
        }
        SLog.i(TAG, "isHuchisonSim true");
        return true;
    }

    public static boolean isTMobileplSim(Context context) {
        // OPERATOR.EURnD - Defect #16416 : 260/02+"T-Mobilr.pl", 260/02+"T-Mobilr.pl Q"
        String mSpn = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA);
        String mccmnc = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            return false;
        }
        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        SLog.i(TAG, "MCC: " + mcc + "/ MNC: " + mnc);
        if (!("260".equals(mcc) && "02".equals(mnc) &&
                ("T-Mobile.pl".equals(mSpn) || "T-Mobile.pl Q".equals(mSpn)))) {
            return false;
        }
        SLog.i(TAG, "isTMobileplSim true");
        return true;
    }
    //[S][2012.07.20][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )
    public static boolean isSupport_Regulatory_model(String sDevice) {
        for (String key : REGULATORY_DEVICE) {
            if (key.equals(sDevice)) {
                return true;
            }
        }
        return false;
    }

    //[E][2012.07.20][yongjaeo.lee@lge.com] embedded battery model ( regulatory & factory date & SN )

    // [S][2012.08.14][yongjaeo.lee] Hardware version add
    public static boolean is_G_model_Device(String sDevice) {
        for (String key : G_MODEL_DEVICE) {
            if (key.equals(sDevice)) {
                return true;
            }
        }
        return false;
    }

    // [E][2012.08.14][yongjaeo.lee] Hardware version add

    public static boolean isbuildProduct(String _product) {
        String product = SystemProperties.get("ro.build.product", "notFound");
        Log.d(TAG, "Product Board : " + product);
        if (product.equals(_product)) {
            return true;
        }
        return false;
    }

    public static boolean istargetOperator(String _operator) {
        String operator = SystemProperties.get("ro.build.target_operator", "notFound");
        Log.d(TAG, "Product Board : " + operator);
        if (operator.equals(_operator)) {
            return true;
        }
        return false;
    }

    public static boolean hasFeatureNfcP2P() {
        if ("SKT".equals(Config.getOperator()) || "LGU".equals(Config.getOperator())
                || "KT".equals(Config.getOperator())
                || "CMCC".equals(Config.getOperator())
                || "CMO".equals(Config.getOperator())) {
            return true;
        }
        return false;
    }

    //jw_skt_modify_menu
    public static boolean hasFeatureNfcInner() {
        if ("SKT".equals(Config.getOperator()) || "LGU".equals(Config.getOperator())
                || "CN".equals(Config.getCountry()) || "KT".equals(Config.getOperator())) {
                if ("CTC".equals(Config.getOperator()) ||
                        "CTO".equals(Config.getOperator())) {
                    return false;
                }
                else {
                    return true;
                }
            //return false;
        }
        return false;
    }

    public static boolean hasFeatureNfcLock() {
        if (true == hasI30NfcSetting() && "KDDI".equals(Config.getOperator())
                || "DCM".equals(Config.getOperator())) {
            return true;
        }
        return false;
    }

    public static boolean hasFeatureNfcLockForKDDI() {
        if (true == hasI30NfcSetting() && "KDDI".equals(Config.getOperator())) {
            return true;
        }
        return false;
    }

    public static boolean hasI30NfcSetting() {
        if (NfcConfigure.IsNfcConfigureValue(NfcConfigure.NFC_SECUREELEMENT_TYPE,
            NfcConfigure.SecureElementList.INITVALUE)) {
            Log.d(TAG, "NFC SecureElement = INITVALUE");
            return false;
        }
        return true;
    } //geungju.yu method name change

    public static boolean hasSprintTouchV2(Context ctx) {
        //return false;

        Log.d(TAG, "HasSprintTouchV2()");

        // jalran 130425 touch's SystemProperty value add, defalt value 0
        String touch = android.os.SystemProperties.get("ro.chameleon.touch", "0");

        if (touch == null || touch.length() == 0) {
            Log.d(TAG, "SystemProperties.get has no [returned data] = " + touch);
            return false;
        }

        Log.d(TAG, "SystemProperties.get = " + touch);

        if (touch.equals("1")) {
            List<PackageInfo> packages = ctx.getPackageManager().getInstalledPackages(0);
            for (PackageInfo info : packages) {
                if (info.packageName.equals("com.sequent.controlpanel")) {
                    if (info.versionCode == 1) {
                        Log.d(TAG, "Hide versionCode = " + info.versionCode + "versionName = "
                                + info.versionName);
                        return false;
                    } else {
                        Log.d(TAG, "Show versionCode =" + info.versionCode + "versionName = "
                                + info.versionName);
                        return true;
                    }
                }
            }
        }
        return false;

    }

    /**
     * Determine whether a package is a "system package", in which case certain things (like
     * disabling notifications or disabling the package altogether) should be disallowed.
     */
    public static boolean isSystemPackage(PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{ getSystemSignature(pm) };
        }
        return sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(pkg));
    }
    private static Signature[] sSystemSignature;

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg != null && pkg.signatures != null && pkg.signatures.length > 0) {
            return pkg.signatures[0];
        }
        return null;
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            final PackageInfo sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return getFirstSignature(sys);
        } catch (NameNotFoundException e) {
        }
        return null;
    }

    public static boolean hasNfcDisplaySettings(Context ctx) {
        return Config.getOperator().equals(Config.VZW);
    }

    public static boolean isSelectOldVersion() {
        return false;
    }

    public static void onHiddenAppsClear() {
        hidden.clear();
    }

    public static final int UID_MY_FOLDER = -123456;

    // yonguk.kim 20120504 Use Resource Array [START]
    static HashSet<String> hidden = new HashSet<String>();

    public static HashSet<String> getHiddenApps(Context context) {
        if (hidden.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(R.array.hidden_apps);
            for (String package_name : hidden_apps) {
                hidden.add(package_name);
            }
        }
        return hidden;
    }

    static HashSet<String> disable_blocked = new HashSet<String>();

    public static HashSet<String> getDisableBlockedApps(Context context) {
        if (disable_blocked.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(
                    R.array.disable_blocked_apps);
            if ("SPR".equals(Config.getOperator())) {
                HashSet<String> enable_spr_pkgList = getDisableDisplayPKGList();
                if (enable_spr_pkgList != null) {
                    for (String package_name : hidden_apps) {
                        if (false == enable_spr_pkgList.contains(package_name)) {
                            disable_blocked.add(package_name);
                        }
                    }
                } else {
                    for (String package_name : hidden_apps) {
                        disable_blocked.add(package_name);
                    }
                }
            } else {
                for (String package_name : hidden_apps) {
                    disable_blocked.add(package_name);
                }
            }
        }
        return disable_blocked;
    }

    static HashSet<String> notification_blocked = new HashSet<String>();

    public static HashSet<String> getNotificationBlockedApps(Context context) {
        if (notification_blocked.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(
                    R.array.notification_blocked_apps);
            for (String package_name : hidden_apps) {
                notification_blocked.add(package_name);
            }
        }
        return notification_blocked;
    }

    //jaeyoon.hyun[Start] VPL FOR ATT
    static HashSet<String> vapps_att = new HashSet<String>();

    public static HashSet<String> getVApps(Context context) {
        if (vapps_att.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(R.array.vapps_att);
            for (String package_name : hidden_apps) {
                vapps_att.add(package_name);
            }
        }
        return vapps_att;
    }

    // [START][2014-11-20][seungyeop.yeom] Method for Block of Force stop button
    static HashSet<String> sForceStopBlockedList = new HashSet<String>();

    public static HashSet<String> getForceStopBlockedApps(Context context) {
        if (sForceStopBlockedList.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(
                    R.array.force_stop_blocked_apps);
            for (String package_name : hidden_apps) {
                sForceStopBlockedList.add(package_name);
            }
        }
        return sForceStopBlockedList;
    }

    // [END][2014-11-20][seungyeop.yeom] Method for Block of Force stop button

    // [START][2014-11-20][seungyeop.yeom] Method for Block of Uninstall button
    static HashSet<String> sUninstallBlockedList = new HashSet<String>();

    public static HashSet<String> getUninstallBlockedApps(Context context) {
        if (sUninstallBlockedList.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(
                    R.array.uninstall_blocked_apps);
            for (String package_name : hidden_apps) {
                sUninstallBlockedList.add(package_name);
            }
        }
        return sUninstallBlockedList;
    }

    // [END][2014-11-20][seungyeop.yeom] Method for Block of Uninstall button

    // [START][2014-11-20][seungyeop.yeom] Method for Block of Clear data button
    static HashSet<String> sClearDataBlockedList = new HashSet<String>();

    public static HashSet<String> getClearDataBlockedApps(Context context) {
        if (sClearDataBlockedList.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(
                    R.array.clear_data_blocked_apps);
            for (String package_name : hidden_apps) {
                sClearDataBlockedList.add(package_name);
            }
        }
        return sClearDataBlockedList;
    }

    static HashSet<String> soundAppsNoneList = new HashSet<String>();

    public static HashSet<String> getsoundAppsNoneList(Context context) {
        if (soundAppsNoneList.size() == 0) {
            String[] hidden_apps = context.getResources().getStringArray(
                    R.array.sound_notificaiton_block_apps);
            for (String package_name : hidden_apps) {
                soundAppsNoneList.add(package_name);
            }
        }
        return soundAppsNoneList;
    }

    // [END][2014-11-20][seungyeop.yeom] Method for Block of Force stop button

    //jaeyoon.hyun[END] VPL FOR ATT
    // yonguk.kim 20120504 Use Resource Array [END]
    // yonguk.kim 20120516 Support Function menu icon [START]
    public static boolean supportFunctionIcon() {
        return true;
    }

    // yonguk.kim 20120516 Support Function menu icon [END]

    //[S] insook.kim@lge.com 2012.05.18: Apply ICS number type. ex) 201-655-2326 --> (201)-655-2326
    public static final String getCurrentCountryIso(Context context) {
        CountryDetector detector =
                (CountryDetector)context.getSystemService(Context.COUNTRY_DETECTOR);
        return detector.detectCountry().getCountryIso();
    }

    //[E] insook.kim@lge.com 2012.05.18: Apply ICS number type. ex) 201-655-2326 --> (201)-655-2326

    // yonguk.kim 20120524 Check if supporting EasySettings
    public static boolean supportEasySettings(Context context) {
        return checkPackage(context, "com.lge.settings.easy");
    }

    public static boolean supportTangibleSettings(Context context) {
        return checkPackage(context, "com.lge.tangible");
    }

    public static boolean supportCoverSettings(Context context) {
        if (Config.getFWConfigBool(context, com.lge.R.bool.config_smart_cover,
                "com.lge.R.bool.config_smart_cover") == false
                && Config.getFWConfigBool(context,
                        com.lge.R.bool.config_using_window_cover,
                        "com.lge.R.bool.config_using_window_cover") == false
                && Config.getFWConfigBool(context,
                        com.lge.R.bool.config_using_lollipop_cover,
                        "com.lge.R.bool.config_using_lollipop_cover") == false
                && Config.getFWConfigBool(context,
                        com.lge.R.bool.config_using_circle_cover,
                        "com.lge.R.bool.config_using_circle_cover") == false
                && Config.getFWConfigBool(context,
                        com.lge.R.bool.config_using_disney_cover,
                        "com.lge.R.bool.config_using_disney_cover") == false) {
            return false;
        } else {
            if (Utils.is_G_model_Device(SystemProperties
                    .get("ro.product.device"))) {
                return false;
            }

            return true;
        }
    }

    /* LGE_CHANGE_S : support miniOS for Factory (M4)
     * 2012-02-15, myunghwan.kim@lge.com,
     * Don`t install all apks when factory mode enabled.
     * Repository : android/device/lge/m4
     */
    public static boolean is_miniOS() {
        boolean is_miniOS = false;
        BufferedReader in = null;

        // Check miniOS mode (Manual Mode NV)
        try {
            in = new BufferedReader(new FileReader(IS_MINIOS_PATH));
            String _is_minios = in.readLine();
            in.close();

            if (_is_minios != null && _is_minios.equals("yes")) {
                is_miniOS = true;
            }
        } catch (IOException e) {
            Log.d(TAG, "unable to read is_miniOS : " + e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                Log.w(TAG, "Exception");
            }
        }

        // check skip file
        try {
            File file = new File(SKIP_MINIOS_PATH);
            if (file.exists()) {
                is_miniOS = false;
            }
        } catch (Exception er) {
            Log.w(TAG, "Exception");
        }

        return is_miniOS;
    }

    //[[LGE_CHANGE_S [L2][SPR] : byungsu.jeon@lge.com : 120604 : chameleon value for Sprint and BM

    private static int readChameleonIntValue(String fn, int ref_value) {

        FileReader fr = null;
        String value = null;
        int ret_value = ref_value;

        File fh = new File(fn);
        if (!fh.exists()) {
            return ret_value;
        }

        try {
            fr = new FileReader(fn);
            BufferedReader inFile = new BufferedReader(fr);
            String line = inFile.readLine();
            if (line != null && line.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(line);
                value = tokenizer.nextToken();
                //Log.d("[node wifi] read "+ fn + " : " + value);
            }
        } catch (IOException e) {
            Log.w(TAG, "IOException");
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
        }

        try {

            if (value != null) {
                ret_value = Integer.parseInt(value);
            } else {
                ret_value = ref_value;
            }

        } catch (Exception e) {
            ret_value = ref_value;
        }

        return ret_value;
    }

    /*
        From chameleon spec.
        TetherData node value 1 :  wifi hotspot, bt pan, usb tethering, wifidirect, bt dun are disabled
        TetherData node value 2 :  wifi hotspot , bt pan are enabled,  usb tethering, wifidirect, bt dun are disabled
        TetherData node value 3 :  wifi hotspot , bt pan, usb tethering, wifidirect, bt dun are enabled
     **/
    public static boolean getChameleonWifiDirectMenuEnabled() {
        boolean result = true;

        if ("SPR".equals(Config.getOperator()) || "BM".equals(Config.getOperator())) {
            String cmln_data_tether = new String("/carrier/data/td");
            int refValue = "SPR".equals(Config.getOperator()) ? 3 : 1;

            refValue = readChameleonIntValue(cmln_data_tether, refValue);

            result = refValue > 2 ? true : false;
        }

        return result;
    }

    public static boolean getChameleonUsbTetheringMenuEnabled() {
        boolean result = true;

        if ("SPR".equals(Config.getOperator()) || "BM".equals(Config.getOperator())) {
            String cmln_data_tether = new String("/carrier/data/td");
            int refValue = "SPR".equals(Config.getOperator()) ? 3 : 1;

            refValue = readChameleonIntValue(cmln_data_tether, refValue);

            result = refValue > 2 ? true : false;
        }

        return result;
    }

    //LGE_CHANGE_E [L2][SPR] : byungsu.jeon@lge.com : 120604 : chameleon value for Sprint and BM ]]
    public static void checkChameleon() {
        String chamelion = SystemProperties.get("ro.chameleon.mobileid", "1");
        if ("1".equals(chamelion) && hidden.contains("com.sprint.w.installer")) {
            hidden.remove("com.sprint.w.installer"); // Sprint ID
        }

        chamelion = SystemProperties.get("ro.chameleon.mobilezone", "1");
        if ("1".equals(chamelion) && hidden.contains("com.sprint.zone")) {
            hidden.remove("com.sprint.zone"); // Sprint mobilezone
        }

        chamelion = SystemProperties.get("ro.chameleon.vvm", "1");
        if ("1".equals(chamelion)) {
            hidden.remove("com.coremobility.app.vnotes"); // VVM will be displayed
            hidden.add("com.sprint.voicemail"); // Traditional VM Should be HIDDEN
        } else {
            hidden.remove("com.sprint.voicemail"); // Traditional VM will be displayed
            hidden.add("com.coremobility.app.vnotes"); // VVM Should be HIDDEN
        }
    }

    public static boolean supportInternalMemory() {
        boolean result = false;
        if (!Environment.isExternalStorageRemovable()) {
            result = true;
        }

        return result;
    }

    //[S][jaeyoon.hyun] This function for upgrade model ICS to JB
    public static boolean isUpgradeModel() {
        return (Build.DEVICE.equals("geeb")
                || Build.DEVICE.equals("geehrc4g") || Build.DEVICE.equals("geehrc")
                || Build.DEVICE.equals("geehdc"));
    }

    //[E][jaeyoon.hyun] This function for upgrade model ICS to JB

    //[S][jaewoong87.lee] Check hotspot use app or common tethering
    public static boolean isLUpgradeModelForMHP() {
        return LgeWifiConfig.useMobileHotspot();
    }
    //[E][jaewoong87.lee] Check hotspot use app or common tethering    

    public static boolean isBackVolumeButtonModel() {
        return (Build.DEVICE.equals("b1") ||
                Build.DEVICE.equals("g2m") ||
                Build.DEVICE.equals("g2mds") || Build.DEVICE.equals("g2"));

    }

    public static boolean isFolderModel(Context context) {
        boolean sIsFolder = false;
        try {
            Class configBuildFlagsClass = Class.forName("com.lge.config.ConfigBuildFlags");
            Field field = configBuildFlagsClass.getField("CAPP_12KEY");
            if (field != null) {
                sIsFolder = field.getBoolean(new LGContext(context));
            }
        } catch (Exception e) {
        }
        sIsFolder = Config.getFWConfigBool(context, sIsFolder, "com.lge.config.ConfigBuildFlags.CAPP_12KEY");        
        return sIsFolder;
    }

    public static boolean supportOneHandOperation(Context context) {
        if ("DCM".equals(Config.getOperator())
                || isUpgradeModel() || supportSplitView(context)) {
            return false;
        }
        else {
            DisplaySizeinfo mdispalysizeinfo = new DisplaySizeinfo(context);
            boolean LCDstatus = false;

            try {

                float displaysize = mdispalysizeinfo.getSquaredDiagonalLengthOfLcdInInches();
                double lcdsize = Math.sqrt(displaysize);

                Log.i("displaysizeinfo", "displaysize :" + displaysize);
                Log.i("displaysizeinfo", "lcdsize :" + lcdsize);

                if (lcdsize >= 4.65) {
                    LCDstatus = true;
                }
            } catch (Exception e) {

                Log.i("displaysizeinfo", "Exception occur");
            }

            return LCDstatus;
        }
    }

    // jongtak0920.kim Add Notification Flash
    public static boolean supportNotificationFlash(Context context) {
        return Config.getFWConfigBool(context, com.lge.R.bool.config_powerLight_available,
                           "com.lge.R.bool.config_powerLight_available");
    }

    // jongtak0920.kim Add Emotional LED
    public static boolean supportEmotionalLED(Context context) {
        if (Config.getFWConfigInteger(context, com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 0) {
            return false;
        } else {
            return true;
        }
    }

    //[S][chris.won][2012-10-29] Share & Connect
    public static boolean supportShareConnect(Context context) {
        if ("DCM".equals(Config.getOperator()) || Utils.isWifiOnly(context)) {
            return false;
        }

        if (NfcAdapter.getDefaultAdapter(context) == null
                && !ShareConnection.isSupportSmartShareBeam(context)
                && !isPCSuiteUISupportSettingMenu(context)
                && !ShareConnection.isNearbyMenu(context)) {
            return false;
        } else {
            return true;
        }
    }

    //[E][chris.won][2012-10-29] Share & Connect
    public static boolean isPCSuiteUISupportSettingMenu(Context context) {

        if (!isEnableCheckPackage(context, PACKAGE_NAME_PCSUITE)) {
            Log.e(TAG, "[isPCSuiteUISupportSettingMenu] not support com.lge.sync");
            return false;
        }

        if (Utils.isUI_4_1_model(context)) {
            PackageManager pm = context.getPackageManager();
            if (null != pm) {
                try {
                    PackageInfo pkgInfo = pm.getPackageInfo(PACKAGE_NAME_PCSUITEUI, 0);
                    if (null != pkgInfo && (pkgInfo.versionCode >= 40200000)) {
                        return true;
                    } else {
                        Log.e(TAG,
                                "[isPCSuiteUISupportSettingMenu] pcsyncui versionCode is lower than 40200000");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "[isPCSuiteUISupportSettingMenu] not support com.lge.pcsyncui");
                    return false;
                }
            }
        }
        return false;
    }

    //[S][hyunjeong.shin][2013-12-12] getDisplaySize for UI
    public static String getScreenSize(Context context) {
        int density = context.getResources().getDisplayMetrics().densityDpi;
        String screenSize = null;
        try {
            switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                screenSize = "density_ldpi";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                screenSize = "density_mdpi";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                screenSize = "density_hdpi";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                screenSize = "density_xhdpi";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                screenSize = "density_xxhdpi";
                break;
            default:
                break;
            }
        } catch (Exception e) {
            Log.i(TAG, "Exception occured in getScreenSize");
        }
        return screenSize;
    }

    //[E][hyunjeong.shin][2013-12-12] getDisplaySize for UI

    public static String getEasyTabIndex(String topActivity) {
        Log.d(TAG, "getEasyTabIndex::topActivity=" + topActivity);

        //[START][TD#38140] Defined EasyTabIndex each Activity. 2013-05-31, ilyong.oh@lge.com
        if ("com.android.settings.Settings$WifiSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$BluetoothSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$ShareConnectionActivity".equals(topActivity) ||
                "com.android.settings.Settings$DataUsageSummaryActivity".equals(topActivity) ||
                "com.android.settings.Settings$TetherNetworkSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$WirelessMoreSettingsActivity".equals(topActivity)) {
            //[E N D][TD#38140] Defined EasyTabIndex each Activity. 2013-05-31, ilyong.oh@lge.com
            return "wireless";
        }
        else if ("com.android.settings.Settings$SoundSettingsActivity".equals(topActivity)) {
            return "sound";
        }
        else if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity)
                ||
                "com.android.settings.Settings$LocationSettingsActivity".equals(topActivity)
                ||
                "com.android.settings.Settings$AccountSyncSettingsActivity".equals(topActivity)
                ||
                "com.android.settings.lge.QuadGearBox".equals(topActivity)
                ||
                "com.android.settings.Settings$PowerSaveSettingsActivity".equals(topActivity)
                ||
                "com.android.settings.Settings$StorageSettingsActivity".equals(topActivity)
                ||
                "com.android.settings.Settings$AccountsGroupSettingsActivity".equals(topActivity)
                ||
                "com.android.settings.Settings$BatterySettingsActivity".equals(topActivity)
                ||
                "com.android.settings.Settings$ConnectivitySettingsActivity".equals(topActivity)
                ||
                "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity"
                        .equals(topActivity) ||
                "com.android.settings.Settings$SmsDefaultSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$DateTimeSettingsActivity".equals(topActivity) ||
                "com.android.settings.UsbSettings".equals(topActivity)
                || "com.android.settings.Settings$UserSettingsActivity".equals(topActivity)) {
            return "general";
        }
        else if ("com.lge.lockscreensettings.lockscreen.LockSettings".equals(topActivity)) {
            return "display";
        }
        return null;
    }

    public static boolean isVeeModel(Context context) {
        return false;
    }

    public static boolean isVeeModel() {
        return false;
    }

    public static boolean isAnimationOff() {
        return false;
    }

    public static boolean isAnimationHalf() {
        return false;
    }

    public static void recycleView(View root) {
        if (root == null) {
            return;
        }

        //root.setBackgroundDrawable(null);
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)root;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                recycleView(group.getChildAt(i));
            }

            if (!(root instanceof AdapterView)) {
                group.removeAllViews();
            }
        }

        if (root instanceof ImageView) {
            ((ImageView)root).setImageDrawable(null);
        }

        root = null;
        return;
    }

    public static String getParentActivityName(Context context, ComponentName cm) {
        PackageManager pm = context.getPackageManager();
        String pn = null;
        try {
            pn = pm.getActivityInfo(cm, PackageManager.GET_META_DATA).parentActivityName;

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pn;
    }

    public static boolean supportHotkey(Context context) {
        return ConfigHelper.isSupportQuickMemoHotkey(context);
    }

    public static boolean supportRearVolumeKey(Context context) {
        return ConfigHelper.isSupportRearSideKey(context);
    }

    public static boolean supportCameraOnlyShortcutKey(Context context) {
        return checkPackage(context, HotkeyInfo.DEFAULT_PACKAGE) ? false : true;
    }

    public static boolean supportShortCutKeyNameAsQmemo(Context context) {
         if ("JP".equals(Config.getCountry()) || "KR".equals(Config.getCountry())) {
                    return true;
                }
                else {
                    return false;
                }
    }

    public static boolean isEnableEasyUI() {
        return false;
    }

    public static boolean supportUnusedApps(Context context) {
        return checkPackage(context, "com.lge.appcleanup");
    }

    public static boolean isSPRModel() {

        if ((Config.getOperator().equals("SPR") ||
                Config.getOperator().equals("SPRINT") || Config.getOperator().equals("BM"))) {
            return true;
        }
        return false;
    }

    public static int getPowerTonePosition() {
        int positionNo = 0;
        Log.d("MPCSPowerTonePicker", "1 = " + String.valueOf(positionNo));
        try {
            File f = new File("data/data/com.android.settings/powersound/");
            File[] files = f.listFiles();
            Log.d("MPCSPowerTonePicker", "2 = " + String.valueOf(positionNo));
            Log.d("MPCSPowerTonePicker", "file name = " + f);
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    positionNo = Integer.parseInt(files[i].getName());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "IOException");
        }
        return positionNo;
    }

    public static boolean hasReadyMobileRadio(Context context) {
        final ConnectivityManager conn = ConnectivityManager.from(context);
        final TelephonyManager tele = TelephonyManager.from(context);
        boolean mRetValue = true;
        // require both supported network and ready SIM
        //[chris.won@lge.com][2012-12-18] get Radio state for CDMA models
        if ("SPR".equals(Config.getOperator())
            || Config.MPCS.equals(Config.getOperator())
            || hasNetworkLocked(context)) {
            mRetValue = conn.isNetworkSupported(TYPE_MOBILE);
        } else if (tele.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA
                && tele.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_FALSE) {
            SLog.i("hasReadyMobileRadio :: " + conn.isNetworkSupported(TYPE_MOBILE));
            mRetValue = conn.isNetworkSupported(TYPE_MOBILE);
        } else {
            if (OverlayUtils.isMultiSimEnabled()) {
                SLog.i("hasReadyMobileRadio :: "
                        + OverlayUtils.getAvailableCurrNetworkSimState(context));
                mRetValue = conn.isNetworkSupported(TYPE_MOBILE)
                        && OverlayUtils.getAvailableCurrNetworkSimState(context);
            } else {
                SLog.i("hasReadyMobileRadio :: " + (tele.getSimState() == SIM_STATE_READY));
                mRetValue = conn.isNetworkSupported(TYPE_MOBILE)
                        && tele.getSimState() == SIM_STATE_READY;
            }
        }
        SLog.i("hasReadyMobileRadio :: mRetValue = " + mRetValue);
        return mRetValue;
    }

    /* Used by UserSettings as well. Call this on a non-ui thread. */
    public static boolean copyMeProfilePhoto(Context context, UserInfo user) {
        Log.d("YSY", "copyMeProfilePhoto");

        Uri contactUri = Profile.CONTENT_URI;

        InputStream avatarDataStream = Contacts.openContactPhotoInputStream(
                context.getContentResolver(),
                contactUri, true);
        // If there's no profile photo, assign a default avatar
        if (avatarDataStream == null) {
            return false;
        }
        int userId = user != null ? user.id : UserHandle.myUserId();
        UserManager um = (UserManager)context.getSystemService(Context.USER_SERVICE);
        Bitmap icon = BitmapFactory.decodeStream(avatarDataStream);
        um.setUserIcon(userId, icon);
        try {
            avatarDataStream.close();
        } catch (IOException ioe) {
            Log.w(TAG, "IOException");
        }
        return true;
    }

    public static String getMeProfileName(Context context, boolean full) {
        if (full) {
            Log.d("YSY", "getMeProfileName, getProfileDisplayName");
            return getProfileDisplayName(context);
        } else {
            Log.d("YSY", "getMeProfileName, getShorterNameIfPossible");
            return getShorterNameIfPossible(context);
        }
    }

    private static String getShorterNameIfPossible(Context context) {
        final String given = getLocalProfileGivenName(context);
        return !TextUtils.isEmpty(given) ? given : getProfileDisplayName(context);
    }

    private static String getLocalProfileGivenName(Context context) {
        final ContentResolver cr = context.getContentResolver();

        // Find the raw contact ID for the local ME profile raw contact.
        final long localRowProfileId;
        final Cursor localRawProfile = cr.query(
                Profile.CONTENT_RAW_CONTACTS_URI,
                new String[] { RawContacts._ID },
                RawContacts.ACCOUNT_TYPE + " IS NULL AND " +
                        RawContacts.ACCOUNT_NAME + " IS NULL",
                null, null);
        if (localRawProfile == null) {
            return null;
        }
        try {
            if (!localRawProfile.moveToFirst()) {
                return null;
            }
            localRowProfileId = localRawProfile.getLong(0);
        } finally {
            localRawProfile.close();
        }

        // Find the structured name for the raw contact.
        final Cursor structuredName = cr
                .query(
                        Profile.CONTENT_URI.buildUpon().appendPath(Contacts.Data.CONTENT_DIRECTORY)
                                .build(),
                        new String[] { CommonDataKinds.StructuredName.GIVEN_NAME,
                                CommonDataKinds.StructuredName.FAMILY_NAME },
                        Data.RAW_CONTACT_ID + "=" + localRowProfileId + " AND " + Data.MIMETYPE
                                + "=" + "\'" + CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "\'",
                        null, null);
        if (structuredName == null) {
            Log.d("YSY", "structuredName == null");
            return null;
        }

        try {
            if (!structuredName.moveToFirst()) {
                Log.d("YSY", "!structuredName.moveToFirst()");
                return null;
            }
            String partialName = structuredName.getString(0);
            if (TextUtils.isEmpty(partialName)) {
                partialName = structuredName.getString(1);
            }
            Log.d("YSY", "partialName : " + partialName);
            return partialName;
        } finally {
            structuredName.close();
        }
    }

    private static final String getProfileDisplayName(Context context) {
        final ContentResolver cr = context.getContentResolver();
        final Cursor profile = cr.query(Profile.CONTENT_URI,
                new String[] { Profile.DISPLAY_NAME }, null, null, null);
        if (profile == null) {
            return null;
        }
        try {
            if (!profile.moveToFirst()) {
                return null;
            }
            return profile.getString(0);
        } finally {
            profile.close();
        }
    }

    /** Not global warming, it's global change warning. */
    public static Dialog buildGlobalChangeWarningDialog(final Context context, int titleResId,
            final Runnable positiveAction) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResId);
        builder.setMessage(R.string.global_change_warning);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positiveAction.run();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    public static boolean hasMultipleUsers(Context context) {
        return ((UserManager)context.getSystemService(Context.USER_SERVICE))
                .getUsers().size() > 1;
    }

    /**
     * Creates a dialog to confirm with the user if it's ok to remove the user
     * and delete all the data.
     *
     * @param context a Context object
     * @param removingUserId The userId of the user to remove
     * @param onConfirmListener Callback object for positive action
     * @return the created Dialog
     */
    public static Dialog createRemoveConfirmationDialog(Context context, int removingUserId,
            DialogInterface.OnClickListener onConfirmListener) {
        UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        UserInfo userInfo = um.getUserInfo(removingUserId);
        int titleResId;
        int messageResId;
        if (UserHandle.myUserId() == removingUserId) {
            titleResId = R.string.user_confirm_remove_self_title_ex;
            messageResId = R.string.user_confirm_remove_self_message_ex4;
        } else if (userInfo.isRestricted()) {
            titleResId = R.string.user_delete_user_description;
            messageResId = R.string.user_confirm_remove_message_ex8;
        } else if (userInfo.isManagedProfile()) {
            titleResId = R.string.work_profile_confirm_remove_title;
            messageResId = R.string.work_profile_confirm_remove_message;
        } else {
            titleResId = R.string.user_delete_user_description;
            messageResId = R.string.user_confirm_remove_message_ex8;
        }
        Dialog dlg = new AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.yes,
                        onConfirmListener)
                .setNegativeButton(R.string.no, null)
                .create();
        return dlg;
    }

    /**
     * Returns true if the current profile is a managed one.
     */
    public static boolean isManagedProfile(UserManager userManager) {
        UserInfo currentUser = userManager.getUserInfo(userManager.getUserHandle());
        return currentUser.isManagedProfile();
    }

    /**
     * Creates a {@link UserSpinnerAdapter} if there is more than one profile on the device.
     *
     * <p> The adapter can be used to populate a spinner that switches between the Settings
     * app on the different profiles.
     *
     * @return a {@link UserSpinnerAdapter} or null if there is only one profile.
     */
    public static UserSpinnerAdapter createUserSpinnerAdapter(UserManager userManager,
            Context context) {
        List<UserHandle> userProfiles = userManager.getUserProfiles();
        if (userProfiles.size() < 2) {
            return null;
        }

        UserHandle myUserHandle = new UserHandle(UserHandle.myUserId());
        // The first option should be the current profile
        userProfiles.remove(myUserHandle);
        userProfiles.add(0, myUserHandle);

        ArrayList<UserDetails> userDetails = new ArrayList<UserDetails>(userProfiles.size());
        final int count = userProfiles.size();
        for (int i = 0; i < count; i++) {
            userDetails.add(new UserDetails(userProfiles.get(i), userManager, context));
        }
        return new UserSpinnerAdapter(context, userDetails);
    }

    /**
     * Returns a circular icon for a user.
     */
    public static Drawable getUserIcon(Context context, UserManager um, UserInfo user) {
        if (user.iconPath != null) {
            Bitmap icon = um.getUserIcon(user.id);
            if (icon != null) {
                return CircleFramedDrawable.getInstance(context, icon);
            }
        }
        return UserIcons.getDefaultUserIcon(user.id, /* light= */false);
    }

    public static boolean supportDefaultApps(Activity activity) {
        return true;
    }

    // support JB+ or over JB+ , embeded battery model
    public static boolean isEmbededBattery(Context context) {
        //return false; // yonguk.kim 20130214 Block because of building error in G2
        return Config.getFWConfigBool(context, com.lge.R.bool.config_is_using_embedded_battery,
                "com.lge.R.bool.config_is_using_embedded_battery");
    }

    public static boolean isEmbededBatteryWithCover(Context context) {
        return Config.getFWConfigBool(context,
                com.lge.R.bool.config_is_using_embedded_battery_with_cover,
                "com.lge.R.bool.config_is_using_embedded_battery_with_cover");
    }

    public static boolean isGCAModel() {
        String ProductName = SystemProperties.get("ro.product.name");
        return (ProductName.equals("geehrc_open_eu") || ProductName.equals("geehrc_org_com")
                || ProductName.equals("geehrc_pls_pl") || ProductName.equals("geehrc_ply_pl")
                || ProductName.equals("geehrc_tel_au") || ProductName.equals("geehrc_tim_it")
                || ProductName.equals("geehrc_tmn_pt") || ProductName.equals("geehrc_tmo_com")
                || ProductName.equals("geehrc_h3g_com") || ProductName.equals("geehrc_open_cis"));
    }

    public static String getVibrateTypeProperty() {
        return SystemProperties.get("ro.config.vibrate_type");
    }

    public static boolean supportFrontTouchKeyLight() {
        boolean result = false;

        boolean naviBoolean = false;
        boolean modelBoolean = false;
        IWindowManager mWindowManagerService;
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

        try {
            naviBoolean = mWindowManagerService.hasNavigationBar();
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException front key");
        }
        if (!SystemProperties.getBoolean("lge.hw.frontkeyled", false)) {
            modelBoolean = true;
        }

        if (naviBoolean || modelBoolean) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    public static boolean supportHomeKey(Context context) {
        if (Config.getFWConfigInteger(context, com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isUsedSUI3_0() {
        return "L05E".equals(SystemProperties.get("ro.build.product"));
    }

    public static boolean isnotSupportHandsFreeMode() {
        return "gvarfhd".equals(SystemProperties.get("ro.build.product")) ||
                "omega".equals(SystemProperties.get("ro.build.product")) ||
                "omegar".equals(SystemProperties.get("ro.build.product"));
    }

    public static boolean isLGExternalInfoSupport(Context mContext) {
        Boolean iseMMC = false;
        Boolean isSdCard = false;

        StorageManager mStorageManager;
        String mPATH_OF_EXTERNAL_SD = "/storage/external_SD";
        mStorageManager = StorageManager.from(mContext);
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();

        for (StorageVolume volume : storageVolumes) {
            if (volume.isEmulated()) {
                iseMMC = true;
            } else if (volume.getPath().startsWith(mPATH_OF_EXTERNAL_SD)) {
                isSdCard = true;
            }
        }
        if ((SystemProperties.get("ro.build.characteristics").contains("nosdcard") == false) &&
             com.lge.config.ConfigBuildFlags.CAPP_MOVE_SDCARD && iseMMC && isSdCard) {
            Log.d("kjo", "isLGExternalInfoSupport OK");
            return true;
        } else {
            Log.d("kjo", "isLGExternalInfoSupport NOK");
            return false;
        }
    }

    //ask130402 :: +
    public static void set_TelephonyListener(TelephonyManager mPhone,
            PhoneStateListener mPhoneStateListener) {
        if (mPhone != null && mPhoneStateListener != null) {
            mPhone.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE
                    | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                    | PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public static void release_TelephonyListener(TelephonyManager mPhone,
            PhoneStateListener mPhoneStateListener) {
        if (mPhone != null && mPhoneStateListener != null) {
            mPhone.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    //ask130402 :: -
    public static boolean isRTLLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("ar") || language.equals("fa")) {
            return true;
        }
        return false;
    }

    public static boolean isEnglishDigitRTLLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("iw") || language.equals("ku")) {
            return true;
        }
        return false;
    }

    public static boolean supportNavigationButtons() {
    /*    boolean naviBoolean = false;

        IWindowManager mWindowManagerService;
        mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

        try {
            naviBoolean = mWindowManagerService.hasNavigationBar();
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException");
        }
    */
        //        return naviBoolean;
        return false;
    }

    public static boolean supportSplitView(Context context) {
        if (SystemProperties.getBoolean("settings.remote.test", false) == true) {
            return true;
        }

        // for remote fragment loading
        if (Settings.mMyOwnResources != null) {
            return Settings.mMyOwnResources.getBoolean(R.bool.lg_preferences_prefer_dual_pane);
        }

        if (context != null) {
            return context.getResources().getBoolean(R.bool.lg_preferences_prefer_dual_pane);
        }
        return false;
    }

    public static boolean supportRemoteFragment(Context context) {
        if (SystemProperties.getBoolean("settings.remote.test.noremote", false) == true) {
            return false;
        }
        return supportSplitView(context);
    }

    public static boolean isOLEDModel(Context context) {
        return Config.getFWConfigBool(context, com.lge.R.bool.config_lcd_oled,
                "com.lge.R.bool.config_lcd_oled");
    }

    public static boolean isFrench() {
        //String language = Locale.getDefault().getLanguage();
        if (Config.getCountry().equals("FR")) { // only france
            return true;
        }
        return false;
    }

    public static boolean isInsertSIM() {
        if (isMultiSimEnabled()) {
            if (TelephonyManager.getDefault().getSimState(0) == TelephonyManager.SIM_STATE_READY
                || TelephonyManager.getDefault().getSimState(1) == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        } else if (isTripleSimEnabled()) {
            if (TelephonyManager.getDefault().getSimState(0) == TelephonyManager.SIM_STATE_READY
                || TelephonyManager.getDefault().getSimState(1) == TelephonyManager.SIM_STATE_READY
                || TelephonyManager.getDefault().getSimState(2) == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        } else {
            if (TelephonyManager.getDefault().getSimState() == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        }
        return false;
    }

    // rebestm - tap&pay
    public static boolean isTapPay(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            Log.d(LOG_TAG, "FEATURE_NFC off - remove tap&pay");
            return false;
        } else {
            // Only show if NFC is on and we have the HCE feature
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
            if ((adapter != null && !adapter.isEnabled())
                    || !context.getPackageManager().hasSystemFeature(
                            PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
                //             Log.d (LOG_TAG, "adapter.isEnabled() = " + adapter.isEnabled());
                Log.d(LOG_TAG, "FEATURE_NFC_HOST_CARD_EMULATION = " +
                        context.getPackageManager().
                                hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION));
                if (Utils.hasFeatureNfcInner()) {
                    NfcAdapterAddon nfcAdapterAddon = NfcAdapterAddon.getNfcAdapterAddon();
                    if (!context.getPackageManager().hasSystemFeature(
                            PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)
                            || (nfcAdapterAddon != null
                            && !nfcAdapterAddon.isNfcCardModeEnabled())) {
                        Log.d(LOG_TAG, "remove tap&pay");
                        return false;
                    }
                } else {
                    Log.d(LOG_TAG, "else remove tap&pay");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSupportedTapPay(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            Log.d(LOG_TAG, "FEATURE_NFC off - remove tap&pay");
            return false;
        } else {
            // Only show if NFC is on and we have the HCE feature
            //NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
            if (!context.getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
                Log.d(LOG_TAG, "FEATURE_NFC_HOST_CARD_EMULATION = " +
                        context.getPackageManager().
                                hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION));
                return false;
            }
        }
        return true;
    }

    public static boolean is070Model(Context context) {
        String salesCode = SystemProperties.get("ro.csc.sales_code", "EMPTY");
        if ("LGT".equals(salesCode)) {
            return true;
        }
        return false;
    }

    public static boolean isTablet() {
        if ("tablet".equals(SystemProperties.get("ro.build.characteristics"))) {
            return true;
        }

        return false;
    }

    /**
     * @author seungyeop.yeom
     * @date   2013/11/08
     * @brief  It is a method that determines volume of storage (GB <-> MB)
     */
    public static String formatFileSize_vzw(Context context, long number, boolean unit_type) {
        if (context == null) {
            return "";
        }
        final int mDISPLAY_UNIT_KB_CNT = 0x1;
        final int mDISPLAY_UNIT_MB_CNT = 0x2;
        final int mDISPLAY_UNIT_GB_CNT = 0x3;
        float result = number;
        int mUnitCount = 0;
        boolean mIsSmallValue = false;
        int suffix = com.android.internal.R.string.byteShort;
        if (result > 900) {
            suffix = com.android.internal.R.string.kilobyteShort;
            result = result / 1024;
            mUnitCount++;
        }
        if (result > 900) {
            suffix = com.android.internal.R.string.megabyteShort;
            result = result / 1024;
            mUnitCount++;
        }
        if (result > 900
                && true == unit_type) {
            suffix = com.android.internal.R.string.gigabyteShort;
            result = result / 1024;
            mUnitCount++;
        }
        if (unit_type) {
            // unit_type = true  : GB
            if (mUnitCount < mDISPLAY_UNIT_GB_CNT) {
                suffix = com.android.internal.R.string.gigabyteShort;
                if (mUnitCount <= mDISPLAY_UNIT_MB_CNT) {
                    if (mUnitCount == mDISPLAY_UNIT_MB_CNT) {
                        result = result / 1024;
                        if (0 < result
                                && result < 0.01) {
                            mIsSmallValue = true;
                        }
                    } else {
                        if (0 < result) {
                            mIsSmallValue = true;
                        }
                    }
                }
            }
        } else {
            // unit_type = false : MB
            if (mUnitCount < mDISPLAY_UNIT_MB_CNT) {
                suffix = com.android.internal.R.string.megabyteShort;
                if (mUnitCount <= mDISPLAY_UNIT_KB_CNT) {
                    if (mUnitCount == mDISPLAY_UNIT_KB_CNT) {
                        result = result / 1024;
                        if (0 < result
                                && result < 0.01) {
                            mIsSmallValue = true;
                        }
                    } else {
                        if (0 < result) {
                            mIsSmallValue = true;
                        }
                    }
                }
            }
        }
        String value;
        if (mIsSmallValue) {
            value = "< 0.01";
        } else if (result == 0) {
            value = "0";
        } else if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.2f", result);
        } else if (result < 100) {
            value = String.format("%.2f", result);
        } else {
            value = String.format("%.0f", result);
        }

        return context.getResources().
                getString(com.android.internal.R.string.fileSizeSuffix,
                        value, context.getString(suffix));
    }

    /**
     * @author seungyeop.yeom
     * @date   2013/11/08
     * @brief  It is a method that determines whether to reflect SmartVideo.
     */
    public static boolean isSupportSmartVideo() {
        String support_smartvideo = null;
        final String VIDEO_PACKAGE_NAME = "com.lge.videoplayer";
        final String VIDEO_CONFIG_FULLPATH = "/system/etc/%s.xml";

        FileInputStream in = null;
        InputStreamReader reader = null;

        try {
            String filename = String.format(VIDEO_CONFIG_FULLPATH, VIDEO_PACKAGE_NAME);
            in = new FileInputStream(filename);
            reader = new InputStreamReader(in);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(reader);

            String strTag = null;
            boolean initem = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    strTag = parser.getName();
                    Log.d("YSY", "strTag name : " + strTag);
                    if (initem == false && "video_configration".equals(strTag)) {
                        initem = true;
                        break;
                    }
                    if (initem == true) {
                        if ("support_smartvideo".equals(strTag)) {
                            support_smartvideo = parser.getAttributeValue(null, "data");
                            break;
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                default:
                    break;
                }
                eventType = parser.next();
            }
        } catch (FileNotFoundException e1) {
            support_smartvideo = "true";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (support_smartvideo != null) {
            Log.d("YSY", "support_smartvideo state : " + "true".equals(support_smartvideo));
            return ("true".equals(support_smartvideo));
        }
        return false;
    }

    // 20131112 yonguk.kim Apply UsageManager for ATT
    public static Intent buildStartFragmentIntent(String name, Bundle args,
            int titleRes, int shortTitleRes, Context context, int iconId) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, name);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, titleRes);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_SHORT_TITLE, shortTitleRes);
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        intent.putExtra("settings:remove_ui_options", true);
        intent.setClass(context, SubSettings.class);
        intent.putExtra("SETTINGS_FUNCTION_ICON_RRESOURCE_ID", iconId);
        return intent;
    }

    public static boolean isG2Model() {
        if ("g2".equals(SystemProperties.get("ro.product.device"))
                || "g2l01f".equals(SystemProperties.get("ro.product.device"))) {
            return true;
        }
        return false;
    }

    public static boolean isBackLEDRGB(Context context) {
        if (Config.getFWConfigBool(context, com.lge.R.bool.config_hasColorBackLed,
                              "com.lge.R.bool.config_hasColorBackLed")) {
            return true;
        }
        return false;
    }

    public static boolean hasFeatureSMSsummary() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals("ko") || language.equals("hi") || language.equals("ja")
                || language.equals("my") || language.equals("gu")
                || language.equals("mr") || language.equals("pa")) {
            return true;
        }
        return false;
    }

    public static boolean isEnableCheckPackage(Context context, String packageName) {
        PackageInfo pi = null;
        PackageManager pm = context.getPackageManager();
        try {
            pi = pm.getPackageInfo(packageName, 0);
            if (pi.packageName.equals(packageName) && (pi.applicationInfo.enabled == true)) {
                return true;
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "isEnableCheckPackage() is not found(" + packageName
                    + ")");
            return false;
        }
        return false;
    }

    public static boolean isSoftwareVersionCheck(Context context, String packageName,
            int checkVersion) {
        boolean retVal = false;
        if (context != null) {
            try {
                int version = context.getPackageManager().getPackageInfo(packageName,
                        PackageManager.GET_META_DATA).versionCode;
                Log.d("YSY", "current software version : " + version);
                if (version >= checkVersion)
                {
                    retVal = true;
                }
            } catch (NameNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        return retVal;
    }

    public static String onLGFormatter(String formattedDates) { // 2MB -> 2 MB (B,MB,KB,GB)
        char format[] = { 'M', 'K', 'G' };

        StringBuffer sb = new StringBuffer(formattedDates);
        int length = sb.length();
        int index = length - 2;
        boolean b2nd = false;

        String language = Locale.getDefault().getLanguage();

        if (length <= 0) {
            Log.e(TAG, "String is null!!! return!!!");
            return formattedDates;
        }

        for (int i = 0; i < format.length; i++) {
            if (formattedDates.charAt(index) == format[i]) {
                b2nd = true;
                break;
            }
        }
        if (!b2nd) {
            index = length - 1;
        }
        if (language.equals("ru") || language.equals("ar") || language.equals("fa")
                || language.equals("iw") || language.equals("ku")) {
            return formattedDates; //sb.insert(index, " ").toString();
        } else {
            return sb.insert(index, " ").toString();
        }
    }

    public static boolean getDefaultBooleanValue(Context context, String pkgName, String Name) {
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
        } catch (NotFoundException e) {
            Log.e(TAG, "NotFoundException : " + e.getMessage());
        }
        return false;
    }

    public static boolean isSharingBetweenSimHotkey(Context context) {
        return ConfigHelper.isSupportSharingWithSimHotKey(context);
    }

    public static boolean isUI_4_1_model(Context context) {
        return true;
    }

    public static boolean isSupportSKTVerifyApps() {
        boolean result = false;
        if ("SKT".equals(Config.getOperator())) {
            if (com.lge.os.Build.LGUI_VERSION.RELEASE >=
                   com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
             result = true;
            } else {
                if ("liger".equals(SystemProperties.get("ro.product.device"))
                || "z2".equals(SystemProperties.get("ro.product.device"))) {
                   result = true;
               }
            }
        } return result;
    }

    public static boolean isSupportUIV4_2() {
        boolean result = false;
        if (com.lge.os.Build.LGUI_VERSION.RELEASE >=
                com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            result = true;
        }
        return result;
    }
    public static boolean isChinaOperator() {
        if (Config.CMCC.equals(Config.getOperator())
                || "CMO".equals(Config.getCountry())) {
            return true;
        } else if ("CN".equals(Config.getCountry())) {
            return true;
        } else {
            return false;
        }
    }
    public static int getAirplaneFlow(Context context) {
        // 1 -> Native - OK
        // 2 -> LG - Yes/no
        int return_value = 1;
        if (isUI_4_1_model(context)) {
            return_value = 2;
        }
        Log.d(TAG, "getAirplaneFlow = " + return_value);
        return return_value;
    }

    /*You can use Settings resources directly here*/
    public static String getString(int resId) {
        return Settings.mMyOwnResources.getString(resId);
    }

    public static Resources getResources() {
        return Settings.mMyOwnResources;
    }

    public static boolean isNotUseEmailLED(Context context) {
        if ("b1".equals(SystemProperties.get("ro.product.device"))
                || isUI_4_1_model(context)) {
            return true;
        }
        return false;
    }

    public static boolean isSupprotColoring(Context context) {
        if ("SKT".equals(Config.getOperator()) &&
                "KR".equals(Config.getCountry())) {
            if ("f70n_skt_kr".equals(SystemProperties.get("ro.product.name"))) {
                return true;
            } else {
                if (isUI_4_1_model(context) == true) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isHapticfeedbackSupport() {
        if ("1".equals(SystemProperties.get("ro.device.hapticfeedback", "1"))) {
            Log.d(TAG, "[isHapticfeedbackSupport] true");
            return true;
        }
        Log.d(TAG, "[isHapticfeedbackSupport] false");
        return false;
    }

    public static boolean getGuestModeFeature(Context context) {
        String config = "android.resource://com.lge.launcher2/bool/config_feature_enable_kidsmode";
        boolean guestModeEnable = getBooleanResource(config, context);
        Log.d(TAG, "guestModeEnable: " + guestModeEnable);
        return guestModeEnable;
    }

    public static boolean getBooleanResource(String resUri, Context context) {
        Uri uri = Uri.parse(resUri);
        String path = uri.getPath();
        String pkg = uri.getHost();

        String[] arr = path.split("/");
        String type = arr[1];
        String name = arr[2];

        if (!type.equalsIgnoreCase("bool")) {
            return false;
        }

        try {
            Resources res = context.getPackageManager().getResourcesForApplication(pkg);
            int id = res.getIdentifier(name, type, pkg);
            return res.getBoolean(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String removeAllWhitespace(String mStr) {
        if (mStr == null) {
            return "";
        }
        StringTokenizer mSt = new StringTokenizer(mStr.trim());
        StringBuilder mSb = new StringBuilder();
        while (mSt.hasMoreTokens()) {
            mSb.append(mSt.nextToken());
        }
        Log.d(TAG, "removeAllWhitespace ->" + mSb);
        return mSb.toString();
    }

    public static boolean existInTokens(String mString, String mFindStr) {
        if (mString == null) {
            return false;
        }
        StringTokenizer mSt = new StringTokenizer(mString, ",");
        while (mSt.hasMoreTokens()) {
            if (mSt.nextToken().equals(mFindStr)) {
                Log.d(TAG, "existInTokens -> find " + mFindStr);
                return true;
            }
        }
        Log.d(TAG, "existInTokens -> not find " + mFindStr);
        return false;
    }

    /* [S][2014.04.24][girisekhar1.a@lge.com][TLF-COM] DataSwitcher pop-up for TLF_COM */
    public static boolean isTlfSpainSim(Context context) {
        String mccmnc = ((TelephonyManager)context.getSystemService(
                Context.TELEPHONY_SERVICE)).getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            Log.d(TAG, "isTlfSpainSim : false, mccmnc null ");
            return false;
        }

        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        Log.i(TAG, "[Step1] MCC: " + mcc + "/ MNC: " + mnc);
        if (!("214".equals(mcc) && ("07".equals(mnc)))) {
            Log.d(TAG, "isTlfSpainSim : false");
            return false;
        }
        Log.d(TAG, "isTlfSpainSim : true");
        return true;
    }

    /* [E][2014.04.24][girisekhar1.a@lge.com][TLF-COM] DataSwitcher pop-up for TLF_COM */

    /* [S][2014.08.28][girisekhar1.a@lge.com][OPEN-ESA] DataSwitcher pop-up for Reliance JIO */
    public static boolean isRelianceJioSim(Context context) {
        String mccmnc = ((TelephonyManager)context.getSystemService(
            Context.TELEPHONY_SERVICE)).getSimOperator();
        if (mccmnc == null || mccmnc.length() < 5) {
            Log.d(TAG, "isRelianceJioSim : false, mccmnc null ");
            return false;
        }

        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        Log.i(TAG, "[Step1] MCC: " + mcc + "/ MNC: " + mnc);
        if (!("405".equals(mcc) && ("840".equals(mnc) ||
                                    "854".equals(mnc) ||
                                    "855".equals(mnc) ||
                                    "856".equals(mnc) ||
                                    "857".equals(mnc) ||
                                    "858".equals(mnc) ||
                                    "859".equals(mnc) ||
                                    "860".equals(mnc) ||
                                    "861".equals(mnc) ||
                                    "862".equals(mnc) ||
                                    "863".equals(mnc) ||
                                    "864".equals(mnc) ||
                                    "865".equals(mnc) ||
                                    "866".equals(mnc) ||
                                    "867".equals(mnc) ||
                                    "868".equals(mnc) ||
                                    "869".equals(mnc) ||
                                    "870".equals(mnc) ||
                                    "871".equals(mnc) ||
                                    "872".equals(mnc) ||
                                    "873".equals(mnc) ||
                                    "874".equals(mnc)))) {
            Log.d(TAG, "isRelianceJioSim : false");
            return false;
        }
        Log.d(TAG, "isRelianceJioSim : true");
        return true;
    }
    /* [E][2014.08.28][girisekhar1.a@lge.com][OPEN-ESA] DataSwitcher pop-up for Reliance JIO */

    /**
    @method      getDisableDisplayPKGList
    @date        2014/09/19
    @author      seungyeop.yeom@lge.com
    @brief       It is used to provide the app list in the function of Apps.
    @warning     Operators used in the SPR (http://mlm.lge.com/di/browse/LGSF-2590)
    */
    public static HashSet<String> getDisableDisplayPKGList() {
        Log.d("yeom", "========== getDisableDisplayPKGList ==========");
        HashSet<String> mDisableDisplayPKGList = new HashSet<String>();
        String mSystemUpkgFile = File.separator + "system" + File.separator + "etc"
                + File.separator + "default_u_pkg_list.cfg";
        String mCarrierUpkgFile = File.separator + "carrier" + File.separator + "cust"
                + File.separator + "config" + File.separator + "custom_u_pkg_list.cfg";

        mDisableDisplayPKGList.clear();
        String configFilePath = null;
        String hfaFlag = SystemProperties.get("ro.sprint.hfa.flag");
        if ((null != hfaFlag) && (hfaFlag.compareTo("activationOK") == 0)) {
            configFilePath = mCarrierUpkgFile;
        } else {
            configFilePath = mSystemUpkgFile;
        }

        Log.d("yeom", "configFilePath: " + configFilePath);
        File customApkListFile = new File(configFilePath);
        if (!customApkListFile.exists()) {
            Log.d("yeom", "File is not exist");
            return null;
        }

        HashSet<String> customApkList = readCustom_U_PKGListFile(customApkListFile
                .getAbsolutePath());

        if (customApkList.isEmpty()) {
            Log.d("yeom", "File is empty");
            return null;
        }

        for (String action : customApkList) {
            String pkgName = action.substring(0);
            mDisableDisplayPKGList.add(pkgName);
        }

        return mDisableDisplayPKGList;
    }

    /**
    @method      readCustom_U_PKGListFile
    @date        2014/09/19
    @author      seungyeop.yeom@lge.com
    @brief       it is defined for getDisableDisplayPKGList method.
    */
    private static HashSet<String> readCustom_U_PKGListFile(String path) {

        Log.d("yeom", "========== readCustom_U_PKGListFile ==========");
        Log.d("yeom", "path: " + path);

        BufferedReader br = null;
        HashSet<String> list = new HashSet<String>();
        try {
            br = new BufferedReader(new FileReader(path));
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    list.add(line);
                }
            }
            br.close();
        } catch (IOException ioe) {
            Log.e("yeom", "Could not read " + path + " -->" + ioe.getMessage());
        }
        return list;
    }

    public static String do_getSoundSimName(Context context, int type, int index) {
        //final int sRENAME_SIM_TYPE_RINGTONE = 0;
        final int sRENAME_SIM_TYPE_NOTIFICATION = 1;
        final int sRENAME_SIM_TYPE_VIBRATION = 2;

        final int sRENAME_SIM1_INDEX = 0;
        final int sRENAME_SIM2_INDEX = 1;
        final int sRENAME_SIM3_INDEX = 2;

        int res_title = R.string.sp_sub_rename_sim_ringtone_title_NORMAL;
        String sim_slot = SettingsConstants.System.SIM1_NAME;
        if (type == sRENAME_SIM_TYPE_NOTIFICATION) { //notification type
            res_title = R.string.sp_sub_rename_sim_notification_title_NORMAL;
        } else if (type == sRENAME_SIM_TYPE_VIBRATION) { //vibration type
            res_title = R.string.sp_rename_vibration_type;
        }

        if (index == sRENAME_SIM2_INDEX) {
            sim_slot = SettingsConstants.System.SIM2_NAME;
        }
        if (index == sRENAME_SIM3_INDEX) {
            sim_slot = SettingsConstants.System.SIM3_NAME;
        }

        String sim_name = OverlayUtils.getMSimTabName(context, sim_slot);
        String org_name = getDefalutSimNameResourceIdx(context, index);
        Log.d(TAG, "sim_name = " + sim_name + "org_name = " + org_name);

        if (sim_name.equals(org_name) || sim_name.equals("NULL")) {
            if (index == sRENAME_SIM1_INDEX) {
                sim_name = context.getResources().getString(R.string.sp_setting_sim1_NORMAL);
            }
            if (index == sRENAME_SIM2_INDEX) {
                sim_name = context.getResources().getString(R.string.sp_setting_sim2_NORMAL);
            }
            if (index == sRENAME_SIM3_INDEX) {
                sim_name = context.getResources().getString(R.string.sp_setting_sim3_NORMAL);
            }
        }

        if (sim_name.equals("Slot 1") || sim_name.equals("Slot 2")
                || sim_name.equals("Slot 3")) {
            sim_name = setSimName(context, index, sim_name);
        }
        Log.d(TAG, "[do_getSoundSimName] = " + sim_slot + ":" + sim_name);
        return context.getResources().getString(res_title, sim_name);
    }

    public static String setSimName(Context context, int index, String sim_name) {
        final int sRENAME_SIM1_INDEX = 0;
        final int sRENAME_SIM2_INDEX = 1;
        final int sRENAME_SIM3_INDEX = 2;

        if (index == sRENAME_SIM1_INDEX) {
            sim_name = context.getResources().getString(
                    R.string.sp_sim_slot_1_NORMAL);
        }
        if (index == sRENAME_SIM2_INDEX) {
            sim_name = context.getResources().getString(
                    R.string.sp_sim_slot_2_NORMAL);
        }
        if (index == sRENAME_SIM3_INDEX) {
            sim_name = context.getResources().getString(
                    R.string.sp_sim_slot_3_NORMAL);
        }
        return sim_name;
    }

    private static String getDefalutSimNameResourceIdx(Context context, int simId) {
        int ret = 0;
        switch (simId) {
        case 0:
            ret = R.string.sp_defaultNameSIM1;
            break;
        case 1:
            ret = R.string.sp_defaultNameSIM2;
            break;
        case 2:
            ret = R.string.sp_defaultNameSIM3;
            break;
        default:
            ret = R.string.sp_defaultNameSIM1;
            break;
        }
        return context.getResources().getString(ret);
    }

    public static String getBaseActivityClassName(Context context) {
        ActivityManager am =
                (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> info = am.getRunningTasks(1);
        ComponentName topActivity = info.get(0).topActivity;
        String topActivityClassName = topActivity.getClassName();
        String baseActivityClassName = info.get(0).baseActivity.getClassName();
        String parentActivityName = Utils.getParentActivityName(context, topActivity);
        Log.i(TAG, "top : " + topActivityClassName);
        Log.i(TAG, "base : " + baseActivityClassName);
        Log.i(TAG, "parent : " + parentActivityName);
        return baseActivityClassName;

    }

    public static int getUsingSettings(int settingStyle, Context context) {
        if (settingStyle == 1 && Utils.supportEasySettings(context)) {
            ActivityManager am = (ActivityManager)context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> info = am.getRunningTasks(1);
            ComponentName topActivity = info.get(0).topActivity;
            String topActivityClassName = topActivity.getClassName();
            String baseActivityClassName = info.get(0).baseActivity.getClassName();
            Log.d(TAG, "top=" + topActivityClassName + "  base=" + baseActivityClassName);
            if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                return sUSING_EASYSETTINGS;
            } else {
                Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                Log.d(TAG, "tabIndex=" + tabIndex);
                settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                context.startActivity(settings);
                return sUSING_SETTINGS;
            }
        }
        return sUSING_NOTHING;
    }

    public static String formatElapsedTime(Context context, double millis, boolean inclSeconds) {
        final int mSECONDS_PER_MINUTE = 60;
        final int mSECONDS_PER_HOUR = 60 * 60;
        final int mSECONDS_PER_DAY = 24 * 60 * 60;

        StringBuilder sb = new StringBuilder();
        int seconds = (int)Math.floor(millis / 1000);
        if (!inclSeconds) {
            // Round up.
            seconds += 30;
        }

        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (seconds > mSECONDS_PER_DAY) {
            days = seconds / mSECONDS_PER_DAY;
            seconds -= days * mSECONDS_PER_DAY;
        }
        if (seconds > mSECONDS_PER_HOUR) {
            hours = seconds / mSECONDS_PER_HOUR;
            seconds -= hours * mSECONDS_PER_HOUR;
        }
        if (seconds > mSECONDS_PER_MINUTE) {
            minutes = seconds / mSECONDS_PER_MINUTE;
            seconds -= minutes * mSECONDS_PER_MINUTE;
        }
        if (inclSeconds) {
            if (days > 0) {
                sb.append(context.getString(R.string.battery_history_days,
                        days, hours, minutes, seconds));
            } else if (hours > 0) {
                sb.append(context.getString(R.string.battery_history_hours,
                        hours, minutes, seconds));
            } else if (minutes > 0) {
                sb.append(context.getString(R.string.battery_history_minutes, minutes, seconds));
            } else {
                sb.append(context.getString(R.string.battery_history_seconds, seconds));
            }
        } else {
            if (days > 0) {
                sb.append(context.getString(R.string.battery_history_days_no_seconds,
                        days, hours, minutes));
            } else if (hours > 0) {
                sb.append(context.getString(R.string.battery_history_hours_no_seconds,
                        hours, minutes));
            } else {
                sb.append(context.getString(R.string.battery_history_minutes_no_seconds, minutes));
            }
        }

        return sb.toString();
    }

    public static boolean isDimmingMobileDataForVolte(Context context) {
        boolean mValue = false;
        if (false == "VZW".equals(Config.getOperator())) {

        } else if (getCurrentVoLTEStatus(context)) {
            mValue = true;
        } else if (isSupportedVolte(context) && isInVideoCall()) {
            mValue = true;
        }
        Log.i(TAG, "Video Call State = " + mValue);
        return mValue;
    }

    public static boolean isSupportedVolte(Context context) {
        if (null == context) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature("com.lge.ims.volte");
    }

    public static final boolean isInVideoCall() {
        TelephonyManager mPhone = TelephonyManager.getDefault();
        boolean mValue = false;
        int state = mPhone.getCallState();
        if (state >= 100) {
            mValue = true;
        }
        Log.i(TAG, "Video Call State = " + mValue);
        return mValue;
    }

    public static int floatToIndex(Context context, float val) {
        String[] indices = context.getResources().getStringArray(
                R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal - lastVal) * .5f)) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }

    public static int getInternalRes(String packageName, String type, String name) {
        int id = 0;
        Log.d("kimyow_test", "getInternalRes:type=" + type + ", name=" + name);
        try {
            Class<?> type_cls = Class.forName(packageName
                    + type);
            Field f = type_cls.getDeclaredField(name);
            id = f.getInt(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return id;
    }

    public static int[] getInternalResArray(String packageName, String type, String name) {
        int [] ids = null;
        Log.d("kimyow_test", "getInternalResArray:type=" + type + ", name=" + name);
        try {
            Class<?> type_cls = Class.forName(packageName
                    + type);
            Field f = type_cls.getDeclaredField(name);

            if (f.getType().isArray()) {
                Class cType = f.getType().getComponentType();

                if (cType == Integer.TYPE) {
                    Object array = f.get(null);
                    int length = Array.getLength(array);
                    ids = new int[length];
                    for (int i = 0; i < length; i++) {
                        ids[i] = (int)Array.getInt(array, i);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return ids;
    }

    //Set auto LTE when Data_enable set on
    public static void restoreLteModeForKddi(Context context, boolean mIsDataEnabled) {
        if (isTestSim(context) || isSupportedVolte(context)) {
            SLog.d("restoreLteModeForKddi == if it is test sim, skip to disable the lte use. ");
            return;
        }

        boolean mIsLteEnable = true;
        if (mIsDataEnabled) {
            int isChecked = android.provider.Settings.Secure.getInt(context.getContentResolver(),
                    "lte_value_when_data_onoff",
                    0);
            SLog.i("restoreLteModeForKddi() Data on isChecked = "
                    + isChecked);
            if (isChecked == 1) {
                Intent intent = new Intent("SetNetworkMode_KDDI_LTE");
                intent.putExtra("NetworkType", 0);
                context.sendBroadcast(intent);
            }
        } else {
            mIsLteEnable = isLteEnabled(context);
            android.provider.Settings.Secure.putInt(context.getContentResolver(),
                    "lte_value_when_data_onoff",
                    (mIsLteEnable == true ? 1 : 0));

            SLog.i("restoreLteModeForKddi() Data off save db lte value = " + mIsLteEnable);
            Intent intent = new Intent("Data_off_LTE_value");
            intent.putExtra("LTE_VALUE", mIsLteEnable);
            context.sendBroadcast(intent);
        }
    }

    public static boolean isLteEnabled(Context context)
    {
        int settingsNetworkMode = android.provider.Settings.Global.getInt(
                context.getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                Phone.PREFERRED_NT_MODE);
        SLog.d("isLteEnabled = " + settingsNetworkMode);
        switch (settingsNetworkMode) {
        case Phone.NT_MODE_LTE_ONLY:
        case Phone.NT_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
        case Phone.NT_MODE_LTE_WCDMA:
        case Phone.NT_MODE_LTE_GSM_WCDMA:
        case Phone.NT_MODE_LTE_CDMA_AND_EVDO:
            return true;
        default:
            return false;
        }
    }

    public static void sendBroadcastDdsForMMS (Context context, boolean mValue) {
        Intent intent = new Intent();
        intent.setAction("com.lge.mms.intent.action.switch_dds");
        intent.putExtra("data_status", mValue);
        SLog.i("sendBroadcastDdsForMMS mValue " + mValue);
        context.sendBroadcast(intent);
    }

    public static boolean hasNetworkLocked(Context context) {
        if (Config.USC.equals(Config.getOperator())) {
            if (SystemProperties.get("gsm.sim.state").equals("CARD_IO_ERROR")) {
                SLog.i("hasNetworkLocked lock ==");
                return true;
            }
            SLog.i("hasNetworkLocked unlock ==");
        }
        return false;
    }


    /**
    * Returns the target user for a Settings activity.
    *
    * The target user can be either the current user, the user that launched this activity or
    * the user contained as an extra in the arguments or intent extras.
    *
    * Note: This is secure in the sense that it only returns a target user different to the current
    * one if the app launching this activity is the Settings app itself, running in the same user
    * or in one that is in the same profile group, or if the user id is provided by the system.
    */
    public static UserHandle getSecureTargetUser(IBinder activityToken,
            UserManager um, @Nullable Bundle arguments, @Nullable Bundle intentExtras) {
        UserHandle currentUser = new UserHandle(UserHandle.myUserId());
        IActivityManager am = ActivityManagerNative.getDefault();
        try {
            String launchedFromPackage = am.getLaunchedFromPackage(activityToken);
            boolean launchedFromSettingsApp = SETTINGS_PACKAGE_NAME.equals(launchedFromPackage);

            UserHandle launchedFromUser = new UserHandle(UserHandle.getUserId(
                    am.getLaunchedFromUid(activityToken)));
            if (launchedFromUser != null && !launchedFromUser.equals(currentUser)) {
                // Check it's secure
                if (isProfileOf(um, launchedFromUser)) {
                    return launchedFromUser;
                }
            }
            UserHandle extrasUser = intentExtras != null
                    ? (UserHandle)intentExtras.getParcelable(EXTRA_USER) : null;
            if (extrasUser != null && !extrasUser.equals(currentUser)) {
                // Check it's secure
                if (launchedFromSettingsApp && isProfileOf(um, extrasUser)) {
                    return extrasUser;
                }
            }
            UserHandle argumentsUser = arguments != null
                    ? (UserHandle)arguments.getParcelable(EXTRA_USER) : null;
            if (argumentsUser != null && !argumentsUser.equals(currentUser)) {
                // Check it's secure
                if (launchedFromSettingsApp && isProfileOf(um, argumentsUser)) {
                    return argumentsUser;
                }
            }
        } catch (RemoteException e) {
            // Should not happen
            Log.v(TAG, "Could not talk to activity manager.", e);
        }
        return currentUser;
    }

    /**
     * Returns the target user for a Settings activity.
     *
     * The target user can be either the current user, the user that launched this activity or
     * the user contained as an extra in the arguments or intent extras.
     *
     * You should use {@link #getSecureTargetUser(IBinder, UserManager, Bundle, Bundle)} if
     * possible.
     *
     * @see #getInsecureTargetUser(IBinder, Bundle, Bundle)
     */
    public static UserHandle getInsecureTargetUser(IBinder activityToken,
            @Nullable Bundle arguments, @Nullable Bundle intentExtras) {
        UserHandle currentUser = new UserHandle(UserHandle.myUserId());
        IActivityManager am = ActivityManagerNative.getDefault();
        try {
            UserHandle launchedFromUser = new UserHandle(UserHandle.getUserId(
                    am.getLaunchedFromUid(activityToken)));
            if (launchedFromUser != null && !launchedFromUser.equals(currentUser)) {
                return launchedFromUser;
            }
            UserHandle extrasUser = intentExtras != null
                    ? (UserHandle)intentExtras.getParcelable(EXTRA_USER) : null;
            if (extrasUser != null && !extrasUser.equals(currentUser)) {
                return extrasUser;
            }
            UserHandle argumentsUser = arguments != null
                    ? (UserHandle)arguments.getParcelable(EXTRA_USER) : null;
            if (argumentsUser != null && !argumentsUser.equals(currentUser)) {
                return argumentsUser;
            }
        } catch (RemoteException e) {
            // Should not happen
            Log.v(TAG, "Could not talk to activity manager.", e);
            return null;
        }
        return currentUser;
    }

    /**
     * Returns true if the user provided is in the same profiles group as the current user.
     */
    private static boolean isProfileOf(UserManager um, UserHandle otherUser) {
        if (um == null || otherUser == null) {
            return false;
        }
        return (UserHandle.myUserId() == otherUser.getIdentifier())
                || um.getUserProfiles().contains(otherUser);
    }

    public static void setIsInUseVoLTE(boolean bSet) {
        Log.d(LOG_TAG, "setIsInUseVoLTE : " + bSet);
        sIsInUseVoLTE = bSet;
    }

    public static boolean getIsInUseVoLTE() {
        return sIsInUseVoLTE;
    }

    public static boolean getVoipEnabledForVoLTE(Context context) {
       int value = 0;
       value = queryIMSIntegerValue(context, CONTENT_URI, "voip_enabled");
       Log.d(LOG_TAG, "getVoipEnabledForVoLTE : " + value);
       return (value == 1) ? true : false;
    }

    public static boolean getCurrentVoLTEStatus(Context context) {
       int value = 0;
       value = queryIMSIntegerValue(context, CONTENT_URI, "voip_state");
       return (value == 1) ? true : false;
    }

    public static int queryIMSIntegerValue(Context context, Uri uri, String columnName) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        int value = -1;

        try {
            cursor = cr.query(uri, null, null, null, null);

            if (cursor == null) {
                Log.d(LOG_TAG, "Cursor is null : ");
                return value;
            }

            if (cursor.getCount() > 0) {
                int index = cursor.getColumnIndex(columnName);
                if (cursor.moveToFirst() && cursor.getCount() == 1) {
                    value = cursor.getInt(index);
                }
            }
        } catch (Exception e) {
                Log.d(LOG_TAG, "Exception = " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        Log.d(LOG_TAG, "queryIMSIntegerValue-value :" + value );
        return value;
    }

    public static void registerUCStateObserve(Context context) {
        if (sContentObserver != null) {
            unregisterObserver(context);
        }

        ContentResolver cr = context.getContentResolver();
        sContext = context;
        sContentObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                setIsInUseVoLTE(getCurrentVoLTEStatus(sContext));
            }
        };
        cr.registerContentObserver(CONTENT_URI, true, sContentObserver);

    }

    public static void unregisterObserver(Context context) {
        if (sContentObserver == null) {
            return;
        }

        ContentResolver cr = context.getContentResolver();
        cr.unregisterContentObserver(sContentObserver);
        sContentObserver = null;
    }
    /**
     * Returns whether or not this device is able to be OEM unlocked.
     */
    static boolean isOemUnlockEnabled(Context context) {
        PersistentDataBlockManager manager = (PersistentDataBlockManager)
                context.getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);
        return manager.getOemUnlockEnabled();
    }

    /**
     * Allows enabling or disabling OEM unlock on this device. OEM unlocked
     * devices allow users to flash other OSes to them.
     */
    static void setOemUnlockEnabled(Context context, boolean enabled) {
        PersistentDataBlockManager manager = (PersistentDataBlockManager)
                context.getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);
        manager.setOemUnlockEnabled(enabled);
    }

    static public String getDefaultLocale() {
        Locale locale = Locale.getDefault();
        try {
            String defaultLocale = locale.getISO3Language();
            if (TextUtils.isEmpty(defaultLocale)) {
                return "";
            }

            if (!TextUtils.isEmpty(locale.getISO3Country())) {
                defaultLocale += "-" + locale.getISO3Country();
            } else {
                return defaultLocale;
            }
            if (!TextUtils.isEmpty(locale.getVariant())) {
                defaultLocale += "-" + locale.getVariant();
            }
            return defaultLocale;
        } catch (MissingResourceException e) {
            return "eng-usa";
        }
    }

    public static int getCurrentDDS(Context mContext) {
        return LGSubscriptionManager.getDefaultDataPhoneIdBySubscriptionManager(mContext);//SubscriptionManager.getDefaultDataPhoneId();
    }

    public static long getSubIdBySlotId(int slotId) {
        long[] subIdForL = null;
        int[] subIdForLMR1 = null;
        long actualSubId = 99999;

        try {
            if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
                subIdForL = LGSubscriptionManager.getSubIdForL(slotId);

                actualSubId = subIdForL[0];
            } else {
                subIdForLMR1 = LGSubscriptionManager.getSubIdForLMR1(slotId);

                actualSubId = subIdForLMR1[0];
            }

            Log.i(TAG, "getSubIdBySlotId() slotId " + slotId + ", subId " + actualSubId);
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException slot " + slotId);
        }

        return actualSubId;
    }

    public static int getSlotIdBySubId(long subId) {
        int slotId = LGSubscriptionManager.getSlotId(subId);
        Log.i(TAG, "getSlotIdBySubId() subId: " + subId + ", slotId = " + slotId);
        return slotId;
    }

    public static void setMobileDataEnabledDB(Context context, int phoneId, boolean enabled) {
        android.provider.Settings.Global.putInt(context.getContentResolver(),
                android.provider.Settings.Global.MOBILE_DATA + phoneId, enabled ? 1 : 0);
    }

    public static String getSubscriberId(int slotID) {
        String imsi = LGSubscriptionManager.getSubscriberId(
                getSubIdBySlotId(slotID));
        return imsi;
    }

    public static boolean isSupportUSBMultipleConfig(Context context) {
        if ("true".equals(ModelFeatureUtils
                .getFeature(context, "nousbmultiple"))) {
            return false;
        }
        return true;
    }

    public static boolean readLogInformationFile() {
        boolean retValue = false;
        String value = "";
        String readValue = "";
        String dirName = "/mpt";
        String fileName = "agree";
        File file = new File(dirName, fileName);
        BufferedReader in = null;

        if (file.exists() && file.canRead()) {
            try {
                in = new BufferedReader(new FileReader(file), 128);
                while ((readValue = in.readLine()) != null && !readValue.isEmpty() ) {
                    value = readValue;
                }
                Log.d(TAG, "value : " + value);
                String[] values = value.split(", ");
                if (values[1] != null && !values[1].isEmpty()) {
                    value = values[1];
                }
                if ("Y".equals(value)) {
                    retValue = true;
                }
            } catch (IOException e) {
                Log.w(TAG, "IOException : " + e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    Log.w(TAG, "IOException : " + e);
                }
            }
        } else {
            Log.w(TAG, "[Utils] readToFile() : value =" + value);
        }

        return retValue;
    }

    public static void writeLogInformationFile(boolean status) {
        BufferedWriter bw = null;
        String dirName = "/mpt";
        String fileName = "agree";
        File file = new File(dirName, fileName);
        Log.d(TAG, "writeLogInformationFile status = " + status);

        try {
            bw = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));
            long current = System.currentTimeMillis();
            file.setReadable(true, false); // everyone can read this file
            if (status) {
                bw.append(current + ", Y\n");
            } else {
                bw.append(current + ", N\n");
            }
            bw.flush();
        } catch (Exception e) {
            Log.e(TAG, "failed agree file writing : " + e.toString());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "failed agree file writing : " + e.toString());
            }
        }
    }

    public static boolean isSBMUserBuild() {
        return ("SBM".equals(Config.getOperator()) && "user".equals(Build.TYPE));
    }

    private static boolean isSupportUsbTethering(ConnectivityManager cm, Context context) {
       boolean usbTetherAvailable = false;
       if ((Config.getCountry().equals("US") && (Config.getOperator().equals(Config.TMO) || Config.getOperator().equals(Config.MPCS)))
            || Config.getOperator().equals(Config.VZW)
            || Config.getOperator().equals(Config.SPRINT)
            || Config.getOperator().equals(Config.BM)
            || !UsbSettingsControl.isNotSupplyUSBTethering(context)) {
            usbTetherAvailable = cm.getTetherableUsbRegexs().length != 0;
       }
       if ((Config.getOperator().equals(Config.SPRINT) || Config.getOperator().equals(Config.BM)) && !getChameleonUsbTetheringMenuEnabled()) {
           usbTetherAvailable = false;
       }
       if ("w3c_vzw".equals(SystemProperties.get("ro.product.name")) || "w5c_vzw".equals(SystemProperties.get("ro.product.name"))) {
           usbTetherAvailable = false;
       }
       return usbTetherAvailable;
    }
    private static boolean isSupportBluetoothTethering() {
       boolean bluetoothTetherAvailable = false;
       String ispan = SystemProperties.get("bluetooth.pan", "false");
       if ("true".equals(ispan)) {
            bluetoothTetherAvailable = true;
       }
       //*s LGBT_TMUS_BT_TETHERING_DISABLE, [hyuntae0.kim@lge.com 2013.06.18]
       if ("US".equals(Config.getCountry()) && ("TMO".equals(Config.getOperator()) || "MPCS".equals(Config.getOperator()))) {
           bluetoothTetherAvailable = false;
       }
       return bluetoothTetherAvailable;
    }
    private static boolean isSupportWifiTethering(Context context) {
       boolean wifiTetherAvailable = true;
       if (("ATT".equals(Config.getOperator())) && (SystemProperties.getInt("wlan.lge.atthotspot", 0) == 1)) {
          wifiTetherAvailable = false;
       } else if ("TRF".equals(Config.getOperator()) || "AIO".equals(Config.getOperator()) || "CRK".equals(SystemProperties.get("ro.build.target_operator"))) {
          wifiTetherAvailable = false;
       } else if ("VZW".equals(Config.getOperator())) {
         if ((android.provider.Settings.System.getInt(context.getContentResolver(), SettingsConstants.System.VZW_HIDDEN_FEATURE_WIFI, 0) == 1)) {
             wifiTetherAvailable = false;
         }
       }
      return wifiTetherAvailable;
    }
    public static boolean isSupportTethering(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!cm.isTetheringSupported() || isSBMUserBuild()) {
           return false;
        }
        if (!isSupportUsbTethering(cm, context) && !isSupportBluetoothTethering() && !isSupportWifiTethering(context)) {
            return false;
        }
        return true;
    }

    public static boolean isSupportQslide() {
        return com.lge.config.ConfigBuildFlags.CAPP_QWINDOW;
    }
    
    public static boolean isCarHomeSupport(Context context) {
        return checkPackage(context, "com.lge.carhome");
    }

    public static boolean isSupportAccountMenu_VZW() {
        return false;
    }

    public static boolean isSupportVPN(Context context) {
        boolean isVPN = false;
        PackageInfo pi = null;
        if (context != null) {
            try {
                pi = context.getPackageManager().getPackageInfo(IPSEC_SERVICE_PACKAGE_NAME, 0);
                isVPN = true;
            } catch (NameNotFoundException e) {
                isVPN = false;
            }
        }
        return isVPN;
    }

    public static boolean isAirplaneModeOn(Context context) {
    return android.provider.Settings.Global.getInt(context.getContentResolver(),
            android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static boolean isDisneyModel(Context context) {
        return Config.getOperator().equals("DCM")
                && Config.getFWConfigBool(context,
                        com.lge.R.bool.config_using_disney_cover,
                        "com.lge.R.bool.config_using_disney_cover");
    }

    public static boolean isChina() {
        if ("CMCC".equals(Config.getOperator())
                || "CMO".equals(Config.getOperator())) {
            return true;
        } else if ("CTC".equals(Config.getOperator())
                || "CTO".equals(Config.getOperator())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCheckSensor(Context context) {
        SensorManager sensorManager = (SensorManager)context
                .getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        boolean ret = false;

        for (int i = 0; i < sensorList.size(); i++) {
            if (sensorList.get(i).getType() == Sensor.TYPE_ACCELEROMETER) {
                ret = true;
            } else if (sensorList.get(i).getType() == LGE_GESTURE_SENSOR_FACING) {
                ret = true;
            } else if (sensorList.get(i).getType() == LGE_GESTURE_SENSOR_TILT) {
                ret = true;
            }
        }
        return ret;
    }

    public static boolean isSetLockSecured(Context context) {
        boolean value = false;
        mLockPatternUtils = new LockPatternUtils(context);
        if (mLockPatternUtils.isSecure()) {
            value = true;
        }
        return value;
    }

    public static int isSupportCredentialStorageType(Context context) {
        UserManager um = (UserManager)context.getSystemService(Context.USER_SERVICE);
        KeyStore mKeyStore = KeyStore.getInstance();
        if (UserHandle.myUserId() == UserHandle.USER_OWNER
            && !um.hasUserRestriction(UserManager.DISALLOW_CONFIG_CREDENTIALS)) {
            final int storageSummaryRes =
                    mKeyStore.isHardwareBacked() ? R.string.credential_storage_type_hardware
                            : R.string.credential_storage_type_software;
            return storageSummaryRes;
        }
        return 0;
    }
}
