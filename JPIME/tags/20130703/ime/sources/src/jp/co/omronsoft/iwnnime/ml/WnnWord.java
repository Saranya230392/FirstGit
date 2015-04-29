/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;

/**
 * The container class of a word.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class WnnWord {
    /** The word's ID */
    public int      id;
    /** The string of this word. */
    public String   candidate;
    /** The reading of this word. */
    public String   stroke;
    /** The attribute of this word when it is assumed a candidate. */
    public int      attribute;
    /** The lexical category of this word. */
    public int      lexicalCategory;
    /** The decoEmoji info of this word. */
    public EmojiAssist.DecoEmojiTextInfo      decoEmojiInfo;
    public int symbolMode = -1;

    /**
     * Constructor
     */
    public WnnWord() {
        this(0, "", "", 0, 0, null);
    }

    /**
     * Constructor
     *
     * @param candidate     The string of word
     * @param stroke        The reading of word
     */
    public WnnWord(String candidate, String stroke) {
        this(0, candidate, stroke, 0, 0, null);
    }

    /**
     * Constructor
     *
     * @param id            The ID of word
     * @param candidate     The string of word
     * @param stroke        The reading of word
     */
    public WnnWord(int id, String candidate, String stroke) {
        this(id, candidate, stroke, 0, 0, null);
    }

    /**
     * Constructor
     *
     * @param id            The ID of word
     * @param candidate     The string of word
     * @param stroke        The reading of word
     * @param attribute     The attribute of word
     */
    public WnnWord(int id, String candidate, String stroke, int attribute) {
        this(id, candidate, stroke, attribute, 0, null);
    }

    /**
     * Constructor
     *
     * @param id              The ID of word
     * @param candidate       The string of word
     * @param stroke          The reading of word
     * @param attribute       The attribute of word
     * @param lexicalCategory The lexical category of word
     */
    public WnnWord(int id, String candidate, String stroke, int attribute, int lexicalCategory) {
        this(id, candidate, stroke, attribute, lexicalCategory, null);
    }
    /**
     * Constructor
     *
     * @param id              The ID of word
     * @param candidate       The string of word
     * @param stroke          The reading of word
     * @param attribute       The attribute of word
     * @param lexicalCategory The lexical category of word
     * @param decoEmojiInfo   The decoEmoji info of this word
     */
    public WnnWord(int id, String candidate, String stroke, int attribute, int lexicalCategory, EmojiAssist.DecoEmojiTextInfo decoEmojiInfo) {
        this.id = id;
        this.candidate = candidate;
        this.stroke = stroke;
        this.attribute = attribute;
        this.lexicalCategory = lexicalCategory;
        this.decoEmojiInfo = decoEmojiInfo;
    }

    /**
     * getSymbolMode
     */
    public int getSymbolMode() {
        return symbolMode;
    }

    /**
     * setSymbolMode
     * @param symbolMode
     */
    public void setSymbolMode(int symbolMode) {
        this.symbolMode = symbolMode;
    }

}
