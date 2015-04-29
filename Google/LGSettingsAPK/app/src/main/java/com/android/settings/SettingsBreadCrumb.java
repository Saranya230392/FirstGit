

package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;

import com.android.settings.R;

public class SettingsBreadCrumb {
    private static final String TAG = "SettingsBreadCrumb";
    private static final float RATIO_HEADER_LIST_AREA = 2;
    private static final float RATIO_FRAGMENT_AREA = 3;
    private static final String TAG_SETTINGS_BREADCRUMB = "tag_settings_breadcrumb";
    private static final String TAG_BREADCRUMB_VIEW = "myView";

    private BackStackChangedListener mBackStackChangedListener;
    private FragmentManager mFragmentManager;
    Context mContext;
    private String mTitle;
    private static int sIsAttached = 0;

    public SettingsBreadCrumb(Context context) {
        mContext = context;
        mFragmentManager = ((Activity)mContext).getFragmentManager();
    }

    public static SettingsBreadCrumb get(Activity activity) {
        Settings settings = null;
        try {
            settings = (Settings)activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.d(TAG, "ClassCastException~~!!");
        }
        return (settings != null) ? settings.getBreadCrumb() : null;
    }

    public ViewGroup get() {
        if (mContext != null) {
            return (ViewGroup)((Activity) mContext).findViewById(R.id.breadcrumb);
        }
        return null;
    }

    public void adjustArea() {
        View headers = ((Activity) mContext).findViewById(com.android.internal.R.id.headers);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)headers.getLayoutParams();
        lp.weight = RATIO_HEADER_LIST_AREA;
        headers.setLayoutParams(lp);

