package com.lemi.mario.widget.helper.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lemi.mario.widget.helper.R;

import java.lang.ref.WeakReference;

/**
 * A style AlertDialog which has same interface with android alertdialog
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */

// TODO use MVC to rewrite this
public class WidgetHelperAlertDialog extends Dialog implements DialogInterface {

  private static final int THEME = R.style.WidgetHelperAlertDialog;

  private Context context;
  private Window mWindow;
  private TextView alrtTitleTextView;
  private TextView messageTextView;
  private LinearLayout buttonBarStyleLayout;
  private Button mButtonPositive;
  private Button mButtonNegative;
  private Message mButtonPositiveMessage;
  private Message mButtonNegativeMessage;
  private boolean hasPositiveButton = false;
  private boolean hasNegativeButton = false;
  private CharSequence mButtonPositiveText;
  private CharSequence mButtonNegativeText;
  private int mPixel;
  private int mPercent;
  private Handler mHandler;
  private CharSequence mMessage;
  private CharSequence mTitle;
  private View.OnClickListener mButtonHandler = new View.OnClickListener() {
    public void onClick(View v) {
      Message m = null;
      if (v == mButtonPositive && mButtonPositiveMessage != null) {
        m = Message.obtain(mButtonPositiveMessage);
      } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
        m = Message.obtain(mButtonNegativeMessage);
      }
      if (m != null) {
        m.sendToTarget();
      }

      dismissIfNeed();
    }
  };

  protected void dismissIfNeed() {
    // Post a message so we dismiss after the above handlers are executed
    mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, WidgetHelperAlertDialog.this)
        .sendToTarget();
  }

  // be careful,the function will return the positive button
  public Button getPositiveButton() {
    return mButtonPositive;
  }

  protected WidgetHelperAlertDialog(Context context) {
    super(context, THEME);
    this.context = context;
    setContentView(getLayoutId());
    mHandler = new ButtonHandler(this);
    mWindow = this.getWindow();
  }

  protected static Display getScreenSize(Context context) {
    return ((Activity) context).getWindow().getWindowManager().getDefaultDisplay();
  }

  protected static int getPercentWidth(Context context, int percent) {
    return getScreenSize(context).getWidth() > getScreenSize(context).getHeight()
        ? (int) ((getScreenSize(context).getHeight() * percent) / 100)
        : (int) ((getScreenSize(context).getWidth() * percent) / 100);
  }

  protected static int getAdjustedDialogWidth(Context context) {
    return getPercentWidth(context, 90);
  }

  protected int getLayoutId() {
    return R.layout.widget_alert_dialog;
  }

  protected void apply() {
    WindowManager.LayoutParams paramsWindow = new WindowManager.LayoutParams();
    paramsWindow.copyFrom(mWindow.getAttributes());
    // FrameLayout.LayoutParams paramsFrameLayout =
    // new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
    // FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    if (mPixel != 0) {
      paramsWindow.width = mPixel;
      // paramsFrameLayout.width = mPixel;
    } else if (mPercent != 0) {
      paramsWindow.width = getPercentWidth(context, mPercent);
      // paramsFrameLayout.width = getPercentWidth(context, mPercent);
    } else {
      paramsWindow.width = getAdjustedDialogWidth(context);
      // paramsFrameLayout.width = getAdjustedDialogWidth(context);
    }
    mWindow.setAttributes(paramsWindow);
    // parentPanelLayout.setLayoutParams(paramsFrameLayout);
    setupContent();
    boolean hasButtons = setupButtons();
    alrtTitleTextView = (TextView) findViewById(R.id.alert_dialog_title);
    setupTitle(alrtTitleTextView);
    buttonBarStyleLayout = (LinearLayout) findViewById(R.id.alert_dialog_buttonPanel);
    if (!hasButtons) {
      buttonBarStyleLayout.setVisibility(View.GONE);
    }
  }

  protected void setupContent() {
    messageTextView = (TextView) findViewById(R.id.alert_dialog_message);
    if (messageTextView == null) {
      return;
    }
    if (mMessage != null) {
      messageTextView.setText(mMessage);
    } else {
      messageTextView.setVisibility(View.GONE);
    }
  }

  protected void setupTitle(TextView titleTextView) {
    final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
    if (hasTextTitle) {
      titleTextView = (TextView) findViewById(R.id.alert_dialog_title);
      titleTextView.setText(mTitle);
    } else {
      titleTextView = (TextView) findViewById(R.id.alert_dialog_title);
      titleTextView.setVisibility(View.GONE);
    }
  }

  public void setPhoenixTitle(CharSequence title) {
    if (title != null) {
      mTitle = title;
    }
  }

  public void setMessage(CharSequence message) {
    if (message != null) {
      mMessage = message;
    }
  }

  public void setDialogWidthPercent(int percent) {
    mPercent = percent;
  }

  public void setDialogWidthPixel(int pixel) {
    mPixel = pixel;
  }

  public void setButton(int whichButton, CharSequence text,
      DialogInterface.OnClickListener listener, Message msg) {

    if (msg == null && listener != null) {
      msg = mHandler.obtainMessage(whichButton, listener);
    }

    switch (whichButton) {

      case DialogInterface.BUTTON_POSITIVE:
        mButtonPositiveText = text;
        mButtonPositiveMessage = msg;
        hasPositiveButton = true;
        break;

      case DialogInterface.BUTTON_NEGATIVE:
        mButtonNegativeText = text;
        mButtonNegativeMessage = msg;
        hasNegativeButton = true;
        break;

      default:
        throw new IllegalArgumentException("Button does not exist");
    }
  }

  protected boolean setupButtons() {
    int BIT_BUTTON_POSITIVE = 1;
    int BIT_BUTTON_NEGATIVE = 2;
    int BIT_BUTTON_NEUTRAL = 4;
    int whichButtons = 0;
    mButtonPositive = (Button) findViewById(R.id.alert_dialog_positiveButton);
    mButtonPositive.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mButtonHandler.onClick(v);
      }
    });

    if (TextUtils.isEmpty(mButtonPositiveText)) {
      mButtonPositive.setVisibility(View.GONE);
    } else {
      mButtonPositive.setText(mButtonPositiveText);
      mButtonPositive.setVisibility(View.VISIBLE);
      whichButtons = whichButtons | BIT_BUTTON_POSITIVE;

    }
    mButtonNegative = (Button) findViewById(R.id.alert_dialog_negativeButton);
    mButtonNegative.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mButtonHandler.onClick(v);
      }
    });

    if (TextUtils.isEmpty(mButtonNegativeText)) {
      mButtonNegative.setVisibility(View.GONE);
    } else {
      mButtonNegative.setText(mButtonNegativeText);
      mButtonNegative.setVisibility(View.VISIBLE);
      whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;

    }


    if (shouldCenterSingleButton()) {
      /*
       * If we only have 1 button it should be centered on the layout and
       * expand to fill 50% of the available space.
       */
      if (whichButtons == BIT_BUTTON_POSITIVE) {
        centerButton(mButtonPositive);
      } else if (whichButtons == BIT_BUTTON_NEGATIVE) {
        centerButton(mButtonNegative);
      }
    }
    return whichButtons != 0;
  }

  protected boolean shouldCenterSingleButton() {
    int temp = 0;
    if (hasPositiveButton) {
      temp++;
    }
    if (hasNegativeButton) {
      temp++;
    }
    if (temp == 1) {
      return true;
    }
    return false;
  }

  protected void centerButton(Button button) {
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
    params.gravity = Gravity.CENTER_HORIZONTAL;
    params.leftMargin = 0;
    params.rightMargin = 0;
    button.setLayoutParams(params);
  }

  @Override
  public void show() {
    apply();
    super.show();
  }

  protected static class AlertParams {

    public int titleId;
    public CharSequence title;
    public int messageId;
    public CharSequence message;
    public int positiveButtonTextId;
    public CharSequence positiveButtonText;
    public OnClickListener positiveButtonOnClickListener;
    public int negativeButtonTextId;
    public CharSequence negativeButtonText;
    public OnClickListener negativeButtonOnClickListener;
    public boolean cancelable = true;
    public OnCancelListener onCancelListener;
    public OnDismissListener onDismissListener;
    public OnKeyListener onKeyListener;
    public int widthInPercent;
    public int widthInPixel;
  }

  public static class Builder {

    protected Context context;
    private AlertParams alertParams;

    public Builder(Context context) {
      this.context = context;
      alertParams = new AlertParams();
    }

    public Context getContext() {
      return context;
    }

    public Builder setTitle(int titleId) {
      alertParams.titleId = titleId;
      return this;
    }

    public Builder setTitle(CharSequence title) {
      alertParams.title = title;
      return this;
    }

    public Builder setMessage(int messageId) {
      alertParams.messageId = messageId;
      return this;
    }

    public Builder setMessage(CharSequence message) {
      alertParams.message = message;
      return this;
    }


    public Builder setPositiveButton(int textId, final OnClickListener listener) {
      alertParams.positiveButtonTextId = textId;
      alertParams.positiveButtonOnClickListener = listener;
      return this;
    }

    public Builder setPositiveButton(CharSequence text, final OnClickListener listener) {
      alertParams.positiveButtonText = text;
      alertParams.positiveButtonOnClickListener = listener;
      return this;
    }

    public Builder setNegativeButton(int textId, final OnClickListener listener) {
      alertParams.negativeButtonTextId = textId;
      alertParams.negativeButtonOnClickListener = listener;
      return this;
    }

    public Builder setNegativeButton(CharSequence text, final OnClickListener listener) {
      alertParams.negativeButtonText = text;
      alertParams.negativeButtonOnClickListener = listener;
      return this;
    }

    public Builder setCancelable(boolean cancelable) {
      alertParams.cancelable = cancelable;
      return this;
    }

    public Builder setOnCancelListener(OnCancelListener onCancelListener) {
      alertParams.onCancelListener = onCancelListener;
      return this;
    }

    public Builder setOnDismissListener(OnDismissListener onDismissListener) {
      alertParams.onDismissListener = onDismissListener;
      return this;
    }

    public Builder setOnKeyListener(OnKeyListener onKeyListener) {
      alertParams.onKeyListener = onKeyListener;
      return this;
    }


    public Builder setDialogWidthPercent(int percent) {
      alertParams.widthInPercent = percent;
      return this;
    }

    public Builder setDialogWidthPixel(int pixel) {
      alertParams.widthInPixel = pixel;
      return this;
    }

    protected WidgetHelperAlertDialog initDialog() {
      return new WidgetHelperAlertDialog(context);
    }

    public WidgetHelperAlertDialog create() {
      WidgetHelperAlertDialog dialog = initDialog();
      if (alertParams.titleId > 0) {
        dialog.setPhoenixTitle(context.getString(alertParams.titleId));
      } else if (alertParams.title != null) {
        dialog.setPhoenixTitle(alertParams.title);
      }

      if (alertParams.messageId > 0) {
        dialog.setMessage(context.getString(alertParams.messageId));
      } else if (alertParams.message != null) {
        dialog.setMessage(alertParams.message);
      }

      if (alertParams.positiveButtonTextId > 0) {
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,
            context.getString(alertParams.positiveButtonTextId),
            alertParams.positiveButtonOnClickListener, null);
      } else if (alertParams.positiveButtonText != null) {
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, alertParams.positiveButtonText,
            alertParams.positiveButtonOnClickListener, null);
      }

      if (alertParams.negativeButtonTextId > 0) {
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            context.getString(alertParams.negativeButtonTextId),
            alertParams.negativeButtonOnClickListener, null);
      } else if (alertParams.negativeButtonText != null) {
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, alertParams.negativeButtonText,
            alertParams.negativeButtonOnClickListener, null);
      }

      dialog.setCancelable(alertParams.cancelable);
      if (alertParams.onCancelListener != null) {
        dialog.setOnCancelListener(alertParams.onCancelListener);
      }
      if (alertParams.onDismissListener != null) {
        dialog.setOnDismissListener(alertParams.onDismissListener);
      }
      if (alertParams.onKeyListener != null) {
        dialog.setOnKeyListener(alertParams.onKeyListener);
      }


      if (alertParams.widthInPixel > 0) {
        dialog.setDialogWidthPixel(alertParams.widthInPixel);
      } else if (alertParams.widthInPercent > 0) {
        dialog.setDialogWidthPercent(alertParams.widthInPercent);
      }
      dialog.apply();
      return dialog;
    }

    public WidgetHelperAlertDialog show() {
      WidgetHelperAlertDialog dialog = this.create();
      if ((context instanceof Activity) && !((Activity) context).isFinishing()) {
        dialog.show();
      }
      return dialog;
    }
  }

  private static final class ButtonHandler extends Handler {
    // Button clicks have Message.what as the BUTTON{1,2,3} constant
    private static final int MSG_DISMISS_DIALOG = 1;
    private WeakReference<DialogInterface> mDialog;

    public ButtonHandler(DialogInterface dialog) {
      mDialog = new WeakReference<DialogInterface>(dialog);
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {

        case DialogInterface.BUTTON_POSITIVE:
        case DialogInterface.BUTTON_NEGATIVE:
        case DialogInterface.BUTTON_NEUTRAL:
          ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
          break;

        case MSG_DISMISS_DIALOG:
          ((DialogInterface) msg.obj).dismiss();
      }
    }
  }
}
