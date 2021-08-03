package com.zml.frescophotoview

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.viewpager2.widget.ViewPager2
import com.zml.frescophotoview.transition.TransitionListener
import com.zml.frescophotoview.transition.TransitionPagerAdapter
import com.zml.frescophotoview.transition.TransitionState

open class PhotoViewerDialog(context: Context) : Dialog(context) {
    var pager: ViewPager2
        private set
    var adapter: TransitionPagerAdapter<*>? = null
        set(value) {
            field = value
            pager.adapter = value
            adapter?.internalTransitionListener = object : TransitionListener {
                override fun onTransitionBegin(state: Int) {}
                override fun onTransitionChanged(state: Int, factor: Float) {
                    //拖动的时候改变背景渐变
                    pager.background.alpha = (factor * 255).toInt()
                }

                override fun onTransitionEnd(state: Int) {
                    //退出过渡动画结束后关闭Dialog
                    if (state == TransitionState.STATE_OUT_TRANSITION) {
                        dismiss()
                    }
                }
            }
        }

    init {
        pager = ViewPager2(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(pager)
        val window = window ?: return
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        pager.setBackgroundColor(context.resources.getColor(android.R.color.black))
        pager.background.alpha = 0
    }

    fun showAnimated(position: Int, startRect: Rect) {
        if (!isShowing) {
            if (adapter == null || position >= adapter?.itemCount ?: 0) {
                show()
                return
            } else {
                pager.setCurrentItem(position, false)
                adapter?.startEnterAnimation(position, startRect)
                show()
            }
        }
    }

    override fun dismiss() {
        if (isShowing) {
            adapter?.onDestroy()
        }
        super.dismiss()
    }
}