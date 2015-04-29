/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiManager;
import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;
import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnSymbolEngine;
import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.jajp.DefaultSoftKeyboardJAJP;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The one touch emoji list manager.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class OneTouchEmojiListViewManager {
    /** Size of candidates view (normal) */
    private static final int VIEW_TYPE_NORMAL = 0;
    /** Size of candidates view (full) */
    private static final int VIEW_TYPE_FULL   = 1;

    /** Default number of candidate display row on landscape */
    private static final int NUMBER_OF_ROW_NUM_LAND_DEFAULT = 5;
    /** Default number of candidate display row on portrait */
    private static final int NUMBER_OF_ROW_NUM_PORT_DEFAULT = 6;
    /** Number of candidate display row on landscape 1st view */
    private static final int NUMBER_OF_1ST_VIEW_ROW_NUM_LAND = 17;
    /** Number of candidate display row on landscape 1st view when qwerty split keyboard */
    private static final int NUMBER_OF_1ST_VIEW_ROW_NUM_LAND_SPLIT_QWERTY = 33;
    /** Number of candidate display row on portrait 1st view */
    private static final int NUMBER_OF_1ST_VIEW_ROW_NUM_PORT = 66;
    /** Number of candidate display column on landscape 1st view */
    private static final int NUMBER_OF_1ST_VIEW_COLUMN_NUM_LAND = 4;
    /** Number of candidate display column on split keyboard QWERTY landscape 1st view */
    private static final int NUMBER_OF_1ST_VIEW_COLUMN_NUM_SPLIT_QWERTY = 2;
    /** Number of candidate display column on portrait 1st view */
    private static final int NUMBER_OF_1ST_VIEW_COLUMN_NUM_PORT = 1;
    /** Number of candidate display row on landscape 2nd view */
    private static final int NUMBER_OF_2ND_VIEW_ROW_NUM_LAND = 6;
    /** Number of candidate display row on portrait 2nd view */
    private static final int NUMBER_OF_2ND_VIEW_ROW_NUM_PORT = 11;
    /** Number of candidate display column on landscape 2nd view */
    private static final int NUMBER_OF_2ND_VIEW_COLUMN_NUM_LAND = 11;
    /** Number of candidate display column on portrait 2nd view */
    private static final int NUMBER_OF_2ND_VIEW_COLUMN_NUM_PORT = 6;
    /** Total Number of candidate display (Contains the read more button) */
    private static final int NUMBER_OF_DISPLAY_CANDIDATE = 66;
    /** Max scroll page number of 1st view */
    private static final int NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE = 4;
    /** Max scroll page number of 1st view on QWERTY split mode*/
    private static final int NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY = 7;
    /** Max scroll page number of 2nd view */
    private static final int NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE = 2;
    /** Number of DecoEmoji when Emoji & Deco mixed mode */
    private static final int NUMBER_OF_MIXED_DECOEMOJI = 32;
    /** Number of Emoji when Emoji & Deco mixed mode */
    private static final int NUMBER_OF_MIXED_EMOJI = 33;
    /** Column of 2nd view read more button on split mode */
    private static final int NUMBER_OF_READMORE_BTN_COLUMN_SPLIT = 4;

    /** Onetouch Emoji setting mode : set emoji when not Emoji & Deco mixed */
    private static final int MODE_ONETOUCHEMOJI_UNMIXED = 0;
    /** Onetouch Emoji setting mode : set DecoEmoji when Emoji & Deco mixed */
    private static final int MODE_ONETOUCHEMOJI_MIXED_DECOEMOJI = 1;
    /** Onetouch Emoji setting mode : set Emoji when Emoji & Deco mixed */
    private static final int MODE_ONETOUCHEMOJI_MIXED_EMOJI = 2;

    /** Message for {@code mHandler} (execute update view) */
    private static final int MSG_UPDATE_VIEW = 1;
    /* delay set scrollview */
    private static final int MSG_SET_SCROLLVIEW = 2;
    /** Delay time(msec.) update view. */
    private static final int UPDATE_VIEW_DELAY_MS = 0;
    /** Delay of set candidate */
    private static final int SET_SCROLLVIEW_DELAY = 0;
    /** Delay time(msec.) update view(decoemoji on). */
    private static final int UPDATE_VIEW_DELAY_DECOEMOJI_MS = 200;
    /** Delay of set candidate(decoemoji on) */
    private static final int SET_SCROLLVIEW_DECOEMOJI_DELAY = 200;
    /** for Delay of set candidate */
    private boolean mInitFlg = false;

    /** Body view of the one touch emoji list */
    private ViewGroup  mViewBody = null;
    /** Base view of full view */
    private LinearLayout mViewBase2nd;

    /** Body view of the one touch emoji list layout1st */
    private LinearLayout  mLinearLayout1st = null;
    /** Body view of the one touch emoji list layout2nd */
    private LinearLayout  mLinearLayout2nd = null;

    /** 1st scroll view of the one touch emoji list */
    private ScrollView  mScrollView1st = null;
    /** 1st scroll view of the one touch emoji list */
    private ScrollView  mScrollView1stSplitQwerty = null;
    /** 2nd scroll view of the one touch emoji list */
    private ScrollView  mScrollView2nd = null;

    /** Layout for the candidates list on normal view */
    private LinearLayout mViewCandidateList1st;
    /** Layout for the candidates list on normal view on qwerty split mode*/
    private LinearLayout mViewCandidateList1stSplitQwerty;
    /** Layout for the candidates list on full view */
    private LinearLayout mViewCandidateList2nd;
    /** Child layout array of 1st scroll view */
    private LinearLayout[] mScrollChildLayout1st = new LinearLayout[NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE];
    /** Child layout array of 1st scroll view on QWERTY split mode */
    private LinearLayout[] mScrollChildLayout1stSplitQwerty
        = new LinearLayout[NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY];
    /** Child layout array of 2nd scroll view */
    private LinearLayout[] mScrollChildLayout2nd = new LinearLayout[NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE];
    /** Page row size array  of 1st scroll view */
    private int[] mScrollPageRowSize1st;
    /** Page row size array  of 1st scroll view on QWERTY split mode */
    private int[] mScrollPageRowSize1stSplitQwerty;
    /** Page row size array  of 2nd scroll view */
    private int[] mScrollPageRowSize2nd;

    /** Text size of candidates */
    private float mCandidateTextSize;
    /** Minimum height of the candidate view */
    private int mCandidateHeight;
    /** Minimum width of the candidate view */
    private int mCandidateWidth;

    /** {@link OpenWnn} instance using this manager */
    private OpenWnn mWnn;

    /** DefaultSoftKeyboard instance using this manager */
    private DefaultSoftKeyboard mInputView;

    /** Button displayed left or right side when there are more candidates */
    private ImageView mReadMoreButton;

    /** Vibrator for touch vibration */
    private Vibrator mVibrator = null;
    /** SoundManager for click sound */
    private boolean mSound = false;

    /** List of candidates for 1stView */
    private ArrayList<WnnWord> mWnnWordArray1st = new ArrayList<WnnWord>();
    /** List of candidates for 2ndView */
    private ArrayList<WnnWord> mWnnWordArray2nd = new ArrayList<WnnWord>();

    /** DecoEmoji row number list of 1stView for mixed display mode */
    private ArrayList<Integer> mMixedDecoEmojiIndexList1st = new ArrayList<Integer>();
    /** DecoEmoji Row number list of 2ndView for mixed display mode */
    private ArrayList<Integer> mMixedDecoEmojiIndexList2nd = new ArrayList<Integer>();
    /** Emoji row number list of 1stView for mixed display mode */
    private ArrayList<Integer> mMixedEmojiIndexList1st = new ArrayList<Integer>();
    /** Emoji row number list of 2ndView for mixed display mode */
    private ArrayList<Integer> mMixedEmojiIndexList2nd = new ArrayList<Integer>();

    /** Pseudo engine (GIJI) for sign (Backup variable for switch) */
    private IWnnSymbolEngine mConverterSymbolEngine = null;

    /** {@code true} if the full screen mode is selected */
    private boolean mIsFullView = false;

    /** {@code true} if the one handed mode is selected */
    private boolean mIsOnehanded = false;

    /** Number of candidate display row default */
    private int mCandidateDefaultRowNum;
    /** Number of candidate display column on 1st view */
    private int mCandidate1stViewColumnNum;
    /** Number of candidate display column on 1st view on qwerty split mode */
    private int mCandidate1stViewColumnNumSplitQwerty;
    /** Number of candidate display column on 2nd view */
    private int mCandidate2ndViewColumnNum;
    /** Index of 1st view read more button */
    private int m1stViewReadMoreIndex = 0;
    /** Candidate count for 1st view */
    private int m1stViewCount = 0;
    /** Id value for 1st view */
    private int m1stViewId = 0;
    /** Index of 2nd view read more button */
    private int m2ndViewReadMoreIndex = 0;
    /** Candidate count for 2nd view */
    private int m2ndViewCount = 0;
    /** Id value for 2nd view */
    private int m2ndViewId = 0;
    /** Max number of candidate display */
    private int mCandidateEmojiMax;
    /** 1st view index after finished setting history in mixed mode */
    private int mMixedHistoryIndex1st;
    /** 2nd view index after finished setting history in mixed mode */
    private int mMixedHistoryIndex2nd;
    /** 1st view index after finished DecoEmoji set in mixed mode */
    private int mMixedDecoEmojiIndex1st;
    /** 2nd view index after finished DecoEmoji set in mixed mode */
    private int mMixedDecoEmojiIndex2nd;
    /** Max number of candidate display in one scroll page of 1st view */
    private int mScrollPageChangeNum1st;
    /** Max number of candidate display in one scroll page of 1st view on qwerty split mode */
    private int mScrollPageChangeNum1stSplitQwerty;
    /** Max number of candidate display in one scroll page of 2nd view */
    private int mScrollPageChangeNum2nd;
    /** Max index value of 1st view */
    private int mIndexMax1st;
    /** Max index value of 1st view on qwerty split mode */
    private int mIndexMax1stSplitQwerty;
    /** Max index value of 2nd view */
    private int mIndexMax2nd;

    /** Mapping table for regist candidate */
    private HashMap<String, String> mRegistCandidate;

    /** Color of the candidates */
    private int mTextColor = 0;

    private String mListMode = DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT;

    /** Width of 1st view base */
    private int mViewBodyWidth;

    /** Width of 2nd view base */
    private int mViewBody2ndWidth;

    /** Whether being able to use Emoji */
    private boolean mEnableEmoji = false;

    /** Whether being able to use DecoEmoji */
    private boolean mEnableDecoEmoji = false;

    /** Whether being able to use Symbol */
    private boolean mEnableSymbol = false;

    /** View body location x on normal mode */
    private float mViewBodyNormalX = 0;

    /** Key of "key_height_portrait" setting. */
    private static final String KEY_HEIGHT_PORTRAIT_KEY = "key_height_portrait";
    /** Key of "key_height_landscape" setting. */
    private static final String KEY_HEIGHT_LANDSCAPE_KEY = "key_height_landscape";

    /** Default value of keyboard line */
    private static final int DEFAULT_VALUE_KEYLINE = 4;

    /** Event listener for touching a candidate of Symbol */
    private OnClickListener mCandidateOnClickForSymbol = new OnClickListener() {
        public void onClick(View v) {
            onClickCandidate(v, IWnnSymbolEngine.MODE_ONETOUCHEMOJI_SYMBOL);
        }
    };

    /** Event listener for touching a candidate of Emoji */
    private OnClickListener mCandidateOnClickForEmoji = new OnClickListener() {
        public void onClick(View v) {
            onClickCandidate(v, IWnnSymbolEngine.MODE_ONETOUCHEMOJI_EMOJI);
        }
    };

    /** Event listener for touching a candidate of Decoemoji */
    private OnClickListener mCandidateOnClickForDecoEmoji = new OnClickListener() {
        public void onClick(View v) {
            onClickCandidate(v, IWnnSymbolEngine.MODE_ONETOUCHEMOJI_DECOEMOJI);
        }
    };

    /** IME Sound change StandardKey */
    private static final int SOUND_STANDARD_KEY = 4;

    /** {@code Handler} for drawing candidates/displaying */
    Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_VIEW:
                    if (mInitFlg) {
                        updateViewInternal();
                    }
                    break;
                case MSG_SET_SCROLLVIEW:
                    setScrollViewLayout(mInputView.mDisplayMode);
                    if (mHandler.hasMessages(MSG_UPDATE_VIEW)) {
                        mHandler.removeMessages(MSG_UPDATE_VIEW);
                        updateViewInternal();
                    }
                    break;

                default:
                    break;

           }
        }
    };

    /**
     * Constructor
     */
    public OneTouchEmojiListViewManager() {
    }

    /**
     * Initialize the view.
     *
     * @param parent    The OpenWnn object
     */
    public void initView(OpenWnn parent, DefaultSoftKeyboard inputView) {
        mWnn = parent;
        mInputView = inputView;

        initScrollViewLayout();

        Resources r = mWnn.getResources();

        mIsOnehanded = mInputView.isOneHandedMode();

        mCandidateTextSize = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_text_size);
        if (!mIsOnehanded) {
            mCandidateWidth = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_width);
        } else {
            mCandidateWidth = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_width_one_handed);
        }

        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        mTextColor = keyskin.getColor(OpenWnn.superGetContext(), R.color.candidate_text);

        String heightKey;
        if (r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            heightKey = KEY_HEIGHT_PORTRAIT_KEY;
        } else {
            heightKey = KEY_HEIGHT_LANDSCAPE_KEY;
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
        int defaultKeyHeight = r.getDimensionPixelSize(R.dimen.key_height);
        int defaultHeight;
        if (!mIsOnehanded) {
            defaultHeight = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_height);
        } else {
            defaultHeight = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_height_one_handed);
        }
        float heightPercent = (float)defaultKeyHeight /
                (float)pref.getInt(heightKey, defaultKeyHeight);
        mCandidateHeight = (int)((float)defaultHeight / heightPercent);

        setDisplayMode(inputView.mDisplayMode);

        if (mConverterSymbolEngine != null) {
            mConverterSymbolEngine.close();
        }
        mConverterSymbolEngine = new IWnnSymbolEngine(OpenWnn.superGetContext(), null);

        LayoutInflater inflater = parent.getLayoutInflater();
        if (mViewBody != null) {
            removeAllViewRecursive(mViewBody);
        }
        mViewBody = (ViewGroup)inflater.inflate(R.layout.one_touch_emoji_list, null);

        mLinearLayout1st = (LinearLayout)mViewBody.findViewById(R.id.one_touch_emoji_list_1st_base);
        if (keyskin.isValid()){
            int top = mLinearLayout1st.getPaddingTop();
            int bottom = mLinearLayout1st.getPaddingBottom();
            mLinearLayout1st.setBackground(keyskin.getKeyboardBg());
            mLinearLayout1st.setPadding(mLinearLayout1st.getPaddingLeft(), top, mLinearLayout1st.getPaddingRight(), bottom);
        }
        mLinearLayout2nd = (LinearLayout)mViewBody.findViewById(R.id.one_touch_emoji_list_2nd_base);
        if (keyskin.isValid()){
            int top = mLinearLayout2nd.getPaddingTop();
            int bottom = mLinearLayout2nd.getPaddingBottom();
            mLinearLayout2nd.setBackground(keyskin.getKeyboardBg());
            mLinearLayout2nd.setPadding(mLinearLayout2nd.getPaddingLeft(), top, mLinearLayout2nd.getPaddingRight(), bottom);
        }

        mScrollView1st = (ScrollView)mViewBody.findViewById(R.id.one_touch_emoji_list_scroll_1st_view);
        mScrollView1st.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mCandidateHeight * mCandidateDefaultRowNum));
        mScrollView1stSplitQwerty = (ScrollView) mViewBody
                .findViewById(R.id.one_touch_emoji_list_scroll_1st_view_split_qwerty);
        mScrollView1stSplitQwerty.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mCandidateHeight * mCandidateDefaultRowNum));
        mScrollView2nd = (ScrollView)mViewBody.findViewById(R.id.one_touch_emoji_list_scroll_2nd_view);
        mScrollView2nd.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mCandidateHeight * mCandidateDefaultRowNum));
        mViewCandidateList1st = (LinearLayout)mViewBody.findViewById(
                R.id.one_touch_emoji_list_1st_view);
        mViewCandidateList1stSplitQwerty = (LinearLayout)mViewBody.findViewById(
                R.id.one_touch_emoji_list_1st_view_split_qwerty);
        mViewCandidateList2nd = (LinearLayout)mViewBody.findViewById(
                R.id.one_touch_emoji_list_2nd_view);
        mViewBase2nd = (LinearLayout)mViewBody.findViewById(
                R.id.one_touch_emoji_list_2nd_base);
        int paddingTop;
        int paddingBottom;
        int paddingLeft;
        int paddingRight;
        if (!mIsOnehanded) {
            paddingTop = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_top);
            paddingBottom = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_bottom);
            paddingLeft = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_left);
            paddingRight = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_right);
        } else {
            paddingTop = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_top_one_handed);
            paddingBottom = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_bottom_one_handed);
            paddingLeft = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_left_one_handed);
            paddingRight = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_padding_keyboard_right_one_handed);
        }
        mViewBase2nd.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        mHandler.removeMessages(MSG_SET_SCROLLVIEW);
        if (mEnableDecoEmoji) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_SCROLLVIEW), SET_SCROLLVIEW_DECOEMOJI_DELAY);
        } else {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_SCROLLVIEW), SET_SCROLLVIEW_DELAY);
        }

        mReadMoreButton = (ImageView)mViewBody.findViewById(
                R.id.one_touch_emoji_list_read_more_button);

        mReadMoreButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (mIsFullView) {
                        setViewLayout(VIEW_TYPE_NORMAL);
                        mInputView.getKeyboardView().setVisibility(View.VISIBLE);
                    } else {
                        setViewLayout(VIEW_TYPE_FULL);
                        mInputView.getKeyboardView().setVisibility(View.GONE);
                        mInputView.getKeyboardView().handleBack();
                    }
                    setReadMore();

                    if (mInputView.isSymbolKeyboard()) {
                        mInputView.getKeyboardView().setVisibility(View.VISIBLE);
                    }
                }
            });
        mReadMoreButton.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
                    int resid = 0;
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (WnnKeyboardFactory.getSplitMode()) {
                            if (mIsFullView) {
                                resid = R.drawable.ime_keypad_emoji_cut_btn_pressed;
                            } else {
                                resid = R.drawable.ime_keypad_emoji_extend_btn_pressed;
                            }
                        } else {
                            if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT.equals(mListMode)) {
                                if (mIsFullView) {
                                    resid = R.drawable.cand_emoji_left_press;
                                } else {
                                    resid = R.drawable.cand_emoji_right_press;
                                }
                            } else {
                                if (mIsFullView) {
                                    resid = R.drawable.cand_emoji_right_press;
                                } else {
                                    resid = R.drawable.cand_emoji_left_press;
                                }
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (WnnKeyboardFactory.getSplitMode()) {
                            if (mIsFullView) {
                                resid = R.drawable.ime_keypad_emoji_cut_btn_normal;
                            } else {
                                resid = R.drawable.ime_keypad_emoji_extend_btn_normal;
                            }
                        } else {
                            if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT.equals(mListMode)) {
                                if (mIsFullView) {
                                    resid = R.drawable.cand_emoji_left;
                                } else {
                                    resid = R.drawable.cand_emoji_right;
                                }
                            } else {
                                if (mIsFullView) {
                                    resid = R.drawable.cand_emoji_right;
                                } else {
                                    resid = R.drawable.cand_emoji_left;
                                }
                            }
                        }
                        break;

                    default:
                        break;

                    }

                    if (resid != 0) {
                        Drawable button = null;
                        if (keyskin.isValid()) {
                            button = keyskin.getDrawable(resid);
                        }
                        if (button != null) {
                            mReadMoreButton.setImageDrawable(button);
                        } else {
                            mReadMoreButton.setImageResource(resid);
                        }
                    }
                    return false;
                }
            });

        setViewLayout(VIEW_TYPE_NORMAL);
        mViewCandidateList1st.setVisibility(View.INVISIBLE);
        mViewCandidateList1stSplitQwerty.setVisibility(View.INVISIBLE);

        return;
    }

    /**
     * Create a view for a candidate.
     *
     * @return the view
     */
    private TextView createCandidateView(int status) {
        TextView text = new TextView(mViewBody.getContext());
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandidateTextSize);
        text.setTextColor(mTextColor);
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        if (keyskin.isValid()) {
            text.setBackgroundDrawable(keyskin.getCandidateBackgroundEmoji());
        } else {
            text.setBackgroundResource(R.drawable.cand_back_emoji);
        }
        text.setCompoundDrawablePadding(0);
        text.setGravity(Gravity.CENTER);
        text.setSingleLine();
        text.setSoundEffectsEnabled(false);
        text.setPadding(0, 0, 0, 0);
        text.setMinHeight(mCandidateHeight);
        if ((status == VIEW_TYPE_NORMAL) && (WnnKeyboardFactory.getSplitMode())) {
            text.setMinimumWidth(mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width_split_1stview));
        } else if (mIsOnehanded) {
            text.setMinimumWidth(mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width_one_handed));
        } else {
            text.setMinimumWidth(mWnn.getResources().getDimensionPixelSize(
                    R.dimen.one_touch_emoji_list_width));
        }
        return text;
    }

    /**
     * Get the view.
     *
     * @return The one touch emoji list view.
     */
    public View getView() {
        return mViewBody;
    }

    /**
     * Show the view.
     */
    public void showView() {
        if (mViewBody != null) {
            mViewBody.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide the view.
     */
    public void hideView() {
        if (mViewBody != null) {
            mViewBody.setVisibility(View.GONE);
        }
    }

    /**
     * Update the view.
     *
     * @param enableEmoji     {@code true}  - Emoji is enabled.
     *                        {@code false} - Emoji is disabled.
     * @param enableDecoEmoji {@code true}  - DecoEmoji is enabled.
     *                        {@code false} - DecoEmoji is disabled.
     * @param enableSymbol    {@code true}  - Symbol is enabled.
     *                        {@code false} - Symbol is disabled.
     * @param listMode        Mode of OneTouchEmojiList
     */
    public void updateView(
            boolean enableEmoji, boolean enableDecoEmoji, boolean enableSymbol, String listMode) {

        mEnableEmoji = enableEmoji;
        mEnableDecoEmoji = enableDecoEmoji;
        mEnableSymbol = enableSymbol;
        mListMode = listMode;

        mHandler.removeMessages(MSG_UPDATE_VIEW);
        if (mEnableDecoEmoji) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_VIEW), UPDATE_VIEW_DELAY_DECOEMOJI_MS);
        } else {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_VIEW), UPDATE_VIEW_DELAY_MS);
        }
    }

    /**
     * Update the view.
     */
    public void updateViewInternal() {

        int decoEmojiNum1stView = 0;
        int decoEmojiNum2ndView = 0;

        m1stViewCount = 0;
        m1stViewId = 0;
        m2ndViewCount = 0;
        m2ndViewId = 0;
        mMixedHistoryIndex1st = 0;
        mMixedHistoryIndex2nd = 0;
        mMixedDecoEmojiIndex1st = 0;
        mMixedDecoEmojiIndex2nd = 0;
        mRegistCandidate = new HashMap<String, String>();

        initStatus();

        if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT.equals(mListMode)) {
            m1stViewReadMoreIndex = 0;
            m2ndViewReadMoreIndex = 0;
        } else {
            m1stViewReadMoreIndex = (mCandidate1stViewColumnNum - 1) * mCandidateDefaultRowNum;
            m2ndViewReadMoreIndex = (mCandidate2ndViewColumnNum - 1) * mCandidateDefaultRowNum;
        }
        if (WnnKeyboardFactory.getSplitMode()) {
            m2ndViewReadMoreIndex = mCandidateDefaultRowNum * NUMBER_OF_READMORE_BTN_COLUMN_SPLIT;
        }

        mWnnWordArray1st.clear();
        mWnnWordArray2nd.clear();
        clearCandidateView();

        if (mEnableDecoEmoji && mEnableEmoji) {
            //Set mixed Emoji and Decoemoji to history candidate view when DecoEmoji and Emoji are both enable
            mConverterSymbolEngine.setMode(IWnnSymbolEngine.MODE_ONETOUCHEMOJI_MIXED_EMOJI);
            setCandidateView(mCandidateEmojiMax, mCandidateEmojiMax, null, true, MODE_ONETOUCHEMOJI_UNMIXED);

            mMixedHistoryIndex1st = m1stViewCount;
            mMixedHistoryIndex2nd = m2ndViewCount;

            if ((m1stViewCount < (mCandidateEmojiMax + 1))
                    || (m2ndViewCount < (mCandidateEmojiMax + 1))) {
                decoEmojiNum1stView = NUMBER_OF_MIXED_DECOEMOJI + m1stViewCount;
                decoEmojiNum2ndView = NUMBER_OF_MIXED_DECOEMOJI + m2ndViewCount;

                //Set DecoEmoji to candidate view first when DecoEmoji and Emoji are both enable
                mConverterSymbolEngine.setMode(IWnnSymbolEngine.MODE_ONETOUCHEMOJI_DECOEMOJI);
                setCandidateView(decoEmojiNum1stView, decoEmojiNum2ndView,
                        mCandidateOnClickForDecoEmoji, false, MODE_ONETOUCHEMOJI_MIXED_DECOEMOJI);

                mMixedDecoEmojiIndex1st = m1stViewCount;
                mMixedDecoEmojiIndex2nd = m2ndViewCount;

                decoEmojiNum1stView = NUMBER_OF_MIXED_EMOJI + m1stViewCount - 1;
                decoEmojiNum2ndView = NUMBER_OF_MIXED_EMOJI + m2ndViewCount - 1;

                //Continuously set Emoji to the left part of candidate view after setting DecoEmoji
                mConverterSymbolEngine.setMode(IWnnSymbolEngine.MODE_ONETOUCHEMOJI_EMOJI);
                setCandidateView(decoEmojiNum1stView, decoEmojiNum2ndView,
                        mCandidateOnClickForEmoji, false, MODE_ONETOUCHEMOJI_MIXED_EMOJI);
            }
        } else if (mEnableDecoEmoji) {
            mConverterSymbolEngine.setMode(IWnnSymbolEngine.MODE_ONETOUCHEMOJI_DECOEMOJI);
            setCandidateView(mCandidateEmojiMax, mCandidateEmojiMax,
                    mCandidateOnClickForDecoEmoji, true, MODE_ONETOUCHEMOJI_UNMIXED);
        } else if (mEnableEmoji) {
            mConverterSymbolEngine.setMode(IWnnSymbolEngine.MODE_ONETOUCHEMOJI_EMOJI);
            setCandidateView(mCandidateEmojiMax, mCandidateEmojiMax,
                    mCandidateOnClickForEmoji, true, MODE_ONETOUCHEMOJI_UNMIXED);
        } else if (mEnableSymbol) {
            mConverterSymbolEngine.setMode(IWnnSymbolEngine.MODE_ONETOUCHEMOJI_SYMBOL);
            setCandidateView(mCandidateEmojiMax, mCandidateEmojiMax,
                    mCandidateOnClickForSymbol, true, MODE_ONETOUCHEMOJI_UNMIXED);
        }
        setReadMore();
    }

    /**
     * Replace the preferences in the candidates view.
     *
     * @param pref    The preferences
     */
    public void setPreferences(SharedPreferences pref) {
        try {
            if (pref.getBoolean("key_vibration", false)) {
                mVibrator = (Vibrator)mWnn.getSystemService(Context.VIBRATOR_SERVICE);
                if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "VIBRATOR ON");}
            } else {
                mVibrator = null;
            }
            mSound = pref.getBoolean("key_sound", true);

            boolean isLeft = false;

            mListMode = pref.getString("opt_one_touch_emoji_list",
                    DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT);
            if (WnnKeyboardFactory.getSplitMode()
                    && (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_RIGHT.equals(mListMode))) {
                mListMode = DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT;
            }
            Resources r = mWnn.getResources();
            if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT.equals(mListMode)) {
                ((LinearLayout)mViewBody.findViewById(R.id.one_touch_emoji_list_read_more))
                        .setGravity(Gravity.LEFT);
                isLeft = true;
            } else if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_RIGHT.equals(mListMode)){
                ((LinearLayout)mViewBody.findViewById(R.id.one_touch_emoji_list_read_more))
                        .setGravity(Gravity.RIGHT);
            } // else

            int readMorePaddingTop;
            int readMorePaddingLeft;
            int readMorePaddingRight;

            if (!mIsOnehanded) {
                readMorePaddingTop =r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_read_more_padding_top);
                readMorePaddingLeft = (WnnKeyboardFactory.getSplitMode()?
                    r.getDimensionPixelSize
                    (R.dimen.one_touch_emoji_list_read_more_padding_left_split_1st) :
                    r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_read_more_padding_left));
                readMorePaddingRight =
                    r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_read_more_padding_right);
            } else {
                readMorePaddingTop =
                    r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_read_more_padding_top_one_handed);
                readMorePaddingLeft = 
                    r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_read_more_padding_left_one_handed);
                readMorePaddingRight =
                    r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_read_more_padding_right_one_handed);
            }

            mReadMoreButton.setPadding(isLeft ? readMorePaddingLeft : 0,
                    readMorePaddingTop,
                    isLeft ? 0 : readMorePaddingRight,
                    0);

        } catch (Exception ex) {
            Log.d("OpenWnn", "NO VIBRATOR");
        }
    }

    /**
     * Set the view layout.
     *
     * @param type      View type,
     */
    private void setViewLayout(int type) {

        LayoutParams params;
        Resources r = mWnn.getResources();

        switch (type) {
        case VIEW_TYPE_FULL:
            mIsFullView = true;
            if (!mIsOnehanded){
                mCandidateWidth = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_width);
            } else {
                mCandidateWidth = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_width_one_handed);
            }
            String heightKey;
            if (r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                heightKey = KEY_HEIGHT_PORTRAIT_KEY;
            } else {
                heightKey = KEY_HEIGHT_LANDSCAPE_KEY;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
            if (WnnKeyboardFactory.getSplitMode()) {
                params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (pref.getInt(heightKey, r.getDimensionPixelSize(R.dimen.key_height))
                        + r.getDimensionPixelSize(R.dimen.keyboard_12key_common_vertical_gap))
                        * DEFAULT_VALUE_KEYLINE 
                        - r.getDimensionPixelSize(R.dimen.keyboard_12key_common_vertical_gap)
                        + r.getDimensionPixelSize(R.dimen.padding_keyboard_top)
                        + r.getDimensionPixelSize(R.dimen.padding_keyboard_bottom));
            } else {
                if (!mIsOnehanded) {
                    params = new LinearLayout.LayoutParams(mViewBody2ndWidth,
                            pref.getInt(heightKey, (r.getDimensionPixelSize(R.dimen.key_height)
                            + r.getDimensionPixelSize(R.dimen.keyboard_12key_common_vertical_gap))) 
                            * DEFAULT_VALUE_KEYLINE 
                            - r.getDimensionPixelSize(R.dimen.keyboard_12key_common_vertical_gap)
                            + r.getDimensionPixelSize(R.dimen.padding_keyboard_top)
                            + r.getDimensionPixelSize(R.dimen.padding_keyboard_bottom));
                } else {
                    params = new LinearLayout.LayoutParams(mViewBody2ndWidth,
                            (r.getDimensionPixelSize(R.dimen.key_height_one_handed)
                            + r.getDimensionPixelSize(R.dimen.keyboard_one_handed_vertical_gap))
                            * DEFAULT_VALUE_KEYLINE 
                            - r.getDimensionPixelSize(R.dimen.keyboard_one_handed_vertical_gap)
                            + r.getDimensionPixelSize(R.dimen.one_handed_keybord_padding_keyboard_top)
                            + r.getDimensionPixelSize(R.dimen.one_handed_keybord_padding_keyboard_bottom));
                }
            }

            mViewBody.setLayoutParams(params);
            if (WnnKeyboardFactory.getSplitMode()) {
                int leftPadding = r.getDimensionPixelSize
                        (R.dimen.one_touch_emoji_list_read_more_padding_left_split_2nd);
                mViewBodyNormalX = mViewBody.getX();

                getView().setX(0);
                mReadMoreButton.setPadding(
                        leftPadding,
                        mReadMoreButton.getPaddingTop(),
                        mReadMoreButton.getPaddingRight(),
                        mReadMoreButton.getPaddingBottom());
            }
            mViewCandidateList1st.setVisibility(View.GONE);
            mViewCandidateList1stSplitQwerty.setVisibility(View.GONE);
            mViewBase2nd.setVisibility(View.VISIBLE);
            mScrollView2nd.scrollTo(0, 0);
            break;

        case VIEW_TYPE_NORMAL:
        default:
            mIsFullView = false;

            if (WnnKeyboardFactory.getSplitMode()) {
                params = new FrameLayout.LayoutParams(mViewBodyWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                if (mViewBodyNormalX != 0) {
                    getView().setX(mViewBodyNormalX);
                    mViewBodyNormalX = 0;
                }
                int leftPadding = r.getDimensionPixelSize
                        (R.dimen.one_touch_emoji_list_read_more_padding_left_split_1st);
                mReadMoreButton.setPadding(leftPadding,
                                mReadMoreButton.getPaddingTop(),
                                mReadMoreButton.getPaddingRight(),
                                mReadMoreButton.getPaddingBottom());
                mCandidateWidth = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_width_split_1stview);
            } else {
                params = new LinearLayout.LayoutParams(mViewBodyWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                if (!mIsOnehanded) {
                    mCandidateWidth = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_width);
                } else {
                    mCandidateWidth = r.getDimensionPixelSize(R.dimen.one_touch_emoji_list_width_one_handed);
                }
            }
            mViewBody.setLayoutParams(params);
            if (isSplitQwerty()) {
                mViewCandidateList1st.setVisibility(View.GONE);
                mViewCandidateList1stSplitQwerty.setVisibility(View.VISIBLE);
                mScrollView1stSplitQwerty.scrollTo(0, 0);
            } else {
                mViewCandidateList1stSplitQwerty.setVisibility(View.GONE);
                mViewCandidateList1st.setVisibility(View.VISIBLE);
                mScrollView1st.scrollTo(0, 0);
            }
            mViewBase2nd.setVisibility(View.GONE);

            break;

        }
    }

    /**
     * Display Read More Button if there are more candidates.
     */
    private void setReadMore() {

        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        int resid = 0;

        if (WnnKeyboardFactory.getSplitMode()) {
            if (mIsFullView) {
                resid = R.drawable.ime_keypad_emoji_cut_btn_normal;
            } else {
                resid = R.drawable.ime_keypad_emoji_extend_btn_normal;
            }
        } else {
            if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_LEFT.equals(mListMode)) {
                if (mIsFullView) {
                    resid = R.drawable.cand_emoji_left;
                } else {
                    resid = R.drawable.cand_emoji_right;
                }
            } else {
                if (mIsFullView) {
                    resid = R.drawable.cand_emoji_right;
                } else {
                    resid = R.drawable.cand_emoji_left;
                }
            }
        }

        if (resid != 0) {
            Drawable button = null;
            if (keyskin.isValid()) {
                button = keyskin.getDrawable(resid);
            }
            if (button != null) {
                mReadMoreButton.setImageDrawable(button);
            } else {
                mReadMoreButton.setImageResource(resid);
            }
        }
    }

    /**
     * Handle a click event on the candidate.
     *
     * @param v  View
     * @param mode  Mode of SymbolEngine
     */
    private void onClickCandidate(View v, int mode) {
        if (!v.isShown()) {
            return;
        }

        playSoundAndVibration();

        WnnWord word;
        if (!mIsFullView) {
            word = mWnnWordArray1st.get(v.getId());
        } else {
            word = mWnnWordArray2nd.get(v.getId());
        }

        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.COMMIT_COMPOSING_TEXT));
        if (mode == IWnnSymbolEngine.MODE_ONETOUCHEMOJI_DECOEMOJI) {
            IDecoEmojiManager bind = OpenWnn.getCurrentIme().getDecoEmojiBindInterface();
            if (bind == null) {
                return;
            }
            try {
                int ret = bind.aidl_changeHistory(word.candidate);
            } catch (RemoteException e) {
                return;
            }
        }
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.SELECT_CANDIDATE, word));

        int oldMode = mConverterSymbolEngine.getMode();
        mConverterSymbolEngine.setMode(mode);
        mConverterSymbolEngine.learn(word);
        mConverterSymbolEngine.setMode(oldMode);

    }

    /**
     * Initialize One Touch Emoji List Status.
     */
    private void initStatus() {
        if (isSplitQwerty()) {
            set1stViewWidth(mCandidateWidth * mCandidate1stViewColumnNumSplitQwerty);
        } else {
            set1stViewWidth(mCandidateWidth * mCandidate1stViewColumnNum);
        }
        setViewLayout(VIEW_TYPE_NORMAL);
        mInputView.getKeyboardView().setVisibility(View.VISIBLE);

        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        if (keyskin.isValid()){
            int top = 0;
            int bottom = 0;

            if (mLinearLayout1st.getBackground() == null) {
                top = mLinearLayout1st.getPaddingTop();
                bottom = mLinearLayout1st.getPaddingBottom();
                mLinearLayout1st.setBackground(keyskin.getKeyboardBg());
                mLinearLayout1st.setPadding(mLinearLayout1st.getPaddingLeft(), top, mLinearLayout1st.getPaddingRight(), bottom);
            }
            if (mLinearLayout2nd.getBackground() == null) {
                top = mLinearLayout2nd.getPaddingTop();
                bottom = mLinearLayout2nd.getPaddingBottom();
                mLinearLayout2nd.setBackground(keyskin.getKeyboardBg());
                mLinearLayout2nd.setPadding(mLinearLayout2nd.getPaddingLeft(), top, mLinearLayout2nd.getPaddingRight(), bottom);
            }
        }

        setReadMore();
    }

    /**
     * Set parameters depending on the display
     *
     * @param displayMode state of the display
     */
    public void setDisplayMode(int displayMode) {

        mScrollPageRowSize1st = new int[NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE];
        mScrollPageRowSize2nd = new int[NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE];

        switch(displayMode) {
        case DefaultSoftKeyboard.PORTRAIT:
            mCandidateEmojiMax = NUMBER_OF_DISPLAY_CANDIDATE - 1;
            mCandidateDefaultRowNum = NUMBER_OF_ROW_NUM_PORT_DEFAULT;
            mCandidate1stViewColumnNum = NUMBER_OF_1ST_VIEW_COLUMN_NUM_PORT;
            mScrollPageChangeNum1st = NUMBER_OF_1ST_VIEW_ROW_NUM_PORT;
            mCandidate2ndViewColumnNum = NUMBER_OF_2ND_VIEW_COLUMN_NUM_PORT;
            mScrollPageChangeNum2nd = NUMBER_OF_ROW_NUM_PORT_DEFAULT * NUMBER_OF_2ND_VIEW_COLUMN_NUM_PORT;
            mIndexMax1st = NUMBER_OF_DISPLAY_CANDIDATE - 1;
            mIndexMax2nd = NUMBER_OF_DISPLAY_CANDIDATE - 1;

            mScrollPageRowSize1st[0] = NUMBER_OF_1ST_VIEW_ROW_NUM_PORT;
            mScrollPageRowSize1st[1] = 0;
            mScrollPageRowSize1st[2] = 0;
            mScrollPageRowSize1st[3] = 0;
            mScrollPageRowSize2nd[0] = NUMBER_OF_ROW_NUM_PORT_DEFAULT;
            mScrollPageRowSize2nd[1] = NUMBER_OF_2ND_VIEW_ROW_NUM_PORT - NUMBER_OF_ROW_NUM_PORT_DEFAULT;
            break;

        case DefaultSoftKeyboard.LANDSCAPE:
        default:
            mCandidateEmojiMax = NUMBER_OF_DISPLAY_CANDIDATE - 1;
            mCandidateDefaultRowNum = NUMBER_OF_ROW_NUM_LAND_DEFAULT;
            mCandidate1stViewColumnNum = NUMBER_OF_1ST_VIEW_COLUMN_NUM_LAND;
            mCandidate1stViewColumnNumSplitQwerty = NUMBER_OF_1ST_VIEW_COLUMN_NUM_SPLIT_QWERTY;
            mScrollPageChangeNum1st = NUMBER_OF_ROW_NUM_LAND_DEFAULT * NUMBER_OF_1ST_VIEW_COLUMN_NUM_LAND;
            mScrollPageChangeNum1stSplitQwerty = NUMBER_OF_ROW_NUM_LAND_DEFAULT
                    * NUMBER_OF_1ST_VIEW_COLUMN_NUM_SPLIT_QWERTY;
            mCandidate2ndViewColumnNum = NUMBER_OF_2ND_VIEW_COLUMN_NUM_LAND;
            mScrollPageChangeNum2nd = NUMBER_OF_ROW_NUM_LAND_DEFAULT * NUMBER_OF_2ND_VIEW_COLUMN_NUM_LAND;
            mIndexMax1st = NUMBER_OF_DISPLAY_CANDIDATE;
            mIndexMax1stSplitQwerty = NUMBER_OF_DISPLAY_CANDIDATE - 1;
            mIndexMax2nd = NUMBER_OF_DISPLAY_CANDIDATE - 1;

            mScrollPageRowSize1st[0] = NUMBER_OF_ROW_NUM_LAND_DEFAULT;
            mScrollPageRowSize1st[1] = NUMBER_OF_ROW_NUM_LAND_DEFAULT;
            mScrollPageRowSize1st[2] = NUMBER_OF_ROW_NUM_LAND_DEFAULT;
            mScrollPageRowSize1st[3] = NUMBER_OF_1ST_VIEW_ROW_NUM_LAND - NUMBER_OF_ROW_NUM_LAND_DEFAULT * 3;

            mScrollPageRowSize1stSplitQwerty = new int[] {
                    NUMBER_OF_ROW_NUM_LAND_DEFAULT,
                    NUMBER_OF_ROW_NUM_LAND_DEFAULT,
                    NUMBER_OF_ROW_NUM_LAND_DEFAULT,
                    NUMBER_OF_ROW_NUM_LAND_DEFAULT,
                    NUMBER_OF_ROW_NUM_LAND_DEFAULT,
                    NUMBER_OF_ROW_NUM_LAND_DEFAULT,
                    NUMBER_OF_1ST_VIEW_ROW_NUM_LAND_SPLIT_QWERTY
                            - NUMBER_OF_ROW_NUM_LAND_DEFAULT * 6 };

            mScrollPageRowSize2nd[0] = NUMBER_OF_ROW_NUM_LAND_DEFAULT;
            mScrollPageRowSize2nd[1] = NUMBER_OF_2ND_VIEW_ROW_NUM_LAND - NUMBER_OF_ROW_NUM_LAND_DEFAULT;
            break;
        }

        setMixedDisplayIndex(displayMode);
    }

    /**
     * Get 1st view column number
     *
     * @param displayMode state of the display
     * @return 1st view column number
     */
    public int get1stViewColumnNum(int displayMode) {
        int num = 0;

        switch(displayMode) {
        case DefaultSoftKeyboard.PORTRAIT:
            num = NUMBER_OF_1ST_VIEW_COLUMN_NUM_PORT;
            break;

        case DefaultSoftKeyboard.LANDSCAPE:
        default:
            if (isSplitQwerty()) {
                num = NUMBER_OF_1ST_VIEW_COLUMN_NUM_SPLIT_QWERTY;
            } else {
                num = NUMBER_OF_1ST_VIEW_COLUMN_NUM_LAND;
            }
            break;

        }
        return num;
    }

    /**
     * Set candidates to 1st and 2nd view
     *
     * @param max1stView Max size of regist candidate for 1stView
     * @param max2ndView Max size of regist candidate for 2ndView
     * @param listener Event listener for touching a candidate
     * @param isRegistHistory Whether to register the history
     * @param flag Mode flag of setting candidate
     * @return regist Number of registered
     */
    private int setCandidateView(int max1stView, int max2ndView, OnClickListener listener,
            boolean isRegistHistory, int flag) {
        TextView textView;
        LinearLayout candidatesLayout;
        WnnWord word;
        int count = 0;
        OnClickListener registListener;
        boolean is1stLandStep1Last = false;

        int indexMax1st;
        if (isSplitQwerty()) {
            indexMax1st = mIndexMax1stSplitQwerty;
        } else {
            indexMax1st = mIndexMax1st;
        }

        while((word = mConverterSymbolEngine.getNextCandidate()) != null) {
            if ((m1stViewId >= mCandidateEmojiMax) && (m2ndViewId >= mCandidateEmojiMax)) {
                break;
            }

            if (IWnnSymbolEngine.isCategory(word) ||
                    mRegistCandidate.containsKey(word.candidate)) {
                continue;
            }
            if (!isRegistHistory && ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_HISTORY) != 0)) {
                continue;
            }
            word.attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_MUHENKAN;
            if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_DECOEMOJI) != 0) {
                word.attribute |= iWnnEngine.WNNWORD_ATTRIBUTE_NO_DICTIONARY;
                registListener = (listener != null) ? listener : mCandidateOnClickForDecoEmoji;
            } else {
                registListener = (listener != null) ? listener : mCandidateOnClickForEmoji;
            }
            mRegistCandidate.put(word.candidate, "");
            CharSequence candidate = DecoEmojiUtil.getSpannedCandidate(word);
            ImageSpan span = null;
            if (word.candidate.equals(" ")) {
                span = new ImageSpan(OpenWnn.superGetContext(), R.drawable.word_half_space,
                                     DynamicDrawableSpan.ALIGN_BASELINE);
            } else if (word.candidate.equals("\u3000" /* full-width space */)) {
                span = new ImageSpan(OpenWnn.superGetContext(), R.drawable.word_full_space,
                                     DynamicDrawableSpan.ALIGN_BASELINE);
            }
            if (span != null) {
                SpannableString spannable = new SpannableString("   ");
                spannable.setSpan(span, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                candidate = spannable;
            }

            int index1stView = 0;
            int index2ndView = 0;
            int page = 0;

            if (m1stViewCount <= max1stView) {
                switch (flag) {
                case MODE_ONETOUCHEMOJI_MIXED_DECOEMOJI:
                    index1stView = mMixedDecoEmojiIndexList1st.get(m1stViewCount - mMixedHistoryIndex1st);
                    if (index1stView == 0) {
                        // get next index because readmore button
                        m1stViewCount++;
                        index1stView = mMixedDecoEmojiIndexList1st.get(m1stViewCount - mMixedHistoryIndex1st);
                    }
                    index1stView = index1stView + mMixedHistoryIndex1st + (mMixedHistoryIndex1st == 0 ? 0 : (- 1));
                    break;
                case MODE_ONETOUCHEMOJI_MIXED_EMOJI:
                    index1stView = mMixedEmojiIndexList1st.get(m1stViewCount - mMixedDecoEmojiIndex1st);
                    index1stView = index1stView + mMixedHistoryIndex1st + (mMixedHistoryIndex1st == 0 ? 0 : (- 1));
                    break;
                case MODE_ONETOUCHEMOJI_UNMIXED:
                default:
                    index1stView = m1stViewCount;
                    break;
                }

                //set 1stView candidate
                if ((index1stView <= indexMax1st) && (!((is1stLandStep1Last) && (index1stView == indexMax1st)))) {
                    mWnnWordArray1st.add(word);
                    if (m1stViewCount == 0) {
                        if (isSplitQwerty()) {
                            candidatesLayout = (LinearLayout)mScrollChildLayout1stSplitQwerty[0]
                                    .getChildAt(m1stViewReadMoreIndex
                                            / (mScrollPageRowSize1stSplitQwerty[0]));
                        } else {
                            candidatesLayout = (LinearLayout)mScrollChildLayout1st[0]
                                    .getChildAt(m1stViewReadMoreIndex / (mScrollPageRowSize1st[0]));
                        }
                        if (candidatesLayout != null) {
                            if (isSplitQwerty()) {
                                textView = (TextView)candidatesLayout
                                        .getChildAt(m1stViewReadMoreIndex
                                                % (mScrollPageRowSize1stSplitQwerty[0]));
                            } else {
                                textView = (TextView)candidatesLayout
                                        .getChildAt(m1stViewReadMoreIndex
                                                % (mScrollPageRowSize1st[0]));
                            }
                            if (textView != null) {
                                textView.setText("");
                                textView.setOnClickListener(null);
                                textView.setClickable(false);
                            }
                        }
                        m1stViewCount++;

                        if (flag == MODE_ONETOUCHEMOJI_MIXED_DECOEMOJI) {
                            index1stView = mMixedDecoEmojiIndexList1st.get(m1stViewCount - mMixedHistoryIndex1st);
                            index1stView = index1stView + mMixedHistoryIndex1st + (mMixedHistoryIndex1st == 0 ? 0 : (- 1));
                        } else {
                            index1stView = m1stViewCount;
                        }
                    }

                    //set special index for the last candidate of 1stView landscape step1
                    if ((indexMax1st > mCandidateEmojiMax) && (index1stView == mCandidateEmojiMax)) {
                        index1stView = indexMax1st;
                        is1stLandStep1Last = true;
                    }

                    if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_RIGHT.equals(mListMode)) {
                        index1stView = changeIndexForRightMode(index1stView, true);
                    }

                    if (isSplitQwerty()) {
                        page = index1stView / mScrollPageChangeNum1stSplitQwerty;
                        //get index value of childlayout
                        index1stView = index1stView % mScrollPageChangeNum1stSplitQwerty;
                        candidatesLayout = (LinearLayout)mScrollChildLayout1stSplitQwerty[page]
                                .getChildAt(index1stView / (mScrollPageRowSize1stSplitQwerty[page]));
                    } else {
                        page = index1stView / mScrollPageChangeNum1st;
                        //get index value of childlayout
                        index1stView = index1stView % mScrollPageChangeNum1st;
                        candidatesLayout = (LinearLayout)mScrollChildLayout1st[page]
                                .getChildAt(index1stView / (mScrollPageRowSize1st[page]));
                    }

                    if (candidatesLayout != null) {
                        if (isSplitQwerty()) {
                            textView = (TextView) candidatesLayout
                                    .getChildAt(index1stView % (mScrollPageRowSize1stSplitQwerty[page]));
                        } else {
                            textView = (TextView) candidatesLayout
                                    .getChildAt(index1stView % (mScrollPageRowSize1st[page]));
                        }
                        if (textView != null) {
                            textView.setText(candidate);
                            textView.setId(m1stViewId);
                            textView.setOnClickListener(registListener);
                            textView.setClickable(true);
                        }
                    }
                    m1stViewId++;
                    m1stViewCount++;
                }
            }

            if (m2ndViewCount <= max2ndView) {
                switch (flag) {
                case MODE_ONETOUCHEMOJI_MIXED_DECOEMOJI:
                    index2ndView = mMixedDecoEmojiIndexList2nd.get(m2ndViewCount - mMixedHistoryIndex2nd);
                    if (index2ndView == 0) {
                        // get next index because readmore button
                        m2ndViewCount++;
                        index2ndView = mMixedDecoEmojiIndexList2nd.get(m2ndViewCount - mMixedHistoryIndex2nd);
                    }
                    index2ndView = index2ndView + mMixedHistoryIndex2nd + (mMixedHistoryIndex2nd == 0 ? 0 : (- 1));
                    break;
                case MODE_ONETOUCHEMOJI_MIXED_EMOJI:
                    index2ndView = mMixedEmojiIndexList2nd.get(m2ndViewCount - mMixedDecoEmojiIndex2nd);
                    index2ndView = index2ndView + mMixedHistoryIndex2nd + (mMixedHistoryIndex2nd == 0 ? 0 : (- 1));
                    break;
                case MODE_ONETOUCHEMOJI_UNMIXED:
                default:
                    index2ndView = m2ndViewCount;
                    break;
                }

                //set 2ndView candidate
                if (index2ndView <= mIndexMax2nd) {
                    mWnnWordArray2nd.add(word);
                    if (((m2ndViewCount == 0) && !WnnKeyboardFactory.getSplitMode())
                            || ((m2ndViewCount == m2ndViewReadMoreIndex)
                                    && WnnKeyboardFactory.getSplitMode())) {
                        candidatesLayout = (LinearLayout)mScrollChildLayout2nd[0]
                                .getChildAt(m2ndViewReadMoreIndex / (mScrollPageRowSize2nd[0]));
                        if (candidatesLayout != null) {
                            textView = (TextView)candidatesLayout
                                    .getChildAt(m2ndViewReadMoreIndex % (mScrollPageRowSize2nd[0]));
                            if (textView != null) {
                                textView.setText("");
                                textView.setOnClickListener(null);
                                textView.setClickable(false);
                            }
                        }
                        m2ndViewCount++;
                        count++;

                        if (flag == MODE_ONETOUCHEMOJI_MIXED_DECOEMOJI) {
                            index2ndView = mMixedDecoEmojiIndexList2nd.get(m2ndViewCount - mMixedHistoryIndex2nd);
                            index2ndView = index2ndView + mMixedHistoryIndex2nd + (mMixedHistoryIndex2nd == 0 ? 0 : (- 1));
                        } else {
                            index2ndView = m2ndViewCount;
                        }
                    }

                    if (DefaultSoftKeyboardJAJP.ONETOUCHEMOJILIST_RIGHT.equals(mListMode)) {
                        index2ndView = changeIndexForRightMode(index2ndView, false);
                    }

                    page = index2ndView / mScrollPageChangeNum2nd;
                    //get index value of childlayout
                    index2ndView = index2ndView % mScrollPageChangeNum2nd;

                    candidatesLayout = (LinearLayout) mScrollChildLayout2nd[page]
                            .getChildAt(index2ndView / (mScrollPageRowSize2nd[page]));
                    if (candidatesLayout != null) {
                        textView = (TextView) candidatesLayout
                                .getChildAt(index2ndView % (mScrollPageRowSize2nd[page]));
                        if (textView != null) {
                            textView.setText(candidate);
                            textView.setId(m2ndViewId);
                            textView.setOnClickListener(registListener);
                            textView.setClickable(true);
                        }
                    }
                    m2ndViewId++;
                    m2ndViewCount++;
                }
            }
            count++;
        }
        return count;
    }

    /**
     * Handle a back key event.
     *
     * @return {@code true}:True when event is digested; {@code false}:if not
     */
    public boolean processBackKeyEvent() {
        boolean ret = false;
        if (mIsFullView) {
            setViewLayout(VIEW_TYPE_NORMAL);
            mInputView.getKeyboardView().setVisibility(View.VISIBLE);
            setReadMore();
            ret = true;
        }
        return ret;
    }

    /**
     * playSoundAndVibration.
     * <br>
     * This method notices the playSoundAndVibration.
     *
     */
    private void playSoundAndVibration() {
        if (mVibrator != null) {
            try {
                int vibrateTime = mWnn.getResources().getInteger(R.integer.vibrate_time);
                mVibrator.vibrate(vibrateTime);
            } catch (Exception ex) {
                Log.e("iwnn", "OneTouchEmojiListViewManager::playSoundAndVibration Vibrator " + ex.toString());
            }
        }
        if (mSound) {
            SoundManager.getInstance().play(SOUND_STANDARD_KEY);
        }
    }

    /**
     * clearCandidateView.
     *
     * This method clear settings for EmojiQuick candidate View.
     *
     */
    private void clearCandidateView() {
        TextView textView;
        LinearLayout candidatesLayout;

        for (int k = 0; k < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE; k++) {
            if (mScrollChildLayout1st[k] != null) {
                for (int i = 0; i < mScrollChildLayout1st[k].getChildCount(); i++) {
                    candidatesLayout = (LinearLayout)mScrollChildLayout1st[k].getChildAt(i);
                    for (int j = 0; j <  candidatesLayout.getChildCount(); j++) {
                        textView = (TextView)candidatesLayout.getChildAt(j);
                        textView.setText("");
                        textView.setOnClickListener(null);
                        textView.setClickable(false);
                    }
                }
            }
        }

        for (int k = 0; k < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY; k++) {
            if (mScrollChildLayout1stSplitQwerty[k] != null) {
                for (int i = 0; i < mScrollChildLayout1stSplitQwerty[k].getChildCount(); i++) {
                    candidatesLayout = (LinearLayout) mScrollChildLayout1stSplitQwerty[k]
                            .getChildAt(i);
                    for (int j = 0; j <  candidatesLayout.getChildCount(); j++) {
                        textView = (TextView)candidatesLayout.getChildAt(j);
                        textView.setText("");
                        textView.setOnClickListener(null);
                        textView.setClickable(false);
                    }
                }
            }
        }

        for (int k = 0; k < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; k++) {
            if (mScrollChildLayout2nd[k] != null) {
                for (int i = 0; i < mScrollChildLayout2nd[k].getChildCount(); i++) {
                    candidatesLayout = (LinearLayout)mScrollChildLayout2nd[k].getChildAt(i);
                    for (int j = 0; j <  candidatesLayout.getChildCount(); j++) {
                        textView = (TextView)candidatesLayout.getChildAt(j);
                        textView.setText("");
                        textView.setOnClickListener(null);
                        textView.setClickable(false);
                    }
                }
            }
        }
    }

    /**
     * close.
     */
    public void close() {
        if (mConverterSymbolEngine != null) {
            mConverterSymbolEngine.close();
        }
        processBackKeyEvent();
    }

    /**
     * Remove all view group.
     *
     * @param viewGroup target ViewGroup.
     */
    private void removeAllViewRecursive(ViewGroup viewGroup) {
        int conut = viewGroup.getChildCount();
        for (int i = 0; i < conut; i++) {
            View view = viewGroup.getChildAt(i);
            unbindView(view);
            if (view instanceof ViewGroup) {
                removeAllViewRecursive((ViewGroup) view);
            }
        }
        viewGroup.removeAllViews();
    }

    /**
     * Unbind view.
     *
     * @param view target view.
     */
    private void unbindView(View view) {
        // set all listeners to null
        view.setOnLongClickListener(null);
        view.setOnClickListener(null);
        view.setOnTouchListener(null);

        // set background to null
        view.setBackgroundDrawable(null);
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.setImageDrawable(null);
        }
    }

    /**
     * Set the 1st view width.
     *
     * @param width width of 1st view.
     */
    private void set1stViewWidth(int width) {
        if (isSplitQwerty()) {
            ViewGroup.LayoutParams param = mViewCandidateList1stSplitQwerty.getLayoutParams();
            param.width = width;
            mViewCandidateList1stSplitQwerty.setLayoutParams(param);
        } else {
            ViewGroup.LayoutParams param = mViewCandidateList1st.getLayoutParams();
            param.width = width;
            mViewCandidateList1st.setLayoutParams(param);
        }
    }

    /**
     * Set the 1st view base width.
     *
     * @param width width of 1st view base.
     */
    public void set1stBaseWidth(int width) {
        mViewBodyWidth = width;
    }

    /**
     * Set the 2nd view base width.
     *
     * @param width width of 1st view base.
     */
    public void set2ndBaseWidth(int width) {
        mViewBody2ndWidth = width;
    }

    /**
     * Set index for DecoEmoji/Emoji mixed mode display depending on the display state
     *
     * @param displayMode state of the display
     */
    private void setMixedDisplayIndex(int displayMode) {
        mMixedDecoEmojiIndexList1st.clear();
        mMixedEmojiIndexList1st.clear();
        mMixedDecoEmojiIndexList2nd.clear();
        mMixedEmojiIndexList2nd.clear();

        int tempIndex1 = 0;
        int tempIndex2 = 0;

        switch(displayMode) {
        case DefaultSoftKeyboard.PORTRAIT:
            //DecoEmoji
            tempIndex1 = mScrollPageChangeNum2nd;
            for (int page = 0; page < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; page++) {
                tempIndex2 = mScrollPageRowSize2nd[page] * 2;
                for (int i = 0; i < (mCandidate2ndViewColumnNum / 2); i++) {
                    for (int j = 0; j < mScrollPageRowSize2nd[page]; j++) {
                        mMixedDecoEmojiIndexList1st.add((j + tempIndex1 * page) + (i * tempIndex2));
                        mMixedDecoEmojiIndexList2nd.add((j + tempIndex1 * page) + (i * tempIndex2));
                    }
                }
            }

            //Emoji
            for (int page = 0; page < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; page++) {
                tempIndex1 = mScrollPageRowSize2nd[page] + mScrollPageChangeNum2nd * page;
                tempIndex2 = mScrollPageRowSize2nd[page] * 2;
                for (int i = 0; i < (mCandidate2ndViewColumnNum / 2); i++) {
                    for (int j = 0; j < mScrollPageRowSize2nd[page]; j++) {
                        mMixedEmojiIndexList1st.add((j + tempIndex1) + (i * tempIndex2));
                        mMixedEmojiIndexList2nd.add((j + tempIndex1) + (i * tempIndex2));
                    }
                }
            }
            break;

        case DefaultSoftKeyboard.LANDSCAPE:
        default:
            int maxPage1st = 0;
            int scrollPageChangeNum1st = 0;
            int candidate1stViewColumnNum = 0;
            int[] scrollPageRowSize1st;
            if (isSplitQwerty()) {
                maxPage1st = NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY;
                scrollPageChangeNum1st = mScrollPageChangeNum1stSplitQwerty;
                candidate1stViewColumnNum = mCandidate1stViewColumnNumSplitQwerty;
                scrollPageRowSize1st = mScrollPageRowSize1stSplitQwerty;
            } else {
                maxPage1st = NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE;
                scrollPageChangeNum1st = mScrollPageChangeNum1st;
                candidate1stViewColumnNum = mCandidate1stViewColumnNum;
                scrollPageRowSize1st = mScrollPageRowSize1st;
            }

            //1stView DecoEmoji
            tempIndex2 = NUMBER_OF_ROW_NUM_LAND_DEFAULT * 2;
            for (int page = 0; page < maxPage1st; page++) {
                tempIndex1 = scrollPageChangeNum1st * page;
                if (isSplitQwerty()) {
                    for (int i = 0; i < (candidate1stViewColumnNum / 2); i++) {
                        for (int j = 0; j < scrollPageRowSize1st[page]; j++) {
                            mMixedDecoEmojiIndexList1st
                                    .add((j + tempIndex1)
                                            + (i * tempIndex2));
                        }
                    }
                } else {
                    if (page != (maxPage1st - 1)) {
                        // set index except last page
                        for (int i = 0; i < (candidate1stViewColumnNum / 2); i++) {
                            for (int j = 0; j < scrollPageRowSize1st[page]; j++) {
                                mMixedDecoEmojiIndexList1st
                                        .add((j + tempIndex1)
                                                + (i * tempIndex2));
                            }
                        }
                    } else {
                        // set index of the last page individually because its
                        // rule is special
                        for (int j = 0; j < (candidate1stViewColumnNum / 2); j++) {
                            mMixedDecoEmojiIndexList1st.add(j + tempIndex1);
                        }
                        mMixedDecoEmojiIndexList1st.add(mCandidateEmojiMax - 1);
                    }
                }
            }

            //1stView Emoji
            for (int page = 0; page < maxPage1st; page++) {
                tempIndex1 = scrollPageRowSize1st[page]
                        + scrollPageChangeNum1st * page;
                if (isSplitQwerty()) {
                    for (int i = 0; i < (candidate1stViewColumnNum / 2); i++) {
                        for (int j = 0; j < scrollPageRowSize1st[page]; j++) {
                            mMixedEmojiIndexList1st.add((j + tempIndex1)
                                    + (i * tempIndex2));
                        }
                    }
                } else {
                    if (page != (maxPage1st - 1)) {
                        // set index except last page
                        for (int i = 0; i < (candidate1stViewColumnNum / 2); i++) {
                            for (int j = 0; j < scrollPageRowSize1st[page]; j++) {
                                mMixedEmojiIndexList1st.add((j + tempIndex1)
                                        + (i * tempIndex2));
                            }
                        }
                    } else {
                        // set index of the last page individually because its
                        // rule is special
                        for (int j = 0; j <= 1; j++) {
                            mMixedEmojiIndexList1st.add(j + tempIndex1);
                        }
                        mMixedEmojiIndexList1st.add(mCandidateEmojiMax + 1);
                    }
                }
            }

            //2ndView DecoEmoji
            for (int page = 0; page < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; page++) {
                tempIndex1 = mScrollPageChangeNum2nd * page;
                tempIndex2 = mScrollPageRowSize2nd[page] * 2;
                if (page != (NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE - 1)) {
                    //set index except last page
                    for (int i = 0; i < ((mCandidate2ndViewColumnNum + 1) / 2); i++) {
                        for (int j = 0; j < mScrollPageRowSize2nd[page]; j++) {
                            mMixedDecoEmojiIndexList2nd.add((j + tempIndex1) + i * tempIndex2);
                        }
                    }
                } else {
                    //set index of the last page individually because its rule is special
                    for (int i = 0; i < ((mCandidate2ndViewColumnNum + 1) / 4); i++) {
                        for (int j = 0; j < mScrollPageRowSize2nd[page]; j++) {
                            mMixedDecoEmojiIndexList2nd.add((j + tempIndex1) + i * tempIndex2);
                        }
                    }
                }
            }


            //2ndView Emoji
            for (int page = 0; page < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; page++) {
                tempIndex1 = mScrollPageRowSize2nd[page] + mScrollPageChangeNum2nd * page;
                tempIndex2 = mScrollPageRowSize2nd[page] * 2;
                if (page != (NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE - 1)) {
                    //set index except last page
                    for (int i = 0; i < (mCandidate2ndViewColumnNum / 2); i++) {
                        for (int j = 0; j < mScrollPageRowSize2nd[page]; j++) {
                            mMixedEmojiIndexList2nd.add((j + tempIndex1) + (i * tempIndex2));
                        }
                    }
                } else {
                    //set index of the last page individually because its rule is special
                    for (int i = 0; i < ((mCandidate2ndViewColumnNum + 1) / 4); i++) {
                        for (int j = 0; j < mScrollPageRowSize2nd[page]; j++) {
                            mMixedEmojiIndexList2nd.add((j + tempIndex1) + (i * tempIndex2));
                        }
                    }
                    tempIndex1 = ((mCandidate2ndViewColumnNum + 1) / 2) + mScrollPageChangeNum2nd * 1;
                    for (int i = 0; i < 5; i++) {
                        mMixedEmojiIndexList2nd.add(tempIndex1 + i);
                    }
                }
            }
            break;
        }
    }

    /**
     * Set scroll view layout
     *
     * @param displayMode state of the display
     */
    private void setScrollViewLayout(int displayMode) {
        LinearLayout columnLayout;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

        for (int i = 0; i < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE; i++) {
            mScrollChildLayout1st[i] = new LinearLayout(mViewBody.getContext());
            mScrollChildLayout1st[i].setOrientation(LinearLayout.HORIZONTAL);
        }

        for (int i = 0; i < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY; i++) {
            mScrollChildLayout1stSplitQwerty[i] = new LinearLayout(mViewBody.getContext());
            mScrollChildLayout1stSplitQwerty[i].setOrientation(LinearLayout.HORIZONTAL);
        }

        for (int i = 0; i < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; i++) {
            mScrollChildLayout2nd[i] = new LinearLayout(mViewBody.getContext());
            mScrollChildLayout2nd[i].setOrientation(LinearLayout.HORIZONTAL);
        }

        switch(displayMode) {
        case DefaultSoftKeyboard.PORTRAIT:
            //set 1stView childlayout(1 page only)
            for (int i = 0; i < mCandidate1stViewColumnNum; i++) {
                columnLayout = new LinearLayout(mViewBody.getContext());
                columnLayout.setOrientation(LinearLayout.VERTICAL);
                mScrollChildLayout1st[0].addView(columnLayout, params);
                for (int j = 0; j < mScrollPageRowSize1st[0]; j++) {
                    TextView text = createCandidateView(VIEW_TYPE_NORMAL);
                    EmojiAssist.getInstance().addView(text);
                    columnLayout.addView(text, params);
                }
            }
            //set 1stView layout
            mViewCandidateList1st.addView(mScrollChildLayout1st[0], params);

           //set 2ndView childlayout
            for (int page = 0; page < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; page++) {
                for (int i = 0; i < mCandidate2ndViewColumnNum; i++) {
                    columnLayout = new LinearLayout(mViewBody.getContext());
                    columnLayout.setOrientation(LinearLayout.VERTICAL);
                    mScrollChildLayout2nd[page].addView(columnLayout, params);
                    for (int j = 0; j < mScrollPageRowSize2nd[page]; j++) {
                        TextView text = createCandidateView(VIEW_TYPE_FULL);
                        EmojiAssist.getInstance().addView(text);
                        columnLayout.addView(text, params);
                    }
                }
            }

           //set 2ndView layout
            for (int i = 0; i < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; i++) {
                mViewCandidateList2nd.addView(mScrollChildLayout2nd[i], params);
            }

            break;
        case DefaultSoftKeyboard.LANDSCAPE:
        default:
            //set 1stView childlayout
            for (int k = 0; k < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE; k++) {
                for (int i = 0; i < mCandidate1stViewColumnNum; i++) {
                    columnLayout = new LinearLayout(mViewBody.getContext());
                    columnLayout.setOrientation(LinearLayout.VERTICAL);
                    mScrollChildLayout1st[k].addView(columnLayout, params);
                    for (int j = 0; j < mScrollPageRowSize1st[k]; j++) {
                        TextView text = createCandidateView(VIEW_TYPE_NORMAL);
                        EmojiAssist.getInstance().addView(text);
                        columnLayout.addView(text, params);
                    }
                }
            }

            //set 1stView layout
            for (int i = 0; i < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE; i++) {
               mViewCandidateList1st.addView(mScrollChildLayout1st[i], params);
            }

            //set 1stView childlayout when qwerty split mode
            for (int k = 0; k < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY; k++) {
                for (int i = 0; i < mCandidate1stViewColumnNumSplitQwerty; i++) {
                    columnLayout = new LinearLayout(mViewBody.getContext());
                    columnLayout.setOrientation(LinearLayout.VERTICAL);
                    mScrollChildLayout1stSplitQwerty[k].addView(columnLayout, params);
                    for (int j = 0; j < mScrollPageRowSize1stSplitQwerty[k]; j++) {
                        TextView text = createCandidateView(VIEW_TYPE_NORMAL);
                        EmojiAssist.getInstance().addView(text);
                        columnLayout.addView(text, params);
                    }
                }
            }

            //set 1stView layout when qwerty split mode
            for (int i = 0; i < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY; i++) {
                mViewCandidateList1stSplitQwerty.addView(
                        mScrollChildLayout1stSplitQwerty[i], params);
            }

            //set 2ndView childlayout
            for (int k = 0; k < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; k++) {
                for (int i = 0; i < mCandidate2ndViewColumnNum; i++) {
                    columnLayout = new LinearLayout(mViewBody.getContext());
                    columnLayout.setOrientation(LinearLayout.VERTICAL);
                    mScrollChildLayout2nd[k].addView(columnLayout, params);
                    for (int j = 0; j < mScrollPageRowSize2nd[k]; j++) {
                        TextView text = createCandidateView(VIEW_TYPE_FULL);
                        EmojiAssist.getInstance().addView(text);
                        columnLayout.addView(text, params);
                    }
                }
            }

            //set 2ndView layout
            for (int i = 0; i < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; i++) {
                mViewCandidateList2nd.addView(mScrollChildLayout2nd[i], params);
            }

            break;
        }
        mInitFlg = true;
    }

    /**
     * Initialize scroll view layout
     *
     */
    private void initScrollViewLayout() {
        for (int k = 0; k < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE; k++) {
            if (mScrollChildLayout1st[k] != null) {
                for (int i = 0; i < mScrollChildLayout1st[k].getChildCount(); i++) {
                    LinearLayout candidatesLayout = (LinearLayout)mScrollChildLayout1st[k].getChildAt(i);
                    for (int j = 0; j <  candidatesLayout.getChildCount(); j++) {
                        TextView text = (TextView)candidatesLayout.getChildAt(j);
                        EmojiAssist.getInstance().removeView(text);
                    }
                }
            }
        }

        for (int k = 0; k < NUMBER_OF_1ST_VIEW_SCROLL_MAX_PAGE_SPLIT_QWERTY; k++) {
            if (mScrollChildLayout1stSplitQwerty[k] != null) {
                for (int i = 0; i < mScrollChildLayout1stSplitQwerty[k].getChildCount(); i++) {
                    LinearLayout candidatesLayout
                        = (LinearLayout)mScrollChildLayout1stSplitQwerty[k].getChildAt(i);
                    for (int j = 0; j <  candidatesLayout.getChildCount(); j++) {
                        TextView text = (TextView)candidatesLayout.getChildAt(j);
                        EmojiAssist.getInstance().removeView(text);
                    }
                }
            }
        }

        for (int k = 0; k < NUMBER_OF_2ND_VIEW_SCROLL_MAX_PAGE; k++) {
            if (mScrollChildLayout2nd[k] != null) {
                for (int i = 0; i < mScrollChildLayout2nd[k].getChildCount(); i++) {
                    LinearLayout candidatesLayout = (LinearLayout)mScrollChildLayout2nd[k].getChildAt(i);
                    for (int j = 0; j <  candidatesLayout.getChildCount(); j++) {
                        TextView text = (TextView)candidatesLayout.getChildAt(j);
                        EmojiAssist.getInstance().removeView(text);
                    }
                }
            }
        }
    }

    /**
     * Change the index value for right display mode
     *
     * @param index Index before change
     * @param is1stView    {@code true}  - 1stView.
     *                     {@code false} - 2ndView.
     * @return Index after change
     */
    private int changeIndexForRightMode(int index, boolean is1stView) {
        int indexForRightMode = 0;
        int pageRowSize = 0;
        int columnSize = 0;
        int pageIndex = 0;
        int columnNum = 0;

        if (is1stView) {
            pageIndex = index / mScrollPageChangeNum1st;
            pageRowSize = mScrollPageRowSize1st[pageIndex];
            columnSize = mCandidate1stViewColumnNum;
            columnNum = (index % mScrollPageChangeNum1st) / pageRowSize + 1;
        } else {
            pageIndex = index / mScrollPageChangeNum2nd;
            pageRowSize = mScrollPageRowSize2nd[pageIndex];
            columnSize = mCandidate2ndViewColumnNum;
            columnNum = (index % mScrollPageChangeNum2nd) / pageRowSize + 1;
        }
        indexForRightMode = index + pageRowSize * (columnSize - columnNum * 2 + 1);

        return indexForRightMode;
    }

    /**
     * Check whether qwerty split mode.
     */
    private boolean isSplitQwerty() {
        return WnnKeyboardFactory.getSplitMode()
                && (mInputView.mCurrentKeyboardType == DefaultSoftKeyboard.KEYBOARD_QWERTY);
       }
}
