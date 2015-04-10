package com.lemi.mario.base.utils;

import java.util.Calendar;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class TimeUtils {
  public static final int MILLIS_OF_DAY = 24 * 60 * 60 * 1000;
  public static final int DAYS_OF_WEEK = 7;

  private TimeUtils() {}

  public static long getCurrentTime() {
    return Calendar.getInstance().getTime().getTime();
  }

  public static long getTodayTime() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime().getTime();
  }

  public static int getDaysInMonth(int month, int year) {
    switch (month) {
      case Calendar.JANUARY:
      case Calendar.MARCH:
      case Calendar.MAY:
      case Calendar.JULY:
      case Calendar.AUGUST:
      case Calendar.OCTOBER:
      case Calendar.DECEMBER:
        return 31;
      case Calendar.APRIL:
      case Calendar.JUNE:
      case Calendar.SEPTEMBER:
      case Calendar.NOVEMBER:
        return 30;
      case Calendar.FEBRUARY:
        return isLeapYear(year) ? 29 : 28;
      default:
        throw new IllegalArgumentException("Invalid Month");
    }
  }

  public static boolean isLeapYear(int year) {
    boolean isLeapYear = false;
    if (year % 400 == 0) {
      isLeapYear = true;
    } else if (year % 4 == 0 && year % 100 != 0) {
      isLeapYear = true;
    }
    return isLeapYear;
  }

  public static boolean isSameDay(Calendar day1, Calendar day2) {
    if (day1 == null || day2 == null) {
      return false;
    }
    return day1.get(Calendar.YEAR) == day2.get(Calendar.YEAR)
        && day1.get(Calendar.MONTH) == day2.get(Calendar.MONTH)
        && day1.get(Calendar.DAY_OF_MONTH) == day2.get(Calendar.DAY_OF_MONTH);
  }

  public static boolean isSameDay(Calendar day1, int year, int monthOfYear, int dayOfMonth) {
    if (day1 == null) {
      return false;
    }
    return day1.get(Calendar.YEAR) == year
        && day1.get(Calendar.MONTH) == monthOfYear
        && day1.get(Calendar.DAY_OF_MONTH) == dayOfMonth;
  }
}
