package com.zml.frescophotoviewdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.zml.frescophotoview.transition.*;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/8 3:30 下午
 */
public class TransitionDemoActivity2 extends Activity {
    PhotoTransitionLayout photoTransitionLayout;
    FrescoTransitionPhotoView frescoTransitionPhotoView;
    ViewGroup root;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activirty_transition_demo_2);
        root = findViewById(R.id.root);
        photoTransitionLayout = findViewById(R.id.transition_layout);
        frescoTransitionPhotoView = findViewById(R.id.photo_view);
        //获取上个页面图片位置，url参数
        Intent intent = getIntent();
        int[] imgLocations = intent.getIntArrayExtra("img_location");
        String url = intent.getStringExtra("url");
        Rect imgRect = PhotoTransitionUtils.intArrayToRect(imgLocations);
        //加载图片，确保这张图已经是在本地缓存的，如果第一时间加载不出图片动画就不流畅
        DraweeController controller =
                Fresco.newDraweeControllerBuilder()
                        .setUri(url)
                        .build();
        frescoTransitionPhotoView.setController(controller);
        //开始入场动画
        photoTransitionLayout.startEnterTransition(imgRect);

        root.setBackgroundColor(getResources().getColor(android.R.color.black));
        root.getBackground().setAlpha(0);
        //入场动画监听（可选），一般会改变底色
        photoTransitionLayout.setTransitionListener(new TransitionListener() {
            @Override
            public void onTransitionBegin(int state) {
            }

            @Override
            public void onTransitionChanged(int state, float factor) {
                    root.getBackground().setAlpha((int) (factor*255));
            }

            @Override
            public void onTransitionEnd(int state) {

            }
        });
        //照片拖拽监听（可选），一般会改变底色
        frescoTransitionPhotoView.setTransitionListener(new TransitionListener() {
            @Override
            public void onTransitionBegin(int state) {

            }

            @Override
            public void onTransitionChanged(int state, float factor) {
                root.getBackground().setAlpha((int) (factor*255));
            }

            @Override
            public void onTransitionEnd(int state) {
                if (state == DragTransitionView.STATE_OUT_TRANSITION) {
                    TransitionDemoActivity2.this.finish();
                }
            }
        });
    }
}
