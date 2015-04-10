package com.lemi.mario.externalmanager.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MountUtils {

  private static final String MOUNT_FLAG = "sdcard";

  public static boolean isSdCardMounted() {
    Process process = null;
    BufferedReader input = null;
    try {
      process = Runtime.getRuntime().exec("getprop persist.mountpoint.ext");
      if (process == null) {
        return false;
      }

      input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String sdCard = input.readLine();
      if (MOUNT_FLAG.equals(sdCard)) {
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }

}
