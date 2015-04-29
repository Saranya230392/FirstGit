/*==========================================================================================================
CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com
WpsNfcDialog.java : This file is for below functions.
1. Make Dialog for WPS_NFC
2. Parsing WPS_NFC payload data.
3. Get WPS_NFC Token from wpa_supplicant or hostapd
4. Write or Push NFC Token to other device.
===========================================================================================================*/
/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.wifi;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;
import android.os.UserHandle;

import com.android.settings.lgesetting.Config.Config;
import com.android.settings.R;

//WPS_NFC
//import android.nfc.NdefMessage;
//import android.nfc.NdefRecord;
//import android.nfc.NfcAdapter;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import com.lge.nfcaddon.NfcAdapterAddon;
import java.io.IOException;

import android.content.Intent;
import android.content.IntentFilter;

import android.app.PendingIntent;
//WPS_NFC

public class WpsNfcDialog extends Activity {
    private static final String TAG = "WpsNfcDialog";
    public static boolean DBG = !(SystemProperties.get("ro.build.type").equals("user"));

    private boolean mWasNfcDisabled = false;
    
    //WPS_NFC
    private NfcAdapter wpsnfcAdapter;
    private static Tag mTag = null;
    private static NdefMessage message = null;
    private String mWpsNfcConfigurationToken;

    public static final String NFC_TOKEN_MIME_TYPE = "application/vnd.wfa.wsc";

    // WPS_NFC
    public static final String WPS_NFC_PWD_TOKEN_REQUEST = "com.lge.wifi.WPS_NFC_PWD_TOKEN_REQUEST";
    public static final String WPS_NFC_PWD_TOKEN_RECEIVED = "com.lge.wifi.WPS_NFC_PWD_TOKEN_RECEIVED";
    public static final String EXTRA_WPS_NFC_PWD_NDEF_TOKEN = "extra_wps_nfc_pwd_ndef_token";

    public static final String WPS_NFC_HANDOVER_REQ_REQUEST = "com.lge.wifi.WPS_NFC_HADOVER_REQ_REQUEST";
    public static final String WPS_NFC_HANDOVER_REQ_RECEIVED = "com.lge.wifi.WPS_NFC_HANDOVER_REQ_RECEIVED";
    public static final String EXTRA_WPS_NFC_HANDOVER_REQ_TOKEN = "extra_wps_nfc_handover";

    public static final int PWD_TOKEN_DIALOG_TYPE = 11; //WpsInfo.NFC_PWD
    public static final int HANDOVER_DIALOG_TYPE = 12; //WpsInfo.NFC_HANDOVER
    public static final int CONF_TOKEN_DIALOG_TYPE = 13; //WpsInfo.NFC_CONFIG
    public static final int CONF_TOKEN_P2PSEND_DIALOG_TYPE = 14; //WpsInfo.NFC_CONFIG

    public static final String WPS_NFC_CONF_TOKEN_REQUEST =    "com.lge.wifi.WPS_NFC_CONF_TOKEN_REQUEST";
    public static final String WPS_NFC_CONF_TOKEN_SEND = "com.lge.wifi.WPS_NFC_CONF_TOKEN_SEND";
    public static final String WPS_NFC_CONF_TOKEN_GEN =    "com.lge.wifi.WPS_NFC_CONF_TOKEN_GEN";
    public static final String WPS_NFC_CONF_TOKEN_P2PSEND_GEN =    "com.lge.wifi.WPS_NFC_CONF_TOKEN_P2PSEND_GEN";

    public static final String WPS_NFC_RX_HANDOVER_SEL_FINISHED = "com.lge.wifi.WPS_NFC_RX_HANDOVER_SEL_FINISHED";

    private Intent wpsIntent = null;
    private final IntentFilter mFilter;    
    private final BroadcastReceiver mReceiver;

    private PendingIntent wpsnfcPendingIntent;
    private IntentFilter wpsnfcWriteTagFilters[];
    private AlertDialog dlg = null;
    
    private WpsNfcWriteDialog mWpsNfcWriteDialog;

    // WPS_NFC

