package com.android.settings.sdencryption;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;


public class SDEncryptionSummaryPreference extends Preference {
    //private static final String TAG = "SDEncryptionSummaryPreference";
    private static final int LEFT_PADDING = 0;
    public static final int SD_ENCRYPT_ENABLED_MSG = 0;
    public static final int SD_ENCRYPT_BATTERY_MSG = 1;
    public static final int SD_ENCRYPT_PLUG_MSG = 2;
    public static final int SD_ENCRYPT_NO_PLUG_NO_BATTERY = 3;

    private Context mContext;
    //private View mView;
    private StringBuilder mSummary;

    private int mSupport_msg = SD_ENCRYPT_BATTERY_MSG;
    private boolean mIsEncrypted = false;
    private static boolean sIsCheckedFull = false;

    public SDEncryptionSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
    }
    
    public SDEncryptionSummaryPreference(Context context, int msg_type) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
        mSupport_msg = msg_type;
        setLayoutResource(R.layout.sd_encryption_summary_preference);
    }

    public SDEncryptionSummaryPreference(Context context, int msg_type, boolean isEncrypted) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
        mSupport_msg = msg_type;
        mIsEncrypted = isEncrypted;
        setLayoutResource(R.layout.sd_encryption_summary_preference);
    }

    @Override
    protected void onBindView(View view) {
        // TODO Auto-generated method stub
        super.onBindView(view);
        mSummary = new StringBuilder();

        //((TextView)view.findViewById(android.R.id.title)).setVisibility(View.GONE);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        //((TextView)view.findViewById(android.R.id.summary))
        //.setTextAppearance(mContext, android.R.attr.textAppearanceLarge);
        updatedDecryptDesc(sIsCheckedFull);

        if (mSupport_msg == SD_ENCRYPT_BATTERY_MSG) {
            setSummary("");
        } else if (mSupport_msg == SD_ENCRYPT_ENABLED_MSG){
            TextView title = (TextView)view.findViewById(android.R.id.title);
            int mTitleString = R.string.sp_storage_sd_card_full_decrypt1_NORMAL;
            if (Utils.isSupportUIV4_2()) {
                mTitleString = R.string.sp_storage_sd_card_full_decrypt1_new_NORMAL_version4_2;
            }
            setTitle(mContext.getString(mTitleString));
            title.setPaddingRelative(title.getPaddingStart(),
                             title.getPaddingTop(),
                             title.getPaddingEnd(),
                             LEFT_PADDING);
        } else if (mSupport_msg == SD_ENCRYPT_PLUG_MSG) {
            if (mIsEncrypted) {
                setTitle(mContext.getString(R.string.battery_power_decrypt_change));
            } else {
                setTitle(mContext.getString(R.string.battery_power_encrypt_change_new));
            }
            setSummary("");
        }  else if (mSupport_msg == SD_ENCRYPT_NO_PLUG_NO_BATTERY) {
            setSummary("");
        }
    }
   
    public void updatedDecryptDesc(boolean isCheckedFull) {
        sIsCheckedFull = isCheckedFull;
        int mDescriptionMessage = R.string.sp_storage_sd_card_full_decrypt2_dynamic_new;
        int mButton = R.string.sp_sd_encryption_dis_button_NORMAL;
        if (Utils.isSupportUIV4_2()) {
            mDescriptionMessage = R.string.sp_storage_sd_card_full_decrypt2_dynamic_new_version4_2;
        }
        if (sIsCheckedFull) {
            mButton = R.string.sp_full_encryption_disable_NORMAL;
        }
        if (null == mSummary) {
            mSummary = new StringBuilder();
        }
        mSummary.append(System.getProperty("line.separator"));
        mSummary.append(mContext.getString(mDescriptionMessage,
                mContext.getString(mButton)));
        mSummary.append(System.getProperty("line.separator"));
        mSummary.append(System.getProperty("line.separator"));
        mSummary.append(mContext.getString(R.string.sp_storage_sd_card_full_decrypt4_NORMAL));
        setSummary(mSummary.toString());  
    }
    
    public void addSummary(String str) {
        if (null == mSummary) {
            mSummary = new StringBuilder();
            updatedDecryptDesc(sIsCheckedFull);
        }
        mSummary.append(System.getProperty("line.separator"));
        mSummary.append(System.getProperty("line.separator"));
        mSummary.append(str);
        setSummary(mSummary.toString());
    }
}
