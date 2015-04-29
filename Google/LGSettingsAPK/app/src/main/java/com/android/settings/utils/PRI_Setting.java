package com.android.settings.utils;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.lang.Runtime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.os.SystemProperties;
import android.util.Log;
//bwk import com.lge.provider.Andy_Flex;
import android.telephony.TelephonyManager; //bwk
//import com.android.settings.PriSettingsResult;
//import com.android.providers.settings;
//import com.android.providers.settings.SimChangedReceiver;

public class PRI_Setting extends ListActivity {
    private static final String TAG = "SettingPRI";

    public static final String SIM_CHANGED_INFO_ACTION = "lge.intent.action.SIM_CHANGED_INFO";
    public static final String PRI_USER_INFO_CHANGED = "lge.intent.action.ACTION_USER_INFO_CHANGED";
    private static final int VSIM_MCC_MNC = 0;
    //private static final int VSIM_GID = 1;
    //private static final int VSIM_SPN = 2;
    //private static final int VSIM_IMSI = 3;
    private static final int VSIM_PRITEST = 4;
    private static final int VSIM_MAX = 5;

    private static final String STR_TITLE = "title";
    private static final String STR_STATUS = "status";

    private static final String[] mTitles = {
            "MCC-MNC(ex>45000)", "GID", "SPN", "IMSI", "VSIM_PRITEST"
    };
/*
    private static final String[] mStatuses = {
            "Disabled", "Enabled"
    };
*/
    private static final String[] mProperties = {
            "persist.service.vsim.mcc-mnc",
            "persist.service.vsim.gid",
            "persist.service.vsim.spn",
            "persist.service.vsim.imsi",
            "none",
            ""
    };

    private int mDialogId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> entries;
        String property;
        String value;

        for (int i = 0; i < VSIM_MAX; i++) {
            property = mProperties[i];
            value = SystemProperties.get(property, "");
            Log.i(TAG, property + ": [" + value + "]onCreate()");
            entries = new HashMap<String, String>();
            entries.put(STR_TITLE, mTitles[i]);
            /*    if(VSIM_MCC_MNC == i) {
                    entries.put(STR_STATUS, mStatuses[value.equals("1") ? 1 : 0]);
                } else*/ {
                entries.put(STR_STATUS, value);
            }
            list.add(entries);
        }
        /*entries = new HashMap<String, String>();
        entries.put(STR_TITLE, mTitles[VSIM_CHANGED]);
        entries.put(STR_STATUS, "");
        list.add(entries);

        entries = new HashMap<String, String>();
        entries.put(STR_TITLE, mTitles[API_TEST]);
        entries.put(STR_STATUS, "");
        list.add(entries);*/

        /*entries = new HashMap<String, String>();
                entries.put(STR_TITLE, mTitles[VSIM_PRITEST]);
                entries.put(STR_STATUS, "");
                list.add(entries); */

        setListAdapter(new SimpleAdapter(this, list,
                android.R.layout.simple_list_item_2, new String[] { STR_TITLE, STR_STATUS },
                new int[] { android.R.id.text1, android.R.id.text2 }));
        getListView().setTextFilterEnabled(true);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        /*if(position == API_TEST) {
            onApiTest();
            return;
        }*/

        /*if(position == VSIM_PRITEST) {
            Log.i(TAG,  " VSIM_PRITEST select onListItemClick(0)");
                    onPriTest();
                    return;
        }*/
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        if (position < VSIM_MCC_MNC || position > VSIM_MAX + 1) {
            Log.i(TAG, " onListItemClick overflow");
            return;
        }

        String property = mProperties[position];
        String value = SystemProperties.get(property, "");
        Log.i(TAG, property + " [" + position + "]:[" + value + "]onListItemClick(0)");
        switch (position) {
        case VSIM_PRITEST:
            Log.i(TAG, " VSIM_PRITEST select onListItemClick(1)");
            onPriTest();
            break;
        default:
            showDialog(position);
            break;
        }

        Log.i(TAG, property + " set: [" + SystemProperties.get(property, "")
                + "]onListItemClick(2)");
        // update list
        l.setAdapter(l.getAdapter());
    }

    protected void onPriTest() {
        Log.i(TAG, " onPriTest START");
        //this.startActivity(new Intent(this, PriSettingsResult.class));
        String value = SystemProperties.get("persist.service.vsim.mcc-mnc");
        String temp_MCC = value.substring(0, 3);
        String temp_MNC = value.substring(3, 5);

        Log.i(TAG, "MCCMNC" + value + "MCC" + temp_MCC + "MNC" + temp_MNC);
        Log.i(TAG,
                "PRI_IMSI" + SystemProperties.get("persist.service.vsim.imsi") + "PRI_SPN"
                        + SystemProperties.get("persist.service.vsim.spn") + "PRI_GID"
                        + SystemProperties.get("persist.service.vsim.gid"));
        Intent intent = new Intent(PRI_USER_INFO_CHANGED);
        intent.putExtra("PRI_MCC", temp_MCC);
        intent.putExtra("PRI_MNC", temp_MNC);
        intent.putExtra("PRI_IMSI", SystemProperties.get("persist.service.vsim.imsi"));
        intent.putExtra("PRI_SPN", SystemProperties.get("persist.service.vsim.spn"));
        intent.putExtra("PRI_GID", SystemProperties.get("persist.service.vsim.gid"));
        //intent.setClassName("com.android.providers.settings", "com.android.providers.settings.SimChangedReceiver");
        intent.setAction(PRI_USER_INFO_CHANGED);
        //startActivity(intent);
        sendBroadcast(intent);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        mDialogId = id;
        //switch (id) {
        //case VSIM_MCC:
        // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.settings_pri, null);
        return new AlertDialog.Builder(PRI_Setting.this)
                .setTitle(mTitles[id])
                .setView(textEntryView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        ListView l = getListView();
                        Map map = (Map)l.getItemAtPosition(mDialogId);
                        if (map == null)
                        {
                            Log.i(TAG, "onCreateDialog(1) map == null -> finish()");
                            finish();
                            return;
                        }
                        String property = mProperties[mDialogId];
                        String value = ((EditText)((Dialog)dialog).findViewById(R.id.input_edit))
                                .getText().toString();
                        Log.i(TAG, property + " [" + mDialogId + "]:[" + value
                                + "]onCreateDialog(2)");

                        //    set property(start service)
                        SystemProperties.set(property, value);

                        // update status(Enabled)
                        map.remove(STR_STATUS);
                        map.put(STR_STATUS, value);
                        l.setAdapter(l.getAdapter());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
        //}
        //return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        mDialogId = id;
        dialog.setTitle(mTitles[id]);
        EditText text = (EditText)dialog.getWindow().getDecorView().findViewById(R.id.input_edit);
        if (text == null) {
            finish();
        }
        String property = mProperties[id];
        String value = SystemProperties.get(property, "");
        text.setText(value);
    }
}
