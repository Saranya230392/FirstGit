package com.lge.handwritingime.popup;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lge.handwritingime.R;
import com.lge.handwritingime.manager.ThemeManager;

public class SelectLanguageSettingPopup extends BasePopup {

    private View mRoot;
    private SharedPreferences mSharedPref;

    public RelativeLayout mActiveModeBtnChar;
    public RelativeLayout mActiveModeBtnText;

    public ImageButton btn;

    private String mLanguageSetting;
    private String mJapaneseSetting;
    private String mActiveModeSetting;
    public String mActiveModeText;
    public String mActiveModeChar;
    public String mPrefKeyActiveMode;
    public String mPrefKeyWorkingLang;
    public String mPrefKeyJapaneseType;

//    int imageButtonResources[];
    private String[] mInputModes;

    public interface OnClickLanguagePopupListener {
//        public void onClick(int id);
        public void onClick(Object tag);
    }

    OnClickLanguagePopupListener mClickListener;

    public void setOnClickLanguagePopupListener(OnClickLanguagePopupListener l) {
        mClickListener = l;
    }

//    public SelectLanguageSettingPopup(View anchor) {
//        super(anchor);
//        mActiveModeText = mContext.getString(R.string.HW_MODE_TEXT);
//        mActiveModeChar = mContext.getString(R.string.HW_MODE_CHAR);
//        mPrefKeyActiveMode = mContext.getString(R.string.HW_PREF_KEY_WORKING_ACTIVE_MODE);
//        mPrefKeyWorkingLang = mContext.getString(R.string.HW_PREF_KEY_WORKING_LANGUAGE);
//        mPrefKeyJapaneseType = mContext.getString(R.string.HW_PREF_KEY_LANGUAGE_JAPAN_TYPE);
//
//        mRoot = (ViewGroup) mThemeManager.inflate(R.layout.select_language_setting_popup_layout, null);
//        mActiveModeBtnChar = (RelativeLayout) mRoot.findViewWithTag("btnChar");
//        mActiveModeBtnChar.setOnClickListener(OCL);
//        mActiveModeBtnText = (RelativeLayout) mRoot.findViewWithTag("btnText");
//        mActiveModeBtnText.setOnClickListener(OCL);
//
//        
//        mInputModes = new String[] {
//                mContext.getString(R.string.HW_LANGUAGE_JAPAN_ALL),
//                mContext.getString(R.string.HW_LANGUAGE_JAPAN_KANJI),
//                mContext.getString(R.string.HW_LANGUAGE_JAPAN_HIRAGANA),
//                mContext.getString(R.string.HW_LANGUAGE_JAPAN_KATAKANA),
//                mContext.getString(R.string.HW_LANGUAGE_JAPAN_ENGLISH),
//                mContext.getString(R.string.HW_LANGUAGE_JAPAN_NUMBER),
//                mContext.getString(R.string.HW_LANGUAGE_JAPAN_SYMBOL),
//                mContext.getString(R.string.HW_RESOURCE_LANG_KR)
//        };
//        
//        for (String s : mInputModes) {
//            btn = (ImageButton) mRoot.findViewWithTag(s);
//            if (btn != null) {
//                btn.setOnClickListener(OCL);
//            }
//        }
//        
////        imageButtonResources = new int[] { R.id.btnAllChar, R.id.btnKanjiChar, R.id.btnKataChar, R.id.btnHiraChar,
////                R.id.btnEngChar, R.id.btnNumChar, R.id.btnSymbolChar, R.id.btnKorChar };
////        
////        for (int res : imageButtonResources) {
////            btn = (ImageButton) mRoot.findViewById(res);
////            if (btn != null) {
////                btn.setOnClickListener(OCL);
////            }
////        }
//        
//        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
//    }

