package com.zml.frescophotoviewdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.zml.frescophotoview.FrescoPhotoView;
import com.zml.frescophotoviewdemo.R;

import com.zml.frescophotoview.ScaleFactorRetriever;

/**
 * @autrhor zhangminglei01
 * @date 2021/6/7 5:33 下午
 */
public class DemoActivity extends Activity {
    FrescoPhotoView frescoPhotoView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        frescoPhotoView = findViewById(R.id.photo_view);
        Bitmap bitmap = Bitmap.createBitmap(100, 100,
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.parseColor("#FF0000"));//填充颜色
        DraweeController controller =
                Fresco.newDraweeControllerBuilder()
                        .setUri("https://www.gstatic.com/webp/gallery/1.sm.jpg")
                        .build();
        frescoPhotoView.setController(controller);
        //二级缩放（可选，默认一级缩放到满屏）
        frescoPhotoView.setMaxScaleFactor(4);
        frescoPhotoView.setTapListener(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }
        });
        frescoPhotoView.setScaleFactorRetriever(new ScaleFactorRetriever() {
            @Override
            public float nextFactor(float currFactor) {
                if (currFactor >= 1 && currFactor < 2) {
                    return 2;
                } else if (currFactor >= 2 && currFactor < 3) {
                    return 3;
                }else if (currFactor >= 3) {
                    return 1;
                }
                return 1;
            }
        });
    }
}
