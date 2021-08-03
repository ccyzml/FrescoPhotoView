package com.zml.frescophotoviewdemo

import android.media.MediaPlayer
import android.net.Uri
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.zml.frescophotoview.transition.*
import com.zml.frescophotoviewdemo.model.DataSource
import com.zml.frescophotoviewdemo.model.PhotoMedia
import com.zml.frescophotoviewdemo.model.VideoMedia

class DemoAdapter : TransitionPagerAdapter<RecyclerView.ViewHolder>(){
    private var data: List<PhotoMedia> = DataSource.medias
    private var playingHolder:VideoVH? =null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            PHOTO -> {
                return PhotoVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.photo_vh, parent, false) as TransitionLayout
                )
            }
            VIDEO -> {
                return VideoVH(
                    LayoutInflater.from(parent.context).inflate(R.layout.video_vh, parent, false) as TransitionLayout
                )
            }
        }
        return PhotoVH(
            LayoutInflater.from(parent.context).inflate(R.layout.photo_vh, parent, false) as TransitionLayout
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PhotoVH) {
            val photoMedia : PhotoMedia = data[position]
            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
                .setUri(photoMedia.picUrl)
                .build()
            holder.photoView.controller = controller
        } else if (holder is VideoVH){
            val videoMedia : VideoMedia = data[position] as VideoMedia
            holder.itemView.tag = videoMedia
            holder.videoCoverView.visibility = View.VISIBLE
            holder.videoCoverView.setImageURI(videoMedia.picUrl)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is VideoVH) {
            playingHolder = holder
            if (!needAnimation) {
                playVideo()
            }
        }
    }

    fun playVideo() {
        val holder = playingHolder ?: return
        val videoMedia = holder.itemView.tag as VideoMedia
        holder.videoView.setVideoURI(Uri.parse(videoMedia.videoUrl))
        holder.videoView.start()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is VideoVH) {
            holder.videoView.stopPlayback()
            holder.videoCoverView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        playingHolder?.videoView?.stopPlayback()
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is VideoMedia) {
            VIDEO
        } else {
            PHOTO
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    internal inner class VideoVH(itemView: TransitionLayout) : RecyclerView.ViewHolder(itemView) {
        private val transitionLayout = itemView
        var videoView: VideoView = itemView.findViewById(R.id.video_view)
        var videoCoverView:SimpleDraweeView = itemView.findViewById(R.id.video_cover);

        init {
            transitionLayout.addTransitionListener(object : TransitionListener {
                override fun onTransitionBegin(state: Int) {}
                override fun onTransitionChanged(state: Int, factor: Float) {}
                override fun onTransitionEnd(state: Int) {
                    if (state == TransitionState.STATE_ENTER_TRANSITION) {
                         playVideo()
                    }
                }
            })
            videoView.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    videoCoverView.visibility = View.GONE
                }
                false
            }
        }
    }


    internal inner class PhotoVH(itemView: TransitionLayout) : RecyclerView.ViewHolder(itemView) {
        private val transitionLayout = itemView
        var photoView: FrescoTransitionPhotoView = itemView.findViewById(R.id.photo_view)
        init {
            //支持嵌套滚动
            photoView.setHorizontalNestedScrollEnabled(true)
            //这行很重要!!!!!!!!使FrescoPhotoView与TransitionLayout能合作处理触控
            transitionLayout.setDelegateDragTransitionView(photoView)
            //如果不需要轻触退出效果的可以不加
            photoView.setTapListener(object : SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    photoView.dismissAnimated()
                    return super.onSingleTapConfirmed(e)
                }
            })
        }
    }

    companion object {
        const val PHOTO = 1
        const val VIDEO = 2
    }
}