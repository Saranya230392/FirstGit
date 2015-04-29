package com.lge.ime_help;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.SpannableStringBuilder;
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

import com.lge.handwritingime.R;

public class HelpActivity extends Activity implements OnClickListener {
    private static final String TAG = "LGHWIMEHelpActivity";
    
    private int mPrevPosition;
    private boolean mAnimated;

    private Context mContext = null;
    private ViewPager mPager = null;
    private LinearLayout mPageMark = null;
    private TextView mTextView = null;
    private TextView mTitle = null;
    private ImageView mImgView = null;
    private Button mPrev = null;
    private Button mNext = null;

    private PageChangeListener mPageChangeListener = null;
    private ImageGetter mImageGetter = null;
    private BkPagerAdapter mPagerAdapter = null;
    
    private int mGuidePopupListCount;
    private int[] mImageArray;
    private String[] mTextArray;
    private String[] mTitleArray;

    
    /**
     * Initialize Data
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ime_help_main);
        
        setResourceArray();
        initView();
        initPageMark();

        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_help_activity);
        }
        mAnimated = true;
    }

    private void initPageMark() {
        for (int i = 0; i < mGuidePopupListCount; i++) {
            ImageView imgView = new ImageView(HelpActivity.this);
            LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            float density = mContext.getResources().getDisplayMetrics().density;
            lp.leftMargin = lp.rightMargin = (int)(1 * density);

            if (i == 0)
                imgView.setBackgroundResource(R.drawable.help_ic_page_view_on);
            else
                imgView.setBackgroundResource(R.drawable.help_ic_page_view_off);

            imgView.setLayoutParams(lp);
            mPageMark.addView(imgView);
        }
        mPrevPosition = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mContext != null) {
            mContext = null;
        }
        if (mPager != null) {
            mPager.setAdapter(null);
            mPager.setOnPageChangeListener(null);
            mPager = null;
            mPagerAdapter.clear();
        }
        if (mPageMark != null) {
            mPageMark = null;
        }
        if (mPrev != null) {
            mPrev.setOnClickListener(null);
            mPrev = null;
        }
        if (mNext != null) {
            mNext.setOnClickListener(null);
            mNext = null;
        }
        if (mTextView != null) {
            mTextView = null;
        }
        if (mTitle != null) {
            mTitle = null;
        }
        if (mImgView != null) {
            mImgView = null;
        }        
        if (mPageChangeListener != null) {
            mPageChangeListener.clear();
            mPageChangeListener = null;
        }
        if (mImageGetter != null) {
            mImageGetter.clear();
            mImageGetter = null;
        }
        if (mImageArray != null) {
            mImageArray = null;
        }
        if (mTextArray != null) {
            mTextArray = null;
        }
        if (mTitleArray != null) {
            mTitleArray = null;
        }
    }
    
    private static class BkPagerAdapter extends PagerAdapter {
        private WeakReference<HelpActivity> mMainActivity;
        private LayoutInflater mInflater;
        
        BkPagerAdapter(HelpActivity keyboardGuide) {
            mMainActivity = new WeakReference<HelpActivity>(keyboardGuide);
            keyboardGuide.mContext = keyboardGuide.getApplicationContext();
        }

        private HelpActivity get() {
            if (mMainActivity == null) {
                return null;
            } else {
                return mMainActivity.get();
            }
        }

        /**
         * get Guide Popup List Count
         * 
         * @param ListCount
         */
        @Override
        public int getCount() {
            HelpActivity keyboardGuide = get();
            if (keyboardGuide == null) {
                Log.e(TAG, "failure : keyboardGuide is null.");
                return 0;
            }
            
            return keyboardGuide.mGuidePopupListCount;
        }

        /**
         * Initialize Item
         * 
         * @param pager, position 
         * Iniialize Item
         */
        @Override
        public Object instantiateItem(ViewGroup pager, int position) {
            HelpActivity keyboardGuide = get();
            if (keyboardGuide == null) {
                Log.e(TAG, "failure : keyboardGuide is null.");
                return null;
            }
            
            mInflater = (LayoutInflater) keyboardGuide.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.ime_help, null);
            if (layout == null) {
                Log.e(TAG, "failure : layout is null.");
                return null;
            }
            
