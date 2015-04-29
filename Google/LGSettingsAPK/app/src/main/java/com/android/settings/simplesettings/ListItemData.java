package com.android.settings.simplesettings;

public class ListItemData {

    private int mType;
    private String mName;
    private String mBtn1;
    private String mBtn2;
    private String mBtn3;
    private Object mDetailView;

    public ListItemData(int mType, String mName, String mBtn1, String mBtn2,
            String mBtn3, Object detailView) {
        this.mType = mType;
        this.mName = mName;
        this.mBtn1 = mBtn1;
        this.mBtn2 = mBtn2;
        this.mBtn3 = mBtn3;
        this.mDetailView = detailView;
    }

    public Object getDetailView() {
        return mDetailView;
    }

    public int getmType() {
        return mType;
    }

    public String getmName() {
        return mName;
    }

    public String getmBtn1() {
        return mBtn1;
    }

    public String getmBtn2() {
        return mBtn2;
    }

    public String getmBtn3() {
        return mBtn3;
    }

}
