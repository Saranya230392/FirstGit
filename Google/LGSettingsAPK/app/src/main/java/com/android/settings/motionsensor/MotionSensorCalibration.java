/*
 * Copyright (C) 2008 The Android/ Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.motionsensor;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.util.Log;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.qualcomm.sensors.sensortest.SensorID.SensorType;
import com.qualcomm.sensors.sensortest.SensorUserCal;
import com.android.settings.SettingsPreferenceFragment;

import android.app.ActionBar;
import android.view.MenuItem;

public class MotionSensorCalibration extends SettingsPreferenceFragment implements SensorEventListener {
    private static final String TAG = "MotionSensorCalibration";
    private static final int WRITE_COUNT = 2;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Button resetsensorbutton;
    private ProgressDialog mProgress;
    private ProgressDialog mBeforeProgress;
    private boolean mStart = true;
    private boolean mTesting = false;
    private boolean mUseProp = false;
    private long mTestTime = 0;
    private Thread mThread = null;

    private String mReadSysFs;
    private String mWriteSysFs;

    private View rootview;
    private ViewGroup mContainer;

    enum DataType { PRIMARY, SECONDARY };

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
    public static final int NOT_FLAT = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity())) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        mContainer = container;
        rootview = inflater.inflate(R.layout.motion_sensor_calibration, container, false);
        initControl();
        return rootview;
    }
    public void initControl() {
        if ("tegra".equals(SystemProperties.get("ro.board.platform"))) {
            mReadSysFs = "/sys/devices/platform/tegra14-i2c.0/i2c-0/0-0011/cal_result";
            mWriteSysFs = "/sys/devices/platform/tegra14-i2c.0/i2c-0/0-0011/run_calibration";
            Log.d("TAG ", "tegra : " + mWriteSysFs);
            mUseProp = true;
        } else if ("mtk".equals(SystemProperties.get("ro.lge.chip.vendor"))) {
            mReadSysFs = "/sys/bus/platform/drivers/gsensor/run_fast_calibration";
            mWriteSysFs = "/sys/bus/platform/drivers/gsensor/run_fast_calibration";
            Log.d("TAG ", "MTK : " + mWriteSysFs);
            mUseProp = true;
        } else if ("mpu".equals(SystemProperties.get("ro.lge.sensor_chip"))) {
            mReadSysFs = "/sys/bus/iio/devices/iio:device0/run_calibration";
            mWriteSysFs = "/sys/bus/iio/devices/iio:device0/run_calibration";
            Log.d("TAG ", "mpu : " + mWriteSysFs);
            mUseProp = true;
        } else if ("qct_kernel".equals(SystemProperties.get("ro.lge.sensor_chip"))) {
            sensorDevices();
            Log.d("TAG ", "qct_kernel");
            mUseProp = true;
        }

        Log.d(TAG, "onCreate() : mReadSysFs = " + mReadSysFs);
        Log.d(TAG, "onCreate() : mWriteSysFs = " + mWriteSysFs);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        resetsensorbutton = (Button)rootview.findViewById(R.id.resetsensorbutton);
        mProgress = new ProgressDialog(resetsensorbutton.getContext());
        if (mProgress != null) {
            mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgress.setMessage(getString(R.string.sp_sensor_calibrating_NORMAL));
            mProgress.setCancelable(false);
            mProgress.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (mProgress != null) {
                        mProgress.cancel();
                        }
                        mTesting = false;
                        Toast.makeText(getActivity().getBaseContext(),
                                R.string.sp_sensor_calibrate_fail_and_again_NORMAL,
                                Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                        return true;
                    }
                    return false;
                }
            });

            resetsensorbutton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mStart) {
                        mTestTime = android.os.SystemClock.uptimeMillis();
                        mStart = false;
                        if (mProgress != null) {
                            mProgress.show();
                            if (mThread == null) {
                                mThread = new Thread(new CalThreadRunnable());
                                mThread.start();
                            }
                        }
                        mTesting = true;
                    }
                }
            });
        }
    }

    public void initButton() {
        resetsensorbutton = (Button)rootview.findViewById(R.id.resetsensorbutton);
        resetsensorbutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mStart) {
                    mTestTime = android.os.SystemClock.uptimeMillis();
                    mStart = false;
                    //mProgress.show();
                    if (mProgress != null) {
                        mProgress.show();
                        if (mThread == null) {
                            mThread = new Thread(new CalThreadRunnable());
                            mThread.start();
                        }
                    }
                    mTesting = true;
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("TAG", "onConfigurationChanged()");

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootview = inflater.inflate(R.layout.motion_sensor_calibration, mContainer, false);
        initButton();
        ViewGroup rootView = (ViewGroup)getView();
        rootView.removeAllViews();
        rootView.addView(rootview);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        mSensorManager.registerListener(this, 
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        if (mBeforeProgress != null) {
            mProgress = mBeforeProgress;
                if (!mTesting && mStart) {
                    mProgress.dismiss();
                }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if (mThread != null) {
            mThread = null;
        }
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent arg0) {
    }

    private static DataType getDataType(Sensor sensor) {
        switch(sensor.getType()) {
            case Sensor.TYPE_PROXIMITY : 
                return DataType.PRIMARY;
            case Sensor.TYPE_LIGHT :
                return DataType.SECONDARY;
            default :
                return DataType.PRIMARY;
        }
    }

    private class CalThreadRunnable implements Runnable {
        public void run() {
            while (mThread != null) {
                if (!mUseProp) {
                    int result = -1;
    
                    if (mTesting) {
                        if (android.os.SystemClock.uptimeMillis() - mTestTime > 1000 * 10) {
                            Log.d(TAG, "Time out");
                            result = 1;
                            handler.sendEmptyMessage(result);
                            break;
                        }
                        
                        SensorType type = SensorType.getSensorType(mSensor);
                        if (type == null) {
                            Log.d(TAG, "error");
                            result = -2;
                            handler.sendEmptyMessage(result);
                            break;
                        } else {
                            switch (SensorUserCal.performUserCal((byte)type.typeValue(), (byte)getDataType(mSensor).ordinal())) {
                                case 0:
                                    Log.d(TAG, "pass");
                                    result = 0;
                                    handler.sendEmptyMessage(result);
                                    break;
                                case 2:
                                    Log.d(TAG, "not flat");
                                    result = 2;
                                    handler.sendEmptyMessage(result);
                                    break;
                                default :
                                  Log.d(TAG, "fail");
                                  result = 1;
                                  handler.sendEmptyMessage(result);
                                  break;
    
                            }
                        }
                    }
    
                    if (result >= 0) {
                        break;
                    }
                } else { // mUserProp == true
                    if (mTesting) {
                        long testTime = android.os.SystemClock.uptimeMillis();

                        boolean resWriteToFile = true;
                            for (int i = 0; i < WRITE_COUNT; i++) {
                                resWriteToFile = WriteToFile();
                                SystemClock.sleep(1000);
                                if (resWriteToFile) {
                                    break;
                                }
                            }
                            if (!resWriteToFile) {
                                Log.d(TAG, "Write error");
                                handler.sendEmptyMessage(FAILED);
                                break;
                            }

                        while (true) {
                            if (android.os.SystemClock.uptimeMillis() - testTime > 1000 * 10) {
                                Log.d(TAG, "Time out");
                                handler.sendEmptyMessage(FAILED);
                                break;
                            }

                            int res = ReadIntFromFile();
                            if (res == SUCCESS) {
                                Log.d(TAG, "success");
                                handler.sendEmptyMessage(SUCCESS);
                                break;
                            } else if (res == FAILED) {
                                Log.i(TAG, "fail");
                                handler.sendEmptyMessage(FAILED);
                                break;
                            } else if (res == NOT_FLAT) {
                                Log.i(TAG, "not flat");
                                handler.sendEmptyMessage(NOT_FLAT);
                                break;
                            } else {
                                Log.i(TAG, "Do nothing");
                            }
                            SystemClock.sleep(500);
                        }
                    }
                }
            }
            SystemClock.sleep(500);
        }
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mTesting) {

                if (getActivity() == null)  {
                    return;
                }

                switch (msg.what) {
                case SUCCESS:
                    Toast.makeText(getActivity(), R.string.sp_sensor_calibrated_NORMAL,
                            Toast.LENGTH_SHORT).show();
                        break;

                case NOT_FLAT:
                        Toast.makeText(getActivity().getBaseContext(),
                            R.string.sp_gesture_motion_not_flat,
                            Toast.LENGTH_SHORT).show();
                        break;

                default:
                        Toast.makeText(getActivity().getBaseContext(),
                            R.string.sp_sensor_calibrate_fail_and_again_NORMAL,
                            Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            mTesting = false;
            if (mProgress != null) {
                mProgress.dismiss();
            }
            try {
                mThread.interrupt();
                mThread = null;
                mStart = true;
                if (msg.what == SUCCESS) {
                    getActivity().onBackPressed();
                }
            } catch (Exception e) {
                Log.d(TAG, "Motion sensor Exception : " + e);
            }
        }
    };

    public int ReadIntFromFile() {
        int result = 1;
        int ret = FAILED;
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(mReadSysFs), 8);
            String in = inReader.readLine();
            if (in != null) {
                result = Integer.parseInt(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inReader != null) {
                    inReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "read result " + result);

        if ("mtk".equals(SystemProperties.get("ro.lge.chip.vendor")) ||
                "mpu".equals(SystemProperties.get("ro.lge.sensor_chip")) ) {
            ret = (result == 0) ? SUCCESS : FAILED;
        } else {
            ret = (result == 1) ? SUCCESS : FAILED;
        }

        if (result == 2) {
            ret = NOT_FLAT;
        }

        return ret;
    }

    public boolean WriteToFile() {
        BufferedWriter out = null;
        try {
            String strValue;
            strValue = "2";
            if ("tegra".equals(SystemProperties.get("ro.board.platform"))) {
                strValue = "1";
            }
            out = new BufferedWriter(new FileWriter(mWriteSysFs));
            out.write(strValue);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            // only trying to Write for BSP calibration.
            return true;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    //iamjun.lee Actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBeforeProgress = mProgress;
        mProgress = null;
    }

    private void sensorDevices() {
        final String procfs_input = "/proc/bus/input/devices";

        try {
            BufferedReader devices = new BufferedReader(new FileReader(procfs_input));
            String line = null;
            boolean hasSensor = false;

            while ((line = devices.readLine()) != null) {
                if (-1 != line.indexOf("N:") && -1 != line.indexOf("accelerometer")) {
                    hasSensor = true;

                }
                if (-1 != line.indexOf("S:") && true == hasSensor) {
                    break;
                }
                line = null;
            }
            if (null != line) {
                Log.i(TAG, "S: " + line);
                //S: Sysfs=
                line = line.replace("S: Sysfs=", "");
                mReadSysFs = "/sys" + line + "/run_fast_calibration";
                mWriteSysFs = "/sys" + line + "/run_fast_calibration";
                Log.i(TAG, "path : " + line);
            }
            else {
                Log.i(TAG, "not found sensor");
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FNFE" + e.getMessage().toString());
        } catch (IOException e) {
            Log.e(TAG, "IOE" + e.getMessage().toString());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage().toString());
        }
    }
}
