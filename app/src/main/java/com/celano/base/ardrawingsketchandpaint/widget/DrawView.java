package com.celano.base.ardrawingsketchandpaint.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import java.util.LinkedHashMap;

public class DrawView extends View {
    private LinkedHashMap<MyPath, PaintOptions> mPaths = new LinkedHashMap<>();
    private LinkedHashMap<MyPath, PaintOptions> mLastPaths = new LinkedHashMap<>();
    private LinkedHashMap<MyPath, PaintOptions> mUndonePaths = new LinkedHashMap<>();

    private Paint mBitmapPaint = new Paint();
    private MyPath mPath = new MyPath();
    private PaintOptions mPaintOptions = new PaintOptions();

    private float currentX = 0f;
    private float currentY = 0f;
    private float firstX = 0f;
    private float firstY = 0f;
    private boolean mIsSaving = false;
    private boolean mIsStrokeWidthBarEnabled = false;
    private Matrix mTransform = new Matrix();

    private boolean mIsScrolling = false;
    private float mScale = 1f;
    private float mScrollOriginX = 0f;
    private float mScrollOriginY = 0f;
    private float mScrollX = 0f;
    private float mScrollY = 0f;
    private ScaleGestureDetector mScaleGestureDetector;

    private Bitmap mBackground = null;
    private RectF mBackgroundRect = new RectF();

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBitmapPaint.setColor(mPaintOptions.getColor());
        mBitmapPaint.setStyle(Paint.Style.STROKE);
        mBitmapPaint.setStrokeJoin(Paint.Join.ROUND);
        mBitmapPaint.setStrokeCap(Paint.Cap.ROUND);
        mBitmapPaint.setStrokeWidth(mPaintOptions.getStrokeWidth());
        mBitmapPaint.setAntiAlias(true);

        mScaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float oldScale = mScale;
                        mScale *= detector.getScaleFactor();
                        mScale = Math.min(Math.max(mScale, 0.5f), 3.0f);
                        mScrollX += detector.getFocusX() * (oldScale - mScale) / mScale;
                        mScrollY += detector.getFocusY() * (oldScale - mScale) / mScale;
                        invalidate();
                        return true;
                    }
                });
    }

    public void reset() {
        mUndonePaths.clear();
        mPaths.clear();
        mLastPaths.clear();
        clearCanvas();
    }

    public void undo() {
        if (mPaths.isEmpty() && !mLastPaths.isEmpty()) {
            mPaths = (LinkedHashMap<MyPath, PaintOptions>) mLastPaths.clone();
            mLastPaths.clear();
            invalidate();
            return;
        }
        if (mPaths.isEmpty()) {
            return;
        }
        PaintOptions lastPath = mPaths.values().stream().reduce((first, second) -> second).orElse(null);
        MyPath lastKey = mPaths.keySet().stream().reduce((first, second) -> second).orElse(null);

        if (lastKey != null && lastPath != null) {
            mPaths.remove(lastKey);
            mUndonePaths.put(lastKey, lastPath);
        }
        invalidate();
    }

    public void redo() {
        if (mUndonePaths.keySet().isEmpty()) {
            return;
        }

        MyPath lastKey = mUndonePaths.keySet().stream().reduce((first, second) -> second).orElse(null);
        if (lastKey != null) {
            addPath(lastKey, mUndonePaths.get(lastKey));
            mUndonePaths.remove(lastKey);
        }
        invalidate();
    }

    public void setColor(int newColor) {
        Log.d("alpha", String.valueOf(mPaintOptions.getAlpha()));
        @ColorInt
        int alphaColor = ColorUtils.setAlphaComponent(newColor, mPaintOptions.getAlpha());
        mPaintOptions.setColor(alphaColor);
        if (mIsStrokeWidthBarEnabled) {
            invalidate();
        }
    }

    public void setAlpha(int newAlpha) {
        mPaintOptions.setAlpha(newAlpha);
        setColor(mPaintOptions.getColor());
    }

    public void setStrokeWidth(float newStrokeWidth) {
        mPaintOptions.setStrokeWidth(newStrokeWidth);
        if (mIsStrokeWidthBarEnabled) {
            invalidate();
        }
    }

    public Bitmap getPaintBackground() {
        return mBackground;
    }

    public void setBackground(Bitmap background) {
        mBackground = background;
        invalidate();
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        mIsSaving = true;
        draw(canvas);
        mIsSaving = false;
        return bitmap;
    }

    public Bitmap getDrawBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // Xóa màu nền

        // Vẽ phần được vẽ lên
        for (MyPath key : mPaths.keySet()) {
            changePaint(mPaths.get(key));
            canvas.drawPath(key, mBitmapPaint);
        }

        return bitmap;
    }


    public void addPath(MyPath path, PaintOptions options) {
        mPaths.put(path, options);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mTransform.setTranslate(mScrollX + canvas.getClipBounds().centerX(), mScrollY + canvas.getClipBounds().centerY());
        mTransform.postScale(mScale, mScale);

        if (mBackground != null) {
            mBackgroundRect.left = -mBackground.getWidth() / 2f;
            mBackgroundRect.right = mBackground.getWidth() / 2f;
            mBackgroundRect.top = -mBackground.getHeight() / 2f;
            mBackgroundRect.bottom = mBackground.getHeight() / 2f;
            if (mBackground.getHeight() == 1 && mBackground.getWidth() == 1) {
                mBackgroundRect.set(canvas.getClipBounds());
            } else {
                mTransform.mapRect(mBackgroundRect);
            }
            canvas.drawBitmap(mBackground, null, mBackgroundRect, null);
        }
        canvas.setMatrix(mTransform);
        mTransform.invert(mTransform);

        for (MyPath key : mPaths.keySet()) {
            changePaint(mPaths.get(key));
            canvas.drawPath(key, mBitmapPaint);
        }

        changePaint(mPaintOptions);
        canvas.drawPath(mPath, mBitmapPaint);
    }

    private void changePaint(PaintOptions paintOptions) {
        mBitmapPaint.setColor(paintOptions.getColor());
        mBitmapPaint.setStrokeWidth(paintOptions.getStrokeWidth());
    }

    public void clearCanvas() {
        mBackground = null;
        mLastPaths = (LinkedHashMap<MyPath, PaintOptions>) mPaths.clone();
        mPath.reset();
        mPaths.clear();
        invalidate();
    }

    private void actionDown(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        currentX = x;
        currentY = y;
    }

    private void actionMove(float x, float y) {
        mPath.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2);
        currentX = x;
        currentY = y;
    }

    private void actionUp() {
        mPath.lineTo(currentX, currentY);

        // draw a dot on click
        if (firstX == currentX && firstY == currentY) {
            mPath.lineTo(currentX, currentY + 2);
            mPath.lineTo(currentX + 1, currentY + 2);
            mPath.lineTo(currentX + 1, currentY);
        }

        mPaths.put(mPath, mPaintOptions);
        mPath = new MyPath();
        mPaintOptions = new PaintOptions(mPaintOptions.getColor(), mPaintOptions.getStrokeWidth(), mPaintOptions.getAlpha());
    }

    private MotionEvent.PointerCoords getPointerCenter(MotionEvent event) {
        MotionEvent.PointerCoords result = new MotionEvent.PointerCoords();
        MotionEvent.PointerCoords temp = new MotionEvent.PointerCoords();
        for (int i = 0; i < event.getPointerCount(); i++) {
            event.getPointerCoords(i, temp);
            result.x += temp.x;
            result.y += temp.y;
        }

        if (event.getPointerCount() > 0) {
            result.x /= event.getPointerCount();
            result.y /= event.getPointerCount();
        }

        return result;
    }

    private boolean handleScroll(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP
            && event.getAction() != MotionEvent.ACTION_DOWN
            && event.getAction() != MotionEvent.ACTION_POINTER_DOWN
            && event.getAction() != MotionEvent.ACTION_POINTER_UP
            && event.getAction() != MotionEvent.ACTION_MOVE
        ) return false;

        boolean shouldScroll = event.getPointerCount() > 1;
        MotionEvent.PointerCoords center = getPointerCenter(event);

        if (shouldScroll != mIsScrolling) {
            mUndonePaths.clear();
            mPath.reset();
            if (shouldScroll) {
                mIsScrolling = true;
                mScrollOriginX = center.x;
                mScrollOriginY = center.y;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mIsScrolling = false;
            }
            return true;
        }

        if (shouldScroll) {
            mScrollX += (center.x - mScrollOriginX) / mScale;
            mScrollY += (center.y - mScrollOriginY) / mScale;
            mScrollOriginX = center.x;
            mScrollOriginY = center.y;
            invalidate();
        }
        return mIsScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        if (handleScroll(event))
            return true;

        float[] points = new float[]{event.getX(), event.getY()};
        mTransform.mapPoints(points);
        float x = points[0];
        float y = points[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstX = x;
                firstY = y;
                actionDown(x, y);
                mUndonePaths.clear();
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                actionUp();
                break;
        }

        invalidate();
        return true;
    }

}
