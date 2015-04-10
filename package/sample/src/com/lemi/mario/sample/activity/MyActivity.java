package com.lemi.mario.sample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lemi.mario.sample.R;
import com.lemi.mario.sample.utils.LogHelper;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class MyActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);

    findViewById(R.id.launch_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.launch(System.currentTimeMillis());
      }
    });

    findViewById(R.id.quit_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.backToBackgroud(System.currentTimeMillis());
      }
    });

    findViewById(R.id.controller_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.connectController("Test Controller");
      }
    });

    findViewById(R.id.button_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.clickEvent("Test Button Name", null);
      }
    });

    findViewById(R.id.download_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.downloadComplete("Test PackageName", LogHelper.StatusType.FAILED,
            "Network Unavailable");
      }
    });

    findViewById(R.id.enter_page_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.enterPage("Test Page Name", null, System.currentTimeMillis());
      }
    });

    findViewById(R.id.exit_page_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.exitPage("Test Page Name", null, System.currentTimeMillis());
      }
    });

    findViewById(R.id.start_game_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.startGame("Test Game", System.currentTimeMillis());
      }
    });

    findViewById(R.id.quit_game_log).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LogHelper.quitGame("Test Game", System.currentTimeMillis());
      }
    });



  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.my, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_MENU) {
      MyActivity.this.startActivity(new Intent(MyActivity.this, TestMemoryLeakActivity.class));
      return true;
    }
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      MyActivity.this.startActivity(new Intent(MyActivity.this, RecycleActivity.class));
      return true;
    }

    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      MyActivity.this.startActivity(new Intent(MyActivity.this, CardActivity.class));
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
