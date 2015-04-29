package com.lge.handwritingime.manager;

import java.util.ArrayList;

import com.lge.handwritingime.data.Stroke;

public class StrokeManager {
    private ArrayList<Stroke> mStrokes;

    public interface OnStrokeChangeListener {
        public void onStrokeChanged();
    }

    // singleton
    private static StrokeManager mInstance = new StrokeManager();

    private StrokeManager() {
        mStrokes = new ArrayList<Stroke>();
    }

    public static StrokeManager getInstance() {
        return mInstance;
    }

    public boolean add(Stroke stroke) {
        mStrokes.add(stroke);
        return true;
    }

    public Stroke remove(int index) {
        return mStrokes.remove(index);
    }

    public boolean remove(Stroke stroke) {
        return mStrokes.remove(stroke);
    }

    public boolean removeAll(ArrayList<Stroke> strokes) {
        return mStrokes.removeAll(strokes);
    }

    public boolean isEmpty() {
        return mStrokes.isEmpty();
    }

    public int size() {
        return mStrokes.size();
    }

    public Stroke get(int index) throws IndexOutOfBoundsException {
        return mStrokes.get(index);
    }

    public ArrayList<Stroke> getAll() {
        return new ArrayList<Stroke>(mStrokes);
    }

    public float getMaxX() {
        float result = Float.MIN_VALUE;

        ArrayList<Stroke> list = new ArrayList<Stroke>(mStrokes);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Stroke s = list.get(i);
                if (s != null) {
                    result = Math.max(s.getMaxX(), result);
                }
            }
        }

        return result;
    }

    public void clear() {
        mStrokes.clear();
    }

}