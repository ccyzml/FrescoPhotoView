package com.zml.frescophotoview;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 11:06 上午
 */
public interface ScaleFactorRetriever {
    //返回值需>=1
    float nextFactor(float currFactor);
}
