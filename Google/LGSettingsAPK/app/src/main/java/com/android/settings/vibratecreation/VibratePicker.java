package com.android.settings.vibratecreation;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.ByteLengthFilter;
import com.android.settings.lgesetting.Config.Config;

import android.os.SystemProperties;

public class VibratePicker extends PreferenceActivity implements OnClickListener,
        OnItemClickListener, OnItemLongClickListener {
    /** Called when the activity is first created. */
    private static final String TAG = "VibratePicker";
    private static final String VIBRATE_TITLE_SET = "vibrate_title";
    private static final int REPEAT = -1; //0

    private static final int RENAME = 0;
    private static final int DELETE = 1;

    private static final int MAX_VIBRATION = 100;
    private static final int MAX_LEN = 15;

    private static final int RENAME_SIM_TYPE_VIBRATION = 2;

    private static final int RENAME_SIM1_INDEX = 0;
    private static final int RENAME_SIM2_INDEX = 1;
    private static final int RENAME_SIM3_INDEX = 2;

    private Context mContext;
    private Vibrator mVibrator;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> mPatternName;
    private boolean isDualSim = false;
    private boolean isTripleSim = false;
    private int mParent_Type = 1;
    //private ArrayList<String> mLgVibrateType;
    //private ArrayList<String> mUserVibrateType;

    private ListView mListView;
    private String mDefaultVibrate;
    private String mSelectVibrate;
    private String mOriginalDefaultVibrate;
    private String mOriginalDefaultPattern;
    private String mReNameVibrate = "empty";
    private String mSelectPattern;
    private Button mCancelBtn;
    private Button mOklBtn;
    private int mRemovePosition;
    private VibratePatternInfo mVibratePatternInfo;
    private Toast mToast;

    private InputMethodManager imm;
    private AlertDialog mSelectDialog = null;

    private AudioManager mAudioManager;

    VibrateAdapter mAdapter = new VibrateAdapter() {

        @Override
        protected View getHeaderView(String caption, int index, View convertView,
                ViewGroup parent) {
            // TODO Auto-generated method stub
            Log.d(TAG, "getHeaderView");
            LinearLayout mLinearLayout = (LinearLayout)getLayoutInflater().inflate(
                    R.layout.vibrate_caption, null);
            TextView result = (TextView)mLinearLayout.getChildAt(0);
            if (result != null) {
                result.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        return true;
                    }
                });
                result.setText(caption);
            }
            return mLinearLayout;
        }
    };

    private void do_setInitTitle(String mUserTitle) {
        isDualSim = Utils.isMultiSimEnabled(); // Dual Sim status check
        isTripleSim = Utils.isTripleSimEnabled();
        if (mParent_Type == VibratePatternInfo.INCOMING_CALL_SIM1 ||
                mParent_Type == VibratePatternInfo.INCOMING_CALL_SIM2 ||
                mParent_Type == VibratePatternInfo.INCOMING_CALL_SIM3) {
            if (true == isTripleSim) {
                if (VibratePatternInfo.INCOMING_CALL_SIM1 == mParent_Type) {
                        getActionBar().setTitle(R.string.sp_vibrate_Call_SIM1_NORMAL);
                        this.setTitle(R.string.sp_vibrate_Call_SIM1_NORMAL);
                    if (mUserTitle == null) {
                        if (Utils.isUI_4_1_model(mContext)) {
                            String title = Utils.do_getSoundSimName(mContext,
                                    RENAME_SIM_TYPE_VIBRATION,
                                    RENAME_SIM1_INDEX);
                            getActionBar().setTitle(title);
                            this.setTitle(title);
                        } else {
                            getActionBar().setTitle(R.string.sim1_incoming_call_vibration);
                            this.setTitle(R.string.sim1_incoming_call_vibration);
                        }
                    }
                    else {
                        getActionBar().setTitle(mUserTitle);
                        this.setTitle(mUserTitle);
                    }
                }
                else if (VibratePatternInfo.INCOMING_CALL_SIM2 == mParent_Type) {
                    if (Utils.isSPRModel()) {
                        getActionBar().setTitle(R.string.sp_vibrate_Call_SIM2_NORMAL);
                        this.setTitle(R.string.sp_vibrate_Call_SIM2_NORMAL);
                    }
                    else {
                        if (mUserTitle == null) {
                            if (Utils.isUI_4_1_model(mContext)) {
                                String title = Utils.do_getSoundSimName(mContext,
                                        RENAME_SIM_TYPE_VIBRATION,
                                        RENAME_SIM2_INDEX);
                                getActionBar().setTitle(title);
                                this.setTitle(title);
                            } else {
                                getActionBar().setTitle(R.string.sim2_incoming_call_vibration);
                                this.setTitle(R.string.sim2_incoming_call_vibration);
                            }
                        }
                        else {
                            getActionBar().setTitle(mUserTitle);
                            this.setTitle(mUserTitle);
                        }
                    }
                }
                else if (VibratePatternInfo.INCOMING_CALL_SIM3 == mParent_Type) {
                    if (Utils.isSPRModel()) {
                        getActionBar().setTitle(R.string.sp_vibrate_Call_SIM3_NORMAL);
                        this.setTitle(R.string.sp_vibrate_Call_SIM3_NORMAL);
                    }
                    else {
                        if (mUserTitle == null) {
                            if (Utils.isUI_4_1_model(mContext)) {
                                String title = Utils.do_getSoundSimName(mContext,
                                        RENAME_SIM_TYPE_VIBRATION,
                                        RENAME_SIM3_INDEX);
                                getActionBar().setTitle(title);
                                this.setTitle(title);
                            } else {
                                getActionBar().setTitle(R.string.sim3_incoming_call_vibration);
                                this.setTitle(R.string.sim3_incoming_call_vibration);
                            }
                        }
                        else {
                            getActionBar().setTitle(mUserTitle);
                            this.setTitle(mUserTitle);
                        }
                    }
                }
            }
            else if (false == isDualSim) {
                if (Utils.isSPRModel()) {
                    getActionBar().setTitle(R.string.sp_vibrate_Call_NORMAL);
                    this.setTitle(R.string.sp_vibrate_Call_NORMAL);
                }
                else {
                    if ( mUserTitle == null ) {
                        if (Utils.isUI_4_1_model(mContext)) {
                            getActionBar().setTitle(R.string.sp_quiet_mode_vibration_type);
                            this.setTitle(R.string.sp_quiet_mode_vibration_type);
                        } else {
                            getActionBar().setTitle(R.string.sp_quiet_mode_incoming_call_vibration);
                            this.setTitle(R.string.sp_quiet_mode_incoming_call_vibration);
                        }
                    } else {
                        getActionBar().setTitle(mUserTitle);
                        this.setTitle(mUserTitle);
                    }
                }
            } else {
                if (VibratePatternInfo.INCOMING_CALL_SIM1 == mParent_Type) {
                    if (Utils.isSPRModel()) {
                        getActionBar().setTitle(R.string.sp_vibrate_Call_SIM1_NORMAL);
                        this.setTitle(R.string.sp_vibrate_Call_SIM1_NORMAL);
                    }
                    else {
                        if (mUserTitle == null) {
                            if (Utils.isUI_4_1_model(mContext)) {
                                String title = Utils.do_getSoundSimName(mContext,
                                        RENAME_SIM_TYPE_VIBRATION,
                                        RENAME_SIM1_INDEX);
                                getActionBar().setTitle(title);
                                this.setTitle(title);
                            } else {
                                getActionBar().setTitle(R.string.sim1_incoming_call_vibration);
                                this.setTitle(R.string.sim1_incoming_call_vibration);
                            }
                        }
                        else {
                            getActionBar().setTitle(mUserTitle);
                            this.setTitle(mUserTitle);
                        }
                    }
                }
                else if (VibratePatternInfo.INCOMING_CALL_SIM2 == mParent_Type) {
                    if (Utils.isSPRModel()) {
                        getActionBar().setTitle(R.string.sp_vibrate_Call_SIM2_NORMAL);
                        this.setTitle(R.string.sp_vibrate_Call_SIM2_NORMAL);
                    }
                    else {
                        if (mUserTitle == null) {
                            if (Utils.isUI_4_1_model(mContext)) {
                                String title = Utils.do_getSoundSimName(mContext,
                                        RENAME_SIM_TYPE_VIBRATION,
                                        RENAME_SIM2_INDEX);
                                getActionBar().setTitle(title);
                                this.setTitle(title);
                            } else {
                                getActionBar().setTitle(R.string.sim2_incoming_call_vibration);
                                this.setTitle(R.string.sim2_incoming_call_vibration);
                            }
                        }                        
                        else {
                            getActionBar().setTitle(mUserTitle);
                            this.setTitle(mUserTitle);
                        }
                    }
                }
            }
        } else if (mParent_Type == VibratePatternInfo.MESSAGE) {
            if (mUserTitle == null) {
                getActionBar().setTitle(R.string.sp_vibrate_Message_NORMAL);
                this.setTitle(R.string.sp_vibrate_Message_NORMAL);
            }
            else {
                getActionBar().setTitle(mUserTitle);
                this.setTitle(mUserTitle);
            }
        } else if (mParent_Type == VibratePatternInfo.EMAIL) {
            if (mUserTitle == null) {
                getActionBar().setTitle(R.string.sp_vibrate_Email_NORMAL);
                this.setTitle(R.string.sp_vibrate_Email_NORMAL);
            }
            else {
                getActionBar().setTitle(mUserTitle);
                this.setTitle(mUserTitle);
            }
        } else if (mParent_Type == VibratePatternInfo.ALARM) {
            if (mUserTitle == null) {
                getActionBar().setTitle(R.string.sp_vibrate_Alarm_NORMAL);
                this.setTitle(R.string.sp_vibrate_Alarm_NORMAL);
            }
            else {
                getActionBar().setTitle(mUserTitle);
                this.setTitle(mUserTitle);
            }
        } else if (mParent_Type == VibratePatternInfo.CALENDAR) {
            if (mUserTitle == null) {
                getActionBar().setTitle(R.string.sp_vibrate_Calendar_NORMAL);
                this.setTitle(R.string.sp_vibrate_Calendar_NORMAL);
            }
            else {
                getActionBar().setTitle(mUserTitle);
                this.setTitle(mUserTitle);
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vibrate_piker);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getActionBar().setIcon(R.drawable.shortcut_vibrate_type);
        mContext = getApplicationContext();

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        Intent intent = getIntent();
        mParent_Type = intent.getIntExtra(VibratePatternInfo.PARENT_TYPE,
                VibratePatternInfo.INCOMING_CALL_SIM1);
        String mUserTitle = null;
        if (intent.getExtras() != null && intent.getExtras().get(VIBRATE_TITLE_SET) != null) {
            mUserTitle = intent.getExtras().get(VIBRATE_TITLE_SET).toString();
        }

        do_setInitTitle(mUserTitle);

        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);

        mVibratePatternInfo = new VibratePatternInfo(mContext, mParent_Type);
        if (mVibratePatternInfo != null) {
            mPatternName = mVibratePatternInfo.getAllpatternName();
        }
        mListView = (ListView)findViewById(android.R.id.list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setTag(false); // click, long click event boolean
        mCancelBtn = (Button)findViewById(R.id.cancel_button);
        mOklBtn = (Button)findViewById(R.id.ok_button);

        //Listener reg
        mCancelBtn.setOnClickListener(this);
        mOklBtn.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        // Original default value
        mOriginalDefaultVibrate = mVibratePatternInfo.getTypeDefaultName(mParent_Type);
        mOriginalDefaultPattern = mVibratePatternInfo.getTypeDefaultPattern(mParent_Type);
        mVibratePatternInfo.setOriginalDefaultVibrate(mOriginalDefaultVibrate);
        mVibratePatternInfo.setOriginalDefaultPattern(mOriginalDefaultPattern);

        mSelectVibrate = mVibratePatternInfo.getDBVibrateName(mParent_Type);
        setAdapter();
    }

    private void setAdapterOnlyList() {
        adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_single_choice_end,
                mPatternName);
        setListAdapter(adapter);
    }

    private void setAdapter_category() {
        try {
            mAdapter.addSection(getString(R.string.my_vibrations_category),
                    new ArrayAdapter(this, R.layout.simple_list_item_single_choice_end,
                            mVibratePatternInfo.getAllPatternName_User()));
            mAdapter.addSection(getString(R.string.lg_vibrations_category),
                    new ArrayAdapter(this, R.layout.simple_list_item_single_choice_end,
                            mVibratePatternInfo.getAllPatternName_LG()));
            setListAdapter(mAdapter);
        } catch (NullPointerException e) {
            Log.w(TAG, "NullPointerException");
        }
    }

    private void setAdapter() {
        if (mVibratePatternInfo.isUserPatternEmply()) {
            setAdapterOnlyList();
        }
        else {
            setAdapter_category();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_HOME:
                Log.i(TAG, "[dispatchKeyEvent] KeyEvent.KEYCODE_HOME");
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                Log.i(TAG, "[dispatchKeyEvent] KeyEvent.KEYCODE_DPAD_UP");
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.i(TAG, "[dispatchKeyEvent] KeyEvent.KEYCODE_DPAD_DOWN");
                break;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.i(TAG, "[dispatchKeyEvent] KeyEvent.KEYCODE_ENTER");
                LinearLayout layout;

                View v = getCurrentFocus().findViewById(R.id.add_vibrate_type);
                View v2 = getCurrentFocus().findViewById(R.id.item1);

                if (null != v || null != v2) {
                    return super.dispatchKeyEvent(event);
                }
                try {
                    layout = (LinearLayout)mListView.getSelectedView();
                    TextView name = (TextView)layout.getChildAt(0);
                    Log.i(TAG, "[dispatchKeyEvent] get title or name : "
                            + name.getText().toString());
                    if (VibratePatternInfo.EMPTY.equals(mVibratePatternInfo.getPattern(name
                            .getText().toString()))) {
                        return true;
                    }
                } catch (ClassCastException e) {
                    Log.w(TAG, "ClassCastException");
                } catch (NullPointerException e) {
                    Log.w(TAG, "NullPointerException");
                }
                break;
            default:
            	break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void updateList(String SelectVibrate) {
        if (Utils.isSPRModel()) {
            mVibratePatternInfo.UpdateUserPatternInfo();
        }
        if (mVibratePatternInfo.isUserPatternEmply()) {
            if (null != adapter) {
                adapter.clear();
            }
        }
        else {
            if (null != mAdapter) {
                mAdapter.clear();
            }
        }
        mPatternName = mVibratePatternInfo.getAllpatternName();
        vibrateDBCheck();

        // sort and list add
        //adapter.addAll(mVibratePatternInfo.getAllpatternName());

        setAdapter();

        //mSelectVibrate = mDefaultVibrate;
        Log.d(TAG, "Default Vibrate : " + mDefaultVibrate);

        // set current vibrate pattern checked
        //int length = adapter.getCount();

        int length = 0;
        if (mVibratePatternInfo.isUserPatternEmply()) {
            if (null != adapter) {
                length = adapter.getCount();
            }
        }
        else {
            if (null != mAdapter) {
                length = mAdapter.getCount();
            }
        }

        int count = currentVibrateChecked(SelectVibrate, length);

        Log.d(TAG, "length : " + length + "   count : " + count);
        // not checkbox state exception
        if (count == length) {
            notCheckedException(length);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mVibratePatternInfo.setItemSelected(false);

        mDefaultVibrate = mVibratePatternInfo.getDBVibrateName(mParent_Type);
        mSelectPattern = mVibratePatternInfo.getDBVibratePattern(mParent_Type);

        mSelectVibrate = mDefaultVibrate;
        invalidateOptionsMenu();
        updateList(mDefaultVibrate);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        vibrateStop();
        if (!mVibratePatternInfo.getItemSelected()) {
            mSelectVibrate = mVibratePatternInfo.getOriginalDefaultVibrate();
            mSelectPattern = mVibratePatternInfo.getOriginalDefaultPattern();
            if (Utils.isSPRModel()) {
                mVibratePatternInfo.saveSelectVibrate(mParent_Type, mSelectVibrate, mSelectPattern,
                        mListView.getCheckedItemPosition());
            }
            else {
                mVibratePatternInfo.saveSelectVibrate(mParent_Type, mSelectVibrate, mSelectPattern);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, mVibratePatternInfo.mHaptic_value);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuItem item = menu.findItem(R.id.item1);
        //hakgyu98.kim [B1 target, ringtone delete function activated]
        int nCount = mVibratePatternInfo.getUserPatternCount();
        if (nCount > 0) {
            item.setEnabled(true);
            item.setVisible(true);
        }
        else {
            item.setEnabled(false);
            item.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        Log.i(TAG, "[onOptionsItemSelected] go to VibrateCreateActivity");
        Log.i(TAG,
                "[onOptionsItemSelected] LG pattern count: "
                        + mVibratePatternInfo.getUserPatternCount());
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            if (item.getItemId() == R.id.search) {
                Intent search_intent = new Intent();
                search_intent.setAction("com.lge.settings.SETTINGS_SEARCH");
                search_intent.putExtra("search", true);
                startActivity(search_intent);
                return true;
            }
        }
        if (item.getItemId() == android.R.id.home) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, mVibratePatternInfo.mHaptic_value);
            Thread.currentThread().interrupt();
            finish();
            return true;
        } else if (item.getItemId() == R.id.add_vibrate_type) {
            startActivityVibrateCreation();
        } else {
            vibrateStop();
            Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibrateDeleteActivity");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void startActivityVibrateCreation() {
        if (MAX_VIBRATION == mVibratePatternInfo.getUserPatternCount()) {
            maxlengthToast();
        }
        else {
            vibrateStop();
            Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    "com.android.settings.vibratecreation.VibrateCreateActivity");

            intent.putExtra(VibratePatternInfo.PARENT_TYPE, mParent_Type);

            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.vibrate_action_menu, menu);

        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            MenuInflater inflater_search = getMenuInflater();
            inflater_search.inflate(R.menu.settings_search, menu);
        }

        return true;
    }

    private int currentVibrateChecked(String vibrationName, int length) {
        // radio check
        mListView.clearChoices();
        //Log.d(TAG,"List length : " +  adapter.getCount());
        Log.d(TAG, "[currentVibrateChecked] currentVibrateChecked : " + vibrationName);
        ArrayList<String> pattern = mVibratePatternInfo.getAllpatternName();
        int i = 0;
        for (i = 0; i < length; i++) {
            Log.d(TAG, "child list :  " + pattern.get(i).toString());
            if (null != pattern.get(i).toString()
                    && vibrationName.equals(pattern.get(i).toString())) {
                Log.d(TAG, "matching title!!!");
                mListView.setItemChecked(i, true);
                mListView.setSelection(i);
                return i;
            }
        }
        return i;
    }

    private void notCheckedException(int length) {
        //mVibratePatternInfo.saveSelectVibrate(mParent_Type, getResources().getString(R.string.vibrate_pattern_standard), VibratePatternInfo.VIBRATE_TYPE[2]);
        mDefaultVibrate = mVibratePatternInfo.getDBVibrateName(mParent_Type);
        mSelectPattern = mVibratePatternInfo.getDBVibratePattern(mParent_Type);
        mVibratePatternInfo.setOriginalDefaultVibrate(mDefaultVibrate);
        mVibratePatternInfo.setOriginalDefaultPattern(mSelectPattern);
        if (Utils.isSPRModel()) {
            mVibratePatternInfo.saveSelectVibrate(mParent_Type, mDefaultVibrate, mSelectPattern,
                    mListView.getCheckedItemPosition());
        }
        else {
            mVibratePatternInfo.saveSelectVibrate(mParent_Type, mDefaultVibrate, mSelectPattern);
        }
        ArrayList<String> pattern = mVibratePatternInfo.getAllpatternName();
        for (int i = 0; i < length; i++) {
            if (mDefaultVibrate.equals(pattern.get(i).toString())) {
                Log.d(TAG, "[exception] matching title!!!");
                mListView.setItemChecked(i, true);
                mListView.setSelection(i);
            }
        }
    }

    private void vibrateDBCheck() {
        if (null == mVibratePatternInfo.getDBVibrateName(mParent_Type)) {
            mVibratePatternInfo.setDBVibrateName(mParent_Type,
                    getResources().getString(R.string.vibrate_pattern_standard));
            mDefaultVibrate = mVibratePatternInfo.getDBVibrateName(mParent_Type);
        }
        if (null == mVibratePatternInfo.getDBVibratePattern(mParent_Type)) {
            mVibratePatternInfo.setDBVibratePattern(mParent_Type,
                    VibratePatternInfo.VIBRATE_TYPE[2]);
            mSelectPattern = mVibratePatternInfo.getDBVibratePattern(mParent_Type);
        }
    }

    private void playVibrate(long[] pattern) {

        if (null == mVibrator) {
            mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        }

        try {
            //mVibrator.vibrate(pattern, REPEAT, mVibratePatternInfo.getVolume(pattern));
            mVibrator.vibrate(pattern, REPEAT);
            mVibratePatternInfo.hapticFeedbackOff(pattern);
        } catch (NullPointerException e) {
            Log.e(TAG, "playVibrate() Null point exception!!!" + e.getMessage());
        }
    }

    public void vibrateStop() {
        if (null != mVibrator) {
            mVibrator.cancel();
        }
        //mVibratePatternInfo.saveSelectVibrate(mParent_Type, mSelectVibrate, mSelectPattern);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        mVibratePatternInfo.setItemSelected(true);
        if (v.equals(mCancelBtn)) {
            mSelectVibrate = mVibratePatternInfo.getOriginalDefaultVibrate();
            mSelectPattern = mVibratePatternInfo.getOriginalDefaultPattern();
            if (Utils.isSPRModel()) {
                mVibratePatternInfo.saveSelectVibrate(mParent_Type, mSelectVibrate, mSelectPattern,
                        mListView.getCheckedItemPosition());
            }
            else {
                mVibratePatternInfo.saveSelectVibrate(mParent_Type, mSelectVibrate, mSelectPattern);
            }
        }
        else if (v.equals(mOklBtn)) {
            mSelectVibrate = mPatternName.get(mListView.getCheckedItemPosition());
            final String vibrateTitle = mPatternName.get(mListView.getCheckedItemPosition())
                    .toString();
            mSelectPattern = mVibratePatternInfo.getPattern(vibrateTitle);
            if (Utils.isSPRModel()) {
                mVibratePatternInfo.saveSelectVibrate(mParent_Type, mSelectVibrate, mSelectPattern,
                        mListView.getCheckedItemPosition());
            }
            else {
                mVibratePatternInfo.saveSelectVibrate(mParent_Type, mSelectVibrate, mSelectPattern);
            }
        }
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, mVibratePatternInfo.getHaptic_value());
        finish();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, mVibratePatternInfo.mHaptic_value);
        Thread.currentThread().interrupt();
        super.onBackPressed();
    }

    private void removePopup(int position) {
        AlertDialog.Builder removeDialogBuilder = new AlertDialog.Builder(VibratePicker.this);
        removeDialogBuilder.setMessage(
                getString(R.string.sp_vibrate_creation_vibration_delete_popup))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        mVibratePatternInfo.removeVibratePattern(mPatternName.get(mRemovePosition)
                                .toString());
                        if (Utils.isSPRModel()) {
                            mVibratePatternInfo.mUserPatternManager
                                    .deleteVibrateUserPattern(mPatternName.get(mRemovePosition)
                                            .toString());
                        } else {
                            //mVibratePatternInfo.removeVibrateNameOthers(mPatternName.get(mRemovePosition).toString());
                        }
                        mListView.setTag(false);
                        updateList(mDefaultVibrate); // ?? ?????
                    }
                });
        removeDialogBuilder.setTitle(mPatternName.get(position));
        if (!Utils.isUI_4_1_model(mContext)) {
            removeDialogBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        removeDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                Log.d(TAG, "[Remove Dialog] Cancel event!");
                mListView.setTag(false);
            }
        });

        if (mVibratePatternInfo.isLGName(mPatternName.get(position))) {
            mListView.setTag(false);
            removeDialogBuilder.setMessage("LG vibrate cannot be deleted.");
        }
        else {
            removeDialogBuilder.setNegativeButton(android.R.string.no, null);
        }
        removeDialogBuilder.show();
    }

    private void removePopup(String patternName) {
        AlertDialog.Builder removeDialogBuilder = new AlertDialog.Builder(VibratePicker.this);
        removeDialogBuilder.setMessage(
                getString(R.string.sp_vibrate_creation_vibration_delete_popup))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (Utils.isSPRModel()) {
                            mVibratePatternInfo.mUserPatternManager
                                    .deleteVibrateUserPattern(mPatternName.get(mRemovePosition)
                                            .toString());
                        } else {
                            mVibratePatternInfo.removeVibrateNameOthers(mPatternName.get(
                                    mRemovePosition).toString());
                        }
                        mVibratePatternInfo.removeVibratePattern(mPatternName.get(mRemovePosition)
                                .toString());
                        mListView.setTag(false);
                        updateList(mDefaultVibrate); // ?? ?????
                        invalidateOptionsMenu();
                    }
                });
        removeDialogBuilder.setNegativeButton(android.R.string.no, null);
        removeDialogBuilder.setTitle(getString(R.string.delete));
        if (!Utils.isUI_4_1_model(mContext)) {
            removeDialogBuilder.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        removeDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                Log.d(TAG, "[Remove Dialog] Cancel event!");
                mListView.setTag(false);
            }
        });
        removeDialogBuilder.show();
    }

    private void selectPopup(int position) {
        CharSequence[] menu = new CharSequence[2];
        menu[0] = getResources().getString(R.string.bluetooth_rename_button);
        menu[1] = getResources().getString(R.string.delete);
        mReNameVibrate = mPatternName.get(position);
        mSelectPattern = mVibratePatternInfo.getPattern(mReNameVibrate);

        if (mSelectDialog != null) {
            mSelectDialog.dismiss();
            mSelectDialog = null;
        }

        mSelectDialog = new AlertDialog.Builder(this)
                .setTitle(mPatternName.get(position))
                .setItems(menu, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int selectedIndex) {
                        // TODO Auto-generated method stub
                        switch (selectedIndex) {
                        case RENAME:
                            createRenameDialog();
                            break;
                        case DELETE:
                            removePopup(mReNameVibrate);
                            break;
                        default:
                            return;
                        }
                    }
                })
                .show();
    }

    private void createRenameDialog() {

        final AlertDialog.Builder customDialogBuidler = new AlertDialog.Builder(this);
        //customDialog.setTitle(R.string.sp_gesture_title_home_tilt_NOMAL);
        LayoutInflater inflate = (LayoutInflater)this
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflate.inflate(R.layout.vibrate_text_input_dialog, null);
        final EditText edit = (EditText)contentView.findViewById(R.id.username_edit);
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

        edit.setText(mReNameVibrate);
        edit.requestFocus();
        edit.selectAll();
        edit.setFilters(new InputFilter[] { filter });
        edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        customDialogBuidler.setTitle(R.string.bluetooth_rename_button);
        customDialogBuidler.setPositiveButton(getResources().getString(R.string.menu_save),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (true == mVibratePatternInfo.isDuplicateName(edit.getText().toString())) {
                            //Toast.makeText(VibratePicker.this, edit.getText()+" is duplicate name.", Toast.LENGTH_SHORT).show();
                        }
                        else if (true == mVibratePatternInfo.iskeyBlank(edit.getText().toString())) {
                        }
                        else {
                            if (Utils.isSPRModel()) {
                                if (mSelectVibrate.equals(mReNameVibrate)) {
                                    Log.d("hkk", "matching!!!" + edit.getText().toString());
                                    mSelectVibrate = edit.getText().toString();
                                    mVibratePatternInfo.setOriginalDefaultVibrate(mSelectVibrate);
                                    mVibratePatternInfo.mUserPatternManager
                                            .updateVibrateUserPatternName(mReNameVibrate,
                                                    mSelectVibrate);

                                    updateList(edit.getText().toString());
                                } else {
                                    mVibratePatternInfo.mUserPatternManager
                                            .updateVibrateUserPatternName(mReNameVibrate, edit
                                                    .getText().toString());
                                    updateList(edit.getText().toString());
                                }

                            } else {
                                mSelectPattern = mVibratePatternInfo.getPattern(mReNameVibrate);
                                mVibratePatternInfo.removeVibratePattern(mReNameVibrate);
                                mVibratePatternInfo.saveVibratePattern(edit.getText().toString(),
                                        mSelectPattern);
                                if (mSelectVibrate.equals(mReNameVibrate)) {
                                    mSelectVibrate = edit.getText().toString();
                                    mVibratePatternInfo.setOriginalDefaultVibrate(mSelectVibrate);
                                    mVibratePatternInfo.renameVibrateNameOthers(mReNameVibrate,
                                            edit.getText().toString());
                                    //mVibratePatternInfo.setDBVibrateName(mParent_Type, edit.getText().toString());
                                    if (Utils.isSPRModel()) {
                                        mVibratePatternInfo.saveSelectVibrate(mParent_Type, edit
                                                .getText().toString(), mSelectPattern, mListView
                                                .getCheckedItemPosition());
                                    }
                                    else {
                                        mVibratePatternInfo.saveSelectVibrate(mParent_Type, edit
                                                .getText().toString(), mSelectPattern);
                                    }
                                    //updateList(edit.getText().toString());
                                } else {
                                    mVibratePatternInfo.renameVibrateNameOthers(mReNameVibrate,
                                            edit.getText().toString());
                                }
                                updateList(edit.getText().toString());
                            }
                        }
                    }
                });
        customDialogBuidler.setNegativeButton(getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });

        customDialogBuidler.setView(contentView);
        //customDialog.show();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                InputMethodManager input = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                //input.showSoftInput(edit, input.SHOW_IMPLICIT);
                input.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);

        AlertDialog dialog = customDialogBuidler.create();

        dialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                // TODO Auto-generated method stub
                //((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                // textwatcher
                edit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //len = filter.getByteLength(edit.getText().toString());
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // TODO Auto-generated method stub
                        Log.i(TAG, "beforeTextChanged : " + s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        /*
                        if ( mVibratePatternInfo.iskeyBlank(s.toString())) {
                                 edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                         }
                        */
                        if (mVibratePatternInfo.isDuplicateName(s.toString())
                                || mVibratePatternInfo.iskeyBlank(s.toString())
                                || mVibratePatternInfo.isAllSpace(s.toString())) {
                            ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setEnabled(false);
                        }
                        else {
                            ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setEnabled(true);
                        }

                    }
                });
            }
        });
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        // TODO Auto-generated method stub

        if (false == mVibratePatternInfo.isLGName(mPatternName.get(position))) {

            if (null == mVibrator) {
                mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
            }
            mVibrator.vibrate(20);
            selectPopup(position);
        }
        //Toast.makeText(VibratePicker.this, "arg2 : " + position, Toast.LENGTH_SHORT).show();
        mRemovePosition = position;
        mListView.setTag(true);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        // TODO Auto-generated method stub
        //mSelectVibrate = mPatternName.get(position);

        if (mSelectDialog != null && mSelectDialog.isShowing()) {
            return;
        }
        mListView.clearChoices();
        mListView.setItemChecked(position, true);

        //Toast.makeText(VibratePicker.this, "Select item : " + mSelectVibrate, Toast.LENGTH_SHORT).show();
        Log.d(TAG, mPatternName.get(position).toString());

        Log.i("soosin", "onItemClick");

        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                final String vibrateTitle = mPatternName.get(mListView.getCheckedItemPosition())
                        .toString();
                //mSelectPattern = mVibratePatternInfo.getPattern(vibrateTitle);
                if (!VibratePatternInfo.EMPTY.equals(mVibratePatternInfo.getPattern(vibrateTitle))) {
                    if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                        playVibrate(mVibratePatternInfo.patternTokenizer(mVibratePatternInfo
                                .getPattern(vibrateTitle)));
                    }
                    Log.d(TAG, "playVibrate");
                }
            }
        }).start();
        Log.d(TAG, "after thread");
    }

    private void maxlengthToast() {
        if (mToast == null) {
            mToast = Toast.makeText(this, R.string.vibrate_max_toast_popup, Toast.LENGTH_SHORT);
        }
        else {
            mToast.setText(R.string.vibrate_max_toast_popup);
        }
        mToast.show();
    }

    private void maxlengthEditToast() {
        if (mToast == null) {
            mToast = Toast.makeText(VibratePicker.this, R.string.sp_auto_reply_maxlength_NORMAL,
                    Toast.LENGTH_SHORT);
        }
        else {
            mToast.setText(R.string.sp_auto_reply_maxlength_NORMAL);
        }
        mToast.show();
    }

}
