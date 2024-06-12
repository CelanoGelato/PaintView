package com.celano.base.ardrawingsketchandpaint.demo.paint.painttools;

import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.ToolInterface;
import com.celano.base.ardrawingsketchandpaint.demo.paint.painttools.abtract.PenAbstract;

public class PlainPen extends PenAbstract implements ToolInterface {
    public PlainPen(int size, int penColor) {
        this(size, penColor, Paint.Style.STROKE);
    }

    public PlainPen(int size, int penColor, Paint.Style style) {
        super(size, penColor, style);
    }

    @NonNull
    @Override
    public String toString() {
        return "\tplainPen: " + "\tshap: " + currentShape + "\thasDraw: " + hasDraw() + "\tsize: " + penSize + "\tstyle:" + style;
    }
}
