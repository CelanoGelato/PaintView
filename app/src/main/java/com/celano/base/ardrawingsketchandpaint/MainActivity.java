package com.celano.base.ardrawingsketchandpaint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnDrawingActivity).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DrawingActivity.class)));

        findViewById(R.id.btnCameraActivity).setOnClickListener(v -> {
//                startActivity(new Intent(MainActivity.this, DrawingActivity.class));
        });
    }
}