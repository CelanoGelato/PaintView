
package com.celano.base.ardrawingsketchandpaint.demo.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.PaintViewCallBack;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.Shapable;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.ShapesInterface;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.ToolInterface;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.UndoCommand;
import com.celano.base.ardrawingsketchandpaint.demo.paint.painttools.Eraser;
import com.celano.base.ardrawingsketchandpaint.demo.paint.painttools.PlainPen;
import com.celano.base.ardrawingsketchandpaint.demo.paint.shapes.Cur;
import com.celano.base.ardrawingsketchandpaint.demo.paint.utils.BitMapUtils;

import java.util.ArrayList;


public class PaintView extends View implements UndoCommand {

    boolean canvasIsCreated = false;

    private Canvas mCanvas = null;

    private ToolInterface mCurrentPainter = null;

    private Bitmap mBitmap = null;

    private Bitmap mOrgBitMap = null;

    private int mBitmapWidth = 0;

    private int mBitmapHeight = 0;

    private int mBackGroundColor = PaintConstants.DEFAULT.BACKGROUND_COLOR;

    private Paint mBitmapPaint = null;

    private PaintPadUndoStack mUndoStack = null;

    private int mPenColor = PaintConstants.DEFAULT.PEN_COLOR;
    ;

    private int mPenSize = PaintConstants.PEN_SIZE.SIZE_1;

    private int mEraserSize = PaintConstants.ERASER_SIZE.SIZE_1;

    int mPaintType = PaintConstants.PEN_TYPE.PLAIN_PEN;

    private PaintViewCallBack mCallBack = null;

    private int mCurrentShapeType = 0;

    private ShapesInterface mCurrentShape = null;

    private Style mStyle = Style.STROKE;

    private boolean isTouchUp = false;

    private int mStackedSize = 50;

    //Scroll
    private boolean mIsScrolling = false;
    private float mScale = 1f;
    private float mScrollingOriginX = 0f;
    private float mScrollingOriginY = 0f;
    private float mScrollX = 0f;
    private float mScrollY = 0f;
    private ScaleGestureDetector mScaleGestureDetector;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
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

    private void init() {
        mCanvas = new Canvas();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mUndoStack = new PaintPadUndoStack(this, mStackedSize);

        mPaintType = PaintConstants.PEN_TYPE.PLAIN_PEN;

        mCurrentShapeType = PaintConstants.SHAP.CURV;
        createNewPen();
    }

