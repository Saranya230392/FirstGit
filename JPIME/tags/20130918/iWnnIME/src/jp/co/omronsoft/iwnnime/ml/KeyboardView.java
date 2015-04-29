/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (C) 2008-2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/* This file is porting from Android framework.
 *   frameworks/base/core/java/android/inputmethodservice/KeyboardView.java
 *
 *package android.inputmethodservice;
 */

package jp.co.omronsoft.iwnnime.ml;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.WindowManager.BadTokenException;
import android.widget.PopupWindow;
import android.widget.TextView;

import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul;
import jp.co.omronsoft.iwnnime.ml.hangul.ko.DefaultSoftKeyboardKorean;
import jp.co.omronsoft.iwnnime.ml.Keyboard;
import jp.co.omronsoft.iwnnime.ml.Keyboard.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A view that renders a virtual {@link Keyboard}. It handles rendering of keys and
 * detecting key presses and touch movements.
 */
public class KeyboardView extends View implements View.OnClickListener {

    /**
     * Listener for virtual keyboard events.
     */
    public interface OnKeyboardActionListener {

        /**
         * Called when the user presses a key. This is sent before the {@link #onKey} is called.
         * For keys that repeat, this is only called once.
         * @param primaryCode the unicode of the key being pressed. If the touch is not on a valid
         * key, the value will be zero.
         */
        void onPress(int primaryCode);

        /**
         * Called when the user releases a key. This is sent after the {@link #onKey} is called.
         * For keys that repeat, this is only called once.
         * @param primaryCode the code of the key that was released
         */
        void onRelease(int primaryCode);

        /**
         * Send a key press to the listener.
         * @param primaryCode this is the key that was pressed
         * @param keyCodes the codes for all the possible alternative keys
         * with the primary code being the first. If the primary key code is
         * a single character such as an alphabet or number or symbol, the alternatives
         * will include other characters that may be on the same key or adjacent keys.
         * These codes are useful to correct for accidental presses of a key adjacent to
         * the intended key.
         */
        void onKey(int primaryCode, int[] keyCodes);

        /**
         * Sends a sequence of characters to the listener.
         * @param text the sequence of characters to be displayed.
         */
        void onText(CharSequence text);

        /**
         * Called when the user quickly moves the finger from right to left.
         */
        void swipeLeft();

        /**
         * Called when the user quickly moves the finger from left to right.
         */
        void swipeRight();

        /**
         * Called when the user quickly moves the finger from up to down.
         */
        void swipeDown();

        /**
         * Called when the user quickly moves the finger from down to up.
         */
        void swipeUp();
    }

    protected static final boolean DEBUG = false;
    private static final int NOT_A_KEY = -1;
    private static final int[] KEY_DELETE = { DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE };
    private static final int[] LONG_PRESSABLE_STATE_SET = {
//// OSK MOD start
//        com.android.internal.R.attr.state_long_pressable
        android.R.attr.state_long_pressable
//// OSK MOD end
    };

    protected Keyboard mKeyboard;
    private int mCurrentKeyIndex = NOT_A_KEY;
    private int mLabelTextSize;
    private int mKeyTextSize;
    private int mKeyTextColor;
    private int mKeyPressedTextColor;
    private int mKeyTextColor2nd;
    private float mShadowRadius;
    private float mShadowRadius2nd;
    private int mShadowColor;
    private int mShadowColor2nd;
    private float mBackgroundDimAmount;
    private float mKeyHintLetterPadding;
    private float mKeyHintLetterRatio;

    private TextView mPreviewText;
    private PopupWindow mPreviewPopup;
    private int mPreviewTextSizeLarge;
    protected int mPreviewOffset;
    protected int mPreviewHeight;
    private int[] mOffsetInWindow;

    protected PopupWindow mPopupKeyboard;
    private View mMiniKeyboardContainer;
    private KeyboardView mMiniKeyboard;
    protected boolean mMiniKeyboardOnScreen;
    private boolean mChangeLangDialogOnScreen = false;
    private boolean mFunctionPopupOnScreen = false;
    private View mPopupParent;
    private int mMiniKeyboardOffsetX;
    private int mMiniKeyboardOffsetY;
    private long mMiniKeyboardPopupTime;
    private Map<Key,View> mMiniKeyboardCache;
    private int[] mWindowOffset;
    private Key[] mKeys;

    /** Listener for {@link OnKeyboardActionListener}. */
    private OnKeyboardActionListener mKeyboardActionListener;

    private static final int MSG_SHOW_PREVIEW = 1;
    private static final int MSG_REMOVE_PREVIEW = 2;
    private static final int MSG_REPEAT = 3;
    private static final int MSG_LONGPRESS = 4;
    private static final int MSG_REMOVE_FUNCTIONPOPUP = 5;
    private static final int MSG_REMOVE_LANGUAGEPOPUP = 6;
    private static final int MSG_REMOVE_PERIODPOPUP = 7;

    private static final int DELAY_BEFORE_PREVIEW = 0;
    private static final int DELAY_AFTER_PREVIEW = 70;
    private static final int DEBOUNCE_TIME = 70;

    private static final float KEY_TEXT_SHADOW_RADIUS = 5;

    // This map caches key label text width in pixel as value and key label text size as map key.
    private static final HashMap<Integer, Float> sTextWidthCache =
            new HashMap<Integer, Float>();
    private static final char[] KEY_NUMERIC_HINT_LABEL_REFERENCE_CHAR = { '8' };

    protected int mVerticalCorrection;
    private int mProximityThreshold;

    private boolean mShowPreview = true;
    private int mPopupPreviewX;
    private int mPopupPreviewY;

    private int mLastX;
    private int mLastY;
    private int mStartX;
    private int mStartY;

    private boolean mProximityCorrectOn;

    private Paint mPaint;
    private Rect mPadding;

    private long mDownTime;
    private long mLastMoveTime;
    private int mLastKey;
    private int mLastCodeX;
    private int mLastCodeY;
    private int mCurrentKey = NOT_A_KEY;
    private int mDownKey = NOT_A_KEY;
    private long mLastKeyTime;
    private long mCurrentKeyTime;
    private int[] mKeyIndices = new int[12];
    private GestureDetector mGestureDetector;
    private int mPopupX;
    private int mPopupY;
    private int mRepeatKeyIndex = NOT_A_KEY;
    private int mPopupLayout;
    private boolean mAbortKey;
    private Key mInvalidatedKey;
    private Rect mClipRegion = new Rect(0, 0, 0, 0);
    private boolean mPossiblePoly;
    private SwipeTracker mSwipeTracker = new SwipeTracker();
    private int mSwipeThreshold;
    private boolean mDisambiguateSwipe;

    // Variables for dealing with multiple pointers
    private int mOldPointerCount = 1;
    private float mOldPointerX;
    private float mOldPointerY;

    private Drawable mKeyBackground;
    private Drawable mKeyBackground2nd;

    private static final int REPEAT_INTERVAL = 50; // ~20 keys per second
    private static final int REPEAT_START_DELAY = 400;
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();

    /** Function index */
    public static final int FunctionKey_FUNC_VOICEINPUT     = 0;
    public static final int FunctionKey_FUNC_KBD            = 1;
    public static final int FunctionKey_FUNC_HANDWRITING    = 2;
    public static final int FunctionKey_FUNC_IMESETTINGS    = 3;
    public static final int FunctionKey_FUNC_NORMALKEYBOARD = 4;
    public static final int FunctionKey_FUNC_ONEHANDED      = 5;
    public static final int FunctionKey_FUNC_SPLIT          = 6;
    public static final int FunctionKey_FUNC_CLIPTRAY       = 7;
    /** Korean */
    public static final int KOREAN = 18;  //WLF_LANG_TYPE_KOREAN

    /** Key code for starting voiceinput */
    public static final int KEYCODE_FUNCTION_VOICEINPUT = -601;
    /** Key code for starting keyboardtype */
    public static final int KEYCODE_FUNCTION_KEYBOARDTYPE = -602;
    /** Key code for starting handwriting */
    public static final int KEYCODE_FUNCTION_HANDWRITING = -603;
    /** Key code for starting function setting */
    public static final int KEYCODE_FUNCTION_SETTINGS = -604;
    /** Key code for starting function onehanded */
    public static final int KEYCODE_FUNCTION_ONEHANDED = -605;
    /** Key code for starting function split */
    public static final int KEYCODE_FUNCTION_SPLIT = -606;
    /** Key code for starting function normalkeyboard */
    public static final int KEYCODE_FUNCTION_NORMALKEYBOARD = -607;
    /** Key code for starting function qliptray */
    public static final int KEYCODE_FUNCTION_CLIPTRAY = -608;
    /** Key code for change language popup title */
    public static final int KEYCODE_POPUP_TITLE_INPUT_LANGUAGE = -611;
    /** Key code for change language popup label Japanese */
    public static final int KEYCODE_POPUP_LABEL_JAPANESE = -612;
    /** Key code for change language popup label Korean */
    public static final int KEYCODE_POPUP_LABEL_KOREAN = -613;
    /** Key code for period */
    public static final int KEYCODE_PERIOD = 46;
    /** Key code for phone keyboard space key */
    public static final int KEYCODE_PHONEKEYBOARD_SPACE= 32;

    public static final int FUNCTION_KEY_TIMEOUT = 2000;
    public static final int LANGUAGE_DIALOG_TIMEOUT = 2000;
    public static final int PERIOD_KEY_TIMEOUT = 4000;

    public static final int[] FUNCTION_KEYTOP_DRAWABLE_TABLE =
    {
        R.drawable.ime_keypad_icon_mic,
        R.drawable.ime_keypad_handwriting_icon_kbd,
        R.drawable.ime_keypad_icon_handwriting,
        R.drawable.ime_keypad_icon_setting
    };
    public static final int[] FUNCTION_KEYTOP_DRAWABLE_TABLE_JP =
    {
        R.drawable.ime_keypad_icon_jp_mic,
        R.drawable.ime_keypad_handwriting_icon_kbd,
        R.drawable.ime_keypad_icon_jp_handwriting,
        R.drawable.ime_keypad_icon_setting
    };
    public static final int[] FUNCTION_KEYTOP_DRAWABLE_TABLE_SMALL =
    {
        R.drawable.ime_keypad_icon_mic_small,
        R.drawable.ime_keypad_handwriting_icon_kbd,
        R.drawable.ime_keypad_icon_handwriting_small,
        R.drawable.ime_keypad_icon_setting_small
    };
    private final int[] FUNCTION_PREVIEW_DRAWABLE_TABLE_BASIC =
    {
        R.drawable.ime_keypad_icon_jp_mic,
        R.drawable.ime_keypad_input_icon_kbd_pressed,
        R.drawable.ime_keypad_icon_jp_handwriting,
        R.drawable.ime_keypad_input_icon_setting_normal,
        R.drawable.ime_keypad_input_icon_10kbd_pressed
    };

    private final int[] FUNCTION_PREVIEW_DRAWABLE_TABLE =
    {
        R.drawable.ime_keypad_icon_jp_mic,
        R.drawable.ime_keypad_input_icon_kbd_normal,
        R.drawable.ime_keypad_icon_jp_handwriting,
        R.drawable.ime_keypad_input_icon_setting_normal,
        R.drawable.ime_keypad_input_icon_10kbd_normal
    };

    private final String[] FUNCTION_PREVIEW_DRAWABLE_TABLE_POPUP =
    {
        DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_MIC_POPUP,
        DefaultSoftKeyboard.IME_KEYPAD_ICON_KBD_NORMAL_POPUP,
        DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_HANDWRITING_POPUP,
        DefaultSoftKeyboard.IME_KEYPAD_ICON_SETTING_POPUP,
        DefaultSoftKeyboard.IME_KEYPAD_INPUT_ICON_10KBD_NORMAL
    };

    private final int[][] FUNCTION_KEY_ICON_TABLE =
    {
        {R.drawable.ime_keypad_input_icon_mic_normal,           R.drawable.ime_keypad_input_icon_mic_pressed        },
        {R.drawable.ime_keypad_input_icon_kbd_normal,           R.drawable.ime_keypad_input_icon_kbd_pressed        },
        {R.drawable.ime_keypad_input_icon_handwriting_normal,   R.drawable.ime_keypad_input_icon_handwriting_pressed},
        {R.drawable.ime_keypad_input_icon_setting_normal,       R.drawable.ime_keypad_input_icon_setting_pressed    },
        {R.drawable.ime_keypad_input_icon_keypad_normal,        R.drawable.ime_keypad_input_icon_keypad_pressed     },
        {R.drawable.ime_keypad_input_icon_onehand_normal,       R.drawable.ime_keypad_input_icon_onehand_pressed    },
        {R.drawable.ime_keypad_input_icon_split_normal,         R.drawable.ime_keypad_input_icon_split_pressed      },
        {R.drawable.ime_keypad_input_icon_clip_normal,          R.drawable.ime_keypad_input_icon_clip_pressed       },
        {R.drawable.ime_keypad_input_icon_10kbd_normal,         R.drawable.ime_keypad_input_icon_10kbd_pressed      },
    };
    private final int FUNCTION_KEY_ICON_10KEY_INDEX = 8;

    private final int[][] FUNCTION_KEY_CONVERT_TABLE =
    {
        {KEYCODE_FUNCTION_VOICEINPUT,       FunctionKey_FUNC_VOICEINPUT    },
        {KEYCODE_FUNCTION_KEYBOARDTYPE,     FunctionKey_FUNC_KBD           },
        {KEYCODE_FUNCTION_HANDWRITING,      FunctionKey_FUNC_HANDWRITING   },
        {KEYCODE_FUNCTION_SETTINGS,         FunctionKey_FUNC_IMESETTINGS   },
        {KEYCODE_FUNCTION_NORMALKEYBOARD,   FunctionKey_FUNC_NORMALKEYBOARD},
        {KEYCODE_FUNCTION_ONEHANDED,        FunctionKey_FUNC_ONEHANDED     },
        {KEYCODE_FUNCTION_SPLIT,            FunctionKey_FUNC_SPLIT         },
        {KEYCODE_FUNCTION_CLIPTRAY,         FunctionKey_FUNC_CLIPTRAY      },
    };
    private final int KEYCODE_FUNCTION_MAX = 8;

    /* keypreview background drawable */
    private final int[] KEYPREVIEW_BACK_DRAWABLE =
    {
        R.drawable.ime_btn_qwerty_effect,
        R.drawable.ime_btn_qwerty_effect_left,
        R.drawable.ime_btn_qwerty_effect_right,
        R.drawable.ime_btn_effect,
        R.drawable.ime_btn_effect_flick,
        R.drawable.ime_btn_effect_left,
        R.drawable.ime_btn_effect_popup,
        R.drawable.ime_btn_effect_right,
    };

    /* keypreview background index */
    public static final int KEYPREVIEW_NONE                 = -1;
    public static final int KEYPREVIEW_QWERTY_EFFECT         = 0;
    public static final int KEYPREVIEW_QWERTY_EFFECT_LEFT    = 1;
    public static final int KEYPREVIEW_QWERTY_EFFECT_RIGHT   = 2;
    public static final int KEYPREVIEW_EFFECT                = 3;
    public static final int KEYPREVIEW_EFFECT_FLICK          = 4;
    public static final int KEYPREVIEW_EFFECT_LEFT           = 5;
    public static final int KEYPREVIEW_EFFECT_POPUP          = 6;
    public static final int KEYPREVIEW_EFFECT_RIGHT          = 7;

    private static int MAX_NEARBY_KEYS = 12;
    private int[] mDistances = new int[MAX_NEARBY_KEYS];

    // For multi-tap
    private int mLastSentIndex;
    private int mTapCount;
    private long mLastTapTime;
    private boolean mInMultiTap;
    private static final int MULTITAP_INTERVAL = 800; // milliseconds
    private StringBuilder mPreviewLabel = new StringBuilder(1);

    /** Whether the keyboard bitmap needs to be redrawn before it's blitted. **/
    private boolean mDrawPending;
    /** The dirty region in the keyboard bitmap */
    private Rect mDirtyRect = new Rect();
    /** The keyboard bitmap for faster updates */
    private Bitmap mBuffer;
    /** Notes if the keyboard just changed, so that we could possibly reallocate the mBuffer. */
    private boolean mKeyboardChanged;
    /** The canvas for the above mutable keyboard bitmap */
    private Canvas mCanvas;
    /** Whether the enable voice input */
    protected static boolean mEnableVoiceInput = false;
    /** Current keyboard type */
    protected int mKeyboardType;
    /** Keyboard (QWERTY keyboard) */
    protected static final int KEYBOARD_QWERTY   = 0;

    /** Key code for showing .com url popup */
    public static final int KEYCODE_COM_URL  = -415;
    /** Key code for showing .com e-mail popup */
    public static final int KEYCODE_COM_EMAIL  = -416;
    /** Key code for showing function popup */
    public static final int KEYCODE_FUNCTION_POPUP  = -426;
    /** Key code for showing change language popup */
    public static final int KEYCODE_CHANGE_LANGUAGE_POPUP  = -230;
    public static final int KEYCODE_CHANGE_LANGUAGE_POPUP_KO  = -122;

