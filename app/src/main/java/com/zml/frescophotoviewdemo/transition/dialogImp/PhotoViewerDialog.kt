package com.zml.frescophotoviewdemo.transition.dialogImp

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.zml.frescophotoview.transition.TransitionListener
import com.zml.frescophotoview.transition.TransitionState
import com.zml.frescophotoviewdemo.model.DataSource.medias
import com.zml.frescophotoviewdemo.PagerAdapter
import com.zml.frescophotoviewdemo.R

class PhotoViewerDialog(context: Context) : FullScreenDialog(context) {
    private var adapter: PagerAdapter
    var root: ViewGroup
    var pager: ViewPager2

    fun showAnimated(position: Int, startRect: Rect) {
        pager.setCurrentItem(position, false)
        adapter.startEnterAnimation(position, startRect)
        show()
    }

    override fun dismiss() {
        super.dismiss()
        adapter.onDestroy()
    }

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.activirty_transition_demo_2, null)
        setContentView(view)
        root = findViewById(R.id.root)
        pager = findViewById(R.id.pager)
        adapter = PagerAdapter()
        pager.adapter = adapter
        adapter.data = medias

        //==============非必要配置========================
        root.setBackgroundColor(context.resources.getColor(android.R.color.black))
        root.background.alpha = 0
        adapter.outerTransitionListener = object : TransitionListener {
            override fun onTransitionBegin(state: Int) {}
            override fun onTransitionChanged(state: Int, factor: Float) {
                //拖动的时候改变背景渐变
                root.background.alpha = (factor * 255).toInt()
            }

            override fun onTransitionEnd(state: Int) {
                //退出过渡动画结束后关闭activity
                if (state == TransitionState.STATE_OUT_TRANSITION) {
                    dismiss()
                }
            }
        }
    }
}