package com.celano.base.ardrawingsketchandpaint.demo.paint.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {
    private Paint mPaint;
    private float radius = 8f;

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = (float) canvas.getWidth();
        float height = (float) canvas.getHeight();
        float cX = width / 2;
        float cY = height / 2;

        canvas.drawCircle(cX, cY, radius / 2, mPaint);
    }

    public void setCircleRadius(float r) {
        radius = r;
        invalidate();
    }

    public void setAlpha(int newAlpha) {
        int alpha = (newAlpha * 255) / 100;
        mPaint.setAlpha(alpha);
        invalidate();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }
}
