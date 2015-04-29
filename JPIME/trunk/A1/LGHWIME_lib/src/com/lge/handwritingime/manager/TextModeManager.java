package com.lge.handwritingime.manager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.lge.handwritingime.HandwritingKeyboard;
import com.lge.handwritingime.R;
import com.lge.handwritingime.StrokeView;
import com.lge.handwritingime.StrokeView.OnStrokeListener;
import com.lge.handwritingime.data.Stroke;
import com.lge.handwritingime.data.StrokeChar;
import com.lge.handwritingime.popup.SelectCandidateDialog;
import com.lge.handwritingime.popup.SelectCandidateDialog.OnClickCandidateListener;
import com.lge.voassist.Utils;
import com.visionobjects.im.Result;

public class TextModeManager implements IModeManager, OnClickListener, OnTouchListener {
    private static final String TAG = "LGHWIMETextModeManager";
    private static final boolean DEBUG = HandwritingKeyboard.DEBUG;
    private static final int DELAY_CHAR_BUTTON = 500;

    private HandwritingKeyboard mKeyboard;
    private StrokeManager mStrokeManager;
    private StrokeCharManager mStrokeCharManager;
    private ScreenManager mScreenManager;

    public FrameLayout mStrokeLayout;
    public FrameLayout mStrokeViewGroupLayout;
    private StrokeView mStrokeView;
    
    private boolean mIsAutoScrollMode;
    private int mAutoDelay;
    private float mNextScreenWidth;

    private ConcurrentHashMap<View, StrokeChar> mCharButtons;
//    public SelectCandidatePopup mSelectCandidatePopup;
    public AlertDialog mSelectCandidatePopup;

    private Handler mHandler;

    private OnStrokeListener mTextOnStrokeListener = new OnStrokeListener() {
        Stroke stroke;

        @Override
        public void onStrokeEvent(float x, float y) {
            if (stroke != null) {
                stroke.add(x, y);
            }
        }

        @Override
        public void onPreStartStrokeEvent(StrokeView v) {
            mHandler.removeMessages(MSG_SCROLL_RIGHT_SCREEN);
            mKeyboard.stopCandidates();
            removeCharButtonAll();
            dismissCharButtonPopups();
        }

        @Override
        public void onStartStrokeEvent(float x, float y) {
            stroke = new Stroke();
            stroke.add(x, y);
        }

        @Override
        public void onEndStrokeEvent(float x, float y) {
            if (stroke != null) {
                stroke.add(x, y);
                if (DEBUG) Log.d(TAG, "mStrokeManager size=" + mStrokeManager.size());
                if( mStrokeManager.size() < 175) {
                    mStrokeManager.add(stroke);
                    mKeyboard.submitStrokes();
                }
            }
        }
    };

    public TextModeManager(HandwritingKeyboard handwritingKeyboard) {
        mKeyboard = handwritingKeyboard;
        mHandler = new TextModeHandler(this);
        mScreenManager = new ScreenManager(handwritingKeyboard, this);
        mStrokeManager = StrokeManager.getInstance();
        mStrokeCharManager = StrokeCharManager.getInstance();
        mCharButtons = new ConcurrentHashMap<View, StrokeChar>();
        
        mIsAutoScrollMode = true;
        mAutoDelay = 5;
        mNextScreenWidth = Float.parseFloat(mKeyboard.getString(R.string.HW_AREA_WIDTH_NORMAL));
    }

