/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml.hangul.ko;

import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.OpenWnnEvent;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.WnnKeyboardFactory;
import jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import java.util.Locale;


/**
 * Software Keyboard class for Korean IME
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class DefaultSoftKeyboardKorean extends DefaultSoftKeyboardHangul {

    /**
     * The Hangul keyboard(KEYMODE_KO_HANGUL), the alphabet
     * keyboard(KEYMODE_KO_ALPHABET) and the number/symbol
     * keyboard(KEYMODE_KO_NUMBER) is active.  The phone number
     * keyboard(KEYMODE_KO_PHONE) and the voice
     * keyboard(KEYMODE_KO_VOICE) is disabled.
     */
    private static final boolean[] ALLOW_TOGGLE_TABLE = {true, true, true, false, false};

    /**
     * The Hangul keyboard(KEYMODE_KO_HANGUL), the alphabet
     * keyboard(KEYMODE_KO_ALPHABET) and the number/symbol
     * keyboard(KEYMODE_KO_NUMBER) is active.  The phone number
     * keyboard(KEYMODE_KO_PHONE) and the voice
     * keyboard(KEYMODE_KO_VOICE) is disabled.
     */
    private static final boolean[] ALLOW_TOGGLE_TABLE_VOICE = {true, true, true, false, false};

    /** Default constructor */
    public DefaultSoftKeyboardKorean(IWnnLanguageSwitcher wnn) {
        super(wnn);
        mCurrentLanguage     = LANG_CN;
        mCurrentKeyboardType = KEYBOARD_QWERTY;
        mShiftOn             = KEYBOARD_SHIFT_OFF;
        mCurrentKeyMode      = KEYMODE_KO_HANGUL;
    }

    /** Input mode toggle cycle table */
    private static final int[] KO_MODE_CYCLE_TABLE = {
        KEYMODE_KO_ALPHABET, KEYMODE_KO_NUMBER
    };

    /** Input mode toggle cycle table voice on */
    private static final int[] KO_MODE_CYCLE_TABLE_VOICE = {
        KEYMODE_KO_ALPHABET, KEYMODE_KO_NUMBER, KEYMODE_KO_VOICE
    };

    /** Input mode default table */
    private static final int[] KO_MODE_DEFAULT_TABLE = {KEYMODE_KO_HANGUL};

    /**
     * The normal keyboard(KEYMODE_KO_BOPOMOFO) and the number/symbol
     * keyboard(KEYMODE_KO_NUMBER) is active.  The phone number
     * keyboard(KEYMODE_KO_PHONE) is disabled.
     */

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#createKeyboards */
    @Override protected void createKeyboards(OpenWnn parent) {
        /* WnnKeyboardFactory[# of Languages][portrait/landscape][# of keyboard type][shift off/on][max # of key-modes][noinput/input] */
        mKeyboard = new WnnKeyboardFactory[3][2][4][2][12][4][2];

        if (mHardKeyboardHidden) {
            WnnKeyboardFactory[][][] keyList;
            /********************* portrait **************************/
            /* qwerty shift_off (portrait) */
            keyList = mKeyboard[LANG_CN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
            keyList[KEYMODE_KO_HANGUL][KEYINPUTTYPE_NORMAL][0]   = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_ko);

            /* qwerty shift_on (portrait) */
            keyList = mKeyboard[LANG_CN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
            keyList[KEYMODE_KO_HANGUL][KEYINPUTTYPE_NORMAL][0]   = new WnnKeyboardFactory(parent, R.xml.keyboard_qwerty_ko_shift);

            WnnKeyboardFactory[][][] keyListPortrait;
            /********************* landscape **************************/
            /* qwerty shift_off (landscape) */
            keyList = mKeyboard[LANG_CN][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
            keyListPortrait = mKeyboard[LANG_CN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
            keyList[KEYMODE_KO_HANGUL][KEYINPUTTYPE_NORMAL][0]   = keyListPortrait[KEYMODE_KO_HANGUL][KEYINPUTTYPE_NORMAL][0];

            /* qwerty shift_on (landscape) */
            keyList = mKeyboard[LANG_CN][LANDSCAPE][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
            keyListPortrait = mKeyboard[LANG_CN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
            keyList[KEYMODE_KO_HANGUL][KEYINPUTTYPE_NORMAL][0]   = keyListPortrait[KEYMODE_KO_HANGUL][KEYINPUTTYPE_NORMAL][0];

            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                          IWnnImeKorean.ENGINE_MODE_OPT_TYPE_QWERTY));
        } else {
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                          IWnnImeKorean.ENGINE_MODE_OPT_TYPE_QWERTY));
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#onKey */
    @Override public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode == KEYCODE_SWITCH_BOPOMOFO) {
            changeKeyMode(KEYMODE_CN_BOPOMOFO);
        }

        // Update shift status.
        super.onKey(primaryCode, keyCodes);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#getAllowToggleTable */
    @Override protected boolean[] getAllowToggleTable() {
        if (mEnableVoiceInput) {
            return ALLOW_TOGGLE_TABLE_VOICE;
        } else {
            return ALLOW_TOGGLE_TABLE;
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#getCurrentLocal */
    @Override protected Locale getCurrentLocal() {
        return Locale.KOREA;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#getModeCycleTable */
    @Override protected int[] getModeCycleTable() {
        if (mEnableVoiceInput) {
            return KO_MODE_CYCLE_TABLE_VOICE;
        } else {
            return KO_MODE_CYCLE_TABLE;
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#getModeDefaultTable */
    @Override protected int[] getModeDefaultTable() {
        return KO_MODE_DEFAULT_TABLE;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#getDefaultKeymode */
    @Override protected int getDefaultKeymode() {
        return KEYMODE_KO_HANGUL;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul#getAlphabetKeymode */
    @Override protected int getAlphabetKeymode() {
        return KEYMODE_KO_ALPHABET;
    }

}
