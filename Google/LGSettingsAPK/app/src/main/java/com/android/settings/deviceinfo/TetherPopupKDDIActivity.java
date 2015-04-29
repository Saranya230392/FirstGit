package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import com.android.settings.R;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.Utils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TetherPopupKDDIActivity extends AlertActivity
        implements DialogInterface.OnClickListener, CompoundButton.OnClickListener {

    private static final String url = "https://cs.kddi.com/smt_i/te/";

    private View mView;

    //private boolean isFinishing = false;
    //private boolean mSelectOk = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AlertController.AlertParams p = mAlertParams;

        p.mTitle = getString(R.string.tethering_popup_kddi_title);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.tethering_popup_kddi_ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.tethering_popup_kddi_cancel);
        p.mNegativeButtonListener = this;
        if (!Utils.isUI_4_1_model(this)) {
		    p.mIconAttrId = android.R.attr.alertDialogIcon;
		}
        setupAlert();

    }

    private View createView() {
        mView = getLayoutInflater().inflate(R.layout.tether_popup_kddi, null);

        TextView link = (TextView)mView.findViewById(R.id.intro_link);

        String urlText = " <a href='" + url + "'>"
                + getResources().getString(R.string.tethering_popup_kddi_web) + "</a>";
        CharSequence textlink = Html.fromHtml(getResources().getString(
                R.string.tethering_popup_kddi_link, urlText));
        link.setText(textlink);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        CheckBox rememberChoice = (CheckBox)mView.findViewById(R.id.tether_popup_kddi_check);
        rememberChoice.setChecked(false);
        rememberChoice.setOnClickListener(this);
        return mView;
    }

    @Override
    public void onClick(View arg0) {
    }

    public void onClick(DialogInterface arg0, int arg1) {
        // TODO Auto-generated method stub

        switch (arg1) {
        case DialogInterface.BUTTON_POSITIVE:
            //mSelectOk = true;
            checkDoNotShow();
            TetherPopupKDDIActivity.this.setResult(Activity.RESULT_OK);
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            //mSelectOk = false;
            break;
        default:
            break;
        }
        //isFinishing = true;
    }

    private void checkDoNotShow() {
        if (mView == null) {
            return;
        }

        final CheckBox donotshow = (CheckBox)mView.findViewById(R.id.tether_popup_kddi_check);
        if (donotshow.isChecked()) {
            Settings.System.putInt(this.getContentResolver(),
                    "TETHER_POPUP_KDDI", 1);
        } else {
            Settings.System.putInt(this.getContentResolver(),
                    "TETHER_POPUP_KDDI", 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            //isFinishing = true;
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
