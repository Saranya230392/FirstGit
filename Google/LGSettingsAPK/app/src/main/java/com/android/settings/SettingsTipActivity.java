/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManagerGlobal;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Button;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.lgesetting.Config.Config;

public class SettingsTipActivity extends Activity {   
	private static final String LOG_TAG = "SettingsTipActivity";
	private  AddNewSettingsTipFragment mSecondFragment;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_traslucent);

        if (savedInstanceState != null) {
            if (mSecondFragment != null) {
                mSecondFragment.dismiss();
                attachFragments();
            }
        } else {
            attachFragments();
        }
    }

    private void attachFragments() {
        mSecondFragment = new AddNewSettingsTipFragment();
        mSecondFragment.show(getFragmentManager(), "AddNewSettingsTipFragment");
    }

     //SecondFragment Will have one image and one text box
    public static class AddNewSettingsTipFragment extends DialogFragment implements OnCheckedChangeListener, OnClickListener {
        private CheckBox visible;
        public Dialog dialog;
        public ImageView mImage;
    	public LinearLayout mLayout;
    	public int mLayoutWidth;
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            dialog = new Dialog(getActivity(), R.style.InsertOrEditGroupDialogTheme);
            dialog.setContentView(R.layout.settings_tip);
            mImage = (ImageView)dialog.findViewById(R.id.tips_image);  
            
            if (Utils.isUI_4_1_model(getActivity())) {
	            mLayout = (LinearLayout)dialog.findViewById(R.id.settings_tips_layout);
	            if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
	            	mLayoutWidth = (int)(getActivity().getResources().getDisplayMetrics().widthPixels * 0.95);
	            } else {
	            	mLayoutWidth = (int)(getActivity().getResources().getDisplayMetrics().widthPixels * 0.65);
	            }
	            mLayout.setLayoutParams(new android.widget.LinearLayout.LayoutParams(mLayoutWidth, LayoutParams.WRAP_CONTENT));
            } else {
                IWindowManager mWindowManagerService;
                mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
                try {
                    boolean showNav = mWindowManagerService.hasNavigationBar();
                    if (showNav == false || SystemProperties.getBoolean("lge.hw.frontkeyled", false)) {
                        mImage.setImageResource(R.drawable.help_settings_tip_hard_key);
                    } 
                } catch (RemoteException e) { 
                    Log.d(LOG_TAG, "RemoteException front key");
                }
            }
            visible = (CheckBox)dialog.findViewById(R.id.guide_tip_text_checkbox);
            visible.setOnCheckedChangeListener(this);
            visible.setOnClickListener(this);
            visible.setChecked(true);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        getActivity().finish();
                    }
                    return false;
                }
            });

            final Button closeButton = (Button)dialog.findViewById(R.id.guide_tip_button);

            closeButton.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (visible.isChecked()) {
                        Settings.System.putInt(getActivity().getContentResolver(), "help_settings_settings_tips", 0);
                    } else {
                        Settings.System.putInt(getActivity().getContentResolver(), "help_settings_settings_tips", 1);
                    }

                    getActivity().finish();
                }
            });
            dialog.show();
            return dialog;
        }

        @Override
        public void onStart() {
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().setCancelable(false);
            super.onStart();
        }

        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

        }

        @Override
        public void onClick(View arg0) {
			
		}
		
    }
}