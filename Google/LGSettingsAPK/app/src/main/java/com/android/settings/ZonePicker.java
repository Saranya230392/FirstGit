/*
 * Copyright (C) 2006 The Android Open Source Project
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
import android.app.AlarmManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

//OPEN_CN_Settings chaoying.hou@lge.com add for timezone Chinese a~z sort[Start]
import com.android.settings.HanziToPinyin;
import com.android.settings.HanziToPinyin.Token;
//OPEN_CN_Settings chaoying.hou@lge.com add for timezone Chinese a~z sort[end]

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.Phone;
import android.os.UserHandle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;

import com.android.internal.telephony.TelephonyIntents;
import com.lge.constants.LGIntent;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.android.internal.telephony.TelephonyProperties;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import libcore.icu.ICU;
import libcore.icu.TimeZoneNames;

/**
 * The class displaying a list of time zones that match a filter string
 * such as "Africa", "Europe", etc. Choosing an item from the list will set
 * the time zone. Pressing Back without choosing from the list will not
 * result in a change in the time zone setting.
 */
public class ZonePicker extends ListFragment {
    private static final String TAG = "ZonePicker";

    public static interface ZoneSelectionListener {
        // You can add any argument if you really need it...
        public void onZoneSelected(TimeZone tz);
    }

    private static final String KEY_ID = "id"; // value: String
    private static final String KEY_DISPLAYNAME = "name"; // value: String
    private static final String KEY_GMT = "gmt"; // value: String
    private static final String KEY_OFFSET = "offset"; // value: int (Integer)
    private static final String XMLTAG_TIMEZONE = "timezone";

    private static final int MENU_TIMEZONE = Menu.FIRST + 1;
    private static final int MENU_ALPHABETICAL = Menu.FIRST;

    private boolean mSortedByTimezone;

    private SimpleAdapter mTimezoneSortedAdapter;
    private SimpleAdapter mAlphabeticalAdapter;

    private ZoneSelectionListener mListener;

    public static boolean sCountryCodeReceived;
    private static boolean sFilterByCountry;

    private static String sCountry;
    private static List<String> sCountryZones = new ArrayList<String>();

    /**
    * Defult time zone list
    *
    **/
    /*
    private static final String ES_DEFAULT_TIMEZONE_ID = "Europe/Madrid";
    private static final String PT_DEFAULT_TIMEZONE_ID = "Europe/Lisbon";
    private static final String RU_DEFAULT_TIMEZONE_ID = "Europe/Samara"; // Europe/Moscow
    private static final String GL_DEFAULT_TIMEZONE_ID = "America/Godthab";
    private static final String CA_DEFAULT_TIMEZONE_ID = "America/Toronto";
    private static final String US_DEFAULT_TIMEZONE_ID = "America/New_York";
    private static final String MX_DEFAULT_TIMEZONE_ID = "America/Mexico_City";
    private static final String KZ_DEFAULT_TIMEZONE_ID = "Asia/Almaty";
    private static final String MN_DEFAULT_TIMEZONE_ID = "Asia/Choibalsan"; // Asia/Ulaanbaatar
    private static final String CL_DEFAULT_TIMEZONE_ID = "America/Santiago";
    private static final String AU_DEFAULT_TIMEZONE_ID = "Australia/Sydney";
    private static final String ID_DEFAULT_TIMEZONE_ID = "Asia/Jakarta";
    private static final String NZ_DEFAULT_TIMEZONE_ID = "Pacific/Auckland";
    private static final String KI_DEFAULT_TIMEZONE_ID = "Pacific/Tarawa";
    private static final String PF_DEFAULT_TIMEZONE_ID = "Pacific/Tahiti";
    private static final String FM_DEFAULT_TIMEZONE_ID = "Pacific/Ponape"; // Pacific/Pohnpei
    private static final String BR_DEFAULT_TIMEZONE_ID = "America/Sao_Paulo";
    private static final String EC_DEFAULT_TIMEZONE_ID = "America/Guayaquil";
    */
    private boolean mDefaultTimezone = false;
    private NotificationManager mNotificationManager;
    private static String sTarget;
    private static String sNotiTarget;
    Toast mNitzToast = null;
    private static Activity sActivity;

