package jp.co.omronsoft.iwnnime.ml;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodSubtype;
//import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnSymbolEngine;

public class InputMethodSwitcher extends InputMethodService {

    /** for DEBUG */
    private static boolean DEBUG = true;

    public boolean mSymbolMode = false;
    public boolean mNavigationMode = false;

    /* The type of IME*/
    public static final int IME_TYPE_HANDWRITING = 0;
    public static final int IME_TYPE_IWNNIME =     1;
    public static final int IME_TYPE_SYMBOL =      2;
    public static final int IME_TYPE_NAVIGATION =  3;

    public InputMethodBase mInputMethodBase;

    /** EditorInfo */
    protected EditorInfo mAttribute = null;

    /** Message for {@code mHandler} (restart) */
    private static final int MSG_RESTART = 7;

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

    @Override
    public void onCreate() {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::onCreate()");}

        if(mInputMethodBase == null){
//            mInputMethodBase = new IWnnLanguageSwitcher(this);
        }
        super.onCreate(); // See superOnCreate() comment.
        mInputMethodBase.onCreate();
    }

    public void superOnCreate() {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superOnCreate()");}
        // TODO:Can not call super.onCreate().
        // If a software keyboard is opened, an aborting will be carried out by WindowManager.
        // [Error Message] Attempted to add input method window with bad token null. Aborting.
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::onStartInputView()");}
        mInputMethodBase.onStartInputView(info, restarting);
    }

