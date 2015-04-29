package com.lge.handwritingime.manager;

import java.util.ArrayList;

import com.lge.handwritingime.data.StrokeChar;

public class StrokeCharManager {
    private ArrayList<StrokeChar> mStrokeChars;

    // singleton
    private static StrokeCharManager mInstance = new StrokeCharManager();

    public static StrokeCharManager getInstance() {
        return mInstance;
    }

    private StrokeCharManager() {
        mStrokeChars = new ArrayList<StrokeChar>();
        clear();
    }

    public void clear() {
        mStrokeChars.clear();
    }

    public void add(StrokeChar strokeChar) {
        mStrokeChars.add(strokeChar);
    }

    public void add(int index, StrokeChar strokeChar) {
        mStrokeChars.add(index, strokeChar);
    }

    // public void removeChar() {
    // if (mStrokeChars.size() > 0) {
    // mStrokeChars.remove(mStrokeChars.size() - 1);
    // }
    // }

    public int indexOf(StrokeChar strokeChar) {
        return mStrokeChars.indexOf(strokeChar);
    }

    public boolean removeChar(StrokeChar sc) {
        return mStrokeChars.remove(sc);
    }

    public StrokeChar remove(int index) {
        return mStrokeChars.remove(index);
    }

    public StrokeChar get(int index) throws IndexOutOfBoundsException {
        return mStrokeChars.get(index);
    }

    public int size() {
        return mStrokeChars.size();
    }

    public boolean isEmpty() {
        return mStrokeChars.isEmpty();
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();

        ArrayList<StrokeChar> list = new ArrayList<StrokeChar>(mStrokeChars);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                StrokeChar sc = list.get(i);
                if (sc != null)
                    sb.append(sc.getSelectedLabel());
            }
        }
        return sb.toString();
    }

    public boolean contains(StrokeChar strokeChar) {
        return mStrokeChars.contains(strokeChar);
    }
}