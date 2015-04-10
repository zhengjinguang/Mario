package com.lemi.controller.lemigameassistance.fragment.listener;

import android.os.Bundle;

import com.lemi.controller.lemigameassistance.fragment.item.FragmentItem;

/**
 * A listener for notify the fragment need to change to another.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface OnBaseTitleFragmentChangeListener {

  /**
   * The callback for fragment.
   *
   * @param ffItem the item want to change.
   * @param args the args for the item.
   * @param addToBackStack if need add to backStack.
   *          if set true, the transaction in which fragments are modified can be placed on an
   *          internal
   *          back-stack of the owning activity. When the user presses back in the activity,
   *          any transactions on the back stack are popped off before the activity itself is
   *          finished.
   *          otherwise, will not add to the back-stack.
   * @param title title to change
   */
  public void onFragmentChange(FragmentItem ffItem, Bundle args, boolean addToBackStack,
      String title);
}
