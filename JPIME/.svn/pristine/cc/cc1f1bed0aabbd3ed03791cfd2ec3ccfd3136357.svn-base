/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.ActionBar.LayoutParams;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.ExtractEditText;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;    // for debug
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;         // for debug
import android.view.inputmethod.InputConnection;    // for debug
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiManager;
import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.KeyAction;
import jp.co.omronsoft.iwnnime.ml.KeyboardLanguagePackData;
import jp.co.omronsoft.iwnnime.ml.TextCandidatesViewManager;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.lang.IllegalAccessException;
import java.lang.NoSuchMethodException;
import java.lang.Object;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The OpenWnn IME's base class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class OpenWnn extends InputMethodBase {
    /** for DEBUG */
    private static boolean DEBUG = false;

    /** for PERFORMANCE DEBUG */
    public static final boolean PERFORMANCE_DEBUG = false;

    /** for  FILE I/O PERFORMANCE DEBUG */
    public static final boolean FILEIO_PERFORMANCE_DEBUG = false;

    /** for isXLarge */
    public static final int DENSITY_XHIGH = 320;

    /** Charaset name of UTF-8 */
    public static final String CHARSET_NAME_UTF8 = "UTF-8";

    /** The path of DecoEmoji pseudo dictionary */
    private static final String DECOEMOJI_GIJI_DICTIONALY_PATH = "/dicset/master/";

    /** The name of DecoEmoji pseudo dictionary */
    private static final String DECOEMOJI_GIJI_DICTIONALY_NAME = "njdecoemoji.a";

    public static final String SUBTYPE_EMOJI_INPUT_EXTRA_VALUE = "emojiinput";

    /** Candidate view */
    protected CandidatesViewManager  mCandidatesViewManager = null;
    /** Conversion engine */
    protected WnnEngine  mConverter = null;
    /** Pre-converter (for Romaji-to-Kana input, Hangul input, etc.) */
    protected LetterConverter  mPreConverter = null;
    /** The inputting/editing string */
    protected ComposingText  mComposingText = null;
    /** The input connection */
    protected InputConnection mInputConnection = null;
    /** Auto hide candidate view */
    protected boolean mAutoHideMode = true;
    /** Direct input mode */
    protected boolean mDirectInputMode = true;

    /** Flag for checking if the previous down key event is consumed by OpenWnn  */
    private boolean mConsumeDownEvent;

    /** The instance of current IME */
    private static OpenWnn mCurrentIme;

    /** Tablet mode */
    private static boolean mTabletMode = false;

    /** Input funfun count */
    private int mFunfun;

    /** Whether keeps inputting. */
    protected boolean mIsKeep;

    /** KeyAction list */
    private List<KeyAction> KeyActionList = new ArrayList<KeyAction>();

    /** Keeped key down event when IME is not started and input type is not null */
    private KeyEvent mKeyDownEvent;

    /** Current input type */
    private int mInputType;

    /** Editor in ime package */
    private boolean mEditorInImePackage = false;

    /** Interface for DecoEmojiManager */
    private IDecoEmojiManager mDecoEmojiInterface = null;

    /** Whether binding service. */
    private boolean mIsBind = false;

    /** for emojiType */
    private static int mEmojiType = 0;

    /** IME body */
    public IWnnImeBase mWnn = null;

    /**
     * View to show the composing wildcard string.
     */
    protected ComposingExtraView mComposingExtraView;

    /**
     * The floating container which contains the composing extra view. If necessary,
     * some other view like candiates container can also be put here.
     */
    protected LinearLayout mExtraViewFloatingContainer;

    /**
     * Window to show the composing extra view.
     */
    protected PopupWindow mFloatingWindow;

    /**
     * Used to show the floating window.
     */
    protected PopupTimer mFloatingWindowTimer = new PopupTimer();

    /** The file name of not reset settings preferences. */
    public static final String FILENAME_NOT_RESET_SETTINGS_PREFERENCE = "not_reset_settings_pref";

    /** bind service*/
    private ServiceConnection mServiceConn = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (DEBUG) {Log.d("OpenWnn", "onServiceConnected() START");}
            mDecoEmojiInterface = IDecoEmojiManager.Stub.asInterface(binder);

            if (getPreferenceId() == -1) {
                callCheckDecoEmoji();
            }

            if (DEBUG) {Log.d("OpenWnn", "onServiceConnected() END");}
        }

        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) {Log.d("OpenWnn", "onServiceDisconnected() START");}
            mDecoEmojiInterface = null;
            if (DEBUG) {Log.d("OpenWnn", "onServiceDisconnected() END");}
        }
    };

    /** Is EmojiAssist working. */
    private boolean mIsEmojiAssistWorking;

    /** Whether received Unlock. */
    protected boolean mIsUnlockReceived = false;

    /** instance of select language information toast. */
    protected Toast mSelectLangToast = null;

    /**
    * BroadcastReceiver of Intent.ACTION_CLOSE_SYSTEM_DIALOGS,
    * Intent.ACTION_SCREEN_ON and Intent.ACTION_SCREEN_OFF
    */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /**
         * Called when the BroadcastReceiver is receiving an Intent broadcast.
         *
         * @see android.content.BroadcastReceiver#onReceive
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mWnn != null && mWnn.getMushroomResultString() != null) {
                KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                boolean isScreenLock = keyguard.inKeyguardRestrictedInputMode();
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && !isScreenLock) {
                    mWnn.AsyncCommitMushRoomString();
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    mIsUnlockReceived = true;
                }
            }
            if (mIsEmojiAssistWorking) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    mEmojiAssist.startAnimation();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    mEmojiAssist.stopAnimation();
                }
            }
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                if (mInputViewManager != null) {
                    mInputViewManager.dismissPopupKeyboard();
                    BaseInputView baseInputView
                            = ((BaseInputView)((DefaultSoftKeyboard) mInputViewManager).getCurrentView());
                    if (baseInputView != null) {
                        baseInputView.closeDialog();
                    }
                }
                if (mCandidatesViewManager != null) {
                    mCandidatesViewManager.closeDialog();
                }
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onFinishInput();
                requestHideSelf(0);
            }
        }
    };

    /**
     * Constructor
     */
    public OpenWnn(InputMethodSwitcher ims) {
      super(ims);
    }

    /***********************************************************************
     * InputMethodService
     **********************************************************************/
    /**
     * Called by the system when the service is first created.
     *
     * @see android.inputmethodservice.InputMethodService#onCreate
     */
    @Override public void onCreate() {
        updateTabletMode(OpenWnn.superGetContext());
        super.onCreate();

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext()); // DEBUG
        DEBUG = pref.getBoolean("debug_log", false);

        mCurrentIme = this;

        if (DEBUG) {Log.d("OpenWnn", "onCreate()");}

        copyPresetDecoEmojiGijiDictionary();

        if (mConverter != null) { mConverter.init(getFilesDirPath()); }
        if (mComposingText != null) { mComposingText.clear(); }

        registReceiver();
        //Log.i("iWnn", getResources().getString(R.string.ti_iWnnIME_version_info));
    }

    /**
     * Create and return the view hierarchy used to show candidates.
     *
     * @see android.inputmethodservice.InputMethodService#onCreateCandidatesView
     */
    @Override public View onCreateCandidatesView() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onCreateCandidatesView()  Unprocessing onCreate() ");
            return super.onCreateCandidatesView();
        } else if (mInputConnection == null) {
            Log.e("iWnn", "OpenWnn::onCreateCandidatesView()  InputConnection is not active");
            return super.onCreateCandidatesView();
        }
        if (mCandidatesViewManager != null) {
            WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            View view = mCandidatesViewManager.initView(this,
                                                        wm.getDefaultDisplay().getWidth(),
                                                        wm.getDefaultDisplay().getHeight());
            mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);

            LayoutInflater inflater = getLayoutInflater();
            // Inflate the extra view floating container view
            mExtraViewFloatingContainer = (LinearLayout) inflater.inflate(R.layout.extra_view_floating_container, null);

            // The first child is the composing extra view.
            mComposingExtraView = (ComposingExtraView) mExtraViewFloatingContainer.getChildAt(0);

            if (null != mFloatingWindow && mFloatingWindow.isShowing()) {
                mFloatingWindowTimer.cancelShowing();
                mFloatingWindow.dismiss();
            }

            mFloatingWindow = new PopupWindow(OpenWnn.superGetContext());
            mFloatingWindow.setClippingEnabled(false);
            mFloatingWindow.setBackgroundDrawable(null);
            mFloatingWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
            mFloatingWindow.setContentView(mExtraViewFloatingContainer);

            if (DEBUG) {Log.d("OpenWnn", "onCreateCandidatesView()="+view);}
            return view;
        } else {
            if (DEBUG) {Log.d("OpenWnn", "onCreateCandidatesView()="+null);}
            return super.onCreateCandidatesView();
        }
    }

    /**
     * Create and return the view hierarchy used for the input area
     * (such as a soft keyboard).
     *
     * @see android.inputmethodservice.InputMethodService#onCreateInputView
     */
    @Override public View onCreateInputView() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onCreateInputView()  Unprocessing onCreate() ");
            return super.onCreateInputView();
        } else if (mInputConnection == null) {
            Log.e("iWnn", "OpenWnn::onCreateInputView()  InputConnection is not active");
            return super.onCreateInputView();
        }
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext()); // DEBUG
        DEBUG = pref.getBoolean("debug_log", false);
        if (DEBUG) {Log.d("OpenWnn", "onCreateInputView()");}

        KeyboardLanguagePackData langPack = KeyboardLanguagePackData.getInstance();
        langPack.setInputMethodSubtypeInstallLangPack(OpenWnn.superGetContext());
        InputMethodInfo imi = langPack.getMyselfInputMethodInfo(OpenWnn.superGetContext());
        int subtypeCount = 1;
        if (imi != null) {
            subtypeCount = imi.getSubtypeCount();
        }
        if (subtypeCount == 1) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(ControlPanelStandard.CHANGE_OTHER_IME_KEY, true);
            editor.commit();
        }

        if (mInputViewManager != null) {
            WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            return mInputViewManager.initView(this,
                                              wm.getDefaultDisplay().getWidth(),
                                              wm.getDefaultDisplay().getHeight());
        } else {
            return super.onCreateInputView();
        }
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     *
     * @see android.inputmethodservice.InputMethodService#onDestroy
     */
    @Override public void onDestroy() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onDestroy()  Unprocessing onCreate() ");
            super.onDestroy();
            return;
        }
        super.onDestroy();
        requestHideSelf(0);
        if (DEBUG) {Log.d("OpenWnn", "onDestroy()");}
        if (mCandidatesViewManager != null) {
            mCandidatesViewManager.setCandidateMsgRemove();
        }
        mCurrentIme = null;
        close();

        freeResources();
    }

    /**
     * Called when the key down event occurred.
     *
     * @see android.inputmethodservice.InputMethodService#onKeyDown
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        cancelToast();
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onKeyDown()  Unprocessing onCreate() ");
            return super.onKeyDown(keyCode, event);
        } else if (mInputConnection == null) {
            Log.e("iWnn", "OpenWnn::onKeyDown()  InputConnection is not active");
            return super.onKeyDown(keyCode, event);
        }
        if (DEBUG) {Log.d("OpenWnn", "onKeyDown("+keyCode+")");}

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mConsumeDownEvent = super.onKeyDown(keyCode, event);
            return mConsumeDownEvent;
        } else {
            if ((mAttribute == null) ||
                (keyCode == KeyEvent.KEYCODE_ESCAPE) || (keyCode == KeyEvent.KEYCODE_ZENKAKU_HANKAKU)) {
                mConsumeDownEvent = onEvent(new OpenWnnEvent(event));
            } else {
                switch (mAttribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
                case EditorInfo.TYPE_CLASS_DATETIME:
                    switch (mAttribute.inputType & EditorInfo.TYPE_MASK_VARIATION) {
                        case EditorInfo.TYPE_DATETIME_VARIATION_DATE:
                        case EditorInfo.TYPE_DATETIME_VARIATION_TIME:
                            mConsumeDownEvent = false;
                            return super.onKeyDown(keyCode, event);
                        default:
                            mConsumeDownEvent = onEvent(new OpenWnnEvent(event));
                            break;
                    }
                    break;
                default:
                    mConsumeDownEvent = onEvent(new OpenWnnEvent(event));
                    break;
                }
            }
        }
        KeyAction Keycodeinfo = new KeyAction();
        Keycodeinfo.mConsumeDownEvent = mConsumeDownEvent;
        Keycodeinfo.mKeyCode = keyCode;

        int cnt = KeyActionList.size();
        if (cnt != 0) {
            for (int i = 0; i < cnt; i++) {
                if (KeyActionList.get(i).mKeyCode == keyCode) {
                    KeyActionList.remove(i);
                    break;
                }
            }
        }
        KeyActionList.add(Keycodeinfo);
        if (!mConsumeDownEvent) {
            return super.onKeyDown(keyCode, event);
        }
        return mConsumeDownEvent;
    }

    /**
     * Intercept key up events before they are processed by the application.
     *
     * @see android.inputmethodservice.InputMethodService#onKeyUp
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onKeyUp()  Unprocessing onCreate() ");
            return super.onKeyUp(keyCode, event);
        } else if (mInputConnection == null) {
            Log.e("iWnn", "OpenWnn::onKeyUp()  InputConnection is not active");
            return super.onKeyUp(keyCode, event);
        }
        boolean ret = mConsumeDownEvent;
        int cnt = KeyActionList.size();
        for (int i = 0; i < cnt; i++) {
            KeyAction Keycodeinfo = KeyActionList.get(i);
            if (Keycodeinfo.mKeyCode == keyCode) {
                ret = Keycodeinfo.mConsumeDownEvent;
                KeyActionList.remove(i);
                break;
            }
        }

        if (ret) {
            OpenWnnEvent wnnEvent = new OpenWnnEvent(event);
            if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
                //This judgment refers to InputmethodService#onKeyUp.
                wnnEvent.code = OpenWnnEvent.INPUT_KEY;
            }
            ret = onEvent(wnnEvent);
        } else {
            ret = super.onKeyUp(keyCode, event);
        }
        if (DEBUG) {Log.d("OpenWnn", "onKeyUp("+keyCode+")");}
        return ret;
    }

    /**
     * Called when the key long press event occurred.
     *
     * @see android.inputmethodservice.InputMethodService#onKeyLongPress
     */
    @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onKeyLongPress()  Unprocessing onCreate() ");
            return super.onKeyLongPress(keyCode, event);
        }
        if (DEBUG) {Log.d("OpenWnn", "onKeyLongPress("+keyCode+")");}

        OpenWnnEvent wnnEvent = new OpenWnnEvent(event);
        wnnEvent.code = OpenWnnEvent.KEYLONGPRESS;
        return onEvent(wnnEvent);
    }

    /**
     * Called to inform the input method that text input has started in an editor.
     *
     * @see android.inputmethodservice.InputMethodService#onStartInput
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onStartInput()  Unprocessing onCreate() ");
            super.onStartInput(attribute, restarting);
            return;
        }
        super.onStartInput(attribute, restarting);
        mInputConnection = getCurrentInputConnection();
        if (!restarting && mComposingText != null && !isKeepInput()) {
            mComposingText.clear();
        }
        mInputType = attribute.inputType;
        mKeyDownEvent = null;
        mEditorInImePackage = getPackageName().equals(attribute.packageName);
        if (DEBUG) {Log.d("OpenWnn", "onStartInput()");}
    }

    /**
     * Called when the input view is being shown and input has started on a new editor.
     *
     * @see android.inputmethodservice.InputMethodService#onStartInputView
     */
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onStartInputView()  Unprocessing onCreate() ");
            super.onStartInputView(attribute, restarting);
            return;
        }
        super.onStartInputView(attribute, restarting);
        mInputConnection = getCurrentInputConnection();

        mAttribute = attribute;

        ExtractEditText extractEditText = getExtractEditText();
        if (extractEditText != null) {
            Bundle bundle = mAttribute.extras;
            if (bundle != null) {
                boolean allowEmoji = bundle.getBoolean("allowEmoji");
                boolean allowDecoEmoji = bundle.getBoolean("allowDecoEmoji");
                Bundle extraBundle = extractEditText.getInputExtras(true);
                extraBundle.putBoolean("allowEmoji", allowEmoji);
                extraBundle.putBoolean("allowDecoEmoji", allowDecoEmoji);
                if (allowEmoji || allowDecoEmoji) {
                    EmojiAssist assist = EmojiAssist.getInstance();
                    assist.removeView(extractEditText);
                    assist.addView(extractEditText);
                }
            }
        }

        if (!restarting && !isKeepInput()) {
            setCandidatesViewShown(false);
        }
        if (DEBUG) {Log.d("OpenWnn", "onStartInputView()");}
        if (mInputConnection != null) {
            mDirectInputMode = false;
            if (!isKeepInput()) {
                if (mConverter != null) { mConverter.init(getFilesDirPath()); }
            }
        } else {
            mDirectInputMode = true;
        }
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(OpenWnn.superGetContext());
        if (mCandidatesViewManager != null) { mCandidatesViewManager.setPreferences(pref);  }
        if (mInputViewManager != null) { mInputViewManager.setPreferences(pref, attribute);  }
        if (mPreConverter != null) { mPreConverter.setPreferences(pref);  }
        if (mConverter != null) { mConverter.setPreferences(pref);  }

        decoEmojiBindStart();
        startEmojiAssist();
    }

    /**
     * Called when the fullscreen-mode extracting editor info has changed,
     * to update the state of its UI such as the action buttons shown.
     *
     * @see android.inputmethodservice.InputMethodService#onUpdateExtractingViews
     */
    @Override public void onUpdateExtractingViews(EditorInfo attribute) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onUpdateExtractingViews()  Unprocessing onCreate() ");
            super.onUpdateExtractingViews(attribute);
            return;
        }
        super.onUpdateExtractingViews(attribute);

        ExtractEditText extractEditText = getExtractEditText();
        if (extractEditText != null) {
            Bundle bundle = mAttribute.extras;
            if (bundle != null) {
                boolean allowEmoji = bundle.getBoolean("allowEmoji");
                boolean allowDecoEmoji = bundle.getBoolean("allowDecoEmoji");
                Bundle extraBundle = extractEditText.getInputExtras(true);
                extraBundle.putBoolean("allowEmoji", allowEmoji);
                extraBundle.putBoolean("allowDecoEmoji", allowDecoEmoji);
                if (allowEmoji || allowDecoEmoji) {
                    EmojiAssist assist = EmojiAssist.getInstance();
                    assist.removeView(extractEditText);
                    assist.addView(extractEditText);
                }
            }
        }
    }
    /**
     * Close this input method's soft input area, removing it from the display.
     *
     * @see android.inputmethodservice.InputMethodService#requestHideSelf
     */
    @Override public void requestHideSelf(int flag) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::requestHideSelf()  Unprocessing onCreate() ");
            super.requestHideSelf(flag);
            return;
        }
        if (DEBUG) {Log.d("OpenWnn", "requestHideSelf()");}
        super.requestHideSelf(flag);

        //mInputConnection = null;
        if (mInputViewManager == null) {
            hideWindow();
        }
    }

    /**
     * Controls the visibility of the candidates display area.
     *
     * @see android.inputmethodservice.InputMethodService#setCandidatesViewShown
     */
    @Override public void setCandidatesViewShown(boolean shown) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::setCandidatesViewShown()  Unprocessing onCreate() ");
            super.setCandidatesViewShown(shown);
            return;
        }
        super.setCandidatesViewShown(shown);
        if (shown) {
            showWindow(true);
        } else {
            if (mAutoHideMode && mInputViewManager == null) {
                hideWindow();
            }
        }
        if (DEBUG) {Log.d("OpenWnn", "setCandidatesViewShown("+shown+")");}
    }

    /**
     * Call the hideWindow
     *
     * @see android.inputmethodservice.InputMethodService#hideWindow
     */
    @Override public void hideWindow() {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::hideWindow()  Unprocessing onCreate() ");
            super.hideWindow();
            return;
        }
        if (DEBUG) {Log.d("OpenWnn", "hideWindow()");}
        super.hideWindow();

        if (mCandidatesViewManager != null) {
           mCandidatesViewManager.setCandidateMsgRemove();
        }

        cancelToast();
        mDirectInputMode = true;
        hideStatusIcon();

        callCheckDecoEmoji();
        endEmojiAssist();

        mIsKeep = false;

        //clear hide softkeyboard flag
        mOriginalInputMethodSwitcher.mIsHideSelf = false;
    }
    /**
     * Compute the interesting insets into your UI.
     *
     * @see android.inputmethodservice.InputMethodService#onComputeInsets
     */
    @Override public void onComputeInsets(InputMethodService.Insets outInsets) {
        if (getCurrentIme() == null) {
            Log.e("iWnn", "OpenWnn::onComputeInsets()  Unprocessing onCreate() ");
            super.onComputeInsets(outInsets);
            return;
        }
        super.onComputeInsets(outInsets);
        outInsets.contentTopInsets = outInsets.visibleTopInsets;
    }

    /**
     * Called when the user has clicked on the extracted text view.
     *
     * @see android.inputmethodservice.InputMethodService#onExtractedTextClicked
     */
    @Override public void onExtractedTextClicked() {
        // Do nothing.
        // Disable closing candidate view when the user has clicked on the extracted text view.
    }


    /**********************************************************************
     * OpenWnn
     **********************************************************************/
    /**
     * Process an event.
     *
     * @param  ev  An event
     * @return {@code false} always return false.
     */
    public boolean onEvent(OpenWnnEvent ev) {
        return false;
    }

    /**
     * Search a character for toggle input.
     *
     * @param prevChar     The character input previous
     * @param toggleTable  Toggle cycle table
     * @param reverse      {@code false} if toggle direction is forward, {@code true} if toggle direction is backward
     * @return          A character ({@code null} if no character is found).
     */
    protected String searchToggleCharacter(String prevChar, String[] toggleTable, boolean reverse) {
        for (int i = 0; i < toggleTable.length; i++) {
            if (prevChar.equals(toggleTable[i])) {
                if (reverse) {
                    i--;
                    if (i < 0) {
                        return toggleTable[toggleTable.length - 1];
                    } else {
                        return toggleTable[i];
                    }
                } else {
                    i++;
                    if (i == toggleTable.length) {
                        return toggleTable[0];
                    } else {
                        return toggleTable[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Processing of resource release when IME ends.
     */
    protected void close() {
        if (mConverter != null) { mConverter.close(); }
    }

    /**
     * Get the instance of current IME.
     *
     * @return the instance of current IME, See {@link jp.co.omronsoft.iwnnime.ml.OpenWnn}
     */
    public static OpenWnn getCurrentIme() {
        return mCurrentIme;
    }

    /**
     * Get Context.
     *
     * @return the instance of current IME Context or service Context
     */
    public static Context getContext() {
        Context context = IWnnEngineService.getCurrentService();
        if (superGetContext() != null){
            context = superGetContext();
        }

        return context;
    }

    /**
     * Get the value of mComposingText.
     *
     * @return the value of mComposingText
     */
    public ComposingText getComposingText() {
        return mComposingText;
    }

    /**
     * Set input funfun count.
     *
     * @param funfun  funfun count
     */
    public void setFunfun (int funfun) {
        mFunfun = funfun;
        if (mComposingExtraView != null && mFunfun == 0) {
            setFunfunText("");
            mComposingExtraView.requestLayout();
            mComposingExtraView.invalidate();
            mComposingExtraView.setVisibility(View.INVISIBLE);
            mExtraViewFloatingContainer.setVisibility(View.INVISIBLE);
            mFloatingWindowTimer.cancelShowing();
        }
    }

    /**
     * set funfun text.
     *
     * @param text  set text
     */
    public void setFunfunText(CharSequence text) {
       if (mComposingExtraView != null) {
           mComposingExtraView.setComposingExtraText(text.toString());
           mComposingExtraView.requestLayout();
           mFloatingWindowTimer.postShowFloatingWindow();

           if (!mExtraViewFloatingContainer.isShown()) {
               mExtraViewFloatingContainer.setVisibility(View.VISIBLE);
           }

           if (!mComposingExtraView.isShown()) {
               mComposingExtraView.setVisibility(View.VISIBLE);
           }
       }
    }

    /**
     * Get input funfun count.
     *
     * @return input funfun count
     */
    public int getFunfun () {
        return mFunfun;
    }

    /**
     * Set state of keep input.
     *
     * @param in  true if to keep input.
     */
    public void setStateOfKeepInput (boolean in) {
        mIsKeep = in;
    }

    /**
     * Return whether a input is keep.
     *
     * @return Whether a input is keep.
     */
    public boolean isKeepInput () {
        return mIsKeep;
    }

    /**
     * Whether the system is debugging.
     *
     * @return      {@code true} if debugging; {@code false} if not debugging.
     */
    public static boolean isDebugging() {
        return DEBUG;
    }

    /**
     * Open this input method's soft input area, showing it on the display.
     */
    public void requestShowSelf() {
        try {
            Method m = InputMethodService.class.getDeclaredMethod("requestShowSelf", int.class);
            m.setAccessible(true);
            m.invoke(mOriginalInputMethodSwitcher, 0);
        } catch (NoSuchMethodException e) {
            Log.e("OpenWnn", "OpenWnn::requestShowSelf " + e.toString());
        } catch (IllegalAccessException e) {
            Log.e("OpenWnn", "OpenWnn::requestShowSelf " + e.toString());
        } catch (InvocationTargetException e) {
            Log.e("OpenWnn", "OpenWnn::requestShowSelf " + e.toString());
        }
    }

    /**
     * Update the Emoji mode.
     */
    public void setEmojiFlag(boolean emoji) {
        mIsEmoji = emoji;
    }
    /**
     * Whether the Emoji mode.
     * @return {@code true} if Emoji can be input;
     */
    public boolean isEmoji() {
        return mIsEmoji;
    }

    /**
     * Update the Emoji type.
     * @param  emojiType  Emoji Type
     */
    public static void setEmojiType(int emojiType) {
        mEmojiType = emojiType;
    }

    /**
     * Get the Emoji type.
     * @return Emoji Type;
     */
    public static int getEmojiType() {
        return mEmojiType;
    }

    /**
     * Update the Tablet mode.
     *
     * @param  context  Context
     */
    public static void updateTabletMode(Context context) {
        mTabletMode = context.getResources().getBoolean(R.bool.tablet_mode);
    }

    /** Get tablet Mode.
     *
     * @return {@code true} tablet mode {@code false} mobile mode
     */
    public static boolean isTabletMode() {
        return mTabletMode;
    }

    /**
     * Bind service start
     */
    private void decoEmojiBindStart() {
        if (DEBUG) {Log.d("OpenWnn", "decoEmojiBindStart() START");}
        EmojiAssist assist = EmojiAssist.getInstance();
        if (assist != null) {
            int functype = assist.getEmojiFunctionType();
            if (functype == 0) {
                return;
            }
            //#ifdef Only-DOCOMO
            assist.setDisplayPop(true);
            //#endif /* Only-DOCOMO */

        }

        Intent intent = new Intent(IDecoEmojiManager.class.getName());
        boolean success = bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
        if (success) {
            mIsBind = true;
        }
        if (DEBUG) {Log.d("OpenWnn", "decoEmojiBindStart() END");}
    }

    /**
     * Get interface for bind service
     *
     * @return Interface for bind
     */
    public IDecoEmojiManager getDecoEmojiBindInterface() {
        if (DEBUG) {Log.d("OpenWnn", "getDecoEmojiBindInterface() START");}
        return mDecoEmojiInterface;
    }

    /**
     * Call DecoEmojiManager bind function
     */
    private void callCheckDecoEmoji() {
        if (DEBUG) {Log.d("OpenWnn", "callCheckDecoEmoji() START");}

        //#ifdef Only-DOCOMO
        DecoEmojiUtil.getDecoEmojiDicInfo(getPreferenceId());
        //#endif /* Only-DOCOMO */

        if (DEBUG) {Log.d("OpenWnn", "callCheckDecoEmoji() END");}
    }

    /**
     * Get EmojiAssist Instance.
     *
     * @return EmojiAssist Instance
     */
    public EmojiAssist getEmojiAssist() {
        if (mEmojiAssist == null) {
            mEmojiAssist = EmojiAssist.getInstance();
        }
        return mEmojiAssist;
    }

    /**
     * Start For EmojiAssist Animation.
     */
    public void startEmojiAssist() {
        if (mEmojiAssist == null) {
            mEmojiAssist = EmojiAssist.getInstance();
        }
        mEmojiAssist.setPictureScale(true);
        mIsEmojiAssistWorking = true;
        mEmojiAssist.startAnimation();
    }

    /**
     * End For EmojiAssist Animation.
     */
    public void endEmojiAssist() {
        if (mEmojiAssist == null) {
            mEmojiAssist = EmojiAssist.getInstance();
        }
        mEmojiAssist.stopAnimation();
        mIsEmojiAssistWorking = false;
    }

    /** Extract EditText Instance. */
    private ExtractEditText extractEditText;

    /**
     * Called by the system when IME fullscreen keyboard.
     *
     * @see android.inputmethodservice.InputMethodService#onCreateExtractTextView
     */

    @Override public View onCreateExtractTextView() {

        View v = super.onCreateExtractTextView();
        EmojiAssist assist = EmojiAssist.getInstance();
        assist.removeView(extractEditText);

        ViewGroup view = (ViewGroup)v;
        extractEditText = (ExtractEditText)view.findViewById(android.R.id.inputExtractEditText);
        if (extractEditText != null) {

            Bundle bundle = mAttribute.extras;
            if (bundle != null) {
                boolean allowEmoji = bundle.getBoolean("allowEmoji");
                boolean allowDecoEmoji = bundle.getBoolean("allowDecoEmoji");
                Bundle extraBundle = extractEditText.getInputExtras(true);
                extraBundle.putBoolean("allowEmoji", allowEmoji);
                extraBundle.putBoolean("allowDecoEmoji", allowDecoEmoji);
                if (allowEmoji || allowDecoEmoji) {
                    assist.removeView(extractEditText);
                    assist.addView(extractEditText);
                }
            }
            mIsFullScreenChanged = true;

            /* Text changed listener for the reading text */
            extractEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Spannable text = new SpannableString(s);
                    int ret = getEmojiAssist().checkTextData(text);
                    TextCandidatesViewManager textCandidate = (TextCandidatesViewManager)mCandidatesViewManager;
                    if (ret > EmojiAssist.TYPE_TEXT_EMOJI) {
                        startEmojiAssist();
                    } else if (textCandidate.checkDecoEmoji() == false) {
                        endEmojiAssist();
                    }
                }
            });
        }
        return v;
    }

    /**
     * Set full screen change flag.
     */
    public void setFullScreenChanged() {
        mIsFullScreenChanged = false;
    }

    /**
     * Get full screen change flag.
     */
    public boolean isFullScreenChanged() {
        return mIsFullScreenChanged;
    }

    /**
     * Get PreferenceId.
     *
     * @return PreferenceId
     */
    private int getPreferenceId() {
        return getIntFromNotResetSettingsPreference(OpenWnn.superGetContext(),
                                                    DecoEmojiListener.PREF_KEY, -1);
    }

    /**
     * Regist broadcast receiver of screen on/off
     */
    private void registReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().registerReceiver(mReceiver, filter);
    }

    /**
     * Unregist broadcast receiver of screen on/off
     */
    private void unregistReceiver() {
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    /**
     * Copies a preset DecoEmoji pseudo dictionary.
     */
    public static void copyPresetDecoEmojiGijiDictionary() {
        if (DEBUG) {Log.d("OpenWnn", "copyPresetDecoEmojiGijiDictionary()");}

        File decoEmojiGijiDic
                = new File(getFilesDirPath(getContext()) + DECOEMOJI_GIJI_DICTIONALY_PATH + DECOEMOJI_GIJI_DICTIONALY_NAME);
        if (!decoEmojiGijiDic.exists()) {
            File presetDecoEmojiGijiDic = new File(
                    Environment.getRootDirectory() + "/etc/" + DECOEMOJI_GIJI_DICTIONALY_NAME);
            if (presetDecoEmojiGijiDic.exists()) {
                FileChannel channelSrc = null;
                FileChannel channelDest = null;

                try {
                    File folder = new File(decoEmojiGijiDic.getParent());
                    boolean mkdirret = folder.mkdirs();
                    if (!mkdirret) {
                        Log.e("OpenWnn", "Fail to copy preset DecoEmoji pseudo dictionary : mkdir fail!!");
                        return;
                    }
                    channelSrc = new FileInputStream(presetDecoEmojiGijiDic).getChannel();
                    channelDest = new FileOutputStream(decoEmojiGijiDic).getChannel();

                    channelSrc.transferTo(0, channelSrc.size(), channelDest);
                } catch (IOException e) {
                    Log.e("OpenWnn", "Fail to copy preset DecoEmoji pseudo dictionary", e);
                } finally {
                    closeChannel(channelSrc);
                    closeChannel(channelDest);
                }
            } else {
                if (DEBUG) {Log.d("OpenWnn", "  Preset DecoEmoji pseudo dictionary is not found");}
            }
        } else {
            if (DEBUG) {Log.d("OpenWnn", "  DecoEmoji pseudo dictionary is found");}
        }
    }

    /**
     * Closes an open channel.
     */
    private static void closeChannel(FileChannel channel) {
        if (DEBUG) {Log.d("OpenWnn", "closeChannel()");}

        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                Log.e("OpenWnn", "Fail to close FileChannel", e);
            }
        }
    }

    public boolean isSubtypeExtraEmojiInput() {
        InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        String subtypeExtra = manager.getCurrentInputMethodSubtype().getExtraValue();
        if (subtypeExtra.equals(SUBTYPE_EMOJI_INPUT_EXTRA_VALUE)) {
            return true;
        }
        return false;
    }

    /**
     * Wrapper of SharedPreferences.getInt(). From not_reset_settings_pref.xml.
     *
     * @param context   Context
     * @param key       The name of the preference to retrieve.
     * @param defValue  Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not an int.
     */
    public static int getIntFromNotResetSettingsPreference(Context context, String key, int defValue) {
        if (context == null) {
            return defValue;
        }
        SharedPreferences pref = context.getSharedPreferences(FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);

        int ret = defValue;
        if (pref.contains(key)) {
            ret = pref.getInt(key, defValue);
        } else if (defaultPref.contains(key)) {
            ret = defaultPref.getInt(key, defValue);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(key, ret);
            editor.commit();
        }

        if (defaultPref.contains(key)) {
            SharedPreferences.Editor defaultEditor = defaultPref.edit();
            defaultEditor.remove(key);
            defaultEditor.commit();
        }
        return ret;
    }

    /**
     * Wrapper of SharedPreferences.getBoolean(). From not_reset_settings_pref.xml.
     *
     * @param context   Context
     * @param key       The name of the preference to retrieve.
     * @param defValue  Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a boolean.
     */
    public static boolean getBooleanFromNotResetSettingsPreference(Context context, String key, boolean defValue) {
        if (context == null) {
            return defValue;
        }
        SharedPreferences pref = context.getSharedPreferences(FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean ret = defValue;
        if (pref.contains(key)) {
            ret = pref.getBoolean(key, defValue);
        } else if (defaultPref.contains(key)) {
            ret = defaultPref.getBoolean(key, defValue);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(key, ret);
            editor.commit();
        }

        if (defaultPref.contains(key)) {
            SharedPreferences.Editor defaultEditor = defaultPref.edit();
            defaultEditor.remove(key);
            defaultEditor.commit();
        }
        return ret;
    }

    /**
     * Wrapper of SharedPreferences.getString(). From not_reset_settings_pref.xml.
     *
     * @param context   Context
     * @param key       The name of the preference to retrieve.
     * @param defValue  Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws ClassCastException if there is a preference with this name that is not a String.
     */
    public static String getStringFromNotResetSettingsPreference(Context context, String key, String defValue) {
        if (context == null) {
            return defValue;
        }
        SharedPreferences pref = context.getSharedPreferences(FILENAME_NOT_RESET_SETTINGS_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);

        String ret = defValue;
        if (pref.contains(key)) {
            ret = pref.getString(key, defValue);
        } else if (defaultPref.contains(key)) {
            ret = defaultPref.getString(key, defValue);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(key, ret);
            editor.commit();
        }

        if (defaultPref.contains(key)) {
            SharedPreferences.Editor defaultEditor = defaultPref.edit();
            defaultEditor.remove(key);
            defaultEditor.commit();
        }
        return ret;
    }

    /**
     * delete SurrogateText.
     *
     * @param keyCode Key Code.
     */
    public void deleteSurrogateText(int keyCode) {
        switch (keyCode) {
            case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
            case DefaultSoftKeyboard.KEYCODE_JP12_BACKSPACE:
            case DefaultSoftKeyboard.KEYCODE_4KEY_CLEAR:
                InputConnection ic = getCurrentInputConnection();
                if (null != ic) {
                    final CharSequence lastChar = ic.getTextBeforeCursor(1, 0);
                    if (!TextUtils.isEmpty(lastChar) && Character.isHighSurrogate(lastChar.charAt(0))) {
                        ic.deleteSurroundingText(1, 0);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Gets user directory path.
     *
     * @return iWnn user data directory path.
     */
    public String getFilesDirPath() {
        File dir = getFilesDir();
        if (dir == null) {
            return null;
        }
        return dir.getPath();
    }

    /**
     * Gets user directory path.
     *
     * @param context Context
     * @return user data directory path.
     */
    public static String getFilesDirPath(Context context) {
        if (context == null) {
            return null;
        }
        File dir = context.getFilesDir();
        if (dir == null) {
            return null;
        }
        return dir.getPath();
    }

    /**
     * Used to show the floating window class.
     *
     */
    private class PopupTimer extends Handler implements Runnable {
        private int mParentLocation[] = new int[2];

        /**
        * Show floating window.
        *
        */
        void postShowFloatingWindow() {
            mExtraViewFloatingContainer.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mFloatingWindow.setWidth(mExtraViewFloatingContainer.getMeasuredWidth());
            mFloatingWindow.setHeight(mExtraViewFloatingContainer.getMeasuredHeight());
            post(this);
        }

        /**
        * Close window.
        *
        */
        void cancelShowing() {
            if (mFloatingWindow.isShowing()) {
                mFloatingWindow.dismiss();
            }
            removeCallbacks(this);
        }

        /**
        * Run method.
        *
        */
        public void run() {
            View view = (View)mCandidatesViewManager.getCurrentView();
            view.getLocationInWindow(mParentLocation);

            int locationAdjustY = getResources().getDimensionPixelSize(
                    R.dimen.candidate_blank_area_height);
            if (getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                int mCandidateMinimumHeight = getResources().getDimensionPixelSize(
                        R.dimen.cand_minimum_height);
                int mCandidateBgViewPaddingTop = getResources().getDimensionPixelSize(
                        R.dimen.cand_base_view_padding_top);
                locationAdjustY = locationAdjustY - mCandidateMinimumHeight
                        - mCandidateBgViewPaddingTop;
            }

            if (!mFloatingWindow.isShowing()) {
                mFloatingWindow.showAtLocation(view,
                                               Gravity.LEFT | Gravity.TOP,
                                               mParentLocation[0],
                                               mParentLocation[1] - mFloatingWindow.getHeight()
                                               + locationAdjustY);
            } else {
                mFloatingWindow.update(mParentLocation[0],
                                       mParentLocation[1] - mFloatingWindow.getHeight()
                                       + locationAdjustY,
                                       mFloatingWindow.getWidth(),
                                       mFloatingWindow.getHeight());
            }
        }
    }
    /**
     * Get Inputyype EditorInfo.
     *
     * @return EditorInfo
     */
    public EditorInfo getEditorInfo() {
        return mAttribute;
    }

    /**
     * Set mIsUnlockReceived flag.
     *
     * @param set set value
     */
    public void setIsUnlockReceived(boolean set) {
        mIsUnlockReceived = set;
    }

    /**
     * Call Toast.cancel().
     *
     */
    public void cancelToast() {
        if (mSelectLangToast != null) {
            mSelectLangToast.cancel();
            mSelectLangToast = null;
        }
    }

    /**
     * Free resources before finalizing this instance.
     *
     */
    public void freeResources() {
        if (mIsBind) {
            unbindService(mServiceConn);
            mIsBind = false;
        }
        unregistReceiver();
    }
}
