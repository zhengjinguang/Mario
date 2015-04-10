package com.lemi.controller.lemigameassistance.recycleview.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.download.DownloadInfo;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemFocusChangeListener;
import com.lemi.controller.lemigameassistance.recycleview.listener.OnItemRemoveListener;
import com.lemi.controller.lemigameassistance.recycleview.tag.TagHelper;
import com.lemi.controller.lemigameassistance.utils.ApiConvertUtils;
import com.lemi.controller.lemigameassistance.utils.ViewUtils;
import com.lemi.controller.lemigameassistance.view.DownloadUnzipHorizontalBar;
import com.lemi.controller.lemigameassistance.view.DownloadUnzipTextView;
import com.lemi.controller.lemigameassistance.view.NetAppButton;
import com.lemi.controller.lemigameassistance.view.NetAppCancelButton;
import com.lemi.mario.base.utils.SizeConvertUtil;
import com.lemi.mario.image.view.AsyncImageView;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadingItemView extends RelativeLayout {

  private View contentView;
  private AsyncImageView iconView;
  private TextView nameView;
  private TextView infoView;
  private NetAppButton downloadButton;
  private NetAppCancelButton cancelButton;
  private DownloadUnzipHorizontalBar progressBar;
  private DownloadUnzipTextView progressText;

  private DownloadInfo downloadInfo;
  private String packageName;

  public enum FocusButton {
    NONE, DOWNLOAD, CANCEL
  }

  public DownloadingItemView(Context context) {
    super(context);
  }

  public DownloadingItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public DownloadingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public static DownloadingItemView newInstance(ViewGroup parent) {
    return (DownloadingItemView) ViewUtils.newInstance(parent,
        R.layout.downloading_item_layout);
  }

  public static DownloadingItemView newInstance(Context context) {
    return (DownloadingItemView) ViewUtils.newInstance(context,
        R.layout.downloading_item_layout);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initView();
  }

  public void bind(DownloadInfo info) {
    if (info == null) {
      return;
    }
    downloadInfo = info;
    packageName = info.getIdentity();

    iconView.loadNetworkImage(info.getIcon(), R.drawable.icon_defualt);
    nameView.setText(info.getTitle());
    infoView.setText(getContext().getString(R.string.game_size,
        SizeConvertUtil.transByte2MBAndFormat(downloadInfo.getTotalBytes()) + " MB"));
    downloadButton.setData(ApiConvertUtils.convertToGameModel(info));
    cancelButton.setData(ApiConvertUtils.convertToGameModel(info));
    progressBar.setData(info.getIdentity());
    progressText.setData(info.getIdentity());

    initItemListener();
  }

  /**
   * use this function only in remove or add view
   */
  public void onlyPositionChange() {
    initItemListener();
  }

  public FocusButton getFocusButton(View view) {
    if (downloadButton.equals(view)) {
      return FocusButton.DOWNLOAD;
    }
    if (cancelButton.equals(view)) {
      return FocusButton.CANCEL;
    }
    return FocusButton.NONE;
  }

  public FocusButton getFocusButton() {
    if (downloadButton.equals(findFocus())) {
      return FocusButton.DOWNLOAD;
    }
    if (cancelButton.equals(findFocus())) {
      return FocusButton.CANCEL;
    }
    return FocusButton.NONE;
  }

  public void focusButton(FocusButton focusButton) {
    if (focusButton == FocusButton.CANCEL) {
      cancelButton.requestFocus();
    } else {
      downloadButton.requestFocus();
    }
  }

  private void initView() {
    contentView = this;
    iconView = (AsyncImageView) findViewById(R.id.downloading_icon);
    nameView = (TextView) findViewById(R.id.downloading_name);
    infoView = (TextView) findViewById(R.id.downloading_info);
    downloadButton = (NetAppButton) findViewById(R.id.downloading_manage_download_button);
    progressBar = (DownloadUnzipHorizontalBar) findViewById(R.id.downloading_progressbar);
    progressText = (DownloadUnzipTextView) findViewById(R.id.downloading_progress);
    cancelButton = (NetAppCancelButton) findViewById(R.id.downloading_cancel_button);
  }


  private void initItemListener() {
    final int position = TagHelper.getPosition(contentView);
    final OnItemFocusChangeListener onItemFocusChangeListener =
        TagHelper.getOnFocusChangeListener(contentView);
    final OnItemRemoveListener onItemRemoveListener = TagHelper.getOnRemoveListener(contentView);

    downloadButton.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (onItemFocusChangeListener != null) {
          onItemFocusChangeListener.onItemFocusChange(v, position, hasFocus);
        }
      }
    });

    cancelButton.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (onItemFocusChangeListener != null) {
          onItemFocusChangeListener.onItemFocusChange(v, position, hasFocus);
        }
      }
    });

    cancelButton.setExtraRunnable(new Runnable() {
      @Override
      public void run() {
        if (onItemRemoveListener != null) {
          onItemRemoveListener.onItemRemove(contentView, position);
        }
      }
    });

  }

}
