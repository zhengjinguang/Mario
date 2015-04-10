/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.lemi.controller.lemigameassistance.view;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lemi.controller.lemigameassistance.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class TextClock extends TextView {

  public static final CharSequence DEFAULT_FORMAT_12_HOUR = "h:mm a";
  private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
  public static final CharSequence DEFAULT_FORMAT_24_HOUR = "H:mm";

  private CharSequence mFormat12;
  private CharSequence mFormat24;

  private CharSequence mFormat;
  private boolean mHasSeconds;

  private boolean mAttached;

  private Calendar mTime;
  private String mTimeZone;

  private final ContentObserver mFormatChangeObserver = new ContentObserver(new Handler()) {
    @Override
    public void onChange(boolean selfChange) {
      chooseFormat();
      onTimeChanged();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
      chooseFormat();
      onTimeChanged();
    }
  };

  private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
        final String timeZone = intent.getStringExtra("time-zone");
        createTime(timeZone);
      }
      onTimeChanged();
    }
  };

  private final Runnable mTicker = new Runnable() {
    public void run() {
      onTimeChanged();

      long now = SystemClock.uptimeMillis();
      long next = now + (1000 - now % 1000);

      getHandler().postAtTime(mTicker, next);
    }
  };

  /**
   * Creates a new clock using the default patterns {@link #DEFAULT_FORMAT_24_HOUR} and
   * {@link #DEFAULT_FORMAT_12_HOUR} respectively for the 24-hour and 12-hour modes.
   * 
   * @param context The Context the view is running in, through which it can
   *          access the current theme, resources, etc.
   */
  @SuppressWarnings("UnusedDeclaration")
  public TextClock(Context context) {
    super(context);
    init();
  }

  /**
   * Creates a new clock inflated from XML. This object's properties are
   * intialized from the attributes specified in XML.
   * 
   * This constructor uses a default style of 0, so the only attribute values
   * applied are those in the Context's Theme and the given AttributeSet.
   * 
   * @param context The Context the view is running in, through which it can
   *          access the current theme, resources, etc.
   * @param attrs The attributes of the XML tag that is inflating the view
   */
  @SuppressWarnings("UnusedDeclaration")
  public TextClock(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  /**
   * Creates a new clock inflated from XML. This object's properties are
   * intialized from the attributes specified in XML.
   * 
   * @param context The Context the view is running in, through which it can
   *          access the current theme, resources, etc.
   * @param attrs The attributes of the XML tag that is inflating the view
   * @param defStyle The default style to apply to this view. If 0, no style
   *          will be applied (beyond what is included in the theme). This may
   *          either be an attribute resource, whose value will be retrieved
   *          from the current theme, or an explicit style resource
   */
  public TextClock(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextClock, defStyle, 0);
    try {
      mFormat12 = a.getText(R.styleable.TextClock_format12Hour);
      mFormat24 = a.getText(R.styleable.TextClock_format24Hour);
      mTimeZone = a.getString(R.styleable.TextClock_timeZone);
    } finally {
      a.recycle();
    }

    init();
  }

  private void init() {
    if (mFormat12 == null || mFormat24 == null) {
      if (mFormat12 == null) {
        mFormat12 = DEFAULT_FORMAT_12_HOUR;
      }
      if (mFormat24 == null) {
        mFormat24 = DEFAULT_FORMAT_24_HOUR;
      }
    }

    createTime(mTimeZone);
    // Wait until onAttachedToWindow() to handle the ticker
    chooseFormat(false);
  }

  private void createTime(String timeZone) {
    if (timeZone != null) {
      mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
    } else {
      mTime = Calendar.getInstance();
    }
  }

  /**
   * Returns the formatting pattern used to display the date and/or time
   * in 12-hour mode. The formatting pattern syntax is described in {@link DateFormat}.
   * 
   * @return A {@link CharSequence} or null.
   * 
   * @see #setFormat12Hour(CharSequence)
   * @see #is24HourModeEnabled()
   */
  public CharSequence getFormat12Hour() {
    return mFormat12;
  }

  public void setFormat12Hour(CharSequence format) {
    mFormat12 = format;

    chooseFormat();
    onTimeChanged();
  }

  /**
   * Returns the formatting pattern used to display the date and/or time
   * in 24-hour mode. The formatting pattern syntax is described in {@link DateFormat}.
   * 
   * @return A {@link CharSequence} or null.
   * 
   * @see #setFormat24Hour(CharSequence)
   * @see #is24HourModeEnabled()
   */
  public CharSequence getFormat24Hour() {
    return mFormat24;
  }

  public void setFormat24Hour(CharSequence format) {
    mFormat24 = format;

    chooseFormat();
    onTimeChanged();
  }

  public boolean is24HourModeEnabled() {
    return DateFormat.is24HourFormat(getContext());
  }

  /**
   * Indicates which time zone is currently used by this view.
   * 
   * @return The ID of the current time zone or null if the default time zone,
   *         as set by the user, must be used
   * 
   * @see TimeZone
   * @see java.util.TimeZone#getAvailableIDs()
   * @see #setTimeZone(String)
   */
  public String getTimeZone() {
    return mTimeZone;
  }

  /**
   * Sets the specified time zone to use in this clock. When the time zone
   * is set through this method, system time zone changes (when the user
   * sets the time zone in settings for instance) will be ignored.
   * 
   * @param timeZone The desired time zone's ID as specified in {@link TimeZone} or null to user the
   *          time zone specified by the user
   *          (system time zone)
   * 
   * @see #getTimeZone()
   * @see java.util.TimeZone#getAvailableIDs()
   * @see TimeZone#getTimeZone(String)
   * 
   * @attr ref android.R.styleable#TextClock_timeZone
   */
  public void setTimeZone(String timeZone) {
    mTimeZone = timeZone;

    createTime(timeZone);
    onTimeChanged();
  }

  /**
   * Selects either one of {@link #getFormat12Hour()} or {@link #getFormat24Hour()} depending on
   * whether the user has selected 24-hour format.
   * 
   * Calling this method does not schedule or unschedule the time ticker.
   */
  private void chooseFormat() {
    chooseFormat(true);
  }

  /**
   * Returns the current format string. Always valid after constructor has
   * finished, and will never be {@code null}.
   * 
   * @hide
   */
  public CharSequence getFormat() {
    return mFormat;
  }

  /**
   * Selects either one of {@link #getFormat12Hour()} or {@link #getFormat24Hour()} depending on
   * whether the user has selected 24-hour format.
   * 
   * @param handleTicker true if calling this method should schedule/unschedule the
   *          time ticker, false otherwise
   */
  private void chooseFormat(boolean handleTicker) {
    final boolean format24Requested = is24HourModeEnabled();
    if (format24Requested) {
      mFormat = abc(mFormat24, mFormat12, DEFAULT_FORMAT_24_HOUR);
    } else {
      mFormat = abc(mFormat12, mFormat24, DEFAULT_FORMAT_12_HOUR);
    }
    boolean hadSeconds = mHasSeconds;
    mHasSeconds = false;

    if (handleTicker && mAttached && hadSeconds != mHasSeconds) {
      if (hadSeconds)
        getHandler().removeCallbacks(mTicker);
      else
        mTicker.run();
    }
  }

  /**
   * Returns a if not null, else return b if not null, else return c.
   */
  private static CharSequence abc(CharSequence a, CharSequence b, CharSequence c) {
    return a == null ? (b == null ? c : b) : a;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (!mAttached) {
      mAttached = true;

      registerReceiver();
      registerObserver();

      createTime(mTimeZone);

      if (mHasSeconds) {
        mTicker.run();
      } else {
        onTimeChanged();
      }
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (mAttached) {
      unregisterReceiver();
      unregisterObserver();

      getHandler().removeCallbacks(mTicker);

      mAttached = false;
    }
  }

  private void registerReceiver() {
    final IntentFilter filter = new IntentFilter();

    filter.addAction(Intent.ACTION_TIME_TICK);
    filter.addAction(Intent.ACTION_TIME_CHANGED);
    filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

    getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
  }

  private void registerObserver() {
    final ContentResolver resolver = getContext().getContentResolver();
    resolver.registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);
  }

  private void unregisterReceiver() {
    getContext().unregisterReceiver(mIntentReceiver);
  }

  private void unregisterObserver() {
    final ContentResolver resolver = getContext().getContentResolver();
    resolver.unregisterContentObserver(mFormatChangeObserver);
  }

  private void onTimeChanged() {
    mTime.setTimeInMillis(System.currentTimeMillis());
    setText(simpleDateFormat.format(mTime.getTime()));
  }
}
