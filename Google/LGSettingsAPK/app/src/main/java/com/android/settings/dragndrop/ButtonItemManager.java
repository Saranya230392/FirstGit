package com.android.settings.dragndrop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.android.settings.R;
import com.android.settings.dragndrop.ButtonConfiguration.ButtonInfo;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Log;

public class ButtonItemManager {
	private static final String TAG = "ButtonItemManager";
    public static char DB_BUTTON_COMBINATION_DELIMETER = '|';
    private final static SimpleStringSplitter sStringColonSplitter =
            new SimpleStringSplitter(DB_BUTTON_COMBINATION_DELIMETER);
    private Context mContext = null;
    private DBInterface mDBInterface = null;
    private int[] mColumnCount = new int[] { 0, 0 };
    
    private static List<ButtonItem> mUpTrayItems = new ArrayList<ButtonItem>();
    private static List<ButtonItem> mDownTrayItems = new ArrayList<ButtonItem>();

    public ButtonItemManager(Context context) {
    	mContext =  context;
    	mDBInterface = new DBInterface(mContext);
    }
    
    public int[] getColumnCount() {
        return mColumnCount;
    }
    
    public void setColumnCount(int[] ConlumnCount) {
        mColumnCount = ConlumnCount;
    }

    public List<ButtonItem> getUpTrayItems() {
        return mUpTrayItems;
    }

    public List<ButtonItem> getDownTrayItems() {
        return mDownTrayItems;
    }

    public void setUpTrayItems(List<ButtonItem> items) {
        mUpTrayItems = items;
    }

    public void setDownTrayItems(List<ButtonItem> items) {
        mDownTrayItems = items;
    }

    public int getUpTrayItemSize() {
        return mUpTrayItems.size();
    }

    public int getDownTrayItemSize() {
        return mDownTrayItems.size();
    }

    public void swapUpTrayItems(int a, int b) {
        Collections.swap(mUpTrayItems, a, b);
    }

    public ButtonItem deleteItemAtDownTray(String key) {
        for (int i = 0; i < mDownTrayItems.size(); i++) {
            ButtonItem item = mDownTrayItems.get(i);
            if (item != null && item.getKey().equals(key)) {
                mDownTrayItems.set(i, null);
                return item;
            }
        }
        return null;
    }

    public ButtonItem deleteItemAtUpTray(String key) {
        Log.d("chan", "deleteItemAtUpTray");
        for (int i = 0; i < mUpTrayItems.size(); i++) {
            ButtonItem item = mUpTrayItems.get(i);
            if (item.getKey().equals(key) && item.getType() != ButtonItem.KEYTYPE_UP_TRAY) {
                return mUpTrayItems.remove(i);
            }
        }
        return null;
    }

    public void moveToUpTray(String key, int to) {
        ButtonItem item = deleteItemAtDownTray(key);
        if (item != null) {
            mUpTrayItems.add(to, item);
        }
    }

    public void moveToDownTray(String key) {
        Log.d("chan", "moveToDownTray");
        ButtonItem item = deleteItemAtUpTray(key);
        if (item != null) {
            int col = ButtonConfiguration.getColumnOfDownTray(key);
            if (col >= 0) {
                mDownTrayItems.set(col, item);
            }
        }
    }
    
    void deleteButtonCombination(String mPackageMame) {
        boolean isExistDB = false;
    	
    	String enableButtonCombi = mDBInterface.readDB();
        if (enableButtonCombi == null) {
            enableButtonCombi = "";
            return;
        }

        Log.d(TAG, "Delete mPackageMame : " + mPackageMame);
        SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enableButtonCombi);

