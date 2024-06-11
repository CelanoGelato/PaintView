package com.celano.base.ardrawingsketchandpaint.widget;

import android.graphics.Path;

import java.io.IOException;
import java.io.Writer;

public class Line implements Action {
    private final float x;
    private final float y;

    public Line(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public float getTargetX() {
        return x;
    }

    @Override
    public float getTargetY() {
        return y;
    }

    @Override
    public void perform(Path path) {
        path.lineTo(x, y);
    }

    @Override
    public void perform(Writer writer) {
        try {
            writer.write("L" + x + "," + y);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error writing line coordinates to Writer", e);
        }
    }
}