            keyboardGuide.mTitle = (TextView) layout.findViewById(R.id.title_View);
            keyboardGuide.mImgView = (ImageView) layout.findViewById(R.id.img_View);
            keyboardGuide.mTextView = (TextView) layout.findViewById(R.id.txt_View);

            if (keyboardGuide.mTitle == null || keyboardGuide.mImgView == null || keyboardGuide.mTextView == null) {
                Log.e(TAG, "failure : mTitle=" + keyboardGuide.mTitle + " mImgView=" + keyboardGuide.mImgView +
                		" mTextView1=" + keyboardGuide.mTextView);
                return null;
            }

            if (position > -1 && position < keyboardGuide.mGuidePopupListCount) {
                keyboardGuide.mTitle.setText(keyboardGuide.mTitleArray[position]);
                keyboardGuide.mTextView.setText(keyboardGuide.makeMessageIcons( keyboardGuide.mTextArray[position]));
                keyboardGuide.mPager.addView(layout, 0);
                keyboardGuide.mImgView.setImageResource(keyboardGuide.mImageArray[position]);
            }
            return layout;
        }

        /**
         * Destroy Item
         * @param ViewGroup, position, view
         * Destroy Item
         */
        @Override
        public void destroyItem(ViewGroup pager, int position, Object view) {
            HelpActivity keyboardGuide = get();
            if (keyboardGuide == null) {
                Log.e(TAG, "failure : keyboardGuide is null.");
                return;
            }
            
            keyboardGuide.mPager.removeView((View) view);
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
        
        void clear() {
            if (mMainActivity != null) {
                mMainActivity.clear();
                mMainActivity = null;
            }
        }
    }

    private static class PageChangeListener implements OnPageChangeListener {
        private WeakReference<HelpActivity> mMainActivity;

        PageChangeListener(HelpActivity keyboardGuide) {
            mMainActivity = new WeakReference<HelpActivity>(keyboardGuide);
        }

        private HelpActivity get() {
            if (mMainActivity == null) {
                return null;
            } else {
                return mMainActivity.get();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        /**
         * Page Scroll State Change process.
         * @param position
         */
        @Override
        public void onPageSelected(int position) {
            HelpActivity keyboardGuide = get();
            if (keyboardGuide == null) {
                Log.e(TAG, "failure : keyboardGuide is null.");
                return;
            }

            if (position < keyboardGuide.mGuidePopupListCount) {
                try {
                    keyboardGuide.mPageMark.getChildAt(keyboardGuide.mPrevPosition).setBackgroundResource(
                            R.drawable.help_ic_page_view_off);
                    keyboardGuide.mPageMark.getChildAt(position).setBackgroundResource(
                            R.drawable.help_ic_page_view_on);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (position == keyboardGuide.mGuidePopupListCount - 1) {
                    keyboardGuide.mPrev.setVisibility(View.VISIBLE);
                    keyboardGuide.mNext.setText(R.string.sp_done_NORMAL);
                } else if (position == 0) {
                    keyboardGuide.mPrev.setVisibility(View.GONE);
                    keyboardGuide.mNext.setText(R.string.ime_guide_next_NOMAL);
                } else {
                    keyboardGuide.mPrev.setVisibility(View.VISIBLE);
                    keyboardGuide.mNext.setText(R.string.ime_guide_next_NOMAL);
                }
                keyboardGuide.mPrevPosition = position;
            }
        }

        void clear() {
            if (mMainActivity != null) {
                mMainActivity.clear();
                mMainActivity = null;
            }
        }
    }

    private static class ImageGetter implements Html.ImageGetter {
        private WeakReference<HelpActivity> mMainActivity;

        ImageGetter(HelpActivity keyboardGuide) {
            mMainActivity = new WeakReference<HelpActivity>(keyboardGuide);
        }
        
        private HelpActivity get() {
            if (mMainActivity == null) {
                return null;
            } else {
                return mMainActivity.get();
            }
        }
        
        @Override
        public Drawable getDrawable(String arg0) {
        	HelpActivity keyboardGuide = get();
            if (keyboardGuide == null) {
                Log.e(TAG, "failure : keyboardGuide is null.");
                return null;
            }

            Resources res = keyboardGuide.getResources();
            if (res == null) {
                Log.e(TAG, "failure : Resources is null.");
                return null;
            }

            Drawable d = null;
            int resId = res.getIdentifier(arg0.replaceAll("\\.png", ""), "drawable", keyboardGuide.getPackageName());
            if (resId > 0) {
            	d = res.getDrawable(resId);
            }
            
            if (d != null) {
                int textSize = (int) (keyboardGuide.mTextView.getTextSize()+12);
                int width = textSize * d.getIntrinsicWidth() / d.getIntrinsicHeight();
                d.setBounds(0, 0, width, textSize);
            }
            return d;
        }
        
        void clear() {
            if (mMainActivity != null) {
                mMainActivity.clear();
                mMainActivity = null;
            }
        }
    }

    /**
     * Click event handling
     * @param view
     */
    @Override
    public void onClick(View v) {
        if (v == null) {
            Log.e(TAG, "failure : v is null.");
            return;
        }

        int viewId = v.getId();

        if (viewId == R.id.ime_previous_button) {
            int cur = mPager.getCurrentItem();
            if (cur > 0) {
                mPager.setCurrentItem(cur - 1, mAnimated);
            }
        } else if (viewId == R.id.ime_next_button) {
            int cur = mPager.getCurrentItem();

            if (cur < mGuidePopupListCount - 1) {
                mPager.setCurrentItem(cur + 1, mAnimated);
            } else {
                finish();
            }
        }
    }

    private CharSequence makeMessageIcons(String str) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        mImageGetter = new ImageGetter(this);
        if (mImageGetter == null) {
            Log.e(TAG, "failure : mImageGetter is null.");
            return str;
        }
    	buf.append(Html.fromHtml(str, mImageGetter, null));
    	return buf;
    }

    private void initView() {
        mPageChangeListener = new PageChangeListener(this);
        mPagerAdapter = new BkPagerAdapter(this);
        mPrev = (Button) findViewById(R.id.ime_previous_button);
        mNext = (Button) findViewById(R.id.ime_next_button);
        mPageMark = (LinearLayout) findViewById(R.id.page_mark);
        mPager = (ViewPager) findViewById(R.id.pager);

        if (mPageChangeListener == null) {
            Log.e(TAG, "failure : mPageChangeListener is null.");
            return;
        }
        if (mPrev == null || mNext == null || mPageMark == null || mPager == null) {
            Log.e(TAG, "failure : mPrev=" + mPrev + " mNext=" + mNext + " mPageMark=" + mPageMark +
            		" mPager=" + mPager);
            return;
        }

        mNext.setOnClickListener(this);
        mPrev.setOnClickListener(this);
        mPrev.setVisibility(View.GONE);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(mPageChangeListener);
    }

    private void setResourceArray() {
        int pos;

        Resources resources = getResources();
        if (resources == null) {
            Log.e(TAG, "failure: getResources() returned a null reference.");
            return;
        }

        String[] guidePopupList = resources.getStringArray(R.array.keyboard_guide_popup_list);
        String[] guidePopupDesc = resources.getStringArray(R.array.keyboard_guide_popup_desc);
        String[] guidePopupTitle = resources.getStringArray(R.array.keyboard_guide_popup_title);

        if (guidePopupList == null || guidePopupDesc == null || guidePopupTitle == null) {
            Log.e(TAG, "failure : guidePopupList=" + Arrays.toString(guidePopupList) +
                    " guidePopupDesc=" + Arrays.toString(guidePopupDesc) +
            		" guidePopupTitle=" + Arrays.toString(guidePopupTitle));
            return;
        }

        final int guidePopupListCount = guidePopupList.length;
        mGuidePopupListCount = guidePopupListCount;

        pos = mGuidePopupListCount;

        mImageArray = new int[pos];
        mTextArray = new String[pos];
        mTitleArray = new String[pos];
        int tmp = 0;

        for (int i = 0; i < mGuidePopupListCount; i++) {
            mImageArray[tmp] = resources.getIdentifier(guidePopupList[i], "drawable",
                    getPackageName());
            mTextArray[tmp] = guidePopupDesc[i];
            mTitleArray[tmp] = guidePopupTitle[i];

            if (tmp < pos)
                tmp += 1;
        }
        mGuidePopupListCount = pos;
        
    }
    
    /**
     * Option Item Select event handling
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null) {
            Log.e(TAG, "failure : item is null");
            return false;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
