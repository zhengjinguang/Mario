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
public class VerticalCardContainer extends KeepOrderViewGroup implements GroupItem {

  private int maxLines = 1;
  private final int lineSpacing;
  private final int minColumnSpacing;
  private int lineCount = 0;
  private int columnSpacing = 0;
  private int columnCount = 0;
  private int childWidth;


  private SparseArray<Integer> childHeightMap = new SparseArray<>();


  public VerticalCardContainer(Context context) {
    super(context);
    lineSpacing = 0;
    minColumnSpacing = 0;
  }

  public VerticalCardContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalCardContainer, 0, 0);
    lineSpacing = a.getDimensionPixelOffset(R.styleable.VerticalCardContainer_lineSpacing, 0);
    minColumnSpacing =
        a.getDimensionPixelOffset(R.styleable.VerticalCardContainer_minColumnSpacing, 0);
    maxLines = a.getInt(R.styleable.VerticalCardContainer_maxLines, 1);
    a.recycle();
  }

  public void setMaxLines(int maxLines) {
    this.maxLines = maxLines;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();

    if (getChildCount() == 0) {
      setMeasuredDimension(width, 0);
      return;
    }
    View child = getChildAt(0);
    measureChild(child, widthMeasureSpec, heightMeasureSpec);
    // we assume that children's width is same
    childWidth = child.getMeasuredWidth();
    childHeightMap.clear();
    childHeightMap.put(0, child.getMeasuredHeight());
    if (childWidth == 0) {
      setMeasuredDimension(width, 0);
      return;
    }

    if (!checkOriginalChildListValid(originalChildList)) {
      originalChildList = getOriginalChildList();
    }

    columnCount = (maxWidth - minColumnSpacing) / (childWidth + minColumnSpacing);
    if (columnCount != 1) {
      columnSpacing = (maxWidth - childWidth * columnCount) / (columnCount - 1);
    } else {
      columnSpacing = minColumnSpacing;
    }
    lineCount = (int) Math.ceil(((double) getChildCount()) / columnCount);
    lineCount = Math.min(lineCount, maxLines);

    boolean needReMeasure = false;

    for (int i = 1; i < getChildCount(); i++) {
      int lineNum = i / columnCount;
      if (lineNum >= lineCount) {
        break;
      }
      child = getChildInOriginal(i);
      measureChild(child, widthMeasureSpec, heightMeasureSpec);
      Integer childHeight = childHeightMap.get(lineNum);
      if (childHeight == null) {
        childHeightMap.put(lineNum, child.getMeasuredHeight());
      } else if (child.getMeasuredHeight() > childHeight) {
        needReMeasure = true;
        childHeightMap.put(lineNum, child.getMeasuredHeight());
      }
    }
    if (needReMeasure) {
      for (int i = 0; i < getChildCount(); i++) {
        int lineNum = i / columnCount;
        if (lineNum >= lineCount) {
          break;
        }
        child = getChildInOriginal(i);
        measureChild(child, widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(childHeightMap.get(lineNum),
                MeasureSpec.EXACTLY));
      }
    }
    int height = getPaddingTop() + getPaddingBottom();
    for (int i = 0; i < childHeightMap.size(); i++) {
      height += childHeightMap.get(i);
    }
    height += lineSpacing * (lineCount - 1);
    width =
        columnCount * childWidth + (columnCount - 1) * columnSpacing + getPaddingLeft()
            + getPaddingRight();
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int parentLeft = getPaddingLeft();
    final int parentTop = getPaddingTop();

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildInOriginal(i);
      int lineNum = i / columnCount;
      if (lineNum >= lineCount) {
        child.setVisibility(View.GONE);
        continue;
      }
      child.setVisibility(View.VISIBLE);
      int columnNum = i % columnCount;
      int childHeight = childHeightMap.get(lineNum);
      int childLeft = parentLeft + columnNum * (childWidth + columnSpacing);
      int childTop = parentTop + lineNum * lineSpacing;
      for (int j = 0; j < lineNum; j++) {
        childTop += childHeightMap.get(j);
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
    return childIndex / columnCount == 0;
  }

  @Override
  public boolean isOnBottomEdge(View child) {
    int childIndex = indexOfOriginalChild(child);
    if (childIndex == -1) {
      return false;
    }
    return childIndex / columnCount == lineCount - 1;
  }

  @Override
  public boolean isOnLeftEdge(View child) {
    int childIndex = indexOfOriginalChild(child);
    if (childIndex == -1) {
      return false;
    }
    return childIndex % columnCount == 0;
  }

  @Override
  public boolean isOnRightEdge(View child) {
    int childIndex = indexOfOriginalChild(child);
    if (childIndex == -1) {
      return false;
    }
    return childIndex % columnCount == columnCount - 1;
  }

  @Override
  public void focusAfterUpScroll(int lastIndex) {
    int columnIndex = lastIndex % columnCount;
    int needFocusIndex = ((lineCount - 1) * columnCount) + columnIndex;
    if (needFocusIndex >= getChildCount()) {
      needFocusIndex = getChildCount() - 1;
    }
    View child = getChildInOriginal(needFocusIndex);
    if (child != null) {
      child.requestFocus();
    }
  }

  @Override
  public void focusAfterDownScroll(int lastIndex) {
    int columnIndex = lastIndex % columnCount;
    int needFocusIndex = columnIndex;
    if (needFocusIndex >= getChildCount()) {
      needFocusIndex = getChildCount() - 1;
    }
    View child = getChildInOriginal(needFocusIndex);
    if (child != null) {
      child.requestFocus();
    }
  }

  @Override
  public void focusAfterLeftScroll(int lastIndex) {
    int lineIndex = lastIndex / columnCount;
    int needFocusIndex = (columnCount * (lineIndex + 1)) - 1;
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
    int lineIndex = lastIndex / columnCount;
    int needFocusIndex = columnCount * lineIndex;
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
