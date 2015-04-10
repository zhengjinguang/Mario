package com.lemi.controller.lemigameassistance.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.lemi.controller.lemigameassistance.R;
import com.lemi.controller.lemigameassistance.focus.view.GroupItem;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class HorizontalCardContainer extends KeepOrderViewGroup implements GroupItem {

  protected int maxColumn = 1;

  private final int columnSpacing;
  private final int minLineSpacing;
  private int columnCount = 0;
  private int lineSpacing = 0;
  private int lineCount = 0;
  private int childHeight;


  private SparseArray<Integer> childWidthMap = new SparseArray<>();


  public HorizontalCardContainer(Context context) {
    super(context);
    columnSpacing = 0;
    minLineSpacing = 0;
  }

  public HorizontalCardContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalCardContainer, 0, 0);
    columnSpacing = a.getDimensionPixelOffset(R.styleable.HorizontalCardContainer_columnSpacing, 0);
    minLineSpacing =
        a.getDimensionPixelOffset(R.styleable.HorizontalCardContainer_minLineSpacing, 0);
    maxColumn = a.getInt(R.styleable.HorizontalCardContainer_maxColumns, 1);
    a.recycle();
  }

  public void setMaxColumns(int maxColumn) {
    this.maxColumn = maxColumn;
  }


  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    int maxHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

    if (getChildCount() == 0) {
      setMeasuredDimension(0, height);
      return;
    }
    View child = getChildAt(0);
    measureChild(child, widthMeasureSpec, heightMeasureSpec);
    // we assume that children's width is same
    childHeight = child.getMeasuredHeight();
    childWidthMap.clear();
    childWidthMap.put(0, child.getMeasuredWidth());
    if (childHeight == 0) {
      setMeasuredDimension(0, height);
      return;
    }

    if (!checkOriginalChildListValid(originalChildList)) {
      originalChildList = getOriginalChildList();
    }

    lineCount = (maxHeight - minLineSpacing) / (childHeight + minLineSpacing);
    if (lineCount != 1) {
      lineSpacing = (maxHeight - childHeight * lineCount) / (lineCount - 1);
    } else {
      lineSpacing = minLineSpacing;
    }
    columnCount = (int) Math.ceil(((double) getChildCount()) / lineCount);
    columnCount = Math.min(columnCount, maxColumn);

    boolean needReMeasure = false;


    for (int i = 1; i < getChildCount(); i++) {
      int columnNum = i / lineCount;
      if (columnNum >= columnCount) {
        break;
      }
      child = getChildInOriginal(i);
      measureChild(child, widthMeasureSpec, heightMeasureSpec);
      Integer childWidth = childWidthMap.get(columnNum);
      if (childWidth == null) {
        childWidthMap.put(columnNum, child.getMeasuredWidth());
      } else if (child.getMeasuredWidth() > childWidth) {
        needReMeasure = true;
        childWidthMap.put(columnNum, child.getMeasuredWidth());
      }
    }
    if (needReMeasure) {
      for (int i = 0; i < getChildCount(); i++) {
        int columnNum = i / lineCount;
        if (columnNum >= columnCount) {
          break;
        }
        child = getChildInOriginal(i);
        measureChild(child, widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(childWidthMap.get(columnNum),
                MeasureSpec.EXACTLY));
      }
    }
    int width = getPaddingLeft() + getPaddingRight();
    for (int i = 0; i < childWidthMap.size(); i++) {
      width += childWidthMap.get(i);
    }
    width += columnSpacing * (columnCount - 1);
    height =
        lineCount * childHeight + (lineCount - 1) * lineSpacing + getPaddingTop()
            + getPaddingBottom();
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int parentLeft = getPaddingLeft();
    final int parentTop = getPaddingTop();

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildInOriginal(i);
      int columnNum = i / lineCount;
      if (columnNum >= columnCount) {
        child.setVisibility(View.GONE);
        continue;
      }
      child.setVisibility(View.VISIBLE);
      int lineNum = i % lineCount;
      int childWidth = childWidthMap.get(columnNum);

      int childTop = parentTop + lineNum * (childHeight + lineSpacing);
      int childLeft = parentLeft + columnNum * columnSpacing;
      for (int j = 0; j < columnNum; j++) {
        childLeft += childWidthMap.get(j);
      }
      child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    }
  }


  /**
   * GroupItem interface
   */

  @Override
  public boolean isOnTopEdge(View child) {
    int childIndex = indexOfOriginalChild(child);
    if (childIndex == -1) {
      return false;
    }
    return childIndex % lineCount == 0;
  }

  @Override
  public boolean isOnBottomEdge(View child) {
    int childIndex = indexOfOriginalChild(child);
    if (childIndex == -1) {
      return false;
    }
    return childIndex % lineCount == lineCount - 1;
  }

  @Override
  public boolean isOnLeftEdge(View child) {
    int childIndex = indexOfOriginalChild(child);
    if (childIndex == -1) {
      return false;
    }
    return childIndex / lineCount == 0;
  }

  @Override
  public boolean isOnRightEdge(View child) {
    int childIndex = indexOfOriginalChild(child);
    if (childIndex == -1) {
      return false;
    }
    return childIndex / lineCount == columnCount - 1;
  }

  @Override
  public void focusAfterUpScroll(int lastIndex) {
    int columnIndex = lastIndex / lineCount;
    int needFocusIndex = (lineCount * (columnIndex + 1)) - 1;
    View child = getChildInOriginal(needFocusIndex);
    if (child != null) {
      child.requestFocus();
    }
  }

  @Override
  public void focusAfterDownScroll(int lastIndex) {
    int columnIndex = lastIndex / lineCount;
    int needFocusIndex = lineCount * columnIndex;
    View child = getChildInOriginal(needFocusIndex);
    if (child != null) {
      child.requestFocus();
    }
  }

  @Override
  public void focusAfterLeftScroll(int lastIndex) {
    int lineIndex = lastIndex % lineCount;
    int needFocusIndex = (lineCount * (columnCount - 1)) + lineIndex;
    if (needFocusIndex >= getChildCount()) {
      needFocusIndex = getChildCount() - 1;
    }
    View child = getChildInOriginal(needFocusIndex);
    if (child != null) {
      child.requestFocus();
    }
  }

  @Override
  public void focusAfterRightScroll(int lastIndex) {
    int lineIndex = lastIndex % lineCount;
    int needFocusIndex = lineIndex;
    if (needFocusIndex >= getChildCount()) {
      needFocusIndex = getChildCount() - 1;
    }
    View child = getChildInOriginal(needFocusIndex);
    if (child != null) {
      child.requestFocus();
    }
  }

  @Override
  public int getIndex(View child) {
    return indexOfOriginalChild(child);
  }
}
