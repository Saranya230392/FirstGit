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

import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.SeekBarVolumizer;

import android.preference.VolumePreference;

import android.provider.Settings;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Vibrator;
import android.media.AudioManagerEx;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.settings.utils.LGSubscriptionManager;

//[AUDIO_FWK]_START, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.
import android.os.SystemProperties;
import android.provider.Settings.Global;

//[AUDIO_FWK]_END, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.
import java.lang.Exception;
import java.lang.reflect.*;

/**
 * Special preference type that allows configuration of both the ring volume and
 * notification volume.
 */
public class RingerVolumePreference extends VolumePreference {
    private static final String TAG = "RingerVolumePreference";
    private static final int MSG_RINGER_MODE_CHANGED = 101;
    private static final String RESUME_INTENT = "lge.settings.intent.action.NOTI_RESUME";
    private static final int STREAM_TYPE_RING = 0;
    private static final int STREAM_TYPE_NOTIFICATION = 1;
    //private static final int STREAM_TYPE_SYSTEM = 2;
    private static final int STREAM_TYPE_MEDIA = 3;
    //private static final int STREAM_TYPE_ALARM = 4;
    private static final int ADD_VOLUME = 1;
    private static final int MINUS_VOLUME = -1;

    public static final boolean RESUME = true;
    public static final boolean PAUSE = false;
    public static boolean sIsActivityState = RESUME;

    private boolean mIsInit = true;
    Vibrator mVibrator;

    private RingerSeekBarVolumizer[] mSeekBarVolumizer;
    private TextView mNotificationSoundTitle = null;
    protected int mOriginalRingerMode;
    protected int mOriginalZenMode;

    //[AUDIO_FWK]_START, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.
    private boolean mIsSoundException = false;
    //[AUDIO_FWK]_END, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.

