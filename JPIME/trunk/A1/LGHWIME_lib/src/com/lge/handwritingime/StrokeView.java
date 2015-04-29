package com.lge.handwritingime;

import java.util.ArrayList;
import java.util.EventListener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.lge.handwritingime.data.Stroke;

// originally from com.example.android.apis.graphics.FingerPaint in ApiDemos
public class StrokeView extends View {
    private final static String TAG = "LGHWIMEStrokeView";
    private final static boolean DEBUG = HandwritingKeyboard.DEBUG && false;

    private Paint mPaint, mRectPaint;
    int mBackgroundColor, mRectColor;
    private MaskFilter mEmboss;
    private OnStrokeListener mOnStrokeListener;
    private Path mPath;
//    private Paint mDebugPaint;

    private boolean mLastScreen;
    private boolean mAutoScroll;
    private float mStrokeWidth;
    private RectF mNextScreenRectF;
    private float mX, mY;
    private float mRatio;

    private static final float TOUCH_TOLERANCE = 16f;
    private static final float POINT_CORRECTION = 1f;

    // called when touch the screen.
    public interface OnStrokeListener extends EventListener {
        public void onPreStartStrokeEvent(StrokeView v);

        public void onStartStrokeEvent(float x, float y);

        public void onStrokeEvent(float x, float y);

        public void onEndStrokeEvent(float x, float y);
    }

    // some constructors are needed when parse a xml file.
    public StrokeView(Context context) {
        super(context);
        init(context);
    }

    public StrokeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public StrokeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mBackgroundColor = getContext().getResources().getColor(R.color.stroke_view_bg_color);
        mRectColor = getContext().getResources().getColor(R.color.stroke_view_rect_color);

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.9f, 4, 3.5f);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(0xFF000000);
        if (!DEBUG)
            mPaint.setMaskFilter(mEmboss);
        if (DEBUG)
            mPaint.setAlpha(0x55);

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setDither(true);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setStrokeJoin(Paint.Join.ROUND);
        mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        mRectPaint.setColor(mRectColor);

