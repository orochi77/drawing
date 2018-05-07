package com.tangbba.drawapp.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import com.tangbba.drawapp.R;

public class ArcView extends View {

    private static final String TAG = "ArcView";

    private static final int DEFAULT_START_ANGLE = 135;
    private static final int DEFAULT_SWEEP_ANGLE = 270;
    private static final int DEFAULT_COLOR = 0xffff0000;
    @Px
    private static final int DEFAULT_STROKE_WIDTH = 40;

    private int mStartAngle;
    private int mSweepAngle;
    private int mDestinationSweepAngle;
    @Px
    private int mStrokeWidth;
    private int mColor;
    private boolean mUseAnimation;
    private boolean mAutoAnimation;

    private Paint mArcPaint;
    private RectF mArcOvalRect;

    private ValueAnimator mSweepAngleAnimator;

    public ArcView(Context context) {
        this(context, null);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutListener();
        initializeView();
        setAttributes(attrs);
    }

    private void initializeView() {
        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mArcOvalRect = new RectF();
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ArcView);

        try {
            mStartAngle = typedArray.getInt(R.styleable.ArcView_startAngle, DEFAULT_START_ANGLE);
            mSweepAngle = typedArray.getInt(R.styleable.ArcView_sweepAngle, DEFAULT_SWEEP_ANGLE);
            mDestinationSweepAngle = typedArray.getInt(R.styleable.ArcView_destinationSweepAngle, DEFAULT_SWEEP_ANGLE);
            mUseAnimation = typedArray.getBoolean(R.styleable.ArcView_useAnimation, false);
            mAutoAnimation = typedArray.getBoolean(R.styleable.ArcView_autoAnimation, false);
            mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.ArcView_strokeWidth, DEFAULT_STROKE_WIDTH);
            mColor = typedArray.getColor(R.styleable.ArcView_arcColor, 0);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            typedArray.recycle();
        }
    }

    private void preDraw() {
        int halfStrokeSize = mStrokeWidth / 2;
        int circleSize = Math.min(getWidth(), getHeight()) - halfStrokeSize;

        mArcPaint.setStrokeWidth(mStrokeWidth);
        mArcPaint.setColor(mColor);

        mArcOvalRect.left = halfStrokeSize;
        mArcOvalRect.top = halfStrokeSize;
        mArcOvalRect.right = circleSize;
        mArcOvalRect.bottom = circleSize;

        if (mUseAnimation && mAutoAnimation) {
            startAnimation();
        }
    }

    private void setLayoutListener() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 그림을 그리기 위한 정보들을 계산한다.
                preDraw();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.drawArc(mArcOvalRect, mStartAngle, mSweepAngle, false, mArcPaint);
        canvas.restore();
    }

    public void setSweepAngle(int sweepAngle) {
        mSweepAngle = sweepAngle;
        invalidate();
    }

    public void setDestinationSweepAngle(int destinationSweepAngle) {
        mDestinationSweepAngle = destinationSweepAngle;
        invalidate();
    }

    public void startAnimation() {
        if (mSweepAngleAnimator != null) {
            mSweepAngleAnimator.removeAllUpdateListeners();
        }
        mSweepAngleAnimator = ValueAnimator.ofInt(0, mDestinationSweepAngle);
        mSweepAngleAnimator.setDuration(500);
        mSweepAngleAnimator.setInterpolator(new DecelerateInterpolator());
        mSweepAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                setSweepAngle(animatedValue);
            }
        });
        mSweepAngleAnimator.start();
    }

}
