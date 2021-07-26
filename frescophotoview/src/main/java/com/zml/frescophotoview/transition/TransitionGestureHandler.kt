package com.zml.frescophotoview.transition

import android.view.MotionEvent
import com.zml.frescophotoview.DefaultGestureHandler

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 2:22 下午
 */
class TransitionGestureHandler(private val transitionZoomableControllerImp: TransitionZoomableControllerImp) :
    DefaultGestureHandler(
        transitionZoomableControllerImp
    ) {
    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val zc = transitionZoomableControllerImp
        if (zc.isInDragTransitionState) {
            return false
        } else {
            super.onFling(e1, e2, velocityX, velocityY)
        }
        return false
    }
}