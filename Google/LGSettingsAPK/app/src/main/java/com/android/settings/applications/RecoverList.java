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
import android.database.Cursor;
import android.drm.DrmStore.Action;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import com.android.internal.telephony.ISms;

import com.android.settings.R;
import com.android.settings.SettingsBreadCrumb;
import com.android.settings.Utils;

public class RecoverList extends Fragment
        implements OnItemClickListener {

    private static final String TAG = "RecoverList";
    static final boolean DEBUG = true;

    private static final int MENU_OPTIONS_BASE = 0;
    public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 1;
    public static final int FILTER_APPS_SDCARD = MENU_OPTIONS_BASE + 2;
    public static final int DATA_SPECIFIC = MENU_OPTIONS_BASE + 4;

    private static final String EXTRA_SORT_ORDER = "sortOrder";

    public static final String APPS_PREFS = "apps_prefs";
    public static final String RESULT_KEY = "result";
    public static final String MESSAGE_KEY = "msg";

    public LayoutInflater mInflater;
    private View mRootView;
    private View mListContainer;

    //action bar
    private ActionBar mActionbar = null;
    private View mCustomActionbar = null;

    // ListView used to display list
    private ListView mListView;

    // rebesmt_recover
    public ArrayList<String> recoverPacakgeList;
    public RecoverDelAdapter mRecoverDelAdapter;
    public AppMultiDeleteGlobalVariable gv;

    // sort order
    private int mSortOrder;
    public CharSequence mInvalidSizeStr;

    private CheckBox mCheckAll;
    private TextView mTSelectAll;
    private TextView mTCheckCount;

    private Button mDelBtn;
    private Button mCanBtn;

    private boolean bCheckAll;

    // AsyncTask
    private AppsMultiRecoverTask mAppsMultiRecoverTask;

    public static final int MULTI_DELETE_FAIL = -1;
    public static final int MULTI_DELETE_FIRST = 0;
    public static final int MULTI_DELETE_ING = 33;
    public static final int MULTI_DELETE_SUCCESS = 100;

    String recover_result;
    String recover_msg;

    // rebestm - mBreadCrumb
    SettingsBreadCrumb mBreadCrumb;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVariableinit();

        final Bundle args = getArguments();
        int sortby = (args != null) ? args.getInt(EXTRA_SORT_ORDER) : null;
        this.mSortOrder = sortby;

        Log.d(TAG, "onCreate , sortby = " + sortby);

        if (recoverPacakgeList == null) {
            recoverPacakgeList = new ArrayList<String>();
        } else {
            recoverPacakgeList.clear();
            recoverPacakgeList = new ArrayList<String>();
        }

        gv = (AppMultiDeleteGlobalVariable)this.getActivity().getApplicationContext();
        setCustomActionbar();
        setHasOptionsMenu(true);
    }

    private void setVariableinit() {
        final Context context = getActivity();
        setSharedStringPreferences(RESULT_KEY, "null", context);
        setSharedStringPreferences(MESSAGE_KEY, "null", context);
        getSharedStringPreferences(RESULT_KEY, recover_result, context);
        getSharedStringPreferences(MESSAGE_KEY, recover_msg, context);
        bCheckAll = false;
        mActionbar = null;
        mCustomActionbar = null;
        mAppsMultiRecoverTask = null;
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
            mActionbar.setDisplayShowCustomEnabled(true);
            mActionbar.setDisplayHomeAsUpEnabled(false);
            mCustomActionbar = LayoutInflater.from(getActivity()).inflate(
                    R.layout.manage_applications_multi_delete_title, null);
            mActionbar.setCustomView(mCustomActionbar, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            mActionbar.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (mRootView != null && mListContainer != null) {
            return mRootView;
        }

        mInflater = inflater;
        View view = mRootView
                = inflater.inflate(R.layout.manage_applications_recover, container, false);

        mListContainer = mRootView.findViewById(R.id.list_container);

        if (mListContainer != null) {
            // Create adapter and list view here
            View emptyView = mListContainer.findViewById(com.android.internal.R.id.empty);
            ListView lv = (ListView)mListContainer.findViewById(android.R.id.list);

            if (emptyView != null) {
                lv.setEmptyView(emptyView);
                TextView app_name = (TextView)emptyView;
                if (Utils.isUI_4_1_model(getActivity())) {
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
            mRecoverDelAdapter = new RecoverDelAdapter(this);
            mListView.setAdapter(mRecoverDelAdapter);
            mListView.setRecyclerListener(mRecoverDelAdapter);
            //           Utils.prepareCustomPreferencesList(contentParent, contentChild, mListView, false);

        }
        return view;
    }

    public void DeleteQDlg() {
        // Create Alert Dialog
        final Context mContext = this.getActivity();
        Builder AlertDlg = new AlertDialog.Builder(mContext);
        AlertDlg.setTitle(getResources().getString(R.string.settings_apps_recover));
        AlertDlg.setMessage
                (getString(R.string.settings_apps_multi_recover_summary, getDelListSize()));

        AlertDlg.setPositiveButton(R.string.def_yes_btn_caption,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mAppsMultiRecoverTask == null) {
                            mAppsMultiRecoverTask = new AppsMultiRecoverTask(mContext);
                            if (mAppsMultiRecoverTask.getStatus() != AsyncTask.Status.RUNNING) {
                                //mAppsMultiRecoverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , (Void[])null);
                                mAppsMultiRecoverTask.execute(0);
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

    private OnClickListener textClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            bCheckAll = !bCheckAll;
            Log.d(TAG, "textClickListener ->  bCheckAll = " + bCheckAll);
            mRecoverDelAdapter.selectAllrecoverPacakgeList(bCheckAll);
            setEnableDelButton();
            setCountActionBarSelected();
            SetSelectAllchecked();
            mRecoverDelAdapter.notifyDataSetChanged();
        }
    };

    private OnClickListener CheckAllClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            bCheckAll = !bCheckAll;
            Log.d(TAG, "CheckAllClickListener -> bCheckAll = " + bCheckAll);
            mCheckAll.setChecked(bCheckAll);
            mRecoverDelAdapter.selectAllrecoverPacakgeList(bCheckAll);
            setEnableDelButton();
            setCountActionBarSelected();
            mRecoverDelAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
        // TODO Auto-generated method stub
        Log.d(TAG, " onItemClick position = " + position);
        Log.d(TAG, " mRecoverDelAdapter.getCount() =  " + mRecoverDelAdapter.getCount());

        if ((position >= 0) && (position < mRecoverDelAdapter.getCount())) {
            mRecoverDelAdapter.nListPosition = position;
            mListView.setItemChecked(position, mListView.isItemChecked(position));
            mRecoverDelAdapter.updaterecoverPacakgeList(mListView.isItemChecked(position));
            setCountActionBarSelected();
            setEnableDelButton();
            SetSelectAllchecked();
            mRecoverDelAdapter.notifyDataSetChanged();
        } else {
            Log.i(TAG, "Out of Range");
        }
    }

    public void SetSelectAllchecked() {
        if (mRecoverDelAdapter.getCount() == recoverPacakgeList.size()) {
            Log.d(TAG, "onAllchecked true");
            bCheckAll = true;
            mCheckAll.setChecked(true);
        } else {
            Log.d(TAG, "onAllchecked false");
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
        if (recoverPacakgeList != null) {
            size = recoverPacakgeList.size();
        }
        else {
            size = 0;
        }
        Log.d(TAG, "getDelListSize = " + size);
        return size;
    }

    static class RecoverDelAdapter extends BaseAdapter implements AbsListView.RecyclerListener {
        private final RecoverList mRecoverList;

        private final Context mContext;
        private ArrayList<ItemInfo> items;
        private boolean mResumed;
        private int mLastSortMode = -1;
        //private boolean mWaitingForData;
        public int nListPosition;

        public RecoverDelAdapter(RecoverList appRecoverList) {
            mRecoverList = appRecoverList;
            mContext = appRecoverList.getActivity();
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
                //                mSession.resume();
                mLastSortMode = sort;
                rebuild_cp(true);
            }
            else {
                rebuild(sort);
            }
        }

        public void pause() {
            if (mResumed) {
                mResumed = false;
                //                mSession.pause();
            }
        }

        public void rebuild(int sort) {
            if (sort == mLastSortMode) {
                return;
            }
            mLastSortMode = sort;
            rebuild_cp(true);
        }

        public void rebuild_cp(boolean eraseold) {
            Log.i(TAG, "rebuild_cp Install app list...");

            items = new ArrayList<ItemInfo>();

            //             String sbCondition = "status != ? ";
            //            String[] args = {"recovery success"};
            //
            //            Uri contentUri = Uri
            //                    .parse("content://com.lge.apprecovery.provider.AppRecoveryProvider/apklist");
            //
            //
            //            Cursor cursor =
            //                    mContext.getContentResolver().query(contentUri, null, where, args, "_id ASC");
            //
            //            int colIndex_pkg_name = cursor.getColumnIndex("package_name");
            //            int colIndex_app_name = cursor.getColumnIndex("app_name");
            //            int colIndex_apk_ver_name = cursor.getColumnIndex("app_version_name");
            //            int colIndex_apk_ver_code = cursor.getColumnIndex("app_version_code");
            //            int colIndex_size = cursor.getColumnIndex("apk_file_size");
            //            int colIndex_app_icon = cursor.getColumnIndex("app_icon");

            Uri contentUri = Uri.parse(EnumManager.UriType.APK_LIST_URI.getValue());
            Cursor cursor = null;

            StringBuffer sbCondition = new StringBuffer();
            sbCondition.append(EnumManager.DBColumnType.COLUMN_STATUS.getValue()).append(" = ? ");
            String[] selectionArgs = { EnumManager.ApkStatus.BACKUP_SUCCESS.getValue() };

            Log.d(TAG, "sbCondition.toString() = " + sbCondition.toString());

            cursor = mContext.getContentResolver().query(contentUri, // URI
                    null, // return columns
                    sbCondition.toString(), // where condition
                    selectionArgs, // where values
                    "_id ASC"); // ord

            if (null == cursor || cursor.getCount() < 0) {
                Log.e(TAG, "Failed - Get backup apk list");
                return;
            }

            int colIndex_pkg_name = cursor
                    .getColumnIndex(EnumManager.DBColumnType.COLUMN_PACKAGE_NAME
                            .getValue());
            int colIndex_app_name = cursor
                    .getColumnIndex(EnumManager.DBColumnType.COLUMN_APP_NAME
                            .getValue());
            //int colIndex_apk_ver_name = cursor
            //        .getColumnIndex(EnumManager.DBColumnType.COLUMN_APP_VERSION_NAME
            //                .getValue());
            //int colIndex_size = cursor
            //        .getColumnIndex(EnumManager.DBColumnType.COLUMN_APK_FILE_SIZE
            //                .getValue());
            int colIndex_app_icon = cursor
                    .getColumnIndex(EnumManager.DBColumnType.COLUMN_APP_ICON
                            .getValue());
            int colIndex_install_date = cursor
                    .getColumnIndex(EnumManager.DBColumnType.COLUMN_APP_PACKAGE_INSTALL_DATE
                            .getValue());
            int colIndex_install_size = cursor
                    .getColumnIndex(EnumManager.DBColumnType.COLUMN_APP_PACKAGE_INSTALL_SIZE
                            .getValue());

            //            String pkgName = cursor.getString(colIndex_pkg_name);
            //            String appName = cursor.getString(colIndex_app_name);
            //            String verName = cursor.getString(colIndex_apk_ver_name);
            //            Long apkSize = cursor.getLong(colIndex_size);
            //            String installDate = cursor.getString(colIndex_install_date);
            //            String installSize = cursor.getString(colIndex_install_size);
            //            byte[] iconByte = cursor.getBlob(colIndex_app_icon);
            //            Bitmap iconBitmap = BitmapFactory.decodeByteArray(iconByte, 0, icon.length);
            //            Drawable iconImage = (Drawable)(new BitmapDrawable(getResources(), iconBitmap));

            Log.d(TAG, "cursor.getCount = " + cursor.getCount());

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        byte[] icon = cursor.getBlob(colIndex_app_icon);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
                        String pkgName = cursor.getString(colIndex_pkg_name);
                        String lable = cursor.getString(colIndex_app_name);
                        //String verName = cursor.getString(colIndex_apk_ver_name);
                        //int verCode = 0; //cursor.getInt(colIndex_apk_ver_code);
                        //long apkSize = cursor.getLong(colIndex_size);
                        long installDate_long = cursor.getLong(colIndex_install_date);
                        String installDate = getDateFormatConvert(installDate_long, mContext);

                        String installSize = cursor.getString(colIndex_install_size);

                        Drawable drawableIcon =
                                (Drawable)(new BitmapDrawable(mContext.getResources(), bitmap));

                        items.add(new ItemInfo(pkgName, lable, null, 0,
                                drawableIcon, installSize, installDate));
                    }
                } else if (cursor.getCount() == 0) {
                    mRecoverList.mActionbar.setDisplayHomeAsUpEnabled(true);
                    mRecoverList.mActionbar.setDisplayShowTitleEnabled(true);
                    if (Utils.isUI_4_1_model(mContext)) {
                        mRecoverList.mActionbar.setDisplayShowHomeEnabled(false);
                    } else {
                        mRecoverList.mActionbar.setDisplayShowHomeEnabled(true);
                    }
                    mRecoverList.mActionbar.setDisplayShowCustomEnabled(false);
                    mRecoverList.mActionbar.setTitle(R.string.settings_apps_recover_apps);
                    mRecoverList.mActionbar.setIcon(R.drawable.ic_settings_applications);
                }
            }

            if (items == null && !eraseold) {
                // Don't have new list yet, but can continue using the old one.
                return;
            }

            notifyDataSetChanged();

            if (items == null) {
                //mWaitingForData = true;
                mRecoverList.mListContainer.setVisibility(View.INVISIBLE);
                //                mTab.mLoadingContainer.setVisibility(View.VISIBLE);
            } else {
                mRecoverList.mListContainer.setVisibility(View.VISIBLE);
            }
        }

        public String getDateFormatConvert(long datetime, Context context) {
            java.text.DateFormat mDateFormat = DateFormat.getDateFormat(context);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(datetime);

            //installed =  mDateFormat.format(calendar.getTime());
            return mDateFormat.format(calendar.getTime());
        }

        public int getCount() {
            return items != null ? items.size() : 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder = AppViewHolder.createOrRecycle(mRecoverList.mInflater,
                    convertView,
                    AppViewHolder.LIST_LAYOUT_TYPE_RECOVER);

            if (holder == null) {
                return null;
            }
            convertView = holder.rootView;
            TextView app_name = (TextView)convertView.findViewById(R.id.app_name);
            if (!Utils.isUI_4_1_model(mContext)) {
                app_name.setTypeface(null, Typeface.BOLD);
            }
            // Bind the data efficiently with the holder
            final ItemInfo recoverItems = items.get(position);

            synchronized (recoverItems) {
                holder.appName.setText(recoverItems.label);
                holder.appIcon.setImageDrawable(recoverItems.icon);
                holder.appSize.setText(recoverItems.sizeStr);
                holder.appDate.setText(recoverItems.installed);
                //                holder.appDate.setVisibility(View.INVISIBLE);
                holder.disabled.setVisibility(View.GONE);
            }
            holder.checkBox.setChecked(((ListView)parent).isItemChecked(position));

            return convertView;
        }

        public void updaterecoverPacakgeList(boolean ischecked) {
            // Bind the data efficiently with the holder

            ItemInfo entry = items.get(nListPosition);
            Log.d(TAG, " Selet position = " + nListPosition + ", label = " + entry.label);

            if (ischecked) {
                if (!mRecoverList.recoverPacakgeList.contains(entry.packageName)) {
                    mRecoverList.recoverPacakgeList.add(entry.packageName);
                }
            } else {
                if (mRecoverList.recoverPacakgeList.contains(entry.packageName)) {
                    mRecoverList.recoverPacakgeList.remove(entry.packageName);
                }
            }
        }

        public void selectAllrecoverPacakgeList(boolean isSelectAll) {
            // Bind the data efficiently with the holder
            Log.d(TAG, " selecAllrecoverPacakgeList -> isSelectAll = " + isSelectAll);

            int size = getCount();

            if (!isSelectAll) {
                mRecoverList.recoverPacakgeList.clear();
                for (int i = 0; i < size; i++) {
                    mRecoverList.mListView.setItemChecked(i, false);
                }
            } else {
                mRecoverList.recoverPacakgeList.clear();
                for (int i = 0; i < size; i++) {
                    ItemInfo entry = items.get(i);
                    //                    Log.d (TAG, " packageName = " + entry.info.packageName);
                    mRecoverList.recoverPacakgeList.add(entry.packageName);
                    mRecoverList.mListView.setItemChecked(i, true);
                }
            }
        }

        // picker
        public void selectPickerrecoverPacakgeList(Calendar picker) {
            // Bind the data efficiently with the holder
            Log.d(TAG, " selectPickerrecoverPacakgeList , picker = " + picker);

            int size = getCount();
            boolean isCheck = false;
            Context context = mRecoverList.getActivity();

            mRecoverList.recoverPacakgeList.clear();

            //          java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MM/dd/yyyy");
            java.text.DateFormat format = DateFormat.getDateFormat(context);

            Date pickerData = picker.getTime();

            for (int i = 0; i < size; i++) {
                ItemInfo entry = items.get(i);

                try {
                    java.util.Date dateEntry = format.parse(entry.installed);

                    String dateString = format.format(dateEntry);
                    String dateString2 = format.format(pickerData);

                    Log.d(TAG, "dateEntry = " + dateString);
                    Log.d(TAG, "pickerData = " + dateString2);

                    if (dateEntry.before(pickerData)) {
                        isCheck = false;
                    } else if (dateEntry.after(pickerData) || dateString.equals(dateString2)) {
                        isCheck = true;
                    }
                    Log.d(TAG, "isCheck = " + isCheck);
                } catch (java.text.ParseException ex) {
                    ex.printStackTrace();
                }

                if (isCheck) {
                    mRecoverList.recoverPacakgeList.add(entry.packageName);
                    mRecoverList.mListView.setItemChecked(i, true);
                }
            }
        }

        @Override
        public void onMovedToScrapHeap(View arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }
    }

    public class AppsMultiRecoverTask extends AsyncTask<Integer, String, Integer> {
        private ProgressDialog mRecoverProgDlg;
        private Context mContext;
        private boolean mCancle;
        private int mTaskCnt;
        private int mLoopCnt;
        //private int mRecoverFailCnt;
        private int mSuccessCnt;
        private boolean mIsStorageFull;

        public AppsMultiRecoverTask(Context context) {
            this.mContext = context;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            try {
                if (mRecoverProgDlg != null) {
                    mRecoverProgDlg.dismiss();
                    mRecoverProgDlg = null;
                }
                mIsStorageFull = false;
                gv.setIsTherad(true);
                mSuccessCnt = 0;
                mLoopCnt = 0;
                //mRecoverFailCnt = 0;
                mCancle = false;
                mTaskCnt = recoverPacakgeList.size();
                mRecoverProgDlg = new ProgressDialog(mContext);
                //                mRecoverProgDlg.setTitle(getResources().getString(R.string.sp_deleting_NORMAL));
                mRecoverProgDlg.setMessage(getResources().getString(
                        R.string.settings_apps_recovering));
                mRecoverProgDlg.setCancelable(true);
                mRecoverProgDlg.setCanceledOnTouchOutside(false);
                //                mRecoverProgDlg.setButton(getResources().getString(R.string.dlg_cancel),
                //                                        new DialogInterface.OnClickListener() {
                //                    @Override
                //                    public void onClick(final DialogInterface arg0, final int arg1) {
                //                        Log.d (TAG, "onClick Cancel");
                //                        // TODO Auto-generated method stub
                //                        if (mAppsMultiRecoverTask != null) {
                //                            mAppsMultiRecoverTask.onCancelled();
                //                            getActivity().finish();
                //                        }
                //                    }
                //                });

                mRecoverProgDlg.setOnCancelListener(new ProgressDialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.d(TAG, "setOnCancelListener");
                        if (mAppsMultiRecoverTask != null) {
                            mAppsMultiRecoverTask.onCancelled();
                            getActivity().onBackPressed();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            mRecoverProgDlg.show();
            super.onPreExecute();
        }

        private void onRecoverSendBradcast(int i) {
            if (i < mTaskCnt) {
                Log.i(TAG, "onRecoverSendBradcast i =  " + i);
                Intent intent = new Intent(
                        EnumManager.IntentActionType.REQUEST_RECOVERY.getValue());
                intent.putExtra(
                        EnumManager.ExtraNameType.EXTRA_PACKAGE_NAME.getValue(),
                        recoverPacakgeList.get(i));
                mContext.sendBroadcast(intent);
            }
            setSharedStringPreferences(RESULT_KEY, "SendEvent", mContext);
            Log.d(TAG, " mLoopCnt = " + mLoopCnt + ", mTaskCnt = " + mTaskCnt);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            boolean Firstintent = true;
            while (mLoopCnt < mTaskCnt) {
                getSharedStringPreferences(RESULT_KEY, recover_result, mContext);

                if (Firstintent) {
                    onRecoverSendBradcast(mLoopCnt);
                    Firstintent = false;
                }

                if (recover_result.equals(EnumManager.RequestResult.SUCCESS.getValue())) {
                    mSuccessCnt++;
                    if (mCancle) {
                        Log.d(TAG, " mCancle1 = " + mCancle);
                        return mTaskCnt;
                    }
                    onRecoverSendBradcast(++mLoopCnt);
                } else if (recover_result.equals(EnumManager.RequestResult.FAIL.getValue())) {
                    if (mCancle) {
                        Log.d(TAG, " mCancle2 = " + mCancle);
                        return mTaskCnt;
                    }

                    getSharedStringPreferences(MESSAGE_KEY, recover_msg, mContext);
                    if (!recover_msg.equals(EnumManager.RequestResult.NULL.getValue())) {
                        //mRecoverFailCnt++;
                        Log.e(TAG, "Recover is fail recover_msg = " + recover_msg);
                        // memory full case
                        if (recover_msg.equals(EnumManager.RecoveryMessage.
                                INSTALL_FAILED_INSUFFICIENT_STORAGE.getValue())) {
                            Log.d(TAG, "Restore Stop111 : " + recover_msg);
                            //StorageFullDlg();
                            mIsStorageFull = true;
                            return mTaskCnt;
                        } else {
                            Log.d(TAG, "Restore Stop222 : " + recover_msg);
                            onRecoverSendBradcast(++mLoopCnt);
                        }
                    } else {
                        Log.d(TAG, "recover_msg is null!!! roop gogo~~");
                    }
                } else if (recover_result.equals("SendEvent")) {
                    if (mCancle) {
                        Log.d(TAG, "Not cancel!!! because package installing~~ ");
                    }
                    //                    Log.d (TAG, "Installing -> " + recoverPacakgeList.get(mLoopCnt));
                    //                    continue;
                }
            }
            return mTaskCnt;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
        }

        @Override
        protected void onPostExecute(Integer result) {
            gv.setIsTherad(false);
            try {
                if (mRecoverProgDlg != null) {
                    mRecoverProgDlg.dismiss();
                    mRecoverProgDlg = null;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (mSuccessCnt == 0) {
                Toast.makeText(mContext, mContext.getString(R.string.settings_apps_recover_allfail)
                        , Toast.LENGTH_LONG).show();
            } else {
                String language = Locale.getDefault().getLanguage();
                if (language.equals("ko")) {
                    Toast.makeText(mContext, mContext.getString(
                            R.string.settings_apps_multi_recover_complete,
                            getDelListSize(), mSuccessCnt), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, mContext.getString(
                            R.string.settings_apps_multi_recover_complete,
                            mSuccessCnt, getDelListSize()), Toast.LENGTH_LONG).show();
                }
            }

            if (recoverPacakgeList != null) {
                recoverPacakgeList.clear();
                Log.i(TAG, "recoverPacakgeList clear");
            }

            if (!mCancle) {
                try {
                    setSharedStringPreferences(ManageApplications.APPS_PREFS_UPDATE_DATA,
                            ManageApplications.APPS_PREFS_UPDATE_DATA_YES, getActivity());
                    getActivity().onBackPressed();
                } catch (Exception e) {
                    Log.d(TAG, "getActivity is null");
                }
            }
            if (mAppsMultiRecoverTask != null) {
                mAppsMultiRecoverTask = null;
            }

            if (mIsStorageFull) {
                Intent intent = new Intent("com.lge.settings.ACTION_STORAGE_FULL");
                startActivity(intent);
            }
        }

        protected void onCancelled() {
            Log.d(TAG, "onCancelled");
            gv.setIsTherad(false);
            mCancle = true;
            if (mRecoverProgDlg != null) {
                mRecoverProgDlg.dismiss();
                mRecoverProgDlg = null;
            }
        }
    }

    public void updateUI() {
        mRecoverDelAdapter.resume(mSortOrder);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();

        // rebestm - mBreadCrumb
        if (Utils.isTablet() || Utils.isWifiOnly(getActivity())) {
//            mListView.bringToFront();
            if (SettingsBreadCrumb.isAttached(getActivity())) {
                if (mRecoverDelAdapter.getCount() > 0) {
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
        if (mBreadCrumb != null) {
            mBreadCrumb.removeSwitch();
        }
    }

    OnKeyListener onKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View arg0, int keycode, KeyEvent event) {
            Log.d(TAG, "AppsMultiRecoverTask onKey keyCode = " + keycode);
            if (keycode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                recoverPacakgeList.clear();

                return true;
            }
            return false;
        }
    };

    private boolean isSupportedRecover(Context content) {
        return ManageApplications.isSafeMode(getActivity());
    }

    public void getSharedStringPreferences(String name, String value, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APPS_PREFS, 0);

        if (name.equals(RESULT_KEY)) {
            recover_result = preferences.getString(name, null);
        } else if (name.equals(MESSAGE_KEY)) {
            recover_msg = preferences.getString(name, null);
            Log.e(TAG, "recover_msg111 = " + recover_msg);
        }
    }

    public void setSharedStringPreferences(String name, String value, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APPS_PREFS, 0);
        preferences.edit().putString(name, value).commit();
    }
    // not used
    /*    public void StorageFullDlg() {
            AlertDialog mStorageFullDlgDialog;
            mStorageFullDlgDialog = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.settings_apps_recover_memoryfull_title))
                .setMessage(getString(R.string.settings_apps_recover_memoryfull))
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.ok, null).create();
            mStorageFullDlgDialog.show();
       }
    */
}
