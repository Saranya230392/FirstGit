/*
 * Copyright (C) 2008-2013  OMRON SOFTWARE Co., Ltd.  All Rights Reserved.
 */
package jp.co.omronsoft.iwnnime.ml;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The preference class of WebAPI apk list for Japanese IME.
 * 
 * @author OMRON SOFTWARE Co., Ltd.
 */
public class WebApiListPreference extends MultiChoicePreference {
    /** The maximum number of webapi list */
    public final static int MAX_ADD_WEBAPI_LIST = 5;

    /**
     * Constructor
     */
    public WebApiListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** @see android.preference.DialogPreference#onDialogClosed */
    @Override protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        PackageManager pm = getContext().getPackageManager();

        Intent intent = new Intent(WebAPIWnnEngine.WEBAPI_ACTION_CODE);
        List<ResolveInfo> resolveInfo = pm.queryBroadcastReceivers(intent, /* no flags */ 0);
        Collections.sort(resolveInfo, new ResolveInfo.DisplayNameComparator(pm));

        int infoSize = resolveInfo.size();
        CharSequence[] entries = new CharSequence[infoSize];
        CharSequence[] entryValues = new CharSequence[infoSize];

        for(int i = 0; i < infoSize; i++) {
            ResolveInfo info = resolveInfo.get(i);
            ActivityInfo actInfo = info.activityInfo;

            CharSequence label;
            label = info.loadLabel(pm);
            if (label == null) {
                if (actInfo != null) {
                    label = actInfo.name;
                } else {
                    label = "";
                }
            }
            entries[i] = label;

            if (actInfo != null) {
                entryValues[i] = actInfo.name;
            } else {
                entryValues[i] = "";
            }
        }

        setEntries(entries);
        setEntryValues(entryValues);

        super.onPrepareDialogBuilder(builder);
    }

    /**
     * Return whether the WebAPI is enabled.
     * @param context Context
     * @return {@code true} if enable.
     */
    public static boolean isEnableWebApi(Context context) {
        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(WebAPIWnnEngine.WEBAPI_ACTION_CODE);
        List<ResolveInfo> resolveInfo = pm.queryBroadcastReceivers(intent, /* no flags */ 0);

        return (resolveInfo.size() != 0);
    }

    /** @see android.preference.ListPreference#onDialogClosed */
    @Override protected void onDialogClosed(boolean positiveResult) {
        Set<String> oldValue = getValues();

        super.onDialogClosed(positiveResult);

        final Set<String> selectedValue = getValues();

        if (positiveResult) {
            final int count = getCheckedCount();
            if (((oldValue == null) || oldValue.isEmpty()) && (selectedValue != null) && !selectedValue.isEmpty()) {
                setValues(new HashSet<String>());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.ti_preference_attention_dialog_title_txt)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.ti_preference_webapi_dialog_message_txt)
                    .setPositiveButton(R.string.ti_dialog_button_ok_txt,
                                       new DialogInterface.OnClickListener() {
                                           public void onClick(DialogInterface dialog, int which) {
                                               WebApiListPreference.this.setValues(selectedValue);
                                               if (count > 0) {
                                                   Toast.makeText(getContext(),
                                                           getContext().getString(R.string.ti_preference_load_comp_message_txt, count, count),
                                                           Toast.LENGTH_LONG).show();
                                               }
                                           }})
                    .setNegativeButton(R.string.ti_dialog_button_cancel_txt, null);
                AlertDialog optionsDialog = builder.create();
                optionsDialog.show();
            } else {
                if (count > 0) {
                    Toast.makeText(getContext(),
                            getContext().getString(R.string.ti_preference_load_comp_message_txt, count, count),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /** @see jp.co.omronsoft.iwnnime.ml.MultiChoicePreference#onMultiChoiceItemsClick */
    @Override public void onMultiChoiceItemsClick(int which, boolean isChecked) {
        super.onMultiChoiceItemsClick(which, isChecked);

        boolean enabled = true;
        if (getCheckedCount() > MAX_ADD_WEBAPI_LIST) {
            if (isChecked) {
                Toast.makeText(getContext(),
                        R.string.ti_preference_webapi_list_max_error_txt,
                        Toast.LENGTH_SHORT).show();
            }
            enabled = false;
        }

        Button positiveButton = (Button)(getDialog().findViewById(android.R.id.button1));
        if (positiveButton != null) {
            positiveButton.setEnabled(enabled);
        }
    }
}
