/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

/**
 * The definition class of event message.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class HWEvent {
    /** Offset value for private events. */
    public static final int PRIVATE_EVENT_OFFSET = 0xFF000000;

    /** Undefined */
    public static final int UNDEFINED = 0;

    /**
     * A notice of change of the Floating/Expanded Floating keyboard..
     */
    public static final int CHANGE_FLOATING_KEYBOARD = 0x00000001;

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
     * Select a candidate.
     */
    public static final int SELECT_CANDIDATE  = 0xF000000B;

    /**
     * Update the candidate view.
     */
    public static final int UPDATE_CANDIDATE = 0xF0000019;

    /**
     * Call a Mushroom.
     */
    public static final int CALL_MUSHROOM  = 0xF0002000;

    /** Event code. */
    public int code = UNDEFINED;
    /** public string candidate*/
    public String string = null;
    /** public int attribute*/
    public int attribute = 0;

    /**
     * Generate {@link OpenWnnEvent}.
     *
     * @param code      The code
     */
    public HWEvent(int code) {
        this.code = code;
    }

    /**
     * Generate {@link OpenWnnEvent} for selecting a candidate.
     *
     * @param code      The code
     * @param candidate The selected candidate
     * @param attribute The selected candidate
     */
    public HWEvent(int code, String candidate, int attribute) {
        this.code = code;
        this.string = candidate;
        this.attribute = attribute;
    }

}
