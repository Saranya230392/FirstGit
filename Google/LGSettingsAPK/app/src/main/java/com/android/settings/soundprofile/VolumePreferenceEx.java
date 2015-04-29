package com.android.settings.soundprofile;

import android.content.Context;
import android.util.AttributeSet;
import android.net.Uri;
import android.provider.Settings;
import android.media.AudioManagerEx;
import android.widget.SeekBar;

import android.view.View;
import android.media.AudioManager;
import android.os.Handler;

import android.content.BroadcastReceiver;
import android.os.SystemProperties;
import android.content.IntentFilter;
import android.content.Intent;
import android.util.Log;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.provider.Settings.System;
import android.media.RingtoneManager;

/**
 * The android.preference.VolumePreference lge extension class.
 */
public class VolumePreferenceEx extends VolumePreference {

    // Audio_Framework: Sound profile
    protected int mOriginalRingerMode;
    // Audio_Framework_END
    // Audio_Framework: VolumePreference exception
    private boolean mIsActive = true;

    // Audio_Framework_END
    public VolumePreferenceEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onActivityStop() {
        super.onActivityStop();
        // Audio_Framework: VolumePreference exception
        mIsActive = false;
        // Audio_Framework_END
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        // Audio_Framework: Sound profile
        final SeekBar seekBar = getSeekBar(view);
        mSeekBarVolumizer = new SeekBarVolumizerEx(getContext(), seekBar, mStreamType);
        AudioManager audioManager = (AudioManager)getContext().getSystemService(
                Context.AUDIO_SERVICE);
        mOriginalRingerMode = audioManager.getRingerMode();
        // Audio_Framework_END
    }

    public class SeekBarVolumizerEx extends SeekBarVolumizer {
        public SeekBarVolumizerEx(Context context, SeekBar seekBar, int streamType) {
            super(context, seekBar, streamType, null);
        }

        public SeekBarVolumizerEx(Context context, SeekBar seekBar, int streamType, Uri defaultUri) {
            super(context, seekBar, streamType, defaultUri);
            // Audio_Framework: VolumePreference exception
            Log.d(TAG, "SeekBarVolumizerEx");
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            filter.addAction("lge.settings.intent.action.NOTI_RESUME");
            filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
            filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            mContext.registerReceiver(mVolumeReceiver, filter);
            // Audio_Framework_END
        }

        protected void initSeekBar(SeekBar seekBar, Uri defaultUri) {
            seekBar.setMax(mAudioManager.getStreamMaxVolume(mStreamType));
            mOriginalStreamVolume = mAudioManager.getLastAudibleStreamVolume(mStreamType);
            seekBar.setProgress(mOriginalStreamVolume);
            seekBar.setOnSeekBarChangeListener(this);

            Log.d(TAG, "initSeekBar registerContentObserver");
            mContext.getContentResolver().registerContentObserver(
                    System.getUriFor(System.VOLUME_SETTINGS[mStreamType]),
                    false, mVolumeObserver);

            if (defaultUri == null) {
                if (mStreamType == AudioManager.STREAM_RING) {
                    defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
                } else if (mStreamType == AudioManager.STREAM_NOTIFICATION) {
                    defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                    // Audio_Framework: System stream feature
                } else if (mStreamType == AudioManager.STREAM_SYSTEM) {
                    defaultUri = Uri.parse("file:///system/media/audio/ui/Effect_Tick.ogg");
                    // Audio_Framework_END
                    // Audio_Framework: VolumePreference exception
                } else if (mStreamType == AudioManager.STREAM_MUSIC) {
                    /* TODO : LG_Media_volume is the same as DEFAULT_RINGTONE_URI but tunning is necessary */
                    defaultUri = Uri.parse("file:///system/media/audio/ui/LG_Media_volume.ogg");
                    // Audio_Framework_END
                } else {
                    defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                }
            }
            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);

            if (mRingtone != null) {
                mRingtone.setStreamType(mStreamType);
            }
            // Audio_Framework_END
        }

