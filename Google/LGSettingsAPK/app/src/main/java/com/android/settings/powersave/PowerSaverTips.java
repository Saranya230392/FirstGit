package com.android.settings.powersave;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import android.os.SystemProperties;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.lge.OverlayUtils;
import com.android.settings.lgesetting.Config.Config;

import com.lge.constants.SettingsConstants;

public class PowerSaverTips extends SettingsPreferenceFragment {
    private Button mOKBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // initialize the inflater
        if (container == null) {
            return null;
        }

        LayoutInflater mInflater = inflater;

        View rootView = mInflater.inflate(R.layout.powersave_tips,
                container, false);

        // jongtak0920.kim 120820 Remove NFC if its not available [START]
        if (NfcAdapter.getDefaultAdapter(getActivity()) == null
                || !"KR".equals(Config.getCountry())) {
            ((LinearLayout)rootView.findViewById(R.id.power_save_nfc)).setVisibility(View.GONE);
        }
        // jongtak0920.kim 120820 Remove NFC if its not available [END]

        if (!Utils.supportFrontTouchKeyLight()) {
            ((LinearLayout)rootView.findViewById(R.id.power_save_front_touch_light))
                    .setVisibility(View.GONE);
        }

        ((LinearLayout)rootView.findViewById(R.id.power_save_front_light)).setVisibility(View.GONE);

        // Remove haptic feedback for U0
        String hapticfeedback = SystemProperties.get("ro.device.hapticfeedback", "1");
        if (hapticfeedback.equals("0")) {
            ((LinearLayout)rootView.findViewById(R.id.power_save_touch)).setVisibility(View.GONE);
        }

        ((LinearLayout)rootView.findViewById(R.id.power_save_tip_eco_mode))
                .setVisibility(View.GONE);

        // Remove Wifi for VZW
        if ((Config.getOperator().equals("VZW")) &&
                (android.provider.Settings.System.getInt(this.getContentResolver(),
                        SettingsConstants.System.VZW_HIDDEN_FEATURE_WIFI, 0) == 1)) {
            Log.d("PowerSaverTips", "##feature remove battery - Help - Wi-Fi");
            ((LinearLayout)rootView.findViewById(R.id.power_save_tip_wifi))
                    .setVisibility(View.GONE);
        }

        // jongtak0920.kim Add Notification LED and Emotional LED
        if (Utils.supportEmotionalLED(getActivity())) {
            if (Utils.supportHomeKey(getActivity())) {
                ((LinearLayout)rootView.findViewById(R.id.power_save_notification_led))
                        .setVisibility(View.GONE);
                if ("VZW".equals(Config.getOperator())) {
                    ((LinearLayout)rootView.findViewById(R.id.power_save_emotional_led))
                            .setVisibility(View.GONE);
                    ((LinearLayout)rootView.findViewById(R.id.power_save_notification_led))
                            .setVisibility(View.VISIBLE);
                }
            } else {
                ((LinearLayout)rootView.findViewById(R.id.power_save_emotional_led))
                        .setVisibility(View.GONE);
            }
        } else {
            ((LinearLayout)rootView.findViewById(R.id.power_save_emotional_led))
                    .setVisibility(View.GONE);
            ((LinearLayout)rootView.findViewById(R.id.power_save_notification_led))
                    .setVisibility(View.GONE);
        }

        // jongtak0920.kim Add Auto-adjust screen tone
        if (!Utils.isOLEDModel(getActivity())) {
            ((LinearLayout)rootView.findViewById(R.id.power_save_oled)).setVisibility(View.GONE);
        }

        if (Config.getFWConfigBool(getActivity(),
                com.lge.R.bool.config_using_lollipop_cover,
                "com.lge.R.bool.config_using_lollipop_cover") == false) {
            ((LinearLayout)rootView.findViewById(R.id.power_save_quick_case))
                    .setVisibility(View.GONE);
        }

        mOKBtn = (Button)rootView.findViewById(R.id.tips_ok_button);

        if (Utils.supportSplitView(getActivity())) {
            ((LinearLayout)rootView.findViewById(R.id.powersaver_tip_layout))
                    .setVisibility(View.GONE);
        }
        mOKBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finishFragment();
            }
        });

        return rootView;
    }
}
