package com.lemi.mario.widget.helper.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.StringUtil;
import com.lemi.mario.widget.helper.R;
import com.lemi.mario.widget.helper.activity.MountActivity;
import com.lemi.mario.widget.helper.dialog.WidgetHelperAlertDialog;
import com.lemi.mario.widget.helper.fragment.base.BaseFragment;
import com.lemi.mario.widget.helper.manager.GiftCardManager;
import com.lemi.mario.widget.helper.manager.SDCardManager;
import com.lemi.mario.widget.helper.model.LocalGameModel;
import com.lemi.mario.widget.helper.view.ActiveImageView;
import com.lemi.mario.widget.helper.view.ApiUtils;
import com.lemi.mario.widget.helper.view.BottomTipsView;
import com.lemi.mario.widget.helper.view.InstallGameCard;
import com.lemi.mario.widget.helper.view.InstallGameLineContainer;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InstallFragment extends BaseFragment {

  private static final int COLUMN_NUM = 5;
  private static final int LINE_PER_PAGE = 2;
  private static final int PAGE_NUM = COLUMN_NUM * LINE_PER_PAGE;

  private int lineMargin = 0;
  private int scrollDistance = 0;

  private ActiveImageView installAll;
  private ActiveImageView cancel;
  private LinearLayout gameContainer;
  private ScrollView containerScrollView;
  private BottomTipsView bottomTipsView;
  private ImageView back;
  private ImageView invalidView;

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    initScrollConfig();
    initStatus();
    setListener();
    initFocus();
    List<LocalGameModel> localGameModels = loadGames();
    if (checkGamesValid(localGameModels)) {
      invalidView.setVisibility(View.GONE);
      bind(localGameModels);
    } else {
      invalidView.setVisibility(View.VISIBLE);
    }
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.install_fragment;
  }

  private void initView() {
    installAll = (ActiveImageView) contentView.findViewById(R.id.install_start_all);
    cancel = (ActiveImageView) contentView.findViewById(R.id.install_cancel);
    gameContainer = (LinearLayout) contentView.findViewById(R.id.install_game_container);
    containerScrollView = (ScrollView) contentView.findViewById(R.id.install_scroll_view);
    bottomTipsView = (BottomTipsView) contentView.findViewById(R.id.install_tips);
    back = (ImageView) contentView.findViewById(R.id.back_button);
    invalidView = (ImageView) contentView.findViewById(R.id.install_invalid_view);
  }

  private void initStatus() {
    checkMountStatusValid();
    if (GiftCardManager.getInstance().isInstalling()) {
      installAll.setAction(ActiveImageView.Action.SLEEP);
      cancel.setAction(ActiveImageView.Action.ACTIVE);
      if (!TextUtils.isEmpty(GiftCardManager.getInstance().getInstallingName())) {
        showInstallTips();
      } else {
        hideTips();
      }
    } else {
      installAll.setAction(ActiveImageView.Action.ACTIVE);
      cancel.setAction(ActiveImageView.Action.SLEEP);
      hideTips();
    }
  }

  private void initScrollConfig() {
    lineMargin = (int) getResources().getDimension(R.dimen.mario_10dp);
  }

  private void setListener() {
    installAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (installAll.getAction() == ActiveImageView.Action.ACTIVE) {
          if (!checkMountStatusValid()) {
            return;
          }
          installAll.setAction(ActiveImageView.Action.SLEEP);
          cancel.setAction(ActiveImageView.Action.ACTIVE);
          scrollToTop();
          GiftCardManager.getInstance().startInstallAll();
        } else {
          MainThreadPostUtils.toast(R.string.install_processing);
        }
      }
    });
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (cancel.getAction() == ActiveImageView.Action.ACTIVE) {
          cancel.setAction(ActiveImageView.Action.SLEEP);
          GiftCardManager.getInstance().cancel();
          showCancelingTips();
        }
      }
    });
    back.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isAdded()) {
          getActivity().finish();
        }
      }
    });
    GiftCardManager.getInstance().setGiftInstallListener(giftInstallListener);
  }

  private void initFocus() {
    installAll.requestFocus();
  }

  private List<LocalGameModel> loadGames() {
    return GiftCardManager.getInstance().getGiftFromLocal();
  }

  private boolean checkGamesValid(List<LocalGameModel> gameModels) {
    if (CollectionUtils.isEmpty(gameModels)) {
      return false;
    }
    return true;
  }

  private void bind(List<LocalGameModel> gameModels) {
    int lineNum = gameModels.size() / COLUMN_NUM;
    if (gameModels.size() % COLUMN_NUM != 0) {
      lineNum++;
    }
    alignColumn(gameModels);
    for (int i = 0; i < lineNum; i++) {
      InstallGameLineContainer lineContainer = InstallGameLineContainer.newInstance(gameContainer);
      for (int j = i * COLUMN_NUM; j < max(i * COLUMN_NUM + COLUMN_NUM, gameModels.size()); j++) {
        InstallGameCard card = InstallGameCard.newInstance(lineContainer);
        card.bind(ApiUtils.convertToGameModel(gameModels.get(j)));
        lineContainer.addView(card);
      }
      if (i != 0) {
        addLineSpace(lineContainer);
      }
      gameContainer.addView(lineContainer);
    }
  }

  private void addLineSpace(View view) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    params.topMargin = lineMargin;
    view.setLayoutParams(params);
  }

  private void alignColumn(List<LocalGameModel> gameModels) {
    int lineNum = gameModels.size() / COLUMN_NUM;
    if (gameModels.size() % COLUMN_NUM != 0) {
      lineNum++;
    } else {
      return;
    }
    for (int i = gameModels.size(); i < lineNum * COLUMN_NUM; i++) {
      gameModels.add(new LocalGameModel());
    }
  }

  private int max(int index, int len) {
    return index >= len ? len : index;
  }

  private GiftCardManager.GiftInstallListener giftInstallListener =
      new GiftCardManager.GiftInstallListener() {
        @Override
        public void onInstallAllComplete() {
          if (installAll != null) {
            installAll.setAction(ActiveImageView.Action.ACTIVE);
          }
          if (cancel != null) {
            cancel.setAction(ActiveImageView.Action.SLEEP);
          }
          showCompleteTips();
        }

        @Override
        public void onInstallAllCancel() {
          if (installAll != null) {
            installAll.setAction(ActiveImageView.Action.ACTIVE);
          }
          if (cancel != null) {
            cancel.setAction(ActiveImageView.Action.SLEEP);
          }
          showCancelCompleteTips();
        }

        @Override
        public void onItemInstall(int position, LocalGameModel model) {
          showInstallTips();
          if (isNeedScroll(position)) {
            scrollToNext();
          }
        }

        @Override
        public void onInstallError(GiftCardManager.InstallError installError) {
          if (installAll != null) {
            installAll.setAction(ActiveImageView.Action.ACTIVE);
          }
          if (cancel != null) {
            cancel.setAction(ActiveImageView.Action.SLEEP);
          }
          showErrorTips();
        }
      };

  private boolean isNeedScroll(int position) {
    if (position != 0 && position % PAGE_NUM == 0) {
      int installPage = position / PAGE_NUM;
      if (containerScrollView.getScrollY() == getScrollDistance() * (installPage - 1)) {
        return true;
      }
    }
    return false;
  }

  private void scrollToNext() {
    scrollDistance = getScrollDistance();
    if (scrollDistance != 0) {
      containerScrollView.scrollBy(0, scrollDistance);
    }
  }

  private int getScrollDistance() {
    int distance = 0;
    View view = null;
    if (gameContainer.getChildCount() > 0) {
      view = gameContainer.getChildAt(0);
    }
    if (view != null) {
      distance = gameContainer.getChildAt(0).getHeight() * 2
          + (int) getResources().getDimension(R.dimen.mario_10dp) * 2;
    }
    return distance;
  }

  private void scrollToTop() {
    containerScrollView.scrollBy(0, -gameContainer.getHeight());
  }

  private void showErrorTips() {
    bottomTipsView.showToastTips(StringUtil.getString(R.string.install_all_index_not_found));
  }

  private void showCompleteTips() {
    bottomTipsView.showToastTips(StringUtil.getString(R.string.install_all_complete));
  }

  private void showCancelCompleteTips() {
    bottomTipsView.showToastTips(StringUtil.getString(R.string.install_all_cancel));
  }

  private void showCancelingTips() {
    bottomTipsView.showTips(StringUtil.getString(R.string.install_canceling));
  }

  private void showInstallTips() {
    bottomTipsView.showTips(StringUtil.getString(R.string.install_game_installing, GiftCardManager
        .getInstance().getInstallingName()));
  }

  private void hideTips() {
    bottomTipsView.hideTips();
  }

  private boolean checkMountStatusValid() {
    if (SDCardManager.getInstance().isMountSuccess()) {
      return true;
    }
    showDialog();
    return false;
  }

  private void showDialog() {
    final WidgetHelperAlertDialog.Builder builder =
        new WidgetHelperAlertDialog.Builder(getActivity());
    builder.setTitle(R.string.install_dialog_title);
    builder.setMessage(R.string.install_dialog_message);
    builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        jumpToMount();
      }
    });
    builder.create().show();
  }

  private void jumpToMount() {
    if (isAdded()) {
      MountActivity.launch(getActivity());
    }
  }
}
