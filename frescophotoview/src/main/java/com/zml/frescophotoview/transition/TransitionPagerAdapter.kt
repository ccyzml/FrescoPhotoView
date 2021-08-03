package com.zml.frescophotoview.transition

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView

abstract class TransitionPagerAdapter<T:RecyclerView.ViewHolder> : RecyclerView.Adapter<T>() {
    var needAnimation: Boolean = false
        private set
    private var animationHolder: RecyclerView.ViewHolder? = null
    private var animationPos: Int? = null
    private var startRect:Rect? = null
    internal var internalTransitionListener:TransitionListener? = null


    override fun onBindViewHolder(holder: T, position: Int) {
        if (needAnimation && animationPos == position) {
            animationHolder = holder
        }
    }

    fun startEnterAnimation(position: Int, startRect: Rect) {
        if (position >= itemCount) return
        needAnimation = true
        animationPos = position
        this.startRect = startRect
    }

    override fun onViewAttachedToWindow(holder: T) {
        super.onViewAttachedToWindow(holder)
        if (needAnimation) {
            needAnimation = false
            (holder.itemView as TransitionLayout).startEnterTransition(startRect!!)
        }
        internalTransitionListener?.let { (holder.itemView as TransitionLayout).addTransitionListener(it) }
    }

    override fun onViewDetachedFromWindow(holder: T) {
        super.onViewDetachedFromWindow(holder)
        internalTransitionListener?.let { (holder.itemView as TransitionLayout).removeTransitionListener(it) }
    }

    open fun onDestroy(){}


}