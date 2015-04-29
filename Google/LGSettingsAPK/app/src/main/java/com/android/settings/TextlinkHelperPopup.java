package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
//import android.text.Html;
//import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;

public class TextlinkHelperPopup extends Activity
        implements OnClickListener {

    public static final String TAG = "TextlinkHelperPopup";

    private int COUNT = 4;
    private int mPrevPosition;

    private ViewPager mPager;
    private LinearLayout mPageMark;
    private Button mPrev;
    private Button mNext;
    private boolean isAnimated;

    private ImageView mHelpImage01;
    private ImageView mHelpImage02;
    private ImageView mHelpImage03;

    //private TextView mHelpGuide1;
    //private TextView mHelpGuide2;
    //private TextView mHelpGuide3;
    //private TextView mHelpGuide4;
    //private TextView mHelpGuide1Vzw;
    //private TextView mHelpGuide2Vzw;
    private ImageButton mPrevButton;

    private LinearLayout category_introduction;
    private LinearLayout category_howtouse;
    private LinearLayout category_tips;
    private LinearLayout category_language;

    private TextView mHelpGuide1_title;
    private TextView mHelpGuide2_title;
    private TextView mHelpGuide3_title;
    private TextView mHelpGuide4_title;

    //description
    private TextView mHelpGuide1_1;
    private TextView mHelpGuide2_1;
    private TextView mHelpGuide2_2;
    private TextView mHelpGuide2_3;
    private TextView mHelpGuide2_4;
    private TextView mHelpGuide3_1;
    private TextView mHelpGuide4_1;
    private TextView mHelpGuide4_2;

    private boolean mVzw = Config.getOperator().equals(Config.VZW);

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.textlink_settings_help_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mVzw) {
                actionBar.setTitle(R.string.sp_mobile_broadband_connection_NORMAL);
            }
            //actionBar.setTitle(R.string.tethering_help_button_text);
            actionBar.setIcon(R.drawable.text_link);

            //actionBar.setTitle(R.string.textlink_guide_title);
            //actionBar.setIcon(R.drawable.shortcut_language);
        }

        mPrev = (Button)findViewById(R.id.previous_button);
        mPrev.setOnClickListener(this);

        mNext = (Button)findViewById(R.id.next_button);
        mNext.setOnClickListener(this);
        mPrev.setVisibility(View.GONE);
        mPageMark = (LinearLayout)findViewById(R.id.page_mark);
        mPageMark.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(new BkPagerAdapter(getApplicationContext()));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                Log.d(TAG, "onCreate onPageSelected position = " + position + " COUNT = " + COUNT);

                if (position < COUNT) {
                    View view = mPageMark.getChildAt(mPrevPosition);
                    if (view != null) {

                        view.setBackgroundResource(R.drawable.gesture_ani_navi_unactive);
                    }
                    view = mPageMark.getChildAt(position);
                    if (view != null) {
                        view.setBackgroundResource(R.drawable.gesture_ani_navi_active);
                    }

                    if (position == (COUNT - 1)) {
                        //mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_done_NORMAL);
                    } else if (position == 0) {
                        mPrev.setVisibility(View.GONE);
                        //  mPrev.setText(R.string.sp_gesture_help_previous_NOMAL);
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
    }

    private void initWidget(View layout) {

        //introduction
        category_introduction = (LinearLayout)layout.findViewById(R.id.category_introduction);

        //how to use
        category_howtouse = (LinearLayout)layout.findViewById(R.id.category_howtouse);

        //tips
        category_tips = (LinearLayout)layout.findViewById(R.id.category_tips);

        //language
        category_language = (LinearLayout)layout.findViewById(R.id.category_language);

        //title
        mHelpGuide1_title = (TextView)layout.findViewById(R.id.text_link_help_guard1_title);
        mHelpGuide2_title = (TextView)layout.findViewById(R.id.text_link_help_guard2_title);
        mHelpGuide3_title = (TextView)layout.findViewById(R.id.text_link_help_guard3_title);
        mHelpGuide4_title = (TextView)layout.findViewById(R.id.text_link_help_guard4_title);

        //image
        mHelpImage01 = (ImageView)layout.findViewById(R.id.help_guide_introduction_01_eng);
        mHelpImage02 = (ImageView)layout.findViewById(R.id.help_guide_how_to_use_01_eng);
        mHelpImage03 = (ImageView)layout.findViewById(R.id.help_guide_tip_01_eng);

        //mHelpImage02 = (ImageView) layout.findViewById(R.id.usb_tethering_help_image);

        //description
        mHelpGuide1_1 = (TextView)layout.findViewById(R.id.text_link_help_guard1_1);
        mHelpGuide2_1 = (TextView)layout.findViewById(R.id.text_link_help_guard2_1);
        mHelpGuide2_2 = (TextView)layout.findViewById(R.id.text_link_help_guard2_2);
        mHelpGuide2_3 = (TextView)layout.findViewById(R.id.text_link_help_guard2_3);
        mHelpGuide2_4 = (TextView)layout.findViewById(R.id.text_link_help_guard2_4);
        mHelpGuide3_1 = (TextView)layout.findViewById(R.id.text_link_help_guard3);
        mHelpGuide4_1 = (TextView)layout.findViewById(R.id.text_link_help_guard4_1);
        mHelpGuide4_2 = (TextView)layout.findViewById(R.id.text_link_help_guard4_2);

        category_introduction.setVisibility(View.GONE);
        category_howtouse.setVisibility(View.GONE);
        category_tips.setVisibility(View.GONE);
        category_language.setVisibility(View.GONE);

        mHelpGuide1_title.setVisibility(View.GONE);
        mHelpGuide2_title.setVisibility(View.GONE);
        mHelpGuide3_title.setVisibility(View.GONE);
        mHelpGuide4_title.setVisibility(View.GONE);

        mHelpGuide1_1.setVisibility(View.GONE);
        mHelpGuide2_1.setVisibility(View.GONE);
        mHelpGuide2_2.setVisibility(View.GONE);
        mHelpGuide2_3.setVisibility(View.GONE);
        mHelpGuide2_4.setVisibility(View.GONE);
        mHelpGuide3_1.setVisibility(View.GONE);
        mHelpGuide4_1.setVisibility(View.GONE);
        mHelpGuide4_2.setVisibility(View.GONE);

        /*
        if (mVzw) {
            Log.d(TAG, "initWidget if mVzw = " + mVzw );
            mHelpGuide1.setVisibility(View.GONE);
            mHelpGuide2.setVisibility(View.GONE);
            mHelpGuide3.setVisibility(View.GONE);
            mHelpGuide4.setVisibility(View.GONE);

            //mHelpGuide1Vzw.setText(R.string.sp_usb_tether_help_sub_title_NORMAL);

            Spanned spanned = Html.fromHtml("&bull;");

            String contents = spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content1_NORMAL) + "\n\n"
                                + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content2_NORMAL) + "\n\n"
                                + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content3_NORMAL) + "\n\n"
                                + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content4_NORMAL) + "\n\n"
                                + spanned.toString() + "  " + getString(R.string.sp_usb_tethering_help_content5_NORMAL);

            mHelpGuide2Vzw.setText(contents);
        }
        else {
            Log.d(TAG, "initWidget else mVzw = " + mVzw );
            //mHelpGuide1Vzw.setVisibility(View.GONE);
            //mHelpGuide2Vzw.setVisibility(View.GONE);
        }
        */
    }

    private void initPageMark() {
        for (int i = 0; i < COUNT; i++) {

            Log.d(TAG, "initPageMark COUNT = " + COUNT);

            ImageView iv = new ImageView(TextlinkHelperPopup.this);
            LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            if (i == 0) {
                Log.d(TAG, "initPageMark  if (i == 0) i = " + i);
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_active);
            }
            else {
                Log.d(TAG, "initPageMark else i = " + i);
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_unactive);
            }
            lp.rightMargin = 4;
            iv.setLayoutParams(lp);
            lp.setMargins(4, 0, 4, 0);
            mPageMark.addView(iv);
        }
        mPrevPosition = 0;
    }

    private void setUI(int position) {
        Log.d(TAG, "setUI position = " + position);

        mHelpImage01.setVisibility(View.GONE);
        mHelpImage02.setVisibility(View.GONE);
        mHelpImage03.setVisibility(View.GONE);

        mHelpGuide1_1.setVisibility(View.GONE);
        mHelpGuide2_title.setVisibility(View.GONE);
        mHelpGuide3_title.setVisibility(View.GONE);
        mHelpGuide4_title.setVisibility(View.GONE);

        //mHelpImage.setVisibility(View.VISIBLE);

        mHelpGuide1_1.setVisibility(View.GONE);
        mHelpGuide2_1.setVisibility(View.GONE);
        mHelpGuide2_2.setVisibility(View.GONE);
        mHelpGuide2_3.setVisibility(View.GONE);
        mHelpGuide2_4.setVisibility(View.GONE);
        mHelpGuide3_1.setVisibility(View.GONE);
        mHelpGuide4_1.setVisibility(View.GONE);
        mHelpGuide4_2.setVisibility(View.GONE);

        if (position == 0) {
            Log.d(TAG, "setUI position ==0 position = " + position);
            category_introduction.setVisibility(View.VISIBLE);

            mHelpGuide1_title.setVisibility(View.VISIBLE);
            mHelpImage01.setVisibility(View.VISIBLE);
            mHelpGuide1_1.setVisibility(View.VISIBLE);
        }
        else if (position == 1) {
            Log.d(TAG, "setUI position == 1 position = " + position);

            category_howtouse.setVisibility(View.VISIBLE);
            mHelpGuide2_title.setVisibility(View.VISIBLE);
            //mHelpImage01.setVisibility(View.VISIBLE);
            mHelpImage02.setVisibility(View.VISIBLE);

            mHelpGuide2_1.setVisibility(View.VISIBLE);
            mHelpGuide2_2.setVisibility(View.VISIBLE);
            mHelpGuide2_3.setVisibility(View.VISIBLE);
            mHelpGuide2_4.setVisibility(View.VISIBLE);
        }
        else if (position == 2) {
            Log.d(TAG, "setUI position == 2 position = " + position);

            category_tips.setVisibility(View.VISIBLE);
            mHelpGuide3_title.setVisibility(View.VISIBLE);
            mHelpImage03.setVisibility(View.VISIBLE);
            mHelpGuide3_1.setVisibility(View.VISIBLE);
        }
        else if (position == 3) {
            Log.d(TAG, "setUI position == 3 position = " + position);

            category_language.setVisibility(View.VISIBLE);
            mHelpGuide4_title.setVisibility(View.VISIBLE);
            mHelpGuide4_1.setVisibility(View.VISIBLE);
            mHelpGuide4_2.setVisibility(View.VISIBLE);
        }
        else {
            Log.d(TAG, "setUI else  position = " + position);
        }

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
            View layout = mInflater.inflate(R.layout.textlink_settings_help, null);

            Log.d(TAG, "instantiateItem position = " + position);

            initWidget(layout);
            setUI(position);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int view = v.getId();
        if (view == R.id.previous_button) {
            int cur = mPager.getCurrentItem();
            if (cur > 0) {
                mPager.setCurrentItem(cur - 1, isAnimated);
            }
            else {
            }
        }
        else if (view == R.id.next_button) {
            int cur = mPager.getCurrentItem();
            if (cur < COUNT - 1) {
                mPager.setCurrentItem(cur + 1, isAnimated);
            }
            else {
                finish();
            }
        }
    }
}
