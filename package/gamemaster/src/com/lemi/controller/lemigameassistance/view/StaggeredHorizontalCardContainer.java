package com.lemi.controller.lemigameassistance.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.lemi.controller.lemigameassistance.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StaggeredHorizontalCardContainer extends KeepOrderViewGroup {

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
  private int maxHeight;

  @SuppressLint("UseSparseArrays")
  private List<ColumnConfig> columnList = new ArrayList<ColumnConfig>();



  public StaggeredHorizontalCardContainer(Context context) {
    super(context);
    columnSpacing = 0;
    minLineSpacing = 0;
  }

  public StaggeredHorizontalCardContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalCardContainer, 0, 0);
    columnSpacing = a.getDimensionPixelOffset(R.styleable.HorizontalCardContainer_columnSpacing, 0);
    minLineSpacing =
        a.getDimensionPixelOffset(R.styleable.HorizontalCardContainer_minLineSpacing, 0);
    maxColumn = a.getInt(R.styleable.HorizontalCardContainer_maxColumns, 1);
    a.recycle();
  }

  public void setMaxColumn(int maxColumn) {
    this.maxColumn = maxColumn;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    final int parentTop = getPaddingTop();
    final int parentBottom = getPaddingTop();

    if (getChildCount() == 0) {
      setMeasuredDimension(0, maxHeight);
      return;
    }

    if (columnList == null) {
      columnList = new ArrayList<ColumnConfig>();
    }
    columnList.clear();

    View child = getChildAt(0);
    measureChild(getChildAt(0), widthMeasureSpec, heightMeasureSpec);
    if (child.getMeasuredWidth() == 0) {
      setMeasuredDimension(0, maxHeight);
      return;
    }

    if (!checkOriginalChildListValid(originalChildList)) {
      originalChildList = getOriginalChildList();
    }

    if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
      for (int i = 0; i < getChildCount(); i++) {
        child = getChildAt(i);
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        int childHeight = child.getMeasuredHeight();

        maxHeight =
            childHeight + parentTop + parentBottom > maxHeight ? childHeight + parentTop
                + parentBottom : maxHeight;

      }
    } else {
      maxHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
    }

    for (int i = 0; i < getChildCount(); i++) {
      child = getChildInOriginal(i);
      if (child == null) {
        continue;
      }

      measureChild(child, widthMeasureSpec, heightMeasureSpec);
      int childWidth = child.getMeasuredWidth();
      int childHeight = child.getMeasuredHeight();

      CardConfig cardConfig = new CardConfig(childWidth, childHeight);

      boolean needNewColumn = true;

      for (int j = 0; j < columnList.size(); j++) {
        ColumnConfig columnConfig = columnList.get(j);
        if (columnConfig != null && cardConfig.equals(columnConfig.getCardConfig())
            && columnConfig.canMeasureChild()) {
          child.setTag(R.id.item_location_column_num, j);
          child.setTag(R.id.item_location_line_num, columnConfig.getMeasureCount());
          columnConfig.addMeasureChild();
          needNewColumn = false;
          cardConfig = null;
          break;
        }
      }

      if (needNewColumn && maxColumn > columnList.size()) {
        int lineCount =
            ((maxHeight - parentBottom - parentTop - childHeight) / (childHeight + minLineSpacing)) + 1;
        int lineSpace;
        if (lineCount == 1) {
          lineSpace = 0;
        } else {
          lineSpace =
              (maxHeight - parentBottom - parentTop - childHeight * lineCount) / (lineCount - 1);
        }
        ColumnConfig newColumn = new ColumnConfig(cardConfig, lineCount, lineSpace);
        child.setTag(R.id.item_location_column_num, columnList.size());
        child.setTag(R.id.item_location_line_num, 0);
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


    setMeasuredDimension(width, maxHeight);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    final int parentLeft = getPaddingLeft();
    final int parentTop = getPaddingTop();

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildInOriginal(i);
      if (child == null) {
        continue;
      }
      Integer columnNum = (Integer) child.getTag(R.id.item_location_column_num);
      if (columnNum == null) {
        continue;
      }
      if (columnNum > columnList.size() - 1 || columnList.get(columnNum) == null) {
        child.setVisibility(View.GONE);
        continue;
      }
      child.setVisibility(View.VISIBLE);
      Integer lineNum = (Integer) child.getTag(R.id.item_location_line_num);
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
      child.layout(childLeft, childTop, childRight, childBottom);
    }
  }

}
