package com.android.settings.vibratecreation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.ByteLengthFilter;
import com.android.settings.lgesetting.Config.Config;

public class VibrateCreateActivity extends Activity implements OnTouchListener, OnClickListener {
    /** Called when the activity is first created. */

    private static final String TAG = "VibrateCreateActivity";
    private static final int MAX_TIME = 8000;
    private static final int DELAY_TIME = 200;
    private static final int WAIT_TIME = 50;
    private static final int GAP = 1;
    private static final int MSG_UPDATE_IMAGE = 1;
    private static final int MSG_UPDATE_VALUE_ANIMATION_START = 2;
    private static final int MSG_UPDATE_SEEKBAR = 3;
    private static final int MSG_UPDATE_BUTTON = 4;
    private static final int MSG_UPDATE_RESET = 5;
    public static final int DEFAULT_COLOR = Color.rgb(229, 229, 229);
    private static final int MAX_LEN = 15;
    private static final int NOT_FOUND = -1;

    private FrameLayout mFrameLayout;
    private VibrateView mVibrateView_default;
    private VibrateView mVibrateView_Vibrate;

    private Button mStop_Save;
    private Button mPreview;
    private Vibrator mVibrator;
    private SeekBar mTimeLine;
    private ArrayList<String> mPattern = new ArrayList<String>();
    private Thread mSwitchImageThread;
    private ValueAnimator mValueAnimator;
    private long mVibrateStart = 0;
    private long mVibrateEnd = 0;
    private long mSilentStart = 0;
    private long mSilentEnd = 0;
    private boolean mRecodeFlag;
    private boolean mTouchEvent_Finish;
    private boolean mIsTouch = false;
    private boolean mIsPreview = false;

    private boolean mBePlayThreadStop = false;
    private int mCurRecTimeMs = 0;
    private int mCurMaxProgress = MAX_TIME;
    private int mParent_Type = VibratePatternInfo.INCOMING_CALL_SIM1;
    private long[] mVibratePattern;
    private Set<String> mPatternSet = new HashSet<String>();
    private VibratePatternInfo mVibratePatternInfo;
    private TextView mTabHere;
    private Toast mToast;
    private int mHaptic_value = VibratePatternInfo.DEFAULT_HAPTIC;
    private int mProgress = 0;

    private AlertDialog mRenameDialog;

