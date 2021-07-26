package com.zml.frescophotoview.gestures

import android.view.MotionEvent

class MultiPointerGestureDetector {

    var isGestureInProgress = false
        private set
    var curPointerCount = 0
        private set
    var lastPointCount = 0
        private set

    private val mId = IntArray(MAX_POINTERS)
    val startX = FloatArray(MAX_POINTERS)
    val startY = FloatArray(MAX_POINTERS)
    val currentX = FloatArray(MAX_POINTERS)
    val currentY = FloatArray(MAX_POINTERS)
    val lastX = FloatArray(MAX_POINTERS)
    val lastY = FloatArray(MAX_POINTERS)
    var listener: GestureListener<MultiPointerGestureDetector>? = null

    private fun reset() {
        isGestureInProgress = false
        curPointerCount = 0
        lastPointCount = 0
        for (i in 0 until MAX_POINTERS) {
            mId[i] = MotionEvent.INVALID_POINTER_ID
            startX[i] = 0f
            startY[i] = 0f
            currentX[i] = 0f
            currentY[i] = 0f
            lastX[i] = 0f
            lastY[i] = 0f
        }
    }

    private fun startGesture() {
        isGestureInProgress = true
        listener?.onGestureBegin(this)
    }

    private fun updateGesture() {
        listener?.onGestureUpdate(this)
    }

    private fun stopGesture() {
        listener?.onGestureEnd(this)
        reset()
    }

    /**
     * Gets the index of the i-th pressed pointer. Normally, the index will be equal to i, except in
     * the case when the pointer is released.
     *
     * @return index of the specified pointer or -1 if not found (i.e. not enough pointers are down)
     */
    private fun getPressedPointerIndex(event: MotionEvent, i: Int): Int {
        var i = i
        val count = event.pointerCount
        val action = event.actionMasked
        val index = event.actionIndex
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            if (i >= index) {
                i++
            }
        }
        return if (i < count) i else -1
    }

    private fun updatePointersOnTap(event: MotionEvent) {
        lastPointCount = curPointerCount
        curPointerCount = 0
        for (i in 0 until MAX_POINTERS) {
            val index = getPressedPointerIndex(event, i)
            if (index == -1) {
                mId[i] = MotionEvent.INVALID_POINTER_ID
            } else {
                mId[i] = event.getPointerId(index)
                lastX[i] = event.getX(index)
                startX[i] = lastX[i]
                currentX[i] = startX[i]
                lastY[i] = event.getY(index)
                startY[i] = lastY[i]
                currentY[i] = startY[i]
                curPointerCount++
            }
        }
    }

    private fun updatePointersOnMove(event: MotionEvent) {
        lastPointCount = curPointerCount
        for (i in 0 until MAX_POINTERS) {
            val index = event.findPointerIndex(mId[i])
            if (index != -1) {
                lastX[i] = currentX[i]
                lastY[i] = currentY[i]
                currentX[i] = event.getX(index)
                currentY[i] = event.getY(index)
            }
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startGesture()
                updatePointersOnTap(event)
            }
            MotionEvent.ACTION_UP -> {
                updatePointersOnTap(event)
                if (curPointerCount == 0) {
                    stopGesture()
                } else {
                    updateGesture()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                updatePointersOnMove(event)
                listener?.onGestureUpdate(this)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                updatePointersOnTap(event)
                updateGesture()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                updatePointersOnTap(event)
                if (curPointerCount == 0) {
                    stopGesture()
                } else {
                    updateGesture()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                stopGesture()
            }
        }
        return true
    }

    companion object {
        private const val MAX_POINTERS = 2

    }
}