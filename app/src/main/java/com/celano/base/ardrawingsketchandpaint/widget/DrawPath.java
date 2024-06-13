package com.celano.base.ardrawingsketchandpaint.widget;

import android.graphics.Path;
import android.graphics.RectF;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class DrawPath extends Path implements Serializable {
    private final LinkedList<Action> actions = new LinkedList<>();

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();

        LinkedList<Action> copiedActions = new LinkedList<>(actions);
        actions.clear();
        for (Action action : copiedActions) {
            action.perform(this);
        }
    }

    @Override
    public void reset() {
        actions.clear();
        super.reset();
    }

    @Override
    public void moveTo(float x, float y) {
        actions.add(new Move(x, y));
        super.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) {
        actions.add(new Line(x, y));
        super.lineTo(x, y);
    }

    @Override
    public void quadTo(float x1, float y1, float x2, float y2) {
        actions.add(new Quad(x1, y1, x2, y2));
        super.quadTo(x1, y1, x2, y2);
    }

    public RectF getBounds() {
        if (actions.isEmpty()) {
            return new RectF();
        }

        float left = actions.get(0).getTargetX();
        float top = actions.get(0).getTargetY();
        float right = left;
        float bottom = top;

        for (Action action : actions) {
            float x = action.getTargetX();
            float y = action.getTargetY();
            if (left > x) {
                left = x;
            }
            if (right < x) {
                right = x;
            }
            if (top > y) {
                top = y;
            }
            if (bottom < y) {
                bottom = y;
            }
        }

        return new RectF(left, top, right, bottom);
    }
}
