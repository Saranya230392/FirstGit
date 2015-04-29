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


/**
 * The class displaying a list of time zones that match a filter string
 * such as "Africa", "Europe", etc. Choosing an item from the list will set
 * the time zone. Pressing Back without choosing from the list will not
 * result in a change in the time zone setting.
 */
public class ZonePickerCT extends ListFragment {
    private static final String TAG = "ZonePickerCT";

    public static interface ZoneSelectionListener {
        // You can add any argument if you really need it...
        public void onZoneSelected(TimeZone tz);
    }

    private static final String KEY_ID = "id";  // value: String
    private static final String KEY_DISPLAYNAME = "name";  // value: String
    private static final String KEY_GMT = "gmt";  // value: String
    private static final String KEY_OFFSET = "offset";  // value: int (Integer)
    private static final String XMLTAG_TIMEZONE = "timezone";

    private static final int HOURS_1 = 60 * 60000;

    private static final int MENU_TIMEZONE = Menu.FIRST+1;
    private static final int MENU_ALPHABETICAL = Menu.FIRST;

    private boolean mSortedByTimezone;

    private SimpleAdapter mTimezoneSortedAdapter;
    private SimpleAdapter mAlphabeticalAdapter;

    private ZoneSelectionListener mListener;

    private static boolean sCountryCodeReceived;
    private static boolean sFilterByCountry;
    private static String sCountry;
    private static List<String> sCountryZones = new ArrayList<String>();
    private static List<HashMap<String, Object>> sCountryZonesData;

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
        final String[] from = new String[] {KEY_DISPLAYNAME, KEY_GMT};
        final int[] to = new int[] {android.R.id.text1, android.R.id.text2};

        final String sortKey = (sortedByName ? KEY_DISPLAYNAME : KEY_OFFSET);
        final MyComparator comparator = new MyComparator(sortKey);
        final List<HashMap<String, Object>> sortedList = getZones(context);
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
            final HashMap<?,?> map = (HashMap<?,?>)adapter.getItem(i);
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
    public void onActivityCreated(Bundle savedInstanseState) {
        super.onActivityCreated(savedInstanseState);
        SLog.d(TAG, "onActivityCreated()");

        final Activity activity = getActivity();
        /*
    activity.setTitle(R.string.date_time_set_timezone);
    activity.getActionBar().setIcon(R.drawable.shortcut_alarm);
    */
        
        sCountryCodeReceived = false;
        sFilterByCountry = false;
        String sTarget = SystemProperties.get("gsm.operator.iso-country", "").toUpperCase();
        Log.e(TAG, "sTarget = " + sTarget);

        if (!TextUtils.isEmpty(sTarget)) {
            sCountryCodeReceived = true;
            sCountry = sTarget;
        }

        mTimezoneSortedAdapter = constructTimezoneAdapter(activity, false);
        mAlphabeticalAdapter = constructTimezoneAdapter(activity, true);

        // Sets the adapter
        setSorting(true);
        setHasOptionsMenu(true);
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

    private static List<HashMap<String, Object>> getZones(Context context) {
        final List<HashMap<String, Object>> myData = new ArrayList<HashMap<String, Object>>();
        sCountryZonesData  = new ArrayList<HashMap<String, Object>>();
        final long date = Calendar.getInstance().getTimeInMillis();

        if (sCountryCodeReceived) {
            getZoneIdsByCountry(context, sCountry);
        }

        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        if (sCountryCodeReceived) {
                            return sCountryZonesData;
                        } else {
                            return myData;
                        }
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    if (sFilterByCountry && sCountryZones != null && !sCountryZones.isEmpty()) {
                        if (sCountryZones.contains(id)) {
                            addItem(sCountryZonesData, id, displayName, date);
                        }
                    } else {
                        addItem(myData, id, displayName, date);
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
            return myData;
        }
    }

    private static boolean equalsHandlesNulls (String a, String b) {
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
            XmlResourceParser xrp = context.getResources().getXml(R.xml.country_timezones);
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
                    String countryCode = xrp.getAttributeValue(0);
                    String id = xrp.nextText();
                    if (equalsHandlesNulls(targetCountry, countryCode)) {
                        found = true;
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

    private static void addItem(
            List<HashMap<String, Object>> myData, String id, String displayName, long date) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ID, id);
        map.put(KEY_DISPLAYNAME, displayName);
        final TimeZone tz = TimeZone.getTimeZone(id);
        final int offset = tz.getOffset(date);
        final int p = Math.abs(offset);
        final StringBuilder name = new StringBuilder();
        name.append("GMT");

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / (HOURS_1));
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        map.put(KEY_GMT, name.toString());
        map.put(KEY_OFFSET, offset);

        myData.add(map);
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {

        // Ignore extra clicks

        if (!isResumed()) {
            return;
        }

        final Map<?, ?> map = (Map<?, ?>)listView.getItemAtPosition(position);
        final String tzId = (String) map.get(KEY_ID);

        // Update the system timezone value
        final Activity activity = getActivity();
        final AlarmManager alarm = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(tzId);
        final TimeZone tz = TimeZone.getTimeZone(tzId);
        Log.d(TAG , "mListener : " + mListener);
        if (mListener != null) {
            mListener.onZoneSelected(tz);
        } else {
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
            if(mSortingKey.equals(KEY_DISPLAYNAME))
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

            return ((Comparable) value1).compareTo(value2);
        }

        private boolean isComparable(Object value) {
            return (value != null) && (value instanceof Comparable);
        }

       //OPEN_CN_Settings chaoying.hou@lge.com add for timezone Chinese a~z sort[start]
        private String getPinyin(String displayName) {
             int check = 0;
             String[] words = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
                     , "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
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
