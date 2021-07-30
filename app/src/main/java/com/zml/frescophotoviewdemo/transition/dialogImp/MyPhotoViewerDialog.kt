package com.zml.frescophotoviewdemo.transition.dialogImp

import android.content.Context
import com.zml.frescophotoview.transition.TransitionListener
import com.zml.frescophotoview.transition.TransitionState
import com.zml.frescophotoviewdemo.DemoAdapter
import com.zml.frescophotoview.PhotoViewerDialog

class MyPhotoViewerDialog(context: Context) : PhotoViewerDialog(context) {

    init {
        adapter = DemoAdapter()
        //==============非必要配置========================
        pager.setBackgroundColor(context.resources.getColor(android.R.color.black))
        pager.background.alpha = 0
        (adapter as DemoAdapter).outerTransitionListener = object : TransitionListener {
            override fun onTransitionBegin(state: Int) {}
            override fun onTransitionChanged(state: Int, factor: Float) {
                //拖动的时候改变背景渐变
                pager.background.alpha = (factor * 255).toInt()
            }

            override fun onTransitionEnd(state: Int) {
                if (state == TransitionState.STATE_ENTER_TRANSITION) {
                    (adapter as DemoAdapter).playVideo()
                }
                //退出过渡动画结束后关闭activity
                if (state == TransitionState.STATE_OUT_TRANSITION) {
                    dismiss()
                }
            }
        }
    }
}