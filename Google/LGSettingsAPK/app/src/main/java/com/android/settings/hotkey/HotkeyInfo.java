package com.android.settings.hotkey;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.List;

public class HotkeyInfo {

    public static final String TAG = "HotkeyInfo";

    public static final String HOTKEY_SHORT_PACKAGE = "hotkey_short_package";
    public static final String HOTKEY_SHORT_CLASS = "hotkey_short_class";
    public static final String HOTKEY_LONG_PACKAGE = "hotkey_long_package";
    public static final String HOTKEY_LONG_CLASS = "hotkey_long_class";

  public static final String DEFAULT_PACKAGE = "com.lge.qmemoplus";
  public static final String DEFAULT_CLASS = "com.lge.qmemoplus.quickmode.QuickModeActivity";

    public static final String HOTKEY_NONE = "none";
    public static final String HOTKEY_SIM_SWITCH = "sim_switch";
    public static final String HOTKEY_ROTATE_SWITCH = "rotate_switch";

    public static final String ACTION_HOTKEY = "com.lge.settings.HOTKEY_SETTINGS";
    public static final String HOTKEY_CLEAR = "not_set";

    public static final String SHORTCUT_KEY_STATUS = "shortcut_key_status";

    public static final int HOTKEY_NOT_FOUND_RESID = -1;

    private List<DummyPKGInfo> mDummy_PKGS;

    private Context mContext;
    private ContentResolver mContentResolver;
    private String mQB_backup_short_cls = null;
    private String mQB_backup_short_pkg = null;
    private String mQB_backup_long_cls = null;
    private String mQB_backup_long_pkg = null;

    public HotkeyInfo(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mDummy_PKGS = new ArrayList<DummyPKGInfo>();
        setDummyPKGInfo();
    }

    private void setDummyPKGInfo() {
        try {
            PackageManager mPm = mContext.getPackageManager();
            //PackageInfo pInfo = mPm.getPackageInfo(HotkeyInfo.DEFAULT_PACKAGE, 0);
            if (null != mContext) {
                /* add : package name | package subject | package icon */
                mDummy_PKGS.add(new DummyPKGInfo(HOTKEY_SIM_SWITCH,
                        R.string.quickbutton_simcard_switch,
                        mPm.getApplicationIcon(HotkeyInfo.DEFAULT_PACKAGE)));
                mDummy_PKGS.add(new DummyPKGInfo(HOTKEY_ROTATE_SWITCH,
                        R.string.quick_button_rotate_screen,
                        mPm.getApplicationIcon(HotkeyInfo.DEFAULT_PACKAGE)));
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "NameNotFoundException");
        }

    }

    public boolean isNormalPackage(String pkg) {
        int length = mDummy_PKGS.size();
        for (int i = 0; i < length; i++) {
            if (true == mDummy_PKGS.get(i).getPKG().equals(pkg)) {
                Log.i(TAG, "[isNormalPackage] Matching dummy PKG : " + pkg);
                return false;
            }
        }
        return true;
    }

    public String getDBHotKeyShortPKG() {
        return Settings.System.getString(mContentResolver, HOTKEY_SHORT_PACKAGE);
    }

    public String getDBHotKeyShortClass() {
        return Settings.System.getString(mContentResolver, HOTKEY_SHORT_CLASS);
    }

    public String getDBHotKeyLongPKG() {
        return Settings.System.getString(mContentResolver, HOTKEY_LONG_PACKAGE);
    }

    public String getDBHotKeyLongClass() {
        return Settings.System.getString(mContentResolver, HOTKEY_LONG_CLASS);
    }

    public void setDBHotKeyShortPKG(String pkg) {
        if (null != pkg) {
        }
        else {
            pkg = HOTKEY_SHORT_PACKAGE;
        }
        Settings.System.putString(mContentResolver, HOTKEY_SHORT_PACKAGE, pkg);

    }

    public void setDBHotKeyShortClass(String cls) {
        if (null != cls) {
        }
        else {
            cls = HOTKEY_SHORT_CLASS;
        }
        Settings.System.putString(mContentResolver, HOTKEY_SHORT_CLASS, cls);

    }

    public void setDBHotKeyLongPKG(String pkg) {
        if (null != pkg) {
        }
        else {
            pkg = HOTKEY_LONG_PACKAGE;
        }
        Settings.System.putString(mContentResolver, HOTKEY_LONG_PACKAGE, pkg);

    }

    public void setDBHotKeyLongClass(String cls) {
        if (null != cls) {
        }
        else {
            cls = HOTKEY_LONG_CLASS;
        }
        Settings.System.putString(mContentResolver, HOTKEY_LONG_CLASS, cls);

    }

    public int getDummyPKGResID(String pkg) {
        int length = mDummy_PKGS.size();
        for (int i = 0; i < length; i++) {
            if (mDummy_PKGS.get(i).getPKG().equals(pkg)) {
                return mDummy_PKGS.get(i).getPKG_resID();
            }
        }
        return HOTKEY_NOT_FOUND_RESID;
    }

    public boolean isSupportAccelSensor() {
        return mContext.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
    }

    public boolean isNotSingleSimModel() {
        return Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled();
    }

    public void deleteQButtonInfo() {
        setDBHotKeyShortClass(HOTKEY_CLEAR);
        setDBHotKeyShortPKG(HOTKEY_CLEAR);
        setDBHotKeyLongClass(HOTKEY_CLEAR);
        setDBHotKeyLongPKG(HOTKEY_CLEAR);
    }

    public void backupQButtonInfo() {
        mQB_backup_short_cls = getDBHotKeyShortClass();
        mQB_backup_short_pkg = getDBHotKeyShortPKG();
        mQB_backup_long_cls = getDBHotKeyLongClass();
        mQB_backup_long_pkg = getDBHotKeyLongPKG();
    }

    public void revertQButtonClear() {
        setDBHotKeyShortClass(mQB_backup_short_cls);
        setDBHotKeyShortPKG(mQB_backup_short_pkg);
        setDBHotKeyLongClass(mQB_backup_long_cls);
        setDBHotKeyLongPKG(mQB_backup_long_pkg);
    }

    public void hasActionReceive(boolean isReceive) {
        int component_enable_state = isReceive ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        PackageManager pm = mContext.getPackageManager();
        pm.setApplicationEnabledSetting("com.android.settings",
                component_enable_state, PackageManager.DONT_KILL_APP);
    }

    class DummyPKGInfo {
        private String pkg = null;
        private int pkgname_resID = 0;

        public DummyPKGInfo(String _pkg, int _pkgname_resID, Drawable _icon) {
            // TODO Auto-generated constructor stub
            pkg = _pkg;
            pkgname_resID = _pkgname_resID;
        }

        public String getPKG() {
            if (null != pkg) {
                return pkg;
            }
            return HOTKEY_NONE;
        }

        public int getPKG_resID() {
            return pkgname_resID;
        }
    }

    public int getDBShortcutKeyStatus() {
        try {
            return Settings.System.getInt(mContentResolver, SHORTCUT_KEY_STATUS);
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "SettingNotFoundException - getDBShortcutKeyStatus()");
            setDBShortcutKeyStatus(1);
            return 1;
        }
    }

    public void setDBShortcutKeyStatus(int state) {
        Settings.System.putInt(mContentResolver, SHORTCUT_KEY_STATUS, state);
    }
}
