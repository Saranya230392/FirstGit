/*
 ***************************************************************************
 ** FontListAdapter.java
 ***************************************************************************
 **
 ** 2012.07.17 Android JB
 **
 ** Mobile Communication R&D Center, Hanyang
 ** Sangmin, Lee (TMSword) ( Mobile Communication R&D Center / Senior Researcher )
 **
 ** This code is a program that changes the font.
 **
 ***************************************************************************
 */
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
import com.android.settings.lgesetting.Config.Config;

/****************************************************************************
 ** Font ListAdapter
 ****************************************************************************/
public class FontListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "HyFontListAdapter";
    private int mDefaultTypefaceIndex = 0;
    private LayoutInflater mInflater;
    private FontSettingsPreference mFontSettingsPref;
    private FontServerConnection mRemoteFontServer;
    private Context mContext;
    private final float mFontScales[] = { 0.8f, 1.0f, 1.15f, 1.30f };
    private int mFontScaleIndex = 1;
    private final Configuration mCurConfig = new Configuration();

    public FontListAdapter(Context context,
            FontSettingsPreference fontSettingsPref) {
        super(context, R.layout.font_list_item);
        setFontListAdapter(context, fontSettingsPref);
    }

    private void setFontListAdapter(Context context,
            FontSettingsPreference fontSettingsPref) {
        mFontSettingsPref = fontSettingsPref;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mRemoteFontServer = mFontSettingsPref.getFontServer();
        if (mRemoteFontServer != null) {
            mDefaultTypefaceIndex = mRemoteFontServer.getDefaultTypefaceIndex();
            String[] fontNameList = mRemoteFontServer.getAllFontNames(); //20130218 getAllFontNames() Null Check

            if (fontNameList != null) {
                addAll(fontNameList);
            } else {
                Log.w(TAG, "fontNameList is null");
            }

            setEntries(fontSettingsPref); // 2012 1113 FontList Focusing correct
        }

        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault()
                    .getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }
        mFontScaleIndex = Utils.floatToIndex(context, mCurConfig.fontScale);
    }

    public void setEntries(FontSettingsPreference fontSettingsPref) { // 2012
                                                                      // 1113
                                                                      // FontList
                                                                      // Focusing
                                                                      // correct
        int numAllFonts = mRemoteFontServer.getNumAllFonts();
        // 20131209 dongseok.lee : [G2][TD135093] check index of fonts.
        if (numAllFonts > 0) {
            CharSequence[] entries = new CharSequence[numAllFonts];
            CharSequence[] entryValues = new CharSequence[numAllFonts];
            for (int n = 0; n < numAllFonts; n++) {
                entries[n] = getItem(n);
                entryValues[n] = String.valueOf(n);
            }
            fontSettingsPref.setEntries(entries);
            fontSettingsPref.setEntryValues(entryValues);
            fontSettingsPref.setValueIndex(mDefaultTypefaceIndex);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        FontItem fi;
        v = null;
        if ((v = mInflater.inflate(R.layout.font_list_item, null)) == null) {
            return v;
        }
        fi = createFontItem(v, position);
        v.setTag(fi);
        return v;
    }

    private FontItem createFontItem(View v, int position) {
        return new FontItem(v, position);
    }

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

    // 2013.02.05 Japan Modified START
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

    // 2013.02.05 Japan Modified END
    private class FontItem {
        public TextView tvFontName;
        public RadioButton rbFontChoice;

        FontItem(View v, int position) {
            tvFontName = (TextView)v.findViewById(R.id.tv_font_name);
            // 20121226 dongseok.lee : [TD 260590] during update list, can't
            // make TypeFace
            // if( mRemoteFontServer != null ) {
            // tvFontName.setTypeface( Typeface.createFromFile(
            // mRemoteFontServer.getFontFullPath( position ) ), Typeface.BOLD );
            // //2012.12.08 Bold Typeface
            // }
            float textSize = tvFontName.getTextSize();
            textSize = textSize * mFontScales[mFontScaleIndex];
            tvFontName.setTextSize(0, textSize);

            // 20121226 dongseok.lee : [TD 260590] during update list, can't
            // make TypeFace [START]
            // tvFontName.setText( getItem( position ) );

            // http://mlm.lge.com/di/browse/LGEIME-5188
            //Added Font style. That Does not show a unique font style.
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
                            + allWebFacename[position] + ">" + getItem(position) + "</FONT>");
                    tvFontName.setText(fontItemName);
                } else {
                    tvFontName.setTypeface(Typeface.createFromFile(mRemoteFontServer
                            .getFontFullPath(position)), Typeface.BOLD);
                    tvFontName.setText(getItem(position));
                }
            } else {
                tvFontName.setText(getItem(position));
            }

            // 2013.02.05 Japan Modified END
            // 20121226 dongseok.lee : [TD 260590] during update list, can't
            // make TypeFace [END]

            // 2013 01 19 echul.lee : [Start] Add Code to block the touch
            //final int which = position;
            // 2013 01 19 echul.lee : [End] Add Code to block the touch

            rbFontChoice = (RadioButton)v.findViewById(R.id.rb_font_choice);
            rbFontChoice.setId(position);
            rbFontChoice.setChecked((position == mDefaultTypefaceIndex ? true
                    : false));
            rbFontChoice.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int selectIndex = v.getId();
                    if (selectIndex != mDefaultTypefaceIndex) {
                        if (mRemoteFontServer != null) {
                            mRemoteFontServer
                                    .selectDefaultTypeface(selectIndex);
                        }
                    } else {
                        Toast.makeText(
                                mContext,
                                R.string.sp_font_type_select_nitify_msg_NORMAL,
                                Toast.LENGTH_SHORT).show();
                    }
                    mFontSettingsPref.getDialog().dismiss();
                    // mFontSettingsPref.getDialog().dismiss();
                }
            });
        }
    }
}
