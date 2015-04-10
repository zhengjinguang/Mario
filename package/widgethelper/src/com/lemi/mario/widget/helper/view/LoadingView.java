package com.lemi.mario.widget.helper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.widget.helper.R;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LoadingView extends ImageView {

  private Animation loadingAnimation;
  private LoadingStatus status;

  public enum LoadingStatus {
    GONE, SHOW
  }

  public LoadingView(Context context) {
    super(context);
  }

  public LoadingView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public static LoadingView newInstance(ViewGroup parent) {
    return (LoadingView) ViewUtils.newInstance(parent, R.layout.loading_view);
  }

  public static LoadingView newInstance(Context context) {
    return (LoadingView) ViewUtils.newInstance(context, R.layout.loading_view);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initAnim();
  }

  public LoadingStatus getStatus() {
    return status;
  }

  public void show() {
    this.status = LoadingStatus.SHOW;
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        notifyUIChange();
      }
    });
  }

  public void hide() {
    this.status = LoadingStatus.GONE;
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        notifyUIChange();
      }
    });
  }

  private void initAnim() {
    loadingAnimation =
        AnimationUtils.loadAnimation(GlobalConfig.getAppContext(), R.anim.loading_rotate);
    loadingAnimation.setInterpolator(new LinearInterpolator());
  }

  private void notifyUIChange() {
    switch (status) {
      case GONE:
        clearAnimation();
        setVisibility(GONE);
        break;
      case SHOW:
        setVisibility(VISIBLE);
        startAnimation(loadingAnimation);
        break;
      default:
        break;
    }
  }

}
