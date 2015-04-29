/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.DownloadDictionaryListPreference.DownloadDictionary;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;

import java.io.File;
import java.util.ArrayList;


/**
 * The preference class of DownloadDictionary settings AsyncTask.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class DownloadDictionaryAsyncTask extends AsyncTask<Void, Integer, String> {
    /** for DEBUG */
    private final static String TAG = "iWnn";
    /** Context */
    private Context mContext = null;
    /** Preference */
    Preference mPref = null;
    /** Loaded download dictionary list. */
    private ArrayList<DownloadDictionary> mDictionaries = null;
    /** The checked items */
    private boolean mChecked[] = null;
    /** The progress dialog */
    private ProgressDialog mProgressDialog = null;
    /** The maximum value of progress */
    private int mMax = 0;
    /** The success count */
    private int mSuccessCount = 0;

    /**
     * Constructor
     * 
     * @param context      the Context.
     * @param pref         the Preference.
     * @param dictionaries array list of download dictionaries.
     * @param checked      checked items.
     */
    public DownloadDictionaryAsyncTask(Context context, Preference pref, ArrayList<DownloadDictionary> dictionaries, boolean[] checked) {
        mContext = context;
        mPref = pref;
        mDictionaries = new ArrayList(dictionaries);
        mChecked = checked;
    }

    /**
     * Runs on the UI thread before doInBackground(Params...).
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        int cnt = 0;
        for (int i = 0; i < mChecked.length; i++) {
            if (mChecked[i]) {
                cnt++;
            }
        }
        mSuccessCount = 0;
        mMax = cnt;
        iWnnEngine engine = iWnnEngine.getEngine();
        engine.setFilesDirPath(mContext.getFilesDir().getPath());
        if (cnt > 0) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(R.string.ti_preference_download_dic_title_txt);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(mContext.getString(
                    R.string.ti_preference_download_dic_load_process_message_txt, 0, mMax));
            mProgressDialog.setMax(mMax);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        iWnnEngine engine = iWnnEngine.getEngine();
        if (OpenWnn.getCurrentIme() == null) {
            engine.init(OpenWnn.getFilesDirPath(mContext));
        }

        for (int i = 0; i < mChecked.length; i++) {
            if (isCancelled()) {
               break;
            }
            mPref.setOrder(mSuccessCount + 1);
            DownloadDictionary dic = mDictionaries.get(i);
            if (mChecked[i]) {
                boolean isSuccess = false;
                if (new File(dic.file()).exists()) {
                    isSuccess = true;
                    if (!dic.isEnabled()) {
                        isSuccess = enableDictionary(dic, mDictionaries);
                    }
                }
                if (isSuccess) {
                    mSuccessCount++;
                    publishProgress(mSuccessCount);
                }
            } else {
                if (dic.isEnabled()) {
                    disableDictionary(dic, mDictionaries);
                }
            }
        }
        return null;
    }

    /**
     * Runs on the UI thread after publishProgress(Progress...) is invoked.
     * 
     * @see android.os.AsyncTask#onProgressUpdate()
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mProgressDialog != null && values.length > 0) {
            mProgressDialog.setMessage(mContext.getString(
                    R.string.ti_preference_download_dic_load_process_message_txt, values[0], mMax));
            mProgressDialog.setProgress(values[0]);
        }
    }

    /** @see android.os.AsyncTask#onCancelled() */
    @Override
    protected void onCancelled() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mMax > 0) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.ti_preference_load_comp_message_txt, mSuccessCount, mMax), Toast.LENGTH_LONG).show();
        }
        mSuccessCount = 0;
    }

    /** @see android.os.AsyncTask#onPostExecute() */
    @Override
    protected void onPostExecute(String result) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog =null;
        }

        if (mMax > 0) {
            Toast.makeText(mContext,
                    mContext.getString(R.string.ti_preference_load_comp_message_txt, mSuccessCount, mMax), Toast.LENGTH_LONG).show();
        }
        mSuccessCount = 0;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = sharedPref.edit();
        long currentTimeMillis = System.currentTimeMillis();
        editor.putLong(ControlPanelStandard.DOWNLOAD_DICTIONARY_UPDATE_KEY, currentTimeMillis);
        editor.commit();
    }

    /**
     * Set dictionary enable, write shared preference and set dictionary of iWnnEngine.
     * 
     * @param dictioary  enable dictionary in Java layer.
     * @param dicArray  dictionaries To set shared preference and dictionary of iWnnEngine.
     * @return true  delete dictionary succeed.
     */
    private boolean enableDictionary(DownloadDictionary dictionary, ArrayList<DownloadDictionary> dicArray) {
        boolean ret = false;
        if (new File(dictionary.file()).exists()) {
            dictionary.enable();
            ret = DownloadDictionaryListPreference.writeDownloadDictionaryToSharedPreferences(dicArray, mContext);
            iWnnEngine.getEngine().setUpdateDownloadDictionary(true);
            iWnnEngine.getEngine().setDictionary(mContext);
        }
        return ret;
    }

    /**
     * Set dictionary disable, write shared preference and set dictionary of iWnnEngine.
     * 
     * @param dictioary  disable dictionary in Java layer.
     * @param dicArray  dictionaries To set shared preference and dictionary of iWnnEngine.
     * @return true  delete dictionary succeed.
     */
    private boolean disableDictionary(DownloadDictionary dictionary, ArrayList<DownloadDictionary> dicArray) {
        boolean ret = false;
        if (new File(dictionary.file()).exists()) {
            dictionary.disable();
            ret = DownloadDictionaryListPreference.writeDownloadDictionaryToSharedPreferences(dicArray, mContext);
            iWnnEngine.getEngine().setUpdateDownloadDictionary(true);
            iWnnEngine.getEngine().setDictionary(mContext);
        }
        return ret;
    }
}
