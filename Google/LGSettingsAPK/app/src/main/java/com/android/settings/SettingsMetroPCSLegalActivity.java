//[2012.07.10][munjohn.kang]added Metro PCS Legal Information
package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class SettingsMetroPCSLegalActivity extends Activity {
    private static final String TAG = "SettingsMetroPCSLegalActivity";
    private static final String MPCSLegal_URL = "http://www.metropcs.com/terms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);

        Uri uri = Uri.parse(MPCSLegal_URL);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
        finish();
    }
/*
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
*/
}