        // Audio_Framework: VolumePreference exception
        // Add receiver because volume observer is too slow.
        private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (mIsActive && (action.equals(Intent.ACTION_HEADSET_PLUG))) {
                    int headsetState = intent.getIntExtra("state", 0);
                    if (headsetState == 1) {
                        try {
                            Thread.currentThread().sleep(100);
                        }
                        catch (InterruptedException e) {
                        	Log.d(TAG, "InterruptedException");
                        }
                    }
                    int newOriginalvolume = mAudioManager.getStreamVolume(mStreamType);
                    if (mSeekBar != null
                            && (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL ||
                            mStreamType == AudioManager.STREAM_MUSIC)) {
                        Log.d(TAG, "ACTION_HEADSET_PLUG mOriginalStreamVolume = " +
                                mOriginalStreamVolume + ", newOriginalvolume = "
                                + newOriginalvolume);
                        mSeekBar.setProgress(newOriginalvolume);
                        mOriginalStreamVolume = newOriginalvolume; //AUDIO_FWK : update mOriginalStreamVolume to new device volume. (heechul.hyun@lge.com)
                    }
                }

                if (mIsActive && action.equals(AudioManager.VOLUME_CHANGED_ACTION)) {
                    int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE,
                            AudioManager.STREAM_RING);
                    if (mSeekBar != null && mStreamType == streamType) {
                        int newOriginalvolume = mAudioManager.getStreamVolume(mStreamType);
                        Log.d(TAG, "VOLUME_CHANGED_ACTION mOriginalStreamVolume = " +
                                mOriginalStreamVolume + ", newOriginalvolume = "
                                + newOriginalvolume);
                        mSeekBar.setProgress(newOriginalvolume);
                    }
                }

                if (mIsActive && action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        stopSample();
                    }
                }

                // Activate statusFlag for resume.
                if ((mIsActive == false)
                        && (action.equals("lge.settings.intent.action.NOTI_RESUME"))) {
                    Log.d(TAG, "VolumeReceiver!!! onReceive..... NOTI_RESUME");
                    mIsActive = true;
                }
                // maintain BGM when changed to Vibrate Mode
                if (action.equals("android.media.RINGER_MODE_CHANGED")) {
                    if (intent.getIntExtra("android.media.EXTRA_RINGER_MODE", 2) != AudioManager.RINGER_MODE_NORMAL) {
                        if (isSamplePlaying()) {
                            stopSample();
                        }
                    }
                }
            }
        };

        // Audio_Framework_END
        public void revertVolume() {
            // Audio_Framework: Sound profile
            //mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 0);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //Revert RingerMode
                    if (mStreamType == AudioManager.STREAM_RING) {
                        //mAudioManager.setRingerMode(mOriginalRingerMode);
                    }

                    //Revert StreamVolume
                    //mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume,
                    //AudioManagerEx.FLAG_KEEP_RINGER_MODES);
                }
            }, 100);
            // Audio_Framework_END
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            // Audio_Framework: VolumePreference exception
            if (!isSamplePlaying()) {
                if (mLastProgress >= 0) {
                    mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);
                }
                if (mLastProgress != 0) {
                    startSample();
                }
            } else if (isSamplePlaying() && mLastProgress == 0) {
                stopSample();
            }
            if (mVolumeReceiver != null) {
                mContext.unregisterReceiver(mVolumeReceiver);
            }
            // Audio_Framework_END
        }

        public void run() {
            mIsActive = true; // CAPP_AUDIO : VolumePreference exception
            //mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);
        }

        public void startSample() {
            onSampleStarting(this);
            if (mRingtone != null) {
                // Audio_Framework: VolumePreference exception
                if (mAudioManager.isMusicActive() && mStreamType == AudioManager.STREAM_MUSIC) {
                    stopSample();
                    return;
                }
                // Audio_Framework_END
                postStartSample();
            }
        }

        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);
            if (!isSamplePlaying()) {
                // Audio_Framework: VolumePreference exception
                //mAudioManager.setStreamVolume(mStreamType, mSeekBar.getProgress(), 0);
                // Audio_Framework_END
                startSample();
            }
            postSetVolume(mSeekBar.getProgress());
            mVolumeBeforeMute = -1;
        }
    }
}
