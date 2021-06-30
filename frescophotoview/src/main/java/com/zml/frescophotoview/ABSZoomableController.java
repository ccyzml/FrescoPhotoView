/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoview;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import com.zml.frescophotoview.gestures.TransformGestureDetector;

/**
 * Zoomable controller that calculates transformation based on touch events.
 */
public abstract class ABSZoomableController
        implements ZoomableController, TransformGestureDetector.Listener {

    /**
     * Interface for handling call backs when the image bounds are set.
     */
    public interface ImageBoundsListener {
        void onImageBoundsSet(RectF imageBounds);
    }

    private static final float DEFAULT_MIN_SCALE_FACTOR = 1.0f;
    private static final float SCALE_FACTOR_UNDEFINE = 0f;

    protected static final float EPS = 1e-3f;

    private static final RectF IDENTITY_RECT = new RectF(0, 0, 1, 1);

    private TransformGestureDetector mGestureDetector;

    private @Nullable
    ImageBoundsListener mImageBoundsListener;

    private @Nullable
    Listener mListener = null;

    private boolean mIsEnabled = false;
    private boolean mIsScaleEnabled = true;
    private boolean mIsTranslationEnabled = true;

    private float mMinScaleFactor = DEFAULT_MIN_SCALE_FACTOR;
    private float mMaxScaleFactor = SCALE_FACTOR_UNDEFINE;
    private float mRecommendedMaxScaleFactor = SCALE_FACTOR_UNDEFINE;

    // View bounds, in view-absolute coordinates
    private final RectF mViewBounds = new RectF();
    // Non-transformed image bounds, in view-absolute coordinates
    private final RectF mImageBounds = new RectF();
    // Transformed image bounds, in view-absolute coordinates
    private final RectF mTransformedImageBounds = new RectF();

    private final Matrix mActiveTransform = new Matrix();
    private final Matrix mActiveTransformInverse = new Matrix();
    private final float[] mTempValues = new float[9];

    public ABSZoomableController(TransformGestureDetector gestureDetector) {
        mGestureDetector = gestureDetector;
        mGestureDetector.setListener(this);
    }

    /**
     * Rests the controller.
     */
    public void reset() {
        mGestureDetector.resetScale();
        mActiveTransform.reset();
        onTransformChanged();
    }

    /**
     * Sets the zoomable listener.
     */
    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Sets whether the controller is enabled or not.
     */
    @Override
    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        if (!enabled) {
            reset();
        }
    }

    /**
     * Gets whether the controller is enabled or not.
     */
    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    /**
     * Sets whether the scale gesture is enabled or not.
     */
    public void setScaleEnabled(boolean enabled) {
        mIsScaleEnabled = enabled;
    }

    /**
     * Gets whether the scale gesture is enabled or not.
     */
    public boolean isScaleEnabled() {
        return mIsScaleEnabled;
    }

    /**
     * Sets whether the translation gesture is enabled or not.
     */
    public void setTranslationEnabled(boolean enabled) {
        mIsTranslationEnabled = enabled;
    }

    /**
     * Gets whether the translations gesture is enabled or not.
     */
    public boolean isTranslationEnabled() {
        return mIsTranslationEnabled;
    }

    /**
     * Sets the minimum scale factor allowed.
     *
     * <p>Hierarchy's scaling (if any) is not taken into account.
     */
    public void setMinScaleFactor(float minScaleFactor) {
        if (minScaleFactor < 1 || minScaleFactor > mMaxScaleFactor) return;
        mMinScaleFactor = minScaleFactor;
    }

    /**
     * Gets the minimum scale factor allowed.
     */
    public float getMinScaleFactor() {
        return mMinScaleFactor;
    }

    /**
     * Sets the maximum scale factor allowed.
     *
     * <p>Hierarchy's scaling (if any) is not taken into account.
     */
    public void setMaxScaleFactor(float maxScaleFactor) {
        if (maxScaleFactor < 1 || maxScaleFactor < mMinScaleFactor) return;
        mMaxScaleFactor = maxScaleFactor;
    }

    /**
     * Gets the maximum scale factor allowed.
     */
    public float getMaxScaleFactor() {
        return mMaxScaleFactor == SCALE_FACTOR_UNDEFINE ? mRecommendedMaxScaleFactor : mMaxScaleFactor;
    }

    /**
     * Gets the current scale factor.
     */
    @Override
    public float getScaleFactor() {
        return getMatrixScaleFactor(mActiveTransform);
    }

    /**
     * Sets the image bounds, in view-absolute coordinates.
     */
    @Override
    public void setImageBounds(RectF imageBounds) {
        if (!imageBounds.equals(mImageBounds)) {
            mImageBounds.set(imageBounds);
            updateRecommendedMaxScale();
            onTransformChanged();
            if (mImageBoundsListener != null) {
                mImageBoundsListener.onImageBoundsSet(mImageBounds);
            }
        }
    }

    private void updateRecommendedMaxScale() {
        RectF viewBounds = getViewBounds();
        RectF imageBounds = getImageBounds();
        if (imageBounds.width() > viewBounds.width() || imageBounds.height() > viewBounds.height()) {
            mRecommendedMaxScaleFactor = Math.max(imageBounds.width() / viewBounds.width(), imageBounds.height() / viewBounds.height());
        } else {
            mRecommendedMaxScaleFactor = Math.max(viewBounds.width() / imageBounds.width(), viewBounds.height() / imageBounds.height());
        }
    }

    /**
     * Gets the non-transformed image bounds, in view-absolute coordinates.
     */
    public RectF getImageBounds() {
        return mImageBounds;
    }

    /**
     * Gets the transformed image bounds, in view-absolute coordinates
     */
    protected RectF getTransformedImageBounds() {
        return mTransformedImageBounds;
    }

    /**
     * Sets the view bounds.
     */
    @Override
    public void setViewBounds(RectF viewBounds) {
        mViewBounds.set(viewBounds);
        updateRecommendedMaxScale();
    }

    /**i
     * Gets the view bounds.
     */
    public RectF getViewBounds() {
        return mViewBounds;
    }

    /**
     * Sets the image bounds listener.
     */
    public void setImageBoundsListener(@Nullable ImageBoundsListener imageBoundsListener) {
        mImageBoundsListener = imageBoundsListener;
    }

    /**
     * Gets the image bounds listener.
     */
    public @Nullable
    ImageBoundsListener getImageBoundsListener() {
        return mImageBoundsListener;
    }

    /**
     * Returns true if the zoomable transform is identity matrix.
     */
    @Override
    public boolean isIdentity() {
        return isMatrixIdentity(mActiveTransform, 1e-3f);
    }

    @Override
    public Matrix getActiveTransform() {
        return mActiveTransform;
    }

    /**
     * Gets the matrix that transforms image-relative coordinates to view-absolute coordinates. The
     * zoomable transformation is taken into account.
     */
    public void getImageRelativeToViewAbsoluteTransform(Matrix outMatrix) {
        outMatrix.setRectToRect(IDENTITY_RECT, mTransformedImageBounds, Matrix.ScaleToFit.FILL);
    }

    /**
     * Maps point from view-absolute to image-relative coordinates. This takes into account the
     * zoomable transformation.
     */
    protected PointF mapViewToImage(PointF viewPoint) {
        float[] points = mTempValues;
        points[0] = viewPoint.x;
        points[1] = viewPoint.y;
        mActiveTransform.invert(mActiveTransformInverse);
        mActiveTransformInverse.mapPoints(points, 0, points, 0, 1);
        mapAbsoluteToRelative(points, points, 1);
        return new PointF(points[0], points[1]);
    }

    /**
     * Maps point from image-relative to view-absolute coordinates. This takes into account the
     * zoomable transformation.
     */
    protected PointF mapImageToView(PointF imagePoint) {
        float[] points = mTempValues;
        points[0] = imagePoint.x;
        points[1] = imagePoint.y;
        mapRelativeToAbsolute(points, points, 1);
        mActiveTransform.mapPoints(points, 0, points, 0, 1);
        return new PointF(points[0], points[1]);
    }

    /**
     * Maps array of 2D points from view-absolute to image-relative coordinates. This does NOT take
     * into account the zoomable transformation. Points are represented by a float array of [x0, y0,
     * x1, y1, ...].
     *
     * @param destPoints destination array (may be the same as source array)
     * @param srcPoints  source array
     * @param numPoints  number of points to map
     */
    protected void mapAbsoluteToRelative(float[] destPoints, float[] srcPoints, int numPoints) {
        for (int i = 0; i < numPoints; i++) {
            destPoints[i * 2 + 0] = (srcPoints[i * 2 + 0] - mImageBounds.left) / mImageBounds.width();
            destPoints[i * 2 + 1] = (srcPoints[i * 2 + 1] - mImageBounds.top) / mImageBounds.height();
        }
    }

    /**
     * Maps array of 2D points from image-relative to view-absolute coordinates. This does NOT take
     * into account the zoomable transformation. Points are represented by float array of [x0, y0, x1,
     * y1, ...].
     *
     * @param destPoints destination array (may be the same as source array)
     * @param srcPoints  source array
     * @param numPoints  number of points to map
     */
    protected void mapRelativeToAbsolute(float[] destPoints, float[] srcPoints, int numPoints) {
        for (int i = 0; i < numPoints; i++) {
            destPoints[i * 2 + 0] = srcPoints[i * 2 + 0] * mImageBounds.width() + mImageBounds.left;
            destPoints[i * 2 + 1] = srcPoints[i * 2 + 1] * mImageBounds.height() + mImageBounds.top;
        }
    }

    /**
     * Sets a new zoom transformation.
     */
    public void setTransform(Matrix newTransform) {
        mActiveTransform.set(newTransform);
        onTransformChanged();
    }

    /**
     * Gets the gesture detector.
     */
    protected TransformGestureDetector getDetector() {
        return mGestureDetector;
    }

    /**
     * Notifies controller of the received touch event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsEnabled) {
            return mGestureDetector.onTouchEvent(event);
        }
        return false;
    }

    /* TransformGestureDetector.Listener methods  */
    @Override
    public void onGestureBegin(TransformGestureDetector detector) {
    }


    @Override
    public void onGestureUpdate(TransformGestureDetector detector) {
        onTransformChanged();
    }

    @Override
    public void onGestureEnd(TransformGestureDetector detector) {
    }

    protected void onTransformChanged() {
        mActiveTransform.mapRect(mTransformedImageBounds, mImageBounds);
        if (mListener != null && isEnabled()) {
            mListener.onTransformChanged(mActiveTransform);
        }
    }


    /**
     * Gets the scale factor for the given matrix. This method assumes the equal scaling factor for X
     * and Y axis.
     */
    protected float getMatrixScaleFactor(Matrix transform) {
        transform.getValues(mTempValues);
        return mTempValues[Matrix.MSCALE_X];
    }

    /**
     * Same as {@code Matrix.isIdentity()}, but with tolerance {@code eps}.
     */
    private boolean isMatrixIdentity(Matrix transform, float eps) {
        // Checks whether the given matrix is close enough to the identity matrix:
        //   1 0 0
        //   0 1 0
        //   0 0 1
        // Or equivalently to the zero matrix, after subtracting 1.0f from the diagonal elements:
        //   0 0 0
        //   0 0 0
        //   0 0 0
        transform.getValues(mTempValues);
        mTempValues[0] -= 1.0f; // m00
        mTempValues[4] -= 1.0f; // m11
        mTempValues[8] -= 1.0f; // m22
        for (int i = 0; i < 9; i++) {
            if (Math.abs(mTempValues[i]) > eps) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int computeHorizontalScrollRange() {
        return (int) mTransformedImageBounds.width();
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return (int) (mViewBounds.left - mTransformedImageBounds.left);
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return (int) mViewBounds.width();
    }

    @Override
    public int computeVerticalScrollRange() {
        return (int) mTransformedImageBounds.height();
    }

    @Override
    public int computeVerticalScrollOffset() {
        return (int) (mViewBounds.top - mTransformedImageBounds.top);
    }

    @Override
    public int computeVerticalScrollExtent() {
        return (int) mViewBounds.height();
    }

    @Override
    public void computeScroll() {

    }

    public Listener getListener() {
        return mListener;
    }

    public boolean isImageInLeftEdge() {
        return Math.abs(mTransformedImageBounds.left - mViewBounds.left) < EPS;
    }

    public boolean isImageInRightEdge() {
        return Math.abs(mTransformedImageBounds.right - mViewBounds.right) < EPS;
    }

    public boolean isImageInTopEdge() {
        return Math.abs(mTransformedImageBounds.top - mViewBounds.top) < EPS;
    }

    public boolean isImageInBottomEdge() {
        return Math.abs(mTransformedImageBounds.bottom - mViewBounds.bottom) < EPS;
    }
}
