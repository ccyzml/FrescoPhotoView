package com.zml.frescophotoviewdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.zml.frescophotoviewdemo.transition.activityImp.TransitionDemoActivity

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 5:25 下午
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn_1).setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    DemoActivity::class.java
                )
            )
        }
        findViewById<View>(R.id.btn_3).setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    TransitionDemoActivity::class.java
                )
            )
        }
    }
}