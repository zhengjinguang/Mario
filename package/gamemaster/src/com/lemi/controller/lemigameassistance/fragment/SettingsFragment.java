package com.lemi.controller.lemigameassistance.fragment;

import static com.lemi.controller.lemigameassistance.manager.UpgradeManager.UpgradeCallback;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.account.GameMasterAccountManager;
import com.lemi.controller.lemigameassistance.activity.SettingsActivity;
import com.lemi.controller.lemigameassistance.dialog.GameMasterDialog;
import com.lemi.controller.lemigameassistance.focus.utils.FocusUtils;
import com.lemi.controller.lemigameassistance.focus.view.TabFragment;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;
import com.lemi.controller.lemigameassistance.fragment.item.FragmentItem;
import com.lemi.controller.lemigameassistance.manager.UpgradeManager;
import com.lemi.controller.lemigameassistance.model.CheckVersionModel;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.controller.lemigameassistance.view.InnerScaleImageView;
import com.lemi.controller.lemigameassistance.view.StaggeredHorizontalCardContainer;
import com.lemi.mario.accountmanager.MarioAccountManager;
import com.lemi.mario.base.utils.ArrayUtil;
import com.lemi.mario.base.utils.DialogUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.StringUtil;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SettingsFragment extends BaseFragment implements TabFragment {

  /**
   * focus item constants
   */
  private static final int LEFT_FOCUS_ITEM = 0;
  private static final int RIGHT_FOCUS_ITEM = 4;
  private static final int DOWN_FOCUS_ITEM = 0;
  private static final int[] TOP_INDEX = {0, 1, 2, 4};
  private StaggeredHorizontalCardContainer staggeredHorizontalCardContainer;
  private InnerScaleImageView accountButton;
  private InnerScaleImageView settingButton;
  private InnerScaleImageView downloadManageButton;
  private InnerScaleImageView uninstallManageButton;
  private InnerScaleImageView checkVersionButton;
  private InnerScaleImageView aboutButton;
  private UpgradeCallback upgradeCallback = new UpgradeCallback() {
    @Override
    public void onNoUpgrade() {}

    @Override
    public void onUpgrade(CheckVersionModel versionModel) {}
  };
  private int itemFocusLayoutTreeIndex = 1;

  public SettingsFragment() {}

  @Override
  public void requestLeftFocus() {
    requestChildFocus(LEFT_FOCUS_ITEM);
  }

  @Override
  public void requestRightFocus() {
    requestChildFocus(RIGHT_FOCUS_ITEM);
  }

  @Override
  public void requestDownFocus() {
    requestChildFocus(DOWN_FOCUS_ITEM);
  }

  @Override
  public boolean isOnTop(View view) {
    int childIndex =
        staggeredHorizontalCardContainer.indexOfOriginalChild(FocusUtils.getParent(view,
            itemFocusLayoutTreeIndex));
    if (ArrayUtil.contains(TOP_INDEX, childIndex)) {
      return true;
    }
    return false;
  }

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    initListener();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.settings_fragment;
  }

  private void initView() {
    staggeredHorizontalCardContainer =
        (StaggeredHorizontalCardContainer) contentView.findViewById(R.id.setting_card_container);
    accountButton = (InnerScaleImageView) contentView.findViewById(R.id.account_button);
    settingButton = (InnerScaleImageView) contentView.findViewById(R.id.settings_button);
    downloadManageButton =
        (InnerScaleImageView) contentView.findViewById(R.id.download_manager_button);
    uninstallManageButton =
        (InnerScaleImageView) contentView.findViewById(R.id.uninstall_manage_button);
    checkVersionButton = (InnerScaleImageView) contentView.findViewById(R.id.check_version_button);
    aboutButton = (InnerScaleImageView) contentView.findViewById(R.id.about_button);
  }

  private void initListener() {
    accountButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isAdded()) {
          return;
        }
        LogHelper.managementCLick(LogHelper.MANAGEMENT_ACCOUNT);
        if (GameMasterAccountManager.getInstance().isLogined()) {
          SettingsActivity.launch(getActivity(), FragmentItem.ACCOUNT, null,
              StringUtil.getString(R.string.settings_account_title));
        } else {
          final Dialog dialogProgress =
              new GameMasterDialog.Builder(getActivity(), R.style.dialog_progress)
                  .setMessage(R.string.account_logining)
                  .create();
          dialogProgress.setCancelable(false);
          DialogUtils.showDialog(dialogProgress);
          GameMasterAccountManager.getInstance().registerOnLoginListener(
              new GameMasterAccountManager.OnLoginListener() {
                @Override
                public void onSucceed() {
                  SettingsActivity.launch(getActivity(), FragmentItem.ACCOUNT, null,
                      StringUtil.getString(R.string.settings_account_title));
                  MainThreadPostUtils.toast(R.string.account_login_success);
                  DialogUtils.dismissDialog(dialogProgress);
                  GameMasterAccountManager.getInstance().unRegisterOnLoginListener(this);
                }

                @Override
                public void onFail(MarioAccountManager.AccountError accountError, String reason) {
                  DialogUtils.dismissDialog(dialogProgress);
                  GameMasterAccountManager.getInstance().unRegisterOnLoginListener(this);
                  if (accountError == MarioAccountManager.AccountError.NOT_REGISTERED) {
                    MainThreadPostUtils.toast(StringUtil.getString(R.string.account_login_failed));
                  } else {
                    MainThreadPostUtils.toast(R.string.network_not_available);
                  }
                }
              });
          GameMasterAccountManager.getInstance().startLogin();
        }

      }
    });
    settingButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isAdded()) {
          return;
        }
        LogHelper.managementCLick(LogHelper.MANAGEMENT_SETTING);
        SettingsActivity.launch(getActivity(), FragmentItem.SETTINGS_DETAIL, null,
            StringUtil.getString(R.string.settings_setting_title));
      }
    });
    downloadManageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isAdded()) {
          return;
        }
        LogHelper.managementCLick(LogHelper.MANAGEMENT_DOWNLOAD);
        SettingsActivity.launch(getActivity(), FragmentItem.DOWNLOAD_MANAGE, null,
            StringUtil.getString(R.string.settings_download_manage_title));
      }
    });
    uninstallManageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isAdded()) {
          return;
        }
        LogHelper.managementCLick(LogHelper.MANAGEMENT_UNINSTALL);
        SettingsActivity.launch(getActivity(), FragmentItem.UNINSTALL, null,
            StringUtil.getString(R.string.settings_uninstall_manage_title));
      }
    });
    checkVersionButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isAdded()) {
          return;
        }
        LogHelper.managementCLick(LogHelper.MANAGEMENT_CHECK_VERSION);
        checkUpdate();
      }
    });
    aboutButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!isAdded()) {
          return;
        }
        LogHelper.managementCLick(LogHelper.MANAGEMENT_ABOUT);
        SettingsActivity.launch(getActivity(), FragmentItem.ABOUT, null,
            StringUtil.getString(R.string.settings_about_title));
      }
    });
  }

  private void requestChildFocus(int index) {
    if (staggeredHorizontalCardContainer != null) {
      View child = staggeredHorizontalCardContainer.getChildInOriginal(index);
      if (child != null) {
        child.requestFocus();
      }
    }
  }

  private synchronized void checkUpdate() {
    UpgradeManager.getInstance().checkUpgradeAndShowDialog(getActivity(), upgradeCallback, true);
  }

}
