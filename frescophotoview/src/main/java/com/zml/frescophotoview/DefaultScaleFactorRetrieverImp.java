package com.zml.frescophotoview;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 11:16 上午
 */
public class DefaultScaleFactorRetrieverImp implements ScaleFactorRetriever {
    private ZoomableControllerImp zc;

    public DefaultScaleFactorRetrieverImp(ZoomableControllerImp zc) {
        this.zc = zc;
    }

    @Override
    public float nextFactor(float currFactor) {
        final float maxScale = zc.getMaxScaleFactor();
        final float minScale = zc.getMinScaleFactor();
        float scale = currFactor < (maxScale + minScale) / 2 ? maxScale : minScale;
        return scale;
    }
}
