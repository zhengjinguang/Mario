package com.lemi.controller.lemigameassistance.recycleview.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.recycleview.model.SubjectDetailItemModel;
import com.lemi.controller.lemigameassistance.view.GameItemView;
import com.lemi.controller.lemigameassistance.view.HorizontalCardContainer;
import com.lemi.controller.lemigameassistance.view.VerticalCardContainer;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.ViewUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SubjectDetailItem extends VerticalCardContainer {

  public SubjectDetailItem(Context context) {
    super(context);
  }

  public SubjectDetailItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public static SubjectDetailItem newInstance(ViewGroup parent) {
    return (SubjectDetailItem) ViewUtils
        .newInstance(parent, R.layout.subject_detail_item_container);
  }

  public void bind(SubjectDetailItemModel model) {
    int i = 0;

    if (model != null && !CollectionUtils.isEmpty(model.getSubjectItemModel())) {
      for (i = 0; i < model.getSubjectItemModel().size(); i++) {
        GameItemView gameItemView = (GameItemView) getChildInOriginal(i);
        if (gameItemView == null) {
          gameItemView = GameItemView.newInstance(this, GameItemView.Type.LITTLE);
          addView(gameItemView);
        }
        gameItemView.setVisibility(View.VISIBLE);
        gameItemView.bind(model.getSubjectItemModel().get(i));
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
