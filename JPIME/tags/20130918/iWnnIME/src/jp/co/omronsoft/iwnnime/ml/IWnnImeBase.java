/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import java.io.UnsupportedEncodingException;

/**
 * The iWnn base class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class IWnnImeBase {

    /** [For debugging] TAG character string of debugging log */
    protected static final String TAG = "OpenWnn";

    /**
     * Mode of the convert engine (Emoji)
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL_EMOJI = 107;

    /**
     * Mode of the convert engine (Symbol)
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL_SYMBOL = 108;

    /**
     * Mode of the convert engine (Kao Moji)
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL_KAO_MOJI = 109;

    /**
     * Mode of the convert engine (DecoEmoji)
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL_DECOEMOJI = 110;

    /**
     * Mode of the convert engine (Additional Symbol)
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL_ADD_SYMBOL = 111;

    /**
     * Mode of the convert engine (unicode6 Emoji)
     * Use with {@code OpenWnn.CHANGE_MODE} event.
     */
    public static final int ENGINE_MODE_SYMBOL_EMOJI_UNI6 = 112;


    /** Switcher, See {@link jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher} */
    protected IWnnLanguageSwitcher mWnnSwitcher = null;

    /** Editor selection start position  */
    protected int mEditorSelectionStart = 0;

    /** Editor selection end position  */
    protected int mEditorSelectionEnd = 0;

    /** Key of first launch Voice Input */
    protected static final String PREF_HAS_USED_VOICE_INPUT = "has_used_voice_input";

    /** True when Voice Input lanched at least one time */
    protected boolean mHasUsedVoiceInput;

    /** True when Voice Input is recognizing. */
    protected boolean mRecognizing;

    /** Voice Input warning dialog. */
    protected AlertDialog mVoiceWarningDialog;

    /** True when commit by voice input */
    protected boolean mHasCommitedByVoiceInput = false;

    /** Current conversion type letters */
    private int mConvertingForFuncKeyType = iWnnEngine.CONVERT_TYPE_NONE;

    /** MushRoomPlus(WnnConnector) Setting */
    protected boolean mIsGetAllText = false;;

    /** MushRoomPlus(WnnConnector) Get Text Type */
    protected boolean mIsGetTextType = false;

    /** Whether fullscreen mode is active or not */
    protected boolean mEnableFullscreen = false;

    /** Number of additional characters of Wildcard input. The number of uninput characters (*) to count.  */
    protected int mFunfun = 0;

    /** Highlight color style for the toggle character */
    protected CharacterStyle mSpanToggleCharacterBgHl = null;

    /** Highlight toggle character color */
    protected CharacterStyle mSpanToggleCharacterText = null;

    /** Last toggle character when the input type is TYPE_NULL */
    protected char mLastToggleCharTypeNull = 0;

    /** Asynchronous handlers to commit a string from the Mushroom */
    protected CharSequence mMushroomResultString = null;

    /** Asynchronous handlers to result type  from the Mushroom */
    protected boolean mMushroomResulttype = false;

    /** Editor selection start position for Mushroom */
    protected int mMushroomSelectionStart = 0;

    /** Editor selection end position for Mushroom */
    protected int  mMushroomSelectionEnd = 0;


    /**
     * Target layer of the {@link ComposingText}
     * <br>
     *
     * [Purpose to use]
     * <br>
     * * fix the target layer for composing text.
     * <br>
     *
     * [Timing to update]
     * <br>
     * * Set COMPOSING_TEXT_LAYER1, when start conversion
     * <br>
     * * Set COMPOSING_TEXT_LAYER2, when start prediction
     */
    protected int mTargetLayer = 1;

    /** IME's status */
    protected int mStatus = STATUS_INIT;

    /** IME's status for {@code mStatus} input/no candidates. */
    protected static final int STATUS_INIT = 0x0000;

    /** IME's status for {@code mStatus}(input characters).
     * <br>
     * When the toggle is inputted, the toggle is done only for mStatus == STATUS_INPUT.
     */
    protected static final int STATUS_INPUT = 0x0001;

    /** IME's status for {@code mStatus}(input functional keys).
     * <br>
     * (It becomes not the toggle because of no input status but the following character input. )
     */
    protected static final int STATUS_INPUT_EDIT      = 0x0003;

    /** IME's status for {@code mStatus}(all candidates are displayed).
     * <br>
     * (It is unused though updates. )
     */
    protected static final int STATUS_CANDIDATE_FULL  = 0x0010;

    /** Highlight color style for the converted clause */
    protected static final CharacterStyle SPAN_CONVERT_BGCOLOR_HL  = new BackgroundColorSpan(0xFF6ED8FF);

    /** Highlight color style for the selected string  */
    protected static final CharacterStyle SPAN_EXACT_BGCOLOR_HL  = new BackgroundColorSpan(0xFF66CDAA);

    /** Highlight color style for EISU-KANA conversion */
    protected static final CharacterStyle SPAN_EISUKANA_BGCOLOR_HL  = new BackgroundColorSpan(0xFF9FB6CD);

    /** Highlight color style for the composing text */
    protected static final CharacterStyle SPAN_REMAIN_BGCOLOR_HL  = new BackgroundColorSpan(0xFFF0FFFF);

    /** Highlight text color */
    protected static final CharacterStyle SPAN_TEXTCOLOR  = new ForegroundColorSpan(0xFF000000);

    /** Underline style for the composing text */
    protected static final CharacterStyle SPAN_UNDERLINE   = new UnderlineSpan();

    /**
     * Constructor
     */
    public IWnnImeBase(IWnnLanguageSwitcher wnn) {
        mWnnSwitcher = wnn;
        Resources res = getResources();
        mSpanToggleCharacterBgHl = new BackgroundColorSpan(res.getInteger(R.integer.span_color_toggle_character_bg_hl));
        mSpanToggleCharacterText = new ForegroundColorSpan(res.getInteger(R.integer.span_color_toggle_character_text));
    }

    /***********************************************************************
     * To use interface to override
     **********************************************************************/
    /**
     * Called by the system when the service is first created.
     *
     * @see android.inputmethodservice.InputMethodService#onCreate
     */
    public void onCreate() {}

    /**
     * Create and return the view hierarchy used to show candidates.
     *
     * @see android.inputmethodservice.InputMethodService#onCreateCandidatesView
     */
    public View onCreateCandidatesView() {
        return null;
    }

    /**
     * Call OpenWnn.onCreate().
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#onCreateOpenWnn
     * */
    public void onCreateOpenWnn() {
        mWnnSwitcher.onCreateOpenWnn();
    }

    /**
     * Create and return the view hierarchy used for the input area
     * (such as a soft keyboard).
     *
     * @see android.inputmethodservice.InputMethodService#onCreateInputView
     */
    public View onCreateInputView() {
        /* Unused! because onCreateInputView() of OpenWnn is executed with Switcher. */
        return null;
    }

    /**
     * Create and return the view hierarchy used for the input area
     * (such as a soft keyboard).
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#onCreateInputView
     */
    public View onCreateInputViewOpenWnn() {
        return mWnnSwitcher.onCreateInputView();
    }

    /**
     * Called to inform the input method that text input has started in an editor.
     *
     * @see android.inputmethodservice.InputMethodService#onStartInput
     */
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        CharSequence result = MushroomControl.getInstance().getResultString();
        Boolean type = MushroomControl.getInstance().getResultType();
        if (result != null) {
            mMushroomResultString = result;
            mMushroomResulttype = type;
            mWnnSwitcher.requestShowSelf();
        }

        mEditorSelectionStart = attribute.initialSelStart;
        mEditorSelectionEnd = attribute.initialSelEnd;
    }

    /**
     * Called when the input view is being shown and input has started on a new editor.
     *
     * @see android.inputmethodservice.InputMethodService#onStartInputView
     */
    public void onStartInputView(EditorInfo attribute, boolean restarting) {}

    /**
     * Called when the input view is being shown and input has started on a new editor.
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#onStartInputViewOpenWnn
     */
    public void onStartInputViewOpenWnn(EditorInfo attribute, boolean restarting) {
        mWnnSwitcher.onStartInputViewOpenWnn(attribute, restarting);
    }

    /**
     * Take care of handling configuration changes.
     *
     * @see android.inputmethodservice.InputMethodService#onConfigurationChanged
     */
    public void onConfigurationChanged(Configuration newConfig) {}

    /**
     * Call OpenWnn.onConfigurationChanged().
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#onConfigurationChangedOpenWnn
     */
    public void onConfigurationChangedOpenWnn(Configuration newConfig) {
        mWnnSwitcher.onConfigurationChangedOpenWnn(newConfig);
    }

    /**
     * Call hideWindow().
     *
     * @see android.inputmethodservice.InputMethodService#hideWindow
     */
    public void hideWindow() {}

    /**
     * Call OpenWnn.hideWindow().
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#hideWindowOpenWnn
     */
    public void hideWindowOpenWnn() {
        mWnnSwitcher.hideWindowOpenWnn();
    }

    /**
     * Called when the application has reported a new selection region of the text.
     *
     * @see android.inputmethodservice.InputMethodService#onUpdateSelection
     */
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {}

    /**
     * Call OpenWnn.onUpdateSelection().
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#onUpdateSelectionOpenWnn
     */
    public void onUpdateSelectionOpenWnn(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        mWnnSwitcher.onUpdateSelectionOpenWnn(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        mEditorSelectionStart = newSelStart;
        mEditorSelectionEnd = newSelEnd;
    }

    /**
     * Process an event.
     *
     * @param  ev  An event
     * @return {@code false}; always return false.
     */
    synchronized public boolean onEvent(OpenWnnEvent ev) {
        return false;
    }

    /**
     * Control when the input method should run in fullscreen mode.
     *
     * @see android.inputmethodservice.InputMethodService#onEvaluateFullscreenMode
     */
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    /**
     * Call OpenWnn.onEvaluateFullscreenMode().
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#onEvaluateFullscreenMode
     */
    public boolean onEvaluateFullscreenModeOpenWnn() {
        return mWnnSwitcher.onEvaluateFullscreenModeOpenWnn();
    }

    /**
     * Control when the soft input area should be shown to the user.
     *
     * @see android.inputmethodservice.InputMethodService#onEvaluateInputViewShown
     */
    public boolean onEvaluateInputViewShown() {
        return false;
    }

    /**
     * Called to inform the input method that text input has finished in the last editor.
     *
     * @see android.inputmethodservice.InputMethodService#onFinishInput
     */
    public void onFinishInput() {}

    /**
     * Call OpenWnn.onFinishInput().
     *
     * @see jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher#onFinishInputOpenWnn
     */
    public void onFinishInputOpenWnn() {
        mWnnSwitcher.onFinishInputOpenWnn();
    }

    /**
     * Processing of resource open when IME ends.
     *
     * @param now  Whether it's executed immediately
     */
    public void close(boolean now) {}

    /***********************************************************************
     * Call InputMethodService
     **********************************************************************/
    /**
     * Return a Resources instance for your application's package.
     *
     * @see android.inputmethodservice.InputMethodService#getResources
     */
    protected Resources getResources() {
        return mWnnSwitcher.getResources();
    }

    /**
     * Return whether the soft input view is currently shown to the user.
     *
     * @see android.inputmethodservice.InputMethodService#isInputViewShown
     */
    protected boolean isInputViewShown() {
        return mWnnSwitcher.isInputViewShown();
    }

    /**
     * Replaces the current input view with a new one.
     *
     * @see android.inputmethodservice.InputMethodService#setInputView
     */
    protected void setInputView(View view) {
        mWnnSwitcher.setInputView(view);
    }

    /**
     * Send the given key event code (as defined by KeyEvent) to the
     * current input connection is a key down + key up event pair.
     *
     * @see android.inputmethodservice.InputMethodService#sendDownUpKeyEvents
     */
    protected void sendDownUpKeyEvents(int keyEventCode) {
        mWnnSwitcher.sendDownUpKeyEvents(keyEventCode);
    }

    /**
     * Send the given UTF-16 character to the current input connection.
     *
     * @see android.inputmethodservice.InputMethodService#sendKeyChar
     */
    protected void sendKeyChar(char charCode) {
        mWnnSwitcher.sendKeyChar(charCode);
    }

    /**
     * Re-evaluate whether the input method should be running in
     * fullscreen mode, and update its UI if this has changed since
     * the last time it was evaluated.
     *
     * @see android.inputmethodservice.InputMethodService#updateFullscreenMode
     */
    protected void updateFullscreenMode() {
        mWnnSwitcher.updateFullscreenMode();
    }

    /**
     * Get the current input editor information.
     *
     * @see android.inputmethodservice.InputMethodService#getCurrentInputEditorInfo()
     */
    protected EditorInfo getCurrentInputEditorInfo() {
        return mWnnSwitcher.getCurrentInputEditorInfo();
    }

    /**
     * Close this input method's soft input area, and remove it from the display.
     *
     * @see android.inputmethodservice.InputMethodService#requestHideSelf
     */
    protected void requestHideSelf(int flag) {
        mWnnSwitcher.requestHideSelf(flag);
    }

    /**
     * Search a character for toggle input.
     *
     * @param prevChar     The character input previous
     * @param toggleTable  Toggle cycle table
     * @param reverse      {@code false} if toggle direction is forward, {@code true} if toggle direction is backward
     * @return          A character ({@code null} if no character is found)
     * @see jp.co.omronsoft.iwnnime.ml.OpenWnn#searchToggleCharacter
     */
    protected String searchToggleCharacter(String prevChar, String[] toggleTable, boolean reverse) {
        return mWnnSwitcher.searchToggleCharacter(prevChar, toggleTable, reverse);
    }

    /**
     * Retrieve the currently active InputConnection that is bound to the input method,
     * or null if there is none.
     *
     * @see android.inputmethodservice.InputMethodService#getCurrentInputConnection
     */
    protected InputConnection getCurrentInputConnection() {
        return mWnnSwitcher.getCurrentInputConnection();
    }

    /***********************************************************************
     * Setter and Getter
     **********************************************************************/
    /**
     * IWnnLanguageSwitcher getter.
     *
     * @return IWnnLanguageSwitcher: Switcher
     */
    public IWnnLanguageSwitcher getSwitcher() {
        return mWnnSwitcher;
    }

    /**
     * Candidate view setter.
     *
     * @param candidatesViewManager  Candidate view
     */
    public void setCandidatesViewManager(CandidatesViewManager candidatesViewManager) {
        mWnnSwitcher.mCandidatesViewManager = candidatesViewManager;
    }

    /**
     * Candidate view getter.
     *
     * @return mCandidatesViewManager: Candidate view
     */
    public CandidatesViewManager getCandidatesViewManager() {
        return mWnnSwitcher.mCandidatesViewManager;
    }

    /**
     * Input view (software keyboard) setter.
     *
     * @param inputViewManager  Input view (software keyboard)
     */
    public void setInputViewManager(InputViewManager inputViewManager) {
        mWnnSwitcher.mInputViewManager = inputViewManager;
    }

    /**
     * Input view (software keyboard) getter.
     *
     * @return mInputViewManager: Input view (software keyboard)
     */
    public InputViewManager getInputViewManager() {
        return mWnnSwitcher.mInputViewManager;
    }

    /**
     * Conversion engine setter.
     *
     * @param converter  Conversion engine
     */
    public void setConverter(WnnEngine converter) {
        mWnnSwitcher.mConverter = converter;
    }

    /**
     * Conversion engine getter.
     *
     * @return mConverter: Conversion engine
     */
    public WnnEngine getConverter() {
        return mWnnSwitcher.mConverter;
    }

    /**
     * Pre-converter setter.
     *
     * @param preConverter  Pre-converter
     */
    public void setPreConverter(LetterConverter preConverter) {
        mWnnSwitcher.mPreConverter = preConverter;
    }

    /**
     * Pre-converter getter.
     *
     * @return mPreConverter: Pre-converter
     */
    public LetterConverter getPreConverter() {
        return mWnnSwitcher.mPreConverter;
    }

    /**
     * The inputing/editing string setter.
     *
     * @param composingText  The inputing/editing string
     */
    public void setComposingText(ComposingText composingText) {
        mWnnSwitcher.mComposingText = composingText;
    }

    /**
     * The inputing/editing string getter.
     *
     * @return mComposingText: The inputing/editing string
     */
    public ComposingText getComposingText() {
        return mWnnSwitcher.mComposingText;
    }

    /**
     * Auto hide candidate view setter.
     *
     * @param autoHideMode  Auto hide candidate view
     */
    public void setAutoHideMode(boolean autoHideMode) {
        mWnnSwitcher.mAutoHideMode = autoHideMode;
    }

    /**
     * Auto hide candidate view getter.
     *
     * @return mAutoHideMode: Auto hide candidate view
     */
    public boolean isAutoHideMode() {
        return mWnnSwitcher.mAutoHideMode;
    }

    /**
     * Direct input mode setter.
     *
     * @param directInputMode  Direct input mode
     */
    public void setDirectInputMode(boolean directInputMode) {
        mWnnSwitcher.mDirectInputMode = directInputMode;
    }

    /**
     * Direct input mode getter.
     *
     * @return mDirectInputMode: Direct input mode
     */
    public boolean isDirectInputMode() {
        return mWnnSwitcher.mDirectInputMode;
    }

    /**
     * The input connection setter.
     *
     * @param inputConnection  The input connection
     */
    public void setInputConnection(InputConnection inputConnection) {
        mWnnSwitcher.mInputConnection = inputConnection;
    }

    /**
     * The input connection getter.
     *
     * @return mInputConnection: The input connection
     */
    public InputConnection getInputConnection() {
        return mWnnSwitcher.mInputConnection;
    }

    /**
     * Initialize the displayed screen by IME
     */
    protected void initializeScreen() {}

    /**
     * Call Mushroom Luncher.
     *
     * @param  word  Old String
     */
    protected void callMushRoom(WnnWord word) {
        CharSequence oldString;
        mIsGetTextType = false;
        mMushroomSelectionStart = 0;
        mMushroomSelectionEnd  = 0;
        Resources res = getResources();
        mIsGetAllText = res.getBoolean(R.bool.mushroomplus_get_text_value);

        CandidatesViewManager candidatesViewManager = getCandidatesViewManager();
        if (candidatesViewManager.isFocusCandidate()) {
            word = candidatesViewManager.getFocusedWnnWord();
        }

        if (word == null) {
            ComposingText composingText = getComposingText();
            if (composingText.size(ComposingText.LAYER2) != 0) {
                oldString = DecoEmojiUtil.convertComposingToCommitText(composingText,
                        ComposingText.LAYER2, 0, composingText.size(ComposingText.LAYER2) - 1).toString();
            } else if (composingText.size(ComposingText.LAYER1) != 0) {
                oldString = composingText.toString(ComposingText.LAYER1);
            } else {
                InputConnection inputConnection = getInputConnection();
                if (inputConnection != null) {
                    if (mEditorSelectionStart != mEditorSelectionEnd) {
                        inputConnection.setSelection(Math.max(mEditorSelectionStart, 0),
                                                    Math.max(mEditorSelectionStart, 0));
                        if (mEditorSelectionStart < mEditorSelectionEnd) {
                            oldString = inputConnection.getTextAfterCursor(mEditorSelectionEnd - mEditorSelectionStart, InputConnection.GET_TEXT_WITH_STYLES);
                        } else {
                            oldString = inputConnection.getTextBeforeCursor(mEditorSelectionStart - mEditorSelectionEnd, InputConnection.GET_TEXT_WITH_STYLES);
                        }
                        mMushroomSelectionStart = mEditorSelectionStart;
                        mMushroomSelectionEnd  = mEditorSelectionEnd;
                        inputConnection.setSelection(Math.max(mEditorSelectionStart, 0),
                                                    Math.max(mEditorSelectionEnd, 0));
                    } else {
                        if (mIsGetAllText) {
                            CharSequence before = inputConnection.getTextBeforeCursor(Integer.MAX_VALUE / 2, InputConnection.GET_TEXT_WITH_STYLES);
                            CharSequence after = inputConnection.getTextAfterCursor(Integer.MAX_VALUE / 2, InputConnection.GET_TEXT_WITH_STYLES);

                            SpannableStringBuilder text = new SpannableStringBuilder();
                            if (before != null) {
                                text.append(before);
                            }
                            if (after != null) {
                                text.append(after);
                            }
                            oldString = text;
                            mIsGetTextType = true;
                        } else {
                            oldString = "";
                        }
                    }
                } else {
                    oldString = "";
                }
            }
        } else {
            if (((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI) != 0)
                    || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NO_CANDIDATE) != 0)
                    || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN) != 0)) {
                oldString = "";
            } else {
                byte[] charArray;
                String normal = "";
                try {
                    charArray = word.candidate.toString().getBytes(OpenWnn.CHARSET_NAME_UTF8);
                    byte[] charArrayNormalTemp = new byte[charArray.length] ;
                    int j =0;
                    int last = charArray.length;
                    for (int i = 0; i < last;) {
                        if (charArray[i] == DecoEmojiUtil.DECOEMOJI_ID_PREFIX) {
                            i += DecoEmojiUtil.DECOEMOJI_ID_LENGTH;
                        } else { // normal string
                            charArrayNormalTemp[j] = charArray[i];
                            j++;
                            i++;
                         }
                    }
                    byte[] charArrayNormal = new byte[j] ;
                    for (int k = 0; k < charArrayNormal.length;) {
                        charArrayNormal[k] = charArrayNormalTemp[k];
                        k++;
                    }
                    normal = new String(charArrayNormal, OpenWnn.CHARSET_NAME_UTF8);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                oldString = normal;
            }
        }

        initializeScreen();
        requestHideSelf(0);
        MushroomControl.getInstance().startMushroomLauncher(oldString,mIsGetTextType);
    }

    /**
     * Set input funfun count.
     *
     * @param funfun    input funfun count
     */
    public void setFunFun (int funfun) {
        OpenWnn wnn = OpenWnn.getCurrentIme();
        if (wnn != null) {
            wnn.setFunfun(funfun);
        }
    }

    /**
     * Commit Mushroom string.
     */
    protected void commitMushRoomString() {
        InputConnection inputConnection = getInputConnection();

        PowerManager pm = (PowerManager) mWnnSwitcher.getSystemService(Context.POWER_SERVICE);
        KeyguardManager keyguard = (KeyguardManager) mWnnSwitcher.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isScreenLock = keyguard.inKeyguardRestrictedInputMode();
        if (mMushroomResultString != null && inputConnection != null && pm.isScreenOn() && !isScreenLock) {
            if (1 <= mMushroomResultString.length()) {
                if (mIsGetTextType && mMushroomResulttype) {
                    inputConnection.deleteSurroundingText (Integer.MAX_VALUE / 2,Integer.MAX_VALUE / 2);
                    mIsGetTextType = false;
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
                inputConnection.commitText(mMushroomResultString, 1);
            }
            mMushroomResultString = null;
            mWnnSwitcher.setIsUnlockReceived(false);
        }
    }

    /**
     * Show Voice Input warning dialog.
     *
     * @param swipe whether this voice input was started by swipe, for logging purposes
     */
    protected void showVoiceWarningDialog(final boolean swipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(OpenWnn.superGetContext());
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_mic_dialog);
        builder.setPositiveButton(R.string.ti_dialog_button_ok_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                reallyStartShortcutIME();
            }
        });
        builder.setNegativeButton(R.string.ti_dialog_button_cancel_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        String message = getResources().getString(R.string.ti_voice_warning_may_not_understand_docomo_txt) + "\n\n" +
            getResources().getString(R.string.ti_voice_warning_how_to_turn_off_txt);
        builder.setMessage(message);

        builder.setTitle(R.string.ti_voice_warning_title_txt);
        mVoiceWarningDialog = builder.create();

        Window window = mVoiceWarningDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = getInputViewManager().getCurrentView().getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mVoiceWarningDialog.show();
    }

    /**
     * If Voice Input first launch, show Voice Input warning dialog.
     *
     * @param swipe whether this voice input was started by swipe, for logging purposes
     */
    protected void startShortcutIME() {
        if (!mHasUsedVoiceInput) {
            // Calls reallyStartListening if user clicks OK, does nothing if user clicks Cancel.
            showVoiceWarningDialog(false);
        } else {
            reallyStartShortcutIME();
        }
    }

    /**
     * Start Voice Input IME.
     *
     * @param swipe whether this voice input was started by swipe, for logging purposes
     */
    protected void reallyStartShortcutIME() {
        if (!mHasUsedVoiceInput) {
            // The user has started a voice input, so remember that in the
            // future (so we don't show the warning dialog after the first run).
            SharedPreferences.Editor editor =
                    PreferenceManager
                    .getDefaultSharedPreferences(OpenWnn.superGetContext()).edit();
            editor.putBoolean(PREF_HAS_USED_VOICE_INPUT, true);
            editor.commit();
            mHasUsedVoiceInput = true;
        }

        // Clear N-best suggestions
        initializeScreen();

        getSwitcher().updateShortcutIME();
        getSwitcher().switchToShortcutIME();
    }

    /**
     * If vibration setting is enabled, get vibrator.
     *
     * @param preference shared preference
     * @return vibrator
     */
    protected Vibrator getVibrator(SharedPreferences preference) {
        Vibrator vibrator = null;
        try {
            if (preference.getBoolean("key_vibration", getResources().getBoolean(R.bool.key_vibration_default_value))) {
                vibrator = (Vibrator)getSwitcher().getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception ex) {
            Log.d(TAG, "NO VIBRATOR");
        }
        return vibrator;
    }

    /**
     * Commit voice result.
     *
     * @param result  Result string.
     */
    protected void commitVoiceResult(String result) {}

    /**
     * Check through key code in IME.
     *
     * @param keyCode  check key code.
     * @return {@code true} if through key code; {@code false} otherwise.
     */
    protected boolean isThroughKeyCode(int keyCode) {
        boolean result;
        switch (keyCode) {
        case KeyEvent.KEYCODE_CALL:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
        case KeyEvent.KEYCODE_MEDIA_NEXT:
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
        case KeyEvent.KEYCODE_MEDIA_REWIND:
        case KeyEvent.KEYCODE_MEDIA_STOP:
        case KeyEvent.KEYCODE_MUTE:
        case KeyEvent.KEYCODE_HEADSETHOOK:
        case KeyEvent.KEYCODE_VOLUME_MUTE:
        case KeyEvent.KEYCODE_MEDIA_CLOSE:
        case KeyEvent.KEYCODE_MEDIA_EJECT:
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
        case KeyEvent.KEYCODE_MEDIA_PLAY:
        case KeyEvent.KEYCODE_MEDIA_RECORD:
        case KeyEvent.KEYCODE_MANNER_MODE:
            result = true;
            break;

        default:
            result = false;
            break;

        }
        return result;
    }

    /**
     * Restarts the IME.
     *
     * @param attribute  Description of the type of text being edited.
     */
    public void restartSelf(EditorInfo attribute) {}

    /**
     * Check ten-key code.
     *
     * @param keyCode  check key code.
     * @return {@code true} if ten-key code; {@code false} not ten-key code.
     */
    protected boolean isTenKeyCode(int keyCode) {
        boolean result = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_NUMPAD_0:
        case KeyEvent.KEYCODE_NUMPAD_1:
        case KeyEvent.KEYCODE_NUMPAD_2:
        case KeyEvent.KEYCODE_NUMPAD_3:
        case KeyEvent.KEYCODE_NUMPAD_4:
        case KeyEvent.KEYCODE_NUMPAD_5:
        case KeyEvent.KEYCODE_NUMPAD_6:
        case KeyEvent.KEYCODE_NUMPAD_7:
        case KeyEvent.KEYCODE_NUMPAD_8:
        case KeyEvent.KEYCODE_NUMPAD_9:
        case KeyEvent.KEYCODE_NUMPAD_DOT:
            result = true;
            break;

        default:
            break;

        }
        return result;
    }


    /**
     * Check fullten-key code.
     *
     * @param keyCode  check key code.
     * @return {@code true} if full-ten-key code; {@code false} not full-ten-key code.
     */
    protected boolean isFullTenKeyCode(int keyCode) {
        boolean result = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_NUMPAD_0:
        case KeyEvent.KEYCODE_NUMPAD_1:
        case KeyEvent.KEYCODE_NUMPAD_2:
        case KeyEvent.KEYCODE_NUMPAD_3:
        case KeyEvent.KEYCODE_NUMPAD_4:
        case KeyEvent.KEYCODE_NUMPAD_5:
        case KeyEvent.KEYCODE_NUMPAD_6:
        case KeyEvent.KEYCODE_NUMPAD_7:
        case KeyEvent.KEYCODE_NUMPAD_8:
        case KeyEvent.KEYCODE_NUMPAD_9:
        case KeyEvent.KEYCODE_NUMPAD_DOT:
        case KeyEvent.KEYCODE_NUMPAD_ADD:
        case KeyEvent.KEYCODE_NUMPAD_COMMA:
        case KeyEvent.KEYCODE_NUMPAD_DIVIDE:
        case KeyEvent.KEYCODE_NUMPAD_EQUALS:
        case KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN:
        case KeyEvent.KEYCODE_NUMPAD_MULTIPLY:
        case KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN:
        case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
            result = true;
            break;

        default:
            break;

        }
        return result;
    }

    /**
     * Set converting type
     *
     * @param  set  Set type
     */
    public void setConvertingForFuncKeyType(int set) {
        mConvertingForFuncKeyType = set;
    }

    /**
     * Get converting type
     *
     * @return  converting type
     */
    public int getConvertingForFuncKeyType() {
        return mConvertingForFuncKeyType;
    }

    /**
     * Handle message "MSG_TOGGLE_TIME_LIMIT".
     *
     */
    protected void processToggleTimeLimit() {
        if (isDirectInputMode()) {
            sendKeyChar(mLastToggleCharTypeNull);
            mLastToggleCharTypeNull = 0;
        } else if (mStatus == STATUS_INPUT) {
            mStatus = STATUS_INPUT_EDIT;
        } // else {}
        DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)getInputViewManager();
        keyboard.setKeepShiftMode(true);
        updateViewStatus(mTargetLayer, false, false);
        keyboard.setKeepShiftMode(false);
    }

    /**
     * Update views and the display of the composing text.
     *
     * @param layer             Display layer of the composing text
     * @param updateCandidates  {@code true} to update the candidates view.
     *                          {@code false} the candidates view is not updated.
     * @param updateEmptyText   {@code true} to update always.
     *                          {@code false} to update the composing text if it is not empty.
     */
    protected void updateViewStatus(int layer, boolean updateCandidates, boolean updateEmptyText) {}

    /**
     * Async Commit Mushroom string.
     *
     */
    public void AsyncCommitMushRoomString() {
        if (mMushroomResultString != null) {
            mCommitMushRoomStringHandler.sendEmptyMessage(0);
        }
    }

    /** Handler class for commitMushRoomString. */
    Handler mCommitMushRoomStringHandler = new Handler() {
        /** @see android.os.Handler#handleMessage */
        @Override
        public void handleMessage(Message msg) {
            commitMushRoomString();
        }
    };

    /**
     * Get mushroom string.
     *
     */
    public CharSequence getMushroomResultString() {
        return mMushroomResultString;
    }

    /**
     * Clear mushroom string.
     *
     */
    public void clearMushroomResultString() {
        mMushroomResultString = null;
    }

    /**
     * Called when the instance is destroyed.
     */
    public void onDestroy() {
        if (getInputViewManager() != null) {
            getInputViewManager().onDestroy();
        }
    }
}
