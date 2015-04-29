package com.lge.nfclock.service.common;

import java.lang.Enum;

import android.os.Parcelable;
import android.os.Parcel;

public enum LockResultType implements Parcelable {

    RESULT_LOCK(1),
    RESULT_UNLOCK(2),
    RESULT_CLF_UNLOCK(3),
    RESULT_UIM_UNLOCK(4),
    RESULT_FAIL_PW_FOR_UNLOCK(5),
    RESULT_FAIL_PW_FOR_LOCK(9),
    RESULT_BLOCK_UIM(6),
    RESULT_CLF_UNLOCK_UNAVAILABLE_UIM(7),
    RESULT_CLF_LOCK_UNAVAILABLE_UIM(8),
    RESULT_ERROR(10),
    RESULT_FAIL_MATCHED_MDN(11),
    RESULT_FAIL_MATCHED_MEID(12);

    private int     mInnerValue;

    LockResultType(int value) {
        mInnerValue = value;
    }

    public static final Parcelable.Creator<LockResultType> CREATOR = new Parcelable.Creator<LockResultType>() {

         public LockResultType createFromParcel(Parcel in) {

             return LockResultType.values()[in.readInt()];
         }

         public LockResultType[] newArray(int size) {
             return new LockResultType[size];
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
