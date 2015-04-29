/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * The view of the base input.
 */
public class BaseInputView extends LinearLayout {
    /** The iwnn dialog title */
    private static final String IWNN_DIALOG_TITLE = "iwnndialog";
    /** Dialog DimAmount**/
    private static final float DIALOG_DIMAMOUNT= 0.6f;

    /** The dialog that opens with long tap */
    public AlertDialog mOptionsDialog = null;

    /**
     * Constructor
     * @param context the Context
     * @param attrs the AttributeSet
     */
    public BaseInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     * @param context the Context
     */
    BaseInputView(Context context) {
        super(context);
    }

    /**
     * Called when the window containing has change its visibility.
     * 
     * @see android.view.View#onWindowVisibilityChanged(int)
     */
    @Override protected void onWindowVisibilityChanged(int visibility) {
       super.onWindowVisibilityChanged(visibility);
       if ((visibility != VISIBLE) && (mOptionsDialog != null)) {
           mOptionsDialog.dismiss();
       }
    }

    /**
     * Show dialog.
     *
     * @param builder   the builder of dialog
     */
    public void showDialog(AlertDialog.Builder builder) {
        if (mOptionsDialog != null) {
            mOptionsDialog.dismiss();
        }

        // Get the token of dialog.
        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams dialogLayoutParams = window.getAttributes();
        window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
        WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialogLayoutParams.dimAmount = DIALOG_DIMAMOUNT;
        dialogLayoutParams.setTitle(IWNN_DIALOG_TITLE);
        dialogLayoutParams.token = getWindowToken();
        dialogLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(dialogLayoutParams);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                   
        // Show the dialog.
        mOptionsDialog.show();
    }

    /**
     * Close dialog.
     */
    public void closeDialog() {
       if (mOptionsDialog != null) {
           mOptionsDialog.dismiss();
           mOptionsDialog = null;
       }
    }
}
