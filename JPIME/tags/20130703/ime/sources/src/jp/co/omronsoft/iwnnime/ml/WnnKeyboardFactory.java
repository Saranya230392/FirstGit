/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
/*
 * Copyright (c) 2009 Sony Ericsson Mobile Communications AB. All rights reserved
 */

package jp.co.omronsoft.iwnnime.ml;

import android.util.Log;
import jp.co.omronsoft.iwnnime.ml.Keyboard;

/**
 * Software Keyboard Factory class
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class WnnKeyboardFactory  {

    /** for DEBUG */
    private static final boolean DEBUG = false;

    /** Keyboard resource ID */
    private int mResourceId;

    /** OpenWnn using the keyboards */
    private OpenWnn mParent = null;

    /** Keyboard surfaces */
    private Keyboard mKeyboard = null;

    /** Keyboard width scale */
    private static float mWidthScale = 1.0f;

    /** Keyboard Mode */
    private static boolean sKeyboardMode = true;

    /** Split Mode */
    private static boolean sSplitMode = false;

    /** Added string split mode */
    private static final String ADDEDSTRING_SPLITMODE = "_split";

    /**
     * Constructor
     * @param parent      The context
     * @param resourceId  The resource ID( ex. ID of "R.xml.keyboard_qwerty_jp" )
     */
    public WnnKeyboardFactory(OpenWnn parent, int resourceId) {
        mResourceId = resourceId;
        mParent     = parent;
    }

    /**
     * Keyboard Create
     * @see android.inputmethodservice.Keyboard#Keyboard
     */
    public Keyboard getKeyboard() {
        if (mKeyboard == null) {
            if (DEBUG) {
                Log.d("OpenWnn",
                      "WnnKeyboardFactory.getKeyboard() new Keyboard:mResourceId="+mResourceId+"");
            }
            int id = mResourceId;
            if (sSplitMode) {
                id = convertSplitResourceId(mResourceId);
            }
            mKeyboard = new Keyboard(OpenWnn.superGetContext(), id, mWidthScale, getKeyboardModeId());
        }
        return mKeyboard;
    }

    /**
     * Set keyboard width scale.
     *
     * @param scale scale of keyboard width.
     */
    public static void setWidthScale(float scale) {
        mWidthScale = scale;
    }

    /**
     * Get keyboard width scale.
     *
     * @return scale scale of keyboard width.
     */
    public static float getWidthScale() {
        return mWidthScale;
    }

    /**
     * Set Keyboard Mode
     * @param mode  keyboard mode
     */
    public static void setKeyboardMode(boolean mode) {
    }

    /**
     * Get Keyboard Mode ID
     * @return keyboard mode id
     */
    private int getKeyboardModeId() {
        if (sKeyboardMode) {
            return R.id.keyboard_mode_enable_one_touch_emoji;
        } else {
            return R.id.keyboard_mode_disable_one_touch_emoji;
        }
    }

    /**
     * Set split mode.
     * @param mode  true if split mode, false othewise.
     */
    public static void setSplitMode(boolean isSplitMode) {
        if (sSplitMode != isSplitMode) {
            sSplitMode = isSplitMode;
        }
    }

    /**
     * Get split mode flag value.
     *
     * @return value of split mode.
     */
    public static boolean getSplitMode() {
        return sSplitMode;
    }

    /**
     * Convert split keyboard resource id
     * @param baseId  The resource ID.
     * @return converted id
     */
    private int convertSplitResourceId(int baseId) {
        int newId = mParent.getResources().getIdentifier(
                mParent.getResources().getResourceName(baseId) + ADDEDSTRING_SPLITMODE, null, null);
        if (newId == 0) {
            newId = baseId;
        }
        return newId;
    }
}
