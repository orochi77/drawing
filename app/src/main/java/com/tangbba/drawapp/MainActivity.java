package com.tangbba.drawapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.tangbba.drawapp.views.GradientArcView;

public class MainActivity extends AppCompatActivity {

    private GradientArcView mGradientArcView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGradientArcView = (GradientArcView) findViewById(R.id.gradient_arc_view);
        mGradientArcView.setDestinationSweepAngle(270);

        findViewById(R.id.gradient_arc_view_animation_start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGradientArcView.startAnimation();
            }
        });
    }
}
