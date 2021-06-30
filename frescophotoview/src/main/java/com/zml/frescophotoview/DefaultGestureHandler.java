/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoview;

import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;

import static com.zml.frescophotoview.ZoomableControllerImp.LIMIT_ALL;

/**
 * Tap gesture listener for double tap to zoom / unzoom and double-tap-and-drag to zoom.
 */
public class DefaultGestureHandler extends GestureDetector.SimpleOnGestureListener {
    private final FrescoPhotoView mFrescoPhotoView;
    private final PointF mDoubleTapViewPoint = new PointF();

    public DefaultGestureHandler(FrescoPhotoView frescoPhotoView) {
        mFrescoPhotoView = frescoPhotoView;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        final ZoomableControllerImp zc =
                (ZoomableControllerImp) mFrescoPhotoView.getZoomableController();
        PointF vp = new PointF(e.getX(), e.getY());
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDoubleTapViewPoint.set(vp);
                break;
            case MotionEvent.ACTION_UP:
                ScaleFactorRetriever fetcher = zc.getScaleFactorFetcher();
                fetcher.nextFactor(zc.getScaleFactor());
                zc.setDisableGesture(true);
                zc.zoomToPoint(
                        fetcher.nextFactor(zc.getScaleFactor()), vp, LIMIT_ALL, zc.getAnimationDuration(), () -> zc.setDisableGesture(false), null);
                break;
        }
        return true;
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final ZoomableControllerImp zc =
                (ZoomableControllerImp) mFrescoPhotoView.getZoomableController();
        if (zc.getScaleFactor() > zc.getMinScaleFactor()) {
            zc.fling((int) velocityX, (int) velocityY);
        }
        return false;
    }
}