    public static final int NOTIFICATION_ID = R.drawable.shortcut_alarm;
    private static final String TARGET_DB = "target_db";
    private SharedPreferences mPrefs;

    public static final String[] DEFAULT_TIMEZONE_ID = {
            "ES_DEFAULT_TIMEZONE_ID", "PT_DEFAULT_TIMEZONE_ID", "RU_DEFAULT_TIMEZONE_ID",
            "GL_DEFAULT_TIMEZONE_ID", "CA_DEFAULT_TIMEZONE_ID", "US_DEFAULT_TIMEZONE_ID",
            "MX_DEFAULT_TIMEZONE_ID", "KZ_DEFAULT_TIMEZONE_ID", "MN_DEFAULT_TIMEZONE_ID",
            "CL_DEFAULT_TIMEZONE_ID", "AU_DEFAULT_TIMEZONE_ID", "ID_DEFAULT_TIMEZONE_ID",
            "NZ_DEFAULT_TIMEZONE_ID", "KI_DEFAULT_TIMEZONE_ID", "PF_DEFAULT_TIMEZONE_ID",
            "FM_DEFAULT_TIMEZONE_ID", "BR_DEFAULT_TIMEZONE_ID", "EC_DEFAULT_TIMEZONE_ID",
            "UA_DEFAULT_TIMEZONE_ID"
    };

    public static final String[] DEFAULT_TIMEZONE_VALUE_ID = {
            "Europe/Madrid", "Europe/Lisbon", "Europe/Moscow",
            "America/Godthab", "America/Toronto", "America/New_York",
            "America/Mexico_City", "Asia/Almaty", "Asia/Choibalsan",
            "America/Santiago", "Australia/Sydney", "Asia/Jakarta",
            "Pacific/Auckland", "Pacific/Tarawa", "Pacific/Tahiti",
            "Pacific/Ponape", "America/Sao_Paulo", "America/Guayaquil",
            "Europe/Kiev"
    };

