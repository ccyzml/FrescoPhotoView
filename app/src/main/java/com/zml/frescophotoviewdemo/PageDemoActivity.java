/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.zml.frescophotoviewdemo;

import android.app.Activity;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;


public class PageDemoActivity extends Activity {

  private MyPagerAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_page_demo);
    ViewPager pager =  findViewById(R.id.pager);
    mAdapter = new MyPagerAdapter(pager.getChildCount());
    pager.setAdapter(mAdapter);
  }

}
