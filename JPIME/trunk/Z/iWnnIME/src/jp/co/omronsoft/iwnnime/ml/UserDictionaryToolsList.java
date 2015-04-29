/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint.FontMetricsInt;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;


/**
 * The abstract class for user dictionary tool.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public abstract class UserDictionaryToolsList extends Activity
    implements View.OnClickListener, OnTouchListener, OnFocusChangeListener {

    /** The maximum count of registered words */
    private static boolean started = false;

    /** The maximum count of registered words */
    public static final int MAX_WORD_COUNT = 500;

    /** The class name of the user dictionary editor */
    protected String  mEditViewName;
    /** The package name of the user dictionary editor */
    protected String  mPackageName;
    /** The interface of a engine for an user dictionary. */
    protected UserDictionaryToolsEngineInterface mEngineInterface;

    /** ID of the menu item (add) */
    private static final int MENU_ITEM_ADD = 0;
    /** ID of the menu item (edit) */
    private static final int MENU_ITEM_EDIT = 1;
    /** ID of the menu item (delete) */
    private static final int MENU_ITEM_DELETE = 2;
    /** ID of the menu item (initialize) */
    private static final int MENU_ITEM_INIT = 3;

    /** ID of the dialog control (confirm deletion) */
    private static final int DIALOG_CONTROL_DELETE_CONFIRM = 0;
    /** ID of the dialog control (confirm initialize) */
    private static final int DIALOG_CONTROL_INIT_CONFIRM = 1;

    /** The size of font*/
    private static final int WORD_TEXT_SIZE = 16;

    /** The color of background (unfocused item) */
    private static final int UNFOCUS_BACKGROUND_COLOR = 0xFF242424;
    /** The color of background (focused item) */
    private static final int FOCUS_BACKGROUND_COLOR = 0xaa33b5e5;

    /** The minimum count of registered words */
    private static final int MIN_WORD_COUNT = 0;
    /** Maximum word count to display */
    private static final int MAX_LIST_WORD_COUNT = 50;

    /** The threshold time of the double tapping */
    private static final int DOUBLE_TAP_TIME = 300;

    /** Widgets which constitute this screen of activity */
    private Menu mMenu;
    /** Table layout for the lists */
    private TableLayout mTableLayout;
    /** Focusing view */
    private UserDictionaryToolsListFocus mFocusingView = null;
    /** Focusing pair view */
    private UserDictionaryToolsListFocus mFocusingPairView = null;

    /** The number of the registered words */
    private int mWordCount = 0;

    /** The state of "Add" menu item */
    private boolean mAddMenuEnabled;
    /** The state of "Edit" menu item */
    private boolean mEditMenuEnabled;
    /** The state of "Delete" menu item */
    private boolean mDeleteMenuEnabled;
    /** The state of "Initialize" menu item */
    private boolean mInitMenuEnabled;

    /** {@code true} if the menu option is initialized */
    private boolean mInitializedMenu = false;
    /** {@code true} if one of words is selected */
    private boolean mSelectedWords;
    /** The viewID which is selected */
    private int mSelectedViewID = -1;
    /** The viewID which was selected previously */
    private static int sBeforeSelectedViewID = -1;
    /** The time of previous action */
    private static long sJustBeforeActionTime = -1;

    /** List of the words in the user dictionary */
    private ArrayList<WnnWord> mWordList = null;

    /** Work area for sorting the word list */
    private WnnWord[] mSortData;

    /** Whether the view is initialized */
    private boolean mInit = false;

    /** Page left button */
    private Button mLeftButton = null;

    /** Page right button */
    private Button mRightButton = null;

    /** Get the comparator for sorting the list. */
    protected abstract Comparator<WnnWord> getComparator();

    /** Whether the current language has no reading. */
    protected boolean mIsNoStroke = false;

    /** Show Dialog Num */
    private int mDialogShow = -1;

    /** HashMap for Locale to choose langage title string conversion */
    private static final HashMap<Integer, Integer> LOCALE_CHOOSELANG_TITLE_MAP = new HashMap<Integer, Integer>() {{
        put(iWnnEngine.LanguageType.ENGLISH_US,          R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.ENGLISH_UK,          R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.GERMAN,              R.string.ti_preference_dictionary_menu_de_txt);
        put(iWnnEngine.LanguageType.ITALIAN,             R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.FRENCH,              R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.SPANISH,             R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.DUTCH,               R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.POLISH,              R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.RUSSIAN,             R.string.ti_preference_dictionary_menu_ru_txt);
        put(iWnnEngine.LanguageType.SWEDISH,             R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.NORWEGIAN_BOKMAL,    R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.CZECH,               R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.SIMPLIFIED_CHINESE,  R.string.ti_preference_dictionary_menu_zhcn_txt);
        put(iWnnEngine.LanguageType.TRADITIONAL_CHINESE, R.string.ti_preference_dictionary_menu_zhtw_txt);
        put(iWnnEngine.LanguageType.JAPANESE,            R.string.ti_preference_dictionary_menu_ja_txt);
        put(iWnnEngine.LanguageType.PORTUGUESE,          R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.CANADA_FRENCH,       R.string.ti_preference_dictionary_menu_en_txt);
        put(iWnnEngine.LanguageType.KOREAN,              R.string.ti_preference_dictionary_menu_ko_txt);
        put(iWnnEngine.LanguageType.ENGLISH,             R.string.ti_preference_dictionary_menu_en_txt);
    }};

    /**
     * Default constructor
     */
    public UserDictionaryToolsList() {
        super();
    }

    /**
     * Called when the activity is starting.
     *
     * @see android.app.Activity#onCreate
     */
    @Override protected void onCreate(Bundle savedInstanceState) {

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreate() : start.");}

        super.onCreate(savedInstanceState);

        UserDictionaryToolsList.started = true;

        /* create XML layout */
        setContentView(R.layout.user_dictionary_tools_list);
        mTableLayout = (TableLayout)findViewById(R.id.user_dictionary_tools_table);

        Button b = (Button)findViewById(R.id.user_dictionary_left_button);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = mWordCount - MAX_LIST_WORD_COUNT;
                    if (0 <= pos) {
                        mWordCount = pos;
                        mSelectedViewID = getInitViewId();
                        updateWordList();
                    }
                }
            });
        mLeftButton = b;

        b = (Button)findViewById(R.id.user_dictionary_right_button);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = mWordCount + MAX_LIST_WORD_COUNT;
                    if (pos < mWordList.size()) {
                        mWordCount = pos;
                        mSelectedViewID = getInitViewId();
                        updateWordList();
                    }
                }
            });
        mRightButton = b;

        if (mIsNoStroke) {
            findViewById(R.id.user_dictionary_title_read).setVisibility(View.GONE);
        }

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreate() : end.");}
    }

    /**
     * Called after onCreate(Bundle) ? or after onRestart() when the activity had been stopped,
     * but is now again being displayed to the user.
     *
     * @see android.app.Activity#onStart
     */
    @Override protected void onStart() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onStart()");}
        super.onStart();
        mDialogShow = -1;
        sBeforeSelectedViewID = -1;
        sJustBeforeActionTime = -1;
        mWordList = mEngineInterface.getWords();
        mWordCount = 0;
        sortList(mWordList);

        final TextView leftText = (TextView) findViewById(R.id.user_dictionary_tools_list_title_words_count);
        leftText.setText(mWordList.size() + "/" + MAX_WORD_COUNT);

        mSelectedViewID = getInitViewId();
        updateWordList();
        if (mInitializedMenu && mWordList.size() >= MIN_WORD_COUNT) {
            onCreateOptionsMenu(mMenu);
        }
    }
    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @see android.app.Activity#onStop
     */
    @Override protected void onPause() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onPause()");}

        if (mDialogShow == DIALOG_CONTROL_DELETE_CONFIRM) {
            dismissDialog(DIALOG_CONTROL_DELETE_CONFIRM);
            mDialogShow = -1;
        } else if (mDialogShow == DIALOG_CONTROL_INIT_CONFIRM){
            dismissDialog(DIALOG_CONTROL_INIT_CONFIRM);
            mDialogShow = -1;
        }

        super.onPause();
    }

    /**
     * Called when you are no longer visible to the user.
     *
     * @see android.app.Activity#onStop
     */
    @Override protected void onStop() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onStop()");}
        super.onStop();
    }

    /**
     * Perform any final cleanup before an activity is destroyed.
     *
     * @see android.app.Activity#onDestroy
     */
    @Override protected void onDestroy() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onDestroy()");}
        super.onDestroy();
        UserDictionaryToolsList.started = false;
    }

    /**
     * Create the parameter table, See {@link android.widget.TableLayout.LayoutParams}.
     *
     * @param  w        The width of the table
     * @param  h        The height of the table
     * @return          The information of the layout
     */
    private TableLayout.LayoutParams tableCreateParam(int w, int h) {
        return new TableLayout.LayoutParams(w, h);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @see android.app.Activity#onCreateOptionsMenu
     */
    @Override public boolean onCreateOptionsMenu(Menu menu) {

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreateOptionsMenu() : start.");}

        /* initialize the menu */
        menu.clear();
        /* set the menu item enable/disable */
        setOptionsMenuEnabled();
        /* [menu] add a word */
        menu.add(0, MENU_ITEM_ADD, 0, R.string.ti_user_dictionary_add_txt)
            .setIcon(android.R.drawable.ic_menu_add)
            .setEnabled(mAddMenuEnabled);
        /* [menu] edit a word */
        menu.add(0, MENU_ITEM_EDIT, 0, R.string.ti_user_dictionary_edit_txt)
            .setIcon(android.R.drawable.ic_menu_edit)
            .setEnabled(mEditMenuEnabled);
        /* [menu] delete a word */
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.ti_user_dictionary_delete_txt)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setEnabled(mDeleteMenuEnabled);
        /* [menu] clear the dictionary */
        menu.add(1, MENU_ITEM_INIT, 0, R.string.ti_user_dictionary_init_txt)
            .setIcon(android.R.drawable.ic_menu_delete)
            .setEnabled(mInitMenuEnabled);

        mMenu = menu;
        mInitializedMenu = true;

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreateOptionsMenu() : end.");}

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Change state of the option menus according to a current state of the list widget
     */
    private void setOptionsMenuEnabled() {

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "setOptionsMenuEnabled() : start.");}

        /* [menu] add a word */
        if (mWordList.size() >= MAX_WORD_COUNT) {
            /* disable if the number of registered word exceeds MAX_WORD_COUNT */
            mAddMenuEnabled = false;
        } else {
            mAddMenuEnabled = true;
        }

        /* [menu] edit a word/delete a word */
        if (mWordList.size() <= MIN_WORD_COUNT) {
            /* disable if no word is registered or no word is selected */
            mEditMenuEnabled = false;
            mDeleteMenuEnabled = false;
            mInitMenuEnabled = false;
        } else {
            if (mSelectedWords) {
                mEditMenuEnabled = true;
                mDeleteMenuEnabled = true;
            } else {
                mEditMenuEnabled = false;
                mDeleteMenuEnabled = false;
            }
            mInitMenuEnabled = true;
        }

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "setOptionsMenuEnabled() : end.");}
    }

    /**
     * Called whenever an item in your options menu is selected.
     *
     * @see android.app.Activity#onOptionsItemSelected
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        boolean ret;
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onOptionsItemSelected() : start.");}
        switch (item.getItemId()) {
        case MENU_ITEM_ADD:
            /* add a word */
            addWord();
            ret = true;
            break;

        case MENU_ITEM_EDIT:
            /* edit the word (show dialog) */
            editWord();
            ret = true;
            break;

        case MENU_ITEM_DELETE:
            /* delete the word (show dialog) */
            showDialog(DIALOG_CONTROL_DELETE_CONFIRM);
            mDialogShow = DIALOG_CONTROL_DELETE_CONFIRM;
            ret = true;
            break;

        case MENU_ITEM_INIT:
            /* clear the dictionary (show dialog) */
            showDialog(DIALOG_CONTROL_INIT_CONFIRM);
            mDialogShow = DIALOG_CONTROL_INIT_CONFIRM;
            ret = true;
            break;

        default:
            ret = false;
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onOptionsItemSelected() : end.");}

        return ret;
    }

    /**
     * Called when a key was released and not handled by any of the views inside of the activity.
     *
     * @see android.app.Activity#onKeyUp
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        /* open the menu if KEYCODE_DPAD_CENTER is pressed */
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Callback for creating dialogs that are managed (saved and restored) for you by the activity.
     *
     * @see android.app.Activity#onCreateDialog
     */
    @Override protected Dialog onCreateDialog(int id) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreateDialog() : start.");}
        switch (id) {
        case DIALOG_CONTROL_DELETE_CONFIRM:
            return new AlertDialog.Builder(UserDictionaryToolsList.this)
                .setMessage(R.string.ti_user_dictionary_delete_confirm_txt)
                .setNegativeButton(R.string.ti_dialog_button_cancel_txt, null)
                .setPositiveButton(R.string.ti_dialog_button_ok_txt, mDialogDeleteWords)
                .setCancelable(true)
                .create();

        case DIALOG_CONTROL_INIT_CONFIRM:
            return new AlertDialog.Builder(UserDictionaryToolsList.this)
                .setMessage(R.string.ti_dialog_delete_user_dictionary_message_txt)
                .setNegativeButton(R.string.ti_dialog_button_cancel_txt, null)
                .setPositiveButton(R.string.ti_dialog_button_ok_txt, mDialogInitWords)
                .setCancelable(true)
                .create();

        default:
            Log.e("OpenWnn", "onCreateDialog : Invaled Get DialogID. ID=" + id);
            break;
        }

        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onCreateDialog() : end.");}

        return super.onCreateDialog(id);
    }


    /**
     * Called when a view has been clicked.
     *
     * @see android.view.View.OnClickListener#onClick
     */
    private DialogInterface.OnClickListener mDialogDeleteWords =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onClick() : start.");}

                mDialogShow = -1;
                CharSequence focusString = mFocusingView.getText();
                WnnWord wnnWordSearch = new WnnWord();

                CharSequence focusPairString = mFocusingPairView.getText();
                if (mSelectedViewID > MAX_WORD_COUNT) {
                    wnnWordSearch.stroke = focusPairString.toString();
                    wnnWordSearch.candidate = focusString.toString();
                } else {
                    wnnWordSearch.stroke = focusString.toString();
                    wnnWordSearch.candidate = focusPairString.toString();
                }
                boolean deleted = mEngineInterface.deleteWord(wnnWordSearch);
                if (deleted) {
                    Toast.makeText(getApplicationContext(),
                                   R.string.ti_user_dictionary_delete_complete_txt,
                                   Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                                   R.string.ti_user_dictionary_delete_fail_txt,
                                   Toast.LENGTH_SHORT).show();
                    return;
                }

                mWordList = mEngineInterface.getWords();
                sortList(mWordList);
                int size = mWordList.size();
                if (size <= mWordCount) {
                    int newPos = (mWordCount - MAX_LIST_WORD_COUNT);
                    mWordCount = (0 <= newPos) ? newPos : 0;
                }
                updateWordList();

                TextView leftText = (TextView) findViewById(R.id.user_dictionary_tools_list_title_words_count);
                leftText.setText(size + "/" + MAX_WORD_COUNT);

                if (mInitializedMenu) {
                    onCreateOptionsMenu(mMenu);
                }
            }
        };

    /**
     * Called when a view has been clicked.
     *
     * @see android.view.View.OnClickListener#onClick
     */
    private DialogInterface.OnClickListener mDialogInitWords =
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {

                if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onClick() : start.");}
                mDialogShow = -1;
                /* clear the user dictionary */
                mEngineInterface.initializeDictionary();

                /* show the message */
                Toast.makeText(getApplicationContext(), R.string.ti_user_dictionary_all_delete_complete_txt,
                               Toast.LENGTH_SHORT).show();
                mWordList = new ArrayList<WnnWord>();
                mWordCount = 0;
                updateWordList();
                TextView leftText = (TextView) findViewById(R.id.user_dictionary_tools_list_title_words_count);
                leftText.setText(mWordList.size() + "/" + MAX_WORD_COUNT);

                if (mInitializedMenu) {
                    onCreateOptionsMenu(mMenu);
                }
                if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onClick() : end.");}
            }
        };

    /**
     * Called when a view has been clicked.
     *
     * @see android.view.View.OnClickListener#onClick
     */
    public void onClick(View arg0) {
        // do nothing; editing handled by afterTextChanged()
    }

    /**
     * Called when a touch event is dispatched to a view.
     *
     * @see android.view.View.OnTouchListener#onTouch
     */
    public boolean onTouch(View v, MotionEvent e) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onTouch() : start.");}

        if (v instanceof UserDictionaryToolsListFocus) {
            UserDictionaryToolsListFocus view = (UserDictionaryToolsListFocus)v;

            switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /* double tap handling */
                if (sBeforeSelectedViewID != view.getId()) {
                    /* save the view id if the id is not same as previously selected one */
                    sBeforeSelectedViewID = view.getId();
                } else {
                    if ((e.getDownTime() - sJustBeforeActionTime) < DOUBLE_TAP_TIME) {
                        /* edit the word if double tapped */
                        mFocusingView = view;
                        mFocusingPairView = view.getPairView();
                        editWord();
                    }
                }
                /* save the action time */
                sJustBeforeActionTime = e.getDownTime();
                break;
            case MotionEvent.ACTION_UP:
                mSelectedViewID = view.getId();
                break;
            default:
                ; // nothing to do.
                break;
            }
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onTouch() : end.");}

        return false;
    }

    /**
     * Called when the focus state of a view has changed.
     *
     * @see android.view.View.OnFocusChangeListener#onFocusChange
     */
    public void onFocusChange(View v, boolean hasFocus) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onFocusChange() : start.");}

        if (v instanceof UserDictionaryToolsListFocus) {
            UserDictionaryToolsListFocus view = (UserDictionaryToolsListFocus)v;
            UserDictionaryToolsListFocus pairView = view.getPairView();

            mSelectedViewID = view.getId();
            mFocusingView = view;
            mFocusingPairView = pairView;
            if (hasFocus) {
                if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "Focused view");}
                view.setTextColor(Color.BLACK);
                view.setBackgroundColor(FOCUS_BACKGROUND_COLOR);
                if (!mIsNoStroke) {
                    pairView.setTextColor(Color.BLACK);
                    pairView.setBackgroundColor(FOCUS_BACKGROUND_COLOR);
                }
                mSelectedWords = true;
            } else {
                mSelectedWords = false;
                view.setTextColor(Color.BLACK);
                view.setBackgroundDrawable(null);
                if (!mIsNoStroke) {
                    pairView.setTextColor(Color.BLACK);
                    pairView.setBackgroundDrawable(null);
                }
            }
            if (mInitializedMenu) {
                onCreateOptionsMenu(mMenu);
            }
        }
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "onFocusChange() : end.");}
    }

    /**
     * Add the word.
     */
    public void addWord() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "addWord() : start.");}
        /** change to the edit window */
        openEditScreen(Intent.ACTION_INSERT, "", "");
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "addWord() : end.");}
    }

    /**
     * Edit the specified word.
     */
    public void editWord() {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "editWord() : start.");}
        String stroke;
        String candidate;
        if (mSelectedViewID > MAX_WORD_COUNT) {
            candidate = mFocusingView.getText().toString();
            stroke = mFocusingPairView.getText().toString();
        } else {
            stroke = mFocusingView.getText().toString();
            candidate = mFocusingPairView.getText().toString();
        }
        openEditScreen(Intent.ACTION_EDIT, stroke, candidate);
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "editWord() : end.");}
    }

    /**
     * Open the screen of edit.
     *
     * @param  action       The string of action
     * @param  stroke       The stroke of word
     * @param  candidate    The candidate of word
     */
    private void openEditScreen(String action, String stroke, String candidate) {
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "openEditScreen() : start.");}

        Intent intent = new Intent(action);
        intent.setClassName(mPackageName, mEditViewName);
        intent.putExtra("stroke", stroke);
        intent.putExtra("candidate", candidate);
        startActivity(intent);
        if (OpenWnn.isDebugging()) {Log.d("OpenWnn", "openEditScreen() : end.");}
    }

    /**
     * Sort the list of words.
     *
     * @param array The array list of the words
     */
    protected void sortList(ArrayList<WnnWord> array) {
        mSortData = new WnnWord[array.size()];
        array.toArray(mSortData);
        Arrays.sort(mSortData, getComparator());
    }


    /**
     * Update the word list.
     */
    private void updateWordList() {
        if (!mInit) {
            mInit = true;
            mSelectedViewID = getInitViewId();

            Window window = getWindow();
            WindowManager windowManager = window.getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            int system_width = display.getWidth();

            UserDictionaryToolsListFocus dummy = new UserDictionaryToolsListFocus(this);
            dummy.setTextSize(WORD_TEXT_SIZE);
            TextPaint paint = dummy.getPaint();
            FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            int row_hight = (Math.abs(fontMetrics.top) + fontMetrics.bottom) * 2;

            Resources res = getResources();
            for (int i = 1; i <= MAX_LIST_WORD_COUNT; i++) {
                TableRow row = new TableRow(this);
                UserDictionaryToolsListFocus stroke = new UserDictionaryToolsListFocus(this);
                stroke.setId(i);
                stroke.setWidth(system_width/2);
                stroke.setTextSize(WORD_TEXT_SIZE);
                stroke.setTextColor(Color.BLACK);
                stroke.setBackgroundDrawable(null);
                stroke.setSingleLine();
                stroke.setPadding(res.getDimensionPixelSize(R.dimen.userdic_list_stroke_padding_left),
                                  res.getDimensionPixelSize(R.dimen.userdic_list_stroke_padding_top),
                                  res.getDimensionPixelSize(R.dimen.userdic_list_stroke_padding_right),
                                  res.getDimensionPixelSize(R.dimen.userdic_list_stroke_padding_bottom));
                stroke.setEllipsize(TextUtils.TruncateAt.END);
                stroke.setClickable(true);
                stroke.setFocusable(true);
                stroke.setFocusableInTouchMode(true);
                stroke.setOnTouchListener(this);
                stroke.setOnFocusChangeListener(this);
                stroke.setHeight(row_hight);
                stroke.setGravity(Gravity.CENTER_VERTICAL);
                if (mIsNoStroke) {
                    stroke.setVisibility(View.GONE);
                }
                row.addView(stroke);

                UserDictionaryToolsListFocus candidate = new UserDictionaryToolsListFocus(this);
                candidate.setId(i+MAX_WORD_COUNT);
                candidate.setWidth(system_width/ (mIsNoStroke ? 1 : 2));
                candidate.setTextSize(WORD_TEXT_SIZE);
                candidate.setTextColor(Color.BLACK);
                candidate.setBackgroundDrawable(null);
                candidate.setSingleLine();
                candidate.setPadding(res.getDimensionPixelSize(R.dimen.userdic_list_candidate_padding_left),
                                     res.getDimensionPixelSize(R.dimen.userdic_list_candidate_padding_top),
                                     res.getDimensionPixelSize(R.dimen.userdic_list_candidate_padding_right),
                                     res.getDimensionPixelSize(R.dimen.userdic_list_candidate_padding_bottom));
                candidate.setEllipsize(TextUtils.TruncateAt.END);
                candidate.setClickable(true);
                candidate.setFocusable(true);
                candidate.setFocusableInTouchMode(true);
                candidate.setOnTouchListener(this);
                candidate.setOnFocusChangeListener(this);
                candidate.setHeight(row_hight);
                candidate.setGravity(Gravity.CENTER_VERTICAL);
                stroke.setPairView(candidate);
                candidate.setPairView(stroke);
                row.addView(candidate);

                mTableLayout.addView(row, tableCreateParam(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        int size = mWordList.size();
        int start = mWordCount;

        TextView t = (TextView)findViewById(R.id.user_dictionary_position_indicator);
        if (size <= MAX_LIST_WORD_COUNT) {
            ((View)mLeftButton.getParent()).setVisibility(View.GONE);
        } else {
            ((View)mLeftButton.getParent()).setVisibility(View.VISIBLE);
            int last = (start + MAX_LIST_WORD_COUNT);
            t.setText((start + 1) + " - " + Math.min(last, size));

            mLeftButton.setEnabled(start != 0);
            mRightButton.setEnabled(last < size);
        }

        int selectedId = mSelectedViewID - ((MAX_WORD_COUNT < mSelectedViewID) ? MAX_WORD_COUNT : 0);

        for (int i = 0; i < MAX_LIST_WORD_COUNT; i++) {
            if ((size - 1) < (start + i)) {
                if ((0 < i) && (selectedId == (i + 1))) {
                    int id = i + (mIsNoStroke ? MAX_WORD_COUNT : 0);
                    mTableLayout.findViewById(id).requestFocus();
                }

                ((View)(mTableLayout.findViewById(i + 1)).getParent()).setVisibility(View.GONE);
                continue;
            }

            WnnWord wnnWordGet;
            if (mSortData == null) {
                break;
            }
            wnnWordGet = mSortData[start + i];
            int len_stroke = wnnWordGet.stroke.length();
            int len_candidate = wnnWordGet.candidate.length();
            if (len_stroke == 0 || len_candidate == 0) {
                break;
            }

            if (selectedId == i + 1) {
                int id = selectedId + (mIsNoStroke ? MAX_WORD_COUNT : 0);
                mTableLayout.findViewById(id).requestFocus();
            }

            TextView text = (TextView)mTableLayout.findViewById(i + 1);
            text.setText(wnnWordGet.stroke);

            text = (TextView)mTableLayout.findViewById(i + 1 + MAX_WORD_COUNT);
            text.setText(wnnWordGet.candidate);

            ((View)text.getParent()).setVisibility(View.VISIBLE);
        }

        mTableLayout.requestLayout();
    }

    /**
     * Whether the User dictionary List started.
     *
     * @return      {@code true} if started; {@code false} if not started.
     */
    public static boolean hasStarted() {
        return started;
    }

    /**
     * The initial value of a user dictionary list.
     *
     * @return  View ID initial value
     */
    private int getInitViewId() {
        return (mIsNoStroke ? MAX_WORD_COUNT + 1 : 1);
    }

    /**
     * Set title.
     *
     * @param lang  set title language {@link jp.co.omronsoft.iwnnime.ml.iwnn.iWnnEngine.LanguageType}
     */
    public void setTitleByLanguage(int lang) {
        setTitle(R.string.ti_user_dictionary_list_words_standard_txt);

        final TextView langView = (TextView) findViewById(R.id.user_dictionary_tools_list_title_language);
        langView.setText(LOCALE_CHOOSELANG_TITLE_MAP.get(lang));
    }
}
