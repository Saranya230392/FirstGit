/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;    //for debug
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import jp.co.omronsoft.iwnnime.ml.Keyboard;
import jp.co.omronsoft.iwnnime.ml.Keyboard.Key;
import jp.co.omronsoft.iwnnime.ml.KeyboardView;
import jp.co.omronsoft.iwnnime.ml.KeyboardView.OnKeyboardActionListener;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;
import jp.co.omronsoft.iwnnime.ml.standardcommon.LanguageManager;
import jp.co.omronsoft.iwnnime.ml.hangul.ko.DefaultSoftKeyboardKorean;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import jp.co.omronsoft.iwnnime.ml.jajp.IWnnImeJaJp;

import java.util.List;
import java.util.Iterator;
import java.util.Locale;

import com.lge.ime.util.LgeImeCliptrayManager;

/**
 * The default software keyboard class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class DefaultSoftKeyboard implements
                                     InputViewManager, KeyboardView.OnKeyboardActionListener,
                                     OnFlickKeyboardActionListener, View.OnTouchListener {
    /*
     *----------------------------------------------------------------------
     * key codes for a software keyboard
     *----------------------------------------------------------------------
     */
    /** Change the keyboard language */
    public static final int KEYCODE_CHANGE_LANG = -500;

    /* for Japanese 12-key keyboard */
    /** Japanese 12-key keyboard [1] */
    public static final int KEYCODE_JP12_1           = -201;
    /** Japanese 12-key keyboard [2] */
    public static final int KEYCODE_JP12_2           = -202;
    /** Japanese 12-key keyboard [3] */
    public static final int KEYCODE_JP12_3           = -203;
    /** Japanese 12-key keyboard [4] */
    public static final int KEYCODE_JP12_4           = -204;
    /** Japanese 12-key keyboard [5] */
    public static final int KEYCODE_JP12_5           = -205;
    /** Japanese 12-key keyboard [6] */
    public static final int KEYCODE_JP12_6           = -206;
    /** Japanese 12-key keyboard [7] */
    public static final int KEYCODE_JP12_7           = -207;
    /** Japanese 12-key keyboard [8] */
    public static final int KEYCODE_JP12_8           = -208;
    /** Japanese 12-key keyboard [9] */
    public static final int KEYCODE_JP12_9           = -209;
    /** Japanese 12-key keyboard [0] */
    public static final int KEYCODE_JP12_0           = -210;
    /** Japanese 12-key keyboard [#] */
    public static final int KEYCODE_JP12_SHARP       = -211;
    /** Japanese 12-key keyboard [*] */
    public static final int KEYCODE_JP12_ASTER       = -213;
    /** Japanese 12-key keyboard [DEL] */
    public static final int KEYCODE_JP12_BACKSPACE   = -214;
    /** Japanese 12-key keyboard [SPACE] */
    public static final int KEYCODE_JP12_SPACE       = -215;
    /** Japanese 12-key keyboard [ENTER] */
    public static final int KEYCODE_JP12_ENTER       = -216;
    /** Japanese 12-key keyboard [RIGHT ARROW] */
    public static final int KEYCODE_JP12_RIGHT       = -217;
    /** Japanese 12-key keyboard [LEFT ARROW] */
    public static final int KEYCODE_JP12_LEFT        = -218;
    /** Japanese 12-key keyboard [REVERSE TOGGLE] */
    public static final int KEYCODE_JP12_REVERSE     = -219;
    /** Japanese 12-key keyboard [CLOSE] */
    public static final int KEYCODE_JP12_CLOSE       = -220;
    /** Japanese 12-key keyboard [KEYBOARD TYPE CHANGE] */
    public static final int KEYCODE_JP12_KBD         = -221;
    /** Japanese 12-key keyboard [EMOJI] */
    public static final int KEYCODE_JP12_EMOJI       = -222;
    /** Japanese 12-key keyboard [FULL-WIDTH HIRAGANA MODE] */
    public static final int KEYCODE_JP12_ZEN_HIRA    = -223;
    /** Japanese 12-key keyboard [FULL-WIDTH NUMBER MODE] */
    public static final int KEYCODE_JP12_ZEN_NUM     = -224;
    /** Japanese 12-key keyboard [FULL-WIDTH ALPHABET MODE] */
    public static final int KEYCODE_JP12_ZEN_ALPHA   = -225;
    /** Japanese 12-key keyboard [FULL-WIDTH KATAKANA MODE] */
    public static final int KEYCODE_JP12_ZEN_KATA    = -226;
    /** Japanese 12-key keyboard [HALF-WIDTH KATAKANA MODE] */
    public static final int KEYCODE_JP12_HAN_KATA    = -227;
    /** Japanese 12-key keyboard [HALF-WIDTH NUMBER MODE] */
    public static final int KEYCODE_JP12_HAN_NUM     = -228;
    /** Japanese 12-key keyboard [HALF-WIDTH ALPHABET MODE] */
    public static final int KEYCODE_JP12_HAN_ALPHA   = -229;
    /** Japanese 12-key keyboard [MODE TOGGLE CHANGE] */
    public static final int KEYCODE_JP12_TOGGLE_MODE = -230;
    /** Japanese 12-key keyboard [UP ARROW] */
    public static final int KEYCODE_JP12_UP          = -235;
    /** Japanese 12-key keyboard [DOWN ARROW] */
    public static final int KEYCODE_JP12_DOWN        = -236;
    /** Japanese 12-key keyboard [BLANK_KEY1] */
    public static final int KEYCODE_JP12_BLANK_KEY1  = -310;
    /** Japanese 12-key keyboard [BLANK_KEY2] */
    public static final int KEYCODE_JP12_BLANK_KEY2  = -412;

    /** Key code for symbol keyboard ALT key */
    public static final int KEYCODE_4KEY_MODE        = -231;
    /** Key code for symbol keyboard up key */
    public static final int KEYCODE_4KEY_UP          = -232;
    /** Key code for symbol keyboard down key */
    public static final int KEYCODE_4KEY_DOWN        = -233;
    /** Key code for symbol keyboard DEL key */
    public static final int KEYCODE_4KEY_CLEAR       = -234;

    /* for Qwerty keyboard */
    /** Qwerty keyboard [DEL] */
    public static final int KEYCODE_QWERTY_BACKSPACE    = -100;
    /** Qwerty keyboard [ENTER] */
    public static final int KEYCODE_QWERTY_ENTER        = -101;
    /** Qwerty keyboard [SHIFT] */
    public static final int KEYCODE_QWERTY_SHIFT        = Keyboard.KEYCODE_SHIFT;
    /** Qwerty number keyboard [SHIFT] */
    public static final int KEYCODE_QWERTY_NUM_SHIFT    = -2;
    /** Phone keyboard [SHIFT] */
    public static final int KEYCODE_JP12_PHONE_SHIFT    = -3;
    /** 12key ALPHABET keyboard [SHIFT] */
    public static final int KEYCODE_JP12_ALPHABET_SHIFT = -4;
    /** Qwerty keyboard [ALT] */
    public static final int KEYCODE_QWERTY_ALT          = -103;
    /** Qwerty keyboard [KEYBOARD TYPE CHANGE] */
    public static final int KEYCODE_QWERTY_KBD          = -104;
    /** Qwerty keyboard [CLOSE] */
    public static final int KEYCODE_QWERTY_CLOSE        = -105;
    /** Japanese Qwerty keyboard [EMOJI] */
    public static final int KEYCODE_QWERTY_EMOJI        = -106;
    /** Japanese Qwerty keyboard [FULL-WIDTH HIRAGANA MODE] */
    public static final int KEYCODE_QWERTY_ZEN_HIRA     = -107;
    /** Japanese Qwerty keyboard [FULL-WIDTH NUMBER MODE] */
    public static final int KEYCODE_QWERTY_ZEN_NUM      = -108;
    /** Japanese Qwerty keyboard [FULL-WIDTH ALPHABET MODE] */
    public static final int KEYCODE_QWERTY_ZEN_ALPHA    = -109;
    /** Japanese Qwerty keyboard [FULL-WIDTH KATAKANA MODE] */
    public static final int KEYCODE_QWERTY_ZEN_KATA     = -110;
    /** Japanese Qwerty keyboard [HALF-WIDTH KATAKANA MODE] */
    public static final int KEYCODE_QWERTY_HAN_KATA     = -111;
    /** Qwerty keyboard [HALF-WIDTH NUMBER MODE] */
    public static final int KEYCODE_QWERTY_HAN_NUM      = -112;
    /** Qwerty keyboard [HALF-WIDTH ALPHABET MODE] */
    public static final int KEYCODE_QWERTY_HAN_ALPHA    = -113;
    /** Qwerty keyboard [HALF-WIDTH CYRILLIC MODE] */
    public static final int KEYCODE_QWERTY_HAN_CYRILLIC = -119;
    /** Qwerty keyboard [MODE TOGGLE CHANGE] */
    public static final int KEYCODE_QWERTY_TOGGLE_MODE  = -114;
    /** Qwerty keyboard [PINYIN MODE] */
    public static final int KEYCODE_QWERTY_PINYIN       = -115;
    /** Qwerty keyboard [TOGGLE COMMA] */
    public static final int KEYCODE_TOGGLE_COMMA        = -116;
    /** Qwerty keyboard [TOGGLE EXCLAMATION] */
    public static final int KEYCODE_TOGGLE_EXCLAMATION  = -117;
    /** Qwerty keyboard [TOGGLE INVERT_EXCLAMATION] */
    public static final int KEYCODE_TOGGLE_INVERTED_EXCLAMATION = -118;
    /** Qwerty keyboard [TOGGLE GREEK_EXCLAMATION] */
    public static final int KEYCODE_TOGGLE_GREEK_EXCLAMATION = -120;
    /** Key code for displaying iWnn IME menu (for XLarge)*/
    public static final int KEYCODE_MENU = -121;
    /** Korean Qwerty keyboard [TO JAPANESE KEYBOARD MODE] */
    public static final int KEYCODE_QWERTY_JP_KEYBOARD = -122;
    /* NOTE: last number is -122 (KEYCODE_QWERTY_JP_KEYBOARD) */

    /** Key code for Chinese (pinyin) */
    public static final int KEYCODE_TOGGLE_TW_PINYIN = -317;

    /** Key code for Chinese (alphabet) */
    public static final int KEYCODE_TOGGLE_TW_ALPHABET = -318;

    /** Key code for Russian (Cyrillic) */
    public static final int KEYCODE_TOGGLE_RU_CYRILLIC = -319;

    /** Key code for Russian (alphabet) */
    public static final int KEYCODE_TOGGLE_RU_ALPHABET = -320;

    /** Key code for lattice input(left) */
    public static final int KEYCODE_PERIOD_LATTICE_LEFT = 12300;

    /** Key code for lattice input(right) */
    public static final int KEYCODE_PERIOD_LATTICE_RIGHT = 12301;

    /** Key code for showing .com url popup */
    public static final int KEYCODE_COM_URL  = -415;

    /** Key code for showing .com e-mail popup */
    public static final int KEYCODE_COM_EMAIL  = -416;

    /** Key code for navigation keypad up arrow key */
    public static final int KEYCODE_KEYPAD_UP = -417;

    /** Key code for navigation keypad down arrow key */
    public static final int KEYCODE_KEYPAD_DOWN = -418;

    /** Key code for navigation keypad left arrow key */
    public static final int KEYCODE_KEYPAD_LEFT = -419;

    /** Key code for navigation keypad right arrow key */
    public static final int KEYCODE_KEYPAD_RIGHT = -420;

    /** Key code for navigation keypad select/deselect key */
    public static final int KEYCODE_KEYPAD_SELECT = -421;

    /** Key code for navigation keypad close key */
    public static final int KEYCODE_KEYPAD_CLOSE = -422;

    /** Key code for navigation keypad copy key */
    public static final int KEYCODE_KEYPAD_COPY = -423;

    /** Key code for navigation keypad paste key */
    public static final int KEYCODE_KEYPAD_PASTE = -424;

    /** Key code for navigation keypad cut key */
    public static final int KEYCODE_KEYPAD_CUT = -425;

    /** Key code for switching to voice mode */
    public static final int KEYCODE_SWITCH_VOICE = -311;

    /** Key code for starting voice input */
    public static final int KEYCODE_START_VOICE_INPUT = -312;

    /** Japanese keyboard [FunctionKey] */
    public static final int KEYCODE_FUNCTION_KEY = -426;

    /** change language(Japanese) */
    public static final int KEYCODE_CHANGE_LANGUAGE_JA = -612;

    /** change language(Korean) */
    public static final int KEYCODE_CHANGE_LANGUAGE_KO = -613;

     /** Key code for navigation keypad home key */
    public static final int KEYCODE_KEYPAD_HOME = -427;

     /** Key code for navigation keypad end key */
    public static final int KEYCODE_KEYPAD_END = -428;

     /** Key code for navigation keypad all key */
    public static final int KEYCODE_KEYPAD_ALL = -429;

    /* KeyDrawable ID */
    /** OK */
    private static final int KEY_ID_OK = 107;
    /** OK(preview) */
    private static final int KEY_ID_FLICK_PREVIEW_OK = 306;
    /** Go */
    private static final int KEY_ID_GO = 108;
    /** GO(preview) */
    private static final int KEY_ID_FLICK_PREVIEW_GO = 307;
    /** Next */
    private static final int KEY_ID_NEXT = 109;
    /** Next(preview) */
    private static final int KEY_ID_FLICK_PREVIEW_NEXT = 308;
    /** Done */
    private static final int KEY_ID_DONE = 110;
    /** Done(preview) */
    private static final int KEY_ID_FLICK_PREVIEW_DONE = 309;
    /** Send */
    private static final int KEY_ID_SEND = 111;
    /** Send(preview) */
    private static final int KEY_ID_FLICK_PREVIEW_SEND = 310;
    /** Previous */
    private static final int KEY_ID_PREVIOUS = 112;
    /** Previous(preview) */
    private static final int KEY_ID_FLICK_PREVIEW_PREVIOUS = 311;

    /** Key code for switching to full-width number mode with temporarily inputed */
    public static final int KEYCODE_SWITCH_FULL_NUMBER_TEMP_INPUT = -404;

    /** Key code for switching to half-width number mode with temporarily inputed */
    public static final int KEYCODE_SWITCH_HALF_NUMBER_TEMP_INPUT = -408;

    /** Key code for switching to Russian number mode with temporarily inputed */
    public static final int KEYCODE_SWITCH_RU_NUMBER_TEMP_INPUT = -409;

    /** Key code for switching to Chinese number mode with temporarily inputed */
    public static final int KEYCODE_SWITCH_CH_NUMBER_TEMP_INPUT = -410;

    /** Qwerty keyboard [MODE TOGGLE CHANGE] with temporarily inputed */
    public static final int KEYCODE_QWERTY_TOGGLE_MODE_TEMP_INPUT  = -411;

    /** Qwerty keyboard [FULL KUTEN] */
    public static final int KEYCODE_TOGGLE_FULL_KUTEN = -430;
    /** Qwerty keyboard [HALF KUTEN] */
    public static final int KEYCODE_TOGGLE_HALF_KUTEN = -431;
    /** Qwerty keyboard [FULL PERIOD] */
    public static final int KEYCODE_TOGGLE_FULL_PERIOD = -432;
    /** Qwerty keyboard [HALF PERIOD] */
    public static final int KEYCODE_TOGGLE_HALF_PERIOD = -433;
    public static final int KEY_ID_PREVIEW_SPACE_JP  = 479;
    public static final int KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_1ST = 480;
    public static final int KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_2ND = 481;
    public static final int KEY_ID_PREVIEW_PHONE_SYM            = 502;
    public static final int KEY_ID_PREVIEW_PHONE_123            = 503;


    public final static String IME_KEYPAD_ICON_DEL_POPUP
            = "ime_keypad_icon_del_popup";
    public final static String IME_KEYPAD_ICON_JP_ARROW_L_POPUP
            = "ime_keypad_icon_jp_arrow_l_popup";
    public final static String IME_KEYPAD_ICON_JP_ARROW_R_POPUP
            = "ime_keypad_icon_jp_arrow_r_popup";
    public final static String IME_KEYPAD_ICON_JP_MIC_POPUP
            = "ime_keypad_icon_jp_mic_popup";
    public final static String IME_KEYPAD_ICON_KBD_NORMAL_POPUP
            = "ime_keypad_input_icon_kbd_normal";
    public final static String IME_KEYPAD_ICON_JP_HANDWRITING_POPUP
            = "ime_keypad_icon_jp_handwriting_popup";
    public final static String IME_KEYPAD_ICON_SETTING_POPUP
            = "ime_keypad_input_icon_setting_normal";
    public final static String IME_KEYPAD_ICON_JP_SYM_POPUP
            = "ime_keypad_icon_jp_sym_popup";
    public final static String IME_KEYPAD_ICON_JP_SPACE_POPUP
            = "ime_keypad_icon_jp_space_popup";
    public final static String IME_KEYPAD_ICON_JP_SEARCH_POPUP
            = "ime_keypad_icon_jp_search_popup";
    public final static String IME_KEYPAD_ICON_SEARCH_POPUP
            = "ime_keypad_icon_search_popup";
    public final static String IME_KEYPAD_ICON_JP_ENTER_POPUP
            = "ime_keypad_icon_jp_enter_popup";
    public final static String IME_KEYPAD_ICON_ENTER_POPUP
            = "ime_keypad_icon_enter_popup";
    public final static String IME_KEYPAD_ICON_JP_SETTING_POPUP
            = "ime_keypad_icon_jp_setting_popup";
    public final static String IME_KEYPAD_ICON_JP_REVERSE_POPUP
            = "ime_keypad_icon_jp_reverse_popup";
    public final static String IME_KEYPAD_ICON_JP_4WAY_UP_POPUP
            = "ime_keypad_icon_jp_4way_up_popup";
    public final static String IME_KEYPAD_ICON_JP_4WAY_DOWN_POPUP
            = "ime_keypad_icon_jp_4way_down_popup";
    public final static String IME_KEYPAD_ICON_JP_4WAY_LEFT_POPUP
            = "ime_keypad_icon_jp_4way_left_popup";
    public final static String IME_KEYPAD_ICON_JP_4WAY_RIGHT_POPUP
            = "ime_keypad_icon_jp_4way_right_popup";
    public final static String IME_KEYPAD_ICON_JP_4WAY_HOME_POPUP
            = "ime_keypad_icon_jp_4way_home_popup";
    public final static String IME_KEYPAD_ICON_JP_4WAY_END_POPUP
            = "ime_keypad_icon_jp_4way_end_popup";
    public final static String IME_KEYPAD_ICON_JP_DAKUTEN_POPUP
            = "ime_keypad_icon_jp_dakuten_popup";
    public final static String IME_SYMBOL_ICON_TEXT_POPUP
    = "ime_symbol_icon_text_popup";
    public final static String IME_SYMBOL_ICON_UP_POPUP
            = "ime_symbol_icon_up_popup";
    public final static String IME_SYMBOL_ICON_DOWN_POPUP
            = "ime_symbol_icon_down_popup";
    public final static String IME_BTN_FUNC_SHIFT_BLK
            = "ime_btn_func_shift_blk";
    public final static String IME_KEYPAD_INPUT_ICON_10KBD_NORMAL
            = "ime_keypad_input_icon_10kbd_normal";
    
    /** Flag to call DOCOMO voice editor app,
        this value is changed to false in the build script of KDDI */
    private static final boolean ENABLE_DOCOMO = true;

    /** OpenWnn instance which hold this software keyboard*/
    protected OpenWnn      mWnn;

    /** Current keyboard view */
    protected KeyboardView mKeyboardView;
    /** Flick keyboard view */
    protected FlickKeyboardView mFlickKeyboardView;
    /** Multi touch keyboard view */
    protected MultiTouchKeyboardView mMultiTouchKeyboardView;

    /** View objects (main side) */
    protected BaseInputView mMainView;
    /** View objects (main side layout) */
    protected ViewGroup mMainLayout;
    /** View objects (sub side) */
    protected ViewGroup mSubView;

    /** View objects (one handed top)*/
    private ViewGroup mOneHandedTopView;

    /** View objects (one handed bottom)*/
    private ViewGroup mOneHandedBottomView;

    /** View objects (one handed left)*/
    protected ViewGroup mOneHandedLeftView;

    /** View objects (one handed right)*/
    protected ViewGroup mOneHandedRightView;

    /** Current keyboard definition */
    protected Keyboard mCurrentKeyboard;

    /** Caps lock state */
    protected boolean mCapsLock;

    /** Input restraint */
    protected boolean mDisableKeyInput = true;
    /**
     * WnnKeyboardFactory surfaces
     * <br>
     * WnnKeyboardFactory[language][portrait/landscape][keyboard type]
     *             [shift off/on][input key type][key-mode]
     */
    protected WnnKeyboardFactory[][][][][][][] mKeyboard;

    /* languages */
    /** Current language */
    protected int mCurrentLanguage;
    /** Language (English) */
    public static final int LANG_EN  = 0;
    /** Language (Japanese) */
    public static final int LANG_JA  = 1;
    /** Language (Chinese) */
    public static final int LANG_CN  = 2;
    /** Language (Hangle) */
    public static final int LANG_KO  = 3;

    /* portrait/landscape */
    /** State of the display */
    protected int mDisplayMode = 0;
    /** Display mode (Portrait) */
    public static final int PORTRAIT  = 0;
    /** Display mode (Landscape) */
    public static final int LANDSCAPE = 1;

    /* keyboard type */
    /** Current keyboard type */
    protected int mCurrentKeyboardType;
    /** Keyboard (QWERTY keyboard) */
    public static final int KEYBOARD_QWERTY  = 0;
    /** Keyboard (12-keys keyboard) */
    public static final int KEYBOARD_12KEY   = 1;
    /** Keyboard (navigation keypad) */
    public static final int KEYBOARD_NAVIGATION = 2;

    /** State of the shift key */
    protected int mShiftOn = 0;
    /** Shift key off */
    public static final int KEYBOARD_SHIFT_OFF = 0;
    /** Shift key on */
    public static final int KEYBOARD_SHIFT_ON  = 1;

    /** Voice input off */
    public static final int KEYBOARD_VOICE_OFF = 0;
    /** Voice input on */
    public static final int KEYBOARD_VOICE_ON  = 1;

    /** Qwerty Left/Right Key off */
    public static final int QWERTY_KEYBOARD_LEFT_RIGHT_KEY_OFF = 0;
    /** Qwerty Left/Right Key  on */
    public static final int QWERTY_KEYBOARD_LEFT_RIGHT_KEY_ON  = 1;

    /* key-modes */
    /** Current key-mode */
    protected static int mCurrentKeyMode;

    /* key-modes for English */
    /** English key-mode (alphabet) */
    public static final int KEYMODE_EN_ALPHABET = 0;
    /** English key-mode (number) */
    public static final int KEYMODE_EN_NUMBER   = 1;
    /** English key-mode (phone number) */
    public static final int KEYMODE_EN_PHONE    = 2;
    /** English key-mode (voice) */
    public static final int KEYMODE_EN_VOICE    = 3;

    /* key-modes for Japanese */
    /** Japanese key-mode (Full-width Hiragana) */
    public static final int KEYMODE_JA_FULL_HIRAGANA = 0;
    /** Japanese key-mode (Full-width alphabet) */
    public static final int KEYMODE_JA_FULL_ALPHABET = 1;
    /** Japanese key-mode (Full-width number) */
    public static final int KEYMODE_JA_FULL_NUMBER   = 2;
    /** Japanese key-mode (Full-width Katakana) */
    public static final int KEYMODE_JA_FULL_KATAKANA = 3;
    /** Japanese key-mode (Half-width alphabet) */
    public static final int KEYMODE_JA_HALF_ALPHABET = 4;
    /** Japanese key-mode (Half-width number) */
    public static final int KEYMODE_JA_HALF_NUMBER   = 5;
    /** Japanese key-mode (Half-width Katakana) */
    public static final int KEYMODE_JA_HALF_KATAKANA = 6;
    /** Japanese key-mode (Half-width phone number) */
    public static final int KEYMODE_JA_HALF_PHONE    = 7;
    /** Japanese key-mode (Voice input) */
    public static final int KEYMODE_JA_VOICE         = 8;
    /** Japanese key-mode (Numeric) */
    public static final int KEYMODE_JA_NUMERIC       = 9;
    /** Japanese key-mode (Numeric decimal) */
    public static final int KEYMODE_JA_NUMERIC_DECIMAL = 10;
    /** Japanese key-mode (Numeric signed) */
    public static final int KEYMODE_JA_NUMERIC_SIGNED  = 11;

    /* key-modes for Chinese */
    /** Chinese key-mode (pinyin) */
    public static final int KEYMODE_CN_PINYIN       = 0;
    /** Chinese key-mode (Full-width number) */
    public static final int KEYMODE_CN_FULL_NUMBER  = 1;
    /** Chinese key-mode (alphabet) */
    public static final int KEYMODE_CN_ALPHABET     = 2;
    /** Chinese key-mode (phone number) */
    public static final int KEYMODE_CN_PHONE        = 3;
    /** Chinese key-mode (Half-width number) */
    public static final int KEYMODE_CN_HALF_NUMBER  = 4;
    /** Chinese key-mode (voice) */
    public static final int KEYMODE_CN_VOICE        = 5;
    /** Chinese key-mode (bopomofo) */
    public static final int KEYMODE_CN_BOPOMOFO     = 6;

    /* key-modes for Russian */
    /** Russian key-mode (Cyrillic) */
    public static final int KEYMODE_RU_CYRILLIC = 0;
    /** Russian key-mode (alphabet) */
    public static final int KEYMODE_RU_ALPHABET = 1;
    /** Russian key-mode (number) */
    public static final int KEYMODE_RU_NUMBER   = 2;
    /** Russian key-mode (phone number) */
    public static final int KEYMODE_RU_PHONE    = 3;
    /** Russian key-mode (voice) */
    public static final int KEYMODE_RU_VOICE    = 4;

    /* key-modes for Korean */
    /** Korean key-mode (hangul) */
    public static final int KEYMODE_KO_HANGUL   = 0;
    /** Korean key-mode (alphabet) */
    public static final int KEYMODE_KO_ALPHABET = 1;
    /** Korean key-mode (number) */
    public static final int KEYMODE_KO_NUMBER   = 2;
    /** Korean key-mode (phone number) */
    public static final int KEYMODE_KO_PHONE    = 3;
    /** Korean key-mode (voice) */
    public static final int KEYMODE_KO_VOICE    = 4;

    /* key-modes for HARD */
    /** HARD key-mode (SHIFT_OFF_ALT_OFF) */
    public static final int HARD_KEYMODE_SHIFT_OFF_ALT_OFF     = 2;
    /** HARD key-mode (SHIFT_ON_ALT_OFF) */
    public static final int HARD_KEYMODE_SHIFT_ON_ALT_OFF      = 3;
    /** HARD key-mode (SHIFT_OFF_ALT_ON) */
    public static final int HARD_KEYMODE_SHIFT_OFF_ALT_ON      = 4;
    /** HARD key-mode (SHIFT_ON_ALT_ON) */
    public static final int HARD_KEYMODE_SHIFT_ON_ALT_ON       = 5;
    /** HARD key-mode (SHIFT_LOCK_ALT_OFF) */
    public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_OFF    = 6;
    /** HARD key-mode (SHIFT_LOCK_ALT_ON) */
    public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_ON     = 7;
    /** HARD key-mode (SHIFT_LOCK_ALT_LOCK) */
    public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK   = 8;
    /** HARD key-mode (SHIFT_OFF_ALT_LOCK) */
    public static final int HARD_KEYMODE_SHIFT_OFF_ALT_LOCK    = 9;
    /** HARD key-mode (SHIFT_ON_ALT_LOCK) */
    public static final int HARD_KEYMODE_SHIFT_ON_ALT_LOCK     = 10;

    /** DISPLAY_SCALE ldpi */
    public static final float DISPLAY_SCALE_LDPI              = 0.75f;
    /** DISPLAY_SCALE mdpi */
    public static final float DISPLAY_SCALE_MDPI              = 1.0f;
    /** DISPLAY_SCALE hdpi */
    public static final float DISPLAY_SCALE_HDPI              = 1.5f;
    /** DISPLAY_SCALE xhdpi */
    public static final float DISPLAY_SCALE_XHDPI             = 2.0f;

    /** Condition of Japanese key-mode (Full-width Hiragana) */
    public static int CONDITION_MODE_HIRAGANA                 = 0x0001;
    /** Condition of Japanese key-mode (Full-width alphabet) */
    public static int CONDITION_MODE_FULL_ALPHA               = 0x0002;
    /** Condition of Japanese key-mode (Full-width number) */
    public static int CONDITION_MODE_FULL_NUM                 = 0x0003;
    /** Condition of Japanese key-mode (Full-width Katakana) */
    public static int CONDITION_MODE_FULL_KATAKANA            = 0x0004;
    /** Condition of Japanese key-mode (Half-width alphabet) */
    public static int CONDITION_MODE_HALF_ALPHA               = 0x0005;
    /** Condition of Japanese key-mode (Half-width number) */
    public static int CONDITION_MODE_HALF_NUM                 = 0x0006;
    /** Condition of Japanese key-mode (Half-width Katakana) */
    public static int CONDITION_MODE_HALF_KATAKANA            = 0x0007;
    /** Condition of Korea */
    public static int CONDITION_MODE_HANGUL                   = 0x0008;
    /** Condition of PINYIN */
    public static int CONDITION_MODE_PINYIN                   = 0x0009;
    /** Condition of BOPOMOFO */
    public static int CONDITION_MODE_BOPOMOFO                 = 0x000A;
    /** Condition of Japanese key-mode */
    public static int CONDITION_MODE                          = 0x000F;
    /** Condition of Voice Input (Off) */
    public static int CONDITION_VOICE_OFF                     = 0x0010;
    /** Condition of Voice Input (On) */
    public static int CONDITION_VOICE_ON                      = 0x0020;
    /** Condition of Switch Ime (Off) */
    public static int CONDITION_SWITCH_IME_OFF                = 0x0040;
    /** Condition of Switch Ime (On) */
    public static int CONDITION_SWITCH_IME_ON                 = 0x0080;
    /** Condition of Cursor (Off) */
    public static int CONDITION_CURSOR_OFF                    = 0x0100;
    /** Condition of Cursor (On) */
    public static int CONDITION_CURSOR_ON                     = 0x0200;

    /** Whether the H/W keyboard is hidden. */
    protected boolean mHardKeyboardHidden = true;

    /** Whether the H/W 12key keyboard. */
    protected boolean mEnableHardware12Keyboard = false;

    /** Symbol keyboard */
    protected Keyboard mSymbolKeyboard;

    /** Symbol keyboard state */
    protected boolean mIsSymbolKeyboard = false;

    /* key input type */
    /** Current key-input-type */
    protected int mCurrentKeyInputType;

    /** touch key info*/
    private Keyboard.Key mTouchKeyInfo = null;

    /** key input type (NORMAL) */
    public static final int KEYINPUTTYPE_NORMAL = 0;
    /** key input type (URL) */
    public static final int KEYINPUTTYPE_URL = 1;
    /** key input type (MAIL) */
    public static final int KEYINPUTTYPE_MAIL = 2;
    /** key input type (Multi-field URL) */
    public static final int KEYINPUTTYPE_MULTI_URL = 3;

    /**
     * Status of the composing text.
     * <br>
     * {@code true} if there is no composing text.
     */
    protected boolean mNoInput = true;

    /** Vibrator for key click vibration */
    protected Vibrator mVibrator = null;

    /** SoundManager for key click sound */
    protected  boolean mSound = false;

    /** Key toggle cycle table currently using */
    protected String[] mCurrentCycleTable;

    /** Use Mushroom */
    protected boolean mEnableMushroom = false;

    /** Use Voice input */
    protected static boolean mEnableVoiceInput = false;

    /** Use Switch Ime */
    protected static boolean mEnableSwitchIme = true;

    /** Use Qwerty Left/Right Key */
    protected static boolean mEnableLeftRightKey = true;

    /** Key top for EnterKey */
    private Drawable mEnterKeyIcon = null;

    /** Key preview for EnterKey */
    private Drawable mEnterKeyIconPreview = null;

    /** Key label for EnterKey */
    private CharSequence mEnterKeyLabel = null;

    /** Not flick action */
    public static final int FLICK_NOT_PRESS = -99999;

    /** Press the key for flick action */
    protected int mFlickPressKey = FLICK_NOT_PRESS;

    /** Direction of flick action */
    protected int mFlickDirection;

    /** Whether flick action has started */
    protected boolean mHasFlickStarted;

    /** Whether onkey process finish */
    protected boolean mIsKeyProcessFinish;

    /** Previous input character code */
    protected int mPrevInputKeyCode = 0;

    /** Previous keyboard type */
    protected int mPrevKeyboardType;

    /** Previous icon's id of Enter key */
    private int mPrevResIdEnterKey = 0;

    /** Previous label's id of Enter key */
    private int mPrevLabelResIdEnterKey = 0;

    /** The last input type */
    protected int mLastInputType = -1;

    /** Indicator label  **/
    protected TextView mTextView;

    /** Indicator label Width size */
    public static final int LABEL_WIDTH_SIZE = 40;

    /** Sound DeleteKey */
    private static final int SOUND_DELETE_KEY = 1;

    /** IME Sound change ReturnKey */
    private static final int SOUND_RETURN_KEY = 2;

    /** IME Sound change SpacebarKey */
    private static final int SOUND_SPACEBAR_KEY = 3;

    /** IME Sound change StandardKey */
    private static final int SOUND_STANDARD_KEY = 4;

    /** IWnnLanguageSwitcher instance which hold this software keyboard */
    protected static IWnnLanguageSwitcher mWnnSwitcher = null;

    /** Whether imeOptions is IME_FLAG_FORCE ASCII. */
    protected boolean mIsImeOptionsForceAscii = false;

    /** The last imeOptions init value */
    protected static final int IME_OPTIONS_INIT = -1;

    /** The last imeOptions */
    protected int mLastImeOptions = IME_OPTIONS_INIT;

    /** Toggle cycle table for FULL KUTEN */
    protected static final String[] CYCLE_TABLE_FULL_KUTEN = {"、", "。"};

    /** Toggle cycle table for HALF KUTEN */
    protected static final String[] CYCLE_TABLE_HALF_KUTEN = {"､", "｡"};

    /** Toggle cycle table for FULL PERIOD */
    protected static final String[] CYCLE_TABLE_FULL_PERIOD = { "．", "，"};

    /** Toggle cycle table for HALF PERIOD */
    protected static final String[] CYCLE_TABLE_HALF_PERIOD = { ".", ","};

    /** Whether keep of shift mode */
    protected boolean mKeepShiftMode = false;

    /** Split mode */
    private static boolean mEnableSplit = true;

    /** one handed keyboard */
    private boolean mEnableOneHanded = true;

    /** Function key one handed **/
    private static boolean mEnableFunctionOneHanded = false;

    /** Function key split **/
    private static boolean mEnableFunctionSplit = false;

    /** One handed keyboard position */
    protected String mOneHandedPosition;

    /** One handed keyboard position status*/
    public static final String ONEHANDED_POSITION_TOP_RIGHT = "top_right";
    public static final String ONEHANDED_POSITION_TOP_LEFT = "top_left";
    public static final String ONEHANDED_POSITION_BOTTOM_RIGHT = "bottom_right";
    public static final String ONEHANDED_POSITION_BOTTOM_LEFT = "bottom_left";

    /** One touch emoji list left mode */
    public static final String ONETOUCHEMOJILIST_LEFT = "left";
    /** One touch emoji list right mode */
    public static final String ONETOUCHEMOJILIST_RIGHT = "right";
    /** One touch emoji list hidden mode */
    public static final String ONETOUCHEMOJILIST_HIDDEN = "hidden";
    /** One touch emoji list mode */
    protected String mOneTouchEmojiListMode;

    /** Event listener for symbol keyboard */
    private OnKeyboardActionListener mSymbolOnKeyboardAction = new OnKeyboardActionListener() {
        public void onKey(int primaryCode, int[] keyCodes) {
            switch (primaryCode) {
            case KEYCODE_4KEY_MODE:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_KEY,
                                              new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)));
                break;

            case KEYCODE_4KEY_UP:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_UP));
                break;

            case KEYCODE_4KEY_DOWN:
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_DOWN));
                break;

            case KEYCODE_4KEY_CLEAR:
                mWnn.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                break;

            default:
                break;
            }
        }
        public void onPress(int primaryCode) {
            playSoundAndVibration(primaryCode);
        }
        public void onText(CharSequence text) { }
        public void swipeLeft() { }
        public void swipeRight() { }
        public void swipeUp() { }
        public void swipeDown() { }
        public void onRelease(int primaryCode) {
            mWnn.deleteSurrogateText(primaryCode);
        }
    };

    /**
     * Constructor
     */
    public DefaultSoftKeyboard(IWnnLanguageSwitcher wnn) {
        if (wnn != null) {
            mWnnSwitcher = wnn;
        }
    }

    /**
     * Create keyboard views.
     *
     * @param parent   OpenWnn using the keyboards.
     */
    protected void createKeyboards(OpenWnn parent) {
        /*
         *  WnnKeyboardFactory[# of Languages][portrait/landscape][# of keyboard type]
         *          [shift off/on][max # of key-modes][input key type][non-input/input]
         */
        mKeyboard = new WnnKeyboardFactory[3][2][4][2][12][4][2];
    }

    /**
     * Get the keyboard changed the specified shift state.
     *
     * @param shift     SHIFT key state
     * @return          Keyboard view
     */
    protected Keyboard getShiftChangeKeyboard(int shift) {
        try {
            WnnKeyboardFactory[] kbds
                    = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType]
                              [shift][mCurrentKeyMode][mCurrentKeyInputType];
            WnnKeyboardFactory kbd;

            if (!mNoInput && (kbds[1] != null) && !IWnnImeJaJp.isLatticeInputMode()) {
                kbd = kbds[1];
            } else {
                kbd = kbds[0];
            }
            return kbd.getKeyboard();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the keyboard changed the specified input mode.
     *
     * @param mode      Input mode,
     * from {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYMODE_EN_ALPHABET} to
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYMODE_RU_PHONE}
     * @return          Keyboard view
     */
    protected Keyboard getModeChangeKeyboard(int mode) {
        try {
            WnnKeyboardFactory[] kbds
                    = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType]
                              [mShiftOn][mode][mCurrentKeyInputType];
            WnnKeyboardFactory kbd;

            if (!mNoInput && kbds[1] != null) {
                kbd = kbds[1];
            } else {
                kbd = kbds[0];
            }
            return kbd.getKeyboard();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the keyboard changed the specified keyboard type.
     *
     * @param type      Keyboard type,
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_QWERTY} or
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_12KEY}.
     * @return          Keyboard view
     */
    protected Keyboard getTypeChangeKeyboard(int type) {
        try {
            WnnKeyboardFactory[] kbds
                    = mKeyboard[mCurrentLanguage][mDisplayMode][type]
                              [mShiftOn][mCurrentKeyMode][mCurrentKeyInputType];
            WnnKeyboardFactory kbd;

            if (!mNoInput && kbds[1] != null) {
                kbd = kbds[1];
            } else {
                kbd = kbds[0];
            }
            return kbd.getKeyboard();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the keyboard when some characters are inputed or no character is inputed.
     *
     * @param inputted   {@code true} if some characters are inputed;
     *                   {@code false} if no character is inputed.
     * @return          Keyboard view
     */
    protected Keyboard getKeyboardInputted(boolean inputted) {
        try {
            WnnKeyboardFactory[] kbds
                    = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType]
                              [mShiftOn][mCurrentKeyMode][mCurrentKeyInputType];
            WnnKeyboardFactory kbd;

            if (inputted && kbds[1] != null) {
                kbd = kbds[1];
            } else {
                kbd = kbds[0];
            }
            return kbd.getKeyboard();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Toggle change the shift lock state.
     */
    protected void toggleShiftLock() {
        if (mShiftOn == 0) {
            /* turn shift on */
            Keyboard newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_ON);
            if (newKeyboard != null) {
                mShiftOn = 1;
                changeKeyboard(newKeyboard);
            }
            mCapsLock = true;
        } else {
            /* turn shift off */
            Keyboard newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF);
            if (newKeyboard != null) {
                mShiftOn = 0;
                changeKeyboard(newKeyboard);
            }
            mCapsLock = false;
        }
    }

    /**
     * Handle the ALT key event.
     */
    protected void processAltKey() {
    }

    /**
     * Change the keyboard type.
     *
     * @param type  Type of the keyboard,
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_QWERTY} or
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_12KEY}.
     */
    public void changeKeyboardType(int type) {
        /* ignore invalid parameter */
        if (type != KEYBOARD_QWERTY && type != KEYBOARD_12KEY) {
            return;
        }

        /* change keyboard view */
        Keyboard kbd = getTypeChangeKeyboard(type);
        if (kbd != null) {
            mCurrentKeyboardType = type;
            changeKeyboard(kbd);
        }

        /* notice that the keyboard is changed */
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                      OpenWnnEvent.Mode.DEFAULT));
    }

    /**
     * Change the keyboard.
     *
     * @param keyboard  The new keyboard,
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_QWERTY} or
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_12KEY}.
     * @return          {@code true} if the keyboard is changed; {@code false} if not changed.
     */
    protected boolean changeKeyboard(Keyboard keyboard) {
        if (mIsSymbolKeyboard) {
            return false;
        }

        if (keyboard == null) {
            return false;
        }

        if (mKeyboardView == null) {
            return false;
        }

        // By type keyboard, replace the keyboard view.
        if (mCurrentKeyboardType == KEYBOARD_QWERTY) {
            mKeyboardView = mMultiTouchKeyboardView;
            mFlickKeyboardView.setVisibility(View.GONE);
            mMultiTouchKeyboardView.setVisibility(View.VISIBLE);
        } else if (mCurrentKeyboardType == KEYBOARD_12KEY) {
            mKeyboardView = mFlickKeyboardView;
            mFlickKeyboardView.setVisibility(View.VISIBLE);
            mMultiTouchKeyboardView.setVisibility(View.GONE);
        }

        /* Set EnterKey */
        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            restoreEnterKey(mKeyboardView.getKeyboard());
        } else {
            restoreEnterKey(mCurrentKeyboard);
        }
        saveEnterKey(keyboard);
        setEnterKey(keyboard);
        if (mCurrentKeyboard != keyboard) {
            clearKeyboardPress(keyboard);
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                if (mIsSymbolKeyboard) {
                    ((MultiTouchKeyboardView) mKeyboardView).setKeyboard(mSymbolKeyboard, mSymbolKeyboard);
                } else {
                    ((MultiTouchKeyboardView) mKeyboardView).setKeyboard(
                                                    getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF),
                                                    getShiftChangeKeyboard(KEYBOARD_SHIFT_ON));
                    ((MultiTouchKeyboardView) mKeyboardView).copyEnterKeyState();
                    mKeyboardView.setKeyboard(keyboard, mCurrentKeyboardType);
                }
            } else {
                mKeyboardView.setKeyboard(keyboard, mCurrentKeyboardType);
            }
            if (mIsSymbolKeyboard) {
                mKeyboardView.setKeyboard(mSymbolKeyboard, KEYBOARD_12KEY);
                mKeyboardView.setShifted((mShiftOn == 0) ? false : true);
            }
            mCurrentKeyboard = keyboard;
            if (OpenWnn.isDebugging()) {
                Log.d("OpenWnn", "SHIFT = "+mShiftOn
                    +", KBD CHANGE : "+mCurrentKeyboard.isShifted());
            }
            if (OpenWnn.isDebugging() && (mCurrentKeyboard != null)) {Log.d("OpenWnn", "SHIFT = "+mShiftOn+", KBD CHANGE : "+mCurrentKeyboard.isShifted());}
            return true;
        } else {
            if (mKeyboardView instanceof FlickKeyboardView) {
                mKeyboardView.setShifted((mShiftOn == 0) ? false : true);
            }
            if (OpenWnn.isDebugging()) {
                if (mCurrentKeyboard == null) {
                    Log.d("OpenWnn", "SHIFT = "+mShiftOn);
                } else {
                    Log.d("OpenWnn", "SHIFT = "+mShiftOn
                              +", KBD NO CHANGE : "+mCurrentKeyboard.isShifted());
                }
            }
            return false;
        }
    }
    //----------------------------------------------------------------------
    // InputViewManager
    //----------------------------------------------------------------------
    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#initView(OpenWnn, int, int) */
    public View initView(OpenWnn parent, int width, int height) {
        mWnn = parent;
        mDisplayMode =
            (parent.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) ? LANDSCAPE : PORTRAIT;

        mEnableSwitchIme = getEnableSwitchIme();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        final KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        if (keyskin.getPackageManger() == null) {
            keyskin.init(OpenWnn.superGetContext());
        }
        keyskin.setPreferences(pref);

        /*
         * create keyboards & the view.
         * To re-display the input view when the display mode is changed portrait <-> landscape,
         * create keyboards every time.
         */
        createKeyboards(parent);

        /* create symbol keyboard */
        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        int resId = R.xml.keyboard_4key;
        if ((mWnn.isSubtypeExtraEmojiInput()) && mEnableSwitchIme) {
            resId = R.xml.keyboard_5key;
        } // else {}

        if (langPack.isValid()) {
            resId = KeyboardLanguagePackData.READ_RESOURCE_TAG_4KEY;
        }

        mSymbolKeyboard = new Keyboard(OpenWnn.superGetContext(), resId);

        boolean isKeep = OpenWnn.getCurrentIme().isKeepInput();
        if (!isKeep) {
            mIsSymbolKeyboard = false;
        }

        // multi
        String skin = new String("keyboard_android_default_multi");
        int id = parent.getResources().getIdentifier(skin, "layout", "jp.co.omronsoft.iwnnime.ml");
        boolean shift = (mMultiTouchKeyboardView != null) ? mMultiTouchKeyboardView.isShifted() : false;
        mMultiTouchKeyboardView = (MultiTouchKeyboardView) mWnn.getLayoutInflater().inflate(id, null);
        mMultiTouchKeyboardView.setOnTouchListener(this);
        mMultiTouchKeyboardView.setOnKeyboardActionListener(this);
        mMultiTouchKeyboardView.setShifted(shift);

        // flick
        skin = new String("keyboard_android_default");
        id = parent.getResources().getIdentifier(skin, "layout", "jp.co.omronsoft.iwnnime.ml");
        shift = (mFlickKeyboardView != null) ? mFlickKeyboardView.isShifted() : false;
        mFlickKeyboardView = (FlickKeyboardView) mWnn.getLayoutInflater().inflate(id, null);
        mFlickKeyboardView.setShifted(shift);
        mFlickKeyboardView.initPopupView(parent);
        mFlickKeyboardView.setOnTouchListener(this);
        mFlickKeyboardView.setModeCycleCount(getSlideCycleCount());
        mFlickKeyboardView.setOnKeyboardActionListener(this);

        if (isKeep) {
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                mKeyboardView = mMultiTouchKeyboardView;
            } else {
                mKeyboardView = mFlickKeyboardView;
            }
        } else {
            if (OpenWnn.isTabletMode()) {
                mKeyboardView = mMultiTouchKeyboardView;
            } else {
                mKeyboardView = mFlickKeyboardView;
            }
        }
        setKeyMode();
        restoreEnterKey(mCurrentKeyboard);
        mCurrentKeyboard = null;

        mMainView = (BaseInputView) parent.getLayoutInflater().
                        inflate(R.layout.keyboard_default_main, null);

        boolean isSplitCurrent = isSplitMode();
        boolean isSplitPrev = WnnKeyboardFactory.getSplitMode();
        if (isSplitPrev != isSplitCurrent) {
            WnnKeyboardFactory.setSplitMode(isSplitCurrent);
        }

        if (isSplitCurrent) {
            mMainLayout = (FrameLayout)mMainView.findViewById(R.id.base_inputview_layout_split);
        } else {
            mMainLayout = (LinearLayout)mMainView.findViewById(R.id.base_inputview_layout);
        }
        mSubView = (ViewGroup) parent.getLayoutInflater().
                       inflate(R.layout.keyboard_default_sub, null);

        mTextView = new TextView(OpenWnn.superGetContext());

        if (!mHardKeyboardHidden) {
            if (!mEnableHardware12Keyboard) {
                mMainView.addView(mSubView);
                mMainLayout.setVisibility(View.GONE);
            }
            mSubView.addView(mTextView);
            Resources res = parent.getResources();
            mSubView.setPadding(res.getDimensionPixelSize(R.dimen.keyboard_subview_padding_left),
                                res.getDimensionPixelSize(R.dimen.keyboard_subview_padding_top),
                                res.getDimensionPixelSize(R.dimen.keyboard_subview_padding_right),
                                res.getDimensionPixelSize(R.dimen.keyboard_subview_padding_bottom));
            mTextView.setTextColor(Color.WHITE);
            mTextView.setBackgroundColor(Color.BLACK);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setWidth(LABEL_WIDTH_SIZE);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    parent.getResources().getDimensionPixelSize(R.dimen.indicator_text_size));
        } else {
            mMainLayout.addView(mFlickKeyboardView);
            mMainLayout.addView(mMultiTouchKeyboardView);
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                mFlickKeyboardView.setVisibility(View.GONE);
            } else {
                mMultiTouchKeyboardView.setVisibility(View.GONE);
            }
        }

        // One-Handed Top Button
        mOneHandedTopView = (ViewGroup)parent.getLayoutInflater().inflate(
                R.layout.one_handed_top, null);

        final ImageView mOneHandedTopButton = (ImageView)mOneHandedTopView.findViewById(
                R.id.onehand_top_imageview);
        if (keyskin.isValid()) {
            mOneHandedTopView.setBackground(keyskin
                    .getDrawable((int) R.drawable.ime_one_hand_bg_width2));
            mOneHandedTopButton.setImageDrawable(keyskin
                    .getDrawable((int) R.drawable.ime_one_hand_btn_up_normal));
        }

        mOneHandedTopButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (keyskin.isValid()) {
                        mOneHandedTopButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_up_pressed));
                    } else {
                        mOneHandedTopButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_up_pressed));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (keyskin.isValid()) {
                        mOneHandedTopButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_up_normal));
                    } else {
                        mOneHandedTopButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_up_normal));
                    }
                    playSoundAndVibration(SOUND_STANDARD_KEY);
                    if (mOneHandedPosition
                            .equals(ONEHANDED_POSITION_BOTTOM_LEFT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_TOP_LEFT;
                    } else if (mOneHandedPosition
                            .equals(ONEHANDED_POSITION_BOTTOM_RIGHT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_TOP_RIGHT;
                    }
                    showOneHandedView();
                    break;
                }

                return true;
            }
        });

        // mMainView.addView(mOneHandedTopView, 0,
        // new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        // ViewGroup.LayoutParams.WRAP_CONTENT));

        // One-Handed Bottom Button
        mOneHandedBottomView = (ViewGroup)parent.getLayoutInflater().inflate(
                R.layout.one_handed_bottom, null);

        final ImageView mOneHandedBottomButton = (ImageView)mOneHandedBottomView.findViewById(
                R.id.onehand_bottom_imageview);

        if (keyskin.isValid()) {
            mOneHandedBottomView.setBackground(keyskin
                    .getDrawable((int) R.drawable.ime_one_hand_bg_width));
            mOneHandedBottomButton
                    .setImageDrawable(keyskin
                            .getDrawable((int) R.drawable.ime_one_hand_btn_down_normal));
        }

        mOneHandedBottomButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (keyskin.isValid()) {
                        mOneHandedBottomButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_down_pressed));
                    } else {
                        mOneHandedBottomButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_down_pressed));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (keyskin.isValid()) {
                        mOneHandedBottomButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_down_normal));
                    } else {
                        mOneHandedBottomButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_down_normal));
                    }
                    playSoundAndVibration(SOUND_STANDARD_KEY);
                    if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_LEFT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_BOTTOM_LEFT;
                    } else if (mOneHandedPosition
                            .equals(ONEHANDED_POSITION_TOP_RIGHT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_BOTTOM_RIGHT;
                    }
                    showOneHandedView();
                    break;
                }

                return true;
            }
        });

        // mMainView.addView(mOneHandedBottomView,
        // new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        // ViewGroup.LayoutParams.WRAP_CONTENT));

        // One-Handed Left Button
        mOneHandedLeftView = (ViewGroup)parent.getLayoutInflater().inflate(
                R.layout.one_handed_left, null);

        final ImageView mOneHandedLeftButton = (ImageView)mOneHandedLeftView.findViewById(
                R.id.onehand_left_imageview);

        if (keyskin.isValid()) {
            mOneHandedLeftView.setBackground(keyskin
                    .getDrawable((int) R.drawable.ime_one_hand_bg_height));
            mOneHandedLeftButton
                    .setImageDrawable(keyskin
                            .getDrawable((int) R.drawable.ime_one_hand_btn_left_normal));
        }

        mOneHandedLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (keyskin.isValid()) {
                        mOneHandedLeftButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_left_pressed));
                    } else {
                        mOneHandedLeftButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_left_pressed));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (keyskin.isValid()) {
                        mOneHandedLeftButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_left_normal));
                    } else {
                        mOneHandedLeftButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_left_normal));
                    }
                    playSoundAndVibration(SOUND_STANDARD_KEY);
                    if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_RIGHT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_TOP_LEFT;
                    } else if (mOneHandedPosition
                            .equals(ONEHANDED_POSITION_BOTTOM_RIGHT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_BOTTOM_LEFT;
                    }
                    showOneHandedView();
                    break;
                }

                return true;
            }
        });

        mMainLayout.addView(mOneHandedLeftView, 0,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // One-Handed Right Button
        mOneHandedRightView = (ViewGroup)parent.getLayoutInflater().inflate(
                R.layout.one_handed_right, null);

        final ImageView mOneHandedRightButton = (ImageView)mOneHandedRightView.findViewById(
                R.id.onehand_right_imageview);

        if (keyskin.isValid()) {
            mOneHandedRightView.setBackground(keyskin
                    .getDrawable((int) R.drawable.ime_one_hand_bg_height2));
            mOneHandedRightButton
                    .setImageDrawable(keyskin
                            .getDrawable((int) R.drawable.ime_one_hand_btn_right_normal));
        }

        mOneHandedRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (keyskin.isValid()) {
                        mOneHandedRightButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_right_pressed));
                    } else {
                        mOneHandedRightButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_right_pressed));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (keyskin.isValid()) {
                        mOneHandedRightButton.setImageDrawable(keyskin
                                .getDrawable((int) R.drawable.ime_one_hand_btn_right_normal));
                    } else {
                        mOneHandedRightButton
                                .setImageDrawable(mWnn
                                        .getResources()
                                        .getDrawable(
                                                (int) R.drawable.ime_one_hand_btn_right_normal));
                    }
                    playSoundAndVibration(SOUND_STANDARD_KEY);
                    if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_LEFT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_TOP_RIGHT;
                    } else if (mOneHandedPosition
                            .equals(ONEHANDED_POSITION_BOTTOM_LEFT)) {
                        mOneHandedPosition = ONEHANDED_POSITION_BOTTOM_RIGHT;
                    }
                    showOneHandedView();
                    break;
                }

                return true;
            }
        });

        mMainLayout.addView(mOneHandedRightView,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                               ViewGroup.LayoutParams.MATCH_PARENT));

        Resources res = mWnn.getResources();
        mEnableOneHanded = pref.getBoolean("one_handed", res.getBoolean(R.bool.one_handed_default_value));
        mOneHandedPosition = pref.getString("one_handed_position", res.getString(R.string.one_handed_position_default_value));

        return mMainView;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#getCurrentView() */
    public View getCurrentView() {
        return mMainView;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#onUpdateState(OpenWnn) */
    public void onUpdateState(OpenWnn parent) {
        try {
            if (!IWnnImeJaJp.isLatticeInputMode()) {
            if (parent.mComposingText.size(1) == 0) {
                if (!mNoInput) {
                    /* when the mode changed to "no input" */
                    mNoInput = true;
                    Keyboard newKeyboard = getKeyboardInputted(false);
                    if (mCurrentKeyboard != newKeyboard) {
                        changeKeyboard(newKeyboard);
                    }
                }
            } else {
                if (mNoInput) {
                    /* when the mode changed to "input some characters" */
                    mNoInput = false;
                    Keyboard newKeyboard = getKeyboardInputted(true);
                    if (mCurrentKeyboard != newKeyboard) {
                        changeKeyboard(newKeyboard);
                    }
                }
            }
            }
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                setEnterKey(mKeyboardView.getKeyboard());
                ((MultiTouchKeyboardView) mKeyboardView).copyEnterKeyState();
            } else {
                setEnterKey(mCurrentKeyboard);
            }

        } catch (Exception ex) {
            Log.e("OpenWnn", "DefaultSoftKeyboard::onUpdateState " + ex.toString());
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#setPreferences(SharedPreferences, EditorInfo) */
    public void setPreferences(SharedPreferences pref, EditorInfo editor) {
        Resources res = mWnn.getResources();

        /* vibrator */
        try {
            if (pref.getBoolean("key_vibration", res.getBoolean(R.bool.key_vibration_default_value))) {
                mVibrator = (Vibrator)mWnn.getSystemService(Context.VIBRATOR_SERVICE);
            } else {
                mVibrator = null;
            }
        } catch (Exception ex) {
            Log.d("OpenWnn", "NO VIBRATOR");
        }

        /* sound */
        mSound = pref.getBoolean("key_sound", true);

        mEnableMushroom = pref.getString("opt_mushroom", res.getString(R.string.mushroom_id_default)).equals("notuse") ? false : true;

        mEnableSwitchIme = getEnableSwitchIme();

        mEnableLeftRightKey = pref.getBoolean("opt_display_left_right_key", res.getBoolean(R.bool.opt_display_left_right_key_default_value));

        mEnableSplit = pref.getBoolean("split_mode", res.getBoolean(R.bool.split_mode_default_value));

        mEnableOneHanded = pref.getBoolean("one_handed", res.getBoolean(R.bool.one_handed_default_value));

        mOneHandedPosition = pref.getString("one_handed_position", res.getString(R.string.one_handed_position_default_value));

        mEnableFunctionSplit = pref.getBoolean("split_mode_show",
                res.getBoolean(R.bool.one_handed_show_default_value));

        mEnableFunctionOneHanded = pref.getBoolean("one_handed_show",
                res.getBoolean(R.bool.split_mode_show_default_value));

        if (WnnKeyboardFactory.getSplitMode() && !mEnableSplit) {
            ((IWnnLanguageSwitcher)mWnn).clearInitializedFlag();
        }

        // If you meet all the following three conditions can be used as voice input.
        // This condition is the same as the Android keyboard.
        // 1. Voice input setting is enabled.
        // 2. ShortcutIME(Google voice input) is available.
        mEnableVoiceInput = false;
        boolean enableVoiceInputSetting = pref.getBoolean("voice_input", res.getBoolean(R.bool.voice_input_default_value));
        boolean availableShortcutIme = mWnnSwitcher.isAvailableShortcutIME();
        boolean disableVoiceInputPrivateIme = mWnnSwitcher.getDisableVoiceInputInPrivateImeOptions(editor);
        if (enableVoiceInputSetting && availableShortcutIme && !disableVoiceInputPrivateIme) {
            mEnableVoiceInput = true;
        }

        // Setting of multi touch keyboard view.
        boolean previewEnable = pref.getBoolean("popup_preview", res.getBoolean(R.bool.popup_preview_default_value));
        if (mMultiTouchKeyboardView != null) {
            mMultiTouchKeyboardView.setPreviewEnabled(previewEnable);
            mMultiTouchKeyboardView.cancelTouchEvent();
            mMultiTouchKeyboardView.invalidateAllKeys();
            mMultiTouchKeyboardView.setEnableMushroom(mEnableMushroom);
            mMultiTouchKeyboardView.setVoiceInput(mEnableVoiceInput);
        }
        clearKeyboardPress(mCurrentKeyboard);
        // Setting of flick keyboard view.
        if (mFlickKeyboardView != null) {
            mFlickKeyboardView.setPreviewEnabled(previewEnable);
            int thres = res.getInteger(R.integer.flick_sensitivity_preference_default);
            try {
                thres = Integer.parseInt(pref.getString(ControlPanelStandard.FLICK_SENSITIVITY_KEY, String.valueOf(thres)));
            } catch (NumberFormatException e) {
                //do nothing
            }
            mFlickKeyboardView.setFlickSensitivity(thres);
            mFlickKeyboardView.setEnableMushroom(mEnableMushroom);
            mFlickKeyboardView.setModeCycleCount(getSlideCycleCount());
            mFlickKeyboardView.clearWindowInfo();
            mFlickKeyboardView.setVoiceInput(mEnableVoiceInput);
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#closing */
    public void closing() {
        if (mKeyboardView != null) {
            mKeyboardView.closing();
        }
        mDisableKeyInput = true;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#showInputView */
    public void showInputView() {
        if (mMainView != null) {
            mMainView.setVisibility(View.VISIBLE);
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#hideInputView */
    public void hideInputView() {
        mMainView.setVisibility(View.GONE);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#dismissPopupKeyboard */
    public void dismissPopupKeyboard() {
        if (mKeyboardView != null) {
            mKeyboardView.closing();
        }
        return;
    }

    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#isPopupKeyboard */
    public Boolean isPopupKeyboard() {
        if (mKeyboardView != null) {
            return mKeyboardView.mMiniKeyboardOnScreen;
        } else {
            return false;
        }
    }
    /** @see jp.co.omronsoft.iwnnime.ml.InputViewManager#closePopupKeyboard */
    public void closePopupKeyboard() {
        if (mKeyboardView != null) {
            mKeyboardView.dismissPopupKeyboard();
        }
    }
    /***********************************************************************
     * onKeyboardActionListener
     ***********************************************************************/
    /**
     * Send a key press to the listener.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onKey
     */
    public void onKey(int primaryCode, int[] keyCodes) {
        mIsKeyProcessFinish = false;

        if (mDisableKeyInput) {
            mIsKeyProcessFinish = true;
            return;
        }

        if (primaryCode == KEYCODE_CHANGE_LANGUAGE_JA) {
            ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ja");
            mIsKeyProcessFinish = true;
            return;
        }else if (primaryCode == KEYCODE_CHANGE_LANGUAGE_KO) {
            ((IWnnLanguageSwitcher)mWnn).changeKeyboardLanguage("ko");
            mIsKeyProcessFinish = true;
            return;
        }

        if (mFlickPressKey != FLICK_NOT_PRESS && mKeyboardView instanceof FlickKeyboardView) {
            ((FlickKeyboardView) mKeyboardView).setFlickDetectMode(false, 0);
            boolean started = mHasFlickStarted;
            mHasFlickStarted = false;
            if ((mFlickDirection != FLICK_DIRECTION_NEUTRAL_INDEX) || started) {
                mIsKeyProcessFinish = true;
                inputByFlickDirection(mFlickDirection, true);
                // update shift key's state
                if (!mCapsLock && (primaryCode != DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT)) {
                    setShiftByEditorInfo(false);
                }
                mPrevInputKeyCode = primaryCode;
                return;
            }
        }
    }

    /**
     * Set the shift key state from {@link android.view.inputmethod.EditorInfo}.
     *
     * @param force {@code true} if force to set the keyboard.
     */
    protected void setShiftByEditorInfo(boolean force) { }

    /**
     * Called when the user quickly moves the finger from left to right.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeRight
     */
    public void swipeRight() { }

    /**
     * Called when the user quickly moves the finger from right to left.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeLeft
     */
    public void swipeLeft() { }

    /**
     * Called when the user quickly moves the finger from up to down.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeDown
     */
    public void swipeDown() { }

    /**
     * Called when the user quickly moves the finger from down to up.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#swipeUp
     */
    public void swipeUp() { }

    /**
     * Called when the user releases a key.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onRelease
     */
    public void onRelease(int x) {
        mWnn.deleteSurrogateText(x);
    }

    /**
     * Called when the user presses a key.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onPress
     */
    public void onPress(int x) {
        playSoundAndVibration(x);
    }

    /**
     * Play sound & vibration.
     */
    private void playSoundAndVibration(int x) {
        /* key click sound & vibration */
        boolean valid = true;
        if (x == 0 || mTouchKeyInfo == null) {
            // Sound and Viberator off
            valid = false;
        }
        else if (x == KEYCODE_JP12_BLANK_KEY1 || x == KEYCODE_JP12_BLANK_KEY2) {
            if( !mTouchKeyInfo.showPreview &&
               ((mTouchKeyInfo.label != null) && mTouchKeyInfo.label.toString().equals(" ")) ) {
                // Sound and Viberator off
                valid = false;
            }
        }

        if (valid) {
            if (mVibrator != null) {
               try {
                   // Viberator on
                   if (ENABLE_DOCOMO) {
                       mVibrator.vibrate(mWnn.getResources().getInteger(
                            R.integer.vibrate_time));
                   } else {
                       mVibrator.vibrate(new long[] {0, mWnn.getResources().getInteger(
                            R.integer.vibrate_time_kddi)}, -1);
                   }
               } catch (Exception ex) {
                   Log.e("OpenWnn", "DefaultSoftKeyboard::onPress vibration " + ex.toString());
               }
           }
           if (mSound) {
               // Sound on
               playKeyClick(x);
           }
        }

        if (mKeyboardView instanceof FlickKeyboardView) {
            if (isEnableFlickMode(x)) {
                mFlickDirection = FLICK_DIRECTION_NEUTRAL_INDEX;
                if (isFlickKey(x)) {
                    mFlickPressKey = x;
                    ((FlickKeyboardView) mKeyboardView).setFlickDetectMode(true, x);
                    ((FlickKeyboardView) mKeyboardView).setFlickedKeyGuide(true);
                } else {
                    mFlickPressKey = FLICK_NOT_PRESS;
                    ((FlickKeyboardView) mKeyboardView).setFlickDetectMode(false, 0);
                }
            } else {
                mFlickPressKey = FLICK_NOT_PRESS;
                ((FlickKeyboardView) mKeyboardView).setFlickDetectMode(false, 0);
            }
        }
    }

    /**
     * Play sound for key click.
     */
    private void playKeyClick(int primaryCode) {
        int sound;
        switch (primaryCode) {
        case KEYCODE_JP12_BACKSPACE:
        case KEYCODE_QWERTY_BACKSPACE:
        case KEYCODE_4KEY_CLEAR:
            sound = SOUND_DELETE_KEY;
            break;
        case KEYCODE_JP12_ENTER:
        case KEYCODE_QWERTY_ENTER:
            sound = SOUND_RETURN_KEY;
            break;
        case KEYCODE_JP12_SPACE:
        case ' ':               // harf-width space
        case '\u3000':          // full-width space
            sound = SOUND_SPACEBAR_KEY;
            break;
        default:
            sound = SOUND_STANDARD_KEY;
            break;
        }
        SoundManager.getInstance().play(sound);
    }

    /**
     * Restore Enter key
     *
     * @param keyboard  The keyboard
     */
    private void restoreEnterKey(Keyboard keyboard) {
        if (keyboard != null) {
            int enterIndex = MultiTouchKeyboardView.getEnterKeyIndex(keyboard);
            Keyboard.Key enterKey = keyboard.getKeys().get(enterIndex);

            if ((enterKey != null) && (mEnterKeyIcon != null || mEnterKeyLabel != null)) {
                enterKey.label = mEnterKeyLabel;
                enterKey.icon = mEnterKeyIcon;
                enterKey.iconPreview = mEnterKeyIconPreview;
                if (mKeyboardView instanceof MultiTouchKeyboardView) {
                    ((MultiTouchKeyboardView) mKeyboardView).copyEnterKeyState();
                }
            }

        }
        mPrevResIdEnterKey = 0;
        mPrevLabelResIdEnterKey = 0;
    }

    /**
     * Save Enter key
     *
     * @param keyboard  The keyboard
     */
    private void saveEnterKey(Keyboard keyboard) {
        if (keyboard != null) {
            int enterIndex = MultiTouchKeyboardView.getEnterKeyIndex(keyboard);
            Keyboard.Key enterKey = keyboard.getKeys().get(enterIndex);
            mEnterKeyLabel = enterKey.label;
            mEnterKeyIcon = enterKey.icon;
            mEnterKeyIconPreview = enterKey.iconPreview;
        }
    }

    /**
     * keycode to keyindex.
     *
     * @param keyboard    disp keyboard.
     * @param keycode     keycode.
     *
     * @return keyindex.
     */
    /*
    private int getKeyIndex(Keyboard keyboard, int keycode) {
        int retindex = -1;
        List<Keyboard.Key> keys = keyboard.getKeys();
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            if (keys.get(i).codes[0] == keycode) {
                retindex = i;
                break;
            }
        }
        return retindex;
    }
    */

    /**
     * (x, y) to key info.
     *
     * @param position_x  x position
     * @param position_y  y_position
     *
     * @return Keyboard.Key.
     */
    private Keyboard.Key getKeyInfo(int position_x, int position_y) {
        if (mKeyboardView == null) {
            return null;
        }

        Keyboard keyboard;
        if (mIsSymbolKeyboard) {
            keyboard = mSymbolKeyboard;
        } else {
            keyboard = mCurrentKeyboard;
        }
        Keyboard.Key retKey = null;
        int keyboardX = mKeyboardView.getTouchKeyboardX(position_x);
        int keyboardY = mKeyboardView.getTouchKeyboardY(position_y);
        int [] keyIndices = keyboard.getNearestKeys(keyboardX, keyboardY);
        final int keyCount = keyIndices.length;

        if (keyCount > 0) {
            List<Keyboard.Key> keys = keyboard.getKeys();
            int i;
            for (i = 0; i < keyCount; i++) {
                final Keyboard.Key key = keys.get(keyIndices[i]);
                if (key.isInside(keyboardX, keyboardY)) {
                    retKey = key;
                    break;
                }
            }
        }
        return retKey;
    }

    /**
     * Set Enter key
     *
     * @param newKeyboard  The new keyboard
     */
    private void setEnterKey(Keyboard newKeyboard) {
        if ((newKeyboard == null) || (mKeyboardView == null)) {
            return;
        }
        int enterIndex = MultiTouchKeyboardView.getEnterKeyIndex(newKeyboard);
        List<Keyboard.Key> newKeys = newKeyboard.getKeys();
        Keyboard.Key newEnterKey = newKeys.get(enterIndex);
        newEnterKey.popupCharacters = null;
        newEnterKey.popupResId = 0;
        EditorInfo edit = mWnn.getCurrentInputEditorInfo();
        int iconResId = 0;
        int iconPreviewResId = 0;
        int labelResId = 0;
        String iconResKey = null;
        String iconPreviewResKey = null;

        int keyDrawableId = 0;
        int keyDrawablePreviewId = 0;
        boolean isQwerty = (mCurrentKeyboardType == KEYBOARD_QWERTY);

        boolean isShownOneTouchEmoji = false;
        if (this instanceof DefaultSoftKeyboardJAJP) {
            isShownOneTouchEmoji = ((DefaultSoftKeyboardJAJP) this).isShownOneTouchEmojiList();
        }
        if (this instanceof DefaultSoftKeyboardKorean) {
            isShownOneTouchEmoji = ((DefaultSoftKeyboardKorean) this).isShownOneTouchEmojiList();
        }

        boolean isSmall = false;
        if (isShownOneTouchEmoji
                && isQwerty
                && (mWnn.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
            isSmall = true;
        }

        if ((mNoInput || IWnnImeJaJp.isLatticeInputMode()) && (mWnn.getFunfun() == 0)) {
            switch (edit.imeOptions
                    & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                labelResId = R.string.ti_enterkey_go;
                keyDrawableId = KEY_ID_GO;
                keyDrawablePreviewId = KEY_ID_FLICK_PREVIEW_GO;
                break;

            case EditorInfo.IME_ACTION_NEXT:
                labelResId = R.string.ti_enterkey_next;
                keyDrawableId = KEY_ID_NEXT;
                keyDrawablePreviewId = KEY_ID_FLICK_PREVIEW_NEXT;
                break;

            case EditorInfo.IME_ACTION_DONE:
                labelResId = R.string.ti_enterkey_done;
                keyDrawableId = KEY_ID_DONE;
                keyDrawablePreviewId = KEY_ID_FLICK_PREVIEW_DONE;
                break;

            case EditorInfo.IME_ACTION_SEARCH:
                if (isQwerty) {
                    if (isSmall) {
                        iconResId = R.drawable.ime_keypad_icon_search_small;
                    } else {
                        iconResId = R.drawable.ime_keypad_icon_search;
                    }
                    iconPreviewResId = R.drawable.ime_keypad_icon_search;
                } else {
                    iconResId = R.drawable.ime_keypad_icon_jp_search;
                    iconPreviewResId = R.drawable.ime_keypad_icon_jp_search;
                }
                iconResKey = "key_enter_search";
                iconPreviewResKey = "key_enter_search_b";
                break;

            case EditorInfo.IME_ACTION_SEND:
                labelResId = R.string.ti_enterkey_send;
                keyDrawableId = KEY_ID_SEND;
                keyDrawablePreviewId = KEY_ID_FLICK_PREVIEW_SEND;
                break;

            case EditorInfo.IME_ACTION_PREVIOUS:
                labelResId = R.string.ti_enterkey_previous;
                keyDrawableId = KEY_ID_PREVIOUS;
                keyDrawablePreviewId = KEY_ID_FLICK_PREVIEW_PREVIOUS;
                break;

            default:
                if (isQwerty) {
                    if (isSmall) {
                        iconResId = R.drawable.ime_keypad_icon_enter_small;
                    } else {
                        iconResId = R.drawable.ime_keypad_icon_enter;
                    }
                    iconPreviewResId = R.drawable.ime_keypad_icon_enter;
                } else {
                    iconResId = R.drawable.ime_keypad_icon_jp_enter;
                    iconPreviewResId = R.drawable.ime_keypad_icon_jp_enter;
                }
                break;
            }
        } else {
            labelResId = R.string.ti_enterkey_ok;
            keyDrawableId = KEY_ID_OK;
            keyDrawablePreviewId = KEY_ID_FLICK_PREVIEW_OK;
        }

        if (mPrevResIdEnterKey == iconResId && mPrevLabelResIdEnterKey == labelResId &&
                (mKeyboardView instanceof FlickKeyboardView)) {
            return;
        }

        mPrevResIdEnterKey = iconResId;
        mPrevLabelResIdEnterKey = labelResId;

        if (iconResId == 0 && iconPreviewResId == 0 && labelResId == 0) {
            newEnterKey.label = mEnterKeyLabel;
            newEnterKey.icon = mEnterKeyIcon;
            newEnterKey.iconPreview = mEnterKeyIconPreview;
        } else if (labelResId == 0) {
            newEnterKey.label = null;
            if (keyDrawableId == 0) {
                KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
                newEnterKey.icon = resMan.getDrawable(
                        OpenWnn.superGetContext(), iconResId, iconResKey);
                newEnterKey.iconPreview = resMan.getDrawable(
                        OpenWnn.superGetContext(), iconPreviewResId, iconPreviewResKey);
            } else {
                newEnterKey.icon = new KeyDrawable(mWnn.getResources(),
                        keyDrawableId, newEnterKey.width, newEnterKey.height);
                newEnterKey.iconPreview = new KeyDrawable(mWnn.getResources(),
                        keyDrawablePreviewId, newEnterKey.width,
                        newEnterKey.height);
            }
        } else {
            newEnterKey.label = mWnn.getResources().getString(labelResId);
            newEnterKey.icon = null;
            newEnterKey.iconPreview = null;
            if (keyDrawableId != 0) {
                newEnterKey.icon = new KeyDrawable(mWnn.getResources(),
                        keyDrawableId, newEnterKey.width, newEnterKey.height);
                newEnterKey.iconPreview = new KeyDrawable(mWnn.getResources(),
                        keyDrawablePreviewId, newEnterKey.width,
                        newEnterKey.height);
            }
        }

        if (newEnterKey.iconPreview != null) {
            newEnterKey.iconPreview.setBounds(0, 0,
                    newEnterKey.iconPreview.getIntrinsicWidth(),
                    newEnterKey.iconPreview.getIntrinsicHeight());
        }

        Keyboard oldKeyboard = mCurrentKeyboard;
        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            oldKeyboard = mKeyboardView.getKeyboard();
        }
        if ((oldKeyboard == newKeyboard) && (0 < mKeyboardView.getWidth())/*Done layout?*/) {
            mKeyboardView.invalidateKey(enterIndex);
        }
    }

    /**
     * Sends a sequence of characters to the listener.
     *
     * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onText
     */
    public void onText(CharSequence text) {}

    /**
     * Get current key mode.
     *
     * @return Current key mode,
     * from {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYMODE_EN_ALPHABET} to
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYMODE_RU_PHONE}.
     */
    public int getKeyMode() {
        return mCurrentKeyMode;
    }

    /**
     * Get current keyboard type.
     *
     * @return Current keyboard type,
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_QWERTY} or
     * {@link jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard#KEYBOARD_12KEY}.
     */
    public int getKeyboardType() {
        return mCurrentKeyboardType;
    }

    /**
     * Set the H/W keyboard's state.
     *
     * @param hidden {@code true} if hidden.
     */
    public void setHardKeyboardHidden(boolean hidden) {
        mHardKeyboardHidden = hidden;
    }

    /**
     * Set the H/W keyboard's type.
     *
     * @param type12Key {@code true} if 12Key.
     */
    public void setHardware12Keyboard(boolean type12Key) {
        mEnableHardware12Keyboard = type12Key;
    }

    /**
     * Get current keyboard view.
     *
     * @return A view that renders a virtual Keyboard.
     */
    public KeyboardView getKeyboardView() {
        return mKeyboardView;
    }

    /**
     * Reset the current keyboard.
     */
    public void resetCurrentKeyboard() {
        closing();
        mDisableKeyInput = false;
        Keyboard keyboard = mCurrentKeyboard;
        restoreEnterKey(mCurrentKeyboard);
        mCurrentKeyboard = null;
        changeKeyboard(keyboard);
    }

    /** @see android.inputmethodservice.KeyboardView.OnFlickKeyboardActionListener#onFlick */
    public void onFlick(int x, int direction) {
        if (mFlickDirection != direction) {
            mHasFlickStarted = true;
            mFlickDirection = direction;
            if (isFlickKey(mFlickPressKey)) {
                inputByFlickDirection(direction, false);
            }
        }
    }

    /**
     * Called when input the character corresponding to flick direction.
     *
     * @param direction     Direction of flick action.
     * @param isCommit      Whether text is commit.
     */
    protected void inputByFlickDirection(int direction, boolean isCommit) {
    }

    /**
     * Called when the character is input by flick action.
     *
     * @param directionIndex    Index of cycle table for flick.
     * @param isCommit          Whether text is commit.
     */
    protected void inputByFlick(int directionIndex, boolean isCommit) {
    }

    /** @see android.inputmethodservice.KeyboardView.OnFlickKeyboardActionListener#onLongPress */
    public boolean onLongPress(Keyboard.Key key) {
        if (mEnableMushroom && getLongpressMushroomKey(key)) {
            // Show dummy dialog for stabilize IWnnLanguageSwitcher#onUpdateSelection
            BaseInputView baseInputView = (BaseInputView)getCurrentView();
            AlertDialog.Builder builder = new AlertDialog.Builder(baseInputView.getContext());
            baseInputView.showDialog(builder);
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CALL_MUSHROOM));
            return true;
        }

        switch (key.codes[0]) {
        case KEYCODE_4KEY_UP:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_FULL_UP));
            return true;

        case KEYCODE_4KEY_DOWN:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CANDIDATE_VIEW_SCROLL_FULL_DOWN));
            return true;

        case '0':
            if (mCurrentKeyMode == KEYMODE_JA_HALF_PHONE) {
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, '+'));
                return true;
            }
            break;
        default:
            break;
        }

        return false;
    }

    /**
     * Sets the keyboard to be shifted.
     *
     * @param shiftState  {@link #KEYBOARD_SHIFT_ON}, {@link #KEYBOARD_SHIFT_OFF}
     */
    protected void setShifted(int shiftState) {
        if (mKeyboardView == null || (mKeyboardView instanceof MultiTouchKeyboardView)) {
            return;
        }
        Keyboard kbd = getShiftChangeKeyboard(shiftState);
        mShiftOn = shiftState;
        if (mShiftOn == KEYBOARD_SHIFT_OFF) {
            mCapsLock = false;
        }

        changeKeyboard(kbd);
    }

    /**
     * Get the enable to start mushroom when longpress.
     *
     * @param key longpress key data
     * @return {@code true} if the longpress mushroom key; {@code false} otherwise.
     */
    protected boolean getLongpressMushroomKey(Keyboard.Key key) {
        boolean result = false;
        if (key != null) {
            switch (key.codes[0]) {
            case KEYCODE_JP12_EMOJI:
            case KEYCODE_QWERTY_EMOJI:
                result = true;
                break;

            default:
                break;
            }
        }
        return result;
    }

    /**
     * Get the ModeKey Cycle Count.
     *
     * @return ModeKey Cycle Count.
     */
    protected int getModeCycleCount() {
        return 1;
    }

    /**
     * Get the Slide Cycle Count.
     *
     * @return Slide Cycle Count.
     */
    protected int getSlideCycleCount() {
        return 1;
    }

    /**
     *  Get the enable to flick key.
     *
     * @param key   key code
     * @return Cycle Mode.
     */
    protected boolean isFlickKey(int key) {
        return false;
    }

    /**
     * Check whether flick mode is active or not.
     *
     * @param key   key code
     * @return The flick mode.
     */
    protected boolean isEnableFlickMode(int key){
        return false;
    }

    /**
     * Set the normal keyboard.
     */
    public void setNormalKeyboard() {
        if (mCurrentKeyboard == null) {
            return;
        }
        if (mKeyboardView != null) {
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                ((MultiTouchKeyboardView) mKeyboardView).setKeyboard(
                                                getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF),
                                                getShiftChangeKeyboard(KEYBOARD_SHIFT_ON));
            } else {
                mKeyboardView.setKeyboard(mCurrentKeyboard, mCurrentKeyboardType);
            }
            mKeyboardView.setOnKeyboardActionListener(this);
        }
        mIsSymbolKeyboard = false;
        setKeyMode();
    }

    /**
     * Check whether Symbol Keyboard is visible or invisible.
     *
     * @return Symbol Keyboard visibilty.
     */
    public boolean isSymbolKeyboard() {
        return mIsSymbolKeyboard;
    }

    /**
     * Set the symbol keyboard.
     */
    public void setSymbolKeyboard() {

        BaseInputView baseInputView = (BaseInputView)getCurrentView();
        if (baseInputView != null) {
            baseInputView.closeDialog();
        }

        if (mKeyboardView != null) {
            mKeyboardView.setVisibility(View.VISIBLE);
            if (mKeyboardView instanceof MultiTouchKeyboardView) {
                ((MultiTouchKeyboardView) mKeyboardView).setKeyboard(mSymbolKeyboard, mSymbolKeyboard);
            } else {
                mKeyboardView.setKeyboard(mSymbolKeyboard, KEYBOARD_12KEY);
            }
            mKeyboardView.setOnKeyboardActionListener(mSymbolOnKeyboardAction);
        }
        mIsSymbolKeyboard = true;
        setKeyMode();
    }

    /**
     * Clear the keyboard press.
     *
     * @param keyboard  The keyboard,
     */
    protected void clearKeyboardPress(Keyboard keyboard) {
        if (keyboard != null) {
            Iterator<Keyboard.Key> i = keyboard.getKeys().iterator();
            while (i.hasNext()) {
                Keyboard.Key k = i.next();
                k.pressed = false;
            }
        }
    }

    /** @see android.view.View.OnTouchListener#onTouch */
    public boolean onTouch(View v, MotionEvent event) {
        mTouchKeyInfo= getKeyInfo((int)event.getX(), (int)event.getY());
        return mDisableKeyInput;
    }

    /**
     * Set OK to Enter key
     */
    public void setOkToEnterKey() {
        setEnterKey(mCurrentKeyboard);
        if ((mCurrentKeyboard == null) || (mKeyboardView == null)) {
            return;
        }
        int enterIndex = MultiTouchKeyboardView.getEnterKeyIndex(mCurrentKeyboard);
        List<Keyboard.Key> newKeys = mCurrentKeyboard.getKeys();
        Keyboard.Key newEnterKey = newKeys.get(enterIndex);
        newEnterKey.popupCharacters = null;
        newEnterKey.popupResId = 0;

        int labelResId = R.string.ti_enterkey_ok;

        mPrevResIdEnterKey = 0;
        mPrevLabelResIdEnterKey = labelResId;

        newEnterKey.label = mWnn.getResources().getString(labelResId);
        newEnterKey.icon = null;
        newEnterKey.iconPreview = null;

        Drawable tmpIcon = new KeyDrawable(mWnn.getResources(),
                KEY_ID_OK, newEnterKey.width, newEnterKey.height);
        if (tmpIcon != null) {
            newEnterKey.icon = tmpIcon;
        } // else {}

        Drawable tmpPreview = newEnterKey.iconPreview = new KeyDrawable(
                mWnn.getResources(), KEY_ID_FLICK_PREVIEW_OK,
                newEnterKey.width, newEnterKey.height);
        if (tmpPreview != null) {
            newEnterKey.iconPreview = tmpPreview;
            newEnterKey.iconPreview.setBounds(0, 0,
                    newEnterKey.iconPreview.getIntrinsicWidth(),
                    newEnterKey.iconPreview.getIntrinsicHeight());
        } // else {}

        mKeyboardView.invalidateKey(enterIndex);
    }

    /**
     * Initialize flick status.
     */
    protected void initializeFlick() {
        mHasFlickStarted = false;
        mFlickDirection = FLICK_DIRECTION_NEUTRAL_INDEX;
        mFlickPressKey = FLICK_NOT_PRESS;
    }

    /**
     * Disable Voice Input.
     */
    protected void disableVoiceInput() {
        mEnableVoiceInput = false;
        if (mFlickKeyboardView != null) {
            mFlickKeyboardView.setModeCycleCount(getSlideCycleCount());
            mFlickKeyboardView.setVoiceInput(mEnableVoiceInput);
        }
        if (mMultiTouchKeyboardView != null) {
            mMultiTouchKeyboardView.setVoiceInput(mEnableVoiceInput);
        }
    }

    /**
     * If voice input keyboard is opened when "Voice Input" setting value is unchecked,
     * set a flag of force closing the keyboard.
     */
    protected void forceCloseVoiceInputKeyboard() {
        if (!mEnableVoiceInput && isVoiceInputMode()) {
            mLastInputType = -1;
        }
    }

    /**
     * Check voice mode.
     *
     * @return boolean  The voice mode,
     */
    protected boolean isVoiceInputMode() {
        return false;
    }

    /** Set current key mode */
    public void setKeyMode() {
        if (mKeyboardView == null) {
            return;
        }

        int top;
        int bottom;
        Resources r = mKeyboardView.getContext().getResources();

        if (mIsSymbolKeyboard) {
            top = r.getDimensionPixelSize(R.dimen.padding_symbol_top);
            bottom = r.getDimensionPixelSize(R.dimen.padding_symbol_bottom);
        } else if (isOneHandedMode()){
            top = mKeyboardView.getPaddingTop();
            bottom =mKeyboardView.getPaddingBottom();
        } else {
            if (mCurrentKeyboardType == KEYBOARD_12KEY || mCurrentKeyboardType == KEYBOARD_NAVIGATION) {
                top = r.getDimensionPixelSize(R.dimen.padding_keyboard_top);
                bottom = r.getDimensionPixelSize(R.dimen.padding_keyboard_bottom);
            } else {
                top = r.getDimensionPixelSize(R.dimen.padding_qwerty_keyboard_top);
                bottom = r.getDimensionPixelSize(R.dimen.padding_qwerty_keyboard_bottom);
            }
        }

        mKeyboardView.setPadding(
            mKeyboardView.getPaddingLeft(), top, mKeyboardView.getPaddingRight(), bottom);
    }

    /**
      * Get state switch ime.
     */
    private boolean getEnableSwitchIme() {
        OpenWnn wnn = OpenWnn.getCurrentIme();
        Resources res = wnn.getResources();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        boolean setting = pref.getBoolean("opt_display_language_switch_key", res.getBoolean(R.bool.opt_display_language_switch_key_default_value));
        if (!setting || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }

        boolean ret = true;
        InputMethodManager manager = (InputMethodManager) wnn.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> imiList = manager.getEnabledInputMethodList();
        boolean enable = pref.getBoolean("opt_change_otherime", res.getBoolean(R.bool.opt_change_otherime_default_value));
        if (imiList.size() == 1 || !enable) {
            final String packageName = wnn.getApplicationContext().getPackageName();
            InputMethodInfo selfImi = null;
            for (final InputMethodInfo imi : manager.getInputMethodList()) {
                if (imi.getPackageName().equals(packageName)) {
                    selfImi = imi;
                    break;
                }
            }

            if (selfImi == null) {
                ret = false;
            } else if (manager.getEnabledInputMethodSubtypeList(selfImi, true).size() <= 1) {
                ret = false;
            }
        }
        return ret;
    }

    /**
      * Set 5 key.
     */
    public void set5key() {
        if ((mWnn.isSubtypeExtraEmojiInput()) && mEnableSwitchIme) {
            mSymbolKeyboard = new Keyboard(OpenWnn.superGetContext(), R.xml.keyboard_5key);
        } else {
            mSymbolKeyboard = new Keyboard(OpenWnn.superGetContext(), R.xml.keyboard_4key);
        }
    }

    /**
     * Get condition according to setting.
     */
    public static boolean getKeyboardCondition(int target) {
        int check = 0;
        boolean result = false;

        if (checkKeyboardCondition(target, CONDITION_VOICE_ON, CONDITION_VOICE_OFF)) {
            if (mEnableVoiceInput) {
                check |= CONDITION_VOICE_ON;
            } else {
                check |= CONDITION_VOICE_OFF;
            }
        } // else {}

        if (checkKeyboardCondition(target, CONDITION_SWITCH_IME_ON, CONDITION_SWITCH_IME_OFF)) {
            if (mEnableSwitchIme) {
                check |= CONDITION_SWITCH_IME_ON;
            } else {
                check |= CONDITION_SWITCH_IME_OFF;
            }
        } // else {}

        if (checkKeyboardCondition(target, CONDITION_CURSOR_ON, CONDITION_CURSOR_OFF)) {
            if (mEnableLeftRightKey) {
                check |= CONDITION_CURSOR_ON;
            } else {
                check |= CONDITION_CURSOR_OFF;
            }
        } // else {}

        if (checkModeCondition(target)) {
            check |= getModeCondition();
       } // else {}

        if (target == check) {
             result = true;
        } // else {}

        return result;
    }

    /**
     * Check condition according to xml.
     */
    public static boolean checkKeyboardCondition(int target, int on, int off) {
        int check = 0;
        boolean result = false;

        check |= on & target;
        check |= off & target;
        if (check != 0) {
            result = true;
        } // else {}

        return result;
    }

    /**
     * Check condition of current mode according to xml.
     */
    public static boolean checkModeCondition(int target) {
        int check = 0;
        boolean result = false;

        check |= CONDITION_MODE & target;
        if (check != 0) {
            result = true;
        } // else {}

        return result;
    }

    /**
     * Get condition of current mode according to xml.
     */
    public static int getModeCondition() {
        int result = 0;

        Locale locale = mWnnSwitcher.getSelectedLocale();
        String lang = IWnnLanguageSwitcher.LOCALE_CONVERT_ENGINELOCALE_TABLE.get(locale.toString());
        int language = LanguageManager.getChosenLanguageType(lang);

        switch (language) {
            case iWnnEngine.LanguageType.JAPANESE:
            case iWnnEngine.LanguageType.ENGLISH:
                switch (mCurrentKeyMode) {
                    case KEYMODE_JA_FULL_HIRAGANA:
                        result = CONDITION_MODE_HIRAGANA;
                        break;
                    case KEYMODE_JA_FULL_ALPHABET:
                        result = CONDITION_MODE_FULL_ALPHA;
                        break;
                    case KEYMODE_JA_FULL_NUMBER:
                        result = CONDITION_MODE_FULL_NUM;
                        break;
                    case KEYMODE_JA_FULL_KATAKANA:
                        result = CONDITION_MODE_FULL_KATAKANA;
                        break;
                    case KEYMODE_JA_HALF_ALPHABET:
                        result = CONDITION_MODE_HALF_ALPHA;
                        break;
                    case KEYMODE_JA_HALF_NUMBER:
                        result = CONDITION_MODE_HALF_NUM;
                        break;
                    case KEYMODE_JA_HALF_KATAKANA:
                        result = CONDITION_MODE_HALF_KATAKANA;
                        break;
                    default:
                        // nothing to do.
                        break;
                }
                break;
            case iWnnEngine.LanguageType.KOREAN:
                switch (mCurrentKeyMode) {
                    case KEYMODE_KO_HANGUL:
                        result = CONDITION_MODE_HANGUL;
                        break;
                    case KEYMODE_KO_ALPHABET:
                        result = CONDITION_MODE_HALF_ALPHA;
                        break;
                    case KEYMODE_KO_NUMBER:
                        result = CONDITION_MODE_HALF_NUM;
                        break;
                    default:
                        // nothing to do.
                        break;
                }
                break;
            case iWnnEngine.LanguageType.CANADA_FRENCH:
            case iWnnEngine.LanguageType.CZECH:
            case iWnnEngine.LanguageType.DUTCH:
            case iWnnEngine.LanguageType.ENGLISH_UK:
            case iWnnEngine.LanguageType.ENGLISH_US:
            case iWnnEngine.LanguageType.FRENCH:
            case iWnnEngine.LanguageType.GERMAN:
            case iWnnEngine.LanguageType.ITALIAN:
            case iWnnEngine.LanguageType.NORWEGIAN_BOKMAL:
            case iWnnEngine.LanguageType.POLISH:
            case iWnnEngine.LanguageType.PORTUGUESE:
            case iWnnEngine.LanguageType.SPANISH:
            case iWnnEngine.LanguageType.SWEDISH:
                switch (mCurrentKeyMode) {
                    case KEYMODE_EN_ALPHABET:
                        result = CONDITION_MODE_HALF_ALPHA;
                        break;
                    case KEYMODE_EN_NUMBER:
                        result = CONDITION_MODE_HALF_NUM;
                        break;
                    default:
                        // nothing to do.
                        break;
                }
                break;
            case iWnnEngine.LanguageType.SIMPLIFIED_CHINESE:
            case iWnnEngine.LanguageType.TRADITIONAL_CHINESE:
                switch (mCurrentKeyMode) {
                    case KEYMODE_CN_PINYIN:
                        result = CONDITION_MODE_PINYIN;
                        break;
                    case KEYMODE_CN_BOPOMOFO:
                        result = CONDITION_MODE_BOPOMOFO;
                        break;
                    case KEYMODE_CN_ALPHABET:
                        result = CONDITION_MODE_HALF_ALPHA;
                        break;
                    case KEYMODE_CN_HALF_NUMBER:
                        result = CONDITION_MODE_HALF_NUM;
                        break;
                    default:
                        // nothing to do.
                        break;
                }
                break;
            default:
                // nothing to do.
                break;
        }

        return result;
    }

    /**
     * Set keep of shift mode.
     *
     * @param set  {@code true} keep of shift mode {@code false} not keep of shift mode.
     */
    public void setKeepShiftMode(boolean set) {
        mKeepShiftMode = set;
    }

    /**
     * Set prev input key code.
     *
     * @param set  input key code
     */
    public void setPrevInputKeyCode(int set) {
        mPrevInputKeyCode = set;
    }

    /**
     * Set shift state
     *
     * @param isShift  set shift state
     */
    protected void setShiftedByMultiTouchKeyboard(boolean isShift) {
        if (mKeyboardView instanceof MultiTouchKeyboardView) {
            if (!mIsSymbolKeyboard) {
                restoreEnterKey(mKeyboardView.getKeyboard());
            }
            mKeyboardView.setShifted(isShift);
            mShiftOn = isShift ? KEYBOARD_SHIFT_ON : KEYBOARD_SHIFT_OFF;
            if (!mIsSymbolKeyboard) {
                Keyboard newKeyboard = mKeyboardView.getKeyboard();
                saveEnterKey(newKeyboard);
                setEnterKey(newKeyboard);
                ((MultiTouchKeyboardView) mKeyboardView).copyEnterKeyState();
            }
        }
    }

    /**
     * Get the shift key state from the editor.
     * <br>
     * @param editor    The editor information
     * @return          The state id of the shift key (0:off, 1:on)
     */
    protected int getShiftKeyState(EditorInfo editor) {
        InputConnection connection = mWnn.getCurrentInputConnection();
        if (connection != null) {
            int caps = connection.getCursorCapsMode(editor.inputType);
            return (caps == 0) ? 0 : 1;
        } else {
            return 0;
        }
    }

    /**
     * Check navigation mode.
     *
     * @return boolean  The navigation mode,
     */
    protected boolean isNaviKeyboard() {
        return mCurrentKeyboardType == KEYBOARD_NAVIGATION;
    }

    /**
     * Check input mode.
     *
     * @return boolean  The input mode,
     */
    protected boolean isNormalInputMode() {
        return (mCurrentKeyMode != KEYMODE_JA_VOICE)
                && (mCurrentKeyMode != KEYMODE_JA_NUMERIC)
                && (mCurrentKeyMode != KEYMODE_JA_NUMERIC_DECIMAL)
                && (mCurrentKeyMode != KEYMODE_JA_NUMERIC_SIGNED)
                && (mCurrentKeyMode != KEYMODE_JA_HALF_PHONE);
    }

    /**
     * Check split mode.
     *
     * @return boolean  The split mode,
     */
    protected boolean isSplitMode() {
        return (mDisplayMode == DefaultSoftKeyboard.LANDSCAPE)
        && mEnableSplit && isNormalInputMode()
        && !mIsSymbolKeyboard && mEnableFunctionSplit;
    }

    /**
     * Check one handed mode.
     *
     * @return boolean  The one handed mode,
     */
    protected boolean isOneHandedMode() {
        boolean retVal = false;
        boolean isEmojiOn = ONETOUCHEMOJILIST_LEFT.equals(mOneTouchEmojiListMode) ||
                ONETOUCHEMOJILIST_RIGHT.equals(mOneTouchEmojiListMode);

        boolean isTextPhonetic = false;
        EditorInfo editor = mWnn.getCurrentInputEditorInfo();
        int inputType = editor.inputType;
        switch (inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_TEXT:
            switch (inputType & EditorInfo.TYPE_MASK_VARIATION) {
            case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                isTextPhonetic = true;
                break;
            }
            break;
        }

        if(isTextPhonetic) {
            retVal = (mDisplayMode == DefaultSoftKeyboard.PORTRAIT)
                    && mEnableOneHanded
                    && mEnableFunctionOneHanded;
        } else {
            retVal = isEmojiOn ? false : (mDisplayMode == DefaultSoftKeyboard.PORTRAIT)
                    && mEnableOneHanded
                    && mEnableFunctionOneHanded;
        }
        return retVal;
    }

    /**
     * Set one handed position.
     */
    protected void setOneHandedPosition() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("one_handed_position", mOneHandedPosition);
        editor.commit();
    }

    /**
     * Show one handed view.
     */
    protected void showOneHandedView() {
        if (mIsSymbolKeyboard) {
            return;
        }
        if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_RIGHT)) {
            mOneHandedTopView.setVisibility(View.GONE);
            mOneHandedBottomView.setVisibility(View.VISIBLE);
            mOneHandedRightView.setVisibility(View.GONE);
            mOneHandedLeftView.setVisibility(View.VISIBLE);
        } else if (mOneHandedPosition.equals(ONEHANDED_POSITION_TOP_LEFT)) {
            mOneHandedTopView.setVisibility(View.GONE);
            mOneHandedBottomView.setVisibility(View.VISIBLE);
            mOneHandedRightView.setVisibility(View.VISIBLE);
            mOneHandedLeftView.setVisibility(View.GONE);
        } else if (mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_LEFT)) {
            mOneHandedTopView.setVisibility(View.VISIBLE);
            mOneHandedBottomView.setVisibility(View.GONE);
            mOneHandedRightView.setVisibility(View.VISIBLE);
            mOneHandedLeftView.setVisibility(View.GONE);
        } else if (mOneHandedPosition.equals(ONEHANDED_POSITION_BOTTOM_RIGHT)) {
            mOneHandedTopView.setVisibility(View.VISIBLE);
            mOneHandedBottomView.setVisibility(View.GONE);
            mOneHandedRightView.setVisibility(View.GONE);
            mOneHandedLeftView.setVisibility(View.VISIBLE);
        }

        mKeyboardView.clearWindowInfo();
        setOneHandedPosition();
    }

    /**
     * Change Function OneHanded of preference.
     */
    protected void setEnableFunctionOneHanded(boolean show) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("one_handed_show", show);
        editor.commit();
        mEnableFunctionOneHanded = show;
    }

    /**
     * Change Function Split mode of preference.
     */
    protected void setEnableFunctionSplit(boolean show) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("split_mode_show", show);
        editor.commit();
        mEnableFunctionSplit = show;
    }

    /**
     * Hide one handed view.
     */
    protected void hideOneHandedView() {
        mOneHandedTopView.setVisibility(View.GONE);
        mOneHandedBottomView.setVisibility(View.GONE);
        mOneHandedRightView.setVisibility(View.GONE);
        mOneHandedLeftView.setVisibility(View.GONE);

        mKeyboardView.clearWindowInfo();
    }

    /**
     * Show Clip Tray.
     */
    protected void showClipTray() {
        if (LgeImeCliptrayManager.isSupportCliptray(OpenWnn.getContext()) == false) {
            return;
        }
        LgeImeCliptrayManager cliptrayMngr = LgeImeCliptrayManager
                                                .getInstances(OpenWnn.getContext());
        if (cliptrayMngr == null) {
            return;
        }
        cliptrayMngr.showClipTray(mWnn.getCurrentInputEditorInfo());
        cliptrayMngr.finishCliptrayService();
        LgeImeCliptrayManager.release();
    }

    /**
     * Called when the instance is destroyed.
     */
    public void onDestroy() {
    }

}
