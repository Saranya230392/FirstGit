package com.android.settings.lge;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.os.SystemProperties;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.ModelFeatureUtils;
import com.android.settings.SettingsPreferenceFragment;

public class EmotionalLEDEffectTab extends SettingsPreferenceFragment implements
        TabHost.OnTabChangeListener {
    static final String TAG = "EmotionalLEDEffectTab";
    private final static int TAB_POS_FRONT = 0;
    private final static int TAB_POS_REAR = 1;
    private final static int ACTIONBAR_TAB_NUM = 2;

    private TabHost mTabHost;
    private TabWidget mTabWidget;
    private ViewPager mViewPager = null;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, EmotionalLEDEffectTab.TabInfo>();
    private EmotionalLEDPagerAdapter mPagerAdapter = null;
    private Fragment mFragment;
    private View mTabTitleView;
    private TextView mTitle;
    private TextView mFront;
    private TextView mRear;
    private boolean mHasBackLED;

    private class TabInfo {
        private String tag;

        TabInfo(String tag, Class<?> clazz, Bundle args) {
            this.tag = tag;
        }
    }

    class TabFactory implements TabContentFactory {
        private final Context mContext;

        public TabFactory(Context context) {
            mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }

    }

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        mHasBackLED = Config.getFWConfigBool(getActivity(), com.lge.R.bool.config_hasBackLed,
                           "com.lge.R.bool.config_hasBackLed");
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (!mHasBackLED) {
            FragmentTransaction ft = getActivity().getFragmentManager()
                    .beginTransaction();
            if (mFragment == null) {
                mFragment = Fragment.instantiate(getActivity(),
                        EmotionalLEDEffectTabFront.class.getName());
            }
            ft.replace(R.id.emotional_temp_content, mFragment);
            ft.commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        final View view;
        if (mHasBackLED) {
            view = inflater.inflate(R.layout.emotional_led_tab_settings, container, false);
            mTabWidget = (TabWidget)view.findViewById(android.R.id.tabs);
            mTabHost = (TabHost)view.findViewById(android.R.id.tabhost);
            mTabHost.setup();
            TabInfo mTabInfo = null;
            AddTab(getActivity(),
                    mTabHost,
                    mTabHost.newTabSpec("front")
                            .setIndicator(
                                    createTabView(getActivity(), mTabWidget,
                                            getString(R.string.display_led_front_tab))),
                    mTabInfo = new TabInfo("front", EmotionalLEDEffectTabFront.class,
                            savedInstanceState));
            mapTabInfo.put(mTabInfo.tag, mTabInfo);
            AddTab(getActivity(),
                    mTabHost,
                    mTabHost.newTabSpec("rear")
                            .setIndicator(
                                    createTabView(getActivity(), mTabWidget,
                                            getString(R.string.sp_emotional_led_back_led_ex))),
                    mTabInfo = new TabInfo("rear", EmotionalLEDEffectTabRear.class,
                            savedInstanceState));
            mapTabInfo.put(mTabInfo.tag, mTabInfo);
            mTabHost.setOnTabChangedListener((OnTabChangeListener)this);

            List<Fragment> fragments = new Vector<Fragment>();
            fragments.add(Fragment.instantiate(getActivity(),
                    EmotionalLEDEffectTabFront.class.getName()));
            fragments.add(Fragment.instantiate(getActivity(),
                    EmotionalLEDEffectTabRear.class.getName()));

            if (mViewPager == null) {
                mViewPager = (ViewPager)view.findViewById(R.id.pager);
            }
            mPagerAdapter = new EmotionalLEDPagerAdapter(getActivity(), mViewPager, mTabHost);
            //mViewPager.setOffscreenPageLimit(1);

            setTextTypeFace(mTabHost.getCurrentTabTag());
        } else {
            view = inflater.inflate(R.layout.emotional_container, container, false);
            //return viewList;
        }
        return view;
    }

    private View createTabView(Context context, TabWidget mTab, String text) {
        mTabTitleView = LayoutInflater.from(context).inflate(R.layout.emotional_tabs_title, mTab,
                false);
        mTitle = (TextView)mTabTitleView.findViewById(R.id.tabText);
        mTitle.setText(text);
        return mTabTitleView;
    }

    private void AddTab(Activity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec 
        tabSpec.setContent(new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }

    public void onTabChanged(String tag) {
        Log.d("chan", "onTabChanged");
        int pos = mTabHost.getCurrentTab();
        mViewPager.setCurrentItem(pos);
        setTextTypeFace(tag);
    }

    public void setTextTypeFace(String tag) {
        if (Utils.isUI_4_1_model(getActivity())) {
            if ("front".equals(tag)) {
                mFront = (TextView)mTabHost.getCurrentTabView().findViewById(R.id.tabText);
                mFront.setTypeface(null, Typeface.BOLD);
                if (mRear != null) {
                    mRear.setTypeface(null, Typeface.NORMAL);
                }
            } else if ("rear".equals(tag)) {
                mRear = (TextView)mTabHost.getCurrentTabView().findViewById(R.id.tabText);
                mRear.setTypeface(null, Typeface.BOLD);
                if (mFront != null) {
                    mFront.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }

    public static class EmotionalLEDPagerAdapter extends FragmentPagerAdapter implements
            ViewPager.OnPageChangeListener {
        private ViewPager mViewPager;
        private EmotionalLEDEffectTabFront mFrontFragment;
        private EmotionalLEDEffectTabRear mRearFragment;
        private TabHost mTabHost;

        public EmotionalLEDPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
        }

        public EmotionalLEDPagerAdapter(Activity activity,
                ViewPager pager, TabHost mTab) {
            // TODO Auto-generated constructor stub
            super(activity.getFragmentManager());
            mTabHost = mTab;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        @Override
        public Fragment getItem(int position) {
            // TODO Auto-generated method stub
            Fragment frag = null;
            switch (position) {
            case TAB_POS_FRONT:
                if (mFrontFragment == null) {
                    mFrontFragment = new EmotionalLEDEffectTabFront();
                }
                frag = mFrontFragment;
                break;
            case TAB_POS_REAR:
                if (mRearFragment == null) {
                    mRearFragment = new EmotionalLEDEffectTabRear();
                }
                frag = mRearFragment;
                break;
            default:
                break;
            }
            return frag;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return ACTIONBAR_TAB_NUM;
        }

        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        public void onPageSelected(int position) {
            // TODO Auto-generated method stub
            Log.d("chan", "onPageSelected");
            Log.d("chan", "Position : " + position);
            mViewPager.setCurrentItem(position, true);
            mTabHost.setCurrentTab(position);
        }

        public PreferenceFragment getCurrentFragment() {
            int i = mViewPager.getCurrentItem();
            return (PreferenceFragment)getItem(i);
        }

        public int getCurrentTabIndex() {
            return mViewPager.getCurrentItem();
        }

    }
}
