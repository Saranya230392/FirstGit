package com.lge.handwritingime.popup;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.lge.handwritingime.R;
import com.lge.voassist.Utils;

public class SelectCandidateDialog extends AlertDialog {
    private final static int BUTTON_ID = 0x8100;
    public interface OnClickCandidateListener {
        public void onClickCandidte(String selectChar);
    }
    
    private SelectCandidateDialog(Context context) {
        super(context);
    }
    
    public static class Builder extends AlertDialog.Builder {
        private ArrayList<String> mCandidates;
        private int mSelectedIndex;
        OnClickCandidateListener mListener;
        View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v instanceof Button && v != null) {
                    String selectedChar = ((Button) v).getText().toString();
                    if (mListener != null) {
                        mListener.onClickCandidte(selectedChar);
                    }
                }
            }
        };    

        public Builder(Context context, int theme) {
            super(context, theme);
        }

        public Builder(Context context) {
            super(context);
        }
        
        public Builder setCandidates(ArrayList<String> candidates) {
            mCandidates = candidates;
            return this;
        }
        
        public Builder setSelectedIndex(int selectedIndex) {
            mSelectedIndex = selectedIndex;
            return this;
        }
        
        public Builder setOnClickCandidateListener(OnClickCandidateListener l) {
            mListener = l;
            return this;
        }
        
        private View createCandidatesView(ArrayList<String> candidates, int selectedIndex) {
            Context context = getContext();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String prefKeyJapaneseType = context.getString(R.string.HW_PREF_KEY_LANGUAGE_JAPAN_TYPE);
            String languageJapanType = sharedPref.getString(prefKeyJapaneseType,
                    context.getString(R.string.HW_LANGUAGE_JAPAN_ALL));
            String prefKeyWorkingLanguage = context.getString(R.string.HW_PREF_KEY_WORKING_LANGUAGE);
            String languageType = sharedPref.getString(prefKeyWorkingLanguage,
                    context.getString(R.string.HW_RESOURCE_LANG_JP));
            
            TableLayout tableLayout = new TableLayout(context);
            TableRow[] tableRow = new TableRow[] {new TableRow(context), new TableRow(context)};

            tableLayout.addView(tableRow[0], new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            tableLayout.addView(tableRow[1], new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            tableLayout.setStretchAllColumns(true);
            tableLayout.setShrinkAllColumns(true);
            
            for (int i=0; i<candidates.size() && i < 10; i++) {
                int row = i/5;
//                int column = i%5;
                
                Button btn = new Button(context);
                btn.setText(candidates.get(i));
                setBackground(btn, languageType, languageJapanType, candidates.get(i));
                
                if (selectedIndex == i) {
                    btn.setPressed(true);
                } else {
                    btn.setPressed(false);
                }
                
                btn.setId(BUTTON_ID + i);
                btn.setSingleLine(true);
                btn.setEllipsize(TruncateAt.END);
                btn.setOnClickListener(mClickListener);
                tableRow[row].addView(btn, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            }
            
            return tableLayout;
        }
        
        private void setBackground(Button btn, String languageType, String languageJapanType, String s) {
            Context context = getContext();
            
            if (languageType.equals(context.getString(R.string.HW_RESOURCE_LANG_KR))) {
                btn.setBackgroundResource(determineBackground(s));
            } else if (languageJapanType.equals(context.getString(R.string.HW_LANGUAGE_JAPAN_KANJI))) {
                btn.setBackgroundResource(R.drawable.button_state_char_candidate_kanji);
            } else if (languageJapanType.equals(context.getString(R.string.HW_LANGUAGE_JAPAN_HIRAGANA))) {
                btn.setBackgroundResource(R.drawable.button_state_char_candidate_hiragana);
            } else if (languageJapanType.equals(context.getString(R.string.HW_LANGUAGE_JAPAN_KATAKANA))) {
                btn.setBackgroundResource(R.drawable.button_state_char_candidate_katakana);
            } else if (languageJapanType.equals(context.getString(R.string.HW_LANGUAGE_JAPAN_ENGLISH))) {
                btn.setBackgroundResource(R.drawable.button_state_char_candidate_alpha);
            } else if (languageJapanType.equals(context.getString(R.string.HW_LANGUAGE_JAPAN_NUMBER))) {
                btn.setBackgroundResource(R.drawable.button_state_char_candidate_number);
            } else if (languageJapanType.equals(context.getString(R.string.HW_LANGUAGE_JAPAN_SYMBOL))) {
//                btn.setBackgroundResource(R.drawable.button_state_char_candidate_etc);
                btn.setBackgroundResource(determineBackground(s));
            } else {
                btn.setBackgroundResource(determineBackground(s));
            }
        }
        
        private int determineBackground(String s) {
            switch (Utils.getCharType(s, 0)) {
            case KANJI:
                return R.drawable.button_state_char_candidate_kanji;
            case HIRAGANA:
                return R.drawable.button_state_char_candidate_hiragana;
            case KATAKANA:
                return R.drawable.button_state_char_candidate_katakana;
            case NUMBER:
                return R.drawable.button_state_char_candidate_number;
            case ENGLISH:
                return R.drawable.button_state_char_candidate_alpha;
            case SYMBOL:
                return R.drawable.button_state_char_candidate_etc;
            case KOREAN:
                return R.drawable.button_state_char_candidate_han;
            default:
                return R.drawable.button_state_char_candidate_etc;
            }
        }
        
        
        @Override
        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            super.setNegativeButton(text, listener);
            return this;
        }

        @Override
        public Builder setNegativeButton(int textId, OnClickListener listener) {
            super.setNegativeButton(textId, listener);
            return this;
        }

        @Override
        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            super.setPositiveButton(text, listener);
            return this;
        }

        @Override
        public Builder setPositiveButton(int textId, OnClickListener listener) {
            super.setPositiveButton(textId, listener);
            return this;
        }

        @Override
        public AlertDialog create() {
            if (mCandidates != null) {
                setView(createCandidatesView(mCandidates, mSelectedIndex));
            }
            return super.create();
        }
    }
}
