package com.lemi.mario.externalmanager.config;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TVConfig {

  /**
   * LETV box
   */
  private static final String G1_MODEL = "REFK02";
  private static final String NEWC1S_MODEL = "AMLOGIC8726MX";

  /**
   * LETV tv
   */
  private static final String X60_70_MODEL = "LeTVX60";
  private static final String S50_X50_AIR_MODEL = "MStar Android TV";
  private static final String X55_MODEL = "MStar Android TV for UHD";
  private static final String S50_MODEL = "Android TV on MStar Amber3";
  private static final String NEW_X60_MODEL = "Letv X60";
  private static final String NEW_X70_MODEL = "Letv Max70";
  private static final String NEW_X55_MODEL = "Letv X55 Air";
  private static final String NEW_X50AIR_MODEL = "Letv X50 Air";
  private static final String NEW_S50AIR_MODEL = "Letv S50 Air";
  private static final String NEW_S40AIR_MODEL = "Letv S40 Air";
  private static final String NEW_S40_MODEL = "Letv S40";
  private static final String NEW_S50_MODEL = "Letv S40";
  private static final String NEW_GS39_MODEL = "Letv GS39";

  private static boolean isLetv = false;
  private static boolean isLetvBox = false;

  public static boolean isIsLetv() {
    return isLetv;
  }

  public static boolean isIsLetvBox() {
    return isLetvBox;
  }


  public static void initTVVersion() {
    isLetv = false;
    isLetvBox = false;
    String model = android.os.Build.MODEL;
    if (model.equals(X60_70_MODEL)
        || model.equals(NEW_X60_MODEL)
        || model.equals(NEW_X70_MODEL)
        || model.equals(S50_MODEL)
        || model.equals(NEW_S50_MODEL)
        || model.equals(NEW_S40_MODEL)
        || model.equals(NEW_GS39_MODEL)
        || model.equals(S50_X50_AIR_MODEL)
        || model.equals(X55_MODEL)
        || model.equals(G1_MODEL)
        || model.equals(NEWC1S_MODEL)
        || model.equals(NEW_S40AIR_MODEL)
        || model.equals(NEW_S50AIR_MODEL)
        || model.equals(NEW_X50AIR_MODEL)
        || model.equals(NEW_X55_MODEL)) {
      isLetv = true;
    }
    if (model.equals(G1_MODEL)
        || model.equals(NEWC1S_MODEL)) {
      isLetvBox = true;
    }
  }

}
