package com.android.settings.lockscreen.widget;

import com.android.internal.widget.LockPatternView.Cell;

import java.util.List;

/**
 * The call back interface for detecting patterns entered by the user.
 */
public interface OnPatternListener {

    /**
     * A new pattern has begun.
     */
    void onPatternStart();

    /**
     * The pattern was cleared.
     */
    void onPatternCleared();

    /**
     * The user extended the pattern currently being drawn by one cell.
     * @param pattern The pattern with newly added cell.
     */
    void onPatternCellAdded(List<Cell> pattern);

    /**
     * A pattern was detected from the user.
     * @param pattern The pattern.
     */
    void onPatternDetected(List<Cell> pattern);
}