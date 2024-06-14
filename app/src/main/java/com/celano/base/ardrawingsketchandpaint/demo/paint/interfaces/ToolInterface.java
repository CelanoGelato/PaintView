package com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces;


import android.graphics.Canvas;

public interface ToolInterface {
    void draw(Canvas canvas);

    void actionDown(float x, float y);

    void actionMove(float x, float y);

    void actionUp(float x, float y);

    boolean hasDraw();
}
