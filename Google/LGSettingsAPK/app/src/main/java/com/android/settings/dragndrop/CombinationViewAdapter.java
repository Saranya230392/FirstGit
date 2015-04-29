/**
 * Copyright 2012 
 * 
 * Nicolas Desjardins  
 * https://github.com/mrKlar
 * 
 * Facilite solutions
 * http://www.facilitesolutions.com/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.android.settings.dragndrop;

import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;

public class CombinationViewAdapter {

    private Context mContext;
//    private CombinationView mGridView;
    private ButtonItemManager mItemManager;
    private static String TAG = "CombinationView";
    public Runnable mPendingCheckForLongPress;
    public boolean mHasPerformedLongPress;
    public View mView;
    public static final int MAX_NUM = 5;

//    public CombinationViewAdapter(Context context, CombinationView gridview) {
    public CombinationViewAdapter(Context context) {
        super();
        mContext = context;
//        mGridView = gridview;
        mItemManager = new ButtonItemManager(context);
        mItemManager.buildItems();
    }

//    public View getView(int index) {
//
//        LinearLayout layout = new LinearLayout(mContext);
//        layout.setOrientation(LinearLayout.VERTICAL);
//
//        ImageView icon = new ImageView(mContext);
//
//        ButtonItem item = mItemManager.getItem(index);
//
//        if (item == null) {
//            return null;
//        }
//
//        icon.setImageResource(item.getDrawableId());
//        //		icon.setPaddingRelative(15, 15, 15, 15);
//        icon.setTag(item.getName());
//
//        layout.addView(icon);
//
//        TextView label = new TextView(mContext);
//        label.setTag("text");
//        label.setHorizontalFadingEdgeEnabled(true);
//        label.setEllipsize(TruncateAt.MARQUEE);
//        label.setSingleLine();
//        if (mContext.getResources().getBoolean(com.lge.R.bool.config_lcd_oled)) {
//            label.setTextColor(Color.WHITE);
//        } else {
//            label.setTextColor(Color.BLACK);
//        }
//        label.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//        if (item.getDrawableId() == R.drawable.setting_button_combination_icon_qmemo) {
//            item.setName(checkQmemoName());
//            label.setText(checkQmemoName());
//        } else {
//            label.setText(item.getName());
//        }
//        layout.setContentDescription(item.getName());
//
//        int mScreenSize = Integer.parseInt(SystemProperties.get("ro.sf.lcd_density"));
//        if (mScreenSize < DisplayMetrics.DENSITY_XHIGH) {
//            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
//        } else {
//            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//        }
//
//        int size = icon.getDrawable().getIntrinsicWidth();
//        if (mItemManager.getDownTrayItemCount() == MAX_NUM) {
//            label.setMaxWidth((int)(size * 1.4));
//            if (mScreenSize < DisplayMetrics.DENSITY_XHIGH) {
//                label.setMaxWidth((int)(size * 1.3));
//            }
//        } else {
//    	    label.setMaxWidth((int)(size * 1.7));
//        }
//
//        label.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT));
//
//        setViewBackground(layout);
//        layout.setClickable(true);
//
//        layout.setOnTouchListener(new OnTouchListener() {
//
//            public boolean onTouch(View v, MotionEvent event) {
//                // TODO Auto-generated method stub
//                int action = event.getAction();
//                mView = v;
//                switch (action & MotionEvent.ACTION_MASK) {
//                case MotionEvent.ACTION_DOWN:
//                    if (mPendingCheckForLongPress == null) {
//                        mPendingCheckForLongPress = new Runnable() {
//                            public void run() {
//                                if (mGridView.startLongClick(mView)) {
//                                    mHasPerformedLongPress = true;
//                                }
//                            }
//                        };
//                    }
//                    mHasPerformedLongPress = false;
//                    v.postDelayed(mPendingCheckForLongPress, 0);
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    if (mHasPerformedLongPress) {
//                        if (mPendingCheckForLongPress != null) {
//                            v.removeCallbacks(mPendingCheckForLongPress);
//                        }
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                    if (mPendingCheckForLongPress != null) {
//                        v.removeCallbacks(mPendingCheckForLongPress);
//                        v.setPressed(false);
//                        mHasPerformedLongPress = false;
//                    }
//                    break;
//                default:
//                    break;
//                }
//                return true;
//            }
//        });
//
//        layout.addView(label);
//        layout.setTag(item.getKey());
//        return layout;
//    }

    //    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setViewBackground(LinearLayout layout) {
        //        if (android.os.Build.VERSION.SDK_INT >= 16) {
        //            layout.setBackground(mContext.getResources().getDrawable(R.drawable.list_selector_holo_light));
        //        }
    }

    public String checkQmemoName() {
        String QmemoString = null;
        String mQmemoPackageName = "com.lge.QuickClip";
        if (Utils.isUI_4_1_model(mContext)) {
            mQmemoPackageName = "com.lge.qmemoplus";
        }

        PackageInfo pi = null;
        PackageManager pm = mContext.getPackageManager();
        try {
            pi = pm.getPackageInfo(mQmemoPackageName, PackageManager.GET_ACTIVITIES);
            if (pi.packageName.equals(mQmemoPackageName)) {
                QmemoString = pi.applicationInfo.loadLabel(mContext.getPackageManager()).toString();
                Log.d(TAG, "QmemoString : " + QmemoString);
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "package is not found(" + mQmemoPackageName + ")");
        }
        return QmemoString;
    }

}
