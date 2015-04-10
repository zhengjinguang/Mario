package com.lemi.mario.widget.helper.utils;

import android.view.KeyEvent;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SurpriseUtils {

  private static final SurpriseKey keyOrder[] = {SurpriseKey.DOWN, SurpriseKey.RIGHT,
      SurpriseKey.DOWN, SurpriseKey.RIGHT, SurpriseKey.MENU};

  private enum SurpriseKey {
    DOWN(KeyEvent.KEYCODE_DPAD_DOWN),
    RIGHT(KeyEvent.KEYCODE_DPAD_RIGHT),
    MENU(KeyEvent.KEYCODE_MENU);

    private int keyCode;

    SurpriseKey(int keyCode) {
      this.keyCode = keyCode;
    }

    public int getKeyCode() {
      return keyCode;
    }
  }

  private int position = 0;

  public SurpriseUtils() {}

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (position < keyOrder.length && keyOrder[position].getKeyCode() == keyCode) {
      position++;
      if (position == keyOrder.length) {
        return true;
      }
    } else {
      position = 0;
    }
    return false;
  }


}
