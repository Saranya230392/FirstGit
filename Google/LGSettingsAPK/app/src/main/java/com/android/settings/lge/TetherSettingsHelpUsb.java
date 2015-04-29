package com.android.settings.lge;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.settings.R;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.Utils;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class TetherSettingsHelpUsb extends Activity
        implements OnClickListener {

    private int COUNT = 2;
    private int mPrevPosition;

    private ViewPager mPager;
    private LinearLayout mPageMark;
    private Button mPrev;
    private Button mNext;
    private boolean isAnimated;

    private ImageView mHelpImage;
    private TextView mHelpTitle;
    private TextView mHelpGuide1;
    private TextView mHelpGuide2;
    private TextView mHelpGuide3;
    private TextView mHelpGuide4;
    private TextView mHelpGuide5;
    private TextView mHelpGuide1Vzw;
    private TextView mHelpGuide2Vzw;
    private ImageButton mPrevButton;

    private boolean mVzw = Config.getOperator().equals(Config.VZW);

    //private static final String SAVE_KEY_CURRENT_PAGE = "save_key_current_page";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if ("SBM".equals(Config.getOperator())
                    && "user".equals(Build.TYPE)) {
            finish();
        }
        String strNewCo = SystemProperties.get("ro.build.target_operator_ext");
        setContentView(R.layout.tether_settings_help_usb_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mVzw) {
                actionBar.setTitle(R.string.sp_mobile_broadband_connection_NORMAL);
            } else if (Config.getCountry().equals("US") && Config.getOperator().equals("TMO")) {
                if ("MPCS_TMO".equals(strNewCo)) {
                    actionBar.setIcon(R.drawable.shortcut_hotspot_newco);
                } else {
                    actionBar.setIcon(R.drawable.shortcut_hotspot_tmus);
                }
            } else if (Config.getOperator().equals("MPCS")) {
                actionBar.setIcon(R.drawable.shortcut_hotspot_mpcs);
            }
            //            actionBar.setTitle(R.string.tether_settings_title_usb);
            //            actionBar.setIcon(R.drawable.usb);
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
                //Log.e("JJJJJ", position+"");
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utils.getResources().getBoolean(R.bool.config_settings_search_enable)) {
            MenuInflater inflater_search = getMenuInflater();
            inflater_search.inflate(R.menu.settings_search, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void initWidget(View layout) {
        String url_en = "http://mpcs.me/hot/";
        String url_es = "http://mpcs.es/hot/";

        String strNewCo = SystemProperties.get("ro.build.target_operator_ext");
        mHelpImage = (ImageView)layout.findViewById(R.id.usb_tethering_help_image);
        mHelpTitle = (TextView)layout.findViewById(R.id.usb_tethering_help_title);
        mHelpGuide1 = (TextView)layout.findViewById(R.id.usb_tethering_help_guide1);
        mHelpGuide2 = (TextView)layout.findViewById(R.id.usb_tethering_help_guide2);
        mHelpGuide3 = (TextView)layout.findViewById(R.id.usb_tethering_help_guide3);
        mHelpGuide4 = (TextView)layout.findViewById(R.id.usb_tethering_help_guide4);
        mHelpGuide5 = (TextView)layout.findViewById(R.id.usb_tethering_help_guide5);
        mHelpGuide1Vzw = (TextView)layout.findViewById(R.id.usb_tethering_help_guide1_vzw);
        mHelpGuide2Vzw = (TextView)layout.findViewById(R.id.usb_tethering_help_guide2_vzw);

        if (Utils.isUI_4_1_model(this)) {
            mHelpGuide1.setText(R.string.usb_tether_help_global_01_ex);
            mHelpGuide2.setText(R.string.usb_tether_help_global_02_ex);
            mHelpGuide3.setText(R.string.usb_tether_help_global_03_ex);
            mHelpGuide3.setVisibility(View.GONE);
        } else {
            mHelpTitle.setVisibility(View.GONE);
        }

        if (mVzw) {
            mHelpGuide1.setVisibility(View.GONE);
            mHelpGuide2.setVisibility(View.GONE);
            mHelpGuide3.setVisibility(View.GONE);
            mHelpGuide4.setVisibility(View.GONE);
            mHelpGuide5.setVisibility(View.GONE);
            mHelpGuide1Vzw.setText(R.string.sp_usb_tether_help_sub_title_NORMAL);

            Spanned spanned = Html.fromHtml("-");
            String contents = null;
            contents = spanned.toString() + "  "
                    + getString(R.string.sp_usb_tethering_help_content1_NORMAL) + "\n\n"
                    + spanned.toString() + "  "
                    + getString(R.string.sp_usb_tethering_help_content3_NORMAL) + "\n\n"
                    + spanned.toString() + "  "
                    + getString(R.string.sp_usb_tethering_help_content5_NORMAL);

            mHelpGuide2Vzw.setText(contents);
        } else if (Config.getCountry().equals("US") && Config.getOperator().equals("TMO")
                || Config.getOperator().equals("MPCS")) {
            Locale locale = Locale.getDefault();
            String urlText = "";
            CharSequence textlink = "";

            //String contents3 = getString(R.string.usb_tethering_help_guide_mpcs_tmo3) + "\n";	
            //String contents4 = getString(R.string.usb_tethering_help_guide_tmus4) + "\n";		
            //String contents5 = getString(R.string.usb_tethering_help_guide_tmus5);	

            if (locale.getLanguage().equals("ES")) {
                urlText = " <a href='"
                        + url_es
                        + "'>"
                        + getResources().getString(
                                R.string.usb_tethering_help_guide_mpcs_tmo_clickhere) + "</a>";
            } else {
                urlText = " <a href='"
                        + url_en
                        + "'>"
                        + getResources().getString(
                                R.string.usb_tethering_help_guide_mpcs_tmo_clickhere) + "</a>";
            }

            if ("MPCS_TMO".equals(strNewCo)
                    || Config.getOperator().equals("MPCS")) {
                if (Utils.isUI_4_1_model(this)) {
                    textlink = Html.fromHtml(getResources().getString(
                            R.string.usb_tether_help_global_01_ex)
                            + "<br><br>"
                            + getResources().getString(R.string.usb_tethering_help_guide_mpcs_tmo2,
                                    urlText));
                } else {
                    textlink = Html.fromHtml(getResources().getString(
                            R.string.usb_tether_help_global_01)
                            + "<br><br>"
                            + getResources().getString(R.string.usb_tethering_help_guide_mpcs_tmo2,
                                    urlText));
                }
            } else {
                if (Utils.isUI_4_1_model(this)) {
                    textlink = Html.fromHtml(getResources().getString(
                            R.string.usb_tether_help_global_01_ex)
                            + "<br><br>"
                            + getResources().getString(R.string.usb_tethering_help_guide_tmus2));
                } else {
                    textlink = Html.fromHtml(getResources().getString(
                            R.string.usb_tether_help_global_01)
                            + "<br><br>"
                            + getResources().getString(R.string.usb_tethering_help_guide_tmus2));
                }
            }

            mHelpGuide1.setText(textlink);
            mHelpGuide1.setMovementMethod(LinkMovementMethod.getInstance());
            mHelpGuide1Vzw.setVisibility(View.GONE);
            mHelpGuide2Vzw.setVisibility(View.GONE);
            if (Utils.isUI_4_1_model(this)) {
                mHelpTitle.setVisibility(View.GONE);
                mHelpGuide4.setText(R.string.usb_tether_help_global_04_tmo_mpcs);
            }
        }
        else {
            mHelpGuide1Vzw.setVisibility(View.GONE);
            mHelpGuide2Vzw.setVisibility(View.GONE);
        }
    }

    private void initPageMark() {
        for (int i = 0; i < COUNT; i++) {
            ImageView iv = new ImageView(TetherSettingsHelpUsb.this);
            LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            if (i == 0) {
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_active);
            }
            else {
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_unactive);
            }
            iv.setLayoutParams(lp);
            mPageMark.addView(iv);
        }
        mPrevPosition = 0;
    }

    private void setUI(int position) {
        if (position == 0) {
            if (mVzw) {
                mHelpGuide1Vzw.setVisibility(View.VISIBLE);
                mHelpGuide2Vzw.setVisibility(View.GONE);
            }
            else {
                mHelpGuide1.setVisibility(View.VISIBLE);
                mHelpGuide2.setVisibility(View.GONE);
                mHelpGuide3.setVisibility(View.GONE);
                mHelpGuide4.setVisibility(View.GONE);
                mHelpGuide5.setVisibility(View.GONE);
            }
            mHelpImage.setVisibility(View.VISIBLE);
        }
        else if (position == 1) {
            if (mVzw) {
                mHelpGuide1Vzw.setVisibility(View.GONE);
                mHelpGuide2Vzw.setVisibility(View.VISIBLE);
            }
            else {
                mHelpGuide1.setVisibility(View.GONE);
                mHelpGuide2.setVisibility(View.VISIBLE);
                mHelpGuide3.setVisibility(View.VISIBLE);
                mHelpGuide4.setVisibility(View.VISIBLE);
                mHelpGuide5.setVisibility(View.VISIBLE);
                if (Utils.isUI_4_1_model(getApplicationContext())) {
                    mHelpGuide3.setVisibility(View.GONE);
                }

            }
            mHelpImage.setVisibility(View.GONE);
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
            View layout = mInflater.inflate(R.layout.tether_settings_help_usb, null);

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

        if (item.getItemId() == R.id.search) {
            Intent search_intent = new Intent();
            search_intent.setAction("com.lge.settings.SETTINGS_SEARCH");
            search_intent.putExtra("search", true);
            startActivity(search_intent);
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
        } else if (view == R.id.next_button) {
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
