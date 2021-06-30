package com.zml.frescophotoview.transition;

import android.graphics.Rect;
import android.view.View;

/**
 * @autrhor zhangminglei01
 * @date 2021/3/12 5:20 下午
 */
public class PhotoTransitionUtils {
    public static Rect calculateInitPhotoRect(View view, int photoWidth, int photoHeight){
        int[] location = new int[2];
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        int[] centerLocation = new int[2];
        view.getLocationOnScreen(location);
        centerLocation[0] = location[0] + viewWidth / 2;
        centerLocation[1] = location[1] + viewHeight / 2;
        Rect photoRect = new Rect();

        if (photoHeight > photoHeight) {
            int imageWidth = (int) (viewHeight * ((float) photoWidth / photoHeight));
            photoRect.set(centerLocation[0] - imageWidth / 2, location[1], centerLocation[0] + imageWidth / 2, location[1] + viewHeight);
        } else {
            int imageHeight = (int) (viewWidth * ((float) photoHeight / photoWidth));
            photoRect.set(location[0], centerLocation[1] - imageHeight / 2, location[0] + viewWidth, centerLocation[1] + imageHeight / 2);
        }
        int[] heightWidth = new int[2];
        heightWidth[0] = view.getHeight();
        heightWidth[1] = view.getWidth();
        return photoRect;
    }

    public static int[] rectToIntArray(Rect rect) {
        return new int[]{rect.left,rect.top,rect.right,rect.bottom};
    }

    public static Rect intArrayToRect(int[] intArray) {
        if (intArray.length != 4){
            throw new IllegalArgumentException("array length must be 4");
        }
        return new Rect(intArray[0],intArray[1],intArray[2],intArray[3]);
    }
}
