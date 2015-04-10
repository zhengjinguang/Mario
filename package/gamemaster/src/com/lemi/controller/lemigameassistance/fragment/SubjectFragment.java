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
import com.lemi.controller.lemigameassistance.model.SubjectListModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.SubjectOptionFields;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.view.SubjectCard;
import com.lemi.controller.lemigameassistance.view.TipsView;
import com.lemi.controller.lemigameassistance.view.VerticalCardContainer;
import com.lemi.mario.base.utils.CollectionUtils;


/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectFragment extends AsyncLoadFragment implements TabFragment {

  private VerticalCardContainer verticalCardContainer;
  private TipsView tipsView;

  private GetSubjectTask subjectTask;

  private static final int SUBJECT_START_INDEX = 0;
  private static final int SUBJECT_NUM = 9;

  /**
   * focus item constants
   */
  private static final int LEFT_FOCUS_ITEM = 0;
  private static final int RIGHT_FOCUS_ITEM = 2;
  private static final int DOWN_FOCUS_ITEM = 0;
  private int itemFocusLayoutTreeIndex = 1;

  public SubjectFragment() {}


  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    initFocusListener();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.subject_fragment;
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
    verticalCardContainer =
        (VerticalCardContainer) contentView.findViewById(R.id.subject_card_container);
    tipsView = (TipsView) contentView.findViewById(R.id.tips_view);
    tipsView.setOnRefreshListener(mOnRefreshListener);
  }

  private void loadDataFromNetWork() {
    if (subjectTask != null) {
      DataUtils.stopAsyncTask(subjectTask);
    }
    subjectTask = new GetSubjectTask();
    DataUtils.runAsyncTask(subjectTask);
  }

  private boolean checkDataValid(SubjectListModel subjectListModel) {
    if (subjectListModel == null || CollectionUtils.isEmpty(subjectListModel.getSubjects())) {
      return false;
    }
    return true;
  }

  private void initData(SubjectListModel subjectListModel) {
    for (int i = 0; i < subjectListModel.getSubjects().size(); i++) {
      if (subjectListModel.getSubjects().get(i) == null) {
        continue;
      }
      SubjectCard subjectCard = SubjectCard.newInstance(verticalCardContainer);
      subjectCard.setData(subjectListModel.getSubjects().get(i));
      verticalCardContainer.addView(subjectCard);
    }
  }

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
    return verticalCardContainer.isOnTopEdge(FocusUtils.getParent(view, itemFocusLayoutTreeIndex));
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
    if (verticalCardContainer != null) {
      View child = verticalCardContainer.getChildInOriginal(index);
      if (child != null) {
        child.requestFocus();
      }
    }
  }


  private class GetSubjectTask extends AsyncTask<Void, Void, SubjectListModel> {
    @Override
    protected SubjectListModel doInBackground(Void... objects) {
      return GameMasterHttpHelper.getSubjects(SUBJECT_START_INDEX, SUBJECT_NUM,
          SubjectOptionFields.SUBJECT_LITE.getOptionFields());
    }

    @Override
    protected void onPostExecute(SubjectListModel subjectListModel) {
      if (!checkDataValid(subjectListModel)) {
        tipsView.changeTipsStatus(TipsView.TipsStatus.FAILED);
        return;
      }
      tipsView.changeTipsStatus(TipsView.TipsStatus.GONE);
      initData(subjectListModel);
    }
  }


  TipsView.OnRefreshListener mOnRefreshListener = new TipsView.OnRefreshListener() {
    @Override
    public void onRefresh() {
      loadDataFromNetWork();
    }
  };

}
