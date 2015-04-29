package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import com.android.settings.R;
import com.android.settings.lge.OverlayUtils;

public class UsbTetherIntroUSCActivity extends AlertActivity
        implements DialogInterface.OnClickListener {

    private static final String url = "http://www.uscellular.com/uscellular/common/common.jsp?path=/android/modem_feature.html";

    private View mView;

    private boolean mSelectOk = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AlertController.AlertParams p = mAlertParams;

        p.mTitle = getString(R.string.sp_usb_tether_intro_usc_title_NORMAL);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.sp_accept_NORMAL);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.cancel);
        p.mNegativeButtonListener = this;

        setupAlert();

    }

    private View createView() {
        mView = getLayoutInflater().inflate(R.layout.usb_tether_intro_usc, null);

        TextView link = (TextView)mView.findViewById(R.id.intro_link);
        String urlText = " <a href='" + url + "'>"
                + getResources().getString(R.string.sp_usb_tether_intro_usc_link_NORMAL) + "</a>";
        CharSequence textlink = Html.fromHtml(getResources().getString(
                R.string.sp_usb_tether_intro_usc_text3_NORMAL, urlText));
        link.setText(textlink);
        link.setMovementMethod(LinkMovementMethod.getInstance());

        return mView;
    }

    public void onClick(DialogInterface arg0, int arg1) {
        // TODO Auto-generated method stub

        switch (arg1) {
        case DialogInterface.BUTTON_POSITIVE:
            mSelectOk = true;
            UsbTetherIntroUSCActivity.this.setResult(Activity.RESULT_OK);
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            mSelectOk = false;
            break;
        default:
            break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
