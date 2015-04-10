package com.lemi.mario.sample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

import com.lemi.mario.sample.R;
import com.lemi.mario.sample.view.CardItemContainer;
import com.lemi.mario.sample.view.ScaleImageButton;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CardActivity extends Activity {

  private CardItemContainer cardItemContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_card);

    cardItemContainer = (CardItemContainer) findViewById(R.id.card_item_container);

    DisplayMetrics DM = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(DM);

    int width = this.getResources().getDisplayMetrics().widthPixels;
    int height = this.getResources().getDisplayMetrics().heightPixels;


    System.out.println("Screen width is = " + width + " , Screen height is = " + height);

    findViewById(R.id.add_large_item).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cardItemContainer.addView(ScaleImageButton.newInstanceLarge(cardItemContainer).getView());
      }
    });

    findViewById(R.id.add_little_item).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cardItemContainer.addView(ScaleImageButton.newInstance(cardItemContainer).getView());
      }
    });

    findViewById(R.id.add_normal_item).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cardItemContainer.addView(ScaleImageButton.newInstanceNormal(cardItemContainer).getView());
      }
    });


    findViewById(R.id.pre).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cardItemContainer.startScrollToPreScreen();
      }
    });

    findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cardItemContainer.startScrollToNextScreen();
      }
    });

    findViewById(R.id.remove_all).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        cardItemContainer.removeAllViews();
      }
    });

  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }
}
