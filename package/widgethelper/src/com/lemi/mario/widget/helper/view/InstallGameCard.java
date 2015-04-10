package com.lemi.mario.widget.helper.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;
import com.lemi.mario.widget.helper.R;
import com.lemi.mario.widget.helper.activity.MountActivity;
import com.lemi.mario.widget.helper.manager.InstallManager;
import com.lemi.mario.widget.helper.manager.SDCardManager;
import com.lemi.mario.widget.helper.model.GameModel;

import java.util.Set;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InstallGameCard extends FrameLayout {

  private AsyncImageView gamePoster;
  private ImageView gameShadow;
  private LoadingView loadingView;

  private Action action;

  private GameModel model;
  private String packageName;

  /**
   * Action of button.
   */
  public static enum Action {
    IDLE {
      @Override
      protected void onClick(final InstallGameCard card) {
        install(card);
      }
    },
    INSTALLING {
      @Override
      protected void onClick(InstallGameCard card) {
        MainThreadPostUtils.toast(R.string.install_processing);
      }
    },
    INSTALLED {
      @Override
      protected void onClick(InstallGameCard card) {

      }
    };

    private void apply(InstallGameCard card) {
      card.refreshCardStatus();
    }

    protected abstract void onClick(InstallGameCard card);
  }

  public InstallGameCard(Context context) {
    super(context);
    InstallManager.getInstance().addListener(installListener);
  }

  public InstallGameCard(Context context, AttributeSet attrs) {
    super(context, attrs);
    InstallManager.getInstance().addListener(installListener);
  }

  public InstallGameCard(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    InstallManager.getInstance().addListener(installListener);
  }

  public static InstallGameCard newInstance(ViewGroup parent) {
    return (InstallGameCard) ViewUtils.newInstance(parent, R.layout.install_game_card);
  }

  public static InstallGameCard newInstance(Context context) {
    return (InstallGameCard) ViewUtils.newInstance(context, R.layout.install_game_card);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    gamePoster = (AsyncImageView) findViewById(R.id.install_game_poster);
    gameShadow = (ImageView) findViewById(R.id.install_game_shadow);
    loadingView = (LoadingView) findViewById(R.id.install_game_loading);

    gamePoster.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (action != null && model != null) {
          action.onClick(InstallGameCard.this);
        }
      }
    });
  }

  public void bind(GameModel model) {
    reset();
    if (TextUtils.isEmpty(model.getPackageName())) {
      setVisibility(INVISIBLE);
    }
    this.model = model;
    this.packageName = model.getPackageName();
    initCardStatus(model);
  }


  private void reset() {
    action = null;
    model = null;
    packageName = null;
  }

  public void setAction(Action action) {
    if (this.action != action) {
      this.action = action;
      action.apply(this);
    }
  }

  public Action getAction() {
    return action;
  }


  private void initCardStatus(GameModel model) {
    gamePoster.loadLocalImage(model.getPosterPath(), R.drawable.game_default_pic);
    if (InstallManager.getInstance().isInstalled(model.getPackageName())) {
      setAction(Action.INSTALLED);
      return;
    }
    InstallManager.InstallStatus installStatus =
        InstallManager.getInstance().getStatus(packageName);
    if (installStatus != null) {
      updateInstallStat(installStatus);
      return;
    }
    setAction(Action.IDLE);
  }


  private InstallManager.InstallListener installListener = new InstallManager.InstallListener() {
    @Override
    public void onInstallStatusChange(String packageName, InstallManager.InstallStatus status) {
      if (!checkPackageNameValid(packageName)) {
        return;
      }
      updateInstallStat(status);
    }
  };

  private void refreshCardStatus() {
    switch (action) {
      case IDLE:
        gameShadow.setVisibility(VISIBLE);
        loadingView.hide();
        break;

      case INSTALLING:
        gameShadow.setVisibility(VISIBLE);
        loadingView.show();
        break;

      case INSTALLED:
        gameShadow.setVisibility(GONE);
        loadingView.hide();
      default:
        break;
    }
  }


  private void updateInstallStat(InstallManager.InstallStatus installStatus) {
    switch (installStatus) {
      case CREATE:
        setAction(Action.INSTALLING);
        break;
      case SILENCE_INSTALLING:
        setAction(Action.INSTALLING);
        break;
      case NORMAL_INSTALLING:
        setAction(Action.IDLE);
        break;
      case FAILED:
        MainThreadPostUtils.toast(R.string.install_failed);
        setAction(Action.IDLE);
        break;
      case SUCCESS:
        setAction(Action.INSTALLED);
        break;
      default:
        break;
    }
  }

  private boolean checkPackageNameValid(String packageName) {
    if (TextUtils.isEmpty(InstallGameCard.this.packageName)) {
      return false;
    }
    return InstallGameCard.this.packageName.equals(packageName);
  }


  private static void install(InstallGameCard card) {
    if (InstallManager.getInstance().isInstalled(card.packageName)) {
      card.setAction(Action.INSTALLED);
      return;
    }
    if(!checkMountStatusValid(card)){
      return;
    }
    Set<String> unFinishedToken = InstallManager.getInstance().getUnFinishedToken();
    if (unFinishedToken.size() > 0) {
      MainThreadPostUtils.toast(R.string.install_others_processing);
      return;
    }
    if (!InstallUtils.canAppInstall(FileUtil.getFileSize(card.model.getApkPath()))) {
      MainThreadPostUtils.toast(R.string.install_no_space);
      return;
    }
    if (!FileUtil.exists(card.model.getApkPath())) {
      MainThreadPostUtils.toast(R.string.install_apk_not_found);
      return;
    }
    InstallManager.getInstance().asyncInstallPackage(card.packageName, card.model.getApkPath());
  }


  private static boolean checkMountStatusValid(InstallGameCard card) {
    if (SDCardManager.getInstance().isMountSuccess()) {
      return true;
    }
    showDialog(card);
    return false;
  }

  private static void showDialog(final InstallGameCard card) {
    final AlertDialog.Builder builder =
            new AlertDialog.Builder(card.getContext());
    builder.setTitle(R.string.install_dialog_title);
    builder.setMessage(R.string.install_dialog_message);
    builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        jumpToMount(card);
      }
    });
    builder.create().show();
  }

  private static void jumpToMount(InstallGameCard card) {
    if (card.getContext() != null) {
      MountActivity.launch(card.getContext());
    }
  }

}
