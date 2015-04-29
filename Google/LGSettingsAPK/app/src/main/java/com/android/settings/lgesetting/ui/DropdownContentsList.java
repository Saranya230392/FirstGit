package com.android.settings.lgesetting.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.android.settings.R;

// child class must call "setAdapter(int, ListAdapter) at setupInitialAdapter()
public abstract class DropdownContentsList extends ContentsList implements DropdownListIntent {

    private static final int REQUEST_CODE_OFFSET_SHOW_DROPDOWN = 0;

    protected abstract ActivityLogicInterface getCurLogic();
    protected abstract int getDropdownEntryResource();
    protected abstract int getDropdownEntrySelector();
    protected abstract int getBaseRequestCode();
    //protected abstract int getCapacity(int aIndex);

    ////////////////////////////////////////////////////////////////////////////
    //    Override
    protected void onAdapterChanged(int aOldId, int aNewId){
    }
    //    Override
    ////////////////////////////////////////////////////////////////////////////

    // NOTE: Don't expose mAdapterList out of this class (ex: getAdapterList()).
    // we expand mAdapterList's size for matching id to index when calling registerAdapter with large number id.
    // so, Calling mAdapterList.add or .remove at outside this class raise unsynced mAdapterCount
    private ArrayList<ListAdapter> mAdapterList = new ArrayList<ListAdapter>();

    private View mHeaderLayout;
    private Button mDropdownButton;
    private int mIndex = -1;
    private String[] mDropdownEntries = null;
    private int mAdapterCount = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViews();

        mDropdownButton.setOnClickListener(this);

        Resources resources = getResources();
        int resourceId = getDropdownEntryResource();
        if(resourceId != 0)
            mDropdownEntries = resources.getStringArray(resourceId);

    }

    public void onClick(View aView) {
        if(aView == mDropdownButton)
            showDropdownList(getDropdownEntryResource(), getDropdownEntrySelector());
        else
            super.onClick(aView);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_CANCELED)
            return;

        int requestCodeOffset = requestCode - getBaseRequestCode();
        switch(requestCodeOffset){
        case REQUEST_CODE_OFFSET_SHOW_DROPDOWN:
            int index = data.getIntExtra(DropdownListIntent.INTENT_EXTRA_INDEX, mIndex);
            setCurAdapter(index);
            return;

        default:

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected int registerAdapter(int aId, ListAdapter aAdapter){
        if(mAdapterList.size() <= aId){
            int count = aId - mAdapterList.size();
            for(int index= 0; index < count; index++){
                mAdapterList.add(null);
            }
            mAdapterList.add(aId, aAdapter);
            mAdapterCount++;
        }else{
            mAdapterList.set(aId, aAdapter);
            if(aAdapter == null)
                mAdapterCount--;
        }

        if(1 < mAdapterCount){
            mHeaderLayout.setVisibility(View.VISIBLE);
        }else{
            mHeaderLayout.setVisibility(View.GONE);
        }

        return aId;
    }

    public ListAdapter getAdapter(int aId){
        return mAdapterList.get(aId);
    }

    protected int getContentViewResourceId(){
        return R.layout.ui_dropdown_contents_list;
    }

    private void findViews(){
        // header
        mHeaderLayout = (LinearLayout)findViewById(R.id.headerLayout);
        mDropdownButton = (Button)mHeaderLayout.findViewById(R.id.dropdownButton);
    }

    protected final void setAdapter(ListAdapter aAdapter){
        // do nothing - this methods hide ContentsList.setAdapter() to child
    }

    protected void setCurAdapter(ListAdapter aAdapter){
        setCurAdapter(mIndex);
    }

    protected int getCurAdapterId(){
        return mIndex;
    }

    protected void setCurAdapter(int aNewId){ //, boolean aForce){
        if(mIndex == aNewId) /*&& (aForce == false)*/{
            return;
        }

        enableShowNoListMessage(false);

        int oldId = mIndex;

        mIndex = aNewId;
        if(mDropdownEntries != null)
            mDropdownButton.setText(mDropdownEntries[aNewId]);

        ListAdapter adapter = mAdapterList.get(aNewId);
        super.setAdapter(adapter);

        onAdapterChanged(oldId, aNewId);

        enableShowNoListMessage(true);
    }

    protected void showDropdownList(int aEntryResource, int aSelector){
        if(aEntryResource != 0){
            Intent intent = new Intent(this, DropdownList.class);
            intent.putExtra(DropdownListIntent.INTENT_EXTRA_ENTRY, aEntryResource);
            intent.putExtra(DropdownListIntent.INTENT_EXTRA_INDEX, mIndex);
            intent.putExtra(DropdownListIntent.INTENT_EXTRA_SELECTOR, aSelector);
            startActivityForResult(intent, getBaseRequestCode() + REQUEST_CODE_OFFSET_SHOW_DROPDOWN);
        }
    }

}