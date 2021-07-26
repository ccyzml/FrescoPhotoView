package com.zml.frescophotoviewdemo.model

import com.zml.frescophotoviewdemo.model.Media

class VideoMedia constructor (
    var videoUrl: String? = null,
    var videoCoverUrl:String? = null,
    var picWidthHeight: IntArray,) : Media()