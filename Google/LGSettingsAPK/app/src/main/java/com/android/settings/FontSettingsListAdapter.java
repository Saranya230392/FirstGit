/*
 ***************************************************************************
 ** FontSettingsListAdapter.java
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

import android.widget.ArrayAdapter;
import android.content.Context;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.RadioButton;
import android.graphics.Typeface;
import android.widget.Toast;
import android.util.Log;
import android.content.res.Configuration;
import android.app.ActivityManagerNative;
import android.os.RemoteException;

import com.android.settings.lgesetting.Config.Config; // LGE Code

/****************************************************************************
 ** Font Settings ListAdapter : KK Ver.
 ****************************************************************************/
public class FontSettingsListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "HyFontSettingsListAdapter";
    private int mDefaultTypefaceIndex = 0;
    private LayoutInflater mInflater;
    private FontServerConnection mRemoteFontServer = null;
    private Context mContext;
    private final float mFontScales[] = { 0.8f, 1.0f, 1.15f, 1.30f };
    private int mFontScaleIndex = 1;
    private final Configuration mCurConfig = new Configuration();

    public FontSettingsListAdapter(Context context,
            FontServerConnection remoteFontServer) {
        super(context, R.layout.font_settings_list_item);
        setFontListAdapter(context, remoteFontServer);
    }

    private void setFontListAdapter(Context context,
            FontServerConnection remoteFontServer) {
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
        final String[] fontNameLists = mRemoteFontServer.getAllFontNames();
        if (fontNameLists != null) {
            addAll(fontNameLists);
            mDefaultTypefaceIndex = mRemoteFontServer.getDefaultTypefaceIndex();
        } else {
            Log.w(TAG, "fontNameLists is null");
        }
    }

    private void setFontScaleIndex() {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault()
                    .getConfiguration());
            mFontScaleIndex = Utils.floatToIndex(mContext, mCurConfig.fontScale);
        } catch (RemoteException e) {
        }
        mFontScaleIndex = (mFontScaleIndex > 3 ? 3 : mFontScaleIndex);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        FontSettingsListItem fi;
        v = null;
        if ((v = mInflater.inflate(R.layout.font_settings_list_item, null)) == null) {
            return v;
        }
        fi = createFontSettingsListItem(v, position);
        v.setTag(fi);
        return v;
    }

    private FontSettingsListItem createFontSettingsListItem(View v, int position) {
        return new FontSettingsListItem(v, position);
    }

    /****************************************************************************
     ** FontSettingsListItem
     ****************************************************************************/
    private class FontSettingsListItem {
        private TextView mFontName = null;
        private RadioButton mFontChoice = null;

        FontSettingsListItem(View v, int position) {
            setFontName(v, position);
            setFontChoice(v, position);
        }

        private void setFontName(View v, int position) {
            if ((mFontName = (TextView) v.findViewById(R.id.tv_settings_font)) != null) {
                float textSize = mFontName.getTextSize();
                textSize = textSize * mFontScales[mFontScaleIndex];
                mFontName.setTextSize(0, textSize);

                // http://mlm.lge.com/di/browse/LGEIME-5188
                if (mRemoteFontServer != null) {
                    String[] allWebFacename = mRemoteFontServer.getAllFontWebFaceNames();
                    if (allWebFacename == null) {
                        return;
                    }
                    Typeface dtf = Typeface.create(allWebFacename[mDefaultTypefaceIndex],
                            Typeface.NORMAL);
                    Typeface tf = Typeface.create(allWebFacename[position], Typeface.NORMAL);
                    int numEmbeddedFonts = mRemoteFontServer.getNumEmbeddedFonts();

                    if ((position < numEmbeddedFonts) || (position == mDefaultTypefaceIndex)
                            || (dtf.equals(tf) == false)) {
                        CharSequence fontItemName = android.text.Html.fromHtml("<FONT face="
                                + allWebFacename[position] + ">"
                                + getItem(position) + "</FONT>");
                        mFontName.setText(fontItemName);
                    } else {
                        mFontName.setTypeface(Typeface.createFromFile(mRemoteFontServer
                                .getFontFullPath(position)), Typeface.BOLD);
                        mFontName.setText(getItem(position));
                    }
                } else {
                    mFontName.setText(getItem(position));
                }
            }
        }

        private void setFontChoice(View v, int position) {
            if ((mFontChoice = (RadioButton) v
                    .findViewById(R.id.rb_settings_font)) != null) {
                mFontChoice.setId(position);
                mFontChoice.setChecked((position == mDefaultTypefaceIndex ? true : false));
            }
        }
    }

    /****************************************************************************
     ** LGE Code
     ****************************************************************************/
    public boolean isDCM() {
        String DCM = Config.getOperator();
        if (DCM == null) {
            return false;
        } else {
            if (DCM.equals("DCM") == true) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isDefaultTypefaceAndJapanLocale(int position) {
        if (position == 0) { // Check default index.
            String curLanguageCode = java.util.Locale.getDefault().getLanguage();
            if (curLanguageCode != null) {
                if (curLanguageCode.equals("ja") == true) { // Check locale.
                    return true;
                }
            }
        }
        return false;
    }
}
// CAPP_FONTS_HYFONTS_END