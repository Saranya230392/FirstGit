package com.android.settings.simplesettings;

public class TypeSeekbarData {
    private String mName;
    private int mSeekLevel;
    private int mTextViewVisible;

    public TypeSeekbarData(String mName, int seekLevel, int isTextViewVisible) {
        this.mName = mName;
        this.mSeekLevel = seekLevel;
        this.mTextViewVisible = isTextViewVisible;
    }

    public String getmName() {
        return mName;
    }

    public int getSeekLevel() {
        return mSeekLevel;
    }

    public int getTextViewVisible() {
        return mTextViewVisible;
    }

}
