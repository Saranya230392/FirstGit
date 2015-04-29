package com.android.settings.lge;

import java.util.ArrayList;

import com.android.settings.R;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TouchCrackSettingsActivity extends Activity {
    private static final String TAG = "TouchCrackSettingsActivity";
    /* Toast Msg */
    private Toast mToast = null;
    private String mMessage = null;

    private final int MAX_INDEX = 1;
    private ArrayList<TextView> itemList = new ArrayList<TextView>();
    private int mSelectedMenuIndex = 0;

    public static final int BYPASS_POWER_KEY = 0x40000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_lcd_crack_setting);

        itemList.add((TextView)findViewById(R.id.ok));
        itemList.add((TextView)findViewById(R.id.dont_show_again));

        enablePowerKeyHooking();
        setItemSelected(mSelectedMenuIndex);
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_POWER) {
            selectMenu();
            return true;
        } else if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
            moveUpMenu();
            return true;
        } else if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
            moveDownMenu();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean enablePowerKeyHooking() {

        WindowManager.LayoutParams attrs;
        attrs = getWindow().getAttributes();
        attrs.privateFlags |= BYPASS_POWER_KEY;
        getWindow().setAttributes(attrs);

        return true;
    }

    public void setItemSelected(int index) {
        if (index < 0 || index > MAX_INDEX) {
            return;
        }

        int size = itemList.size();
        for (int i = 0; i < size; i++) {
            TextView item = itemList.get(i);
            item.setBackgroundColor(Color.rgb(255, 255, 255));
        }

        itemList.get(index).setBackgroundColor(Color.rgb(195, 210, 215));
    }

    public void moveUpMenu() {
        mSelectedMenuIndex--;
        if (mSelectedMenuIndex < 0) {
            mSelectedMenuIndex = MAX_INDEX;
        }

        setItemSelected(mSelectedMenuIndex);
    }

    public void moveDownMenu() {
        mSelectedMenuIndex++;
        if (mSelectedMenuIndex > MAX_INDEX) {
            mSelectedMenuIndex = 0;
        }

        setItemSelected(mSelectedMenuIndex);
    }

    public void selectMenu() {
        /* confirm menu index */
        confirmMenuAndSendIntent(mSelectedMenuIndex);

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }

        /* Exit App */
        finish();

    }

    public void showToastMsg(Context context, String message) {

        if (null != context) {
            if ((null != message) && (0 < message.length())) {
                if (null == mToast) {
                    mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    mMessage = message;
                } else {
                    if (false == message.equals(mMessage)) {
                        mToast.setText(message);
                        mMessage = message;
                    }
                }

                if (null != mToast) {
                    mToast.show();
                }
            }
        }
    }

    public void confirmMenuAndSendIntent(int menuIndex) {
        int value = 0;

        switch (menuIndex) {
        case 0: // Ok
            value = 1;
            break;
        case 1: // Don't show again.
            value = 2;
            break;

        }

        Intent intent = new Intent();
        intent.setAction("com.lge.android.intent.action.TOUCHCRACK_MODE_EVENT");
        intent.putExtra("TOUCHCRACK_MODE", value);
        sendBroadcast(intent);
    }
}
