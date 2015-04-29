/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.applications;

import static android.net.NetworkPolicyManager.POLICY_NONE;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager; // rebestm - kk migration
import android.app.Dialog;
import android.app.Fragment;
import android.app.INotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFrameLayout;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.BidiFormatter; // rebestm - kk migration
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
//import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;
import java.util.Locale;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.content.PackageHelper;
import com.android.settings.R;
import com.android.settings.SLog;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.UserSpinnerAdapter;
import com.android.settings.Settings.RunningServicesActivity;
import com.android.settings.Settings.StorageUseActivity;
import com.android.settings.applications.ApplicationsState.AppEntry;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.unusedapps.ui.settings.AppCleanupSettingsFragment;
import com.android.settings.Utils;

import com.lge.sui.widget.control.SUIDrumDatePicker;
import com.lge.sui.widget.control.SUIDrumPicker.OnDataChangedListener;
import com.lge.sui.widget.dialog.SUIDrumDatePickerDialog;
import com.lge.sui.widget.dialog.SUIDrumPickerDialog.OnDataSetListener;
import com.lge.sui.widget.dialog.SUIDrumTimePickerDialog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import android.app.AlertDialog.Builder;
// 3LM_MDM_DCM jihun.im@lge.com 20121015
import android.app.admin.DevicePolicyManager;
import android.os.Build;

final class CanBeOnSdCardChecker {
    final IPackageManager mPm;
    int mInstallLocation;

    CanBeOnSdCardChecker() {
        mPm = IPackageManager.Stub.asInterface(
                ServiceManager.getService("package"));
    }

    void init() {
        try {
            mInstallLocation = mPm.getInstallLocation();
        } catch (RemoteException e) {
            Log.e("CanBeOnSdCardChecker", "Is Package Manager running?");
            return;
        }
    }

    boolean check(ApplicationInfo info) {
        boolean canBe = false;
        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            canBe = true;
        } else {
            if ((info.flags & ApplicationInfo.FLAG_FORWARD_LOCK) == 0 &&
                    (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                if (info.installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL ||
                        info.installLocation == PackageInfo.INSTALL_LOCATION_AUTO) {
                    canBe = true;
                } else if (info.installLocation
                == PackageInfo.INSTALL_LOCATION_UNSPECIFIED) {
                    if (mInstallLocation == PackageHelper.APP_INSTALL_EXTERNAL) {
                        // For apps with no preference and the default value set
                        // to install on sdcard.
                        canBe = true;
                    }
                }
            }
        }
        return canBe;
    }
}

interface AppClickListener {
    void onItemClick(ManageApplications.TabInfo tab, AdapterView<?> parent,
            View view, int position, long id);
}

/**
 * Activity to pick an application that will be used to display installation information and
 * options to uninstall/delete user data for system applications. This activity
 * can be launched through Settings or via the ACTION_MANAGE_PACKAGE_STORAGE
 * intent.
 */
