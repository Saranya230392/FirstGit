package com.android.settings.lge;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

import android.provider.Settings;
import android.content.ComponentName;

import android.Manifest;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.text.TextUtils.SimpleStringSplitter;
import com.android.settings.lgesetting.Config.Config;
import java.util.Collections;
import java.util.Comparator;
import java.text.Collator;
import android.os.SystemProperties;

public class HideNavigationAppSelect extends SettingsPreferenceFragment {
    private static final String TAG = "HideNavigationAppSelect";
    private static final int SUMMARY_PACKAGENAME_COLUMN_INDEX = 1;
    private static final int SUMMARY_APPNAME_COLUMN_INDEX = 2;
    private static final int SELECT_ALL_ID = 1;
    private static final int DESELECT_ALL_ID = 2;
    private static final int HELP_ID = 3;
    private static final int HELP_REQUEST = -1;

    private IActivityManager mActivityManager = null;
    private MatrixCursor mMatrixCursor = null;
    private ArrayList<AppData> mAppList = new ArrayList<AppData>();
    private List<String> mIgnoreList = null;
    private ArrayList<String> mPackageNameList = new ArrayList<String>();
    private boolean isExistDB = false;
    private boolean isSelectedAll = false;
    private HideNavigationComparator mComparator;
    private Menu mMenu;
    private CheckBox mVisible;
    private ImageView mAniImage;
    private AnimationDrawable mAni;
    private Runnable mRunAni;
    private TextView mSummaryText;
    private boolean mIsDialogCheck = false;
    //private static final String BOOTUP_APK_PATH = Environment.getRootDirectory() + "/apps/bootup";
    private ListView list;

    private Dialog mInitGuideDialog;
    private ArrayList<AppData> mSelectedPackageList = new ArrayList<AppData>();
    private ArrayList<AppData> mUnSelectedPackageList = new ArrayList<AppData>();

