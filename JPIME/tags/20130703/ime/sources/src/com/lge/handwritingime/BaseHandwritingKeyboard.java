package com.lge.handwritingime;

import java.util.ArrayList;

import jp.co.omronsoft.iwnnime.ml.HWEvent;
import jp.co.omronsoft.iwnnime.ml.InputMethodSwitcher;
import jp.co.omronsoft.iwnnime.ml.SoundManager;
import jp.co.omronsoft.iwnnime.ml.WrapTextCandidatesViewManager;
import jp.co.omronsoft.iwnnime.ml.WrapWnnEngine;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.InputMethodService.Insets;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lge.voassist.Core;

public class BaseHandwritingKeyboard extends HandwritingKeyboard {
    private static final String TAG = "BaseHandwritingKeyboard";
    public static final boolean DEBUG = HandwritingKeyboard.DEBUG;
    
    
    /** Sound DeleteKey */
    private static final int SOUND_DELETE_KEY = 1;
    /** IME Sound change ReturnKey */
    private static final int SOUND_RETURN_KEY = 2;
    /** IME Sound change SpacebarKey */
    private static final int SOUND_SPACEBAR_KEY = 3;
    /** IME Sound change StandardKey */
    private static final int SOUND_STANDARD_KEY = 4;

    private WrapTextCandidatesViewManager mCandidatesViewManager;
    private View mCandidatesView;
    private boolean mIsCandidatesViewShown;
    private WrapWnnEngine mDictionaryEngine;

    public BaseHandwritingKeyboard(InputMethodService ims) {
        super(ims);
    }

