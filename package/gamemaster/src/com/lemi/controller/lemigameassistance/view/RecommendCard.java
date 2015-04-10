package com.lemi.controller.lemigameassistance.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.account.GameMasterAccountManager;
import com.lemi.controller.lemigameassistance.activity.GameDetailActivity;
import com.lemi.controller.lemigameassistance.activity.LotteryActivity;
import com.lemi.controller.lemigameassistance.activity.SubjectDetailActivity;
import com.lemi.controller.lemigameassistance.dialog.GameMasterDialog;
import com.lemi.controller.lemigameassistance.helper.PosterHelper;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.mario.accountmanager.MarioAccountManager;
import com.lemi.mario.base.utils.DialogUtils;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.StringUtil;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RecommendCard extends LinearLayout {

  public static final int TAG_CARD_EVENT = R.id.recommend_card_event;
  private static final int TAG_CARD_TYPE = R.id.recommend_card_type;
  private RecommendCardContentType contentType = RecommendCardContentType.GAME;
  private String token;
  private String title;
  private AsyncImageView recommendImage;

  public RecommendCard(Context context) {
    super(context);
  }

  public RecommendCard(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RecommendCard(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public static RecommendCard newInstance(ViewGroup parent, RecommendCardType type) {
    RecommendCard recommendCard;
    if (type == RecommendCardType.LARGE) {
      recommendCard = (RecommendCard) ViewUtils.newInstance(parent, R.layout.recommend_large_card);
      recommendCard.setTag(TAG_CARD_TYPE, RecommendCardType.LARGE.toString());
    } else {
      recommendCard = (RecommendCard) ViewUtils.newInstance(parent, R.layout.recommend_little_card);
      recommendCard.setTag(TAG_CARD_TYPE, RecommendCardType.LITTLE.toString());
    }
    return recommendCard;
  }

  public static RecommendCard newInstance(Context context, RecommendCardType type) {
    if (type == RecommendCardType.LARGE) {
      return (RecommendCard) ViewUtils.newInstance(context, R.layout.recommend_large_card);
    } else {
      return (RecommendCard) ViewUtils.newInstance(context, R.layout.recommend_little_card);
    }
  }

  public View getView() {
    return this;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initView();
  }

  public void setData(final RecommendCardContentType contentType, String iconUrl, String token,
      String title) {

    this.contentType = contentType;
    this.token = token;
    this.title = title;

    if (judgeCardType() == RecommendCardType.LARGE) {
      loadImage(iconUrl, R.drawable.recommend_large_card_default);
    } else {
      loadImage(iconUrl, R.drawable.recommend_little_card_default);
    }
  }

  private void initView() {
    recommendImage = (AsyncImageView) findViewById(R.id.recommend_image);
    recommendImage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (TextUtils.isEmpty(token)) {
          return;
        }
        String eventMsg = (String) RecommendCard.this.getTag(TAG_CARD_EVENT);
        if (eventMsg != null) {
          LogHelper.recommendClick(eventMsg);
        }
        if (contentType == RecommendCardContentType.GAME) {
          GameDetailActivity.launch(RecommendCard.this.getContext(), token);
        } else if (contentType == RecommendCardContentType.SUBJECT) {
          SubjectDetailActivity.launch(RecommendCard.this.getContext(), Long.valueOf(token), title);
        } else {
          if (GameMasterAccountManager.getInstance().isLogined()) {
            LotteryActivity.launch(RecommendCard.this.getContext(),
                Long.valueOf(token), Long.valueOf(title));
          } else {
            jumpToLottery();
          }
        }

      }
    });
  }

  private void jumpToLottery() {
    final Dialog dialogProgress =
        new GameMasterDialog.Builder(RecommendCard.this.getContext(), R.style.dialog_progress)
            .setMessage(R.string.account_logining)
            .create();
    dialogProgress.setCancelable(false);
    DialogUtils.showDialog(dialogProgress);
    GameMasterAccountManager.getInstance().registerOnLoginListener(
        new GameMasterAccountManager.OnLoginListener() {
          @Override
          public void onSucceed() {
            LotteryActivity.launch(RecommendCard.this.getContext(),
                Long.valueOf(token), Long.valueOf(title));
            MainThreadPostUtils.toast(R.string.account_login_success);
            DialogUtils.dismissDialog(dialogProgress);
            GameMasterAccountManager.getInstance().unRegisterOnLoginListener(this);
          }

          @Override
          public void onFail(MarioAccountManager.AccountError accountError, String reason) {
            DialogUtils.dismissDialog(dialogProgress);
            GameMasterAccountManager.getInstance().unRegisterOnLoginListener(this);
            if (accountError == MarioAccountManager.AccountError.NOT_REGISTERED) {
              MainThreadPostUtils.toast(StringUtil.getString(R.string.account_login_failed));
            } else {
              MainThreadPostUtils.toast(R.string.network_not_available);
            }
          }
        });
    GameMasterAccountManager.getInstance().startLogin();
  }

  private RecommendCardType judgeCardType() {
    if (RecommendCardType.LARGE.toString().equals(getTag(TAG_CARD_TYPE))) {
      return RecommendCardType.LARGE;
    }
    return RecommendCardType.LITTLE;
  }

  private void loadImage(String url, int resId) {
    String filePath = PosterHelper.getPosterFileNameFromUrl(url);
    if (FileUtil.exists(filePath)) {
      recommendImage.loadLocalImageImmediate(filePath);
    } else {
      recommendImage.loadNetworkImage(url, resId);
    }
  }

  public enum RecommendCardType {
    LARGE, LITTLE
  }

  public enum RecommendCardContentType {
    GAME, SUBJECT, LOTTERY
  }

}
