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

public class ImagePreference extends Preference {

    ImageView mImageView;
    boolean IsRIdImage = false;
    int RIdImage;

    public ImagePreference(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        // TODO Auto-generated constructor stub
        setLayoutResource(R.layout.image_preference);

    }

    public ImagePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub

    }

    @Override
    protected void onBindView(View arg0) {
        // TODO Auto-generated method stub
        super.onBindView(arg0);
        mImageView = (ImageView)arg0.findViewById(R.id.image_load);
        if(IsRIdImage == true) {
            mImageView.setImageResource(RIdImage);
        }
    }

    public void setImage(int Setimage)
    {
        RIdImage = Setimage;
        IsRIdImage = true;
    }

    public ImageView getImage()
    {
        return mImageView;
    }
}
