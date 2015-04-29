package com.android.settings;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.os.Environment;
import com.android.settings.R;
import android.os.SystemProperties;
import android.widget.ImageView;
import android.os.Build;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.media.AudioManager;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.res.AssetFileDescriptor;
import android.app.ActivityManager.RunningTaskInfo;

public class MPCSPowerTonePicker extends ListActivity {

    static final String TAG = "MPCSPowerTonePicker";
    private static final String SETTING_STYLE = "settings_style";
    Button mCancelButton;
    Button mOKButton;
    Context mContext;
    private ListView listView;
    private int currentPowerTonePosition;
    private int currentPosition = 0;
    MediaPlayer mPlayer = new MediaPlayer();
    ArrayList<String> PowerToneitems;
    public static final String MPCS_POWERUP_TONE = "mpcs_powerup_tone";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.xml.ringtone_picker);

        getActionBar().setIcon(R.drawable.power_tone);
        setTitle(R.string.sp_sound_power_up_tone);

        initPowerToneUI();

        updatePowerToneList();

        mCancelButton = (Button)findViewById(R.id.cancel_button);
        mOKButton = (Button)findViewById(R.id.ok_button);

        mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPlayer.stop();
                MPCSPowerTonePicker.this.finish();
            }
        });

        mOKButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d("MPCSPowerTonePicker", "Save sound index : " + currentPowerTonePosition);
                try {
                    Log.d("MPCSPowerTonePicker", "insert!!");
                    writePowerOnTone(currentPowerTonePosition);
                } catch (Exception e) {
                    Log.w(TAG, "Exception");
                }
                MPCSPowerTonePicker.this.finish();
            }
        });

        setVolumeControlStream(AudioManager.STREAM_SYSTEM);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        currentPowerTonePosition = position;
        mPlayer.stop();
        mPlayer.release();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(android.media.AudioManager.STREAM_SYSTEM);
        switch (position) {
        case 0:
            try {
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
            break;
        case 1:
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.poweron_mpcs1);
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                        afd.getLength());
                afd.close();
                mPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                mPlayer.prepare();
                mPlayer.start();
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
            break;
        case 2:
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.poweron_mpcs2);
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                        afd.getLength());
                afd.close();
                mPlayer.setAudioStreamType(android.media.AudioManager.STREAM_SYSTEM);
                mPlayer.prepare();
                mPlayer.start();
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
            break;
        case 3:
            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.poweron_mpcs3);
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                        afd.getLength());
                afd.close();
                mPlayer.setAudioStreamType(android.media.AudioManager.STREAM_SYSTEM);
                mPlayer.prepare();
                mPlayer.start();
            } catch (Exception e) {
                Log.w(TAG, "Exception");
            }
            break;
        default:
            mPlayer.stop();
            break;
        }
    }

    private void initPowerToneUI() {

        PowerToneitems = new ArrayList<String>();

        PowerToneitems.add(getString(R.string.ringtone_silent));
        PowerToneitems.add(getString(R.string.sp_powertone_mpcs1));
        PowerToneitems.add(getString(R.string.sp_powertone_mpcs2));
        PowerToneitems.add(getString(R.string.sp_powertone_mpcs3));

        ArrayAdapter<String> Adapter;
        Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                PowerToneitems);

        setListAdapter(Adapter);

        listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        currentPosition = Utils.getPowerTonePosition();
        Log.d("MPCSPowerTonePicker", "4 = " + String.valueOf(currentPosition));
        listView.setItemChecked(currentPosition, true);

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;

        }
    }

    private void updatePowerToneList() {
    }

    private void onPrepareUI() {
        if (listView != null) {
            listView.setItemChecked(Utils.getPowerTonePosition() + 1, true);
            listView.setSelection(Utils.getPowerTonePosition() + 1);
        }
    }

    private void deletePowerSoundFolder(File folder) {
        File[] childFileList = folder.listFiles();
        if (childFileList != null) {
            for (File childFile : childFileList) {
                Log.d("MPCSPowerTonePicker", "[deletePowerSoundFolder] for");
                childFile.delete();
            }
        }
        folder.delete();
    }

    private void writePowerOnTone(int index) {
        String dir = "data/data/com.android.settings/";
        String file_name = String.valueOf(index);
        File folder = new File(dir + "powersound");

        Log.d("MPCSPowerTonePicker", "[writePowerOnTone] index hhh : " + index);

        folder.mkdirs();
        deletePowerSoundFolder(folder);
        folder.mkdirs();

        folder.setExecutable(true, false);
        folder.setWritable(true, true);
        folder.setReadable(true, false);
        Log.d("MPCSPowerTonePicker", "[writePowerOnTone] mkdir");
        if (0 != index) {
            Log.d("MPCSPowerTonePicker", "[writePowerOnTone] mkdir2");
            File outfile = new File(dir + "powersound/" + file_name);
            Log.d("MPCSPowerTonePicker", "[writePowerOnTone] mkdir3");
            try {
                Log.d("MPCSPowerTonePicker", "[writePowerOnTone] createnew file");
                boolean abc = outfile.createNewFile();
                outfile.setExecutable(true, false);
                outfile.setWritable(true, true);
                outfile.setReadable(true, false);
                Log.d("MPCSPowerTonePicker", "[writePowerOnTone] createnew file : " + abc);
            } catch (IOException e) {
                Log.i(TAG, "IOException");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        switch (item.getItemId()) {
        case android.R.id.home:
            int settingStyle = Settings.System.getInt(getContentResolver(), SETTING_STYLE, 0);
            ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> info = am.getRunningTasks(1);
/*            try {
                ComponentName topActivity = info.get(0).topActivity;
                String topActivityClassName = topActivity.getClassName();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } */
            String baseActivityClassName = info.get(0).baseActivity.getClassName();

            if (settingStyle == 1 && Utils.supportEasySettings(this)) {

                if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName) ||
                        "com.android.settings.Settings".equals(baseActivityClassName)) {
                    Log.d("soosin", "onBackPressed");
                    onBackPressed();
                    return true;
                } else {
                    Log.d("soosin", "intent action - SOUND_SETTINGS");
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.setAction("com.android.settings.SOUND_SETTINGS");
                    startActivity(i);
                    finish();
                    return true;
                }
            }
            else if ("com.android.settings.Settings".equals(baseActivityClassName)) {
                Log.d("soosin", "onBackPressed");
                onBackPressed();
                return true;
            }
            else {
                Log.d("soosin", "intent action - SOUND_SETTINGS");
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.setAction("com.android.settings.SOUND_SETTINGS");
                startActivity(i);
                finish();
                return true;
            }
        default:
            return false;
        }
    }

}