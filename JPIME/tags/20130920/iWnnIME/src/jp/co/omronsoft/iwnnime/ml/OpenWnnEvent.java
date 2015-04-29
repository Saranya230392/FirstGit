/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */
package jp.co.omronsoft.iwnnime.ml;

import android.view.KeyEvent;

import java.util.HashMap;


/**
 * The definition class of event message used by OpenWnn framework.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class OpenWnnEvent {
    /** Offset value for private events. */
    public static final int PRIVATE_EVENT_OFFSET = 0xFF000000;

    /** Undefined */
    public static final int UNDEFINED = 0;

    /**
     * Reverse key.
     * <br>
     * This is used for multi-tap keyboard like 12-key.
     */
    public static final int TOGGLE_REVERSE_CHAR = 0xF0000001;

    /**
     * Convert.
     * <br>
     * This event makes {@link OpenWnn} to display conversion candidates from {@link ComposingText}.
     */
    public static final int CONVERT = 0xF0000002;

    /**
     * Predict.
     * <br>
     * This event makes {@link OpenWnn} to display prediction candidates from {@link ComposingText}.
     */
    public static final int PREDICT = 0xF0000008;

    /**
     * List candidates (normal view).
     * <br>
     * This event changes the candidates view's size.
     */
    public static final int LIST_CANDIDATES_NORMAL = 0xF0000003;

    /**
     * List candidates (wide view).
     * <br>
     * This event changes the candidates view's size.
     */
    public static final int LIST_CANDIDATES_FULL = 0xF0000004;

    /**
     * Close view.
     */
    public static final int CLOSE_VIEW = 0xF0000005;

    /**
     * Insert character(s).
     * <br>
     * This event input specified character({@code chars}) into the cursor position.
     */
    public static final int INPUT_CHAR = 0xF0000006;

    /**
     * Toggle a character.
     * <br>
     * This event changes a character at cursor position with specified rule({@code toggleMap}).
     * This is used for multi-tap keyboard.
     */
    public static final int TOGGLE_CHAR = 0xF000000C;

    /**
     * Replace a character at the cursor.
     */
    public static final int REPLACE_CHAR = 0xF000000D;

    /**
     * Input key.
     * <br>
     * This event processes a {@code keyEvent}.
     */
    public static final int INPUT_KEY  = 0xF0000007;

    /**
     * Input Soft key.
     * <br>
     * This event processes a {@code keyEvent}.
     * If the event is not processed in {@link OpenWnn}, the event is thrown to the IME's client.
     */
    public static final int INPUT_SOFT_KEY  = 0xF000000E;

    /**
     * Focus to the candidates view.
     */
    public static final int FOCUS_TO_CANDIDATE_VIEW  = 0xF0000009;

    /**
     * Focus out from the candidates view.
     */
    public static final int FOCUS_OUT_CANDIDATE_VIEW  = 0xF000000A;

    /**
     * Select a candidate.
     */
    public static final int SELECT_CANDIDATE  = 0xF000000B;

    /**
     * Change Mode.
     */
    public static final int CHANGE_MODE  = 0xF000000F;

    /**
     * The definition class of engine's mode.
     */
    public static final class Mode {
        /** Default (use both of the {@link LetterConverter} and the {@link WnnEngine}). */
        public static final int DEFAULT      = 0;
        /** Direct input (not use the {@link LetterConverter} nand the {@link WnnEngine}). */
        public static final int DIRECT       = 1;
        /** Do not use the {@link LetterConverter}. */
        public static final int NO_LV1_CONV  = 2;
        /** Do not use the {@link WnnEngine}. */
        public static final int NO_LV2_CONV  = 3;
        /**
         * Default constructor
         */
        public Mode() {
            super();
        }
    }

    /**
     * Commit the composing text.
     */
    public static final int COMMIT_COMPOSING_TEXT  = 0xF0000010;

    /**
     * List symbols.
     */
    public static final int LIST_SYMBOLS  = 0xF0000011;

    /**
     * Switch Language.
     */
    public static final int SWITCH_LANGUAGE  = 0xF0000012;

    /**
     * Initialize the user dictionary.
     */
    public static final int INITIALIZE_USER_DICTIONARY = 0xF0000013;

    /**
     * Initialize the learning dictionary.
     */
    public static final int INITIALIZE_LEARNING_DICTIONARY = 0xF0000014;

    /**
     * List words on the user dictionary.
     * <br>
     * Get words on the list, use {@code GET_WORD} event.
     */
    public static final int LIST_WORDS_IN_USER_DICTIONARY = 0xF0000015;

    /**
     * Get a word on the user dictionary.
     * <br>
     * Get a word on the top of the list made by {@code LIST_WORDS_IN_USER_DICTIONARY}.
     */
    public static final int GET_WORD  = 0xF0000018;

    /**
     * Add a word to the user dictionary.
     */
    public static final int ADD_WORD     = 0xF0000016;

    /**
     * Delete a word from the dictionary.
     */
    public static final int DELETE_WORD  = 0xF0000017;

    /**
     * Update the candidate view.
     */
    public static final int UPDATE_CANDIDATE = 0xF0000019;

    /**
     * Edit words on the user dictionary.
     */
    public static final int EDIT_WORDS_IN_USER_DICTIONARY = 0xF000001A;

    /**
     * Undo.
     */
    public static final int UNDO  = 0xF000001B;

    /**
     * Change input view.
     */
    public static final int CHANGE_INPUT_VIEW = 0xF000001C;

    /**
     * Key up event.
     */
    public static final int KEYUP = 0xF000001F;

    /**
     * Touch the other key.
     */
    public static final int TOUCH_OTHER_KEY = 0xF0000020;

    /**
     * Call external converter
     */
    public static final int FLICK_INPUT_CHAR = 0xF0000023;
    /**
     * Key long press event.
     */
    public static final int KEYLONGPRESS = 0xF0000024;

    /**
     * Input text using Voice
     */
    public static final int VOICE_INPUT  = 0xF0000025;

    /**
     * Toggle input cancel
     */
    public static final int TOGGLE_INPUT_CANCEL  = 0xF0000026;

    /**
     * Select a WebAPI Button.
     */
    public static final int SELECT_WEBAPI  = 0xF0001000;

    /**
     * Result WebAPI OK.
     */
    public static final int RESULT_WEBAPI_OK  = 0xF0001001;

    /**
     * Result WebAPI NG.
     */
    public static final int RESULT_WEBAPI_NG  = 0xF0001002;

    /**
     * Cancel WebAPI get candidate.
     */
    public static final int CANCEL_WEBAPI  = 0xF0001003;

    /**
     * Timeout WebAPI.
     */
    public static final int TIMEOUT_WEBAPI  = 0xF0001004;

    /**
     * Auto Learning for ReplyMail.
     */
    public static final int AUTO_LEARNING = 0xF0001005;

    /**
     * Select a WebAPI get again Button.
     */
    public static final int SELECT_WEBAPI_GET_AGAIN  = 0xF0001006;

    /**
     * Call a Mushroom.
     */
    public static final int CALL_MUSHROOM  = 0xF0002000;

    /**
     * Scroll up for symbol keyboard.
     */
    public static final int CANDIDATE_VIEW_SCROLL_UP = 0xF0003000;

    /**
     * Scroll down for symbol keyboard.
     */
    public static final int CANDIDATE_VIEW_SCROLL_DOWN = 0xF0003001;

    /**
     * Scroll full up for symbol keyboard.
     */
    public static final int CANDIDATE_VIEW_SCROLL_FULL_UP = 0xF0003002;

    /**
     * Scroll full down for symbol keyboard.
     */
    public static final int CANDIDATE_VIEW_SCROLL_FULL_DOWN = 0xF0003003;

    /**
     * Start focus candidate.
     */
    public static final int FOCUS_CANDIDATE_START = 0xF0004000;

    /**
     * End focus candidate.
     */
    public static final int FOCUS_CANDIDATE_END = 0xF0004001;

    /**
     * Receive a DecoEmoji.
     */
    public static final int RECEIVE_DECOEMOJI = 0xF0005000;

    /**
     * Commit the insert text
     */
    public static final int COMMIT_INPUT_TEXT  = 0xF0006000;

    /**
     * Update view status by the focused candidate.
     */
    public static final int UPDATE_VIEW_STATUS_USE_FOCUSED_CANDIDATE  = 0xF0007000;

    /** Event code. */
    public int code = UNDEFINED;
    /** Detail mode of the event. */
    public int mode = 0;
    /** Input character(s). */
    public char[] chars = null;
    /** Key event. */
    public KeyEvent keyEvent = null;
    /** Mapping table for toggle input. */
    public String[]  toggleTable = null;
    /** Mapping table for toggle input. */
    public HashMap<?,?> replaceTable = null;
    /** Word's information. */
    public WnnWord  word = null;
    /** String data. */
    public String string = null;

    /**
     * Generate {@link OpenWnnEvent}.
     *
     * @param code      The code
     */
    public OpenWnnEvent(int code) {
        this.code = code;
    }
    /**
     * Generate {@link OpenWnnEvent} for changing the mode.
     *
     * @param code      The code
     * @param mode      The mode
     */
    public OpenWnnEvent(int code, int mode) {
        this.code = code;
        this.mode = mode;
    }
    /**
     * Generate {@link OpenWnnEvent} for a inputing character.
     *
     * @param code      The code
     * @param c         The inputing character
     */
    public OpenWnnEvent(int code, char c) {
        this.code = code;
        this.chars = new char[1];
        this.chars[0] = c;
     }
    /**
     * Generate {@link OpenWnnEvent} for inputing characters.
     *
     * @param code      The code
     * @param c         The array of inputing character
     */
    public OpenWnnEvent(int code, char c[]) {
        this.code = code;
        this.chars = (char [])c.clone();
    }
    /**
     * Generate {@link OpenWnnEvent} for toggle inputing a character.
     *
     * @param code          The code
     * @param toggleTable   The array of toggle inputing a character
     */
    public OpenWnnEvent(int code, String[] toggleTable) {
        this.code = code;
        this.toggleTable = (String [])toggleTable.clone();
    }
    /**
     * Generate {@link OpenWnnEvent} for replacing a character.
     *
     * @param code          The code
     * @param replaceTable  The replace table
     */
    public OpenWnnEvent(int code, HashMap<?,?> replaceTable) {
        this.code = code;
        this.replaceTable = (HashMap<?,?>)replaceTable.clone();
    }
    /**
     * Generate {@link OpenWnnEvent} from {@link KeyEvent}.
     * <br>
     * This constructor is same as {@code OpenWnnEvent(INPUT_KEY, ev)}.
     *
     * @param ev    The key event
     */
    public OpenWnnEvent(KeyEvent ev) {
        if(ev.getAction() != KeyEvent.ACTION_UP){
            this.code = INPUT_KEY;
        }else{
            this.code = KEYUP;
        }
        this.keyEvent = ev;
    }
    /**
     * Generate {@link OpenWnnEvent} from {@link android.view.KeyEvent}.
     *
     * @param code      The code
     * @param ev        The key event
     */
    public OpenWnnEvent(int code, KeyEvent ev) {
        this.code = code;
        this.keyEvent = ev;
    }
    /**
     * Generate {@link OpenWnnEvent} for selecting a candidate.
     *
     * @param code      The code
     * @param word      The selected candidate
     */
    public OpenWnnEvent(int code, WnnWord word) {
        this.code = code;
        this.word = word;
    }

    /**
     * Generate {@link OpenWnnEvent} for string.
     *
     * @param code      The code
     * @param string    The string
     */
    public OpenWnnEvent(int code, String string) {
        this.code = code;
        this.string = string;
    }
}
