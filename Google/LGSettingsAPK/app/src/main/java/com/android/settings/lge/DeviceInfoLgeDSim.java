/*
 * Copyright (C) 2011 The Android Open Source Project
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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.android.settings.Utils.prepareCustomPreferencesList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserManager;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
//import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceGroup;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
//import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.RenameEditTextPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utf8ByteLengthFilter;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.utils.MSIMUtils;
import com.lge.constants.SettingsConstants;

public class DeviceInfoLgeDSim extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener {
    private static final String LOG_TAG = "aboutphone # DeviceInfoLgeDSim";

    private static final String TAB_SIM1 = "sim1";
    private static final String TAB_SIM2 = "sim2";
    private static final String TAB_COMMON = "common";

    private static final String KEY_NETWORK = "network";
    private static final String KEY_PHONEIDENTITY = "phoneidentity";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_HARDWARE_INFORMATION = "hardwareinformation";
    private static final String KEY_SOFTWARE_INFORMATION = "softwareinformation";
    private static final String KEY_LEGAL_INFORMAtION = "legalinformation";
    private static final String KEY_SUTEL = "sutel";

    private Preference mAboutPhonePreference;

    private TabHost mTabHost;
    private TabWidget mTabWidget;
    private ListView mListView;
    private ImageView img_view;
    private String mCurrentTab = null;
    private TextView mTextView_sim1;
    private TextView mTextView_sim2;
    private TextView mTextView_sim3;
    int insertedSimvalue = 0;

    public static final int IN_SIM_NOT = 0;
    public static final int IN_SIM_1 = 1;
    public static final int IN_SIM_2 = 2;
    public static final int IN_SIM_1_2 = 3;

    public static String sim1_state = "";
    public static String sim2_state = "";

    private static final String KEY_SYSTEM_UPDATE_CENTER = "update_center";

    private EditTextPreference mDeviceNamePref;
    private RenameEditTextPreference mRenameDeviceNamePref;
    private static final int PHONE_NAME_MAXLEN = 32;
    private static final String KEY_RENAME_DEVICE = "my_phone_name";
    private static final String DEFAULT_DEVICE_NAME = "Optimus";
    private Preference mLegalInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public String read_db_device_name() {
        String device_name = null;
        if (UserManager.supportsMultipleUsers()) {
            device_name = Settings.Global.getString(getContentResolver(), "lg_device_name");
        } else {
            device_name = Settings.System.getString(getContentResolver(), "lg_device_name");
        }

        if (device_name == null) {
            Log.d(LOG_TAG, "read_db_device_name() : device_name : (on DB) , default: "
                    + device_name);
            device_name = DEFAULT_DEVICE_NAME;
        } else {
            Log.d(LOG_TAG, "read_db_device_name() : device_name : (on DB) "
                    + device_name);
        }
        return device_name;
    }

    public boolean onPreferenceClick(Preference pref) {
        //Log.d(LOG_TAG, "onPreferenceClick");
        if (mDeviceNamePref != null) {
            mDeviceNamePref.setText(read_db_device_name());
            mDeviceNamePref.getEditText().selectAll();
//            mDeviceNamePref.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = inflater.getContext();
        View view;
        if (Utils.isUI_4_1_model(getActivity())) {
            if ("CTC".equals(Config.getOperator()) || "CTO".equals(Config.getOperator())) {
                view = inflater.inflate(R.layout.device_info_msim_ctc, container, false);
            } else {
                view = inflater.inflate(R.layout.device_info_msim, container, false);
            }
        } else {
            view = inflater.inflate(R.layout.device_info_dsim, container, false);
        }
        //final View view = inflater.inflate(R.layout.device_info_dsim, container, false);
        img_view = (ImageView)view.findViewById(R.id.ic_simcard);
        img_view.setImageResource(R.drawable.ic_simcard);

        mTabHost = (TabHost)view.findViewById(android.R.id.tabhost);
        mTabWidget = (TabWidget)view.findViewById(android.R.id.tabs);
        mListView = (ListView)view.findViewById(android.R.id.list);
        mTextView_sim1 = (TextView)view.findViewById(R.id.empt_sim1);
        mTextView_sim2 = (TextView)view.findViewById(R.id.empt_sim2);
        mTextView_sim3 = (TextView)view.findViewById(R.id.empt_sim3);
        mTextView_sim1.setTextColor(Color.BLACK);
        mTextView_sim2.setTextColor(Color.BLACK);
        mTextView_sim3.setTextColor(Color.BLACK);

        prepareCustomPreferencesList(container, view, mListView, true);
        // [S][2012.11.01][yongjaeo.lee@lge.com] left,right margin 24 fix ; blocking
        //((PreferenceFrameLayout.LayoutParams) view.getLayoutParams()).removeBorders = true;
        // [E][2012.11.01][yongjaeo.lee@lge.com] left,right margin 24 fix

        mTabHost.setup();
        mTabHost.setOnTabChangedListener(mTabListener);

        mListView.setItemsCanFocus(true);
        mListView.setVisibility(View.VISIBLE);
        img_view.setVisibility(View.GONE);
        mTextView_sim3.setVisibility(View.GONE);

        mTabHost.clearAllTabs();
        mTabHost.addTab(buildTabSpec(context, TAB_SIM1, OverlayUtils.getMSimTabName(context,
                SettingsConstants.System.SIM1_NAME)));
        mTabHost.addTab(buildTabSpec(context, TAB_SIM2, OverlayUtils.getMSimTabName(context,
                SettingsConstants.System.SIM2_NAME)));
        mTabHost.addTab(buildTabSpec(TAB_COMMON, R.string.sp_sim_common_NORMAL));

        mTabWidget.setVisibility(View.VISIBLE);

        mCurrentTab = mTabHost.getCurrentTabTag();

        Log.i(LOG_TAG, "oncreateView () , addTab called, mCurrentTab:" + mCurrentTab);
        //        insertedSimvalue = OverlayUtils.check_SIM_inserted(getActivity().getApplicationContext());

        sim1_state = OverlayUtils.get_SIM_state(getActivity().getApplicationContext(), 0);
        sim2_state = OverlayUtils.get_SIM_state(getActivity().getApplicationContext(), 1);

        return view;
    }

    @Override
    public void onPause() {
        //Log.i(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        //Log.i(LOG_TAG, "onResume");
        super.onResume();
        //        updateTabs();
    }

    @Override
    public void onDestroy() {
        //Log.i(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    private void updateTabs() {
        Log.i(LOG_TAG, "updateTabs");

        updateBody();
    }

    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        public View createTabContent(String tag) {
            return new View(mTabHost.getContext());
        }
    };

    private TabSpec buildTabSpec(String tag, int titleRes) {
        return mTabHost.newTabSpec(tag).setIndicator(getText(titleRes))
                .setContent(mEmptyTabContent);
    }

    private TabSpec buildTabSpec(String tag, String titleRes) {
        return mTabHost.newTabSpec(tag).setIndicator(titleRes).setContent(
                mEmptyTabContent);
    }

    private TabSpec buildTabSpec(Context context, String tag, String titleRes) {
        final int sRENAME_SIM1_INDEX = 0;
        final int sRENAME_SIM2_INDEX = 1;
        final int sRENAME_SIM3_INDEX = 2;

        if ("Slot 1".equals(titleRes)) {
            titleRes = Utils.setSimName(context, sRENAME_SIM1_INDEX, titleRes);
        } else if ("Slot 2".equals(titleRes)) {
            titleRes = Utils.setSimName(context, sRENAME_SIM2_INDEX, titleRes);
        } else if ("Slot 3".equals(titleRes)) {
            titleRes = Utils.setSimName(context, sRENAME_SIM3_INDEX, titleRes);
        }
        return mTabHost.newTabSpec(tag).setIndicator(titleRes).setContent(
                mEmptyTabContent);
    }

    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        public void onTabChanged(String tabId) {
            updateBody();
        }
    };

    private void updateBody() {
        Log.i(LOG_TAG, "updateBody");

        final String new_currentTab = mTabHost.getCurrentTabTag();

        if (new_currentTab != null) //[2012.09.27][munjohn.kang] WBT
        {
            if (!new_currentTab.equals(mCurrentTab)) {
                mCurrentTab = new_currentTab;
                MSIMUtils.write_SharedPreference("tab", new_currentTab, getActivity()
                        .getApplicationContext()); // [2012.09.22][yongjaeo.lee@lge.com][Dual_SIM] dual sim phoneNumber
            }
        }
        updateAboutPhoneTabSubmenu();
    }

    private void updateAboutPhoneTabSubmenu() {
        final Activity act = getActivity();

        //Log.i(LOG_TAG, "updateAboutPhoneTabSubmenu");
        int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        PreferenceGroup parentPreference = getPreferenceScreen();

        removePreferenceFromScreen(KEY_NETWORK);
        removePreferenceFromScreen(KEY_PHONEIDENTITY);

        removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
        removePreferenceFromScreen(KEY_BATTERY);
        removePreferenceFromScreen(KEY_HARDWARE_INFORMATION);
        removePreferenceFromScreen(KEY_SOFTWARE_INFORMATION);
        removePreferenceFromScreen(KEY_LEGAL_INFORMAtION);
        removePreferenceFromScreen(KEY_SYSTEM_UPDATE_CENTER);
        removePreferenceFromScreen(KEY_RENAME_DEVICE);
        removePreferenceFromScreen("epush");

        // default string
        /*
        mTextView_sim1.setText(getString(R.string.sp_no_sim));
        mTextView_sim2.setText(getString(R.string.sp_no_sim));
        mTextView_sim3.setText(getString(R.string.sp_no_sim));
        */

        if (mCurrentTab.equals(TAB_SIM1) || mCurrentTab.equals(TAB_SIM2)) {
            //            Log.i(LOG_TAG, "insertedSimvalue:" + insertedSimvalue);

            if (mCurrentTab.equals(TAB_SIM1)) {
                mTextView_sim2.setVisibility(View.GONE);
                //                if ( insertedSimvalue == IN_SIM_1 || insertedSimvalue == IN_SIM_1_2 ){ 
                if (OverlayUtils.check_SIM_inserted(0)) {
                    if (sim1_state.equals("pin_lock") || sim1_state.equals("puk_lock")
                            || sim1_state.equals("perm_lock")) { // sim lock check
                        mListView.setVisibility(View.GONE);
                        img_view.setVisibility(View.VISIBLE);

                        if ("CTC".equals(Config.getOperator())
                                || "CTO".equals(Config.getOperator())) {
                            if (sim1_state.equals("pin_lock")) {
                                mTextView_sim1.setText(getString(R.string.sp_uimpinlocked_new_NORMAL));
                            } else if (sim1_state.equals("puk_lock")) {
                                mTextView_sim1.setText(getString(R.string.sp_uimpuklocked_new_NORMAL));
                            } else {
                                mTextView_sim1.setText(getString(R.string.sp_uimpermlocked_new_NORMAL));
                            }
                        } else {
                            if (sim1_state.equals("pin_lock")) {
                                mTextView_sim1.setText(getString(R.string.sp_sim1pinlocked_new_NORMAL));
                            } else if (sim1_state.equals("puk_lock")) {
                                mTextView_sim1.setText(getString(R.string.sp_sim1puklocked_new_NORMAL));
                            } else {
                                mTextView_sim1.setText(getString(R.string.sp_sim1permlocked_new_NORMAL));
                            }
                        }
                        mTextView_sim1.setVisibility(View.VISIBLE);
                    } else {
                        mListView.setVisibility(View.VISIBLE);
                        mTextView_sim1.setVisibility(View.GONE);
                        img_view.setVisibility(View.GONE);
                    }

                    addPreferencesFromResource(R.xml.device_info_lge_dsim_tab12);
                } else {
                    mListView.setVisibility(View.GONE);
                    img_view.setVisibility(View.VISIBLE);
                    mTextView_sim1.setVisibility(View.VISIBLE);
                }
            } else {
                mTextView_sim1.setVisibility(View.GONE);
                //                if ( insertedSimvalue == IN_SIM_2 || insertedSimvalue == IN_SIM_1_2 ) { 
                if (OverlayUtils.check_SIM_inserted(1)) {
                    if (sim2_state.equals("pin_lock") || sim2_state.equals("puk_lock")
                            || sim2_state.equals("perm_lock")) { // sim lock check
                        mListView.setVisibility(View.GONE);
                        img_view.setVisibility(View.VISIBLE);

                        if ("CTC".equals(Config.getOperator())
                                || "CTO".equals(Config.getOperator())) {
                            if (sim2_state.equals("pin_lock")) {
                                mTextView_sim2.setText(getString(R.string.sp_simpinlocked_new_NORMAL));
                            } else if (sim2_state.equals("puk_lock")) {
                                mTextView_sim2.setText(getString(R.string.sp_simpuklocked_new_NORMAL));
                            } else {
                                mTextView_sim2.setText(getString(R.string.sp_simpermlocked_new_NORMAL));
                            }
                        } else {
                            if (sim2_state.equals("pin_lock")) {
                                mTextView_sim2.setText(getString(R.string.sp_sim2pinlocked_new_NORMAL));
                            } else if (sim2_state.equals("puk_lock")) {
                                mTextView_sim2.setText(getString(R.string.sp_sim2puklocked_new_NORMAL));
                            } else {
                                mTextView_sim2.setText(getString(R.string.sp_sim2permlocked_new_NORMAL));
                            }
                        }
                        mTextView_sim2.setVisibility(View.VISIBLE);
                    } else {
                        mListView.setVisibility(View.VISIBLE);
                        mTextView_sim2.setVisibility(View.GONE);
                        img_view.setVisibility(View.GONE);
                    }

                    addPreferencesFromResource(R.xml.device_info_lge_dsim_tab12);
                } else {
                    mListView.setVisibility(View.GONE);
                    img_view.setVisibility(View.VISIBLE);
                    mTextView_sim2.setVisibility(View.VISIBLE);
                }
            }
        } else if (mCurrentTab.equals(TAB_COMMON)) {
            //            if (insertedSimvalue == IN_SIM_NOT) {
            if (!OverlayUtils.check_SIM_inserted(0) && !OverlayUtils.check_SIM_inserted(1)) {
                addPreferencesFromResource(R.xml.device_info_lge_dsim_tab_all);
            } else {
                addPreferencesFromResource(R.xml.device_info_lge_dsim_tab3);
            }

            boolean mUpdateCenter = Utils.IsVersionCheck(getActivity(), "com.lge.updatecenter");
            mDeviceNamePref = (EditTextPreference)findPreference(KEY_RENAME_DEVICE);
            Log.d(LOG_TAG, "mUpdateCenter : " + mUpdateCenter);
            if (mUpdateCenter) {
                removePreferenceFromScreen(KEY_SYSTEM_UPDATE_SETTINGS);
            } else {
                removePreferenceFromScreen("update_center");
            }

            mLegalInfo = findPreference("legalinformation");
            if (mLegalInfo != null) {
                if (Config.CMCC.equals(Config.getOperator()) || "CMO".equals(Config.getOperator())
                        || "CN".equals(Config.getCountry())) {
                    Log.d("Deviceinfolge", "Config.getOperator() IN" + Config.getOperator());
                    mLegalInfo.setSummary(getString(R.string.settings_license_activity_title));
                } else {
                    mLegalInfo.setSummary(getString(R.string.sp_LegalInfoSummary_eula_NORMAL));
                }
            }
            if (!Utils.checkPackage(getActivity(), "com.ctc.epush")
                    || !("CTC".equals(Config.getOperator())
                            || "CTO".equals(Config.getOperator()))) {
                removePreferenceFromScreen("epush");
            }
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                    KEY_SYSTEM_UPDATE_SETTINGS,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

            mDeviceNamePref.setPositiveButtonText(getString(R.string.def_save_btn_caption));
            mDeviceNamePref.setOnPreferenceClickListener(this);

            mDeviceNamePref.getEditText().setFilters(new InputFilter[] {
                    new Utf8ByteLengthFilter(PHONE_NAME_MAXLEN,
                            getActivity(), mDeviceNamePref.getEditText())
            });

            mRenameDeviceNamePref = new RenameEditTextPreference(getActivity(), null);
            mRenameDeviceNamePref.setmDeviceNamePref(mDeviceNamePref);
            mDeviceNamePref.setSummary(read_db_device_name());
            mDeviceNamePref.setText(read_db_device_name());
            mDeviceNamePref.getEditText().addTextChangedListener(mRenameDeviceNamePref);

            mTextView_sim1.setVisibility(View.GONE);
            mTextView_sim2.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            img_view.setVisibility(View.GONE);
        }

        mAboutPhonePreference = findPreference(KEY_PHONEIDENTITY);
        if (mAboutPhonePreference != null) {
            if (TelephonyManager.PHONE_TYPE_CDMA != activePhoneType) {
                mAboutPhonePreference.setSummary(getString(R.string.sp_PhoneSummary_NORMAL));
            } else {
                mAboutPhonePreference.setSummary(getString(R.string.sp_PhoneSummary_MEID_NORMAL));
            }
        }

        //Sutel information for Costa Rica versions
        Preference sutelPreference = (Preference)findPreference(KEY_SUTEL);
        if (sutelPreference != null && getPreferenceScreen() != null) {
            if (SystemProperties.getBoolean("persist.sys.cust.sutel_menu", false)) {
                if (!Utils.isCostaRicaMCC(getActivity().getApplicationContext())) {
                    getPreferenceScreen().removePreference(sutelPreference);
                }
            } else {
                getPreferenceScreen().removePreference(sutelPreference);
            }
        }
        //Sutel information for Costa Rica versions

    }

    private static View inflatePreference(LayoutInflater inflater, ViewGroup root, View widget) {
        final View view = inflater.inflate(R.layout.preference, root, false);
        final LinearLayout widgetFrame = (LinearLayout)view.findViewById(android.R.id.widget_frame);
        widgetFrame.addView(widget, new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        return view;
    }

    private static View inflateAppTitle(LayoutInflater inflater, ViewGroup root, CharSequence label) {
        final TextView view = (TextView)inflater.inflate(R.layout.device_info_title, root, false);
        view.setText(label);

        return view;
    }

    private static void insetListViewDrawables(ListView view, int insetSide) {
        final Drawable stub = new ColorDrawable(Color.TRANSPARENT);
        view.setSelector(stub);
        view.setDivider(stub);
    }

    private static void setPreferenceTitle(View parent, int resId) {
        final TextView title = (TextView)parent.findViewById(android.R.id.title);
        title.setText(resId);
    }

    private void removePreferenceIfPropertyMissing(PreferenceGroup preferenceGroup,
            String preference, String property) {
        if (SystemProperties.get(property).equals("")) {
            // Property is missing so remove preference from group
            try {
                if (preferenceGroup != null) {
                    preferenceGroup.removePreference(findPreference(preference));
                }
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Property '" + property + "' missing and no '" + preference
                        + "' preference");
            }
        }
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);

        //Log.e(LOG_TAG, "removePreferenceFromScreen  !!!, key:"+key);
        if (pref != null && getPreferenceScreen() != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    private void addPreferenceFromScreen(String key) {
        Preference pref = findPreference(key);

        //Log.e(LOG_TAG, "addPreferenceFromScreen  !!!, key:"+key);

        if (pref == null) {
            Log.i(LOG_TAG, "if pref == null that reload Preferences");
            if (mCurrentTab.equals(TAB_SIM1) || mCurrentTab.equals(TAB_SIM2)) {
                addPreferencesFromResource(R.xml.device_info_lge_dsim_tab12);
            } else {
                addPreferencesFromResource(R.xml.device_info_lge_dsim_tab3);
            }

            if (getPreferenceScreen() != null) {
                //Log.e(LOG_TAG, "addPreferenceFromScreen  !!!1111 add~~~~~, key:"+key);
                getPreferenceScreen().addPreference(findPreference(key));
            }
        } else if (getPreferenceScreen() != null) {
            //Log.e(LOG_TAG, "addPreferenceFromScreen  !!!222 add~~~~~, key:"+key);
            getPreferenceScreen().addPreference(pref);
        }
    }
}
