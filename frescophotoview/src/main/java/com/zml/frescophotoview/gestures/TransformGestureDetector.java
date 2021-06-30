/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoview.gestures;

import android.view.MotionEvent;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Component that detects translation, scale and rotation based on touch events.
 *
 * <p>This class notifies its listeners whenever a gesture begins, updates or ends. The instance of
 * this detector is passed to the listeners, so it can be queried for pivot, translation, scale or
 * rotation.
 */
public class TransformGestureDetector implements MultiPointerGestureDetector.Listener {

    /**
     * The listener for receiving notifications when gestures occur.
     */
    public interface Listener {
        /**
         * A callback called right before the gesture is about to start.
         */
        void onGestureBegin(TransformGestureDetector detector);

        /**
         * A callback called each time the gesture gets updated.
         */
        void onGestureUpdate(TransformGestureDetector detector);

        /**
         * A callback called right after the gesture has finished.
         */
        void onGestureEnd(TransformGestureDetector detector);
    }

    private final MultiPointerGestureDetector mDetector;
    private static final int GESTURE_DIRECTION_EPS = 10;


    @Nullable
    private Listener mListener = null;

    private TransformGestureDetector(MultiPointerGestureDetector multiPointerGestureDetector) {
        mDetector = multiPointerGestureDetector;
        mDetector.setListener(this);
    }

    /**
     * Factory method that creates a new instance of TransformGestureDetector
     */
    public static TransformGestureDetector newInstance() {
        return new TransformGestureDetector(MultiPointerGestureDetector.newInstance());
    }

