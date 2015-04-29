/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The preference class of keyboard skin apk list for Japanese IME.
 * This class notices to {@code iWnnIME} that the keyboard skin apk.
 *
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class KeyBoardSkinAddListPreference extends ListPreference {
    /** The Intent Action Code */
    private static final String KEYBOARDSKINADD_ACTION = "jp.co.omronsoft.iwnnime.ml.ADD_SKIN_LG";
    /** The package name of KeyboardSkin-Cozy  */
    private static final String KEYBOARDSKIN_COZY =
            "jp.co.omronsoft.iwnnime.ml.kbd.cozywall.iWnnIME_Kbd_Cozywall";
    /** The package name of KeyboardSkin-Marsh  */
    private static final String KEYBOARDSKIN_MARSH =
            "jp.co.omronsoft.iwnnime.ml.kbd.marshmallow.iWnnIME_Kbd_Marshmallow";
    /** The standard entry */
    private CharSequence mStandardEntry;
    /** The standard entry value */
    private CharSequence mStandardEntryValue;

    /**
     * Constructor
     */
    public KeyBoardSkinAddListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        CharSequence[] standardEntries = getEntries();
        CharSequence[] standardEntryValues = getEntryValues();
        mStandardEntry = standardEntries[0];
        mStandardEntryValue = standardEntryValues[0];
    }

    /** @see android.preference.DialogPreference#onDialogClosed */
    @Override protected void onPrepareDialogBuilder(Builder builder) {
        Context context = getContext();
        PackageManager pm = context.getPackageManager();

        List<ResolveInfo> resolveInfo = getKeyboardSkinInfo(context);

        int infoSize = resolveInfo.size();
        int keyboardskinSize = 0;
 
        for (int i = 0; i < infoSize; i++) {
           ResolveInfo info = resolveInfo.get(i);
           ActivityInfo actInfo = info.activityInfo;

            if (actInfo != null) {
                keyboardskinSize++;
            }
        }

        CharSequence[] entries = new CharSequence[keyboardskinSize + 1];
        CharSequence[] entryValues = new CharSequence[keyboardskinSize + 1];

        entries[0] = getLabel(mStandardEntry, context.getResources().getDrawable(R.drawable.standard_skin));
        entryValues[0] = mStandardEntryValue;

        int index = 0;
        entries[index] = getLabel(
                mStandardEntry, getContext().getResources().getDrawable(R.drawable.ime_keypad_popup_thumbnail_none));
        entryValues[index] = mStandardEntryValue;
        index++;

        for (int i = 0; i < infoSize; i++) {
            ResolveInfo info = resolveInfo.get(i);
            ActivityInfo actInfo = info.activityInfo;
            CharSequence label = info.loadLabel(pm);
            if (label == null) {
                if (actInfo != null) {
                    label = actInfo.name;
                } else {
                    label = "";
                }
            }

            entries[index] = getLabel(label, null);

            if (actInfo != null) {
                entryValues[index] = actInfo.name;
            } else {
                entryValues[index] = "";
            }
            index++;
        }

        setEntries(entries);
        setEntryValues(entryValues);

        SharedPreferences sharedPref = getSharedPreferences();
        if (sharedPref != null) {
            setValue(sharedPref.getString(getKey(),""));
        } else {
            setValue("");
        }

        super.onPrepareDialogBuilder(builder);
    }

    /** @see android.preference.DialogPreference#onDialogClosed */
    @Override protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            OpenWnn wnn = OpenWnn.getCurrentIme();
            int code = OpenWnnEvent.CHANGE_INPUT_VIEW;
            OpenWnnEvent ev = new OpenWnnEvent(code);
            try {
                wnn.onEvent(ev);
            } catch (Exception ex) {
                Log.e("OpenWnn", "KeyBoardSkinAddListPreference::onDialogClosed " + ex.toString());
            }
        }
    }

    private SpannableString getLabel(CharSequence label, Drawable icon) {
        Drawable d = icon;

        if (d == null) {
            d = getContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon);
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

    /** @see android.preference.ListPreference#onSetInitialValue */
    @Override protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, "");
    }

    /**
     * Get the information of keyboard skin apk.
     *
     * @param  context  The context.
     * @return The information of language package.
     */
    public static List<ResolveInfo> getKeyboardSkinInfo(Context context) {
        List<ResolveInfo> resolveInfo = new ArrayList<ResolveInfo>();
        if (context != null) {
            PackageManager pm = context.getPackageManager();

            Intent intent = new Intent(KEYBOARDSKINADD_ACTION);
            resolveInfo = pm.queryIntentActivities(intent, /* no flags */ 0);
            Collections.sort(resolveInfo, new ResolveInfo.DisplayNameComparator(pm));
        }
        return resolveInfo;
    }
}
