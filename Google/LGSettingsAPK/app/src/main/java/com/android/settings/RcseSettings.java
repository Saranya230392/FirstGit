/*
 * 
 */

package com.android.settings;

import android.os.SystemProperties;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import android.app.Activity;
import android.preference.PreferenceGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.settings.R;
import android.database.sqlite.SQLiteException;
import com.android.settings.lgesetting.Config.Config;
import java.io.File;
import java.io.FileReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.provider.Settings;

import com.android.settings.RcseSettingsInfo;

public class RcseSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnCheckedChangeListener {
    private static final String TAG = "RcseSettings";
    public static final String ACTION_SETTINGS_CHANGED = "com.lge.ims.rcs.SETTINGS_CHANGED";
    public static final String ACTIONSETTINGSCHANGEDRCSE =
            "com.lge.ims.action.SETTINGS_CHANGED";
    static final String CONTENT_URI = "content://com.lge.ims.provisioning/settings";
    static final String CONTENT_URI_BB = "content://com.lge.ims.rcs/user";
    static final String CONTENT_URI_PROFILE = "content://com.lge.ims.rcs/ac";
    private static String ACCEPT_RCSe = "is_accept";
    private static String ACCEPT_BB = "starter_accept";

    private boolean hasIccCard = false;
    private boolean mVersionIsRCSe = false;
    private PreferenceScreen parent;
    private CheckBoxPreference mCheckBoxService = null;
    private CheckBoxPreference mCheckBoxRoaming = null;
    private ListPreference mJoynServiceProfile = null;

    private boolean bRcs_e_service_in_DB = false;
    private int bRcs_service_onoff_in_DB = 0;
    private boolean bRcs_e_roaming_in_DB = false;
    private PreferenceScreen mChatSettings = null;

    private final static int DEFAULT_VALUE = 0;
    private final static int MSG_CHECK_MULTICLIENT = 0;
    private final static int MSG_UPDATE_PREFERNECE = 1;

    private final static int ON = 1;
    private final static int OFF = 0;

    private static boolean sRcsCheckMultiClient_resume = false; //to service on if there's no activated client after checking multi-client

