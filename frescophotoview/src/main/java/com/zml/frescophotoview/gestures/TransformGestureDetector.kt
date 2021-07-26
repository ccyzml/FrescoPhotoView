package com.zml.frescophotoview.gestures

import android.view.MotionEvent
import java.util.*
import kotlin.math.abs
import kotlin.math.hypot

class TransformGestureDetector : GestureListener<MultiPointerGestureDetector> {
    var listener: GestureListener<TransformGestureDetector>? = null
    private val detector: MultiPointerGestureDetector = MultiPointerGestureDetector()
    private val ignoreGestureIntents = ArrayList<Int>()

    var scale = 1f
        private set
    var translationX = 0f
        private set
    var translationY = 0f
        private set
    var pivotX = 0f
        private set
    var pivotY = 0f
        private set
    var deltaTranslationX = 0f
        private set
    var deltaTranslationY = 0f
        private set
    var deltaScale = 1f
        private set

    //手势刚开始时的意图方向，单个手指
    var gestureIntent = GestureInitDirectionIntent.UNDEFINE
        private set


    private fun reset(){
        scale = 1f
        translationX = 0f
        translationY = 0f
        pivotX = 0f
        pivotY = 0f
        deltaTranslationX = 0f
        deltaTranslationY = 0f
        deltaScale = 1f
        gestureIntent = GestureInitDirectionIntent.UNDEFINE
    }

    companion object {
        private const val GESTURE_DIRECTION_EPS = 10
    }


    init {
        detector.listener = this
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
    }

    override fun onGestureUpdate(detector: MultiPointerGestureDetector) {
        if (!isSinglePointer) {
            val startDeltaX = detector.startX[1] - detector.startX[0]
            val startDeltaY = detector.startY[1] - detector.startY[0]
            val currentDeltaX = detector.currentX[1] - detector.currentX[0]
            val currentDeltaY = detector.currentY[1] - detector.currentY[0]
            val startDist = hypot(startDeltaX.toDouble(), startDeltaY.toDouble())
                .toFloat()
            val currentDist = hypot(currentDeltaX.toDouble(), currentDeltaY.toDouble())
                .toFloat()
            val lastScale = scale
            scale = currentDist / startDist
            deltaScale = scale / lastScale
            pivotX = calcAverage(detector.currentX, detector.curPointerCount)
            pivotY = calcAverage(detector.currentY, detector.curPointerCount)
        } else {
            scale = 1f
            deltaScale = 1f
        }
        deltaTranslationX = (calcAverage(detector.currentX, detector.curPointerCount)
                - calcAverage(detector.lastX, detector.curPointerCount))
        translationX += deltaTranslationX
        deltaTranslationY = (calcAverage(detector.currentY, detector.curPointerCount)
                - calcAverage(detector.lastY, detector.curPointerCount))
        translationY += deltaTranslationY
        if (isSinglePointer) {
            if (gestureIntent == GestureInitDirectionIntent.UNDEFINE) {
                if (abs(translationX) - abs(translationY) >= 0) {
                    if (translationX < -GESTURE_DIRECTION_EPS) {
                        if (!ignoreGestureIntents.contains(GestureInitDirectionIntent.LEFT)) {
                            gestureIntent = GestureInitDirectionIntent.LEFT
                        }
                    }
                    if (translationX > GESTURE_DIRECTION_EPS) {
                        if (!ignoreGestureIntents.contains(GestureInitDirectionIntent.RIGHT)) {
                            gestureIntent = GestureInitDirectionIntent.RIGHT
                        }
                    }
                } else {
                    if (translationY < -GESTURE_DIRECTION_EPS) {
                        if (!ignoreGestureIntents.contains(GestureInitDirectionIntent.UP)) {
                            gestureIntent = GestureInitDirectionIntent.UP
                        }
                    }
                    if (translationY > GESTURE_DIRECTION_EPS) {
                        if (!ignoreGestureIntents.contains(GestureInitDirectionIntent.DOWN)) {
                            gestureIntent = GestureInitDirectionIntent.DOWN
                        }
                    }
                }
            }
        } else {
            gestureIntent = GestureInitDirectionIntent.IGNORE
        }
        listener?.onGestureUpdate(this)
    }

    override fun onGestureBegin(detector: MultiPointerGestureDetector) {
        listener?.onGestureBegin(this)
    }

    override fun onGestureEnd(detector: MultiPointerGestureDetector) {
        listener?.onGestureEnd(this)
        reset()
    }

    private fun calcAverage(arr: FloatArray, len: Int): Float {
        var sum = 0f
        for (i in 0 until len) {
            sum += arr[i]
        }
        return if (len > 0) sum / len else 0f
    }

    var isPointerCountChanged: Boolean = false
        get() = detector.curPointerCount != detector.lastPointCount
        private set

    var isSinglePointer: Boolean = false
        get() = detector.curPointerCount == 1
        private set

    var isMultiPointer: Boolean = false
        get() = detector.curPointerCount > 1
        private set

    var isGestureInProgress: Boolean = false
        get() = detector.isGestureInProgress
        private set


    //如果手势意图被忽视那么判断初始手势时会跳过该动作
    fun ignoreInitGestureIntent(gestureIntent: Int) {
        if (!ignoreGestureIntents.contains(gestureIntent)) {
            ignoreGestureIntents.add(gestureIntent)
        }
    }
}