package com.zml.frescophotoviewdemo.model

import com.zml.frescophotoviewdemo.R

object DataSource {
    val medias:ArrayList<PhotoMedia> = ArrayList()
    init {
        medias.add(
            PhotoMedia("https://www.gstatic.com/webp/gallery/1.sm.jpg",
            intArrayOf(320, 214))
        )
        medias.add(
            PhotoMedia("https://www.gstatic.com/webp/gallery/2.sm.jpg",
            intArrayOf(320,235))
        )
        medias.add(VideoMedia(resToUrl(R.raw.cat), resToUrl(R.mipmap.cat_cover), intArrayOf(1920,1080)))
        medias.add(
            PhotoMedia("https://www.gstatic.com/webp/gallery/3.sm.jpg",
            intArrayOf(320,180))
        )
        medias.add(
            PhotoMedia("https://www.gstatic.com/webp/gallery/4.sm.jpg",
            intArrayOf(320,241))
        )
        medias.add(
            PhotoMedia("https://www.gstatic.com/webp/gallery/5.sm.jpg",
            intArrayOf(320,235))
        )
        medias.add(VideoMedia(resToUrl(R.raw.cat), resToUrl(R.mipmap.cat_cover), intArrayOf(1920,1080)))
    }

    private fun resToUrl(res:Int):String {
        return "android.resource://com.zml.frescophotoview/${res}"
    }
}