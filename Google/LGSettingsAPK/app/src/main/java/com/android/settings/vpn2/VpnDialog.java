/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.vpn2;

import com.android.internal.net.VpnProfile;
import com.android.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class VpnDialog extends AlertDialog implements TextWatcher,
        View.OnClickListener, AdapterView.OnItemSelectedListener {
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private final DialogInterface.OnClickListener mListener;
    private final VpnProfile mProfile;

    private boolean mEditing;
    private boolean mNew; // kerry

    private View mView;

    private TextView mName;
    private Spinner mType;
    private TextView mServer;
    private TextView mUsername;
    private TextView mPassword;
    private TextView mSearchDomains;
    private TextView mDnsServers;
    private TextView mRoutes;
    private CheckBox mMppe;
    private TextView mL2tpSecret;
    private TextView mIpsecIdentifier;
    private TextView mIpsecSecret;
    private Spinner mIpsecUserCert;
    private Spinner mIpsecCaCert;
    private Spinner mIpsecServerCert;
    private CheckBox mSaveLogin;

    // [shpark82.park] FRIENDLY VPN [START]
    public ArrayAdapter<String> UserAdapter;
    public ArrayAdapter<String> CAadApter;
    public ArrayAdapter<String> ServerAdapter;
    public List<String> oPerlishArrayUser = new ArrayList<String>();
    public List<String> oPerlishArrayCA = new ArrayList<String>();
    public List<String> oPerlishArrayServer = new ArrayList<String>();
    public String mCurrentUserKey = null;
    public String mCurrentCAKey = null;
    public String mCurrentServerKey = null;
    public int mCurrentUserPos = 0;
    public int mCurrentCAPos = 0;
    public int mCurrentServerPos = 0;
    // [shpark82.park] FRIENDLY VPN [END]
    VpnDialog(Context context, DialogInterface.OnClickListener listener,
            VpnProfile profile, boolean editing, boolean isNew) {
        super(context);
        mListener = listener;
        mProfile = profile;
        mEditing = editing;
        mNew = isNew;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mView = (VpnSettings.ISTABLET ?
                getLayoutInflater().inflate(R.layout.vpn_dialog_tablet, null)
                : getLayoutInflater().inflate(R.layout.vpn_dialog, null));
        setView(mView);
        setInverseBackgroundForced(true);

        Context context = getContext();

        // First, find out all the fields.
        mName = (TextView)mView.findViewById(R.id.name);
        mType = (Spinner)mView.findViewById(R.id.type);
        mServer = (TextView)mView.findViewById(R.id.server);
        mUsername = (TextView)mView.findViewById(R.id.username);
        mPassword = (TextView)mView.findViewById(R.id.password);
        mSearchDomains = (TextView)mView.findViewById(R.id.search_domains);
        mDnsServers = (TextView)mView.findViewById(R.id.dns_servers);
        mRoutes = (TextView)mView.findViewById(R.id.routes);
        mMppe = (CheckBox)mView.findViewById(R.id.mppe);
        mL2tpSecret = (TextView)mView.findViewById(R.id.l2tp_secret);
        mIpsecIdentifier = (TextView)mView.findViewById(R.id.ipsec_identifier);
        mIpsecSecret = (TextView)mView.findViewById(R.id.ipsec_secret);
        mIpsecUserCert = (Spinner)mView.findViewById(R.id.ipsec_user_cert);
        mIpsecCaCert = (Spinner)mView.findViewById(R.id.ipsec_ca_cert);
        mIpsecServerCert = (Spinner)mView.findViewById(R.id.ipsec_server_cert);
        mSaveLogin = (CheckBox)mView.findViewById(R.id.save_login);

        // Second, copy values from the profile.
        mName.setText(mProfile.name); // kerry start - cursor position on vpn edit dialog
        EditText et = (EditText)mName;
        CharSequence text = mName.getText();
        if (text != null && text.length() != 0) {
            et.setSelection(text.length());
        }
        mName = (TextView)et; // kerry end
        mType.setSelection(mProfile.type);
        mServer.setText(mProfile.server);
        if (mProfile.saveLogin) {
            mUsername.setText(mProfile.username);
            mPassword.setText(mProfile.password);
        }
        mSearchDomains.setText(mProfile.searchDomains);
        mDnsServers.setText(mProfile.dnsServers);
        mRoutes.setText(mProfile.routes);
        mMppe.setChecked(mProfile.mppe);
        mL2tpSecret.setText(mProfile.l2tpSecret);
        mIpsecIdentifier.setText(mProfile.ipsecIdentifier);
        mIpsecSecret.setText(mProfile.ipsecSecret);

        // [encryption-vpn@lge.com] FRIENDLY VPN [START]
        loadUserCertificates(mIpsecUserCert, Credentials.USER_PRIVATE_KEY,
                R.string.device_info_not_available, mProfile.ipsecUserCert);
        loadCACertificates(mIpsecCaCert, Credentials.CA_CERTIFICATE,
                R.string.vpn_no_ca_cert, mProfile.ipsecCaCert);
        loadServerCertificates(mIpsecServerCert, Credentials.USER_CERTIFICATE,
                R.string.sp_vpn_no_server_cert_NORMAL, mProfile.ipsecServerCert);
        mSaveLogin.setChecked(mProfile.saveLogin);
        // [encryption-vpn@lge.com] FRIENDLY VPN [END]

        // Third, add listeners to required fields.
        mName.addTextChangedListener(this);
        mType.setOnItemSelectedListener(this);
        mServer.addTextChangedListener(this);
        mUsername.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        mDnsServers.addTextChangedListener(this);
        mRoutes.addTextChangedListener(this);
        mIpsecSecret.addTextChangedListener(this);
        mIpsecUserCert.setOnItemSelectedListener(this);
        // [encryption-vpn@lge.com] FRIENDLY VPN [START]
        mIpsecCaCert.setOnItemSelectedListener(this);
        mIpsecServerCert.setOnItemSelectedListener(this);
        // [encryption-vpn@lge.com] FRIENDLY VPN [END]

        // kerry start
        mMppe.setSoundEffectsEnabled(true);
        mMppe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // v.playSoundEffect(0);
            }
        });

        mSaveLogin.setSoundEffectsEnabled(true);
        mSaveLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // v.playSoundEffect(0);
            }
        });
        // kerry end

        // Forth, determine to do editing or connecting.
        boolean valid = validate(true);
        mEditing = mEditing || !valid;

        if (mEditing) {

            if (true == mNew) {
                setTitle(R.string.vpn_create);
            } else {
                setTitle(R.string.vpn_edit);
            }

            // Show common fields.
            mView.findViewById(R.id.editor).setVisibility(View.VISIBLE);

            // Show type-specific fields.
            changeType(mProfile.type);

            // Show advanced options directly if any of them is set.
            View showOptions = mView.findViewById(R.id.show_options);
            if (mProfile.searchDomains.isEmpty() && mProfile.dnsServers.isEmpty() &&
                    mProfile.routes.isEmpty()) {
                showOptions.setOnClickListener(this);
            } else {
                onClick(showOptions);
            }

            // Create a button to save the profile.
            setButton(DialogInterface.BUTTON_POSITIVE,
                    context.getString(R.string.vpn_save), mListener);
        } else {
            setTitle(context.getString(R.string.vpn_connect_to, mProfile.name));

            // Not editing, just show username and password.
            mView.findViewById(R.id.login).setVisibility(View.VISIBLE);

            // Create a button to connect the network.
            setButton(DialogInterface.BUTTON_POSITIVE,
                    context.getString(R.string.vpn_connect), mListener);
        }

        // Always provide a cancel button.
        setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.vpn_cancel), mListener);

        // Let AlertDialog create everything.
        super.onCreate(null);

        // Disable the action button if necessary.
        getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(mEditing ? valid : validate(false));

        // Workaround to resize the dialog for the input method.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void afterTextChanged(Editable field) {
        getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(validate(mEditing));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void onClick(View showOptions) {
        showOptions.setVisibility(View.GONE);
        mView.findViewById(R.id.options).setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // [encryption-vpn@lge.com] FRIENDLY VPN [START]
        if (parent == mType) {
            changeType(position);
        } else if (parent == mIpsecUserCert) {
            if (mIpsecUserCert.getSelectedItemPosition() == 0) {
                mIpsecUserCert.setSelection(mCurrentUserPos);
                mCurrentCAKey = (String)mIpsecCaCert.getItemAtPosition(mCurrentCAPos);
                mCurrentServerKey = (String)mIpsecServerCert.getItemAtPosition(mCurrentServerPos);
                Intent intent = new Intent(getContext(), CertInstallSender.class);
                intent.putExtra("Type", 1);
                getContext().startActivity(intent);
            } else {
                mCurrentUserPos = mIpsecUserCert.getSelectedItemPosition();
            }
        } else if (parent == mIpsecCaCert) {
            if (mIpsecCaCert.getSelectedItemPosition() == 0) {
                mIpsecCaCert.setSelection(mCurrentCAPos);
                mCurrentUserKey = (String)mIpsecUserCert.getItemAtPosition(mCurrentUserPos);
                mCurrentServerKey = (String)mIpsecServerCert.getItemAtPosition(mCurrentServerPos);
                Intent intent = new Intent(getContext(), CertInstallSender.class);
                intent.putExtra("Type", 2);
                getContext().startActivity(intent);
            } else {
                mCurrentCAPos = mIpsecCaCert.getSelectedItemPosition();
            }
        } else if (parent == mIpsecServerCert) {
            if (mIpsecServerCert.getSelectedItemPosition() == 0) {
                mIpsecServerCert.setSelection(mCurrentServerPos);
                mCurrentUserKey = (String)mIpsecUserCert.getItemAtPosition(mCurrentUserPos);
                mCurrentCAKey = (String)mIpsecCaCert.getItemAtPosition(mCurrentCAPos);
                Intent intent = new Intent(getContext(), CertInstallSender.class);
                intent.putExtra("Type", 3);
                getContext().startActivity(intent);
            } else {
                mCurrentServerPos = mIpsecServerCert.getSelectedItemPosition();
            }
        }
        // [encryption-vpn@lge.com] FRIENDLY VPN [END]
        getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(validate(mEditing));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void changeType(int type) {
        // First, hide everything.
        mMppe.setVisibility(View.GONE);
        mView.findViewById(R.id.l2tp).setVisibility(View.GONE);
        mView.findViewById(R.id.ipsec_psk).setVisibility(View.GONE);
        mView.findViewById(R.id.ipsec_user).setVisibility(View.GONE);
        mView.findViewById(R.id.ipsec_peer).setVisibility(View.GONE);

        // Then, unhide type-specific fields.
        switch (type) {
            case VpnProfile.TYPE_PPTP:
                mMppe.setVisibility(View.VISIBLE);
                break;

            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
                mView.findViewById(R.id.l2tp).setVisibility(View.VISIBLE);
                //FALL-THROUGH
            case VpnProfile.TYPE_IPSEC_XAUTH_PSK:
                mView.findViewById(R.id.ipsec_psk).setVisibility(View.VISIBLE);
                break;

            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
                mView.findViewById(R.id.l2tp).setVisibility(View.VISIBLE);
                //FALL-THROUGH
            case VpnProfile.TYPE_IPSEC_XAUTH_RSA:
                mView.findViewById(R.id.ipsec_user).setVisibility(View.VISIBLE);
                //FALL-THROUGH
            case VpnProfile.TYPE_IPSEC_HYBRID_RSA:
                mView.findViewById(R.id.ipsec_peer).setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private boolean validate(boolean editing) {
        if (!editing) {
            return mUsername.getText().length() != 0 && mPassword.getText().length() != 0;
        }
        if (mName.getText().length() == 0 || mServer.getText().length() == 0 ||
                !validateAddresses(mDnsServers.getText().toString(), false) ||
                !validateAddresses(mRoutes.getText().toString(), true)) {
            return false;
        }
        switch (mType.getSelectedItemPosition()) {
            case VpnProfile.TYPE_PPTP:
            case VpnProfile.TYPE_IPSEC_HYBRID_RSA:
                return true;

            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
            case VpnProfile.TYPE_IPSEC_XAUTH_PSK:
                return mIpsecSecret.getText().length() != 0;

            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
            case VpnProfile.TYPE_IPSEC_XAUTH_RSA:
            return mIpsecUserCert.getSelectedItemPosition() != 1;
            default:
                break;
        }
        return false;
    }

    private boolean validateAddresses(String addresses, boolean cidr) {
        try {
            for (String address : addresses.split(" ")) {
                if (address.isEmpty()) {
                    continue;
                }
                // Legacy VPN currently only supports IPv4.
                int prefixLength = 32;
                if (cidr) {
                    String[] parts = address.split("/", 2);
                    address = parts[0];
                    prefixLength = Integer.parseInt(parts[1]);
                }
                byte[] bytes = InetAddress.parseNumericAddress(address).getAddress();
                int integer = (bytes[3] & 0xFF) | (bytes[2] & 0xFF) << 8 |
                        (bytes[1] & 0xFF) << 16 | (bytes[0] & 0xFF) << 24;
                if (bytes.length != 4 || prefixLength < 0 || prefixLength > 32 ||
                        (prefixLength < 32 && (integer << prefixLength) != 0)) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void loadCertificates(Spinner spinner, String prefix, int firstId, String selected) {
        Context context = getContext();
        String first = (firstId == 0) ? "" : context.getString(firstId);
        String[] certificates = mKeyStore.saw(prefix);

        if (certificates == null || certificates.length == 0) {
            certificates = new String[] {first};
        } else {
            String[] array = new String[certificates.length + 1];
            array[0] = first;
            System.arraycopy(certificates, 0, array, 1, certificates.length);
            certificates = array;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, certificates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        for (int i = 1; i < certificates.length; ++i) {
            if (certificates[i].equals(selected)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    // [encryption-vpn@lge.com] FRIENDLY VPN [START]
    public void initUserCert(String[] keys, int type, String installedKey) {
        final Context context = getContext();
        oPerlishArrayUser.clear();
        oPerlishArrayUser.add(context.getResources().getString(
                R.string.sp_credentials_install_NORMAL));
        oPerlishArrayUser.add(context.getString(R.string.device_info_not_available));
        for (String key : keys) {
            oPerlishArrayUser.add(key);
        }

        UserAdapter.sort(new Comparator<String>() {
            public int compare(String arg0, String arg1) {
                if (arg0.equals(context.getString(R.string.device_info_not_available))
                        || arg0.equals(context.getString(R.string.sp_credentials_install_NORMAL))
                        || arg1.equals(context.getString(R.string.device_info_not_available))
                        || arg1.equals(context.getString(R.string.sp_credentials_install_NORMAL))) {
                    return 0;
                } else {
                    return arg0.compareTo(arg1);
                }
            }
        });
        UserAdapter.notifyDataSetChanged();

        if (type == 1 && installedKey != null) {
            mIpsecUserCert.setSelection(UserAdapter.getPosition(installedKey));
        }
        if (mCurrentUserKey != null) {
            mIpsecUserCert.setSelection(UserAdapter.getPosition(mCurrentUserKey));
            mCurrentUserKey = null;
        }
    }

    public void initCACert(String[] keys, int type, String installedKey) {
        final Context context = getContext();
        oPerlishArrayCA.clear();
        oPerlishArrayCA.add(context.getResources()
                .getString(R.string.sp_credentials_install_NORMAL));
        oPerlishArrayCA.add(context.getString(R.string.vpn_no_ca_cert));
        for (String key : keys) {
            oPerlishArrayCA.add(key);
        }

        CAadApter.sort(new Comparator<String>() {
            public int compare(String arg0, String arg1) {
                if (arg0.equals(context.getString(R.string.vpn_no_ca_cert))
                        || arg0.equals(context.getString(R.string.sp_credentials_install_NORMAL))
                        || arg1.equals(context.getString(R.string.vpn_no_ca_cert))
                        || arg1.equals(context.getString(R.string.sp_credentials_install_NORMAL))) {
                    return 0;
                } else {
                    return arg0.compareTo(arg1);
                }
            }
        });
        CAadApter.notifyDataSetChanged();
        if (type == 2 && installedKey != null) {
            mIpsecCaCert.setSelection(CAadApter.getPosition(installedKey));
        }
        if (mCurrentCAKey != null) {
            mIpsecCaCert.setSelection(CAadApter.getPosition(mCurrentCAKey));
            mCurrentCAKey = null;
        }
    }

    public void initServerCert(String[] keys, int type, String installedKey) {
        final Context context = getContext();
        oPerlishArrayServer.clear();
        oPerlishArrayServer.add(context.getResources().getString(
                R.string.sp_credentials_install_NORMAL));
        oPerlishArrayServer.add(context.getString(R.string.sp_vpn_no_server_cert_NORMAL));
        for (String key : keys) {
            oPerlishArrayServer.add(key);
        }

        ServerAdapter.sort(new Comparator<String>() {
            public int compare(String arg0, String arg1) {
                if (arg0.equals(context.getString(R.string.sp_vpn_no_server_cert_NORMAL))
                        || arg0.equals(context.getString(R.string.sp_credentials_install_NORMAL))
                        || arg1.equals(context.getString(R.string.sp_vpn_no_server_cert_NORMAL))
                        || arg1.equals(context.getString(R.string.sp_credentials_install_NORMAL))) {
                    return 0;
                } else {
                    return arg0.compareTo(arg1);
                }
            }
        });
        ServerAdapter.notifyDataSetChanged();
        if (type == 3 && installedKey != null) {
            mIpsecServerCert.setSelection(ServerAdapter.getPosition(installedKey));
        }
        if (mCurrentServerKey != null) {
            mIpsecServerCert.setSelection(ServerAdapter.getPosition(mCurrentServerKey));
            mCurrentServerKey = null;
        }
    }

    private void loadUserCertificates(Spinner spinner, String prefix, int firstId, String selected) {
        Context context = getContext();
        String[] certificates = mKeyStore.saw(prefix);

        UserAdapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, oPerlishArrayUser) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater)mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(android.R.layout.simple_spinner_item, null);
                }
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                if (position == 0) {
                    tv.setText(UserAdapter.getItem(mCurrentUserPos));
                } else {
                    tv.setText(UserAdapter.getItem(position));
                }
                return v;
            }
        };
        UserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(UserAdapter);

        initUserCert(certificates, 0, null);

        spinner.setSelection(1);
        for (int i = 0; i < UserAdapter.getCount(); ++i) {
            if (UserAdapter.getItem(i).equals(selected)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void loadCACertificates(Spinner spinner, String prefix, int firstId, String selected) {
        Context context = getContext();
        String[] certificates = mKeyStore.saw(prefix);

        CAadApter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, oPerlishArrayCA) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater)mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(android.R.layout.simple_spinner_item, null);
                }
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                if (position == 0) {
                    tv.setText(CAadApter.getItem(mCurrentCAPos));
                } else {
                    tv.setText(CAadApter.getItem(position));
                }
                return v;
            }
        };
        CAadApter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(CAadApter);

        initCACert(certificates, 0, null);

        spinner.setSelection(1);
        for (int i = 0; i < CAadApter.getCount(); ++i) {
            if (CAadApter.getItem(i).equals(selected)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void loadServerCertificates(Spinner spinner, String prefix, int firstId, String selected) {
        Context context = getContext();
        String[] certificates = mKeyStore.saw(prefix);

        ServerAdapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, oPerlishArrayServer) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater)mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(android.R.layout.simple_spinner_item, null);
                }
                TextView tv = (TextView)v.findViewById(android.R.id.text1);

                if (position == 0) {
                    tv.setText(ServerAdapter.getItem(mCurrentServerPos));
                } else {
                    tv.setText(ServerAdapter.getItem(position));
                }
                return v;
            }
        };
        ServerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ServerAdapter);

        initServerCert(certificates, 0, null);

        spinner.setSelection(1);
        for (int i = 0; i < ServerAdapter.getCount(); ++i) {
            if (ServerAdapter.getItem(i).equals(selected)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    // [encryption-vpn@lge.com] FRIENDLY VPN [END]


    boolean isEditing() {
        return mEditing;
    }

    VpnProfile getProfile() {
        // First, save common fields.
        VpnProfile profile = new VpnProfile(mProfile.key);
        profile.name = mName.getText().toString();
        profile.type = mType.getSelectedItemPosition();
        profile.server = mServer.getText().toString().trim();
        profile.username = mUsername.getText().toString();
        profile.password = mPassword.getText().toString();
        profile.searchDomains = mSearchDomains.getText().toString().trim();
        profile.dnsServers = mDnsServers.getText().toString().trim();
        profile.routes = mRoutes.getText().toString().trim();

        // Then, save type-specific fields.
        switch (profile.type) {
            case VpnProfile.TYPE_PPTP:
                profile.mppe = mMppe.isChecked();
                break;

            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
                profile.l2tpSecret = mL2tpSecret.getText().toString();
                //FALL-THROUGH
            case VpnProfile.TYPE_IPSEC_XAUTH_PSK:
                profile.ipsecIdentifier = mIpsecIdentifier.getText().toString();
                profile.ipsecSecret = mIpsecSecret.getText().toString();
                break;

            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
                profile.l2tpSecret = mL2tpSecret.getText().toString();
                //FALL-THROUGH
            case VpnProfile.TYPE_IPSEC_XAUTH_RSA:
            if (mIpsecUserCert.getSelectedItemPosition() != 1) {
                    profile.ipsecUserCert = (String)mIpsecUserCert.getSelectedItem();
                }
                //FALL-THROUGH
            case VpnProfile.TYPE_IPSEC_HYBRID_RSA:
            if (mIpsecCaCert.getSelectedItemPosition() != 1) {
                    profile.ipsecCaCert = (String)mIpsecCaCert.getSelectedItem();
                }
            if (mIpsecServerCert.getSelectedItemPosition() != 1) {
                    profile.ipsecServerCert = (String)mIpsecServerCert.getSelectedItem();
                }
                break;
            default:
                break;
        }

        profile.saveLogin = mSaveLogin.isChecked();
        return profile;
    }
}
