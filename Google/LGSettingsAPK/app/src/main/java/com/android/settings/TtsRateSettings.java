package com.android.settings;

import com.android.settings.R;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

//import com.lge.provider.SettingsEx;
import android.util.Log;
import android.app.ActionBar;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import static android.provider.Settings.Secure.TTS_DEFAULT_RATE;
import static android.provider.Settings.Secure.TTS_DEFAULT_SYNTH;
import android.provider.Settings.SettingNotFoundException;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TtsEngines;
import android.content.ContentResolver;
import android.content.ActivityNotFoundException;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

import java.util.Locale;

import android.text.TextUtils;

public class TtsRateSettings extends Activity implements OnClickListener, OnItemClickListener {

    //private static final int PREVIEW = Menu.FIRST;

    private static final String TAG = "TtsRateSettings";

    private ListView mListView;
    private Button mCancelBtn;
    private Button mOklBtn;

    private static final int TTS_RATE = 1;
    private static final int TTS_PITCH = 2;
    private static final int GET_SAMPLE_TEXT = 1983;
    private int mDefaultRate = TextToSpeech.Engine.DEFAULT_RATE;
    private static final String[] DEFAULT_TTSRATE_ENTRY = { "60", "80", "100", "150", "200" };
    private static final long[] DEFAULT_TTSRATE_VALUE = { 60, 80, 100, 150, 200 };
    private static final long[] DEFAULT_TTSPITCH_VALUE = { 10, 55, 100, 145, 190 };
    private static final long[] DEFAULT_LGTTSPITCH_VALUE = { 10, 80, 100, 120, 190 };
    private TextToSpeech mTts = null;
    private TtsEngines mEnginesHelper = null;
    private AudioManager mAudioManager;
    private int mRate = TextToSpeech.Engine.DEFAULT_RATE;
    //private String[] mstr;
    private boolean isOkbtn = false;
    private boolean mRatOKbtn = false;
    private int mCurremtMode;
    private static final String LG_TTS = "com.lge.tts.sfplus";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.tts_rate_settings);

        // actionbar status settings
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        String mMode = intent.getStringExtra("mode");
        Log.d(TAG, "Current mode : " + mMode);
        if ("rate".equals(mMode)) {
            mCurremtMode = TTS_RATE;
        } else if ("pitch".equals(mMode)) {
            mCurremtMode = TTS_PITCH;
            this.setTitle(R.string.tts_pitch_change_title);
        }

        mListView = (ListView)findViewById(android.R.id.list);
        mListView.setAdapter(initAdapter());
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        initSettings();

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setTag(false); // click, long click event boolean
        mCancelBtn = (Button)findViewById(R.id.cancel_button);
        mOklBtn = (Button)findViewById(R.id.ok_button);

        mAudioManager = (AudioManager)getSystemService(this.getApplicationContext().AUDIO_SERVICE);
        //Listener reg

        mCancelBtn.setOnClickListener(this);
        mOklBtn.setOnClickListener(this);
        mListView.setOnItemClickListener(this);

    }

    private void initSettings() {
        final ContentResolver resolver = getContentResolver();
        //mstr = new String[mListView.getCount()];

        mTts = new TextToSpeech(getApplicationContext(), mInitListener);
        mEnginesHelper = new TtsEngines(getApplicationContext());
        if (mCurremtMode == TTS_RATE) {
            try {
                mDefaultRate = Settings.Secure.getInt(resolver,
                        TTS_DEFAULT_RATE);
            } catch (SettingNotFoundException e) {
                // Default rate setting not found, initialize it
                mDefaultRate = TextToSpeech.Engine.DEFAULT_RATE;
            }

            Log.d(TAG, "initSettings count : " + mListView.getCount()
                    + " mDefaultRate : " + mDefaultRate);

            for (int i = 0; i < mListView.getCount(); i++) {
                if (String.valueOf(mDefaultRate).contentEquals(
                        DEFAULT_TTSRATE_ENTRY[i])) {
                    mListView.setItemChecked(i, true);
                }
            }
        } else {
            mDefaultRate = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.TTS_DEFAULT_PITCH,
                    TextToSpeech.Engine.DEFAULT_PITCH);
            int arrayId = R.array.tts_pitch_values;
            if (mTts.getCurrentEngine().equals(LG_TTS)) {
                arrayId = R.array.lg_tts_pitch_values;
            }
            String[] mPitchValues = getResources().getStringArray(arrayId);
            for (int i = 0; i < mListView.getCount(); i++) {
                if (String.valueOf(mDefaultRate).equals(mPitchValues[i])) {
                    mListView.setItemChecked(i, true);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        if (mCurremtMode == TTS_RATE) {
            mRate = (int)DEFAULT_TTSRATE_VALUE[position];
        } else {
            if (mTts.getCurrentEngine().equals(LG_TTS)) {
                mRate = (int)DEFAULT_LGTTSPITCH_VALUE[position];
            } else {
                mRate = (int)DEFAULT_TTSPITCH_VALUE[position];
            }
        }
        
        if (mTts == null) {
            Log.d(TAG, "[onClick] mTts is null");
            return;
        }

        Log.d(TAG, "onListItemClick position : " + position + " value : " + mRate);

        try {
            //Settings.Secure.putInt(getContentResolver(), TTS_DEFAULT_RATE, mRate);
            if (mCurremtMode == TTS_RATE) {
                mTts.setSpeechRate(mRate / 100.0f);
            } else {
                mTts.setPitch(mRate / 100.0f);
                mTts.setSpeechRate(TextToSpeech.Engine.DEFAULT_RATE / 100.0f);
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist default TTS rate setting", e);
        }

        mRatOKbtn = true;
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        getSampleText();

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick  mRate : " + mRate);
        if (mTts == null) {
            Log.d(TAG, "[onClick] mTts is null");
            return;
        }

        if (v.equals(mCancelBtn)) {
            try {
                if (mCurremtMode == TTS_RATE) {
                    Settings.Secure.putInt(getContentResolver(), TTS_DEFAULT_RATE, mDefaultRate);
                    mTts.setSpeechRate(mDefaultRate / 100.0f);
                } else {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH, mDefaultRate);
                    mTts.setPitch(mDefaultRate / 100.0f);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist default TTS rate setting", e);
            }
            if (mTts.isSpeaking()) {
                Log.d(TAG, "isSpeaking : ");
                mTts.stop();
            }
        }
        else if (v.equals(mOklBtn)) {
            try {
                isOkbtn = true;
                if (mRatOKbtn) {
                    Log.d(TAG, "onClick  mRatOKbtn : " + mRatOKbtn);
                    if (mCurremtMode == TTS_RATE) {
                        Settings.Secure.putInt(getContentResolver(), TTS_DEFAULT_RATE, mRate);
                        mTts.setSpeechRate(mRate / 100.0f);
                    } else {
                        Settings.Secure.putInt(getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH, mRate);
                        mTts.setPitch(mRate / 100.0f);
                    }
                    mRatOKbtn = false;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist default TTS rate setting", e);
            }
            if (mTts.isSpeaking()) {
                Log.d(TAG, "isSpeaking : ");
                mTts.stop();
            }
        }
        finish();
    }

    /*
        // Initialize the state of radiobutton
        private void initToggles() {
        /*
            mFirstScreenOffEffect.setChecked(false);
            mSecondScreenOffEffect.setChecked(false);
            mThirdScreenOffEffect.setChecked(false);
    */

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy isOkbtn : " + isOkbtn);
        if (!isOkbtn) {
            try {
                if (mCurremtMode == TTS_RATE) {
                    Settings.Secure.putInt(getContentResolver(), TTS_DEFAULT_RATE, mDefaultRate);
                    if (mTts != null) {
                        mTts.setSpeechRate(mDefaultRate / 100.0f);
                    }
                } else {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH, mDefaultRate);
                    if (mTts != null) {
                        mTts.setPitch(mDefaultRate / 100.0f);
                    }
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist default TTS rate setting", e);
            }
        }
        isOkbtn = false;

        if (mTts != null) {
            mTts.shutdown();
            mTts = null;
        }

        mAudioManager.abandonAudioFocus(mAudioFocusListener);
    }

    // The creation of the Menu button
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, PREVIEW, Menu.NONE, R.string.screen_off_effect_preview_button);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setEnabled(true);

        MenuItem ok = menu.add(0, PREVIEW, Menu.NONE, R.string.screen_off_effect_preview_button);
        ok.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        ok.setEnabled(true);

        return true;
    }
    */

    // Operation of the Menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "1111 onActivityResult");
        if (requestCode == GET_SAMPLE_TEXT) {
            onSampleTextReceived(resultCode, data);
        }
    }

    private ArrayAdapter<CharSequence> initAdapter() {
        int arrayId;
        if (mCurremtMode == TTS_RATE) {
            arrayId = R.array.tts_rate_entries;
        } else {
            arrayId = R.array.tts_pitch_entries;
        }
        return ArrayAdapter.createFromResource(this, arrayId,
                android.R.layout.simple_list_item_single_choice);
    }

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                break;

            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                break;

            default:
                Log.d(TAG, "Unknown audio focus change code");
            }
        }
    };

    private void getSampleText() {
        String currentEngine = mTts.getCurrentEngine();
        String targetCountry = Config.getCountry();

        Log.d(TAG, "getSampleText() " + currentEngine + " targetCountry : " + mEnginesHelper);

        if (TextUtils.isEmpty(currentEngine)) {
            currentEngine = mTts.getDefaultEngine();
        }

        maybeUpdateTtsLanguage(currentEngine);
        Locale currentLocale = mTts.getLanguage();

        Intent intent = new Intent(TextToSpeech.Engine.ACTION_GET_SAMPLE_TEXT);

        intent.putExtra("language", currentLocale.getLanguage());
        intent.putExtra("country", currentLocale.getCountry());
        intent.putExtra("variant", currentLocale.getVariant());

        Log.d(TAG, "getLanguage = " + currentLocale.getLanguage());
        Log.d(TAG, "getCountry = " + currentLocale.getCountry());
        Log.d(TAG, "getVariant = " + currentLocale.getVariant());

        if ("".equals(currentLocale.getLanguage())) {
        	Locale newLocale = checkTTSLocaleForVariation(currentEngine);
            if (newLocale != null) {
                intent.putExtra("language", newLocale.getLanguage());
                intent.putExtra("country", newLocale.getCountry());
                intent.putExtra("variant", newLocale.getVariant());
            }
        }
            
        intent.setPackage(currentEngine);

        try {
            startActivityForResult(intent, GET_SAMPLE_TEXT);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Failed to get sample text, no activity found for " + intent + ")");
        }
    }

    private Locale checkTTSLocaleForVariation(String currentEngine) {
    	if (mEnginesHelper == null) {
    		Log.d(TAG, "checkLocaleForVariation():mEnginesHelper is null");
    	}
    	
    	String targetCountry = Config.getCountry();
        Locale engineLocale = mEnginesHelper.getLocalePrefForEngine(currentEngine);
        Locale result = null;
        if ((engineLocale.toString().equals("en_US") || engineLocale.toString().equals("ko_KR")) 
        		&& "JP".equalsIgnoreCase(targetCountry)) {
        	result = mEnginesHelper.parseLocaleString("eng-USA-FEMALE2");
        } else if (mTts != null && "com.svox.pico".equals(mTts.getDefaultEngine())
                && engineLocale.toString().equals("ko_KR")) {
        	result = mEnginesHelper.parseLocaleString("deu-DEU");
        } else {
        	result = engineLocale;
        }
        return result;
    }
    
    private void maybeUpdateTtsLanguage(String currentEngine) {
    	if (currentEngine == null || mTts == null) {
    		Log.d(TAG, "maybeUpdateTtsLanguage() : currentEngine is " 
    				+ currentEngine + " mTts is " + mTts);
    		return;
    	}
    	
        Locale newLocale = checkTTSLocaleForVariation(currentEngine);
        
        if (newLocale != null) {
            Locale ttsLocale = mTts.getLanguage();

            if (!newLocale.equals(ttsLocale)) {
                mTts.setLanguage(newLocale);
            }
        }
    }

    private void onSampleTextReceived(int resultCode, Intent data) {
        String sample = getDefaultSampleString();

        if (resultCode == TextToSpeech.LANG_AVAILABLE && data != null) {
            if (data != null && data.getStringExtra("sampleText") != null) {
                sample = data.getStringExtra("sampleText");
            }
        } else {
            Log.d(TAG, "Using default sample text :" + sample);
        }

        Log.d(TAG, "resultCode = " + resultCode + ", sample = " + sample);
        if (sample != null && mTts != null) {
            mTts.speak(sample, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            Log.e(TAG, "Did not have a sample string for the requested language");
        }
    }

    private String getDefaultSampleString() {
        if (mTts != null) {
            Log.d(TAG, "mTts.getLanguage() = " + mTts.getLanguage());
        }

        if (mTts != null && mTts.getLanguage() != null) {
            final String currentLang = mTts.getLanguage().getISO3Language();
            String[] strings = this.getResources().getStringArray(
                    R.array.tts_demo_strings);
            String[] langs = this.getResources().getStringArray(
                    R.array.tts_demo_string_langs);

            for (int i = 0; i < strings.length; ++i) {
                if (langs[i].equals(currentLang)) {
                    return strings[i];
                }
            }
        }
        return null;
    }

    private final TextToSpeech.OnInitListener mInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            //            onInitEngine(status);
        }
    };
}
