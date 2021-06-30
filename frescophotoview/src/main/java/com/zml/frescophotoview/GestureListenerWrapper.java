/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoview;

import android.view.GestureDetector;
import android.view.MotionEvent;


/** Wrapper for SimpleOnGestureListener as GestureDetector does not allow changing its listener. */
public class GestureListenerWrapper extends GestureDetector.SimpleOnGestureListener {

  private GestureDetector.SimpleOnGestureListener mOuterListener;
  private DefaultGestureHandler mDefaultGestureHandler;

  public GestureListenerWrapper(DefaultGestureHandler defaultGestureHandler) {
    mDefaultGestureHandler = defaultGestureHandler;
  }

  public void setListener(GestureDetector.SimpleOnGestureListener listener) {
    mOuterListener = listener;
  }

  @Override
  public void onLongPress(MotionEvent e) {
    if (mOuterListener != null) {
      mOuterListener.onLongPress(e);
    }
    mDefaultGestureHandler.onLongPress(e);
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    if (mOuterListener != null) {
      mOuterListener.onScroll(e1, e2, distanceX, distanceY);
    }
    return mDefaultGestureHandler.onScroll(e1, e2, distanceX, distanceY);
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    if (mOuterListener != null) {
      mOuterListener.onFling(e1, e2, velocityX, velocityY);
    }
    return mDefaultGestureHandler.onFling(e1, e2, velocityX, velocityY);
  }

  @Override
  public void onShowPress(MotionEvent e) {
    if (mOuterListener != null) {
      mOuterListener.onShowPress(e);
    }
    mDefaultGestureHandler.onShowPress(e);
  }

  @Override
  public boolean onDown(MotionEvent e) {
    if (mOuterListener != null) {
      mOuterListener.onDown(e);
    }
    return mDefaultGestureHandler.onDown(e);
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {
    if (mOuterListener != null) {
      mOuterListener.onDoubleTap(e);
    }
    return mDefaultGestureHandler.onDoubleTap(e);
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent e) {
    if (mOuterListener != null) {
      mOuterListener.onDoubleTapEvent(e);
    }
    return mDefaultGestureHandler.onDoubleTapEvent(e);
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
    if (mOuterListener != null) {
      mOuterListener.onSingleTapConfirmed(e);
    }
    return mDefaultGestureHandler.onSingleTapConfirmed(e);
  }

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    if (mOuterListener != null) {
      mOuterListener.onSingleTapUp(e);
    }
    return mDefaultGestureHandler.onSingleTapUp(e);
  }
}
