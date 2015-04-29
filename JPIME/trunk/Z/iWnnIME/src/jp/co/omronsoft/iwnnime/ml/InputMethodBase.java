/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.SoundManager;
import jp.co.omronsoft.iwnnime.ml.DefaultSoftKeyboard;
import jp.co.omronsoft.iwnnime.ml.hangul.DefaultSoftKeyboardHangul;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import com.lge.handwritingime.BaseHandwritingKeyboard;

import dalvik.system.PathClassLoader;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.inputmethodservice.ExtractEditText;
import android.inputmethodservice.InputMethodService;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.DisplayMetrics;

import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Input method base class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class InputMethodBase {

    /** for DEBUG */
    private static boolean DEBUG = false;

/*  candidate debug for iWnnIME */
    /** Full screen change flag. */
    public boolean mIsFullScreenChanged = false;

    /** Input view (software keyboard) */
    protected InputViewManager  mInputViewManager = new DefaultSoftKeyboard(null);

    /** for isEmoji */
    protected boolean mIsEmoji = false;

    /** EmojiAssist Instance. */
    public EmojiAssist mEmojiAssist = null;

    /** Is EmojiAssist working. */
    private boolean mIsEmojiAssistWorking;

    /** Inputyype EditorInfo */
    protected EditorInfo mAttribute = new EditorInfo();

/*  candidate debug for iWnnIME */

    /** The PopupWindow for floating popup */
    private PopupWindow mFloatingPopupWindow = null;

    /** The view for IME input view instead of floating popup view */
    private ViewGroup mOldParent = null;

    /** Whether expand floating mode or not */
    private boolean mExpandFloatingMode = false;

    /** ZdiSplitWindowManager for split mode */
    private Object zdiSplitWindowManager = null;

    /** Method instance of ZdiSplitWindowManager.isSplitViewMode() */
    private Method isSplitModeMethod = null;

    /** Flag of ZdiSplitWindowManager.isSplitViewMode() exists */
    private boolean notFoundLibraryMethod = false;

    public static InputMethodSwitcher mOriginalInputMethodSwitcher;

    /** Out side view of input view */
    private LinearLayout mBaseLayout = null;

    /** Whether floating keyboard in landscape mode or not */
    private boolean mFloatingLandscape = false;

    /** Force portrait orientation even if mFloatingLandscape is true */
    private boolean mForceFloatingPortrait = false;

    /** Display width and height */
    private int mDisplayWidth = 0;
    private int mDisplayHeight = 0;

    /** X coordinate when you touch down */
    private int mBaseX = 0;
    /** Y coordinate when you touch down */
    private int mBaseY = 0;

    /** X coordinate of FloatingKeyboard */
    private int mFKPosX = 0;
    /** Y coordinate of FloatingKeyboard */
    private int mFKPosY = 0;

    /** Floating keyboard offset position. */
    public static final int FLOATING_KEYBOARD_OFFSET_X =   0;
    public static final int FLOATING_KEYBOARD_OFFSET_Y = -78;

    /** Key for floating keyboard position. */
    public static final String FLOATING_POPUP_X_PORT_KEY = "floating_popup_x_port_key";
    public static final String FLOATING_POPUP_Y_PORT_KEY = "floating_popup_y_port_key";
    public static final String FLOATING_POPUP_X_LAND_KEY = "floating_popup_x_land_key";
    public static final String FLOATING_POPUP_Y_LAND_KEY = "floating_popup_y_land_key";

    /** callback of Show floating. */
    private Runnable mShowFloatingPopupRunnable;

    protected InputMethodBase(InputMethodSwitcher ims) {
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::InputMethodBase()");}
        mOriginalInputMethodSwitcher = ims;
    }

    public void onCreate(){
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::onCreate()");}
        superOnCreate();
    }

    public void superOnCreate() {
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::superOnCreate()");}
        mOriginalInputMethodSwitcher.superOnCreate();
    }

    public void onStartInput(EditorInfo info, boolean restarting){
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::onStartInput()");}
        superOnStartInput(info, restarting);
    }
    public void superOnStartInput(EditorInfo info, boolean restarting) {
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::superOnStartInput()");}
        mOriginalInputMethodSwitcher.superOnStartInput(info, restarting);
    }

    public void onStartInputView(EditorInfo info, boolean restarting){
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::onStartInputView()");}
        superOnStartInputView(info, restarting);
    }

    public void superOnStartInputView(EditorInfo info, boolean restarting) {
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::superOnStartInputView()");}
        mOriginalInputMethodSwitcher.superOnStartInputView(info, restarting);
    }

    public void onFinishInputView(boolean finishingInput) {
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::onFinishInputView()");}
        superOnFinishInputView(finishingInput);
    }

    public void superOnFinishInputView(boolean finishingInput) {
        if (DEBUG) {Log.d("InputMethodBase", "InputMethodBase::superOnFinishInputView()");}
        mOriginalInputMethodSwitcher.superOnFinishInputView(finishingInput);
    }

    /**
     * Process an event.
     *
     * @param  ev  An event
     * @return {@code false}; always return false.
     */
    synchronized public boolean onEvent(HWEvent ev) {
        return false;
    }

    public void onWindowShown() {
        superOnWindowShown();
    }

     public void superOnWindowShown() {
         mOriginalInputMethodSwitcher.superOnWindowShown();
     }

     public void onConfigurationChanged(Configuration newConfig) {
         superOnConfigurationChanged(newConfig);
     }

     public void superOnConfigurationChanged(Configuration newConfig) {
         mOriginalInputMethodSwitcher.superOnConfigurationChanged(newConfig);
     }

     public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd){
         superOnUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
     }

     public void superOnUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd){
         mOriginalInputMethodSwitcher.superOnUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
     }

     public void onDestroy() {
         superOnDestroy();
     }

     public void superOnDestroy() {
         mOriginalInputMethodSwitcher.superOnDestroy();
     }

     public LayoutInflater getLayoutInflater () {
         return superGetLayoutInflater();
     }

     public LayoutInflater superGetLayoutInflater(){
         return mOriginalInputMethodSwitcher.superGetLayoutInflater();
     }

     public void switchInputMethod(String id) {
         superSwitchInputMethod(id);
     }

     public void superSwitchInputMethod(String id) {
         mOriginalInputMethodSwitcher.superSwitchInputMethod(id);
     }

     public void setCandidatesViewShown(boolean shown) {
         superSetCandidatesViewShown(shown);
     }

     public void superSetCandidatesViewShown(boolean shown) {
         mOriginalInputMethodSwitcher.superSetCandidatesViewShown(shown);
     }

     /**
      * Whether the Emoji mode.
      * @return {@code true} if Emoji can be input;
      */
     public boolean isEmoji() {
         return mIsEmoji;
     }

     public View onCreateInputView() {
         return superOnCreateInputView();
     }

     public void onCreateFloatingView() {
         if (mBaseLayout == null) {
             mBaseLayout = new LinearLayout(getApplicationContext());
         }
     }

     public View attachFloatingView(View view) {
         if (mBaseLayout == null) {
             mBaseLayout = new LinearLayout(getApplicationContext());
         }
         if (mBaseLayout != view) {
             mBaseLayout.removeAllViews();
             mBaseLayout.addView(view);
         }
         return mBaseLayout;
     }

     public View superOnCreateInputView() {
         // If have event-post of show Floating keyboard, Removed.
         if (mShowFloatingPopupRunnable != null) {
             mBaseLayout.removeCallbacks(mShowFloatingPopupRunnable);
             mShowFloatingPopupRunnable = null;
         }
         if (mFloatingPopupWindow != null) {
             mFloatingPopupWindow.dismiss();
         }
         mFloatingPopupWindow = null;
         return mOriginalInputMethodSwitcher.superOnCreateInputView();
     }

     public View onCreateCandidatesView() {
         return superOnCreateCandidatesView();
     }

     public View superOnCreateCandidatesView(){
         return mOriginalInputMethodSwitcher.superOnCreateInputView();
     }

     public void setCandidatesView(View view) {
         superSetCandidatesView(view);
     }

     public void superSetCandidatesView(View view) {
         mOriginalInputMethodSwitcher.superSetCandidatesView(view);
     }

     public boolean onKeyDown(int keyCode, KeyEvent event) {
         return superOnKeyDown(keyCode, event);
     }

     public boolean superOnKeyDown(int keyCode, KeyEvent event) {
         return mOriginalInputMethodSwitcher.superOnKeyDown(keyCode, event);
     }

     public boolean onKeyUp(int keyCode, KeyEvent event) {
         return superOnKeyUp(keyCode, event);
     }
     public boolean superOnKeyUp(int keyCode, KeyEvent event) {
         return mOriginalInputMethodSwitcher.superOnKeyUp(keyCode, event);
     }

     public boolean onKeyLongPress(int keyCode, KeyEvent event) {
         return superOnKeyLongPress(keyCode, event);
     }

     public boolean superOnKeyLongPress(int keyCode, KeyEvent event) {
         return mOriginalInputMethodSwitcher.superOnKeyLongPress(keyCode, event);
     }

     public InputConnection getCurrentInputConnection() {
         return superGetCurrentInputConnection();
     }

     public InputConnection superGetCurrentInputConnection(){
         return mOriginalInputMethodSwitcher.superGetCurrentInputConnection();
     }

     public void startActivity(Intent intent) {
         superStartActivity(intent);
     }
     public void superStartActivity(Intent intent){
         mOriginalInputMethodSwitcher.superStartActivity(intent);
     }

     public static Context superGetContext() {
         return  mOriginalInputMethodSwitcher;
     }

     public static Context getBaseContext() {
         return superGetBaseContext();
     }

     public static Context superGetBaseContext(){
         return mOriginalInputMethodSwitcher.superGetBaseContext();
     }

     public Context getApplicationContext() {
         return superGetApplicationContext();
     }
     public Context superGetApplicationContext(){
         return mOriginalInputMethodSwitcher.superGetApplicationContext();
     }

     public Object getSystemService(String name) {
         return superGetSystemService(name);
     }
     public Object superGetSystemService(String name) {
         return mOriginalInputMethodSwitcher.superGetSystemService(name);
     }

     public Resources getResources() {
         return superGetResources();
     }
     public Resources superGetResources() {
         return mOriginalInputMethodSwitcher.superGetResources();
     }

     public void onUpdateExtractingViews(EditorInfo el){
         superOnUpdateExtractingViews(el);
     }

     public void superOnUpdateExtractingViews(EditorInfo el){
         mOriginalInputMethodSwitcher.superOnUpdateExtractingViews(el);
     }

     public void requestHideSelf(int flags){
         superRequestHideSelf(flags);
     }

     public void superRequestHideSelf(int flags){
         mOriginalInputMethodSwitcher.superRequestHideSelf(flags);
     }

     public void hideWindow(){
         hideFloatingWindow();
         superHideWindow();
     }

     public void superHideWindow(){
         mOriginalInputMethodSwitcher.superHideWindow();
     }

     public View onCreateExtractTextView(){
         return superOnCreateExtractTextView();
     }

     public View superOnCreateExtractTextView(){
         return mOriginalInputMethodSwitcher.superOnCreateExtractTextView();
     }

     public void onFinishInput(){
         superOnFinishInput();
     }

     public void superOnFinishInput(){
         mOriginalInputMethodSwitcher.superOnFinishInput();
     }

     public void unbindService(ServiceConnection conn){
         superUnbindService(conn);
     }

     public void superUnbindService(ServiceConnection conn){
         mOriginalInputMethodSwitcher.superUnbindService(conn);
     }

     public String getPackageName(){
         return superGetPackageName();
     }

     public String superGetPackageName(){
         return mOriginalInputMethodSwitcher.superGetPackageName();
     }

     public void showWindow(boolean showInput){
         superShowWindow(showInput);
     }

     public void superShowWindow(boolean showInput){
         mOriginalInputMethodSwitcher.superShowWindow(showInput);
     }
     public void hideStatusIcon(){
         superHideStatusIcon();
     }

     public void superHideStatusIcon(){
         mOriginalInputMethodSwitcher.superHideStatusIcon();
     }

     public boolean bindService(Intent service, ServiceConnection conn, int flags){
         return superBindService(service, conn, flags);
     }

     public boolean  superBindService(Intent service, ServiceConnection conn, int flags){
         return mOriginalInputMethodSwitcher.superBindService(service, conn, flags);
     }

     public void onExtractedTextClicked(){
         superOnExtractedTextClicked();
     }

     public void superOnExtractedTextClicked(){
         mOriginalInputMethodSwitcher.superOnExtractedTextClicked();
     }

     public File getFilesDir(){
         return superGetFilesDir();
     }

     public File superGetFilesDir(){
         return mOriginalInputMethodSwitcher.superGetFilesDir();
     }

     public void onComputeInsets(InputMethodService.Insets outInsets) {
        superOnComputeInsets(outInsets);
     }

     public void superOnComputeInsets(InputMethodService.Insets outInsets) {
         mOriginalInputMethodSwitcher.superOnComputeInsets(outInsets);
     }

     public boolean isInputViewShown(){
         return superIsInputViewShown();
     }

     public boolean superIsInputViewShown(){
         return mOriginalInputMethodSwitcher.superIsInputViewShown();
     }

     public void setInputView(View view){
         superSetInputView(attachFloatingView(view));
     }

     public void superSetInputView(View view){
         mOriginalInputMethodSwitcher.superSetInputView(view);
     }

     public void sendDownUpKeyEvents(int keyEventCode){
         superSendDownUpKeyEvents(keyEventCode);
     }

     public void superSendDownUpKeyEvents(int keyEventCode){
         mOriginalInputMethodSwitcher.superSendDownUpKeyEvents(keyEventCode);
     }

     public void sendKeyChar(char charCode){
         superSendKeyChar(charCode);
     }

     public void superSendKeyChar(char charCode){
         mOriginalInputMethodSwitcher.superSendKeyChar(charCode);
     }

     public void updateFullscreenMode(){
         superUpdateFullscreenMode();
     }

     public void superUpdateFullscreenMode(){
         mOriginalInputMethodSwitcher.superUpdateFullscreenMode();
     }

     public EditorInfo getCurrentInputEditorInfo(){
         return superGetCurrentInputEditorInfo();
     }

     public EditorInfo superGetCurrentInputEditorInfo(){
         return mOriginalInputMethodSwitcher.superGetCurrentInputEditorInfo();
     }

     public boolean onEvaluateFullscreenMode(){
         return superOnEvaluateFullscreenMode();
     }

     public boolean superOnEvaluateFullscreenMode(){
         return mOriginalInputMethodSwitcher.superOnEvaluateFullscreenMode();
     }

     public boolean onEvaluateInputViewShown(){
         return superOnEvaluateInputViewShown();
     }

     public boolean superOnEvaluateInputViewShown(){
         return mOriginalInputMethodSwitcher.superOnEvaluateInputViewShown();
     }

     protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype){
         superOnCurrentInputMethodSubtypeChanged(newSubtype);
     }

     protected void superOnCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype){
         mOriginalInputMethodSwitcher.superOnCurrentInputMethodSubtypeChanged(newSubtype);
     }

     public SharedPreferences getSharedPreferences(String name, int mode){
         return superGetSharedPreferences(name, mode);
     }

     public SharedPreferences superGetSharedPreferences(String name, int mode){
         return mOriginalInputMethodSwitcher.superGetSharedPreferences(name, mode);
     }

     public Dialog getWindow(){
         return superGetWindow();
     }

     public Dialog superGetWindow(){
         return mOriginalInputMethodSwitcher.superGetWindow();
     }

     public PackageManager getPackageManager(){
         return superGetPackageManager();
     }

     public PackageManager superGetPackageManager(){
         return mOriginalInputMethodSwitcher.superGetPackageManager();
     }

     public void sendBroadcast(Intent intent) {
         superSendBroadcast(intent);
     }

     public void superSendBroadcast(Intent intent) {
         mOriginalInputMethodSwitcher.superSendBroadcast(intent);
     }

     public void showStatusIcon(int iconResId) {
         superShowStatusIcon(iconResId);
     }

     public void superShowStatusIcon(int iconResId) {
         mOriginalInputMethodSwitcher.superShowStatusIcon(iconResId);
     }

    /**
     * Get the selected additional symbol list.
     * @param mode change imemode.
     * 0 - Handwriting.
     * 1 - iWnnime.
     * 2 - Symbol list.
     * 3 - Navigation keypad.
    */
    public boolean changeMode(int mode){
        mOriginalInputMethodSwitcher.changeInputMethod(mode);
        return false;
    }

    /**
     * Get ExtractEditText Instance.
     *
     * @return ExtractEditText
     */
    public ExtractEditText getExtractEditText() {
        ExtractEditText extractEditText = null;
        try {
            Field f = InputMethodService.class.getDeclaredField("mExtractEditText");
            f.setAccessible(true);
            Object object = f.get(this.mOriginalInputMethodSwitcher);
            extractEditText = (ExtractEditText)object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extractEditText;
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
     * End For EmojiAssist Animation.
     */
    public void endEmojiAssist() {
        if (mEmojiAssist == null) {
            mEmojiAssist = EmojiAssist.getInstance();
        }
        mEmojiAssist.stopAnimation();
        mIsEmojiAssistWorking = false;
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
     * Get content view of floating window.
     */
    public View getFloatingContentView() {
        if (mFloatingPopupWindow != null) {
            return mFloatingPopupWindow.getContentView();
        }
        return null;
    }

    /**
     * Whether split mode or not.
     */
    public boolean isSplitViewMode() {
        boolean ret = false;

        try {
            if ( zdiSplitWindowManager == null && !notFoundLibraryMethod ) {
                // reflect the method ZdiSplitWindowManager.isSplitMode
                ClassLoader cloader = new PathClassLoader("/system/framework/com.lge.zdi.splitwindow.jar", getClass().getClassLoader());
                Class<?> splitwindowClass = Class.forName("com.lge.zdi.splitwindow.ZdiSplitWindowManager", true, cloader);
                isSplitModeMethod = splitwindowClass.getDeclaredMethod("isSplitView", (Class<?>[])null);
                zdiSplitWindowManager = splitwindowClass.getDeclaredConstructor(Context.class).newInstance(this.getApplicationContext());
            };

            if( isSplitModeMethod != null && zdiSplitWindowManager != null ) {
                // call the method of ZdiSplitWindowManager.isSplitMode
                ret = (Boolean) isSplitModeMethod.invoke(zdiSplitWindowManager, (Object[])null);
            }
        } catch (IllegalAccessException e) {
            notFoundLibraryMethod = true;
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            notFoundLibraryMethod = true;
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            notFoundLibraryMethod = true;
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            notFoundLibraryMethod = true;
            e.printStackTrace();
        } catch (InstantiationException e) {
            notFoundLibraryMethod = true;
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            notFoundLibraryMethod = true;
            e.printStackTrace();
        }
        if (DEBUG) {Log.d("FLO", "isSplitViewMode() ret=[" + ret + "]");}
        return ret;
    }

    /**
     * Show floating popup window.
     *
     * @param mainLayout top layout view of floating popup window
     * @param inputView IME input view of floating popup window
     * @param candidateView IME candidate view of floating popup window
     */
    private void showFloating(final ViewGroup mainLayout, final View inputView, final View candidateView) {
        if (DEBUG) {Log.d("FLO", "showFloating() candidateView.getWindowToken():[" + candidateView.getWindowToken() + "]");}
        if (mOriginalInputMethodSwitcher.isEnableFloating()) {
            // change to FLOATING keyboard from normal keyboard
            int popupWidth;
            int popupHeight;
            int popupX;
            int popupY;

            Context context = mOriginalInputMethodSwitcher.getApplicationContext();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            if (context.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                popupX = sharedPref.getInt(FLOATING_POPUP_X_LAND_KEY, context.getResources().
                        getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_x));
                popupY = sharedPref.getInt(FLOATING_POPUP_Y_LAND_KEY, context.getResources().
                        getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_y));
            } else {
                popupX = sharedPref.getInt(FLOATING_POPUP_X_PORT_KEY, context.getResources().
                        getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_x));
                popupY = sharedPref.getInt(FLOATING_POPUP_Y_PORT_KEY, context.getResources().
                        getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_y));
            }

            popupWidth = context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_width);
            popupHeight = context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_height)
                    + context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_actionbar_height)
                    + context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_candidate_area_height);

            if (mFloatingPopupWindow != null && mFloatingPopupWindow.isShowing()) {
                // move floating window
                if (mOldParent != null) {
                    // set input view to content view of popup window
                    LinearLayout keyboardArea = (LinearLayout)mOldParent.findViewById(R.id.keyboard_area);
                    keyboardArea.removeAllViews();
                    keyboardArea.addView(inputView);

                    // set candidate view to content view of popup window
                    LinearLayout candidateArea = (LinearLayout)mOldParent.findViewById(R.id.candidate_area);
                    View candidateBlankArea = candidateView.findViewById(R.id.candidate_blank_area);
                    candidateBlankArea.setMinimumHeight(0);
                    candidateArea.removeAllViews();
                    candidateArea.addView(candidateView);
                }

                mFloatingPopupWindow.update(popupX, popupY,
                        popupWidth, popupHeight, true);
            } else {
                mOriginalInputMethodSwitcher.freeImeMap();

                // show floating window
                mBaseLayout.removeAllViews();

                if (mShowFloatingPopupRunnable != null) {
                    mBaseLayout.removeCallbacks(mShowFloatingPopupRunnable);
                }
                mShowFloatingPopupRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Floating window's parent view is IME view, 
                        // and floating window can't show because IME view is still invisible in onStartInputView().
                        // Thus, calling post() method can show floating window in main thread. 
                        showFloatingPopup(mainLayout, inputView, candidateView);
                    }
                };

                // show floating window in main thread
                mBaseLayout.post(mShowFloatingPopupRunnable);
            }
        } else {
            // change to NORMAL keyboard from floating keyboard
            if (mFloatingPopupWindow != null) {
                mOriginalInputMethodSwitcher.freeImeMap();
                Context context = mOriginalInputMethodSwitcher.getApplicationContext();

                closeFloatingWindow();

                // set input view and candidate view
                ViewGroup parent = (ViewGroup)inputView.getParent();
                if (null != parent) {
                    parent.removeView(inputView);
                }

                setInputView(inputView);
                View candidateBlankArea = candidateView.findViewById(R.id.candidate_blank_area);
                candidateBlankArea.setMinimumHeight(context.getResources().getDimensionPixelSize(R.dimen.candidate_blank_area_height));
                candidateView.setVisibility(View.VISIBLE);
                setCandidatesView(candidateView);

                resetKeyMode();

            }
        }
    }

    public void resetKeyMode() {
        if (DEBUG) { Log.d("OpenWnn","InputMethodBase.resetKeyMode()"); }
        return;
    }

    private void hideFloatingWindow() {
        if (DEBUG) {Log.d("FLO", "InputMethodBase.hideFloatingWindow()");}
        // If have event-post of show Floating keyboard, Removed.
        if (mShowFloatingPopupRunnable != null) {
            mBaseLayout.removeCallbacks(mShowFloatingPopupRunnable);
            mShowFloatingPopupRunnable = null;
        }
        if (mFloatingPopupWindow != null) {
            mFloatingPopupWindow.dismiss();
        }

        if (mOldParent != null) {
            LinearLayout keyboardArea = (LinearLayout)mOldParent.findViewById(R.id.keyboard_area);
            keyboardArea.removeAllViews();
            LinearLayout candidateArea = (LinearLayout)mOldParent.findViewById(R.id.candidate_area);
            candidateArea.removeAllViews();
        }
        return;
    }

    public void closeFloatingWindow() {
        if (DEBUG) {Log.d("FLO", "InputMethodBase.closeFloatingWindow()");}
        // If have event-post of show Floating keyboard, Removed.
        if (mShowFloatingPopupRunnable != null) {
            mBaseLayout.removeCallbacks(mShowFloatingPopupRunnable);
            mShowFloatingPopupRunnable = null;
        }
        // remove content views from popup window
        Context context = mOriginalInputMethodSwitcher.getApplicationContext();
        if (mFloatingPopupWindow != null) {
            mFloatingPopupWindow.dismiss();
            mFloatingPopupWindow.setContentView(new View(context));
            mFloatingPopupWindow = null;
        }

        if (mOldParent != null) {
            LinearLayout keyboardArea = (LinearLayout)mOldParent.findViewById(R.id.keyboard_area);
            keyboardArea.removeAllViews();
            LinearLayout candidateArea = (LinearLayout)mOldParent.findViewById(R.id.candidate_area);
            candidateArea.removeAllViews();
            mOldParent = null;
        }

        /* For some reason, this code is necessary */
        mBaseLayout = null;
    }

    /**
     * toggle expand mode or not.
     */
    public void toggleExpandFloatingMode() {
        InputMethodBase.mOriginalInputMethodSwitcher
                .onEvent(new HWEvent(HWEvent.CHANGE_FLOATING_KEYBOARD));
        mExpandFloatingMode = !mExpandFloatingMode;
        hideFloatingWindow();
        mFloatingPopupWindow = null;
        mOldParent = null;
    }

    /**
     * Create popup window and show it.
     *
     * @param mainLayout top layout view of floating popup window
     * @param inputView IME input view of floating popup window
     * @param candidateView IME candidate view of floating popup window
     */
    private void showFloatingPopup(ViewGroup mainLayout, View inputView, View candidateView) {

        int popupWidth;
        int popupHeight;
        int popupX;
        int popupY;
        Rect rect = new Rect();

        WindowManager wm = (WindowManager)OpenWnn.superGetContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        mDisplayWidth = displayMetrics.widthPixels;
        getWindow().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        mDisplayHeight = rect.height();

        Context context = mOriginalInputMethodSwitcher.getApplicationContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            popupX = sharedPref.getInt(FLOATING_POPUP_X_LAND_KEY, context.getResources().
                    getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_x));
            popupY = sharedPref.getInt(FLOATING_POPUP_Y_LAND_KEY, context.getResources().
                    getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_y));
        } else {
            popupX = sharedPref.getInt(FLOATING_POPUP_X_PORT_KEY, context.getResources().
                    getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_x));
            popupY = sharedPref.getInt(FLOATING_POPUP_Y_PORT_KEY, context.getResources().
                    getDimensionPixelSize(R.dimen.floating_keyboard_default_pos_y));
        }

        popupWidth = context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_width);
        popupHeight = context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_height)
                + context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_actionbar_height)
                + context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_candidate_area_height);

        mOldParent = (ViewGroup)mOriginalInputMethodSwitcher.getLayoutInflater().inflate(R.layout.floating_keyboard, null);

        // set callback of dragging action bar
        View actionBar = (View)mOldParent.findViewById(R.id.action_bar);
        actionBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int popupWidth;
                int popupHeight;
                int popupX;
                int popupY;

                Context context = mOriginalInputMethodSwitcher.getApplicationContext();
                popupWidth = context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_width);
                popupHeight = context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_height)
                        + context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_actionbar_height)
                        + context.getResources().getDimensionPixelSize(R.dimen.floating_keyboard_candidate_area_height);

                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBaseX = (int)event.getRawX();
                    mBaseY = (int)event.getRawY();
                    int[] windowLocation = new int[2];
                    v.getLocationOnScreen(windowLocation);
                    mFKPosX = windowLocation[0];
                    mFKPosY = windowLocation[1];
                    mFKPosY += v.getHeight() / 2;
                    mInputViewManager.closePopupKeyboard();
                    break;

                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    popupX = (int)(event.getRawX()) - mBaseX + mFKPosX + FLOATING_KEYBOARD_OFFSET_X;
                    popupY = (int)(event.getRawY()) - mBaseY + mFKPosY + FLOATING_KEYBOARD_OFFSET_Y;

                    if (popupX < (popupWidth / 2 * -1)) {
                        popupX = popupWidth / 2 * -1;
                    }
                    if (popupX > (mDisplayWidth - popupWidth / 2)) {
                        popupX = mDisplayWidth - popupWidth / 2;
                    }
                    if (popupY < 0) {
                        popupY = 0;
                    }
                    if (popupY > (mDisplayHeight - popupHeight / 2)) {
                        popupY = mDisplayHeight - popupHeight / 2;
                    }

                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        mFloatingPopupWindow.update(popupX, popupY, popupWidth, popupHeight, true);
                    } else {
                        int orientation = context.getResources().getConfiguration().orientation;
                        saveFlaotingPosition(popupX, popupY, orientation);
                    }
                    break;

                default:
                    break;
                }
                return true;
            }
        });

        // set callback of pushing expand button
