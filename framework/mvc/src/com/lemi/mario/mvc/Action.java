package com.lemi.mario.mvc;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface Action {
  void execute();

  public static class EmptyAction implements Action {
    @Override
    public void execute() {}
  }
}
