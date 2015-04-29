package com.android.settings.hotkey;

import com.android.settings.Utils;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.util.Log;
import java.util.HashSet;
import android.os.SystemProperties;
import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

public class PackageIntentReceiver extends BroadcastReceiver {

    private static final String TAG = "PackageIntentReceiver";
    public static final boolean DEBUG = false;

    public static final String HOTKEY_SHORT_PACKAGE = "hotkey_short_package";
    public static final String HOTKEY_SHORT_CLASS = "hotkey_short_class";
    public static final String HOTKEY_LONG_PACKAGE = "hotkey_long_package";
    public static final String HOTKEY_LONG_CLASS = "hotkey_long_class";

    private static final String HOTKEY_NONE = "none";

    @Override
    public void onReceive(Context context, Intent i) {
        final String action = i.getAction();

        if (Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action) ||
                Intent.ACTION_PACKAGE_REPLACED.equals(action)) {

            if (context == null) {
                Log.d(TAG, "Context is null ");
                return;
            }

            if (!Utils.supportHotkey(context)) {
                Log.d(TAG, "Not supported Hotkey customization function ");
                return;
            }

            String DEFAULT_PACKAGE = context.getResources().getStringArray(R.array.default_apps)[0];
            String DEFAULT_CLASS = context.getResources().getStringArray(R.array.default_apps)[1];

            String deletePackageName = i.getData().getSchemeSpecificPart();
            String hotkey_short_package = Settings.System.getString(context.getContentResolver(),
                    HOTKEY_SHORT_PACKAGE);
            String hotkey_long_package = Settings.System.getString(context.getContentResolver(),
                    HOTKEY_LONG_PACKAGE);

            if (hotkey_short_package == null || hotkey_long_package == null) {
                return;
            }
            if (hotkey_short_package.equals(deletePackageName)) {
                Settings.System.putString(context.getContentResolver(), HOTKEY_SHORT_PACKAGE,
                        DEFAULT_PACKAGE);
                Settings.System.putString(context.getContentResolver(), HOTKEY_SHORT_CLASS,
                        DEFAULT_CLASS);
            }

            if (hotkey_long_package.equals(deletePackageName)) {
                Settings.System.putString(context.getContentResolver(), HOTKEY_LONG_PACKAGE,
                        HOTKEY_NONE);
                Settings.System.putString(context.getContentResolver(), HOTKEY_LONG_CLASS,
                        HOTKEY_NONE);
            }
            Log.i(TAG, "delete Package Name : " + i.getData().getSchemeSpecificPart());

            //            PackageManager pm = ctxt.getPackageManager();
            //            ContentResolver cr = ctxt.getContentResolver();
            //
            //            for (int id = 1; id <= ThemeSettingInfo.SHORTCUTLIST_MAX; id++) {
            //                String pn = LockSettingsProvider.Shortcut.getPackage(cr, id);
            //                Log.d("LockScreenSettings", "Package name=" + pn + ", id=" + id);
            //
            //                if (pn != null && !pn.isEmpty()) {
            //                    String lb = LockSettingsProvider.Shortcut.getLabel(cr, id);
            //                    String cn = LockSettingsProvider.Shortcut.getClass(cr, id);
            //                    Log.d("LockScreenSettings", "label=" + lb);
            //                    Log.d("LockScreenSettings", "class name=" + cn);
            //
            //                    try {
            //                        lb = (String) pm.getActivityInfo(new ComponentName(pn, cn),
            //                                PackageManager.GET_ACTIVITIES).loadLabel(pm);
            //                        // LockSettingsProvider.Shortcut.add(cr, id, lb, pn,
            //                        // cn);
            //                    } catch (NameNotFoundException e) {
            //                        LockSettingsProvider.Shortcut.add(cr, id, "", "", "");
            //                    }
            //                }
            //            }
            if ("ATT".equals(Config.getOperator())
                    && checkPackageNameForVapp(deletePackageName, context)
                    && checkPackage(deletePackageName, context)
                    && (Intent.ACTION_PACKAGE_REMOVED.equals(action))) {
                try {
                    if ((context.getPackageManager().getApplicationInfo(deletePackageName, 0).flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        if ((context.getPackageManager().getApplicationInfo(deletePackageName, 0).flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                            Log.i(TAG, "UPGrade");
                        } else {
                            Log.i(TAG, "Will be disable : " + deletePackageName);
                            context.getPackageManager().setApplicationEnabledSetting(
                                    deletePackageName,
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, 0);
                        }

                    }
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    private boolean checkPackageNameForVapp(String packageName, Context context) {
        if (packageName == null) {
            return false;
        }
        HashSet<String> vapps = Utils.getVApps(context);
        if (vapps == null || vapps.size() == 0) {
            return false;
        }
        return vapps.contains(packageName);
    }

    private boolean checkPackage(String packageName, Context context) {
        //Search PackageManager
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo(packageName, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }

    }

}
