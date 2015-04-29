/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.lockscreen.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.settings.R;
import com.lge.os.Build;

import java.util.ArrayList;
import java.util.List;


/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 *
 * Is also capable of displaying a static pattern in "in progress", "wrong" or
 * "correct" states.
 */
public class LgeLockPatternViewBase extends View {
    // Aspect to use when rendering this view
    private static final int ASPECT_SQUARE = 0; // View will be the minimum of width/height
    private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will be minimum of (w,h)
    private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will be minimum of (w,h)

    protected static final boolean PEARL_UI = Build.LGUI_VERSION.RELEASE >= Build.LGUI_VERSION_NAMES.V4_2;

    private static final boolean PROFILE_DRAWING = false;
    private boolean mDrawingProfilingStarted = false;

    // TODO: make this common with PhoneWindow
    static final int STATUS_BAR_HEIGHT = 25;

    private OnPatternListener mOnPatternListener;
    protected ArrayList<Cell> mPattern = new ArrayList<Cell>(9);

    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    protected boolean[][] mPatternDrawLookup = new boolean[3][3];
    protected boolean mInStealthMode = false;
    protected DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    protected float mDiameterFactor = 0.10f; // TODO: move to attrs

    /**
     * the in progress point:
     * - during interaction: where the user's finger is
     * - during animation: the current tip of the animating line
     */
    private float mInProgressX = -1;
    private float mInProgressY = -1;

    private boolean mInputEnabled = true;
    private boolean mEnableHapticFeedback = true;
    private boolean mPatternInProgress = false;

    private float mHitFactor = 0.6f;

    private float mSquareWidth;
    private float mSquareHeight;

    protected final Rect mInvalidate = new Rect();
    private int mAspect;

    /**
     * How to display the current pattern.
     */
    public enum DisplayMode {

        /**
         * The pattern drawn is correct (i.e draw it in a friendly color)
         */
        Correct,

        /**
         * Animate the pattern (for demo, and help).
         */
        Animate,

        /**
         * The pattern is wrong (i.e draw a foreboding color)
         */
        Wrong
    }

    public LgeLockPatternViewBase(Context context) {
        this(context, null);
    }