    private static final int KEY_ID_QWERTY_NUM_SYMBOL_Q  = 117;
    private static final int KEY_ID_QWERTY_NUM_SYMBOL_P  = 126;
    private static final int KEY_ID_QWERTY_NUM_SYMBOL_SLASH = 136;
    private static final int KEY_ID_QWERTY_NUM_SYMBOL_M  = 143;

    public static final int KEY_ID_PHONE_NUM           = 90;
    public static final int KEY_ID_PHONE_SYM           = 102;

    private final static int[] KEY_STATE_NORMAL = {
    };

    /** keyboard background Drawable*/
    private Drawable mKeyboardBackground = null;

    /** Defalut key size rate */
    private static final float DEFALT_KEY_SIZE_RATE = 1.0f;

    /** Version of keytop image permission scale */
    private static final double VERSION_KEYTOPIMAGE_PREMIT_SCALE = 2.30;

    /** Whether the enable mushroom */
    protected boolean mEnableMushroom = false;

    /** Dummy value of pop-up resource id. */
    protected static final int DUMMY_POPUP_RESID = 1;

    /** popup keyboard id */
    private int mPopupKeyboardId;

    /** Flick area for DEBUG */
    protected ArrayList<Path> mDebugFlickArea = new ArrayList<Path>();

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_PREVIEW:
                    showKey(msg.arg1);
                    break;
                case MSG_REMOVE_PREVIEW:
                    mPreviewText.setVisibility(INVISIBLE);
                    break;
                case MSG_REPEAT:
                    if (repeatKey()) {
                        Message repeat = Message.obtain(this, MSG_REPEAT);
                        sendMessageDelayed(repeat, REPEAT_INTERVAL);
                    }
                    break;
                case MSG_LONGPRESS:
                    openPopupIfRequired((MotionEvent) msg.obj);
                    break;
                case MSG_REMOVE_FUNCTIONPOPUP:
                    dismissPopupKeyboard();
                    mMiniKeyboardCache.clear();
                    break;
                case MSG_REMOVE_LANGUAGEPOPUP:
                    dismissPopupKeyboard();
                    //mMiniKeyboardCache.clear();
                    break;
                case MSG_REMOVE_PERIODPOPUP:
                    dismissPopupKeyboard();
                    mMiniKeyboardCache.clear();
                    break;
                default:
                    break;
            }
        }
    };

    /** Constructor */
    public KeyboardView(Context context, AttributeSet attrs) {
//// OSK MOD start
//        this(context, attrs, com.android.internal.R.attr.keyboardViewStyle);
        this(context, attrs, 0);
//// OSK MOD end
    }

    /** Constructor */
    public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a =
            context.obtainStyledAttributes(
//// OSK MOD start
//                attrs, android.R.styleable.KeyboardView, defStyle, 0);
                attrs, R.styleable.WnnKeyboardView, defStyle, R.style.WnnKeyboardView);
