/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings.inputmethod;

import com.android.settings.R;
import com.android.settings.Settings.KeyboardLayoutPickerActivity;
import com.android.settings.Settings.SpellCheckersSettingsActivity;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.tts.TtsEnginePreference;
import android.speech.tts.TtsEngines;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.UserDictionarySettings;
import com.android.settings.Utils;
import com.android.settings.VoiceInputOutputSettings;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import com.android.settings.lgesetting.Config.Config;

//[S][2012.02.09][jm.lee2@lge.com][Common] Added method for spell checker count
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.TextServicesManager;
//[E][2012.02.09][jm.lee2@lge.com][Common] Added method for spell checker count

// goodluck@lge.com 121107 merged from ICS [START]
import android.telephony.TelephonyManager;
// goodluck@lge.com 121107 merged from ICS [END]

import android.os.Build;

import android.util.Log;
import android.content.pm.PackageInfo;
import android.os.SystemProperties;

public class InputMethodAndLanguageSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, InputManager.InputDeviceListener,
        KeyboardLayoutDialogFragment.OnSetupKeyboardLayoutsListener, Indexable {

    private static final String KEY_PHONE_LANGUAGE = "phone_language";
    private static final String KEY_CURRENT_INPUT_METHOD = "current_input_method";
    private static final String KEY_INPUT_METHOD_SELECTOR = "input_method_selector";
    private static final String KEY_USER_DICTIONARY_SETTINGS = "key_user_dictionary_settings";
//    private static final String KEY_TEXT_LINK_SETTINGS = "key_text_link_settings";
    // false: on ICS or later
    private static final boolean SHOW_INPUT_METHOD_SWITCHER_SETTINGS = false;

    /*
        private static final String[] HARDKEYBOAD_KEYS = {
            "auto_replace", "auto_caps", "auto_punctuate",
        };
    */
    private int mDefaultInputMethodSelectorVisibility = 0;
    private ListPreference mShowInputMethodSelectorPref;
    private PreferenceCategory mKeyboardSettingsCategory;
//    private PreferenceCategory mHardKeyboardCategory;
    private PreferenceCategory mGameControllerCategory;
    private Preference mLanguagePref;
    // [Start][IMEED-158] 20130820 makutum.halyal crash when press back key from
    // language list
    private Preference mUserDictionaryPref;
    // [End][IMEED-158] 20130820 makutum.halyal crash when press back key from
    // language list
    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList =
            new ArrayList<InputMethodPreference>();
    private final ArrayList<PreferenceScreen> mHardKeyboardPreferenceList =
            new ArrayList<PreferenceScreen>();
    private InputManager mIm;
    private InputMethodManager mImm;
    private List<InputMethodInfo> mImis;
    private boolean mIsOnlyImeSettings;
    private boolean mIsAllowedByOrganization;
    private Handler mHandler;
    @SuppressWarnings("unused")
    private SettingsObserver mSettingsObserver;
    private Intent mIntentWaitingForResult;
    private InputMethodSettingValuesWrapper mInputMethodSettingValues;
    private DevicePolicyManager mDpm;

    private SpellCheckersPreference mSpellCheckersPreference;
    private Preference mCurrentPreference;
    private VoiceInputOutputSettings mVoiceInputOutputSettings;
    private Preference mPointerSpeedPreference;
    private CheckBoxPreference mVibrateInputDevices;

    public final static List<SearchIndexableRaw> mResult = new ArrayList<SearchIndexableRaw>();
    private String mSearch_result;
    private boolean mIsFirst = false;

    private final OnPreferenceChangeListener mOnImePreferenceChangedListener =
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference arg0, Object arg1) {
                    InputMethodSettingValuesWrapper.getInstance(
                            arg0.getContext()).refreshAllInputMethodAndSubtypes();
                    ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                    updateInputMethodPreferenceViews();
                    return true;
                }
            };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.language_settings);

        try {
            mDefaultInputMethodSelectorVisibility = Integer.valueOf(
                    getString(R.string.input_method_selector_visibility_default_value));
        } catch (NumberFormatException e) {
            Log.w("[InputMethodAndLanguageSettings]", "NumberFormatException");
        }

        if (getActivity().getAssets().getLocales().length == 1) {
            // No "Select language" pref if there's only one system locale available.
            getPreferenceScreen().removePreference(findPreference(KEY_PHONE_LANGUAGE));
        } else {
            mLanguagePref = findPreference(KEY_PHONE_LANGUAGE);
        }
        if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
            mShowInputMethodSelectorPref = (ListPreference)findPreference(
                    KEY_INPUT_METHOD_SELECTOR);
            mShowInputMethodSelectorPref.setOnPreferenceChangeListener(this);
            // TODO: Update current input method name on summary
            updateInputMethodSelectorSummary(loadInputMethodSelectorVisibility());
        }

        mVoiceInputOutputSettings = new VoiceInputOutputSettings(this);
        mVoiceInputOutputSettings.onCreate();

        // Get references to dynamically constructed categories.
