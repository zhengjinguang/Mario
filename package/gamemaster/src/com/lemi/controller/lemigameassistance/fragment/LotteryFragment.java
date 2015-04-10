package com.lemi.controller.lemigameassistance.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lemi.controller.lemigameassistance.FakeAwardsFactory;
import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.account.GameMasterAccountManager;
import com.lemi.controller.lemigameassistance.config.Intents;
import com.lemi.controller.lemigameassistance.config.LotteryReturnValues;
import com.lemi.controller.lemigameassistance.dialog.GameMasterDialog;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;
import com.lemi.controller.lemigameassistance.model.AwardListModel;
import com.lemi.controller.lemigameassistance.model.AwardModel;
import com.lemi.controller.lemigameassistance.model.DrawLotteryModel;
import com.lemi.controller.lemigameassistance.model.OtherAwardListModel;
import com.lemi.controller.lemigameassistance.model.OtherAwardModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.mario.accountmanager.MarioAccountManager;
import com.lemi.mario.accountmanager.config.ReturnValues;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.base.utils.DialogUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class LotteryFragment extends BaseFragment {
  private static final long DRAW_TIME = 3000l;
  private static final long DRAW_NO_AWARD_TIME = 1000l;
  private static final long DUMPLING_CHANGE_INTERNAl = 500l;
  private static final long DAY_TIME_BY_SECOND = 60 * 60 * 24l;
  private TextView nick;
  private TextView id;
  private TextView awardsTitle;
  private TextView awards;
  private Button getAwards;
  private Button drawLottery;
  private long lotteryStartTime;
  private long lotteryStopTime;
  private User user;
  private Random rd = new Random();
  private int selectedPosition = 3;
  Runnable lottery = new Runnable() {
    @Override
    public void run() {
      int nextSeleted = rd.nextInt(dumplingsId.length);
      while (selectedPosition == nextSeleted) {
        nextSeleted = rd.nextInt(dumplingsId.length);
      }
      getViewById(dumplingsId[selectedPosition]).setImageResource(R.drawable.lottery_dumpling);
      getViewById(dumplingsId[nextSeleted]).setImageResource(R.drawable.lottery_dumpling_selected);
      getViewById(chopsticksId[selectedPosition]).setVisibility(View.INVISIBLE);
      getViewById(chopsticksId[nextSeleted]).setVisibility(View.VISIBLE);
      selectedPosition = nextSeleted;
      MainThreadPostUtils.postDelayed(this, DUMPLING_CHANGE_INTERNAl);
    }
  };
  private int[] dumplingsId = new int[] {
      R.id.lottery_dumpling_10, R.id.lottery_dumpling_11, R.id.lottery_dumpling_12,
      R.id.lottery_dumpling_13,
      R.id.lottery_dumpling_20, R.id.lottery_dumpling_21, R.id.lottery_dumpling_22,
      R.id.lottery_dumpling_23,
      R.id.lottery_dumpling_30, R.id.lottery_dumpling_31, R.id.lottery_dumpling_32,
      R.id.lottery_dumpling_33,
  };
  private int[] chopsticksId = new int[] {
      R.id.lottery_chopsticks_10, R.id.lottery_chopsticks_11, R.id.lottery_chopsticks_12,
      R.id.lottery_chopsticks_13,
      R.id.lottery_chopsticks_20, R.id.lottery_chopsticks_21, R.id.lottery_chopsticks_22,
      R.id.lottery_chopsticks_23,
      R.id.lottery_chopsticks_30, R.id.lottery_chopsticks_31, R.id.lottery_chopsticks_32,
      R.id.lottery_chopsticks_33,
  };

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    getBundle();
    initView();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.lottery_fragment;
  }

  private void getBundle() {
    Bundle bundle = getArguments();
    if (bundle != null) {
      lotteryStartTime = bundle.getLong(Intents.INTENT_EXTRA_LOTTERY_START_TIME);
      lotteryStopTime = bundle.getLong(Intents.INTENT_EXTRA_LOTTERY_STOP_TIME);
    }
  }

  private void initView() {
    nick = (TextView) contentView.findViewById(R.id.lottery_nick);
    id = (TextView) contentView.findViewById(R.id.lottery_uid);
    awardsTitle = (TextView) contentView.findViewById(R.id.lottery_awards_title);
    awards = (TextView) contentView.findViewById(R.id.lottery_awards);
    getAwards = (Button) contentView.findViewById(R.id.lottery_getawards);
    drawLottery = (Button) contentView.findViewById(R.id.lottery_draw);
    initListeners();
    setData();
  }

  private void initListeners() {
    getAwards.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.lotteryClick(LogHelper.LOTTERY_GET_AWARDS);

        if (GameMasterAccountManager.getInstance().isLogined()) {
          DataUtils.runAsyncTask(new AwardsAsyncTask());
          MainThreadPostUtils.getHandler().removeCallbacks(lottery);
        } else {
          showLoginDialog(StringUtil.getString(R.string.account_login_dialog));
        }
      }
    });
    drawLottery.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.lotteryClick(LogHelper.LOTTERY_DRAW);

        if (GameMasterAccountManager.getInstance().isLogined()) {
          drawLottery.setEnabled(false);
          DataUtils.runAsyncTask(new DrawLotteryAsyncTask());
          MainThreadPostUtils.postDelayed(lottery, DUMPLING_CHANGE_INTERNAl);
        } else {
          showLoginDialog(StringUtil.getString(R.string.account_login_dialog));
        }
      }
    });
  }

  private void showLoginDialog(String message) {
    Dialog dialog = new GameMasterDialog.Builder(getActivity())
        .setMessage(message)
        .setPositiveButton(R.string.account_login_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final Dialog dialogProgress =
                new GameMasterDialog.Builder(getActivity(), R.style.dialog_progress)
                    .setMessage(R.string.account_logining)
                    .create();
            dialogProgress.setCancelable(false);
            DialogUtils.showDialog(dialogProgress);
            GameMasterAccountManager.getInstance().registerOnLoginListener(
                new GameMasterAccountManager.OnLoginListener() {
                  @Override
                  public void onSucceed() {
                    MainThreadPostUtils.toast(R.string.account_login_success);
                    DialogUtils.dismissDialog(dialogProgress);
                    GameMasterAccountManager.getInstance().unRegisterOnLoginListener(this);
                  }

                  @Override
                  public void onFail(MarioAccountManager.AccountError accountError, String reason) {
                    MainThreadPostUtils.toast(R.string.network_not_available);
                    DialogUtils.dismissDialog(dialogProgress);
                    GameMasterAccountManager.getInstance().unRegisterOnLoginListener(this);
                  }
                });
            GameMasterAccountManager.getInstance().startLogin();
          }
        })
        .setNegativeButton(R.string.account_login_cancel, null)
        .create();
    DialogUtils.showDialog(dialog);
  }

  private void setData() {
    user = GameMasterAccountManager.getInstance().getUserInfo();
    LogHelper
        .lotteryDetailClick(com.lemi.controller.lemigameassistance.config.Constants.LOTTERY_ID);
    if (user != null) {
      nick.append(user.getNick());
      id.append(com.lemi.controller.lemigameassistance.config.Constants.USERID_PREFIX);
      id.append(String.valueOf(user.getUid()));
    }
    if (lotteryStartTime + DAY_TIME_BY_SECOND > System.currentTimeMillis() / 1000 ||
        lotteryStopTime + DAY_TIME_BY_SECOND < System.currentTimeMillis() / 1000) {
      awardsTitle.setVisibility(View.INVISIBLE);
      awards.setVisibility(View.INVISIBLE);
    } else {
      DataUtils.runAsyncTask(new OtherAwardsAsyncTask());
    }
  }

  private ImageView getViewById(int id) {
    return (ImageView) contentView.findViewById(id);
  }

  public void setAwardsInfo(List<OtherAwardModel> list) {
    if (list == null) {
      list = new ArrayList<>();
    }
    StringBuilder builder = new StringBuilder();
    List<OtherAwardModel> newList = getShowList(list);
    for (OtherAwardModel award : newList) {
      builder.append(award.toString());
    }
    awardsTitle.setVisibility(View.VISIBLE);
    awards.setVisibility(View.VISIBLE);
    awards.setText(builder.toString());
  }

  private List<OtherAwardModel> getShowList(List<OtherAwardModel> list) {
    Collections.sort(list, new Comparator<OtherAwardModel>() {
      @Override
      public int compare(OtherAwardModel lhs, OtherAwardModel rhs) {
        if (lhs.getAwardTime() < rhs.getAwardTime()) {
          return 1;
        } else if (lhs.getAwardTime() > rhs.getAwardTime()) {
          return -1;
        }
        return 0;
      }
    });

    List<OtherAwardModel> newList = new ArrayList<>();
    newList.add(FakeAwardsFactory.generateFakeAward());
    newList.add(FakeAwardsFactory.generateFakeAward());
    if (list.size() > 0) {
      newList.add(list.get(0));
    } else {
      newList.add(FakeAwardsFactory.generateFakeAward());
    }
    if (list.size() > 1) {
      newList.add(list.get(1));
    } else {
      newList.add(FakeAwardsFactory.generateFakeAward());
    }
    newList.add(FakeAwardsFactory.generateFakeAward());
    newList.add(FakeAwardsFactory.generateFakeAward());
    return newList;
  }

  class AwardsAsyncTask extends AsyncTask<Void, Void, AwardListModel> {

    @Override
    protected AwardListModel doInBackground(Void... params) {
      return GameMasterHttpHelper.getAwards();
    }

    @Override
    protected void onPostExecute(AwardListModel awardListModel) {
      super.onPostExecute(awardListModel);
      if (!isAdded()) {
        return;
      }
      if (awardListModel == null) {
        MainThreadPostUtils.toast(R.string.network_not_available);
        return;
      }
      if (awardListModel.getRet() == ReturnValues.VALID_RETURN) {
        if (awardListModel.getAwards() != null && awardListModel.getAwards().size() > 0) {
          List<String> awards = new ArrayList<>();
          for (AwardModel award : awardListModel.getAwards()) {
            awards.add(award.toString());
          }
          Dialog dialogListView =
              new GameMasterDialog.Builder(getActivity(), R.style.dialog_list)
                  .setMessages(awards)
                  .setPositiveButton(R.string.draw_lottery_dialog_ok, null)
                  .create();
          DialogUtils.showDialog(dialogListView);
        } else {
          MainThreadPostUtils.toast(R.string.lottery_no_awards);
        }
      } else if (awardListModel.getRet() == ReturnValues.AUTHCODE_INVALID) {
        GameMasterAccountManager.getInstance().logout();
        showLoginDialog(StringUtil.getString(R.string.account_auth_invalidate_dialog));
      } else {
        MainThreadPostUtils.toast(R.string.network_not_available);
      }
    }
  }
  class OtherAwardsAsyncTask extends AsyncTask<Void, Void, OtherAwardListModel> {
    @Override
    protected OtherAwardListModel doInBackground(Void... params) {
      return GameMasterHttpHelper.getOtherAwards();
    }

    @Override
    protected void onPostExecute(OtherAwardListModel otherAwardListModel) {
      super.onPostExecute(otherAwardListModel);
      if (!isAdded()) {
        return;
      }
      if (otherAwardListModel != null) {
        if (otherAwardListModel.getAwards() != null) {
          setAwardsInfo(otherAwardListModel.getAwards());
        }
      } else {
        MainThreadPostUtils.toast(R.string.network_not_available);
      }
    }
  }

  class DrawLotteryAsyncTask extends AsyncTask<Void, Void, DrawLotteryModel> {
    @Override
    protected DrawLotteryModel doInBackground(Void... params) {
      return GameMasterHttpHelper.drawLottery();
    }

    @Override
    protected void onPostExecute(final DrawLotteryModel drawLotteryModel) {
      super.onPostExecute(drawLotteryModel);
      if (!isAdded()) {
        return;
      }
      if (drawLotteryModel == null) {
        MainThreadPostUtils.getHandler().removeCallbacks(lottery);
        MainThreadPostUtils.toast(R.string.network_not_available);
        drawLottery.setEnabled(true);
        return;
      }
      if (drawLotteryModel.getRet() == ReturnValues.VALID_RETURN) {
        MainThreadPostUtils.postDelayed(new Runnable() {
          @Override
          public void run() {
            if (!isAdded()) {
              return;
            }
            MainThreadPostUtils.getHandler().removeCallbacks(lottery);
            drawLottery.setEnabled(true);
            final Toast toast = new Toast(getActivity());
            TextView view = new TextView(getActivity());
            view.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
            view.setGravity(Gravity.CENTER);
            view.setBackgroundResource(R.drawable.lottery_coin);
            view.setText(drawLotteryModel.getAwardName());
            view.setTextColor(Color.WHITE);
            view.setTextSize(30f);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
          }
        }, DRAW_TIME);
      } else if (drawLotteryModel.getRet() == ReturnValues.AUTHCODE_INVALID) {
        GameMasterAccountManager.getInstance().logout();
        showLoginDialog(StringUtil.getString(R.string.account_auth_invalidate_dialog));
      } else {
        long delayedTime = DRAW_NO_AWARD_TIME;
        if (drawLotteryModel.getRet() == LotteryReturnValues.LOTTERY_NO_AWARD) {
          delayedTime = DRAW_TIME;
        }
        MainThreadPostUtils.postDelayed(new Runnable() {
          @Override
          public void run() {
            if (!isAdded()) {
              return;
            }
            MainThreadPostUtils.getHandler().removeCallbacks(lottery);
            new GameMasterDialog.Builder(getActivity())
                .setMessage(drawLotteryModel.getReason())
                .setPositiveButton(R.string.draw_lottery_dialog_ok, null)
                .show();
            drawLottery.setEnabled(true);
          }
        }, delayedTime);
      }

    }
  }
}
