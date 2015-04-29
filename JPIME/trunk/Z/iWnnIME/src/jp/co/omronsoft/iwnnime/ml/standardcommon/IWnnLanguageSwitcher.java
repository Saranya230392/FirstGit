/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml.standardcommon;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.inputmethodservice.InputMethodService;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;
import android.util.Log;

import jp.co.omronsoft.iwnnime.ml.CandidatesViewManager;
import jp.co.omronsoft.iwnnime.ml.ComposingText;
import jp.co.omronsoft.iwnnime.ml.ControlPanelStandard;
import jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard;
import jp.co.omronsoft.iwnnime.ml.IWnnImeBase;
import jp.co.omronsoft.iwnnime.ml.InputMethodBase;
import jp.co.omronsoft.iwnnime.ml.InputMethodSwitcher;
import jp.co.omronsoft.iwnnime.ml.InputViewManager;
import jp.co.omronsoft.iwnnime.ml.KeyboardSkinData;
import jp.co.omronsoft.iwnnime.ml.KeyboardLanguagePackData;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;
import jp.co.omronsoft.iwnnime.ml.OpenWnnEvent;
//import jp.co.omronsoft.iwnnime.ml.cyrillic.ru.IWnnImeRussian;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul;
import jp.co.omronsoft.iwnnime.ml.hangul.ko.IWnnImeKorean;
//import jp.co.omronsoft.iwnnime.ml.latin.cs.IWnnImeCzech;
//import jp.co.omronsoft.iwnnime.ml.latin.de.IWnnImeGerman;
//import jp.co.omronsoft.iwnnime.ml.latin.en.uk.IWnnImeEnUk;
//import jp.co.omronsoft.iwnnime.ml.latin.en.us.IWnnImeEnUs;
//import jp.co.omronsoft.iwnnime.ml.latin.es.IWnnImeSpanish;
//import jp.co.omronsoft.iwnnime.ml.latin.fr.IWnnImeFrench;
//import jp.co.omronsoft.iwnnime.ml.latin.fr.ca.IWnnImeFrCa;
//import jp.co.omronsoft.iwnnime.ml.latin.it.IWnnImeItalian;
//import jp.co.omronsoft.iwnnime.ml.latin.nb.IWnnImeNorwegian;
//import jp.co.omronsoft.iwnnime.ml.latin.nl.IWnnImeDutch;
//import jp.co.omronsoft.iwnnime.ml.latin.pl.IWnnImePolish;
//import jp.co.omronsoft.iwnnime.ml.latin.pt.IWnnImePortuguese;
//import jp.co.omronsoft.iwnnime.ml.latin.sv.IWnnImeSwedish;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import jp.co.omronsoft.iwnnime.ml.jajp.IWnnImeJaJp;
import jp.co.omronsoft.iwnnime.ml.R;
import jp.co.omronsoft.iwnnime.ml.SoundManager;
import jp.co.omronsoft.iwnnime.ml.UserDictionaryToolsList;
//import jp.co.omronsoft.iwnnime.ml.zh.cn.pinyin.IWnnImeZhCnP;
//import jp.co.omronsoft.iwnnime.ml.zh.tw.bopomofo.IWnnImeChineseTw;

