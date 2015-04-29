/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.search;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewDebug.FlagToString;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.remote.LGPreferenceActivity;
import com.android.settings.search.SearchSwitchPreference;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchResultsSummary extends LGPreferenceActivity implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private static final String LOG_TAG = "SearchResultsSummary";
    private Cursor mCursor;
    private HashMap<String, Context> mContextMap = new HashMap<String, Context>();
    private boolean mDataValid;
    private PreferenceScreen mPreference;
    private UpdateSearchResultsTask mUpdateSearchResultsTask;
    private ArrayList<SearchResult> mSearchResult;
    private ArrayList<PreferenceCategory> mPreferenceCategory;
    private String mQuery;
    private SearchView mSearchView;
    private View actionbar_view;
    private RelativeLayout mEmptyLayout;
    private TextView mEmptyText;

    private class UpdateSearchResultsTask extends
            AsyncTask<String, Void, Cursor> {
        @Override
        protected Cursor doInBackground(String... params) {
            return Index.getInstance(getApplicationContext()).search(params[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (!isCancelled()) {
                updataeIndexItem(cursor);
                if (mSearchResult.size() == 0) {
                    addNoResultDataPreference();
                } else {
                    addResultPreference();
                }
            } else if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Index.getInstance(getApplicationContext()).update();
        addPreferencesFromResource(R.xml.settings_search);
        setContentView(R.layout.settings_search_layout);
        mEmptyLayout = (RelativeLayout)findViewById(R.id.emptyView);
        mEmptyText = (TextView)findViewById(R.id.emptyViewText);
        mPreference = getPreferenceScreen();
        mSearchResult = new ArrayList<SearchResult>();
        mPreferenceCategory = new ArrayList<PreferenceCategory>();
        if (getActionBar() != null) {
            getActionBar().setTitle(null);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSearchView != null) {
            mSearchView.requestFocus();
            updateSearchResults();
        }
        if (mKeyPadHandler != null) {
            mKeyPadHandler.removeMessages(0);
            mKeyPadHandler.sendEmptyMessageDelayed(0, 300);
        }
    }

    private Handler mKeyPadHandler = new Handler() {
        public void handleMessage(Message msg) {

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInputUnchecked(0, null);
            }
        };
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionbar_view = new RelativeLayout(this);
        actionbar_view.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mSearchView = new SearchView(this);
        mSearchView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        setupSearchView();
        ((RelativeLayout)actionbar_view).addView(mSearchView);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(actionbar_view);
        mSearchView.requestFocus();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSearchView() {
        mSearchView.setQueryHint(getString(R.string.settings_search_hint));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | EditorInfo.IME_ACTION_DONE);
        mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(arg0, 0);
                    }
                }
            }
        });

        mSearchView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_MENU) {
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        if (mSearchView != null && mSearchView.getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mUpdateSearchResultsTask = null;
        mSearchView = null;
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        clearResults();
    }

    public void addResultPreference() {
        mEmptyLayout.setVisibility(View.GONE);
        mPreference.removeAll();
        mPreferenceCategory.clear();
        for (int i = 0; i < mSearchResult.size(); i++) {
            if (mSearchResult.get(i).visible == 0) {
                continue;
            }
            
            String title = mSearchResult.get(i).title;
            String screenTitle = mSearchResult.get(i).screenTitle;
            String preferenceType = mSearchResult.get(i).preferenceType;
            String dbTable = mSearchResult.get(i).settingsDBTableName;
            String dbField = mSearchResult.get(i).settingsDBField;
            String summaryOn = mSearchResult.get(i).summaryOn;
            String summaryOff = mSearchResult.get(i).summaryOff;
            String intentAction = mSearchResult.get(i).intentAction;
            String intentPackage = mSearchResult.get(i).intentTargetPackage;
            String intentTargetClass = mSearchResult.get(i).intentTargetClass;
            String key = mSearchResult.get(i).key;
            int currentEnable = mSearchResult.get(i).currentEnable;
            int checkValue = mSearchResult.get(i).checkValue;
            
            if (!checkPreferenceCategory(screenTitle)) {
                setPreferenceCategoty(screenTitle);
            }
            if ("CheckBox".equals(preferenceType)) {
                setCheckBoxPreference(title, summaryOn, summaryOff, dbTable,
                        dbField, intentAction, intentPackage,
                        intentTargetClass, key, currentEnable, screenTitle,
                        checkValue);
            } else if ("Switch".equals(preferenceType)) {
                setSwitchPreference(title, summaryOn, summaryOff, dbTable,
                        dbField, intentAction, intentPackage,
                        intentTargetClass, key, currentEnable, screenTitle,
                        checkValue);
            } else {
                setPreference(title, summaryOn, intentAction,
                        intentPackage, intentTargetClass, key, screenTitle);
            }
        }
    }

    private void setPreferenceCategoty(String screenTitle) {
        PreferenceCategory preferenceCategory = new PreferenceCategory(this);
        preferenceCategory.setKey(screenTitle);
        if ("main".equals(screenTitle)) {
            preferenceCategory.setTitle(getResources().getString(R.string.main_menu));
            preferenceCategory.setOrder(0);
        } else {
            preferenceCategory.setTitle(screenTitle);
            preferenceCategory.setOrder(1);
        }
        mPreferenceCategory.add(preferenceCategory);
        mPreference.addPreference(preferenceCategory);
    }

    private void setCheckBoxPreference(String title, String summaryOn,
            String summaryOff, String dbTable, String dbField,
            String intentAction, String intentPackage, String intentClass,
            String key, int currentEnable, String screenTitle, int checkValue) {
        SearchCheckBoxPreference checkBoxPreference = new SearchCheckBoxPreference(
                this);
        checkBoxPreference.setTitle(highlightSearchText(mQuery, title));
        checkBoxPreference.setSummary(highlightSearchText(mQuery, summaryOn));
        checkBoxPreference.setSummaryOn(highlightSearchText(mQuery, summaryOn));
        checkBoxPreference
                .setSummaryOff(highlightSearchText(mQuery, summaryOff));
        if (dbTable != null && dbField != null) {
            checkBoxPreference.setChecked(checkIndexItemChecked(dbTable,
                    dbField));
        } else {
            checkBoxPreference.setChecked(checkValue == 1);
        }
        checkBoxPreference.setIntent(setPreferenceInentAction(intentAction,
                intentPackage, intentClass, key));
        checkBoxPreference.setEnabled(currentEnable == 1);
        checkBoxPreference.setOrder(0);
        PreferenceCategory preferenceCategory = (PreferenceCategory)mPreference
                .findPreference(screenTitle);
        preferenceCategory.addPreference(checkBoxPreference);
        mPreference.removePreference(findPreference(screenTitle));
        mPreference.addPreference(preferenceCategory);
    }

    private void setSwitchPreference(String title, String summaryOn,
            String summaryOff, String dbTable, String dbField,
            String intentAction, String intentPackage, String intentClass,
            String key, int currentEnable, String screenTitle, int checkValue) {
        SearchSwitchPreference switchPreference = new SearchSwitchPreference(
                this);
        switchPreference.setTitle(highlightSearchText(mQuery, title));
        switchPreference.setSummary(highlightSearchText(mQuery, summaryOn));
        switchPreference.setSummaryOn(highlightSearchText(mQuery, summaryOn));
        switchPreference.setSummaryOff(highlightSearchText(mQuery, summaryOff));
        switchPreference.setSwitchTextOff("");
        switchPreference.setSwitchTextOn("");
        if (dbTable != null && dbField != null) {
            switchPreference
                    .setChecked(checkIndexItemChecked(dbTable, dbField));
        } else {
            switchPreference.setChecked(checkValue == 1);
        }
        switchPreference.setIntent(setPreferenceInentAction(intentAction,
                intentPackage, intentClass, key));
        switchPreference.setEnabled(currentEnable == 1);
        if ("main".equals(screenTitle)) {
            switchPreference.setIcon(MainIcon.getIconkForTitle(title));
        }
        switchPreference.setOrder(0);
        PreferenceCategory preferenceCategory = (PreferenceCategory)mPreference
                .findPreference(screenTitle);
        preferenceCategory.addPreference(switchPreference);
        mPreference.removePreference(findPreference(screenTitle));
        mPreference.addPreference(preferenceCategory);
    }

    private void setPreference(String title, String summaryOn,
            String intentAction, String intentPackage, String intentClass,
            String key, String screenTitle) {
        Preference preference = new Preference(this);
        preference.setTitle(highlightSearchText(mQuery, title));
        preference.setSummary(highlightSearchText(mQuery, summaryOn));
        preference.setIntent(setPreferenceInentAction(intentAction,
                intentPackage, intentClass, key));
        if ("main".equals(screenTitle)) {
            preference.setIcon(MainIcon.getIconkForTitle(title));
        }
        preference.setOrder(0);
        PreferenceCategory preferenceCategory = (PreferenceCategory)mPreference
                .findPreference(screenTitle);
        preferenceCategory.addPreference(preference);
        mPreference.removePreference(findPreference(screenTitle));
        mPreference.addPreference(preferenceCategory);
    }

    private boolean checkPreferenceCategory(String screenTitle) {
        for (int i = 0; i < mPreferenceCategory.size(); i++) {
            if ("main".equals(screenTitle)) {
                if (mPreferenceCategory.get(i).getKey().equals(screenTitle)) {
                    return true;
                }
            } else {
                if (mPreferenceCategory.get(i).getTitle().equals(screenTitle)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static CharSequence highlightSearchText(String search,
            String originalText) {
        if (originalText == null) {
            return originalText;
        }
        String normalizedText = originalText.toLowerCase();

        int start = normalizedText.indexOf(search.toLowerCase());
        if (start < 0) {
            return originalText;
        } else {
            Spannable highlighted = new SpannableString(originalText);
            while (start >= 0) {
                int spanStart = Math.min(start, originalText.length());
                int spanEnd = Math.min(start + search.length(),
                        originalText.length());
                highlighted.setSpan(new ForegroundColorSpan(Utils.getResources().getColor(R.color.search_color)),
                        spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = normalizedText.indexOf(search.toLowerCase(), spanEnd);
            }
            return highlighted;
        }
    }

    private boolean checkIndexItemChecked(String dbTable, String dbField) {
        boolean isChecked = false;
        if ("System".equals(dbTable)) {
            isChecked = Settings.System
                    .getInt(getContentResolver(), dbField, 0) == 1 ? true
                    : false;
        } else if ("Secure".equals(dbTable)) {
            isChecked = Settings.Secure
                    .getInt(getContentResolver(), dbField, 0) == 1 ? true
                    : false;
        } else if ("Global".equals(dbTable)) {
            isChecked = Settings.Global
                    .getInt(getContentResolver(), dbField, 0) == 1 ? true
                    : false;
        }
        return isChecked;
    }

    private Intent setPreferenceInentAction(String action,
            String targetPackage, String targetClass, String key) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (targetPackage != null && targetClass != null) {
            ComponentName comp = new ComponentName(targetPackage, targetClass);
            intent.setComponent(comp);
        }
        intent.putExtra("search_item", key);
        intent.putExtra("perform", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    protected static class SearchResult {
        public Context context;
        public String title;
        public String screenTitle;
        public String summaryOn;
        public String summaryOff;
        public String entries;
        public int iconResId;
        public String key;
        public int currentEnable;
        public String preferenceType;
        public String intentAction;
        public String intentTargetClass;
        public String intentTargetPackage;
        public String settingsDBTableName;
        public String settingsDBField;
        public int visible;
        public int checkValue;

        public SearchResult(Context context, String title, String screenTitle,
                String summaryOn, String summaryOff, String entries,
                int iconResId, String key, int currentEnable,
                String preferenceType, String intentAction,
                String intentTargetClass, String intentTargetPackage,
                String settingsDBTableName, String settingsDBField,
                int visible, int checkValue) {
            this.context = context;
            this.title = title;
            this.screenTitle = screenTitle;
            this.summaryOn = summaryOn;
            this.summaryOff = summaryOff;
            this.entries = entries;
            this.iconResId = iconResId;
            this.key = key;
            this.currentEnable = currentEnable;
            this.preferenceType = preferenceType;
            this.intentAction = intentAction;
            this.intentTargetClass = intentTargetClass;
            this.intentTargetPackage = intentTargetPackage;
            this.settingsDBTableName = settingsDBTableName;
            this.settingsDBField = settingsDBField;
            this.visible = visible;
            this.checkValue = checkValue;
        }
    }

    private void setResultsCursor(Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    private void clearResults() {
        if (mUpdateSearchResultsTask != null) {
            mUpdateSearchResultsTask.cancel(false);
            mUpdateSearchResultsTask = null;
        }
        if (mSearchResult != null) {
            mSearchResult.clear();
        }
        if (mPreference != null) {
            mPreference.removeAll();
        }
        setResultsCursor(null);
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mDataValid = true;
        } else {
            mDataValid = false;
        }
        return oldCursor;
    }

    private Object updataeIndexItem(Cursor cursor) {
        if (mSearchResult != null) {
            mSearchResult.clear();
        }
        setResultsCursor(cursor);
        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
                .moveToNext()) {
            if (mDataValid) {
                final String title = mCursor
                        .getString(Index.COLUMN_INDEX_TITLE);
                final String screenTitle = mCursor
                        .getString(Index.COLUMN_INDEX_SCREEN_TITLE);
                final String summaryOn = mCursor
                        .getString(Index.COLUMN_INDEX_SUMMARY_ON);
                final String summaryOff = mCursor
                        .getString(Index.COLUMN_INDEX_SUMMARY_OFF);
                final String entries = mCursor
                        .getString(Index.COLUMN_INDEX_ENTRIES);
                final String iconResStr = mCursor
                        .getString(Index.COLUMN_INDEX_ICON);
                final String className = mCursor
                        .getString(Index.COLUMN_INDEX_CLASS_NAME);
                final String packageName = mCursor
                        .getString(Index.COLUMN_INDEX_INTENT_ACTION_TARGET_PACKAGE);
                final String key = mCursor.getString(Index.COLUMN_INDEX_KEY);
                final int currentEnable = mCursor
                        .getInt(Index.COLUMN_INDEX_CURRENT_ENABLE);
                final String preferenceType = mCursor
                        .getString(Index.COLUMN_INDEX_PREPERENCE_TYPE);
                final String intentAction = mCursor
                        .getString(Index.COLUMN_INDEX_INTENT_ACTION);
                final String intentTargetClass = mCursor
                        .getString(Index.COLUMN_INDEX_INTENT_ACTION_TARGET_CLASS);
                final String settingsDBTableName = mCursor
                        .getString(Index.COLUMN_INDEX_SETTINGS_DB_TABLE);
                final String settingsDBField = mCursor
                        .getString(Index.COLUMN_INDEX_SETTINGS_DB_FIELD);
                final int visible = mCursor.getInt(Index.COLUMN_INDEX_VISIBLE);
                final int checkValue = mCursor
                        .getInt(Index.COLUMN_INDEX_CHECK_VALUE);
                Context packageContext;
                if (TextUtils.isEmpty(className)
                        && !TextUtils.isEmpty(packageName)) {
                    packageContext = mContextMap.get(packageName);
                    if (packageContext == null) {
                        try {
                            packageContext = this.createPackageContext(
                                    packageName, 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.e(LOG_TAG,
                                    "Cannot create Context for package: "
                                            + packageName);
                            return null;
                        }
                        mContextMap.put(packageName, packageContext);
                    }
                } else {
                    packageContext = getApplicationContext();
                }

                final int iconResId = TextUtils.isEmpty(iconResStr) ? R.drawable.empty_icon
                        : Integer.parseInt(iconResStr);

                mSearchResult.add(new SearchResult(packageContext, title,
                        screenTitle, summaryOn, summaryOff, entries, iconResId,
                        key, currentEnable, preferenceType, intentAction,
                        intentTargetClass, packageName, settingsDBTableName,
                        settingsDBField, visible, checkValue));
            }
        }
        return null;
    }

    private void addNoResultDataPreference() {
        mPreference.removeAll();
        mEmptyLayout.setVisibility(View.VISIBLE);
        mEmptyText.setText(R.string.no_search_results);
    }

    public boolean onQueryTextSubmit(String query) {
        mQuery = getFilteredQueryString(query);
        updateSearchResults();
        return true;
    }

    public boolean onQueryTextChange(String query) {
        final String newQuery = getFilteredQueryString(query);

        mQuery = newQuery;

        if (TextUtils.isEmpty(mQuery)) {
            clearResults();
            mEmptyLayout.setVisibility(View.VISIBLE);
            mEmptyText.setText(R.string.sp_type_keyword_NORMAL);
        } else {
            updateSearchResults();
        }

        return true;
    }

    private String getFilteredQueryString(CharSequence query) {
        if (query == null) {
            return null;
        }
        final StringBuilder filtered = new StringBuilder();
        for (int n = 0; n < query.length(); n++) {
            char c = query.charAt(n);
            if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c)) {
                continue;
            }
            filtered.append(c);
        }
        return filtered.toString();
    }

    private void updateSearchResults() {
        clearAllTasks();
        if (TextUtils.isEmpty(mQuery)) {
            setResultsCursor(null);
        } else {
            mUpdateSearchResultsTask = new UpdateSearchResultsTask();
            mUpdateSearchResultsTask.execute(mQuery);
        }
    }

    private void clearAllTasks() {
        if (mUpdateSearchResultsTask != null) {
            mUpdateSearchResultsTask.cancel(false);
            mUpdateSearchResultsTask = null;
        }
    }

    @Override
    public boolean onClose() {
        return false;
    }
}
