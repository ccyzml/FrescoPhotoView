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

import static com.zml.frescophotoview.gestures.GestureInitDirectionIntent.*;

public class ZoomableControllerImp extends ABSZoomableController {

    private final float[] mStartValues = new float[9];
    private final float[] mStopValues = new float[9];
    private final float[] mCurrentValues = new float[9];
    private final Matrix mNewTransform = new Matrix();
    private final Matrix mAnimeWorkingTransform = new Matrix();
    private final Matrix mFlingStartTempTransform = new Matrix();
    private ScaleFactorRetriever scaleFactorRetriever;

    private boolean mHorizontalNestedScrollEnabled = false;
    private final ValueAnimator mValueAnimator;

    public static final long DEFAULT_ANIMATION_DURATION = 400;
    private long animationDuration = DEFAULT_ANIMATION_DURATION;
    private Runnable enableGestureWhenCompleted = () -> setDisableGesture(false);

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

    public void zoomToPoint(
            float scaleFactor,
            PointF viewPoint,
            @Nullable Runnable completeListener,
            @Nullable ValueAnimator.AnimatorUpdateListener updateListener) {
        if (isAnimating()) {
            stopAnimating();
        }
        mNewTransform.reset();
        calculateZoomToPointTransform(mNewTransform, scaleFactor, viewPoint);
        setTransformAnimated(mNewTransform, getAnimationDuration(), completeListener, updateListener);
    }

    public void fling(int velocityX, int velocityY) {
        if (isAnimating() || !isTranslationEnabled()) return;
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
            calculateTranslateToPointTransform(mNewTransform, currX, currY);
            setTransformImmediate(mNewTransform);
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
                        //将动画结束后的状态更新到初始矩阵
                        mValueAnimator.removeAllUpdateListeners();
                        mValueAnimator.removeAllListeners();
                        if (completeListener != null) {
                            completeListener.run();
                        }
                    }
                });
        mValueAnimator.start();
    }

    @Override
    public void onGestureBegin(TransformGestureDetector detector) {
        if (mDisableGesture) return;
        stopAnimatingAndScrolling();
    }

    @Override
    public void onGestureUpdate(final TransformGestureDetector detector) {
        if (mDisableGesture) return;
        if (isAnimatingOrScrolling()) {
            return;
        }
        //当多根手指变更为一根手指
        if (detector.isPointerCountChanged() && detector.isSinglePointer()) {
            trySpringBack();
        } else {
            calculateGestureTransform(getActiveTransform());
            onTransformChanged();
        }
    }

    @Override
    public void onGestureEnd(TransformGestureDetector detector) {
        if (mDisableGesture) return;
    }

    protected boolean calculateGestureTransform(Matrix outTransform) {
        TransformGestureDetector detector = getDetector();
        outTransform.set(getActiveTransform());
        if (isTranslationEnabled()) {
            outTransform.postTranslate(detector.getDeltaTranslationX(), detector.getDeltaTranslationY());
        }
        if (isScaleEnabled() && detector.isMultiPointer()) {
            outTransform.postScale(detector.getDeltaScale(), detector.getDeltaScale(), detector.getPivotX(), detector.getPivotY());
        }
        if (detector.isSinglePointer()) {
            limitTranslation(outTransform);
        }
        return true;
    }

    protected void calculateZoomToPointTransform(
            Matrix outTransform,
            float scale,
            PointF viewPoint) {
        PointF imagePoint = mapViewToImage(viewPoint);
        outTransform.setScale(scale, scale, imagePoint.x, imagePoint.y);
        limitScale(outTransform, imagePoint.x, imagePoint.y);
        limitTranslation(outTransform);
    }

    protected void calculateTranslateToPointTransform(
            Matrix outTransform,
            float x,
            float y) {
        outTransform.postTranslate(x, y);
        limitTranslation(outTransform);
    }

    protected boolean limitScale(
            Matrix transform, float pivotX, float pivotY) {
        float currentScale = getMatrixScaleFactor(transform);
        float targetScale =  Math.min(Math.max(getMinScaleFactor(), currentScale), getMaxScaleFactor());
        if (targetScale != currentScale) {
            float scale = targetScale / currentScale;
            transform.postScale(scale, scale, pivotX, pivotY);
            return true;
        }
        return false;
    }

    private final RectF mTempRect = new RectF();

    protected boolean limitTranslation(Matrix transform) {
        RectF b = mTempRect;
        RectF imageBounds = getImageBounds();
        RectF viewBounds = getViewBounds();
        b.set(imageBounds);
        transform.mapRect(b);
        float offsetLeft = getOffset(b.left, b.right, viewBounds.left, viewBounds.right, imageBounds.centerX());
        float offsetTop = getOffset(b.top, b.bottom, viewBounds.top, viewBounds.bottom, imageBounds.centerY());
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

    public void trySpringBack() {
        mNewTransform.set(getActiveTransform());
        RectF imageBounds = getViewBounds();
        boolean limited =  limitScale(mNewTransform,imageBounds.centerX(),imageBounds.centerY());
        limited |= limitTranslation(mNewTransform);
        if (limited) {
            setDisableGesture(true);
            setTransformAnimated(mNewTransform,animationDuration,enableGestureWhenCompleted,null);
        }
    }

    protected void calculateInterpolation(Matrix outMatrix, float fraction) {
        for (int i = 0; i < 9; i++) {
            mCurrentValues[i] = (1 - fraction) * mStartValues[i] + fraction * mStopValues[i];
        }
        outMatrix.setValues(mCurrentValues);
    }

    public boolean isScrolling() {
        return mScroller.computeScrollOffset();
    }

    public void stopScrolling() {
        if (isScrolling()) {
            mScroller.forceFinished(true);
        }
    }

    public boolean isAnimating() {
        return mValueAnimator.isRunning();
    }

    public void stopAnimating() {
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
            if ((isImageInLeftEdge() || isIdentity()) && (detector.getGestureIntent() == RIGHT)) {
                return true;
            }
            if ((isImageInRightEdge() || isIdentity()) && (detector.getGestureIntent() == LEFT)) {
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


    public boolean isImageOutTopEdge() {
        return getViewBounds().top - getTransformedImageBounds().top > EPS;
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

    public Matrix getAnimeWorkingTransform() {
        return mAnimeWorkingTransform;
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
}