    @Override
    public void onCreate() {
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        getPrefLangSettings();

        mCandidatesViewManager = new WrapTextCandidatesViewManager();
        mDictionaryEngine = getInitializedDictionaryEngine();
        // TODO : This is just for test.
        //        We should change the arguments depends on handwriting status.
        int language = WrapWnnEngine.JAPANESE;
        if (mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_JP))) {
            language = WrapWnnEngine.JAPANESE;
        } else {
            language = WrapWnnEngine.KOREAN;
        }
        mDictionaryEngine.setDictionary(language, WrapWnnEngine.NORMAL);
        mCandidatesViewManager.setWnnEngine(mDictionaryEngine);

        mStrokeManager.clear();

        try {
            mCore = new Core(getApplicationContext());
            mCore.setRecognizerListener(mRecognizerListener);
        } catch (Exception e) {
            Log.e(TAG, "recognizer not initialized!", e);
        }
        super.onCreate();
        Log.i(TAG, "onCreate()");
    }
    
    private WrapWnnEngine getInitializedDictionaryEngine() {
        WrapWnnEngine dictionaryEngine = new WrapWnnEngine();
        dictionaryEngine.init(getFilesDir().getPath());
        return dictionaryEngine;
    }
    
    @Override
    public void goIwnnIme() {
        Log.i(TAG, "goIwnnIME()");
        mDictionaryEngine.close();
        changeMode(InputMethodSwitcher.IME_TYPE_IWNNIME);
    }

    @Override
    public void goNaviKey() {
        if (DEBUG) Log.i(TAG, "goNaviKey()");
        mDictionaryEngine.close();
        changeMode(InputMethodSwitcher.IME_TYPE_NAVIGATION);
    }

    @Override
    public void goSymbolList() {
        if (DEBUG) Log.i(TAG, "goSymbolList()");
        mDictionaryEngine.close();
//        changeMode(InputMethodSwitcher.IME_TYPE_SYMBOL);
        changeMode(InputMethodSwitcher.IME_TYPE_EMOJIPALLETE);
    }
    

    @Override
    public void goSettingMenu() {
        ComponentName name = new ComponentName("jp.co.omronsoft.iwnnime.ml",
                "jp.co.omronsoft.iwnnime.ml.ControlPanelStandard");
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(name).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "not found " + name.toShortString());
            super.goSettingMenu();
        }
    }

    @Override
    public View onCreateInputView() {
        Log.i(TAG, "onCreateInputView() mInputView "+mInputView);
        getPrefSettings();

        mInputView = (LinearLayout) getLayoutInflater().inflate(R.layout.handwriting_ime, null);
//        Log.e(TAG, "R.layout.handwriting_ime=" + R.layout.handwriting_ime);
//        PackageManager manager = getPackageManager();
//        Resources resources = getResources();
//        try {
//            Context packageContext = getBaseContext().createPackageContext("com.lge.handwritingime.kbd.black", Context.CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY);
//            resources = manager.getResourcesForApplication("com.lge.handwritingime.kbd.black");
//            int resID = resources.getIdentifier("handwriting_ime", "layout", "com.lge.handwritingime.kbd.black");
//            mInputView = (LinearLayout) View.inflate(packageContext, resID, null);
//            
//            Log.e(TAG, "OK");
//        } catch (NameNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        mStrokeLayout = (FrameLayout) mInputView.findViewById(R.id.strokeLayout);
        mStrokeViewGroupLayout = (FrameLayout) mInputView.findViewById(R.id.strokeViewGroupLayout);
        setModeManager();
        if (mStrokeManager.isEmpty()) {
            mModeManager.clear();
        }

        LinearLayout buttonLayout = (LinearLayout) mInputView.findViewById(R.id.buttonView);
        mButtonLayoutManager.onCreateButtonLayout(buttonLayout);

//        mCandidateScrollView = (ScrollView) mInputView.findViewById(R.id.candidateScrollView);
//        mCandidateLayout = (FrameLayout) mCandidateScrollView.findViewById(R.id.candidateLayout);
//
//        ImageButton folderButton = (ImageButton) mInputView.findViewById(R.id.folder);
//        folderButton.setOnClickListener(mClickListener);

        mbFoldCandidateView = true;
        return mInputView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        if (mModeName.equals(getString(R.string.HW_MODE_CHAR)) || mStrokeManager.isEmpty()) {
            mModeManager.clear();
        }
//        mButtonLayoutManager.setPrefSettingIcon();
        // TODO : This is just for test.
        //        We should change the arguments depends on handwriting status.
        int language = WrapWnnEngine.JAPANESE;
        if (mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_JP))) {
            language = WrapWnnEngine.JAPANESE;
        } else {
            language = WrapWnnEngine.KOREAN;
        }
        mDictionaryEngine.setDictionary(language, WrapWnnEngine.NORMAL);
        mCandidatesViewManager.setPreferences(mSharedPref);
        updateCandidateView();
        
        if (DEBUG) Log.i(TAG, "onStartInputView restarting=" + restarting);
        super.onStartInputView(info, restarting);
    }    

    @Override
    public void onFinishInputView(boolean finishingInput) {
        if (DEBUG) Log.d(TAG, "[onFinishInputView()] mModeName=" + mModeName +
                " finishingInput=" + finishingInput + " mIsCandidatesViewShown=" + mIsCandidatesViewShown);
        if (mModeName.equals(getString(R.string.HW_MODE_CHAR))) {
            mModeManager.clear();
        }
        dismissAllPopups();
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();

        // TODO : many many duplicated codes here
        String modeName = mSharedPref.getString(getString(R.string.HW_PREF_KEY_WORKING_ACTIVE_MODE),
                getString(R.string.HW_MODE_TEXT));
        String workingLang = mSharedPref.getString(getString(R.string.HW_PREF_KEY_WORKING_LANGUAGE),
                getString(R.string.HW_RESOURCE_LANG_JP));
        boolean bSubmitStrokes = false;
        if (!workingLang.equals(mWorkingLang))
            bSubmitStrokes = true;
        getPrefSettings();
        if (bSubmitStrokes && mModeName.equals(getString(R.string.HW_MODE_TEXT)))
            submitStrokes();
        if (!mModeName.equals(modeName)) {
            clearAll();
            setModeManager();
        }

        // TODO : NPE mModeManager is not initialized
        if (mModeManager != null && mPenColor != null && mPenType != null) {
            mModeManager.setPenType(mPenColor, mPenType);
        }
        mModeManager.setAutoScrollWidth(mNextScreenWidth);
        if (DEBUG) Log.i(TAG, "onWindowShown " + mWorkingLang);
    }

    @Override
    public View onCreateCandidatesView() {
//        LinearLayout layout = new LinearLayout(getBaseContext());
//        layout.setOrientation(LinearLayout.VERTICAL);
//        View blankView = new View(getBaseContext());
//        int blankHeight = (int) getResources().getDimension(R.dimen.candidate_blank_height);
//        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, blankHeight);
//        blankView.setLayoutParams(params);
//        blankView.setVisibility(View.INVISIBLE);
//        layout.addView(blankView);
//
//        mCandidateView = new CandidateView(getBaseContext());
//        mCandidateView.setService(this);
//        if (mModeName.equals(getString(R.string.HW_MODE_CHAR))) {
//            mCandidateView.setCharMode(true);
//        } else {
//            mCandidateView.setCharMode(false);
//        }
//        layout.addView(mCandidateView);
//
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        mCandidatesView = mCandidatesViewManager.initView(this, size.x, size.y);
        return mCandidatesView;
    }

    // NOTE : mDictionaryEngine.predict() or setCandidates() should be called before this
    @Override
    public void setCandidatesViewVisibility(boolean shown) {
        if (DEBUG) Log.d(TAG, "setCandidatesViewShown shown=" + shown);
        mIsCandidatesViewShown = shown;
        if (shown) {
            if (isInputViewShown()) {
                mCandidatesViewManager.displayCandidates();
            }
            mCandidatesViewManager.setEnableCandidateLongClick(
                    mModeName.equals(getString(R.string.HW_MODE_TEXT)));
        } else {
            mCandidatesViewManager.clearCandidates();
        }
    }

    @Override
    public void updateCandidateView() {
        ArrayList<String> suggestions = mModeManager.getSuggestions();
        if (suggestions == null || suggestions.isEmpty()) {
            setCandidatesViewVisibility(false);
            return ;
        }
        // TODO : doing this in mModeManager, maybe.
        if (mModeName.equals(getString(R.string.HW_MODE_TEXT))) {
            if (mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_JP))) {
                mDictionaryEngine.predict(suggestions.get(0));
            } else {
                mDictionaryEngine.setCandidates(new String[] {suggestions.get(0)});
            }
        } else {
            mDictionaryEngine.setCandidates(suggestions.toArray(new String[suggestions.size()]));
        }
        setCandidatesViewVisibility(true);
    }
    
    private void changeEngine() {
        if (mCandidatesViewManager == null || mDictionaryEngine == null) {
            return ;
        }
        
        setCandidatesViewVisibility(false);
        // TODO : This is just for test.
        //        We should change the arguments depends on handwriting status.
        int language = WrapWnnEngine.JAPANESE;
        if (mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_JP))) {
            language = WrapWnnEngine.JAPANESE;
        } else {
            language = WrapWnnEngine.KOREAN;
        }
        mDictionaryEngine.setDictionary(language, WrapWnnEngine.NORMAL);
        mCandidatesViewManager.setWnnEngine(mDictionaryEngine);
    }
    
    @Override
    public void getPrefLangSettings() {
        String prevWorkingLang = new String(mWorkingLang);
        super.getPrefLangSettings();
        if (!prevWorkingLang.equals(mWorkingLang)) {
            changeEngine();
        }
    }

    @Override
    @Deprecated
    public void setCandidatesSelectedIndex(int index) {
        if (mModeName.equals(getString(R.string.HW_MODE_CHAR))) {
            mCandidatesViewManager.setFocusCandidate(index);
        }
    }

    @Override
    public void onComputeInsets(Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (mIsCandidatesViewShown && mCandidatesView != null) {
            int blankHeight = (int) getResources().getDimension(R.dimen.candidate_blank_height);
            outInsets.contentTopInsets -= (mCandidatesView.getHeight()-blankHeight);
            outInsets.visibleTopInsets += blankHeight;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dismissAllPopups())
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    

    @Override
    public void touchFeedback(int id) {
        //key_vibration R.bool.key_vibration_default_value
        //pref.getBoolean("key_sound", true); key_sound_default_value
        int vibDefaultId = getResources().getIdentifier("key_vibration_default_value", "bool", getPackageName());
        boolean isVibOn = (vibDefaultId != 0) ? getResources().getBoolean(vibDefaultId) : true;
        isVibOn = mSharedPref.getBoolean("key_vibration", isVibOn);
        
        if (isVibOn) {
            Vibrator vib = (Vibrator) HandwritingKeyboard.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
            
            int vibTimeId = getResources().getIdentifier("vibrate_time", "integer", getPackageName());
            int vibrateTime = (vibTimeId != 0) ? getResources().getInteger(vibTimeId) : 100;
            vib.vibrate(vibrateTime);
        }

        int soundDefaultId = getResources().getIdentifier("key_sound_default_value", "bool", getPackageName());
        boolean isSoundOn = (soundDefaultId != 0) ? getResources().getBoolean(vibDefaultId) : true;
        isSoundOn = mSharedPref.getBoolean("key_sound", isSoundOn);
        
        if (isSoundOn) {
            int keySound = SOUND_STANDARD_KEY;
            switch (id) {
            case R.id.enter:
                keySound = SOUND_SPACEBAR_KEY;
                break;
            case R.id.space:
                keySound = SOUND_SPACEBAR_KEY;
                break;
            case R.id.delete:
                keySound = SOUND_DELETE_KEY;
                break;
            default:
                break;
            }
            
            SoundManager.getInstance().play(keySound);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged() " + newConfig.orientation);
        dismissAllPopups();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd, int candidatesStart,
            int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        Log.d(TAG, "oldSelStart" + oldSelStart + " oldSelEnd" + oldSelEnd +
                " newSelStart" + newSelStart + " newSelEnd" + newSelEnd +
                " candidatesStart" + candidatesStart + "candidatesEnd" + candidatesEnd);

        mModeManager.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
    }

    @Override
    public synchronized boolean onEvent(HWEvent ev) {
        switch(ev.code){
            case HWEvent.LIST_CANDIDATES_NORMAL:
                mInputView.setVisibility(View.VISIBLE);
                break;
            case HWEvent.LIST_CANDIDATES_FULL:
                mInputView.setVisibility(View.GONE);
                break;
                //Text display -> init screen -> invisible candidate view
            case HWEvent.SELECT_CANDIDATE:
                // TODO : doing it in mModeManager
                pickSuggestionManually(ev.string);
                if (mModeName.equals(getString(R.string.HW_MODE_TEXT))) {
                    setCandidatesViewVisibility(false);
                } else {
                    // TODO : pickSuggestionManually is actually calling this,
                    //        but, it is not keep highlight.
                    //        I suspect that WeakReference in Handler is referring
                    //        HandwritingKeyboard - not Base.
                    setCandidatesSelectedIndex(ev.attribute);
                }
                break;
//            case HWEvent.UPDATE_CANDIDATE:
//                break;
//            case HWEvent.CALL_MUSHROOM:
//                break;
            default :
                if (DEBUG) Log.i(TAG, "HWEvent " + "no Event");
                break;
        }
        return super.onEvent(ev);
    }


    @Override
    public void onDestroy() {
        if (mModeManager != null) {
            mModeManager.clear();
        }
        mCore.destroy();
        mCandidatesViewManager.close();
        mDictionaryEngine.close();
        super.onDestroy();
    }
}
