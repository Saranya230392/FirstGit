/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.soundprofile;

import com.android.internal.telephony.TelephonyIntents;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import android.preference.SeekBarDialogPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.os.Vibrator;

import android.app.Activity;
import android.app.ActivityManager;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.soundprofile.VolumePreferenceEx;
import com.android.settings.soundprofile.VolumePreference.VolumeStore;

/**
 * Special preference type that allows configuration of both the ring volume and
 * notification volume.
 */
public class RingerVolumePreferenceEx extends SeekBarDialogPreference
        implements View.OnKeyListener {
    private static final String TAG = "RingerVolumePreferenceEx";

    private static final int STREAM_TYPE_RING = 0;
    private static final int STREAM_TYPE_NOTIFICATION = 1;
    private static final int STREAM_TYPE_SYSTEM = 2;
    private static final int STREAM_TYPE_MEDIA = 3;
    private static final int SOUNDPROFILE_SOUND_VIBRATE = 3;
    //private static final int STREAM_TYPE_ALARM = 4;
    private static final int ADD_VOLUME = 1;
    private static final int MINUS_VOLUME = -1;
    //private boolean mIsInit = true;
    private Vibrator mVibrator;
    private int mCurrentSoundProfile;
    private int mOrgSoundProfile;

    private RingerSeekBarVolumizer[] mSeekBarVolumizer;

    //[S][2012.01.16][susin.park][common] Optimus 3.0 scenario - Volume popup
    // SEEKBAR_ID, SEEKBAR_TYPE, CHECKBOX_VIEW_ID, mSEEKBAR_MUTED_RES_ID, mSEEKBAR_UNMUTED_RES_ID
    // 1. Phone ringtone
    // 2. Notification ringtone
    // 3. System and feedback
    // 4. Music, Video, games and other media
    //[E][2012.01.16][susin.park][common] Optimus 3.0 scenario - Volume popup
    // These arrays must all match in length and order
    private static final int[] SEEKBAR_ID = new int[] {
            R.id.ringer_volume_seekbar,
            R.id.notification_volume_seekbar,
            R.id.system_volume_seekbar,
            R.id.media_volume_seekbar,
            //R.id.alarm_volume_seekbar
    };

    private static final int[] SEEKBAR_TYPE = new int[] {
            AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_MUSIC,
            //AudioManager.STREAM_ALARM
    };

    private static final int[] CHECKBOX_VIEW_ID = new int[] {
            R.id.ringer_mute_button,
            R.id.notification_mute_button,
            R.id.system_mute_button,
            R.id.media_mute_button,
            //R.id.alarm_mute_button
    };

    private int[] mSEEKBAR_MUTED_RES_ID = new int[] {
            R.drawable.ic_audio_ring_notif_mute,
            R.drawable.ic_audio_notification_mute,
            R.drawable.ic_touch_feedback_off,
            R.drawable.ic_audio_media_mute,
            //R.drawable.ic_audio_alarm
    };

    private int[] mSEEKBAR_UNMUTED_RES_ID = new int[] {
            R.drawable.ic_audio_ring_notif,
            R.drawable.ic_audio_notification,
            R.drawable.ic_touch_feedback_on,
            R.drawable.ic_audio_media,
            //R.drawable.ic_audio_alarm
    };

    private void setRingMuteIconRes() {
        if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
            if (0 == mSeekBars[STREAM_TYPE_MEDIA].getProgress()) {
            }
        } else {
            if (0 == mSeekBars[STREAM_TYPE_RING].getProgress() && 
                mCurrentSoundProfile == AudioManager.RINGER_MODE_VIBRATE) {
                mSEEKBAR_MUTED_RES_ID[STREAM_TYPE_RING] = R.drawable.ic_audio_ring_notif_vibrate;
            }
        }
    }

    private ImageView[] mCheckBoxes = new ImageView[mSEEKBAR_MUTED_RES_ID.length];
    private SeekBar[] mSeekBars = new SeekBar[SEEKBAR_ID.length];

    /*
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            updateSlidersAndMutedStates();
        }
    };
    */

    @Override
    public void createActionButtons() {
        setPositiveButtonText(R.string.dlg_ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    private void updateSlidersAndMutedStates() {
        Log.i("soosin", "[updateSlidersAndMutedStates] init");
        //boolean muted = mSeekBars[STREAM_TYPE_RING].getProgress() == 0 ? true : false;
        boolean muted = mCurrentSoundProfile != AudioManager.RINGER_MODE_NORMAL ? true : false;

        for (int i = 0; i < SEEKBAR_TYPE.length; i++) {
            int streamType = SEEKBAR_TYPE[i];

            if (streamType == AudioManager.STREAM_MUSIC) {
                muted = false;
            }

            if (mCheckBoxes[i] != null) {

                if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
                    if (streamType == AudioManager.STREAM_NOTIFICATION &&
                            mAudioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
                        int res = Utils.isbuildProduct("p2")
                                ?
                                R.drawable.ic_audio_ring_notif_vibrate
                                :
                                R.drawable.ic_audio_ring_notif_vibrate_disabled;
                        mCheckBoxes[i].setImageResource(res);
                    } else {
                        mCheckBoxes[i].setImageResource(
                                muted ? mSEEKBAR_MUTED_RES_ID[i] : mSEEKBAR_UNMUTED_RES_ID[i]);
                    }
                } else {
                    if (streamType == AudioManager.STREAM_RING && muted
                            && mAudioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
                        int res = Utils.isbuildProduct("p2")
                                ?
                                R.drawable.ic_audio_ring_notif_vibrate
                                :
                                R.drawable.ic_audio_ring_notif_vibrate_disabled;
                        mCheckBoxes[i].setImageResource(res);
                        if (muted) {
                            mSeekBars[i].setProgress(0);
                        }
                    } else {
                        mCheckBoxes[i].setImageResource(
                                muted ? mSEEKBAR_MUTED_RES_ID[i] : mSEEKBAR_UNMUTED_RES_ID[i]);
                        if (muted) {
                            mSeekBars[i].setProgress(0);
                            mSeekBars[i].setProgress(0);
                        }
                    }
                }
            }
            if (null != mSeekBars[i]) {
                if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
                    if (i != STREAM_TYPE_NOTIFICATION) {
                        mSeekBars[i].setEnabled(muted ? false : true);
                    }
                } else {
                    if (i != STREAM_TYPE_RING) {
                        mSeekBars[i].setEnabled(muted ? false : true);
                    }
                }
            }
        }
    }

    private BroadcastReceiver mExternalStatusChangedReceiver;
    private AudioManager mAudioManager;
    private SoundProfileInfo mSoundProfileInfo;

    public RingerVolumePreferenceEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_ringervolume);
        mVibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        setItemImage();
        mSeekBarVolumizer = new RingerSeekBarVolumizer[SEEKBAR_ID.length];
        if (do_checkFromSoundProfile()) {
            mSoundProfileInfo = new SoundProfileInfo(getContext());
        }
    }

    //[S][2012.03.27][susin.park][P2] p2 only image changed
    private final void setItemImage() {
        if (Utils.isbuildProduct("p2")) {
            mSEEKBAR_MUTED_RES_ID[0] = R.drawable.ic_p2_audio_ring_notif_mute;
            mSEEKBAR_MUTED_RES_ID[1] = R.drawable.ic_p2_audio_notification_mute;
            mSEEKBAR_MUTED_RES_ID[2] = R.drawable.ic_p2_touch_feedback_off;
            mSEEKBAR_MUTED_RES_ID[3] = R.drawable.ic_p2_audio_vol;
            mSEEKBAR_MUTED_RES_ID[4] = R.drawable.ic_p2_audio_alarm;

            mSEEKBAR_UNMUTED_RES_ID[0] = R.drawable.ic_p2_audio_ring_notif;
            mSEEKBAR_UNMUTED_RES_ID[1] = R.drawable.ic_p2_audio_notification;
            mSEEKBAR_UNMUTED_RES_ID[2] = R.drawable.ic_p2_touch_feedback_on;
            mSEEKBAR_UNMUTED_RES_ID[3] = R.drawable.ic_p2_audio_vol;
            mSEEKBAR_UNMUTED_RES_ID[4] = R.drawable.ic_p2_audio_alarm;
        }
    }

    //[S][2012.03.27][susin.park][P2] p2 only image changed

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
            final LinearLayout layout_ringtone =
                    (LinearLayout)view.findViewById(R.id.ringer_section);
            layout_ringtone.setVisibility(View.GONE);
        }

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar)view.findViewById(SEEKBAR_ID[i]);
            mSeekBars[i] = seekBar;
            {
                mSeekBarVolumizer[i] = new RingerSeekBarVolumizer(getContext(), seekBar,
                        SEEKBAR_TYPE[i], getOrgVolume(i));
            }
        }

        for (int i = 0; i < mCheckBoxes.length; i++) {
            mCheckBoxes[i] = (ImageView)view.findViewById(CHECKBOX_VIEW_ID[i]);
        }

        mCurrentSoundProfile = Integer.parseInt(
                mSoundProfileInfo.getSoundProfileEachData(SoundProfileInfo.INDEX_SOUNDPROFILE));
        mOrgSoundProfile = mCurrentSoundProfile;
        if (mCurrentSoundProfile == SOUNDPROFILE_SOUND_VIBRATE) {
            mCurrentSoundProfile = AudioManager.RINGER_MODE_NORMAL;
        }
        do_showProfileVolume();
        Log.i("soosin", "mCurrentSoundProfile :" + mCurrentSoundProfile);

        if (mCurrentSoundProfile == AudioManager.RINGER_MODE_VIBRATE) {
            Log.i("soosin", "vib icon set");
            mSEEKBAR_MUTED_RES_ID[STREAM_TYPE_RING] = R.drawable.ic_audio_ring_notif_vibrate;
        }
        else if (mCurrentSoundProfile == AudioManager.RINGER_MODE_SILENT) {
            Log.i("soosin", "Silent icon set");
            mSEEKBAR_MUTED_RES_ID[STREAM_TYPE_RING] = R.drawable.ic_audio_ring_notif_mute;
        }
        updateSlidersAndMutedStates();

        if (mExternalStatusChangedReceiver == null) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addDataScheme("file");
            mExternalStatusChangedReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                        Log.i(TAG, "ACTION_MEDIA_MOUNTED");
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }

                    }
                    else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                        Log.i(TAG, "ACTION_MEDIA_UNMOUNTED");
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                    else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                        Log.i(TAG, "ACTION_MEDIA_EJECT");
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                }
            };
            getContext().registerReceiver(mExternalStatusChangedReceiver, filter);
        }

        View hidealarm = view.findViewById(R.id.alarm_volume);
        View hidealarmtext = view.findViewById(R.id.alarm_textview);
        hidealarm.setVisibility(View.GONE);
        hidealarmtext.setVisibility(View.GONE);
        //setisInit(true)
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    /* not used method
    private boolean getisInit() {
        return mIsInit;
    }

    private void setisInit(boolean value) {
        mIsInit = value;
    }
*/
    private void vibrateOn() {
        mVibrator.vibrate(200);
    }

    /* not used
    private Uri getMediaVolumeUri(Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getPackageName()
                + "/" + R.raw.media_volume);
    }
    */

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Log.d(TAG, "onDialogClosed - " + positiveResult);

        if (positiveResult) {
            do_setProfileVolume();
            getContext().sendBroadcast(new Intent("com.lge.update_soundprofile"));
        }

        for (RingerSeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null) {
                vol.stopSample();
                vol.revertVolume();
            }
        }
        cleanup();
    }

    protected void onSampleStarting(SeekBarVolumizer volumizer) {
        for (RingerSeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null && vol != volumizer) {
                vol.stopSample();
            }
        }
    }

    private void cleanup() {
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            if (mSeekBarVolumizer[i] != null) {
                mSeekBarVolumizer[i].stop();
                mSeekBarVolumizer[i] = null;
            }
        }
        if (mExternalStatusChangedReceiver != null) {
            getContext().unregisterReceiver(mExternalStatusChangedReceiver);
            mExternalStatusChangedReceiver = null;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        VolumeStore[] volumeStore = myState.getVolumeStore(SEEKBAR_ID.length);
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            RingerSeekBarVolumizer vol = mSeekBarVolumizer[i];
            if (vol != null) {
                vol.onSaveInstanceState(volumeStore[i]);
            }
        }
        return myState;
    }

    @Override
    protected void onClick() {
        // TODO Auto-generated method stub
        if (null == getDialog()) {
            super.onClick();
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        VolumeStore[] volumeStore = myState.getVolumeStore(SEEKBAR_ID.length);
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            RingerSeekBarVolumizer vol = mSeekBarVolumizer[i];
            if (vol != null) {
                vol.onRestoreInstanceState(volumeStore[i]);
            }
        }
    }

    private static class SavedState extends BaseSavedState {
        VolumeStore[] mVolumeStore;

        public SavedState(Parcel source) {
            super(source);
            mVolumeStore = new VolumeStore[SEEKBAR_ID.length];
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                mVolumeStore[i] = new VolumeStore();
                mVolumeStore[i].volume = source.readInt();
                mVolumeStore[i].originalVolume = source.readInt();
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                dest.writeInt(mVolumeStore[i].volume);
                dest.writeInt(mVolumeStore[i].originalVolume);
            }
        }

        VolumeStore[] getVolumeStore(int count) {
            if (mVolumeStore == null || mVolumeStore.length != count) {
                mVolumeStore = new VolumeStore[count];
                for (int i = 0; i < count; i++) {
                    mVolumeStore[i] = new VolumeStore();
                }
            }
            return mVolumeStore;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        /* not used
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
         */
    }

    //[S][Settings][TD] sample ringtone stop when incomming calling
    public void RingStop() {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.isShowing())
        {
            for (RingerSeekBarVolumizer vol : mSeekBarVolumizer) {
                if (vol != null) {
                    vol.stopSample();
                }
            }
        }
    }

    //[E][Settings][TD] sample ringtone stop when incomming calling

    public class RingerSeekBarVolumizer extends SeekBarVolumizer {
        public RingerSeekBarVolumizer(Context context, SeekBar seekBar, int streamType,
                int orgVolume) {
            super(context, seekBar, streamType, orgVolume);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            super.onStartTrackingTouch(seekBar);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            super.onStopTrackingTouch(seekBar);
            if (seekBar.getId() == R.id.media_volume_seekbar) {
            }
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

            if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
                if (seekBar.getId() == R.id.media_volume_seekbar && 0 == seekBar.getProgress()) {
                    RingStop();
                }

            } else {
                if (seekBar.getId() == R.id.ringer_volume_seekbar && 0 == seekBar.getProgress()) {
                    Log.i("soosin", "onProgressChanged init");
                    if (true == fromTouch) {
                        vibrateOn();
                        mCurrentSoundProfile = AudioManager.RINGER_MODE_VIBRATE;
                        mOrgSoundProfile = mCurrentSoundProfile;
                    }
                    setRingMuteIconRes();

                    mSeekBars[STREAM_TYPE_NOTIFICATION].setProgress(0);
                    mSeekBars[STREAM_TYPE_SYSTEM].setProgress(0);
                }
                else if (seekBar.getId() == R.id.ringer_volume_seekbar
                        && 0 != seekBar.getProgress()) {
                    mCurrentSoundProfile = AudioManager.RINGER_MODE_NORMAL;
                    mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION].getSeekBar().setProgress(
                            mLastProgress);

                    mSeekBars[STREAM_TYPE_NOTIFICATION]
                            .setProgress(mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION]
                            .mLastProgress);

                    mSeekBars[STREAM_TYPE_SYSTEM]
                            .setProgress(mSeekBarVolumizer[STREAM_TYPE_SYSTEM]
                            .mLastProgress);
                }
            }
            updateSlidersAndMutedStates();
            super.onProgressChanged(seekBar, progress, fromTouch);
        }
    }

    private int getOrgVolume(int StreamType) {
        //int volume = 0;
        String volume = "0";

        switch (StreamType) {
        case STREAM_TYPE_RING:
            volume = mSoundProfileInfo.getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_RING);
            break;
        case STREAM_TYPE_NOTIFICATION:
            volume = mSoundProfileInfo
                    .getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_NOTIFICATION);
            break;

        case STREAM_TYPE_SYSTEM:
            volume = mSoundProfileInfo
                    .getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_SYSTEM);
            break;

        case STREAM_TYPE_MEDIA:
            volume = mSoundProfileInfo.getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_MUSIC);
            break;
        default:
            break;
        }
        return Integer.parseInt(volume);
    }

    private void do_showProfileVolume() {
        String volume_ring = mSoundProfileInfo
                .getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_RING);
        String volume_notification = mSoundProfileInfo
                .getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_NOTIFICATION);
        String volume_system = mSoundProfileInfo
                .getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_SYSTEM);
        String volume_music = mSoundProfileInfo
                .getSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_MUSIC);
        if (null != mSeekBars) {
            Log.d(TAG, "getEachData ring : " + volume_ring);
            Log.d(TAG, "getEachData nifi : " + volume_notification);
            Log.d(TAG, "getEachData syst : " + volume_system);
            Log.d(TAG, "getEachData musi : " + volume_music);

            //mSeekBarVolumizer[STREAM_TYPE_RING].mLastProgress = Integer.parseInt(volume_ring);
            //mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION].mLastProgress = Integer.parseInt(volume_notification);
            //mSeekBarVolumizer[STREAM_TYPE_SYSTEM].mLastProgress = Integer.parseInt(volume_system);

            if (mCurrentSoundProfile != AudioManager.RINGER_MODE_NORMAL &&
                mCurrentSoundProfile == 3) {
                mSeekBars[STREAM_TYPE_RING].setProgress(0);
                mSeekBars[STREAM_TYPE_NOTIFICATION].setProgress(0);
                mSeekBars[STREAM_TYPE_SYSTEM].setProgress(0);
            }
            else {
                mSeekBars[STREAM_TYPE_RING]
                        .setProgress(Integer.parseInt(volume_ring));
                mSeekBars[STREAM_TYPE_NOTIFICATION]
                        .setProgress(Integer.parseInt(volume_notification));
                mSeekBars[STREAM_TYPE_SYSTEM].setProgress(Integer.parseInt(volume_system));
            }
            mSeekBars[3].setProgress(Integer.parseInt(volume_music));
        }

    }

    private void do_setProfileVolume() {
        if (null != mSeekBars[0]) {
            if (mCurrentSoundProfile == AudioManager.RINGER_MODE_NORMAL) {
                Log.d(TAG,
                        "close Integer.toString(mSeekBars[0].getProgress())"
                                + Integer.toString(mSeekBars[0].getProgress()));
                Log.d(TAG,
                        "close Integer.toString(mSeekBars[1].getProgress())"
                                + Integer.toString(mSeekBars[1].getProgress()));
                Log.d(TAG,
                        "close Integer.toString(mSeekBars[2].getProgress())"
                                + Integer.toString(mSeekBars[2].getProgress()));

                mSoundProfileInfo.setSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_RING,
                        Integer.toString(mSeekBars[0].getProgress()));
                mSoundProfileInfo.setSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_NOTIFICATION,
                        Integer.toString(mSeekBars[1].getProgress()));
                mSoundProfileInfo.setSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_SYSTEM,
                        Integer.toString(mSeekBars[2].getProgress()));
            }
            Log.d(TAG,
                    "close Integer.toString(mSeekBars[3].getProgress())"
                            + Integer.toString(mSeekBars[3].getProgress()));
            mSoundProfileInfo.setSoundProfileEachData(SoundProfileInfo.INDEX_VOLUME_MUSIC,
                    Integer.toString(mSeekBars[3].getProgress()));

            if (0 == mSeekBars[STREAM_TYPE_RING].getProgress() 
                && mCurrentSoundProfile != AudioManager.RINGER_MODE_SILENT) {
                mSoundProfileInfo.setSoundProfileEachData(
                        SoundProfileInfo.INDEX_SOUNDPROFILE,
                        Long.toString(AudioManager.RINGER_MODE_VIBRATE));
                mSoundProfileInfo.setSoundProfileEachData(
                        SoundProfileInfo.INDEX_VOLUME_NOTIFICATION,
                        Integer.toString(mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION]
                                .mLastProgress));
                mSoundProfileInfo.setSoundProfileEachData(
                        SoundProfileInfo.INDEX_VOLUME_SYSTEM,
                        Integer.toString(mSeekBarVolumizer[STREAM_TYPE_SYSTEM].mLastProgress));
            } else if (0 < mSeekBars[STREAM_TYPE_RING].getProgress() ){
                if (mOrgSoundProfile != SOUNDPROFILE_SOUND_VIBRATE) {
                    mSoundProfileInfo.setSoundProfileEachData(
                            SoundProfileInfo.INDEX_SOUNDPROFILE,
                            Long.toString(AudioManager.RINGER_MODE_NORMAL));
                } else {
                    mSoundProfileInfo.setSoundProfileEachData(
                            SoundProfileInfo.INDEX_SOUNDPROFILE,
                            Long.toString(SOUNDPROFILE_SOUND_VIBRATE));
                }
            }
        }
    }

    public boolean do_checkFromSoundProfile() {
        if ("soundprofile.AddSoundProfiles".equals(((Activity)getContext()).getLocalClassName())) {
            Log.i(TAG, "init for AddSoundProfile");
            return true;
        }
        Log.i(TAG, "init for SoundSettings");
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
                if (true == isdown && 0 != mSeekBars[STREAM_TYPE_NOTIFICATION].getProgress()) {
                    if (null != mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION]) {
                        mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION].
                                changeVolumeBy(MINUS_VOLUME);
                    }
                }
            } else {
                if (true == isdown && 1 < mSeekBars[STREAM_TYPE_RING].getProgress()) {
                    if (null != mSeekBarVolumizer[STREAM_TYPE_RING]) {
                        mSeekBarVolumizer[STREAM_TYPE_RING].
                                changeVolumeBy(MINUS_VOLUME);
                    }
                }
                else if (true == isdown && 1 == mSeekBars[STREAM_TYPE_RING].getProgress()) {
                    mSeekBarVolumizer[STREAM_TYPE_RING].
                            changeVolumeBy(MINUS_VOLUME);
                    vibrateOn();
                    mCurrentSoundProfile = AudioManager.RINGER_MODE_VIBRATE;
                    mOrgSoundProfile = mCurrentSoundProfile;
                    setRingMuteIconRes();
                    mCheckBoxes[STREAM_TYPE_RING]
                            .setImageResource(mSEEKBAR_MUTED_RES_ID[STREAM_TYPE_RING]);
                }
            }
            return true;

        case KeyEvent.KEYCODE_VOLUME_UP:
            if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
                if (true == isdown
                        && mSeekBars[STREAM_TYPE_NOTIFICATION].getMax() != mSeekBars[STREAM_TYPE_NOTIFICATION]
                                .getProgress()) {
                    if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        if (null != mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION]) {
                            mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION].startSample();
                        }
                        return true;
                    }
                    else {
                        if (null != mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION]) {
                            mSeekBarVolumizer[STREAM_TYPE_NOTIFICATION].
                                    changeVolumeBy(ADD_VOLUME);
                        }
                    }
                }
            } else {
                if (true == isdown
                        && mSeekBars[STREAM_TYPE_RING].getMax() != mSeekBars[STREAM_TYPE_RING]
                                .getProgress()) {
                    if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        if (null != mSeekBarVolumizer[STREAM_TYPE_RING]) {
                            mSeekBarVolumizer[STREAM_TYPE_RING].startSample();
                        }
                        return true;
                    }
                    else {
                        if (null != mSeekBarVolumizer[STREAM_TYPE_RING]) {
                            mSeekBarVolumizer[STREAM_TYPE_RING].
                                    changeVolumeBy(ADD_VOLUME);
                        }
                    }
                }
            }
            break;
        default:
            return false;
        }
        return true;
    }

    /**
     * Turns a {@link SeekBar} into a volume control.
     */
    public class SeekBarVolumizer implements OnSeekBarChangeListener, Handler.Callback {

        protected Context mContext;
        protected Handler mHandler;

        protected AudioManager mAudioManager;
        protected int mStreamType;
        protected int mOriginalStreamVolume;
        protected Ringtone mRingtone;

        protected int mLastProgress = -1;
        protected SeekBar mSeekBar;
        protected int mVolumeBeforeMute = -1;

        private static final int MSG_SET_STREAM_VOLUME = 0;
        private static final int MSG_START_SAMPLE = 1;
        private static final int MSG_STOP_SAMPLE = 2;
        private static final int CHECK_RINGTONE_PLAYBACK_DELAY_MS = 1000;

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType, int orgVolume) {
            this(context, seekBar, streamType, null, orgVolume);
        }

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType, Uri defaultUri,
                int orgVolume) {
            mContext = context;
            mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            mStreamType = streamType;
            mSeekBar = seekBar;

            HandlerThread thread = new HandlerThread(TAG + ".CallbackHandler");
            thread.start();
            mHandler = new Handler(thread.getLooper(), this);

            initSeekBar(seekBar, defaultUri, orgVolume);
        }

        protected void initSeekBar(SeekBar seekBar, Uri defaultUri, int orgVolume) {
            seekBar.setMax(mAudioManager.getStreamMaxVolume(mStreamType));
            mOriginalStreamVolume = orgVolume;
            mLastProgress = mOriginalStreamVolume;

            Log.e("soosin", "[initSeekBar] ori : " + mOriginalStreamVolume + "  " +
                    "mLast : " + mLastProgress);
            seekBar.setProgress(mOriginalStreamVolume);
            seekBar.setOnSeekBarChangeListener(this);

            if (defaultUri == null) {
                if (mStreamType == AudioManager.STREAM_RING) {
                    defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
                } else if (mStreamType == AudioManager.STREAM_NOTIFICATION) {
                    defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                } else if (mStreamType == AudioManager.STREAM_MUSIC) {
                    //defaultUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    defaultUri = Uri.parse("/system/media/audio/ui/LG_Media_volume.ogg");
                }
                else {
                    defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
                }
            }

            Log.i("soosin", "[initSeekBar] URI : " + defaultUri.toSafeString());
            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);

            if (mStreamType == AudioManager.STREAM_RING) {
                mStreamType = AudioManager.STREAM_ALARM;
            }

            if (mRingtone != null) {
                mRingtone.setStreamType(mStreamType);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SET_STREAM_VOLUME:
                Log.i("soosin", "Set stream volume : " + mLastProgress);
                mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);
                mSeekBar.setProgress(mLastProgress);
                break;
            case MSG_START_SAMPLE:
                onStartSample();
                break;
            case MSG_STOP_SAMPLE:
                onStopSample();
                break;
            default:
                Log.e("soosin", "invalid SeekBarVolumizer message: " + msg.what);
            }
            return true;
        }

        protected void postStartSample() {
            mHandler.removeMessages(MSG_START_SAMPLE);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_SAMPLE),
                    isSamplePlaying() ? CHECK_RINGTONE_PLAYBACK_DELAY_MS : 0);
        }

        private void onStartSample() {
            if (!isSamplePlaying()) {
                onSampleStarting(this);
                if (mRingtone != null) {
                    Log.i("soosin", "[onStartSample] Ringtone play");
                    if (mStreamType == AudioManager.STREAM_SYSTEM) {
                        mAudioManager.playSoundEffect(SoundEffectConstants.CLICK, mLastProgress);
                    }
                    else {
                        mRingtone.play();
                    }
                }
            }
        }

        private void postStopSample() {
            // remove pending delayed start messages
            mHandler.removeMessages(MSG_START_SAMPLE);
            mHandler.removeMessages(MSG_STOP_SAMPLE);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_SAMPLE));
        }

        private void onStopSample() {
            if (mRingtone != null) {
                Log.i("soosin", "[onStopSample] Ringtone stop");
                mRingtone.stop();
            }
        }

        public void stop() {
            postStopSample();
            mSeekBar.setOnSeekBarChangeListener(null);
        }

        public void revertVolume() {
            mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 0);
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            Log.e("soosin", "[in class onProgressChanged] fromTouch : " + fromTouch +
                    " progress : " + progress +
                    " lastprogress : " + mLastProgress);
            if (!fromTouch) {
                return;
            }
            postSetVolume(progress);
        }

        protected void postSetVolume(int progress) {
            // Do the volume changing separately to give responsive UI
            mLastProgress = progress;
            mHandler.removeMessages(MSG_SET_STREAM_VOLUME);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_STREAM_VOLUME));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i("soosin", "[onStopTrackingTouch] init");
            postStartSample();
        }

        public boolean isSamplePlaying() {
            return mRingtone != null && mRingtone.isPlaying();
        }

        public void startSample() {
            postStartSample();
        }

        public void stopSample() {
            postStopSample();
        }

        public SeekBar getSeekBar() {
            return mSeekBar;
        }

        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);
            postStartSample();
            postSetVolume(mSeekBar.getProgress());
            mVolumeBeforeMute = -1;
        }

        public void muteVolume() {
            if (mVolumeBeforeMute != -1) {
                mSeekBar.setProgress(mVolumeBeforeMute);
                postSetVolume(mVolumeBeforeMute);
                postStartSample();
                mVolumeBeforeMute = -1;
            } else {
                mVolumeBeforeMute = mSeekBar.getProgress();
                mSeekBar.setProgress(0);
                postStopSample();
                postSetVolume(0);
            }
        }

        public void onSaveInstanceState(VolumeStore volumeStore) {
            if (mLastProgress >= 0) {
                volumeStore.volume = mLastProgress;
                volumeStore.originalVolume = mOriginalStreamVolume;
            }
        }

        public void onRestoreInstanceState(VolumeStore volumeStore) {
            if (volumeStore.volume != -1) {
                mOriginalStreamVolume = volumeStore.originalVolume;
                mLastProgress = volumeStore.volume;
                postSetVolume(mLastProgress);
            }
        }
    }
}
