package com.lemi.mario.download.listener;

/**
 * Interface used to confirm current network is able to download.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface NetworkStatusStub {

  /**
   * Enum for network status.
   */
  public enum NetworkStatus {
    NETWORK_USB_CONNECTED,
    NETWORK_WIFI_CONNECTED,
    NETWORK_MOBILE_CONNECTED,
    NETWORK_NO_CONNECTION
  }

  /**
   * Function to get current status.
   *
   * @return network status
   */
  NetworkStatus getCurrentNetWorkStatus();
}
