package com.zml.frescophotoview

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent

class GestureListenerWrapper(private val defaultGestureHandler: DefaultGestureHandler) :
    SimpleOnGestureListener() {
    private var mOuterListener: SimpleOnGestureListener? = null
    fun setListener(listener: SimpleOnGestureListener?) {
        mOuterListener = listener
    }

    override fun onLongPress(e: MotionEvent) {
        mOuterListener?.onLongPress(e)
        defaultGestureHandler.onLongPress(e)
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        mOuterListener?.onScroll(e1, e2, distanceX, distanceY)
        return defaultGestureHandler.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        mOuterListener?.onFling(e1, e2, velocityX, velocityY)
        return defaultGestureHandler.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onShowPress(e: MotionEvent) {
        mOuterListener?.onShowPress(e)
        defaultGestureHandler.onShowPress(e)
    }

    override fun onDown(e: MotionEvent): Boolean {
        mOuterListener?.onDown(e)
        return defaultGestureHandler.onDown(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        mOuterListener?.onDoubleTap(e)
        return defaultGestureHandler.onDoubleTap(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        mOuterListener?.onDoubleTapEvent(e)
        return defaultGestureHandler.onDoubleTapEvent(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        mOuterListener?.onSingleTapConfirmed(e)
        return defaultGestureHandler.onSingleTapConfirmed(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        mOuterListener?.onSingleTapUp(e)
        return defaultGestureHandler.onSingleTapUp(e)
    }
}