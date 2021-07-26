package com.zml.frescophotoviewdemo

import android.graphics.Rect
import android.net.Uri
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.zml.frescophotoview.transition.FrescoTransitionPhotoView
import com.zml.frescophotoview.transition.TransitionLayout
import com.zml.frescophotoview.transition.TransitionListener
import com.zml.frescophotoview.transition.TransitionState
import com.zml.frescophotoviewdemo.model.Media
import com.zml.frescophotoviewdemo.model.PhotoMedia
import com.zml.frescophotoviewdemo.model.VideoMedia

class PagerAdapter : RecyclerView.Adapter<ViewHolder>(),LifecycleObserver{
    var outerTransitionListener: TransitionListener? = null
    var data: List<Media>? = null
    private var needAnimating: Boolean = false
    private var animeHolder: ViewHolder? = null
    private var animaPos: Int? = null
    private var startRect:Rect? = null
    private var playingHolder:VideoVH? =null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            PHOTO -> {
                return PhotoVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.photo_vh, parent, false)
                )
            }
            VIDEO -> {
                return VideoVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.video_vh, parent, false)
                )
            }
        }
        return PhotoVH(
            LayoutInflater.from(parent.context).inflate(R.layout.photo_vh, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is PhotoVH) {
            val photoMedia : PhotoMedia = data?.get(position) as PhotoMedia
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setUri(photoMedia.picUrl)
                .build()
            holder.photoView.controller = controller
        } else if (holder is VideoVH){
            val videoMedia : VideoMedia = data?.get(position) as VideoMedia
            holder.itemView.tag = videoMedia
            holder.videoCoverView.visibility = View.VISIBLE
            holder.videoCoverView.setImageURI(videoMedia.videoCoverUrl)
        }
        if (needAnimating && animaPos == position) {
            animeHolder = holder
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is VideoVH) {
            playingHolder = holder
            if (!needAnimating) {
                playVideo()
            }
        }
        if (needAnimating) {
            needAnimating = false
            if (animeHolder is PhotoVH && startRect != null) {
                (animeHolder as PhotoVH).transitionLayout.startEnterTransition(startRect!!);
            }
            if (animeHolder is VideoVH && startRect != null) {
                (animeHolder as VideoVH).transitionLayout.startEnterTransition(startRect!!);
            }
        }
    }

    fun playVideo() {
        val holder = playingHolder ?: return
        val videoMedia = holder.itemView.tag as VideoMedia
        holder.videoView.setVideoURI(Uri.parse(videoMedia.videoUrl))
        holder.videoView.start()
        holder.videoView.postDelayed({
            holder.videoCoverView.visibility =  View.GONE
        },500)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is VideoVH) {
            holder.videoView.stopPlayback()
            holder.videoCoverView.visibility = View.VISIBLE
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        playingHolder?.videoView?.stopPlayback()
    }

    override fun getItemViewType(position: Int): Int {
        return if (data?.get(position) is PhotoMedia) {
            PHOTO
        } else {
            VIDEO
        }
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    fun startEnterAnimation(position: Int, startRect: Rect) {
        needAnimating = true
        animaPos = position
        this.startRect = startRect
    }

    internal inner class VideoVH(itemView: View) : ViewHolder(itemView) {
        var transitionLayout: TransitionLayout = itemView as TransitionLayout
        var videoView: VideoView = itemView.findViewById(R.id.video_view)
        var videoCoverView:SimpleDraweeView = itemView.findViewById(R.id.video_cover);

        init {
            transitionLayout.setTransitionListener(object : TransitionListener {
                override fun onTransitionBegin(state: Int) {
                    outerTransitionListener?.onTransitionBegin(state)
                }

                override fun onTransitionChanged(state: Int, factor: Float) {
                    outerTransitionListener?.onTransitionChanged(state, factor)
                }

                override fun onTransitionEnd(state: Int) {
                    outerTransitionListener?.onTransitionEnd(state)
                    if (state == TransitionState.STATE_ENTER_TRANSITION) {
                        playVideo()
                    }
                }
            })
        }

    }

    internal inner class PhotoVH(itemView: View) : ViewHolder(itemView) {
        var photoView: FrescoTransitionPhotoView = itemView.findViewById(R.id.photo_view)
        var transitionLayout: TransitionLayout = itemView as TransitionLayout
        private val transitionListener = object : TransitionListener {
            override fun onTransitionBegin(state: Int) {
                outerTransitionListener?.onTransitionBegin(state)
            }

            override fun onTransitionChanged(state: Int, factor: Float) {
                outerTransitionListener?.onTransitionChanged(state, factor)
            }

            override fun onTransitionEnd(state: Int) {
                outerTransitionListener?.onTransitionEnd(state)
            }
        }
        init {
            photoView.setHorizontalNestedScrollEnabled(true)
            transitionLayout.setDelegateDragTransitionView(photoView)
            photoView.setTapListener(object : SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    photoView.dismissAnimated()
                    return super.onSingleTapConfirmed(e)
                }
            })
            transitionLayout.setTransitionListener(transitionListener)
            photoView.setTransitionListener(transitionListener)
        }
    }

    companion object {
        const val PHOTO = 1
        const val VIDEO = 2
    }
}