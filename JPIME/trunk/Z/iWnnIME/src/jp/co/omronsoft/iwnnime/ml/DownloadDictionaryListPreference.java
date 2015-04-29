/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * The preference screen of the Download dictionary.
 */
public class DownloadDictionaryListPreference extends DialogPreference {
    /** for DEBUG */
    private final static String TAG = "iWnn";

    /** Max number of Download dictionary. */
    public final static int MAX_DOWNLOAD_DIC = 10;

    /** The Download dictionary name key. */
    public final static String DOWNLOAD_DICTIONARY_NAME_KEY = "download_dic_name";

    /** The Download Dictionary action code */
    private static final String ACTION_INTENT_STRING = "jp.co.omronsoft.iwnnime.ml.DOWNLOAD_DICTIONARY";

    /** Content Provider URI. */
    private final static String CONTENT_PROVIDER_NAME_KEY = "providername";

    /** Column name of dictionary name. */
    public final static String QUERY_PARAM_NAME = "DICTIONARY_NAME";

    /** Column name of dictionary file path. */
    public final static String QUERY_PARAM_FILE = "DICTIONARY_FILE";

    /** Column name of convert high. */
    public final static String QUERY_PARAM_CONVERT_HIGH = "CONVERT_HIGH";

    /** Column name of convert base. */
    public final static String QUERY_PARAM_CONVERT_BASE = "CONVERT_BASE";

    /** Column name of predict high. */
    public final static String QUERY_PARAM_PREDICT_HIGH = "PREDICT_HIGH";

    /** Column name of predict base. */
    public final static String QUERY_PARAM_PREDICT_BASE = "PREDICT_BASE";

    /** Column name of morpho high. */
    public final static String QUERY_PARAM_MORPHO_HIGH = "MORPHO_HIGH";

    /** Column name of morpho base. */
    public final static String QUERY_PARAM_MORPHO_BASE = "MORPHO_BASE";

    /** Column name of cache flag. */
    public final static String QUERY_PARAM_CACHE_FLAG = "CACHE_FLAG";

    /** Column name of cache limit. */
    public final static String QUERY_PARAM_LIMIT = "LIMIT";

    /** Column name of cache providername. */
    public final static String QUERY_PARAM_PROVIDERNAME = "PROVIDERNAME";

    /** Column name of cache packagename. */
    public final static String QUERY_PARAM_PACKAGENAME = "PACKAGENAME";

    /** Loaded download dictionary list. */
    private ArrayList<DownloadDictionary> mDownloadDictionaries = null;
    
    /** The checked items */
    private boolean mChecked[] = null;

    /** The checked count */
    private int mCheckCnt = 0;

    /** The DownloadDictionaryAsyncTask object */
    private DownloadDictionaryAsyncTask mProgressAsyncTask = null;

    /** The DownloadDictionaryListPreference object */
    public static DownloadDictionaryListPreference mDialogPref = null;

    /** List of ContentProviderName  */
    private static ArrayList<String> mContentProviderUriList;

    /**
     * Information of the download dictionary.
     */
    public static class DownloadDictionary implements Comparable<DownloadDictionary> {

        /** this dictionary is enable or disable */
        private boolean mEnable;

        /** dictionary name */
        private String mName;

        /** dictionary file path */
        private String mFile;

        /** convert high */
        private int mConvertHigh;

        /** convert base */
        private int mConvertBase;

        /** predict high */
        private int mPredictHigh;

        /** predict base */
        private int mPredictBase;

        /** morpho high */
        private int mMorphoHigh;

        /** morpho base */
        private int mMorphoBase;

        /** cache enable or disable */
        private boolean mCache;

        /** number of limit */
        private int mLimit;

        /** Provider Name */
        private String mProviderName;
        
        /** Package Name */
        private String mPackageName;