//        mHardKeyboardCategory = (PreferenceCategory)findPreference("hard_keyboard");
        mKeyboardSettingsCategory = (PreferenceCategory)findPreference(
                "keyboard_settings_category");
        mGameControllerCategory = (PreferenceCategory)findPreference(
                "game_controller_settings_category");

        // Filter out irrelevant features if invoked from IME settings button.
        mIsOnlyImeSettings = Settings.ACTION_INPUT_METHOD_SETTINGS.equals(
                getActivity().getIntent().getAction());
        getActivity().getIntent().setAction(null);
        if (mIsOnlyImeSettings) {
            getPreferenceScreen().removeAll();
            //getPreferenceScreen().addPreference(mHardKeyboardCategory);
            if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
                getPreferenceScreen().addPreference(mShowInputMethodSelectorPref);
            }
            getPreferenceScreen().addPreference(mKeyboardSettingsCategory);
        }

        // Build IME preference category.
        mImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mImis = mImm.getInputMethodList();

        mInputMethodSettingValues =
                InputMethodSettingValuesWrapper.getInstance(getActivity());
        //LGE_JP_FWK 20130228 minho.choo@lge.com [A]featuring for hiding HandwritingIME for JP[START]
        String targetCountry = Config.getCountry();
        if (targetCountry.equalsIgnoreCase("JP")
                && (Build.MODEL.equals("L-04E") || Build.MODEL.equals("L-05E"))) {
            for (int i = 0; i < mImis.size(); i++) {
                if (mImis.get(i).getPackageName()
                        .equals("com.sevenknowledge.sevennotestabproduct.lg001.tab50")) {
                    mImis.remove(i);
                    break;
                }
            }
        }
        //LGE_JP_FWK [END]
        mKeyboardSettingsCategory.removeAll();
        if (!mIsOnlyImeSettings) {
            final PreferenceScreen currentIme = new PreferenceScreen(getActivity(), null);
            currentIme.setKey(KEY_CURRENT_INPUT_METHOD);
            currentIme.setTitle(getResources().getString(R.string.current_input_method));
            mKeyboardSettingsCategory.addPreference(currentIme);
        }

        synchronized (mInputMethodPreferenceList) {
            mInputMethodPreferenceList.clear();
            final List<InputMethodInfo> imis = mInputMethodSettingValues.getInputMethodList();
            final int N = (imis == null ? 0 : imis.size());
            for (int i = 0; i < N; ++i) {
                final InputMethodInfo imi = imis.get(i);
                final InputMethodPreference pref = getInputMethodPreference(imi);
                pref.setOnImePreferenceChangeListener(mOnImePreferenceChangedListener);
                mInputMethodPreferenceList.add(pref);
            }

            if (!mInputMethodPreferenceList.isEmpty()) {
                Collections.sort(mInputMethodPreferenceList);
                for (int i = 0; i < N; ++i) {
                    mKeyboardSettingsCategory.addPreference(mInputMethodPreferenceList.get(i));
                }
            }
        }

        // Build hard keyboard and game controller preference categories.
        mIm = (InputManager)getActivity().getSystemService(Context.INPUT_SERVICE);
        updateInputDevices();

        // Spell Checker
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(getActivity(), SpellCheckersSettingsActivity.class);
        final SpellCheckersPreference scp = ((SpellCheckersPreference)findPreference(
                "spellcheckers_settings"));
        mSpellCheckersPreference = scp;
        if (scp != null) {
            scp.setFragmentIntent(this, intent);

            //[S][2012.02.09][jm.lee2@lge.com][Common] Added method for spell checker count
            if (getSpellCheckerCount() == 0) {
                getPreferenceScreen().removePreference(scp);
            }
            //[E][2012.02.09][jm.lee2@lge.com][Common] Added method for spell checker count
        }

        mHandler = new Handler();
        mSettingsObserver = new SettingsObserver(mHandler, getActivity());
        mDpm = (DevicePolicyManager)(getActivity().
                getSystemService(Context.DEVICE_POLICY_SERVICE));
        // [Start][IMEED-158] 20130820 makutum.halyal crash when press back key
        // from language list
        mUserDictionaryPref = findPreference(KEY_USER_DICTIONARY_SETTINGS);
        // [End][IMEED-158] 20130820 makutum.halyal crash when press back key
        // from language list
        mPointerSpeedPreference = findPreference("pointer_speed");
        mIsFirst = true;
    }

    private void updateInputMethodSelectorSummary(int value) {
        String[] inputMethodSelectorTitles = getResources().getStringArray(
                R.array.input_method_selector_titles);
        if (inputMethodSelectorTitles.length > value) {
            mShowInputMethodSelectorPref.setSummary(inputMethodSelectorTitles[value]);
            mShowInputMethodSelectorPref.setValue(String.valueOf(value));
        }
    }

    //[S][2012.02.09][jm.lee2@lge.com][Common] Added method for spell checker count
    private int getSpellCheckerCount() {
        TextServicesManager tsm = (TextServicesManager)getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
        if (tsm != null) {
            final SpellCheckerInfo[] enabledScis = tsm.getEnabledSpellCheckers();

            if (enabledScis != null) {
                Log.d("[InputMethodAndLanguageSettings]",
                        "[getSpellCheckerCount] enabledScis.length = ");
                return enabledScis.length;
            }
        }

        return 0;
    }

    //[E][2012.02.09][jm.lee2@lge.com][Common] Added method for spell checker count

    // [Start][IMEED-191] 20130820 makutum.halyal
    private void updateUserDictionaryPreference(Preference userDictionaryPreference) {
        final Activity activity = getActivity();
        final TreeSet<String> localeSet = UserDictionaryList.getUserDictionaryLocalesSet(activity);
        if (null == localeSet) {
            // The locale list is null if and only if the user dictionary service is
            // not present or disabled. In this case we need to remove the preference.
            getPreferenceScreen().removePreference(userDictionaryPreference);
        } else {
            if (localeSet.size() <= 1) {
                userDictionaryPreference.setTitle(R.string
                        .user_dict_single_settings_title);
            } else {
                userDictionaryPreference.setTitle(R.string
                        .user_dict_multiple_settings_title);
            }
            userDictionaryPreference.setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference arg0) {
                            // Redirect to UserDictionarySettings if the user needs only one
                            // language.
                            final Bundle extras = new Bundle();
                            final Class<? extends Fragment> targetFragment;
                            final int titleRes;
                            if (localeSet.size() <= 1) {
                                if (!localeSet.isEmpty()) {
                                    // If the size of localeList is 0, we don't set the locale
                                    // parameter in the extras. This will be interpreted by the
                                    // UserDictionarySettings class as meaning
                                    // "the current locale". Note that with the current code for
                                    // UserDictionaryList#getUserDictionaryLocalesSet()
                                    // the locale list always has at least one element, since it
                                    // always includes the current locale explicitly.
                                    // @see UserDictionaryList.getUserDictionaryLocalesSet().
                                    extras.putString("locale", localeSet.first());
                                }
                                targetFragment = UserDictionarySettings.class;
                                titleRes = R.string.user_dict_single_settings_title;
                            } else {
                                targetFragment = UserDictionaryList.class;
                                titleRes = R.string.user_dict_multiple_settings_title;
                            }
                            startFragment(InputMethodAndLanguageSettings.this,
                                    targetFragment.getCanonicalName(), -1, extras, titleRes);
                            return true;
                        }
                    });
        }
    }

    // [End][IMEED-191] 20130820 makutum.halyal

    @Override
    public void onResume() {
        super.onResume();

        mSettingsObserver.resume();
        mIm.registerInputDeviceListener(this, null);

        if (!mIsOnlyImeSettings) {
            if (mLanguagePref != null) {
                Configuration conf = getResources().getConfiguration();
                String language = conf.locale.getLanguage();
                String localeString;

                // goodluck@lge.com 121107 merged from ICS [START]
                Context mContext = getPreferenceScreen().getContext();
                String mMccCode = getRuntimeMccCode(mContext);
                // goodluck@lge.com 121107 merged from ICS [END]

                // TODO: This is not an accurate way to display the locale, as it is
                // just working around the fact that we support limited dialects
                // and want to pretend that the language is valid for all locales.
                // We need a way to support languages that aren't tied to a particular
                // locale instead of hiding the locale qualifier.
                if (language.equals("zz")) {
                    String country = conf.locale.getCountry();
                    if (country.equals("ZZ")) {
                        localeString = "[Developer] Accented English (zz_ZZ)";
                    } else if (country.equals("ZY")) {
                        localeString = "[Developer] Fake Bi-Directional (zz_ZY)";
                    } else {
                        localeString = "";
                    }
                } else if (hasOnlyOneLanguageInstance(language,
                        Resources.getSystem().getAssets().getLocales())) {
                    localeString = conf.locale.getDisplayLanguage(conf.locale);
                } else {
                    localeString = conf.locale.getDisplayName(conf.locale);
                }

                // CAPP_LOCALEPICKER
                if ("es".equals(conf.locale.getLanguage())
                        && SystemProperties.getBoolean("persist.sys.cust.latamspanish", false)) {
                    // "Español (Latinoamérica)"
                    Locale latamSpanish = new Locale("es", "419");
                    localeString = latamSpanish.getDisplayName();
                }
                // CAPP_LOCALEPICKER

                if (localeString.length() > 1) {
                    localeString = Character.toUpperCase(localeString.charAt(0))
                            + localeString.substring(1);
                    // goodluck@lge.com 121122 merged from ICS [START]
                    if (!mMccCode.equals("294")
                            && ("mk_MK".equals(conf.locale.toString())
                                    || "mk".equals(conf.locale.toString()) || localeString
                                        .equals("Macedonian"))) {
                        mLanguagePref.setSummary("FYROM");
                        // goodluck@lge.com 121122 merged from ICS [END]
                        // [S][scottie.park.com][130116][common][TD:262866] Change language name
                    } else if ("eu_ES".equals(conf.locale.toString())
                            || localeString.equals("Basque (Spain)")) {
                        mLanguagePref.setSummary("Euskara");
                    } else if ("gl_ES".equals(conf.locale.toString())
                            || localeString.equals("Galician (Spain)")) {
                        mLanguagePref.setSummary("Galego");
                    } else if ("bs_BA".equals(conf.locale.toString())
                            || localeString.equals("Bosnian (Bosnia and Herzegovina)")) {
                        mLanguagePref.setSummary("Bosnian");
                    } else if ("et_EE".equals(conf.locale.toString())
                            || localeString.equals("Eesti (Eesti)")) {
                        mLanguagePref.setSummary("Eesti");
                    } else if ("is_IS".equals(conf.locale.toString())
                            || localeString.equals("Icelandic (Iceland)")) {
                        mLanguagePref.setSummary("Icelandic");
                    } else if ("ar_EG".equals(conf.locale.toString())
                            && localeString.equals("العربية (مصر)")) {
                        mLanguagePref.setSummary("العربية");
                        // [E][scottie.park.com][130116][common][TD:262866] Change language name
                    } else if ("ar_EG".equals(conf.locale.toString())
                            || "ar_IL".equals(conf.locale.toString())
                            || localeString.equals("(مصر) العربية")) {
                        mLanguagePref.setSummary("العربية");
                    } else if ("ku_IQ".equals(conf.locale.toString())) {
                        mLanguagePref.setSummary("Kurdish");
                    } else {
                        mLanguagePref.setSummary(localeString);
                    }
                }

                //2014-02-16 TaeHo-Kim (evan.kim@lge.com) [X3N/TD#175012]
                //Only espanol will be displayed when user select the Esponol lanugage or SIM Inserted [START]
               if ("EU".equals(Config.getCountry()) && "OPEN".equals(Config.getOperator())
                    && "es_ES".equals(conf.locale.toString())) {
                    mLanguagePref.setSummary("Español");
                }
                //2014-02-16 TaeHo-Kim (evan.kim@lge.com) [X3N/TD#175012]
                //Only espanol will be displayed when user select the Esponol lanugage or SIM Inserted  [END] 

            }

            // [Start][IMEED-158] 20130820 makutum.halyal crash when press back
            // key from language list
            if (Utils.isUI_4_1_model(getActivity())) {
                getPreferenceScreen().removePreference(mUserDictionaryPref);
            } else {
                updateUserDictionaryPreference(mUserDictionaryPref);
            }
            // [End][IMEED-158] 20130820 makutum.halyal crash when press back
            // key from language list
            if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
                mShowInputMethodSelectorPref.setOnPreferenceChangeListener(this);
            }
        }
        /*
                // Hard keyboard
                if (!mHardKeyboardPreferenceList.isEmpty()) {
                    for (int i = 0; i < HARDKEYBOAD_KEYS.length; ++i) {
                        CheckBoxPreference chkPref = (CheckBoxPreference)
                                mHardKeyboardCategory.findPreference(HARDKEYBOAD_KEYS[i]);
                        chkPref.setChecked(
                                System.getInt(getContentResolver(), SYSTEM_SETTINGNAMES[i], 1) > 0);
                    }
                }
        */
        updateInputDevices();

        // Refresh internal states in mInputMethodSettingValues to keep the latest
        // "InputMethodInfo"s and "InputMethodSubtype"s
        mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        // TODO: Consolidate the logic to InputMethodSettingsWrapper
        InputMethodAndSubtypeUtil.loadInputMethodSubtypeList(
                this, getContentResolver(),
                mInputMethodSettingValues.getInputMethodList(), null);
        updateInputMethodPreferenceViews();

        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        boolean checkPerfrom = getActivity().getIntent()
                .getBooleanExtra("perform", false);
        if (mSearch_result != null && !mSearch_result.equals("") && mIsFirst
                && checkPerfrom) {
            boolean newValue =  getActivity().getIntent().getBooleanExtra("newValue", false);
            Log.d("search", "newValue : " + newValue);
            startResult(newValue);
            mIsFirst = false;
        }
    }

    private void goMainScreen() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setClassName("com.android.settings",
                "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity");
        startActivity(i);
        getActivity().finish();
    }

    private void startResult(boolean newValue) {
        if (Utils.supportSplitView(getActivity())) {
            goMainScreen();
            return;
        }

        if (mSearch_result.equals(KEY_CURRENT_INPUT_METHOD)) {
            mCurrentPreference.performClick(getPreferenceScreen());
            return;
        } else if (mSearch_result.equals("tts_settings")) {
            mVoiceInputOutputSettings.getTtsSettingsPreference().performClick(getPreferenceScreen());
            return;
        }
        goMainScreen();
    }

    @Override
    public void onPause() {
        super.onPause();

        mIm.unregisterInputDeviceListener(this);
        mSettingsObserver.pause();

        if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
            mShowInputMethodSelectorPref.setOnPreferenceChangeListener(null);
        }
        // TODO: Consolidate the logic to InputMethodSettingsWrapper
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(
                this, getContentResolver(), mInputMethodSettingValues.getInputMethodList(),
                !mHardKeyboardPreferenceList.isEmpty());
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        updateInputDevices();
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        updateInputDevices();
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        updateInputDevices();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Input Method stuff
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (preference instanceof PreferenceScreen) {
            if (preference.getFragment() != null) {
                // Fragment will be handled correctly by the super class.
            } else if (KEY_CURRENT_INPUT_METHOD.equals(preference.getKey())) {
                final InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showInputMethodPicker();
            }
        } else if (preference instanceof CheckBoxPreference) {
            final CheckBoxPreference chkPref = (CheckBoxPreference)preference;
            /*
            if (!mHardKeyboardPreferenceList.isEmpty()) {
                for (int i = 0; i < HARDKEYBOAD_KEYS.length; ++i) {
                    if (chkPref == mHardKeyboardCategory.findPreference(HARDKEYBOAD_KEYS[i])) {
                        System.putInt(getContentResolver(), SYSTEM_SETTINGNAMES[i],
                                chkPref.isChecked() ? 1 : 0);
                        return true;
                    }
                }
            }
            */
            if (chkPref == mGameControllerCategory.findPreference("vibrate_input_devices")) {
                System.putInt(getContentResolver(), Settings.System.VIBRATE_INPUT_DEVICES,
                        chkPref.isChecked() ? 1 : 0);
                return true;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    // goodluck@lge.com 121107 merged from ICS [START]
    private String getRuntimeMccCode(Context context) {
        String retValue = "0";
        String mccCode = null;
        String simOperator = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getSimOperator();

        if (simOperator != null) {
            if (simOperator.length() >= 3) {
                mccCode = simOperator.substring(0, 3);
            }
        }

        if (mccCode != null) {
            if (!TextUtils.isEmpty(mccCode)) {
                retValue = mccCode;
            }
        }

        return retValue;
    }

    // goodluck@lge.com 121107 merged from ICS [END]

    private boolean hasOnlyOneLanguageInstance(String languageCode, String[] locales) {
        int count = 0;
        for (String localeCode : locales) {
            if (localeCode.length() > 2
                    && localeCode.startsWith(languageCode)) {
                count++;
                if (count > 1) {
                    return false;
                }
            }
        }
        return count == 1;
    }

    private void saveInputMethodSelectorVisibility(String value) {
        try {
            int intValue = Integer.valueOf(value);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.INPUT_METHOD_SELECTOR_VISIBILITY, intValue);
            updateInputMethodSelectorSummary(intValue);
        } catch (NumberFormatException e) {
            Log.w("[InputMethodAndLanguageSettings]", "NumberFormatException");
        }
    }

    private int loadInputMethodSelectorVisibility() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.INPUT_METHOD_SELECTOR_VISIBILITY,
                mDefaultInputMethodSelectorVisibility);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (SHOW_INPUT_METHOD_SWITCHER_SETTINGS) {
            if (preference == mShowInputMethodSelectorPref) {
                if (value instanceof String) {
                    saveInputMethodSelectorVisibility((String)value);
                }
            }
        }
        return false;
    }

    private void updateInputMethodPreferenceViews() {
        synchronized (mInputMethodPreferenceList) {
            // Clear existing "InputMethodPreference"s
            for (final InputMethodPreference imp : mInputMethodPreferenceList) {
                mKeyboardSettingsCategory.removePreference(imp);
            }
            mInputMethodPreferenceList.clear();
            List<String> permittedList = mDpm.getPermittedInputMethodsForCurrentUser();
            final List<InputMethodInfo> imis = mInputMethodSettingValues.getInputMethodList();
            final int mN = (imis == null ? 0 : imis.size());
            for (int i = 0; i < mN; ++i) {
                final InputMethodInfo imi = imis.get(i);
                mIsAllowedByOrganization = permittedList == null
                        || permittedList.contains(imi.getPackageName());
                Log.d("[InputMethodAndLanguageSettings]", "mIsAllowedByOrganization : " + mIsAllowedByOrganization);
                final InputMethodPreference pref = getInputMethodPreference(imi);
                pref.setOnImePreferenceChangeListener(mOnImePreferenceChangedListener);
                mInputMethodPreferenceList.add(pref);
            }

            if (!mInputMethodPreferenceList.isEmpty()) {
                Collections.sort(mInputMethodPreferenceList);
                for (int i = 0; i < mN; ++i) {
                    mKeyboardSettingsCategory.addPreference(mInputMethodPreferenceList.get(i));
                }
            }

            // update views status

            for (Preference pref : mInputMethodPreferenceList) {
                if (pref instanceof InputMethodPreference) {
                    ((InputMethodPreference)pref).updatePreferenceViews();
                }
            }
        }
        updateCurrentImeName();
    }

    private void updateCurrentImeName() {
        final Context context = getActivity();
        if (context == null || mImm == null) {
            return;
        }
        final Preference curPref = getPreferenceScreen().findPreference(KEY_CURRENT_INPUT_METHOD);
        mCurrentPreference = curPref;
        //LGE_JP_FWK 20130228 minho.choo@lge.com [A]featuring for hiding HandwritingIME for JP[START]
        String targetCountry = Config.getCountry();
        if (targetCountry.equalsIgnoreCase("JP")
                && (Build.MODEL.equals("L-04E") || Build.MODEL.equals("L-05E"))) {
            List<InputMethodInfo> imis = mImm.getInputMethodList();
            if (curPref == null || imis == null) {
                return;
            }

            CharSequence summary = null;
            CharSequence summaryIwnn = null;
            boolean isCurHandwriting = false;
            final String currentInputMethodId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            for (InputMethodInfo imi : imis) {
                if (currentInputMethodId.equals(imi.getId())) {
                    if (imi.getPackageName().equals(
                            "com.sevenknowledge.sevennotestabproduct.lg001.tab50")) {
                        isCurHandwriting = true;
                        continue;
                    }
                    final android.view.inputmethod.InputMethodSubtype subtype = mImm
                            .getCurrentInputMethodSubtype();
                    final CharSequence imiLabel = imi.loadLabel(getPackageManager());
                    summary = subtype != null
                            ? TextUtils.concat(subtype.getDisplayName(context,
                                    imi.getPackageName(), imi.getServiceInfo().applicationInfo),
                                    (TextUtils.isEmpty(imiLabel) ?
                                            "" : " - " + imiLabel))
                            : imiLabel;
                }
                if (imi.getPackageName().equals("jp.co.omronsoft.iwnnime.ml")) {
                    summaryIwnn = imi.loadLabel(getPackageManager());
                }
            }

            final CharSequence curIme = (isCurHandwriting) ? summaryIwnn : summary;
            if (!TextUtils.isEmpty(curIme)) {
                synchronized (this) {
                    curPref.setSummary(curIme);
                }
            }
            return;
        }
        //LGE_JP_FWK [END]
        if (curPref != null) {
            final CharSequence curIme =
                    mInputMethodSettingValues.getCurrentInputMethodName(context);
            if (!TextUtils.isEmpty(curIme)) {
                synchronized (this) {
                    curPref.setSummary(curIme);
                }
            }
        }
    }

    private InputMethodPreference getInputMethodPreference(InputMethodInfo imi) {
        final PackageManager pm = getPackageManager();
        final CharSequence label = imi.loadLabel(pm);
        // IME settings
        final Intent intent;
        final String settingsActivity = imi.getSettingsActivity();
        if (!TextUtils.isEmpty(settingsActivity)) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(imi.getPackageName(), settingsActivity);
        } else {
            intent = null;
        }

        // Add a check box for enabling/disabling IME
        final InputMethodPreference pref =
                new InputMethodPreference(this, intent, mImm, imi, mIsAllowedByOrganization);
        pref.setKey(imi.getId());
        pref.setTitle(label);
        return pref;
    }

    private void updateInputDevices() {
        updateHardKeyboards();
        updateGameControllers();
    }

    private void updateHardKeyboards() {
        mHardKeyboardPreferenceList.clear();
        if (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY) {
            final int[] devices = InputDevice.getDeviceIds();
            for (int i = 0; i < devices.length; i++) {
                InputDevice device = InputDevice.getDevice(devices[i]);
                final InputDeviceIdentifier inputDeviceIdentifier;
                if (device != null
                        && !device.isVirtual()
                        && device.isFullKeyboard()) {
                    inputDeviceIdentifier = device.getIdentifier();
                    final String keyboardLayoutDescriptor =
                            mIm.getCurrentKeyboardLayoutForInputDevice(inputDeviceIdentifier);
                    final KeyboardLayout keyboardLayout = keyboardLayoutDescriptor != null ?
                            mIm.getKeyboardLayout(keyboardLayoutDescriptor) : null;

                    final PreferenceScreen pref = new PreferenceScreen(getActivity(), null);
                    pref.setTitle(device.getName());
                    if (keyboardLayout != null) {
                        pref.setSummary(keyboardLayout.toString());
                    } else {
                        pref.setSummary(R.string.keyboard_layout_default_label);
                    }
                    pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            showKeyboardLayoutDialog(inputDeviceIdentifier);
                            return true;
                        }
                    });
                    mHardKeyboardPreferenceList.add(pref);
                }
            }
        }
        /*
                if (!mHardKeyboardPreferenceList.isEmpty()) {
                    for (int i = mHardKeyboardCategory.getPreferenceCount(); i-- > 0; ) {
                        final Preference pref = mHardKeyboardCategory.getPreference(i);
                        if (pref.getOrder() < 1000) {
                            mHardKeyboardCategory.removePreference(pref);
                        }
                    }

                    Collections.sort(mHardKeyboardPreferenceList);
                    final int count = mHardKeyboardPreferenceList.size();
                    for (int i = 0; i < count; i++) {
                        final Preference pref = mHardKeyboardPreferenceList.get(i);
                        pref.setOrder(i);
                        mHardKeyboardCategory.addPreference(pref);
                    }

                    getPreferenceScreen().addPreference(mHardKeyboardCategory);
                } else {
                    getPreferenceScreen().removePreference(mHardKeyboardCategory);
                }
                */
    }

    private void showKeyboardLayoutDialog(InputDeviceIdentifier inputDeviceIdentifier) {
        KeyboardLayoutDialogFragment fragment =
                new KeyboardLayoutDialogFragment(inputDeviceIdentifier);
        fragment.setTargetFragment(this, 0);
        fragment.show(getActivity().getFragmentManager(), "keyboardLayout");
    }

    @Override
    public void onSetupKeyboardLayouts(InputDeviceIdentifier inputDeviceIdentifier) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(getActivity(), KeyboardLayoutPickerActivity.class);
        intent.putExtra(KeyboardLayoutPickerFragment.EXTRA_INPUT_DEVICE_DESCRIPTOR,
                inputDeviceIdentifier);
        mIntentWaitingForResult = intent;
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mIntentWaitingForResult != null) {
            InputDeviceIdentifier inputDeviceIdentifier = (InputDeviceIdentifier)mIntentWaitingForResult.getParcelableExtra("input_device_identifier");
            mIntentWaitingForResult = null;
            showKeyboardLayoutDialog(inputDeviceIdentifier);
        }
    }

    private void updateGameControllers() {
        if (haveInputDeviceWithVibrator()) {
            getPreferenceScreen().addPreference(mGameControllerCategory);

            CheckBoxPreference chkPref = (CheckBoxPreference)
                    mGameControllerCategory.findPreference("vibrate_input_devices");
		    mVibrateInputDevices = chkPref;
            chkPref.setChecked(System.getInt(getContentResolver(),
                    Settings.System.VIBRATE_INPUT_DEVICES, 1) > 0);
        } else {
            getPreferenceScreen().removePreference(mGameControllerCategory);
        }
    }

    private boolean haveInputDeviceWithVibrator() {
        final int[] devices = InputDevice.getDeviceIds();
        for (int i = 0; i < devices.length; i++) {
            InputDevice device = InputDevice.getDevice(devices[i]);
            if (device != null && !device.isVirtual() && device.getVibrator().hasVibrator()) {
                return true;
            }
        }
        return false;
    }

    private class SettingsObserver extends ContentObserver {
        private Context mContext;

        public SettingsObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            updateCurrentImeName();
        }

        public void resume() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD), false, this);
            cr.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE), false, this);
        }

        public void pause() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {

        @Override
        public List<SearchIndexableRaw> getRawDataToIndex(Context context,
                boolean enabled) {
            if (mResult != null) {
                mResult.clear();
            }

            String strSearchCategoryTitle = context.getString(R.string.language_settings);
            setSearchIndexData(context, "inputmethod_language_settings",
                    context.getString(R.string.language_settings), "main",
                    null, null,
					"android.intent.action.MAIN",
                    "com.android.settings.Settings$InputMethodAndLanguageSettingsActivity",
                    "com.android.settings", 1,
                    null, null, null, 1, 0);

            setSearchIndexData(context, KEY_PHONE_LANGUAGE,
                    context.getString(R.string.phone_language),
                    strSearchCategoryTitle,
                    null,
                    null, "android.settings.LOCALE_SETTINGS",
                    null, null, 1,
                    null, null, null, 1, 0);

            setSearchIndexData(context, KEY_CURRENT_INPUT_METHOD,
                    context.getString(R.string.current_input_method),
                    strSearchCategoryTitle,
                    null,
                    null, "android.settings.INPUT_METHOD_SETTINGS",
                    null, null, 1,
                    null, null, null, 1, 0);

            setSearchIndexData(context, "tts_settings",
                    context.getString(R.string.tts_settings_title),
                    strSearchCategoryTitle,
                    null,
                    null, "android.settings.INPUT_METHOD_SETTINGS",
                    null, null, 1,
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
