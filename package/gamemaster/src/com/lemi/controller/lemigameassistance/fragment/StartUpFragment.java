package com.lemi.controller.lemigameassistance.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.activity.ExploreActivity;
import com.lemi.controller.lemigameassistance.fragment.base.AsyncLoadFragment;
import com.lemi.controller.lemigameassistance.manager.StartupManager;
import com.lemi.controller.lemigameassistance.utils.ViewPagerUtils;
import com.lemi.controller.lemigameassistance.view.StartUpPosterView;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StartUpFragment extends AsyncLoadFragment {

  private ViewPager viewPager;
  private List<StartUpPosterView> posterViews = new ArrayList<>();

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.start_up_fragment;
  }

  @Override
  protected void onPrepareLoading() {}

  @Override
  protected void onStartLoading() {
    loadData();
  }

  private void initView() {
    viewPager = (ViewPager) contentView.findViewById(R.id.viewpager);
  }

  private void loadData() {
    List<String> path = StartupManager.getInstance().getPostersFilePath();
    if (!checkDataValid(path)) {
      finishAndJump();
      return;
    }
    setData(path);
    initViewPager();
  }

  private void setData(List<String> posterPaths) {
    if (CollectionUtils.isEmpty(posterPaths)) {
      return;
    }
    for (String filePath : posterPaths) {
      if (!TextUtils.isEmpty(filePath)) {
        if (!isAdded()) {
          continue;
        }
        StartUpPosterView startUpPosterView = StartUpPosterView.newInstance(getActivity());
        startUpPosterView.setData(filePath);
        posterViews.add(startUpPosterView);
      }
    }
  }

  private void initViewPager() {
    ViewPagerUtils.changeScrollerSpeed(viewPager);
    StartUpAdapter adapter = new StartUpAdapter();
    viewPager.setAdapter(adapter);
  }

  private boolean checkDataValid(List<String> path) {
    if (CollectionUtils.isEmpty(path)) {
      return false;
    }
    return true;
  }

  private void finishAndJump() {
    Context context;
    if (isAdded()) {
      context = getActivity();
      ExploreActivity.launch(context);
      getActivity().finish();
    } else {
      context = GlobalConfig.getAppContext();
      ExploreActivity.launch(context);
    }
  }

  private class StartUpAdapter extends PagerAdapter {

    @Override
    public void destroyItem(View container, int position, Object object) {
      StartUpPosterView posterView = posterViews.get(position);
      ((ViewPager) container).removeView(posterView);
    }

    @Override
    public Object instantiateItem(View container, int position) {
      StartUpPosterView posterView = posterViews.get(position);
      ((ViewPager) container).addView(posterView);
      return posterView;
    }

    @Override
    public int getCount() {
      // TODO Auto-generated method stub
      return posterViews.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
      // TODO Auto-generated method stub
      return arg0 == arg1;
    }

    @Override
    public int getItemPosition(Object object) {
      return POSITION_NONE;
    }

  }



}
