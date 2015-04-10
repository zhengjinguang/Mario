package com.lemi.mario.download.rpc.speed;

import android.os.SystemClock;

import java.util.TreeMap;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class SpeedRecorder {

  /** the result when range is too large return. */
  public static final int RESULT_TOO_LARGE_RANGE = -2;
  /** the result when there is no record of this task return. */
  public static final int RESULT_NO_SPEED_RECORD = -1;

  private final long originTime;
  private final long nodeInterval;
  private final long maxRange;
  private final TreeMap<Integer, Long> nodes;
  private long lastEraseTime;

  public SpeedRecorder(long nodeInterval, long maxRange) {
    this.nodeInterval = nodeInterval;
    this.maxRange = maxRange;
    this.nodes = new TreeMap<Integer, Long>();
    this.originTime = current();
    this.lastEraseTime = current();
  }

  /**
   * Record a speed node, from start to end, the task of this id transferrd this size.
   * 
   * @param start the start time of this node.
   * @param end the end time of this node.
   * @param bytes the transferrd size of this node.
   */
  public void record(long start, long end, long bytes) {
    int startKey = computeKey(start);
    int endKey = computeKey(end);
    // allocation size to every node
    long nodeSize = bytes / (endKey - startKey + 1);
    while (startKey <= endKey) {
      appendSizeToNode(startKey, nodeSize);
      ++startKey;
    }
    // erase exired nodes
    if (current() - lastEraseTime > maxRange) {
      eraseExpired();
      lastEraseTime = current();
    }
  }

  /**
   * Get the average speed, from {current - range} to current.
   * 
   * @param range the range time before current.
   * @return the speed of this range, speed unit is byte/second.
   */
  public long getAverageSpeed(long range) {
    // range is too large
    if (range > maxRange) {
      return RESULT_TOO_LARGE_RANGE;
    }
    // no recode
    if (nodes.isEmpty()) {
      return RESULT_NO_SPEED_RECORD;
    }
    long totalSize = 0;
    int startKey = computeKey(current() - range);
    int currentKey = computeKey(current());
    while (startKey <= currentKey) {
      Long size = null;
      try {
        size = nodes.get(startKey);
      } catch (NullPointerException e) {
        // some case(unkown why), maybe throw nullpointerex
        e.printStackTrace();
      }

      if (size != null) {
        totalSize += size;
      }
      ++startKey;
    }
    return totalSize * 1000 / range;
  }


  private void appendSizeToNode(int timeKey, long size) {
    Long oldSize = nodes.get(timeKey);
    oldSize = oldSize == null ? size : oldSize + size;
    nodes.put(timeKey, oldSize);
  }

  private void eraseExpired() {
    int currentKey = computeKey(current());
    int firstKey;
    // if the first node is expired, then remove it.
    while ((currentKey - (firstKey = nodes.firstKey())) > (maxRange / nodeInterval)) {
      nodes.remove(firstKey);
    }
  }

  /**
   * The key is second, which use {@code time / 1000},
   * because the time should not before the {@code orginTime},
   * so we add {@code time - orginTime} to convert the second to a smaller int.
   */
  private int computeKey(long time) {
    return (int) ((time - originTime) / nodeInterval);
  }

  /**
   * Return current time, here we use {@link android.os.SystemClock#elapsedRealtime()},
   * because it won't be changed if we change time of mobile.
   */
  public static long current() {
    return SystemClock.elapsedRealtime();
  }
}
