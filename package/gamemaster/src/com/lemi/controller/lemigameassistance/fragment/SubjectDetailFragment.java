package com.lemi.controller.lemigameassistance.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.fetcher.BaseFetcher;
import com.lemi.controller.lemigameassistance.focus.anim.FixScrollHelper;
import com.lemi.controller.lemigameassistance.fragment.base.GroupItemFragment;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.model.SubjectContentModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.GameOptionFields;
import com.lemi.controller.lemigameassistance.net.filter.SubjectOptionFields;
import com.lemi.controller.lemigameassistance.recycleview.adapter.BaseRecycleViewAdapter;
import com.lemi.controller.lemigameassistance.recycleview.adapter.SubjectDetailAdapter;
import com.lemi.controller.lemigameassistance.recycleview.model.SubjectDetailItemModel;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.ListUtil;
import com.lemi.mario.image.view.AsyncImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectDetailFragment extends GroupItemFragment<GameModel> {

  /**
   * private constants
   */
  private static final int TANSPARENT_RESID = -1;
  private static final int ITEM_NUM = 6;
  private static final int INIT_FOCUS_NUM = 0;


  private AsyncImageView posterView;
  private RelativeLayout gameListLayout;
  private ImageView prePageButton;
  private ImageView nextPageButton;


  /**
   * custom model
   */
  private List<GameModel> gameModelList = new ArrayList<>();
  private List<SubjectDetailItemModel> itemModelList = new ArrayList<>();

  private SubjectContentModel subjectModel;


  private long subjectId;

  /**
   * fix scroll
   */
  private FixScrollHelper fixScrollHelper;


  public SubjectDetailFragment() {}


  @Override
  protected int getLayoutResId() {
    return R.layout.subject_detail_fragment;
  }

  @Override
  protected BaseRecycleViewAdapter getAdapter() {
    recycleAdapter = new SubjectDetailAdapter();
    ((SubjectDetailAdapter) recycleAdapter).setData(itemModelList);
    return recycleAdapter;
  }

  @Override
  protected LinearLayoutManager getLayoutManager() {
    layoutManager = new LinearLayoutManager(GlobalConfig.getAppContext());
    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    return layoutManager;
  }

  @Override
  protected LayoutOrientation getOrientation() {
    return LayoutOrientation.HORIZONTAL;
  }

  @Override
  protected int getItemFocusLayoutTreeIndexLayoutTree() {
    return 1;
  }

  @Override
  protected List<GameModel> fetchHttpData(int start, int size) throws ExecutionException {
    SubjectContentModel subjectContentModel = GameMasterHttpHelper.getSubjectContent(subjectId,
        GameOptionFields.CATEGORY_LITE.getOptionFields(),
        SubjectOptionFields.ALL.getOptionFields());
    if (subjectContentModel == null || subjectContentModel.getSubject() == null) {
      return null;
    }
    subjectModel = subjectContentModel;
    return new ArrayList<GameModel>();
  }

  @Override
  protected String getCacheKey() {
    return null;
  }

  @Override
  protected void onFetched(int start, int size, BaseFetcher.ResultList<GameModel> result) {
    setData(subjectModel);
  }

  @Override
  protected void onFailed(int start, ExecutionException e) {}

  @Override
  protected LoadNetworkTimes getLoadNetworkTimes() {
    return LoadNetworkTimes.ONCE;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    getBundle();
    return contentView;
  }

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    super.onInflated(contentView, savedInstanceState);
    initView();
    initPageInfo();
    initFixScroll();
    initButtonStat();
    initFocus(INIT_FOCUS_NUM);
  }

  private void initView() {
    posterView = (AsyncImageView) contentView.findViewById(R.id.subject_detail_poster);
    gameListLayout = (RelativeLayout) contentView.findViewById(R.id.subject_detail_recycle_layout);
    prePageButton = (ImageView) contentView.findViewById(R.id.subject_detail_pre_screen_button);
    nextPageButton = (ImageView) contentView.findViewById(R.id.subject_detail_next_screen_button);
    prePageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scrollLeft();
        requestFocusAfterLeftScroll(null);
      }
    });
    nextPageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scrollRight();
        requestFocusAfterRightScroll(null);
      }
    });
  }

  private void initPageInfo() {
    itemInnerNum = ITEM_NUM;
  }

  private void initFixScroll() {
    fixScrollHelper = new FixScrollHelper();
  }

  private void getBundle() {
    Bundle bundle = getArguments();
    if (bundle != null) {
      subjectId = bundle.getLong(Intents.INTENT_EXTRA_SID);
    }
  }

  @Override
  protected void onScrollStateIdle() {
    changeButtonStat();
    if (fixScrollHelper.needFixScrollHorizontal()) {
      fixScrollHelper.fixScroll(recyclerView);
    }
  }


  private void setData(SubjectContentModel subjectContentModel) {
    if (!CollectionUtils.isEmpty(subjectContentModel.getSubject().getPosters())) {
      posterView.loadNetworkImage(subjectContentModel.getSubject().getPosters().get(0).getUrl(),
          TANSPARENT_RESID);
    }
    if (!CollectionUtils.isEmpty(subjectContentModel.getGames())) {
      gameListLayout.setVisibility(View.VISIBLE);
      gameModelList.addAll(subjectContentModel.getGames());
      genItemModelAndNotify();
    } else {
      gameListLayout.setVisibility(View.INVISIBLE);
    }
    changeButtonStat();
  }

  private void genItemModelAndNotify() {
    pageNum = (int) Math.ceil(((double) gameModelList.size()) / itemInnerNum);
    for (int i = 0; i < pageNum; i++) {
      int start = i * itemInnerNum;
      int end = start + itemInnerNum;
      if (end > gameModelList.size()) {
        end = gameModelList.size();
      }
      itemModelList.add(new SubjectDetailItemModel(new ListUtil<GameModel>().subList(
          gameModelList, start, end)));
    }
    if (itemModelList.size() > 0) {
      recycleAdapter.notifyItemRangeChanged(0, pageNum);
    }
  }

  @Override
  protected boolean scrollLeft() {
    recyclerView.smoothScrollToPosition(getFocusPosition() - 1);
    fixScrollHelper.setNeedFixLeftScroll();
    return true;
  }

  @Override
  protected boolean scrollRight() {
    recyclerView.smoothScrollToPosition(getFocusPosition() + 1);
    fixScrollHelper.setNeedFixRightScroll();
    return true;
  }


  private void initButtonStat() {
    prePageButton.setVisibility(View.GONE);
    nextPageButton.setVisibility(View.GONE);
  }

  private void changeButtonStat() {
    if (pageNum <= 1) {
      nextPageButton.setVisibility(View.GONE);
    } else {
      if (getFocusPosition() < pageNum - 1) {
        nextPageButton.setVisibility(View.VISIBLE);
      } else {
        nextPageButton.setVisibility(View.GONE);
      }
    }

    if (getFocusPosition() > 0) {
      prePageButton.setVisibility(View.VISIBLE);
    } else {
      prePageButton.setVisibility(View.GONE);
    }
  }

}