    public SelectLanguageSettingPopup(ThemeManager themeManager, View anchor) {
        super(themeManager, anchor);
        mActiveModeText = mContext.getString(R.string.HW_MODE_TEXT);
        mActiveModeChar = mContext.getString(R.string.HW_MODE_CHAR);
        mPrefKeyActiveMode = mContext.getString(R.string.HW_PREF_KEY_WORKING_ACTIVE_MODE);
        mPrefKeyWorkingLang = mContext.getString(R.string.HW_PREF_KEY_WORKING_LANGUAGE);
        mPrefKeyJapaneseType = mContext.getString(R.string.HW_PREF_KEY_LANGUAGE_JAPAN_TYPE);
        
        mRoot = (ViewGroup) mThemeManager.inflate(R.layout.select_language_setting_popup_layout, null);
        mActiveModeBtnChar = (RelativeLayout) mRoot.findViewWithTag("btnChar");
        mActiveModeBtnChar.setOnClickListener(OCL);
        mActiveModeBtnText = (RelativeLayout) mRoot.findViewWithTag("btnText");
        mActiveModeBtnText.setOnClickListener(OCL);
        
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        
        mInputModes = new String[] {
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_ALL),
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_KANJI),
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_HIRAGANA),
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_KATAKANA),
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_ENGLISH),
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_NUMBER),
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_SYMBOL),
                mContext.getString(R.string.HW_RESOURCE_LANG_KR)
        };
        
        for (String s : mInputModes) {
            btn = (ImageButton) mRoot.findViewWithTag(s);
            if (btn != null) {
                btn.setOnClickListener(OCL);
            }
        }
        
        
    }

    public void show(int viewX, int viewY) {
//        Point size = new Point();
//        mWindowManager.getDefaultDisplay().getSize(size);
//        int viewWidth = (int) (Math.min(size.x, size.y) * 0.82);
        
        if (mHandler != null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_POPUP_DISAPPER), 3000);
        }
        
        mLanguageSetting = mSharedPref.getString(mPrefKeyWorkingLang,
                mContext.getString(R.string.HW_RESOURCE_LANG_JP));
        mJapaneseSetting = mSharedPref.getString(mPrefKeyJapaneseType,
                mContext.getString(R.string.HW_LANGUAGE_JAPAN_ALL));
        

        for (String s : mInputModes) {
            btn = (ImageButton) mRoot.findViewWithTag(s);
            if (btn != null) {
                if (mLanguageSetting.equals(mContext.getString(R.string.HW_RESOURCE_LANG_KR))) {
                    if (btn.getTag().equals(mLanguageSetting)) {
                        btn.setPressed(true);
                    } else {
                        btn.setPressed(false);
                    }
                } else {
                    if (btn.getTag().equals(mJapaneseSetting)) {
                        btn.setPressed(true);
                    } else {
                        btn.setPressed(false);
                    }
                }
            }
        }
        mActiveModeSetting = mSharedPref.getString(mPrefKeyActiveMode, mActiveModeText);
        if (mActiveModeSetting.equals(mActiveModeText)) {
            ImageView imgView = (ImageView) mRoot.findViewWithTag("btnTextCheck");
            imgView.setEnabled(false);
        } else if (mActiveModeSetting.equals(mActiveModeChar)) {
            ImageView imgView = (ImageView) mRoot.findViewWithTag("btnCharCheck");
            imgView.setEnabled(false);
        }

        mWindow.setContentView(mRoot);
//        mWindow.setWidth(viewWidth);
        mWindow.showAtLocation(mAnchor, Gravity.LEFT | Gravity.BOTTOM, viewX, viewY);
    }

    OnClickListener OCL = new OnClickListener() {

        @Override
        public void onClick(View v) {
//            int id = v.getId();
            Object tag = v.getTag();
            String stringTag = null;
            if (tag instanceof String) {
                stringTag = (String) tag;
            }
            
            // set Japan Type
            if (stringTag.equals("btnChar")) {
                if (!mActiveModeSetting.equals(mActiveModeChar)) {
                    mSharedPref.edit().putString(mPrefKeyActiveMode, mActiveModeChar).commit();
                }
            } else if (stringTag.equals("btnText")) {
                if (!mActiveModeSetting.equals(mActiveModeText)) {
                    mSharedPref.edit().putString(mPrefKeyActiveMode, mActiveModeText).commit();
                }
            } else if (stringTag.equals(mContext.getString(R.string.HW_RESOURCE_LANG_KR))) {
                // if (id == R.id.btnKorChar) {
                // set Lang

                mSharedPref.edit()
                        .putString(mPrefKeyWorkingLang, mContext.getString(R.string.HW_RESOURCE_LANG_KR))
                        .commit();
            } else {
                mSharedPref.edit().putString(mPrefKeyWorkingLang, mContext.getString(R.string.HW_RESOURCE_LANG_JP))
                        .commit();
                mSharedPref.edit().putString(mPrefKeyJapaneseType, stringTag).commit();
            }
            
//            int res = 0;
//            if (id == R.id.btnAllChar) {
//                res = R.string.HW_LANGUAGE_JAPAN_ALL;
//            } else if (id == R.id.btnKanjiChar) {
//                res = R.string.HW_LANGUAGE_JAPAN_KANJI;
//            } else if (id == R.id.btnHiraChar) {
//                res = R.string.HW_LANGUAGE_JAPAN_HIRAGANA;
//            } else if (id == R.id.btnKataChar) {
//                res = R.string.HW_LANGUAGE_JAPAN_KATAKANA;
//            } else if (id == R.id.btnEngChar) {
//                res = R.string.HW_LANGUAGE_JAPAN_ENGLISH;
//            } else if (id == R.id.btnNumChar) {
//                res = R.string.HW_LANGUAGE_JAPAN_NUMBER;
//            } else if (id == R.id.btnSymbolChar) {
//                res = R.string.HW_LANGUAGE_JAPAN_SYMBOL;
//            } 
//            mSharedPref.edit().putString(mPrefKeyJapaneseType, mContext.getString(res)).commit();
            
            if (mClickListener != null) {
//                mClickListener.onClick(v.getId());
                mClickListener.onClick(v.getTag());
            }
            dismiss();
        }
    };
}
