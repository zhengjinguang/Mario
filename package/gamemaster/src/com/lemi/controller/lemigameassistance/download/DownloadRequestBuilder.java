package com.lemi.controller.lemigameassistance.download;

import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.controller.lemigameassistance.manager.UpgradeManager;
import com.lemi.controller.lemigameassistance.model.CheckVersionModel;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.utils.GameDownloadUtils;
import com.lemi.controller.lemigameassistance.utils.PathUtils;
import com.lemi.controller.lemigameassistance.utils.SuffixUtils;
import com.lemi.mario.base.utils.SizeConvertUtil;

/**
 * build DownloadRequest from some custom model
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadRequestBuilder {

  public static DownloadRequest buildDownloadRequest(GameModel gameModel) {
    if (gameModel == null || TextUtils.isEmpty(gameModel.getPackageName())
        || TextUtils.isEmpty(gameModel.getApkUrl())) {
      return null;
    }
    float apkSize;
    float dateSize;
    if (TextUtils.isEmpty(gameModel.getApkSize())) {
      apkSize = 0;
    } else {
      apkSize = Float.parseFloat(gameModel.getApkSize());
    }
    if (TextUtils.isEmpty(gameModel.getRealSize())) {
      dateSize = 0;
    } else {
      dateSize = Float.parseFloat(gameModel.getRealSize());
    }
    DownloadInfo.ContentType contentType =
        SuffixUtils.getContentTypeBySuffix(gameModel.getApkUrl());

    DownloadRequest.Builder builder = DownloadRequest.newBuilder();
    builder.setIdentity(gameModel.getPackageName());
    builder.setAllowInMobile(true);
    builder.setVisible(true);
    builder.setUrl(gameModel.getApkUrl());
    builder.setContentType(contentType);
    builder.setIconUrl(gameModel.getIconUrl());

    /**
     * download name protocol in order to show install name after install
     */
    builder.setTitle(gameModel.getName());

    String folderPath = GameDownloadUtils.judgeGameDownloadPath(
        SizeConvertUtil.transMB2Byte(apkSize),
        SizeConvertUtil.transMB2Byte(dateSize),
        contentType);

    if (TextUtils.isEmpty(folderPath)) {
      return null;
    }

    /**
     * download file folder name protocol
     */
    builder.setFolderPath(folderPath);

    /**
     * download file name protocol
     */
    if (contentType == DownloadInfo.ContentType.APP) {
      builder.setFileName(gameModel.getPackageName() + Constants.APK_SUFFIX);
    } else if (contentType == DownloadInfo.ContentType.ZIP) {
      builder.setFileName(gameModel.getPackageName() + Constants.ZIP_SUFFIX);
    }

    return builder.build();
  }


  public static DownloadRequest buildDownloadRequest(CheckVersionModel versionModel) {
    if (versionModel == null || TextUtils.isEmpty(versionModel.getApkUrl())) {
      return null;
    }

    DownloadRequest.Builder builder = DownloadRequest.newBuilder();
    builder.setIdentity(UpgradeManager.GAME_MASTER_UPGRADE_SELF_ID);
    builder.setAllowInMobile(true);
    /**
     * upgrade is invisible download
     */
    builder.setVisible(false);
    builder.setUrl(versionModel.getApkUrl());
    builder.setContentType(DownloadInfo.ContentType.UPGRADE);

    String folderPath = PathUtils.getDownloadFolderPath(DownloadInfo.ContentType.UPGRADE);

    if (TextUtils.isEmpty(folderPath)) {
      return null;
    }
    builder.setFolderPath(folderPath);
    builder.setFileName(UpgradeManager.GAME_MASTER_UPGRADE_SELF_ID + Constants.APK_SUFFIX);

    return builder.build();
  }

}