//// OSK MOD end

        LayoutInflater inflate =
                (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        int previewLayout = 0;
        int keyTextSize = 0;

        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
            case R.styleable.WnnKeyboardView_keyBackground:
                mKeyBackground = a.getDrawable(attr);
                break;
            case R.styleable.WnnKeyboardView_verticalCorrection:
                mVerticalCorrection = a.getDimensionPixelSize(attr, 0);
                break;
            case R.styleable.WnnKeyboardView_keyPreviewLayout:
                previewLayout = a.getResourceId(attr, 0);
                break;
            case R.styleable.WnnKeyboardView_keyPreviewOffset:
                mPreviewOffset = a.getDimensionPixelSize(attr, 0);
                break;
            case R.styleable.WnnKeyboardView_keyPreviewHeight:
                mPreviewHeight = a.getDimensionPixelSize(attr, 80);
                break;
            case R.styleable.WnnKeyboardView_keyTextSize:
                mKeyTextSize = a.getDimensionPixelSize(attr, 18);
                if (langPack.isValid()) {
                    int tempKeyTextSize = langPack.getDimen(R.dimen.key_text_size_default);
                    if (tempKeyTextSize != 0) {
                        mKeyTextSize = tempKeyTextSize;
                    }
                }
                break;
            case R.styleable.WnnKeyboardView_keyTextColor:
                mKeyTextColor = a.getColor(attr, 0xFF000000);
                break;
            case R.styleable.WnnKeyboardView_keyPressedTextColor:
                mKeyPressedTextColor = a.getColor(attr, 0xFFFFFFFF);
                break;
            case R.styleable.WnnKeyboardView_labelTextSize:
                mLabelTextSize = a.getDimensionPixelSize(attr, 14);
                if (langPack.isValid()) {
                    int tempLabelTextSize = langPack.getDimen(R.dimen.key_label_text_size);
                    if (tempLabelTextSize != 0) {
                        mLabelTextSize = tempLabelTextSize;
                    }
                }
                break;
            case R.styleable.WnnKeyboardView_popupLayout:
                mPopupLayout = a.getResourceId(attr, 0);
                break;
            case R.styleable.WnnKeyboardView_shadowColor:
                mShadowColor = a.getColor(attr, 0);
                break;
            case R.styleable.WnnKeyboardView_shadowRadius:
                mShadowRadius = a.getFloat(attr, 0f);
                break;
            case R.styleable.WnnKeyboardView_keyHintLetterPadding:
                mKeyHintLetterPadding = a.getDimensionPixelSize(attr, 0);
                break;
            case R.styleable.WnnKeyboardView_keyHintLetterRatio:
                mKeyHintLetterRatio = getRatio(a, attr);
                break;
            default:
                break;
            }
        }

        a.recycle();
        a = context.obtainStyledAttributes(attrs, R.styleable.WnnKeyboardView, 0, 0);
        mKeyBackground2nd = a.getDrawable(R.styleable.WnnKeyboardView_keyBackground2nd);
        mKeyTextColor2nd = a.getColor(R.styleable.WnnKeyboardView_keyTextColor2nd, 0xFF000000);
        mShadowColor2nd = a.getColor(R.styleable.WnnKeyboardView_shadowColor2nd, mShadowColor);
        mShadowRadius2nd = a.getFloat(R.styleable.WnnKeyboardView_shadowRadius2nd, mShadowRadius);

        mBackgroundDimAmount = a.getFloat(R.styleable.WnnKeyboardView_backgroundDimAmount, 0.5f);

        mPreviewPopup = new PopupWindow(context);
        if (previewLayout != 0) {
            mPreviewText = (TextView) inflate.inflate(previewLayout, null);
            mPreviewTextSizeLarge = (int) mPreviewText.getTextSize();
            mPreviewPopup.setContentView(mPreviewText);
            mPreviewPopup.setBackgroundDrawable(null);
        } else {
            mShowPreview = false;
        }

        mPreviewPopup.setTouchable(false);

        mPopupKeyboard = new PopupWindow(context);
        mPopupKeyboard.setBackgroundDrawable(null);
        //mPopupKeyboard.setClippingEnabled(false);

        mPopupParent = this;
        //mPredicting = true;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(keyTextSize);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAlpha(255);

        mPadding = new Rect(0, 0, 0, 0);
        mMiniKeyboardCache = new HashMap<Key,View>();
        mKeyBackground.getPadding(mPadding);

        mSwipeThreshold = (int) (500 * getResources().getDisplayMetrics().density);

        //// OSK MOD start
        // ref: frameworks/base/core/res/res/values/config.xml

        // mDisambiguateSwipe = getResources().getBoolean(
        //         com.android.internal.R.bool.config_swipeDisambiguation);
        mDisambiguateSwipe = true;
        //// OSK MOD end

        resetMultiTap();
        initGestureDetector();
        a.recycle();

        if (getId() == R.id.keyboardView) {
            KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
            if (keyskin.isValid()) {
                Drawable KeybackgroundStd = keyskin.getKeybackgroundStd();
                if (KeybackgroundStd != null) {
                    mKeyBackground = KeybackgroundStd;
                }

                int color = keyskin.getKeyPressedTextColor();
                if (color != 0) {
                    mKeyPressedTextColor = color;
                }
            }
        }

        if (getId() != R.id.keyboardView) {
            KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
            Drawable keyboardbg = resMan.getKeyboardBg();
            if (keyboardbg != null) {
                setBackgroundDrawable(keyboardbg);
                if (!keyboardbg.getPadding(new Rect())) {
                    setPadding(0, 0, 0, 0);
                }
            }

            Drawable keybg = resMan.getKeyBg();
            if (keybg != null) {
                mKeyBackground = keybg;
                mKeyBackground2nd = keybg;
            }

            Drawable keybg2nd = resMan.getKeyBg2nd();
            if (keybg2nd != null) {
                mKeyBackground2nd = keybg2nd;
            }

            int color = resMan.getKeyTextColor();
            if (color != 0) {
                mKeyTextColor = color;
            }

            color = resMan.getKeyTextColor2nd();
            if (color != 0) {
                mKeyTextColor2nd = color;
            }

            color = resMan.getShadowColor();
            if (color != 0) {
                mShadowColor = color;
            }

            color = resMan.getShadowColor2nd();
            if (color != 0) {
                mShadowColor2nd = color;
            }

            float radius = resMan.getShadowRadius();
            if (radius >= 0f) {
                mShadowRadius = radius;
            }

            radius = resMan.getShadowRadius2nd();
            if (radius >= 0f) {
                mShadowRadius2nd = radius;
            }
        }
    }

    private boolean isFunctionResId(int id) {
        boolean mFunctionResId = false;

        switch (id) {
        case R.xml.keyboard_functionkey_preview:
        case R.xml.keyboard_functionkey_preview_onehand:
        case R.xml.keyboard_functionkey_preview_split:
        case R.xml.keyboard_functionkey_preview_qwerty:
        case R.xml.keyboard_functionkey_preview_qwerty_onehand:
        case R.xml.keyboard_functionkey_preview_qwerty_split:
        case R.xml.keyboard_functionkey_preview_unable_keyboard:
        case R.xml.keyboard_functionkey_preview_unable_voice:
        case R.xml.keyboard_functionkey_preview_unable_voice_input:
        case R.xml.keyboard_functionkey_preview_unable_voice_onehand:
        case R.xml.keyboard_functionkey_preview_unable_voice_split:
        case R.xml.keyboard_functionkey_preview_unable_voice_input_qwerty:
        case R.xml.keyboard_functionkey_preview_unable_voice_qwerty_onehand:
        case R.xml.keyboard_functionkey_preview_unable_voice_qwerty_split:
        case R.xml.keyboard_functionkey_preview_ko:
        case R.xml.keyboard_functionkey_preview_ko_onehand:
        case R.xml.keyboard_functionkey_preview_ko_split:
        case R.xml.keyboard_functionkey_preview_ko_unable_voice:
        case R.xml.keyboard_functionkey_preview_ko_unable_voice_input:
        case R.xml.keyboard_functionkey_preview_ko_unable_voice_onehand:
        case R.xml.keyboard_functionkey_preview_ko_unable_voice_split:
        case R.xml.keyboard_functionkey_preview_ko_unable_keyboard:
        case R.xml.keyboard_functionkey_preview_ko_unable_voice_keyboard:
        case R.xml.keyboard_functionkey_preview_unable_12key:
        case R.xml.keyboard_functionkey_preview_unable_qwerty:
        case R.xml.keyboard_functionkey_preview_unable_voice_12key:
        case R.xml.keyboard_functionkey_preview_unable_voice_qwerty:
        case R.xml.keyboard_functionkey_preview_emoji_on:
        case R.xml.keyboard_functionkey_preview_ko_emoji_on:
        case R.xml.keyboard_functionkey_preview_ko_unable_voice_input_emoji_on:
        case R.xml.keyboard_functionkey_preview_qwerty_emoji_on:
        case R.xml.keyboard_functionkey_preview_unable_voice_keyboard:
        case R.xml.keyboard_functionkey_preview_unable_voice_input_emoji_on:
        case R.xml.keyboard_functionkey_preview_unable_voice_input_qwerty_emoji_on:
            mFunctionResId = true;
            break;

        default:
            break;
        }
        return mFunctionResId;
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent me1, MotionEvent me2,
                    float velocityX, float velocityY) {
                if (mPossiblePoly) return false;
                final float absX = Math.abs(velocityX);
                final float absY = Math.abs(velocityY);
                float deltaX = me2.getX() - me1.getX();
                float deltaY = me2.getY() - me1.getY();
                int travelX = getWidth() / 2; // Half the keyboard width
                int travelY = getHeight() / 2; // Half the keyboard height
                mSwipeTracker.computeCurrentVelocity(1000);
                final float endingVelocityX = mSwipeTracker.getXVelocity();
                final float endingVelocityY = mSwipeTracker.getYVelocity();
                boolean sendDownKey = false;
                if (velocityX > mSwipeThreshold && absY < absX && deltaX > travelX) {
                    if (mDisambiguateSwipe && endingVelocityX < velocityX / 4) {
                        sendDownKey = true;
                    } else {
                        swipeRight();
                        return true;
                    }
                } else if (velocityX < -mSwipeThreshold && absY < absX && deltaX < -travelX) {
                    if (mDisambiguateSwipe && endingVelocityX > velocityX / 4) {
                        sendDownKey = true;
                    } else {
                        swipeLeft();
                        return true;
                    }
                } else if (velocityY < -mSwipeThreshold && absX < absY && deltaY < -travelY) {
                    if (mDisambiguateSwipe && endingVelocityY > velocityY / 4) {
                        sendDownKey = true;
                    } else {
                        swipeUp();
                        return true;
                    }
                } else if (velocityY > mSwipeThreshold && absX < absY / 2 && deltaY > travelY) {
                    if (mDisambiguateSwipe && endingVelocityY < velocityY / 4) {
                        sendDownKey = true;
                    } else {
                        swipeDown();
                        return true;
                    }
                }

                if (sendDownKey) {
                    detectAndSendKey(mDownKey, mStartX, mStartY, me1.getEventTime());
                }
                return false;
            }
        });

        mGestureDetector.setIsLongpressEnabled(false);
    }

    /**
     * Set the {@link OnKeyboardActionListener} object.
     * @param listener  The OnKeyboardActionListener to set.
     */
    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        mKeyboardActionListener = listener;
    }

    /**
     * Returns the {@link OnKeyboardActionListener} object.
     * @return the listener attached to this keyboard
     */
    protected OnKeyboardActionListener getOnKeyboardActionListener() {
        return mKeyboardActionListener;
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the
     * view will re-layout itself to accommodate the keyboard.
     * @see Keyboard
     * @see #getKeyboard()
     * @param keyboard the keyboard to display in this view
     */
    public void setKeyboard(Keyboard keyboard) {
        int oldRepeatKeyCode = NOT_A_KEY;
        if (mKeyboard != null) {
            showPreview(NOT_A_KEY);
            // Save old repeat keycode
            if ((mRepeatKeyIndex != NOT_A_KEY) && (mRepeatKeyIndex < mKeys.length)) {
                oldRepeatKeyCode = mKeys[mRepeatKeyIndex].codes[0];
            }
        }
        // Remove pending messages
        mHandler.removeMessages(MSG_LONGPRESS);
        mHandler.removeMessages(MSG_SHOW_PREVIEW);

        Keyboard lastKeyboard = mKeyboard;
        mKeyboard = keyboard;
        List<Key> keys = mKeyboard.getKeys();
        mKeys = keys.toArray(new Key[keys.size()]);
        requestLayout();
        // Hint to reallocate the buffer if the size changed
        mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(keyboard);
        mMiniKeyboardCache.clear(); // Not really necessary to do every time, but will free up views
        // Switching to a different keyboard should abort any pending keys so that the key up
        // doesn't get delivered to the old or new keyboard
        boolean abort = true;
        if (oldRepeatKeyCode != NOT_A_KEY) {
            int keyIndex = getKeyIndices(mStartX, mStartY, null);
            if ((keyIndex != NOT_A_KEY)
                && (keyIndex < mKeys.length)
                && (oldRepeatKeyCode == mKeys[keyIndex].codes[0])) {
                abort = false;
                mRepeatKeyIndex = keyIndex;
            }
        }
        if (abort) {
            mHandler.removeMessages(MSG_REPEAT);
        }
        mAbortKey = abort; // Until the next ACTION_DOWN

        if ((keyboard.getXmlLayoutResId() == R.xml.popup_mail)
                || (keyboard.getXmlLayoutResId() == R.xml.popup_url)
                || (keyboard.getXmlLayoutResId() == R.xml.popup_changelanguage)
                || (keyboard.getXmlLayoutResId() == R.xml.popup_changelanguage_unable_ko)) {
            mPaint.setTextAlign(Align.LEFT);
        }

        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        int keyboard_4key = R.xml.keyboard_4key;
        if (langPack.isValid()) {
            keyboard_4key = KeyboardLanguagePackData.READ_RESOURCE_TAG_4KEY;
        }

        if (keyboard.getXmlLayoutResId() == keyboard_4key) {
            if (!(lastKeyboard != null && lastKeyboard.getXmlLayoutResId() == keyboard_4key)) {
                mKeyboardBackground = null;
                clearWindowInfo();

                KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
                Drawable background = resMan.getKeyboardBg1Line();
                if (background != null) {
                    mKeyboardBackground = getBackground();
                    setBackgroundDrawable(background);
                }
            }
        } else {
            if (lastKeyboard != null && lastKeyboard.getXmlLayoutResId() == keyboard_4key) {
                clearWindowInfo();
            }
            if (mKeyboardBackground != null) {
                setBackgroundDrawable(mKeyboardBackground);
                mKeyboardBackground = null;
            }
        }
    }

    /**
     * Returns the current keyboard being displayed by this view.
     * @return the currently attached keyboard
     * @see #setKeyboard(Keyboard)
     */
    public Keyboard getKeyboard() {
        return mKeyboard;
    }

    /**
     * Sets the state of the shift key of the keyboard, if any.
     * @param shifted whether or not to enable the state of the shift key
     * @return true if the shift key state changed, false if there was no change
     * @see KeyboardView#isShifted()
     */
    public boolean setShifted(boolean shifted) {
        if (mKeyboard != null) {
            if (mKeyboard.setShifted(shifted)) {
                // The whole keyboard probably needs to be redrawn
                invalidateAllKeys();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the state of the shift key of the keyboard, if any.
     * @return true if the shift is in a pressed state, false otherwise. If there is
     * no shift key on the keyboard or there is no keyboard attached, it returns false.
     * @see KeyboardView#setShifted(boolean)
     */
    public boolean isShifted() {
        if (mKeyboard != null) {
            return mKeyboard.isShifted();
        }
        return false;
    }

    /**
     * Enables or disables the key feedback popup. This is a popup that shows a magnified
     * version of the depressed key. By default the preview is enabled.
     * @param previewEnabled whether or not to enable the key feedback popup
     * @see #isPreviewEnabled()
     */
    public void setPreviewEnabled(boolean previewEnabled) {
        mShowPreview = previewEnabled;
    }

    /**
     * Returns the enabled state of the key feedback popup.
     * @return whether or not the key feedback popup is enabled
     * @see #setPreviewEnabled(boolean)
     */
    public boolean isPreviewEnabled() {
        return mShowPreview;
    }

    /**
     * Returns the root parent has the enabled state of the key feedback popup.
     * @return whether or not the key feedback popup is enabled
     * @see #setPreviewEnabled(boolean)
     */
    public boolean isParentPreviewEnabled() {
        if ((mPopupParent != null) && (mPopupParent != this)
                && (mPopupParent instanceof KeyboardView)) {
            return ((KeyboardView)mPopupParent).isParentPreviewEnabled();
        } else {
            return mShowPreview;
        }
    }

    public void setVerticalCorrection(int verticalOffset) {

    }

    /**
     * Set View on the PopupParent.
     * @param v  The View to set.
     */
    public void setPopupParent(View v) {
        mPopupParent = v;
    }

    /**
     * Set parameters on the KeyboardOffset.
     * @param x  The value of KeyboardOffset.
     * @param y  The value of KeyboardOffset.
     */
    public void setPopupOffset(int x, int y) {
        mMiniKeyboardOffsetX = x;
        mMiniKeyboardOffsetY = y;
        if (mPreviewPopup.isShowing()) {
            mPreviewPopup.dismiss();
        }
    }

    /**
     * When enabled, calls to {@link OnKeyboardActionListener#onKey} will include key
     * codes for adjacent keys.  When disabled, only the primary key code will be
     * reported.
     * @param enabled whether or not the proximity correction is enabled
     */
    public void setProximityCorrectionEnabled(boolean enabled) {
        mProximityCorrectOn = enabled;
    }

    /**
     * Returns true if proximity correction is enabled.
     */
    public boolean isProximityCorrectionEnabled() {
        return mProximityCorrectOn;
    }

    /**
     * Popup keyboard close button clicked.
     */
    public void onClick(View v) {
        dismissPopupKeyboard();
    }

    private CharSequence adjustCase(CharSequence label) {
        if (mKeyboard.isShifted() && label != null && label.length() < 3 && label.length() > 0
                && Character.isLowerCase(label.charAt(0))) {
            label = iWnnEngine.getEngine().toUpperCase(label.toString());
        }
        return label;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Round up a little
        if (mKeyboard == null) {
            setMeasuredDimension(getPaddingLeft() + getPaddingRight(), getPaddingTop() + getPaddingBottom());
        } else {
            int width = mKeyboard.getDisplayWidth() + getPaddingLeft() + getPaddingRight();
            if (mKeyboard.getMiniKeyboardWidth() > 0) {
                width = mKeyboard.getMiniKeyboardWidth() + getPaddingLeft() + getPaddingRight();
            }
            if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
                width = MeasureSpec.getSize(widthMeasureSpec);
            }
            setMeasuredDimension(width, mKeyboard.getHeight() + getPaddingTop() + getPaddingBottom());
        }
    }

    /**
     * Compute the average distance between adjacent keys (horizontally and vertically)
     * and square it to get the proximity threshold. We use a square here and in computing
     * the touch distance from a key's center to avoid taking a square root.
     * @param keyboard
     */
    private void computeProximityThreshold(Keyboard keyboard) {
        if (keyboard == null) return;
        final Key[] keys = mKeys;
        if (keys == null) return;
        int length = keys.length;
        int dimensionSum = 0;
        for (int i = 0; i < length; i++) {
            Key key = keys[i];
            dimensionSum += Math.min(key.width, key.height) + key.gap;
        }
        if (dimensionSum < 0 || length == 0) return;
        mProximityThreshold = (int) (dimensionSum * 1.4f / length);
        mProximityThreshold *= mProximityThreshold; // Square it
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Release the buffer, if any and it will be reallocated on the next draw
        mBuffer = null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawPending || mBuffer == null || mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(mBuffer, 0, 0, null);
    }

    private void onBufferDraw() {
        boolean isBufferNull = (mBuffer == null);
        Resources r = getContext().getResources();
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        if (isBufferNull || mKeyboardChanged) {
            if (isBufferNull || mKeyboardChanged &&
                    (mBuffer.getWidth() != getWidth() || mBuffer.getHeight() != getHeight())) {
                // Make sure our bitmap is at least 1x1
                final int width = Math.max(1, getWidth());
                final int height = Math.max(1, getHeight());
                mBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBuffer);
            }
            invalidateAllKeys();
            mKeyboardChanged = false;
        }
        final Canvas canvas = mCanvas;
        canvas.clipRect(mDirtyRect, Op.REPLACE);

        if (mKeyboard == null) return;

        final Paint paint = mPaint;
        final Rect clipRegion = mClipRegion;
        final Rect padding = mPadding;
        final int kbdPaddingLeft = getPaddingLeft();
        final int kbdPaddingTop = getPaddingTop();
        final Key[] keys = mKeys;
        final Key invalidKey = mInvalidatedKey;

        paint.setColor(mKeyTextColor);
        boolean drawSingleKey = false;
        if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
          // Is clipRegion completely contained within the invalidated key?
          if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left &&
                  invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top &&
                  invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right &&
                  invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
              drawSingleKey = true;
          }
        }
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);

        Context context = getContext();
        Resources res = context.getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int maxHeight = 0;
        int defaultHeight = 0;
        int setHeight = 0;
        if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            maxHeight = res.getDimensionPixelSize(R.dimen.keysize_preference_portrait_max);
            defaultHeight = KeySizeDialogPreference.getKeyHeight(context, R.dimen.key_height,
                                                                 Configuration.ORIENTATION_PORTRAIT,
                                                                 res.getInteger(R.integer.key_height_default_value));
            setHeight = sharedPref.getInt(KeySizeDialogPreference.KEY_HEIGHT_PORTRAIT_KEY, defaultHeight);
        } else {
            maxHeight = res.getDimensionPixelSize(R.dimen.keysize_preference_landscape_max);
            defaultHeight = KeySizeDialogPreference.getKeyHeight(context, R.dimen.key_height,
                                                                 Configuration.ORIENTATION_LANDSCAPE,
                                                                 res.getInteger(R.integer.key_height_default_value));
            setHeight = sharedPref.getInt(KeySizeDialogPreference.KEY_HEIGHT_LANDSCAPE_KEY, defaultHeight);
        }
        float scaleRate = (float)setHeight / (float)maxHeight;
        float defaultscale = (float)defaultHeight / (float)maxHeight;

        KeyboardSkinData skin = KeyboardSkinData.getInstance();
        // Don't resize if the active keyboard skin does NOT support resizing.
        boolean isSkinNotPermitScale = (skin.isValid() && (skin.getTargetVersion() < VERSION_KEYTOPIMAGE_PREMIT_SCALE));

        final int keyCount = keys.length;
        int functionIndex = FunctionKey_FUNC_VOICEINPUT;
        if(!isFunctionEnable(FunctionKey_FUNC_VOICEINPUT)){
            if(mKeyboardType == KEYBOARD_QWERTY) {
                functionIndex = FunctionKey_FUNC_NORMALKEYBOARD;
            } else {
                functionIndex = FunctionKey_FUNC_KBD;
            }
            if(OpenWnn.getCurrentIme() != null) {
                DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
                if (keyboard instanceof DefaultSoftKeyboardKorean) {
                    functionIndex = FunctionKey_FUNC_HANDWRITING;
                }
            }
        }
        for (int i = 0; i < keyCount; i++) {
            final Key key = keys[i];
            if (drawSingleKey && invalidKey != key) {
                continue;
            }

            if (key.pressed) {
                if(key.codes[0] != KEYCODE_POPUP_TITLE_INPUT_LANGUAGE){
                    if (keyskin.isValid()) {
                        int color = keyskin.getKeyPressedTextColor();
                        if (color != 0) {
                            mKeyPressedTextColor = color;
                        }
                    } else {
                        mKeyPressedTextColor = keyskin.getColor(
                                OpenWnn.getContext(), R.color.key_qwerty_text_color);
                    }
                    paint.setColor(key.isSecondKey ? mKeyTextColor2nd : mKeyPressedTextColor);
                }
            } else {
                paint.setColor(key.isSecondKey ? mKeyTextColor2nd : mKeyTextColor);
            }

            Drawable keyBackground;

            if (!key.showPreview && (key.label != null && key.label.toString().equals(" "))) {
                if (keyskin.isValid()) {
                    keyBackground = key.isSecondKey ?
                            keyskin.getDrawable(R.drawable.btn_keyboard_key_normal_2nd) : keyskin.getDrawable(R.drawable.ime_keypad_btn_normal);
                } else {
                    keyBackground = key.isSecondKey ?
                            r.getDrawable(R.drawable.btn_keyboard_key_normal_2nd) : r.getDrawable(R.drawable.ime_keypad_btn_normal);
                }
            } else if (key.isLineImageKey) {
                if (keyskin.isValid()) {
                    keyBackground = keyskin.getDrawable(R.drawable.ime_btn_effect_line);
                } else {
                    keyBackground = r.getDrawable(R.drawable.ime_btn_effect_line);
                }
            } else {
                keyBackground = key.isSecondKey ? mKeyBackground2nd : mKeyBackground;
            }

            int[] drawableState = null;
            if (key.codes[0] == KEYCODE_POPUP_TITLE_INPUT_LANGUAGE) {
                drawableState = KEY_STATE_NORMAL;
            } else {
                drawableState = key.getCurrentDrawableState();
            }
            keyBackground.setState(drawableState);

            String label = key.label == null? null : key.label.toString();

            final Rect bounds = keyBackground.getBounds();
            if (key.width != bounds.right ||
                    key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height);
            }
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            keyBackground.draw(canvas);

            boolean isQwertyEiji = (key.icon != null)
                    && (key.keyIconId >= KEY_ID_QWERTY_NUM_SYMBOL_Q)
                    && (key.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_M);
            boolean isPhoneNum = (key.keyIconId >= KEY_ID_PHONE_NUM)
                    && (key.keyIconId < KEY_ID_PHONE_SYM);

            if ((label != null)
                    && (!isQwertyEiji)
                    && (!isPhoneNum)
                    && (DefaultSoftKeyboard.KEYCODE_JP12_ENTER != key.codes[0])
                    && OpenWnn.getCurrentIme() != null) {
                DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;

                // For characters, use large font. For labels like "Done", use small font.
                int textSize = 0;
                if (label.length() > 1 && key.codes.length < 2) {
                    textSize = mLabelTextSize;
                    paint.setTypeface(Typeface.DEFAULT);
                } else {
                    textSize = mKeyboard.isPhone ?
                            r.getDimensionPixelSize(R.dimen.key_label_phone_text_size)
                            : mKeyTextSize;
                    if ((mKeyboardType == KEYBOARD_QWERTY)
                            && ((keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_HALF_NUMBER)
                             || (keyboard.getKeyMode() == DefaultSoftKeyboard.KEYMODE_JA_FULL_NUMBER))) {
                        textSize = r.getDimensionPixelSize(R.dimen.key_qwerty_text_size);
                        paint.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                }

                if ((mKeyboard.getXmlLayoutResId() != R.xml.keyboard_popup_num_symbol_template)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_mail)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_url)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_en)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_en_full)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_ja)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_ja_full)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.keyboard_4key)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_changelanguage)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_changelanguage_unable_ko)) {
                    boolean mOneHandedShown = false;
                    if (keyboard != null) {
                        mOneHandedShown = ((DefaultSoftKeyboard)keyboard).isOneHandedMode();
                    }
                    if (mOneHandedShown) {
                        textSize = r.getDimensionPixelSize(R.dimen.key_onehanded_qwerty_text_size);
                    }
                }

                if ((mKeyboard.getXmlLayoutResId() == R.xml.popup_mail)
                        || (mKeyboard.getXmlLayoutResId() == R.xml.popup_url)) {
                    textSize = r.getDimensionPixelSize(R.dimen.popup_email_url_text_size);
                    paint.setTypeface(Typeface.DEFAULT);
                } else if (isPeriodPopupWindow(mKeyboard.getXmlLayoutResId())) {
                    textSize = r.getDimensionPixelSize(R.dimen.popup_period_text_size);
                    paint.setTypeface(Typeface.DEFAULT);
                } else if ((mKeyboard.getXmlLayoutResId() == R.xml.popup_changelanguage)
                        || (mKeyboard.getXmlLayoutResId() == R.xml.popup_changelanguage_unable_ko)) {
                    textSize = r.getDimensionPixelSize(R.dimen.popup_change_language_text_size);
                    paint.setTypeface(Typeface.DEFAULT);
                }

                float textScaleRate = scaleRate;
                if (isSkinNotPermitScale) {
                    textScaleRate = defaultscale;
                }

                float setSize = ((float)textSize) * textScaleRate;
                paint.setTextSize(setSize);

                // Draw a drop shadow for the text
                if (key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_ENTER) {
                    int colorId = R.color.key_text_shadow_color;
                    if (!skin.isValid() || skin.getColor(colorId) != 0) {
                        paint.setShadowLayer(KEY_TEXT_SHADOW_RADIUS, 0, 0,
                                skin.getColor(OpenWnn.getContext(), colorId));
                    }
                } else {
                    paint.setShadowLayer(key.isSecondKey ? mShadowRadius2nd : mShadowRadius, 0, 0,
                                            key.isSecondKey ? mShadowColor2nd : mShadowColor);
                }

                // Draw the text
                float posX = 0;
                float posY = 0;
                if ((mKeyboard.getXmlLayoutResId() == R.xml.popup_mail)
                        || (mKeyboard.getXmlLayoutResId() == R.xml.popup_url)) {
                    if (key.pressed) {
                        paint.setColor(keyskin.getColor(
                                OpenWnn.getContext(), R.color.popup_text_color_press));
                    } else {
                        paint.setColor(keyskin.getColor(
                                OpenWnn.getContext(), R.color.popup_text_color));
                    }
                    posX = (float)r.getDimensionPixelSize(R.dimen.popup_url_text_left_gap);
                    posY = (((float) key.height) - ((float) padding.top) - ((float) padding.bottom))
                            / 2.0f
                            + (((float) paint.getTextSize()) - ((float) paint.descent())) / 2.0f
                            + ((float) padding.top);
                } else if((mKeyboard.getXmlLayoutResId() == R.xml.popup_changelanguage) ||
                        (mKeyboard.getXmlLayoutResId() == R.xml.popup_changelanguage_unable_ko)){
                    if ((key.pressed) && (key.codes[0] != KEYCODE_POPUP_TITLE_INPUT_LANGUAGE)) {
                        paint.setColor(keyskin.getColor(
                                OpenWnn.getContext(), R.color.popup_text_color_press));
                    } else {
                        paint.setColor(keyskin.getColor(
                                OpenWnn.getContext(), R.color.popup_text_color));
                    }

                    if (key.codes[0] == KEYCODE_POPUP_TITLE_INPUT_LANGUAGE ) {
                        paint.setTextAlign(Align.LEFT);
                        posX = (float)padding.left;
                    } else {
                        paint.setTextAlign(Align.CENTER);
                        posX = (float)((key.width - padding.left - padding.right) / 2 + padding.left);
                    }
                    posY = (((float) key.height) - ((float) padding.top) - ((float) padding.bottom))
                            / 2.0f
                            + (((float) paint.getTextSize()) - ((float) paint.descent())) / 2.0f
                            + ((float) padding.top);
                } else {
                    if ((mKeyboard.getXmlLayoutResId() == R.xml.keyboard_popup_num_symbol_template)
                            || isPeriodPopupWindow(mKeyboard.getXmlLayoutResId())) {
                        if (key.pressed) {
                            paint.setColor(keyskin.getColor(
                                    OpenWnn.getContext(), R.color.popup_text_color_press));
                        } else {
                            paint.setColor(keyskin.getColor(
                                    OpenWnn.getContext(), R.color.popup_text_color));
                        }
                    } else if (mKeyboard.isPhone) {
                        if (key.pressed) {
                            if (!keyskin.isValid()) {
                                paint.setColor(r.getColor(R.color.key_text_color_std));
                            } else {
                                paint.setColor(keyskin.getColor(
                                        OpenWnn.getContext(), R.color.key_10key_text_color_top));
                            }
                        } else {
                            paint.setColor(keyskin.getColor(
                                    OpenWnn.getContext(), R.color.key_10key_text_color_top));
                        }
                    }

                    posX = (((float)key.width) - ((float)padding.left) - ((float)padding.right)) / 2.0f
                            + ((float)padding.left);
                    posY = (((float) key.height) - ((float) padding.top) - ((float) padding.bottom))
                            / 2.0f
                            + (((float) paint.getTextSize()) - ((float) paint.descent())) / 2.0f
                            + ((float) padding.top);
                }
                canvas.drawText(label, posX, posY, paint);

                // Turn off drop shadow
                paint.setShadowLayer(0, 0, 0, 0);
            } else if (key.icon != null) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
                String emojiMode = pref.getString("opt_one_touch_emoji_list",
                        DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_HIDDEN);
                boolean isQwerty = (mKeyboardType == KEYBOARD_QWERTY);
                boolean isSmall = false;
                if (!emojiMode.equals(DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_HIDDEN) &&
                        (r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) &&
                        isQwerty) {
                    isSmall = true;
                }

                if (DefaultSoftKeyboard.KEYCODE_FUNCTION_KEY == key.codes[0]) {
                    if (keyskin.isValid()) {
                        if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                                keyskin.getPackageName())
                                || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                                keyskin.getPackageName())) {
                            key.iconPreview = keyskin.getDrawable(
                                    FUNCTION_PREVIEW_DRAWABLE_TABLE_POPUP[functionIndex]);
                        } else {
                            key.iconPreview = keyskin.getDrawable(
                                    FUNCTION_PREVIEW_DRAWABLE_TABLE[functionIndex]);
                        }
                    } else {
                        key.iconPreview =
                                r.getDrawable(FUNCTION_PREVIEW_DRAWABLE_TABLE_BASIC[functionIndex]);
                    }
                    if (key.iconPreview != null) {
                        key.iconPreview.setBounds(0, 0,
                                key.iconPreview.getIntrinsicWidth(),
                                key.iconPreview.getIntrinsicHeight());
                    }
                } else if (Keyboard.KEYCODE_SHIFT == key.codes[0]) {
                    key.icon.setState(drawableState);
                } // else

                int row = 0;
                int column = 0; // key released
                for (int j=0; j<KEYCODE_FUNCTION_MAX; j++) {
                    if (FUNCTION_KEY_CONVERT_TABLE[j][0] == key.codes[0]) {
                        if (key.pressed) {
                            column = 1; // key pressed 
                        }
                        row = FUNCTION_KEY_CONVERT_TABLE[j][1];
                        if (KEYCODE_FUNCTION_KEYBOARDTYPE == key.codes[0]) {
                            if (       mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_qwerty
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_qwerty_emoji_on
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_qwerty_onehand
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_qwerty_split
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_unable_qwerty
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_unable_voice_input_qwerty
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_unable_voice_input_qwerty_emoji_on
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_unable_voice_qwerty
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_unable_voice_qwerty_onehand
                                    || mKeyboard.getXmlLayoutResId() == R.xml.keyboard_functionkey_preview_unable_voice_qwerty_split) {

                                row = FUNCTION_KEY_ICON_10KEY_INDEX;
                            }
                        }

                        if (keyskin.isValid()) {
                            key.icon = keyskin.getDrawable(FUNCTION_KEY_ICON_TABLE[row][column]);
                        } else {
                            key.icon = r.getDrawable(FUNCTION_KEY_ICON_TABLE[row][column]);
                        }
                        break;
                    }
                }

                if (DefaultSoftKeyboard.KEYCODE_JP12_RIGHT == key.codes[0]) {
                    if (isSmall) {
                        if(keyskin.isValid()){
                            key.icon = keyskin.getDrawable((int)R.drawable.ime_keypad_icon_right_small);
                        } else {
                            key.icon = r.getDrawable(R.drawable.ime_keypad_icon_right_small);
                        }
                        if (key.iconPreview != null) {
                            key.iconPreview.setBounds(0, 0,
                                    key.iconPreview.getIntrinsicWidth(),
                                    key.iconPreview.getIntrinsicHeight());
                        }
                    }
                    drawThemeKeyPreviewPopupIcon(
                            keyskin, key, DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_ARROW_R_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_JP12_LEFT == key.codes[0]) {
                    if (isSmall) {
                        if (key.iconPreview != null) {
                            key.iconPreview.setBounds(0, 0,
                                    key.iconPreview.getIntrinsicWidth(),
                                    key.iconPreview.getIntrinsicHeight());
                        }
                    } 
                    drawThemeKeyPreviewPopupIcon(
                            keyskin, key, DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_ARROW_L_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE == key.codes[0]
                        || DefaultSoftKeyboard.KEYCODE_JP12_BACKSPACE == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(
                            keyskin, key, DefaultSoftKeyboard.IME_KEYPAD_ICON_DEL_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_JP12_SPACE == key.codes[0]) {
                    if (key.keyIconPreviewId != DefaultSoftKeyboard.KEY_ID_PREVIEW_SPACE_JP) {
                        drawThemeKeyPreviewPopupIcon(
                                keyskin, key, DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_SPACE_POPUP);
                    }
                }
                if ((DefaultSoftKeyboard.KEYCODE_JP12_ENTER == key.codes[0] && label == null) ||
                        (DefaultSoftKeyboard.KEYCODE_QWERTY_ENTER == key.codes[0]
                        && label == null)) {
                    OpenWnn wnn = OpenWnn.getCurrentIme();
                    if (wnn != null) {
                        EditorInfo edit = wnn.getCurrentInputEditorInfo();
                        int enterType = (edit.imeOptions
                                & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION));
                        if (enterType == EditorInfo.IME_ACTION_SEARCH) {
                            if (isQwerty) {
                                drawThemeKeyPreviewPopupIcon(keyskin, key,
                                        DefaultSoftKeyboard.IME_KEYPAD_ICON_SEARCH_POPUP);
                            } else {
                                drawThemeKeyPreviewPopupIcon(keyskin, key,
                                        DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_SEARCH_POPUP);
                            }
                        } else {
                            if (isQwerty) {
                                drawThemeKeyPreviewPopupIcon(keyskin, key,
                                        DefaultSoftKeyboard.IME_KEYPAD_ICON_ENTER_POPUP);
                            } else {
                                drawThemeKeyPreviewPopupIcon(keyskin, key,
                                        DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_ENTER_POPUP);
                            }
                        }
                    }
                }
                if (DefaultSoftKeyboard.KEYCODE_JP12_REVERSE == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_REVERSE_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_KEYPAD_UP == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_4WAY_UP_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_KEYPAD_DOWN == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_4WAY_DOWN_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_KEYPAD_LEFT == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_4WAY_LEFT_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_KEYPAD_RIGHT == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_4WAY_RIGHT_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_KEYPAD_HOME == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_4WAY_HOME_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_KEYPAD_END == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_4WAY_END_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_4KEY_MODE == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_SYMBOL_ICON_TEXT_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_4KEY_UP == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_SYMBOL_ICON_UP_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_4KEY_DOWN == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_SYMBOL_ICON_DOWN_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_4KEY_CLEAR == key.codes[0]) {
                    drawThemeKeyPreviewPopupIcon(keyskin, key,
                            DefaultSoftKeyboard.IME_KEYPAD_ICON_DEL_POPUP);
                }
                if (DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT == key.codes[0]) {
                    if( (key.keyIconPreviewId == DefaultSoftKeyboard.KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_1ST)
                      || (key.keyIconPreviewId == DefaultSoftKeyboard.KEY_ID_PREVIEW_QWERTY_NUM_SHIFT_2ND)
                      || (key.keyIconPreviewId == DefaultSoftKeyboard.KEY_ID_PREVIEW_PHONE_SYM) 
                      || (key.keyIconPreviewId == DefaultSoftKeyboard.KEY_ID_PREVIEW_PHONE_123)){
                    } else {
                        drawThemeKeyPreviewPopupIcon(keyskin, key,
                               DefaultSoftKeyboard.IME_BTN_FUNC_SHIFT_BLK);
                    }
                }
                if ((mKeyboard.getXmlLayoutResId() == R.xml.keyboard_12key_phone)
                        && DefaultSoftKeyboardJAJP.KEYCODE_KEYBOARD_SETTING == key.codes[0]) {
                    if (keyskin.isValid()) {
                        key.icon = keyskin.getDrawable((int)R.drawable.ime_keypad_icon_jp_setting);
                        if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                                keyskin.getPackageName())
                                || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                                keyskin.getPackageName())) {
                            key.iconPreview = keyskin.getDrawable(
                                    DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_SETTING_POPUP);
                        } else {
                            key.iconPreview = keyskin.getDrawable(
                                    (int)R.drawable.ime_keypad_icon_jp_setting);
                        }
                    } else {
                        key.icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_setting);
                        key.iconPreview = r.getDrawable((int)R.drawable.ime_keypad_icon_jp_setting);
                    }
                    if (key.iconPreview != null) {
                        key.iconPreview.setBounds(0, 0,
                                key.iconPreview.getIntrinsicWidth(),
                                key.iconPreview.getIntrinsicHeight());
                    }
                }
                if (mKeyboard.getXmlLayoutResId() == R.xml.keyboard_12key_phone_shift) {
                    if (KEYCODE_PHONEKEYBOARD_SPACE == key.codes[0]) {
                        if (keyskin.isValid()) {
                            key.icon = keyskin.getDrawable((int)R.drawable.ime_keypad_icon_jp_space_blk);
                            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                                    keyskin.getPackageName())
                                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                                    keyskin.getPackageName())) {
                                key.iconPreview = keyskin.getDrawable(
                                        DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_SPACE_POPUP);
                            } else {
                                key.iconPreview = keyskin.getDrawable(
                                        (int)R.drawable.ime_keypad_icon_jp_space_blk);
                            }
                        } else {
                            key.icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_space_blk);
                            key.iconPreview = r.getDrawable(
                                    (int)R.drawable.ime_keypad_icon_jp_space_blk);
                        }
                        if (key.iconPreview != null) {
                            key.iconPreview.setBounds(0, 0,
                                    key.iconPreview.getIntrinsicWidth(),
                                    key.iconPreview.getIntrinsicHeight());
                        }
                    }
                    if (DefaultSoftKeyboardJAJP.KEYCODE_KEYBOARD_SETTING == key.codes[0]) {
                        if (keyskin.isValid()) {
                            key.icon = keyskin.getDrawable((int)R.drawable.ime_keypad_icon_jp_setting);
                            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                                    keyskin.getPackageName())
                                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                                    keyskin.getPackageName())) {
                                key.iconPreview = keyskin.getDrawable(
                                        DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_SETTING_POPUP);
                            } else {
                                key.iconPreview = keyskin.getDrawable(
                                        (int)R.drawable.ime_keypad_icon_jp_setting);
                            }
                        } else {
                            key.icon = r.getDrawable(R.drawable.ime_keypad_icon_jp_setting);
                            key.iconPreview = r.getDrawable(
                                    (int)R.drawable.ime_keypad_icon_jp_setting);
                        }
                        if (key.iconPreview != null) {
                            key.iconPreview.setBounds(0, 0,
                                    key.iconPreview.getIntrinsicWidth(),
                                    key.iconPreview.getIntrinsicHeight());
                        }
                    }
                } else {
                    if (KEYCODE_PHONEKEYBOARD_SPACE == key.codes[0]) {
                        if (key.keyIconPreviewId != DefaultSoftKeyboard.KEY_ID_PREVIEW_SPACE_JP) {
                            drawThemeKeyPreviewPopupIcon(keyskin, key,
                                    DefaultSoftKeyboard.IME_KEYPAD_ICON_JP_SPACE_POPUP);
                        }
                    }
                }

                Drawable draw = key.icon;
                float drawableScaleRate = scaleRate;
                if (isSkinNotPermitScale) {
                    if (key.mIsIconSkin) {
                        drawableScaleRate = DEFALT_KEY_SIZE_RATE;
                    } else {
                        drawableScaleRate = defaultscale;
                    }
                }
                if ((key.icon instanceof BitmapDrawable) && (drawableScaleRate != DEFALT_KEY_SIZE_RATE)) {
                    final Bitmap tempBitmap = ((BitmapDrawable)(key.icon)).getBitmap();
                    Matrix matrix = new Matrix();
                    matrix.setScale(drawableScaleRate, drawableScaleRate);

                    Bitmap scaleBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true);
                    draw = new BitmapDrawable(res, scaleBitmap);
                }
                final int drawableX = (key.width - padding.left - padding.right
                                - draw .getIntrinsicWidth()) / 2 + padding.left;
                int drawableY = (key.height - padding.top - padding.bottom
                        - draw.getIntrinsicHeight()) / 2 + padding.top;
                if (Keyboard.KEYCODE_SHIFT == key.codes[0] && res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    drawableY += key.height / 20;
                }
                if ((mKeyboard.getXmlLayoutResId() != R.xml.popup_changelanguage)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_changelanguage_unable_ko)) {
                    canvas.translate(drawableX, drawableY);
                }
                draw.setBounds(0, 0,
                        draw.getIntrinsicWidth(), draw.getIntrinsicHeight());
                draw.draw(canvas);
                if ((mKeyboard.getXmlLayoutResId() != R.xml.popup_changelanguage)
                        && (mKeyboard.getXmlLayoutResId() != R.xml.popup_changelanguage_unable_ko)) {
                    canvas.translate(-drawableX, -drawableY);
                }
                if (key.keyIconId >= KEY_ID_QWERTY_NUM_SYMBOL_Q && key.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_M) {

                    boolean mOneHandedShown  = false;
                    if(OpenWnn.getCurrentIme() != null) {
                        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;

                        if (keyboard != null) {
                            mOneHandedShown = ((DefaultSoftKeyboard)keyboard).isOneHandedMode();
                        }
                    }
                    if (mOneHandedShown) {
                        paint.setTextSize(r.getDimensionPixelSize(R.dimen.key_onehanded_qwerty_text_size));
                    } else {
                        paint.setTextSize(r.getDimensionPixelSize(R.dimen.key_qwerty_text_size));
                    }

                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    paint.setColor(keyskin.getColor(OpenWnn.getContext(), R.color.key_qwerty_text_color));
                    paint.setTextAlign(Align.CENTER);
                    canvas.drawText(label,
                            (((float)key.width) - ((float)padding.left) - ((float)padding.right)) / 2
                                    + ((float)padding.left),
                            (((float)key.height) - ((float)padding.top) - ((float)padding.bottom)) / 2
                                    + (((float)paint.getTextSize()) - ((float)paint.descent())) / 2
                                    + ((float)padding.top),
                             paint);
                }
            } // else {}
            canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);

            // Draw hint label.
            if (key.hintLabel != null) {
                final CharSequence hint = key.hintLabel;
                final float hintSize = key.height * mKeyHintLetterRatio;
                paint.setTextSize(hintSize);
                final KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
                paint.setColor(resMan.getColor(getContext(), R.color.key_hint_letter, "KeyHintColor"));
                paint.setTextAlign(Align.CENTER);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                final int paddingTop = getPaddingTop();
                final float hintX = key.x + key.width - mKeyHintLetterPadding
                        - getCharWidth(KEY_NUMERIC_HINT_LABEL_REFERENCE_CHAR, paint) / 2;
                final float hintY = paddingTop + key.y - paint.ascent() + mKeyHintLetterPadding;
                canvas.drawText(hint, 0, hint.length(), hintX, hintY, paint);
            }

        }
        mInvalidatedKey = null;
        // Overlay a dark rectangle to dim the keyboard
        if (mMiniKeyboardOnScreen) {
            paint.setColor((int) (mBackgroundDimAmount * 0xFF) << 24);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }

        if (DEBUG) {
            if (!mDebugFlickArea.isEmpty()) {
                final int[] colorList = {Color.YELLOW, Color.RED, Color.BLUE, Color.WHITE};
                for (Path path : mDebugFlickArea) {
                    if (path != null) {
                        int color = colorList[mDebugFlickArea.indexOf(path) % colorList.length];
                        paint.setColor(color);
                        paint.setAlpha(128);
                        canvas.drawPath(path, paint);
                    }
                }
            }
        }

        mDrawPending = false;
        mDirtyRect.setEmpty();
    }

    private void drawThemeKeyPreviewPopupIcon(KeyboardSkinData keyskin, Key key, String id) {
        if (keyskin.isValid()) {
            if (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_LOVELY.endsWith(
                    keyskin.getPackageName())
                    || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(
                    keyskin.getPackageName())) {
                key.iconPreview = keyskin.getDrawable(id);
            }
        }
        if (key.iconPreview != null) {
            key.iconPreview.setBounds(0, 0,
                    key.iconPreview.getIntrinsicWidth(),
                    key.iconPreview.getIntrinsicHeight());
        }
    }

    private int getKeyIndices(int x, int y, int[] allKeys) {
        final Key[] keys = mKeys;
        int primaryIndex = NOT_A_KEY;
        int closestKey = NOT_A_KEY;
        int closestKeyDist = mProximityThreshold + 1;
        java.util.Arrays.fill(mDistances, Integer.MAX_VALUE);
        int [] nearestKeyIndices = mKeyboard.getNearestKeys(x, y);
        final int keyCount = nearestKeyIndices.length;
        for (int i = 0; i < keyCount; i++) {
            final Key key = keys[nearestKeyIndices[i]];
            int dist = 0;
            boolean isInside = key.isInside(x,y);
            if (isInside) {
                primaryIndex = nearestKeyIndices[i];
            }

            if (((mProximityCorrectOn
                    && (dist = key.squaredDistanceFrom(x, y)) < mProximityThreshold)
                    || isInside)
                    && key.codes[0] > 32) {
                // Find insertion point
                final int nCodes = key.codes.length;
                if (dist < closestKeyDist) {
                    closestKeyDist = dist;
                    closestKey = nearestKeyIndices[i];
                }

                if (allKeys == null) continue;

                for (int j = 0; j < mDistances.length; j++) {
                    if (mDistances[j] > dist) {
                        // Make space for nCodes codes
                        System.arraycopy(mDistances, j, mDistances, j + nCodes,
                                mDistances.length - j - nCodes);
                        System.arraycopy(allKeys, j, allKeys, j + nCodes,
                                allKeys.length - j - nCodes);
                        for (int c = 0; c < nCodes; c++) {
                            allKeys[j + c] = key.codes[c];
                            mDistances[j + c] = dist;
                        }
                        break;
                    }
                }
            }
        }
        if (primaryIndex == NOT_A_KEY) {
            primaryIndex = closestKey;
        }
        return primaryIndex;
    }

    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        if (index != NOT_A_KEY && index < mKeys.length) {
            final Key key = mKeys[index];
            if (key.text != null) {
                mKeyboardActionListener.onText(key.text);
                mKeyboardActionListener.onRelease(NOT_A_KEY);
            } else {
                int code = key.codes[0];
                //TextEntryState.keyPressedAt(key, x, y);
                int[] codes = new int[MAX_NEARBY_KEYS];
                Arrays.fill(codes, NOT_A_KEY);
                getKeyIndices(x, y, codes);
                // Multi-tap
                if (mInMultiTap) {
                    if (mTapCount != -1) {
                        if (code == KEYCODE_PERIOD) {
                            OpenWnn wnn = OpenWnn.getCurrentIme();
                            if (wnn != null) {
                                InputConnection ic = wnn
                                        .getCurrentInputConnection();
                                if (ic != null) {
                                    ic.deleteSurroundingText(1, 0);
                                }
                            }
                        } else {
                            mKeyboardActionListener
                                    .onKey(DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE,
                                            KEY_DELETE);
                        }
                    } else {
                        mTapCount = 0;
                    }
                    code = key.codes[mTapCount];
                }
                mKeyboardActionListener.onKey(code, codes);
                mKeyboardActionListener.onRelease(code);
            }
            mLastSentIndex = index;
            mLastTapTime = eventTime;
        }
    }

    /**
     * Handle multi-tap keys by producing the key label for the current multi-tap state.
     */
    private CharSequence getPreviewText(Key key) {
        if (mInMultiTap) {
            // Multi-tap
            mPreviewLabel.setLength(0);
            mPreviewLabel.append((char) key.codes[mTapCount < 0 ? 0 : mTapCount]);
            return adjustCase(mPreviewLabel);
        } else {

            if (key.label == null ) {
                return key.keyPreviewLabel;
            } else {
                return key.label;
            }
        }
    }

    private void showPreview(int keyIndex) {
        int oldKeyIndex = mCurrentKeyIndex;
        final PopupWindow previewPopup = mPreviewPopup;

        mCurrentKeyIndex = keyIndex;
        final Key[] keys = mKeys;
        if (isFunctionResId(mKeyboard.getXmlLayoutResId()) == true) {
                if(keyIndex != 0){
                    keys[0].onReleased(false);
                    invalidateKey(0);
                }
            }

        // Release the old key and press the new key
        if (oldKeyIndex != mCurrentKeyIndex) {
            if (oldKeyIndex != NOT_A_KEY && keys.length > oldKeyIndex) {
                keys[oldKeyIndex].onReleased(mCurrentKeyIndex == NOT_A_KEY);
                invalidateKey(oldKeyIndex);
            }
            if (mCurrentKeyIndex != NOT_A_KEY && keys.length > mCurrentKeyIndex) {
                keys[mCurrentKeyIndex].onPressed();
                invalidateKey(mCurrentKeyIndex);
            }
        }

        // If key changed and preview is on ...
        if (oldKeyIndex != mCurrentKeyIndex && mShowPreview && isParentPreviewEnabled()) {
            mHandler.removeMessages(MSG_SHOW_PREVIEW);
            if (previewPopup.isShowing()) {
                if (keyIndex == NOT_A_KEY) {
                    mHandler.sendMessageDelayed(mHandler
                            .obtainMessage(MSG_REMOVE_PREVIEW),
                            DELAY_AFTER_PREVIEW);
                }
            }
            if (keyIndex != NOT_A_KEY) {
                if (previewPopup.isShowing() && mPreviewText.getVisibility() == VISIBLE) {
                    // Show right away, if it's already visible and finger is moving around
                    showKey(keyIndex);
                } else {
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_SHOW_PREVIEW, keyIndex, 0),
                            DELAY_BEFORE_PREVIEW);
                }
            }
        }
    }

    private void showKey(final int keyIndex) {
        final PopupWindow previewPopup = mPreviewPopup;
        previewPopup.dismiss();

        final Key[] keys = mKeys;
        if (keyIndex < 0 || keyIndex >= mKeys.length) return;
        Key key = keys[keyIndex];

        if (!key.showPreview) {
            return;
        }

        if (OpenWnn.isTabletMode()) {
            if (key.codes[0] == 12288 || key.codes[0] == 32) {
                return;
            }
        }

        if (key.codes != null &&
                (key.codes[0] == DefaultSoftKeyboard.KEYCODE_JP12_EMOJI ||
                key.codes[0] == DefaultSoftKeyboard.KEYCODE_QWERTY_EMOJI ||
                key.codes[0] == DefaultSoftKeyboardJAJP.KEYCODE_EISU_KANA)) {
            if (mEnableMushroom) {
                key.popupResId = DUMMY_POPUP_RESID;
            } else {
                key.popupResId = 0;
            }
        }

        Resources res = getContext().getResources();
        mPreviewText.setBackgroundDrawable(res.getDrawable(R.drawable.keyboard_key_feedback));

        KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
        Drawable keyprev = resMan.getKeyPreview();
        if (keyprev != null) {
            mPreviewText.setBackgroundDrawable(keyprev);
        }
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        boolean isKeySkin = keyskin.isValid();
        int keyPreviewIndex = KEYPREVIEW_NONE;
        if (key.codes[0] == Keyboard.KEYCODE_VOICE_INPUT) {
            if (isKeySkin) {
                mPreviewText.setBackgroundDrawable(
                        keyskin.getDrawable(R.drawable.ime_btn_effect_voice_input));
            } else {
                mPreviewText.setBackgroundDrawable(res.getDrawable(
                        R.drawable.ime_btn_effect_voice_input));
            }
        } else {
            if ((key.width >= (mKeyboard.getMiniKeyboardWidth() / 5))
                    || (key.width >= (mKeyboard.getMiniKeyboardWidth() / 4))) {
                if (mKeyboardType == KEYBOARD_QWERTY) {
                    keyPreviewIndex = KEYPREVIEW_QWERTY_EFFECT;
                } else {
                    if (DefaultSoftKeyboard.KEYCODE_KEYPAD_END <= key.codes[0]
                        && key.codes[0] <= DefaultSoftKeyboard.KEYCODE_4KEY_UP) {
                        keyPreviewIndex = KEYPREVIEW_EFFECT_POPUP;
                     } else {
                        OpenWnn wnn = OpenWnn.getCurrentIme();
                        if(wnn != null) {
                            EditorInfo edit = wnn.getCurrentInputEditorInfo();
                            switch(edit.inputType & EditorInfo.TYPE_MASK_CLASS){
                                case EditorInfo.TYPE_CLASS_NUMBER:
                                case EditorInfo.TYPE_CLASS_PHONE:
                                    keyPreviewIndex = KEYPREVIEW_EFFECT_POPUP;
                                    break;
                                default:
                                    keyPreviewIndex = KEYPREVIEW_EFFECT_FLICK;
                                    break;
                            }
                        }
                     }
                }
            } else {
                if (mKeyboardType == KEYBOARD_QWERTY) {
                    keyPreviewIndex = KEYPREVIEW_QWERTY_EFFECT;
                } else {
                    keyPreviewIndex = KEYPREVIEW_EFFECT;
                }
            }
            setKeyPreviewBackground(keyPreviewIndex, mPreviewText);
        }

        if ((key.popupResId == 0)
                || (key.codes != null && key.codes[0] == DefaultSoftKeyboard.KEYCODE_JP12_LEFT)) {
            if ((key.edgeFlags & Keyboard.EDGE_LEFT) > 0) {
                if (mKeyboardType == KEYBOARD_QWERTY) {
                    keyPreviewIndex = KEYPREVIEW_QWERTY_EFFECT_LEFT;
                } else {
                    keyPreviewIndex = KEYPREVIEW_EFFECT_LEFT;
                }
                setKeyPreviewBackground(keyPreviewIndex, mPreviewText);
            } else if ((key.edgeFlags & Keyboard.EDGE_RIGHT) > 0) {
                if (mKeyboardType == KEYBOARD_QWERTY) {
                    keyPreviewIndex = KEYPREVIEW_QWERTY_EFFECT_RIGHT;
                } else {
                    keyPreviewIndex = KEYPREVIEW_EFFECT_RIGHT;
                }
                setKeyPreviewBackground(keyPreviewIndex, mPreviewText);
            }
        } else {
            if ((key.edgeFlags & Keyboard.EDGE_LEFT) > 0) {

                if (mKeyboardType == KEYBOARD_QWERTY) {
                    keyPreviewIndex = KEYPREVIEW_QWERTY_EFFECT_LEFT;
                } else {
                    keyPreviewIndex = KEYPREVIEW_EFFECT_LEFT;
                }
                setKeyPreviewBackground(keyPreviewIndex, mPreviewText);

            } else if ((key.edgeFlags & Keyboard.EDGE_RIGHT) > 0) {

                if (mKeyboardType == KEYBOARD_QWERTY) {
                    keyPreviewIndex = KEYPREVIEW_QWERTY_EFFECT_RIGHT;
                } else {
                    keyPreviewIndex = KEYPREVIEW_EFFECT_RIGHT;
                }
                setKeyPreviewBackground(keyPreviewIndex, mPreviewText);
            }
        }

        if (isKeySkin) {
            int color = keyskin.getKeyPreviewColor();
            if (color != 0) {
                mPreviewText.setTextColor(color);
            }
        }

        int popupHeight = mPreviewText.getBackground().getIntrinsicHeight();
        mPreviewText.setHeight(popupHeight);
        boolean isQwertyEiji = (key.icon != null)
                && (key.keyIconId >= KEY_ID_QWERTY_NUM_SYMBOL_Q)
                && (key.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_M);
        if (((key.icon != null) && !isQwertyEiji)
                || (key.iconPreview != null)) {
            int top = 0;
            int bottom = 0;
            int bottomAdjust = res.getDimensionPixelSize(R.dimen.preview_icon_padding_bottom_adjust);
            if (key.iconPreview != null && !(key.iconPreview instanceof KeyDrawable)) {
                if (isKeySkin
                        && (KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_BLACK.endsWith(keyskin.getPackageName())
                            || KeyboardSkinData.KEYBOARD_THEME_PACKAGENAME_NATURAL.endsWith(keyskin.getPackageName()))) {
                    bottom = (popupHeight -  key.iconPreview.getIntrinsicHeight()) / 2;
                } else {
                    bottom = (popupHeight - mPreviewOffset -  key.iconPreview.getIntrinsicHeight()) / 2
                            + mPreviewOffset - bottomAdjust;
                }

            } else {
                top = res.getDimensionPixelSize(R.dimen.preview_icon_padding_top);
                bottom = res.getDimensionPixelSize(R.dimen.preview_icon_padding_bottom);
            }

            mPreviewText.setText(null);
            mPreviewText.setPadding(
                res.getDimensionPixelSize(R.dimen.preview_icon_padding_left),
                top,
                res.getDimensionPixelSize(R.dimen.preview_icon_padding_right),
                bottom);

            key.resizeIconPreview();
            mPreviewText.setCompoundDrawables(null, null, null,
                    key.iconPreview != null ? key.iconPreview : key.icon);
        } else {
            mPreviewText.setCompoundDrawables(null, null, null, null);

            mPreviewText.setText(getPreviewText(key));
            int paddingTop = 0;
            if (key.label.length() > 1 && key.codes.length < 2) {
                mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mKeyTextSize);
                paddingTop += res.getDimensionPixelSize(R.dimen.preview_text_padding_top_2word);
            } else {
                mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPreviewTextSizeLarge);
            }

            paddingTop += res.getDimensionPixelSize(R.dimen.preview_qwety_text_padding_top);

            mPreviewText.setPadding(
                res.getDimensionPixelSize(R.dimen.preview_text_padding_left),
                paddingTop,
                res.getDimensionPixelSize(R.dimen.preview_text_padding_right),
                res.getDimensionPixelSize(R.dimen.preview_text_padding_bottom));
        }
        mPreviewText.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int popupWidth = mPreviewText.getBackground().getIntrinsicWidth();
        LayoutParams lp = mPreviewText.getLayoutParams();
        if (lp != null) {
            lp.width = popupWidth;
            lp.height = popupHeight;
        }

        boolean mOneHandedShown  = false;

        if(OpenWnn.getCurrentIme() != null) {
            DefaultSoftKeyboard keyboardmode =
                    (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
            if (keyboardmode != null) {
                mOneHandedShown = ((DefaultSoftKeyboard)keyboardmode).isOneHandedMode();
            }
        }

        mPopupPreviewX = (key.x + (key.width / 2)) - (popupWidth / 2);
        if (mOneHandedShown) {
            mPopupPreviewY = key.y - popupHeight + key.vgap
                    - res.getDimensionPixelSize(R.dimen.preview_y_gap);
        } else if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPopupPreviewY = key.y - popupHeight + key.vgap
                    + getPaddingBottom()
                    - res.getDimensionPixelSize(R.dimen.preview_y_gap);
        } else {
            mPopupPreviewY = key.y - popupHeight + getPaddingBottom()
                    - res.getDimensionPixelSize(R.dimen.preview_y_gap);
        }

        mHandler.removeMessages(MSG_REMOVE_PREVIEW);
        if (mOffsetInWindow == null) {
            mOffsetInWindow = new int[2];
            getLocationInWindow(mOffsetInWindow);
            mOffsetInWindow[0] += mMiniKeyboardOffsetX; // Offset may be zero
            mOffsetInWindow[1] += mMiniKeyboardOffsetY; // Offset may be zero
            int[] mWindowLocation = new int[2];
            getLocationOnScreen(mWindowLocation);
        }

        mPopupPreviewX += mOffsetInWindow[0];
        mPopupPreviewY += mOffsetInWindow[1];

        // Set the preview background state
        if ((key.popupResId == 0) ||
                (key.codes != null && key.codes[0] ==  DefaultSoftKeyboard.KEYCODE_JP12_LEFT)) {
            if ((key.edgeFlags & Keyboard.EDGE_LEFT) > 0) {
                if ((mKeyboardType == KEYBOARD_QWERTY) && !mKeyboard.isPhone) {
                    if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        mPopupPreviewX = (int) getX() + key.x + getPaddingLeft()
                                - res.getDimensionPixelSize(R.dimen.preview_text_qwerty_left_x_gap);
                    }
                } else {
                    mPopupPreviewX = (int) getX() + key.x + getPaddingLeft();
                }
            } else if ((key.edgeFlags & Keyboard.EDGE_RIGHT) > 0) {
                if ((mKeyboardType == KEYBOARD_QWERTY) && !mKeyboard.isPhone) {
                    if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        mPopupPreviewX = (int) getX()
                                + key.x
                                - popupWidth
                                + key.width + getPaddingLeft()
                                + res.getDimensionPixelSize(R.dimen.preview_text_qwerty_left_x_gap);
                    }
                } else {
                    mPopupPreviewX = (int) getX() + key.x - popupWidth + key.width + getPaddingLeft();
                }
            } else {
                mPreviewText.getBackground().setState(EMPTY_STATE_SET);
            }
        } else {
            if ((key.edgeFlags & Keyboard.EDGE_LEFT) > 0) {
                if ((mKeyboardType == KEYBOARD_QWERTY)) {
                    if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        mPopupPreviewX = (int) getX()
                                + key.x
                                - res.getDimensionPixelSize(R.dimen.preview_text_qwerty_left_x_gap);
                    }
                } else {
                    mPopupPreviewX = (int) getX() + key.x + getPaddingLeft();
                }
            }
            if (isKeySkin) {
                keyprev = keyskin.getKeyPreview();
                if (keyprev != null) {
                    mPreviewText.getBackground().setState(LONG_PRESSABLE_STATE_SET);
                }
            }
            if ((key.edgeFlags & Keyboard.EDGE_LEFT) <= 0
                && (key.edgeFlags & Keyboard.EDGE_RIGHT) <= 0) {
                mPreviewText.getBackground().setState(EMPTY_STATE_SET);
            }

            if (key.icon != null) {
                if( key.codes[0] == DefaultSoftKeyboard.KEYCODE_FUNCTION_KEY ){
                    mPreviewText.setPadding(
                            res.getDimensionPixelSize(R.dimen.preview_icon_padding_left),
                            res.getDimensionPixelSize(R.dimen.preview_icon_padding_top),
                            res.getDimensionPixelSize(R.dimen.preview_icon_padding_right),
                            mPreviewText.getPaddingBottom());
                } else {
                    mPreviewText.setPadding(
                            res.getDimensionPixelSize(R.dimen.preview_icon_padding_left),
                            res.getDimensionPixelSize(R.dimen.preview_icon_padding_top),
                            res.getDimensionPixelSize(R.dimen.preview_icon_padding_right),
                            res.getDimensionPixelSize(R.dimen.preview_icon_padding_bottom));
                }
            } else {
                mPreviewText.setPadding(
                    res.getDimensionPixelSize(R.dimen.preview_text_padding_left),
                    res.getDimensionPixelSize(R.dimen.preview_text_padding_top),
                    res.getDimensionPixelSize(R.dimen.preview_text_padding_right),
                    res.getDimensionPixelSize(R.dimen.preview_text_padding_bottom));
            }
        }

        if (previewPopup.isShowing()) {
            previewPopup.update(mPopupPreviewX, mPopupPreviewY,
                    popupWidth, popupHeight, true);
        } else {
            previewPopup.setWidth(popupWidth);
            previewPopup.setHeight(popupHeight);
            try {
                previewPopup.showAtLocation(mPopupParent, Gravity.NO_GRAVITY,
                        mPopupPreviewX, mPopupPreviewY);
            } catch (BadTokenException e) {
                Log.d("KeyboardView", "BadTokenException");
            } catch (Exception e) {
                Log.d("KeyboardView", "Exception");
            }
        }
        mPreviewText.setVisibility(VISIBLE);
    }

    /**
     * Requests a redraw of the entire keyboard. Calling {@link #invalidate} is not sufficient
     * because the keyboard renders the keys to an off-screen buffer and an invalidate() only
     * draws the cached buffer.
     * @see #invalidateKey(int)
     */
    public void invalidateAllKeys() {
        mDirtyRect.union(0, 0, getWidth(), getHeight());
        mDrawPending = true;
        invalidate();
    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only
     * one key is changing it's content. Any changes that affect the position or size of the key
     * may not be honored.
     * @param keyIndex the index of the key in the attached {@link Keyboard}.
     * @see #invalidateAllKeys
     */
    public void invalidateKey(int keyIndex) {
        if (mKeys == null) return;
        if (keyIndex < 0 || keyIndex >= mKeys.length) {
            return;
        }
        final Key key = mKeys[keyIndex];
        mInvalidatedKey = key;
        mDirtyRect.union(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
        onBufferDraw();
        invalidate(key.x + getPaddingLeft(), key.y + getPaddingTop(),
                key.x + key.width + getPaddingLeft(), key.y + key.height + getPaddingTop());
    }

    private boolean openPopupIfRequired(MotionEvent me) {
        // Check if we have a popup layout specified first.
        if (mPopupLayout == 0) {
            return false;
        }
        if (mCurrentKey < 0 || mCurrentKey >= mKeys.length) {
            return false;
        }

        Key popupKey = mKeys[mCurrentKey];
        boolean result = onLongPress(popupKey);
        if (result) {
            mAbortKey = true;
            showPreview(NOT_A_KEY);
        }
        return result;
    }

    /**
     * Called when a key is long pressed. By default this will open any popup keyboard associated
     * with this key through the attributes popupLayout and popupCharacters.
     * @param popupKey the key that was long pressed
     * @return true if the long press is handled, false otherwise. Subclasses should call the
     * method on the base class if the subclass doesn't wish to handle the call.
     */
    protected boolean onLongPress(Key popupKey) {
        mPopupKeyboardId = popupKey.popupResId;

        if (mPopupKeyboardId != 0) {
            mMiniKeyboardContainer = mMiniKeyboardCache.get(popupKey);
            Resources r = getContext().getResources();
            int popup_view_padding_top = 0;
            int popup_view_padding_right = 0;
            int popup_view_padding_left = r.getDimensionPixelSize(R.dimen.popup_view_padding_left);
            int popup_view_padding_bottom = 0;

            if(mPopupKeyboardId == R.xml.popup_changelanguage) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean enableKoInput = pref.getBoolean("opt_korean_keyboard", true);
                if (!enableKoInput) {
                    mPopupKeyboardId = R.xml.popup_changelanguage_unable_ko;
                }
                OpenWnn wnn = OpenWnn.getCurrentIme();
                if(wnn != null ) {
                    EditorInfo edit = wnn.getCurrentInputEditorInfo();
                    switch(edit.inputType & EditorInfo.TYPE_MASK_CLASS){
                    case EditorInfo.TYPE_CLASS_NUMBER:
                    case EditorInfo.TYPE_CLASS_DATETIME:
                    case EditorInfo.TYPE_CLASS_PHONE:
                        mPopupKeyboardId = R.xml.popup_changelanguage_unable_ko;
                        break;
                    case EditorInfo.TYPE_CLASS_TEXT:
                        switch(edit.inputType & EditorInfo.TYPE_MASK_VARIATION){
                        case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                        case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                        case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                            mPopupKeyboardId = R.xml.popup_changelanguage_unable_ko;
                            break;
                        default:
                            break;
                        }
                        break;
                    default:
                        break;
                    }
                }
            }

            if (mMiniKeyboardContainer == null || isFunctionResId(mPopupKeyboardId)) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                mMiniKeyboardContainer = inflater.inflate(mPopupLayout, null);
                if (popupKey.codes[0] == KEYCODE_COM_EMAIL ||
                        popupKey.codes[0] == KEYCODE_COM_URL) {
                    popup_view_padding_top =
                            r.getDimensionPixelSize(R.dimen.popup_view_padding_com_top);
                    popup_view_padding_right =
                            r.getDimensionPixelSize(R.dimen.popup_view_padding_com_right);
                    popup_view_padding_left =
                            r.getDimensionPixelSize(R.dimen.popup_view_padding_com_left);
                    popup_view_padding_bottom = r.getDimensionPixelSize(R.dimen.popup_view_padding_com_bottom);

                } else if (isPeriodPopupWindow(mPopupKeyboardId)) {
                    popup_view_padding_top = r.getDimensionPixelSize(R.dimen.popup_view_padding_period_top);
                    popup_view_padding_right = r.getDimensionPixelSize(R.dimen.popup_view_padding_period_right);
                    popup_view_padding_left = r.getDimensionPixelSize(R.dimen.popup_view_padding_period_left);
                    popup_view_padding_bottom = r.getDimensionPixelSize(R.dimen.popup_view_padding_period_bottom);
                } else if (popupKey.codes[0] == KEYCODE_FUNCTION_POPUP){
                    popup_view_padding_top = r.getDimensionPixelSize(R.dimen.popup_view_padding_function_top);
                    popup_view_padding_right = r.getDimensionPixelSize(R.dimen.popup_view_padding_function_right);
                    popup_view_padding_left = r.getDimensionPixelSize(R.dimen.popup_view_padding_function_left);
                    popup_view_padding_bottom = r.getDimensionPixelSize(R.dimen.popup_view_padding_function_bottom);
                } else if (popupKey.codes[0] == KEYCODE_CHANGE_LANGUAGE_POPUP
                     || popupKey.codes[0] == KEYCODE_CHANGE_LANGUAGE_POPUP_KO) {
                    popup_view_padding_top = r.getDimensionPixelSize(R.dimen.popup_view_padding_change_language_top);
                    popup_view_padding_right = r.getDimensionPixelSize(R.dimen.popup_view_padding_change_language_right);
                    popup_view_padding_left = r.getDimensionPixelSize(R.dimen.popup_view_padding_change_language_left);
                } else if (popupKey.keyIconId >= KEY_ID_QWERTY_NUM_SYMBOL_Q
                        && popupKey.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_M) {
                    popup_view_padding_top = r.getDimensionPixelSize(R.dimen.popup_view_padding_num_symbol_top);
                    popup_view_padding_right = r.getDimensionPixelSize(R.dimen.popup_view_padding_num_symbol_right);
                    popup_view_padding_left = r.getDimensionPixelSize(R.dimen.popup_view_padding_num_symbol_left);
                }
                KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
                if (keyskin.isValid()) {
                    Drawable KeyFeedback = keyskin.getKeyboardKeyFeedback();
                    if (KeyFeedback != null) {
                        mMiniKeyboardContainer.setBackgroundDrawable(KeyFeedback);
                     }
                }
                mMiniKeyboardContainer.setPadding(
                        popup_view_padding_left,
                        popup_view_padding_top,
                        popup_view_padding_right,
                        popup_view_padding_bottom);

                mMiniKeyboard = (KeyboardView) mMiniKeyboardContainer.findViewById(R.id.keyboardView);
                mMiniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
                    public void onKey(int primaryCode, int[] keyCodes) {
                        if (KEYCODE_POPUP_TITLE_INPUT_LANGUAGE != primaryCode) {
                            mKeyboardActionListener.onKey(primaryCode, keyCodes);
                        }
                        if ((KEYCODE_FUNCTION_SETTINGS <= primaryCode)
                                && (primaryCode <= KEYCODE_FUNCTION_VOICEINPUT)) {
                            mHandler.removeMessages(MSG_REMOVE_FUNCTIONPOPUP);
                        }
                        if ((KEYCODE_POPUP_TITLE_INPUT_LANGUAGE <= primaryCode)
                                && (primaryCode <= KEYCODE_POPUP_LABEL_KOREAN)) {
                            mHandler.removeMessages(MSG_REMOVE_LANGUAGEPOPUP);
                        }
                        if (KEYCODE_POPUP_TITLE_INPUT_LANGUAGE != primaryCode) {
                            if (isPeriodPopupWindow(mPopupKeyboardId)) {
                                mHandler.removeMessages(MSG_REMOVE_PERIODPOPUP);
                                Message msg = mHandler.obtainMessage(MSG_REMOVE_PERIODPOPUP);
                                mHandler.sendMessageDelayed(msg, PERIOD_KEY_TIMEOUT);
                            } else {
                                dismissPopupKeyboard();
                            }
                        }
                    }

                    public void onText(CharSequence text) {
                        mKeyboardActionListener.onText(text);
                        dismissPopupKeyboard();
                    }

                    public void swipeLeft() { }
                    public void swipeRight() { }
                    public void swipeUp() { }
                    public void swipeDown() { }
                    public void onPress(int primaryCode) {
                        mKeyboardActionListener.onPress(primaryCode);
                    }
                    public void onRelease(int primaryCode) {
                        mKeyboardActionListener.onRelease(primaryCode);
                    }
                });
                //mInputView.setSuggest(mSuggest);
                Keyboard keyboard;

                boolean mOneHandedShown  = false;
                boolean mSplitShown  = false;
                boolean mNormalShown  = false;
                boolean bEmojiShown  = false;

                if(OpenWnn.getCurrentIme() != null) {
                    DefaultSoftKeyboard keyboardmode =
                            (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
                    if (keyboardmode != null) {
                        mOneHandedShown = ((DefaultSoftKeyboard)keyboardmode).isOneHandedMode();
                        mSplitShown = ((DefaultSoftKeyboard)keyboardmode).isSplitMode();
                        mNormalShown = ((DefaultSoftKeyboard)keyboardmode).isNormalInputMode();
                    }
                    if (keyboardmode instanceof DefaultSoftKeyboardJAJP) {
                        bEmojiShown = ((DefaultSoftKeyboardJAJP)keyboardmode).isShownOneTouchEmojiList();
                    } else if (keyboardmode instanceof DefaultSoftKeyboardHangul) {
                        bEmojiShown = ((DefaultSoftKeyboardHangul)keyboardmode).isShownOneTouchEmojiList();
                    }
                }

                Context context = getContext();
                Resources res = context.getResources();
                SharedPreferences pref =
                        PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
                boolean mEnableOneHanded =
                        pref.getBoolean("one_handed", res.getBoolean(R.bool.one_handed_default_value));
                boolean mEnableSplit =
                        pref.getBoolean("split_mode", res.getBoolean(R.bool.split_mode_default_value));

                if (mPopupKeyboardId == R.xml.keyboard_functionkey_preview_ko) {
                    if (!mEnableVoiceInput) {
                        if (res.getConfiguration().orientation
                                == Configuration.ORIENTATION_PORTRAIT) {
                            if (!mEnableOneHanded) {
                                mPopupKeyboardId = R.xml.keyboard_functionkey_preview_ko_unable_voice_keyboard;
                            } else if (mNormalShown && mOneHandedShown) {
                                mPopupKeyboardId =
                                        R.xml.keyboard_functionkey_preview_ko_unable_voice_onehand;
                            } else {
                                mPopupKeyboardId = bEmojiShown == true ?
                                        R.xml.keyboard_functionkey_preview_ko_unable_voice_input_emoji_on :
                                        R.xml.keyboard_functionkey_preview_ko_unable_voice_input;
                            }
                        } else {
                            if (!mEnableSplit) {
                                mPopupKeyboardId = R.xml.keyboard_functionkey_preview_ko_unable_voice_keyboard;
                            } else if (mNormalShown && mSplitShown) {
                                mPopupKeyboardId =
                                        R.xml.keyboard_functionkey_preview_ko_unable_voice_split;
                            } else {
                                mPopupKeyboardId =
                                        R.xml.keyboard_functionkey_preview_ko_unable_voice;
                            }
                        }
                    } else {
                        if (res.getConfiguration().orientation
                                == Configuration.ORIENTATION_PORTRAIT) {
                            if (!mEnableOneHanded) {
                                mPopupKeyboardId =
                                        R.xml.keyboard_functionkey_preview_ko_unable_keyboard;
                            } else if (mNormalShown && mOneHandedShown) {
                                mPopupKeyboardId = R.xml.keyboard_functionkey_preview_ko_onehand;
                            } else {
                                mPopupKeyboardId = bEmojiShown == true ?
                                        R.xml.keyboard_functionkey_preview_ko_emoji_on :
                                        R.xml.keyboard_functionkey_preview_ko;
                            }
                        } else {
                            if (!mEnableSplit) {
                                mPopupKeyboardId =
                                        R.xml.keyboard_functionkey_preview_ko_unable_keyboard;
                            } else if (mNormalShown && mSplitShown) {
                                mPopupKeyboardId = R.xml.keyboard_functionkey_preview_ko_split;
                            } else {
                                mPopupKeyboardId = R.xml.keyboard_functionkey_preview_ko;
                            }
                        }
                    }
                }

                if (mPopupKeyboardId == R.xml.keyboard_functionkey_preview) {
                    if (!mEnableVoiceInput) {
                        if (mKeyboardType == KEYBOARD_QWERTY) {
                            if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                                if (!mEnableOneHanded) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_voice_qwerty;
                                } else if (mNormalShown && mOneHandedShown) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_voice_qwerty_onehand;
                                } else {
                                    mPopupKeyboardId = bEmojiShown == true ?
                                            R.xml.keyboard_functionkey_preview_unable_voice_input_qwerty_emoji_on :
                                            R.xml.keyboard_functionkey_preview_unable_voice_input_qwerty;
                                }
                            } else {
                                if (!mEnableSplit) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_voice_qwerty;
                                } else if (mNormalShown && mSplitShown) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_voice_qwerty_split;
                                } else {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_voice_input_qwerty;
                                }
                            }
                        } else {
                            if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                                if (!mEnableOneHanded) {
                                    mPopupKeyboardId =
                                            R.xml.keyboard_functionkey_preview_unable_voice_12key;
                                } else if(mNormalShown && mOneHandedShown) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_voice_onehand;
                                } else {
                                    mPopupKeyboardId = bEmojiShown == true ?
                                            R.xml.keyboard_functionkey_preview_unable_voice_input_emoji_on :
                                            R.xml.keyboard_functionkey_preview_unable_voice_input;
                                }
                            } else {
                                if (!mEnableSplit) {
                                    mPopupKeyboardId =
                                            R.xml.keyboard_functionkey_preview_unable_voice_12key;
                                } else if (mNormalShown && mSplitShown) {
                                    mPopupKeyboardId =
                                            R.xml.keyboard_functionkey_preview_unable_voice_split;
                                } else {
                                    mPopupKeyboardId =
                                            R.xml.keyboard_functionkey_preview_unable_voice;
                                }
                            }
                        }
                    } else {
                        if (mKeyboardType == KEYBOARD_QWERTY) {
                            if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                                if (!mEnableOneHanded) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_qwerty;
                                } else if (mNormalShown && mOneHandedShown) {
                                    mPopupKeyboardId =
                                            R.xml.keyboard_functionkey_preview_qwerty_onehand;
                                } else {
                                    mPopupKeyboardId = bEmojiShown == true ?
                                            R.xml.keyboard_functionkey_preview_qwerty_emoji_on :
                                            R.xml.keyboard_functionkey_preview_qwerty;
                                }
                            } else {
                                if (!mEnableSplit) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_qwerty;
                                } else if (mNormalShown && mSplitShown) {
                                    mPopupKeyboardId =
                                            R.xml.keyboard_functionkey_preview_qwerty_split;
                                } else {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_qwerty;
                                }
                            }
                        } else {
                            if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                                if (!mEnableOneHanded) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_12key;
                                } else if (mNormalShown && mOneHandedShown) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_onehand;
                                } else {
                                    mPopupKeyboardId = bEmojiShown == true ?
                                            R.xml.keyboard_functionkey_preview_emoji_on :
                                            R.xml.keyboard_functionkey_preview;
                                }
                            } else {
                                if (!mEnableSplit) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_unable_12key;
                                } else if (mNormalShown && mSplitShown) {
                                    mPopupKeyboardId = R.xml.keyboard_functionkey_preview_split;
                                } else {
                                    mPopupKeyboardId =
                                            R.xml.keyboard_functionkey_preview;
                                }
                            }
                        }
                    }
                }

                if (popupKey.popupCharacters != null) {
                    keyboard = new Keyboard(getContext(), mPopupKeyboardId,
                            popupKey.popupCharacters, -1, getPaddingLeft() + getPaddingRight());
                } else {
                    keyboard = new Keyboard(getContext(), mPopupKeyboardId);
                }
                mMiniKeyboard.setKeyboard(keyboard);

                if (isFunctionResId(mPopupKeyboardId) == true) {

                    mMiniKeyboard.getKeyboard().getKeys().get(0).pressed = true;
                    invalidateKey(0);
                }

                mMiniKeyboard.setPopupParent(this);
                mMiniKeyboard.setPreviewEnabled(false);
                mMiniKeyboardContainer.measure(
                        MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

                mMiniKeyboardCache.put(popupKey, mMiniKeyboardContainer);
            } else {
                mMiniKeyboard = (KeyboardView) mMiniKeyboardContainer.findViewById(R.id.keyboardView);
            }

            mWindowOffset = new int[2];
            getLocationInWindow(mWindowOffset);

            mPopupX = popupKey.x + getPaddingLeft();
            mPopupY = popupKey.y + getPaddingTop();
            mPopupX = mPopupX + popupKey.width - mMiniKeyboardContainer.getMeasuredWidth();
            mPopupY = mPopupY - mMiniKeyboardContainer.getMeasuredHeight();
            final int x = mPopupX + mMiniKeyboardContainer.getPaddingRight() + mWindowOffset[0];
            final int y = mPopupY + mMiniKeyboardContainer.getPaddingBottom() + mWindowOffset[1] -15;

            mMiniKeyboard.setPopupOffset(x < 0 ? 0 : x, y);
            mMiniKeyboard.setShifted(isShifted());
            mPopupKeyboard.setContentView(mMiniKeyboardContainer);

            int popup_x = 0;
            int popup_y = 0;
            int popup_width = 0;
            int popup_height = 0;
            int keynum = 1;
            int popup_offset_y = r.getDimensionPixelSize(R.dimen.popup_offset_y);
            if (popupKey.codes[0] == KEYCODE_COM_EMAIL) {
                popup_width = r.getDimensionPixelSize(R.dimen.popup_mail_width);
                popup_height = r.getDimensionPixelSize(R.dimen.popup_mail_height);
                popup_x = (int) getX() + popupKey.x
                        - r.getDimensionPixelSize(R.dimen.popup_mail_width)
                        + popupKey.width
                        + r.getDimensionPixelSize(R.dimen.popup_url_mail_x_gap);
                if (mKeyboardType != KEYBOARD_QWERTY) {
                    keynum = 2;
                }
            } else if (popupKey.codes[0] == KEYCODE_COM_URL) {
                popup_width = r.getDimensionPixelSize(R.dimen.popup_url_width);
                popup_height = r.getDimensionPixelSize(R.dimen.popup_url_height);
                popup_x = (int) getX() + popupKey.x
                        - r.getDimensionPixelSize(R.dimen.popup_url_width)
                        + popupKey.width
                        + r.getDimensionPixelSize(R.dimen.popup_url_mail_x_gap);
                if (mKeyboardType != KEYBOARD_QWERTY) {
                    keynum = 2;
                }
            } else if (isPeriodPopupWindow(mPopupKeyboardId)) {
                popup_width = r.getDimensionPixelSize(R.dimen.popup_period_width);
                popup_height = r.getDimensionPixelSize(R.dimen.popup_period_height);
                popup_x = (int) getX() + popupKey.x
                        - r.getDimensionPixelSize(R.dimen.popup_period_left_x_gap);
            } else if (popupKey.codes[0] == KEYCODE_FUNCTION_POPUP){
                mFunctionPopupOnScreen = true;

                Context context = getContext();
                Resources res = context.getResources();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
                boolean mEnableOneHanded = pref.getBoolean("one_handed", res.getBoolean(R.bool.one_handed_default_value));
                boolean mEnableSplit = pref.getBoolean("split_mode", res.getBoolean(R.bool.split_mode_default_value));

                boolean bEmojiShown  = false;
                if(OpenWnn.getCurrentIme() != null) {
                    DefaultSoftKeyboard kb = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
                    if (kb instanceof DefaultSoftKeyboardJAJP) {
                        bEmojiShown = ((DefaultSoftKeyboardJAJP)kb).isShownOneTouchEmojiList();
                    } else if (kb instanceof DefaultSoftKeyboardHangul) {
                        bEmojiShown = ((DefaultSoftKeyboardHangul)kb).isShownOneTouchEmojiList();
                    }
                }

                if (iWnnEngine.getEngine().getLanguage() == KOREAN) {
                    if(mEnableVoiceInput){
                        if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                            if (!mEnableOneHanded || bEmojiShown) {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_ko_width);
                            } else {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_ko_width);
                            }
                        } else if (!mEnableSplit) {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_ko_width);
                        } else {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_ko_width);
                        }
                    } else {
                        if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                            if (!mEnableOneHanded || bEmojiShown) {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_ko_width_voiceoff);
                            } else {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_ko_width_voiceoff);
                            }
                        } else if (!mEnableSplit) {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_ko_width_voiceoff);
                        } else {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_ko_width_voiceoff);
                        }
                    }
                } else {
                    if(mEnableVoiceInput){
                        if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                            if (!mEnableOneHanded || bEmojiShown) {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_width);
                            } else {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_width);
                            }
                        } else if (!mEnableSplit) {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_width);
                        } else {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_width);
                        }
                    } else {
                        if (res.getConfiguration().orientation
                                    == Configuration.ORIENTATION_PORTRAIT) {
                            if (!mEnableOneHanded || bEmojiShown) {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_width_voiceoff);
                            } else {
                                popup_width = r.getDimensionPixelSize(R.dimen.popup_function_width_voiceoff);
                            }
                        } else if (!mEnableSplit) {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_noicon_width_voiceoff);
                        } else {
                            popup_width = r.getDimensionPixelSize(R.dimen.popup_function_width_voiceoff);
                        }
                    }
                }

                popup_height = r.getDimensionPixelSize(R.dimen.popup_function_height);
                popup_x = (int) getX() + popupKey.x
                        + getPaddingLeft()
                        + r.getDimensionPixelSize(R.dimen.popup_function_left_x_gap);
                if (mKeyboardType != KEYBOARD_QWERTY) {
                    keynum = 4;
                }
            } else if (popupKey.codes[0] == KEYCODE_CHANGE_LANGUAGE_POPUP
                    || popupKey.codes[0] == KEYCODE_CHANGE_LANGUAGE_POPUP_KO) {
                mChangeLangDialogOnScreen = true;
                popup_width = r.getDimensionPixelSize(R.dimen.popup_change_language_width);
                if (mPopupKeyboardId == R.xml.popup_changelanguage_unable_ko) {
                    popup_height = r.getDimensionPixelSize(R.dimen.popup_change_language_height_unable_ko);
                } else {
                    popup_height = r.getDimensionPixelSize(R.dimen.popup_change_language_height);
                }
                popup_x = (int) getX() + popupKey.x
                        + getPaddingLeft()
                        + r.getDimensionPixelSize(R.dimen.popup_function_left_x_gap);
            } else if (popupKey.keyIconId >= KEY_ID_QWERTY_NUM_SYMBOL_Q
                    && popupKey.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_M) {
                if (popupKey.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_P) {
                    keynum = 4;
                } else if (popupKey.keyIconId > KEY_ID_QWERTY_NUM_SYMBOL_P
                        && popupKey.keyIconId < KEY_ID_QWERTY_NUM_SYMBOL_SLASH) {
                    keynum = 3;
                } else if (popupKey.keyIconId > KEY_ID_QWERTY_NUM_SYMBOL_SLASH
                        && popupKey.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_M) {
                    keynum =2;
                }
                popup_width = r.getDimensionPixelSize(R.dimen.popup_num_symbol_width);
                popup_height = r.getDimensionPixelSize(R.dimen.popup_num_symbol_height);

                int popup_rightEdge_x = (int) getX() + popupKey.x + popup_width;
                int keyboard_rightEdge_x = (int) getX() + getWidth();
                if (popup_rightEdge_x > keyboard_rightEdge_x) {
                    popup_x = (int) getX() + getWidth() - popup_width;
                } else {
                    popup_x = (int) getX() + popupKey.x + getPaddingLeft()
                            + r.getDimensionPixelSize(R.dimen.popup_function_left_x_gap);
                }
            } else {
                popup_width = mMiniKeyboardContainer.getMeasuredWidth();
                popup_height = mMiniKeyboardContainer.getMeasuredHeight();
                popup_x = x;
            }
            mPopupKeyboard.setWidth(popup_width);
            mPopupKeyboard.setHeight(popup_height);
            popup_y = getPaddingBottom()
                    + popupKey.height * keynum
                    + popupKey.vgap * (keynum -1)
                    + r.getDimensionPixelSize(R.dimen.popup_period_bottom_y_gap);
            mPopupKeyboard.showAtLocation(this, Gravity.LEFT | Gravity.BOTTOM,
                    popup_x, popup_y);

            mMiniKeyboardOnScreen = true;
            //mMiniKeyboard.onTouchEvent(getTranslatedEvent(me));
            long eventTime = SystemClock.uptimeMillis();
            mMiniKeyboardPopupTime = eventTime;

            if(popupKey.codes[0] != KEYCODE_CHANGE_LANGUAGE_POPUP
                && popupKey.codes[0] != KEYCODE_CHANGE_LANGUAGE_POPUP_KO){
                    if( popupKey.codes[0] != KEYCODE_FUNCTION_POPUP){
                        MotionEvent downEvent;
                        int symbol_width = r.getDimensionPixelSize(R.dimen.popup_view_symbol_text_width);

                        if (isPeriodPopupWindow(mPopupKeyboardId)) {
                           List<Key> keys = mMiniKeyboard.getKeyboard().getKeys();
                           downEvent = MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, MotionEvent.ACTION_DOWN,
                                    (keys.get(keys.size() -1).x + symbol_width / 2),
                                    popup_height + popup_offset_y  - (popupKey.height / 2), 0);
                           Message msg = mHandler.obtainMessage(MSG_REMOVE_PERIODPOPUP);
                           mHandler.sendMessageDelayed(msg, PERIOD_KEY_TIMEOUT);
                        } else if (popupKey.keyIconId >= KEY_ID_QWERTY_NUM_SYMBOL_Q
                                && popupKey.keyIconId <= KEY_ID_QWERTY_NUM_SYMBOL_M) {
                           downEvent = MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, MotionEvent.ACTION_DOWN,
                                    (popup_view_padding_left + symbol_width / 2),
                                    popup_height + popup_offset_y  - (popupKey.height / 2), 0);
                        } else {
                           downEvent = MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, MotionEvent.ACTION_DOWN,
                                    popupKey.x - popup_x + (popupKey.width / 2),
                                    popup_height + popup_offset_y - (popupKey.height / 2), 0);
                        }
                        mMiniKeyboard.onTouchEvent(downEvent);
                        downEvent.recycle();
                    }else{
                        Message msg = mHandler.obtainMessage(MSG_REMOVE_FUNCTIONPOPUP);
                        mHandler.sendMessageDelayed(msg, FUNCTION_KEY_TIMEOUT);
                    }
            }else{
                    Message msg = mHandler.obtainMessage(MSG_REMOVE_LANGUAGEPOPUP);
                    mHandler.sendMessageDelayed(msg, LANGUAGE_DIALOG_TIMEOUT);
            }

            mMiniKeyboard.setPadding(r.getDimensionPixelSize(R.dimen.popup_padding_left),
                    r.getDimensionPixelSize(R.dimen.popup_padding_top), 0, 0);

            invalidateAllKeys();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (OpenWnn.getCurrentIme() == null) {
            return true;
        }
        // Convert multi-pointer up/down events to single up/down events to
        // deal with the typical multi-pointer behavior of two-thumb typing
        final int pointerCount = me.getPointerCount();
        final int action = me.getAction();
        boolean result = false;
        final long now = me.getEventTime();
        final boolean isPointerCountOne = (pointerCount == 1);

        if (mMiniKeyboard != null) {
            //except popup automatically turned off in 2seconds.
            if (isFunctionResId(mMiniKeyboard.getKeyboard().getXmlLayoutResId()) != true
                    && !isPeriodPopupWindow(mMiniKeyboard.getKeyboard().getXmlLayoutResId())) {
                final long eventTime = me.getEventTime();
                final int index = me.getActionIndex();
                final int id = me.getPointerId(index);
                final int miniKeyboardPointerIndex = me.findPointerIndex(id);
                if (miniKeyboardPointerIndex >= 0 && miniKeyboardPointerIndex < pointerCount) {
                    final int miniKeyboardX = (int)me.getX(miniKeyboardPointerIndex);
                    final int miniKeyboardY = (int)me.getY(miniKeyboardPointerIndex);
                    MotionEvent translated = generateMiniKeyboardMotionEvent(action,
                            miniKeyboardX, miniKeyboardY, eventTime);
                    mMiniKeyboard.onTouchEvent(translated);
                    translated.recycle();
                }
                return true;
            }
        }

        if (pointerCount != mOldPointerCount) {
            if (isPointerCountOne) {
                // Send a down event for the latest pointer
                MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN,
                        me.getX(), me.getY(), me.getMetaState());
                result = onModifiedTouchEvent(down, false);
                down.recycle();
                // If it's an up action, then deliver the up as well.
                if (action == MotionEvent.ACTION_UP) {
                    result = onModifiedTouchEvent(me, true);
                }
            } else {
                // Send an up event for the last pointer
                MotionEvent up = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP,
                        mOldPointerX, mOldPointerY, me.getMetaState());
                result = onModifiedTouchEvent(up, true);
                up.recycle();
            }
        } else {
            if (isPointerCountOne) {
                result = onModifiedTouchEvent(me, false);
                mOldPointerX = me.getX();
                mOldPointerY = me.getY();
            } else {
                // Don't do anything when 2 pointers are down and moving.
                result = true;
            }
        }
        mOldPointerCount = pointerCount;

        return result;
    }

    private MotionEvent generateMiniKeyboardMotionEvent(int action, int x, int y, long eventTime) {
        int[] windowLocation = new int[2];
        getLocationOnScreen(windowLocation);
        int[] miniWindowLocation = new int[2];
        mMiniKeyboard.getLocationOnScreen(miniWindowLocation);
        x = x + windowLocation[0] - miniWindowLocation[0];
        y = y + windowLocation[1] - miniWindowLocation[1];
        y = Math.min(y, mMiniKeyboard.getHeight());
        return MotionEvent.obtain(mMiniKeyboardPopupTime, eventTime, action, x, y, 0);
    }

    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        int touchX = (int) me.getX() - getPaddingLeft();
        int touchY = (int) me.getY() + mVerticalCorrection - getPaddingTop();
        final int action = me.getAction();
        final long eventTime = me.getEventTime();
        int keyIndex = getKeyIndices(touchX, touchY, null);
        mPossiblePoly = possiblePoly;

        // Track the last few movements to look for spurious swipes.
        if (action == MotionEvent.ACTION_DOWN) mSwipeTracker.clear();
        mSwipeTracker.addMovement(me);

        // Ignore all motion events until a DOWN.
        if (mAbortKey
                && action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL) {
            return true;
        }

        if (mGestureDetector.onTouchEvent(me)) {
            if ((mKeyboard.getXmlLayoutResId() != R.xml.popup_url)&&
                (mKeyboard.getXmlLayoutResId() != R.xml.popup_mail)&&
                (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_en)&&
                (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_en_full)&&
                (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_ja)&&
                (mKeyboard.getXmlLayoutResId() != R.xml.popup_period_ja_full)&&
                (mKeyboard.getXmlLayoutResId() != R.xml.keyboard_popup_num_symbol_template)) {
                showPreview(NOT_A_KEY);
                mHandler.removeMessages(MSG_REPEAT);
                mHandler.removeMessages(MSG_LONGPRESS);
                return true;
            }
        }

        if(mMiniKeyboardOnScreen){
            dismissPopupKeyboard();
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mAbortKey = false;
                mStartX = touchX;
                mStartY = touchY;
                mLastCodeX = touchX;
                mLastCodeY = touchY;
                mLastKeyTime = 0;
                mCurrentKeyTime = 0;
                mLastKey = NOT_A_KEY;
                mCurrentKey = keyIndex;
                mDownKey = keyIndex;
                mDownTime = me.getEventTime();
                mLastMoveTime = mDownTime;
                checkMultiTap(eventTime, keyIndex);
                if (mCurrentKey >= 0 && mKeys[mCurrentKey].repeatable) {
                    mRepeatKeyIndex = mCurrentKey;
                    Message msg = mHandler.obtainMessage(MSG_REPEAT);
                    mHandler.sendMessageDelayed(msg, REPEAT_START_DELAY);
                    repeatKey();
                    // Delivering the key could have caused an abort
                    if (mAbortKey) {
                        mRepeatKeyIndex = NOT_A_KEY;
                        break;
                    }
                } else {
                    mRepeatKeyIndex = NOT_A_KEY;
                }
                mHandler.removeMessages(MSG_LONGPRESS);
                if (mCurrentKey != NOT_A_KEY) {
                    Message msg = mHandler.obtainMessage(MSG_LONGPRESS, me);
                    mHandler.sendMessageDelayed(msg, LONGPRESS_TIMEOUT);
                }
                showPreview(keyIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                boolean continueLongPress = false;
                if (keyIndex != NOT_A_KEY) {
                    if (mCurrentKey == NOT_A_KEY) {
                        mCurrentKey = keyIndex;
                        mCurrentKeyTime = eventTime - mDownTime;
                    } else {
                        if (keyIndex == mCurrentKey) {
                            mCurrentKeyTime += eventTime - mLastMoveTime;
                            continueLongPress = true;
                        } else if (mRepeatKeyIndex == NOT_A_KEY) {
                            resetMultiTap();
                            mLastKey = mCurrentKey;
                            mLastCodeX = mLastX;
                            mLastCodeY = mLastY;
                            mLastKeyTime =
                                    mCurrentKeyTime + eventTime - mLastMoveTime;
                            mCurrentKey = keyIndex;
                            mCurrentKeyTime = 0;
                        }
                    }
                }
                if (!continueLongPress) {
                    // Cancel old longpress
                    mHandler.removeMessages(MSG_LONGPRESS);
                    // Start new longpress if key has changed
                    if (mCurrentKey != NOT_A_KEY) {
                        Message msg = mHandler.obtainMessage(MSG_LONGPRESS, me);
                        mHandler.sendMessageDelayed(msg, LONGPRESS_TIMEOUT);
                    }
                }
                showPreview(mCurrentKey);
                mLastMoveTime = eventTime;
                break;

            case MotionEvent.ACTION_UP:
                removeMessages();
                if (keyIndex == mCurrentKey) {
                    mCurrentKeyTime += eventTime - mLastMoveTime;
                } else {
                    resetMultiTap();
                    mLastKey = mCurrentKey;
                    mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime;
                    mCurrentKey = keyIndex;
                    mCurrentKeyTime = 0;
                }
                if (mCurrentKeyTime < mLastKeyTime && mCurrentKeyTime < DEBOUNCE_TIME
                        && mLastKey != NOT_A_KEY) {
                    mCurrentKey = mLastKey;
                    touchX = mLastCodeX;
                    touchY = mLastCodeY;
                }
                showPreview(NOT_A_KEY);
                Arrays.fill(mKeyIndices, NOT_A_KEY);
                // If we're not on a repeating key (which sends on a DOWN event)
                if (mRepeatKeyIndex == NOT_A_KEY && !mMiniKeyboardOnScreen && !mAbortKey) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime);
                }
                invalidateKey(keyIndex);
                mRepeatKeyIndex = NOT_A_KEY;
                break;
            case MotionEvent.ACTION_CANCEL:
                removeMessages();
                dismissPopupKeyboard();
                mAbortKey = true;
                showPreview(NOT_A_KEY);
                invalidateKey(mCurrentKey);
                break;
            default:
                break;
        }
        mLastX = touchX;
        mLastY = touchY;
        return true;
    }

    private boolean repeatKey() {
        boolean bValue = false;
        if (mRepeatKeyIndex != NOT_A_KEY && mRepeatKeyIndex < mKeys.length) {
            Key key = mKeys[mRepeatKeyIndex];
            detectAndSendKey(mCurrentKey, key.x, key.y, mLastTapTime);
            bValue = true;
        }
        return bValue;
    }

    protected void swipeRight() {
        mKeyboardActionListener.swipeRight();
    }

    protected void swipeLeft() {
        mKeyboardActionListener.swipeLeft();
    }

    protected void swipeUp() {
        mKeyboardActionListener.swipeUp();
    }

    protected void swipeDown() {
        mKeyboardActionListener.swipeDown();
    }

    public void closing() {
        if (mPreviewPopup.isShowing()) {
            mPreviewPopup.dismiss();
        }
        removeMessages();

        dismissPopupKeyboard();
        mBuffer = null;
        mCanvas = null;
        mMiniKeyboardCache.clear();
    }

    private void removeMessages() {
        mHandler.removeMessages(MSG_REPEAT);
        mHandler.removeMessages(MSG_LONGPRESS);
        mHandler.removeMessages(MSG_SHOW_PREVIEW);
        mHandler.removeMessages(MSG_REMOVE_FUNCTIONPOPUP);
        mHandler.removeMessages(MSG_REMOVE_LANGUAGEPOPUP);
        mHandler.removeMessages(MSG_REMOVE_PERIODPOPUP);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    public void dismissPopupKeyboard() {
        if (mPopupKeyboard.isShowing()) {
            mPopupKeyboard.dismiss();
            mMiniKeyboard = null;
            mMiniKeyboardOnScreen = false;
            mChangeLangDialogOnScreen = false;
            mFunctionPopupOnScreen = false;
            invalidateAllKeys();
            mHandler.removeMessages(MSG_REMOVE_FUNCTIONPOPUP);
            mHandler.removeMessages(MSG_REMOVE_LANGUAGEPOPUP);
            mHandler.removeMessages(MSG_REMOVE_PERIODPOPUP);
        }
    }

    public boolean isMiniKeyboardOnScreen() {
            return mMiniKeyboardOnScreen;
    }

    public boolean handleBack() {
        if (mPopupKeyboard.isShowing()) {
            dismissPopupKeyboard();
            return true;
        }
        return false;
    }

    private void resetMultiTap() {
        mLastSentIndex = NOT_A_KEY;
        mTapCount = 0;
        mLastTapTime = -1;
        mInMultiTap = false;
    }

    private void checkMultiTap(long eventTime, int keyIndex) {
        if (keyIndex == NOT_A_KEY) return;
        Key key = mKeys[keyIndex];
        if (key.codes.length > 1) {
            mInMultiTap = true;
            if (eventTime < mLastTapTime + MULTITAP_INTERVAL
                    && keyIndex == mLastSentIndex) {
                mTapCount = (mTapCount + 1) % key.codes.length;
                return;
            } else {
                mTapCount = -1;
                return;
            }
        }
        if (eventTime > mLastTapTime + MULTITAP_INTERVAL || keyIndex != mLastSentIndex) {
            resetMultiTap();
        }
    }

    private static class SwipeTracker {

        static final int NUM_PAST = 4;
        static final int LONGEST_PAST_TIME = 200;

        final float mPastX[] = new float[NUM_PAST];
        final float mPastY[] = new float[NUM_PAST];
        final long mPastTime[] = new long[NUM_PAST];

        float mYVelocity;
        float mXVelocity;

        public void clear() {
            mPastTime[0] = 0;
        }

        public void addMovement(MotionEvent ev) {
            long time = ev.getEventTime();
            final int N = ev.getHistorySize();
            for (int i=0; i<N; i++) {
                addPoint(ev.getHistoricalX(i), ev.getHistoricalY(i),
                        ev.getHistoricalEventTime(i));
            }
            addPoint(ev.getX(), ev.getY(), time);
        }

        private void addPoint(float x, float y, long time) {
            int drop = -1;
            int i;
            final long[] pastTime = mPastTime;
            for (i=0; i<NUM_PAST; i++) {
                if (pastTime[i] == 0) {
                    break;
                } else if (pastTime[i] < time-LONGEST_PAST_TIME) {
                    drop = i;
                }
            }
            if (i == NUM_PAST && drop < 0) {
                drop = 0;
            }
            if (drop == i) drop--;
            final float[] pastX = mPastX;
            final float[] pastY = mPastY;
            if (drop >= 0) {
                final int start = drop+1;
                final int count = NUM_PAST-drop-1;
                System.arraycopy(pastX, start, pastX, 0, count);
                System.arraycopy(pastY, start, pastY, 0, count);
                System.arraycopy(pastTime, start, pastTime, 0, count);
                i -= (drop+1);
            }
            pastX[i] = x;
            pastY[i] = y;
            pastTime[i] = time;
            i++;
            if (i < NUM_PAST) {
                pastTime[i] = 0;
            }
        }

        public void computeCurrentVelocity(int units) {
            computeCurrentVelocity(units, Float.MAX_VALUE);
        }

        public void computeCurrentVelocity(int units, float maxVelocity) {
            final float[] pastX = mPastX;
            final float[] pastY = mPastY;
            final long[] pastTime = mPastTime;

            final float oldestX = pastX[0];
            final float oldestY = pastY[0];
            final long oldestTime = pastTime[0];
            float accumX = 0;
            float accumY = 0;
            int N=0;
            while (N < NUM_PAST) {
                if (pastTime[N] == 0) {
                    break;
                }
                N++;
            }

            for (int i=1; i < N; i++) {
                final int dur = (int)(pastTime[i] - oldestTime);
                if (dur == 0) continue;
                float dist = pastX[i] - oldestX;
                float vel = (dist/dur) * units;   // pixels/frame.
                if (accumX == 0) accumX = vel;
                else accumX = (accumX + vel) * .5f;

                dist = pastY[i] - oldestY;
                vel = (dist/dur) * units;   // pixels/frame.
                if (accumY == 0) accumY = vel;
                else accumY = (accumY + vel) * .5f;
            }
            mXVelocity = accumX < 0.0f ? Math.max(accumX, -maxVelocity)
                    : Math.min(accumX, maxVelocity);
            mYVelocity = accumY < 0.0f ? Math.max(accumY, -maxVelocity)
                    : Math.min(accumY, maxVelocity);
        }

        public float getXVelocity() {
            return mXVelocity;
        }

        public float getYVelocity() {
            return mYVelocity;
        }
    }

    /**
     * Set enable voice input.
     *
     * @param enableVoiceInput  enable voice input.
     */
    public void setVoiceInput(boolean enableVoiceInput) {
        mEnableVoiceInput = enableVoiceInput;
    }

    /**
     * Check whether function is enable.
     * @param functionIndex Index of function to be enabled.
     * @return true enable
     *         false disable
     */
    public boolean isFunctionEnable(int functionIndex) {
        boolean enable = false;

        switch (functionIndex) {
        case FunctionKey_FUNC_VOICEINPUT:
            enable = mEnableVoiceInput;
            break;
        default:
            enable = false;
            break;
        }

        return enable;
    }

     /**
     * get ChangeLangDialog ONOFF.
     */
    public boolean getChangeLangDialogOnScreen(){
        return mChangeLangDialogOnScreen;
    }

     /**
     * set ChangeLangDialog ONOFF.
     */
    public void setChangeLangDialogOnScreen(boolean onoff){
        mChangeLangDialogOnScreen = onoff;
        return;
    }
     /**
     * get ChangeLangDialog ONOFF.
     */
    public boolean getFunctionPopupOnScreen(){
        return mFunctionPopupOnScreen;
    }

     /**
     * set ChangeLangDialog ONOFF.
     */
    public void setFunctionPopupOnScreen(boolean onoff){
        mFunctionPopupOnScreen = onoff;
        return;
    }

    /**
     * Clear window info.
     */
    public void clearWindowInfo() {
        mOffsetInWindow = null;
    }

    private static final Rect sTextBounds = new Rect();

    private static int getCharGeometryCacheKey(char reference, Paint paint) {
        final int labelSize = (int)paint.getTextSize();
        final Typeface face = paint.getTypeface();
        final int codePointOffset = reference << 15;
        if (face == Typeface.DEFAULT) {
            return codePointOffset + labelSize;
        } else if (face == Typeface.DEFAULT_BOLD) {
            return codePointOffset + labelSize + 0x1000;
        } else if (face == Typeface.MONOSPACE) {
            return codePointOffset + labelSize + 0x2000;
        } else {
            return codePointOffset + labelSize;
        }
    }

    private static float getCharWidth(char[] character, Paint paint) {
        final Integer key = getCharGeometryCacheKey(character[0], paint);
        final Float cachedValue = sTextWidthCache.get(key);
        if (cachedValue != null)
            return cachedValue;

        paint.getTextBounds(character, 0, 1, sTextBounds);
        final float width = sTextBounds.width();
        sTextWidthCache.put(key, width);
        return width;
    }

    // Read fraction value in TypedArray as float.
    private static float getRatio(TypedArray a, int index) {
        return a.getFraction(index, 1000, 1000, 1) / 1000.0f;
    }

    /**
     * Enable mushroom.
     *
     * @param enableMushroom  enable mushroom ({@code ture}) or disable ({@code false}).
     */
    public void setEnableMushroom(boolean enableMushroom) {
        mEnableMushroom = enableMushroom;
    }

    /**
     * Dismiss key preview.
     *
     */
    public void dismissKeyPreview() {
        if (mPreviewPopup.isShowing()) {
            mPreviewText.setVisibility(INVISIBLE);
        }
    }

    /**
     * Called when attaches a keyboard to this view.
     *
     * @param keyboard The keyboard.
     */
    public void setKeyboard(Keyboard keyboard, int keyboardType) {
        mKeyboardType = keyboardType;
        setKeyboard(keyboard);
    }

    public void setKeyPreviewBackground(int keyPreviewIndex, TextView previewText){

        if (keyPreviewIndex < 0) {
            return;
        }

        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        boolean setIsKeyskin = keyskin.isValid();
        if (setIsKeyskin) {
            previewText.setBackgroundDrawable(
                keyskin.getDrawable(KEYPREVIEW_BACK_DRAWABLE[keyPreviewIndex]));
        } else {
            previewText.setBackgroundDrawable(getContext().getResources().getDrawable(
                KEYPREVIEW_BACK_DRAWABLE[keyPreviewIndex]));
        }

        return;
    }

    /**
     * whether period popup window.
     *
     * @param popupId
     *            The xml resource ID
     * @return true if the popup is period popup window, false if the popup is
     *         not period popup window
     */
    private boolean isPeriodPopupWindow(int popupId) {
        return (popupId == R.xml.popup_period_en_full
                || popupId == R.xml.popup_period_en
                || popupId == R.xml.popup_period_ja_full
                || popupId == R.xml.popup_period_ja);
    }

    /**
     * Gets the touch x-coordinate of the point on the keyboard.
     *
     * @param x The x-coordinate of the point.
     * @return The touch x-coordinate of the point on the keyboard.
     */
    public int getTouchKeyboardX(int x) {
        return x - getPaddingLeft();
    }

    /**
     * Gets the touch y-coordinate of the point on the keyboard.
     *
     * @param y The y-coordinate of the point.
     * @return The touch y-coordinate of the point on the keyboard.
     */
    public int getTouchKeyboardY(int y) {
        return y + mVerticalCorrection - getPaddingTop();
    }
}
