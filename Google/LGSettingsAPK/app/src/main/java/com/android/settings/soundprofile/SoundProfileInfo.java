package com.android.settings.soundprofile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import com.android.settings.R;
import android.os.SystemProperties;
import com.android.settings.Utils;

import android.media.AudioManager;
import com.lge.media.RingtoneManagerEx;
import android.net.Uri;

import com.lge.systemservice.core.LGContext;
//import com.lge.systemservice.core.LGContextImpl;
import com.lge.systemservice.core.VolumeVibratorManager;

public class SoundProfileInfo {
    private static final String TAG = "SoundProfileInfo";

    private SharedPreferences mSoundProfilesPref; //my sound profiles list sp
    private SharedPreferences mSoundProfilesPref_each; //my sound profiles list sp
    private SharedPreferences mSoundProfilesPref_flag; //my sound profiles list sp
    private SharedPreferences.Editor mSoundProfilesEditor;
    private SharedPreferences.Editor mSoundProfilesEachEditor;
    private SharedPreferences.Editor mSoundProfilesFlagEditor;
    private Map<String, ?> mProfilesMap;

    private SharedPreferences mSoundProfilesPref_default;
    private SharedPreferences.Editor mSoundProfilesDefaultEditor;

    private Context mContext;
    AudioManager mAudioManager;

    private LGContext mServiceContext;
    private VolumeVibratorManager mVolumeVibrator;

    private static final String USER_SOUNDPROFILE_FLAG = "user_soundprofile_flag"; //to check user sound profile is set
    private static final String USER_SOUNDPROFILE = "user_soundprofile";
    private static final String USER_SOUNDPROFILE_EACH = "user_soundprofile_each";

    private static final String USER_SOUNDPROFILE_DEFAULT = "user_soundprofile_default";

    public static final int TYPE_RINGTONE = 1;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_RINGTONE_SIM2 = 8;
    public static final int TYPE_NOTIFICATION_SIM2 = 16;
    public static final int TYPE_RINGTONE_SIM3 = 128;
    public static final int TYPE_NOTIFICATION_SIM3 = 256;

    public static final int INDEX_SOUNDPROFILE = 0;
    public static final int INDEX_VOLUME_RING = 1;
    public static final int INDEX_VOLUME_NOTIFICATION = 2;
    public static final int INDEX_VOLUME_SYSTEM = 3;
    public static final int INDEX_VOLUME_MUSIC = 4;
    public static final int INDEX_VIBRATE_RING = 5;
    public static final int INDEX_VIBRATE_HAPTIC = 6;
    public static final int INDEX_VIBRATE_NOTIFICATION = 7;
    public static final int INDEX_RINGTONE = 8;
    public static final int INDEX_NOTIFICATION = 9;
    public static final int INDEX_RINGTONE_SIM2 = 10;
    public static final int INDEX_NOTIFICATION_SIM2 = 11;
    public static final int INDEX_RINGTONE_SIM3 = 12;
    public static final int INDEX_NOTIFICATION_SIM3 = 13;

    public static final int FLAG_TRUE = 1;
    public static final int FLAG_FALSE = 0;

    public static final String[] SOUNDPROFILE_EACH_NAME = {
            "sound_profile",
            "volume_ring",
            "volume_notification",
            "volume_system",
            "volume_music",
            "vibrate_ring",
            "vibrate_haptic",
            "vibrate_notification",
            "ringtone",
            "notification",
            "ringtone_sim2",
            "notification_sound_sim2",
            "ringtone_sim3",
            "notification_sound_sim3",
    };

    String[][] mParentTypeDB = {{"1", Settings.System.RINGTONE},
            {"2", Settings.System.NOTIFICATION_SOUND},
            //{"4", Settings.System.ALARM_ALERT},
            {"8", SOUNDPROFILE_EACH_NAME[INDEX_RINGTONE_SIM2]},
            {"16", SOUNDPROFILE_EACH_NAME[INDEX_NOTIFICATION_SIM2]},
            //{"32", RINGTONE_VIDEOCALL},
            {"128", SOUNDPROFILE_EACH_NAME[INDEX_RINGTONE_SIM3]},
            {"256", SOUNDPROFILE_EACH_NAME[INDEX_NOTIFICATION_SIM3]}
    };

