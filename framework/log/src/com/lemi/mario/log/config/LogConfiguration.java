package com.lemi.mario.log.config;

import android.content.Context;

import com.lemi.mario.log.sender.LogSender;

import java.util.Map;

/**
 * Application can implement this and pass it as parameter to build
 * {@link com.lemi.mario.log.sender.LogSender} and {@link com.lemi.mario.log.LogReporter}
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public interface LogConfiguration {

  /**
   * @return the profile name for this log reporter.
   */
  String getProfileName();

  /**
   * This is for old implementation in which the header parameters will be set as header when a
   * bunch of log event is sent to log server.
   * These parameters should also be stable.
   * 
   * @return a map contains the header parameters.
   */
  Map<String, String> buildHeaderParams(Context context);

  /**
   * Set the common parameters which all log events will have here.
   * These parameters should be stable and never change.
   * 
   * @return a map contains the stable common parameters.
   */
  Map<String, String> buildStableCommonParams(Context context);

  /**
   * Set the common parameters which all log events will have here.
   * These parameters can be changeable and will rebuild when the log event happens.
   * such as: network type.
   * 
   * @return a map contains the volatile common parameter.
   */
  Map<String, String> buildVolatileCommonParams(Context context);

  /**
   * Set the send policy in wifi.
   * 
   * @return the send policy in wifi
   */
  LogSender.SenderPolicyModel getWifiSendPolicy();

  /**
   * Set the send policy in mobile network.
   * 
   * @return the send policy in mobile network.
   */
  LogSender.SenderPolicyModel getMobileSendPolicy();
}
