package com.android.settings.lge;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;

import com.android.settings.Utils;
import com.android.settings.remote.RemoteFragmentManager;
import com.android.settings.remote.RemoteFragmentManager.RemoteContextInfo;

import java.util.Set;

public class HideNavigationAppsReceiver extends BroadcastReceiver {
    private static final String TAG = "HideNavigationAppsReceiver";
    private Context mContext;
    private boolean isExistDB = false;
    private static final char ENABLED_HIDE_NAVIGATION_SEPARATOR = ':';
    private final static SimpleStringSplitter sStringColonSplitter =
            new SimpleStringSplitter(ENABLED_HIDE_NAVIGATION_SEPARATOR);

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        String action = intent.getAction();
        String pkgName = intent.getData().getSchemeSpecificPart();
        final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

        Log.d(TAG, "Receive package name : " + pkgName + ", replacing=" + replacing + ", action="
                + action);
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_CHANGED.equals(action) ||
                Intent.ACTION_PACKAGE_ADDED.equals(action)
                || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            Log.d(TAG, "replacing : " + replacing);
            if (!replacing) {
                deleteHideNavigationApp(pkgName);
            }
            if (Utils.supportRemoteFragment(mContext) == true) {
                checkRemoteClass(pkgName);
            }
        }
    }

    private void checkRemoteClass(String pkgName) {
        Set<String> keySet = RemoteFragmentManager.sContextMap.keySet();

        Object[] keyArray = keySet.toArray();

        for (int i = 0; i < keyArray.length; i++) {
            String key = (String)keyArray[i];
            Log.d(TAG, "packageName_" + i + " = " + key);
            RemoteContextInfo contextInfo = RemoteFragmentManager.sContextMap.get(key);
            if (pkgName.equals(contextInfo.mContext.getPackageName()) == true) {
                Log.d(TAG, "force stop settings because of related apps is upgraded.");
                ActivityManager am = (ActivityManager)mContext.getSystemService(
                        Context.ACTIVITY_SERVICE);
                am.forceStopPackage("com.android.settings");
                return;
            }
        }
    }

    private void deleteHideNavigationApp(String mPackageMame) {
        String enableHideApp = readDB();
        if (enableHideApp == null) {
            enableHideApp = "";
        }

        Log.d(TAG, "Delete mPackageMame : " + mPackageMame);
        SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enableHideApp);

        StringBuilder enabledHideNavigationBuilder = new StringBuilder(enableHideApp);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            if (componentNameString.equals(mPackageMame)) {
                StringBuilder componentNameStringBuilder = new StringBuilder(componentNameString);
                componentNameStringBuilder.append(ENABLED_HIDE_NAVIGATION_SEPARATOR);
                enabledHideNavigationBuilder
                        .delete(enabledHideNavigationBuilder.indexOf(componentNameStringBuilder
                                .toString()),
                                enabledHideNavigationBuilder.indexOf(componentNameStringBuilder
                                        .toString())
                                        + componentNameStringBuilder.toString().length());
                isExistDB = true;
            }
        }
        if (isExistDB) {
            writeDB(enabledHideNavigationBuilder.toString());
            isExistDB = false;
        }
    }

    private String readDB() {
        return Settings.System.getString(mContext.getContentResolver(),
                "enable_hide_navigation_apps");
    }

    private void writeDB(String str) {
        Settings.System
                .putString(mContext.getContentResolver(), "enable_hide_navigation_apps", str);
        Log.d(TAG,
                "write HideNavigation app DB : "
                        + Settings.System.getString(mContext.getContentResolver(),
                                "enable_hide_navigation_apps"));
    }

}
