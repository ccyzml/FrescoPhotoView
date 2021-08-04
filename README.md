# FrescoPhotoView

![](https://upload-images.jianshu.io/upload_images/7523005-91121ef21b4dd590.gif?imageMogr2/auto-orient/strip|imageView2/2/w/320/format/webp)



![](https://upload-images.jianshu.io/upload_images/7523005-b929a6d09bcbbf72.gif?imageMogr2/auto-orient/strip|imageView2/2/w/320/format/webp)

Download

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```css
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```css
	dependencies {
	        implementation 'com.github.ccyzml:FrescoPhotoView:1.0.0'
	}
```



## Usage

```kotlin
frescoPhotoView = findViewById(R.id.photo_view);
DraweeController controller = Fresco.newDraweeControllerBuilder()
                              .setUri("https://www.gstatic.com/webp/gallery/1.sm.jpg")
                              .build();
frescoPhotoView.setController(controller);
```

## 
