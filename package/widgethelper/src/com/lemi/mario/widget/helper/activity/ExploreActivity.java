package com.lemi.mario.widget.helper.activity;

import android.os.Bundle;

import com.lemi.mario.widget.helper.activity.base.BaseFragmentActivity;
import com.lemi.mario.widget.helper.fragment.ExploreFragment;
import com.lemi.mario.widget.helper.utils.LogHelper;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ExploreActivity extends BaseFragmentActivity {


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fragment = new ExploreFragment();
    replaceFragment(fragment);
    long currentTime = System.currentTimeMillis();
    // in order to make lunch event is pair
    LogHelper.launch(currentTime);
    LogHelper.backToBackground(currentTime + 1);
  }
}