    public SoundProfileInfo(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mSoundProfilesPref = mContext.getSharedPreferences(USER_SOUNDPROFILE,
                Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
        mSoundProfilesPref_each = mContext.getSharedPreferences(USER_SOUNDPROFILE_EACH,
                Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
        mSoundProfilesPref_flag = mContext.getSharedPreferences(USER_SOUNDPROFILE_FLAG,
                Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);

        mSoundProfilesPref_default = mContext.getSharedPreferences(USER_SOUNDPROFILE_DEFAULT,
                Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);

        mSoundProfilesEditor = mSoundProfilesPref.edit();
        mSoundProfilesEachEditor = mSoundProfilesPref_each.edit();
        mSoundProfilesFlagEditor = mSoundProfilesPref_flag.edit();

        mSoundProfilesDefaultEditor = mSoundProfilesPref_default.edit();

        mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        mServiceContext = new LGContext(mContext);
        mVolumeVibrator = (VolumeVibratorManager)mServiceContext
                .getLGSystemService(LGContext.VOLUMEVIBRATOR_SERVICE);
    }

    public SoundProfileInfo(Context context, int flag) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mSoundProfilesPref = mContext.getSharedPreferences(USER_SOUNDPROFILE, flag);
        mSoundProfilesPref_each = mContext.getSharedPreferences(USER_SOUNDPROFILE_EACH, flag);
        mSoundProfilesPref_flag = mContext.getSharedPreferences(USER_SOUNDPROFILE_FLAG, flag);

        mSoundProfilesEditor = mSoundProfilesPref.edit();
        mSoundProfilesEachEditor = mSoundProfilesPref_each.edit();
        mSoundProfilesFlagEditor = mSoundProfilesPref_flag.edit();

        mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        mServiceContext = new LGContext(mContext);
        mVolumeVibrator = (VolumeVibratorManager)mServiceContext
                .getLGSystemService(LGContext.VOLUMEVIBRATOR_SERVICE);
    }

    public void setUserProfileName(String name) {
        mSoundProfilesFlagEditor.putString("isUserProfile", name);
        Log.d(TAG, "setUserProfileFlag " + name);
        mSoundProfilesFlagEditor.commit();
    }

    public String getUserProfileName() {
        String name = mSoundProfilesPref_flag.getString("isUserProfile", "");
        return name;
    }

    public void setDeafultSoundValue() {

        int volume_ring = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        int volume_notification = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int volume_system = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int volume_music = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int vibrate_ring = mVolumeVibrator
                .getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING);
        int vibrate_haptic = mVolumeVibrator
                .getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC);
        int vibrate_notification = mVolumeVibrator
                .getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION);

        //sound profile
        int value = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.VIBRATE_WHEN_RINGING, 0);
        if (value == 1 && mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            setSoundProfileEachData(INDEX_SOUNDPROFILE, "3");
        } else {
            setSoundProfileEachData(INDEX_SOUNDPROFILE, Long.toString(mAudioManager.getRingerMode()));
        }
        //volume
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            setSoundProfileEachData(INDEX_VOLUME_RING,
                        Long.toString(mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_RING)));
            setSoundProfileEachData(INDEX_VOLUME_NOTIFICATION,
                        Long.toString(mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_NOTIFICATION)));
            setSoundProfileEachData(INDEX_VOLUME_SYSTEM,
                        Long.toString(mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_SYSTEM)));
        } else {
            setSoundProfileEachData(INDEX_VOLUME_RING, Long.toString(volume_ring));
            setSoundProfileEachData(INDEX_VOLUME_NOTIFICATION, Long.toString(volume_notification));
            setSoundProfileEachData(INDEX_VOLUME_SYSTEM, Long.toString(volume_system));
        }
        setSoundProfileEachData(INDEX_VOLUME_MUSIC, Long.toString(volume_music));
        //vibrate
        setSoundProfileEachData(INDEX_VIBRATE_RING, Long.toString(vibrate_ring));
        setSoundProfileEachData(INDEX_VIBRATE_HAPTIC, Long.toString(vibrate_haptic));
        setSoundProfileEachData(INDEX_VIBRATE_NOTIFICATION, Long.toString(vibrate_notification));
        //ringtone and notification

        setDeafultSoundValue_Ringtone();
    }
    
    private void setDeafultSoundValue_Ringtone() {
        Uri ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, RingtoneManagerEx.TYPE_RINGTONE);
        Uri notificationUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, RingtoneManagerEx.TYPE_NOTIFICATION);
        Uri sim2ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_RINGTONE_SIM2);
        Uri sim2notificationUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_NOTIFICATION_SIM2);
        Uri sim3ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_RINGTONE_SIM3);
        Uri sim3notificationUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_NOTIFICATION_SIM3);
        
        if (null != ringtoneUri) {
            setSoundProfileEachData(INDEX_RINGTONE, ringtoneUri.toString());
        } else {
            setSoundProfileEachData(INDEX_RINGTONE, null);
        }
        if (null != notificationUri) {
            setSoundProfileEachData(INDEX_NOTIFICATION, notificationUri.toString());
        } else {
            setSoundProfileEachData(INDEX_NOTIFICATION, null);
        }
        if (null != sim2ringtoneUri) {
            setSoundProfileEachData(INDEX_RINGTONE_SIM2, sim2ringtoneUri.toString());
        } else {
            setSoundProfileEachData(INDEX_RINGTONE_SIM2, null);
        }
        if (null != sim2notificationUri) {
            setSoundProfileEachData(INDEX_NOTIFICATION_SIM2, sim2notificationUri.toString());
        } else {
            setSoundProfileEachData(INDEX_NOTIFICATION_SIM2, null);
        }
        if (null != sim3ringtoneUri) {
            setSoundProfileEachData(INDEX_RINGTONE_SIM3, sim3ringtoneUri.toString());
        } else {
            setSoundProfileEachData(INDEX_RINGTONE_SIM3, null);
        }
        if (null != sim3notificationUri) {
            setSoundProfileEachData(INDEX_NOTIFICATION_SIM3, sim3notificationUri.toString());
        } else {
            setSoundProfileEachData(INDEX_NOTIFICATION_SIM3, null);
        }
    }

    public void setSoundProfileData(String name) {
        ArrayList<String> values = getEachvalue_User();
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            valueBuilder.append(values.get(i));
            valueBuilder.append(",");
        }
        mSoundProfilesEditor.putString(name, valueBuilder.toString());
        Log.d(TAG, "setSoundProfileData name = " + name);
        Log.d(TAG, "setSoundProfileData value = " + valueBuilder.toString());
        mSoundProfilesEditor.commit();
    }

    private void setDeafultSoundValue_Ringtone_Default() {
        Uri ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, RingtoneManagerEx.TYPE_RINGTONE);
        Uri notificationUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, RingtoneManagerEx.TYPE_NOTIFICATION);
        Uri sim2ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_RINGTONE_SIM2);
        Uri sim2notificationUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_NOTIFICATION_SIM2);
        Uri sim3ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_RINGTONE_SIM3);
        Uri sim3notificationUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext, TYPE_NOTIFICATION_SIM3);
        Log.d(TAG, "ringtoneUri " + ringtoneUri);
        Log.d(TAG, "notificationUri " + notificationUri);
        Log.d(TAG, "sim2ringtoneUri " + sim2ringtoneUri);
        Log.d(TAG, "sim2notificationUri " + sim2notificationUri);
        Log.d(TAG, "sim3ringtoneUri " + sim3ringtoneUri);
        Log.d(TAG, "sim3notificationUri " + sim3notificationUri);

        if (null != ringtoneUri) {
            setSoundDefaultData(INDEX_RINGTONE, ringtoneUri.toString());
        } else {
            setSoundDefaultData(INDEX_RINGTONE, null);
        }
        if (null != notificationUri) {
            setSoundDefaultData(INDEX_NOTIFICATION, notificationUri.toString());
        } else {
            setSoundDefaultData(INDEX_NOTIFICATION, null);
        }
        if (null != sim2ringtoneUri) {
            setSoundDefaultData(INDEX_RINGTONE_SIM2, sim2ringtoneUri.toString());
        } else {
            setSoundDefaultData(INDEX_RINGTONE_SIM2, null);
        }
        if (null != sim2notificationUri) {
            setSoundDefaultData(INDEX_NOTIFICATION_SIM2, sim2notificationUri.toString());
        } else {
            setSoundDefaultData(INDEX_NOTIFICATION_SIM2, null);
        }
        if (null != sim3ringtoneUri) {
            setSoundDefaultData(INDEX_RINGTONE_SIM3, sim3ringtoneUri.toString());
        } else {
            setSoundDefaultData(INDEX_RINGTONE_SIM3, null);
        }
        if (null != sim3notificationUri) {
            setSoundDefaultData(INDEX_NOTIFICATION_SIM3, sim3notificationUri.toString());
        } else {
            setSoundDefaultData(INDEX_NOTIFICATION_SIM3, null);
        }
    }


    public void setDeafultSoundValue_Default() {

        int volume_ring = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        int volume_notification = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int volume_system = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int volume_music = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int vibrate_ring = mVolumeVibrator
                .getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING);
        int vibrate_haptic = mVolumeVibrator
                .getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC);
        int vibrate_notification = mVolumeVibrator
                .getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION);

        //sound profile

        int value = Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.VIBRATE_WHEN_RINGING, 0);
        if (value == 1 && mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            setSoundDefaultData(INDEX_SOUNDPROFILE, "3");
        } else {
            setSoundDefaultData(INDEX_SOUNDPROFILE, Long.toString(mAudioManager.getRingerMode()));
        }

        //volume
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            setSoundDefaultData(INDEX_VOLUME_RING,
                        Long.toString(mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_RING)));
            setSoundDefaultData(INDEX_VOLUME_NOTIFICATION,
                        Long.toString(mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_NOTIFICATION)));
            setSoundDefaultData(INDEX_VOLUME_SYSTEM,
                        Long.toString(mAudioManager.getLastAudibleStreamVolume(AudioManager.STREAM_SYSTEM)));
        } else {
            setSoundDefaultData(INDEX_VOLUME_RING, Long.toString(volume_ring));
            setSoundDefaultData(INDEX_VOLUME_NOTIFICATION, Long.toString(volume_notification));
            setSoundDefaultData(INDEX_VOLUME_SYSTEM, Long.toString(volume_system));
        }
        setSoundDefaultData(INDEX_VOLUME_MUSIC, Long.toString(volume_music));
        //vibrate
        setSoundDefaultData(INDEX_VIBRATE_RING, Long.toString(vibrate_ring));
        setSoundDefaultData(INDEX_VIBRATE_HAPTIC, Long.toString(vibrate_haptic));
        setSoundDefaultData(INDEX_VIBRATE_NOTIFICATION, Long.toString(vibrate_notification));
        //ringtone and notification

        setDeafultSoundValue_Ringtone_Default();
    }

    public int getRingtoneIndexbyParentType(int index) {
        switch(index) {
            case 1://ringtone
                return INDEX_RINGTONE;
            case 2://notification
                return INDEX_NOTIFICATION;
            case 8://sim2_ringtone
                return INDEX_RINGTONE_SIM2;
            case 16://sim2_notification
                return INDEX_NOTIFICATION_SIM2;
            case 128://sim3_ringtone
                return INDEX_RINGTONE_SIM3;
            case 256://sim3_notification
                return INDEX_NOTIFICATION_SIM3;
            default:
                return INDEX_RINGTONE;
        }
    }

    public void setSoundDefaultData(int index, String value) {
        mSoundProfilesDefaultEditor.putString(SOUNDPROFILE_EACH_NAME[index], value);
        Log.d(TAG, "setSoundDefaultData " + index + " " + value);
        mSoundProfilesDefaultEditor.commit();
    }

    public void setSoundProfileEachData(int index, String value) {
        mSoundProfilesEachEditor.putString(SOUNDPROFILE_EACH_NAME[index], value);
        Log.d(TAG, "setSoundProfileEachData " + index + " " + value);
        mSoundProfilesEachEditor.commit();
    }



    public String getSoundProfileEachData(int index) {
        String value = mSoundProfilesPref_each.getString(SOUNDPROFILE_EACH_NAME[index], "");
        Log.d(TAG, "getSoundProfileEachData " + index + " " + value);
        return value;
    }

    public String getSoundDefaultData(int index) {
        String value = mSoundProfilesPref_default.getString(SOUNDPROFILE_EACH_NAME[index], "");
        Log.d(TAG, "getSoundDefaultData " + index + " " + value);
        return value;
    }

    //add for not set issue
    public void removeProfiles(String ProfilesName) {
        mSoundProfilesEditor.remove(ProfilesName);
        mSoundProfilesEditor.commit();
    }

    public int getSoundProfilesCount() {

        mProfilesMap = mSoundProfilesPref.getAll();
        Log.d(TAG, "getSoundProfilesCount = " + mProfilesMap.size());
        return mProfilesMap.size();
    }

    public ArrayList<String> getAllProfileValue_User() {

        ArrayList<String> patternValueList_User = new ArrayList<String>();
        mProfilesMap = mSoundProfilesPref.getAll();
        for (Map.Entry<String, ?> entry : mProfilesMap.entrySet())
        {
            patternValueList_User.add(entry.getValue().toString());
        }
        //Collections.sort(patternNameList_User);
        return patternValueList_User;

    }

    public ArrayList<String> getEachvalue_User() {
        ArrayList<String> valueList_User = new ArrayList<String>();
        for (int i = 0; i < SOUNDPROFILE_EACH_NAME.length; i++) {
            valueList_User.add(getSoundProfileEachData(i));
        }

        //Collections.sort(patternNameList_User);
        return valueList_User;

    }
    public ArrayList<String> getAllProfileName_User() {
        ArrayList<String> patternNameList_User = new ArrayList<String>();
        mProfilesMap = mSoundProfilesPref.getAll();
        for (Map.Entry<String, ?> entry : mProfilesMap.entrySet())
        {
            patternNameList_User.add(entry.getKey().toString());
        }
        Collections.sort(patternNameList_User);
        return patternNameList_User;

    }

    public String[] valueTokenizer(String value) {
        String[] soundprofile_value = new String[SOUNDPROFILE_EACH_NAME.length];
        int i = 0;
        for (String token : value.split(",")) {
            soundprofile_value[i] = token;
            i++;
        }
        return soundprofile_value;
    }

    public void setProfileDatatoSystem(String name) {
        String value = mSoundProfilesPref.getString(name, "");
        Log.d(TAG, "setProfileDatatoSystem " + name + " " + value);
        String[] soundprofile_value = new String[SOUNDPROFILE_EACH_NAME.length];
        soundprofile_value = valueTokenizer(value);
        for (int i = 0; i < soundprofile_value.length; i++) {
            Log.d(TAG, "setProfileDatatoSystem = " + soundprofile_value[i]);
        }

        mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_RING]), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_NOTIFICATION]), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_SYSTEM]), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_MUSIC]), AudioManager.FLAG_SHOW_UI_WARNINGS);
        if (soundprofile_value[INDEX_SOUNDPROFILE].equals("3")) {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Settings.System.putInt(mContext.getContentResolver(),
                                    Settings.System.VIBRATE_WHEN_RINGING, 1);
        } else {
            Settings.System.putInt(mContext.getContentResolver(),
                                    Settings.System.VIBRATE_WHEN_RINGING, 0);
            // If stored user sound profile is a no sound (DND),
            // ZEN MODE should be set as ZEN_MODE_NO_INTERRUPTIONS.
            if (soundprofile_value[INDEX_SOUNDPROFILE].equals("0")) {
                Global.putInt(mContext.getContentResolver(), Global.ZEN_MODE, Global.ZEN_MODE_NO_INTERRUPTIONS);
            } else {
                mAudioManager.setRingerMode(Integer.parseInt(soundprofile_value[INDEX_SOUNDPROFILE]));
            }
        }

        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING,
                Integer.parseInt(soundprofile_value[INDEX_VIBRATE_RING]));
        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC,
                Integer.parseInt(soundprofile_value[INDEX_VIBRATE_HAPTIC]));
        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION,
                Integer.parseInt(soundprofile_value[INDEX_VIBRATE_NOTIFICATION]));

        for (int i = 0; i < mParentTypeDB.length; i++ ) {
            int index = getRingtoneIndexFromType(Integer.parseInt(mParentTypeDB[i][0]));
            Settings.System.putString(
                        mContext.getContentResolver(),
                        mParentTypeDB[i][1],
                        soundprofile_value[index]);
        }
    }

    public void setProfileDatatoDefault(int which) {

        String value = null;
        String[] soundprofile_value = new String[SOUNDPROFILE_EACH_NAME.length];
        for (int i = 0; i < SOUNDPROFILE_EACH_NAME.length; i++) {
            value = mSoundProfilesPref_default.getString(SOUNDPROFILE_EACH_NAME[i], "");
            soundprofile_value[i] = value;
        }
        for (int i = 0; i < soundprofile_value.length; i++) {
            Log.d(TAG, "setProfileDatatoDefault = " + soundprofile_value[i]);
        }

        mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_RING]), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_NOTIFICATION]), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_SYSTEM]), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                Integer.parseInt(soundprofile_value[INDEX_VOLUME_MUSIC]), 0);

        if (which == 3) {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Settings.System.putInt(mContext.getContentResolver(),
                                    Settings.System.VIBRATE_WHEN_RINGING, 1);
        } else {
            mAudioManager.setRingerMode(which);
            Settings.System.putInt(mContext.getContentResolver(),
                                    Settings.System.VIBRATE_WHEN_RINGING, 0);
        }

        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING,
                Integer.parseInt(soundprofile_value[INDEX_VIBRATE_RING]));
        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC,
                Integer.parseInt(soundprofile_value[INDEX_VIBRATE_HAPTIC]));
        mVolumeVibrator.setVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION,
                Integer.parseInt(soundprofile_value[INDEX_VIBRATE_NOTIFICATION]));

        for (int i = 0; i < mParentTypeDB.length; i++ ) {
            int mIndex = getRingtoneIndexFromType(Integer.parseInt(mParentTypeDB[i][0]));
            Log.d(TAG, "mIndex = " + getRingtoneIndexFromType(Integer.parseInt(mParentTypeDB[i][0])));
            Settings.System.putString(
                        mContext.getContentResolver(),
                        mParentTypeDB[i][1],
                        soundprofile_value[mIndex]);
        }
    }

    public void getProfileData(String name) {
        String value = mSoundProfilesPref.getString(name, "");
        Log.d(TAG, "getProfileData " + name + " " + value);
        String[] soundprofile_value = new String[SOUNDPROFILE_EACH_NAME.length];
        soundprofile_value = valueTokenizer(value);

        for (int i = 0; i < soundprofile_value.length; i++) {
            setSoundProfileEachData(i, soundprofile_value[i]);
        }
    }

    public int getRingtoneIndexFromType(int type) {
        int ret = INDEX_RINGTONE;
        if (type == RingtoneManagerEx.TYPE_RINGTONE) {
            ret = INDEX_RINGTONE;
        } else if (type == RingtoneManagerEx.TYPE_NOTIFICATION) {
            ret = INDEX_NOTIFICATION;
        } else if (type == TYPE_RINGTONE_SIM2) {
            ret = INDEX_RINGTONE_SIM2;
        } else if (type == TYPE_NOTIFICATION_SIM2) {
            ret = INDEX_NOTIFICATION_SIM2;
        } else if (type == TYPE_RINGTONE_SIM3) {
            ret = INDEX_RINGTONE_SIM3;
        } else if (type == TYPE_NOTIFICATION_SIM3) {
            ret = INDEX_NOTIFICATION_SIM3;
        }
        return ret;
    }
}
