/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.settings;

import com.android.settings.applications.ManageApplications;
import com.android.settings.fuelgauge.PowerSaveBatteryDetail;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.widget.TextView;
import com.android.settings.R;

public class UsageManagerActivity extends Activity {
    static final String TAG = "UsageManagerActivity";
    private ActionBar mActionBar;
    private Configuration mCurrentConfig;
    private final ActionBarTabSetter mTabSetter = new ActionBarTabSetter();
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (Utils.isUI_4_1_model(this)) {
            setContentView(R.layout.usage_manager);
            mCurrentConfig = getResources().getConfiguration();
            setUpActionBarTabs(0);
            mHandler = new Handler();
        } else {
            createTabs();
        }


    }

    private void createTabs() {

        mActionBar = getActionBar();

        if (mActionBar == null) {
            Log.d(TAG, "ActionBar is null.");
        } else {
            mActionBar.removeAllTabs();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            setContentView(R.layout.usage_manager);

            final ActionBar.Tab batteryTab = mActionBar.newTab();
            final ActionBar.Tab appsTab = mActionBar.newTab();
            final ActionBar.Tab dataTab = mActionBar.newTab();
            batteryTab.setText(R.string.power_usage_summary_title);
            batteryTab.setTabListener(new UsageManagerTabListener<PowerSaveBatteryDetail>(this, "battery", PowerSaveBatteryDetail.class));

            appsTab.setText(R.string.applications_settings);
            appsTab.setTabListener(new UsageManagerTabListener<ManageApplications>(this, "apps", ManageApplications.class));

            dataTab.setText(R.string.data_size_label);
            dataTab.setTabListener(new UsageManagerTabListener<DataUsageSummary>(this, "data", DataUsageSummary.class));

            mActionBar.addTab(batteryTab);
            mActionBar.addTab(appsTab);
            mActionBar.addTab(dataTab);
        }
    }

    private void setUpActionBarTabs(int initSelection) {
        ActionBar ab = getActionBar();

        if (mCurrentConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getActionBar().setDisplayShowTitleEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setTitle(getString(R.string.usage_manager_title));
        } else {
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowHomeEnabled(false);
        }

        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.removeAllTabs();

        UsageManagerTabListener tmp;

        tmp = new UsageManagerTabListener<PowerSaveBatteryDetail>(this, "battery",
                PowerSaveBatteryDetail.class);
        addNewTab(ab, getString(R.string.power_usage_summary_title), tmp);

        tmp = new UsageManagerTabListener<ManageApplications>(this, "apps",
                ManageApplications.class);
        addNewTab(ab, getString(R.string.applications_settings), tmp);

        tmp = new UsageManagerTabListener<DataUsageSummary>(this, "data", DataUsageSummary.class);
        addNewTab(ab, getString(R.string.data_size_label), tmp);

        ab.setSelectedNavigationItem(initSelection);
        displayTabDivider(initSelection);
    }

    private void addNewTab(ActionBar ab, String tabText, UsageManagerTabListener ut) {
        ActionBar.Tab tab = ab.newTab();
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.tab_view, null);
        LayoutParams lp;

        if (mCurrentConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("jw", "ORIENTATION_PORTRAIT");
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

        } else {
            Log.d("jw", "NOT ORIENTATION_PORTRAIT");
            int width = getResources().getDimensionPixelSize(R.dimen.tab_width_land) / 3;
            lp = new LinearLayout.LayoutParams(width,
                    LinearLayout.LayoutParams.MATCH_PARENT);

        }

        customView.setLayoutParams(lp);
        tab.setCustomView(customView);

        ((TextView)customView.findViewById(R.id.tabText)).setText(tabText);

        tab.setTabListener(ut);

        ab.addTab(tab);
    }

    private void displayTabDivider(int selectedPosition) {
        if (!Utils.isUI_4_1_model(this)) {
            return;
        }
        ActionBar ab = getActionBar();
        int tabCount = ab.getNavigationItemCount();

        for (int i = 0; i < tabCount; i++) {
            Tab tab = ab.getTabAt(i);
            View vi = tab.getCustomView();
            View divider = vi.findViewById(R.id.tabDivider);
            TextView tv = (TextView)vi.findViewById(R.id.tabText);

            if (i == selectedPosition) {
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setTypeface(null, Typeface.NORMAL);
            }

            if (i == selectedPosition || i == selectedPosition - 1
                    || i == tabCount - 1) {
                divider.setVisibility(View.INVISIBLE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }
        }
    }

    public class UsageManagerTabListener<T extends Fragment> implements TabListener {
        private Fragment mFragment;
        private final Context mContext;
        private final Class<T> mClass;
        private final String mTag;

        public UsageManagerTabListener(Context context, String tag, Class<T> clazz) {
            mContext = context;
            mTag = tag;
            mClass = clazz;
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mContext, mClass.getName());
            }
            displayTabDivider(tab.getPosition());
            ft.replace(R.id.usage_manage_content, mFragment, mTag);

        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        if (Utils.isUI_4_1_model(this)) {
            mCurrentConfig = newConfig;

            ActionBar ab = getActionBar();
            mTabSetter.mSaveTabIndex = ab.getSelectedNavigationIndex();

            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

            mHandler.post(mTabSetter);
        }
        super.onConfigurationChanged(newConfig);
    }

    class ActionBarTabSetter implements Runnable {
        int mSaveTabIndex = 0;

        @Override
        public void run() {
            setUpActionBarTabs(mSaveTabIndex);
        }

    }
}