/*
        View expandButton = (View)mOldParent.findViewById(R.id.expand_button);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpandFloatingMode();
            }
        });
*/

        // set callback of pushing close button
        View closeButton = mOldParent.findViewById(R.id.floating_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mOriginalInputMethodSwitcher.mInputMethodBase.requestHideSelf(0);
            }
        });

        // set input view to content view of popup window
        LinearLayout keyboardArea = (LinearLayout)mOldParent.findViewById(R.id.keyboard_area);
        keyboardArea.addView(inputView);

        // set blank view to candidate view of input method
        LinearLayout candidateArea = (LinearLayout)mOldParent.findViewById(R.id.candidate_area);
        setCandidatesView(new View(mOriginalInputMethodSwitcher.getApplicationContext()));

        // set candidate view to content view of popup window
        View candidateBlankArea = candidateView.findViewById(R.id.candidate_blank_area);
        candidateBlankArea.setMinimumHeight(0);
        candidateArea.removeAllViews();
        candidateArea.addView(candidateView);

        // show the popup window
        mFloatingPopupWindow = new PopupWindow(OpenWnn.superGetContext());
        mainLayout.addView(mOldParent);
        mFloatingPopupWindow.setContentView(mainLayout);
        mFloatingPopupWindow.setBackgroundDrawable(null);
        mFloatingPopupWindow.setTouchable(true);
        mFloatingPopupWindow.setWidth(popupWidth);
        mFloatingPopupWindow.setHeight(popupHeight);
        mFloatingPopupWindow.setClippingEnabled(false);
        mFloatingPopupWindow.showAtLocation(mBaseLayout, Gravity.LEFT | Gravity.TOP,
                popupX, popupY);
        mShowFloatingPopupRunnable = null;
    }

    /**
     * Get the parent view for popup window or dialog in floating mode
     * @return popup parent view
     */
    public View getFloatingPopupParent() {
        return mBaseLayout;
    }

    /**
     * The judgment of the Expanded Floating keyboard
     * @return true: Expanded Floating keyboard false: Floating keyboard
     */
    public boolean isExpandedFloatingMode() {
        return mExpandFloatingMode;
    }

    /**
     * Display a keyboard on Floating keyboard.
     * @param mainLayout Layout view of the Handwriting keyboard
     * @param view View of the Handwriting keyboard
     */
    public void openFloatingKeyboard(ViewGroup mainLayout, View inputView, View candidateView) {
        Log.d("InputMethodBase", "isExpandedFloatingMode()=[" + isExpandedFloatingMode() + "]");
        showFloating(mainLayout, inputView, candidateView);
    }

    /**
     * Display a Handwriting keyboard on Floating keyboard.
     * @param mainLayout Layout view of the Handwriting keyboard
     * @param view View of the Handwriting keyboard
     */
    public void openFloatingKeyboardHW(ViewGroup mainLayout, View view) {
        if (null == view) {return;}

        // set flag for Floating keyboard of Handwriting.
        Context context = mOriginalInputMethodSwitcher.getApplicationContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = getResources();
        mOriginalInputMethodSwitcher.setEnableFloating(
                pref.getBoolean(getFloatingPreferenceKeyName(),
                res.getBoolean(R.bool.floating_mode_show_default_value)));

        if ((isNotExpandedFloatingMode()) && (null != mainLayout)) {
            mainLayout.removeView(view);
        }

        if ((isNotExpandedFloatingMode())) {
            openFloatingKeyboard(new LinearLayout(mOriginalInputMethodSwitcher.getApplicationContext()),
                    view,
                    onCreateCandidatesView());
        } else if ((mFloatingPopupWindow != null) && (null != mainLayout)) {
            openFloatingKeyboard(new LinearLayout(mOriginalInputMethodSwitcher.getApplicationContext()),
                    mainLayout,
                    onCreateCandidatesView());
        } // else{}

    }

    public boolean isNotExpandedFloatingMode() {
        return mOriginalInputMethodSwitcher.isEnableFloating() && !isExpandedFloatingMode();
    }

    /**
     * Set whether floating keyboard in landscape mode or not
     * @param isLandscape true if landscape
     */
    public void setFloatingLandscape(boolean isLandscape) {
        mFloatingLandscape = isLandscape;
    }

    /**
     * Set portrait orientation in floating keyboard even if mFloatingLandscape is true.
     * 
     * Orientation is always portrait in floating keyboard,
     * but in some cases, orientation must be portrait in floating keyboard.
     * (ex. url / mail list of portrait orientation is too long to show on floating keyboard in landscape orientation)
     * @param isForcePortrait true if portrait
     */
    public void setForceFloatingPortrait(boolean isForcePortrait) {
        mForceFloatingPortrait = isForcePortrait;
    }

    /**
     * Get whether floating keyboard in landscape mode or not
     * @return true if landscape
     */
    public boolean isFloatingLandscape() {
        return (isNotExpandedFloatingMode() && !mForceFloatingPortrait && mFloatingLandscape);
    }

    public boolean isFloatingPopupShown() {
        return (mFloatingPopupWindow != null) && (mFloatingPopupWindow.isShowing());
    }

    /**
     * Change show Floating keyboard flag.
     *
     * @param show  {@code true} if show floating keyboard.
     */
    public void setEnableFloating(boolean show) {
        mOriginalInputMethodSwitcher.setEnableFloating(show);
    }

    /**
     * Check show floating keyboard flag.
     *
     * @return boolean  show floating flag.
     */
    public boolean isEnableFloating() {
        return mOriginalInputMethodSwitcher.isEnableFloating();
    }

    /**
     * Get preference key name of floating flag.
     * @param orientation orientation of screen. use value of {@link Configuration}.
     * @return preference key name of floating flag.
     */
    public String getFloatingPreferenceKeyName(int orientation) {
        String keyName = "floating_show_";

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            keyName += "landscape";
        } else {
            keyName += "portrait";
        }

        if (isSplitViewMode()) {
            keyName += "_split_view_mode_on";
        } else {
            keyName += "_split_view_mode_off";
        }

        return keyName;
    }

    /**
     * Get preference key name of floating flag.
     * @return preference key name of floating flag.
     */
    public String getFloatingPreferenceKeyName() {
        Context context = mOriginalInputMethodSwitcher.getApplicationContext();
        return getFloatingPreferenceKeyName(context.getResources().getConfiguration().orientation);
    }

    /**
     * Save the display position of Floating keyboard.
     * @param orientation orientation of screen. use value of {@link Configuration}.
     * @return true saved, false save failure
     */
    public boolean saveFlaotingPosition(int orientation) {
        if (isFloatingPopupShown()) {
            View v = mFloatingPopupWindow.getContentView();
            WindowManager.LayoutParams param = (WindowManager.LayoutParams)v.getLayoutParams();
            saveFlaotingPosition(param.x, param.y, orientation);
            return true;
        }
        return false;
    }

    /**
     * Save the display position of Floating keyboard.
     * @param popupX x of position
     * @param popupY y of position 
     * @param orientation orientation of screen. use value of {@link Configuration}.
     */
    private void saveFlaotingPosition(int popupX, int popupY, int orientation) {
        Context context = mOriginalInputMethodSwitcher.getApplicationContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            editor.putInt(FLOATING_POPUP_X_LAND_KEY, popupX);
            editor.putInt(FLOATING_POPUP_Y_LAND_KEY, popupY);
        } else {
            editor.putInt(FLOATING_POPUP_X_PORT_KEY, popupX);
            editor.putInt(FLOATING_POPUP_Y_PORT_KEY, popupY);
        }
        editor.commit();
    }
}
