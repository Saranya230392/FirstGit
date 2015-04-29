package com.android.settings.lgesetting.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.settings.R;

public class DialogFactory {

    public static Dialog createProgressingDialog(Context aContext, int aMessageResourceId, DialogInterface.OnClickListener aCancelClickListener){
        ProgressDialog dialog = new ProgressDialog(aContext);
        // LGE_CHAGE_S [hd.jin@lge.com] 143302 by CA3
        if ( dialog != null ) {
            dialog.setMessage(aContext.getResources().getString(aMessageResourceId));
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                if(aCancelClickListener != null)
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, aContext.getResources().getString(R.string.def_cancel_btn_caption), aCancelClickListener);
        }
        // LGE_CHAGE_E [hd.jin@lge.com] by CA3
        return dialog;
    }

}
