
package com.android.settings.defaultapp;

public class DefaultAppInfo implements Comparable<DefaultAppInfo> {
    public String mLabel;
    public String mPackageName;

    public DefaultAppInfo(String name, String packageName) {
        mLabel = name;
        mPackageName = packageName;
    }

    @Override
    public int compareTo(DefaultAppInfo another) {
        return mLabel.compareTo(another.mLabel);
    }


}