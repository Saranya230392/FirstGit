package com.android.settings.lge;

import com.android.settings.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class UsbTetheringHelp extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.usb_tethering_help);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView tv = (TextView)findViewById(R.id.usb_tether_help_contents);
        Spanned spanned = Html.fromHtml("&bull;");

        String contents = spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content1_NORMAL) + "\n"
                            + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content2_NORMAL) + "\n"
                            + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content3_NORMAL) + "\n"
                            + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content4_NORMAL) + "\n"
                            + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content5_NORMAL);
        tv.setText(contents);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
