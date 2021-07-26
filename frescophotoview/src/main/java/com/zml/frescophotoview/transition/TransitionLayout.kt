package com.zml.frescophotoview.transition

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.NonNull
import com.zml.frescophotoview.gestures.GestureInitDirectionIntent
import com.zml.frescophotoview.gestures.GestureListener
import com.zml.frescophotoview.gestures.TransformGestureDetector

/**
 * @autrhor zhangminglei01
 * @date 2021/3/12 11:29 上午
 */
class TransitionLayout : FrameLayout, GestureListener<TransformGestureDetector>,
    TransitionView {
    private var startRect: Rect = Rect()

    // endRect由第一次测量获得
    private var endRect: Rect? = null
    private val workingRect: Rect = Rect()
    private val workingRectF: RectF = RectF()
    private val workingTransform = Matrix()
    private var childView: View? = null
    private val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val detector: TransformGestureDetector = TransformGestureDetector()
    private var transitionState = -1
    private var skipLayout = false
    private var transitionViewDelegate: TransitionView? = null
    private val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)
    private var transitionListener: TransitionListener? = null
    private var dragFactor = 1f
    private val dismissAlphaAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        val alpha = animation?.animatedValue as Float
        val flingView = childView ?: return@AnimatorUpdateListener
        flingView.alpha = alpha
    }

    private val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        val startRect = startRect
        val endRect = endRect ?: return@AnimatorUpdateListener
        val flingView = childView ?: return@AnimatorUpdateListener
        val value = animation?.animatedValue as Float
        workingRect.left = startRect.left.plus(value * (endRect.left - startRect.left)).toInt()
        workingRect.top = startRect.top.plus(value * (endRect.top - startRect.top)).toInt()
        workingRect.right = startRect.right.plus(value * (endRect.right - startRect.right)).toInt()
        workingRect.bottom = startRect.bottom.plus(value * (endRect.bottom - startRect.bottom)).toInt()
        transformView(workingRect, flingView)
        when (transitionState) {
            TransitionState.STATE_ENTER_TRANSITION -> {
                transitionListener?.onTransitionChanged(transitionState, value)
            }
            TransitionState.STATE_RESUME_TRANSITION -> {
                transitionListener?.onTransitionChanged(transitionState, dragFactor + (1 - dragFactor) * value)
            }
            TransitionState.STATE_OUT_TRANSITION -> {
                transitionListener?.onTransitionChanged(transitionState, dragFactor * (1 - value))
            }
        }
    }

    private val animatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            transitionListener?.onTransitionBegin(transitionState)
        }

        override fun onAnimationEnd(animation: Animator?) {
            transitionListener?.onTransitionEnd(transitionState)
        }

        override fun onAnimationCancel(animation: Animator?) {
            transitionListener?.onTransitionEnd(transitionState)
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        detector.ignoreInitGestureIntent(GestureInitDirectionIntent.UP)
        animator.addUpdateListener(animatorUpdateListener)
        animator.addListener(animatorListener)
        animator.duration = 250
        alphaAnimator.duration = 250
        alphaAnimator.addUpdateListener(dismissAlphaAnimatorUpdateListener)
        detector.listener = this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (endRect == null) {
            endRect = Rect(0, 0, measuredWidth, measuredHeight)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount < 2) {
            if (childCount == 1) {
                childView = getChildAt(0)
            }
        } else {
            throw IllegalStateException("only allow one child view")
        }
        if (skipLayout) {
            return
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (animator.isRunning) return false
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (transitionViewDelegate == null) {
            ev?.let { detector.onTouchEvent(it) }
            requestDisallowInterceptTouchEvent(transitionState == TransitionState.STATE_DRAG_TRANSITION)
        }
        return transitionState == TransitionState.STATE_DRAG_TRANSITION
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (transitionState == TransitionState.STATE_DRAG_TRANSITION) {
            requestDisallowInterceptTouchEvent(true)
        }
        detector.onTouchEvent(event)
        return true
    }

    override fun setTransitionListener(listener: TransitionListener?) {
        transitionListener = listener
    }

    private fun transformView(rect: Rect, view: View) {
        view.x = rect.left.toFloat()
        view.y = rect.top.toFloat()
        view.layoutParams.width = rect.width()
        view.layoutParams.height = rect.height()
        view.requestLayout()
    }

    override fun onGestureBegin(detector: TransformGestureDetector) {
        transitionState = TransitionState.UNDEFINE
    }

    override fun onGestureUpdate(detector: TransformGestureDetector) {
        if (this.detector.gestureIntent == GestureInitDirectionIntent.DOWN) {
            transitionState = TransitionState.STATE_DRAG_TRANSITION
            transitionListener?.onTransitionBegin(transitionState)
        }
        if (transitionState == TransitionState.STATE_DRAG_TRANSITION) {
            workingTransform.reset()
            dragFactor = getDragFactor()
            workingTransform.postTranslate(this.detector.translationX, this.detector.translationY)
            workingTransform.postScale(dragFactor, dragFactor, workingRect.centerX().toFloat(), workingRect.centerY().toFloat())
            workingRectF.set(endRect!!)
            workingTransform.mapRect(workingRectF)
            workingRect.set(workingRectF)
            childView?.let {
                transformView(workingRect, it)
            }
            transitionListener?.onTransitionChanged(transitionState, dragFactor)
        }
    }

    private fun getDragFactor(): Float {
        val translationY = this.detector.translationY
        if (detector.translationY > 0) {
            return 1 - (translationY / 1300).coerceAtMost(0.95f)
        }
        return 1f
    }

    var eps = 0.7f

    override fun onGestureEnd(detector: TransformGestureDetector) {
        if (transitionState == TransitionState.STATE_DRAG_TRANSITION) {
            transitionListener?.onTransitionEnd(transitionState)
            dragFactor = getDragFactor()
            if (dragFactor < eps) {
                startRect.set(workingRect)
                workingTransform.reset()
                workingTransform.postScale(0f, 0f, workingRect.centerX().toFloat(), workingRect.centerY().toFloat())
                workingRectF.set(endRect!!)
                workingTransform.mapRect(workingRectF)
                workingRect.set(workingRectF)
                endRect?.set(workingRect)
                transitionState = TransitionState.STATE_OUT_TRANSITION
                animator.start()
                alphaAnimator.start()
            } else {
                startRect.set(workingRect)
                transitionState = TransitionState.STATE_RESUME_TRANSITION
                animator.start()
            }
        }
    }

    fun Rect.set(rectF: RectF) {
        left = rectF.left.toInt()
        top = rectF.top.toInt()
        right = rectF.right.toInt()
        bottom = rectF.bottom.toInt()
    }

    fun startEnterTransition(@NonNull startRect: Rect) {
        skipLayout = true
        if (animator.isRunning) return
        transitionState = TransitionState.STATE_ENTER_TRANSITION
        this.startRect = startRect
        animator.start()
        postDelayed({ skipLayout = false }, 50)
    }

    fun setDelegateDragTransitionView(transitionView: TransitionView) {
        transitionViewDelegate = transitionView
        transitionViewDelegate?.setTransitionListener(transitionListener)
    }
}