public class ManageApplications extends Fragment implements
        AppClickListener, DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, OnItemSelectedListener,
        SUIDrumDatePickerDialog.OnDateSetListener, Indexable {

    static final String TAG = "ManageApplications";
    static final boolean DEBUG = false;

    private static final String EXTRA_LIST_TYPE = "currentListType";
    private static final String EXTRA_SORT_ORDER = "sortOrder";
    private static final String EXTRA_SHOW_BACKGROUND = "showBackground";
    private static final String EXTRA_DEFAULT_LIST_TYPE = "defaultListType";
    private static final String EXTRA_RESET_DIALOG = "resetDialog";
    private static final String EXTRA_PICKER = "Picker";
    private static final String RECOVER_APP_NAME = "com.lge.apprecovery";

    // attributes used as keys when passing values to InstalledAppDetails activity
    public static final String APP_CHG = "chg";

    public static final String APPS_PREFS = "apps_prefs";
    public static final String APPS_PREFS_DATE_FORMAT = "DateFormat";
    public static final String APPS_PREFS_BATTERY = "BatteryLevel";
    public static final String APPS_PREFS_UPDATE_DATA = "DATAUPDATE";
    public static final String APPS_PREFS_UPDATE_DATA_YES = "YES";
    public static final String APPS_PREFS_UPDATE_DATA_NO = "NO";

    private static final int RECOVER_LIMITED_BATTERY = 5;

    // constant value that can be used to check return code from sub activity.
    private static final int INSTALLED_APP_DETAILS = 1;

    public static final int SIZE_TOTAL = 0;
    public static final int SIZE_INTERNAL = 1;
    public static final int SIZE_EXTERNAL = 2;

    // sort order that can be changed through the menu can be sorted alphabetically
    // or size(descending)
    private static final int MENU_OPTIONS_BASE = 0;
    // Filter options used for displayed list of applications
    public static final int FILTER_APPS_ALL = MENU_OPTIONS_BASE + 0;
    public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 1;
    public static final int FILTER_APPS_SDCARD = MENU_OPTIONS_BASE + 2;
    public static final int FILTER_APPS_DISABLED = MENU_OPTIONS_BASE + 3; // rebestm - kk migration

    public static final int SORT_POPUP = MENU_OPTIONS_BASE + 4;
    public static final int SORT_ORDER_ALPHA = MENU_OPTIONS_BASE + 5;
    public static final int SORT_ORDER_SIZE = MENU_OPTIONS_BASE + 6;
    public static final int SHOW_RUNNING_SERVICES = MENU_OPTIONS_BASE + 7;
    public static final int SHOW_BACKGROUND_PROCESSES = MENU_OPTIONS_BASE + 8;
    public static final int RESET_APP_PREFERENCES = MENU_OPTIONS_BASE + 9;
    public static final int UNUSED_APPS = MENU_OPTIONS_BASE + 10;
    public static final int DEFAULT_APPS = MENU_OPTIONS_BASE + 11;
    public static final int MULTIDELETE_APPS = MENU_OPTIONS_BASE + 12; //rebestm
    public static final int RECOVER_APPS = MENU_OPTIONS_BASE + 13; //
    public static final int SPECIFIC_DATE = MENU_OPTIONS_BASE + 14; // picker_porting

    public static final int SORT_ORDER_DATE = 100; //rebestm
    // picker_porting
    private static final int DIALOG_DATEPICKER = 0;
    private Dialog Pickerdialog;
    Calendar pickCalendar;
    // sort order
    private int mSortOrder = SORT_ORDER_ALPHA;

    private ApplicationsState mApplicationsState;
    public AppMultiDeleteGlobalVariable gv;
    private SettingsBreadCrumb mBreadCrumb;
    TabInfo tabinfo;

    String batteryLevel;

    // [START][2015-02-16][seungyeop.yeom] Create value for Search function
    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    // [END][2015-02-16][seungyeop.yeom] Create value for Search function

    public static class TabInfo implements OnItemClickListener {
        public final ManageApplications mOwner;
        public final ApplicationsState mApplicationsState;
        public final CharSequence mLabel;
        public final int mListType;
        public final int mFilter;
        public final AppClickListener mClickListener;
        public final CharSequence mInvalidSizeStr;
        public final CharSequence mComputingSizeStr;
        private final Bundle mSavedInstanceState;

        public ApplicationsAdapter mApplications;
        public LayoutInflater mInflater;
        public View mRootView;

        private IMediaContainerService mContainerService;

        private View mLoadingContainer;

        private View mListContainer;

        private ViewGroup mPinnedHeader;

        // ListView used to display list
        private ListView mListView;
        // Custom view used to display running processes
        private RunningProcessesView mRunningProcessesView;

        View mEmptyView;
        // Block for L OS
        /*
        private LinearColorBar mColorBar;
        private TextView mStorageChartLabel;
        private TextView mUsedStorageText;
        private TextView mFreeStorageText;
        */

        private long mFreeStorage = 0;
        private long mAppStorage = 0;
        /*
        private long mTotalStorage = 0;
        private long mLastUsedStorage;
        private long mLastFreeStorage;
        */

        final Runnable mRunningProcessesAvail = new Runnable() {
            public void run() {
                handleRunningProcessesAvail();
            }
        };

        public TabInfo(ManageApplications owner, ApplicationsState apps,
                CharSequence label, int listType, AppClickListener clickListener,
                Bundle savedInstanceState) {
            mOwner = owner;
            mApplicationsState = apps;
            mLabel = label;
            mListType = listType;
            switch (listType) {
            case LIST_TYPE_DOWNLOADED:
                mFilter = FILTER_APPS_THIRD_PARTY;
                break;
            case LIST_TYPE_SDCARD:
                mFilter = FILTER_APPS_SDCARD;
                break;
            case LIST_TYPE_DISABLED:
                mFilter = FILTER_APPS_DISABLED;
                break; // rebestm - kk migration
            default:
                mFilter = FILTER_APPS_ALL;
                break;
            }
            mClickListener = clickListener;
            mInvalidSizeStr = owner.getActivity().getText(R.string.invalid_size_value);
            mComputingSizeStr = owner.getActivity().getText(R.string.computing_size);
            mSavedInstanceState = savedInstanceState;
        }

        public void setContainerService(IMediaContainerService containerService) {
            mContainerService = containerService;
            updateStorageUsage();
        }

        public View build(LayoutInflater inflater, ViewGroup contentParent, View contentChild) {
            if (mRootView != null) {
                return mRootView;
            }

            mInflater = inflater;
            mRootView = inflater.inflate(mListType == LIST_TYPE_RUNNING
                    ? Utils.getResources().getLayout(R.layout.manage_applications_running)
                    : Utils.getResources().getLayout(R.layout.manage_applications_apps), null);
            mPinnedHeader = (ViewGroup)mRootView.findViewById(R.id.pinned_header);
            if (mOwner.mProfileSpinnerAdapter != null) {
                Spinner spinner = (Spinner)inflater.inflate(R.layout.spinner_view, null);
                spinner.setAdapter(mOwner.mProfileSpinnerAdapter);
                spinner.setOnItemSelectedListener(mOwner);
                mPinnedHeader.addView(spinner);
                mPinnedHeader.setVisibility(View.VISIBLE);
            }
            mLoadingContainer = mRootView.findViewById(R.id.loading_container);
            mLoadingContainer.setVisibility(View.VISIBLE);
            mListContainer = mRootView.findViewById(R.id.list_container);
            if (mListContainer != null) {
                // Create adapter and list view here
                View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
                ListView lv = (ListView)mListContainer.findViewById(android.R.id.list);
                if (emptyView != null) {
                    mEmptyView = emptyView;
                    lv.setEmptyView(emptyView);
                    TextView app_name = (TextView)emptyView;
                    if (Utils.isUI_4_1_model(mOwner.getActivity())) {
                        app_name.setTextColor(Color.BLACK);
                        app_name.setPaddingRelative(0, 16, 0, 0);
                        app_name.setTextSize(18);
                        lv.setFastScrollEnabled(true);
                    } else {
                        app_name.setCompoundDrawablePadding(-28);
                        app_name.setTypeface(null, Typeface.BOLD);
                    }
                }
                lv.setOnItemClickListener(this);
                lv.setSaveEnabled(true);
                lv.setItemsCanFocus(true);
                lv.setTextFilterEnabled(true);
                mListView = lv;
                mApplications = new ApplicationsAdapter(mApplicationsState, this, mFilter);
                mListView.setAdapter(mApplications);
                mListView.setRecyclerListener(mApplications);
                // Block for L OS
                /*
                mColorBar = (LinearColorBar)mListContainer.findViewById(R.id.storage_color_bar);
                mStorageChartLabel = (TextView)mListContainer.findViewById(R.id.storageChartLabel);
                mUsedStorageText = (TextView)mListContainer.findViewById(R.id.usedStorageText);
                mFreeStorageText = (TextView)mListContainer.findViewById(R.id.freeStorageText);
                */
                // KK -Not used - List side margin
                // Utils.prepareCustomPreferencesList(contentParent, contentChild, mListView, false);
                if (mFilter == FILTER_APPS_SDCARD) {
                    // Block for L OS
                    /*
                    mStorageChartLabel.setText(mOwner.getActivity().getText(
                            R.string.sd_card_storage));
                    if (Utils.supportInternalMemory()
                            && !Utils.isLGExternalInfoSupport(mOwner.getActivity())) {
                        // yonguk.kim 20120816 ICS Stuff Porting to JB
                        mStorageChartLabel.setText(mOwner.getActivity()
                                .getText(R.string.sp_settings_apps_internal_memory_NORMAL));
                    }
                    */
                } else {
                    // Block for L OS
                    /*
                    mStorageChartLabel.setText(mOwner.getActivity().getText(
                            R.string.internal_storage));
                    */
                }
                applyCurrentStorage();
            }
            mRunningProcessesView = (RunningProcessesView)mRootView.findViewById(
                    R.id.running_processes);
            if (mRunningProcessesView != null) {
                mRunningProcessesView.doCreate(mSavedInstanceState);
            }

            return mRootView;
        }

        public void detachView() {
            if (mRootView != null) {
                ViewGroup group = (ViewGroup)mRootView.getParent();
                if (group != null) {
                    group.removeView(mRootView);
                }
            }
        }

        public void resume(int sortOrder) {
            if (mApplications != null) {
                mApplications.resume(sortOrder);
            }
            if (mRunningProcessesView != null) {
                boolean haveData = mRunningProcessesView.doResume(mOwner, mRunningProcessesAvail);
                if (haveData) {
                    mRunningProcessesView.setVisibility(View.VISIBLE);
                    mLoadingContainer.setVisibility(View.INVISIBLE);
                } else {
                    mLoadingContainer.setVisibility(View.VISIBLE);
                }
            }
        }

        public void pause() {
            if (mApplications != null) {
                mApplications.pause();
            }
            if (mRunningProcessesView != null) {
                mRunningProcessesView.doPause();
            }
        }

        public void release() {
            if (mApplications != null) {
                mApplications.release();
            }
        }

        void updateStorageUsage() {
            // Make sure a callback didn't come at an inopportune time.
            if (mOwner.getActivity() == null) {
                return;
            }
            // Doesn't make sense for stuff that is not an app list.
            if (mApplications == null) {
                return;
            }
            mFreeStorage = 0;
            mAppStorage = 0;
            // mTotalStorage = 0;

            if (mFilter == FILTER_APPS_SDCARD) {
                if (mContainerService != null) {
                    try {
                        final long[] stats;
                        if (Utils.isLGExternalInfoSupport(mOwner.getActivity())) {
                            stats = mContainerService.getFileSystemStats("/storage/external_SD");
                        } else {
                            stats = mContainerService.getFileSystemStats(
                                    Environment.getExternalStorageDirectory().getPath());
                        }

                        // mTotalStorage = stats[0];
                        mFreeStorage = stats[1];
                    } catch (RemoteException e) {
                        Log.w(TAG, "Problem in container service", e);
                    } catch (IllegalStateException e) {
                        Log.w(TAG, "IllegalStateException", e);
                        // mTotalStorage = 0;
                        mFreeStorage = 0;
                    }
                }

                if (mApplications != null) {
                    final int N = mApplications.getCount();
                    for (int i = 0; i < N; i++) {
                        ApplicationsState.AppEntry ae = mApplications.getAppEntry(i);
                        if (ae == null) {
                            continue; //myeonghwan.kim@lge.com 20120927 WBT Issue
                        }
                        mAppStorage += ae.externalCodeSize + ae.externalDataSize
                                + ae.externalCacheSize;
                    }
                }
            } else {
                if (mContainerService != null) {
                    try {
                        final long[] stats = mContainerService.getFileSystemStats(
                                Environment.getDataDirectory().getPath());
                        // mTotalStorage = stats[0];
                        mFreeStorage = stats[1];
                    } catch (RemoteException e) {
                        Log.w(TAG, "Problem in container service", e);
                    }
                }

                final boolean emulatedStorage = !Utils
                        .isLGExternalInfoSupport(mOwner.getActivity()); // Environment.isExternalStorageEmulated();
                if (mApplications != null) {
                    final int N = mApplications.getCount();
                    for (int i = 0; i < N; i++) {
                        ApplicationsState.AppEntry ae = mApplications.getAppEntry(i);
                        if (ae == null) {
                            continue; //myeonghwan.kim@lge.com 20120927 WBT Issue
                        }
                        mAppStorage += ae.codeSize + ae.dataSize;
                        if (emulatedStorage) {
                            mAppStorage += ae.externalCodeSize + ae.externalDataSize;
                        }
                    }
                }
                mFreeStorage += mApplicationsState.sumCacheSizes();
            }

            applyCurrentStorage();
        }

        void applyCurrentStorage() {
            // If view hierarchy is not yet created, no views to update.
            if (mRootView == null) {
                return;
            }
            // Block for L OS
            /*if (mTotalStorage > 0) {
                if (Utils.isLGExternalInfoSupport(mOwner.getActivity())) {
                    mColorBar.setVisibility(View.VISIBLE);
                }
                BidiFormatter bidiFormatter = BidiFormatter.getInstance(); // rebestm - kk migration
                // yonguk.kim 20120816 ICS Stuff Porting to JB - Decrease Total & Free size in DCM : 10MB
                if("DCM".equalsIgnoreCase(Config.getOperator()){
                    Log.d(TAG, "Before: totalStorage:"+mTotalStorage+",  freeStorage:"+mFreeStorage);
                    mTotalStorage -= (10*1024*1024) ;
                    mFreeStorage -= (10*1024*1024) ;
                    if(mFreeStorage<0)
                        mFreeStorage = 0 ;
                    Log.d(TAG, "After: totalStorage:"+mTotalStorage+",  freeStorage:"+mFreeStorage);
                }

                mColorBar.setRatios((mTotalStorage - mFreeStorage - mAppStorage)
                        / (float)mTotalStorage,
                        mAppStorage / (float)mTotalStorage, mFreeStorage / (float)mTotalStorage);
                long usedStorage = mTotalStorage - mFreeStorage;
                if (mLastUsedStorage != usedStorage) {
                    mLastUsedStorage = usedStorage;
                    // rebestm - kk migration
                    String sizeStr = bidiFormatter.unicodeWrap(
                            Formatter.formatShortFileSize(mOwner.getActivity(), usedStorage));
                    //Lavanya added for TD227437 GB reversal for hebrew
                    if ("iw".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
                        mUsedStorageText.setText(mOwner
                                .getActivity()
                                .getResources()
                                .getString(
                                        R.string.service_foreground_processes,
                                        Utils.onLGFormatter("\u202A" + sizeStr + "\u202C")));
                    } else {
                        mUsedStorageText.setText(mOwner
                                .getActivity()
                                .getResources()
                                .getString(
                                        R.string.service_foreground_processes,
                                        Utils.onLGFormatter(sizeStr)));
                    }
                    //Lavanya added for TD227437 GB reversal for hebrew
                }
                if (mLastFreeStorage != mFreeStorage) {
                    mLastFreeStorage = mFreeStorage;
                    // rebestm - kk migration
                    String sizeStr = bidiFormatter.unicodeWrap(
                            Formatter.formatShortFileSize(mOwner.getActivity(), mFreeStorage));
                    //Lavanya added for TD227437 GB reversal for hebrew
                    if ("iw".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
                        mFreeStorageText.setText(mOwner
                                .getActivity()
                                .getResources()
                                .getString(
                                        R.string.service_background_processes,
                                        Utils.onLGFormatter("\u202A" + sizeStr + "\u202C")));
                    } else {
                        mFreeStorageText.setText(mOwner
                                .getActivity()
                                .getResources()
                                .getString(
                                        R.string.service_background_processes,
                                        Utils.onLGFormatter(sizeStr)));
                    }
                    //Lavanya added for TD227437 GB reversal for hebrew

                }
            } else {
                if (Utils.isLGExternalInfoSupport(mOwner.getActivity())) {
                    mColorBar.setVisibility(View.INVISIBLE);
                }
                mColorBar.setRatios(0, 0, 0);
                if (mLastUsedStorage != -1) {
                    mLastUsedStorage = -1;
                    mUsedStorageText.setText("");
                }
                if (mLastFreeStorage != -1) {
                    mLastFreeStorage = -1;
                    mFreeStorageText.setText("");
                }
            }*/
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mClickListener.onItemClick(this, parent, view, position, id);
        }

        void handleRunningProcessesAvail() {
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(
                    mOwner.getActivity(), android.R.anim.fade_out));
            mRunningProcessesView.startAnimation(AnimationUtils.loadAnimation(
                    mOwner.getActivity(), android.R.anim.fade_in));
            mRunningProcessesView.setVisibility(View.VISIBLE);
            mLoadingContainer.setVisibility(View.GONE);
        }
    }

    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private int mNumTabs; // rebestm - kk migration
    TabInfo mCurTab = null;

    // Size resource used for packages whose size computation failed for some reason
    CharSequence mInvalidSizeStr;
    //private CharSequence mComputingSizeStr;

    // layout inflater object used to inflate views
    private LayoutInflater mInflater;

    private String mCurrentPkgName;

    private Menu mOptionsMenu;

    // These are for keeping track of activity and spinner switch state.
    private boolean mActivityResumed;

    private static final int LIST_TYPE_MISSING = -1;
    static final int LIST_TYPE_DOWNLOADED = 0;
    static final int LIST_TYPE_RUNNING = 1;
    static final int LIST_TYPE_SDCARD = 2;
    static final int LIST_TYPE_ALL = 3;
    static final int LIST_TYPE_DISABLED = 4; // rebestm - kk migration

    private boolean mShowBackground = false;

    private int mDefaultListType = -1;

    private ViewGroup mContentContainer;
    private View mRootView;
    private ViewPager mViewPager;
    private UserSpinnerAdapter mProfileSpinnerAdapter;
    private Context mContext;

    AlertDialog mResetDialog;

    // yonguk.kim 20120816 ICS Stuff Porting to JB - [LGP708g][4097] delete tags apk request TCL vendor
    private static String sTags = "com.android.apps.tag";

    class MyPagerAdapter extends PagerAdapter
            implements ViewPager.OnPageChangeListener {
        int mCurPos = 0;

        @Override
        public int getCount() {
            return mNumTabs; // rebestm - kk migration
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TabInfo tab = mTabs.get(position);
            View root = tab.build(mInflater, mContentContainer, mRootView);
            container.addView(root);
            root.setTag(R.id.name, tab); // rebestm - kk migration
            return root;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        // rebestm - kk migration
        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
            //return ((TabInfo)((View)object).getTag(R.id.name)).mListType;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).mLabel;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mCurPos = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                updateCurrentTab(mCurPos);
            }
        }
    }

    /*
     * Custom adapter implementation for the ListView
     * This adapter maintains a map for each displayed application and its properties
     * An index value on each AppInfo object indicates the correct position or index
     * in the list. If the list gets updated dynamically when the user is viewing the list of
     * applications, we need to return the correct index of position. This is done by mapping
     * the getId methods via the package name into the internal maps and indices.
     * The order of applications in the list is mirrored in mAppLocalList
     */
    static class ApplicationsAdapter extends BaseAdapter implements Filterable,
            ApplicationsState.Callbacks, AbsListView.RecyclerListener {
        private final ApplicationsState mState;
        private final ApplicationsState.Session mSession;
        private final TabInfo mTab;
        private final Context mContext;
        private final ArrayList<View> mActive = new ArrayList<View>();
        private final int mFilterMode;
        private ArrayList<ApplicationsState.AppEntry> mBaseEntries;
        private ArrayList<ApplicationsState.AppEntry> mEntries;
        private boolean mResumed;
        private int mLastSortMode = -1;
        //private boolean mWaitingForData;
        private int mWhichSize = SIZE_TOTAL;
        CharSequence mCurFilterPrefix;

        private Filter mFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<ApplicationsState.AppEntry> entries
                = applyPrefixFilter(constraint, mBaseEntries);
                FilterResults fr = new FilterResults();
                fr.values = entries;
                fr.count = entries.size();
                return fr;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mCurFilterPrefix = constraint;
                mEntries = (ArrayList<ApplicationsState.AppEntry>)results.values;
                notifyDataSetChanged();
                mTab.updateStorageUsage();
            }
        };

        public ApplicationsAdapter(ApplicationsState state, TabInfo tab, int filterMode) {
            mState = state;
            mSession = state.newSession(this);
            mTab = tab;
            mContext = tab.mOwner.getActivity();
            mFilterMode = filterMode;
        }

        public void resume(int sort) {
            Log.i(TAG, "Resume!  mResumed= " + mResumed);

            java.text.DateFormat NewDateFormat = DateFormat.getDateFormat(mContext);
            SharedPreferences preferences = mContext.getSharedPreferences(APPS_PREFS, 0);
            String OldDateFormat = preferences.getString(APPS_PREFS_DATE_FORMAT, null);
            String isDataUpdate = preferences.getString(APPS_PREFS_UPDATE_DATA, null);

            if (Utils.isUI_4_1_model(mContext)
                    && (Utils.isWifiOnly(mContext) || Utils.isTablet())) {
                mTab.mListView.bringToFront();
            }
            if (getCount() == 0
                    && (Utils.isWifiOnly(mContext) || Utils.isTablet())
                    && "VZW".equals(Config.getOperator())) {
                Log.d(TAG, "adapter resume, item count 0");
                mTab.mEmptyView.setVisibility(View.VISIBLE);
            }
            if ((OldDateFormat != null && !NewDateFormat.toString().equals(OldDateFormat))
                    || (isDataUpdate != null
                    && isDataUpdate.toString().equals(APPS_PREFS_UPDATE_DATA_YES))) {
                preferences.edit().putString(APPS_PREFS_UPDATE_DATA, APPS_PREFS_UPDATE_DATA_NO)
                        .commit();
                Log.i(TAG, "Update List!!!");
                mResumed = true;
                mSession.resume(true);
                mLastSortMode = sort;
                if (getCount() > 0
                        && (Utils.isWifiOnly(mContext) || Utils.isTablet())
                        && "VZW".equals(Config.getOperator())) {
                    Log.i(TAG, "adapter resume, item count 1 upper");
                    mTab.mEmptyView.setVisibility(View.INVISIBLE);
                    mTab.mListView.bringToFront();
                }
                rebuild(true);
                return;
            }

            if (!mResumed) {
                mResumed = true;
                mSession.resume(false);
                mLastSortMode = sort;
                rebuild(true);
            } else {
                rebuild(sort);
            }
        }

        public void pause() {
            java.text.DateFormat OldDateFormat = DateFormat.getDateFormat(mContext);
            String OldDateFormatString = OldDateFormat.toString();
            SharedPreferences preferences = mContext.getSharedPreferences(APPS_PREFS, 0);
            preferences.edit().putString(APPS_PREFS_DATE_FORMAT, OldDateFormatString).commit();

            if (mResumed) {
                mResumed = false;
                mSession.pause();
            }
        }

        public void release() {
            mSession.release();
        }

        public void rebuild(int sort) {
            if (sort == mLastSortMode) {
                return;
            }
            mLastSortMode = sort;
            rebuild(true);
        }

        public void rebuild(boolean eraseold) {
            Log.i(TAG, "Rebuilding app list...");
            ApplicationsState.AppFilter filterObj;
            Comparator<AppEntry> comparatorObj;
            boolean emulated = !Utils.isLGExternalInfoSupport(mContext); //Environment.isExternalStorageEmulated() || Utils.iSLGExternalInfoSupport();
            if (emulated) {
                mWhichSize = SIZE_TOTAL;
            } else {
                mWhichSize = SIZE_INTERNAL;
            }
            switch (mFilterMode) {
            case FILTER_APPS_THIRD_PARTY:
                filterObj = ApplicationsState.THIRD_PARTY_FILTER;
                break;
            case FILTER_APPS_SDCARD:
                filterObj = ApplicationsState.ON_SD_CARD_FILTER;
                if (!emulated) {
                    mWhichSize = SIZE_EXTERNAL;
                }
                break;
            case FILTER_APPS_DISABLED:
                filterObj = ApplicationsState.DISABLED_FILTER; // rebestm - kk migration
                break;
            default:
                filterObj = ApplicationsState.ALL_ENABLED_FILTER; // rebestm - kk migration
                break;
            }
            switch (mLastSortMode) {
            case SORT_ORDER_SIZE:
                switch (mWhichSize) {
                case SIZE_INTERNAL:
                    comparatorObj = ApplicationsState.INTERNAL_SIZE_COMPARATOR;
                    break;
                case SIZE_EXTERNAL:
                    comparatorObj = ApplicationsState.EXTERNAL_SIZE_COMPARATOR;
                    break;
                default:
                    comparatorObj = ApplicationsState.SIZE_COMPARATOR;
                    break;
                }
                break;
            //rebestm
            case SORT_ORDER_ALPHA:
                comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
                break;
            case SORT_ORDER_DATE:
                comparatorObj = ApplicationsState.DATE_COMPARATOR;
                break;
            default:
                comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
                break;
            }
            ArrayList<ApplicationsState.AppEntry> entries = mSession.rebuild(filterObj,
                    comparatorObj);
            if (entries == null && !eraseold) {
                // Don't have new list yet, but can continue using the old one.
                return;
            }
            mBaseEntries = entries;
            if (mBaseEntries != null) {
                mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
            } else {
                mEntries = null;
            }
            notifyDataSetChanged();
            mTab.updateStorageUsage();

            if (entries == null) {
                //mWaitingForData = true;
                mTab.mListContainer.setVisibility(View.INVISIBLE);
                mTab.mLoadingContainer.setVisibility(View.VISIBLE);
            } else {
                mTab.mListContainer.setVisibility(View.VISIBLE);
                mTab.mLoadingContainer.setVisibility(View.GONE);
            }
        }

        ArrayList<ApplicationsState.AppEntry> applyPrefixFilter(CharSequence prefix,
                ArrayList<ApplicationsState.AppEntry> origEntries) {

            // yonguk.kim 20120816 ICS Stuff Porting to JB - Apply Resource Array
            HashSet<String> hidden_apps = Utils.getHiddenApps(mContext);
            if ((Config.TCL).equals(Config.getOperator())) {
                hidden_apps.add(sTags);
            }
            if ("SPR".equals(Config.getOperator()) || "BM".equals(Config.getOperator())) {
                Utils.checkChameleon();
            }

            Bundle bundle = null;
            boolean hidden_flag = false;

            if (prefix == null || prefix.length() == 0) {
                ArrayList<ApplicationsState.AppEntry> newEntries = new ArrayList<ApplicationsState.AppEntry>();

                for (int i = 0; i < origEntries.size(); i++) {
                    ApplicationsState.AppEntry entry = origEntries.get(i);

                    try {
                        ApplicationInfo info = mContext.getPackageManager()
                                .getApplicationInfo(entry.info.packageName,
                                        PackageManager.GET_META_DATA);
                        bundle = info.metaData;
                        hidden_flag = bundle.getBoolean("com.lge.settings.hidden_app", false);

                    } catch (NullPointerException e) {
                        hidden_flag = false;
                    } catch (NameNotFoundException e) {
                        hidden_flag = false;
                    }
                    if (entry != null
                            && entry.info != null
                            && hidden_apps.size() > 0
                            && hidden_apps.contains(entry.info.packageName) == false
                            && hidden_flag == false) {
                        newEntries.add(entry);
                    }
                }
                return newEntries;
            } else {
                String prefixStr = ApplicationsState.normalize(prefix.toString());
                final String spacePrefixStr = " " + prefixStr;
                ArrayList<ApplicationsState.AppEntry> newEntries = new ArrayList<ApplicationsState.AppEntry>();
                for (int i = 0; i < origEntries.size(); i++) {
                    ApplicationsState.AppEntry entry = origEntries.get(i);
                    String nlabel = entry.getNormalizedLabel();
                    try {
                        ApplicationInfo info = mContext.getPackageManager()
                                .getApplicationInfo(entry.info.packageName,
                                        PackageManager.GET_META_DATA);
                        bundle = info.metaData;
                        hidden_flag = bundle.getBoolean("com.lge.settings.hidden_app", false);

                        Log.d(TAG, "hidden flag = " + hidden_flag);
                    } catch (NullPointerException e) {
                        Log.d(TAG, "NullPointerException -> hidden flag false");
                        hidden_flag = false;
                    } catch (NameNotFoundException e) {
                        Log.d(TAG, "NameNotFoundException -> hidden flag false");
                        hidden_flag = false;
                    }
                    if (nlabel.startsWith(prefixStr) || nlabel.indexOf(spacePrefixStr) != -1) {
                        if (entry != null
                                && entry.info != null
                                && hidden_apps.size() > 0
                                && hidden_apps.contains(entry.info.packageName) == false
                                && hidden_flag == false) {
                            newEntries.add(entry);
                        }
                    }
                }
                return newEntries;
            }
        }

        @Override
        public void onRunningStateChanged(boolean running) {
            try {
                mTab.mOwner.getActivity().setProgressBarIndeterminateVisibility(running);
            } catch (NullPointerException e) {
                Log.d(TAG, "onRunningStateChanged");
            }

        }

        @Override
        public void onRebuildComplete(ArrayList<AppEntry> apps) {
            if (mTab.mLoadingContainer.getVisibility() == View.VISIBLE) {
                mTab.mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(
                        mContext, android.R.anim.fade_out));
                mTab.mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        mContext, android.R.anim.fade_in));
            }
            mTab.mListContainer.setVisibility(View.VISIBLE);
            mTab.mLoadingContainer.setVisibility(View.GONE);
            //mWaitingForData = false;
            mBaseEntries = apps;
            mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
            notifyDataSetChanged();
            mTab.updateStorageUsage();
        }

        @Override
        public void onPackageListChanged() {
            rebuild(false);
        }

        @Override
        public void onPackageIconChanged() {
            // We ensure icons are loaded when their item is displayed, so
            // don't care about icons loaded in the background.
        }

        @Override
        public void onPackageSizeChanged(String packageName) {
            for (int i = 0; i < mActive.size(); i++) {
                AppViewHolder holder = (AppViewHolder)mActive.get(i).getTag();
                if (holder.entry.info.packageName.equals(packageName)) {
                    synchronized (holder.entry) {
                        holder.updateSizeText(mTab.mInvalidSizeStr, mWhichSize, mLastSortMode);
                    }
                    if (holder.entry.info.packageName.equals(mTab.mOwner.mCurrentPkgName)
                            && mLastSortMode == SORT_ORDER_SIZE) {
                        // We got the size information for the last app the
                        // user viewed, and are sorting by size...  they may
                        // have cleared data, so we immediately want to resort
                        // the list with the new size to reflect it to the user.
                        rebuild(false);
                    }
                    mTab.updateStorageUsage();
                    return;
                }
            }
        }

        @Override
        public void onAllSizesComputed() {
            if (mLastSortMode == SORT_ORDER_SIZE) {
                rebuild(false);
            }
            mTab.updateStorageUsage();
        }

        public int getCount() {
            return mEntries != null ? mEntries.size() : 0;
        }

        public Object getItem(int position) {
            // yonguk.kim 20120816 ICS Stuff Porting to JB - Handle Null Pointer Exception
            if (position >= 0 && position < mEntries.size()) {
                return mEntries.get(position);
            } else {
                return null;
            }
        }

        public ApplicationsState.AppEntry getAppEntry(int position) {
            // yonguk.kim 20120816 ICS Stuff Porting to JB - Handle Null Pointer Exception
            if (position >= 0 && position < mEntries.size()) {
                return mEntries.get(position);
            } else {
                return null;
            }
        }

        public long getItemId(int position) {
            // yonguk.kim 20120816 ICS Stuff Porting to JB - Handle Null Pointer Exception
            if (position >= 0 && position < mEntries.size()) {
                return mEntries.get(position).id;
            } else {
                return 0;
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unnecessary calls
            // to findViewById() on each row.
            AppViewHolder holder = AppViewHolder.createOrRecycle(mTab.mInflater, convertView);

            if (holder != null) {
                convertView = holder.rootView;
                TextView app_name = (TextView)convertView.findViewById(R.id.app_name);
                if (!Utils.isUI_4_1_model(mContext)) {
                    app_name.setTypeface(null, Typeface.BOLD);
                }
            } else {
                Log.e(TAG, " holder is null!!! ");
                return null;
            }

            // Bind the data efficiently with the holder
            ApplicationsState.AppEntry entry = mEntries.get(position);
            synchronized (entry) {
                holder.entry = entry;
                if (entry.label != null) {
                    holder.appName.setText(entry.label);
                    /*
                    holder.appName.setTextColor(mContext.getResources().getColorStateList(
                            entry.info.enabled ? android.R.color.primary_text_dark
                                    : android.R.color.secondary_text_dark));
                    */
                }
                mState.ensureIcon(entry);
                if (entry.icon != null) {
                    holder.appIcon.setImageDrawable(entry.icon);
                }
                holder.updateSizeText(mTab.mInvalidSizeStr, mWhichSize, mLastSortMode);
                if ((entry.info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
                    holder.disabled.setVisibility(View.VISIBLE);
                    holder.disabled.setText(R.string.not_installed);
                } else if (!entry.info.enabled) {
                    holder.disabled.setVisibility(View.VISIBLE);
                    holder.disabled.setText(R.string.disabled);
                } else {
                    holder.disabled.setVisibility(View.GONE);
                }
                if (mFilterMode == FILTER_APPS_SDCARD) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    holder.checkBox.setChecked((entry.info.flags
                            & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0);
                } else {
                    holder.checkBox.setVisibility(View.GONE);
                }
            }
            mActive.remove(convertView);
            mActive.add(convertView);
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        @Override
        public void onMovedToScrapHeap(View view) {
            mActive.remove(view);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mContext = getActivity();
        if (!SettingsBreadCrumb.isAttached(getActivity())) {
            Log.d("YSY", "ManageApplications, else");
            getActivity().setTitle(R.string.applications_settings);

            if ("VZW".equals(Config.getOperator())) {
                Log.d("YSY", "ManageApplications, VZW");
                getActivity().setTitle(R.string.applications_settings_title);
            } else if ("LRA".equals(Config.getOperator())) {
                Log.d("YSY", "ManageApplications, LRA");
                getActivity().setTitle(R.string.applications_settings);
            }
        }

        mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        int defaultListType = LIST_TYPE_DOWNLOADED;
        String className = getArguments() != null
                ? getArguments().getString("classname") : null;
        if (className == null) {
            className = intent.getComponent().getClassName();
        }
        if (className.equals(RunningServicesActivity.class.getName())
                || className.endsWith(".RunningServices")
                || (getActivity() instanceof PreferenceActivity) == false) {
            defaultListType = LIST_TYPE_RUNNING;
        } else if (className.equals(StorageUseActivity.class.getName())
                || Intent.ACTION_MANAGE_PACKAGE_STORAGE.equals(action)
                || className.endsWith(".StorageUse")) {
            mSortOrder = SORT_ORDER_SIZE;
            defaultListType = LIST_TYPE_ALL;
        } else if (android.provider.Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS.equals(action)) {
            // Select the all-apps list, with the default sorting
            defaultListType = LIST_TYPE_ALL;
        }

        if (savedInstanceState != null) {
            mSortOrder = savedInstanceState.getInt(EXTRA_SORT_ORDER, mSortOrder);
            int tmp = savedInstanceState.getInt(EXTRA_DEFAULT_LIST_TYPE, -1);
            if (tmp != -1) {
                defaultListType = tmp;
            }
            mShowBackground = savedInstanceState.getBoolean(EXTRA_SHOW_BACKGROUND, false);
        }

        mDefaultListType = defaultListType;

        final Intent containerIntent = new Intent().setComponent(
                new ComponentName(
                        "com.android.defcontainer",
                        "com.android.defcontainer.DefaultContainerService"));
        getActivity().bindService(containerIntent, mContainerConnection, Context.BIND_AUTO_CREATE);

        mInvalidSizeStr = getActivity().getText(R.string.invalid_size_value);
        //mComputingSizeStr = getActivity().getText(R.string.computing_size);

        TabInfo tab = new TabInfo(this, mApplicationsState,
                getActivity().getString(R.string.filter_apps_third_party),
                LIST_TYPE_DOWNLOADED, this, savedInstanceState);
        tabinfo = tab;

        mTabs.clear(); // 20131114 yonguk.kim Initialization before adding the TabInfo item

        mTabs.add(tab);

        if (Utils.isLGExternalInfoSupport(getActivity())) { //if (!Environment.isExternalStorageEmulated() 
            // yonguk.kim 20120816 ICS Stuff Porting to JB - change to emmc model supported token
            String label = getActivity().getString(R.string.filter_apps_onsdcard);
            if (Utils.supportInternalMemory() && !Utils.isLGExternalInfoSupport(getActivity())) {
                label = getActivity().getString(R.string.sp_settings_apps_internal_memory_NORMAL);
            }

            tab = new TabInfo(this, mApplicationsState,
                    label,
                    LIST_TYPE_SDCARD, this, savedInstanceState);
            mTabs.add(tab);
        }

        tab = new TabInfo(this, mApplicationsState,
                getActivity().getString(R.string.filter_apps_running),
                LIST_TYPE_RUNNING, this, savedInstanceState);
        mTabs.add(tab);

        tab = new TabInfo(this, mApplicationsState,
                getActivity().getString(R.string.settings_apps_tap_all),
                LIST_TYPE_ALL, this, savedInstanceState);
        mTabs.add(tab);
        // rebestm - kk migration
        tab = new TabInfo(this, mApplicationsState,
                getActivity().getString(R.string.disabled),
                LIST_TYPE_DISABLED, this, savedInstanceState);
        mTabs.add(tab);

        // rebestm - kk migration
        mNumTabs = mTabs.size();

        final UserManager um = (UserManager)mContext.getSystemService(Context.USER_SERVICE);
        mProfileSpinnerAdapter = Utils.createUserSpinnerAdapter(um, mContext);

        batteryLevel = null;
        gv = (AppMultiDeleteGlobalVariable)this.getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // initialize the inflater
        mInflater = inflater;

        View rootView = mInflater.inflate(R.layout.manage_applications_content,
                container, false);
        mContentContainer = container;
        mRootView = rootView;

        mViewPager = (ViewPager)rootView.findViewById(R.id.pager);
        MyPagerAdapter adapter = new MyPagerAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(adapter);
        PagerTabStrip tabs = (PagerTabStrip)rootView.findViewById(R.id.tabs);
        tabs.setTabIndicatorColorResource(R.color.theme_accent);

        // We have to do this now because PreferenceFrameLayout looks at it
        // only when the view is added.
        if (container instanceof PreferenceFrameLayout) {
            ((PreferenceFrameLayout.LayoutParams)rootView.getLayoutParams()).removeBorders = true;
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean(EXTRA_RESET_DIALOG)) {
            buildResetDialog();
        }

        if (savedInstanceState == null) {
            // First time init: make sure view pager is showing the correct tab.
            int extraCurrentListType = getActivity().getIntent().getIntExtra(EXTRA_LIST_TYPE,
                    LIST_TYPE_MISSING);
            int currentListType = (extraCurrentListType != LIST_TYPE_MISSING)
                    ? extraCurrentListType : mDefaultListType;
            for (int i = 0; i < mNumTabs; i++) {
                TabInfo tab = mTabs.get(i);
                if (tab.mListType == currentListType) {
                    mViewPager.setCurrentItem(i);
                    break;
                }
            }
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("kjo", "resume() = " + mViewPager.getCurrentItem());
        mActivityResumed = true;
        updateCurrentTab(mViewPager.getCurrentItem());
        updateNumTabs(); // rebestm - kk migration
        updateOptionsMenu();
        Dialog mDialog = Pickerdialog;
        if (mDialog != null) {
            if (mDialog instanceof SUIDrumDatePickerDialog) {
                ((SUIDrumDatePickerDialog)mDialog).getDatePicker().refresh();
            } else if (mDialog instanceof SUIDrumTimePickerDialog) {
                ((SUIDrumTimePickerDialog)mDialog).getTimePicker().refresh();
            }
        }

        IntentFilter Batteryfilter = new IntentFilter();
        Batteryfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(mBatteryReceiver, Batteryfilter);

        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                if ("VZW".equals(Config.getOperator())) {
                    mBreadCrumb.setTitle(Utils.getResources().getString(
                            R.string.applications_settings_title));
                } else if ("LRA".equals(Config.getOperator())) {
                    mBreadCrumb.setTitle(Utils.getResources().getString(
                            R.string.applications_settings));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SORT_ORDER, mSortOrder);
        if (mDefaultListType != -1) {
            outState.putInt(EXTRA_DEFAULT_LIST_TYPE, mDefaultListType);
        }
        outState.putBoolean(EXTRA_SHOW_BACKGROUND, mShowBackground);
        if (mResetDialog != null) {
            outState.putBoolean(EXTRA_RESET_DIALOG, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivityResumed = false;
        for (int i = 0; i < mTabs.size(); i++) {
            mTabs.get(i).pause();
        }
        getActivity().unregisterReceiver(mBatteryReceiver);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.setTitle(null);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // We are going to keep the tab data structures around, but they
        // are no longer attached to their view hierarchy.
        for (int i = 0; i < mTabs.size(); i++) {
            mTabs.get(i).detachView();
            mTabs.get(i).release();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALLED_APP_DETAILS && mCurrentPkgName != null) {
            mApplicationsState.requestSize(mCurrentPkgName);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        UserHandle selectedUser = mProfileSpinnerAdapter.getUserHandle(position);
        if (selectedUser.getIdentifier() != UserHandle.myUserId()) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int currentTab = mViewPager.getCurrentItem();
            intent.putExtra(EXTRA_LIST_TYPE, mTabs.get(currentTab).mListType);
            mContext.startActivityAsUser(intent, selectedUser);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Nothing to do
    }

    private void updateNumTabs() {
        int newNum = mApplicationsState.haveDisabledApps() ? mTabs.size() : (mTabs.size() - 1);
        if (newNum != mNumTabs) {
            mNumTabs = newNum;
            if (mViewPager != null) {
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    TabInfo tabForType(int type) {
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tab = mTabs.get(i);
            if (tab.mListType == type) {
                return tab;
            }
        }
        return null;
    }

    // utility method used to start sub activity
    private void startApplicationDetailsActivity() {
        // start new fragment to display extended information
        Bundle args = new Bundle();
        args.putString(InstalledAppDetails.ARG_PACKAGE_NAME, mCurrentPkgName);

        // 20131112 yonguk.kim Apply UsageManager for ATT
        if (getActivity() instanceof PreferenceActivity) {
            PreferenceActivity pa = (PreferenceActivity)getActivity();
            pa.startPreferencePanel(InstalledAppDetails.class.getName(), args,
                    R.string.application_info_label, null, this, INSTALLED_APP_DETAILS);
        } else {
            Intent intent = Utils.buildStartFragmentIntent(InstalledAppDetails.class.getName(),
                    args, R.string.application_info_label, 0, getActivity(),
                    R.drawable.shortcut_apps);
            this.startActivityForResult(intent, INSTALLED_APP_DETAILS);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mOptionsMenu = menu;
        // note: icons removed for now because the cause the new action
        // bar UI to be very confusing.
        // rebestm_popup
        menu.add(0, SORT_POPUP, 1, R.string.settings_apps_menu_sortby)
                //.setIcon(android.R.drawable.ic_menu_sort_alphabetically)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, SORT_ORDER_ALPHA, 2, R.string.sort_order_alpha)
                //.setIcon(android.R.drawable.ic_menu_sort_alphabetically)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, SORT_ORDER_SIZE, 3, R.string.sort_order_size)
                //.setIcon(android.R.drawable.ic_menu_sort_by_size)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, SHOW_RUNNING_SERVICES, 4, R.string.show_running_services)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, SHOW_BACKGROUND_PROCESSES, 4, R.string.show_background_processes)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, RESET_APP_PREFERENCES, 5, R.string.reset_app_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (Utils.supportDefaultApps(getActivity())) {
            menu.add(0, DEFAULT_APPS, 6, R.string.sp_reset_defaults_apps_NORMAL)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (Utils.supportUnusedApps(getActivity())) {
            menu.add(0, UNUSED_APPS, 7, R.string.unused_apps)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        // rebestm_popup
        menu.add(0, MULTIDELETE_APPS, 8, R.string.settings_apps_uninstall_apps)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, RECOVER_APPS, 9, R.string.settings_apps_recover_apps)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, SPECIFIC_DATE, 10, R.string.settings_apps_specific_date2)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        updateOptionsMenu();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        updateOptionsMenu();
    }

    @Override
    public void onDestroyOptionsMenu() {
        mOptionsMenu = null;
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(mContainerConnection);
        super.onDestroy();
    }

    void updateOptionsMenu() {
        if (mOptionsMenu == null) {
            return;
        }

        /*
         * The running processes screen doesn't use the mApplicationsAdapter
         * so bringing up this menu in that case doesn't make any sense.
         */
        if (mCurTab != null && mCurTab.mListType == LIST_TYPE_RUNNING) {
            TabInfo tab = tabForType(LIST_TYPE_RUNNING);
            boolean showingBackground = tab != null && tab.mRunningProcessesView != null
                    ? tab.mRunningProcessesView.mAdapter.getShowBackground() : false;
            mOptionsMenu.findItem(SORT_ORDER_ALPHA).setVisible(false);
            mOptionsMenu.findItem(SORT_ORDER_SIZE).setVisible(false);
            mOptionsMenu.findItem(SHOW_RUNNING_SERVICES).setVisible(showingBackground);
            mOptionsMenu.findItem(SHOW_BACKGROUND_PROCESSES).setVisible(!showingBackground);
            mOptionsMenu.findItem(RESET_APP_PREFERENCES).setVisible(false);
            mShowBackground = showingBackground;
            if (mOptionsMenu.findItem(UNUSED_APPS) != null) {
                mOptionsMenu.findItem(UNUSED_APPS).setVisible(false);
            }
            if (mOptionsMenu.findItem(DEFAULT_APPS) != null) {
                mOptionsMenu.findItem(DEFAULT_APPS).setVisible(false);
            }
            mOptionsMenu.findItem(SORT_POPUP).setVisible(false);
            mOptionsMenu.findItem(MULTIDELETE_APPS).setVisible(false);
            mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
            mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(false);
        }
        else if ((mCurTab != null) && (mCurTab.mListType == LIST_TYPE_DOWNLOADED)) {
            if (this.tabinfo.mOwner.isMultiDeleteSupport()) {
                if (this.tabinfo.mApplications != null && this.tabinfo.mApplications.getCount() > 0) {
                    mOptionsMenu.findItem(SORT_POPUP).setVisible(true);
                    if (gv.getIsTherad()) {
                        mOptionsMenu.findItem(MULTIDELETE_APPS).setVisible(false);
                    } else {
                        mOptionsMenu.findItem(MULTIDELETE_APPS).setVisible(true);
                    }
                    if (isSafeMode(getActivity())) {
                        if (gv.getIsTherad()) {
                            mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(false);
                        } else {
                            mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(true);
                        }
                        mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
                    } else {
                        if (isRestoreMenu() && !gv.getIsTherad()) {
                            mOptionsMenu.findItem(RECOVER_APPS).setVisible(true);
                        } else {
                            mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
                        }
                        mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(false);
                    }
                    if ((Utils.isWifiOnly(getActivity()) || Utils.isTablet())
                            && "VZW".equals(Config.getOperator())) {
                        Log.d(TAG, "1111 items 1 upper");
                        tabinfo.mEmptyView.setVisibility(View.INVISIBLE);
                        tabinfo.mListView.bringToFront();
                    }
                } else if (this.tabinfo.mApplications != null && this.tabinfo.mApplications.getCount() == 0 && isRestoreMenu()) {
                    Log.i(TAG, "1111this.tabinfo.mApplications.getCount() = "
                            + this.tabinfo.mApplications.getCount());
                    if ((Utils.isWifiOnly(getActivity()) || Utils.isTablet())
                            && "VZW".equals(Config.getOperator())) {
                        Log.d(TAG, "1111 items 0");
                        tabinfo.mEmptyView.setVisibility(View.VISIBLE);
                    }
                    if (isSafeMode(getActivity())) {
                        mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
                    } else {
                        if (!gv.getIsTherad()) {
                            mOptionsMenu.findItem(RECOVER_APPS).setVisible(true);
                        } else {
                            mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
                        }
                    }
                    mOptionsMenu.findItem(SORT_POPUP).setVisible(false);
                    mOptionsMenu.findItem(MULTIDELETE_APPS).setVisible(false);
                    mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(false);
                } else {
                    Log.i(TAG, "2222this.tabinfo.mApplications.getCount() = "
                            + this.tabinfo.mApplications.getCount());
                    if ((Utils.isWifiOnly(getActivity()) || Utils.isTablet())
                            && "VZW".equals(Config.getOperator())) {
                        if (this.tabinfo.mApplications.getCount() > 0) {
                            Log.d(TAG, "1111 items 1 upper");
                            tabinfo.mEmptyView.setVisibility(View.INVISIBLE);
                            tabinfo.mListView.bringToFront();
                        } else {
                            Log.d(TAG, "2222 items 0 ");
                            tabinfo.mEmptyView.setVisibility(View.VISIBLE);
                        }
                    }
                    mOptionsMenu.findItem(SORT_POPUP).setVisible(false);
                    mOptionsMenu.findItem(MULTIDELETE_APPS).setVisible(false);
                    mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
                    mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(false);

                }
                mOptionsMenu.findItem(SORT_ORDER_ALPHA).setVisible(false);
                mOptionsMenu.findItem(SORT_ORDER_SIZE).setVisible(false);
                mOptionsMenu.findItem(SHOW_RUNNING_SERVICES).setVisible(false);
                mOptionsMenu.findItem(SHOW_BACKGROUND_PROCESSES).setVisible(false);
                mOptionsMenu.findItem(RESET_APP_PREFERENCES).setVisible(true);
                if (mOptionsMenu.findItem(UNUSED_APPS) != null) {
                    mOptionsMenu.findItem(UNUSED_APPS).setVisible(true);
                }
                if (mOptionsMenu.findItem(DEFAULT_APPS) != null) {
                    mOptionsMenu.findItem(DEFAULT_APPS).setVisible(true);
                }
            } else {
                mOptionsMenu.findItem(SORT_ORDER_ALPHA).setVisible(
                        mSortOrder != SORT_ORDER_ALPHA);
                mOptionsMenu.findItem(SORT_ORDER_SIZE).setVisible(
                        mSortOrder != SORT_ORDER_SIZE);
                mOptionsMenu.findItem(SHOW_RUNNING_SERVICES).setVisible(false);
                mOptionsMenu.findItem(SHOW_BACKGROUND_PROCESSES).setVisible(false);
                mOptionsMenu.findItem(RESET_APP_PREFERENCES).setVisible(true);
                if (mOptionsMenu.findItem(UNUSED_APPS) != null) {
                    mOptionsMenu.findItem(UNUSED_APPS).setVisible(true);
                }
                if (mOptionsMenu.findItem(DEFAULT_APPS) != null) {
                    mOptionsMenu.findItem(DEFAULT_APPS).setVisible(true);
                }
                mOptionsMenu.findItem(SORT_POPUP).setVisible(false);
                mOptionsMenu.findItem(MULTIDELETE_APPS).setVisible(false);
                mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
                mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(false);
            }
        }
        else {
            Log.d(TAG, "updateOptionsMenu(), !isMultiDeleteSupport()");
            if ((Utils.isWifiOnly(getActivity()) || Utils.isTablet())) {
                if (this.tabinfo.mApplications != null && this.tabinfo.mApplications.getCount() > 0) {
                    Log.d(TAG, "updateOptionsMenu(), !isMultiDeleteSupport(), items 1 upper");
                    tabinfo.mEmptyView.setVisibility(View.INVISIBLE);
                    tabinfo.mListView.bringToFront();
                } else {
                    Log.d(TAG, "updateOptionsMenu(), !isMultiDeleteSupport(), items 0");

                    if (tabinfo != null && tabinfo.mEmptyView != null) {
                        tabinfo.mEmptyView.setVisibility(View.VISIBLE);
                    }
                }
            }
            mOptionsMenu.findItem(SORT_ORDER_ALPHA).setVisible(mSortOrder != SORT_ORDER_ALPHA);
            mOptionsMenu.findItem(SORT_ORDER_SIZE).setVisible(mSortOrder != SORT_ORDER_SIZE);
            mOptionsMenu.findItem(SHOW_RUNNING_SERVICES).setVisible(false);
            mOptionsMenu.findItem(SHOW_BACKGROUND_PROCESSES).setVisible(false);
            mOptionsMenu.findItem(RESET_APP_PREFERENCES).setVisible(true);
            if (mOptionsMenu.findItem(UNUSED_APPS) != null) {
                mOptionsMenu.findItem(UNUSED_APPS).setVisible(true);
            }
            if (mOptionsMenu.findItem(DEFAULT_APPS) != null) {
                mOptionsMenu.findItem(DEFAULT_APPS).setVisible(true);
            }
            //rebestm_popup
            mOptionsMenu.findItem(SORT_POPUP).setVisible(false);
            mOptionsMenu.findItem(MULTIDELETE_APPS).setVisible(false);
            mOptionsMenu.findItem(RECOVER_APPS).setVisible(false);
            mOptionsMenu.findItem(SPECIFIC_DATE).setVisible(false);
        }
        // 3LM_MDM_DCM_START jihun.im@lge.com 20121015
        // If 3LM is an active admin, do not allow user to reset apps
        if (com.android.settings.lgesetting.Config.Config.THREELM_MDM) {
            DevicePolicyManager dm =
                    (DevicePolicyManager)getActivity().getSystemService(
                            Context.DEVICE_POLICY_SERVICE);
            List<ComponentName> admins = dm.getActiveAdmins();
            if (admins == null) {
                return;
            }
            for (ComponentName comp : admins) {
                if (comp.getPackageName().startsWith("com.threelm")) {
                    mOptionsMenu.findItem(RESET_APP_PREFERENCES).setVisible(false);
                    break;
                }
            }
        }
        // 3LM_MDM_DCM_END jihun.im@lge.com 20121015
    }

    void buildResetDialog() {
        if (mResetDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.reset_app_preferences_title);
            builder.setMessage(R.string.reset_app_preferences_desc);
            builder.setPositiveButton(R.string.def_yes_btn_caption, this);
            builder.setNegativeButton(R.string.def_no_btn_caption, null);
            if (!Utils.isUI_4_1_model(getActivity())) {
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
            }
            mResetDialog = builder.show();
            mResetDialog.setOnDismissListener(this);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mResetDialog == dialog) {
            mResetDialog = null;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mResetDialog == dialog) {
            final PackageManager pm = getActivity().getPackageManager();
            // rebestm - kk migration
            //final IPackageManager mIPm = IPackageManager.Stub.asInterface(
            //        ServiceManager.getService("package"));
            final INotificationManager nm = INotificationManager.Stub.asInterface(
                    ServiceManager.getService(Context.NOTIFICATION_SERVICE));
            final NetworkPolicyManager npm = NetworkPolicyManager.from(getActivity());
            // rebestm - kk migration
            final AppOpsManager aom = (AppOpsManager)getActivity().getSystemService(
                    Context.APP_OPS_SERVICE);
            final Handler handler = new Handler(getActivity().getMainLooper());
            (new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    List<ApplicationInfo> apps = pm.getInstalledApplications(
                            PackageManager.GET_DISABLED_COMPONENTS);
                    for (int i = 0; i < apps.size(); i++) {
                        ApplicationInfo app = apps.get(i);
                        //rebestm - kk migration
                        try {
                            if (DEBUG) {
                                Log.v(TAG, "Enabling notifications: " + app.packageName);
                            }
                            nm.setNotificationsEnabledForPackage(app.packageName, app.uid, true);
                        } catch (android.os.RemoteException ex) {
                            Log.w(TAG, "android.os.RemoteException");
                        }

                        if (DEBUG) {
                            Log.v(TAG, "Clearing preferred: " + app.packageName);
                        }
                        if ("com.android.settings".equals(app.packageName)
                                || "com.lge.settings.easy".equals(app.packageName)) {
                            ;
                        } else {
                            pm.clearPackagePreferredActivities(app.packageName);
                        }
                        if (!app.enabled) {
                            if (DEBUG) {
                                Log.v(TAG, "Enabling app: " + app.packageName);
                            }
                            if (pm.getApplicationEnabledSetting(app.packageName)
                            == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                                pm.setApplicationEnabledSetting(app.packageName,
                                        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                        PackageManager.DONT_KILL_APP);
                            }
                        }
                    }
                    // We should have cleared all of the preferred apps above;
                    // just in case some may be lingering, retrieve whatever is
                    // still set and remove it.
                    ArrayList<IntentFilter> filters = new ArrayList<IntentFilter>();
                    ArrayList<ComponentName> prefActivities = new ArrayList<ComponentName>();
                    pm.getPreferredActivities(filters, prefActivities, null);
                    for (int i = 0; i < prefActivities.size(); i++) {
                        if (DEBUG) {
                            Log.v(TAG, "Clearing preferred: "
                                    + prefActivities.get(i).getPackageName());
                        }
                        if ("com.android.settings".equals(prefActivities.get(i).getPackageName())
                                || "com.lge.settings.easy".equals(prefActivities.get(i)
                                        .getPackageName())) {
                            ;
                        } else {
                            pm.clearPackagePreferredActivities(prefActivities.get(i)
                                    .getPackageName());
                        }
                    }
                    //rebestm - kk migration -> not used,  above On the process.
                    //                    try {
                    //                        mIPm.resetPreferredActivities(UserHandle.myUserId());
                    //                    } catch (RemoteException e) {
                    //                    }
                    aom.resetAllModes();
                    final int[] restrictedUids = npm.getUidsWithPolicy(
                            POLICY_REJECT_METERED_BACKGROUND);
                    final int currentUserId = ActivityManager.getCurrentUser();
                    for (int uid : restrictedUids) {
                        // Only reset for current user
                        if (UserHandle.getUserId(uid) == currentUserId) {
                            if (DEBUG) {
                                Log.v(TAG, "Clearing data policy: " + uid);
                            }
                            npm.setUidPolicy(uid, POLICY_NONE);
                        }
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (DEBUG) {
                                Log.v(TAG, "Done clearing");
                            }
                            if (getActivity() != null && mActivityResumed) {
                                if (DEBUG) {
                                    Log.v(TAG, "Updating UI!");
                                }
                                for (int i = 0; i < mTabs.size(); i++) {
                                    TabInfo tab = mTabs.get(i);
                                    if (tab.mApplications != null) {
                                        tab.mApplications.pause();
                                    }
                                }
                                if (mCurTab != null) {
                                    mCurTab.resume(mSortOrder);
                                }
                            }
                        }
                    });
                    return null;
                }
            }).execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if (menuId == SORT_POPUP) {
            showSortByDialog();
            return true;
        } else if ((menuId == SORT_ORDER_ALPHA) || (menuId == SORT_ORDER_SIZE)) {
            mSortOrder = menuId;
            if (mCurTab != null && mCurTab.mApplications != null) {
                mCurTab.mApplications.rebuild(mSortOrder);
            }
            return true; // yonguk.kim 20120816 ICS Stuff Porting to JB - Don't udpate options menu here
        } else if (menuId == SHOW_RUNNING_SERVICES) {
            mShowBackground = false;
            if (mCurTab != null && mCurTab.mRunningProcessesView != null) {
                mCurTab.mRunningProcessesView.mAdapter.setShowBackground(false);
            }
        } else if (menuId == SHOW_BACKGROUND_PROCESSES) {
            mShowBackground = true;
            if (mCurTab != null && mCurTab.mRunningProcessesView != null) {
                mCurTab.mRunningProcessesView.mAdapter.setShowBackground(true);
            }
        } else if (menuId == RESET_APP_PREFERENCES) {
            //            ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
            //            .showSoftInputUnchecked(0, null);
            buildResetDialog();
        } else if (menuId == UNUSED_APPS) {
            if (Utils.supportSplitView(getActivity())) {
                PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
                //Fragment f = Fragment.instantiate(preferenceActivity,
                //        AppCleanupSettingsFragment.class.getName());
                preferenceActivity.startPreferencePanel(AppCleanupSettingsFragment.class.getName(),
                        null, R.string.unused_apps, getString(R.string.unused_apps), null, 0);
                //preferenceActivity.startPreferenceFragment(f, true);
            } else {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.lge.appcleanup",
                        "com.lge.appcleanup.ui.settings.AppCleanupSettingsActivity"));
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("fromSettings", true);
                startActivity(intent);
            }
        } else if (menuId == DEFAULT_APPS) {
            Intent intent = new Intent("com.lge.settings.DEFAULT_APPS");
            startActivity(intent);
        }
        else if (menuId == MULTIDELETE_APPS) {
            setAppMultiDeleteGlobalVariable();
            Bundle args = new Bundle();
            args.putInt(EXTRA_SORT_ORDER, mSortOrder);
            PreferenceActivity pa = (PreferenceActivity)getActivity();
            pa.startPreferencePanel(AppMultiDelete.class.getName(), args,
                    R.string.application_info_label, null, this, INSTALLED_APP_DETAILS);
        } else if (menuId == RECOVER_APPS) {
            SharedPreferences preferences = getActivity().getSharedPreferences(APPS_PREFS, 0);
            int batterylevel = preferences.getInt(APPS_PREFS_BATTERY, 0);

            if (batterylevel < RECOVER_LIMITED_BATTERY) {
                Log.d(TAG, "batterylevel is " + batterylevel);
                Toast.makeText(getActivity(), R.string.settings_apps_battery_5under,
                        Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Recover apps go");
                setAppMultiDeleteGlobalVariable();
                Bundle args = new Bundle();
                args.putInt(EXTRA_SORT_ORDER, mSortOrder);
                PreferenceActivity pa = (PreferenceActivity)getActivity();
                pa.startPreferencePanel(RecoverList.class.getName(), args,
                        R.string.application_info_label, null, this, INSTALLED_APP_DETAILS);
            }
        } else if (menuId == SPECIFIC_DATE) {
            onPickerDialog(DIALOG_DATEPICKER);
            if (Pickerdialog != null) {
                Pickerdialog.show();
            }
        } else {
            // Handle the home button
            return false;
        }
        updateOptionsMenu();
        return true;
    }

    public void onItemClick(TabInfo tab, AdapterView<?> parent, View view, int position,
            long id) {
        if (tab.mApplications != null && tab.mApplications.getCount() > position) {
            ApplicationsState.AppEntry entry = tab.mApplications.getAppEntry(position);
            if (entry != null) { // yonguk.kim 20120816 ICS Stuff Porting to JB - WBT Issue
                mCurrentPkgName = entry.info.packageName;
            }
            startApplicationDetailsActivity();
        }
    }

    public void updateCurrentTab(int position) {
        TabInfo tab = mTabs.get(position);
        mCurTab = tab;

        // Put things in the correct paused/resumed state.
        if (mActivityResumed) {
            mCurTab.build(mInflater, mContentContainer, mRootView);
            mCurTab.resume(mSortOrder);
        } else {
            mCurTab.pause();
        }
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo t = mTabs.get(i);
            if (t != mCurTab) {
                t.pause();
            }
        }

        mCurTab.updateStorageUsage();
        updateOptionsMenu();
        final Activity host = getActivity();
        if (host != null) {
            host.invalidateOptionsMenu();
        }
    }

    private volatile IMediaContainerService mContainerService;

    private final ServiceConnection mContainerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mContainerService = IMediaContainerService.Stub.asInterface(service);
            for (int i = 0; i < mTabs.size(); i++) {
                mTabs.get(i).setContainerService(mContainerService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mContainerService = null;
        }
    };

    public void setAppMultiDeleteGlobalVariable() {
        gv.setApplicationsAdapter(this.tabinfo.mApplications);
        gv.setSession(this.tabinfo.mApplications.mSession);
        gv.setEntries(this.tabinfo.mApplications.mEntries);
        gv.setContext(this.tabinfo.mOwner.getActivity());
        gv.setPickCalendar(this.pickCalendar);
    }

    private void showSortByDialog() {
        Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.settings_apps_menu_sortby);
        builder.setSingleChoiceItems(R.array.sort_choose_entries,
                getSortMode(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int choose) {
                        Log.d(TAG, "showSortByDialog = " + choose);
                        if (choose == 0) {
                            mSortOrder = SORT_ORDER_ALPHA;
                        } else if (choose == 1) {
                            mSortOrder = SORT_ORDER_SIZE;
                        } else if (choose == 2) {
                            mSortOrder = SORT_ORDER_DATE;
                        }
                        if (mCurTab != null && mCurTab.mApplications != null) {
                            mCurTab.mApplications.rebuild(mSortOrder);
                        }
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        builder.show();
    }

    private int getSortMode() {
        int choose = -1;
        if (mSortOrder == SORT_ORDER_ALPHA) {
            choose = 0;
        } else if (mSortOrder == SORT_ORDER_SIZE) {
            choose = 1;
        } else if (mSortOrder == SORT_ORDER_DATE) {
            choose = 2;
        }
        Log.d(TAG, "getSortMode = " + choose);
        return choose;
    }

    public static boolean isSafeMode(Context mContext) {
        final PackageManager pm = mContext.getPackageManager();
        final boolean safeMode = pm.isSafeMode();
        Log.i(TAG, "safeMode = " + safeMode);
        if (safeMode && "VZW".equals(Config.getOperator())) {
            return true;
        } else {
            return false;
        }
        //        return true;
    }

    private boolean isMultiDeleteSupport() {
        return "VZW".equals(Config.getOperator());
    }

    public Dialog onPickerDialog(int id) {
        switch (id) {
        case DIALOG_DATEPICKER: {
            final Calendar calendar = Calendar.getInstance();
            SUIDrumDatePickerDialog mSUIDialog =
                    new SUIDrumDatePickerDialog(getActivity()
                            , this
                            , calendar.get(Calendar.YEAR)
                            , calendar.get(Calendar.MONTH) + 1
                            , calendar.get(Calendar.DAY_OF_MONTH)
                            , 0 /*com.lge.sui.widget.R.layout.sui_drum_date_picker_dialog*/
                            , 0 /*com.lge.sui.widget.R.id.datePicker*/
                            , false
                            , new SUIDrumDatePickerDialog.TitleBuilder() {
                                public String getTitle(Calendar c, int dateFormat) {
                                    return getString(R.string.date_time_set_date);
                                }
                            }
                    );
            mSUIDialog.getDatePicker().setStartEndYear(1970, 2036); // yw2.kim SE.142506 SUIDateDrumpicker range setting error. 120306
            mSUIDialog.setButton(getResources().getString(R.string.dlg_ok), mSUIDialog);
            Pickerdialog = mSUIDialog;
            break;
        }
        default:
            Pickerdialog = null;
            break;
        }
        return Pickerdialog;
    }

    @Override
    public void onDateSet(SUIDrumDatePicker view, int year, int month, int day) {
        Log.d(TAG, "year= " + year);
        Log.d(TAG, "month= " + month);
        Log.d(TAG, "day= " + day);
        pickCalendar = Calendar.getInstance();
        pickCalendar.set(Calendar.YEAR, year);
        pickCalendar.set(Calendar.MONTH, month - 1);
        pickCalendar.set(Calendar.DAY_OF_MONTH, day);
        setAppMultiDeleteGlobalVariable();
        Bundle args = new Bundle();
        args.putInt(EXTRA_SORT_ORDER, mSortOrder);
        args.putBoolean(EXTRA_PICKER, true);
        PreferenceActivity pa = (PreferenceActivity)getActivity();
        pa.startPreferencePanel(AppMultiDelete.class.getName(), args,
                R.string.application_info_label, null, this, INSTALLED_APP_DETAILS);
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int value = level * 100 / scale;

                batteryLevel = Utils.getBatteryPercentage(intent);

                SharedPreferences preferences = getActivity().getSharedPreferences(APPS_PREFS, 0);
                preferences.edit().putInt(APPS_PREFS_BATTERY, value).commit();
            }
        }
    };

    private boolean isRestoreMenu() {
        Uri contentUri = Uri.parse(EnumManager.UriType.APK_LIST_URI.getValue());
        Cursor cursor = null;

        StringBuffer sbCondition = new StringBuffer();
        sbCondition.append(EnumManager.DBColumnType.COLUMN_STATUS.getValue()).append(" = ? ");
        String[] selectionArgs = { EnumManager.ApkStatus.BACKUP_SUCCESS.getValue() };

        cursor = getActivity().getContentResolver().query(contentUri, // URI
                null, // return columns
                sbCondition.toString(), // where condition
                selectionArgs, // where values
                "_id ASC"); // ord

        if (null == cursor
                || (cursor.getCount() <= 0)
                || (isEnableCheckPackage(getActivity(), RECOVER_APP_NAME)) == false
                || (gv.getIsTherad() == true)) {
            Log.d(TAG, "isEnableCheckPackage(getActivity(), RECOVER_APP_NAME) = "
                    + isEnableCheckPackage(getActivity(), RECOVER_APP_NAME));
            Log.d(TAG, "gv.getIsTherad() = " + gv.getIsTherad());
            Log.d(TAG, "Restore menu is disable");
            return false;
        }
        return true;
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

    /*
     * Author : seungyeop.yeom
     * Type : Search object
     * Date : 2015-02-23
     * Brief : Create of Apps search
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            String mTitle = context.getString(R.string.applications_settings);
            if ("VZW".equals(Config.getOperator())) {
                mTitle = context.getString(R.string.applications_settings_title);
            } else if ("LRA".equals(Config.getOperator())) {
                mTitle = context.getString(R.string.applications_settings);
            }

            setSearchIndexData(context, "application_settings", mTitle, "main", null,
                    null, "android.settings.APPLICATION_SETTINGS", null, null, 1,
                    null, null, null, 1, 0);
            return mResult;
        }
    };

    public static void setSearchIndexData(Context context, String key,
            String title, String screenTtile, String summaryOn,
            String summaryOff, String intentAction, String intentTargetClass,
            String intentTargetPackage, int currentEnable,
            String preferenceType, String settingsDBTableName,
            String settingsDBField, int visible, int checkValue) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        data.key = key;
        data.title = title;
        data.screenTitle = screenTtile;
        data.summaryOn = summaryOn;
        data.summaryOff = summaryOff;
        data.intentAction = intentAction;
        data.intentTargetClass = intentTargetClass;
        data.intentTargetPackage = intentTargetPackage;
        data.currentEnable = currentEnable;
        data.preferenceType = preferenceType;
        data.settingsDBTableName = settingsDBTableName;
        data.settingsDBField = settingsDBField;
        data.visible = visible;
        data.checkValue = checkValue;
        mResult.add(data);
    }
}