    private static final char ENABLED_HIDE_NAVIGATION_SEPARATOR = ':';
    private final static SimpleStringSplitter sStringColonSplitter =
            new SimpleStringSplitter(ENABLED_HIDE_NAVIGATION_SEPARATOR);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity().getApplicationContext())) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } else {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        setHasOptionsMenu(false);

        if (!"VZW".equals(Config.getOperator())) {
            mIsDialogCheck = true;
            showInitialGuidePopup();
        }
    }

    private void showInitialGuidePopup() {
    	int currentValue = Settings.System.getInt(getActivity().getContentResolver(),
                "hide_navigation_do_not_show", 0);
        if (currentValue == 1) {
            return;
        }
    	
    	if (mInitGuideDialog == null) {
            createInitialGuidePopup();
        }
         
        setUpGuideDialogContent();
        mInitGuideDialog.show();
    }
    
    private void createInitialGuidePopup() {
        int dialog_theme_res = getResources().getIdentifier("Theme.LGE.White.Dialog.MinWidth", "style", "com.lge");
         
        mInitGuideDialog = new Dialog(getActivity(), dialog_theme_res);
        mInitGuideDialog.setCanceledOnTouchOutside(false);
         
        Window dialogWindow = mInitGuideDialog.getWindow();
        dialogWindow.requestFeature(Window.FEATURE_NO_TITLE);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    
    private void setUpGuideDialogContent() {
        int init_guide = getResources().getIdentifier("dialog_init_guide", "layout", "com.lge");
        mInitGuideDialog.setContentView(init_guide);
     
        TextView title = (TextView)mInitGuideDialog.findViewById(android.R.id.title);
        mAniImage = (ImageView)mInitGuideDialog.findViewById(android.R.id.icon);
        TextView text = (TextView)mInitGuideDialog.findViewById(android.R.id.message);
        mVisible = (CheckBox)mInitGuideDialog.findViewById(android.R.id.checkbox);
        Button button = (Button)mInitGuideDialog.findViewById(android.R.id.button1); 
        
        title.setText(R.string.hide_home_navigation_title);
        mAniImage.setBackgroundResource(R.anim.ani_hide_navigation_guide);

        mAni = (AnimationDrawable)mAniImage.getBackground();
        mAniImage.postDelayed(mRunAni = new Runnable() {
            public void run() {
                if (mAni != null) {
                    mAni.start();
                }
            }
        }, 500);
        
        text.setText(R.string.hide_home_touch_button_help_desc_ex);
         
        mVisible.setText(R.string.develop_do_not_show);
        mVisible.setChecked(mIsDialogCheck);
        mVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            }
        });
         
        button.setText(R.string.dlg_ok);
        button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mVisible.isChecked()) {
            		Settings.System.putInt(getContentResolver(), 
            				"hide_navigation_do_not_show", 1);
				}
            	mInitGuideDialog.dismiss();
			}
		});
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (mInitGuideDialog != null && mInitGuideDialog.isShowing()) {
            mIsDialogCheck = mVisible.isChecked();
            setUpGuideDialogContent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        makingList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        LayoutInflater mInflater = inflater;
        View rootView = mInflater.inflate(R.layout.hide_navigation_view,
                container, false);
        rootView.findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
        mSummaryText = (TextView)rootView.findViewById(R.id.summary_textview);
        mSummaryText.setText(R.string.hide_home_navigation_select_apps_desc);
        if ("VZW".equals(Config.getOperator())) {
            mSummaryText
                    .setText(R.string.hide_home_navigation_select_apps_desc_vzw);
        }
        if (mActivityManager == null) {
            mActivityManager = ActivityManagerNative.getDefault();
        }

        mIgnoreList = Arrays.asList(getResources().getStringArray(
                R.array.hide_navigation_ignorelist));
        if ("KDDI".equals(Config.getOperator())) {
            addKDDIOperatorIgnoreList();
        }

        registerBReceiver();
        mComparator = new HideNavigationComparator();
        new MakePackagesListTask(this).execute();

        // TODO Auto-generated method stub
        return rootView;
    }

    private class MakePackagesListTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<HideNavigationAppSelect> mHideNavigation;

        //private ProgressDialog mProgressDialog;

        public MakePackagesListTask(HideNavigationAppSelect mHideNavi) {
            mHideNavigation = new WeakReference<HideNavigationAppSelect>(mHideNavi);
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            final HideNavigationAppSelect mHideNavi = mHideNavigation.get();
            if (mHideNavi != null) {
                mHideNavi.makingList();
            }
        }
    }

    private void addKDDIOperatorIgnoreList() {
        ArrayList<String> mKDDIIgnoreList = new ArrayList<String>();
        List<String> mTempIgnoreList = null;
        mTempIgnoreList = Arrays.asList(getResources().
                getStringArray(R.array.hide_navigation_ignorelist_kddi));
        mKDDIIgnoreList.addAll(mIgnoreList);
        mKDDIIgnoreList.addAll(mTempIgnoreList);

        mIgnoreList = mKDDIIgnoreList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            getActivity().onBackPressed();
            return true;
        case SELECT_ALL_ID:
            if (mAppList.size() > 0) {
                setSelectAll();
            }
            return true;
        case DESELECT_ALL_ID:
            if (mAppList.size() > 0) {
                setDeselectAll();
            }
            return true;
        case HELP_ID:
            startFragment(this, HideNavigationAppHelp.class.getCanonicalName(),
                    HELP_REQUEST, null, R.string.tethering_help_button_text);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mMenu = menu;
        checkProperOptionMenu();
    }

    public void checkProperOptionMenu() {
        mMenu.removeItem(SELECT_ALL_ID);
        mMenu.removeItem(DESELECT_ALL_ID);
        mMenu.removeItem(HELP_ID);

        if (checkAllItemSelected()) {
            //mMenu.add(0, DESELECT_ALL_ID, 0,  getResources().getString(R.string.deselect_all));
            mMenu.add(0, HELP_ID, 0,
                    getResources().getString(R.string.tethering_help_button_text));
        } else {
            //mMenu.add(0, SELECT_ALL_ID, 0,  getResources().getString(R.string.select_all));
            mMenu.add(0, HELP_ID, 0,
                    getResources().getString(R.string.tethering_help_button_text));
        }
    }

    public boolean checkAllItemSelected() {
        String enableHideApp = readDB();
        if (enableHideApp == null) {
            enableHideApp = "";
        }

        SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enableHideApp);
        int count = 0;

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            for (int i = 0; i < mAppList.size(); i++) {
                if (componentNameString.equals(mAppList.get(i).packageName)) {
                    count++;
                }
            }
        }

        Log.d(TAG, "mAppList.size() : " + mAppList.size());
        Log.d(TAG, "count : " + count);

        if ((mAppList.size() == count) && (mAppList.size() != 0)) {
            isSelectedAll = true;
            return true;
        } else {
            isSelectedAll = false;
            return false;
        }
    }

    private void makingList() {
        PackageManager pm = getActivity().getPackageManager();

        mMatrixCursor = new MatrixCursor(new String[] { "_id", "packagename", "appname" });
        try {
            synchronized (getActivity()) {
                //only app that is shown in launcher is added to the list
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);

                mainIntent.addCategory(Intent.CATEGORY_HOME);

                int _id = 0;
                ResolveInfo rInfo;

                String appName;
                String packageName;
                mAppList.clear();
                mPackageNameList.clear();
                mSelectedPackageList.clear();
                mUnSelectedPackageList.clear();
                for (int i = 0; i < list.size(); i++) {
                    rInfo = list.get(i);
                    packageName = rInfo.activityInfo.applicationInfo.packageName;
                    appName = pm.getApplicationLabel(rInfo.activityInfo.applicationInfo).toString();

                    if (packageName == null) {
                        continue;
                    }

                    if (mIgnoreList.contains(packageName)) {
                        continue;
                    }

                    if (!mPackageNameList.contains(packageName)) {
                        AppData appData = new AppData();
                        appData.appName = appName;
                        appData.packageName = packageName;
                        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                            if (isSeletedItem(packageName)) {
                                mSelectedPackageList.add(appData);
                            } else {
                                mUnSelectedPackageList.add(appData);
                            }
                        } else {
                            mAppList.add(appData);
                        }
                        mPackageNameList.add(packageName);                        
                    }
                }
                if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
                    addItemCursor(mSelectedPackageList, _id);
                    addItemCursor(mUnSelectedPackageList, _id);
                } else {
                    addItemCursor(mAppList, _id);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception occurred " + e);
        }

        if (com.lge.os.Build.LGUI_VERSION.RELEASE >= com.lge.os.Build.LGUI_VERSION_NAMES.V4_2) {
            mAppList.addAll(mSelectedPackageList);
            mAppList.addAll(mUnSelectedPackageList);
        }

        list = getListView();
        PackageConfigListItemAdapter adapter = new PackageConfigListItemAdapter(getActivity(),
                R.layout.hide_navigation_item_row, mMatrixCursor);
        if (mAppList.size() == 0) {
            list.setEmptyView(getView().findViewById(android.R.id.empty));
        }
        list.setAdapter(adapter);
        if (list != null) {
            list.setFastScrollEnabled(true);
        }
        if (mMenu != null) {
            //checkProperOptionMenu();
        }
    }

    private boolean isSeletedItem(String pkg_name) {
        String enableHideApp = readDB();
        if (enableHideApp == null) {
            enableHideApp = "";
        }

        SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enableHideApp);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            if (componentNameString.equals(pkg_name)) {
                return true;
            }
        }
        return false;
    }

    private void addItemCursor(ArrayList<AppData> list, int cursorId) {
        int total = list.size();
        if (total != 0) {
            Collections.sort(list, mComparator);
            for (int i = 0; i < total; i++) {
                mMatrixCursor.addRow(new Object[] { cursorId,
                        list.get(i).packageName, list.get(i).appName });
                cursorId++;
            }
        }
    }

    private final class PackageConfigListItemAdapter extends ResourceCursorAdapter {
        public PackageConfigListItemAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final PackageListItemCache cache = (PackageListItemCache)view.getTag();
            cache.packageName = cursor.getString(SUMMARY_PACKAGENAME_COLUMN_INDEX);
            cache.appName = cursor.getString(SUMMARY_APPNAME_COLUMN_INDEX);
            cache.nameView.setText(cache.appName);
            Drawable icon = null;
            try {
                icon = getPackageManager().getApplicationIcon(cache.packageName);
            } catch (NameNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            cache.iconView.setImageDrawable(icon);

            String enableHideApp = readDB();
            if (enableHideApp == null) {
                enableHideApp = "";
            }

            SimpleStringSplitter colonSplitter = sStringColonSplitter;
            colonSplitter.setString(enableHideApp);

            if (isSelectedAll) {
                cache.checkBox.setChecked(true);
            } else {
                if (enableHideApp.equals("")) {
                    cache.checkBox.setChecked(false);
                } else {
                    while (colonSplitter.hasNext()) {
                        String componentNameString = colonSplitter.next();
                        if (componentNameString.equals(cache.packageName)) {
                            cache.checkBox.setChecked(true);
                            break;
                        } else {
                            cache.checkBox.setChecked(false);
                        }
                    }
                }
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            PackageListItemCache cache = new PackageListItemCache();
            cache.iconView = (ImageView)view.findViewById(R.id.app_icon);
            cache.nameView = (TextView)view.findViewById(R.id.Name);
            cache.checkBox = (CheckBox)view.findViewById(R.id.CheckBox01);

            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    final PackageListItemCache cache = (PackageListItemCache)v.getTag();
                    boolean enable = !cache.checkBox.isChecked();
                    if (!cache.checkBox.isEnabled()) {
                        return;
                    }
                    cache.checkBox.setChecked(enable);
                    if (mActivityManager == null) {
                        mActivityManager = ActivityManagerNative.getDefault();
                    }

                    if (enable) {
                        setHideNavigationApp(cache.packageName);
                    } else {
                        deleteHideNavigationApp(cache.packageName);
                    }

                    try {
                        mActivityManager.forceStopPackage(
                                cache.packageName, UserHandle.myUserId());
                    } catch (android.os.RemoteException e) {
                        e.printStackTrace();
                    }

                    //checkProperOptionMenu();
                }
            });
            view.setTag(cache);
            return view;
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        getActivity().unregisterReceiver(mPackageAdd);
        mMatrixCursor = null;
        Utils.recycleView(list);
        if (mInitGuideDialog != null) {
            mAni.stop();
            mAniImage.removeCallbacks(mRunAni);
            mAniImage.setBackgroundDrawable(null);
            mRunAni = null;
        }  
        System.gc();
        super.onDestroy();
    }

    private void registerBReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        getActivity().registerReceiver(mPackageAdd, filter);
    }

    private final BroadcastReceiver mPackageAdd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pkgName = intent.getData().getSchemeSpecificPart();
            String action = intent.getAction();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            Log.d(TAG, "Receive package name : " + pkgName);
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    deleteHideNavigationApp(pkgName);
                }
            }
            makingList();
        }
    };

    static final class PackageListItemCache {
        public ImageView iconView;
        public TextView nameView;
        public CheckBox checkBox;
        public String appName;
        public String packageName;
    }

    static final class AppData {
        public String appName;
        public String packageName;
    }

    private void setHideNavigationApp(String mPackageMame) {
        String enableHideApp = readDB();
        if (enableHideApp == null) {
            enableHideApp = "";
        }

        StringBuilder enabledHideNavigationBuilder = new StringBuilder(enableHideApp);
        enabledHideNavigationBuilder.append(mPackageMame);
        enabledHideNavigationBuilder.append(ENABLED_HIDE_NAVIGATION_SEPARATOR);
        Log.d(TAG, "Add componentNameString : " + mPackageMame);
        writeDB(enabledHideNavigationBuilder.toString());
    }

    private void deleteHideNavigationApp(String mPackageMame) {
        String enableHideApp = readDB();
        if (enableHideApp == null) {
            enableHideApp = "";
        }

        Log.d(TAG, "Delete componentNameString : " + mPackageMame);
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
        return Settings.System.getString(getActivity().getContentResolver(),
                "enable_hide_navigation_apps");
    }

    private void writeDB(String str) {
        Settings.System.putString(getActivity().getContentResolver(),
                "enable_hide_navigation_apps", str);
        Log.d(TAG,
                "write HideNavigation app DB : "
                        + Settings.System.getString(getActivity().getContentResolver(),
                                "enable_hide_navigation_apps"));
    }

    private void setSelectAll() {
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            CheckBox tempCheckBox = (CheckBox)child.findViewById(R.id.CheckBox01);
            if (!tempCheckBox.isChecked()) {
                tempCheckBox.setChecked(true);
            }
        }
        isSelectedAll = true;
        writeDB("");

        String enableHideApp = readDB();
        if (enableHideApp == null) {
            enableHideApp = "";
        }

        StringBuilder enabledHideNavigationBuilder = new StringBuilder(enableHideApp);
        for (int j = 0; j < mAppList.size(); j++) {
            enabledHideNavigationBuilder.append(mAppList.get(j).packageName);
            enabledHideNavigationBuilder.append(ENABLED_HIDE_NAVIGATION_SEPARATOR);
            try {
                mActivityManager.forceStopPackage(
                        mAppList.get(j).packageName, UserHandle.myUserId());
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }
        writeDB(enabledHideNavigationBuilder.toString());
    }

    private void setDeselectAll() {
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            CheckBox tempCheckBox = (CheckBox)child.findViewById(R.id.CheckBox01);
            if (tempCheckBox.isChecked()) {
                tempCheckBox.setChecked(false);
            }
        }

        String enableHideApp = readDB();

        if (enableHideApp == null) {
            enableHideApp = "";
        }
        SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enableHideApp);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            try {
                mActivityManager.forceStopPackage(
                        componentNameString, UserHandle.myUserId());
            } catch (android.os.RemoteException e) {
                e.printStackTrace();
            }
        }

        isSelectedAll = false;
        writeDB("");
    }

    private static class HideNavigationComparator implements Comparator<AppData> {
        private final Collator mCollator = Collator.getInstance();

        @Override
        public int compare(AppData lhs, AppData rhs) {
            return mCollator.compare(sortKey(lhs), sortKey(rhs));
        }

        private String sortKey(AppData di) {
            StringBuilder sb = new StringBuilder();
            sb.append(di.appName);
            return sb.toString();
        }
    }
}
