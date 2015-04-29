/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.content.SharedPreferences;
import android.view.View;

/**
 * The default candidates view manager class using {@link android.widget.EditText}.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class WrapTextCandidatesViewManager extends TextCandidatesViewManager {

    /**
     * Constructor
     */
    public WrapTextCandidatesViewManager() {
        super();
    }

    //----------------------------------------------------------------------
    // CandidatesViewManager
    //----------------------------------------------------------------------
    /** #initView */
    public View initView(InputMethodBase parent, int width, int height) {
        return super.initView(parent, width, height);
  }

    /** #displayCandidates */
    public void displayCandidates() {
        super.displayCandidates(mConverter);
    }

    public void setWnnEngine(WrapWnnEngine engine) {
        super.setWnnEngine(engine);
    }

    /** #clearCandidates */
    public void clearCandidates() {
        super.clearCandidates();
    }

    /** #setPreferences */
    public void setPreferences(SharedPreferences pref) {
        super.setPreferences(pref);
    }

    protected String[] strCandidates;

    /**
     * Set focus for candidate.
     *
     * @param index  The index of focus.
     */
    public void setFocusCandidate(int index) {
        super.setFocusCandidate(index);
    }

    /** #close */
    public void close() {
    }

    protected int getCandidateViewHeight() {
        return 885;
    }

}