package com.zml.frescophotoview

/**
 * @autrhor zhangminglei01
 * 每次双击缩放时可以通过此接口告知缩放大小
 */
interface ScaleFactorRetriever {
    /**
     * @param currFactor 当前的缩放参数因子
     * @return 下一个缩放参数因子
     */
    fun nextFactor(currFactor: Float): Float
}