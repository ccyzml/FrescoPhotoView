package com.zml.frescophotoview.transition

import android.content.Context
import android.util.AttributeSet
import com.zml.frescophotoview.DefaultGestureHandler
import com.zml.frescophotoview.FrescoPhotoView
import com.zml.frescophotoview.gestures.TransformGestureDetector
import java.util.*

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 2:00 下午
 */
class FrescoTransitionPhotoView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrescoPhotoView(context, attrs, defStyle), TransitionView {
    private lateinit var controllerImp: TransitionZoomableControllerImp
    private var curDragFactor = 0f
    private val transitionListeners = LinkedHashSet<TransitionListener>()
    private val transitionListenerWrapper: TransitionListener = object : TransitionListener {
        override fun onTransitionBegin(state: Int) {
            transitionListeners.forEach {it.onTransitionBegin(state)}
        }

        override fun onTransitionChanged(state: Int, factor: Float) {
            transitionListeners.forEach {it.onTransitionChanged(state,factor)}
            if (state == TransitionState.STATE_DRAG_TRANSITION) {
                curDragFactor = factor
            }
            if (state == TransitionState.STATE_OUT_TRANSITION) {
                if (curDragFactor > 0 ) {
                    alpha = 1 / curDragFactor * factor
                }
            }
        }

        override fun onTransitionEnd(state: Int) {
            transitionListeners.forEach {it.onTransitionEnd(state)}
        }
    }

    var transitionEnabled : Boolean = false
        set(enabled) {
            controllerImp.setTransitionEnabled(enabled)
        }

    init {
        controllerImp.addTransitionListener(
            transitionListenerWrapper
        )
    }

    override fun createController(): TransitionZoomableControllerImp {
        controllerImp = TransitionZoomableControllerImp(TransformGestureDetector(), context)
        return controllerImp
    }

    override fun createGestureHandler(): DefaultGestureHandler {
        return TransitionGestureHandler(controllerImp)
    }

    override fun addTransitionListener(transitionListener: TransitionListener) {
        transitionListeners.add(transitionListener)
    }

    fun dismissAnimated() {
        controllerImp.dismissAnimated()
    }

    override fun removeTransitionListener(listener: TransitionListener) {
        transitionListeners.remove(listener)
    }
}