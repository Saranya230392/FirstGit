/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.jajp;
///////////////// iwnn_trial_add_0

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.MetaKeyKeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.BaseInputView;
import jp.co.omronsoft.iwnnime.ml.CandidatesViewManager;
import jp.co.omronsoft.iwnnime.ml.ComposingText;
import jp.co.omronsoft.iwnnime.ml.ControlPanelStandard;
import jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard;
import jp.co.omronsoft.iwnnime.ml.IWnnImeBase;
import jp.co.omronsoft.iwnnime.ml.InputMethodBase;
import jp.co.omronsoft.iwnnime.ml.InputViewManager;
import jp.co.omronsoft.iwnnime.ml.KeyboardView;
import jp.co.omronsoft.iwnnime.ml.LetterConverter;
import jp.co.omronsoft.iwnnime.ml.MushroomControl;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.OpenWnnEvent;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.StrSegment;
import jp.co.omronsoft.iwnnime.ml.TextCandidatesViewManager;
import jp.co.omronsoft.iwnnime.ml.WnnEngine;
import jp.co.omronsoft.iwnnime.ml.WnnWord;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiOperationQueue;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;
import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnSymbolEngine;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import jp.co.omronsoft.iwnnime.ml.jajp.Romkan;
import jp.co.omronsoft.iwnnime.ml.jajp.RomkanFullKatakana;
import jp.co.omronsoft.iwnnime.ml.jajp.RomkanHalfKatakana;
import jp.co.omronsoft.iwnnime.ml.jajp.Directkan;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
///////////////// iwnn_trial_add_1

