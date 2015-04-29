package com.lge.handwritingime.manager;

import java.util.Arrays;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.lge.handwritingime.HandwritingKeyboard;
import com.lge.handwritingime.R;
import com.lge.handwritingime.TextDrawable;
import com.lge.handwritingime.popup.SelectDeleteButtonPopup;
import com.lge.handwritingime.popup.SelectDeleteButtonPopup.OnClickDeletePopupListener;
import com.lge.handwritingime.popup.SelectKeyboardPopup;
import com.lge.handwritingime.popup.SelectKeyboardPopup.OnClickKeyboardPopupListener;
import com.lge.handwritingime.popup.SelectLanguageSettingPopup;
import com.lge.handwritingime.popup.SelectLanguageSettingPopup.OnClickLanguagePopupListener;


//class R {}

public class ButtonLayoutManager implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    private static final String TAG = "LGHWIMEButtonLayoutManager";
    private static final boolean DEBUG = HandwritingKeyboard.DEBUG;
    
    public static final boolean ENABLE_BUTTON_DISABLING = true; 
    
    public static final int STATE_ENTER_ENTER = 0x0;
    public static final int STATE_ENTER_OK = 0x1;
    
    private HandwritingKeyboard mKeyboard;
    private StrokeManager mStrokeDataManager;
    
    private TextDrawable mEnterDrawable;
    private String mEnterString;

    // initialized at onCreateButtonLayout
    private LinearLayout mButtonViewLayout;
    private SelectKeyboardPopup mSelectKeyboardPopup;
    private SelectLanguageSettingPopup mSelectLanguageSettingPopup;
    private SelectDeleteButtonPopup mSelectDeleteButtonPopup;
    
    private OnClickLanguagePopupListener mLanguagePopupListener = new OnClickLanguagePopupListener() {
        @Override
        public void onClick(Object tag) {
            String stringTag = null;
            if (tag instanceof String) {
                stringTag = (String) tag;
            }
            String[] inputModes = new String[] {
                    mKeyboard.getString(R.string.HW_LANGUAGE_JAPAN_ALL),
                    mKeyboard.getString(R.string.HW_LANGUAGE_JAPAN_KANJI),
                    mKeyboard.getString(R.string.HW_LANGUAGE_JAPAN_HIRAGANA),
                    mKeyboard.getString(R.string.HW_LANGUAGE_JAPAN_KATAKANA),
                    mKeyboard.getString(R.string.HW_LANGUAGE_JAPAN_ENGLISH),
                    mKeyboard.getString(R.string.HW_LANGUAGE_JAPAN_NUMBER),
                    mKeyboard.getString(R.string.HW_LANGUAGE_JAPAN_SYMBOL),
                    mKeyboard.getString(R.string.HW_RESOURCE_LANG_KR)
            };
            
            if (stringTag.equals("btnChar") && mKeyboard.mModeManager instanceof TextModeManager ||
                    stringTag.equals("btnText") && mKeyboard.mModeManager instanceof CharModeManager) {
                mKeyboard.clearAll();
                mKeyboard.setModeManager();
                checkSettingState();
//                setPrefSettingIcon();
            } else if (Arrays.asList(inputModes).contains(stringTag)) {
                mKeyboard.getPrefLangSettings();
                if (mKeyboard.mModeManager != null && mKeyboard.mModeManager instanceof TextModeManager) {
                    ((TextModeManager) mKeyboard.mModeManager).removeCharButtonAll();
                }
                mKeyboard.submitStrokes();
            }
        }
    };
    
    private OnClickKeyboardPopupListener mKeyboardPoupListener = new OnClickKeyboardPopupListener() {
        @Override
        public void onClick(Object tag) {
            String stringTag = null;
            if (tag instanceof String) {
                stringTag = (String) tag;
            }
            
            if (stringTag.equals("keyboardSetting")) {
                mKeyboard.goSettingMenu();
            } else if (stringTag.equals("keyboardClip")) {
                mKeyboard.showClipTray();
            } else if (stringTag.equals("keyboardInput")) {
                mKeyboard.goIwnnIme();
            }
        }
    };
    
    private OnClickDeletePopupListener mDeletePopupListener = new OnClickDeletePopupListener() {
        @Override
        public void onClick(Object tag) {
            String stringTag = null;
            if (tag instanceof String) {
                stringTag = (String) tag;
            }
            
            if (stringTag.equals("deleteAll")) {
                mKeyboard.clearAll();
            } else if (stringTag.equals("deleteOne")) {
                mKeyboard.deleteOne();
            } else if (stringTag.equals("deleteStroke")) {
                mKeyboard.deleteStroke();
            }
        }
    };
    
    int mButtonViewHeight;
