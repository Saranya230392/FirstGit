/*
***************************************************************************
** FontSettingsPreference.java
***************************************************************************
**
** 2012.07.17 Android JB
**
** Mobile Communication R&D Center, Hanyang
** Sangmin, Lee (TMSword) ( Mobile Communication R&D Center / Senior Researcher )
**
** This code is a program that changes the font.
**
***************************************************************************
*/
package com.android.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class FontSettingsPreference extends ListPreference
{
    private FontServerConnection mRemoteFontServer;
    private Context mContext;
    private int mDefaultTypefaceIndex = 0;
    private FontListAdapter mFontListAdapter;
    private boolean mFontServerConnected = false;
    private boolean mRetryShowDialog = false;

    // After the service connection, processing.
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case FontServerConnection.FONT_SERVER_CONNECTED:
                initFontSettings();
                break;

            default:
                break;
            }
        }
    };

    public FontSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void connectFontServer() {
        if (mRemoteFontServer == null) {
            mRemoteFontServer = new FontServerConnection(mContext, mHandler);
            mRemoteFontServer.connectFontServerService();
        }
    }

    public void disconnectFontServer() {
        if (mRemoteFontServer != null) {
            mRemoteFontServer.disconnectFontServerService();
            mRemoteFontServer = null;
        }
    }

    public void updateFontServer() {
        if (mRemoteFontServer != null) {
            mRemoteFontServer.updateFontServer(); // system settings only.
            mDefaultTypefaceIndex = mRemoteFontServer.getDefaultTypefaceIndex();
        }
    }

    public FontServerConnection getFontServer() {
        return mRemoteFontServer;
    }

    private void initFontSettings() {
        setFontServerConnected(true);
        updateFontServer();

        // 20131024 dongseok.lee:[Z/KDDI][TD103958]exception when rotate orientation quickly[START]
        // 20121123 dongseok.lee : check null
        if (mRemoteFontServer != null) {
            setSummary(mRemoteFontServer.getSummary());
            mFontListAdapter = new FontListAdapter(mContext, FontSettingsPreference.this);
            if (hasRetryShowDialog()) {
                setRetryShowDialog(false);
                showDialog(null);
            }
        }
        // 20131024 dongseok.lee:[Z/KDDI][TD103958]exception when rotate orientation quickly[END]
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        initFontSettings(); //Dialog When loading, update Font Server after Bind status check of Font Server
        super.onPrepareDialogBuilder(builder); //2012 1113 Font List Focusing correct

        if (FontDownloadConnection.isStartUpPreloadModel() == true
                || FontDownloadConnection.isInstalledLGSmartWorld(mContext) == true) {
            builder.setPositiveButton(
                    mContext.getResources().getString(R.string.sp_add_fonts_NORMAL),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            FontDownloadConnection.connectLGSmartWorld(mContext);
                        }
                    }
                    );
        } else {
            builder.setPositiveButton(null, null); // Hide : Download link button.
        }

        builder.setNegativeButton(
                mContext.getResources().getString(R.string.sp_btn_font_cancel_SHORT),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
                );

        builder.setAdapter(mFontListAdapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDefaultTypefaceIndex != which) {
                            // 20121123 dongseok.lee : check null
                            if (mRemoteFontServer != null) {
                                mRemoteFontServer.selectDefaultTypeface(which);
                            }
                        } else {
                            Toast.makeText(mContext,
                                    R.string.sp_font_type_select_nitify_msg_NORMAL,
                                    Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                }
                );
    }

    /****************************************************************************
    ** LCD Rotate.
    ****************************************************************************/
    private void setRetryShowDialog(boolean retry) {
        mRetryShowDialog = retry;
    }

    private boolean hasRetryShowDialog() {
        return mRetryShowDialog;
    }

    private void setFontServerConnected(boolean connected) {
        mFontServerConnected = connected;
    }

    private boolean hasFontServerConnected() {
        return mFontServerConnected;
    }

    protected void showDialog(Bundle state) {
        if (!hasFontServerConnected()) {
            setRetryShowDialog(true);
        } else {
            // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [S]
            // Just handle no support delete feature case
            if (!isSupportFontDelete()) {
                super.showDialog(state);
            }
            // 20140205 gaenoo.lee@lge.com Apply Font type delete feature [E]
        }
    }

    private boolean isSupportFontDelete() {
        return mContext.getResources().getBoolean(R.bool.font_type_support_delete_feature);
    }

}
