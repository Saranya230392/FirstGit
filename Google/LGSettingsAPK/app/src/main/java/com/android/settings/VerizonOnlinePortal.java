package com.android.settings;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.telephony.TelephonyManager;
import android.net.Uri;
import android.view.KeyEvent;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.os.SystemProperties;
import android.net.NetworkInfo;
import com.lge.constants.LGIntent;

import org.apache.http.util.EncodingUtils;
import android.provider.Settings.Secure;

public class VerizonOnlinePortal extends Activity implements OnClickListener {
    private static final String LOG_TAG = "VerizonOnlinePortal";
    private WebView mWebView;
    private Button mUserButton;
    private boolean mPayComplete = false;
    // FOR PRODUCTION URL
    private static final String FOURG_PREPAY_PRODUCTION_URL_HTTPS_WO_QMARK = "https://quickaccess.verizonwireless.com/bbportal/oem/start.do?";
    private BroadcastReceiver mKeyPadShowReceiver = null;
    private BroadcastReceiver mKeyPadHideReceiver = null;
    private LinearLayout mButtonLinearLayout;

    // This intent appeared when finishing
    private static final String ACTION_FINISHED_VERIZONE_PORTAL =
            "com.mediatek.server.action.ACTION_FINISHED_VERIZONE_PORTAL";
    private static final String FINISHED_CAUSE = "cause";
    private Context mContext;
    private TelephonyManager mTelephonyManager;
    private boolean mLoaded = false;
    private boolean mTimerOut = false;
    private Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(LOG_TAG, "Timer out");
            mTimerOut = true;
            if (mHandler != null && mRunnable != null) {
                mHandler.removeCallbacks(mRunnable);
            }
            setVerizonOnlinePortalWebLoadUrl();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.vzw_online_portal);

        mContext = getApplicationContext();
        mTelephonyManager = TelephonyManager.from(mContext);

        mUserButton = (Button)this.findViewById(R.id.userButton);
        mUserButton.setOnClickListener(this);

        mWebView = (WebView)this.findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());

        WebSettings set = mWebView.getSettings();
        set.setJavaScriptEnabled(true);
        set.setSupportZoom(true);
        set.setBuiltInZoomControls(true);
        set.setDisplayZoomControls(false);

        mWebView.addJavascriptInterface(new JSInterface(), "orderStatus");

        mButtonLinearLayout = (LinearLayout)findViewById(R.id.webView_button_bar);
        registerKeyPadShowListener();
        registerKeyPadHideListener();

        setVerizonOnlinePortalWebLoadUrl();

        final ConnectivityManager conn = ConnectivityManager.from(mContext);
        conn.removeNotification();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mDataStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mDataStateReceiver);
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "mPayComplete : " + mPayComplete);
        switch (v.getId()) {
        case R.id.userButton:
            Log.d(LOG_TAG, "onClick - R.id.userButton ");
            if (true == mPayComplete) {
                PowerManager pm;
                pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                pm.reboot("null");
                sendFinishedBroadcast(true);
                break;
            } else {
                setMobileDataEnabled(false);
                finish();
                sendFinishedBroadcast(false);
            }
            break;
        default:
            Log.d(LOG_TAG, "onClick - default ");
            break;
        }
    }

    private void setMobileDataEnabled(boolean enabled) {
        int pco_internet = SystemProperties.getInt("persist.lg.data.internet_pco", -1);
        int pco_ims = SystemProperties.getInt("persist.lg.data.ims_pco", -1);
        int pco_radio_off_by_pco5 = ( enabled == true ) ? 0 : 1;
        Log.d(LOG_TAG, "pco_radio_off_by_pco5 : " + pco_radio_off_by_pco5);

        if (pco_internet == 3) {
            mTelephonyManager.setDataEnabled(enabled);
        } else if (pco_ims == 5) {
            mTelephonyManager.setDataEnabled(enabled);
            mTelephonyManager.setRadioPower(enabled);
            android.provider.Settings.Secure.putInt(mContext.getContentResolver(),
                        "radio_off_by_pco5", pco_radio_off_by_pco5);
        }
    }

    private void sendFinishedBroadcast(boolean ret) {
        Log.d(LOG_TAG, "sendFinishedBroadcast, ret : " + ret);
        Intent intent = new Intent(ACTION_FINISHED_VERIZONE_PORTAL);
        intent.putExtra(FINISHED_CAUSE, ret);
        sendBroadcast(intent);
    }

    @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     if ((keyCode == KeyEvent.KEYCODE_BACK) && !mWebView.canGoBack())
     {
         Log.d(LOG_TAG, "Pressed back key when can not go back");
         if (true == mPayComplete) {
             PowerManager pm;
             pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
             pm.reboot("null");
             sendFinishedBroadcast(true);
         } else {
             setMobileDataEnabled(false);
             finish();
             sendFinishedBroadcast(false);
         }
         return true;
     }

     return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (mKeyPadShowReceiver != null) {
            unregisterReceiver(mKeyPadShowReceiver);
            mKeyPadShowReceiver = null;
        }

        if (mKeyPadHideReceiver != null) {
            unregisterReceiver(mKeyPadHideReceiver);
            mKeyPadHideReceiver = null;
        }

        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private boolean IsFromPcoMobilePopup() {
        int pco_internet = SystemProperties.getInt("persist.lg.data.internet_pco", -1);
        int pco_ims = SystemProperties.getInt("persist.lg.data.ims_pco", -1);

        if (pco_internet == 3 || pco_ims == 5) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isDataConnected() {
        ConnectivityManager cm = (ConnectivityManager)this
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean result = false;
        if (null != cm) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                result = networkInfo.isConnected();
                Log.d(LOG_TAG, "networkInfo.isConnected() : " + networkInfo.isConnected());
            }
        }
        return result;
    }

    private void setVerizonOnlinePortalWebLoadUrl() {
        String iccid = null;
        String imei = null;
        String portalPostData = null;

        Log.d(LOG_TAG, "setVerizonOnlinePortalWebLoadUrl");

        if (isDataConnected() == true) {
            mLoaded = true;
        } else if (Utils.isAirplaneModeOn(mContext)) {
            Log.d(LOG_TAG, "No Wait because Airplane mode is in On");
            mLoaded = true;
        } else if (IsFromPcoMobilePopup() == true && mTimerOut == false) {
            Log.d(LOG_TAG, "Data is in the state disconnected");
            mHandler.postDelayed(mRunnable, 10000);
            mLoaded = false;
            return;
        } else {
            mLoaded = true;
        }

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager != null) {
            imei = telephonyManager.getDefault().getDeviceId();
            iccid = telephonyManager.getDefault().getSimSerialNumber();
        }

        portalPostData = "iccid=" + iccid + "&imei=" + imei;
        Log.d(LOG_TAG, "portalPostData = " + portalPostData);
        mWebView.postUrl(FOURG_PREPAY_PRODUCTION_URL_HTTPS_WO_QMARK,
                EncodingUtils.getBytes(portalPostData, "BASE64"));
    }

    private final BroadcastReceiver mDataStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(LOG_TAG, "received ConnectivityManager.CONNECTIVITY_ACTION");

                if (mLoaded == true) {
                    Log.d(LOG_TAG, "Loaded already");
                    return;
                }

                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    Log.d(LOG_TAG, "networkInfo.isConnected() : " + networkInfo.isConnected());
                    if (networkInfo.isConnected() == true) {
                        setVerizonOnlinePortalWebLoadUrl();
                    }
                }
            }
        }
    };

    private void registerKeyPadShowListener() {
        if (mKeyPadShowReceiver == null) {
            Log.d(LOG_TAG, "registerKeyPadShowListener");
            mKeyPadShowReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(LGIntent.ACTION_SOFT_KEYPAD_SHOW)) {
                        Log.d(LOG_TAG, "registerKeyPadShowListener action : "
                                + action);
                        mButtonLinearLayout.setVisibility(View.GONE);
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(LGIntent.ACTION_SOFT_KEYPAD_SHOW);
            registerReceiver(mKeyPadShowReceiver, iFilter);
        }
    }

    private void registerKeyPadHideListener() {
        if (mKeyPadHideReceiver == null) {
            Log.d(LOG_TAG, "registerKeyPadHideListener");
            mKeyPadHideReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(LGIntent.ACTION_SOFT_KEYPAD_HIDE)) {
                        Log.d(LOG_TAG, "registerKeyPadHideListener action : "
                                + action);
                        mButtonLinearLayout.setVisibility(View.VISIBLE);
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(LGIntent.ACTION_SOFT_KEYPAD_HIDE);
            registerReceiver(mKeyPadHideReceiver, iFilter);
        }
    }

    public class JSInterface {
        @JavascriptInterface
        public void closeWebview() {
            runOnUiThread(new Runnable() {
                public void run() {
                    // TODO Auto-generated method stub
                    Log.d(LOG_TAG, "JSInterface - closeWebview ");
                    finish();
                }
            });
        }

        @JavascriptInterface
        public void onSuccess() {
            runOnUiThread(new Runnable() {
                public void run() {
                    // TODO Auto-generated method stub
                    Log.d(LOG_TAG, "JSInterface - onSuccess ");
                    mUserButton.setText(R.string.print_restart);
                    mPayComplete = true;
                }
            });
        }
    }
}
