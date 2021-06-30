/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import com.zml.frescophotoview.gestures.TransformGestureDetector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ZoomableControllerImp extends ABSZoomableController {

    private final float[] mStartValues = new float[9];
    private final float[] mStopValues = new float[9];
    private final float[] mCurrentValues = new float[9];
    private final Matrix mNewTransform = new Matrix();
    private final Matrix mAnimeWorkingTransform = new Matrix();
    private final Matrix mPreviousTempTransform = new Matrix();
    private final Matrix mFlingStartTempTransform = new Matrix();
    private ScaleFactorRetriever scaleFactorRetriever;

    private boolean mHorizontalNestedScrollEnabled = false;
    private final ValueAnimator mValueAnimator;

    public static final long DEFAULT_ANIMATION_DURATION = 400;
    private long animationDuration = DEFAULT_ANIMATION_DURATION;

    private Scroller mScroller;
    private Context mContext;

    @IntDef(
            flag = true,
            value = {LIMIT_NONE, LIMIT_TRANSLATION_X, LIMIT_TRANSLATION_Y, LIMIT_SCALE, LIMIT_ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LimitFlag {
    }

    public static final int LIMIT_NONE = 0;
    public static final int LIMIT_TRANSLATION_X = 1;
    public static final int LIMIT_TRANSLATION_Y = 1 << 1;
    public static final int LIMIT_SCALE = 1 << 2;
    public static final int LIMIT_ALL = LIMIT_TRANSLATION_X | LIMIT_TRANSLATION_Y | LIMIT_SCALE;

    public ZoomableControllerImp(TransformGestureDetector transformGestureDetector, Context context) {
        super(transformGestureDetector);
        mValueAnimator = ValueAnimator.ofFloat(0, 1);
        mValueAnimator.setInterpolator(new DecelerateInterpolator(1.2f));
        mContext = context;
        mScroller = new Scroller(mContext);
    }

    @Override
    public void reset() {
        stopAnimatingAndScrolling();
        mAnimeWorkingTransform.reset();
        mNewTransform.reset();
        super.reset();
    }

    protected void updatePreviousMatrix() {
        mPreviousTempTransform.set(getActiveTransform());
        getDetector().resetGestureStart();
        mRetainedScale = 1f;
    }

    protected void setHorizontalNestedScrollEnabled(boolean enabled) {
        mHorizontalNestedScrollEnabled = enabled;
    }

    /**
     * Returns true if the zoomable transform is identity matrix, and the controller is idle.
     */
    @Override
    public boolean isIdentity() {
        return !isAnimatingOrScrolling() && super.isIdentity();
    }

    public void zoomToPoint(
            float scale,
            PointF viewPoint,
            @LimitFlag int limitFlags,
            long durationMs,
            @Nullable Runnable completeListener,
            @Nullable ValueAnimator.AnimatorUpdateListener updateListener) {
        if (isAnimating()) {
            stopAnimating();
        }
        PointF imagePoint = mapViewToImage(viewPoint);
        calculateZoomToPointTransform(mNewTransform, scale, imagePoint, viewPoint, limitFlags);
        setTransform(mNewTransform, durationMs, completeListener, updateListener);
    }

    public void fling(int velocityX, int velocityY) {
        if (needSpringBack()) return;
        if (isScrolling()) {
            stopScrolling();
        }
        mScroller.fling(0, 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        mFlingStartTempTransform.set(getActiveTransform());
        setTransform(getActiveTransform());
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();
            mNewTransform.set(mFlingStartTempTransform);
            calculateTranslateToPointTransform(mNewTransform, currX, currY, LIMIT_TRANSLATION_X|LIMIT_TRANSLATION_Y);
            setTransform(mNewTransform);
        }
    }

    public void setTransform(
            Matrix newTransform, long durationMs, @Nullable Runnable onAnimationComplete, @Nullable ValueAnimator.AnimatorUpdateListener listener) {
        if (durationMs <= 0) {
            setTransformImmediate(newTransform);
        } else {
            setTransformAnimated(newTransform, durationMs, onAnimationComplete, listener);
        }
    }

    private void setTransformImmediate(final Matrix newTransform) {
        if (isAnimating()) {
            stopAnimating();
        }
        mAnimeWorkingTransform.set(newTransform);
        super.setTransform(newTransform);
    }

    public void setTransformAnimated(
            final Matrix newTransform, long durationMs, @Nullable final Runnable completeListener,
            final ValueAnimator.AnimatorUpdateListener updateListener) {
        if (isAnimating()) {
            stopAnimating();
        }
        mValueAnimator.setDuration(durationMs);
        getActiveTransform().getValues(mStartValues);
        newTransform.getValues(mStopValues);
        mValueAnimator.addUpdateListener(
                valueAnimator -> {
                    calculateInterpolation(mAnimeWorkingTransform, (float) valueAnimator.getAnimatedValue());
                    setTransform(mAnimeWorkingTransform);
                    if (updateListener != null) {
                        updateListener.onAnimationUpdate(valueAnimator);
                    }
                });
        mValueAnimator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationStopped();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onAnimationStopped();
                    }

                    private void onAnimationStopped() {
                        getDetector().resetTranslate();
                        //将动画结束后的状态更新到初始矩阵
                        updatePreviousMatrix();
                        mValueAnimator.removeAllUpdateListeners();
                        mValueAnimator.removeAllListeners();
                        if (completeListener != null) {
                            completeListener.run();
                        }
                    }
                });
        mValueAnimator.start();
    }

    protected boolean isAnimatingOrScrolling() {
        return mValueAnimator.isRunning() || mScroller.computeScrollOffset();
    }

    private boolean mDisableGesture = false;

    public void setDisableGesture(boolean disable) {
        mDisableGesture = disable;
    }

    public boolean isDisableGesture() {
        return mDisableGesture;
    }

    @Override
    public void onGestureBegin(TransformGestureDetector detector) {
        if (mDisableGesture) return;
        mRetainedScale = 1f;
        stopAnimatingAndScrolling();
        mPreviousTempTransform.set(getActiveTransform());
        mPreviousPointerCount = detector.getPointerCount();
    }

    //记录维持双指缩放后又放开，之前的缩放大小
    private float mRetainedScale = 1f;
    private int mPreviousPointerCount = 0;

    @Override
    public void onGestureUpdate(final TransformGestureDetector detector) {
        if (mDisableGesture) return;
        if (isAnimatingOrScrolling()) {
            return;
        }
        //在缩放时超过最大限制时松开手指,回弹
        if (detector.getPointerCount() == 1 && mPreviousPointerCount == 2) {
            trySpringBack(null, null);
        } else {
            mPreviousPointerCount = detector.getPointerCount();
            mRetainedScale = detector.getScale();
        }
        calculateGestureTransform(getActiveTransform());
        onTransformChanged();
    }

    public boolean isImageOutTopEdge() {
        return getViewBounds().top - getTransformedImageBounds().top > EPS;
    }

    @Override
    public void onGestureEnd(TransformGestureDetector detector) {
        if (mDisableGesture) {
            return;
        }
        if (needSpringBack()) {
            setDisableGesture(true);
            trySpringBack(enableGestureWhenCompleted, null);
        }
    }

    protected boolean calculateGestureTransform(Matrix outTransform) {
        TransformGestureDetector detector = getDetector();
        outTransform.set(mPreviousTempTransform);
        if (isScaleEnabled()) {
            float previousScale = getMatrixScaleFactor(mPreviousTempTransform);
            float scale = mRetainedScale;
            //大于或者小于Scale后，形成一定缩放阻尼
            if (previousScale * scale < getMinScaleFactor()) {
                float thresholdScale = getMinScaleFactor() / previousScale;
                scale = Utils.calculateDamping(scale, thresholdScale, 0.02f);
            } else if (previousScale * scale > getMaxScaleFactor()) {
                float thresholdScale = getMaxScaleFactor() / previousScale;
                scale = Utils.calculateDamping(scale, thresholdScale, 0.02f);
            }
            outTransform.postScale(scale, scale, detector.getPivotX(), detector.getPivotY());
        }
        if (isTranslationEnabled()) {
            float translateX = detector.getTranslationX();
            float translateY = detector.getTranslationY();
            outTransform.postTranslate(translateX, translateY);
        }
        limitTranslation(outTransform, LIMIT_TRANSLATION_X);
        limitTranslation(outTransform, LIMIT_TRANSLATION_Y);
        return true;
    }

    protected boolean calculateZoomToPointTransform(
            Matrix outTransform,
            float scale,
            PointF imagePoint,
            PointF viewPoint,
            @LimitFlag int limitFlags) {
        float[] viewAbsolute = mTempValues;
        viewAbsolute[0] = imagePoint.x;
        viewAbsolute[1] = imagePoint.y;
        mapRelativeToAbsolute(viewAbsolute, viewAbsolute, 1);
        float distanceX = viewPoint.x - viewAbsolute[0];
        float distanceY = viewPoint.y - viewAbsolute[1];
        boolean transformCorrected = false;
        outTransform.setScale(scale, scale, viewAbsolute[0], viewAbsolute[1]);
        transformCorrected |= limitScale(outTransform, viewAbsolute[0], viewAbsolute[1], limitFlags);
        outTransform.postTranslate(distanceX, distanceY);
        transformCorrected |= limitTranslation(outTransform, limitFlags);
        return transformCorrected;
    }


    protected boolean calculateTranslateToPointTransform(
            Matrix outTransform,
            float x,
            float y,
            @LimitFlag int limitFlags) {
        boolean transformCorrected = false;
        outTransform.postTranslate(x, y);
        transformCorrected |= limitTranslation(outTransform, limitFlags);
        return transformCorrected;
    }

    protected boolean limitScale(
            Matrix transform, float pivotX, float pivotY, @LimitFlag int limitTypes) {
        if (!shouldLimit(limitTypes, LIMIT_SCALE)) {
            return false;
        }
        float currentScale = getMatrixScaleFactor(transform);
        float targetScale = limit(currentScale, getMinScaleFactor(), getMaxScaleFactor());
        if (targetScale != currentScale) {
            float scale = targetScale / currentScale;
            transform.postScale(scale, scale, pivotX, pivotY);
            return true;
        }
        return false;
    }

    private final RectF mTempRect = new RectF();

    protected boolean limitTranslation(Matrix transform, @LimitFlag int limitTypes) {
        if (!shouldLimit(limitTypes, LIMIT_TRANSLATION_X | LIMIT_TRANSLATION_Y)) {
            return false;
        }
        RectF b = mTempRect;
        RectF imageBounds = getImageBounds();
        RectF viewBounds = getViewBounds();
        b.set(imageBounds);
        transform.mapRect(b);
        float offsetLeft =
                shouldLimit(limitTypes, LIMIT_TRANSLATION_X)
                        ? getOffset(
                        b.left, b.right, viewBounds.left, viewBounds.right, imageBounds.centerX())
                        : 0;
        float offsetTop =
                shouldLimit(limitTypes, LIMIT_TRANSLATION_Y)
                        ? getOffset(
                        b.top, b.bottom, viewBounds.top, viewBounds.bottom, imageBounds.centerY())
                        : 0;
        if (offsetLeft != 0 || offsetTop != 0) {
            transform.postTranslate(offsetLeft, offsetTop);
            return true;
        }
        return false;
    }

    /**
     * Returns the offset necessary to make sure that: - the image is centered within the limit if the
     * image is smaller than the limit - there is no empty space on left/right if the image is bigger
     * than the limit
     */
    protected float getOffset(
            float imageStart, float imageEnd, float limitStart, float limitEnd, float limitCenter) {
        float imageWidth = imageEnd - imageStart, limitWidth = limitEnd - limitStart;
        float limitInnerWidth = Math.min(limitCenter - limitStart, limitEnd - limitCenter) * 2;
        // center if smaller than limitInnerWidth
        if (imageWidth < limitInnerWidth) {
            return limitCenter - (imageEnd + imageStart) / 2;
        }
        // to the edge if in between and limitCenter is not (limitLeft + limitRight) / 2
        if (imageWidth < limitWidth) {
            if (limitCenter < (limitStart + limitEnd) / 2) {
                return limitStart - imageStart;
            } else {
                return limitEnd - imageEnd;
            }
        }
        // to the edge if larger than limitWidth and empty space visible
        if (imageStart > limitStart) {
            return limitStart - imageStart;
        }
        if (imageEnd < limitEnd) {
            return limitEnd - imageEnd;
        }
        return 0;
    }

    private float limit(float value, float min, float max) {
        return Math.min(Math.max(min, value), max);
    }

    private static boolean shouldLimit(@LimitFlag int limits, @LimitFlag int flag) {
        return (limits & flag) != LIMIT_NONE;
    }

    public boolean needSpringBack() {
        return isOutOfMinScale() || isOutOfMaxScale();
    }

    public boolean isOutOfMaxScale() {
        return getScaleFactor() > getMaxScaleFactor();
    }

    public boolean isOutOfMinScale() {
        return getScaleFactor() < getMinScaleFactor();
    }

    private Runnable enableGestureWhenCompleted = new Runnable() {
        @Override
        public void run() {
            setDisableGesture(false);
        }
    };

    public boolean trySpringBack(@Nullable Runnable completeListener, @Nullable ValueAnimator.AnimatorUpdateListener updateListener) {
        RectF viewBounds = getViewBounds();
        PointF vp = new PointF(viewBounds.centerX(), viewBounds.centerY());
        if (isOutOfMinScale()) {
            zoomToPoint(getMinScaleFactor(), vp, LIMIT_ALL, animationDuration, completeListener, updateListener);
            return true;
        }
        if (isOutOfMaxScale()) {
            zoomToPoint(getMaxScaleFactor(), vp, LIMIT_ALL, animationDuration, completeListener, updateListener);
            return true;
        }
        return false;
    }

    protected void calculateInterpolation(Matrix outMatrix, float fraction) {
        for (int i = 0; i < 9; i++) {
            mCurrentValues[i] = (1 - fraction) * mStartValues[i] + fraction * mStopValues[i];
        }
        outMatrix.setValues(mCurrentValues);
    }

    private final float[] mTempValues = new float[9];

    public boolean isScrolling() {
        return mScroller.computeScrollOffset();
    }

    public void stopScrolling(){
        if (isScrolling()) {
            mScroller.forceFinished(true);
        }
    }

    public boolean isAnimating() {
        return mValueAnimator.isRunning();
    }

    public void stopAnimating(){
        if (isAnimating()) {
            mValueAnimator.cancel();
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.removeAllListeners();
        }
    }

    public void stopAnimatingAndScrolling() {
        stopAnimating();
        stopScrolling();
    }

    public boolean isHorizontalNestedScrollEnabled() {
        return mHorizontalNestedScrollEnabled;
    }

    /**
     * 在嵌套模式下，外层嵌套父view可滑动容器，如ViewPage，recyclerview
     * 判断是否是让父view处理滑动
     *
     * @return
     */
    public boolean isParentScrollEnabled() {
        if (isHorizontalNestedScrollEnabled()) {
            TransformGestureDetector detector = getDetector();
            if ((isImageInLeftEdge() || isIdentity()) && (detector.getGestureIntent() == TransformGestureDetector.GESTURE_INTENT_DIRECTION_RIGHT)) {
                return true;
            }
            if ((isImageInRightEdge() || isIdentity()) && (detector.getGestureIntent() == TransformGestureDetector.GESTURE_INTENT_DIRECTION_LEFT)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            return super.onTouchEvent(event);
        } else {
            boolean parentScroll = isParentScrollEnabled();
            if (parentScroll) {
                return false;
            } else {
                return super.onTouchEvent(event);
            }
        }
    }

    public ScaleFactorRetriever getScaleFactorFetcher() {
        if (scaleFactorRetriever == null) {
            scaleFactorRetriever = new DefaultScaleFactorRetrieverImp(this);
        }
        return scaleFactorRetriever;
    }

    public long getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void setScaleFactorFetcher(ScaleFactorRetriever scaleFactorRetriever) {
        this.scaleFactorRetriever = scaleFactorRetriever;
    }

    public Matrix getPreviousTempTransform() {
        return mPreviousTempTransform;
    }

    public Matrix getAnimeWorkingTransform() {
        return mAnimeWorkingTransform;
    }
}
