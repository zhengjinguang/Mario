package com.lemi.controller.lemigameassistance.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.model.CategoryModel;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.base.utils.StringUtil;

import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameModelFormatUtils {

  private static final int GAME_PLAYMODE_SINGLE = 1;
  private static final int GAME_PLAYMODE_ONLINE = 2;
  private static final int GAME_PERSONMODE_SINGLE = 1;
  private static final int GAME_PERSONMODE_DOUBLE = 2;
  private static final int GAME_PERSONMODE_MANY = 3;
  private static final int GAME_SUPPORT_TYPE_JOYSTICK = 1;
  private static final int GAME_SUPPORT_TYPE_CONTROLPANEL = 2;
  private static final int GAME_SUPPORT_TYPE_MOUSE = 4;

  private static final String PLAY_MODE_SEPARATOR = " | ";


  public static String formatGamePlay(int playMode, int personMode,
      List<CategoryModel> categoryModelList) {
    StringBuilder builder = new StringBuilder();

    if (categoryModelList != null && categoryModelList.size() > 0) {
      for (CategoryModel categoryModel : categoryModelList) {
        if (!TextUtils.isEmpty(categoryModel.getName())) {
          builder.append(categoryModel.getName()).append(PLAY_MODE_SEPARATOR);
        }
      }
    }

    int playModeResId = R.string.game_playmode_single;
    if (playMode == GAME_PLAYMODE_ONLINE) {
      playModeResId = R.string.game_playmode_online;
    }
    builder.append(StringUtil.getString(playModeResId));
    if (personMode == GAME_PERSONMODE_DOUBLE) {
      builder.append(PLAY_MODE_SEPARATOR).append(
          StringUtil.getString(R.string.game_personmode_double));
    }
    else if (personMode == GAME_PERSONMODE_MANY) {
      builder.append(PLAY_MODE_SEPARATOR).append(
          StringUtil.getString(R.string.game_personmode_many));
    }
    return builder.toString();
  }

  public static void formatGameOperate(int operate, ImageView joyStickView,
      ImageView controlPanelView, ImageView mouseView) {
    if (operate < 0 || operate > 7) {
      operate = GAME_SUPPORT_TYPE_JOYSTICK;
    }

    if ((operate & GAME_SUPPORT_TYPE_JOYSTICK) != 0) {
      joyStickView.setVisibility(View.VISIBLE);
    } else {
      joyStickView.setVisibility(View.GONE);
    }
    if ((operate & GAME_SUPPORT_TYPE_CONTROLPANEL) != 0) {
      controlPanelView.setVisibility(View.VISIBLE);
    } else {
      controlPanelView.setVisibility(View.GONE);
    }

    if ((operate & GAME_SUPPORT_TYPE_MOUSE) != 0) {
      mouseView.setVisibility(View.VISIBLE);
    } else {
      mouseView.setVisibility(View.GONE);
    }
  }


  public static void isInstall(String packageName, View statView) {
    if (AppManager.getInstance().syncIsAppInstalled(packageName)) {
      statView.setVisibility(View.VISIBLE);
    } else {
      statView.setVisibility(View.GONE);
    }
  }

}
