/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

import jp.co.omronsoft.iwnnime.ml.KeyboardLanguagePackData;
import jp.co.omronsoft.iwnnime.ml.DownloadDictionaryListPreference;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;
import jp.co.omronsoft.iwnnime.ml.standardcommon.LanguageManager;

/**
 * The Uninstall BroadcastReceiver Class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class UninstallReceiver extends BroadcastReceiver {

    /** Key of "Keyboard Image" setting. */
    private static final String KEYBOARD_IMAGE_KEY = "keyboard_skin_add";

    /** Key of "Additional Symbol List" setting. */
    private static final String ADD_SYMBOL_LIST_KEY = "opt_add_symbol_list";

    /** Key of "WebAPI" setting. */
    private static final String WEBAPI_KEY = "opt_multiwebapi";

    /** Language package core name */
    private static final String LANGPACK_CORE_NAME = "jp.co.omronsoft.iwnnime.languagepack";


    /** @see android.content.BroadcastReceiver#onReceive */
    @Override public void onReceive(Context context,Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            return;
        }
        String uninstallPackageName = uri.getSchemeSpecificPart();
        if (uninstallPackageName == null) {
            return;
        }

        DownloadDictionaryListPreference.isEnableDownloadDictionary(context);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String classname = pref.getString(KEYBOARD_IMAGE_KEY,"");
        if (!classname.equals("")) {
            String packagename = classname.substring(0, classname.lastIndexOf('.'));
            if (uninstallPackageName.equals(packagename)) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(KEYBOARD_IMAGE_KEY, "");
                editor.commit();

                restartIme();
            }
        }

        Set<String> packageNames = pref.getStringSet(ADD_SYMBOL_LIST_KEY, null);
        if (packageNames != null) {
            if (packageNames.contains(uninstallPackageName)) {
                SharedPreferences.Editor editor = pref.edit();
                editor.remove(ADD_SYMBOL_LIST_KEY);
                editor.commit();

                packageNames.remove(uninstallPackageName);
                editor.putStringSet(ADD_SYMBOL_LIST_KEY, packageNames);
                editor.commit();

                restartIme();
            }
        }

        Set<String> classNames = pref.getStringSet(WEBAPI_KEY, null);
        if (classNames != null) {
            Iterator<String> iterator = classNames.iterator();
            boolean isModified = false;
            while (iterator.hasNext()) {
                String className = iterator.next();
                String packageName = className.substring(0, className.lastIndexOf('.'));
                if (uninstallPackageName.equals(packageName)) {
                    iterator.remove();
                    isModified = true;
                }
            }

            if (isModified) {
                SharedPreferences.Editor editor = pref.edit();
                editor.remove(WEBAPI_KEY);
                editor.commit();

                editor.putStringSet(WEBAPI_KEY, classNames);
                editor.commit();

                restartIme();
            }
        }

        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        String uninstallPackCoreName = uninstallPackageName.substring(0, uninstallPackageName.lastIndexOf('.'));
        if (uninstallPackCoreName.equals(LANGPACK_CORE_NAME)) {
            // uninstall apk is language pack.
            langPack.reset();
            langPack.setInputMethodSubtypeInstallLangPack(context);
            // To update the system has been retained, the configuration information of the InputMethodSubtype.
            // Start "com.android.settings.inputmethod.InputMethodAndSubtypeEnablerActivity".
            InputMethodInfo imi = langPack.getMyselfInputMethodInfo(context);
            if (imi != null ) {
                int subTypeCount = imi.getSubtypeCount();
                for (int i = 0; i < subTypeCount; i++) {
                    InputMethodSubtype subtype = imi.getSubtypeAt(i);
                    if (subtype != null && subtype.getExtraValue().equals(uninstallPackageName)) {
                        subTypeCount -= 1;
                     }
                 }
                if (subTypeCount > 1) {
                    if (OpenWnn.getCurrentIme() == null) {
                        int langtype = LanguageManager.getChosenLanguageType(pref);
                        String prefLangPackClassName = langPack.getLocalLangPackClassName(context, langtype);
                        if (prefLangPackClassName == null) {
                            IWnnLanguageSwitcher.updateChoosedLanguageForcing(KeyboardLanguagePackData.DEFAULT_LANG, context);
                        }
                    }
                    intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.inputmethod.InputMethodAndSubtypeEnablerActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(android.provider.Settings.EXTRA_INPUT_METHOD_ID, imi.getId());
                    context.startActivity(intent);
                    Toast.makeText(context, R.string.ti_uninstall_languagepack_txt, Toast.LENGTH_LONG).show();
                } else {
                    // Now that one subtype, switch to the Japanese.
                    // For setting normally does not change even if the setting screen is displayed.
                    // (Seems to be a bug in Android)
                    OpenWnn wnn = OpenWnn.getCurrentIme();
                    if (wnn instanceof IWnnLanguageSwitcher) {
                        int languageType = LanguageManager.getChosenLanguageType(KeyboardLanguagePackData.DEFAULT_LANG);
                        iWnnEngine engine = iWnnEngine.getEngine();
                        ((IWnnLanguageSwitcher)wnn).setLanguage(engine.getLocale(languageType),
                                                                languageType, true, true);
                    }
                    IWnnLanguageSwitcher.updateChoosedLanguageForcing(KeyboardLanguagePackData.DEFAULT_LANG, context);
                    Toast.makeText(context, R.string.ti_all_uninstall_languagepack_txt, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Restarts the IME.
     */
    private void restartIme() {
        OpenWnn wnn = OpenWnn.getCurrentIme();
        OpenWnnEvent ev = new OpenWnnEvent(OpenWnnEvent.CHANGE_INPUT_VIEW);
        try {
            wnn.requestHideSelf(0);
            wnn.onEvent(ev);
        } catch (Exception ex) {
            Log.e("OpenWnn", "UninstallReceiver::restartIme " + ex.toString());
        }
    }
}
