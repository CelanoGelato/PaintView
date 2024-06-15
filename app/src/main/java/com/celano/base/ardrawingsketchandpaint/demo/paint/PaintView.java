package com.celano.base.ardrawingsketchandpaint.demo.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.PaintViewCallBack;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.ShapeAble;
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

    private float mScale = 1.0f;
    private float mScrollX = 0;
    private float mScrollY = 0;
    private boolean mIsScrolling = false;
    private float mScrollOriginX, mScrollOriginY;
    private final Matrix mTransform = new Matrix();
    private ScaleGestureDetector mScaleGestureDetector;

    private Bitmap mBackgroundImage = null; // Ảnh nền
    private final Matrix mBackgroundMatrix = new Matrix(); // Ma trận để biến đổi ảnh nền
    private RectF mDrawingRect; // Ma trận để biến đổi ảnh nền

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
    }

    private void init() {
        mCanvas = new Canvas();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mUndoStack = new PaintPadUndoStack(this, PaintConstants.STACKED_SIZE);

        mPaintType = PaintConstants.PEN_TYPE.PLAIN_PEN;

        mCurrentShapeType = PaintConstants.SHAPE.CUR;
        createNewPen();

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float oldScale = mScale;
                mScale *= detector.getScaleFactor();
                mScale = Math.max(1f, Math.min(mScale, 3.0f));  // Giới hạn scale từ 0.5 đến 3.0
                mScrollX += detector.getFocusX() * (oldScale - mScale) / mScale;
                mScrollY += detector.getFocusY() * (oldScale - mScale) / mScale;
                invalidate();
                return true;
            }
        });
    }

    public void setBackgroundImage(Bitmap backgroundImage) {
        mBackgroundImage = backgroundImage;

        if (mBackgroundImage != null) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            int imageWidth = mBackgroundImage.getWidth();
            int imageHeight = mBackgroundImage.getHeight();

            float scale = Math.min((float) viewWidth / imageWidth, (float) viewHeight / imageHeight);

            float dx = (viewWidth - imageWidth * scale) / 2;
            float dy = (viewHeight - imageHeight * scale) / 2;

            mBackgroundMatrix.setScale(scale, scale);
            mBackgroundMatrix.postTranslate(dx, dy);

            // Tạo Rect xác định vùng vẽ
            float left = dx;
            float top = dy;
            float right = left + imageWidth * scale;
            float bottom = top + imageHeight * scale;

            mDrawingRect = new RectF(left, top, right, bottom);
        }

        invalidate();
    }

    public void setCallBack(PaintViewCallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);

        // Xử lý các sự kiện chạm cho zoom và di chuyển
        if (handleScroll(event)) return true;

        float[] transformedPoints = applyInverseTransform(event.getX(), event.getY());
        float x = transformedPoints[0];
        float y = transformedPoints[1];

        if (!mDrawingRect.contains(x, y)) {
            return true;
        }

        isTouchUp = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCanvas.setBitmap(mBitmap);
                createNewPen();
                mCurrentPainter.actionDown(x, y);
                mUndoStack.clearRedo();
                mCallBack.onTouchDown();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentPainter.actionMove(x, y);
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
                mCurrentPainter.actionUp(x, y);

                mCurrentPainter.draw(mCanvas);
                invalidate();
                isTouchUp = true;
                break;
        }
        return true;
    }

    private float[] applyInverseTransform(float x, float y) {
        float[] points = new float[]{x, y};
        Matrix inverse = new Matrix();
        mTransform.invert(inverse);
        inverse.mapPoints(points);
        return points;
    }

    private boolean handleScroll(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_POINTER_DOWN && event.getAction() != MotionEvent.ACTION_POINTER_UP && event.getAction() != MotionEvent.ACTION_MOVE)
            return false;

        boolean shouldScroll = event.getPointerCount() > 1;
        MotionEvent.PointerCoords center = getPointerCenter(event);

        if (shouldScroll != mIsScrolling) {
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

    private MotionEvent.PointerCoords getPointerCenter(MotionEvent event) {
        MotionEvent.PointerCoords result = new MotionEvent.PointerCoords();
        MotionEvent.PointerCoords temp = new MotionEvent.PointerCoords();
        for (int i = 0; i < event.getPointerCount(); i++) {
            event.getPointerCoords(i, temp);
            result.x += temp.x;
            result.y += temp.y;
        }
        result.x /= event.getPointerCount();
        result.y /= event.getPointerCount();
        return result;
    }

    private void setShape() {
        if (mCurrentPainter instanceof ShapeAble) {
            switch (mCurrentShapeType) {
                case PaintConstants.SHAPE.CUR:
                    mCurrentShape = new Cur((ShapeAble) mCurrentPainter);
                    break;
                default:
                    break;
            }
            ((ShapeAble) mCurrentPainter).setShape(mCurrentShape);
        }
    }

    @Override
    protected void onDraw(Canvas cv) {
        super.onDraw(cv);

        // Lưu trạng thái hiện tại của canvas
        cv.save();

        // Thiết lập ma trận biến đổi cho zoom và di chuyển
        mTransform.setTranslate(mScrollX + getWidth() / 2f, mScrollY + getHeight() / 2f);
        mTransform.postScale(mScale, mScale);
        mTransform.postTranslate(-getWidth() / 2f, -getHeight() / 2f);
        cv.concat(mTransform);

        // Vẽ màu nền
        cv.drawColor(mBackGroundColor);

        // Vẽ ảnh nền đã điều chỉnh vị trí và tỉ lệ
        if (mBackgroundImage != null) {
            cv.drawBitmap(mBackgroundImage, mBackgroundMatrix, mBitmapPaint);
        }

        // Chỉ vẽ trong vùng vẽ hợp lệ
        if (mBitmap != null && mDrawingRect != null) {
            cv.save();
            cv.clipRect(mDrawingRect);
            cv.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            cv.restore();
        }

        // Vẽ bitmap lên canvas
        cv.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        // Vẽ các thành phần khác nếu cần
        if (!isTouchUp) {
            if (mPaintType != PaintConstants.PEN_TYPE.ERASER) {
                mCurrentPainter.draw(cv);
            }
        }

        // Khôi phục trạng thái canvas
        cv.restore();
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
        if (mBackgroundImage != null) {
            setBackgroundImage(mBackgroundImage);
        }

        if (!canvasIsCreated) {
            mBitmapWidth = w;
            mBitmapHeight = h;
            createCanvasBitmap(w, h);
            canvasIsCreated = true;
        }
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
            createCanvasBitmap(mBitmapWidth, mBitmapHeight);
        } else {
            if (mOrgBitMap != null) {
                mBitmap = BitMapUtils.duplicateBitmap(mOrgBitMap);
                mCanvas.setBitmap(mBitmap);
            } else {
                createCanvasBitmap(mBitmapWidth, mBitmapHeight);
            }
        }
        mUndoStack.clearAll();
        invalidate();
    }

    private void createCanvasBitmap(int w, int h) {
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

    public void setCurrentShapeType(int type) {
        switch (type) {
            case PaintConstants.SHAPE.CUR:
            case PaintConstants.SHAPE.LINE:
            case PaintConstants.SHAPE.RECT:
            case PaintConstants.SHAPE.CIRCLE:
            case PaintConstants.SHAPE.OVAL:
            case PaintConstants.SHAPE.SQUARE:
            case PaintConstants.SHAPE.STAR:
                mCurrentShapeType = type;
                break;
            default:
                mCurrentShapeType = PaintConstants.SHAPE.CUR;
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
                    mPaintView.createCanvasBitmap(mPaintView.mBitmapWidth, mPaintView.mBitmapHeight);
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
                    mPaintView.createCanvasBitmap(mPaintView.mBitmapWidth, mPaintView.mBitmapHeight);
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
