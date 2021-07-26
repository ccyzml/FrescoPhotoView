package com.zml.frescophotoviewdemo

import android.app.Activity
import android.os.Bundle
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.zml.frescophotoview.FrescoPhotoView
import com.zml.frescophotoview.ScaleFactorRetriever

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 5:33 下午
 */
class DemoActivity : Activity() {
    lateinit var frescoPhotoView: FrescoPhotoView
    var allowZooming = true
    var allowTransition = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        frescoPhotoView = findViewById(R.id.photo_view)
        val controller: DraweeController = Fresco.newDraweeControllerBuilder()
            .setUri("https://www.gstatic.com/webp/gallery/1.sm.jpg")
            .build()
        frescoPhotoView.controller = controller



        //==================可选配置======================
        //自定义缩放
        frescoPhotoView.setMaxScaleFactor(4f)
        frescoPhotoView.setHorizontalNestedScrollEnabled(true)
        frescoPhotoView.setScaleFactorRetriever(object : ScaleFactorRetriever {
            override fun nextFactor(currFactor: Float): Float {
                if (currFactor >= 1 && currFactor < 2) {
                    return 2f
                } else if (currFactor >= 2 && currFactor < 3) {
                    return 3f
                } else if (currFactor >= 3) {
                    return 1f
                }
                return 1f
            }
        })
        //
        frescoPhotoView.setTapListener(object : SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                Toast.makeText(this@DemoActivity, "double click", Toast.LENGTH_SHORT).show()
                return super.onDoubleTap(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Toast.makeText(this@DemoActivity, "click", Toast.LENGTH_SHORT).show()
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent) {
                Toast.makeText(this@DemoActivity, "longPress", Toast.LENGTH_SHORT).show()
                super.onLongPress(e)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.allow_zoom) {
            allowZooming = !allowZooming
            frescoPhotoView.setZoomingEnabled(allowZooming)
            item.isCheckable = allowZooming
        } else if (item.itemId == R.id.allow_transition) {
            allowTransition = !allowTransition
            frescoPhotoView.setTranslationEnabled(allowTransition)
            item.isCheckable = allowTransition
        }
        return super.onOptionsItemSelected(item)
    }
}