package com.android.settings.lge;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.UsbSettingsControl;
import com.android.settings.lgesetting.Config.Config;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup.LayoutParams;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.android.settings.SettingsPreferenceFragment;

public class ConnectivityHelperPopup extends SettingsPreferenceFragment
        implements OnClickListener {
    final static private String TAG = "ConnectivityHelperPopup";
    private LinearLayout mChargeLayout;
    private LinearLayout mMassStorageLayout;
    private LinearLayout mMtpLinearLayout;
    private LinearLayout mInternetConnectionLayout;
    private LinearLayout mTetherLinearLayout;
    private LinearLayout mLgSoftwareLinearLayout;
    private LinearLayout mPtpLinearLayout;
    private ViewPager mPager;
    private LinearLayout mPageMark;
    private Button mPrev;
    private Button mNext;
    private boolean isAnimated;
    private int mTotalPage;
    private int mPrevPosition;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (container == null) {
            return null;
        }
        View rootView = inflater.inflate(R.layout.usb_settings_help_main, null);

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity().getApplicationContext())) {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } else {
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        mTotalPage = dividePage();
        Log.d(TAG, "mTotalPage=" + mTotalPage);

        mPrev = (Button)rootView.findViewById(R.id.previous_button);
        mPrev.setOnClickListener(this);
        mNext = (Button)rootView.findViewById(R.id.next_button);
        mNext.setOnClickListener(this);
        mPrev.setVisibility(View.GONE);
        mPageMark = (LinearLayout)rootView.findViewById(R.id.page_mark);
        mPageMark.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        mPager = (ViewPager)rootView.findViewById(R.id.pager);
        mPager.setAdapter(new BkPagerAdapter(getActivity()
                .getApplicationContext()));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position < mTotalPage) {
                    View view = mPageMark.getChildAt(mPrevPosition);
                    if (view != null) {
                        view.setBackgroundResource(R.drawable.gesture_ani_navi_unactive);
                    }
                    view = mPageMark.getChildAt(position);
                    if (view != null) {
                        view.setBackgroundResource(R.drawable.gesture_ani_navi_active);
                    }

                    if (position == (mTotalPage - 1)) {
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_done_NORMAL);
                    } else if (position == 0) {
                        mPrev.setVisibility(View.GONE);
                        mNext.setText(R.string.sp_gesture_help_next_NOMAL);
                    } else {
                        mPrev.setVisibility(View.VISIBLE);
                        mNext.setText(R.string.sp_gesture_help_next_NOMAL);
                    }
                    mPrevPosition = position;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffest,
                    int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        isAnimated = false;
        initPageMark();
        return rootView;
    }

    private void initPageMark() {
        for (int i = 0; i < mTotalPage; i++) {
            ImageView iv = new ImageView(getActivity());
            LinearLayout.LayoutParams lp = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            if (i == 0) {
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_active);
            } else {
                iv.setBackgroundResource(R.drawable.gesture_ani_navi_unactive);
            }

            iv.setLayoutParams(lp);
            mPageMark.addView(iv);
        }
        mPrevPosition = 0;
    }

    private int dividePage() {
        int totalPage = 3;
        if (Config.getOperator().equals(Config.VZW)) {
            if (UsbSettingsControl.isMassStorageSupport(getActivity()
                    .getApplicationContext())) {
                totalPage++;
            }
            totalPage--;
        } else {
            if (UsbSettingsControl.isMassStorageSupport(getActivity()
                    .getApplicationContext())) {
                totalPage++;
            }
            totalPage--;
        }
        return totalPage;
    }

    private class BkPagerAdapter extends PagerAdapter {
        private LayoutInflater mInflater;

        public BkPagerAdapter(Context con) {
            super();
            mInflater = LayoutInflater.from(con);
        }

        @Override
        public int getCount() {
            return mTotalPage;
        }

        @Override
        public Object instantiateItem(View pager, int position) {

            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = mInflater.inflate(R.layout.usb_connection_type_help,
                    null);

            initLayout(view);
            updatePageView(position);

            ((ViewPager)pager).addView(view, 0);
            return view;

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

    private void updatePageView(int pageNum) {
        mChargeLayout.setVisibility(View.GONE);
        mMassStorageLayout.setVisibility(View.GONE);
        mMtpLinearLayout.setVisibility(View.GONE);
        mInternetConnectionLayout.setVisibility(View.GONE);
        mTetherLinearLayout.setVisibility(View.GONE);
        mLgSoftwareLinearLayout.setVisibility(View.GONE);
        mPtpLinearLayout.setVisibility(View.GONE);

        if (mMtpLinearLayout.isEnabled() || mPtpLinearLayout.isEnabled()) {
            if (Config.getOperator().equals(Config.VZW)) {
                switch (pageNum) {
                case 0:
                    if (mMassStorageLayout.isEnabled()) {
                        mChargeLayout.setVisibility(View.VISIBLE);
                        mMassStorageLayout.setVisibility(View.VISIBLE);
                    } else {
                        mChargeLayout.setVisibility(View.VISIBLE);
                        mMtpLinearLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    if (mMassStorageLayout.isEnabled()) {
                        mMtpLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        mPtpLinearLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case 2:
                    if (mMassStorageLayout.isEnabled()) {
                        mInternetConnectionLayout.setVisibility(View.VISIBLE);
                    } else {
                        mPtpLinearLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case 3:
                    if (mMassStorageLayout.isEnabled()) {
                        mPtpLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        // No Page
                    }
                    break;
                default:
                    break;
                }
            } else {
                switch (pageNum) {
                case 0:
                    if (mMassStorageLayout.isEnabled()) {
                        mChargeLayout.setVisibility(View.VISIBLE);
                        mMassStorageLayout.setVisibility(View.VISIBLE);
                    } else {
                        mChargeLayout.setVisibility(View.VISIBLE);
                        mMtpLinearLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    if (mMassStorageLayout.isEnabled()) {
                        mMtpLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        if (mTetherLinearLayout.isEnabled()) {
                            mTetherLinearLayout.setVisibility(View.VISIBLE);
                        } else {
                            if (mLgSoftwareLinearLayout.isEnabled()) {
                                mLgSoftwareLinearLayout
                                        .setVisibility(View.VISIBLE);
                                mPtpLinearLayout.setVisibility(View.VISIBLE);
                            } else {
                                mPtpLinearLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    break;
                case 2:
                    if (mMassStorageLayout.isEnabled()) {
                        if (mTetherLinearLayout.isEnabled()) {
                            mTetherLinearLayout.setVisibility(View.VISIBLE);
                        } else {
                            if (mLgSoftwareLinearLayout.isEnabled()) {
                                mLgSoftwareLinearLayout
                                        .setVisibility(View.VISIBLE);
                                mPtpLinearLayout.setVisibility(View.VISIBLE);
                            } else {
                                mPtpLinearLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        if (mLgSoftwareLinearLayout.isEnabled()) {
                            mLgSoftwareLinearLayout.setVisibility(View.VISIBLE);
                            mPtpLinearLayout.setVisibility(View.VISIBLE);
                        } else {
                            mPtpLinearLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case 3:
                    if (mMassStorageLayout.isEnabled()) {
                        if (mLgSoftwareLinearLayout.isEnabled()) {
                            mLgSoftwareLinearLayout.setVisibility(View.VISIBLE);
                            mPtpLinearLayout.setVisibility(View.VISIBLE);
                        } else {
                            mPtpLinearLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // No Page
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getActivity().onBackPressed();
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
            } else {
            }
        } else if (view == R.id.next_button) {
            int cur = mPager.getCurrentItem();
            if (cur < mTotalPage - 1) {
                mPager.setCurrentItem(cur + 1, isAnimated);
            } else {
                finish();
            }
        }
    }

    private void initLayout(View view) {
        mChargeLayout = (LinearLayout)view.findViewById(R.id.charge_help);
        mMassStorageLayout = (LinearLayout)view
                .findViewById(R.id.mass_storage_help);
        mMtpLinearLayout = (LinearLayout)view.findViewById(R.id.mtp_help);
        mInternetConnectionLayout = (LinearLayout)view
                .findViewById(R.id.internet_connection_help);
        mTetherLinearLayout = (LinearLayout)view.findViewById(R.id.tether_help);
        mLgSoftwareLinearLayout = (LinearLayout)view
                .findViewById(R.id.lg_software_help);
        mPtpLinearLayout = (LinearLayout)view.findViewById(R.id.ptp_help);

        if (Config.getOperator().equals(Config.ATT)
                && Config.getCountry().equals("US")) {
            TextView software = (TextView)mLgSoftwareLinearLayout
                    .findViewById(R.id.lg_software_title);
            software.setText(R.string.sp_pc_software_NORMAL);
            Typeface tf = Typeface.defaultFromStyle(Typeface.BOLD);
            software.setTypeface(tf);
        }

        mTetherLinearLayout.setVisibility(View.GONE);
        mTetherLinearLayout.setEnabled(false);
        if (Utils.isSupportUSBMultipleConfig(getActivity())) {
            mLgSoftwareLinearLayout.setVisibility(View.GONE);
            mLgSoftwareLinearLayout.setEnabled(false);
        }

        if (Config.getOperator().equals(Config.VZW)) {
            TextView mPtp_title = (TextView)mPtpLinearLayout
                    .findViewById(R.id.ptp_help_title);
            mPtp_title.setText(R.string.usb_ptp_title);
        } else {
            mInternetConnectionLayout.setVisibility(View.GONE);
            mInternetConnectionLayout.setEnabled(false);
        }

        if (!"VZW".equals(Config.getOperator())) {
            TextView usb_ptp_desc1 = (TextView)mPtpLinearLayout
                    .findViewById(R.id.ptp_help_desc1);
            usb_ptp_desc1.setText(R.string.sp_connectivity_help_18_NORMAL_ex);
            if (!"ATT".equals(Config.getOperator())) {
                TextView usb_mtp_title = (TextView)mMtpLinearLayout
                        .findViewById(R.id.mtp_title);
                usb_mtp_title.setText(R.string.sp_usbtype_mtp_title_NORMAL_ex);
            }
        }

        if (!UsbSettingsControl.isMtpSupport(getActivity()
                .getApplicationContext())) {
            mMtpLinearLayout.setVisibility(View.GONE);
            mMtpLinearLayout.setEnabled(false);
            mPtpLinearLayout.setVisibility(View.GONE);
            mPtpLinearLayout.setEnabled(false);
        }
        if (!UsbSettingsControl.isMassStorageSupport(getActivity()
                .getApplicationContext())) {
            mMassStorageLayout.setVisibility(View.GONE);
            mMassStorageLayout.setEnabled(false);
        }
    }
}
