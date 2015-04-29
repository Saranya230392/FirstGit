package com.lge.handwritingime.manager;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;

import com.lge.handwritingime.HandwritingKeyboard;
import com.lge.handwritingime.R;
import com.lge.handwritingime.data.Stroke;
import com.lge.handwritingime.data.StrokeChar;

public class ScreenManager {
    private final static String TAG = "LGHWIMEScreenManager";
    private static final boolean DEBUG = HandwritingKeyboard.DEBUG;

    private HandwritingKeyboard mContext;
    private TextModeManager mText;

    private static final int PORTRAIT = 0;
    private static final int LANDSCAPE = 1;
    private static final float BASERATIO = 100;
    private static final float MAXWIDTH = 6000;
    private static final int ANIMATION_DURATION = 200;

    private float mNextScreenWidth = 62;

    // common
    private int mNumScreens = 1;
    private int mIndexScreen = 0;
    private RectF mNextScreenRectF;

    // portrait/landscape
    private int mOrientation;
    private int mWidthScreens[];
    private int mHeightScreens[];

    public ScreenManager(HandwritingKeyboard keyboard, TextModeManager mode) {
        mContext = keyboard;
        mText = mode;

        mOrientation = PORTRAIT;
        mWidthScreens = new int[2];
        mHeightScreens = new int[2];
        mNextScreenRectF = new RectF(0, 0, 0, 0);
    }

    public RectF getNextScreenRectF() {
        if (mIndexScreen != mNumScreens) {
            return mNextScreenRectF;
        }
        return null;
    }

    public int getWidthScreen() {
        return mWidthScreens[mOrientation];
    }

    public int getHeightScreen() {
        return mHeightScreens[mOrientation];
    }

    public void scrollScreen(boolean bLeft) {
        if (bLeft) {
            if (mIndexScreen > 0) {
                --mIndexScreen;
                recaleNextScreenRectF(getWidthScreen(), getHeightScreen());
                mText.mStrokeViewGroupLayout.scrollBy(-getScrollSize(), 0);
            }
        } else {
            if (mIndexScreen < mNumScreens - 1) {
                ++mIndexScreen;
                recaleNextScreenRectF(getWidthScreen(), getHeightScreen());
                recalcScroll(false);
                mText.mStrokeViewGroupLayout.scrollBy(getScrollSize(), 0);
            }
        }
        updateScreenArrow();
    }

