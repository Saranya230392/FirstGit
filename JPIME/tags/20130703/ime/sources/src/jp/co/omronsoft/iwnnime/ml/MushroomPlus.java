/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MushroomPlus extends ListActivity {
    /** The mushroom action code */
    public static final String MUSHROOM_ACTION = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    /** The mushroom category code */
    public static final String MUSHROOM_CATEGORY = "com.adamrocker.android.simeji.REPLACE";
    /** The mushroom intent send key code */
    public static final String MUSHROOM_REPLACE_KEY = "replace_key";

    /** The keyword of the get string from IME */
    public static final String GET_STRING_TYPE = "get_string_type";

    /** The mushroom apk start message */
    private static final int MSG_START_MUSHROOM = 1;
    
    /** The mushroom apk start message */
    private static final int MSG_START_WNNCONNECTOR = 100;

    /** The WnnConnector action code */
    private static final String ACTION_RECEIVE = "jp.co.omronsoft.iwnnime.WnnConnector.RECEIVE";

    /** The keyword of the text data from the IME   */
    private static final String KEY_WORD = "text";

    /** The keyword of the text data sent to IME */
    private static final String KEY_SEND = "modifiedtext";

    /** WnnConnector Count */
    private int mWnnConnectorCnt = 0;

    final ArrayList<CharSequence> mClassNameArray = new ArrayList<CharSequence>();
    final ArrayList<CharSequence> mPackageNameArray = new ArrayList<CharSequence>();

    /** The Mushroom send string */
    private CharSequence mCharSequence;
    
    /** The Mushroom send type */
    private boolean mType;

    /** @see android.app.Activity#onCreate */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Intent intent = getIntent();
        mCharSequence = intent.getCharSequenceExtra(MUSHROOM_REPLACE_KEY);
        mType = intent.getBooleanExtra(GET_STRING_TYPE,false);
        
        ArrayList<CharSequence> labelArray = new ArrayList<CharSequence>();
        
        PackageManager pm = this.getPackageManager();
        
        Intent connectorIntent = new Intent(ACTION_RECEIVE);
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(connectorIntent, 0);
        Collections.sort(resolveInfo, new ResolveInfo.DisplayNameComparator(pm));

        Intent mushintent = new Intent(MUSHROOM_ACTION);
        mushintent.addCategory(MUSHROOM_CATEGORY);
        List<ResolveInfo> mushresolveInfo = pm.queryIntentActivities(mushintent, /* no flags */ 0);

        Collections.sort(mushresolveInfo, new ResolveInfo.DisplayNameComparator(pm));
        
        mWnnConnectorCnt = resolveInfo.size();

        resolveInfo.addAll(mushresolveInfo);
        for(int i = 0; i <  resolveInfo.size(); i++) {
            ResolveInfo info = resolveInfo.get(i);
            ActivityInfo actInfo = info.activityInfo;
            CharSequence label = actInfo.loadLabel(pm);
            String  classname  = actInfo.name;
            String  packagename  = actInfo.packageName;
            CharSequence iconLabel = getLabel(label, info.loadIcon(pm));
            labelArray.add(iconLabel);
            mClassNameArray.add(classname);
            mPackageNameArray.add(packagename);
        }

        CharSequence[] items = null;
        ArrayAdapter<CharSequence> adapter = null;

        if (labelArray.size() > 0) {
            items = new CharSequence[labelArray.size()];
            for (int i = 0; i < labelArray.size(); i++) {
                items[i] = labelArray.get(i);
            }
        } else {
            items = new CharSequence[1];
            items[0] = getResources().getString(R.string.ti_mushroom_launcher_activity_list_empty_txt);
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, items);
        setListAdapter(adapter);
    }

    /** @see android.app.Activity#onActivityResult */
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MSG_START_MUSHROOM && resultCode == RESULT_OK) {
            MushroomControl.getInstance().setResultString(data.getStringExtra(MUSHROOM_REPLACE_KEY));
            MushroomControl.getInstance().setResultType(false);
        } else if (requestCode == MSG_START_WNNCONNECTOR && resultCode == RESULT_OK){
            MushroomControl.getInstance().setResultString(data.getCharSequenceExtra(KEY_SEND));
            MushroomControl.getInstance().setResultType(true);
        }
        finish();
    }

    /** @see android.app.Activity#onBackPressed */
    @Override public void onBackPressed() {
        MushroomControl.getInstance().setResultString("");
        super.onBackPressed();
    }

    /** @see android.app.ListActivity#onListItemClick */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        if (mPackageNameArray.size() < 1) {
            MushroomControl.getInstance().setResultString("");
            //finish popup at here.
            finish();
            return;
        }
        
        Intent intent = new Intent();
        intent.setClassName(mPackageNameArray.get(position).toString(), mClassNameArray.get(position).toString());
        if (mWnnConnectorCnt < position + 1) {
            intent.setAction(MUSHROOM_ACTION);
            intent.addCategory(MUSHROOM_CATEGORY);
            if (mType) {
                intent.putExtra(MUSHROOM_REPLACE_KEY, "");
            } else {
                intent.putExtra(MUSHROOM_REPLACE_KEY, mCharSequence.toString());
            }
            startActivityForResult(intent, MSG_START_MUSHROOM);
        } else {
            intent.setAction(ACTION_RECEIVE);
            intent.putExtra(KEY_WORD, mCharSequence);
            startActivityForResult(intent, MSG_START_WNNCONNECTOR);
        }
    }
    
    /**
     * Convert ComposingText to CommitText with DecoEmoji span.
     * 
     * @param  label  AppLabel
     * @param  icon  AppIcon
     * @return  APPIcon + AppLabel 
     */
    private SpannableString getLabel(CharSequence label, Drawable icon) {
        Drawable d = createIconThumbnail(icon);

        if (d == null) {
            d = getApplicationContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        }
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        ListPreferenceSpan span = new ListPreferenceSpan(d, DynamicDrawableSpan.ALIGN_BASELINE,
                getApplicationContext().getResources().getDimensionPixelSize(R.dimen.list_preference_span_string_gap),
                getApplicationContext().getResources().getDimensionPixelSize(R.dimen.list_preference_span_image_gap),
                getApplicationContext().getResources().getDimensionPixelSize(R.dimen.list_preference_span_right_gap));
        SpannableString spannable = new SpannableString("+" + label);
        spannable.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Returns a Drawable representing the thumbnail of the specified Drawable.
     *
     * @param icon The icon to get a thumbnail of.
     *
     * @return A thumbnail for the specified icon or the icon itself if the
     *         thumbnail could not be created. 
     */
    public Drawable createIconThumbnail(Drawable icon) {
        int mIconWidth = (int)getResources().getDimension(R.dimen.app_icon_size);
        int mIconHeight = (int)getResources().getDimension(R.dimen.app_icon_size);

        
        Canvas mCanvas = new Canvas();
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        final Rect mOldBounds = new Rect();
        
        
        int width = mIconWidth;
        int height =mIconHeight;

        final int iconWidth = icon.getIntrinsicWidth();
        final int iconHeight = icon.getIntrinsicHeight();

        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        }

        if (width > 0 && height > 0) {
            if (width < iconWidth || height < iconHeight) {
                final float ratio = (float) iconWidth / iconHeight;

                if (iconWidth > iconHeight) {
                    height = (int) (width / ratio);
                } else if (iconHeight > iconWidth) {
                    width = (int) (height * ratio);
                }

                final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ?
                            Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                final Canvas canvas = mCanvas;
                canvas.setBitmap(thumb);
                mOldBounds.set(icon.getBounds());
                final int x = (mIconWidth - width) / 2;
                final int y = (mIconHeight - height) / 2;
                icon.setBounds(x, y, x + width, y + height);
                icon.draw(canvas);
                icon.setBounds(mOldBounds);
                icon = new BitmapDrawable(getResources(), thumb);
                canvas.setBitmap(null);
            } else if (iconWidth < width && iconHeight < height) {
                final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(mIconWidth, mIconHeight, c);
                final Canvas canvas = mCanvas;
                canvas.setBitmap(thumb);
                mOldBounds.set(icon.getBounds());
                final int x = (width - iconWidth) / 2;
                final int y = (height - iconHeight) / 2;
                icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                icon.draw(canvas);
                icon.setBounds(mOldBounds);
                icon = new BitmapDrawable(getResources(), thumb);
                canvas.setBitmap(null);
            }
        }
        return icon;
    }
}
