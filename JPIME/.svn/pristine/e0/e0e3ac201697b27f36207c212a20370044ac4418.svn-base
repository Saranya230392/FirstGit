/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package com.lge.handwritingime;

import java.util.ArrayList;
import java.util.Locale;

import jp.co.omronsoft.iwnnime.ml.HWEvent;
import jp.co.omronsoft.iwnnime.ml.InputMethodSwitcher;
import jp.co.omronsoft.iwnnime.ml.SoundManager;
import jp.co.omronsoft.iwnnime.ml.WnnWord;
import jp.co.omronsoft.iwnnime.ml.WrapTextCandidatesViewManager;
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

import com.lge.handwritingime.manager.ThemeManager;
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
    
//	// ㄱ ㄲ ㄴ ㄷ ㄸ ㄹ ㅁ ㅂ ㅃ ㅅ ㅆ ㅇ ㅈ ㅉ ㅊ ㅋ ㅌ ㅍ ㅎ		
//	static final char[] mChoSung = { 0x3131, 0x3132, 0x3134, 0x3137, 0x3138,
//			0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148,
//			0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e };
//	
//	// ㅏ ㅐ ㅑ ㅒ ㅓ ㅔ ㅕ ㅖ ㅗ ㅘ ㅙ ㅚ ㅛ ㅜ ㅝ ㅞ ㅟ ㅠ ㅡ ㅢ ㅣ		
//	static final char[] mJwungSung = { 0x314f, 0x3150, 0x3151, 0x3152,
//			0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a,
//			0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162,
//			0x3163 };
//	
//	// ㄱ ㄲ ㄳ ㄴ ㄵ ㄶ ㄷ ㄹ ㄺ ㄻ ㄼ ㄽ ㄾ ㄿ ㅀ ㅁ ㅂ ㅄ ㅅ ㅆ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ		
//	static final char[] mJongSung = { 0, 0x3131, 0x3132, 0x3133, 0x3134,
//			0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d,
//			0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146,
//			0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e };
    
    class WrapWnnEngine extends jp.co.omronsoft.iwnnime.ml.WrapWnnEngine {
    	private String mPredicted;
    	private boolean mPredictedShown;
    	private boolean mHasUpperHead;
    	private boolean mStopped;
    	
//		public String hangulToJaso(String s) {
//			int indexChoSung, indexJwungSung, indexJongSung;
//			String result = "";
//
//			for (int i = 0; i < s.length(); i++) {
//				char ch = s.charAt(i);
//
//				if (ch >= 0xAC00 && ch <= 0xD7A3) {
//					indexJongSung = ch - 0xAC00;
//					indexChoSung = indexJongSung / (21 * 28);
//					indexJongSung = indexJongSung % (21 * 28);
//					indexJwungSung = indexJongSung / 28;
//					indexJongSung = indexJongSung % 28;
//
//					result = result + mChoSung[indexChoSung] + mJwungSung[indexJwungSung];
//					if (indexJongSung != 0)
//						result = result + mJongSung[indexJongSung];
//				} else {
//					result = result + ch;
//				}
//			}
//			return result;
//		}
    	
		@Override
		public WnnWord getNextCandidate() {
			if (mStopped) {
				return null;
			}
			
			WnnWord result = super.getNextCandidate();
			
			// Korean Check
//			if (mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_KR))) {
//				String checkingString = new String(mPredicted);
//				char lastChar = checkingString.charAt(checkingString.length()-1);
//				if (lastChar < 0xAC00 || lastChar > 0xD7A3) {
//					checkingString.substring(0, checkingString.length()-1);
//				}
//				
//				while (result != null && checkingString.length() <= result.candidate.length() &&
//						!checkingString.equals(result.candidate.substring(0, checkingString.length()))) {
//					Log.e(TAG, "checking string=" + checkingString + " result.candidate=" + result.candidate);
//					result = super.getNextCandidate();
//				}
//				if (result != null) {
//					Log.e(TAG, "checking string=" + checkingString + " result.candidate=" + result.candidate);
//				}
//			}
			
			
			if (result != null) {
//				Log.e(TAG, "candidates=" + result.candidate + " mPredicted=" + mPredicted);
				if (result.candidate.equals(mPredicted)) {
					if (!mPredictedShown) {
						mPredictedShown = true;
					} else {
						result = super.getNextCandidate();
					}
				} 
			} else if (mHCandidatesList != null) {
				setCandidates(null);
				result = super.getNextCandidate();
			}
			// TODO : make sure it is okay.
			if (result != null && mHasUpperHead) {
				String str = result.candidate;
				str = (str.length() > 1) ? str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1) : 
					str.substring(0, 1).toUpperCase(Locale.ENGLISH);
				result.candidate = str;
			}
			
			return result;
		}

		@Override
		public int predict(String str) {
			mStopped = false;
			
			mPredicted = new String(str);
			mPredictedShown = false;
			
			// Korean Check
//			if (mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_KR))) {
//				str = hangulToJaso(str);
//			}
						
			// TODO : make sure it is okay.
			if(str.length() > 0 && Character.isUpperCase(str.charAt(0))) {
				mHasUpperHead = true;
				str = (str.length() > 1) ? str.substring(0, 1).toLowerCase(Locale.ENGLISH) + str.substring(1) : 
					str.substring(0, 1).toLowerCase(Locale.ENGLISH);
			} else {
				mHasUpperHead = false;
			}
			
			int result = super.predict(str);
			mHCandidatesList = new String[] {mPredicted};
			
			return result;
		}
		
		@Override
		public void setCandidates(String[] strs) {
			mStopped = false;
			super.setCandidates(strs);
		}

		public void stop() {
			mStopped = true;
		}
    }
    
    
    public BaseHandwritingKeyboard(InputMethodService ims) {
        super(ims);
    }

    @Override
    public void onCreate() {
        hideStatusIcon();
        
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
        if (DEBUG) Log.i(TAG, "onCreate()");
    }
    
    private WrapWnnEngine getInitializedDictionaryEngine() {
        WrapWnnEngine dictionaryEngine = new WrapWnnEngine();
        dictionaryEngine.init(getFilesDir().getPath());
        return dictionaryEngine;
    }
    
    @Override
    public void goIwnnIme() {
        if (DEBUG) Log.i(TAG, "goIwnnIME()");
        if (mIsCandidatesViewShown) {
            setCandidatesViewVisibility(false);
        }
        mDictionaryEngine.close();
        changeMode(InputMethodSwitcher.IME_TYPE_IWNNIME);
    }

    @Override
    public void goNaviKey() {
        if (DEBUG) Log.i(TAG, "goNaviKey()");
        mDictionaryEngine.close();
        if (mIsCandidatesViewShown) {
            setCandidatesViewVisibility(false);
        }
        changeMode(InputMethodSwitcher.IME_TYPE_NAVIGATION);
    }

    @Override
    public void goSymbolList() {
        if (DEBUG) Log.i(TAG, "goSymbolList()");
        mDictionaryEngine.close();
        if (mIsCandidatesViewShown) {
            setCandidatesViewVisibility(false);
        }
        changeMode(InputMethodSwitcher.IME_TYPE_SYMBOL);
//        changeMode(InputMethodSwitcher.IME_TYPE_EMOJIPALLETE);
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
    
    private void createInputView() {
        getPrefSettings();
        mThemeManager = new ThemeManager(getApplicationContext());

//        mInputView = (LinearLayout) getLayoutInflater().inflate(R.layout.handwriting_ime, null);
        mInputView = (LinearLayout) mThemeManager.inflate(R.layout.handwriting_ime, null);
                
        mStrokeLayout = (FrameLayout) mInputView.findViewWithTag("storkeLayout");
        mStrokeViewGroupLayout = (FrameLayout) mInputView.findViewWithTag("strokeViewGroupLayout");
        setModeManager();
        if (mStrokeManager.isEmpty()) {
            mModeManager.clear();
        }

        LinearLayout buttonLayout = (LinearLayout) mInputView.findViewWithTag("buttonView");
        mButtonLayoutManager.onCreateButtonLayout(buttonLayout);

//      mCandidateScrollView = (ScrollView) mInputView.findViewById(R.id.candidateScrollView);
//      mCandidateLayout = (FrameLayout) mCandidateScrollView.findViewById(R.id.candidateLayout);
//
//      ImageButton folderButton = (ImageButton) mInputView.findViewById(R.id.folder);
//      folderButton.setOnClickListener(mClickListener);

//      mbFoldCandidateView = true;
        
        if (DEBUG) Log.i(TAG, "createInputView() mInputView "+mInputView);
    }

    @Override
    public View onCreateInputView() {
        createInputView();
        return mInputView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        if(mThemeManager.isThemeChanged()) {
            createInputView();
            setInputView(mInputView);
        }
        
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
        if (mIsCandidatesViewShown) {
            setCandidatesViewVisibility(false);
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
        if (mModeManager == null) {
            if (DEBUG) Log.d(TAG, "mModeManager is null!");
            return;
        }
        
//        if (mPenColor != null && mPenType != null) {
//            mModeManager.setPenType(mPenColor, mPenType);
//        }
//        mModeManager.setAutoScrollWidth(mNextScreenWidth);
        mModeManager.setValuesByPrefs(mSharedPref);
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
        if (DEBUG) Log.d(TAG, "onCreateCandidtaesView size=" + size.x + ", " + size.y);
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
    public boolean isCandidatesViewVisible() {
    	return mIsCandidatesViewShown;
    }
    
    @Override
    public void stopCandidates() {
        mDictionaryEngine.stop();
    }

    @Override
    public void updateCandidateView() {
        if (DEBUG) Log.d(TAG, "updateCandidateView isInputViewShown()=" + isInputViewShown());
        @SuppressWarnings("unchecked")
        ArrayList<String> suggestions = mModeManager.getSuggestions();
        if (suggestions == null || suggestions.isEmpty()) {
            setCandidatesViewVisibility(false);
            return ;
        }
        
        // TODO : doing this in mModeManager, maybe.
        if (mModeName.equals(getString(R.string.HW_MODE_TEXT))) {
        	// TODO : make sure it is okay to use. Unless then, cannot be merged.
            if (suggestions.get(0).matches("[a-zA-Z-'.,]*")) {
                changeEngine(WrapWnnEngine.ENGLISH);
            } else {
            	changeEngine();
            }
            
        	if (suggestions.get(0).contains(" ")) {
        		mDictionaryEngine.setCandidates(new String[] {suggestions.get(0)});
        	} else {
	            mDictionaryEngine.predict(suggestions.get(0));
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
        
//        setCandidatesViewVisibility(false);
        // TODO : This is just for test.
        //        We should change the arguments depends on handwriting status.
        int language = WrapWnnEngine.JAPANESE;
        if (mWorkingLang.equals(getString(R.string.HW_RESOURCE_LANG_JP))) {
            language = WrapWnnEngine.JAPANESE;
        } else {
            language = WrapWnnEngine.KOREAN;
        }
        if (mDictionaryEngine.getLanguage() == language) {
        	return;
        }
        
        mDictionaryEngine.setDictionary(language, WrapWnnEngine.NORMAL);
//        mCandidatesViewManager.setWnnEngine(mDictionaryEngine);
    }
    
    private void changeEngine(int language) {
        if (mCandidatesViewManager == null || mDictionaryEngine == null ||
        		mDictionaryEngine.getLanguage() == language) {
            return ;
        }
        
        switch(language) {
        case WrapWnnEngine.JAPANESE:
        case WrapWnnEngine.KOREAN:
        case WrapWnnEngine.ENGLISH:
        	break;
    	default:
        	return;
        }
        
      	mDictionaryEngine.setDictionary(language, WrapWnnEngine.NORMAL);
        
//        setCandidatesViewVisibility(false);
//        mDictionaryEngine.setDictionary(language, WrapWnnEngine.NORMAL);
//        mCandidatesViewManager.setWnnEngine(mDictionaryEngine);
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
    public void touchFeedback(Object tag) {
        int vibDefaultId = getResources().getIdentifier("key_vibration_default_value", "bool", getPackageName());
        boolean isVibOn = (vibDefaultId != 0) ? getResources().getBoolean(vibDefaultId) : false;
        isVibOn = mSharedPref.getBoolean("key_vibration", isVibOn);
        
        if (DEBUG) Log.d(TAG, "isVibOn=" + isVibOn);
        if (isVibOn) {
            Vibrator vib = (Vibrator) HandwritingKeyboard.getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
            
            int vibTimeId = getResources().getIdentifier("vibrate_time", "integer", getPackageName());
            int vibrateTime = (vibTimeId != 0) ? getResources().getInteger(vibTimeId) : 100;
//            vib.vibrate(vibrateTime);
            vib.vibrate(new long[] {0, vibrateTime}, -1);
        }

//        int soundDefaultId = getResources().getIdentifier("key_sound_default_value", "bool", getPackageName());
//        boolean isSoundOn = (soundDefaultId != 0) ? getResources().getBoolean(vibDefaultId) : true;
        boolean isSoundOn = mSharedPref.getBoolean("key_sound", true);
        
        if (DEBUG) Log.d(TAG, "isSoundOn=" + isSoundOn);
        if (isSoundOn) {
            int keySound = SOUND_STANDARD_KEY;
            
            if (tag instanceof String) {
                String tagString = (String) tag;
                if (tagString.equals("enter")) {
                    keySound = SOUND_RETURN_KEY;
                } else if (tagString.equals("space")) {
                	keySound = SOUND_SPACEBAR_KEY;
                } else if (tagString.equals("delete")) {
                    keySound = SOUND_DELETE_KEY;
                }
            }
            
            SoundManager.getInstance().play(keySound);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (DEBUG) Log.i(TAG, "onConfigurationChanged() " + newConfig.orientation);
        dismissAllPopups();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd, int candidatesStart,
            int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        if (DEBUG) Log.d(TAG, "oldSelStart" + oldSelStart + " oldSelEnd" + oldSelEnd +
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
        if (DEBUG) Log.i(TAG, "onDestroy()");
        if (mModeManager != null) {
            mModeManager.clear();
        }
        mCore.destroy();
        mCandidatesViewManager.close();
        mDictionaryEngine.close();
        if (mHandler != null) {
	        mHandler.removeMessages(MSG_DELETE_TEXT);
	        mHandler.removeMessages(MSG_SUBMIT_STROKES);
	        mHandler.removeMessages(MSG_UPDATE_CANDIDATES);
	        mHandler = null;
        }
        super.onDestroy();
    }
}
