package com.lemi.mario.widget.helper.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.widget.helper.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class BottomTipsView extends RelativeLayout {

  private static final String TIPS_TIMER = "tips_timer";
  private static final int TOAST_TIME = 1500;

  public enum TipsStatus {
    GONE, ERROR_SHOW, TIPS_SHOW, TOAST_SHOW, EXPAND_SHOW
  }

  private TipsStatus status = TipsStatus.GONE;

  private TextView tipsText;
  private ImageView tipsImage;
  private String tips;
  private Animation animation;

  private Timer tipsTimer;
  private TimerTask tipsTask;

  private boolean toastFlag = false;
  private boolean animFlag = false;

  public BottomTipsView(Context context) {
    super(context);
  }

  public BottomTipsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public static BottomTipsView newInstance(ViewGroup parent) {
    return (BottomTipsView) ViewUtils.newInstance(parent, R.layout.mount_tips_view);
  }

  public static BottomTipsView newInstance(Context context) {
    return (BottomTipsView) ViewUtils.newInstance(context, R.layout.mount_tips_view);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    tipsText = (TextView) this.findViewById(R.id.tips_text);
    tipsImage = (ImageView) this.findViewById(R.id.tips_image);
    status = TipsStatus.GONE;
    notifyUIChange();
  }

  public void showToastTips(String tips) {
    this.tips = tips;
    changeStatus(TipsStatus.TOAST_SHOW);
  }

  public void showExpandTips(String tips) {
    this.tips = tips;
    changeStatus(TipsStatus.EXPAND_SHOW);
  }

  public void showTips(String tips) {
    this.tips = tips;
    changeStatus(TipsStatus.TIPS_SHOW);
  }

  public void showErrorTips(String tips) {
    this.tips = tips;
    changeStatus(TipsStatus.ERROR_SHOW);
  }

  public void hideTips() {
    this.tips = null;
    changeStatus(TipsStatus.GONE);
  }

  private void changeStatus(TipsStatus status) {
    this.status = status;
    MainThreadPostUtils.post(new Runnable() {
      @Override
      public void run() {
        notifyUIChange();
      }
    });
  }

  public TipsStatus getTipsStatus() {
    return status;
  }

  private void notifyUIChange() {
    reset();
    switch (status) {
      case GONE:
        this.setVisibility(GONE);
        break;

      case ERROR_SHOW:
        if (TextUtils.isEmpty(tips)) {
          return;
        }
        tipsText.setText(tips);
        tipsImage.setVisibility(VISIBLE);
        this.setVisibility(VISIBLE);
        break;

      case TIPS_SHOW:
        if (TextUtils.isEmpty(tips)) {
          return;
        }
        tipsText.setText(tips);
        tipsImage.setVisibility(GONE);
        this.setVisibility(VISIBLE);
        break;

      case TOAST_SHOW:
        if (TextUtils.isEmpty(tips)) {
          return;
        }
        toastFlag = true;
        tipsText.setText(tips);
        tipsImage.setVisibility(GONE);
        this.setVisibility(VISIBLE);
        startTimer();
        break;
      case EXPAND_SHOW:
        if (TextUtils.isEmpty(tips)) {
          return;
        }
        tipsText.setText(tips);
        tipsImage.setVisibility(GONE);
        this.setVisibility(VISIBLE);
        startExpandAnim();
        break;

      default:
        break;
    }
  }


  public void startTimer() {
    cancelTimer();
    tipsTimer = new Timer(TIPS_TIMER, true);
    tipsTask = new TipsTask();
    tipsTimer.schedule(tipsTask, TOAST_TIME);
  }

  private void cancelTimer() {
    if (tipsTimer != null) {
      tipsTimer.cancel();
    }
  }

  private void reset() {
    toastFlag = false;
    animFlag = false;
    clearAnimation();
    cancelTimer();
  }

  private class TipsTask extends TimerTask {
    @Override
    public void run() {
      if (toastFlag) {
        MainThreadPostUtils.post(new Runnable() {
          @Override
          public void run() {
            startQuitAnim();
          }
        });
      }
    }
  }

  private void startExpandAnim() {
    animation = AnimationUtils.loadAnimation(getContext(), R.anim.tips_toast_expand);
    startAnimation(animation);
  }

  private void startQuitAnim() {
    animFlag = true;
    animation = AnimationUtils.loadAnimation(getContext(), R.anim.tips_toast_down);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        if (toastFlag && animFlag) {
          animFlag = false;
          BottomTipsView.this.clearAnimation();
          BottomTipsView.this.setVisibility(GONE);
        }
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });
    startAnimation(animation);
  }

}
