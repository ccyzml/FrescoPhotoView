package com.zml.frescophotoview.transition

import android.view.MotionEvent

/**
 * @autrhor zhangminglei01
 * @date 2021/3/15 10:13 上午
 */
interface TransitionView {
    fun onTouchEvent(event: MotionEvent): Boolean
    fun setTransitionListener(listener: TransitionListener?)

}