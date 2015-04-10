package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.lemi.controller.lemigameassistance.utils.ViewUtils;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TaggedTab extends PagerSlidingTabStrip.Tab {


  public TaggedTab(String id, CharSequence text) {
    super(id, text);
  }

  public TaggedTab(String id, View customView) {
    super(id, customView);
  }

  @Override
  public void setText(CharSequence text) {
    super.setText(text);
  }


  @Override
  public View buildTabView(Context context, final int position, final ViewPager pager) {
    View view = super.buildTabView(context, position, pager);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (v.isSelected()) {
          ViewUtils.scrollToTop(v.getContext());
        } else {
          pager.setCurrentItem(position, true);
        }
      }
    });
    view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (v instanceof TextView) {
          if (hasFocus) {
            if(pager.getCurrentItem() != position){
              pager.setCurrentItem(position);
            }
          }
        }

      }
    });
    return view;
  }
}
