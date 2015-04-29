package com.android.settings.lge;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.content.Context;
import android.util.Log;
import android.content.res.Configuration;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.lgesetting.Config.Config;
import com.android.settings.Utils;
import java.util.Locale;

public class LGSoftwareHelp extends SettingsPreferenceFragment implements OnClickListener {
    private View mView = null;
    private Button mOk;

    @Override
    public void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged( newConfig );
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.lg_software_help_view, null);
        initControl();
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(mView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        ActionBar actionBar = getActivity().getActionBar();
        if (Utils.supportSplitView(getActivity().getApplicationContext())) {
           if (actionBar != null) {
               actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } else {
            if (actionBar != null) {
               actionBar.setDisplayHomeAsUpEnabled(true);
               actionBar.setIcon(R.drawable.shortcut_connectivity);
            }
        }

        mView = inflater.inflate(R.layout.lg_software_help_view, null);
        initControl();

        return mView;
    }

    private void initControl() {
        //title
        TextView lg_software_title = (TextView)mView.findViewById(R.id.help_howtouse);

        //PC Suite
        LinearLayout category_pcsuite = (LinearLayout)mView.findViewById(R.id.category_pcsuite);

        //OSP
        LinearLayout category_osp = (LinearLayout)mView.findViewById(R.id.category_osp);

        //button
        mOk = (Button)mView.findViewById(R.id.ok_button);
        mOk.setOnClickListener(this);

        //Description
        TextView lg_software_help_number01 = (TextView)mView.findViewById(R.id.lg_software_help_number01);
        TextView lg_software_help_number02 = (TextView)mView.findViewById(R.id.lg_software_help_number02);
        TextView lg_software_help_number03 = (TextView)mView.findViewById(R.id.lg_software_help_number03);
        TextView lg_software_help_number04 = (TextView)mView.findViewById(R.id.lg_software_help_number04);
        TextView lg_software_help_desc00 = (TextView)mView.findViewById(R.id.help_desc);
        TextView lg_software_help_desc01 = (TextView)mView.findViewById(R.id.lg_software_help_desc01);
        TextView lg_software_help_desc02 = (TextView)mView.findViewById(R.id.lg_software_help_desc02);
        TextView lg_software_help_desc04 = (TextView)mView.findViewById(R.id.lg_software_help_desc04);

        //text is set string
        String name = getString(R.string.sp_lg_software_help_software_title_NORMAL);

        if (ConnectivitySettings.getSupportPCSuite() == false) {
            name = getString(R.string.sp_osp_title_NORMAL);
            category_pcsuite.setVisibility(View.GONE);
            lg_software_title.setText(name);
        }

        if (ConnectivitySettings.getSupportOSP() == false) {
            name = getString(R.string.sp_link_cloud_category_NORMAL);
            category_osp.setVisibility(View.GONE);
            lg_software_title.setText(name);
        }

        if (Utils.isRTLLanguage() || Utils.isEnglishDigitRTLLanguage()) {
            lg_software_help_number01.setText(" ." + String.format(Locale.getDefault(), "%d", 1));
            lg_software_help_number02.setText(" ." + String.format(Locale.getDefault(), "%d", 2));
            lg_software_help_number03.setText(" ." + String.format(Locale.getDefault(), "%d", 3));
            lg_software_help_number04.setText(" ." + String.format(Locale.getDefault(), "%d", 4));
        } else {
            lg_software_help_number01.setText("1. ");
            lg_software_help_number02.setText("2. ");
            lg_software_help_number03.setText("3. ");
            lg_software_help_number04.setText("4. ");
        }

        if ((Config.LGU).equals(Config.getOperator()) || (Config.KT).equals(Config.getOperator()) || (Config.SKT).equals(Config.getOperator()))
            lg_software_help_desc01.setText(getString(R.string.sp_lg_software_help_desc01_1_NORMAL, name, "www.lgmobile.co.kr"));
        else
            lg_software_help_desc01.setText(getString(R.string.sp_lg_software_help_desc01_1_NORMAL, name, "www.lg.com"));

        lg_software_help_desc00.setText(getString(R.string.sp_lg_software_help_desc00_NORMAL, name));
        lg_software_help_desc02.setText(getString(R.string.sp_lg_software_help_desc02_NORMAL, name));
        lg_software_help_desc04.setText(getString(R.string.sp_lg_software_help_desc04_NORMAL, name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onClick(View v) {
        int view = v.getId();
        Log.e("PCSuite", "onclikck");
        if (view == R.id.ok_button) {
            finish();
        }
    }


}
