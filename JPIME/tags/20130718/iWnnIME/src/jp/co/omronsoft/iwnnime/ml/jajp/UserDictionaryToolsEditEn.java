/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.jajp;

import android.os.Bundle;

import jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsEdit;
import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnUserDictionaryToolsEngineInterface;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;

/**
 * User dictionary's word editor for Japanese IME in English mode.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class UserDictionaryToolsEditEn extends UserDictionaryToolsEdit {
    /** constructor */
    public UserDictionaryToolsEditEn() {
        super();
        int language = iWnnEngine.LanguageType.ENGLISH;
        int hashCode = this.hashCode();
        mEngineInterface = IWnnUserDictionaryToolsEngineInterface.getEngineInterface(language,
                                                                                     hashCode);
    }
    
    /**
     * Called when the activity is starting.
     * 
     * @see jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsEdit#onCreate
     */
    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        mReadEditText.setPrivateImeOptions(IWnnImeJaJp.DEFAULT_KEYMODE_EIJI_ONLY_STR);
    }
}
