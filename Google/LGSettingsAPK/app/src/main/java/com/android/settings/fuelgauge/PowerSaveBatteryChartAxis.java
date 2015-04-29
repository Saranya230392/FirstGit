package com.android.settings.fuelgauge;

import com.android.settings.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.android.settings.Utils;
import java.util.Locale;

public class PowerSaveBatteryChartAxis extends View {
    private int mLevelTop;
    private int mLevelBottom;
    private int mLayoutLevelHeight = 0;

    final TextPaint mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    int mThinLineWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            2, getResources().getDisplayMetrics());

    private Layout mLayoutLevel = makeLayout("100%");
    //private Layout mLayoutTime = makeLayout("00:00");

    final int mPadding4 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            4, getResources().getDisplayMetrics());

    private int mPadding22 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            22, getResources().getDisplayMetrics());

    public PowerSaveBatteryChartAxis(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mLevelTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                18, getResources().getDisplayMetrics());

        mPaint.setStrokeWidth(mThinLineWidth);
        mPaint.setARGB(255, 145, 145, 145);

        mLayoutLevelHeight = mLayoutLevel.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = getViewWidth();

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        final int height = getHeight();

        mLevelBottom = height - mPadding22;

        for (int i = 0; i <= 10; i++) { // jongtak0920
            int y = mLevelTop + ((mLevelBottom - mLevelTop) * i) / 10;

            if (Utils.isRTLLanguage()) {
                mLayoutLevel = makeLayout("%"
                        + String.format(Locale.getDefault(), "%d", (100 - i * 10)));
            } else {
                mLayoutLevel = makeLayout((100 - i * 10) + "%");
            }

            if (mLayoutLevel != null) {
                canvas.save();
                canvas.translate(getViewWidth() - mLayoutLevel.getWidth() - mPadding4
                        - mThinLineWidth, y - (mLayoutLevelHeight / 2));
                mLayoutLevel.draw(canvas);
                canvas.restore();
            }
        }
        canvas.drawLine(getViewWidth() - mPaint.getStrokeWidth(), mLevelTop, getViewWidth()
                - mPaint.getStrokeWidth(), mLevelBottom + mPaint.getStrokeWidth(), mPaint);
        canvas.drawLine(getViewWidth() - mPaint.getStrokeWidth(), mLevelBottom
                + (mThinLineWidth / 2), getViewWidth(), mLevelBottom + (mThinLineWidth / 2), mPaint);
    }

    public int getViewWidth() {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                38, getResources().getDisplayMetrics());
    }

    private Layout makeLayout(CharSequence text) {
        final Resources res = getResources();
        final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.density = res.getDisplayMetrics().density;
        paint.setCompatibilityScaling(res.getCompatibilityInfo().applicationScale);
        paint.setColor(res.getColor(R.color.graph_text_color));
        paint.setTextSize(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, res.getDisplayMetrics()));

        return new StaticLayout(text, paint,
                (int)Math.ceil(Layout.getDesiredWidth(text, paint)),
                Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
    }
}