    private InputMethodManager imm;
    private boolean isRingerMode_changed = false;
    private boolean isPause = false;
    private int mBefore_soundprofile;
    private AudioManager mAudioManager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            MenuInflater inflater_search = getMenuInflater();
            inflater_search.inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                Log.i(TAG, "[onReceive] #####RINGER_MODE_CHANGED_ACTION#####");
                if (mBefore_soundprofile != mAudioManager.getRingerMode()) {
                    Log.i(TAG, "[onReceive] before sound profile != current sound profile");
                    isRingerMode_changed = true;
                    stateReset();
                    mSilentStart = 0;
                }
            }
        }
    };

    private Runnable mSwitchImageRunnable = new Runnable() {
        @SuppressWarnings("static-access")
        public void run() {
            try {
                while (null != mSwitchImageThread &&
                        !mSwitchImageThread.interrupted() &&
                        false == mBePlayThreadStop) {
                    imageChanger();
                    if (null != mSwitchImageThread && mSwitchImageThread.isAlive()) {
                        mSwitchImageThread.interrupt();
                    }
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    AnimatorUpdateListener mAnimatorUpdateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            // TODO Auto-generated method stub
            int animProgress = 0;
            if (animation != null) {
                animProgress = (Integer)animation.getAnimatedValue();
            }
            mCurRecTimeMs = animProgress;
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SEEKBAR, mCurRecTimeMs));
            //Log.i(TAG, "onAnimationUpdate :" + animProgress);
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_IMAGE:
                Log.i(TAG, "[Handler] MSG_UPDATE_IMAGE image number : " + (Boolean)msg.obj);
                if (mStop_Save.getText() == getResources().getText(R.string.menu_save)) {
                    setImageDefault();
                    return;
                }
                if ((Boolean)msg.obj) {
                    setImageDefault();
                }
                else {
                    setImageVibrate();
                }
                break;
            case MSG_UPDATE_VALUE_ANIMATION_START:
                mValueAnimator.start();
                break;
            case MSG_UPDATE_SEEKBAR:
                mProgress = (Integer)msg.obj;
                mTimeLine.setProgress(mProgress);
                mCurMaxProgress = mProgress;
                break;
            case MSG_UPDATE_BUTTON:
                setPreviewButtonEnabled((Boolean)msg.obj);
                mTimeLine.setProgress(mTimeLine.getMax());
                break;
            case MSG_UPDATE_RESET:
                reSetPatternInfo();
                break;
            default:
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vibrate_create);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.drawable.shortcut_vibrate_type);
        }

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        Intent intent = getIntent();
        mParent_Type = intent.getIntExtra(VibratePatternInfo.PARENT_TYPE,
                VibratePatternInfo.INCOMING_CALL_SIM1);

        mVibratePatternInfo = new VibratePatternInfo(getBaseContext(), mParent_Type);
        mTimeLine = (SeekBar)findViewById(R.id.seekbar);
        mVibrateView_default = (VibrateView)findViewById(R.id.vibrateview_01);
        mVibrateView_default.setDefaultImage();
        mVibrateView_Vibrate = (VibrateView)findViewById(R.id.vibrateview_02);
        mVibrateView_Vibrate.setVibrateImage();
        mVibrateView_Vibrate.setVisibility(View.GONE);
        mPreview = (Button)findViewById(R.id.bt_preview);
        mStop_Save = (Button)findViewById(R.id.bt_save_stop);
        mVibrator = (Vibrator)getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        mFrameLayout = (FrameLayout)findViewById(R.id.midLayout);

        int color = mVibratePatternInfo.getColorForResName(this,
                VibratePatternInfo.PKG,
                VibratePatternInfo.BG_COLOR_ID);

        if (NOT_FOUND != color) {
            mFrameLayout.setBackgroundColor(color);
            Log.i(TAG, "overlay color set");
        }
        mTabHere = (TextView)findViewById(R.id.tab_here);
        mTabHere.setTextColor(getResources().getColor(android.R.color.black));

        mPattern.add(Integer.toString(0));
        mTimeLine.setMax(mCurMaxProgress); // 8 seconds
        mRecodeFlag = false;
        mTouchEvent_Finish = false;

        mHaptic_value = Settings.System.getInt(getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                VibratePatternInfo.DEFAULT_HAPTIC);

        disableSeekbarTouchEvent();
        setStopSaveButtonEnabled(false);
        setPreviewButtonEnabled(false);

        // listener set
        mVibrateView_default.setOnTouchListener(this);
        mFrameLayout.setOnTouchListener(this);
        mPreview.setOnClickListener(this);
        mStop_Save.setOnClickListener(this);
        setSeekbarListener();

        // thread new
        mSwitchImageThread = new Thread(mSwitchImageRunnable);
    }

    private void reSetPatternInfo() {
        Log.i(TAG, "[reSetPatternInfo] init");
        mValueAnimator.cancel();
        mSilentStart = 0;
        mProgress = 0;
        mIsTouch = false;
        mIsPreview = false;
        mPattern.clear();
        mPattern.add(Integer.toString(0));
        mCurMaxProgress = MAX_TIME;
        mTimeLine.setMax(mCurMaxProgress); // 8 seconds
        mTimeLine.setProgress(mProgress);
        mRecodeFlag = false;
        mTouchEvent_Finish = false;
        mStop_Save.setText(R.string.service_stop);
        mTabHere.setVisibility(View.VISIBLE);
        setStopSaveButtonEnabled(false);
        setPreviewButtonEnabled(false);
        mValueAnimator.removeAllListeners();
        setSeekbarListener();
    }

    private void setPreviewButtonEnabled(boolean state) {
        mPreview.setEnabled(state);
        mPreview.setFocusable(state);
    }

    private void setStopSaveButtonEnabled(boolean state) {
        mStop_Save.setEnabled(state);
        mStop_Save.setFocusable(state);
    }

    private void disableSeekbarTouchEvent() {
        // seekbar touch event disabled
        mTimeLine.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });
    }

    private void setSeekbarUpdateListener() {
        mValueAnimator = ValueAnimator.ofInt(0, mTimeLine.getMax());
        mValueAnimator.setInterpolator(AnimationUtils.loadInterpolator(
                this,
                android.R.anim.linear_interpolator));
        mValueAnimator.setDuration(mCurMaxProgress);
        mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
    }

    private void setSeekbarListener() {
        setSeekbarUpdateListener();
        mValueAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
                Log.i(TAG, "onAnimationStart");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub
                Log.i(TAG, "onAnimationRepeat");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                Log.i(TAG, "onAnimationEnd");

                new Timer().schedule(new TimerTask() {
                    public void run() {
                        Log.i(TAG, "onAnimationEnd - Button enabled!");
                        Log.i(TAG, "onAnimationEnd - isRingerMode_changed : " + isRingerMode_changed
                            + "\n" +  "isPause : " + isPause);
                        if (true == isRingerMode_changed) {
                            isRingerMode_changed = false;
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_RESET));
                        } else if  (isPause) {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SEEKBAR, 0));
                        }
                        else {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_BUTTON, true));
                        }
                    }
                }, WAIT_TIME);

                mStop_Save.setText(R.string.menu_save);
                mTabHere.setVisibility(View.INVISIBLE);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.HAPTIC_FEEDBACK_ENABLED,
                        mHaptic_value);
                enableRotation();

                if (null != mSwitchImageThread && mSwitchImageThread.isAlive()) {
                    mSwitchImageThread.interrupt();
                }
                setImageDefault();
                long lastTime = System.currentTimeMillis();
                ;
                if (true == mRecodeFlag) {
                    if (null != mVibrator) {
                        mVibrator.cancel();
                        mTouchEvent_Finish = true;
                        Log.i(TAG, "mTouchEvent_Finish : " + mTouchEvent_Finish);
                    }
                    if (true == mIsTouch) {
                        mVibrateEnd = lastTime;
                        mSilentStart = mVibrateEnd;
                        Log.i(TAG, "mIsTouch ==true : last array - "
                                + (mVibrateEnd - mVibrateStart));
                        if (0 != mSilentStart && false == mIsPreview) {
                            Log.i(TAG, "mISPreview : " + mIsPreview);
                            mPattern.add(Long.toString(mVibrateEnd - mVibrateStart));
                        }
                    } else {
                        mVibrateStart = lastTime;
                        mSilentEnd = mVibrateStart;
                        Log.i(TAG, "mIsTouch ==false : last array - "
                                + (mSilentEnd - mSilentStart));
                        if (0 != mSilentStart && false == mIsPreview) {
                            Log.i(TAG, "mISPreview : " + mIsPreview);
                            mPattern.add(Long.toString(mSilentEnd - mSilentStart));
                        }
                    }
                }
                mBePlayThreadStop = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
                Log.i(TAG, "onAnimationCancel");
            }
        });
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isPause = false;
        mTimeLine.setProgress(mProgress);
        mSilentStart = 0;
        mBefore_soundprofile = mAudioManager.getRingerMode();
        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        log("onResume");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        log("onPause");
        super.onPause();
        isPause = true;

        Settings.System.putInt(getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                mHaptic_value);
        unregisterReceiver(mReceiver);

        if (mRenameDialog != null) {
            mRenameDialog.dismiss();
        }
        stateReset();
    }

    private void stateReset() {
        if (null != mVibrator) {
            mVibrator.cancel();
        }

        if (mTouchEvent_Finish == false) {
            Log.i(TAG, "mTouchEvent_Finish false");
            if (true == isRingerMode_changed) {
                mValueAnimator.end();
            }
            else if (true == isPause) {
                Log.i(TAG, "[stateReset] isPause = true");
                reSetPatternInfo();
            }
        }
        else {
            Thread.currentThread().interrupt();
            mValueAnimator.clearAllAnimations();
            if (null != mSwitchImageThread && mSwitchImageThread.isAlive()) {
                mSwitchImageThread.interrupt();
            }
            setImageDefault();
            mStop_Save.setText(R.string.menu_save);
            setPreviewButtonEnabled(true);
            mValueAnimator.cancel();

        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mPattern.clear();
        super.onDestroy();
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }

    private long[] buildlongArray(ArrayList<String> pattern) {
        long[] result = new long[pattern.size()];
        int i = 0;
        //mPatternSet.addAll(mPattern);
        for (String n : pattern) {
            result[i++] = Long.parseLong(n);
            Log.i(TAG, "array value[" + (i - GAP) + "] :" + result[i - GAP]);
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        log("onBackPressed");
        stateReset();
        super.onBackPressed();
    }

    private int sumVibrateTime(ArrayList<String> pattern) {
        long result = 0;
        for (String n : pattern) {
            result = result + Long.parseLong(n);
        }
        return (int)result;
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mPreview) {
            mBePlayThreadStop = false;
            if (mSwitchImageThread.getState() == Thread.State.NEW) {
            }
            else {
                mSwitchImageThread.interrupt();
                mSwitchImageThread = new Thread(mSwitchImageRunnable);
            }
            mSwitchImageThread.start();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "Seekbar animation start");
                    while (!Thread.currentThread().isInterrupted()) {
                        Looper.prepare();
                        mHandler.sendMessage(mHandler.obtainMessage(
                                MSG_UPDATE_VALUE_ANIMATION_START,
                                true));
                        Looper.loop();
                    }
                }
            }).start();

            mStop_Save.setText(R.string.service_stop);
            setPreviewButtonEnabled(false);
            mIsPreview = true;
        } else if (v == mStop_Save) {
            if (mStop_Save.getText().toString()
                    .equals(getResources().getText(R.string.service_stop))) {
                if (false == mIsPreview) {
                    if (mIsTouch == true) {
                        actionUpevent();
                    }
                    mTouchEvent_Finish = true;
                    mCurMaxProgress = mCurRecTimeMs;
                    mCurRecTimeMs = 0;
                    mCurMaxProgress = sumVibrateTime(mPattern);
                    mTimeLine.setMax(mCurMaxProgress);
                    mValueAnimator.end();
                    mValueAnimator.removeAllListeners();
                    setSeekbarListener();
                }
                else {
                    mValueAnimator.clearAllAnimations();
                }

                if (null != mSwitchImageThread && mSwitchImageThread.isAlive()) {
                    mSwitchImageThread.interrupt();
                }
                setImageDefault();
                mStop_Save.setText(R.string.menu_save);
                setPreviewButtonEnabled(true);

                mValueAnimator.cancel();
                //mTimeLine.setProgress(mCurMaxProgress);
                mProgress = mTimeLine.getMax();
                mTimeLine.setProgress(mCurMaxProgress);

                mVibrator.cancel();
            }
            else if (mStop_Save.getText().toString()
                    .equals(getResources().getText(R.string.menu_save))) {
                createRenameDialog();
            }
        }
    }

    private static final int DEFAULT_LAND = 8;
    private static final int DEFAULT_PORT = 9;

    private void disableRotation(Activity activity)
    {
        final int orientation = activity.getResources().getConfiguration().orientation;
        final int rotation = activity.getWindowManager().getDefaultDisplay().getOrientation();

        // Copied from Android docs, since we don't have these values in Froyo 2.2

        int landscape = DEFAULT_LAND;
        int portrait = DEFAULT_PORT;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO)
        {
            landscape = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            portrait = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
        {
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270)
        {
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                activity.setRequestedOrientation(portrait);
            }
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                activity.setRequestedOrientation(landscape);
            }
        }
    }

    private void enableRotation()
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub

        if (v == mVibrateView_default || v == mVibrateView_Vibrate || v == mFrameLayout) {
            Log.i(TAG, "mVibrateView_default onTouch");

            if (true == mTouchEvent_Finish) {
                Log.i(TAG, "onTouch return false");
                return false;
            }
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                Log.i(TAG, getRequestedOrientation() + "");
                disableRotation(this);
                if (0 == mSilentStart) {
                    // start seekbar running
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.i(TAG, "Seekbar animation start");
                            Looper.prepare();
                            mHandler.sendMessage(mHandler.obtainMessage(
                                    MSG_UPDATE_VALUE_ANIMATION_START,
                                    true));
                            Looper.loop();
                        }
                    }).start();

                    Settings.System.putInt(getContentResolver(),
                            Settings.System.HAPTIC_FEEDBACK_ENABLED,
                            0);
                    setStopSaveButtonEnabled(true);
                    Log.i(TAG, "start seekbar running!!");
                }
                mIsTouch = true;

                if (null == mPattern) {
                    return false;
                }
                mVibrateStart = System.currentTimeMillis();
                mSilentEnd = mVibrateStart;
                if (0 != mSilentStart) {
                    mPattern.add(Long.toString(mSilentEnd - mSilentStart));
                }
                playVibrate(MAX_TIME + DELAY_TIME);
                setImageVibrate();

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                mRecodeFlag = true;
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mIsTouch == true) {
                    actionUpevent();
                }
                break;
            }
            default:
                break;
            }
        }
        return true;
    }

    private void actionUpevent() {
        mIsTouch = false;
        mVibrateEnd = System.currentTimeMillis();
        mSilentStart = mVibrateEnd;
        mVibrator.cancel();
        mPattern.add(Long.toString(mVibrateEnd - mVibrateStart));
        Log.i(TAG, "touch time : " + (mVibrateEnd - mVibrateStart));
        setImageDefault();
        mRecodeFlag = true;
    }

    private void playVibrate(int time) {
        //mVibrator.vibrate(time, mVibrator.getVibrateVolume(Vibrator.VIBRATE_TYPE_RING));
        mVibrator.vibrate(time);
    }

    private static final int NOREPEAT = -1;

    synchronized private int playVibratePattern() {
        mVibratePattern = buildlongArray(mPattern);
        hapticFeedbackOff();

        mVibrator.vibrate(mVibratePattern, NOREPEAT);
        Log.i(TAG, "playVibratePattern()");
        return mVibratePattern.length;
    }

    private static final int DEFAULT_FOR = 1;

    private void imageChanger() throws InterruptedException {
        synchronized (this) {
            boolean switchimage = false;
            Log.i(TAG, "switchimage1 : " + switchimage);
            int length = playVibratePattern();

            for (int i = DEFAULT_FOR; i < length; i++) {
                if (mStop_Save.getText() == getResources().getText(R.string.menu_save)) {
                    Log.i(TAG, "imageChanger() - cancel");
                    mVibrator.cancel();
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, true));
                    return;
                }
                mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, switchimage));

                switchimage = !switchimage;
                try {
                    Thread.sleep(mVibratePattern[i]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    Log.i(TAG, "IllegalArgumentException : " + e);
                }
                Log.i(TAG, "switchimage2 : " + switchimage);
            }
        }
    }

    private void hapticFeedbackOff() {
        Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);

        new Timer().schedule(new TimerTask() {
            public void run() {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.HAPTIC_FEEDBACK_ENABLED,
                        mHaptic_value);
            }
        }, sumVibrateTime(mPattern));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        mVibrateView_default.setBackgroundImage();
    }

    private static final int INPUT_DELAY = 100;

    private void createRenameDialog() {

        final AlertDialog.Builder customDialogBuidler = new AlertDialog.Builder(this);
        //customDialog.setTitle(R.string.sp_gesture_title_home_tilt_NOMAL);
        LayoutInflater inflate = (LayoutInflater)
                this.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
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
        boolean loop = true;
        while (loop) {
            if (mVibratePatternInfo.isDuplicateName(
                    "Vibration " +
                            mVibratePatternInfo.getDBMyVibrationCountString())) {
                mVibratePatternInfo.setDBMyVibrationCount(
                        mVibratePatternInfo.getDBMyVibrationCount() + GAP);
            }
            else {
                loop = false;
                edit.setText("Vibration " + mVibratePatternInfo.getDBMyVibrationCountString());
            }
        }

        edit.requestFocus();
        edit.selectAll();
        edit.setFilters(new InputFilter[] { filter });
        edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        customDialogBuidler.setTitle(R.string.vibrate_type_name);
        customDialogBuidler.setPositiveButton(getResources()
                .getString(R.string.menu_save),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (true == mVibratePatternInfo.isDuplicateName(edit.getText().toString())) {
                            Toast.makeText(VibrateCreateActivity.this,
                                    edit.getText() + " is duplicate name.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else if (true == mVibratePatternInfo.iskeyBlank(edit.getText().toString())) {
                            Toast.makeText(VibrateCreateActivity.this,
                                    "blank name",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mPatternSet.addAll(mPattern);
                            Log.i(TAG, "mPattern : " + mPattern.toString());
                            String pattern = mPattern.toString();
                            pattern = removeToken(mPattern.toString());
                            Log.i(TAG, "toekn remove mPattern : " + pattern);
                            if (!Utils.isSPRModel()) {
                                mVibratePatternInfo.saveSelectVibrate(mParent_Type,
                                        edit.getText().toString(),
                                        pattern);
                                mVibratePatternInfo.saveVibratePattern(edit.getText().toString(),
                                        pattern);
                            } else {
                                mVibratePatternInfo.saveVibratePattern(edit.getText().toString(),
                                        pattern);
                                mVibratePatternInfo.saveSelectVibrate(mParent_Type,
                                        edit.getText().toString(),
                                        pattern, 0);
                            }
                            mVibratePatternInfo.setItemSelected(true);
                            finish();
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
        new Handler().postDelayed(new Runnable() {
            public void run() {
                InputMethodManager input = (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
                input.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
            }
        }, INPUT_DELAY);

        mRenameDialog = customDialogBuidler.create();

        mRenameDialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                // TODO Auto-generated method stub
                edit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Log.i(TAG, "onTextChanged : " + s.toString());
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                            int after) {
                        Log.i(TAG, "beforeTextChanged : " + s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (mVibratePatternInfo.isDuplicateName(s.toString()) ||
                                mVibratePatternInfo.iskeyBlank(s.toString()) ||
                                mVibratePatternInfo.isAllSpace(s.toString())) {
                            ((AlertDialog)dialog)
                                    .getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                        else {
                            ((AlertDialog)dialog)
                                    .getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });

            }
        });
        mRenameDialog.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mPreview.setClickable(true);
        mRenameDialog.show();
    }

    private String removeToken(String pattern) {
        pattern = pattern.replace(" ", "");
        pattern = pattern.replace("[", "");
        pattern = pattern.replace("]", "");
        return pattern;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            stateReset();
            finish();
            return true;
        }

        if (getResources().getBoolean(R.bool.config_settings_search_enable)) {
            if (item.getItemId() == R.id.search) {
                Intent search_intent = new Intent();
                search_intent.setAction("com.lge.settings.SETTINGS_SEARCH");
                search_intent.putExtra("search", true);
                startActivity(search_intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void maxlengthEditToast() {
        if (mToast == null) {
            mToast = Toast.makeText(VibrateCreateActivity.this,
                    R.string.sp_auto_reply_maxlength_NORMAL,
                    Toast.LENGTH_SHORT);
        }
        else {
            mToast.setText(R.string.sp_auto_reply_maxlength_NORMAL);
        }
        mToast.show();
    }

    private void setImageDefault() {
        if (null != mVibrateView_Vibrate) {
            mVibrateView_Vibrate.setVisibility(View.GONE);
            mVibrateView_default.setVisibility(View.VISIBLE);
        }
    }

    private void setImageVibrate() {
        if (null != mVibrateView_Vibrate) {
            mVibrateView_default.setVisibility(View.GONE);
            mVibrateView_Vibrate.setVisibility(View.VISIBLE);
        }
    }
}

class VibrateView extends ImageView {
    private static final int NOT_FOUND = -1;
    private static final int ALPHA = 255;
    private boolean mBGimage;
    private VibratePatternInfo mVInfo;

    public VibrateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mBGimage = false;

        this.setAlpha(ALPHA);
        //        this.setImageAlpha(ALPHA);

        mVInfo = new VibratePatternInfo(context, 0);
        int color = mVInfo.getColorForResName(context,
                VibratePatternInfo.PKG,
                VibratePatternInfo.BG_COLOR_ID);

        if (NOT_FOUND != color) {
            this.setBackgroundColor(color);
        }
    }

    public void setDefaultBackgroundImage() {
        mBGimage = false;
        //this.setImageResource(R.drawable.setting_vibrate_creator_image_01);
        this.setVisibility(View.VISIBLE);
    }

    public void setVibrateBackgroundImage() {
        mBGimage = true;
        //this.setImageResource(R.drawable.setting_vibrate_creator_image_02);
    }

    public void setDefaultImage() {
        this.setImageResource(R.drawable.img_sound_vibrate_creator_01);
    }

    public void setVibrateImage() {
        this.setImageResource(R.drawable.img_sound_vibrate_creator_02);
    }

    public void setBackgroundImage() {
        if (mBGimage) {
            //setDefaultBackgroundImage();
            setVibrateBackgroundImage();
        }
        else {
            //setVibrateBackgroundImage();
            setDefaultBackgroundImage();
        }
    }
}
