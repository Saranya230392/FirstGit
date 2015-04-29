package com.lge.handwritingime.preference;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.lge.handwritingime.HandwritingKeyboard;
import com.lge.handwritingime.R;

public class ColorPickerDialogPreference extends Preference implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener, PreferenceManager.OnActivityDestroyListener, View.OnClickListener{
    public static final boolean DEBUG = HandwritingKeyboard.DEBUG;
    private static final String TAG = "LGHWIMEColorPickerDialogPreference";
    private static final int MAX_PEN_COLOR = 16;
    private Dialog mDialog;
    private CharSequence mDialogTitle;
    private Context mContext;
    private AlertDialog.Builder mBuilder;
    private int mDialogLayoutResId;
    private View colorPickerView;
    
    
    public ColorPickerDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerDialogPreference, 0, 0);
        
        mDialogTitle = a.getString(R.styleable.ColorPickerDialogPreference_dialogTitle);
        if (mDialogTitle == null) {
            mDialogTitle = getTitle();
        }
        
        mDialogLayoutResId = a.getResourceId(R.styleable.ColorPickerDialogPreference_dialogLayout, mDialogLayoutResId);
        
        a.recycle();
    }
    
    public ColorPickerDialogPreference(Context context){
        this(context, null);
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        mDialogLayoutResId = dialogLayoutResId;
    }

    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }
    
    protected View onCreateDialogView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        colorPickerView = inflater.inflate(R.layout.color_picker_layout, null);
        int penColor = getSelectedPenColor();
        for( int i = 1; i <= MAX_PEN_COLOR; i++ ){
            colorPickerView.findViewById(getResId("color_picker_", "id", i)).setOnClickListener(this);
            if ( i == penColor ){
                adjustViewShape(penColor, true);
            }
        }
        return colorPickerView;
    }
    
    public void setDialogTitle(CharSequence dialogTitle) {
        mDialogTitle = dialogTitle;
    }
    
    public void setDialogTitle(int dialogTitleResId) {
        setDialogTitle(getContext().getString(dialogTitleResId));
    }
    
    public CharSequence getDialogTitle() {
        return mDialogTitle;
    }
    
    private int getResId(final String s, final String category, int index) {
        String resName = null; 
        if( index < 10 )
            resName = s + "0" + index;
        else
            resName = s + index;
        try {
            return mContext.getResources().getIdentifier(resName, category, mContext.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void adjustViewShape(int penColor, boolean stroke) {
        View selectColor = null;
        
        if(penColor == -1) {
            return;
        }
        selectColor = colorPickerView.findViewById(getResId("color_picker_", "id", penColor));
        if( stroke ){
//            GradientDrawable bg = (GradientDrawable) mContext.getResources().getDrawable(R.drawable.color_picker_border);
//            bg.setColor(mContext.getResources().getColor(getResId("pen_color_", "color", penColor)));
//            selectColor.setBackgroundDrawable(bg);
            Drawable bg = mContext.getResources().getDrawable(R.drawable.color_picker_border);
            bg.setColorFilter(mContext.getResources().getColor(getResId("pen_color_", "color", penColor)), Mode.DST_OVER);     
            selectColor.setBackground(bg);
        }else{
            selectColor.setBackgroundColor(mContext.getResources().getColor(getResId("pen_color_", "color", penColor)));
        }
    }

    private int getSelectedPenColor() {
        String colorResourceId = 
                getPersistedString(mContext.getResources().getString(R.string.HW_PEN_COLOR_DEFAULT));
        if ( colorResourceId != null)
            return Integer.parseInt(colorResourceId.substring(10));
        return -1;
    }

    @Override
    public void onActivityDestroy() {
        if(mDialog == null || !mDialog.isShowing()) {
            return;
        }
        mDialog.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mDialog = null;
    }

    @Override
    protected void onClick() {
        showDialog(null);
    }

    private void showDialog(Bundle state) {
        mBuilder = new AlertDialog.Builder(mContext)
        .setTitle(mDialogTitle)
        .setNegativeButton(android.R.string.cancel, this);
    
    View contentView = onCreateDialogView();
    
    if(contentView != null) {
        mBuilder.setView(contentView);
    }

    final Dialog dialog = mDialog = mBuilder.create();

    if(state != null) {
        dialog.onRestoreInstanceState(state);
    }
    
    dialog.setOnDismissListener(this);
    dialog.show();
        
    }

    @Override
    public void onClick(View v) {
        Context context = mContext;        
        if(context == null){
            if (DEBUG)
                Log.d(TAG, "failure : context is null");
            return;
        }        
        
        String colorResourceId = context.getString(R.string.HW_PEN_COLOR_DEFAULT);
        View searchedView;
        for( int i = 1; i <= MAX_PEN_COLOR; i++ ){
            searchedView = colorPickerView.findViewById(getResId("color_picker_", "id", i));
            if ( v.equals(searchedView) ){
                colorResourceId = String.format(Locale.ENGLISH, "pen_color_%02d", i);
            }
        }
                
        adjustViewShape(getSelectedPenColor(), false);
        persistString(colorResourceId);
        adjustViewShape(getSelectedPenColor(), true);

        // null pointer exception
        Dialog dialog = mDialog;
        if(dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        
    }    
    
    private static class SavedState extends BaseSavedState {
        boolean isDialogShowing;
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
        
//        public static final Parcelable.Creator<SavedState> CREATOR =
//                new Parcelable.Creator<SavedState>() {
//            public SavedState createFromParcel(Parcel in) {
//                return new SavedState(in);
//            }
//
//            public SavedState[] newArray(int size) {
//                return new SavedState[size];
//            }
//        };
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mDialog == null || !mDialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = mDialog.onSaveInstanceState();
        return myState;
    }  
    
}
 
//public final class PenColorPreference extends DialogPreference {
//    private Context mContext;
//    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
//    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
//    private final int DEFAULT_CURRENT_VALUE;
//    private final int mDefaultValue;
//    private String mCurrentValue;
//    
//    public PenColorPreference(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        mContext = context;
//        Resources r = context.getResources();
//        DEFAULT_CURRENT_VALUE = r.getColor(R.color.pen_color_16);
//        
//        mDefaultValue = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
//    }
//
//    @Override
//    protected View onCreateDialogView() {
//        mCurrentValue = getPersistedString(mContext.getString(mDefaultValue));
//        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.color_picker_layout, null);
//        colorChild(view);
//        return view;
//    }
//    
//    private void colorChild(LinearLayout view) {
//        for (int i=0; i< view.getChildCount(); i++) {
//            View v = view.getChildAt(i);
//            if(v instanceof ImageButton) {
//                v.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {                        
//                        if(v instanceof ImageButton) {
//                            ((ImageButton) v).setImageResource(R.drawable.ime_color_box_selected);
//                        }
//                        
//                    }
//                });
//            }   
//            else if(v instanceof LinearLayout){
//                colorChild((LinearLayout) v);
//            }
//        }
//    }
//
//    @Override
//    protected void onDialogClosed(boolean positiveResult) {
//        super.onDialogClosed(positiveResult);
//        if (!positiveResult) {
//            return;
//        }
//        if (shouldPersist()) {
//            persistString(mCurrentValue);
//        }
//        notifyChanged();
//    }
//
//}