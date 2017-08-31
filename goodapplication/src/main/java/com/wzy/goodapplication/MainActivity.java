package com.wzy.goodapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jakewharton.scalpel.ScalpelFrameLayout;

public class MainActivity extends AppCompatActivity {

    private ScalpelFrameLayout scFrameTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        scFrameTest = (ScalpelFrameLayout) findViewById(R.id.scFrameTest);
        scFrameTest.setLayerInteractionEnabled(true);
        scFrameTest.setDrawViews(true);
        scFrameTest.setDrawIds(true);

    }
}
