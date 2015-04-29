/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.settings.lge;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.android.settings.SettingsBreadCrumb;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.DreamBackend.DreamInfo;
import com.android.settings.hotkey.HotkeyInfo;
import com.android.settings.lgesetting.Config.Config;

import java.util.ArrayList;
import java.util.List;
import com.android.settings.R;
import com.lge.constants.SettingsConstants;

public class ISAISettings extends SettingsPreferenceFragment implements OnClickListener {
    private static final String TAG = "ISAISettings";
    static final boolean DEBUG = false;
    private static final int SettingMenuSize = 2;
    public static final String ISAI_ENABLED = "isai_enabled";
    public static final String ISAI_MODE_SELECT = "isai_mode_select";
    public static final String ISAI_SHORTCUT_PACKAGE = "isai_shortcut_package";
    public static final String ISAI_SHORTCUT_CLASS = "isai_shortcut_class";
    public static final String ISAI_SHORTCUT_APP_NAME = "isai_shortcut_app_name";
    public static final int RESULT_CODE = 101;
    public ListView listView;

    private static final int ISAI_MOTION = 0;
    private static final int APP_SHORTCUT = 1;

    private List<GestureInfo> gestureInfos;
    private Context mContext;

    private ISAISettingsInfoAdapter mAdapter;
    private Switch mSwitch;

    SettingsBreadCrumb mBreadCrumb;
    private PackageManager packageManager;

    public static class GestureInfo {
        CharSequence caption;
        //        Drawable icon;
        boolean isActive;
        public String classPackage;
        public String className;
        public String summery;
        int modeSet;
    }

