package com.zml.frescophotoview.transition

import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Matrix
import com.zml.frescophotoview.ZoomableControllerImp
import com.zml.frescophotoview.gestures.GestureInitDirectionIntent
import com.zml.frescophotoview.gestures.TransformGestureDetector

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 2:13 下午
 */
class TransitionZoomableControllerImp(
    transformGestureDetector: TransformGestureDetector?,
    context: Context?
) : ZoomableControllerImp(transformGestureDetector, context), TransitionView {
    private var transitionListener: TransitionListener? = null
    private var dragTransitionEnabled = true
    private var transitionState = TransitionState.UNDEFINE
    private val newTransform = Matrix()
    private var allowDragTransition = false
    private val dragTransitionStartMatrix = Matrix()
    private val transitionAnimatorListener = AnimatorUpdateListener {
        val previousScaleFactor = getMatrixScaleFactor(dragTransitionStartMatrix)
        val workingScaleFactor = getMatrixScaleFactor(animeWorkingTransform)
        val factor = workingScaleFactor / previousScaleFactor
        transitionListener?.onTransitionChanged(transitionState, factor)
    }

    override fun setTransitionListener(listener: TransitionListener?) {
        transitionListener = listener
    }

    val isInDragTransitionState: Boolean
        get() = transitionState == TransitionState.STATE_DRAG_TRANSITION

    override fun onGestureBegin(detector: TransformGestureDetector) {
        super.onGestureBegin(detector)
        if (isDisableGesture) return
        transitionState = TransitionState.UNDEFINE
        //当手势刚开始时图片在顶部边缘或者未超过顶部，那么可能进入DragTransition，置为true
        allowDragTransition = !isImageOutTopEdge
    }

    override fun onGestureUpdate(detector: TransformGestureDetector) {
        if (isDisableGesture) return
        if (isAnimatingOrScrolling) {
            return
        }
        if (dragTransitionEnabled && detector.gestureIntent == GestureInitDirectionIntent.DOWN &&
            allowDragTransition
        ) {
            if (transitionState == TransitionState.UNDEFINE) {
                transitionState = TransitionState.STATE_DRAG_TRANSITION
                dragTransitionStartMatrix.set(activeTransform)
                transitionListener?.onTransitionBegin(transitionState)
            }
        }
        if (transitionState == TransitionState.STATE_DRAG_TRANSITION) {
            calculateDragGestureTransform(activeTransform)
            onTransformChanged()
        } else {
            super.onGestureUpdate(detector)
        }
    }

    override fun onGestureEnd(detector: TransformGestureDetector) {
        if (isDisableGesture) {
            return
        }
        if (transitionState == TransitionState.STATE_DRAG_TRANSITION) {
            val factor = dragFactor
            transitionListener?.onTransitionEnd(transitionState)
            if (factor < DRAG_EPS) {
                dismissAnimated()
            } else {
                resumeAnimated()
            }
        } else {
            super.onGestureEnd(detector)
        }
    }

    private fun resumeAnimated() {
        transitionState = TransitionState.STATE_RESUME_TRANSITION
        isDisableGesture = true
        newTransform.set(dragTransitionStartMatrix)
        transitionListener?.onTransitionBegin(transitionState)
        setTransformAnimated(newTransform, animationDuration, {
            isDisableGesture = false
            transitionListener?.onTransitionEnd(transitionState)
        }, transitionAnimatorListener)
    }

    fun dismissAnimated() {
        transitionState = TransitionState.STATE_OUT_TRANSITION
        isDisableGesture = true
        newTransform.set(activeTransform)
        val rectF = transformedImageBounds
        newTransform.postScale(0f, 0f, rectF.centerX(), rectF.centerY())
        transitionListener?.onTransitionBegin(transitionState)
        setTransformAnimated(newTransform, animationDuration, {
            transitionListener?.onTransitionEnd(transitionState)
        }, transitionAnimatorListener)
    }

    private val dragFactor: Float
        get() {
            if (dragTransitionEnabled) {
                val detector = detector
                val translationY = detector.translationY
                if (translationY > 0) {
                    val translationYAvailable = viewBounds.height()
                    return 1 - (translationY / translationYAvailable).coerceAtMost(0.95f)
                }
            }
            return 1f
        }

    protected fun calculateDragGestureTransform(outTransform: Matrix): Boolean {
        val detector = detector
        val imageBound = imageBounds
        outTransform.set(dragTransitionStartMatrix)
        val translationY = detector.translationY
        val scale = dragFactor
        outTransform.postScale(scale, scale, imageBound.centerX(), imageBound.centerY())
        transitionListener?.onTransitionChanged(transitionState, scale)
        if (isTranslationEnabled) {
            outTransform.postTranslate(detector.translationX, translationY)
        }
        return false
    }

    fun setTransitionEnabled(enabled: Boolean) {
        dragTransitionEnabled = enabled
    }

    companion object {
        private const val DRAG_EPS = 0.8f
    }
}