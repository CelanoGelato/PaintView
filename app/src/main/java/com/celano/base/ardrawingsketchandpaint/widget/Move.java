package com.celano.base.ardrawingsketchandpaint.widget;

import android.graphics.Path;

import java.io.IOException;
import java.io.Writer;

public class Move implements Action {
    private final float x;
    private final float y;

    public Move(float x, float y) {
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
        path.moveTo(x, y);
    }

    @Override
    public void perform(Writer writer) {
        try {
            writer.write("M" + x + "," + y);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error writing move coordinates to Writer", e);
        }
    }
}
