package com.android.settings.lge;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

//import com.android.settings.CustomImagePreference;
import com.android.settings.R;
import com.android.settings.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.Log;
import android.util.PrefixPrinter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lge.constants.SettingsConstants;
import com.android.settings.DoubleTitleListPreference;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.SettingsPreferenceFragment;

import android.app.ActionBar;
import android.view.MenuItem;
import android.view.ViewGroup;

public class FrontKeyLight extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String TAG = "FrontKeyLight";

    private static final String KEY_TURN_ON_FRONT_KEY_LIGHT = "checkbox_front_key_light";
    private static final String KEY_FRONT_KEY_LIGHT_DURATION = "front_key_light_duration";
    //private static final String KEY_FRONT_KEY_LIGHT_IMAGE = "front_key_light_image";
    private static final String FRONT_KEY_NOTIFICATION = "front_key_notification";
    private static final String FRONT_KEY_CATEGORY = "frontkey_category";

    private CheckBoxPreference mFrontKeyNotification;
    private CheckBoxPreference mTurnOnFrontKeyLight;
    private DoubleTitleListPreference mFrontKeyLightDuration;
    private ImageView mCustomImagePreference;
    int preLightDurationValue;

    private boolean isPortrait;

    private boolean isPortraitOri() {
        Configuration conf = this.getResources().getConfiguration();
        return (conf.orientation == Configuration.ORIENTATION_PORTRAIT)
                || (conf.orientation == Configuration.ORIENTATION_UNDEFINED);
    }

    private View updateUi(LayoutInflater inflater) {
        View view = null;

        isPortrait = isPortraitOri();
        if (isPortrait) {
            view = inflater.inflate(R.layout.custom_image_preference, null);
            mCustomImagePreference = (ImageView)view.findViewById(R.id.front_key_light_image);
            mCustomImagePreference.setScaleType(ScaleType.FIT_CENTER);
        } else {
            view = inflater.inflate(R.layout.custom_image_preference, null);
            mCustomImagePreference = (ImageView)view.findViewById(R.id.front_key_light_image);
            mCustomImagePreference.setScaleType(ScaleType.FIT_CENTER);
        }
        return view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return updateUi(inflater);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mTurnOnFrontKeyLight.isChecked()) {
            chooseSelectImage(true);
            updateFrontKeyLEDTimeoutSummary(mFrontKeyLightDuration.getValue());
        } else {
            chooseSelectImage(false);
            updateFrontKeyLEDTimeoutSummary(String.valueOf(preLightDurationValue));
        }
    }

    private boolean isContainfromValues(Object value)
    {
        if (mFrontKeyLightDuration == null)
        {
            return false;
        }

        CharSequence[] values = mFrontKeyLightDuration.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        ContentResolver resolver = getContentResolver();
        addPreferencesFromResource(R.xml.front_key_light);

        preLightDurationValue = Settings.System.getInt(resolver,
                SettingsConstants.System.FRONT_KEY_LIGHT_PREVALUE, 1500);

        mFrontKeyNotification = (CheckBoxPreference)findPreference(FRONT_KEY_NOTIFICATION);
        mTurnOnFrontKeyLight = (CheckBoxPreference)findPreference(KEY_TURN_ON_FRONT_KEY_LIGHT);
        mFrontKeyNotification.setChecked(Settings.System.getInt(
                resolver, SettingsConstants.System.FRONT_KEY_INDICATOR, 1) != 0);
        mTurnOnFrontKeyLight.setChecked(Settings.Secure.getInt(
                resolver, SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 1500) != 0);

        PreferenceCategory pc = (PreferenceCategory)findPreference(FRONT_KEY_CATEGORY);
        PreferenceScreen front_ps = (PreferenceScreen)findPreference("ps_front");

        front_ps.removePreference(mFrontKeyNotification);
        front_ps.removePreference(pc);

        mFrontKeyLightDuration = (DoubleTitleListPreference)findPreference(KEY_FRONT_KEY_LIGHT_DURATION);
        mFrontKeyLightDuration
                .setSubTitle(getString(R.string.display_home_touch_button_light_duration_summary));

        if (Utils.isFolderModel(getActivity())) {
            mFrontKeyLightDuration.setEntryValues(R.array.frontkey_led_timeout_values_12key);
            mFrontKeyLightDuration.setEntries(R.array.front_key_light_NORMAL_12key);
        } else {
            if (Config.ATT.equals(Config.getOperator()))
            {
                mFrontKeyLightDuration.setEntryValues(R.array.sp_frontkey_led_timeout_values_l1_NORMAL);
                mFrontKeyLightDuration.setEntries(R.array.sp_front_key_light_att_NORMAL);
            } else if (Config.VZW.equals(Config.getOperator())) {
                mFrontKeyLightDuration.setEntries(R.array.sp_front_key_light_NORMAL);
            }
        }

        // value check for upgrading
        int currentledtime = Settings.Secure.getInt(resolver,
                SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 1500);
        if (currentledtime == 3000 && !isContainfromValues(String.valueOf(currentledtime))) {
            // set 2700ms(default) to 3000ms for ICS
            Settings.Secure.putInt(getContentResolver(),
                    SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 2700);
        }
        else if (currentledtime != 0 && !isContainfromValues(String.valueOf(currentledtime))) {
            if (Config.ATT.equals(Config.getOperator())) {
                Settings.Secure.putInt(getContentResolver(),
                        SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 6000);
            } else {
                Settings.Secure.putInt(getContentResolver(),
                        SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 1500);
            }
        }

        mFrontKeyLightDuration.setValue(String.valueOf(Settings.Secure.getInt(
                resolver, SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 1500)));

        mFrontKeyLightDuration
                .setMainTitle(getString(R.string.display_home_touch_button_light_duration));

        mFrontKeyLightDuration.setOnPreferenceChangeListener(this);

        //iamjun.lee Actionbar
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Utils.isFolderModel(getActivity())) {
            mFrontKeyLightDuration.setTitle(R.string.keypad_light_duration);
            mFrontKeyLightDuration
                    .setMainTitle(getString(R.string.keypad_light_duration));
            mFrontKeyLightDuration
                    .setSubTitle(getString(R.string.keypad_light_duration_popup_summary));
            mTurnOnFrontKeyLight.setTitle(R.string.turn_on_keypad_light);
            mTurnOnFrontKeyLight.setSummary(null);
            front_ps.setTitle(R.string.keypad_light_duration);
            if (actionBar != null) {
                actionBar.setTitle(R.string.keypad_light_title);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        if (preference == mTurnOnFrontKeyLight)
        {
            if (!mTurnOnFrontKeyLight.isChecked())
            {
                preLightDurationValue = Integer.parseInt(mFrontKeyLightDuration.getValue());
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_LIGHT_PREVALUE, preLightDurationValue);

                Settings.Secure.putInt(getContentResolver(),
                        SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 0);

                updateFrontKeyLEDTimeoutSummary(Settings.System.getString(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_LIGHT_PREVALUE));

                chooseSelectImage(false);
            }
            else
            {
                preLightDurationValue = Settings.System.getInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_LIGHT_PREVALUE, 1500);

                Settings.Secure.putInt(getContentResolver(),
                        SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, preLightDurationValue);
                mFrontKeyLightDuration
                        .setValue(String.valueOf(Settings.Secure.getInt(
                                getContentResolver(),
                                SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, 1500)));

                updateFrontKeyLEDTimeoutSummary(mFrontKeyLightDuration.getValue());

                chooseSelectImage(true);
            }
        }

        if (preference == mFrontKeyNotification)
        {
            if (mFrontKeyNotification.isChecked())
            {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_INDICATOR, 1);
            }
            else
            {
                Settings.System.putInt(getContentResolver(),
                        SettingsConstants.System.FRONT_KEY_INDICATOR, 0);
            }

        }

        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        // TODO Auto-generated method stub
        final String key = arg0.getKey();

        if (KEY_FRONT_KEY_LIGHT_DURATION.equals(key) && mTurnOnFrontKeyLight.isChecked())
        {
            int value = Integer.parseInt((String)arg1);

            Settings.Secure.putInt(getContentResolver(),
                    SettingsConstants.Secure.FRONTKEY_LED_TIMEOUT, value);

            updateFrontKeyLEDTimeoutSummary(arg1);
        }

        return false;
    }

    private void updateFrontKeyLEDTimeoutSummary(Object value) {
        CharSequence[] summaries;

        if (Utils.isFolderModel(getActivity())) {
            summaries = getResources().getTextArray(R.array.front_key_light_NORMAL_12key);
        } else {
            if (Config.ATT.equals(Config.getOperator()))
            {
                summaries = getResources().getTextArray(R.array.sp_front_key_light_att_NORMAL);
            }
            else if (Config.VZW.equals(Config.getOperator()))
            {
                summaries = getResources().getTextArray(R.array.sp_front_key_light_NORMAL);
            }
            else
            {
                summaries = getResources().getTextArray(R.array.sp_front_key_light_NORMAL_ex);
            }
        }

        CharSequence[] values = mFrontKeyLightDuration.getEntryValues();

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                mFrontKeyLightDuration.setSummary(summaries[i]);
                mFrontKeyLightDuration.setValueIndex(i);
                break;
            }
        }
    }

    //iamjun.lee Actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseSelectImage(boolean isOn)
    {
        if (isOn == true) {
            if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {
                mCustomImagePreference.setImageResource(R.drawable.img_front_key_led_on_dome);
                Log.i(TAG, "Support model Emotional LED = " + isOn);
            } else if (Utils.isFolderModel(getActivity())) {
                mCustomImagePreference.setImageResource(R.drawable.img_keypad_led_on);
            } else {
                mCustomImagePreference.setImageResource(R.drawable.img_front_key_led_on);
                Log.i(TAG, "Not support model Emotional LED = " + isOn);
            }
        }
        else {
            if (Config.getFWConfigInteger(getActivity(), com.lge.R.integer.config_emotionalLedType, 
                "com.lge.R.integer.config_emotionalLedType") == 1) {
                mCustomImagePreference.setImageResource(R.drawable.img_front_key_led_off_dome);
                Log.i(TAG, "Support model Emotional LED = " + isOn);
            } else if (Utils.isFolderModel(getActivity())) {
                mCustomImagePreference.setImageResource(R.drawable.img_keypad_led_off);
            } else
            {
                mCustomImagePreference.setImageResource(R.drawable.img_front_key_led_off);
                Log.i(TAG, "Not support model Emotional LED = " + isOn);
            }
        }
    }

    private boolean isNotUpgradeGModel() {
        if (Utils.is_G_model_Device(Build.DEVICE)
                &&
                ("SHB".equals(Config.getOperator())
                        || "VDF".equals(Config.getOperator())
                        ||
                        "SCA".equals(android.os.SystemProperties.get("ro.build.target_region"))
                        ||
                        //qingtai.wang@lge.com 20130104 changed for CUCC CN [S]
                        "CUCC".equals(Config.getOperator())
                        ||
                        //qingtai.wang@lge.com 20130104 changed for CUCC CN [E]
                        "STL".equals(Config.getOperator()) || "MON".equals(Config.getOperator()) ||
                        "ESA".equals(Config.getCountry()) || "AME".equals(Config.getCountry()) ||
                        Build.PRODUCT.equals("geehrc_byt_fr")
                        || Build.PRODUCT.equals("geehrc_free_fr") ||
                        Build.PRODUCT.equals("geehrc_h3g_com")
                        || Build.PRODUCT.equals("geehrc_ncm_nw") ||
                        Build.PRODUCT.equals("geehrc_nrj_fr")
                        || Build.PRODUCT.equals("geehrc_open_cis") ||
                        Build.PRODUCT.equals("geehrc_open_eu")
                        || Build.PRODUCT.equals("geehrc_org_com") ||
                        Build.PRODUCT.equals("geehrc_pls_pl")
                        || Build.PRODUCT.equals("geehrc_tel_au") ||
                        Build.PRODUCT.equals("geehrc_tim_it")
                        || Build.PRODUCT.equals("geehrc_tlf_eu") ||
                        Build.PRODUCT.equals("geehrc_tln_nw")
                        || Build.PRODUCT.equals("geehrc_tmn_pt") ||
                Build.PRODUCT.equals("geehrc_tmo_com"))) {
            return true;
        }
        else {
            return false;
        }
    }
}
