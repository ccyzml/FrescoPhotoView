package com.zml.frescophotoview

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 11:16 上午
 */
class DefaultScaleFactorRetrieverImp(private val zc: ZoomableControllerImp) : ScaleFactorRetriever {
    override fun nextFactor(currFactor: Float): Float {
        val maxScale = zc.maxScaleFactor
        val minScale = zc.minScaleFactor
        return if (currFactor < (maxScale + minScale) / 2) maxScale else minScale
    }
}