    private BroadcastReceiver mNitzreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            SLog.d(TAG, "action :" + action);
            if (action.equals("com.lge.intent.action.NITZ_RECEIVED")) {
                if (mNitzToast != null) {
                    mNitzToast.cancel();
                }
                mNitzToast = Toast.makeText(getActivity(), R.string.sp_default_timezone_nitz_toast,
                        Toast.LENGTH_LONG);
                mNitzToast.show();
                mDefaultTimezone = true;
                if (!TextUtils.isEmpty(sNotiTarget)) {
                    Editor ed = mPrefs.edit();
                    ed.putString("Country", "");
                    ed.commit();
                }
                getActivity().onBackPressed();
            }
        }
    };

    /**
     * Constructs an adapter with TimeZone list. Sorted by TimeZone in default.
     *
     * @param sortedByName use Name for sorting the list.
     */
    public static SimpleAdapter constructTimezoneAdapter(Context context,
            boolean sortedByName) {
        /*
        return constructTimezoneAdapter(context, sortedByName,
                R.layout.date_time_setup_custom_list_item_2);
        */

        return constructTimezoneAdapter(context, sortedByName,
                android.R.layout.simple_list_item_2);

    }

    /**
     * Constructs an adapter with TimeZone list. Sorted by TimeZone in default.
     *
     * @param sortedByName use Name for sorting the list.
     */
    public static SimpleAdapter constructTimezoneAdapter(Context context,
            boolean sortedByName, int layoutId) {
        final String[] from = new String[] { KEY_DISPLAYNAME, KEY_GMT };
        final int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

        final String sortKey = (sortedByName ? KEY_DISPLAYNAME : KEY_OFFSET);
        final MyComparator comparator = new MyComparator(sortKey);
        ZoneGetter zoneGetter = new ZoneGetter();
        final List<HashMap<String, Object>> sortedList = zoneGetter.getZones(context);
        Collections.sort(sortedList, comparator);
        final SimpleAdapter adapter = new SimpleAdapter(context,
                sortedList,
                layoutId,
                from,
                to);

        return adapter;
    }

    /**
     * Searches {@link TimeZone} from the given {@link SimpleAdapter} object, and returns
     * the index for the TimeZone.
     *
     * @param adapter SimpleAdapter constructed by
     * {@link #constructTimezoneAdapter(Context, boolean)}.
     * @param tz TimeZone to be searched.
     * @return Index for the given TimeZone. -1 when there's no corresponding list item.
     * returned.
     */
    public static int getTimeZoneIndex(SimpleAdapter adapter, TimeZone tz) {
        final String defaultId = tz.getID();
        final int listSize = adapter.getCount();
        for (int i = 0; i < listSize; i++) {
            // Using HashMap<String, Object> induces unnecessary warning.
            final HashMap<?, ?> map = (HashMap<?, ?>)adapter.getItem(i);
            final String id = (String)map.get(KEY_ID);
            if (defaultId.equals(id)) {
                // If current timezone is in this list, move focus to it
                return i;
            }
        }
        return -1;
    }

    /**
     * @param item one of items in adapters. The adapter should be constructed by
     * {@link #constructTimezoneAdapter(Context, boolean)}.
     * @return TimeZone object corresponding to the item.
     */
    public static TimeZone obtainTimeZoneFromItem(Object item) {
        return TimeZone.getTimeZone((String)((Map<?, ?>)item).get(KEY_ID));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SLog.d(TAG, "onActivityCreated()");

        sActivity = getActivity();
        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
        /*
        activity.setTitle(R.string.date_time_set_timezone);
        activity.getActionBar().setIcon(R.drawable.shortcut_alarm);
        */

        sCountryCodeReceived = false;
        sFilterByCountry = false;

        if (sActivity.getIntent().getExtras() == null) {
            SLog.i(TAG, "intent " + sActivity.getIntent());
            sTarget = null;
        } else {
            sTarget = sActivity.getIntent().getExtras().getString("country");
        }
        mPrefs = sActivity.getSharedPreferences(TARGET_DB, sActivity.MODE_PRIVATE);
        SLog.d(TAG, "autoTimeZoneEnabled :" + autoTimeZoneEnabled);
        if (autoTimeZoneEnabled) {
            if (!TextUtils.isEmpty(sTarget)) {
                Editor ed = mPrefs.edit();
                ed.putString("Country", sTarget);
                ed.commit();
            }
        } else {
            Editor ed = mPrefs.edit();
            ed.putString("Country", "");
            ed.commit();
        }

        sNotiTarget = mPrefs.getString("Country", "");
        SLog.d(TAG, "sTarget : " + sTarget + "\n" + "sNotiTarget : " + sNotiTarget);

        if (!TextUtils.isEmpty(sTarget) || !TextUtils.isEmpty(sNotiTarget)) {
            sCountryCodeReceived = true;
            if (TextUtils.isEmpty(sTarget)) {
                sTarget = sNotiTarget;
            }
            sCountry = sTarget;
        }

        mTimezoneSortedAdapter = constructTimezoneAdapter(sActivity, false);
        mAlphabeticalAdapter = constructTimezoneAdapter(sActivity, true);

        // Sets the adapter
        setSorting(true);
        setHasOptionsMenu(true);

        if (!TextUtils.isEmpty(sTarget)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.lge.intent.action.NITZ_RECEIVED");
            getActivity().registerReceiver(mNitzreceiver, filter);
        }
    }

    private boolean getAutoState(String name) {
        try {
            return Settings.Global.getInt(getActivity().getContentResolver(), name) > 0;
        } catch (SettingNotFoundException snfe) {
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView list = (ListView)view.findViewById(android.R.id.list);
        Utils.forcePrepareCustomPreferencesList(container, view, list, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_ALPHABETICAL, 0, R.string.zone_list_menu_sort_alphabetically)
                .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
        menu.add(0, MENU_TIMEZONE, 0, R.string.zone_list_menu_sort_by_timezone)
                .setIcon(R.drawable.ic_menu_3d_globe);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mSortedByTimezone) {
            menu.findItem(MENU_TIMEZONE).setVisible(false);
            menu.findItem(MENU_ALPHABETICAL).setVisible(true);
        } else {
            menu.findItem(MENU_TIMEZONE).setVisible(true);
            menu.findItem(MENU_ALPHABETICAL).setVisible(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SLog.d(TAG, "onResume()");
        //to do list        
    }

    @Override
    public void onPause() {
        super.onPause();
        SLog.d(TAG, "onPause()");

        final Activity mActivity = getActivity();
        if (!TextUtils.isEmpty(sTarget) && !mDefaultTimezone) {
            int mZoneValueId = 0;
            String mZoneId = null;
            int mTimezoneId;
            int mMAX_DEFAUTLTIMEZONE = 19;
            for (mTimezoneId = 0; mTimezoneId < mMAX_DEFAUTLTIMEZONE; mTimezoneId++) {
                Log.e(TAG, "DEFAULT_TIMEZONE_ID[mTimezoneId] = " + DEFAULT_TIMEZONE_ID[mTimezoneId]
                        + ":    :" + "target = " + sTarget);
                if (DEFAULT_TIMEZONE_ID[mTimezoneId].startsWith(sTarget)) {
                    Log.d(TAG, "target = " + sTarget
                            + " mTimezoneId : " + Integer.toString(mTimezoneId));
                    mZoneValueId = mTimezoneId;
                    break;
                }
            }

            mZoneId = DEFAULT_TIMEZONE_VALUE_ID[mZoneValueId];

            Log.e(TAG, "mZoneId = " + mZoneId);

            String text = "";
            String title = mActivity.getString(R.string.sp_default_timezone_notification_title);

            setAndBroadcastNetworkSetTimeZone(mActivity, mZoneId);

            Intent intent = new Intent();
            intent.setAction("com.lge.settings.TIMEZONE_DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NO_HISTORY);
            PendingIntent contentIntent = PendingIntent.getActivity(mActivity, 0, intent, 0);

            mNotificationManager = (NotificationManager)mActivity
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Notification mNotification = new Notification();

            mNotification.icon = R.drawable.detail;
            mNotification.flags = mNotification.flags | Notification.FLAG_AUTO_CANCEL;
            mNotification.tickerText = mActivity
                    .getString(R.string.sp_default_timezone_notification_title);
            text = mActivity.getString(R.string.sp_default_timezone_notification_summary);
            mNotification.setLatestEventInfo(mActivity, title, text, contentIntent);

            if (mNotificationManager == null) {
                mNotificationManager =
                        (NotificationManager)mActivity
                                .getSystemService(Context.NOTIFICATION_SERVICE);
            }
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }

        //to do list
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        if (!TextUtils.isEmpty(sTarget)) {
            getActivity().unregisterReceiver(mNitzreceiver);
        }

        if (!TextUtils.isEmpty(sTarget) && mDefaultTimezone) {
            if (mNotificationManager == null) {
                mNotificationManager =
                        (NotificationManager)getActivity().getSystemService(
                                Context.NOTIFICATION_SERVICE);
            }
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    /**
    * Set the timezone and send out a sticky broadcast so the system can
    * determine if the timezone was set by the carrier.
    *
    * @param zoneId timezone set by carrier
    */
    public static void setAndBroadcastNetworkSetTimeZone(Context context, String zoneId) {
        Log.d(TAG, "setAndBroadcastNetworkSetTimeZone: setTimeZone=" + zoneId);
        //        if (DBG) log("setAndBroadcastNetworkSetTimeZone: setTimeZone=" + zoneId);
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(zoneId);
        Intent intent = new Intent(TelephonyIntents.ACTION_NETWORK_SET_TIMEZONE);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("time-zone", zoneId);
        context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        Log.d(TAG,
                "setAndBroadcastNetworkSetTimeZone: call alarm.setTimeZone and broadcast zoneId=" +
                        zoneId);
        /*
        if (DBG) {
           log("setAndBroadcastNetworkSetTimeZone: call alarm.setTimeZone and broadcast zoneId=" +
               zoneId);
        }
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case MENU_TIMEZONE:
            setSorting(true);
            return true;

        case MENU_ALPHABETICAL:
            setSorting(false);
            return true;

        default:
            return false;
        }
    }

    public void setZoneSelectionListener(ZoneSelectionListener listener) {
        mListener = listener;
    }

    private void setSorting(boolean sortByTimezone) {
        final SimpleAdapter adapter =
                sortByTimezone ? mTimezoneSortedAdapter : mAlphabeticalAdapter;
        setListAdapter(adapter);
        mSortedByTimezone = sortByTimezone;
        final int defaultIndex = getTimeZoneIndex(adapter, TimeZone.getDefault());
        if (defaultIndex >= 0) {
            setSelection(defaultIndex);
        }
    }


    static class ZoneGetter {
        private final List<HashMap<String, Object>> mZones =
                new ArrayList<HashMap<String, Object>>();
        private static List<HashMap<String, Object>> sCountryZonesData;
        private final HashSet<String> mLocalZones = new HashSet<String>();
        private final Date mNow = Calendar.getInstance().getTime();
        private final SimpleDateFormat mZoneNameFormatter = new SimpleDateFormat("zzzz");
        final long date = Calendar.getInstance().getTimeInMillis();

        private List<HashMap<String, Object>> getZones(Context context) {
            sCountryZonesData = new ArrayList<HashMap<String, Object>>();
            if (sCountryCodeReceived) {
                getZoneIdsByCountry(context, sCountry);
            }

            for (String olsonId : TimeZoneNames.forLocale(Locale.getDefault())) {
                mLocalZones.add(olsonId);
            }

            try {
                XmlResourceParser xrp = getXmlResourceParser(context);
                while (xrp.next() != XmlResourceParser.START_TAG) {
                    continue;
                }
                xrp.next();
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    Log.d(TAG, "xrp.getEventType() : " + xrp.getEventType());
                    Log.d(TAG, "XmlResourceParser.END_TAG : " + XmlResourceParser.END_TAG);
                    while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                        if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                            if (sCountryCodeReceived) {
                                return sCountryZonesData;
                            } else {
                                return mZones;
                            }
                        }
                        xrp.next();
                    }
                    if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                        String olsonId = xrp.getAttributeValue(0);
                        if (sFilterByCountry && sCountryZones != null && !sCountryZones.isEmpty()) {
                            if (sCountryZones.contains(olsonId)) {
                                Log.d(TAG, "starmotor IN");
                                addTimeZone(olsonId);
                            }
                        } else {
                            Log.d(TAG, "starmotor IN 2");
                            addTimeZone(olsonId);
                        }
                    }
                    while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                        xrp.next();
                    }
                    xrp.next();
                }
                xrp.close();
            } catch (XmlPullParserException xppe) {
                Log.e(TAG, "Ill-formatted timezones.xml file");
            } catch (java.io.IOException ioe) {
                Log.e(TAG, "Unable to read timezones.xml file");
            }
            if (sCountryCodeReceived) {
                return sCountryZonesData;
            } else {
                return mZones;
            } 
        }

        private XmlResourceParser getXmlResourceParser(Context context) {
            XmlResourceParser xrp;
            Log.d(TAG, "sCountryCodeReceived : " + sCountryCodeReceived + ", sCountry : " + sCountry);
            if (sCountryCodeReceived) {
                xrp = context.getResources().getXml(R.xml.timezones_multi);
            } else {
                xrp = context.getResources().getXml(R.xml.timezones);
            }

            return xrp;
        }

        private void addTimeZone(String olsonId) {
            // We always need the "GMT-07:00" string.
            final TimeZone tz = TimeZone.getTimeZone(olsonId);

            // For the display name, we treat time zones within the country differently
            // from other countries' time zones. So in en_US you'd get "Pacific Daylight Time"
            // but in de_DE you'd get "Los Angeles" for the same time zone.
            String displayName;
            if (SystemProperties.get("ro.build.target_region").equals("SCA")) {
                // we use the exemplar location.
                final String localeName = Locale.getDefault().toString();
                displayName = TimeZoneNames.getExemplarLocation(localeName, olsonId);
            } else {
            if (mLocalZones.contains(olsonId)) {
                // Within a country, we just use the local name for the time zone.
                mZoneNameFormatter.setTimeZone(tz);
                displayName = mZoneNameFormatter.format(mNow);
            } else {
                // For other countries' time zones, we use the exemplar location.
                final String localeName = Locale.getDefault().toString();
                displayName = TimeZoneNames.getExemplarLocation(localeName, olsonId);
            }
            }

            final HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(KEY_ID, olsonId);
            if (olsonId.equals("Europe/Madrid")) {
                displayName = sActivity.getString(R.string.madrid_barcelona);
            } else if (olsonId.equals("Asia/Shanghai")) {
                displayName = sActivity.getString(R.string.beijing_time);
            } else if (olsonId.equals("Europe/Samara")) {
                final String localeNameSa = Locale.getDefault().toString();
                displayName = TimeZoneNames.getExemplarLocation(localeNameSa, olsonId);
            } else if (olsonId.equals("Europe/Simferopol")) {
                final String localeNameSi = Locale.getDefault().toString();
                displayName = TimeZoneNames.getExemplarLocation(localeNameSi, olsonId);
            } else if (olsonId.equals("Europe/Kaliningrad")) {
                final String localeNameKa = Locale.getDefault().toString();
                displayName = TimeZoneNames.getExemplarLocation(localeNameKa, olsonId);
            }
            map.put(KEY_DISPLAYNAME, displayName);
            map.put(KEY_GMT, DateTimeSettings.getTimeZoneText(tz, false));
            map.put(KEY_OFFSET, tz.getOffset(mNow.getTime()));
            if (sCountryCodeReceived) {
                sCountryZonesData.add(map);
                Log.d(TAG, "sCountryZonesData map : " + map);
            } else {
                mZones.add(map);
                Log.d(TAG, "map : " + map);
            }
        }
    }

    private static boolean mEqualsHandlesNulls(String a, String b) {
        return (TextUtils.isEmpty(a) ? TextUtils.isEmpty(b) : a.equals(b));
    }

    private static void getZoneIdsByCountry(Context context, String target) { // target = country
        boolean found = false;

        sCountryZones.clear();
        if (TextUtils.isEmpty(target)) {
            return;
        }

        String targetCountry = target.toUpperCase();

        try {
            XmlResourceParser xrp = context.getResources().getXml(com.android.internal.R.xml.time_zones_by_country);
            while (xrp.next() != XmlResourceParser.START_TAG) {
                continue;
            }
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String countryCode = xrp.getAttributeValue(0).toUpperCase();
                    Log.d(TAG, "countryCode : " + countryCode);
                    String id = xrp.nextText();
                    if (mEqualsHandlesNulls(targetCountry, countryCode)) {
                        found = true;
                        Log.d(TAG, "starmotor id : " + id);
                        sCountryZones.add(id);
                    } else if (found) {
                        xrp.close();
                        if (sCountryZones.size() > 0) {
                            sFilterByCountry = true;
                        }
                        return;
                    }
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Ill-formatted timezones.xml file");
        } catch (java.io.IOException ioe) {
            Log.e(TAG, "Unable to read timezones.xml file");
        }

        if (sCountryZones.size() > 0) {
            sFilterByCountry = true;
        }
        return;
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {

        // Ignore extra clicks

        if (!isResumed()) {
            return;
        }

        final Map<?, ?> map = (Map<?, ?>)listView.getItemAtPosition(position);
        final String tzId = (String)map.get(KEY_ID);

        // Update the system timezone value
        final Activity activity = getActivity();
        final AlarmManager alarm = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(tzId);
        final TimeZone tz = TimeZone.getTimeZone(tzId);
        Log.d(TAG, "mListener : " + mListener);
        if (mListener != null) {
            mListener.onZoneSelected(tz);
        } else {
            mDefaultTimezone = true;
            if (!TextUtils.isEmpty(sNotiTarget)) {
                Editor ed = mPrefs.edit();
                ed.putString("Country", "");
                ed.commit();
            }
            getActivity().onBackPressed();
        }
    }

    private static class MyComparator implements Comparator<HashMap<?, ?>> {
        private String mSortingKey;

        public MyComparator(String sortingKey) {
            mSortingKey = sortingKey;
        }

        public void setSortingKey(String sortingKey) {
            mSortingKey = sortingKey;
        }

        public int compare(HashMap<?, ?> map1, HashMap<?, ?> map2) {
            //OPEN_CN chaoying.hou@lge.com add for timezone Chinese a~z sort[start]
            Object value1;
            Object value2;
            if (mSortingKey.equals(KEY_DISPLAYNAME))
            {
                value1 = this.getPinyin(map1.get(mSortingKey).toString());
                value2 = this.getPinyin(map2.get(mSortingKey).toString());
            }
            else
            //OPEN_CN chaoying.hou@lge.com add for timezone Chinese a~z sort[end]
            {
                value1 = map1.get(mSortingKey);
                value2 = map2.get(mSortingKey);
            }

            /*
             * This should never happen, but just in-case, put non-comparable
             * items at the end.
             */
            if (!isComparable(value1)) {
                return isComparable(value2) ? 1 : 0;
            } else if (!isComparable(value2)) {
                return -1;
            }

            return ((Comparable)value1).compareTo(value2);
        }

        private boolean isComparable(Object value) {
            return (value != null) && (value instanceof Comparable);
        }

        //OPEN_CN_Settings chaoying.hou@lge.com add for timezone Chinese a~z sort[start]
        private String getPinyin(String displayName) {
            int check = 0;
            String[] words = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
                    "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
                    , "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
                    "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
            for (String aStart : words)
            {
                if (displayName.startsWith(aStart)) {
                    check = 1;
                }
            }
            if (check == 1) {
                String ss = displayName;
                if (ss.startsWith("the ") || ss.startsWith("a ") || ss.startsWith("an "))
                {
                    if (ss.startsWith("the ")) {
                        ss = ss.substring(4);
                        for (String aStart : words)
                        {
                            if (!(ss.startsWith(aStart))) {
                                check = 0;
                            }
                            else {
                                check = 1;
                                break;
                            }
                        }

                    }
                    else if (ss.startsWith("a ")) {
                        ss = ss.substring(2);
                        for (String aStart : words)
                        {
                            if (!(ss.startsWith(aStart))) {
                                check = 0;
                            }
                            else {
                                check = 1;
                                break;
                            }
                        }
                    }
                    else if (ss.startsWith("an ")) {
                        ss = ss.substring(3);
                        for (String aStart : words)
                        {
                            if (!(ss.startsWith(aStart))) {
                                check = 0;
                            }
                            else {
                                check = 1;
                                break;
                            }
                        }

                    }
                    else {
                        return displayName;
                    }
                }
                else {
                    return displayName;
                }
            }
            if (check == 1) {
                return displayName;
            }
            else {
                ArrayList<Token> tokens = HanziToPinyin.getInstance().get(displayName);
                if (tokens != null && tokens.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Token token : tokens) {
                        if (Token.PINYIN == token.type) {
                            sb.append(token.target);
                        }
                        else
                        {
                            sb.append(token.source);
                        }
                    }
                    return sb.toString();
                }
                return displayName;
            }
        }
        //OPEN_CN_Settings chaoying.hou@lge.com add for timezone Chinese a~z sort[end]
    }

}
