package com.android.settings.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.settings.R;

import java.util.List;

public class CheckPackage extends Activity {
    TextView mPackageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_package);
        mPackageText = (TextView)findViewById(R.id.package_txt);

        isCallable(new Intent().setClassName("com.lge.ir.remote.settings",
                "com.lge.ir.remote.settings.MainSettingsPrefAct"), "Qremote");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.lge.settings.ONE_HAND_SETTINGS"), "One-handed Operation");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.lge.settings.HOTKEY_SETTINGS"), "QuickButton");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.android.settings.Settings$WirelessSettingsActivity"), "More...");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.android.settings.Settings$TetherNetworkSettingsActivity"),
                "Tethering and networks");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.android.settings.Settings$ShareConnectionActivity"), "Share and Connect");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.android.settings.Settings$DataUsageSummaryActivity"), "Mobile data");
        isCallable(new Intent().setClassName("com.android.settingsaccessibility",
                "com.android.settingsaccessibility.SettingsAccessibilityActivity"),
                "LG Accessibility");
        isCallable(new Intent().setClassName("com.nttdocomo.android.fota",
                "com.nttdocomo.android.fota.screens.DmcFota"), "Software Update");
        isCallable(new Intent().setClassName("com.nttdocomo.android.osv",
                "com.nttdocomo.android.osv.StartupActivity"), "Upgrade Android software");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.android.settings.inputmethod.SpellCheckersSettings"), "Spelling correction");
        isCallable(new Intent().setClassName("com.android.settings",
                "com.android.settings.TextLinkSettings"), "Text Link");

        appIsEnabled("com.nttdocomo.android.cloudset", "Docomo Cloud");
        appIsEnabled("com.nttdocomo.android.docomoset", "Docomo Service");
    }

    private void isCallable(Intent intent, String menuName) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            mPackageText.setText(mPackageText.getText() + menuName + " is Displayed." + "\n");
        } else {
            mPackageText.setText(mPackageText.getText() + menuName + " is removed." + "\n");
        }
    }

    /*
     * Use this method instead of 'isCallable()', if you know only the package name.
     */
    public void appIsEnabled(String packageName, String menuName) {
        mPackageText.setText(mPackageText.getText() + menuName + " is Displayed." + "\n");
    }
}
