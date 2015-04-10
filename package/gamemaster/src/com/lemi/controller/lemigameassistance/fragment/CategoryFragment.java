package com.lemi.controller.lemigameassistance.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.fetcher.BaseFetcher;
import com.lemi.controller.lemigameassistance.focus.view.GroupItem;
import com.lemi.controller.lemigameassistance.focus.view.TabFragment;
import com.lemi.controller.lemigameassistance.fragment.base.GroupItemFragment;
import com.lemi.controller.lemigameassistance.fragment.tabhost.TabHostFragment;
import com.lemi.controller.lemigameassistance.model.CategoryListModel;
import com.lemi.controller.lemigameassistance.model.CategoryModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.recycleview.adapter.BaseRecycleViewAdapter;
import com.lemi.controller.lemigameassistance.recycleview.adapter.CategoryAdapter;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryInfo;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryItemModel;
import com.lemi.controller.lemigameassistance.utils.ApiConvertUtils;
import com.lemi.controller.lemigameassistance.view.KeepOrderViewGroup;
import com.lemi.controller.lemigameassistance.view.TipsView;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.ListUtil;
import com.lemi.mario.base.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zhengjinguang@letv.com (shining)
 */
public class CategoryFragment extends GroupItemFragment<CategoryModel> implements TabFragment {


  private int lastChildIndex;


  private static final int INNER_ITEM_NUM = 8;
  /**
   * focus item constants
   */
  private final int[] LEFT_FOCUS_ITEM = {1, 0};
  private final int[] DOWN_FOCUS_ITEM = {1, 0};

