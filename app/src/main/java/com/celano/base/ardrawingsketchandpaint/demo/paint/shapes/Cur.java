package com.celano.base.ardrawingsketchandpaint.demo.paint.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.ShapeAble;
import com.celano.base.ardrawingsketchandpaint.demo.paint.shapes.abtract.ShapeAbstract;

public class Cur extends ShapeAbstract {

    public Cur(ShapeAble paintTool) {
        super(paintTool);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);
        canvas.drawPath(mPath, paint);
    }

    @NonNull
    @Override
    public String toString() {
        return "Cur";
    }
}