        /**
         * Constructor.
         * @param name name of download dictionary
         * @param file file path of download dictionary
         * @param convertHigh value of convert high
         * @param convertBase value of convert base
         * @param predictHigh value of predict high
         * @param predictBase value of predict base
         * @param morphoHigh value of morpho high
         * @param morphoBase value of morpho base
         * @param cache value of cache
         * @param limit value of limit
         */
        public DownloadDictionary(String name, String file, int convertHigh, int convertBase,int predictHigh,
                int predictBase, int morphoHigh, int morphoBase, boolean cache, int limit, 
                String providerName, String packageName) {
            mEnable = false;
            mName = name;
            mFile = file;
            mConvertHigh = convertHigh;
            mConvertBase = convertBase;
            mPredictHigh = predictHigh;
            mPredictBase = predictBase;
            mMorphoHigh = morphoHigh;
            mMorphoBase = morphoBase;
            mCache = cache;
            mLimit = limit;
            mProviderName = providerName;
            mPackageName = packageName;
        }

        /**
         * Return name.
         * @return name.
         */
        public String name() {
            return mName;
        }

        /**
         * Return file path.
         * @return file.
         */
        public String file() {
            return mFile;
        }

        /** @see java.lang.Comparable#compareTo */
        public int compareTo(DownloadDictionary other) {
            return mFile.compareTo(other.mFile);
        }

        /** @see java.lang.Object#equals */
        @Override public boolean equals(Object other) {
            if (other instanceof DownloadDictionary) {
                return mFile.equals(((DownloadDictionary)other).mFile);
            } else {
                return false;
            }
        }

        /** @see java.lang.Object#hashCode */
        @Override public int hashCode() {
            return mFile.hashCode();
        }

        /**
         * Set enable dictionary.
         */
        public void enable() {
            mEnable = true;
        }

        /**
         * Set disable dictionary.
         */
        public void disable() {
            mEnable = false;
        }

        /**
         * Return enable/disable dictionary.
         * @return true if dictionary is enable, either false if dictionary is disable.
         */
        public boolean isEnabled() {
            return mEnable;
        }

        /**
         * Return value of convert high.
         * @return convert high.
         */
        public int convertHigh() {
            return mConvertHigh;
        }

        /**
         * Return value of convert base.
         * @return convert base.
         */
        public int convertBase() {
            return mConvertBase;
        }

        /**
         * Return value of predict high.
         * @return predict high.
         */
        public int predictHigh() {
            return mPredictHigh;
        }

        /**
         * Return value of predict base.
         * @return predict base.
         */
        public int predictBase() {
            return mPredictBase;
        }

        /**
         * Return value of morpho high.
         * @return morpho high.
         */
        public int morphoHigh() {
            return mMorphoHigh;
        }

        /**
         * Return value of morpho base.
         * @return morpho base.
         */
        public int morphoBase() {
            return mMorphoBase;
        }

        /**
         * Return value of cache.
         * @return cache.
         */
        public boolean cache() {
            return mCache;
        }

        /**
         * Return number of limit.
         * @return limit.
         */
        public int limit() {
            return mLimit;
        }

        /**
         * Return String of providername.
         * @return limit.
         */
        public String providername() {
            return mProviderName;

        }
        /**
         * Return String of packageName.
         * @return limit.
         */
        public String packageName() {
            return mPackageName;
        }

    }

    /**
     * Constructor
     * 
     * @param context the Context
     * @param attrs the AttributeSet
     */
    public DownloadDictionaryListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogPref = this;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.MultiChoicePreference#onPrepareDialogBuilder */
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        ArrayList<DownloadDictionary> list = getDownloadDictionary(getContext());
        int size = list.size();
        mCheckCnt = 0;
        mChecked = new boolean[size];
        CharSequence[] entries = null;
        if (size > 0) {
            entries = new CharSequence[size];
            for (int i = 0; i < size; i++) {
                entries[i] = list.get(i).name();
                for (int cnt = 0; cnt < MAX_DOWNLOAD_DIC; cnt++) {
                    String file = OpenWnn.getStringFromNotResetSettingsPreference(getContext(), createSharedPrefKey(cnt, QUERY_PARAM_FILE), null);
                    if (file != null && file.equals(list.get(i).file())) {
                        mChecked[i] = true;
                        mCheckCnt++;
                        break;
                    }
                }
            }
        }

