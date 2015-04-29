package com.lge.handwritingime.popup;

import com.lge.handwritingime.HandwritingKeyboard;
import com.lge.handwritingime.manager.ThemeManager;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;

public abstract class BasePopup {

    public static final int MSG_POPUP_DISAPPER = 11;

    public View mAnchor;
    public PopupWindow mWindow;
    public Context mContext;
    public LayoutInflater mInflater;
    public WindowManager mWindowManager;
    protected ThemeManager mThemeManager;

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MSG_POPUP_DISAPPER:
                dismiss();
                break;
            default:
                break;
            }
        };
    };

//    public BasePopup(View anchor) {
//        // TODO Auto-generated constructor stub
//        mAnchor = anchor;
////        mContext = anchor.getContext();
//        mContext = HandwritingKeyboard.getBaseContext();
//        mWindow = new PopupWindow(mContext);
//        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//        mWindow.setTouchInterceptor(OTL);
//        mWindow.setOutsideTouchable(true);
//        mWindow.setBackgroundDrawable(new ColorDrawable());
//        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
//        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//    }
    
    public BasePopup(ThemeManager themeManager, View anchor) {
        // TODO Auto-generated constructor stub
        mThemeManager = themeManager;
        mAnchor = anchor;
//        mContext = anchor.getContext();
        mContext = HandwritingKeyboard.getBaseContext();
        mWindow = new PopupWindow(mContext);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindow.setTouchInterceptor(OTL);
        mWindow.setOutsideTouchable(true);
        mWindow.setBackgroundDrawable(new ColorDrawable());
        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public abstract void show(int viewX, int viewY);

    public boolean isShowingPopup() {
        return mWindow.isShowing();
    }

    public void dismiss() {
        mWindow.dismiss();
    }

    OnTouchListener OTL = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                dismiss();
                return true;
            }
            return false;
        }
    };
}
