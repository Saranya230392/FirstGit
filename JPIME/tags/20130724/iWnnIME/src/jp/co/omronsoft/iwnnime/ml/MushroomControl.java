/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.Intent;
import jp.co.omronsoft.iwnnime.ml.Mushroom;
import jp.co.omronsoft.iwnnime.ml.OpenWnn;

/**
 * The Mushroom Control class.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class MushroomControl {

    /** The instance of Mushroom class */
    private static MushroomControl mMushroomControl;

    /** The Mushroom apk result string */
    private CharSequence mMushroomResult;
    
    /** The Mushroom result type */
    private boolean mResultType;

    /** Constructor */
    public MushroomControl() {
        mMushroomControl = null;
        mMushroomResult = null;
    }

    /**
     * Get the instance of MushroomControl.
     * 
     * @return the instance of MushroomControl,
     */
    synchronized public static MushroomControl getInstance() {
        if (mMushroomControl == null) {
            mMushroomControl = new MushroomControl();
        }
        return mMushroomControl;
    }

    /**
     * Start Mushroom Luncher.
     *
     * @param  oldString  Old String
     */
    public void startMushroomLauncher(CharSequence oldString, Boolean type) {
        mMushroomResult = null;

        OpenWnn wnn = OpenWnn.getCurrentIme();
        Intent intent = new Intent();
        intent.setClass(OpenWnn.superGetContext(), MushroomPlus.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(MushroomPlus.MUSHROOM_REPLACE_KEY, oldString);
        intent.putExtra(MushroomPlus.GET_STRING_TYPE, type);
        wnn.startActivity(intent);
    }

    /**
     * Get the Result String.
     * 
     * @return the Result String,
     */
    public CharSequence getResultString() {
        CharSequence result = mMushroomResult;
        if (mMushroomResult != null) {
            mMushroomResult = null;
        }
        return result;
    }

    /**
     * Set the Result String.
     * 
     * @param result  the Result String
     */
    public void setResultString(CharSequence result) {
        mMushroomResult = result;
    }
    
    /**
     * Get the Result Type.
     * 
     * @return the Result Type,
     */
    public Boolean getResultType() {
        boolean type = mResultType;
        mResultType = false;
        return type;
    }

    /**
     * Set the Result Type.
     * 
     * @param result  the Result Type
     */
    public void setResultType(Boolean type) {
        mResultType = type;
    }
}
