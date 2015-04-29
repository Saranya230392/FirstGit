package com.lge.nfclock.service.common;

import android.os.Parcelable;
import android.os.Parcel;

/*
// [START] remote lock value
public static final int STATE_REMOTE_ALL_UNLOCKED = 0;// clf, uim, mdm unlock
public static final int STATE_REMOTE_CLF_LOCKED   = 1;// clf lock
public static final int STATE_REMOTE_UIM_LOCKED   = 2;// uim lock
public static final int STATE_REMOTE_MDM_LOCKED   = 3;// mdm lock
public static final int STATE_REMOTE_CLF_UIM_LOCKED = 4;// clf, uim lock
public static final int STATE_REMOTE_CLF_MDM_LOCKED = 5;// clf, mdm lock
public static final int STATE_REMOTE_UIM_MDM_LOCKED = 6;// uim, mdm lock
public static final int STATE_REMOTE_ALL_LOCKED = 7;// clf, uim, mdm lock
public static final int STATE_REMOTE_CLF_UNLOCKED_UIM_UNAVAILABLE = 8;
public static final int STATE_REMOTE_CLF_LOCKED_UIM_UNAVAILABLE = 9;
//[END]
*/

public enum RemoteLockStatusType implements Parcelable {
    REMOTE_ERROR(-1),
    REMOTE_ALL_UNLOCK(0),
    REMOTE_CLF_LOCK(1),
    REMOTE_UIM_LOCK(2),
    REMOTE_MDM_LOCK(3),
    REMOTE_CLF_UIM_LOCK(4),
    REMOTE_CLF_MDM_LOCK(5),
    REMOTE_UIM_MDM_LOCK(6),
    REMOTE_CLF_UNLOCK_UNAVAILABLE_UIM(8),
    REMOTE_CLF_LOCK_UNAVAILABLE_UIM(9),
    REMOTE_ALL_LOCK(7);

    private int     mInnerValue;

    RemoteLockStatusType(int value) {
        mInnerValue = value;
    }

    public static final Parcelable.Creator<RemoteLockStatusType> CREATOR = new Parcelable.Creator<RemoteLockStatusType>() {

         public RemoteLockStatusType createFromParcel(Parcel in) {

             return RemoteLockStatusType.values()[in.readInt()];
         }

         public RemoteLockStatusType[] newArray(int size) {
             return new RemoteLockStatusType[size];
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
