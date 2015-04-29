
package com.android.settings;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AutoSeparator {

    public static final String SEPARATE_MAC = ":";
    public static final String SEPARATE_IP = ".";
    public static final int MAC_ADDRESS = 0;
    public static final int IP_ADDRESS = 1;

    // ip saving class
    private class AutoDot {
        String beforeText;
        String afterText;

        AutoDot() {
            beforeText = "";
            afterText = "";
        }

        public void clear() {
            setBeforeText("");
            setAfterText("");
        }

        public String getAfterText() {
            Log.w(TAG, "[MHP_GOOKY] getAfterText >> afterText: "+afterText);
            return afterText;
        }

        public String[] getAfterTextArr(String separate) {
            Log.w(TAG, "[MHP_GOOKY] getAfterTextArr >> separate: "+getAfterText().split("\\"+separate));
            return getAfterText().split("\\" + separate);
        }

        public int getAfterTextArrLen(String separate) {
            Log.w(TAG, "[MHP_GOOKY] getAfterTextArrLen >> separate: "+separate);
            return getAfterTextArr(separate).length;
        }

        public int getAfterTextLen() {
            Log.w(TAG, "[MHP_GOOKY] getAfterTextLen >> afterText: "+afterText.length());
            return afterText.length();
        }

        public String getBeforeText() {
            Log.w(TAG, "[MHP_GOOKY] getBeforeText >> getBeforeText(): "+beforeText);
            return beforeText;
        }

        public void setAfterText(String afterText) {
            this.afterText = afterText;
            Log.w(TAG, "[MHP_GOOKY] setAfterText >> afterText: "+afterText);
        }

        public void setBeforeText(String beforeText) {
            this.beforeText = beforeText;
            Log.w(TAG, "[MHP_GOOKY] setBeforeText >> beforText: "+beforeText);
        }
    }

    private final String TAG = "AutoSeparator";

    // 0(ip address), 1(mac address)
    int kind = 0;
    Context context;
    // ip or mac EditText
    EditText et;

    // ip saving obj.
    AutoDot ad;
    Button mButton1;

    Button mButton2;

    // ip TextWatcher
    TextWatcher ipTextWatcher = new TextWatcher() {
        String beforeText;
        String afterText;

        // @Override
        @Override
        public void afterTextChanged(Editable s) {
            String ipaddress = s.toString();
            Log.w(TAG, "[MHP_GOOKY] IP string >> " + ipaddress);

            if (mButton1 != null) {
                mButton1.setEnabled(ipaddress.split("\\.").length == 4);
            }
            if (mButton2 != null) {
                mButton2.setEnabled(ipaddress.split("\\.").length == 4);
            }

        }

        // @Override
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {

            beforeText = s.toString();
            if (ad != null) {
                Log.w(TAG, "[MHP_GOOKY] beforeTextChanged >>s: "+s+",count: "+count+",after: "+after);
                ad.setBeforeText(beforeText);
            }
            Log.w(TAG,"[MHP_GOOKY] beforeTextChanged >> 2, before: "+beforeText);
        }

        // 20110307
        // @Override
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {

            Log.w(TAG, "[MHP_GOOKY] onTextChanged >>");

            if (et != null) {

                String oriIpAddr = s.toString();
                int posCur = et.getSelectionEnd();
                String[] arrDot = oriIpAddr.split("\\.");
                // String result = "";
                StringBuffer result = new StringBuffer();
                boolean isBeforeDot = false;
                boolean isDel = false;

                Log.w(TAG, "[MHP_GOOKY] onTextChanged >> 1");

                // mode is del or not
                if (oriIpAddr.length() < beforeText.length()) {
                    isDel = true;
                }
                // each room length
                int tmpArrDotLen = 0;
                // end cursor pos.
                int tmpPosCur = 0;

                // no event for dot
                if (oriIpAddr.indexOf('.') != -1
                        && oriIpAddr.charAt(oriIpAddr.length() - 1) == SEPARATE_IP
                                .charAt(0)) {
                    isBeforeDot = true;
                    Log.w(TAG, "[MHP_GOOKY] onTextChanged >> 1-1");

                    // delete proc.
                    if (isDel
                            && oriIpAddr.length() > 1
                            && oriIpAddr.charAt(oriIpAddr.length() - 2) != SEPARATE_IP
                                    .charAt(0)) {

                        et.setText(oriIpAddr.substring(0,
                                oriIpAddr.length() - 1));
                        ad.setAfterText(et.getText().toString());
                        et.setSelection(posCur - 1);
                        Log.w(TAG, "[MHP_GOOKY] onTextChanged >> 1-1-1, posCur: "+posCur);
                    }
                }
                // input proc.
                if (!isDel && !isBeforeDot && arrDot.length < 4) {
                    Log.w(TAG, "[MHP_GOOKY] onTextChanged >> 1-2");
                    // calculate for the dot standard
                    for (int i = 0; i < arrDot.length; i++) {
                        // each room length
                        tmpArrDotLen += arrDot[i].length();
                        // end cursor pos.
                        tmpPosCur = tmpArrDotLen + (arrDot.length - 1);
                        Log.w(TAG, "[MHP_GOOKY] onTextChanged >> 1-2-1, tmpArrDotLen: "+tmpArrDotLen+",posCur: "+tmpPosCur);
                        // if room length is 3 and current cursor pos is end,
                        // input dot
                        if (arrDot[i].length() == 3 && posCur == tmpPosCur) {
                            // result += arrDot[i] + SEPARATE_IP;
                            result.append(arrDot[i] + SEPARATE_IP);
                            Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-2-1-1");
                        } else if (arrDot[i].length() > 3) {
                            // 123.->123->1234->123.4
                            // result += arrDot[i].substring(0, 3) +
                            // SEPARATE_IP + arrDot[i].substring(3);
                            result.append(arrDot[i].substring(0, 3)
                                    + SEPARATE_IP
                                    + arrDot[i].substring(3));
                            Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-2-1-2");
                        } else {
                            Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-2-1-3");

                            if (i < arrDot.length - 1) {
                                // result += arrDot[i] + SEPARATE_IP;
                                result.append(arrDot[i] + SEPARATE_IP);
                                Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-2-1-3-1");
                            } else {
                                // result += arrDot[i];
                                result.append(arrDot[i]);
                                Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-2-1-3-2");
                            }
                        }
                    }
                    afterText = result.toString();
                    Log.w(TAG, "[MHP_GOOKY] onTextChanged >> 1-3");
                    if (!result.toString().equals(s.toString())) {
                        et.setText(result.toString());
                        // et.setSelection(posCur+1); // for preventing
                        // outofbound error when monkey test
                        et.setSelection(result.toString().length());
                        ad.setAfterText(afterText);
                        Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-3-1, resultstring: "+result.toString()+",posCur: "+posCur);
                    } else {
                        ad.setAfterText(s.toString());
                        Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-3-1, s.string: "+s.toString());
                    }
                } else {
                    ad.setAfterText(et.getText().toString());
                    Log.w(TAG,"[MHP_GOOKY] onTextChanged >> 1-4, s.string: "+s.toString());
                }

            }
        }

    };

    // mac TextWatcher
    TextWatcher macTextWatcher = new TextWatcher() {
        String beforeText;
        String afterText;
        int afterTextLength;

        @Override
        public void afterTextChanged(Editable s) {
            if (mButton1 != null) {
                // LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com], 2011.08.25,
                // fixed for TD#22965
                mButton1.setEnabled((s.length() > 16) || (afterTextLength > 16));
                // LGE_VERIZON_WIFI_E, [kyubyoung.lee@lge.com]
            }
            Log.w(TAG, "[MHP_GOOKY] afterTextChanged : "+s.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            Log.w(TAG,"[MHP_GOOKY] beforeTextChanged >>s: "+s+",count: "+count+",after: "+after);
            beforeText = s.toString();
            afterTextLength = after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            if (s == null) {
                return;
            }
            try {
                Log.w(TAG,"[MHP_GOOKY] onTextChanged >>s: "+s.toString()+",start: "+start+",before: "+before+",count: "+count);
                String oriMacAddr = s.toString();
                // cursor pos.
                int posCur = 0;

                oriMacAddr = oriMacAddr.replaceAll(SEPARATE_MAC, "");

                String result = "";
                boolean isDel = false;
                boolean isErr = false;

                Log.w(TAG,"[MHP_GOOKY] onTextChanged >> beforText: "+beforeText);

                if (s == null || beforeText == null) {
                    return;
                }

                if (s.toString().length() < beforeText.length()) {
                    isDel = true;
                }

                if (s.toString().split(SEPARATE_MAC).length <= 6) {

                    result = getMacResult(oriMacAddr, isDel);

                    // isErr = chkErrMacAddr(result,s.toString());
                    // isErr = checkMacAddress(result);

                    Log.w(TAG,"[MHP_GOOKY] length <= 6 >> result "+result+",isDel: "+isDel);

                    afterText = result;
                    if (!isErr) {

                        if (result != null && !result.equals(s.toString())) {
                            Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 1 ");

                            if ((isDel && (et.getSelectionEnd() - 1) == afterText
                                    .length())
                                    || et.getSelectionEnd() == (afterText
                                            .length() - 1)
                                    || et.getSelectionEnd() == afterText
                                            .length()) {
                                posCur = result.length();
                                Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 1-1, posCur: "+posCur);

                                if (isDel
                                        && et.getSelectionEnd() == afterText
                                                .length() - 1) {
                                    posCur -= 1;
                                    Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 1-1-1, posCur: "+posCur);
                                }
                            } else {
                                posCur = et.getSelectionEnd();
                                Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 1-2, posCur: "+posCur);
                                /*
                                 * // 1st. MR QM TD fixed if( result.length() <
                                 * posCur ){ posCur = result.length(); } //
                                 * LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com],
                                 * 2011.08.25, fixed for TD#22965 if( posCur <
                                 * result.length()) { if(result.length() >
                                 * MAC_MAX_LENGTH ) { posCur =
                                 * MAC_MAX_LENGTH; //MHPLog.w(TAG,
                                 * "[MHP_GOOKY] onTextChanged  >> MAX < result.length(), posCur: "
                                 * +posCur); } } // LGE_VERIZON_WIFI_E,
                                 * [kyubyoung.lee@lge.com], 2011.08.25, fixed
                                 * for TD#22965 //when set value with separator,
                                 * temporary cursor position(getSelectionEnd())
                                 * is zero. //manually set cursor postion. if(
                                 * posCur == 0 && result.length() > 0){ posCur =
                                 * result.length(); //MHPLog.w(TAG,
                                 * "[MHP_GOOKY] onTextChanged  >> 1-2-1, posCur: "
                                 * +posCur); }
                                 */
                                if (posCur != 0) {
                                    if (isDel) {
                                        Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 1-2-2-1, posCur: "+posCur);
                                        if (result.charAt(posCur - 1) == SEPARATE_MAC
                                                .charAt(0)) {
                                            posCur -= 1;
                                            Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 2-1-1-1, posCur: "+posCur);
                                        }
                                    } else {
                                        Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 1-2-1-2, posCur: "+posCur);
                                        if (result.charAt(posCur - 1) == SEPARATE_MAC
                                                .charAt(0)) {
                                            posCur += 1;
                                            Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 1-2-1-2-1, posCur: "+posCur);
                                        }
                                    }
                                }
                            }

                            et.setText(result);
                            // LGE_VERIZON_WIFI_S, [kyubyoung.lee@lge.com],
                            // 2011.08.25, fixed for TD#22965
                            /*
                             * // 1st. MR QM TD fixed if( posCur <
                             * result.length()) { //MHPLog.w(TAG,
                             * "[MHP_GOOKY] onTextChanged posCur < result >> 2, posCur: "
                             * +posCur); if( result.length() >
                             * MAC_MAX_LENGTH ) { posCur =
                             * MAC_MAX_LENGTH; //MHPLog.w(TAG,
                             * "[MHP_GOOKY] onTextChanged result >MAX  >> 2-1, posCur: "
                             * +posCur); } else { posCur = result.length();
                             * //MHPLog.w(TAG,
                             * "[MHP_GOOKY] onTextChanged result < MAX  >> 2-2, posCur: "
                             * +posCur); } } else if(posCur > result.length()) {
                             * posCur = result.length(); //MHPLog.w(TAG,
                             * "[MHP_GOOKY] onTextChanged posCur > result >> 2, posCur: "
                             * +posCur); } // LGE_VERIZON_WIFI_E,
                             * [kyubyoung.lee@lge.com], 2011.08.25, fixed for
                             * TD#22965
                             */
                             // [START][LGE_WIFI][TMO][US][jeongwook.kim@lge.com] : fixed mac address cursor error when paste mac address
			     if (result.length() >= 17) {
                                et.setSelection(17);
			     } else {
			        if (beforeText.length() - afterText.length() > 1 ||
					afterText.length() - beforeText.length() > 1) {
					et.setSelection(result.length());
				} else {
					et.setSelection(posCur);
				}
			     }
			     // [START][LGE_WIFI][TMO][US][jeongwook.kim@lge.com] : fixed mac address cursor error when paste mac address

                            Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 2, posCur: "+posCur);

                        } else {
                            Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 3, posCur: "+posCur);
                            if (result != null && result.length() != 0
                                    && beforeText != null && afterText != null) {
                                Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 3-1, posCur: "+posCur);
                                if (beforeText.length() > afterText.length()) {
                                    Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 3-1-1, posCur: "+posCur);
                                    if (et.getSelectionEnd() % 3 == 0
                                            && et.getSelectionEnd() == afterText
                                                    .length()) {
                                        et.setSelection(result.length() - 1);
                                        Log.w(TAG,"[MHP_GOOKY] onTextChanged  >> 3-1-1-1, posCur: "+posCur+"length: "+result.length());
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "wrong MacAddress",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (Exception e) {
                Log.e("TextChanged ERR", e.getMessage());
            }
        }
    };

    public AutoSeparator(Context context, EditText et, int kind) {
        this(et, kind);
        this.context = context;
        Log.w(TAG, "[MHP_GOOKY] AutoSeparator >>  2");
    }

    public AutoSeparator(EditText et, int kind) {
        this.et = et;
        this.kind = kind;

        if (kind == IP_ADDRESS) {
            ad = new AutoDot();
            Log.w(TAG, "[MHP_GOOKY] AutoSeparator >> ");
        }
    }

    public void adClear() {
        if (ad != null) {
            ad.clear();
            Log.w(TAG, "[MHP_GOOKY] adClear >> ");
        }
    }

    private boolean chkContSeparate(String st, String separate) {
        boolean isErr = false;
        if (st.indexOf(separate + separate) != -1) {
            isErr = true;
        }
        Log.w(TAG, "[MHP_GOOKY] chkContSeparate(), isErr: "+isErr);
        return isErr;
    }

    public boolean chkErrMacAddr(String result, String input) {
        boolean isErr = false;

        if (input.indexOf(SEPARATE_MAC) == 0
                || chkContSeparate(input, SEPARATE_MAC)) {
            isErr = true;
        } else if (input.endsWith(SEPARATE_MAC)
                && input.split(SEPARATE_MAC)[input
                        .split(SEPARATE_MAC).length - 1].length() == 1) {
            isErr = true;
        }
        Log.w(TAG, "[MHP_GOOKY] chkErrMacAddr(), isErr: "+isErr);
        return isErr;
    }

    // 20110307
    public String getMacResult(String oriMacAddr, boolean isDel) {
        // String result = "";
        StringBuffer result = new StringBuffer();

        Log.w(TAG,"[MHP_GOOKY] getMacResult(): oriMacAddre: "+oriMacAddr+", isDel: "+isDel);

        for (int i = 0; i < oriMacAddr.length(); i++) {
            if (!isDel && i != 0 && (i + 1) % 2 == 0 && i < 11) {

                // result += oriMacAddr.charAt(i) + SEPARATE_MAC;
                result.append(oriMacAddr.charAt(i) + SEPARATE_MAC);
                Log.w(TAG, "[MHP_GOOKY] getMacResult(): 1");
            } else if (isDel && i != 0 && (i + 1) % 2 == 0
                    && i < (oriMacAddr.length() - 1) && i < 11) {

                // result += oriMacAddr.charAt(i) + SEPARATE_MAC;
                result.append(oriMacAddr.charAt(i) + SEPARATE_MAC);
                Log.w(TAG, "[MHP_GOOKY] getMacResult(): 2");
            } else {

                // result += oriMacAddr.charAt(i);
                result.append(oriMacAddr.charAt(i));
                Log.w(TAG, "[MHP_GOOKY] getMacResult(): 3");
            }
        }
        return result.toString();
    }

    public TextWatcher getTextWatcher() {
        Log.w(TAG, "[MHP_GOOKY] getTextWatcher >> "+kind);
        return (kind == IP_ADDRESS) ? ipTextWatcher : macTextWatcher;
    }

    public void setButton(Button btn1, Button btn2) {
        mButton1 = btn1;
        mButton2 = btn2;
        Log.w(TAG, "[MHP_GOOKY] setButton >> ");
    }

}