    @Override
    public void onCreateStrokeLayout(FrameLayout strokeLayout) {
        mStrokeLayout = strokeLayout;
        mStrokeViewGroupLayout = (FrameLayout) mStrokeLayout.findViewWithTag("strokeViewGroupLayout");
        
        View v = mStrokeViewGroupLayout.findViewWithTag("strokeViewText");
        if (v instanceof StrokeView) {
            mStrokeView = (StrokeView) v;
        } else {
            mStrokeViewGroupLayout.removeView(v);
            mStrokeView = new StrokeView(HandwritingKeyboard.getBaseContext());
            mStrokeView.setLayoutParams(v.getLayoutParams());
            mStrokeViewGroupLayout.addView(mStrokeView);
            mStrokeView.setTag("strokeViewText");
        }
        
        mStrokeView.setBackgroundColor(mKeyboard.mThemeManager.getColor(R.color.stroke_view_bg_color));
        mStrokeView.setRectColor(mKeyboard.mThemeManager.getColor(R.color.stroke_view_rect_color));        
        
        mStrokeView.setOnStrokeListener(mTextOnStrokeListener);
        mStrokeView.setVisibility(View.VISIBLE);

        mStrokeView.redraw(mStrokeManager.getAll());
        updateCharButton();

        recalcWindowSize();
        mScreenManager.recalcScroll(true);
        mScreenManager.updateScreenArrow();

        ImageButton beforeScreen = (ImageButton) mStrokeLayout.findViewWithTag("beforeScreen");
        ImageButton afterScreen = (ImageButton) mStrokeLayout.findViewWithTag("afterScreen");
        beforeScreen.setOnClickListener(this);
        beforeScreen.setOnTouchListener(this);
        afterScreen.setOnClickListener(this);
        afterScreen.setOnTouchListener(this);
    }

    private void setAutoScrollWidth(float nextScreenWidth) {
        mScreenManager.setNextScreenWidthDip(nextScreenWidth);
        // TODO : why does it need parameters?
        mScreenManager.recaleNextScreenRectF(mScreenManager.getWidthScreen(), mScreenManager.getHeightScreen());
        mScreenManager.recalcScroll(true);
    }

    public void removeCharButtonAll() {
        mHandler.removeMessages(MSG_UPDATE_CHAR_BUTTON);
        for (View btn : mCharButtons.keySet()) {
            removeCharButton(btn);
        }
    }

    @SuppressWarnings("unchecked")
    public void setResult(Result result) {
        ArrayList<?>[] prevLabels = new ArrayList[mStrokeCharManager.size()];
        int[] prevIndex = new int[mStrokeCharManager.size()];
        for (int i=0;i<mStrokeCharManager.size();i++) {
            prevLabels[i] = (ArrayList<String>) mStrokeCharManager.get(i).getLabels().clone();
            prevIndex[i] = mStrokeCharManager.get(i).getSelectedIndex();
        }

        mStrokeCharManager.clear();
        for (int i = 0; i < result.getItemCount(); i++) {
            ArrayList<String> labels = new ArrayList<String>();
            ArrayList<Float> scores = new ArrayList<Float>();
            int start = 0;
            int end = 0;

            for (int j = 0; j < result.getItemCandidateCount(i); j++) {
                String label = result.getItemCandidateLabel(i, j);
                Float score = result.getItemCandidateScore(i, j);
                Utils.CHAR_TYPE charType = Utils.getCharType(label, 0);
                if (charType == Utils.CHAR_TYPE.OTHER)
                    continue;
                labels.add(label);
                scores.add(score);
            }
            
            try {
                start = result.getItemInkElementFirstStroke(i, 0);
                end = result.getItemInkElementLastStroke(i, 0) + 1;
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "i=" + i + " itemCount=" + result.getItemCount(), e);
                continue;
            }
            
            if (mStrokeManager.size() < end) {
                Log.w(TAG, "stroke list size is smaller than end index");
                // mUpdated = false;
                return;
            } else if (labels.isEmpty()) {
                // NOTE : this behaviour makes StrokeChars get different size
                // and index from Result.
                if (DEBUG) Log.d(TAG, i + ": no labels");
                continue;
            }
            
            List<Stroke> strokes = mStrokeManager.getAll().subList(start, end);
            StrokeChar strokeChar = new StrokeChar(strokes, scores, labels);
            
            // NOTE : keep selection if labels are same as before.
            if (i < prevLabels.length) {
                try {
                    ArrayList<?> prev = prevLabels[i];
//                    if (DEBUG) Log.d(TAG, "labels=" + labels.toString() + "  prev=" + prev);
                    if (labels.equals(prev)) {
                        strokeChar.setSelectedIndex(prevIndex[i]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (DEBUG) Log.d(TAG, "label size=" + labels.size() + " prev label size=" + prevLabels[i].size(), e);
                }
            }
            if (DEBUG) Log.d(TAG, "labels=" + strokeChar.getLabels().toString());
            mStrokeCharManager.add(strokeChar);
        }
        if (DEBUG) Log.d(TAG, "input text=" + mStrokeCharManager.getText()); 

        // TODO : when RecognitionEnd?? change the location.
        if (!result.isIntermediate())
            mHandler.sendEmptyMessage(MSG_GO_SCREEN);
        // mHandler.sendEmptyMessageDelayed(MSG_UPDATE_CHAR_BUTTONS, 100);
        if (DEBUG) Log.d(TAG, "mIsAutoScrollMode=" + mIsAutoScrollMode);
        if (mIsAutoScrollMode) {
            mHandler.sendEmptyMessageDelayed(MSG_SCROLL_RIGHT_SCREEN, mAutoDelay * 100);
        }
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_CHAR_BUTTON, DELAY_CHAR_BUTTON);
        dismissCharButtonPopups();
    }

