package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.utils.ViewUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TipsView extends RelativeLayout {

  public enum TipsStatus {
    GONE, LOADING, FAILED
  }

  private TipsStatus status = TipsStatus.GONE;
  private OnRefreshListener onRefreshListener;
  private OnFocusLostListener onFocusLostListener;

  private RelativeLayout tipsContainer;
  private ImageView tipsImage;
  private Button refreshButton;

  public TipsView(Context context) {
    super(context);
  }

  public TipsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public static TipsView newInstance(ViewGroup parent) {
    return (TipsView) ViewUtils.newInstance(parent,
        R.layout.tips_layout);
  }

  public static TipsView newInstance(Context context) {
    return (TipsView) ViewUtils.newInstance(context,
        R.layout.tips_layout);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    tipsContainer = (RelativeLayout) this.findViewById(R.id.tips_container);
    tipsContainer.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
          if (status == TipsStatus.FAILED) {
            refreshButton.requestFocus();
          }
        }
      }
    });
    tipsImage = (ImageView) this.findViewById(R.id.tips_image);
    refreshButton = (Button) this.findViewById(R.id.tips_refresh_button);
    status = TipsStatus.GONE;
    notifyUIChange();
  }

  public void changeTipsStatus(TipsStatus status) {
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

  public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
    this.onRefreshListener = onRefreshListener;
  }

  public void setOnFocusLostListener(OnFocusLostListener onFocusLostListener) {
    this.onFocusLostListener = onFocusLostListener;
  }

  public void requestLoadingFocus() {
    tipsContainer.requestFocus();
  }

  public void requestRefreshButtonFocus() {
    refreshButton.requestFocus();
  }


  private void notifyUIChange() {

    switch (status) {

      case GONE:
        checkFocusAndNotify();
        tipsContainer.setVisibility(GONE);
        break;

      case LOADING:
        tipsContainer.setVisibility(VISIBLE);
        tipsImage.setVisibility(VISIBLE);
        tipsImage.setImageResource(R.drawable.tips_loading);
        refreshButton.setVisibility(GONE);
        break;

      case FAILED:
        tipsContainer.setVisibility(VISIBLE);
        tipsImage.setVisibility(VISIBLE);
        tipsImage.setImageResource(R.drawable.tips_fail);
        refreshButton.setVisibility(VISIBLE);
        requestRefreshButtonFocus();
        refreshButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (onRefreshListener != null) {
              MainThreadPostUtils.post(new Runnable() {
                @Override
                public void run() {
                  setRefreshStatus();
                  onRefreshListener.onRefresh();
                }
              });
            }
          }
        });
        break;

      default:
        break;
    }
  }

  private void setRefreshStatus() {
    this.status = TipsStatus.LOADING;
    tipsContainer.setVisibility(VISIBLE);
    tipsImage.setVisibility(VISIBLE);
    tipsImage.setImageResource(R.drawable.tips_loading);
    requestLoadingFocus();
    refreshButton.setVisibility(GONE);
  }

  private void checkFocusAndNotify() {
    if (findFocus() != null && onFocusLostListener != null) {
      onFocusLostListener.onFocusLost();
    }
  }


  public interface OnRefreshListener {
    void onRefresh();
  }

  public interface OnFocusLostListener {
    void onFocusLost();
  }
}
