package com.android.settings;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

public class MultiTaskingGuide {
    private ImageView mAniImage;
    private AnimationDrawable mAni;

    Context mContext;
    LayoutInflater inf;
    View mView;
    Resources mRes;
    ImageView mPlayMultitasking;
    Runnable mRunAni;

    boolean bRunAni = false;

    CheckBox mCheckBox;

    public MultiTaskingGuide(Context context) {
        mContext = context;
        mContext.setTheme(Resources.getSystem().getIdentifier("Theme.LGE.White", "style", "com.lge.internal"));

        inf = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inf.inflate(R.layout.multitasking_guide, null);

        mPlayMultitasking = (ImageView)mView.findViewById(R.id.play_multitasking);
        mAniImage = (ImageView)mView.findViewById(R.id.ani_multitasking);
        mAni = (AnimationDrawable)mAniImage.getDrawable();
//        mAniImage.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (bRunAni) {
//                    mPlayMultitasking.setVisibility(View.VISIBLE);
//                    mAni.stop();
//                    mAniImage.removeCallbacks(mRunAni);
//                    bRunAni = false;
//                }
//                return false;
//            }
//        });
//        mPlayMultitasking.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
                mPlayMultitasking.setVisibility(View.GONE);
                mAniImage.postDelayed(mRunAni = new Runnable() {
                    @Override
                    public void run() {
                        if (mView != null) {
                            mAniImage = (ImageView)mView.findViewById(R.id.ani_multitasking);
                            mAni = (AnimationDrawable)mAniImage.getDrawable();
                            mAni.start();

                            bRunAni = true;
                        }
                    }
                }, 1);
//                mView.playSoundEffect(SoundEffectConstants.CLICK);
//                return false;
//            }
//        });

    }

    public View getView() {
        return mView;
    }

    public void cleanUp() {
        mAni.stop();
        mPlayMultitasking.setOnTouchListener(null);
        mAniImage.removeCallbacks(mRunAni);
       mAniImage.setBackgroundDrawable(null);
        mRunAni = null;
        mAni = null;
         Utils.recycleView(mView);
        mView = null;
    }
}
