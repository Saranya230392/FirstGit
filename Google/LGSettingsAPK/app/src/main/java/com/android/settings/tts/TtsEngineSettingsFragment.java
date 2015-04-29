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

package com.android.settings.tts;

import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TtsEngines;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class TtsEngineSettingsFragment extends SettingsPreferenceFragment implements
        OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String TAG = "TtsEngineSettings";
    private static final boolean DBG = false;

    private static final String KEY_ENGINE_LOCALE = "tts_default_lang";
    private static final String KEY_ENGINE_SETTINGS = "tts_engine_settings";
    private static final String KEY_INSTALL_DATA = "tts_install_data";

    private static final String STATE_KEY_LOCALE_ENTRIES = "locale_entries";
    private static final String STATE_KEY_LOCALE_ENTRY_VALUES = "locale_entry_values";
    private static final String STATE_KEY_LOCALE_VALUE = "locale_value";

    private static final int VOICE_DATA_INTEGRITY_CHECK = 1977;

    private TtsEngines mEnginesHelper;
    private ListPreference mLocalePreference;
    private Preference mEngineSettingsPreference;
    private Preference mInstallVoicesPreference;
    private Intent mEngineSettingsIntent;
    private String mDefaultLocaleStr = null;
    private Locale mCurrentLocale;
    //private Intent mVoiceDataDetails;

    private SharedPreferences mDefaultLanguagePreference;
    private SharedPreferences.Editor mDefaultLanguageEditor;
    private SharedPreferences mSelectedLanguagePreference;
    private SharedPreferences.Editor mSelectedLanguageEditor;

    private static final String SYSTEM_LANGUAGE = "system_language";
    private static final String SELECTED_LANGUAGE = "selected_language";
    private static final String LG_TTS = "com.lge.tts.sfplus";
    private String mCurrentEngine;

    private TextToSpeech mTts;

    private int mSelectedLocaleIndex = -1;

    private final TextToSpeech.OnInitListener mTtsInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status != TextToSpeech.SUCCESS) {
                finishFragment();
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLocalePreference.setEnabled(true);
                        updateVoiceDetails();
                    }
                });
            }
        }
    };

    private final BroadcastReceiver mLanguagesChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Installed or uninstalled some data packs
            if (TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED.equals(intent.getAction())) {
                checkTtsData();
            }
        }
    };

    public TtsEngineSettingsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tts_engine_settings);
        mEnginesHelper = new TtsEngines(getActivity());
        mTts = new TextToSpeech(getActivity().getApplicationContext(), mTtsInitListener,
                getEngineName());
        mCurrentEngine = mTts.getCurrentEngine();
        getActivity().setTitle(R.string.tts_settings);
        if (!Utils.supportSplitView(getActivity())) {
            getActivity().getActionBar().setIcon(R.drawable.shortcut_language);
        }
        final PreferenceScreen root = getPreferenceScreen();
        mLocalePreference = (ListPreference)root.findPreference(KEY_ENGINE_LOCALE);
        mLocalePreference.setOnPreferenceChangeListener(this);
        mEngineSettingsPreference = root.findPreference(KEY_ENGINE_SETTINGS);
        mEngineSettingsPreference.setOnPreferenceClickListener(this);
        mInstallVoicesPreference = root.findPreference(KEY_INSTALL_DATA);
        mInstallVoicesPreference.setOnPreferenceClickListener(this);
        root.removePreference(mInstallVoicesPreference);
        root.setTitle(getEngineLabel());
        root.setKey(getEngineName());
        mEngineSettingsPreference.setTitle(getResources().getString(
                R.string.tts_engine_settings_title, getEngineLabel()));

        mEngineSettingsIntent = mEnginesHelper.getSettingsIntent(getEngineName());
        if (mEngineSettingsIntent == null) {
            mEngineSettingsPreference.setEnabled(false);
        }
        mInstallVoicesPreference.setEnabled(false);
        if (mCurrentEngine.equals(LG_TTS)) {
            saveLGTTSSelectedLanguage();

        } else {
            saveGoogleSelectedLanguage(savedInstanceState);
        }
        // Check if data packs changed
            checkTtsData();

            getActivity().registerReceiver(mLanguagesChangedReceiver,
                new IntentFilter(TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED));
    }

    private void saveGoogleSelectedLanguage(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mLocalePreference.setEnabled(false);
            mLocalePreference.setEntries(new CharSequence[0]);
            mLocalePreference.setEntryValues(new CharSequence[0]);
        } else {
            // Repopulate mLocalePreference with saved state. Will be updated later with
            // up-to-date values when checkTtsData() calls back with results.
            final CharSequence[] entries =
                savedInstanceState.getCharSequenceArray(STATE_KEY_LOCALE_ENTRIES);
            final CharSequence[] entryValues =
                 savedInstanceState.getCharSequenceArray(STATE_KEY_LOCALE_ENTRY_VALUES);
            final CharSequence value =
                 savedInstanceState.getCharSequence(STATE_KEY_LOCALE_VALUE);

            mLocalePreference.setEntries(entries);
            mLocalePreference.setEntryValues(entryValues);
            mLocalePreference.setValue(value != null ? value.toString() : null);
            mLocalePreference.setEnabled(entries.length > 0);
         }
    }

    private void saveLGTTSSelectedLanguage() {
        mDefaultLocaleStr = Utils.getDefaultLocale();
        mDefaultLanguagePreference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mDefaultLanguageEditor = mDefaultLanguagePreference.edit();
        if (getSystemLocale() == null) {
            setSystemLocale(mDefaultLocaleStr);
        }
        mSelectedLanguagePreference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mSelectedLanguageEditor = mSelectedLanguagePreference.edit();
        if (getSelectedLocale() == null) {
            setSelectedLocale(mDefaultLocaleStr);
        }
    }


    public void setSystemLocale(String localeLanguage) {
           mDefaultLanguageEditor.putString(SYSTEM_LANGUAGE, localeLanguage);
           mDefaultLanguageEditor.apply();
    }

    public String getSystemLocale() {
        String value = null;
        if (mDefaultLanguagePreference != null) {
            value = mDefaultLanguagePreference.getString(SYSTEM_LANGUAGE, mDefaultLocaleStr);
        }
        return value;
    }

    public void setSelectedLocale(String selectedLanguage) {
           mSelectedLanguageEditor.putString(SELECTED_LANGUAGE, selectedLanguage);
           mSelectedLanguageEditor.apply();
    }

    public String getSelectedLocale() {
        String value = null;
        if (mSelectedLanguagePreference != null) {
            mCurrentLocale = mEnginesHelper.getLocalePrefForEngine(
                    getEngineName());
            value = mSelectedLanguagePreference.getString(SELECTED_LANGUAGE, getLocaleString(mCurrentLocale));
        }
        return value;
    }

    @Override
    public void onDestroy() {
        if (mCurrentEngine.equals(LG_TTS)) {
            setSystemLocale(mDefaultLocaleStr);
        }
        getActivity().unregisterReceiver(mLanguagesChangedReceiver);
        mTts.shutdown();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the mLocalePreference values, so we can repopulate it with entries.
        outState.putCharSequenceArray(STATE_KEY_LOCALE_ENTRIES,
                mLocalePreference.getEntries());
        outState.putCharSequenceArray(STATE_KEY_LOCALE_ENTRY_VALUES,
                mLocalePreference.getEntryValues());
        outState.putCharSequence(STATE_KEY_LOCALE_VALUE,
                mLocalePreference.getValue());
    }

    private final void checkTtsData() {
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        intent.setPackage(getEngineName());
        try {
            if (DBG) {
                Log.d(TAG, "Updating engine: Checking voice data: " + intent.toUri(0));
            }
            startActivityForResult(intent, VOICE_DATA_INTEGRITY_CHECK);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Failed to check TTS data, no activity found for " + intent + ")");
        }
    }

    private void updateVoiceDetails() {
        final Intent voiceDataDetails = getArguments().getParcelable(
                TtsEnginePreference.FRAGMENT_ARGS_VOICES);
        // [S][2012.01.03][hyoungjun21.lee@lge.com][Common] Fix WBT issues
        if (voiceDataDetails == null) {
            Log.e(TAG, "TTS data check failed (voiceDataDetails == null).");
            return;
        }
        // [E][2012.01.03][hyoungjun21.lee@lge.com][Common] Fix WBT issues
        if (DBG) {
            Log.d(TAG, "Parsing voice data details, data: " + voiceDataDetails.toUri(0));
        }
        ArrayList<String> available = voiceDataDetails.getStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
        ArrayList<String> unavailable = voiceDataDetails.getStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES);

        //[S][2012.01.15][jm.lee2] ICS 4.0.3 merge
        if (available == null) {
            Log.e(TAG, "TTS data check failed (available == null).");
            // [S][2012.01.20][seungeene][TD_123405] Add the concept that tts sub menus are activated as a state of available voices
            /*
            final CharSequence[] empty = new CharSequence[0];
            mLocalePreference.setEntries(empty);
            mLocalePreference.setEntryValues(empty);
            */
            mLocalePreference.setEnabled(false);
            // [E][2012.01.20][seungeene][TD_123405] Add the concept that tts sub menus are activated as a state of available voices
            return;
        }

        if (unavailable != null && unavailable.size() > 0) {
            mInstallVoicesPreference.setEnabled(true);
            getPreferenceScreen().addPreference(mInstallVoicesPreference);
            Log.d(TAG, "[updateVoiceDetails] unavailable.size() = " + unavailable.size());
        } else {
            getPreferenceScreen().removePreference(mInstallVoicesPreference);
        }
        //[E][2012.01.15][jm.lee2] ICS 4.0.3 merge

        if (available.size() > 0) {
            if (mCurrentEngine.equals(LG_TTS)) {
            try {
                updateLGTTSDefaultLocalePref(available);
               }  catch (Exception e) {
                  e.printStackTrace();
               }
            } else {
                updateDefaultLocalePref(available);
            }
        } else {
            mLocalePreference.setEnabled(false);
        }
    }

    private void updateLGTTSDefaultLocalePref(ArrayList<String> availableLangs) {
    // LGtts support china operator (chinese & english)
           mCurrentLocale = mEnginesHelper.getLocalePrefForEngine(
                    getEngineName());

            CharSequence[] entries = new CharSequence[availableLangs.size()];
            CharSequence[] entryValues = new CharSequence[availableLangs.size()];

            int selectedLanguageIndex = 0;
            for (int i = 0; i < availableLangs.size(); i++) {
                String[] langCountryVariant = availableLangs.get(i).split("-");
                Locale loc = null;
                if (langCountryVariant.length == 3) {
                    loc = new Locale(langCountryVariant[0], langCountryVariant[1],
                            langCountryVariant[2]);
                    if (langCountryVariant[1].equals("USA")) {
                          if (langCountryVariant[2].equals("FEMALE1")) {
                              entries[i] = getString(R.string.tts_lang_usa_female1);
                          }
                          if (langCountryVariant[2].equals("FEMALE2")) {
                              entries[i] = getString(R.string.tts_lang_usa_female2);
                          } 
                     } else {
                        entries[i] = loc.getDisplayName();
                    }
                } else {
                     if (langCountryVariant.length == 1) {
                        loc = new Locale(langCountryVariant[0]);
                     } else if (langCountryVariant.length == 2) {
                        loc = new Locale(langCountryVariant[0], langCountryVariant[1]);
                     }
                     entries[i] = loc.getDisplayName();
                }
                entryValues[i] = availableLangs.get(i);
                String currentLocaleString = getLocaleString(mCurrentLocale);
                String entryValueString = String.valueOf(entryValues[i]);
                if (entryValueString.equalsIgnoreCase(currentLocaleString)) {
                    selectedLanguageIndex = i;
                    mLocalePreference.setSummary(entries[i]);
                }
           }
            mLocalePreference.setEntries(entries);
            mLocalePreference.setEntryValues(entryValues);
            mLocalePreference.setValueIndex(selectedLanguageIndex);
    }

    private void updateDefaultLocalePref(ArrayList<String> availableLangs) {
    // google tts migration
        if (availableLangs == null || availableLangs.size() == 0) {
            mLocalePreference.setEnabled(false);
            return;
        }
        Locale currentLocale = null;
        if (!mEnginesHelper.isLocaleSetToDefaultForEngine(getEngineName())) {
            currentLocale = mEnginesHelper.getLocalePrefForEngine(getEngineName());
        }

        ArrayList<Pair<String, Locale>> entryPairs =
                new ArrayList<Pair<String, Locale>>(availableLangs.size());
        for (int i = 0; i < availableLangs.size(); i++) {
            Locale locale = mEnginesHelper.parseLocaleString(availableLangs.get(i));
            if (locale != null) {
                entryPairs.add(new Pair<String, Locale>(
                        locale.getDisplayName(), locale));
            }
        }

        // Sort it
        Collections.sort(entryPairs, new Comparator<Pair<String, Locale>>() {
            @Override
            public int compare(Pair<String, Locale> lhs, Pair<String, Locale> rhs) {
                return lhs.first.compareToIgnoreCase(rhs.first);
            }
        });

        mSelectedLocaleIndex = 0; // use system language
        CharSequence[] entries = new CharSequence[availableLangs.size() + 1];
        CharSequence[] entryValues = new CharSequence[availableLangs.size() + 1];

        entries[0] = getActivity().getString(R.string.use_system_language_to_select_input_method_subtypes);
        entryValues[0] = "";

        int i = 1;
        for (Pair<String, Locale> entry : entryPairs) {
            if (entry.second.equals(currentLocale)) {
                mSelectedLocaleIndex = i;
            }
            entries[i] = entry.first;
            entryValues[i++] = entry.second.toString();
        }

        mLocalePreference.setEntries(entries);
        mLocalePreference.setEntryValues(entryValues);
        mLocalePreference.setEnabled(true);
        setLocalePreference(mSelectedLocaleIndex);
    }


    /** Set entry from entry table in mLocalePreference */
    private void setLocalePreference(int index) {
        if (index < 0) {
            mLocalePreference.setValue("");
            mLocalePreference.setSummary(R.string.tts_lang_not_selected);
        } else {
            mLocalePreference.setValueIndex(index);
            mLocalePreference.setSummary(mLocalePreference.getEntries()[index]);
        }
    }

    /**
     * Ask the current default engine to launch the matching INSTALL_TTS_DATA activity
     * so the required TTS files are properly installed.
     */
    private void installVoiceData() {
        if (TextUtils.isEmpty(getEngineName())) {
            return;
        }
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getEngineName());
        try {
            Log.v(TAG, "Installing voice data: " + intent.toUri(0));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Failed to install TTS data, no acitivty found for " + intent + ")");
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mInstallVoicesPreference) {
            installVoiceData();
            return true;
        } else if (preference == mEngineSettingsPreference) {
            startActivity(mEngineSettingsIntent);
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLocalePreference) {
            String localeString = (String)newValue;
            updateLanguageTo((!TextUtils.isEmpty(localeString) ?
                    mEnginesHelper.parseLocaleString(localeString) : null));
            if (getEngineName() != null && mCurrentEngine.equals(LG_TTS)) {
                mEnginesHelper.updateLocalePrefForEngine(getEngineName(), (!TextUtils.isEmpty(localeString) ?
                    mEnginesHelper.parseLocaleString(localeString) : null));
            }
            updateVoiceDetails();
            return true;
        }
        return false;
    }

    private void updateLanguageTo(Locale locale) {
        int selectedLocaleIndex = -1;
        String localeString = (locale != null) ? locale.toString() : "";
        for (int i = 0; i < mLocalePreference.getEntryValues().length; i++) {
            if (localeString.equalsIgnoreCase(mLocalePreference.getEntryValues()[i].toString())) {
                selectedLocaleIndex = i;
                break;
            }
        }

        if (selectedLocaleIndex == -1) {
            Log.w(TAG, "updateLanguageTo called with unknown locale argument");
            return;
        }
        mLocalePreference.setSummary(mLocalePreference.getEntries()[selectedLocaleIndex]);
        mSelectedLocaleIndex = selectedLocaleIndex;
        mEnginesHelper.updateLocalePrefForEngine(getEngineName(), locale);

        if (getEngineName().equals(mTts.getCurrentEngine()) && !mCurrentEngine.equals(LG_TTS)) {
            // Null locale means "use system default"
            mTts.setLanguage((locale != null) ? locale : Locale.getDefault());
        }
    }

    private String getEngineName() {
        return getArguments().getString(TtsEnginePreference.FRAGMENT_ARGS_NAME);
    }

    private String getEngineLabel() {
        return getArguments().getString(TtsEnginePreference.FRAGMENT_ARGS_LABEL);
    }

    private String getLocaleString(Locale locale) {
        String localeString;
        if (locale != null) {
            localeString = locale.getISO3Language();
            if (!TextUtils.isEmpty(locale.getISO3Country())) {
                localeString += "-" + locale.getISO3Country();
            } else {
                return localeString;
            }
            if (!TextUtils.isEmpty(locale.getVariant())) {
                localeString += "-" + locale.getVariant();
            }
        } else {
            localeString = "";
        }
        return localeString;
    }
}
