package com.lge.handwritingime;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lge.handwritingime.data.Stroke;
import com.lge.handwritingime.manager.ButtonLayoutManager;
import com.lge.handwritingime.manager.CharModeManager;
import com.lge.handwritingime.manager.IModeManager;
import com.lge.handwritingime.manager.StrokeManager;
import com.lge.handwritingime.manager.TextModeManager;
import com.lge.handwritingime.manager.ThemeManager;
import com.lge.handwritingime.preference.HandwritingPreferenceActivity;
import com.lge.ime.util.LgeImeCliptrayManager;
import com.lge.voassist.Core;
//import com.lge.voassist.Core.RECOGNITION_STATE;
import com.visionobjects.im.EventListener;
import com.visionobjects.im.Result;

public class HandwritingKeyboard extends KeyboardParent {
    public static final boolean DEBUG = true;
    private static final String TAG = "LGHWIMEHandwritingKeyboard";
    
    private static final int SUBMIT_DELAY = 250;

    private static final float POSITION_BASELINE = 80f;
    private static final float POSITION_XHEIGHT = 40f;
    private static final float POSITION_LINESPACING = 20f;
    
    // data, manager
    public ThemeManager mThemeManager;
    protected StrokeManager mStrokeManager;
    public IModeManager mModeManager;
    protected ButtonLayoutManager mButtonLayoutManager;
    protected Core mCore;
    private boolean mIsCommiting;

    // UI side
    protected LinearLayout mInputView;
    public FrameLayout mStrokeLayout;
    public FrameLayout mStrokeViewGroupLayout;

    // from shared pref.
    protected SharedPreferences mSharedPref;

    public String mModeName;
    protected String mWorkingLang;
    private String mWorkingLangJapanType;

