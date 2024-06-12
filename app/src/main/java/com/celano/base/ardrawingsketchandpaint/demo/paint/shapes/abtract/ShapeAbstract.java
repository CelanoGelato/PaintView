package com.celano.base.ardrawingsketchandpaint.demo.paint.shapes.abtract;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.ShapeAble;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.ShapesInterface;
import com.celano.base.ardrawingsketchandpaint.demo.paint.painttools.FirstCurrentPosition;


public abstract class ShapeAbstract implements ShapesInterface {

    public ShapeAble paintTool;
    public FirstCurrentPosition firstCurrentPos;
    public Path mPath;
    public float x1 = 0;
    public float y1 = 0;
    public float x2 = 0;
    public float y2 = 0;

    public ShapeAbstract(ShapeAble paintTool) {
        assert (paintTool != null);
        this.paintTool = paintTool;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        firstCurrentPos = paintTool.getFirstLastPoint();
        mPath = paintTool.getPath();
        x1 = firstCurrentPos.firstX;
        y1 = firstCurrentPos.firstY;
        x2 = firstCurrentPos.currentX;
        y2 = firstCurrentPos.currentY;
    }

}