    public WpsNfcDialog() {
        mFilter = new IntentFilter();
        mFilter.addAction(WPS_NFC_PWD_TOKEN_RECEIVED);
        mFilter.addAction(WPS_NFC_HANDOVER_REQ_RECEIVED);
        mFilter.addAction(WPS_NFC_RX_HANDOVER_SEL_FINISHED);
        mFilter.addAction(WPS_NFC_CONF_TOKEN_GEN);
        
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.registerReceiver(mReceiver, mFilter);
        wpsnfcAdapter = NfcAdapter.getDefaultAdapter(this);
//[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for NFC enable if disabled
        if ( wpsnfcAdapter != null && !wpsnfcAdapter.isEnabled() ) {
            wpsnfcAdapter.enable();
            mWasNfcDisabled = true; 
        }
//[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for NFC enable if disabled
        
        wpsnfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        wpsnfcWriteTagFilters = new IntentFilter[] { ndefDetected, tagDetected };


        String action = getIntent().getStringExtra("ACTION");

        Log.i(TAG, "WPS: onCreate get action : "+action);
        if (action != null) { // sunghee78.lee@lge.com 2012.07.04 WBT #379725, #379726 Null Pointer Dereference fixed
            if (action.equals(WPS_NFC_PWD_TOKEN_REQUEST)) {
                mWpsNfcWriteDialog = new WpsNfcWriteDialog(this , PWD_TOKEN_DIALOG_TYPE);
                if(mWpsNfcWriteDialog != null)
                    mWpsNfcWriteDialog.show();  
            } else if (action.equals(WPS_NFC_HANDOVER_REQ_REQUEST)) {
                mWpsNfcWriteDialog = new WpsNfcWriteDialog(this , HANDOVER_DIALOG_TYPE);
                if(mWpsNfcWriteDialog != null)
                    mWpsNfcWriteDialog.show();  

            } else if (action.equals(WPS_NFC_CONF_TOKEN_REQUEST)) {
                Log.d(TAG, "WPS: get action WPS_NFC_CONF_TOKEN_REQUEST");
                mWpsNfcWriteDialog = new WpsNfcWriteDialog(this , CONF_TOKEN_DIALOG_TYPE);
                if(mWpsNfcWriteDialog != null)
                    mWpsNfcWriteDialog.show();  

                handleEvent(this, new Intent(WPS_NFC_CONF_TOKEN_GEN) );
            } else if (action.equals(WPS_NFC_CONF_TOKEN_SEND)) {
                Log.d(TAG, "WPS: get action WPS_NFC_CONF_TOKEN_SEND");
                mWpsNfcWriteDialog = new WpsNfcWriteDialog(this , CONF_TOKEN_DIALOG_TYPE);
                if(mWpsNfcWriteDialog != null)
                    mWpsNfcWriteDialog.show();

                handleEvent(this, new Intent(WPS_NFC_CONF_TOKEN_P2PSEND_GEN) );
            }
        }

        if(mWpsNfcWriteDialog != null)
            mWpsNfcWriteDialog.setOnCancelListener(new OnCancelListener(){

                @Override
                public void onCancel(DialogInterface arg0) {
                    // TODO Auto-generated method stub
                    finish();
                }

            });

        if(mWpsNfcWriteDialog != null)
            mWpsNfcWriteDialog.setOnDismissListener(new OnDismissListener(){

                @Override
                public void onDismiss(DialogInterface arg0) {
                    // TODO Auto-generated method stub
                    finish();
                }

            });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (wpsnfcAdapter != null) {
            Log.d(TAG, "WPS: WpsNfcDialog onResume");
//            wpsnfcAdapter.enableForegroundDispatch(this, wpsnfcPendingIntent, wpsnfcWriteTagFilters, null);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        //finish();
        super.onPause();
        
        if (wpsnfcAdapter != null) {
            Log.d(TAG, "WPS: WpsNfcDialog onPause");
//            wpsnfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
//[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for NFC enable if disabled
        if ( mWasNfcDisabled ) {
            wpsnfcAdapter.disable();
        }
//[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for NFC enable if disabled
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void onNewIntent(Intent passedIntent) {
        Log.d(TAG, "WPS: onNewIntent() called.");
 
        if (passedIntent != null) {
            mTag = passedIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            handleEvent(this, passedIntent);
        }
    }

    private void handleEvent(Context context, Intent intent) {
         String action = intent.getAction();
         Log.d(TAG, "WPS: handleEvent action : " + action);
    
         // WPS_NFC
         if (WPS_NFC_PWD_TOKEN_RECEIVED.equals(action)) {
             byte[] payload;
             String pre_wps_nfc_pwd_token = intent.getStringExtra(EXTRA_WPS_NFC_PWD_NDEF_TOKEN);
             String wps_nfc_pwd_token = "";
    
             if (pre_wps_nfc_pwd_token != null) {
                wps_nfc_pwd_token = pre_wps_nfc_pwd_token.substring(52, pre_wps_nfc_pwd_token.length());
             } else {
                 Log.i(TAG, "WPS: There is no EXTRA_WPS_NFC_PWD_NDEF_TOKEN.");
                 Toast.makeText(WpsNfcDialog.this, "Tag read Fail!",
                    Toast.LENGTH_SHORT).show();
                 return;
             }

             if(DBG) Log.d(TAG, "WPS: before NDEF Header remove : " + pre_wps_nfc_pwd_token);
    
/*            payload = createStringToPayload(pre_wps_nfc_pwd_token);
             try {
                 if(payload != null)
                   message = new NdefMessage(payload);
                      
             }
             catch (FormatException fe) {
                 Log.d(TAG, "WPS: NDEF Format : Format Error!\n");
             }
*/
             payload = createStringToPayload(wps_nfc_pwd_token);
             NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "application/vnd.wfa.wsc".getBytes(), new byte[0], payload);
             message = new NdefMessage(new NdefRecord[] {record});
             
             if(DBG) Log.d(TAG, "WPS: message : " + message);
//             wpsnfcAdapter.enableForegroundDispatch(this, wpsnfcPendingIntent, wpsnfcWriteTagFilters, null);
//             WPSNfcPushModeOn(message); 

         }
         else if (WPS_NFC_HANDOVER_REQ_RECEIVED.equals(action)) {
             byte[] payload;
             String pre_wps_nfc_handover_token = intent.getStringExtra(EXTRA_WPS_NFC_HANDOVER_REQ_TOKEN);
             String wps_nfc_handover_token = "";

             context.removeStickyBroadcastAsUser(intent, UserHandle.ALL);

             if (pre_wps_nfc_handover_token != null) {
                 wps_nfc_handover_token = pre_wps_nfc_handover_token.substring(52, pre_wps_nfc_handover_token.length());
             } else {
                 Log.i(TAG, "WPS: There is no EXTRA_WPS_NFC_HANDOVER_REQ_TOKEN.");
                 Toast.makeText(WpsNfcDialog.this, "Fail to get data!",
                    Toast.LENGTH_SHORT).show();
                 return;
             }
             if(DBG) Log.d(TAG, "WPS: before NDEF Header remove : " + pre_wps_nfc_handover_token);
    
    
             payload = createStringToPayload(pre_wps_nfc_handover_token);
             NdefMessage HandoverReqData = null;
            try {
                if(payload != null)
                     HandoverReqData = new NdefMessage(payload);
            }
            catch (FormatException fe) {
                Log.d(TAG, "WPS: NDEF Format : Format Error!\n");
            }

             if(HandoverReqData != null) {
                if(DBG) Log.i(TAG, "WPS: HandoverReqData:" + HandoverReqData.toString());
                WPSNfcPushModeOn(HandoverReqData);
             }
         } else if (WPS_NFC_RX_HANDOVER_SEL_FINISHED.equals(action)) {
             if(mWpsNfcWriteDialog != null && mWpsNfcWriteDialog.isShowing()){
                             mWpsNfcWriteDialog.dismiss();
             }
             Toast.makeText(WpsNfcDialog.this, "Connection handover success!",
                 Toast.LENGTH_SHORT).show();
             finish();
         } else if (WPS_NFC_CONF_TOKEN_GEN.equals(action)) {
            Log.i(TAG, "WPS: WPS_NFC_CONF_TOKEN_GEN received");
                mWpsNfcConfigurationToken = com.lge.wifi.extension.LgWifiManager.getWifiSoftAPIface().getWpsNfcConfTokenFromAP(1, 0);
                if(mWpsNfcConfigurationToken != null) {
                    Log.i(TAG, "mWpsNfcConfigurationToken : " + mWpsNfcConfigurationToken);
//                    getConfigTokenMessage(mWpsNfcConfigurationToken);

                    wpsnfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
                        @Override
                        public void onTagDiscovered(Tag tag) {
                            handleWriteNfcEvent(tag);
                        }
                    }, NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V,
                    null);
                } else {
                     if(mWpsNfcWriteDialog != null && mWpsNfcWriteDialog.isShowing()){
                             Toast.makeText(WpsNfcDialog.this, "Generate Configuration Token Fail...\n Try again..",
                                Toast.LENGTH_SHORT).show();
                             mWpsNfcWriteDialog.dismiss();
                     }
                     finish();
                }
         } else if (WPS_NFC_CONF_TOKEN_P2PSEND_GEN.equals(action)) {
            Log.i(TAG, "WPS: WPS_NFC_CONF_TOKEN_P2PSEND_GEN received");
                String wps_nfc_configuration_token = com.lge.wifi.extension.LgWifiManager.getWifiSoftAPIface().getWpsNfcConfTokenFromAP(1, 1);
                if(wps_nfc_configuration_token != null) {
                    getConfigTokenMessage(wps_nfc_configuration_token);
                } else {
                     if(mWpsNfcWriteDialog != null && mWpsNfcWriteDialog.isShowing()){
                             Toast.makeText(WpsNfcDialog.this, "Generate Configuration Token Fail...\n Try again..",
                                Toast.LENGTH_SHORT).show();
                             mWpsNfcWriteDialog.dismiss();
                     }
                     finish();
                }
                if (wpsnfcAdapter != null) {
                    wpsnfcAdapter.setNdefPushMessage(message, this, NfcAdapter.FLAG_NDEF_PUSH_NO_CONFIRM);
                }
         } else {
             if (mTag == null) {
                 Log.i(TAG, "WPS: No Tag!!!WPSNfcPushModeOn");
                 WPSNfcPushModeOn(message); 
                 return;
             }
             Log.i(TAG, "WPS: WPSNfcTagWrite!!");
             WPSNfcTagWrite(message);
             
             if(mWpsNfcWriteDialog != null && mWpsNfcWriteDialog.isShowing()){
                 mWpsNfcWriteDialog.dismiss();
                 wpsnfcAdapter.disableReaderMode(this);
             }
             finish();
         }
    }

