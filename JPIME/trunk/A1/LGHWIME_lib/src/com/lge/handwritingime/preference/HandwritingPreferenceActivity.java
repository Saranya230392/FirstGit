package com.lge.handwritingime.preference;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

import com.lge.handwritingime.R;

public class HandwritingPreferenceActivity extends PreferenceActivity {
    private ListPreference mListPrefJapanType;
    private ListPreference mListPrefLanguage;
    private ListPreference mWorkingActiveMode;
    private PreferenceCategory mHandwritingCategory;

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mHandwritingCategory = (PreferenceCategory) findPreference("handwriting_pref_category");
        mListPrefLanguage = (ListPreference) findPreference(getString(R.string.HW_PREF_KEY_WORKING_LANGUAGE));
        mListPrefJapanType = (ListPreference) findPreference(getString(R.string.HW_PREF_KEY_LANGUAGE_JAPAN_TYPE));
        mWorkingActiveMode = (ListPreference) findPreference(getString(R.string.HW_PREF_KEY_WORKING_ACTIVE_MODE));

        mHandwritingCategory.removePreference(mListPrefLanguage);
        mHandwritingCategory.removePreference(mListPrefJapanType);
        mHandwritingCategory.removePreference(mWorkingActiveMode);
    }
}
