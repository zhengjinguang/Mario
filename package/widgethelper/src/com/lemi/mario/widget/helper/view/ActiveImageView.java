package com.lemi.mario.widget.helper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lemi.mario.widget.helper.R;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class ActiveImageView extends ImageView {

  protected static final int[] STATE_ACTIVE = {R.attr.state_active};
  protected static final int[] STATE_SLEEP = {R.attr.state_sleep};

  /**
   * Action of button.
   */
  public static enum Action {
    ACTIVE(STATE_ACTIVE),
    SLEEP(STATE_SLEEP);

    private final int[] drawableState;

    private Action(int[] drawableState) {
      this.drawableState = drawableState;
    }

    private int[] getDrawableState() {
      return drawableState;
    }

  }

  public ActiveImageView(Context context) {
    super(context);
  }

  public ActiveImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ActiveImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private Action action;

  public void setAction(Action action) {
    if (this.action != action) {
      this.action = action;
      refreshDrawableState();
    }
  }

  public Action getAction() {
    return action;
  }

  @Override
  public int[] onCreateDrawableState(int extraSpace) {
    int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (action == null) {
      mergeDrawableStates(drawableState, STATE_SLEEP);
    } else {
      mergeDrawableStates(drawableState, action.getDrawableState());
    }
    return drawableState;
  }

}
