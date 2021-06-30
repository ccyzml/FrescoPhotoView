package com.zml.frescophotoview.transition;

import android.content.Context;
import android.util.AttributeSet;
import com.zml.frescophotoview.DefaultGestureHandler;
import com.zml.frescophotoview.FrescoPhotoView;
import com.zml.frescophotoview.gestures.TransformGestureDetector;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 2:00 下午
 */
public class FrescoTransitionPhotoView extends FrescoPhotoView implements DragTransitionView{
    private TransitionZoomableControllerImp controllerImp;
    public FrescoTransitionPhotoView(Context context) {
        this(context,null);
    }

    public FrescoTransitionPhotoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FrescoTransitionPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        ((TransitionZoomableControllerImp)getZoomableController()).setTransitionListener(mTransitionListenerWrapper);
    }

    @Override
    protected TransitionZoomableControllerImp createController() {
        controllerImp = new TransitionZoomableControllerImp(TransformGestureDetector.newInstance(), getContext());
        return controllerImp;
    }

    @Override
    protected DefaultGestureHandler createGestureHandler() {
        return new TransitionGestureHandler(this);
    }

    private float mDragFactor = 0f;
    private TransitionListener mTransitionListenerWrapper = new TransitionListener() {
        @Override
        public void onTransitionBegin(int state) {
            if (mTransitionListener != null) {
                mTransitionListener.onTransitionBegin(state);
            }
        }

        @Override
        public void onTransitionChanged(int state, float factor) {
            if (mTransitionListener != null) {
                mTransitionListener.onTransitionChanged(state,factor);
            }
            if (state == DragTransitionView.STATE_DRAG_TRANSITION) {
                mDragFactor = factor;
            }
            if (state == DragTransitionView.STATE_OUT_TRANSITION) {
                setAlpha(1/mDragFactor*factor);
            }
        }

        @Override
        public void onTransitionEnd(int state) {
            if (mTransitionListener != null) {
                mTransitionListener.onTransitionEnd(state);
            }
        }
    };
    private TransitionListener mTransitionListener;

    @Override
    public void setTransitionListener(TransitionListener listener) {
        mTransitionListener = listener;
    }

    public void setTransitionEnabled(boolean enabled) {
        controllerImp.setTransitionEnabled(enabled);
    }
}
