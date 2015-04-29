package com.android.settings.hotkey;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;

public class AppListItemWrapper {
    View base;
    TextView label = null;
    ImageView icon = null;

    AppListItemWrapper(View base) {
        super();
        this.base = base;
    }

    TextView getLabel() {
        if (label == null) {
            label = (TextView)base.findViewById(R.id.label);
        }
        return label;
    }

    ImageView getIcon() {
        if (icon == null) {
            icon = (ImageView)base.findViewById(R.id.icon);
        }
        return icon;
    }
}
