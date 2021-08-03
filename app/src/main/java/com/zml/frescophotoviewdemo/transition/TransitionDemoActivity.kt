package com.zml.frescophotoviewdemo.transition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.zml.frescophotoview.PhotoViewerDialog
import com.zml.frescophotoview.transition.TransitionUtils
import com.zml.frescophotoviewdemo.DemoAdapter
import com.zml.frescophotoviewdemo.R
import com.zml.frescophotoviewdemo.model.DataSource
import com.zml.frescophotoviewdemo.model.PhotoMedia
import com.zml.frescophotoviewdemo.model.VideoMedia

/**
 * @autrhor zhangminglei01
 * @date 2021/6/8 3:18 下午
 */
class TransitionDemoActivity : AppCompatActivity() {
    lateinit var rv: RecyclerView
    var medias: List<PhotoMedia> = DataSource.medias
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition_demo)
        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                return VH(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_vh, parent, false)
                )
            }

            override fun onBindViewHolder(holder: VH, position: Int) {
                val media = medias[position]
                val url = media.picUrl
                holder.draweeView.setImageURI(url)
                if (media is VideoMedia) {
                    holder.playIV.visibility = View.VISIBLE
                }
                holder.draweeView.setOnClickListener { v: View? ->
                    val startRect = TransitionUtils.calculateStartRect(
                        holder.draweeView,
                        media.picWidthHeight
                    )
                    PhotoViewerDialog(this@TransitionDemoActivity).apply { adapter = DemoAdapter() }.showAnimated(position,startRect)
                }
            }

            override fun getItemCount(): Int {
                return medias.size
            }
        }
    }

    internal class VH(itemView: View) : ViewHolder(itemView) {
        var draweeView: SimpleDraweeView = itemView.findViewById(R.id.img_sdv)
        var playIV: ImageView = itemView.findViewById(R.id.play_iv)

    }
}