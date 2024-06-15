package com.celano.base.ardrawingsketchandpaint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.celano.base.ardrawingsketchandpaint.demo.paint.PaintConstants;
import com.celano.base.ardrawingsketchandpaint.demo.paint.PaintView;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.PaintViewCallBack;
import com.celano.base.ardrawingsketchandpaint.demo.paint.view.CircleView;

public class PaintActivity extends AppCompatActivity {


    private PaintView mPaintView;
    private ImageButton btnPencil, btnErasor, btnUndo, btnRedo, btnClear, btnSetWidthShape;

    private View toolbar;
    private boolean toolbarOpen;

    private CircleView preview;
    private SeekBar seekBarWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        mPaintView = (PaintView) findViewById(R.id.paintView);

        btnPencil = (ImageButton) findViewById(R.id.btnPencil);
        btnErasor = (ImageButton) findViewById(R.id.btnErasor);
        btnUndo = (ImageButton) findViewById(R.id.btnUndo);
        btnRedo = (ImageButton) findViewById(R.id.btnRedo);
        btnClear = (ImageButton) findViewById(R.id.btnClear);
        btnSetWidthShape = (ImageButton) findViewById(R.id.btnSetWidthShape);
        preview = findViewById(R.id.circle_view_preview);
        seekBarWidth = findViewById(R.id.seekBar_width);

        toolbar = findViewById(R.id.draw_tools);
        toolbarOpen = false;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.image);
        mPaintView.setBackgroundImage(bitmap);

        btnPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonPencil();
            }
        });
        btnErasor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonEraser();
            }
        });
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonUndo();
            }
        });
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonRedo();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaintView.clearAll(false);
            }
        });
        btnSetWidthShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleToolbar();
            }
        });

        seekBarWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPaintView.setPenSize(progress);
                preview.setCircleRadius((float) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        initCallBack();
    }

    private void toggleToolbar() {
        toolbar.animate().translationY((toolbarOpen ? 56 : 0) * getResources().getDisplayMetrics().density);
        toolbarOpen = !toolbarOpen;
    }

    private void onClickButtonPencil() {
        mPaintView.setCurrentPainterType(PaintConstants.PEN_TYPE.PLAIN_PEN);
    }

    private void onClickButtonEraser() {
        mPaintView.setCurrentPainterType(PaintConstants.PEN_TYPE.ERASER);
    }

    private void onClickButtonRedo() {
        mPaintView.redo();
        upDateUndoRedo();
    }

    /**
     * undo
     */
    private void onClickButtonUndo() {
        mPaintView.undo();
        upDateUndoRedo();
    }

    private void upDateUndoRedo() {
        if (mPaintView.canUndo()) {
            enableUndoButton();
        } else {
            disableUndoButton();
        }
        if (mPaintView.canRedo()) {
            enableRedoButton();
        } else {
            disableRedoButton();
        }
    }

    private void initCallBack() {
        mPaintView.setCallBack(new PaintViewCallBack() {
            @Override
            public void onHasDraw() {
                enableUndoButton();
                disableRedoButton();
            }

            @Override
            public void onTouchDown() {
            }
        });
    }

    private void disableRedoButton() {

    }

    private void enableRedoButton() {

    }

    private void disableUndoButton() {

    }

    private void enableUndoButton() {
    }
}