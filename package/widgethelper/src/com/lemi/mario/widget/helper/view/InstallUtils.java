package com.lemi.mario.widget.helper.view;

import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.widget.helper.config.Constants;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InstallUtils {

  public static boolean canAppInstall(long appSize) {
    if (FileUtil.getAvailableBytes(Constants.ROOT_PATH) >= appSize) {
      return true;
    }
    return false;
  }

}
