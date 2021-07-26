package com.zml.frescophotoviewdemo

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 5:36 下午
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}