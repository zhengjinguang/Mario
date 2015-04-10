package com.lemi.controller.lemigameassistance.activity;


import android.os.Bundle;

import com.lemi.controller.lemigameassistance.fragment.WelcomeFragment;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class WelcomeActivity extends BaseFragmentActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    fragment = new WelcomeFragment();
    replaceFragment(fragment);
  }
}
