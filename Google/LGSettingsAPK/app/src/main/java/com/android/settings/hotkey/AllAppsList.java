package com.android.settings.hotkey;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.text.Collator;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.ListActivity;
//import android.app.UsageStats; // Temporary blocked on pre-pdk
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
//import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import android.os.ServiceManager;
//import com.android.internal.app.IUsageStats; // Temporary blocked on pre-pdk
//import com.android.internal.os.PkgUsageStats;
import com.android.settings.hotkey.HotkeyInfo;

public class AllAppsList extends SettingsPreferenceFragment implements OnItemClickListener {

    private static final String TAG = "AllAppsList";
    public static final boolean DEBUG = false;

    private PackageManager mPm;
    private List<ResolveInfoEx> mAllApps;
    private List<ResolveInfoEx> mFrequentlyApps;
    private List<ResolveInfoEx> mFixedApps;
    private List<ResolveInfoEx> mRecommendedApps;

    public SeparatedListAdpater mAdapter;

    private boolean FEATURE_EXCLUDE_APP_LIST = true;

    public static final String INTENT_CATEGORY_LOCKSCREEN = "com.lge.lockscreensettings.CATEGORY_LOCKSCREEN";

    //private PkgUsageStats[] mStats;
//    private UsageStats.PackageStats[] mStats; // Temporary blocked on pre-pdk
    private HotkeyInfo mHotkeyInfo;

    private ListView l;