import java.util.List;
import java.util.Locale;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * The iWnn Language Switching class. This class is bridge between IMF and IME body.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class IWnnLanguageSwitcher extends OpenWnn {
    /** for debug */
    private static final boolean DEBUG = false;
    /** for debug */
    private static final String TAG = "OpenWnn";
    /** Selected locale (language) of IME */
    private Locale mSelectedLocale = null;
    /** Candidates view shown */
    private boolean mIsCandidatesViewShown = false;
    /** Initialize state */
    private boolean notInitialized = true;

    /** Locale of Czech */
    public static final Locale LOCALE_CS_CZ = new Locale("cs", "CZ");
    /** Locale of German(GERMAN) */
    public static final Locale LOCALE_DE    = Locale.GERMAN;
    /** Locale of English(United Kingdom) */
    public static final Locale LOCALE_EN_GB = Locale.UK;
    /** Locale of English(United States) */
    public static final Locale LOCALE_EN_US = Locale.US;
    /** Locale of Spanish */
    public static final Locale LOCALE_ES_ES = new Locale("es", "ES");
    /** Locale of French(France) */
    public static final Locale LOCALE_FR    = Locale.FRENCH;
    /** Locale of Italian(Italy) */
    public static final Locale LOCALE_IT    = Locale.ITALIAN;
    /** Locale of Japanese */
    public static final Locale LOCALE_JA    = Locale.JAPANESE;
    /** Locale of Japanese */
    private static final Locale LOCALE_JA_JP = Locale.JAPAN;
    /** Locale of Norwegian Bokmal */
    public static final Locale LOCALE_NB_NO = new Locale("nb", "NO");
    /** Locale of Dutch(Netherlands) */
    public static final Locale LOCALE_NL_NL = new Locale("nl", "NL");
    /** Locale of Polish */
    public static final Locale LOCALE_PL_PL = new Locale("pl", "PL");
    /** Locale of Russian */
    public static final Locale LOCALE_RU_RU = new Locale("ru", "RU");
    /** Locale of Swedish */
    public static final Locale LOCALE_SV_SE = new Locale("sv", "SE");
    /** Locale of Chinese(PRC) */
    public static final Locale LOCALE_ZH_CN = Locale.SIMPLIFIED_CHINESE;
    /** Locale of Chinese(TW) */
    public static final Locale LOCALE_ZH_TW = Locale.TRADITIONAL_CHINESE;
    /** Locale of Portuguese */
    public static final Locale LOCALE_PT_PT = new Locale("pt", "PT");
    /** Locale of French(Canada) */
    public static final Locale LOCALE_FR_CA = Locale.CANADA_FRENCH;
    /** Locale of French(Canada) */
    public static final Locale LOCALE_KO_KR = Locale.KOREA;

    public static final String DICTIONARY_VERSION_KEY = "dic_version";
    /** Caution!! Change the following value at the time of dictionary update */
    private static final String DICTIONARY_VERSION = "7.0";

    /** Current language of IME */
    private static String mCurrentLanguage = LOCALE_JA_JP.getLanguage();

    /** Hangul keep Flag For Handwriting to IME */
    private static boolean mHangulFlagForHw = false;

    /** Sound DeleteKey */
    private static final int SOUND_DELETE_KEY = 1;

    /** IME Sound change ReturnKey */
    private static final int SOUND_RETURN_KEY = 2;

    /** IME Sound change SpacebarKey */
    private static final int SOUND_SPACEBAR_KEY = 3;

    /** IME Sound change StandardKey */
    private static final int SOUND_STANDARD_KEY = 4;

    /** save old EditorInfo */
    private static EditorInfo mOldEditorInfo;

    /** Instance of InputMethodInfo */
    private InputMethodInfo mShortcutInputMethodInfo = null;
    /** Instance of InputMethodSubtype */
    private InputMethodSubtype mShortcutSubtype = null;
    /** Subtype of require network connectivity */
    private static final String SUBTYPE_EXTRAVALUE_REQUIRE_NETWORK_CONNECTIVITY = "requireNetworkConnectivity";
    /** Voice input is an disable privateImeOption **/
    private static final String PRIVATE_IME_OPTION_DISABLE_VOICE_INPUT = "com.google.android.inputmethod.latin.noMicrophoneKey";
    private static final String PRIVATE_IME_OPTION_DISABLE_VOICE_INPUT_COMPAT = "nm";

    /** Return from the symbol list to Hangul or not */
    private boolean mIsSymbolBackHangul = false;

    /** The language type of class for specified Locale HashMap. */
    public static final HashMap<String, String> LOCALE_CONVERT_ENGINELOCALE_TABLE = new HashMap<String, String>() {{
        put(LOCALE_EN_US.toString(), "en_us");    // English US
        put(LOCALE_EN_GB.toString(), "en_uk");    // English UK
        put(LOCALE_DE.toString(),    "de");       // German
        put(LOCALE_IT.toString(),    "it");       // Italy
        put(LOCALE_FR.toString(),    "fr");       // French
        put(LOCALE_ES_ES.toString(), "es");       // Spanish
        put(LOCALE_NL_NL.toString(), "nl");       // Dutch
        put(LOCALE_PL_PL.toString(), "pl");       // Polish
        put(LOCALE_RU_RU.toString(), "ru");       // Russian
        put(LOCALE_SV_SE.toString(), "sv");       // Swedish
        put(LOCALE_NB_NO.toString(), "nb");       // Norwegian
        put(LOCALE_CS_CZ.toString(), "cs");       // Czech
        put(LOCALE_ZH_CN.toString(), "zh_cn_p");  // Chinese (China)
        put(LOCALE_ZH_TW.toString(), "zh_tw_z");  // Chinese (Taiwan)
        put(LOCALE_JA.toString(),    "ja");       // Japanese
        put(LOCALE_PT_PT.toString(), "pt");       // Portuguese
        put(LOCALE_FR_CA.toString(), "fr_ca");    // French (Canada)
        put(LOCALE_KO_KR.toString(), "ko");       // Korean
    }};

    /** It is determined that a transition is made from Korean input mode. */
    private boolean mIsFromHangulIme = false;

    /**
     * Constructor
     */
    public IWnnLanguageSwitcher(InputMethodSwitcher ims) {
        super(ims);
    }

    /**
     * Called by the system when the service is first created.
     *
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#onCreate
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreate
     */
    @Override public void onCreate() {
        if (OpenWnn.PERFORMANCE_DEBUG) {Log.d(TAG, "IWnnLanguageSwitcher::onCreate()  Start");}

        updateTabletMode(OpenWnn.superGetContext());

        initialLanguage(OpenWnn.superGetContext());
        iWnnEngine engine = iWnnEngine.getEngine();
        String currentLocale = getSubtypeLocale(getCurrentInputMethodSubtype());
        int languageType = LanguageManager.getChosenLanguageType(currentLocale);
        setLanguage(engine.getLocale(languageType), languageType, false, false);

        super.onCreate();   // See onCreateOpenWnn() comment.
        mWnn.onCreate();

        KeyboardSkinData.getInstance().init(OpenWnn.superGetContext());

        SoundManager sManager;
        sManager = SoundManager.getInstance();
        sManager.init(OpenWnn.getContext());
        sManager.addSound(SOUND_DELETE_KEY, R.raw.delete);
        sManager.addSound(SOUND_RETURN_KEY, R.raw.keyreturn);
        sManager.addSound(SOUND_SPACEBAR_KEY, R.raw.spacebar);
        sManager.addSound(SOUND_STANDARD_KEY, R.raw.standard);

        resetExtendedInfo();
    }

    /**
     * Call OpenWnn class onCreate.
     * Do nothing!!
     */
    public void onCreateOpenWnn() {
        // TODO:Can not call super.onCreate().
        // If a software keyboard is opened, an aborting will be carried out by WindowManager.
        // [Error Message] Attempted to add input method window with bad token null. Aborting.
        //super.onCreate();
    }

    /**
     * Create and return the view hierarchy used to show candidates.
     *
     * @see android.inputmethodservice.InputMethodService#onCreateCandidatesView
     */
    @Override public View onCreateCandidatesView() {
        mWnn.onCreateCandidatesView();
        return super.onCreateCandidatesView();
    }

    /**
     * Create and return the view hierarchy used for the input area
     *  (such as a soft keyboard).
     *
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#onCreateInputView
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onCreateInputView
     */
    @Override public View onCreateInputView() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onCreateInputView()  Unprocessing onCreate() ");
            return super.onCreateInputView();
        } else if (mInputConnection == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onCreateInputView()  InputConnection is not active");
            return super.onCreateInputView();
        }
        // A return value of mWnn.onCreateInputView() is not necessary.
        // Because this value is null always.
        mWnn.onCreateInputView();
        return super.onCreateInputView();
    }

    /**
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#onKeyDown
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onKeyDown()  Unprocessing onCreate() ");
            return super.onKeyDown(keyCode, event);
        } else if (mInputConnection == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onKeyDown()  InputConnection is not active");
            return super.onKeyDown(keyCode, event);
        }
        if (!mDirectInputMode) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (event.isShiftPressed()) {
                    if (null != mWnn) {
                        int tempConvertingForFuncKeyType = mWnn.getConvertingForFuncKeyType();
                        boolean convertingForFuncKey = (tempConvertingForFuncKeyType != iWnnEngine.CONVERT_TYPE_NONE);
                        mWnn.setConvertingForFuncKeyType(iWnnEngine.CONVERT_TYPE_NONE);
                        if (convertingForFuncKey && (null != mComposingText)) {
                            int len = mComposingText.size(ComposingText.LAYER1);
                            if (0 < len) {
                                // Conversion of function keys, Menu key is invalid.
                                mWnn.setConvertingForFuncKeyType(tempConvertingForFuncKeyType);
                                return true;
                            }
                        }
                        // Since there is no doubt the character of a flag is true convertingForFuncKey is inconsistent.
                        // So, as a false flag operation carried out.
                    }
                    Intent intent = new Intent();
                    intent.setClass(OpenWnn.superGetContext(), ControlPanelStandard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                }
                break;

            default:
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Called to inform the input method that text input has started in an editor.
     *
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#onStartInput
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onStartInput
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onStartInput()  Unprocessing onCreate() ");
            super.onStartInput(attribute, restarting);
            return;
        }
        super.onStartInput(attribute, restarting);
        mWnn.onStartInput(attribute, restarting);
    }

    /**
     * Called when the input view is being shown and input has started on a new editor.
     *
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onStartInputView(EditorInfo, boolean)
     */
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        if (DEBUG) {Log.d(TAG, "onStartInputView");}
        try {
            if (getCurrentIme() == null) {
                Log.e("iWnn",
                        "IWnnLanguageSwitcher::onStartInputView()  Unprocessing onCreate() ");
                if (DEBUG) {Log.d(TAG, "Unprocessing oncreate");}
                super.onStartInputView(attribute, restarting);
                return;
            }
            mIsUnlockReceived = false;
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(OpenWnn.superGetContext());

            // For dynamically set keyboard layout, Set setting before keyboard
            // layout is set.
            if (mInputViewManager != null) {
                mInputViewManager.setPreferences(pref, attribute);
            } // else {}

            if (mHangulFlagForHw
                    && !OpenWnn.mOriginalInputMethodSwitcher.mNavigationMode
                    && !OpenWnn.mOriginalInputMethodSwitcher.mSymbolMode) {
                mCurrentLanguage = LOCALE_KO_KR.getLanguage();
                setHangulFlagForHw(false);
            }

            boolean isInputTypeChanged = (mOldEditorInfo == null)
                    || (mOldEditorInfo.inputType != attribute.inputType);

            if (isInputTypeChanged) {
                Resources res = getResources();
                boolean enableInputJa = pref.getBoolean(ControlPanelStandard.INPUT_LANGUAGE_JA_KEY,
                                            res.getBoolean(R.bool.input_language_ja_default_value));
                boolean enableInputEn = pref.getBoolean(ControlPanelStandard.INPUT_LANGUAGE_EN_KEY,
                                            res.getBoolean(R.bool.input_language_en_default_value));
                boolean enableInputKo = pref.getBoolean(ControlPanelStandard.INPUT_LANGUAGE_KO_KEY,
                                            res.getBoolean(R.bool.input_language_ko_default_value));

                if (!enableInputJa && !enableInputEn) {
                    mCurrentLanguage = LOCALE_KO_KR.getLanguage();
                }

                if (mInputViewManager != null) {
                    if (mInputViewManager instanceof DefaultSoftKeyboardJAJP) {
                        if ((((DefaultSoftKeyboardJAJP)mInputViewManager).getKeyMode()
                                                == DefaultSoftKeyboardJAJP.KEYMODE_JA_HALF_ALPHABET)
                           && (!enableInputEn)
                           && (enableInputKo)
                           && (((DefaultSoftKeyboardJAJP)mInputViewManager).isEnableKoreanImeByEdit())) {
                            mCurrentLanguage = LOCALE_KO_KR.getLanguage();
                        }
                    }

                }
            }

            int languageType = LanguageManager
                    .getChosenLanguageType(mCurrentLanguage);
            iWnnEngine engine = iWnnEngine.getEngine();

            if (LOCALE_KO_KR.getLanguage().equals(mCurrentLanguage) && isInputTypeChanged) {
                int defaultKeyMode = IWnnImeJaJp.getDefaultKeyMode(attribute);
                if ((isNormalKeyboad(attribute))
                        && (pref.getBoolean("input_language_ko", false))
                        && ((defaultKeyMode == IWnnImeJaJp.DEFAULT_KEYMODE_NOTHING)
                                || (defaultKeyMode ==
                                IWnnImeJaJp.DEFAULT_KEYMODE_LGE_AIME_KEEP_CURRENT_LANG))) {
                    setLanguage(engine.getLocale(languageType), languageType, true, true);
                } else {
                    InputMethodManager manager
                    = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    mCurrentLanguage = LOCALE_JA_JP.getLanguage();
                    languageType = LanguageManager.getChosenLanguageType(
                                    getSubtypeLocale(manager.getCurrentInputMethodSubtype()));
                    setLanguage(engine.getLocale(languageType), languageType, true, true);
                }
            } else {
                setLanguage(engine.getLocale(languageType), languageType, true, true);
            }
            mOldEditorInfo = attribute;
            mWnn.onStartInputView(attribute, restarting);
            if (KeyboardSkinData.getInstance().getPackageManger() == null) {
                KeyboardSkinData.getInstance().init(OpenWnn.superGetContext());
            }
            KeyboardSkinData.getInstance().setPreferences(pref);
        } catch (NullPointerException ex) {
            Log.e("iWnn_switchIME_error",
                    "IWnnLanguageSwitcher::onStartInputView() NullPointerException");
            int code = OpenWnnEvent.CHANGE_INPUT_VIEW;
            OpenWnnEvent ev = new OpenWnnEvent(code);
            mWnn.onEvent(ev);
            View v = onCreateCandidatesView();
            if( v != null){
                setCandidatesView(v);
            }
        }

        if (OpenWnn.PERFORMANCE_DEBUG) {Log.d(TAG, "IWnnLanguageSwitcher::onStartInputView()  End");}
    }

    /**
     * Called when the input view is being shown and input has started on a new editor.
     *
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#onStartInputView(EditorInfo, boolean)
     */
    public void onStartInputViewOpenWnn(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
    }

    /**
     * Take care of handling configuration changes.
     *
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onConfigurationChanged(Configuration)
     */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onConfigurationChanged()  Unprocessing onCreate() ");
            super.onConfigurationChanged(newConfig);
            return;
        }
        if (DEBUG) {Log.d(TAG, "IWnnLanguageSwitcher.onConfigurationChanged");}

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
        InputMethodBase.mOriginalInputMethodSwitcher.setEnableFloating(
                pref.getBoolean(getFloatingPreferenceKeyName(), OpenWnn.superGetContext()
                        .getResources().getBoolean(R.bool.floating_mode_show_default_value)));
        mWnn.onConfigurationChanged(newConfig);
    }

    /**
     * Take care of handling configuration changes.
     * <br>
     * @see android.inputmethodservice.InputMethodService#onConfigurationChanged(Configuration)
     */
    public void onConfigurationChangedOpenWnn(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Call the hideWindow
     *
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#hideWindow
     */
    @Override public void hideWindow() {
        if (OpenWnn.PERFORMANCE_DEBUG) {Log.d(TAG, "IWnnLanguageSwitcher::hideWindow()  Start");}

        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::hideWindow()  Unprocessing onCreate() ");
            super.hideWindow();
            return;
        }
        mWnn.hideWindow();

        if (OpenWnn.PERFORMANCE_DEBUG) {Log.d(TAG, "IWnnLanguageSwitcher::hideWindow()  End");}
    }

    /**
     * Call the hideWindow.
     *
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#hideWindow
     */
    public void hideWindowOpenWnn() {
        super.hideWindow();
    }

    /**
     * Called when the application has reported a new selection region of the text.
     *
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onUpdateSelection
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onUpdateSelection()  Unprocessing onCreate() ");
            super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
            return;
        }
        mWnn.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
    }

    /**
     * Called when the application has reported a new selection region of the text.
     *
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#onUpdateSelection
     */
    public void onUpdateSelectionOpenWnn(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvent */
    @Override synchronized public boolean onEvent(OpenWnnEvent ev) {
        if (mInputConnection == null) {
            Log.e("iWnn", "OpenWnn::onEvent()  InputConnection is not active");
            return false;
        }
        return mWnn.onEvent(ev);
    }

    /**
     * Controls the visibility of the candidates display area.
     *
     * @see android.inputmethodservice.InputMethodService#setCandidatesViewShown
     */
    @Override public void setCandidatesViewShown(boolean shown) {
        mIsCandidatesViewShown = shown;
        super.setCandidatesViewShown(shown);
    }

    /**
     * @see android.inputmethodservice.InputMethodService#onComputeInsets
     */
    @Override public void onComputeInsets(InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (mIsCandidatesViewShown) {
            outInsets.visibleTopInsets += getResources().getDimensionPixelSize(
                    R.dimen.candidate_blank_area_height);
        }
        outInsets.contentTopInsets = outInsets.visibleTopInsets;
    }

    /**
     * Override this to control when the input method should run in fullscreen mode.
     *
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvaluateFullscreenMode
     */
    @Override public boolean onEvaluateFullscreenMode() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onEvaluateFullscreenMode()  Unprocessing onCreate() ");
            return super.onEvaluateFullscreenMode();
        }
        return mWnn.onEvaluateFullscreenMode();
    }

    /**
     * Override this to control when the input method should run in fullscreen mode.
     *
     * @see android.inputmethodservice.InputMethodService#onEvaluateFullscreenMode
     */
    public boolean onEvaluateFullscreenModeOpenWnn() {
        return super.onEvaluateFullscreenMode();
    }

    /**
     * Override this to control when the soft input area should be shown to the user.
     *
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onEvaluateInputViewShown
     */
    @Override public boolean onEvaluateInputViewShown() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onEvaluateInputViewShown()  Unprocessing onCreate() ");
            return super.onEvaluateInputViewShown();
        }
        return mWnn.onEvaluateInputViewShown();
    }

    /**
     * Called to inform the input method that text input has finished in the last editor.
     *
     * @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#onFinishInput
     */
    @Override public void onFinishInput() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "IWnnLanguageSwitcher::onFinishInput()  Unprocessing onCreate() ");
            super.onFinishInput();
            return;
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean isScreenLock = keyguard.inKeyguardRestrictedInputMode();
        if (pm.isScreenOn() && !isScreenLock && !mIsUnlockReceived) {
            mWnn.clearMushroomResultString();
        }
        mIsUnlockReceived = false;
        mWnn.onFinishInput();
    }

    /**
     * Called to inform the input method that text input has finished in the last editor.
     * <br>
     * @see android.inputmethodservice.InputMethodService#onFinishInput
     */
    public void onFinishInputOpenWnn() {
        super.onFinishInput();
    }

    /** @see jp.co.omronsoft.iwnnime.ml.IWnnImeBase#close */
    @Override protected void close() {
        mWnn.close(false);
    }

    /**
     * Set language for a Standard Keyboard.
     *
     * @param locale  Requested locale.
     * @param locale  Requested locale type.(value of LanguageManager.getChosenLanguageType())
     * @param requestInitialize  {@code true} if IME initialization is necessary, else {@code false}.
     * @param prefChange  {@code true} if necessary to change Preferences, else {@code false}.
     * @return set locale code.
     */
    public void setLanguage(Locale locale, int langtype, boolean requestInitialize, boolean prefChange) {
        if (DEBUG) {Log.d(TAG, "IWnnLanguageSwitcher.setLanguage(" + locale.toString() + ")");}

        boolean isEqual = (mSelectedLocale != null) && mSelectedLocale.equals(locale);
        if (isEqual) {
            String localeCode = selectLanguage(locale, false);
            if (localeCode != null) {
                if(prefChange){
                    updateChoosedLanguage(localeCode, OpenWnn.superGetContext());
                }
            }
        } else {
            String localeCode = selectLanguage(locale, true);

            if (localeCode != null) {

                iWnnEngine engine = iWnnEngine.getEngine();
                // If the language used when it becomes three or more,
                // instead of engine.syncDictionary(), it is necessary to run engine.close().
                if (engine.isInit()) {
                    engine.syncDictionary(iWnnEngine.LanguageType.JAPANESE, iWnnEngine.SetType.LEARNDIC);
                    engine.syncDictionary(iWnnEngine.LanguageType.ENGLISH, iWnnEngine.SetType.LEARNDIC);
                    engine.syncDictionary(iWnnEngine.LanguageType.KOREAN, iWnnEngine.SetType.LEARNDIC);
                }

                mSelectedLocale = locale;

                if(prefChange){
                    updateChoosedLanguage(localeCode, OpenWnn.superGetContext());
                }
            }
        }

        if ((requestInitialize) && (!isEqual || notInitialized)) {
            notInitialized = false;
            mWnn.onCreate();
            View v = onCreateInputView();
            if (v != null) {
                if (!isFloatingPopupShown()) {
                    setInputView(v);
                }
            }
            View candidatesView = onCreateCandidatesView();
            if (candidatesView != null) {
                if (!isFloatingPopupShown()) {
                    setCandidatesView(candidatesView);
                }
            }
        }
        return;
    }

    public void clearInitializedFlag(){
        notInitialized = true;
    }

    /**
     * Returns the InitilizedState
     * @return the InitilizedState
     */
    public boolean getInitializeState() {
        return notInitialized;
    }

    /**
     * Return true if {@code language} equals {@link java.util.Locale#getLanguage}.
     * @param language  Checked language.
     * @param locale  Checked locale.
     * @return {@code true} if match, else {@code false}.
     */
    private static boolean isMatchLanguage(String language, Locale locale) {
        return locale.getLanguage().equals(language);
    }

    /**
     * Return whether {@code id} is enabled or not on IME.
     *
     * @param manager  Used to get enabled input methods.
     * @param id  Unique identifier of the new input method on start.
     * @return {@code true} if id is enabled IME's, else {@code false}.
     */
    public static boolean isEnabledImeId(InputMethodManager manager, String id) {
        if (DEBUG) {Log.d(TAG, "IWnnLanguageSwitcher.isEnabledImeId");}
        List<InputMethodInfo> infoList = manager.getEnabledInputMethodList();

        int infoListSize = infoList.size();
        for (int i = 0; i < infoListSize; i++) {
            if(infoList.get(i).getId().equals(id)){
                if (DEBUG) {Log.d(TAG, "  matched!!");}
                return true;
            }
        }
        return false;
    }

    /**
     * Select language of a Standard Keyboard.
     *
     * @param locale  Requested locale.
     * @param imeInit  {@code true} if New does IWnnIME, else {@code false}.
     * @return localeCode
     */
    public String selectLanguage(Locale locale, boolean imeInit) {
        return selectLanguage(locale, imeInit, this);
    }

    /**
     * Select language of a Standard Keyboard.
     *
     * @param locale  Requested locale.
     * @param imeInit {@code true} if New does IWnnIME, else {@code false}.
     * @param self    IWnnLanguageSwitcher.
     * @return localeCode
     */
    public static String selectLanguage(Locale locale, boolean imeInit, IWnnLanguageSwitcher self) {
        String language = locale.getLanguage();

        IWnnImeBase wnn = null;
        String localeCode = null;

        if (isMatchLanguage(language, LOCALE_EN_US)) {
//            if (isMatchCountry(country, LOCALE_EN_GB)) {
//                if (imeInit) {wnn = new IWnnImeEnUk(self); }
//                localeCode = "en_uk";
//            } else {
//                if (imeInit) {wnn = new IWnnImeEnUs(self); }
//                localeCode = "en_us";
//            }
//        } else if (isMatchLanguage(language, LOCALE_DE)) {
//            if (imeInit) {wnn = new IWnnImeGerman(self); }
//            localeCode = "de";
//        } else if (isMatchLanguage(language, LOCALE_IT)) {
//            if (imeInit) {wnn = new IWnnImeItalian(self); }
//            localeCode = "it";
//        } else if (isMatchLanguage(language, LOCALE_FR)) {
//            if (isMatchCountry(country, LOCALE_FR_CA)) {
//                if (imeInit) {wnn = new IWnnImeFrCa(self); }
//                localeCode = "fr_ca";
//            } else {
//                if (imeInit) {wnn = new IWnnImeFrench(self); }
//                localeCode = "fr";
//            }
//        } else if (isMatchLanguage(language, LOCALE_ES_ES)) {
//            if (imeInit) {wnn = new IWnnImeSpanish(self); }
//            localeCode = "es";
//        } else if (isMatchLanguage(language, LOCALE_RU_RU)) {
//            if (imeInit) {wnn = new IWnnImeRussian(self); }
//            localeCode = "ru";
//        } else if (isMatchLanguage(language, LOCALE_NL_NL)) {
//            if (imeInit) {wnn = new IWnnImeDutch(self); }
//            localeCode = "nl";
//        } else if (isMatchLanguage(language, LOCALE_PL_PL)) {
//            if (imeInit) {wnn = new IWnnImePolish(self); }
//            localeCode = "pl";
//        } else if (isMatchLanguage(language, LOCALE_SV_SE)) {
//            if (imeInit) {wnn = new IWnnImeSwedish(self); }
//            localeCode = "sv";
//        } else if (isMatchLanguage(language, LOCALE_NB_NO)) {
//            if (imeInit) {wnn = new IWnnImeNorwegian(self); }
//            localeCode = "nb";
//        } else if (isMatchLanguage(language, LOCALE_CS_CZ)) {
//            if (imeInit) {wnn = new IWnnImeCzech(self); }
//            localeCode = "cs";
//        } else if (isMatchLanguage(language, LOCALE_ZH_CN)) {
//            if (isMatchCountry(country, LOCALE_ZH_TW)) {
//                if (imeInit) {wnn = new IWnnImeChineseTw(self); }
//                localeCode = "zh_tw_z";
//            } else {
//                if (imeInit) {wnn = new IWnnImeZhCnP(self); }
//                localeCode = "zh_cn_p";
//            }
        } else if (isMatchLanguage(language, LOCALE_JA_JP)
                || isMatchLanguage(language, LOCALE_JA)) {
            if (imeInit) {
                if ((self != null) && (self.mWnn != null)) {
                    self.mWnn.onDestroy();
                }
                wnn = new IWnnImeJaJp(self);
            }
            localeCode = "ja";
//        } else if (isMatchLanguage(language, LOCALE_PT_PT)) {
//            if (imeInit) {wnn = new IWnnImePortuguese(self); }
//            localeCode = "pt";
        } else if (isMatchLanguage(language, LOCALE_KO_KR)) {
            if (imeInit) {
                if ((self != null) && (self.mWnn != null)) {
                    self.mWnn.onDestroy();
                }
                wnn = new IWnnImeKorean(self);
            }
            localeCode = "ko";
        } // else {}

        if (!imeInit) {
            return localeCode;
        }

        if ((wnn == null) && (self != null && (self.mWnn == null))) {
            wnn = new IWnnImeJaJp(self);
            localeCode = "ja";
        }

        if (DEBUG) {
            if (wnn != null) {
                Log.d(TAG, "  new: " + wnn.toString());
            }
        }

        if (wnn != null && self != null) {
            if (DEBUG) {
                if (self.mWnn != null) { Log.d(TAG, " mWnn: " + self.mWnn.toString() + " -> wnn: " + wnn.toString());}
            }
            if (self.mWnn != null) {
                self.mWnn.closeSwitch();
            }
            self.mWnn = wnn;
        }

        return localeCode;
    }

    /**
     * Update the chosen language
     *
     * @param localeCode LocaleCode
     * @param context Context
     */
    public static void updateChoosedLanguage(String localeCode, Context context) {
        if (UserDictionaryToolsList.hasStarted()) {
            KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
            int userDicLang = UserDictionaryToolsListStandard.getUserDicLanguage();
            if (userDicLang == iWnnEngine.LanguageType.NONE) {
                return;
            } else if (langPack.getLocalLangPackClassName(context, userDicLang) != null) {
                return;
            } else {
                int prefLocale = LanguageManager.getChosenLanguageType(context);
                if (prefLocale != userDicLang) {
                    // During the editing multi-language dictionary user may enter a state in which there is no pack for that language.
                    // At that time, if they have the same value and the user dictionary editing language of preference, the
                    // Preference will be updated, even while editing a user dictionary.
                    return;
                }
            }
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("choosed_language", localeCode);
        editor.commit();
    }

    /**
     * Update the chosen language(Forcing)
     *
     * @param localeCode LocaleCode
     * @param context Context
     */
    public static void updateChoosedLanguageForcing(String localeCode, Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("choosed_language", localeCode);
        editor.commit();
    }

    /**
     * The initial language
     *
     * @param context Context
     */
    public static void initialLanguage(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String localeCode = pref.getString("choosed_language", "");
        if (localeCode.equals("")) {
            localeCode = selectLanguage(LOCALE_JA, false, null);
            updateChoosedLanguage(localeCode, context);
        }
    }

    /**
     * Called when the subtype was changed.
     *
     * @see android.inputmethodservice.InputMethodService#onCurrentInputMethodSubtypeChanged
     */
    @Override protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
        InputMethodSubtype currentSubtype = getCurrentInputMethodSubtype();

        super.onCurrentInputMethodSubtypeChanged(currentSubtype);

        String currentLocale = getSubtypeLocale(currentSubtype);

        if (isInputViewShown()) {
            if (mInputConnection != null) {
                mInputConnection.finishComposingText();
            }

            mWnn.onFinishInput();

            int languageType = LanguageManager.getChosenLanguageType(currentLocale);
            iWnnEngine engine = iWnnEngine.getEngine();
            setLanguage(engine.getLocale(languageType), languageType, true, true);

            mWnn.restartSelf(getCurrentInputEditorInfo());

            cancelToast();
            StringBuffer strBuf = new StringBuffer("ti_select_language_");
            strBuf.append(currentLocale);
            strBuf.append("_txt");
            int resId = getResources().getIdentifier(strBuf.toString(), "string", getPackageName());
            mSelectLangToast = Toast.makeText(OpenWnn.superGetContext(),
                                                resId, Toast.LENGTH_SHORT);
            mSelectLangToast.setGravity(Gravity.BOTTOM, 0, 0);
            mSelectLangToast.show();
        } else {
            int languageType = LanguageManager.getChosenLanguageType(currentLocale);
            iWnnEngine engine = iWnnEngine.getEngine();
            setLanguage(engine.getLocale(languageType), languageType, true, true);
        }
    }

    /**
     * Gets the locale of the subtype.
     *
     * @param subtype  The subtype.
     * @return The locale of the subtype.
     */
    public static String getSubtypeLocale(InputMethodSubtype subtype) {
        if (subtype == null) {
            return "ja";
        }

        String locale = subtype.getLocale();

        String tempLocale = LOCALE_CONVERT_ENGINELOCALE_TABLE.get(locale);
        if (tempLocale != null) {
            locale = tempLocale;
        }

        if (locale.length() == 0) {
            if (LOCALE_KO_KR.getLanguage().equals(mCurrentLanguage)) {
                locale = "ko";
            } else if (LOCALE_EN_US.getLanguage().equals(mCurrentLanguage)) {
                locale = "en_us";
            } else {
                locale = "ja";
            }
        }

        return locale;
    }


    /**
     * Reset Learning ExtendedInfo
     *
     */
    private void resetExtendedInfo() {
        String dicVersion = OpenWnn
                .getStringFromNotResetSettingsPreference(
                        OpenWnn.superGetContext(), DICTIONARY_VERSION_KEY, "");
        if (!dicVersion.equals(DICTIONARY_VERSION)) {
            iWnnEngine engine = iWnnEngine.getEngine();
            String[] confFileList = engine.getConfTable();
            for (int i = 0; i < confFileList.length; i++) {
                File confFile = new File(confFileList[i]);
                if (confFile.exists()) {
                    engine.resetExtendedInfo(confFileList[i]);
                }
            }
            SharedPreferences.Editor editor = getSharedPreferences(FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE).edit();
            editor.putString(DICTIONARY_VERSION_KEY, DICTIONARY_VERSION);
            editor.commit();
        }
    }

    /**
     * Is Enable ShortcutIME.
     *
     * @return true : Enable ShortcutIME
     *          false: Disable ShortcutIME
     */
    public boolean isEnableShortcutIME() {
        boolean ret = false;
        updateShortcutIME();
        if (mShortcutInputMethodInfo != null) {
            ret = true;
        }
        return ret;
    }

    /**
     * Is Available ShortcutIME.
     *
     * @return true : ShourtcutIME is available.
     *          false: ShourtcutIME isn't available.
     *  Diversion destination : packages/inputmethods/LatinIME/java/src/com/android/inputmethod/latin/SubtypeSwitcher#isShortcutImeReady()
     */
    public boolean isAvailableShortcutIME() {
        if (!isEnableShortcutIME()) {
            return false;
        }
        if (mShortcutSubtype == null) {
            return true;
        }
        boolean contain = contains(SUBTYPE_EXTRAVALUE_REQUIRE_NETWORK_CONNECTIVITY,
                                   mShortcutSubtype.getExtraValue());
        if (contain) {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Get that privateImeOptions is set disable voice-input.
     *
     * @param  editor EditorInfo,
     * @return boolean  privateImeOptions is set disable voice-input, (true:set, false:not set)
     *  Diversion destination : /packages/inputmethods/LatinIME/java/src/com/android/inputmethod/latin/Utils#inPrivateImeOptions()
     */
    public boolean getDisableVoiceInputInPrivateImeOptions(EditorInfo editor) {
        if (editor != null) {
            return (contains(PRIVATE_IME_OPTION_DISABLE_VOICE_INPUT, editor.privateImeOptions) ||
                     contains(PRIVATE_IME_OPTION_DISABLE_VOICE_INPUT_COMPAT, editor.privateImeOptions));
        }
        return false;
    }

    /**
     * contains key.
     *
     * @param keyStr    Determine if the key contains,
     * @param splitStr  String to be split by ','.
     * @return boolean  contains the key to containsStr, (true:contain, false:not contain)
     */
    private boolean contains(String keyStr, String splitStr) {
        if (splitStr != null) {
            for (String option : splitStr.split(",")) {
                if (option.equals(keyStr)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * handle LanguageSwitchKey.
     *
     */

    public void handleLanguageSwitchKey() {
        final InputMethodManager manager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        final boolean includesOtherImes = pref.getBoolean(
                "opt_change_otherime",
                getResources().getBoolean(
                        R.bool.opt_change_otherime_default_value));
        final IBinder token = getWindow().getWindow().getAttributes().token;

        manager.switchToNextInputMethod(token, !includesOtherImes);
    }

    /**
     * Update ShortcutIME.
     *
     *  Diversion destination : packages/inputmethods/LatinIME/java/src/com/android/inputmethod/latin/SubtypeSwitcher#updateShortcutIME()
     */
    public void updateShortcutIME() {
        final InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        final Map<InputMethodInfo, List<InputMethodSubtype>> shortcuts = manager.getShortcutInputMethodsAndSubtypes();
        mShortcutInputMethodInfo = null;
        mShortcutSubtype = null;
        for (InputMethodInfo imi : shortcuts.keySet()) {
            List<InputMethodSubtype> subtypes = shortcuts.get(imi);
            mShortcutInputMethodInfo = imi;
            mShortcutSubtype = subtypes.size() > 0 ? subtypes.get(0) : null;
            break;
        }
    }

    /**
     * Switch to Shortcut IME.
     *
     *  Diversion destination : packages/inputmethods/LatinIME/java/src/com/android/inputmethod/latin/SubtypeSwitcher#switchToShortcutIME()
     */
    public void switchToShortcutIME() {
        if (mShortcutInputMethodInfo == null) {
            return;
        }

        final String imiId = mShortcutInputMethodInfo.getId();
        final InputMethodSubtype subtype = mShortcutSubtype;
        switchToTargetIME(imiId, subtype);
    }

    /**
     * Switch to Target IME.
     *
     *  Diversion destination : packages/inputmethods/LatinIME/java/src/com/android/inputmethod/latin/SubtypeSwitcher#switchToTargetIME()
     */
    private void switchToTargetIME(final String imiId, final InputMethodSubtype subtype) {
        final InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        final IBinder token = getWindow().getWindow().getAttributes().token;
        if (token == null) {
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                manager.setInputMethodAndSubtype(token, imiId, subtype);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Calls in this method need to be done in the same thread as the thread which
                // called switchToShortcutIME().

                // Notify an event that the current subtype was changed. This event will be
                // handled if "onCurrentInputMethodSubtypeChanged" can't be implemented
                // when the API level is 10 or previous.
                // Therefore, APILevel because of the 14, where the processing is unnecessary, especially.
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Get current subtype.
     *
     * @return current subtype.
     */
    private InputMethodSubtype getCurrentInputMethodSubtype() {
        InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodSubtype currentSubtype = manager.getCurrentInputMethodSubtype();
        return currentSubtype;
    }

    /**
     * Get current locale.
     *
     * @return current locale.
     */
    public Locale getSelectedLocale() {
        return mSelectedLocale;
    }


    /**
     * Called when the subtype was changed.
     *
     * @param language  Choose language.
     */
    public void changeKeyboardLanguage(String language) {

        if (isInputViewShown()) {
            if (mInputConnection != null) {
                mInputConnection.finishComposingText();
            }

            mWnn.onFinishInput();

            mCurrentLanguage = language;
            int languageType = LanguageManager.getChosenLanguageType(language);
            iWnnEngine engine = iWnnEngine.getEngine();
            setLanguage(engine.getLocale(languageType), languageType, true, true);

            mWnn.onStartInputView(getCurrentInputEditorInfo(), false);
        }
    }

    /**
     * Return whether {@code attribute} is Normal keyboad.
     * @param attribute  EditorInfo.
     * @param locale  Checked locale.
     * @return {@code true} if attribute is normal keyboad, else {@code false}.
     */
    private boolean isNormalKeyboad(EditorInfo attribute) {
        int inputType = attribute.inputType;

        switch (inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
        case EditorInfo.TYPE_CLASS_DATETIME:
        case EditorInfo.TYPE_CLASS_PHONE:
            return false;
        case EditorInfo.TYPE_CLASS_TEXT:
            switch (inputType & EditorInfo.TYPE_MASK_VARIATION) {
            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_URI:
            case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                return false;
            default:
                return true;
            }
        default:
            return true;
        }
    }

    /**
     * Hangul keep Flag For Handwriting to IME.
     *
     * @param hangulFlag  Hangul on flag.
     */
    public void setHangulFlagForHw(boolean hangulFlag) {
        mHangulFlagForHw = hangulFlag;
    }

    /**
     * Get the candidate view manager.
     *
     * @return candidate view manager.
     */
    public CandidatesViewManager getCandidateViewManager() {
        return mCandidatesViewManager;
    }

    /**
     * Get the input view manager.
     *
     * @return input view manager.
     */
    public InputViewManager getInputViewManager() {
        return mInputViewManager;
    }

    /**
     * Free resources before finalizing this instance.
     *
     */
    @Override public void freeResources() {
        if (mWnn != null) {
            mWnn.onDestroy();
        }
        super.freeResources();
    }

    @Override public void toggleExpandFloatingMode() {
        super.toggleExpandFloatingMode();

        onStartInput(getEditorInfo(), false);
        onStartInputView(getEditorInfo(), false);
    }

    /**
     * Save flag from the symbol list to Hangul or not.
     *
     * @param isHangul  Hangle is true.
     */
    public void setSymbolBackHangul(boolean isHangul) {
        mIsSymbolBackHangul = isHangul;
    }

    /**
     * Return flag from the symbol list to Hangul or not.
     *
     * @return  flag from the symbol list to Hangul or not.
     */
    public boolean getSymbolBackHangul() {
        return mIsSymbolBackHangul;
    }

    /**
     * Saving that has been switched from the Korean language input mode.
     *
     * @param mode Flag has been switched from Korean input mode.
     */
    public void setIsFromHangulIme(boolean mode) {
        mIsFromHangulIme = mode;
        return;
    }

    /**
     * The return of the flag that has been switched from Korean input mode.
     *
     * @return If switching from Korean input mode, true.
     */
    public boolean isFromHangulIme() {
        return mIsFromHangulIme;
    }

    @Override
    public void resetKeyMode() {
        if (DEBUG) { Log.d(TAG,"IWnnLanguageSwitcher.resetKeyMode()"); }
        clearInitializedFlag();
        onStartInputView(getCurrentInputEditorInfo(), false);
        DefaultSoftKeyboard kb = (DefaultSoftKeyboard)getInputViewManager();
        if (kb instanceof DefaultSoftKeyboardJAJP) {
            DefaultSoftKeyboardJAJP inputViewManager =
                    (DefaultSoftKeyboardJAJP)getInputViewManager();
            int oldCurrentKeyMode = inputViewManager.getKeyMode();
            inputViewManager.changeKeyMode(oldCurrentKeyMode);
        } else {
            DefaultSoftKeyboardHangul inputViewManager =
                    (DefaultSoftKeyboardHangul)getInputViewManager();
            int oldCurrentKeyMode = inputViewManager.getKeyMode();
            inputViewManager.changeKeyMode(oldCurrentKeyMode);
        }
        return;
    }

}
