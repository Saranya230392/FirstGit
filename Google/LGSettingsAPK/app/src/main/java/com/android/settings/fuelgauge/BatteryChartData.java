package com.android.settings.fuelgauge;

import android.graphics.Canvas;
import android.graphics.Paint;

public class BatteryChartData {
    static final int CHART_DATA_X_MASK = 0x0000ffff;
    static final int CHART_DATA_BIN_MASK = 0xffff0000;
    static final int CHART_DATA_BIN_SHIFT = 16;

    int[] mColors;
    Paint[] mPaints;

    int mNumTicks;
    int[] mTicks;
    int mLastBin;

    void setColors(int[] colors) {
        mColors = colors;
        mPaints = new Paint[colors.length];
        for (int i = 0; i < colors.length; i++) {
            mPaints[i] = new Paint();
            mPaints[i].setColor(colors[i]);
            mPaints[i].setStyle(Paint.Style.FILL);
        }
    }

    void init(int width) {
        if (width > 0) {
            mTicks = new int[width * 2];
        } else {
            mTicks = null;
        }
        mNumTicks = 0;
        mLastBin = 0;
    }

    void addTick(int x, int bin) {
        if (bin != mLastBin && mNumTicks < mTicks.length) {
            mTicks[mNumTicks] = x | bin << CHART_DATA_BIN_SHIFT;
            mNumTicks++;
            mLastBin = bin;
        }
    }

    void finish(int width) {
        if (mLastBin != 0) {
            addTick(width, 0);
        }
    }

    void draw(Canvas canvas, int top, int height) {
        int lastBin = 0;
        int lastX = 0;
        int bottom = top + height;
        for (int i = 0; i < mNumTicks; i++) {
            int tick = mTicks[i];
            int x = tick & CHART_DATA_X_MASK;
            int bin = (tick & CHART_DATA_BIN_MASK) >> CHART_DATA_BIN_SHIFT;
            if (lastBin != 0 && mPaints.length > lastBin) { // yonguk.kim 20120404 Array Boundar
                canvas.drawRect(lastX, top, x, bottom, mPaints[lastBin]);
            }
            lastBin = bin;
            lastX = x;
        }
    }

}
