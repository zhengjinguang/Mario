package com.lemi.controller.lemigameassistance.fragment.item;

import android.support.v4.app.Fragment;

import com.lemi.controller.lemigameassistance.fragment.AboutFragment;
import com.lemi.controller.lemigameassistance.fragment.AccountDetailFragment;
import com.lemi.controller.lemigameassistance.fragment.DownloadManageFragment;
import com.lemi.controller.lemigameassistance.fragment.GameDetailFragment;
import com.lemi.controller.lemigameassistance.fragment.SettingsDetailFragment;
import com.lemi.controller.lemigameassistance.fragment.UninstallManageFragment;

/**
 * A enum for the activity support.
 * All fragment add in this enum, need contain the default (that is, zero-argument)
 * constructor. If there is no such constructor, or if the creation fails (either because of
 * a lack of available memory or because an exception is thrown by the
 * constructor), an {@code InstantiationException} is thrown. If the default
 * constructor exists but is not accessible from the context where this
 * method is invoked, an {@code IllegalAccessException} is thrown.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public enum FragmentItem {

  GAME_DETAIL(GameDetailFragment.class),
  DOWNLOAD_MANAGE(DownloadManageFragment.class),
  UNINSTALL(UninstallManageFragment.class),
  SETTINGS_DETAIL(SettingsDetailFragment.class),
  ACCOUNT(AccountDetailFragment.class),
  ABOUT(AboutFragment.class);

  private final Class<? extends Fragment> fragment;

  private FragmentItem(Class<? extends Fragment> fragment) {
    this.fragment = fragment;
  }

  public Class<? extends Fragment> getFragment() {
    return fragment;
  }

}