    private void handleWriteNfcEvent(Tag tag) {
        Ndef ndef = Ndef.get(tag);

        if (ndef != null) {
            if (ndef.isWritable()) {
                NdefRecord record = NdefRecord.createMime(
                        NFC_TOKEN_MIME_TYPE,
                        createStringToPayload(mWpsNfcConfigurationToken));
                Log.d(TAG, "record : " + record);
                try {
                    Log.d(TAG, "tag connect");
                    ndef.connect();
                    Log.d(TAG, "write Tag write");
                    ndef.writeNdefMessage(new NdefMessage(record));
                    Log.d(TAG, "finish Tag write - 1");
                    makeToast("Tag Write Success!");
                } catch (IOException e) {
                    makeToast("Tag Write Fail!");
                    Log.e(TAG, "Unable to write Wi-Fi config to NFC tag.", e);
                    return;
                } catch (FormatException e) {
                    makeToast("Tag Write Fail!");
                    Log.e(TAG, "Unable to write Wi-Fi config to NFC tag.", e);
                    return;
                } catch (Exception e) {
                    makeToast("Tag Write Fail!");
                    Log.e(TAG, "Unable to write Wi-Fi config to NFC tag.", e);
                }
            } else {
                makeToast("Tag is not writable!");
                Log.e(TAG, "Tag is not writable");
            }
            Log.d(TAG, "finish Tag write - 2");
            if(mWpsNfcWriteDialog != null && mWpsNfcWriteDialog.isShowing()){
                mWpsNfcWriteDialog.dismiss();
                wpsnfcAdapter.disableReaderMode(this);
            }
        } else {
            makeToast("Tag is not writable!");
            Log.e(TAG, "Tag does not support NDEF");
        }
    }

