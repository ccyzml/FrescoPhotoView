package com.zml.frescophotoview.transition

/**
 * @autrhor zhangminglei01
 * @date 2021/3/22 4:58 下午
 * 每一个过渡state都会经历begin，changed，end状态
 */
interface TransitionListener {
    fun onTransitionBegin(state: Int)

    /**
     * 当过渡动画进行中，所有导致画面变化的行为触发回调
     * 当一个过渡state触发开始时，factor为1，后续随拖动情况改变
     * 入场动画刚开始取值为0，到完成取值为1
     * 可以利用这个接口在过渡状态下做些动画，例如改变背景色
     */
    fun onTransitionChanged(state: Int, factor: Float)

    fun onTransitionEnd(state: Int)
}