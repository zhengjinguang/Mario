package com.lemi.controller.lemigameassistance.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.focus.utils.FocusUtils;
import com.lemi.controller.lemigameassistance.focus.view.TabFragment;
import com.lemi.controller.lemigameassistance.fragment.base.AsyncLoadFragment;
import com.lemi.controller.lemigameassistance.fragment.tabhost.TabHostFragment;
import com.lemi.controller.lemigameassistance.model.GetLotteryModel;
import com.lemi.controller.lemigameassistance.model.RecommendsModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.GameOptionFields;
import com.lemi.controller.lemigameassistance.net.filter.SubjectOptionFields;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.controller.lemigameassistance.view.RecommendCard;
import com.lemi.controller.lemigameassistance.view.StaggeredHorizontalCardContainer;
import com.lemi.controller.lemigameassistance.view.TipsView;
import com.lemi.mario.accountmanager.config.ReturnValues;
import com.lemi.mario.base.utils.ArrayUtil;
import com.lemi.mario.base.utils.CollectionUtils;


/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RecommendFragment extends AsyncLoadFragment implements TabFragment {

  private static final int[] LARGE_CARD_INDEX = {0, 4};
  private static final int[] LARGE_CARD_DATA_INDEX = {0, 1};
  private static final int SUBJECT_CARD_INDEX = 5;
  private static final int RECOMMEND_CARD_NUM = 8;
  private static final int SUBJECT_DATA_ITEM = 0;
  private static final int POSTER_DATA_ITEM = 0;
  /**
   * focus item constants
   */
  private static final int LEFT_FOCUS_ITEM = 0;
  private static final int RIGHT_FOCUS_ITEM = 6;
  private static final int DOWN_FOCUS_ITEM = 0;
  private static final int[] TOP_INDEX = {0, 1, 4, 5};
  private static final int LOTTERY_INDEX = 5;
  TipsView.OnRefreshListener mOnRefreshListener = new TipsView.OnRefreshListener() {
    @Override
    public void onRefresh() {
      loadDataFromNetWork();
    }
  };
  private StaggeredHorizontalCardContainer staggeredHorizontalCardContainer;
  private TipsView tipsView;
  private RecommendContentType contentType;
  private GetRecommendTask recommendTask;
  private int itemFocusLayoutTreeIndex = 1;
  private GetLotteryModel lotteryModel = null;

  public RecommendFragment() {}

  @Override
  public void requestLeftFocus() {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      tipsView.requestFocus();
      return;
    }
    requestChildFocus(LEFT_FOCUS_ITEM);
  }

  @Override
  public void requestRightFocus() {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      tipsView.requestFocus();
      return;
    }
    requestChildFocus(RIGHT_FOCUS_ITEM);
  }

  @Override
  public void requestDownFocus() {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      tipsView.requestFocus();
      return;
    }
    requestChildFocus(DOWN_FOCUS_ITEM);
  }

  @Override
  public boolean isOnTop(View view) {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      return true;
    }
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
    initFocusListener();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.recommend_fragment;
  }

  @Override
  protected void onPrepareLoading() {
    tipsView.changeTipsStatus(TipsView.TipsStatus.LOADING);
  }

  @Override
  protected void onStartLoading() {
    loadDataFromNetWork();
  }

  private void initView() {
    staggeredHorizontalCardContainer =
        (StaggeredHorizontalCardContainer) contentView.findViewById(R.id.recommend_card_container);
    tipsView = (TipsView) contentView.findViewById(R.id.tips_view);
    tipsView.setOnRefreshListener(mOnRefreshListener);
  }

  private void loadDataFromNetWork() {
    if (recommendTask != null) {
      DataUtils.stopAsyncTask(recommendTask);
    }
    DataUtils.runAsyncTask(new GetLotteryAsyncTask());
  }

  private boolean checkDataValid(RecommendsModel recommendsModel) {
    if (recommendsModel == null || CollectionUtils.isEmpty(recommendsModel.getGames())
        || recommendsModel.getGames().size() < 8) {
      return false;
    }
    if (!CollectionUtils.isEmpty(recommendsModel.getSubjects())) {
      contentType = RecommendContentType.GAME_AND_SUBJECT;
      return true;
    }
    contentType = RecommendContentType.ALL_GAME;
    return true;
  }

  private void initData(RecommendsModel recommendsModel) {
    for (int i = 0; i < RECOMMEND_CARD_NUM; i++) {
      RecommendCard recommendCard;
      int dataIndex;
      if (ArrayUtil.contains(LARGE_CARD_INDEX, i)) {
        recommendCard =
            RecommendCard.newInstance(staggeredHorizontalCardContainer,
                RecommendCard.RecommendCardType.LARGE);
        setLogEvent(recommendCard, i);
        dataIndex = getDataIndex(i);
        if (recommendsModel.getGames().get(dataIndex) == null
            || CollectionUtils.isEmpty(recommendsModel.getGames().get(dataIndex).getPosters())
            || recommendsModel.getGames().get(dataIndex).getPosters().get(POSTER_DATA_ITEM) == null) {
          continue;
        }
        recommendCard.setData(RecommendCard.RecommendCardContentType.GAME,
            recommendsModel.getGames().get(dataIndex).getPosters().get(POSTER_DATA_ITEM).getUrl(),
            recommendsModel.getGames().get(dataIndex).getPackageName(),
            null);
      } else {
        recommendCard =
            RecommendCard.newInstance(staggeredHorizontalCardContainer,
                RecommendCard.RecommendCardType.LITTLE);
        setLogEvent(recommendCard, i);
        if (SUBJECT_CARD_INDEX == i && contentType == RecommendContentType.GAME_AND_SUBJECT) {
          if (recommendsModel.getGames().get(SUBJECT_DATA_ITEM) == null) {
            continue;
          }
          recommendCard.setData(RecommendCard.RecommendCardContentType.SUBJECT,
              recommendsModel.getSubjects().get(SUBJECT_DATA_ITEM).getIconUrl(),
              String.valueOf(recommendsModel.getSubjects().get(SUBJECT_DATA_ITEM).getSid()),
              recommendsModel.getSubjects().get(SUBJECT_DATA_ITEM).getName());
        } else {
          dataIndex = getDataIndex(i);
          if (recommendsModel.getGames().get(dataIndex) == null
              || CollectionUtils.isEmpty(recommendsModel.getGames().get(dataIndex).getPosters())
              || recommendsModel.getGames().get(dataIndex).getPosters().get(POSTER_DATA_ITEM) == null) {
            continue;
          }
          recommendCard.setData(
              RecommendCard.RecommendCardContentType.GAME,
              recommendsModel.getGames().get(dataIndex).getPosters().get(POSTER_DATA_ITEM)
                  .getUrl(),
              recommendsModel.getGames().get(dataIndex).getPackageName(),
              null);
        }
      }
      if (lotteryModel != null && i == LOTTERY_INDEX) {
        recommendCard.setData(
            RecommendCard.RecommendCardContentType.LOTTERY,
            lotteryModel.getIconUrl(), String.valueOf(lotteryModel.getStartTime()),
            String.valueOf(lotteryModel.getStopTime()));
      }
      staggeredHorizontalCardContainer.addView(recommendCard);
    }
  }

  private void setLogEvent(RecommendCard recommendCard, int index) {
    if (recommendCard == null) return;
    String eventMsg = null;
    switch (index) {
      case 0:
        eventMsg = LogHelper.RECOMMEND_LARGE0;
        break;
      case 1:
        eventMsg = LogHelper.RECOMMEND_LITTLE1;
        break;
      case 2:
        eventMsg = LogHelper.RECOMMEND_LITTLE2;
        break;
      case 3:
        eventMsg = LogHelper.RECOMMEND_LITTLE3;
        break;
      case 4:
        eventMsg = LogHelper.RECOMMEND_LARGE4;
        break;
      case 5:
        eventMsg = LogHelper.RECOMMEND_LITTLE5;
        break;
      case 6:
        eventMsg = LogHelper.RECOMMEND_LITTLE6;
        break;
      case 7:
        eventMsg = LogHelper.RECOMMEND_LITTLE7;
        break;
      default:
        break;
    }
    if (eventMsg != null) {
      recommendCard.setTag(RecommendCard.TAG_CARD_EVENT, eventMsg);
    }
  }

  private int getDataIndex(int viewIndex) {
    if (viewIndex == LARGE_CARD_INDEX[0]) {
      return LARGE_CARD_DATA_INDEX[0];
    }
    if (viewIndex == LARGE_CARD_INDEX[1]) {
      return LARGE_CARD_DATA_INDEX[1];
    }
    if (viewIndex > LARGE_CARD_INDEX[0] && viewIndex < LARGE_CARD_INDEX[1]) {
      return viewIndex + 1;
    }
    return viewIndex;
  }

  private void initFocusListener() {
    tipsView.setOnFocusLostListener(new TipsView.OnFocusLostListener() {
      @Override
      public void onFocusLost() {
        checkNeedGetFocus();
      }
    });
  }

  private void checkNeedGetFocus() {
    Fragment fragment = getParentFragment();
    if (fragment instanceof TabHostFragment) {
      if (this.equals(((TabHostFragment) fragment).getCurrentFragment())) {
        requestChildFocus(LEFT_FOCUS_ITEM);
      }
    }
  }

  private void requestChildFocus(int index) {
    if (staggeredHorizontalCardContainer != null) {
      View child = staggeredHorizontalCardContainer.getChildInOriginal(index);
      if (child != null) {
        child.requestFocus();
      }
    }
  }

  private enum RecommendContentType {
    ALL_GAME, GAME_AND_SUBJECT
  }

  private class GetRecommendTask extends AsyncTask<Void, Void, RecommendsModel> {
    @Override
    protected RecommendsModel doInBackground(Void... objects) {
      return GameMasterHttpHelper.getRecommend(GameOptionFields.RECOMMEND_LITE.getOptionFields(),
          SubjectOptionFields.SUBJECT_LITE.getOptionFields());
    }

    @Override
    protected void onPostExecute(RecommendsModel recommendsModel) {
      if (!checkDataValid(recommendsModel)) {
        tipsView.changeTipsStatus(TipsView.TipsStatus.FAILED);
        return;
      }
      tipsView.changeTipsStatus(TipsView.TipsStatus.GONE);
      initData(recommendsModel);
    }
  }

  class GetLotteryAsyncTask extends AsyncTask<Void, Void, GetLotteryModel> {
    @Override
    protected GetLotteryModel doInBackground(Void... params) {
      return GameMasterHttpHelper.getLottery();
    }

    @Override
    protected void onPostExecute(GetLotteryModel getLotteryModel) {
      super.onPostExecute(getLotteryModel);
      if (getLotteryModel != null) {
        if (getLotteryModel.getRet() == ReturnValues.VALID_RETURN) {
          long time = System.currentTimeMillis() / 1000;
          if (getLotteryModel.getStartTime() <= time && getLotteryModel.getStopTime() >= time) {
            lotteryModel = getLotteryModel;
          }
        }
      }
      recommendTask = new GetRecommendTask();
      DataUtils.runAsyncTask(recommendTask);
    }
  }

}
