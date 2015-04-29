package com.android.settings.lge;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.ServiceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.widget.Button;

import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.android.settings.R;
import com.lge.constants.SettingsConstants;

import java.util.Locale;


public class SKTPhoneModeSetupWizard extends Activity implements OnClickListener {

    private static final String TAG = "SKTPhoneModeSetupWizard";
    private Button btnBack;
    private Button btnNext;
    private WebView firstWebview;
    private WebView secondWebview;
    public static final String PHONE_MODE_SET = "phone_mode_set";
    public static final String PHONE_MODE_AGREE = "phone_mode_agree";
    public static final int RESULT_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.dark_header_title));

        init();

    }

    private void init() {

        setContentView(R.layout.setup_phone20);

        firstWebview = (WebView)findViewById(R.id.webView);
        firstWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        firstWebview.loadUrl("file:///android_asset/Terms_conditions.html");
        firstWebview.setOnLongClickListener(new android.view.View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                return true;
            }
        });

        secondWebview = (WebView)findViewById(R.id.webView2);
        secondWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        secondWebview.loadUrl("file:///android_asset/Privacy_Policy.html");

        secondWebview.setOnLongClickListener(new android.view.View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                return true;
            }
        });

        btnBack = (Button)findViewById(R.id.backButton);
        btnBack.setOnClickListener(this);

        btnNext = (Button)findViewById(R.id.nextButton);
        btnNext.setOnClickListener(this);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            Intent intent = new Intent(
                    "com.lge.setupwizard_flowcontroller.Back");
            setResult(Activity.RESULT_CANCELED, intent);
            finish();

            return false;

        default:
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        init();
    }

    private boolean isInCall() {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                return true;
            }
        }

        int vtCallState = 0;
        ITelephony ts = ITelephony.Stub.asInterface(ServiceManager
                .checkService(Context.TELEPHONY_SERVICE));
        if (ts != null) {
            try {
                vtCallState = ts.getCallState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (vtCallState >= 100) {
            return true;
        }

        return false;
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
        case R.id.backButton:
            Log.d(TAG, "onClick back Button clicked");
            //            intent = new Intent("com.lge.setupwizard_flowcontroller.Back");
            //            setResult(Activity.RESULT_CANCELED, intent);
            finish();
            break;

        case R.id.nextButton:
            if (isInCall()) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources()
                                .getString(R.string.sp_not_available_during_a_call_NORMAL),
                        Toast.LENGTH_LONG).show();
                return;
            }

            Settings.System.putInt(getContentResolver(), PHONE_MODE_SET, 1);
            Settings.Global.putInt(getContentResolver(), PHONE_MODE_AGREE, 1);
            Log.d(TAG, "onClick next Button clicked");
            intent = getIntent();
            setResult(RESULT_CODE, intent);

            Intent mIntent = new Intent("com.skt.prod.dialer.CHANGE_TPHONE_MODE_SETTING");
            sendBroadcast(mIntent);
            Log.d("jw", "sendBroadcast CHANGE_TPHONE_MODE_SETTING");

            finish();
            break;
        default:
            Log.d(TAG, "invalid id : " + v.getId());
            return;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
