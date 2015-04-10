package com.lemi.controller.lemigameassistance.adapter.base;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.config.SmoothScrollConfig;
import com.lemi.mario.mvc.BaseController;
import com.lemi.mario.mvc.BaseModel;
import com.lemi.mario.mvc.BaseView;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 * 
 * @param <M> model type.
 */
public abstract class BaseAdapter<M extends BaseModel>
    extends DataAdapter<M> {

  public static final String TAG = BaseAdapter.class.getSimpleName();

  private static final int TAG_KEY_CONTROLLER = R.id.card_controller;

  @Override
  public void setData(List<M> modelList) {
    super.setData(modelList);
    if (modelList == null || modelList.isEmpty()) {
      Log.d(TAG, "set data : list is null or empty");
    } else {
      Log.d(TAG, "set data : model class is " + modelList.get(0).getClass().getSimpleName());
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    BaseController cardController;
    BaseView baseView;
    View itemView;
    if (convertView instanceof BaseView) {
      baseView = (BaseView) convertView;
      itemView = baseView.getView();
      cardController = (BaseController) itemView.getTag(TAG_KEY_CONTROLLER);
    } else {
      baseView = newView(position, getItem(position), parent);
      itemView = baseView.getView();
      cardController = newController(position, getItem(position));
      itemView.setTag(TAG_KEY_CONTROLLER, cardController);
      if (SmoothScrollConfig.ENABLE_CARD_ITEM_HARDWARE) {
        itemView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
      }
    }
    Log.d(TAG, "get view position ：" + position + " model : " + getItem(position).getClass()
        + " controller : " + cardController.getClass());
    doBind(cardController, baseView, getItem(position));

    return itemView;
  }

  protected void doBind(BaseController controller, BaseView baseView, M baseModel) {
    controller.bind(baseView, baseModel);
  }


  protected abstract BaseView newView(int position, M model, ViewGroup parent);

  protected abstract BaseController newController(int position, M model);
}
