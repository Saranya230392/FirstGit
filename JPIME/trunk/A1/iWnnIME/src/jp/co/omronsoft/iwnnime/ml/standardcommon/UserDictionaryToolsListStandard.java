/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.standardcommon;

import android.content.Intent;
import android.os.Bundle;

import jp.co.omronsoft.iwnnime.ml.KeyboardLanguagePackData;
import jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsList;
import jp.co.omronsoft.iwnnime.ml.WnnWord;
//import jp.co.omronsoft.iwnnime.ml.cyrillic.ListComparatorCyrillicAlphabet;
import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnUserDictionaryToolsEngineInterface;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.hangul.ListComparatorHangul;
//import jp.co.omronsoft.iwnnime.ml.latin.ListComparatorLatin;
import jp.co.omronsoft.iwnnime.ml.R;
//import jp.co.omronsoft.iwnnime.ml.zh.ListComparatorZh;

import java.util.Comparator;


/**
 * The user dictionary tool class for Standard Keyboard IME
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class UserDictionaryToolsListStandard extends UserDictionaryToolsList {
    /** Current language */
    private static int mLanguage = iWnnEngine.LanguageType.NONE;

    /** constructor */
    public UserDictionaryToolsListStandard() {
        mEditViewName = "jp.co.omronsoft.iwnnime.ml.standardcommon.UserDictionaryToolsEditStandard";
        mPackageName  = "jp.co.omronsoft.iwnnime.ml";
    }

    /**
     * Called when the activity is starting.
     *
     * @see jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsList#onCreate
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String langtype = intent.getStringExtra("lang_type");
        mLanguage = LanguageManager.getChosenLanguageType(langtype);
        if (LanguageManager.isNoStroke(mLanguage)) {
            mIsNoStroke = true;
        }
        mEngineInterface = IWnnUserDictionaryToolsEngineInterface.getEngineInterface(mLanguage,
                                                                                     this.hashCode());
        mEngineInterface.setDirPath(getFilesDir().getPath());
        super.onCreate(savedInstanceState);
        setTitleByLanguage(mLanguage);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsList#getComparator */
    @Override protected Comparator<WnnWord> getComparator() {
        /* Switch a comparator for each language */
        switch(mLanguage) {
        /* for Cyrillic */
//        case iWnnEngine.LanguageType.RUSSIAN:
//            return new ListComparatorCyrillicAlphabet();

        /* for Bopomofo */
//        case iWnnEngine.LanguageType.TRADITIONAL_CHINESE:
//            return new ListComparatorZh();

        /* for Hangul */
        case iWnnEngine.LanguageType.KOREAN:
            return new ListComparatorHangul();

        default:
            return new ListComparatorHangul();//add
//            return new ListComparatorLatin();
        }
    }

    /**
     * Called when the activity is resume.
     *
     * @see jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsList#onResume
     */
    @Override protected void onResume() {
        super.onResume();
        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        if (langPack.getLocalLangPackClassName(this, mLanguage) == null) {
            // If the language pack in multi-language processing is uninstalled, the
            // Because you can not continue processing, you finish editing the user dictionary.
            finish();
        }
    }

    /**
     * Get the User dictionary language.
     *
     * @return  user dictionary language.
     */
    public static int getUserDicLanguage() {
        return mLanguage;
    }
}
