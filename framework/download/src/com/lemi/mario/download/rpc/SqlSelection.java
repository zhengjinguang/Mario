package com.lemi.mario.download.rpc;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlSelection {

  public StringBuilder mWhereClause = new StringBuilder();
  public List<String> mParameters = new ArrayList<String>();

  public <T> void appendClause(String newClause, final T... parameters) {
    if (TextUtils.isEmpty(newClause)) {
      return;
    }
    if (mWhereClause.length() != 0) {
      mWhereClause.append(" AND ");
    }
    mWhereClause.append("(");
    mWhereClause.append(newClause);
    mWhereClause.append(")");
    if (parameters != null) {
      for (Object parameter : parameters) {
        mParameters.add(parameter.toString());
      }
    }
  }

  public String getSelection() {
    return mWhereClause.toString();
  }

  public String[] getParameters() {
    String[] array = new String[mParameters.size()];
    return mParameters.toArray(array);
  }
}