//    private SharedPreferences mSharedPref;

    public ButtonLayoutManager(HandwritingKeyboard handwritingKeyboard) {
        super();
        this.mKeyboard = handwritingKeyboard;
        mStrokeDataManager = StrokeManager.getInstance();
    }
//0504 UI comment
   /*  
    public void setPrefSettingIcon() {
        String activeMode = mSharedPref.getString(mKeyboard.getString(R.string.HW_PREF_KEY_WORKING_ACTIVE_MODE),
                mKeyboard.getString(R.string.HW_MODE_TEXT));
        ImageButton btn = (ImageButton) mButtonViewLayout.findViewById(R.id.settings);        
//        btn.setImageResource(R.drawable.ime_keypad_handwriting_icon);
      
        if (activeMode.equals(mKeyboard.getString(R.string.HW_MODE_CHAR))) {
            btn.setImageResource(R.drawable.vo_stylus_cursive);
        } else {
            btn.setImageResource(R.drawable.vo_stylus_iso);
        }
    }
*/
//0504 UI comment
    public void onCreateButtonLayout(LinearLayout buttonViewLayout) {
        if (buttonViewLayout == null) {
            Log.e(TAG, "ButtonViewLayout is null!", new RuntimeException());
        }
                
        mButtonViewLayout = buttonViewLayout;
        mButtonViewHeight = mKeyboard.getResources().getDimensionPixelSize(R.dimen.button_layout_height);
//        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mKeyboard.getApplicationContext());

//        int[] imageButtonResources = new int[] { R.id.settings, R.id.emoji, R.id.keyboard, R.id.space, R.id.backward,
//                R.id.forward, R.id.enter, R.id.delete, };
        
        String[] imageButtonTags = new String[] {"settings", "emoji", "keyboard", "space", "backward",
                "forward", "enter", "delete"};
        String[] longClickableButtons = new String[] {"settings", "keyboard", "backward", "delete"};
         
        // NOTE : beforeScreen, afterScreen, folder doesn't use LongClick
//        for (int res : imageButtonResources) {
        for (String s : imageButtonTags) {
//            ImageButton btn = (ImageButton) mButtonViewLayout.findViewById(res);
            ImageButton btn = (ImageButton) mButtonViewLayout.findViewWithTag(s);
            if (btn == null) {
                if (DEBUG) Log.w(TAG, "there is no view like " + s);
                continue;
            }
            btn.setOnClickListener(this);
            btn.setOnTouchListener(this);
//          if (btnId == R.id.settings || btnId == R.id.keyboard || btnId == R.id.backward || btnId == R.id.delete) {
            if (Arrays.asList(longClickableButtons).contains(s)) {
                btn.setOnLongClickListener(this);
            }
            
            if (btn.getTag().equals("space")) {
                btn.setEnabled(mStrokeDataManager.isEmpty());
            }

//            int id = btn.getId();
//            if (id == R.id.settings) {  
//            } else if (id == R.id.keyboard) {
//                btn.setBackgroundResource(R.drawable.button_state_keypad_normal);
//            } else if (id == R.id.backward) {
//                btn.setBackgroundResource(R.drawable.button_state_keypad_normal);
//            }
        }

//        setPrefSettingIcon();
        checkDeleteState();
        checkSettingState();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mKeyboard.touchFeedback(v.getTag());
            break;
        default:
            break;
        }
        return false;
    }
    @Override
    public void onClick(View v) {        
        InputConnection ic = mKeyboard.getCurrentInputConnection();
        if (ic == null) {
            return;
        }

        mKeyboard.dismissAllPopups();
        
        boolean isInputting = false;
        if (mKeyboard.isCandidatesViewVisible()) {
            isInputting = true;
        }
        
        String tag = ((v.getTag() instanceof String) ? (String) v.getTag() : null);
        if (tag == null) {
            return;
        } else if (tag.equals("settings")) {
            mSelectLanguageSettingPopup = new SelectLanguageSettingPopup(mKeyboard.mThemeManager, v);
            mSelectLanguageSettingPopup.show(0, mButtonViewHeight + 10);
            mSelectLanguageSettingPopup.setOnClickLanguagePopupListener(mLanguagePopupListener);
        } else if (tag.equals("emoji")) {
            mKeyboard.goSymbolList();
        } else if (tag.equals("keyboard")) {
            mKeyboard.goIwnnIme();
        } else if (tag.equals("space")) {
            if (isInputting) {
                // do nothing
            } else { // !isInputting
                // ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                mKeyboard.sendKeyChar(' ');
            }
        } else if (tag.equals("backward")) {
            mKeyboard.mModeManager.dPadBackward();
        } else if (tag.equals("forward")) {
            mKeyboard.mModeManager.dPadForward();
        } else if (tag.equals("enter")) {
            if (isInputting) {
                if (mKeyboard.mModeManager instanceof TextModeManager) {
                    try {
                        mKeyboard.pickSuggestionManually(0);
                        mKeyboard.updateCandidateView();
                    } catch (NullPointerException e) {
                        // do nothing
                    }
                } else {
                    ((CharModeManager)mKeyboard.mModeManager).finishComposingText();
                    mKeyboard.mModeManager.clear();
                    mKeyboard.setCandidatesViewVisibility(false);
                }
            } else { // !isInputting 
                //ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                mKeyboard.sendKeyChar('\n');
            }
        } else if (tag.equals("delete")) {
            if (mStrokeDataManager.isEmpty()) {
                mKeyboard.deleteText();
                return;
            }
            mKeyboard.deleteStroke();
        }
    }

    public void checkDeleteState() {
//        ImageButton btn = (ImageButton) mButtonViewLayout.findViewById(R.id.delete);
        if (mButtonViewLayout == null) {
            return;
        }
        
        mEnterDrawable = null;
        mEnterString = null;
        
        ImageButton btn = (ImageButton) mButtonViewLayout.findViewWithTag("delete");
        int resId = R.drawable.hw_ime_keypad_icon_del;
        if (btn != null) {
            if (mStrokeDataManager != null && !mStrokeDataManager.isEmpty()) {
                resId = R.drawable.hw_ime_keypad_icon_del_stroke;
            }
            
            if (mKeyboard != null && mKeyboard.mThemeManager != null ) {
                btn.setImageResource(mKeyboard.mThemeManager.getResId(resId));
            }
        }
    }
    
    public void checkSettingState() {
        if (mButtonViewLayout == null) {
            return;
        }
        
        ImageButton btn = (ImageButton) mButtonViewLayout.findViewWithTag("setting");
        int resId = R.drawable.hw_ime_keypad_handwriting_icon;
        if (btn != null) {
            if (mKeyboard.mModeName.equals(R.string.HW_MODE_CHAR)) {
                // TODO : change to CHAR MODE icon.
                resId = R.drawable.hw_ime_keypad_handwriting_icon;
            }            
            if (mKeyboard != null && mKeyboard.mThemeManager != null ) {
                btn.setImageResource(mKeyboard.mThemeManager.getResId(resId));
            }
        }
    }
    
    public void setButtonEnabled(String tag, boolean enabled) {
        if (ENABLE_BUTTON_DISABLING && mButtonViewLayout != null) {
            View v = mButtonViewLayout.findViewWithTag(tag);
            if (v != null) {
                v.setEnabled(enabled);
            }
        }
    }
    
    public void changeEnterState(int state) {
        if (mButtonViewLayout == null) {
            return;
        }
        ImageButton enterButton = (ImageButton) mButtonViewLayout.findViewWithTag("enter");
        if (enterButton == null) {
            return;
        }
        
        switch (state) {
        case STATE_ENTER_OK:
            String enterString = mKeyboard.getString(R.string.button_enter_ok);
            
            if (mEnterDrawable == null || mEnterString == null || !mEnterString.equals(enterString)) {
                mEnterString = enterString;
                float textSize = mKeyboard.getResources().getDimension(R.dimen.hw_ime_keypad_wht_btn_func_text_normal);
                float gap = mKeyboard.getResources().getDimension(R.dimen.hw_ime_keypad_wht_btn_gap_bottom);
                int textColor = mKeyboard.mThemeManager.getColor(R.color.hw_ime_keypad_wht_btn_func_text_normal);
                int shadowColor = mKeyboard.mThemeManager.getColor(R.color.hw_ime_keypad_wht_btn_func_text_normal_shadow);
                mEnterDrawable = new TextDrawable(enterString, textSize, textColor, shadowColor);
                mEnterDrawable.setGapBottom(gap);
            }
            
            enterButton.setImageDrawable(mEnterDrawable);
            break;
            
        case STATE_ENTER_ENTER:
        default:
            enterButton.setImageResource(mKeyboard.mThemeManager.getResId(R.drawable.hw_ime_keypad_icon_enter));
            break;
        }
    }

    public boolean dismissAllPopups() {
        boolean hasDismissed = false;
        if (mSelectLanguageSettingPopup != null && mSelectLanguageSettingPopup.isShowingPopup()) {
            mSelectLanguageSettingPopup.dismiss();
            hasDismissed = true;
        }
        if (mSelectDeleteButtonPopup != null && mSelectDeleteButtonPopup.isShowingPopup()) {
            mSelectDeleteButtonPopup.dismiss();
            hasDismissed = true;
        }
        if (mSelectKeyboardPopup != null && mSelectKeyboardPopup.isShowingPopup()) {
            mSelectKeyboardPopup.dismiss();
            hasDismissed = true;
        }

        return hasDismissed;
    }
    

    @Override
    public boolean onLongClick(View v) {
//        mKeyboard.touchFeedback();
        
//        InputMethodManager inputMethodManager = (InputMethodManager) mKeyboard
//                .getSystemService(Context.INPUT_METHOD_SERVICE);
        InputConnection ic = mKeyboard.getCurrentInputConnection();
        mKeyboard.dismissAllPopups();
        if (ic == null) {
            return false;
        }

//        int id = v.getId();
        String tag = ((v.getTag() instanceof String) ? (String) v.getTag() : null);
        
        if (tag == null) {
            return false;
        } else if (tag.equals("settings")) {
            mKeyboard.goHWSetting();
        } else if (tag.equals("keyboard")) {
            FrameLayout keyboardLayout = (FrameLayout) mButtonViewLayout.findViewWithTag("keyboardButtonLayout");
            mSelectKeyboardPopup = new SelectKeyboardPopup(mKeyboard.mThemeManager, v);
            int keyboardButtongetX = (int) keyboardLayout.getX();
            mSelectKeyboardPopup.show(keyboardButtongetX, mButtonViewHeight + 5);
            mSelectKeyboardPopup.setOnClickKeyboardPopupListener(mKeyboardPoupListener);
        } else if (tag.equals("emoji")) {
        } else if (tag.equals("backward")) {
            //ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
            mKeyboard.goNaviKey();
        } else if (tag.equals("forward")) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
        } else if (tag.equals("delete")) {
            if (!mStrokeDataManager.isEmpty()) {
                mSelectDeleteButtonPopup = new SelectDeleteButtonPopup(mKeyboard.mThemeManager, v);
                mSelectDeleteButtonPopup.show(0, mButtonViewHeight + 10);
                if (mKeyboard.mModeName.equals(mKeyboard.getString(R.string.HW_MODE_CHAR))) {
                    mSelectDeleteButtonPopup.mBtnDeleteAll.setVisibility(View.GONE);
                }
                mSelectDeleteButtonPopup.setOnClickDeletePopupListener(mDeletePopupListener);
            } else if (mStrokeDataManager.isEmpty()) {
                final ImageButton btnDelete = (ImageButton) mButtonViewLayout.findViewWithTag("delete");
                Thread deleteThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (btnDelete.isPressed()) {
                                Thread.sleep(100);
                                // mKeyboard.deleteText();
                                mKeyboard.mHandler.sendMessage(mKeyboard.mHandler
                                        .obtainMessage(HandwritingKeyboard.MSG_DELETE_TEXT));
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                deleteThread.start();
            }
        } else {
            return false;
        }
        return true;
    }
}