    public void setAnimation(View v) {
        if (v.getAnimation() != null && v.getAnimation().hasStarted()) {
            return;
        }
        TranslateAnimation slideLeft = new TranslateAnimation(getScrollSize(), 0, 0, 0);
        if (slideLeft != null) {
            slideLeft.setDuration(ANIMATION_DURATION);
            slideLeft.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationEnd(Animation arg0) {
                    mText.updateCharButton();
                    // TODO : update Screen.
                    // updateScreenArrow();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }
            });
            v.startAnimation(slideLeft);
        }
    }

    public void goCurrentScreen(boolean bDelStroke) {
        float curX = 0;
        if (bDelStroke) {
            if (StrokeManager.getInstance().size() <= 0)
                // getInstance
                return;

            Stroke curStroke = StrokeManager.getInstance().get(StrokeManager.getInstance().size() - 1);
            if (curStroke.size() > 0)
                // curX = curStroke.getX(curStroke.size() - 1);
                curX = curStroke.getMaxX();
            else
                return;
        } else {
            StrokeChar sc = null;
            if (StrokeCharManager.getInstance().size() > 0)
                sc = StrokeCharManager.getInstance().get(StrokeCharManager.getInstance().size() - 1);
            if (sc != null)
                curX = sc.getMaxX();
            else
                return;
        }

        int indexScreen = findScreenIndex(unnormalizePoint(curX), true);
        if (indexScreen >= 0) {
            if (mIndexScreen != indexScreen) {
                mIndexScreen = indexScreen;
                recaleNextScreenRectF(getWidthScreen(), getHeightScreen());
                recalcScroll(true);
                updateScreenArrow();
            }
        }
    }

    public void updateScreenArrow() {
        ImageButton btn = (ImageButton) mText.mStrokeLayout.findViewWithTag("beforeScreen");
        if (btn != null) {
            if (mIndexScreen > 0) {
                btn.setVisibility(View.VISIBLE);
            } else {
                btn.setVisibility(View.INVISIBLE);
            }
        }
        btn = (ImageButton) mText.mStrokeLayout.findViewWithTag("afterScreen");
        if (btn != null) {
            if (mNumScreens > mIndexScreen + 1) {
                btn.setVisibility(View.VISIBLE);
            } else {
                btn.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void clear() {
        mNumScreens = 1;
        mIndexScreen = 0;

        recaleNextScreenRectF(getWidthScreen(), getHeightScreen());
        recalcScroll(true);
        updateScreenArrow();
    }

    public void init() {
        WindowManager wm = (WindowManager) HandwritingKeyboard.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        Configuration config = mContext.getResources().getConfiguration();

        mOrientation = (config.orientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT : LANDSCAPE;

        int padding = mContext.getResources().getDimensionPixelSize(R.dimen.stroke_layout_padding_left_right);
        int width = size.x - (padding * 2);
        int height = mContext.getResources().getDimensionPixelSize(R.dimen.stroke_layout_height);
        if (getWidthScreen() == 0) {
            mWidthScreens[mOrientation] = width;
            mHeightScreens[mOrientation] = height;
        }
        if (DEBUG) Log.i(TAG, "width " + width + " height " + height);
    }

    public void recalcScreen(float maxX, float curX) {
        maxX = unnormalizePoint(maxX);
        curX = unnormalizePoint(curX);

        int numScreens = 1;
        int indexScreen = 0;
        if (maxX > 0) {
            if (maxX <= getWidthScreen()) {
                numScreens = 2;
            } else {
                numScreens = (int) (maxX - getWidthScreen()) / getScrollSize();
                numScreens += 3;
            }
            while (mWidthScreens[mOrientation] + (numScreens - 1) * getScrollSize() > MAXWIDTH) {
                numScreens--;
            }

            mNumScreens = numScreens;
            indexScreen = findScreenIndex(curX, true);
            if (indexScreen >= 0) {
                mIndexScreen = indexScreen;
            } else {
                mIndexScreen = mNumScreens - 1;
            }
        } else {
            mNumScreens = numScreens;
            mIndexScreen = indexScreen;
        }

        recaleNextScreenRectF(getWidthScreen(), getHeightScreen());

        if (DEBUG) Log.i(TAG, " curX " + curX + " maxX " + maxX);
        if (DEBUG) Log.i(TAG, "getWidthScreen() " + getWidthScreen() + " getHeightScreen() " + getHeightScreen()
                    + " numScreens " + numScreens + " indexScreen " + indexScreen);
    }

    @Deprecated
    private int getWidthTotScreen() {
        return mWidthScreens[mOrientation] + (mNumScreens - 1) * getScrollSize();
    }

    public void recalcScroll(boolean bScroll) {
        if (mText.mStrokeViewGroupLayout == null) {
            return;
        }
        ViewGroup.LayoutParams params = mText.mStrokeViewGroupLayout.getLayoutParams();
        params.width = getWidthTotScreen();
        mText.mStrokeViewGroupLayout.setLayoutParams(params);
        if (bScroll)
            mText.mStrokeViewGroupLayout.scrollTo(mIndexScreen * getScrollSize(), 0);
    }

    // for MSG_SCROLL_RIGHT_SCREEN
    public void autoScrollRight(View v) {
        float x = 0;
        StrokeManager strokeTextManager = StrokeManager.getInstance();
        if (!strokeTextManager.isEmpty()) {
            x = unnormalizePoint(strokeTextManager.get(strokeTextManager.size() - 1).getMaxX());
        }

        if (!isLastScreen() && mNextScreenRectF.contains(x, mNextScreenRectF.top)) {
            scrollScreen(false);
            setAnimation(v);
        }

        updateScreenArrow();
    }

    public void changeScreenSize() {
        float maxX = StrokeManager.getInstance().getMaxX();
        if (DEBUG) Log.i(TAG, "[shrink()] cond " + (getWidthTotScreen() - getScrollSize() * 2) + " getNextScreenWidth "
                    + getNextScreenWidthPx());
        if (DEBUG) Log.i(TAG, "[shrink()] maxX " + maxX + " mNumScreens " + mNumScreens);
        if ((unnormalizePoint(maxX) <= getWidthTotScreen() - getScrollSize() * 2)) {
            if (mNumScreens > 1) {
                --mNumScreens;
                mIndexScreen = Math.min(mIndexScreen, mNumScreens - 1);
            }
            updateScreenArrow();
        } else {
            // result = grow(maxX);
            if ((unnormalizePoint(maxX) > (getWidthTotScreen() - getScrollSize()))) {
                mNumScreens++;
                if (getWidthTotScreen() > MAXWIDTH) {
                    mNumScreens--;
                }
                updateScreenArrow();
            }
            if (DEBUG) Log.i(TAG, "[shrink()] mNumScreens " + mNumScreens);
        }
    }

    public void recaleNextScreenRectF(float width, float height) {
        float left = (width + mIndexScreen * (width - getNextScreenWidthPx())) - getNextScreenWidthPx();
        float right = left + getNextScreenWidthPx();

        mNextScreenRectF.set(left, 0, right, height);
    }

    private float getNextScreenWidthPx() {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (mNextScreenWidth * density);
    }

    public void setNextScreenWidthDip(float nextScreenWidth) {
        mNextScreenWidth = nextScreenWidth;
    }

    private int findScreenIndex(float strokeX, boolean additory) {
        int nResult = -1;
        int nLeft = 0;
        int nRight = 0;
        for (int i = 0; i < mNumScreens; i++) {
            nLeft = nRight;
            nRight = (i + 1) * getScrollSize();
            if (!additory)
                nRight += getNextScreenWidthPx();
            if ((int) strokeX > nLeft && (int) strokeX < nRight) {
                nResult = i;
                break;
            }
        }
        return nResult;
    }

    private int getScrollSize() {
        return mWidthScreens[mOrientation] - (int) getNextScreenWidthPx();
    }

    private float unnormalizePoint(float value) {
        return (mHeightScreens[mOrientation] / BASERATIO) * value;
    }

    public boolean isLastScreen() {
        if ((mWidthScreens[mOrientation] + (mIndexScreen + 1) * getScrollSize()) > MAXWIDTH) {
            return true;
        }
        return false;
    }
}
