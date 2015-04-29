package com.lge.handwritingime.manager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lge.handwritingime.HandwritingKeyboard;
import com.lge.handwritingime.R;
import com.lge.handwritingime.StrokeView;
import com.lge.handwritingime.StrokeView.OnStrokeListener;
import com.lge.handwritingime.data.Stroke;
import com.lge.handwritingime.data.StrokeChar;
import com.lge.voassist.Utils;
import com.visionobjects.im.Result;

public class CharModeManager implements IModeManager {
    private static final String TAG = "LGHWIMECharModeManager";
    private static final boolean DEBUG = HandwritingKeyboard.DEBUG;
    private static final int DELAY_CLEAR = 5000;
    private static final int MAX_CANDIDATES = 9;
    private static final int BACKGROUND_COLOR = 0xFF66CDAA;

    private HandwritingKeyboard mKeyboard;
    private StrokeManager mStrokeManager;
    private StrokeCharManager mStrokeCharManager;

    private FrameLayout mStrokeLayout;
    private FrameLayout mStrokeViewGroupLayout;
    private StrokeView mStrokeViewLeft;
    private StrokeView mStrokeViewRight;
    private StrokeView mCurrentStrokeView;

    // composing text
    private int mCandidatesStart;
    private int mCandidatesSize;
    private int mCandidatesSelection;
    private int mCurSelStart;

    private Handler mHandler;

    private OnStrokeListener mCharOnStrokeListener = new OnStrokeListener() {
        Stroke strokes;

        @Override
        public void onStrokeEvent(float x, float y) {
            strokes.add(x, y);
        }

        @Override
        public void onPreStartStrokeEvent(StrokeView v) {
            mCurrentStrokeView = (StrokeView) v;
            StrokeView otherView = (v.equals(mStrokeViewLeft)) ? mStrokeViewRight : mStrokeViewLeft;
            mHandler.removeMessages(MSG_CLEAR_STROKE_VIEW);
            if (!otherView.isEmpty()) {
                mStrokeManager.clear();
                otherView.redraw(mStrokeManager.getAll());
            }
        }

        @Override
        public void onStartStrokeEvent(float x, float y) {
            strokes = new Stroke();
            strokes.add(x, y);
        }

        @Override
        public void onEndStrokeEvent(float x, float y) {
            strokes.add(x, y);
            mStrokeManager.add(strokes);
            mKeyboard.checkDeleteButton();
            if (mStrokeManager.size() == 1) {
                StrokeChar strokeChar = new StrokeChar(mStrokeManager.getAll());
                if (mCurSelStart >= 0 && mCurSelStart == mCandidatesStart) {
                    mStrokeCharManager.add(0, strokeChar);
                    ++mCurSelStart;
                } else if (++mCandidatesSelection > 0) {
                    mStrokeCharManager.add(mCandidatesSelection, strokeChar);
                } else {
                    mStrokeCharManager.add(strokeChar);
                }
            }
            mKeyboard.submitStrokes();
        }
    };

    public void setResult(Result result) {
        // NOTE : some cases (like changing working language), strokes are not
        // changed but commit is called,
        // and in this case, stroke view shouldn't cleared for a moment.
        mHandler.removeMessages(MSG_CLEAR_STROKE_VIEW);

        if (result.getItemCount() <= 0 || mStrokeCharManager.size() == 0)
            return;

        int index = (mCandidatesSelection >= 0) ? mCandidatesSelection : mStrokeCharManager.size() - 1;
        // mStrokeCharManager.addResult(index, result);

        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Float> scores = new ArrayList<Float>();

        for (int i = 0; i < result.getItemCandidateCount(0) && i < MAX_CANDIDATES ; i++) {
            String label = result.getItemCandidateLabel(0, i);
            Utils.CHAR_TYPE charType = Utils.getCharType(label, 0);
            if (charType == Utils.CHAR_TYPE.OTHER)
                continue;
            labels.add(result.getItemCandidateLabel(0, i));
            scores.add(result.getItemCandidateScore(0, i));
        }

        StrokeChar strokeChar = mStrokeCharManager.get(index);
        if (strokeChar != null) {
            strokeChar.setResult(scores, labels);
        }

        mHandler.sendEmptyMessage(MSG_COMPOSING_TEXT);
        mHandler.sendEmptyMessageDelayed(MSG_CLEAR_STROKE_VIEW, DELAY_CLEAR);
        
    }

