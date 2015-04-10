package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.activity.GameDetailActivity;
import com.lemi.controller.lemigameassistance.focus.anim.ScaleAnimHelper;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.recycleview.model.MyGameModel;
import com.lemi.controller.lemigameassistance.utils.AppUtils;
import com.lemi.controller.lemigameassistance.utils.GameModelFormatUtils;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameItemView extends LinearLayout {

  private ScaleAnimHelper scaleAnimHelper;

  private RelativeLayout itemLayout;
  private RelativeLayout underLayout;
  private AsyncImageView iconView;
  private ImageView installStatView;
  private ImageView joystickView;
  private ImageView panelView;
  private ImageView mouseView;
  private TextView downloadCountView;
  private TextView gameNameView;

  private String packageName;
  private GameModel gameModel;

  private ItemClickListener itemClickListener = new ItemClickListener();

  public enum Type {
    LITTLE, LARGE
  }

  public GameItemView(Context context) {
    super(context);
  }

  public GameItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
    scaleAnimHelper = new ScaleAnimHelper(context, attrs);
  }

  public static GameItemView newInstance(ViewGroup parent, Type type) {
    GameItemView gameItemView;
    if (type == Type.LITTLE) {
      gameItemView = (GameItemView) ViewUtils.newInstance(parent, R.layout.game_little_item);
    } else {
      gameItemView = (GameItemView) ViewUtils.newInstance(parent, R.layout.game_large_item);
    }
    return gameItemView;

  }



  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    itemLayout = (RelativeLayout) findViewById(R.id.game_item_layout);
    underLayout = (RelativeLayout) findViewById(R.id.game_item_under_layout);
    iconView = (AsyncImageView) findViewById(R.id.game_item_icon);
    installStatView = (ImageView) findViewById(R.id.game_item_state);
    joystickView = (ImageView) findViewById(R.id.game_item_operation_joystick);
    panelView = (ImageView) findViewById(R.id.game_item_operation_control_panel);
    mouseView = (ImageView) findViewById(R.id.game_item_operation_mouse);
    downloadCountView = (TextView) findViewById(R.id.game_item_download_count);
    gameNameView = (TextView) findViewById(R.id.game_item_name);

    scaleAnimHelper.setFocusListener(itemLayout);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    scaleAnimHelper.onLayout(itemLayout);
  }

  public void bind(GameModel gameModel) {
    if (gameModel == null) {
      return;
    }
    this.gameModel = gameModel;
    packageName = gameModel.getPackageName();

    itemLayout.setOnClickListener(itemClickListener);
    underLayout.setVisibility(GONE);
    iconView.loadNetworkImage(gameModel.getIconUrl(), R.drawable.icon_defualt);
    GameModelFormatUtils.isInstall(gameModel.getPackageName(), installStatView);
    GameModelFormatUtils.formatGameOperate(gameModel.getOperationMode(),
        joystickView, panelView, mouseView);
    downloadCountView.setText(String.valueOf(gameModel.getDownloadCount()));
    gameNameView.setText(String.valueOf(gameModel.getName()));
  }



  private class ItemClickListener implements OnClickListener {

    @Override
    public void onClick(View v) {
      if (!TextUtils.isEmpty(packageName)) {
        if (gameModel instanceof MyGameModel) {
          openApp();
          return;
        }
        GameDetailActivity.launch(getContext(), packageName);
      }
    }
  }

  private void openApp() {
    if (!AppManager.getInstance().syncIsAppInstalled(packageName)) {
      MainThreadPostUtils.toast(R.string.net_button_app_is_uninstall);
      return;
    }
    AppUtils.startApp(getContext(), packageName);
  }
}
