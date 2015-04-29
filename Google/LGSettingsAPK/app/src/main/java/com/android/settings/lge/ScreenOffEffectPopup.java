package com.android.settings.lge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
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

public class ScreenOffEffectPopup extends Activity {
    private String[] mRetro_first_set;
    private String[] mNot_retro_first_set;
    private static final int NO_EFFECT_NUM = 0;
    private static final int CIRCLE_EFFECT_NUM = 1;
    private static final int ANDROID_EFFECT_NUM = 2;

    private ScreenOffEffectAnimation mScreenOffAnimation;
    private int mCurrentEffect;
    private int mAniItem;

    private class ScreenOffEffectAnimation extends FrameLayout {
        private WindowManager windowManager;
        private WindowManager.LayoutParams windowLayoutParams;

        private ImageView aniImage;
        private AnimationDrawable aniDrawable;

        private Handler animHandler;
        private Runnable animRunnable;

        public ScreenOffEffectAnimation(Context context) {
            super(context);

            windowManager = (WindowManager)context
                    .getSystemService(Context.WINDOW_SERVICE);

            windowLayoutParams = new WindowManager.LayoutParams();
            windowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowLayoutParams.type =
                    WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
            windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            windowLayoutParams.format = PixelFormat.TRANSLUCENT;

            aniImage = new ImageView(context);
            animHandler = new Handler();

            aniImage.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(aniImage);
        }

        public void startAnimation() {
            windowManager.addView(this, windowLayoutParams);

            if (mAniItem == NO_EFFECT_NUM) {
                aniImage.setBackgroundResource(R.anim.ani_screen_off_noeffect);
            } else if (mAniItem == CIRCLE_EFFECT_NUM) {
                aniImage.setBackgroundResource(R.anim.ani_screen_off_circle);
            } else if (mAniItem == ANDROID_EFFECT_NUM) {
                aniImage.setBackgroundResource(R.anim.ani_screen_off_android_style);
            }

            int totalDuration = 0;
            aniDrawable = (AnimationDrawable)aniImage.getBackground();
            for (int i = 0; i < aniDrawable.getNumberOfFrames(); i++) {
                totalDuration += aniDrawable.getDuration(i);
            }

            aniDrawable.start();
            animHandler.postDelayed(animRunnable = new Runnable() {
                @Override
                public void run() {
                    initAnimResource();
                }
            }, totalDuration);
        }

        public void stopAnimation() {
            if (isAnimRunning()) {
                initAnimResource();
                animHandler.removeCallbacks(animRunnable);
            }
        }

        public boolean isAnimRunning() {
            return (aniDrawable != null && aniDrawable.isRunning());
        }

        private void initAnimResource() {
            aniDrawable.stop();
            aniDrawable = null;
            try {
                windowManager.removeView(ScreenOffEffectAnimation.this);
            } catch (Exception e) {
                Log.w("ScreenOffEffectPopup", "Exception");
            }
            aniImage.setBackgroundDrawable(null);
            System.gc();
        }
    }

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        mScreenOffAnimation = new ScreenOffEffectAnimation(ScreenOffEffectPopup.this);

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
        Dialog screenOffDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, chekedItem, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
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
                        if (!mScreenOffAnimation.isAnimRunning()) {
                            dialog.dismiss();
                            finish();
                        }
                    }
                })
                .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //int setItem = setRetroTVItem(mCurrentEffect);
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
                                // TODO Auto-generated method stub
                                if (!mScreenOffAnimation.isAnimRunning()) {
                                    mScreenOffAnimation.stopAnimation();
                                    mScreenOffAnimation.startAnimation();
                                    dialog.dismiss();
                                    if (isNotSetScreenOffRetro()) {
                                        callPopup(mNot_retro_first_set, mCurrentEffect);
                                    } else {
                                        callPopup(mRetro_first_set, mCurrentEffect);
                                    }
                                }
                            }
                        })
                .create();
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

    private boolean isNotSetScreenOffRetro() {
        if ((Utils.isG2Model() && !Utils.isUI_4_1_model(this))
                || "vu3".equals(Build.DEVICE)
                || "awifi".equals(Build.DEVICE)) {
            return true;
        }
        return false;
    }
}
