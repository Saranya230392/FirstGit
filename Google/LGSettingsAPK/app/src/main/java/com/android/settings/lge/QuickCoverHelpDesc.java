package com.android.settings;

import android.R.attr;
import android.R.string;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class QuickCoverHelpDesc extends Preference {

    public QuickCoverHelpDesc(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        // TODO Auto-generated constructor stub
        setLayoutResource(R.layout.quick_cover_view);

    }

    public QuickCoverHelpDesc(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub

    }
}
