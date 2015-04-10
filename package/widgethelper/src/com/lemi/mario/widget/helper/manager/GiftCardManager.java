package com.lemi.mario.widget.helper.manager;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.widget.helper.model.LocalGameModel;
import com.lemi.mario.widget.helper.view.InstallUtils;
import com.lemi.mario.widget.helper.view.PathUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GiftCardManager {

  private static GiftCardManager instance;
  private Gson gson;

  private List<LocalGameModel> installList;
  private LocalGameModel installingModel;
  private int installPosition = 0;

  private WeakReference<GiftInstallListener> giftInstallListener;

  private boolean cancelFlag = false;
  private boolean installingFlag = false;

  public enum InstallError {
    NO_INDEX_FILE
  }

  private GiftCardManager() {
    gson = new Gson();
    InstallManager.getInstance().addListener(installListener);
  }

  public static synchronized GiftCardManager getInstance() {
    if (instance == null) {
      instance = new GiftCardManager();
    }
    return instance;
  }

  public void setGiftInstallListener(GiftInstallListener listener) {
    if (listener != null) {
      giftInstallListener = new WeakReference<GiftInstallListener>(listener);
    }
  }

  public List<LocalGameModel> getGiftFromLocal() {
    String indexString = FileUtil.readFileFirstLine(PathUtils.INDEX_FILE_PATH);
    if (TextUtils.isEmpty(indexString)) {
      return null;
    }
    installList = gson.fromJson(indexString, new TypeToken<List<LocalGameModel>>() {}.getType());
    return installList;
  }

  public String getInstallingName() {
    if (installingModel != null) {
      return installingModel.getGameName();
    }
    return null;
  }

  public boolean isInstalling() {
    return installingFlag;
  }

  public boolean isInstalling(String packageName) {
    if (!TextUtils.isEmpty(packageName)
        && installingModel != null
        && packageName.equals(installingModel.getPackageName())) {
      return true;
    }
    return false;
  }

  public synchronized void startInstallAll() {
    // avoid duplicate install
    if (installingFlag) {
      return;
    }
    if (CollectionUtils.isEmpty(installList)) {
      notifyInstallError(InstallError.NO_INDEX_FILE);
      return;
    }
    installingFlag = true;
    installAtPosition(0);
  }

  public synchronized void cancel() {
    if (!installingFlag) {
      notifyCancel();
      return;
    }
    cancelFlag = true;
  }

  private void installAtPosition(int position) {
    if (checkInstallComplete(position)) {
      return;
    }
    LocalGameModel model = installList.get(position);
    if (model == null || TextUtils.isEmpty(model.getPackageName())
        || TextUtils.isEmpty(PathUtils.getApkPath(model.getPackageName()))) {
      return;
    }
    installPosition = position;
    installingModel = model;
    notifyItemInstall(position, model);
    if (InstallManager.getInstance().isInstalled(model.getPackageName())) {
      installNext();
      return;
    }
    installApk(model);
  }

  private void installNext() {
    if (checkCancelFlag()) {
      installingFlag = false;
      notifyCancel();
      return;
    }
    if (checkInstallComplete(installPosition + 1)) {
      return;
    }
    installAtPosition(installPosition + 1);
  }

  private boolean checkCancelFlag() {
    if (cancelFlag) {
      cancelFlag = false;
      return true;
    }
    return false;
  }

  private InstallManager.InstallListener installListener = new InstallManager.InstallListener() {
    @Override
    public void onInstallStatusChange(String packageName, InstallManager.InstallStatus status) {
      if (!checkPackageNameValid(packageName)) {
        return;
      }
      switch (status) {
        case CREATE:
        case SILENCE_INSTALLING:
        case NORMAL_INSTALLING:
          break;
        case FAILED:
          installNext();
          break;
        case SUCCESS:
          installNext();
          break;
        default:
          break;
      }
    }
  };

  private boolean checkInstallComplete(int position) {
    if (CollectionUtils.isEmpty(installList)
        || position >= installList.size()
        || installList.get(position) == null
        || TextUtils.isEmpty(installList.get(position).getPackageName())) {
      installingFlag = false;
      notifyComplete();
      return true;
    }
    return false;
  }

  private boolean checkPackageNameValid(String packageName) {
    if (TextUtils.isEmpty(packageName)) {
      return false;
    }
    if (CollectionUtils.isEmpty(installList) || installingModel == null) {
      return false;
    }
    return packageName.equals(installingModel.getPackageName());
  }

  private void notifyCancel() {
    if (giftInstallListener != null && giftInstallListener.get() != null) {
      giftInstallListener.get().onInstallAllCancel();
    }
  }

  private void notifyComplete() {
    if (giftInstallListener != null && giftInstallListener.get() != null) {
      giftInstallListener.get().onInstallAllComplete();
    }
  }

  private void notifyItemInstall(int position, LocalGameModel model) {
    if (giftInstallListener != null && giftInstallListener.get() != null) {
      giftInstallListener.get().onItemInstall(position, model);
    }
  }

  private void notifyInstallError(InstallError installError) {
    if (giftInstallListener != null && giftInstallListener.get() != null) {
      giftInstallListener.get().onInstallError(installError);
    }
  }

  private void installApk(LocalGameModel model) {
    long apkSize = FileUtil.getFileSize(PathUtils.getApkPath(model.getPackageName()));

    if (!InstallUtils.canAppInstall(apkSize)
        || !FileUtil.exists(PathUtils.getApkPath(model.getPackageName()))) {
      installNext();
      return;
    }

    InstallManager.getInstance().asyncInstallPackage(model.getPackageName(),
        PathUtils.getApkPath(model.getPackageName()));
  }

  public interface GiftInstallListener {
    void onInstallAllComplete();

    void onInstallAllCancel();

    void onItemInstall(int position, LocalGameModel model);

    void onInstallError(InstallError installError);
  }
}
