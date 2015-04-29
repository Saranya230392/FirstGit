package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lge.constants.SettingsConstants;

import java.util.List;

public class EasyToUse extends Activity implements OnClickListener {

    private RadioButton easyBtn;
    private RadioButton normalBtn;
    private Button previewBtn;
    private Button applyBtn;

    private ImageView staterImg;
    private ImageView standardImg;
    //private static boolean CheckIntent = false;
    private static View checkEnable;
    private static final int STARTER = 1;
    private static final int STANDARD = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        super.onCreate(savedInstanceState);
        setContentView(R.layout.easy_to_use);

        easyBtn = (RadioButton)findViewById(R.id.easy_button);
        easyBtn.setOnClickListener(this);

        normalBtn = (RadioButton)findViewById(R.id.normal_button);
        normalBtn.setOnClickListener(this);

        previewBtn = (Button)findViewById(R.id.preview_button);
        previewBtn.setOnClickListener(this);

        applyBtn = (Button)findViewById(R.id.apply_button);
        applyBtn.setOnClickListener(this);

        staterImg = (ImageView)findViewById(R.id.easy_img);
        staterImg.setOnClickListener(this);

        standardImg = (ImageView)findViewById(R.id.normal_img);
        standardImg.setOnClickListener(this);

        if (savedInstanceState != null) {
            int checkMode = savedInstanceState.getInt("mode");
            if (checkMode == STARTER) {
                checkEnable = easyBtn;
            } else {
                checkEnable = normalBtn;
            }
            btnCheck(checkEnable);
        } else {
            updateBtn();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        if (checkEnable == easyBtn) {
            outState.putInt("mode", STARTER);
        } else {
            outState.putInt("mode", STANDARD);
        }
    }

    public void onClick(View view) {
        // TODO Auto-generated method stub
        //Intent intent = new Intent(EasyToUse.this, EasyToUseSelectDescript.class);
        switch (view.getId()) {

        case R.id.easy_button:
            btnCheck(easyBtn);
            break;
        case R.id.normal_button:
            btnCheck(normalBtn);
            break;
        case R.id.easy_img:
            btnCheck(easyBtn);
            break;
        case R.id.normal_img:
            btnCheck(normalBtn);
            break;
        case R.id.preview_button:
            previewOpen();
            break;
        case R.id.apply_button:
            showDialog();
            break;

        default:
            break;
        }
    }

    public void updateBtn()
    {
        int checkDB = android.provider.Settings.System.getInt(getContentResolver(),
                "ui_type_settings", 0);

        if (checkDB == 0) {
            btnCheck(normalBtn);
        }
        else {
            btnCheck(easyBtn);
        }
    }

    public void btnCheck(View view)
    {
        switch (view.getId()) {
        case R.id.easy_button:
            easyBtn.setChecked(true);
            normalBtn.setChecked(false);
            break;
        case R.id.normal_button:
            easyBtn.setChecked(false);
            normalBtn.setChecked(true);
            break;
        default:
            break;
        }
    }

    public void updateEasyUiDB()
    {
        int updateDB = 0;
        if (easyBtn.isChecked()) {
            updateDB = 1;
        } else if (normalBtn.isChecked()) {
            updateDB = 0;
        }
        android.provider.Settings.System.putInt(getContentResolver(), "ui_type_settings", updateDB);
        openHome();
    }

    public void previewOpen() {
        Intent intent = new Intent(EasyToUse.this, EasyToUseSelectDescript.class);

        if (easyBtn.isChecked()) {
            intent.putExtra("mode_key", "starter");
            startActivity(intent);
        } else {
            intent.putExtra("mode_key", "standard");
            startActivity(intent);
        }
    }

    public void openHome() {
        finish();

        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        am.forceStopPackage("com.lge.settings.easy");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            int settingStyle = Settings.System.getInt(getContentResolver(),
                    SettingsConstants.System.SETTING_STYLE, 0);
            if (settingStyle == 1 && Utils.supportEasySettings(this)) {
                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> info = am.getRunningTasks(1);
                if (info != null) {
                    ComponentName topActivity = info.get(0).topActivity;
                    String topActivityClassName = topActivity.getClassName();
                    String baseActivityClassName = info.get(0).baseActivity.getClassName();
                    Log.d("kimyow", "top=" + topActivityClassName + "  base="
                            + baseActivityClassName);
                    if ("com.lge.settings.easy.EasySettings".equals(baseActivityClassName)) {
                        onBackPressed();
                        return true;
                    } else {
                        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        String tabIndex = Utils.getEasyTabIndex(topActivityClassName);
                        Log.d("kimyow", "tabIndex=" + tabIndex);
                        settings.putExtra(Intent.EXTRA_TEXT, tabIndex);
                        startActivity(settings);
                        finish();
                        return true;
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public String getEasyTabIndex(String topActivity) {
        if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity)) {
            return "sound";
        } else if ("com.android.settings.Settings$ManageApplicationsActivity".equals(topActivity) ||
                "com.android.settings.Settings$LocationSettingsActivity".equals(topActivity) ||
                "com.android.settings.Settings$AccountSyncSettingsActivity".equals(topActivity)) {
            return "general";
        }
        return null;
    }

    public void showDialog() {
        String msg = null;

        if (easyBtn.isChecked()) {
            msg = getResources().getString(R.string.easytouse_popup_starter_desc);
        } else {
            msg = getResources().getString(R.string.easytouse_popup_standard_desc);
        }

        new AlertDialog.Builder(EasyToUse.this)
                .setMessage(msg)
                .setTitle(R.string.easytouse_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        updateEasyUiDB();
                    }
                })
                .setNegativeButton(R.string.easytouse_cancel, null)
                .show();
    }
}
