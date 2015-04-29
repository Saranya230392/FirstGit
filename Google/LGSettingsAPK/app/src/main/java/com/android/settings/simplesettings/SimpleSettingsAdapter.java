package com.android.settings.simplesettings;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.media.Ringtone;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;

import com.android.settings.lgesetting.Config.Config;

import com.lge.media.RingtoneManagerEx;
import com.android.settings.SoundSettings;

import java.util.ArrayList;
import java.util.List;

public class SimpleSettingsAdapter extends BaseAdapter {
    private static final String TAG = "SimpleSettingsAdapter";

    private Context mContext;
    private ArrayList<ListItemData> mArrData;
    private LayoutInflater mInflater;
    private float mSetFontSize = 0;
    private TextView mSelectedText;
    private final Configuration mCurConfig = new Configuration();
    public int mCheckFontinit = -1;
    public int mCheckUserCheckFontSeekbar = 0;
    public int mCheckFontProgress;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    public static SeekBar sVolumeSeekBar;
    private Ringtone mRingtone;
    private boolean mIsSeletecVolumeBtn = false;
    private boolean mIsSeekBarMaxLevel = false;
    public AlertDialog mFontWarnigDialog = null;
    private final WifiManager mWifiManager;
    private Button mWifi_btn1 = null;
    private Button mWifi_btn2 = null;

    private Button mBtn1_type3 = null;
    private Button mBtn2_type3 = null;
    private Button mBtn3_type3 = null;

