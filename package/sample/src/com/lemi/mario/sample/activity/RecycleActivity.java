package com.lemi.mario.sample.activity;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import com.lemi.mario.base.utils.MainThreadPostUtils;
import com.lemi.mario.sample.R;
import com.lemi.mario.sample.recyclerview.BaseRecycleAdapter;
import com.lemi.mario.sample.recyclerview.GridItemModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RecycleActivity extends Activity {

  private RecyclerView recyclerView;
  private BaseRecycleAdapter recycleAdapter;
  private RecyclerView.LayoutManager layoutManager;
  private RecyclerView.SmoothScroller smoothScroller;

  private static List<GridItemModel> dataList;
  private int position = 0;
  private String[] urlList =
      {
          "http://i1.hexunimg.cn/2013-01-08/149920102.jpg",
          "http://i3.hexunimg.cn/2013-01-08/149920105.jpg",
          "http://i3.hexunimg.cn/2013-01-08/149920001.jpg",
          "http://images.cnblogs.com/cnblogs_com/skynet/WindowsLiveWriter/Androidandroid_1C63/Android_system_architecture_2.jpg",
          "http://i3.hexunimg.cn/2013-01-08/149920106.jpg",
          "http://i3.hexunimg.cn/2013-01-08/149920108.jpg",
          "http://i3.hexunimg.cn/2013-01-08/149920109.jpg",
          "http://i3.hexunimg.cn/2013-01-08/149920111.jpg",
          "http://networksecurity.isvoc.com/images/bf79071dac0d41b1bbd3993309ebfdd1.jpg",
          "http://networksecurity.isvoc.com/images/820b3378f2c84f1da2c81e7b83458f5b.jpg"};


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recycle);
    position = 0;

    recyclerView = (RecyclerView) findViewById(R.id.recycle_container);
    // layoutManager = new GridLayoutManager(this, 3);
    layoutManager = new LinearLayoutManager(this);
    layoutManager.scrollToPosition(0);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(true);

    initData(0);

    recycleAdapter = new BaseRecycleAdapter(dataList);

    recyclerView.setAdapter(recycleAdapter);

  }


  private void initData(int start) {
    if (dataList == null) {
      dataList = new ArrayList<GridItemModel>();
    }
    dataList.clear();
    if (start < 0) {
      start = 0;
    }
    int end = start + 300;
    int urlLenth = urlList.length;
    for (int i = start; i < end; i++) {
      GridItemModel gridItemModel = new GridItemModel("" + i, urlList[i % urlLenth]);
      dataList.add(gridItemModel);
    }
  }

  private void judgeScrollTo() {
    if (position == 0) {
      smoothScroller = new LinearSmoothScroller(RecycleActivity.this) {
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
          return new PointF(0, 1);
        }
      };
      position = 99;

    } else if (position == 99) {
      smoothScroller = new LinearSmoothScroller(RecycleActivity.this) {
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
          return new PointF(0, 1);
        }
      };
      position = 198;
    } else if (position == 198) {
      smoothScroller = new LinearSmoothScroller(RecycleActivity.this) {
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
          return new PointF(0, -1);
        }
      };
      position = 0;
    }
    smoothScroller.setTargetPosition(position);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      initData(1000);
      recycleAdapter.notifyDataSetChanged();
    }

    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//      System.out.println("scorll to = "
//          + (recyclerView.getChildPosition(recyclerView.getFocusedChild()) + 1) + "\n");
//
//      recyclerView.smoothScrollToPosition(recyclerView.getChildPosition(recyclerView
//              .getFocusedChild()) + 1);
//      focusAfterscroll();
      recyclerView.smoothScrollBy(0,1200);
      focusAfterscroll();
    }

    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_UP:
//        System.out.println("activity key down is = up");
        break;
      case KeyEvent.KEYCODE_DPAD_DOWN:
//        System.out.println("activity key down is = down");
        break;
    }

    return super.onKeyDown(keyCode, event);
  }


  private void focusAfterscroll(){
    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForPosition(recyclerView.getChildPosition(recyclerView.getFocusedChild()) + 1);
    if(viewHolder != null){
      viewHolder.itemView.requestFocus();
    }else {
      MainThreadPostUtils.postDelayed(new Runnable() {
        @Override
        public void run() {
          focusAfterscroll();
        }
      }, 50l);
    }

  }


}
