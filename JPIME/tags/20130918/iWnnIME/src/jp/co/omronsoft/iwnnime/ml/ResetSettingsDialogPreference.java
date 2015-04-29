/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import com.lge.handwritingime.BaseHandwritingKeyboard;

/**
 * The preference class to reset setttings.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class ResetSettingsDialogPreference extends DialogPreference {
    /** Constructor.
      *
     * @param context Context
     * @param attrs AttributeSet
     */
    public ResetSettingsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Called when the dialog is dismissed and should be used to save data to the SharedPreferences.
     * 
     * @see android.preference.DialogPreference#onDialogClosed
     */
    @Override protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Context context = getContext();
            // copy preference key "preferenceId"
            OpenWnn.getIntFromNotResetSettingsPreference(context, DecoEmojiListener.PREF_KEY, -1);

            // copy preference key "dic_version"
            OpenWnn.getStringFromNotResetSettingsPreference(context, IWnnLanguageSwitcher.DICTIONARY_VERSION_KEY, "");

            // copy preference additional dic key
            for (int languageType = iWnnEngine.LanguageType.JAPANESE; languageType < iWnnEngine.LanguageType.COUNT_OF_LANGUAGETYPE; languageType++) {
                for (int index = 1; index <= AdditionalDictionaryPreferenceActivity.MAX_ADDITIONAL_DIC; index++) {
                    String key = AdditionalDictionaryPreferenceActivity.createAdditionalDictionaryKey(languageType, index);
                    OpenWnn.getStringFromNotResetSettingsPreference(context, key, null);
                }
            }

            // copy preference download dic key
            for (int index = 0; index < DownloadDictionaryListPreference.MAX_DOWNLOAD_DIC; index++) {
                OpenWnn.getStringFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_NAME), null);
                OpenWnn.getStringFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_FILE), null);
                OpenWnn.getIntFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_CONVERT_HIGH), 0);
                OpenWnn.getIntFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_CONVERT_BASE), 0);
                OpenWnn.getIntFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_PREDICT_HIGH), 0);
                OpenWnn.getIntFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_PREDICT_BASE), 0);
                OpenWnn.getIntFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_MORPHO_HIGH), 0);
                OpenWnn.getIntFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_MORPHO_BASE), 0);
                OpenWnn.getBooleanFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_CACHE_FLAG), false);
                OpenWnn.getIntFromNotResetSettingsPreference(context, DownloadDictionaryListPreference.createSharedPrefKey(index, DownloadDictionaryListPreference.QUERY_PARAM_LIMIT), 0);
            }

            boolean voiceDisp = true;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            if (!sharedPref.getBoolean(ControlPanelStandard.VOICE_SETTINGS_KEY, context.getResources().getBoolean(R.bool.voice_input_default_value))) {
                voiceDisp = false;
            }

            // clear all preference key.
            SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
            sharedPrefeditor.clear();
            sharedPrefeditor.commit();

            // show completed message.
            Toast.makeText(context, R.string.ti_dialog_reset_setting_done_txt, Toast.LENGTH_SHORT).show();

            // update PreferenceScreen.
            ControlPanelStandard conpane = ControlPanelStandard.getCurrentControlPanel();
            if (conpane != null) {
                conpane.setFlagDispVoiceDialog(voiceDisp);
                conpane.updatePreferenceScreen();
            }

            OpenWnn wnn = OpenWnn.getCurrentIme();
            if (wnn != null) {
                ((IWnnLanguageSwitcher)wnn).clearInitializedFlag();

                if (wnn.mOriginalInputMethodSwitcher.mInputMethodBase
                        instanceof BaseHandwritingKeyboard) {
                    SharedPreferences pref = 
                            PreferenceManager.getDefaultSharedPreferences(context);
                    KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
                    if (keyskin.getPackageManger() == null) {
                        keyskin.init(context);
                    }
                    keyskin.setPreferences(pref);

                    wnn.mOriginalInputMethodSwitcher.changeInputMethod(
                            InputMethodSwitcher.IME_TYPE_HANDWRITING);
                }
            }
        }
    }
}
