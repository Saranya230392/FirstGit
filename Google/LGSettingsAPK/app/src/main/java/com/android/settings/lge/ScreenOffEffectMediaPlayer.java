package com.android.settings.lge;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.android.settings.Utils;
import com.android.settings.R;

public class ScreenOffEffectMediaPlayer extends Activity implements Callback,
        OnBufferingUpdateListener,
        OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener {
    private static final String TAG = "ScreenOffEffectMediaPlayer";
    private static final int NO_EFFECT_NUM = 0;
    private static final int CIRCLE_EFFECT_NUM = 1;
    private static final int ANDROID_EFFECT_NUM = 2;

    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mPreview;
    private boolean mIsVideoSizeKnown;
    private boolean mIsVideoReadyToBePlayed;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mAni;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.screeen_off_effect_media_player);
        mPreview = (SurfaceView)findViewById(R.id.surface);
        mSurfaceHolder = mPreview.getHolder();
        mSurfaceHolder.addCallback(this);

        mIntent = new Intent();
        mIntent = getIntent();
        mAni = mIntent.getIntExtra("animation", ANDROID_EFFECT_NUM);
        Log.d(TAG, "mAni : " + mAni);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        onCompletion(mMediaPlayer);
    }

    public void playVideo(int media) {
        Uri video;
        doCleanUp();
        try {
            switch (media) {
            case NO_EFFECT_NUM:
                Log.d(TAG, "Play No effect");
                video = Uri.parse("android.resource://" + getPackageName() + "/raw/no_effect");
                break;
            case ANDROID_EFFECT_NUM:
                Log.d(TAG, "Play Retro");
                video = Uri.parse("android.resource://" + getPackageName() + "/raw/retro");
                break;
            case CIRCLE_EFFECT_NUM:
                Log.d(TAG, "Play Circle");
                video = Uri.parse("android.resource://" + getPackageName() + "/raw/circle");
                break;
            default:
                Log.d(TAG, "Play default Retro");
                video = Uri.parse("android.resource://" + getPackageName() + "/raw/retro");
                break;
            }
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(getApplicationContext(), video);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            Log.d(TAG, "Exception!!! : " + e + "\n" + e.getStackTrace());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        playVideo(mAni);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setResult(RESULT_OK, mIntent);
        finish();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer arg0, int arg1) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared");
        mIsVideoReadyToBePlayed = true;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayBack();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.d(TAG, "onVideoSizeChanged");
        if (width == 0 || height == 0) {
            Log.d(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayBack();
        }
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayBack() {
        Log.d(TAG, "startVideoPlayBack");
        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onCompletion(mMediaPlayer);
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onCompletion(mMediaPlayer);
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onCompletion(mMediaPlayer);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
