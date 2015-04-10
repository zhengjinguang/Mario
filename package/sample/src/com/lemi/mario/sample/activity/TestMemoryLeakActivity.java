package com.lemi.mario.sample.activity;

import android.app.Activity;
import android.os.Bundle;

import com.lemi.mario.image.view.AsyncImageView;
import com.lemi.mario.sample.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TestMemoryLeakActivity extends Activity {

  private AsyncImageView a1;
  private AsyncImageView a2;
  private AsyncImageView a3;
  private AsyncImageView a4;
  private AsyncImageView a5;
  private AsyncImageView a6;

  private static final String TEST_URL = "http://i3.hexunimg.cn/2013-01-08/149920105.jpg";
  private static final int MEMORY_LEAK_TASK_INTERVAL = 5000;
  private static final String MEMORY_LEAK_TIMER = "memory_leak_timer";

  private TimerTask memoryLeakTask;
  private Timer memoryLeakTimer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.test_memory_leak_layout);

    a1 = (AsyncImageView) findViewById(R.id.a1);
    a2 = (AsyncImageView) findViewById(R.id.a2);
    a3 = (AsyncImageView) findViewById(R.id.a3);
    a4 = (AsyncImageView) findViewById(R.id.a4);
    a5 = (AsyncImageView) findViewById(R.id.a5);
    a6 = (AsyncImageView) findViewById(R.id.a5);

    a1.loadNetworkImage(TEST_URL, R.drawable.water_360_221);
    a2.loadNetworkImage(TEST_URL, R.drawable.water_360_221);
    a3.loadNetworkImage(TEST_URL, R.drawable.water_360_221);
    a4.loadNetworkImage(TEST_URL, R.drawable.water_360_221);
    a5.loadNetworkImage(TEST_URL, R.drawable.water_360_221);
    a6.loadNetworkImage(TEST_URL, R.drawable.water_360_221);

    startMemoryLeakTimer();

  }


  public synchronized void startMemoryLeakTimer() {
    if (memoryLeakTimer != null) {
      memoryLeakTimer.cancel();
    }
    memoryLeakTimer = new Timer(MEMORY_LEAK_TIMER, true);
    if (memoryLeakTask != null) {
      memoryLeakTask.cancel();
    }
    memoryLeakTask = new MemoryLeakTask();

    memoryLeakTimer.schedule(memoryLeakTask, MEMORY_LEAK_TASK_INTERVAL, MEMORY_LEAK_TASK_INTERVAL);
  }

  class MemoryLeakTask extends TimerTask {
    @Override
    public void run() {
      System.out.println("MemoryLeak Task running!");
    }
  }


}
