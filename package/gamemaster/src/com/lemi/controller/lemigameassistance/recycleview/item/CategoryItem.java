package com.lemi.controller.lemigameassistance.recycleview.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryItemModel;
import com.lemi.controller.lemigameassistance.view.CategoryItemView;
import com.lemi.controller.lemigameassistance.view.HorizontalCardContainer;
import com.lemi.controller.lemigameassistance.view.VerticalCardContainer;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.ViewUtils;

/**
 * @author zhengjinguang@letv.com (shining)
 */
public class CategoryItem extends HorizontalCardContainer {

    public CategoryItem(Context context) {
        super(context);
    }

    public CategoryItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static CategoryItem newInstance(ViewGroup parent) {
        return (CategoryItem) ViewUtils.newInstance(parent, R.layout.category_item_container);
    }

    public void bind(CategoryItemModel model) {
        int i = 0;

        if (model != null && !CollectionUtils.isEmpty(model.getCategoryItemModel())) {
            for (i = 0; i < model.getCategoryItemModel().size(); i++) {
                CategoryItemView categoryItemView = (CategoryItemView) getChildInOriginal(i);
                if (categoryItemView == null) {
                    categoryItemView = CategoryItemView.newInstance(this);
                    addView(categoryItemView);
                }
                categoryItemView.setVisibility(View.VISIBLE);
                categoryItemView.bind(model.getCategoryItemModel().get(i));
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

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
        System.out.println("HorizontalCardContainer KEYCODE_DPAD_LEFT " );
        break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        System.out.println("HorizontalCardContainer KEYCODE_DPAD_RIGHT ");
        break;
      case KeyEvent.KEYCODE_DPAD_UP:
        System.out.println("HorizontalCardContainer KEYCODE_DPAD_UP ");
        break;
      case KeyEvent.KEYCODE_DPAD_DOWN:
        System.out.println("HorizontalCardContainer KEYCODE_DPAD_DOWN " );
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

}
