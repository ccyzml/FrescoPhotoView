/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import androidx.core.view.ScrollingView;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.GenericDraweeHierarchyInflater;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.GenericDraweeView;
import com.zml.frescophotoview.gestures.TransformGestureDetector;

public class FrescoPhotoView extends GenericDraweeView
        implements ScrollingView{

    private final RectF mImageBounds = new RectF();
    private final RectF mViewBounds = new RectF();
    private ZoomableControllerImp mZoomableController;
    private GestureDetector mGestureDetector;
    private GestureListenerWrapper mTapListenerWrapper;

    private boolean mZoomingEnabled = true;

    private BaseControllerListener<Object> mOuterControllerListener;

    private final ControllerListener<Object> mControllerListener =
            new BaseControllerListener<Object>() {
                @Override
                public void onIntermediateImageSet(String id, Object imageInfo) {
                    super.onIntermediateImageSet(id, imageInfo);
                    if (mOuterControllerListener != null) {
                        mOuterControllerListener.onIntermediateImageSet(id,imageInfo);
                    }
                }

                @Override
                public void onFinalImageSet(
                        String id, @Nullable Object imageInfo, @Nullable Animatable animatable) {
                    FrescoPhotoView.this.onFinalImageSet();
                    if (mOuterControllerListener != null) {
                        mOuterControllerListener.onFinalImageSet(id,imageInfo,animatable);
                    }
                }

                @Override
                public void onRelease(String id) {
                    FrescoPhotoView.this.onRelease();
                    if (mOuterControllerListener != null) {
                        mOuterControllerListener.onRelease(id);
                    }
                }
            };

    private final ZoomableController.Listener mZoomableListener =
            new ZoomableController.Listener() {
                @Override
                public void onTransformChanged(Matrix transform) {
                    invalidate();
                }
            };

    public FrescoPhotoView(Context context) {
        super(context);
        inflateHierarchy(context, null);
        init();
    }

    public FrescoPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateHierarchy(context, attrs);
        init();
    }

    public FrescoPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflateHierarchy(context, attrs);
        init();
    }

    protected void inflateHierarchy(Context context, @Nullable AttributeSet attrs) {
        Resources resources = context.getResources();
        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(resources)
                        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
        GenericDraweeHierarchyInflater.updateBuilder(builder, context, attrs);
        setAspectRatio(builder.getDesiredAspectRatio());
        setHierarchy(builder.build());
    }

    private void init() {
        mZoomableController = createController();
        mZoomableController.setListener(mZoomableListener);
        mTapListenerWrapper = new GestureListenerWrapper(createGestureHandler());
        mGestureDetector = new GestureDetector(getContext(), mTapListenerWrapper);
        mGestureDetector.setIsLongpressEnabled(true);
    }

    protected ZoomableControllerImp createController() {
        return new ZoomableControllerImp(TransformGestureDetector.newInstance(),getContext());
    }

    protected DefaultGestureHandler createGestureHandler() {
        return new DefaultGestureHandler(this);
    }

    /**
     * Gets the original image bounds, in view-absolute coordinates.
     *
     * <p>The original image bounds are those reported by the hierarchy. The hierarchy itself may
     * apply scaling on its own (e.g. due to scale type) so the reported bounds are not necessarily
     * the same as the actual bitmap dimensions. In other words, the original image bounds correspond
     * to the image bounds within this view when no zoomable transformation is applied, but including
     * the potential scaling of the hierarchy. Having the actual bitmap dimensions abstracted away
     * from this view greatly simplifies implementation because the actual bitmap may change (e.g.
     * when a high-res image arrives and replaces the previously set low-res image). With proper
     * hierarchy scaling (e.g. FIT_CENTER), this underlying change will not affect this view nor the
     * zoomable transformation in any way.
     */
    protected void getImageBounds(RectF outBounds) {
        getHierarchy().getActualImageBounds(outBounds);
    }

    /**
     * Gets the bounds used to limit the translation, in view-absolute coordinates.
     *
     * <p>These bounds are passed to the zoomable controller in order to limit the translation. The
     * image is attempted to be centered within the limit bounds if the transformed image is smaller.
     * There will be no empty spaces within the limit bounds if the transformed image is bigger. This
     * applies to each dimension (horizontal and vertical) independently.
     *
     * <p>Unless overridden by a subclass, these bounds are same as the view bounds.
     */
    protected void getLimitBounds(RectF outBounds) {
        outBounds.set(0, 0, getWidth(), getHeight());
    }

    /**
     * Gets the zoomable controller.
     *
     * <p>Zoomable controller can be used to zoom to point, or to map point from view to image
     * coordinates for instance.
     */
    public ZoomableController getZoomableController() {
        return mZoomableController;
    }

    public void setHorizontalNestedScrollEnabled(boolean enabled) {
        mZoomableController.setHorizontalNestedScrollEnabled(enabled);
    }

    /**
     * Sets the tap listener.
     */
    public void setTapListener(GestureDetector.SimpleOnGestureListener tapListener) {
        mTapListenerWrapper.setListener(tapListener);
    }

    public void setZoomingEnabled(boolean zoomingEnabled) {
        mZoomingEnabled = zoomingEnabled;
        mZoomableController.setEnabled(false);
    }

    /**
     * Sets the image controller.
     */
    @Override
    public void setController(@Nullable DraweeController controller) {
        removeControllerListener(getController());
        addControllerListener(controller);
        super.setController(controller);
    }

    private void removeControllerListener(DraweeController controller) {
        if (controller instanceof AbstractDraweeController) {
            ((AbstractDraweeController) controller).removeControllerListener(mControllerListener);
        }
    }

    private void addControllerListener(DraweeController controller) {
        if (controller instanceof AbstractDraweeController) {
            ((AbstractDraweeController) controller).addControllerListener(mControllerListener);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.concat(mZoomableController.getActiveTransform());
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        //仅仅是想获取双击，Fling等回调，不关心返回值（默认都返回false)
        mGestureDetector.onTouchEvent(event);
        if (mZoomableController.onTouchEvent(event)) {
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        } else {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        // None of our components reported that they handled the touch event. Upon returning false
        // from this method, our parent won't send us any more events for this gesture. Unfortunately,
        // some components may have started a delayed action, such as a long-press timer, and since we
        // won't receive an ACTION_UP that would cancel that timer, a false event may be triggered.
        // To prevent that we explicitly send one last cancel event when returning false.
        MotionEvent cancelEvent = MotionEvent.obtain(event);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
        mGestureDetector.onTouchEvent(cancelEvent);
        mZoomableController.onTouchEvent(cancelEvent);
        cancelEvent.recycle();
        return false;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollVertically(direction);
    }

    @Override
    public int computeHorizontalScrollRange() {
        return mZoomableController.computeHorizontalScrollRange();
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return mZoomableController.computeHorizontalScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return mZoomableController.computeHorizontalScrollExtent();
    }

    @Override
    public int computeVerticalScrollRange() {
        return mZoomableController.computeVerticalScrollRange();
    }

    @Override
    public int computeVerticalScrollOffset() {
        return mZoomableController.computeVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtent() {
        return mZoomableController.computeVerticalScrollExtent();
    }

    @Override
    public void computeScroll() {
        mZoomableController.computeScroll();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateZoomableControllerBounds();
    }

    private void onFinalImageSet() {
        if (!mZoomableController.isEnabled() && mZoomingEnabled) {
            mZoomableController.setEnabled(true);
            updateZoomableControllerBounds();
        }
    }

    private void onRelease() {
        mZoomableController.setEnabled(false);
    }

    protected void updateZoomableControllerBounds() {
        getImageBounds(mImageBounds);
        getLimitBounds(mViewBounds);
        mZoomableController.setImageBounds(mImageBounds);
        mZoomableController.setViewBounds(mViewBounds);
    }

    public void setControllerListener(BaseControllerListener listener) {
        mOuterControllerListener = listener;
    }

    public void setMaxScaleFactor(float maxScale) {
        mZoomableController.setMaxScaleFactor(maxScale);
    }

    public void setAnimationDuration(long duration) {
        mZoomableController.setAnimationDuration(duration);
    }

    public void setScaleFactorRetriever(ScaleFactorRetriever scaleFactorRetriever) {
        mZoomableController.setScaleFactorFetcher(scaleFactorRetriever);
    }
}
