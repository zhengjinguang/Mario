package com.lemi.controller.lemigameassistance.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

public class NetworkCheckUtil {



  public static void setNetworkIcon(Context context, final ImageView icon) {
    ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    if (activeNetwork == null) {
      icon.setImageLevel(0);
    } else {
      boolean isConnected = activeNetwork.isConnectedOrConnecting();
      int type = activeNetwork.getType();
      if (isConnected) {
        if (type == ConnectivityManager.TYPE_ETHERNET) {
          icon.setImageLevel(1);
        } else if (type == ConnectivityManager.TYPE_WIFI) {
          icon.setImageLevel(2);
        }
      } else {
        icon.setImageLevel(0);
      }
    }
  }
}