    private void makeToast(final String toastMessage) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WpsNfcDialog.this, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getConfigTokenMessage(String pre_wps_nfc_config_token) {
             byte[] payload;
             String wps_nfc_config_token = "";

             if (pre_wps_nfc_config_token != null) {
                wps_nfc_config_token = pre_wps_nfc_config_token.substring(52, pre_wps_nfc_config_token.length());
             } else {
                 Log.i(TAG, "WPS: pre_wps_nfc_config_token is null");
                 Toast.makeText(WpsNfcDialog.this, "Fail to get data!",
                    Toast.LENGTH_SHORT).show();
                 return;
             }

             if(DBG) Log.d(TAG, "WPS: before NDEF Header remove : " + pre_wps_nfc_config_token);
    
             payload = createStringToPayload(wps_nfc_config_token);
    
             if(wps_nfc_config_token == null) {
                 Log.e(TAG, "WPS: wps_nfc_config_token is null!!");
                 return;
             }
             if(DBG) Log.d(TAG, "WPS: wps_nfc_config_token : " + wps_nfc_config_token);
    
             NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "application/vnd.wfa.wsc".getBytes(), new byte[0], payload);
             message = new NdefMessage(new NdefRecord[] {record});
    }

    public byte[] createStringToPayload(String string) {
        
        int len = string.length();
        String[] payloadSplit = new String[len / 2];

        if(DBG) Log.d(TAG, "WPS: createStringToPayload string: "  + string);
        for (int i = 0; i < len / 2; i++) {
            int a = (i * 2);
            payloadSplit[i] = string.substring(a, a + 2);
            
        }
        
        if(DBG) Log.d(TAG, "WPS: payloadSplit: "  + payloadSplit);

        // make string to byte type.
        ArrayList<Byte> byteArray = new ArrayList<Byte>();
        for (String payloadbyte : payloadSplit) {
//            if(DBG) Log.d(TAG, "WPS: byte : " + payloadbyte);
            try {
                int temp = Integer.parseInt(payloadbyte, 16);
                byteArray.add((byte) temp);
            } catch (Exception e) {
                 Log.d(TAG, "WPS: Parse fail!");
            }
        }

        Byte[] payloadByte = new Byte[byteArray.size()];
        payloadByte = byteArray.toArray(payloadByte);
        byte[] mPayload = new byte[byteArray.size()];
        for (int i = 0; i < payloadByte.length; i++) {
            mPayload[i] = payloadByte[i];
        }
        return mPayload;        
    }

    private void WPSNfcPushModeOn(NdefMessage message) {
//        wpsnfcAdapter.disableForegroundDispatch(this);
//[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for delete shrink&touch UI
//        wpsnfcAdapter.enableForegroundNdefPush(this, message);
        wpsnfcAdapter.setNdefPushMessage(message, this, NfcAdapter.FLAG_NDEF_PUSH_NO_CONFIRM | NfcAdapterAddon.FLAG_HANDOVER_SERVICE);
//[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for delete shrink&touch UI
        if(DBG) Log.i(TAG, "WPS: push: " + message);
    }

    private void WPSNfcPushModeOff() {
//        wpsnfcAdapter.enableForegroundDispatch(this, wpsnfcPendingIntent, wpsnfcWriteTagFilters, null);
//[START] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for delete shrink&touch UI
//        wpsnfcAdapter.disableForegroundNdefPush(this);
        wpsnfcAdapter.setNdefPushMessage(null, this);
//[END] CONFIG_LGE_WPS_NFC : sanghyuk.nam@lge.com : for delete shrink&touch UI
    }

    private void WPSNfcTagWrite(NdefMessage message) {
        // Get NDEF Formatable Tag
        boolean isWriteFail = false;
        NdefFormatable tag = NdefFormatable.get(mTag);
        if (tag != null) {
            // Write to Tag!
            try {
                Log.d(TAG, "WPS: tag.isConnected");
                if (tag.isConnected()==false) {
                    Log.d(TAG, "WPS: tag.connect");
                    tag.connect();
                }
                Log.d(TAG, "WPS: tag.format");
                tag.format(message);
                //tag.format(null);
                Log.d(TAG, "WPS: tag.close");
                tag.close();
            }
            catch (IOException ie){
                isWriteFail = true;
                Log.e(TAG, "WPS: NDEF Format : I/O Error!\n");
            }
            catch (FormatException fe) {
                isWriteFail = true;
                Log.e(TAG, "WPS: NDEF Format : Format Error!\n");
            }
        }
        else {
            Log.d(TAG, "WPS: Ndef.get");
            Ndef ndeftag = Ndef.get(mTag);
            if (ndeftag == null) {
                isWriteFail = true;
                Toast.makeText(WpsNfcDialog.this, "Tag Write Fail!",
                    Toast.LENGTH_SHORT).show();
                Log.e(TAG, "WPS: ndeftag is null");
                return;
            }
            try {
                Log.d(TAG, "WPS: ndeftag.isConnected");
                if (ndeftag.isConnected()==false) {
                    Log.d(TAG, "WPS: ndeftag.connect");
                    ndeftag.connect();
                }
                Log.d(TAG, "WPS: ndeftag.writeNdefMessage");
                if(DBG) Log.d(TAG, "WPS: message = " + message);
                ndeftag.writeNdefMessage(message);
                Log.d(TAG, "WPS: ndeftag.close");
                ndeftag.close();
            }
            catch (IOException ie){
                isWriteFail = true;
                Log.e(TAG, "WPS: NDEF Rewrite : I/O Error!\n");
            }
            catch (FormatException fe) {
                isWriteFail = true;
                Log.e(TAG, "WPS: NDEF Rewrite : Format Error!\n");
            }
        }
        if(isWriteFail == false) {
            Toast.makeText(WpsNfcDialog.this, "Tag Write Success!",
                Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(WpsNfcDialog.this, "Tag Write Fail!",
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return null;
    }

    public void onInit(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        // TODO Auto-generated method stub
        super.onPrepareDialog(id, dialog, args);
    }

}
