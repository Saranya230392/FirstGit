package com.android.settings.lockscreen.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View.BaseSavedState;

/**
 * The parecelable for saving and restoring a lock pattern view.
 */
class LgeLockPatternSavedState extends BaseSavedState {

    private final String mSerializedPattern;
    private final int mDisplayMode;
    private final boolean mInputEnabled;
    private final boolean mInStealthMode;
    private final boolean mTactileFeedbackEnabled;

    /**
     * Constructor called from {@link LgeLockPatternViewBase#onSaveInstanceState()}
     */
    LgeLockPatternSavedState(Parcelable superState, String serializedPattern, int displayMode,
            boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled) {
        super(superState);
        mSerializedPattern = serializedPattern;
        mDisplayMode = displayMode;
        mInputEnabled = inputEnabled;
        mInStealthMode = inStealthMode;
        mTactileFeedbackEnabled = tactileFeedbackEnabled;
    }

    /**
     * Constructor called from {@link #CREATOR}
     */
    private LgeLockPatternSavedState(Parcel in) {
        super(in);
        mSerializedPattern = in.readString();
        mDisplayMode = in.readInt();
        mInputEnabled = (Boolean)in.readValue(null);
        mInStealthMode = (Boolean)in.readValue(null);
        mTactileFeedbackEnabled = (Boolean)in.readValue(null);
    }

    public String getSerializedPattern() {
        return mSerializedPattern;
    }

    public int getDisplayMode() {
        return mDisplayMode;
    }

    public boolean isInputEnabled() {
        return mInputEnabled;
    }

    public boolean isInStealthMode() {
        return mInStealthMode;
    }

    public boolean isTactileFeedbackEnabled() {
        return mTactileFeedbackEnabled;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mSerializedPattern);
        dest.writeInt(mDisplayMode);
        dest.writeValue(mInputEnabled);
        dest.writeValue(mInStealthMode);
        dest.writeValue(mTactileFeedbackEnabled);
    }

    public static final Parcelable.Creator<LgeLockPatternSavedState> CREATOR =
            new Creator<LgeLockPatternSavedState>() {
                @Override
                public LgeLockPatternSavedState createFromParcel(Parcel in) {
                    return new LgeLockPatternSavedState(in);
                }

                @Override
                public LgeLockPatternSavedState[] newArray(int size) {
                    return new LgeLockPatternSavedState[size];
                }
            };
}