        ViewGroup prefsFrame = (ViewGroup)((Activity) mContext).findViewById(com.android.internal.R.id.prefs_frame);
        LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)prefsFrame.getLayoutParams();
        lp2.weight = RATIO_FRAGMENT_AREA;
        prefsFrame.setLayoutParams(lp2);
    }

    public void attach() {
        View prefs = (ViewGroup)((Activity) mContext).findViewById(com.android.internal.R.id.prefs);
        ViewGroup parentView = (ViewGroup)prefs.getParent();
        View breadcrumbContainer = ((Activity) mContext).getLayoutInflater().inflate(R.layout.lg_fragment_bread_crumb_item, parentView, false);
        breadcrumbContainer.setTag(TAG_SETTINGS_BREADCRUMB);
        parentView.addView(breadcrumbContainer, 0);
        if (mBackStackChangedListener == null) {
            mBackStackChangedListener = new BackStackChangedListener(mContext);
            mFragmentManager.addOnBackStackChangedListener(mBackStackChangedListener);
        }
    }

    public void setVisibility(int visibility) {
        if (visibility == View.INVISIBLE) {
            visibility = View.GONE;
        }

        View prefs = (ViewGroup)((Activity) mContext).findViewById(com.android.internal.R.id.prefs);
        ViewGroup parentView = (ViewGroup)prefs.getParent();
        View breadcrumbContainer = parentView.findViewWithTag(TAG_SETTINGS_BREADCRUMB);
        if (breadcrumbContainer != null) {
            breadcrumbContainer.setVisibility(visibility);
        }
    }

    public void clean() {
        if (mFragmentManager != null && mBackStackChangedListener != null) {
            mFragmentManager.removeOnBackStackChangedListener(mBackStackChangedListener);
            mBackStackChangedListener = null;
        }
    }

    public static boolean isAttached(Activity activity) {
        if (activity == null) {
            return false;
        }

        View prefs = (ViewGroup)((Activity)activity).findViewById(com.android.internal.R.id.prefs);
        
        if (prefs == null) {
            return false;
        }
        
        ViewGroup parentView = (ViewGroup)prefs.getParent();
        
        if (parentView == null) {
            return false;
        }
        
        sIsAttached = 
                (parentView.findViewWithTag(TAG_SETTINGS_BREADCRUMB) == null) ? -1 : 1;

        return (get(activity) != null && sIsAttached > 0);
    }

    public void showBreadCrumb(CharSequence title, CharSequence shortTitle) {
        Log.d(TAG, "showBreadCrumb:title=" + title + ", shortTitle=" + shortTitle);
        if (mBackStackChangedListener != null) {
            if (mBackStackChangedListener.mRootTitle != null
                    && mBackStackChangedListener.mRootTitle.length() > 0
                    && mBackStackChangedListener.mRootTitle.equals(title.toString()) == false) {
                mBackStackChangedListener.mForced = true;
            }
            mBackStackChangedListener.mRootTitle = title;
            mBackStackChangedListener.onBackStackChanged();
        }
    }

    public void setTitle(String title) {
        if (mContext == null) {
            return;
        }
        Log.d(TAG, "setTitle:" + title);
        //Activity activity = (Activity)mContext;
        //TextView tv = (TextView)activity.findViewById(R.id.breadcrumb_title);
        //tv.setText(title);
        mTitle = title;
    }

    private class BackStackChangedListener implements FragmentManager.OnBackStackChangedListener {
        public CharSequence mRootTitle = null;
        private Activity mActivity;
        public boolean mForced = false;
        public BackStackChangedListener(Context context) {
            mActivity = (Activity)context;
        }

        @Override
        public void onBackStackChanged() {
            Log.d(TAG, "onBackStackChanged");
            if (get() == null) {
                return;
            }
            int numEntries = mFragmentManager.getBackStackEntryCount();
            ImageView upIcon = (ImageView)mActivity.findViewById(R.id.up_arrow);
            if (Utils.isRTLLanguage() || Utils.isEnglishDigitRTLLanguage()) {
                upIcon.setImageResource(R.drawable.ic_ab_back_holo_light_rtl);
            }
            TextView tv = (TextView)mActivity.findViewById(R.id.breadcrumb_title);
            View upicon_title = mActivity.findViewById(R.id.upicon_title);
            if (numEntries > 0) {
                upIcon.setVisibility(View.VISIBLE);
                if (mTitle != null && mTitle.length() > 0) {
                    tv.setText(mTitle);
                    mTitle = "";
                } else {
                    if (mForced == true) {
                        tv.setText(mRootTitle);
                    } else {
                        tv.setText(mFragmentManager.getBackStackEntryAt(numEntries - 1).getBreadCrumbTitle());
                    }
                }
                upicon_title.setOnClickListener(null);
                upicon_title.setClickable(true);
                upicon_title.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        try {
                            mFragmentManager.popBackStack();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                upIcon.setVisibility(View.GONE);
                tv.setText(mRootTitle);
                upicon_title.setOnClickListener(null);
                upicon_title.setClickable(false);
            }
            mForced = false;
        }
    }

    public void addSwitch(Switch mySwitch) {
/*        Log.d(TAG, "addSwitch(),=" + mySwitch);
        if (get() == null || mySwitch == null) {
            Log.d(TAG, "addSwitch(), get() == null || mySwitch == null");
            return ;
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        lp.addRule(RelativeLayout.END_OF, R.id.breadcrumb_title);
        mySwitch.setTag("mySwitch");
        detachView(mySwitch);
        removeSwitch();
        get().addView(mySwitch, lp);*/

        addView(mySwitch, null);
    }

    private void detachView(View view) {
        Log.d(TAG, "detachView," + view);
        ViewGroup vp = (ViewGroup)view.getParent();
        if (vp != null) {
            Log.d(TAG, "detachView, parent=" + vp);
            vp.removeView(view);
        }
    }

    public void removeSwitch() {
/*        Log.d(TAG, "removeSwitch()");
        if (get() == null) {
            Log.d(TAG, "removeSwitch(), get() == null");
            return ;
        }

        Switch mySwitch = (Switch)get().findViewWithTag("mySwitch");
        if (mySwitch != null) {
            Log.d(TAG, "removeSwitch(), remove Switch completed");
            get().removeView(mySwitch);
        } else {
            Log.d(TAG, "removeSwitch(), mySwitch is null");
        }*/
        removeView(null);
    }

    public void addImageButton(ImageButton myButton) {
        if (get() == null || myButton == null) {
            Log.d(TAG, "addImageButton:get="+get()+", myButton="+myButton);
            return ;
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        myButton.setTag("myButton");
        detachView(myButton);
        removeImageButton();
        //get().addView(myButton, lp);
        ViewGroup parentView = (ViewGroup)get().findViewById(R.id.breadcrumb_extra_view);
        if (parentView != null) {
            parentView.setVisibility(View.VISIBLE);
            parentView.addView(myButton, lp);
        }
    }

    public void removeImageButton() {
        if (get() == null) {
            Log.d(TAG, "removeImageButton:get="+get());
            return ;
        }

        ImageButton myButton = (ImageButton)get().findViewWithTag("myButton");
        if (myButton != null) {
            //get().removeView(myButton);
            ViewGroup parentView = (ViewGroup)get().findViewById(R.id.breadcrumb_extra_view);
            if (parentView != null) {
                parentView.setVisibility(View.GONE);
                parentView.removeView(myButton);
            }
        }
    }

    public ImageButton getImageButton() {
    	if (get() == null) {
    		Log.d(TAG, "getImageButton:get() return null.");
    		return null;
    	}
        ImageButton myButton = (ImageButton)get().findViewWithTag("myButton");
        Log.d(TAG, "getImageButton="+myButton);
        return myButton;
    }

    /* BreadCrumb �� View �� �߰��ϱ� ���� API �߰��մϴ�. Switch / ImageButton ��� parent �� View class �̹Ƿ�,
     * �ϱ� API �� �ᵵ �����մϴ�. */

    public void addView(View view, LayoutParams params) {
        if (get() == null || view == null) {
            Log.d(TAG, "addView:get=" + get() + ", view=" + view);
            return ;
        }

        if (params == null) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);
            lp.addRule(RelativeLayout.CENTER_VERTICAL);
            params = lp;
        }

        view.setTag(TAG_BREADCRUMB_VIEW);
        detachView(view);
        removeView(view);
//        get().addView(view, params);
        ViewGroup parentView = (ViewGroup)get().findViewById(R.id.breadcrumb_extra_view);
        if (parentView != null) {
            parentView.setVisibility(View.VISIBLE);
            parentView.addView(view, params);
        }
    }

    public void removeView(View view) {
        if (get() == null) {
            Log.d(TAG, "removeView:get=" + get());
            return ;
        }

        View myView = get().findViewWithTag(TAG_BREADCRUMB_VIEW);
        if (myView != view) {
            Log.d(TAG, "removeView:view is not attached to the breadcrumb~~!!, view = " + view);
        }
        /* Finally, the view attached to the breadcrumb should be removed anyway.*/
        if (myView != null) {
            Log.d(TAG, "removeView:view is removed.");
//            get().removeView(myView);
            ViewGroup parentView = (ViewGroup)get().findViewById(R.id.breadcrumb_extra_view);
            if (parentView != null) {
                parentView.setVisibility(View.GONE);
                parentView.removeView(myView);
            }
        }
    }

}