package com.zml.frescophotoview;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/3 11:38 上午
 */
public class Utils {
    //计算阻尼
    public static float calculateDamping(float curValue, float thresholdValue, float factor) {
        if (curValue < 0 || thresholdValue < 0 || factor < 0) return curValue;
        if (curValue < thresholdValue) {
            return curValue;
        } else {
            float overValue = curValue - thresholdValue;
            float dampingValue = thresholdValue;
            int n = 100;
            float dampingFactor = 1f / 100;
            while (overValue - factor > 0) {
                dampingValue += factor * (dampingFactor * n);
                overValue -= factor;
                if (n > 31) {
                    n--;
                }
            }
            dampingValue += overValue * dampingFactor * (n);
            return dampingValue;
        }
    }
}
