package com.android.settings;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.content.res.Resources;
import android.os.SystemProperties;
import android.provider.Settings;
import com.android.settings.lgesetting.Config.ConfigHelper;

import android.view.MenuItem;
import com.android.settings.lgesetting.Config.Config;

public class OneHandOperationHelp extends SettingsPreferenceFragment implements OnClickListener {
    private static final String TAG = "OneHandOperationHelp";

    // [START][seungyeop.yeom][2014-02-28][User Help Guide]
    private int mHelpGuidePosition;
    final static String HELP_GUIDE_FUNCTUION = "help_guide_function";
    final static String HELP_GUIDE_NUMBER = "help_guide_number";
    // [END]

    private int COUNT;
    private int mPrevPosition;

    private ViewPager mPager;
    private LinearLayout mPageMark;
    private Button mPrev;
    private Button mNext;
    private TextView mTv;
    private TextView mTitle;
    private ImageView mIv;
    private boolean isAnimated;
    //private boolean SupportMessage;
    private BkPagerAdapter mPageAdapter;

    final static int INDEX_DIAL_KEYPAD = R.drawable.help_dial_keypad;
    final static int INDEX_CALL_SCREEN = R.drawable.help_call_screen;
    final static int INDEX_LG_KEYPAD = R.drawable.help_lg_keyboard;
    final static int INDEX_MESSAGE = R.drawable.help_message;
    final static int INDEX_LOCKSCREEN = R.drawable.help_lock_screen;
    final static int INDEX_SWIPEFRONT_TOUCHBUTTON = R.drawable.help_swipe_front_touch_buttons;
    final static int INDEX_FLOATINGFRONT_TOUCHBUTTON = R.drawable.help_answer_incoming_call;
    final static int INDEX_SIDE_KEY_CAMERA = R.drawable.help_camera_01;
    final static int INDEX_REAR_KEY_CAMERA = R.drawable.help_camera_02;
    final static int INDEX_MINI_VIEW = R.drawable.help_mini_view;
    //for hardkey
    final static int INDEX_NAVIGATION_BUTTON_TITLE
        = R.string.sp_one_hand_help_hardkey_less_navigation_button_NORMAL;
    final static int INDEX_NAVIGATION_BUTTON_TEXT
        = R.string.sp_one_hand_help_hardkey_navigation_button_NORMAL;
    final static int INDEX_NAVIGATION_BUTTON_IMAGE = R.drawable.help_swipe_front_touch_buttons;

    ArrayList<Integer> mImageArray;
    ArrayList<Integer> mTextArray;
    ArrayList<Integer> mTitleArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        ApplicationInfo info = null;

        mImageArray = new ArrayList<Integer>(
                Arrays.asList(R.drawable.help_dial_keypad,
                        R.drawable.help_call_screen,
                        R.drawable.help_lg_keyboard,
                        R.drawable.help_message,
                        R.drawable.help_lock_screen,
                        R.drawable.help_mini_view,
                        R.drawable.help_swipe_front_touch_buttons, //swipe front
                        R.drawable.help_camera_01,
                        R.drawable.help_camera_02));

        mTextArray = new ArrayList<Integer>(
                Arrays.asList(R.string.sp_one_hand_help3_dial_keypad_NORMAL,
                        R.string.sp_one_hand_help_call_screen_NORMAL_2,
                        R.string.sp_one_hand_help3_lg_keyboard_NORMAL,
                        R.string.sp_one_hand_help2_message_NORMAL,
                        R.string.sp_one_hand_help3_lockscreen_NORMAL,
                        R.string.sp_one_hand_mini_view_help_NORMAL,
                        R.string.sp_one_hand_help_front_touch_buttons_NORMAL,
                        R.string.sp_one_hand_help_camera_NORMAL,
                        R.string.sp_one_hand_help_camera_NORMAL));

