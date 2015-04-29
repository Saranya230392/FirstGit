package com.android.settings.dragndrop;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.R;

public class ButtonCombinationDrag extends SettingsPreferenceFragment implements OnClickListener {
    private static final String TAG = "ButtonCombinationDrag";

    //private ButtonCombinationPreference mButtonCombination;
    private CombinationView mCombinationView;
    private ImageView mAniImage;
    private AnimationDrawable mAni;
    private View mView;
    Runnable mRunAni;
    private ScrollView mScrollView;
    private RelativeLayout mRlayout;
    private TextView mSummaryText;
    private TextView mHelpText;
    private String mSearch_result;
    
    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {

            if (Utils.supportSplitView(getActivity())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        
        mSearch_result = getActivity().getIntent()
                .getStringExtra("search_item");
        if (mSearch_result != null && !mSearch_result.equals("")) {
            getActivity().setTitle(R.string.button_combi_title);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        /* Create combination view which applied drag & drop function. */
        mView = inflater.inflate(R.layout.button_combi_drag_view, null);
        registerBReceiver();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        /* Button Configuration */
        ButtonConfiguration.configure(getActivity());
        setLayout();
    }

    private void registerBReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        getActivity().registerReceiver(mPackageAdd, filter);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mCombinationView.aViewIsDragged()) {
            mCombinationView.pauseTouch();
        }
        ButtonConfiguration.resetConfigure();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mAni.stop();
        mAniImage.removeCallbacks(mRunAni);
        mAniImage.setBackgroundDrawable(null);
        mRunAni = null;
        Utils.recycleView(mView);
        mView = null;
        getActivity().unregisterReceiver(mPackageAdd);
        System.gc();
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        //Toast.makeText(getActivity(), "If you want change position of item, just drag the item and drop the place you want. the view="+v.getTag(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        setLayout();
    }

    public void setLayout() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.button_combi_drag_view, null);
        init();
        ViewGroup rootView = (ViewGroup)getView();
        rootView.removeAllViews();
        rootView.addView(mView);
    }

    public void init() {
        mCombinationView = (CombinationView)mView.findViewById(R.id.gridview);
        mScrollView = (ScrollView)mView.findViewById(R.id.scroll_view);
        mRlayout = (RelativeLayout)mView.findViewById(R.id.layout_container);
        mSummaryText = (TextView)mView.findViewById(R.id.summary_text);
        mHelpText = (TextView)mView.findViewById(R.id.help_text);
        int mBGsize = getResources().getDrawable(R.drawable.img_button_combination_bg)
                .getIntrinsicWidth();
        LinearLayout.LayoutParams lp = null;
        lp = (LinearLayout.LayoutParams)mRlayout.getLayoutParams();
        lp.width = mBGsize;
        mRlayout.setLayoutParams(lp);
        CombinationViewAdapter adapter = new CombinationViewAdapter(
                getActivity());
        mCombinationView.setAdapter(adapter);
        mCombinationView.setOnClickListener(this);

        mCombinationView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {
                if (mCombinationView.aViewIsDragged()) {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                } else {
                    mScrollView.requestDisallowInterceptTouchEvent(false);
                }
                return mCombinationView.onTouch(view, event);
            }
        });

        mScrollView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                mCombinationView.setPressed(false);
                mCombinationView.pauseTouch();
                return false;
            }
        });

        mAniImage = (ImageView)mView.findViewById(R.id.ani_drag_help);

        if (Utils.isUI_4_1_model(getActivity())) {
            boolean mSupportDualWindow = SystemProperties.getBoolean(
                    "ro.lge.capp_splitwindow", false);
            if (mSupportDualWindow) {
                if (Utils.isSupportQslide()) {
                    mAniImage.setBackgroundResource(R.anim.ani_button_combi_guide);
                } else {
                    mAniImage.setBackgroundResource(R.anim.ani_button_combi_no_qslide_guide);
                }
            } else {
                if (Utils.isSupportQslide()) {
                    mAniImage.setBackgroundResource(R.anim.ani_button_combi_sim_guide);
                } else {
                    mAniImage.setBackgroundResource(R.anim.ani_button_combi_no_qslide_dual_guide);
                }
            }
        } else {
            if (Utils.isMultiSimEnabled() || Utils.isTripleSimEnabled()) {
                mAniImage.setBackgroundResource(R.anim.ani_button_combi_sim_guide);
            } else {
                mAniImage.setBackgroundResource(R.anim.ani_button_combi_guide);
            }
        }

        mAni = (AnimationDrawable)mAniImage.getBackground();

        mAniImage.postDelayed(mRunAni = new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                if (mView != null) {
                    if (mAni != null) {
                        mAni.start();
                    }
                }
            }
        }, 1000);

        if (Utils.isUI_4_1_model(getActivity())) {
            mSummaryText.setText(R.string.setting_button_combination_summary_text_ex_home);
            mHelpText.setText(R.string.setting_button_combination_help_text_ex_home);
        }
    }

    private final BroadcastReceiver mPackageAdd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pkgName = intent.getData().getSchemeSpecificPart();
            String action = intent.getAction();
            String mQmemoPackageName = "com.lge.qmemoplus";

            if (!Utils.isUI_4_1_model(context)) {
                return;
            }

            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            Log.d(TAG, "Receive package name : " + pkgName);
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing && pkgName.equals(mQmemoPackageName)) {
                    ButtonItemManager buttonItemManager = new ButtonItemManager(context);
                    buttonItemManager.deleteButtonCombination(pkgName);
                    ButtonConfiguration.resetConfigure();
                    onResume();
                }
            } else {
                if (pkgName.equals(mQmemoPackageName)) {
                    ButtonConfiguration.resetConfigure();
                    onResume();
                }
            }
        }
    };
}