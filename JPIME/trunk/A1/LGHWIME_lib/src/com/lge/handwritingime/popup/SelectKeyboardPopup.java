package com.lge.handwritingime.popup;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.lge.handwritingime.R;
import com.lge.handwritingime.manager.ThemeManager;
import com.lge.ime.util.LgeImeCliptrayManager;

public class SelectKeyboardPopup extends BasePopup {

    private View mRoot;
//    public ImageButton mBtnVoiceInput;
    public ImageButton mBtnKeyboardInput;
    public ImageButton mBtnSetting;
    public ImageButton mBtnClip;

    public interface OnClickKeyboardPopupListener {
//        public void onClick(int id);
        public void onClick(Object tag);
    }

    OnClickKeyboardPopupListener mClickListener;

    public void setOnClickKeyboardPopupListener(OnClickKeyboardPopupListener l) {
        mClickListener = l;
    }

//    public SelectKeyboardPopup(View anchor) {
//        super(anchor);
//        mRoot = (ViewGroup) mInflater.inflate(R.layout.select_keyboard_popup_layout, null);
//        mBtnKeyboardInput = (ImageButton) mRoot.findViewById(R.id.keyboardInput);
//        mBtnSetting = (ImageButton) mRoot.findViewById(R.id.keyboardSetting);
//        mBtnClip = (ImageButton) mRoot.findViewById(R.id.keyboardClip);
//        mBtnKeyboardInput.setOnClickListener(OCL);
//        mBtnSetting.setOnClickListener(OCL);
//        mBtnClip.setOnClickListener(OCL);
//        
//        if (!LgeImeCliptrayManager.isSupportCliptray(mContext)) {
//            mBtnClip.setVisibility(View.GONE);
//        }
//        
//        mBtnKeyboardInput.setPressed(true);
//    }
    
    public SelectKeyboardPopup(ThemeManager themeManager, View anchor) {
        super(themeManager, anchor);
        
        mRoot = (ViewGroup) mThemeManager.inflate(R.layout.select_keyboard_popup_layout, null);
        mBtnKeyboardInput = (ImageButton) mRoot.findViewWithTag("keyboardInput");
        mBtnSetting = (ImageButton) mRoot.findViewWithTag("keyboardSetting");
        mBtnClip = (ImageButton) mRoot.findViewWithTag("keyboardClip");
        mBtnKeyboardInput.setOnClickListener(OCL);
        mBtnSetting.setOnClickListener(OCL);
        mBtnClip.setOnClickListener(OCL);
        
        if (!LgeImeCliptrayManager.isSupportCliptray(mContext)) {
            mBtnClip.setVisibility(View.GONE);
        }
        
        mBtnKeyboardInput.setPressed(true);
    }

    public void show(int viewX, int viewY) {
        mWindow.setContentView(mRoot);
        mWindow.showAtLocation(mAnchor, Gravity.LEFT | Gravity.BOTTOM, viewX, viewY);

        if (mHandler != null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_POPUP_DISAPPER), 2000);
        }
    }

    OnClickListener OCL = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
//                mClickListener.onClick(v.getId());
                mClickListener.onClick(v.getTag());
            }
            dismiss();
        }
    };
}
