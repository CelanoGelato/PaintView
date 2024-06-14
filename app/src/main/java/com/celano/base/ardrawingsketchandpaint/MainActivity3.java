package com.celano.base.ardrawingsketchandpaint;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.celano.base.ardrawingsketchandpaint.R;
import com.celano.base.ardrawingsketchandpaint.demo.paint.PaintConstants;
import com.celano.base.ardrawingsketchandpaint.demo.paint.PaintView;
import com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces.PaintViewCallBack;

public class MainActivity3 extends AppCompatActivity {


    private PaintView mPaintView;
    private Button btnPencil, btnErasor, btnUndo, btnRedo, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        mPaintView = (PaintView) findViewById(R.id.paintView);
//        mPaintView.setBackgroundResource(R.drawable.z);

        btnPencil = (Button) findViewById(R.id.btnPencil);
        btnErasor = (Button) findViewById(R.id.btnErasor);
        btnUndo = (Button) findViewById(R.id.btnUndo);
        btnRedo = (Button) findViewById(R.id.btnRedo);
        btnClear = (Button) findViewById(R.id.btnClear);

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

        initCallBack();
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
            // 当画了之后对Button进行更新
            @Override
            public void onHasDraw() {
                enableUndoButton();
                disableRedoButton();
            }

            // 当点击之后让各个弹出的窗口都消失
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