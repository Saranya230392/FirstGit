package com.lge.handwritingime.popup;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.lge.handwritingime.R;
import com.lge.handwritingime.manager.ThemeManager;

public class SelectDeleteButtonPopup extends BasePopup {
    private View mRoot;
    public ImageButton mBtnDeleteAll;
    public ImageButton mBtnDeleteOne;
    public ImageButton mBtnDeleteStroke;

    public interface OnClickDeletePopupListener {
//        public void onClick(int id);
        public void onClick(Object tag);
    }

    OnClickDeletePopupListener mClickListener;

    public void setOnClickDeletePopupListener(OnClickDeletePopupListener l) {
        mClickListener = l;
    }

    public SelectDeleteButtonPopup(ThemeManager themeManager, View anchor) {
        super(themeManager, anchor);
//        mRoot = (ViewGroup) mInflater.inflate(R.layout.select_delete_button_popup_layout, null);
        mRoot = (ViewGroup) mThemeManager.inflate(R.layout.select_delete_button_popup_layout, null);
        mBtnDeleteAll = (ImageButton) mRoot.findViewWithTag("deleteAll");
        mBtnDeleteOne = (ImageButton) mRoot.findViewWithTag("deleteOne");
        mBtnDeleteStroke = (ImageButton) mRoot.findViewWithTag("deleteStroke");
        mBtnDeleteAll.setOnClickListener(OCL);
        mBtnDeleteOne.setOnClickListener(OCL);
        mBtnDeleteStroke.setOnClickListener(OCL);
        mBtnDeleteStroke.setPressed(true);
    }

    public void show(int viewX, int viewY) {
        mWindow.setContentView(mRoot);
        mWindow.showAtLocation(mAnchor, Gravity.RIGHT | Gravity.BOTTOM, viewX, viewY);

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
