package com.android.settings.lgesetting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListAdapter;

public interface ActivityLogicInterface {
    public ListAdapter onCreate(Bundle savedInstanceState);
    public void onListViewItemClick(AdapterView<?> aParent, View aView, int aPosition, long aId);
    public void onModeChanged(int aOldMode, int aNewMode);
    // return false if not handed -> call super.onActivityResult
    public boolean onActivityResult(int requestCode, int resultCode, Intent data);
    public int getPrepareOptionsMenuSelector(Menu aMenu);
    public void onAddButtonClick(View aView);
    public void onRemoveButtonClick(View aView);
    public void onRemoveConfirmDialogConfirmed();
    public void onDataSetChanged();
    public void onAdapterChanged(int aOldId, int aNewId);
    public boolean onOptionsItemSelected(MenuItem item);
    public void onOptionsMenuRemoveAllConfirmed();
    //public void onListViewItemButtonClick(View aView);
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
    public boolean onItemLongClick(AdapterView<?> aParent, View aView, int aPosition, long aId);
    public boolean onContextItemSelected(MenuItem item);
    public boolean onBackKeyDown(int aKeyCode, KeyEvent aEvent);
    public void onOptionsMenuMarkAllItemSelected(boolean aIsSelected);

    public void onResume();
    public void onPause();
//    public Dialog onCreateDialog(int aId);
//    public void onPrepareDialog(int aId, Dialog aDialog);
}
