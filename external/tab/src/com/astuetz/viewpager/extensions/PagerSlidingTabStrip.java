/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.astuetz.viewpager.extensions;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;


@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class PagerSlidingTabStrip extends HorizontalScrollView {

  private static final int[] ATTRS = new int[] {
      android.R.attr.textSize,
      android.R.attr.textColor
  };
  private final PageListener pageListener = new PageListener();
  public OnPageChangeListener delegatePageListener;
  private LinearLayout.LayoutParams defaultTabLayoutParams;
  private LinearLayout.LayoutParams expandedTabLayoutParams;
  private LinearLayout tabsContainer;
  private ViewPager pager;
  private int tabCount;
  private int currentPosition = 0;
  private float currentPositionOffset = 0f;
  private int currentSelectedPosition = -1;
  private Paint rectPaint;
  private Paint dividerPaint;
  private boolean checkedTabWidths = false;
  private int indicatorColor = 0xFF666666;
  private int underlineColor = 0x1A000000;
  private int dividerColor = 0x1A000000;
  private boolean shouldExpand = false;
  private boolean textAllCaps = true;
  private int scrollOffset = 52;
  private int indicatorHeight = 8;
  private int underlineHeight = 2;
  private int dividerPadding = 12;
  private int tabPadding = 24;
  private int dividerWidth = 1;
  private int tabTextSize = 12;
  private int tabTextColor = 0xFF666666;
  private ColorStateList tabTextColorStateList;
  private Typeface tabTypeface = null;
  private int tabTypefaceStyle = Typeface.NORMAL;
  private int lastScrollX = 0;
  private int tabBackgroundResId = 0;
  private Locale locale;

  public PagerSlidingTabStrip(Context context) {
    this(context, null);
  }

  public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setFillViewport(true);
    setWillNotDraw(false);

    DisplayMetrics dm = getResources().getDisplayMetrics();
    scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
    indicatorHeight =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
    underlineHeight =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
    dividerPadding =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
    tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
    dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
    tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

    // get system attrs (android:textSize and android:textColor)
    TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
    tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
    tabTextColor = a.getColor(1, tabTextColor);
    a.recycle();

    // get custom attrs
    a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);
    indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_indicatorColor, indicatorColor);
    underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_underlineColor, underlineColor);
    dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_dividerColor, dividerColor);
    indicatorHeight =
        a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_indicatorHeight, indicatorHeight);
    underlineHeight =
        a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_underlineHeight, underlineHeight);
    dividerPadding =
        a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_dividerPadding, dividerPadding);
    tabPadding =
        a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_tabPaddingLeftRight, tabPadding);
    tabBackgroundResId =
        a.getResourceId(R.styleable.PagerSlidingTabStrip_tabBackground, tabBackgroundResId);
    shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_shouldExpand, shouldExpand);
    scrollOffset =
        a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_scrollOffset, scrollOffset);
    textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_textAllCaps, textAllCaps);
    tabTextColorStateList = a.getColorStateList(R.styleable.PagerSlidingTabStrip_tabTextColor);
    tabTextSize = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_tabTextSize,
        tabTextSize);
    a.recycle();

    rectPaint = new Paint();
    rectPaint.setAntiAlias(true);
    rectPaint.setStyle(Style.FILL);

    dividerPaint = new Paint();
    dividerPaint.setAntiAlias(true);
    dividerPaint.setStrokeWidth(dividerWidth);

    defaultTabLayoutParams =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

    if (locale == null) {
      locale = getResources().getConfiguration().locale;
    }
    tabsContainer = new LinearLayout(context);
    tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT);
    params.bottomMargin = underlineHeight;
    tabsContainer.setLayoutParams(params);
    addView(tabsContainer);
  }

  public void setViewPager(final ViewPager viewPager) {
    this.pager = viewPager;
    if (pager.getAdapter() == null) {
      throw new IllegalStateException("ViewPager does not have adapter instance.");
    }
    pager.setOnPageChangeListener(pageListener);
    getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

      @SuppressWarnings("deprecation")
      @SuppressLint("NewApi")
      @Override
      public void onGlobalLayout() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
          getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
          getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }

        currentPosition = pager.getCurrentItem();
        currentPositionOffset = 0;
        scrollToChild(currentPosition, 0);
      }
    });
    notifyDataSetChanged();
    tabsContainer.getChildAt(pager.getCurrentItem()).requestFocus();
  }

  public void setOnPageChangeListener(OnPageChangeListener listener) {
    this.delegatePageListener = listener;
  }

  public void setAllTabEnabled(boolean isEnabled) {
    for (int i = 0; i < tabsContainer.getChildCount(); ++i) {
      View tab = tabsContainer.getChildAt(i);
      tab.setEnabled(isEnabled);
    }
  }

  public void notifyDataSetChanged() {
    tabsContainer.removeAllViews();
    tabCount = pager.getAdapter().getCount();
    for (int i = 0; i < tabCount; i++) {
      if (pager.getAdapter() instanceof TabProvider) {
        addTab(i, ((TabProvider) pager.getAdapter()).getTab(i));
      } else {
        Tab tab = new Tab(Integer.toString(i), pager.getAdapter().getPageTitle(i));
        // TODO yangfan, id = ?
        addTab(i, tab);
      }
    }
    updateTabStyles();
    selectTab(pager.getCurrentItem());
    checkedTabWidths = false;
  }

  public void requestTabFocus(int index) {
    if (index < tabsContainer.getChildCount()) {
      View child = tabsContainer.getChildAt(index);
      if(child != null){
        child.requestFocus();
      }
    }
  }

  private void addTab(final int position, Tab tab) {
    View tabView = tab.buildTabView(getContext(), position, pager);
    tabsContainer.addView(tabView, position);
  }

  private void updateTabStyles() {
    for (int i = 0; i < tabCount; i++) {
      View v = tabsContainer.getChildAt(i);
      v.setLayoutParams(defaultTabLayoutParams);
      v.setBackgroundResource(tabBackgroundResId);
      v.setPadding(tabPadding, 0, tabPadding, 0);

      if (v instanceof TextView) {
        TextView tab = (TextView) v;
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
        tab.setTypeface(tabTypeface, tabTypefaceStyle);
        if (tabTextColorStateList != null) {
          tab.setTextColor(tabTextColorStateList);
        } else {
          tab.setTextColor(tabTextColor);
        }
        // setAllCaps() is only available from API 14, so the upper case is made manually if we are
        // on a
        // pre-ICS-build
        if (textAllCaps) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            tab.setAllCaps(true);
          } else {
            tab.setText(tab.getText().toString().toUpperCase(locale));
          }
        }
      }
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (!shouldExpand || checkedTabWidths ||
        MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      return;
    }
    if (!checkedTabWidths) {
      // not measured yet
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    int myWidth = getMeasuredWidth();
    int childWidth = 0;
    for (int i = 0; i < tabCount; i++) {
      childWidth += tabsContainer.getChildAt(i).getMeasuredWidth();
    }
    if (childWidth > 0 && myWidth > 0) {
      scrollOffset = tabsContainer.getChildAt(0).getMeasuredWidth();
      if (childWidth <= myWidth) {
        for (int i = 0; i < tabCount; i++) {
          View v = tabsContainer.getChildAt(i);
          v.setLayoutParams(expandedTabLayoutParams);
          v.setPadding(0, 0, 0, 0);
        }
      }
      checkedTabWidths = true;
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    updateTabStyles();
    checkedTabWidths = false;
    post(new Runnable() {
      @Override
      public void run() {
        scrollToChild(currentSelectedPosition, 0);
      }
    });
  }

  private void scrollToChild(int position, int offset) {
    if (tabCount == 0) {
      return;
    }
    int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;
    if (position > 0 || offset > 0) {
      newScrollX -= scrollOffset;
    }
    if (newScrollX != lastScrollX) {
      lastScrollX = newScrollX;
      scrollTo(newScrollX, 0);
    }
  }

  private void selectTab(int position) {
    if (currentSelectedPosition == position) {
      return;
    }
    if (position >= tabCount || position < 0) {
      return;
    }
    View previousSelectedView = tabsContainer.getChildAt(currentSelectedPosition);
    if (previousSelectedView != null) {
      previousSelectedView.setSelected(false);
    }
    currentSelectedPosition = position;
    View currentSelectedView = tabsContainer.getChildAt(currentSelectedPosition);
    if (currentSelectedView != null) {
      currentSelectedView.setSelected(true);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (isInEditMode() || tabCount == 0) {
      return;
    }
    final int height = getHeight();
    // draw underline
    rectPaint.setColor(underlineColor);
    canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);
    // draw indicator line
    rectPaint.setColor(indicatorColor);

    // default: line below current tab
    View currentTab = tabsContainer.getChildAt(currentPosition);
    float lineLeft = currentTab.getLeft();
    float lineRight = currentTab.getRight();

    // if there is an offset, start interpolating left and right coordinates between current and
    // next tab
    if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {
      View nextTab = tabsContainer.getChildAt(currentPosition + 1);
      final float nextTabLeft = nextTab.getLeft();
      final float nextTabRight = nextTab.getRight();

      lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
      lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
    }
    canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);

    // draw divider
    dividerPaint.setColor(dividerColor);
    for (int i = 0; i < tabCount - 1; i++) {
      View tab = tabsContainer.getChildAt(i);
      canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding,
          dividerPaint);
    }
  }

  public void setIndicatorColorResource(int resId) {
    this.indicatorColor = getResources().getColor(resId);
    invalidate();
  }

  public int getIndicatorColor() {
    return this.indicatorColor;
  }

  public void setIndicatorColor(int indicatorColor) {
    this.indicatorColor = indicatorColor;
    invalidate();
  }

  public int getIndicatorHeight() {
    return indicatorHeight;
  }

  public void setIndicatorHeight(int indicatorLineHeightPx) {
    this.indicatorHeight = indicatorLineHeightPx;
    invalidate();
  }

  public void setUnderlineColorResource(int resId) {
    this.underlineColor = getResources().getColor(resId);
    invalidate();
  }

  public int getUnderlineColor() {
    return underlineColor;
  }

  public void setUnderlineColor(int underlineColor) {
    this.underlineColor = underlineColor;
    invalidate();
  }

  public void setDividerColorResource(int resId) {
    this.dividerColor = getResources().getColor(resId);
    invalidate();
  }

  public int getDividerColor() {
    return dividerColor;
  }

  public void setDividerColor(int dividerColor) {
    this.dividerColor = dividerColor;
    invalidate();
  }

  public int getUnderlineHeight() {
    return underlineHeight;
  }

  public void setUnderlineHeight(int underlineHeightPx) {
    this.underlineHeight = underlineHeightPx;
    invalidate();
  }

  public int getDividerPadding() {
    return dividerPadding;
  }

  public void setDividerPadding(int dividerPaddingPx) {
    this.dividerPadding = dividerPaddingPx;
    invalidate();
  }

  public int getScrollOffset() {
    return scrollOffset;
  }

  public void setScrollOffset(int scrollOffsetPx) {
    this.scrollOffset = scrollOffsetPx;
    invalidate();
  }

  public boolean getShouldExpand() {
    return shouldExpand;
  }

  public void setShouldExpand(boolean shouldExpand) {
    this.shouldExpand = shouldExpand;
    requestLayout();
  }

  public boolean isTextAllCaps() {
    return textAllCaps;
  }

  public void setAllCaps(boolean textAllCaps) {
    this.textAllCaps = textAllCaps;
  }

  public int getTextSize() {
    return tabTextSize;
  }

  public void setTextSize(int textSizePx) {
    this.tabTextSize = textSizePx;
    updateTabStyles();
  }

  public void setTextColorResource(int resId) {
    this.tabTextColor = getResources().getColor(resId);
    updateTabStyles();
  }

  public int getTextColor() {
    return tabTextColor;
  }

  public void setTextColor(int textColor) {
    this.tabTextColor = textColor;
    updateTabStyles();
  }

  public void setTypeface(Typeface typeface, int style) {
    this.tabTypeface = typeface;
    this.tabTypefaceStyle = style;
    updateTabStyles();
  }

  public int getTabBackground() {
    return tabBackgroundResId;
  }

  public void setTabBackground(int resId) {
    this.tabBackgroundResId = resId;
  }

  public int getTabPaddingLeftRight() {
    return tabPadding;
  }

  public void setTabPaddingLeftRight(int paddingPx) {
    this.tabPadding = paddingPx;
    updateTabStyles();
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    currentPosition = savedState.currentPosition;
    requestLayout();
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.currentPosition = currentPosition;
    return savedState;
  }

  public interface TabProvider {
    Tab getTab(int position);

    Tab getTab(String id);

    int getTabPositionById(String id);

    String getTabIdByPosition(int position);
  }

  static class SavedState extends BaseSavedState {
    public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
    int currentPosition;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      currentPosition = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(currentPosition);
    }
  }

  public static class Tab {
    private CharSequence text;
    private View customView;
    private View tabView;
    private int position;
    /**
     * Tab id, used for navigation.
     */
    private String id;

    public Tab(String id) {
      this.id = id;
    }

    public Tab(String id, CharSequence text) {
      this(id);
      this.text = text;
    }

    public Tab(String id, View customView) {
      this(id);
      this.customView = customView;
    }

    public CharSequence getText() {
      return text;
    }

    public void setText(CharSequence text) {
      this.text = text;
      if (tabView instanceof TextView) {
        ((TextView) tabView).setText(text);
      }
    }

    public View getCustomView() {
      return customView;
    }

    public View getTabView() {
      return tabView;
    }

    public int getPosition() {
      return position;
    }

    public View buildTabView(Context context, final int position, final ViewPager pager) {
      this.position = position;
      if (customView != null) {
        tabView = customView;
      } else {
        tabView = new TextView(context);
        TextView textTabView = (TextView) tabView;
        textTabView.setText(text);
        textTabView.setFocusable(true);
        textTabView.setFocusableInTouchMode(true);
        textTabView.setGravity(Gravity.CENTER);
        textTabView.setSingleLine();
      }
      tabView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          pager.setCurrentItem(position);
        }
      });
      return tabView;
    }

    public String getId() {
      return id;
    }
  }

  private class PageListener implements OnPageChangeListener {

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      if (position >= tabsContainer.getChildCount()) {
        return;
      }
      currentPosition = position;
      currentPositionOffset = positionOffset;
      scrollToChild(position,
          (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));

      invalidate();

      if (delegatePageListener != null) {
        delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
      }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
      if (state == ViewPager.SCROLL_STATE_IDLE) {
        scrollToChild(pager.getCurrentItem(), 0);
      }

      if (delegatePageListener != null) {
        delegatePageListener.onPageScrollStateChanged(state);
      }
    }

    @Override
    public void onPageSelected(int position) {
      if (pager.getAdapter().getCount() <= position) {
        return;
      }
      selectTab(position);
      invalidate();
      if (delegatePageListener != null) {
        delegatePageListener.onPageSelected(position);
      }
    }

  }

}
