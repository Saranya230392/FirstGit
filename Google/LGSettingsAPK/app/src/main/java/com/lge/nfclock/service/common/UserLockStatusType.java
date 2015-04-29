package com.lge.nfclock.service.common;

import android.os.Parcelable;
import android.os.Parcel;

public enum UserLockStatusType implements Parcelable {
    USER_ERROR(-1),
    USER_ALL_UNLOCK(0),
    USER_CLF_UNLOCK(1),
    USER_UIM_UNLOCK(2),
    USER_CLF_LOCK_UNAVAILABLE_UIM(4),
    USER_CLF_UNLOCK_UNAVAILABLE_UIM(5),
    USER_ALL_LOCK(3);

    private int     mInnerValue;

    UserLockStatusType(int value) {
        mInnerValue = value;
    }

    public static final Parcelable.Creator<UserLockStatusType> CREATOR = new Parcelable.Creator<UserLockStatusType>() {

         public UserLockStatusType createFromParcel(Parcel in) {

             return UserLockStatusType.values()[in.readInt()];
         }

         public UserLockStatusType[] newArray(int size) {
             return new UserLockStatusType[size];
         }
     };

     @Override
     public int describeContents() {
         return 0;
     }


     @Override
     public void writeToParcel (Parcel out, int flag) {
         out.writeInt(ordinal());
     }


    @Override
    public String toString() {
        return this.name();
    }

    public int getValue() {
        return mInnerValue;
    }
}
