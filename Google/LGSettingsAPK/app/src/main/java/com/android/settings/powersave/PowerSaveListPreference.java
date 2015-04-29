package com.android.settings.powersave;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class PowerSaveListPreference extends ListPreference {

    private String mSummary;

    public PowerSaveListPreference(final Context context) {
        this(context, null);
    }

    public PowerSaveListPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {

        if (mSummary == null) {
            return super.getSummary();
        }
        else {
            return mSummary; //return String.format(summary.toString(), entry);
        }
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && mSummary != null) {
            mSummary = null;
        } else if (summary != null && !summary.equals(mSummary)) {
            mSummary = summary.toString();
        }
    }

}
