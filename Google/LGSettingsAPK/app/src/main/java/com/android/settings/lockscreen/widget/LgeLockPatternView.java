package com.android.settings.lockscreen.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import com.android.internal.widget.LockPatternView.Cell;
import com.android.settings.R;
import com.android.settings.lockscreen.widget.LgeLockPatternViewBase.DisplayMode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class LgeLockPatternView extends LgeLockPatternViewBase {
    private long mAnimatingPeriodStart;

    /**
     * How many milliseconds we spend animating each circle of a lock pattern
     * if the animating mode is set.  The entire animation should take this
     * constant * the length of the pattern to complete.
     */
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private static final float DOWN_SCALE_DOT_ICON = 0.35f;

    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();

    private Bitmap mBitmapBtnDefault;
    private Bitmap mBitmapBtnTouched;
    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapCircleGreen;
    private Bitmap mBitmapCircleRed;

    private Bitmap mBitmapArrowGreenUp;
    private Bitmap mBitmapArrowRedUp;

    private final Path mCurrentPath = new Path();

    private int mBitmapWidth;
    private int mBitmapHeight;

    private final int mStrokeAlpha = 128;

    private final Matrix mArrowMatrix = new Matrix();
    private final Matrix mCircleMatrix = new Matrix();
    private final CellState[][] mCellStates;

    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mLinearOutSlowInInterpolator;

    public static class CellState {
        public float scale = 1.0f;
     }

    public LgeLockPatternView(Context context) {
        this(context, null);
    }

    public LgeLockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPathPaint(false);

        // lot's of bitmaps!
        mBitmapBtnDefault = getBitmapFor(R.drawable.btn_code_lock_default_holo);
        mBitmapBtnTouched = getBitmapFor(R.drawable.btn_code_lock_touched_holo);
        mBitmapCircleDefault = getBitmapFor(R.drawable.indicator_code_lock_point_area_default_holo);
        mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_holo);
        mBitmapCircleRed = getBitmapFor(R.drawable.indicator_code_lock_point_area_red_holo);

        mBitmapArrowGreenUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_green_up);
        mBitmapArrowRedUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_red_up);

        // bitmaps have the size of the largest bitmap in this group
        final Bitmap bitmaps[] = { mBitmapBtnDefault, mBitmapBtnTouched, mBitmapCircleDefault,
                mBitmapCircleGreen, mBitmapCircleRed };

        for (Bitmap bitmap : bitmaps) {
            mBitmapWidth = Math.max(mBitmapWidth, bitmap.getWidth());
            mBitmapHeight = Math.max(mBitmapHeight, bitmap.getHeight());
        }

        mCellStates = new CellState[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mCellStates[i][j] = new CellState();
            }
        }

        mFastOutSlowInInterpolator =
                AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_slow_in);
        mLinearOutSlowInInterpolator =
                AnimationUtils.loadInterpolator(context, android.R.interpolator.linear_out_slow_in);
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    private void drawArrow(Canvas canvas, float leftX, float topY, Cell start, Cell end) {
        boolean green = mPatternDisplayMode != DisplayMode.Wrong;

        final int endRow = end.getRow();
        final int startRow = start.getRow();
        final int endColumn = end.getColumn();
        final int startColumn = start.getColumn();

        // offsets for centering the bitmap in the cell
        final int offsetX = ((int)getSquareWidth() - mBitmapWidth) / 2;
        final int offsetY = ((int)getSquareHeight() - mBitmapHeight) / 2;

        // compute transform to place arrow bitmaps at correct angle inside circle.
        // This assumes that the arrow image is drawn at 12:00 with it's top edge
        // coincident with the circle bitmap's top edge.
        Bitmap arrow = green ? mBitmapArrowGreenUp : mBitmapArrowRedUp;
        final int cellWidth = mBitmapWidth;
        final int cellHeight = mBitmapHeight;

        // the up arrow bitmap is at 12:00, so find the rotation from x axis and add 90 degrees.
        final float theta = (float)Math.atan2(endRow - startRow, endColumn - startColumn);
        final float angle = (float)Math.toDegrees(theta) + 90.0f;

        // compose matrix
        float sx = Math.min(getSquareWidth() / mBitmapWidth, 1.0f);
        float sy = Math.min(getSquareHeight() / mBitmapHeight, 1.0f);
        mArrowMatrix.setTranslate(leftX + offsetX, topY + offsetY); // transform to cell position
        mArrowMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mArrowMatrix.preScale(sx, sy);
        mArrowMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);
        mArrowMatrix.preRotate(angle, cellWidth / 2.0f, cellHeight / 2.0f);  // rotate about cell center
        mArrowMatrix.preTranslate((cellWidth - arrow.getWidth()) / 2.0f, 0.0f); // translate to 12:00 pos
        canvas.drawBitmap(arrow, mArrowMatrix, mPaint);
    }

    /**
     * @param canvas
     * @param leftX
     * @param topY
     * @param partOfPattern Whether this circle is part of the pattern.
     */
    private void drawCircle(Canvas canvas, int leftX, int topY, boolean partOfPattern) {
        Bitmap outerCircle;
        Bitmap innerCircle;

        if (!partOfPattern || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
            // unselected circle
            outerCircle = mBitmapCircleDefault;
            innerCircle = mBitmapBtnDefault;
        } else if (isPatternInProgress()) {
            // user is in middle of drawing a pattern
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnDefault;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            // the pattern is wrong
            outerCircle = mBitmapCircleRed;
            innerCircle = mBitmapBtnDefault;
        } else if (mPatternDisplayMode == DisplayMode.Correct
                || mPatternDisplayMode == DisplayMode.Animate) {
            // the pattern is correct
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnDefault;
        } else {
            throw new IllegalStateException("unknown display mode " + mPatternDisplayMode);
        }

        final int width = mBitmapWidth;
        final int height = mBitmapHeight;

        final float squareWidth = getSquareWidth();
        final float squareHeight = getSquareHeight();

        int offsetX = (int)((squareWidth - width) / 2f);
        int offsetY = (int)((squareHeight - height) / 2f);

        // Allow circles to shrink if the view is too small to hold them.
        float sx = Math.min(getSquareWidth() / mBitmapWidth, 1.0f);
        float sy = Math.min(getSquareHeight() / mBitmapHeight, 1.0f);

        mCircleMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mCircleMatrix.preScale(sx, sy);
        mCircleMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);

        canvas.drawBitmap(outerCircle, mCircleMatrix, mPaint);
        canvas.drawBitmap(innerCircle, mCircleMatrix, mPaint);
    }

    private void drawTouchedCircle(Canvas canvas, int leftX, int topY, boolean partOfPattern, float scale) {
        if (!PEARL_UI || !partOfPattern || mInStealthMode) {
            return;
        }

        Bitmap touchedCircle = mBitmapBtnTouched;

        final int width = mBitmapWidth;
        final int height = mBitmapHeight;

        final float squareWidth = getSquareWidth();
        final float squareHeight = getSquareHeight();

        int offsetX = (int)((squareWidth - width) / 2f);
        int offsetY = (int)((squareHeight - height) / 2f);

        mCircleMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mCircleMatrix.preScale(scale, scale);
        mCircleMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);

        canvas.drawBitmap(touchedCircle, mCircleMatrix, mPaint);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mBitmapWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mBitmapWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if (mPatternDisplayMode == DisplayMode.Animate) {

            // figure out which circles to draw

            // + 1 so we pause on complete pattern
            final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int)(SystemClock.elapsedRealtime() - mAnimatingPeriodStart)
                    % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;

            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }

            // figure out in progress portion of ghosting line

            final boolean needToUpdateInProgressPoint = numCircles > 0 && numCircles < count;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle = ((float)(spotInCycle % MILLIS_PER_CIRCLE_ANIMATING))
                        / MILLIS_PER_CIRCLE_ANIMATING;

                final Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterXForColumn(currentCell.getColumn());
                final float centerY = getCenterYForRow(currentCell.getRow());

                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle
                        * (getCenterXForColumn(nextCell.getColumn()) - centerX);
                final float dy = percentageOfNextCircle
                        * (getCenterYForRow(nextCell.getRow()) - centerY);
                setInProgressX(centerX + dx);
                setInProgressY(centerY + dy);
            }
            // TODO: Infinite loop here...
            invalidate();
        }

        final float squareWidth = getSquareWidth();
        final float squareHeight = getSquareHeight();

        float radius = (squareWidth * mDiameterFactor * 0.5f);
        mPathPaint.setStrokeWidth(radius);

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        // draw the circles
        final int paddingTop = mPaddingTop;
        final int paddingLeft = mPaddingLeft;

        for (int i = 0; i < 3; i++) {
            float topY = paddingTop + i * squareHeight;
            for (int j = 0; j < 3; j++) {
                float leftX = paddingLeft + j * squareWidth;
                CellState cellState = mCellStates[i][j];
                drawTouchedCircle(canvas, (int)leftX, (int)topY, drawLookup[i][j], cellState.scale);
                drawCircle(canvas, (int)leftX, (int)topY, drawLookup[i][j]);
            }
        }

        // TODO: the path should be created and cached every time we hit-detect a cell
        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless the user is in progress, and
        // we are in stealth mode)
        final boolean drawPath = (!mInStealthMode || mPatternDisplayMode == DisplayMode.Wrong);

        // draw the arrows associated with the path (unless the user is in progress, and
        // we are in stealth mode)
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        mPaint.setFilterBitmap(true); // draw with higher quality since we render with transforms
        if (drawPath && !PEARL_UI) {
            for (int i = 0; i < count - 1; i++) {
                Cell cell = pattern.get(i);
                Cell next = pattern.get(i + 1);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[next.getRow()][next.getColumn()]) {
                    break;
                }

                float leftX = paddingLeft + cell.getColumn() * squareWidth;
                float topY = paddingTop + cell.getRow() * squareHeight;

                drawArrow(canvas, leftX, topY, cell, next);
            }
        }

        if (drawPath) {
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

            final boolean isRedPath = ((mPatternDisplayMode == DisplayMode.Wrong) && PEARL_UI);
            setPathPaint(isRedPath);

            canvas.drawPath(currentPath, mPathPaint);
        }

        mPaint.setFilterBitmap(oldFlag); // restore default flag
    }

    @Override
    public void setDisplayMode(DisplayMode displayMode) {
        super.setDisplayMode(displayMode);
        if (displayMode == DisplayMode.Animate) {
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
        }
    }

    @Override
    protected void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        if (!mInStealthMode && PEARL_UI) {
            startCellActivatedAnimation(newCell);
        }
        notifyCellAdded();
    }

    private void setPathPaint(boolean isRedPath) {
        if (mPathPaint != null) {
            mPathPaint.setAntiAlias(true);
            mPathPaint.setDither(true);
            mPathPaint.setColor(isRedPath ? Color.RED : Color.GRAY);
            mPathPaint.setAlpha(mStrokeAlpha);
            mPathPaint.setStyle(Paint.Style.STROKE);
            mPathPaint.setStrokeJoin(Paint.Join.ROUND);
            mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    private void startCellActivatedAnimation(Cell cell) {
        final CellState cellState = mCellStates[cell.getRow()][cell.getColumn()];
        startSizeAnimation(DOWN_SCALE_DOT_ICON, 1.0f, 96, mLinearOutSlowInInterpolator,
                cellState, new Runnable() {
            @Override
            public void run() {
                startSizeAnimation(1.0f, DOWN_SCALE_DOT_ICON, 192, mFastOutSlowInInterpolator,
                        cellState, null);
            }
        });
    }

    private void startSizeAnimation(float start, float end, long duration, Interpolator interpolator,
            final CellState state, final Runnable endRunnable) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                state.scale = (Float)animation.getAnimatedValue();
                invalidate();
            }
        });
        if (endRunnable != null) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    endRunnable.run();
                }
            });
        }
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }
}
