package com.android.settings.soundprofile;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
//weiyt.jiang
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.Ringtone;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.System;
import android.widget.EditText;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.widget.Toast;
import android.widget.Button;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.graphics.Paint;
import android.view.inputmethod.InputMethodManager;
import com.android.settings.ByteLengthFilter;
import android.content.DialogInterface.OnShowListener;

import java.util.ArrayList;

//[S][2012.02.09][susin.park][common][common] Add to Quiet time menu 
import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.database.ContentObserver;
import android.text.format.DateFormat;
import android.content.SharedPreferences;
import android.view.MenuItem;

//import com.lge.provider.SettingsEx;
//[E][2012.02.09][susin.park][common][common] Add to Quiet time menu 

import com.android.settings.lgesetting.Config.Config;

import java.util.Map;
import com.android.settings.R;
import com.android.settings.SoundSettings;
import android.widget.CheckBox;
import com.android.settings.Utils;

//LGE_E nexti. dongkyu31.lee
public class AddSoundProfiles extends PreferenceActivity implements
        OnPreferenceChangeListener,OnClickListener {

    private static final String TAG = "AddSoundProfiles";
    private Preference usernameEditTextPreference = null;
    private static final int MSG_UPDATE_RINGTONE_SUMMARY = 1;
    private static final int MSG_UPDATE_NOTIFICATION_SUMMARY = 2;
    private static final int MSG_UPDATE_RINGTONE_SUMMARY2 = 3;
    private static final int MSG_UPDATE_NOTIFICATION_SUMMARY2 = 4;
    private static final int MSG_UPDATE_RINGTONE_SUMMARY3 = 5;
    private static final int MSG_UPDATE_NOTIFICATION_SUMMARY3 = 6;
    private static final int MAX_LEN = 15;

    private static final int RENAME_SIM_TYPE_RINGTONE = 0;
    private static final int RENAME_SIM_TYPE_NOTIFICATION = 1;

    private static final int RENAME_SIM1_INDEX = 0;
    private static final int RENAME_SIM2_INDEX = 1;
    private static final int RENAME_SIM3_INDEX = 2;

    private int ringerMode;
    private int temp_ringermode_value = 2;
    ArrayList<String[]> sound_profile_entry;
    ArrayList<String[]> sound_profile_value;
    private Context mContext;

    private AudioManager mAudioManager;
    private Preference mLGRingtonePreference;
    private Preference mLGNotificationPreference;
    private Preference mSubRingtonePreference;
    private Preference mSubNotificationPreference;
    private Preference mThirdRingtonePreference;
    private Preference mThirdNotificationPreference;
    private Preference mLGVibrateVolumePreference;
    private Preference mSoundProfileStatusPreference;
    private RingerVolumePreferenceEx mRingVolPreference;
    private AlertDialog mRenameDialog;
    private Toast mToast;
    private InputMethodManager imm;
    //private int len;

    SoundProfileInfo mSoundProfileInfo;
    private SharedPreferences.Editor mProfilesPrefEditor;
    private SharedPreferences mSoundProfilesPref; // my sound profiles list sp
    private static final String USER_SOUNDPROFILE = "user_soundprofile";

    private static final String KEY_LG_RINGTONE = "lg_ringtone";
    private static final String KEY_SUB_RINGTONE = "sub_ringtone"; // sub sim
    private static final String KEY_THIRD_RINGTONE = "third_ringtone"; // sub sim

    private static final String KEY_LG_NOTIFICATION = "lg_notification";
    private static final String KEY_SUB_NOTIFICATION_SOUND = "sub_notification_sound"; // sub
    private static final String KEY_THIRD_NOTIFICATION_SOUND = "third_notification_sound"; // sub

    private boolean mIsModifyProfile = false;
    private boolean CompareResultSame = false;
    private String mModifyName = null;
    private Map<String, ?> mProfilesMap;
    private CheckBox visibleCheckBox;
    private ArrayList<String> mItems;

    private int mCurrentRingtoneType = 1;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.lge.update_soundprofile")) {
                do_updateSoundProfile();
            }
        }
    };

    // MyProfile Menu Support Button
    Button mCancel;
    Button mSave;
    boolean mCancelFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MyProfile Set Button
        setContentView(R.layout.vibrate_piker);
        mSave = (Button)findViewById(R.id.ok_button);
        mSave.setOnClickListener(this);
        mSave.setText(R.string.def_save_btn_caption);

        mCancel = (Button)findViewById(R.id.cancel_button);
        mCancel.setOnClickListener(this);


        addPreferencesFromResource(R.xml.add_sound_profiles);
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.sp_sound_profile_add_NORMAL);
        }
        mContext = getApplicationContext();

        usernameEditTextPreference = (Preference)findPreference("profiles_name");
        usernameEditTextPreference.setOnPreferenceChangeListener(this);
        mLGRingtonePreference = findPreference(KEY_LG_RINGTONE);
        mLGNotificationPreference = findPreference(KEY_LG_NOTIFICATION);
        mSubRingtonePreference = findPreference(KEY_SUB_RINGTONE);
        mSubNotificationPreference = findPreference(KEY_SUB_NOTIFICATION_SOUND);
        mThirdRingtonePreference = findPreference(KEY_THIRD_RINGTONE);
        mThirdNotificationPreference = findPreference(KEY_THIRD_NOTIFICATION_SOUND);
        mSoundProfileStatusPreference = (Preference)findPreference("Status");
        mLGVibrateVolumePreference = (Preference)findPreference("vibrate_volume");
        mRingVolPreference = (RingerVolumePreferenceEx)findPreference("ring_volume");

        sound_profile_entry = new ArrayList<String[]>();
        sound_profile_value = new ArrayList<String[]>();
        sound_profile_entry.add(
                0,
                getResources().getStringArray(
                        R.array.sp_sound_profile_los_entries_NORMAL));
        sound_profile_value.add(
                0,
                getResources().getStringArray(
                        R.array.sp_sound_profile_values_NORMAL));

        mItems = new ArrayList<String>();
        mItems.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_SILENT]);
        mItems.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_VIBRATE]);
        mItems.add(sound_profile_entry.get(0)[AudioManager.RINGER_MODE_NORMAL]);
        mItems.add(getResources()
                .getString(R.string.sp_SoundProfile_Sound_vibrate_NORMAL));

        mAudioManager = (AudioManager)this
                .getSystemService(Context.AUDIO_SERVICE);

        // do_updateRingtoneName();
        mSoundProfileInfo = new SoundProfileInfo(mContext);

        Intent intent = this.getIntent();
        mModifyName = intent.getStringExtra("user_soundprofile_modify");
        if (mModifyName != null) {
            mIsModifyProfile = true;
        }
        if (!mIsModifyProfile) {
            mSoundProfileInfo.setDeafultSoundValue();
        } else {
            usernameEditTextPreference.setSummary(mModifyName);
            mSoundProfileInfo.getProfileData(mModifyName);
            if (actionBar != null) {
                actionBar.setTitle(R.string.sp_sound_profile_edit_NORMAL);
            }
        }
        mSoundProfilesPref = this.getSharedPreferences(USER_SOUNDPROFILE, 0);
        do_InitSIMDependancyMenu();

        if (!(Utils.getVibrateTypeProperty().equals("1") || Utils.getVibrateTypeProperty().equals(
                "2"))) {
            if (null != mLGVibrateVolumePreference) {
                getPreferenceScreen().removePreference(mLGVibrateVolumePreference);
            }
        }
    }

    private void do_updateRingtoneName() {

        if (mLGRingtonePreference != null) {
            updateRingtoneName(RingtoneManager.TYPE_RINGTONE,
                    mLGRingtonePreference, MSG_UPDATE_RINGTONE_SUMMARY);
        }
        if (mSubRingtonePreference != null) {
            updateRingtoneName(SoundProfileInfo.TYPE_RINGTONE_SIM2,
                    mSubRingtonePreference, MSG_UPDATE_RINGTONE_SUMMARY2);
        }
        if (mThirdRingtonePreference != null) {
            updateRingtoneName(SoundProfileInfo.TYPE_RINGTONE_SIM3,
                    mThirdRingtonePreference, MSG_UPDATE_RINGTONE_SUMMARY3);
        }

        if (mLGNotificationPreference != null) {
            updateRingtoneName(RingtoneManager.TYPE_NOTIFICATION,
                    mLGNotificationPreference, MSG_UPDATE_NOTIFICATION_SUMMARY);
        }
        if (mSubNotificationPreference != null) {
            updateRingtoneName(SoundProfileInfo.TYPE_NOTIFICATION_SIM2,
                    mSubNotificationPreference, MSG_UPDATE_NOTIFICATION_SUMMARY2);
        }
        if (mThirdNotificationPreference != null) {
            updateRingtoneName(SoundProfileInfo.TYPE_NOTIFICATION_SIM3,
                    mThirdNotificationPreference, MSG_UPDATE_NOTIFICATION_SUMMARY3);
        }

    }

    private void updateRingtoneName(int type, Preference preference, int msg) {
        if (preference == null) {
            return;
        }
        int index = SoundProfileInfo.INDEX_RINGTONE;
        switch (type) {
        case RingtoneManager.TYPE_RINGTONE:
            index = SoundProfileInfo.INDEX_RINGTONE;
            break;
        case SoundProfileInfo.TYPE_RINGTONE_SIM2:
            index = SoundProfileInfo.INDEX_RINGTONE_SIM2;
            break;
        case SoundProfileInfo.TYPE_RINGTONE_SIM3:
            index = SoundProfileInfo.INDEX_RINGTONE_SIM3;
            break;
        case RingtoneManager.TYPE_NOTIFICATION:
            index = SoundProfileInfo.INDEX_NOTIFICATION;
            break;
        case SoundProfileInfo.TYPE_NOTIFICATION_SIM2:
            index = SoundProfileInfo.INDEX_NOTIFICATION_SIM2;
            break;
        case SoundProfileInfo.TYPE_NOTIFICATION_SIM3:
            index = SoundProfileInfo.INDEX_NOTIFICATION_SIM3;
            break;
        default:
            break;
        }

        Uri ringtoneUri = Uri.parse(mSoundProfileInfo
                .getSoundProfileEachData(index));
        CharSequence summary = null;
        summary = SoundSettings.getTitle(this, ringtoneUri, true, type);
        Log.d(TAG, "updateRingtoneName = " + summary);
        mHandler.sendMessage(mHandler.obtainMessage(msg, summary));
    }

    private void do_handleMessage_Ringtone(Message msg) {
        switch (msg.what) {
        case MSG_UPDATE_RINGTONE_SUMMARY:

            if (mLGRingtonePreference != null) {
                mLGRingtonePreference.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_RINGTONE_SUMMARY2:
            if (mSubRingtonePreference != null) {
                mSubRingtonePreference.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_RINGTONE_SUMMARY3:
            if (mThirdRingtonePreference != null) {
                mThirdRingtonePreference.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_NOTIFICATION_SUMMARY:
            if (mLGNotificationPreference != null) {
                mLGNotificationPreference.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_NOTIFICATION_SUMMARY2:
            if (mSubNotificationPreference != null) {
                mSubNotificationPreference.setSummary((CharSequence)msg.obj);
            }
            break;
        case MSG_UPDATE_NOTIFICATION_SUMMARY3:
            if (mThirdNotificationPreference != null) {
                mThirdNotificationPreference.setSummary((CharSequence)msg.obj);
            }
            break;
        default:
            break;
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_RINGTONE_SUMMARY:
            case MSG_UPDATE_NOTIFICATION_SUMMARY:
            case MSG_UPDATE_RINGTONE_SUMMARY2:
            case MSG_UPDATE_NOTIFICATION_SUMMARY2:
            case MSG_UPDATE_RINGTONE_SUMMARY3:
            case MSG_UPDATE_NOTIFICATION_SUMMARY3:
                do_handleMessage_Ringtone(msg);
                break;
            default:
                break;
            }
        }
    };

    private int getCurrentRingtoneType() {
        return mCurrentRingtoneType;
    }

    private void setCurrentRingtoneType(int type) {
        mCurrentRingtoneType = type;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        Log.d("wey", "onPreferenceTreeClick");
        if (preference.equals(usernameEditTextPreference)) {
            createNameDialog();
        } else if (preference.equals(mSoundProfileStatusPreference)) {
            showSoundProfileDialog();

        } else if (preference == mLGRingtonePreference ||
                preference == mLGNotificationPreference ||
                preference == mSubRingtonePreference ||
                preference == mSubNotificationPreference ||
                preference == mThirdRingtonePreference ||
                preference == mThirdNotificationPreference) {
            if (preference == mLGRingtonePreference) {
                setCurrentRingtoneType(RingtoneManager.TYPE_RINGTONE);
            } else if (preference == mLGNotificationPreference) {
                setCurrentRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
            } else if (preference == mSubRingtonePreference) {
                setCurrentRingtoneType(SoundProfileInfo.TYPE_RINGTONE_SIM2);
            } else if (preference == mSubNotificationPreference) {
                setCurrentRingtoneType(SoundProfileInfo.TYPE_NOTIFICATION_SIM2);
            } else if (preference == mThirdRingtonePreference) {
                setCurrentRingtoneType(SoundProfileInfo.TYPE_RINGTONE_SIM3);
            } else if (preference == mThirdNotificationPreference) {
                setCurrentRingtoneType(SoundProfileInfo.TYPE_NOTIFICATION_SIM3);
            }
            ringerMode = mAudioManager.getRingerMode();
            if (ringerMode == AudioManager.RINGER_MODE_SILENT
                    || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                int currentValue = Settings.System.getInt(getContentResolver(),
                        "RINGTONE_NOTIFICATION_DO_NOT_SHOW", 0);
                if (currentValue != 1) {
                    LayoutInflater factory = LayoutInflater.from(this);
                    View inpuView = factory.inflate(R.layout.notification_do_not_show, null);
                    visibleCheckBox = (CheckBox)inpuView.findViewById(R.id.notification_check);
                    visibleCheckBox.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            //visibleNotification.playSoundEffect(AudioManager.FX_KEY_CLICK);
                        }
                    });
                        new AlertDialog.Builder(this)
                            .setTitle(R.string.sp_sound_profile_note_NORMAL)
                            .setView(inpuView)
                            .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    if (visibleCheckBox.isChecked()) {
                                        Settings.System.putInt(getContentResolver(),
                                                "RINGTONE_NOTIFICATION_DO_NOT_SHOW", 1);
                                    }
                                    showRingtonePicker();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                } else {
                    showRingtonePicker();
                }
            } else {
                showRingtonePicker();
            }
        }
        return true;
    }

    private void showRingtonePicker() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings",
                "com.android.settings.Settings$RingtonePickerActivity"));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                getCurrentRingtoneType());
        intent.putExtra("user_soundprofile", true);
        startActivity(intent);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        Log.d("wey", "onPreferenceChange");
        if (preference.equals(usernameEditTextPreference)) {
            usernameEditTextPreference.setSummary(newValue.toString());
        }
        return true;
    }

    private void createNameDialog() {

        final AlertDialog.Builder customDialogBuidler = new AlertDialog.Builder(
                this);

        LayoutInflater inflate = (LayoutInflater)this
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflate.inflate(
                R.layout.vibrate_text_input_dialog, null);
        final EditText edit = (EditText)contentView
                .findViewById(R.id.username_edit);
        edit.setPaintFlags(edit.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        final ByteLengthFilter filter = new ByteLengthFilter(this, MAX_LEN);
        filter.setInputProperty(imm, edit);
        filter.setOnMaxLengthListener(new ByteLengthFilter.OnMaxLengthListener() {
            @Override
            public void onMaxLength() {
                maxlengthEditToast();
            }
        });

        //weiyt.jiang add for rename [s]
        if (mIsModifyProfile || !usernameEditTextPreference.getSummary()
                .equals(getResources()
                        .getString(R.string.sp_sound_profile_default_summary_NORMAL))) {
            edit.setText(usernameEditTextPreference.getSummary());
        }
        //weiyt.jiang add for rename [e]
        edit.requestFocus();
        edit.selectAll();
        edit.setFilters(new InputFilter[] { filter });
        edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        customDialogBuidler.setTitle(R.string.sp_sound_profile_name_NORMAL);
        customDialogBuidler.setPositiveButton(
                getResources().getString(R.string.dlg_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        mProfilesMap = mSoundProfilesPref.getAll();
                        for (Map.Entry<String, ?> entry : mProfilesMap
                                .entrySet()) {
                            if (edit.getText()
                                    .toString()
                                    .toUpperCase()
                                    .equals(entry.getKey().toString()
                                            .toUpperCase())) {
                                CompareResultSame = true;
                            }
                        }

                        if (edit.getText()
                                .toString()
                                .equalsIgnoreCase(
                                        sound_profile_entry.get(0)[AudioManager.RINGER_MODE_SILENT]
                                                .toString())
                                || edit.getText()
                                        .toString()
                                        .equalsIgnoreCase(
                                                sound_profile_entry.get(0)[AudioManager.RINGER_MODE_VIBRATE]
                                                        .toString())
                                || edit.getText()
                                        .toString()
                                        .equalsIgnoreCase(
                                                sound_profile_entry.get(0)[AudioManager.RINGER_MODE_NORMAL]
                                                        .toString())
                                || edit.getText()
                                        .toString()
                                        .equalsIgnoreCase(
                                                getResources()
                                                        .getString(
                                                                R.string.sp_SoundProfile_Sound_vibrate_NORMAL))
                                || edit.getText()
                                        .toString()
                                        .equalsIgnoreCase(
                                                getResources()
                                                        .getString(
                                                                R.string.sp_sound_profile_default_summary_NORMAL))
                                || CompareResultSame) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    R.string.sp_sound_profile_duplicated_alert_NORMAL,
                                    Toast.LENGTH_SHORT).show();
                        } else if (true == iskeyBlank(edit.getText().toString())) {
                            Toast.makeText(
                                    AddSoundProfiles.this,
                                    R.string.sp_sound_profile_unnamed_NORMAL,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            //weiyt.jiang add for rename  [s]
                            if (usernameEditTextPreference.getSummary().toString()
                                    .equals(mSoundProfileInfo.getUserProfileName())) {
                                mSoundProfileInfo.setUserProfileName(edit.getText().toString());
                            }
                            mSoundProfileInfo.removeProfiles(usernameEditTextPreference
                                    .getSummary().toString());
                            usernameEditTextPreference.setSummary(edit.getText().toString());
                            //weiyt.jiang add for rename  [e]

                        }
                        CompareResultSame = false;
                    }
                });
        customDialogBuidler.setNegativeButton(
                getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });
        customDialogBuidler.setView(contentView);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                InputMethodManager input = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                input.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);

        mRenameDialog = customDialogBuidler.create();

        mRenameDialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                // TODO Auto-generated method stub
                edit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                            int before, int count) {
                        // TODO Auto-generated method stub

                        //len = filter.getByteLength(edit.getText().toString());
                        //int checkSpace = edit.getText().toString().trim()
                        //       .length();
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                            int count, int after) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // TODO Auto-generated method stub

                        if (iskeyBlank(s.toString())
                                || isAllSpace(s.toString())) {
                            ((AlertDialog)dialog).getButton(
                                    AlertDialog.BUTTON_POSITIVE).setEnabled(
                                    false);
                        } else {
                            ((AlertDialog)dialog).getButton(
                                    AlertDialog.BUTTON_POSITIVE).setEnabled(
                                    true);
                        }
                    }
                });

            }
        });

        mRenameDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mRenameDialog.show();
        mRenameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void maxlengthEditToast() {
        if (mToast == null) {
            mToast = Toast
                    .makeText(AddSoundProfiles.this,
                            R.string.sp_auto_reply_maxlength_NORMAL,
                            Toast.LENGTH_SHORT);
        } else {
            mToast.setText(R.string.sp_auto_reply_maxlength_NORMAL);
        }
        mToast.show();
    }

    public boolean iskeyBlank(String saveName) {
        return "".equals(saveName) ? true : false;
    }

    public boolean isAllSpace(String name) {
        int length = name.length();
        boolean isAllSpace = true;
        for (int i = 0; i < length; i++) {
            if (name.charAt(i) == ' ') {
                isAllSpace = true;
            } else {
                isAllSpace = false;
            }
        }
        return isAllSpace;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter1 = new IntentFilter("com.lge.update_soundprofile");
        this.registerReceiver(mReceiver, filter1);

        do_updateRingtoneName();
        do_updateSoundProfile();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mCancelFlag) {
            mSoundProfileInfo.setSoundProfileData(usernameEditTextPreference
                    .getSummary().toString());
        }
        updateDefaultRingtone();
        mRingVolPreference.RingStop();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (usernameEditTextPreference
                    .getSummary()
                    .toString()
                    .equals(getResources().getString(
                            R.string.sp_sound_profile_default_summary_NORMAL))) {
                showExitAlert();
                return false;
            } else {
                mToast.makeText(this, R.string.sp_auto_reply_saved_NORMAL, Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (usernameEditTextPreference.getSummary().toString()
                .equals(getResources().getString(R.string.sp_sound_profile_default_summary_NORMAL))) {
            showExitAlert();
        } else {
            super.onBackPressed();
            mToast.makeText(this, R.string.sp_auto_reply_saved_NORMAL, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDefaultRingtone() {
        // If current sound profile is user defined sound profile
        // and its ringtones have been changed,
        // the changed ringtone should be applied to default.
        if (usernameEditTextPreference.getSummary().toString()
                .equals(mSoundProfileInfo.getUserProfileName())) {
            mSoundProfileInfo.setProfileDatatoSystem(
                mSoundProfileInfo.getUserProfileName());
        }
    }

    private void do_updateSoundProfile() {
        String soundprofile_status = mSoundProfileInfo
                .getSoundProfileEachData(SoundProfileInfo.INDEX_SOUNDPROFILE);
        Log.d(TAG, "VIBRATE_WHEN_RINGING = " + soundprofile_status);
        if (soundprofile_status.equals("3")) {
            temp_ringermode_value = Integer.parseInt(soundprofile_status);
            mSoundProfileStatusPreference
                    .setSummary(R.string.sp_SoundProfile_Sound_vibrate_NORMAL);
        } else {
            temp_ringermode_value = Integer
                    .parseInt(sound_profile_value.get(0)[Integer
                            .parseInt(soundprofile_status)]);
            mSoundProfileStatusPreference
                    .setSummary(sound_profile_entry.get(0)[temp_ringermode_value]);
        }
    }

    private void showExitAlert() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sp_sound_profile_note_NORMAL)
                .setMessage(R.string.sp_sound_profile_unnamed_NORMAL)
                .setPositiveButton(R.string.dlg_ok, null)
                .setNegativeButton(R.string.sp_sound_profile_exit_NORMAL,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                mProfilesPrefEditor = mSoundProfilesPref.edit();
                                mProfilesPrefEditor
                                        .remove(getResources()
                                                .getString(
                                                        R.string.sp_sound_profile_default_summary_NORMAL));
                                mProfilesPrefEditor.commit();
                                finish();
                            }
                        }).show();
    }

    public void showSoundProfileDialog() {
        Log.d(TAG, "showSoundProfileDialog");
        String soundprofile_status = mSoundProfileInfo
                .getSoundProfileEachData(SoundProfileInfo.INDEX_SOUNDPROFILE);
        if (soundprofile_status.equals("3")) {
            temp_ringermode_value = Integer.parseInt(soundprofile_status);
        } else {
            temp_ringermode_value = Integer
                    .parseInt(sound_profile_value.get(0)[Integer
                            .parseInt(soundprofile_status)]);
        }

        CharSequence[] cs = mItems.toArray(new CharSequence[mItems.size()]);

        new AlertDialog.Builder(this)
                .setTitle(mSoundProfileStatusPreference.getTitle())
                .setSingleChoiceItems(cs,
                        temp_ringermode_value,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface _dialog,
                                    int which) {
                                if (which == 3) {
                                    temp_ringermode_value = 3;
                                    mSoundProfileStatusPreference
                                            .setSummary(R.string.sp_SoundProfile_Sound_vibrate_NORMAL);
                                } else {
                                    temp_ringermode_value = Integer
                                            .parseInt(sound_profile_value.get(0)[which]);
                                    mSoundProfileStatusPreference
                                            .setSummary(sound_profile_entry.get(0)[which]);
                                }
                                mSoundProfileInfo
                                        .setSoundProfileEachData(
                                                SoundProfileInfo.INDEX_SOUNDPROFILE,
                                                Integer.toString(temp_ringermode_value));
                                _dialog.cancel();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
    }

    private void do_InitSIMDependancyMenu() {
        boolean isDualSim = Utils.isMultiSimEnabled(); // Dual Sim status check
        boolean isTripleSim = Utils.isTripleSimEnabled();

        Log.d(TAG, "isDualSim = " + isDualSim);
        //boolean isDualSim = true;
        if (isTripleSim == true) { //triple
            if (null != mLGRingtonePreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_RINGTONE,
                            RENAME_SIM1_INDEX);
                    mLGRingtonePreference.setTitle(sim_name);
                } else {
                    mLGRingtonePreference.setTitle(R.string.sp_sub_sim1_ringtone_title_NORMAL);
                }
            }
            if (null != mSubRingtonePreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_RINGTONE,
                            RENAME_SIM2_INDEX);
                    mSubRingtonePreference.setTitle(sim_name);
                } else {
                    mSubRingtonePreference.setTitle(R.string.sp_sub_sim2_ringtone_title_NORMAL);
                }
            }
            if (null != mThirdRingtonePreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_RINGTONE,
                            RENAME_SIM3_INDEX);
                    mThirdRingtonePreference.setTitle(sim_name);
                } else {
                    mThirdRingtonePreference.setTitle(R.string.sp_sub_sim3_ringtone_title_NORMAL);
                }
            }

            if (null != mLGNotificationPreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_NOTIFICATION,
                            RENAME_SIM1_INDEX);
                    mLGNotificationPreference.setTitle(sim_name);
                }
            }
            if (null != mSubNotificationPreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_NOTIFICATION,
                            RENAME_SIM2_INDEX);
                    mSubNotificationPreference.setTitle(sim_name);
                }
            }
            if (null != mThirdNotificationPreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_NOTIFICATION,
                            RENAME_SIM3_INDEX);
                    mThirdNotificationPreference.setTitle(sim_name);
                }
            }
        } else if (false == isDualSim) { //sigle
            if (Utils.isUI_4_1_model(mContext)) {
                mLGRingtonePreference.setTitle(R.string.sp_sub_sim1_ringtone_title_NORMAL_ex);
            }
            if (null != mSubRingtonePreference
                    || null != mSubNotificationPreference
                    || null != mThirdRingtonePreference
                    || null != mThirdNotificationPreference) {
                if (null != mSubRingtonePreference) {
                    //category.removePreference(mSubRingtonePreference);
                    getPreferenceScreen().removePreference(mSubRingtonePreference);
                }
                if (null != mSubNotificationPreference) {
                    //category.removePreference(mSubNotificationPreference);
                    getPreferenceScreen().removePreference(mSubNotificationPreference);
                }
                if (null != mThirdRingtonePreference) {
                    //category.removePreference(mThirdRingtonePreference);
                    getPreferenceScreen().removePreference(mThirdRingtonePreference);
                }
                if (null != mThirdNotificationPreference) {
                    //category.removePreference(mThirdNotificationPreference);
                    getPreferenceScreen().removePreference(mThirdNotificationPreference);
                }
            }
        } else {
            if (null != mLGRingtonePreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_RINGTONE,
                            RENAME_SIM1_INDEX);
                    mLGRingtonePreference.setTitle(sim_name);
                } else {
                    mLGRingtonePreference.setTitle(R.string.sp_sub_sim1_ringtone_title_NORMAL);
                }
            }
            if (null != mSubRingtonePreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_RINGTONE,
                            RENAME_SIM2_INDEX);
                    mSubRingtonePreference.setTitle(sim_name);
                }
            }

            if (null != mLGNotificationPreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_NOTIFICATION,
                            RENAME_SIM1_INDEX);
                    mLGNotificationPreference.setTitle(sim_name);
                }
            }
            if (null != mSubNotificationPreference) {
                if (Utils.isUI_4_1_model(mContext)) {
                    String sim_name = Utils.do_getSoundSimName(mContext,
                            RENAME_SIM_TYPE_NOTIFICATION,
                            RENAME_SIM2_INDEX);
                    mSubNotificationPreference.setTitle(sim_name);
                }
            }
            if (null != mThirdRingtonePreference
                    && null != mThirdNotificationPreference) {
                //category.removePreference(mThirdRingtonePreference);
                //category.removePreference(mThirdNotificationPreference);
            }
            if (null != mThirdRingtonePreference) {
                getPreferenceScreen().removePreference(mThirdRingtonePreference);
            }
            if (null != mThirdNotificationPreference) {
                getPreferenceScreen().removePreference(mThirdNotificationPreference);
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        case R.id.cancel_button:
            mCancelFlag = true;
            finish();
            break;
        case R.id.ok_button:
            if (usernameEditTextPreference
                    .getSummary()
                    .toString().equals(getResources().getString(
                            R.string.sp_sound_profile_default_summary_NORMAL))) {
                showExitAlert();
            } else {
                super.onBackPressed();
                mToast.makeText(this, R.string.sp_auto_reply_saved_NORMAL,
                        Toast.LENGTH_SHORT).show();
            }
            break;
        default:
            break;
        }
    }
}
