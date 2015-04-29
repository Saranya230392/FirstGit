/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package com.lge.emoji;

import jp.co.omronsoft.iwnnime.ml.WrapTextCandidatesViewManager;

import android.content.Context;
import android.graphics.Point;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.WindowManager;

public class BaseEmojiKeyboard extends EmojiKeyboard {

    private WrapTextCandidatesViewManager mCandidatesViewManager;
    private View mCandidatesView;
    
    public BaseEmojiKeyboard(InputMethodService ims) {
        super(ims);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        mCandidatesViewManager = new WrapTextCandidatesViewManager();
        super.onCreate();
    }

    @Override
    public View onCreateCandidatesView() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        mCandidatesView = mCandidatesViewManager.initView(this, size.x, size.y);
        return mCandidatesView;
        // return super.onCreateCandidatesView();
    }
}
