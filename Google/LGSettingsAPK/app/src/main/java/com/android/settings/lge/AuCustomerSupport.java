package com.android.settings.lge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.content.DialogInterface;
import android.preference.Preference.OnPreferenceClickListener;
import android.content.DialogInterface.OnCancelListener;

public class AuCustomerSupport extends SettingsPreferenceFragment {
    private static final String TAG = "AuCustomerSupport";

    private static final String mKeyGotoWebsite = "goto_au_customer_website";
    private static final String mKeyCallCustomerSupport = "call_au_customer_support";

    private static final String mAuCustomerWebsite = "http://cs.kddi.com/?bid=cs-cs-mb-0001";
    private static final String mCustomerSupportNumber = "157";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.au_customer_support);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (mKeyGotoWebsite.equals(preference.getKey())) {
            Uri uri = Uri.parse(mAuCustomerWebsite);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            getActivity().finish();
            return true;
        } else if (mKeyCallCustomerSupport.equals(preference.getKey())) {
            Log.d(TAG, "AlertDialog!!");
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.sp_call_au_customer_support_NORMAL)
                .setMessage(R.string.sp_call_au_customer_support_question_NORMAL)
                .setCancelable(true)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                            placeCall(mCustomerSupportNumber);
                        }
                })
                .create()
                .show();
                return true;
        }

        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void placeCall(String number) {
        TelephonyManager tm = (TelephonyManager)getActivity().getApplicationContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
            intent.setData(Uri.fromParts("tel", number, null));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().getApplicationContext().startActivity(intent);
        }
    }

}
