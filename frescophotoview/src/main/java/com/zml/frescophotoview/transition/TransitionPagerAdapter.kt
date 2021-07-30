package com.zml.frescophotoview.transition

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView

abstract class TransitionPagerAdapter<T : TransitionViewHolder> : RecyclerView.Adapter<T>() {
    var needAnimating: Boolean = false
        private set
    private var animeHolder: RecyclerView.ViewHolder? = null
    private var animaPos: Int? = null
    private var startRect:Rect? = null


    override fun onBindViewHolder(holder: T, position: Int) {
        if (needAnimating && animaPos == position) {
            animeHolder = holder
        }
    }

    fun startEnterAnimation(position: Int, startRect: Rect) {
        if (position >= itemCount) return
        needAnimating = true
        animaPos = position
        this.startRect = startRect
    }

    override fun onViewAttachedToWindow(holder: T) {
        super.onViewAttachedToWindow(holder)
        if (needAnimating) {
            needAnimating = false
            holder.transitionLayout.startEnterTransition(startRect!!)
        }
    }

    open fun onDestroy(){}

}