//        if (DEBUG) {
//            mDebugPaint = new Paint(mPaint);
//            mDebugPaint.setColor(0xFF00FF00);
//        }

        mRatio = getResources().getDimension(R.dimen.stroke_layout_height) / 100;
        mLastScreen = false;
        mAutoScroll = true;
        mStrokeWidth = 10;
        mPaint.setStrokeWidth(mStrokeWidth);
        mPath = new Path();

        if (DEBUG) Log.d(TAG, "init completed");
    }
    
    public void setRectColor(int color) {
        mRectColor = color;
        if (mRectPaint != null) {
            mRectPaint.setColor(mRectColor);
        }
    }
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public void setStrokeWidth(int value) {
        mStrokeWidth = value;
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    public void setPaintColor(String paintColor) {
//        mPaint.setColor(Color.parseColor(paintColor));
        int colorId = getResources().getIdentifier(paintColor,"color", getContext().getPackageName());
        if (colorId == 0) {
            Log.e(TAG, "paintColor=" + paintColor);
        }
        
        mPaint.setColor(getResources().getColor(colorId));
        
    }

    public void setNextScreenRectF(RectF value) {
        mNextScreenRectF = value;
    }

    public void setAutoScroll(boolean value) {
        mAutoScroll = value;
    }

    public void setLastScreen(boolean value) {
        mLastScreen = value;
    }

    public boolean isAutoScroll() {
        return mAutoScroll;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (DEBUG) Log.d(TAG, "w=" + oldw + "->" + w + " h=" + oldh + "->" + h);
        // redraw();

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);
        if (!mLastScreen && mAutoScroll && mNextScreenRectF != null) {
            canvas.drawRect(mNextScreenRectF, mRectPaint);
        }
        canvas.drawPath(mPath, mPaint);
    }

    private int mActiveId = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int pointerIndex = event.findPointerIndex(mActiveId);

        if ((mNextScreenRectF != null && x > mNextScreenRectF.right) || getAnimation() != null
                && getAnimation().hasStarted()) {
            if (mActiveId != -1) {
                touchUp();
                mActiveId = -1;
            }
            return false;
        }
        if (DEBUG) {
            mPath.addCircle(x, y, mPaint.getStrokeWidth(), Direction.CW);
        }

        if (DEBUG) Log.d(TAG, "Motion:" + event.getAction() + " x=" + x + " y=" + y + " norX=" + x / mRatio +
                " norY=" + y / mRatio);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
            if (mActiveId == -1) {
                touchStart(x, y);
                mActiveId = event.getPointerId(0);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (pointerIndex != -1) {
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);
                touchMove(x, y);
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            if (pointerIndex != -1) {
                touchUp();
                mActiveId = -1;
            }
            break;
        }
        return true;
    }

    private void touchStart(float x, float y) {
        if (mOnStrokeListener != null) {
            mOnStrokeListener.onPreStartStrokeEvent(this);
        }
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        if (mOnStrokeListener != null) {
            mOnStrokeListener.onStartStrokeEvent(x / mRatio, y / mRatio);
        }
        invalidate();
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            if (mOnStrokeListener != null) {
                mOnStrokeListener.onStrokeEvent(x / mRatio, y / mRatio);
            }
            invalidate();
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY + 0.01f);
        if (mOnStrokeListener != null) {
            mOnStrokeListener.onEndStrokeEvent(mX / mRatio, mY / mRatio + POINT_CORRECTION);
        }
        invalidate();
    }

    public void clearView() {
        if (getWidth() <= 0 && getHeight() <= 0) {
            return;
        }
        mPath.reset();
        invalidate();
    }

    public void remakePath(ArrayList<Stroke> strokeList) {
        if (strokeList == null) {
            return;
        }
        mPath.reset();
        for (int i = 0; i < strokeList.size(); i++) {
            Stroke stroke = strokeList.get(i);
            if (stroke == null || stroke.size() <= 0) {
                continue;
            }
            mPath.moveTo(stroke.getX(0) * mRatio, stroke.getY(0) * mRatio);
            for (int j = 1; j < stroke.size() - 1; j++) {
                float toX = (stroke.getX(j - 1) + stroke.getX(j)) * mRatio / 2;
                float toY = (stroke.getY(j - 1) + stroke.getY(j)) * mRatio / 2;
                mPath.quadTo(stroke.getX(j - 1) * mRatio, stroke.getY(j - 1) * mRatio, toX, toY);
            }
            mPath.lineTo(stroke.getX(stroke.size() - 1) * mRatio, (stroke.getY(stroke.size() - 1) - POINT_CORRECTION)
                    * mRatio + 0.01f);
        }
    }

    public void redraw(ArrayList<Stroke> strokeList) {
        remakePath(strokeList);
        invalidate();
    }

    public boolean isEmpty() {
        return mPath.isEmpty();
    }

    public void setOnStrokeListener(OnStrokeListener l) {
        mOnStrokeListener = l;
    }

    public void setPenType(String penColor, String penType) {
        setPaintColor(penColor);
        if (penType.equals(getResources().getString(R.string.HW_PEN_TYPE_BALLPEN))) {
            setStrokeWidth(10);
        } else if (penType.equals(getResources().getString(R.string.HW_PEN_TYPE_INKPEN))) {
            setStrokeWidth(7);
        } else if (penType.equals(getResources().getString(R.string.HW_PEN_TYPE_BRUSH))) {
            setStrokeWidth(15);
        } else if (penType.equals(getResources().getString(R.string.HW_PEN_TYPE_PENCIL))) {
            setStrokeWidth(4);
        }
    }

    /*
     * private void updateDebug(Canvas canvas) { if (mHWKeyboard != null) {
     * canvas.drawText(String.format("X=%8.04f, Y=%8.04f", mX, mY),
     * mHWKeyboard.mScreenManager.getCurrentScreenLeft(), 20f, mDebugPaint);
     * canvas.drawText(String.format("[screen] tot=%d ind=%d",
     * mHWKeyboard.mScreenManager.getNumScreens(),
     * mHWKeyboard.mScreenManager.getIndexScreen()),
     * mHWKeyboard.mScreenManager.getCurrentScreenLeft(), 50f, mDebugPaint); }
     * // if (mHWKeyboard != null && mHWKeyboard.mCharManager != null &&
     * mHWKeyboard.mCharManager.getCharRectF() != null) { //
     * canvas.drawRect(mHWKeyboard.mCharManager.getCharRectF(), mDebugPaint); //
     * } canvas.drawRect(mNextScreenRectF, mDebugPaint); }
     */
}