/**
 * The iWnn Japanese IME class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class IWnnImeJaJp extends IWnnImeBase {
///////////////// iwnn_trial_add_2

    //////////////////////////////////////////////////////////////// public static final
    /**
     * Mode of the convert engine (Full-width KATAKANA).
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_FULL_KATAKANA = 101;


    /**
     * Mode of the convert engine (Half-width KATAKANA).
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_HALF_KATAKANA = 102;

    /**
     * Mode of the convert engine (EISU-KANA conversion).
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_EISU_KANA = 103;

    /**
     * Mode of the convert engine (Symbol list).
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL = 104;

    /**
     * Mode of the convert engine (Keyboard type is QWERTY).
     * Use with {@code OpenWnn.CHANGE_MODE} event to change ambiguous searching pattern.
     * (This setting becomes OR. It treats in a word as an additive attribute without
     *  nullifying other ENGINE_MODE_xxx. )
     */
    public static final int ENGINE_MODE_OPT_TYPE_QWERTY = 105;

    /**
     * Mode of the convert engine (Keyboard type is 12-keys).
     * Use with {@code OpenWnn.CHANGE_MODE} event to change ambiguous searching pattern.
     * (This setting becomes OR. It treats in a word as an additive attribute without
     *  nullifying other ENGINE_MODE_xxx. )
     */
    public static final int ENGINE_MODE_OPT_TYPE_12KEY = 106;

    //////////////////////////////////////////////////////////////// private static final
    /** [For debugging] True when debugging processing is effectively done */
    private static final boolean DEBUG = false;

    /** [For debugging] True if method tracing is enabled. */
    private static final boolean PROFILE = false;

    /** Highlight color style for the converted clause */
    private static final CharacterStyle SPAN_CONVERT_TEXTCOLOR = new ForegroundColorSpan(0xFF000000);

    /** Highlight text color */
    private static final CharacterStyle SPAN_TEXTCOLOR  = new ForegroundColorSpan(0xFF008AAC);

     /** IME's status for {@code mStatus}(auto cursor done).
     * <br>
     * After the cursor moves automatically, the TOGGLE_REVERSE_CHAR can be done.
     */
    private static final int STATUS_AUTO_CURSOR_DONE  = 0x0100;

   /** Alphabet-last pattern */
    private static final Pattern ENGLISH_CHARACTER_LAST = Pattern.compile(".*[a-zA-Z]$");

    /** Alphabet-all pattern */
    private static final Pattern ENGLISH_CHARACTER_ALL = Pattern.compile("^[a-zA-Z]+$");

    /** private area character code got by <code>KeyEvent.getUnicodeChar()</code> */
    private static final int PRIVATE_AREA_CODE = 61184;

    /** Maximum length of input string */
    private static final int LIMIT_INPUT_NUMBER = 50;

    /** Large alphabet charcode */
    private static final int CHARCODE_LARGE_A = 65;
    private static final int CHARCODE_LARGE_Z = 90;

    /** Offset, alphabet charcode (Large => Small)  */
    private static final int CHARCODE_OFFSET_LARGE_TO_SMALL = 32;

    /** Bit flag for English auto commit mode (ON) */
    private static final int AUTO_COMMIT_ENGLISH_ON = 0x0000;
    /** Bit flag for English auto commit mode (OFF) */
    private static final int AUTO_COMMIT_ENGLISH_OFF = 0x0001;
    /** Bit flag for English auto commit mode (symbol list) */
    private static final int AUTO_COMMIT_ENGLISH_SYMBOL  = 0x0010;

    /** Message for {@code mHandler} (close) */
    private static final int MSG_CLOSE = 2;

    /** Message for {@code mHandler} (toggle time limit) */
    private static final int MSG_TOGGLE_TIME_LIMIT = 3;

    /** Message for {@code mHandler} (updating the learning dictionary) */
    private static final int MSG_UPDATE_LEARNING_DICTIONARY = 5;

    /** Message for {@code mHandler} (updating the DecoEmoji dictionary) */
    private static final int MSG_UPDATE_DECOEMOJI_DICTIONARY = 6;

    /** Message for {@code mHandler} (restart) */
    private static final int MSG_RESTART = 7;

    /** Message for {@code mHandler} (delete decoemoji the learning dictionary) */
    private static final int MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY = 8;

    /** Message for {@code mHandler} (sync the learning dictionary) */
    private static final int MSG_SYNC_LEARNING_DICTIONARY = 9;

    /** Delay time(msec.) updating the learning dictionary. */
    private static final int DELAY_MS_UPDATE_LEARNING_DICTIONARY = 0;

    /** Delay time(msec.) updating the DecoEmoji dictionary. */
    private static final int DELAY_MS_UPDATE_DECOEMOJI_DICTIONARY = 0;

    /** Delay time(msec.) delete decoemoji the learning dictionary. */
    private static final int DELAY_MS_DELETE_DECOEMOJI_LEARNING_DICTIONARY = 0;

    /** Transmitted definition for APP. */
    public static final String COMMIT_TEXT_THROUGH_ACTION = "jp.co.omronsoft.iwnnime.ml";

    /** Action key for Through YOMI of committed text. */
    public static final String COMMIT_TEXT_THROUGH_KEY_YOMI = "yomi";

    /** Turn off the Automatic cursor movement. **/
    private static final int AUTO_CURSOR_MOVEMENT_OFF = 0;

    /** privateImeOption eiji **/
    private static final String DEFAULT_KEYMODE_EIJI_STR = "jp.co.omoronsoft.iwnnime.ml.mode=eng";

    /** privateImeOption eiji for LG **/
    private static final String DEFAULT_KEYMODE_EIJI_LG_STR = "com.diotek.mode=eng";

    /** privateImeOption eiji only **/
    public static final String DEFAULT_KEYMODE_EIJI_ONLY_STR = "jp.co.omronsoft.iwnnime.ml.mode=eiji";

    /** privateImeOption hiragana **/
    private static final String DEFAULT_KEYMODE_HIRAGANA_STR = "jp.co.omoronsoft.iwnnime.ml.mode=jpn";

    /** privateImeOption hiragana for LG **/
    private static final String DEFAULT_KEYMODE_HIRAGANA_LG_STR = "com.diotek.mode=jpn";

    /** privateImeOption hiragana only **/
    public static final String DEFAULT_KEYMODE_HIRAGANA_ONLY_STR = "jp.co.omronsoft.iwnnime.ml.mode=hiragana";

    /** privateImeOption num **/
    private static final String DEFAULT_KEYMODE_NUM_STR = "jp.co.omoronsoft.iwnnime.ml.mode=num";

    /** privateImeOption full hiragana **/
    private static final String DEFAULT_KEYMODE_FULL_HIRAGANA_STR = "jp.co.omronsoft.iwnnime.ml.mode=1";

    /** privateImeOption full_katakana **/
    private static final String DEFAULT_KEYMODE_FULL_KATAKANA_STR = "jp.co.omronsoft.iwnnime.ml.mode=2";

    /** privateImeOption half_katakana **/
    private static final String DEFAULT_KEYMODE_HALF_KATAKANA_STR = "jp.co.omronsoft.iwnnime.ml.mode=3";

    /** privateImeOption full_alphabet **/
    private static final String DEFAULT_KEYMODE_FULL_ALPHABET_STR = "jp.co.omronsoft.iwnnime.ml.mode=4";

    /** privateImeOption half_alphabet **/
    private static final String DEFAULT_KEYMODE_HALF_ALPHABET_STR = "jp.co.omronsoft.iwnnime.ml.mode=5";

    /** privateImeOption full_number **/
    private static final String DEFAULT_KEYMODE_FULL_NUMBER_STR = "jp.co.omronsoft.iwnnime.ml.mode=6";

    /** privateImeOption half_number **/
    private static final String DEFAULT_KEYMODE_HALF_NUMBER_STR = "jp.co.omronsoft.iwnnime.ml.mode=7";

    /** privateImeOption half_phone **/
    private static final String DEFAULT_KEYMODE_HALF_PHONE_STR = "jp.co.omronsoft.iwnnime.ml.mode=9";

    /** privateImeOption lge_aime_keep_current_language **/
    private static final String DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG_STR = "com.lge.android.aime.KeepCurrentLang";

    /** return value (default keymode Nothing) **/
    public static final int DEFAULT_KEYMODE_NOTHING = 1;

    /** return value (default keymode full_hiragana) **/
    public static final int DEFAULT_KEYMODE_FULL_HIRAGANA = 2;

    /** return value (default keymode full_katakana) **/
    public static final int DEFAULT_KEYMODE_FULL_KATAKANA = 3;

    /** return value (default keymode half_katakana) **/
    public static final int DEFAULT_KEYMODE_HALF_KATAKANA = 4;

    /** return value (default keymode full_alphabet) **/
    public static final int DEFAULT_KEYMODE_FULL_ALPHABET = 5;

    /** return value (default keymode half_alphabet) **/
    public static final int DEFAULT_KEYMODE_HALF_ALPHABET = 6;

    /** return value (default keymode full_number) **/
    public static final int DEFAULT_KEYMODE_FULL_NUMBER = 7;

    /** return value (default keymode half_number) **/
    public static final int DEFAULT_KEYMODE_HALF_NUMBER = 8;

    /** return value (default keymode voice) **/
    public static final int DEFAULT_KEYMODE_VOICE = 9;

    /** return value (default keymode phone) **/
    public static final int DEFAULT_KEYMODE_HALF_PHONE = 10;

    /** return value (default keymode full_hiragana_only) **/
    public static final int DEFAULT_KEYMODE_FULL_HIRAGANA_ONLY = 11;

    /** return value (default keymode half_alphabet_only) **/
    public static final int DEFAULT_KEYMODE_HALF_ALPHABET_ONLY = 12;

    /** return value (default keymode lge_aime_keep_current_lang) **/
    public static final int DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG = 13;

    /** Max code of ascii chars. */
    private static final int MAX_ASCII_CODE = 0x7F;

    /** Key of "Japanese Input Word Learning" setting. **/
    private static final String OPT_ENABLE_LEARNING_KEY = "opt_enable_learning";

    /** Key of "Japanese Word Prediction" setting. **/
    private static final String OPT_PREDICTION_KEY = "opt_prediction";

    /** Key of "Japanese Wildcard Prediction" setting. **/
    private static final String OPT_FUNFUN_KEY = "opt_funfun";

    /** Key of "Japanese Typing Error Correction" setting. **/
    private static final String OPT_SPELL_CORRECTION_KEY = "opt_spell_correction";

    /** H/W 12Keyboard keycode replace table */
    private static final HashMap<Integer, Integer> HW12KEYBOARD_KEYCODE_REPLACE_TABLE
            = new HashMap<Integer, Integer>() {{
          put(KeyEvent.KEYCODE_0, DefaultSoftKeyboard.KEYCODE_JP12_0);
          put(KeyEvent.KEYCODE_1, DefaultSoftKeyboard.KEYCODE_JP12_1);
          put(KeyEvent.KEYCODE_2, DefaultSoftKeyboard.KEYCODE_JP12_2);
          put(KeyEvent.KEYCODE_3, DefaultSoftKeyboard.KEYCODE_JP12_3);
          put(KeyEvent.KEYCODE_4, DefaultSoftKeyboard.KEYCODE_JP12_4);
          put(KeyEvent.KEYCODE_5, DefaultSoftKeyboard.KEYCODE_JP12_5);
          put(KeyEvent.KEYCODE_6, DefaultSoftKeyboard.KEYCODE_JP12_6);
          put(KeyEvent.KEYCODE_7, DefaultSoftKeyboard.KEYCODE_JP12_7);
          put(KeyEvent.KEYCODE_8, DefaultSoftKeyboard.KEYCODE_JP12_8);
          put(KeyEvent.KEYCODE_9, DefaultSoftKeyboard.KEYCODE_JP12_9);
          put(KeyEvent.KEYCODE_POUND, DefaultSoftKeyboard.KEYCODE_JP12_SHARP);
          put(KeyEvent.KEYCODE_STAR, DefaultSoftKeyboard.KEYCODE_JP12_ASTER);
          put(KeyEvent.KEYCODE_CALL, DefaultSoftKeyboard.KEYCODE_JP12_REVERSE);
    }};

    private boolean isEmailAddressUriEditor = false;
    ////////////////////////////////////////////////////////////////// classes

    /** Convert engine's state */
    private static class EngineState {
        /** Definition for {@code EngineState.*} (invalid) */
        public static final int INVALID = -1;

        /** Definition for {@code EngineState.dictionarySet} (Japanese) */
        public static final int LANGUAGE_JP = 0;

        /** Definition for {@code EngineState.dictionarySet} (English) */
        public static final int LANGUAGE_EN = 1;

        /** Definition for {@code EngineState.convertType} (prediction/no conversion) */
        public static final int CONVERT_TYPE_NONE = 0;

        /** Definition for {@code EngineState.convertType} (consecutive clause conversion) */
        public static final int CONVERT_TYPE_RENBUN = 1;

        /** Definition for {@code EngineState.convertType} (EISU-KANA conversion) */
        public static final int CONVERT_TYPE_EISU_KANA = 2;

        /** Definition for {@code EngineState.temporaryMode} (change back to the normal dictionary) */
        public static final int TEMPORARY_DICTIONARY_MODE_NONE = 0;

        /** Definition for {@code EngineState.temporaryMode} (change to the symbol dictionary) */
        public static final int TEMPORARY_DICTIONARY_MODE_SYMBOL = 1;

        /** Definition for {@code EngineState.temporaryMode} (change to the user dictionary) */
        public static final int TEMPORARY_DICTIONARY_MODE_USER = 2;

        /** Definition for {@code EngineState.preferenceDictionary} (no preference dictionary) */
        public static final int PREFERENCE_DICTIONARY_NONE = 0;

        /** Definition for {@code EngineState.preferenceDictionary} (person's name) */
        public static final int PREFERENCE_DICTIONARY_PERSON_NAME = 1;

        /** Definition for {@code EngineState.preferenceDictionary} (place name) */
        public static final int PREFERENCE_DICTIONARY_POSTAL_ADDRESS = 2;

        /** Definition for {@code EngineState.preferenceDictionary} (email/URI) */
        public static final int PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI = 3;

        /** Definition for <code>EngineState.keyboard</code> (undefined) */
        public static final int KEYBOARD_UNDEF = 0;

        /** Definition for <code>EngineState.keyboard</code> (undefined) */
        public static final int KEYBOARD_QWERTY = 1;

        /** Definition for <code>EngineState.keyboard</code> (undefined) */
        public static final int KEYBOARD_12KEY  = 2;

        /** Type of Language */
        public int language = INVALID;

        /** Type of conversion */
        public int convertType = INVALID;

        /** Temporary mode */
        public int temporaryMode = INVALID;

        /** Preference dictionary setting */
        public int preferenceDictionary = INVALID;

        /** keyboard */
        public int keyboard = INVALID;

        /**
         * Return whether current type of conversion is a consecutive clause(RENBUNSETSU) conversion.
         *
         * @return {@code true} if current type of conversion is consecutive clause conversion.
         */
        public boolean isRenbun() {
            return convertType == CONVERT_TYPE_RENBUN;
        }

        /**
         * Return whether current type of conversion is EISU-KANA conversion.
         *
         * @return {@code true} if current type of conversion is EISU-KANA conversion.
         */
        public boolean isEisuKana() {
            return convertType == CONVERT_TYPE_EISU_KANA;
        }

        /**
         * Return whether current type of conversion is no conversion.
         *
         * @return {@code true} if no conversion is executed currently.
         */
        public boolean isConvertState() {
            return convertType != CONVERT_TYPE_NONE;
        }

        /**
         * Check whether or not the mode is "symbol list".
         *
         * @return {@code true} if the mode is "symbol list".
         */
        public boolean isSymbolList() {
            return temporaryMode == TEMPORARY_DICTIONARY_MODE_SYMBOL;
        }

        /**
         * Check whether or not the current language is English.
         *
         * @return {@code true} if the current language is English.
         */
        public boolean isEnglish() {
            return language == LANGUAGE_EN;
        }
    }

    //////////////////////////////////////////////////////////////// protected
    /** Whether exact match searching or not */
    protected boolean mExactMatchMode = false;

    /** Spannable string builder to display the composing text */
    protected SpannableStringBuilder mDisplayText;

    //////////////////////////////////////////////////////////////// private


    /** Backup for switching the converter */
    private WnnEngine mConverterBack;

    /** Backup for switching the pre-converter */
    private LetterConverter mPreConverterBack;

    /** iWnn conversion engine for Japanese */
    private iWnnEngine mConverterIWnn;

    /** Pseudo engine (GIJI) for sign (Backup variable for switch) */
    private IWnnSymbolEngine mConverterSymbolEngineBack;

    /** Romaji-to-Kana converter (HIRAGANA) */
    private Romkan mPreConverterHiragana;

    /** Romaji-to-Kana converter (full-width KATAKANA) */
    private RomkanFullKatakana mPreConverterFullKatakana;

    /** Romaji-to-Kana converter (half-width KATAKANA) */
    private RomkanHalfKatakana mPreConverterHalfKatakana;

    /** Direct-Kana converter (HIRAGANA) */
    private Directkan mPreConverterHiraganaDirect;

    /** for change kana input mode pre-converter */
    private LetterConverter mPreConverterKanaRoman;
    private LetterConverter mPreConverterKanaDirect;

    /** True when Wildcard input is effectively done */
    private boolean mEnableFunfun = true;

    /** Conversion Engine's state */
    private EngineState mEngineState = new EngineState();

    /** Whether learning function is active or not. */
    private boolean mEnableLearning = true;

    /** Whether prediction is active or not. */
    private boolean mEnablePrediction = true;

    /** Whether using the converter */
    private boolean mEnableConverter = true;

    /** Whether displaying the symbol list */
    private boolean mEnableSymbolList = true;

    /** Whether non ASCII code is enabled */
    private boolean mEnableSymbolListNonHalf = true;

    /** Whether being able to use Emoji */
    private boolean mEnableEmoji = false;

    /** Whether being able to use DecoEmoji */
    private boolean mEnableDecoEmoji = false;

    /** Whether mistyping correction is enable or not */
    private boolean mEnableSpellCorrection = true;

    /** Auto commit state (in English mode) */
    private int mDisableAutoCommitEnglishMask = AUTO_COMMIT_ENGLISH_ON;

    /** Whether removing a space before a separator or not. (in English mode) */
    private boolean mEnableAutoDeleteSpace = false;

    /** Whether auto-spacing is enabled or not. */
    private boolean mEnableAutoInsertSpace = true;

    /** Stroke of committed text */
    private String mStrokeOfCommitText = null;

    /** Size of the stroke to delete */
    private int mSizeOfDeleteStroke = 0;

    /** Whether the H/W keyboard is hidden. */
    private boolean mHardKeyboardHidden = true;

    /** Whether the H/W 12keyboard is active or not. */
    private boolean mEnableHardware12Keyboard = false;

    /** Package name of the last application which used iWnn IME*/
    private String mPreEditorPackageName = "";

    /** Whether lattice input mode is active or not */
    private static boolean mLatticeInputMode = false;

    /**
     * Number of committed clauses on a consecutive clause conversion.
     * <br>
     * [Use purpose]
     * <br>
     * * fix the candidate position on a consecutive clause conversion.
     * <br>
     * * Convert consecutive clause when call updateViewStatus() with mCommitCount == 0
     * <br>
     *   (when mCommitCount != 0, it gets only the candidate)
     * <br>
     *
     * [Update time]
     * <br>
     * * Clear 0, when Start converting or reconverting
     * <br>
     * * Increment, when it's committed
     */
    private int mCommitCount = 0;

    /** Current orientation of the display */
    private int mOrientation = Configuration.ORIENTATION_UNDEFINED;

    /** Regular expression pattern for English separators */
    private  Pattern mEnglishAutoCommitDelimiter = null;

    /** Cursor position in the composing text */
    private int mComposingStartCursor = 0;

    /** Cursor position before committing text */
    private int mCommitStartCursor = 0;

    /** Previous committed text */
    private StringBuffer mPrevCommitText = null;

    /** Set when the IME is wanted to be disable initialization on onUpdateSelection() */
    private boolean mIgnoreCursorMove = false;

    /** Auto caps mode */
    private boolean mAutoCaps = false;

    /** Predict character string when fixed (for Undo) */
    private String  mCommitPredictKey = null;

    /** Wildcard prediction when fixed (for Undo) */
    private int mCommitFunfun = 0;

    /** Cursor position when fixed (for Undo) */
    private int mCommitCursorPosition = 0;

    /** State of character number specification predict when fixed (for Undo) */
    private boolean mCommitExactMatch = false;

    /** State of conversion when fixed (for Undo)*/
    private int mCommitConvertType = 0;

    /** True in case of possible Undo (for Undo) */
    private boolean mCanUndo = false;

    /** Layer1 information of the composing text when committed */
    private ArrayList<StrSegment> mCommitLayer1StrSegment = null;

    /** Whether there is a continuable predicted candidate */
    private boolean mHasContinuedPrediction;

    /** True when input is a consecutive */
    private boolean mIsInputSequenced;

    /** Whether text selection has started */
    private boolean mHasStartedTextSelection = false;

    /** Cursor of starting selection. */
    private int mSelectionStartCursor;

    /** Whether candidate is fullscreen or not */
    private boolean mFullCandidate = false;

    /** Use Mushroom */
    protected boolean mEnableMushroom = false;

    /** TextCandidatesViewManager */
    protected TextCandidatesViewManager mTextCandidatesViewManager = null;

    /** Automatic cursor movement speed */
    private int mAutoCursorMovementSpeed;

    /** Direct Kana Input mode */
    private boolean mDirectKana = false;

    /** EditorInfo */
    protected EditorInfo mAttribute = null;

    /** Enable head conversion */
    private boolean mEnableHeadConv = true;

    /** Whether auto-punctuation is enabled or not. */
    private boolean mAutoPunctuation = true;

    /** {@code Handler} for drawing candidates/displaying */
    Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            if (OpenWnn.PERFORMANCE_DEBUG) {Log.d(TAG, "handel Message Start");}

            switch (msg.what) {
                case MSG_CLOSE:
                    if (mConverterIWnn != null) mConverterIWnn.close();
                    if (mConverterSymbolEngineBack != null) mConverterSymbolEngineBack.close();
                    break;

                case MSG_TOGGLE_TIME_LIMIT:
                    processToggleTimeLimit();
                    break;

                case MSG_UPDATE_LEARNING_DICTIONARY:
                    if (DEBUG) {Log.d(TAG, "Learning dictionary update");}
                    if (mConverterIWnn != null) {
                        mConverterIWnn.writeoutDictionary(iWnnEngine.LanguageType.JAPANESE,
                                iWnnEngine.SetType.LEARNDIC);
                        mConverterIWnn.writeoutDictionary(iWnnEngine.LanguageType.ENGLISH,
                                iWnnEngine.SetType.LEARNDIC);
                    }
                    break;

                case MSG_UPDATE_DECOEMOJI_DICTIONARY:
                    if (DEBUG) {Log.d(TAG, "DecoEmoji dictionary update");}
                    boolean result = DecoEmojiOperationQueue.getInstance().executeOperation("", null);
                    if (result) {
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(MSG_UPDATE_DECOEMOJI_DICTIONARY),
                                DELAY_MS_UPDATE_DECOEMOJI_DICTIONARY);
                    }
                    break;

                case MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY:
                    if (DEBUG) {Log.d(TAG, "Delete DecoEmoji for learning dictionary");}
                    result = mConverterIWnn.executeOperation(OpenWnn.superGetContext());
                    if (result) {
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY),
                                DELAY_MS_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
                    }
                    break;

                case MSG_RESTART:
                    onStartInput(mAttribute, false);
                    onStartInputView(mAttribute, false);
                    break;

                case MSG_SYNC_LEARNING_DICTIONARY:
                    if (DEBUG) {Log.d(TAG, "Sync dictionary update");}
                    if (mConverterIWnn != null) {
                        mConverterIWnn.syncDictionary(iWnnEngine.LanguageType.JAPANESE,
                                iWnnEngine.SetType.LEARNDIC);
                        mConverterIWnn.syncDictionary(iWnnEngine.LanguageType.ENGLISH,
                                iWnnEngine.SetType.LEARNDIC);
                        mConverterIWnn.sync();
                    }
                    break;
                default:
                    break;

           }
            if (OpenWnn.PERFORMANCE_DEBUG) {Log.d(TAG, "handel Message End");}
        }
    };

    ////////////////////////////////////////////////////////////////////// methods
    /**
     * Constructor
     */
    public IWnnImeJaJp(IWnnLanguageSwitcher wnn) {
        super(wnn);
        setComposingText(new ComposingText());
        mTextCandidatesViewManager = new TextCandidatesViewManager(iWnnEngine.CANDIDATE_MAX);
        setCandidatesViewManager(mTextCandidatesViewManager);
        setInputViewManager(createInputViewManager(wnn));
        mConverterIWnn = iWnnEngine.getEngine();
        setConverter(mConverterIWnn);
        String localeString = mConverterIWnn.getLocaleString(iWnnEngine.LanguageType.JAPANESE);
        mConverterSymbolEngineBack = new IWnnSymbolEngine(OpenWnn.superGetContext(), localeString);
        mPreConverterHiragana = new Romkan();
        setPreConverter(mPreConverterHiragana);
        mPreConverterFullKatakana = new RomkanFullKatakana();
        mPreConverterHalfKatakana = new RomkanHalfKatakana();
        mPreConverterHiraganaDirect = new Directkan();
        mPreConverterKanaRoman = mPreConverterHiragana;
        mPreConverterKanaDirect = mPreConverterHiraganaDirect;

        mDisplayText = new SpannableStringBuilder();
        setAutoHideMode(false);

        mPrevCommitText = new StringBuffer();
        mCommitLayer1StrSegment = new ArrayList<StrSegment>();

        ((DefaultSoftKeyboard) getInputViewManager()).resetCurrentKeyboard();
    }

    /**
     * Constructor
     *
     * @param context  The context
     */
    public IWnnImeJaJp(Context context, IWnnLanguageSwitcher wnn) {
        this(wnn);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreate */
    @Override public void onCreate() {
        onCreateOpenWnn();

        String delimiter = Pattern.quote(getResources().getString(R.string.ti_en_word_separators_txt));
        mEnglishAutoCommitDelimiter = Pattern.compile(".*[" + delimiter + "]$", Pattern.DOTALL);

        EngineState state = new EngineState();
        state.keyboard = EngineState.KEYBOARD_QWERTY;
        updateEngineState(state);

        mConverterIWnn.setConvertedCandidateEnabled(true);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreateCandidatesView */
    @Override public View onCreateCandidatesView() {
        return null;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreateInputView */
    @Override public View onCreateInputView() {
        int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
        mHardKeyboardHidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
        boolean type12Key
                = (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_12KEY);
        ((DefaultSoftKeyboard) getInputViewManager()).setHardKeyboardHidden(mHardKeyboardHidden);
        ((DefaultSoftKeyboard) getInputViewManager()).setHardware12Keyboard(type12Key);
        mTextCandidatesViewManager.setHardKeyboardHidden(mHardKeyboardHidden);
        mEnableHardware12Keyboard = type12Key;

        View v = super.onCreateInputView();
        return v;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onStartInput */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        mEnableFullscreen = false;
        if (attribute.initialSelStart != attribute.initialSelEnd) {
            mHasStartedTextSelection = true;
            mSelectionStartCursor = attribute.initialSelStart;
        }
        DefaultSoftKeyboardJAJP keyboard = ((DefaultSoftKeyboardJAJP)getInputViewManager());
        keyboard.onUpdateSelection(mHasStartedTextSelection);

        if (!mPreEditorPackageName.equals(attribute.packageName)) {
            mConverterIWnn.setDictionary(iWnnEngine.LanguageType.JAPANESE,
                    iWnnEngine.SetType.AUTOLEARNINGDIC, getSwitcher().hashCode());
            mConverterIWnn.deleteAutoLearningDictionary(iWnnEngine.SetType.AUTOLEARNINGDIC);
            mPreEditorPackageName = attribute.packageName;
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onStartInputView */
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        if (DEBUG) {Log.d(TAG, "onStartInputView(" + attribute + "," + restarting + ")");}

        boolean keep = restarting && (mAttribute != null)
            && TextUtils.equals(mAttribute.fieldName, attribute.fieldName);

        mAttribute = attribute;

        if (PROFILE) {
            Log.d(TAG, "onStartInputView() startMethodTracing.");
            Debug.startMethodTracing("iWnnIME");
        }

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();

        if (keep) {
            InputConnection ic = getInputConnection();
            if (ic != null) {
                CharSequence chars = ic.getTextBeforeCursor(1, 0);
                if ((chars != null) && chars.equals("")) {
                    keep = false;
                }
            }
        }

        // load preferences
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        if (keep || OpenWnn.getCurrentIme().isKeepInput()) {
            onStartInputViewOpenWnn(attribute, keep);
        } else {
            // Recreate a symbol engine. There is a possibility that
            // a history of symbols had been deleted by a user dictionary
            // tools.
            if (mConverterSymbolEngineBack != null) {
                mConverterSymbolEngineBack.close();
            }
            String localeString = mConverterIWnn.getLocaleString(getLanguage());
            mConverterSymbolEngineBack = new IWnnSymbolEngine(
                        OpenWnn.superGetContext(), localeString);

            mIgnoreCursorMove = false;
            clearCommitInfo();

            // initialize screen
            initializeScreen();

            ((DefaultSoftKeyboard) getInputViewManager()).resetCurrentKeyboard();

            onStartInputViewOpenWnn(attribute, keep);

            // initialize views
            setCandidateIsViewTypeFull(false);
            candidatesViewManager.clearCandidates();

            InputConnection inputConnection = getInputConnection();
            if (inputConnection != null) {
                getInputConnection().finishComposingText();
            }
            InputConnection ic = getInputConnection();
            if (ic != null) {
                int allMetaState = KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON
                        | KeyEvent.META_ALT_RIGHT_ON | KeyEvent.META_SHIFT_ON
                        | KeyEvent.META_SHIFT_LEFT_ON | KeyEvent.META_SHIFT_RIGHT_ON
                        | KeyEvent.META_SYM_ON;
                ic.clearMetaKeyStates(allMetaState);
            }
        }

        showFloating();

        isEmailAddressUriEditor = false;
        fitInputType(pref, attribute);
        updateFullscreenMode();

        mTextCandidatesViewManager.setAutoHide(true);

        // The connection of study is cut by IME beginning.
        if (isEnableL2Converter()) {
            breakSequence();
        }

        if (!mHandler.hasMessages(MSG_UPDATE_DECOEMOJI_DICTIONARY)) {
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(MSG_UPDATE_DECOEMOJI_DICTIONARY),
                    DELAY_MS_UPDATE_DECOEMOJI_DICTIONARY);
        }

        if (!mHandler.hasMessages(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY)) {
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY),
                    DELAY_MS_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
        }

        ((DefaultSoftKeyboard) getInputViewManager()).set5key();

        AsyncCommitMushRoomString();

        if (!(isEmailAddressUriEditor && !DefaultSoftKeyboard.mIsFromKoreanToJapanese)) {
            ((DefaultSoftKeyboardJAJP)getInputViewManager()).initInputKeyMode();
        }
        DefaultSoftKeyboard.mIsFromKoreanToJapanese = false;

        OpenWnn ime = OpenWnn.getCurrentIme();
        if (ime != null) {
            if (ime.mOriginalInputMethodSwitcher.mSymbolMode) {
                onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeJaJp.ENGINE_MODE_SYMBOL_SYMBOL));
            }
            if (ime.mOriginalInputMethodSwitcher.mNavigationMode) {
                ((DefaultSoftKeyboardJAJP)getInputViewManager()).wrapStartNavigationKeypad();
            }
            if (ime.mOriginalInputMethodSwitcher.mNumericMode) {
                IWnnLanguageSwitcher switcher = getSwitcher();
                switcher.setSymbolBackHangul(true);
                ((DefaultSoftKeyboardJAJP)getInputViewManager()).changeKeyMode(
                                                    DefaultSoftKeyboard.KEYMODE_JA_HALF_NUMBER);
                ime.mOriginalInputMethodSwitcher.mNumericMode = false;
            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#hideWindow */
    @Override public void hideWindow() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.setCandidateMsgRemove();

        InputViewManager inputViewManager = getInputViewManager();

        BaseInputView baseInputView = ((BaseInputView)((DefaultSoftKeyboard) inputViewManager).getCurrentView());
        if (baseInputView != null) {
            baseInputView.closeDialog();
        }

        getComposingText().clear();
        IWnnLanguageSwitcher switcher = getSwitcher();
        inputViewManager.onUpdateState(switcher);
        clearCommitInfo();
        inputViewManager.closing();

        mTextCandidatesViewManager.closeDialog();

        hideWindowOpenWnn();

        OpenWnn.getCurrentIme().onEvent(new OpenWnnEvent(OpenWnnEvent.CANCEL_WEBAPI));
        mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
        mHandler.removeMessages(MSG_RESTART);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SYNC_LEARNING_DICTIONARY),
                DELAY_MS_UPDATE_LEARNING_DICTIONARY);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onUpdateSelection */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        if (DEBUG) {Log.d(TAG, "onUpdateSelection()" + oldSelStart + ":" + oldSelEnd + ":"
                          + newSelStart + ":" + newSelEnd + ":" + candidatesStart + ":" + candidatesEnd);}
        onUpdateSelectionOpenWnn(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

        mComposingStartCursor = (candidatesStart < 0) ? newSelEnd : candidatesStart;

        // It makes it to improper undo by the selection beginning.
        boolean prevSelection = mHasStartedTextSelection;
        if (newSelStart != newSelEnd) {
            clearCommitInfo();
            mHasStartedTextSelection = true;
            mSelectionStartCursor = newSelStart;
        } else {
            mHasStartedTextSelection = false;
        }

        if (mHasContinuedPrediction) {
            mHasContinuedPrediction = false;
            mIgnoreCursorMove = false;
            return;
        }

        if (mEngineState.isSymbolList()) {
            return;
        }

        boolean isNotComposing = ((candidatesStart < 0) && (candidatesEnd < 0));
        ComposingText composingText = getComposingText();
        if (((composingText.size(ComposingText.LAYER1) + mFunfun) != 0)
              && !isNotComposing) {

            if (mHasStartedTextSelection) {
                InputConnection inputConnection = getInputConnection();
                if (inputConnection != null) {
                    if (0 < mFunfun) {
                        CharSequence composingString= convertComposingToCommitText(
                                composingText, mTargetLayer);
                        inputConnection.setComposingText(composingString, 1);
                        int funfunStart = Math.max(candidatesEnd - mFunfun, 0);
                        int selStart = newSelStart;
                        int selEnd = newSelEnd;
                        if (funfunStart < newSelStart) {
                            selStart = Math.max(newSelStart - mFunfun, funfunStart);
                        }
                        if (funfunStart < newSelEnd) {
                            selEnd = Math.max(newSelEnd - mFunfun, funfunStart);
                        }
                        inputConnection.setSelection(Math.max(selStart, 0),
                                Math.max(selEnd, 0));
                        mFunfun = 0;
                        setFunFun(mFunfun);
                    }
                    inputConnection.finishComposingText();
                }
                composingText.clear();
                initializeScreen();
            } else {
                updateViewStatus(mTargetLayer, false, true);
            }
            mIgnoreCursorMove = false;
        } else {
            if (mIgnoreCursorMove) {
                mIgnoreCursorMove = false;
            } else {
                int commitEnd = mCommitStartCursor + mPrevCommitText.length();
                if ((((newSelEnd < oldSelEnd) || (commitEnd < newSelEnd)) && clearCommitInfo())
                    || isNotComposing) {
                    // The learning connection is cut by the cursor movement.
                    if (isEnableL2Converter()) {
                        breakSequence();
                    }

                    InputConnection inputConnection = getInputConnection();
                    if (inputConnection != null) {
                        if (isNotComposing && (composingText.size(ComposingText.LAYER1) != 0)) {
                            inputConnection.finishComposingText();
                        }
                    }

                    // Initialize screen if text selection does not keep.
                    // (Stop initialization when the symbol list open on text selection.)
                    if ((prevSelection != mHasStartedTextSelection) || !mHasStartedTextSelection) {
                        composingText.clear();
                        initializeScreen();
                    }
                }
            }
        }

        if ((prevSelection != mHasStartedTextSelection)
                && (mHasStartedTextSelection || (newSelStart != mSelectionStartCursor))) {
            DefaultSoftKeyboardJAJP keyboard = ((DefaultSoftKeyboardJAJP)getInputViewManager());
            keyboard.onUpdateSelection(mHasStartedTextSelection);
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onConfigurationChanged */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        if (DEBUG) {Log.d(TAG, "onConfigurationChanged()");}
        View getCurrentView = getCandidatesViewManager().getCurrentView();
        boolean onRotation = (mOrientation != newConfig.orientation);
        boolean candidateShown = false;
        if (getCurrentView != null) {
            boolean isFloatingShown;
            if (onRotation) {
                // Get the display state of Floating keyboard before rotation.
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                        OpenWnn.superGetContext());
                isFloatingShown = pref.getBoolean(
                        mWnnSwitcher.getFloatingPreferenceKeyName(mOrientation),
                        getResources().getBoolean(R.bool.floating_mode_show_default_value));
            } else {
                isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase
                        .isNotExpandedFloatingMode();
            }
            if (isFloatingShown) {
                candidateShown = isCandidatesViewShownFloating();
            } else {
                candidateShown = getCurrentView.isShown();
            }
        }
        try {
            InputConnection inputConnection = getInputConnection();
            if (inputConnection != null) {
                if (onRotation) {
                    mOrientation = newConfig.orientation;
                    ComposingText composingText = getComposingText();
                    if ((0 < (composingText.size(ComposingText.LAYER1) + mFunfun))
                            || candidateShown
                            || mEngineState.isSymbolList()) {
                        OpenWnn.getCurrentIme().setStateOfKeepInput(true);
                    }
                }

                int hiddenState = newConfig.hardKeyboardHidden;
                mHardKeyboardHidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
                boolean type12Key = (newConfig.keyboard == Configuration.KEYBOARD_12KEY);
                if ((mOrientation != newConfig.orientation)
                        || (mEnableHardware12Keyboard != type12Key)) {
                    if (mRecognizing) {
                        mRecognizing = false;
                    }
                    mOrientation = newConfig.orientation;
                    mEnableHardware12Keyboard = type12Key;
                    commitConvertingText();
                    commitText(false);
                    mFunfun = 0;
                    setFunFun(mFunfun);
                    //So as not to display a wildcard prediction, initialize the screen.
                    initializeScreen();
                    updateViewStatus(mTargetLayer, false, true);
                }

                onConfigurationChangedOpenWnn(newConfig);

                if (super.isInputViewShown()) {
                    boolean requestCandidate = (!onRotation || candidateShown);
                    if (requestCandidate) {
                        WnnEngine converter = getConverter();
                       if (converter != null && converter instanceof IWnnSymbolEngine) {
                           IWnnSymbolEngine symbolEngine = (IWnnSymbolEngine)converter;
                           if (symbolEngine.getMode() == IWnnSymbolEngine.MODE_DECOEMOJI) {
                               symbolEngine.setMode(IWnnSymbolEngine.MODE_DECOEMOJI);
                           }
                       }
                   }
                    updateViewStatus(mTargetLayer, requestCandidate, true);
                }

                ((DefaultSoftKeyboard) getInputViewManager()).setHardKeyboardHidden(mHardKeyboardHidden);
                ((DefaultSoftKeyboard) getInputViewManager()).setHardware12Keyboard(type12Key);
                mTextCandidatesViewManager.setHardKeyboardHidden(mHardKeyboardHidden);
            } else {
                onConfigurationChangedOpenWnn(newConfig);
            }

        } catch (Exception ex) {
            Log.e(TAG, "IWnnImeJaJp::onConfigurationChanged " + ex.toString());
        }
        OpenWnn.getCurrentIme().setStateOfKeepInput(false);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvent */
    @Override synchronized public boolean onEvent(OpenWnnEvent ev) {
        boolean ret = false;
        InputConnection inputConnection = getInputConnection();
        if ((mFullCandidate && ((mStatus & STATUS_CANDIDATE_FULL) != 0))
                || isRightOrLeftKeyEvents(ev) || (inputConnection == null)) {
            ret = handleEvent(ev);
        } else {
            inputConnection.beginBatchEdit();
            ret = handleEvent(ev);
            inputConnection.endBatchEdit();
        }
        return ret;
    }

    /**
     * Process an event.
     *
     * @param  ev  An event
     * @return  {@code true} if the event is processed in this method; {@code false} if not.
     */
    private boolean handleEvent(OpenWnnEvent ev) {
        if (DEBUG) {Log.d(TAG, "onEvent()");}
///////////////// iwnn_trial_add_3

        EngineState state;

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        InputConnection inputConnection = getInputConnection();

        // Event handling
        // The keyboard is processed when it is invalid executable.
        switch (ev.code) {

        case OpenWnnEvent.RECEIVE_DECOEMOJI:
            if ((mConverterIWnn.getLanguage() != iWnnEngine.LanguageType.NONE)
                    && !mHandler.hasMessages(MSG_UPDATE_DECOEMOJI_DICTIONARY)) {
                mHandler.sendMessageDelayed(
                        mHandler.obtainMessage(MSG_UPDATE_DECOEMOJI_DICTIONARY),
                        DELAY_MS_UPDATE_DECOEMOJI_DICTIONARY);
            }

            if ((mConverterIWnn.getLanguage() == iWnnEngine.LanguageType.JAPANESE)
                    && !mHandler.hasMessages(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY)) {
                mHandler.sendMessageDelayed(
                        mHandler.obtainMessage(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY),
                        DELAY_MS_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
            }
            return true;

        case OpenWnnEvent.KEYUP:
            onKeyUpEvent(ev.keyEvent);
            return true;

        case OpenWnnEvent.KEYLONGPRESS:
            return onKeyLongPressEvent(ev.keyEvent);

        // Switch a dictionary engine
        case OpenWnnEvent.CHANGE_MODE:
            changeEngineMode(ev.mode);
            if (!((ev.mode == ENGINE_MODE_SYMBOL)
                  || (ev.mode == ENGINE_MODE_EISU_KANA)
                  || (ev.mode == ENGINE_MODE_SYMBOL_EMOJI)
                  || (ev.mode == ENGINE_MODE_SYMBOL_EMOJI_UNI6)
                  || (ev.mode == ENGINE_MODE_SYMBOL_SYMBOL)
                  || (ev.mode == ENGINE_MODE_SYMBOL_DECOEMOJI)
                  || (ev.mode == ENGINE_MODE_SYMBOL_KAO_MOJI)
                  || (ev.mode == ENGINE_MODE_SYMBOL_ADD_SYMBOL))) {
                if (!OpenWnn.getCurrentIme().isKeepInput()) {
                    initializeScreen();
                }
            }
            return true;
        // Update candidates
        case OpenWnnEvent.UPDATE_CANDIDATE:
            if (mEngineState.isRenbun()) {
                // Back to a prediction instead of reconversion
                // (there's a possibility that a committing text could be changed by deleting candidate)
                ComposingText composingText = getComposingText();
                composingText.setCursor(ComposingText.LAYER1,
                                         composingText.toString(ComposingText.LAYER1).length());
                mExactMatchMode = false;
                mFunfun = 0;
                setFunFun(mFunfun);
                updateViewStatusForPrediction(true, true); // prediction only
            } else {
                updateViewStatus(mTargetLayer, true, true);
            }
            return true;

        // Switch a view
        case OpenWnnEvent.CHANGE_INPUT_VIEW:
            View inputView = onCreateInputViewOpenWnn();
            if (inputView != null) {
                setInputView(inputView);
            }
            WindowManager wm = (WindowManager)mWnnSwitcher.getSystemService(Context.WINDOW_SERVICE);
            int candidateWidth;
            boolean isFloatingShown = InputMethodBase.mOriginalInputMethodSwitcher.mInputMethodBase
                    .isNotExpandedFloatingMode();
            if (isFloatingShown) {
                candidateWidth = getResources().getDimensionPixelSize(
                        R.dimen.floating_keyboard_width)
                        - getResources().getDimensionPixelSize(
                                R.dimen.floating_candidate_padding_left)
                        - getResources().getDimensionPixelSize(
                                R.dimen.floating_candidate_padding_right);
            } else {
                candidateWidth = wm.getDefaultDisplay().getWidth();
            }
            mTextCandidatesViewManager.initView(mWnnSwitcher,
                                              candidateWidth,
                                              wm.getDefaultDisplay().getHeight());
            View view = null;
            view = mTextCandidatesViewManager.getCurrentView();
            if (view != null) {
                mWnnSwitcher.setCandidatesView(view);
            }
            return true;

        case OpenWnnEvent.TOUCH_OTHER_KEY:
            mStatus |= STATUS_INPUT_EDIT;
            return true;

        case OpenWnnEvent.UNDO:
            return undo();

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_UP:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollUp();
            }
            return true;

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_DOWN:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollDown();
            }
            return true;

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_FULL_UP:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollFullUp();
            }
            return true;

        case OpenWnnEvent.CANDIDATE_VIEW_SCROLL_FULL_DOWN:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).setScrollFullDown();
            }
            return true;

        case OpenWnnEvent.FOCUS_CANDIDATE_START:
            ((DefaultSoftKeyboard) getInputViewManager()).setOkToEnterKey();
            return true;

        case OpenWnnEvent.FOCUS_CANDIDATE_END:
            getInputViewManager().onUpdateState(getSwitcher());
            return true;

        case OpenWnnEvent.CANCEL_WEBAPI:
            mConverterIWnn.onDoneGettingCandidates();
            return true;

        case OpenWnnEvent.TIMEOUT_WEBAPI:
            boolean success = mConverterIWnn.isWebApiSuccessReceived();
            int errorCodeResourceId;
            if (success) {
                errorCodeResourceId = R.string.ti_webapi_timeout_error_received_display_txt;
            } else {
                errorCodeResourceId = R.string.ti_webapi_timeout_error_txt;
            }

            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).onWebApiError(
                        getResources().getString(errorCodeResourceId));
            }

            if (success) {
                OpenWnn.getCurrentIme().onEvent(new OpenWnnEvent(OpenWnnEvent.RESULT_WEBAPI_OK));
                mConverterIWnn.onDoneGettingCandidates();
            }
            return true;

        case OpenWnnEvent.VOICE_INPUT:
            startShortcutIME();
            return true;

        case OpenWnnEvent.UPDATE_VIEW_STATUS_USE_FOCUSED_CANDIDATE:
            if (mEngineState.isRenbun()) {
                WnnWord word = getFocusedCandidate();
                updateComposingText(word);
                updateViewStatus(ComposingText.LAYER2, false, false);
            }
            return true;

        default:
            // No processing
            break;
        }

        // Get the Keycode
        KeyEvent keyEvent = ev.keyEvent;
        int keyCode = 0;
        if (keyEvent != null) {
            keyCode = keyEvent.getKeyCode();
        }

        if (isDirectInputMode()) {
            if (inputConnection != null) {
                switch (ev.code) {
                case OpenWnnEvent.INPUT_SOFT_KEY:
                    sendKeyEventDirect(keyEvent);
                    return true;

                case OpenWnnEvent.INPUT_CHAR:
                    if(ev.chars[0] >= '0' && ev.chars[0] <= '9') {
                        // Through YOMI of committed text for APP
                        commitTextToInputConnection(String.valueOf((char) ev.chars[0]));
                    } else {
                        sendKeyChar(ev.chars[0]);
                    }
                    return true;

                default:
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (isInputViewShown()) {
                                InputViewManager inputViewManager = getInputViewManager();
                                inputViewManager.closing();
                                requestHideSelf(0);
                                return true;
                                }

                        case KeyEvent.KEYCODE_ZENKAKU_HANKAKU:
                            processKeyEvent(keyEvent);
                            return true;

                        default:
                            break;
                       }
                    break;
                }
            }

            // The processing from now on is not executed in case of input invalid.
            return false;
        }

        if (mEngineState.isSymbolList()
                && !(mEnableHardware12Keyboard && ((keyCode == KeyEvent.KEYCODE_DEL)
                        || (keyCode == KeyEvent.KEYCODE_SOFT_LEFT)))) {
            if (keyEvent != null && keyEvent.isPrintingKey() && isTenKeyCode(keyCode) && !keyEvent.isNumLockOn()) {
                // If the numeric keypad is pressed when NumLock is OFF,
                // Key events since the original notification will come, this is not done.
                return false;
            }
            switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL:
                return false;

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                OpenWnn ime = OpenWnn.getCurrentIme();
                if ((ime != null) && ime.mOriginalInputMethodSwitcher.mSymbolMode
                        && !ime.mOriginalInputMethodSwitcher.mNumericMode) {
                    getSwitcher().changeMode(
                            ime.mOriginalInputMethodSwitcher.IME_TYPE_HANDWRITING);
                } else {
                    initializeScreen();
                    if (ime != null) {
                        if ((!(ime.mOriginalInputMethodSwitcher.mNumericMode)) && (getSwitcher().getSymbolBackHangul())) {
                            getSwitcher().changeKeyboardLanguage("ko");
                            getSwitcher().setSymbolBackHangul(false);
                        }
                        ime.mOriginalInputMethodSwitcher.mNumericMode = false;
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                //select Candidate
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.selectFocusCandidate();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (candidatesViewManager.isFocusCandidate()) {
                    processLeftKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (candidatesViewManager.isFocusCandidate()) {
                    processRightKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                processDownKeyEvent();
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (candidatesViewManager.isFocusCandidate()) {
                    processUpKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_SPACE:
                if (keyEvent != null) {
                    if (keyEvent.isShiftPressed()) {
                        onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_UP));
                    } else if (keyEvent.isAltPressed()) {
                        if (keyEvent.getRepeatCount() == 0) {
                            switchSymbolList();
                        }
                    } else {
                        onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_DOWN));
                    }
                }
                return true;

            case KeyEvent.KEYCODE_SYM:
                switchSymbolList();
                return true;

            case KeyEvent.KEYCODE_PAGE_UP:
                candidatesViewManager.scrollPageAndUpdateFocus(false);
                return true;

            case KeyEvent.KEYCODE_PAGE_DOWN:
                candidatesViewManager.scrollPageAndUpdateFocus(true);
                return true;

            case KeyEvent.KEYCODE_MOVE_END:
                if (candidatesViewManager.isFocusCandidate()) {
                    processEndKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_MOVE_HOME:
                if (candidatesViewManager.isFocusCandidate()) {
                    processHomeKeyEvent();
                    return true;
                }
                return false;

            case KeyEvent.KEYCODE_SWITCH_CHARSET:
                initializeScreen();
                switchCharset();
                return true;

            case KeyEvent.KEYCODE_PICTSYMBOLS:
                if (keyEvent != null) {
                    if (keyEvent.getRepeatCount() == 0) {
                        switchSymbolList();
                    }
                }
                return true;

            case KeyEvent.KEYCODE_STAR:
                if (mEnableHardware12Keyboard) {
                    onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_UP));
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_POUND:
                if (mEnableHardware12Keyboard) {
                    onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_DOWN));
                    return true;
                }
                break;

            default:
                // Do nothing.
            }

            if ((ev.code == OpenWnnEvent.INPUT_KEY) &&
                (keyCode != KeyEvent.KEYCODE_SEARCH) &&
                (keyCode != KeyEvent.KEYCODE_ALT_LEFT) &&
                (keyCode != KeyEvent.KEYCODE_ALT_RIGHT) &&
                (keyCode != KeyEvent.KEYCODE_SHIFT_LEFT) &&
                (keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT)) {
                state = new EngineState();
                state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
                updateEngineState(state);
            }
        }

        if ((ev.code == OpenWnnEvent.INPUT_KEY) && processHardware12Keyboard(keyEvent)) {
            return true;
        }

        if (ev.code == OpenWnnEvent.LIST_CANDIDATES_FULL) {
            setCandidateIsViewTypeFull(true);
            return true;
        } else if (ev.code == OpenWnnEvent.LIST_CANDIDATES_NORMAL) {
            setCandidateIsViewTypeFull(false);
            return true;
        } // else {}

        // In case of events other than the undo command
        if (!((ev.code == OpenWnnEvent.UNDO)
              || (ev.code == OpenWnnEvent.COMMIT_COMPOSING_TEXT)
              || ((keyEvent != null)
                  && ((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT)
                      ||(keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
                      ||(keyCode == KeyEvent.KEYCODE_ALT_LEFT)
                      ||(keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
                      ||(keyEvent.isShiftPressed()
                         && (((keyCode == KeyEvent.KEYCODE_DEL) || (keyCode == KeyEvent.KEYCODE_FORWARD_DEL))
                             || (keyCode == KeyEvent.KEYCODE_SPACE)))
                      ||(keyEvent.isAltPressed() && (keyCode == KeyEvent.KEYCODE_SPACE)))))) {

            clearCommitInfo();
        }

        boolean ret = false;
        switch (ev.code) {
        case OpenWnnEvent.INPUT_CHAR:
            mFunfun = 0;
            setFunFun(mFunfun);
            if ((getPreConverter() == null) && !isEnableL2Converter()) {
                // Direct input (Full-width alphabet/number)
                commitText(false);
                commitText(new String(ev.chars));
                candidatesViewManager.clearCandidates();
                mEnableAutoDeleteSpace = false;
            } else if (!isEnableL2Converter()) {
                processSoftKeyboardCodeWithoutConversion(ev.chars);
            } else {
                processSoftKeyboardCode(ev.chars);
            }
            ret = true;
            break;

        case OpenWnnEvent.FLICK_INPUT_CHAR:
            processFlickInputChar(new String(ev.chars));
            ret = true;
            break;

        case OpenWnnEvent.TOGGLE_CHAR:
            processSoftKeyboardToggleChar(ev.toggleTable);
            ret = true;
            break;

        case OpenWnnEvent.TOGGLE_REVERSE_CHAR:
            if ((((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT)
                || ((mStatus & STATUS_AUTO_CURSOR_DONE) != 0))
                && !(mEngineState.isConvertState()) && (ev.toggleTable != null)) {

                ComposingText composingText = getComposingText();
                int cursor = composingText.getCursor(ComposingText.LAYER1);
                if (cursor > 0) {
                    StrSegment strSegment = composingText.getStrSegment(ComposingText.LAYER1, cursor - 1);
                    if (strSegment != null) {
                        String prevChar = strSegment.string;
                        if (prevChar != null) {
                            String c = searchToggleCharacter(prevChar, ev.toggleTable, true);
                            if (c != null) {
                                composingText.delete(ComposingText.LAYER1, false);
                                appendToggleString(c);
                                mFunfun = 0;
                                setFunFun(mFunfun);
                                updateViewStatusForPrediction(true, true);
                                ret = true;
                                break;
                            }
                        }
                    }
                }
            }
            break;

        case OpenWnnEvent.REPLACE_CHAR:
            ComposingText composingText = getComposingText();
            int cursor = composingText.getCursor(ComposingText.LAYER1);
            if ((cursor > 0)
                && !(mEngineState.isConvertState())) {
                StrSegment strSegment = composingText.getStrSegment(ComposingText.LAYER1, cursor - 1);
                if (strSegment != null) {
                    String search = strSegment.string;
                    if (search != null) {
                        String c = (String)ev.replaceTable.get(search);
                        if (c != null) {
                            composingText.delete(1, false);
                            appendStrSegment(new StrSegment(c));
                            mFunfun = 0;
                            setFunFun(mFunfun);
                            ret = true;
                            mStatus = STATUS_INPUT_EDIT;
                            updateViewStatusForPrediction(true, true);
                            break;
                        }
                    }
                }
            }
            break;

        case OpenWnnEvent.INPUT_KEY:
            if (keyEvent == null) {
                break;
            }

            switch (keyCode) {
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                return false;

            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT:
                composingText = getComposingText();
                if ((composingText.size(ComposingText.LAYER1) + mFunfun) < 1) {
                    return false;
                } else {
                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!candidatesViewManager.isFocusCandidate()) {
                    IWnnLanguageSwitcher switcher = getSwitcher();
                    getInputViewManager().onUpdateState(switcher);
                }
                break;

            case KeyEvent.KEYCODE_PAGE_UP:
                if (candidatesViewManager.getCurrentView().isShown()) {
                    if (mFullCandidate) {
                        candidatesViewManager.scrollPageAndUpdateFocus(false);
                    } else if (mTextCandidatesViewManager.getCanReadMore()) {
                        mTextCandidatesViewManager.setFullMode();
                    }
                    return true;
                } else {
                    if (getConvertingForFuncKeyType() != iWnnEngine.CONVERT_TYPE_NONE) {
                        return true;
                    }
                    return false;
                }

            case KeyEvent.KEYCODE_PAGE_DOWN:
                if (candidatesViewManager.getCurrentView().isShown()) {
                    if (mFullCandidate) {
                        candidatesViewManager.scrollPageAndUpdateFocus(true);
                    } else if (mTextCandidatesViewManager.getCanReadMore()) {
                        mTextCandidatesViewManager.setFullMode();
                    }
                    return true;
                } else {
                    if (getConvertingForFuncKeyType() != iWnnEngine.CONVERT_TYPE_NONE) {
                        return true;
                    }
                    return false;
                }

            case KeyEvent.KEYCODE_SWITCH_CHARSET:
                switchCharset();
                return true;

            case KeyEvent.KEYCODE_PICTSYMBOLS:
                commitAllText();
                if (mEnableEmoji) {
                    changeEngineMode(ENGINE_MODE_SYMBOL_EMOJI);
                } else {
                    if (mEnableSymbolListNonHalf) {
                        changeEngineMode(ENGINE_MODE_SYMBOL_KAO_MOJI);
                    } else {
                        changeEngineMode(ENGINE_MODE_SYMBOL_SYMBOL);
                    }
                }
                return true;

            default:
                ;// nothing to do.
                break;
            }

            /* handle other key event */
            ret = processKeyEvent(keyEvent);
            break;

        case OpenWnnEvent.INPUT_SOFT_KEY:
            ret = processKeyEvent(keyEvent);
            if (!ret) {
                sendKeyEventDirect(keyEvent);
                ret = true;
            }
            break;

        case OpenWnnEvent.SELECT_CANDIDATE:
            initCommitInfoForWatchCursor();
            if (isEnglishPrediction()) {
                getComposingText().clear();
            }
            mStatus = commitText(ev.word);
            if (isEnglishPrediction() && !mEngineState.isSymbolList() && mEnableAutoInsertSpace) {
                commitSpaceJustOne();
            }
            checkCommitInfo();

            if (mEngineState.isSymbolList()) {
                mEnableAutoDeleteSpace = false;
            }
            break;

        case OpenWnnEvent.CONVERT:
            if (mEngineState.isRenbun()) {
                if (!candidatesViewManager.isFocusCandidate()) {
                    processDownKeyEvent();
                }
                processRightKeyEvent();
                break;
            }
            startConvert(EngineState.CONVERT_TYPE_RENBUN);
            break;

        case OpenWnnEvent.COMMIT_COMPOSING_TEXT:
            commitAllText();
            break;

        case OpenWnnEvent.SELECT_WEBAPI:
            mConverterIWnn.startWebAPI(getComposingText());
            break;

        case OpenWnnEvent.SELECT_WEBAPI_GET_AGAIN:
            mConverterIWnn.startWebAPIGetAgain(getComposingText());
            break;

        case OpenWnnEvent.RESULT_WEBAPI_OK:
            mConverterIWnn.setWebAPIWordsEnabled(true);
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).remakeCandidatesWebApi();
            }
            break;

        case OpenWnnEvent.RESULT_WEBAPI_NG:
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).onWebApiError(ev.string);
            }
            break;

        case OpenWnnEvent.CALL_MUSHROOM:
            callMushRoom(ev.word);
            break;

        case OpenWnnEvent.COMMIT_INPUT_TEXT:
            commitAllText();
            initCommitInfoForWatchCursor();
            appendStrSegment(new StrSegment(new String(ev.chars)));
            commitText(true);
            checkCommitInfo();
            break;

        case OpenWnnEvent.TOGGLE_INPUT_CANCEL:
            if (mHandler.hasMessages(MSG_TOGGLE_TIME_LIMIT)) {
                mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
                processToggleTimeLimit();
            } // else {}
            ret = true;
            break;

        default:
            break;
        }

        return ret;
    }

    /**
     * update the composing text
     *
     * @param word  The focused candidate
     */
    private void updateComposingText(WnnWord word) {
        int textLayer = ComposingText.LAYER2;
        StrSegment segment = new StrSegment(word.candidate, 0,
                mConverterIWnn.getSegmentStrokeLength(mCommitCount) - 1);
        ComposingText text = getComposingText();
        text.setCursor(textLayer, 1);
        text.replaceStrSegment(textLayer, new StrSegment[]{segment}, 1);
   }

    /**
     * Auto move a cursor.
     */
    private void forwardCursor() {
        mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
        if (!mExactMatchMode) {
            ComposingText composingText = getComposingText();
            int cursor = composingText.getCursor(ComposingText.LAYER1);
            if ((mAutoCursorMovementSpeed != AUTO_CURSOR_MOVEMENT_OFF)
                    && (composingText.size(ComposingText.LAYER1) == cursor)) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TOGGLE_TIME_LIMIT),
                                            mAutoCursorMovementSpeed);
            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvaluateFullscreenMode */
    @Override public boolean onEvaluateFullscreenMode() {
        if (DEBUG) {Log.d(TAG, "onEvaluateFullscreenMode()");}
        boolean ret = false;
        if (!mEnableFullscreen ||
            (getResources().getConfiguration().hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES)) {
            ret = false;
        } else {
            ret = onEvaluateFullscreenModeOpenWnn();
        }
        return ret;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvaluateInputViewShown */
     @Override public boolean onEvaluateInputViewShown() {
        // Always show the keyboard
        return true;
    }

    /**
     * Create a <code>StrSegment</code> from a character code.
     * <br>
     * @param charCode character code
     * @return <code>StrSegment</code> created; null if an error occurs.
     */
    private StrSegment createStrSegment(int charCode) {
        if (charCode == 0 || (charCode & KeyCharacterMap.COMBINING_ACCENT) != 0 || charCode == PRIVATE_AREA_CODE) {
            return null;
        }
        return new StrSegment(Character.toChars(charCode));
    }

    /**
     * Key event handler.
     *
     * @param ev        A key event
     * @return  {@code true} if the event is handled in this method.
     */
    private boolean processKeyEvent(KeyEvent ev) {
        if (ev == null) {
            return false;
        }
        InputConnection inputConnection = getInputConnection();

        int key = ev.getKeyCode();
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        InputViewManager inputViewManager = getInputViewManager();
        WnnEngine converter = getConverter();
        int tempConvertingForFuncKeyType = getConvertingForFuncKeyType();
        boolean convertingForFuncKey = (tempConvertingForFuncKeyType != iWnnEngine.CONVERT_TYPE_NONE);
        setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_NONE);

        if (convertingForFuncKey) {
            ComposingText composingText = getComposingText();
            int len = composingText.size(ComposingText.LAYER1) + mFunfun;
            if (0 < len) {
                // Conversion of function keys, except the following key to disable.
                // F6 - F10 keys
                // BackSpace key
                // Esc key
                // Enter key
                // Text input key
                // Muhenkan key
                switch (key) {
                case KeyEvent.KEYCODE_F6:
                case KeyEvent.KEYCODE_F7:
                case KeyEvent.KEYCODE_F8:
                case KeyEvent.KEYCODE_F9:
                case KeyEvent.KEYCODE_F10:
                case KeyEvent.KEYCODE_MUHENKAN:
                    setConvertingForFuncKeyType(tempConvertingForFuncKeyType);
                    break;

                case KeyEvent.KEYCODE_DEL:
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    break;

                case KeyEvent.KEYCODE_ZENKAKU_HANKAKU:
                    setConvertingForFuncKeyType(tempConvertingForFuncKeyType);
                    return true;

                default:
                    if (ev.isPrintingKey()) {
                        break;
                    }
                    setConvertingForFuncKeyType(tempConvertingForFuncKeyType);
                    return !isThroughKeyCode(key);
                }
            } else {
                // Since there is no doubt the character of a flag is true convertingForFuncKey is inconsistent.
                // So, as a false flag operation carried out.
                convertingForFuncKey = false;
            }
        }

        if (KeyEvent.KEYCODE_ZENKAKU_HANKAKU == key) {
            // Inactive IME: IME launch
            // Active IME  : IME ends
            // In the active IME, and Alt keys pressed: Switching character types
            if (isInputViewShown()) {
                if (ev.isAltPressed()) {
                    // In the active IME, and Alt keys pressed
                    switchCharset();
                } else {
                    // Active IME
                    if (0 == ev.getRepeatCount()) {
                        inputViewManager.closing();
                        requestHideSelf(0);
                    }
                }
            } else {
                // Inactive IME
                if (0 == ev.getRepeatCount()) {
                    IWnnLanguageSwitcher switcher = getSwitcher();
                    switcher.requestShowSelf();
                }
            }
            return true;
        }

        // keys which produce a glyph
        if (ev.isPrintingKey()) {
            if (convertingForFuncKey) {
                setConvertingForFuncKeyType(tempConvertingForFuncKeyType);
            }
            if (isTenKeyCode(key) && !ev.isNumLockOn()) {
                // If the numeric keypad is pressed when NumLock is OFF,
                // in order to get the key events of the original notification, return false.
                return false;
            }
            if (ev.isCtrlPressed()){
                if (key == KeyEvent.KEYCODE_A || key == KeyEvent.KEYCODE_F || key == KeyEvent.KEYCODE_C ||
                    key == KeyEvent.KEYCODE_V || key == KeyEvent.KEYCODE_X || key == KeyEvent.KEYCODE_Y || key == KeyEvent.KEYCODE_Z) {
                    if (convertingForFuncKey) {
                       return true;
                    }
                    ComposingText composingText = getComposingText();
                    if( composingText.size(ComposingText.LAYER1) + mFunfun < 1 ) {
                      return false;
                    } else  {
                      return true;
                    }
                }
            }
            /* do nothing if the character cannot to be displayed, or the character is dead key */
            if ((ev.isAltPressed() == true && ev.isShiftPressed() == true)) {
                int charCode = ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_ALT_ON);

                if (charCode == 0 || (charCode & KeyCharacterMap.COMBINING_ACCENT) != 0 || charCode == PRIVATE_AREA_CODE) {
                    return true;
                }
            }

            commitConvertingText();

            EditorInfo edit = getCurrentInputEditorInfo();
            StrSegment str;

            /* get the key character */
            if (ev.isAltPressed() == false && ev.isShiftPressed() == false) {
                /* no meta key is locked */
                int shift = (mAutoCaps)? getShiftKeyState(edit) : 0;

                /* Kana direct mode --> Disable auto_caps */
                if (isEnableKanaInput()) {
                    shift = 0;
                }

                if (shift != 0 && (key >= KeyEvent.KEYCODE_A && key <= KeyEvent.KEYCODE_Z)) {
                    /* handle auto caps for a alphabet character */
                    str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON));
                } else {
                    /* Kana direct mode --> disable CapsLock */
                    if (isEnableKanaInput()) {
                        int charCode = ev.getUnicodeChar();
                        /* Large A(U+0041)..Z(U+0061) => Small a(U+005A)..z(U+007A) */
                        if (charCode >= CHARCODE_LARGE_A && charCode <= CHARCODE_LARGE_Z) {
                            str = createStrSegment(charCode + CHARCODE_OFFSET_LARGE_TO_SMALL);
                        } else {
                            switch (key) {
                                case KeyEvent.KEYCODE_RO:
                                    /* Keycode 217 U+005C BACKSLASH */
                                    /* yen(u00a5) -> backslash(u005c) */
                                    str = createStrSegment(92);
                                    break;

                                default:
                                    str = createStrSegment(charCode);
                                    break;
                            }
                        }
                    } else {
                        str = createStrSegment(ev.getUnicodeChar());
                    }
                }
            } else if (ev.isShiftPressed() == true && ev.isAltPressed() == false) {
                switch (key) {
                    case KeyEvent.KEYCODE_0:
                        if (isEnableKanaInput()) {
                            /* Keycode 7 U+3094 hiragana "wo" */
                            /* "0"(u308f) -> hiragana "wo"(u3092) */
                            str = createStrSegment(12434);
                            break;
                        }
                    default:
                        str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON));
                        break;
                }
            } else if (ev.isShiftPressed() == false && ev.isAltPressed() == true) {
                switch (key) {
                    case KeyEvent.KEYCODE_S:
                    case KeyEvent.KEYCODE_C:
                        //no input char
                        str = null;
                        break;
                    default:
                        str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_ALT_ON));
                        break;
                }
            } else {    // ev.isShiftPressed() == true && ev.isAltPressed() == true
                switch (key) {
                    case KeyEvent.KEYCODE_S:
                    case KeyEvent.KEYCODE_C:
                        //no input char
                        str = null;
                        break;
                    default:
                        str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON
                                                 | MetaKeyKeyListener.META_ALT_ON));
                        break;
                }
            }

            if (str == null) {
                return true;
            }

            if (convertingForFuncKey) {
                // Confirmed by a letter from, to do text input.
                initCommitInfoForWatchCursor();
                mStatus = commitText(true);
                checkCommitInfo();
                mEnableAutoDeleteSpace = false;
            }
            setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_NONE);

            mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
            ((DefaultSoftKeyboard)getInputViewManager()).setPrevInputKeyCode(key);

            /* append the character to the composing text if the character is not TAB */
            if (str.string.charAt(0) != '\u0009') { // not TAB
                processHardwareKeyboardInputChar(str, key);
                return true;
            }else{
                commitText(true);
                commitText(str.string);
                initializeScreen();
                return true;
            }

        } else if (key == KeyEvent.KEYCODE_SPACE) {
            processHardwareKeyboardSpaceKey(ev);
            return true;

        } else if (key == KeyEvent.KEYCODE_MENU && mEnableMushroom && ev.isAltPressed()) {
            callMushRoom(null);
            return true;

        } else if (key == KeyEvent.KEYCODE_SYM) {
            /* display the symbol list */
            initCommitInfoForWatchCursor();
            mStatus = commitText(true);
            checkCommitInfo();
            if (mEnableEmoji) {
                changeEngineMode(ENGINE_MODE_SYMBOL_EMOJI);

            } else {
                changeEngineMode(ENGINE_MODE_SYMBOL_SYMBOL);
            }
            return true;
        } else if (key == KeyEvent.KEYCODE_LANGUAGE_SWITCH) {
            if (isInputViewShown()) {
                switchCharset();
                return true;
            }
            return false;
        } else if (key == KeyEvent.KEYCODE_EISU) {
            if (!mEngineState.isEnglish()) {
                switchCharset();
            }
            return true;
        } else if (key == KeyEvent.KEYCODE_KANA) {
            if (mEngineState.isEnglish()) {
                switchCharset();
            }
            return true;
        } else if (key == KeyEvent.KEYCODE_KATAKANA_HIRAGANA) {
            boolean isPressedAlt = ev.isAltPressed();
            boolean isPressedShift = ev.isShiftPressed();
            boolean isPressedCtrl = ev.isCtrlPressed();
            if ((isPressedAlt && !isPressedShift && !isPressedCtrl) ||
                (isPressedAlt && isPressedShift && !isPressedCtrl)) {
                showSwitchKanaRomanModeDialog();
            } else if (mEngineState.isEnglish()) {
                switchCharset();
            }
            return true;
        } else if (key == KeyEvent.KEYCODE_HENKAN) {
            processHardwareKeyboardHenkanKey(ev);
            return true;
        } else if (key == KeyEvent.KEYCODE_MUHENKAN) {
            processHardwareKeyboardMuhenkanKey(ev);
            return true;
        } // else {}

        if (key == KeyEvent.KEYCODE_BACK) {
            KeyboardView keyboardView = ((DefaultSoftKeyboard) inputViewManager).getKeyboardView();
            if (keyboardView.handleBack()) {
                return true;
            }
        }

        ComposingText composingText = getComposingText();
        if (DEBUG) {Log.d(TAG, "mComposingText.size="+composingText.size(1));}
        // Functional key
        if ((composingText.size(ComposingText.LAYER1) + mFunfun) > 0) {
            switch (key) {
            case KeyEvent.KEYCODE_MOVE_HOME:
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_HOME);
                } else {
                    int len = composingText.getCursor(ComposingText.LAYER1);
                    int funfunlen = mFunfun;
                    for (int i = 0; i <= len + funfunlen; i++) {
                        processLeftKeyEvent();
                    }
                }
                return true;

            case KeyEvent.KEYCODE_MOVE_END:
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_END);
                } else {
                    int pos = composingText.getCursor(ComposingText.LAYER1);
                    int maxlen = composingText.toString(ComposingText.LAYER1).length();
                    if (pos != maxlen) {
                        if (mFunfun == 0) {
                            for (;;) {
                                if (mEngineState.isRenbun()) {
                                    if (pos == maxlen) {
                                        break;
                                    }
                                } else {
                                    if (pos == maxlen + 1) {
                                        break;
                                    }
                                }
                                processRightKeyEvent();
                                pos++;
                            }
                        }
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL:
                if (processDelKeyEventForUndo(ev)) {
                    return true;
                }

                mStatus = STATUS_INPUT_EDIT;
                if (mEngineState.isConvertState() || convertingForFuncKey) {
                    composingText.setCursor(ComposingText.LAYER1,
                                             composingText.toString(ComposingText.LAYER1).length());
                    mExactMatchMode = false;
                    mFunfun = 0;
                    setFunFun(mFunfun);
                } else {
                    if (mFunfun > 0) {
                        mFunfun = 0;
                        setFunFun(mFunfun);
                    } else {
                        if (key == KeyEvent.KEYCODE_FORWARD_DEL) {
                            composingText.deleteForward(ComposingText.LAYER1);
                         } else {
                             if ((composingText.size(ComposingText.LAYER1) == 1)
                                    && composingText.getCursor(ComposingText.LAYER1) != 0) {
                                initializeScreen();
                                return true;
                            } else {
                                composingText.delete(ComposingText.LAYER1, false);
                            }
                        }
                    }
                }
                updateViewStatusForPrediction(true, true); // prediction only
                return true;

            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                if (DEBUG) {Log.d(TAG, "mCandidatesViewManager.getViewType()="+candidatesViewManager.getViewType());}
                if (candidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
                    setCandidateIsViewTypeFull(false);
                } else {
                    if (((DefaultSoftKeyboardJAJP)inputViewManager).processBackKeyEvent()) {
                        return true;
                    } else if (!mEngineState.isConvertState() && !convertingForFuncKey) {
                        initializeScreen();
                        if (converter != null) {
                            converter.init(mWnnSwitcher.getFilesDirPath());
                        }
                    } else {
                        candidatesViewManager.clearCandidates();
                        mStatus = STATUS_INPUT_EDIT;
                        mFunfun = 0;
                        setFunFun(mFunfun);
                        mExactMatchMode = false;
                        composingText.setCursor(ComposingText.LAYER1,
                                                 composingText.toString(ComposingText.LAYER1).length());
                        updateViewStatusForPrediction(true, true); // clear
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!isEnableL2Converter()) {
                    commitText(false);
                    return false;
                } else {
                    processLeftKeyEvent();
                    return true;
                }

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!isEnableL2Converter()) {
                    if (mEngineState.keyboard == EngineState.KEYBOARD_12KEY) {
                        commitText(false);
                    }
                } else {
                    processRightKeyEvent();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                processDownKeyEvent();
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (candidatesViewManager.isFocusCandidate()) {
                    processUpKeyEvent();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                if (candidatesViewManager.isFocusCandidate()) {
                    candidatesViewManager.selectFocusCandidate();
                    return true;
                }
                if (!isEnglishPrediction()) {
                    int cursor = getComposingText().getCursor(ComposingText.LAYER1) + mFunfun;
                    if (cursor < 1) {
                        return true;
                    }
                }

                initCommitInfoForWatchCursor();
                mStatus = commitText(true);
                checkCommitInfo();

                if (isEnglishPrediction()) {
                    initializeScreen();
                }

                if (0 < mFunfun) {
                    initializeScreen();
                    breakSequence();
                }
                mEnableAutoDeleteSpace = false;
                return true;

            case KeyEvent.KEYCODE_F6:
                converterComposingText(iWnnEngine.CONVERT_TYPE_HIRAGANA);
                return true;

            case KeyEvent.KEYCODE_F7:
                converterComposingText(iWnnEngine.CONVERT_TYPE_KATAKANA);
                return true;

            case KeyEvent.KEYCODE_F8:
                converterComposingText(iWnnEngine.CONVERT_TYPE_HANKATA);
                return true;

            case KeyEvent.KEYCODE_F9:
                switch (getConvertingForFuncKeyType()) {
                    case iWnnEngine.CONVERT_TYPE_ZEN_EIJI_LOWER:
                        setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_ZEN_EIJI_UPPER);
                        break;

                    case iWnnEngine.CONVERT_TYPE_ZEN_EIJI_UPPER:
                        setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_ZEN_EIJI_CAP);
                        break;

                    case iWnnEngine.CONVERT_TYPE_ZEN_EIJI_CAP:
                    default:
                        setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_ZEN_EIJI_LOWER);
                        break;
                }
                converterComposingText(getConvertingForFuncKeyType());
                return true;

            case KeyEvent.KEYCODE_F10:
                switch (getConvertingForFuncKeyType()) {
                    case iWnnEngine.CONVERT_TYPE_HAN_EIJI_LOWER:
                        setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_HAN_EIJI_UPPER);
                        break;

                    case iWnnEngine.CONVERT_TYPE_HAN_EIJI_UPPER:
                        setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_HAN_EIJI_CAP);
                        break;

                    case iWnnEngine.CONVERT_TYPE_HAN_EIJI_CAP:
                    default:
                        setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_HAN_EIJI_LOWER);
                        break;
                }
                converterComposingText(getConvertingForFuncKeyType());
                return true;

            case KeyEvent.KEYCODE_TAB:
                processTabKeyEvent(ev);
                return true;

            default:
                return !isThroughKeyCode(key);
            }
        } else {
            // if there is no composing string.
            if (candidatesViewManager.getCurrentView().isShown()) {
                // displaying relational prediction candidates
                switch (key) {
                case KeyEvent.KEYCODE_MOVE_HOME:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_HOME);
                    }
                    return true;

                case KeyEvent.KEYCODE_MOVE_END:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_END);
                    }
                    return true;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
                        return true;
                    }
                    if (isEnableL2Converter()) {
                        // initialize the converter
                        converter.init(mWnnSwitcher.getFilesDirPath());
                    }
                    mStatus = STATUS_INPUT_EDIT;
                    updateViewStatusForPrediction(true, true);
                    return false;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
                        return true;
                    }
                    if (mEnableFunfun) {
                        mFunfun++;
                        setFunFun(mFunfun);
                    } else if (isEnableL2Converter()) {
                        /* initialize the converter */
                        converter.init(mWnnSwitcher.getFilesDirPath());
                    } // else {}
                    mStatus = STATUS_INPUT_EDIT;
                    updateViewStatusForPrediction(true, true);

                    if (mEnableFunfun) {
                        return true;
                    } else {
                        return false;
                    }

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    processDownKeyEvent();
                    return true;

                case KeyEvent.KEYCODE_DPAD_UP:
                    if (candidatesViewManager.isFocusCandidate()) {
                        processUpKeyEvent();
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                    if (candidatesViewManager.isFocusCandidate()) {
                        candidatesViewManager.selectFocusCandidate();
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_TAB:
                    processTabKeyEvent(ev);
                    return true;

                default:
                    break;

                }
                return processKeyEventNoInputCandidateShown(ev);
            } else {
                switch (key) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ESCAPE:
                    OpenWnn ime = OpenWnn.getCurrentIme();
                    if ((ime != null) && ime.mOriginalInputMethodSwitcher.mSymbolMode) {
                        getSwitcher().changeMode(ime.mOriginalInputMethodSwitcher.IME_TYPE_HANDWRITING);
                        return true;
                    }

                    if (((DefaultSoftKeyboardJAJP)inputViewManager).processBackKeyEvent()) {
                        return true;
                    }
                    /*
                     * If 'BACK' key is pressed when the SW-keyboard is shown
                     * and the candidates view is not shown, dismiss the SW-keyboard.
                     */
                    if (isInputViewShown()) {
                        inputViewManager.closing();
                        requestHideSelf(0);
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_DEL:
                case KeyEvent.KEYCODE_FORWARD_DEL:
                    if (processDelKeyEventForUndo(ev)) {
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (inputConnection == null) {
                        return false;
                    }
                    CharSequence afterCursor = inputConnection.getTextAfterCursor(1, 0);

                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        if (!TextUtils.isEmpty(afterCursor)
                                || ((DefaultSoftKeyboardJAJP)getInputViewManager()).getSelectionMode()) {
                            return sendNavigationKeypadKeyEvent(
                                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                        }
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (inputConnection == null) {
                        return false;
                    }
                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
                        if (!TextUtils.isEmpty(beforeCursor)
                                || ((DefaultSoftKeyboardJAJP)getInputViewManager()).getSelectionMode()) {
                            return sendNavigationKeypadKeyEvent(
                                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                        }
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_UP:
                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        return sendNavigationKeypadKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        return sendNavigationKeypadKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
                    }
                    break;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_SELECT:
                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        inputConnection = getInputConnection();
                        if (inputConnection == null) {
                            return false;
                        }
                        if (!((DefaultSoftKeyboardJAJP)getInputViewManager()).getSelectionMode()) {
                            inputConnection.setSelection(Math.max(mEditorSelectionEnd, 0),
                                                        Math.max(mEditorSelectionEnd, 0));
                            return true;
                        }
                    }
                    break;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_CLOSE:
                    inputConnection = getInputConnection();
                    inputConnection.setSelection(Math.max(mEditorSelectionEnd, 0),
                                                Math.max(mEditorSelectionEnd, 0));
                  //OpenWnn ime = OpenWnn.getCurrentIme();
                    ime = OpenWnn.getCurrentIme();
                    if ((ime != null) && ime.mOriginalInputMethodSwitcher.mNavigationMode) {
                        getSwitcher().changeMode(ime
                                .mOriginalInputMethodSwitcher.IME_TYPE_HANDWRITING);
                    }
                    return true;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_COPY:
                    inputConnection = getInputConnection();
                    inputConnection.performContextMenuAction(android.R.id.copy);
                    inputConnection.setSelection(Math.max(mEditorSelectionEnd, 0),
                                                Math.max(mEditorSelectionEnd, 0));
                    return true;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_PASTE:
                    inputConnection = getCurrentInputConnection();

                    if (((ClipboardManager)mWnnSwitcher
                            .getSystemService(Context.CLIPBOARD_SERVICE))
                                .getPrimaryClip() == null) {
                        return true;
                    }

                    ClipData.Item item = ((ClipboardManager)mWnnSwitcher
                                            .getSystemService(Context.CLIPBOARD_SERVICE))
                                                .getPrimaryClip().getItemAt(0);
                    inputConnection.commitText(item.getText(), 1);
                    return true;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_CUT:
                    inputConnection = getCurrentInputConnection();
                    inputConnection.performContextMenuAction(android.R.id.cut);
                    return true;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_HOME:
                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        if (inputConnection == null) {
                            return false;
                        }
                        if (!((DefaultSoftKeyboardJAJP)getInputViewManager()).getSelectionMode()) {
                            inputConnection.sendKeyEvent(new
                                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
                            inputConnection.sendKeyEvent(new
                                    KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_HOME));
                            return true;
                        } else {
                            return sendNavigationKeypadKeyEvent(
                                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
                        }
                    }
                    break;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_END:
                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        if (inputConnection == null) {
                            return false;
                        }
                        if (!((DefaultSoftKeyboardJAJP)getInputViewManager()).getSelectionMode()) {
                            inputConnection.sendKeyEvent(new
                                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
                            inputConnection.sendKeyEvent(new
                                    KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_END));
                            return true;
                        } else {
                            return sendNavigationKeypadKeyEvent(
                                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
                        }
                    }
                    break;

                case DefaultSoftKeyboard.KEYCODE_KEYPAD_ALL:
                    if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyboardType()
                            == DefaultSoftKeyboard.KEYBOARD_NAVIGATION) {
                        if (inputConnection == null) {
                            return false;
                        }
                        inputConnection.setSelection(0, 0);
                        inputConnection.performContextMenuAction(android.R.id.selectAll);
                        return true;
                    }
                    break;

                default:
                    break;
                }
            }
        }

        return false;
    }

    /**
     * Handle the space key event from the Hardware keyboard.
     *
     * @param ev  The space key event
     */
    private void processHardwareKeyboardSpaceKey(KeyEvent ev) {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        /* H/W space key */
        if (ev.isShiftPressed()) {
            switchCharset();
        } else if(ev.isAltPressed()){
            commitAllText();
            if (mEnableEmoji) {
                changeEngineMode(ENGINE_MODE_SYMBOL_EMOJI);

            } else {
                changeEngineMode(ENGINE_MODE_SYMBOL_SYMBOL);
            }
        } else if (isEnglishPrediction()) {
            /* Auto commit when English mode */
            if (getComposingText().size(0) == 0) {
                commitText(" ");
                candidatesViewManager.clearCandidates();
                breakSequence();
            } else {
                initCommitInfoForWatchCursor();
                commitText(true);
                commitSpaceJustOne();
                checkCommitInfo();
            }
            mEnableAutoDeleteSpace = false;
        } else if (mEngineState.isRenbun()) {
            if (!candidatesViewManager.isFocusCandidate()) {
                processDownKeyEvent();
            }
            processRightKeyEvent();
        } else {
            /* start a consecutive clause conversion when Japanese mode */
            if (getComposingText().size(0) == 0) {
                if (mEngineState.isEnglish()) {
                    commitText(" ");
                } else {
                    commitText("\u3000");
                }
                candidatesViewManager.clearCandidates();
                breakSequence();
            } else {
                startConvert(EngineState.CONVERT_TYPE_RENBUN);
            }
        }
    }

    /**
     * Handle the henkan key event from the Hardware keyboard.
     *
     * @param ev  The henkan key event
     */
    private void processHardwareKeyboardHenkanKey(KeyEvent ev) {
        /* H/W henkan key */
        if (ev == null) {
            return;
        } // else {}

        if (ev.isShiftPressed() || ev.isAltPressed() || ev.isCtrlPressed() || mEngineState.isEnglish()) {
            return;
        } // else {}

        if (mEngineState.isRenbun()) {
            CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
            if (!candidatesViewManager.isFocusCandidate()) {
                processDownKeyEvent();
            }
            processRightKeyEvent();
        } else if (getComposingText().size(0) != 0) {
            /* start a consecutive clause conversion when Japanese mode */
            startConvert(EngineState.CONVERT_TYPE_RENBUN);
        } // else {}
    }


    /**
     * Handle the muhenkan key event from the Hardware keyboard.
     *
     * @param ev  The muhenkan key event
     */
    private void processHardwareKeyboardMuhenkanKey(KeyEvent ev) {
        /* H/W muhenkan key */
        if (ev == null) {
            return;
        } // else {}

        boolean isShift = ev.isShiftPressed();
        if (!ev.isAltPressed() && !ev.isCtrlPressed()) {
            if (getComposingText().size(0) == 0) {
                boolean isEnglish = mEngineState.isEnglish();
                if ((isShift && !isEnglish) || (!isShift && isEnglish)) {
                    switchCharset();
                } // else {}
            } else {
                int convertingForFuncKeyType = getConvertingForFuncKeyType();
                if (isShift) {
                    switch (convertingForFuncKeyType) {
                    case iWnnEngine.CONVERT_TYPE_ZEN_EIJI_CAP:
                    case iWnnEngine.CONVERT_TYPE_ZEN_EIJI_UPPER:
                    case iWnnEngine.CONVERT_TYPE_ZEN_EIJI_LOWER:
                        converterComposingText(iWnnEngine.CONVERT_TYPE_HAN_EIJI_LOWER);
                        break;

                    default:
                        converterComposingText(iWnnEngine.CONVERT_TYPE_ZEN_EIJI_LOWER);
                        break;
                    }
                } else {
                    switch (convertingForFuncKeyType) {
                    case iWnnEngine.CONVERT_TYPE_NONE:
                    case iWnnEngine.CONVERT_TYPE_HIRAGANA:
                        converterComposingText(iWnnEngine.CONVERT_TYPE_KATAKANA);
                        break;

                    case iWnnEngine.CONVERT_TYPE_KATAKANA:
                        converterComposingText(iWnnEngine.CONVERT_TYPE_HANKATA);
                        break;

                    default:
                        converterComposingText(iWnnEngine.CONVERT_TYPE_HIRAGANA);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Handle the character code from the hardware keyboard except the space key.
     *
     * @param str  The input character
     * @param key  KeyCode
     */
    private void processHardwareKeyboardInputChar(StrSegment str, int key) {
        LetterConverter preConverter;
        if (isEnableKanaInput() && isFullTenKeyCode(key)) {
            preConverter = mPreConverterKanaRoman;
        } else {
            preConverter = getPreConverter();
        }

        if (isEnableL2Converter()) {
            boolean commit = false;
            if (preConverter == null) {
                Matcher m = mEnglishAutoCommitDelimiter.matcher(str.string);
                if (m.matches()) {
                    commitText(true);
                    commit = true;
                }
                appendStrSegment(str);
            } else {
                appendStrSegment(str);
                preConverter.convert(getComposingText());
            }

            if (commit) {
                commitText(true);
            } else {
                mStatus = STATUS_INPUT;
                mFunfun = 0;
                setFunFun(mFunfun);
                updateViewStatusForPrediction(true, true);
            }
        } else {
            appendStrSegment(str);
            boolean completed = true;
            if (preConverter != null) {
                completed = preConverter.convert(getComposingText());
            }

            if (completed) {
                if (isEnableKanaInput()) {
                    /* Direct kana input mode is not commit */
                    commitTextWithoutLastKana();
                } else {
                    if (!mEngineState.isEnglish()) {
                        commitTextWithoutLastAlphabet();
                    } else {
                        commitText(false);
                    }
                }
            } else {
                updateViewStatus(ComposingText.LAYER1, false, true);
            }
        }
    }

    /**
     * Update candidates.
     */
    private void updatePrediction() {
        int candidates = 0;
        ComposingText composingText = getComposingText();
        int cursor = composingText.getCursor(ComposingText.LAYER1);
        WnnEngine converter = getConverter();

        if (isEnableL2Converter() || mEngineState.isSymbolList()) {
            CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
            if (candidatesViewManager instanceof TextCandidatesViewManager) {
                ((TextCandidatesViewManager) candidatesViewManager).cancelLastSetCandiateEvent();
            }
            mConverterIWnn.setEnableHeadConversion(mEnableHeadConv);
            if (mExactMatchMode) {
                // exact matching
                mConverterIWnn.setEnableHeadConversion(false);
                candidates = converter.predict(composingText, 0, cursor);
            } else {
                if (mEnableFunfun && mFunfun > 0) {
                    // wildcard prediction
                    if (mFunfun < 4) {
                        // try exact matching
                        candidates = converter.predict(composingText, cursor + mFunfun,
                                                        cursor + mFunfun);
                    }
                    if (candidates <= 0) {
                        // try prefix matching
                        while ((candidates = converter.predict(composingText,
                                                                cursor + mFunfun, -1)) <= 0) {
                            // if there is no candidates, decrement a wildcard prediction length.
                            if (--mFunfun == 0) {
                                setFunFun(mFunfun);
                                candidates = converter.predict(composingText, 0, -1);
                                break;
                            }
                        }
                    }
                } else {
                    // normal prediction
                    candidates = converter.predict(composingText, 0, -1);
                }
            }
        } else {
            mFunfun = 0;
            setFunFun(mFunfun);
        }

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        // update the candidates view
        if (candidates > 0) {
            mHasContinuedPrediction = (((composingText.size(ComposingText.LAYER1) + mFunfun) == 0)
                                       && !mEngineState.isSymbolList());
            candidatesViewManager.setEnableCandidateLongClick(true);
            candidatesViewManager.displayCandidates(converter);
        } else {
            candidatesViewManager.clearCandidates();
        }
    }

    /**
     * Handle a left key event.
     */
    private void processLeftKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        if (candidatesViewManager.isFocusCandidate()) {
            candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
            return;
        }

        ComposingText composingText = getComposingText();
        if (mEngineState.isConvertState()) {
            if (mEngineState.isEisuKana()) {
                mExactMatchMode = true;
            }

            if (1 < composingText.getCursor(ComposingText.LAYER1)) {
                composingText.moveCursor(ComposingText.LAYER1, -1);
            }
        } else if (mExactMatchMode) {
            composingText.moveCursor(ComposingText.LAYER1, -1);
        } else {
            if (mEnableFunfun && mFunfun > 0) {
                mFunfun--;
                setFunFun(mFunfun);
            } else {
                if (isEnglishPrediction()) {
                    composingText.moveCursor(ComposingText.LAYER1, -1);
                } else {
                    mExactMatchMode = true;
                }
            }
        }

        mCommitCount = 0; // Clear a commit count, to start reconverting.
        mStatus = STATUS_INPUT_EDIT;
        updateViewStatus(mTargetLayer, true, true);
    }

    /**
     * Handle a right key event.
     */
    private void processRightKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        if (candidatesViewManager.isFocusCandidate()) {
            candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
            return;
        }

        int layer = mTargetLayer;
        ComposingText composingText = getComposingText();
        if (mExactMatchMode || (mEngineState.isConvertState())) {
            int textSize = composingText.size(ComposingText.LAYER1);
            if (composingText.getCursor(ComposingText.LAYER1) == textSize) {
                mExactMatchMode = false;
                mFunfun = 0;
                setFunFun(mFunfun);
                layer = ComposingText.LAYER1; // convert -> prediction

                EngineState state = new EngineState();
                state.convertType = EngineState.CONVERT_TYPE_NONE;
                updateEngineState(state);
            } else {
                if (mEngineState.isEisuKana()) {
                    mExactMatchMode = true;
                }
                composingText.moveCursor(ComposingText.LAYER1, 1);
            }
        } else {
            if (composingText.getCursor(ComposingText.LAYER1)
                    < composingText.size(ComposingText.LAYER1)) {
                composingText.moveCursor(ComposingText.LAYER1, 1);
            } else if (mEnableFunfun) {
                String composingString = composingText.toString(ComposingText.LAYER1);

                if (!isEnglishPrediction()
                        && (mIsInputSequenced || !isAlphabetAll(composingString))) {
                    int size = composingString.length();
                    int count = 0;
                    boolean change = false;
                    boolean infront = false;
                    for (count = 0 ; count < size ; count++ ) {
                        if (isAlphabetLast(composingString)) {
                            int last = composingString.length();
                            change = false;
                            char a = composingString.charAt(last - 1);
                            if (a == "n".charAt(0) || a == "N".charAt(0)){
                                change = true;
                            } else if (a == "y".charAt(0) || a == "Y".charAt(0)) {
                                infront = true;
                            } else {
                                infront = false;
                            }
                            //When the end of line is a consonant, deletes it.
                            composingText.deleteStrSegment(ComposingText.LAYER1, last - 1, last - 1);
                            composingText.setCursor(ComposingText.LAYER1, last);
                        } else {
                            break;
                        }
                        composingString = composingText.toString(ComposingText.LAYER1);
                    }

                    int clearedSize = composingString.length();
                    if ((0 < size) && (0 == clearedSize)) {
                        mIgnoreCursorMove = true;
                    }

                    //Insert hiragana's "n", when the last deleted consonant is "n" or "N".
                    //However do nothing, if it's "ny" or "NY".
                    if (change && !infront) {
                        String str = "\u3093";
                        ComposingText text = composingText;
                        text.insertStrSegment(0, ComposingText.LAYER1, new StrSegment(str));
                    }

                }
                mFunfun++;
                setFunFun(mFunfun);
            } // else {}
        }

        mCommitCount = 0; // Clear a commit count, to start reconverting.
        mStatus = STATUS_INPUT_EDIT;

        updateViewStatus(layer, true, true);
    }

    /**
     * Handle a down key event.
     */
    private void processDownKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
    }

    /**
     * Handle a up key event.
     */
    private void processUpKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
    }

    /**
     * Handle a home key event.
     */
    private void processHomeKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_HOME);
    }

    /**
     * Handle a end key event.
     */
    private void processEndKeyEvent() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        candidatesViewManager.processMoveKeyEvent(KeyEvent.KEYCODE_MOVE_END);
    }

    /**
     * Process a input keycode. [DEL] [Undo Only processing]
     *
     * @param ev        A key event
     * @return {@code true}:True when event is digested; {@code false}:if not
     */
    private boolean processDelKeyEventForUndo(KeyEvent ev){
        //Undo, if the Key event is "Shift+Del"
        if (ev.isShiftPressed()) {
            if (undo()) {
                return true;
            }
        }
        clearCommitInfo();
        return false;
    }

    /**
     * Handle a key event which is not right or left key when the
     * composing text is empty and some candidates are shown.
     *
     * @param ev        A key event
     * @return          {@code true} if this consumes the event; {@code false} if not.
     */
    boolean processKeyEventNoInputCandidateShown(KeyEvent ev) {
        boolean ret = true;
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        WnnEngine converter = getConverter();
        int key = ev.getKeyCode();

        switch (key) {
        case KeyEvent.KEYCODE_DEL:
        case KeyEvent.KEYCODE_FORWARD_DEL:
            if (processDelKeyEventForUndo(ev)) {
                return ret;
            }

            // Only close the candidate window.
            break;
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_NUMPAD_ENTER:
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_MENU:
            ret = false;
            break;

        case KeyEvent.KEYCODE_DPAD_CENTER:
            break;

        case KeyEvent.KEYCODE_BACK:
        case KeyEvent.KEYCODE_ESCAPE:
            if (candidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
                setCandidateIsViewTypeFull(false);
                return true;
            } else if (((DefaultSoftKeyboardJAJP)getInputViewManager()).processBackKeyEvent()) {
                return true;
            } else {
                ret = true;
            }
            break;

        default:
            return !isThroughKeyCode(key);
        }

        if (converter != null) {
            // initialize the converter
            converter.init(mWnnSwitcher.getFilesDirPath());
        }
        updateViewStatusForPrediction(true, true);
        return ret;
    }

    /**
     * Send key events (DOWN and UP) to the text field directly.
     *
     * @param keyEvent  Key event
     */
    private void sendKeyEventDirect(KeyEvent keyEvent) {
        if (keyEvent == null) {
            return;
        }
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER || keyEvent.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
            sendKeyChar('\n');
        } else {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                InputConnection ic = getCurrentInputConnection();
                if (null != ic) {
                    ic.sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
                }
            } else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP){
                InputConnection ic = getCurrentInputConnection();
                if (null != ic) {
                    ic.sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
                }
            } else {
                mWnnSwitcher.sendDownUpKeyEvents(keyEvent.getKeyCode());
            }
        }
    }

    /**
     * Update views and the display of the composing text for predict mode.
     *
     * @param updateCandidates  {@code true} - to update the candidates view.
     *                          {@code false} - the candidates view is not updated.
     * @param updateEmptyText   {@code true} - to update always.
     *                          {@code false} - to update the composing text if it is not empty.
     */
    private void updateViewStatusForPrediction(boolean updateCandidates, boolean updateEmptyText) {
        EngineState state = new EngineState();
        state.convertType = EngineState.CONVERT_TYPE_NONE;
        updateEngineState(state);

        updateViewStatus(ComposingText.LAYER1, updateCandidates, updateEmptyText);
    }

    /**
     * Update views and the display of the composing text.
     *
     * @param layer             Display layer of the composing text.
     * @param updateCandidates  {@code true} - to update the candidates view.
     *                          {@code false} - the candidates view is not updated.
     * @param updateEmptyText   {@code true} - to update always.
     *                          {@code false} - to update the composing text if it is not empty.
     */
    @Override protected void updateViewStatus(int layer, boolean updateCandidates, boolean updateEmptyText) {
        mTargetLayer = layer;
        ComposingText composingText = getComposingText();

        if (updateCandidates) {
            updateCandidateView();
            if (mFullCandidate) {
                mFullCandidate = false;
                updateFullscreenMode();
            }
        }
        // notice to the input view
        IWnnLanguageSwitcher switcher = getSwitcher();
        getInputViewManager().onUpdateState(switcher);

        // set the text for displaying as the composing text
        mDisplayText.clear();
        mDisplayText.insert(0, composingText.toString(layer));
        StringBuffer funfunText = new StringBuffer(mDisplayText.toString());

        for (int i = 0; i < mFunfun; i++) {
            funfunText.append("\u25cb");
        }
        if (mFunfun > 0 ){
            switcher.setFunfunText(funfunText.toString());
        }

        // add decoration to the text
        int cursor = composingText.getCursor(layer);

        // TODO: Examination is required; for null check.
        //       there is a case that "mInputConnection (getCurrentInputConnection()) == null" on new SDK.
        InputConnection inputConnection = getInputConnection();
        if ((inputConnection != null) && (mDisplayText.length() != 0 || updateEmptyText)) {
            if (!isLatticeInputMode()) {
                // text color in the highlight
                mDisplayText.setSpan(SPAN_TEXTCOLOR, 0,
                                     mDisplayText.length(),
                                     Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (cursor != 0) {
                int highlightEnd = 0;

                if (((mExactMatchMode && (!mEngineState.isEisuKana()))
                    || (isEnglishPrediction()
                        && (cursor < composingText.size(ComposingText.LAYER1))))
                    && (getConvertingForFuncKeyType() == iWnnEngine.CONVERT_TYPE_NONE)){

                    mDisplayText.setSpan(SPAN_EXACT_BGCOLOR_HL, 0, cursor,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    highlightEnd = cursor;

                } else if (mEngineState.isEisuKana()) {
                    mDisplayText.setSpan(SPAN_EISUKANA_BGCOLOR_HL, 0, cursor,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    highlightEnd = cursor;

                } else if (layer == ComposingText.LAYER2) {
                    CharSequence firstSegment = convertComposingToCommitText(
                            composingText, ComposingText.LAYER2, 0, 0);
                    if (mEnableDecoEmoji) {
                        CharSequence composingString = convertComposingToCommitText(
                                composingText, ComposingText.LAYER2);
                        mDisplayText.clear();
                          if(composingString.length() == 0) {
                                  CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
                                  ArrayList<TextView> mTextViewArray = candidatesViewManager.getTextViewArray1st();
                                  TextView tv = mTextViewArray.get(0);
                                  composingString = tv.getText();
                                  }
                          mDisplayText.insert(0, composingString);
                    }
                    highlightEnd = firstSegment.length();

                    // highlights the first segment
                    mDisplayText.setSpan(SPAN_CONVERT_BGCOLOR_HL, 0,
                                         highlightEnd,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if ((mAutoCursorMovementSpeed != AUTO_CURSOR_MOVEMENT_OFF)
                        && (mStatus == STATUS_INPUT)
                        && (mHandler.hasMessages(MSG_TOGGLE_TIME_LIMIT))) {
                    mDisplayText.setSpan(mSpanToggleCharacterBgHl, cursor - 1, cursor,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mDisplayText.setSpan(mSpanToggleCharacterText, cursor - 1, cursor,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } // else {}

                if (highlightEnd != 0) {
                    // highlights remaining text
                    mDisplayText.setSpan(SPAN_REMAIN_BGCOLOR_HL, highlightEnd,
                                         mDisplayText.length(),
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // text color in the highlight
                    mDisplayText.setSpan(SPAN_CONVERT_TEXTCOLOR, 0,
                                         mDisplayText.length(),
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            if (!isLatticeInputMode()) {
                mDisplayText.setSpan(SPAN_UNDERLINE, 0, mDisplayText.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            int displayCursor = ((cursor + mFunfun) == 0) ?  0 : 1;

            // update the composing text on the EditView
            if ((mDisplayText.length() != 0) || !mHasStartedTextSelection) {
                inputConnection.setComposingText(mDisplayText, displayCursor);
            }
        }
    }

    /**
     * Update the candidates view.
     */
    private void updateCandidateView() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();

        switch (mTargetLayer) {
        case ComposingText.LAYER0:
        case ComposingText.LAYER1: // prediction
            if (mEnablePrediction || mEngineState.isSymbolList() || mEngineState.isEisuKana()) {
                // update the candidates view
                updatePrediction();
            } else {
                candidatesViewManager.clearCandidates();
            }
            break;
        case ComposingText.LAYER2: // convert
            WnnEngine converter = getConverter();
            ComposingText composingText = getComposingText();
            if (mCommitCount == 0) {
                converter.convert(composingText);
            }

            int candidates = converter.makeCandidateListOf(mCommitCount);

            if (candidates != 0) {
                composingText.setCursor(ComposingText.LAYER2, 1);
                boolean isExistOtherSegment = (composingText.size(ComposingText.LAYER2) > 1);
                candidatesViewManager.setEnableCandidateLongClick(!isExistOtherSegment);
                candidatesViewManager.displayCandidates(converter);
            } else {
                composingText.setCursor(ComposingText.LAYER1,
                                         composingText.toString(ComposingText.LAYER1).length());
                candidatesViewManager.clearCandidates();
            }
            break;
        default:
            break;
        }
    }

    /**
     * Commit the displaying composing text.
     *
     * @param learn  {@code true} to register the committed string to the learning dictionary.
     * @return          IME's status after committing
     */
    private int commitText(boolean learn) {
        ComposingText composingText = getComposingText();
        if (isEnglishPrediction()) {
            // Full text character fixation in case of English input.
            composingText.setCursor(ComposingText.LAYER1,
                                     composingText.size(ComposingText.LAYER1));
        }

        int layer = mTargetLayer;
        int cursor = composingText.getCursor(layer);
        String tmp = composingText.toString(layer, 0, cursor - 1);

        if (getConverter() != null) {
            if (learn) {
                if (mEngineState.isRenbun()) {
                    learnWord(null);
                } else {
                    if (composingText.size(ComposingText.LAYER1) != 0) {
                        WnnWord word = new WnnWord(0, tmp, tmp,
                                                   iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN);
                        learnWord(word);
                    }
                }
            } else {
                breakSequence();
            }
        }
        if (!isEnglishPrediction()) {
            mStrokeOfCommitText = composingText.toString(ComposingText.LAYER1,
                    0, composingText.getCursor(ComposingText.LAYER1) - 1);
        }

        CharSequence commitText = convertComposingToCommitText(
                composingText, layer, 0, cursor - 1);
        return commitComposingText(commitText);
    }

    /**
     * Commit the displaying composing text without last alphabet.
     */
    private void commitTextWithoutLastAlphabet() {
        int layer = mTargetLayer;
        ComposingText composingText = getComposingText();
        StrSegment strSegment = composingText.getStrSegment(layer, -1);
        if (strSegment != null) {
            String tmp = strSegment.string;

            if (tmp != null && isAlphabetLast(tmp)) {
                composingText.moveCursor(ComposingText.LAYER1, -1);
                commitText(false);
                composingText.moveCursor(ComposingText.LAYER1, 1);
            } else {
                commitText(false);
            }
        }
    }

    /**
     * Commit the displaying composing text without last Kana.
     */
    private void commitTextWithoutLastKana() {
        int layer = mTargetLayer;
        ComposingText composingText = getComposingText();
        StrSegment strSegment = composingText.getStrSegment(layer, -1);
        if (composingText.size(ComposingText.LAYER1) > 1) {
            if (strSegment != null) {
                String tmp = strSegment.string;
                if (tmp != null) {
                    composingText.moveCursor(ComposingText.LAYER1, -1);
                    commitText(false);
                    composingText.moveCursor(ComposingText.LAYER1, 1);
                } else {
                    commitText(false);
                }
            }
        } else {
            updateViewStatus(ComposingText.LAYER1, false, true);
        }
    }

    /**
     * Commit all uncommitted words.
     */
    private void commitAllText() {
        initCommitInfoForWatchCursor();
        if (mEngineState.isConvertState()) {
            commitConvertingText();
        } else {
            ComposingText composingText = getComposingText();
            composingText.setCursor(ComposingText.LAYER1,
                                     composingText.size(ComposingText.LAYER1));
            mStatus = commitText(true);
        }
        checkCommitInfo();
    }

    /**
     * Commit a word.
     *
     * @param word              A word to commit
     * @return                  IME's status after committing
     */
    private int commitText(WnnWord word) {
        if (getConverter() != null) {
            learnWord(word);
        }
        if (!isEnglishPrediction()) {
            mStrokeOfCommitText = word.stroke;
        }

        int mode = word.getSymbolMode();
        if ((mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_SYMBOL)
                || (mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_EMOJI)
                || (mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_DECOEMOJI)
                || (mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_MIXED_EMOJI)) {

            mConverterIWnn.init(mWnnSwitcher.getFilesDirPath());
        }

        return commitComposingText(DecoEmojiUtil.getSpannedCandidate(word));
    }

    /**
     * Commit a string.
     *
     * @param str  A string to commit
     */
    private void commitText(String str) {
        mFunfun = 0;
        setFunFun(mFunfun);
        // Through YOMI of committed text for APP
        commitTextToInputConnection(str);

        mPrevCommitText.append(str);
        mIgnoreCursorMove = true;
        mEnableAutoDeleteSpace = true;
        mIsInputSequenced = true;

        updateViewStatusForPrediction(false, false);
        // for debug
        //mComposingText.debugout();
    }

    /**
     * Commit a string through {@link android.view.inputmethod.InputConnection}.
     *
     * @param string  A string to commit
     * @return                  IME's status after committing
     */
    private int commitComposingText(CharSequence string) {
        int layer = mTargetLayer;
        ComposingText composingText = getComposingText();

        if (0 < string.length()) {
            calculateSizeOfDeleteStroke();
            // Through YOMI of committed text for APP
            commitTextToInputConnection(string);
            mPrevCommitText.append(string);
            mIgnoreCursorMove = true;

            int cursor = composingText.getCursor(layer);
            if (cursor > 0) {
                int check = cursor - mSizeOfDeleteStroke;
                if ((mSizeOfDeleteStroke > 0) && (check > 0) && mEnableHeadConv) {
                    composingText.deleteStrSegment(layer, 0, mSizeOfDeleteStroke - 1);
                } else {
                    composingText.deleteStrSegment(layer, 0, composingText.getCursor(layer) - 1);
                }
                composingText.setCursor(layer, composingText.size(layer));
            }
            mExactMatchMode = false;
            mFunfun = 0;
            setFunFun(mFunfun);
            mCommitCount++;

            if ((layer == ComposingText.LAYER2) && (composingText.size(layer) == 0)) {
                layer = 1; // for connected prediction
            }

            boolean committed = false;
            if (string.length() == 1) {
                committed = autoCommitEnglish();
            }
            if ((mEnableAutoInsertSpace == false) && (EngineState.LANGUAGE_EN == mEngineState.language) && (!mEngineState.isSymbolList())) {
                committed = true;
                getCandidatesViewManager().clearCandidates();
            }
            mEnableAutoDeleteSpace = true;
            mIsInputSequenced = true;

            if (layer == ComposingText.LAYER2) {
                EngineState state = new EngineState();
                state.convertType = EngineState.CONVERT_TYPE_RENBUN;
                updateEngineState(state);

                updateViewStatus(layer, !committed, false);
            } else {
                updateViewStatusForPrediction(!committed, false);
            }
        }

        if (composingText.size(ComposingText.LAYER0) == 0) {
            return STATUS_INIT;
        } else {
            return STATUS_INPUT_EDIT;
        }
    }

    /**
     * Returns whether it is English prediction mode or not.
     *
     * @return  {@code true} if it is English prediction mode; otherwise, {@code false}.
     */
    private boolean isEnglishPrediction() {
        return (mEngineState.isEnglish() && isEnableL2Converter());
    }

    /**
     * Change the conversion engine and the letter converter(Romaji-to-Kana converter).
     *
     * @param mode  Engine's mode to be changed
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnnEvent.Mode
     */
    private void changeEngineMode(int mode) {
        EngineState state = new EngineState();

        switch (mode) {
        case ENGINE_MODE_OPT_TYPE_QWERTY:
            state.keyboard = EngineState.KEYBOARD_QWERTY;
            updateEngineState(state);
            clearCommitInfo();
            return;

        case ENGINE_MODE_OPT_TYPE_12KEY:
            state.keyboard = EngineState.KEYBOARD_12KEY;
            updateEngineState(state);
            clearCommitInfo();
            return;

        case ENGINE_MODE_EISU_KANA:
            if (mEngineState.isEisuKana()) {
                state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
                updateEngineState(state);
                updateViewStatusForPrediction(true, true); // prediction only
            } else {
                startConvert(EngineState.CONVERT_TYPE_EISU_KANA);
            }
            return;

        case ENGINE_MODE_SYMBOL:
            if (mEnableSymbolList && !isDirectInputMode()) {
                mFunfun = 0;
                setFunFun(mFunfun);
                state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL;
                updateEngineState(state);
                updateViewStatusForPrediction(true, true);
            }
            return;

        case ENGINE_MODE_SYMBOL_EMOJI:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_EMOJI);
            return;

        case ENGINE_MODE_SYMBOL_EMOJI_UNI6:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_EMOJI_UNI6);
            return;

        case ENGINE_MODE_SYMBOL_DECOEMOJI:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_DECOEMOJI);
            return;

        case ENGINE_MODE_SYMBOL_SYMBOL:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_SYMBOL);
            return;

        case ENGINE_MODE_SYMBOL_KAO_MOJI:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_KAO_MOJI);
            return;

        case ENGINE_MODE_SYMBOL_ADD_SYMBOL:
            changeSymbolEngineState(state, IWnnSymbolEngine.MODE_ADD_SYMBOL);
            return;

        default:
            // The processing from now on is executed only for other modes.
            break;
        }

        state = new EngineState();
        state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
        updateEngineState(state);

        state = new EngineState();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        Resources res = getResources();
        switch (mode) {
        /* Full/Half-width number or Full-width alphabet */
        case OpenWnnEvent.Mode.DIRECT:
            setConverter(null);
            setPreConverter(null);
            break;

        case OpenWnnEvent.Mode.NO_LV1_CONV:
            /* no Romaji-to-Kana conversion (=English prediction mode) */
            state.language = EngineState.LANGUAGE_EN;
            updateEngineState(state);
            setConverter(mConverterIWnn);
            setPreConverter(null);

            mEnableFunfun = pref.getBoolean(OPT_FUNFUN_KEY, res.getBoolean(R.bool.opt_funfun_default_value));
            mEnableLearning   = pref.getBoolean(OPT_ENABLE_LEARNING_KEY, res.getBoolean(R.bool.opt_enable_learning_default_value));
            mEnablePrediction = pref.getBoolean(OPT_PREDICTION_KEY, res.getBoolean(R.bool.opt_prediction_default_value));
            mEnableSpellCorrection = pref.getBoolean(OPT_SPELL_CORRECTION_KEY, res.getBoolean(R.bool.opt_spell_correction_default_value));

            if (!mEnablePrediction) {
                mEnableFunfun = false;
            }

            if(mAttribute != null){
                switch (mAttribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
                case EditorInfo.TYPE_CLASS_TEXT:
                    switch (mAttribute.inputType & EditorInfo.TYPE_MASK_FLAGS) {
                    case EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS:
                        mEnablePrediction = false;
                        mEnableFunfun     = false;
                        mEnableLearning   = false;
                        mEnableSpellCorrection = false;
                        break;
                    default:
                        break;
                      }
                default:
                    break;
                  }
              }
            break;

        case OpenWnnEvent.Mode.NO_LV2_CONV: // Unused
            setConverter(null);
            mPreConverterKanaRoman = mPreConverterHiragana;
            mPreConverterKanaDirect = mPreConverterHiraganaDirect;
            break;

        case ENGINE_MODE_FULL_KATAKANA:
            state.language = EngineState.LANGUAGE_JP;
            updateEngineState(state);

            setConverter(null);
            mPreConverterKanaRoman = mPreConverterFullKatakana;
            mPreConverterKanaDirect = mPreConverterFullKatakana;
            break;

        case ENGINE_MODE_HALF_KATAKANA:
            state.language = EngineState.LANGUAGE_JP;
            updateEngineState(state);

            setConverter(null);
            mPreConverterKanaRoman = mPreConverterHalfKatakana;
            mPreConverterKanaDirect = mPreConverterHalfKatakana;
            break;

        default:
            /* HIRAGANA input mode */
            state.language = EngineState.LANGUAGE_JP;
            updateEngineState(state);

            setConverter(mConverterIWnn);
            mPreConverterKanaRoman = mPreConverterHiragana;
            mPreConverterKanaDirect = mPreConverterHiraganaDirect;

            mEnableFunfun = pref.getBoolean(OPT_FUNFUN_KEY, res.getBoolean(R.bool.opt_funfun_default_value));
            mEnableLearning   = pref.getBoolean(OPT_ENABLE_LEARNING_KEY, res.getBoolean(R.bool.opt_enable_learning_default_value));
            mEnablePrediction = pref.getBoolean(OPT_PREDICTION_KEY, res.getBoolean(R.bool.opt_prediction_default_value));
            mEnableSpellCorrection = pref.getBoolean(OPT_SPELL_CORRECTION_KEY, res.getBoolean(R.bool.opt_spell_correction_default_value));

            if (!mEnablePrediction) {
                mEnableFunfun = false;
            }
            break;
        }

        if (!(mode == OpenWnnEvent.Mode.DIRECT || mode == OpenWnnEvent.Mode.NO_LV1_CONV)) {
            if (isEnableKanaInput()) {
                setPreConverter(mPreConverterKanaDirect);
            } else {
                if (!mEngineState.isEnglish()) {
                    setPreConverter(mPreConverterKanaRoman);
                }
            }
        }

        mPreConverterBack = getPreConverter();
        mConverterBack = getConverter();
    }

    /**
     * Update the conversion engine's state.
     *
     * @param state  Engine's state to be updated. See the {@link EngineState} class.
     */
    private void updateEngineState(EngineState state) {
        if (DEBUG) {Log.d(TAG, "updateEngineState()");}

        EngineState myState = mEngineState;

        // Language setting
        if (state.language != EngineState.INVALID) {
            myState.language = state.language;
            setDictionary();
            breakSequence();

            /* update keyboard setting */
            if (state.keyboard == EngineState.INVALID) {
                state.keyboard = myState.keyboard;
            }
        }

        // Conversion type setting
        if ((state.convertType != EngineState.INVALID)
            && (myState.convertType != state.convertType)) {

            myState.convertType = state.convertType;
            setDictionary();
        }

        // Switch setting Temporary dictionary.
        if (state.temporaryMode != EngineState.INVALID) {

            switch (state.temporaryMode) {
            case EngineState.TEMPORARY_DICTIONARY_MODE_NONE:
                if (DEBUG) {Log.d(TAG, "EngineState.TEMPORARY_DICTIONARY_MODE_NONE");}

                if (myState.temporaryMode != EngineState.TEMPORARY_DICTIONARY_MODE_NONE) {
                    setDictionary();
                    mConverterSymbolEngineBack.initializeMode();
                    setPreConverter(mPreConverterBack);
                    setConverter(mConverterBack);
                    mDisableAutoCommitEnglishMask &= ~AUTO_COMMIT_ENGLISH_SYMBOL;

                    mTextCandidatesViewManager.setSymbolMode(false, 0);
                    ((DefaultSoftKeyboard)getInputViewManager()).setNormalKeyboard();
                }
                break;

            case EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL:
                if (DEBUG) {Log.d(TAG, "EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL");}

                setConverter(mConverterSymbolEngineBack);
                mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_SYMBOL;
                mTextCandidatesViewManager.setSymbolMode(true, mConverterSymbolEngineBack.getMode());
                mConverterSymbolEngineBack.updateAdditionalSymbolInfo(mEnableEmoji, mEnableDecoEmoji);
                ((DefaultSoftKeyboard)getInputViewManager()).setSymbolKeyboard();
                break;

            default:
                break;
            }

            myState.temporaryMode = state.temporaryMode;
        }

        // Set the preference dictionary
        if ((state.preferenceDictionary != EngineState.INVALID)
            && (myState.preferenceDictionary != state.preferenceDictionary)) {

            if (DEBUG) {Log.d(TAG, "EngineState.preferenceDictionary");}
            myState.preferenceDictionary = state.preferenceDictionary;

            setDictionary();
        }

        /* keyboard type */
        if (state.keyboard != EngineState.INVALID) {
            int flexibleType;
            int keyboardType;

            switch (state.keyboard) {
            case EngineState.KEYBOARD_12KEY:
                keyboardType = iWnnEngine.KeyboardType.KEY_TYPE_KEYPAD12;
                if (myState.language == EngineState.LANGUAGE_EN) {
                    flexibleType = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_OFF;
                } else {
                    flexibleType = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_ON;
                }
                break;

            case EngineState.KEYBOARD_QWERTY:
            default:
                keyboardType = iWnnEngine.KeyboardType.KEY_TYPE_QWERTY;

                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(OpenWnn.superGetContext());
                Resources res = getResources();
                mEnableSpellCorrection = pref.getBoolean(OPT_SPELL_CORRECTION_KEY, res.getBoolean(R.bool.opt_spell_correction_default_value));

                if (!mEnableSpellCorrection) {
                    flexibleType = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_OFF;
                } else {
                    flexibleType = iWnnEngine.FlexibleSearchType.FLEXIBLE_SEARCH_ON;
                }
                break;
            }

            // Doesn't process while setting change.
            if (!OpenWnn.getCurrentIme().isKeepInput()) {
                mConverterIWnn.setFlexibleCharset(flexibleType, keyboardType);
            }
            myState.keyboard = state.keyboard;
        }
    }

    /**
     * Set dictionaries to be used.
     */
    private void setDictionary() {
        int dictionary = iWnnEngine.SetType.NORMAL;
        int language;

        if (mEngineState.convertType == EngineState.CONVERT_TYPE_EISU_KANA) {
            language = iWnnEngine.LanguageType.JAPANESE;
            dictionary = iWnnEngine.SetType.EISUKANA;
        } else {
            switch (mEngineState.language) {
            case EngineState.LANGUAGE_EN:
                language = iWnnEngine.LanguageType.ENGLISH;
                mConverterIWnn.setConvertedCandidateEnabled(true);

                switch (mEngineState.preferenceDictionary) {
                case EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI:
                    dictionary = iWnnEngine.SetType.EMAIL_ADDRESS;
                    break;
                default:
                    // Preference dictionary none
                    break;
                }

                break;

            default:
            case EngineState.LANGUAGE_JP:
                language = iWnnEngine.LanguageType.JAPANESE;
                mConverterIWnn.setConvertedCandidateEnabled(false);

                switch (mEngineState.preferenceDictionary) {
                case EngineState.PREFERENCE_DICTIONARY_PERSON_NAME:
                    dictionary = iWnnEngine.SetType.JINMEI;
                    break;
                case EngineState.PREFERENCE_DICTIONARY_POSTAL_ADDRESS:
                    dictionary = iWnnEngine.SetType.POSTAL_ADDRESS;
                    break;
                default:
                    // Preference dictionary none
                    break;
                }
                break;
            }
        }

        mConverterIWnn.setDictionary(language, dictionary, getSwitcher().hashCode());
    }

    /**
     * Create the input view manager.
     *
     * @return The input view manager for a specific language
     */
    protected InputViewManager createInputViewManager(IWnnLanguageSwitcher wnn) {
        return new DefaultSoftKeyboardJAJP(wnn);
    }

    /**
     * Return the specific language.
     *
     * @return The specific language type of {@link jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine.LanguageType} class
     */
    protected int getLanguage() {
        return iWnnEngine.LanguageType.JAPANESE;
    }


    /**
     * Handle a toggle key input event.
     *
     * @param table  Table of toggle characters
     */
    private void processSoftKeyboardToggleChar(String[] table) {
        if (table == null) {
            return;
        }

        commitConvertingText();

        boolean toggled = false;
        if ((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT) {
            ComposingText composingText = getComposingText();
            int cursor = composingText.getCursor(ComposingText.LAYER1);
            if (cursor > 0) {
                StrSegment strSegment = composingText.getStrSegment(ComposingText.LAYER1,cursor - 1);
                if (strSegment != null) {
                    String prevChar = strSegment.string;
                    if (prevChar != null) {
                        String c = searchToggleCharacter(prevChar, table, false);
                        if (c != null) {
                            composingText.delete(ComposingText.LAYER1, false);
                            appendToggleString(c);
                            toggled = true;
                        }
                    }
                }
            }
        }

        boolean appendComplete = true;
        if (!toggled) {
            if (!isEnableL2Converter()) {
                commitText(false);
            }

            appendComplete = appendToggleString(table[0]);
        }

        mFunfun = 0;
        setFunFun(mFunfun);
        if (appendComplete) {
            mStatus = STATUS_INPUT;
        } else {
            mStatus = STATUS_INPUT_EDIT;
        }

        updateViewStatusForPrediction(true, true);
    }

    /**
     * Handle character input from the software keyboard without listing candidates.
     *
     * @param chars  The input character(s)
     */
    private void processSoftKeyboardCodeWithoutConversion(char[] chars) {
        if (chars == null) {
            return;
        }

        ComposingText text = getComposingText();
        appendStrSegment(new StrSegment(chars));

        if (!isAlphabetLast(text.toString(ComposingText.LAYER1))) {
            // When the input character is not an alphabet, immediately fix it.
            commitText(false);
        } else {
            boolean completed = getPreConverter().convert(text);
            if (completed) {
                commitTextWithoutLastAlphabet();
            } else {
                mStatus = STATUS_INPUT;
                mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
                updateViewStatusForPrediction(true, true);
            }
        }
    }

    /**
     * Handle character input from the software keyboard.
     *
     * @param chars   The input character(s)
     */
    private void processSoftKeyboardCode(char[] chars) {
        if (chars == null) {
            return;
        }

        ComposingText composingText = getComposingText();
        if ((chars[0] == ' ') || (chars[0] == '\u3000' /* Full-width space */)) {
            if (composingText.size(0) == 0) {
                getCandidatesViewManager().clearCandidates();
                commitText(new String(chars));
                breakSequence();

                InputConnection inputConnection = getInputConnection();
                if (inputConnection == null) {
                    return;
                }

                CharSequence afterCursor = inputConnection.getTextAfterCursor(1, 0);

                if (TextUtils.isEmpty(afterCursor)) {
                    if (!mAutoPunctuation) {
                        return;
                    }

                    int keymode = ((DefaultSoftKeyboard) getInputViewManager()).getKeyMode();
                    if (keymode == DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET) {
                        CharSequence beforeThreeCursor
                                = inputConnection.getTextBeforeCursor(3, 0);

                        if (!TextUtils.isEmpty(beforeThreeCursor)) {
                            if (beforeThreeCursor.length() < 3) {
                                return;
                            } else {
                                if ((beforeThreeCursor.charAt(0) != ' ')
                                        && (beforeThreeCursor.charAt(0) != '.')
                                        && (beforeThreeCursor.charAt(1) == ' ')
                                        && (beforeThreeCursor.charAt(2) == ' ')) {
                                    inputConnection.deleteSurroundingText(2,0);
                                    commitText(". ");
                                }
                            }
                        }
                    }
                }
            } else {
                if (isEnglishPrediction()) {
                    initCommitInfoForWatchCursor();
                    commitText(true);
                    commitSpaceJustOne();
                    checkCommitInfo();
                } else {
                    if (mEngineState.isRenbun()) {
                        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
                        if (!candidatesViewManager.isFocusCandidate()) {
                            processDownKeyEvent();
                        }
                        processRightKeyEvent();
                    } else {
                        startConvert(EngineState.CONVERT_TYPE_RENBUN);
                    }
                }
            }
            mEnableAutoDeleteSpace = false;
        } else {
            commitConvertingText();

            // And, an English predict is fixed for QWERTY by '.'etc.
            boolean commit = false;
            if (isEnglishPrediction()
                    && (mEngineState.keyboard == EngineState.KEYBOARD_QWERTY)) {

                Matcher m = mEnglishAutoCommitDelimiter.matcher(new String(chars));
                if (m.matches()) {
                    commit = true;
                }
            }

            if (commit || isLatticeInputMode()) {
                commitText(true);

                appendStrSegment(new StrSegment(chars));
                commitText(true);
            } else {
                appendStrSegment(new StrSegment(chars));
                LetterConverter preConverter = getPreConverter();
                if (preConverter != null) {
                    preConverter.convert(composingText);
                    mStatus = STATUS_INPUT;
                }
                mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
                updateViewStatusForPrediction(true, true);
            }
        }
    }

    /**
     * Start a consecutive clause conversion on EISU-KANA conversion mode.
     *
     * @param convertType  The conversion type({@code EngineState.CONVERT_TYPE_*})
     */
    private void startConvert(int convertType) {
        if (!isEnableL2Converter()) {
            return;
        }

        if (mEngineState.convertType != convertType) {
            // The cursor is specified for an appropriate position if there is no range specification.
            if (!mExactMatchMode) {
                if (convertType == EngineState.CONVERT_TYPE_RENBUN) {
                    // The clause position is not specified when consecutive clause conversion.
                    getComposingText().setCursor(ComposingText.LAYER1, 0);
                } else {
                    if (mEngineState.isRenbun()) {
                        /* EISU-KANA conversion specifying the position of the segment if previous mode is conversion mode */
                        mExactMatchMode = true;
                    } else {
                        // It is a range specification as for all input character. (EISU-KANA)
                        ComposingText composingText = getComposingText();
                        composingText.setCursor(ComposingText.LAYER1,
                                             composingText.size(ComposingText.LAYER1));
                    }
                }
            }

            if (convertType == EngineState.CONVERT_TYPE_RENBUN) {
                // clears variables for the prediction
                mFunfun = 0;
                setFunFun(mFunfun);
                mExactMatchMode = false;
            }
            // clears variables for the convert
            mCommitCount = 0;

            int layer;
            if (convertType == EngineState.CONVERT_TYPE_EISU_KANA) {
                mFunfun = 0;
                setFunFun(mFunfun);
                layer = ComposingText.LAYER1;
            } else {
                layer = ComposingText.LAYER2;
            }

            EngineState state = new EngineState();
            state.convertType = convertType;
            updateEngineState(state);

            updateViewStatus(layer, true, true);
        }
    }

    /**
     * Auto commit a word in English (on half-width alphabet mode).
     *
     * @return  {@code true} if auto-committed; otherwise, {@code false}.
     */
    private boolean autoCommitEnglish() {
        if (isEnglishPrediction() && (mDisableAutoCommitEnglishMask == AUTO_COMMIT_ENGLISH_ON)) {
            InputConnection inputConnection = getInputConnection();
            CharSequence seq = inputConnection.getTextBeforeCursor(2, 0);
            if (seq != null) {
                Matcher m = mEnglishAutoCommitDelimiter.matcher(seq);
                if (m.matches()) {
                    if ((seq.charAt(0) == ' ') && mEnableAutoDeleteSpace) {
                        inputConnection.deleteSurroundingText(2, 0);
                        CharSequence str = seq.subSequence(1, 2);

                        // Through YOMI of committed text for APP
                        commitTextToInputConnection(str);

                        mPrevCommitText.append(str);
                        mIgnoreCursorMove = true;
                    }

                    // Remove delayed event that was sent from previous suggestion,
                    // to hide the candidate view.
                    getCandidatesViewManager().clearCandidates();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Insert a space if the previous character is not a space.
     */
    private void commitSpaceJustOne() {
        CharSequence seq = getInputConnection().getTextBeforeCursor(1, 0);
        if ((seq != null) && (seq.length() > 0)) {
            if (seq.charAt(0) != ' ') {
                commitText(" ");
            }
        }
    }

    /**
     * Get the shift key state from the editor.
     *
     * @param editor  editor
     *
     * @return state id of the shift key (0:off, 1:on)
     */
    protected int getShiftKeyState(EditorInfo editor) {
        return (getCurrentInputConnection().getCursorCapsMode(editor.inputType) == 0) ? 0 : 1;
    }

    /**
     * Memorize a selected word.
     *
     * @param word  a selected word
     */
    private void learnWord(WnnWord word) {
        if (word == null || (!mEnableLearning
                && ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_WORD) != 0))) {
            /* learning ConvertMultiple */
            mConverterIWnn.learn(mEnableLearning);
        } else {
            /* learning Nomal */
            if (!mEnableLearning) {
                word.attribute = word.attribute | iWnnEngine.WNNWORD_ATTRIBUTE_NO_DICTIONARY;
            }

            getConverter().learn(word);
        }
    }

    /**
     * Fit an editor info.
     *
     * @param preference  an preference data.
     * @param info  an editor info.
     */
    private void fitInputType(SharedPreferences preference, EditorInfo info) {
        if (info.inputType == EditorInfo.TYPE_NULL) {
            setDirectInputMode(true);
            return;
        }

        DefaultSoftKeyboardJAJP inputViewManager = (DefaultSoftKeyboardJAJP)getInputViewManager();

        /* voice input initialize */
        mHasUsedVoiceInput = preference.getBoolean(PREF_HAS_USED_VOICE_INPUT, false);

        /////////// Initial value
        // wildcard prediction mode
        Resources res = getResources();
        mEnableFunfun = preference.getBoolean(OPT_FUNFUN_KEY, res.getBoolean(R.bool.opt_funfun_default_value));
        mEnableLearning   = preference.getBoolean(OPT_ENABLE_LEARNING_KEY, res.getBoolean(R.bool.opt_enable_learning_default_value));
        mEnablePrediction = preference.getBoolean(OPT_PREDICTION_KEY, res.getBoolean(R.bool.opt_prediction_default_value));
        mEnableSpellCorrection = preference.getBoolean(OPT_SPELL_CORRECTION_KEY, res.getBoolean(R.bool.opt_spell_correction_default_value));

        mEnableFullscreen = preference.getBoolean("fullscreen_mode", res.getBoolean(R.bool.fullscreen_mode_default_value));
        mEnableMushroom = preference.getString("opt_mushroom", res.getString(R.string.mushroom_id_default)).equals("notuse") ? false : true;
        mDisableAutoCommitEnglishMask &= ~AUTO_COMMIT_ENGLISH_OFF;
        int preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_NONE;
        mEnableConverter = true;
        mEnableSymbolList = true;
        mEnableSymbolListNonHalf = true;
        mEnableEmoji = false;

        mEnableDecoEmoji = false;

        inputViewManager.setEnableEmojiUNI6(false);
        setEnabledTabs(false, true, false);



        mAutoCaps = preference.getBoolean("auto_caps", res.getBoolean(R.bool.auto_caps_default_value));
        mConverterIWnn.setEmailAddressFilter(false);
        mEnableAutoInsertSpace = preference.getBoolean("opt_auto_space", res.getBoolean(R.bool.opt_auto_space_default_value));
        mAutoCursorMovementSpeed =
                Integer.parseInt(preference.getString("opt_auto_cursor_movement",
                        res.getString(R.string.auto_cursor_movement_id_default)));
        if (mAutoCursorMovementSpeed != AUTO_CURSOR_MOVEMENT_OFF
                && (preference.getBoolean("flick_input", res.getBoolean(R.bool.flick_input_default_value))
                    && !preference.getBoolean("flick_toggle_input", res.getBoolean(R.bool.flick_toggle_input_default_value)))) {
            mAutoCursorMovementSpeed = AUTO_CURSOR_MOVEMENT_OFF;
        }
        if ((info.imeOptions & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0) {
            mEnableFullscreen = false;
        }

        mAutoPunctuation = preference.getBoolean("auto_punctuation", false);
        mDirectKana = preference.getString("kana_roman_input", res.getString(R.string.kana_roman_input_default_value)).equals("Kana") ? true : false;

        if (inputViewManager.isEnableModekey(DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA)) {
            if (isEnableKanaInput()) {
                setPreConverter(mPreConverterKanaDirect);
                mPreConverterBack = getPreConverter();
            } else {
                if (!mEngineState.isEnglish()) {
                    setPreConverter(mPreConverterKanaRoman);
                    mPreConverterBack = getPreConverter();
                }
            }
        }

        mEnableHeadConv = preference.getBoolean("opt_head_conversion", res.getBoolean(R.bool.opt_head_conversion_default_value));
        mConverterIWnn.setEnableHeadConversion(mEnableHeadConv);

        /////////// The state is changed according to the input type.

        int defaultKeyMode = IWnnImeJaJp.getDefaultKeyMode(info);
        if ((defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_NOTHING)
                || (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG)
                || (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_FULL_HIRAGANA_ONLY)
                || (defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_HALF_ALPHABET_ONLY)) {
            switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
                mEnableConverter = false;
                if (EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD == (info.inputType & EditorInfo.TYPE_MASK_VARIATION)) {
                    mEnableSymbolListNonHalf = false;
                    mTextCandidatesViewManager.setEnableEmoticon(false);
                    mTextCandidatesViewManager.setEnableEmojiUNI6(false);
                    inputViewManager.setEnableEmojiUNI6(false);
                }
                break;

            case EditorInfo.TYPE_CLASS_DATETIME:
                mEnableConverter = false;
                break;

            case EditorInfo.TYPE_CLASS_PHONE:
                mEnableSymbolList = false;
                mEnableConverter = false;
                mEnableSymbolListNonHalf = false;
                mConverterIWnn.setEmailAddressFilter(true);
                mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
                mTextCandidatesViewManager.setEnableEmoticon(false);
                mTextCandidatesViewManager.setEnableEmojiUNI6(false);
                inputViewManager.setEnableEmojiUNI6(false);
                break;

            case EditorInfo.TYPE_CLASS_TEXT:
                int keymode = inputViewManager.getKeyMode();
                if(keymode == DefaultSoftKeyboardJAJP.KEYMODE_JA_HALF_ALPHABET){
                    switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
                    case EditorInfo.TYPE_CLASS_TEXT:
                        switch (info.inputType & EditorInfo.TYPE_MASK_FLAGS) {
                        case EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS:
                            mEnablePrediction = false;
                            mEnableFunfun     = false;
                            mEnableLearning   = false;
                            mEnableSpellCorrection = false;
                            break;
                        default:
                            break;
                        }
                    default:
                        break;
                      }
                }

                switch (info.inputType & EditorInfo.TYPE_MASK_VARIATION) {
                case EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME:
                    preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_PERSON_NAME;
                    break;

                case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                    mEnablePrediction = false;
                    mEnableLearning = false;
                    mEnableConverter = false;
                    mEnableSymbolListNonHalf = false;
                    mConverterIWnn.setEmailAddressFilter(true);
                    mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
                    mTextCandidatesViewManager.setEnableEmoticon(false);
                    mTextCandidatesViewManager.setEnableEmojiUNI6(false);
                    inputViewManager.setEnableEmojiUNI6(false);
                    break;

                case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                case EditorInfo.TYPE_TEXT_VARIATION_URI:
                    mEnableAutoInsertSpace = false;
                    preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI;
                    isEmailAddressUriEditor = true;
                    break;

                case EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
                    preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_POSTAL_ADDRESS;
                    break;

                case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                    mEnableLearning = false;
                    mEnableConverter = false;
                    mEnableSymbolList = false;
                    break;

                default:
                    // Uncorrespondence
                    break;
                }
                break;

            default:
                break;
            }
        }

        mConverterIWnn.setEmojiFilter(!mEnableEmoji);
        mConverterIWnn.setPreferences(preference);
        mConverterIWnn.setDecoEmojiFilter(!mEnableDecoEmoji);
        DecoEmojiUtil.setConvertFunctionEnabled(mEnableDecoEmoji);
        inputViewManager.setEnableEmojiSymbol(mEnableEmoji, mEnableDecoEmoji, mEnableSymbolList);

        if (!mEnablePrediction) {
            mEnableFunfun = false;
        }

        // Doesn't process while setting change.
        if (!OpenWnn.getCurrentIme().isKeepInput()) {
            EngineState state = new EngineState();
            state.language = mEngineState.language;
            state.preferenceDictionary = preferenceDictionary;
            state.convertType = EngineState.CONVERT_TYPE_NONE;

            updateEngineState(state);
        }
    }

    /**
     * Get default keymode by privateImeOptions.
     *
     * @param  editor  EditorInfo
     * @return  {@code DEFAULT_KEYMODE_NOTHING} - Option Nothing;
     *          {@code DEFAULT_KEYMODE_EIJI} - default keymode is eiji;
     *          {@code DEFAULT_KEYMODE_HIRAGANA} - default keymode is hiragana.
     */
    public static int getDefaultKeyMode(EditorInfo editor) {
        int result = DEFAULT_KEYMODE_NOTHING;
        if (editor.privateImeOptions != null) {
            if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_EIJI_STR)) {
                result = DEFAULT_KEYMODE_HALF_ALPHABET;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_EIJI_LG_STR)) {
                result = DEFAULT_KEYMODE_HALF_ALPHABET;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_HIRAGANA_STR)) {
                result = DEFAULT_KEYMODE_FULL_HIRAGANA;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_HIRAGANA_LG_STR)) {
                result = DEFAULT_KEYMODE_FULL_HIRAGANA;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_NUM_STR)) {
                result = DEFAULT_KEYMODE_HALF_NUMBER;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_FULL_HIRAGANA_STR)) {
                result = DEFAULT_KEYMODE_FULL_HIRAGANA;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_FULL_ALPHABET_STR)) {
                result = DEFAULT_KEYMODE_FULL_ALPHABET;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_FULL_NUMBER_STR)) {
                result = DEFAULT_KEYMODE_FULL_NUMBER;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_FULL_KATAKANA_STR)) {
                result = DEFAULT_KEYMODE_FULL_KATAKANA;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_HALF_ALPHABET_STR)) {
                result = DEFAULT_KEYMODE_HALF_ALPHABET;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_HALF_NUMBER_STR)) {
                result = DEFAULT_KEYMODE_HALF_NUMBER;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_HALF_KATAKANA_STR)) {
                result = DEFAULT_KEYMODE_HALF_KATAKANA;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_HALF_PHONE_STR)) {
                result = DEFAULT_KEYMODE_HALF_PHONE;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_HIRAGANA_ONLY_STR)) {
                result = DEFAULT_KEYMODE_FULL_HIRAGANA_ONLY;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_EIJI_ONLY_STR)) {
                result = DEFAULT_KEYMODE_HALF_ALPHABET_ONLY;
            } else if (editor.privateImeOptions.equals(DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG_STR)) {
                result = DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG;
            } // else {}
        }
        return result;
    }



    /**
     * Append a {@link StrSegment} to the composing text.
     * <br>
     * If the length of the composing text exceeds
     * {@code LIMIT_INPUT_NUMBER}, the appending operation is ignored.
     *
     * @param  str  Input segment
     * @return  {@code true} - append complete;
     *          {@code false} - the composing text length is exceed;
     */
    private boolean appendStrSegment(StrSegment str) {
        ComposingText composingText = getComposingText();

        if (composingText.size(ComposingText.LAYER1) >= LIMIT_INPUT_NUMBER) {
            return false;//When the number of maximum input characters is exceeded, nothing is processed.
        }
        composingText.insertStrSegment(ComposingText.LAYER0, ComposingText.LAYER1, str);
        return true;
    }

    /**
     * Commit the consecutive clause conversion.
     */
    private void commitConvertingText() {
        ComposingText composingText = getComposingText();
        if (mEngineState.isRenbun()) {
            int size = composingText.size(ComposingText.LAYER2);
            if (size <= 0) {
                return;
            }

            CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
            if (candidatesViewManager.isFocusCandidate()) {
                learnWord(getFocusedCandidate());
                mCommitCount++;
            }

            for (int i = mCommitCount; i < size; i++) {
                getConverter().makeCandidateListOf(i);
                learnWord(null);
            }

            CharSequence text = convertComposingToCommitText(composingText, ComposingText.LAYER2);

            // Through YOMI of committed text for APP
            mStrokeOfCommitText = composingText.toString(ComposingText.LAYER1);
            commitTextToInputConnection(text);

            mPrevCommitText.append(text);
            mIgnoreCursorMove = true;
            initializeScreen();
        }

        if (mEngineState.isEisuKana()) {
            composingText.setCursor(ComposingText.LAYER1,
                                     composingText.size(ComposingText.LAYER1));
            mStatus = commitText(true);
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#initializeScreen */
    @Override protected void initializeScreen() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();

        ComposingText composingText = getComposingText();
        if ((composingText.size(ComposingText.LAYER0) != 0) || (mFunfun != 0)) {
            getInputConnection().setComposingText("", 0);
        }
        composingText.clear();
        mExactMatchMode = false;
        mFunfun = 0;
        setFunFun(mFunfun);
        mStatus = STATUS_INIT;
        View candidateView = candidatesViewManager.getCurrentView();
        if ((candidateView != null) && candidateView.isShown()) {
            candidatesViewManager.clearCandidates();
        }
        IWnnLanguageSwitcher switcher = getSwitcher();
        getInputViewManager().onUpdateState(switcher);

        EngineState state = new EngineState();
        state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
        updateEngineState(state);
        if (mFullCandidate) {
            mFullCandidate = false;
            updateFullscreenMode();
        }
    }

    /**
     * Whether the tail of the string is alphabet or not.
     *
     * @param  str      The string
     * @return          {@code true} if the tail is alphabet; {@code false} if otherwise.
     */
    private boolean isAlphabetLast(String str) {
        Matcher m = ENGLISH_CHARACTER_LAST.matcher(str);
        return m.matches();
    }

    /**
     * Judge whether all characters are alphabet.
     *
     * @param  str Input character
     * @return  {@code true} - The alphabet; {@code false} - Excluding the alphabet.
     */
    private boolean isAlphabetAll(String str) {
        Matcher m = ENGLISH_CHARACTER_ALL.matcher(str);
        return m.matches();
    }

    /**
     * Called to inform the input method that text input has finished in the last editor.
     */
    @Override public void onFinishInput() {
        if (PROFILE) {
            Debug.stopMethodTracing();
            Log.d(TAG, "onFinishInput() stopMethodTracing.");
        }
        InputConnection inputConnection = getInputConnection();
        if (inputConnection != null) {
            if (mFunfun > 0) {
                mFunfun = 0;
                setFunFun(mFunfun);
            }
            //So as not to display wildcard prediction, delete the composing text.
            initializeScreen();
        }
        onFinishInputOpenWnn();
    }

    /**
     * Check whether Layer 2 converter is active or not.
     *
     * @return {@code true} if the converter is active.
     */
    private boolean isEnableL2Converter() {
        if (getConverter() == null || !mEnableConverter) {
            return false;
        }

        if (mEngineState.isEnglish() && !mEnablePrediction) {
            return false;
        }

        return true;
    }

    /**
     * Handling KeyEvent(KEYUP)
     * <br>
     * This method is called from {@link #handleEvent}.
     *
     * @param ev   An up key event
     */
    private void onKeyUpEvent(KeyEvent ev) {
        int key = ev.getKeyCode();

        if (mEnableHardware12Keyboard && !isDirectInputMode()) {
            if (isHardKeyboard12KeyLongPress(key)
                    && ((ev.getFlags() & KeyEvent.FLAG_CANCELED_LONG_PRESS) == 0)) {
                switch (key) {
                case KeyEvent.KEYCODE_SOFT_LEFT:
                    if (mEngineState.isSymbolList()) {
                        switchSymbolList();
                    } else if ((getComposingText().size(0) != 0) && !mEngineState.isRenbun()
                            && (((DefaultSoftKeyboardJAJP)getInputViewManager()).getKeyMode()
                                     == DefaultSoftKeyboardJAJP.KEYMODE_JA_FULL_HIRAGANA)) {
                        startConvert(EngineState.CONVERT_TYPE_RENBUN);
                    } else {
                        ((DefaultSoftKeyboard) getInputViewManager()).onKey(
                                DefaultSoftKeyboard.KEYCODE_JP12_EMOJI, null);
                    }
                    break;

                case KeyEvent.KEYCODE_SOFT_RIGHT:
                    ((DefaultSoftKeyboardJAJP) getInputViewManager()).showInputModeSwitchDialog();
                    break;

                case KeyEvent.KEYCODE_DEL:
                    int newKeyCode = KeyEvent.KEYCODE_FORWARD_DEL;
                    ComposingText composingText = getComposingText();
                    int composingTextSize = composingText.size(ComposingText.LAYER1);
                    if ((composingTextSize + mFunfun) > 0) {
                        if (composingText.getCursor(ComposingText.LAYER1) > (composingTextSize - 1)) {
                            newKeyCode = KeyEvent.KEYCODE_DEL;
                        }
                        KeyEvent keyEvent = new KeyEvent(ev.getAction(), newKeyCode);
                        if (!processKeyEvent(keyEvent)) {
                            sendKeyEventDirect(keyEvent);
                        }
                    } else {
                        InputConnection inputConnection = getInputConnection();
                        if (inputConnection != null) {
                            CharSequence text = inputConnection.getTextAfterCursor(1, 0);
                            if ((text == null) || (text.length() == 0)) {
                                newKeyCode = KeyEvent.KEYCODE_DEL;
                            }
                        }
                        KeyEvent keyEvent = new KeyEvent(ev.getAction(), newKeyCode);
                        sendKeyEventDirect(keyEvent);
                    }
                    break;

                default:
                    break;

                }
            }
        }
    }

    /**
     * Handling KeyEvent(KEYLONGPRESS)
     * <br>
     * This method is called from {@link #handleEvent}.
     *
     * @param ev   An long press key event
     * @return    {@code true} if the event is processed in this method; {@code false} if not.
     */
    private boolean onKeyLongPressEvent(KeyEvent ev) {
        //If you need this event, Call KeyEvent#startTracking().
        if (mEnableHardware12Keyboard) {
            int keyCode = 0;
            if (ev != null) {
                keyCode = ev.getKeyCode();
            }
            switch (keyCode) {
            case KeyEvent.KEYCODE_SOFT_LEFT:
                if (mEnableMushroom) {
                    callMushRoom(null);
                }
                return true;

            case KeyEvent.KEYCODE_SOFT_RIGHT:
                getSwitcher().requestHideSelf(0);
                Intent intent = new Intent();
                intent.setClass(OpenWnn.superGetContext(), ControlPanelStandard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mWnnSwitcher.startActivity(intent);
                return true;

            case KeyEvent.KEYCODE_DEL:
                initializeScreen();
                InputConnection inputConnection = getInputConnection();
                if (inputConnection != null) {
                    inputConnection.deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE);
                }
                return true;

            default:
                break;

            }
        }
        return false;
    }

    /**
     * Processing the undo.
     *
     * @return {@code true}:Processing undo , {@code false}:if not.
     */
    private boolean undo() {
        if (mCanUndo) {
            boolean hasCommitedByVoiceInput= mHasCommitedByVoiceInput;

            EngineState state = new EngineState();
            state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
            updateEngineState(state);

            //The character string fixed to be displayed is deleted.
            getInputConnection().deleteSurroundingText(mPrevCommitText.length(), 0);

            DefaultSoftKeyboardJAJP inputManager = ((DefaultSoftKeyboardJAJP)getInputViewManager());
            inputManager.undoKeyMode();
            clearCommitInfo();

            // The cursor moves by deleteSurroundingText().
            // To avoid initializing, the increment does a final frequency.
            mIgnoreCursorMove = true;

            if (mEnableLearning && !hasCommitedByVoiceInput) {
                mConverterIWnn.undo(1);
            }
            ComposingText composingText = getComposingText();
            composingText.clear();

            //The predict character string is returned while inputting it.
            String[] strSpilit = mCommitPredictKey.split("");
            int length = mCommitPredictKey.split("").length;
            length--;
            for (int i = 0; i < length; i++) {
                appendStrSegment(new StrSegment(strSpilit[i + 1]));
            }

            if (length != 0) {
                int size = mCommitLayer1StrSegment.size();
                StrSegment[] layer1StrSegment = new StrSegment[size];
                mCommitLayer1StrSegment.toArray(layer1StrSegment);
                composingText.setCursor(ComposingText.LAYER1, length);
                composingText.replaceStrSegment(ComposingText.LAYER1, layer1StrSegment, length);

                mFunfun = mCommitFunfun;
                setFunFun(mFunfun);
            }
            mExactMatchMode = mCommitExactMatch;
            composingText.setCursor(ComposingText.LAYER1, mCommitCursorPosition);

            mStatus = STATUS_INPUT_EDIT;
            if (isEnableL2Converter()) {
                getConverter().init(mWnnSwitcher.getFilesDirPath());
            }

            if (mCommitConvertType == EngineState.CONVERT_TYPE_EISU_KANA) {
                startConvert(EngineState.CONVERT_TYPE_EISU_KANA);
            } else {
                updateViewStatusForPrediction(true, true);
            }

            breakSequence();
            return true;
        }
        return false;
    }

    /**
     * Initialize the committed text's information.
     */
    private void initCommitInfoForWatchCursor() {
        if (!isEnableL2Converter()) {
            return;
        }

        mCommitStartCursor = mComposingStartCursor;
        mPrevCommitText.delete(0, mPrevCommitText.length());

        // for undo
        if ((0 <= mCommitStartCursor) && !mEngineState.isSymbolList()) {
            ComposingText composingText = getComposingText();
            mCommitConvertType = mEngineState.convertType;
            mCommitFunfun = mFunfun;
            mCommitPredictKey = composingText.toString(ComposingText.LAYER0);
            if (mEngineState.isRenbun()) {
                mCommitCursorPosition = composingText.size(ComposingText.LAYER1);
            } else {
                mCommitCursorPosition = composingText.getCursor(ComposingText.LAYER1);
            }
            mCommitExactMatch = mExactMatchMode;
            DefaultSoftKeyboardJAJP inputManager = ((DefaultSoftKeyboardJAJP)getInputViewManager());
            inputManager.setUndoKey(true);
            mCanUndo = false;

            mCommitLayer1StrSegment.clear();
            int size = composingText.size(ComposingText.LAYER1);
            for (int i = 0; i < size; i++) {
                StrSegment c = composingText.getStrSegment(ComposingText.LAYER1, i);
                if (c != null) {
                    mCommitLayer1StrSegment.add(new StrSegment(c.string, c.from, c.to));
                }
            }
        }
    }

    /**
     * Clear the commit text's info.
     *
     * @return {@code true}:cleared, {@code false}:has already cleared.
     */
    private boolean clearCommitInfo() {
        if (mCommitStartCursor < 0) {
            return false;
        }

        mCommitStartCursor = -1;

        // for undo
        DefaultSoftKeyboardJAJP inputManager = ((DefaultSoftKeyboardJAJP)getInputViewManager());
        inputManager.setUndoKey(false);
        mCanUndo = false;
        mHasCommitedByVoiceInput = false;

        return true;
    }

    /**
     * Verify the commit text.
     */
    private void checkCommitInfo() {
        if (mCommitStartCursor < 0) {
            return;
        }

        CharSequence composingString = convertComposingToCommitText(
                getComposingText(), mTargetLayer);
        int composingLength = composingString.length();

        CharSequence seq = getInputConnection().getTextBeforeCursor(mPrevCommitText.length() + composingLength, 0);
        if (seq != null && seq.length() >= composingLength) {
            seq = seq.subSequence(0, seq.length() - composingLength);
            if (!seq.equals(mPrevCommitText.toString())) {
                mIgnoreCursorMove = false;
                clearCommitInfo();
            }
        } else {
            mIgnoreCursorMove = false;
            clearCommitInfo();
        }
    }

    /** @see IWnnImeBase#close */
    @Override public void close(boolean now) {
        super.close(now);
        mHandler.removeMessages(MSG_UPDATE_DECOEMOJI_DICTIONARY);
        mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
        mHandler.removeMessages(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
        mHandler.removeMessages(MSG_RESTART);

        Message message = mHandler.obtainMessage(MSG_CLOSE);
        if (now) {
            mHandler.handleMessage(message);
        } else {
            mHandler.sendMessageDelayed(message, 0);
        }
    }

    /** @see IWnnImeBase#closeSwitch */
    @Override
    public void closeSwitch() {
        super.closeSwitch();
        mHandler.removeMessages(MSG_UPDATE_DECOEMOJI_DICTIONARY);
        mHandler.removeMessages(MSG_TOGGLE_TIME_LIMIT);
        mHandler.removeMessages(MSG_DELETE_DECOEMOJI_LEARNING_DICTIONARY);
        mHandler.removeMessages(MSG_RESTART);
    }

    /**
     * Break the sequence of words.
     */
    private void breakSequence() {
        mEnableAutoDeleteSpace = false;
        mIsInputSequenced = false;
        mConverterIWnn.breakSequence();
    }

    /**
     * Return whether the key event is left/right or not.
     *
     * @param  ev  An event
     * @return {@code true} if key event is right or left; {@code false} if otherwise.
     */
    private boolean isRightOrLeftKeyEvents(OpenWnnEvent ev) {
        boolean ret = false;
        if (ev.code == OpenWnnEvent.INPUT_SOFT_KEY) {
            KeyEvent keyEvent = ev.keyEvent;
            int keyCode = 0;
            if (keyEvent != null) {
                keyCode = keyEvent.getKeyCode();
            }

            if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                    || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Change symbol engine state.
     *
     * @param  state  Engine state
     * @param  mode   Engine mode
     */
    private void changeSymbolEngineState(EngineState state, int mode) {
        if (mEnableSymbolList && !isDirectInputMode()) {
            if (mode == IWnnSymbolEngine.MODE_NONE) {
                if (mEnableSymbolListNonHalf) {
                    mConverterSymbolEngineBack.setSymToggle(mEnableEmoji, mEnableDecoEmoji, !mEnableEmoji, mEnableSymbolListNonHalf);
                } else {
                    mConverterSymbolEngineBack.setSymToggle(false, false, false, false);
                }
            } else {
                mConverterSymbolEngineBack.setMode(mode);
            }
            mFunfun = 0;
            setFunFun(mFunfun);
            clearCommitInfo();
            state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL;
            updateEngineState(state);
            updateViewStatusForPrediction(true, true);
        }
    }

    /**
     * Called when processes the character input by flick action.
     *
     * @param input The flick input character.
     */
    private void processFlickInputChar(String input) {
        if (input == null) {
            return;
        }

        commitConvertingText();

        if (!isEnableL2Converter()) {
            commitText(false);
        }

        appendStrSegment(new StrSegment(input));

        mFunfun = 0;
        setFunFun(mFunfun);
        mStatus = STATUS_INPUT_EDIT;

        updateViewStatusForPrediction(true, true);
    }

    /**
     * Commit text and input connection.
     *
     * @param commitText  committed text
     */
    public void commitTextToInputConnection(CharSequence commitText) {
        if (commitText != null) {
            InputConnection inputConnection = getInputConnection();

            inputConnection.commitText(commitText, 1);
            Bundle data = new Bundle();
            if ((mStrokeOfCommitText != null) && (mStrokeOfCommitText.length() > 0)) {
                data.putString(COMMIT_TEXT_THROUGH_KEY_YOMI, mStrokeOfCommitText);
            } else {
                data.putString(COMMIT_TEXT_THROUGH_KEY_YOMI, commitText.toString());
            }
            inputConnection.performPrivateCommand(COMMIT_TEXT_THROUGH_ACTION, data);
        }
        mStrokeOfCommitText = null;
    }

    /**
     * Set candidate view type.
     *
     * @param isFullCandidate {@code true} - to full view.
     *                        {@code false} - to normal view.
     */
    private void setCandidateIsViewTypeFull(boolean isFullCandidate) {
        mFullCandidate = isFullCandidate;
        if (mFullCandidate) {
            mStatus |= STATUS_CANDIDATE_FULL;
            getCandidatesViewManager().setViewType(CandidatesViewManager.VIEW_TYPE_FULL);
            updateFullscreenMode();
            if (!mEngineState.isSymbolList()) {
                getInputViewManager().hideInputView();
            }
        } else {
            mStatus &= ~STATUS_CANDIDATE_FULL;
            getCandidatesViewManager().setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
            updateFullscreenMode();
            getInputViewManager().showInputView();
        }
    }

    /**
     * Set enable tabs.
     *
     * @param enableEmoji    {@code true}  - Emoji is enabled.
     *                       {@code false} - Emoji is disabled.
     * @param enableEmoticon {@code true}  - Emoticon is enabled.
     *                       {@code false} - Emoticon is disabled.
     * @param enableEmojiUNI6 {@code true}  - EmojiUNI6 is enabled.
     *                        {@code false} - EmojiUNI6 is disabled.
     */
    private void setEnabledTabs(boolean enableEmoji, boolean enableEmoticon, boolean enableEmojiUNI6) {
        mTextCandidatesViewManager.setEnableEmoji(enableEmoji);
        mTextCandidatesViewManager.setEnableEmoticon(enableEmoticon);
        mTextCandidatesViewManager.setEnableEmojiUNI6(enableEmojiUNI6);

        mTextCandidatesViewManager.setEnableDecoEmoji(mEnableDecoEmoji);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#commitMushRoomString */
    @Override protected void commitMushRoomString() {
        InputConnection inputConnection = getInputConnection();
        PowerManager pm = (PowerManager) mWnnSwitcher.getSystemService(Context.POWER_SERVICE);
        KeyguardManager keyguard = (KeyguardManager) mWnnSwitcher.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isScreenLock = keyguard.inKeyguardRestrictedInputMode();
        if (mMushroomResultString != null && getInputConnection() != null && pm.isScreenOn() && !isScreenLock) {
            if (1 <= mMushroomResultString.length()) {
                if (mIsGetTextType && mMushroomResulttype) {
                    inputConnection.deleteSurroundingText(Integer.MAX_VALUE / 2,Integer.MAX_VALUE / 2);
                }
                int wordcnt = mMushroomSelectionEnd - mMushroomSelectionStart;
                if ((wordcnt != 0) && (mEditorSelectionEnd == mEditorSelectionStart)
                        && (mEditorSelectionStart == mMushroomSelectionEnd)) {
                    if (wordcnt > 0) {
                        inputConnection.deleteSurroundingText(wordcnt, 0);
                    } else {
                        inputConnection.deleteSurroundingText(0, Math.abs(wordcnt));
                    }
                    mMushroomSelectionStart = 0;
                    mMushroomSelectionEnd  = 0;
                }
                mStrokeOfCommitText = mMushroomResultString.toString();
                inputConnection.commitText(mMushroomResultString, 1);
                Bundle data = new Bundle();
                if ((mStrokeOfCommitText != null) && (mStrokeOfCommitText.length() > 0)) {
                    data.putString(COMMIT_TEXT_THROUGH_KEY_YOMI, mStrokeOfCommitText);
                } else {
                    data.putString(COMMIT_TEXT_THROUGH_KEY_YOMI, mMushroomResultString.toString());
                }
                inputConnection.performPrivateCommand(COMMIT_TEXT_THROUGH_ACTION, data);
                mStrokeOfCommitText = null;
            }
            mMushroomResultString = null;
            mWnnSwitcher.setIsUnlockReceived(false);
        }
    }

    /**
     * Set lattice input mode.
     *
     * @param lattice {@code true} - Lattice input mode enabled.
     *                {@code false} - Lattice input mode disabled.
     */
    public static void setLatticeInputMode(boolean lattice) {
        mLatticeInputMode = lattice;
    }

    /**
     * Return whether lattice input mode is active or not.
     *
     * @return {@code true} - if lattice input mode is active.
     *         {@code false} - if otherwise..
     */
    public static boolean isLatticeInputMode() {
        return mLatticeInputMode;
    }

    /**
     * Append a toggle input string to the composing text.
     *
     * @param  appendString  append string
     * @return  {@code true} - append complete;
     *          {@code false} - the composing text length is exceed;
     */
    private boolean appendToggleString(String appendString) {
        boolean appendComplete = appendStrSegment(new StrSegment(appendString));
        if (!isLatticeInputMode()) {
            forwardCursor();
        }
        return appendComplete;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#commitVoiceResult */
    @Override protected void commitVoiceResult(String result) {
        clearCommitInfo();
        initCommitInfoForWatchCursor();
        commitText(result);
        checkCommitInfo();
    }

    /**
      * Switch input mode.
     */
    private void switchCharset(){
        DefaultSoftKeyboardJAJP inputViewManager = (DefaultSoftKeyboardJAJP)getInputViewManager();
        /* change Japanese <-> English mode */
        if (mEngineState.isEnglish()) {
            // English mode to Japanese mode
            inputViewManager.changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA);
        } else {
            // Japanese mode to English mode
            inputViewManager.changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET);
        }
        mTextCandidatesViewManager.clearCandidates();
    }

    /**
      * Switch symbol list.
     */
    private void switchSymbolList(){
        changeSymbolEngineState(new EngineState(), IWnnSymbolEngine.MODE_NONE);
    }

    /**
     * Convert ComposingText to CommitText with DecoEmoji span.
     *
     * @param  composingText  ComposingText
     * @param  targetLayer  Layer of converting
     * @return  CommitText
     */
    private CharSequence convertComposingToCommitText(ComposingText composingText, int targetLayer) {
        return convertComposingToCommitText(composingText, targetLayer, 0,
                composingText.size(targetLayer) - 1);
    }

    /**
     * Convert ComposingText to CommitText with DecoEmoji span.
     *
     * @param  composingText  ComposingText
     * @param  targetLayer  Layer of converting
     * @param  from  Convert range from
     * @param  to  Convert range to
     * @return  CommitText
     */
    private CharSequence convertComposingToCommitText(ComposingText composingText, int targetLayer,
            int from, int to) {
        if (mEnableDecoEmoji) {
            if (mEngineState.isRenbun()) {
                return DecoEmojiUtil.convertComposingToCommitText(composingText, targetLayer, from, to);
            }
        }
        return composingText.toString(targetLayer, from, to);
    }

    /**
     * In the composingText, convert to a given character type, to be displayed.
     *
     * @param type          Casing to be converted
     */
    private void converterComposingText(int type) {
        ComposingText composingText = getComposingText();
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        WnnEngine converter = getConverter();

        // By converting the specified character type.
        converter.getgijistr(composingText, type);
        // clears variables for the prediction
        mFunfun = 0;
        setFunFun(mFunfun);
        mExactMatchMode = false;
        setConvertingForFuncKeyType(type);
        // In pseudo-candidate string and update the display of composingText.
        updateViewStatus(ComposingText.LAYER2, false, true);
        // To clear the candidatesview.
        candidatesViewManager.clearCandidates();
    }

    /**
     * Handle a Tab key event.
     *
     * @param  ev  KeyEvent
     */
    private void processTabKeyEvent(KeyEvent ev) {
        if (null == ev) {
            return;
        }

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        boolean isShowCand = candidatesViewManager.getCurrentView().isShown();
        if (!isShowCand) {
            return;
        }
        boolean isFocusCand = candidatesViewManager.isFocusCandidate();

        if (ev.isShiftPressed()) {
            if (isFocusCand) {
                processLeftKeyEvent();
            } else {
                // Focus on the final candidate.
                processEndKeyEvent();
            }
        } else {
            if (isFocusCand) {
                processRightKeyEvent();
            } else {
                // Focus on the first candidate.
                processDownKeyEvent();
            }
        }
    }

    /**
     *  Kana_Roman_Input mode state.
     *
     *  @return {@code true}  Kana input mode;
     *          {@code false} Roman input mode.
     */
    private boolean isEnableKanaInput() {
         return (mDirectKana && !mHardKeyboardHidden && !mEngineState.isEnglish());
    }

    /**
     * Is enable hard keyboard 12Key long press keycode.
     *
     * @param  keyCode  keycode.
     * @return  {@code true} if enable long press keycode; {@code false} if not.
     */
    private boolean isHardKeyboard12KeyLongPress(int keyCode) {
        boolean isLongPress = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_SOFT_LEFT:
        case KeyEvent.KEYCODE_SOFT_RIGHT:
        case KeyEvent.KEYCODE_DEL:
            isLongPress = true;
            break;

        default:
            break;
        }
        return isLongPress;
    }

    /**
     * Key event handler for hardware 12Keyboard.
     *
     * @param keyEvent A key event
     * @return  {@code true} if the event is handled in this method.
     */
    private boolean processHardware12Keyboard(KeyEvent keyEvent) {
        boolean ret = false;
        if (mEnableHardware12Keyboard && (keyEvent != null)) {
            int keyCode = keyEvent.getKeyCode();

            if (isHardKeyboard12KeyLongPress(keyCode)) {
                if (keyEvent.getRepeatCount() == 0) {
                    keyEvent.startTracking();
                }
                ret = true;
            } else {
                Integer code = HW12KEYBOARD_KEYCODE_REPLACE_TABLE.get(keyCode);
                if (code != null) {
                    if (keyEvent.getRepeatCount() == 0) {
                        ((DefaultSoftKeyboard) getInputViewManager()).onKey(code.intValue(), null);
                    }
                    ret = true;
                }
            }
        }
        return ret;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#restartSelf */
    public void restartSelf(EditorInfo attribute) {
        mAttribute = attribute;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RESTART), 0);
    }

    /**
     * Send key event of navigation keypad arrow key.
     *
     * @param keyEvent KeyEvent
     * @return {@code true} if the event is processed in this method.
     */
    private boolean sendNavigationKeypadKeyEvent(KeyEvent keyEvent) {
        InputConnection connection = getCurrentInputConnection();
        if (connection != null) {
            if (((DefaultSoftKeyboardJAJP)getInputViewManager()).getSelectionMode()) {
                connection.sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
                getSwitcher().sendDownUpKeyEvents(keyEvent.getKeyCode());
                connection.sendKeyEvent(
                        new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
            } else {
                getSwitcher().sendDownUpKeyEvents(keyEvent.getKeyCode());
            }
            return true;
        }
        return false;
    }

    /**
     * Emoji or DecoEmoji flag setter
     *
     */
    private void setEmojiFlag() {
        IWnnLanguageSwitcher switcher = getSwitcher();
        switcher.setEmojiFlag(mEnableEmoji | mEnableDecoEmoji);
        return ;
    }

    /**
     * Get focused candidate.
     *
     * @return Focused candidate.
     */
    private WnnWord getFocusedCandidate() {
        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        WnnWord word = candidatesViewManager.getFocusedWnnWord();
        if (((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI) != 0)
                || (word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NO_CANDIDATE) != 0
                || (word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN) != 0) {
            word = mTextCandidatesViewManager.getFirstWnnWord();
        }
        return word;
    }

    /**
     * show switch input mode dialog. (roman input / kana input)
     */
    private void showSwitchKanaRomanModeDialog() {
        BaseInputView baseInputView = (BaseInputView)(((DefaultSoftKeyboard) getInputViewManager()).getCurrentView());
        AlertDialog.Builder builder = new AlertDialog.Builder(baseInputView.getContext());
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.ti_dialog_button_ok_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                switchKanaRomanMode();
            }
        });
        builder.setNegativeButton(R.string.ti_dialog_button_cancel_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        String message = baseInputView.getResources().getString(R.string.ti_switch_kana_input_mode_txt);
        if (mDirectKana) {
            message = baseInputView.getResources().getString(R.string.ti_switch_roman_input_mode_txt);
        } // else {}
        builder.setMessage(message);

        builder.setTitle(R.string.ti_preference_kana_roman_input_mode_title_txt);
        baseInputView.showDialog(builder);
    }

    /**
     * Switch input mode.
     */
    public void switchKanaRomanMode() {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        SharedPreferences.Editor editor = pref.edit();
        Resources res = getResources();
        if (mDirectKana) {
            editor.putString("kana_roman_input", res.getString(R.string.kana_roman_input_mode_list_item_roman));
            mDirectKana = false;
        } else {
            editor.putString("kana_roman_input", res.getString(R.string.kana_roman_input_mode_list_item_kana));
            mDirectKana = true;
        }
        editor.commit();

        DefaultSoftKeyboardJAJP inputViewManager = (DefaultSoftKeyboardJAJP)getInputViewManager();
        if (mEngineState.isEnglish()) {
            inputViewManager.changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET);
        } else {
            inputViewManager.changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA);
        }
        mTextCandidatesViewManager.clearCandidates();
    }

    /**
     * Calculate size of the stroke to delete
     *
     */
    private void calculateSizeOfDeleteStroke() {
        mSizeOfDeleteStroke = 0;

        if ((isEnglishPrediction()) || (mStrokeOfCommitText == null)) {
            return;
        } // else {}
        if (getCandidatesViewManager().getStrokeLength() < 0) {
            mSizeOfDeleteStroke = mStrokeOfCommitText.length();
        } else {
            mSizeOfDeleteStroke = getCandidatesViewManager().getStrokeLength();
        }
        return ;
    }

    /**
     * Handle message "MSG_TOGGLE_TIME_LIMIT".
     *
     */
    @Override protected void processToggleTimeLimit() {
        if ((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT) {
            mStatus = STATUS_INPUT_EDIT | STATUS_AUTO_CURSOR_DONE;
            DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)getInputViewManager();
            keyboard.setKeepShiftMode(true);
            updateViewStatus(mTargetLayer, false, false);
            keyboard.setKeepShiftMode(false);
        } // else {}
    }
}