    public void setCallBack(PaintViewCallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        isTouchUp = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCanvas.setBitmap(mBitmap);
                createNewPen();
                mCurrentPainter.touchDown(x, y);
                mUndoStack.clearRedo();
                mCallBack.onTouchDown();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentPainter.touchMove(x, y);
                if (mPaintType == PaintConstants.PEN_TYPE.ERASER) {
                    mCurrentPainter.draw(mCanvas);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrentPainter.hasDraw()) {
                    mUndoStack.push(mCurrentPainter);
                    if (mCallBack != null) {

                        mCallBack.onHasDraw();
                    }
                }
                mCurrentPainter.touchUp(x, y);

                mCurrentPainter.draw(mCanvas);
                invalidate();
                isTouchUp = true;
                break;
        }
        return true;
    }


    private void setShape() {
        if (mCurrentPainter instanceof Shapable) {
            switch (mCurrentShapeType) {
                case PaintConstants.SHAP.CURV:
                    mCurrentShape = new Cur((Shapable) mCurrentPainter);
                    break;
                default:
                    break;
            }
            ((Shapable) mCurrentPainter).setShap(mCurrentShape);
        }
    }

    @Override
    public void onDraw(Canvas cv) {
        cv.drawColor(mBackGroundColor);

        cv.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        if (!isTouchUp) {
            if (mPaintType != PaintConstants.PEN_TYPE.ERASER) {
                mCurrentPainter.draw(cv);
            }
        }
    }

    void createNewPen() {
        ToolInterface tool = null;
        switch (mPaintType) {
            case PaintConstants.PEN_TYPE.PLAIN_PEN:
                tool = new PlainPen(mPenSize, mPenColor, mStyle);
                break;
            case PaintConstants.PEN_TYPE.ERASER:
                tool = new Eraser(mEraserSize);
                break;
            default:
                break;
        }
        mCurrentPainter = tool;
        setShape();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!canvasIsCreated) {
            mBitmapWidth = w;
            mBitmapHeight = h;
            creatCanvasBitmap(w, h);
            canvasIsCreated = true;
        }
    }


    public void setForeBitMap(Bitmap bitmap) {
        if (bitmap != null) {
            recycleMBitmap();
            recycleOrgBitmap();
        }
        mBitmap = BitMapUtils.duplicateBitmap(bitmap, getWidth(), getHeight());
        mOrgBitMap = BitMapUtils.duplicateBitmap(mBitmap);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        invalidate();
    }


    private void recycleOrgBitmap() {
        if (mOrgBitMap != null && !mOrgBitMap.isRecycled()) {
            mOrgBitMap.recycle();
            mOrgBitMap = null;
        }
    }

    private void recycleMBitmap() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }


    public Bitmap getSnapShoot() {

        setDrawingCacheEnabled(true);
        buildDrawingCache(true);
        Bitmap bitmap = getDrawingCache(true);
        Bitmap bmp = BitMapUtils.duplicateBitmap(bitmap);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        setDrawingCacheEnabled(false);
        return bmp;
    }

    public void clearAll(boolean clearBackground) {
        if (clearBackground) {
            recycleMBitmap();
            recycleOrgBitmap();
            creatCanvasBitmap(mBitmapWidth, mBitmapHeight);
        } else {
            if (mOrgBitMap != null) {
                mBitmap = BitMapUtils.duplicateBitmap(mOrgBitMap);
                mCanvas.setBitmap(mBitmap);
            } else {
                creatCanvasBitmap(mBitmapWidth, mBitmapHeight);
            }
        }
        mUndoStack.clearAll();
        invalidate();
    }

    private void creatCanvasBitmap(int w, int h) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
    }


    public void setCurrentPainterType(int type) {
        switch (type) {
            case PaintConstants.PEN_TYPE.BLUR:
            case PaintConstants.PEN_TYPE.PLAIN_PEN:
            case PaintConstants.PEN_TYPE.EMBOSS:
            case PaintConstants.PEN_TYPE.ERASER:
                mPaintType = type;
                break;
            default:
                mPaintType = PaintConstants.PEN_TYPE.PLAIN_PEN;
                break;
        }
    }

    public void setCurrentShapType(int type) {
        switch (type) {
            case PaintConstants.SHAP.CURV:
            case PaintConstants.SHAP.LINE:
            case PaintConstants.SHAP.RECT:
            case PaintConstants.SHAP.CIRCLE:
            case PaintConstants.SHAP.OVAL:
            case PaintConstants.SHAP.SQUARE:
            case PaintConstants.SHAP.STAR:
                mCurrentShapeType = type;
                break;
            default:
                mCurrentShapeType = PaintConstants.SHAP.CURV;
                break;
        }
    }


    public int getCurrentPainter() {
        return mPaintType;
    }


    public void setPenSize(int size) {
        mPenSize = size;
    }


    public void setEraserSize(int size) {
        mEraserSize = size;
    }


    public int getPenSize() {
        return mPenSize;
    }

    public void resetState() {
        setCurrentPainterType(PaintConstants.PEN_TYPE.PLAIN_PEN);
        setPenColor(PaintConstants.DEFAULT.PEN_COLOR);
        setBackGroundColor(PaintConstants.DEFAULT.BACKGROUND_COLOR);
        mUndoStack.clearAll();
    }

    public void setBackGroundColor(int color) {
        mBackGroundColor = color;
        invalidate();
    }

    public int getBackGroundColor() {
        return mBackGroundColor;
    }

    public void setPenColor(int color) {
        mPenColor = color;
    }

    public int getPenColor() {
        return mPenColor;
    }


    protected void setTempForeBitmap(Bitmap tempForeBitmap) {
        if (null != tempForeBitmap) {
            recycleMBitmap();
            mBitmap = BitMapUtils.duplicateBitmap(tempForeBitmap);
            if (null != mBitmap && null != mCanvas) {
                mCanvas.setBitmap(mBitmap);
                invalidate();
            }
        }
    }

    public void setPenStyle(Style style) {
        mStyle = style;
    }

    public byte[] getBitmapArr() {
        return BitMapUtils.bitampToByteArray(mBitmap);
    }

    @Override
    public void undo() {
        if (null != mUndoStack) {
            mUndoStack.undo();
        }
    }

    @Override
    public void redo() {
        if (null != mUndoStack) {
            mUndoStack.redo();
        }
    }

    @Override
    public boolean canUndo() {
        return mUndoStack.canUndo();
    }

    @Override
    public boolean canRedo() {
        return mUndoStack.canRedo();
    }

    @NonNull
    @Override
    public String toString() {
        return "mPaint" + mCurrentPainter + mUndoStack;
    }


    public class PaintPadUndoStack {
        private final int mStackSize;

        private final PaintView mPaintView;

        private final ArrayList<ToolInterface> mUndoStack = new ArrayList<>();

        private final ArrayList<ToolInterface> mRedoStack = new ArrayList<>();

        private final ArrayList<ToolInterface> mOldActionStack = new ArrayList<>();

        public PaintPadUndoStack(PaintView paintView, int stackSize) {
            mPaintView = paintView;
            mStackSize = stackSize;
        }


        public void push(ToolInterface penTool) {
            if (null != penTool) {

                if (mUndoStack.size() == mStackSize && mStackSize > 0) {
                    ToolInterface removedTool = mUndoStack.get(0);
                    mOldActionStack.add(removedTool);
                    mUndoStack.remove(0);
                }

                mUndoStack.add(penTool);
            }
        }

        public void clearAll() {
            mRedoStack.clear();
            mUndoStack.clear();
            mOldActionStack.clear();
        }

        public void undo() {
            if (canUndo() && null != mPaintView) {
                ToolInterface removedTool = mUndoStack.get(mUndoStack.size() - 1);
                mRedoStack.add(removedTool);
                mUndoStack.remove(mUndoStack.size() - 1);

                if (null != mOrgBitMap) {

                    mPaintView.setTempForeBitmap(mPaintView.mOrgBitMap);
                } else {
                    mPaintView.creatCanvasBitmap(mPaintView.mBitmapWidth, mPaintView.mBitmapHeight);
                }

                Canvas canvas = mPaintView.mCanvas;

                // First draw the removed tools from undo stack.
                for (ToolInterface paintTool : mOldActionStack) {
                    paintTool.draw(canvas);
                }

                for (ToolInterface paintTool : mUndoStack) {
                    paintTool.draw(canvas);
                }

                mPaintView.invalidate();
            }
        }

        public void redo() {
            if (canRedo() && null != mPaintView) {
                ToolInterface removedTool = mRedoStack.get(mRedoStack.size() - 1);
                mUndoStack.add(removedTool);
                mRedoStack.remove(mRedoStack.size() - 1);

                if (null != mOrgBitMap) {
                    mPaintView.setTempForeBitmap(mPaintView.mOrgBitMap);
                } else {
                    mPaintView.creatCanvasBitmap(mPaintView.mBitmapWidth, mPaintView.mBitmapHeight);
                }

                Canvas canvas = mPaintView.mCanvas;
                for (ToolInterface sketchPadTool : mOldActionStack) {
                    sketchPadTool.draw(canvas);
                }
                for (ToolInterface sketchPadTool : mUndoStack) {
                    sketchPadTool.draw(canvas);
                }

                mPaintView.invalidate();
            }
        }

        public boolean canUndo() {
            return (mUndoStack.size() > 0);
        }

        public boolean canRedo() {
            return (mRedoStack.size() > 0);
        }

        public void clearRedo() {
            mRedoStack.clear();
        }

        @NonNull
        @Override
        public String toString() {
            return "canUndo" + canUndo();
        }
    }


}