    // @Override
    // public void onPostRecoginitionEnd(Result result) {
    // Log.e(TAG, "post rec");
    //
    // if (mComposingCharIndex > 0) {
    // mStrokeCharManager.addResult(mComposingCharIndex, result);
    // } else {
    // mStrokeCharManager.addResult(result);
    // }
    //
    // mHandler.sendEmptyMessage(MSG_COMPOSING_TEXT);
    // mHandler.sendEmptyMessageDelayed(MSG_CLEAR_STROKE_VIEW, CLEAR_DELAY);
    // }

    public CharModeManager(HandwritingKeyboard handwritingKeyboard) {
        mKeyboard = handwritingKeyboard;
        mHandler = new CharModeHandler(this);
        mStrokeManager = StrokeManager.getInstance();
        mStrokeCharManager = StrokeCharManager.getInstance();
        mCandidatesSelection = -1;
        mCandidatesStart = -1;
        mCurSelStart = -1;
        mCandidatesSize = 0;
    }
    
    public void composingText(String strData, boolean cursor) {
        if (DEBUG) Log.i(TAG, "[composingText] strData=" + strData);
        if (DEBUG) Log.d(TAG, "  mCandidatesStart=" + mCandidatesStart +
                " mCandidatesSelection=" + mCandidatesSelection +
                " mCurSelStart=" + mCurSelStart);
        if (strData == null || strData.length() <= 0)
            return;

        SpannableStringBuilder sbb = new SpannableStringBuilder(strData);
        sbb.setSpan(new UnderlineSpan(), 0, strData.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // even though mCandidatesStart = -1, means onUpdateSelection is not
        // worked, background of current item should be highlighted.
        if (mCandidatesStart == -1 || mCandidatesStart != mCurSelStart) {
            if (mCandidatesSelection >= 0 && mCandidatesSelection < strData.length()) {
                sbb.setSpan(new BackgroundColorSpan(BACKGROUND_COLOR), mCandidatesSelection, mCandidatesSelection + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        // TODO : problem
        // 1. mCanditatesStart are zero in both cursor is on 0, and 1
        // 2. after setComposingText, cursor is located to end of candidates,
        // so then location is changed. it makes customized onUpdateSelection to
        // think
        // that cursor is moved by user, so strokes are cleared.

        // To solve 2, begin/endBatchEdit is used.
        // it may solve the flickering issue during "����" button on iWnnIME.

        InputConnection ic = mKeyboard.getCurrentInputConnection();
        if (ic != null) {
            boolean isBatching = ic.beginBatchEdit();
            ic.setComposingText(sbb, 1);
            if (cursor && mCurSelStart != mCandidatesStart + mCandidatesSize) {
                if (mCandidatesStart >= 0) {
                    if (mCandidatesStart != mCurSelStart) {
                        ic.setSelection(mCandidatesStart + mCandidatesSelection + 1, mCandidatesStart
                                + mCandidatesSelection + 1);
                        mCurSelStart = mCandidatesStart + mCandidatesSelection + 1;
                    } else {
                        ic.setSelection(mCandidatesStart + mCandidatesSelection, mCandidatesStart
                                + mCandidatesSelection);
                        mCurSelStart = mCandidatesStart + mCandidatesSelection;
                    }
                } else { // in case onUpdateSelection is not called ever
                }
            }

            if (isBatching) {
                ic.endBatchEdit();
            }
        }
    }

    public void composingText(String strData) {
        composingText(strData, true);
    }

    private void clearComposingChars() {
        if (DEBUG) Log.i(TAG, "[clearComposingChars]");
        mStrokeCharManager.clear();
        mCandidatesSelection = -1;
        mCandidatesStart = -1;
        mCurSelStart = -1;
        // NOTE : shouldn't be update Views when
        // IME is not shown. it makes IME noti showing.
        // clearStrokeViewData();
    }

    public void finishComposingText() {
        if (DEBUG) Log.i(TAG, "[finishComposingText] mCurSelStart=" + mCurSelStart +
                " mCandidatesStart" + mCandidatesStart + " mCandidatesSize" + mCandidatesSize);
        String candidateData = mStrokeCharManager.getText();
        InputConnection ic = mKeyboard.getCurrentInputConnection();
        if (ic != null) {
            boolean isBatching = ic.beginBatchEdit();
            ic.finishComposingText();
            if (isBatching) {
                ic.endBatchEdit();
            }
        }
        clearComposingChars();
        
        if (mKeyboard.isCandidatesViewVisible()) {
            mKeyboard.setCandidatesViewVisibility(false);
        }
    }
    
    @Override
    public void onCreateStrokeLayout(FrameLayout strokeLayout) {
        if (DEBUG) Log.i(TAG, "[onCreateStrokeLayout]");
        mStrokeLayout = strokeLayout;
        mStrokeViewGroupLayout = (FrameLayout) mStrokeLayout.findViewWithTag("strokeViewGroupLayout");
        
        
//      <com.lge.handwritingime.StrokeView
//      android:id="@+id/strokeViewCharLeft"
//      android:tag="strokeViewCharLeft"
//      android:layout_width="@dimen/stroke_view_height"
//      android:layout_height="@dimen/stroke_view_height"
//      android:layout_gravity="center"
//      android:layout_marginRight="@dimen/stroke_view_char_margin"
//      android:visibility="gone" />
//
//  <com.lge.handwritingime.StrokeView
//      android:id="@+id/strokeViewCharRight"
//      android:tag="strokeViewCharRight"
//      android:layout_width="@dimen/stroke_view_height"
//      android:layout_height="@dimen/stroke_view_height"
//      android:layout_gravity="center"
//      android:layout_marginLeft="@dimen/stroke_view_char_margin"
//      android:visibility="gone" />

//      mStrokeViewLeft = (StrokeView) mStrokeViewGroupLayout.findViewById(R.id.strokeViewCharLeft);
//      mStrokeViewRight = (StrokeView) mStrokeViewGroupLayout.findViewById(R.id.strokeViewCharRight);
        
        View v = mStrokeViewGroupLayout.findViewWithTag("strokeViewCharLeft");
        if (v instanceof StrokeView) {
            mStrokeViewLeft = (StrokeView) v;
        } else {
            mStrokeViewLeft = new StrokeView(HandwritingKeyboard.getBaseContext());
            mStrokeViewLeft.setLayoutParams(v.getLayoutParams());
            mStrokeViewLeft.setTag("strokeViewCharLeft");
            
            LinearLayout layout = (LinearLayout) mStrokeLayout.findViewWithTag("strokeViewCharLeftLayout");
            layout.removeView(v);
            layout.addView(mStrokeViewLeft);
        }
        
        v = mStrokeViewGroupLayout.findViewWithTag("strokeViewCharRight");        
        if (v instanceof StrokeView) {
            mStrokeViewRight = (StrokeView) v;
        } else {
            mStrokeViewRight = new StrokeView(HandwritingKeyboard.getBaseContext());
            mStrokeViewRight.setLayoutParams(v.getLayoutParams());
            mStrokeViewRight.setTag("strokeViewCharRight");
            
            LinearLayout layout = (LinearLayout) mStrokeLayout.findViewWithTag("strokeViewCharRightLayout");
            layout.removeView(v);
            layout.addView(mStrokeViewRight);
        }
        
        mStrokeViewLeft.setBackgroundColor(mKeyboard.mThemeManager.getColor(R.color.stroke_view_bg_color));
        mStrokeViewLeft.setRectColor(0x000000);
        mStrokeViewRight.setBackgroundColor(mKeyboard.mThemeManager.getColor(R.color.stroke_view_bg_color));
        mStrokeViewRight.setRectColor(0x000000);
        
        ViewGroup.LayoutParams lp = mStrokeViewGroupLayout.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mStrokeViewGroupLayout.setLayoutParams(lp);
        mStrokeViewLeft.setOnStrokeListener(mCharOnStrokeListener);
        mStrokeViewRight.setOnStrokeListener(mCharOnStrokeListener);
        mStrokeViewLeft.setVisibility(View.VISIBLE);
        mStrokeViewRight.setVisibility(View.VISIBLE);
    }
    
    public void dPadBackward() {
        Log.e(TAG, "mCurSelStart=" + mCurSelStart + " mCandidatesStart" + mCandidatesStart);
        if (mKeyboard.isCandidatesViewVisible()) {
            mCandidatesSelection = Math.max(mCandidatesSelection-1, 0);
//            String candidateData = mStrokeCharManager.getText();
//            composingText(candidateData, false);
            mHandler.sendEmptyMessage(MSG_COMPOSING_TEXT_NO_CUR_MOVE);
            updateCadidates();
            
            mStrokeManager.clear();
            mCurrentStrokeView.redraw(mStrokeManager.getAll());
            mKeyboard.checkDeleteButton();
        } else { // if (mCurSelStart > mCandidatesStart) {
            mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
        }
    }

    public void dPadForward() {
        Log.e(TAG, "mCurSelStart=" + mCurSelStart + " mCandidatesStart" + mCandidatesStart);
        if (mKeyboard.isCandidatesViewVisible()) {
            mCandidatesSelection = Math.min(mCandidatesSelection+1, mStrokeCharManager.size()-1);
//            String candidateData = mStrokeCharManager.getText();
//            composingText(candidateData, false);
            mHandler.sendEmptyMessage(MSG_COMPOSING_TEXT_NO_CUR_MOVE);
            updateCadidates();
            
            mStrokeManager.clear();
            mCurrentStrokeView.redraw(mStrokeManager.getAll());
            mKeyboard.checkDeleteButton();
        } else { // if (mCurSelStart < mCandidatesStart + mCandidatesSize) {
            mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
        }
    }

    private void deleteComposingChar() {
        mHandler.removeMessages(MSG_CLEAR_STROKE_VIEW);
        
        
        Log.e(TAG, " mStrokeManager.isEmpty()=" + mStrokeManager.isEmpty() +
                " mCandidatesSelection=" + mCandidatesSelection +
                " mStrokeCharManager.size()=" + mStrokeCharManager.size() 
                );
        if (mStrokeManager.isEmpty()) {
            if (mCandidatesSelection >= 0 && mCandidatesSelection < mStrokeCharManager.size()) {
                mStrokeCharManager.remove(mCandidatesSelection);
                mCandidatesSelection = Math.max(mCandidatesSelection-1, 0);
            }
            if (!mStrokeCharManager.isEmpty()) {
                composingText(mStrokeCharManager.getText(), false);
                // mKeyboard.mSuggestions = getSuggestions();
                mKeyboard.updateCandidateView();
                // TODO : do it in HandwritingKeyboard!
                mKeyboard.setCandidatesSelectedIndex(mStrokeCharManager.get(mStrokeCharManager.size() - 1)
                        .getSelectedIndex());
            } else {
                InputConnection ic = mKeyboard.getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(mStrokeCharManager.getText(), 1);
                }

                clearComposingChars();
                mKeyboard.clearAll();
            }
        } else {
            mKeyboard.submitStrokes();
        }
        mKeyboard.checkDeleteButton();
    }

    @Override
    public void deleteStroke() {
        if (!mStrokeManager.isEmpty()) {
            mStrokeManager.remove(mStrokeManager.size() - 1);
        }
        if (mCurrentStrokeView != null) {
            mCurrentStrokeView.redraw(mStrokeManager.getAll());
        }
        deleteComposingChar();
    }

    @Override
    public void deleteOne() {
        if (!mStrokeManager.isEmpty()) {
            mStrokeManager.clear();
        }
        if (mCurrentStrokeView != null) {
            mCurrentStrokeView.redraw(mStrokeManager.getAll());
        }
        deleteComposingChar();
    }

    // @Override
    // public void deleteOne(int index) {
    // if(!mStrokeDataManager.isEmpty()) {
    // mStrokeDataManager.clear();
    // }
    // if (mCurrentStrokeView != null) {
    // mCurrentStrokeView.redraw(mStrokeDataManager.getAll());
    // }
    // deleteComposingChar();
    // }

    @Override
    public void deleteText() {
        if (!mStrokeCharManager.isEmpty()) {
            deleteComposingChar();
        } else {
            InputConnection ic = mKeyboard.getCurrentInputConnection();
            if (ic != null) {
            mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
//            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }
        }
    }

    @Override
    public ArrayList<String> getSuggestions() {
        if (mStrokeCharManager.isEmpty() || mCandidatesSelection < 0 || mCandidatesSelection > mStrokeCharManager.size()) {
            return null;
        }
        return mStrokeCharManager.get(mCandidatesSelection).getLabels();
    }

    @Override
    public void pickSuggestionManually(int index) {
        StrokeChar sc = mStrokeCharManager.get(mCandidatesSelection);
        if (sc != null && index >= 0) {
            sc.setSelectedIndex(index);
        }

        String candidateData = mStrokeCharManager.getText();
        composingText(candidateData);
    }

    @Override
    public void pickSuggestionManually(String string) {
        StrokeChar sc = mStrokeCharManager.get(mCandidatesSelection);
        if (sc != null) {
            sc.setSelectedLabel(string);
        }

        String candidateData = mStrokeCharManager.getText();
        composingText(candidateData);
    }
    
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart,
            int candidatesEnd) {
        
        // TODO : by uncomment the below code, move cursor is not able during handwriting.
        if (true) {
            mCurSelStart = newSelStart;
            mCandidatesStart = candidatesStart;
            
            if (mKeyboard.isCandidatesViewVisible()) {
                // NOTE : Double-tap or long-press, selection is more than 1 letter
                if (newSelStart != newSelEnd) {
                    if (DEBUG) Log.i(TAG, "selection enabled");
                    mKeyboard.clearAll();
                    return;
                } else if (candidatesStart < 0) { // NOTE : App changed cursor forced (such as sp-mode mail)
                    if (DEBUG) Log.i(TAG, "cursor changed unexpectedly");
                    mStrokeCharManager.clear();
                    mKeyboard.clearAll();
                    return;
                }
                
                if (newSelStart != candidatesEnd) {
                    InputConnection ic = mKeyboard.getCurrentInputConnection();
                    
                    if(ic!=null && candidatesEnd >= 0) {
                        ic.setSelection(candidatesEnd, candidatesEnd);
                    }
                }
            }
            return;
        }
        
        // long-press or out-of-candidates
        // composing text and composing chars are differ.
        if (DEBUG) Log.d(TAG, " mStrokeCharManager.size()=" + mStrokeCharManager.size() + " candidatesEnd-candidatesStart="
                + (candidatesEnd - candidatesStart) + " mCandidatesSize=" + mCandidatesSize);
        if (newSelStart != newSelEnd || newSelStart < candidatesStart || newSelStart > candidatesEnd) {
            if (DEBUG) Log.w(TAG, "newSelStart != newSelEnd=" + (newSelStart != newSelEnd) +
                    " newSelStart < candidatesStart=" + (newSelStart < candidatesStart) +
                    " newSelStart > candidatesEnd=" + (newSelStart > candidatesEnd));
                    
            // clearAll() contains finishoComposingText().
            //finishComposingText();
            mKeyboard.clearAll();
            return;
        }
        if (mStrokeCharManager.size() != candidatesEnd - candidatesStart) {
            Log.w(TAG, "strokeCharSize and candidatesSize is differ");
            // in this case, i guess recognizing is not finished until cursor changed.
            //finishComposingText();
//            mKeyboard.clearAll();
//            return;
        }

        if (mCandidatesSize == candidatesEnd - candidatesStart) {
            if (DEBUG) Log.d(TAG, "mCandidatesSize=" + mCandidatesSize + " thus, clear StrokeView");
            mStrokeManager.clear();
            mCurrentStrokeView.redraw(mStrokeManager.getAll());
        }        
        mCandidatesSize = candidatesEnd - candidatesStart;
        mCandidatesStart = candidatesStart;
        mCandidatesSelection = Math.max(newSelStart - candidatesStart - 1, 0);
        mCurSelStart = newSelStart;
        
        updateCadidates();
//        mKeyboard.mHandler.sendMessageDelayed(mKeyboard.mHandler.obtainMessage(HandwritingKeyboard.MSG_UPDATE_CANDIDATES,
//                selected, 0), 200);
        if (DEBUG) Log.i(TAG, "[onUpdateSelection()] mCandidatesSelection=" + mCandidatesSelection);

        mHandler.sendEmptyMessage(MSG_COMPOSING_TEXT);
        mKeyboard.checkDeleteButton();
    }
    
    private void updateCadidates() {
     // TODO: NPE here
        int selected = 0;
        if (mCandidatesSelection < mStrokeCharManager.size()) {
            selected = mStrokeCharManager.get(mCandidatesSelection).getSelectedIndex();
        }
        // TODO DO DO DO
        mKeyboard.mHandler.sendMessage(mKeyboard.mHandler.obtainMessage(HandwritingKeyboard.MSG_UPDATE_CANDIDATES,
                selected, 0));
    }

    @Deprecated
    @Override
    public boolean dismissCharButtonPopups() {
        // TODO Auto-generated method stub
        return false;
    }


    private void setPenType(String penColor, String penType) {
        mStrokeViewLeft.setPenType(penColor, penType);
        mStrokeViewRight.setPenType(penColor, penType);
    }
        
    @Override
    public void setValuesByPrefs(SharedPreferences sharedPref) {
        String penColor = sharedPref.getString(mKeyboard.getString(R.string.HW_PREF_KEY_PEN_COLOR),
                mKeyboard.getString(R.string.HW_PEN_COLOR_DEFAULT));
        String penType = sharedPref.getString(mKeyboard.getString(R.string.HW_PREF_KEY_PEN_TYPE),
                mKeyboard.getString(R.string.HW_PEN_TYPE_BALLPEN));
        
        if (penColor != null && penType != null) {
            setPenType(penColor, penType);
        }
    }

    @Override
    public void clear() {
        mHandler.removeMessages(MSG_CLEAR_STROKE_VIEW);
        finishComposingText();
        mStrokeManager.clear();
        mStrokeCharManager.clear();
        mStrokeViewLeft.redraw(mStrokeManager.getAll());
        mStrokeViewRight.redraw(mStrokeManager.getAll());
        mCandidatesSelection = -1;
        mCandidatesStart = -1;
        mCurSelStart = -1;
        mCandidatesSize = 0;
    }

    @Override
    public void destroy() {
        mHandler.removeMessages(MSG_CLEAR_STROKE_VIEW);
        if (mStrokeViewLeft != null) {
            mStrokeViewLeft.setOnStrokeListener(null);
            mStrokeViewLeft.setVisibility(View.GONE);
            mStrokeViewLeft = null;
        }

        if (mStrokeViewRight != null) {
            mStrokeViewRight.setOnStrokeListener(null);
            mStrokeViewRight.setVisibility(View.GONE);
            mStrokeViewRight = null;
        }
    }

    private final static int MSG_CLEAR_STROKE_VIEW = 11;
    private final static int MSG_COMPOSING_TEXT = 12;
    private final static int MSG_COMPOSING_TEXT_NO_CUR_MOVE = 13;

    private static class CharModeHandler extends Handler {
        private final WeakReference<CharModeManager> mRef;

        public CharModeHandler(CharModeManager manager) {
            mRef = new WeakReference<CharModeManager>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            CharModeManager manager = mRef.get();
            if (DEBUG) Log.i(TAG, "[handleMessage()] " + msg.what);
            String candidateData = null;
            switch (msg.what) {
            case MSG_CLEAR_STROKE_VIEW:
                manager.mStrokeManager.clear();
                manager.mCurrentStrokeView.redraw(manager.mStrokeManager.getAll());
                manager.mKeyboard.checkDeleteButton();
                break;
            case MSG_COMPOSING_TEXT:
                candidateData = manager.mStrokeCharManager.getText();
                manager.composingText(candidateData);
                break;
            case MSG_COMPOSING_TEXT_NO_CUR_MOVE:
                candidateData = manager.mStrokeCharManager.getText();
                manager.composingText(candidateData, false);
                break;
            }
        }
    }
}
