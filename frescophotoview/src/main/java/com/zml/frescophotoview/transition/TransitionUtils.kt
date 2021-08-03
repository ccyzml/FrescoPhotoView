package com.zml.frescophotoview.transition

import android.graphics.Rect
import android.view.View

/**
 * @autrhor zhangminglei01
 * @date 2021/3/12 5:20 下午
 */
object TransitionUtils {

    @JvmStatic
    fun calculateStartRect(view: View, photoWidthHeight:IntArray): Rect {
        return calculateStartRect(view,photoWidthHeight[0],photoWidthHeight[1])
    }

    @JvmStatic
    fun calculateStartRect(view: View, photoWidth: Int, photoHeight: Int): Rect {
        val location = IntArray(2)
        val viewWidth = view.width
        val viewHeight = view.height
        val centerLocation = IntArray(2)
        view.getLocationOnScreen(location)
        centerLocation[0] = location[0] + viewWidth / 2
        centerLocation[1] = location[1] + viewHeight / 2
        val photoRect = Rect()
        if (photoHeight > photoHeight) {
            val imageWidth = (viewHeight * (photoWidth.toFloat() / photoHeight)).toInt()
            photoRect[centerLocation[0] - imageWidth / 2, location[1], centerLocation[0] + imageWidth / 2] =
                location[1] + viewHeight
        } else {
            val imageHeight = (viewWidth * (photoHeight.toFloat() / photoWidth)).toInt()
            photoRect[location[0], centerLocation[1] - imageHeight / 2, location[0] + viewWidth] =
                centerLocation[1] + imageHeight / 2
        }
        val heightWidth = IntArray(2)
        heightWidth[0] = view.height
        heightWidth[1] = view.width
        return photoRect
    }

    @JvmStatic
    fun rectToIntArray(rect: Rect): IntArray {
        return rect.run {
            intArrayOf(left, top, right, bottom)
        }
    }

    @JvmStatic
    fun intArrayToRect(intArray: IntArray): Rect {
        require(intArray.size == 4) { "array length must be 4" }
        return intArray.let {
            Rect(it[0],it[1],it[2],it[3])
        }
    }
}