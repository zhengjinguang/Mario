package com.lemi.controller.lemigameassistance.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class BaseFragmentActivity extends FragmentActivity {

  protected BaseFragment fragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutId());
  }

  protected void replaceFragment(Fragment newFragment) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.fragment_container, newFragment);
    transaction.commit();
  }

  protected int getLayoutId() {
    return R.layout.base_fragment_activity;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (fragment != null) {
      if (fragment.onKeyDown(keyCode, event)) {
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }
}
