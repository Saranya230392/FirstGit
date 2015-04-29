/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml.hangul.ko;

import jp.co.omronsoft.iwnnime.ml.InputViewManager;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;
import jp.co.omronsoft.iwnnime.ml.hangul.IWnnImeHangul;

/**
 * The iWnn Chinese(TW) IME class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class IWnnImeKorean extends IWnnImeHangul {
    /**
     * Constructor
     *
     * @param wnn  IWnnLanguageSwitcher instance
     */
    public IWnnImeKorean(IWnnLanguageSwitcher wnn) {
        super(wnn);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeHangul#setDictionary */
    @Override protected void setDictionary() {
        int language = iWnnEngine.LanguageType.KOREAN;
        int dictionary = iWnnEngine.SetType.NORMAL;

        switch (mEngineState.preferenceDictionary) {
        case EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI:
            dictionary = iWnnEngine.SetType.EMAIL_ADDRESS;
            break;
        default:
            break;
        }
        mConverterIWnn.setConvertedCandidateEnabled(true);
        mConverterIWnn.setDictionary(language, dictionary, mWnnSwitcher.hashCode());
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeHangul#createInputViewManager */
    @Override protected InputViewManager createInputViewManager(IWnnLanguageSwitcher wnn) {
        return new DefaultSoftKeyboardKorean(wnn);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeHangul#getLanguage */
    @Override protected int getLanguage() {
        return iWnnEngine.LanguageType.KOREAN;
    }
}
