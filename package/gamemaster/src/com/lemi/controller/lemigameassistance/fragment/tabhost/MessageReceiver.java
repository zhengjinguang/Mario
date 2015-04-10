package com.lemi.controller.lemigameassistance.fragment.tabhost;

import android.os.Bundle;

/**
 * Fragment implements this has the ability to handle arguments when the fragment is already
 * loaded in viewpager.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface MessageReceiver {

  /**
   * Fragment should handle the new args here, this method is always called after the fragment is
   * created.
   * 
   * @param args the args that passed to this fragment
   */
  void onMessageReceived(Bundle args);
}