        StringBuilder enabledButtonCombinationBuilder = new StringBuilder(enableButtonCombi);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            if (componentNameString.equals("QMemo")) {
                Log.d(TAG, "Exist DB");
                enabledButtonCombinationBuilder.delete(
                        enabledButtonCombinationBuilder.indexOf(componentNameString),
                        enabledButtonCombinationBuilder.indexOf(componentNameString)
                                + componentNameString.length() + 1);
                isExistDB = true;
            }
        }
        if (isExistDB) {
        	mDBInterface.writeDB(enabledButtonCombinationBuilder.toString());
        }
    }
    
    public void buildItems() {
//        if (mDBInterface.readDB(context, this) == false) {
        if (isInit() == false) {
            initItems();
        }
    }

    private boolean isInit() {
    	String combination = mDBInterface.readDB();
        if (combination == null || combination.length() == 0) {
            return false;
        }

        List<ButtonItem> upTrayitems = new ArrayList<ButtonItem>();

        List<String> fixedDownTrayItems = ButtonConfiguration.getDownTrayItems(mContext);
        String temp = new String(combination);
        while (combination.length() > 0) {
            int index = combination.indexOf(DB_BUTTON_COMBINATION_DELIMETER);
            index = index >= 0 ? index : combination.length();
            String key = combination.substring(0, index);
            if (combination.length() > index + 1) {
                combination = combination.substring(index + 1);
            } else {
                combination = "";
            }
            int type = ButtonItem.KEYTYPE_UP_TRAY;
            if (fixedDownTrayItems.contains(key)) {
                type = ButtonItem.KEYTYPE_DOWN_TRAY;
            }
            ButtonItem item = createButtonItem(key, type);
            upTrayitems.add(item);
        }
        setUpTrayItems(upTrayitems);

        List<ButtonItem> downTrayitems = new ArrayList<ButtonItem>();

        for (String key : fixedDownTrayItems) {
            if (temp.contains(key)) {
                downTrayitems.add(null);
                continue;
            }
            downTrayitems.add(createButtonItem(key, ButtonItem.KEYTYPE_DOWN_TRAY));
        }
        setDownTrayItems(downTrayitems);

        return true;
	}

	private void initItems() {
        if (mContext == null) {
            return;
        }
        List<ButtonItem> upTrayitems = new ArrayList<ButtonItem>();

        List<String> uptrayItems = ButtonConfiguration.getUpTrayItems(mContext);
        for (String item : uptrayItems) {
            upTrayitems.add(createButtonItem(item, ButtonItem.KEYTYPE_UP_TRAY));
        }
        setUpTrayItems(upTrayitems);

        List<ButtonItem> downTrayitems = new ArrayList<ButtonItem>();

        List<String> downtrayItems = ButtonConfiguration.getDownTrayItems(mContext);
        for (String item : downtrayItems) {
            downTrayitems.add(createButtonItem(item, ButtonItem.KEYTYPE_DOWN_TRAY));
        }
        setDownTrayItems(downTrayitems);
    }
    
    public ButtonItem createButtonItem(String key, int type) {
        HashMap<String, ButtonInfo> buttonInfos = ButtonConfiguration.getButtonInfos();
        ButtonInfo buttonInfo = buttonInfos.get(key);
        return new ButtonItem(key, mContext.getString(buttonInfo.mStringId), buttonInfo.mDrawableId,
                type);
    }
    
    private int getIndex(ButtonItem item) {
        int column = ButtonConfiguration.getColumnOfDownTray(item.getKey());
        return mColumnCount[0] + column;
    }
    
    public ButtonItem getItem(int index) {
        ButtonItem item = null;
        if (index >= getUpTrayItemCount()) {
            index -= getUpTrayItemCount();
            item = getItemAtDownTray(index);
        } else {
            item = getItemAtUpTray(index);
        }
        return item;
    }
    
    public int getUpTrayItemCount() {
        return getUpTrayItemSize();
    }

    public int getDownTrayItemCount() {
        /*		int count = 0;
        		for (ButtonItem item : getDownTrayItems()) {
        			if (item == null) continue;
        			count ++;
        		}
        		return count;*/
        return getDownTrayItemSize();
    }
    public ButtonItem getItemAtUpTray(int index) {
        List<ButtonItem> items = getUpTrayItems();
        if (index >= 0 && items.size() > index) {
            return items.get(index);
        } else {
            return null;
        }
    }

    public ButtonItem getItemAtDownTray(int index) {
        List<ButtonItem> items = getDownTrayItems();
        if (index >= 0 && items.size() > index) {
            return items.get(index);
        } else {
            return null;
        }
    }
    
    public int getEmptyColumnAtDownTray() {
        for (int i = 0; i < getUpTrayItemCount(); i++) {
            ButtonItem item = getItemAtUpTray(i);
            if (item == null) {
                return -1;
            }

            if (item.getType() == ButtonItem.KEYTYPE_DOWN_TRAY) {
                return ButtonConfiguration.getColumnOfDownTray(item.getKey());
            }
        }
        return -1;
    }

    
    public int getItemCount() {
        Log.d(TAG, "DownTrayItemCount:" + getDownTrayItemCount() + ", UpTrayItemCount:"
                + getUpTrayItemCount());
        return getDownTrayItemCount() + getUpTrayItemCount();
    }

	public void builderitemlist(List<ButtonItem> buttons, List<ButtonItem> upTrayItems) {
      StringBuilder builder = new StringBuilder();
      for (ButtonItem button : buttons) {
          builder.append(button.getKey());
          builder.append(DB_BUTTON_COMBINATION_DELIMETER);
      }
      if (builder.length() > 0) {
          builder.deleteCharAt(builder.length() - 1);
      }
      mDBInterface.writeDB( builder.toString());
	}
	
    public ButtonItem getItem(String key) {
        for (ButtonItem item : getUpTrayItems()) {
            if (item != null && item.getKey().equals(key)) {
                return item;
            }
        }

        for (ButtonItem item : getDownTrayItems()) {
            if (item != null && item.getKey().equals(key)) {
                return item;
            }
        }
        return null;
    }
    
    public boolean isInOriginDownTray(String key) {
        return ButtonConfiguration.getDownTrayItems(mContext).contains(key);
    }

    public boolean isInOriginUpTray(String key) {
        return ButtonConfiguration.getUpTrayItems(mContext).contains(key);
    }

    public boolean availableLeftRightSwitch(ButtonItem item) {
        List<ButtonItem> items = getUpTrayItems();
        return items.contains(item);
    }
    


    private boolean isDifferentLocation(ButtonItem a, ButtonItem b) {
        if (getUpTrayItemSize() == ButtonConfiguration.CONFIG_MAX_UP_TRAY_KEY_NUM) {
            return (getUpTrayItems().contains(a) && getDownTrayItems().contains(b)) ||
                    (getUpTrayItems().contains(b) && getDownTrayItems().contains(a));
        } else {
            return false;
        }
    }

    public boolean needAnimation(ButtonItem selectedItem, ButtonItem targetItem) {
        if (availableLeftRightSwitch(selectedItem) && availableLeftRightSwitch(targetItem)) {
            return true;
        }

        if (selectedItem.getType() == ButtonItem.KEYTYPE_DOWN_TRAY) {
            if (targetItem != null && targetItem.getType() == ButtonItem.KEYTYPE_DOWN_TRAY) {
                boolean isDifferenceLocation = isDifferentLocation(selectedItem, targetItem);
                return isDifferenceLocation;
            } else if (findItemAtUpTray(selectedItem.getKey())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean findItemAtUpTray(String key) {
        List<ButtonItem> items = getUpTrayItems();
        for (ButtonItem item : items) {
            if (item.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }
    public void printItems() {
        List<ButtonItem> items = getUpTrayItems();
        for (ButtonItem item : items) {
            Log.d(TAG, "printItems: item=" + item.getName() + " at UpTray");
        }
        items = getDownTrayItems();
        for (ButtonItem item : items) {
            if (item != null) {
                Log.d(TAG, "printItems: item=" + item.getName() + " at DownTray");
            } else {
                Log.d(TAG, "printItems: item is Null at DownTray");
            }
        }
    }
    
    public void swapItems(int itemIndexA, int itemIndexB) {
        swapUpTrayItems(itemIndexA, itemIndexB);
    }
	
}
