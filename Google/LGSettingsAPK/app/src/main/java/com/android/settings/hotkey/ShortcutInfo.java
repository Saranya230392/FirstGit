package com.android.settings.hotkey;

public class ShortcutInfo {
    String label;
    String packageName;
    String className;
    int mCustomIconId;

    ShortcutInfo(String label, String packageName, String className) {
        super();
        this.label = label;
        this.packageName = packageName;
        this.className = className;
        this.mCustomIconId = 0;
    }

    public ShortcutInfo(String label, String packageName, String className, int customIconId) {
        super();
        this.label = label;
        this.packageName = packageName;
        this.className = className;
        this.mCustomIconId = customIconId;
    }

    @Override
    public String toString() {
        return String.format(
                "ShortcutInfo [label=%s, packageName=%s, className=%s, customIconId=%s]", label,
                packageName, className, mCustomIconId);
    }

}
