package com.lemi.mario.widget.helper.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.StringUtil;
import com.lemi.mario.externalmanager.model.ExternalStorageInfo;
import com.lemi.mario.widget.helper.R;
import com.lemi.mario.widget.helper.WidgetHelpPreferences;
import com.lemi.mario.widget.helper.dialog.WidgetHelperAlertDialog;
import com.lemi.mario.widget.helper.fragment.base.BaseFragment;
import com.lemi.mario.widget.helper.manager.SDCardManager;
import com.lemi.mario.widget.helper.manager.SDCardManager.MountProcessListener;
import com.lemi.mario.widget.helper.manager.SDCardManager.MountProcessTask;
import com.lemi.mario.widget.helper.manager.SDCardManager.ProcessError;
import com.lemi.mario.widget.helper.utils.LogHelper;
import com.lemi.mario.widget.helper.utils.SurpriseUtils;
import com.lemi.mario.widget.helper.view.ActiveImageView;
import com.lemi.mario.widget.helper.view.ActiveImageView.Action;
import com.lemi.mario.widget.helper.view.BottomTipsView;
import com.lemi.mario.widget.helper.view.LoadingView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MountFragment extends BaseFragment {

  private static final String START_MOUNT = "START_MOUNT";

  private RelativeLayout mountContainer;
  private FrameLayout firstStartContainer;

  private ImageView firstStartButton;

  private ActiveImageView startButton;

  private ActiveImageView checkDot;
  private ActiveImageView checkCard;

  private ActiveImageView copyDot;
  private ActiveImageView copyCard;
  private ActiveImageView copyLine;

  private ActiveImageView restartDot;
  private ActiveImageView restartLine;
  private ActiveImageView restartCard;

  private ActiveImageView isInsertCheckMark;
  private ActiveImageView isFatCheckMark;
  private ActiveImageView isWritableCheckMark;

  private ActiveImageView isCompleteCheckMark;

  private BottomTipsView bottomTipsView;

  private LoadingView loadingView;
  private boolean loadingFlag = false;

  private ImageView back;

  private ExternalStorageInfo mountCard;

  private SurpriseUtils surpriseUtils;

  private MountProcessListener mountProcessListener = new MountProcessListener() {
    @Override
    public void receiveMountBroadcast(Intent intent) {
      refreshCheckMarkStatus();
    }

    @Override
    public void processTaskChange(MountProcessTask task) {
      handleTaskChange(task);
      changeProcessTask(task);
    }

    @Override
    public void processTaskFail(MountProcessTask task, ProcessError error) {
      handleError(task, error);
    }
  };

  @Override
  protected void onInflated(View contentView, Bundle savedInstanceState) {
    initView();
    initStatus();
    initSurprise();
    setListener();
    initFocus();
    refreshCheckMarkStatus();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.mount_fragment;
  }

  private void initSurprise() {
    surpriseUtils = new SurpriseUtils();
  }

  private void startFirstMount() {
    firstStartContainer.setVisibility(View.GONE);
    mountContainer.setVisibility(View.VISIBLE);
    WidgetHelpPreferences.setFirstMount();
    startButton.requestFocus();
    startMount();
  }

  private void startMount() {
    bottomTipsView.hideTips();
    changeLoadingViewStatus(true);
    SDCardManager.getInstance().startMount();
    LogHelper.mountErrorReport(START_MOUNT);
  }

  private void showDialog() {
    final WidgetHelperAlertDialog.Builder builder =
        new WidgetHelperAlertDialog.Builder(getActivity());
    builder.setTitle(R.string.mount_dialog_title);
    builder.setMessage(R.string.mount_dialog_message);
    builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        startMount();
        startButton.setAction(Action.SLEEP);
      }
    });
    builder.setNegativeButton(R.string.dialog_cancel, null);
    builder.create().show();
  }


  private void initView() {
    mountContainer = (RelativeLayout) contentView.findViewById(R.id.mount_container);
    firstStartContainer = (FrameLayout) contentView.findViewById(R.id.mount_first_start_container);
    firstStartButton = (ImageView) contentView.findViewById(R.id.mount_first_start_button);
    startButton = (ActiveImageView) contentView.findViewById(R.id.mount_start_button);
    checkDot = (ActiveImageView) contentView.findViewById(R.id.mount_check_dot);
    checkCard = (ActiveImageView) contentView.findViewById(R.id.mount_check_card);
    copyDot = (ActiveImageView) contentView.findViewById(R.id.mount_copy_dot);
    copyCard = (ActiveImageView) contentView.findViewById(R.id.mount_copy_card);
    copyLine = (ActiveImageView) contentView.findViewById(R.id.mount_copy_line);
    restartDot = (ActiveImageView) contentView.findViewById(R.id.mount_restart_dot);
    restartCard = (ActiveImageView) contentView.findViewById(R.id.mount_restart_card);
    restartLine = (ActiveImageView) contentView.findViewById(R.id.mount_restart_line);
    isInsertCheckMark = (ActiveImageView) contentView.findViewById(R.id.mount_status_is_insert);
    isFatCheckMark = (ActiveImageView) contentView.findViewById(R.id.mount_status_is_fat32);
    isWritableCheckMark = (ActiveImageView) contentView.findViewById(R.id.mount_status_is_writable);
    isCompleteCheckMark =
        (ActiveImageView) contentView.findViewById(R.id.mount_status_is_mount_complete);
    bottomTipsView = (BottomTipsView) contentView.findViewById(R.id.mount_tips);
    loadingView = (LoadingView) contentView.findViewById(R.id.mount_loading);
    back = (ImageView) contentView.findViewById(R.id.back_button);
  }

  private void initStatus() {
    if (WidgetHelpPreferences.isFirstMount() && !SDCardManager.getInstance().isMountSuccess()) {
      firstStartContainer.setVisibility(View.VISIBLE);
      mountContainer.setVisibility(View.INVISIBLE);
    } else {
      firstStartContainer.setVisibility(View.GONE);
      mountContainer.setVisibility(View.VISIBLE);
    }
    bottomTipsView.hideTips();
    MountProcessTask processTask = SDCardManager.getInstance().getMountProcessTask();
    handleTaskChange(processTask);
    changeProcessTask(processTask);
    changeLoadingViewStatus(false);
  }

  private void setListener() {
    SDCardManager.getInstance().setMountProcessListener(mountProcessListener);
    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (startButton.getAction() == Action.ACTIVE) {
          if (SDCardManager.getInstance().isMountSuccess()) {
            setAlreadyMounted();
            return;
          }
          showDialog();
        } else {
          if (SDCardManager.getInstance().getMountProcessTask() == MountProcessTask.RESTART) {
            bottomTipsView.showTips(StringUtil.getString(R.string.mount_tips_re_insert));
          } else {
            MainThreadPostUtils.toast(R.string.mount_tips_process_duplicate);
          }

        }
      }
    });
    back.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (loadingFlag) {
          MainThreadPostUtils.toast(R.string.mount_tips_process_not_complete);
          return;
        }
        if (isAdded()) {
          getActivity().finish();
        }
      }
    });
    firstStartButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startFirstMount();
      }
    });
  }

  private void initFocus() {
    if (WidgetHelpPreferences.isFirstMount()) {
      firstStartButton.requestFocus();
    } else {
      startButton.requestFocus();
    }
  }

  private void changeProcessTask(MountProcessTask mountProcessTask) {
    changeProgressBar(mountProcessTask);
    switch (mountProcessTask) {
      case IDLE:
        startButton.setAction(Action.ACTIVE);
        checkCard.setAction(Action.SLEEP);
        copyCard.setAction(Action.SLEEP);
        restartCard.setAction(Action.SLEEP);
        break;
      case CHECK:
        startButton.setAction(Action.SLEEP);
        checkCard.setAction(Action.SLEEP);
        copyCard.setAction(Action.SLEEP);
        restartCard.setAction(Action.SLEEP);
        break;
      case COPY:
        startButton.setAction(Action.SLEEP);
        checkCard.setAction(Action.ACTIVE);
        copyCard.setAction(Action.SLEEP);
        restartCard.setAction(Action.SLEEP);
        break;
      case RESTART:
        startButton.setAction(Action.SLEEP);
        checkCard.setAction(Action.ACTIVE);
        copyCard.setAction(Action.ACTIVE);
        restartCard.setAction(Action.SLEEP);
        break;
      case COMPLETE:
        startButton.setAction(Action.ACTIVE);
        checkCard.setAction(Action.ACTIVE);
        copyCard.setAction(Action.ACTIVE);
        restartCard.setAction(Action.ACTIVE);
        break;
      default:
        break;
    }
  }

  private void changeProgressBar(MountProcessTask mountProcessTask) {
    switch (mountProcessTask) {
      case IDLE:
      case CHECK:
        checkDot.setAction(Action.SLEEP);
        copyDot.setAction(Action.SLEEP);
        copyLine.setAction(Action.SLEEP);
        restartDot.setAction(Action.SLEEP);
        restartLine.setAction(Action.SLEEP);
        break;
      case COPY:
        checkDot.setAction(Action.ACTIVE);
        copyDot.setAction(Action.SLEEP);
        copyLine.setAction(Action.SLEEP);
        restartDot.setAction(Action.SLEEP);
        restartLine.setAction(Action.SLEEP);
        break;
      case RESTART:
        checkDot.setAction(Action.ACTIVE);
        copyDot.setAction(Action.ACTIVE);
        copyLine.setAction(Action.ACTIVE);
        restartDot.setAction(Action.SLEEP);
        restartLine.setAction(Action.SLEEP);
        break;
      case COMPLETE:
        checkDot.setAction(Action.ACTIVE);
        copyDot.setAction(Action.ACTIVE);
        copyLine.setAction(Action.ACTIVE);
        restartDot.setAction(Action.ACTIVE);
        restartLine.setAction(Action.ACTIVE);
        break;
      default:
        break;
    }
  }

  private void refreshCheckMarkStatus() {
    if (SDCardManager.getInstance().isMountSuccess()) {
      isCompleteCheckMark.setAction(Action.ACTIVE);
      // if mount complete SDCard is mnt/sdcard so we can not detect by sdcard utils
      isInsertCheckMark.setAction(Action.ACTIVE);
      isFatCheckMark.setAction(Action.ACTIVE);
      isWritableCheckMark.setAction(Action.ACTIVE);
      return;
    } else {
      isCompleteCheckMark.setAction(Action.SLEEP);
    }

    mountCard = SDCardManager.getInstance().getMountCard();
    if (mountCard != null) {
      isInsertCheckMark.setAction(Action.ACTIVE);
    } else {
      isInsertCheckMark.setAction(Action.SLEEP);
    }
    if (SDCardManager.getInstance().isMountCardFat32(mountCard)) {
      isFatCheckMark.setAction(Action.ACTIVE);
    } else {
      isFatCheckMark.setAction(Action.SLEEP);
    }
    if (SDCardManager.getInstance().isMountCardWritable(mountCard)) {
      isWritableCheckMark.setAction(Action.ACTIVE);
    } else {
      isWritableCheckMark.setAction(Action.SLEEP);
    }
  }

  private void handleTaskChange(MountProcessTask task) {
    if (task == MountProcessTask.COMPLETE) {
      refreshCheckMarkStatus();
      bottomTipsView.hideTips();
      changeLoadingViewStatus(false);
      LogHelper.mountErrorReport(task.toString());
    } else if (task == MountProcessTask.RESTART) {
      bottomTipsView.showExpandTips(StringUtil.getString(R.string.mount_tips_re_insert));
      SDCardManager.getInstance().waitForRestart();
    } else if (task == MountProcessTask.COPY) {
      bottomTipsView.showTips(StringUtil.getString(R.string.mount_tips_copying));
    }
  }

  private void handleError(MountProcessTask task, ProcessError error) {
    switch (error) {
      case NOT_SUPPORT:
        changeProcessTask(MountProcessTask.IDLE);
        bottomTipsView.showErrorTips(StringUtil.getString(R.string.mount_tips_not_support));
        break;
      case NO_EXTERNAL_STORAGE:
        changeProcessTask(MountProcessTask.IDLE);
        bottomTipsView.showErrorTips(StringUtil.getString(R.string.mount_tips_not_insert));
        changeLoadingViewStatus(false);
        break;
      case NOT_FAT32:
        changeProcessTask(MountProcessTask.IDLE);
        bottomTipsView.showErrorTips(StringUtil.getString(R.string.mount_tips_not_fat32));
        changeLoadingViewStatus(false);
        break;
      case CANNOT_WRITE:
        changeProcessTask(MountProcessTask.IDLE);
        bottomTipsView.showErrorTips(StringUtil.getString(R.string.mount_tips_not_writable));
        changeLoadingViewStatus(false);
        break;
      case NO_SPACE:
        changeProcessTask(MountProcessTask.IDLE);
        bottomTipsView.showErrorTips(StringUtil.getString(R.string.mount_tips_not_enough_space));
        changeLoadingViewStatus(false);
        break;
      case DATA_COPY_FAILED:
        changeProcessTask(MountProcessTask.IDLE);
        bottomTipsView.showErrorTips(StringUtil.getString(R.string.mount_tips_copy_failed));
        changeLoadingViewStatus(false);
        break;
      case DUPLICATE:
        MainThreadPostUtils.toast(R.string.mount_tips_process_duplicate);
        break;
      case ALREADY_COMPLETE:
        setAlreadyMounted();
        break;
      default:
        break;
    }
    LogHelper.mountErrorReport(error.toString());
  }

  private void changeLoadingViewStatus(boolean isLoading) {
    if (isLoading) {
      loadingView.show();
      loadingFlag = true;
    } else {
      loadingView.hide();
      loadingFlag = false;
    }

  }

  private void setAlreadyMounted() {
    changeProcessTask(MountProcessTask.COMPLETE);
    MainThreadPostUtils.toast(R.string.mount_tips_process_already_complete);
    changeLoadingViewStatus(false);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (loadingFlag && keyCode == KeyEvent.KEYCODE_BACK) {
      MainThreadPostUtils.toast(R.string.mount_tips_process_not_complete);
      return true;
    }
    if (surpriseUtils.onKeyDown(keyCode, event)) {
      SDCardManager.getInstance().startUnMount();
      changeProcessTask(MountProcessTask.IDLE);
      bottomTipsView.showTips(StringUtil.getString(R.string.mount_tips_start_surprise));
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
