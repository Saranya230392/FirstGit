package com.lge.handwritingime;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;

public class TextDrawable extends Drawable {
    private final static boolean DEBUG = HandwritingKeyboard.DEBUG;
    private final static String TAG = "LGHWIMETextDrawable";
        
    private final static float DEFAULT_TEXT_SIZE = 44f;
    private final static int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;

    private final static float DEFAULT_SHADOW_RADIUS = 5f;
    private final static int DEFAULT_SHADOW_COLOR = 0xFF000000;
    
    private String mText;
    private TextPaint mTextPaint;
    private float mGapBottom;
    
    public TextDrawable(String text) {
        this(text, DEFAULT_TEXT_SIZE, DEFAULT_TEXT_COLOR, DEFAULT_SHADOW_COLOR);
    }
    
    public TextDrawable(String text, float textSize, int textColor, int shadowColor) {
        super();
        
        mTextPaint = new TextPaint();
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Align.CENTER);

        mText = text;
        mGapBottom=0;
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        mTextPaint.setShadowLayer(DEFAULT_SHADOW_RADIUS, 0, 0, shadowColor);
    }
    
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
    }
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
    }
    
    public void setShadowColor(int shadowColor) {
        mTextPaint.setShadowLayer(DEFAULT_SHADOW_RADIUS, 0, 0, shadowColor);
    }
    
    public void setGapBottom(float gap) {
        mGapBottom = gap;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = getBounds();
                
        float fontHeight = Math.abs(mTextPaint.descent()+mTextPaint.ascent())/2;
        float x = rect.exactCenterX();
        float y = rect.exactCenterY() + fontHeight - mGapBottom;
        if (DEBUG) Log.d(TAG, "x=" + x + " y=" + y);
        canvas.drawText(mText, x, y, mTextPaint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        // Alpha is not supported
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
     // Color Filter is not supported.
    }

}