    @Override
    public void onAttach(Activity activity) {
        logd("onAttach(%s)", activity.getClass().getSimpleName());
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle icicle) {
        logd("onCreate(%s)", icicle);
        super.onCreate(icicle);
        Activity activity = getActivity();
        mSwitch = new Switch(activity);
        mSwitch.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mSwitch SET : " + isChecked);
                Settings.System.putInt(getContentResolver(), ISAI_ENABLED,
                        isChecked == true ? 1 : 0);

            }
        });

        final int padding = activity.getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mSwitch.setPaddingRelative(0, 0, 0, 0);
        } else {
            mSwitch.setPaddingRelative(0, 0, padding, 0);
        }
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(mSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }

        //Actionbar
        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(mContext)) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } else {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        packageManager = getActivity().getPackageManager();

    }

    @Override
    public void onDestroyView() {
        if (SettingsBreadCrumb.isAttached(getActivity()) == false) {
            getActivity().getActionBar().setCustomView(null);
        }

        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        logd("onActivityCreated(%s)", savedInstanceState);
        super.onActivityCreated(savedInstanceState);

        listView = getListView();
        mAdapter = new ISAISettingsInfoAdapter(mContext);
        initItems();
        updateItems();
        updateSummary();
        listView.setAdapter(mAdapter);

    }

    @Override
    public void onPause() {
        logd("onPause()");
        super.onPause();
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            if (mBreadCrumb != null) {
                mBreadCrumb.removeSwitch();
            }
        }
        getContentResolver().unregisterContentObserver(mIsaISwitch);
    }

    private ContentObserver mIsaISwitch = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            upDateISAISwitch();
        }
    };

    private void upDateISAISwitch() {
        //ISAI Settings Switch
        if (Settings.System.getInt(getContentResolver(), ISAI_ENABLED, 0) == 1) {
            mSwitch.setChecked(true);
            mAdapter.notifyDataSetChanged();
        } else {
            mSwitch.setChecked(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        logd("onResume()");
        super.onResume();
        
        if (SettingsBreadCrumb.isAttached(getActivity())) {
            mBreadCrumb = new SettingsBreadCrumb(getActivity());
            if (mBreadCrumb != null) {
                mBreadCrumb.addSwitch(mSwitch);
            }
        }
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(ISAI_ENABLED), true, mIsaISwitch);
        upDateISAISwitch();
        updateItems();
        updateSummary();
    }

    private void initItems() {

        boolean mIsSwitch = Settings.System.getInt(getContentResolver(), ISAI_ENABLED, 0) > 0 ? true
                : false;
        mSwitch.setChecked(mIsSwitch);

        mAdapter.clear();
        gestureInfos = new ArrayList<GestureInfo>(SettingMenuSize);
        GestureInfo temp1 = new GestureInfo();

        temp1.caption = getString(R.string.sp_gesture_isai_motion);;
        temp1.isActive = true;
        temp1.classPackage = "com.android.settings";
        temp1.className = "com.android.settings.lge.ISAIMotionSettings";
        temp1.modeSet = ISAI_MOTION;
        temp1.summery = "";
        gestureInfos.add(temp1);
        mAdapter.addAll(gestureInfos);

        GestureInfo temp2 = new GestureInfo();
        temp2.caption = getString(R.string.sp_gesture_isai_app_shortcut);
        temp2.isActive = true;
        temp2.classPackage = "com.android.settings";
        temp2.className = "com.android.settings.lge.ISAIAppSettings";
        temp2.modeSet = APP_SHORTCUT;
        temp2.summery = "";
        gestureInfos.add(temp2);
        mAdapter.addAll(gestureInfos);

    }

    private void updateItems() {
        Log.d(TAG,
                "MODE SET : " + Settings.System.getInt(getContentResolver(), ISAI_MODE_SELECT, 0));

        int mode = Settings.System.getInt(getContentResolver(), ISAI_MODE_SELECT, 0);

        if (mode == ISAI_MOTION) {
            gestureInfos.get(ISAI_MOTION).isActive = true;
            gestureInfos.get(APP_SHORTCUT).isActive = false;

        } else if (mode == APP_SHORTCUT) {
            gestureInfos.get(ISAI_MOTION).isActive = false;
            gestureInfos.get(APP_SHORTCUT).isActive = true;
        }

    }

    private static void logd(String msg, Object... args) {
        if (DEBUG)
            Log.d(TAG, args == null || args.length == 0 ? msg : String.format(msg, args));
    }

    //Actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ISAISettingsInfoAdapter extends ArrayAdapter<GestureInfo> {
        private final LayoutInflater mInflater;

        public ISAISettingsInfoAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GestureInfo gestureInfo = getItem(position);
            logd("getView(%s)", gestureInfo.caption);
            if (position == 0) {
                View row = createSummaryRow(parent);
                ((TextView)row.findViewById(android.R.id.title))
                        .setText(R.string.sp_gesture_isai_settings_des);

                return row;
            }

            if (convertView != null) {
                ImageView icon = (ImageView)convertView.findViewById(android.R.id.icon);
                if (icon == null) {
                    convertView = null;
                }
            }

            final View row = convertView != null ? convertView : createGestureInfoRow(parent);
            row.setTag(gestureInfo);

            TextView mTitle = ((TextView)row.findViewById(android.R.id.title));
            mTitle.setText(gestureInfo.caption);
            
            TextView mSummary = ((TextView)row.findViewById(android.R.id.summary));
            mSummary.setText(gestureInfo.summery);
            if (gestureInfo.modeSet == ISAI_MOTION) {
                mSummary.setVisibility(View.GONE);
            }

            if (gestureInfo.modeSet == APP_SHORTCUT) {
                if (gestureInfo.summery.equals("")) {
                    mSummary.setVisibility(View.GONE);
                } else {
                    mSummary.setVisibility(View.VISIBLE);
                }
            }
            

            // bind radio button
            RadioButton radioButton = (RadioButton)row.findViewById(android.R.id.button1);
            radioButton.setChecked(gestureInfo.isActive);

            radioButton.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    row.onTouchEvent(event);
                    return false;
                }
            });

            ImageView settingsButton = (ImageView)row.findViewById(android.R.id.button2);
            settingsButton.setVisibility(View.VISIBLE);
            settingsButton.setAlpha(gestureInfo.isActive ? 1f : Utils.DISABLED_ALPHA);
            settingsButton.setEnabled(gestureInfo.isActive);
            settingsButton.setFocusable(gestureInfo.isActive);
            settingsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getCurrentSelection() != null) {
                        Log.d("jw ", "getCurrentSelection :" + getCurrentSelection().caption);
                        ComponentName c;
                        c = new ComponentName(getCurrentSelection().classPackage,
                                getCurrentSelection().className);
                        Intent i = new Intent(Intent.ACTION_MAIN);
                        i.setComponent(c);
                        startActivityForResult(i, RESULT_CODE);
                    }

                }
            });
            
            if (mSwitch.isChecked()) {
                radioButton.setEnabled(true);
                mTitle.setEnabled(true);
                mSummary.setEnabled(true);
            } else {
                radioButton.setEnabled(false);
                mTitle.setEnabled(false);
                mSummary.setEnabled(false);
                settingsButton.setEnabled(false);
            }
            return row;
        }

        private View createSummaryRow(ViewGroup parent) {
            final View row = mInflater.inflate(R.layout.description_preference_information, parent,
                    false);
            row.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.setPressed(true);
                    v.setSoundEffectsEnabled(false);
                    return true;
                }
            }
                    );
            return row;
        }

        private View createGestureInfoRow(ViewGroup parent) {
            final View row = mInflater.inflate(R.layout.gesture_info_row, parent, false);
            final View header = row.findViewById(android.R.id.widget_frame);
            header.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSwitch.isChecked()) {
                        return;
                    }
                    v.setPressed(true);
                    activate((GestureInfo)row.getTag());
                    Log.d("jw ", "createGestureInfoRow");
                    if (getCurrentSelection() == null) {
                        return;
                    }
                    if (getCurrentSelection().modeSet == ISAI_MOTION) {
                        Settings.System.putInt(getContentResolver(),
                                ISAI_MODE_SELECT, ISAI_MOTION);
                    } else {
                        Settings.System.putInt(getContentResolver(),
                                ISAI_MODE_SELECT, APP_SHORTCUT);
                    }
                    Log.d(TAG,
                            "Check DB SET : "
                                    + Settings.System.getInt(getContentResolver(),
                                            ISAI_MODE_SELECT, 0));
                }
            });
            return row;
        }

        private GestureInfo getCurrentSelection() {
            for (int i = 0; i < getCount(); i++) {
                GestureInfo Info = getItem(i);
                if (Info.isActive) {
                    return Info;
                }

            }
            return null;
        }

        private void activate(GestureInfo dreamInfo) {
            if (dreamInfo.equals(getCurrentSelection()))
                return;
            for (int i = 0; i < getCount(); i++) {
                getItem(i).isActive = false;
            }
            dreamInfo.isActive = true;
            notifyDataSetChanged();
        }

    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        Log.d("jw ", "onClick");

    }
    
    private void updateSummary() {
        try {
            gestureInfos.get(APP_SHORTCUT).summery = packageManager.getApplicationLabel(
                    packageManager.getPackageInfo(
                            Settings.System.getString(getContentResolver(),
                                    ISAI_SHORTCUT_PACKAGE),
                            0).applicationInfo).toString();
        } catch (NameNotFoundException e) {
            gestureInfos.get(APP_SHORTCUT).summery ="";
        }
        mAdapter.notifyDataSetChanged();

    }
    
}
