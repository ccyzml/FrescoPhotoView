package com.zml.frescophotoviewdemo.transition.activityImp

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
import com.zml.frescophotoview.transition.TransitionUtils.calculateStartRect
import com.zml.frescophotoviewdemo.R
import com.zml.frescophotoviewdemo.transition.dialogImp.PhotoViewerDialog
import com.zml.frescophotoviewdemo.model.DataSource
import com.zml.frescophotoviewdemo.model.Media
import com.zml.frescophotoviewdemo.model.PhotoMedia
import com.zml.frescophotoviewdemo.model.VideoMedia

/**
 * @autrhor zhangminglei01
 * @date 2021/6/8 3:18 下午
 */
class TransitionDemoActivity : AppCompatActivity() {
    lateinit var rv: RecyclerView
    var medias: List<Media> = DataSource.medias
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
                val url =
                    if (media is PhotoMedia) media.picUrl else (media as VideoMedia).videoCoverUrl
                holder.draweeView.setImageURI(url)
                if (media is VideoMedia) {
                    holder.playIV.visibility = View.VISIBLE
                }
                holder.draweeView.setOnClickListener { v: View? ->
                    var imageWidthHeight = IntArray(2)
                    if (media is VideoMedia) {
                        imageWidthHeight = media.picWidthHeight
                    }
                    if (media is PhotoMedia) {
                        imageWidthHeight = media.picWidthHeight
                    }
                    val startRect = calculateStartRect(
                        holder.draweeView,
                        imageWidthHeight[0], imageWidthHeight[1]
                    )
                    //Activity实现
    //                    Intent intent = new Intent(TransitionDemoActivity.this,TransitionDemoActivity2.class);
    //                    intent.putExtra("start_rect_array",PhotoTransitionUtils.rectToIntArray(startRect));
    //                    intent.putExtra("position",position);
    //                    startActivity(intent);
                    //Dialog实现
                    PhotoViewerDialog(this@TransitionDemoActivity).showAnimated(position, startRect)
                }
            }

            override fun getItemCount(): Int {
                return medias.size
            }
        }
    }

    internal class VH(itemView: View) : ViewHolder(itemView) {
        var draweeView: SimpleDraweeView
        var playIV: ImageView

        init {
            draweeView = itemView.findViewById(R.id.img_sdv)
            playIV = itemView.findViewById(R.id.play_iv)
        }
    }
}