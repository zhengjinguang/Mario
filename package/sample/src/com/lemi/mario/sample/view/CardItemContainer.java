package com.lemi.mario.sample.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.lemi.mario.sample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CardItemContainer extends ViewGroup {


  private class CardConfig {
    private int cardWidth;
    private int cardHeight;

    private CardConfig(int cardWidth, int cardHeight) {
      this.cardWidth = cardWidth;
      this.cardHeight = cardHeight;
    }

    public int getCardWidth() {
      return cardWidth;
    }

    public int getCardHeight() {
      return cardHeight;
    }

    public void setCardWidth(int cardWidth) {
      this.cardWidth = cardWidth;
    }

    public void setCardHeight(int cardHeight) {
      this.cardHeight = cardHeight;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof CardConfig)) return false;

      CardConfig that = (CardConfig) o;

      if (cardHeight != that.cardHeight) return false;
      if (cardWidth != that.cardWidth) return false;

      return true;
    }
  }

  private class ColumnConfig {
    private CardConfig cardConfig;
    private int lineCount;
    private int lineSpacing;
    private int measureCount;

    private ColumnConfig(CardConfig cardConfig, int lineCount, int lineSpacing) {
      this.cardConfig = cardConfig;
      this.lineCount = lineCount;
      this.lineSpacing = lineSpacing;
      this.measureCount = 0;
    }

    public CardConfig getCardConfig() {
      return cardConfig;
    }

    public int getMeasureCount() {
      return measureCount;
    }

    public boolean canMeasureChild() {
      return lineCount > measureCount;
    }

    public void addMeasureChild() {
      measureCount++;
    }

    public int getLineSpacing() {
      return lineSpacing;
    }

  }

  private int maxColumn = 1;
  private final int columnSpacing;
  private final int minLineSpacing;
  private final int screenMargin;
  private int screenWidth;
  private Scroller mScroller = null;



  @SuppressLint("UseSparseArrays")
  private List<ColumnConfig> columnList = new ArrayList<ColumnConfig>();



  public CardItemContainer(Context context) {
    super(context);
    columnSpacing = 0;
    minLineSpacing = 0;
    screenMargin = 0;
    mScroller = new Scroller(context);
  }

  public CardItemContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardItemContainer, 0, 0);
    columnSpacing = a.getDimensionPixelOffset(R.styleable.CardItemContainer_columnSpacing, 0);
    minLineSpacing =
        a.getDimensionPixelOffset(R.styleable.CardItemContainer_minLineSpacing, 0);
    maxColumn = a.getInt(R.styleable.CardItemContainer_maxColumns, 1);
    screenMargin = a.getDimensionPixelOffset(R.styleable.CardItemContainer_scrollMargin, 0);
    mScroller = new Scroller(context);
    a.recycle();
  }

  public void setMaxColumn(int maxColumn) {
    this.maxColumn = maxColumn;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


    System.out.println("onMeasure!");

    int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    int maxHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

    screenWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

    System.out.println("parent width is = " + screenWidth);

    if (getChildCount() == 0) {
      setMeasuredDimension(0, height);
      return;
    }

    if (columnList == null) {
      columnList = new ArrayList<ColumnConfig>();
    }
    columnList.clear();

    View child = getChildAt(0);
    measureChild(getChildAt(0), widthMeasureSpec, heightMeasureSpec);
    if (child.getMeasuredWidth() == 0) {
      setMeasuredDimension(0, height);
      return;
    }

    for (int i = 0; i < getChildCount(); i++) {
      child = getChildAt(i);
      measureChild(child, widthMeasureSpec, heightMeasureSpec);
      int childWidth = child.getMeasuredWidth();
      int childHeight = child.getMeasuredHeight();

      CardConfig cardConfig = new CardConfig(childWidth, childHeight);

      boolean needNewColumn = true;

      for (int j = 0; j < columnList.size(); j++) {
        ColumnConfig columnConfig = columnList.get(j);
        if (columnConfig != null && cardConfig.equals(columnConfig.getCardConfig())
            && columnConfig.canMeasureChild()) {
          child.setTag(R.string.item_location_column_num, j);
          child.setTag(R.string.item_location_line_num, columnConfig.getMeasureCount());
          System.out.println("child height is =" + childHeight + " , width is = " + childWidth
              + " , location column is = " + j + " , loaction line is = "
              + columnConfig.getMeasureCount());
          columnConfig.addMeasureChild();
          needNewColumn = false;
          cardConfig = null;
          break;
        }
      }

      if (needNewColumn && maxColumn > columnList.size()) {
        int lineCount = ((maxHeight - childHeight) / (childHeight + minLineSpacing)) + 1;
        int lineSpace;
        if (lineCount == 1) {
          lineSpace = 0;
          // measure the large children twice, to make sure it is aligned
          measureChild(child, widthMeasureSpec,
              MeasureSpec.makeMeasureSpec(maxHeight,
                  MeasureSpec.EXACTLY));
          cardConfig.setCardHeight(maxHeight);
        } else {
          lineSpace = (maxHeight - childHeight * lineCount) / (lineCount - 1);
        }
        ColumnConfig newColumn = new ColumnConfig(cardConfig, lineCount, lineSpace);
        child.setTag(R.string.item_location_column_num, columnList.size());
        child.setTag(R.string.item_location_line_num, 0);
        System.out.println("child height is =" + childHeight + " , width is = " + childWidth
            + " , location column is = " + columnList.size() + " , loaction line is = "
            + 0);
        newColumn.addMeasureChild();
        columnList.add(newColumn);
      }

    }

    int width = getPaddingLeft() + getPaddingRight();
    for (ColumnConfig columnConfig : columnList) {
      if (columnConfig != null && columnConfig.getCardConfig() != null) {
        width += columnConfig.getCardConfig().getCardWidth();
      }
    }
    width += columnSpacing * (columnList.size() - 1);

    System.out.println("finally parent width is = " + width);

    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {


    System.out.println("onLayout!");

    final int parentLeft = getPaddingLeft();
    final int parentTop = getPaddingTop();

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child == null) {
        continue;
      }
      Integer columnNum = (Integer) child.getTag(R.string.item_location_column_num);
      if (columnNum == null) {
        continue;
      }
      if (columnNum > columnList.size() - 1 || columnList.get(columnNum) == null) {
        child.setVisibility(View.GONE);
        continue;
      }
      child.setVisibility(View.VISIBLE);
      Integer lineNum = (Integer) child.getTag(R.string.item_location_line_num);
      if (lineNum == null) {
        continue;
      }
      int childHeight = columnList.get(columnNum).getCardConfig().getCardHeight();
      int childWidth = columnList.get(columnNum).getCardConfig().getCardWidth();
      int childLeft = parentLeft + columnNum * columnSpacing;
      int childTop =
          parentTop + lineNum * columnList.get(columnNum).getCardConfig().cardHeight + lineNum
              * columnList.get(columnNum).getLineSpacing();
      for (int j = 0; j < columnNum; j++) {
        childLeft += columnList.get(j).getCardConfig().getCardWidth();
      }
      int childRight = childLeft + childWidth;
      int childBottom = childTop + childHeight;

      System.out.println("child height is =" + childHeight + " , width is = " + childWidth
          + " , location left is = " + childLeft + " , loaction top is = "
          + childTop + " , loaction right is = " + childRight + ", loaction bottom is = "
          + childBottom);
      child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    }
  }


  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
      System.out.println("Current X is = " + mScroller.getCurrX() + " and Y is = "
          + mScroller.getCurrY());
      postInvalidate();
    }
  }

  public void startScrollToNextScreen() {
    System.out.println("from " + getScrollX() + " , dx " + (screenWidth - screenMargin * 2));
    mScroller.startScroll(getScrollX(), 0, screenWidth - screenMargin * 2, 0, Math.abs(500));
    invalidate();
  }

  public void startScrollToPreScreen() {
    System.out.println("from " + getScrollX() + " , dx " + (-screenWidth + screenMargin * 2));
    mScroller.startScroll(getScrollX(), 0, -screenWidth + screenMargin * 2, 0, Math.abs(500));
    invalidate();
  }

}
