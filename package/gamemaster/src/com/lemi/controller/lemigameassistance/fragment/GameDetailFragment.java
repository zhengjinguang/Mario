package com.lemi.controller.lemigameassistance.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.fragment.base.AsyncLoadFragment;
import com.lemi.controller.lemigameassistance.model.GameListModel;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.GameOptionFields;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.utils.GameModelFormatUtils;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.controller.lemigameassistance.view.DetailPosterView;
import com.lemi.controller.lemigameassistance.view.DownloadUnzipRoundBar;
import com.lemi.controller.lemigameassistance.view.NetAppButton;
import com.lemi.controller.lemigameassistance.view.TipsView;
import com.lemi.mario.base.utils.SizeConvertUtil;
import com.lemi.mario.base.utils.StringUtil;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameDetailFragment extends AsyncLoadFragment {

  private String packageName;
  private GameModel gameModel;
  private GetGameDetailTask getGameDetailTask;

  private AsyncImageView iconView;
  private TextView nameView;
  private TextView sizeView;
  private TextView downloadCountView;
  private TextView playModeView;
  private ImageView operationJoyStick;
  private ImageView operationControlPanel;
  private ImageView operationMouse;
  private NetAppButton downloadButton;
  private TextView noticeTitleView;
  private TextView noticeMessageView;
  private TextView descriptionTitleView;
  private TextView descriptionMessageView;
  private LinearLayout screenShotListView;
  private RelativeLayout gameDetailContainer;
  private DownloadUnzipRoundBar progressbar;

  private TipsView tipsView;

  public GameDetailFragment() {}

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    getBundle();
    initView();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.game_detail_fragment;
  }

  @Override
  protected void onPrepareLoading() {
    tipsView.changeTipsStatus(TipsView.TipsStatus.LOADING);
  }

  @Override
  protected void onStartLoading() {
    loadDataFromNetWork();
  }

  private void getBundle() {
    Bundle bundle = getArguments();
    if (bundle != null) {
      packageName = bundle.getString(Intents.INTENT_EXTRA_PACKAGENAME);
      if (TextUtils.isEmpty(packageName)) {
        throw new IllegalArgumentException("packageName can not be null");
      }
    }
  }

  private void initView() {
    screenShotListView =
        (LinearLayout) contentView.findViewById(R.id.game_detail_sreen_shot_container);
    gameDetailContainer = (RelativeLayout) contentView.findViewById(R.id.game_detail_container);
    iconView = (AsyncImageView) contentView.findViewById(R.id.game_detail_icon);
    nameView = (TextView) contentView.findViewById(R.id.game_detail_name);
    sizeView = (TextView) contentView.findViewById(R.id.game_detail_size);
    downloadCountView = (TextView) contentView.findViewById(R.id.game_detail_download_count);
    playModeView = (TextView) contentView.findViewById(R.id.game_detail_play_mode);
    operationJoyStick = (ImageView) contentView.findViewById(R.id.game_detail_operation_joystick);
    operationControlPanel =
        (ImageView) contentView.findViewById(R.id.game_detail_operation_control_panel);
    operationMouse = (ImageView) contentView.findViewById(R.id.game_detail_operation_mouse);

    downloadButton = (NetAppButton) contentView.findViewById(R.id.game_detail_download_button);
    progressbar =
        (DownloadUnzipRoundBar) contentView.findViewById(R.id.game_detail_round_progressbar);

    tipsView = (TipsView) contentView.findViewById(R.id.tips_view);
    tipsView.setOnRefreshListener(mOnRefreshListener);

    noticeTitleView = (TextView) contentView.findViewById(R.id.game_detail_notice_title);
    noticeMessageView =
        (TextView) contentView.findViewById(R.id.game_detail_notice_message);
    descriptionTitleView = (TextView) contentView.findViewById(R.id.game_detail_description_title);
    descriptionMessageView =
        (TextView) contentView.findViewById(R.id.game_detail_description_message);
  }

  private void loadDataFromNetWork() {
    if (getGameDetailTask != null) {
      DataUtils.stopAsyncTask(getGameDetailTask);
    }
    gameDetailContainer.setVisibility(View.GONE);
    getGameDetailTask = new GetGameDetailTask();
    DataUtils.runAsyncTask(getGameDetailTask);
  }

  private void setData() {
    downloadButton.setData(gameModel);
    progressbar.setData(packageName);
    LogHelper.gameDetailCLick(gameModel.getName());

    iconView.loadNetworkImage(gameModel.getIconUrl(), R.drawable.icon_defualt);

    nameView.setText(gameModel.getName());
    if (!TextUtils.isEmpty(gameModel.getApkSize())) {
      sizeView.setText(StringUtil.getString(R.string.game_size,
          SizeConvertUtil.formatSize(Float.valueOf(gameModel.getApkSize()))));
    }

    downloadCountView.setText(StringUtil.getString(R.string.game_download_total,
        gameModel.getDownloadCount()));

    playModeView.setText(StringUtil.getString(R.string.game_palypode,
        GameModelFormatUtils.formatGamePlay(gameModel.getPlayMode(), gameModel.getPersonMode(),
            gameModel.getCategory())));

    GameModelFormatUtils.formatGameOperate(gameModel.getOperationMode(),
        operationJoyStick, operationControlPanel, operationMouse);

    String notice = gameModel.getNotice();

    if (!TextUtils.isEmpty(notice)) {
      noticeTitleView.setText(R.string.game_notice);
      noticeMessageView.setText(notice);
      noticeTitleView.setVisibility(View.VISIBLE);
      noticeMessageView.setVisibility(View.VISIBLE);
    } else {
      noticeTitleView.setVisibility(View.GONE);
      noticeMessageView.setVisibility(View.GONE);
    }
    descriptionTitleView.setText(R.string.game_depiction);
    descriptionMessageView.setText(gameModel.getDescription());

    setImageListData();
    setFirstFocusView();
  }

  private void setImageListData() {

    if (gameModel.getImages() == null || gameModel.getImages().size() <= 0) {
      return;
    }

    for (int i = 0; i < gameModel.getImages().size(); i++) {
      if (!isAdded() || gameModel.getImages() == null
          || TextUtils.isEmpty(gameModel.getImages().get(i).getUrl())) {
        continue;
      }
      AsyncImageView imageView = DetailPosterView.newInstance(screenShotListView);
      screenShotListView.addView(imageView);
      imageView.loadNetworkImage(gameModel.getImages().get(i).getUrl(),
          R.drawable.detail_poster_default);
    }
  }

  private void setFirstFocusView() {
    downloadButton.requestFocus();
  }


  private final class GetGameDetailTask extends AsyncTask<Void, Void, GameModel> {

    @Override
    protected GameModel doInBackground(Void... voids) {
      GameListModel gameList =
          GameMasterHttpHelper.getGamesByPackageName(packageName,
              GameOptionFields.ALL.getOptionFields());
      if (gameList == null || gameList.getGames() == null || gameList.getGames().size() < 1) {
        return null;
      }
      return gameList.getGames().get(0);
    }

    @Override
    protected void onPostExecute(GameModel gameModel) {
      super.onPostExecute(gameModel);
      if (gameModel == null) {
        tipsView.changeTipsStatus(TipsView.TipsStatus.FAILED);
        return;
      } else {
        tipsView.changeTipsStatus(TipsView.TipsStatus.GONE);
        gameDetailContainer.setVisibility(View.VISIBLE);
      }
      GameDetailFragment.this.gameModel = gameModel;
      setData();
    }
  }

  TipsView.OnRefreshListener mOnRefreshListener = new TipsView.OnRefreshListener() {
    @Override
    public void onRefresh() {
      loadDataFromNetWork();
    }
  };

}
