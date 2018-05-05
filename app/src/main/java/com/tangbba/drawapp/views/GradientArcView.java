package com.tangbba.drawapp.views;

import android.animation.AnimatorListenerAdapter;
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
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.tangbba.drawapp.R;

public class GradientArcView extends View {

    private static final String TAG = "ArcView";

    private static final int DEFAULT_START_ANGLE = 135;
    private static final int DEFAULT_SWEEP_ANGLE = 270;
    @Px
    private static final int DEFAULT_STROKE_WIDTH = 40;
    private static final int[] DEFAULT_COLORS = {Color.BLACK, Color.BLACK};
    private static final float[] DEFAULT_POSITIONS = {0.0f, 1.0f};

    private int mStartAngle;
    private int mSweepAngle = 0;
    private int mDestinationSweepAngle;
    @Px
    private int mStrokeWidth;
    private boolean mUseAnimation;
    private boolean mAutoAnimation;
    private int[] mColors;
    private float[] mPositions;

    private Paint mArcPaint;
    private RectF mArcOvalRect;
    private LinearGradient mGradient;

    private ValueAnimator mSweepAngleAnimator;

    public GradientArcView(Context context) {
        this(context, null);
    }

    public GradientArcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.GradientArcView);

        try {
            mStartAngle = typedArray.getInt(R.styleable.GradientArcView_startAngle, DEFAULT_START_ANGLE);
            mSweepAngle = typedArray.getInt(R.styleable.GradientArcView_sweepAngle, DEFAULT_SWEEP_ANGLE);
            mDestinationSweepAngle = typedArray.getInt(R.styleable.GradientArcView_destinationSweepAngle, DEFAULT_SWEEP_ANGLE);
            mUseAnimation = typedArray.getBoolean(R.styleable.GradientArcView_useAnimation, false);
            mAutoAnimation = typedArray.getBoolean(R.styleable.GradientArcView_autoAnimation, false);
            mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.GradientArcView_strokeWidth, DEFAULT_STROKE_WIDTH);

            int colorsId = typedArray.getResourceId(R.styleable.GradientArcView_gradientColors, 0);
            if (colorsId != 0) {
                TypedArray colors = getResources().obtainTypedArray(colorsId);
                mColors = new int[colors.length()];
                for (int i = 0; i < colors.length(); i++) {
                    mColors[i] = colors.getColor(i, 0);
                }
                colors.recycle();
            } else {
                mColors = DEFAULT_COLORS;
            }

            int positionsId = typedArray.getResourceId(R.styleable.GradientArcView_gradientColorPositions, 0);
            if (positionsId != 0) {
                TypedArray positions = getResources().obtainTypedArray(positionsId);
                mPositions = new float[positions.length()];
                for (int i = 0; i < positions.length(); i++) {
                    mPositions[i] = positions.getFloat(i, 0.0f);
                }
                positions.recycle();
            } else {
                mPositions = DEFAULT_POSITIONS;
            }
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

        mArcOvalRect.left = halfStrokeSize;
        mArcOvalRect.top = halfStrokeSize;
        mArcOvalRect.right = circleSize;
        mArcOvalRect.bottom = circleSize;

        mGradient = new LinearGradient(0, 0, getWidth(), 0, mColors, mPositions, Shader.TileMode.CLAMP);

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

        mArcPaint.setShader(mGradient);
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
