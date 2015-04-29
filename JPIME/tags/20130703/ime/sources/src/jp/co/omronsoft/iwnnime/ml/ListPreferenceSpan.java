/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/**
 * The span class of list preference.
 * 
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class ListPreferenceSpan extends ImageSpan {

    /** String Gap */
    private int mStringGap;
    /** Image Height */
    private int mImageHeight;
    /** Rigth Gap */
    private int mRigthGap;

    /**
     * Constructor
     * @param d  Drawable of image.
     * @param verticalAlignment one of {@link android.text.style.DynamicDrawableSpan#ALIGN_BOTTOM} or
     * {@link android.text.style.DynamicDrawableSpan#ALIGN_BASELINE}.
     * @param stringGap  String gap.
     * @param imageHeight  Image height.
     * @param rightGap  Right gap.
     */
    public ListPreferenceSpan(Drawable d, int verticalAlignment,int stringGap, int imageHeight, int rightGap) {
        super(d, verticalAlignment);
        mStringGap = stringGap;
        mImageHeight = imageHeight;
        mRigthGap = rightGap;
    }

    /** @see android.text.style.DynamicDrawableSpan#getSize */
    @Override public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        int result = super.getSize(paint, text, start, end, fm);
        if (fm != null) {
            fm.ascent += mStringGap;
            fm.top = fm.ascent;
        }
        return result + mRigthGap;
    }

    /** @see android.text.style.DynamicDrawableSpan#draw */
    @Override public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x, 
                     int top, int y, int bottom, Paint paint) {
        int imageBottom = 0;
        if (paint != null) {
            FontMetrics fm = paint.getFontMetrics();
            int stringHeight = (int) Math.abs(fm.ascent) + (int) Math.abs(fm.descent);
            imageBottom = Math.abs(mImageHeight - stringHeight) / 2;
        }
        super.draw(canvas, text, start, end, x, top, y, bottom + imageBottom, paint);
    }
}
