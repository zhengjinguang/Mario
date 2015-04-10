package com.lemi.controller.lemigameassistance.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.fragment.item.FragmentItem;
import com.lemi.controller.lemigameassistance.fragment.listener.OnBaseTitleFragmentChangeListener;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SettingsActivity extends BaseTitleFragmentActivity
    implements
    OnBaseTitleFragmentChangeListener {

  private static final String ARGUMENT_TYPE = "argumentType";
  private static final String ARGUMENT_BUNDLE = "argumentBundle";
  private static final String ARGUMENT_TITLE = "argumentTitle";

  private FragmentItem currentItem;


  /**
   * launch the activity for the FFActivity.
   *
   * @param context
   * @param ffItem the item want to launch.
   * @param args the args for the item.
   */
  public static void launch(final Context context, final FragmentItem ffItem, final Bundle args,
      String title) {
    if (context == null) {
      return;
    }

    if (ffItem == null) {
      throw new IllegalArgumentException("must set ffItem for activity");
    }

    final Intent intent = new Intent(context, SettingsActivity.class);

    // if context is application context, need set flag.
    if (!(context instanceof Activity)) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    intent.putExtra(ARGUMENT_TYPE, ffItem);
    intent.putExtra(ARGUMENT_TITLE, title);
    if (args != null) {
      intent.putExtra(ARGUMENT_BUNDLE, args);
    }

    context.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getIntent() != null) {
      Bundle args = getIntent().getBundleExtra(ARGUMENT_BUNDLE);
      FragmentItem item = (FragmentItem) getIntent().getSerializableExtra(ARGUMENT_TYPE);
      if (item != null) {
        currentItem = item;
        fragment =
            (com.lemi.controller.lemigameassistance.fragment.base.BaseFragment) Fragment
                .instantiate(this, item.getFragment().getName(), args);
        if (fragment != null) {
          FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
          transaction.replace(R.id.fragment_container, fragment);
          transaction.commit();
        }
      }
      setTitleText(getIntent().getStringExtra(ARGUMENT_TITLE));
    }

  }

  @Override
  protected String getTitleText() {
    return null;
  }

  @Override
  public void onFragmentChange(FragmentItem fragmentItem, Bundle args, boolean addToBackStack,
      String title) {
    if (fragmentItem == null) {
      return;
    }
    currentItem = fragmentItem;
    fragment =
        (com.lemi.controller.lemigameassistance.fragment.base.BaseFragment) Fragment.instantiate(
            this, fragmentItem.getFragment().getName(), args);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    if (addToBackStack) {
      transaction.addToBackStack(fragmentItem.name());
    }
    transaction.replace(R.id.fragment_container, fragment);
    transaction.commit();
    setTitleText(title);
  }

}
