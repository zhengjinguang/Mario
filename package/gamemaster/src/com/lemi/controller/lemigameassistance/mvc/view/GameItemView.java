package com.lemi.controller.lemigameassistance.mvc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;
import com.lemi.mario.mvc.BaseView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameItemView extends RelativeLayout implements BaseView {

  private AsyncImageView icon;
  private ImageView state;
  private TextView name;

  public GameItemView(Context context) {
    super(context);
  }

  public GameItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public static GameItemView newInstance(ViewGroup parent) {
    return (GameItemView) ViewUtils.newInstance(parent,
        R.layout.game_little_item);
  }


  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    icon = (AsyncImageView) this.findViewById(R.id.game_item_icon);
    state = (ImageView) this.findViewById(R.id.game_item_state);
    name = (TextView) this.findViewById(R.id.game_item_name);
  }

  @Override
  public View getView() {
    return this;
  }

  public AsyncImageView getIcon() {
    return icon;
  }

  public ImageView getState() {
    return state;
  }

  public TextView getName() {
    return name;
  }



}
