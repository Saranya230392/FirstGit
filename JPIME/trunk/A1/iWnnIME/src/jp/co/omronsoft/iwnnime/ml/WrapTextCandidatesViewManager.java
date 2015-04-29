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

    private static final int CANDIDATES_VIEW_HEIGHT = 885;

    /**
     * Constructor
     */
    public WrapTextCandidatesViewManager() {
        super();
    }

    //----------------------------------------------------------------------
    // CandidatesViewManager
    //----------------------------------------------------------------------
    /** @see CandidatesViewManager#initView */
    public View initView(InputMethodBase parent, int width, int height) {
        return super.initView(parent, width, height);
    }

    /** @see CandidatesViewManager#displayCandidates */
    public void displayCandidates() {
        super.displayCandidates(mConverter);
    }

    /**
     * Set WnnEngine.
     */
    public void setWnnEngine(WrapWnnEngine engine) {
        super.setWnnEngine(engine);
    }

    /** @see CandidatesViewManager#clearCandidates */
    public void clearCandidates() {
        super.clearCandidates();
    }

    /** @see CandidatesViewManager#setPreferences */
    public void setPreferences(SharedPreferences pref) {
        super.setPreferences(pref);
    }

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

    /** @see CandidatesViewManager#getCandidatesViewHeight */
    protected int getCandidateViewHeight() {
        return CANDIDATES_VIEW_HEIGHT;
    }

}