    public HandwritingHandler mHandler;
    // EventListener which get result from recognizer.
    protected EventListener mRecognizerListener = new EventListener() {
        @Override
        public void onRecognitionEnd() {
            mIsCommiting = false;
            if (DEBUG) Log.i(TAG, "[onRecognitionEnd()] mIsCommiting=" + mIsCommiting);
            Result result = mCore.getResult(true, true);
            if (result == null)
                return;
            
            mModeManager.setResult(result);
            result.destroy();

            Message msg = new Message();
            msg.what = MSG_UPDATE_CANDIDATES;
            msg.arg1 = 0;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onRecognitionStart(int arg0, int arg1, int arg2) {
            super.onRecognitionStart(arg0, arg1, arg2);
            mIsCommiting = true;
            if (DEBUG) Log.i(TAG, "[onRecognitionStart()] mIsCommiting=" + mIsCommiting);
        }

        @Override
        public void onCancel() {
            super.onCancel();
            mIsCommiting = false;
            if (DEBUG) Log.i(TAG, "[onCancel()] mIsCommiting=" + mIsCommiting);
        }

        @Override
        public void onCancelFailed(RuntimeException arg0) {
            super.onCancelFailed(arg0);
            if (DEBUG) Log.i(TAG, "[onCancelFailed()] mIsCommiting=" + mIsCommiting);
        }

        @Override
        public void onCommit() {
            super.onCommit();
            if (DEBUG) Log.i(TAG, "[onCommit()] mIsCommiting=" + mIsCommiting);
        }

        @Override
        public void onCommitFailed(RuntimeException arg0) {
            super.onCommitFailed(arg0);
            if (DEBUG) Log.i(TAG, "[onCommitFailed()] mIsCommiting=" + mIsCommiting);
        }

        @Override
        public void onRecognitionFailed(RuntimeException arg0) {
            super.onRecognitionFailed(arg0);
            if (DEBUG) Log.i(TAG, "[onRecognitionFailed()] mIsCommiting=" + mIsCommiting);
        }
    };
    
    public HandwritingKeyboard() {
        initDatasAfterConstruct();
    }
        
    public HandwritingKeyboard(InputMethodService ims) {
        super(ims);
        initDatasAfterConstruct();
    }
    
    private void initDatasAfterConstruct() {
        mStrokeManager = StrokeManager.getInstance();
        mButtonLayoutManager = new ButtonLayoutManager(this);
        mHandler = new HandwritingHandler(this);

        mWorkingLang = "";
        mWorkingLangJapanType = "";
        mModeName = "";
        mIsCommiting = false;
    }
        
    public void goHWSetting() {
        startActivity(new Intent(getBaseContext(), HandwritingPreferenceActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void goSettingMenu() {
        startActivity(new Intent(getBaseContext(), HandwritingPreferenceActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    
    public void goSymbolList() {
        // TODO Auto-generated method stub
    }
    
    public void showClipTray() {
        if (!LgeImeCliptrayManager.isSupportCliptray(getBaseContext())) {
            return;
        }
        LgeImeCliptrayManager cliptrayMngr = LgeImeCliptrayManager.getInstances(getBaseContext());
        if (cliptrayMngr == null) { 
            return;
        }
        cliptrayMngr.showClipTray(getCurrentInputEditorInfo());
        cliptrayMngr.finishCliptrayService();
        LgeImeCliptrayManager.release();
    }

    public void goNaviKey() {
        InputConnection ic = getCurrentInputConnection();
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
    }

    public void goIwnnIme() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledImList = inputMethodManager.getEnabledInputMethodList();

        for (int i = 0; i < enabledImList.size(); i++) {
            if (enabledImList.get(i).getPackageName().equals("jp.co.omronsoft.iwnnime.ml")) {
                switchInputMethod(enabledImList.get(i).getId());
            }
        }
    }

    public void deleteStroke() {
        mModeManager.deleteStroke();
        mButtonLayoutManager.checkDeleteState();
    }

    public void deleteOne() {
        mModeManager.deleteOne();
        mButtonLayoutManager.checkDeleteState();
    }

    public void deleteText() {
        mModeManager.deleteText();
    }
    
    public void setButtonEnabled(String tag, boolean enabled) {
        mButtonLayoutManager.setButtonEnabled(tag, enabled);
    }

    public boolean dismissAllPopups() {
        boolean result = false;
        if (mModeManager != null) {
            result |= mModeManager.dismissCharButtonPopups();
        }
        if (mButtonLayoutManager != null) {
            result |= mButtonLayoutManager.dismissAllPopups();
        }
        return result;
    }

    public void setModeManager() {
        if (mSharedPref == null) {
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }
        mIsCommiting = false;
        if (mHandler != null) {
            mHandler.removeMessages(MSG_DELETE_TEXT);
            mHandler.removeMessages(MSG_SUBMIT_STROKES);
            mHandler.removeMessages(MSG_UPDATE_CANDIDATES);
        }
        
        mModeName = mSharedPref.getString(getString(R.string.HW_PREF_KEY_WORKING_ACTIVE_MODE),
                getString(R.string.HW_MODE_TEXT));

        if (DEBUG) Log.i(TAG, "[changeMode] mModeName " + mModeName);

        // TODO : destroy mStrokeLayoutManager only when mModeName is changed.
        // at the moment, mModeName changed in many place.
        // so, fix them first, then add flag check.
        boolean isCharMode = mModeName.equals(getString(R.string.HW_MODE_CHAR));
        if (mModeManager != null) {
            // NOTE : DO NOT clear ModeManager. 
            //        When orientation changed, it will clear all Stroke informations.
            // mModeManager.clear();
            mModeManager.destroy();
        }
        mModeManager = (isCharMode) ? new CharModeManager(this) : new TextModeManager(this);
        if (mInputView == null) {
            Log.e(TAG, "mInputView is null!", new RuntimeException());
            return;
        }
        // TODO : we need to make one method to set Views like mStrokeLayout.
        //        without the method it is difficult to find bugs.
        if (mStrokeLayout == null) {
            mStrokeLayout = (FrameLayout) mInputView.findViewWithTag("strokeLayout");
        }
        
        mModeManager.onCreateStrokeLayout(mStrokeLayout);
        mModeManager.setValuesByPrefs(mSharedPref);
    }
    
    public void submitStrokes() {
        String mode = (mModeName.equals(getString(R.string.HW_MODE_CHAR))) ? getString(R.string.HW_RESOURCE_MODE_CHAR_ALL)
                : getString(R.string.HW_RESOURCE_MODE_TEXT_ALL);
        submitStrokes(mode);
    }

    public String getResourceMode(String mode) {
        if (!mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_JP))) {
            return mode;
        }

        int[][] modes = new int[][] {
                { R.string.HW_LANGUAGE_JAPAN_ALL, R.string.HW_RESOURCE_MODE_CHAR_ALL,
                        R.string.HW_RESOURCE_MODE_TEXT_ALL },
                { R.string.HW_LANGUAGE_JAPAN_KANJI, R.string.HW_RESOURCE_MODE_CHAR_KANJI,
                        R.string.HW_RESOURCE_MODE_TEXT_KANJI },
                { R.string.HW_LANGUAGE_JAPAN_HIRAGANA, R.string.HW_RESOURCE_MODE_CHAR_HIRAGANA,
                        R.string.HW_RESOURCE_MODE_TEXT_HIRAGANA },
                { R.string.HW_LANGUAGE_JAPAN_KATAKANA, R.string.HW_RESOURCE_MODE_CHAR_KATAKANA,
                        R.string.HW_RESOURCE_MODE_TEXT_KATAKANA },
                { R.string.HW_LANGUAGE_JAPAN_ENGLISH, R.string.HW_RESOURCE_MODE_CHAR_ENGLISH,
                        R.string.HW_RESOURCE_MODE_TEXT_ENGLISH },
                { R.string.HW_LANGUAGE_JAPAN_NUMBER, R.string.HW_RESOURCE_MODE_DIGIT, R.string.HW_RESOURCE_MODE_NUMBER },
                { R.string.HW_LANGUAGE_JAPAN_SYMBOL, R.string.HW_RESOURCE_MODE_CHAR_SYMBOL,
                        R.string.HW_RESOURCE_MODE_TEXT_SYMBOL } };
        int mode_index = (mode.equalsIgnoreCase(getString(R.string.HW_MODE_CHAR))) ? 1 : 2;
        for (int i = 0; i < modes.length; i++) {
            if (mWorkingLangJapanType.equals(getString(modes[i][0]))) {
                if (DEBUG) Log.d(TAG, "mode = " + getString(modes[i][mode_index]));
                return getString(modes[i][mode_index]);
            }
        }
        return mode;
    }

    public void submitStrokes(String mode) {
        if (mCore == null) {
            try {
                mCore = new Core(getApplicationContext());
                mCore.setRecognizerListener(mRecognizerListener);
            } catch (IOException e) {
                Log.e(TAG, "Recognizer not initialized!", e);
                return;
            }
        }
        if (mModeManager != null && mModeManager instanceof TextModeManager) {
            ((TextModeManager) mModeManager).removeCharButtonAll();
        }
        
        
        if (DEBUG) Log.d(TAG, "submitStrokes() mIsCommiting=" + mIsCommiting);
        if (mIsCommiting) {
            mHandler.removeMessages(MSG_SUBMIT_STROKES);
            mHandler.sendEmptyMessageDelayed(MSG_SUBMIT_STROKES, SUBMIT_DELAY);
            return;
        }

        mode = getResourceMode(mode);
        if (DEBUG) Log.i(TAG, "[submitStrokes()] mWorkingLang=" + mWorkingLang + " mode=" + mode);
        mCore.setMode(mWorkingLang, mode);
        mCore.setPositionAndScaleIndicator(POSITION_BASELINE, POSITION_XHEIGHT, POSITION_LINESPACING);

        for (Stroke s : mStrokeManager.getAll()) {
            mCore.addStroke(s);
        }
        mCore.commit();
        mButtonLayoutManager.checkDeleteState();
    }

    public void updateCandidateView() {
        if (mModeManager.getSuggestions() != null && !mModeManager.getSuggestions().isEmpty()) {
            setCandidatesViewVisibility(true);
        }
        if (DEBUG) Log.d(TAG, "updateCandidateView()");
    }
    
    public void pickSuggestionManually(int index) {
        // TODO : 1. naming 2. parameter 3. working
        mModeManager.pickSuggestionManually(index);
    }
    public void pickSuggestionManually(String string) {
        // TODO : 1. naming 2. parameter 3. working
        mModeManager.pickSuggestionManually(string);
    }

//    public void clearStrokeViewData() {
//        mModeManager.clear();
//
//        setCandidatesViewVisibility(false);
//        updateCandidateView();
//    }

    /**
     * This is kind-a-wrapper method of setCandidatesViewShown().
     * It is needed to integrate with iWnnIME.
     * 
     * @param visibility visibility of candidatesView.
     */
    public void setCandidatesViewVisibility(boolean visibility) {
        setCandidatesViewShown(visibility);
    }
    
    public boolean isCandidatesViewVisible() {
        return (mOriginalInputMethodSwitcher.getCandidatesHiddenVisibility() != View.INVISIBLE);
    }
    
    /**
     * Set the selected index of candidates.
     * This is temporary methods for integrating with iWnnIME.
     * 
     * @param index the index of selected candidate
     */
    public void setCandidatesSelectedIndex(int index) {
//        if (mCandidateView != null) {
//            mCandidateView.setSelectedIndex(index);
//        }
    }
    

    public void clearAll() {
        mModeManager.clear();
        setCandidatesViewVisibility(false);
        getPrefSettings();
        updateCandidateView();
        mButtonLayoutManager.checkDeleteState();
    }

    public void getPrefSettings() {        
        if (mSharedPref == null) {
            Log.w(TAG, "mSharedPref is null, get again");
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }
        mModeName = mSharedPref.getString(getString(R.string.HW_PREF_KEY_WORKING_ACTIVE_MODE),
                getString(R.string.HW_MODE_TEXT));
        if (DEBUG) Log.d(TAG, "mModeName=" + mModeName);
    }

    public void getPrefLangSettings() {
        if (mSharedPref == null) {
            Log.w(TAG, "mSharedPref is null, get again");
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }
        mWorkingLang = mSharedPref.getString(getString(R.string.HW_PREF_KEY_WORKING_LANGUAGE),
                getString(R.string.HW_RESOURCE_LANG_JP));
        mWorkingLangJapanType = mSharedPref.getString(getString(R.string.HW_PREF_KEY_LANGUAGE_JAPAN_TYPE),
                getString(R.string.HW_LANGUAGE_JAPAN_ALL));
        if (DEBUG) Log.d(TAG, "mWorkingLang=" + mWorkingLang + " mWorkingLangJapanType" + mWorkingLangJapanType);
    }

    public void checkDeleteButton() {
        mButtonLayoutManager.checkDeleteState();
    }
    
    public void touchFeedback() {
//        Vibrator vib = (Vibrator) HandwritingKeyboard.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
//        vib.vibrate(100);
    }
    public void touchFeedback(int id) {
//        Vibrator vib = (Vibrator) HandwritingKeyboard.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
//        vib.vibrate(100);
    }
    public void touchFeedback(Object tag) {
//        Vibrator vib = (Vibrator) HandwritingKeyboard.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
//        vib.vibrate(100);
    }

    public static final int MSG_UPDATE_CANDIDATES = 0;
    protected static final int MSG_SUBMIT_STROKES = 1;
    public static final int MSG_DELETE_TEXT = 2;

    public static class HandwritingHandler extends Handler {
        private final WeakReference<HandwritingKeyboard> mRef;

        public HandwritingHandler(HandwritingKeyboard keyboard) {
            mRef = new WeakReference<HandwritingKeyboard>(keyboard);
        }

        @Override
        public void handleMessage(Message msg) {
            HandwritingKeyboard handwritingKeyboard = mRef.get();

            switch (msg.what) {
            case MSG_UPDATE_CANDIDATES:
                handwritingKeyboard.updateCandidateView();
                handwritingKeyboard.setCandidatesSelectedIndex(msg.arg1);
                break;
            case MSG_SUBMIT_STROKES:
                handwritingKeyboard.submitStrokes();
                break;
            case MSG_DELETE_TEXT:
                handwritingKeyboard.deleteText();
                break;
            }
        }
    }

    public void stopCandidates() {
        // TODO Auto-generated method stub
    }
}
