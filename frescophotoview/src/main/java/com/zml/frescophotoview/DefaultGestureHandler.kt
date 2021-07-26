package com.zml.frescophotoview

import android.graphics.PointF
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent

/**
 * 默认的行为处理双击 fling动作
 */
open class DefaultGestureHandler(private val zoomableControllerImp: ZoomableControllerImp) :
    SimpleOnGestureListener() {
    private val mDoubleTapViewPoint = PointF()
    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return super.onSingleTapConfirmed(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        val zc = zoomableControllerImp
        val vp = PointF(e.x, e.y)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> mDoubleTapViewPoint.set(vp)
            MotionEvent.ACTION_UP -> {
                val fetcher = zc.scaleFactorFetcher
                fetcher.nextFactor(zc.scaleFactor)
                zc.isDisableGesture = true
                zc.zoomToPoint(
                    fetcher.nextFactor(zc.scaleFactor),
                    vp,
                    { zc.isDisableGesture = false },
                    null
                )
            }
        }
        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val zc = zoomableControllerImp
        if (zc.scaleFactor > zc.minScaleFactor) {
            zc.fling(velocityX.toInt(), velocityY.toInt())
        }
        return false
    }
}