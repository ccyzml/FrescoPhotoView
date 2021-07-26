package com.zml.frescophotoview.transition

/**
 * 入场，或者当用户向下拖拽，离场时会触发过渡动画
 */
interface TransitionState {
    companion object {
        //不在过渡状态
        const val UNDEFINE = -1
        //入场过渡动画状态
        const val STATE_ENTER_TRANSITION = 0
        //拖动后未到阈值回弹过渡动画状态
        const val STATE_RESUME_TRANSITION = 1
        //离场过渡动画状态
        const val STATE_OUT_TRANSITION = 2
        //拖动时过渡动画
        const val STATE_DRAG_TRANSITION = 3
    }
}