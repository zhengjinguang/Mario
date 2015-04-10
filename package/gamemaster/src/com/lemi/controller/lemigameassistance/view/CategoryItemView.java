package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.activity.CategoryDetailActivity;
import com.lemi.controller.lemigameassistance.focus.anim.ScaleAnimHelper;
import com.lemi.controller.lemigameassistance.recycleview.model.CategoryInfo;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author zhengjinguang@letv.com (shining)
 */
public class CategoryItemView extends LinearLayout {

  private ScaleAnimHelper scaleAnimHelper;

  private RelativeLayout itemLayout;
  private AsyncImageView iconView;

  private CategoryInfo categoryInfo;

  private ItemClickListener itemClickListener = new ItemClickListener();

  public CategoryItemView(Context context) {
    super(context);
  }

  public CategoryItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
    scaleAnimHelper = new ScaleAnimHelper(context, attrs);
  }

  public static CategoryItemView newInstance(ViewGroup parent) {
    return (CategoryItemView) ViewUtils.newInstance(parent, R.layout.category_card_item);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    itemLayout = (RelativeLayout) findViewById(R.id.category_item);
    iconView = (AsyncImageView) findViewById(R.id.category_item_icon);
    scaleAnimHelper.setFocusListener(itemLayout);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    scaleAnimHelper.onLayout(itemLayout);
  }

  public void bind(CategoryInfo categoryInfo) {
    if (categoryInfo == null) {
      return;
    }
    this.categoryInfo = categoryInfo;
    if (categoryInfo.isEmpty()) {
      setVisibility(INVISIBLE);
      itemLayout.setVisibility(INVISIBLE);
      return;
    }
    setVisibility(VISIBLE);
    itemLayout.setVisibility(VISIBLE);
    itemLayout.setOnClickListener(itemClickListener);
    if (categoryInfo.getIconResId() > 0) {
      iconView.setImageResource(categoryInfo.getIconResId());
    } else {
      iconView.loadNetworkImage(categoryInfo.getIconUrl(), R.drawable.category_card_default);
    }
  }


  private class ItemClickListener implements OnClickListener {

    @Override
    public void onClick(View v) {
      CategoryDetailActivity.launch(getContext(), categoryInfo.getCid(), categoryInfo.getName(),
          categoryInfo.getCount());
    }
  }

}
