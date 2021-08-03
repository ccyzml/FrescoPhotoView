package com.zml.frescophotoviewdemo.model

class VideoMedia constructor(
    var videoUrl: String,
    picUrl: String,
    picWidthHeight: IntArray,
) : PhotoMedia(picUrl, picWidthHeight)