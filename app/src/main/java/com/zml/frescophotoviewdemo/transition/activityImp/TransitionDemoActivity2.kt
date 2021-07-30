package com.zml.frescophotoviewdemo.transition.activityImp

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.zml.frescophotoview.transition.TransitionListener
import com.zml.frescophotoview.transition.TransitionState
import com.zml.frescophotoview.transition.TransitionUtils.intArrayToRect
import com.zml.frescophotoviewdemo.DemoAdapter
import com.zml.frescophotoviewdemo.R
import com.zml.frescophotoviewdemo.model.DataSource.medias

/**
 * @autrhor zhangminglei01
 * Activity实现过渡效果
 */
class TransitionDemoActivity2 : AppCompatActivity() {
    private lateinit var adapter: DemoAdapter
    private lateinit var root: ViewGroup
    private lateinit var pager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activirty_transition_demo_2)
        root = findViewById(R.id.root)
        pager = findViewById(R.id.pager)
        adapter = DemoAdapter()
        pager.adapter = adapter
        //==============必要配置========================
        //获取上个页面图片位置，url参数
        val intent = intent
        val startRectArray = intent.getIntArrayExtra("start_rect_array")
        val position = intent.getIntExtra("position", 0)
        pager.setCurrentItem(position, false)
        val startRect = intArrayToRect(startRectArray!!)
        adapter.startEnterAnimation(position, startRect)

        //==============非必要配置========================
        root.setBackgroundColor(resources.getColor(android.R.color.black))
        root.background.alpha = 0
        adapter.outerTransitionListener = object : TransitionListener {
            override fun onTransitionBegin(state: Int) {}
            override fun onTransitionChanged(state: Int, factor: Float) {
                //拖动的时候改变背景渐变
                root.background.alpha = (factor * 255).toInt()
            }

            override fun onTransitionEnd(state: Int) {
                //退出过渡动画结束后关闭activity
                if (state == TransitionState.STATE_OUT_TRANSITION) {
                    finish()
                }
            }
        }
    }
}