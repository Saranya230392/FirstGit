package com.android.settings;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;

public class DoubleTitleListPreference extends ListPreference {
    private TextView mTitle;
    private TextView mSubTitle;
    private View mView;
    private String mTitleRes = null;
    private String mSubTitleRes = null;
    private Context mContext;

    public DoubleTitleListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public DoubleTitleListPreference(Context context) {
        this(context, null);
        mContext = context;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_title_secondlines, null);
        //builder.setView(layout);
        builder.setCustomTitle(layout);

        mTitle = (TextView)layout.findViewById(R.id.title);
        if (mTitle != null && mTitleRes != null) {
            //mTitle.setText(R.string.sp_screen_timeout_NORMAL);
            mTitle.setText(mTitleRes);
        }

        mSubTitle = (TextView)layout.findViewById(R.id.subtitle);
        if (mSubTitle != null && mSubTitleRes != null) {
            //mSubTitle.setText(R.string.sp_screen_timeout_sub_title_NORMAL);
            mSubTitle.setText(mSubTitleRes);
        }

        mView = (View)layout.findViewById(R.id.view);

        if (Utils.isUI_4_1_model(mContext)) {
            if (mTitle != null) {
                mTitle.setBackgroundResource(R.color.double_title_background);
            }
            if (mView != null) {
                mView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        View view = (View)getDialog().getWindow().findViewById(
                com.android.internal.R.id.titleDivider);
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    public void setMainTitle(String res) {
        mTitleRes = res;
    }

    public void setSubTitle(String res) {
        mSubTitleRes = res;
    }
}
