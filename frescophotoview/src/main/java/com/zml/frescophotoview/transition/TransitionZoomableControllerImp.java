package com.zml.frescophotoview.transition;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import com.zml.frescophotoview.ZoomableControllerImp;
import com.zml.frescophotoview.gestures.TransformGestureDetector;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 2:13 下午
 */
public class TransitionZoomableControllerImp extends ZoomableControllerImp implements DragTransitionView{
    private TransitionListener mTransitionListener;
    private boolean mDragTransitionEnabled = true;
    private static final float DRAG_EPS = 0.7f;
    private int mTransitionState = DragTransitionView.STATE_UNDEFINE;
    private final Matrix mNewTransform = new Matrix();
    private boolean allowDragTransition = false;


    private ValueAnimator.AnimatorUpdateListener mTransitionAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float previousScaleFactor = getMatrixScaleFactor(getPreviousTempTransform());
            float workingScaleFactor = getMatrixScaleFactor(getAnimeWorkingTransform());
            float factor = workingScaleFactor / previousScaleFactor;
            if (mTransitionListener != null) {
                mTransitionListener.onTransitionChanged(mTransitionState, factor);
            }
        }
    };

    public TransitionZoomableControllerImp(TransformGestureDetector transformGestureDetector, Context context) {
        super(transformGestureDetector, context);
    }

    @Override
    public void setTransitionListener(TransitionListener listener) {
        mTransitionListener = listener;
    }

    public boolean isInDragTransitionState() {
        return mTransitionState == DragTransitionView.STATE_DRAG_TRANSITION;
    }


    @Override
    public void onGestureBegin(TransformGestureDetector detector) {
        super.onGestureBegin(detector);
        if (isDisableGesture()) return;
        mTransitionState = DragTransitionView.STATE_UNDEFINE;
        //当手势刚开始时图片在顶部边缘或者未超过顶部，那么可能进入DragTransition，置为true
        if (!isImageOutTopEdge()) {
            allowDragTransition = true;
        } else {
            allowDragTransition = false;
        }
    }

    @Override
    public void onGestureUpdate(TransformGestureDetector detector) {
        if (isDisableGesture()) return;
        if (isAnimatingOrScrolling()) {
            return;
        }

        if (mDragTransitionEnabled &&
                detector.getGestureIntent() == TransformGestureDetector.GESTURE_INTENT_DIRECTION_DOWN &&
                allowDragTransition ) {
            if (mTransitionState == DragTransitionView.STATE_UNDEFINE) {
                mTransitionState = DragTransitionView.STATE_DRAG_TRANSITION;
                if (mTransitionListener != null) {
                    mTransitionListener.onTransitionBegin(mTransitionState);
                }
            }
        }

        if (mTransitionState == DragTransitionView.STATE_DRAG_TRANSITION) {
            calculateDragGestureTransform(getActiveTransform());
            onTransformChanged();
        } else {
            super.onGestureUpdate(detector);
        }
    }

    @Override
    public void onGestureEnd(TransformGestureDetector detector) {
        if (isDisableGesture()) {
            return;
        }
        if (mTransitionState == DragTransitionView.STATE_DRAG_TRANSITION) {
            float factor = getDragFactor();
            if (mTransitionListener != null) {
                mTransitionListener.onTransitionEnd(mTransitionState);
            }
            if (factor < DRAG_EPS) {
                dragToDismissStatus(factor);
            } else {
                resumeToDragBeginningStatus();
            }
        } else {
           super.onGestureEnd(detector);
        }
    }

    private void resumeToDragBeginningStatus() {
        mTransitionState = DragTransitionView.STATE_RESUME_TRANSITION;
        setDisableGesture(true);
        mNewTransform.set(getPreviousTempTransform());
        if (mTransitionListener != null) {
            mTransitionListener.onTransitionBegin(mTransitionState);
        }
        setTransformAnimated(mNewTransform, getAnimationDuration(), () -> {
            setDisableGesture(false);
            if (mTransitionListener != null) {
                mTransitionListener.onTransitionEnd(mTransitionState);
            }
        }, mTransitionAnimatorListener);
    }


    private void dragToDismissStatus(float startFactor) {
        mTransitionState = DragTransitionView.STATE_OUT_TRANSITION;
        setDisableGesture(true);
        mNewTransform.set(getActiveTransform());
        RectF rectF = getTransformedImageBounds();
        mNewTransform.postScale(0, 0, rectF.centerX(), rectF.centerY());
        long duration = (long) (startFactor * 600);
        if (mTransitionListener != null) {
            mTransitionListener.onTransitionBegin(mTransitionState);
        }
        setTransformAnimated(mNewTransform, duration, () -> {
            if (mTransitionListener != null) {
                mTransitionListener.onTransitionEnd(mTransitionState);
            }
        }, mTransitionAnimatorListener);
    }

    private float getDragFactor() {
        if (mDragTransitionEnabled) {
            TransformGestureDetector detector = getDetector();
            float translationY = detector.getTranslationY();
            if (detector.getTranslationY() > 0) {
                float scale = 1 - Math.min(translationY / 1300, 0.95f);
                return scale;
            }
        }
        return 1;
    }


    protected boolean calculateDragGestureTransform(Matrix outTransform) {
        TransformGestureDetector detector = getDetector();
        RectF imageBound = getImageBounds();
        outTransform.set(getPreviousTempTransform());
        float translationY = detector.getTranslationY();
        float scale = getDragFactor();
        outTransform.postScale(scale, scale, imageBound.centerX(), imageBound.centerY());
        if (mTransitionListener != null) {
            mTransitionListener.onTransitionChanged(mTransitionState, scale);
        }
        if (isTranslationEnabled()) {
            outTransform.postTranslate(detector.getTranslationX(), translationY);
        }
        return false;
    }

    public void setTransitionEnabled(boolean enabled) {
        mDragTransitionEnabled = enabled;
    }
}
