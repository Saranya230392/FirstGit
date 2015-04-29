/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;

/**
 * The preference class of use One touch emoji list setting list for Japanese IME.
 * This class notices to {@code iWnnIME} that the use one touch emoji list setting.
 *
 * @author Copyright (C) 2012, OMRON SOFTWARE CO., LTD.
 */
public class OneTouchEmojiListPreference extends ListPreference {

    /** The standard entries */
    private CharSequence[] mStandardEntries;
    /** Values of label image id */
    private int[] mImageIds = { R.drawable.ime_keypad_popup_thumbnail_left,
                                R.drawable.ime_keypad_popup_thumbnail_right,
                                R.drawable.ime_keypad_popup_thumbnail_none };

    /** Constructor */
    public OneTouchEmojiListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mStandardEntries = getEntries();
    }

    /** @see android.preference.DialogPreference#onDialogClosed */
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        CharSequence[] labeledEntries = new CharSequence[mStandardEntries.length];
        for (int i = 0; i < mStandardEntries.length; i++) {
            Drawable d = KeyboardSkinData.getInstance().getDrawable(mImageIds[i]);
            if (d == null) {
                d = getContext().getResources().getDrawable(mImageIds[i]);
            }
            labeledEntries[i] = getLabel(mStandardEntries[i],d);
        }
        setEntries(labeledEntries);
        super.onPrepareDialogBuilder(builder);
    }

    /**
     * Generate label that was appended icon.
     *
     * @param label    Label string
     * @param icon     Append icon
     * @return generated label
     */
    private SpannableString getLabel(CharSequence label, Drawable icon) {
        Drawable d = icon;
        if (d == null) {
            return  new SpannableString(label);
        }

        int dWidth = d.getIntrinsicWidth();
        int dHeight = d.getIntrinsicHeight();
        d.setBounds(0, 0, dWidth, dHeight);
        ListPreferenceSpan span = new ListPreferenceSpan(d, DynamicDrawableSpan.ALIGN_BOTTOM,
                getContext().getResources().getDimensionPixelSize(R.dimen.list_preference_span_string_gap),
                dHeight,
                getContext().getResources().getDimensionPixelSize(R.dimen.list_preference_span_right_gap));

        SpannableString spannable = new SpannableString("+" + label);
        spannable.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