    private void postDeleteInner() {
        mKeyboard.stopCandidates();
        mStrokeCharManager.clear();
        removeCharButtonAll();

        mScreenManager.goCurrentScreen(true);
        mStrokeView.redraw(mStrokeManager.getAll());

        if (mStrokeManager.isEmpty()) {
            mKeyboard.updateCandidateView();
            mScreenManager.clear();
        } else {
            mKeyboard.submitStrokes();
        }

        mScreenManager.updateScreenArrow();
        mStrokeView.setLastScreen(mScreenManager.isLastScreen());
    }

    @Override
    public void dPadBackward() {
        mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
//        InputConnection ic = mKeyboard.getCurrentInputConnection();
//        if (ic != null) {
//          ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
//        }
    }

    @Override
    public void dPadForward() {
        mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
//        InputConnection ic = mKeyboard.getCurrentInputConnection();
//        if (ic != null) {
//            mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
//          ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
//        }
    }

    @Override
    public void deleteStroke() {
        if (!mStrokeManager.isEmpty()) {
            Stroke deletedStroke = mStrokeManager.remove(mStrokeManager.size() - 1);
            for (int i = 0; i < mStrokeCharManager.size(); i++) {
                StrokeChar sc = mStrokeCharManager.get(i);
                if (sc.getStrokes().contains(deletedStroke)) {
                    mStrokeCharManager.removeChar(sc);
                    i--;
                }
            }
        }
        postDeleteInner();
    }

    @Override
    public void deleteOne() {
        deleteOne(mStrokeCharManager.size() - 1);
    }

    private void deleteOne(int index) {
        if (!mStrokeManager.isEmpty()) {
            StrokeChar deletedChar = mStrokeCharManager.remove(index);
            mStrokeManager.removeAll(deletedChar.getStrokes());
        }
        postDeleteInner();
    }

