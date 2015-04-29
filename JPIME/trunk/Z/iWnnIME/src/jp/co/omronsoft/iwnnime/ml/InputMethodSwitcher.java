/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.lge.handwritingime.BaseHandwritingKeyboard;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.LinearLayout;

import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnSymbolEngine;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import jp.co.omronsoft.iwnnime.ml.jajp.IWnnImeJaJp;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

/**
 * Input method switching class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class InputMethodSwitcher extends InputMethodService {

    /** for DEBUG */
    private static boolean DEBUG = true;

    public boolean mSymbolMode = false;
    public boolean mNavigationMode = false;
    public boolean mNumericMode = false;

    /* The type of IME*/
    public static final int IME_TYPE_HANDWRITING = 0;
    public static final int IME_TYPE_IWNNIME =     1;
    public static final int IME_TYPE_SYMBOL =      2;
    public static final int IME_TYPE_NAVIGATION =  3;

    public InputMethodBase mInputMethodBase;

    /** Map of orientation and iwnn instance */
    private HashMap<Integer, InputMethodBase> mIwnnImeMap = new HashMap<Integer, InputMethodBase>();

    /** The previous type of ime type */
    public int mMode;

    public boolean mIsHideSelf = false;

    /** EditorInfo */
    private EditorInfo mAttribute = null;

    /** Orientaion type for iwnn instace caches of each orientations */
    private int mOrientation = 0;

    /** Locale type for iwnn instace caches of each locale */
    private Locale mLocale;

    /** Message for {@code mHandler} (restart) */
    private static final int MSG_RESTART = 7;

    /** Whether changing keyboard theme or not */
    private boolean isChangeTheme = false;

    /** Calling onDestory() for free resources ONLY (not call InputMethodService.onDestroy()) */
    private boolean isFreeResources = false;

    /** Flag of show Floating. */
    private boolean mEnableFloating = false;

    /** {@code Handler} for drawing candidates/displaying */
    Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESTART:
                    onStartInput(mAttribute, false);
                    onStartInputView(mAttribute, false);
                    break;
            }
        }
    };

    /**
     * Constructor
     */

    public InputMethodSwitcher() {
      super();
      if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::InputMethodSwitcher()");}
    }

    /**
     * Called by the system when the service is first created.
     *
     * @see android.inputmethodservice.InputMethodService#onCreate
     */
    @Override public void onCreate() {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::onCreate()");}

        if(mInputMethodBase == null){
            mInputMethodBase = new IWnnLanguageSwitcher(this);
            mMode = IME_TYPE_IWNNIME;
        }
        super.onCreate(); // See superOnCreate() comment.

        Configuration config = getResources().getConfiguration();
        mLocale = config.locale;

        mInputMethodBase.onCreate();
    }

    public void superOnCreate() {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superOnCreate()");}
        // TODO:Can not call super.onCreate().
        // If a software keyboard is opened, an aborting will be carried out by WindowManager.
        // [Error Message] Attempted to add input method window with bad token null. Aborting.
    }

    /**
     * Called when the input view is being shown and input has started on a new editor.
     *
     * @see android.inputmethodservice.InputMethodService#onStartInputView
     */
    @Override public void onStartInputView(EditorInfo info, boolean restarting) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::onStartInputView()");}
        //if the hide softkeyboard flag is true, it will be not to call onStartInputView
        if (mIsHideSelf) {
            return;
        }
        mInputMethodBase.onStartInputView(info, restarting);
    }

    public void superOnStartInputView(EditorInfo info, boolean restarting) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superOnStartInputView()");}
        //if the hide softkeyboard flag is true, it will be not to call onStartInputView
        if (mIsHideSelf) {
            return;
        }
        super.onStartInputView(info, restarting);
    }

    /**
     * Called to inform the input method that text input has started in an editor.
     *
     * @see android.inputmethodservice.InputMethodService#onStartInput
     */
    @Override public void onStartInput(EditorInfo info, boolean restarting){
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::onStartInput()");}
        super.onStartInput(info, restarting);
        mInputMethodBase.onStartInput(info, restarting);
    }

    public void superOnStartInput(EditorInfo info, boolean restarting){
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superOnStartInput()");}
        //super.onStartInput(info, restarting);
    }

    /**
     * Process an event.
     *
     * @param  ev  An event
     * @return {@code false}; always return false.
     */
    synchronized public boolean onEvent(HWEvent ev) {
        Log.d("InputMethodSwitcher", "onEvent() code=[" + ev.code + "]");
        if (mInputMethodBase instanceof BaseHandwritingKeyboard) {
            mInputMethodBase.onEvent(ev);
        }
        return false;
    }

    /* @see android.inputmethodservice.InputMethodService#onFinishInputView */
    @Override public void onFinishInputView(boolean finishingInput) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::onFinishInputView()");}
        mInputMethodBase.onFinishInputView(finishingInput);
    }

    public void superOnFinishInputView(boolean finishingInput) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superOnFinishInputView()");}
        super.onFinishInputView(finishingInput);
        //clear the hide softkeyboard flag.
        mIsHideSelf = false;
    }

    /* @see android.inputmethodservice.InputMethodService#onWindowShown */
    @Override public void onWindowShown() {
        mInputMethodBase.onWindowShown();
    }

    public void superOnWindowShown() {
        super.onWindowShown();
    }

    /* @see android.inputmethodservice.InputMethodService#onConfigurationChanged */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        if (mOrientation != newConfig.orientation) {
            // Save the position of the floating keyboard in the direction
            // of the screen before the rotation.
            mInputMethodBase.saveFlaotingPosition(mOrientation);
            // clear iwnn ime cache instance
            mOrientation = newConfig.orientation;

            freeImeMap();
        }
        if (!newConfig.locale.equals(mLocale)) {
            mLocale = newConfig.locale;
            if (mMode == IME_TYPE_HANDWRITING) {
                // Handwriting keyboard use the cached Candidate View of iWnn.
                // So Recreate even the Candidate view of iWnn.
                InputMethodBase iWnn = mIwnnImeMap.get(mOrientation);
                if (iWnn != null) {
                    iWnn.onCreateCandidatesView();
                }
            }
        }
        mInputMethodBase.closeFloatingWindow();
        mInputMethodBase.onConfigurationChanged(newConfig);
    }

    public void superOnConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /* @see android.inputmethodservice.InputMethodService#onUpdateSelection */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                            int newSelStart, int newSelEnd,
                                            int candidatesStart, int candidatesEnd){
         mInputMethodBase.onUpdateSelection(oldSelStart, oldSelEnd,
                                            newSelStart, newSelEnd,
                                            candidatesStart, candidatesEnd);
    }
    public void superOnUpdateSelection(int oldSelStart, int oldSelEnd,
                                       int newSelStart, int newSelEnd,
                                       int candidatesStart, int candidatesEnd){
        super.onUpdateSelection(oldSelStart, oldSelEnd,
                                newSelStart, newSelEnd,
                                candidatesStart, candidatesEnd);
    }

     /* @see android.inputmethodservice.InputMethodService#onDestroy */
    @Override public void onDestroy() {
        mInputMethodBase.onDestroy();
    }
    public void superOnDestroy() {
        if (!isFreeResources) {
            // Unless this branch, IME closed when changing to handwriting keyboard
            // because calling InputMethodService.onDestroy().
            super.onDestroy();
        }
        isFreeResources = false;
    }

    /* @see android.inputmethodservice.InputMethodService#getLayoutInflater */
    @Override public LayoutInflater getLayoutInflater () {
        return mInputMethodBase.getLayoutInflater();
    }
    public LayoutInflater superGetLayoutInflater () {
        return super.getLayoutInflater();
    }

    /* @see android.inputmethodservice.InputMethodService#switchInputMethod */
    @Override public void switchInputMethod(String id) {
        mInputMethodBase.switchInputMethod(id);
    }
    public void superSwitchInputMethod(String id) {
        super.switchInputMethod(id);
    }

    /* @see android.inputmethodservice.InputMethodService#setCandidatesViewShown */
    @Override public void setCandidatesViewShown(boolean shown) {
        mInputMethodBase.setCandidatesViewShown(shown);
    }
    public void superSetCandidatesViewShown(boolean shown) {
        Log.d("FLO", "superSetCandidatesViewShown(" + shown + ")");
        super.setCandidatesViewShown(shown);
    }

    /* @see android.inputmethodservice.InputMethodService#onCreateInputView */
    @Override public View onCreateInputView() {
        mInputMethodBase.onCreateFloatingView();
        View view =  mInputMethodBase.onCreateInputView();
        return mInputMethodBase.attachFloatingView(view);
    }
    public View superOnCreateInputView() {
        return super.onCreateInputView();
    }

    /* @see android.inputmethodservice.InputMethodService#onCreateCandidatesView */
    @Override public View onCreateCandidatesView() {
        return mInputMethodBase.onCreateCandidatesView();
    }
    public View superOnCreateCandidatesView() {
        return super.onCreateCandidatesView();
    }

    /* @see android.inputmethodservice.InputMethodService#setCandidatesView */
    @Override public void setCandidatesView(View view) {
        mInputMethodBase.setCandidatesView(view);
    }
    public void superSetCandidatesView(View view) {
        Log.d("FLO", "superSetCandidatesView(" + view + ")");
        super.setCandidatesView(view);
    }

    /* @see android.inputmethodservice.InputMethodService#onKeyDown */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mInputMethodBase.onKeyDown(keyCode, event);
    }
    public boolean superOnKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    /* @see android.inputmethodservice.InputMethodService#onKeyUp */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mInputMethodBase.onKeyUp(keyCode, event);
    }
    public boolean superOnKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
    /* @see android.inputmethodservice.InputMethodService#onKeyLongPress */
    @Override public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return mInputMethodBase.onKeyLongPress(keyCode, event);
    }
    public boolean superOnKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }
    /* @see android.inputmethodservice.InputMethodService#getCurrentInputConnection */
    @Override public InputConnection getCurrentInputConnection() {
        return mInputMethodBase.getCurrentInputConnection();
    }
    public InputConnection superGetCurrentInputConnection() {
        return super.getCurrentInputConnection();
    }
    /* @see android.inputmethodservice.InputMethodService#startActivity */
    @Override public void startActivity(Intent intent) {
        mInputMethodBase.startActivity(intent);
    }
    public void superStartActivity(Intent intent) {
        super.startActivity(intent);
    }
    /* @see android.inputmethodservice.InputMethodService#getBaseContext */
    @Override public Context getBaseContext() {
        return InputMethodBase.getBaseContext();
    }
    public Context superGetBaseContext() {
        return super.getBaseContext();
    }

    /* @see android.inputmethodservice.InputMethodService#getApplicationContext */
    @Override public Context getApplicationContext() {
        return mInputMethodBase.getApplicationContext();
    }
    public Context superGetApplicationContext() {
        return super.getApplicationContext();
    }

    /* @see android.inputmethodservice.InputMethodService#getSystemService */
    @Override public Object getSystemService(String name) {
        return mInputMethodBase.getSystemService(name);
    }
    public Object superGetSystemService(String name) {
        return super.getSystemService(name);
    }

    /* @see android.inputmethodservice.InputMethodService#getResources */
    @Override public Resources getResources() {
        return mInputMethodBase.getResources();
    }
    public Resources superGetResources() {
        return super.getResources();
    }

    /* @see android.inputmethodservice.InputMethodService#onUpdateExtractingViews */
    @Override public void onUpdateExtractingViews(EditorInfo el){
        mInputMethodBase.onUpdateExtractingViews(el);
    }
    public void superOnUpdateExtractingViews(EditorInfo el){
        super.onUpdateExtractingViews(el);
    }

    /* @see android.inputmethodservice.InputMethodService#requestHideSelf */
    @Override public void requestHideSelf(int flags){
        mInputMethodBase.requestHideSelf(flags);
    }
    public void superRequestHideSelf(int flags){
        super.requestHideSelf(flags);
    }

    /* @see android.inputmethodservice.InputMethodService#hideWindow */
    @Override public void hideWindow(){
        mInputMethodBase.hideWindow();
        mHandler.removeMessages(MSG_RESTART);
    }
    public void superHideWindow(){
        super.hideWindow();
    }

    /* @see android.inputmethodservice.InputMethodService#onCreateExtractTextView */
    @Override public View onCreateExtractTextView(){
        return mInputMethodBase.onCreateExtractTextView();
    }
    public View superOnCreateExtractTextView(){
        return super.onCreateExtractTextView();
    }

    /* @see android.inputmethodservice.InputMethodService#onFinishInput */
    @Override public void onFinishInput(){
        mInputMethodBase.onFinishInput();
    }
    public void superOnFinishInput(){
        super.onFinishInput();
    }

    /* @see android.inputmethodservice.InputMethodService#getPackageName */
    @Override public String getPackageName(){
        return mInputMethodBase.getPackageName();
    }
    public String superGetPackageName(){
        return super.getPackageName();
    }

    /* @see android.inputmethodservice.InputMethodService#showWindow */
    @Override public void showWindow(boolean showInput){
        mInputMethodBase.showWindow(showInput);
    }
    public void superShowWindow(boolean showInput){
        super.showWindow(showInput);
    }

    /* @see android.inputmethodservice.InputMethodService#hideStatusIcon */
    @Override public void hideStatusIcon(){
        mInputMethodBase.hideStatusIcon();
    }
    public void superHideStatusIcon(){
        super.hideStatusIcon();
    }

    /* @see android.inputmethodservice.InputMethodService#bindService */
    @Override public boolean bindService(Intent service, ServiceConnection conn, int flags){
        return mInputMethodBase.bindService(service, conn, flags);
    }
    public boolean superBindService(Intent service, ServiceConnection conn, int flags){
        return super.bindService(service, conn, flags);
    }

    /* @see android.inputmethodservice.InputMethodService#unbindService */
    @Override public void unbindService(ServiceConnection conn){
        mInputMethodBase.unbindService(conn);
    }
    public void superUnbindService(ServiceConnection conn){
        super.unbindService(conn);
    }

    /* @see android.inputmethodservice.InputMethodService#onComputeInsets */
    @Override public void onComputeInsets(InputMethodService.Insets outInsets) {
        if (mEnableFloating == true) {
            outInsets.contentTopInsets = getResources().getDisplayMetrics().heightPixels; 
            outInsets.visibleTopInsets = getResources().getDisplayMetrics().heightPixels;
            return;
        }
        mInputMethodBase.onComputeInsets(outInsets);
    }
    public void superOnComputeInsets(InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
    }

    /* @see android.inputmethodservice.InputMethodService#onExtractedTextClicked */
    @Override public void onExtractedTextClicked(){
        mInputMethodBase.onExtractedTextClicked();
    }
    public void superOnExtractedTextClicked(){
        super.onExtractedTextClicked();
    }

    /* @see android.inputmethodservice.InputMethodService#getFilesDir */
    @Override public File getFilesDir(){
        return superGetFilesDir();
    }
    public File superGetFilesDir(){
        return super.getFilesDir();
    }

    /* @see android.inputmethodservice.InputMethodService#isInputViewShown */
    @Override public boolean isInputViewShown(){
        return mInputMethodBase.isInputViewShown();
    }

    public boolean superIsInputViewShown(){
        return super.isInputViewShown();
    }

    /* @see android.inputmethodservice.InputMethodService#setInputView */
    @Override public void setInputView(View view){
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::setInputView()");}
        mInputMethodBase.setInputView(view);
    }

    public void superSetInputView(View view){
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superSetInputView()");}
        super.setInputView(view);
    }

    /* @see android.inputmethodservice.InputMethodService#sendDownUpKeyEvents */
    @Override public void sendDownUpKeyEvents(int keyEventCode){
        mInputMethodBase.sendDownUpKeyEvents(keyEventCode);
    }

    public void superSendDownUpKeyEvents(int keyEventCode){
        super.sendDownUpKeyEvents(keyEventCode);
    }

    /* @see android.inputmethodservice.InputMethodService#sendKeyChar */
    @Override public void sendKeyChar(char charCode){
        mInputMethodBase.sendKeyChar(charCode);
    }

    public void superSendKeyChar(char charCode){
        super.sendKeyChar(charCode);
    }

    /* @see android.inputmethodservice.InputMethodService#updateFullscreenMode */
    @Override public void updateFullscreenMode(){
        mInputMethodBase.updateFullscreenMode();
    }

    public void superUpdateFullscreenMode(){
        super.updateFullscreenMode();
    }

    /* @see android.inputmethodservice.InputMethodService#getCurrentInputEditorInfo */
    @Override public EditorInfo getCurrentInputEditorInfo(){
        return mInputMethodBase.getCurrentInputEditorInfo();
    }

    public EditorInfo superGetCurrentInputEditorInfo(){
        return super.getCurrentInputEditorInfo();
    }

    /* @see android.inputmethodservice.InputMethodService#onEvaluateFullscreenMode */
    @Override public  boolean onEvaluateFullscreenMode(){
        return mInputMethodBase.onEvaluateFullscreenMode();
    }

    public  boolean superOnEvaluateFullscreenMode(){
        return super.onEvaluateFullscreenMode();
    }

    /* @see android.inputmethodservice.InputMethodService#onEvaluateInputViewShown */
    @Override public boolean onEvaluateInputViewShown(){
        return mInputMethodBase.onEvaluateInputViewShown();
    }

    public boolean superOnEvaluateInputViewShown(){
        return super.onEvaluateInputViewShown();
    }

    /* @see android.inputmethodservice.InputMethodService#onCurrentInputMethodSubtypeChanged */
    @Override protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype){
        mInputMethodBase.onCurrentInputMethodSubtypeChanged(newSubtype);
    }

    protected void superOnCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype){
        super.onCurrentInputMethodSubtypeChanged(newSubtype);
    }

    /* @see android.inputmethodservice.InputMethodService#getSharedPreferences */
    @Override public SharedPreferences getSharedPreferences(String name, int mode){
        return mInputMethodBase.getSharedPreferences(name, mode);
    }

    public SharedPreferences superGetSharedPreferences(String name, int mode){
        return super.getSharedPreferences(name, mode);
    }

    /* @see android.inputmethodservice.InputMethodService#getWindow */
    @Override public Dialog getWindow(){
        return mInputMethodBase.getWindow();
    }

    public Dialog superGetWindow(){
        return super.getWindow();
    }

    /* @see android.inputmethodservice.InputMethodService#getPackageManager */
    @Override public PackageManager getPackageManager(){
        return mInputMethodBase.getPackageManager();
    }
    public PackageManager superGetPackageManager(){
        return super.getPackageManager();
    }

    /* @see android.inputmethodservice.InputMethodService#sendBroadcast */
    @Override public void sendBroadcast(Intent intent) {
        mInputMethodBase.sendBroadcast(intent);
    }

    public void superSendBroadcast(Intent intent) {
        super.sendBroadcast(intent);
    }

    /* @see android.inputmethodservice.InputMethodService#showStatusIcon */
    @Override public void showStatusIcon(int iconResId) {
        mInputMethodBase.showStatusIcon(iconResId);
    }

    public void superShowStatusIcon(int iconResId) {
        super.showStatusIcon(iconResId);
    }

    /**
     * Change Input Method.
     *
     * @param mode Description of the type of ime type.
     */
    public void changeInputMethod( int mode ) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::changeInputMethod(" + mode + ").");}

        mAttribute = getCurrentInputEditorInfo();
        View v = null;
        View candidatesView = null;;

        if (mInputMethodBase != null){
            mInputMethodBase.onFinishInput();
            mInputMethodBase.closeFloatingWindow();
        }

        if (mInputMethodBase instanceof BaseHandwritingKeyboard) {
            // BaseHandwritingKeyboard instance must be called onDestroy() before the instance finalized.
            isFreeResources = true;
            mInputMethodBase.onDestroy();
        }

        mSymbolMode = false;
        mNavigationMode = false;

        boolean isSkipOnCreate = false;

        switch (mode) {
            case IME_TYPE_HANDWRITING:
                if (DEBUG) {Log.d("InputMethodSwitcher", "change handwriting ime.");}
                if ((mMode == IME_TYPE_IWNNIME)
                        || (mMode == IME_TYPE_SYMBOL) || (mMode == IME_TYPE_NAVIGATION)) {
                    mIwnnImeMap.put(mOrientation, mInputMethodBase);
                }
                OpenWnn wnn = OpenWnn.getCurrentIme();
                if (wnn != null) {
                    isChangeTheme = ((IWnnLanguageSwitcher)wnn).getInitializeState();
                }
                mInputMethodBase = new BaseHandwritingKeyboard(this);
                break;
            case IME_TYPE_IWNNIME:
                if (DEBUG) {Log.d("InputMethodSwitcher", "change iwnnime.");}
                if (mMode == IME_TYPE_HANDWRITING
                        && mIwnnImeMap.get(mOrientation) != null
                        && !isChangeTheme) {
                    mInputMethodBase = mIwnnImeMap.get(mOrientation);
                    isSkipOnCreate = true;
                } else {
                    mInputMethodBase = new IWnnLanguageSwitcher(this);
                }
                ((IWnnLanguageSwitcher)mInputMethodBase).mInputConnection
                        = getCurrentInputConnection();
                isChangeTheme = false;
                break;
            case IME_TYPE_SYMBOL:
                if (DEBUG) {Log.d("InputMethodSwitcher", "change iwnnime.");}
                if (mMode == IME_TYPE_HANDWRITING && mIwnnImeMap.get(mOrientation) != null) {
                    mInputMethodBase = mIwnnImeMap.get(mOrientation);
                    isSkipOnCreate = true;
                    ((IWnnLanguageSwitcher)mInputMethodBase)
                        .onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE, IWnnImeJaJp.ENGINE_MODE_SYMBOL_SYMBOL));
                } else {
                    mInputMethodBase = new IWnnLanguageSwitcher(this);
                }
                ((IWnnLanguageSwitcher)mInputMethodBase).mInputConnection
                        = getCurrentInputConnection();
                if (DEBUG) {Log.d("InputMethodSwitcher", "change symbol.");}
                mSymbolMode = true;
                break;
            case IME_TYPE_NAVIGATION:
                if (mMode == IME_TYPE_HANDWRITING && mIwnnImeMap.get(mOrientation) != null) {
                    mInputMethodBase = mIwnnImeMap.get(mOrientation);
                    isSkipOnCreate = true;
                    ((DefaultSoftKeyboardJAJP)((IWnnLanguageSwitcher)mInputMethodBase).getInputViewManager()).wrapStartNavigationKeypad();
                } else {
                    mInputMethodBase = new IWnnLanguageSwitcher(this);
                }
                ((IWnnLanguageSwitcher)mInputMethodBase).mInputConnection
                        = getCurrentInputConnection();
                if (DEBUG) {Log.d("InputMethodSwitcher", "change navigation.");}
                mNavigationMode = true;
                break;
            default:
                if (DEBUG) {Log.d("InputMethodSwitcher", "no find type.");}
                break;
        }
        if (!isSkipOnCreate) {
            mInputMethodBase.onCreate();
            mInputMethodBase.onCreateFloatingView();
            v = mInputMethodBase.onCreateInputView();
        } else {
            mInputMethodBase.onCreateFloatingView();
            if (mInputMethodBase instanceof IWnnLanguageSwitcher) {
                v = ((IWnnLanguageSwitcher)mInputMethodBase).getInputViewManager().getCurrentView();
            }
        }
        if (v != null) {
            ViewGroup vg = (ViewGroup)(v.getParent());
            if (vg != null) {
                vg.removeView(v);
            }
            setInputView(v);
        }
        if (!isSkipOnCreate) {
            candidatesView = mInputMethodBase.onCreateCandidatesView();
        } else {
            if (mInputMethodBase instanceof IWnnLanguageSwitcher) {
                candidatesView = ((IWnnLanguageSwitcher)mInputMethodBase).getCandidateViewManager().getCurrentView();
            }
        }
        if (candidatesView != null) {
            setCandidatesView(candidatesView);
        }

        if ((v != null) && (candidatesView != null)
                && (mInputMethodBase.isNotExpandedFloatingMode())) {
            mInputMethodBase.onStartInputView(getCurrentInputEditorInfo(), false);
        }

        if (mode != IME_TYPE_HANDWRITING) {
            restartSelf(mAttribute);
        }

        mMode = mode;
    }

    /**
     * Restarts the IME.
     *
     * @param attribute  Description of the type of text being edited.
     */
    private void restartSelf(EditorInfo attribute) {
        mAttribute = attribute;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RESTART), 0);
    }

    /**
     * Free mImeMap resource.
     */
    public void freeImeMap() {
        // Before clearing the cache map, free resources of each map entries.
        for(Map.Entry<Integer, InputMethodBase> e : mIwnnImeMap.entrySet()) {
            // The map contains mInputMethodBase that is current ime instance, so do not free resources.
            if (e.getValue() != mInputMethodBase) {
                ((IWnnLanguageSwitcher)e.getValue()).freeResources();
            }
        }
        mIwnnImeMap.clear();
        return;
    }

    /**
     * Change show Floating keyboard flag.
     * 
     * @param show  {@code true} if show floating keyboard.
     */
    public void setEnableFloating(boolean show) {
        mEnableFloating = show;
    }

    /**
     * Check show floating keyboard flag.
     *
     * @return boolean  show floating flag.
     */
    public boolean isEnableFloating() {
        return mEnableFloating;
    }

}
