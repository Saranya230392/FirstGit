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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.TouchFeedbackAndSystemPreference;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessLockDetails extends PreferenceFragment {
    static final String TAG = "AccessLockDetails";

    public static final String ARG_PACKAGE_NAME = "package";

    private AccessLockState mState;
    private PackageManager mPm;
    private AppOpsManager mAppOps;
    private PackageInfo mPackageInfo;
    //private LayoutInflater mInflater;
    private View mRootView;
    private TextView mAppVersion;
    private PreferenceScreen ps;
    private LinearLayout mNoFeatureLayout;

    // Utility method to set application label and icon.
    private void setAppLabelAndIcon(PackageInfo pkgInfo) {
        final View appSnippet = mRootView.findViewById(R.id.app_snippet);
        appSnippet.setPaddingRelative(0, appSnippet.getPaddingTop(), 0,
                appSnippet.getPaddingBottom());

        ImageView icon = (ImageView)appSnippet.findViewById(R.id.app_icon);
        icon.setImageDrawable(mPm.getApplicationIcon(pkgInfo.applicationInfo));
        // Set application name.
        TextView label = (TextView)appSnippet.findViewById(R.id.app_name);
        label.setText(mPm.getApplicationLabel(pkgInfo.applicationInfo));
        // Version number of application
        mAppVersion = (TextView)appSnippet.findViewById(R.id.app_size);

        if (pkgInfo.versionName != null) {
            mAppVersion.setVisibility(View.VISIBLE);
            mAppVersion.setText(getActivity().getString(R.string.version_text,
                    String.valueOf(pkgInfo.versionName)));
        } else {
            mAppVersion.setVisibility(View.INVISIBLE);
        }
    }

    private String retrieveAppEntry() {
        final Bundle args = getArguments();
        String packageName = (args != null) ? args.getString(ARG_PACKAGE_NAME) : null;
        if (packageName == null) {
            Intent intent = (args == null) ?
                    getActivity().getIntent() : (Intent)args.getParcelable("intent");
            if (intent != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }
        }
        try {
            mPackageInfo = mPm.getPackageInfo(packageName,
                    PackageManager.GET_DISABLED_COMPONENTS |
                            PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Exception when retrieving package:" + packageName, e);
            mPackageInfo = null;
        }

        return packageName;
    }

    private boolean refreshUi() {
        if (mPackageInfo == null) {
            return false;
        }

        setAppLabelAndIcon(mPackageInfo);
        Resources res = getActivity().getResources();

        ps.removeAll();
        //mOperationsSection.removeAllViews();
        //String lastPermGroup = "";
        ArrayList<CheckBoxPreference> cp_array = new ArrayList<CheckBoxPreference>();
        for (AccessLockState.OpsTemplate tpl : AccessLockState.ALL_TEMPLATES) {
            List<AccessLockState.AppOpEntry> entries = mState.buildState(tpl,
                    mPackageInfo.applicationInfo.uid, mPackageInfo.packageName);
            for (final AccessLockState.AppOpEntry entry : entries) {
                final AppOpsManager.OpEntry firstOp = entry.getOpEntry(0);

                /*
                String perm = AppOpsManager.opToPermission(firstOp.getOp());
                if (perm != null) {
                    try {
                        PermissionInfo pi = mPm.getPermissionInfo(perm, 0);
                        if (pi.group != null && !lastPermGroup.equals(pi.group)) {
                            lastPermGroup = pi.group;
                            PermissionGroupInfo pgi = mPm.getPermissionGroupInfo(pi.group, 0);
                            if (pgi.icon != 0) {
                            }
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
                */

                CheckBoxPreference cp = new CheckBoxPreference(getActivity());
                cp.setTitle(entry.getSwitchText(mState));
                cp.setSummary(entry.getTimeText(res, true));
                final int switchOp = AppOpsManager.opToSwitch(firstOp.getOp());
                Log.i(TAG, "title : " + entry.getSwitchText(mState) + "   " +
                           "switchOp :" + switchOp);

                cp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        // TODO Auto-generated method stub
                        Log.i(TAG, "Title : " + preference.getTitle() +
                                   "    switchop : " + switchOp + "" +
                                   "   uid : " + entry.getPackageOps().getUid());
                        mAppOps.setMode(switchOp, entry.getPackageOps().getUid(),
                                entry.getPackageOps().getPackageName(), (Boolean)newValue
                                        ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_IGNORED);
                        ((CheckBoxPreference)preference).setChecked((Boolean)newValue);
                        return true;
                    }
                });
                cp.setChecked(mAppOps.checkOp(switchOp, entry.getPackageOps().getUid(),
                        entry.getPackageOps().getPackageName()) == AppOpsManager.MODE_ALLOWED);

                if (AppOpsManager.OP_WRITE_SMS != switchOp) {
                    cp_array.add(cp);
                }

                //ps.addPreference(cp);
            }
        }

        Collections.sort(cp_array);
        for (int i = 0; i < cp_array.size(); i++) {
            ps.addPreference(cp_array.get(i));
        }

        Log.i(TAG, "preference no1 : " + ps.getPreferenceCount());
        if (0 != ps.getPreferenceCount()) {
            Log.i(TAG, "Delete no feature layout");
            mNoFeatureLayout.setVisibility(View.GONE);
        }
        else {
            Log.i(TAG, "List delete");
            LinearLayout list_layout = (LinearLayout)mRootView.findViewById(R.id.list_layout);
            TextView desc = (TextView)mRootView.findViewById(R.id.tv_desc);
            desc.setVisibility(View.GONE);
            list_layout.setVisibility(View.GONE);
        }

        return true;
    }

    private void setIntentAndFinish(boolean finish, boolean appChanged) {
        Intent intent = new Intent();
        intent.putExtra(ManageApplications.APP_CHG, appChanged);
        PreferenceActivity pa = (PreferenceActivity)getActivity();
        pa.finishPreferencePanel(this, Activity.RESULT_OK, intent);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(R.string.access_request_title);
        if (Utils.supportSplitView(getActivity())) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } else {
            if (actionBar != null) {
               actionBar.setDisplayHomeAsUpEnabled(true);
               actionBar.setIcon(R.drawable.shortcut_security);
            }
        }

        Log.i(TAG, "locail class name : " + getActivity().getLocalClassName());

        mState = new AccessLockState(getActivity());
        mPm = getActivity().getPackageManager();
        //mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAppOps = (AppOpsManager)getActivity().getSystemService(Context.APP_OPS_SERVICE);
        retrieveAppEntry();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.access_lock_details, container, false);
        mRootView = view;
        ps = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(ps);
        mNoFeatureLayout = (LinearLayout)view.findViewById(R.id.no_contacts_layout);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!refreshUi()) {
            setIntentAndFinish(true, true);
        }
    }

    private boolean isCallToPKGManager() {
        if ("SubSettings".equals(getActivity().getLocalClassName())) {
            return false;
        }
        return true;
    }
}
