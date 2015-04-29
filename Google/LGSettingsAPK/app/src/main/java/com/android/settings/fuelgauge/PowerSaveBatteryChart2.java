/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.fuelgauge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.SystemClock;
import android.os.BatteryStats.HistoryItem;
import com.lge.constants.SettingsConstants;
import android.telephony.ServiceState;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.HorizontalScrollView;
import android.provider.Settings;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;

public class PowerSaveBatteryChart2 extends HorizontalScrollView {
    // jongtak0920.kim 120817 Edit Graph GUI [START]
    final int mPaddingText18 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            18, getResources().getDisplayMetrics());
    final int mPaddingText22 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            22, getResources().getDisplayMetrics());
    // jongtak0920.kim 120817 Edit Graph GUI [END]

    private static final String TAG = "PowerSaveBatteryChart2";

    final Paint mBatteryBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint mBatteryGoodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // jongtak0920
    final Paint mBatteryEstimatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // jongtak0920
    final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    // jongtak0920
    final Paint mGrayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint mAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    final Path mBatEstimateDischargingLevelPath = new Path();
    final Path mBatEstimateChargingLevelPath = new Path();
    // jongtak0920
    final Path mBatLevelPath = new Path();

    private final List<Path> mBatBackgroundPath = new ArrayList<Path>();
    private final List<Path> mBatPath = new ArrayList<Path>();

    BatteryStats mStats;
    long mStatsPeriod;

    int mTextAscent;
    int mTextDescent;
    int mDurationStringWidth;
    int mTotalDurationStringWidth;

    boolean mLargeMode;

    int mLineWidth;
    int mThinLineWidth;
    int mLevelOffset;
    int mLevelTop;
    int mLevelBottom;

    int mNumHist;
    long mHistStart;
    long mHistEnd;
    int mBatLow;
    int mBatHigh;

    // jongtak0920
    private int scrollPosition;

    private int lastLevelPositon;
    private int lastTimePosition;

    //private final int minTime = 3; // minimum time ( min )
    private final int minRefTime = 10; // minimum reference time ( min )

    private int onDischargingTotalLevel = 0;
    private long onDischargingTotalTime = 0;

    private int onChargingTotalLevel = 0;
    private long onChargingTotalTime = 0;

    private int lastStatus = 0;
    private int lastLevel = 0;
    private long lastTime = 0;

    private long mEstimateDischargingTime = 0;
    private long mEstimateChargingTime = 0;

    private static final int UNIT_HOUR = 1;
    private static final int REFERENCE_HOUR = 3;
    //private static final int UNIT_LAYOUT = 3;
    private static final int ESTIMATE_LAYOUT = 12;

    final int mPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            3.3f, getResources().getDisplayMetrics());
    final int mGraphPadding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            38, getResources().getDisplayMetrics());

    public final int DEFAULT_LINE = 0;
    public final int ESTIMATE_LINE = 1;

    private int chargingDefaultstate = DEFAULT_LINE;
    private int dischargingDefaultstate = DEFAULT_LINE;

    private int unitWidth;
    private double realTimeGraphWidth;

    private long timeStart;
    private long timeChange;

    final Calendar now = Calendar.getInstance();

    private Layout mRefLayout;
    private Layout mRefTimeLayout = makeLayout("+24"
            + getResources().getString(R.string.sp_powersave_battery_use_hours_text_NORMAL));
    private Layout mRefDateLayout = makeLayout(DateFormat.getTimeFormat(getContext()).format(
            now.getTime()));
    //private Layout mRefLevelLayout = makeLayout("100%");
    // jongtak0920

    //private ContentResolver mContentResolver;

    public PowerSaveBatteryChart2(Context context, AttributeSet attrs) {
        super(context, attrs);
        //mContentResolver = context.getContentResolver();

        // jongtak0920.kim 120817 Edit Graph GUI [START]
        mBatteryBackgroundPaint.setARGB(51, 190, 190, 190);
        // jongtak0920.kim 120817 Edit Graph GUI [END]
        mBatteryBackgroundPaint.setStyle(Paint.Style.FILL);
        mBatteryGoodPaint.setARGB(255, 61, 196, 226);
        mBatteryGoodPaint.setStyle(Paint.Style.STROKE);

        // jongtak0920
        DashPathEffect dashEffect = new DashPathEffect(new float[] { 5, 5 }, 1);
        mBatteryEstimatePaint.setPathEffect(dashEffect);
        mBatteryEstimatePaint.setARGB(255, 61, 196, 226);
        mBatteryEstimatePaint.setStyle(Paint.Style.STROKE);
        // jongtak0920

        // jongtak0920
        mGrayPaint.setStrokeWidth(1.0f);
        // jongtak0920

        if (mRefTimeLayout.getWidth() > mRefDateLayout.getWidth()) {
            mRefLayout = mRefTimeLayout;
        } else {
            mRefLayout = mRefDateLayout;
        }
    }

    void setStats(BatteryStats stats) {
        mStats = stats;

        long uSecTime = mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000,
                BatteryStats.STATS_SINCE_CHARGED);
        mStatsPeriod = uSecTime;

        int pos = 0;

        mBatLow = 0;
        mBatHigh = 100;
        boolean first = true;

        //jongtak
        // calculate hist start time and end time
        if (stats.startIteratingHistoryLocked()) {
            final HistoryItem rec = new HistoryItem();
            while (stats.getNextHistoryLocked(rec)) {
                pos++;
                if (rec.cmd == HistoryItem.CMD_UPDATE) {
                    if (first) {
                        first = false;
                        mHistStart = rec.time;
                    }
                    mHistEnd = rec.time;
                }
            }
        }
        mNumHist = pos;

        if (mHistEnd <= mHistStart) {
            mHistEnd = mHistStart + 1;
        }
        // jongtak0920.kim@lge.com 120807 [START]
        // mTotalDurationString = Utils.formatElapsedTime(getContext(), mHistEnd - mHistStart);
        // jongtak0920.kim@lge.com 120807 [END]

        timeChange = mHistEnd - mHistStart;
        timeStart = mHistStart;
    }

    public long getStatsPeriod() {
        return mStatsPeriod;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTextAscent = (int)mTextPaint.ascent();
        mTextDescent = (int)mTextPaint.descent();

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = getViewWidth();

        setMeasuredDimension(widthSize, heightSize);

    }

    void finishPaths(int w, int h, int levelh, int startX, int y, Path curLevelPath,
            int lastX, Path lastPath) {
        if (curLevelPath != null) {
            if (lastX >= 0 && lastX < w) {
                if (lastPath != null) {
                    lastPath.lineTo(w, y);
                }
                curLevelPath.lineTo(w, y);
            }
            curLevelPath.lineTo(w, mLevelTop + levelh);
            curLevelPath.lineTo(startX, mLevelTop + levelh);
            curLevelPath.close();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // jongtak0920.kim@lge.com 120817 Edit Graph GUI [START]
        int textHeight = mTextDescent - mTextAscent + mPaddingText18;
        // jongtak0920.kim@lge.com 120817 Edit Graph GUI [END]
        mThinLineWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                2, getResources().getDisplayMetrics());
        //        if (h > (textHeight*6)) {
        mLargeMode = true;
        if (h > (textHeight * 15)) {
            // Plenty of room for the chart.
            mLineWidth = textHeight / 2;
        } else {
            // Compress lines to make more room for chart.
            mLineWidth = textHeight / 3;
        }
        // jongtak0920.kim@lge.com 120817 Edit Graph GUI [START]
        //            mLevelTop = textHeight + paddingText18;
        // jongtak0920.kim@lge.com 120817 Edit Graph GUI [END]

        mLevelTop = mPaddingText18;

        //        } else {
        //            mLargeMode = false;
        //            mLineWidth = mThinLineWidth;
        //            mLevelTop = 0;
        //        }
        if (mLineWidth <= 0) {
            mLineWidth = 1;
        }
        mTextPaint.setStrokeWidth(mThinLineWidth);
        mTextPaint.setARGB(255, 145, 145, 145);
        mBatteryGoodPaint.setStrokeWidth(mThinLineWidth);
        mBatteryEstimatePaint.setStrokeWidth(mThinLineWidth);

        if (mLargeMode) {
            //int barOffset = textHeight + mLineWidth;
            mLevelOffset = /*mPhoneSignalOffset +*/((mLineWidth * 3) / 2);
        } else {
            mLevelOffset = mLineWidth * 3;
        }

        generatePath(w, h);
        // jongtak0920
    }

    private void generatePath(int w, int h) {
        mBatLevelPath.reset();
        mBatBackgroundPath.clear();
        mBatPath.clear();

        mBatEstimateDischargingLevelPath.reset();
        mBatEstimateChargingLevelPath.reset();

        final int batChange = mBatHigh - mBatLow;

        //        mLevelBottom = mLevelTop + levelh;
        mLevelBottom = h - mPaddingText22;
        final int levelh = mLevelBottom - mLevelTop;

        int x = 0;
        int y = 0;
        int startX = 0;
        int startPoint = mRefLayout.getWidth() / 2;

        int lastX = -1;
        int lastY = -1;
        int i = 0;

        double curTimePosition = startPoint + realTimeGraphWidth;
        int arraySize = (int)(realTimeGraphWidth / (unitWidth * ESTIMATE_LAYOUT)) + 1;
        int pathWidth = (int)(realTimeGraphWidth / arraySize);
        int pathNum = 0;

        Log.i(TAG, "arraySize : " + arraySize);
        Log.i(TAG, "pathWidth : " + pathWidth);

        for (int j = 0; j < arraySize; j++) {
            mBatBackgroundPath.add(new Path());
            mBatPath.add(new Path());
        }

        Path curLevelPath = null;
        Path lastLinePath = null;
        Path path = null;
        Path paintPath = null;
        Path dischargingPath = null;
        Path chargingPath = null;

        final int N = mNumHist;

        Log.i(TAG, "Hist number : " + mNumHist);
        Log.i(TAG, "realTimeGraphWidth : " + realTimeGraphWidth);
        Log.i(TAG, "unitWidth : " + unitWidth);
        Log.i(TAG, "timeChange : " + timeChange);

        if (mStats.startIteratingHistoryLocked()) {
            final HistoryItem rec = new HistoryItem();
            while (mStats.getNextHistoryLocked(rec) && i++ < N) { // jongtak0920

                if (rec.cmd == BatteryStats.HistoryItem.CMD_UPDATE) {

                    x = startPoint
                            + (int)(((rec.time - timeStart) * realTimeGraphWidth) / timeChange);
                    y = mLevelBottom - (rec.batteryLevel * levelh) / batChange;

                    //                    Log.i(TAG, "x : "+x);

                    if (lastX != x) {
                        // We have moved by at least a pixel.
                        if (lastY != y) {
                            // Don't plot changes within a pixel.
                            //byte value = rec.batteryLevel;

                            if (x > (pathNum + 1) * curTimePosition / arraySize) {
                                if (pathNum < arraySize - 1) {
                                    pathNum++;
                                }
                            }

                            if (x <= (pathNum + 1) * curTimePosition / arraySize) {
                                if (pathNum == 0) {
                                    path = mBatPath.get(pathNum);
                                    paintPath = mBatBackgroundPath.get(pathNum);
                                } else {
                                    Log.i("hong", "pathNum : " + pathNum);
                                    path = mBatPath.get(pathNum);

                                    if (paintPath == mBatBackgroundPath.get(pathNum - 1)) {
                                        mBatBackgroundPath.get(pathNum - 1).lineTo(x, y);
                                        mBatBackgroundPath.get(pathNum - 1).lineTo(x, mLevelBottom);
                                        mBatBackgroundPath.get(pathNum - 1).lineTo(startX,
                                                mLevelBottom);
                                        mBatBackgroundPath.get(pathNum - 1).close();
                                        curLevelPath = null;
                                        paintPath = mBatBackgroundPath.get(pathNum);
                                    }
                                }
                            }

                            // path = mBatPath3;
                            dischargingPath = mBatEstimateDischargingLevelPath;
                            chargingPath = mBatEstimateChargingLevelPath;

                            if (path != lastLinePath) {
                                if (lastLinePath != null) {
                                    lastLinePath.lineTo(x, y);
                                }
                                if (path != null) {
                                    path.moveTo(x, y);
                                }
                                lastLinePath = path;
                            } else {
                                if (path != null) {
                                    path.lineTo(x, y);
                                }
                            }

                            if (curLevelPath == null) {
                                // curLevelPath = mBatLevelPath;
                                curLevelPath = mBatBackgroundPath.get(pathNum);
                                curLevelPath.moveTo(x, y);
                                startX = x;
                            } else {
                                curLevelPath.lineTo(x, y);
                            }

                            // jongtak0920 calculate battery information during last 3 hours
                            if (rec.time >= mHistEnd - REFERENCE_HOUR * HOUR_IN_MILLIS) {
                                if (rec.batteryStatus == lastStatus) {
                                    if (rec.batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING
                                            || rec.batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
                                        if (Math.abs(rec.batteryLevel - lastLevel) <= 1) {
                                            onChargingTotalLevel += Math.abs(rec.batteryLevel
                                                    - lastLevel);
                                            onChargingTotalTime += Math.abs(rec.time - lastTime);
                                        }
                                    } else {
                                        if (Math.abs(rec.batteryLevel - lastLevel) <= 1) {
                                            onDischargingTotalLevel += Math.abs(rec.batteryLevel
                                                    - lastLevel);
                                            onDischargingTotalTime += Math.abs(rec.time - lastTime);
                                        }
                                    }
                                }
                            }

                            lastStatus = rec.batteryStatus;
                            lastLevel = rec.batteryLevel;
                            lastTime = rec.time;

                            lastX = x;
                            lastY = y;
                        }
                    }
                }
                else if (rec.cmd != BatteryStats.HistoryItem.CMD_OVERFLOW) {
                    if (curLevelPath != null) {
                        finishPaths(x + 1, h, levelh, startX, lastY, curLevelPath, lastX,
                                lastLinePath);
                        lastLevelPositon = lastY;
                        lastX = lastY = -1;
                        curLevelPath = null;
                        lastLinePath = null;
                    }
                }

            }
        }

        // Draw Path Until Current Time
        lastX = startPoint + (int)(((mHistEnd - timeStart) * realTimeGraphWidth) / timeChange);
        if (lastLinePath != null) {
            lastLinePath.lineTo(lastX, lastY);
        }
        if (curLevelPath != null) {
            curLevelPath.lineTo(lastX, lastY);
        }

        // calculate scroll position
        scrollPosition = lastX
                - (int)(((UNIT_HOUR * HOUR_IN_MILLIS) * realTimeGraphWidth) / timeChange);
        if (lastY > -1) {
            lastLevelPositon = lastY;
        }
        lastTimePosition = lastX;

        //        finishPaths(realTimeGraphWidth, h, levelh, startX, lastY, curLevelPath, lastX, lastLinePath);
        if (curLevelPath != null) {
            curLevelPath.lineTo(lastX, mLevelBottom);
            curLevelPath.lineTo(startX, mLevelBottom);
            curLevelPath.close();
        }

        if (dischargingPath != null) {
            dischargingPath.moveTo(lastX, lastY);
        }

        if (chargingPath != null) {
            chargingPath.moveTo(lastX, lastY);
        }


        final long elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;

        final long drainTime = mStats.computeBatteryTimeRemaining(
                elapsedRealtimeUs);
        final long chargeTime = mStats.computeChargeTimeRemaining(
                elapsedRealtimeUs);

        // on battery estimate path [START]
        if (drainTime == -1/*onDischargingTotalTime < minTime * MINUTE_IN_MILLIS || onDischargingTotalLevel == 0*/) {
            dischargingDefaultstate = DEFAULT_LINE;
            mEstimateDischargingTime = lastLevel * (minRefTime * MINUTE_IN_MILLIS); // 10 min per 1% battery (default)
        } else {
            dischargingDefaultstate = ESTIMATE_LINE;
            mEstimateDischargingTime = drainTime / 1000;
        }

        if (w < (lastX + (int)((mEstimateDischargingTime * unitWidth) / HOUR_IN_MILLIS))) {
            long deltaY = (lastLevel * (w - lastX))
                    / (int)((mEstimateDischargingTime * unitWidth) / HOUR_IN_MILLIS);
            if (dischargingPath != null) {
                dischargingPath.lineTo(w, mLevelBottom - ((lastLevel - deltaY) * levelh)
                        / batChange);
            }
        } else {
            if (dischargingPath != null) {
                dischargingPath.lineTo(lastX + (int)
                        ((mEstimateDischargingTime * unitWidth) / HOUR_IN_MILLIS), mLevelBottom);
            }
        }
        Log.i(TAG, "onDischargingTotalTime : " + onDischargingTotalTime
                + ", onDischargingTotalLevel : " + onDischargingTotalLevel
                + ", mEstimateDischargingTime : " + mEstimateDischargingTime);
        // on battery estimate path [END]

        // on charging estimate path [START]
        if (chargeTime == -1/*onChargingTotalTime < minTime * MINUTE_IN_MILLIS || onChargingTotalLevel == 0*/) {
            chargingDefaultstate = DEFAULT_LINE;
            mEstimateChargingTime = (mBatHigh - lastLevel) * (minRefTime * MINUTE_IN_MILLIS); // 10 min per 1% battery (default)
        } else {
            chargingDefaultstate = ESTIMATE_LINE;
            mEstimateChargingTime = chargeTime / 1000;
        }

        if (w < (lastX + (int)((mEstimateChargingTime * unitWidth) / HOUR_IN_MILLIS))) {
            long deltaY = (lastLevel * (w - lastX))
                    / (int)((mEstimateChargingTime * unitWidth) / HOUR_IN_MILLIS);
            if (chargingPath != null) {
                chargingPath.lineTo(w, mLevelBottom - ((lastLevel + deltaY) * levelh) / batChange);
            }
        } else {
            if (chargingPath != null) {
                chargingPath.lineTo(lastX + (int)
                        ((mEstimateChargingTime * unitWidth) / HOUR_IN_MILLIS), mLevelTop);
            }
        }

        Log.i(TAG, "onChargingTotalTime : " + onChargingTotalTime + ", onChargingTotalLevel : "
                + onChargingTotalLevel + ", mEstimateChargingTime : " + mEstimateChargingTime);
        // on charging estimate path [END]
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = getWidth();
        //final int height = getHeight();

        // Draw vertical grid and X axis layout
        int layoutX = lastTimePosition + ESTIMATE_LAYOUT * unitWidth;
        int drawHour = ESTIMATE_LAYOUT;
        Layout mLayout;

        mGrayPaint.setARGB(255, 185, 185, 185);

        while (layoutX >= 0) {
            if (drawHour == 0) {
                canvas.save();

                mLayout = makeLayout(DateFormat.getTimeFormat(getContext()).format(now.getTime()));
                canvas.translate(layoutX - mLayout.getWidth() / 2, mLevelBottom + mPadding);
                mLayout.draw(canvas);
                canvas.restore();
            } else if (drawHour % 3 == 0) {
                canvas.save();
                //final Calendar now = Calendar.getInstance();
                mLayout = makeLayout(String.format("%+d", drawHour)
                        + getResources().getString(
                                R.string.sp_powersave_battery_use_hours_text_NORMAL));
                canvas.translate(layoutX - mLayout.getWidth() / 2, mLevelBottom + mPadding);
                mLayout.draw(canvas);
                canvas.restore();
            }

            canvas.drawLine(layoutX, mLevelTop, layoutX, mLevelBottom, mGrayPaint);
            layoutX -= unitWidth;
            drawHour--;
        }

        //        if (mLargeMode) {
        // jongtak
        //int powerSaveMode = Settings.System.getInt(mContentResolver,
        //        SettingsConstants.System.POWER_SAVE_MODE, -1);

        mGrayPaint.setARGB(255, 228, 228, 228);

        for (int i = 0; i < 10; i++) { // yonguk.kim 20120420 PowerSave start immediatly
            int y = mLevelTop + ((mLevelBottom - mLevelTop) * i) / 10;
            canvas.drawLine(0, y, width, y, mGrayPaint); // jongtak Draw chart grid

            // jongtak0920 remove power save mode line
            //                if((powerSaveMode > -1) && (powerSaveMode==(10-i)*10)) {
            //                    canvas.drawLine(0, y, width, y, mTextRedPaint);
            //                }
        }

        // canvas.drawPath(mBatLevelPath, mBatteryBackgroundPaint);
        for (Path p : mBatBackgroundPath) {
            if (!p.isEmpty()) {
                canvas.drawPath(p, mBatteryBackgroundPaint);
            }
        }

        for (Path p : mBatPath) {
            if (!p.isEmpty()) {
                canvas.drawPath(p, mBatteryGoodPaint);
            }
        }

        if (PowerSaveBatteryDetail.getCurState() == PowerSaveBatteryDetail.CHART_STATUS_CHARGING) {
            canvas.drawPath(mBatEstimateChargingLevelPath, mBatteryEstimatePaint);
            Log.i(TAG, "Draw Charging path");
        } else {
            canvas.drawPath(mBatEstimateDischargingLevelPath, mBatteryEstimatePaint);
            Log.i(TAG, "Draw Discharging path");
        }

        // jongtak Draw axis
        mTextPaint.reset();
        mTextPaint.setStrokeWidth(mThinLineWidth);
        mTextPaint.setARGB(255, 145, 145, 145);

        canvas.drawLine(0, mLevelBottom + (mThinLineWidth / 2), width,
                mLevelBottom + (mThinLineWidth / 2), mTextPaint);
        //            canvas.drawLine(mRefTimeLayoutLevelWidth, mLevelTop, mRefTimeLayoutLevelWidth,
        //                    mLevelBottom+(mThinLineWidth/2), mTextPaint);

        //        }
    }

    private Layout makeLayout(CharSequence text) {
        final Resources res = getResources();
        final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.density = res.getDisplayMetrics().density;
        paint.setCompatibilityScaling(res.getCompatibilityInfo().applicationScale);
        paint.setColor(res.getColor(R.color.graph_text_color));
        paint.setTextSize(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, res.getDisplayMetrics()));
