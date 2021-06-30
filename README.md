# FrescoPhotoView
一个基于Fresco的流畅看图组件
## 使用
```
frescoPhotoView = findViewById(R.id.photo_view);
DraweeController controller = Fresco.newDraweeControllerBuilder()
                              .setUri("https://www.gstatic.com/webp/gallery/1.sm.jpg")
                              .build();
frescoPhotoView.setController(controller);
```

## 嵌套滚动支持(如在ViewPager，RecyclerView中)
```
frescoPhotoView.setHorizontalNestedScrollEnabled(true);
```

## 任意多级缩放控制
```
 frescoPhotoView.setScaleFactorRetriever(new ScaleFactorFetcher() {
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
```
