/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.inputmethodservice.ExtractEditText;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.text.TextUtils;
import android.text.TextPaint;
import android.preference.PreferenceManager;
import android.text.Annotation;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.Toast;

import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.DecoEmojiContract;
import jp.co.omronsoft.android.decoemojimanager_docomo.interfacedata.IDecoEmojiManager;
import jp.co.omronsoft.android.emoji.EmojiAssist;
import jp.co.omronsoft.android.text.EmojiDrawable;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;

import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;
import jp.co.omronsoft.iwnnime.ml.iwnn.IWnnSymbolEngine;
import jp.co.omronsoft.iwnnime.ml.standardcommon.IWnnLanguageSwitcher;

import java.util.ArrayList;
import java.util.LinkedList;

import com.lge.handwritingime.BaseHandwritingKeyboard;

/**
 * The default candidates view manager class using {@link android.widget.EditText}.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class TextCandidatesViewManager extends CandidatesViewManager implements GestureDetector.OnGestureListener {
    /** Number of lines to display (Portrait) */
    public static final int LINE_NUM_PORTRAIT       = 2;
    /** Number of lines to display (Landscape) */
    public static final int LINE_NUM_LANDSCAPE      = 1;
    /** Number of history lines to display */
    public static final int LINE_NUM_HISTORY        = 2;

    /** Maximum lines */
    private static final int DISPLAY_LINE_MAX_COUNT = 500;
    /** Maximum number of displaying candidates par one line (full view mode) */
    private static final int FULL_VIEW_DIV = 5;
    /** Maximum number of displaying candidates par one line (full view mode)(symbol)(portrait) */
    private static final int FULL_VIEW_SYMBOL_DIV_PORT = 6;
    /** Maximum number of displaying candidates par one line (full view mode)(symbol)(landscape) */
    private static final int FULL_VIEW_SYMBOL_DIV_LAND = 10;
    /** Maximum number of displaying candidates par one line (full view mode)(symbol)(portrait) */
    private static final int FULL_VIEW_EMOJI_DIV_PORT = 4;
    /** Maximum number of displaying candidates par one line (full view mode)(symbol)(landscape) */
    private static final int FULL_VIEW_EMOJI_DIV_LAND = 8;
    /** Delay of set candidate */
    private static final int SET_CANDIDATE_DELAY = 160;
    private static final int SET_CANDIDATE_DELAY_PART1 = 60;
    private static final int SET_CANDIDATE_DELAY_PART2 = 40;
    /** Delay of set DECO candidate */
    private static final int SET_CANDIDATE_DELAY_DECO = 50;
    /** First line count */
    private static final int SET_CANDIDATE_LINE_COUNT = 1;
    private static final int SET_CANDIDATE_LINE_COUNT_PART1 = 24;
    private static final int SET_CANDIDATE_LINE_COUNT_PART2 = 48;
    /** Delay line count */
    private static final int SET_CANDIDATE_DELAY_LINE_COUNT = 1;
    /** Delay of more display candidate */
    private static final int MORE_DISPLAY_CANDIDATE_DELAY = 500;

    /** Focus is none now */
    private static final int FOCUS_NONE = -1;
    /** Handler for focus Candidate */
    private static final int MSG_MOVE_FOCUS = 0;
    /** Handler for set  Candidate */
    private static final int MSG_SET_CANDIDATES = 1;
    /** Handler for select Candidate */
    private static final int MSG_SELECT_CANDIDATES = 2;
    /** Handler for more display candidate */
    private static final int MSG_MORE_DISPLAY_CANDIDATES = 3;

    /** NUmber of Candidate display lines */
    private static final int SETTING_NUMBER_OF_LINEMAX = 5;

    /** Default value of keyboard line */
    private static final int DEFAULT_VALUE_KEYLINE = 4;

    /** Key of "key_height_portrait" setting. */
    private static final String KEY_HEIGHT_PORTRAIT_KEY = "key_height_portrait";
    /** Key of "key_height_landscape" setting. */
    private static final String KEY_HEIGHT_LANDSCAPE_KEY = "key_height_landscape";
    /** Key of "key_height_portrait" setting. */
    private static final String KEY_HEIGHT_PORTRAIT_SYMBOL_KEY = "key_height_portrait_symbol";
    /** Key of "key_height_landscape" setting. */
    private static final String KEY_HEIGHT_LANDSCAPE_SYMBOL_KEY = "key_height_landscape_symbol";

    /** Special value for Move to home */
    private static final int DIRECTION_TO_HOME = 999;
    /** Special value for Move to end */
    private static final int DIRECTION_TO_END = -999;

    /** Body view of the candidates list */
    private ViewGroup  mViewBody = null;

    /** Scroller of {@link #mViewTabBase} */
    private HorizontalScrollView mViewTabScroll;
    /** Base of the Tab */
    private ViewGroup mViewTabBase;

    /** The view of the Pictgram Tab */
    private TextView mViewTabPictgram;
    /** The view of the Unicode6 Pictgram Tab */
    private TextView mViewTabPictgram_uni6;
    /** The view of the Symbol Tab */
    private TextView mViewTabSymbol;
    /** The view of the Emoticon Tab */
    private TextView mViewTabEmoticon;

    /** The view of the DecoEmoji Tab */
    private TextView mViewTabDecoEmoji;

    /** Scroller of {@link #mViewCandidateBase} */
    private ScrollView mViewBodyScroll;
    /** Base of {@link #mViewCandidateList1st}, {@link #mViewCandidateList2nd} */
    private ViewGroup mViewCandidateBase;
    /** Background view of candidate */
    private LinearLayout mViewCandidateBaseBg;
    /** Button displayed bottom of the view when there are more candidates. */
    private ImageView mReadMoreButton;
    /** Layout for the candidates list on normal view */
    private LinearLayout mViewCandidateList1st;
    /** Layout for the candidates list on full view */
    private AbsoluteLayout mViewCandidateList2nd;
    /** View when candidates are nothing */
    private View mViewCandidateNothing;
    /** View when candidates of blank area */
    private View mViewCandidateBlankArea;
    /** View for symbol tab */
    private LinearLayout mViewCandidateListTab;
    /** View type (VIEW_TYPE_NORMAL or VIEW_TYPE_FULL or VIEW_TYPE_CLOSE) */
    private int mViewType;
    /** Portrait display({@code true}) or landscape({@code false}) */
    private boolean mPortrait;

    /** Width of the view */
    private int mViewWidth;
    /** Minimum width of the candidate view */
    private int mCandidateMinimumWidth;
    /** Minimum height of the candidate view */
    private int mCandidateMinimumHeight;
    /** Minimum height of the candidate view */
    private int mCandidateSymbolMinimumHeight;
    /** Minimum height of the category candidate view */
    private int mCandidateCategoryMinimumHeight;
    /** Left align threshhold of the candidate view */
    private int mCandidateLeftAlignThreshold;
    /** Top padding of the candidate list background view */
    private int mCandidateBgViewPaddingTop;
    /** Bottom padding of the candidate list background view */
    private int mCandidateBgViewPaddingBottom;
    /** Height of keyboard */
    private int mKeyboardHeight;
    /** Height of symbol keyboard */
    private int mSymbolKeyboardHeight;
    /** Height of symbol keyboard tab */
    private int mSymbolKeyboardTabHeight;
    /** Whether being able to use Emoji */
    private boolean mEnableEmoji = false;

    /** Whether being able to use DecoEmoji */
    private boolean mEnableDecoEmoji = false;

    /** Whether being able to use Emoticon */
    private boolean mEnableEmoticon = false;

    /** Whether being able to use EmojiUNI6 */
    private boolean mEnableEmojiUNI6 = false;

    /** Whether hide the view if there is no candidate */
    private boolean mAutoHideMode = true;
    /** The converter to get candidates from and notice the selected candidate to. */
    protected WnnEngine mConverter;
    /** Limitation of displaying candidates */
    private int mDisplayLimit;

    /** Vibrator for touch vibration */
    private Vibrator mVibrator = null;
    /** SoundManager for click sound */
    private boolean mSound = false;

    /** Number of candidates displaying for 1st */
    private int mWordCount1st;
    /** Number of candidates displaying for 2nd */
    private int mWordCount2nd;
    /** List of candidates for 1st */
    private ArrayList<WnnWord> mWnnWordArray1st = new ArrayList<WnnWord>();
    /** List of candidates for 2nd */
    private ArrayList<WnnWord> mWnnWordArray2nd = new ArrayList<WnnWord>();
    /** List of select candidates */
    private LinkedList<WnnWord> mWnnWordSelectedList = new LinkedList<WnnWord>();

    /** Character width of the candidate area */
    private int mLineLength = 0;
    /** Number of lines displayed */
    private int mLineCount = 1;

    /** {@code true} if the full screen mode is selected */
    private boolean mIsFullView = false;

    /** The offset when the candidate is flowed out the candidate window */
    private int mDisplayEndOffset = 0;
    /** {@code true} if there are more candidates to display. */
    private boolean mCanReadMore = false;
    /** Color of the candidates */
    private int mTextColor = 0;
    /** Template object for each candidate and normal/full view change button */
    private TextView mViewCandidateTemplate;
    /** Number of candidates in full view */
    private int mFullViewWordCount;
    /** Number of candidates in the current line (in full view) */
    private int mFullViewOccupyCount;
    /** View of the previous candidate (in full view) */
    private TextView mFullViewPrevView;
    /** Layout of the previous candidate (in full view) */
    private ViewGroup.LayoutParams mFullViewPrevParams;
    /** Whether all candidates are displayed */
    private boolean mCreateCandidateDone;
    /** Number of lines in normal view */
    private int mNormalViewWordCountOfLine;

    /** List of textView for CandiData List 1st for Symbol mode */
    private ArrayList<TextView> mTextViewArray1st = new ArrayList<TextView>();
    /** List of textView for CandiData List 2st for Symbol mode */
    private ArrayList<TextView> mTextViewArray2nd = new ArrayList<TextView>();
    /** Now focus textView index */
    private int mCurrentFocusIndex = FOCUS_NONE;
    /** Focused View */
    private View mFocusedView = null;
    /** Focused View Background */
    private Drawable mFocusedViewBackground = null;
    /** Axis to find next TextView for Up/Down */
    private int mFocusAxisX = 0;
    /** Now focused TextView in mTextViewArray1st */
    private boolean mHasFocusedArray1st = true;

    /** Portrait Number of Lines from Preference */
    private int mPortraitNumberOfLine = LINE_NUM_PORTRAIT;
    /** Landscape Number of Lines from Preference */
    private int mLandscapeNumberOfLine = LINE_NUM_LANDSCAPE;

    /** Whether ViewLayout Change Enable */
    private boolean mIsViewLayoutEnableWebAPI;

    /** Color of the WebAPI Key */
    private int mWebAPIKeyTextColor = 0;

    /** Color of the WebAPI candidates */
    private int mWebAPICandTextColor = 0;

    /** Color of the WebAPI no candidates */
    private int mNoCandidateTextColor = 0;

    /** Coordinates of line */
    private int mLineY = 0;

    /** {@code true} if the candidate is selected */
    private boolean mIsSymbolSelected = false;

    /** Whether candidates is symbol */
    private boolean mIsSymbolMode = false;

    /** Symbol mode */
    private int mSymbolMode = IWnnSymbolEngine.MODE_SYMBOL;

    /** Text size of candidates */
    private float mCandNormalTextSize;

    /** Text size of candidates */
    private float mCandEmojiTextSize;

    /** Text size of Unicode candidates */
    private float mCandUnicodeTextSize;

    /** Text size of category */
    private float mCandCategoryTextSize;

    /** TextView of WebAPI button */
    private TextView mWebApiButton = null;

    /** Display WebApi button or words */
    private boolean mIsDisplayWebApi = false;

    /** HardKeyboard hidden({@code true}) or disp({@code false}) */
    private boolean mHardKeyboardHidden = true;

    /** Whether candidates long click enable */
    private boolean mEnableCandidateLongClick = true;

    /** The tab list of additional symbol list */
    private ArrayList<TextView> mAddSymbolTabList = new ArrayList<TextView>();

    /** The left padding of tab text */
    private int mTabTextPaddingLeft;

    /** The right padding of tab text */
    private int mTabTextPaddingRight;

    /** The category position list of symbol list */
    private ArrayList<Integer> mCategoryYposArrayList = new ArrayList<Integer>();

    /** The history area line of symbol list */
    private int mSymbolHistoryLine = 0;

    /** The Emoji Kind of previous Content */
    private int mPreviousEmojiKind = DecoEmojiContract.KIND_PICTURE;

    /** View's of addView to EmojiAssist*/
    private ArrayList<TextView> mAddViewList;

    /** Whether there is a button or category */
    private boolean mIsCategoryorButton = false;

    /** Init Constant */
    private static final int INIT_CONSTANT_LINE_LAST_INDEX = -1;
    private static final int INIT_CONSTANT_FIRST_INDEX_CATEGORY_CNT = -1;

    /** 1stview 1line last index */
    private int m1stView1stLineLastIndex = INIT_CONSTANT_LINE_LAST_INDEX;

    /** 2ndview 1line last index */
    private int m2ndView1stLineLastIndex = INIT_CONSTANT_LINE_LAST_INDEX;
    /** 1st index category no */
    private int m1stIndexCategoryNo = INIT_CONSTANT_FIRST_INDEX_CATEGORY_CNT;
    /** Number of candidates displaying for 2nd (Except category) */
    private int mWordCount2ndNotCategory = 0;
    /** Whether creatting 2ndview 1stline */
    private boolean mCreatting2ndView1stLine = true;

    /** IME Sound change StandardKey */
    private static final int SOUND_STANDARD_KEY = 4;

    /** {@code Handler} Handler for focus Candidate wait delay */
    private Handler mHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_MOVE_FOCUS:
                    moveFocus(msg.arg1, msg.arg2 == 1);
                    break;

                case MSG_SET_CANDIDATES:
                    if (mViewType == CandidatesViewManager.VIEW_TYPE_FULL && mIsSymbolMode) {
                        displayCandidates(mConverter, false, SET_CANDIDATE_DELAY_LINE_COUNT);
                    }
                    break;

                case MSG_SELECT_CANDIDATES:
                    WnnWord word = null;
                    while ((word = mWnnWordSelectedList.poll()) != null) {
                        selectCandidate(word);
                    }
                    break;

                case MSG_MORE_DISPLAY_CANDIDATES:
                    if (!mIsFullView
                            && (mViewCandidateList2nd.getVisibility() != View.VISIBLE)) {
                        displayCandidates(mConverter, false, -1);
                        mViewCandidateList2nd.setVisibility(View.VISIBLE);
                    }
                    break;

                default:
                    break;
                }
            }
        };

    /** Event listener for touching a candidate for 1st */
    private OnClickListener mCandidateOnClick1st = new OnClickListener() {
        public void onClick(View v) {
            onClickCandidate(v, mWnnWordArray1st);
        }
    };

    /** Event listener for touching a candidate for 2nd */
    private OnClickListener mCandidateOnClick2nd = new OnClickListener() {
        public void onClick(View v) {
            onClickCandidate(v, mWnnWordArray2nd);
        }
    };

    /** Event listener for long-clicking a candidate for 1st */
    private OnLongClickListener mCandidateOnLongClick1st = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            return onLongClickCandidate(v, mWnnWordArray1st);
        }
    };

    /** Event listener for long-clicking a candidate for for 2nd */
    private OnLongClickListener mCandidateOnLongClick2nd = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            return onLongClickCandidate(v, mWnnWordArray2nd);
        }
    };

    /** Event listener for click a symbol tab */
    private OnClickListener mTabOnClick = new OnClickListener() {
        public void onClick(View v) {
            if (!v.isShown()) {
                return;
            }
            if (OpenWnn.getCurrentIme() == null) {
                return;
            }
            mViewBodyScroll.smoothScrollTo(0, 0);
            playSoundAndVibration();

            if ((v instanceof TextView) && (mWnn instanceof IWnnLanguageSwitcher) ) {
                TextView text = (TextView)v;
                switch (text.getId()) {
                case R.id.candview_symbol:
                    if ((mSymbolMode != IWnnSymbolEngine.MODE_SYMBOL)
                            && (mSymbolMode != IWnnSymbolEngine.MODE_OTHERS_SYMBOL)) {
                            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                      IWnnImeBase.ENGINE_MODE_SYMBOL_SYMBOL));
                    }
                    break;

                case R.id.candview_emoticon:
                    if ((mSymbolMode != IWnnSymbolEngine.MODE_KAO_MOJI)
                            && (mSymbolMode != IWnnSymbolEngine.MODE_OTHERS_KAO_MOJI)) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                      IWnnImeBase.ENGINE_MODE_SYMBOL_KAO_MOJI));
                    }
                    break;

                case R.id.candview_decoemojilist:
                    if (mSymbolMode != IWnnSymbolEngine.MODE_DECOEMOJI) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                      IWnnImeBase.ENGINE_MODE_SYMBOL_DECOEMOJI));
                    }
                    break;

                case R.id.candview_pictgram:
                    if (mSymbolMode != IWnnSymbolEngine.MODE_EMOJI) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                      IWnnImeBase.ENGINE_MODE_SYMBOL_EMOJI));
                    }
                    break;

                case R.id.candview_pictgram_uni6:
                    if (mSymbolMode != IWnnSymbolEngine.MODE_EMOJI_UNI6) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                                      IWnnImeBase.ENGINE_MODE_SYMBOL_EMOJI_UNI6));
                    }
                    break;

                default:
                    IWnnSymbolEngine symbolEngine = ((IWnnSymbolEngine) mConverter);
                    int index = mAddSymbolTabList.indexOf(text);
                    int current = symbolEngine.getAdditionalSymbolIndex();
                    if ((0 <= index) && ((index != current)
                            || (mSymbolMode != IWnnSymbolEngine.MODE_ADD_SYMBOL))) {
                        symbolEngine.setAdditionalSymbolIndex(index);
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                        IWnnImeBase.ENGINE_MODE_SYMBOL_ADD_SYMBOL));

                    }
                    break;
                }
            }
        }
    };

    /**
     * Constructor
     */
    public TextCandidatesViewManager() {
        this(iWnnEngine.CANDIDATE_MAX);
    }

    /**
     * Constructor
     *
     * @param displayLimit      The limit of display
     */
    public TextCandidatesViewManager(int displayLimit) {
        mDisplayLimit = displayLimit;
    }

    /**
     * Handle a click event on the candidate.
     * @param v  View
     * @param list  List of candidates
     */
    private void onClickCandidate(View v, ArrayList<WnnWord> list) {
        if (!v.isShown()) {
            return;
        }
        if (OpenWnn.getCurrentIme() == null) {
            return;
        }

        if (mWnn.mInputViewManager.isPopupKeyboard()){
            mWnn.mInputViewManager.closePopupKeyboard();
            return;
        }

        playSoundAndVibration();

        if (v instanceof TextView) {
            TextView text = (TextView)v;
            int wordcount = text.getId();
            WnnWord word = list.get(wordcount);
            if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI) != 0) {
                clearFocusCandidate();
                selectWebApiButton();
                return;
            }
            if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN) != 0) {
                clearFocusCandidate();
                selectWebApiGetAgainButton();
                return;
            }
            clearFocusCandidate();
            selectCandidate(word);
        }
    }

    /**
     * Handle a long click event on the candidate.
     * @param v  View
     * @param list  List of candidates
     */
    public boolean onLongClickCandidate(View v, ArrayList<WnnWord> list) {
        if (mWnn instanceof IWnnLanguageSwitcher) {
            if (mWnn.mInputViewManager.isPopupKeyboard()){
                mWnn.mInputViewManager.closePopupKeyboard();
                return false;
            }
        }
        if (mViewLongPressDialog == null) {
            return false;
        }

        if (mIsSymbolMode && mSymbolMode != IWnnSymbolEngine.MODE_ADD_SYMBOL) {
            return false;
        }

        if (!mEnableCandidateLongClick) {
            return false;
        }

        if (!v.isShown()) {
            return true;
        }

        Drawable d = v.getBackground();
        if (d != null) {
            // ignores if the selection is cleared when onTouch()
            if(d.getState().length == 0){
                return true;
            }
        }

        if (v instanceof TextView) {
            int wordcount = ((TextView)v).getId();

            if (mConverter instanceof IWnnSymbolEngine) {
                boolean ret = false;
                WnnWord word = list.get(wordcount);
                ret = ((IWnnSymbolEngine) mConverter).startLongPressActionAdditionalSymbol(word);
                return ret;
            }

            mWord = list.get(wordcount);
            clearFocusCandidate();
            if (mIsDisplayWebApi) {
                if (mWnn instanceof IWnnLanguageSwitcher) {
                    ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CANCEL_WEBAPI));
                }
            }
            displayDialog(v, mWord);
        }
        return true;
    }

    /**
     * Set auto-hide mode.
     * @param hide      {@code true} if the view will hidden when no candidate exists;
     *                  {@code false} if the view is always shown.
     */
    public void setAutoHide(boolean hide) {
        mAutoHideMode = hide;
    }

    //----------------------------------------------------------------------
    // CandidatesViewManager
    //----------------------------------------------------------------------
    /** @see CandidatesViewManager#initView */
    public View initView(InputMethodBase parent, int width, int height) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "TextCandidatesView::initView() height="+height+", width="+width);}
        EmojiAssist assist = EmojiAssist.getInstance();
        if (mViewLongPressDialog != null) {
            TextView tv = (TextView)mViewLongPressDialog.findViewById(R.layout.candidate_longpress_dialog);
            EmojiAssist.getInstance().removeView(tv);
        }
        mAddViewList = new ArrayList<TextView>();
        if (mViewCandidateList1st != null) {
            for (int i = 0; i < SETTING_NUMBER_OF_LINEMAX; i++) {
                LinearLayout lineView = (LinearLayout)mViewCandidateList1st.getChildAt(i);
                int childCount = lineView.getChildCount();
                for (int j = 0; j < childCount; j++) {
                    TextView text = (TextView)lineView.getChildAt(j);
                    assist.removeView(text);
                }
            }
        }

        if (mViewCandidateList2nd != null) {
            int childCount = mViewCandidateList2nd.getChildCount();
            for (int j = 0; j < childCount; j++) {
                TextView text = (TextView)mViewCandidateList2nd.getChildAt(j);
                assist.removeView(text);
            }
        }
        ExtractEditText extractEditText = parent.getExtractEditText();
        if (extractEditText != null && parent.isFullScreenChanged()) {
            CharSequence text = extractEditText.getText();
            int selStart = Selection.getSelectionStart(text);
            int selEnd = Selection.getSelectionEnd(text);

            if ((selStart >= 0) && (selEnd >= 0)) {
                Selection.setSelection(extractEditText.getText(), selStart, selEnd);
            }
            parent.setFullScreenChanged();
        }

        mWnn = parent;
        mIsViewLayoutEnableWebAPI = true;
        mViewWidth = width;
        Resources r = mWnn.getResources();
        mCandidateMinimumWidth = r.getDimensionPixelSize(R.dimen.cand_minimum_width);
        mCandidateMinimumHeight = r.getDimensionPixelSize(R.dimen.cand_minimum_height);
        mCandidateSymbolMinimumHeight = r.getDimensionPixelSize(R.dimen.cand_symbol_minimum_height);
        mCandidateCategoryMinimumHeight = r.getDimensionPixelSize(R.dimen.cand_category_minimum_height);
        mCandidateLeftAlignThreshold = r.getDimensionPixelSize(R.dimen.cand_left_align_threshold);
        mCandidateBgViewPaddingTop = r.getDimensionPixelSize(R.dimen.cand_base_view_padding_top);
        mCandidateBgViewPaddingBottom = r.getDimensionPixelSize(R.dimen.cand_base_view_padding_bottom);

        int tab_top_padding = r.getDimensionPixelSize(R.dimen.cand_category_padding_top);
        int tab_bottom_padding = r.getDimensionPixelSize(R.dimen.cand_category_padding_bottom);
        int tab_height = r.getDimensionPixelSize(R.dimen.cand_tab_height);
        mSymbolKeyboardTabHeight = tab_height + tab_top_padding + tab_bottom_padding;

        mPortrait =
            (r.getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE);

        mCandNormalTextSize = r.getDimensionPixelSize(R.dimen.cand_normal_text_size);
        mCandCategoryTextSize = r.getDimensionPixelSize(R.dimen.cand_category_text_size);

        mCandEmojiTextSize = r.getDimensionPixelSize(R.dimen.cand_emoji_text_size);
        mCandUnicodeTextSize = r.getDimensionPixelSize(R.dimen.cand_emoji_text_size_unicode);

        if (mViewBody != null) {
            removeAllViewRecursive(mViewBody);
        }

        LayoutInflater inflater = parent.getLayoutInflater();
        mViewBody = (ViewGroup)inflater.inflate(R.layout.candidates, null);

        mViewTabScroll = (HorizontalScrollView)mViewBody.findViewById(R.id.tab_scroll);

        mViewTabBase = (ViewGroup)mViewBody.findViewById(R.id.tab_base);

        mViewTabPictgram = (TextView)mViewBody.findViewById(R.id.candview_pictgram);
        mViewTabPictgram_uni6 = (TextView)mViewBody.findViewById(R.id.candview_pictgram_uni6);
        mViewTabSymbol = (TextView)mViewBody.findViewById(R.id.candview_symbol);
        mViewTabEmoticon = (TextView)mViewBody.findViewById(R.id.candview_emoticon);

        mViewTabDecoEmoji = (TextView) mViewBody.findViewById(R.id.candview_decoemojilist);

        mViewBodyScroll = (ScrollView)mViewBody.findViewById(R.id.candview_scroll);
        mViewBodyScroll.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(mWnn != null) {
                    if (mWnn.mInputViewManager.isPopupKeyboard()){
                        mWnn.mInputViewManager.closePopupKeyboard();
                        return false;
                    }
                }
            return false;
            }
        });

        mViewCandidateBase = (ViewGroup)mViewBody.findViewById(R.id.candview_base);
        mViewCandidateBaseBg = (LinearLayout)mViewBody.findViewById(R.id.candview_base_background);

        setNumeberOfDisplayLines();
        createNormalCandidateView();

        mViewCandidateList2nd = (AbsoluteLayout)mViewBody.findViewById(R.id.candidates_2nd_view);

        mViewCandidateNothing = mViewBody.findViewById(R.id.candidates_are_nothing);

        mViewCandidateBlankArea = mViewBody.findViewById(R.id.candidate_blank_area);

        KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
        mTextColor = resMan.getColor(OpenWnn.superGetContext(), R.color.candidate_text, "CandidateColor");

        mWebAPIKeyTextColor = resMan.getColor(OpenWnn.superGetContext(), R.color.webapi_text_key, "WebApiButtonColor");
        mWebAPICandTextColor = resMan.getColor(OpenWnn.superGetContext(), R.color.webapi_text_candidate, "WebApiCandidateColor");
        mNoCandidateTextColor = resMan.getColor(OpenWnn.superGetContext(), R.color.webapi_text_nocandidate, "NoCandidateColor");

        mTabTextPaddingLeft = r.getDimensionPixelSize(R.dimen.tab_text_padding_left);
        mTabTextPaddingRight = r.getDimensionPixelSize(R.dimen.tab_text_padding_right);

        mReadMoreButton = (ImageView)mViewBody.findViewById(R.id.read_more_button);
        int readMorePaddingTop = r.getDimensionPixelSize(R.dimen.read_more_padding_top)
                + mCandidateBgViewPaddingTop;
        int readMorePaddingRight = r.getDimensionPixelSize(R.dimen.read_more_padding_right);
        mReadMoreButton.setPadding(0, readMorePaddingTop, readMorePaddingRight, 0);
        mReadMoreButton.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if(mWnn != null) {
                        if (mWnn.mInputViewManager.isPopupKeyboard()){
                            mWnn.mInputViewManager.closePopupKeyboard();
                            return true;
                        }
                    }
                    int resid = 0;
                    String reskey = null;
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mIsFullView) {
                            resid = R.drawable.cand_up_press;
                            reskey = "cand_up_press";
                        } else {
                            resid = R.drawable.cand_down_press;
                            reskey = "cand_down_press";
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mIsFullView) {
                            resid = R.drawable.cand_up;
                            reskey = "cand_up";
                        } else {
                            resid = R.drawable.cand_down;
                            reskey = "cand_down";
                        }
                        break;
                    default:
                        break;
                    }

                    if (resid != 0) {
                        Drawable button = null;
                        KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
                        button = resMan.getDrawable(OpenWnn.superGetContext(), resid, reskey);
                        mReadMoreButton.setImageDrawable(button);
                    }
                    return false;
                }
            });
        mReadMoreButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!v.isShown()) {
                        return;
                    }
                    playSoundAndVibration();

                    if (mIsFullView) {
                        mIsFullView = false;
                        if (mWnn instanceof IWnnLanguageSwitcher) {
                            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
                        } else {
                            mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
                            setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
                        }
                    } else {
                        mIsFullView = true;
                        if (mWnn instanceof IWnnLanguageSwitcher) {
                            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_FULL));
                        } else {
                            mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_FULL));
                            setViewType(CandidatesViewManager.VIEW_TYPE_FULL);
                        }
                    }
                }
            });

        setViewType(CandidatesViewManager.VIEW_TYPE_CLOSE);

        if (mViewLongPressDialog != null) {
            removeAllViewRecursive((ViewGroup)mViewLongPressDialog);
        }
        mViewLongPressDialog = (View)inflater.inflate(R.layout.candidate_longpress_dialog, null);

        /* select button */
        Button longPressDialogButton = (Button)mViewLongPressDialog.findViewById(R.id.candidate_longpress_dialog_select);
        longPressDialogButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    playSoundAndVibration();
                    clearFocusCandidate();
                    selectCandidate(mWord);
                    closeDialog();
                }
            });

        /* mashup button */
        longPressDialogButton = (Button)mViewLongPressDialog.findViewById(R.id.candidate_longpress_dialog_mashup);
        longPressDialogButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    playSoundAndVibration();
                    if (mWnn instanceof IWnnLanguageSwitcher) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CALL_MUSHROOM, mWord));
                    } else {
                        mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.CALL_MUSHROOM, mWord.candidate, mWord.attribute));
                    }
                    closeDialog();
                }
            });

        /* delete button */
        longPressDialogButton = (Button)mViewLongPressDialog.findViewById(R.id.candidate_longpress_dialog_delete);
        longPressDialogButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    playSoundAndVibration();
                    if (mWnn instanceof IWnnLanguageSwitcher) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
                    } else {
                        mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
                    }
                    mConverter.deleteWord(mWord);
                    if (mWnn instanceof IWnnLanguageSwitcher) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.UPDATE_CANDIDATE));
                    } else {
                        mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.UPDATE_CANDIDATE));
                    }
                    closeDialog();
                }
            });

        /* cancel button */
        longPressDialogButton = (Button)mViewLongPressDialog.findViewById(R.id.candidate_longpress_dialog_cancel);
        longPressDialogButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    playSoundAndVibration();
                    if (mWnn instanceof IWnnLanguageSwitcher) {
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
                        ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.UPDATE_CANDIDATE));
                    } else {
                        mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
                        mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.UPDATE_CANDIDATE));
                    }
                    closeDialog();
                }
            });

        return mViewBody;
    }

    /**
     * Create the normal candidate view.
     */
    private void createNormalCandidateView() {
        mViewCandidateList1st = (LinearLayout)mViewBody.findViewById(R.id.candidates_1st_view);

        mViewCandidateListTab = (LinearLayout)mViewBody.findViewById(R.id.candview_tab);
        TextView tPictgram = mViewTabPictgram;
        tPictgram.setOnClickListener(mTabOnClick);
        TextView tPictgram_uni6 = mViewTabPictgram_uni6;
        tPictgram_uni6.setOnClickListener(mTabOnClick);
        TextView tSymbol = mViewTabSymbol;
        tSymbol.setOnClickListener(mTabOnClick);
        TextView tEmoticon = mViewTabEmoticon;
        tEmoticon.setOnClickListener(mTabOnClick);

        TextView tDecoEmoji = mViewTabDecoEmoji;
        tDecoEmoji.setOnClickListener(mTabOnClick);

        int line = SETTING_NUMBER_OF_LINEMAX;
        int width = mViewWidth;
        for (int i = 0; i < line; i++) {
            LinearLayout lineView = new LinearLayout(mViewBodyScroll.getContext());
            lineView.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                              ViewGroup.LayoutParams.WRAP_CONTENT);
            lineView.setLayoutParams(layoutParams);
            lineView.setBaselineAligned(false);
            for (int j = 0; j < (width / getCandidateMinimumWidth()); j++) {
                TextView tv = createCandidateView();
                lineView.addView(tv);
            }

            if (i == 0) {
                // Add TextView (which is shown under the candidate window expansion button) to the first line.
                TextView tv = createCandidateView();
                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                             ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 0;
                layoutParams.gravity = Gravity.RIGHT;
                tv.setLayoutParams(layoutParams);
                tv.setMinimumWidth(mCandidateMinimumWidth);

                lineView.addView(tv);
                mViewCandidateTemplate = tv;
            }
            mViewCandidateList1st.addView(lineView);
        }
    }

    /** @see CandidatesViewManager#getCurrentView */
    public View getCurrentView() {
        return mViewBody;
    }

    /** @see CandidatesViewManager#setViewType */
    public void setViewType(int type) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "TextCandidatesView:setViewType("+type+")");}

        boolean readMore = setViewLayout(type);

        if (mWnn.onEvaluateFullscreenMode()) {
            mViewCandidateBlankArea.setVisibility(View.GONE);
        } else {
            mViewCandidateBlankArea.setVisibility(View.VISIBLE);
        }

        if (readMore) {
            displayCandidates(this.mConverter, false, -1);
        } else {
            if (type == CandidatesViewManager.VIEW_TYPE_NORMAL) {
                mIsFullView = false;
                if (mDisplayEndOffset > 0) {
                    int maxLine = getMaxLine();
                    displayCandidates(this.mConverter, false, maxLine);
                } else {
                    setReadMore();
                }
            } else {
                if (mViewBody.isShown()) {
                    mWnn.setCandidatesViewShown(false);
                }
            }
        }
    }

    /**
     * Set the view layout.
     *
     * @param type      View type,
     * from {@link jp.co.omronsoft.iwnnime.ml.CandidatesViewManager#VIEW_TYPE_NORMAL} to
     * {@link jp.co.omronsoft.iwnnime.ml.CandidatesViewManager#VIEW_TYPE_CLOSE}
     * @return          {@code true} if display is updated; {@code false} if otherwise
     */
    private boolean setViewLayout(int type) {
        if (!mIsViewLayoutEnableWebAPI) {
            return false;
        }

        ViewGroup.LayoutParams params;
        int line = (mPortrait) ? mPortraitNumberOfLine : mLandscapeNumberOfLine;
        int hight = mCandidateMinimumHeight * line;

        if ((mViewType == CandidatesViewManager.VIEW_TYPE_FULL)
                && (type == CandidatesViewManager.VIEW_TYPE_NORMAL)) {
            clearFocusCandidate();
        }

        if (mIsSymbolMode && (type != CandidatesViewManager.VIEW_TYPE_FULL)) {
            hight = hight + mCandidateBgViewPaddingTop + mCandidateBgViewPaddingBottom;
        }

        if (mViewType != type) {
            setCandidateListViewBg();
        }
        mViewType = type;

        switch (type) {
        case CandidatesViewManager.VIEW_TYPE_CLOSE:
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, hight);
            mViewBodyScroll.setLayoutParams(params);
            mViewCandidateListTab.setVisibility(View.GONE);
            mViewCandidateBase.setMinimumHeight(-1);
            mHandler.removeMessages(MSG_SET_CANDIDATES);
            return false;

        case CandidatesViewManager.VIEW_TYPE_NORMAL:
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, hight);
            mViewBodyScroll.setLayoutParams(params);
            mViewBodyScroll.smoothScrollTo(0, 0);
            mViewCandidateListTab.setVisibility(View.GONE);
            mViewCandidateList1st.setVisibility(View.VISIBLE);

            if (mFullViewPrevView != null) {
                mViewCandidateList2nd.setVisibility(View.VISIBLE);
            } else {
                mViewCandidateList2nd.setVisibility(View.GONE);
            }

            mViewCandidateNothing.setVisibility(View.GONE);
            mViewCandidateBase.setMinimumHeight(-1);
            return false;

        case CandidatesViewManager.VIEW_TYPE_FULL:
        default:
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                                   getCandidateViewHeight());
            mViewBodyScroll.setLayoutParams(params);
            if (mIsSymbolMode) {
                updateSymbolType();
                mViewCandidateListTab.setVisibility(View.VISIBLE);
            } else {
                mViewCandidateListTab.setVisibility(View.GONE);
            }
            mViewCandidateList2nd.setVisibility(View.VISIBLE);
            mViewCandidateBase.setMinimumHeight(-1);
            return true;
        }
    }

    /** @see CandidatesViewManager#getViewType */
    public int getViewType() {
        return mViewType;
    }

    /** @see CandidatesViewManager#displayCandidates */
    public void displayCandidates(WnnEngine converter) {

        mHandler.removeMessages(MSG_MORE_DISPLAY_CANDIDATES);

        if (mIsSymbolSelected) {
            mIsSymbolSelected = false;
            int prevLineCount = mSymbolHistoryLine;
            int prevWordCount1st = mWordCount1st;
            clearNormalViewCandidate();
            mWordCount1st = 0;
            mLineCount = 1;
            mLineLength = 0;
            mNormalViewWordCountOfLine = 0;
            mWnnWordArray1st.clear();
            mTextViewArray1st.clear();
            displaySymbolHistory(converter);
            if (((prevWordCount1st == 0) && (mWordCount1st == 1)) ||
                (prevLineCount < mSymbolHistoryLine)) {
                int addHeight = getCandidateMinimumHeight();
                mViewBodyScroll.scrollTo(0, mViewBodyScroll.getScrollY() + addHeight);

                if (mIsCategoryorButton) {
                    // Since the height of the history area has changed, update the list of category coordinates.
                    ArrayList<Integer> tempCategoryYposArrayList = (ArrayList<Integer>)mCategoryYposArrayList.clone();
                    mCategoryYposArrayList.clear();
                    int preIndexCnt = tempCategoryYposArrayList.size();
                    int index = 0;
                    if (!((prevWordCount1st == 0) && (mWordCount1st == 1))) {
                        index = 1;
                    }

                    Integer pos = Integer.valueOf(0);
                    mCategoryYposArrayList.add(pos);
                    for (; index < preIndexCnt; index++) {
                         int preY = tempCategoryYposArrayList.get(index).intValue();
                         pos = Integer.valueOf(preY + addHeight);
                         mCategoryYposArrayList.add(pos);
                    }
                }
            }
            if (isFocusCandidate() && mHasFocusedArray1st) {
                mCurrentFocusIndex = 0;
                Message m = mHandler.obtainMessage(MSG_MOVE_FOCUS, 0, 0);
                mHandler.sendMessage(m);
            }
            if (converter instanceof IWnnSymbolEngine) {
                ((IWnnSymbolEngine) converter).restoreCurrentIndex();
            }
            return;
        }

        mCanReadMore = false;
        mDisplayEndOffset = 0;
        mIsFullView = false;
        mFullViewWordCount = 0;
        mFullViewOccupyCount = 0;
        mFullViewPrevView = null;
        mCreateCandidateDone = false;
        mNormalViewWordCountOfLine = 0;
        mIsDisplayWebApi = false;

        int cnt = mViewCandidateList1st.getChildCount();

        for (int i = 0; i < cnt; i++) {
            LinearLayout lineView = (LinearLayout) mViewCandidateList1st.getChildAt(i);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lineView.setLayoutParams(layoutParams);
        }

        clearCandidates();
        if (!mWnn.isEmoji()) {
            mWnn.endEmojiAssist();
        }
        mConverter = converter;
        if (mWnn instanceof IWnnLanguageSwitcher) {
            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
        } else {
            mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
            setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
        }

        mViewCandidateTemplate.setVisibility(View.VISIBLE);
        setCandidateBackGround(mViewCandidateTemplate, R.drawable.cand_back);
        mViewCandidateTemplate.setMinHeight(getCandidateMinimumHeight());
        mViewCandidateTemplate.setMinimumWidth(
                mWnn.getResources().getDimensionPixelSize(R.dimen.cand_up_down_button_width));

        displayCandidates(converter, true, getMaxLine());

        if (mIsSymbolMode) {
            createAdditionalTab();
            mIsFullView = true;
            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_FULL));

        } else {
            mHandler.sendEmptyMessageDelayed(MSG_MORE_DISPLAY_CANDIDATES, MORE_DISPLAY_CANDIDATE_DELAY);
        }
    }

    /**
     * Create additional tab.
     */
    private void createAdditionalTab() {
        for (TextView tab : mAddSymbolTabList) {
            tab.setOnClickListener(null);
            mViewTabBase.removeView(tab);
        }
        mAddSymbolTabList.clear();

        String[] tabNames = ((IWnnSymbolEngine)mConverter).getAdditionalSymbolTabNames();
        if (tabNames != null) {
            for (String tabName : tabNames) {
                TextView tab = createTab(tabName);
                mViewTabBase.addView(tab, mViewTabSymbol.getLayoutParams());
                mAddSymbolTabList.add(tab);
            }
        }
    }

    /**
     * Get max number of lines to display.
     *
     * @return  Maximum number of lines to display,
     * {@link jp.co.omronsoft.iwnnime.ml.TextCandidatesViewManager#LINE_NUM_PORTRAIT} or
     * {@link jp.co.omronsoft.iwnnime.ml.TextCandidatesViewManager#LINE_NUM_LANDSCAPE}
     */
    private int getMaxLine() {
        // max number of lines to display
        int maxLine = (mPortrait) ? mPortraitNumberOfLine : mLandscapeNumberOfLine;
        return maxLine;
    }

    /**
     * Display the candidates.
     *
     * @param converter  {@link WnnEngine} which holds candidates.
     * @param dispFirst  Whether it is the first time displaying the candidates
     * @param maxLine    The maximum number of displaying lines,
     * {@link jp.co.omronsoft.iwnnime.ml.TextCandidatesViewManager#LINE_NUM_PORTRAIT} or
     * {@link jp.co.omronsoft.iwnnime.ml.TextCandidatesViewManager#LINE_NUM_LANDSCAPE}
     */
    private void displayCandidates(WnnEngine converter, boolean dispFirst, int maxLine) {
        if (OpenWnn.PERFORMANCE_DEBUG) {Log.d("iwnn", "TextCandidatesView::displayCandidates  Start");}

        if (converter == null) {
            return;
        }

        /* Concatenate the candidates already got and the last one in dispFirst mode */
        int displayLimit = mDisplayLimit;

        boolean isDelay = false;
        boolean isHistorySequence = false;
        boolean isBreak = false;

        if (converter instanceof IWnnSymbolEngine) {
            if (!dispFirst && !mCreateCandidateDone) {
                if (maxLine == -1 || maxLine == SET_CANDIDATE_DELAY_LINE_COUNT) {
                    isDelay = true;
                    if (maxLine == SET_CANDIDATE_DELAY_LINE_COUNT) {
                        maxLine = mLineCount + SET_CANDIDATE_DELAY_LINE_COUNT;
                    } else {
                        maxLine = mLineCount + SET_CANDIDATE_LINE_COUNT;
                    }
                    if (mSymbolMode == IWnnSymbolEngine.MODE_DECOEMOJI) {
                        mHandler.sendEmptyMessageDelayed(MSG_SET_CANDIDATES, SET_CANDIDATE_DELAY_DECO);
                    } else if (maxLine <= SET_CANDIDATE_LINE_COUNT_PART1) {
                        mHandler.sendEmptyMessageDelayed(MSG_SET_CANDIDATES, SET_CANDIDATE_DELAY);
                    } else if (SET_CANDIDATE_LINE_COUNT_PART1 < maxLine
                                && maxLine <= SET_CANDIDATE_LINE_COUNT_PART2) {
                        mHandler.sendEmptyMessageDelayed(MSG_SET_CANDIDATES, SET_CANDIDATE_DELAY_PART1);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_SET_CANDIDATES, SET_CANDIDATE_DELAY_PART2);
                    }
                }
            }
            displayLimit = -1;
        }

        /* Get candidates */
        WnnWord result = null;
        String prevCandidate = null;
        while ((displayLimit == -1 || getWordCount() < displayLimit)) {
            for (int i = 0; i < DISPLAY_LINE_MAX_COUNT; i++) {
                result = converter.getNextCandidate();
                if (result == null) {
                    break;
                }

                if ((result.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_SYMBOLLIST) != 0) {
                    break;
                }

                if ((prevCandidate == null) || !prevCandidate.equals(result.candidate)) {
                    break;
                }
            }

            if (result == null) {
                break;
            } else {
                prevCandidate = result.candidate;
            }

            //When showing EMOJI and Full/Half-width symbol list.
            if ((result.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_SYMBOLLIST) != 0) {
                //When showing the history of EMOJI and Full/Half-width symbol list.
                if ((result.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_HISTORY) != 0) {
                    isHistorySequence = true;
                } else {
                    //Categorize it when the list is EMOJI
                    if (isCategory(result)) {
                        if (getWordCount() != 0) {
                            createNextLine(false);
                        }
                        result.candidate = result.candidate.substring(1,result.candidate.length()-1);
                        setCandidate(true, result);
                        createNextLine(true);
                        isHistorySequence = false;
                        continue;
                    } else {
                        if (isHistorySequence) {
                            createNextLine(false);
                            isHistorySequence = false;
                        }
                    }
                }
            }

            if ((result.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI) != 0) {
                result.candidate = mWnn.getResources().getString(R.string.ti_webapi_button_txt);
            }

            if ((result.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NO_CANDIDATE) != 0) {
                result.candidate = mWnn.getResources().getString(R.string.ti_webapi_no_candidate_txt);
            }

            if ((result.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN) != 0) {
                result.candidate = mWnn.getResources().getString(R.string.ti_webapi_get_again_txt);
            }

            if ((result.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_WORD) != 0
                    && "".equals(result.candidate)) {
                continue;
            }

            boolean ret = setCandidate(false, result);
            if (!ret) {
                continue;
            }

            if ((dispFirst || isDelay) && (maxLine < mLineCount)) {
                mCanReadMore = true;
                isBreak = true;
                break;
            }
        }

        if (!isBreak && !mCreateCandidateDone) {
            /* align left if necessary */
            createNextLine(false);
            mCreateCandidateDone = true;
            mHandler.removeMessages(MSG_SET_CANDIDATES);
        }

        if (getWordCount() < 1) { /* no candidates */
            if (mAutoHideMode) {
                if (!mIsSymbolMode) {
                    mWnn.setCandidatesViewShown(false);
                    return;
                }
            } else {
                mCanReadMore = false;
                mIsFullView = false;
                //mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
                setViewLayout(CandidatesViewManager.VIEW_TYPE_NORMAL);
            }
        }

        if ((mWordCount2nd == 0) && mIsFullView) {
            mViewCandidateList2nd.setVisibility(View.GONE);
            mViewCandidateNothing.setVisibility(View.VISIBLE);
        }

        setReadMore();

        if (!(mViewBody.isShown())) {
            mWnn.setCandidatesViewShown(true);
        }
        if (OpenWnn.PERFORMANCE_DEBUG) {Log.d("iwnn", "TextCandidatesView::displayCandidates  End");}
        return;
    }

    /**
     * Display the symbol history.
     * @param converter  {@link WnnEngine} which holds candidates.
     */
    private void displaySymbolHistory(WnnEngine converter) {
        WnnWord word = null;

        while ((word = converter.getNextCandidate()) != null) {
            if (isSymbolHistory(word)) {
                setCandidate(false, word);
            } else {
                break;
            }
        }
    }

    /**
     * Add a candidate into the list.
     *
     * @param isCategory  {@code true}:caption of category, {@code false}:normal word
     * @param word        A candidate word
     */
    private boolean setCandidate(boolean isCategory, WnnWord word) {
        CharSequence candidate = DecoEmojiUtil.getSpannedCandidate(word);
        if (candidate.length() == 0) {
            return false;
        }

        boolean isEmojiSymbol = false;
        int emojiKind = 0;
        if (mIsSymbolMode && (candidate.length() < 3)) {
            isEmojiSymbol = true;
        }
        int decowidth = 0;
        if ((DecoEmojiUtil.isDecoEmoji(word.candidate)) || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_DECOEMOJI) != 0)) {
            String normal = "";
            StringBuffer buf = new StringBuffer();
            SpannableStringBuilder text = (SpannableStringBuilder)candidate;
            Annotation[] annotation = null;
            for (int i = 0; i < text.length();i++) {
                annotation = text.getSpans(i, i+1, Annotation.class);
                if (annotation != null && annotation.length > 0) {
                    if (annotation[0].getValue().indexOf(EmojiAssist.SPLIT_KEY) < 0) {
                        continue;
                    }
                    String[] split = annotation[0].getValue().split(EmojiAssist.SPLIT_KEY,0);
                    int temp_decowidth = Integer.parseInt(split[1]);
                    int height = Integer.parseInt(split[2]);
                    emojiKind = Integer.parseInt(split[3]);

                    if (height > EmojiDrawable.DECO_EMOJI_BASE_PIXEL) {
                        decowidth += (int)((float)temp_decowidth / ((float)height / (float)EmojiDrawable.DECO_EMOJI_BASE_PIXEL));
                    } else {
                        decowidth += temp_decowidth;
                    }
                } else {
                    buf.append(candidate.subSequence(i, i+1));
                }
            }
            normal = buf.toString();
            decowidth += measureText(normal, 0, normal.length());
        }

        int viewDivison = getCandidateViewDivison();
        int indentWidth = mViewWidth / viewDivison;
        int spaceWidth = getCandidateSpaceWidth(isEmojiSymbol);
        TextView template = mViewCandidateTemplate;
        int textLength = (candidate.length() < 2) ? 0 : measureText(candidate, 0, candidate.length()) + template.getPaddingLeft() + template.getPaddingRight();
        if (decowidth != 0) {
            textLength = decowidth + template.getPaddingLeft() + template.getPaddingRight();
            TextPaint paint = mViewCandidateTemplate.getPaint();
            float textSize = paint.getTextSize();
            textLength = (int) (textLength * (textSize / EmojiDrawable.DECO_EMOJI_BASE_PIXEL ));
        }
        int maxWidth = mViewWidth;
        TextView textView = null;
        boolean isButton = false;

        boolean is2nd = isFirstListOver(mIsFullView, mLineCount, word);
        if (is2nd) {
            /* Full view */
            int occupyCount = Math.min((textLength + indentWidth + spaceWidth) / indentWidth, viewDivison);

            int buttonAttribute
                = iWnnEngine.WNNWORD_ATTRIBUTE_NEXT_BUTTON
                    | iWnnEngine.WNNWORD_ATTRIBUTE_PREV_BUTTON;
            isButton = ((word.attribute & buttonAttribute) != 0);
            if (isCategory || isButton) {
                mPreviousEmojiKind = DecoEmojiContract.KIND_PICTURE;
                emojiKind = DecoEmojiContract.KIND_PICTURE;
                if (isButton && (getWordCount() != 0)) {
                    createNextLine(false);
                }
                occupyCount = viewDivison;
            }

            boolean tmpEnter = false;
            if (emojiKind == DecoEmojiContract.KIND_PICTURE && emojiKind != mPreviousEmojiKind) {
                tmpEnter = true;
            }

            mPreviousEmojiKind = emojiKind;
            if (viewDivison < (mFullViewOccupyCount + occupyCount) || tmpEnter ) {
                mFullViewPrevParams.width += mViewWidth - (mFullViewOccupyCount * indentWidth);
                if (mFullViewPrevView != null) {
                    mViewCandidateList2nd.updateViewLayout(mFullViewPrevView, mFullViewPrevParams);
                }
                mFullViewOccupyCount = 0;
                mLineCount++;
                if (isCategory) {
                    mLineY += mCandidateCategoryMinimumHeight;
                } else {
                    mLineY += getCandidateMinimumHeight();
                    mCreatting2ndView1stLine = false;
                }
            }
            if (mFullViewWordCount == 0) {
                mLineY = 0;
            }

            if (mCreatting2ndView1stLine) {
                m2ndView1stLineLastIndex = mWordCount2nd;
            }

            ViewGroup layout = mViewCandidateList2nd;

            int width = indentWidth * occupyCount;
            int height = getCandidateMinimumHeight();
            if (occupyCount == viewDivison) {
                width = mViewWidth;
            }
            if (isCategory) {
                height = mCandidateCategoryMinimumHeight;
            }

            ViewGroup.LayoutParams params = buildLayoutParams(mViewCandidateList2nd, width, height);

            textView = (TextView) layout.getChildAt(mFullViewWordCount);
            if (textView == null) {
                textView = createCandidateView();
                textView.setLayoutParams(params);

                mViewCandidateList2nd.addView(textView);
            } else {
                mViewCandidateList2nd.updateViewLayout(textView, params);
            }

            mFullViewOccupyCount += occupyCount;
            mFullViewWordCount++;
            mFullViewPrevView = textView;
            mFullViewPrevParams = params;

        } else {
            textLength = Math.max(textLength, indentWidth - spaceWidth);

            /* Normal view */
            int nextEnd = mLineLength + textLength + spaceWidth;

            if (mLineCount == 1 && !mIsSymbolMode) {
                maxWidth -= mCandidateMinimumWidth;
            }

            if (mSymbolHistoryLine == 0) {
                mSymbolHistoryLine = 1;
            }

            if ((maxWidth < nextEnd) && (getWordCount() != 0)) {
                //Do nothing if mLineCount is bigger than the Maximum line number,
                //when showing the history view of EMOJI, Full/Half-width symbol list
                if ((mLineCount == LINE_NUM_HISTORY) && ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_HISTORY) != 0)) {
                    return true;
                }

                createNextLineFor1st();
                if (getMaxLine() < mLineCount) {
                    mLineLength = 0;
                    /* Call this method again to add the candidate in the full view */
                    if (!mIsSymbolSelected) {
                        setCandidate(isCategory, word);
                    }
                    return true;
                }

                mLineLength = textLength + spaceWidth;
            } else {
                mLineLength = nextEnd;
            }

            if (mSymbolHistoryLine == 1) {
                m1stView1stLineLastIndex = mWordCount1st;
            }

            LinearLayout lineView = (LinearLayout) mViewCandidateList1st.getChildAt(mLineCount - 1);
            //check mNormalViewWordCountOfLine value is valid or not.
            if (mNormalViewWordCountOfLine < 0 ) {
                mNormalViewWordCountOfLine = 0;
            } else if (mNormalViewWordCountOfLine >= lineView.getChildCount()) {
                mNormalViewWordCountOfLine = lineView.getChildCount() - 1;
            }
            
            textView = (TextView) lineView.getChildAt(mNormalViewWordCountOfLine);

            if (isCategory) {
                if (mLineCount == 1) {
                    mViewCandidateTemplate.setBackgroundDrawable(null);
                }
                mLineLength += mCandidateLeftAlignThreshold;

                lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                                                       ViewGroup.LayoutParams.WRAP_CONTENT));
             } else {
                lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                                                       getCandidateMinimumHeight(), 1.0f));
            }

            mNormalViewWordCountOfLine++;
        }

        textView.setText(candidate);

        if (is2nd) {
            textView.setId(mWordCount2nd);
        } else {
            textView.setId(mWordCount1st);
        }
        textView.setVisibility(View.VISIBLE);
        textView.setPressed(false);
        textView.setFocusable(false);

        Resources r = mViewBodyScroll.getContext().getResources();
        if (isCategory) {
            textView.setText(word.candidate);

            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandCategoryTextSize);
            textView.setGravity(Gravity.LEFT | Gravity.CENTER);
            textView.setMinHeight(mCandidateCategoryMinimumHeight);
            textView.setHeight(mCandidateCategoryMinimumHeight);

            textView.setOnClickListener(null);
            textView.setOnLongClickListener(null);
            KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
            boolean valid = keyskin.isValid();
            if (valid) {
                textView.setBackgroundDrawable(keyskin.getDrawable(R.drawable.ime_symbol_title));
                textView.setTextColor(keyskin.getColor(R.color.category_textcolor));
            } else {
                textView.setBackgroundDrawable(r.getDrawable(R.drawable.ime_symbol_title));
                textView.setTextColor(r.getColor(R.color.category_textcolor));
            }

        } else {
            if (mSymbolMode == IWnnSymbolEngine.MODE_EMOJI) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandEmojiTextSize);
            } else if (mSymbolMode == IWnnSymbolEngine.MODE_EMOJI_UNI6) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandUnicodeTextSize);
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandNormalTextSize);
            }
            textView.setGravity(Gravity.CENTER);

            if (is2nd) {
                textView.setMinHeight(getCandidateMinimumHeight());
                textView.setHeight(getCandidateMinimumHeight());
                textView.setOnClickListener(mCandidateOnClick2nd);
                textView.setOnLongClickListener(mCandidateOnLongClick2nd);
            } else {
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.FILL_PARENT, 1.0f));
                textView.setOnClickListener(mCandidateOnClick1st);
                textView.setOnLongClickListener(mCandidateOnLongClick1st);
            }

            setCandidateBackGround(textView, R.drawable.cand_back);

            if (((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI) != 0)
                    || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN) != 0)) {
                textView.setTextColor(mWebAPIKeyTextColor);
                textView.setOnLongClickListener(null);
                setCandidateBackGround(textView, R.drawable.cand_back_webapi);
                mWebApiButton = textView;
                mIsDisplayWebApi = true;
            } else if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_WORD) != 0) {
                textView.setTextColor(mWebAPICandTextColor);
                mIsDisplayWebApi = true;
            } else if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_JOJO_WORD) != 0) {
                textView.setTextColor(mTextColor);
                setCandidateBackGround(textView, R.drawable.cand_back_jojo);
            } else if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NO_CANDIDATE) != 0) {
                textView.setTextColor(mNoCandidateTextColor);
                textView.setOnLongClickListener(null);
                textView.setOnClickListener(null);
                setCandidateBackGround(textView, R.drawable.cand_back_webapi);
                mIsDisplayWebApi = true;
            } else {
                textView.setTextColor(mTextColor);
            }
        }

        Integer pos = null;
        if (!is2nd) {
            if (mCategoryYposArrayList.size() == 0) {
                pos = Integer.valueOf(0);
                mCategoryYposArrayList.add(pos);
            }
            m1stIndexCategoryNo = 0;
        }

        if (isButton || isCategory) {
            // update the list of category coordinates.
            int addHeight = getCandidateMinimumHeight() * mSymbolHistoryLine;
            pos = Integer.valueOf(mLineY + addHeight);
            mCategoryYposArrayList.add(pos);
            mIsCategoryorButton = true;
        }

        if (maxWidth < textLength) {
            textView.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            textView.setEllipsize(null);
        }

        ImageSpan span = null;
        if (word.candidate.equals(" ")
                || word.candidate.equals("\u2005" /* Quarter blank (unicode emoji) */)) {
            span = new ImageSpan(OpenWnn.superGetContext(), R.drawable.word_quarter_space,
                                 DynamicDrawableSpan.ALIGN_BASELINE);
        } else if (word.candidate.equals("\u3000" /* full-width space */)
                || word.candidate.equals("\u2003" /* Full blank (unicode emoji) */)) {
            span = new ImageSpan(OpenWnn.superGetContext(), R.drawable.word_full_space,
                                 DynamicDrawableSpan.ALIGN_BASELINE);
        } else if (word.candidate.equals("\u2002" /* Half blank (unicode emoji) */)) {
            span = new ImageSpan(OpenWnn.superGetContext(), R.drawable.word_half_space,
                                 DynamicDrawableSpan.ALIGN_BASELINE);
        }

        if (span != null) {
            SpannableString spannable = new SpannableString("   ");
            spannable.setSpan(span, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandNormalTextSize);
        }

        int leftPadding = isCategory ? r.getDimensionPixelSize(R.dimen.symbol_category_padding) : 0;
        textView.setPadding(leftPadding, 0, 0, 0);

        if (is2nd) {
            mWnnWordArray2nd.add(mWordCount2nd, word);
            mWordCount2nd++;
            mTextViewArray2nd.add(textView);
            if (!isCategory) {
                mWordCount2ndNotCategory++;
            }
            // Get the index of the first category which has candidates.
            // And there is no deal to a list of candidates to the first category.
            int categoryYposArrayListSize = mCategoryYposArrayList.size();
            if ((m1stIndexCategoryNo == INIT_CONSTANT_FIRST_INDEX_CATEGORY_CNT) && (mWordCount2ndNotCategory == 1) && (categoryYposArrayListSize > 0)) {
                m1stIndexCategoryNo = (categoryYposArrayListSize - 1);
            }
        } else {
            mWnnWordArray1st.add(mWordCount1st, word);
            mWordCount1st++;
            mTextViewArray1st.add(textView);
        }

        Spannable text = new SpannableString(textView.getText());
        int ret = EmojiAssist.getInstance().checkTextData(text);
        if (!mEnableEmojiUNI6) {
            if (ret > EmojiAssist.TYPE_TEXT_NORMAL) {
                EmojiAssist.getInstance().addView(textView);
                mAddViewList.add(textView);

//#ifdef EMOJI-NO-SBM
                if (ret >= EmojiAssist.TYPE_TEXT_DECOEMOJI && mWnn.isEmoji()) {
//#endif EMOJI-NO-SBM

                    mWnn.startEmojiAssist();
                }
            }
        } else {
            if (ret >= EmojiAssist.TYPE_TEXT_DECOEMOJI) {
                EmojiAssist.getInstance().addView(textView);
                mAddViewList.add(textView);
                if (ret >= EmojiAssist.TYPE_TEXT_DECOEMOJI && mWnn.isEmoji()) {
                    mWnn.startEmojiAssist();
                }
            }
        }
        return true;
    }

    /**
     * Create the AbsoluteLayout.LayoutParams.
     *
     * @param layout AbsoluteLayout
     * @param width  The width of the display
     * @param height The height of the display
     * @return Layout parameter
     */
    private ViewGroup.LayoutParams buildLayoutParams(AbsoluteLayout layout, int width, int height) {

        int viewDivison = getCandidateViewDivison();
        int indentWidth = mViewWidth / viewDivison;
        int x         = indentWidth * mFullViewOccupyCount;
        int y         = mLineY;
        ViewGroup.LayoutParams params
              = new AbsoluteLayout.LayoutParams(width, height, x, y);

        return params;
    }

    /**
     * Create a view for a candidate.
     *
     * @return the view
     */
    private TextView createCandidateView() {
        TextView text = new CandidateTextView(mViewBodyScroll.getContext());
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandNormalTextSize);
        setCandidateBackGround(text, R.drawable.cand_back);
        text.setCompoundDrawablePadding(0);
        text.setGravity(Gravity.CENTER);
        text.setSingleLine();
        text.setPadding(0, 0, 0, 0);
        text.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                           ViewGroup.LayoutParams.FILL_PARENT,
                                                           1.0f));
        text.setMinHeight(getCandidateMinimumHeight());
        text.setMinimumWidth(getCandidateMinimumWidth());
        text.setSoundEffectsEnabled(false);

        ((CandidateTextView) text).setmWnn(mWnn);
        return text;
    }

    /**
     * Display Read More Button if there are more candidates.
     */
    private void setReadMore() {
        if (mIsSymbolMode) {
            mReadMoreButton.setVisibility(View.GONE);
            mViewCandidateTemplate.setVisibility(View.GONE);
            return;
        }

        KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
        int resid = 0;
        String reskey = null;

        if (mIsFullView) {
            mReadMoreButton.setVisibility(View.VISIBLE);
            resid = R.drawable.cand_up;
            reskey = "cand_up";
        } else {
            if (mCanReadMore) {
                mReadMoreButton.setVisibility(View.VISIBLE);
                resid = R.drawable.cand_down;
                reskey = "cand_down";
            } else {
                mReadMoreButton.setVisibility(View.GONE);
                mViewCandidateTemplate.setVisibility(View.GONE);
            }
        }

        if (resid != 0) {
            Drawable button = null;
            button = resMan.getDrawable(OpenWnn.superGetContext(), resid, reskey);
            mReadMoreButton.setImageDrawable(button);
        }
    }

    /**
     * Clear the list of the normal candidate view.
     */
    private void clearNormalViewCandidate() {
        LinearLayout candidateList = mViewCandidateList1st;
        int lineNum = candidateList.getChildCount();
        for (int i = 0; i < lineNum; i++) {

            LinearLayout lineView = (LinearLayout)candidateList.getChildAt(i);
            int size = lineView.getChildCount();
            for (int j = 0; j < size; j++) {
                View v = lineView.getChildAt(j);
                v.setVisibility(View.GONE);
            }
        }
    }

    /** @see CandidatesViewManager#clearCandidates */
    public void clearCandidates() {
        mHandler.removeMessages(MSG_MORE_DISPLAY_CANDIDATES);

        closeDialog();
        clearFocusCandidate();
        if (mIsDisplayWebApi) {
            if (mWnn instanceof IWnnLanguageSwitcher) {
                ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.CANCEL_WEBAPI));
            }
        }

        clearNormalViewCandidate();

        ViewGroup layout = mViewCandidateList2nd;
        int size = layout.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = layout.getChildAt(i);
            v.setVisibility(View.GONE);
        }
        if (mAddViewList != null) {
            for (TextView v : mAddViewList) {
                EmojiAssist.getInstance().removeView(v);
            }
            mAddViewList.clear();
        }
        if (mWnn.isEmoji() && (mWnn.getExtractEditText() != null) && !mPortrait)  {
            Spannable text = (Spannable)mWnn.getExtractEditText().getText();
            int ret = mWnn.getEmojiAssist().checkTextData(text);

//#ifdef EMOJI-NO-SBM
            if (ret >= EmojiAssist.TYPE_TEXT_DECOEMOJI) {
//#endif EMOJI-NO-SBM

                mWnn.startEmojiAssist();
            }
        }

        mLineCount = 1;
        mWordCount1st = 0;
        mWordCount2nd = 0;
        mWnnWordArray1st.clear();
        mWnnWordArray2nd.clear();
        mTextViewArray1st.clear();
        mTextViewArray2nd.clear();
        mLineLength = 0;
        mWebApiButton = null;

        mLineY = 0;
        mCategoryYposArrayList.clear();
        mSymbolHistoryLine = 0;
        mIsCategoryorButton = false;
        m1stView1stLineLastIndex = INIT_CONSTANT_LINE_LAST_INDEX;
        m2ndView1stLineLastIndex = INIT_CONSTANT_LINE_LAST_INDEX;
        mWordCount2ndNotCategory = 0;
        m1stIndexCategoryNo = INIT_CONSTANT_FIRST_INDEX_CATEGORY_CNT;
        mCreatting2ndView1stLine = true;
        mIsFullView = false;
        // sets VIEW_TYPE_NORMAL to set height of the next candidate view
        if (mWnn instanceof IWnnLanguageSwitcher) {
            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
        } else {
            mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
        }
        if (mAutoHideMode) {
            setViewLayout(CandidatesViewManager.VIEW_TYPE_CLOSE);
        }

        if (mAutoHideMode && mViewBody.isShown()) {
            mWnn.setCandidatesViewShown(false);
            /**
             * When Candidate View closes on a Full screen,
             * in order to avoid the issue in which a background is drawn,
             * Candidate View is displayed and undisplayed clearly.
             */
            mWnn.setCandidatesViewShown(true);
            mWnn.setCandidatesViewShown(false);
        }
        mCanReadMore = false;
        setReadMore();
    }


    /**
     * Remake the candidate view.
     */
    public void remakeCandidatesWebApi() {
        int focus = mCurrentFocusIndex;
        boolean is1st = mHasFocusedArray1st;

        ((iWnnEngine)mConverter).initGijiList();
        mIsViewLayoutEnableWebAPI = false;
        displayCandidates(mConverter);
        mIsViewLayoutEnableWebAPI = true;
        if (mViewType == CandidatesViewManager.VIEW_TYPE_FULL) {
            mIsFullView = true;
            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_FULL));
        } else {
            displayCandidates(mConverter, false, -1);
        }

        if (focus != FOCUS_NONE) {
            mCurrentFocusIndex = focus;
            mHasFocusedArray1st = is1st;
            if (mHasFocusedArray1st) {
                int size = mTextViewArray1st.size();
                if (mCurrentFocusIndex >= size) {
                    mCurrentFocusIndex = size - 1;
                }
            } else {
                if (mTextViewArray2nd.size() <= 0) {
                    mHasFocusedArray1st = true;
                    mCurrentFocusIndex = mTextViewArray1st.size() - 1;
                }
            }
            Message m = mHandler.obtainMessage(MSG_MOVE_FOCUS, 0, 0);
            mHandler.sendMessage(m);
        }
    }

    /** @see CandidatesViewManager#setPreferences */
    public void setPreferences(SharedPreferences pref) {
        Resources res = mWnn.getResources();
        try {
            if (pref.getBoolean("key_vibration", res.getBoolean(R.bool.key_vibration_default_value))) {
                mVibrator = (Vibrator)mWnn.getSystemService(Context.VIBRATOR_SERVICE);
                if (OpenWnn.isDebugging()) {Log.d("iwnn", "VIBRATOR ON");}
            } else {
                mVibrator = null;
            }
            mSound = pref.getBoolean("key_sound", true);
            setNumeberOfDisplayLines();
        } catch (Exception ex) {
            Log.d("iwnn", "NO VIBRATOR");
        }
        mEnableMushroom = pref.getString("opt_mushroom", res.getString(R.string.mushroom_id_default)).equals("notuse") ? false : true;
    }

    /**
     * Set normal mode.
     */
    public void setNormalMode() {
        setReadMore();
        mIsFullView = false;
        if (mWnn instanceof IWnnLanguageSwitcher) {
            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
        } else {
            mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
        }
    }

    /**
     * Set full mode.
     */
    public void setFullMode() {
        mIsFullView = true;
        if (mWnn instanceof IWnnLanguageSwitcher) {
            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_FULL));
        } else {
            mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_FULL));
        }
    }

    /**
     * Set symbol mode.
     */
    public void setSymbolMode(boolean enable, int mode) {
        if (mIsSymbolMode && !enable) {
            setViewType(CandidatesViewManager.VIEW_TYPE_CLOSE);
        }

        mSymbolMode = mode;
        mIsSymbolMode = enable;
    }

    /**
     * Set scroll up.
     */
    public void setScrollUp() {
        int categoryCnt = mCategoryYposArrayList.size();
        if (categoryCnt <= 0) {
            return;
        }

        int jumpY = mCategoryYposArrayList.get(0).intValue();
        int currentY = mViewBodyScroll.getScrollY();
        if (currentY <= jumpY) {
            if (mWnnWordArray2nd.size() > 0) {
                WnnWord word = mWnnWordArray2nd.get(0);
                if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_PREV_BUTTON) != 0) {
                    selectCandidate(word);
                }
            }
            return;
        } else {
            for (int index = 1; index < categoryCnt; index++) {
                int indexPos = mCategoryYposArrayList.get(index).intValue();
                if (currentY <= indexPos) {
                    break;
                }
                jumpY = indexPos;
            }
        }
        mViewBodyScroll.smoothScrollTo(0, jumpY);
    }

    /**
     * Set scroll down.
     */
    public void setScrollDown() {
        int categoryCnt = mCategoryYposArrayList.size();
        if (categoryCnt <= 0) {
            return;
        }

        int jumpY = mCategoryYposArrayList.get(categoryCnt-1).intValue();
        int currentY = mViewBodyScroll.getScrollY();
        int displayHeight = mViewBodyScroll.getHeight();
        int scrollBodyHeight = mViewBodyScroll.getChildAt(0).getHeight();
        int dispButtomY = currentY + displayHeight;
        if ((currentY >= jumpY) || (dispButtomY >= scrollBodyHeight)) {
            int wordSize = mWnnWordArray2nd.size();
            if (wordSize > 0) {
                WnnWord word = mWnnWordArray2nd.get(wordSize-1);
                if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NEXT_BUTTON) != 0) {
                    selectCandidate(word);
                }
            }
            return;
        } else {
            for (int index = categoryCnt-2; index >= 0; index--) {
                int indexPos = mCategoryYposArrayList.get(index).intValue();
                if (currentY >= indexPos) {
                    break;
                }
                jumpY = indexPos;
            }
        }
        mViewBodyScroll.smoothScrollTo(0, jumpY);
    }

    /**
     * Set scroll full up.
     */
    public void setScrollFullUp() {
        int categoryCnt = mCategoryYposArrayList.size();
        if (categoryCnt <= 0) {
            return;
        }

        int currentY = mViewBodyScroll.getScrollY();
        int firstCategoryY = mCategoryYposArrayList.get(0).intValue();
        if (firstCategoryY < currentY) {
            mViewBodyScroll.smoothScrollTo(0, firstCategoryY);
        }
    }

    /**
     * Set scroll full down.
     */
    public void setScrollFullDown() {
        int categoryCnt = mCategoryYposArrayList.size();
        if (categoryCnt <= 0) {
            return;
        }

        int currentY = mViewBodyScroll.getScrollY();
        int lastCategoryY = mCategoryYposArrayList.get(categoryCnt-1).intValue();
        if (lastCategoryY > currentY) {
            mViewBodyScroll.smoothScrollTo(0, lastCategoryY);
        }
    }

    /**
     * Select a candidate.
     * <br>
     * This method notices the selected word to {@link OpenWnn}.
     *
     * @param word  The selected word
     */
    private void selectCandidate(WnnWord word) {
//#ifdef Only-DOCOMO
        if ((mIsSymbolMode) && (mSymbolMode == IWnnSymbolEngine.MODE_DECOEMOJI)) {
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
//#endif Only-DOCOMO

        if (!mIsSymbolMode) {
            mIsFullView = false;
            if (mWnn instanceof IWnnLanguageSwitcher) {
                ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
            } else {
                mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
            }
        }

        boolean isPrevButton = ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_PREV_BUTTON) != 0);
        boolean isNextButton = ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NEXT_BUTTON) != 0);

        if (isPrevButton || isNextButton) {
            if (mConverter instanceof IWnnSymbolEngine) {
                if (isPrevButton) {
                    ((IWnnSymbolEngine) mConverter).pagePrev();
                } else { // == if (isNextButton)
                    ((IWnnSymbolEngine) mConverter).pageNext();
                }
                displayCandidates(mConverter);
            }
        } else {
            if (mConverter instanceof IWnnSymbolEngine) {
                ((IWnnSymbolEngine) mConverter).saveCurrentIndex();
            }
            mIsSymbolSelected = mIsSymbolMode;
            if (mWnn instanceof IWnnLanguageSwitcher) {
                ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.SELECT_CANDIDATE, word));
            } else {
                mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.SELECT_CANDIDATE, word.candidate, word.id));
            }
        }
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
                Log.e("iwnn", "TextCandidatesViewManager::playSoundAndVibration Vibrator " + ex.toString());
            }
        }
        if (mSound) {
            SoundManager.getInstance().play(SOUND_STANDARD_KEY);
        }
    }

    /**
     * Notified when a tap occurs with the down MotionEvent  that triggered it.
     *
     * @see android.view.GestureDetector.OnGestureListener#onDown
     */
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    /**
     * Notified of a fling event when it occurs with the initial on down MotionEvent
     * and the matching up MotionEvent.
     *
     * @see android.view.GestureDetector.OnGestureListener#onFling
     */
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        boolean consumed = false;

        if (arg1 != null && arg0 != null && arg1.getY() < arg0.getY()) {
            if ((mViewType == CandidatesViewManager.VIEW_TYPE_NORMAL) && mCanReadMore) {
                if (mVibrator != null) {
                    try {
                        int vibrateTime = mWnn.getResources().getInteger(R.integer.vibrate_time);
                        mVibrator.vibrate(vibrateTime);
                    } catch (Exception ex) {
                        Log.e("iwnn", "TextCandidatesViewManager::onFling Vibrator " + ex.toString());
                    }
                }
                mIsFullView = true;
                if (mWnn instanceof IWnnLanguageSwitcher) {
                    ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_FULL));
                } else {
                    mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_FULL));
                }
                consumed = true;
            }
        } else {
            if (mViewBodyScroll.getScrollY() == 0) {
                if (mVibrator != null) {
                    try {
                        int vibrateTime = mWnn.getResources().getInteger(R.integer.vibrate_time);
                        mVibrator.vibrate(vibrateTime);
                    } catch (Exception ex) {
                        Log.e("iwnn", "TextCandidatesViewManager::onFling Sound " + ex.toString());
                    }
                }
                mIsFullView = false;
                if (mWnn instanceof IWnnLanguageSwitcher) {
                    ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_CANDIDATES_NORMAL));
                } else {
                    mWnn.mOriginalInputMethodSwitcher.onEvent(new HWEvent(HWEvent.LIST_CANDIDATES_NORMAL));
                }
                consumed = true;
            }
        }

        return consumed;
    }

    /**
     * Notified when a long press occurs with the initial on down MotionEvent
     * that triggered it.
     *
     * @see android.view.GestureDetector.OnGestureListener#onLongPress
     */
    public void onLongPress(MotionEvent arg0) {
        return;
    }

    /**
     * Notified when a scroll occurs with the initial on down MotionEvent and
     * the current move MotionEvent.
     *
     * @see android.view.GestureDetector.OnGestureListener#onScroll
     */
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    /**
     * The user has performed a down MotionEvent and not performed a move or up yet.
     *
     * @see android.view.GestureDetector.OnGestureListener#onShowPress
     */
    public void onShowPress(MotionEvent arg0) {
    }

    /**
     * Notified when a tap occurs with the up MotionEvent  that triggered it.
     *
     * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp
     */
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    /**
     * Retrieve the width of string to draw.
     *
     * @param text          The string
     * @param start         The start position (specified by the number of character)
     * @param end           The end position (specified by the number of character)
     * @return          The width of string to draw
     */
    public int measureText(CharSequence text, int start, int end) {
        /*
        if (end - start < 3) {
            return getCandidateMinimumWidth();
        }
        */
        TextPaint paint = mViewCandidateTemplate.getPaint();
        return CandidateTextView.getTextWidths(text, paint);
    }

    /**
     * Create a layout for the next line.
     */
    private void createNextLine(boolean isCategory) {
        if (isFirstListOver(mIsFullView, mLineCount, null)) {
            /* Full view */
            mFullViewOccupyCount = 0;
            if (isCategory) {
                mLineY += mCandidateCategoryMinimumHeight;
            } else {
                mLineY += getCandidateMinimumHeight();
            }
            mLineCount++;
            if (mWordCount2ndNotCategory > 0) {
                mCreatting2ndView1stLine = false;
            }
        } else {
            createNextLineFor1st();
        }
    }

    /**
     * Create a layout for the next line.
     */
    private void createNextLineFor1st() {
        LinearLayout lineView = (LinearLayout) mViewCandidateList1st.getChildAt(mLineCount - 1);
        if (mLineLength < mCandidateLeftAlignThreshold) {
            if (mLineCount == 1) {
                mViewCandidateTemplate.setVisibility(View.GONE);
            }
        }

        LinearLayout.LayoutParams params
            = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.FILL_PARENT,
                                            1.0f);

        int child = lineView.getChildCount();
        for (int i = 0; i < child; i++) {
            View view = lineView.getChildAt(i);

            if (view != mViewCandidateTemplate) {
                view.setLayoutParams(params);
                view.setPadding(0, 0, 0, 0);
            }
        }

        mLineLength = 0;
        mNormalViewWordCountOfLine = 0;
        mLineCount++;
        if (mSymbolHistoryLine < LINE_NUM_HISTORY) {
            mSymbolHistoryLine++;
        }
    }

    /**
     * Judge if it's a category.
     *
     * @return {@code true} if category
     */
    boolean isCategory(WnnWord word) {
        return IWnnSymbolEngine.isCategory(word);
    }

    /**
     * Get a minimum width of a candidate view.
     *
     * @return the minimum width of a candidate view.
     */
    private int getCandidateMinimumWidth() {
        return mCandidateMinimumWidth;
    }

    /**
     * Get a minimum height of a candidate view.
     *
     * @return the minimum height of a candidate view.
     */
    private int getCandidateMinimumHeight() {
        if (mIsSymbolMode) {
            return mCandidateSymbolMinimumHeight;
        } else {
            return mCandidateMinimumHeight;
        }
    }

    /**
     * Get a height of a candidate view.
     *
     * @return the height of a candidate view.
     */
    protected int getCandidateViewHeight() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
        if ( !mPortrait ){
            mKeyboardHeight = pref.getInt(KEY_HEIGHT_LANDSCAPE_KEY,
                    mWnn.getResources().getDimensionPixelOffset(R.dimen.key_height)) *
                    DEFAULT_VALUE_KEYLINE +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_keyboard_top) +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_keyboard_bottom);
            mSymbolKeyboardHeight = pref.getInt(KEY_HEIGHT_LANDSCAPE_SYMBOL_KEY,
                    mWnn.getResources().getDimensionPixelOffset(R.dimen.symbol_keyboard_height)) +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_symbol_top) +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_symbol_bottom);
        } else {
            mKeyboardHeight = pref.getInt(KEY_HEIGHT_PORTRAIT_KEY,
                    mWnn.getResources().getDimensionPixelOffset(R.dimen.key_height)) *
                    DEFAULT_VALUE_KEYLINE +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_keyboard_top) +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_keyboard_bottom);
            mSymbolKeyboardHeight = pref.getInt(KEY_HEIGHT_PORTRAIT_SYMBOL_KEY,
                    mWnn.getResources().getDimensionPixelOffset(R.dimen.symbol_keyboard_height)) +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_symbol_top) +
                    mWnn.getResources().getDimensionPixelSize(R.dimen.padding_symbol_bottom);
        }

        int numberOfLine = (mPortrait) ? mPortraitNumberOfLine : mLandscapeNumberOfLine;
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        Drawable keyboardBackground;
        if (keyskin.isValid()) {
            keyboardBackground = keyskin.getKeyboardBg();
        } else {
            Resources resource = mWnn.getResources();
            keyboardBackground = resource.getDrawable(R.drawable.keyboard_background);
        }
        Rect keyboardPadding = new Rect(0 ,0 ,0 ,0);
        keyboardBackground.getPadding(keyboardPadding);
        int keyboardTotalPadding = keyboardPadding.top + keyboardPadding.bottom;
        if (mIsSymbolMode) {
                return mKeyboardHeight + numberOfLine * mCandidateMinimumHeight
                       - mSymbolKeyboardHeight - mSymbolKeyboardTabHeight
                       + mCandidateBgViewPaddingTop + mCandidateBgViewPaddingBottom;
        } else if (!mHardKeyboardHidden) {
                return mKeyboardHeight + numberOfLine * mCandidateMinimumHeight
                       - mSymbolKeyboardHeight
                       + mCandidateBgViewPaddingTop + mCandidateBgViewPaddingBottom;
        } else {
            KeyboardView keyboardView = ((DefaultSoftKeyboard) ((OpenWnn)mWnn).mInputViewManager).getKeyboardView();
            int KeyboardViewHeight = keyboardView.getHeight();

            DefaultSoftKeyboard keyboard = (DefaultSoftKeyboard)OpenWnn.getCurrentIme().mInputViewManager;
            boolean mOneHandedShown = false;
                       
            if (keyboard instanceof DefaultSoftKeyboard) {
                mOneHandedShown = ((DefaultSoftKeyboard)keyboard).isOneHandedMode();
            }
            if (mOneHandedShown) {
                KeyboardViewHeight 
                    += mWnn.getResources().getDimensionPixelSize(R.dimen.one_handed_keybord_view_height);
            }
            return KeyboardViewHeight + keyboardTotalPadding
                       + numberOfLine * mCandidateMinimumHeight
                       + mCandidateBgViewPaddingBottom;
        }
    }

    /**
     * Update symbol type.
     */
    private void updateSymbolType() {
        switch (mSymbolMode) {
        case IWnnSymbolEngine.MODE_SYMBOL:
        case IWnnSymbolEngine.MODE_OTHERS_SYMBOL:
            updateTabStatus(mViewTabPictgram, mEnableEmoji, mEnableEmoji, false);
            updateTabStatus(mViewTabPictgram_uni6, mEnableEmojiUNI6, mEnableEmojiUNI6, false);
            updateTabStatus(mViewTabSymbol, true, true, true);
            updateTabStatus(mViewTabEmoticon, mEnableEmoticon, true, false);
            updateTabStatus(mViewTabDecoEmoji, mEnableDecoEmoji, mEnableDecoEmoji, false);
            break;

        case IWnnSymbolEngine.MODE_KAO_MOJI:
        case IWnnSymbolEngine.MODE_OTHERS_KAO_MOJI:
            updateTabStatus(mViewTabPictgram, mEnableEmoji, mEnableEmoji, false);
            updateTabStatus(mViewTabPictgram_uni6, mEnableEmojiUNI6, mEnableEmojiUNI6, false);
            updateTabStatus(mViewTabSymbol, true, true, false);
            updateTabStatus(mViewTabEmoticon, mEnableEmoticon, true, true);
            updateTabStatus(mViewTabDecoEmoji, mEnableDecoEmoji, mEnableDecoEmoji, false);
            break;

        case IWnnSymbolEngine.MODE_DECOEMOJI:
            updateTabStatus(mViewTabPictgram, mEnableEmoji, mEnableEmoji, false);
            updateTabStatus(mViewTabPictgram_uni6, mEnableEmojiUNI6, mEnableEmojiUNI6, false);
            updateTabStatus(mViewTabSymbol, true, true, false);
            updateTabStatus(mViewTabEmoticon, mEnableEmoticon, true, false);
            updateTabStatus(mViewTabDecoEmoji, mEnableDecoEmoji, mEnableDecoEmoji, true);
            break;

        case IWnnSymbolEngine.MODE_ADD_SYMBOL:
            updateTabStatus(mViewTabPictgram, mEnableEmoji, mEnableEmoji, false);
            updateTabStatus(mViewTabPictgram_uni6, mEnableEmojiUNI6, mEnableEmojiUNI6, false);
            updateTabStatus(mViewTabSymbol, true, true, false);
            updateTabStatus(mViewTabEmoticon, mEnableEmoticon, true, false);
            updateTabStatus(mViewTabDecoEmoji, mEnableDecoEmoji, mEnableDecoEmoji, false);
            break;

        case IWnnSymbolEngine.MODE_EMOJI_UNI6:
            updateTabStatus(mViewTabPictgram, mEnableEmoji, mEnableEmoji, false);
            updateTabStatus(mViewTabPictgram_uni6, mEnableEmojiUNI6, mEnableEmojiUNI6, true);
            updateTabStatus(mViewTabSymbol, true, true, false);
            updateTabStatus(mViewTabEmoticon, mEnableEmoticon, true, false);
            updateTabStatus(mViewTabDecoEmoji, mEnableDecoEmoji, mEnableDecoEmoji, false);
            break;

        default:
            updateTabStatus(mViewTabPictgram, mEnableEmoji, mEnableEmoji, true);
            updateTabStatus(mViewTabPictgram_uni6, mEnableEmojiUNI6, mEnableEmojiUNI6, false);
            updateTabStatus(mViewTabSymbol, true, true, false);
            updateTabStatus(mViewTabEmoticon, mEnableEmoticon, true, false);
            updateTabStatus(mViewTabDecoEmoji, mEnableDecoEmoji, mEnableDecoEmoji, false);
            break;
        }
        updateAdditionalSymbolTabStatus();
    }

    /**
     * Update tab status.
     *
     * @param tab           The tab view.
     * @param enabled       The tab is enabled.
     * @param visibled      The tab is visibled.
     * @param selected      The tab is selected.
     */
    private void updateTabStatus(TextView tab, boolean enabled, boolean visibled, boolean selected) {
        if (visibled) {
            tab.setVisibility(View.VISIBLE);
            tab.setEnabled(enabled);
            KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
            Drawable background = null;
            int backgroundId = 0;
            int colorId = 0;
            String colorkey = null;
            if (enabled) {
                if (selected) {
                    backgroundId = R.drawable.cand_tab;
                    colorId = R.color.tab_textcolor_select;
                    colorkey = "TabSelectColor";
                    background = resMan.getTab();
                    mViewTabScroll.requestChildFocus(mViewTabBase, tab);
                } else {
                    backgroundId = R.drawable.cand_tab_noselect;
                    colorId = R.color.tab_textcolor_no_select;
                    colorkey = "TabNoSelectColor";
                    background = resMan.getTabNoSelect();
                }
            } else {
                backgroundId = R.drawable.cand_tab_noselect;
                colorId = R.color.tab_textcolor_disable;
                colorkey = "TabDisableColor";
                background = resMan.getTabNoSelect();
            }
            if (background != null) {
                tab.setBackgroundDrawable(background);
            } else {
                tab.setBackgroundResource(backgroundId);
            }

            tab.setTextColor(resMan.getColor(OpenWnn.superGetContext(), colorId, colorkey));
        } else {
            tab.setVisibility(View.GONE);
        }
        tab.setPadding(mTabTextPaddingLeft, 0, mTabTextPaddingRight, 0);
    }

    /**
     * Get candidate number of division.
     * @return Number of division
     */
    private int getCandidateViewDivison() {
        int viewDivison;

        if (mIsSymbolMode) {
            int mode = mSymbolMode;
            switch (mode) {
            case IWnnSymbolEngine.MODE_EMOJI:
            case IWnnSymbolEngine.MODE_EMOJI_UNI6:
            case IWnnSymbolEngine.MODE_DECOEMOJI:
                viewDivison = (mPortrait) ? FULL_VIEW_EMOJI_DIV_PORT : FULL_VIEW_EMOJI_DIV_LAND;
                break;
            case IWnnSymbolEngine.MODE_SYMBOL:
            case IWnnSymbolEngine.MODE_OTHERS_SYMBOL:
            case IWnnSymbolEngine.MODE_ADD_SYMBOL:
            case IWnnSymbolEngine.MODE_KAO_MOJI:
            case IWnnSymbolEngine.MODE_OTHERS_KAO_MOJI:
                viewDivison = (mPortrait) ? FULL_VIEW_SYMBOL_DIV_PORT : FULL_VIEW_SYMBOL_DIV_LAND;
                break;
            default:
                viewDivison = FULL_VIEW_DIV;
                break;
            }
        } else {
             viewDivison = FULL_VIEW_DIV;
        }
        return viewDivison;
    }

    /**
     * @return Word count
     */
    private int getWordCount() {
        return mWordCount1st + mWordCount2nd;
    }

    /**
     * @return Add second
     */
    private boolean isFirstListOver(boolean isFullView, int lineCount, WnnWord word) {

        if (mIsSymbolMode) {
            switch (mSymbolMode) {
            case IWnnSymbolEngine.MODE_KAO_MOJI:
            case IWnnSymbolEngine.MODE_OTHERS_KAO_MOJI:
            case IWnnSymbolEngine.MODE_EMOJI:
            case IWnnSymbolEngine.MODE_EMOJI_UNI6:
            case IWnnSymbolEngine.MODE_DECOEMOJI:
            case IWnnSymbolEngine.MODE_SYMBOL:
            case IWnnSymbolEngine.MODE_OTHERS_SYMBOL:
            case IWnnSymbolEngine.MODE_ADD_SYMBOL:
                if (isSymbolHistory(word)) {
                    return false;
                } else {
                    return true;
                }
            default:
                return (isFullView || getMaxLine() < lineCount);
            }
        } else {
            return (isFullView || getMaxLine() < lineCount);
        }
    }

    /**
     * @return Is symbol history
     */
    private static boolean isSymbolHistory(WnnWord word) {
        if (word == null) {
            return false;
        }
        if (((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_SYMBOLLIST) != 0) &&
            ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_HISTORY) != 0)) {
            return true;
        }
        return false;
    }

    /**
     * @return Candidate space width
     */
    private int getCandidateSpaceWidth(boolean isEmojiSymbol) {
        Resources r = mWnn.getResources();
        if (mPortrait) {
            if (isEmojiSymbol) {
                return 0;
            } else {
                return r.getDimensionPixelSize(R.dimen.cand_space_width);
            }
        } else {
            if (isEmojiSymbol) {
                return r.getDimensionPixelSize(R.dimen.cand_space_width_emoji_symbol);
            } else {
                return r.getDimensionPixelSize(R.dimen.cand_space_width);
            }
        }
    }

    /**
     * KeyEvent action for the candidte view.
     *
     * @param key    Key event
     */
    public void processMoveKeyEvent(int key) {
        if (!mViewBody.isShown()) {
            return;
        }

        switch (key) {
        case KeyEvent.KEYCODE_DPAD_UP:
            moveFocus(-1, true);
            break;

        case KeyEvent.KEYCODE_DPAD_DOWN:
            moveFocus(1, true);
            break;

        case KeyEvent.KEYCODE_DPAD_LEFT:
            moveFocus(-1, false);
            break;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            moveFocus(1, false);
            break;

        case KeyEvent.KEYCODE_MOVE_HOME:
            moveFocus(DIRECTION_TO_HOME,true);
            break;

        case KeyEvent.KEYCODE_MOVE_END:
            moveFocus(DIRECTION_TO_END,false);
            break;

        default:
            break;
        }
    }

    /**
     * Get a flag candidate is focused now.
     *
     * @return the Candidate is focused of a flag.
     */
    public boolean isFocusCandidate(){
        if (mCurrentFocusIndex != FOCUS_NONE) {
            return true;
        }
        return false;
    }

    /**
     * Assists to Show FullList of candidate.
     *
     * @param message   action key
     */
    public void showFullListDelay(int message){
    }

    /**
     * Give focus to View of candidate.
     */
    public void setViewStatusOfFocusedCandidate(){
        View view = mFocusedView;
        if (view != null) {
            view.setBackgroundDrawable(mFocusedViewBackground);
            view.setPadding(0, 0, 0, 0);
        }

        TextView v = getFocusedView();
        mFocusedView = v;
        if (v != null) {
            mFocusedViewBackground = v.getBackground();
            setCandidateBackGround(v, R.drawable.cand_back_focuse);
            v.setPadding(0, 0, 0, 0);

            int viewBodyTop = getViewTopOnScreen(mViewBodyScroll);
            int viewBodyBottom = viewBodyTop + mViewBodyScroll.getHeight();
            int focusedViewTop = getViewTopOnScreen(v);
            int focusedViewBottom = focusedViewTop + v.getHeight();

            if (focusedViewBottom > viewBodyBottom) {
                // Scroll Down.
                mViewBodyScroll.scrollBy(0, (focusedViewBottom - viewBodyBottom));
            } else if (focusedViewTop < viewBodyTop) {
                // Scroll Up.
                mViewBodyScroll.scrollBy(0, (focusedViewTop - viewBodyTop));
            }
            if (mWnn instanceof IWnnLanguageSwitcher) {
                ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.UPDATE_VIEW_STATUS_USE_FOCUSED_CANDIDATE));
            }
        }
    }

    /**
     * Clear focus to selected candidate.
     */
    public void clearFocusCandidate(){
        View view = mFocusedView;
        if (view != null) {
            view.setBackgroundDrawable(mFocusedViewBackground);
            view.setPadding(0, 0, 0, 0);
            mFocusedView = null;
        }

        mFocusAxisX = 0;
        mHasFocusedArray1st = true;
        mCurrentFocusIndex = FOCUS_NONE;
        mHandler.removeMessages(MSG_MOVE_FOCUS);

        if (view != null) {
            if (mWnn instanceof IWnnLanguageSwitcher) {
                ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.FOCUS_CANDIDATE_END));
            }
        }
    }

    /**
     * @see CandidatesViewManager#selectFocusCandidate
     */
    public void selectFocusCandidate(){
        if (mCurrentFocusIndex != FOCUS_NONE) {
            WnnWord word = getFocusedWnnWord();
            if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NO_CANDIDATE) != 0) {
                return;
            }

            if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI) != 0) {
                selectWebApiButton();
            } else if ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN) != 0) {
                selectWebApiGetAgainButton();
            } else {
                selectCandidate(word);
            }
        }
    }

    /** @see CandidatesViewManager#getFocusedWnnWord */
    public WnnWord getFocusedWnnWord() {
        return getWnnWord(mCurrentFocusIndex);
    }

    /**
     * Get WnnWord.
     *
     * @return WnnWord word
     */
    public WnnWord getWnnWord(int index) {
        WnnWord word = null;
        if (index < 0) {
            index = 0;
            mHandler.removeMessages(MSG_MOVE_FOCUS);
            Log.i("iwnn", "TextCandidatesViewManager::getWnnWord  index < 0 ");
        } else {
            int size = mHasFocusedArray1st ? mWnnWordArray1st.size() : mWnnWordArray2nd.size();
            if (index >= size) {
                index = size - 1;
                mHandler.removeMessages(MSG_MOVE_FOCUS);
                Log.i("iwnn", "TextCandidatesViewManager::getWnnWord  index > candidate max ");
            }
        }

        if (mHasFocusedArray1st) {
            word = mWnnWordArray1st.get(index);
        } else {
            word = mWnnWordArray2nd.get(index);
        }
        return word;
    }

    /**
     * Get get First WnnWord.
     *
     * @return WnnWord word
     */
    public WnnWord getFirstWnnWord() {
        return mWnnWordArray1st.get(0);
    }

    /**
     * Set display candidate line from SharedPreferences.
     */
    private void setNumeberOfDisplayLines(){
        Resources res = mWnn.getResources();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OpenWnn.superGetContext());
        mPortraitNumberOfLine = Integer.parseInt(pref.getString("setting_portrait", res.getString(R.string.setting_portrait_default_value)));
        mLandscapeNumberOfLine = Integer.parseInt(pref.getString("setting_landscape", res.getString(R.string.setting_landscape_default_value)));
    }

    /**
     * Set emoji enabled.
     */
    public void setEnableEmoji(boolean enableEmoji) {
        mEnableEmoji = enableEmoji;
    }

    /**
     * Set decoemoji enabled.
     */
    public void setEnableDecoEmoji(boolean enableDecoEmoji) {
        mEnableDecoEmoji = enableDecoEmoji;
    }

    /**
     * Set emoticon enabled.
     */
    public void setEnableEmoticon(boolean enableEmoticon) {
        mEnableEmoticon = enableEmoticon;
    }

    /**
     * Set emojiuni6 enabled.
     */
    public void setEnableEmojiUNI6(boolean enableEmojiUNI6) {
        mEnableEmojiUNI6 = enableEmojiUNI6;
    }

    /**
     * Get View of focus candidate.
     */
    public TextView getFocusedView() {
        if (mCurrentFocusIndex == FOCUS_NONE) {
            return null;
        }
        TextView t;
        if (mHasFocusedArray1st) {
            t = mTextViewArray1st.get(mCurrentFocusIndex);
        } else {
            t = mTextViewArray2nd.get(mCurrentFocusIndex);
        }
        return t;
    }

    /**
     * Move the focus to next candidate.
     *
     * @param direction  The direction of increment or decrement.
     * @param updown     {@code true} if move is up or down.
     */
    public boolean moveFocus(int direction, boolean updown) {
        boolean hasReversed = false;
        boolean isStart = (mCurrentFocusIndex == FOCUS_NONE);
        if (direction == 0) {
            setViewStatusOfFocusedCandidate();
        }

        int size1st = mTextViewArray1st.size();
        if (mHasFocusedArray1st && (size1st == 0)) {
            mHasFocusedArray1st = false;
        }
        ArrayList<TextView> list = mHasFocusedArray1st ? mTextViewArray1st : mTextViewArray2nd;
        int size = list.size();
        int index = -1;
        boolean hasChangedLine = false;
        if (direction != DIRECTION_TO_HOME && direction != DIRECTION_TO_END) {
            int start = (mCurrentFocusIndex == FOCUS_NONE) ? 0 : (mCurrentFocusIndex + direction);

            // Search next focus.
            for (int i = start; (0 <= i) && (i < size); i += direction) {
                TextView view = list.get(i);
                if (!view.isShown()) {
                    break; // Out of bounds.
                }

                if (mIsSymbolMode && (view.getCurrentTextColor() == -1)) {
                    continue; // Category of symbols.
                }

                if (updown) {
                    int left = view.getLeft();
                    if ((left <= mFocusAxisX)
                            && (mFocusAxisX < view.getRight())) {
                        index = i;
                        break;
                    }

                    if (left == 0) {
                        hasChangedLine = true;
                    }
                } else {
                    index = i;
                    break;
                }
            }
        } else if (direction == DIRECTION_TO_HOME) {
            if (mHasFocusedArray1st == false) {
                mHasFocusedArray1st = true;
            }
            if (size1st == 0) {
                mHasFocusedArray1st = false;
                index = 1;
            } else {
                index = 0;
            }
            mFocusAxisX = 0;
        } else if (direction == DIRECTION_TO_END) {
            if(mHasFocusedArray1st == true) {
                mHasFocusedArray1st = false;
            }
            index = mTextViewArray2nd.size() - 1; // Set focus to last candidate.
            if (index < 0 ) {
                index = mTextViewArray1st.size() - 1; // Set focus to last candidate.
                mHasFocusedArray1st = true;
            }
        }

        if ((index < 0) && hasChangedLine && !mHasFocusedArray1st && (0 < direction)) {
            index = mTextViewArray2nd.size() - 1; // Set focus to last candidate.
        }

        if (0 <= index) {
            // Next focus has found.
            mCurrentFocusIndex = index;
            setViewStatusOfFocusedCandidate();
            if (!updown) {
                TextView tv = getFocusedView();
                if (tv != null) {
                    mFocusAxisX = tv.getLeft();
                }
            }
        } else {
            // Not found.
            if (mCanReadMore && (0 < size1st)) {
                // Focus out of the list.

                if ((mHasFocusedArray1st && (direction < 0))
                        || (!mHasFocusedArray1st && (0 < direction))) {
                    updown = false; // Has wrapped. Focus will be first or last.
                    hasReversed = true;
                }

                mHasFocusedArray1st = !mHasFocusedArray1st;

                // Set to full list if the focus move from 1st to 2nd.
                if (!mHasFocusedArray1st && !mIsFullView) {
                    displayCandidates(mConverter, false, -1);
                    mViewCandidateList2nd.setVisibility(View.VISIBLE);
                }
            }

            if (size1st == 0) {
                updown = false; // Has wrapped. Focus will be first or last.
                hasReversed = true;
            }

            if (0 < direction) {
                mCurrentFocusIndex = -1;
            } else {
                mCurrentFocusIndex = (mHasFocusedArray1st ? size1st : mTextViewArray2nd.size());
            }
            Message m = mHandler.obtainMessage(MSG_MOVE_FOCUS, direction, updown ? 1 : 0);
            mHandler.sendMessage(m);
        }

        if (mCurrentFocusIndex != -1) {
            // Conditions to adjust the scroll position
            // 1. And if there is no 1stview, in the first line of focus position is 2ndview
            // 2. And if the focus is on the 1stview, in the first line of focus position is 1stview
            if (((size1st == 0) && (mCurrentFocusIndex <= m2ndView1stLineLastIndex)) ||
                (mHasFocusedArray1st && (mCurrentFocusIndex <= m1stView1stLineLastIndex))) {
                if (m1stIndexCategoryNo > INIT_CONSTANT_FIRST_INDEX_CATEGORY_CNT) {
                    mViewBodyScroll.scrollTo(0, mCategoryYposArrayList.get(m1stIndexCategoryNo).intValue());
                }
            }
        }

        if (isStart) {
            if (mWnn instanceof IWnnLanguageSwitcher) {
                ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.FOCUS_CANDIDATE_START));
            }
        }
        return hasReversed;
    }

    /** @see CandidatesViewManager#scrollPageAndUpdateFocus */
    public void scrollPageAndUpdateFocus(boolean scrollDown) {

        // if not focus, call "moveFocus()" only once
        if (!isFocusCandidate()) {
            moveFocus(1, true);
            return;
        }

        final int direction = scrollDown? 1 : -1;
        final int pageSize = mViewBodyScroll.getHeight();
        final int height = getCandidateMinimumHeight();
        int moveCnt = pageSize / height;
        mHandler.removeMessages(MSG_MOVE_FOCUS);
        mFocusAxisX = 0;
        TextView view = getFocusedView();
        if (!scrollDown && (view != null) && (view.getLeft() != 0)) {
            moveCnt++;
        }
        for (int i = 0; i < moveCnt; i++) {
            boolean preHasFocusedArray1st = mHasFocusedArray1st;
            int preFocusIndex = mCurrentFocusIndex;
            boolean reverse = moveFocus(direction, true);
            if (mHandler.hasMessages(MSG_MOVE_FOCUS)) {
                mHandler.removeMessages(MSG_MOVE_FOCUS);
                moveFocus(direction, true);
            }
            // if focus reversed (moved from top to end or moved from end to top),
            // stop moving focus
            if (reverse) {
                // if "moveFocus()" was called twice or more, move back focus
                if (i > 0) {
                    mHasFocusedArray1st = preHasFocusedArray1st;
                    mCurrentFocusIndex = preFocusIndex;
                    setViewStatusOfFocusedCandidate();
                }
                break;
            }
        }

        if (mCurrentFocusIndex != -1) {
            // Conditions to adjust the scroll position
            // 1. And if there is no 1stview, in the first line of focus position is 2ndview
            // 2. And if the focus is on the 1stview, in the first line of focus position is 1stview
            if (((mTextViewArray1st.size() == 0) && (mCurrentFocusIndex <= m2ndView1stLineLastIndex)) ||
                (mHasFocusedArray1st && (mCurrentFocusIndex <= m1stView1stLineLastIndex))) {
                if (m1stIndexCategoryNo > INIT_CONSTANT_FIRST_INDEX_CATEGORY_CNT) {
                    mViewBodyScroll.scrollTo(0, mCategoryYposArrayList.get(m1stIndexCategoryNo).intValue());
                }
            }
        }
    }

    /**
     * WebAPI error.
     *
     * @param errorCode error code.
     */
    public void onWebApiError(String errorCode) {
        if (mWebApiButton != null) {
            mWebApiButton.setText(mWnn.getResources().getString(R.string.ti_webapi_button_txt));
            if (errorCode != null) {
                Toast.makeText(OpenWnn.superGetContext(), errorCode, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Select WebAPI button.
     */
    private void selectWebApiButton() {
        if (mWebApiButton != null) {
            mWebApiButton.setText(mWnn.getResources().getString(R.string.ti_webapi_connect_txt));
            if (mWnn instanceof IWnnLanguageSwitcher) {
                ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.SELECT_WEBAPI));
            }
        }
    }

    /**
     * Select WebAPI get again button.
     */
    private void selectWebApiGetAgainButton() {
        mWebApiButton.setText(mWnn.getResources().getString(R.string.ti_webapi_connect_txt));
        if (mWnn instanceof IWnnLanguageSwitcher) {
            ((OpenWnn)mWnn).onEvent(new OpenWnnEvent(OpenWnnEvent.SELECT_WEBAPI_GET_AGAIN));
        }
    }

    /**
     * Set hardkeyboard hidden.
     *
     * @param hardKeyboardHidden hardkeyaboard hidden.
     */
    public void setHardKeyboardHidden(boolean hardKeyboardHidden) {
        mHardKeyboardHidden = hardKeyboardHidden;
    }

    /**
     * Get view top position on screen.
     *
     * @param view target view.
     * @return int view top position on screen
     */
    public int getViewTopOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[1];
    }

    /**
     * Get information if can read more candidates
     *
     * @return boolean information if can read more candidates
     */
    public boolean getCanReadMore() {
        return mCanReadMore;
    }

    /** @see CandidatesViewManager#setEnableCandidateLongClick */
    public void setEnableCandidateLongClick(boolean enable) {
        mEnableCandidateLongClick = enable;
    }

    /** @see CandidatesViewManager#setCandidateMsgRemove */
    public void setCandidateMsgRemove() {
        mHandler.removeMessages(MSG_MOVE_FOCUS);
        mHandler.removeMessages(MSG_SET_CANDIDATES);
        mHandler.removeMessages(MSG_MORE_DISPLAY_CANDIDATES);
    }

    public ArrayList<TextView> getTextViewArray1st() {
        return mTextViewArray1st;
    }

    /**
     * Check DecoEmoji candidates
     *
     * @return boolean true if candidates contain DecoEmoji
     */
    public boolean checkDecoEmoji() {
        boolean check = false;
        if (mAddViewList != null) {
            for (int i = 0; i < mAddViewList.size(); i++) {
                Spannable text = new SpannableString(mAddViewList.get(i).getText());
                int ret = EmojiAssist.getInstance().checkTextData(text);
                if (ret >= EmojiAssist.TYPE_TEXT_DECOEMOJI && mWnn.isEmoji()) {
                    check = true;
                    break;
                }
            }
        }
        return check;
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
     * Set candidate view background.
     *
     * @param view target view.
     * @param resourceId drawable resource id.
     */
    private void setCandidateBackGround(View view, int resourceId) {
        KeyboardResourcesDataManager resMan = KeyboardResourcesDataManager.getInstance();
        Drawable drawable = null;

        switch (resourceId) {
        case R.drawable.cand_back:
                if (mIsSymbolMode) {
                    drawable = resMan.getCandidateBackgroundSymbol();
                } else {
                    drawable = resMan.getCandidateBackground();
                }
                break;

            case R.drawable.cand_back_jojo:
                drawable = resMan.getCandidateBackgroundJoJo();
            break;

        case R.drawable.cand_back_webapi:
            drawable = resMan.getCandidateBackgroundWebApi();
            break;

        case R.drawable.cand_back_focuse:
            if (isFocusCandidate()) {
                WnnWord word = getFocusedWnnWord();
                if ((word != null)
                        && (((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_NO_CANDIDATE) != 0)
                        || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI) != 0)
                        || ((word.attribute & iWnnEngine.WNNWORD_ATTRIBUTE_WEBAPI_GET_AGAIN) != 0))) {
                    drawable = resMan.getCandidateFocusBackgroundWebApi();
                }
            }
            if (drawable == null) {
                drawable = resMan.getCandidateFocusBackground();
            }
            break;

        default:
            break;

        }

        if (drawable != null) {
            view.setBackgroundDrawable(drawable);
        } else {
            if ((resourceId == R.drawable.cand_back) && mIsSymbolMode) {
                resourceId = R.drawable.cand_back_symbol;
            }
            view.setBackgroundResource(resourceId);
        }
    }

    /**
     * Creates the Tab of additional symbol list.
     *
     * @param name  The tab name.
     */
    private TextView createTab(String name) {
        TextView tab = new TextView(mViewBodyScroll.getContext());
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                mWnn.getResources().getDimensionPixelSize(R.dimen.tab_text_size));
        tab.setText(name);
        tab.setBackgroundResource(R.drawable.cand_tab);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        tab.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                                          ViewGroup.LayoutParams.FILL_PARENT,
                                                          1.0f));
        tab.setSoundEffectsEnabled(false);
        tab.setEllipsize(TextUtils.TruncateAt.END);
        tab.setOnClickListener(mTabOnClick);
        return tab;
    }

    /**
     * Update tab status of additional symbol list.
     */
    private void updateAdditionalSymbolTabStatus() {
        int current = ((IWnnSymbolEngine)mConverter).getAdditionalSymbolIndex();
        for (int i = 0; i < mAddSymbolTabList.size(); i++) {
            boolean selected
                    = ((mSymbolMode == IWnnSymbolEngine.MODE_ADD_SYMBOL) && (i == current));
            updateTabStatus(mAddSymbolTabList.get(i), true, true, selected);
        }
    }

    /**
     * Cancel last MSG_SET_CANDIDATES event.
     */
    public void cancelLastSetCandiateEvent(){
        if (mConverter instanceof IWnnSymbolEngine) {
            boolean isLastNextButton = ((IWnnSymbolEngine) mConverter).isIsLastNextButton();
            if (isLastNextButton) {
                createNextLine(false);
                mCreateCandidateDone = true;
                mHandler.removeMessages(MSG_SET_CANDIDATES);
            }
        }
    }

    /**
     * Set background for candidates list view .
     */
    private void setCandidateListViewBg() {
        KeyboardSkinData keyskin = KeyboardSkinData.getInstance();
        if (mIsSymbolMode) {
            if (keyskin.isValid()) {
                mViewBodyScroll.setBackground(keyskin.getDrawable(R.drawable.ime_keypad_symbol_btn_normal));
                mViewCandidateBaseBg.setBackground(keyskin.getDrawable(R.drawable.ime_keypad_symbol_bg));
            } else {
                mViewBodyScroll.setBackground(mWnn.getResources().getDrawable(R.drawable.ime_keypad_symbol_btn_normal));
                mViewCandidateBaseBg.setBackground(mWnn.getResources().getDrawable(R.drawable.ime_keypad_symbol_bg));
            }
            mViewCandidateBaseBg.setPadding(mWnn.getResources().getDimensionPixelSize(R.dimen.cand_base_view_padding_symbol_left),
                    0,
                    mWnn.getResources().getDimensionPixelSize(R.dimen.cand_base_view_padding_symbol_right),
                    0);
        } else {
            if (keyskin.isValid()) {
                mViewBodyScroll.setBackground(keyskin.getDrawable(R.drawable.cand_back_normal));
                mViewCandidateBaseBg.setBackground(keyskin.getDrawable(R.drawable.ime_keypad_candidate_bg));
            } else {
                mViewBodyScroll.setBackground(mWnn.getResources().getDrawable(R.drawable.cand_back_normal));
                mViewCandidateBaseBg.setBackground(mWnn.getResources().getDrawable(R.drawable.ime_keypad_candidate_bg));
            }
            if (mIsFullView) {
                mViewCandidateBaseBg.setPadding(mWnn.getResources().getDimensionPixelSize(R.dimen.cand_base_view_padding_left),
                        mCandidateBgViewPaddingTop,
                        mWnn.getResources().getDimensionPixelSize(R.dimen.cand_base_view_padding_right),
                        0);
            } else {
                mViewCandidateBaseBg.setPadding(mWnn.getResources().getDimensionPixelSize(R.dimen.cand_base_view_padding_left),
                        mCandidateBgViewPaddingTop,
                        mWnn.getResources().getDimensionPixelSize(R.dimen.cand_base_view_padding_right),
                        mCandidateBgViewPaddingBottom);
            }
        }
    }

    /**
     * Set focus for candidate.
     *
     * @param index  The index of focus.
     */
    public void setFocusCandidate(int index) {
        if (index < mTextViewArray1st.size()) {
            mHasFocusedArray1st = true;
            mCurrentFocusIndex = index;
            setViewStatusOfFocusedCandidate();
        } else if ((index - mTextViewArray1st.size()) < mTextViewArray2nd.size()) {
            mHasFocusedArray1st = false;
            mCurrentFocusIndex = index - mTextViewArray1st.size();
            setViewStatusOfFocusedCandidate();
        } //else
    }

//* for debug. */
    /**
     * Set WnnEngine.
     */
    public void setWnnEngine(WrapWnnEngine engine){
        mConverter = engine;
    }
}
