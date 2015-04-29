package com.android.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class DualWindowHelp extends SettingsPreferenceFragment implements
        OnClickListener {

    // [START][seungyeop.yeom][2014-02-28][User Help Guide]
    private int mHelpGuidePosition;
    final static String HELP_GUIDE_FUNCTUION = "help_guide_function";
    final static String HELP_GUIDE_NUMBER = "help_guide_number";
    // [END]

    private int mPageCount = 0;
    private int mPrevPosition;
    private ViewPager mPager;
    private LinearLayout mPageMark;
    private Button mPrev, mNext;
    private TextView tv, title;
    private ImageView iv;
    private boolean isAnimated;
    private BkPagerAdapter mPageAdapter;

    private ArrayList<Integer> mImageArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.drawable.help_start_dual_window,
                    R.drawable.help_select_apps,
                    R.drawable.help_use_menus,
                    R.drawable.help_split_view_smart_tips));

    private ArrayList<Integer> mTextArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.dualwindow_help_desc_1,
                    R.string.dualwindow_help_desc2,
                    R.string.dualwindow_help_desc3,
                    R.string.dualwindow_splitview_help));
    private ArrayList<Integer> mTitleArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.use_dual_window,
                    R.string.app_split_view_help_two_fixed,
                    R.string.app_split_view_help_three,
                    R.string.multitasking_split_view_title));

    public DualWindowHelp() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        getActivity().getActionBar().setIcon(R.drawable.ic_settings_multitasking_splitview);
        //getActivity().getActionBar().setTitle(R.string.app_name_dual_window);
        
        if (checkThinkFreeVersion(getActivity()) == false) {
        	mImageArray.remove(mImageArray.size() - 1);
        	mTextArray.remove(mTextArray.size() - 1);
        	mTitleArray.remove(mTitleArray.size() - 1);
        }
        
        mPageCount = mImageArray.size();

        // [START][seungyeop.yeom][2014-02-28][User Help Guide]
        if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
            mHelpGuidePosition = getActivity().getIntent().getIntExtra(HELP_GUIDE_NUMBER, 0);
            Log.d("YSY", "help Guide Position : " + mHelpGuidePosition);
            getActivity().getActionBar().setTitle(R.string.smart_tips_title);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // [END]

    }

    public static boolean checkThinkFreeVersion(Context context) {
    	String packageName = "com.lge.thinkfreeviewer";
    	boolean result = false;
    	try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
			Log.d("kimyow", "ThinkFree Viewer version = "
					+ pi.versionName + ", versionCode = " + pi.versionCode);
			if (pi.versionCode >= 401000010) { // http://mlm.lge.com/di/browse/UXMALVOE-664
				result = true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.gesture_help_main,
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
        }
        // [END]

        mPager = (ViewPager)rootview.findViewById(R.id.pager);
        mPager.setAdapter(new BkPagerAdapter(getActivity()
                .getApplicationContext()));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position < mPageCount) {

                    try {
                        mPageMark.getChildAt(mPrevPosition).setBackgroundResource(
                                R.drawable.gesture_ani_navi_unactive);
                        mPageMark.getChildAt(position).setBackgroundResource(
                                R.drawable.gesture_ani_navi_active);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (position == mPageCount - 1) {
                        //mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_done_NORMAL);
                    } else if (position == 0) {
                        mPrev.setVisibility(View.GONE);
                        //      mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
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

    private void initPageMark() {
        for (int i = 0; i < mPageCount; i++) {
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
                mPageCount = 1;
            }
            // [END]

            return mPageCount;
        }

        @Override
        public Object instantiateItem(View pager, int position) {

            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = null;

            try {
                layout = mInflater.inflate(R.layout.gesture_help, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

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

            if (position == 2 || mHelpGuidePosition == 2) {
                SpannableStringBuilder stringBuilder = makeContentDescription();
                tv.setText(stringBuilder);
            }

            if (position == 0) {
                iv.setContentDescription(mContext.getString(R.string.pair_appicon_guide)
                        + mContext.getString(R.string.single_appicon_guide));
            } else {
                iv.setContentDescription(mContext.getString(R.string.split_button_guide));
            }

            //            title.setTextSize((float)19.33);
            //            tv.setTextSize((float)15.67);

            Typeface tf = Typeface.defaultFromStyle(Typeface.BOLD);
            title.setTypeface(tf);

            //            tv.setVisibility(View.GONE);

            ((ViewPager)pager).addView(layout, 0);
            return layout;

        }

        private SpannableStringBuilder makeContentDescription() {
            String desc = getString(R.string.dualwindow_help_desc3);
            desc += "\r\n";
            desc += getString(R.string.dualwindow_help_desc_sub1); // dual_window_use_menus_swich
            desc += "\r\n";
            desc += getString(R.string.dualwindow_help_desc_sub2); // dual_window_use_menus_list
            desc += "\r\n";
            desc += getString(R.string.dualwindow_help_desc_sub3); // dual_window_use_menus_fullscreen
            desc += "\r\n";
            desc += getString(R.string.dualwindow_help_desc_sub4); // dual_window_use_menus_close

            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(desc);

            String tmp = desc;
            int insertIndex = tmp.indexOf("+");

            setSpan(stringBuilder, insertIndex, R.drawable.img_dual_window_use_menus_button);

            tmp = desc.substring(insertIndex + 2);
            insertIndex += 2;
            insertIndex += tmp.indexOf("+");

            setSpan(stringBuilder, insertIndex, R.drawable.img_dual_window_use_menus_swich);

            tmp = desc.substring(insertIndex + 2);
            insertIndex += 2;
            insertIndex += tmp.indexOf("+");

            setSpan(stringBuilder, insertIndex, R.drawable.img_dual_window_use_menus_list);

            tmp = desc.substring(insertIndex + 2);
            insertIndex += 2;
            insertIndex += tmp.indexOf("+");

            setSpan(stringBuilder, insertIndex, R.drawable.img_dual_window_use_menus_fullscreen);

            tmp = desc.substring(insertIndex + 2);
            insertIndex += 2;
            insertIndex += tmp.indexOf("+");

            setSpan(stringBuilder, insertIndex, R.drawable.img_dual_window_use_menus_close);

            return stringBuilder;
        }

        private void setSpan(SpannableStringBuilder stringBuilder, int index, int resId) {
            stringBuilder.setSpan(new ImageSpan(getActivity(), resId),
                    index - 1, index + 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
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
        } else if (view == R.id.gesture_next_button) {
            int cur = mPager.getCurrentItem();
            if (cur < mPageCount - 1) {
                mPager.setCurrentItem(cur + 1, isAnimated);
            } else {
                finishFragment();
            }

            // [START][seungyeop.yeom][2014-02-28][User Help Guide]
            if (getActivity().getIntent().hasExtra(HELP_GUIDE_FUNCTUION)) {
                ComponentName c;
                c = new ComponentName("com.android.settings",
                        "com.android.settings.Settings$DualWindowSettingsActivity");
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(c);
                startActivity(i);
                finishFragment();
            }
            // [END]
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