    private final int mFREQ_APPS_TO_BE_DISPLAYED = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.sp_hotkey_select_application_NORMAL);
            if (Utils.isSharingBetweenSimHotkey(getActivity().getApplicationContext())) {
                actionBar.setIcon(R.drawable.shortcut_customizing_key_4th);
            }
        }
        getActivity().setTitle(R.string.sp_hotkey_select_application_NORMAL);
        mHotkeyInfo = new HotkeyInfo(getActivity().getBaseContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // initialize the inflater
        LayoutInflater mInflater = inflater;

        View rootView = mInflater.inflate(R.layout.hotkey_listview,
                container, false);

        l = (ListView)rootView.findViewById(android.R.id.list);

        if (l != null) {
            //            l.setFastScrollEnabled(true);
        }

        makeFixedAppListItems();
        makeAppListItems();
        makeRecommendedAppListItems();

        mAdapter = new SeparatedListAdpater(getActivity());
        mAdapter.addSection("NO_HEADER", new FixedAppsListAdapter(getActivity()));
        mAdapter.addSection(getString(R.string.sp_hotkey_recommended_apps_NORMAL),
                new RecommendedAppsListAdapter(getActivity()));
        mAdapter.addSection(getString(R.string.sp_hotkey_all_apps_NORMAL), new AllAppsListAdapter(
                getActivity()));

        if (l != null) {
            l.setOnItemClickListener(this);
            l.setAdapter(mAdapter);
        }

        return rootView;
    }

    private void makeFixedAppListItems() {
        mFixedApps = new ArrayList<ResolveInfoEx>();
        mFixedApps.add(null); // For None
    }

    ResolveInfoEx getResolveInfo(String packageName) {
        if (mAllApps == null || mAllApps.size() == 0) {
            return null;
        }

        for (ResolveInfoEx ri : mAllApps) {
            if (packageName.equals(ri.activityInfo.packageName)) {
                return new ResolveInfoEx(ri);
                //return ri ;
            }
        }
        return null;
    }

    private void makeRecommendedAppListItems() {
        String[] recommendedApps = null;

        if (mHotkeyInfo.isNotSingleSimModel()) {
            recommendedApps = getResources().getStringArray(R.array.recommended_apps_wx);
        }
        else {
            recommendedApps = getResources().getStringArray(R.array.recommended_apps);
        }

        if (false == mHotkeyInfo.isSupportAccelSensor() &&
                true == Utils.isSharingBetweenSimHotkey(getActivity())) {
            ArrayList<String> recommendedAppsEx = new ArrayList<String>();
            Collections.addAll(recommendedAppsEx, recommendedApps);
            recommendedAppsEx.add(0, HotkeyInfo.HOTKEY_ROTATE_SWITCH); // add first
            recommendedApps = recommendedAppsEx.toArray(new String[recommendedAppsEx.size()]);
        }

        mRecommendedApps = new ArrayList<ResolveInfoEx>();

        for (int i = 0; i < recommendedApps.length; i++) {
            ResolveInfoEx ri = getResolveInfo(recommendedApps[i]);

            if (HotkeyInfo.DEFAULT_PACKAGE.equals(recommendedApps[i])) {
                mRecommendedApps.add(null); // For Quick Button
                continue;
            }
            else if (HotkeyInfo.HOTKEY_SIM_SWITCH.equals(recommendedApps[i]) 
            && Utils.isSharingBetweenSimHotkey(getActivity())) {
                ri = getResolveInfo("com.android.settings");
                if (ri != null) {
                    ri.setPKGName(HotkeyInfo.HOTKEY_SIM_SWITCH); // For Sim switch
                }
            }
            else if (HotkeyInfo.HOTKEY_ROTATE_SWITCH.equals(recommendedApps[i])) {
                ri = getResolveInfo("com.android.settings");
                if (ri != null) {
                    ri.setPKGName(HotkeyInfo.HOTKEY_ROTATE_SWITCH); // For Sim switch
                }
            }
            if (ri != null) {
                mRecommendedApps.add(ri);
            }
        }
    }

    private void makeFrequentlyAppListItems() {
//        IUsageStats usageStatsService = IUsageStats.Stub.asInterface(ServiceManager
//                .getService("usagestats")); // Temporary blocked on pre-pdk

// Temporary blocked on pre-pdk
/*        if (usageStatsService == null) {
            return;
        }*/

        ArrayList<ResolveInfoEx> sortedApps = new ArrayList<ResolveInfoEx>(mAllApps);
        // Temporary blocked on pre-pdk
        /*try {
            //mStats = usageStatsService.getAllPkgUsageStats();
            mStats = usageStatsService.getAllPkgUsageStats(ActivityThread.currentPackageName()); 

            Collections.sort(sortedApps, new Comparator<ResolveInfoEx>() {
                public int compare(ResolveInfoEx object1, ResolveInfoEx object2) {
                    int launchCount1 = getAppLaunchCount(object1.activityInfo.packageName);
                    int launchCount2 = getAppLaunchCount(object2.activityInfo.packageName);
                    return (launchCount2 - launchCount1);
                }
            });
        } catch (RemoteException e) {
            Log.d(TAG, "usageStatsService remote exception");
        }*/// Temporary blocked on pre-pdk

        int index = 0;
        for (index = 0; index < sortedApps.size(); index++) {
            int launchCount = getAppLaunchCount(sortedApps.get(index).activityInfo.packageName);
            if (launchCount <= 0 || index >= (mFREQ_APPS_TO_BE_DISPLAYED - 1)) {
                break;
            }
        }

        if (index > 0) {
            mFrequentlyApps = sortedApps.subList(0, index);
        }
        mFrequentlyApps.add(0, null); // For QuickMemo
    }

    private void makeAppListItems() {
        mPm = this.getPackageManager();
        final Intent intentLauncher = new Intent(Intent.ACTION_MAIN, null);
        intentLauncher.addCategory(Intent.CATEGORY_LAUNCHER);

        mAllApps = new ArrayList<ResolveInfoEx>();
        for (ResolveInfo resolveinfo : mPm.queryIntentActivities(intentLauncher, 0)) {
            mAllApps.add(new ResolveInfoEx(resolveinfo));
        }

        Collections.sort(mAllApps, new Comparator<ResolveInfoEx>() {
            private final Collator sCollator = Collator.getInstance();

            public int compare(ResolveInfoEx object1, ResolveInfoEx object2) {
                return sCollator.compare(object1.loadLabel(mPm), object2.loadLabel(mPm));
            }
        });

        if (FEATURE_EXCLUDE_APP_LIST) {
            filterApps();
        }
    }

    private int getAppLaunchCount(String packageName) {
    	// Temporary blocked on pre-pdk
/*        if (mStats != null) {
            for (UsageStats.PackageStats pus : mStats) {
                if (packageName.equals(pus.getPackageName())) {
                    return pus.getLaunchCount();
                }
            }
        }*/
        return -1;
    }

    @Override
    public void onDestroy() {
        Utils.recycleView(l);
        System.gc();
        super.onDestroy();
    }

    private void filterApps() {
        String[] exclusionList = getResources().getStringArray(R.array.exclude_apps);
        ArrayList<ResolveInfoEx> removeList = new ArrayList<ResolveInfoEx>();

        for (String name : exclusionList) {
            for (ResolveInfoEx info : mAllApps) {
                String activityName = info.activityInfo.name;

                if (name.equals(activityName)) {
                    Log.d("LockScreenSettings", "[AllAppsList:OnCreate] App excluded : "
                            + activityName);
                    removeList.add(info);
                    break;
                }
            }
        }

        mAllApps.removeAll(removeList);
    }

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        ResolveInfoEx appInfo = (ResolveInfoEx)mAdapter.getItem(position);

        if (appInfo != null && true == mHotkeyInfo.isNormalPackage(appInfo.getPKGName())) {
            mHotkeyInfo.setDBHotKeyShortPKG(appInfo.activityInfo.packageName);
            mHotkeyInfo.setDBHotKeyShortClass(appInfo.activityInfo.name);

            Log.i(TAG, "onListItemClick Normal - label : " + appInfo.loadLabel(mPm));
            Log.i(TAG, "onListItemClick Normal - package_name : "
                    + appInfo.activityInfo.packageName);
            Log.i(TAG, "onListItemClick Normal - class_name : " + appInfo.activityInfo.name);

        } else if (appInfo != null && false == mHotkeyInfo.isNormalPackage(appInfo.getPKGName())) {
            mHotkeyInfo.setDBHotKeyShortPKG(appInfo.getPKGName());
            mHotkeyInfo.setDBHotKeyShortClass(HotkeyInfo.HOTKEY_NONE);

            Log.i(TAG, "onListItemClick Dummy1 - package_name : " + appInfo.getPKGName());
            Log.i(TAG, "onListItemClick Dummy1 - class_name : " + HotkeyInfo.HOTKEY_NONE);
            Log.i(TAG, "onListItemClick Dummy - label : " + appInfo.loadLabel(mPm));
        }
        else {
            if (position == 0) {

                Settings.System.putString(getContentResolver(), HotkeyInfo.HOTKEY_SHORT_PACKAGE,
                        HotkeyInfo.HOTKEY_NONE);
                Settings.System.putString(getContentResolver(), HotkeyInfo.HOTKEY_SHORT_CLASS,
                        HotkeyInfo.HOTKEY_NONE);

                Log.i(TAG,
                        "onListItemClick - label : "
                                + getResources().getString(R.string.sp_hotkey_none_NORMAL));
                Log.i(TAG,
                        "onListItemClick - package_name : "
                                + getResources().getString(R.string.sp_hotkey_none_NORMAL));
                Log.i(TAG,
                        "onListItemClick - class_name : "
                                + getResources().getString(R.string.sp_hotkey_none_NORMAL));

            } else {
                try {
                    Settings.System.putString(getContentResolver(),
                            HotkeyInfo.HOTKEY_SHORT_PACKAGE, HotkeyInfo.DEFAULT_PACKAGE);
                    Settings.System.putString(getContentResolver(), HotkeyInfo.HOTKEY_SHORT_CLASS,
                            HotkeyInfo.DEFAULT_CLASS);

                    Log.i(TAG,
                            "onListItemClick - label : "
                                    + mPm.getApplicationLabel(mPm.getPackageInfo(
                                            HotkeyInfo.DEFAULT_PACKAGE, 0).applicationInfo));
                    Log.i(TAG, "onListItemClick - package_name : " + "com.lge.QuickClip");
                    Log.i(TAG, "onListItemClick - class_name : "
                            + "com.lge.QuickClip.QuickClipActivity");

                } catch (NameNotFoundException e) {
                    Log.w(TAG, "NameNotFoundException");
                }
            }

        }
        finishFragment();
    }

    class FixedAppsListAdapter extends ArrayAdapter<ResolveInfoEx> {
        Activity context;

        FixedAppsListAdapter(Activity context) {
            super(context, R.layout.hotkey_item_row, mFixedApps);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            AppListItemWrapper wrapper = null;

            if (row == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                row = inflater.inflate(R.layout.hotkey_item_row, null);
                wrapper = new AppListItemWrapper(row);
                row.setTag(wrapper);
            } else {
                wrapper = (AppListItemWrapper)row.getTag();
            }

            if (position == 0) {
                wrapper.getLabel().setText(R.string.sp_hotkey_none_NORMAL);
                wrapper.getIcon().setImageResource(R.drawable.ic_none);
            }

            final ImageView divider = (ImageView)row.findViewById(R.id.hotkey_list_divider);
            if (position == getCount() - 1) {
                divider.setVisibility(View.INVISIBLE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }

            return (row);
        }
    }

    class RecommendedAppsListAdapter extends ArrayAdapter<ResolveInfoEx> {
        Activity context;

        RecommendedAppsListAdapter(Activity context) {
            super(context, R.layout.hotkey_item_row, mRecommendedApps);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            AppListItemWrapper wrapper = null;

            if (row == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                row = inflater.inflate(R.layout.hotkey_item_row, null);
                wrapper = new AppListItemWrapper(row);
                row.setTag(wrapper);
            } else {
                wrapper = (AppListItemWrapper)row.getTag();
            }

            ResolveInfoEx appInfo = mRecommendedApps.get(position);
            if (appInfo == null) {
                try {
                    PackageInfo pInfo = mPm.getPackageInfo(HotkeyInfo.DEFAULT_PACKAGE, 0);
                    wrapper.getLabel().setText(mPm.getApplicationLabel(pInfo.applicationInfo));
                    wrapper.getIcon().setImageDrawable(
                            mPm.getApplicationIcon(HotkeyInfo.DEFAULT_PACKAGE));
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "NameNotFoundException");
                }
            }
            else if (HotkeyInfo.HOTKEY_SIM_SWITCH.equals(appInfo.getPKGName())) {
                wrapper.getLabel().setText(
                        getResources().getString(R.string.quickbutton_simcard_switch));
                wrapper.getIcon().setImageDrawable(
                        getResources().getDrawable(R.drawable.shortcut_dual_sim));
            }
            else if (HotkeyInfo.HOTKEY_ROTATE_SWITCH.equals(appInfo.getPKGName())) {
                wrapper.getLabel().setText(getResources()
                        .getString(R.string.quick_button_rotate_screen));
                wrapper.getIcon().setImageDrawable(getResources()
                        .getDrawable(R.drawable.shortcut_auto_rotate_screen));
            }
            else {
                wrapper.getLabel().setText(appInfo.loadLabel(mPm));
                setAppIcon(wrapper.getIcon(), appInfo);
            }

            final ImageView divider = (ImageView)row.findViewById(R.id.hotkey_list_divider);
            if (position == getCount() - 1) {
                divider.setVisibility(View.INVISIBLE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }

            return (row);
        }
    }

    class FreqAppsListAdapter extends ArrayAdapter<ResolveInfoEx> {
        Activity context;

        FreqAppsListAdapter(Activity context) {
            super(context, R.layout.hotkey_item_row, mFrequentlyApps);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            AppListItemWrapper wrapper = null;

            if (row == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                row = inflater.inflate(R.layout.hotkey_item_row, null);
                wrapper = new AppListItemWrapper(row);
                row.setTag(wrapper);
            } else {
                wrapper = (AppListItemWrapper)row.getTag();
            }

            ResolveInfoEx appInfo = mFrequentlyApps.get(position);
            if (position == 0) {
                try {
                    PackageInfo pInfo = mPm.getPackageInfo(HotkeyInfo.DEFAULT_PACKAGE, 0);
                    wrapper.getLabel().setText(mPm.getApplicationLabel(pInfo.applicationInfo));
                    wrapper.getIcon().setImageDrawable(
                            mPm.getApplicationIcon(HotkeyInfo.DEFAULT_PACKAGE));
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "NameNotFoundException");
                }
            } else {
                wrapper.getLabel().setText(appInfo.loadLabel(mPm));
                setAppIcon(wrapper.getIcon(), appInfo);
            }

            final ImageView divider = (ImageView)row.findViewById(R.id.hotkey_list_divider);
            if (position == getCount() - 1) {
                divider.setVisibility(View.INVISIBLE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }

            return (row);
        }
    }

    class AllAppsListAdapter extends ArrayAdapter<ResolveInfoEx> {
        Activity context;

        AllAppsListAdapter(Activity context) {
            super(context, R.layout.hotkey_item_row, mAllApps);
            this.context = context;
        }

        public View getView(int position, View convertView,
                ViewGroup parent) {

            View row = convertView;
            AppListItemWrapper wrapper = null;

            if (row == null) {
                LayoutInflater inflater = context.getLayoutInflater();

                row = inflater.inflate(R.layout.hotkey_item_row, null);
                wrapper = new AppListItemWrapper(row);
                row.setTag(wrapper);
            } else {
                wrapper = (AppListItemWrapper)row.getTag();
            }

            ResolveInfoEx appInfo = mAllApps.get(position);
            wrapper.getLabel().setText(appInfo.loadLabel(mPm));
            setAppIcon(wrapper.getIcon(), appInfo);

            return (row);
        }
    }

    private void setAppIcon(ImageView iv, ResolveInfoEx appInfo) {
        iv.setImageDrawable(appInfo.loadIcon(mPm));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ResolveInfoEx extends ResolveInfo {
        private String pkgName = null;

        public ResolveInfoEx() {
            super();
            // TODO Auto-generated constructor stub
            /*
            try {
                pkgName = this.activityInfo.packageName;
            } catch(NullPointerException e) {
                pkgName = HOTKEY_NONE;
            }
            */
            if (null != this.activityInfo) {
                pkgName = this.activityInfo.packageName;
            }
            else {
                pkgName = HotkeyInfo.HOTKEY_NONE;
            }
        }

        public ResolveInfoEx(String pkg) {
            // TODO Auto-generated constructor stub
            pkgName = pkg;
        }

        public ResolveInfoEx(ResolveInfo resolveinfo) {
            // TODO Auto-generated constructor stub
            super(resolveinfo);
        }

        public void setPKGName(String pkg) {
            if (null != pkg) {
                pkgName = pkg;
            }
            else {
                pkgName = HotkeyInfo.HOTKEY_NONE;
            }
        }

        public String getPKGName() {
            if (null != pkgName) {
                return pkgName;
            }

            return HotkeyInfo.HOTKEY_NONE;
        }
    }
}
