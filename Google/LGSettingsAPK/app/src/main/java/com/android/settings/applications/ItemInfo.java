package com.android.settings.applications;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

public class ItemInfo {

    protected int order;
    protected String packageName;
    protected String className;
    protected String label;
    protected Drawable icon;
    protected String sizeStr;
    protected String installed;

    protected ItemInfo() {
    }

    public ItemInfo(ItemInfo info) {
        if (info != null) {
            this.packageName = info.getPackageName();
            this.label = info.getLabel();
            this.className = info.getClassName();
            this.order = info.getOrder();
            this.icon = info.getIcon();
            this.sizeStr = info.getSizeStr();
            this.installed = info.getInstalled();
        }
    }

    public ItemInfo(String packageName, String label, String className,
            int order, Drawable icon, String sizeStr, String installed) {
        this.packageName = packageName;
        this.label = label;
        this.className = className;
        this.order = order;
        this.icon = icon;
        this.sizeStr = sizeStr;
        this.installed = installed;

    }

    public String getPackageName() {
        return packageName;
    }

    public String getLabel() {
        return label;
    }

    private Drawable getIcon() {
        // TODO Auto-generated method stub
        return icon;
    }

    public String getClassName() {
        return className;
    }

    public int getOrder() {
        return order;
    }

    public String getSizeStr() {
        return sizeStr;
    }

    public String getInstalled() {
        return installed;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ComponentName getComponentName() {
        return new ComponentName(packageName, className);
    }

    public boolean isSelected() {
        return false;
    }
}
