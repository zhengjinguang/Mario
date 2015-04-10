package com.lemi.controller.lemigameassistance.mvc.controller;

import android.view.View;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.mvc.model.GameItemModel;
import com.lemi.controller.lemigameassistance.mvc.view.GameItemView;
import com.lemi.mario.mvc.BaseController;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameItemController implements BaseController<GameItemView, GameItemModel> {

  protected static final int MODEL_TAG = R.id.category_grid_item_tag;

  @Override
  public void bind(GameItemView gameItemView, GameItemModel gameItemModel) {
    if (gameItemModel == null) {
      return;
    }
    gameItemView.getName().setText(gameItemModel.getName());
    gameItemView.getIcon().loadNetworkImage(gameItemModel.getIconUrl(), R.drawable.icon_defualt);

    gameItemView.getView().setTag(MODEL_TAG, gameItemModel);

  }

  public static GameItemController getModel(View ItemView) {
    if (ItemView == null) {
      return null;
    }
    return (GameItemController) ItemView.getTag(MODEL_TAG);
  }

}
