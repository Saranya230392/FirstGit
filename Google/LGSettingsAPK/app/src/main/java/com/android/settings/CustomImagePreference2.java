package com.android.settings;

import android.R.attr;
import android.R.string;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class CustomImagePreference2 extends Preference {

    ImageView mImageView;
    boolean IsRIdImage = false;
    int RIdImage;
//    private static final String IMAGE_TEST = "imagetest";

    public CustomImagePreference2(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        // TODO Auto-generated constructor stub
        setLayoutResource(R.layout.custom_image_preference2);

    }

    public CustomImagePreference2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub

    }

    @Override
    protected void onBindView(View arg0) {
        // TODO Auto-generated method stub
        super.onBindView(arg0);
        mImageView = (ImageView)arg0.findViewById(R.id.custom_image);
        if(IsRIdImage == true)
            mImageView.setImageResource(RIdImage);
    }


    public void setImage(int Setimage)
    {
        RIdImage = Setimage;
        IsRIdImage = true;
    }


}
