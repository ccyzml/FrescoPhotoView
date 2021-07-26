package com.zml.frescophotoview.gestures

/**
 * 初始的手势意图
 */
interface GestureInitDirectionIntent {
    companion object {
        const val IGNORE = -1
        const val UNDEFINE = 0
        const val LEFT = 1
        const val UP = 2
        const val RIGHT = 3
        const val DOWN = 4
    }
}