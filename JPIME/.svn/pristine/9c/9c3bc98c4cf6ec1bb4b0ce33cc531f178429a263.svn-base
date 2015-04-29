/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;

/**
 * iWnn IM Class iWnnEngine
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class WrapWnnEngine extends iWnnEngine {

    /**Dictionary Language */
    /** Japanese */
    public static final int JAPANESE = 0;
    /** English */
    public static final int ENGLISH = 1;
    /** Korean */
    public static final int KOREAN = 18;

    /** Type of Dictionary (Set No) */
    /** None */
    public static final int NONE = -1;
    /** Normal */
    public static final int NORMAL = 0;
    /** Person's Name */
    public static final int JINMEI = 3;
    /** Postal Address */
    public static final int POSTAL_ADDRESS = 4;
    /** EMail Address */
    public static final int EMAIL_ADDRESS = 5;
    /**

    /**
     * Constructor
     */
    public WrapWnnEngine() {
    }

    /** init */
    public void init(String dirPath) {
        super.init(dirPath);
    }

    /** predict */
    public int predict(String str) {
        return super.predict(str);
    }

    /** setDictionary */
    public boolean setDictionary(int language, int setType){
        super.setDecoEmojiFilter(true);
        super.setEmojiFilter(true);
        return super.setDictionary(language, setType, 0);
    }

    /** setCandidates */
    public void setCandidates(String[] strs){
        super.setCandidates(strs);
    }

    /** close */
    public void close() {
        super.close();
    }

}