        mTitleArray = new ArrayList<Integer>(
                Arrays.asList(R.string.sp_one_hand_dial_keypad_title_NORMAL,
                        R.string.sp_one_hand_call_screen_title_NORMAL,
                        R.string.sp_one_hand_lg_keyboard_title_NORMAL,
                        R.string.sp_Message_NORMAL,
                        R.string.sp_one_hand_lockscreen_title_NORMAL,
                        R.string.one_hand_mini_view_title,
                        R.string.sp_one_hand_front_touch_buttons_title_NORMAL,
                        R.string.sp_one_hand_camera_title_NORMAL,
                        R.string.sp_one_hand_camera_title_NORMAL));

        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            mHelpGuidePosition = getActivity().getIntent().getIntExtra(HELP_GUIDE_NUMBER, 0);
            Log.d("YSY", "help Guide Position : " + mHelpGuidePosition);
            getActivity().getActionBar().setTitle(R.string.smart_tips_title);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // [END]

        try {
            info = pm.getApplicationInfo("com.lge.navibtn",
                    PackageManager.GET_META_DATA);
            Log.i(TAG, "info = " + info.name);
            if (!Utils.supportNavigationButtons()) { //hardkey
                //hakgyu98.kim.. later
                //setHelpScreenResExceptTitle(INDEX_NAVIGATION_BUTTON_TITLE, INDEX_NAVIGATION_BUTTON_IMAGE, INDEX_NAVIGATION_BUTTON_TEXT);
            }
        } catch (NameNotFoundException e) {
            Log.d(TAG, "removeHelpScreen INDEX_SWIPEFRONT_TOUCHBUTTON");

            removeHelpScreen(INDEX_FLOATINGFRONT_TOUCHBUTTON);
        }

        removeHelpScreen(INDEX_SWIPEFRONT_TOUCHBUTTON);

        removeHelpScreen(INDEX_CALL_SCREEN);

        //SupportMessage = Settings.System.getInt(getActivity().getContentResolver(),
        //        "mms_support_one_hand_keyboard", 1) == 1 ? true : false;

        //Log.i("SupportMessage",
        //        "SupportMessage"
        //                + Settings.System.getInt(getActivity().getContentResolver(),
        //                        "mms_support_one_hand_keyboard", 1));

        // [seungyeop.yeom][2013-12-27] delete help page of message
        removeHelpScreen(INDEX_MESSAGE);

        // [START][seungyeop.yeom][2014-03-31] delete category of LG Keyboard for JP country
        if ("JP".equals(Config.getCountry())) {
            removeHelpScreen(INDEX_LG_KEYPAD);
        }
        // [END]

        try {

            info = pm.getApplicationInfo("com.lge.onehandcontroller",
                    PackageManager.GET_META_DATA);
            Log.i(TAG, "info = " + info.name);

            if (ConfigHelper.isSupportSlideCover(getActivity())) {
                Log.i(TAG, "support config_using_slide_cover : miniview removed.");
                removeHelpScreen(INDEX_MINI_VIEW);
            }
        } catch (NameNotFoundException e) {
            removeHelpScreen(INDEX_MINI_VIEW);
            Log.i(TAG, "MINI VIEW APP NOT FOUND");
        }

        // [seungyeop.yeom][2013-12-27] delete help page of camera if This model exist key of back side
        removeHelpScreen(INDEX_SIDE_KEY_CAMERA);
        removeHelpScreen(INDEX_REAR_KEY_CAMERA);