    /**
     * Sets the listener.
     *
     * @param listener listener to set
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Handles the given motion event.
     *
     * @param event event to handle
     * @return whether or not the event was handled
     */
    public boolean onTouchEvent(final MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private float mScale = 1;
    private float mTranslationX = 0;
    private float mTranslationY = 0;
    private float mPivotX = 0;
    private float mPivotY = 0;

    //手势刚开始时的意图方向，单个手指
    public static final int GESTURE_INTENT_DIRECTION_IGNORE = -1;
    public static final int GESTURE_INTENT_DIRECTION_UNDEFINE = 0;
    public static final int GESTURE_INTENT_DIRECTION_LEFT = 1;
    public static final int GESTURE_INTENT_DIRECTION_UP = 2;
    public static final int GESTURE_INTENT_DIRECTION_RIGHT = 3;
    public static final int GESTURE_INTENT_DIRECTION_DOWN = 4;
    private int mGestureIntent = GESTURE_INTENT_DIRECTION_UNDEFINE;


    @Override
    public void onGestureUpdate(MultiPointerGestureDetector detector) {
        if (!mDetector.isSingleFinger()) {
            float startDeltaX = mDetector.getStartX()[1] - mDetector.getStartX()[0];
            float startDeltaY = mDetector.getStartY()[1] - mDetector.getStartY()[0];
            float currentDeltaX = mDetector.getCurrentX()[1] - mDetector.getCurrentX()[0];
            float currentDeltaY = mDetector.getCurrentY()[1] - mDetector.getCurrentY()[0];
            float startDist = (float) Math.hypot(startDeltaX, startDeltaY);
            float currentDist = (float) Math.hypot(currentDeltaX, currentDeltaY);
            mScale = currentDist / startDist;
            mPivotX = calcAverage(mDetector.getStartX(), mDetector.getPointerCount());
            mPivotY = calcAverage(mDetector.getStartY(), mDetector.getPointerCount());
        } else {
            mScale = 1;
        }
        mTranslationX += calcAverage(mDetector.getCurrentX(), mDetector.getPointerCount())
                - calcAverage(mDetector.getLastX(), mDetector.getPointerCount());
        mTranslationY += calcAverage(mDetector.getCurrentY(), mDetector.getPointerCount())
                - calcAverage(mDetector.getLastY(), mDetector.getPointerCount());
        if (mDetector.isSingleFinger()) {
            if (mGestureIntent == GESTURE_INTENT_DIRECTION_UNDEFINE) {
                if (Math.abs(getTranslationX()) - Math.abs(getTranslationY()) >= 0) {
                    if (getTranslationX() < -GESTURE_DIRECTION_EPS) {
                        if (!mIgnoreGestureIntents.contains(GESTURE_INTENT_DIRECTION_LEFT)) {
                            mGestureIntent = GESTURE_INTENT_DIRECTION_LEFT;
                        }
                    }
                    if (getTranslationX() > GESTURE_DIRECTION_EPS) {
                        if (!mIgnoreGestureIntents.contains(GESTURE_INTENT_DIRECTION_RIGHT)) {
                            mGestureIntent = GESTURE_INTENT_DIRECTION_RIGHT;
                        }
                    }
                } else {
                    if (getTranslationY() < - GESTURE_DIRECTION_EPS) {
                        if (!mIgnoreGestureIntents.contains(GESTURE_INTENT_DIRECTION_UP)) {
                            mGestureIntent = GESTURE_INTENT_DIRECTION_UP;
                        }
                    }
                    if (getTranslationY() > GESTURE_DIRECTION_EPS) {
                        if (!mIgnoreGestureIntents.contains(GESTURE_INTENT_DIRECTION_DOWN)) {
                            mGestureIntent = GESTURE_INTENT_DIRECTION_DOWN;
                        }
                    }
                }
            }
        } else {
                mGestureIntent = GESTURE_INTENT_DIRECTION_IGNORE;
        }
        if (mListener != null) {
            mListener.onGestureUpdate(this);
        }
    }

    public void resetScale() {
        mScale = 1;
    }

    public void resetTranslate() {
        mTranslationY = 0;
        mTranslationX = 0;
    }

    @Override
    public void onGestureBegin(MultiPointerGestureDetector detector) {
        if (mListener != null) {
            mListener.onGestureBegin(this);
        }
    }

    @Override
    public void onGestureEnd(MultiPointerGestureDetector detector) {
        if (mListener != null) {
            mListener.onGestureEnd(this);
        }
        resetScale();
        resetTranslate();
        mPivotX = 0;
        mPivotY = 0;
        mGestureIntent = GESTURE_INTENT_DIRECTION_UNDEFINE;
    }

    private float calcAverage(float[] arr, int len) {
        float sum = 0;
        for (int i = 0; i < len; i++) {
            sum += arr[i];
        }
        return (len > 0) ? sum / len : 0;
    }

    /**
     * Gets whether there is a gesture in progress
     */
    public boolean isGestureInProgress() {
        return mDetector.isGestureInProgress();
    }

    /**
     * Gets the number of pointers after the current gesture
     */
    public int getNewPointerCount() {
        return mDetector.getNewPointerCount();
    }

    /**
     * Gets the number of pointers in the current gesture
     */
    public int getPointerCount() {
        return mDetector.getPointerCount();
    }

    /**
     * Gets the X coordinate of the pivot point
     */
    public float getPivotX() {
        return mPivotX;
    }

    /**
     * Gets the Y coordinate of the pivot point
     */
    public float getPivotY() {
        return mPivotY;
    }

    /**
     * Gets the X component of the translation
     */
    public float getTranslationX() {
        return mTranslationX;
    }

    /**
     * Gets the Y component of the translation
     */
    public float getTranslationY() {
        return mTranslationY;
    }

    public float getScale() {
        return mScale;
    }

    public void resetGestureStart() {
        mDetector.resetGestureStart();
    }

    /**
     * Gets the rotation in radians
     */
    public float getRotation() {
        if (mDetector.getPointerCount() < 2) {
            return 0;
        } else {
            float startDeltaX = mDetector.getStartX()[1] - mDetector.getStartX()[0];
            float startDeltaY = mDetector.getStartY()[1] - mDetector.getStartY()[0];
            float currentDeltaX = mDetector.getCurrentX()[1] - mDetector.getCurrentX()[0];
            float currentDeltaY = mDetector.getCurrentY()[1] - mDetector.getCurrentY()[0];
            float startAngle = (float) Math.atan2(startDeltaY, startDeltaX);
            float currentAngle = (float) Math.atan2(currentDeltaY, currentDeltaX);
            return currentAngle - startAngle;
        }
    }

    public int getGestureIntent() {
        return mGestureIntent;
    }

    private final List<Integer> mIgnoreGestureIntents = new ArrayList();

    //如果手势意图被忽视那么判断手势时会跳过该动作
    public void ignoreGestureIntent(int gestureIntent) {
        if (!mIgnoreGestureIntents.contains(gestureIntent)) {
            mIgnoreGestureIntents.add(gestureIntent);
        }
    }
}