//B2_TD54818_Dispaly of the number, symbol (-/+) and the hour on the battery usage alignment
        if ("iw".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            return new StaticLayout("\u202A" + text + "\u202C", paint,
                    (int)Math.ceil(Layout.getDesiredWidth(text, paint)),
                    Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
        }
        else {
            return new StaticLayout(text, paint,
                    (int)Math.ceil(Layout.getDesiredWidth(text, paint)),
                     Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
        }
    }

    public long getEstimateChargingTime() {
        return mEstimateChargingTime;
    }

    public long getEstimateDischargingTime() {
        return mEstimateDischargingTime;
    }

    public int getOnChargingTotalLevel() {
        return onChargingTotalLevel;
    }

    public int getOnDischargingTotalLevel() {
        return onDischargingTotalLevel;
    }

    public int getChargingDefaultstate() {
        return chargingDefaultstate;
    }

    public int getDischargingDefaultstate() {
        return dischargingDefaultstate;
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public int getLastLevelPosition() {
        return lastLevelPositon;
    }

    public int getLastTimePosition() {
        return lastTimePosition;
    }

    public int getViewWidth() {
        int windowSize = getContext().getResources().getDisplayMetrics().widthPixels
                - mGraphPadding * 2;

        unitWidth = windowSize / 6; // 1hour
        realTimeGraphWidth = timeChange * unitWidth / HOUR_IN_MILLIS;

        return (int)(realTimeGraphWidth + unitWidth * ESTIMATE_LAYOUT + mRefLayout.getWidth());
    }

}
