/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Intent用ラッパークラス
 * 
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class StartApps extends Activity {

    /** @see android.app.Activity#onCreate */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_apps);
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.lge.apps.jp.phone");
            intent.putExtra("CategoryID", "IMEDIC");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }
}
