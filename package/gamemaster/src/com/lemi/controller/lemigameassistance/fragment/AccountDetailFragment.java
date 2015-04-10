package com.lemi.controller.lemigameassistance.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.account.GameMasterAccountManager;
import com.lemi.controller.lemigameassistance.activity.LotteryActivity;
import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.dialog.GameMasterDialog;
import com.lemi.controller.lemigameassistance.fragment.base.BaseFragment;
import com.lemi.controller.lemigameassistance.model.GetLotteryModel;
import com.lemi.controller.lemigameassistance.net.GameMasterHttpHelper;
import com.lemi.controller.lemigameassistance.utils.DataUtils;
import com.lemi.controller.lemigameassistance.utils.FormateUtils;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.mario.accountmanager.MarioAccountManager;
import com.lemi.mario.accountmanager.config.ReturnValues;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.base.utils.DialogUtils;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.StringUtil;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class AccountDetailFragment extends BaseFragment {
  private ImageView avatar;
  private ImageView changeGender;
  private ImageView lottery;
  private TextView id;
  private EditText nick;
  private EditText phone;
  private EditText email;
  private ImageView save;

  private User user;
  GameMasterAccountManager.OnUserInfoChangeListener onUserInfoChangeListener =
      new GameMasterAccountManager.OnUserInfoChangeListener() {

        @Override
        public void onSucceed(User newUser) {
          MainThreadPostUtils.toast(R.string.change_userinfo_success);
          user = newUser;
        }

        @Override
        public void onFail(MarioAccountManager.AccountError accountError, String reason) {
          if (accountError == MarioAccountManager.AccountError.NETWORK_INVALID) {
            MainThreadPostUtils.toast(R.string.network_not_available);
          } else if (accountError == MarioAccountManager.AccountError.AUTH_INVALID) {
            showLoginDialog(StringUtil.getString(R.string.account_auth_invalidate_dialog));
          } else {
            MainThreadPostUtils.toast(getString(R.string.change_userinfo_fail)
                + reason);
          }
        }
      };
  private GetLotteryModel lotteryModel;
  private int gender = 0;

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    setData();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.account_detail_fragment;
  }

  private void initView() {
    avatar = (ImageView) contentView.findViewById(R.id.account_avatar);
    changeGender = (ImageView) contentView.findViewById(R.id.account_change_gender);
    lottery = (ImageView) contentView.findViewById(R.id.account_lottery);
    id = (TextView) contentView.findViewById(R.id.account_id);
    nick = (EditText) contentView.findViewById(R.id.account_nick);
    phone = (EditText) contentView.findViewById(R.id.account_phone);
    email = (EditText) contentView.findViewById(R.id.account_email);
    save = (ImageView) contentView.findViewById(R.id.account_save);
    changeGender.setNextFocusRightId(R.id.account_nick);
    lottery.setNextFocusRightId(R.id.account_save);
    lottery.requestFocus();
    DataUtils.runAsyncTask(new GetLotteryAsyncTask());
    initListeners();
  }

  private void initListeners() {
    changeGender.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setAvatar((gender + 1) % 2);
      }
    });
    lottery.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.accountClick(LogHelper.ACCOUNT_LOTTERY);
        if (GameMasterAccountManager.getInstance().isLogined()) {
          if (lotteryModel != null) {
            LotteryActivity.launch(getActivity(), lotteryModel.getStartTime(),
                lotteryModel.getStopTime());
          } else {
            LotteryActivity.launch(getActivity(), System.currentTimeMillis(),
                System.currentTimeMillis());
          }
        } else {
          showLoginDialog(StringUtil.getString(R.string.account_login_dialog));
        }
      }
    });
    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.accountClick(LogHelper.ACCOUNT_CHANGE_INFO);
        if (GameMasterAccountManager.getInstance().isLogined()) {
          final User newUser = getNewUser();
          if (!validateData()) {
            return;
          }
          GameMasterAccountManager.getInstance().asyncChangeUserInfo(newUser);
        } else {
          showLoginDialog(StringUtil.getString(R.string.account_login_dialog));
        }

      }
    });
    GameMasterAccountManager.getInstance().registerUserInfoChangeListener(
        onUserInfoChangeListener);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    GameMasterAccountManager.getInstance().unRegisterUserInfoChangeListener(
        onUserInfoChangeListener);
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

  private boolean validateData() {
    User newUser = getNewUser();
    boolean ret = true;
    if (TextUtils.isEmpty(newUser.getNick())) {
      MainThreadPostUtils.toast(R.string.account_nick_null);
      ret = false;
    } else if (TextUtils.isEmpty(user.getPhone())) {
      if (!TextUtils.isEmpty(newUser.getPhone()) && !FormateUtils.isValidPhone(newUser.getPhone())) {
        MainThreadPostUtils.toast(R.string.account_phone_wrong);
        ret = false;
      }
    } else if (TextUtils.isEmpty(newUser.getPhone())) {
      MainThreadPostUtils.toast(R.string.account_phone_null);
      ret = false;
    } else if (!FormateUtils.isValidPhone(newUser.getPhone())) {
      MainThreadPostUtils.toast(R.string.account_phone_wrong);
      ret = false;
    } else if (TextUtils.isEmpty(user.getEmail())) {
      if (!TextUtils.isEmpty(newUser.getEmail()) && !FormateUtils.isValidEmail(newUser.getEmail())) {
        MainThreadPostUtils.toast(R.string.account_email_wrong);
        ret = false;
      }
    } else if (TextUtils.isEmpty(newUser.getEmail())) {
      MainThreadPostUtils.toast(R.string.account_email_null);
      ret = false;
    } else if (!FormateUtils.isValidEmail(newUser.getEmail())) {
      MainThreadPostUtils.toast(R.string.account_email_wrong);
      ret = false;
    }
    return ret ? hasDataChange(user, newUser) : ret;
  }

  private boolean hasDataChange(User user, User newUser) {
    if (user.getGender() == newUser.getGender()
        && user.getNick().equals(newUser.getNick())
        && user.getPhone().equals(newUser.getPhone())
        && user.getEmail().equals(newUser.getEmail())) {
      return false;
    }
    return true;
  }

  private User getNewUser() {
    User user = new User();
    user.setUid(this.user.getUid());
    user.setUdid(this.user.getUdid());
    user.setGender(gender);
    user.setNick(nick.getText().toString().trim());
    user.setPhone(phone.getText().toString().trim());
    user.setEmail(email.getText().toString().trim());
    return user;
  }

  private void setData() {
    LogHelper.accountDetailClick();

    user = GameMasterAccountManager.getInstance().getUserInfo();
    if (user != null) {
      id.append(Constants.USERID_PREFIX);
      id.append(String.valueOf(user.getUid()));
      setAvatar(user.getGender());
      nick.setText(user.getNick());
      phone.setText(user.getPhone());
      email.setText(user.getEmail());
    }
  }

  private void setAvatar(int gender) {
    this.gender = gender % 2;
    if (this.gender % 2 == 0) {
      avatar.setImageResource(R.drawable.gender_girl);
    }
    else {
      avatar.setImageResource(R.drawable.gender_boy);
    }
  }

  class GetLotteryAsyncTask extends AsyncTask<Void, Void, GetLotteryModel> {
    @Override
    protected GetLotteryModel doInBackground(Void... params) {
      return GameMasterHttpHelper.getLottery();
    }

    @Override
    protected void onPostExecute(GetLotteryModel getLotteryModel) {
      super.onPostExecute(getLotteryModel);
      if (getLotteryModel != null && getLotteryModel.getRet() == ReturnValues.VALID_RETURN) {
        lotteryModel = getLotteryModel;
      } else {
        MainThreadPostUtils.toast(R.string.network_not_available);
      }

    }
  }

}
