package com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces;

import android.graphics.Path;

import com.celano.base.ardrawingsketchandpaint.demo.paint.painttools.FirstCurrentPosition;

public interface ShapeAble {
    Path getPath();

    FirstCurrentPosition getFirstLastPoint();

    void setShape(ShapesInterface shape);
}
