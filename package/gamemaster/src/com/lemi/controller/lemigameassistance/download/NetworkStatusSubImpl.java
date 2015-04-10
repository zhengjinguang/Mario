package com.lemi.controller.lemigameassistance.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.lemi.mario.base.utils.NetworkUtil;
import com.lemi.mario.download.listener.NetworkStatusStub;

/**
 * implement NetworkStatusStub used to get network status.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class NetworkStatusSubImpl implements NetworkStatusStub {

  private Context context;

  public NetworkStatusSubImpl(final Context context) {
    this.context = context;
  }

  /**
   * Check current network status.
   * 
   * @return able to download
   */
  @Override
  public NetworkStatus getCurrentNetWorkStatus() {

    ConnectivityManager connectivity = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivity == null) {
      return NetworkStatus.NETWORK_NO_CONNECTION;
    }

    NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
    if (activeInfo == null) {
      return NetworkStatus.NETWORK_NO_CONNECTION;
    } else if (activeInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
      return NetworkStatus.NETWORK_MOBILE_CONNECTED;
    } else if (activeInfo.getType() == ConnectivityManager.TYPE_WIFI) {
      if (NetworkUtil.checkWifiIsHotSpot(context)) {
        return NetworkStatus.NETWORK_MOBILE_CONNECTED;
      } else {
        return NetworkStatus.NETWORK_WIFI_CONNECTED;
      }
    } else if (activeInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
      return NetworkStatus.NETWORK_WIFI_CONNECTED;
    }
    return NetworkStatus.NETWORK_NO_CONNECTION;
  }

}
