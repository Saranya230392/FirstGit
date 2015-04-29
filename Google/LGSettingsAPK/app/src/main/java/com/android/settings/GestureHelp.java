package com.android.settings;

import java.util.zip.Inflater;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.content.Intent;
import android.content.res.Configuration;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.lgesetting.Config.ConfigHelper;
import android.os.Build;
import com.android.settings.SettingsPreferenceFragment;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.os.SystemProperties;
import android.content.res.Resources;
import android.view.MenuItem;

public class GestureHelp extends SettingsPreferenceFragment implements
        OnClickListener {
    private int mCOUNT = 14;

    // [START][seungyeop.yeom][2014-02-28][User Help Guide]
    private int mHelpGuidePosition;
    final static String HELP_GUIDE_FUNCTUION = "help_guide_function";
    final static String HELP_GUIDE_NUMBER = "help_guide_number";
    // [END]

    final static int INDEX_TAKESCREENSHOT = R.drawable.help_take_screenshot;
    final static int INDEX_KNOCK = R.drawable.help_screen_on_off;
    final static int INDEX_KNOCKOFF = R.drawable.help_screen_off;
    final static int INDEX_MEDIA_VOLUME_CONTROL = R.drawable.help_media_volume_control;
    final static int INDEX_APP_SHORTCUT = R.drawable.help_app_shortcut;
    final static int INDEX_ANSWERANINCOMINGCALL = R.drawable.help_answer_incoming_call;
    final static int INDEX_FADEOUTRINGTONE = R.drawable.help_fade_out_ringtone;
    final static int INDEX_SILENCEINCOMINGCALLS = R.drawable.help_silence_incoming_calls;
    final static int INDEX_SPEAKERPHONE = R.drawable.help_turn_on_off_speakerphone;
    final static int INDEX_HIDEDISPLAY = R.drawable.help_privacy_screen;
    final static int INDEX_SLIDEASIDE = R.drawable.help_slide_aside;
    final static int INDEX_SNOOZETURNOFFALARM = R.drawable.help_snooze_or_stop_alarm;
    final static int INDEX_PAUSEVIDEO = R.drawable.help_pause_video;
    final static int INDEX_MOVEHOME = R.drawable.help_move_home_screen_items;

    private int mPrevPosition;
    private static final String TAG = "GestureHelp";
    private ViewPager mPager;
    private LinearLayout mPageMark;
    private Button mPrev;
    private Button mNext;
    private TextView tv;
    private TextView title;
    private ImageView iv;
    private boolean isAnimated;
    private BkPagerAdapter mPageAdapter;
    final static int INDEX_KNOCKON_TITLE = 1;
    final static int sNotFoundInde = -1;

    private ArrayList<Integer> mImageArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.drawable.help_take_screenshot,
                    R.drawable.help_screen_on_off, //screen on/off knock
                    R.drawable
                    .help_screen_off, //screen off knock
                    R.drawable
                    .help_app_shortcut, //app shortcut
                    R.drawable
                    .help_media_volume_control, //media volume control
                    R.drawable.help_answer_incoming_call, //answer an incoming call
                    R.drawable.help_fade_out_ringtone, //fade out ringtone
                    R.drawable.help_silence_incoming_calls, //silence incoming calls
                    R.drawable.help_turn_on_off_speakerphone, //turn on/off speakerphone
                    R.drawable.help_privacy_screen, //hide display
                    R.drawable.help_slide_aside, //slide aside
                    R.drawable.help_snooze_or_stop_alarm, //snooze or turn off alarm
                    R.drawable.help_pause_video, //pause video
                    R.drawable.help_move_home_screen_items)); //move home screen items

    private ArrayList<Integer> mTextArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.gesture_summay_take_screenshot,
                    R.string.
                    gesture_screen_on_off_summary_open, //screen on/off knock
                    R.string.gesture_screen_on_off_help_summary, //screen off knock
                    R.string
                    .gesture_app_short_cut_help, //app shortcut
                    R.string.
                    gesture_media_volume_control_help, //media volume control
                    R.string.gesture_summary_incoming_call_help, //answer an incoming call
                    R.string.gesture_summary_fadeout_ringtone_help, //fade out ringtone
                    R.string.
                    gesture_silence_incoming_calls_description, //silence incoming calls
                    R.string.gesture_turn_onoff_speakerphone_help_desc, //turn on/off speakerphone
                    R.string.gesture_privacy_screen_summary, //hide display
                    R.string.gesture_help_desc_slide_aside, //slide aside
                    R.string.sp_ges_help_flip_alarm_sum_NORMAL, //snooze or turn off alarm
                    R.string.gesture_flip_video_help, //pause video
                    R.string.gesture_help_move_home)); //move home screen items
    private ArrayList<Integer> mTitleArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.gesture_title_take_screenshot,
                    R.string.gesture_title_knockon, //screen on/off knock
                    R.string.gesture_knockon_screenoff, //screen off knock
                    R.string
                    .gesture_app_shortcut_title, //app shortcut
                    R.string.gesture_media_volume_control_title, //media volume control
                    R.string.gesture_answer_the_incoming_call, //answer an incoming call
                    R.string.gesture_fadeout_ringtone, //fade out ringtone
                    R.string.sp_gesture_title_call_NORMAL, //silence incoming calls
                    R.string.gesture_turn_onoff_speakerphone, //turn on/off speakerphone
                    R.string.gesture_privacy_screen_title, //hide display
                    R.string.sp_settings_slideaside, //slide aside
                    R.string.sp_gesture_title_alarm_NORMAL, //snooze or turn off alarm
                    R.string.sp_gesture_title_video_NORMAL, //pause video
                    R.string.sp_gesture_title_home_NORMAL)); //move home screen items

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            mHelpGuidePosition = getActivity().getIntent().getIntExtra(HELP_GUIDE_NUMBER, 0);
            Log.d("YSY", "help Guide Position : " + mHelpGuidePosition);
            getActivity().getActionBar().setTitle(R.string.smart_tips_title);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // [END]

        if ((Config.DCM).equals(Config.getOperator())
                || !Utils.checkPackage(getActivity(), "com.lge.videoplayer")) {
            removeHelpScreen(INDEX_PAUSEVIDEO);
            mTitleArray.set(
                    INDEX_KNOCKON_TITLE,
                    R.string.gesture_seekbar_screenonoff);
        }
        //VZW Help KnockOn Title
        if ((Config.VZW).equals(Config.getOperator())) {
            mTitleArray.set(
                    INDEX_KNOCKON_TITLE,
                    R.string.gesture_title_turn_screen_on);
            mTextArray.set(INDEX_KNOCKON_TITLE, R.string.gesture_screen_on_off_help_summary_vzw);
        }

        removeHelpScreen(INDEX_SLIDEASIDE);

        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo("com.lge.gestureanswering",
                    PackageManager.GET_META_DATA);
            Log.d(TAG, "info.name = " + info.name);
        } catch (NameNotFoundException e) {
            removeHelpScreen(INDEX_ANSWERANINCOMINGCALL);
            removeHelpScreen(INDEX_FADEOUTRINGTONE);
        }
        Context mcontext = getActivity().getBaseContext();

        Log.i("Utils.isWifiOnly(mcontext)", ":" + Utils.isWifiOnly(mcontext));
        if (Utils.isWifiOnly(mcontext)) {

            removeHelpScreen(INDEX_SILENCEINCOMINGCALLS);
        }

        if (!SystemProperties.getBoolean("ro.lge.capp_privatemode", false)) {
            removeHelpScreen(INDEX_HIDEDISPLAY);
        }

        updateKnockonHelp(mcontext);

        removeHelpScreen(INDEX_TAKESCREENSHOT);
        removeHelpScreen(INDEX_SPEAKERPHONE);

        //removeHelpScreen(INDEX_HIDEDISPLAY);

        if (!CheckConfigValue(getActivity(), "config_knockon_app_shortcut_available")) {
            removeHelpScreen(INDEX_APP_SHORTCUT);
        }

        if (!CheckConfigValue(getActivity(), "config_knockon_media_volume_control_available")) {
            removeHelpScreen(INDEX_MEDIA_VOLUME_CONTROL);
        }
        SensorManager sensorManager =
                (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) == null) {
            removeHelpScreen(INDEX_MOVEHOME);
        }
        //[jongwon007.kim] KK Remove Move Home screen Item
        removeHelpScreen(INDEX_MOVEHOME);
        Log.i(TAG, "Sensor.TYPE_ORIENTATION :"
                + (sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) == null));

        //[jongwon007.kim][AnswerMe+][Remove Knockon]
        if (Utils.isUI_4_1_model(mcontext)) {
            if (!(getIndex(INDEX_ANSWERANINCOMINGCALL) == sNotFoundInde)) {
                mTextArray.set(getIndex(INDEX_ANSWERANINCOMINGCALL),
                        R.string.gesture_answer_summery);
                updateKnockonHelp(mcontext);
            }
        }

        if (ConfigHelper.isRemovedFadeoutRington(mcontext)) {
            if (!(getIndex(INDEX_FADEOUTRINGTONE) == sNotFoundInde)) {
                removeHelpScreen(INDEX_FADEOUTRINGTONE);
            }
        }
    }

    private void updateKnockonHelp(Context context) {

        if (!ConfigHelper.isSupportScreenOnOff(context)) {
            removeHelpScreen(INDEX_KNOCK);
        } else if (!ConfigHelper.isSupportKnockCode2_0(context)) {
            removeHelpScreen(INDEX_KNOCK);
        }

        if (!ConfigHelper.isSupportScreenOff(context)) {
            removeHelpScreen(INDEX_KNOCKOFF);
        }
    }

    public static boolean CheckConfigValue(Context context, String config_name) {
        Context con = null;
        final String pkgName = "com.lge.internal";
        final String res_string = config_name;
        Log.d(TAG, "res_string" + res_string);
        int id = 0;
        try {
            con = context.createPackageContext(pkgName, Context.MODE_PRIVATE);
            Resources res = null;
            if (null != con && null != con.getResources()) {
                res = con.getResources();
                id = res.getIdentifier(res_string, "bool", pkgName);
                return Boolean.parseBoolean(con.getText(id).toString());
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, " NameNotFoundException : " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, " NullPointerException : " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, " resourceException : " + e.getMessage());
        }
        return false;
    }

    public void removeHelpScreen(int imageresId) {
        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            return;
        }
        // [END]

        int i = 0;
        for (i = 0; i < mImageArray.size(); i++) {
            if (mImageArray.get(i) == imageresId) {
                mImageArray.remove(i);
                mTextArray.remove(i);
                mTitleArray.remove(i);
                mCOUNT = mCOUNT - 1;
            }
        }
    }

    public int getIndex(int imageresId) {
        for (int index = 0; index < mImageArray.size(); index++) {
            if (mImageArray.get(index) == imageresId) {
                return index;
            }
        }
        return sNotFoundInde;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        //PackageManager pm = getPackageManager();
        LayoutInflater mInflater = inflater;

        View rootview = mInflater.inflate(R.layout.gesture_help_main,
                container, false);

        mPrev = (Button)rootview.findViewById(R.id.gesture_previous_button);
        mPrev.setOnClickListener(this);

        mNext = (Button)rootview.findViewById(R.id.gesture_next_button);
        mNext.setOnClickListener(this);
        mPrev.setVisibility(View.GONE);
        mPageMark = (LinearLayout)rootview.findViewById(R.id.page_mark);
        mPageMark.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            mPageMark.setVisibility(View.GONE);
            mNext.setText(R.string.myguide_setting_button_text);

            if (mHelpGuidePosition == INDEX_KNOCKON_TITLE) {
                mNext.setText(R.string.sp_lg_software_help_btn_ok);
            }
        }
        // [END]

        mPager = (ViewPager)rootview.findViewById(R.id.pager);
        mPager.setAdapter(new BkPagerAdapter(getActivity()
                .getApplicationContext()));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //Log.e("JJJJJ", position+"");
                if (position < mCOUNT) {

                    try {
                        mPageMark.getChildAt(mPrevPosition).setBackgroundResource(
                                R.drawable.gesture_ani_navi_unactive);
                        mPageMark.getChildAt(position).setBackgroundResource(
                                R.drawable.gesture_ani_navi_active);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (position == mCOUNT - 1) {
                        //mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_done_NORMAL);
                    } else if (position == 0) {
                        mPrev.setVisibility(View.GONE);
                        //    mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
                        mNext.setText(R.string.sp_gesture_help_next_NOMAL);
                    } else {
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_gesture_help_next_NOMAL);
                    }
                    mPrevPosition = position;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffest, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        getActivity().getActionBar().setIcon(R.drawable.shortcut_gesture);
        initPageMark();

        isAnimated = false;

        return rootview;
    }

    private void initPageMark() {
        for (int i = 0; i < mCOUNT; i++)
        {
            ImageView iv = new ImageView(getActivity());
            LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            if (i == 0) {
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_active);
            } else {
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_unactive);
            }

            //lp.rightMargin = 4;
            iv.setLayoutParams(lp);
            //lp.setMargins(4, 0, 4, 0);
            mPageMark.addView(iv);
        }
        mPrevPosition = 0;
    }

    private class BkPagerAdapter extends PagerAdapter {
        private LayoutInflater mInflater;

        public BkPagerAdapter(Context con) {
            super();
            mInflater = LayoutInflater.from(con);
        }

        @Override
        public int getCount() {
            // [START][seungyeop.yeom][2014-02-28][User Help Guide]
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
                mCOUNT = 1;
            }
            // [END]

            return mCOUNT;
        }

        @Override
        public Object instantiateItem(View pager, int position) {

            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.gesture_help, null);

            title = (TextView)layout.findViewById(R.id.txt_View_1);
            iv = (ImageView)layout.findViewById(R.id.img_View_1);
            tv = (TextView)layout.findViewById(R.id.txt_View_2);

            // [START][seungyeop.yeom][2014-02-28][User Help Guide]
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
                title.setText(mTitleArray.get(mHelpGuidePosition));
                iv.setImageResource(mImageArray.get(mHelpGuidePosition));
                tv.setText(mTextArray.get(mHelpGuidePosition));
            } else {
                title.setText(mTitleArray.get(position));
                iv.setImageResource(mImageArray.get(position));
                tv.setText(mTextArray.get(position));
            }

            //    title.setTextSize((float) 19.33);
            //    tv.setTextSize((float) 15.67);

            Typeface tf = Typeface.defaultFromStyle(Typeface.BOLD);
            title.setTypeface(tf);

            ((ViewPager)pager).addView(layout, 0);
            return layout;

        }

        @Override
        public void destroyItem(View pager, int position, Object view) {
            ((ViewPager)pager).removeView((View)view);
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    @Override
    public void onClick(View v) {
        int view = v.getId();
        if (view == R.id.gesture_previous_button) {
            int cur = mPager.getCurrentItem();
            if (cur > 0) {
                //    Log.e("JJJJJ", "OnClick");
                //    Log.e("JJJJJ", "cur : " + cur);

                mPager.setCurrentItem(cur - 1, isAnimated);
            }
            else {
                //    mPrev.setVisibility(View.GONE);
                //finish();
            }
        }
        else if (view == R.id.gesture_next_button) {
            int cur = mPager.getCurrentItem();
            if (cur < mCOUNT - 1) {
                mPager.setCurrentItem(cur + 1, isAnimated);
                //    mPrev.setVisibility(View.VISIBLE);
            }
            else {
                finishFragment();
            }

            // [START][seungyeop.yeom][2014-02-28][User Help Guide]
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {

                if (mHelpGuidePosition == INDEX_KNOCKON_TITLE) {
                    finish();
                } else {
                    ComponentName c;
                    c = new ComponentName("com.android.settings",
                            "com.android.settings.Settings$GestureSettingsActivity");
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setComponent(c);
                    startActivity(i);
                    finishFragment();
                }

            }
            // [END]
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        // getActivity().recreate();
        mPageAdapter = new BkPagerAdapter(getActivity().getApplicationContext());
        mPager.setAdapter(mPageAdapter);
        mPager.setCurrentItem(mPrevPosition, isAnimated);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            Log.d("YSY", "itemId == android.R.id.home");
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
                Log.d("YSY", "itemId == android.R.id.home : finish()");
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
