package com.lemi.mario.download.rpc.speed;

/**
 * A helper of speed recoder, this helper will save last update time to recode speed simply,
 * just call {@link SpeedHelper#record(long)}, and the helper also provide a AVERAGE_SPEED_RANGE
 * to get average speed simply, just call {@link SpeedHelper#getSpeed()}.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SpeedHelper {

  private static final long NODE_INTERVAL = 500L;
  private static final long AVERAGE_SPEED_RANGE = 3000L;
  private static final long RECORDER_MAX_RANGE = 5000L;

  private long lastUpdateTime;
  private final SpeedRecorder speedRecorder;

  public SpeedHelper() {
    this.lastUpdateTime = SpeedRecorder.current();
    this.speedRecorder = new SpeedRecorder(NODE_INTERVAL, RECORDER_MAX_RANGE);
  }

  /**
   * Record a speed node.
   * 
   * @param size the transferrd speed from last update time.
   */
  public void record(long size) {
    long current = SpeedRecorder.current();
    speedRecorder.record(lastUpdateTime, current, size);
    lastUpdateTime = current;
  }

  /**
   * @return the task speed.
   */
  public long getSpeed() {
    return speedRecorder.getAverageSpeed(AVERAGE_SPEED_RANGE);
  }
}
