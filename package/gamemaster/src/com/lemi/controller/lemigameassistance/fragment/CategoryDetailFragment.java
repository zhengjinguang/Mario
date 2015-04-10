package com.lemi.controller.lemigameassistance.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.fetcher.BaseFetcher;
import com.lemi.controller.lemigameassistance.focus.anim.FixScrollHelper;
import com.lemi.controller.lemigameassistance.fragment.base.GroupItemFragment;
import com.lemi.controller.lemigameassistance.manager.MyGameManager;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.model.GamesTimeLineModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.net.filter.GameOptionFields;
import com.lemi.controller.lemigameassistance.recycleview.adapter.BaseRecycleViewAdapter;
import com.lemi.controller.lemigameassistance.recycleview.adapter.CategoryDetailAdapter;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryDetailItemModel;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CategoryDetailFragment extends GroupItemFragment<GameModel> {

  /**
   * private constants
   */
  private static final int ITEM_NUM = 10;
  private static final int INIT_FOCUS_NUM = 0;
  private static final String CACHE_KEY = "CategoryDetail";


  private ImageView emptyView;
  private ImageView prePageButton;
  private ImageView nextPageButton;
  private TextView pageTextView;
  private ProgressBar progressBar;


  /**
   * custom model
   */
  private final List<GameModel> gameModelList = new ArrayList<>();
  private final List<CategoryDetailItemModel> itemModelList = new ArrayList<>();



  private String categoryId;
  private int totalNum;
  private int totalPageNum;
  private int currentPageNum;

  private FixScrollHelper fixScrollHelper;


  public CategoryDetailFragment() {}


  @Override
  protected int getLayoutResId() {
    return R.layout.category_detail_fragment;
  }

  @Override
  protected BaseRecycleViewAdapter getAdapter() {
    recycleAdapter = new CategoryDetailAdapter();
    ((CategoryDetailAdapter) recycleAdapter).setData(itemModelList);
    return recycleAdapter;
  }

  @Override
  protected LinearLayoutManager getLayoutManager() {
    layoutManager = new LinearLayoutManager(GlobalConfig.getAppContext());
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    return layoutManager;
  }

  @Override
  protected LayoutOrientation getOrientation() {
    return LayoutOrientation.VERTICAL;
  }

  @Override
  protected int getItemFocusLayoutTreeIndexLayoutTree() {
    return 1;
  }

  @Override
  protected List<GameModel> fetchHttpData(int start, int size) throws ExecutionException {
    if (Constants.MY_GAME.equals(categoryId)) {
      return fetchMyGameData();
    } else {
      return fetchCategoryData(start, size);
    }
  }

  @Override
  protected String getCacheKey() {
    return CACHE_KEY + categoryId;
  }


  @Override
  protected void onFetched(int start, int size, BaseFetcher.ResultList<GameModel> result) {
    setData(result.data);
    setPageData();
  }

  @Override
  protected void onFailed(int start, ExecutionException e) {}

  @Override
  protected LoadNetworkTimes getLoadNetworkTimes() {
    if (Constants.MY_GAME.equals(categoryId)) {
      return LoadNetworkTimes.ONCE;
    } else {
      return LoadNetworkTimes.MORE;
    }
  }

  @Override
  protected void onScrollStateIdle() {
    updatePageText();
    changeButtonStat();
    if (fixScrollHelper.needFixScrollVertical()) {
      fixScrollHelper.fixScroll(recyclerView);
    } else {
      super.onScrollStateIdle();
    }
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
    emptyView = (ImageView) contentView.findViewById(R.id.empty_view);
    pageTextView = (TextView) contentView.findViewById(R.id.category_detail_page_num);
    progressBar = (ProgressBar) contentView.findViewById(R.id.load_more_progress);
    prePageButton = (ImageView) contentView.findViewById(R.id.category_detail_pre_screen_button);
    nextPageButton = (ImageView) contentView.findViewById(R.id.category_detail_next_screen_button);
    prePageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scrollUp();
        requestFocusAfterUpScroll(null);
      }
    });
    nextPageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scrollDown();
        requestFocusAfterDownScroll(null);
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
      categoryId = bundle.getString(Intents.INTENT_EXTRA_CATEGORY_ID);
      totalNum = bundle.getInt(Intents.INTENT_EXTRA_CATEGORY_COUNT);
    }
  }

  /**
   * set data
   */

  private void setData(List<GameModel> model) {
    gameModelList.addAll(model);
    genItemModelAndNotify();
    judgeEmptyImageNeedShow();
  }

  private void setPageData() {
    updatePageText();
    changeButtonStat();
  }

  private void genItemModelAndNotify() {
    int startPageNum = pageNum;
    pageNum = (int) Math.ceil(((double) gameModelList.size()) / itemInnerNum);
    for (int i = startPageNum; i < pageNum; i++) {
      int start = i * itemInnerNum;
      int end = start + itemInnerNum;
      if (end > gameModelList.size()) {
        end = gameModelList.size();
      }
      itemModelList.add(new CategoryDetailItemModel(new ListUtil<GameModel>().subList(
          gameModelList, start, end)));
    }
    if (itemModelList.size() > 0) {
      recycleAdapter.notifyItemRangeChanged(startPageNum, pageNum - startPageNum);
    }
  }


  @Override
  protected boolean checkNeedLoadMore() {
    if (Constants.MY_GAME.equals(categoryId)) {
      return false;
    }
    if (currentPageNum == pageNum && pageNum < totalPageNum) {
      return true;
    }
    return false;
  }

  @Override
  protected boolean checkNeedPreLoad() {
    if (Constants.MY_GAME.equals(categoryId)) {
      return false;
    }
    if (currentPageNum == pageNum - 1 && pageNum < totalPageNum) {
      return true;
    }
    return false;
  }

  /**
   * scroll
   */

  @Override
  protected boolean scrollUp() {
    int toPosition = getFocusPosition() - 1;
    if (toPosition < 0) {
      toPosition = 0;
    }
    recyclerView.smoothScrollToPosition(toPosition);
    fixScrollHelper.setNeedFixUpScroll();
    return true;
  }

  @Override
  protected boolean scrollDown() {
    int toPosition = getFocusPosition() + 1;
    if (toPosition >= recycleAdapter.getItemCount()) {
      toPosition = recycleAdapter.getItemCount() - 1;
    }
    recyclerView.smoothScrollToPosition(toPosition);
    fixScrollHelper.setNeedFixDownScroll();
    return true;
  }



  private void judgeEmptyImageNeedShow() {
    synchronized (itemModelList) {
      if (CollectionUtils.isEmpty(itemModelList)) {
        emptyView.setVisibility(View.VISIBLE);
      } else {
        emptyView.setVisibility(View.GONE);
      }
    }
  }

  /**
   * page info
   */

  private void updatePageText() {
    updatePageInfo();
    if (totalPageNum < 1) {
      pageTextView.setVisibility(View.GONE);
    } else {
      pageTextView.setVisibility(View.VISIBLE);
    }
    pageTextView.setText(currentPageNum + " / " + totalPageNum);
  }

  private void updatePageInfo() {
    totalPageNum = (int) Math.ceil(((double) totalNum) / itemInnerNum);
    int focusPosition = recyclerView.getChildPosition(recyclerView.getFocusedChild());
    if (focusPosition < 0) {
      focusPosition = 0;
    }
    currentPageNum = focusPosition + 1;
  }


  /**
   * button info
   */

  private void initButtonStat() {
    prePageButton.setVisibility(View.GONE);
    nextPageButton.setVisibility(View.GONE);
  }

  private void changeButtonStat() {
    if (totalPageNum <= 1) {
      nextPageButton.setVisibility(View.GONE);
    } else {
      if (getFocusPosition() < totalPageNum - 1) {
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


  private List<GameModel> fetchCategoryData(int start, int size) throws ExecutionException {
    GamesTimeLineModel gamesTimeLine =
        GameMasterHttpHelper.getGamesTimeLine(start, size, categoryId,
            GameOptionFields.CATEGORY_LITE.getOptionFields());
    if (gamesTimeLine == null) {
      throw new ExecutionException(new IllegalStateException("data size is null"));
    }
    if (gamesTimeLine.getRet() != GameMasterHttpHelper.VALID_RETURN) {
      if (start == 0) {
        throw new ExecutionException(new IllegalStateException("data size is null"));
      } else {
        return null;
      }
    }
    if (CollectionUtils.isEmpty(gamesTimeLine.getGames())) {
      return null;
    }
    totalNum = gamesTimeLine.getCount();
    return gamesTimeLine.getGames();
  }

  private List<GameModel> fetchMyGameData() {
    List<GameModel> gameModels = MyGameManager.getInstance().getMyInstalledGames();
    if (gameModels != null) {
      totalNum = gameModels.size();
    }
    return gameModels;
  }

}