        COUNT = mImageArray.size();
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
            }
        }

    }

    public void setHelpScreenResExceptImage(int imageresId, int titleresid, int textresid) {
        int i = 0;
        for (i = 0; i < mImageArray.size(); i++) {
            if (mImageArray.get(i) == imageresId) {
                mTextArray.set(i, textresid);
                mTitleArray.set(i, titleresid);
            }
        }
    }

    public void setHelpScreenResExceptTitle(int titleresid, int imageresId, int textresid) {
        int i = 0;
        for (i = 0; i < mTitleArray.size(); i++) {
            if (mTitleArray.get(i) == titleresid) {
                mTextArray.set(i, textresid);
                mImageArray.set(i, imageresId);
            }
        }
    }

    public void setHelpScreenResWithImage(int oldimageresId, int newimageresid, int titleresid,
            int textresid) {
        int i = 0;
        for (i = 0; i < mImageArray.size(); i++) {
            if (mImageArray.get(i) == oldimageresId) {
                mImageArray.set(i, newimageresid);
                mTextArray.set(i, textresid);
                mTitleArray.set(i, titleresid);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        mPageAdapter = new BkPagerAdapter(getActivity().getApplicationContext());
        mPager.setAdapter(mPageAdapter);
        mPager.setCurrentItem(mPrevPosition, isAnimated);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // initialize the inflater
        if (container == null) {
            return null;
        }

        LayoutInflater mInflater = inflater;

        View rootView = mInflater.inflate(R.layout.one_hand_help_main,
                container, false);

        mPrev = (Button)rootView.findViewById(R.id.gesture_previous_button);
        mPrev.setOnClickListener(this);

        mNext = (Button)rootView.findViewById(R.id.gesture_next_button);
        mNext.setOnClickListener(this);
        mPrev.setVisibility(View.GONE);
        mPageMark = (LinearLayout)rootView.findViewById(R.id.page_mark);
        mPageMark.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            mPageMark.setVisibility(View.GONE);
            mNext.setText(R.string.myguide_setting_button_text);
        }
        // [END]

        mPager = (ViewPager)rootView.findViewById(R.id.pager);
        mPageAdapter = new BkPagerAdapter(getActivity().getApplicationContext());
        mPager.setAdapter(mPageAdapter);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position < COUNT) {
                    // [jongtak0920.kim] Fix WBT issue
                    try {

                        mPageMark.getChildAt(mPrevPosition).setBackgroundResource(
                                R.drawable.gesture_ani_navi_unactive);
                        mPageMark.getChildAt(position).setBackgroundResource(
                                R.drawable.gesture_ani_navi_active);

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    if (position == (COUNT - 1)) {
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_done_NORMAL);
                    } else if (position == 0) {
                        mPrev.setVisibility(View.GONE);
                        mNext.setText(R.string.sp_gesture_help_next_NOMAL);
                    } else {
                        Log.d("YSY", "onPageSelected else");
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

        return rootView;
    }

    private void initPageMark() {
        for (int i = 0; i < COUNT; i++)
        {
            ImageView mIv = new ImageView(getActivity());
            LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            if (i == 0) {
                mIv.setBackgroundResource(R.drawable.gesture_ani_navi_active);
            } else {
                mIv.setBackgroundResource(R.drawable.gesture_ani_navi_unactive);
            }

            //lp.rightMargin = 4;
            mIv.setLayoutParams(lp);
            //lp.setMargins(4, 0, 4, 0);
            mPageMark.addView(mIv);
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
                COUNT = 1;
            }
            // [END]
            return COUNT;

        }

        @Override
        public Object instantiateItem(View pager, int position) {

            mInflater = (LayoutInflater)getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.one_hand_help, null);

            mTitle = (TextView)layout.findViewById(R.id.txt_View_1);
            mIv = (ImageView)layout.findViewById(R.id.img_View_1);
            mTv = (TextView)layout.findViewById(R.id.txt_View_2);

            // [START][seungyeop.yeom][2014-02-28][User Help Guide]
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
                mTitle.setText(mTitleArray.get(mHelpGuidePosition));
                mIv.setImageResource(mImageArray.get(mHelpGuidePosition));
                mTv.setText(mTextArray.get(mHelpGuidePosition));
                mTv.setContentDescription(mContext.getResources()
                        .getString(mTextArray.get(mHelpGuidePosition))
                        .toLowerCase());
            } else {
                mTitle.setText(mTitleArray.get(position));
                mIv.setImageResource(mImageArray.get(position));
                mTv.setText(mTextArray.get(position));
                mTv.setContentDescription(mContext.getResources()
                        .getString(mTextArray.get(position))
                        .toLowerCase());
            }
            // [END]

            mTitle.setSelected(true);   // for marquee effect

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
            if (cur < COUNT - 1) {
                mPager.setCurrentItem(cur + 1, isAnimated);
            }
            else {
                Log.d("YSY", "onClick()");
                finishFragment();
            }

            // [START][seungyeop.yeom][2014-02-28][User Help Guide]
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
                ComponentName c;
                c = new ComponentName("com.android.settings",
                        "com.android.settings.Settings$OneHandOperationSettingsActivity");
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(c);
                startActivity(i);
                finishFragment();
            }
            // [END]
        }
    }

    public static boolean CheckOHOConfigValue(Context context, String config_name) {
        Context con = null;
        final String pkgName = "com.lge.systemui";
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
