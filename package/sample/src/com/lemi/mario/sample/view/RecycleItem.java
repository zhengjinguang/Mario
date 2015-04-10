package com.lemi.mario.sample.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemi.mario.base.utils.ViewUtils;
import com.lemi.mario.sample.R;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class RecycleItem extends RelativeLayout {

  private ScaleAsyncImageView i1;
  private ScaleAsyncImageView i2;
  private ScaleAsyncImageView i3;
  private ScaleAsyncImageView i4;
  private ScaleAsyncImageView i5;
  private TextView t1;
  private TextView t2;
  private TextView t3;
  private TextView t4;
  private TextView t5;

  public RecycleItem(Context context) {
    super(context);
  }

  public RecycleItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RecycleItem(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    i1 = (ScaleAsyncImageView) findViewById(R.id.i1);
    i2 = (ScaleAsyncImageView) findViewById(R.id.i2);
    i3 = (ScaleAsyncImageView) findViewById(R.id.i3);
    i4 = (ScaleAsyncImageView) findViewById(R.id.i4);
    i5 = (ScaleAsyncImageView) findViewById(R.id.i5);

    t1 = (TextView) findViewById(R.id.t1);
    t2 = (TextView) findViewById(R.id.t2);
    t3 = (TextView) findViewById(R.id.t3);
    t4 = (TextView) findViewById(R.id.t4);
    t5 = (TextView) findViewById(R.id.t5);

    this.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
          setChildFocus();
        }
      }
    });

  }

  private void clearChildFocus(){
    i1.setFocusable(false);
    i2.setFocusable(false);
    i3.setFocusable(false);
    i4.setFocusable(false);
    i5.setFocusable(false);
  }

  private void setChildFocus(){
    i1.setFocusable(true);
    i2.setFocusable(true);
    i3.setFocusable(true);
    i4.setFocusable(true);
    i5.setFocusable(true);
  }

  public void bind(int position , String url){
    i1.loadNetworkImage(url,-1);
    i2.loadNetworkImage(url,-1);
    i3.loadNetworkImage(url,-1);
    i4.loadNetworkImage(url,-1);
    i5.loadNetworkImage(url,-1);

    t1.setText("\"IT 'S ME\" "+position);
    t2.setText(""+position);
    t3.setText(""+position);
    t4.setText(""+position);
    t5.setText(""+position);
  }

  public static RecycleItem newInstance(ViewGroup parent) {
    return (RecycleItem) ViewUtils.newInstance(parent, R.layout.recycle_item);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_UP:
//        System.out.println("Layout key down is = up");
        break;
      case KeyEvent.KEYCODE_DPAD_DOWN:
//        System.out.println("Layout key down is = down");
        break;
    }

    return super.onKeyDown(keyCode, event);
  }


}
