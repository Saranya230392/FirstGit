package com.android.settings.lge;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HotSpotConfigParser {

    private static final String CUPSS_DIR = SystemProperties.get("ro.lge.capp_cupss.rootdir",
            "/cust");

    private static final String CUPSS_HOTSPOT_FILE = "/config/wifi_hotspot.xml";

    private static final String ELEMENT_PROFILES = "profiles";
    private static final String ELEMENT_WIFI_HOTSPOT = "wifi_hotspot";
    private static final String ATTR_SSID = "ssid";
    private static final String ATTR_KEYMGMT = "key_mgmt";
    private static final String ATTR_PASSWORD = "password";
    private static final String IMEI_TAG = "{IMEI}";

    public static final int OPEN_VALUE = 0;
    public static final int WPA_VALUE = 1;
    public static final int WPA2_VALUE = 2;

    private static final String LOG_TAG = "HotSpotParser";

    private static HotSpotConfigParser mInstance = null;
    private static WifiHotspotData mHotspotConfig = null;

    private class WifiHotspotData {
        public String ssid = null;
        public String key_mgmt = null;
        public String password = null;

        public WifiHotspotData(String ssid, String keyMgmt, String password) {
            this.ssid = ssid;
            this.key_mgmt = keyMgmt;
            this.password = password;
        }

        @Override
        public String toString() {
            return ssid + "|" + key_mgmt + "|" + password;
        }
    }

    // Singleton
    private HotSpotConfigParser() {
    }

    // Singleton
    public static HotSpotConfigParser getInstance() {
        if (mInstance == null) {
            mInstance = new HotSpotConfigParser();
            mInstance.parseHotSpotConfig();
        }
        return mInstance;
    }

    public boolean isHotspotAvailable() {
        return (mHotspotConfig != null);

    }

    public String getHotspotSSID(String imei) {
        if (isHotspotAvailable() && !TextUtils.isEmpty(mHotspotConfig.ssid)) {
            String resultSSID = mHotspotConfig.ssid;
            if (mHotspotConfig.ssid.contains(IMEI_TAG) && (imei != null && imei.length() >= 4)) {
                resultSSID = mHotspotConfig.ssid.replace(IMEI_TAG,
                        imei.substring(imei.length() - 4, imei.length()));
            }
            return resultSSID;
        }
        return null;
    }

    /**
     * 
     * @return
     */
    public int getHotspotKEYMGM() {
        int result = OPEN_VALUE;
        if (isHotspotAvailable() && !TextUtils.isEmpty(mHotspotConfig.key_mgmt)) {

            if ("WPA".equalsIgnoreCase(mHotspotConfig.key_mgmt)) {
                result = WPA_VALUE;
            }
            else if ("WPA2".equalsIgnoreCase(mHotspotConfig.key_mgmt)) {
                result = WPA2_VALUE;
            }
            else {
                result = OPEN_VALUE;
            }
        }
        return result;
    }

    public String getHotspotPassword(String imei) {
        if (isHotspotAvailable() && !TextUtils.isEmpty(mHotspotConfig.password)) {
            if (mHotspotConfig.password.contains(IMEI_TAG) && imei != null) {
                mHotspotConfig.password = imei;
            }
            return mHotspotConfig.password;
        }
        return null;
    }

    private void parseHotSpotConfig() {

        File hotSpotFile = getHotSpotFile();

        if ((hotSpotFile == null) || !(hotSpotFile.exists())) {
            return;
        }

        XmlPullParserFactory factory;
        XmlPullParser parser;
        FileReader reader = null;

        try {

            reader = new FileReader(hotSpotFile);

            factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(reader);

            XmlUtils.beginDocument(parser, ELEMENT_PROFILES);

            while (XmlPullParser.END_DOCUMENT != parser.getEventType()) {

                XmlUtils.nextElement(parser);
                WifiHotspotData item = loadHotspotData(parser);

                if (item != null && mHotspotConfig == null) {
                    mHotspotConfig = item;
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Could not find hotspot file");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while parsing hotspot file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(LOG_TAG, "IOException");
                }
            }
        }
    }

    private WifiHotspotData loadHotspotData(XmlPullParser parser) throws XmlPullParserException,
            IOException {

        if (ELEMENT_WIFI_HOTSPOT.equals(parser.getName())) {

            String ssid = parser.getAttributeValue(null, ATTR_SSID);
            String keymgmt = parser.getAttributeValue(null, ATTR_KEYMGMT);
            String password = parser.getAttributeValue(null, ATTR_PASSWORD);

            return new WifiHotspotData(ssid, keymgmt, password);
        }

        return null;
    }

    private File getHotSpotFile() {
        // try to get regular CUPSS file
        File cupssFile = new File(CUPSS_DIR + CUPSS_HOTSPOT_FILE);
        if (cupssFile.exists()) {
            return cupssFile;
        }

        // no configuration should be set
        return null;
    }
}