    private static final int PREV_VIBRATE_TIME = 200;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        public void run() {
            do_ringtoneStop();
        }
    };

    public SimpleSettingsAdapter(Context context,
            ArrayList<ListItemData> arrData) {
        this.mContext = context;
        this.mArrData = arrData;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAudioManager = (AudioManager)mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public int getCount() {
        return mArrData.size();
    }

    @Override
    public ListItemData getItem(int position) {
        return mArrData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        return mArrData.get(position).getmType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView position=" + position + "  " + getItemViewType(position));

        int res = 0;
        switch (getItemViewType(position)) {
        case 0:
            res = R.layout.simple_settings_type_seekbar;
            break;
        case 1:
            res = R.layout.simple_settings_type_text_button;
            break;
        case 2:
            res = R.layout.simple_settings_type_two_button;
            break;
        case 3:
            res = R.layout.simple_settings_type_three_button;
            break;
        case 4:
            res = R.layout.simple_settings_type_text_seekbar;
            break;
        default:
            res = R.layout.simple_settings_type_three_button;
            break;
        }
        convertView = mInflater.inflate(res, parent, false);
        convertView.setOnHoverListener(new ListOnHoverListener(position, convertView));

        TextView mListName = (TextView)convertView.findViewById(R.id.tv_name);
        mListName.setText(getItem(position).getmName());
        switch (getItemViewType(position)) {
        case 0:
        case 4:
            TypeSeekbarData tsd = (TypeSeekbarData)getItem(position).getDetailView();
            SeekBar seekbar = (SeekBar)convertView.findViewById(R.id.seekbar);
            seekbar.setOnSeekBarChangeListener(new TypeSeekbarOnSeekBarChangeListener(position, convertView));
            seekbar.setOnFocusChangeListener(mOnFocusChangeHandler);
            seekbar.setOnHoverListener(new SeekbarOnHoverListener(position, convertView, seekbar));
            seekbar.setMax(tsd.getSeekLevel());
            if (position == SimpleSettingsConstants.INDEX_VOLUME) {
                sVolumeSeekBar = seekbar;
            }
            TextView previewText = (TextView)convertView.findViewById(R.id.text_preveiw);
            previewText.setVisibility(tsd.getTextViewVisible());
            initSeekBar(position, seekbar, convertView);
            break;
        case 1:
            Button btn1_type1 = (Button)convertView.findViewById(R.id.btn1);
            mSelectedText = (TextView)convertView.findViewById(R.id.tv_selected);
            if (position == SimpleSettingsConstants.INDEX_RINGTONE) {
                mSelectedText.setText(getRingtoneTitle());
            } else if (position == SimpleSettingsConstants.INDEX_CHANGE_HOMEMODE) {
                mSelectedText.setText(getLauncherCurName(mContext));
            }
            btn1_type1.setOnClickListener(new TypeTextButtonOnButtonListener(position));
            break;
        case 2:
            Button btn1_type2 = (Button)convertView.findViewById(R.id.btn1);
            btn1_type2.setText(getItem(position).getmBtn1());
            btn1_type2.setOnClickListener(new TypeTwobuttonOnButtonListener(position, convertView));
            Button btn2_type2 = (Button)convertView.findViewById(R.id.btn2);
            btn2_type2.setText(getItem(position).getmBtn2());
            btn2_type2.setOnClickListener(new TypeTwobuttonOnButtonListener(position, convertView));
            initButton(position, convertView);
            break;
        case 3:
        default:
            mBtn1_type3 = (Button)convertView.findViewById(R.id.btn1);
            mBtn1_type3.setText(getItem(position).getmBtn1());
            mBtn1_type3.setOnClickListener(new ListOnButtonListener(position, convertView));
            mBtn2_type3 = (Button)convertView.findViewById(R.id.btn2);
            mBtn2_type3.setText(getItem(position).getmBtn2());
            mBtn2_type3.setOnClickListener(new ListOnButtonListener(position, convertView));
            mBtn3_type3 = (Button)convertView.findViewById(R.id.btn3);
            mBtn3_type3.setText(getItem(position).getmBtn3());
            mBtn3_type3.setOnClickListener(new ListOnButtonListener(position, convertView));
            initButton(position, convertView);
            break;
        }

        return convertView;
    }

    public CharSequence getRingtoneTitle() {
        CharSequence summary = mContext.getString(com.android.internal.R.string.ringtone_unknown);
        Uri ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext,
                RingtoneManagerEx.TYPE_RINGTONE);
        summary = SoundSettings.getTitle(mContext, ringtoneUri, true, RingtoneManagerEx.TYPE_RINGTONE);

        return summary;
    }

    public static String getLauncherCurName(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo r = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (null != r) {
            return r.loadLabel(context.getPackageManager()).toString();
        } else {
            return "";
        }
    }

    public void setFontSizeUserSelect(int progress, View view) {
        String[] mPreviewValues = mContext.getResources().getStringArray(
                R.array.entries_font_size_wine_ex);
        TextView previewText = (TextView)view.findViewById(R.id.text_preveiw);
        switch (progress) {
        case 0:
            mSetFontSize = 1.0f;
            previewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            previewText.setText(mPreviewValues[0]);
            break;
        case 1:
            mSetFontSize = 1.20f;
            previewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
            previewText.setText(mPreviewValues[1]);
            break;
        case 2:
            mSetFontSize = 1.40f;
            previewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            previewText.setText(mPreviewValues[2]);
            break;
        case 3:
            mSetFontSize = 1.40f;
            previewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 29);
            previewText.setText(mPreviewValues[3]);
            if (mIsSeekBarMaxLevel) {
                ShowFontWarningDialog();
            }
            mIsSeekBarMaxLevel = false;
            break;
        default:
            mSetFontSize = 1.40f;
            previewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            previewText.setText(mPreviewValues[2]);
            break;
        }
    }

    private void ShowFontWarningDialog() {
        if (mFontWarnigDialog == null) {
            mFontWarnigDialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.dialog_title_font_size)
                .setMessage(R.string.fontsize_maximum_waning_wine)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFontWarnigDialog.dismiss();
                        mFontWarnigDialog = null;
                    }
                })
                .setOnCancelListener(new OnCancelListener() {            
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mFontWarnigDialog.dismiss();
                        mFontWarnigDialog = null;
                    }
                })
                .show();
        }
    }

    public void setFontSize() {
        if (mCheckUserCheckFontSeekbar != 0) {
            if (mCheckFontProgress == 3) {
                Settings.System.putInt(mContext.getContentResolver(), "sync_large_text", 1);
                mContext.sendBroadcast(new Intent("lge.settings.intent.action.FONT_SIZE"));
            } else {
                Settings.System.putInt(mContext.getContentResolver(), "sync_large_text", 0);
            }
            mCurConfig.fontScale = mSetFontSize;
            try {
                ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void do_ringtonePlay() {
        if (mRingtone != null && !mRingtone.isPlaying()) {
            mRingtone.play();
        }
    }

    public void do_ringtoneStop() {
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
    }

    /**************************
    * RINGER_MODE_SILENT = 0    *
    * RINGER_MODE_VIBRATE = 1 *
    * RINGER_MODE_NORMAL = 2  *
    ***************************/
    public void do_RingerModeChange() {
        Log.d(TAG, "mAudioManager.getRingerMode() : " + mAudioManager.getRingerMode());
        if (sVolumeSeekBar != null && mBtn2_type3 != null) {
            if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                sVolumeSeekBar.setProgress(0);
                mBtn1_type3.setActivated(false);
                mBtn2_type3.setActivated(false);
                mBtn3_type3.setActivated(true);
            } else if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                sVolumeSeekBar.setProgress(0);
                mBtn1_type3.setActivated(false);
                mBtn2_type3.setActivated(true);
                mBtn3_type3.setActivated(false);
            } else {
                sVolumeSeekBar.setProgress(mAudioManager
                        .getStreamVolume(AudioManager.STREAM_RING));
                mBtn1_type3.setActivated(true);
                mBtn2_type3.setActivated(false);
                mBtn3_type3.setActivated(false);
            }
        }
     }

    public void do_WifiModeChange() {
        Log.d(TAG, "do_WifiModeChange ");
        if (mWifi_btn1 == null || mWifi_btn2 == null) {
            Log.d(TAG, "do_WifiModeChange button null");
            return;
        }
        if (mWifiManager != null) {
            boolean isWifiEnabled = mWifiManager.isWifiEnabled();
            if (isWifiEnabled) {
                mWifi_btn1.setActivated(true);
                mWifi_btn2.setActivated(false);
            } else {
                mWifi_btn1.setActivated(false);
                mWifi_btn2.setActivated(true);
            }
        }
    }

    private void initSeekBar(int position, SeekBar seekbar, View view) {
        if (position == SimpleSettingsConstants.INDEX_FONTSIZE) {
            try {
                mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (mCheckFontinit == -1) {
                mCheckFontProgress = changeSizeToProgress(mCurConfig.fontScale);
                seekbar.setProgress(mCheckFontProgress);
                setFontSizeUserSelect(mCheckFontProgress, view);
                mCheckFontinit = mCheckFontProgress;
            } else {
                seekbar.setProgress(mCheckFontinit);
                setFontSizeUserSelect(mCheckFontinit, view);
            }
        } else if (position == SimpleSettingsConstants.INDEX_VOLUME) {
            seekbar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_RING));
            Uri ringtoneUri = RingtoneManagerEx.getActualDefaultRingtoneUri(mContext,
                    RingtoneManagerEx.TYPE_RINGTONE);
            mRingtone = RingtoneManagerEx.getRingtone(mContext, ringtoneUri);
        }
    }

    private void initButton(int position, View convertView) {
        if (position == SimpleSettingsConstants.INDEX_SOUND_PROFILE) {

            int mRingerMode = mAudioManager.getRingerMode();
            mBtn1_type3 = (Button)convertView.findViewById(R.id.btn1);
            mBtn2_type3 = (Button)convertView.findViewById(R.id.btn2);
            mBtn3_type3 = (Button)convertView.findViewById(R.id.btn3);
            switch (mRingerMode) {
            case AudioManager.RINGER_MODE_NORMAL:
                mBtn1_type3.setActivated(true);
                mBtn2_type3.setActivated(false);
                mBtn3_type3.setActivated(false);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                mBtn1_type3.setActivated(false);
                mBtn2_type3.setActivated(true);
                mBtn3_type3.setActivated(false);
                break;
            case AudioManager.RINGER_MODE_SILENT:
                mBtn1_type3.setActivated(false);
                mBtn2_type3.setActivated(false);
                mBtn3_type3.setActivated(true);
                break;
            default:
                break;
            }
        } else if (position == SimpleSettingsConstants.INDEX_WIFI) {
            WifiManager mWifiManager = (WifiManager)convertView.getContext().getSystemService(
                    Context.WIFI_SERVICE);
            if (mWifiManager != null) {
                boolean isWifiEnabled = mWifiManager.isWifiEnabled();
                Button btn1_type2 = (Button)convertView.findViewById(R.id.btn1);
                Button btn2_type2 = (Button)convertView.findViewById(R.id.btn2);

                mWifi_btn1 = btn1_type2;
                mWifi_btn2 = btn2_type2;

                if (isWifiEnabled) {
                    btn1_type2.setActivated(true);
                    btn2_type2.setActivated(false);
                } else {
                    btn1_type2.setActivated(false);
                    btn2_type2.setActivated(true);
                }
            }
        }
    }

    private int changeSizeToProgress(float fontSize) {
        int mProgress = 0;
        if (Float.compare(fontSize, 1.0f) == 0) {
            mProgress = 0;
        } else if (Float.compare(fontSize, 1.20f) == 0) {
            mProgress = 1;
        } else if (Float.compare(fontSize, 1.40f) == 0) {
            int bIsMaximumFont = 0;
            bIsMaximumFont = Settings.System.getInt(mContext.getContentResolver(),
                    "sync_large_text", 0);
            if (bIsMaximumFont == 1) {
                mProgress = 3;
            } else {
                mProgress = 2;
            }
        }
        return mProgress;
    }

    private final class ListOnHoverListener implements View.OnHoverListener {
        private int mPosition;

        public ListOnHoverListener(int position, View view) {
            this.mPosition = position;
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            if (mPosition == SimpleSettingsConstants.INDEX_WIFI) {
                WifiManager mWifiManager = (WifiManager)v.getContext().getSystemService(
                        Context.WIFI_SERVICE);
                if (mWifiManager != null) {
                    boolean isWifiEnabled = mWifiManager.isWifiEnabled();
                    if (isWifiEnabled) {
                        v.setContentDescription(v.getResources().getString(R.string.wifi_settings) + "\n"
                                + v.getResources().getString(R.string.simplesettings_wifi_on));
                    } else {
                        v.setContentDescription(v.getResources().getString(R.string.wifi_settings) + "\n"
                                + v.getResources().getString(R.string.simplesettings_wifi_off));
                    }
                }
            } else if (mPosition == SimpleSettingsConstants.INDEX_SOUND_PROFILE) {
                int mRingerMode = mAudioManager.getRingerMode();
                switch (mRingerMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    v.setContentDescription(v.getResources().getString(R.string.simplesettings_sound_profile) + "\n"
                        + v.getResources().getString(R.string.sound_settings));
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    v.setContentDescription(v.getResources().getString(R.string.simplesettings_sound_profile) + "\n"
                            + v.getResources().getString(R.string.vibrate_title));
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    v.setContentDescription(v.getResources().getString(R.string.simplesettings_sound_profile) + "\n"
                            + v.getResources().getString(R.string.ringtone_silent));
                    break;
                default:
                    break;
                }
            }
            return false;
        }
    }

    private final class SeekbarOnHoverListener implements View.OnHoverListener {
        private int mPosition;

        public SeekbarOnHoverListener(int position, View view, SeekBar seekbar) {
            this.mPosition = position;
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            if (mPosition == SimpleSettingsConstants.INDEX_FONTSIZE) {
                v.setContentDescription(v.getResources().getString(R.string.dialog_title_font_size));
            } else if (mPosition == SimpleSettingsConstants.INDEX_FONTSIZE) {
                v.setContentDescription(v.getResources().getString(R.string.simplesettings_volume));
            }
            return false;
        }
    }

    private final class ListOnButtonListener implements View.OnClickListener {

        private int mPosition;
        private View mView;

        public ListOnButtonListener(int position, View view) {
            this.mPosition = position;
            this.mView = view;
        }

        public void onClick(View v) {
            try {
                if (v.getId() == R.id.btn3) {
                    mAudioManager
                            .setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    sVolumeSeekBar.setProgress(0);
                } else if (v.getId() == R.id.btn2) {
                    mAudioManager
                            .setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    mVibrator.vibrate(PREV_VIBRATE_TIME);
                    sVolumeSeekBar.setProgress(0);
                } else {
                    mAudioManager
                            .setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    sVolumeSeekBar.setProgress(mAudioManager
                            .getStreamVolume(AudioManager.STREAM_RING));
                }
                initButton(mPosition, mView);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist emergency tone setting", e);
            }
        }

    }

    private final class TypeTwobuttonOnButtonListener implements View.OnClickListener {

        private int mPosition;
        private View mView;

        public TypeTwobuttonOnButtonListener(int position, View view) {
            this.mPosition = position;
            this.mView = view;
        }

        public void onClick(View v) {
            if (mPosition == SimpleSettingsConstants.INDEX_WIFI) {
                if (v.getId() == R.id.btn1) {
                    if (mWifiManager != null) {
                        if (!mWifiManager.isWifiEnabled()) {

                            int wifiState = mWifiManager.getWifiState();

                            if ( wifiState == WifiManager.WIFI_STATE_ENABLING
                                || wifiState == WifiManager.WIFI_STATE_DISABLING ) {
                                return;
                            }

                            int wifiApState = mWifiManager.getWifiApState();

                            if ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED)) {

                                //Hotspot Warning Popup for KT, 2014-04-21,ilyong.oh@lgepartner.com
                                if (Utils.isUI_4_1_model(mContext)  &&
                                    Config.getOperator().equals(Config.KT)) {
                                    createHotspotWarningPopupForKT();
                                    return;
                                }
                                mWifiManager.setWifiApEnabled(null, false);
                            }

                            mWifiManager.setWifiEnabled(true);
                            Button btn1_type2 = (Button)mView.findViewById(R.id.btn1);
                            Button btn2_type2 = (Button)mView.findViewById(R.id.btn2);
                            btn1_type2.setActivated(true);
                            btn2_type2.setActivated(false);
                        }
                        // Go into WiFi Settings.
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings",
                                 "com.android.settings.Settings$WifiSettingsActivity");
                        intent.putExtra("isSimpleSettings", true);
                        v.getContext().startActivity(intent);
                    }
                } else if (v.getId() == R.id.btn2) {
                    if (mWifiManager != null) {
                        if (mWifiManager.isWifiEnabled()) {
                            mWifiManager.setWifiEnabled(false);
                            Button btn1_type2 = (Button)mView.findViewById(R.id.btn1);
                            Button btn2_type2 = (Button)mView.findViewById(R.id.btn2);
                            btn1_type2.setActivated(false);
                            btn2_type2.setActivated(true);
                        }
                    }
                }
            } else if (mPosition == SimpleSettingsConstants.INDEX_WALLPAPER) {
                if (v.getId() == R.id.btn1) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(
                            "com.lge.launcher2",
                            "com.lge.launcher2.WallpaperChooser"));
                    v.getContext().startActivity(intent);
                } else if (v.getId() == R.id.btn2) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(
                            "com.android.gallery3d",
                            "com.android.gallery3d.app.Wallpaper"));                  
                    v.getContext().startActivity(intent);
                }

            }
        }

    }

    private final class TypeSeekbarOnSeekBarChangeListener implements OnSeekBarChangeListener {

        private int mPosition;
        private View mView;

        public TypeSeekbarOnSeekBarChangeListener(int position, View view) {
            this.mPosition = position;
            this.mView = view;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            if (mPosition == SimpleSettingsConstants.INDEX_VOLUME
                    && (fromUser || mIsSeletecVolumeBtn)) {
                mAudioManager.setStreamVolume(
                        AudioManager.STREAM_RING, progress, 0);
                do_ringtonePlay();
                mIsSeletecVolumeBtn = false;
            } else if (mPosition == SimpleSettingsConstants.INDEX_FONTSIZE) {
                if (fromUser) {
                    mCheckFontProgress = progress;
                    mCheckUserCheckFontSeekbar = 1;
                    if (progress == seekBar.getMax()) {
                        mIsSeekBarMaxLevel = true;
                    }
                    setFontSizeUserSelect(progress, mView);
                }
                String[] mPreviewValues = mContext.getResources().getStringArray(
                        R.array.entries_font_size_wine_ex);
                seekBar.setContentDescription(mPreviewValues[progress]);
                do_ringtoneStop();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mPosition == SimpleSettingsConstants.INDEX_VOLUME) {
                mHandler.removeCallbacks(mRunnable);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPosition == SimpleSettingsConstants.INDEX_VOLUME) {
                mHandler.postDelayed(mRunnable, 3000);
            }
        }        
    }

    private final class TypeTextButtonOnButtonListener implements View.OnClickListener {

        private int mPosition;

        public TypeTextButtonOnButtonListener(int position) {
            this.mPosition = position;
        }

        public void onClick(View v) {
            if (mPosition == SimpleSettingsConstants.INDEX_RINGTONE) {
                Intent intent = new Intent();
                intent.putExtra(RingtoneManagerEx.EXTRA_RINGTONE_TYPE,
                        RingtoneManagerEx.TYPE_RINGTONE);
                intent.setComponent(new ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings$RingtonePickerActivity"));
                v.getContext().startActivity(intent);
            } else if (mPosition == SimpleSettingsConstants.INDEX_CHANGE_HOMEMODE) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "com.lge.homeselector",
                        "com.lge.homeselector.HomeSelector"));
                v.getContext().startActivity(intent);
            }
        }
    }

    // Hotspot Warning Popup for KT, 2014-04-21, ilyong.oh@lgepartner.com
    private void createHotspotWarningPopupForKT() {

        if (mWifiManager.getWifiApState() != WifiManager.WIFI_AP_STATE_ENABLED) {
            if (!mWifiManager.setWifiEnabled(true)) {
                // Error 
                Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        AlertDialog mKT_Hotspot_warning_dialog = new AlertDialog.Builder(mContext)
            .setTitle(R.string.notification_volume_title)
            //.setIconAttribute(android.R.attr.alertDialogIcon) //Not Used
            .setMessage(R.string.sp_hotspot_incompatible_wifi_wanning_NORMAL)
            .setCancelable(false)
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) { 
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                    dialog.dismiss();
                    return;
                }
            })
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Disable tethering if enabling Wifi
                    if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLING ||
                        mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                        mWifiManager.setWifiApEnabled(null, false);
                    }

                    if (mWifiManager.setWifiEnabled(true)) {                        
                        Log.d(TAG, "WiFi Enable OK");
                        // Go into WiFi Settings.
                        Intent intent = new Intent();
                        intent.setClassName("com.android.settings",
                                 "com.android.settings.Settings$WifiSettingsActivity");
                        intent.putExtra("isSimpleSettings", true);
                        mContext.startActivity(intent);
                    } else {
                        // Error
                        Toast.makeText(mContext, R.string.wifi_error, Toast.LENGTH_SHORT).show();
                    }
 
                    dialog.dismiss();
                    return;
                }
            }).create();
        mKT_Hotspot_warning_dialog.show();
    }

    OnFocusChangeListener mOnFocusChangeHandler = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    do_ringtoneStop();
                }
            }
        };
}
