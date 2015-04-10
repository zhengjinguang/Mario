package com.lemi.controller.lemigameassistance.recycleview.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.model.GameModel;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemFocusChangeListener;
import com.lemi.controller.lemigameassistance.recycleview.tag.TagHelper;
import com.lemi.controller.lemigameassistance.utils.AppUtils;
import com.lemi.controller.lemigameassistance.utils.ViewUtils;
import com.lemi.mario.appmanager.AppManager;
import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author zhoulei@letv.com (Zhou Lei)
 */
public class UninstallItemView extends RelativeLayout {
  private String packageName;

  private AsyncImageView iconView;

  private TextView nameView;
  private TextView infoView;
  private Button uninstallButton;

  public UninstallItemView(Context context) {
    super(context);
  }

  public UninstallItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public UninstallItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public static UninstallItemView newInstance(ViewGroup parent) {
    return (UninstallItemView) ViewUtils.newInstance(parent,
        R.layout.uninstall_item_layout);
  }

  public static UninstallItemView newInstance(Context context) {
    return (UninstallItemView) ViewUtils.newInstance(context,
        R.layout.uninstall_item_layout);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initView();
  }

  public void bind(GameModel gameModel) {
    if (gameModel == null) {
      return;
    }
    packageName = gameModel.getPackageName();

    iconView.loadNetworkImage(gameModel.getIconUrl(), R.drawable.icon_defualt);
    nameView.setText(gameModel.getName());
    infoView.setText(getContext().getString(R.string.game_size, gameModel.getApkSize() + " MB"));

    initItemListener();
  }

  public String getPackageName() {
    return packageName;
  }

  /**
   * use this function only in remove or add view
   */
  public void onlyPositionChange() {
    initItemListener();
  }

  public void focusButton() {
    uninstallButton.requestFocus();
  }

  private void initView() {
    iconView = (AsyncImageView) findViewById(R.id.uninstall_icon);
    nameView = (TextView) findViewById(R.id.uninstall_name);
    infoView = (TextView) findViewById(R.id.uninstall_info);
    uninstallButton = (Button) findViewById(R.id.uninstall_manage_button);
  }

  private void initItemListener() {
    final OnItemFocusChangeListener onFocusChangeListener =
        TagHelper.getOnFocusChangeListener(this);

    final int position = TagHelper.getPosition(this);

    uninstallButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        uninstallPackage(packageName);
      }
    });

    uninstallButton.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (onFocusChangeListener != null) {
          onFocusChangeListener.onItemFocusChange(UninstallItemView.this, position, b);
        }
      }
    });
  }

  private void uninstallPackage(String packageName) {
    if (AppUtils.isSystemApp(packageName)) {
      MainThreadPostUtils.toastLong(R.string.uninstall_system_app);
      return;
    }
    AppManager.getInstance().uninstallApp(packageName);
  }

}