    @Override
    public void deleteText() {
        InputConnection ic = mKeyboard.getCurrentInputConnection();
        if (ic != null) {
        mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
//        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        }
    }

    private ImageButton createCharButton() {
        ImageButton btn = new ImageButton(HandwritingKeyboard.getBaseContext());
        btn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setPadding(0, 0, 0, 0);
        Drawable drawable = mKeyboard.mThemeManager.getDrawable(R.drawable.button_state_char_candidate);
        btn.setImageDrawable(drawable);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setHapticFeedbackEnabled(false);
        btn.setSoundEffectsEnabled(false);
        btn.setOnTouchListener(this);
        // mStrokeViewGroupLayout.addView(btn);
        
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mCharButtons == null) {
                    return;
                }
                
                StrokeChar sc = mCharButtons.get(v);
                ArrayList<String> candidates = null;
                int selectedIndex = -1;
                if (sc != null) {
                    candidates = sc.getLabels();
                    selectedIndex = sc.getSelectedIndex();
                }
                if (candidates == null || selectedIndex == -1) {
                    return;
                }                
                
                OnClickCandidateListener onClickCandidateListener = new OnClickCandidateListener() {
                    @Override
                    public void onClickCandidte(String selectChar) {
                        if (mCharButtons != null) {
                            StrokeChar sc = mCharButtons.get(v);
                            if (sc != null) {
                                try {
                                    sc.setSelectedLabel(selectChar);
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    if (DEBUG) e.printStackTrace();
                                }
                                mKeyboard.updateCandidateView();
                            }
                        }
                        dismissCharButtonPopups();
                    }
                };
                
                mSelectCandidatePopup = new SelectCandidateDialog.Builder(HandwritingKeyboard.getBaseContext())
                        .setPositiveButton(R.string.candidate_popup_button_clear, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mCharButtons != null) {
                                    StrokeChar sc= mCharButtons.get(v);
                                    if (sc != null) {
                                        int index = mStrokeCharManager.indexOf(sc);
                                        if (index >= 0) {
                                            try {
                                                deleteOne(index);
                                            } catch (ArrayIndexOutOfBoundsException e) {
                                                if (DEBUG) Log.w(TAG, "strokeChar already removed", e);
                                            }
                                        }
                                    }
                                }
                                mKeyboard.checkDeleteButton();
                                dismissCharButtonPopups();
                            }
                        })
                        .setNegativeButton(R.string.candidate_popup_button_cancel, null)
                        .setCandidates(candidates)
                        .setSelectedIndex(selectedIndex)
                        .setOnClickCandidateListener(onClickCandidateListener)
                        .create();
                
                Window window = mSelectCandidatePopup.getWindow();
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.token = mStrokeLayout.getWindowToken();
                lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
                
                window.setAttributes(lp);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.6f);
                window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                mSelectCandidatePopup.show();
            }
        });
        return btn;
    }

    @Override
    public ArrayList<String> getSuggestions() {
        if (mStrokeCharManager.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mStrokeCharManager.size(); i++) {
            StrokeChar sc = mStrokeCharManager.get(i);
            if (sc != null)
                sb.append(sc.getSelectedLabel());
        }
        return new ArrayList<String>(Arrays.asList(sb.toString()));
    }

    private void removeCharButton(View btn) {
        mStrokeViewGroupLayout.removeView(btn);
        mCharButtons.remove(btn);
        btn = null;
    }

    @Override
    public void onClick(View v) {
        mHandler.removeMessages(MSG_SCROLL_RIGHT_SCREEN);
        
        Object tag = v.getTag();
        String stringTag = null;
        if (tag instanceof String) {
            stringTag = (String) tag;
        }
        
        if (stringTag.equals("beforeScreen")) {
            mScreenManager.scrollScreen(true);
            mStrokeView.setLastScreen(mScreenManager.isLastScreen());
        } else if (stringTag.equals("afterScreen")) {
            mScreenManager.scrollScreen(false);
            mStrokeView.setLastScreen(mScreenManager.isLastScreen());
        }
        
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

    public void updateCharButton() {
        // NOTE: don't understand, but mStrokeViewGroupLayout should be gotten
        // again after orientation changed
        if (mCharButtons==null) {
            return;
        }
        
        Resources res = mKeyboard.getResources();

        if (DEBUG) Log.d(TAG, "stroke size=" + mStrokeManager.size() + " char size=" + mStrokeCharManager.size());

        // create if btn is not added
        for (int i = 0; i < mStrokeCharManager.size(); i++) {
            StrokeChar strokeChar = mStrokeCharManager.get(i);
//            if (DEBUG) Log.e(TAG, "strokeChar=" + strokeChar);
            if (strokeChar == null || strokeChar.getLabels() == null ||
                    mCharButtons.containsValue(strokeChar) || strokeChar.getLabels().isEmpty() ||
                    (strokeChar.getLabels().size() == 1 && strokeChar.getLabels().get(0).equals(" "))) {
                continue;
            }
            ImageButton btn = createCharButton();
            mCharButtons.put(btn, strokeChar);
        }
        // remove if strokeChar is removed
        // TODO : shouldn't be like this. change mCharButton!
        for (View btn : mCharButtons.keySet()) {
            StrokeChar strokeChar = mCharButtons.get(btn);
            if (strokeChar == null || !mStrokeCharManager.contains(strokeChar)) {
                removeCharButton(btn);
            }
        }
        // update location of btn.
        int loopIndex = 0;
        int loopCount = 1;
        for (int i = 0; i < mStrokeCharManager.size(); i++) {
            StrokeChar strokeChar = mStrokeCharManager.get(i);
            if (strokeChar == null)
                continue;
            View btn = null;
            for (View v : mCharButtons.keySet()) {
                if (strokeChar.equals(mCharButtons.get(v))) {
                    btn = v;
                }
            }
            if (btn == null)
                continue;

            if (btn.getParent() != null && !btn.getParent().equals(mStrokeViewGroupLayout)
                    && btn.getParent() instanceof FrameLayout) {
                ((FrameLayout) btn.getParent()).removeView(btn);
            }
//            if (DEBUG) Log.d(TAG, "btn.getParent()=" + btn.getParent());
            if (btn.getParent() == null) {
                mStrokeViewGroupLayout.addView(btn);
            }

            float ratio = mKeyboard.getResources().getDimension(R.dimen.stroke_layout_height) / 100;
            float minX = strokeChar.getMinX() * ratio;
            float maxX = strokeChar.getMaxX() * ratio;
            int densityDpi = res.getDisplayMetrics().densityDpi;
            float imgWidth = res.getDrawableForDensity(R.drawable.button_state_char_candidate, densityDpi)
                    .getIntrinsicWidth();
            float imgHeight = res.getDrawableForDensity(R.drawable.button_state_char_candidate, densityDpi)
                    .getIntrinsicHeight();
            
            loopIndex++;
            if (loopIndex == loopCount) {
                int count = 0;
                for (int j = 0; j < mStrokeCharManager.size(); j++) {
                    StrokeChar sc = mStrokeCharManager.get(j);
                    if (sc != null) {
                        if (sc.getLabels().size() == 1 && sc.getLabels().get(0).equals(" "))
                            continue;
                        ArrayList<Stroke> strokeList = sc.getStrokes();
                        if (strokeList.containsAll(strokeChar.getStrokes()))
                            count++;
                    }
                }
                loopCount = count > 1 ? count : 1;
                loopIndex = 0;
            }
            btn.setX((minX + maxX - imgWidth * loopCount) / 2 + imgWidth * loopIndex);
            float buttonY = res.getDimension(R.dimen.stroke_view_height) -
                    res.getDimension(R.dimen.char_button_margin_bottom) - imgHeight;
            btn.setY(buttonY);
//            if (DEBUG) Log.e(TAG, "x=" + btn.getX() + " y=" + btn.getY());      
        }
    }

    @Override
    public void pickSuggestionManually(int index) {
        InputConnection ic = mKeyboard.getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(getSuggestions().get(index), 1);
        }
        clear();
    }
    
    @Override
    public void pickSuggestionManually(String string) {
        InputConnection ic = mKeyboard.getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(string, 1);
        }
        clear();
    }

    @Override
    @Deprecated
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart,
            int candidatesEnd) {
    }

    @Override
    public boolean dismissCharButtonPopups() {
//        if (mSelectCandidatePopup != null && mSelectCandidatePopup.isShowingPopup()) {
        if (mSelectCandidatePopup != null && mSelectCandidatePopup.isShowing()) {
            mSelectCandidatePopup.dismiss();
            return true;
        }
        return false;
    }

    private void setPenType(String penColor, String penType) {
        mStrokeView.setPenType(penColor, penType);
    }
    
    @Override
    public void setValuesByPrefs(SharedPreferences sharedPref) {
        String penColor = sharedPref.getString(mKeyboard.getString(R.string.HW_PREF_KEY_PEN_COLOR),
                mKeyboard.getString(R.string.HW_PEN_COLOR_DEFAULT));
        String penType = sharedPref.getString(mKeyboard.getString(R.string.HW_PREF_KEY_PEN_TYPE),
                mKeyboard.getString(R.string.HW_PEN_TYPE_BALLPEN));
        mIsAutoScrollMode = sharedPref.getBoolean(mKeyboard.getString(R.string.HW_PREF_KEY_AUTO_SCROLL), true);
        mAutoDelay = sharedPref.getInt(mKeyboard.getString(R.string.HW_PREF_KEY_AUTO_SCROLL_DELAY), 5);
        mNextScreenWidth = Float.parseFloat(sharedPref.getString(mKeyboard.getString(R.string.HW_PREF_KEY_AUTO_SCROLL_WIDTH),
                mKeyboard.getString(R.string.HW_AREA_WIDTH_NORMAL)));
        
        if (penColor != null && penType != null) {
            setPenType(penColor, penType);
        }
        mStrokeView.setAutoScroll(mIsAutoScrollMode);
        setAutoScrollWidth(mNextScreenWidth);
    }

    @Override
    public void clear() {
        mHandler.removeMessages(MSG_SCROLL_RIGHT_SCREEN);
        mStrokeManager.clear();
        mStrokeCharManager.clear();
        mScreenManager.clear();
        mStrokeView.redraw(mStrokeManager.getAll());
        removeCharButtonAll();

        mKeyboard.checkDeleteButton();
        // updateCharButton();
    }

    @Override
    public void destroy() {
        mHandler = null;
        if (mStrokeView != null) {
            mStrokeView.setOnStrokeListener(null);
            mStrokeView.setVisibility(View.GONE);
            mStrokeView = null;
        }
        if (mScreenManager != null) {
            mScreenManager.clear();
            mScreenManager = null;
        }

        // is it really in this way?
        for (View btn : mCharButtons.keySet()) {
            removeCharButton(btn);
        }
        mCharButtons = null;
    }

    private void recalcWindowSize() {
        float maxX = 0;
        float curX = 0;
        if (!mStrokeManager.isEmpty()) {
            maxX = mStrokeManager.getMaxX();
            Stroke curStroke = mStrokeManager.get(mStrokeManager.size() - 1);
            if (curStroke.size() > 0) {
                curX = curStroke.getX(curStroke.size() - 1);
            }
        }

        mScreenManager.init();
        mScreenManager.recalcScreen(maxX, curX);

        // screen manager
        if (mIsAutoScrollMode) {
            mStrokeView.setNextScreenRectF(mScreenManager.getNextScreenRectF());
        } else {
            mStrokeView.setNextScreenRectF(null);
        }
    }

    private final static int MSG_UPDATE_CHAR_BUTTON = 21;
    private final static int MSG_SCROLL_RIGHT_SCREEN = 22;
    private final static int MSG_GO_SCREEN = 23;

    private static class TextModeHandler extends Handler {
        private final WeakReference<TextModeManager> mRef;

        public TextModeHandler(TextModeManager manager) {
            mRef = new WeakReference<TextModeManager>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            TextModeManager manager = mRef.get();
            if (DEBUG) Log.i(TAG, "[handleMessage()] " + msg.what);            
            if (manager == null || manager.mScreenManager == null) {
                return;
            }

            switch (msg.what) {
            case MSG_UPDATE_CHAR_BUTTON:
                manager.updateCharButton();
                break;
            case MSG_SCROLL_RIGHT_SCREEN:
                manager.mScreenManager.autoScrollRight(manager.mStrokeView);
                break;
            case MSG_GO_SCREEN:
                manager.mScreenManager.changeScreenSize();
                manager.mStrokeView.setLastScreen(manager.mScreenManager.isLastScreen());
                break;
            }
        }
    }

}
