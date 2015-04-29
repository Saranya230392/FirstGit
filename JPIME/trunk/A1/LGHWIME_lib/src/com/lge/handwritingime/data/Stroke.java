package com.lge.handwritingime.data;

import java.util.ArrayList;

import android.graphics.Point;
import android.graphics.PointF;

import com.visionobjects.im.IStroke;

public class Stroke extends ArrayList<StrokePoint> implements IStroke {
    private static final long serialVersionUID = 5297221888824868549L;

    public boolean add(float x, float y) {
        return add(new StrokePoint(x, y));
    }

    public float getMinX() {
        float minX = Float.MAX_VALUE;

        for (StrokePoint point : this) {
            minX = Math.min(minX, point.x);
        }
        return minX;
    }

    public float getMaxX() {
        float maxX = Float.MIN_VALUE;

        for (StrokePoint point : this) {
            maxX = Math.max(maxX, point.x);
        }
        return maxX;
    }

    @Override
    public int getPointCount() {
        return size();
    }

    @Override
    public float getX(int index) throws IndexOutOfBoundsException {
        return get(index).x;
    }

    @Override
    public float getY(int index) throws IndexOutOfBoundsException {
        return get(index).y;
    }
}

class StrokePoint extends PointF {
    public StrokePoint(float x, float y) {
        super(x, y);
    }

    public StrokePoint(Point p) {
        super(p);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
