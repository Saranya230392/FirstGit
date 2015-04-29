package com.android.settings;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class CustomDialogToast extends Dialog {
    private int mDuration = 0;

    private final int TOAST_LENGTH_DEFAULT_TIME = 1000;
    private final int TOAST_LENGTH_SHORT_TIME = 2000;
    private final int TOAST_LENGTH_LONG_TIME = 4000;

    public CustomDialogToast(Context context, String text, int duration) {
    super(context);
    // TODO Auto-generated constructor stub
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    LayoutInflater inflater = (LayoutInflater)context
                  .getSystemService(android.content.Context.
                   LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.custom_dialog_toast, null);
        TextView tv = (TextView)layout.findViewById(R.id.custom_toast_message);
        tv.setText(text);
        setContentView(layout);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDuration = duration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
        super.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                dismiss();
            }
        }, getDurationTime(mDuration));
    }

    private int getDurationTime(int duration) {
        int returnTime = TOAST_LENGTH_DEFAULT_TIME;
        switch (duration) {
        case Toast.LENGTH_SHORT:
            returnTime = TOAST_LENGTH_SHORT_TIME;
            break;

        case Toast.LENGTH_LONG:
            returnTime = TOAST_LENGTH_LONG_TIME;
            break;

        default:
            break;
        }
        return returnTime;
    }
}