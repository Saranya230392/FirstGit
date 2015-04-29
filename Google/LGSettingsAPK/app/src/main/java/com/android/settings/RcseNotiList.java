package com.android.settings;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.android.settings.R;
import android.database.sqlite.SQLiteException;

public class RcseNotiList extends ListActivity {
    /** Called when the activity is first created. */
    private static final String LOG_TAG = "Noti_List";
    private Uri mNoticeDBUri;
    private ContentResolver mContentResolver;
    static final String CONTENT_URI = "content://com.lge.ims.ac.noticeprovider/notice";
    private static ArrayList<HashMap<String, ?>> data = new ArrayList<HashMap<String, ?>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        readNoticeDB();
        SimpleAdapter adapter = new SimpleAdapter(this,
                data,
                R.layout.rcs_e_notice_item,
                new String[] { "title", "listDate" },
                new int[] { R.id.noti_title, R.id.noti_date });
        setListAdapter(adapter);

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        data.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        //Toast.makeText(this, data.get(position).get("url")+"", Toast.LENGTH_SHORT).show();
        ContentResolver mContentResolver = this.getContentResolver();
        ContentValues row = new ContentValues();

        try {
            String noticeID = data.get(position).get("noticeid").toString();
            row.put("read", "TRUE");
            mContentResolver.update(Uri.parse(CONTENT_URI), row, "noticeid ="
                    + noticeID, null);
        } catch (NullPointerException e) {
            Log.w(LOG_TAG, "NullPointerException");
        }

        Intent intent = new Intent(RcseNotiList.this, RcseNotiView.class);
        intent.putExtra("url", data.get(position).get("url") + "");
        startActivity(intent);
    }

    public void readNoticeDB() {

        mNoticeDBUri = Uri.parse((CONTENT_URI));
        mContentResolver = this.getContentResolver();

        Cursor mCursor = mContentResolver.query(mNoticeDBUri, null, null, null, null);

        if (mCursor != null) {

            if (mCursor.moveToFirst() == false) {
                //Log.d(LOG_TAG, "DB is empty");
                mCursor.close();
                mCursor = null;
                return;
            }
        }

        //Log.d(LOG_TAG, "Notice DB row count: "+ count);
        //HashMap<String, String> row;
        try {
            if (mCursor != null) {

            }

            if (mCursor != null && mCursor.moveToFirst()) {
                HashMap<String, String> row = new HashMap<String, String>();

                row.put("noticeid", mCursor.getString(mCursor.getColumnIndex("noticeid")));
                row.put("title", mCursor.getString(mCursor.getColumnIndex("title")));
                row.put("date", mCursor.getString(mCursor.getColumnIndex("date")));
                row.put("url", mCursor.getString(mCursor.getColumnIndex("url")));
                row.put("read", mCursor.getString(mCursor.getColumnIndex("read")));
                String[] listDate = mCursor.getString(mCursor.getColumnIndex("date")).split(" ");
                row.put("listDate", listDate[0]);
                data.add(row);
                //Log.d(LOG_TAG, "noticeid : "+mCursor.getString(mCursor.getColumnIndex("noticeid")));
                //Log.d(LOG_TAG, "title : "+mCursor.getString(mCursor.getColumnIndex("title")));
                //Log.d(LOG_TAG, "date : "+mCursor.getString(mCursor.getColumnIndex("date")));
                //Log.d(LOG_TAG, "url : "+mCursor.getString(mCursor.getColumnIndex("url")));
                //Log.d(LOG_TAG, "read : "+mCursor.getString(mCursor.getColumnIndex("read")));
            }
            while (mCursor != null && mCursor.moveToNext()) {
                HashMap<String, String> row = new HashMap<String, String>();
                row.put("noticeid", mCursor.getString(mCursor.getColumnIndex("noticeid")));
                row.put("title", mCursor.getString(mCursor.getColumnIndex("title")));
                row.put("date", mCursor.getString(mCursor.getColumnIndex("date")));
                row.put("url", mCursor.getString(mCursor.getColumnIndex("url")));
                row.put("read", mCursor.getString(mCursor.getColumnIndex("read")));
                String[] listDate = mCursor.getString(mCursor.getColumnIndex("date")).split(" ");
                row.put("listDate", listDate[0]);
                data.add(row);
                //Log.d(LOG_TAG, "noticeid : "+mCursor.getString(mCursor.getColumnIndex("noticeid")));
                //Log.d(LOG_TAG, "title : "+mCursor.getString(mCursor.getColumnIndex("title")));
                //Log.d(LOG_TAG, "date : "+mCursor.getString(mCursor.getColumnIndex("date")));
                //Log.d(LOG_TAG, "url : "+mCursor.getString(mCursor.getColumnIndex("url")));
                //Log.d(LOG_TAG, "read : "+mCursor.getString(mCursor.getColumnIndex("read")));
            }
        } catch (SQLiteException e) {
            Log.w(LOG_TAG, "SQLiteException");
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = null;
        }
    }
}
