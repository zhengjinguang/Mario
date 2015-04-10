package com.lemi.mario.log.model;

import java.util.Map;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class LogEventModel {

  public enum Priority {
    NORMAL(0), REAL_TIME(100);

    private int level;

    private Priority(int level) {
      this.level = level;
    }

    public int getLevel() {
      return level;
    }
  }

  public final String event;
  public final Map<String, String> params;
  public final long createdAt;
  public final Priority priority;

  public LogEventModel(String event, Map<String, String> params, long createdAt,
      Priority priority) {
    this.event = event;
    this.params = params;
    this.createdAt = createdAt;
    this.priority = priority;
  }
}