    public LgeLockPatternViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView);

        final String aspect = a.getString(R.styleable.LockPatternView_aspect);

        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }

        setClickable(true);

    }

    /**
     * @return Whether the view is in stealth mode.
     */
    public boolean isInStealthMode() {
        return mInStealthMode;
    }

    /**
     * @return Whether the view has tactile feedback enabled.
     */
    public boolean isTactileFeedbackEnabled() {
        return mEnableHapticFeedback;
    }

    /**
     * Set whether the view is in stealth mode.  If true, there will be no
     * visible feedback as the user enters the pattern.
     *
     * @param inStealthMode Whether in stealth mode.
     */
    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }

    /**
     * Set whether the view will use tactile feedback.  If true, there will be
     * tactile feedback as the user enters the pattern.
     *
     * @param tactileFeedbackEnabled Whether tactile feedback is enabled
     */
    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    /**
     * Set the call back for pattern detection.
     * @param onPatternListener The call back.
     */
    public void setOnPatternListener(
            OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }

    /**
     * Set the pattern explicitely (rather than waiting for the user to input
     * a pattern).
     * @param displayMode How to display the pattern.
     * @param pattern The pattern.
     */
    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }

        setDisplayMode(displayMode);
    }

    /**
     * Set the display mode of the current pattern.  This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     * @param displayMode The display mode.
     */
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException("you must have a pattern to "
                        + "animate if you want to set the display mode to animate");
            }
            final Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.getColumn());
            mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }
        invalidate();
    }

    protected void notifyCellAdded() {
        announceForAccessibility(mContext.getString(R.string.lockscreen_access_pattern_cell_added));
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
    }

    protected void notifyPatternStarted() {
        announceForAccessibility(mContext.getString(R.string.lockscreen_access_pattern_start));
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStart();
        }
    }

    protected void notifyPatternDetected() {
        announceForAccessibility(mContext.getString(R.string.lockscreen_access_pattern_detected));
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPattern);
        }
    }

    protected void notifyPatternCleared() {
        announceForAccessibility(mContext.getString(R.string.lockscreen_access_pattern_cleared));
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
    }

    /**
     * Clear the pattern.
     */
    public void clearPattern() {
        resetPattern();
    }

    /**
     * Reset all pattern state.
     */
    protected void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    /**
     * Clear the pattern lookup table.
     */
    protected void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }
    }

    /**
     * Disable input (for instance when displaying a message that will
     * timeout so user doesn't get view into messy state).
     */
    public void disableInput() {
        mInputEnabled = false;
    }

    /**
     * Enable input.
     */
    public void enableInput() {
        mInputEnabled = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - mPaddingLeft - mPaddingRight;
        mSquareWidth = width / 3.0f;

        final int height = h - mPaddingTop - mPaddingBottom;
        mSquareHeight = height / 3.0f;
    }

    private int resolveMeasured(int measureSpec, int desired)
    {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
            default:
                break;
        }

        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the cell, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {

            // check for gaps in existing pattern
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.getRow() - lastCell.getRow();
                int dColumn = cell.getColumn() - lastCell.getColumn();

                int fillInRow = lastCell.getRow();
                int fillInColumn = lastCell.getColumn();

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.getRow() + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.getColumn() + ((dColumn > 0) ? 1 : -1);
                }

                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null &&
                    !mPatternDrawLookup[fillInGapCell.getRow()][fillInGapCell.getColumn()]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if (mEnableHapticFeedback) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            return cell;
        }
        return null;
    }

    protected void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        notifyCellAdded();
    }

    // helper method to find which cell a point maps to
    private Cell checkForNewHit(float x, float y) {

        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }

        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }

    /**
     * Helper method to find the row that y falls into.
     * @param y The y coordinate
     * @return The row that y falls in, or -1 if it falls in no row.
     */
    protected int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = mPaddingTop + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {

            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to find the column x fallis into.
     * @param x The x coordinate.
     * @return The column that x falls in, or -1 if it falls in no column.
     */
    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = mPaddingLeft + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        if (AccessibilityManager.getInstance(mContext).isTouchExplorationEnabled()) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    event.setAction(MotionEvent.ACTION_DOWN);
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    event.setAction(MotionEvent.ACTION_MOVE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    event.setAction(MotionEvent.ACTION_UP);
                    break;
                default:
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (mPatternInProgress) {
                    mPatternInProgress = false;
                    resetPattern();
                    notifyPatternCleared();
                }
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
            default:
                break;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
        // Handle all recent motion events so we don't skip any cells even when the device
        // is busy...
        final int historySize = event.getHistorySize();
        for (int i = 0; i < historySize + 1; i++) {
            final float x = i < historySize ? event.getHistoricalX(i) : event.getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event.getY();
            final int patternSizePreHitDetect = mPattern.size();
            Cell hitCell = detectAndAddHit(x, y);
            final int patternSize = mPattern.size();
            if (hitCell != null && patternSize == 1) {
                mPatternInProgress = true;
                notifyPatternStarted();
            }
            // note current x and y for rubber banding of in progress patterns
            final float dx = Math.abs(x - mInProgressX);
            final float dy = Math.abs(y - mInProgressY);
            if (dx + dy > mSquareWidth * 0.01f) {
                float oldX = mInProgressX;
                float oldY = mInProgressY;

                mInProgressX = x;
                mInProgressY = y;

                if (mPatternInProgress && patternSize > 0) {
                    final ArrayList<Cell> pattern = mPattern;
                    final float radius = mSquareWidth * mDiameterFactor * 0.5f;

                    final Cell lastCell = pattern.get(patternSize - 1);

                    float startX = getCenterXForColumn(lastCell.getColumn());
                    float startY = getCenterYForRow(lastCell.getRow());

                    float left;
                    float top;
                    float right;
                    float bottom;

                    final Rect invalidateRect = mInvalidate;

                    if (startX < x) {
                        left = startX;
                        right = x;
                    } else {
                        left = x;
                        right = startX;
                    }

                    if (startY < y) {
                        top = startY;
                        bottom = y;
                    } else {
                        top = y;
                        bottom = startY;
                    }

                    // Invalidate between the pattern's last cell and the current location
                    invalidateRect.set((int)(left - radius), (int)(top - radius),
                            (int)(right + radius), (int)(bottom + radius));

                    if (startX < oldX) {
                        left = startX;
                        right = oldX;
                    } else {
                        left = oldX;
                        right = startX;
                    }

                    if (startY < oldY) {
                        top = startY;
                        bottom = oldY;
                    } else {
                        top = oldY;
                        bottom = startY;
                    }

                    // Invalidate between the pattern's last cell and the previous location
                    invalidateRect.union((int)(left - radius), (int)(top - radius),
                            (int)(right + radius), (int)(bottom + radius));

                    // Invalidate between the pattern's new cell and the pattern's previous cell
                    if (hitCell != null) {
                        startX = getCenterXForColumn(hitCell.getColumn());
                        startY = getCenterYForRow(hitCell.getRow());

                        if (patternSize >= 2) {
                            // (re-using hitcell for old cell)
                            hitCell = pattern.get(patternSize - 1 - (patternSize - patternSizePreHitDetect));
                            oldX = getCenterXForColumn(hitCell.getColumn());
                            oldY = getCenterYForRow(hitCell.getRow());

                            if (startX < oldX) {
                                left = startX;
                                right = oldX;
                            } else {
                                left = oldX;
                                right = startX;
                            }

                            if (startY < oldY) {
                                top = startY;
                                bottom = oldY;
                            } else {
                                top = oldY;
                                bottom = startY;
                            }
                        } else {
                            left = right = startX;
                            top = bottom = startY;
                        }

                        final float widthOffset = mSquareWidth / 2f;
                        final float heightOffset = mSquareHeight / 2f;

                        invalidateRect.set((int)(left - widthOffset),
                                (int)(top - heightOffset), (int)(right + widthOffset),
                                (int)(bottom + heightOffset));
                    }

                    invalidate();
                } else {
                    invalidate();
                }
            }
        }
    }

    private void handleActionUp(MotionEvent event) {
        // report pattern detected
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
            notifyPatternDetected();
            invalidate();
        }
        if (PROFILE_DRAWING) {
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else if (mPatternInProgress) {
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.getColumn());
            final float startY = getCenterYForRow(hitCell.getRow());

            final float widthOffset = mSquareWidth / 2f;
            final float heightOffset = mSquareHeight / 2f;

            invalidate((int)(startX - widthOffset), (int)(startY - heightOffset),
                    (int)(startX + widthOffset), (int)(startY + heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        if (PROFILE_DRAWING) {
            if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("LockPatternDrawing");
                mDrawingProfilingStarted = true;
            }
        }
    }

    // CAPP_LOCKSCREEN : Hidden pattern
    public float getCenterXForLastColumn() {
        final Cell lastCell = mPattern.get(mPattern.size() - 1);
        return mPaddingLeft + lastCell.getColumn() * mSquareWidth + mSquareWidth / 2f;
    }

    public float getCenterYForLastRow() {
        final Cell lastCell = mPattern.get(mPattern.size() - 1);
        return mPaddingTop + lastCell.getRow() * mSquareHeight + mSquareHeight / 2f;
    }
    // CAPP_LOCKSCREEN_END

    protected float getCenterXForColumn(int column) {
        return mPaddingLeft + column * mSquareWidth + mSquareWidth / 2f;
    }

    protected float getCenterYForRow(int row) {
        return mPaddingTop + row * mSquareHeight + mSquareHeight / 2f;
    }

    protected float getInProgressX() {
        return mInProgressX;
    }

    protected void setInProgressX(float inProgressX) {
        mInProgressX = inProgressX;
    }

    protected float getInProgressY() {
        return mInProgressY;
    }

    protected void setInProgressY(float inProgressY) {
        mInProgressY = inProgressY;
    }

    protected float getSquareWidth() {
        return mSquareWidth;
    }

    protected float getSquareHeight() {
        return mSquareHeight;
    }

    protected float getHitFactor() {
        return mHitFactor;
    }

    protected boolean isPatternInProgress() {
        return mPatternInProgress;
    }

    public void onResume() {
    }

    public void onPause() {
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new LgeLockPatternSavedState(superState,
                LockPatternUtils.patternToString(mPattern),
                mPatternDisplayMode.ordinal(),
                mInputEnabled, mInStealthMode, mEnableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final LgeLockPatternSavedState ss = (LgeLockPatternSavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(
                DisplayMode.Correct,
                LockPatternUtils.stringToPattern(ss.getSerializedPattern()));
        mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        mInputEnabled = ss.isInputEnabled();
        mInStealthMode = ss.isInStealthMode();
        mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
    }

    protected final Path mCurrentPath = new Path();
    protected Paint mPathPaint = new Paint();
    protected final int mStrokeAlpha = 128;
    protected void drawPattern(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        float radius = (getSquareWidth() * mDiameterFactor * 0.5f);
        mPathPaint.setStrokeWidth(radius);
        // TODO: the path should be created and cached every time we hit-detect a cell
        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless the user is in progress, and
        // we are in stealth mode)
        final boolean drawPath = (!mInStealthMode || mPatternDisplayMode == DisplayMode.Wrong);

        if (drawPath) {
            if (mPatternDisplayMode != DisplayMode.Wrong) {
                mPathPaint.setColor(Color.WHITE);
                mPathPaint.setAlpha(mStrokeAlpha);
            } else {
                mPathPaint.setColor(0xff2f33);
                mPathPaint.setAlpha(mStrokeAlpha);
            }

            boolean anyCircles = false;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[cell.getRow()][cell.getColumn()]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.getColumn());
                float centerY = getCenterYForRow(cell.getRow());
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }

            // add last in progress section
            if ((isPatternInProgress() || mPatternDisplayMode == DisplayMode.Animate) && anyCircles) {
                currentPath.lineTo(getInProgressX(), getInProgressY());
            }
            canvas.drawPath(currentPath, mPathPaint);
        }
    }
}
