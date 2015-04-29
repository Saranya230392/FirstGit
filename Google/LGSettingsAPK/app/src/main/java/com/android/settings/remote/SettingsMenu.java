package com.android.settings.remote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class SettingsMenu implements Menu {
    private Context mContext;
    private Menu mMenu;

    public SettingsMenu(Context context) {
        mContext = context;
    }

    public SettingsMenu(Context context, Menu menu) {
        mContext = context;
        mMenu = menu;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return mMenu.add(groupId, itemId, order, mContext.getString(titleRes));
    }

    @Override
    public MenuItem add(CharSequence title) {
        return mMenu.add(title);
    }

    @Override
    public MenuItem add(int titleRes) {
        return mMenu.add(mContext.getString(titleRes));
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        return mMenu.add(groupId, itemId, order, title);
    }

    @Override
    public SubMenu addSubMenu(CharSequence title) {
        return mMenu.addSubMenu(title);
    }

    @Override
    public SubMenu addSubMenu(int titleRes) {
        return mMenu.addSubMenu(mContext.getString(titleRes));
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return mMenu.addSubMenu(groupId, itemId, order, title);
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return mMenu.addSubMenu(groupId, itemId, order, mContext.getString(titleRes));
    }

    @Override
    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller,
            Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        return mMenu.addIntentOptions(groupId, itemId, order, caller, specifics, intent, flags,
                outSpecificItems);
    }

    @Override
    public void removeItem(int id) {
        mMenu.removeItem(id);
    }

    @Override
    public void removeGroup(int groupId) {
        mMenu.removeGroup(groupId);
    }

    @Override
    public void clear() {
        mMenu.clear();
    }

    @Override
    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        mMenu.setGroupCheckable(group, checkable, exclusive);
    }

    @Override
    public void setGroupVisible(int group, boolean visible) {
        mMenu.setGroupVisible(group, visible);
    }

    @Override
    public void setGroupEnabled(int group, boolean enabled) {
        mMenu.setGroupEnabled(group, enabled);
    }

    @Override
    public boolean hasVisibleItems() {
        return mMenu.hasVisibleItems();
    }

    @Override
    public MenuItem findItem(int id) {
        return mMenu.findItem(id);
    }

    @Override
    public int size() {
        return mMenu.size();
    }

    @Override
    public MenuItem getItem(int index) {
        return mMenu.getItem(index);
    }

    @Override
    public void close() {
        mMenu.close();
    }

    @Override
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        return mMenu.performShortcut(keyCode, event, flags);
    }

    @Override
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return mMenu.isShortcutKey(keyCode, event);
    }

    @Override
    public boolean performIdentifierAction(int id, int flags) {
        return mMenu.performIdentifierAction(id, flags);
    }

    @Override
    public void setQwertyMode(boolean isQwerty) {
        mMenu.setQwertyMode(isQwerty);
    }

}