        builder.setMultiChoiceItems(entries, mChecked, new OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                onMultiChoiceItemsClick(which, isChecked);
            }
        });
    }

    public void onMultiChoiceItemsClick(int which, boolean isChecked) {
        Button positiveButton = (Button) (getDialog().findViewById(android.R.id.button1));
        if (positiveButton != null) {
            if (getCheckedCount() > MAX_DOWNLOAD_DIC) {
                if (isChecked) {
                    Toast.makeText(getContext(), R.string.ti_preference_download_dic_max_error_txt, Toast.LENGTH_SHORT).show();
                    positiveButton.setEnabled(false);
                }
            } else {
                positiveButton.setEnabled(true);
            }
        }
        if (mChecked != null) {
            mChecked[which] = isChecked;
        }
    }

    /** @see android.preference.DialogPreference#onPrepareDialogBuilder */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Preference preference = new Preference(getContext());
        if (positiveResult) {
            SharedPreferences sharedPref = getSharedPreferences();
            if (sharedPref == null) {
                return;
            }

            for (int num = 0; num < mCheckCnt; num++) {
                preference.setOrder(num + 1);
            }

            mProgressAsyncTask = new DownloadDictionaryAsyncTask(getContext(), preference, mDownloadDictionaries, mChecked);
            mProgressAsyncTask.execute();
        }
    }

    /**
     * Return whether the download dictionary is enabled.
     * @param context Context
     * @return {@code true} if enable.
     */
    public static void setContenteProviderUriList(Context context) {
        mContentProviderUriList = new ArrayList<String>();
        PackageManager pm = context.getPackageManager();
        Intent connectorIntent = new Intent(ACTION_INTENT_STRING);
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(connectorIntent, 0);
        Collections.sort(resolveInfo, new ResolveInfo.DisplayNameComparator(pm));

        for(int i = 0; i <  resolveInfo.size(); i++) {
            ResolveInfo info = resolveInfo.get(i);
            ActivityInfo actInfo = info.activityInfo;
            String  classname  = actInfo.name;
            String  packagename  = actInfo.packageName;
            ComponentName name = new ComponentName(packagename, classname);
            try {
                ActivityInfo metadata  = pm.getActivityInfo(name, PackageManager.GET_META_DATA);
                String cpname = metadata.metaData.getString(CONTENT_PROVIDER_NAME_KEY);
                mContentProviderUriList.add("content://" + cpname);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return whether the download dictionary is enabled.
     * @param context Context
     * @return {@code true} if enable.
     */
    public static boolean isEnableDownloadDictionary(Context context) {
        setContenteProviderUriList(context);
        ArrayList<DownloadDictionary> cp = getDictionaryFromContentProvider(context);
        ArrayList<DownloadDictionary> fp = checkDictionaryFilePath(cp);
        ArrayList<DownloadDictionary> sp = readDownloadDictionaryFromSharedPreferences(context);

        ArrayList<DownloadDictionary> dictionary = checkConsistency(fp, sp, context);

        if (dictionary.size() < 1) {
            return false;
        }

        return true;
    }

    /**
     * Get download dictionaries from preferences.
     * @param context  context in order to get shared preferences.
     * @return ArrayList  download dictionaries.
     */
    public static ArrayList<DownloadDictionary> readDownloadDictionaryFromSharedPreferences(Context context) {
        ArrayList<DownloadDictionary> dicArray = new ArrayList<DownloadDictionary>();
        for (int i = 0; i < MAX_DOWNLOAD_DIC; i++) {
            if (OpenWnn.getStringFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_FILE), null) != null) {
                DownloadDictionary dictionary = new DownloadDictionary(
                        OpenWnn.getStringFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_NAME), null),
                        OpenWnn.getStringFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_FILE), null),
                        OpenWnn.getIntFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_CONVERT_HIGH), 0),
                        OpenWnn.getIntFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_CONVERT_BASE), 0),
                        OpenWnn.getIntFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_PREDICT_HIGH), 0),
                        OpenWnn.getIntFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_PREDICT_BASE), 0),
                        OpenWnn.getIntFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_MORPHO_HIGH), 0),
                        OpenWnn.getIntFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_MORPHO_BASE), 0),
                        OpenWnn.getBooleanFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_CACHE_FLAG), false),
                        OpenWnn.getIntFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_LIMIT), 0),
                        OpenWnn.getStringFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_PROVIDERNAME), null),
                        OpenWnn.getStringFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_PACKAGENAME), null));
                dictionary.enable();
                dicArray.add(dictionary);
            }
        }
        return dicArray;
    }

    /**
     * Write download dictionaries to shared preferences.
     * At first, delete all dictionaries. Then, write all available dictionaries.
     * @param dicArray  dictionaries To set shared preference and dictionary of iWnnEngine.
     * @param context  context in order to get shared preferences.
     */
    public static boolean writeDownloadDictionaryToSharedPreferences(ArrayList<DownloadDictionary> dicArray, Context context) {
        removeAllDownloadDictionaryFromSharedPreferences(context);

        SharedPreferences sharedPref = context.getSharedPreferences(OpenWnn.FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Iterator<DownloadDictionary> it = dicArray.iterator();
        int i = 0;
        while (it.hasNext()) {
            DownloadDictionary dictionary = it.next();
            if (dictionary.isEnabled()) {
                editor.putString(createSharedPrefKey(i, QUERY_PARAM_NAME), dictionary.name());
                editor.putString(createSharedPrefKey(i, QUERY_PARAM_FILE), dictionary.file());
                editor.putInt(createSharedPrefKey(i, QUERY_PARAM_CONVERT_HIGH), dictionary.convertHigh());
                editor.putInt(createSharedPrefKey(i, QUERY_PARAM_CONVERT_BASE), dictionary.convertBase());
                editor.putInt(createSharedPrefKey(i, QUERY_PARAM_PREDICT_HIGH), dictionary.predictHigh());
                editor.putInt(createSharedPrefKey(i, QUERY_PARAM_PREDICT_BASE), dictionary.predictBase());
                editor.putInt(createSharedPrefKey(i, QUERY_PARAM_MORPHO_HIGH), dictionary.morphoHigh());
                editor.putInt(createSharedPrefKey(i, QUERY_PARAM_MORPHO_BASE), dictionary.morphoBase());
                editor.putBoolean(createSharedPrefKey(i, QUERY_PARAM_CACHE_FLAG), dictionary.cache());
                editor.putInt(createSharedPrefKey(i, QUERY_PARAM_LIMIT), dictionary.limit());
                editor.putString(createSharedPrefKey(i, QUERY_PARAM_PROVIDERNAME), dictionary.providername());
                editor.putString(createSharedPrefKey(i, QUERY_PARAM_PACKAGENAME), dictionary.packageName());
                i++;
            }
        }
        editor.commit();
        return setDownloadDictionary(context);
    }

    /**
     * Delete download dictionaries to shared preferences.
     * @param context  context in order to get shared preferences.
     */
    private static void removeAllDownloadDictionaryFromSharedPreferences(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(OpenWnn.FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor defaultEditor = defaultPref.edit();
        for (int i = 0; i < MAX_DOWNLOAD_DIC; i++) {
            if (OpenWnn.getStringFromNotResetSettingsPreference(context, createSharedPrefKey(i, QUERY_PARAM_FILE), null) != null) {
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_NAME));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_FILE));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_CONVERT_HIGH));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_CONVERT_BASE));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_PREDICT_HIGH));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_PREDICT_BASE));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_MORPHO_HIGH));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_MORPHO_BASE));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_CACHE_FLAG));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_LIMIT));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_PROVIDERNAME));
                editor.remove(createSharedPrefKey(i, QUERY_PARAM_PACKAGENAME));
            }
            // corresponding compatibility when upgrading to v2.3 from v2.2.6.
            if (defaultPref.getString(createSharedPrefKey(i, QUERY_PARAM_FILE), null) != null) {
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_NAME));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_FILE));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_CONVERT_HIGH));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_CONVERT_BASE));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_PREDICT_HIGH));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_PREDICT_BASE));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_MORPHO_HIGH));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_MORPHO_BASE));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_CACHE_FLAG));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_LIMIT));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_PROVIDERNAME));
                defaultEditor.remove(createSharedPrefKey(i, QUERY_PARAM_PACKAGENAME));
            }
        }
        editor.commit();
        setDownloadDictionary(context);
    }

    /**
     * Generate shared preference key of download dictionary.
     * @param index  index of dictionary array.
     * @param key  column name key.
     * @return String  download dictionary key.
     */
    public static String createSharedPrefKey(int index, String key) {
        return new StringBuilder(DOWNLOAD_DICTIONARY_NAME_KEY).append(".").append(index).
                append("#").append(key).toString();
    }

    /**
     * Return download dictionaries from Content Provider, shared preferences.
     * 1. get from Content Provider.
     * 2. check exists dictionary file.
     * 3. get from shared preferences.
     * 4. check consistency of 1, 2 and 3.
     * @param context  context in order to get shared preferences.
     * @return dicArray  dictionaries To set shared preference and dictionary of iWnnEngine.
     */
    public ArrayList<DownloadDictionary> getDownloadDictionary(Context context) {
        ArrayList<DownloadDictionary> cp = getDictionaryFromContentProvider(context);
        ArrayList<DownloadDictionary> fp = checkDictionaryFilePath(cp);
        ArrayList<DownloadDictionary> sp = readDownloadDictionaryFromSharedPreferences(context);

        ArrayList<DownloadDictionary> dictionary = checkConsistency(fp, sp, context);

        mDownloadDictionaries = dictionary;

        return dictionary;
    }

    /**
     * Return download dictionaries from Content Provider.
     * @param context  context in order to get shared preferences.
     * @return dicArray  dictionaries from Content Provider.
     */
    public static ArrayList<DownloadDictionary> getDictionaryFromContentProvider(Context context) {
        ArrayList<DownloadDictionary> dicList = new ArrayList<DownloadDictionary>();
        int urlSize = mContentProviderUriList.size();
        for (int i = 0;i < urlSize ; i ++) {
            Cursor cursor = context.getContentResolver().query(Uri.parse(mContentProviderUriList.get(i)),
                            new String[]{}, "", new String[]{}, ""); 
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String dictionaryName = cursor.getString(cursor.getColumnIndexOrThrow(QUERY_PARAM_NAME));
                        String dictionaryFile = cursor.getString(cursor.getColumnIndexOrThrow(QUERY_PARAM_FILE));
                        int convertHigh = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_PARAM_CONVERT_HIGH));
                        int convertBase = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_PARAM_CONVERT_BASE));
                        int predictHigh = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_PARAM_PREDICT_HIGH));
                        int predictBase = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_PARAM_PREDICT_BASE));
                        int morphoHigh = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_PARAM_MORPHO_HIGH));
                        int morphoBase = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_PARAM_MORPHO_BASE));
                        boolean cacheFlag = cursor.getInt(
                                cursor.getColumnIndexOrThrow(QUERY_PARAM_CACHE_FLAG)) == 0 ? false : true;
                        int limit = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_PARAM_LIMIT));

                        if (dictionaryName!= null && dictionaryName.length() != 0
                                && dictionaryFile != null && dictionaryFile.length() != 0) {
                            String[] packagename = mContentProviderUriList.get(i).split("content://");
                            dicList.add(new DownloadDictionary(dictionaryName, dictionaryFile, convertHigh,
                                    convertBase, predictHigh, predictBase, morphoHigh, morphoBase, cacheFlag, limit,
                                    mContentProviderUriList.get(i), packagename[1]));
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error occured querying Content Provider.", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return dicList;
    }

    /**
     * Check exists dictionary files.
     * Return download dictionaries which exclude files that is not exist.
     * @param cpDictionary  dictionaries from Content Provider.
     * @return retDic  dictionaries exclude files that is not exist.
     */
    public static ArrayList<DownloadDictionary> checkDictionaryFilePath(ArrayList<DownloadDictionary> cpDictionary) {
        DownloadDictionary dic;
        ArrayList<DownloadDictionary> retDic = new ArrayList<DownloadDictionary>();
        Iterator<DownloadDictionary> it = cpDictionary.iterator();
        while (it.hasNext()) {
            dic = it.next();
            if (new File(dic.file()).exists()) {
                retDic.add(dic);
            }
        }
        return retDic;
    }

    /**
     * Check consistency of Content Provider and shared preferences.
     *
     * @param contentProv dictionaries from Content Provider.
     * @param sharedPref dictionaries from shared preferences.
     * @param context  context in order to get shared preferences.
     * @return consisted dictionaries.
     */
    public static ArrayList<DownloadDictionary> checkConsistency(
            ArrayList<DownloadDictionary> contentProv,
            ArrayList<DownloadDictionary> sharedPref, Context context) {
        DownloadDictionary contDic;
        DownloadDictionary prefDic;
        Iterator<DownloadDictionary> contIt;
        Iterator<DownloadDictionary> prefIt;

        prefIt = sharedPref.iterator();
        while (prefIt.hasNext()) {
            prefDic = prefIt.next();
            boolean match = false;
            contIt = contentProv.iterator();
            while (contIt.hasNext()) {
                contDic = contIt.next();
                if (prefDic.equals(contDic)) {
                    match = true;
                }
            }
            if (!match) {
                prefDic.disable();
                try {
                    context.getContentResolver().delete(Uri.parse(prefDic.mProviderName),
                            new StringBuilder(QUERY_PARAM_FILE).append("=?").toString(),
                            new String[]{prefDic.file()}); 
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error occured deleting Content Provider.", e);
                }
                writeDownloadDictionaryToSharedPreferences(sharedPref, context);
            }
        }

        contIt = contentProv.iterator();
        while (contIt.hasNext()) {
            contDic = contIt.next();
            contDic.disable();
            prefIt = sharedPref.iterator();
            while (prefIt.hasNext()) {
                prefDic = prefIt.next();
                if (contDic.equals(prefDic)) {
                    contDic.enable();
                    break;
                }
            }
        }

        return contentProv;
    }

    /**
     * Set download dictionary config to temporary area in native layer.
     *
     * @param context     Context using to get shared preferences
     * @return true if setting dictionary succeed.
     */
    public static boolean setDownloadDictionary(Context context) {
        ArrayList<DownloadDictionary> dicArray = readDownloadDictionaryFromSharedPreferences(context);
        Iterator<DownloadDictionary> ite = dicArray.iterator();
        int index = 0;
        while (ite.hasNext()) {
            DownloadDictionary dic = ite.next();
            iWnnEngine.getEngine().setDownloadDictionary(
                    index++,
                    dic.name(),
                    dic.file(),
                    dic.convertHigh(),
                    dic.convertBase(),
                    dic.predictHigh(),
                    dic.predictBase(),
                    dic.morphoHigh(),
                    dic.morphoBase(),
                    dic.cache(),
                    dic.limit()
            );
        }
        for (int i = index; i < DownloadDictionaryListPreference.MAX_DOWNLOAD_DIC; i++) {
            iWnnEngine.getEngine().setDownloadDictionary(i, null, null, 0, 0, 0, 0, 0, 0, false, 0);
        }

        return iWnnEngine.getEngine().refreshConfFile();
    }

    /**
     * Get the count of checked items.
     * 
     * @return The count of checked items.
     */
    public int getCheckedCount() {
        int checkedCount = 0;
        if (mChecked != null) {
            for (int i = 0; i < mChecked.length; i++) {
                if (mChecked[i]) {
                    checkedCount++;
                }
            }
        }
        return checkedCount;
    }
}