  @Override
  public void requestLeftFocus() {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      tipsView.requestFocus();
      return;
    }
    requestChildFocus(LEFT_FOCUS_ITEM[0], LEFT_FOCUS_ITEM[1]);
    scrollLeft();
  }

  @Override
  public void requestRightFocus() {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      tipsView.requestFocus();
      return;
    }
    requestChildFocus(pageNum - 1, lastChildIndex % INNER_ITEM_NUM);
  }

  @Override
  public void requestDownFocus() {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      tipsView.requestFocus();
      return;
    }
    requestChildFocus(DOWN_FOCUS_ITEM[0], DOWN_FOCUS_ITEM[1]);
    scrollLeft();
  }

  @Override
  public boolean isOnTop(View view) {
    if (tipsView != null && tipsView.getTipsStatus() != TipsView.TipsStatus.GONE) {
      return true;
    }
    GroupItem groupItem = getFocusGroupItem();
    if (groupItem != null) {
      return groupItem.isOnTopEdge(getFocusInnerItem());
    }
    return false;
  }

  public static enum SpecialCategory {
    MYGAME(Constants.MY_GAME, StringUtil.getString(R.string.mine), R.drawable.category_mygame);
    private final String categoryName;
    private final String cid;
    private final int iconResId;

    private SpecialCategory(String cid, String categoryName, int iconResId) {
      this.cid = cid;
      this.categoryName = categoryName;
      this.iconResId = iconResId;
    }

    public String getCategoryName() {
      return categoryName;
    }

    public String getCid() {
      return cid;
    }

    public int getIconResId() {
      return iconResId;
    }
  }

  /**
   * custom model
   */
  private List<CategoryInfo> categoryModelList = new ArrayList<>();
  private List<CategoryItemModel> itemModelList = new ArrayList<>();


  /**
   * default categories;
   */

  static CategoryInfo myGameInfo = new CategoryInfo();
  static CategoryInfo emptyCategoryInfo = new CategoryInfo();

  public CategoryFragment() {}


  @Override
  protected int getLayoutResId() {
    return R.layout.category_fragment;
  }

  @Override
  protected BaseRecycleViewAdapter getAdapter() {
    recycleAdapter = new CategoryAdapter();
    ((CategoryAdapter) recycleAdapter).setData(itemModelList);
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
  protected List<CategoryModel> fetchHttpData(int start, int size) throws ExecutionException {
    CategoryListModel categoryListModel = GameMasterHttpHelper.getCategroys();
    if (categoryListModel == null || CollectionUtils.isEmpty(categoryListModel.getCategorys())) {
      return null;
    }
    return categoryListModel.getCategorys();
  }

  @Override
  protected String getCacheKey() {
    return null;
  }

  @Override
  protected void onFetched(int start, int size, BaseFetcher.ResultList<CategoryModel> result) {
    setData(convertCategoryModelListToCategoryInfoList(result.data));
  }

  @Override
  protected void onFailed(int start, ExecutionException e) {}

  @Override
  protected LoadNetworkTimes getLoadNetworkTimes() {
    return LoadNetworkTimes.ONCE;
  }


  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    super.onInflated(contentView, savedInstanceState);
    initPageInfo();
    initFocusListener();
  }

  private void initPageInfo() {
    itemInnerNum = INNER_ITEM_NUM;
    blockAtEdge = false;
    firstIndex = 1;
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
        requestChildFocus(LEFT_FOCUS_ITEM[0], LEFT_FOCUS_ITEM[1]);
      }
    }
  }


  @Override
  protected boolean scrollLeft() {
    View item = (View) getGroupItem(getFocusPosition() - 1);
    if (item != null) {
      recyclerView.smoothScrollBy(-item.getWidth(), 0);
    }
    return true;
  }

  @Override
  protected boolean scrollRight() {
    recyclerView.smoothScrollToPosition(getFocusPosition() + 1);
    return true;
  }


  public void setData(List<CategoryInfo> addList) {
    addEmptyItemModel();
    addSpecialCategories();
    categoryModelList.addAll(addList);
    setLastChildIndex(categoryModelList.size() - 1);
    int count = itemInnerNum - (categoryModelList.size() % itemInnerNum);
    for (int i = 0; i < count; i++) {
      categoryModelList.add(getEmptyInstance());
    }
    genItemModelAndNotify();
  }

  private void setLastChildIndex(int index) {
    lastChildIndex = index;
  }

  private void addEmptyItemModel() {
    for (int i = 0; i < itemInnerNum; i++) {
      categoryModelList.add(getEmptyInstance());
    }
  }

  private CategoryInfo getEmptyInstance() {
    if (emptyCategoryInfo == null) {
      emptyCategoryInfo = new CategoryInfo();
    }
    emptyCategoryInfo.setEmpty(true);
    return emptyCategoryInfo;
  }

  private void addSpecialCategories() {
    initSpecialCategory(myGameInfo, SpecialCategory.MYGAME);
    categoryModelList.add(myGameInfo);
  }

  private void initSpecialCategory(CategoryInfo categoryInfo, SpecialCategory category) {
    categoryInfo.setCid(category.getCid());
    categoryInfo.setName(category.getCategoryName());
    categoryInfo.setIconResId(category.getIconResId());
  }

  private void genItemModelAndNotify() {
    pageNum = (int) Math.ceil(((double) categoryModelList.size()) / itemInnerNum);
    itemModelList.clear();

    for (int i = 0; i < pageNum; i++) {
      int start = i * itemInnerNum;
      int end = start + itemInnerNum;
      if (end > categoryModelList.size()) {
        end = categoryModelList.size();
      }
      itemModelList.add(new CategoryItemModel(new ListUtil<CategoryInfo>().subList(
          categoryModelList, start, end)));
    }
    if (itemModelList.size() > 0) {
      recycleAdapter.notifyItemRangeChanged(0, pageNum);
    }
  }


  private void requestChildFocus(int childIndex, int itemIndex) {
    if (childIndex < 0) {
      return;
    }
    RecyclerView.ViewHolder child = recyclerView.findViewHolderForPosition(childIndex);
    if (child != null && child.itemView instanceof KeepOrderViewGroup) {
      View view = ((KeepOrderViewGroup) child.itemView).getChildInOriginal(itemIndex);
      if (view != null) {
        view.requestFocus();
      }
    }
  }

  private List<CategoryInfo> convertCategoryModelListToCategoryInfoList(
      List<CategoryModel> categoryModelList) {
    if (categoryModelList == null || categoryModelList.isEmpty()) {
      return null;
    }
    List<CategoryInfo> categoryInfoList = new ArrayList<>();
    for (CategoryModel categoryModel : categoryModelList) {
      CategoryInfo categoryInfo = ApiConvertUtils.categoryModelConvertToCategoryInfo(categoryModel);
      if (categoryInfo != null) {
        categoryInfoList.add(categoryInfo);
      }
    }
    return categoryInfoList;
  }

}