    CharSequence[] mRcs_profile_entry = new CharSequence[2];
    CharSequence[] mRcs_profile_entry_temp;
    CharSequence[] rcs_profile_value;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
            case MSG_CHECK_MULTICLIENT:
                boolean mEnableStatus = false;
                mEnableStatus = RcseSettingsInfo.checkMultiClientEnabled(getActivity());
                if (mEnableStatus) {
                    rcs_multiclient_dialog();
                }
                break;
            case MSG_UPDATE_PREFERNECE:
                RcseSettingsInfo.editPref(getActivity(),
                        mJoynServiceProfile.getEntry().equals("Activate") ? true : false);
                break;
            default:
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RcseSettingsInfo.checkProfile(getActivity())) {
            mVersionIsRCSe = true;
            Log.e(TAG, "mVersionIsRCSe" + mVersionIsRCSe);
        }
        sRcsCheckMultiClient_resume = false;
        addPreferencesFromResource(R.layout.rcse_settings);
        parent = (PreferenceScreen)findPreference("rcse_settings");
        mJoynServiceProfile = (ListPreference)findPreference("rcs_e_service_profile");
        if (!mVersionIsRCSe) {
                getActivity().getActionBar().setTitle(R.string.sp_rich_communication_title_NORMAL);
                mJoynServiceProfile.setTitle(R.string.sp_rich_communication_enable_NORMAL);
                mJoynServiceProfile.setSummary(
                        R.string.sp_rcseservicestarter_rich_noti_description2_MLINE);
                mJoynServiceProfile.setDialogTitle(R.string.sp_rich_communication_enable_NORMAL);
        }
        setJoynProfileArrayList();
        setJoynEntriesValues();
        mJoynServiceProfile.setOnPreferenceChangeListener(this);
        mCheckBoxService = (CheckBoxPreference)findPreference("rcs_e_service");
        mCheckBoxRoaming = (CheckBoxPreference)findPreference("rcs_e_roaming");
        mChatSettings = (PreferenceScreen)findPreference("rcs_e_chat_settings");
        Log.e(TAG, "mVersionIsRCSe" + mVersionIsRCSe);
        Intent itNoticeNum = new Intent("com.lge.ims.ac.AC_NOTICELIST_NUMS_REQUEST");
        getActivity().sendBroadcast(itNoticeNum);
        Intent itNoticeList = new Intent("com.lge.ims.ac.AC_NOTICELIST_REQUEST");
        getActivity().sendBroadcast(itNoticeList);
        //Intent itTerm = new Intent("com.lge.ims.ac.AC_TERMS_REQUEST");
        //getActivity().sendBroadcast(itTerm);
        hasIccCard = TelephonyManager.getDefault().hasIccCard();
        if (RcseSettingsInfo.isTest) {
            hasIccCard = true;
            bRcs_service_onoff_in_DB = 1;
        }
        Log.e(TAG, "hasIccCard" + hasIccCard);
        RcseSettingsInfo.readIMSI(getActivity());

        checkMenuList();
        final Activity act = getActivity();
        // These are contained in the "container" preference group
        PreferenceGroup parentPreference = (PreferenceGroup)findPreference("container");
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "rcs_e_terms",
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "rcs_e_privacy",
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        if (Config.getCountry().equals("KR")) {
            if (!checkAgreement()) {
                Log.e(TAG, "INININ");
                Intent itTerm = new Intent("com.lge.ims.ac.AC_TERMS_REQUEST");
                getActivity().sendBroadcast(itTerm);
                Intent intent = new Intent();
                intent.setClassName("com.lge.ims.rcsstarter",
                        "com.lge.ims.rcsstarter.ui.TermActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("type", "starter_activity");
                startActivityForResult(intent, 1);
            }
            getPreferenceScreen().removePreference(parent.findPreference("rcs_e_roaming"));
        } else {
            if (parent != null) {
                parent.removePreference(parent.findPreference("rcs_about_service"));
            }
        }
        //handler.sendEmptyMessageDelayed(MSG_CHECK_MULTICLIENT, 100);
    }

    private void init_UI() {
        Cursor mCursor = null;
        Cursor mCursor_BB = null;
        ContentResolver mContentResolver = null;
        if (mVersionIsRCSe) {
            try {
                mContentResolver = getContentResolver();
                mCursor =
                        mContentResolver.query(Uri.parse(
                                CONTENT_URI + "/" + RcseSettingsInfo.getIMSIvalue()), null, null,
                                null, null);

                if (mCursor != null && mCursor.moveToNext()) {
                    mCheckBoxService =
                            (CheckBoxPreference)findPreference("rcs_e_service");
                    bRcs_e_service_in_DB =
                            (mCursor.getInt(
                                    mCursor.getColumnIndex("rcs_e_service")) == ON) ? true : false;
                    mCheckBoxService.setChecked(bRcs_e_service_in_DB);

                    if (!Config.getCountry().equals("KR")) {
                        mCheckBoxRoaming =
                                (CheckBoxPreference)findPreference("rcs_e_roaming");
                        bRcs_e_roaming_in_DB = (mCursor.getInt(
                                mCursor.getColumnIndex(
                                        "rcs_e_roaming")) == ON) ? true : false;
                        Log.e("JJJJJ", "" + bRcs_e_roaming_in_DB);
                        mCheckBoxRoaming.setChecked(bRcs_e_roaming_in_DB);
                    }
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLiteException : " + e.getMessage());
            } finally {
                if (mCursor != null) {
                    mCursor.close();
                }
            }

            if (!hasIccCard) {
                mCheckBoxService.setEnabled(false);
                if (!Config.getCountry().equals("KR")) {
                    mCheckBoxRoaming.setEnabled(false);
                }
            }

            if (false == mCheckBoxService.isChecked()) {
                mChatSettings.setEnabled(false);
                mCheckBoxRoaming.setEnabled(false);
            } else {
                mChatSettings.setEnabled(true);
                mCheckBoxRoaming.setEnabled(true);
            }
        } else {
            try {
                mContentResolver = getContentResolver();
                mCursor_BB = mContentResolver.query(Uri.parse(CONTENT_URI_BB),
                        null, null, null, null);
                if (mCursor_BB != null && mCursor_BB.moveToNext()) {
                    bRcs_service_onoff_in_DB = mCursor_BB.getInt(mCursor_BB
                            .getColumnIndex("service_onoff"));
                    if (RcseSettingsInfo.isTest) {
                        bRcs_service_onoff_in_DB = 1;
                    }
                    Log.d(TAG, "[init_UI]bRcs_service_onoff_in_DB " + bRcs_service_onoff_in_DB);
                    if (bRcs_service_onoff_in_DB == ON) {
                        mJoynServiceProfile.setValue(String.valueOf(0));
                    } else {
                        mJoynServiceProfile.setValue(String.valueOf(1));
                    }
                }
            } catch (SQLiteException e) {
                Log.d(TAG, "excetpion1 - bRcs_service_onoff_in_DB" + e.toString());
            } finally {
                if (mCursor_BB != null) {
                    mCursor_BB.close();
                }
                Log.d(TAG, "excetpion1 - bRcs_service_onoff_in_DB" + bRcs_service_onoff_in_DB);
            }

            if (bRcs_service_onoff_in_DB == ON) {
                mJoynServiceProfile.setValue(String.valueOf(0));
            } else {
                mJoynServiceProfile.setValue(String.valueOf(1));
            }

            //if (bRcs_service_onoff_in_DB == 1) {
            if (bRcs_service_onoff_in_DB == ON) {
                mChatSettings.setEnabled(true);
            } else {
                mChatSettings.setEnabled(false);
            }
            Log.d(TAG, "init_UI - bRcs_service_onoff_in_DB" + bRcs_service_onoff_in_DB);
            Log.d(TAG, "init_UI - mJoynServiceProfile.getEntry()" + mJoynServiceProfile.getEntry());

            if (!hasIccCard) {
                mJoynServiceProfile.setEnabled(false);
                mCheckBoxService.setEnabled(false);
                if (!Config.getCountry().equals("KR")) {
                    mCheckBoxRoaming.setEnabled(false);
                }
            }
            setJoynSummary();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sRcsCheckMultiClient_resume) {
            boolean mEnableStatus = false;
            mEnableStatus = RcseSettingsInfo.checkMultiClientEnabled(getActivity());
            if (!mEnableStatus) {
                RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(), "service_onoff", true);
                Intent intent = new Intent();
                intent.setAction("com.lge.ims.rcsservice.action.DELETE_FOR_RCS_ACTIVATE");
                getActivity().sendBroadcast(intent);
                Log.d(TAG, "send broadcast com.lge.ims.rcsservice.action.DELETE_FOR_RCS_ACTIVATE");
            }
            sRcsCheckMultiClient_resume = false;
        }
        init_UI();
        if (!checkAgreement()) {
            //    finish();
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PREFERNECE, 100);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        //Cursor mCursor = mContentResolver.query(Uri.parse(CONTENT_URI + "/" + IMSI), null, null, null, null);
        //Cursor mCursor = mContentResolver.query(Uri.parse(CONTENT_URI), null, null, null, null);
        if (preference == mCheckBoxService) {
            if (mCheckBoxService.isChecked()) {
                mHandler.sendEmptyMessageDelayed(MSG_CHECK_MULTICLIENT, 100);
                RcseSettingsInfo
                        .checkValueChangedandBroadcast(getActivity(), "rcs_e_service", true);
                mCheckBoxRoaming.setEnabled(true);
                mChatSettings.setEnabled(true);
            } else {
                rcs_attention_dialog();
                //mCheckBoxService.setChecked(bRcs_e_service_in_DB);
                mCheckBoxRoaming.setEnabled(false);
                mChatSettings.setEnabled(false);
            }
        } else if (preference == mCheckBoxRoaming) {
            if (mCheckBoxRoaming.isChecked()) {
                rcs_e_roaming_dialog();
            } else {
                RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(), "rcs_e_roaming",
                        false);
            }
        } else if (preference == mChatSettings) {
            Intent intent;
            try {
                if (mVersionIsRCSe) {
                    intent = new Intent("com.lge.ims.rcsim.action.RICHCHAT_INTRO");
                    intent.putExtra("com.lge.ims.rcsim.fromSettings", true);
                } else {
                    intent = new Intent("com.android.mms.ui.SETTING_VIEW");
                    intent.putExtra("com.android.mms.fromSettings", true);
                }
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(),
                        getString(R.string.toast_joyn_roaming),
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        // init_UI(); 
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        // Don't remove this function
        // If remove this, onCreate() will show password popup when orientation is changed. 
        super.onConfigurationChanged(newConfig);
    }

    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        // TODO Auto-generated method stub
        if (mCheckBoxService.isChecked()) {
        } else {
        }
    }

    public void checkMenuList() {
        PreferenceCategory category = (PreferenceCategory)parent
                .findPreference("rcse_network");
        PreferenceCategory category_chat_settings = (PreferenceCategory)parent
                .findPreference("rcse_chat_settings");
        if (mVersionIsRCSe) {
            getPreferenceScreen().removePreference(parent
                    .findPreference("rcs_e_service_profile"));
        } else {
            getPreferenceScreen().removePreference(parent
                    .findPreference("rcs_e_roaming"));
            getPreferenceScreen().removePreference(parent
                    .findPreference("rcs_e_service"));
            getPreferenceScreen().removePreference(category);
            getPreferenceScreen().removePreference(category_chat_settings);
        }
    }

    public void setJoynProfileArrayList() {
        if (mVersionIsRCSe) {
            mRcs_profile_entry = getResources().getStringArray(
                    R.array.rcs_e_service_profile_entries);
        } else {
            mRcs_profile_entry_temp = getResources().getStringArray(
                    R.array.rcs_e_service_profile_entries_rich);
            for (int i = 0; i < 2; i++) {
                mRcs_profile_entry[i] = mRcs_profile_entry_temp[i];
            }
        }
        rcs_profile_value = getResources().getStringArray(
                R.array.rcs_e_service_profile_values);
    }

    public void setJoynEntriesValues() {
        mJoynServiceProfile.setEntries(mRcs_profile_entry);
        mJoynServiceProfile.setEntryValues(rcs_profile_value);
        mJoynServiceProfile.setValue(String.valueOf(DEFAULT_VALUE));
    }

    public void setJoynSummary() {
        if (mJoynServiceProfile.getEntry().equals("Activate")) {
            if (mVersionIsRCSe) {
                 mJoynServiceProfile.setSummary(
                        R.string.sp_rcseservicestarter_joyn_noti_description2_MLINE);
            } else {
                 mJoynServiceProfile.setSummary(
                       R.string.sp_rcseservicestarter_rich_noti_description2_MLINE);
             }
        } else {
             String mDateTime = RcseSettingsInfo.getDateTimeFormatForRCS(getActivity());
             if (mDateTime != null ) {
              mJoynServiceProfile.setSummary(mJoynServiceProfile.getEntry() + " " +
                 getResources().getString(R.string.sp_rich_communication_since_NORMAL)
                 + " " + mDateTime);
             } else {
                if (mVersionIsRCSe) {
                  mJoynServiceProfile.setSummary(mJoynServiceProfile.getEntry() + " " +
                  getResources().getString(R.string.sp_joyn_title_NORMAL));
                } else {
                       mJoynServiceProfile.setSummary(mJoynServiceProfile.getEntry() + " " +
                       getResources().getString(R.string.sp_rich_communication_title_NORMAL));
                }
			}
       }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();

        if ("rcs_e_service_profile".equals(key)) {
            try {
                int value = Integer.parseInt((String)objValue);
                if (value == 0) {
                    //Activate 
                    mHandler.sendEmptyMessageDelayed(MSG_CHECK_MULTICLIENT, 100);
                    mCheckBoxRoaming.setEnabled(false);
                    Log.d(TAG, "Activate in preferencechange");
                    RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(), "service_onoff",
                            true);

                    mJoynServiceProfile.setValue(String.valueOf(0));
                    mCheckBoxRoaming.setEnabled(false);
                    mChatSettings.setEnabled(false);
                } else if (value == 1) {
                    //Deactivate 
                    rcs_attention_dialog();
                    Log.d(TAG, "Deactivate in preferencechange");
                    //    writeDB("service_onoff", 0);
                    //    mJoynServiceProfile.setValue(String.valueOf(1));
                    mCheckBoxRoaming.setEnabled(true);
                    mChatSettings.setEnabled(true);
                } else {
                    rcs_BB_attention_dialog();
                    mCheckBoxRoaming.setEnabled(false);
                    mChatSettings.setEnabled(false);
                }
            } catch (NumberFormatException e) {
                Log.d(TAG, "could not persist rcse profile setting", e);
            }
        }
        Log.d(TAG, "change" + bRcs_service_onoff_in_DB);
        init_UI();
        return true;
    }

    private void rcs_multiclient_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (mVersionIsRCSe) {
            builder.setTitle(R.string.sp_rcs_e_service_NORMAL);
        } else {
            builder.setTitle(R.string.sp_rich_communication_enable_NORMAL);
        }
        builder.setMessage(R.string.rcse_multi_clent_dexcription_rich);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (RcseSettingsInfo.getEnabledPackageName() != null) {
                            Log.d(TAG,
                                    "intentinfo in dialog = "
                                            + RcseSettingsInfo.getEnabledPackageName());
                            Intent i = new Intent(RcseSettingsInfo.getEnabledPackageName());
                            startActivity(i);
                            sRcsCheckMultiClient_resume = true;
                        }
                        mCheckBoxService.setChecked(false);
                        mJoynServiceProfile.setValue(String.valueOf(1));
                        setJoynSummary();
                        if (mVersionIsRCSe) {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "rcs_e_service", false);
                        } else {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "service_onoff", false);
                        }

                        mChatSettings.setEnabled(false);
                        mCheckBoxRoaming.setEnabled(false);

                        Intent intent = new Intent();
                        intent.setAction("com.lge.ims.rcsservice.action.READY_FOR_RCS_ACTIVATE");
                        getActivity().sendBroadcast(intent);
                        Log.d(TAG, "send broadcast com.lge.ims.rcsservice.action.READY_FOR_RCS_ACTIVATE");
                    }
                });
        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckBoxService.setChecked(false);
                        mJoynServiceProfile.setValue(String.valueOf(1));
                        setJoynSummary();
                        if (mVersionIsRCSe) {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "rcs_e_service", false);
                        } else {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "service_onoff", false);
                        }

                        mCheckBoxRoaming.setEnabled(false);
                        mChatSettings.setEnabled(false);
                    }
                });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        mCheckBoxService.setChecked(false);
                        mJoynServiceProfile.setValue(String.valueOf(1));
                        setJoynSummary();
                        if (mVersionIsRCSe) {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "rcs_e_service", false);
                        } else {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "service_onoff", false);
                        }

                        mCheckBoxRoaming.setEnabled(false);
                        mChatSettings.setEnabled(false);
                        break;
                    default:
                        break;
                    }
                }
                return false;
            }
        });
        builder.show();
    }

    //[E][20121004][yj1.cho]

    private void rcs_attention_dialog() {
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.sp_rcs_e_setting_dialog_title_NORMAL);
        if (mVersionIsRCSe) {
            builder.setMessage(R.string.sp_rcs_e_service_dialog_message_MLINE);
        } else {
                builder.setMessage(R.string.sp_rcs_e_service_dialog_message_rich_MLINE);
        }
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.def_yes_btn_caption,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckBoxService.setChecked(false);
                        //Deactivate
                        mJoynServiceProfile.setValue(String.valueOf(1));
                        setJoynSummary();
                        if (mVersionIsRCSe) {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "rcs_e_service", false);
                        } else {
                            RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                    "service_onoff", false);
                        }

                        mCheckBoxRoaming.setEnabled(false);
                        mChatSettings.setEnabled(false);
                    }
                });
        builder.setNegativeButton(R.string.def_no_btn_caption,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckBoxService.setChecked(true);
                        Log.d(TAG, "rcs_attention_dialog - bRcs_service_onoff_in_DB"
                                + bRcs_service_onoff_in_DB);
                        if (bRcs_service_onoff_in_DB == OFF) {
                            mJoynServiceProfile.setValue(String.valueOf(1));
                        } else if (bRcs_service_onoff_in_DB == ON) {
                            mJoynServiceProfile.setValue(String.valueOf(0));
                        }
                        setJoynSummary();
                    }
                });
        //[S] 2011-12-01 yongnam.cha@lge.com
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        mCheckBoxService.setChecked(true);
                        //Activate
                        if (bRcs_service_onoff_in_DB == OFF) {
                            mJoynServiceProfile.setValue(String.valueOf(1));
                        } else if (bRcs_service_onoff_in_DB == ON) {
                            mJoynServiceProfile.setValue(String.valueOf(0));
                        }
                        setJoynSummary();
                        break;
                    default:
                    }
                }
                return false;
            }
        });
        //[E] 2011-12-01 yongnam.cha@lge.com
        dialog = builder.create();
        //dialog.setCanceledOnTouchOutside(true);
        //dialog.setCancelable(false);
        dialog.show();
    }

    private void rcs_BB_attention_dialog() { // disable joyn permanently popup
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.sp_dlg_note_NORMAL);
        if (mVersionIsRCSe) {
            builder.setMessage(R.string.rcs_bb_disable_permanently);
            builder.setIconAttribute(android.R.attr.alertDialogIcon);
        } else {
            builder.setMessage(R.string.rcs_bb_disable_permanently_rich);
        }
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.def_yes_btn_caption,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckBoxService.setChecked(false);
                        //Deactivate
                        mJoynServiceProfile.setValue(String.valueOf(2));
                        setJoynSummary();
                        RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                "service_onoff", false);

                        Intent intent = new Intent();
                        intent.setAction("com.lge.ims.rcs.DISABLE_PERMANENTLY");
                        getActivity().sendBroadcast(intent);

                        mCheckBoxRoaming.setEnabled(false);
                        mChatSettings.setEnabled(false);
                        RcseSettingsInfo.setRCSSupportStatus(getActivity(), false);
                    }
                });
        builder.setNegativeButton(R.string.def_no_btn_caption,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckBoxService.setChecked(true);
                        Log.d(TAG, "rcs_BB_attention_dialog - bRcs_service_onoff_in_DB"
                                + bRcs_service_onoff_in_DB);

                        if (bRcs_service_onoff_in_DB == OFF) {
                            mJoynServiceProfile.setValue(String.valueOf(1));
                        } else if (bRcs_service_onoff_in_DB == ON) {
                            mJoynServiceProfile.setValue(String.valueOf(0));
                        }

                        setJoynSummary();
                    }
                });
        //[S] 2011-12-01 yongnam.cha@lge.com
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        mCheckBoxService.setChecked(true);
                        if (bRcs_service_onoff_in_DB == OFF) {
                            mJoynServiceProfile.setValue(String.valueOf(1));
                        } else if (bRcs_service_onoff_in_DB == ON) {
                            mJoynServiceProfile.setValue(String.valueOf(0));
                        }
                        setJoynSummary();
                        break;
                    default:
                    }
                }
                return false;
            }
        });
        //[E] 2011-12-01 yongnam.cha@lge.com
        dialog = builder.create();
        //dialog.setCanceledOnTouchOutside(true);   
        //dialog.setCancelable(false);
        dialog.show();
    }

    private void rcs_e_roaming_dialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.sp_rcs_e_setting_dialog_title_NORMAL);
        builder.setMessage(R.string.sp_rcs_e_roaming_dialog_message_MLINE);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.def_yes_btn_caption,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckBoxRoaming.setChecked(true);
                        RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                "rcs_e_roaming", true);
                    }
                });
        builder.setNegativeButton(R.string.def_no_btn_caption,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckBoxRoaming.setChecked(false);
                        RcseSettingsInfo.checkValueChangedandBroadcast(getActivity(),
                                "rcs_e_roaming", false);
                    }
                });
        //[S] 2011-12-01 yongnam.cha@lge.com
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        mCheckBoxRoaming.setChecked(false);
                        break;
                    default:
                    }
                }
                return false;
            }
        });
        builder.show();
    }

    private boolean checkAgreement() {
        boolean mAgreement = false;
        Cursor mCursor = null;
        ContentResolver mContentResolver = null;

        try {
            mContentResolver = getContentResolver();
            if (mVersionIsRCSe) {
                mCursor = mContentResolver.query(Uri.parse(
                        CONTENT_URI + "/" + RcseSettingsInfo.getIMSIvalue()), null, null, null,
                        null);
            } else {
                mCursor = mContentResolver.query(Uri.parse(CONTENT_URI_BB), null,
                        null, null, null);
            }

            if (mCursor != null && mCursor.moveToNext()) {
                if (mVersionIsRCSe) {
                    mAgreement = (mCursor.getInt(mCursor
                            .getColumnIndex(ACCEPT_RCSe)) == ON) ? true : false;
                } else {
                    mAgreement = (mCursor.getInt(mCursor
                            .getColumnIndex(ACCEPT_BB)) == ON) ? true : false;
                }
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "SQLiteException");
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
        return mAgreement;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        boolean mAgreement = checkAgreement();
        switch (requestCode) {
        case 1:
            if (!mAgreement) {
                finish();
            }
            break;
        default:
            break;
        }
    }
}
