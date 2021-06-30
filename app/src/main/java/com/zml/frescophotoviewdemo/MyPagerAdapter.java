/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoviewdemo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.viewpager.widget.PagerAdapter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.zml.frescophotoview.FrescoPhotoView;
import com.zml.frescophotoviewdemo.R;
import com.zml.frescophotoview.ScaleFactorRetriever;


class MyPagerAdapter extends PagerAdapter {

    private static final String[] SAMPLE_URIS = {
            "https://www.gstatic.com/webp/gallery/1.sm.jpg",
            "https://www.gstatic.com/webp/gallery/2.sm.jpg",
            "https://www.gstatic.com/webp/gallery/3.sm.jpg",
            "https://www.gstatic.com/webp/gallery/4.sm.jpg",
            "https://www.gstatic.com/webp/gallery/5.sm.jpg",
    };

    private final int mItemCount;

    public MyPagerAdapter(int itemCount) {
        mItemCount = itemCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FrameLayout page = (FrameLayout) container.getChildAt(position);
        if (page == null) {
            return null;
        }
        FrescoPhotoView frescoPhotoView = page.findViewById(R.id.zoomableView);
        frescoPhotoView.setHorizontalNestedScrollEnabled(true);
        DraweeController controller =
                Fresco.newDraweeControllerBuilder()
                        .setUri(SAMPLE_URIS[position % SAMPLE_URIS.length])
                        .build();
        frescoPhotoView.setController(controller);
        frescoPhotoView.setMaxScaleFactor(4);
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
        page.requestLayout();
        return page;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        FrameLayout page = (FrameLayout) container.getChildAt(position);
        FrescoPhotoView frescoPhotoView = (FrescoPhotoView) page.getChildAt(0);
        frescoPhotoView.setController(null);
    }

    @Override
    public int getCount() {
        return mItemCount;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getItemPosition(Object object) {
        // We want to create a new view when we call notifyDataSetChanged() to have the correct behavior
        return POSITION_NONE;
    }
}
