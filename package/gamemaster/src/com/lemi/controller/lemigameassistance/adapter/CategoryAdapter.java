package com.lemi.controller.lemigameassistance.adapter;

import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.adapter.base.BaseAdapter;
import com.lemi.controller.lemigameassistance.mvc.controller.GameItemController;
import com.lemi.controller.lemigameassistance.mvc.model.GameItemModel;
import com.lemi.controller.lemigameassistance.mvc.view.GameItemView;
import com.lemi.mario.mvc.BaseController;
import com.lemi.mario.mvc.BaseView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CategoryAdapter extends BaseAdapter<GameItemModel> {

  @Override
  protected BaseView newView(int position, GameItemModel model, ViewGroup parent) {
    return GameItemView.newInstance(parent);
  }

  @Override
  protected BaseController newController(int position, GameItemModel model) {
    return new GameItemController();
  }

}
