package com.lemi.mario.download.rpc;

import android.text.TextUtils;
import android.util.Pair;

import com.lemi.mario.download.rpc.DownloadConstants.ResourceType;
import com.lemi.mario.download.rpc.InnerDownloadFilter.DownloadFilterBuilder.FilterItem;

import java.util.ArrayList;
import java.util.List;

public class InnerDownloadFilter {

  private static final int VISIBLE = 1;
  private static final int NOT_VISIBLE = 0;
  private List<FilterItem> filters = new ArrayList<FilterItem>();

  public InnerDownloadFilter(List<FilterItem> filters) {
    this.filters = filters;
  }

  public enum FilterArea {

    CONTENT(DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_TYPE),
    STATUS(DownloadConstants.Database.COLUMNS.COLUMN_STATUS),
    SIZE(DownloadConstants.Database.COLUMNS.COLUMN_TOTAL_BYTES),
    VISIBLE(DownloadConstants.Database.COLUMNS.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI),
    EXTRA(DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_EXTRAS);

    private final String column;

    private FilterArea(String column) {
      this.column = column;
    }

    public String getColumnName() {
      return this.column;
    }
  }

  public List<FilterItem> getFilters() {
    return this.filters;
  }

  public enum Operator {
    MORE, LESS, EQUAL, LIKE
  }

  public static DownloadFilterBuilder newBuilder() {
    return new DownloadFilterBuilder();
  }

  public static final class DownloadFilterBuilder {

    private List<FilterItem> filters = new ArrayList<FilterItem>();
    private boolean hasSizeInfo = false;

    private DownloadFilterBuilder() {};

    public DownloadFilterBuilder setSizeFilter(Operator operator, int value) {
      List<String> values = new ArrayList<String>();
      values.add(String.valueOf(value));
      filters.add(new FilterItem(FilterArea.SIZE, operator, values));
      hasSizeInfo = true;
      return this;
    }

    public DownloadFilterBuilder setTypeFilter(List<ResourceType> types) {
      List<String> values = new ArrayList<String>();
      for (ResourceType type : types) {
        values.add(String.valueOf(type.ordinal()));
      }
      filters.add(new FilterItem(FilterArea.CONTENT, Operator.EQUAL, values));
      return this;
    }

    public DownloadFilterBuilder setStatusFilter(List<Integer> statuses) {
      List<String> values = new ArrayList<String>();
      for (Integer status : statuses) {
        values.add(String.valueOf(status));
      }
      filters.add(new FilterItem(FilterArea.STATUS, Operator.EQUAL, values));
      return this;
    }

    public DownloadFilterBuilder setVisibilityFilter(boolean visible) {
      List<String> visibilityList = new ArrayList<String>(1);
      if (visible) {
        visibilityList.add(String.valueOf(VISIBLE));
      } else {
        visibilityList.add(String.valueOf(NOT_VISIBLE));
      }
      filters.add(new FilterItem(FilterArea.VISIBLE, Operator.EQUAL, visibilityList));
      return this;
    }

    public DownloadFilterBuilder setExtraFilter(List<Pair<String, String>> extras) {
      List<String> extraRegexList = new ArrayList<String>();
      for (Pair<String, String> pair : extras) {
        if (!TextUtils.isEmpty(pair.first) && !TextUtils.isEmpty(pair.second)) {
          StringBuilder sb = new StringBuilder();
          sb.append("\'%\"").append(pair.first).append("\":\"").append(pair.second).append("\"%\'");
          extraRegexList.add(sb.toString());
        }
      }

      filters.add(new FilterItem(FilterArea.EXTRA, Operator.LIKE, extraRegexList));
      return this;
    }

    public class FilterItem {

      public FilterArea columnName;
      public Operator type;
      public List<String> value;

      FilterItem(FilterArea column, Operator type, List<String> values) {
        this.columnName = column;
        this.type = type;
        this.value = values;
      }
    }

    public InnerDownloadFilter build() {
      if (hasSizeInfo) {
        for (FilterItem item : filters) {
          if (item.columnName.equals(FilterArea.CONTENT.getColumnName())
              || item.columnName.equals(FilterArea.STATUS.getColumnName())) {
            if (item.value.size() > 1) {
              throw new IllegalArgumentException(
                  "when has size info, content or status value can't be more than one");
            }
          }
        }
      }
      return new InnerDownloadFilter(filters);
    }

  }

}
