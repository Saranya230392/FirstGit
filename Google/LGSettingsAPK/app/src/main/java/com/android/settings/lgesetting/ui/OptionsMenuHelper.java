package com.android.settings.lgesetting.ui;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;

public class OptionsMenuHelper {
    public interface ActionHandler{
        //public void onOptionsMenuRemoveConfirmed();
        //public void onOptionsMenuRemoveAllConfirmed();
        public void onOptionsMenuRemoveSelected();
        public void onOptionsMenuRemoveAllSelected();
        public void onOptionsMenuMarkAllItemSelected(boolean aIsSelected);

        //public void onOPtionsMenuUnmarkAllItemSelected();
    }

    public static final int NO_MENU = 0x00000000;

    public static final int VISIBLE_REMOVE = 0x00000001;
    public static final int VISIBLE_REMOVE_ALL = 0x00000002;
    public static final int VISIBLE_MARK_ALL = 0x00000004;
    public static final int VISIBLE_UNMARK_ALL = 0x00000008;

    public static final int ENABLE_REMOVE = 0x00010000;
    public static final int ENABLE_REMOVE_ALL = 0x00020000;
    public static final int ENABLE_MARK_ALL = 0x00040000;
    public static final int ENABLE_UNMARK_ALL = 0x00080000;

    public static final int ID_REMOVE = R.id.remove;
    public static final int ID_REMOVE_ALL = R.id.removeAll;
    public static final int ID_MARK_ALL = R.id.markAll;
    public static final int ID_UNMARK_ALL = R.id.unmarkAll;

    public static boolean onCreate(MenuInflater aInlfater, Menu aMenu){
        aInlfater.inflate(R.menu.ui_contents_list_option_menu, aMenu);
        return true;
    }

    public static boolean onPrepare(int selector, Menu aMenu){
        boolean visible[] = {false, false, false, false};
        boolean enabled[] = {false, false, false, false};
        int visibleCount = 0;

        if(selector == 0){
            // no option menu
            return false;
        }

        if((selector & VISIBLE_REMOVE) == VISIBLE_REMOVE){
            visible[0] = true;
            visibleCount++;
            if((selector & ENABLE_REMOVE) == ENABLE_REMOVE){
                enabled[0] = true;
            }
        }
        if((selector & VISIBLE_REMOVE_ALL) == VISIBLE_REMOVE_ALL){
            visible[1] = true;
            visibleCount++;
            if((selector & ENABLE_REMOVE_ALL) == ENABLE_REMOVE_ALL){
                enabled[1] = true;
            }
        }
        if((selector & VISIBLE_MARK_ALL) == VISIBLE_MARK_ALL){
            visible[2] = true;
            visibleCount++;
            if((selector & ENABLE_MARK_ALL) == ENABLE_MARK_ALL){
                enabled[2] = true;
            }
        }
        if((selector & VISIBLE_UNMARK_ALL) == VISIBLE_UNMARK_ALL){
            visible[3] = true;
            visibleCount++;
            if((selector & ENABLE_UNMARK_ALL) == ENABLE_UNMARK_ALL){
                enabled[3] = true;
            }
        }

        if(visibleCount <= 0)
            return false;

        int count = aMenu.size();
        for(int index=0; index < count; index++){
            MenuItem item = aMenu.getItem(index);
            int id = item.getItemId();
            switch(id){
            case ID_REMOVE:
                item.setVisible(visible[0]);
                item.setEnabled(enabled[0]);
                break;
            case ID_REMOVE_ALL:
                item.setVisible(visible[1]);
                item.setEnabled(enabled[1]);
                break;
            case ID_MARK_ALL:
                item.setVisible(visible[2]);
                item.setEnabled(enabled[2]);
                break;
            case ID_UNMARK_ALL:
                item.setVisible(visible[3]);
                item.setEnabled(enabled[3]);
                break;
            }
        }

        return true;
    }

    private static boolean isSupportedId(int aId){
        return (aId == ID_REMOVE || aId == ID_REMOVE_ALL || aId == ID_MARK_ALL || aId == ID_UNMARK_ALL);
    }

    public static boolean onItemSelected(Context aContext, MenuItem item, final ActionHandler aHandler){
        int id = item.getItemId();
        if(isSupportedId(id)){
            //AlertDialog.Builder builder = null;
            switch(id){
                case ID_REMOVE:
                    aHandler.onOptionsMenuRemoveAllSelected();

                    break;

                case ID_REMOVE_ALL:
                    aHandler.onOptionsMenuRemoveAllSelected();

                    break;

                case ID_MARK_ALL:
                    aHandler.onOptionsMenuMarkAllItemSelected(true);
                    break;

                case ID_UNMARK_ALL:
                    aHandler.onOptionsMenuMarkAllItemSelected(false);
                    break;
            }
            return true;
        }
        return false;
    }
}
