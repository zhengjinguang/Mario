package com.lemi.controller.lemigameassistance;

import com.lemi.controller.lemigameassistance.model.OtherAwardModel;
import com.lemi.mario.base.utils.StringUtil;

import java.util.Random;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class FakeAwardsFactory {

  public static final String[] AWARDNAMES = new String[] {
      StringUtil.getString(R.string.awards_gamepad), StringUtil.getString(R.string.awards_doll),
      StringUtil.getString(R.string.awards_sdcard),
      StringUtil.getString(R.string.awards_red_envelope),
      StringUtil.getString(R.string.awards_red_envelope)
  };
  private static Random random = new Random();

  public static OtherAwardModel generateFakeAward() {
    OtherAwardModel otherAwardModel = new OtherAwardModel();
    otherAwardModel.setAwardName(AWARDNAMES[random.nextInt(AWARDNAMES.length)]);
    otherAwardModel.setUid(random.nextInt(1000));
    return otherAwardModel;
  }
}
