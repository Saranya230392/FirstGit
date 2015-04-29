package com.android.settings.utils;

import com.android.settings.R;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import com.lge.systemservice.core.LGContext;
import com.lge.systemservice.core.VolumeVibratorManager;

public class CheckApi extends Activity {
    EditText mVibrateInput;
    EditText mVibrateInputNoti;
    EditText mVibrateInputTouch;
    Button mCheckApi;
    Vibrator mVibrator;
    TextView mResult;
    VolumeVibratorManager mVolumeVibrator;
    LGContext mServiceContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.check_api);

        mVibrateInput = (EditText)findViewById(R.id.edt_vibrate_strength);
        mVibrateInputNoti = (EditText)findViewById(R.id.edt_vibrate_strength_noti);
        mVibrateInputTouch = (EditText)findViewById(R.id.edt_vibrate_strength_touch);
        mCheckApi = (Button)findViewById(R.id.btn_check);
        mResult = (TextView)findViewById(R.id.txv_result);
        mServiceContext = new LGContext(this.getApplicationContext());
        mVolumeVibrator = (VolumeVibratorManager)mServiceContext.getLGSystemService(LGContext.VOLUMEVIBRATOR_SERVICE);
        mResult.setText("Incoming calls vibrate strength : " + mVolumeVibrator.getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_RING) + "\n"
                + "Notification vibrate strength      : " + mVolumeVibrator.getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION) + "\n"
                + "Vibrate on touch strength         : " +  mVolumeVibrator.getVibrateVolume(VolumeVibratorManager.VIBRATE_TYPE_HAPTIC));
        mCheckApi.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mResult.setText(null);
                CheckVibrateStrength(Integer.parseInt(mVibrateInput.getText().toString()), VolumeVibratorManager.VIBRATE_TYPE_RING);
                CheckVibrateStrength(Integer.parseInt(mVibrateInputNoti .getText().toString()), VolumeVibratorManager.VIBRATE_TYPE_NOTIFICATION);
                CheckVibrateStrength(Integer.parseInt(mVibrateInputTouch .getText().toString()), VolumeVibratorManager.VIBRATE_TYPE_HAPTIC);

            }
        });


    }

    private void CheckVibrateStrength(int input, int type) {
        //mVibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);

        //mVibrator.setVibrateVolume(input);
        mVolumeVibrator.setVibrateVolume(type, input);
        mResult.setText(mResult.getText() + "Set Vibrate strength : " + input + "\nGet Vibrate strength : " + mVolumeVibrator.getVibrateVolume(type)+ "\n");
        if(input ==  mVolumeVibrator.getVibrateVolume(type)) {
            mResult.setText(mResult.getText() + "      PASS \n");
        } else {
            mResult.setText(mResult.getText() + "      Fail \n");

        }


    }


}
