package com.zml.frescophotoview.transition;

import android.view.MotionEvent;
import com.zml.frescophotoview.DefaultGestureHandler;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 2:22 下午
 */
public class TransitionGestureHandler extends DefaultGestureHandler {
    private FrescoTransitionPhotoView frescoTransitionPhotoView;
    public TransitionGestureHandler(FrescoTransitionPhotoView frescoPhotoView) {
        super(frescoPhotoView);
        frescoTransitionPhotoView = frescoPhotoView;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final TransitionZoomableControllerImp zc =
                (TransitionZoomableControllerImp) frescoTransitionPhotoView.getZoomableController();
        if (zc.isInDragTransitionState()) {
            return false;
        } else {
            super.onFling(e1,e2,velocityX,velocityY);
        }
        return false;
    }

}
