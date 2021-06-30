package com.zml.frescophotoview.transition;

/**
 * @autrhor zhangminglei01
 * @date 2021/3/22 4:58 下午
 */
public interface TransitionListener {
    void onTransitionBegin(int state);
    void onTransitionChanged(int state, float factor);
    void onTransitionEnd(int state);
}
