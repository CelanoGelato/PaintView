package com.celano.base.ardrawingsketchandpaint.widget;

import android.graphics.Color;

import java.io.Serializable;

public class PaintOptions implements Serializable {
    private int color;
    private float strokeWidth;
    private int alpha;

    public PaintOptions() {
        this(Color.BLACK, 8f, 255);
    }

    public PaintOptions(int color, float strokeWidth, int alpha) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.alpha = alpha;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaintOptions that = (PaintOptions) o;

        if (color != that.color) return false;
        if (Float.compare(that.strokeWidth, strokeWidth) != 0) return false;
        return alpha == that.alpha;
    }

    @Override
    public int hashCode() {
        int result = color;
        result = 31 * result + (strokeWidth != +0.0f ? Float.floatToIntBits(strokeWidth) : 0);
        result = 31 * result + alpha;
        return result;
    }

    @Override
    public String toString() {
        return "PaintOptions{" +
                "color=" + color +
                ", strokeWidth=" + strokeWidth +
                ", alpha=" + alpha +
                '}';
    }
}
