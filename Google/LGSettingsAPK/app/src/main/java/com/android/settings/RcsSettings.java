package com.android.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.util.Log;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.database.sqlite.SQLiteException;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;

public class RcsSettings extends Activity {
    private static final String TAG = "RcsSettings";
    private ContentResolver mContentResolver;
    public static final String CONTENT_URI = "content://com.lge.ims.ac.termsprovider/terms";
    //private String termsTitle = null;
    private String termsUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rcs_e_notice_detail);
        readTermDB();
        WebView webview = (WebView)findViewById(R.id.noti_detail);
        try {
            webview.getSettings().setJavaScriptEnabled(true);
        } catch (NullPointerException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        webview.setWebViewClient(new WebViewClient() {

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

        });
        //webview.setWebViewClient(new WebViewClient());
        webview.loadUrl(termsUrl);
    }

    private void readTermDB() {
        Cursor mCursor = null;

        try {
            mContentResolver = getContentResolver();
            mCursor = mContentResolver.query(Uri.parse(CONTENT_URI), null, null, null, null);

            if (mCursor != null) {

            }

            if (Config.getOperator().equals("LGU")) {
                if (mCursor != null && mCursor.moveToFirst()) {
                    if (mCursor.getInt(mCursor.getColumnIndex("_id")) == 1) {
                        //termsTitle = (mCursor.getString(mCursor.getColumnIndex("title")));
                        termsUrl = (mCursor.getString(mCursor.getColumnIndex("url")));
                    }

                }

                if (mCursor != null && mCursor.moveToNext()) {
                    if (mCursor.getInt(mCursor.getColumnIndex("_id")) == 1) {
                        //termsTitle = (mCursor.getString(mCursor.getColumnIndex("title")));
                        termsUrl = (mCursor.getString(mCursor.getColumnIndex("url")));
                    }
                }
            } else {
                if (mCursor != null && mCursor.moveToFirst()) {
                    if (mCursor.getInt(mCursor.getColumnIndex("_id")) == 2) {
                        //termsTitle = (mCursor.getString(mCursor.getColumnIndex("title")));
                        termsUrl = (mCursor.getString(mCursor.getColumnIndex("url")));
                    }

                }

                if (mCursor != null && mCursor.moveToNext()) {
                    if (mCursor.getInt(mCursor.getColumnIndex("_id")) == 2) {
                        //termsTitle = (mCursor.getString(mCursor.getColumnIndex("title")));
                        termsUrl = (mCursor.getString(mCursor.getColumnIndex("url")));
                    }
                }
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "SQLiteException");
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }

        }

    }

}
