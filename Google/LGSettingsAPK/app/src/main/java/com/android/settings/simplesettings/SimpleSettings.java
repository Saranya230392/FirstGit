package com.android.settings.simplesettings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;

import java.util.ArrayList;

public class SimpleSettings extends Activity implements OnClickListener {
    private static final String TAG = "SimpleSettings";
    AudioManager mAudioManager;
    // [seungyeop.yeom][2014-09-03] add Receiver for event of end key
    private static final String BEFORE_KELL_APP_BY_FWK = "com.lge.android.intent.action.BEFORE_KILL_APP_BY_FWK";

    ListView mList;
    SimpleSettingsAdapter mAdapter;
    ArrayList<ListItemData> mArrData;
    private final Configuration mCurConfig = new Configuration();

    private BroadcastReceiver mRingerModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action : " + action);
            if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                mAdapter.do_RingerModeChange();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                mAdapter.do_WifiModeChange();
            }
            if (AudioManager.VOLUME_CHANGED_ACTION.equals(action)) {
                SimpleSettingsAdapter.sVolumeSeekBar.setProgress(mAudioManager
                        .getStreamVolume(AudioManager.STREAM_RING));
            }
        }
    };

    // [START][seungyeop.yeom][2014-09-03] add Receiver for event of end key
    private BroadcastReceiver mSimpleSettingReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BEFORE_KELL_APP_BY_FWK.equals(intent.getAction())) {
                Log.d("jw", "BEFORE_KELL_APP_BY_FWK");
                finish();

            }

        }

    };
    // [END]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_settings);
        mAudioManager = (AudioManager)this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        // [seungyeop.yeom][2014-09-03] add Receiver for event of end key
        IntentFilter filter = new IntentFilter();
        filter.addAction(BEFORE_KELL_APP_BY_FWK);
        registerReceiver(mSimpleSettingReceiver, filter);

        setData();

        mAdapter = new SimpleSettingsAdapter(this, mArrData);

        mList = (ListView)findViewById(R.id.list);
        mList.setAdapter(mAdapter);
        mList.setItemsCanFocus(true);
        mList.setDivider(null);

        Button bSettings = (Button)findViewById(R.id.btn_bottom_runsettings);
        bSettings.setOnClickListener(this);
    }

    private void setData() {
        mArrData = new ArrayList<ListItemData>();
        mArrData.add(new ListItemData(3, getResources().getString(R.string.simplesettings_sound_profile),
                getResources().getString(R.string.sound_settings),
                getResources().getString(R.string.vibrate_title),
                getResources().getString(R.string.ringtone_silent), null));
        mArrData.add(new ListItemData(0, getResources().getString(R.string.simplesettings_volume),
                "", "", "", new TypeSeekbarData(getResources().getString(R.string.simplesettings_volume),
                        mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                        TextView.GONE)));
        mArrData.add(new ListItemData(1, getResources().getString(R.string.ringtone_title_ex),
                "", "", "", null));
        mArrData.add(new ListItemData(4, getResources().getString(R.string.dialog_title_font_size),
                "", "", "", new TypeSeekbarData(getResources().getString(
                        R.string.dialog_title_font_size), 3,
                        TextView.VISIBLE)));
        mArrData.add(new ListItemData(2,
                getResources().getString(R.string.wallpaper_settings_title),
                getResources().getString(R.string.simplesettings_setwallpaper_NORMAL),
                getResources().getString(R.string.simplesettings_wallpaper_gallery), "", null));
        mArrData.add(new ListItemData(2, getResources().getString(R.string.wifi_settings),
                getResources().getString(R.string.simplesettings_wifi_on),
                getResources().getString(R.string.simplesettings_wifi_off), "", null));
        mArrData.add(new ListItemData(1, getResources().getString(R.string.simplesettings_change_homemode),
                "", "", "", null));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_bottom_runsettings:
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_SETTINGS);
            startActivity(intent);
            break;
        default:
            break;
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        mAdapter.setFontSize();
        mAdapter.mCheckFontinit = -1;
        mAdapter.do_ringtoneStop();
        if (mRingerModeReceiver != null) {
            this.unregisterReceiver(mRingerModeReceiver);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
        this.registerReceiver(mRingerModeReceiver, filter);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mAdapter.mCheckFontinit = mAdapter.mCheckFontProgress;
        mCurConfig.updateFrom(newConfig);
        mAdapter.do_ringtoneStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.simple_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_SETTINGS);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // [seungyeop.yeom][2014-09-03] add Receiver for event of end key
        unregisterReceiver(mSimpleSettingReceiver);
        super.onDestroy();
    }
}
