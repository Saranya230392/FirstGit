/**
 * Copyright (C) 2007 The Android Open Source Project
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

import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Timer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.admin.DevicePolicyManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.drm.DrmStore.Action;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceFrameLayout;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RemoteViews.OnClickHandler;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import android.widget.Filter.FilterResults;

import com.android.internal.telephony.ISms;
import com.android.settings.applications.ApplicationsState.AppEntry;

import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;
import com.android.settings.applications.ManageApplications.ApplicationsAdapter;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;
import com.lge.sui.widget.control.SUIDrumDatePicker;
import com.lge.sui.widget.control.SUIDrumPicker.OnDataChangedListener;
import com.lge.sui.widget.dialog.SUIDrumDatePickerDialog;
import com.lge.sui.widget.dialog.SUIDrumPickerDialog.OnDataSetListener;
import com.lge.sui.widget.dialog.SUIDrumTimePickerDialog;

public class AppMultiDelete extends Fragment
        implements ApplicationsState.Callbacks, OnItemClickListener,
        SUIDrumDatePickerDialog.OnDateSetListener {

    private static final String TAG = "AppMultiDelete";
    static final boolean DEBUG = true;

    public static final int SIZE_TOTAL = 0;
    public static final int SIZE_INTERNAL = 1;
    public static final int SIZE_EXTERNAL = 2;

    public static final String APPS_PREFS = "apps_prefs";
    public static final String RESULT_KEY = "result";
    public static final String MESSAGE_KEY = "msg";
    private static final String RECOVER_APP_NAME = "com.lge.apprecovery";

    private static final int MENU_OPTIONS_BASE = 0;
    public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 1;
    public static final int FILTER_APPS_SDCARD = MENU_OPTIONS_BASE + 2;
    public static final int DATA_SPECIFIC = MENU_OPTIONS_BASE + 4;

    private static final String EXTRA_SORT_ORDER = "sortOrder";
    private static final String EXTRA_PICKER = "Picker";
    public static final int DEFFER_PACKAGE = -1;

    private final int UNINSTALL_COMPLETE = 1;

    private ApplicationsState.Session mSession;
    private PackageManager packageManager;
    private ApplicationsState mApplicationsState;

    public LayoutInflater mInflater;
    private View mRootView;
    private View mListContainer;

    // ListView used to display list
    private ListView mListView;

    public ArrayList<ItemInfo> delPacakgeList;
    public ApplicationsAdapter mApplicationsAdapter;

    public int mFilter;
    public boolean isFromDownloaded;
    public Calendar calendar;
    // sort order
    public int mSortOrder;
    public CharSequence mInvalidSizeStr;

    // application class
    public AppMultiDeleteGlobalVariable gv;

    private ArrayList<ApplicationsState.AppEntry> mgvEntries;
    private Context mContext;
    private DevicePolicyManager mDpm;

    //action bar
    public ActionBar mActionbar = null;
    private View mCustomActionbar = null;

    private CheckBox mCheckAll;
    private TextView mTSelectAll;
    private TextView mTCheckCount;

    private Button mDelBtn;
    private Button mCanBtn;

    private boolean bCheckAll = false;

    // AsyncTask
    private AppsMultiDeleteTask mAppsMultiDeleteTask;
    private static int packageDeleteStatus;

    public static final int MULTI_DELETE_FAIL = -1;
    public static final int MULTI_DELETE_FIRST = 0;
    public static final int MULTI_DELETE_ING = 33;
    public static final int MULTI_DELETE_SUCCESS = 100;

    // DatePicker
    private static final int DIALOG_DATEPICKER = 0;
    private Dialog Pickerdialog;

    String backup_result;
    String backup_msg;

    public int mSuccessCnt;
    // rebestm - mBreadCrumb
    SettingsBreadCrumb mBreadCrumb;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UNINSTALL_COMPLETE:
            {
                String packageName = (String)msg.obj;
                switch (msg.arg1) {
                case PackageManager.DELETE_SUCCEEDED:
                    Log.d(TAG, "Uninstall succeed :" + packageName);
                    packageDeleteStatus = AppMultiDelete.MULTI_DELETE_SUCCESS;
                    mSuccessCnt++;
                    break;
                case PackageManager.DELETE_FAILED_DEVICE_POLICY_MANAGER:
                    Log.e(TAG, "Uninstall failed because " + packageName
                            + " is a device admin");
                    packageDeleteStatus = AppMultiDelete.MULTI_DELETE_FAIL;
                    break;
                default:
                    Log.e(TAG, "Uninstall failed for " + packageName
                            + " with code " + msg.arg1);
                    packageDeleteStatus = AppMultiDelete.MULTI_DELETE_FAIL;
                    break;
                }
            }
                break;
            default:
                break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getActivity();
        //        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        //mSession = mApplicationsState.newSession(this);
        packageManager = getActivity().getPackageManager();

        this.mFilter = FILTER_APPS_THIRD_PARTY;

        final Bundle args = getArguments();
        this.mSortOrder = (args != null) ? args.getInt(EXTRA_SORT_ORDER) : null;
        this.isFromDownloaded = (args != null) ? args.getBoolean(EXTRA_PICKER) : null;

        mInvalidSizeStr = getActivity().getText(R.string.invalid_size_value);

        packageDeleteStatus = AppMultiDelete.MULTI_DELETE_FIRST;
        Log.d(TAG, "sortby = " + this.mSortOrder);

        if (delPacakgeList == null) {
            delPacakgeList = new ArrayList<ItemInfo>();
        } else {
            delPacakgeList.clear();
            delPacakgeList = new ArrayList<ItemInfo>();
        }

        mDpm = (DevicePolicyManager)getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        gv = (AppMultiDeleteGlobalVariable)this.getActivity().getApplicationContext();
        getAppMultiDeleteGlobalVariable();
        setCustomActionbar();

        setHasOptionsMenu(true);

        setSharedStringPreferences(RESULT_KEY, "null", context);
        setSharedStringPreferences(MESSAGE_KEY, "null", context);
        getSharedStringPreferences(RESULT_KEY, backup_result, context);
        getSharedStringPreferences(MESSAGE_KEY, backup_msg, context);

        mAppsMultiDeleteTask = null;
    }

    private void setCustomActionbar() {
        mActionbar = getActivity().getActionBar();
        
        // mBreadCrumb
        if (Utils.isTablet() || Utils.isWifiOnly(getActivity())) {
            
//            if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
//                mBreadCrumb = SettingsBreadCrumb.get(getActivity()); 
//                mBreadCrumb.clean();
//            }
            mCustomActionbar = LayoutInflater.from(getActivity()).inflate(
                    R.layout.manage_applications_multi_delete_title, null);
            mActionbar.setCustomView( mCustomActionbar, new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
//            mActionbar.setTitle(R.string.settings_label);
//            mActionbar.setIcon(R.mipmap.ic_launcher_settings);
        //    mActionbar.show();
        } else {
            //        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
            mActionbar.setDisplayShowTitleEnabled(false);
            mActionbar.setDisplayShowHomeEnabled(false);
            mActionbar.setDisplayHomeAsUpEnabled(false);
            mActionbar.setDisplayShowCustomEnabled(true);
            mCustomActionbar = LayoutInflater.from(getActivity()).inflate(
                    R.layout.manage_applications_multi_delete_title, null);
            mActionbar.setCustomView(mCustomActionbar, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            mActionbar.show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mRootView != null && mListContainer != null) {
            return mRootView;
        }

        mInflater = inflater;
        View view = mRootView = inflater.inflate(R.layout.manage_applications_multi_delete,
                container, false);

        mListContainer = mRootView.findViewById(R.id.list_container);

        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            ListView lv = (ListView)mListContainer.findViewById(android.R.id.list);

            if (emptyView != null) {
                lv.setEmptyView(emptyView);
                TextView app_name = (TextView)emptyView;
                if (Utils.isUI_4_1_model(mContext)) {
                    app_name.setTextColor(Color.BLACK);
                    app_name.setPaddingRelative(0, 16, 0, 0);
                    app_name.setTextSize(18);
                    lv.setFastScrollEnabled(true);
                } else {
                    app_name.setCompoundDrawablePadding(-28);
                    app_name.setTypeface(null, Typeface.BOLD);
                }
            }

            mDelBtn = (Button)mRootView.findViewById(R.id.del_ok);
            mDelBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    DeleteQDlg(); //deletePackage();
                }
            });

            mCanBtn = (Button)mRootView.findViewById(R.id.del_cancel);
            mCanBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    getActivity().onBackPressed();
                }
            });

            mTSelectAll = (TextView)mCustomActionbar.findViewById(R.id.select_all_title);
            mTCheckCount = (TextView)mCustomActionbar.findViewById(R.id.selected);
            mCheckAll = (CheckBox)mCustomActionbar.findViewById(R.id.checkall);
            setCountActionBarSelected();
            setEnableDelButton();
            mTSelectAll.setOnClickListener(textClickListener);
            mCheckAll.setOnClickListener(CheckAllClickListener);

            lv.setOnItemClickListener(this);
            //            lv.setSaveEnabled(true);
            //            lv.setTextFilterEnabled(true);
            lv.setChoiceMode(lv.CHOICE_MODE_MULTIPLE);

            mListView = lv;
            mApplicationsAdapter = new ApplicationsAdapter(mApplicationsState, this, mFilter);
            mListView.setAdapter(mApplicationsAdapter);
            mListView.setRecyclerListener(mApplicationsAdapter);
            //           Utils.prepareCustomPreferencesList(contentParent, contentChild, mListView, false);

        }
        return view;
    }

    public void DeleteQDlg() {
        // Create Alert Dialog
        final Context mContext = this.getActivity();
        Builder AlertDlg = new AlertDialog.Builder(mContext);
        AlertDlg.setTitle(getResources().getString(R.string.uninstall));
        if (isSupportedRecover(mContext)) {
            if (getDelListSize() == 1) {
                AlertDlg.setMessage(getString(
                        R.string.settings_apps_multi_recover_unistall_summary3_one));
            } else {
                AlertDlg.setMessage(getString(
                        R.string.settings_apps_multi_recover_unistall_summary2,
                        getDelListSize()));
            }
        } else {
            if (getDelListSize() == 1) {
                AlertDlg.setMessage(getString(R.string.settings_apps_multi_unistall_summary3_one));
            } else {
                AlertDlg.setMessage(getString(
                        R.string.settings_apps_multi_unistall_summary2,
                        getDelListSize()));
            }
        }
        AlertDlg.setPositiveButton(R.string.def_yes_btn_caption
                , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mAppsMultiDeleteTask == null) {
                            mAppsMultiDeleteTask = new AppsMultiDeleteTask(mContext);
                            if (mAppsMultiDeleteTask.getStatus() != AsyncTask.Status.RUNNING) {
                                //mAppsMultiDeleteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , (Void[])null);
                                mAppsMultiDeleteTask.execute(0);
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.def_no_btn_caption,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                    }
                }).show();
    }

    public void getAppMultiDeleteGlobalVariable() {
        mSession = gv.getSession();
        mgvEntries = gv.getEntries();
        mContext = gv.getContext();
        calendar = gv.getPickCalendar();
    }

    private OnClickListener textClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            bCheckAll = !bCheckAll;
            Log.d(TAG, "textClickListener ->  bCheckAll = " + bCheckAll);
            mApplicationsAdapter.selectAllDelPacakgeList(bCheckAll);
            setEnableDelButton();
            setCountActionBarSelected();
            SetSelectAllchecked();
            mApplicationsAdapter.notifyDataSetChanged();
        }
    };

    private OnClickListener CheckAllClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            bCheckAll = !bCheckAll;
            Log.d(TAG, "CheckAllClickListener -> bCheckAll = " + bCheckAll);
            mCheckAll.setChecked(bCheckAll);
            mApplicationsAdapter.selectAllDelPacakgeList(bCheckAll);
            setEnableDelButton();
            setCountActionBarSelected();
            mApplicationsAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
        // TODO Auto-generated method stub
        Log.d(TAG, " onItemClick position = " + position
                + "mApplicationsAdapter.getCount() =  " + mApplicationsAdapter.getCount());

        if ((position >= 0) && (position < mApplicationsAdapter.getCount())) {
            mApplicationsAdapter.nListPosition = position;
            mListView.setItemChecked(position, mListView.isItemChecked(position));
            mApplicationsAdapter.updatedelPacakgeList(mListView.isItemChecked(position));
            setCountActionBarSelected();
            setEnableDelButton();
            SetSelectAllchecked();
            mApplicationsAdapter.notifyDataSetChanged();
        } else {
            Log.i(TAG, "Out of Range");
        }
    }

    public void SetSelectAllchecked() {
        if (mApplicationsAdapter.getCount() == delPacakgeList.size()) {
            Log.d(TAG, "onAllchecked is true");
            bCheckAll = true;
            mCheckAll.setChecked(true);
        } else {
            Log.d(TAG, "onAllchecked is false");
            bCheckAll = false;
            mCheckAll.setChecked(false);
        }
    }

    public void setCountActionBarSelected() {
        mTCheckCount.setText(getString(R.string.sp_selected_number_NORMAL, getDelListSize()));
    }

    public void setEnableDelButton() {
        if (getDelListSize() > 0) {
            mDelBtn.setEnabled(true);
        } else {
            mDelBtn.setEnabled(false);
        }
    }

    public int getDelListSize() {
        int size;
        if (delPacakgeList != null) {
            size = delPacakgeList.size();
        }
        else {
            size = 0;
        }
        Log.d(TAG, "getDelListSize = " + size);
        return size;
    }

    static class ApplicationsAdapter extends BaseAdapter implements
            /*Filterable,
            ApplicationsState.Callbacks,*/AbsListView.RecyclerListener {
        private final AppMultiDelete mAppMultiDelete;
        private final ApplicationsState mState;
        private final ApplicationsState.Session mSession;
        private final Context mContext;
        //        private final ArrayList<View> mActive = new ArrayList<View>();
        // private final int mFilterMode;
        //        private ArrayList<ApplicationsState.AppEntry> mBaseEntries;
        private ArrayList<ApplicationsState.AppEntry> mEntries;
        private boolean mResumed;
        private int mLastSortMode = -1;
        //private boolean mWaitingForData;
        private int mWhichSize = SIZE_TOTAL;
        CharSequence mCurFilterPrefix;
        public int nListPosition;

        //        private Filter mFilter = new Filter() {
        //            @Override
        //            protected FilterResults performFiltering(CharSequence constraint) {
        //                ArrayList<ApplicationsState.AppEntry> entries
        //                        = applyPrefixFilter(constraint, mBaseEntries);
        //                FilterResults fr = new FilterResults();
        //                fr.values = entries;
        //                fr.count = entries.size();
        //                return fr;
        //            }
        //
        //            @Override
        //            protected void publishResults(CharSequence constraint, FilterResults results) {
        //                mCurFilterPrefix = constraint;
        //                mEntries = (ArrayList<ApplicationsState.AppEntry>)results.values;
        //                notifyDataSetChanged();
        //            }
        //        };

        public ApplicationsAdapter(ApplicationsState state, AppMultiDelete appMultiDelete,
                int filterMode) {
            mState = state;
            //            mSession = state.newSession(this);
            mSession = appMultiDelete.mSession;

            //            mSession = mAppMultiDelete.mgvApplicationsAdapter.

            mAppMultiDelete = appMultiDelete;
            mContext = appMultiDelete.getActivity();
            // mFilterMode = filterMode;

            mEntries = mAppMultiDelete.mgvEntries;
        }

        public void resume(int sort) {
            if (DEBUG) {
                Log.i(TAG, "Resume!  mResumed = " + mResumed);
            }
            if (DEBUG) {
                Log.i(TAG, "Resume!  sort = " + sort);
            }
            if (!mResumed) {
                mResumed = true;
                mSession.resume(false);
                mLastSortMode = sort;
                rebuild(true);
            }
            else {
                rebuild(sort);
            }
        }

        public void pause() {
            if (mResumed) {
                mResumed = false;
                mSession.pause();
            }
        }

        public void rebuild(int sort) {
            if (sort == mLastSortMode) {
                return;
            }
            mLastSortMode = sort;
            rebuild(true);
        }

        public void rebuild(boolean eraseold) {
            if (DEBUG) {
                Log.i(TAG, "Rebuilding app list...");
            }
            //            ApplicationsState.AppFilter filterObj;
            //            Comparator<AppEntry> comparatorObj;
            //            boolean emulated = Environment.isExternalStorageEmulated();
            //            if (emulated) {
            //                mWhichSize = SIZE_TOTAL;
            //            } else {
            //                mWhichSize = SIZE_INTERNAL;
            //            }
            //            switch (mFilterMode) {
            //                case FILTER_APPS_THIRD_PARTY:
            //                    filterObj = ApplicationsState.THIRD_PARTY_FILTER;
            //                    break;
            //                case FILTER_APPS_SDCARD:
            //                    filterObj = ApplicationsState.ON_SD_CARD_FILTER;
            //                    if (!emulated) {
            //                        mWhichSize = SIZE_EXTERNAL;
            //                    }
            //                    break;
            //                default:
            //                    filterObj = null;
            //                    break;
            //            }
            //            switch (mLastSortMode) {
            //                case SORT_ORDER_SIZE:
            //                    switch (mWhichSize) {
            //                        case SIZE_INTERNAL:
            //                            comparatorObj = ApplicationsState.INTERNAL_SIZE_COMPARATOR;
            //                            break;
            //                        case SIZE_EXTERNAL:
            //                            comparatorObj = ApplicationsState.EXTERNAL_SIZE_COMPARATOR;
            //                            break;
            //                        default:
            //                            comparatorObj = ApplicationsState.SIZE_COMPARATOR;
            //                            break;
            //                    }
            //                    break;
            //                case SORT_ORDER_ALPHA:
            //                    comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
            //                    break;
            //                    //rebestm
            //                case SORT_ORDER_DATE:
            //                    comparatorObj = ApplicationsState.DATE_COMPARATOR;
            //                    break;
            //                default:
            //                    comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
            //                    break;
            //            }
            ArrayList<ApplicationsState.AppEntry> entries = mAppMultiDelete.mgvEntries;
            //= mSession.rebuild(filterObj, comparatorObj);

            if (entries == null && !eraseold) {
                // Don't have new list yet, but can continue using the old one.
                return;
            }

            int nloopCnt = 0;

            if (entries != null) {
                nloopCnt = entries.size() - 1;
            }

            for (int i = nloopCnt; i > -1; i--) {
                ApplicationsState.AppEntry entry = mEntries.get(i);
                boolean enabled = true;
                boolean mUpdatedSysApp = false;
                boolean mIsMyapp = false;
                
                mUpdatedSysApp = (entry.info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
                if (mAppMultiDelete.mDpm.packageHasActiveAdmins(entry.info.packageName)) {
                    Log.d(TAG, "Remove Admins label = " + entry.label);
                    enabled = false;
                }
                
                if ((entry.info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
                    Log.d (TAG, "No my app = " + entry.label);
                    mIsMyapp = true;
                }
                
                if (mUpdatedSysApp || (!enabled) || mIsMyapp) {
                    mEntries.remove(i);
                    Log.d(TAG, "i = " + i + ",   Remove update label = " + entry.label);
                }
            }

            if (entries.size() == 0) {
                mAppMultiDelete.mActionbar.setDisplayHomeAsUpEnabled(true);
                mAppMultiDelete.mActionbar.setDisplayShowTitleEnabled(true);
                if (Utils.isUI_4_1_model(mContext)) {
                    mAppMultiDelete.mActionbar.setDisplayShowHomeEnabled(false);
                } else {
                    mAppMultiDelete.mActionbar.setDisplayShowHomeEnabled(true);
                }
                mAppMultiDelete.mActionbar.setDisplayShowCustomEnabled(false);
                mAppMultiDelete.mActionbar.setTitle(R.string.settings_apps_uninstall_apps);
                mAppMultiDelete.mActionbar.setIcon(R.drawable.ic_settings_applications);
            }

            notifyDataSetChanged();

            if (entries == null) {
                //mWaitingForData = true;
                mAppMultiDelete.mListContainer.setVisibility(View.INVISIBLE);
                //                mTab.mLoadingContainer.setVisibility(View.VISIBLE);
            } else {
                mAppMultiDelete.mListContainer.setVisibility(View.VISIBLE);
            }

        }

        //        // yonguk.kim 20120816 ICS Stuff Porting to JB - [LGP708g][4097] delete tags apk request TCL vendor
        //        private static String tags ="com.android.apps.tag";
        //
        //        ArrayList<ApplicationsState.AppEntry> applyPrefixFilter(CharSequence prefix,
        //                ArrayList<ApplicationsState.AppEntry> origEntries) {
        //
        //            // yonguk.kim 20120816 ICS Stuff Porting to JB - Apply Resource Array
        //            HashSet<String> hidden_apps = Utils.getHiddenApps(mContext);
        //            if((Config.TCL).equals(Config.getOperator()))
        //                hidden_apps.add(tags);
        //
        //            if("SPR".equals(Config.getOperator())|| "BM".equals(Config.getOperator()) ){
        //                Utils.checkChameleon();
        //            }
        //
        //            if (prefix == null || prefix.length() == 0) {
        //                ArrayList<ApplicationsState.AppEntry> newEntries = new ArrayList<ApplicationsState.AppEntry>();
        //
        //                for (int i=0; i<origEntries.size(); i++) {
        //                    ApplicationsState.AppEntry entry = origEntries.get(i);
        //                    if(entry!=null && entry.info !=null && hidden_apps.size()>0 &&
        //                            hidden_apps.contains(entry.info.packageName)==false)
        //                        newEntries.add(entry);
        //                }
        //                return newEntries;
        //            } else {
        //                String prefixStr = ApplicationsState.normalize(prefix.toString());
        //                final String spacePrefixStr = " " + prefixStr;
        //                ArrayList<ApplicationsState.AppEntry> newEntries
        //                        = new ArrayList<ApplicationsState.AppEntry>();
        //                for (int i=0; i<origEntries.size(); i++) {
        //                    ApplicationsState.AppEntry entry = origEntries.get(i);
        //                    String nlabel = entry.getNormalizedLabel();
        //                    if (nlabel.startsWith(prefixStr) || nlabel.indexOf(spacePrefixStr) != -1) {
        //                        if(entry!=null && entry.info !=null && hidden_apps.size()>0 &&
        //                                hidden_apps.contains(entry.info.packageName)==false)
        //                            newEntries.add(entry);
        //                    }
        //                }
        //                return newEntries;
        //            }
        //        }
        //
        //        @Override
        //        public void onRunningStateChanged(boolean running) {
        //        }
        //
        //        @Override
        //        public void onRebuildComplete(ArrayList<AppEntry> apps) {
        ////            mWaitingForData = false;
        ////            mBaseEntries = apps;
        ////            mEntries = applyPrefixFilter(mCurFilterPrefix, mBaseEntries);
        ////            notifyDataSetChanged();
        //        }
        //
        //        @Override
        //        public void onPackageListChanged() {
        //            rebuild(false);
        //        }
        //
        //        @Override
        //        public void onPackageIconChanged() {
        //            // We ensure icons are loaded when their item is displayed, so
        //            // don't care about icons loaded in the background.
        //        }
        //
        //        @Override
        //        public void onPackageSizeChanged(String packageName) {
        ////            for (int i=0; i<mActive.size(); i++) {
        ////                AppViewHolder holder = (AppViewHolder)mActive.get(i).getTag();
        ////                if (holder.entry.info.packageName.equals(packageName)) {
        ////                    synchronized (holder.entry) {
        ////                        holder.updateSizeText(mAppMultiDelete.mInvalidSizeStr, mWhichSize, mLastSortMode);
        ////                    }
        //////                    if (holder.entry.info.packageName.equals(mTab.mOwner.mCurrentPkgName)
        //////                           && mLastSortMode == SORT_ORDER_SIZE) {
        ////                        // We got the size information for the last app the
        ////                        // user viewed, and are sorting by size...  they may
        ////                        // have cleared data, so we immediately want to resort
        ////                        // the list with the new size to reflect it to the user.
        //////                        rebuild(false);
        //////                    }
        //////                    mTab.updateStorageUsage();
        ////                    return;
        ////                }
        ////            }
        //        }

        //        @Override
        //        public void onAllSizesComputed() {
        ////            if (mLastSortMode == SORT_ORDER_SIZE) {
        ////                rebuild(false);
        ////            }
        //        }

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
            AppViewHolder holder = AppViewHolder.createOrRecycle(mAppMultiDelete.mInflater
                    , convertView, AppViewHolder.LIST_LAYOUT_TYPE_DELETE);
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

                holder.updateSizeText(mAppMultiDelete.mInvalidSizeStr, mWhichSize, mLastSortMode);

                if ((entry.info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
                    holder.disabled.setVisibility(View.VISIBLE);
                    holder.disabled.setText(R.string.not_installed);
                } else if (!entry.info.enabled) {
                    holder.disabled.setVisibility(View.VISIBLE);
                    holder.disabled.setText(R.string.disabled);
                } else {
                    holder.disabled.setVisibility(View.GONE);
                }
            }

            holder.checkBox.setChecked(((ListView)parent).isItemChecked(position));

            return convertView;
        }

        public void updatedelPacakgeList(boolean ischecked) {
            // Bind the data efficiently with the holder
            ApplicationsState.AppEntry entry = mEntries.get(nListPosition);
            Log.d(TAG, " Selet position = " + nListPosition + ", label = " + entry.label);

            ItemInfo itemInfo = new ItemInfo(entry.info.packageName, entry.label, null, 0, null
                    , entry.sizeStr, entry.installed);

            int isSamePackageindex = isSamePackage(entry.info.packageName);

            if (ischecked) {
                if (isSamePackageindex == DEFFER_PACKAGE) {
                    mAppMultiDelete.delPacakgeList.add(itemInfo);
                }
            } else {
                if (isSamePackageindex != DEFFER_PACKAGE) {
                    mAppMultiDelete.delPacakgeList.remove(isSamePackageindex);
                }
            }
        }

        public int isSamePackage(String packagename) {
            int delCnt = mAppMultiDelete.delPacakgeList.size();
            for (int i = 0; i < delCnt; i++) {
                if (mAppMultiDelete.delPacakgeList.get(i).getPackageName().equals(packagename)) {
                    return i;
                }
            }
            return DEFFER_PACKAGE;
        }

        public void selectAllDelPacakgeList(boolean isSelectAll) {
            // Bind the data efficiently with the holder
            Log.d(TAG, " selecAllDelPacakgeList -> isSelectAll = " + isSelectAll);

            int size = getCount();

            if (!isSelectAll) {
                mAppMultiDelete.delPacakgeList.clear();
                for (int i = 0; i < size; i++) {
                    mAppMultiDelete.mListView.setItemChecked(i, false);
                }
            } else {
                mAppMultiDelete.delPacakgeList.clear();
                for (int i = 0; i < size; i++) {
                    ApplicationsState.AppEntry entry = mEntries.get(i);
                    //                    Log.d (TAG, " packageName = " + entry.info.packageName);
                    mAppMultiDelete.delPacakgeList.add
                            (new ItemInfo(entry.info.packageName, entry.label, null, 0, null
                                    , entry.sizeStr, entry.installed));
                    mAppMultiDelete.mListView.setItemChecked(i, true);
                }
            }
        }

        // picker
        public void selectPickerDelPacakgeList(Calendar picker) {
            // Bind the data efficiently with the holder
            Log.d(TAG, " selectPickerDelPacakgeList , picker = " + picker);

            int size = getCount();
            boolean isCheck = false;
            Context context = mAppMultiDelete.getActivity();

            mAppMultiDelete.delPacakgeList.clear();

            Date pickerData = picker.getTime();

            //            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MM/dd/yyyy");
            java.text.DateFormat format = DateFormat.getDateFormat(context);

            for (int i = 0; i < size; i++) {
                ApplicationsState.AppEntry entry = mEntries.get(i);

                try {
                    java.util.Date dateEntry = format.parse(entry.installed);

                    String dateString = format.format(dateEntry);
                    String dateString2 = format.format(pickerData);

                    Log.d(TAG, "dateEntry = " + dateString);
                    Log.d(TAG, "pickerData = " + dateString2);

                    if (dateEntry.after(pickerData) || dateString.equals(dateString2)) {
                        isCheck = true;
                    } else if (dateEntry.before(pickerData)) {
                        isCheck = false;
                    }
                    Log.d(TAG, "isCheck = " + isCheck);
                } catch (java.text.ParseException ex) {
                    ex.printStackTrace();
                }

                if (isCheck) {
                    mAppMultiDelete.delPacakgeList.add
                            (new ItemInfo(entry.info.packageName, entry.label, null, 0, null
                                    , entry.sizeStr, entry.installed));
                    mAppMultiDelete.mListView.setItemChecked(i, true);
                }
            }
        }

        @Override
        public void onMovedToScrapHeap(View arg0) {
            // TODO Auto-generated method stub

        }
    }

    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            mHandler.sendMessage(msg);
        }
    }

    public class AppsMultiDeleteTask extends AsyncTask<Integer, String, Integer> {
        private ProgressDialog mDeletingProgDlg;
        private Context mContext;

        boolean bCancel;
        int taskCnt;
        int nloopcnt;
        int deletfailcnt;
        int mTotalCnt;
        int mBackupFailCnt;

        public AppsMultiDeleteTask(Context context) {
            this.mContext = context;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            try {
                if (mDeletingProgDlg != null) {
                    mDeletingProgDlg.dismiss();
                    mDeletingProgDlg = null;
                }
                gv.setIsTherad(true);
                mSuccessCnt = 0;
                mBackupFailCnt = 0;
                nloopcnt = 0;
                deletfailcnt = 0;
                bCancel = false;
                taskCnt = delPacakgeList.size();
                mTotalCnt = mApplicationsAdapter.getCount();
                mDeletingProgDlg = new ProgressDialog(mContext);
                //                mDeletingProgDlg.setTitle(getResources().getString(R.string.sp_deleting_NORMAL));
                mDeletingProgDlg.setMessage(getResources().getString(
                        R.string.settings_apps_uninstalling2));
                mDeletingProgDlg.setCancelable(true);
                mDeletingProgDlg.setCanceledOnTouchOutside(false);
                //                mDeletingProgDlg.setButton(getResources().getString(R.string.dlg_cancel),
                //                                                    new DialogInterface.OnClickListener() {
                //                    @Override
                //                    public void onClick(final DialogInterface arg0, final int arg1) {
                //                        Log.d (TAG, "onClick Cancel");
                //                        // TODO Auto-generated method stub
                //                        if (mAppsMultiDeleteTask != null) {
                //                            mAppsMultiDeleteTask.onCancelled();
                //                            getActivity().finish();
                //                        }
                //                    }
                //                });

                mDeletingProgDlg.setOnCancelListener(new ProgressDialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.d(TAG, "setOnCancelListener");
                        if (mAppsMultiDeleteTask != null) {
                            mAppsMultiDeleteTask.onCancelled();
                            getActivity().onBackPressed();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            mDeletingProgDlg.show();
            super.onPreExecute();
        }

        private long getAPKInstalled(String packageName) {
            PackageManager pm = mContext.getPackageManager();
            try {
                long temp_installed = pm.getPackageInfo(packageName, 0).firstInstallTime;
                return temp_installed;
            } catch (NameNotFoundException e1) {
                Log.e(TAG, "NameNotFoundException");
                return 0;
            }
        }

        private void onCancelSendBradcast(int nloopcnt) {
            Log.i(TAG, "onCancelSendBradcast ---> ");
            if (nloopcnt >= taskCnt) {
                Log.e(TAG, "onCancelSendBradcast - nloopcnt is total same!!!");
                return;
            }

            Intent intent = new Intent(
                    EnumManager.IntentActionType.REQUEST_BACKUP_CANCEL.getValue());
            intent.putExtra(EnumManager.ExtraNameType.EXTRA_PACKAGE_NAME.getValue()
                    , delPacakgeList.get(nloopcnt).getPackageName());

            Log.i(TAG, "nloopcnt = " + nloopcnt);
            Log.i(TAG, "del label name =  " + delPacakgeList.get(nloopcnt).getLabel());

            mContext.sendBroadcast(intent);
            Log.i(TAG, "<--- onCancelSendBradcast");
        }

        private void onToastSendBradcast(int success, int total) {
            Intent intent = new Intent(EnumManager.IntentActionType.RESULT_TOAST.getValue());
            intent.putExtra(EnumManager.ExtraNameType.EXTRA_PACKAGE_SUCCESS_CNT.getValue(), success);
            intent.putExtra(EnumManager.ExtraNameType.EXTRA_PACKAGE_TOTAL_CNT.getValue(), total);

            Log.i(TAG, "success = " + success);
            Log.i(TAG, "total =  " + total);

            mContext.sendBroadcast(intent);
            Log.i(TAG, "onToastSendBradcast = " + nloopcnt);
        }

        private void onBackupSendBradcast(int nloopcnt) {
            Log.i(TAG, "onBackupSendBradcast --->");
            if (nloopcnt >= taskCnt) {
                Log.e(TAG, "onBackupSendBradcast - nloopcnt is total same!!!");
                return;
            }

            Intent intent = new Intent(EnumManager.IntentActionType.REQUEST_BACKUP.getValue());
            intent.putExtra(
                    EnumManager.ExtraNameType.EXTRA_PACKAGE_INSTALL_DATE.getValue(),
                    getAPKInstalled(delPacakgeList.get(nloopcnt).getPackageName()));
            intent.putExtra(
                    EnumManager.ExtraNameType.EXTRA_PACKAGE_INSTALL_SIZE
                            .getValue(), delPacakgeList.get(nloopcnt)
                            .getSizeStr());
            intent.putExtra(
                    EnumManager.ExtraNameType.EXTRA_PACKAGE_NAME.getValue(),
                    delPacakgeList.get(nloopcnt).getPackageName());

            Log.i(TAG, "nloopCnt = " + nloopcnt);
            Log.i(TAG, "Del label =  " + delPacakgeList.get(nloopcnt).getLabel());
            Log.i(TAG, "entry.sizeStr = "
                    + delPacakgeList.get(nloopcnt).getSizeStr()
                    + ", entry.installed = "
                    + delPacakgeList.get(nloopcnt).getInstalled());

            mContext.sendBroadcast(intent);
            // backup_result = "sendEvent";
            setSharedStringPreferences(RESULT_KEY, "sendEvent", mContext);
            Log.i(TAG, "<--- onBackupSendBradcast");
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            boolean Firstintent = true;

            if (isSupportedRecover(mContext)) {
                PackageDeleteObserver observer = new PackageDeleteObserver();

                while (nloopcnt < taskCnt) {
                    //                    getSharedStringPreferences(RESULT_KEY, backup_result);
                    if (bCancel) {
                        //                            && (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS) ) {
                        Log.d(TAG, "Loop User Cancel ----> ");
                        getSharedStringPreferences(RESULT_KEY, backup_result, mContext);
                        if (backup_result.equals("sendEvent")) {
                            //onCancelled();
                            Log.d(TAG, "1111App multi delete Cancel");
                            return taskCnt;
                        } else {
                            if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_ING) {
                                Log.d(TAG, "Deleting ~ ing!!! go loop ~~~!!!");
                            } else if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS) {
                                Log.d(TAG, "2222App multi delete Cancel");
                                return taskCnt;
                            }
                        }

                        //                        getSharedStringPreferences(RESULT_KEY, backup_result, mContext);
                        //                        if (backup_result.equals("CancelSendEvent")) {
                        //                            return taskCnt;
                        //                        } else if (backup_result.equals("sendEvent")) {
                        //                            
                        //                        }
                        //                        
                        //                        getSharedStringPreferences(RESULT_KEY, backup_result, mContext);
                        //                        if (backup_result.equals("sendEvent")) {
                        //                            if ( packageDeleteStatus == AppMultiDelete.MULTI_DELETE_ING)  {
                        //                                Log.d (TAG, "Deleting ~ ing!!! go loop ~~~!!!");
                        //                            } else if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS) {
                        //                                Log.d (TAG, "App multi delete Cancel");
                        //                                return taskCnt;
                        //                            }
                        //                        }
                        Log.d(TAG, "<--- Loop User Cancel end ");
                    }
                    if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_FIRST ||
                            packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS) {
                        if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS) {
                            getSharedStringPreferences(RESULT_KEY, backup_result, mContext);
                            if (backup_result.equals("backupok")) {
                                nloopcnt++;
                            }
                            if ((nloopcnt == taskCnt) || bCancel) {
                                Log.d(TAG, " nloopcnt == taskCnt ----> END loop ");
                                Log.d(TAG, " bCancel = " + bCancel);
                                return taskCnt;
                            } else if (backup_result.equals("backupok")) {
                                onBackupSendBradcast(nloopcnt);
                            }
                        }
                        else if (Firstintent) {
                            onBackupSendBradcast(nloopcnt);
                            Firstintent = false;
                        }
                        getSharedStringPreferences(RESULT_KEY, backup_result, mContext);
                        if (backup_result.equals(EnumManager.RequestResult.SUCCESS.getValue())) {
                            try {
                                Log.d(TAG, "nloopcnt=" + nloopcnt + "" +
                                        ", packageDeleteStatus = " + packageDeleteStatus);
                                /*Check if the package is installed or not*/
                                if (!bCancel) {
                                    packageManager.deletePackage(
                                            delPacakgeList.get(nloopcnt).getPackageName(),
                                            observer, 0);
                                    packageDeleteStatus = AppMultiDelete.MULTI_DELETE_ING;
                                } else {
                                    return taskCnt;
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "" + delPacakgeList.get(nloopcnt) + " " +
                                        "is not installed.(Successfully Removed)");
                            }
                            //backup_result = "backupok";
                            setSharedStringPreferences(RESULT_KEY, "backupok", mContext);
                        }
                        else if (backup_result.equals(EnumManager.RequestResult.FAIL.getValue())) {
                            mBackupFailCnt++;
                            deletfailcnt++;
                            packageDeleteStatus = AppMultiDelete.MULTI_DELETE_SUCCESS;
                            //backup_result = "backupok";
                            setSharedStringPreferences(RESULT_KEY, "backupok", mContext);
                            Log.e(TAG, "mBackupFailCnt = " + mBackupFailCnt);
                            Log.e(TAG, "Label = " + delPacakgeList.get(nloopcnt).getLabel());
                            // memory full case???
                            getSharedStringPreferences(MESSAGE_KEY, "null", mContext);
                            Log.e(TAG, "backup_msg = " + backup_msg);
                        }
                    } else if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_FAIL) {
                        deletfailcnt++;
                        Log.e(TAG, "deletfailcnt = " + deletfailcnt);
                        Log.i(TAG, "packageDeleteStatus -> MULTI_DELETE_FAIL");
                        Log.e(TAG, "LabelName = " + delPacakgeList.get(nloopcnt).getLabel());

                        // send event
                        Intent intent = new Intent
                                (EnumManager.IntentActionType.REQUEST_REMOVE_BACKUP_DATA.getValue());
                        intent.putExtra(
                                EnumManager.ExtraNameType.EXTRA_PACKAGE_NAME.getValue(),
                                delPacakgeList.get(nloopcnt).getPackageName());
                        mContext.sendBroadcast(intent);

                        packageDeleteStatus = AppMultiDelete.MULTI_DELETE_SUCCESS;
                    }
                    //            publishProgress("progress", Integer.toString(i), " " + Integer.toString(i) + " ");                
                    //            SystemClock.sleep(300);    
                }

            } else {
                PackageDeleteObserver observer = new PackageDeleteObserver();

                while (nloopcnt < taskCnt) {
                    if (bCancel
                            &&
                            (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS || packageDeleteStatus == AppMultiDelete.MULTI_DELETE_FIRST))
                    {
                        Log.d(TAG, "App multi delete Cancel");
                        return taskCnt;
                    }

                    if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_FIRST ||
                            packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS)
                    {
                        if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_SUCCESS) {
                            nloopcnt++;
                            Log.d(TAG, "Delete success nloopCnt = " + nloopcnt);

                            if ((nloopcnt == taskCnt) || bCancel) {
                                Log.d(TAG, " nloopcnt == taskCnt ----> END loop ");
                                Log.d(TAG, " bCancel = " + bCancel);
                                return taskCnt;
                            }
                        }

                        try {
                            Log.d(TAG, "nloopcnt=" + nloopcnt + "" +
                                    ", packageDeleteStatus = " + packageDeleteStatus);
                            /*Check if the package is installed or not*/
                            packageManager.deletePackage(
                                    delPacakgeList.get(nloopcnt).getPackageName(), observer, 0);
                            packageDeleteStatus = AppMultiDelete.MULTI_DELETE_ING;
                        } catch (Exception e) {
                            Log.d(TAG, "+delPacakgeList.get(nloopcnt)+" +
                                    " is not installed.(Successfully Removed)");
                        }
                    } else if (packageDeleteStatus == AppMultiDelete.MULTI_DELETE_FAIL) {
                        deletfailcnt++;
                        //                        mAppsMultiDeleteTask.onCancelled();
                        //                        getActivity().finish();
                        Log.e(TAG, "deletfailcnt = " + deletfailcnt);
                        Log.i(TAG, "packageDeleteStatus -> MULTI_DELETE_FAIL");
                        Log.e(TAG, "Lable = " + delPacakgeList.get(nloopcnt).getLabel());
                        packageDeleteStatus = AppMultiDelete.MULTI_DELETE_SUCCESS;
                        //                        return taskCnt;
                    }

                    //            publishProgress("progress", Integer.toString(i), " " + Integer.toString(i) + " ");
                    //            SystemClock.sleep(300);
                }
            }

            // for loop backup
            //            for (int i = 0; i < delPacakgeList.size(); i++) {
            //                if (bCancel) {
            //                    Log.d (TAG, "App multi delete cancel");
            //                    break;
            //                }
            //
            //                try{
            //                    /*Check if the package is installed or not*/
            //                    final PackageInfo pi = packageManager.getPackageInfo(delPacakgeList.get(i), 0);
            //
            //                    PackageDeleteObserver observer = new PackageDeleteObserver();
            //                    packageManager.deletePackage(delPacakgeList.get(i), observer, 0);
            //
            //                }catch(Exception e){
            //                    Log.d (TAG,""+delPacakgeList.get(i)+" is not installed.(Successfully Removed)");
            //                }
            //    //            publishProgress("progress", Integer.toString(i), " " + Integer.toString(i) + " ");
            //
            //    //            SystemClock.sleep(300);
            //            }
            return taskCnt;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.i(TAG, "onProgressUpdate");
            //            if (progress[0].equals("progress")) {
            //                mDeletingProgDlg.setProgress(Integer.parseInt(progress[1]));
            //                mDeletingProgDlg.setMessage(progress[2]);
            //            }
            //            else if (progress[0].equals("max")) {
            //                mDeletingProgDlg.setMax(Integer.parseInt(progress[1]));
            //            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            gv.setIsTherad(false);
            Log.i(TAG, "onPostExecute");
            int DelListCnt = getDelListSize();
            int Delsuccess = mSuccessCnt;

            //            if( bCancel) {
            //                Delsuccess = nloopcnt - deletfailcnt;
            //            } else {
            //                Delsuccess = getDelListSize() - deletfailcnt;
            //            }

            try {
                if (mDeletingProgDlg != null) {
                    mDeletingProgDlg.dismiss();
                    mDeletingProgDlg = null;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (Delsuccess == 0) {
                Toast.makeText(mContext, R.string.settings_apps_uninstall_allfail
                        , Toast.LENGTH_LONG).show();
            } else {
                String language = Locale.getDefault().getLanguage();
                if (language.equals("ko")) {
                    Toast.makeText(mContext, mContext.getString(
                            R.string.settings_apps_multi_unistall_complete
                            , DelListCnt, Delsuccess), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, mContext.getString(
                            R.string.settings_apps_multi_unistall_complete
                            , Delsuccess, DelListCnt), Toast.LENGTH_LONG).show();
                }
            }

            //onToastSendBradcast(Delsuccess,DelListCnt);

            if (delPacakgeList != null) {
                delPacakgeList.clear();
                Log.i(TAG, "delPacakgeList clear");
            }

            //mo2boseon.hwang@lge.com 2011.12.21 td 105126
            if (!bCancel) {
                //                Intent intent = new Intent(AppMultiDelete.this, ManageApplications.class);
                //                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //                startActivity(intent);

                try {
                    setSharedStringPreferences(ManageApplications.APPS_PREFS_UPDATE_DATA,
                            ManageApplications.APPS_PREFS_UPDATE_DATA_YES, getActivity());
                    getActivity().onBackPressed();
                } catch (Exception e) {
                    Log.d(TAG, "getActivity is null");
                }
            }

            if (mAppsMultiDeleteTask != null) {
                mAppsMultiDeleteTask = null;
            }
        }

        protected void onCancelled() {
            Log.i(TAG, "onCancelled");
            gv.setIsTherad(false);
            if (isSupportedRecover(mContext)) {
                onCancelSendBradcast(nloopcnt);
            }
            bCancel = true;
            if (mDeletingProgDlg != null) {
                mDeletingProgDlg.dismiss();
                mDeletingProgDlg = null;
            }
        }
    }

    ////////////////////////    ////////////////////////    ////////////////////////    ////////////////////////

    ////////////////////////    ////////////////////////    ////////////////////////    ////////////////////////
    ////////////////////////    ////////////////////////    ////////////////////////    ////////////////////////

    ////////////////////////    ////////////////////////    ////////////////////////    ////////////////////////

    ////////////////////////    ////////////////////////    ////////////////////////    ////////////////////////
    ////////////////////////    ////////////////////////    ////////////////////////    ////////////////////////
    //    private void refreshItemList() {
    //        allComponents = loadStoredAllAppList();
    //        addMissedComponents(allComponents);
    //        delPacakgeList = new ArrayList<String>();
    //        listAdapter = new ComponentListAdapter(this, R.layout.layout_appinfo,
    //                allComponents);
    //        setListAdapter(listAdapter);
    //    }
    //

    public void updateUI() {

        //        build(mInflater, mContentContainer, mRootView);
        mApplicationsAdapter.resume(mSortOrder);

        //        final Activity host = getActivity();
        //        if (host != null) {
        //            host.invalidateOptionsMenu();
        //        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();

        if (isFromDownloaded == true) {
            updateList(calendar);
            isFromDownloaded = false;
        }

        // picker
        IntentFilter Pickerfilter = new IntentFilter();
        Pickerfilter.addAction(Intent.ACTION_TIME_TICK);
        Pickerfilter.addAction(Intent.ACTION_TIME_CHANGED);
        Pickerfilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getActivity().registerReceiver(mIntentReceiver, Pickerfilter, null, null);

        Dialog mDialog = Pickerdialog;
        if (mDialog != null) {
            if (mDialog instanceof SUIDrumDatePickerDialog) {
                ((SUIDrumDatePickerDialog)mDialog).getDatePicker().refresh();
            } else if (mDialog instanceof SUIDrumTimePickerDialog) {
                ((SUIDrumTimePickerDialog)mDialog).getTimePicker().refresh();
            }
        }
        //        if (!refreshUi()) {
        //            setIntentAndFinish(true, true);
        //        }
        
        
        // rebestm - mBreadCrumb
        if (Utils.isTablet() || Utils.isWifiOnly(getActivity())) {
//            mListView.bringToFront();
            if (SettingsBreadCrumb.isAttached(getActivity())) {
                if (mApplicationsAdapter.getCount() > 0) {
                    mBreadCrumb = SettingsBreadCrumb.get(getActivity()); 
                    if (mBreadCrumb != null) {
                        mBreadCrumb.setTitle("");
                        mBreadCrumb.addView( mCustomActionbar, null);
                    }
                } else {
                    mBreadCrumb = SettingsBreadCrumb.get(getActivity()); 
                    if (mBreadCrumb != null) {
                        mBreadCrumb.setTitle(getResources().getString(
                                R.string.settings_apps_uninstall_apps));
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mIntentReceiver);
        // rebestm - mBreadCrumb
        if (mBreadCrumb != null) {
            mBreadCrumb.removeSwitch();
        }
    }

    OnKeyListener onKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View arg0, int keycode, KeyEvent event) {
            Log.d(TAG, "AppsMultiDeleteTask onKey keyCode = " + keycode);
            if (keycode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                delPacakgeList.clear();

                return true;
            }
            return false;
        }
    };

    @Override
    public void onStop() {
        Log.d(TAG, "on stop");
        //        if (delPacakgeList!=null) {
        //            delPacakgeList.clear();
        //        }
        super.onStop();

        //        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        //
        //        if (pm != null && pm.isScreenOn() && mAppsMultiDeleteTask != null) {
        //            mAppsMultiDeleteTask.onCancelled();
        //            getActivity().finish();
        //            Log.d (TAG, "Onstop Task release");
        //        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        Log.d(TAG, "on onDestroyView");

        super.onDestroyView();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onPrepareOptionsMenu(menu);

        if (menu.findItem(DATA_SPECIFIC) == null) {
            //            menu.add(0, DATA_SPECIFIC, 0, getString(R.string.sp_shareconnect_dlna_option_NORMAL));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == DATA_SPECIFIC) {
            //            onCreateDialog(DIALOG_DATEPICKER).show();
            //            Intent intent = new Intent();
            //            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            //            intent.putExtra(APP_PACKAGE_NAME, TEST_APP_PACKAGE_NAME);
            //            intent.setAction(ACTION_RECOVERY);
            //            getActivity().sendBroadcast(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // picker
    public Dialog onCreateDialog(int id) {

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

        Calendar pickCalendar;
        pickCalendar = Calendar.getInstance();
        pickCalendar.set(Calendar.YEAR, year);
        pickCalendar.set(Calendar.MONTH, month - 1);
        pickCalendar.set(Calendar.DAY_OF_MONTH, day);

        updateList(pickCalendar);
    }

    public void updateList(Calendar pickCalendar) {
        Log.d(TAG, "updateList = " + pickCalendar);
        mApplicationsAdapter.selectPickerDelPacakgeList(pickCalendar);
        SetSelectAllchecked();
        setEnableDelButton();
        setCountActionBarSelected();
        mApplicationsAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Activity activity = getActivity();
            if (activity != null) {
                // updateList(pickCalendar);
            }
        }
    };

    private boolean isSupportedRecover(Context content) {
        boolean isSafe = ManageApplications.isSafeMode(content);
        boolean isAppRecover = ManageApplications.isEnableCheckPackage(getActivity()
                , RECOVER_APP_NAME);
        Log.d(TAG, "isSafe = " + isSafe);
        Log.d(TAG, "isAppRecover = " + isAppRecover);

        if (isSafe && isAppRecover) {
            return true;
        }
        return false;
    }

    public void getSharedStringPreferences(String name, String value, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APPS_PREFS, 0);

        if (name.equals(RESULT_KEY)) {
            backup_result = preferences.getString(name, null);
        } else if (name.equals(MESSAGE_KEY)) {
            backup_msg = preferences.getString(name, null);
        }
    }

    public void setSharedStringPreferences(String name, String value, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APPS_PREFS, 0);
        preferences.edit().putString(name, value).commit();
    }

    @Override
    public void onAllSizesComputed() {
    }

    @Override
    public void onPackageIconChanged() {
    }

    @Override
    public void onPackageListChanged() {
        //        refreshUi();
    }

    @Override
    public void onRebuildComplete(ArrayList<AppEntry> apps) {
    }

    @Override
    public void onPackageSizeChanged(String packageName) {
    }

    @Override
    public void onRunningStateChanged(boolean running) {
    }
}
