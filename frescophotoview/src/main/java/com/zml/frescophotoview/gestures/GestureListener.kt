package com.zml.frescophotoview.gestures

interface GestureListener<T> {
    /**
     * 手势动作开始，第一个手指触碰屏幕，后续的手指不会在触发该接口，触发onGestureUpdate
     */
    fun onGestureBegin(detector: T)

    /**
     * 任一手指数目变动，或者手指移动
     */
    fun onGestureUpdate(detector: T)

    /**
     * 手势动作结束，最后一个手指离开屏幕
     */
    fun onGestureEnd(detector: T)
}