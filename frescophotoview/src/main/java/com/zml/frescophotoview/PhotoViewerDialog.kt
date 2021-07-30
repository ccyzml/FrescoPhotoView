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
import com.zml.frescophotoview.transition.TransitionPagerAdapter

open class PhotoViewerDialog(context: Context) : Dialog(context) {
    var pager: ViewPager2
        private set
    var adapter: TransitionPagerAdapter<*>? = null
        set(value) {
            field = value
            pager.adapter = value
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