    //[S][2012.01.16][susin.park][common] Optimus 3.0 scenario - Volume popup
    // SEEKBAR_ID, SEEKBAR_TYPE, CHECKBOX_VIEW_ID, SEEKBAR_MUTED_RES_ID, SEEKBAR_UNMUTED_RES_ID
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
            R.id.alarm_volume_seekbar
    };

    private static final int[] SEEKBAR_TYPE = new int[] {
            AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_ALARM
    };

    private static final int[] CHECKBOX_VIEW_ID = new int[] {
            R.id.ringer_mute_button,
            R.id.notification_mute_button,
            R.id.system_mute_button,
            R.id.media_mute_button,
            R.id.alarm_mute_button
    };

    private static int[] SEEKBAR_MUTED_RES_ID = new int[] {
            R.drawable.ic_audio_ring_notif_mute,
            R.drawable.ic_audio_notification_mute,
            R.drawable.ic_touch_feedback_off,
            R.drawable.ic_audio_media_mute,
            R.drawable.ic_audio_alarm
    };

    private static int[] SEEKBAR_UNMUTED_RES_ID = new int[] {
            R.drawable.ic_audio_ring_notif,
            R.drawable.ic_audio_notification,
            R.drawable.ic_touch_feedback_on,
            R.drawable.ic_audio_media,
            R.drawable.ic_audio_alarm
    };

    private ImageView[] mCheckBoxes = new ImageView[SEEKBAR_MUTED_RES_ID.length];
    private SeekBar[] mSeekBars = new SeekBar[SEEKBAR_ID.length];

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            updateSlidersAndMutedStates();
        }
    };
    private boolean mHeadSetFlag;

    @Override
    public void createActionButtons() {
        setPositiveButtonText(R.string.dlg_ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public void updateUI() {
        updateSlidersAndMutedStates();
        Intent intent = new Intent();
        intent.setAction(RESUME_INTENT);
        getContext().sendBroadcast(intent);
        sIsActivityState = RESUME;
    }

    private void updateSlidersAndMutedStates() {
        for (int i = 0; i < SEEKBAR_TYPE.length; i++) {
            int streamType = SEEKBAR_TYPE[i];
            boolean muted = mAudioManager.isStreamMute(streamType);

            Log.d(TAG,
                    "[updateSlidersAndMutedStates] ##### ringerMode = "
                            + mAudioManager.getRingerMode());
            Log.d(TAG,
                    "[updateSlidersAndMutedStates] ##### vibrateMode = "
                            + mAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER));
            Log.d(TAG, "[updateSlidersAndMutedStates] stream type : " + SEEKBAR_TYPE[i]
                    + "     mute :" + muted);

            if (mCheckBoxes[i] != null) {
                if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
                    if (streamType == AudioManager.STREAM_NOTIFICATION && muted
                            && mAudioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
                        //[S][12.05.18][yj1.cho] Change a vibrate icon for P2
                        int res = Utils.isbuildProduct("p2") ? R.drawable.ic_audio_ring_notif_vibrate
                                : R.drawable.ic_audio_ring_notif_vibrate_disabled;
                        //[E][12.05.18][yj1.cho] Change a vibrate icon for P2
                        mCheckBoxes[i].setImageResource(res);
                    } else {
                        mCheckBoxes[i].setImageResource(
                                muted ? SEEKBAR_MUTED_RES_ID[i] : SEEKBAR_UNMUTED_RES_ID[i]);
                        //mHeadSetFlag = getStatus();
                        do_setMeidaIcon();
                    }
                } else {
                    if (streamType == AudioManager.STREAM_RING && muted
                            && mAudioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
                        //[S][12.05.18][yj1.cho] Change a vibrate icon for P2
                        int res = Utils.isbuildProduct("p2") ? R.drawable.ic_audio_ring_notif_vibrate
                                : R.drawable.ic_audio_ring_notif_vibrate_disabled;
                        //[E][12.05.18][yj1.cho] Change a vibrate icon for P2
                        mCheckBoxes[i].setImageResource(res);
                    } else {
                        mCheckBoxes[i].setImageResource(
                                muted ? SEEKBAR_MUTED_RES_ID[i] : SEEKBAR_UNMUTED_RES_ID[i]);
                        //mHeadSetFlag = getStatus();
                        do_setMeidaIcon();
                    }
                }
            }
            //[S][2012.02.08][susin.park][common] Volume popup seekbar state change
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
            //[E][2012.02.08][susin.park][common] Volume popup seekbar state change

            if (mSeekBars[i] != null) {
                //[AUDIO_FWK]_START, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.
                final int volume = (muted && !mIsSoundException) ? mAudioManager
                        .getLastAudibleStreamVolume(streamType)
                        : mAudioManager.getStreamVolume(streamType);
                //[AUDIO_FWK]_END, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.
                mSeekBars[i].setProgress(volume);
            }
        }
    }

    private void do_setMeidaIcon() {
        boolean muted = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "Get muted :" + muted);

        if ((mAudioManager.getDevicesForStream(AudioManager.STREAM_MUSIC) & (AudioManager.DEVICE_OUT_BLUETOOTH_A2DP
                | AudioManager.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES | AudioManager.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER)) != 0) {
            mCheckBoxes[STREAM_TYPE_MEDIA]
                    .setImageResource(R.drawable.ic_audio_bt);
        } else if (mHeadSetFlag) {
            if (muted && mSeekBars[STREAM_TYPE_MEDIA].getProgress() == 0) {
                mCheckBoxes[STREAM_TYPE_MEDIA]
                        .setImageResource(R.drawable.ic_audio_headset_mute);
            } else {
                mCheckBoxes[STREAM_TYPE_MEDIA]
                        .setImageResource(R.drawable.ic_audio_headset);
            }

        } else {
            if (mSeekBars[STREAM_TYPE_MEDIA].getProgress() == 0) {
                mCheckBoxes[STREAM_TYPE_MEDIA]
                        .setImageResource(R.drawable.ic_audio_media_mute);
            } else {
                mCheckBoxes[STREAM_TYPE_MEDIA]
                        .setImageResource(R.drawable.ic_audio_media);
            }
        }
    }

    private BroadcastReceiver mValueUpdateReceiver;
    private BroadcastReceiver mExternalStatusChangedReceiver;
    private AudioManager mAudioManager;

    //private SeekBarVolumizer mNotificationSeekBarVolumizer;
    //private TextView mNotificationVolumeTitle;

    public RingerVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // The always visible seekbar is for ring volume
        if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
            setStreamType(AudioManager.STREAM_NOTIFICATION);
        } else {
            setStreamType(AudioManager.STREAM_RING);
        }

        setDialogLayoutResource(R.layout.preference_dialog_ringervolume);
        //setDialogIcon(R.drawable.ic_settings_sound);

        mSeekBarVolumizer = new RingerSeekBarVolumizer[SEEKBAR_ID.length];

        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        //[AUDIO_FWK]_START, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.
        mIsSoundException = SystemProperties.getBoolean("ro.lge.audio_soundexception", false);
        //[AUDIO_FWK]_END, 20120111, soon1.choi@lge.com, LGE headset/BT sound exception scenario.

        //[S][2012.03.27][susin.park][P2] p2 only image changed
        setItemImage();
        //[S][2012.03.27][susin.park][P2] p2 only image changed

        mVibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    //[S][2012.03.27][susin.park][P2] p2 only image changed
    private final void setItemImage() {
        if (Utils.isbuildProduct("p2")) {
            SEEKBAR_MUTED_RES_ID[0] = R.drawable.ic_p2_audio_ring_notif_mute;
            SEEKBAR_MUTED_RES_ID[1] = R.drawable.ic_p2_audio_notification_mute;
            SEEKBAR_MUTED_RES_ID[2] = R.drawable.ic_p2_touch_feedback_off;
            SEEKBAR_MUTED_RES_ID[3] = R.drawable.ic_p2_audio_vol;
            SEEKBAR_MUTED_RES_ID[4] = R.drawable.ic_p2_audio_alarm;

            SEEKBAR_UNMUTED_RES_ID[0] = R.drawable.ic_p2_audio_ring_notif;
            SEEKBAR_UNMUTED_RES_ID[1] = R.drawable.ic_p2_audio_notification;
            SEEKBAR_UNMUTED_RES_ID[2] = R.drawable.ic_p2_touch_feedback_on;
            SEEKBAR_UNMUTED_RES_ID[3] = R.drawable.ic_p2_audio_vol;
            SEEKBAR_UNMUTED_RES_ID[4] = R.drawable.ic_p2_audio_alarm;
        }
    }

    //[S][2012.03.27][susin.park][P2] p2 only image changed

    @Override
    protected void onBindDialogView(View view) {
    	
    	/* id seekbar is referenced by VolumePreference located at FW. So, dummy view should be added. 
    	 * This view should be gone. For more information, please contact yonguk.kim */ 
    	SeekBar dummySeekbar = new SeekBar(getContext());
    	dummySeekbar.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT));
    	dummySeekbar.setVisibility(View.GONE);
    	dummySeekbar.setId(com.android.internal.R.id.seekbar);
    	
    	View iconView = view.findViewById(android.R.id.icon);
    	if (iconView != null) {
    		ViewParent vp = iconView.getParent();
    		((ViewGroup)vp).addView(dummySeekbar);
    	}
    	
        super.onBindDialogView(view);
        sIsActivityState = RESUME;
        if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
            final LinearLayout layout_ringtone = (LinearLayout)view
                    .findViewById(R.id.ringer_section);
            layout_ringtone.setVisibility(View.GONE);
        }

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar)view.findViewById(SEEKBAR_ID[i]);
            mSeekBars[i] = seekBar;
            //[AUDIO_FWK]_START, 2012320, seil.park@lge.com, For System & Music Volume Setting Scenario
            //if (SEEKBAR_TYPE[i] == AudioManager.STREAM_MUSIC) {
            //mSeekBarVolumizer[i] = new SeekBarVolumizer(getContext(), seekBar,
            //SEEKBAR_TYPE[i], getMediaVolumeUri(getContext()));
            //} else
            //[AUDIO_FWK]_END2012320, seil.park@lge.com, For System & Music Volume Setting Scenario
            {
                int phoneId = LGSubscriptionManager.getPhoneId(LGSubscriptionManager.getDefaultSubId());
                Uri defaultUri = null;
                defaultUri = setDefualtUri(i, phoneId, defaultUri);

                mSeekBarVolumizer[i] = new RingerSeekBarVolumizer(getContext(), SEEKBAR_TYPE[i], defaultUri, this);
                mSeekBarVolumizer[i].setSeekBar(seekBar);
            }
        }
        mOriginalRingerMode = mAudioManager.getRingerMode();
        mOriginalZenMode = Global.getInt(getContext().getContentResolver(), Global.ZEN_MODE, 0);

        //final int silentableStreams = System.getInt(getContext().getContentResolver(),
        //        System.MODE_RINGER_STREAMS_AFFECTED,
        //        ((1 << AudioSystem.STREAM_NOTIFICATION) | (1 << AudioSystem.STREAM_RING)));
        // Register callbacks for mute/unmute buttons
        for (int i = 0; i < mCheckBoxes.length; i++) {
            ImageView checkbox = (ImageView)view.findViewById(CHECKBOX_VIEW_ID[i]);
            mCheckBoxes[i] = checkbox;
        }

        // Load initial states from AudioManager
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
                    if (intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                        Log.i(TAG, "ACTION_MEDIA_MOUNTED");
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }

                    }
                    else if (intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                        Log.i(TAG, "ACTION_MEDIA_UNMOUNTED");
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                    else if (intent.ACTION_MEDIA_EJECT.equals(action)) {
                        Log.i(TAG, "ACTION_MEDIA_EJECT");
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                }
            };
            getContext().registerReceiver(mExternalStatusChangedReceiver, filter);
        }
        // Listen for updates from AudioManager
        if (mValueUpdateReceiver == null) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            filter.addAction("voice_video_record_finish");
            //filter.addDataScheme("file");
            mValueUpdateReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                        Log.d(TAG, "### onReceive - RINGER_MODE_CHANGED_ACTION");
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_RINGER_MODE_CHANGED, intent
                                .getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1), 0));
                        if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE
                                && getisInit() == false) {
                            Log.d(TAG, "vibrateOn()");
                            vibrateOn();
                        }
                        setisInit(false);
                    }
                    //[S][12.06.01][yj1.cho] Add Headset broadcastreceiver with headset icon
                    else if (intent.ACTION_HEADSET_PLUG.equals(action)) {
                        Log.d(TAG, "### onReceive - ACTION_HEADSET_PLUG");
                        mHeadSetFlag = (intent.getIntExtra("state", 1) == 1);
                        do_setMeidaIcon();
                    }
                    //[E][12.06.01][yj1.cho] Add Headset broadcastreceiver with headset icon
                    else if ("voice_video_record_finish".equals(action)) {
                        Log.d(TAG, "voice_video_record_finish");
                        updateSlidersAndMutedStates();
                        //donghyub.cho 121218 notification sound volume level during voice recording
                        /*VolumeStore m_volume = new VolumeStore();
                        m_volume.volume =  mAudioManager.getStreamVolume(AudioSystem.STREAM_NOTIFICATION);
                        m_volume.originalVolume =  mAudioManager.getStreamVolume(AudioSystem.STREAM_NOTIFICATION);
                        mSeekBarVolumizer[1].onRestoreInstanceState(m_volume);
                        */
                    }

                }
            };
            getContext().registerReceiver(mValueUpdateReceiver, filter);
        }

        //[S][2011.11.24][susin.park][common] Optimus 3.0 scenario - Volume popup
        // Disable either ringer+notifications or notifications
        /*
        int id;
        if (!Utils.isVoiceCapable(getContext())) {
            id = R.id.ringer_section;
        } else {
            id = R.id.notification_section;
        }
        View hideSection = view.findViewById(id);
        hideSection.setVisibility(View.GONE);
        */
        //[E][2011.11.24][susin.park][common] Optimus 3.0 scenario - Volume popup
        View hidealarm = view.findViewById(R.id.alarm_volume);
        View hidealarmtext = view.findViewById(R.id.alarm_textview);
        hidealarm.setVisibility(View.GONE);
        hidealarmtext.setVisibility(View.GONE);
        setisInit(true);
        do_setUI4_1_MODEL(view);
    }

    private Uri setDefualtUri(int i, int phoneId, Uri defaultUri) {
        if (SEEKBAR_TYPE[i] == AudioManager.STREAM_RING) {
            if (phoneId == PhoneConstants.PHONE_ID2) {
                defaultUri = com.lge.provider.LGSettings.DEFAULT_RINGTONE_SIM2_URI;
            } else if (phoneId == PhoneConstants.PHONE_ID3) {
                defaultUri = com.lge.provider.LGSettings.DEFAULT_RINGTONE_SIM3_URI;
            } else {
                defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
            }
        } else if (SEEKBAR_TYPE[i] == AudioManager.STREAM_NOTIFICATION) {
            if (phoneId == PhoneConstants.PHONE_ID2) {
                defaultUri = com.lge.provider.LGSettings.DEFAULT_NOTIFICATION_SIM2_URI;
            } else if (phoneId == PhoneConstants.PHONE_ID3) {
                defaultUri = com.lge.provider.LGSettings.DEFAULT_NOTIFICATION_SIM3_URI;
            } else {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            }
        } else if (SEEKBAR_TYPE[i] == AudioManager.STREAM_SYSTEM) {
            defaultUri = Uri
                    .parse("file:///system/media/audio/ui/Effect_Tick.ogg");
        } else if (SEEKBAR_TYPE[i] == AudioManager.STREAM_MUSIC) {
            defaultUri = Uri
                    .parse("file:///system/media/audio/ui/LG_Media_volume.ogg");
        }
        return defaultUri;
    }

    private void do_setUI4_1_MODEL(View view) {
        if (Utils.isUI_4_1_model(getContext())) {
            mNotificationSoundTitle = (TextView)view
                    .findViewById(R.id.notification_description_text);
            mNotificationSoundTitle.setText(R.string.sp_sound_noti_NORMAL);
        }
    }

    private boolean getisInit() {
        return mIsInit;
    }

    private void setisInit(boolean value) {
        mIsInit = value;
    }

    private void vibrateOn() {
        mVibrator.vibrate(200);
    }

    private Uri getMediaVolumeUri(Context context) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getPackageName()
                + "/" + R.raw.media_volume);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Log.d(TAG, "onDialogClosed - " + positiveResult);
        if (!positiveResult) {
            for (RingerSeekBarVolumizer vol : mSeekBarVolumizer) {
                if (vol != null) {
                    vol.revertVolume();
                }
            }
        }
        cleanup();
    }

    @Override
    public void onActivityStop() {
        super.onActivityStop();
        Log.d(TAG, "onActivityStop");
        for (RingerSeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null) {
                vol.stopSample();
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

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
                if (true == isdown && 0 != mSeekBars[STREAM_TYPE_RING].getProgress()) {
                    if (null != mSeekBarVolumizer[STREAM_TYPE_RING]) {
                        mSeekBarVolumizer[STREAM_TYPE_RING].
                                changeVolumeBy(MINUS_VOLUME);
                    }
                }
            }
            return true;

        case KeyEvent.KEYCODE_VOLUME_UP:
            if (Utils.supportSplitView(getContext()) && !Utils.is070Model(getContext())) {
                if (true == isdown
                        && mSeekBars[STREAM_TYPE_NOTIFICATION].getMax() != mSeekBars[STREAM_TYPE_NOTIFICATION]
                                .getProgress()) {
                    if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                        Global.putInt(getContext().getContentResolver(), Global.ZEN_MODE, 0);
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
                    }
                    else {
                        int currentZenMode = Global.getInt(getContext()
                                .getContentResolver(), Global.ZEN_MODE, 0);
                        if (currentZenMode != Global.ZEN_MODE_OFF) {
                            Global.putInt(getContext().getContentResolver(),
                                          Global.ZEN_MODE, 0);
                        }
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

    @Override
    public void onSampleStarting(SeekBarVolumizer volumizer) {
        super.onSampleStarting(volumizer);
        for (RingerSeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null && vol != volumizer) {
                vol.stopSample();
            }
        }
    }

    protected void cleanup() {
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            if (mSeekBarVolumizer[i] != null) {
                Dialog dialog = getDialog();
                if (dialog != null && dialog.isShowing()) {
                    // Stopped while dialog was showing, revert changes
                    mSeekBarVolumizer[i].revertVolume();
                }
                mSeekBarVolumizer[i].stop();
                mSeekBarVolumizer[i] = null;
            }
        }
        if (mValueUpdateReceiver != null) {
            getContext().unregisterReceiver(mValueUpdateReceiver);
            mValueUpdateReceiver = null;
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

    //LGE_CHANGE_S [susin.park@lge.com 2011.11.15.] multi popup issue fix.[S][yj1.cho] Fix for issue
    @Override
    protected void onClick() {
        // TODO Auto-generated method stub
        if (null == getDialog()) {
            super.onClick();
        }
    }

    //LGE_CHANGE_E [susin.park@lge.com 2011.11.15.] multi popup issue fix.[E][yj1.cho] Fix for issue

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
        sIsActivityState = PAUSE;
        onActivityStop();
    }

    //[E][Settings][TD] sample ringtone stop when incomming calling
    public class RingerSeekBarVolumizer extends SeekBarVolumizer {

        private static final String TAG = "RingerSeekBarVolumizer";

        private final LGVolumePreferenceReceiver mReceiver = new LGVolumePreferenceReceiver();

        public RingerSeekBarVolumizer(Context context, int streamType, Uri defaultUri, Callback callback) {
            super(context, streamType, defaultUri, callback);
            // Audio_Framework: VolumePreference exception
            Log.d(TAG, "RingerSeekBarVolumizer");
            mReceiver.setListening(true);
            mOriginalStreamVolume = mAudioManager.getLastAudibleStreamVolume(mStreamType);
            // Audio_Framework_END
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Do Not anything
                Log.d(TAG, "Do Not anything LOS");
            } else {
                startForAfterLMR1();
            }
        }

        private void startForAfterLMR1() {
            try {
                Class clz = getClass();
                Method method = clz.getMethod("start");
                method.invoke(RingerSeekBarVolumizer.this);
                Log.d(TAG, "startForAfterLMR1 method.invoke");
            } catch (NoSuchMethodException e) {
                Log.d(TAG, e.getMessage());
            } catch (IllegalArgumentException e) {
                Log.d(TAG, e.getMessage());
            } catch (IllegalAccessException e) {
                Log.d(TAG, e.getMessage());
            } catch (InvocationTargetException e) {
                Log.d(TAG, e.getMessage());
            }
        }

        public void revertVolume() {
            // Audio_Framework: Sound profile
            // Revert RingerMode when cancel VolumePopup
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (mOriginalZenMode != Global.ZEN_MODE_OFF) {
                        Global.putInt(mContext.getContentResolver(),
                                      Global.ZEN_MODE, mOriginalZenMode);
                    }
                    //Revert RingerMode
                    if ((mStreamType == AudioManager.STREAM_RING)
                            && (mAudioManager.getRingerMode() != mOriginalRingerMode)) {
                        if (mOriginalZenMode != Global.ZEN_MODE_NO_INTERRUPTIONS) {
                            mAudioManager.setRingerMode(mOriginalRingerMode);
                        }
                    }

                    //Revert StreamVolume
                    Log.d(TAG, "revertVolume mOriginalStreamVolume : " + mOriginalStreamVolume);
                    mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume,
                                                    AudioManagerEx.FLAG_KEEP_RINGER_MODES);
                }
            }, 100 );
            // Audio_Framework_END
        }

        @Override
        public void stop() {
            super.stop();
            mReceiver.setListening(false);
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

            if (seekBar.getId() == R.id.media_volume_seekbar) {
                do_setMeidaIcon();
            }
        // Audio_Framework_END
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromTouch) {
            // TODO Auto-generated method stub
            super.onProgressChanged(seekBar, progress, fromTouch);
            if (seekBar.getId() == R.id.media_volume_seekbar) {
                do_setMeidaIcon();
            }
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
                if ((mOriginalZenMode != Global.ZEN_MODE_OFF)
                        && !(mStreamType == AudioManager.STREAM_MUSIC)) {
                    Global.putInt(mContext.getContentResolver(),
                                  Global.ZEN_MODE, 0);
                }
                postStartSample();
            }
        }

        private final class LGVolumePreferenceReceiver extends BroadcastReceiver {
            private boolean mListening;

            public void setListening(boolean listening) {
                Log.d(TAG, "setListening() listening = " + listening);
                if (mListening == listening) {
                    return;
                }
                mListening = listening;
                if (listening) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_HEADSET_PLUG);
                    filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                    filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
                    mContext.registerReceiver(mReceiver, filter);
                } else {
                    mContext.unregisterReceiver(mReceiver);
                }
            }

            private void onActionHeadsetPlug(int headsetState) {
                if (headsetState == 1) {
                    try {
                        Thread.currentThread().sleep(100);
                    }
                    catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException");
                    }
                }

                int newOriginalvolume = mAudioManager.getStreamVolume(mStreamType);
                if (mSeekBar != null
                    && (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL
                    || mStreamType == AudioManager.STREAM_MUSIC)) {

                    Log.d(TAG, "ACTION_HEADSET_PLUG: Org vol: " + mOriginalStreamVolume
                            + ", New vol" + newOriginalvolume);

                    mSeekBar.setProgress(newOriginalvolume);
                    mOriginalStreamVolume = newOriginalvolume;
                }
            }

            private void onActionPhoneStateChanged(String state) {
                if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    stopSample();
                }
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive()");

                if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                    int headsetState = intent.getIntExtra("state", 0);
                    onActionHeadsetPlug(headsetState);
                    return;
                }

                // Audio_Framework: VolumePreference exception
                // stop playing sample sound when call is comming
                if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    onActionPhoneStateChanged(state);
                    return;
                }
                // Audio_Framework_END

                // maintain BGM when changed to Vibrate Mode 185
                if (intent.getAction().equals(
                        "android.media.RINGER_MODE_CHANGED")) {
                    if (intent
                            .getIntExtra("android.media.EXTRA_RINGER_MODE", 2) != AudioManager.RINGER_MODE_NORMAL) {
                        if (isSamplePlaying()) {
                            stopSample();
                        }
                    }
                }
                // Audio_Framework_END
            }
        }
    }
}
