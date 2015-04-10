package com.lemi.controller.lemigameassistance.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.mario.base.utils.CollectionUtils;
import com.lemi.mario.base.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class GameMasterDialog extends Dialog {
  private Param param;
  private RelativeLayout dialog;
  private TextView title;
  private ProgressBar progressBar;
  private TextView message;
  private ListView messageLv;
  private TextView positive;
  private TextView negative;
  private LinearLayout dialog_bts_layout;
  private int height;
  View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
      TextView tv = (TextView) v;
      if (hasFocus) {
        tv.setBackgroundResource(R.drawable.dialog_button_focus);
        tv.setTextColor(Color.WHITE);
      }
      else {
        tv.setBackgroundResource(R.drawable.dialog_button);
        tv.setTextColor(Color.GRAY);
      }
    }

  };
  private View.OnClickListener clickListener = new View.OnClickListener() {

    @Override
    public void onClick(View v) {
      for (int i = 0; i < dialog_bts_layout.getChildCount(); i++) {
        if (v == dialog_bts_layout.getChildAt(i)) {
          if (i == 0 && param.positiveButtonListener != null) {
            param.positiveButtonListener.onClick(GameMasterDialog.this, BUTTON_POSITIVE);
          } else if (i == 1 && param.negativeButtonListener != null) {
            param.negativeButtonListener.onClick(GameMasterDialog.this, BUTTON_NEGATIVE);
          }
          break;
        }
      }
      GameMasterDialog.this.dismiss();
    }
  };

  private GameMasterDialog(Context context,
      Param param) {
    super(context, R.style.DialogStyle);
    this.param = param;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_gamemaster_dialog);
    dialog = (RelativeLayout) findViewById(R.id.gamemaster_dialog);
    title = (TextView) findViewById(R.id.dialog_title);
    progressBar = (ProgressBar) findViewById(R.id.dialog_progressbar);
    messageLv = (ListView) findViewById(R.id.dialog_list_message);
    message = (TextView) findViewById(R.id.dialog_message);
    dialog_bts_layout = (LinearLayout) findViewById(R.id.dialog_bts);
    initViews();
  }

  private void initViews() {
    dialog.setBackgroundResource(R.drawable.dialog_notitle_background);
    if (!TextUtils.isEmpty(param.title)) {
      dialog.setBackgroundResource(R.drawable.dialog_title_background);
    }
    if (param.style == R.style.dialog_big) {
      height = param.mContext.getResources().getDimensionPixelSize(R.dimen.mario_500dp);
      dialog.setBackgroundResource(R.drawable.dialog_big_background);
    } else if (param.style == R.style.dialog_progress) {
      height = param.mContext.getResources().getDimensionPixelSize(R.dimen.mario_120dp);
    } else if (param.style == R.style.dialog_list) {
      height = param.mContext.getResources().getDimensionPixelSize(R.dimen.mario_300dp);
    } else {
      height = param.mContext.getResources().getDimensionPixelSize(R.dimen.mario_240dp);
    }
    dialog.setLayoutParams(new FrameLayout.LayoutParams(param.mContext.getResources()
        .getDimensionPixelSize(R.dimen.mario_480dp), height));

    if (!TextUtils.isEmpty(param.title)) {
      title.setVisibility(View.VISIBLE);
      title.setText(param.title);
    }
    if (param.style == R.style.dialog_progress) {
      progressBar.setVisibility(View.VISIBLE);
    }
    if (!CollectionUtils.isEmpty(param.messages)) {
      if (param.messages.size() == 1) {
        message.setVisibility(View.VISIBLE);
        message.setText(param.messages.get(0));
      }
      else {
        messageLv.setVisibility(View.VISIBLE);
        messageLv.setAdapter(new ListAdapter());
      }
    }
    if (!TextUtils.isEmpty(param.positiveButtonText)) {
      dialog_bts_layout.setVisibility(View.VISIBLE);
      dialog_bts_layout.setOrientation(LinearLayout.HORIZONTAL);
      int width =
          param.mContext.getResources().getDimensionPixelSize(R.dimen.mario_171dp);
      int parentWidth = param.mContext.getResources().getDimensionPixelSize(R.dimen.mario_480dp);
      LinearLayout.LayoutParams lp =
          new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
      positive = new TextView(param.mContext);
      initTextView(positive);
      positive.setText(param.positiveButtonText);
      if (TextUtils.isEmpty(param.negativeButtonText)) {
        lp.setMargins((parentWidth - width) / 2, 0, 0, 0);
      }
      else {
        lp.setMargins((parentWidth - 2 * width) / 3, 0, 0, 0);
        negative = new TextView(param.mContext);
        initTextView(negative);
        negative.setText(param.negativeButtonText);
      }
      lp.gravity = Gravity.CENTER;
      positive.setLayoutParams(lp);
      dialog_bts_layout.addView(positive);
      if (negative != null) {
        negative.setLayoutParams(lp);
        dialog_bts_layout.addView(negative);
      }
      positive.requestFocus();
    }
  }

  private void initTextView(TextView tv) {
    if (tv == null) {
      return;
    }
    tv.setFocusable(true);
    tv.setClickable(true);
    tv.setTextColor(Color.GRAY);
    tv.setVisibility(View.VISIBLE);
    tv.setGravity(Gravity.CENTER);
    tv.setTextSize(26);
    tv.setBackgroundResource(R.drawable.dialog_button);
    tv.setOnFocusChangeListener(onFocusChangeListener);
    tv.setOnClickListener(clickListener);
  }

  public static class Builder {
    private final Param param = new Param();

    public Builder(Context context) {
      this(context, R.style.dialog_normal);
    }

    public Builder(Context context, int style) {
      param.mContext = context;
      param.style = style;
    }

    public Builder setTitle(int titleId) {
      param.title = StringUtil.getString(titleId);
      return this;
    }

    public Builder setTitle(String title) {
      param.title = title;
      return this;
    }

    public Builder setMessage(int messageId) {
      param.messages.add(StringUtil.getString(messageId));
      return this;
    }

    public Builder setMessage(String message) {
      param.messages.add(message);
      return this;
    }

    public Builder setMessages(List<String> messages) {
      param.messages.addAll(messages);
      return this;
    }

    public Builder setPositiveButton(int textId, OnClickListener listener) {
      param.positiveButtonText = StringUtil.getString(textId);
      param.positiveButtonListener = listener;
      return this;
    }

    public Builder setPositiveButton(String text, OnClickListener listener) {
      param.positiveButtonText = text;
      param.positiveButtonListener = listener;
      return this;
    }

    public Builder setNegativeButton(int textId, OnClickListener listener) {
      param.negativeButtonText = StringUtil.getString(textId);
      param.negativeButtonListener = listener;
      return this;
    }

    public Builder setNegativeButton(String text, OnClickListener listener) {
      param.negativeButtonText = text;
      param.negativeButtonListener = listener;
      return this;
    }

    public GameMasterDialog create() {
      final GameMasterDialog dialog = new GameMasterDialog(param.mContext, param);
      return dialog;
    }

    public void show() {
      GameMasterDialog dialog = new GameMasterDialog(param.mContext, param);
      dialog.show();
    }
  }

  public static class Param {
    Context mContext;
    String title;
    final List<String> messages = new ArrayList<>();
    String positiveButtonText;
    String negativeButtonText;
    OnClickListener positiveButtonListener;
    OnClickListener negativeButtonListener;
    int style = -1;
  }

  class ListAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return param.messages.size();
    }

    @Override
    public Object getItem(int position) {
      return param.messages.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView textView;
      if (convertView == null)
      {
        convertView = View.inflate(param.mContext, R.layout.dialog_listview_item, null);
        textView = (TextView) convertView.findViewById(R.id.dialog_list_item);
        convertView.setTag(textView);
      } else {
        textView = (TextView) convertView.getTag();
      }
      textView.setText(param.messages.get(position));

      return convertView;
    }
  }
}
