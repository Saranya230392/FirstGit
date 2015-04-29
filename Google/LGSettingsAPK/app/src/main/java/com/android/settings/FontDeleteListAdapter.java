/*
 ***************************************************************************
 ** FontListAdapterK.java
 ***************************************************************************
 **
 ** 2013.10.22 Android KK
 **
 ** Mobile Communication R&D Center, Hanyang
 ** Sangmin, Lee (TMSword) ( Mobile Communication R&D Center / Senior Researcher )
 **
 ** This code is a program that changes the font.
 **
 ***************************************************************************
 */
// CAPP_FONTS_HYFONTS
/****************************************************************************
 ** User Define : Package name, please change the name of your package.
 ****************************************************************************/
package com.android.settings;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

/****************************************************************************
 ** Font Delete ListAdapter : KK Ver.
 ****************************************************************************/
public class FontDeleteListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "HyFontDeleteListAdapter";
    private int mDefaultTypefaceIndex = 0;
    private LayoutInflater mInflater;
    private Context mContext;
    private FontServerConnection mRemoteFontServer = null;
    private final float mFontScales[] = { 0.8f, 1.0f, 1.15f, 1.30f };
    private int mFontScaleIndex = 1;
    private final Configuration mCurConfig = new Configuration();
    private boolean[] mChecked;
    private int mAllFontCount = 0;
    private int mEmbeddedFontCount = 0;
    private int mDownloadFontCount = 0;
    private String[] mAllWebfaceNames;
    private String[] mAllFontNames;
    //private String[] mDownloadWebfaceNames;
    private String[] mDownloadFontNames;

    public FontDeleteListAdapter(Context context, FontServerConnection remoteFontServer) {
        super(context, R.layout.font_delete_list_item);
        setFontListAdapter(context, remoteFontServer);
    }

    private void setFontListAdapter(Context context, FontServerConnection remoteFontServer) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mRemoteFontServer = remoteFontServer;
        if (mRemoteFontServer != null) {
            setFontLists();
            setFontScaleIndex();
        } else {
            Log.w(TAG, "RemoteFontServer is null");
        }
    }

    private void setFontLists() {
        mAllFontCount = mRemoteFontServer.getNumAllFonts();
        mEmbeddedFontCount = mRemoteFontServer.getNumEmbeddedFonts();
        mDownloadFontCount = (mAllFontCount - mEmbeddedFontCount);
        mDownloadFontCount = (mDownloadFontCount < 0 ? 0 : mDownloadFontCount);

        mAllWebfaceNames = mRemoteFontServer.getAllFontWebFaceNames();
        mAllFontNames = mRemoteFontServer.getAllFontNames();

        // 20140415 dongseok.lee : [WBT] 
        if (mAllWebfaceNames == null) {
            return;
        }
        // 20140415 dongseok.lee : [WBT]
        if (mAllFontNames == null) {
            return;
        }
        //mDownloadWebfaceNames = new String[mDownloadFontCount];
        mDownloadFontNames = new String[mDownloadFontCount];
        mChecked = new boolean[mDownloadFontCount];

        for (int n = 0; n < mDownloadFontCount; n++) {
            //mDownloadWebfaceNames[n] = mAllWebfaceNames[mEmbeddedFontCount + n];
            mDownloadFontNames[n] = mAllFontNames[mEmbeddedFontCount + n];
            mChecked[n] = false;
        }

        addAll(mDownloadFontNames);
        mDefaultTypefaceIndex = mRemoteFontServer.getDefaultTypefaceIndex();
    }

    private void setFontScaleIndex() {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            mFontScaleIndex = Utils.floatToIndex(mContext, mCurConfig.fontScale);
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException");
        }
        mFontScaleIndex = (mFontScaleIndex > 3 ? 3 : mFontScaleIndex);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        FontDeleteListItem fi;
        v = null;
        if ((v = mInflater.inflate(R.layout.font_delete_list_item, null)) == null) {
            return v;
        }
        fi = createFontDeleteListItem(v, position);
        v.setTag(fi);
        return v;
    }

    private FontDeleteListItem createFontDeleteListItem(View v, int position) {
        return new FontDeleteListItem(v, position);
    }

    /****************************************************************************
     ** FontDeleteListItem
     ****************************************************************************/
    private class FontDeleteListItem {
        private CheckBox mDeleteFont = null;
        private int mRealPosition = -1;

        FontDeleteListItem(View v, int position) {
            setDeleteFont(v, position);
        }

        private void setDeleteFont(View v, int position) {
            if ((mDeleteFont = (CheckBox)v.findViewById(R.id.cb_delete_font)) != null) {
                float textSize = mDeleteFont.getTextSize();
                textSize = textSize * mFontScales[mFontScaleIndex];
                mDeleteFont.setTextSize(0, textSize);
                mDeleteFont.setChecked(mChecked[position]);

                mRealPosition = (mEmbeddedFontCount + position);

                if (mRemoteFontServer != null) {
                    String[] allWebFacename = mRemoteFontServer
                            .getAllFontWebFaceNames();
                    if (allWebFacename == null) {
                        return;
                    }
                    Typeface dtf = Typeface.create(
                            allWebFacename[mDefaultTypefaceIndex],
                            Typeface.NORMAL);
                    Typeface tf = Typeface.create(
                            allWebFacename[mRealPosition], Typeface.NORMAL);

                    if ((mRealPosition == mDefaultTypefaceIndex)
                            || (dtf.equals(tf) == false)) {
                        CharSequence fontItemName = android.text.Html
                                .fromHtml("<FONT face="
                                        + allWebFacename[mRealPosition] + ">"
                                        + getItem(position) + "</FONT>");
                        mDeleteFont.setText(fontItemName);
                    } else {
                        mDeleteFont.setTypeface(Typeface
                                .createFromFile(mRemoteFontServer
                                        .getFontFullPath(mRealPosition)),
                                Typeface.BOLD);
                        mDeleteFont.setText(getItem(position));
                    }
                } else {
                    mDeleteFont.setText(getItem(position));
                }
            }
        }
    }

    /****************************************************************************
     ** public Functions
     ****************************************************************************/
    public boolean setChecked(int position) {
        if (mChecked != null) {
            mChecked[position] = (mChecked[position] == true ? false : true);
            for (int n = 0; n < numDownloadFont(); n++) {
                if (mChecked[n] != true) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void updateAllChecked(boolean checked) {
        for (int n = 0; n < numDownloadFont(); n++) {
            mChecked[n] = checked;
        }
    }

    public int numChecked() {
        int num = 0;
        for (int n = 0; n < numDownloadFont(); n++) {
            if (mChecked[n] == true) {
                num++;
            }
        }
        return num;
    }

    public int numDownloadFont() {
        return mDownloadFontCount;
    }

    public int numEmbeddedFont() {
        return mEmbeddedFontCount;
    }

    public boolean getChecked(int position) {
        if (position >= numDownloadFont()) {
            return false;
        } else {
            return mChecked[position];
        }
    }

    public boolean useFontDelete() {
        if (mDefaultTypefaceIndex < numEmbeddedFont()) {
            return false;
        }

        for (int n = 0; n < numDownloadFont(); n++) {
            if (mChecked[n] == true) {
                if ((n + numEmbeddedFont()) == mDefaultTypefaceIndex) {
                    return true;
                }
            }
        }
        return false;
    }
}
// CAPP_FONTS_HYFONTS_END
