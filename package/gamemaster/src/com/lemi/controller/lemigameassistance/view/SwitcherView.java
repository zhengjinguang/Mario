package com.lemi.controller.lemigameassistance.view;

import static com.lemi.controller.lemigameassistance.manager.CleanManager.OnFinishCleanListener;
import static com.lemi.controller.lemigameassistance.manager.SDCardManager.SDCardMountListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.manager.CleanManager;
import com.lemi.controller.lemigameassistance.manager.SDCardManager;
import com.lemi.controller.lemigameassistance.utils.LogHelper;
import com.lemi.mario.base.utils.MainThreadPostUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SwitcherView extends TextView {

  protected static final int[] STATE_ON = {R.attr.state_switcher_on};
  protected static final int[] STATE_OFF = {R.attr.state_switcher_off};
  protected static final int[] STATE_CLEAN_ON = {R.attr.state_switcher_clean_on};
  protected static final int[] STATE_CLEAN_OFF = {R.attr.state_switcher_clean_off};

  {
    setFocusable(true);
    setFocusableInTouchMode(true);
    setEnabled(true);
    setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        SwitcherView.this.onClick();
      }
    });
  }

  /**
   * Action of swither.
   */
  public static enum Action {

    ON(STATE_ON, R.string.switcher_null) {
      @Override
      protected void onClick(SwitcherView switcher) {
        switch (switcher.switcherType) {
          case EXTERNAL_SD_CARD:
            removeSDCard(switcher);
            break;
        }
      }
    },
    OFF(STATE_OFF, R.string.switcher_null) {
      @Override
      public void onClick(final SwitcherView switcher) {
        switch (switcher.switcherType) {
          case EXTERNAL_SD_CARD:
            LogHelper.settingClick(LogHelper.SETTING_SDCARD);
            mountSDCard(switcher);
            break;
        }
      }
    },
    CLEAN_ON(STATE_CLEAN_ON, R.string.switcher_clean) {
      @Override
      protected void onClick(SwitcherView switcher) {
        switcher.setAction(CLEAN_OFF);
        if (CleanManager.getInstance().isCleaning()) {
          toastIsCleaning();
          return;
        }
        LogHelper.settingClick(LogHelper.SETTING_CLEAR);

        startClean(switcher);
      }
    },
    CLEAN_OFF(STATE_CLEAN_OFF, R.string.switcher_cleaning) {
      @Override
      public void onClick(final SwitcherView switcher) {
        toastIsCleaning();
      }
    };

    private final int[] drawableState;
    private final int textResId;

    private Action(int[] drawableState, int textResId) {
      this.drawableState = drawableState;
      this.textResId = textResId;
    }

    private int[] getDrawableState() {
      return drawableState;
    }

    private void apply(SwitcherView switcher) {
      switcher.setText(textResId);
    }

    protected abstract void onClick(SwitcherView switcher);
  }

  public enum SwitcherType {
    CLEAN, EXTERNAL_SD_CARD, GOOGLE_SERVICE
  }


  public SwitcherView(Context context) {
    super(context);
  }

  public SwitcherView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SwitcherView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private OnFinishCleanListener onFinishCleanListener =
      new OnFinishCleanListener() {
        @Override
        public void onFinishClean(long releaseSizeInByte) {
          if (switcherType == SwitcherType.CLEAN) {
            setAction(Action.CLEAN_ON);
          }
        }
      };

  private SDCardMountListener sdCardMountListener =
      new SDCardMountListener() {
        @Override
        public void onMountComplete() {
          if (switcherType == SwitcherType.EXTERNAL_SD_CARD) {
            setAction(Action.ON);
            SwitcherView.this.requestFocus();
          }

        }

        @Override
        public void onUnMountComplete() {
          setAction(Action.OFF);
          SwitcherView.this.requestFocus();
        }
      };

  private Action action;
  private SwitcherType switcherType;

  public void setAction(Action action) {
    if (this.action != action) {
      this.action = action;
      action.apply(this);
      refreshDrawableState();
    }
  }

  protected void onClick() {
    if (action != null) {
      action.onClick(this);
    }
  }

  @Override
  public int[] onCreateDrawableState(int extraSpace) {
    int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (action == null) {
      mergeDrawableStates(drawableState, STATE_OFF);
    } else {
      mergeDrawableStates(drawableState, action.getDrawableState());
    }
    return drawableState;
  }

  public void setData(SwitcherType switcherType) {
    reset();
    this.switcherType = switcherType;
    initSwitcher();
  }

  private void reset() {
    switcherType = null;
    action = null;
  }

  private void initSwitcher() {
    switch (switcherType) {
      case CLEAN:
        CleanManager.getInstance().setCleanListener(onFinishCleanListener);
        initCleaningStatus();
        break;
      case EXTERNAL_SD_CARD:
        SDCardManager.getInstance().setSDCardMountListener(sdCardMountListener);
        initExternalSDCardStatus();
        break;
    }
  }


  private void initCleaningStatus() {
    if (CleanManager.getInstance().isCleaning()) {
      setAction(Action.CLEAN_OFF);
    } else {
      setAction(Action.CLEAN_ON);
    }
  }

  private void initExternalSDCardStatus() {
    SDCardManager.getInstance().initSdCardStatus();
    if (SDCardManager.getInstance().getSdCardStatus() == SDCardManager.SDCardStatus.ENABLED) {
      setAction(Action.ON);
    } else {
      setAction(Action.OFF);
    }
  }


  /**
   * clean
   */
  private static void startClean(SwitcherView switcher) {
    CleanManager.getInstance().asyncCleanWithDefaultToast();
  }

  private static void toastIsCleaning() {
    MainThreadPostUtils.toast(R.string.switcher_cleaning_tips);
  }



  /**
   * mount sd card
   */
  private static void mountSDCard(SwitcherView switcher) {
    if (SDCardManager.getInstance().getSdCardStatus() == SDCardManager.SDCardStatus.ENABLED) {
      switcher.setAction(Action.ON);
    }
    SDCardManager.getInstance().updateSdCardUnMountStatus();
    if (SDCardManager.getInstance().getSdCardStatus() == SDCardManager.SDCardStatus.NO_SDCARD) {
      SDCardManager.getInstance().showAlertDialog(switcher.getContext(),
          SDCardManager.ProcessTask.NOT_FOUND);
    } else if (SDCardManager.getInstance().getSdCardStatus() == SDCardManager.SDCardStatus.NO_MOUNT) {
      SDCardManager.getInstance().showAlertDialog(switcher.getContext(),
          SDCardManager.ProcessTask.MOUNT);
    }
  }

  private static void removeSDCard(SwitcherView switcher) {
    SDCardManager.getInstance().showAlertDialog(switcher.getContext(),
        SDCardManager.ProcessTask.REMOVE);
  }
}
