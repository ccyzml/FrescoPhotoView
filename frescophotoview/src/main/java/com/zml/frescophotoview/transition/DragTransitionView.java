package com.zml.frescophotoview.transition;

import android.view.MotionEvent;

/**
 * @autrhor zhangminglei01
 * @date 2021/3/15 10:13 上午
 */
public interface DragTransitionView {
    int STATE_UNDEFINE = -1;
    int STATE_ENTER_TRANSITION = 0;
    int STATE_RESUME_TRANSITION = 1;
    int STATE_OUT_TRANSITION = 2;
    int STATE_DRAG_TRANSITION = 3;
    boolean onTouchEvent(MotionEvent event);
    void setTransitionListener(TransitionListener listener);
}
