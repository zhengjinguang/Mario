package com.lemi.mario.download.rpc.speed;

/**
 * Global speed manager to record and calulate all task speed.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GlobalSpeedManager {

  private static GlobalSpeedManager instance;

  private long totalLength;
  private long lastLogTime;

  private GlobalSpeedManager() {
    // do nothing
  }

  public static synchronized GlobalSpeedManager getInstance() {
    if (instance == null) {
      instance = new GlobalSpeedManager();
    }
    return instance;
  }

  public synchronized void record(long bytes) {
    if (lastLogTime == 0) {
      lastLogTime = SpeedRecorder.current();
    }
    totalLength += bytes;
  }

  /**
   * Calulate speed from last update time, and then reset the length to zero.
   * 
   * @return the global speed, the speed unit is second / byte.
   */
  public synchronized Speed getAndResetSpeed() {
    // cal speed
    long current = SpeedRecorder.current();
    long timeInterval = current - lastLogTime;
    Speed speed = new Speed(totalLength, timeInterval);
    // reset value
    lastLogTime = current;
    totalLength = 0L;
    return speed;
  }

  public static class Speed {

    /** the total transported length in this duration, the unit is {@code byte}. */
    public final long length;
    /** the total transport duration, the unit is {@code ms}. */
    public final long duration;
    /** the speed of this duration, the unit is {@code byte/s}. */
    public final long speed;

    private Speed(long length, long duration) {
      this.length = length;
      this.duration = duration;
      this.speed = duration == 0 ? 0 : length * 1000 / duration;
    }
  }
}
