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

package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
//import android.preference.SeekBarPreference;
import android.provider.Settings;
//import android.provider.Settings.System;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.preference.SeekBarDialogPreference;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.view.View.OnClickListener;
import android.content.ContentResolver;
import android.util.Log;
import android.media.AudioManager;
import com.android.settings.Utils;
// import com.lge.provider.SettingsEx; KLP
import com.lge.constants.SettingsConstants;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.VolumeVibratorManager;

import com.android.settings.soundprofile.SoundProfileInfo;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import java.util.List;
import android.content.ComponentName;
import android.content.ContentResolver;

/**
 * Special preference type that allows configuration of both the ring volume and
 * notification volume.
 */
/** @hide */
public class VibrateVolumePreference extends SeekBarDialogPreference implements
        PreferenceManager.OnActivityStopListener, View.OnKeyListener {

    private static final String TAG = "VibrateVolumePreference";

    private SoundProfileInfo mSoundProfileInfo;
    private SeekBarVolumizer mSeekBarVolumizer_call;
    private SeekBarVolumizer mSeekBarVolumizer_notification;
    private SeekBarVolumizer mSeekBarVolumizer_touch;
    private SeekBarVolumizer mMaster_SeekBarVolumizer;
    private boolean onbindDialog = false;
    private Context mContext;

    //int current_vibrateMode = 0;
    //ImageView checkbox;

    //private AudioManager mAudioManager;

    public VibrateVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setDialogLayoutResource(R.layout.preference_dialog_vibratevolumes);
        //TypedArray a = context.obtainStyledAttributes(attrs,
        //com.android.internal.R.styleable.VolumePreference, 0, 0);

        //mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (do_checkFromSoundProfile()) {
            mSoundProfileInfo = new SoundProfileInfo(getContext());
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        final LinearLayout layout_call = (LinearLayout)view.findViewById(R.id.vibrate_call_layout);
        final LinearLayout layout_notification = (LinearLayout)view
                .findViewById(R.id.vibrate_notification_layout);
        final LinearLayout layout_touch = (LinearLayout)view
                .findViewById(R.id.vibrate_touch_layout);

        final SeekBar seekBar_call = (SeekBar)view.findViewById(R.id.vibrate_call_seekbar);
        final SeekBar seekBar_notification = (SeekBar)view
                .findViewById(R.id.vibrate_notification_seekbar);
        final SeekBar seekBar_touch = (SeekBar)view.findViewById(R.id.vibrate_touch_seekbar);

        final TextView textView_call = (TextView)view.findViewById(R.id.vibrate_volume_description);
        final TextView textView_touch = (TextView)view.findViewById(R.id.vibrate_touch_description);
        //checkbox = (ImageView) view.findViewById(R.id.vibrate_mute_button);

        //current_vibrateMode = mAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        //    Log.d(TAG, "##### current_vibrateMode = " + current_vibrateMode);

        //if (current_vibrateMode == AudioManager.VIBRATE_SETTING_ON) {
        //checkbox.setImageResource(com.android.internal.R.drawable.ic_audio_ring_notif_mute);
        //}
        //else{
        //checkbox.setImageResource(com.android.internal.R.drawable.ic_audio_vol);
        //}

        //checkbox.setOnClickListener(this);

        mSeekBarVolumizer_call = new SeekBarVolumizer(getContext(), seekBar_call,
                VolumeVibratorManager.VIBRATE_TYPE_RING);
        mSeekBarVolumizer_notification = new SeekBarVolumizer(getContext(), seekBar_notification,
                VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION);
        mSeekBarVolumizer_touch = new SeekBarVolumizer(getContext(), seekBar_touch,
                VolumeVibratorManager.VIBRATE_TYPE_HAPTIC);

        if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
            mMaster_SeekBarVolumizer = mSeekBarVolumizer_notification;
        } else {
            mMaster_SeekBarVolumizer = mSeekBarVolumizer_call;
        }

        //getPreferenceManager().registerOnActivityStopListener(this);

        // grab focus and key events so that pressing the volume buttons in the
        // dialog doesn't also show the normal volume adjust toast.

        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        if (Utils.isUpgradeModel()) {
            layout_notification.setVisibility(View.GONE);
            layout_touch.setVisibility(View.GONE);
            textView_call.setVisibility(View.GONE);
        }

        if (Utils.getVibrateTypeProperty().equals("2")) {
            layout_touch.setVisibility(View.GONE);
        }

        if (Utils.supportSplitView(mContext)) {
            if (!Utils.is070Model(mContext)) {
                if (layout_call != null) {
                    layout_call.setVisibility(View.GONE);
                    textView_call.setVisibility(View.GONE);
                }
            }
        }
        if (Utils.isUI_4_1_model(mContext)) {
            if (layout_touch != null && textView_touch != null) {
                textView_touch.setText(R.string.haptic_feedback_enable_title_tap);
            }
        }
        Log.d(TAG, "onBindDialog");
        onbindDialog = true;
    }

    @Override
    public void createActionButtons() {
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (true == onbindDialog) {
            // If key arrives immediately after the activity has been cleaned up.
            Log.d(TAG, "keyCode : " + keyCode + " event :" + event.getAction());
            if (mSeekBarVolumizer_call == null) {
                return super.onKey(v, keyCode, event);
            }
            boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
            switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (isdown) {
                    mMaster_SeekBarVolumizer.changeVolumeBy(-1);
                    Log.d(TAG, "volume down1");
                }
                Log.d(TAG, "volume down2");
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (isdown) {
                    mMaster_SeekBarVolumizer.changeVolumeBy(1);
                    Log.d(TAG, "volume up1");
                }
                Log.d(TAG, "volume up2");
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (isdown) {
                    mMaster_SeekBarVolumizer.changeVolumeBy(-1);
                    Log.d(TAG, "left");
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (isdown) {
                    mMaster_SeekBarVolumizer.changeVolumeBy(1);
                    Log.d(TAG, "right");
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (isdown) {
                    Log.d(TAG, "center");
                    return true;
                }
            case KeyEvent.KEYCODE_ENTER:
                Log.d(TAG, "ENTER");
                return true;
            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "BACK");
                return false;
            default:
                return false;

            }
        }
        Log.d(TAG, "onKey");
        return super.onKey(v, keyCode, event);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (do_checkFromSoundProfile() == true) {
            if (positiveResult) {
                Log.i("soosin", "onDialongClosed - profile mode");
                do_setProfileVibrateVolume();
                return;
            }
        }

        if (!positiveResult && mSeekBarVolumizer_call != null) {
            mSeekBarVolumizer_call.revertVolume();
            mSeekBarVolumizer_notification.revertVolume();
            mSeekBarVolumizer_touch.revertVolume();

        }
        onbindDialog = false;
        cleanup();
    }

    public void onActivityStop() {
        if (mSeekBarVolumizer_call != null) {
            mSeekBarVolumizer_call.stopSample();
            mSeekBarVolumizer_call.revertVolume();
        }
        cleanup(); //TODO : Check the Stern Porting

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void cleanup() {
        //getPreferenceManager().unregisterOnActivityStopListener(this);

        if (mSeekBarVolumizer_call != null) {
            Dialog dialog = getDialog();
            if (dialog != null && dialog.isShowing()) {
                View view = dialog.getWindow().getDecorView()
                        .findViewById(R.id.vibrate_call_layout);

                if (view != null) {
                    view.setOnKeyListener(null);
                }
                mSeekBarVolumizer_call.revertVolume();
            }
            mSeekBarVolumizer_call.stop();
            mSeekBarVolumizer_call = null;
        }

    }

    protected void onSampleStarting(SeekBarVolumizer volumizer) {
        if (mSeekBarVolumizer_call != null && volumizer != mSeekBarVolumizer_call) {
            mSeekBarVolumizer_call.stopSample();
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
        if (mSeekBarVolumizer_call != null) {
            mSeekBarVolumizer_call.onSaveInstanceState(myState.getVolumeStore());
        }
        return myState;
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
        if (mSeekBarVolumizer_call != null) {
            mSeekBarVolumizer_call.onRestoreInstanceState(myState.getVolumeStore());
        }
    }

    /*
    public void onClick(View v) {
        // Touching any of the mute buttons causes us to get the state from the system and toggle it

        int vibrateMode = (mAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER) == AudioManager.VIBRATE_SETTING_ON) ? AudioManager.VIBRATE_SETTING_ON : AudioManager.VIBRATE_SETTING_ONLY_SILENT;

        if (current_vibrateMode == AudioManager.VIBRATE_SETTING_ON) {
            //mAudioManager.setRingerMode(AudioManager.VIBRATE_SETTING_OFF);
            //Settings.System.putInt(mContext.getContentResolver(), SettingsConstants.System.VIBRATE_IN_SILENT, 0);
            //current_vibrateMode = 0;
            mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,vibrateMode);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, vibrateMode);

            Log.d(TAG, "onClick @@@ic_audio_vol");

            checkbox.setImageResource(com.android.internal.R.drawable.ic_audio_vol);
        }
        else{
            //mAudioManager.setRingerMode(AudioManager.VIBRATE_SETTING_ON);
            mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,vibrateMode);
                mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, vibrateMode);
            Log.d(TAG, "onClick !!!!audio_ring_notif_mute");
            checkbox.setImageResource(com.android.internal.R.drawable.ic_audio_ring_notif_mute);
        }

    }
    */

    /** @hide */
    public static class VolumeStore {
        public int volume = -1;
        public int originalVolume = -1;
    }

    private static class SavedState extends BaseSavedState {
        VolumeStore mVolumeStore = new VolumeStore();

        public SavedState(Parcel source) {
            super(source);
            mVolumeStore.volume = source.readInt();
            mVolumeStore.originalVolume = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mVolumeStore.volume);
            dest.writeInt(mVolumeStore.originalVolume);
        }

        VolumeStore getVolumeStore() {
            return mVolumeStore;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    /** @hide */
    public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {

        private Context mContext;
        private Handler mHandler = new Handler();
        private LGContext mServiceContext;
        private VolumeVibratorManager mVolumeVibrator;
        private int mOriginalVibrateVolume;
        private int mLastProgress = -1;
        private SeekBar mSeekBar;
        private int mVibrateType = -1;
        private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mSeekBar != null) {
                    int volume = mVolumeVibrator.getVibrateVolume(mVibrateType);
                    // Works around an atomicity problem with volume updates
                    // TODO: Fix the actual issue, probably in AudioService
                    if (volume >= 0) {
                        mSeekBar.setProgress(volume);
                    }
                }
            }
        };

        public SeekBarVolumizer(Context context, SeekBar seekBar) {
            this(context, seekBar, 0);
        }

        public SeekBarVolumizer(Context context, SeekBar seekBar, int type) {
            mContext = context;
            mServiceContext = new LGContext(mContext.getApplicationContext());
            mVolumeVibrator = (VolumeVibratorManager)mServiceContext
                    .getLGSystemService(LGContext.VOLUMEVIBRATOR_SERVICE);
            mSeekBar = seekBar;
            mVibrateType = type;
            initSeekBar(seekBar);
        }

        private void initSeekBar(SeekBar seekBar) {
            seekBar.setMax(VolumeVibratorManager.VIBRATE_MAX_VOLUME);

            if (null != mVolumeVibrator) {
                if (true == do_checkFromSoundProfile()) {
                    mOriginalVibrateVolume = do_getProfileVibrateVolume(do_getProfileMode_Vibrate_Type(mVibrateType));
                }
                else {
                    mOriginalVibrateVolume = mVolumeVibrator.getVibrateVolume(mVibrateType);
                }
            }
            else {
                Log.i(TAG, "[initSeekBar] mVolumeVibrator null case");
            }

            seekBar.setProgress(mOriginalVibrateVolume);
            seekBar.setOnSeekBarChangeListener(this);

            // mContext.getContentResolver().registerContentObserver(
            //Settings.System.getUriFor(Settings.System.VOLUME_VIBRATE),
            //false, mVolumeObserver);
        }

        private int do_getProfileMode_Vibrate_Type(int org_type) {
            switch (org_type) {
            case VolumeVibratorManager.VIBRATE_TYPE_RING:
                return SoundProfileInfo.INDEX_VIBRATE_RING;
            case VolumeVibratorManager.VIBRATE_TYPE_HAPTIC:
                return SoundProfileInfo.INDEX_VIBRATE_HAPTIC;
            case VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION:
                return SoundProfileInfo.INDEX_VIBRATE_NOTIFICATION;
            default:
                return -1;
            }
        }

        public void stop() {
            stopSample();
            mContext.getContentResolver().unregisterContentObserver(mVolumeObserver);
            mSeekBar.setOnSeekBarChangeListener(null);
        }

        public void setVolume() {

            if (null != mVolumeVibrator) {
                if (Utils.isUpgradeModel()) {
                    mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING,
                            mLastProgress);
                    mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC,
                            mLastProgress);
                    mVolumeVibrator.setVibrateVolume(
                            VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION, mLastProgress);
                } else {
                    mVolumeVibrator.setVibrateVolume(mVibrateType, mLastProgress);
                }
            }
        }

        public void revertVolume() {
            if (null != mVolumeVibrator) {
                if (Utils.isUpgradeModel()) {
                    mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING,
                            mOriginalVibrateVolume);
                    mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC,
                            mOriginalVibrateVolume);
                    mVolumeVibrator
                            .setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION,
                                    mOriginalVibrateVolume);
                } else {
                    mVolumeVibrator.setVibrateVolume(mVibrateType, mOriginalVibrateVolume);
                }
            }
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            if (!fromTouch) {
                return;
            }
            postSetVolume(progress);
        }

        void postSetVolume(int progress) {
            // Do the volume changing separately to give responsive UI
            mLastProgress = progress;
            mHandler.removeCallbacks(this);
            mHandler.post(this);
            //if (play)    sample(progress);

        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mVolumeVibrator != null) {
                sample();
            }
        }

        public void run() {
            if (mVolumeVibrator != null) {
                if (Utils.isUpgradeModel()) {
                    mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING,
                            mLastProgress);
                    mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC,
                            mLastProgress);
                    mVolumeVibrator.setVibrateVolume(
                            VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION, mLastProgress);
                } else {
                    if (do_checkFromSoundProfile() == true) {
                        //do_showProfileVibrateVolume();
                    } else {
                        mVolumeVibrator.setVibrateVolume(mVibrateType, mLastProgress);
                    }
                }
            }
        }

        private void sample() {
            onSampleStarting(this);
            Log.d(TAG, "mSeekBar.getProgress() = " + mSeekBar.getProgress());
            int volume = mSeekBar.getProgress();
            if (mVibrateType == VolumeVibratorManager.VIBRATE_TYPE_HAPTIC) {
                //mVolumeVibrator.vibrate(30, mSeekBar.getProgress());
                mVolumeVibrator.vibrate(new long[] { 0, 30 }, -1, new int[] { volume, volume });
            } else {
                //mVolumeVibrator.vibrate(1000, mSeekBar.getProgress());
                mVolumeVibrator.vibrate(new long[] { 0, 1000 }, -1, new int[] { volume, volume });
            }
        }

        public void stopSample() {
            if (mVolumeVibrator != null) {
                mVolumeVibrator.cancel();
            }
        }

        public SeekBar getSeekBar() {
            return mSeekBar;
        }

        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);

            if (mVolumeVibrator != null) {
                sample();
            }
            postSetVolume(mSeekBar.getProgress());
        }

        public void onSaveInstanceState(VolumeStore volumeStore) {
            if (mLastProgress >= 0) {
                volumeStore.volume = mLastProgress;
                volumeStore.originalVolume = mOriginalVibrateVolume;
            }
        }

        public void onRestoreInstanceState(VolumeStore volumeStore) {
            if (volumeStore.volume != -1) {
                mOriginalVibrateVolume = volumeStore.originalVolume;
                mLastProgress = volumeStore.volume;
                postSetVolume(mLastProgress);
            }
        }
    }

    private boolean do_checkFromSoundProfile() {
        if ("soundprofile.AddSoundProfiles"
                .equals(((Activity)getContext()).getLocalClassName())) {
            return true;
        }
        Log.i("soosin", "init for SoundSettings");
        return false;
    }

    private void do_showProfileVibrateVolume() {
        if (do_checkFromSoundProfile() == true) {
            String vibrate_ring = mSoundProfileInfo
                    .getSoundProfileEachData(SoundProfileInfo.INDEX_VIBRATE_RING);
            String vibrate_notification = mSoundProfileInfo
                    .getSoundProfileEachData(SoundProfileInfo.INDEX_VIBRATE_NOTIFICATION);
            String vibrate_haptic = mSoundProfileInfo
                    .getSoundProfileEachData(SoundProfileInfo.INDEX_VIBRATE_HAPTIC);

            Log.i("soosin", "Ringtone : " + vibrate_ring);
            Log.i("soosin", "vibrate_notification : " + vibrate_notification);
            Log.i("soosin", "vibrate_haptic : " + vibrate_haptic);
            if (null != mSeekBarVolumizer_call) {
                mSeekBarVolumizer_call.mSeekBar.setProgress(Integer.parseInt(vibrate_ring));
                mSeekBarVolumizer_notification.mSeekBar
                        .setProgress(Integer.parseInt(vibrate_notification));
                mSeekBarVolumizer_touch.mSeekBar
                        .setProgress(Integer.parseInt(vibrate_haptic));
            }
        }
    }

    private void do_setProfileVibrateVolume() {
        if (null != mSeekBarVolumizer_call) {
            mSoundProfileInfo.setSoundProfileEachData(SoundProfileInfo.INDEX_VIBRATE_RING,
                    Integer.toString(mSeekBarVolumizer_call.mSeekBar.getProgress()));
            mSoundProfileInfo.setSoundProfileEachData(SoundProfileInfo.INDEX_VIBRATE_NOTIFICATION,
                    Integer.toString(mSeekBarVolumizer_notification.mSeekBar.getProgress()));
            mSoundProfileInfo.setSoundProfileEachData(SoundProfileInfo.INDEX_VIBRATE_HAPTIC,
                    Integer.toString(mSeekBarVolumizer_touch.mSeekBar.getProgress()));
        }
    }

    private int do_getProfileVibrateVolume(int type) {
        return Integer.parseInt(mSoundProfileInfo.getSoundProfileEachData(type));
    }

}
