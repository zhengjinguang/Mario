package com.lemi.mario.widget.helper.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.lemi.mario.widget.helper.R;
import com.lemi.mario.widget.helper.WidgetHelperApplication;
import com.lemi.mario.widget.helper.activity.InstallActivity;
import com.lemi.mario.widget.helper.activity.MountActivity;
import com.lemi.mario.widget.helper.fragment.base.BaseFragment;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ExploreFragment extends BaseFragment {


  private ImageView mountCard;
  private ImageView installCard;

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    setListener();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.explore_fragment;
  }

  private void initView() {
    mountCard = (ImageView) contentView.findViewById(R.id.explore_mount_card);
  }

  private void setListener() {
    mountCard.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isAdded()) {
          MountActivity.launch(getActivity());
        } else {
          MountActivity.launch(WidgetHelperApplication.getAppContext());
        }

      }
    });
  }
}
