package com.android.settings.utils;
import com.android.settings.AddPatchInfo;
import com.android.settings.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;

public class CheckPatchInfo extends Activity {

    private TextView mAddPatch;
    private AddPatchInfo mAddPatchInfo;
    private int mItem = 0;
    private ArrayList<String> mDEV = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_package);
        getActionBar().setDisplayShowHomeEnabled(true);
        mAddPatch = (TextView) findViewById(R.id.package_txt);
        int item = getIntent().getIntExtra(SettingsUtil.ITEM, SettingsUtil.SOUND);
        set_DevInfo();
        mAddPatchInfo = new AddPatchInfo();
        registerForContextMenu(mAddPatch);
        String title = "patch list";
        switch(item) {
        case SettingsUtil.SOUND :
            title = "Sound patch list";
            //AddPatchInfo.Sound soundInfo = mAddPatchInfo.new Sound();
            break;
        case SettingsUtil.QUICK_BUTTON :
            title = "Quick button patch list";
            //AddPatchInfo.QuickButton quickButton_Info = mAddPatchInfo.new QuickButton();
            break;
        case SettingsUtil.SHORT_CUT :
            title = "Short cut patch list";
            //AddPatchInfo.ShortCut shorCut_Info = mAddPatchInfo.new ShortCut();
            break;
        case SettingsUtil.ACCESSIBILITY :
            title = "Accessibility patch list";
            //AddPatchInfo.Acccessibility accessibility_Info = mAddPatchInfo.new Acccessibility();
            break;
        case SettingsUtil.SECURITY :
            title = "Security patch list";
            //AddPatchInfo.Security security_Info = mAddPatchInfo.new Security();
            break;
        default :
            break;
        }
        setTitle(title);
        mAddPatch.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        mAddPatch.setText(mAddPatchInfo.getPatchInfo().toString());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if ("ALL".equals(item.getTitle().toString())) {
            mAddPatch.setText(mAddPatchInfo.getPatchInfo().toString());
        }
        else {
            mAddPatch.setText(mAddPatchInfo.getPatchInfo_user(item.getTitle().toString()));
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if ("ALL".equals(item.getTitle().toString())) {
            mAddPatch.setText(mAddPatchInfo.getPatchInfo().toString());
        }
        else {
            mAddPatch.setText(mAddPatchInfo.getPatchInfo_user(item.getTitle().toString()));
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int length = mDEV.size();
        for (int i = 0; i < length; i++) {
            menu.add(0, i, i, mDEV.get(i));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        super.onCreateContextMenu(menu, v, menuInfo);
        int length = mDEV.size();
        for (int i = 0; i < length; i++) {
            menu.add(0, i, i, mDEV.get(i));
        }
    }

    private void set_DevInfo() {
        mDEV.add("ALL");
        switch(mItem) {
        case SettingsUtil.SOUND :
        case SettingsUtil.QUICK_BUTTON :
        case SettingsUtil.SHORT_CUT :
        case SettingsUtil.ACCESSIBILITY :
        case SettingsUtil.SECURITY :
            mDEV.add("hakgyu98.kim");
            mDEV.add("hyunjeong.shin");
            mDEV.add("susin.park");
            break;

        default :
            break;
        }
    }
}
