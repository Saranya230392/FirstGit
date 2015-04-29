package com.android.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
//import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class SlideAsideHelp extends SettingsPreferenceFragment implements
        OnClickListener {
    private int COUNT = 1;

    final static int INDEX_TAKESCREENSHOT = R.drawable.help_take_screenshot;

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

    private ArrayList<Integer> mImageArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.drawable.help_slide_aside
                    )); //move home screen items

    private ArrayList<Integer> mTextArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.gesture_help_desc_slide_aside
                    )); //move home screen items
    private ArrayList<Integer> mTitleArray = new ArrayList<Integer>(
            Arrays.asList(
                    R.string.sp_settings_slideaside
                    )); //move home screen items

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setIcon(R.drawable.ic_settings_multitasking_splitview);
        getActivity().getActionBar().setTitle(R.string.sp_settings_slideaside);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

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
        mPager = (ViewPager)rootview.findViewById(R.id.pager);
        mPager.setAdapter(new BkPagerAdapter(getActivity()
                .getApplicationContext()));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //Log.e("JJJJJ", position+"");
                if (position < COUNT) {

                    try {
                        mPageMark.getChildAt(mPrevPosition).setBackgroundResource(
                                R.drawable.gesture_ani_navi_unactive);
                        mPageMark.getChildAt(position).setBackgroundResource(
                                R.drawable.gesture_ani_navi_active);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (position == COUNT - 1) {
                        //mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_done_NORMAL);
                    } else if (position == 0) {
                        mPrev.setVisibility(View.GONE);
                        //	mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
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

        mNext.setText(R.string.dlg_ok);

        initPageMark();

        isAnimated = false;

        return rootview;
    }

    private void initPageMark() {
        for (int i = 0; i < COUNT; i++)
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
            iv.setVisibility(View.INVISIBLE);
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
            return COUNT;
        }

        @Override
        public Object instantiateItem(View pager, int position) {

            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.gesture_help, null);

            title = (TextView)layout.findViewById(R.id.txt_View_1);
            iv = (ImageView)layout.findViewById(R.id.img_View_1);
            tv = (TextView)layout.findViewById(R.id.txt_View_2);

            title.setText(mTitleArray.get(position));
            iv.setImageResource(mImageArray.get(position));
            tv.setText(mTextArray.get(position));

            title.setTextSize((float)19.33);
            tv.setTextSize((float)15.67);

            Typeface tf = Typeface.defaultFromStyle(Typeface.BOLD);
            title.setTypeface(tf);
            title.setVisibility(View.INVISIBLE);

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
                //	Log.e("JJJJJ", "OnClick");
                //	Log.e("JJJJJ", "cur : " + cur);

                mPager.setCurrentItem(cur - 1, isAnimated);
            }
            else {
                //	mPrev.setVisibility(View.GONE);
                //finish();
            }
        }
        else if (view == R.id.gesture_next_button) {
            int cur = mPager.getCurrentItem();
            if (cur < COUNT - 1) {
                mPager.setCurrentItem(cur + 1, isAnimated);
                //	mPrev.setVisibility(View.VISIBLE);
            }
            else {
                finishFragment();
            }
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

}
