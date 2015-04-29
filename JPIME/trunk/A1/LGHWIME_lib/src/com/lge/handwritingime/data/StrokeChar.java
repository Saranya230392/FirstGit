package com.lge.handwritingime.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StrokeChar {
    class StrokeResult implements Comparable<StrokeResult> {
        public float score;
        public String label;

        public StrokeResult(float score, String label) {
            this.score = score;
            this.label = label;
        }

        @Override
        public int compareTo(StrokeResult another) {
            return (score > another.score) ? 1 : 0;
        }
    }

    private ArrayList<Stroke> mStrokes;
    private ArrayList<StrokeResult> mResults;
    private float mMinX;
    private float mMaxX;
    private int mSelectedIndex;

    public StrokeChar(List<Stroke> strokes) {
        mStrokes = new ArrayList<Stroke>(strokes);
        mMinX = Float.MAX_VALUE;
        mMaxX = Float.MIN_VALUE;
        for (Stroke s : strokes) {
            mMaxX = Math.max(s.getMaxX(), mMaxX);
            mMinX = Math.min(s.getMinX(), mMinX);
        }

        mResults = new ArrayList<StrokeResult>();
        mSelectedIndex = 0;
    }

    public StrokeChar(List<Stroke> strokes, ArrayList<Float> scores, ArrayList<String> labels) {
        mStrokes = new ArrayList<Stroke>(strokes);
        mMinX = Float.MAX_VALUE;
        mMaxX = Float.MIN_VALUE;
        for (Stroke s : mStrokes) {
            mMaxX = Math.max(s.getMaxX(), mMaxX);
            mMinX = Math.min(s.getMinX(), mMinX);
        }
        mResults = new ArrayList<StrokeResult>();
        for (int i = 0; i < labels.size(); i++) {
            StrokeResult result = new StrokeResult(scores.get(i), labels.get(i));
            mResults.add(result);
        }
        Collections.sort(mResults);
        mSelectedIndex = 0;
    }

    public synchronized void setResult(ArrayList<Float> scores, ArrayList<String> labels) {
        if (scores == null || labels == null)
            return;

        mResults.clear();
        for (int i = 0; i < labels.size(); i++) {
            StrokeResult result = new StrokeResult(scores.get(i), labels.get(i));
            mResults.add(result);
        }
        Collections.sort(mResults);
        mSelectedIndex = 0;
    }

    public float getMinX() {
        return mMinX;
    }

    public float getMaxX() {
        return mMaxX;
    }

    public boolean setSelectedLabel(String string) {
        for (int i = 0; i < mResults.size(); i++) {
            if (mResults.get(i).label.equals(string)) {
                mSelectedIndex = i;
                return true;
            }
        }
        return false;
    }

    public ArrayList<Stroke> getStrokes() {
        return mStrokes;
    }

    public synchronized ArrayList<String> getLabels() {
        ArrayList<String> list = new ArrayList<String>();
        for (StrokeResult sr : mResults) {
            list.add(sr.label);
        }
        return list;
    }

    public String getSelectedLabel() {
        return getLabel(mSelectedIndex);
    }

    public String getLabel(int index) {
        if (index < 0)
            return "";

        if (index < mResults.size())
            return mResults.get(mSelectedIndex).label;
        else
            return "";
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    @Override
    public String toString() {
        return getSelectedLabel();
    }
}