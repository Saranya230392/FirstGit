package com.android.settings.powersave;

import com.android.settings.R;
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
import android.os.Build;

import android.view.MenuItem;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

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

public class BatterySaverTips extends SettingsPreferenceFragment implements
        OnClickListener {

    // [START][seungyeop.yeom][2014-02-28][User Help Guide]
    private int mHelpGuidePosition;
    final static String HELP_GUIDE_FUNCTUION = "help_guide_function";
    final static String HELP_GUIDE_NUMBER = "help_guide_number";
    // [END]

    private int mCOUNT = 2;

    final static int INDEX_SMART = R.drawable.help_settings_smart_battery;
    final static int INDEX_BATTERY = R.drawable.help_settings_power_saver;

    private int mPrevPosition;
    private ViewPager mPager;
    private LinearLayout mPageMark;
    private Button mPrev;
    private Button mNext;
    private TextView tv;
    private TextView title;
    private ImageView iv;
    private boolean isAnimated;
    private BkPagerAdapter mPageAdapter;
    private boolean mSupportSmartBattery;
    private Context mContext;

    private ArrayList<Integer> mImageArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.drawable.help_settings_smart_battery,
                    R.drawable.help_settings_power_saver
                    ));

    private ArrayList<Integer> mTextArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.sp_power_save_help_smart_NORMAL,
                    R.string.
                    sp_battery_saver_tip_description_ui4_2
                    ));
    private ArrayList<Integer> mTitleArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.sp_power_save_battery_smart_charge,
                    R.string.sp_power_saver_NORMAL
                    ));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            mHelpGuidePosition = getActivity().getIntent().getIntExtra(HELP_GUIDE_NUMBER, 0);
            Log.d("YSY", "help Guide Position : " + mHelpGuidePosition);
            getActivity().getActionBar().setTitle(R.string.smart_tips_title);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // [END]

        mSupportSmartBattery = Config.getFWConfigBool(mContext,
                com.lge.R.bool.config_smart_battery,
                "com.lge.R.bool.config_smart_battery");
        if (!mSupportSmartBattery) {
            removeHelpScreen(INDEX_SMART);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        LayoutInflater mInflater = inflater;

        View rootview = mInflater.inflate(R.layout.battery_help_main,
                container, false);

        mPrev = (Button)rootview.findViewById(R.id.gesture_previous_button);
        mPrev.setOnClickListener(this);

        mNext = (Button)rootview.findViewById(R.id.gesture_next_button);
        mNext.setOnClickListener(this);
        mPrev.setVisibility(View.GONE);
        mPageMark = (LinearLayout)rootview.findViewById(R.id.page_mark);
        mPageMark.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        if (!mSupportSmartBattery) {
            mPageMark.setVisibility(View.GONE);
            mNext.setText(R.string.sp_lg_software_help_btn_ok);
        }

        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            mPageMark.setVisibility(View.GONE);
            mNext.setText(R.string.myguide_setting_button_text);
        }
        // [END]

        mPager = (ViewPager)rootview.findViewById(R.id.pager);
        mPager.setAdapter(new BkPagerAdapter(getActivity()
                .getApplicationContext()));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
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

        initPageMark();

        isAnimated = false;

        return rootview;
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

            lp.rightMargin = 4;
            iv.setLayoutParams(lp);
            lp.setMargins(4, 0, 4, 0);
            mPageMark.addView(iv);
        }
        mPrevPosition = 0;
    }

    private class BkPagerAdapter extends PagerAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public BkPagerAdapter(Context con) {
            super();
            mContext = con;
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
            View layout = mInflater.inflate(R.layout.battery_help, null);

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
                mPager.setCurrentItem(cur - 1, isAnimated);
            }
            else {

            }
        }
        else if (view == R.id.gesture_next_button) {
            int cur = mPager.getCurrentItem();
            if (cur < mCOUNT - 1) {
                mPager.setCurrentItem(cur + 1, isAnimated);
            }
            else {
                finishFragment();
            }

            // [START][seungyeop.yeom][2014-02-28][User Help Guide]
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
                ComponentName c;
                c = new ComponentName("com.android.settings",
                        "com.android.settings.Settings$BatterySettingsActivity");
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(c);
                startActivity(i);
                finishFragment();
            }
            // [END]
        }
    }

}
