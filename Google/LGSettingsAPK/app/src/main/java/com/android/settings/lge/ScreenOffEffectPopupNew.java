package com.android.settings.lge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import com.android.settings.Utils;
import com.lge.constants.SettingsConstants;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.settings.R;
import android.os.Build;

public class ScreenOffEffectPopupNew extends Activity {
    private String[] mRetro_first_set;
    private String[] mNot_retro_first_set;
    //private static final int NO_EFFECT_NUM = 0;
    //private static final int CIRCLE_EFFECT_NUM = 1;
    private static final int ANDROID_EFFECT_NUM = 2;
    private static final int ANI_REQUEST = 10;
    private int mCurrentEffect;
    private int mAniItem;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);

        mRetro_first_set = new String[] {
                getString(R.string.screen_off_effect_android_style_ex),
                getString(R.string.screen_off_effect_circle_ex),
                getString(R.string.screen_off_effect_none_ex2) };
        mNot_retro_first_set = new String[] {
                getString(R.string.screen_off_effect_none_ex2),
                getString(R.string.screen_off_effect_circle_ex),
                getString(R.string.screen_off_effect_android_style_ex) };
        mCurrentEffect = getInitState();
        mAniItem = mCurrentEffect;
        if (isNotSetScreenOffRetro()) {
            callPopup(mNot_retro_first_set, setRetroTVItem(mCurrentEffect));
        } else {
            callPopup(mRetro_first_set, setRetroTVItem(mCurrentEffect));
        }
    }

    private void callPopup(String[] items, int chekedItem) {
        // TODO Auto-generated method stub
        mCurrentEffect = setRetroTVItem(mAniItem);
        Dialog screenOffDialog;
        if (Utils.isFolderModel(this)) {
            screenOffDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, chekedItem, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCurrentEffect = which;
                        mAniItem = setRetroTVItem(which);
                    }
                })
                .setTitle(R.string.screen_off_effect_title)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.System.putInt(getContentResolver(),
                                        SettingsConstants.System.SCREEN_OFF_EFFECT_SET,
                                        mAniItem);
                                dialog.dismiss();
                                finish();
                            }
                        })
                .create();
        } else {
            screenOffDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, chekedItem, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCurrentEffect = which;
                        mAniItem = setRetroTVItem(which);
                    }
                })
                .setTitle(R.string.screen_off_effect_title)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.System.putInt(getContentResolver(),
                                        SettingsConstants.System.SCREEN_OFF_EFFECT_SET,
                                        mAniItem);
                                dialog.dismiss();
                                finish();
                            }
                        })
                .setNeutralButton(R.string.screen_off_effect_preview_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent aniIntent = new Intent();
                                aniIntent.setClassName("com.android.settings",
                                        "com.android.settings.lge.ScreenOffEffectMediaPlayer");
                                aniIntent.putExtra("animation", mAniItem);
                                startActivityForResult(aniIntent, ANI_REQUEST);
                                dialog.dismiss();
                            }
                        })
                .create();
        }
        screenOffDialog.show();
    }

    public int getInitState() {
        return Settings.System.getInt(getContentResolver(),
                SettingsConstants.System.SCREEN_OFF_EFFECT_SET, ANDROID_EFFECT_NUM);
    }

    public int setRetroTVItem(int item) {
        if (!isNotSetScreenOffRetro()) {
            if (item == 0) {
                return 2;
            }
            if (item == 2) {
                return 0;
            }
        }
        return item;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ANI_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (isNotSetScreenOffRetro()) {
                    callPopup(mNot_retro_first_set, mCurrentEffect);
                } else {
                    callPopup(mRetro_first_set, mCurrentEffect);
                }
            }
        }
    }

    private boolean isNotSetScreenOffRetro() {
        if (Utils.isG2Model()
                || "vu3".equals(Build.DEVICE)
                || "awifi".equals(Build.DEVICE)
                || Utils.isFolderModel(this)) {
            return true;
        }
        return false;
    }
}
