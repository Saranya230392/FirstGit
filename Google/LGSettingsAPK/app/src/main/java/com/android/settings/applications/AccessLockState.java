/**
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.settings.applications;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import android.util.Log;
import android.util.SparseArray;
import com.android.settings.R;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AccessLockState {
    static final String TAG = "AccessLockState";
    static final boolean DEBUG = true;
    static final int ALL_APPOPS = 0;
    static final int LOCATION = 1;
    static final int PERSONAL = 2;
    static final int MESSAGING = 3;
    static final int MEDIA = 4;
    static final int DEVICE = 5;

    public static final int OP_WIFI_CHANGE = 43;
    public static final int OP_BLUETOOTH_CHANGE = 44;
    public static final int OP_DATA_CONNECT_CHANGE = 45;
    public static final int OP_SEND_MMS = 46;
    public static final int OP_READ_MMS = 47;
    public static final int OP_WRITE_MMS = 48;
    //public static final int OP_DELETE_MMS = 49;
    //public static final int OP_DELETE_SMS = 50;
    public static final int OP_BOOT_COMPLETED = 49;
    public static final int OP_DELETE_CONTACTS = 50;
    public static final int OP_DELETE_CALL_LOG = 51;
    public static final int NUM_OP = 52;

    final Context mContext;
    final AppOpsManager mAppOps;
    final PackageManager mPm;
    final CharSequence[] mOpSummaries;
    final CharSequence[] mOpLabels;

    List<AppOpEntry> mApps;

    public AccessLockState(Context context) {
        mContext = context;
        mAppOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        mPm = context.getPackageManager();
        mOpSummaries = context.getResources().getTextArray(R.array.access_lock_summaries);
        mOpLabels = context.getResources().getTextArray(R.array.access_lock_labels);
    }

    public static class OpsTemplate implements Parcelable {
        public final int[] ops;
        public final boolean[] showPerms;

        public OpsTemplate(int[] _ops, boolean[] _showPerms) {
            ops = _ops;
            showPerms = _showPerms;
        }

        OpsTemplate(Parcel src) {
            ops = src.createIntArray();
            showPerms = src.createBooleanArray();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeIntArray(ops);
            dest.writeBooleanArray(showPerms);
        }

        public static final Creator<OpsTemplate> CREATOR = new Creator<OpsTemplate>() {
            @Override
            public OpsTemplate createFromParcel(Parcel source) {
                return new OpsTemplate(source);
            }

            @Override
            public OpsTemplate[] newArray(int size) {
                return new OpsTemplate[size];
            }
        };
    }

    public static OpsTemplate setTemplate(int type) {
        switch (type) {
        case ALL_APPOPS:
            return getAllTemplates();
        case LOCATION:
            return isLEV1TempCase() == false ?
                    AccessLockState.LOCATION_TEMPLATE : AccessLockState.LOCATION_TEMPLATE_LEV1;
        case PERSONAL:
            return isLEV1TempCase() == false ?
                    AccessLockState.PERSONAL_TEMPLATE : AccessLockState.PERSONAL_TEMPLATE_LEV1;
        case MESSAGING:
            return isLEV1TempCase() == false ?
                    AccessLockState.MESSAGING_TEMPLATE : AccessLockState.MESSAGING_TEMPLATE_LEV1;
        case MEDIA:
            return isLEV1TempCase() == false ?
                    AccessLockState.MEDIA_TEMPLATE : AccessLockState.MEDIA_TEMPLATE_LEV1;
        case DEVICE:
            return isLEV1TempCase() == false ?
                    AccessLockState.DEVICE_TEMPLATE : AccessLockState.DEVICE_TEMPLATE_LEV1;
        default:
            break;
        }
        return null;
    }

    public static final OpsTemplate LOCATION_TEMPLATE = new OpsTemplate(
            new int[] { AppOpsManager.OP_COARSE_LOCATION,
                    AppOpsManager.OP_FINE_LOCATION,
                    AppOpsManager.OP_GPS,
                    AppOpsManager.OP_WIFI_SCAN,
                    AppOpsManager.OP_WIFI_CHANGE,
                    AppOpsManager.OP_BLUETOOTH_CHANGE,
                    AppOpsManager.OP_NEIGHBORING_CELLS,
                    AppOpsManager.OP_MONITOR_LOCATION,
                    AppOpsManager.OP_MONITOR_HIGH_POWER_LOCATION },
            new boolean[] { true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true }
            );

    public static final OpsTemplate LOCATION_TEMPLATE_LEV1 = new OpsTemplate(
            new int[] { AppOpsManager.OP_COARSE_LOCATION,
                    AppOpsManager.OP_FINE_LOCATION,
                    AppOpsManager.OP_GPS
            },
            new boolean[] { true,
                    true,
                    true }
            );

    public static final OpsTemplate PERSONAL_TEMPLATE = new OpsTemplate(
            new int[] { AppOpsManager.OP_READ_CONTACTS,
                    AppOpsManager.OP_WRITE_CONTACTS,
                    AppOpsManager.OP_READ_CALL_LOG,
                    AppOpsManager.OP_WRITE_CALL_LOG,
                    AppOpsManager.OP_READ_CALENDAR,
                    AppOpsManager.OP_WRITE_CALENDAR,
                    AppOpsManager.OP_READ_CLIPBOARD,
                    AppOpsManager.OP_WRITE_CLIPBOARD,
                    AppOpsManager.OP_DELETE_CONTACTS,
                    AppOpsManager.OP_DELETE_CALL_LOG
                    },
            new boolean[] { true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true
                    }
            );

    public static final OpsTemplate PERSONAL_TEMPLATE_LEV1 = new OpsTemplate(
            new int[] { AppOpsManager.OP_READ_CONTACTS,
                    AppOpsManager.OP_READ_CALL_LOG },
            new boolean[] { true,
                    true }
            );

    public static final OpsTemplate MESSAGING_TEMPLATE = new OpsTemplate(
            new int[] { AppOpsManager.OP_READ_SMS,
                    AppOpsManager.OP_RECEIVE_SMS,
                    AppOpsManager.OP_RECEIVE_EMERGECY_SMS,
                    AppOpsManager.OP_RECEIVE_MMS,
                    AppOpsManager.OP_RECEIVE_WAP_PUSH,
                    AppOpsManager.OP_WRITE_SMS,
                    AppOpsManager.OP_SEND_SMS,
                    AppOpsManager.OP_READ_ICC_SMS,
                    AppOpsManager.OP_WRITE_ICC_SMS,
                    AppOpsManager.OP_SEND_MMS,
                    AppOpsManager.OP_READ_MMS,
                    AppOpsManager.OP_WRITE_MMS,
                    //OP_DELETE_MMS,
                    //OP_DELETE_SMS
                    },
            new boolean[] { true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true,
                    false,
                    false,
                    true,
                    false,
                    false,
                    //true,
                    //true
                    }
            );

    public static final OpsTemplate MESSAGING_TEMPLATE_LEV1 = new OpsTemplate(
            new int[] { AppOpsManager.OP_READ_SMS,
                    AppOpsManager.OP_SEND_SMS,
                    AppOpsManager.OP_SEND_MMS },
            new boolean[] { true,
                    true,
                    true }
            );

    public static final OpsTemplate MEDIA_TEMPLATE = new OpsTemplate(
            new int[] { AppOpsManager.OP_VIBRATE,
                    AppOpsManager.OP_CAMERA,
                    AppOpsManager.OP_RECORD_AUDIO,
                    AppOpsManager.OP_PLAY_AUDIO,
                    AppOpsManager.OP_TAKE_MEDIA_BUTTONS,
                    AppOpsManager.OP_TAKE_AUDIO_FOCUS,
                    AppOpsManager.OP_AUDIO_MASTER_VOLUME,
                    AppOpsManager.OP_AUDIO_VOICE_VOLUME,
                    AppOpsManager.OP_AUDIO_RING_VOLUME,
                    AppOpsManager.OP_AUDIO_MEDIA_VOLUME,
                    AppOpsManager.OP_AUDIO_ALARM_VOLUME,
                    AppOpsManager.OP_AUDIO_NOTIFICATION_VOLUME,
                    AppOpsManager.OP_AUDIO_BLUETOOTH_VOLUME, },
            new boolean[] { true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true }
            );

    public static final OpsTemplate MEDIA_TEMPLATE_LEV1 = new OpsTemplate(
            new int[] {
                    AppOpsManager.OP_CAMERA,
                    AppOpsManager.OP_RECORD_AUDIO },
            new boolean[] {
                    true,
                    true }
            );

    public static final OpsTemplate DEVICE_TEMPLATE = new OpsTemplate(
            new int[] { AppOpsManager.OP_POST_NOTIFICATION,
                    AppOpsManager.OP_ACCESS_NOTIFICATIONS,
                    AppOpsManager.OP_CALL_PHONE,
                    AppOpsManager.OP_WRITE_SETTINGS,
                    AppOpsManager.OP_SYSTEM_ALERT_WINDOW,
                    AppOpsManager.OP_WAKE_LOCK,
                    AppOpsManager.OP_DATA_CONNECT_CHANGE,
                    AppOpsManager.OP_BOOT_COMPLETED },
            new boolean[] { true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
            }
            );

    public static final OpsTemplate DEVICE_TEMPLATE_LEV1 = new OpsTemplate(
            new int[] {
                    AppOpsManager.OP_CALL_PHONE },
            new boolean[] {
                    true
            }
            );

    public static final OpsTemplate[] ALL_TEMPLATES = new OpsTemplate[] {
            setAllTemplates(LOCATION),
            setAllTemplates(PERSONAL),
            setAllTemplates(MESSAGING),
            setAllTemplates(MEDIA),
            setAllTemplates(DEVICE)
    };

    public static int getSize() {
        return setAllTemplates(LOCATION).ops.length +
                setAllTemplates(PERSONAL).ops.length +
                setAllTemplates(MESSAGING).ops.length +
                setAllTemplates(MEDIA).ops.length +
                setAllTemplates(DEVICE).ops.length;
    }

    public static OpsTemplate getAllTemplates() {
        final int size = getSize();
        int[] ops = new int[size];
        boolean[] showPerms = new boolean[size];

        int a = 0;
        for (OpsTemplate tpl : ALL_TEMPLATES) {
            int length = tpl.ops.length;
            for (int b = 0; b < length; b++) {
                ops[a] = tpl.ops[b];
                showPerms[a] = tpl.showPerms[b];
                a++;
            }
        }
        OpsTemplate allTemplate = new OpsTemplate(ops, showPerms);
        return allTemplate;
    }

    private static OpsTemplate setAllTemplates(int type) {
        switch (type) {
        case LOCATION:
            return isLEV1TempCase() == false ?
                    LOCATION_TEMPLATE : LOCATION_TEMPLATE_LEV1;
        case PERSONAL:
            return isLEV1TempCase() == false ?
                    PERSONAL_TEMPLATE : PERSONAL_TEMPLATE_LEV1;
        case MESSAGING:
            return isLEV1TempCase() == false ?
                    MESSAGING_TEMPLATE : MESSAGING_TEMPLATE_LEV1;
        case MEDIA:
            return isLEV1TempCase() == false ?
                    MEDIA_TEMPLATE : MEDIA_TEMPLATE_LEV1;
        case DEVICE:
            return isLEV1TempCase() == false ?
                    DEVICE_TEMPLATE : DEVICE_TEMPLATE_LEV1;
        default:
            break;
        }
        return null;
    }

    public static final boolean isLEV1TempCase() {
        return false;
    }

    /**
     * This class holds the per-item data in our Loader.
     */
    public static class AppEntry {
        private final AccessLockState mState;
        private final ApplicationInfo mInfo;
        private final File mApkFile;
        private final SparseArray<AppOpsManager.OpEntry> mOps = new SparseArray<AppOpsManager.OpEntry>();
        private final SparseArray<AppOpEntry> mOpSwitches = new SparseArray<AppOpEntry>();
        private String mLabel;
        private Drawable mIcon;
        private boolean mMounted;

        public AppEntry(AccessLockState state, ApplicationInfo info) {
            mState = state;
            mInfo = info;
            mApkFile = new File(info.sourceDir);
        }

        public void addOp(AppOpEntry entry, AppOpsManager.OpEntry op) {
            mOps.put(op.getOp(), op);
            mOpSwitches.put(AppOpsManager.opToSwitch(op.getOp()), entry);
        }

        public boolean hasOp(int op) {
            return mOps.indexOfKey(op) >= 0;
        }

        public AppOpEntry getOpSwitch(int op) {
            return mOpSwitches.get(AppOpsManager.opToSwitch(op));
        }

        public ApplicationInfo getApplicationInfo() {
            return mInfo;
        }

        public String getLabel() {
            return mLabel;
        }

        public Drawable getIcon() {
            if (mIcon == null) {
                if (mApkFile.exists()) {
                    mIcon = mInfo.loadIcon(mState.mPm);
                    return mIcon;
                } else {
                    mMounted = false;
                }
            } else if (!mMounted) {
                // If the app wasn't mounted but is now mounted, reload
                // its icon.
                if (mApkFile.exists()) {
                    mMounted = true;
                    mIcon = mInfo.loadIcon(mState.mPm);
                    return mIcon;
                }
            } else {
                return mIcon;
            }

            return mState.mContext.getResources().getDrawable(
                    android.R.drawable.sym_def_app_icon);
        }

        @Override
        public String toString() {
            return mLabel;
        }

        void loadLabel(Context context) {
            if (mLabel == null || !mMounted) {
                if (!mApkFile.exists()) {
                    mMounted = false;
                    mLabel = mInfo.packageName;
                } else {
                    mMounted = true;
                    CharSequence label = mInfo.loadLabel(context.getPackageManager());
                    mLabel = label != null ? label.toString() : mInfo.packageName;
                }
            }
        }
    }

    /**
     * This class holds the per-item data in our Loader.
     */
    public static class AppOpEntry {
        private final AppOpsManager.PackageOps mPkgOps;
        private final ArrayList<AppOpsManager.OpEntry> mOps = new ArrayList<AppOpsManager.OpEntry>();
        private final ArrayList<AppOpsManager.OpEntry> mSwitchOps = new ArrayList<AppOpsManager.OpEntry>();
        private final AppEntry mApp;
        private final int mSwitchOrder;

        public AppOpEntry(AppOpsManager.PackageOps pkg, AppOpsManager.OpEntry op, AppEntry app,
                int switchOrder) {
            mPkgOps = pkg;
            mApp = app;
            mSwitchOrder = switchOrder;
            mApp.addOp(this, op);
            mOps.add(op);
            mSwitchOps.add(op);
        }

        private static void addOp(ArrayList<AppOpsManager.OpEntry> list, AppOpsManager.OpEntry op) {
            for (int i = 0; i < list.size(); i++) {
                AppOpsManager.OpEntry pos = list.get(i);
                if (pos.isRunning() != op.isRunning()) {
                    if (op.isRunning()) {
                        list.add(i, op);
                        return;
                    }
                    continue;
                }
                if (pos.getTime() < op.getTime()) {
                    list.add(i, op);
                    return;
                }
            }
            list.add(op);
        }

        public void addOp(AppOpsManager.OpEntry op) {
            mApp.addOp(this, op);
            addOp(mOps, op);
            if (mApp.getOpSwitch(AppOpsManager.opToSwitch(op.getOp())) == null) {
                addOp(mSwitchOps, op);
            }
        }

        public AppEntry getAppEntry() {
            return mApp;
        }

        public int getSwitchOrder() {
            return mSwitchOrder;
        }

        public AppOpsManager.PackageOps getPackageOps() {
            return mPkgOps;
        }

        public int getNumOpEntry() {
            return mOps.size();
        }

        public AppOpsManager.OpEntry getOpEntry(int pos) {
            return mOps.get(pos);
        }

        private CharSequence getCombinedText(ArrayList<AppOpsManager.OpEntry> ops,
                CharSequence[] items) {
            if (ops.size() == 1) {
                try {
                    return items[ops.get(0).getOp()];
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, TAG + e.getLocalizedMessage());
                    return "index Out";
                }
            } else {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < ops.size(); i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    try {
                        builder.append(items[ops.get(i).getOp()]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        /*
                        if (OP_SEND_MMS == i) {
                            //builder.append(items[ops.get(i).getOp()]);
                        }
                        */
                        Log.e(TAG, TAG + e.getLocalizedMessage());
                    }
                }
                return builder.toString();
            }
        }

        public CharSequence getSummaryText(AccessLockState state) {
            return getCombinedText(mOps, state.mOpSummaries);
        }

        public CharSequence getSwitchText(AccessLockState state) {
            if (mSwitchOps.size() > 0) {
                return getCombinedText(mSwitchOps, state.mOpLabels);
            } else {
                return getCombinedText(mOps, state.mOpLabels);
            }
        }

        public CharSequence getTimeText(Resources res, boolean showEmptyText) {
            if (isRunning()) {
                return res.getText(R.string.app_ops_running);
            }
            if (getTime() > 0) {
                return DateUtils.getRelativeTimeSpanString(getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
            }
            return showEmptyText ? res.getText(R.string.app_ops_never_used) : "";
        }

        public boolean isRunning() {
            return mOps.get(0).isRunning();
        }

        public long getTime() {
            return mOps.get(0).getTime();
        }

        @Override
        public String toString() {
            return mApp.getLabel();
        }
    }

    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<AppOpEntry> APP_OP_COMPARATOR = new Comparator<AppOpEntry>() {

        private final Collator mCollator = Collator.getInstance();

        @Override
        public int compare(AppOpEntry object1, AppOpEntry object2) {
            /*
            if (object1.getSwitchOrder() != object2.getSwitchOrder()) {
                return object1.getSwitchOrder() < object2.getSwitchOrder() ? -1 : 1;
            }
            if (object1.isRunning() != object2.isRunning()) {
                // Currently running ops go first.
                return object1.isRunning() ? -1 : 1;
            }
            if (object1.getTime() != object2.getTime()) {
                // More recent times go first.
                return object1.getTime() > object2.getTime() ? -1 : 1;
            }
            */
            return mCollator.compare(object1.getAppEntry().getLabel(),
                    object2.getAppEntry().getLabel());
        }
    };

    private void addOp(List<AppOpEntry> entries, AppOpsManager.PackageOps pkgOps,
            AppEntry appEntry, AppOpsManager.OpEntry opEntry, boolean allowMerge, int switchOrder) {
        if (allowMerge && entries.size() > 0) {
            AppOpEntry last = entries.get(entries.size() - 1);
            if (last.getAppEntry() == appEntry) {
                boolean lastExe = last.getTime() != 0;
                boolean entryExe = opEntry.getTime() != 0;
                if (lastExe == entryExe) {
                    Log.d(TAG, "[addOp]Add op " + opEntry.getOp() +
                            " to package " + pkgOps.getPackageName() + ": append to " + last);
                    last.addOp(opEntry);
                    return;
                }
            }
        }
        AppOpEntry entry = appEntry.getOpSwitch(opEntry.getOp());
        if (entry != null) {
            entry.addOp(opEntry);
            return;
        }
        entry = new AppOpEntry(pkgOps, opEntry, appEntry, switchOrder);
        if (DEBUG) {
            Log.d(TAG, "Add op " + opEntry.getOp() + " to package "
                    + pkgOps.getPackageName() + ": making new " + entry);
        }
        entries.add(entry);
    }

    public List<AppOpEntry> buildState(OpsTemplate tpl) {
        return buildState(tpl, 0, null);
    }

    private AppEntry getAppEntry(final Context context, final HashMap<String, AppEntry> appEntries,
            final String packageName, ApplicationInfo appInfo) {
        AppEntry appEntry = appEntries.get(packageName);
        if (appEntry == null) {
            if (appInfo == null) {
                try {
                    appInfo = mPm.getApplicationInfo(packageName,
                            PackageManager.GET_DISABLED_COMPONENTS
                                    | PackageManager.GET_UNINSTALLED_PACKAGES);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "Unable to find info for package " + packageName);
                    return null;
                }
            }
            appEntry = new AppEntry(this, appInfo);
            appEntry.loadLabel(context);
            appEntries.put(packageName, appEntry);
        }
        return appEntry;
    }

    public boolean isSystemPackage(PackageInfo pi) {

        ApplicationInfo ai;
        try {
            ai = mPm.getApplicationInfo(pi.packageName, 0);
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
            else if ((ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true;
            }
            else {
                Log.i(TAG, "3rd party package : " + pi.packageName + "   flag : " + ai.flags +
                        "      >>>packages dir is : " + ai.publicSourceDir);
                return false;
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "[isSystemPackage] NameNotFoundException");
            return false;
        }
    }

    public List<AppOpEntry> buildState(OpsTemplate tpl, int uid, String packageName) {

        final Context context = mContext;
        final HashMap<String, AppEntry> appEntries = new HashMap<String, AppEntry>();
        final List<AppOpEntry> entries = new ArrayList<AppOpEntry>();
        final ArrayList<String> perms = new ArrayList<String>();
        final ArrayList<Integer> permOps = new ArrayList<Integer>();
        //final int[] opToOrder = new int[AppOpsManager._NUM_OP];
        final int[] opToOrder = new int[AppOpsManager._NUM_OP];
        int length = tpl.ops.length;
        Log.i(TAG, "tpl.ops.length : " + length);
        Log.i(TAG, "------------------------------------------------------");
        for (int i = 0; i < length; i++) {
            try {
                Log.i(TAG, "tpl showPerms : " + tpl.ops[i] + " - " + tpl.showPerms[i] + "     " +
                        "perms : " + AppOpsManager.opToPermission(tpl.ops[i]));
                if (tpl.showPerms[i]) {
                    String perm = AppOpsManager.opToPermission(tpl.ops[i]);
                    //if (perm != null && !perms.contains(perm)) {
                    if (null != perm) {
                        Log.i(TAG, "add perms :" + AppOpsManager.opToPermission(tpl.ops[i]));
                        perms.add(perm);
                        permOps.add(tpl.ops[i]);
                        opToOrder[tpl.ops[i]] = i;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "[ArrayIndexOutOfBoundsException]" + e.getLocalizedMessage());
            }
        }
        Log.i(TAG, "------------------------------------------------------");

        List<AppOpsManager.PackageOps> pkgs;
        if (null != packageName) {
            pkgs = mAppOps.getOpsForPackage(uid, packageName, tpl.ops);
        } else {
            pkgs = mAppOps.getPackagesForOps(tpl.ops);
        }

        if (pkgs != null && null != packageName) {
            Log.i(TAG, "pkgs size : " + pkgs.size());
            for (int i = 0; i < pkgs.size(); i++) {
                AppOpsManager.PackageOps pkgOps = pkgs.get(i);
                AppEntry appEntry = getAppEntry(context, appEntries, pkgOps.getPackageName(), null);
                if (appEntry == null) {
                    continue;
                }
                try {
                    if (true == isSystemPackage(mPm.getPackageInfo(
                            pkgs.get(i).getPackageName(),
                            PackageManager.GET_PERMISSIONS))) {
                    }
                    else {
                        Log.i(TAG, "3rd permission pkg : " + pkgs.get(i).getPackageName());
                        for (int j = 0; j < pkgOps.getOps().size(); j++) {
                            AppOpsManager.OpEntry opEntry = pkgOps.getOps().get(j);
                            addOp(entries, pkgOps, appEntry, opEntry, packageName == null,
                                    packageName == null ? 0 : opToOrder[opEntry.getOp()]);
                        }
                    }
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "NameNotFoundException");
                }
            }
        }

        List<PackageInfo> apps;
        if (packageName != null) {
            Log.i(TAG, "packageName not null case");
            apps = new ArrayList<PackageInfo>();
            try {
                PackageInfo pi = mPm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                apps.add(pi);
            } catch (NameNotFoundException e) {
                Log.w(TAG, "NameNotFoundException");
            }
        } else {
            Log.i(TAG, "packageName null case");
            String[] permsArray = new String[perms.size()];
            perms.toArray(permsArray);

            apps = new ArrayList<PackageInfo>();
            //apps = mPm.getPackagesHoldingPermissions(permsArray, 0);
            //List<PackageInfo> installedApps = mPm.getPackagesHoldingPermissions(permsArray, 0);
            List<PackageInfo> installedApps = mPm
                    .getInstalledPackages(PackageManager.GET_PERMISSIONS);
            //List<ApplicationInfo> appslist
            //= mPm.getInstalledApplications(PackageManager.GET_PERMISSIONS);
            apps.clear();
            for (PackageInfo pi : installedApps) {
                if (true == isSystemPackage(pi)) {

                } else {
                    apps.add(pi);
                }
            }
        }

        int app_size = apps.size();
        Log.i(TAG, "#### apps size : " + app_size);
        Log.i(TAG, "***********************************************************");
        for (int i = 0; i < apps.size(); i++) {
            PackageInfo appInfo = apps.get(i);
            Log.i(TAG, "apps : " + appInfo.applicationInfo.className
                    + "     name : " + appInfo.applicationInfo.name);
            AppEntry appEntry = getAppEntry(context, appEntries, appInfo.packageName,
                    appInfo.applicationInfo);
            if (appEntry == null) {
                continue;
            }
            List<AppOpsManager.OpEntry> dummyOps = null;
            AppOpsManager.PackageOps pkgOps = null;

            if (appInfo.requestedPermissions != null) {
                int req_per_length = appInfo.requestedPermissions.length;
                for (int j = 0; j < req_per_length; j++) {
                    if (appInfo.requestedPermissionsFlags != null) {
                        if ((appInfo.requestedPermissionsFlags[j]
                        & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                            Log.d(TAG, "Pkg : " + appInfo.packageName + "   " +
                                    "perm : " + appInfo.requestedPermissions[j] + "   " +
                                    " req flag : " + appInfo.requestedPermissionsFlags[j] +
                                    " not granted skipping");
                            continue;
                        }
                    }
                    int perms_size = perms.size();
                    Log.i(TAG, "req_perm : " + appInfo.requestedPermissions[j] +
                            "perm size : " + perms_size);
                    for (int k = 0; k < perms_size; k++) {
                        if (!perms.get(k).equals(appInfo.requestedPermissions[j])) {
                            continue;
                        }

                        Log.d(TAG, "Pkg " + appInfo.packageName + " perm " + perms.get(k) +
                                " has op " + permOps.get(k) +
                                ": " + appEntry.hasOp(permOps.get(k)));
                        Log.i(TAG, "[" + k + "] " + "op_perm : " + perms.get(k));
                        /*
                        if (appEntry.hasOp(permOps.get(k))) {
                            continue;
                        }
                        */
                        if (dummyOps == null) {
                            dummyOps = new ArrayList<AppOpsManager.OpEntry>();
                            pkgOps = new AppOpsManager.PackageOps(
                                    appInfo.packageName, appInfo.applicationInfo.uid, dummyOps);

                        }
                        AppOpsManager.OpEntry opEntry = new AppOpsManager.OpEntry(
                                permOps.get(k), AppOpsManager.MODE_ALLOWED, 0, 0, 0);
                        dummyOps.add(opEntry);
                        Log.i(TAG, "add op " + appInfo.packageName + " : " + opEntry.getOp());
                        addOp(entries, pkgOps, appEntry, opEntry, packageName == null,
                                packageName == null ? 0 : opToOrder[opEntry.getOp()]);
                    }
                    Log.i(TAG, " ");
                }
            }
        }
        Log.i(TAG, "***********************************************************");

        // Sort the list.
        Collections.sort(entries, APP_OP_COMPARATOR);

        // Done!
        return entries;
    }
}
