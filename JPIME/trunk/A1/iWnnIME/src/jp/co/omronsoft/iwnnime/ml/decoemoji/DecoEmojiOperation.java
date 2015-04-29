/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml.decoemoji;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiAttrInfo;

/** Container class of DecoEmoji operation. */
public class DecoEmojiOperation {
    /** The information added to dictionary. */
    private DecoEmojiAttrInfo mDecoEmojiAttrInfo;

    /** The dictionary processing type. */
    private int mType;

    /**
     * Constructor.
     *
     * @param decoemojiattrinfo  The information added to dictionary.
     * @param type  The dictionary processing type.
     */
    public DecoEmojiOperation(DecoEmojiAttrInfo decoemojiattrinfo, int type) {
        mDecoEmojiAttrInfo = decoemojiattrinfo;
        mType = type;
    }

    /**
     * Gets the information added to dictionary.
     *
     * @return The information added to dictionary.
     */
    public DecoEmojiAttrInfo[] getDecoEmojiAttrInfo() {
        return new DecoEmojiAttrInfo[] { mDecoEmojiAttrInfo };
    }

    /**
     * Gets the dictionary processing type.
     *
     * @return The dictionary processing type.
     */
    public int getType() {
        return mType;
    }
}
