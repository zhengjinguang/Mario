package com.lemi.controller.lemigameassistance.recycleview.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryDetailItemModel;
import com.lemi.controller.lemigameassistance.view.GameItemView;
import com.lemi.controller.lemigameassistance.view.VerticalCardContainer;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.ViewUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CategoryDetailItem extends VerticalCardContainer {

  public CategoryDetailItem(Context context) {
    super(context);
  }

  public CategoryDetailItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public static CategoryDetailItem newInstance(ViewGroup parent) {
    return (CategoryDetailItem) ViewUtils.newInstance(parent,
        R.layout.category_detail_item_container);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  public void bind(CategoryDetailItemModel model) {
    int i = 0;

    if (model != null && !CollectionUtils.isEmpty(model.getCategoryItemModel())) {
      for (i = 0; i < model.getCategoryItemModel().size(); i++) {
        GameItemView gameItemView = (GameItemView) getChildInOriginal(i);
        if (gameItemView == null) {
          gameItemView = GameItemView.newInstance(this, GameItemView.Type.LARGE);
          addView(gameItemView);
        }
        gameItemView.setVisibility(View.VISIBLE);
        gameItemView.bind(model.getCategoryItemModel().get(i));
      }
    }

    removeRedundant(i);
  }

  /**
   * remove the redundant view int the container.
   * 
   * @param beginIndex begin from which index
   */
  private void removeRedundant(int beginIndex) {
    for (int k = beginIndex; k < getChildCount(); k++) {
      removeViewAt(k);
    }
  }

}