    public void superOnStartInputView(EditorInfo info, boolean restarting) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superOnStartInputView()");}
        super.onStartInputView(info, restarting);
    }

    @Override
    public void onStartInput(EditorInfo info, boolean restarting){
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
//    synchronized public boolean onEvent(HWEvent ev) {
//        if (mInputMethodBase instanceof BaseHandwritingKeyboard) {
//            mInputMethodBase.onEvent(ev);
//        }
//        return false;
//    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::onFinishInputView()");}
        mInputMethodBase.onFinishInputView(finishingInput);
    }

    public void superOnFinishInputView(boolean finishingInput) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superOnFinishInputView()");}
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onWindowShown() {
        mInputMethodBase.onWindowShown();
    }

    public void superOnWindowShown() {
        super.onWindowShown();
    }

    @Override
     public void onConfigurationChanged(Configuration newConfig) {
        mInputMethodBase.onConfigurationChanged(newConfig);
     }

     public void superOnConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
     }

    @Override
     public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd){
         mInputMethodBase.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
     }
     public void superOnUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd){
         super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
     }
    @Override
     public void onDestroy() {
         mInputMethodBase.onDestroy();
     }
     public void superOnDestroy() {
         super.onDestroy();
     }
    @Override
     public LayoutInflater getLayoutInflater () {
         return mInputMethodBase.getLayoutInflater();
     }
     public LayoutInflater superGetLayoutInflater () {
         return super.getLayoutInflater();
     }
    @Override
     public void switchInputMethod(String id) {
         mInputMethodBase.switchInputMethod(id);
     }
     public void superSwitchInputMethod(String id) {
         super.switchInputMethod(id);
     }
    @Override
     public void setCandidatesViewShown(boolean shown) {
         mInputMethodBase.setCandidatesViewShown(shown);
     }
     public void superSetCandidatesViewShown(boolean shown) {
         super.setCandidatesViewShown(shown);
     }
    @Override
     public View onCreateInputView() {
         return mInputMethodBase.onCreateInputView();
     }
     public View superOnCreateInputView() {
         return super.onCreateInputView();
     }
    @Override
     public View onCreateCandidatesView() {
         return mInputMethodBase.onCreateCandidatesView();
     }
     public View superOnCreateCandidatesView() {
         return super.onCreateCandidatesView();
     }

    @Override
     public void setCandidatesView(View view) {
         mInputMethodBase.setCandidatesView(view);
     }
     public void superSetCandidatesView(View view) {
         super.setCandidatesView(view);
     }
    @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         return mInputMethodBase.onKeyDown(keyCode, event);
     }
     public boolean superOnKeyDown(int keyCode, KeyEvent event) {
         return super.onKeyDown(keyCode, event);
     }
    @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         return mInputMethodBase.onKeyUp(keyCode, event);
     }
     public boolean superOnKeyUp(int keyCode, KeyEvent event) {
         return super.onKeyUp(keyCode, event);
     }
    @Override
     public boolean onKeyLongPress(int keyCode, KeyEvent event) {
         return mInputMethodBase.onKeyLongPress(keyCode, event);
     }
     public boolean superOnKeyLongPress(int keyCode, KeyEvent event) {
         return super.onKeyLongPress(keyCode, event);
     }
    @Override
     public InputConnection getCurrentInputConnection() {
         return mInputMethodBase.getCurrentInputConnection();
     }
     public InputConnection superGetCurrentInputConnection() {
         return super.getCurrentInputConnection();
     }
    @Override
     public void startActivity(Intent intent) {
         mInputMethodBase.startActivity(intent);
     }
     public void superStartActivity(Intent intent) {
         super.startActivity(intent);
     }
    @Override
     public Context getBaseContext() {
         return InputMethodBase.getBaseContext();
     }
     public Context superGetBaseContext() {
         return super.getBaseContext();
     }
    @Override
     public Context getApplicationContext() {
         return mInputMethodBase.getApplicationContext();
     }
     public Context superGetApplicationContext() {
         return super.getApplicationContext();
     }
    @Override
     public Object getSystemService(String name) {
         return mInputMethodBase.getSystemService(name);
     }
     public Object superGetSystemService(String name) {
         return super.getSystemService(name);
     }
    @Override
     public Resources getResources() {
         return mInputMethodBase.getResources();
     }
     public Resources superGetResources() {
         return super.getResources();
     }

    @Override
     public void onUpdateExtractingViews(EditorInfo el){
         mInputMethodBase.onUpdateExtractingViews(el);
     }
     public void superOnUpdateExtractingViews(EditorInfo el){
         super.onUpdateExtractingViews(el);
     }
    @Override
     public void requestHideSelf(int flags){
         mInputMethodBase.requestHideSelf(flags);
     }
     public void superRequestHideSelf(int flags){
         super.requestHideSelf(flags);
     }
    @Override
     public void hideWindow(){
         mInputMethodBase.hideWindow();
         mHandler.removeMessages(MSG_RESTART);
     }
     public void superHideWindow(){
         super.hideWindow();
     }
    @Override
     public View onCreateExtractTextView(){
         return mInputMethodBase.onCreateExtractTextView();
     }
     public View superOnCreateExtractTextView(){
         return super.onCreateExtractTextView();
     }
    @Override
    public void onFinishInput(){
        mInputMethodBase.onFinishInput();
    }
    public void superOnFinishInput(){
        super.onFinishInput();
    }
    @Override
    public String getPackageName(){
        return mInputMethodBase.getPackageName();
    }
    public String superGetPackageName(){
        return super.getPackageName();
    }
    @Override
    public void showWindow(boolean showInput){
        mInputMethodBase.showWindow(showInput);
    }
    public void superShowWindow(boolean showInput){
        super.showWindow(showInput);
    }
    @Override
    public void hideStatusIcon(){
        mInputMethodBase.hideStatusIcon();
    }
    public void superHideStatusIcon(){
        super.hideStatusIcon();
    }
    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags){
        return mInputMethodBase.bindService(service, conn, flags);
    }
    public boolean superBindService(Intent service, ServiceConnection conn, int flags){
        return super.bindService(service, conn, flags);
    }
    @Override
    public void unbindService(ServiceConnection conn){
        mInputMethodBase.unbindService(conn);
    }
    public void superUnbindService(ServiceConnection conn){
        super.unbindService(conn);
    }
    @Override
    public void onComputeInsets(InputMethodService.Insets outInsets) {
        mInputMethodBase.onComputeInsets(outInsets);
    }
    public void superOnComputeInsets(InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
    }
    @Override
    public void onExtractedTextClicked(){
        mInputMethodBase.onExtractedTextClicked();
    }
    public void superOnExtractedTextClicked(){
        super.onExtractedTextClicked();
    }
    @Override
    public File getFilesDir(){
        return superGetFilesDir();
    }
    public File superGetFilesDir(){
        return super.getFilesDir();
    }
    @Override
    public boolean isInputViewShown(){
        return mInputMethodBase.isInputViewShown();
    }

    public boolean superIsInputViewShown(){
        return super.isInputViewShown();
    }
    @Override
    public void setInputView(View view){
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::setInputView()");}
        mInputMethodBase.setInputView(view);
    }

    public void superSetInputView(View view){
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::superSetInputView()");}
        super.setInputView(view);
    }
    @Override
    public void sendDownUpKeyEvents(int keyEventCode){
        mInputMethodBase.sendDownUpKeyEvents(keyEventCode);
    }

    public void superSendDownUpKeyEvents(int keyEventCode){
        super.sendDownUpKeyEvents(keyEventCode);
    }
    @Override
    public void sendKeyChar(char charCode){
        mInputMethodBase.sendKeyChar(charCode);
    }

    public void superSendKeyChar(char charCode){
        super.sendKeyChar(charCode);
    }
    @Override
    public void updateFullscreenMode(){
        mInputMethodBase.updateFullscreenMode();
    }

    public void superUpdateFullscreenMode(){
        super.updateFullscreenMode();
    }
    @Override
    public EditorInfo getCurrentInputEditorInfo(){
        return mInputMethodBase.getCurrentInputEditorInfo();
    }

    public EditorInfo superGetCurrentInputEditorInfo(){
        return super.getCurrentInputEditorInfo();
    }
    @Override
    public  boolean onEvaluateFullscreenMode(){
        return mInputMethodBase.onEvaluateFullscreenMode();
    }

    public  boolean superOnEvaluateFullscreenMode(){
        return super.onEvaluateFullscreenMode();
    }

    @Override
    public boolean onEvaluateInputViewShown(){
        return mInputMethodBase.onEvaluateInputViewShown();
    }

    public boolean superOnEvaluateInputViewShown(){
        return super.onEvaluateInputViewShown();
    }

    @Override
    protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype){
        mInputMethodBase.onCurrentInputMethodSubtypeChanged(newSubtype);
    }

    protected void superOnCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype){
        super.onCurrentInputMethodSubtypeChanged(newSubtype);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode){
        return mInputMethodBase.getSharedPreferences(name, mode);
    }

    public SharedPreferences superGetSharedPreferences(String name, int mode){
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public Dialog getWindow(){
        return mInputMethodBase.getWindow();
    }

    public Dialog superGetWindow(){
        return super.getWindow();
    }

    @Override
    public PackageManager getPackageManager(){
        return mInputMethodBase.getPackageManager();
    }
    public PackageManager superGetPackageManager(){
        return super.getPackageManager();
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mInputMethodBase.sendBroadcast(intent);
    }

    public void superSendBroadcast(Intent intent) {
        super.sendBroadcast(intent);
    }

    @Override
    public void showStatusIcon(int iconResId) {
        mInputMethodBase.showStatusIcon(iconResId);
    }

    public void superShowStatusIcon(int iconResId) {
        super.showStatusIcon(iconResId);
    }

    public void changeInputMethod( int mode ) {
        if (DEBUG) {Log.d("InputMethodSwitcher", "InputMethodSwitcher::changeInputMethod.");}

        mAttribute = getCurrentInputEditorInfo();
        View v,candidatesView;

        if (mInputMethodBase != null){
            mInputMethodBase.onFinishInput();
        }

        mSymbolMode = false;
        mNavigationMode = false;

//        switch (mode) {
//            case IME_TYPE_HANDWRITING:
//                if (DEBUG) {Log.d("InputMethodSwitcher", "change handwriting ime.");}
//                mInputMethodBase = new BaseHandwritingKeyboard(this);
//                break;
//            case IME_TYPE_IWNNIME:
//                if (DEBUG) {Log.d("InputMethodSwitcher", "change iwnnime.");}
//                mInputMethodBase = new IWnnLanguageSwitcher(this);
//               ((IWnnLanguageSwitcher)mInputMethodBase).mInputConnection = getCurrentInputConnection();
//                break;
//            case IME_TYPE_SYMBOL:
//                if (DEBUG) {Log.d("InputMethodSwitcher", "change iwnnime.");}
//                mInputMethodBase = new IWnnLanguageSwitcher(this);
//               ((IWnnLanguageSwitcher)mInputMethodBase).mInputConnection = getCurrentInputConnection();
//                if (DEBUG) {Log.d("InputMethodSwitcher", "change symbol.");}
//                mSymbolMode = true;
//                break;
//            case IME_TYPE_NAVIGATION:
//                mInputMethodBase = new IWnnLanguageSwitcher(this);
//               ((IWnnLanguageSwitcher)mInputMethodBase).mInputConnection = getCurrentInputConnection();
//                if (DEBUG) {Log.d("InputMethodSwitcher", "change symbol.");}
//                mNavigationMode = true;
//                break;
//            default:
//                if (DEBUG) {Log.d("InputMethodSwitcher", "no find type.");}
//                break;
//        }
        mInputMethodBase.onCreate();
        v = mInputMethodBase.onCreateInputView();
        if (v != null) {
            setInputView(v);
        }
        candidatesView = mInputMethodBase.onCreateCandidatesView();
        if (candidatesView != null) {
            setCandidatesView(candidatesView);
        }
        //if (mode == mInputMethodBase.IME_TYPE_IWNNIME) {
        if (mode != IME_TYPE_HANDWRITING) {
            restartSelf(mAttribute);
        }
    }

    private void restartSelf(EditorInfo attribute) {
        mAttribute = attribute;
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RESTART), 0);
    }

}
