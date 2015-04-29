/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import java.io.File;
import java.lang.reflect.Field;

import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.SoundManager;

import com.lge.handwritingime.BaseHandwritingKeyboard;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.inputmethodservice.ExtractEditText;
import android.inputmethodservice.InputMethodService;
import android.util.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodSubtype;

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


    public static InputMethodSwitcher mOriginalInputMethodSwitcher;

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

     public View superOnCreateInputView() {
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
         superSetInputView(view);
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
            //Object object = f.get(this);
            //extractEditText = (ExtractEditText)object;
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

    /* LGEmoji palette */
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return mOriginalInputMethodSwitcher.registerReceiver(receiver, filter);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        mOriginalInputMethodSwitcher.unregisterReceiver(receiver);
    }

    public SoundManager getSoundManager() {
        return SoundManager.getInstance();
    }
}
