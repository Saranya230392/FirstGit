/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.text.Spanned;
import android.text.Styled;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.HashMap;

import com.lge.handwritingime.BaseHandwritingKeyboard;

import jp.co.omronsoft.android.text.EmojiDrawable;
import jp.co.omronsoft.iwnnime.ml.decoemoji.DecoEmojiUtil;

/**
 * The default candidates view manager using {@link TextView}.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class CandidateTextView extends TextView {
    /** dialog */
    private Dialog mCandidateDialog = null;

    /** OpenWnn instance */
    private InputMethodBase mWnn;

    /** Temporary Paint for getTextWidths. */
    private static Paint sTmpPaint = new Paint(); 

    /** Temporary TextPaint for getTextWidths. */
    private static TextPaint sTmpTextPaint = new TextPaint();

    /** Temporary FontMetricsInt for getTextWidths. */
    private static Paint.FontMetricsInt sTmpFontMetricsInt = new Paint.FontMetricsInt();

    /** Compsite Unicode hash map */
    private static final HashMap<Integer, Integer> COMPOSITE_UNICODE_TABLE = new HashMap<Integer, Integer>() {{
        put(0x1F1EF, 0x1F1F5);
        put(0x1F1FA, 0x1F1F8);
        put(0x1F1EB, 0x1F1F7);
        put(0x1F1E9, 0x1F1EA);
        put(0x1F1EE, 0x1F1F9);
        put(0x1F1EC, 0x1F1E7);
        put(0x1F1EA, 0x1F1F8);
        put(0x1F1F7, 0x1F1FA);
        put(0x1F1E8, 0x1F1F3);
        put(0x1F1F0, 0x1F1F7);
        put(0x0023, 0x20E3);
        put(0x0031, 0x20E3);
        put(0x0032, 0x20E3);
        put(0x0033, 0x20E3);
        put(0x0034, 0x20E3);
        put(0x0035, 0x20E3);
        put(0x0036, 0x20E3);
        put(0x0037, 0x20E3);
        put(0x0038, 0x20E3);
        put(0x0039, 0x20E3);
        put(0x0030, 0x20E3);
    }};

   /**
    * Constructor
    * @param context    context
    */
    public CandidateTextView(Context context) {
        super(context);
    }

    /** @see View#setBackgroundDrawable */
    @Override public void setBackgroundDrawable(Drawable d) {
        super.setBackgroundDrawable(d);
        // These values will be overwritten with 0 when setting candidates.
        // Therefore these values are useful just in case of 1 Line Canditates (for v2.1-T, v2.2-T).
        Resources res = getContext().getResources();
        setPadding(res.getDimensionPixelSize(R.dimen.candidate_default_padding_left),
                   res.getDimensionPixelSize(R.dimen.candidate_default_padding_top),
                   res.getDimensionPixelSize(R.dimen.candidate_default_padding_right),
                   res.getDimensionPixelSize(R.dimen.candidate_default_padding_bottom));
    }

    /**
     * Display word dialog.
     *
     * @param builder  The dialog builder
     */
    public void displayCandidateDialog(Dialog builder) {
        if (mCandidateDialog != null) {
            mCandidateDialog.dismiss();
            mCandidateDialog = null;
        }
        mCandidateDialog = builder;
        Window window = mCandidateDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        lp.dimAmount = 0.6f;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mCandidateDialog.show();
    }

    /** @see android.view.View#onWindowVisibilityChanged */
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if ((visibility != VISIBLE) && (mCandidateDialog != null)) {
                mCandidateDialog.dismiss();
        }
    }
    /**
     * Called when handle touch screen motion events.
     * 
     * @param me The motion event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if((mWnn != null) && !(mWnn instanceof BaseHandwritingKeyboard)) {
            if (mWnn.mInputViewManager.isPopupKeyboard()){
                mWnn.mInputViewManager.closePopupKeyboard();
                return true;
            }
        }
        return super.onTouchEvent(me);
    }

    /**
     * OpenWnn object setter.
     * 
     * @param Wnn The OpenWnn object
     */
    public void setmWnn(InputMethodBase Wnn) {
        mWnn = Wnn;
    }

    /**
     * Get the advance widths for the characters in the string contains emoji.
     *
     * @param text   The text to be measured.
     * @param paint  The paint.
     * @return       advance width of the text.
     */
    public static int getTextWidths(CharSequence text, TextPaint paint) {
        int result = 0;

        if (text == null || paint == null) {
            return result;
        }

        final int length = text.length();
        float[] charWidths = new float[length]; 

        if (text instanceof Spanned) {
            synchronized (sTmpTextPaint) {
                result = Styled.getTextWidths(paint, sTmpTextPaint, (Spanned) text,
                        0, text.length(), charWidths, sTmpFontMetricsInt);
            }
        } else {
            result = paint.getTextWidths(text.toString(), charWidths);
        }

        sTmpPaint.setTextSize(paint.getTextSize());
        sTmpPaint.setTextScaleX(paint.getTextScaleX());

        String str = text.toString();
        int first = 0;
        int second = 0;
        int cpCount = 0;

        for (int i = 0; i < length; i++) {
            first = str.codePointAt(i);
            cpCount = Character.charCount(first);
            if (i < (length - cpCount)) {
                second = str.codePointAt(i + cpCount);
            } else {
                second = 0;
            }
            if (isCompositeUnicode(first, second)) {
                charWidths[i + cpCount] = 0;
                i += cpCount;
                cpCount = Character.charCount(second);
                i += cpCount;
                continue;
            }

            final char high = text.charAt(i);
            if (Character.isHighSurrogate(high)) {
                final char low = text.charAt(i + 1);
                if (Character.isLowSurrogate(low)) {
                    final int codePoint = Character.toCodePoint(high, low);
                    if (EmojiDrawable.isEmoji(codePoint)) {
                        charWidths[i] = EmojiDrawable.getEmojiWidth(codePoint, sTmpPaint);
                    }
                    i++;
                }
            }
        }

        for(int cnt = 0; cnt < charWidths.length; cnt++) {
            result += charWidths[cnt];
        }
        return result;
    }
    /**
     * Returns whether the code is the Compsite Unicode.
     *
     * @param first   Unicode6 first code.
     * @param second  Unicode6 second code.
     * @return        Boolean type if the code is Compsite Unicode.
     */
    private static boolean isCompositeUnicode(int first, int second) {
        boolean ret = false;
        if (COMPOSITE_UNICODE_TABLE.containsKey(first)) {
            if (COMPOSITE_UNICODE_TABLE.get(first) == second) {
                ret = true;
            }
        }
        return ret;
    }
}
